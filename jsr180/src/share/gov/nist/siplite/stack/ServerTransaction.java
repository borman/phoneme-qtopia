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
import gov.nist.siplite.address.*;
import gov.nist.core.*;
import gov.nist.siplite.*;

import java.io.IOException;
import javax.microedition.sip.SipException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Represents a server transaction.
 *
 *
 * @version JAIN-SIP-1.1
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ServerTransaction
        extends Transaction
        implements SIPServerRequestInterface {
    /** Collection time. */
    protected int collectionTime;
    /** Real RequestInterface to pass messages to. */
    private SIPServerRequestInterface requestOf;
    /** Flag indicating this transaction is known to the stack. */
    protected boolean isMapped;

    /**
     * Sends the SIP response.
     * @param transactionResponse the transaction response
     * @exception IOException if the response could not be sent
     */
    private void sendSIPResponse(Response transactionResponse)
    throws IOException {
        if (transactionResponse.getTopmostVia().
                getParameter(ViaHeader.RECEIVED) == null) {
            // Send the response back on the same peer
            // as received.
            getMessageChannel().sendMessage(transactionResponse);
        } else {
            // Respond to the host name in the received parameter.
            ViaHeader via = transactionResponse.getTopmostVia();
            String host = via.getParameter(ViaHeader.RECEIVED);
            int port = via.getPort();
            if (port == -1) port = 5060;
            String transport = via.getTransport();
            Hop hop = new Hop(host+":"+port+"/" +transport);
            MessageChannel messageChannel =
                    ((SIPTransactionStack)getSIPStack()).
                    createRawMessageChannel(hop);
            messageChannel.sendMessage(transactionResponse);
        }
        this.lastResponse = transactionResponse;
    }

    /**
     * Delays the sending of the Trying state.
     */
    class SendTrying extends Thread {
        /** Current server transaction. */
        ServerTransaction myTransaction;

        /**
         * Constructore with initial transaction.
         * @param transaction the transaction to be sent
         */
        public SendTrying(ServerTransaction transaction) {
            myTransaction = transaction;
            Thread myThread = new Thread(this);
            myThread.start();
        }

        /** Main loop for sending transaction asynchronously. */
        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) { }

            if (myTransaction.getState() == TRYING_STATE) {
                try {
                    myTransaction.sendMessage
                            (myTransaction.getOriginalRequest().
                            createResponse(100, "Trying"));
                } catch (IOException ex) {}
            }
            return;
        }
    }

    /**
     * Creates a new server transaction.
     *
     * @param newSIPMessageStack Transaction stack this transaction
     * belongs to.
     * @param newChannelToHeaderUse Channel to encapsulate.
     */
    protected ServerTransaction(SIPTransactionStack newSIPMessageStack,
            MessageChannel newChannelToHeaderUse) {
        super(newSIPMessageStack, newChannelToHeaderUse);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Creating Server Transaction" + this);
            // new Exception().printStackTrace();
        }
    }

    /**
     * Sets the real RequestInterface this transaction encapsulates.
     *
     * @param newRequestOf RequestInterface to send messages to.
     */
    public void setRequestInterface(SIPServerRequestInterface newRequestOf) {
        requestOf = newRequestOf;
    }

    /**
     * Gets the processing infromation.
     * @return the processing information
     */
    public String getProcessingInfo() {
        return requestOf.getProcessingInfo();
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
    public boolean isMessagePartOfTransaction(
            Message messageToHeaderTest) {
        // List of Via headers in the message to test
        ViaList viaHeaders;
        // ToHeaderpmost Via header in the list
        ViaHeader topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;


        transactionMatches = false;
        // Compensation for retransmits after OK has been dispatched
        // as suggested by Antonis Karydas.
        if ((((SIPTransactionStack)getSIPStack()).isDialogCreated
                (((Request)messageToHeaderTest).getMethod()))
                || !isTerminated()) {
            // Get the topmost Via header and its branch parameter
            viaHeaders = messageToHeaderTest.getViaHeaders();
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
                    if (getBranch().equals(messageBranch)
                    && topViaHeader.getSentBy().
                            equals(((ViaHeader)getOriginalRequest().
                            getViaHeaders().getFirst()).
                            getSentBy())) {
                        // Matching server side transaction with only the
                        // branch parameter.
                        transactionMatches = true;
                    }
                    // If this is an RFC2543-compliant message,
                } else {

                    // If RequestURI, ToHeader tag, FromHeader tag,
                    // CallIdHeader, CSeqHeader number, and top Via
                    // headers are the same,
                    String originalFromHeaderTag =
                            getOriginalRequest().getFromHeader().
                            getTag();
                    String thisFromHeaderTag =
                            messageToHeaderTest.getFromHeader().getTag();
                    boolean skipFromHeader =
                            (originalFromHeaderTag == null ||
                            thisFromHeaderTag == null);
                    String originalToHeaderTag =
                            getOriginalRequest().getTo().
                            getTag();
                    String thisToHeaderTag =
                            messageToHeaderTest.getTo().getTag();
                    boolean skipToHeader =
                            (originalToHeaderTag == null ||
                            thisToHeaderTag == null);
                    if (getOriginalRequest().
                            getRequestURI().
                            equals(((Request)messageToHeaderTest).
                            getRequestURI()) &&
                            (skipFromHeader ||
                            originalFromHeaderTag.equals(thisFromHeaderTag)) &&
                            (skipToHeader ||
                            originalToHeaderTag.equals(thisToHeaderTag)) &&
                            getOriginalRequest().
                            getCallId().getCallId().
                            equals(messageToHeaderTest.getCallId()
                            .getCallId()) &&
                            getOriginalRequest().
                            getCSeqHeader().getSequenceNumber() ==
                            messageToHeaderTest.getCSeqHeader().
                            getSequenceNumber() &&
                            topViaHeader.equals(
                            getOriginalRequest().
                            getViaHeaders().getFirst())) {
                        transactionMatches = true;
                    }
                }
            }
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "TRANSACTION MATCHES:" + transactionMatches);
        }

        return transactionMatches;
    }

    /**
     * Sends out a trying response (only happens when the transaction is
     * mapped). Otherwise the transaction is not known to the stack.
     * @exception IOException if the attempt to send fails
     */
    protected void map() throws IOException {
        if (getState() == -1 ||
                getState() == TRYING_STATE) {
            if (isInviteTransaction() && ! this.isMapped) {
                this.isMapped = true;
                // Has side-effect of setting
                // state to "Proceeding"
                new SendTrying(this);
            } else {
                isMapped = true;
            }
        }
    }

    /**
     * Returns true if the transaction is known to stack.
     * @return true if the transaction is already mapped
     */
    public boolean isTransactionMapped() {
        return this.isMapped;
    }

    /**
     * Processes a new request message through this transaction.
     * If necessary, this message will also be passed onto the TU.
     *
     * IMPL_NOTE:
     * Receiving the PUBLISH request at ESC (i.e. UAS):
     * The event state is identified by three major pieces:
     * Request-URI, event-type and entity-tag (RFC3903, section 4.1).
     * Maybe it is needed to maintain the event state vector in our
     * implementation.
     *
     * @param transactionRequest Request to process.
     * @param sourceChannel Channel that received this message.
     */
    public void processRequest(
            Request transactionRequest,
            MessageChannel sourceChannel)
            throws SIPServerException {
        boolean toTu = false;

        try {
            // If this is the first request for this transaction,
            if (getState() == -1) {
                // Save this request as the one this
                // transaction is handling
                setOriginalRequest(transactionRequest);
                setState(TRYING_STATE);
                toTu = true;
                if (isInviteTransaction() && this.isMapped) {
                    // Has side-effect of setting
                    // state to "Proceeding".
                    sendMessage(transactionRequest.
                            createResponse(100, "Trying"));
                }
                // If an invite transaction is ACK'ed while in
                // the completed state,
            } else if (isInviteTransaction()
                    && COMPLETED_STATE == getState()
                    && transactionRequest.getMethod().equals(Request.ACK)) {
                setState(CONFIRMED_STATE);
                disableRetransmissionTimer();
                if (!isReliable()) {
                    if (this.lastResponse != null
                            && this.lastResponse.getStatusCode()
                            == SIPErrorCodes.REQUEST_TERMINATED) {
                        setState(TERMINATED_STATE);
                    } else {
                        enableTimeoutTimer(TIMER_I);
                    }
                } else {
                    setState(TERMINATED_STATE);
                }
                // Application should not Ack in CONFIRMED state
                return;
            } else if (transactionRequest.getMethod().equals
                    (getOriginalRequest().getMethod())) {
                if (getState() == PROCEEDING_STATE ||
                        getState() == COMPLETED_STATE) {
                    // Resend the last response to
                    // the client
                    if (lastResponse != null) {
                        try {
                            // Send the message to the client
                            getMessageChannel().sendMessage
                                    (lastResponse);
                        } catch (IOException e) {
                            setState(TERMINATED_STATE);
                            throw e;
                        }
                    }
                } else if (transactionRequest.getMethod().
                        equals(Request.ACK)) {
                    // This is passed up to the TU to suppress
                    // retransmission of OK
                    requestOf.processRequest
                            (transactionRequest, this);
                }
                return;
            }

            // Pass message to the TU
            if (COMPLETED_STATE != getState()
                    && TERMINATED_STATE != getState()
                    && requestOf != null) {
                if (getOriginalRequest().getMethod()
                    .equals(transactionRequest.getMethod())) {
                    // Only send original request to TU once!
                    if (toTu)
                        requestOf.processRequest(transactionRequest,
                                this);
                } else {
                    requestOf.processRequest(transactionRequest,
                            this);
                }
            } else {
                // need revisit
                // I am allowing it through!
                if (((SIPTransactionStack) getSIPStack()).isDialogCreated(
                        getOriginalRequest().getMethod())
                        && getState() == TERMINATED_STATE
                        && transactionRequest.getMethod().equals
                        (Request.ACK)
                        && requestOf != null) {
                    if (! this.getDialog().ackSeen) {
                        (this.getDialog()).ackReceived(
                                transactionRequest);
                        requestOf.processRequest
                                (transactionRequest, this);
                    }
                } else if (
                        transactionRequest.getMethod().equals
                        (Request.CANCEL)) {

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "Too late to cancel Transaction");
                    }
                }

                // send OK and just ignore the CANCEL.
                try {
                    this.sendMessage(transactionRequest.
                        createResponse(SIPErrorCodes.OK));
                } catch (IOException ex) {
                    // Transaction is already terminated
                    // just ignore the IOException.
                }

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "Dropping request " + getState());
                }
            }
        } catch (IOException e) {
            raiseErrorEvent
                    (SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }
    }


    /**
     * Sends a response message through this transactionand onto
     * the client.
     *
     * @param messageToSend Response to process and send.
     */
    public void sendMessage(Message messageToSend)
    throws IOException {

        // Message typecast as a response
        Response transactionResponse;
        // Status code of the response being sent to the client
        int statusCode;

        // Get the status code from the response
        transactionResponse = (Response)messageToSend;
        statusCode = transactionResponse.getStatusCode();
        Dialog dialog = this.dialog;
        // super.checkCancel(transactionResponse);
        // Provided we have set the banch id for this we set the BID for the
        // outgoing via.
        if (this.getBranch() != null) {
            transactionResponse.getTopmostVia().setBranch
                    (this.getBranch());
        } else {
            transactionResponse.getTopmostVia().removeParameter
                    (ViaHeader.BRANCH);
        }
        // Method of the response does not match the request used to
        // create the transaction - transaction state does not change.
        if (! transactionResponse.getCSeqHeader().getMethod().equals
                (getOriginalRequest().getMethod())) {
            sendSIPResponse(transactionResponse);
            return;
        }
        if (this.dialog != null) {
            if (this.dialog.getRemoteTag() == null &&
                    transactionResponse.getTo().getTag() != null &&
                    ((SIPTransactionStack) this.getSIPStack()).isDialogCreated
                    (transactionResponse.getCSeqHeader().getMethod())) {
                this.dialog.setRemoteTag(transactionResponse.getTo().getTag());
                ((SIPTransactionStack) this.getSIPStack())
                .putDialog(this.dialog);
                if (statusCode/100 == 1)
                    this.dialog.setState(Dialog.EARLY_STATE);
            } else if (((SIPTransactionStack) this.getSIPStack())
            .isDialogCreated
                    (transactionResponse.getCSeqHeader().getMethod())) {
                if (statusCode / 100 == 2) {
                    if (!this.isInviteTransaction()) {
                        this.dialog.setState(Dialog.CONFIRMED_STATE);
                    } else {
                        if (this.dialog.getState() == -1)
                            this.dialog.setState(Dialog.EARLY_STATE);
                    }
                } else if (statusCode >= 300 && statusCode <= 699 &&
                        (this.dialog.getState() == -1 ||
                        this.dialog.getState() == Dialog.EARLY_STATE)) {
                    this.dialog.setState(Dialog.TERMINATED_STATE);
                }
            } else if (transactionResponse.getCSeqHeader().getMethod()
            .equals(Request.BYE) &&
                    statusCode/100 == 2) {
                // Dialog will be terminated when the transction is terminated.
                if (! isReliable()) this.dialog
                        .setState(Dialog.COMPLETED_STATE);
                else this.dialog.setState(Dialog.TERMINATED_STATE);
            }
        }
        // If the TU sends a provisional response while in the
        // trying state,
        if (getState() == TRYING_STATE) {
            if (statusCode / 100 == 1) {
                setState(PROCEEDING_STATE);
            } else if (200 <= statusCode && statusCode <= 699) {
                if (! isInviteTransaction()) {
                    setState(COMPLETED_STATE);
                } else {
                    if (statusCode /100 == 2) {
                        this.collectionTime = TIMER_J;
                        setState(TERMINATED_STATE);
                    } else
                        setState(COMPLETED_STATE);
                }
                if (!isReliable()) {
                    enableRetransmissionTimer();
                }
                enableTimeoutTimer(TIMER_J);
            }
            // If the transaction is in the proceeding state,
        } else if (getState() == PROCEEDING_STATE) {
            if (isInviteTransaction()) {
                // If the response is a failure message,
                if (statusCode / 100 == 2) {
                    // Set up to catch returning ACKs
                    // Antonis Karydas: Suggestion
                    // Recall that the CANCEL's response will go
                    // through this transaction
                    // and this may well be it. Do NOT change the
                    // transaction state if this
                    // is a response for a CANCEL.
                    // Wait, instead for the 487 from TU.
                    if (!transactionResponse.getCSeqHeader().getMethod().equals
                            (Request.CANCEL)) {
                        setState(TERMINATED_STATE);
                        if (!isReliable()) {
                            ((Dialog) this.getDialog())
                            .setRetransmissionTicks();
                            enableRetransmissionTimer();

                        }
                        this.collectionTime = TIMER_J;
                        enableTimeoutTimer(TIMER_J);
                    }
                } else if (300 <= statusCode && statusCode <= 699) {
                    // Set up to catch returning ACKs
                    setState(COMPLETED_STATE);
                    if (!isReliable()) {
                        enableRetransmissionTimer();
                    }
                    // Changed to TIMER_H as suggested by
                    // Antonis Karydas
                    enableTimeoutTimer(TIMER_H);
                    // If the response is a success message,
                } else if (statusCode / 100 == 2) {
                    // Terminate the transaction
                    setState(TERMINATED_STATE);
                    disableRetransmissionTimer();
                    disableTimeoutTimer();
                }
                // If the transaction is not an invite transaction
                // and this is a final response,
            } else if (200 <= statusCode && statusCode <= 699) {
                // Set up to retransmit this response,
                // or terminate the transaction
                setState(COMPLETED_STATE);
                if (!isReliable()) {
                    disableRetransmissionTimer();
                    enableTimeoutTimer(TIMER_J);
                } else {
                    setState(TERMINATED_STATE);
                }
            }

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "SEND MESSAGE :: SERVER TRANSACTION STATE SET " +
                        getState());
            }

            // If the transaction has already completed,
        } else if (getState() == COMPLETED_STATE) {
            return;
        }

        try {
            // Send the message to the client
            lastResponse = transactionResponse;
            sendSIPResponse(transactionResponse);
        } catch (IOException e) {
            setState(TERMINATED_STATE);
            throw e;
        }
    }

    /**
     * Gets the via host name.
     * @return the via host
     */
    public String getViaHost() {
        return encapsulatedChannel.getViaHost();
    }

    /**
     * Gets the via port number.
     * @return the via port number
     */
    public int getViaPort() {
        return encapsulatedChannel.getViaPort();
    }

    /**
     * Called by the transaction stack when a retransmission
     * timer fires. This retransmits the last response when the
     * retransmission filter is enabled.
     */
    protected void fireRetransmissionTimer() {
        try {
            // Resend the last response sent by this transaction
            if (isInviteTransaction() &&
                    ((SIPTransactionStack)getSIPStack()).retransmissionFilter)
                getMessageChannel().sendMessage(lastResponse);
        } catch (IOException e) {
            raiseErrorEvent
                    (SIPTransactionErrorEvent.TRANSPORT_ERROR);
            
            if (lastResponse.getErrorListener() != null) {
                lastResponse.getErrorListener().notifyError("Failed to " +
                        "retransmit the following response: " + lastResponse);
            }
        }
    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "ServerTransaction.fireTimeoutTimer "
                + this.getState() + " method = " +
                this.getOriginalRequest().getMethod());
        }

        Dialog dialog = (Dialog) this.getDialog();
        int mystate = this.getState();

        if (((SIPTransactionStack)getSIPStack()).isDialogCreated
                (this.getOriginalRequest().getMethod()) &&
                (mystate == super.CALLING_STATE ||
                mystate == super.TRYING_STATE)) {
            dialog.setState(Dialog.TERMINATED_STATE);
        } else if (getOriginalRequest().getMethod().equals(Request.BYE)) {
            if (dialog != null)
                dialog.setState(Dialog.TERMINATED_STATE);
        }

        if ((getState() == CONFIRMED_STATE ||
                getState() == COMPLETED_STATE) &&
                isInviteTransaction()) {
            raiseErrorEvent
                    (SIPTransactionErrorEvent.TIMEOUT_ERROR);
            setState(TERMINATED_STATE);
        } else if (! isInviteTransaction() && (
                getState() == COMPLETED_STATE ||
                getState() == CONFIRMED_STATE)) {
            setState(TERMINATED_STATE);
        } else if (isInviteTransaction() &&
                getState() == TERMINATED_STATE) {
            // This state could be reached when retransmitting
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            if (dialog != null) dialog.setState(Dialog.TERMINATED_STATE);
        }
    }

    /**
     * Gets the last response.
     * @return the last response
     */
    public Response getLastResponse() {
        return this.lastResponse;
    }

    /**
     * Sets the original request.
     * @param originalRequest original request to remember
     */
    public void setOriginalRequest(Request originalRequest) {
        super.setOriginalRequest(originalRequest);
        // ACK Server Transaction is just a dummy transaction.
        if (originalRequest.getMethod().equals("ACK"))
            this.setState(TERMINATED_STATE);

    }

    /**
     * Sends specified Response message to a Request which is identified by the
     * specified server transaction identifier. The semantics for various
     * application behaviour on sending Responses to Requests is outlined at
     * {@link SipListener#processRequest(RequestEvent)}.
     * <p>
     * Note that when a UAS core sends a 2xx response to an INVITE, the server
     * transaction is destroyed, by the underlying JAIN SIP implementation.
     * This means that when the ACK sent by the corresponding UAC arrives
     * at the UAS, there will be no matching server transaction for the ACK,
     * and based on this rule, the ACK is passed to the UAS application core,
     * where it is processed.
     * This ensures that the three way handsake of an INVITE that is managed by
     * the UAS application and not JAIN SIP.
     *
     * @param response the Response to send to the Request
     * @throws IOException if an I/O error occured
     * @throws SipException if implementation cannot send response for any
     * other reason
     * @see Response
     */
    public void sendResponse(Response response)
            throws IOException, SipException {
        try {
            Dialog dialog = (Dialog) getDialog();
            // Fix up the response if the dialog has already been established.
            Response responseImpl = response;
            int statusCode = responseImpl.getStatusCode();
            int statusGroup = statusCode / 100;
            if (statusGroup == 2 &&
                    parentStack.isDialogCreated
                    (responseImpl.getCSeqHeader().getMethod()) &&
                    dialog != null &&
                    dialog.getLocalTag() == null &&
                    responseImpl.getTo().getTag() == null) {
                throw new SipException("ToHeader tag must be set for OK",
                    SipException.INVALID_MESSAGE);
            }

            if (statusGroup == 2 &&
                    responseImpl.getCSeqHeader().getMethod().equals
                    (Request.INVITE) &&
                    responseImpl.getHeader(Header.CONTACT) == null) {
                throw new SipException("Contact Header is mandatory for the OK",
                    SipException.INVALID_MESSAGE);
            }

            // If sending the response within an established dialog, then
            // set up the tags appropriately.
            if (dialog != null && dialog.getLocalTag() != null) {
                responseImpl.getTo().setTag(dialog.getLocalTag());
            }

            String fromTag = getRequest().getFromHeader().getTag();

            // Backward compatibility slippery slope....
            // Only set the from tag in the response when the
            // incoming request has a from tag.
            if (fromTag != null) {
                responseImpl.getFromHeader().setTag(fromTag);
            } else {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "WARNING -- Null From tag Dialog layer in jeopardy!!");
                }
            }

            sendMessage(response);

            // Transaction successfully cancelled but dialog has not yet
            // been established so delete the dialog.
            if (Utils.equalsIgnoreCase(
                    responseImpl.getCSeqHeader().getMethod(),
                    Request.CANCEL)
              && statusGroup == 2 
                    // && (!dialog.isReInvite())
              && parentStack.isDialogCreated(getOriginalRequest().getMethod())
              && (dialog.getState() == Dialog.INITIAL_STATE
                 || dialog.getState() == Dialog.EARLY_STATE)) {
                dialog.setState(Dialog.TERMINATED_STATE);
            }
            // See if the dialog needs to be inserted into the dialog table
            // or if the state of the dialog needs to be changed.
            if (dialog != null) {
                dialog.printTags();
                if (Utils.equalsIgnoreCase
                        (responseImpl.getCSeqHeader().getMethod(),
                        Request.BYE)) {
                    dialog.setState(Dialog.TERMINATED_STATE);
                } else if (Utils.equalsIgnoreCase
                        (responseImpl.getCSeqHeader().getMethod(),
                        Request.CANCEL)) {
                    if (dialog.getState() == -1 ||
                            dialog.getState() == Dialog.EARLY_STATE) {
                        dialog.setState(Dialog.TERMINATED_STATE);
                    }
                } else {
                    if (dialog.getLocalTag() == null &&
                        responseImpl.getTo().getTag() != null) {
                        if (statusCode != 100)
                            dialog.setLocalTag(responseImpl.getTo().getTag());
                    }
                    if (parentStack.isDialogCreated(responseImpl
                        .getCSeqHeader().getMethod())) {
                        if (statusGroup == 1 && statusCode != 100) {
                            dialog.setState(Dialog.EARLY_STATE);
                        } else if (statusGroup == 2) {
                            dialog.setState(Dialog.CONFIRMED_STATE);
                        }
                        // Enter into our dialog table provided this is a
                        // dialog creating method.
                        if (statusCode != 100)
                            parentStack.putDialog(dialog);
                    }
                }
            }
        } catch (NullPointerException npe) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "ServerTransaction.sendResponse(): NPE occured: " + npe);
                npe.printStackTrace();
            }

            throw new SipException("NPE occured: " + npe.getMessage(),
                SipException.GENERAL_ERROR);
        }
    }

    /**
     * Returns this transaction.
     * @return the response message channel
     */
    public MessageChannel getResponseChannel() {
        return this;
    }
}
