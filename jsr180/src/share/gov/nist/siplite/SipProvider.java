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
import java.io.*;
import javax.microedition.sip.SipException;

import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.stack.*;
import gov.nist.core.*;
import gov.nist.siplite.address.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Implementation of the JAIN-SIP provider interface.
 *
 * @version  JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">
 * This code is in the public domain.</a>
 *
 */
public final class SipProvider implements
        SIPTransactionEventListener {
    /** Current SIP listener. */
    protected   SipListener sipListener;
    /** Flag to indicate the provider is active. */
    protected boolean isActive;
    /** Current SIP Stack context. */
    protected  SipStack sipStack;
    /** Current event listening filter. */
    protected  ListeningPoint listeningPoint;
    /** Curent server transactions. */
    protected   ServerTransaction currentTransaction;
    /** Curent event scanner processor. */
    private    EventScanner eventScanner;

    /**
     * Stops processing messages for this provider. Post an empty
     * message to our message processing queue that signals us to
     * quit.
     */
    protected  void stop() {
        // Put an empty event in the queue and post ourselves a message.
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Exiting provider");
        }

        synchronized (this) {
            listeningPoint.removeSipProvider();
        }

        this.eventScanner.stop();
    }


    /**
     * Handles the SIP event - because we have only one listener and we are
     * already in the context of a separate thread, we dont need to enque
     * the event and signal another thread.
     *
     * @param sipEvent is the event to process.
     * @param transaction the current transaction
     *
     */
    public void handleEvent(SipEvent sipEvent,
            Transaction transaction) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "handleEvent " + sipEvent +
                "currentTransaction = " + transaction +
                "this.sipListener = " + this.sipListener);
        }

        if (this.sipListener == null)
            return;

        EventWrapper eventWrapper = new EventWrapper();
        eventWrapper.sipEvent = sipEvent;
        eventWrapper.transaction = transaction;

        if (transaction != null &&
            transaction instanceof ClientTransaction) {
                ((ClientTransaction)transaction).setEventPending();
        }

        this.eventScanner.addEvent(eventWrapper);
    }

    /**
     * Creates a new instance of SipProvider.
     * @param sipStack the current SIP stack context
     */
    protected SipProvider(SipStack sipStack) {
        this.sipStack = sipStack;
        this.eventScanner = sipStack.eventScanner;
    }

    /**
     * Compares to this instance for equivalence.
     * @param obj object for comparison
     * @return true if the object is the same
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * This method registers the SipListener object to this SipProvider, once
     * registered the SIP Listener can send events on the SipProvider and
     * recieve events emitted from the SipProvider. As JAIN SIP resticts a
     * unicast Listener special case, that is, that one and only one Listener
     * may be registered on the SipProvider concurrently.
     * <p>
     * If an attempt is made to re-register the existing SipListener this
     * method returns silently. A previous SipListener must be removed from the
     * SipProvider before another SipListener can be registered to
     * the SipProvider.
     *
     * @param sipListener to be registered with the
     * Provider.
     * @throws TooManyListenersException this exception is thrown when a new
     * SipListener attempts to register with the SipProvider when another
     * SipListener is already registered with this SipProvider.
     *
     */
    public void addSipListener(SipListener sipListener)
    throws TooManyListenersException {

        synchronized (sipStack) {
            Enumeration it = sipStack.getSipProviders();
            while (it.hasMoreElements()) {
                SipProvider provider = (SipProvider) it.nextElement();
                if (provider.sipListener  != null &&
                        provider.sipListener != sipListener)
                    throw new TooManyListenersException();
            }
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "add SipListener " + sipListener);
        }

        this.sipListener = sipListener;
        sipStack.sipListener = sipListener;

        synchronized (sipStack) {
            Enumeration it = sipStack.getSipProviders();
            while (it.hasMoreElements()) {
                SipProvider provider = (SipProvider) it.nextElement();
                provider.sipListener = sipListener;
            }
        }
    }

    /**
     * Returns the ListeningPoint of this SipProvider.
     * A SipProvider has a single Listening Point at any specific point in time.
     *
     * @see ListeningPoint
     * @return the ListeningPoint of this SipProvider
     */
    public ListeningPoint getListeningPoint() {
        return this.listeningPoint;
    }

    /**
     * Returns a unique CallIdHeader for identifying dialogues between two
     * SIP applications.
     *
     * @return new CallId unique within the SIP Stack.
     */
    public CallIdHeader getNewCallId() {
        String callId = SIPUtils.generateCallIdentifier
                (this.getSipStack().getIPAddress());
        CallIdHeader callid = new  CallIdHeader();
        callid.setCallId(callId);
        return callid;

    }

    /**
     * Once an application wants to a send a new request it must first request
     * a new client transaction identifier. This method is called by an
     * application to create the client transaction befores it sends the Request
     * via the SipProvider on that transaction. This methods returns a new
     * unique client transaction identifier that can be passed to the stateful
     * sendRequest method on the SipProvider and the sendAck/sendBye
     * methods on the Dialog in order to send a request.
     *
     * @param request the new Request message that is to handled
     * statefully by the Provider.
     * @return a new unique client transation identifier
     * @see ClientTransaction
     * @since v1.1
     */
    public ClientTransaction getNewClientTransaction(Request request)
            throws TransactionUnavailableException {
        if (request == null) {
            throw new NullPointerException("null request");
        }

        if (request.getTransaction() != null) {
            throw new TransactionUnavailableException
                    ("Transaction already assigned to request");
        }

        if (request.getMethod().equals(Request.CANCEL)) {
            ClientTransaction ct = (ClientTransaction)
                sipStack.findCancelTransaction(request, false);

            if (ct != null) {
                ClientTransaction retval = sipStack.createClientTransaction
                        (ct.getMessageChannel());
                ((Transaction)retval).setOriginalRequest(request);
                ((Transaction)retval).addEventListener(this);
                sipStack.addTransaction((ClientTransaction)retval);
                ((ClientTransaction)retval).setDialog((Dialog)ct.getDialog());
                return retval;
            }

        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "could not find existing transaction for "
                + request.getFirstLine());
        }

        String dialogId = request.getDialogId(false);
        Dialog dialog = sipStack.getDialog(dialogId);
        // isDialog - is request sends inside dialog
        boolean isDialog = false;
        if (dialog != null) {
            int dialogState = dialog.getState();
            if (dialogState == Dialog.EARLY_STATE ||
                dialogState == Dialog.CONFIRMED_STATE ||
                dialogState == Dialog.COMPLETED_STATE) {
                isDialog = true;
            }
        }
        Enumeration it = sipStack.getRouter().getNextHops(request, isDialog);

        if (it == null || !it.hasMoreElements())  {
            // could not route the request as out of dialog.
            // maybe the user has no router or the router cannot resolve
            // the route.
            // If this is part of a dialog then use the route from the dialog
            if (dialog != null)   {
                try {
                    Hop hop = dialog.getNextHop();

                    if (hop != null) {
                        ClientTransaction ct = null;

                        ct = (ClientTransaction) sipStack
                            .createMessageChannel(hop);
                        
                        String branchId = SIPUtils.generateBranchId();

                        if (request.getTopmostVia() != null) {
                            request.getTopmostVia().setBranch(branchId);
                        } else {
                            // Find a message processor to assign this
                            // transaction to.
                            MessageProcessor messageProcessor =
                                    this.listeningPoint.messageProcessor[0];
                            ViaHeader via = messageProcessor.getViaHeader();
                            request.addHeader(via);
                        }

                        ct.setOriginalRequest(request);
                        ct.setBranch(branchId);
                        ct.setDialog(dialog);
                        ct.addEventListener(this);

                        return ct;
                    } // end if
                } catch (Exception ex) {
                    throw new TransactionUnavailableException(ex.getMessage());
                }
            } else {
                throw new TransactionUnavailableException("no route!");
            }
        } else {
            try {
                // An out of dialog route was found. Assign this to the
                // client transaction.
                while (it.hasMoreElements()) {
                    Hop hop = (Hop) it.nextElement();

                    ClientTransaction ct = null;

                    ct = (ClientTransaction) sipStack
                        .createMessageChannel(hop);

                    // ClientTransaction ct =
                    //   (ClientTransaction) sipStack.createMessageChannel(hop);

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180, "hop = " + hop + "ct " + ct);
                    }

                    if (ct == null) continue;

                    String branchId = SIPUtils.generateBranchId();

                    if (request.getTopmostVia() != null) {
                        request.getTopmostVia().setBranch(branchId);
                    } else {
                        // Find a message processor to assign
                        // this transaction to.
                        MessageProcessor messageProcessor =
                                listeningPoint.messageProcessor[0];
                        ViaHeader via = messageProcessor.getViaHeader();
                        request.addHeader(via);
                    }

                    ct.setOriginalRequest(request);
                    ct.setBranch(branchId);

                    if (sipStack.isDialogCreated(request.getMethod())) {
                        // create a new dialog to contain this transaction
                        // provided this is necessary.
                        // This could be a re-invite
                        // (noticed by Brad Templeton)

                        if (dialog != null) {
                            ct.setDialog(dialog);
                        } else {
                            sipStack.createDialog(ct);
                        }
                    } else {
                        ct.setDialog(dialog);
                    }

                    // The provider is the event listener for all transactions.
                    ct.addEventListener(this);
                    return (ClientTransaction) ct;
                } // end while()
            } catch (SipException ex) {
                throw new TransactionUnavailableException(ex.getMessage());
            }
        } // end else

        throw new TransactionUnavailableException
                ("Could not create transaction - could not resolve next hop! ");
    }

    /**
     * An application has the responsibility of deciding to respond to a
     * Request that does not match an existing server transaction. The method
     * is called by an application that decides to respond to an unmatched
     * Request statefully. This methods return a new unique server transaction
     * identifier that can be passed to the stateful sendResponse methods in
     * order to respond to the request.
     *
     * @param request the initial Request message that the doesn't
     * match an existing
     * transaction that the application decides to handle statefully.
     * @return a new unique server transation identifier
     * @throws TransactionAlreadyExistsException if a transaction already exists
     * that is already handling this Request. This may happen if the application
     * gets retransmits of the same request before the initial transaction is
     * allocated.
     * @see ServerTransaction
     * @since v1.1
     */
    public ServerTransaction
            getNewServerTransaction(Request request)
            throws TransactionAlreadyExistsException,
            TransactionUnavailableException {

        try {
            ServerTransaction transaction = null;
            Request sipRequest = (Request) request;
            if (sipStack.isDialogCreated(sipRequest.getMethod())) {
                if (sipStack.findTransaction((Request)request, true) != null)
                    throw new TransactionAlreadyExistsException
                            ("server transaction already exists!");
                transaction = (ServerTransaction) this.currentTransaction;
                if (transaction == null)
                    throw new  TransactionUnavailableException
                            ("Transaction not available");
                if (!transaction
                        .isMessagePartOfTransaction((Request) request)) {
                    throw new TransactionUnavailableException
                            ("Request Mismatch");
                }
                transaction.setOriginalRequest(sipRequest);
                try {
                    sipStack.addTransaction(transaction);
                } catch (IOException ex) {
                    throw new TransactionUnavailableException
                            ("Error sending provisional response");
                }
                equipADialogForTransaction(transaction, sipRequest);
            } else {
                transaction = (ServerTransaction)
                sipStack.findTransaction((Request) request, true);
                if (transaction != null)
                    throw new TransactionAlreadyExistsException
                            ("Transaction exists! ");
                transaction = (ServerTransaction) this.currentTransaction;
                if (transaction == null)
                    throw new TransactionUnavailableException
                            ("Transaction not available!");
                if (!transaction
                        .isMessagePartOfTransaction((Request) request))
                    throw new TransactionUnavailableException
                            ("Request Mismatch");
                transaction.setOriginalRequest(sipRequest);
                // Map the transaction.
                try {
                    sipStack.addTransaction(transaction);
                } catch (IOException ex) {
                    throw new TransactionUnavailableException
                            ("Could not send back provisional response!");
                }
                String dialogId =   sipRequest.getDialogId(true);
                Dialog dialog = sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                }
            }
            return transaction;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Find or create a dialog with the dialog-id obtained from the request.
     * Initializing the dialog's route information.
     * Bind the dialog and the transaction to each other.
     * @param transaction trasaction for the request
     * @param sipRequest request whose tags are the source of dialog-ID
     *                   and route information
     */
    public void equipADialogForTransaction(
            ServerTransaction transaction,
            Request sipRequest) {
        // So I can handle timeouts.
        // IMPL_NOTE: do we need it here, or in the calling code?
        transaction.addEventListener(this);

        String dialogId = sipRequest.getDialogId(true);
        Dialog dialog = sipStack.getDialog(dialogId);

        if (dialog == null) {
            dialog = sipStack.createDialog(transaction);
        }

        dialog.setStack(this.sipStack);
        dialog.addRoute(sipRequest);

        if (dialog.getRemoteTag() != null &&
                dialog.getLocalTag() != null)  {
            this.sipStack.putDialog(dialog);
        }

        transaction.setDialog(dialog);
    }

    /**
     * Returns the SipStack that this SipProvider is attached to. A SipProvider
     * can only be attached to a single SipStack object which belongs to
     * the same SIP stack as the SipProvider.
     *
     * @see SipStack
     * @return the attached SipStack.
     */
    public SipStack getSipStack() {
        return  this.sipStack;
    }

    /**
     * Removes the SipListener from this SipProvider. This method returns
     * silently if the <var>sipListener</var> argument is not registered
     * with the SipProvider.
     *
     * @param sipListener - the SipListener to be removed from this
     * SipProvider
     */
    public void removeSipListener(SipListener sipListener) {
        if (sipListener == this.sipListener) {
            this.sipListener = null;
        }
    }

    /**
     * Sends specified Request and returns void i.e.
     * no transaction record is associated with this action. This method
     * implies that the application is functioning statelessly specific to this
     * Request, hence the underlying SipProvider acts statelessly.
     * <p>
     * Once the Request message has been passed to this method, the SipProvider
     * will forget about this Request. No transaction semantics will be
     * associated with the Request and no retranmissions will occur on the
     * Request by the SipProvider, if these semantics are required it is the
     * responsibility of the application not the JAIN SIP Stack.
     * <ul>
     * <li>Stateless Proxy - A stateless proxy simply forwards every request
     *  it receives downstream and discards information about the request
     *  message once the message has been forwarded. A stateless proxy does not
     *  have any notion of a transaction.
     * </ul>
     *
     * @since v1.1
     * @see Request
     * @param request - the Request message to send statelessly
     * @throws SipException if implementation cannot send request for any reason
     */
    public void sendRequest(Request request) throws SipException {
        // request sends out of dialog
        Enumeration it = sipStack.getRouter().getNextHops(request, false);
        if (it == null || !it.hasMoreElements()) {
            throw new SipException("could not determine next hop!",
                SipException.GENERAL_ERROR);
        }

        // Will slow down the implementation because it involves
        // a search to see if a transaction exists.
        // Just to double check adding some assertion
        // checking under debug.
        Transaction tr = sipStack.findTransaction(request, false);
        if (tr != null) {
            throw new SipException("Cannot send: stateless Transaction found!",
                SipException.GENERAL_ERROR);
        }

        while (it.hasMoreElements()) {
            Hop nextHop = (Hop) it.nextElement();

            Request sipRequest = request;
            String bid = sipRequest.getTransactionId();
            ViaHeader via = sipRequest.getTopmostVia();
            via.setBranch(bid);
            Request newRequest;

            // Do not create a transaction for this request. If it has
            // Mutliple route headers then take the first one off the
            // list and copy into the request URI.
            if (sipRequest.getHeader(Header.ROUTE) != null) {
                newRequest = (Request) sipRequest.clone();
                Enumeration rl =
                        newRequest.getHeaders(Header.ROUTE);
                RouteHeader route = (RouteHeader) rl.nextElement();
                newRequest.setRequestURI(route.getAddress().getURI());
                sipRequest.removeHeader(Header.ROUTE, true);
            } else {
                newRequest = sipRequest;
            }

            MessageChannel messageChannel =
                    sipStack.createRawMessageChannel(nextHop);
            try {
                if (messageChannel != null) {
                    messageChannel.sendMessage((Message)newRequest);
                    return;
                } else {
                    throw new SipException("Could not forward request.",
                        SipException.GENERAL_ERROR);
                }

            } catch (IOException ex) {
                continue;
            }
        }
    }

    /**
     * Sends specified {@link Response} and returns void i.e.
     * no transaction record is associated with this action. This method implies
     * that the application is functioning as either a stateless proxy or a
     * stateless User Agent Server.
     * <ul>
     *  <li> Stateless proxy - A stateless proxy simply forwards every response
     *  it receives upstream and discards information about the response message
     *  once the message has been forwarded. A stateless proxy does not
     *  have any notion of a transaction.
     *  <li>Stateless User Agent Server - A stateless UAS does not maintain
     *  transaction state. It replies to requests normally, but discards
     *  any state that would ordinarily be retained by a UAS after a response
     *  has been sent.  If a stateless UAS receives a retransmission of a
     *  request, it regenerates the response and resends it, just as if it
     *  were replying to the first instance of the request. A UAS cannot be
     *  stateless unless the request processing for that method would always
     *  result in the same response if the requests are identical. Stateless
     *  UASs do not use a transaction layer; they receive requests directly
     *  from the transport layer and send responses directly to the transport
     *  layer.
     * </ul>
     *
     * @see Response
     * @param sipResponse the Response to send statelessly.
     * @throws IOException if I/O error occured
     * @throws SipException if implementation cannot send response for
     * any other reason
     * @see Response
     * @since v1.1
     */
    public void sendResponse(Response sipResponse)
            throws IOException, SipException {
        ViaHeader via = sipResponse.getTopmostVia();
        if (via == null) {
            throw new SipException("No via header in response!",
                SipException.INVALID_MESSAGE);
        }

        int    port = via.getPort();
        String transport = via.getTransport();
        // check to see if Via has "received paramaeter". If so
        // set the host to the via parameter. Else set it to the
        // Via host.
        String host = via.getReceived();

        if (host == null) {
            host = via.getHost();
        }

        if (port == -1) {
            port = 5060; // IMPL_NOTE: move to SIPConstants
        }

        Hop hop = new Hop(host + ":" + port + "/" + transport);
        MessageChannel messageChannel = sipStack.createRawMessageChannel(hop);
        messageChannel.sendMessage(sipResponse);
    }

    /**
     * This method sets the listening point of the SipProvider.
     * A SipProvider can only have a single listening point at any
     * specific time. This method returns
     * silently if the same <var>listeningPoint</var> argument is re-set
     * on the SipProvider.
     * <p>
     * JAIN SIP supports recieving messages from
     * any port and interface that a server listens on for UDP, on that same
     * port and interface for TCP in case a message may need to be sent
     * using TCP, rather than UDP, if it is too large. In order to satisfy this
     * functionality an application must create two SipProviders and set
     * identical listeningPoints except for transport on each SipProvder.
     * <p>
     * Multiple SipProviders are prohibited to listen on the same
     * listening point.
     *
     * @param listeningPoint of this SipProvider
     * @see ListeningPoint
     * @since v1.1
     */
    public void setListeningPoint(ListeningPoint listeningPoint)  {
        if (listeningPoint == null)
            throw new NullPointerException("Null listening point");
        ListeningPoint lp = (ListeningPoint) listeningPoint;
        lp.sipProviderImpl = this;
        this.listeningPoint = (ListeningPoint) listeningPoint;

    }

    /**
     * Invoked when an error has ocurred with a transaction.
     * Propagate up to the listeners.
     *
     * @param transactionErrorEvent Error event.
     */
    public void transactionErrorEvent
            (SIPTransactionErrorEvent transactionErrorEvent) {
        Transaction transaction =
                (Transaction) transactionErrorEvent.getSource();

        if (transactionErrorEvent.getErrorID() ==
                SIPTransactionErrorEvent.TRANSPORT_ERROR) {

            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "TransportError occured on " + transaction);
            }

            //  handle Transport error as timeout.
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            TimeoutEvent ev = null;

            if (errorObject instanceof ServerTransaction) {
                ev = new TimeoutEvent(this, (ServerTransaction)
                errorObject);
            } else {
                ev = new TimeoutEvent(this, (ClientTransaction)
                errorObject,
                        timeout);
            }
            this.handleEvent(ev, (Transaction) errorObject);

        } else {
            //  This is a timeout event.
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            TimeoutEvent ev = null;

            if (errorObject instanceof ServerTransaction) {
                ev = new TimeoutEvent(this, (ServerTransaction)
                errorObject);
            } else {
                ev = new TimeoutEvent(this, (ClientTransaction)
                errorObject,
                        timeout);
            }
            this.handleEvent(ev, (Transaction) errorObject);

        }
    }
}
