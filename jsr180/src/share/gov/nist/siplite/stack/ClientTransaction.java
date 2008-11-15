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

import gov.nist.siplite.message.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.*;
import gov.nist.siplite.address.*;
import gov.nist.microedition.sip.RefreshManager;
import gov.nist.core.*;
import java.util.*;

import java.io.IOException;
import javax.microedition.sip.SipException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Represents a client transaction.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * <pre>
 *
 * Implements the following state machines. (FromHeader RFC 3261)
 *
 * |INVITE from TU
 * Timer A fires |INVITE sent
 * Reset A, V Timer B fires
 * INVITE sent +-----------+ or Transport Err.
 * +---------| |---------------+inform TU
 * | | Calling | |
 * +-------->| |-------------->|
 * +-----------+ 2xx |
 * | | 2xx to TU |
 * | |1xx |
 * 300-699 +---------------+ |1xx to TU |
 * ACK sent | | |
 * resp. to TU | 1xx V |
 * | 1xx to TU -----------+ |
 * | +---------| | |
 * | | |Proceeding |-------------->|
 * | +-------->| | 2xx |
 * | +-----------+ 2xx to TU |
 * | 300-699 | |
 * | ACK sent, | |
 * | resp. to TU| |
 * | | | NOTE:
 * | 300-699 V |
 * | ACK sent +-----------+Transport Err. | transitions
 * | +---------| |Inform TU | labeled with
 * | | | Completed |-------------->| the event
 * | +-------->| | | over the action
 * | +-----------+ | to take
 * | ^ | |
 * | | | Timer D fires |
 * +--------------+ | - |
 * | |
 * V |
 * +-----------+ |
 * | | |
 * | Terminated|<--------------+
 * | |
 * +-----------+
 *
 * Figure 5: INVITE client transaction
 *
 *
 * |Request from TU
 * |send request
 * Timer E V
 * send request +-----------+
 * +---------| |-------------------+
 * | | Trying | Timer F |
 * +-------->| | or Transport Err.|
 * +-----------+ inform TU |
 * 200-699 | | |
 * resp. to TU | |1xx |
 * +---------------+ |resp. to TU |
 * | | |
 * | Timer E V Timer F |
 * | send req +-----------+ or Transport Err. |
 * | +---------| | inform TU |
 * | | |Proceeding |------------------>|
 * | +-------->| |-----+ |
 * | +-----------+ |1xx |
 * | | ^ |resp to TU |
 * | 200-699 | +--------+ |
 * | resp. to TU | |
 * | | |
 * | V |
 * | +-----------+ |
 * | | | |
 * | | Completed | |
 * | | | |
 * | +-----------+ |
 * | ^ | |
 * | | | Timer K |
 * +--------------+ | - |
 * | |
 * V |
 * NOTE: +-----------+ |
 * | | |
 * transitions | Terminated|<------------------+
 * labeled with | |
 * the event +-----------+
 * over the action
 * to take
 *
 * Figure 6: non-INVITE client transaction
 * </pre>
 */
public class ClientTransaction
        extends Transaction
        implements SIPServerResponseInterface {
    /** Last request. */
    private Request lastRequest;
    /** Flag indicating events are pending. */
    private boolean eventPending;
    /** Via port number. */
    private int viaPort;
    /** Via host. */
    private String viaHost;
    /** Real ResponseInterface to pass messages to. */
    private SIPServerResponseInterface respondTo;

    /**
     * Creates a new client transaction.
     *
     * @param newSIPMessageStack Transaction stack this transaction
     * belongs to.
     * @param newChannelToHeaderUse Channel to encapsulate.
     */
    protected ClientTransaction(
            SIPTransactionStack newSIPMessageStack,
            MessageChannel newChannelToHeaderUse) {
        super(newSIPMessageStack, newChannelToHeaderUse);
        setBranch(SIPUtils.generateBranchId());

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Creating clientTransaction " + this);
            // new Exception().printStackTrace();
        }
    }

    /**
     * Sets the real ResponseInterface this transaction encapsulates.
     *
     * @param newRespondToHeader ResponseInterface to send messages to.
     */
    public void setResponseInterface(SIPServerResponseInterface
            newRespondToHeader) {
        respondTo = newRespondToHeader;
    }

    /**
     * Gets the processing information.
     * @return processing information
     */
    public String getProcessingInfo() {
        return respondTo.getProcessingInfo();
    }

    /**
     * Returns this transaction.
     * @return request channel transaction
     */
    public MessageChannel getRequestChannel() {
        return this;
    }

    /**
     * Deterines if the message is a part of this transaction.
     *
     * @param messageToHeaderTest Message to check if it is part of this
     * transaction.
     *
     * @return True if the message is part of this transaction,
     * false if not.
     */
    public boolean isMessagePartOfTransaction(Message messageToHeaderTest) {
        return isMessageTransOrMult(messageToHeaderTest, false);
    }

    /**
     * Deterines if the message is a part of this transaction or it is
     * multiple 2xx response.
     *
     * @param messageToHeaderTest Message to check if it is part of this
     * transaction.
     *
     * @return True if the message is part of this transaction,
     * false if not.
     */
    public boolean isMessageTransOrMult(Message messageToHeaderTest) {
        return isMessageTransOrMult(messageToHeaderTest, true);
    }

    /**
     * Deterines if the message is a part of this transaction or it is
     * multiple 2xx response.
     *
     * @param messageToHeaderTest Message to check if it is part of this
     * transaction.
     * @param checkMultResponse flag of checking multiple 2xx response
     *
     * @return True if the message is part of this transaction,
     * false if not.
     */
    private boolean isMessageTransOrMult(Message messageToHeaderTest,
        boolean checkMultResponse) {

        // List of Via headers in the message to test
        ViaList viaHeaders = messageToHeaderTest.getViaHeaders();
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;
        String messageBranch = ((ViaHeader)viaHeaders.getFirst()).getBranch();
        boolean rfc3261Compliant =
            (getBranch() != null) &&
            (messageBranch != null) &&
            getBranch().startsWith(SIPConstants.GENERAL_BRANCH_MAGIC_COOKIE) &&
            messageBranch.startsWith(SIPConstants.GENERAL_BRANCH_MAGIC_COOKIE);

        /**
         * if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         "--------- TEST ------------");
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         " testing " + this.getOriginalRequest());
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         "Against " + messageToHeaderTest);
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         "isTerminated = " + isTerminated());
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         "messageBranch = " + messageBranch);
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *         "viaList = " + messageToHeaderTest.getViaHeaders());
         *     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
         *        "myBranch = " + getBranch());
         * }
         */

        transactionMatches = false;
        // Response 2xx should be processed even in TERMINATED state
        // RFC 3261, 13.2.2.4:
        // Multiple 2xx responses may arrive at the UAC for a single INVITE
        // request due to a forking proxy.
        boolean isResponse = messageToHeaderTest instanceof Response;
        if (!isTerminated() ||
            (checkMultResponse && isTerminated()
                && isResponse && isInviteTransaction() &&
                (((Response)messageToHeaderTest).getStatusCode()/100 == 2))) {
            if (rfc3261Compliant) {
                if (viaHeaders != null) {
                    // If the branch parameter is the
                    // same as this transaction and the method is the same,
                    if (getBranch().equals
                            (((ViaHeader)viaHeaders.getFirst()).
                            getBranch())) {
                        transactionMatches =
                                getOriginalRequest().getCSeqHeader().
                                getMethod().equals
                                (messageToHeaderTest.getCSeqHeader().
                                getMethod());

                    }
                }
            } else {
                transactionMatches =
                        getOriginalRequest().getTransactionId().equals
                        (messageToHeaderTest.getTransactionId());

            }

        }
        return transactionMatches;

    }

    /**
     * Deterines if the response is multiple (RFC 3261, 13.2.2.4).
     *
     * @param response response for checking
     * transaction.
     *
     * @return True if the input response has 2xx status, the current
     * transaction has TERMINATED state and the To tag is not same as
     * To tag of current transaction. This method doesn't compare other
     * members, it should be use with method isMessageTransOrMult
     * together .
     */
    public boolean isMultipleResponse(Response response) {
        boolean returnValue = false;
        if ((response.getStatusCode()/100 == 2) && isTerminated()) {
            Response lastResponse = getLastResponse();
            if (lastResponse != null) {
                String newTag = response.getToTag();
                returnValue = !newTag.equals(lastResponse.getToTag());
            }
        }
        return returnValue;
    }

    /**
     * Sends a request message through this transaction and
     * onto the client.
     *
     * @param messageToHeaderSend Request to process and send.
     */
    public void sendMessage(Message messageToHeaderSend)
    throws IOException {

        // Message typecast as a request
        Request transactionRequest;


        transactionRequest = (Request)messageToHeaderSend;


        // Set the branch id for the top via header.
        ViaHeader topVia =
                (ViaHeader) transactionRequest.getViaHeaders().getFirst();
        // Tack on a branch identifier to match responses.

        topVia.setBranch(getBranch());

        // If this is the first request for this transaction,
        if (getState() == INITIAL_STATE) {
            // Save this request as the one this transaction
            // is handling
            setOriginalRequest(transactionRequest);

            // Change to trying/calling state
            if (transactionRequest.getMethod().equals(Request.INVITE)) {
                setState(CALLING_STATE);
            } else if (transactionRequest.getMethod().equals(Request.ACK)) {
                // Acks are never retransmitted.
                setState(TERMINATED_STATE);
            } else {
                setState(TRYING_STATE);
            }

            if (!isReliable()) {
                enableRetransmissionTimer();
            }

            if (isInviteTransaction()) {
                enableTimeoutTimer(TIMER_B);
            } else {
                enableTimeoutTimer(TIMER_F);
            }

        } else if (getState() == PROCEEDING_STATE ||
                getState() == CALLING_STATE) {

            // If this is a TU-generated ACK request,
            if (transactionRequest.getMethod().equals(Request.ACK)) {
                // Send directly to the underlying
                // transport and close this transaction
                setState(TERMINATED_STATE);
                getMessageChannel().sendMessage(transactionRequest);
                return;

            }

        }

        try {
            // Send the message to the server
            lastRequest = transactionRequest;
            getMessageChannel().sendMessage(transactionRequest);
        } catch (IOException e) {
            setState(TERMINATED_STATE);
            throw e;
        }
    }


    /**
     * Processes a new response message through this transaction.
     * If necessary, this message will also be passed onto the TU.
     *
     * @param transactionResponse Response to process.
     * @param sourceChannel Channel that received this message.
     */
    public synchronized void processResponse(
            Response transactionResponse,
            MessageChannel sourceChannel)
            throws SIPServerException {

        // Log the incoming response in our log file.
        if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES))
            this.logResponse(transactionResponse,
                    System.currentTimeMillis(), "normal processing");

        int statusGroup = transactionResponse.getStatusCode()/100;

        // Ignore 1xx
        if (getState() == COMPLETED_STATE && statusGroup == 1) {
            return;
        // This block is against RFC 3261 17.1.1.2, figure 5 and
        // 17.1.4, figure 6
        /*
        } else if (PROCEEDING_STATE == this.getState()
        && transactionResponse.getStatusCode() == 100) {
            // Ignore 100 if received after 180
            return;
        */
        } else {
            // IMPL_NOTE: investigate if this flag may be completely removed.
            while (eventPending) {
                try {
                    // Wait for clearEventPending() call.
                    wait();
                } catch (InterruptedException e) {
                    // intentionally ignored
                    // wait for clearEventPending() call
                }
            }
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "processing " +
                transactionResponse.getFirstLine()
                + "current state = "
                + getState());
        }

        this.lastResponse = transactionResponse;

        if ((dialog != null) && (statusGroup < 3)) {
            // add the route before you process the response.
            dialog.addRoute(transactionResponse);
        }

        String method = transactionResponse.getCSeqHeader().getMethod();
        
        if (dialog != null) {
            boolean added = false;
            SIPTransactionStack sipStackImpl
                    = (SIPTransactionStack) getSIPStack();

            // A tag just got assigned or changed.
            if (dialog.getRemoteTag() == null &&
                    transactionResponse.getTo().getTag() != null) {

                // Dont assign tag on provisional response
                if (transactionResponse.getStatusCode() != 100) {
                    dialog.setRemoteTag(transactionResponse.getToTag());
                }

                String dialogId = transactionResponse.getDialogId(false);
                dialog.setDialogId(dialogId);

                if (sipStackImpl.isDialogCreated(method) &&
                        transactionResponse.getStatusCode() != 100) {
                    sipStackImpl.putDialog(dialog);
                    if (statusGroup == 1) {
                        dialog.setState(Dialog.EARLY_STATE);
                    } else if (statusGroup == 2) {
                        dialog.setState(Dialog.CONFIRMED_STATE);
                    }
                    added = true;
                }

            } else if (dialog.getRemoteTag() != null &&
                    transactionResponse.getToTag() != null &&
                    ! dialog.getRemoteTag().equals
                    (transactionResponse.getToTag())) {
                dialog.setRemoteTag(transactionResponse.getToTag());
                String dialogId = transactionResponse.getDialogId(false);
                dialog.setDialogId(dialogId);

                if (sipStackImpl.isDialogCreated(method)) {
                    sipStackImpl.putDialog(dialog);
                    added = true;
                }
            }

            if (sipStackImpl.isDialogCreated(method)) {
                // Make a final tag assignment.
                if (transactionResponse.getToTag() != null &&
                        statusGroup == 2) {
                    // This is a dialog creating method (such as INVITE).
                    // 2xx response -- set the state to the confirmed
                    // state.
                    dialog.setRemoteTag(transactionResponse.getToTag());
                    dialog.setState(Dialog.CONFIRMED_STATE);
                } else if ((
                        transactionResponse.getStatusCode() == 487 ||
                        statusGroup == 5 ||
                        statusGroup == 6) &&
                        (dialog.getState() == -1 ||
                        dialog.getState() ==
                        Dialog.EARLY_STATE)) {
                    // Invite transaction generated an error.
                    dialog.setState(Dialog.TERMINATED_STATE);
                }
            }
            
            // 200 OK for a bye so terminate the dialog.
            if (transactionResponse.getCSeqHeader().
                    getMethod().equals(Request.BYE) &&
                        transactionResponse.getStatusCode() == 200) {
                dialog.setState(Dialog.TERMINATED_STATE);
            }
        }
        
        try {
            if (isInviteTransaction()) {
                inviteClientTransaction(transactionResponse, sourceChannel);
            } else {
                nonInviteClientTransaction(transactionResponse, sourceChannel);
            }
        } catch (IOException ex) {
            setState(TERMINATED_STATE);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     * @param transactionResponse -- transaction response received.
     * @param sourceChannel - source channel on which the response was received.
     * <pre>
     *
     * |Request from TU
     * |send request
     * Timer E V
     * send request +-----------+
     * +---------| |-------------------+
     * | | Trying | Timer F |
     * +-------->| | or Transport Err.|
     * +-----------+ inform TU |
     * 200-699 | | |
     * resp. to TU | |1xx |
     * +---------------+ |resp. to TU |
     * | | |
     * | Timer E V Timer F |
     * | send req +-----------+ or Transport Err. |
     * | +---------| | inform TU |
     * | | |Proceeding |------------------>|
     * | +-------->| |-----+ |
     * | +-----------+ |1xx |
     * | | ^ |resp to TU |
     * | 200-699 | +--------+ |
     * | resp. to TU | |
     * | | |
     * | V |
     * | +-----------+ |
     * | | | |
     * | | Completed | |
     * | | | |
     * | +-----------+ |
     * | ^ | |
     * | | | Timer K |
     * +--------------+ | - |
     * | |
     * V |
     * NOTE: +-----------+ |
     * | | |
     * transitions | Terminated|<------------------+
     * labeled with | |
     * the event +-----------+
     * over the action
     * to take
     *
     * Figure 6: non-INVITE client transaction
     */
    private void nonInviteClientTransaction(
            Response transactionResponse,
            MessageChannel sourceChannel)
            throws IOException, SIPServerException {
        int currentState = getState();

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "nonInviteClientTransaction " +
                transactionResponse.getFirstLine());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "currentState = " + currentState);
        }

        int statusCode = transactionResponse.getStatusCode();
        if (currentState == TRYING_STATE) {
            if (statusCode / 100 == 1) {
                // Response to TU, RFC 3261, 17.1.4, figure 6
                respondTo.processResponse(transactionResponse, this);
                setState(PROCEEDING_STATE);
                enableRetransmissionTimer
                        (MAXIMUM_RETRANSMISSION_TICK_COUNT);
                enableTimeoutTimer(TIMER_F);
            } else if (200 <= statusCode && statusCode <= 699) {
                // Send the response up to the TU.
                respondTo.processResponse(transactionResponse, this);
                if (! isReliable()) {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_K);
                } else {
                    setState(TERMINATED_STATE);
                }
            }
        } else if (currentState == PROCEEDING_STATE &&
                200 <= statusCode && statusCode <= 699) {

            respondTo.processResponse(transactionResponse, this);

            disableRetransmissionTimer();
            disableTimeoutTimer();
            if (! isReliable()) {
                setState(COMPLETED_STATE);
                enableTimeoutTimer(TIMER_K);
            } else {
                setState(TERMINATED_STATE);
	        }
        } else if (currentState == PROCEEDING_STATE &&
                statusCode / 100 == 1) {
		    // Response to TU, RFC 3261, 17.1.4, figure 6
            respondTo.processResponse(transactionResponse, this);
        }
    }

    /**
     * Implements the state machine for invite client transactions.
     * @param transactionResponse -- transaction response received.
     * @param sourceChannel - source channel on which the response was received.
     * <pre>
     *
     * |INVITE from TU
     * Timer A fires |INVITE sent
     * Reset A, V Timer B fires
     * INVITE sent +-----------+ or Transport Err.
     * +---------| |---------------+inform TU
     * | | Calling | |
     * +-------->| |-------------->|
     * +-----------+ 2xx |
     * | | 2xx to TU |
     * | |1xx |
     * 300-699 +---------------+ |1xx to TU |
     * ACK sent | | |
     * resp. to TU | 1xx V |
     * | 1xx to TU -----------+ |
     * | +---------| | |
     * | | |Proceeding |-------------->|
     * | +-------->| | 2xx |
     * | +-----------+ 2xx to TU |
     * | 300-699 | |
     * | ACK sent, | |
     * | resp. to TU| |
     * | | | NOTE:
     * | 300-699 V |
     * | ACK sent +-----------+Transport Err. | transitions
     * | +---------| |Inform TU | labeled with
     * | | | Completed |-------------->| the event
     * | +-------->| | | over the action
     * | +-----------+ | to take
     * | ^ | |
     * | | | Timer D fires |
     * +--------------+ | - |
     * | |
     * V |
     * +-----------+ |
     * | | |
     * | Terminated|<--------------+
     * | |
     * +-----------+
     * </pre>
     */
    private void inviteClientTransaction(
            Response transactionResponse,
            MessageChannel sourceChannel)
            throws IOException, SIPServerException {
        int statusCode = transactionResponse.getStatusCode();
        int currentState = getState();

        if (currentState == TERMINATED_STATE) {
            // Do nothing in the terminated state.
            return;
        } else if (currentState == CALLING_STATE) {
            if (statusCode/100 == 2) {
                // 200 responses are always seen by TU.
                respondTo.processResponse(transactionResponse, this);
                disableRetransmissionTimer();
                disableTimeoutTimer();
                setState(TERMINATED_STATE);
            } else if (statusCode/100 == 1) {
                disableRetransmissionTimer();
                disableTimeoutTimer();
                respondTo.processResponse(transactionResponse, this);
                setState(PROCEEDING_STATE);
            } else if (300 <= statusCode && statusCode <= 699) {
                // When in either the "Calling" or "Proceeding" states,
                // reception of response with status code from 300-699
                // MUST cause the client transaction to
                // transition to "Completed".
                // The client transaction MUST pass the received response up to
                // the TU, and the client transaction MUST generate an
                // ACK request.

                respondTo.processResponse(transactionResponse, this);

                // Send back an ACK request
                try {
                    sendMessage((Request) createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                if (! isReliable()) {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    // Proceed immediately to the TERMINATED state.
                    setState(TERMINATED_STATE);
                }
            }
        } else if (currentState == PROCEEDING_STATE) {
            if (statusCode / 100 == 1) {
                respondTo.processResponse(transactionResponse, this);
            } else if (statusCode / 100 == 2) {
                setState(TERMINATED_STATE);
                respondTo.processResponse(transactionResponse, this);
            } else if (300 <= statusCode && statusCode <= 699) {
                respondTo.processResponse(transactionResponse, this);
                // Send back an ACK request
                try {
                    sendMessage((Request)createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                if (! isReliable()) {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    setState(TERMINATED_STATE);
                }
            }
        } else if (currentState == COMPLETED_STATE) {
            if (300 <= statusCode && statusCode <= 699) {
                // Send back an ACK request
                try {
                    sendMessage((Request)createAck());
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }

        }

    }

    /**
     * Sends specified {@link gov.nist.siplite.message.Request} on a unique
     * client transaction identifier. This method implies that the application
     * is functioning as either a User Agent Client or a Stateful proxy, hence
     * the underlying SipProvider acts statefully.
     * <p>
     * JAIN SIP defines a retransmission utility specific to user agent
     * behaviour and the default retransmission behaviour for each method.
     * <p>
     * When an application wishes to send a message, it creates a Request
     * message passes that Request to this method, this method returns the
     * cleintTransactionId generated by the SipProvider. The Request message
     * gets sent via the ListeningPoint that this SipProvider is attached to.
     * <ul>
     * <li>User Agent Client - must not send a BYE on a confirmed INVITE until
     * it has received an ACK for its 2xx response or until the server
     * transaction times out.
     * </ul>
     *
     * @throws IOException if an I/O error occured
     * @throws SipException if implementation cannot send request for any
     * other reason
     */
    public void sendRequest() throws IOException, SipException {
        Request sipRequest = getOriginalRequest();
        sendMessage(sipRequest);
    }


    /**
     * Called by the transaction stack when a retransmission timer
     * fires.
     */
    protected void fireRetransmissionTimer() {
        boolean noSend = false;
        try {
            // Resend the last request sent
            // System.out.println("fireRetransmissionTimer ");
            if (this.getState() == -1) {
                noSend = true;
            } else {
                MessageProcessor mp = this.getMessageProcessor();
                if (mp == null) {
                    noSend = true;
                } else if (mp.toExit()) {
                    noSend = true;
                }
            }
            int currentState = this.getState();
            if (!noSend && (currentState == CALLING_STATE ||
                currentState == TRYING_STATE)) {
                getMessageChannel().sendMessage(lastRequest);
            }
        } catch (IOException e) {
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
            
            if (lastRequest.getErrorListener() != null) {
                lastRequest.getErrorListener().notifyError("Failed to " +
                        "retransmit the following request: " + lastRequest);
            }
        }
    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {
        Dialog dialogImpl = this.getDialog();
        if (getState() == CALLING_STATE ||
                getState() == TRYING_STATE ||
                getState() == PROCEEDING_STATE) {
            // Timeout occured. If this is asociated with a transaction
            // creation then kill the dialog.
            if (dialogImpl != null) {
                if (((SIPTransactionStack)getSIPStack()).isDialogCreated
                        (this.getOriginalRequest().getMethod())) {
                    // terminate the enclosing dialog.
                    dialogImpl.setState(Dialog.TERMINATED_STATE);
                } else if (getOriginalRequest().getMethod().equals
                        (Request.BYE)) {
                    // Terminate the associated dialog on BYE Timeout.
                    dialogImpl.setState(Dialog.TERMINATED_STATE);
                }
            }
        }
        if (getState() != COMPLETED_STATE) {
            /*
             * If refresh responses are not received due to network problems, 
             * failures are reported to the user in the refreshEvent() callback.
             */
            Request request = getRequest();
            int refreshID = request.getRefreshID();
            if (refreshID == -1) {
                return;
            }
            RefreshManager refreshManager = RefreshManager.getInstance();
            String taskID = "";
            try {
                taskID = String.valueOf(refreshID);
            } catch (NumberFormatException nfe) {
                return;
            }
            refreshManager.getTask(taskID).getSipRefreshListener().refreshEvent(
                    refreshID, 
                    SIPErrorCodes.REQUEST_TIMEOUT, 
                    SIPErrorCodes.getReasonPhrase(SIPErrorCodes.REQUEST_TIMEOUT));
            
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
        } else {
            setState(TERMINATED_STATE);
        }
    }

    /**
     * Creates a new Cancel message from the Request associated with this client
     * transaction. The CANCEL request, is used to cancel the previous request
     * sent by this client transaction. Specifically, it asks the UAS to cease
     * processing the request and to generate an error response to that request.
     *
     * @return a cancel request generated from the original request.
     * @throws SipException if the request cannot be cancelled.
     */
    public Request createCancel() throws SipException {
        Request originalRequest = getOriginalRequest();
        return originalRequest.createCancelRequest();
    }

    /**
     * Creates an ACK request for this transaction
     *
     * @return an ack request generated from the original request.
     * @throws SipException if transaction is in the wrong state to be acked.
     */
    public Request createAck() throws SipException {
        Request originalRequest = getOriginalRequest();
        int statusCode = 0;

        if (originalRequest.getMethod().equals(Request.ACK)) {
            throw new SipException("Cannot ACK an ACK!",
                SipException.INVALID_OPERATION);
        } else if (lastResponse == null) {
            throw new SipException("bad Transaction state",
                SipException.INVALID_STATE);
        } else {
            statusCode = lastResponse.getStatusCode();
            if (statusCode < 200) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "lastResponse = " + lastResponse);
                }

                throw new SipException("Cannot ACK a provisional response!",
                    SipException.INVALID_OPERATION);
            }
        }

        Request ackRequest =
            originalRequest.createAckRequest((ToHeader)lastResponse.getTo());

        // Automatic ACK at transaction layer
        if (300 <= statusCode && statusCode <= 699) {
            ViaHeader topmostVia = originalRequest.getTopmostVia();
            if (topmostVia != null) {
                ackRequest.setHeader(topmostVia);
            }
            return ackRequest;
        }

        // Pull the record route headers from the last reesponse.
        buildRouteSet(ackRequest);
        return ackRequest;


    }

    /**
     * Sets the port of the recipient.
     * @param port the new via port
     */
    protected void setViaPort(int port) { this.viaPort = port; }

    /**
     * Sets the port of the recipient.
     * @param host the new via host
     */
    protected void setViaHost(String host) { this.viaHost = host; }

    /**
     * Gets the port of the recipient.
     * @return the via port
     */
    public int getViaPort() { return this.viaPort; }

    /**
     * Gets the host of the recipient.
     * @return the via host
     */
    public String getViaHost() { return this.viaHost; }

    /**
     * Gets the via header for an outgoing request.
     * @return the via header reader
     */
    public ViaHeader getOutgoingViaHeader() {
        return this.getMessageProcessor().getViaHeader();
    }

    /**
     * Checks if connection is secure.
     * @return true if connection is secure.
     */
    public boolean isSecure() { return encapsulatedChannel.isSecure(); }

    /**
     * Clears the event pending flag.
     */
    public synchronized void clearEventPending() {
        eventPending = false;
        notify();
    }

    /**
     * Sets the event pending flag.
     */
    public synchronized void setEventPending() {
        eventPending = true;
    }

    /**
     * Create a new client transaction based on current.
     * Field lastResponse is filled by input parameter.
     * @param lastResponse last response
     * @return new instance of client transaction.
     */
    public ClientTransaction cloneWithNewLastResponse(Response lastResponse) {
        ClientTransaction clientTransaction = new ClientTransaction(
            (SIPTransactionStack)getSIPStack(), getMessageChannel());
        clientTransaction.lastResponse = lastResponse;
        clientTransaction.setOriginalRequest(getOriginalRequest());
        return clientTransaction;
    }

}
