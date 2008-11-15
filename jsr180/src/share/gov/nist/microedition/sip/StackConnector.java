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
 * StackConnector.java
 *
 * Created on Feb 11, 2004
 *
 */
package gov.nist.microedition.sip;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;

import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipDialog;

import gov.nist.microedition.io.j2me.sip.DistributedRandom;
import gov.nist.siplite.ConfigurationProperties;
import gov.nist.siplite.ListeningPoint;
import gov.nist.siplite.ObjectInUseException;
import gov.nist.siplite.PeerUnavailableException;
import gov.nist.siplite.RequestEvent;
import gov.nist.siplite.ResponseEvent;
import gov.nist.siplite.SipFactory;
import gov.nist.siplite.SipListener;
import gov.nist.siplite.SipProvider;
import gov.nist.siplite.SipStack;
import gov.nist.siplite.TimeoutEvent;
import gov.nist.siplite.TooManyListenersException;
import gov.nist.siplite.TransportNotSupportedException;
import gov.nist.siplite.address.AddressFactory;
import gov.nist.siplite.address.SipURI;
import gov.nist.siplite.header.HeaderFactory;
import gov.nist.siplite.header.AcceptContactHeader;
import gov.nist.siplite.message.MessageFactory;
import gov.nist.siplite.message.Request;
import gov.nist.siplite.message.Response;
import gov.nist.siplite.stack.ClientTransaction;
import gov.nist.siplite.stack.ServerTransaction;
import gov.nist.core.NameValueList;

import com.sun.j2me.security.Token;
import gov.nist.siplite.SIPConstants;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

import gov.nist.core.PushUtil;

/**
 * This class is the connector between the JSR180 and
 * the nist-siplite stack. This class create a stack from the
 * SipConnector.open(SIP_URI) with the listening point equals
 * to the one specified in the SIP URI. If none is specified, a random one is
 * allowed by the system.
 * This class receive the messages from the stack because it's implementing the
 * SipListener class and transmit them to either SipConnectionNotifier or
 * SipClientConnection or both.
 * This class follow the singleton design pattern and is thread-safe
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class StackConnector implements SipListener {

    /**
     * Security token for SIP/SIPS protocol class
     */
    private Token classSecurityToken;
    /**
     * The unique instance of this class
     */
    private static StackConnector instance = null;
    /**
     * The actual stack
     */
    protected SipStack sipStack = null;
    /**
     * listen address
     */
    private String localAddress = null;
    /**
     * Temporary listening point.
     */
    private ListeningPoint tempListeningPoint = null;
    /**
     * Temporary sip provider.
     */
    private SipProvider tempSipProvider = null;
    /**
     * list of connection notifiers
     */
    protected Vector connectionNotifiersList = null;
    /**
     * list of client connections
     */
    // protected Vector clientConnectionList = null;
    /**
     * list of all current dialogs
     */
    protected Vector sipDialogList = null;
    /**
     * Address factory handle.
     */
    public static AddressFactory addressFactory = null;
    /**
     * Message factory handle.
     */
    public static MessageFactory messageFactory = null;
    /**
     * Header factory handle.
     */
    public static HeaderFactory  headerFactory = null;
    /**
     * Shared listening point
     */
    private ListeningPoint sharedListeningPoint = null;
    /**
     * Shared sipProvider instance
     */
    private SipProvider sharedSipProvider = null;
    /**
     * Shared port number
     */
    int sharedPortNumber = -1;

    /**
     * Indicates mime types used by applications for SipConnectionNotifiers
     * in shared mode
     */
    Vector sharedMimeTypes = null;

    static {
        // Creates the factories to help to construct messages
        messageFactory = new MessageFactory();
        addressFactory = new AddressFactory();
        headerFactory = new HeaderFactory();
    }

    /**
     * Constructor
     * Creates the stack
     * @param classSecurityToken security token for SIP/SIPS protocol class
     */
    private StackConnector(Token classSecurityToken)
    throws IOException {
        com.sun.j2me.io.ConnectionBaseInterface conn;

        connectionNotifiersList = new Vector();
        // clientConnectionList = new Vector();
        sipDialogList = new Vector();
        // Create the sipStack
        SipFactory sipFactory  =  SipFactory.getInstance();
        ConfigurationProperties properties = new ConfigurationProperties();
        int randomPort = new DistributedRandom().nextInt(60000)+1024;

        /*
         * Original NIST method for opening the serversocket connection
         * has been replaced by direct calls to instantiate the protocol
         * handler, in order to pass the security token for use of lower
         * level transport connection.
         * Original NIST sequence is :
         *
         * ServerSocketConnection serverSoc =
         *      (ServerSocketConnection)Connector.open("socket://:"+randomPort);
         *
         */

        ServerSocketConnection serverSoc =
                com.sun.j2me.conn.Connector.getServerSocketConnection(classSecurityToken,
                "//:" + randomPort);

        localAddress = serverSoc.getLocalAddress();
        properties.setProperty("javax.sip.IP_ADDRESS", localAddress);
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
        properties.setProperty("gov.nist.javax.sip.LOG_FILE_NAME",
                "/tmp/jsr180-log");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                "0"); // TRACE_PRIVATE

        // Initialise the log file
        serverSoc.close();
        try {
            //  Create SipStack object
            sipStack = sipFactory.createSipStack(properties,
                    classSecurityToken);
            sipStack.setStackConnector(this);
        } catch (PeerUnavailableException e) {
            //  SipStackImpl in the classpath
            // e.printStackTrace();
            throw new IOException(e.getMessage());
        }
        // save security token
        this.classSecurityToken = classSecurityToken;
    }

    /**
     * Get the unique instance of this class
     * @param classSecurityToken security token for SIP/SIPS protocol class
     * @return the unique instance of this class
     */
    public synchronized static StackConnector getInstance(Token
            classSecurityToken)
            throws IOException {
        if (instance == null)
            instance = new StackConnector(classSecurityToken);
        return instance;
    }

    /**
     * remove the instance of the stack.
     */
    public synchronized static void releaseInstance() {
        instance = null;
    }

    /**
     * Creates a sip connection notifier in shared mode. In shared mode,
     * SipConnectionNotifier is supposed to use a single shared port or
     * listening point for all applications
     *
     * @param secure  flag to specify whether to use or not the secure layer
     * @param transport  transport protocol name
     * @param mimeType parameter for filtering incomming SIP packets or null
     * @return the sip connection notifier that will receive request
     * @throws IOException  if we cannot create the sip connection notifier
     * for whatsoever reason
     */
    public SipConnectionNotifier createSharedSipConnectionNotifier(
            boolean secure,
            String transport,
            String mimeType)
            throws IOException {

        if (sharedMimeTypes != null) {
            for (int i = 0; i < sharedMimeTypes.size(); i++) {
                if (mimeType.equalsIgnoreCase(
                        ((String)sharedMimeTypes.elementAt(i)))) {
                    throw new IOException("Application type is already " +
                            "reserved");
                }
            }
        } else {
            sharedMimeTypes = new Vector();
        }

        /*
         * Add the mimeType to sharedMimeTypes vector
         */
        sharedMimeTypes.addElement((String)mimeType);

        /*
         * SipConnectionNotifier in shared mode must use shared system SIP port
         * and shared SIP identity. So a shared listening point and sipProvider
         * must be used for every SipConnectionNotifier in shared mode
         */

        if ((sharedListeningPoint == null) &&
            (sharedSipProvider == null)) {

            // select a free port
            sharedPortNumber = selectPort(sharedPortNumber, transport);

            sharedListeningPoint =
                this.tempListeningPoint; // initialized by "selectPort()"
            sharedSipProvider =
                this.tempSipProvider; // initialized by "selectPort()"
        }

        SipConnectionNotifier sipConnectionNotifier =
                new SipConnectionNotifierImpl(sharedSipProvider, localAddress,
                sharedPortNumber, this.classSecurityToken, mimeType, true, 
                secure);
        // ((SipConnectionNotifierImpl)sipConnectionNotifier).start();
        // Add the the newly created sip connection notifier to the list of
        // connection notifiers
        this.connectionNotifiersList.addElement(sipConnectionNotifier);

        return sipConnectionNotifier;
    }

    /**
     * Create a listening point ans sip provider on given or random
     * selected port. When port is selected successfully,
     * ListeningPoint and SipProvider instances are created.
     * @param portNumber the number of the port on which we must listen
     * for incoming requests or -1 to select random port
     * @param transport transport protocol name
     * @return the selected port number
     * @throws IOException if given port is busy
     */
    private int selectPort(int portNumber, String transport)
        throws IOException {

        boolean isRandomPort = (portNumber == -1);
        final int MAX_ATTEMPTS = 1000;
        int attemps = 0;

        if (isRandomPort) {
            // Try the default port first.
            portNumber = SIPConstants.DEFAULT_NONTLS_PORT;
        }
        
        do {
            if (attemps++ > MAX_ATTEMPTS) {
                throw new IOException("Cannot select a port!");
            }

            // Creates the listening point
            // IMPL_NOTE : Use the parameters to restrain the incoming messages
            try {
                tempListeningPoint =
                    sipStack.createListeningPoint(portNumber, transport);
            } catch (TransportNotSupportedException tnse) {
                // tnse.printStackTrace();
                throw new IOException(tnse.getMessage());
            } catch (IllegalArgumentException iae) {
                if (isRandomPort) { // port is busy
                    // Select a random port from 1024 to 10000.
                    portNumber = new DistributedRandom().nextInt(8975) + 1024;
                    continue;
                } else {
                    throw new IOException(iae.getMessage());
                }
            }

            // Creates the sip provider
            try {
                tempSipProvider =
                    sipStack.createSipProvider(tempListeningPoint);
            } catch (ObjectInUseException oiue) {
                if (isRandomPort) { // port is busy
                    // Select a random port from 1024 to 10000.
                    portNumber = new DistributedRandom().nextInt(8975) + 1024;
                    continue;
                } else {
                    // oiue.printStackTrace();
                    throw new ObjectInUseException(oiue.getMessage());
                }
            }
            
            isRandomPort = false;
            
            // Add this class as a listener for incoming messages
            try {
                tempSipProvider.addSipListener(this);
            } catch (TooManyListenersException tmle) {
                // tmle.printStackTrace();
                throw new IOException(tmle.getMessage());
            }
        } while (isRandomPort);

        return portNumber;
    }

    /**
     * Create a sip connection notifier on a specific port
     * using or not the sip secure layer and with some restrictive parameters
     * to receive requests
     * @param portNumber the number of the port on which we must listen
     * for incoming requests or -1 to select random port
     * @param secure flag to specify whether to use or not the secure layer
     * @param transport transport protocol name
     * @param mimeType parameter for filtering incomming SIP packets or null
     * @return the sip connection notifier that will receive request
     * @throws IOException if we cannot create the sip connection notifier
     * for whatsoever reason
     */
    public SipConnectionNotifier createSipConnectionNotifier(
        int portNumber,
        boolean secure,
        String transport,
        String mimeType)
        throws IOException {

        // select a free port (if need)
        portNumber = selectPort(portNumber, transport);

        SipConnectionNotifier sipConnectionNotifier =
                new SipConnectionNotifierImpl(tempSipProvider, localAddress,
                portNumber, this.classSecurityToken, mimeType, false, secure);
        // ((SipConnectionNotifierImpl)sipConnectionNotifier).start();
        // Add the the newly created sip connection notifier to the list of
        // connection notifiers
        this.connectionNotifiersList.addElement(sipConnectionNotifier);

        return sipConnectionNotifier;
    }

    /**
     * Creates a sip Client Connection to send a request to the
     * following SIP URI user@host:portNumber;parameters
     * @param inputURI input SIP URI
     * @return the sip client connection
     */
    public SipClientConnection createSipClientConnection(SipURI inputURI) {

        SipClientConnection sipClientConnection =
                new SipClientConnectionImpl(inputURI, this.classSecurityToken);
        return sipClientConnection;
    }

    /**
     * Gets the current connection notifier list.
     * @return the connection notifier list
     */
    public Vector getConnectionNotifiersList() {
        return connectionNotifiersList;
    }

    /**
     * Gets the current SipStack object.
     * @return the current SipStack object
     */
    public SipStack getSipStack() {
        return sipStack;
    }

    /**
     * Retrieve from the list of connection notifier the one that use the same
     * port as in parameter
     *
     * @param portNumber the port number
     * @param acceptContactType MIME type as in Accept-Contact header
     *
     * @return the connection notifier matching the same port
     */
    public SipConnectionNotifier getSipConnectionNotifier(int portNumber,
            String acceptContactType) {
        Enumeration e = connectionNotifiersList.elements();
        while (e.hasMoreElements()) {
            SipConnectionNotifier sipConnectionNotifier =
                    (SipConnectionNotifier)e.nextElement();
            try {
                if (sipConnectionNotifier.getLocalPort() != portNumber) {
                    continue;
                }

                if (acceptContactType != null) {
                    String scnMimeType =
                            ((SipConnectionNotifierImpl)sipConnectionNotifier).
                            getMIMEType();
                    if (scnMimeType != null) {
                        if (scnMimeType.equalsIgnoreCase(acceptContactType) ||
                                "*".equals(acceptContactType)) {
                            return sipConnectionNotifier;
                        }
                    }
                } else {
                    return sipConnectionNotifier;
                }
            } catch (IOException ioe) {
                // Intentionally ignored.
            }
        }
        return null;
    }

   /**
     * Retrieve from the list of connection notifier the one that use the same
     * port as in parameter
     *
     * @param acceptContactType MIME type as in Accept-Contact header
     *
     * @return the connection notifier matching the same port
     */
    public SipConnectionNotifier getSipConnectionNotifier(String acceptContactType) {
        return getSipConnectionNotifier(sharedPortNumber, acceptContactType);
    }

    /**
     * generate a random tag that can be used either in the FromHeader or in the
     * ToHeader
     * @return the randomly generated tag
     */
    protected static String generateTag() {
        return String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
    }

    /**
     * find in the dialog list, the sip dialog with the same dialog ID
     * as the one in parameter
     * @param dialogID dialogID to test against
     * @return the sip dialog with the same dialog ID
     */
    protected SipDialog findDialog(String dialogID) {
        Enumeration e = sipDialogList.elements();
        while (e.hasMoreElements()) {
            SipDialog sipDialog = (SipDialog)e.nextElement();
            if (sipDialog.getDialogID() != null &&
                    sipDialog.getDialogID().equals(dialogID)) {
                return sipDialog;
            }
        }
        return null;
    }


    /**
     * Processes the current transaction event.
     * @param requestEvent the protocol transition event
     */
    public void processRequest(RequestEvent requestEvent) {
        try {
            String acceptContactType = null;
            Request request = requestEvent.getRequest();
            AcceptContactHeader acHdr = request.getAcceptContact();
            if (acHdr != null) { // Accept-Contact header present
                acceptContactType = acHdr.getType();
            }

            // Retrieve the SipConnectionNotifier from the transaction
            SipConnectionNotifierImpl sipConnectionNotifier = null;
            ServerTransaction serverTransaction =
                    requestEvent.getServerTransaction();
            if (serverTransaction != null)
                sipConnectionNotifier =
                        (SipConnectionNotifierImpl)serverTransaction
                        .getApplicationData();
            // If it's a new request coming in, the
            // sipConnectionNotifier will certainly be null, so
            // retrieve from the list of connection notifier the one
            // that use the same port as in parameter
            if (sipConnectionNotifier == null) {
                SipProvider sipProvider = (SipProvider)requestEvent.getSource();
                ListeningPoint listeningPoint = sipProvider.getListeningPoint();
                sipConnectionNotifier =
                        ((SipConnectionNotifierImpl)
                        getSipConnectionNotifier(listeningPoint.getPort(),
                            acceptContactType));
                
                if ((serverTransaction != null) &&
                    (sipConnectionNotifier != null)) {
                    serverTransaction.setApplicationData(sipConnectionNotifier);
                }
            }

            if (sipConnectionNotifier != null) {
                if (!PushUtil.checkSipFilters(sipConnectionNotifier, request))
                    return;
                sipConnectionNotifier.notifyRequestReceived(request);
            } else {
                // No need to throw any RuntimeException; just log the error
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "we cannot find any connection notifier" +
                            "matching to handle this request");
                }
            }
        } catch (NullPointerException npe) {
            // npe.printStackTrace();
        } catch (IllegalArgumentException iae) {
            // iae.printStackTrace();
        }
    }

    /**
     * Processes the resposne event.
     * @param responseEvent the transition reply event
     */
    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();

            // Retrieve the SipClientConnection from the transaction
            ClientTransaction clientTransaction =
                    responseEvent.getClientTransaction();
            SipClientConnectionImpl sipClientConnection =
                (SipClientConnectionImpl)clientTransaction.getApplicationData();

            if (sipClientConnection != null) {
                // translate null when client transactions are same
                if (sipClientConnection.getClientTransaction()
                    .equals(clientTransaction)) {
                    sipClientConnection.notifyResponseReceived(response, null);
                } else { // send new client transaction
                    sipClientConnection.notifyResponseReceived(response,
                        clientTransaction);
                }
            } else {
                throw new RuntimeException(
                    "we cannot find any client connection" +
                        "matching to handle this request");
            }
        } catch (NullPointerException npe) {
            // npe.printStackTrace();
        } catch (IllegalArgumentException iae) {
            // iae.printStackTrace();
        }
    }

    /**
     * Process a timeout event.
     * @param timeoutEvent state transition timeout event
     */
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    /**
     * Close a SipConnectionNotifier in shared mode
     *
     * @param mimeType MIME type of the SipConnectionNotifier
     */
    public void closeSharedSipConnectionNotifier(String mimeType)
    throws IOException {

        // Ignore when sharedMimeTypes already removed
        if (sharedMimeTypes == null) {
            return;
        }

        sharedMimeTypes.removeElement((String)mimeType);

        if (sharedMimeTypes.size() == 0) {
            try {
                sipStack.deleteListeningPoint(sharedListeningPoint);
                sipStack.deleteSipProvider(sharedSipProvider);
            } catch (ObjectInUseException oiue) {
                throw new IOException(oiue.getMessage());
            }

            sharedMimeTypes = null;

            /*
             * There are no more associated listening points in shared mode;
             * so stop the stack
             */
            sipStack.stopStack();
        }
    }
}
