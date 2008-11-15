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
 * RefreshTask.java
 *
 * Created on Apr 8, 2004
 *
 */
package gov.nist.microedition.sip;

import gov.nist.core.ParseException;
import gov.nist.siplite.SIPErrorCodes;
import gov.nist.siplite.header.Header;
import gov.nist.siplite.header.CallIdHeader;
import gov.nist.siplite.header.CSeqHeader;
import gov.nist.siplite.header.ViaHeader;
import gov.nist.siplite.message.Request;
import gov.nist.microedition.sip.RefreshManager;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipException;
import javax.microedition.sip.SipRefreshHelper;
import javax.microedition.sip.SipRefreshListener;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Refreshs the transaction state.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RefreshTask extends TimerTask {
    /** The current request to be processed. */
    private Request request = null;
    /** The associated connection client. */
    private SipClientConnection sipClientConnection = null;
    /** The connection state notifier. */
    private SipConnectionNotifier sipConnectionNotifier = null;
    /** The current refresh event listener. */
    private SipRefreshListener sipRefreshListener;
    /** The task identifier. */
    private String taskId;

    /**
     * Creates a new instance of RefreshTask
     * @param id the task identifier
     * @param rq the request to resend
     * @param scn the connection used to send the request
     * @param listener the callback interface used listening for
     * refresh event on this task
     * @param scc the connection to update
     */
    public RefreshTask(
                      String id,
                      Request rq,
                      SipConnectionNotifier scn,
                      SipRefreshListener listener,
                      SipClientConnection scc) {
        taskId = id;
        request = rq;
        sipConnectionNotifier = scn;
        sipRefreshListener = listener;
        sipClientConnection = scc;
    }

    /**
     * Run the refresh thread.
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Request clonedRequest = (Request)request.clone();
        // setRequestHeaders(clonedRequest);
        request = null;

        try {
            updateAndSendRequest(clonedRequest);
        } catch (Exception ex) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                           "RefreshTask.run(): can't send a message: " + ex);
            }
        }
    }

    /**
     * Sets appropriate CSeq and Via headers in the given request
     * preparing it for sending.
     * @param clonedRequest the request to modify
     */
    public synchronized void setRequestHeaders(Request clonedRequest) {
        // RFC 3261, section 10.2.4, Refreshing Bindings:
        // "A UA SHOULD use the same Call-ID for all registrations during a
        // single boot cycle".
        //
        // Call-Id header was added in SipClientConnectionImpl.send()
        // when the initial request was sent. This is the reason why
        // Call-Id header is not added here.

        // Update the CSeq header. RFC 3261, p. 58:
        // A UA MUST increment the CSeq value by one for each
        // REGISTER request with the same Call-ID.
        CSeqHeader cseq = clonedRequest.getCSeqHeader();

        if (cseq != null) {
            cseq.setSequenceNumber(cseq.getSequenceNumber() + 1);
        } else {
            // log an error
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "RefreshTask.run(): The request doesn't " +
                               "contain CSeq header!");
            }
        }

        // ViaHeader
        clonedRequest.removeHeader(ViaHeader.NAME);

        Vector viaHeaders = new Vector();
        try {
            ViaHeader viaHeader = StackConnector
                                  .headerFactory
                                  .createViaHeader(
                                      sipConnectionNotifier.getLocalAddress(),
                                      sipConnectionNotifier.getLocalPort(),
                                      ((SipConnectionNotifierImpl)
                                       sipConnectionNotifier)
                                      .getSipProvider().getListeningPoint()
                                      .getTransport(),
                                      null);
            viaHeaders.addElement(viaHeader);
        } catch (ParseException ex) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "RefreshTask.run(): can't create Via header: " + ex);
            }
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                       "RefreshTask.run(): can't create Via header: " + ioe);
            }
        }

        try {
            clonedRequest.setVia(viaHeaders);
        } catch (SipException ex) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                       "RefreshTask.run(): can't set Via header: " + ex);
            }
        }
    }

    /**
     * Return the callback interface listening for events on this task
     * @return the callback interface listening for events on this task
     */
    public SipRefreshListener getSipRefreshListener() {
        return sipRefreshListener;
    }

    /**
     * Return the callback interface listening for events on this task
     * @return the callback interface listening for events on this task
     */
    public SipConnectionNotifier getSipConnectionNotifier() {
        return sipConnectionNotifier;
    }

    /**
     * Return the sipClientconnection on which is enabled the listener
     * @return the sipClientconnection on which is enabled the listener
     */
    public SipClientConnection getSipClientConnection() {
        return sipClientConnection;
    }

    /**
     * Updates the request in the sipClientConnection and sends it.
     * @param updatedRequest the updated request
     * @throws IOException if the message could not be sent or because
     * of network failure
     * @throws InterruptedIOException if a timeout occurs while
     * either trying to send the message or if this Connection object
     * is closed during this send operation
     * @throws SipException INVALID_STATE if the message cannot be sent
     * in this state. <br> INVALID_MESSAGE there was an error
     * in message format
     */
    public synchronized void updateAndSendRequest(Request updatedRequest)
            throws IOException, InterruptedIOException, SipException {
        request = updatedRequest;
        int refreshId;
        try {
            refreshId = Integer.parseInt(taskId);            
        } catch (NumberFormatException ne) {
            return;
        }
        request.setRefreshID(refreshId);
        setRequestHeaders(request);        
        try {
            ((SipClientConnectionImpl)getSipClientConnection()).
                    setUpdateState(false);
            ((SipClientConnectionImpl)sipClientConnection).
            updateAndSendRequestFromRefresh(request);
        } catch (IOException ex) { 
           /*
            * In case of failure of a refresh request the refreshing 
            * is automatically stopped and corresponding refresh ID 
            * is invalidated.
            */
            getSipRefreshListener().refreshEvent(
                refreshId, 
                SIPErrorCodes.SERVICE_UNAVAILABLE,
                SIPErrorCodes.
                getReasonPhrase(SIPErrorCodes.SERVICE_UNAVAILABLE));
            /* Cancel the timer */
            cancel();
            RefreshManager.getInstance().removeTask(taskId);
            throw ex;
        }
    }

    /**
     * Updates the request in the sipClientConnection and calls
     * SipClientConnection.openContentOutputStream() to fill
     * the new message body content.
     * @param updatedRequest the updated request
     * @return OutputStream to write body content
     * @throws IOException if the OutputStream can not be opened,
     * because of an I/O error occurred.
     * @throws SipException INVALID_STATE the OutputStream can not be opened
     * in this state (e.g. no message initialized).
     * UNKNOWN_LENGTH Content-Length header not set.
     * UNKNOWN_TYPE Content-Type header not set.
     */
    public synchronized OutputStream 
        updateRequestAndOpenOutputStream(Request updatedRequest)
        throws IOException, SipException {

        request = updatedRequest;
        setRequestHeaders(request);
        return ((SipClientConnectionImpl)sipClientConnection).
                    updateRequestAndOpenOutputStream(request);
    }

    /**
     * Gets the new caller identifier.
     * @return the caller identifier
     */
    public CallIdHeader getNewCallId() {
        return ((SipConnectionNotifierImpl)sipConnectionNotifier).
                                getSipProvider().getNewCallId();
    }

    /**
     * Returns the request to refresh
     * @return the request to refresh
     */
    public synchronized Request getRequest() {
        return request;
    }

    /**
     * Returns the task identifier
     * @return the task identifier
     */
    public String getTaskId() {
        return taskId;
    }
}
