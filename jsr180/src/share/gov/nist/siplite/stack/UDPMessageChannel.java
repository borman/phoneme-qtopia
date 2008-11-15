/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
/*
 */

package gov.nist.siplite.stack;

import java.io.*;
import java.util.*;

import javax.microedition.io.*;

import com.sun.j2me.log.LogChannels;
import com.sun.j2me.log.Logging;

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.parser.*;

/**
 * This is the UDP Message handler that gets created when a UDP message
 * needs to be processed. The message is processed by creating a String
 * Message parser and invoking it on the message read from the UPD socket.
 * The parsed structure is handed off via a SIP stack request for further
 * processing. This stack structure isolates the message handling logic
 * from the mechanics of sending and recieving messages (which could
 * be either udp or tcp.
 * Acknowledgement:
 * Kim Kirby of Keyvoice suggested that duplicate checking should be added
 * to the stack.
 *
 *@see gov.nist.siplite.parser.StringMsgParser
 *@see gov.nist.siplite.stack.SIPServerRequestInterface
 *@since v1.0
 *
 */
public class UDPMessageChannel
extends MessageChannel
implements ParseExceptionListener, Runnable {
    /** SIP Stack structure for this channel.  */
    protected SIPMessageStack stack;
    /** Sender address (from getPeerName()) */
    private String myAddress;
    /** Local port number. */
    private int myPort;
    /** Remote host address. */
    private String peerAddress;
    /** Remote port number. */
    private int peerPort;
    /** Remote connection transport. */
    private String peerProtocol;
    /* Inbound message transport. */
    // private String receiverProtocol;
    /** Raw message contents. */
    private byte[] msgBytes;
    /** Input data length. */
    private int packetLength;
    /** Datagram connection for transport. */
    private DatagramConnection datagramConnection;
    /** Current serevr request message. */
    private Datagram incomingPacket;
    /** Pool of duplicate messages. */
    protected static Hashtable duplicates;
    /* Current SIP message. */
    // private Message sipMessage;
    /** Timestamp of current inbound message. */
    private long receptionTime;

    /**
     * Constructor - takes a datagram packet and a stack structure
     * Extracts the address of the other from the datagram packet and
     * stashes away the pointer to the passed stack structure.
     * @param packet is the UDP Packet that contains the request.
     * @param sipStack stack is the shared SipStack structure
     * @param messageProcessor the UDP input message channel
     */
    public UDPMessageChannel(Datagram packet,
                             SIPMessageStack sipStack,
                             MessageProcessor messageProcessor) {
        incomingPacket = packet;
        stack = sipStack;
        // format: "protocol://address:port"
        String address = packet.getAddress();

        try {
            int firstColon = address.indexOf("://");
            int secondColon = address.indexOf(":", firstColon+1);
            peerAddress = address.substring(firstColon+3, secondColon);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                             "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                             "sender address: " + peerAddress);
            }

            String portString = address.substring(secondColon+1);
            this.peerPort = Integer.parseInt(portString);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                             "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                             "sender port: " + peerPort);
            }
        } catch (NumberFormatException e) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                             "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                             "exception raised: " + e);
            }

            this.peerPort = -1;
        }

        packetLength = packet.getLength();

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                           "packet length: " + packetLength);
        }

        byte[] bytes = packet.getData();
        msgBytes = new byte[packetLength];
        for (int i = 0; i < packetLength; i++) {
            msgBytes[i] = bytes[i];
        }

        String msgString = new String(msgBytes, 0, packetLength);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                           " message received: " + msgString.trim());
        }

        this.messageProcessor = messageProcessor;
        // Supports only the single threaded model.
        this.run();
    }

    /**
     * Constructor. We create one of these when we send out a message.
     * @param targetAddr internet address of the place where we want to send
     * messages.
     * @param port target port (where we want to send the message).
     * @param sipStack our SIP Stack.
     * @param processor inbound message processor
     */
    public UDPMessageChannel(String targetAddr, int port,
                             SIPMessageStack sipStack,
                             UDPMessageProcessor processor) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DEBUG, UDPMessageChannel, UDPMessageChannel(), " +
                           "Creating message channel on "
                           + targetAddr
                           + "/" + port);
        }

        this.peerPort = port;
        this.peerAddress = targetAddr;
        this.myPort = port;
        this.myAddress = processor.getSIPStack().getHostAddress();
        this.messageProcessor = processor;
        this.peerProtocol = SIPConstants.TRANSPORT_UDP;
        stack = sipStack;
    }

    /**
     * Runs method specified by runnnable.
     */
    public void run() {
        Message sipMessage = null;
        // Create a new string message parser to parse the list of messages.
        // This is a huge performance hit -- need to optimize by pre-create
        // parser when one is needed....
        StringMsgParser myParser = new StringMsgParser();
        
        try {
            this.receptionTime = System.currentTimeMillis();

            sipMessage = myParser.parseSIPMessage(msgBytes);
        } catch (ParseException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DEBUG, UDPMessageChannel, run(), " +
                               "Rejecting message during parsing " +
                               "An exception has been raised: " + ex);
            }

            // ex.printStackTrace();
            return;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                       "DEBUG, UDPMEssageChannel, run(), sipMessage parsed: " +
                       sipMessage.encode());
        }

        if (sipMessage == null) {
            return;
        }

        ViaList viaList = sipMessage.getViaHeaders();
        // For a request first via header tells where the message
        // is coming from.
        // For response, just get the port from the packet.
        // format: address:port
        String address = incomingPacket.getAddress();
        try {
            int firstColon = address.indexOf("://");
            int secondColon = address.indexOf(":", firstColon+1);
            peerAddress = address.substring(firstColon+3,
                                            secondColon);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DEBUG, UDPMessageChannel, run(), sender address: " +
                           peerAddress);
            }

            String senderPortString = address.substring(secondColon+1);
            peerPort = Integer.parseInt(senderPortString);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                             "DEBUG, UDPMessageChannel, run(), sender port: " +
                             peerPort);
            }
        } catch (NumberFormatException e) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DEBUG, UDPMessageChannel, run(), " +
                               "exception raised: " + e);
            }
            // e.printStackTrace();
            peerPort = -1;
        }
        
        // Check for the required headers.
        if (sipMessage.getFromHeader() == null ||
            // sipMessage.getFromHeader().getTag() == null ||
            sipMessage.getTo() == null ||
            sipMessage.getCallId() == null ||
            sipMessage.getCSeqHeader() == null ||
            sipMessage.getViaHeaders() == null) {
            String badmsg = new String(msgBytes);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                               "bad message " + badmsg);
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                               ">>> Dropped Bad Msg " +
                               "FromHeader = "
                               + sipMessage.getFromHeader() +
                               "ToHeader = " + sipMessage.getTo() +
                               "CallId = " + sipMessage.getCallId() +
                               "CSeqHeader = "
                               + sipMessage.getCSeqHeader() +
                               "Via = " + sipMessage.getViaHeaders());
            }

            stack.logBadMessage(badmsg);
            return;
        }
        
        // For a request first via header tells where the message
        // is coming from.
        // For response, just get the port from the packet.
        if (sipMessage instanceof Request) {
            ViaHeader v = (ViaHeader)viaList.first();
            if (v.hasPort()) {
                if (sipMessage instanceof Request) {
                    this.peerPort = v.getPort();
                }
            } else this.peerPort = SIPConstants.DEFAULT_NONTLS_PORT;
            this.peerProtocol = v.getTransport();
            Request sipRequest = (Request) sipMessage;
            // This is a request - process it.
            SIPServerRequestInterface sipServerRequest =
            stack.newSIPServerRequest(sipRequest, this);
            // Drop it if there is no request returned
            if (sipServerRequest == null) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_JSR180,
                                   "Null request interface returned");
                }
                return;
            }

            int stPort = -1;
            try {
                stPort = stack.getPort(this.getTransport());
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_JSR180,
                                   "About to process " +
                                   sipRequest.getFirstLine() + "/" +
                                   sipServerRequest);
                }

                sipServerRequest.processRequest(sipRequest, this);

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_JSR180,
                                   "Done processing " +
                                   sipRequest.getFirstLine() + "/" +
                                   sipServerRequest);
                }

                // So far so good -- we will commit this message if
                // all processing is OK.
                if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
                    if (sipServerRequest.getProcessingInfo() == null) {
                        ServerLog.logMessage(sipMessage,
                                             sipRequest.getViaHost() + ":" +
                                             sipRequest.getViaPort(),
                                             stack.getHostAddress() + ":" +
                                             stPort,
                                             false,
                                             new Long(receptionTime)
                                             .toString());
                    } else {
                        ServerLog.logMessage(sipMessage,
                                             sipRequest.getViaHost() + ":" +
                                             sipRequest.getViaPort(),
                                             stack.getHostAddress() + ":" +
                                             stPort,
                                             sipServerRequest
                                             .getProcessingInfo(),
                                             false, new Long(receptionTime)
                                             .toString());
                    }
                }
            } catch (Exception ex) {
                if (ex instanceof SIPServerException) {
                    handleException((SIPServerException)ex);
                }
            }
        } else {
            // Handle a SIP Reply message.
            Response sipResponse = (Response) sipMessage;
            SIPServerResponseInterface sipServerResponse =
            stack.newSIPServerResponse(sipResponse, this);
            try {
                if (sipServerResponse != null) {
                    sipServerResponse.processResponse(sipResponse, this);
                    // Normal processing of message.
                } else {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_JSR180,
                                       "null sipServerResponse!");
                    }
                }
            } catch (Exception ex) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_JSR180,
                                   ">>>>>Message = " + new String(msgBytes));
                }

                if (ServerLog.needsLogging
                    (ServerLog.TRACE_MESSAGES)) {
                    this.logResponse(sipResponse,
                                     receptionTime,
                                     ex.getMessage()+ "-- Dropped!");
                }

                ServerLog.logException(ex);
            }
        }
    }

    /**
     * Returns a reply from a pre-constructed reply. This sends the message
     * back to the entity who caused us to create this channel in the
     * first place.
     * @param sipMessage Message string to send.
     * @throws IOException If there is a problem with sending the
     * message.
     */
    public void sendMessage(Message sipMessage) throws IOException {
        byte[] msg = sipMessage.encodeAsBytes();
        sendMessage(msg, peerAddress, peerPort, peerProtocol);
    }

    /**
     * Sends a message to a specified receiver address.
     * @param msg message string to send.
     * @param receiverAddress Address of the place to send it to.
     * @param receiverPort the port to send it to.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg,
                               String receiverAddress,
                               int receiverPort)
    throws IOException {
        // msg += "\r\n\r\n";
        // Via is not included in the request so silently drop the reply.
        if (receiverPort == -1) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                               "DEBUG, UDPMessageChannel, sendMessage(), " +
                               "The message is not sent: the receiverPort=-1");
            }
            throw new IOException("Receiver port not set ");
        }
        try {

            /*
             * Original NIST method for opening the datagram connection
             * has been replaced by direct calls to instantiate the protocol
             * handler, in order to pass the security token for use of lower
             * level transport connection.
             * Original NIST sequence is :
             *
             * //format: "datagram://address:port"
             * String url = "datagram://"+peerAddress+":"+peerPort;
             * this.datagramConnection =
             *         (DatagramConnection)Connector.open(url);
             *
             */

            String url = "//"+peerAddress+":"+peerPort;

            datagramConnection = com.sun.j2me.conn.Connector.getDatagramConnection(stack.getSecurityToken(),
                                 url, Connector.WRITE, true);

            // timeouts are ignored for datagrams

            Datagram reply = datagramConnection.newDatagram(msg, msg.length);

            datagramConnection.send(reply);
            String msgString = new String(msg, 0, msg.length);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                     "DEBUG, UDPMessageChannel, sendMessage(), " +
                     "Datagram sent on datagram:" + url + ", message sent:\n" +
                     msgString.trim());
            }

            datagramConnection.close();
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180, "toto");
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "DEBUG, UDPMessageChannel, sendMessage(), " +
                           "The message is not sent: exception raised: " + ex);
            }
        }
    }

    /**
     * Sends a message to a specified receiver address.
     * @param msg message string to send.
     * @param receiverAddress Address of the place to send it to.
     * @param receiverPort the port to send it to.
     * @param receiverProtocol protocol to use to send.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg,
                               String receiverAddress,
                               int receiverPort,
                               String receiverProtocol)
    throws IOException {
        // msg += "\r\n\r\n";
        // Via is not included in the request so silently drop the reply.
        if (receiverPort == -1) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                               "DEBUG, UDPMessageChannel, sendMessage(), " +
                               "The message is not sent: " +
                               "the receiverPort=-1");
            }

            throw new IOException("Receiver port not set ");
        }

        if (Utils.compareToIgnoreCase(receiverProtocol, SIPConstants.
                TRANSPORT_UDP) == 0) {
            try {
                /*
                 * Original NIST method for opening the datagram connection
                 * has been replaced by direct calls to instantiate the
                 * protocol handler, in order to pass the security token
                 * for use of lower level transport connection.
                 * Original NIST sequence is :
                 *
                 * String url = "datagram://"
                 *       + receiverAddress + ":" + receiverPort;
                 * // format: "datagram://address:port"
                 * datagramConnection =
                 *         (DatagramConnection)Connector.open(url);
                 *
                 */

                String url = "//" + receiverAddress + ":" + receiverPort;

                datagramConnection = com.sun.j2me.conn.Connector.getDatagramConnection(stack.getSecurityToken(),
                                                 url,Connector.WRITE, true);
                // timeouts are ignored
                Datagram datagram = datagramConnection
                                    .newDatagram(msg, msg.length);
                datagramConnection.send(datagram);
                String msgString = new String(msg, 0, msg.length);

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                                 "DEBUG, UDPMessageChannel, sendMessage(), " +
                                 " Datagram sent on datagram: " +
                                 url + ", message sent:\n" + msgString.trim());
                }

                datagramConnection.close();
            } catch (IOException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                             "DEBUG, UDPMessageChannel, sendMessage(), " +
                             "The message is not sent: exception raised:" + ex);

                }
            }
        } else {
            // TCP is not supported
            throw new IOException("Unsupported protocol");
        }
    }

    /**
     * Gets the stack pointer.
     * @return The sip stack for this channel.
     */
    public SIPMessageStack getSIPStack() {
        return stack;
    }

    /**
     * Returns a transport string.
     * @return the string "udp" in this case.
     */
    public String getTransport() {
        return "udp";
    }

    /**
     * Gets the stack address for the stack that received this message.
     * @return The stack address for our stack.
     */
    public String getHost() {
        return stack.stackAddress;
    }

    /**
     * Gets the port.
     * @return Our port (on which we are getting datagram
     * packets).
     */
    public int getPort() {
        return this.myPort;
    }

    /**
     * Handles an exception - construct a sip reply and send it back to the
     * caller.
     * @param ex The exception thrown at us by our
     * application.
     */
    public void handleException(SIPServerException ex) {
        // Return a parse error message to the client on the other end
        // if he is still alive.
        // ex.printStackTrace();
        int rc = ex.getRC();
        Request request = (Request) ex.getSIPMessage();
        Response response;
        String msgString = ex.getMessage();
        if (rc != 0) {
            response = request.createResponse(rc, msgString);
            // messageFormatter.newResponse(rc,request,msgString);
            try {
                sendMessage(response);
            } catch (IOException ioex) {
                ServerLog.logException(ioex);
            }
        } else {
            // Assume that the message has already been formatted.
            try {
                sendMessage(msgString);
            } catch (IOException ioex) {
                ServerLog.logException(ioex);
            }
        }
    }

    /**
     * Compares two UDP Message channels for equality.
     * @param other The other message channel with which to compare oursleves.
     * @return true if the objects match
     */
    public boolean equals(Object other) {
        if (other == null)
            return false;
        boolean retval;
        if (!this.getClass().equals(other.getClass())) {
            retval = false;
        } else {
            UDPMessageChannel that = (UDPMessageChannel) other;
            retval = this.peerAddress.equals(that.peerAddress);
        }
        return retval;
    }

    /**
     * Gets the key.
     * @return the key
     */
    public String getKey() {
        return myAddress + ":" + myPort + "/" + SIPConstants.TRANSPORT_UDP;
    }

    /**
     * Sends the message.
     * @param msg the message to be processed.
     * @exception IOException if the send can not be processed
     */
    private void sendMessage(String msg)
    throws IOException {
        sendMessage(msg.getBytes(), peerAddress,
                    peerPort, peerProtocol);
    }

    /**
     * Sends the message.
     * @param msg the message to be processed.
     * @exception IOException if the send can not be processed
     */
    private void sendMessage(byte[] msg)
    throws IOException {
        sendMessage(msg, peerAddress, peerPort, peerProtocol);
    }

    /**
     * Gets the logical originator of the message (from the top via header).
     * @return topmost via header sentby field
     */
    public String getViaHost() {
        return this.myAddress;
    }

    /**
     * Gets the logical port of the message orginator (from the top via hdr).
     * @return the via port from the topmost via header.
     */
    public int getViaPort() {
        return this.myPort;
    }

    /**
     * Checks if the transport is reliable
     * @return always false
     */
    public boolean isReliable() {
        return false;
    }

    /**
     * Closes the message channel.
     */
    public void close() {
    }

    /**
     * Gets the peer address of the machine that sent us this message.
     * @return a string contianing the ip address or host name of the sender
     * of the message.
     */
    public String getPeerAddress() {
        return this.peerAddress;
    }

    /**
     * Gets the sender port (the port of the other end that sent me
     * the message).
     * @return the remote port number
     */
    public int getPeerPort() {
        return this.peerPort;
    }

    /**
     * Returns true if this is a secure channel.
     * @return always false
     */
    public boolean isSecure() {

        return false;
    }

    /**
     * This gets called from the parser when a parse error is generated.
     * The handler is supposed to introspect on the error class and
     * header name to handle the error appropriately. The error can
     * be handled by :
     * <ul>
     * <li>1. Re-throwing an exception and aborting the parse.
     * <li>2. Ignoring the header (attach the unparseable header to
     * the Message being parsed).
     * <li>3. Re-Parsing the bad header and adding it to the sipMessage
     * </ul>
     *
     * @param ex parse exception being processed.
     * @param sipMessage sip message being processed.
     * @param hdrClass header parsing class
     * @param headerText header/RL/SL text being parsed.
     * @param messageText message where this header was detected.
     */
    public void handleException(ParseException ex, Message sipMessage,
                                Class hdrClass,
                                String headerText,
                                String messageText) throws ParseException {

        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           ex.getMessage());
        }

        // Log the bad message for later reference.
        if (hdrClass .equals(FromHeader.clazz) ||
            hdrClass.equals(ToHeader.clazz) ||
            hdrClass.equals(CSeqHeader.clazz) ||
            hdrClass.equals(ViaHeader.clazz) ||
            hdrClass.equals(CallIdHeader.clazz) ||
            hdrClass.equals(RequestLine.clazz) ||
            hdrClass.equals(StatusLine.clazz)) {
            stack.logBadMessage(messageText);
            throw ex;
        } else {
            sipMessage.addUnparsed(headerText);
        }
    }
}

