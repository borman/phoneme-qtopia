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

import gov.nist.siplite.message.Request;
import gov.nist.siplite.message.Response;
import gov.nist.siplite.stack.ClientTransaction;
import gov.nist.siplite.stack.Dialog;
import gov.nist.siplite.stack.ServerTransaction;
import gov.nist.siplite.stack.Transaction;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Event scanner.
 */
public class EventScanner implements Runnable {
    /** Current SIP stack context. */
    private SipStack sipStack;
    /** Vector of pending events. */
    private Vector pendingEvents;
    /** Current SIP event listener. */
    private SipListener sipListener;
    /** Flag to indicate if event processing is stopped. */
    private boolean isStopped;

    /**
     * Constructor.
     * @param sipStack the current transaction context
     */
    public EventScanner(SipStack sipStack) {
        this.sipStack = sipStack;
        this.pendingEvents = new Vector();
    }

    /**
     * Starts the scanning for events.
     */
    public void start() {
        Thread myThread = new Thread(this);
        myThread.setPriority(Thread.MAX_PRIORITY);
        myThread.start();
    }

    /**
     * Stops the scanning for events.
     */
    public void stop() {
        synchronized (this.pendingEvents) {
            this.isStopped = true;
            this.pendingEvents.notify();
        }
    }

    /**
     * Adds a new event to be processed.
     * @param eventWrapper the event filter handler to be added
     */
    public void addEvent(EventWrapper eventWrapper) {
        synchronized (pendingEvents) {
            pendingEvents.addElement(eventWrapper);
            pendingEvents.notify();
        }
    }

    /**
     * Starts the scanner procesing.
     */
    public void run() {
        while (true) {
            SipEvent sipEvent = null;
            EventWrapper eventWrapper = null;
            
            synchronized (this.pendingEvents) {
                if (pendingEvents.isEmpty()) {
                    try {
                        pendingEvents.wait();
                    } catch (InterruptedException ex) {
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180, "Interrupted!");
                        }
                        continue;
                    }
                }

                if (this.isStopped) {
                    return;
                }

                SipListener sipListener = sipStack.getSipListener();
                Enumeration iterator = pendingEvents.elements();
                
                while (iterator.hasMoreElements()) {
                    eventWrapper = (EventWrapper) iterator.nextElement();
                    sipEvent = eventWrapper.sipEvent;

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "Processing "
                            + sipEvent
                            + "nevents "
                            + pendingEvents.size());
                    }

                    try {
                        if (sipEvent instanceof RequestEvent) {
                            // Check if this request has already created a
                            // transaction
                            Request sipRequest = (Request)
                                ((RequestEvent) sipEvent).getRequest();
                            // Check if this request has already created a
                            // transaction. If this is a dialog creating
                            // method for which a server transaction
                            // already exists or a method which is not
                            // dialog creating and not within an existing
                            // dialog (special handling for cancel) then
                            // check to see if the listener already
                            // created a transaction to handle this
                            // request and discard the duplicate request
                            // if a transaction already exists. If the
                            // listener chose to handle the request
                            // statelessly, then the listener will see the
                            // retransmission.  Note that in both of these
                            // two cases, JAIN SIP will allow you to
                            // handle the request statefully or
                            // statelessly.  An example of the latter case
                            // is REGISTER and an example of the former
                            // case is INVITE.


                            /*
                             * The transaction was added to the list
                             * in SIPTransactionStack, so it is not the right
                             * place to check if the transaction already exists.
                             *
                             * IMPL_NOTE: remove the following block of code after
                             *       ensuring that it doesn't break anything.
                             */

/*
                            if (sipStack.
                                isDialogCreated(sipRequest.getMethod())) {
                                SipProvider sipProvider =
                                    (SipProvider)sipEvent.getSource();
                                sipProvider.currentTransaction =
                                    (ServerTransaction)
                                        eventWrapper.transaction;
                                ServerTransaction tr = (ServerTransaction)
                                    sipStack.findTransaction(sipRequest, true);
                                Dialog dialog = sipStack.getDialog
                                    (sipRequest.getDialogId(true));

                                if (tr != null && !tr.passToListener()) {
                                    if (Logging.REPORT_LEVEL <=
                                        Logging.INFORMATION) {
                                        Logging.report(Logging.INFORMATION,
                                        LogChannels.LC_JSR180,
                                        "transaction already exists!");
                                    }
                                    continue;
                                }
                            } else if (!sipRequest.getMethod()
                                .equals(Request.CANCEL) &&
                                sipStack.getDialog(sipRequest
                                .getDialogId(true))
                                == null) {
                                // not dialog creating and not a cancel.
                                // transaction already processed this message.
                                Transaction tr = sipStack
                                    .findTransaction(sipRequest,
                                    true);
                                //
                                // Should this be allowed?
                                // SipProvider sipProvider =
                                //     (SipProvider) sipEvent.getSource();
                                // sipProvider.currentTransaction =
                                // (ServerTransaction) eventWrapper.transaction;
                                // If transaction already exists bail.
                                if (tr != null) {
                                    if (Logging.REPORT_LEVEL <=
                                        Logging.INFORMATION) {
                                        Logging.report(Logging.INFORMATION,
                                        LogChannels.LC_JSR180,
                                        "transaction already exists!");
                                    }
                                    continue;
                                }
                            }
*/

                            // Processing incoming CANCEL.
                            if (sipRequest.getMethod().equals(Request.CANCEL)) {
                                Transaction tr =
                                    sipStack.findTransaction(sipRequest, true);
                                if (tr != null &&
                                    tr.getState() ==
                                    Transaction.TERMINATED_STATE) {
                                    // If transaction already exists but it is
                                    // too late to cancel the transaction then
                                    // just respond OK to the CANCEL and bail.
                                    if (Logging.REPORT_LEVEL <=
                                        Logging.INFORMATION) {
                                        Logging.report(Logging.INFORMATION,
                                        LogChannels.LC_JSR180,
                                        "Too late to cancel Transaction");
                                    }

                                    // send OK and just ignore the CANCEL.
                                    try {
                                        tr.sendMessage
                                            (sipRequest
                                            .createResponse(SIPErrorCodes.OK));
                                    } catch (IOException ex) {
                                        // Ignore?
                                    }
                                    continue;
                                }
                            }

                            sipListener.processRequest
                                ((RequestEvent) sipEvent);
                        } else if (sipEvent instanceof ResponseEvent) {
                            sipListener.processResponse
                                ((ResponseEvent) sipEvent);
                            ClientTransaction ct =
                                ((ResponseEvent) sipEvent).
                                    getClientTransaction();
                            ct.clearEventPending();
                        } else if (sipEvent instanceof TimeoutEvent) {
                            sipListener.processTimeout
                                ((TimeoutEvent) sipEvent);
                            // Mark that Timeout event has been processed
                            if (eventWrapper.transaction != null) {
                                if (eventWrapper.transaction instanceof
                                    ClientTransaction) {
                                    ((ClientTransaction) eventWrapper.
                                        transaction).clearEventPending();
                                }
                            }
                        } else {
                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                    LogChannels.LC_JSR180, "bad event");
                            }
                        }
                    } catch (Throwable exc) { // ignore
                        if (Logging.REPORT_LEVEL <=
                            Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "Uncaught exception ");
                        }
                    }
                }

                pendingEvents.removeAllElements();
            } // end of Synchronized block
        } // end While
    }

}

