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
package gov.nist.siplite.stack;

import gov.nist.microedition.sip.SipClientConnectionImpl;
import gov.nist.siplite.message.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;
import java.util.*;
import java.io.IOException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Adds a transaction layer to the {@link SIPMessageStack} class. This
 * is done by
 * replacing the normal MessageChannels returned by the base class with
 * transaction-aware MessageChannels that encapsulate the original channels
 * and handle the transaction state machine, retransmissions, etc.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1
 *
 */
public abstract class SIPTransactionStack
        extends SIPMessageStack implements SIPTransactionEventListener {

    /**
     * Number of milliseconds between timer ticks (500).
     */
    public static final int BASE_TIMER_INTERVAL = 500;

    /** Collection of current client transactions. */
    private Vector clientTransactions;
    /** Collection or current server transactions. */
    private Vector serverTransactions;
    /** Table of dialogs. */
    private Hashtable dialogTable;

    /** Max number of server transactions concurrent. */
    protected int transactionTableSize;

    /**
     * Retransmission filter - indicates the stack will retransmit
     * 200 OK for invite transactions.
     */
    protected boolean retransmissionFilter;

    /** A set of methods that result in dialog creations. */
    protected Hashtable dialogCreatingMethods;

    /** Default constructor. */
    protected SIPTransactionStack() {
        super();
        this.transactionTableSize = -1;
        // a set of methods that result in dialog creation.
        this.dialogCreatingMethods = new Hashtable();
        // Standard set of methods that create dialogs.
        this.dialogCreatingMethods.put(Request.REFER, "");
        this.dialogCreatingMethods.put(Request.INVITE, "");
        this.dialogCreatingMethods.put(Request.SUBSCRIBE, "");
        // Notify may or may not create a dialog. This is handled in
        // the code.
        // this.dialogCreatingMethods.add(Request.NOTIFY);
        // this.dialogCreatingMethods.put(Request.MESSAGE, "");
        // Create the transaction collections
        clientTransactions = new Vector();
        serverTransactions = new Vector();
        // Dialog dable.
        this.dialogTable = new Hashtable();


        // Start the timer event thread.
        // System.out.println("Starting timeout");
        new Thread(new TransactionScanner()).start();

    }

    /**
     * Prints the Dialog creating methods.
     */
    private void printDialogCreatingMethods() {
        System.out.println("PRINTING DIALOGCREATINGMETHODS HASHTABLE");
        Enumeration e = dialogCreatingMethods.keys();
        while (e.hasMoreElements()) {
            System.out.println(e.nextElement());
        }
        System.out.println("DIALOGCREATINGMETHODS HASHTABLE PRINTED");
    }

    /**
     * Returns true if extension is supported.
     * @param method the name of the method used for create
     * @return true if extension is supported and false otherwise.
     */
    public boolean isDialogCreated(String method) {
        // printDialogCreatingMethods();
        // System.out.println("CHECKING IF DIALOG HAS BEEN CREATED"
        // + " FOR THE FOLLOWING METHOD"
        // + method.toUpperCase());
        return dialogCreatingMethods.containsKey(method.toUpperCase());
    }

    /**
     * Returns true if method can change dialog state.
     * @param method the name of the method used for create
     * @return true if extension is supported and false otherwise.
     */
    public boolean allowDialogStateChange(String method) {
        return dialogCreatingMethods.containsKey(method.toUpperCase()) ||
            method.equalsIgnoreCase(Request.BYE) ||
            method.equalsIgnoreCase(Request.NOTIFY);
    }

    /**
     * Adds an extension method.
     * @param extensionMethod -- extension method to support for dialog
     * creation
     */
    public void addExtensionMethod(String extensionMethod) {
        if (! extensionMethod.equals(Request.NOTIFY)) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "NOTIFY Supported Natively");
            }
        } else {
            this.dialogCreatingMethods.put(extensionMethod, "");
        }
    }

    /**
     * Puts a dialog into the dialog table.
     * @param dialog -- dialog to put into the dialog table.
     */
    public void putDialog(Dialog dialog) {
        String dialogId = dialog.getDialogId();

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "putDialog dialogId=" + dialogId);
        }

        // if (this.getDefaultRouteHeader() != null)
        // dialog.addRoute(this.getDefaultRouteHeader(), false);
        dialog.setStack(this);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            // new Exception().printStackTrace();
        }

        synchronized (dialogTable) {
            dialogTable.put(dialogId, dialog);
        }

    }

    /**
     * Creates a new dialog for requested transaction.
     * @param transaction the requested transaction
     * @return the new Dialog  object
     */
    public synchronized Dialog
            createDialog(Transaction transaction) {
        Request sipRequest = transaction.getOriginalRequest();
        Dialog retval = new Dialog(transaction);

        return retval;
    }

    /**
     * Returns the dialog for a given dialog ID. If compatibility is
     * enabled then we do not assume the presence of tags and hence
     * need to add a flag to indicate whether this is a server or
     * client transaction.
     * @param dialogId is the dialog id to check.
     * @return the Dialog object for the requested id
     */
    public Dialog getDialog(String dialogId) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Getting dialog for " + dialogId);
        }

        synchronized (dialogTable) {
            return (Dialog)dialogTable.get(dialogId);
        }
    }

    /**
     * Finds a matching client SUBSCRIBE to the incoming notify.
     * NOTIFY requests are matched to such SUBSCRIBE requests if they
     * contain the same "Call-ID", a "ToHeader" header "tag" parameter which
     * matches the "FromHeader" header "tag" parameter of the SUBSCRIBE, and the
     * same "Event" header field. Rules for comparisons of the "Event"
     * headers are described in section 7.2.1. If a matching NOTIFY request
     * contains a "Subscription-State" of "active" or "pending", it creates
     * a new subscription and a new dialog (unless they have already been
     * created by a matching response, as described above).
     *
     * @param notifyMessage the request to be matched
     * @return the new client transaction object
     */
    public ClientTransaction findSubscribeTransaction
            (Request notifyMessage) {
        synchronized (clientTransactions) {
            Enumeration it = clientTransactions.elements();
            String thisToHeaderTag = notifyMessage.getTo().getTag();
            if (thisToHeaderTag == null)
                return null;
            EventHeader eventHdr =
                    (EventHeader)notifyMessage.getHeader(Header.EVENT);
            if (eventHdr == null)
                return null;
            while (it.hasMoreElements()) {
                ClientTransaction ct =
                        (ClientTransaction)it.nextElement();
                Request sipRequest = ct.getOriginalRequest();
                String fromTag = sipRequest.getFromHeader().getTag();
                EventHeader hisEvent =
                        (EventHeader)sipRequest.getHeader(Header.EVENT);
                // Event header is mandatory but some slopply clients
                // dont include it.
                if (hisEvent == null) continue;
                if (sipRequest.getMethod().equals(Request.SUBSCRIBE) &&
                        Utils.equalsIgnoreCase(fromTag, thisToHeaderTag) &&
                        hisEvent != null && eventHdr.match(hisEvent) &&
                        Utils.equalsIgnoreCase
                        (notifyMessage.getCallId().getCallId(),
                        sipRequest.getCallId().getCallId()))
                    return ct;
            }

        }
        return null;
    }

    /**
     * Finds the transaction corresponding to a given request.
     * @param sipMessage request for which to retrieve the transaction.
     * @param isServer search the server transaction table if true.
     * @return the transaction object corresponding to the request or null
     * if no such mapping exists.
     */
    public Transaction
            findTransaction(Message sipMessage, boolean isServer) {

        if (isServer) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "searching server transaction for "
                    + sipMessage + " size = " +
                    this.serverTransactions.size());
            }

            synchronized (this.serverTransactions) {
                Enumeration it = serverTransactions.elements();
                while (it.hasMoreElements()) {
                    ServerTransaction sipServerTransaction =
                            (ServerTransaction)it.nextElement();
                    if (sipServerTransaction
                            .isMessagePartOfTransaction(sipMessage))
                        return sipServerTransaction;
                }
            }
        } else {
            synchronized (this.clientTransactions) {
                Enumeration it = clientTransactions.elements();
                while (it.hasMoreElements()) {
                    ClientTransaction clientTransaction =
                            (ClientTransaction)it.nextElement();
                    if (clientTransaction
                            .isMessagePartOfTransaction(sipMessage))
                        return clientTransaction;
                }
            }

        }
        return null;


    }

    /**
     * Gets the transaction to cancel. Search the server transaction
     * table for a transaction that matches the given transaction.
     * @param cancelRequest the request to be found
     * @param isServer true if this is a server request
     * @return the transaction object requested
     */
    public Transaction
            findCancelTransaction(Request cancelRequest, boolean isServer) {

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "findCancelTransaction request = \n"
                + cancelRequest +
                "\nfindCancelRequest isServer = " + isServer);
        }

        if (isServer) {
            synchronized (this.serverTransactions) {
                Enumeration li = this.serverTransactions.elements();
                while (li.hasMoreElements()) {
                    Transaction transaction =
                            (Transaction)li.nextElement();
                    Request sipRequest =
                            (Request) (transaction.getRequest());
                    ServerTransaction sipServerTransaction =
                            (ServerTransaction) transaction;
                    if (sipServerTransaction.doesCancelMatchTransaction
                            (cancelRequest))
                        return sipServerTransaction;
                }
            }
        } else {
            synchronized (this.clientTransactions) {
                Enumeration li = this.clientTransactions.elements();
                while (li.hasMoreElements()) {
                    Transaction transaction = (Transaction)li.nextElement();
                    Request sipRequest =
                            (Request) (transaction.getRequest());

                    ClientTransaction sipClientTransaction =
                            (ClientTransaction) transaction;
                    if (sipClientTransaction.doesCancelMatchTransaction
                            (cancelRequest))
                        return sipClientTransaction;

                }
            }
        }
        return null;
    }

    /**
     * Construcor for the stack. Registers the request and response
     * factories for the stack.
     * @param messageFactory User-implemented factory for processing
     * messages.
     */
    protected SIPTransactionStack(SIPStackMessageFactory messageFactory) {
        this();
        super.sipMessageFactory = messageFactory;
    }

    /**
     * Thread used to throw timer events for all transactions.
     */
    class TransactionScanner implements Runnable {
        /**
         * Main transaction scanner processing loop.
         */
        public void run() {

            // Iterator through all transactions
            Enumeration transactionIterator;
            // One transaction in the set
            Transaction nextTransaction;

            // Loop while this stack is running
            while (isAlive()) {
                try {
                    // Sleep for one timer "tick"
                    Thread.sleep(BASE_TIMER_INTERVAL);

                    // System.out.println("clientTransactionTable size " +
                    // clientTransactions.size());
                    // System.out.println("serverTransactionTable size " +
                    // serverTransactions.size());
                    // Check all client transactions

                    Vector fireList = new Vector();
                    Vector removeList = new Vector();

                    // Check all server transactions
                    synchronized (serverTransactions) {
                        transactionIterator =
                                serverTransactions.elements();
                        while (transactionIterator.hasMoreElements()) {

                            nextTransaction =
                                    (Transaction)
                                    transactionIterator.nextElement();

                            // If the transaction has terminated,
                            if (nextTransaction.isTerminated()) {
                                // Keep the transaction hanging around
                                // to catch the incoming ACK.
                                if (((ServerTransaction)nextTransaction).
                                        collectionTime == 0) {
                                    // Remove it from the set
                                    if (Logging.REPORT_LEVEL <=
                                            Logging.INFORMATION) {
                                        Logging.report(Logging.INFORMATION,
                                            LogChannels.LC_JSR180,
                                            "removing" + nextTransaction);
                                    }
                                    removeList.addElement(nextTransaction);
                                } else {
                                    ((ServerTransaction)nextTransaction).
                                            collectionTime --;
                                }
                                // If this transaction has not
                                // terminated,
                            } else {
                                // Add to the fire list -- needs to be moved
                                // outside the synchronized block to prevent
                                // deadlock.
                                fireList.addElement(nextTransaction);

                            }

                        }
                        for (int j = 0; j < removeList.size(); j++) {
                            serverTransactions.removeElement
                                    (removeList.elementAt(j));
                        }
                    }

                    removeList = new Vector();



                    synchronized (clientTransactions) {
                        transactionIterator = clientTransactions.elements();
                        while (transactionIterator.hasMoreElements()) {

                            nextTransaction = (Transaction)
                            transactionIterator.nextElement();

                            // If the transaction has terminated,
                            // and SipClientConnection instance,
                            // associated with it has TERMINATED state
                            boolean removeTransaction = false;
                            if (nextTransaction.isTerminated()) {
                                SipClientConnectionImpl conn = 
                                    (SipClientConnectionImpl)
                                    nextTransaction.getApplicationData();
                                if (conn == null) { // no connection
                                    removeTransaction = true;
                                } else if (conn.getState() == 
                                    SipClientConnectionImpl.TERMINATED) {
                                    removeTransaction = true;
                                }
                            }
                            if (removeTransaction) {
                                // Remove it from the set
                                if (Logging.REPORT_LEVEL <=
                                       Logging.INFORMATION) {
                                   Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_JSR180,
                                       "Removing clientTransaction " +
                                       nextTransaction);
                                }

                                removeList.addElement(nextTransaction);

                                // If this transaction has not
                                // terminated,
                            } else {
                                // Add to the fire list -- needs to be moved
                                // outside the synchronized block to prevent
                                // deadlock.
                                fireList.addElement(nextTransaction);

                            }
                        }
                        for (int j = 0; j < removeList.size(); j++) {
                            clientTransactions.removeElement
                                    (removeList.elementAt(j));
                        }
                    }
                    removeList = new Vector();

                    synchronized (dialogTable) {
                        Enumeration values = dialogTable.elements();
                        while (values.hasMoreElements()) {
                            Dialog d = (Dialog) values.nextElement();
                            // System.out.println("dialogState = " +
                            // d.getState() +
                            // " isServer = " + d.isServer());
                            if (d.getState() ==
                                    Dialog.TERMINATED_STATE) {
                                if (Logging.REPORT_LEVEL <=
                                        Logging.INFORMATION) {
                                    String dialogId =
                                            d.getDialogId();
                                    Logging.report(Logging.INFORMATION,
                                        LogChannels.LC_JSR180,
                                        "Removing Dialog " +
                                        dialogId);
                                }

                                removeList.addElement(d);
                            }

                            if (d.isServer() && (! d.ackSeen) &&
                                    d.isInviteDialog()) {
                                Transaction transaction =
                                        d.getLastTransaction();
                                // If stack is managing the transaction
                                // then retransmit the last response.
                                if (transaction.getState() ==
				    Transaction.TERMINATED_STATE
				    && transaction instanceof
				    ServerTransaction
				    && ((ServerTransaction) transaction)
				    .isMapped) {
                                    Response response =
					transaction.getLastResponse();
                                    // Retransmit to 200 until ack received.
                                    if (response.getStatusCode() == 200) {
                                        try {
                                            if (d.toRetransmitFinalResponse())
                                                transaction
                                                    .sendMessage(response);
                                        } catch (IOException ex) {
                                            /* Will eventully time out */
                                            d.setState(Dialog.TERMINATED_STATE);
                                            if (response.getErrorListener() != 
                                                    null) {
                                                response.getErrorListener().
                                                  notifyError("Failed to " +
                                                  "retransmit the following " +
                                                  "response: " + response);
                                            }
                                        } finally {
                                            // Need to fire the timer so
                                            // transaction will eventually
                                            // time out whether or not
                                            // the IOException occurs
                                            fireList.addElement(transaction);
                                        }
                                    }
                                }
                            }

                        }
                        for (int j = 0; j < removeList.size(); j++) {
                            Dialog d =
                                    (Dialog)removeList.elementAt(j);
                            dialogTable.remove(d.getDialogId());
                        }
                    }

                    for (int i = 0; i < fireList.size(); i++) {
                        nextTransaction =
                                (Transaction) fireList.elementAt(i);
                        nextTransaction.fireTimer();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    // Ignore
                }

            }

        }
    }

    /**
     * Creates or terminates a dialog if a subscription matching the given
     * NOTIFY request exists. To create a dialog a list of transactions
     * is searched for the matching transaction and then the corresponding
     * SipClientConnection is notified. To terminate a dialog a list of
     * dialogs is searched for the existing subscription that matches
     * the given NOTIFY request.
     * @param requestReceived NOTIFY request that may create or terminate
     * a dialog.
     */
    private void createOrTerminateDialog(Request requestReceived) {
        // System.out.println(">>> NOTIFY: Scanning dialogs...");

        synchronized (dialogTable) {
            Enumeration e = dialogTable.elements();

            while (e.hasMoreElements()) {
                Dialog nextDialog = (Dialog)e.nextElement();
                Subscription s = nextDialog.subscriptionList.
                    getMatchingSubscription(requestReceived);

                if (s != null) {
                    SubscriptionStateHeader ssh = (SubscriptionStateHeader)
                        requestReceived.getHeader(Header.SUBSCRIPTION_STATE);

                    if (ssh != null && ssh.isTerminated()) {
                        nextDialog.subscriptionList.removeSubscription(s);
                    } else {
                        nextDialog.setState(Dialog.CONFIRMED_STATE);
                    }

                    return;
                }
            }
        }

        // System.out.println(">>> NOTIFY: Scanning transactions...");

        // Iterator through all client transactions
        Enumeration transactionIterator;
        ClientTransaction currClientTransaction = null;

        // Loop through all client transactions
        synchronized (clientTransactions) {
            transactionIterator = clientTransactions.elements();
            currClientTransaction = null;

            String receivedToTag = requestReceived.getToTag();
            CallIdHeader receivedCid = requestReceived.getCallId();
            EventHeader receivedEvent = (EventHeader)
                requestReceived.getHeader(Header.EVENT);

            while (transactionIterator.hasMoreElements()) {
                currClientTransaction =
                    (ClientTransaction)transactionIterator.nextElement();
                Request request = currClientTransaction.getRequest();
                String method = request.getMethod();
                String fromTag = request.getFromHeaderTag();
                CallIdHeader cid = request.getCallId();
                EventHeader hEvent = (EventHeader)
                    request.getHeader(Header.EVENT);
                boolean isSameEvent = (hEvent != null) &&
                    hEvent.match(receivedEvent);

                if (((method.equals(Request.SUBSCRIBE) && isSameEvent) ||
                        method.equals(Request.REFER)) &&
                    fromTag != null && fromTag.equals(receivedToTag) &&
                        cid != null && cid.equals(receivedCid)) {

                    SipClientConnectionImpl sipClientConnection =
                        (SipClientConnectionImpl)
                            currClientTransaction.getApplicationData();

                    if (sipClientConnection != null) {
                        sipClientConnection.handleMatchingNotify(
                            requestReceived);
                    } else {
                        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                            Logging.report(Logging.WARNING,
                            LogChannels.LC_JSR180,
                            "SIPTransactionStack.createOrTerminateDialog(): " +
                            "Cannot find SCC for the given NOTIFY.");
                        }
                    }

                    break;
                }
            }
        }
    }

    /**
     * Handles a new SIP request.
     * It finds a server transaction to handle
     * this message. If none exists, it creates a new transaction.
     * @param requestReceived Request to handle.
     * @param requestMessageChannel Channel that received message.
     * @return A server transaction.
     */
    protected SIPServerRequestInterface
            newSIPServerRequest(Request requestReceived,
            MessageChannel requestMessageChannel) {

        try {
            // Iterator through all server transactions
            Enumeration transactionIterator;
            // Next transaction in the set
            ServerTransaction nextTransaction;
            // Transaction to handle this request
            ServerTransaction currentTransaction = null;

            if (requestReceived.getMethod().equals(Request.NOTIFY)) {
                createOrTerminateDialog(requestReceived);
            }

            // Loop through all server transactions
            synchronized (serverTransactions) {
                transactionIterator = serverTransactions.elements();
                currentTransaction = null;
                while (transactionIterator.hasMoreElements() &&
                        currentTransaction == null) {

                    nextTransaction =
                        (ServerTransaction)transactionIterator.nextElement();

                    // If this transaction should handle this request,
                    if (!nextTransaction.isTerminated() &&
                            nextTransaction.isMessagePartOfTransaction(
                                requestReceived)) {
                        // Mark this transaction as the one
                        // to handle this message
                        currentTransaction = nextTransaction;
                    }
                }

                // If no transaction exists to handle this message
                if (currentTransaction == null) {
                    currentTransaction =
                            createServerTransaction(requestMessageChannel);
                    currentTransaction.setOriginalRequest(requestReceived);
                    
                    if (!isDialogCreated(requestReceived.getMethod())) {
                        // Server transactions must always be added
                        // to the corresponding vector.
                        serverTransactions.addElement(currentTransaction);
                        currentTransaction.isMapped = true;
                    } else {
                        // Create the transaction but dont map it.
                        String dialogId = requestReceived.getDialogId(true);
                        Dialog dialog = getDialog(dialogId);
                        
                        if (dialog == null) {
                            // This is a dialog creating request
                            dialog = createDialog(currentTransaction);
        
                            dialog.setStack(this);
                            dialog.addRoute(requestReceived);
                            if (dialog.getRemoteTag() != null &&
                                dialog.getLocalTag() != null)  {
                                putDialog(dialog);
                                
                                currentTransaction.setDialog(dialog);
                            }
                        } else {
                            // This is a dialog creating request that is
                            // part of an existing dialog
                            // (eg. re-Invite). Re-invites get a non null
                            // server transaction Id (unlike the original
                            // invite).
                            if (requestReceived.getCSeqHeader().getSequenceNumber()
                                    > dialog.getRemoteSequenceNumber()) {
                                try {
                                    currentTransaction.map();
                                } catch (IOException ex) {
                                    /* Ignore */
                                }
                            }
                        }

                        serverTransactions.addElement(currentTransaction);
                        currentTransaction.toListener = true;
                    }
                }
                // attach to request
                requestReceived.setTransaction(currentTransaction);

                // Set ths transaction's encapsulated request
                // interface from the superclass
                currentTransaction.setRequestInterface
                        (super.newSIPServerRequest
                        (requestReceived, currentTransaction));

                return currentTransaction;
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * Handles a new SIP response.
     * It finds a client transaction to handle
     * this message. If none exists, it sends the message directly to the
     * superclass.
     * @param responseReceived Response to handle.
     * @param responseMessageChannel Channel that received message.
     * @return A client transaction.
     */
    SIPServerResponseInterface
            newSIPServerResponse(Response responseReceived,
            MessageChannel responseMessageChannel) {
        // System.out.println("response = " + responseReceived.encode());
        // Iterator through all client transactions
        Enumeration transactionIterator;
        // Next transaction in the set
        ClientTransaction nextTransaction;
        // Transaction to handle this request
        ClientTransaction currentTransaction;

        // Loop through all client transactions
        synchronized (clientTransactions) {
            transactionIterator = clientTransactions.elements();
            currentTransaction = null;
            int i = -1;
            while (transactionIterator.hasMoreElements() &&
                    currentTransaction == null) {
                i++;
                nextTransaction =
                        (ClientTransaction)transactionIterator.nextElement();
                // If this transaction should handle this request,
                if (nextTransaction.isMessageTransOrMult(responseReceived)) {
                    if (nextTransaction.isMultipleResponse(responseReceived)) {
                        // RFC 3261, 13.2.2.4:
                        // Multiple 2xx responses may arrive at the UAC for
                        // a single INVITE request due to a forking proxy.
                        // create a new client transaction
                        currentTransaction =
                            nextTransaction.cloneWithNewLastResponse
                                (responseReceived);
                        currentTransaction.setState
                            (Transaction.PROCEEDING_STATE);
                        currentTransaction.setApplicationData
                            (nextTransaction.getApplicationData());
                        Dialog dialog = new Dialog(currentTransaction);
                        dialog.setDialogId(responseReceived.getDialogId(false));
                        dialog.setRemoteTag(responseReceived.getToTag());
                        dialog.setStack(this);
                        putDialog(dialog);
                        currentTransaction.setDialog(dialog);
                        // change from old to new transaction
                        clientTransactions.setElementAt(currentTransaction, i);
                    } else { // not multiple response
                        // Mark this transaction as the one to
                        // handle this message
                        currentTransaction = nextTransaction;
                    }

                }
            }
        }
        // If no transaction exists to handle this message,
        if (currentTransaction == null) {
            // Pass the message directly to the TU
            return super.newSIPServerResponse
                    (responseReceived, responseMessageChannel);
        }
        // Set ths transaction's encapsulated response interface
        // from the superclass
        currentTransaction.setResponseInterface(super.newSIPServerResponse
                (responseReceived,
                currentTransaction));
        return currentTransaction;
    }

    /**
     * Creates a client transaction to handle a new request.
     * Gets the real
     * message channel from the superclass, and then creates a new client
     * transaction wrapped around this channel.
     * @param nextHop Hop to create a channel to contact.
     * @return the requested message channel
     */
    public MessageChannel createMessageChannel(Hop nextHop) {
        synchronized (clientTransactions) {
            // New client transaction to return
            Transaction returnChannel;

            // Create a new client transaction around the
            // superclass' message channel
            MessageChannel mc = super.createMessageChannel(nextHop);
            if (mc == null)
                return null;
            returnChannel =
                    createClientTransaction(mc);
            clientTransactions.addElement(returnChannel);
            ((ClientTransaction)returnChannel).setViaPort(nextHop.getPort());
            ((ClientTransaction)returnChannel).setViaHost(nextHop.getHost());
            return returnChannel;
        }
    }

    /**
     * Creates a client transaction from a raw channel.
     * @param rawChannel is the transport channel to encapsulate.
     * @return the requested message channel
     */
    public MessageChannel createMessageChannel
            (MessageChannel rawChannel) {
        synchronized (clientTransactions) {
            // New client transaction to return
            Transaction returnChannel =
                    createClientTransaction(rawChannel);
            clientTransactions.addElement(returnChannel);
            ((ClientTransaction)returnChannel).setViaPort
                    (rawChannel.getViaPort());
            ((ClientTransaction)returnChannel).setViaHost
                    (rawChannel.getHost());
            return returnChannel;
        }
    }

    /**
     * Creates a client transaction from a raw channel.
     * @param transaction the requested transaction
     * @return the requested message channel
     */
    public MessageChannel createMessageChannel
            (Transaction transaction) {
        synchronized (clientTransactions) {
            // New client transaction to return
            Transaction returnChannel =
                    createClientTransaction(transaction.getMessageChannel());
            clientTransactions.addElement(returnChannel);
            ((ClientTransaction)returnChannel).setViaPort
                    (transaction.getViaPort());
            ((ClientTransaction)returnChannel).setViaHost
                    (transaction.getViaHost());
            return returnChannel;
        }
    }


    /**
     * Creates a client transaction that encapsulates a MessageChannel.
     * Useful for implementations that want to subclass the standard
     * @param encapsulatedMessageChannel
     * Message channel of the transport layer.
     * @return the requested client transaction
     */
    public ClientTransaction
            createClientTransaction(MessageChannel
            encapsulatedMessageChannel) {

        return new ClientTransaction
                (this, encapsulatedMessageChannel);

    }

    /**
     * Creates a server transaction that encapsulates a MessageChannel.
     * Useful for implementations that want to subclass the standard
     * @param encapsulatedMessageChannel
     * Message channel of the transport layer.
     * @return the requested server transaction
     */
    public ServerTransaction
            createServerTransaction(MessageChannel encapsulatedMessageChannel) {

        return new ServerTransaction
                (this, encapsulatedMessageChannel);
    }

    /**
     * Creates a raw message channel. A raw message channel has no
     * transaction wrapper.
     * @param hop hop for which to create the raw message channel.
     * @return the requested message channel
     */
    public MessageChannel createRawMessageChannel(Hop hop) {
        return super.createMessageChannel(hop);
    }


    /**
     * Adds a new client transaction to the set of existing transactions.
     * @param clientTransaction -- client transaction to add to the set.
     */
    public void addTransaction
            (ClientTransaction clientTransaction) {
        synchronized (clientTransactions) {
            clientTransactions.addElement(clientTransaction);
        }
    }

    /**
     * Adds a new client transaction to the set of existing transactions.
     * @param serverTransaction -- server transaction to add to the set.
     */
    public void addTransaction
            (ServerTransaction serverTransaction) throws IOException {
        synchronized (serverTransactions) {
            this.serverTransactions.addElement(serverTransaction);
        }
    }
}
