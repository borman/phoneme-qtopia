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
 * TCPMessageChannel.java
 *
 * Created on September 3, 2002, 3:47 PM
 */

package gov.nist.siplite.stack;

import gov.nist.siplite.header.*;
import gov.nist.siplite.parser.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.SIPConstants;
import gov.nist.core.*;
import javax.microedition.io.*;
import java.io.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Handle a TCP stream connection.
 *
 * @version 1.0
 */
public class TCPMessageChannel extends MessageChannel
        implements SIPMessageListener {
    /** Stream connection handle. */
    private SocketConnection mySock;
    /** Message parser handle. */
    private PipelinedMsgParser myParser;
    /** Input stream for client thread. */
    private InputStream myClientInputStream;
    /** Output stream for client thread. */
    private OutputStream myClientOutputStream;
    /** Current SIP message stack. */
    private SIPMessageStack stack;
    /** Local address. */
    private String myAddress;
    /** Local port number. */
    private int myPort;
    /** Remote address. */
    private String peerAddress;
    /** Remote port number. */
    private int peerPort;
    /** Remote transport type. */
    private String peerProtocol;
    /** Indicates channel is shutting down. */
    private boolean exitFlag;
    /** Number of transaction that uses this channel. */
    private int useCounter;
    
    /**
     * Constructor - gets called from the SIPMessageStack class with a socket
     * on accepting a new client. All the processing of the message is
     * done here with the stack being freed up to handle new connections.
     * The sock input is the socket that is returned from the accept.
     * Global data that is shared by all threads is accessible in the Server
     * structure.
     * @param sock Socket from which to read and write messages. The socket
     * is already connected (was created as a result of an accept).
     * @param sipStack Ptr to SIP Stack
     * @param msgProcessor handler for TCP message communication
     */
    protected TCPMessageChannel(SocketConnection sock, SIPMessageStack sipStack,
                                TCPMessageProcessor msgProcessor) 
    throws IOException {
        mySock = sock;
        myClientInputStream = sock.openInputStream();
        myClientOutputStream = sock.openOutputStream();
        stack = sipStack;
        messageProcessor = msgProcessor;
        // peerAddress will be updated when 
        // first Request packet is received
        // see processMessage(...)
        peerAddress = sock.getAddress();
        peerPort = sock.getPort();
        myAddress = sock.getLocalAddress();
        myPort = sock.getLocalPort();
        start();
        incrementUseCounter();
    }

    /**
     * Constructor - connects to the given inet address.
     * @param targetHostPort inet address to connect to.
     * @param sipStack is the sip stack from which we are created.
     * @param msgProcessor TCP message processor
     * @throws IOException if we cannot connect.
     */
    protected TCPMessageChannel(HostPort targetHostPort,
                                SIPMessageStack sipStack,
                                TCPMessageProcessor msgProcessor)
    throws IOException {
        stack = sipStack;
        messageProcessor = msgProcessor;
        myAddress = sipStack.getHostAddress();
        peerAddress = targetHostPort.getHost().getHostname();
        peerPort = targetHostPort.getPort();
        makeSocket(peerAddress, peerPort);
        start();
        incrementUseCounter();
    }

    /**
     * Creates a socket connection.
     * @param host Host name
     * @param port Port number
     * @exception IOException if the socket can not be created.
     */
    private void makeSocket(String host, int port) throws IOException {
        if (host == null ||
            host.length() == 0 ||
            port < 0) {
            throw new IOException("Invalid hostname or port number");
        }
        
        String name = "//" + host + ":" + port;
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Opening outbound socket connection :" + host + ":" + port);
        }
        
        /*
         * Original NIST method for opening the socket connection
         * has been replaced by direct calls to instantiate the protocol 
         * handler, in order to pass the security token for use of lower 
         * level transport connection. 
         * Original NIST sequence is :
         *
         * socket = (SocketConnection)Connector.open(name);
         *
         */
        try {
            mySock = com.sun.j2me.conn.Connector.getSocketConnection(stack.getSecurityToken(), name);
        } catch (ConnectionNotFoundException ex) {
            throw new IOException("Can't connect to "+name);
        }
        myClientInputStream = mySock.openInputStream();
        myClientOutputStream = mySock.openOutputStream();
    }
    
    /**
     * Returns "true" as this is a reliable transport.
     * @return true if reliable stream transport
     */
    public boolean isReliable() {
        return true;
    }
    
    /**
     * Closes the message channel.
     */
    synchronized public void close() {
        if (0 != useCounter) {
            useCounter--;
        } 
        if (0 == useCounter) {
            exit();
            if (null != messageProcessor) {
                ((TCPMessageProcessor)messageProcessor).notifyClose(this);
            }
        }
    }

    /**
     * Closes the message channel regardless whether it is used or not.
     */
    synchronized protected void exit() {
        useCounter = 0;
        exitFlag = true;
        shutDownConnection();
        // allow gc to collect MP
        messageProcessor = null;
    }
    
    /**
     * Gets my SIP Stack.
     * @return The SIP Stack for this message channel.
     */
    public SIPMessageStack getSIPStack() {
        return stack;
    }
    
    /**
     * Gets the transport string.
     * @return "tcp" in this case.
     */
    public String getTransport() {
        return SIPConstants.TRANSPORT_TCP;
    }
    
    /**
     * Gets the address of the client that sent the data to us.
     * @return Address of the client that sent us data
     * that resulted in this channel being
     * created.
     */
    public String getPeerAddress() {
        return peerAddress;
    }
    
    /**
     * Sends message to whoever is connected to us.
     * Uses the topmost via address to send to.
     * @param msg is the message to send.
     */
    synchronized private void sendMessage(byte[] msg) throws IOException {
        if (exitFlag) {
            return;
        }

        if (mySock == null) {
            reConnect();
        }

        myClientOutputStream.write(msg);
    }
    
    /**
     * Returns a formatted message to the client.
     * We try to re-connect with the peer on the other end if possible.
     * @param sipMessage Message to send.
     * @throws IOException If there is an error sending the message
     */
    public void sendMessage(Message sipMessage) throws IOException {
        if (sipMessage == null) {
            throw new NullPointerException("null arg!");
        }

        byte[] msg = sipMessage.encodeAsBytes();
        long time = System.currentTimeMillis();

        sendMessage(msg);

        if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
            logMessage(sipMessage, peerAddress, peerPort, time);
        }
    }
    
    /**
     * Sends a message to a specified address.
     * @param message Pre-formatted message to send.
     * @param receiverAddress Address to send it to.
     * @param receiverPort Receiver port.
     * @throws IOException If there is a problem connecting or sending.
     */
    public void sendMessage(byte message[], String receiverAddress,
            int receiverPort)
            throws IOException, IllegalArgumentException {
        if (message == null || receiverAddress == null) {
            throw new IllegalArgumentException("Null argument");
        }

        if (!receiverAddress.equals(peerAddress) || peerPort != receiverPort) {
            throw new IOException("This channel is bound to different peer");
        }
        
        sendMessage(message);
    }
    
    /**
     * Exception processor for exceptions detected from the application.
     * @param ex The exception that was generated.
     */
    public void handleException(SIPServerException ex) {
        // Return a parse error message to the client on the other end
        // if he is still alive.
        int rc = ex.getRC();
        String msgString = ex.getMessage();
        if (rc != 0) {
            // Do we have a valid Return code ? --
            // in this case format the message.
            Request request =
                    (Request) ex.getSIPMessage();
            Response response =
                    request.createResponse(rc);
            try {
                sendMessage(response);
            } catch (IOException ioex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        ioex.getMessage());
                }
            }
        } else {
            // Otherwise, message is already formatted --
            // just return it
            try {
                sendMessage(msgString.getBytes());
            } catch (IOException ioex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        ioex.getMessage());
                }
            }
        }
    }
    
    
    /**
     * Exception processor for exceptions detected from the parser. (This
     * is invoked by the parser when an error is detected).
     * @param ex parse exception detected by the parser.
     * @param sipMessage message that incurred the error.
     * @param hdrClass header parsing class
     * @param header header that caused the error.
     * @param message descriptive text for exception
     * @throws ParseException Thrown if we want to reject the message.
     */
    public void handleException(ParseException ex,
            Message sipMessage,
            Class hdrClass,
            String header,
            String message)
            throws ParseException {
                
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
                hdrClass.equals(RequestLine.clazz)||
                hdrClass.equals(StatusLine.clazz)) {
            stack.logBadMessage(message);
            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
        
    }
    
    
    /**
     * Gets invoked by the parser as a callback on successful message
     * parsing (i.e. no parser errors).
     * @param sipMessage Mesage to process (this calls the application
     * for processing the message).
     */
    public void processMessage(Message sipMessage) {
        if (sipMessage.getFromHeader() == null ||
                sipMessage.getTo() == null ||
                sipMessage.getCallId() == null ||
                sipMessage.getCSeqHeader() == null ||
                sipMessage.getViaHeaders() == null) {
            String badmsg = sipMessage.encode();
            
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                ServerLog.logMessage("bad message " + badmsg);
                ServerLog.logMessage(">>> Dropped Bad Msg");
            }
            
            stack.logBadMessage(badmsg);
            
            return;
        }
        
        ViaList viaList = sipMessage.getViaHeaders();

        // For a request
        // first via header tells where the message is coming from.
        // For response, this has already been recorded in the outgoing
        // message.
        long receptionTime = 0;
        
        if (sipMessage instanceof Request) {
            ViaHeader v = (ViaHeader)viaList.first();
            if (v.hasPort()) {
                peerPort = v.getPort();
            } else {
                peerPort = SIPConstants.DEFAULT_NONTLS_PORT;
            }
            peerProtocol = v.getTransport();

            // System.out.println("receiver address = " + receiverAddress);
        
            // Foreach part of the request header, fetch it and process it
            receptionTime = System.currentTimeMillis();

            // This is a request - process the request.
            Request sipRequest = (Request)sipMessage;
            
            // Create a new sever side request processor for this
            // message and let it handle the rest.
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "----Processing Message---");
            }
            
            SIPServerRequestInterface sipServerRequest =
                    stack.newSIPServerRequest(sipRequest, this);
            try {
                sipServerRequest.processRequest(sipRequest, this);
                
                ServerLog.logMessage(sipMessage,
                        sipRequest.getViaHost() + ":" +
                        sipRequest.getViaPort(),
                        stack.getHostAddress() + ":" +
                        stack.getPort(this.getTransport()),
                        false,
                        receptionTime);
                
            } catch (SIPServerException ex) {
                ServerLog.logMessage(sipMessage,
                        sipRequest.getViaHost() + ":"
                        + sipRequest.getViaPort(),
                        stack.getHostAddress() + ":" +
                        stack.getPort(this.getTransport()),
                        ex.getMessage(), false, receptionTime);
                handleException(ex);
            }
        } else {
            // This is a response message - process it.
            Response sipResponse = (Response)sipMessage;
            SIPServerResponseInterface sipServerResponse =
                    stack.newSIPServerResponse(sipResponse, this);
                    
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "got a response interface " + sipServerResponse);
            }
            
            try {
                sipServerResponse.processResponse(sipResponse, this);
            } catch (SIPServerException ex) {
                // An error occured processing the message -- just log it.
                ServerLog.logMessage(sipMessage,
                        getPeerAddress().toString() + ":"
                        + getPeerPort(),
                        stack.getHostAddress() + ":" +
                        stack.getPort(this.getTransport()),
                        ex.getMessage(), false, receptionTime);
                // Ignore errors while processing responses??
            }
        }
    }
    
    /**
     * This gets invoked when thread.start is called from the constructor.
     * Implements a message loop - reading the tcp connection and processing
     * messages until we are done or the other end has closed.
     */
    private void start() {
        // Create a pipelined message parser to read and parse
        // messages that we write out to him.
        myParser = new PipelinedMsgParser(this, myClientInputStream);
        // Enable the flag to parse message content.
        // Start running the parser thread.
        myParser.processInput();
        
    }

    /**
     * Increments the number of user of this channel.
     */
    synchronized protected void incrementUseCounter() {
        if (!exitFlag) {
            useCounter++;
        }
    }
    
    
    /**
     * Called when the pipelined parser cannot read input because the
     * other end closed the connection.
     */
    public void handleIOException() {
        if (!exitFlag) {
            shutDownConnection();
        }
    }

    /** 
     * Closes all input and output stream and socket.
     */
    synchronized private void shutDownConnection() {
        try {
            if (null != myClientInputStream) {
                myClientInputStream.close();
            }
        } catch (IOException ioe) {
            // intentionally ignored
        }

        try {
            if (null != myClientOutputStream) {
                myClientOutputStream.close();            }

        } catch (IOException ioe) {
            // intentionally ignored
        }

        try {
            if (null != mySock) {
                mySock.close();
            }
        } catch (IOException ioe) {
            // intentionally ignored
        }
        // null mySock indicates closed connection
        // see sendMessage()
        mySock = null;
    }
    
    /**
     * Reconnect to the server.
     * @exception IOException if the connection can not be established
     */
    private void reConnect() throws IOException {
        shutDownConnection();
        makeSocket(peerAddress, peerPort);
        myParser = new PipelinedMsgParser(this, myClientInputStream);
        myParser.processInput();
    }
    
    /**
     * Equals predicate.
     * @param other is the other object to compare ourselves to for equals
     * @return true if object matches
     */
    public boolean equals(Object other) {
        if (!this.getClass().equals(other.getClass()))
            return false;
        else {
            TCPMessageChannel that = (TCPMessageChannel)other;
            if (this.mySock != that.mySock)
                return false;
            else return true;
        }
    }
    
    /**
     * Gets an identifying key. This key is used to cache the connection
     * and re-use it if necessary.
     * @return the identifying key
     */
    public String getKey() {
        return getKey(peerAddress, peerPort, SIPConstants.TRANSPORT_TCP);
    }
    
    /**
     * Gets the host to assign to outgoing messages.
     * @return the host to assign to the via header.
     */
    public String getViaHost() {
        return myAddress;
    }
    
    /**
     * Gets the port for outgoing messages sent from the channel.
     * @return the port to assign to the via header.
     */
    public int getViaPort() {
        return myPort;
    }
    
    /**
     * Gets the port of the peer to whom we are sending messages.
     * @return the peer port.
     */
    public int getPeerPort() {
        return peerPort;
    }
    
    /**
     * TCP Is not a secure protocol.
     * @return always false
     */
    public boolean isSecure() {
        return false;
    }
}
