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

import java.util.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.*;
import gov.nist.core.*;
import java.io.IOException;
import javax.microedition.sip.SipException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Tracks dialogs. A dialog is a peer to peer association of communicating
 * SIP entities. For INVITE transactions, a Dialog is created when a success
 * message is received (i.e. a response that has a ToHeader tag).
 * The SIP Protocol stores enough state in the
 * message structure to extract a dialog identifier that can be used to
 * retrieve this structure from the SipStack.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Dialog {
    /** Opaque pointer to application data. */
    private Object applicationData;
    /** First transaction. */
    private Transaction firstTransaction;
    /** Last transaction. */
    private Transaction lastTransaction;
    /** Dialog identifier. */
    private String dialogId;
    /** Local sequence number. */
    private int localSequenceNumber;
    /** Remote sequence number. */
    private int remoteSequenceNumber;
    /** Local tag. */
    private String myTag;
    /** Remote tag. */
    private String hisTag;
    /** Route list. */
    private RouteList routeList;
    /** Contact route. */
    private RouteHeader contactRoute;
    /** User name. */
    private String user;
    /** Default route. */
    private RouteHeader defaultRoute;
    /** Current context SIP stack. */
    private SIPTransactionStack sipStack;
    /** Current Dialog state. */
    private int dialogState;
    /** ACK has been processed. */
    protected boolean ackSeen;
    /** Previous ACk. */
    protected Request lastAck;
    /** List of active subscriptions. */
    public SubscriptionList subscriptionList;
    /** Time remaining for retransmit. */
    private int retransmissionTicksLeft;
    /** Time used on previous retransmit. */
    private int prevRetransmissionTicks;
    /** Initial state. */
    public final static int INITIAL_STATE = -1;
    /** Early state. */
    public final static int EARLY_STATE = 1;
    /** Confirmed state. */
    public final static int CONFIRMED_STATE = 2;
    /** Completed state. */
    public final static int COMPLETED_STATE = 3;
    /** Terminated state. */
    public final static int TERMINATED_STATE = 4;

    /**
     * Sets the pointer to application data.
     * @param applicationData the new application data
     */
    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    /**
     * Gets pointer to opaque application data.
     * @return application data
     */
    public Object getApplicationData() {
        return this.applicationData;
    }

    /**
     * A debugging print routine.
     */
    private void printRouteList() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "this : " + this);
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "printRouteList : " + this.routeList.encode());

            if (this.contactRoute != null) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "contactRoute : " + this.contactRoute.encode());
            } else {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "contactRoute : null");
            }
        }
    }

    /**
     * Gets the next hop.
     * @return the next hop
     * @exception SipException if an error occurs
     */
    public Hop getNextHop() throws SipException {
        // This is already an established dialog so dont consult the router.
        // Route the request based on the request URI.
        RouteList rl = this.getRouteList();
        SipURI sipUri = null;
        String transport;

        if (rl != null && ! rl.isEmpty()) {
            RouteHeader route = (RouteHeader) rl.getFirst();
            sipUri = (SipURI) (route.getAddress().getURI());
            transport = route.getAddress().
                            getParameter(SIPConstants.GENERAL_TRANSPORT);
        } else if (contactRoute != null) {
            sipUri = (SipURI) (contactRoute.getAddress().getURI());
            transport = contactRoute.getAddress().
                            getParameter(SIPConstants.GENERAL_TRANSPORT);
        } else {
            throw new SipException("No route found!",
                SipException.GENERAL_ERROR);
        }

        String host = sipUri.getHost();
        int port = sipUri.getPort();

        if (port == -1) {
            port = SIPConstants.DEFAULT_NONTLS_PORT;
        }

        if (transport == null) {
            transport = SIPConstants.TRANSPORT_UDP;
        }

        return new Hop(host, port, transport);
    }

    /**
     * Returns true if this is a client dialog.
     *
     * @return true if the transaction that created this dialog is a
     *client transaction and false otherwise.
     */
    public boolean isClientDialog() {
        Transaction transaction = (Transaction) getFirstTransaction();
        return transaction instanceof ClientTransaction;
    }

    /**
     * Sets the state for this dialog.
     *
     * @param state is the state to set for the dialog.
     */
    public void setState(int state) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Setting dialog state for " + this);
            // new Exception().printStackTrace();

            if (state != INITIAL_STATE &&
                    state != this.dialogState) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "New dialog state is " +
                    state +
                    "dialogId = " +
                    this.getDialogId());
            }

        }

        this.dialogState = state;
    }

    /**
     * Debugging print for the dialog.
     */
    public void printTags() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "isServer = " + isServer());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "localTag = " + getLocalTag());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "remoteTag = " + getRemoteTag());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "firstTransaction = " +
                    ((Transaction) firstTransaction).getOriginalRequest());
        }
    }

    /**
     * Marks that the dialog has seen an ACK.
     * @param sipRequest the current SIP transaction
     */
    public void ackReceived(Request sipRequest) {
        if (isServer()) {
            ServerTransaction st = (ServerTransaction) getFirstTransaction();
            if (st == null) {
                return;
            }

            // Suppress retransmission of the final response (in case
            // retransmission filter is being used).
            if (st.getOriginalRequest().getCSeqHeader().getSequenceNumber() ==
                    sipRequest.getCSeqHeader().getSequenceNumber()) {
                st.setState(Transaction.TERMINATED_STATE);
                ackSeen = true;
                lastAck = sipRequest;
            }
        }
    }

    /**
     * Returns true if the dialog has been acked. The ack is sent up to the
     * TU exactly once when retransmission filter is enabled.
     * @return true if ACK has been processed
     */
    public boolean isAckSeen() {
        return this.ackSeen;
    }

    /**
     * Gets the last ACK.
     * @return the last ack
     */
    public Request getLastAck() {
        return this.lastAck;
    }


    /**
     * Gets the transaction that created this dialog.
     * @return the first transaction
     */
    public Transaction getFirstTransaction() {
        return firstTransaction;
    }

    /**
     * Gets the route set for the dialog.
     * When acting as an User Agent Server
     * the route set MUST be set to the list of URIs in the
     * Record-Route header field from the request, taken in order and
     * preserving all URI parameters. When acting as an User Agent
     * Client the route set MUST be set to the list of URIs in the
     * Record-Route header field from the response, taken in
     * reverse order and preserving all URI parameters. If no Record-Route
     * header field is present in the request or response,
     * the route set MUST be set to the empty set. This route set,
     * even if empty, overrides any
     * pre-existing route set for future requests in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route
     * and Contact header fields.
     * However, these requests do not cause the dialog's route set to be
     * modified.
     * <p>
     * The User Agent Client uses the remote target
     * and route set to build the
     * Request-URI and Route header field of the request.
     *
     * @return an Iterator containing a list of route headers to be used for
     * forwarding. Empty iterator is returned if route has not
     * been established.
     */
    public Enumeration getRouteSet() {
        if (this.routeList == null)
            return null;
        else
            return this.getRouteList().getElements();
    }

    /**
     * Gets the route list.
     * @return the route list
     */
    private RouteList getRouteList() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "getRouteList " + this.getDialogId());
        }

        // Find the top via in the route list.
        Vector li = routeList.getHeaders();
        RouteList retval = new RouteList();

        // If I am a UA then I am not record routing the request.

        retval = new RouteList();

        for (int i = 0; i < li.size(); i++) {
            RouteHeader route = (RouteHeader) li.elementAt(i);
            retval.add(route.clone());
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "----->>> ");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "getRouteList for " + this);

            if (retval != null) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "RouteList = " + retval.encode());
            }

            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "myRouteList = " + routeList.encode());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "----->>> ");
        }

        return retval;
    }

    /**
     * Sets the stack address.
     * Prevent us from routing messages to ourselves.
     * @param sipStack the address of the SIP stack.
     */
    public void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    /**
     * Sets the default route (the default next hop for the proxy or
     * the proxy address for the user agent).
     *
     * @param defaultRoute is the default route to set.
     *
     */
    public void setDefaultRoute(RouteHeader defaultRoute) {
        this.defaultRoute = (RouteHeader) defaultRoute.clone();
        // addRoute(defaultRoute,false);
    }

    /**
     * Sets the user name for the default route.
     *
     * @param user is the user name to set for the default route.
     *
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Adds a route list extracted from a record route list.
     * If this is a server dialog then we assume that the record
     * are added to the route list IN order. If this is a client
     * dialog then we assume that the record route headers give us
     * the route list to add in reverse order.
     *
     * @param recordRouteList -- the record route list from the incoming
     * @param transport       -- the transport parameter
     * message.
     */
    private void addRoute(RecordRouteList recordRouteList, String transport) {
        if (this.isClientDialog()) {
            // This is a client dialog so we extract the record
            // route from the response and reverse its order to
            // careate a route list.
            this.routeList = new RouteList();
            // start at the end of the list and walk backwards
            Vector li = recordRouteList.getHeaders();
            for (int i = li.size() -1; i >= 0; i--) {
                RecordRouteHeader rr = (RecordRouteHeader) li.elementAt(i);
                Address addr = (Address) rr.getAddress();
                RouteHeader route = new RouteHeader();
                route.setAddress
                        ((Address)(rr.getAddress()).clone());
                route.setParameters
                        ((NameValueList)rr.getParameters().clone());
                setRouteTransport(route, transport);
                this.routeList.add(route);
            }
        } else {
            // This is a server dialog. The top most record route
            // header is the one that is closest to us. We extract the
            // route list in the same order as the addresses in the
            // incoming request.
            this.routeList = new RouteList();
            Vector li = recordRouteList.getHeaders();
            for (int i = 0; i < li.size(); i++) {
                RecordRouteHeader rr = (RecordRouteHeader) li.elementAt(i);
                RouteHeader route = new RouteHeader();
                route.setAddress
                        ((Address)(rr.getAddress()).clone());
                route.setParameters((NameValueList)rr.getParameters().
                        clone());
                setRouteTransport(route, transport);
                routeList.add(route);
            }
        }
    }

    /**
     * Sets the transport parameter to route header.
     *
     * @param route the route header for setting transport param
     * @param transport the name of transport param
     *
     */
    private void setRouteTransport(RouteHeader route, String transport) {
        if (route.getAddress().
            getParameter(SIPConstants.GENERAL_TRANSPORT) == null) {
            route.getAddress().setParameter(SIPConstants.GENERAL_TRANSPORT,
                                            transport);
        }
    }

    /**
     * Adds a route list extacted from the contact list of the incoming
     * message.
     *
     * @param contactList contact list extracted from the incoming
     * message.
     * @param transport the transport parameter
     *
     */
    private void addRoute(ContactList contactList, String transport) {
        if (contactList.size() == 0)
            return;
        ContactHeader contact = (ContactHeader) contactList.getFirst();
        RouteHeader route = new RouteHeader();
        route.setAddress
                ((Address)((Address)(contact.getAddress())).clone());
        setRouteTransport(route, transport);
        this.contactRoute = route;
    }

    /**
     * Extracts the route information from this SIP Message and
     * add the relevant information to the route set.
     * @param sipMessage is the SIP message for which we want
     * to add the route.
     */
    public synchronized void addRoute(Message sipMessage) {
        String method = sipMessage.getCSeqHeader().getMethod();

        // cannot add route list after the dialog is initialized.
        try {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "addRoute: dialogState: " + this + "state = " +
                    this.getState());
            }

            String transport =
                ((ViaHeader)(sipMessage.getViaHeaders().getFirst())).
                    getTransport();

            // UPDATE processing: change route according to contact list
            if (Request.UPDATE.equals(method) &&
                ((this.dialogState == EARLY_STATE) ||
                    (this.dialogState == CONFIRMED_STATE))) {

                ContactList contactList = sipMessage.getContactHeaders();
                if (contactList != null) {
                    this.addRoute(contactList, transport);
                }
                return;
            }

            if (this.dialogState == CONFIRMED_STATE ||
                    this.dialogState == COMPLETED_STATE ||
                    this.dialogState == TERMINATED_STATE) {
                return;
            }

            if (!isServer()) {
                // I am CLIENT dialog.
                if (sipMessage instanceof Response) {
                    Response sipResponse = (Response) sipMessage;
                    if (sipResponse.getStatusCode() == 100) {
                        // Do nothing for trying messages.
                        return;
                    }

                    RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();

                    // Add the route set from the incoming response in reverse
                    // order
                    if (rrlist != null) {
                        this.addRoute(rrlist, transport);
                    } else {
                        // Set the rotue list to the last seen route list.
                        this.routeList = new RouteList();
                    }

                    ContactList contactList = sipMessage.getContactHeaders();
                    if (contactList != null) {
                        if (sipResponse.getStatusCode()/100 == 2 &&
                                (dialogState == INITIAL_STATE ||
                                        dialogState == EARLY_STATE)) {
                            ContactHeader contact =
                                    (ContactHeader)contactList.getFirst();

                            if (contact != null) {
                                String newTransport = contact.getAddress().
                                        getParameter(SIPConstants.GENERAL_TRANSPORT);
                                if (newTransport != null) {
                                    transport = newTransport;
                                }
                            }
                        }
                        
                        this.addRoute(contactList, transport);
                    }
                }
            } else {
                if (sipMessage instanceof Request) {
                    // Incoming Request has the route list
                    RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();
                    // Add the route set from the incoming response in reverse
                    // order
                    if (rrlist != null) {

                        this.addRoute(rrlist, transport);
                    } else {
                        // Set the rotue list to the last seen route list.
                        this.routeList = new RouteList();
                    }
                    // put the contact header from the incoming request into
                    // the route set.
                    ContactList contactList = sipMessage.getContactHeaders();
                    if (contactList != null) {
                        this.addRoute(contactList, transport);
                    }
                }
            }
        } finally {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                // new Exception()printStackTrace();
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "added a route = " + routeList.encode() +
                    "contactRoute = " + contactRoute);
            }
        }
    }

    /**
     * Protected Dialog constructor.
     */
    private Dialog() {
        subscriptionList = new SubscriptionList();
        routeList = new RouteList();
        dialogState = INITIAL_STATE; // not yet initialized.
        localSequenceNumber = 0;
        remoteSequenceNumber = -1;
    }

    /**
     * Sets the dialog identifier.
     * @param newDialogId the new dialog identifier
     */
    public void setDialogId(String newDialogId) {
        dialogId = newDialogId;
    }

    /**
     * Constructor given the first transaction.
     *
     * @param transaction is the first transaction.
     */
    protected Dialog(Transaction transaction) {
        this();
        addTransaction(transaction);
    }

    /**
     * Create route set for request.
     *
     * @param request the input request
     * @throws SipException if any occurs
     */
    private void  buildRouteSet(Request request) throws SipException {
        getLastTransaction().buildRouteSet(request);
    }

    /**
     * Returns true if is server.
     *
     * @return true if is server transaction created this dialog.
     */
    public boolean isServer() {
        return getFirstTransaction() instanceof ServerTransaction;
    }

    /**
     * Gets the id for this dialog.
     *
     * @return the string identifier for this dialog.
     */
    public String getDialogId() {
        if (firstTransaction instanceof ServerTransaction) {
            // if (true || dialogId == null) {
            Request sipRequest = (Request)
                ((ServerTransaction) firstTransaction).getOriginalRequest();
            dialogId = sipRequest.getDialogId(true, myTag);
            // }
        } else {
            // This is a client transaction. Compute the dialog id
            // from the tag we have assigned to the outgoing
            // response of the dialog creating transaction.
            if (firstTransaction != null &&
                    ((ClientTransaction)getFirstTransaction()).
                        getLastResponse() != null) {
                dialogId = ((ClientTransaction)getFirstTransaction()).
                        getLastResponse().getDialogId(false, hisTag);
            }
        }

        return dialogId;
    }

    /**
     * Adds a transaction record to the dialog.
     *
     * @param transaction is the transaction to add to the dialog.
     */
    public void addTransaction(Transaction transaction) {
        Request sipRequest = (Request) transaction.getOriginalRequest();

        // Processing a re-invite.
        // IMPL_NOTE : handle the re-invite
        // Set state to Completed if we are processing a
        // BYE transaction for the dialog.
        // Will be set to TERMINATED after the BYE
        // transaction completes.

        if (sipRequest.getMethod().equals(Request.BYE)) {
            this.setState(COMPLETED_STATE);
        }

        if (firstTransaction == null) {
            // Record the local and remote sequence
            // numbers and the from and to tags for future
            // use on this dialog.
            firstTransaction = transaction;
            // IMPL_NOTE : set the local and remote party
            if (transaction instanceof ServerTransaction) {
                hisTag = sipRequest.getFromHeader().getTag();
                // My tag is assigned when sending response
            } else {
                setLocalSequenceNumber
                        (sipRequest.getCSeqHeader().getSequenceNumber());
                // his tag is known when receiving response
                myTag = sipRequest.getFromHeader().getTag();
                if (myTag == null) {
                    throw new RuntimeException("bad message tag missing!");
                }
            }
        } else {
            String origMethod = 
                transaction.getOriginalRequest().getMethod();
            if (origMethod.equals(Request.ACK)) {
                origMethod = Request.INVITE;
            }
            if (firstTransaction.getOriginalRequest().getMethod().equals
                (origMethod) &&
                (((firstTransaction instanceof ServerTransaction) &&
                (transaction instanceof ClientTransaction)) ||
                ((firstTransaction instanceof ClientTransaction) &&
                (transaction instanceof ServerTransaction)))) {
                // Switch from client side to server side for re-invite
                // (put the other side on hold).

                firstTransaction = transaction;
            }
        }

        if (transaction instanceof ServerTransaction) {
            setRemoteSequenceNumber(sipRequest.getCSeqHeader().
                getSequenceNumber());
        }

        lastTransaction = transaction;

        // set a back ptr in the incoming dialog.
        transaction.setDialog(this);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Transaction Added " + this + myTag + "/" + hisTag);
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "TID = " + transaction.getTransactionId() +
                    "/" + transaction.IsServerTransaction());
            // new Exception().printStackTrace();
        }
    }

    /**
     * Sets the remote tag.
     *
     * @param hisTag is the remote tag to set.
     */
    public void setRemoteTag(String hisTag) {
        this.hisTag = hisTag;
    }

    /**
     * Gets the last transaction from the dialog.
     * @return last transaction
     */
    public Transaction getLastTransaction() {
        return this.lastTransaction;
    }

    /**
     * Sets the local sequece number for the dialog (defaults to 1 when
     * the dialog is created).
     *
     * @param lCseq is the local cseq number.
     *
     */
    protected void setLocalSequenceNumber(int lCseq) {
        this.localSequenceNumber = lCseq;
    }

    /**
     * Sets the remote sequence number for the dialog.
     *
     * @param rCseq is the remote cseq number.
     *
     */
    protected void setRemoteSequenceNumber(int rCseq) {
        this.remoteSequenceNumber = rCseq;
    }

    /**
     * Increments the local CSeqHeader # for the dialog.
     *
     * @return the incremented local sequence number.
     *
     */
    public int incrementLocalSequenceNumber() {
        return ++this.localSequenceNumber;
    }

    /**
     * Gets the remote sequence number (for cseq assignment of outgoing
     * requests within this dialog).
     *
     * @return local sequence number.
     */
    public int getRemoteSequenceNumber() {
        return this.remoteSequenceNumber;
    }

    /**
     * Gets the local sequence number (for cseq assignment of outgoing
     * requests within this dialog).
     *
     * @return local sequence number.
     */
    public int getLocalSequenceNumber() {
        return this.localSequenceNumber;
    }

    /**
     * Gets local identifier for the dialog.
     * This is used in FromHeader header tag construction
     * for all outgoing client transaction requests for
     * this dialog and for all outgoing responses for this dialog.
     * This is used in ToHeader tag constuction for all outgoing
     * transactions when we are the server of the dialog.
     * Use this when constucting ToHeader header tags for BYE requests
     * when we are the server of the dialog.
     *
     * @return the local tag.
     */
    public String getLocalTag() {
        return this.myTag;
    }

    /**
     * Gets peer identifier identifier for the dialog.
     * This is used in ToHeader header tag construction for all outgoing
     * requests when we are the client of the dialog.
     * This is used in FromHeader tag construction for all outgoing
     * requests when we are the Server of the dialog. Use
     * this when costructing FromHeader header Tags for BYE requests
     * when we are the server of the dialog.
     *
     * @return the remote tag
     * (note this is read from a response to an INVITE).
     *
     */
    public String getRemoteTag() {
        return hisTag;
    }

    /**
     * Sets local tag for the transaction.
     *
     * @param mytag is the tag to use in FromHeader headers client
     * transactions that belong to this dialog and for
     * generating ToHeader tags for Server transaction requests that belong
     * to this dialog.
     */
    public void setLocalTag(String mytag) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "set Local tag " + mytag + " " + this.dialogId);
            // new Exception().printStackTrace();
        }

        myTag = mytag;
    }

    /**
     * Marks all the transactions in the dialog inactive and ready
     * for garbage collection.
     */
    protected void deleteTransactions() {
        firstTransaction = null;
        lastTransaction = null;
    }

    /**
     * This method will release all resources associated with this dialog
     * that are tracked by the Provider. Further references to the dialog by
     * incoming messages will result in a mismatch.
     * Since dialog destruction is left reasonably open ended in RFC3261,
     * this delete method is provided
     * for future use and extension methods that do not require a BYE to
     * terminate a dialogue. The basic case of the INVITE and all dialogues
     * that we are aware of today it is expected that BYE requests will
     * end the dialogue.
     */
    public void delete() {
        // the reaper will get him later.
        setState(TERMINATED_STATE);
    }

    /**
     * Returns the Call-ID for this SipSession. This is the value of the
     * Call-ID header for all messages belonging to this session.
     *
     * @return the Call-ID for this Dialogue
     */
    public CallIdHeader getCallId() {
        Request sipRequest =
                ((Transaction)this.getFirstTransaction()).getOriginalRequest();
        return sipRequest.getCallId();
    }

    /**
     * Gets the local Address for this dialog.
     *
     * @return the address object of the local party.
     */
    public Address getLocalParty() {
        Request sipRequest =
                ((Transaction)this.getFirstTransaction()).getOriginalRequest();
        if (!isServer()) {
            return sipRequest.getFromHeader().getAddress();
        } else {
            return sipRequest.getTo().getAddress();
        }
    }

    /**
     * Returns the Address identifying the remote party.
     * This is the value of the ToHeader header of locally initiated
     * requests in this dialogue when acting as an User Agent Client.
     * <p>
     * This is the value of the FromHeader header of recieved responses in this
     * dialogue when acting as an User Agent Server.
     *
     * @return the address object of the remote party.
     */
    public Address getRemoteParty() {
        Request sipRequest =
                ((Transaction)this.getFirstTransaction()).getOriginalRequest();
        if (!isServer()) {
            return sipRequest.getTo().getAddress();
        } else {
            return sipRequest.getFromHeader().getAddress();
        }
    }

    /**
     * Returns the Address identifying the remote target.
     * This is the value of the Contact header of recieved Responses
     * for Requests or refresh Requests
     * in this dialogue when acting as an User Agent Client <p>
     * This is the value of the Contact header of received Requests
     * or refresh Requests in this dialogue when acting as an User
     *
     * @return the address object of the remote target.
     */
    public Address getRemoteTarget() {
        if (contactRoute == null) {
            return null;
        }
        return contactRoute.getAddress();
    }

    /**
     * Returns the current state of the dialogue. The states are as follows:
     * <ul>
     * <li> Early - A dialog is in the "early" state, which occurs when it is
     * created when a provisional response is recieved to the INVITE Request.
     * <li> Confirmed - A dialog transitions to the "confirmed" state when a 2xx
     * final response is received to the INVITE Request.
     * <li> Completed - A dialog transitions to the "completed" state when a BYE
     * request is sent or received by the User Agent Client.
     * <li> Terminated - A dialog transitions to the "terminated" state when it
     * can be garbage collection.
     * </ul>
     * Independent of the method, if a request outside of a dialog generates a
     * non-2xx final response, any early dialogs created through provisional
     * responses to that request are terminated. If no response arrives at all
     * on the early dialog, it also terminates.
     *
     * @return a DialogState determining the current state of the dialog.
     */
    public int getState() {
        return this.dialogState;
    }

    /**
     * Returns true if this Dialog is secure i.e. if the request arrived over
     * TLS, and the Request-URI contained a SIPS URI, the "secure" flag is set
     * to TRUE.
     *
     * @return <code>true</code> if this dialogue was established using a sips
     * URI over TLS, and <code>false</code> otherwise.
     */
    public boolean isSecure() {
        return Utils.equalsIgnoreCase(getFirstTransaction().getRequest().
                getRequestURI().getScheme(), SIPConstants.SCHEME_SIPS);
    }

    /**
     * Sends ACK Request to the remote party of this Dialogue.
     *
     * @param request the new ACK Request message to send.
     * @param isResend true if the method is called for resending request
     * @throws SipException if implementation cannot send the ACK Request for
     * any other reason
     */
    public void sendAck(Request request, boolean isResend) throws SipException {
        Request ackRequest = (Request) request;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "sendAck" + this);
        }

        if (isServer()) {
            throw new SipException("Cannot sendAck from " +
                "Server side of Dialog", SipException.DIALOG_UNAVAILABLE);
        }

        if (!ackRequest.getMethod().equals(Request.ACK)) {
            throw new SipException("Bad request method -- should be ACK",
                SipException.INVALID_MESSAGE);
        }

        if (getState() == -1 || getState() == EARLY_STATE) {
            throw new SipException("Bad dialog state " + getState(),
                SipException.INVALID_STATE);
        }

        if (! ((Transaction)getFirstTransaction()).
                getOriginalRequest().getCallId().
                getCallId().equals(((Request)request).
                getCallId().getCallId())) {
            throw new SipException("Bad call ID in request",
                SipException.INVALID_MESSAGE);
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "setting from tag For outgoing ACK = " + this.getLocalTag());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "setting ToHeader tag for outgoing ACK = " +
                    this.getRemoteTag());
        }

        if (this.getLocalTag() != null)
            ackRequest.getFromHeader().setTag(this.getLocalTag());
        if (this.getRemoteTag() != null)
            ackRequest.getTo().setTag(this.getRemoteTag());

        // Create the route request and set it appropriately.
        // Note that we only need to worry about being on the client
        // side of the request.
        buildRouteSet(ackRequest);

        Hop hop = getNextHop();

        try {
            MessageChannel messageChannel =
                    sipStack.createRawMessageChannel(hop);
            if (messageChannel == null) {
                // procedures of 8.1.2 and 12.2.1.1 of RFC3261 have
                // been tried but the resulting next hop cannot be
                // resolved (recall that the exception thrown is
                // caught and ignored in
                // SIPMessageStack.createMessageChannel() so we end
                // up here with a null messageChannel instead of the
                // exception handler below). All else failing, try
                // the outbound proxy in accordance with 8.1.2, in
                // particular: This ensures that outbound proxies
                // that do not add Record-Route header field values
                // will drop out of the path of subsequent requests.
                // It allows endpoints that cannot resolve the first
                // Route URI to delegate that task to an outbound
                // proxy.
                //
                // if one considers the 'first Route URI' of a
                // request constructed according to 12.2.1.1
                // to be the request URI when the route set is empty.
                Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
                if (outboundProxy == null) {
                    throw new SipException("No route found!",
                        SipException.GENERAL_ERROR);
                }

                messageChannel = sipStack.createRawMessageChannel(
                        outboundProxy);
            }
            // Wrap a client transaction around the raw message channel.
            ClientTransaction clientTransaction =
                    (ClientTransaction)
                    sipStack.createMessageChannel(messageChannel);
            clientTransaction.setOriginalRequest(ackRequest);
            clientTransaction.sendMessage((Message)ackRequest);
            // Do not retransmit the ACK so terminate the transaction
            // immediately.
            this.lastAck = ackRequest;
            clientTransaction.setState(Transaction.TERMINATED_STATE);
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "Exception occured: " + ex);
                // ex.printStackTrace();
            }
            
            if ((isResend) && (ackRequest.getErrorListener() != null)) {
                ackRequest.getErrorListener().notifyError("Failed to " +
                        "retransmit the following request: " + ackRequest);
            }

            throw new SipException("Cold not create message channel",
                SipException.GENERAL_ERROR);
        }
    }

    /**
     * Creates a new Request message based on the dialog creating request.
     * This method should be used for but not limited to creating Bye's,
     * Refer's and re-Invite's on the Dialog. The returned Request will be
     * correctly formatted that is it will contain the correct
     * CSeqHeader header,
     * Route headers and requestURI (derived from the remote target). This
     * method should not be used for Ack, that is the application should
     * create the Ack from the MessageFactory.
     *
     * If the route set is not empty, and the first URI in the route set
     * contains the lr parameter (see Section 19.1.1), the UAC MUST place
     * the remote target URI into the Request-URI and MUST include a Route
     * header field containing the route set values in order, including all
     * parameters.
     * If the route set is not empty, and its first URI does not contain the
     * lr parameter, the UAC MUST place the first URI from the route set
     * into the Request-URI, stripping any parameters that are not allowed
     * in a Request-URI. The UAC MUST add a Route header field containing
     * the remainder of the route set values in order, including all
     * parameters. The UAC MUST then place the remote target URI into the
     * Route header field as the last value.
     *
     * @param method the string value that determines if the request to be
     * created.
     * @return the newly created Request message on this Dialog.
     * @throws SipException if the Dialog is not yet established.
     */
    public Request createRequest(String method) throws SipException {
        // Set the dialog back pointer.
        if (method == null)
            throw new NullPointerException("null method");
        else if (this.getState() == -1 ||
                ((! method.equals(Request.BYE)) &&
                this.getState() == TERMINATED_STATE)
                || (method.equals(Request.BYE) &&
                this.getState() == EARLY_STATE)) {
            throw new SipException("Dialog not yet established or terminated",
                SipException.DIALOG_UNAVAILABLE);
        }

        Request originalRequest = (Request) getFirstTransaction().getRequest();

        RequestLine requestLine = new RequestLine();
        requestLine.setUri((URI)getRemoteParty().getURI());
        requestLine.setMethod(method);

        Request sipRequest =
                originalRequest.createRequest(requestLine, isServer());

        // Guess of local sequence number - this is being re-set when
        // the request is actually dispatched (reported by Brad Templeton
        // and Antonis Karydas).
        if (! method.equals(Request.ACK)) {
            CSeqHeader cseq = (CSeqHeader) sipRequest.getCSeqHeader();
            cseq.setSequenceNumber(this.localSequenceNumber + 1);
        }

        if (isServer()) {
            // Remove the old via headers.
            sipRequest.removeHeader(Header.VIA);
            // Add a via header for the outbound request based on the
            // transport of the message processor.

            // MessageProcessor messageProcessor = sipStack.getMessageProcessor(
            //         firstTransaction.encapsulatedChannel.getTransport());
            MessageProcessor messageProcessor =
                firstTransaction.encapsulatedChannel.getMessageProcessor();

            ViaHeader via = messageProcessor.getViaHeader();
            sipRequest.addHeader(via);
        }

        FromHeader from = (FromHeader) sipRequest.getFromHeader();
        ToHeader to = (ToHeader) sipRequest.getTo();

        from.setTag(getLocalTag());
        to.setTag(getRemoteTag());

        // RFC 3261, p. 75:
        // A UAC SHOULD include a Contact header field in any target refresh
        // requests within a dialog, and unless there is a need to change it,
        // the URI SHOULD be the same as used in previous requests within the
        // dialog.
        ContactList cl = originalRequest.getContactHeaders();
        if (cl != null) {
            sipRequest.addHeader((ContactList)(cl.clone()));
        }

        // get the route list from the dialog.
        buildRouteSet(sipRequest);

        return sipRequest;
    }

    /**
     * Sends a Request to the remote party of this dialog. This method
     * implies that the application is functioning as UAC hence the
     * underlying SipProvider acts statefully. This method is useful for
     * sending Bye's for terminating a dialog or Re-Invites on the Dialog
     * for third party call control.
     * <p>
     * This methods will set the FromHeader and the ToHeader tags for
     * the outgoing
     * request and also set the correct sequence number to the outgoing
     * Request and associate the client transaction with this dialog.
     * Note that any tags assigned by the user will be over-written by this
     * method.
     * <p>
     * The User Agent must not send a BYE on a confirmed INVITE until it has
     * received an ACK for its 2xx response or until the server transaction
     * timeout is received.
     * <p>
     * When the retransmissionFilter is <code>true</code>,
     * that is the SipProvider takes care of all retransmissions for the
     * application, and the SipProvider can not deliver the Request after
     * multiple retransmits the SipListener will be notified with a
     * {@link TimeoutEvent} when the transaction expires.
     * @param clientTransactionId the new ClientTransaction object identifying
     * this transaction, this clientTransaction should be requested from
     * SipProvider.getNewClientTransaction
     * @throws TransactionDoesNotExistException if the serverTransaction does
     * not correspond to any existing server transaction.
     * @throws SipException if implementation cannot send the Request for
     * any reason.
     */
    public void sendRequest(ClientTransaction clientTransactionId) throws
            SipException {
        if (clientTransactionId == null) {
            throw new NullPointerException("null parameter");
        }

        Request dialogRequest = clientTransactionId.getOriginalRequest();
        String method = dialogRequest.getMethod();
        
        if (method.equals(Request.ACK) || method.equals(Request.CANCEL)) {
            throw new SipException("Bad Request Method: " +
                    dialogRequest.getMethod(), SipException.INVALID_MESSAGE);
        }

        // Cannot send bye until the dialog has been established.
        if (getState() == INITIAL_STATE) {
            throw new SipException("Bad dialog state (-1).",
                SipException.DIALOG_UNAVAILABLE);
        }

        if (Utils.equalsIgnoreCase(dialogRequest.getMethod(), Request.BYE) &&
                getState() == EARLY_STATE) {
            throw new SipException("Bad dialog state ",
                SipException.DIALOG_UNAVAILABLE);
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "dialog.sendRequest " +
                " dialog = " + this + "\ndialogRequest = \n" +
                dialogRequest);
        }

        if (dialogRequest.getTopmostVia() == null) {
            ViaHeader via = ((ClientTransaction) clientTransactionId).
                getOutgoingViaHeader();
            dialogRequest.addHeader(via);
        }

        if (! ((Transaction)this.getFirstTransaction()).
                getOriginalRequest().getCallId().
                getCallId().equals(dialogRequest.
                getCallId().getCallId())) {
            throw new SipException("Bad call ID in request",
                SipException.INVALID_MESSAGE);
        }

        // Set the dialog back pointer.
        ((ClientTransaction)clientTransactionId).setDialog(this);

        FromHeader from = (FromHeader) dialogRequest.getFromHeader();
        ToHeader to = (ToHeader) dialogRequest.getTo();

        from.setTag(this.getLocalTag());
        to.setTag(this.getRemoteTag());

        // Caller has not assigned the route header - set the route header
        // and the request URI for the outgoing request.
        // Bugs reported by Brad Templeton.

        buildRouteSet(dialogRequest);

        Hop hop = this.getNextHop();

        try {
            MessageChannel messageChannel =
                    sipStack.createRawMessageChannel(hop);
            ((ClientTransaction) clientTransactionId).
                    encapsulatedChannel = messageChannel;

            if (messageChannel == null) {
                // procedures of 8.1.2 and 12.2.1.1 of RFC3261 have
                // been tried but the resulting next hop cannot be
                // resolved (recall that the exception thrown is
                // caught and ignored in
                // SIPMessageStack.createMessageChannel() so we end
                // up here with a null messageChannel instead of the
                // exception handler below). All else failing, try
                // the outbound proxy in accordance with 8.1.2, in
                // particular: This ensures that outbound proxies
                // that do not add Record-Route header field values
                // will drop out of the path of subsequent requests.
                // It allows endpoints that cannot resolve the first
                // Route URI to delegate that task to an outbound
                // proxy.
                //
                // if one considers the 'first Route URI' of a
                // request constructed according to 12.2.1.1
                // to be the request URI when the route set is empty.
                Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
                if (outboundProxy == null) {
                    throw new SipException("No route found!",
                        SipException.GENERAL_ERROR);
                }
                messageChannel =
                    sipStack.createRawMessageChannel(outboundProxy);
            }

            ((ClientTransaction) clientTransactionId).encapsulatedChannel =
                messageChannel;
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "Exception occured: " + ex);
            }

            throw new SipException("Could not create message channel.",
                SipException.GENERAL_ERROR);
        }

        // Increment before setting!!
        localSequenceNumber ++;
        dialogRequest.getCSeqHeader().
                setSequenceNumber(getLocalSequenceNumber());

        if (this.isServer()) {
            // ServerTransaction serverTransaction = (ServerTransaction)
            //     getFirstTransaction();

            from.setTag(this.myTag);
            to.setTag(this.hisTag);

            try {
                ((ClientTransaction) clientTransactionId).sendMessage(
                        dialogRequest);
                // If the method is BYE then mark the dialog completed.
                if (dialogRequest.getMethod().equals(Request.BYE)) {
                    setState(COMPLETED_STATE);
                }
            } catch (IOException ex) {
                throw new SipException("Error sending message.",
                    SipException.GENERAL_ERROR);
            }
        } else {
            // I am the client so I do not swap headers.
            // ClientTransaction clientTransaction = (ClientTransaction)
            //     getFirstTransaction();

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "setting tags from " + this.getDialogId());
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "fromTag " + this.myTag);
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "toTag " + this.hisTag);
            }

            from.setTag(this.myTag);
            to.setTag(this.hisTag);

            try {
                ((ClientTransaction)clientTransactionId).
                        sendMessage(dialogRequest);

                // If the method is BYE then mark the dialog completed.
                if (dialogRequest.getMethod().equals(Request.BYE)) {
                    this.setState(COMPLETED_STATE);
                }
            } catch (IOException ex) {
                throw new SipException("Error sending message.",
                    SipException.GENERAL_ERROR);
            }
        }
    }

    /**
     * Returns yes if the last response is to be retransmitted.
     * @return true if final response retransmitted
     */
    protected boolean toRetransmitFinalResponse() {
        if (--retransmissionTicksLeft == 0) {
            retransmissionTicksLeft = 2*prevRetransmissionTicks;
            prevRetransmissionTicks = retransmissionTicksLeft;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets retransmission ticks.
     */
    protected void setRetransmissionTicks() {
        retransmissionTicksLeft = 1;
        prevRetransmissionTicks = 1;
    }

    /**
     * Resends the last ack.
     */
    public void resendAck() {
        // Check for null.
        try {
            if (lastAck != null)
                sendAck(lastAck, true);
        } catch (SipException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "Dialog.resendAck(): SipException occured:" + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Checks if this is an invitation dialog.
     * @return true if this is an invitation dialog
     */
    public boolean isInviteDialog() {
        return getFirstTransaction().getRequest().getMethod().
                equals(Request.INVITE);
    }

    /**
     * Checks if this is a subscription dialog.
     * @return true if this is a subscription dialog
     */
    public boolean isSubscribeDialog() {
        String method = getFirstTransaction().getRequest().getMethod();
        // REFER creates an implicit subscription
        return (method.equals(Request.SUBSCRIBE) ||
                method.equals(Request.REFER));
    }
}
