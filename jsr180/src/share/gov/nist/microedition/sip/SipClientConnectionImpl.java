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
 *   
 *
 * Created on Jan 29, 2004
 *
 */
package gov.nist.microedition.sip;

import gov.nist.core.ParseException;
import gov.nist.core.NameValue;
import gov.nist.siplite.SIPErrorCodes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Vector;
import java.util.Enumeration;

import com.sun.j2me.rms.RecordEnumeration;
import com.sun.j2me.rms.RecordStore;
import com.sun.j2me.rms.RecordStoreException;

import javax.microedition.sip.SipConnection;

import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipClientConnectionListener;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipDialog;
import javax.microedition.sip.SipException;
import javax.microedition.sip.SipRefreshListener;
import javax.microedition.sip.SipErrorListener;

import gov.nist.siplite.TransactionUnavailableException;
import gov.nist.siplite.SipStack;
import gov.nist.siplite.address.Address;
import gov.nist.siplite.address.SipURI;
import gov.nist.siplite.address.URI;
import gov.nist.siplite.message.*;
import gov.nist.siplite.stack.Subscription;
import gov.nist.siplite.stack.ClientTransaction;
import gov.nist.siplite.stack.Dialog;
import gov.nist.siplite.stack.authentication.Credentials;
import gov.nist.siplite.stack.authentication.DigestClientAuthentication;
import gov.nist.siplite.header.CSeqHeader;
import gov.nist.siplite.header.CallIdHeader;
import gov.nist.siplite.header.ContactHeader;
import gov.nist.siplite.header.ContentLengthHeader;
import gov.nist.siplite.header.ContactList;
import gov.nist.siplite.header.ContentTypeHeader;
import gov.nist.siplite.header.ExpiresHeader;
import gov.nist.siplite.header.FromHeader;
import gov.nist.siplite.header.Header;
import gov.nist.siplite.header.HeaderList;
import gov.nist.siplite.header.MaxForwardsHeader;
import gov.nist.siplite.header.ToHeader;
import gov.nist.siplite.header.ViaHeader;
import gov.nist.siplite.header.SubscriptionStateHeader;
import gov.nist.siplite.header.ParameterLessHeader;
import gov.nist.siplite.header.ExtensionHeader;
import gov.nist.siplite.header.HeaderFactory;
import gov.nist.siplite.SIPUtils;
import gov.nist.siplite.SIPConstants;
import gov.nist.core.NameValueList;
import gov.nist.core.Utils;
import gov.nist.core.Separators;
import com.sun.j2me.security.Token;
import gov.nist.siplite.parser.Lexer;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Client SIP connection implementation.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class SipClientConnectionImpl
    implements SipClientConnection {
    // Runnable {
    /**
     * Security token for SIP/SIPS protocol class
     */
    private Token classSecurityToken;

    // Client Transaction States
    /**
     * Terminated, the final state, in which the SIP connection has
     * been terminated by error or closed
     */
    public static final int TERMINATED = 0;
    /**
     * Created, SipClientConnection created from Connector or SipDialog
     */
    public static final int CREATED = 1;
    /**
     * Initialized, request has been initialized with initRequest(...)
     * or initAck()  or initCancel()
     */
    public static final int INITIALIZED = 2;
    /**
     * Stream Open, OutputStream opened with openContentOutputStream().
     * Opening InputStream for received response does not trigger
     * state transition.
     */
    public static final int STREAM_OPEN = 3;
    /**
     * Proceeding, request has been sent, waiting for the response, or
     * provisional 1xx response received. initCancel() can be called,
     * which will spawn a new SipClientConnection which is in
     * Initialized state
     */
    public static final int PROCEEDING = 4;
    /**
     * Unauthorized, transaction completed with response 401
     * (Unauthorized) or 407 (Proxy Authentication Required).
     */
    public static final int UNAUTHORIZED = 5;
    /**
     * Completed, transaction completed with final response
     * (2xx, 3xx, 4xx, 5xx, 6xx) in this state the ACK can be initialized.
     * Multiple 200 OK responses can be received. Note different state
     * transition for responses 401 and 407.
     */
    public static final int COMPLETED = 6;

    /**
     * Max length of queue of incoming responses.
     */
    private static final int MAX_NUM_RESPONSES = 10;

    /**
     * the sip dialog this client transaction belongs to
     */
    private SipDialog sipDialog = null;

    /**
     * Listener to notify when a response will be received
     */
    private SipClientConnectionListener sipClientConnectionListener = null;
    /**
     * Listener to notify about failures of an asynchoronous send operation
     */
    private SipErrorListener sipErrorListener = null;
    /**
     * The Sip Connection notifier associated with this client connection
     */
    private SipConnectionNotifier sipConnectionNotifier = null;
    /**
     * Callback interface for refresh events
     */
    private SipRefreshListener sipRefreshListener = null;
    /**
     * The refresh ID of the refresh task associated with this client
     * connection if there is any
     */
    private String refreshID = null;
    /**
     * current state of this client transaction
     */
    protected int state;
    /**
     * flag to know the state of the connection (open or close)
     */
    private boolean connectionOpen;
    /**
     * list of credentials that can be used for authorization
     */
    private Vector credentials;
    /**
     * The request for this client transaction.
     */
    private Request request = null;
    /**
     * The initial request saved before ACK is sent.
     */
    private Request requestSavedBeforeACK = null;
    /**
     * the response to the actual request
     */
    private Response response = null;
    /**
     * The queue of responses for processing.
     */
    private Vector responses = new Vector();
    /**
     * the last received response
     */
    private Response responseReceived = null;
    /**
     * content of the response body
     */
    private SDPOutputStream contentOutputStream = null;
    /**
     * content from the request body
     */
    private InputStream contentInputStream = null;
    /**
     * The request URI created from the user, host, port and
     * parameters attributes
     */
    private URI requestURI = null;
    /**
     * Scheme name
     */
    private String scheme = null;
    /**
     * the user part of the SIP URI
     */
    private String user = null;
    /**
     * the host part of the SIP URI
     */
    private String host = null;
    /**
     * the port Number on which to send the request, part of
     * the SIP URI
     */
    private int port = -1;
    /**
     * the parameters of the SIP URI
     */
    private NameValueList parameters = null;
    /**
     * the sip uri of the user
     */
    private String userSipURI = "sip:anonymous@anonymous.invalid";
    /**
     * the client Transaction for an INVITE request
     */
    private ClientTransaction clientTransaction = null;
    /**
     * Handle for asynchronous listening thread.
     */
    private Thread listeningThread = null;
    /**
     * Current stack of connectors.
     */
    private StackConnector stackConnector = null;
    /**
     * Flag of creating internal notifier.
     */
    private boolean isNotifierCreated = false;
    /**
     * Flag indicating which SIP message (request or response)
     * should be used in getHeader()/getHeaders().
     */
    private boolean useRequest;
    /**
     * Permission of generating CANCEL request.
     */
    protected boolean enableInitCancel = false;
    /**
     * Count of authorization requests (RFC 2617, 3.2.2).
     */
    private int countReoriginateRequest = 1;
    /**
     * The flag indicates if the error response (3xx-6xx) was received on the 
     * connection. It is used by getDialog() method
     */    
    private boolean isErrorResponseReceived = false;
    
    /**
     * State of update operation. false stands for ongoing update operation,
     * true - valid state to start update operation
     */
    private boolean updateState = false;

    /**
     * Creates a sip Client Connection to send a request to the
     * following SIP URI user@host:portNumber;parameters
     * @param inputURI input SIP URI
     * @param classSecurityToken Security token for SIP/SIPS protocol class
     */
    protected SipClientConnectionImpl(
        SipURI inputURI,
        Token classSecurityToken) throws IllegalArgumentException {
        this.user = inputURI.getUser();
        this.host = inputURI.getHost();
        this.port = inputURI.getPort();
        this.parameters = inputURI.getUriParms();
        this.classSecurityToken = classSecurityToken;

        this.scheme = inputURI.getScheme();

        connectionOpen = true;
        credentials = new Vector();

        try {
            stackConnector = StackConnector.getInstance(classSecurityToken);
        } catch (IOException ioe) {
        }

        // Create the REQUEST URI of the request
        try {
            requestURI = StackConnector.addressFactory.createURI(scheme + ":" +
                ((user == null) ? "" : (user + "@")) + host);

            if (port != -1) {
                ((SipURI) requestURI).setPort(port);
            }

            // handle the parameters
            if (parameters != null) {
                Enumeration parNames = parameters.getKeys();
                while (parNames.hasMoreElements()) {
                    String name = (String) parNames.nextElement();
                    String value = (String) parameters.getValue(name);
                    ((SipURI) requestURI).setParameter(name, value);
                }
            }
        } catch (ParseException pe) {
            throw new
                IllegalArgumentException("The request URI can not be" +
                                         " created, check the URI syntax");
        }

        state = CREATED;
        useRequest = true;
    }

    /**
     * Constructs the client connection implementation.
     * @param requestURI the target SIP session URI
     * @param sipDialog the current transaction state
     */
    protected SipClientConnectionImpl(URI requestURI,
                                      SipDialog sipDialog)
        throws IllegalArgumentException {
        if (!requestURI.isSipURI()) {
            throw new IllegalArgumentException("URI is not correct");
        }

        SipURI sipURI = (SipURI) requestURI;
        SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;

        user = sipURI.getUser();
        host = sipURI.getHost();
        port = sipURI.getPort();
        parameters = sipURI.getUriParms();
        classSecurityToken = sipDialogImpl.getSecurityToken();
        scheme = requestURI.getScheme();
        connectionOpen = true;
        credentials = new Vector();

        // Create the REQUEST URI of the request
        this.requestURI = requestURI;
        this.sipDialog = sipDialog;
        this.refreshID = sipDialogImpl.getRefreshID();

        // this.sipClientConnectionListener =
        //    ((SipDialogImpl)sipDialog).getSipClientConnectionListener();

        try {
            stackConnector = StackConnector.getInstance(classSecurityToken);
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "Could not create SipClientConnectionImpl: " +
                               ioe);
            }
        }

        sipDialogImpl.dialog.setStack(stackConnector.getSipStack());

        state = CREATED;
        useRequest = true;
    }

    /**
     * Constructs the client connection implementation.
     * @param request the current transaction
     * @param sipConnectionNotifier the state transition notifier
     * @param sipUserURI the user session information
     */
    private SipClientConnectionImpl(Request request,
                                    SipConnectionNotifier
                                    sipConnectionNotifier,
                                    String sipUserURI)
        throws IllegalArgumentException {
        connectionOpen = true;
        credentials = new Vector();
        // Create the REQUEST of the connection
        this.request = request;
        this.userSipURI = sipUserURI;
        // Create the REQUEST URI of the request
        this.requestURI = request.getRequestURI();
        this.sipConnectionNotifier = sipConnectionNotifier;

        try {
            stackConnector = StackConnector.getInstance(classSecurityToken);
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "Could not create SipClientConnectionImpl: " +
                               ioe);
            }
        }

        if (request.getMethod().equals(Request.CANCEL)) {
            state = INITIALIZED;
        } else {
            state = CREATED;
        }

        useRequest = true;
    }

    /**
     * Initializes the connection.
     * @param method the operation to be performed
     * @param scn the state transition notifier
     * @throws NullPointerException - if the method is null
     * @throws IllegalArgumentException - if the method is invalid
     * @throws SipException - INVALID_STATE if the request can not be set,
     * because of wrong state in SipClientConnection. Furthermore, ACK and
     * CANCEL methods can not be initialized in Created state.
     * @throws SipException - INVALID_OPERATION if the method argument is one
     * of {BYE, NOTIFY, PRACK, UPDATE}
     */
    public void initRequest(String method, SipConnectionNotifier scn) throws
        SipException {

        if (method == null) {
            throw new NullPointerException("The method can not be null");
        }

        if (!Lexer.isValidName(method)) {
            throw new IllegalArgumentException("Invalid method: '" +
                                               method + "'");
        }
        
        if ((method.equals(Request.BYE)) ||
            (method.equals(Request.NOTIFY)) ||
            (method.equals(Request.PRACK)) ||
            (method.equals(Request.UPDATE))) {
            throw new SipException("Method " + method + " should be created " +
                    "using SipDialog.getNewClientConnection().",
                    SipException.INVALID_OPERATION);
        }
        
        initRequestImpl(method, scn);
    }

    protected void initRequestImpl(String method, SipConnectionNotifier scn) throws
        SipException {
        if (state != CREATED) {
            throw new SipException("the request can not be initialized," +
                                   " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if ((method.equals(Request.ACK)) ||
            (method.equals(Request.CANCEL))) {
            throw new SipException("the request can not be initialized," +
                                   " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        // Affect the sip connection notifier
        if (scn != null) {
            sipConnectionNotifier = scn;
        } else {
                String transport = parameters.getValueDefault(
                  SIPConstants.GENERAL_TRANSPORT, SIPConstants.TRANSPORT_UDP);

                int localPort;

                // see RFC 3261, 18.1.1 - selecting port number
                if (transport.equalsIgnoreCase(SIPConstants.TRANSPORT_TLS)) {
                    localPort = SIPConstants.DEFAULT_TLS_PORT;
                } else { // other transport protocol
                    localPort = SIPConstants.DEFAULT_NONTLS_PORT;
                }

                // Check if sipConnectionNotifier already exists on the same
                // port. This is always true when UAC and UAS are in the same
                // application and the user has opened a connection (like
                // Connector.open("sip:5060");) before calling this function.
                Vector connectionNotifiersList =
                    stackConnector.getConnectionNotifiersList();

                try {
                    for (int i = 0; i < connectionNotifiersList.size(); i++) {
                        SipConnectionNotifier currNotifier =
                            (SipConnectionNotifier)
                            connectionNotifiersList.elementAt(i);

                        if ((currNotifier.getLocalPort() == localPort) &&
                            (((SipConnectionNotifierImpl) currNotifier).
                             getSipProvider().getListeningPoint().
                             getTransport().equalsIgnoreCase(transport))) {
                            sipConnectionNotifier = currNotifier;
                            break;
                        }
                    }
                } catch (IOException ioe) {
                    throw new SipException(ioe.getMessage(),
                                           SipException.GENERAL_ERROR);
                }

                if (sipConnectionNotifier == null) {
                    // Notifier was not found - create it.
                    try {
                        sipConnectionNotifier =
                            stackConnector.createSipConnectionNotifier(
                                localPort,
                                scheme.equals(SIPConstants.SCHEME_SIP),
                                transport, null);
                    } catch (IOException ioe) {
                        throw new SipException(ioe.getMessage(),
                                               SipException.GENERAL_ERROR);
                    }

                    isNotifierCreated = true;
                } // end if
        } // end else (scn == null)
        
        // redirect the methods ACK and CANCEL towards their helper
        // methods
        if (method.equals(Request.ACK)) {
            initAck();
        }

        if (method.equals(Request.BYE) && (sipDialog != null)) {
            initBye();
            state = INITIALIZED;
            useRequest = true;
            return;
        }

        if (method.equals(Request.NOTIFY)) {
            // if a dialog was not created, send NOTIFY out of dialog.
            if (sipDialog != null) {
                initNotify();
                state = INITIALIZED;
                useRequest = true;
                return;
            }
        }

        // Create request into dialog
        if (sipDialog != null) {
            byte dialogState = sipDialog.getState();
            if ((dialogState == SipDialog.EARLY) ||
                (dialogState == SipDialog.CONFIRMED)) {

                if (method.equals(Request.PRACK) &&
                    (! ((SipDialogImpl) sipDialog).isReliableProvReceived)) {
                    return;
                }

                // if (sipDialog.getState() == SipDialog.CONFIRMED) {
                // When SipDialog instance has CONFIRMED state, any new
                // request should be inside of dialog and have same
                // headers (To, From, Call-ID...) as original request.
                try {
                    request =
                        ((SipDialogImpl) sipDialog).dialog.createRequest(
                        method);
                } catch (SipException ex) {
                    throw ex;
                    // throw new IllegalArgumentException(
                    //     "Could not create the bye request! " + ex);
                }
                state = INITIALIZED;
                useRequest = true;
                return;
            }
        }

        // We lookup in a record store to see whether or not there is
        // the user sip uri
        String sipURI = null;

        try {
            RecordStore rs = RecordStore.openRecordStore("UserSipUri", false);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            if (re.hasNextElement()) {
                int recordID = re.nextRecordId();
                sipURI = new String(rs.getRecord(recordID));
            }
        } catch (RecordStoreException rse) {
            // rse.printStackTrace();
        }

        // if the record store is null the sip uri for the user
        // it is an anonymous sip uri
        if (sipURI != null) {
            userSipURI = sipURI;
        }

        Address userAddress = null;
        try {
            userAddress = StackConnector.addressFactory
                .createAddress(userSipURI);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("The system property UserSipUri"
                                         +
                                         "can not be parsed, check the syntax");
        }

        // Call ID
        CallIdHeader callIdHeader = null;
        String callId = SIPUtils.generateCallIdentifier
            (stackConnector.getSipStack().getIPAddress());
        callIdHeader = new CallIdHeader();
        callIdHeader.setCallId(callId);

        // CSeq
        CSeqHeader cSeqHeader = null;

        try {
            cSeqHeader = StackConnector.headerFactory.createCSeqHeader(1,
                method);
        } catch (ParseException pe) {
            throw new SipException("Problem during the creation" +
                                   " of the CSeqHeader",
                                   SipException.GENERAL_ERROR);
        }

        // From
        FromHeader fromHeader = null;
        try {
            fromHeader = StackConnector
                .headerFactory
                .createFromHeader(
                    userAddress,
                    StackConnector.generateTag());
        } catch (ParseException ex) {
            throw new SipException("Problem during the creation" +
                                   " of the FromHeader",
                                   SipException.GENERAL_ERROR);
        }

        // ToHeader
        Address toAddress = StackConnector
            .addressFactory.createAddress(
                requestURI);
        ToHeader toHeader = null;
        try {
            toHeader = StackConnector
                .headerFactory.createToHeader(
                    toAddress, null);
        } catch (ParseException ex) {
            throw new SipException("Problem during the creation" +
                                   " of the ToHeader",
                                   SipException.GENERAL_ERROR);
        }

        // ViaHeader
        Vector viaHeaders = new Vector();
        String viaLocalAddress;
        String viaTransport;
        int viaLocalPort;

        try {
            viaLocalAddress = sipConnectionNotifier.getLocalAddress();
            viaLocalPort = sipConnectionNotifier.getLocalPort();
            viaTransport =
                ((SipConnectionNotifierImpl) sipConnectionNotifier)
                .getSipProvider().getListeningPoint().getTransport();
        } catch (IOException ioe) {
            throw new SipException("Internal Error, cannot get " +
                                   "the local port or address",
                                   SipException.GENERAL_ERROR);
        }

        try {
            ViaHeader viaHeader = StackConnector
                .headerFactory
                .createViaHeader(
                    viaLocalAddress,
                    viaLocalPort,
                    viaTransport,
                    SIPUtils.generateBranchId());
            viaHeaders.addElement(viaHeader);
        } catch (ParseException ex) {
            throw new SipException("Problem during the creation" +
                                   " of the ViaHeaders",
                                   SipException.GENERAL_ERROR);
        }

        // Max Forward Header
        MaxForwardsHeader maxForwardsHeader =
            StackConnector.headerFactory.createMaxForwardsHeader(70);

        // generate the request
        try {
            request = StackConnector
                .messageFactory.createRequest(
                    requestURI,
                    method,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader);
        } catch (ParseException ex) {
            throw new SipException("Problem during the creation " +
                                   " of the Request " + method,
                                   SipException.GENERAL_ERROR);
        }

        /*
         * Contact header - not in MESSAGE request (RFC 3428, 4).
         * RFC 3903, p. 5:
         * The PUBLISH request MAY contain a Contact header field, but including
         * one in a PUBLISH request has no meaning in the event publication
         * context and will be ignored by the ESC (Event State Compositor).
         */
        if (!method.equals(Request.MESSAGE) &&
            !method.equals(Request.PUBLISH)) {
            ContactHeader contactHeader = null;

            try {
                if (isNotifierCreated) {
                    // Notifier was not passed as an argument to initRequest()
                    SipURI contactURI = StackConnector
                        .addressFactory
                        .createSipURI("anonymous", // name
                                      viaLocalAddress);
                    contactURI.setTransportParam(viaTransport);
                    contactURI.setPort(viaLocalPort);
                    contactHeader =
                        StackConnector
                        .headerFactory
                        .createContactHeader(
                            StackConnector
                            .addressFactory
                            .createAddress(contactURI));
                } else { // notifier is given
                    SipURI contactURI = StackConnector
                        .addressFactory
                        .createSipURI(
                            userSipURI
                            .substring(scheme.length() + 1,
                                       userSipURI.indexOf("@")),
                            sipConnectionNotifier.getLocalAddress());
                    contactURI
                        .setTransportParam(
                            ((SipConnectionNotifierImpl)
                             sipConnectionNotifier).
                            getSipProvider().getListeningPoint()
                            .getTransport());
                    contactHeader =
                        StackConnector
                        .headerFactory
                        .createContactHeader(
                            StackConnector
                            .addressFactory
                            .createAddress(contactURI));
                    contactURI.setPort(sipConnectionNotifier.getLocalPort());
                }
            } catch (IOException ioe) {
                throw new SipException("Internal Error, cannot get " +
                                       "the local port or address",
                                       SipException.GENERAL_ERROR);
            } catch (ParseException ex) {
                throw new SipException("Problem during the creation " +
                                       "of the Contact Header",
                                       SipException.GENERAL_ERROR);
            }

            // set the header
            request.addHeader(contactHeader);
        }

        state = INITIALIZED;
        useRequest = true;
    }

    /**
     * @see SipClientConnection#setRequestURI(java.lang.String)
     */
    /**
     * Sets Request-URI explicitly. Request-URI can be set only in
     * Initialized state.
     * @param newUri Request-URI
     * @throws IllegalArgumentException MAY be thrown if the URI is invalid
     * @throws SipException INVALID_STATE if the Request-URI can not be set,
     * because of wrong state.
     * INVALID_OPERATION if the Request-URI is not allowed to be set.
     */
    public void setRequestURI(String newUri) throws SipException {
        if (state != INITIALIZED) {
            throw new SipException("the request URI can not be set, " +
                                   " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        URI uri = null;
        if ((newUri != null) && (!newUri.equals(""))) {
            try {
                uri = StackConnector.addressFactory.createURI(newUri);
            } catch (ParseException pe) {
                throw new IllegalArgumentException("Invalid URI");
            }
        }

        request.setRequestURI(uri);
        requestURI = uri;
    }

    /**
     * Convenience method to initialize SipClientConnection with SIP request
     * method ACK. ACK can be applied only to INVITE request.
     * @see JSR180 spec, v 1.0.1, p 27
     *
     */
    public void initAck() throws SipException {
        if (state != COMPLETED) {
            throw new SipException("the ACK request can not be initialized,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        // restore first request
        if (requestSavedBeforeACK != null) {
            request = requestSavedBeforeACK;
        }

        if (!request.getMethod().equals(Request.INVITE)) {
            // original request is non-INVITE
            throw new SipException("Original request is non-INVITE",
                                   SipException.INVALID_OPERATION);
        }

        // JSR180: For error responses (3xx-6xx) the ACK is sent
        // automatically by the system in transaction level.
        // If user initializes an ACK which has already been
        // sent an Exception will be thrown.
        int statusCode = 0;

        if (response != null) {
            statusCode = response.getStatusCode() / 100;
        } else if (responseReceived != null) {
            statusCode = responseReceived.getStatusCode() / 100;
        }

        if (responseReceived.getStatusCode() / 100 > 2) {
            throw new SipException("ACK request was already sent",
                                   SipException.INVALID_OPERATION);
        }

        requestSavedBeforeACK = request; // save request
        // This may throw SipException.
        request = clientTransaction.createAck();

        state = INITIALIZED;
        useRequest = true;
    }

    /**
     * Initialize the session termination transaction.
     */
    protected void initBye() {
        // Generate Request
        SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;
        try {
            request = sipDialogImpl.dialog.createRequest(Request.BYE);
            // handle the parameters
            if (parameters != null) {
                Enumeration parNames = parameters.getKeys();
                while (parNames.hasMoreElements()) {
                    String name = (String) parNames.nextElement();
                    String value = (String) parameters.getValue(name);
                    ((SipURI) requestURI).setParameter(name, value);
                }
            }
        } catch (SipException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "Could not create BYE request! " + ex);
            }
        } catch (ParseException pe) {
            // intentionally ignored
            // setParameter() is used with verified parameters
        }
    }

    /**
     * Convenience method to initialize SipClientConnection with SIP request
     * method NOTIFY.
     * This method is copied from latest updates to NIST workspace
     */
    protected void initNotify() {
        // don't call this method out of dialog
        if (sipDialog == null) {
            throw new IllegalArgumentException(
                "Initialization NOTIFY request out of dialog");
        }

        // Generate Request
        SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;
        try {
            request = sipDialogImpl.dialog.createRequest(Request.NOTIFY);

            // handle the parameters
            if (parameters != null) {
                Enumeration parNames = parameters.getKeys();
                while (parNames.hasMoreElements()) {
                    String name = (String) parNames.nextElement();
                    String value = (String) parameters.getValue(name);
                    ((SipURI) requestURI).setParameter(name, value);
                }
            }
        } catch (SipException ex) {
            // IMPL_NOTE : cleanup
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "Could not create the notify request! " + ex);
            }
        } catch (ParseException pe) {
            // intentionally ignored
            // setParameter() is used with verified parameters
        }
    }

    /**
     * Convenience method to initialize SipClientConnection with SIP request
     * method CANCEL.
     * @return A new SipClientConnection with preinitialized CANCEL request.
     * @throws SipException - INVALID_STATE if the request can not be set,
     * because of wrong state (in SipClientConnection) or the system
     * has already
     * got the 200 OK response (even if not read with receive() method).
     * INVALID_OPERATION if CANCEL method can not be applied to the current
     * request method.
     * @see javax.microedition.sip.SipClientConnection#initCancel()
     */
    public SipClientConnection initCancel() throws SipException {
        if ((state != PROCEEDING) || !enableInitCancel) {
            throw new SipException("the CANCEL request can not be initialized,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        // JSR180: The CANCEL request will be built according to
        // the original INVITE request within this connection.
        // Therefore building CANCEL request from not-INVITE
        // original request is not allowed.
        if (!request.getMethod().equals(Request.INVITE)) {
            throw new SipException("The method of original request " +
                                   "is not INVITE",
                                   SipException.INVALID_OPERATION);
        }

        // init the cancel request
        Request cancelRequest = clientTransaction.createCancel();
        SipClientConnection sipClientConnectionCancel =
            new SipClientConnectionImpl(
                cancelRequest,
                sipConnectionNotifier,
                userSipURI);
        // stackConnector.clientConnectionList.addElement(
        //     sipClientConnectionCancel);
        return sipClientConnectionCancel;
    }

    /**
     * Receives SIP response message. 
     * @param timeout - the maximum time to wait in milliseconds.
     * 0 = do not wait, just poll
     * @return Returns true if response was received.
     * @throws SipException - INVALID_STATE if the receive can not be
     * called because of wrong state.
     * @throws IOException - if the message could not be received or
     * because of network failure
     */
    public boolean receive(long timeout) throws SipException, IOException {
        if ((state != PROCEEDING) && (state != COMPLETED)) {
            throw new SipException(SipException.INVALID_STATE);
        }

        // check for a response
        if (responses.isEmpty()) {
            // wait for a response during the time specified by the timeout
            if (timeout != 0) {
                synchronized (this) {
                    try {
                        // listeningThread.sleep(timeout);
                        wait(timeout);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        if (responses.isEmpty()) {
            return false; // queue is empty
        }

        // get first response from queue
        IncomingQueueElement incomingElement =
            (IncomingQueueElement) responses.firstElement();
        responseReceived = incomingElement.getResponse();
        responses.removeElementAt(0); // remove from queue
        useRequest = false;

        // change client transaction if need
        if (incomingElement.containsClientTransaction()) {
            clientTransaction = incomingElement.getClientTransaction();
        }

        final int statusGroup = responseReceived.getStatusCode() / 100;
        if ((statusGroup == 2) &&
            (state == COMPLETED)) { // multiple responses
            // change dialog
            sipDialog = new SipDialogImpl(clientTransaction.getDialog(),
                                          sipConnectionNotifier,
                                          classSecurityToken);
            // transaction is INVITE, checked in
            // ClientTransaction.isMessageTransOrMult()
            ((SipDialogImpl) sipDialog).setWaitForBye(true);
            ((SipDialogImpl) sipDialog).setState(SipDialog.CONFIRMED);
        }
        
        if ((sipDialog != null) &&
            (statusGroup > 2 && statusGroup < 7)) {
            // Error response (3xx-6xx) received (or sent).            
            isErrorResponseReceived = true;
            if (sipDialog.getState() != SipDialog.CONFIRMED) {
                ((SipDialogImpl)sipDialog).setState(SipDialog.TERMINATED);
            }
        }
        
        changeDialogState();
        changeClientConnectionState();

        return true;
    }

    /**
     * Sets the listener for incoming responses. If a listener is
     * already set it
     * will be overwritten. Setting listener to null will remove the current
     * listener.
     * @param sccl - reference to the listener object. Value null will remove
     *  the existing listener.
     * @throws IOException - if the connection is closed.
     */
    public void setListener(SipClientConnectionListener sccl)
        throws IOException {
        if (!connectionOpen) {
            throw new IOException("The Connection has been closed!");
        }
        this.sipClientConnectionListener = sccl;
    }

    /**
     * Enables the refresh on for the request to be sent. The method return a
     * refresh ID, which can be used to update or stop the refresh.
     * @param srl - callback interface for refresh events, if this is null the
     * method returns 0 and refresh is not enabled.
     * @return refresh ID. If the request is not refreshable returns 0.
     * @throws SipException - INVALID_STATE if the refresh can not be enabled
     * in this state.
     */
    public int enableRefresh(SipRefreshListener srl) throws SipException {
        if (state != INITIALIZED) {
            throw new SipException("can not enable the refresh,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if (srl == null) {
            return 0;
        }

        String method = request.getMethod();
        if (!method.equals(Request.REGISTER) &&
            !method.equals(Request.SUBSCRIBE) &&
            !method.equals(Request.PUBLISH)) {
            return 0;
        }

        if (sipRefreshListener != null) {
            // JSR 180: Calling enableRefresh for the second time with a 
            // non-null value does not overwrite the previously set listener.
            // In this case the previously set listener remains valid, and 
            // the method throws SipException.INVALID_STATE
            throw new SipException("SipRefreshListener is already set",
                                   SipException.INVALID_STATE);
        }
        
        // understand the refresh listener thing
        sipRefreshListener = srl;
        int taskID = RefreshManager
            .getInstance()
            .createRefreshTask(
                request,
                sipConnectionNotifier,
                sipRefreshListener,
                this);
        refreshID = String.valueOf(taskID);
        return taskID;
    }

    /**
     * Sets credentials for possible digest authentication.
     * @param username username (for this protection domain)
     * @param password user password (for this protection domain)
     * @param realm defines the protection domain
     * @throws SipException INVALID_STATE if the credentials can not
     * be set in this state.
     * @throws NullPointerException - if the username, password or realm is null
     */
    public void setCredentials(String username, String password, String realm)
        throws SipException {
        if (state != INITIALIZED && state != UNAUTHORIZED) {
            throw new SipException("can not set the credentials, " +
                                   "because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if (username == null || password == null || realm == null) {
            throw new NullPointerException();
        }

        Credentials credential = new Credentials(username, password, realm);
        credentials.addElement(credential);

        // reoriginate the requests with the proper credentials
        if (state == UNAUTHORIZED) {
            reoriginateRequest();
        }
    }

    /**
     * Sets multiple credentials triplets for possible digest authentication.
     * @param usernames - array of user names. The array element username[i] is
     * for the protection domain realm[i]
     * @param passwords - array of user passwords. The array element
     * passwords[i] is for the protection domain realm[i]
     * @param realms - array of protection domains
     * @throws SipException - INVALID_STATE if the credentials can not
     * be set in this state.
     * @throws NullPointerException - if the usernames, passwords or realms
     * array is null or any of their elements is null
     * @throws IllegalArgumentException - if the length of the parameter arrays 
     * are not equal or the length of at least one of them is 0
     */
    public void setCredentials(String[] usernames, String[] passwords,
             String[] realms) throws SipException {
        if (state != INITIALIZED && state != UNAUTHORIZED) {
            throw new SipException("can not set the credentials, " +
                                   "because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if (usernames == null || passwords == null || realms == null) {
            throw new NullPointerException("At least one of the parameter " +
                    "arrays is null");
        }

        if ((usernames.length != passwords.length) ||
            (usernames.length != realms.length)) {
            throw new IllegalArgumentException("Lengths of the parameter " +
                    "arrays are not equal");
        }
        
        if (usernames.length == 0) {
            throw new IllegalArgumentException("Lengths of the parameter " +
                    "arrays are 0");
        }            

        for (int i = 0; i < usernames.length; i++) {
            if ((usernames[i] == null) ||
                (passwords[i] == null) ||
                (realms[i] == null)) {
                throw new NullPointerException("At least one of the elements " +
                        "of the parameter arrays is null");
            }            
        }
        
        for (int i = 0; i < usernames.length; i++) {
            Credentials credential = new Credentials(usernames[i], passwords[i], 
                    realms[i]);
            credentials.addElement(credential);
        }

        // reoriginate the requests with the proper credentials
        if (state == UNAUTHORIZED) {
            reoriginateRequest();
        }
    }
    
    /**
     * Sends the SIP message. Send must also close the OutputStream
     * if it was opened.
     * @throws IOException if the message could not be sent or because
     * of network failure
     * @throws SipException INVALID_STATE if the message cannot be sent
     * in this state. <br> INVALID_MESSAGE there was an error
     * in message format
     */
    public void send()
        throws IOException, SipException {
        try {
            sendRequestImpl(false);
        } catch (SipException se) { 
            throw se;  
        } catch (IOException ioe) {
            try {
                close();
            } catch(IOException e) {} // ignore
            
            throw ioe;
        }
    }

    /**
     * This function is an implementation for send(). It sends the SIP message.
     * Send must also close the OutputStream if it was opened.
     * @param isRefreshRequest a flag indicating if the request to be sent
     * is a refreshing request (isRefreshRequest is true) or it is a regular
     * request (isRefreshRequest is false).
     * @throws IOException if the message could not be sent or because
     * of network failure
     * @throws SipException INVALID_STATE if the message cannot be sent
     * in this state. <br> INVALID_MESSAGE there was an error
     * in message format
     */
    private void sendRequestImpl(boolean isRefreshRequest) throws IOException,
        SipException {

        if (state != STREAM_OPEN && state != INITIALIZED) {
            throw new SipException("can not send the request, " +
                                   "because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if (!connectionOpen) {
            throw new IOException("The Connection has been closed!");
        }

        if (contentOutputStream != null) {
            contentOutputStream.setOpen(false);
            
            byte[] content = contentOutputStream.
                    getByteArrayOutputStream().toByteArray();
            request.setContent(content, 
                    (ContentTypeHeader) request.getHeader(Header.CONTENT_TYPE));
            
            request.removeHeader(Header.CONTENT_LENGTH);
            request.setContentLength(new ContentLengthHeader(content.length));
            
            contentOutputStream = null;
        }

        // Check mandatory headers (RFC3261, 8.1.1)
        String[] mandatoryHeaders = {
            Header.TO, Header.FROM, Header.CSEQ,
            Header.CALL_ID, Header.MAX_FORWARDS, Header.VIA};
        Vector mandatoryList = new Vector();

        // add header names for all types of requests
        for (int i = 0; i < mandatoryHeaders.length; i++) {
            mandatoryList.addElement(mandatoryHeaders[i]);
        }

        String method = request.getMethod();

        // RFC 3515, p. 6:
        // A REFER request MUST contain exactly one Refer-To header field value.
        if (method.equals(Request.REFER)) {
            mandatoryList.addElement(Header.REFER_TO);
        }

        // RFC3265, p. 15:
        // NOTIFY requests MUST contain a "Subscription-State" header with
        // a value of "active", "pending", or "terminated".
        if (method.equals(Request.NOTIFY)) {
            mandatoryList.addElement(Header.SUBSCRIPTION_STATE);
        }

        for (int i = 0; i < mandatoryList.size(); i++) {
            if (request.getHeader(
                (String) mandatoryList.elementAt(i)) == null) {
                throw new SipException("Header " +
                                     (String) mandatoryList.elementAt(i) +
                                     " is missed", SipException.INVALID_STATE);
            }
        }

        // add "tag" parameter to "From" header if necessary
        FromHeader fromHeader = (FromHeader) request.getHeader(Header.FROM);

        // it is not null - please see above
        if (!fromHeader.hasTag()) { // no "tag" parameter
            fromHeader.setTag(StackConnector.generateTag());
        }

        // Request-URI
        // Fix added per NIST cvs digest dated July 3, 2005
        // RFC 3261, 10.2:
        // Request-URI: ... The "userinfo" and "@" components of the
        // SIP URI MUST NOT be present.
        if (method.equals(Request.REGISTER)) {
            Address reqUriAddress = null;
            try {
                reqUriAddress =
                    StackConnector.addressFactory.createAddress(
                        requestURI.toString());
                if (reqUriAddress.isSIPAddress()) {
                    ((SipURI) reqUriAddress.getURI()).removeUser();
                    requestURI = reqUriAddress.getURI();
                }
                request.setRequestURI(requestURI);
            } catch (ParseException pe) {
                throw new SipException(
                    "The system property UserSipUri can not be " +
                    "parsed, check the syntax",
                    SipException.INVALID_OPERATION);
            } catch (NullPointerException npe) {
                throw new SipException(
                    "Requested URI is null",
                    SipException.INVALID_OPERATION);
            }
        }

        // Check that the parameters specified in Via header match
        // those which were set in sipConnectionNotifier.
        ViaHeader requestViaHeader =
            (ViaHeader) request.getViaHeaders().getFirst();
        int viaPort = requestViaHeader.getPort();
        int localPort = sipConnectionNotifier.getLocalPort();
        String transport = requestViaHeader.getTransport();

        if (localPort != viaPort) {
            throw new IOException("Via port (" + viaPort + ") doesn't " +
                                  "match the listener's port (" + localPort +
                                  ")!");
        }

        SipConnectionNotifierImpl notifierImpl =
            (SipConnectionNotifierImpl) sipConnectionNotifier;

        if (!notifierImpl.getSipProvider().getListeningPoint().
            getTransport().equalsIgnoreCase(transport)) {
            throw new IOException("Via transport doesn't match " +
                                  "the listener's transport!");
        }

        // RFC 3903 (PUBLISH method), p. 5:
        // The Record-Route header field has no meaning in PUBLISH
        // requests or responses, and MUST be ignored if present.
        if (method.equals(Request.PUBLISH)) {
            request.removeHeader(Header.RECORD_ROUTE);
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "Request to be sent : " + request);
        }

        // System.out.println(">>> Request to be sent: \n" + request);

        // Set SipErrorListener in case send retrasmission fails 
        request.setErrorListener(sipErrorListener);
        
        // Send the request
        if (method.equals(Request.ACK)) {
            if ((state != INITIALIZED) || (clientTransaction == null)) {
                throw new SipException("Can not send the ACK, " +
                                       "because of wrong state.",
                                        SipException.INVALID_STATE);
            }
            
            Dialog dlg = clientTransaction.getDialog();
            if (dlg.isServer()) {
              // Switch from server side to client side for re-invite
              dlg.addTransaction(clientTransaction);
            }
            try {
                dlg.sendAck(request, false);
            } catch (IllegalArgumentException iae) {
                try {
                    close();
                } catch (IOException ioe) { // ignore
                }
                throw new SipException("SCC.send(): can't send ACK: " + iae,
                                       SipException.GENERAL_ERROR);
            }
            state = COMPLETED;
            return;
        } else {
            // Creates the Nist-Siplite client Transaction for this
            // request
            try {
                if (request.getRequestURI() == null) {
                    throw new SipException("Requested URI is null", 
                            SipException.INVALID_OPERATION);
                }
                
                clientTransaction =
                    ((SipConnectionNotifierImpl) sipConnectionNotifier).
                    getSipProvider().getNewClientTransaction(request);
            } catch (TransactionUnavailableException tue) {
                throw new SipException("Cannot create a new Client " +
                                       " Transaction for this request",
                                       SipException.TRANSACTION_UNAVAILABLE);
            } catch (IllegalArgumentException iae) {
                throw new SipException("SCC.send(): IAE occured (1): " +
                                   iae.getMessage(), SipException.GENERAL_ERROR);
            } catch (NullPointerException npe) {
                throw new SipException("SCC.send(): NPE occured (1): " +
                                   npe.getMessage(), SipException.GENERAL_ERROR);
            }

            // Set the application data so that when the response comes in,
            // it will retrieve this SipClientConnection
            clientTransaction.setApplicationData(this);
            
            if (sipDialog != null && !isRefreshRequest) {
                // if (method.equals(Request.BYE) ||
                //   method.equals(Request.NOTIFY)) {
                // If the request is a BYE, we must send it with the dialog

                // If the dialog is established, all further requests should
                // be sent within it.
                SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;

                if (method.equals(Request.SUBSCRIBE) ||
                    method.equals(Request.REFER)) {
                    // Add a subscription
                    sipDialogImpl.addSubscription(
                        new Subscription(sipDialogImpl.getDialog(), request));
                } else if (method.equals(Request.INVITE)) {
                    sipDialogImpl.setWaitForBye(true);
                }

                sipDialogImpl.dialog.sendRequest(clientTransaction);
                state = PROCEEDING;
                return;
            } else {
                clientTransaction.sendRequest();            
            }
        }

        // An INVITE, SUBSCRIBE or REFER has been sent, so a dialog need to
        // be created.
        if (stackConnector.getSipStack().isDialogCreated(method) &&
            !isRefreshRequest) {
            sipDialog = new SipDialogImpl(clientTransaction.getDialog(),
                                          sipConnectionNotifier,
                                          classSecurityToken);

            SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;
            sipDialogImpl.setRefreshID(refreshID);
            sipDialogImpl.setSipClientConnectionListener(
                sipClientConnectionListener);
            stackConnector.sipDialogList.addElement(sipDialog);

            // Add a subscription
            if (!method.equals(Request.INVITE)) {
                sipDialogImpl.addSubscription(
                    new Subscription(sipDialogImpl.getDialog(), request));
            } else {
                sipDialogImpl.setWaitForBye(true);
            }
        }

        // If the method is a REGISTER it means that we are using a
        // proxy so we put put the route of the proxy in the router
        if (request.getMethod().equals(Request.REGISTER)) {
            SipURI sipURI = (SipURI) request.getRequestURI();
            
            int requestPort = sipURI.getPort();
            if (requestPort == -1) { // get port from sipConnectionNotifier
                requestPort = localPort;
            }

            String requestTransport = sipURI.getTransportParam();

            if ((requestTransport == null) ||
                (requestTransport.length() < 1)) {
                // get transport from sipConnectionNotifier
                requestTransport =
                    ((SipConnectionNotifierImpl) sipConnectionNotifier).
                    getSipProvider().getListeningPoint().getTransport();
            }

            stackConnector.sipStack.getRouter().setOutboundProxy(
                sipURI.getHost()
                + ":" + requestPort
                + "/"
                + requestTransport);
            // outboundProxy = true;
        }

        // Refresh must be scheduled after receiving a response,
        // refer the comments at the end of notifyResponseReceived().
        // scheduleRefresh(request.getMethod(), request, false);

        state = PROCEEDING;
    }

    /**
     * Sets header value in SIP message. If the header does not exist
     * it will be added to the message, otherwise the existing header is
     * overwritten. If multiple header field values exist the topmost is
     * overwritten. The implementations MAY restrict the access to some headers
     * according to RFC 3261.
     * @param name - name of the header, either in full or compact form.
     * RFC 3261 p.32
     * @param value - the header value
     * @throws java.lang.NullPointerException - if name is null
     * @throws SipException - INVALID_STATE if header can not be set in
     * this state. <br> INVALID_OPERATION if the system does not allow to set
     * this header.
     * @throws IllegalArgumentException - if the header or value is invalid.
     */
    public void setHeader(String name, String value) throws SipException {
        if (state != INITIALIZED) {
            throw new SipException("the Header can not be set,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        if (name == null) {
            throw new NullPointerException(HeaderFactory.npeMessage);
        }
        
        if (value == null)
            value = "";
        
        if (request == null) {
            throw new SipException("Failure in setHeader(),"
                    + " associated request is null",
                    SipException.INVALID_STATE);
        }
        
        int delimIndex;
        /* Header of those types uses "," as parameter separator or can contain 
           "," as part of the value */
        if (Header.isAuthorization(name) || 
            name.equalsIgnoreCase(Header.DATE) ||
            name.equalsIgnoreCase(Header.ORGANIZATION) ||
            name.equalsIgnoreCase(Header.SUBJECT) ||
            name.equalsIgnoreCase(Header.RETRY_AFTER) ||
            name.equalsIgnoreCase(Header.SERVER)) {
        
            delimIndex = 0;
        } else {
            delimIndex = value.lastIndexOf(',');
        }

        boolean replaceFlag = true;
        boolean stop = false;
        int headerNum = 0;        
        Header prevHeader = request.getHeader(name);
        try {
            do {
                String headerValue;
                if (delimIndex > 0) {
                    headerValue = value.substring(delimIndex + 1,
                            value.length()).trim();
                    value = value.substring(0, delimIndex).trim();
                    delimIndex = value.lastIndexOf(',');
                } else {
                    headerValue = value;
                    stop = true;
                }

                Header header = null;
                header = StackConnector.headerFactory.createHeader(name,
                        headerValue);

                request.attachHeader(header, replaceFlag, true);
                headerNum++;
                if (replaceFlag == true) {
                    replaceFlag = false;
                }
            } while (!stop);
        } catch (Exception e) {
            if (headerNum > 0) {
                for (int i = 0; i < headerNum; i++) {
                    request.removeHeader(name, true);
                }
                request.addHeader(prevHeader);
            }
            
            if (e instanceof ParseException) {
                throw new IllegalArgumentException(e.getMessage());
            } else if (e instanceof SipException) {
                throw new SipException(e.getMessage(), 
                        ((SipException)e).getErrorCode());
            } else if (e instanceof NullPointerException) {
                throw new IllegalArgumentException("Invalid header value");
            } 
        }
    }

    /**
     * Adds a header to the SIP message. If multiple header field values exist
     * the header value is added topmost of this type of headers.
     * The implementations MAY restrict the access to some headers
     * according to RFC 3261.
     * @param name - name of the header, either in full or compact form.
     * RFC 3261 p.32
     * @param value - the header value
     * @throws java.lang.NullPointerException - if name is null
     * @throws SipException - INVALID_STATE if header can not be set in
     * this state. <br> INVALID_OPERATION if the system does not allow to set
     * this header.
     * @throws IllegalArgumentException - if the header or value is invalid.
     */
    public void addHeader(String name, String value) throws SipException {
        if (state != INITIALIZED)
            throw new SipException("the Header can not be set,"
                    + " because of wrong state.",
                    SipException.INVALID_STATE);

        if (name == null) {
            throw new NullPointerException(HeaderFactory.npeMessage);
        }
        
        if (value == null)
            value = "";

        if (request == null) {
            throw new SipException("Failure in addHeader(),"
                    + " associated response is null",
                    SipException.INVALID_STATE);
        }
        
        int delimIndex;
        /* Header of those types uses "," as parameter separator or can contain 
           "," as part of the value */
        if (Header.isAuthorization(name) || 
            name.equalsIgnoreCase(Header.DATE) ||
            name.equalsIgnoreCase(Header.ORGANIZATION) ||
            name.equalsIgnoreCase(Header.SUBJECT) ||
            name.equalsIgnoreCase(Header.RETRY_AFTER) ||
            name.equalsIgnoreCase(Header.SERVER)) {
        
            delimIndex = 0;
        } else {
            delimIndex = value.lastIndexOf(',');
        }
        
        boolean stop = false;
        int headerNum = 0;
        try {
            do {
                String headerValue;
                if (delimIndex > 0) {
                    headerValue = value.substring(delimIndex + 1,
                            value.length()).trim();
                    value = value.substring(0, delimIndex).trim();
                    delimIndex = value.lastIndexOf(',');
                } else {
                    headerValue = value;
                    stop = true;
                }

                Header header = null;
                header = StackConnector.headerFactory.createHeader(name,
                        headerValue);
                
                request.addHeader(header);
                headerNum++;
            } while (!stop);
        } catch (Exception e) {
            for (int i = 0; i < headerNum; i++) {
                request.removeHeader(name, true);
            }
            
            if (e instanceof ParseException) {
                throw new IllegalArgumentException(e.getMessage());
            } else if (e instanceof SipException) {
                throw new SipException(e.getMessage(), 
                        ((SipException)e).getErrorCode());
            } else if (e instanceof NullPointerException) {
                throw new IllegalArgumentException("Invalid header value");
            }
        }
    }

    /**
     * Reomves header from the SIP message. If multiple header field
     * values exist
     * the topmost is removed.
     * The implementations MAY restrict the access to some headers
     * according to RFC 3261.
     * If the named header is not found this method does nothing.
     * @param name - name of the header to be removed, either int
     * full or compact form RFC 3261 p.32.
     * @throws java.lang.NullPointerException - if name is null
     * @throws SipException - INVALID_STATE if header can not be removed in
     * this state. <br> INVALID_OPERATION if the system does not allow to remove
     * this header.
     */
    public void removeHeader(String name) throws SipException {
        if (state != INITIALIZED) {
            throw new SipException("the Header can not be removed,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }
        if (name == null) {
            throw new NullPointerException(HeaderFactory.npeMessage);
        }
        request.removeHeader(name, true);
    }

    /**
     * Gets the header field value(s) of specified header type
     * @param name - name of the header, either in full or compact form.
     * RFC 3261 p.32
     * @throws java.lang.NullPointerException - if name is null
     * @return array of header field values (topmost first), or null if the
     * current message does not have such a header or the header is for other
     * reason not available (e.g. message not initialized).
     *
     * Javadoc is not clear on whether this method should be applied to
     * request or response. The NIST implementation uses response only to
     * calculate the size; but that seems to be wrong.
     *
     */
    public String[] getHeaders(String name) {
        if (name == null) {
            throw new NullPointerException(HeaderFactory.npeMessage);
        }
        
        Message currentMessage = useRequest ? (Message) request :
            (Message) responseReceived;

        if (null == currentMessage || TERMINATED == state) {
            // There 'request' may absent in the CREATED state
            return null;
        }

        HeaderList nameList = currentMessage.getHeaderList(name);

        if (nameList == null) {
            return null;
        }

        int size = nameList.size();

        if (size < 1) {
            return null;
        }

        String[] headerValues = new String[size];

        for (int count = 0; count < size; count++) {
            headerValues[count] =
                ((Header) nameList.elementAt(count)).getHeaderValue();
        }

        return headerValues;
    }

    /**
     * Gets the header field value of specified header type.
     * @param name - name of the header type, either in full or compact form.
     * RFC 3261 p.32
     * @throws java.lang.NullPointerException - if name is null
     * @return topmost header field value, or null if the
     * current message does not have such a header or the header is for other
     * reason not available (e.g. message not initialized).
     *
     * Javadoc is not clear on whether this method should be applied to
     * request or response. The NIST implementation uses response; but that
     * seems to be wrong.
     *
     */
    public String getHeader(String name) {
        if (name == null) {
            throw new NullPointerException(HeaderFactory.npeMessage);
        }
        
        Message currentMessage = useRequest ? (Message) request :
            (Message) responseReceived;

        if (null == currentMessage || TERMINATED == state) {
            // There 'request' may absent in the CREATED state
            return null;
        }

        Header nameHeader = currentMessage.getHeader(name);

        if (nameHeader == null) {
            return null;
        }

        return nameHeader.getHeaderValue();
    }

    /**
     * Gets the SIP method. Applicable when a message has been
     * initialized or received.
     * @return SIP method name REGISTER, INVITE, NOTIFY, etc. Returns null if
     * the method is not available.
     */
    public String getMethod() {
        if (null == request || TERMINATED == state) {
            return null;
        } else {
            return request.getMethod();
        }
    }

    /**
     * Gets Request-URI. Available when SipClientConnection is in Initialized
     * state or when SipServerConnection is in Request Received state.
     * Built from the original URI given in Connector.open().
     * See RFC 3261 p.35 (8.1.1.1 Request-URI)
     * @return Request-URI of the message. Returns null if the Request-URI
     * is not available.
     */
    public String getRequestURI() {
        if (state != INITIALIZED || request == null || 
            request.getRequestURI() == null) {
            return null;
        } else {
            return request.getRequestURI().toString();
        }
    }

    /**
     * Gets SIP response status code. Available when SipClientConnection is in
     * Proceeding, Unauthorized or Completed state or when SipServerConnection is in
     * Initialized state.
     * @return status code 1xx, 2xx, 3xx, 4xx, ... Returns 0 if the status code
     * is not available.
     */
    public int getStatusCode() {
        if (state != PROCEEDING && state != COMPLETED && state != UNAUTHORIZED
            || responseReceived == null) {
            return 0;
        } else {
            return responseReceived.getStatusCode();
        }
    }

    /**
     * Gets SIP response reason phrase. Available when SipClientConnection is in
     * Proceeding, Unauthorized or Completed state or when SipServerConnection is in
     * Initialized state.
     * @return reason phrase. Returns null if the reason phrase is
     * not available.
     */
    public String getReasonPhrase() {
        if (state != PROCEEDING && state != COMPLETED && state != UNAUTHORIZED
            || responseReceived == null) {
            return null;
        } else {
            return responseReceived.getReasonPhrase();
        }
    }

    /**
     * Returns the current SIP dialog. This is available when the SipConnection
     * belongs to a created SipDialog and the system has received (or sent)
     * provisional (101-199) or final response (200).
     * @return SipDialog object if this connection belongs to a dialog,
     * otherwise returns null.
     */
    public SipDialog getDialog() {
        /* Spec 1.1.0, p. 27:
         * The method returns null if a terminating error response (3xx - 6xx)
         * is received or sent on the connection or the connection is closed.
         */        
        if ((state == TERMINATED) ||
            (isErrorResponseReceived)) {
            return null;        
        }
        
        if (sipDialog != null) {
            byte dialogState = sipDialog.getState();
            if ((dialogState != SipDialog.EARLY) &&
                (dialogState != SipDialog.CONFIRMED)) {
                return null;
            }
        }

        return sipDialog;
    }

    /**
     * Returns InputStream to read SIP message body content.
     * @return InputStream to read body content
     * @throws java.io.IOException - if the InputStream can not be opened,
     * because of an I/O error occurred.
     * @throws SipException - INVALID_STATE the InputStream can not be opened
     * in this state (e.g. no message received).
     */
    public InputStream openContentInputStream() throws IOException,
        SipException {
        String errStateMsg = "the content input stream can not be open, " +
            "because of wrong state.";

        if ((state != COMPLETED) && (state != PROCEEDING)) {
            throw new SipException(errStateMsg, SipException.INVALID_STATE);
        }

        if (!connectionOpen) {
            throw new IOException("The Connection has been closed!");
        }

        if (responseReceived == null) {
            // Although openContentInputStream() is called in the correct
            // user-level state, we may have a situation when this method
            // is called before the response was received. In this case
            // the internal state of SCC is invalid and the proper exception
            // to throw is SipException.INVALID_STATE.
            throw new SipException(errStateMsg, SipException.INVALID_STATE);
        }

        ContentLengthHeader contentLengthHeader =
            responseReceived.getContentLengthHeader();
        if (contentLengthHeader == null) {
            throw new
                IOException("Response contains no content length header.");
        }

        int bodyLength = contentLengthHeader.getContentLength();

        if (bodyLength == 0) {
            throw new IOException("Response's body has zero length.");
        }

        byte[] buf = responseReceived.getRawContent();
        if (buf == null) { // body is empty
            throw new IOException("Body of SIP response is empty.");
        }
        contentInputStream = new ByteArrayInputStream(buf);
        return contentInputStream;
    }

    /**
     * Returns OutputStream to fill the SIP message body content.
     * @return OutputStream to write body content
     * @throws IOException if the OutputStream can not be opened,
     * because of an I/O error occurred.
     * @throws SipException INVALID_STATE the OutputStream can not be opened
     * in this state (e.g. no message initialized).
     * UNKNOWN_TYPE Content-Type header not set.
     */
    public OutputStream openContentOutputStream() throws IOException,
        SipException {
        if (state != INITIALIZED) {
            throw new SipException("the content output stream can not be open,"
                                   + " because of wrong state.",
                                   SipException.INVALID_STATE);
        }
        if (request.getHeader(Header.CONTENT_TYPE) == null) {
            throw new SipException(
                "Content-Type unknown, set the content-type "
                + "header first",
                SipException.UNKNOWN_TYPE);
        }
        if (!connectionOpen) {
            throw new IOException("The Connection has been closed!");
        }
        contentOutputStream = new SDPOutputStream(this);
        state = STREAM_OPEN;
        return contentOutputStream;
    }

    /**
     * Close the clientconnection.
     * @see javax.microedition.io.Connection#close()
     */
    public void close() throws IOException {
        responses.removeAllElements(); // clear queue
        // cleanup
        if (isNotifierCreated && (sipConnectionNotifier != null)) {
            try {
               sipConnectionNotifier.close();
            } catch (IOException exc) { // ignore
            }
        }

        // Removing the connection from the connection list held by
        // the stackConnector

        // StackConnector.getInstance().
        // clientConnectionList.removeElement(this);
        connectionOpen = false;
        listeningThread = null;
        state = TERMINATED;
    }

    /**
     * Reoriginate the request with the proper credentials
     */
    private void reoriginateRequest() {

        // clear dialog
        if (sipDialog != null) {
            if (sipDialog.getState() == SipDialog.TERMINATED) {
                sipDialog = null;
            }
        }

        DigestClientAuthentication authentication =
            new DigestClientAuthentication(credentials);

        // Reoriginate the request with the proper credentials
        Request newRequest = authentication.createNewRequest(
            stackConnector.sipStack,
            this.request,
            this.responseReceived,
            this.countReoriginateRequest);

        if (newRequest != null) {
            this.countReoriginateRequest++;
            this.request = newRequest;

            // The request has been reinitialized...
            state = INITIALIZED;
            useRequest = true;

            // ...so it is sent out
            try {
                this.send();
            } catch (IOException ioe) {
                // ioe.printStackTrace();
            }
        }
    }

    /**
     * Change the state of this Client Connection due to an incoming response.
     */
    private void changeClientConnectionState() {
        // Change the Client Connection state
        // If it's a trying, the state is PROCEEDING
        if (responseReceived.getStatusCode() / 100 == 1
            && state == PROCEEDING) {
            state = PROCEEDING;
        }
        // If it's a 401 or 407, the state is UNAUTHORIZED
        else if (state == PROCEEDING &&
                 (responseReceived.getStatusCode() == SIPErrorCodes.UNAUTHORIZED ||
                  responseReceived.getStatusCode() ==
                  SIPErrorCodes.PROXY_AUTHENTICATION_REQUIRED)) {
            state = UNAUTHORIZED;
        }
        // Otherwise this is COMPLETED
        else {
            state = COMPLETED;
        }
    }

    /**
     * Change the state of the dialog due to an incoming response.
     */
    private void changeDialogState() {
        // Change the dialog state

        // REGISTER method doesn't establish a dialog, so sipDialog
        // should be null in this case.
        // IMPL_NOTE: check if it is really null as supposed to be.
        if (sipDialog == null) {
            return;
        }

        SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;
        String method = responseReceived.getCSeqHeader().getMethod();
        if (!stackConnector.getSipStack().allowDialogStateChange(method)) {
            return;
        }
        int statusCode = responseReceived.getStatusCode();

        if (statusCode / 100 == 2) {
            // RFC 3261, section 13.2.2.4:
            // If the dialog identifier in the 2xx response matches the dialog
            // identifier of an existing dialog, the dialog MUST be transitioned
            // to the "confirmed" state.

            // sipDialog cannot be null here - this check was done
            // at the beginning of the function
            sipDialogImpl.setState(SipDialog.CONFIRMED);
            sipDialogImpl.setDialogID(responseReceived.getDialogId(false));

            if (statusCode == 200) {
                if (method.equals(Request.NOTIFY)) {
                    sipDialogImpl.handleNotify(request, null,
                                           responseReceived.getDialogId(false));
                    return;
                }

                // handle the un-Subscribe state
                if (method.equals(Request.SUBSCRIBE)) {
                    ExpiresHeader expiresHeader = (ExpiresHeader)
                        responseReceived.getHeader(ExpiresHeader.NAME);

                    // According to RFC3265, p. 6:
                    // "200-class responses to SUBSCRIBE requests also
                    // MUST contain an "Expires" header."
                    //
                    // But we have to handle a situation when it is missing.
                    // RFC 3265, p. 8:
                    // "200-class responses indicate that the subscription
                    // has been accepted".
                    //
                    // So, the dialog state is set to CONFIRMED even if
                    // no Expires header is present.
                    if (expiresHeader != null &&
                        expiresHeader.getExpires() == 0) { // unsubscribe
                        sipDialogImpl.terminateIfNoSubscriptions();
                    } else { // subscribe confirmation
                        sipDialogImpl.setState(SipDialog.CONFIRMED);
                        sipDialogImpl.setDialogID(
                            responseReceived.getDialogId(false));
                    }
                } else if (method.equals(Request.BYE)) {
                    // IMPL_NOTE: check the RFC 3261. Probably we have to terminate
                    // the dialog for the responses other than 200 OK.
                    sipDialogImpl.setWaitForBye(false);
                    sipDialogImpl.terminateIfNoSubscriptions();
                }
            }
        } else if (statusCode / 100 == 1) {
            // provisional response
            if (statusCode != 100) {
                if (sipDialog.getState() == SipDialogImpl.INITIALIZED) {
                    // switch to EARLY state
                    sipDialogImpl.setState(SipDialog.EARLY);
                }
                /*
                 * Add a check to verify if it is reliable provisional
                 * response.
                 */
                Header requireHeader =
                    (ParameterLessHeader) responseReceived.getHeader(
                        Header.REQUIRE);
                if (requireHeader != null) {
                    sipDialogImpl.isReliableProvReceived =
                        Header.isReliableTagPresent(
                            requireHeader.getHeaderValue());
                }
                // RFC 3261, 12.1:
                // Within this specification, only 2xx and 101-199
                // responses with a To tag ... will establish a dialog.
                // set the dialog ID
                sipDialogImpl.setDialogID(responseReceived.getDialogId(false));
            }

        } else { // another response code - switch to TERMINATED state
            // Remove the subscription if any
            sipDialogImpl.removeSubscription(response);

            // JSR180 - not from CONFIRMED state
            if (sipDialog.getState() != SipDialog.CONFIRMED) {
                if (method.equals(Request.INVITE)) {
                    sipDialogImpl.setWaitForBye(false);
                }
                sipDialogImpl.terminateIfNoSubscriptions();
            }
        }
    }

    /**
     * Updates and sends the request from the refresh.
     * @param updatedRequest the updated request
     * @throws IOException if the message could not be sent or because
     * of network failure
     * @throws InterruptedIOException if a timeout occurs while
     * either trying
     * to send the message or if this Connection object is closed
     * during this send operation
     * @throws SipException INVALID_STATE if the message cannot be sent
     * in this state. <br> INVALID_MESSAGE there was an error
     * in message format
     */
    protected void updateAndSendRequestFromRefresh(Request updatedRequest)
        throws IOException, InterruptedIOException, SipException {
        
        request = updatedRequest;
        state = INITIALIZED;

        // If the request to be refreshed creates a dialog (i.e. SUBSCRIBE),
        // the next request will be sent within a dialog using the rules for
        // sending in-dialog requests. To avoid it, isRefreshRequest parameter
        // is used.
        try {
            sendRequestImpl(true);
        } catch (SipException se) { 
            throw se;  
        } catch (IOException ioe) {
            try {
                close();
            } catch(IOException e) {} // ignore
            
            throw ioe;
        }
    }

    /**
     * Updates the request and calls openContentOutputStream()
     * to fill the new message body.
     * @param updatedRequest the updated request
     * @return OutputStream to write body content
     * @throws IOException if the OutputStream can not be opened,
     * because of an I/O error occurred.
     * @throws SipException INVALID_STATE the OutputStream can not be opened
     * in this state (e.g. no message initialized).
     * UNKNOWN_LENGTH Content-Length header not set.
     * UNKNOWN_TYPE Content-Type header not set.
     */
    protected OutputStream updateRequestAndOpenOutputStream(
        Request updatedRequest) throws IOException, SipException {
        request = updatedRequest;
        state = INITIALIZED;
        return openContentOutputStream();
    }

    /**
     * Gets the current request.
     * @return the current request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Return the Call Identifier of this client connection
     * If there is no call id yet, this method return an empty String
     * @return the call Identifier
     */
    protected String getCallIdentifier() {
        if (request == null) {
            return "";
        }

        return request.getCallIdentifier();
    }

    /**
     * The stack connector notifies this class when it receive NOTIFY
     * request matching the previous SUBSCRIBE or REFER request.
     * @param notifyRequest NOTIFY request to process
     */
    public void handleMatchingNotify(Request notifyRequest) {
        SipDialogImpl sipDialogImpl = (SipDialogImpl) sipDialog;
        int state = sipDialogImpl.getState();
        SubscriptionStateHeader ssh = (SubscriptionStateHeader)
            notifyRequest.getHeader(Header.SUBSCRIPTION_STATE);
        boolean isUnsubscribe = (ssh != null && ssh.isTerminated());

        if (state == SipDialogImpl.INITIALIZED ||
            (state == SipDialogImpl.CONFIRMED && isUnsubscribe)) {
            String dialogId = notifyRequest.getDialogId(false);
            // sipDialogImpl.setState(isUnsubscribe ?
            //       SipDialog.TERMINATED : SipDialog.CONFIRMED);
            sipDialogImpl.setDialogID(dialogId);
            sipDialogImpl.handleNotify(notifyRequest, null, dialogId);
        }
    }

    /**
     * The stack connector notifies this class when it receive a new response.
     * @param response the repsonse event to be propagated
     * @param inputClientTransaction client transaction of this response
     */
    protected void notifyResponseReceived(Response response,
                                          ClientTransaction
                                          inputClientTransaction) {
        // System.out.println(">>> Response received : \n" + response);
        int statusCode = response.getStatusCode();
        int statusGroup = statusCode / 100;
        String responseBranch = ((ViaHeader) response.getTopmostVia()).getBranch();
        String requestBranch = ((ViaHeader) request.getTopmostVia()).getBranch();
        
        if ((statusGroup == 2) && (responseBranch.equals(requestBranch))) {
            setUpdateState(true);
        }

        boolean ignoreResponse = false;

        if (state == COMPLETED) { // 2xx responses only
            if (statusGroup != 2) {
                ignoreResponse = true;
            }
        } else if (state == PROCEEDING) {
            // If there is some credentials and the client connection is in an
            // UNAUTHORIZED state, the request is reoriginate automatically
            if (credentials.size() > 0 &&
                ((statusCode == SIPErrorCodes.UNAUTHORIZED) ||
                 (statusCode == SIPErrorCodes.PROXY_AUTHENTICATION_REQUIRED))) {
                this.responseReceived = response;
                if (sipDialog != null) {
                    ((SipDialogImpl) sipDialog).setState(SipDialog.TERMINATED);
                }
                reoriginateRequest();
                ignoreResponse = true;
            }
        } else { // not COMPLETED and PROCEEDING
            ignoreResponse = true;
        }

        // check the queue size
        if (responses.size() > MAX_NUM_RESPONSES) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "Queue of incoming SIP packets is overflow");
            }
            ignoreResponse = true;
        }

        if (ignoreResponse) { // ignore response
            return;
        }

        IncomingQueueElement incomingElement =
            new IncomingQueueElement(response, inputClientTransaction);
        responses.addElement(incomingElement); // put to queue

        this.response = response;

        if (state == PROCEEDING) {
            if (statusGroup == 1) {
                // provisional response
                // JSR180: SipClientConnection: initCancel: The method is
                // available when a provisional response
                // has been received.
                enableInitCancel = true;
            } else {
                // All responses from 200-699 are final
                // JSR180: SipClientConnection: initCancel:
                // Throws: SipException - INVALID_STATE if ... or the system
                // has already got the 200 OK response (even if not read with
                // receive() method).
                enableInitCancel = false;
            }
        }

        if (response.getCSeqHeaderNumber() == request.getCSeqHeaderNumber()) {
            synchronized (this) {
                notify();
            }
            // We notify the listener that a response has been received
            if (sipClientConnectionListener != null) {
                sipClientConnectionListener.notifyResponse(this);
            }
        }

        String method = response.getCSeqHeader().getMethod();

        if (method.equals(Request.PUBLISH)) {
            // RFC 3903, p. 6:
            // When updating previously published event state, PUBLISH
            // requests MUST contain a single SIP-If-Match header field
            // identifying the specific event state that the request is
            // refreshing, modifying or removing. This header field MUST
            // contain a single entity-tag that was returned by the ESC
            // in the SIP-ETag header field of the response to a previous
            // publication.
            Header hEtag = response.getHeader(Header.SIP_ETAG);

            if (hEtag != null) {
                Header hIfMatch = request.getHeader(Header.SIP_IF_MATCH);

                if (hIfMatch == null) {
                    Exception ex = null;
                    // Create SIP_IF_MATCH header
                    try {
                        hIfMatch = StackConnector.headerFactory.createHeader(
                            Header.SIP_IF_MATCH, hEtag.getHeaderValue());
                        request.addHeader(hIfMatch);
                    } catch (NullPointerException npe) {
                        ex = npe;
                    } catch (ParseException pe) {
                        ex = pe;
                    } catch (SipException se) {
                        ex = se;
                    }
                    if (ex != null) {
                        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                            Logging.report(Logging.ERROR,
                               LogChannels.LC_JSR180,
                               "scc.notifyResponseReceived(): can't create " +
                               "SIP-If-Match header:" + ex);
                            ex.printStackTrace();
                        }
                    }
                } else {
                    hIfMatch.setHeaderValue(hEtag.getHeaderValue());
                }

                request.removeHeader(Header.SIP_ETAG);
            } else {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                         "scc.notifyResponseReceived(): response to PUBLISH " +
                         "doesn't contain SIP-Etag header!");
                }
            }
        }

        // Schedule the refresh if required (i.e., if the method is
        // refreshable). Refresh time is taken from the response,
        // either from Expires header or from "expires" parameter
        // of Contact header as described in RFCs 3261, section 10.2.4
        // and RFC 3265, section 3.1.1.
        // If a listener for refresh event has been set, it is notified.
        if (statusCode == SIPErrorCodes.OK) {
            scheduleRefresh(method);
        }

        // Notify the listener
        if (refreshID != null) {

            /*
             * Must report failure response codes (3xx - 6xx), 
             * may success codes (2xx), 
             * must not report provisional (1xx) responses.
             *
             * If a response arrives to a refresh request that was updated 
             * by the application since the request was sent then the response 
             * is not reported in the refreshEvent() callback.
             */
            if ((statusGroup != 1) && (responseBranch.equals(requestBranch))) {
                sipRefreshListener.refreshEvent(Integer.parseInt(refreshID),
                                                statusCode,
                                                response.getReasonPhrase());
            }
        }
    }

    /**
     * Gets the current state of connection.
     * @return the current state
     */
    public int getState() {
        return state;
    }

    /**
     * Gets the current client transaction.
     * @return the current client transaction
     */
    protected ClientTransaction getClientTransaction() {
        return clientTransaction;
    }

    /**
     * Gets the current sip stack.
     * @return the current sip stack
     */
    protected SipStack getSipStack() {
        return stackConnector.getSipStack();
    }

    /**
     * Gets the assigned SipConnectionNotifier.
     * @return the current SipConnectionNotifier
     */
    protected SipConnectionNotifier getSipConnectionNotifier() {
        return sipConnectionNotifier;
    }

    /**
     * Gets the response.
     * @return the response instance
     */
    protected Response getResponse() {
        return response;
    }

    /**
     * Clears the current response.
     */
    protected void clearResponse() {
        this.response = null;
    }

    /**
     * Schedules refreshing of the request if required.
     * @param method SIP method of the message
     */
    private void scheduleRefresh(String method) {
        if (sipRefreshListener == null) {
            return;
        }

        if (! (method.equals(Request.REGISTER) ||
               method.equals(Request.SUBSCRIBE) ||
               method.equals(Request.PUBLISH))) {
            return;
        }

        // Remove the body of the message in case if the request is PUBLISH,
        // see RFC 3903, p. 7 (section 4.1):
        // +-----------+-------+---------------+---------------+
        // | Operation | Body? | SIP-If-Match? | Expires Value |
        // +-----------+-------+---------------+---------------+
        // | Initial   | yes   | no            | > 0           |
        // | Refresh   | no    | yes           | > 0           |
        // | Modify    | yes   | yes           | > 0           |
        // | Remove    | no    | yes           | 0             |
        // +-----------+-------+---------------+---------------+
        if (method.equals(Request.PUBLISH)) {
            request.removeContent();
        }

        // If the expires is set, the refresh is scheduled for the
        // duration of the expires
        int expires, minExpires = Integer.MAX_VALUE;

        // RFC 3265, p. 6:
        // An "expires" parameter on the "Contact" header has no semantics for
        // SUBSCRIBE and is explicitly not equivalent to an "Expires" header in
        // a SUBSCRIBE request or response.
        if (!method.equals(Request.SUBSCRIBE)) {
            ContactList cl = response.getContactHeaders();

            if (cl != null) {
                // Take a minimal expiration time from Contact headers.
                Enumeration en = cl.getElements();

                while (en.hasMoreElements()) {
                    ContactHeader contactHeader =
                        (ContactHeader) en.nextElement();

                    if (contactHeader != null) {
                        try {
                            expires = Integer.parseInt(
                                contactHeader.getExpires());
                            if ((expires > 0) && (expires < minExpires)) {
                                minExpires = expires;
                            }
                        } catch (NumberFormatException e) {
                            // intentionally ignored
                            // in the worst case 
                            // minExpires = Integer.MAX_VALUE
                        }
                    }
                }
            }
        } // end if (not SUBSCRIBE)

        // Take an expiration time from the Expires header.
        ExpiresHeader expiresHeader =
            (ExpiresHeader) response.getHeader(ExpiresHeader.NAME);

        if (expiresHeader != null) {
            expires = expiresHeader.getExpires();

            if ((expires > 0) && (expires < minExpires)) {
                minExpires = expires;
            }
        }

        if (minExpires == Integer.MAX_VALUE || minExpires < 0) {
            // Apply defaults.
            minExpires = 3600;
        }

        // System.out.println(">>> Refresh time: " + minExpires);

        /*
                 if (expiresHeader != null) {
            expires = expiresHeader.getExpires();
            System.out.println(">>> From header: " + expires);
                 }
         */

        if (minExpires != 0) {
            RefreshManager.getInstance().scheduleTask(refreshID, minExpires);
        }
    }
    
    /**
     * Sets the listener for error notifications. Applications that want to 
     * receive notification about a failure of an asynchoronous send operation 
     * must implement the SipErrorListener interface and register it with a 
     * connection using this method. Only one listener can be set at any time, 
     * if a listener is already set it will be overwritten. Setting listener to 
     * null will remove the current listener.
     * @see JSR180 spec, v 1.1.0, p 28
     *
     * @param sel - reference to the listener object. The value null will remove
     * the existing listener.
     * @throws SipException - INVALID_STATE if the connection is closed
     */
    public void setErrorListener(SipErrorListener sel) throws SipException {        
        if (!connectionOpen) {
            throw new SipException("The error listener can not be set,"
                    + " because of wrong state.",
                    SipException.INVALID_STATE);
        }
        this.sipErrorListener = sel;
    }
    
    /**
     * Sets update state.
     * @param state - false stands for ongoing update operation, 
     * true - valid state to start update operation
     */
    public synchronized void setUpdateState(boolean state) {
        updateState = state;
    }
    
    /**
     * Gets update state
     * @return state - false stands for ongoing update operation,
     * true - valid state to start update operation
     */
    public synchronized boolean getUpdateState() {
        return updateState;
    }
}
