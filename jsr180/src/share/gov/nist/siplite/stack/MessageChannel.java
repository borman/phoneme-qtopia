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

import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import gov.nist.siplite.message.*;
import java.util.Enumeration;
import java.io.IOException;
import gov.nist.core.*;

/**
 * Message channel abstraction for the SIP stack.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class MessageChannel {
    /**
     * Message processor to whom I belong (if set).
     */
    protected MessageProcessor messageProcessor;
    
    /**
     * Closes the message channel.
     */
    public abstract void close();
    
    /**
     * Gets the SIPMessageStack object from this message channel.
     * @return SIPMessageStack object of this message channel
     */
    public abstract SIPMessageStack getSIPStack();
    
    /**
     * Gets transport string of this message channel.
     * @return Transport string of this message channel.
     */
    public abstract String getTransport();
    
    /**
     * Gets whether this channel is reliable or not.
     * @return True if reliable, false if not.
     */
    public abstract boolean isReliable();
    
    /**
     * Returns true if this is a secure channel.
     * @return true if connection is secure
     */
    public abstract boolean isSecure();
    
    /**
     * Sends the message (after it has been formatted)
     * @param sipMessage Message to send.
     */
    public abstract void sendMessage(Message sipMessage)
    throws IOException;
    
    /**
     * Gets the peer address of the machine that sent us this message.
     * @return a string contianing the ip address or host name of the sender
     * of the message.
     */
    public abstract String getPeerAddress();
    
    /**
     * Gets the name of the machine that sent us this message.
     * @return a string contianing the ip address or host name of the sender
     * of the message.
     * public abstract String getPeerName();
     */
    
    /**
     * Gets the sender port (the port of the other end that sent me
     * the message).
     * @return the peer port
     */
    public abstract int getPeerPort();
    
    /**
     * Generates a key which identifies the message channel.
     * This allows us to cache the message channel.
     * @return the key
     */
    public abstract String getKey();
    
    /**
     * Gets the host to assign for an outgoing Request via header.
     * @return the via host
     */
    public abstract String getViaHost();
    
    /**
     * Gets the port to assign for the via header of an outgoing message.
     * @return the via port
     */
    public abstract int getViaPort();
    
    /**
     * Sends the message (after it has been formatted), to a specified
     * address and a specified port
     * @param message Message to send.
     * @param receiverAddress Address of the receiver.
     * @param receiverPort Port of the receiver.
     */
    protected abstract void sendMessage(byte[] message,
            String receiverAddress,
            int receiverPort)
            throws IOException;
    
    /**
     * Gets the host of this message channel.
     * @return host of this messsage channel.
     */
    public String getHost() {
        return this.getSIPStack().getHostAddress();
    }
    
    /**
     * Gets port of this message channel.
     * @return Port of this message channel.
     */
    public int getPort() {
        if (this.messageProcessor != null)
            return messageProcessor.getPort();
        else return -1;
    }
    
    /**
     * Handles an exception.
     * @param ex the exception to process
     */
    public abstract void handleException(SIPServerException ex);
    
    /**
     * Sends a message given SIP message.
     * @param sipMessage is the messge to send.
     * @param receiverAddress is the address to which we want to send
     * @param receiverPort is the port to which we want to send
     */
    public void sendMessage(Message sipMessage,
            String receiverAddress,
            int receiverPort) throws IOException {
        long time = System.currentTimeMillis();
        byte[] bytes = sipMessage.encodeAsBytes();
        sendMessage(bytes, receiverAddress, receiverPort);
        logMessage(sipMessage, receiverAddress, receiverPort, time);
    }
    
    /**
     * Generates a key given the inet address port and transport.
     * @param inetAddr internet address
     * @param port the connection end point
     * @param transport the connection type
     * @return the connection key
     */
    public static String
            getKey(String inetAddr, int port, String transport) {
        return transport+":"+ inetAddr +":"+port;
    }
    
    /**
     * Gets the hostport structure of this message channel.
     * @return the host and port
     */
    public HostPort getHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(this.getHost()));
        retval.setPort(this.getPort());
        return retval;
    }
    
    /**
     * Gets the peer host and port.
     *
     * @return a HostPort structure for the peer.
     */
    public HostPort getPeerHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(this.getPeerAddress()));
        retval.setPort(this.getPeerPort());
        return retval;
    }
    
    /**
     * Gets the Via header for this transport.
     * Note that this does not set a branch identifier.
     *
     * @return a via header for outgoing messages sent from this channel.
     */
    public ViaHeader getViaHeader() {
        ViaHeader channelViaHeader;
        
        channelViaHeader = new ViaHeader();
        
        channelViaHeader.setTransport(getTransport());
        
        channelViaHeader.setSentBy(getHostPort());
        return channelViaHeader;
    }
    
    /**
     * Gets the via header host:port structure.
     * This is extracted from the topmost via header of the request.
     *
     * @return a host:port structure
     */
    public HostPort getViaHostPort() {
        HostPort retval = new HostPort();
        retval.setHost(new Host(this.getViaHost()));
        retval.setPort(this.getViaPort());
        return retval;
    }
    
    /**
     * Logs a message sent to an address and port via the default interface.
     * @param sipMessage is the message to log.
     * @param address is the inet address to which the message is sent.
     * @param port is the port to which the message is directed.
     * @param time timestamp for the logged message
     */
    protected void logMessage(Message sipMessage,
            String address, int port, long time) {
        String firstLine = sipMessage.getFirstLine();
        CSeqHeader cseq = (CSeqHeader) sipMessage.getCSeqHeader();
        CallIdHeader callid = (CallIdHeader) sipMessage.getCallId();
        String cseqBody = cseq.encodeBody();
        String callidBody = callid.encodeBody();
        // Default port.
        if (port == -1) port = 5060;
        if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
            Enumeration extList = sipMessage.getHeaders("NISTExtension");
            String status = null;
            if (extList != null && extList.hasMoreElements()) {
                Header exthdr = null;
                exthdr = (Header) extList.nextElement();
                status = exthdr.getHeaderValue();
            }
            ServerLog.logMessage(sipMessage.encode(),
                    this.getHost()+":"+this.getPort(),
                    address +
                    ":" + port, true, callidBody,
                    firstLine, status,
                    sipMessage.getTransactionId(), time);
        }
    }
    
    /**
     * Logs a response received at this message channel.
     * This is used for processing incoming responses to a client transaction.
     * @param sipResponse the response object to be logged
     * @param receptionTime is the time at which the response was received.
     * @param status is the processing status of the message.
     */
    public void
            logResponse(Response sipResponse,
            long receptionTime,
            String status) {
        try {
            int peerport = getPeerPort();
            if (peerport == 0 && sipResponse.getContactHeaders() != null) {
                ContactHeader contact =
		    (ContactHeader) sipResponse.getContactHeaders().getFirst();
                peerport = ((Address)contact.getAddress()).getPort();
                
            }
            String from = getPeerAddress() + ":" + peerport;
            String to = this.getHost() + ":" + getPort();
            ServerLog.logMessage(sipResponse,
                    from, to, status, false, receptionTime);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Gets the message processor.
     * @return the message processor
     */
    public MessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }
}
