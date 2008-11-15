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
package gov.nist.siplite;

import gov.nist.siplite.message.*;
import gov.nist.siplite.stack.*;
import gov.nist.siplite.header.*;
import gov.nist.core.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * An adapter class from the JAIN implementation objects to the NIST-SIP stack.
 * This is the class that is instantiated by the NistSipMessageFactory to
 * create a new SIPServerRequest or SIPServerResponse.
 * Note that this is not part of the JAIN-SIP spec (it does not implement
 * a JAIN-SIP interface). This is part of the glue that ties together the
 * NIST-SIP stack and event model with the JAIN-SIP stack. Implementors
 * of JAIN services need not concern themselves with this class.
 *
 * @version  JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NistSipMessageHandlerImpl
        implements SIPServerRequestInterface, SIPServerResponseInterface {
    /** Current transaction channel. */
    protected Transaction transactionChannel;
    /** Raw message channel. */
    protected MessageChannel rawMessageChannel;
    // protected Request sipRequest;
    // protected Response sipResponse;
    /** Current listening filter. */
    protected ListeningPoint listeningPoint;
    /**
     * Process a request.
     * @param sipRequest the request to be processed
     * @param incomingMessageChannel the inbound message connection
     * @exception SIPServerException is thrown when there is an error
     * processing the request.
     */
    public void processRequest(Request sipRequest,
            MessageChannel incomingMessageChannel)
            throws SIPServerException {
        // Generate the wrapper JAIN-SIP object.
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "PROCESSING INCOMING REQUEST " + sipRequest.getFirstLine());
        }
        this.rawMessageChannel = incomingMessageChannel;
        
        SipStack sipStack = (SipStack) transactionChannel.getSIPStack();
        
        /*
         * What if the listeningPoint of the incomingMessagechannel different
         * than the one that set during initialization in
         * NistSipMesageFactoryImpl?
         * So, initialize it explicitely for the incomingMessageChannel
         */
        listeningPoint =
              incomingMessageChannel.getMessageProcessor().getListeningPoint();
        if (listeningPoint == null) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                    "Dropping message: No listening point registered!");
            }
            return;
        }
        
        SipProvider sipProvider = listeningPoint.getProvider();
        
        if (sipProvider == null)  {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                    "No provider - dropping !!");
            }
            return;
        }

        // SipListener sipListener = sipProvider.sipListener;
        Transaction transaction = transactionChannel;
        
        // Look for the registered SIPListener for the message channel.
        synchronized (sipProvider) {
            String dialogId = sipRequest.getDialogId(true);
            Dialog dialog = sipStack.getDialog(dialogId);
            final ServerTransaction requestTransaction =
                    (ServerTransaction) sipRequest.getTransaction();
            if (null == requestTransaction.getDialog()) {
                requestTransaction.setDialog(dialog);
            }
            if (sipRequest.getMethod().equals(Request.ACK)) {
                // Could not find transaction. Generate an event
                // with a null transaction identifier.
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "Processing ACK for dialog " + dialog);
                }
                
                if (dialog == null) {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "Dialog does not exist "
                            + sipRequest.getFirstLine() +
                            " isServerTransaction = " + true);
                    }

                    transaction =
                            sipStack.findTransaction(sipRequest, true);
                } else if (dialog.getLastAck() != null &&
                        dialog.getLastAck().equals(sipRequest)) {
                    if (sipStack.isRetransmissionFilterActive()) {
                        dialog.ackReceived(sipRequest);
                        transaction.setDialog(dialog);
                        
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180,
                                "Retransmission Filter enabled -" 
                                + " dropping Ack retransmission");
                        }
                        
                        // filter out retransmitted acks if
                        // retransmission filter is enabled.
                        return;
                    }
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "ACK retransmission for 2XX "
                            + "response "
                            + "Sending ACK to the TU");
                    }
                } else {
                    //	This could be a re-invite processing.
                    // check to see if the ack matches with the last
                    // transaction.
                    
                    Transaction tr = dialog.getLastTransaction();
                    Response sipResponse = tr.getLastResponse();
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180, "TRANSACTION:" + tr);
                    }
                    
                    if (tr instanceof ServerTransaction &&
                            sipResponse != null &&
                            sipResponse.getStatusCode() / 100 == 2
                            && sipResponse.getCSeqHeader().getSequenceNumber()
                            == sipRequest.getCSeqHeader().getSequenceNumber()) {
                        transaction.setDialog(dialog);
                        dialog.ackReceived(sipRequest);
                        
                        if (sipStack.isRetransmissionFilterActive() &&
                                tr.isAckSeen()) {
                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                    LogChannels.LC_JSR180,
                                    "ACK retransmission for"
                                    + " 2XX response --- "
                                    + "Dropping ");
                            }
                            return;
                        } else {
                            // record that we already saw an ACK for
                            // this transaction.
                            tr.setAckSeen();
                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                    LogChannels.LC_JSR180,
                                    "ACK retransmission for 2XX "
                                    + "response --- "
                                    + "sending to TU ");
                            }
                        }
                        
                    } else {
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180,
                                "ACK retransmission for non"
                                + " 2XX response "
                                + "Discarding ACK");
                        }
                        
                        // Could not find a transaction.
                        if (tr == null) {
                            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                                Logging.report(Logging.WARNING,
                                    LogChannels.LC_JSR180,
                                    "Could not find transaction ACK dropped");
                            }
                            return;
                        }
                        
                        transaction = tr;
                        if (transaction instanceof ClientTransaction) {
                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                    LogChannels.LC_JSR180,
                                    "Dropping late ACK");
                            }
                            return;
                        }
                    }
                }
            } else if (sipRequest.getMethod().equals(Request.BYE)) {
                transaction = this.transactionChannel;
                // If the stack has not mapped this transaction because
                // of sequence number problem then just drop the BYE
                if (transaction != null &&
                       ((ServerTransaction)transaction).isTransactionMapped()) {
                    // Get the dialog identifier for the bye request.
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180, "dialogId = " + dialogId);
                    }
                    
                    // Find the dialog identifier in the SIP stack and
                    // mark it for garbage collection.
                    if (dialog != null) {
                        // Remove dialog marks all
                        dialog.addTransaction(transaction);
                    } else {
                        dialogId = sipRequest.getDialogId(false);
                        
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180,
                                "dialogId = " + dialogId);
                        }
                        
                        dialog = sipStack.getDialog(dialogId);
                        
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180, "dialog = " + dialog);
                        }
                        
                        if (dialog != null) {
                            dialog.addTransaction(transaction);
                        } else {
                            transaction = null;
                            // pass up to provider for
                            // stateless handling.
                        }
                    }
                } else if (transaction != null)  {
                    // This is an out of sequence BYE
                    // transaction was allocated but
                    // not mapped to the stack so
                    // just discard it.
                    if (dialog != null) {
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180,
                                "Dropping out of sequence BYE");
                        }
                        return;
                    } else transaction = null;
                }
                // note that the transaction may be null (which
                // happens when no dialog for the bye was fund.
            } else if (sipRequest.getRequestLine().getMethod().equals
                    (Request.CANCEL)) {
                
                // The ID refers to a previously sent
                // INVITE therefore it refers to the
                // server transaction table.
                // Find the transaction to cancel.
                // Send a 487 for the cancel to inform the
                // other side that we've seen it but do not send the
                // request up to the application layer.
                
                // Get rid of the CANCEL transaction -- we pass the
                // transaciton we are trying to cancel up to the TU.
                
                // Antonis Karydas: Suggestion
                // 'transaction' here refers to the transaction to
                // be cancelled. Do not change
                // it's state because of the CANCEL.
                // Wait, instead for the 487 from TU.
                // transaction.setState(Transaction.TERMINATED_STATE);
                
                ServerTransaction serverTransaction =
                        (ServerTransaction)
                        sipStack.findCancelTransaction(sipRequest, true);
                
                // Generate an event
                // with a null transaction identifier.
                if (serverTransaction == null) {
                    // Could not find the invite transaction.
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "transaction "
                            + " does not exist "
                            + sipRequest.getFirstLine()
                            + "isServerTransaction = "
                            + true);
                    }
                    transaction = null;
                } else {
                    transaction = serverTransaction;
                }
            }
            
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "-----------------");
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    sipRequest.toString());
            }
            
            if (dialog != null &&
                    transaction != null &&
                    ! sipRequest.getMethod().equals(Request.BYE) &&
                    ! sipRequest.getMethod().equals(Request.CANCEL) &&
                    ! sipRequest.getMethod().equals(Request.ACK)) {
                // already dealt with bye above.
                // Note that route updates are only effective until
                // Dialog is in the confirmed state.
                if (dialog.getRemoteSequenceNumber() >=
                        sipRequest.getCSeqHeader().getSequenceNumber()) {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "Dropping out of sequence message " +
                            dialog.getRemoteSequenceNumber() +
                            " "  + sipRequest.getCSeqHeader());
                    }
                    
                    return;
                }
                
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
            }
            
            
            RequestEvent sipEvent;
            
            if (dialog == null && sipRequest.getMethod().equals
                    (Request.NOTIFY)) {
                ClientTransaction ct =
                        sipStack.findSubscribeTransaction(sipRequest);
                // From RFC 3265
                // If the server transaction cannot be found or if it
                // aleady has a dialog attached to it then just assign the
                // notify to this dialog and pass it up.
                if (ct != null) {
                    transaction.setDialog(ct.getDialog());
                    if (ct.getDialog().getState() == Dialog.INITIAL_STATE) {
                        sipEvent = new RequestEvent
                                (sipProvider, null, (Request) sipRequest);
                    } else {
                        sipEvent = new RequestEvent
                                (sipProvider,
                                (ServerTransaction)transaction,  sipRequest);
                    }
                } else {
                    // Got a notify out of the blue - just pass it up
                    // for stateless handling by the application.
                    sipEvent = new RequestEvent
                            (sipProvider, null,  sipRequest);
                }
                
            } else {
                // For a dialog creating event - set the transaction to null.
                // The listener can create the dialog if needed.
                if (transaction != null &&
                        ((ServerTransaction) transaction).isTransactionMapped())
                    sipEvent = new RequestEvent(sipProvider,
                            (ServerTransaction) transaction,
                            sipRequest);
                else sipEvent = new RequestEvent
                        (sipProvider, null,  sipRequest);
            }
            sipProvider.handleEvent(sipEvent,  transaction);
        }
        
    }
    
    /**
     * Process the response.
     * @param sipResponse the response message
     * @param incomingMessageChannel  message channel on which the
     * response is received.
     * @exception SIPServerException is thrown when there is an error
     * processing the response
     */
    public void processResponse(Response sipResponse,
            MessageChannel incomingMessageChannel)
            throws SIPServerException {
        try {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "PROCESSING INCOMING RESPONSE" + sipResponse.encode());
            }

            /*
             * What if the listeningPoint of the incomingMessagechannel
             * different than the one that set during initialization in
             * NistSipMesageFactoryImpl?
             * So, initialize it explicitely for the incomingMessageChannel.
             */
            listeningPoint =
               incomingMessageChannel.getMessageProcessor().getListeningPoint();

            if (listeningPoint == null) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "Dropping message: No listening point registered!");
                }
                return;
            }

            Transaction transaction = this.transactionChannel;
            SipProvider sipProvider = listeningPoint.getProvider();
            
            if (sipProvider == null) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "Dropping message:  no provider");
                }
                return;
            }
            
            SipStack sipStack = sipProvider.sipStack;
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "Transaction = " + transaction);
            }
            
            if (this.transactionChannel == null) {
                String dialogId = sipResponse.getDialogId(false);
                Dialog dialog = sipStack.getDialog(dialogId);

                //  Have a dialog but could not find transaction.
                if (sipProvider.sipListener == null) {
                    return;
                } else if (dialog != null) {
                    if (sipResponse.getStatusCode() != SIPErrorCodes.OK) {
                        return;
                    } else if (sipStack.isRetransmissionFilterActive()) {
                        // 200  retransmission for the final response.
                        if (sipResponse.getCSeqHeader().equals(
                                dialog.getFirstTransaction().getRequest().
                                    getHeader(Header.CSEQ))) {
                            dialog.resendAck();
                            return;
                        }
                    }
                }
                
                // long receptionTime = System.currentTimeMillis();
                // Pass the response up to the application layer to handle
                // statelessly.
                
                // Dialog is null so this is handled statelessly
                ResponseEvent sipEvent =
                      new ResponseEvent(sipProvider, null, sipResponse);
                sipProvider.handleEvent(sipEvent, transaction);
                // transaction.logResponse(sipResponse,
                //       receptionTime,"Retransmission");
                return;
            }
            
            this.rawMessageChannel = incomingMessageChannel;
            
            String method = sipResponse.getCSeqHeader().getMethod();
            // Retrieve the client transaction for which we are getting
            // this response.
            ClientTransaction clientTransaction =
                    (ClientTransaction) this.transactionChannel;
            
            Dialog dialog = null;
            if (transaction != null) {
                dialog =  transaction.getDialog();
                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION &&
                        dialog == null) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "dialog not found for " + sipResponse.getFirstLine());
                }
            }
            
            // SipListener sipListener = sipProvider.sipListener;
            
            ResponseEvent responseEvent = new ResponseEvent
                    (sipProvider, (ClientTransaction)transaction, sipResponse);
            sipProvider.handleEvent(responseEvent, transaction);
        } catch (NullPointerException ex) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "null ptr");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gets the sender channel.
     * @return the request channel
     */
    public MessageChannel getRequestChannel() {
        return this.transactionChannel;
    }
    
    /**
     * Gets the channel if we want to initiate a new transaction to
     * the sender of  a response.
     * @return a message channel that points to the place from where we got
     * the response.
     */
    public MessageChannel getResponseChannel() {
        if (this.transactionChannel != null)
            return this.transactionChannel;
        else return this.rawMessageChannel;
    }
    
    /**
     * Just a placeholder. This is called from the stack
     * for message logging. Auxiliary processing information can
     * be passed back to be  written into the log file.
     * @return auxiliary information that we may have generated during the
     * message processing which is retrieved by the message logger.
     * (Always returns null)
     */
    public String getProcessingInfo() {
        return null;
    }
}
