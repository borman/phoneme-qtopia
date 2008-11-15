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

import gov.nist.siplite.parser.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;
import java.util.*;

import java.io.IOException;
import javax.microedition.sip.SipException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Abstract class to support both client and server transactions.
 * Provides an encapsulation of a message channel, handles timer events,
 * and creation of the Via header for a message.
 *
 * @version JAIN-SIP-1.1
 *
 */
public abstract class Transaction
        extends MessageChannel {
    /** Transaction timer interval. */
    protected static final int BASE_TIMER_INTERVAL =
            SIPTransactionStack.BASE_TIMER_INTERVAL;
    /** RTT Estimate. 500ms default. */
    protected static final int T1 = 500/BASE_TIMER_INTERVAL;
    /** 5 sec Maximum duration a message will remain in the network */
    protected static final int T4 = 5000/BASE_TIMER_INTERVAL;
    /**
     * The maximum retransmit interval for non-INVITE
     * requests and INVITE responses
     */
    protected static final int T2 = 4000/BASE_TIMER_INTERVAL;
    /** INVITE request retransmit interval, for UDP only */
    protected static final int TIMER_A = 1*T1;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_B = 64*T1;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_J = 64*T1;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_F = 64*T1;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_H = 64*T1;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_I = T4;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_K = T4;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_D = 32000/BASE_TIMER_INTERVAL;
    /** INVITE transaction timeout timer */
    protected static final int TIMER_C = 3*60*1000/BASE_TIMER_INTERVAL;
    /** Last response message. */
    protected Response lastResponse;
    /** Current SIP dialog. */
    protected Dialog dialog;
    /** Flag indicating an ACK was received. */
    protected boolean ackSeenFlag;
    /** Flag indicating listener waiting. */
    protected boolean toListener;
    /**
     * Initialized but no state assigned.
     */
    public static final int INITIAL_STATE = -1;
    /**
     * Trying state.
     */
    public static final int TRYING_STATE = 1;
    /**
     * CALLING State.
     */
    public static final int CALLING_STATE = 2;
    /**
     * Proceeding state.
     */
    public static final int PROCEEDING_STATE = 3;
    /**
     * Completed state.
     */
    public static final int COMPLETED_STATE = 4;
    /**
     * Confirmed state.
     */
    public static final int CONFIRMED_STATE = 5;
    /**
     * Terminated state.
     */
    public static final int TERMINATED_STATE = 6;
    /**
     * Maximum number of ticks between retransmissions.
     */
    protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT =
        4000/BASE_TIMER_INTERVAL;
    /** Parent stack for this transaction. */
    protected SIPTransactionStack parentStack;
    /** Original request that is being handled by this transaction. */
    private Request originalRequest;
    /**
     * Underlying channel being used to send messages for this
     * transaction.
     */
    protected MessageChannel encapsulatedChannel;
    /** Transaction branch ID. */
    private String branch;
    /** Current transaction state. */
    private int currentState;
    /** Number of ticks the retransmission timer was set to last. */
    private int retransmissionTimerLastTickCount;
    /** Number of ticks before the message is retransmitted. */
    private int retransmissionTimerTicksLeft;
    /** Number of ticks before the transaction times out. */
    private int timeoutTimerTicksLeft;
    /** List of event listeners for this transaction. */
    private Vector eventListeners;
    /** Flag to indcate that this has been cancelled. */
    protected boolean isCancelled;
    /**
     * Object representing the connection being held by the JSR180
     * Implementation It can be either a SipClientConnection in case
     * of a ClientTransaction or a SipConnectionNotifier in case of a
     *ServerTransaction.
     */
    protected Object applicationData;

    /**
     * Retrieves the application data.
     * @return Object representing the connection being held by the JSR180
     * Implementation. It can be either a SipClientConnection in case of a
     * ClientTransaction or a SipConnectionNotifier in case of a
     * ServerTransaction
     */
    public Object getApplicationData() {
        return applicationData;
    }

    /**
     * Sets the application data.
     * @param newApplicationData Object representing the connection being held
     * by the JSR180
     * Implementation. It can be either a SipClientConnection in case of a
     * ClientTransaction or a SipConnectionNotifier in case of a
     * ServerTransaction
     */
    public void setApplicationData(Object newApplicationData) {
        applicationData = newApplicationData;
    }

    /**
     * Gets the branch identifier.
     * @return the current branch id
     */
    public String getBranchId() {
        return branch;
    }

    /**
     * Transaction constructor.
     *
     * @param newParentStack Parent stack for this transaction.
     * @param newEncapsulatedChannel
     * Underlying channel for this transaction.
     */
    protected Transaction(
            SIPTransactionStack newParentStack,
            MessageChannel newEncapsulatedChannel) {
        parentStack = newParentStack;
        encapsulatedChannel = newEncapsulatedChannel;
        currentState = INITIAL_STATE;
        disableRetransmissionTimer();
        disableTimeoutTimer();
        eventListeners = new Vector();
        // Always add the parent stack as a listener
        // of this transaction
        addEventListener(newParentStack);
    }

    /**
     * Sets the request message that this transaction handles.
     *
     * @param newOriginalRequest Request being handled.
     */
    public void setOriginalRequest(
            Request newOriginalRequest) {
        // Branch value of topmost Via header
        String newBranch;

        originalRequest = newOriginalRequest;
        originalRequest.setTransaction(this);
        // If the message has an explicit branch value set,
        newBranch = ((ViaHeader)newOriginalRequest.getViaHeaders().
                getFirst()).getBranch();
        if (newBranch != null) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "Setting Branch id : " + newBranch);
            }

            // Override the default branch with the one
            // set by the message
            setBranch(newBranch);
        } else {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "Branch id is null!" + newOriginalRequest.encode());
            }
        }
    }

    /**
     * Gets the request being handled by this transaction.
     * @return Request being handled.
     */
    public Request getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Gets the original request but cast to a Request structure.
     * @return the request that generated this transaction.
     */
    public Request getRequest() {
        return (Request) originalRequest;
    }

    /**
     * Returns a flag stating whether this transaction is for an
     * INVITE request or not.
     * @return True if this is an INVITE request, false if not.
     */
    protected final boolean isInviteTransaction() {
        return originalRequest.getMethod().equals(Request.INVITE);
    }

    /**
     * Returns true if the transaction corresponds to a CANCEL message.
     * @return true if the transaciton is a CANCEL transaction.
     */
    protected final boolean isCancelTransaction() {
        return originalRequest.getMethod().equals(Request.CANCEL);
    }

    /**
     * Returns a flag that states if this is a BYE transaction.
     * @return true if the transaciton is a BYE transaction.
     */
    protected final boolean isByeTransaction() {
        return originalRequest.getMethod().equals(Request.BYE);
    }

    /**
     * Returns the message channel used for
     * transmitting/receiving messages
     * for this transaction. Made public in support of JAIN dual
     * transaction model.
     * @return Encapsulated MessageChannel.
     */
    public MessageChannel getMessageChannel() {
        return encapsulatedChannel;
    }

    /**
     * Sets the Via header branch parameter used to identify
     * this transaction.
     * @param newBranch New string used as the branch
     * for this transaction.
     */
    public final void setBranch(String newBranch) {
        branch = newBranch;
    }

    /**
     * Gets the current setting for the branch parameter of this transaction.
     * @return Branch parameter for this transaction.
     */
    public final String getBranch() {
        if (branch == null) {
            branch = getOriginalRequest().getTopmostVia().getBranch();
        }
        return branch;
    }

    /**
     * Changes the state of this transaction.
     * @param newState New state of this transaction.
     */
    public void setState(int newState) {
        currentState = newState;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "setState " + this + " " + newState);
        }

        // If this transaction is being terminated,
        if (newState == TERMINATED_STATE) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "Transaction is being terminated!");
                // new Exception().printStackTrace();
            }
        }
    }

    /**
     * Gets the current state of this transaction.
     * @return Current state of this transaction.
     */
    public final int getState() {
        return currentState;
    }

    /**
     * Enables retransmission timer events for this transaction to begin in
     * one tick.
     */
    protected final void enableRetransmissionTimer() {
        enableRetransmissionTimer(1);
    }

    /**
     * Enables retransmission timer events for this
     * transaction to begin after the number of ticks passed to
     * this routine.
     * @param tickCount Number of ticks before the
     * next retransmission timer
     * event occurs.
     */
    protected final void enableRetransmissionTimer(int tickCount) {
        retransmissionTimerTicksLeft =
                Math.min(tickCount, MAXIMUM_RETRANSMISSION_TICK_COUNT);
        retransmissionTimerLastTickCount =
                retransmissionTimerTicksLeft;
    }

    /**
     * Turns off retransmission events for this transaction.
     */
    protected final void disableRetransmissionTimer() {
        retransmissionTimerTicksLeft = -1;
    }

    /**
     * Enables a timeout event to occur for this transaction after the number
     * of ticks passed to this method.
     * @param tickCount Number of ticks before this transaction times out.
     */
    protected final void enableTimeoutTimer(int tickCount) {
        timeoutTimerTicksLeft = tickCount;
    }

    /**
     * Disabled the timeout timer.
     */
    protected final void disableTimeoutTimer() {
        timeoutTimerTicksLeft = -1;
    }

    /**
     * Fired after each timer tick.
     * Checks the retransmission and timeout
     * timers of this transaction, and fired these events
     * if necessary.
     */
    synchronized final void fireTimer() {
        // If the timeout timer is enabled,
        if (timeoutTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--timeoutTimerTicksLeft == 0) {
                // Fire the timeout timer
                fireTimeoutTimer();
            }
        }
        // If the retransmission timer is enabled,
        if (retransmissionTimerTicksLeft != -1) {
            // Count down the timer, and if it has run out,
            if (--retransmissionTimerTicksLeft == 0) {
                // Enable this timer to fire again after
                // twice the original time
                enableRetransmissionTimer
                        (retransmissionTimerLastTickCount * 2);
                // Fire the timeout timer
                fireRetransmissionTimer();
            }
        }
    }

    /**
     * Tests a message to see if it is part of this transaction.
     * @param messageToHeaderTest message to be processed
     * @return True if the message is part of this
     * transaction, false if not.
     */
    public abstract boolean
            isMessagePartOfTransaction(Message messageToHeaderTest);

    /**
     * This method is called when this transaction's
     * retransmission timer has fired.
     */
    protected abstract void fireRetransmissionTimer();

    /**
     * This method is called when this transaction's
     * timeout timer has fired.
     */
    protected abstract void fireTimeoutTimer();

    /**
     * Tests if this transaction has terminated.
     * @return Trus if this transaction is terminated, false if not.
     */
    protected final boolean isTerminated() {
        return (getState() == TERMINATED_STATE);
    }

    /**
     * Gets the host.
     * @return the host
     */
    public String getHost() {
        return encapsulatedChannel.getHost();
    }

    /**
     * Gets the key.
     * @return the key
     */
    public String getKey() {
        return encapsulatedChannel.getKey();
    }

    /**
     * Gets the port.
     * @return the port
     */
    public int getPort() {
        return encapsulatedChannel.getPort();
    }

    /**
     * Gets the SIP stack context.
     * @return the SIP Stack
     */
    public SIPMessageStack getSIPStack() {
        return parentStack;
    }

    /**
     * Gets the remote address.
     * @return the remote address
     */
    public String getPeerAddress() {
        return encapsulatedChannel.getPeerAddress();
    }

    /**
     * Gets the remote port number.
     * @return the remote port number
     */
    public int getPeerPort() {
        return encapsulatedChannel.getPeerPort();
    }

    /**
     * Gets the connection transport.
     * @return the connection transport
     */
    public String getTransport() {
        return encapsulatedChannel.getTransport();
    }

    /**
     * Checks if the connection is reliable
     * @return true if channel is on a stream connection
     */
    public boolean isReliable() {
        return encapsulatedChannel.isReliable();
    }

    /**
     * Returns the Via header for this channel. Gets the Via header of the
     * underlying message channel, and adds a branch parameter to it for this
     * transaction.
     * @return the via header
     */
    public ViaHeader getViaHeader() {
        // Via header of the encapulated channel
        ViaHeader channelViaHeader;
        // Add the branch parameter to the underlying
        // channel's Via header
        channelViaHeader = super.getViaHeader();
        channelViaHeader.setBranch(branch);
        return channelViaHeader;
    }

    /**
     * Process an exception.
     * @param ex the exception to handle
     */
    public void handleException(SIPServerException ex) {
        encapsulatedChannel.handleException(ex);
    }

    /**
     * Processes the message through the transaction and sends it to the SIP
     * peer.
     * @param messageToHeaderSend Message to send to the SIP peer.
     */
    abstract public void sendMessage(Message messageToHeaderSend)
    throws IOException;

    /**
     * Parses the byte array as a message, process it through the
     * transaction, and send it to the SIP peer.
     *
     * @param messageBytes Bytes of the message to send.
     * @param receiverAddress Address of the target peer.
     * @param receiverPort Network port of the target peer.
     *
     * @throws IOException If there is an error parsing
     * the byte array into an object.
     */
    protected void sendMessage(
            byte[] messageBytes,
            String receiverAddress,
            int receiverPort)
            throws IOException {
        // Object representation of the SIP message
        Message messageToHeaderSend;
        try {
            StringMsgParser messageParser = new StringMsgParser();
            messageToHeaderSend =
                    messageParser.parseSIPMessage(messageBytes);
            sendMessage(messageToHeaderSend);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Adds a new event listener to this transaction.
     * @param newListener Listener to add.
     */
    public void addEventListener(
            SIPTransactionEventListener newListener) {
        eventListeners.addElement(newListener);
    }

    /**
     * Removes an event listener from this transaction.
     * @param oldListener Listener to remove.
     */
    public void removeEventListener(
            SIPTransactionEventListener oldListener) {
        eventListeners.removeElement(oldListener);
    }

    /**
     * Creates a SIPTransactionErrorEvent and sends it
     * to all of the listeners of this transaction.
     * This method also flags the transaction as
     * terminated.
     * @param errorEventID ID of the error to raise.
     */
    protected void raiseErrorEvent(
            int errorEventID) {
        // Error event to send to all listeners
        SIPTransactionErrorEvent newErrorEvent;
        // Iterator through the list of listeners
        Enumeration listenerIterator;
        // Next listener in the list
        SIPTransactionEventListener nextListener;
        // Create the error event
        newErrorEvent = new SIPTransactionErrorEvent(this,
                errorEventID);
        // Loop through all listeners of this transaction
        synchronized (eventListeners) {
            listenerIterator = eventListeners.elements();
            while (listenerIterator.hasMoreElements()) {
                // Send the event to the next listener
                nextListener = (SIPTransactionEventListener)
                listenerIterator.nextElement();
                nextListener.transactionErrorEvent
                        (newErrorEvent);
            }
        }
        // Clear the event listeners after propagating the error.
        eventListeners.removeAllElements();
        // Errors always terminate a transaction
        setState(TERMINATED_STATE);
        if (this instanceof ServerTransaction &&
                this.isByeTransaction() && this.dialog != null)
            this.dialog.setState(Dialog.TERMINATED_STATE);
    }

    /**
     * A shortcut way of telling if we are a server transaction.
     * @return true if this is a servertransaction
     */
    protected boolean IsServerTransaction() {
        return this instanceof ServerTransaction;
    }

    /**
     * Gets the dialog object of this Transaction object. This object
     * returns null if no dialog exists. A dialog only exists for a
     * transaction when a session is setup between a User Agent Client and a
     * User Agent Server, either by a 1xx Provisional Response for an early
     * dialog or a 200OK Response for a committed dialog.
     * @return the Dialog Object of this Transaction object.
     * @see Dialog
     */
    public Dialog getDialog() {
        return dialog;
    }

    /**
     * Sets the dialog object.
     * @param newDialog the dialog to set.
     */
    public void setDialog(Dialog newDialog) {
        dialog = newDialog;
    }

    /**
     * Returns the current value of the retransmit timer in
     * milliseconds used to retransmit messages over unreliable transports.
     * @return the integer value of the retransmit timer in milliseconds.
     */
    public int getRetransmitTimer() {
        return SIPTransactionStack.BASE_TIMER_INTERVAL;
    }

    /**
     * Gets the host to assign for an outgoing Request via header.
     * @return the via host
     */
    public String getViaHost() {
        return getViaHeader().getHost();
    }

    /**
     * Gets the last response.
     * @return the last response
     */
    public Response getLastResponse() { return this.lastResponse; }

    /**
     * Gets the transaction Id.
     * @return the transaction id
     */
    public String getTransactionId() {
        return getOriginalRequest().getTransactionId();
    }

    /**
     * Gets the port to assign for the via header of an outgoing message.
     * @return the via port number
     */
    public int getViaPort() {
        return getViaHeader().getPort();
    }

    /**
     * A method that can be used to test if an incoming request
     * belongs to this transction. This does not take the transaction
     * state into account when doing the check otherwise it is identical
     * to isMessagePartOfTransaction. This is useful for checking if
     * a CANCEL belongs to this transaction.
     * @param requestToHeaderTest is the request to test.
     * @return true if the the request belongs to the transaction.
     */
    public boolean doesCancelMatchTransaction(
            Request requestToHeaderTest) {
        // List of Via headers in the message to test
        ViaList viaHeaders;
        // ToHeaderpmost Via header in the list
        ViaHeader topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;
        transactionMatches = false;

        if (getOriginalRequest() == null ||
                getOriginalRequest().getMethod().equals(Request.CANCEL)) {
            return false;
        }

        // Get the topmost Via header and its branch parameter
        viaHeaders = requestToHeaderTest.getViaHeaders();
        if (viaHeaders != null) {
            topViaHeader = (ViaHeader)viaHeaders.getFirst();
            messageBranch = topViaHeader.getBranch();
            if (messageBranch != null) {
                // If the branch parameter exists but
                // does not start with the magic cookie,
                if (!messageBranch.toUpperCase().startsWith(SIPConstants.
                        GENERAL_BRANCH_MAGIC_COOKIE.toUpperCase())) {
                    // Flags this as old
                    // (RFC2543-compatible) client
                    // version
                    messageBranch = null;
                }
            }
            // If a new branch parameter exists,
            if (messageBranch != null &&
                    this.getBranch() != null) {
                // If the branch equals the branch in
                // this message,
                if (getBranch().equals(messageBranch)
                && topViaHeader.getSentBy().
                        equals(((ViaHeader)getOriginalRequest().
                        getViaHeaders().getFirst()).
                        getSentBy())) {
                    transactionMatches = true;

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180, "returning true");
                    }
                }
            } else {
                // If this is an RFC2543-compliant message,
                // If RequestURI, ToHeader tag, FromHeader tag,
                // CallIdHeader, CSeqHeader number, and top Via
                // headers are the same,
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "testing against " + getOriginalRequest());
                }

                if (

                        getOriginalRequest().getRequestURI().equals
                        (requestToHeaderTest.getRequestURI()) &&
                        getOriginalRequest().getTo().equals
                        (requestToHeaderTest.getTo()) &&
                        getOriginalRequest().getFromHeader().equals
                        (requestToHeaderTest.getFromHeader()) &&
                        getOriginalRequest().getCallId().
                        getCallId().equals
                        (requestToHeaderTest.getCallId() .getCallId()) &&
                        getOriginalRequest().
                        getCSeqHeader().getSequenceNumber() ==
                        requestToHeaderTest.getCSeqHeader().
                        getSequenceNumber() &&
                        topViaHeader.equals
                        (getOriginalRequest().
                        getViaHeaders().getFirst())) {
                    transactionMatches = true;
                }
            }
        }
        return transactionMatches;
    }

    /**
     * Checks if transaction has been sent to the listener.
     * @return true if transaction has been sent
     */
    public boolean passToListener() {
        return toListener;
    }

    /**
     * Closes the encapsulated channel.
     */
    public void close() {
        encapsulatedChannel.close();
    }

    /**
     * Check if this connection is secure.
     * @return true if this is a secure channel
     */
    public boolean isSecure() {
        return encapsulatedChannel.isSecure();
    }

    /**
     * Gets the message processor handling this transaction.
     * @return the mesage processor for this transaction
     */
    public MessageProcessor getMessageProcessor() {
        return encapsulatedChannel.getMessageProcessor();
    }

    /**
     * Sets the ACK has been seen flag.
     */
    public void setAckSeen() {
        ackSeenFlag = true;
    }

    /**
     * Checks if the ACK has been seen flag is set.
     * @return true if the ACK has been seen
     */
    public boolean isAckSeen() {
        return ackSeenFlag;
    }

    /**
     * Create route set for request.
     *
     * @param request the input request
     * @throws SipException if any occurs
     */
    protected void buildRouteSet(Request request) throws SipException {
        Message origMsg = lastResponse;
        boolean isServer = false;
        Dialog dlg = getDialog();

        if (dlg != null) { // inside of dialog
            Transaction firstTransaction = dlg.getFirstTransaction();
            if (dlg.isServer()) { // dialog was created on server side
               origMsg = firstTransaction.getOriginalRequest();
               isServer = true;
           } else { // dialog was created on client side
               origMsg = firstTransaction.getLastResponse();
           }
        } else { // out of dialog
            // IMPL_NOTE: implement a support for the case when Contact
            // header is not present.
            if (this instanceof ServerTransaction) {
                origMsg = getOriginalRequest();
                isServer = true;
            }
        }

        if (origMsg == null) {
            // Avoid NPE in case when sending ACK from the part that
            // initiated a Re-INVITE.
            return;
        }

        RecordRouteList recordRouteList = origMsg.getRecordRouteHeaders();
        ContactList cl = origMsg.getContactHeaders();

        if (cl == null) {
            // Prevent NPE and log an error: Contact header is absent.
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "Transaction.buildRouteSet(): Contact must be present!");
            }
            return;
        }

        ContactHeader contact = (ContactHeader)cl.getFirst();

        if (recordRouteList == null) {
            URI remoteTarget = contact.getAddress().getURI();

            // RFC 3261, 12.2.1.1 Generating the Request
            // If the route set is empty, the UAC MUST place the remote
            // target URI into the Request-URI.  The UAC MUST NOT add
            // a Route header field to the request.
            request.setRequestURI(remoteTarget);
        } else {
            request.removeHeader(Header.ROUTE);

            RouteList routeList = new RouteList();
            // start at the end of the list and walk backwards
            Vector li = recordRouteList.getHeaders();

            int recSize = li.size();
            for (int i = 0; i < recSize; i++) {
                int j = i;
                if (!isServer) { // on client side the order is reversed
                    j = recSize - i - 1;
                }
                RecordRouteHeader rr = (RecordRouteHeader) li.elementAt(j);
                Address addr = rr.getAddress();
                RouteHeader route = new RouteHeader();
                route.setAddress((Address)rr.getAddress().clone());
                route.setParameters((NameValueList)rr.getParameters().clone());
                routeList.add(route);
            }

            RouteHeader firstRoute = (RouteHeader) routeList.getFirst();

            if (!((SipURI)firstRoute.getAddress().getURI()).hasLrParam()) {
                RouteHeader route = new RouteHeader();
                route.setAddress((Address)contact.getAddress().clone());
                routeList.removeFirst();
                // IMPL_NOTE: is clone() need?
                URI uri = firstRoute.getAddress().getURI();
                request.setRequestURI(uri);
                routeList.add(route);
                request.addHeader(routeList);
            } else {
                URI uri = (URI) contact.getAddress().getURI().clone();
                request.setRequestURI(uri);
                request.addHeader(routeList);
            }
        }
    }
}
