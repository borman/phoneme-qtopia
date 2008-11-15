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
package gov.nist.siplite;

import java.util.*;
import gov.nist.siplite.stack.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;
import gov.nist.microedition.sip.*;
import com.sun.j2me.security.Token;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;
import gov.nist.siplite.SIPConstants;

/**
 * Implementation of SipStack.
 *
 * The JAIN-SIP stack is initialized by a set of properties (see the JAIN
 * SIP documentation for an explanation of these properties).
 * In addition to these, the following are meaningful properties for
 * the NIST SIP stack (specify these in the property array when you create
 * the JAIN-SIP statck).:
 * <ul>
 *
 * <li><b>gov.nist.javax.sip.TRACE_LEVEL = integer </b><br>
 * Currently only 16 and 32 is meaningful.
 * If this is set to 16 or above, then incoming
 * valid messages are  logged in SERVER_LOG. If you set this to 32 and
 * specify a DEBUG_LOG then vast amounts of trace information will be dumped
 * in to the specified DEBUG_LOG.  The server log accumulates the signaling
 * trace.
 * This can be viewed using the trace viewer tool .
 * Please send us both the server log and debug log
 * when reporting non-obvious problems.</li>
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */
public class SipStack
        extends SIPTransactionStack {
    /** Current listening points. */
    private Hashtable listeningPoints;
    /** Vector of SIP providers. */
    private Vector sipProviders;
    /** Flag indicating the provider has been initialized. */
    protected boolean stackInitialized;
    /** Pathto router. */
    protected String routerPath;
    /** Current eventscanner. */
    protected EventScanner  eventScanner;
    /** Current SIP listener. */
    protected SipListener sipListener;
    /** Connector for SIP stack. */
    protected StackConnector sipStackConnector;
    /** Name of the logfile used to log messages */
    private String logFilename = null;
    /** Outbound proxy for this stack */
    protected String outboundProxy = null;
    
    /** Outbound proxy port for this stack */
    protected int outboundPort = -1;

    /**
     * Constructor.
     * @param stackConnector connection to use for SIP Stack
     */
    public void setStackConnector(StackConnector stackConnector) {
        this.sipStackConnector = stackConnector;
        
    }
    
    /**
     * Creates a new instance of SipStack.
     */
    protected SipStack() {
        super();
        NistSipMessageFactoryImpl msgFactory =
                new NistSipMessageFactoryImpl(this);
        super.setMessageFactory(msgFactory);
        this.listeningPoints = new Hashtable();
        this.sipProviders = new Vector();
    }
    
    /**
     * Stops the SIP stack processing.
     */
    public void stopStack() {
        super.stopStack();
        this.eventScanner.stop();
        this.sipStackConnector.releaseInstance();
    }
    
    /**
     * Construct a SIP Stack top match requested configuration.
     * @param configurationProperties selectors for SIP Stack
     * @param classSecurityToken security token for saving
     */
    public SipStack(ConfigurationProperties configurationProperties,
                    Token classSecurityToken)
    throws PeerUnavailableException {
        this();
        this.eventScanner = new EventScanner(this);
        this.eventScanner.start();
        String address = configurationProperties.getProperty
                ("javax.sip.IP_ADDRESS");
        
        /** Retrieve the stack IP address */
        if (address == null)
            throw new PeerUnavailableException("address not specified");
        super.setHostAddress(address);
        
        
        /** Retrieve the stack name */
        String name = configurationProperties.getProperty
                ("javax.sip.STACK_NAME");
        if (name == null)
            throw new PeerUnavailableException("stack name is missing");
        super.setStackName(name);
        
        
        routerPath = "gov.nist.siplite.stack.DefaultRouter";
        outboundProxy =
                configurationProperties.getProperty
                ("javax.sip.OUTBOUND_PROXY");

        Exception ex = null;
        try {
            Class routerClass = Class.forName(routerPath);
            Router router = (Router) routerClass.newInstance();
            if (outboundProxy != null)
                router.setOutboundProxy(outboundProxy);
            router.setSipStack(this);
            super.setRouter(router);
            
        } catch (ClassNotFoundException cnfe) {
            ex = cnfe;
        } catch (InstantiationException ie) {
           ex = ie;
        } catch (IllegalAccessException iae) {
            ex = iae;
        }
        if (ex != null) {
           throw new PeerUnavailableException("Could not instantiate router");
        }
        if (outboundProxy != null) {
            Hop hop = new Hop(outboundProxy);
            this.outboundProxy = hop.getHost();
            this.outboundPort =  hop.getPort();
        }
        
        
        
        /**
         * Retrieve the EXTENSION Methods. These are used for instantiation
         * of Dialogs.
         */
        String extensionMethods =
                configurationProperties.getProperty
                ("javax.sip.EXTENSION_METHODS");
        
        if (extensionMethods != null) {
            gov.nist.core.StringTokenizer st = new
                    gov.nist.core.StringTokenizer(extensionMethods, ':');
            
            while (st.hasMoreChars()) {
                String em = st.nextToken();
                if (em.toUpperCase().equals(Request.BYE)
                || em.toUpperCase().equals(Request.ACK)
                || em.toUpperCase().equals(Request.OPTIONS))
                    throw new PeerUnavailableException
                            ("Bad extension method " + em);
                else this.addExtensionMethod(em.toUpperCase());
            }
        }
        
        /* Set the retransmission filter. For SIPLite this is always true */
        this.retransmissionFilter = true;
        

        String maxConnections =
                configurationProperties.getProperty
                ("gov.nist.javax.sip.MAX_CONNECTIONS");
        if (maxConnections != null) {
            try {
                this.maxConnections = Integer.parseInt(maxConnections);
            } catch (NumberFormatException nfe)  {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "max connections - bad value " + nfe.getMessage());
                }
            }
        }
        
        String threadPoolSize = configurationProperties.getProperty
                ("gov.nist.javax.sip.THREAD_POOL_SIZE");
        if (threadPoolSize != null) {
            try {
                this.threadPoolSize = Integer.parseInt(threadPoolSize);
            } catch (NumberFormatException nfe)  {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "thread pool size - bad value " + ex.getMessage());
                }
            }
        }
        
        String transactionTableSize = configurationProperties.getProperty
                ("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
        if (transactionTableSize != null) {
            try {
                this.transactionTableSize =
                        Integer.parseInt(transactionTableSize);
            } catch (NumberFormatException nfe)  {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "transaction table size - bad value " + 
                         ex.getMessage());
                }
            }
        }
        this.setSecurityToken(classSecurityToken);
    }
    
    
    /**
     * Gets the sip listener for the stack.
     * @return the SIP listener
     */
    public SipListener getSipListener() {
        return this.sipListener;
    }
    
    
    /**
     * Creates a new peer ListeningPoint on this SipStack on a specified
     * host, port and transport and returns a reference to the newly created
     * ListeningPoint object. The newly created ListeningPoint is implicitly
     * attached to this SipStack upon execution of this method, by adding the
     * ListeningPoint to the {@link SipStack#getListeningPoints()} of this
     * SipStack, once it has been successfully created.
     *
     * @param port the port of the new ListeningPoint.
     * @param transport the transport of the new ListeningPoint.
     * SipStack.
     * @return The peer ListeningPoint attached to this SipStack.
     */
    public synchronized ListeningPoint createListeningPoint(int port,
            String transport)
            throws TransportNotSupportedException, IllegalArgumentException {
        if (transport == null)
            throw new NullPointerException("null transport");
        if (port <= 0)
            throw new IllegalArgumentException("bad port");
        if (!Utils.equalsIgnoreCase(transport, SIPConstants.TRANSPORT_UDP) &&
                !Utils.equalsIgnoreCase(transport, SIPConstants.TRANSPORT_TCP))
            throw new TransportNotSupportedException
                    ("bad transport " + transport);
       
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "createListeningPoint " + transport + " / " + port);
        }
        
        String key = ListeningPoint.makeKey
                (super.stackAddress, port, transport);
        
        ListeningPoint lip = (ListeningPoint)listeningPoints.get(key);
        if (lip != null) {
            return lip;
        } else {
            try {
                MessageProcessor messageProcessor =
                        this.createMessageProcessor(port, transport);
                lip = new ListeningPoint(this, this.getIPAddress(), port, 
                                         transport);
                lip.messageProcessor[0] = messageProcessor;
                messageProcessor.setListeningPoint(lip);
                
                if (Utils.equalsIgnoreCase(transport, SIPConstants.
                        TRANSPORT_UDP)) {
                    messageProcessor =
                        this.createMessageProcessor(port, SIPConstants.
                            TRANSPORT_TCP);
                    
                    lip.messageProcessor[1] = messageProcessor;
                    messageProcessor.setListeningPoint(lip);                
                } else {
                    lip.messageProcessor[1] = null;
                }                
                
                this.listeningPoints.put(key, lip);
                return  lip;
            } catch (java.io.IOException ex) {
                if (lip != null) {
                    try {
                       deleteListeningPoint(lip);
                    } catch (ObjectInUseException e) {} // ignore
                }
                throw new IllegalArgumentException(ex.getMessage());
            }
        }
    }
    /**
     * Creates a new peer SipProvider on this SipStack on a specified
     * ListeningPoint and returns a reference to the newly created SipProvider
     * object. The newly created SipProvider is implicitly attached to this
     * SipStack upon execution of this method, by adding the SipProvider to the
     * {@link SipStack#getSipProviders()} of this SipStack, once it has been
     * successfully created.
     *
     * @param listeningPoint the ListeningPoint the SipProvider is to
     * be attached to in order to send and Receive messages.
     * @return The peer SipProvider attached to this SipStack on the specified
     * ListeningPoint.
     * @throws ListeningPointUnavailableException thrown if another
     * SipProvider is already using the ListeningPoint.
     */
    public SipProvider createSipProvider(ListeningPoint listeningPoint)
    throws ObjectInUseException {
        if (listeningPoint == null)
            throw new NullPointerException("null listeningPoint");
        
	if (listeningPoint.sipProviderImpl != null) {
	    throw new ObjectInUseException
                    ("Provider already attached!");
        }
        
        SipProvider provider = new SipProvider(this);
        provider.setListeningPoint(listeningPoint);
        this.sipProviders.addElement(provider);
        return provider;
    }
        
    /**
     * Deletes the specified peer ListeningPoint attached to this SipStack. The
     * specified ListeningPoint is implicitly detached from this SipStack upon
     * execution of this method, by removing the ListeningPoint from the
     * {@link SipStack#getListeningPoints()} of this SipStack.
     *
     * @param listeningPoint the peer SipProvider to be deleted from
     * this SipStack.
     * @exception ObjectInUseException thrown if the specified peer
     * ListeningPoint cannot be deleted because the peer ListeningPoint is
     * currently in use.
     *
     * @since v1.1
     */
    public void deleteListeningPoint(ListeningPoint listeningPoint)
    throws ObjectInUseException {
        if (listeningPoint == null)
            throw new NullPointerException("null listeningPoint arg");
        ListeningPoint lip = (ListeningPoint) listeningPoint;
        // Stop the message processing thread in the listening point.
        lip.messageProcessor[0].stop();
        if (lip.messageProcessor[1] != null){
            lip.messageProcessor[1].stop();
        }
        String key = lip.getKey();
        this.listeningPoints.remove(key);
    }
    
    /**
     * Deletes the specified peer SipProvider attached to this SipStack. The
     * specified SipProvider is implicitly detached from this SipStack upon
     * execution of this method, by removing the SipProvider from the
     * {@link SipStack#getSipProviders()} of this SipStack. Deletion of a
     * SipProvider does not automatically delete the ListeningPoint from the
     * SipStack.
     *
     * @param sipProvider the peer SipProvider to be deleted from
     * this SipStack.
     * @exception ObjectInUseException thrown if the specified peer
     * SipProvider cannot be deleted because the peer SipProvider is currently
     * in use.
     *
     */
    public void deleteSipProvider(SipProvider sipProvider)
    throws ObjectInUseException {
        
        if (sipProvider == null)
            throw new NullPointerException("null provider arg");
        SipProvider sipProviderImpl = (SipProvider) sipProvider;
        if ((sipProviderImpl.listeningPoint.messageProcessor[0].inUse()) ||
            ((sipProviderImpl.listeningPoint.messageProcessor[1] != null) &&
             (sipProviderImpl.listeningPoint.messageProcessor[1].inUse()))) {
            throw new ObjectInUseException("Provider in use");
        }
        sipProviderImpl.sipListener = null;
        sipProviders.removeElement(sipProvider);
    }
    
    /**
     * Gets the IP Address that identifies this SipStack instance. Every Sip
     * Stack object must have an IP Address and only a single SipStack object
     * can service a single IP Address. This value is set using the Properties
     * object passed to the {@link
     * SipFactory#createSipStack} method upon
     * creation of the SIP Stack object.
     *
     * @return a string identifing the IP Address
     * @since v1.1
     */
    public String getIPAddress() {
        return super.getHostAddress();
    }
    
    /**
     * Returns an Iterator of existing ListeningPoints created by this
     * SipStack. All of the peer SipProviders of this SipStack will be
     * proprietary objects belonging to the same stack vendor.
     *
     * @return an Iterator containing all existing peer ListeningPoints created
     * by this SipStack. Returns an empty Iterator if no ListeningPoints exist.
     */
    public java.util.Enumeration getListeningPoints() {
        return this.listeningPoints.elements();
    }
    
    /**
     * Gets the listening point for a given transport and port.
     * @param port the communication port
     * @param transport the connection channel
     * @return the listening port
     */
    public ListeningPoint getListeningPoint(int port, String transport) {
        String key = ListeningPoint.makeKey
                (super.stackAddress, port, transport);
                
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "getListeningPoint " + port + "/" + transport);
        }
        
        return (ListeningPoint) listeningPoints.get(key);
    }
    
    
    /**
     * Gets the outbound proxy specification. Return null if no outbound
     * proxy is specified.
     * @return the outbound proxy address
     */
    public String getOutboundProxy() {
        return this.outboundProxy;
    }
    
    /**
     * This method returns the value of the retransmission filter helper
     * function for User Agent Client and User Agent Server applications. This
     * value is set using the Properties object passed to the
     * {@link SipFactory#createSipStack} method upon
     * creation of the SIP Stack
     * object.
     * <p>
     * The default value of the retransmission filter boolean is
     * <var>false</var>.
     * When retransmissions are handled by the SipProvider the application will
     * not receive {@link Timeout#RETRANSMIT} notifications encapsulated in
     * {@link gov.nist.siplite.TimeoutEvent}'s. However an application will get
     * notified when a the underlying transaction expired with
     * {@link Timeout#TRANSACTION} notifications encapsulated in a
     * {@link gov.nist.siplite.TimeoutEvent}.</p>
     *
     * @return the value of the retransmission filter, true if the filter
     * is set false otherwise.
     * @since v1.1
     */
    public boolean isRetransmissionFilterActive() {
        return this.retransmissionFilter;
    }
    
    /**
     * Gets the Router object that identifies the default
     * Routing policy of this
     * SipStack. It also provides means to set an outbound proxy. This value is
     * set using the Properties object passed to the
     * {@link SipFactory#createSipStack} method upon
     * creation of the SIP Stack object.
     *
     * @return a the Router object identifying the Router policy.
     * @since v1.1
     */
    public Router getRouter() {
        return  super.getRouter();
    }
    
    /**
     * Returns an Iterator of existing peer SipProviders that have been
     * created by this SipStack. All of the peer SipProviders of this
     * SipStack will be proprietary objects belonging to the same stack vendor.
     *
     * @return an Iterator containing all existing peer SipProviders created
     * by this SipStack. Returns an empty Iterator if no SipProviders exist.
     */
    public Enumeration getSipProviders() {
        return this.sipProviders.elements();
    }
    
    /**
     * Gets the user friendly name that identifies this SipStack instance. This
     * value is set using the Properties object passed to the
     * {@link SipFactory#createSipStack} method upon
     * creation of the SIP Stack object.
     *
     * @return a string identifing the stack instance
     */
    public String getStackName() {
        return this.stackName;
    }
    
    
    /**
     * The default transport to use for via headers.
     * @return the default transport
     */
    public String getDefaultTransport() {
        if (isTransportEnabled("udp"))
            return "udp";
        else if (isTransportEnabled("tcp"))
            return "tcp";
        else
            return null;
    }
    
    
    
    /**
     * Invoked when an error has ocurred with a transaction.
     *
     * @param transactionErrorEvent Error event.
     */
    public void transactionErrorEvent
            (SIPTransactionErrorEvent transactionErrorEvent) {
        Transaction transaction =
                (Transaction) transactionErrorEvent.getSource();
        
    }
}

