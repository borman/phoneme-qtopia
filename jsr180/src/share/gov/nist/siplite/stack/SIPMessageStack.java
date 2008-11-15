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

import java.io.IOException;
import gov.nist.core.*;
import gov.nist.siplite.address.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.SIPConstants;
import java.util.Vector;
import java.util.Enumeration;
import com.sun.j2me.security.Token;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This class defines a SIP Stack. In order to build a SIP server (UAS/UAC or
 * Proxy etc.) you need to extend this class and instantiate it in your
 * application. After you have done so, call
 * {@link #createMessageProcessor}
 * to create message processors and then start these message processors to
 * get the stack the process messages.
 * This will start the necessary threads that wait for incoming SIP messages.
 * A general note about the handler structures -- handlers are expected to
 * returnResponse for successful message processing and throw
 * SIPServerException for unsuccessful message processing.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public abstract class SIPMessageStack {
    /**
     * Security token for SIP/SIPS protocol class
     */
    protected Token securityToken;
    /** Flag indicating tcp connection in use. */
    protected boolean tcpFlag;
    /** Flag indicating udp connection in use. */
    protected boolean udpFlag;
    /** The outbound proxy location. */
    protected String outboundProxy;
    /** The outbound proxy server port. */
    protected int outboundPort = -1;

    /**
     * Flag that indicates that the stack is active.
     */
    protected boolean toExit;

    /**
     * Bad message log. The name of a file that stores bum messages for
     * debugging.
     */
    protected String badMessageLog;

    /**
     * Internal flag for debugging
     */
    protected boolean debugFlag;

    /**
     * Name of the stack.
     */
    protected String stackName;

    /**
     * IP address of stack.
     */
    protected String stackAddress; // My host address.

    /**
     * Request factory interface (to be provided by the application).
     */
    protected SIPStackMessageFactory sipMessageFactory;

    /**
     * Router to determine where to forward the request.
     */
    protected Router router;

    /**
     * Starts a single processing thread for all UDP messages
     * (otherwise, the stack will start a new thread for each UDP
     * message).
     */
    protected int threadPoolSize;

    /** Max number of simultaneous connections. */
    protected int maxConnections;

    /** A collection of message processors. */
    private Vector messageProcessors;


    /**
     * Logs a bad message (invoked when a parse exception arises).
     *
     * @param message is a string that contains the bad message to log.
     */
    public void logBadMessage(String message) {
        if (badMessageLog != null) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    message + badMessageLog);
            }
        }
    }

    /**
     * Gets the file name of the bad message log.
     * @return the file where bad messages are logged.
     */
    public String getBadMessageLog() {
        return this.badMessageLog;
    }

    /**
     * Sets the flag that instructs the stack to only start a single
     * thread for sequentially processing incoming udp messages (thus
     * serializing the processing).
     * Caution: If the user-defined function called by the
     * processing thread blocks, then the entire server will block.
     */
    public void setSingleThreaded() {
        this.threadPoolSize = 1;
    }

    /**
     * Sets the thread pool size for processing incoming UDP messages.
     * Limit the total number of threads for processing udp messages.
     * Caution: If the user-defined function called by the
     * processing thread blocks, then the entire server will block.
     * @param size the new thread pool size
     */
    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }

    /**
     * Sets the max # of simultaneously handled TCP connections.
     * @param nconnections the new max connections
     */
    public void setMaxConnections(int nconnections) {
        this.maxConnections = nconnections;
    }

    /**
     * Construcor for the stack. Registers the request and response
     * factories for the stack.
     * @param messageFactory User-implemented factory for processing
     * messages.
     * @param stackAddress -- IP address or host name of the stack.
     * @param stackName -- descriptive name for the stack.
     */
    public SIPMessageStack(SIPStackMessageFactory messageFactory,
            String stackAddress,
            String stackName) throws IllegalArgumentException {
        this();
        sipMessageFactory = messageFactory;
        if (stackAddress == null) {
            throw new IllegalArgumentException
                    ("stack Address not set");
        }

        // Set a descriptive name for the message trace logger.
        ServerLog.description = stackName;
        ServerLog.stackIpAddress = stackAddress;
    }

    /**
     * Sets the server Request and response factories.
     * @param messageFactory User-implemented factory for processing
     * messages.
     */
    public void setStackMessageFactory
            (SIPStackMessageFactory messageFactory) {
        sipMessageFactory = messageFactory;
    }

    /**
     * Sets the descriptive name of the stack.
     * @param stackName -- descriptive name of the stack.
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
        ServerLog.setDescription(stackName);
        ServerLog.stackIpAddress = stackAddress;
    }

    /**
     * Gets the Stack name.
     * @return name of the stack.
     */
    public String getStackName() {
        return this.stackName;
    }

    /**
     * Sets my address.
     * @param stackAddress -- A string containing the stack address.
     */
    public void setHostAddress(String stackAddress) {
        if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
        && stackAddress.trim().charAt(0) != '[')
            this.stackAddress = '[' + stackAddress + ']';
        else
            this.stackAddress = stackAddress;
    }

    /**
     * Gets my address.
     * @return hostAddress - my host address.
     */
    public String getHostAddress() {
        return this.stackAddress;
    }

    /**
     * Gets the default next hop from the router.
     * @return the default next hop
     */
    public Hop getNextHop() {
        return (Hop) this.router.getOutboundProxy();

    }

    /**
     * Gets port of the message processor (based on the transport). If
     * multiple ports are enabled for the same transport then the first
     * one is retrieved.
     * @param transport is the transport for which to get the port.
     * @return the message processor port
     * @exception IllegalArgumentException if the transport is not
     * supported
     */
    public int getPort(String transport) throws IllegalArgumentException {
        synchronized (messageProcessors) {
            Enumeration it = messageProcessors.elements();
            while (it.hasMoreElements()) {
                MessageProcessor mp = (MessageProcessor) it.nextElement();
                if (Utils.equalsIgnoreCase(mp.getTransport(), transport))
                    return mp.getPort();
            }
            throw new IllegalArgumentException
                    ("Transport not supported " + transport);
        }
    }

    /**
     * Returns true if a transport is enabled.
     * @param transport is the transport to check.
     * @return true if transport is enabled
     */
    public boolean isTransportEnabled(String transport) {
        synchronized (messageProcessors) {
            Enumeration it = messageProcessors.elements();
            while (it.hasMoreElements()) {
                MessageProcessor mp = (MessageProcessor) it.nextElement();
                if (Utils.equalsIgnoreCase(mp.getTransport(), transport))
                    return true;
            }
            return false;
        }
    }

    /**
     * Returns true if the transport is enabled for a given port.
     * @param transport transport to check
     * @param port port to check transport at.
     * @return true if transport is enabled
     */
    public boolean isTransportEnabled(String transport, int port) {
        synchronized (messageProcessors) {
            Enumeration it = messageProcessors.elements();
            while (it.hasMoreElements()) {
                MessageProcessor mp = (MessageProcessor) it.nextElement();
                if (Utils.equalsIgnoreCase(mp.getTransport(), transport) &&
                        mp.getPort() == port)
                    return true;
            }
            return false;
        }
    }

    /**
     * Default constructor.
     */
    public SIPMessageStack() {
        this.toExit = false;
        // Set an infinit thread pool size.
        this.threadPoolSize = -1;
        // Max number of simultaneous connections.
        this.maxConnections = -1;
        // Array of message processors.
        messageProcessors = new Vector();
    }

    /**
     * Generates a new SIPSeverRequest from the given Request. A
     * SIPServerRequest is generated by the application
     * SIPServerRequestFactoryImpl. The application registers the
     * factory implementation at the time the stack is initialized.
     * @param siprequest Request for which we want to generate
     * thsi SIPServerRequest.
     * @param msgchan Message channel for the request for which
     * we want to generate the SIPServerRequest
     * @return Generated SIPServerRequest.
     */
    protected SIPServerRequestInterface
            newSIPServerRequest(Request siprequest, MessageChannel msgchan) {
        return sipMessageFactory.newSIPServerRequest
                (siprequest, msgchan);
    }

    /**
     * Generates a new SIPSeverResponse from the given Response.
     * @param sipresponse Response from which the SIPServerResponse
     * is to be generated. Note - this just calls the factory interface
     * to do its work. The factory interface is provided by the user.
     * @param msgchan Message channel for the SIPServerResponse
     * @return SIPServerResponse generated from this SIP
     * Response
     */
    SIPServerResponseInterface
            newSIPServerResponse(Response sipresponse,
            MessageChannel msgchan) {
        return sipMessageFactory.newSIPServerResponse
                (sipresponse, msgchan);
    }

    /**
     * Sets the router algorithm.
     * @param router A class that implements the Router interface.
     */
    public void setRouter(Router router) {
        this.router = router;
    }

    /**
     * Gets the router algorithm.
     * @return Router router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Gets the default route.
     * @return the default route
     */
    public Hop getDefaultRoute() {
        return this.router.getOutboundProxy();
    }

    /**
     * Gets the route header for this hop.
     * @param hop the hop to be processed
     * @return the route header for the hop.
     */
    public RouteHeader getRouteHeader(Hop hop) {
        HostPort hostPort = new HostPort();
        Host h = new Host(hop.getHost());
        hostPort.setHost(h);
        hostPort.setPort(hop.getPort());
        gov.nist.siplite.address.SipURI uri = new SipURI();
        uri.setHostPort(hostPort);
        uri.setScheme(SIPConstants.SCHEME_SIP);

        try {
            uri.setTransportParam(hop.getTransport());
        } catch (ParseException ex) {
            InternalErrorHandler.handleException(ex);
        }

        Address address = new Address();
        address.setURI(uri);
        RouteHeader route = new RouteHeader();
        route.setAddress(address);

        return route;
    }

    /**
     * Gets the route header corresponding to the default route.
     * @return the default route header
     */
    public RouteHeader getDefaultRouteHeader() {
        if (router.getOutboundProxy() != null) {
            Hop hop = ((Hop) router.getOutboundProxy());
            return getRouteHeader(hop);
        } else
            return null;
    }

    /**
     * Returns the status of the toExit flag.
     * @return true if the stack object is alive and false otherwise.
     */
    public synchronized boolean isAlive() {
        return !toExit;
    }

    /**
     * Makes the stack close all accept connections and return. This
     * is useful if you want to start/stop the stack several times from
     * your application. Caution : use of this function could cause
     * peculiar bugs as messages are prcessed asynchronously by the stack.
     */

    public void stopStack() {
        synchronized (this.messageProcessors) {
            // Threads must periodically check this flag.
            this.toExit = true;
            Vector processorList;
            processorList = getMessageProcessors();
            /*
             * IMPL_NOTE:
             * Normally, messageprocessors are already stopped before
             * stopStack() is explicitely invoked from close() of
             * SipConnectionNotifier. So it is not needed to iterate through the
             * list. However, this part of the code is not yet removed as it is
             * not known if sipStack() needs to be closed independantly.
             * A safetly check is added before invoking stop() for a message
             * processor to verify if it was already closed
             */
            for (int i = 0; i < processorList.size(); i++) {
                MessageProcessor mp =
                        (MessageProcessor) processorList.elementAt(i);
                if (!mp.toExit()) {
                    mp.stop();
                }
            }
            processorList.removeAllElements();
        }
    }

    /**
     * Adds a new MessageProcessor to the list of running processors
     * for this SIPMessageStack and starts it. You can use this method
     * for dynamic stack configuration.
     * Acknowledgement: This code is contributed by Jeff Keyser.
     * @param newMessageProcessor the new message processor to
     * register
     */
    public void addMessageProcessor(MessageProcessor newMessageProcessor)
            throws IOException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "addMessageProcessor " +
                newMessageProcessor.getPort() + " / " +
                newMessageProcessor.getTransport());
        }

        synchronized (messageProcessors) {
            messageProcessors.addElement(newMessageProcessor);
            newMessageProcessor.start();
        }
    }

    /**
     * Removes a MessageProcessor from this SIPMessageStack. Acknowledgement:
     * Code contributed by Jeff Keyser.
     * @param oldMessageProcessor
     */
    public void
            removeMessageProcessor(MessageProcessor oldMessageProcessor) {
        synchronized (messageProcessors) {

            if (messageProcessors.removeElement(oldMessageProcessor)) {

                oldMessageProcessor.stop();
            }
        }
    }


    /**
     * Gets an array of running MessageProcessors on this SIPMessageStack.
     * Acknowledgement: Jeff Keyser suggested that applications should
     * have access to the running message processors and contributed
     * this code.
     * @return an array of running message processors.
     *
     */
    public Vector getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Gets a message processor for the given transport.
     * @param transport the transport to be checked
     * @return the message processor for the transport
     */
    public MessageProcessor getMessageProcessor(String transport) {
        synchronized (messageProcessors) {
            Enumeration it = messageProcessors.elements();
            while (it.hasMoreElements()) {
                MessageProcessor mp = (MessageProcessor) it.nextElement();
                if (Utils.equalsIgnoreCase(mp.getTransport(), transport)) {
                    return mp;
                }
            }

            return null;
        }
    }

    /**
     * Creates the equivalent of a JAIN listening point and attaches
     * to the stack.
     * @param port the message processor port address
     * @param transport the message processor transport type
     * @return the requested message processor
     */
    public MessageProcessor createMessageProcessor(int port, String transport)
            throws java.io.IOException, IllegalArgumentException {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "createMessageProcessor : " +
                port + " / " + transport);
        }

        if (Utils.equalsIgnoreCase(transport, SIPConstants.TRANSPORT_UDP)) {
            UDPMessageProcessor
                    udpMessageProcessor =
                    new UDPMessageProcessor(this, port);
            this.addMessageProcessor(udpMessageProcessor);
            this.udpFlag = true;
            return udpMessageProcessor;
        } else if (Utils.equalsIgnoreCase(transport,
                                SIPConstants.TRANSPORT_TCP)) {
            TCPMessageProcessor
                    tcpMessageProcessor =
                    new TCPMessageProcessor(this, port);
            this.addMessageProcessor(tcpMessageProcessor);
            this.tcpFlag = true;
            return tcpMessageProcessor;
        } else {
            throw new IllegalArgumentException("bad transport");
        }

    }

    
    /**
     * Sets the message factory.
     * @param messageFactory -- messageFactory to set.
     */
    protected
            void setMessageFactory(SIPStackMessageFactory messageFactory) {
        this.sipMessageFactory = messageFactory;
    }

    /**
     * Creates a new MessageChannel for a given Hop.
     * @param nextHop Hop to create a MessageChannel to.
     * @return A MessageChannel to the specified Hop, or null if
     * no MessageProcessors support contacting that Hop.
     * @throws UnknwonHostException If the host in the Hop doesn't
     * exist.
     */
    public MessageChannel createMessageChannel(Hop nextHop) {
        Host targetHost;
        HostPort targetHostPort;
        MessageProcessor nextProcessor;
        MessageChannel newChannel;

        // Create the host/port of the target hop
        targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "createMessageChannel " + nextHop);
        }

        // Search each processor for the correct transport
        newChannel = null;
        Enumeration processorIterator = messageProcessors.elements();

        while (processorIterator.hasMoreElements() && newChannel == null) {
            nextProcessor =
                    (MessageProcessor) processorIterator.nextElement();
            // If a processor that supports the correct
            // transport is found,
            if (Utils.equalsIgnoreCase
                    (nextHop.getTransport(), nextProcessor.getTransport())) {
                try {
                    // Create a channel to the target host/port
                    newChannel = nextProcessor.
                            createMessageChannel(targetHostPort);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Ignore channel creation error -
                    // try next processor
                }
            }
        }

        if (newChannel == null) { // Message processor was not found
                                  // Try to create it
            try {
                MessageProcessor processor = createMessageProcessor(
                    nextHop.getPort(), nextHop.getTransport());
                // IMPL_NOTE: The previous message processor should be
                // removed on level of re-routing SIP messages
                newChannel = processor.createMessageChannel(targetHostPort);
            } catch (IOException ex) {
            } catch (IllegalArgumentException ex) {
            }
        }
        // Return the newly-created channel
        return newChannel;
    }

    /**
     * Return a security token associated with the protocol class
     * @return Security token
     */
    protected Token getSecurityToken() {
        return securityToken;
    }

    /**
     * Set the security token associated with the protocol class
     * @param token Security token from SIP/SIPS Protocol class
     */
    protected void setSecurityToken(Token token) {
        securityToken = token;
    }

}
