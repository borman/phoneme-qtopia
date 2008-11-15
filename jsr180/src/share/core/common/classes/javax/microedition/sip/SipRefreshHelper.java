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

package javax.microedition.sip;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.IllegalArgumentException;
import javax.microedition.sip.SipException;

import gov.nist.core.ParseException;
import gov.nist.microedition.sip.RefreshManager;
import gov.nist.microedition.sip.RefreshTask;
import gov.nist.microedition.sip.StackConnector;
import gov.nist.microedition.sip.SipClientConnectionImpl;
import gov.nist.siplite.address.Address;
import gov.nist.siplite.header.Header;
import gov.nist.siplite.header.CallIdHeader;
import gov.nist.siplite.header.ContactHeader;
import gov.nist.siplite.header.ContactList;
import gov.nist.siplite.header.ContentLengthHeader;
import gov.nist.siplite.header.ContentTypeHeader;
import gov.nist.siplite.header.ExpiresHeader;
import gov.nist.siplite.header.CSeqHeader;
import gov.nist.siplite.message.Request;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This class implements the functionality that facilitates the handling of
 * refreshing requests on behalf of the application.
 * @see JSR180 spec, v 1.0.1, p 58-61
 *
 */
public class SipRefreshHelper {
    /**
     * The unique instance of this class
     */
    private static SipRefreshHelper instance = null;
    /** Refresh manager. */
    private static RefreshManager refreshManager = null;

    /**
     * Hide default constructor.
     */
    private SipRefreshHelper() {
    }
    
    /**
     * Returns the instance of SipRefreshHelper
     * @see JSR180 spec, v 1.1.0, p 75
     *
     * @return the instance of SipRefreshHelper singleton
     */
    public static javax.microedition.sip.SipRefreshHelper getInstance() {
        if (instance == null) {
            instance = new SipRefreshHelper();
            refreshManager = RefreshManager.getInstance();
        }

        return instance;
    }

    /**
     * Stops refreshing a specific request related to refeshID. 
     * @see JSR180 spec, v 1.1.0, p 75
     *
     * @param refreshID the ID of the refresh to be stopped. If the ID
     * does not match any refresh task the method does nothing.
     * @throws SipException INVALID_STATE if the refreshID doesn't represent 
     * an ongoing refresh operation (e.g. the refresh is already stopped by 
     * the application or because of an error) or stop can not be called 
     * in the current state of the refresh operation.
     */
    public void stop(int refreshID) throws SipException {
        boolean reportStop = false;
        RefreshTask refreshTask = getRefreshTask(refreshID);
        if (refreshTask == null) {
            throw new SipException("refreshID doesn't represent " +
                                   "an ongoing refresh operation",
                                   SipException.INVALID_STATE);
        }
        if (!connectionStateIsOK(refreshTask)) {
             throw new SipException("the request can not be initialized," +
                                   "because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        Request requestNotCloned = refreshTask.getRequest();
        Request request = (Request)requestNotCloned.clone();
        requestNotCloned = null;
        reportStop = removeBindings(refreshTask, request);
        doStop(refreshID, refreshTask);
    }
    
    /**
     * Updates one refreshed request with new values.
     * @see JSR180 spec, v 1.1.0, p 75
     *
     * @param type value of Content-Type (null or empty, no content)
     * @param length value of Content-Length (<=0, no content)
     * @param expires value of Expires (-1, no Expires header),
     * (0, stop the refresh)
     * @return Returns the OutputStream to fill the content. If the update does
     * not have new content (type = null and/or length = 0) method returns null
     * and the message is sent automatically.
     * @throws java.lang.IllegalArgumentException if some input parameter
     * is invalid
     * @throws SipException INVALID_STATE if the refreshID doesn't represent 
     * an ongoing refresh operation (e.g. the refresh is already stopped by 
     * the application or because of an error) or update can not be called 
     * in the current state of the refresh operation.
     */
    public java.io.OutputStream update(
            int refreshID,
            java.lang.String[] contact,
            java.lang.String type,
            int length,
            int expires) throws SipException {
        /* expires = 0 has the same effect as calling stop(refreshID) */
        if (expires == 0) {
                stop(refreshID);
                
                /*
                 * Ambiguousness in the JSR180 spec:
                 * "expires = 0 has the same effect as calling stop(refreshID)"
                 * But: "update() returns the OutputStream to fill the content.
                 * If the update does not have new content (type = null and/or
                 * length = 0) method returns null and the message is sent
                 * automatically".
                 * It is not clear what to do if the expires is 0 and the
                 * content is not null. If the refresh is stopped, we can't
                 * provide the OutputStream to fill the content, 
                 * so return null.
                 */
                return null;
        }        

        RefreshTask refreshTask = getRefreshTask(refreshID);
        if (refreshTask == null) {
            throw new SipException("refreshID doesn't represent " +
                                   "an ongoing refresh operation",
                                   SipException.INVALID_STATE);
        }
        if (!connectionStateIsOK(refreshTask)) {
             throw new SipException("the request can not be initialized," +
                                   "because of wrong state.",
                                   SipException.INVALID_STATE);
        }

        // System.out.println(">>> update, type = " + type);

        Request requestNotCloned = refreshTask.getRequest();
        Request request = (Request)requestNotCloned.clone();
        requestNotCloned = null;

        // Contacts
        if (contact != null && contact.length != 0) {
            // JSR180: contact replaces all old values.
            // Multiple Contact header values are applicable
            // only for REGISTER method.
            if (contact.length > 1 &&
                    !request.getMethod().equals(Request.REGISTER)) {
                throw new IllegalArgumentException(
                    "Only one contact is allowed for non-REGISTER requests.");
            }

            // Remove old contacts from the request.
            ContactList cl = request.getContactHeaders();
            int n = (cl != null) ? cl.size() : 0;

            for (int i = 0; i < n; i++) {
                request.removeHeader(ContactHeader.NAME);
            }

            // Add new contacts to the request.
            for (int i = 0; i < contact.length; i++) {
                String contactURI = contact[i];
                Address address = null;

                try {
                    address = StackConnector.addressFactory
                            .createAddress(contactURI);
                } catch (ParseException pe) {
                    throw new
                            IllegalArgumentException("one of the contact "
                            + "addresses is not valid");
                }

                try {
                    ContactHeader contactHeader = StackConnector.headerFactory.
                        createContactHeader(address);
                    request.addHeader(contactHeader);
                } catch (SipException ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        }

        // Expires
        if (expires == -1) {
            request.removeHeader(ExpiresHeader.NAME);
        } else if (expires >= 0) {
            ExpiresHeader eh = (ExpiresHeader)request.getHeader(
                                                  ExpiresHeader.NAME);
            if (eh == null) {
                // Try to add an ExpiresHeader
                try {
                    eh = StackConnector.headerFactory.
                            createExpiresHeader(expires);
                    request.addHeader(eh);
                } catch (SipException ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }

            eh.setExpires(expires);
        } else {
            throw new
                IllegalArgumentException("the expires value is not correct");
        }

        // Content Length
        ContentLengthHeader contentLengthHeader =
                request.getContentLengthHeader();

        if (contentLengthHeader == null) {
            try {
                request.addHeader(StackConnector.headerFactory.
                    createContentLengthHeader(0));
            } catch (SipException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        // Content Type
        if (length > 0 && type != null && !type.equals("")) {
            request.removeHeader(Header.CONTENT_TYPE);

            Exception ex = null;
            try {
                ContentTypeHeader contentTypeHeader = (ContentTypeHeader)
                    StackConnector.headerFactory.createHeader(
                        Header.CONTENT_TYPE, type);
                request.addHeader(contentTypeHeader);
            } catch (ParseException pe) {
                ex = pe;
            } catch (SipException se) {
                ex = se;
            }
            if (ex != null) {
                throw new IllegalArgumentException(ex.getMessage());
            }

            request.getContentLengthHeader().setContentLength(length);
        } else {
            request.removeContent();
        }

        // Call-Id header was added in SipClientConnectionImpl.send()
        // when the initial request was sent. This is the reason why
        // Call-Id header is not added here.

        // Cancel the timer
        refreshTask.cancel();

        SipClientConnectionImpl sipClientConnection =
                (SipClientConnectionImpl)refreshTask.getSipClientConnection();

        if (type == null || type.equals("") || length <= 0) {
            // If the is no new content, send the message automatically
            // Don't call sipClientConnection.send() directly
            // because CSeq header must be updated.
            try {
                refreshTask.updateAndSendRequest(request);
            } catch (IOException ex) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "SipRefreshHelper.update(): " + ex);
                }
            }

            return null;
        } else {
            OutputStream contentOutputStream = null;

            try {
                // set the content
                // contentOutputStream = sipClientConnection
                //         .openContentOutputStream();
                contentOutputStream =
                    refreshTask.updateRequestAndOpenOutputStream(request);
            } catch (IOException ioe) {
                return null;
            }

            return contentOutputStream;
        }
    }
    
    /**
     * Stop and update methods can not be called in every phase of the 
     * refresh operation. See JSR180 spec, v 1.1.0, p 75, 76 for details.
     * @param refreshTask required to get connection state
     * @return true if it is appropriate state, 
     * false if it is inappropriate state
     */
    private boolean connectionStateIsOK(RefreshTask refreshTask) {
        return ((SipClientConnectionImpl)refreshTask.
                    getSipClientConnection()).getUpdateState();
    }


    /**
     * Finds a refresh task corresponding to the given refreshID.
     * @param refreshID the ID of the refresh.
     * @return RefreshTask object that corresponds to the given refreshID
     * or null if there is no such task.
     */
    private RefreshTask getRefreshTask(int refreshID) {
        String taskId;

        try {
            taskId = String.valueOf(refreshID);
        } catch (NumberFormatException nfe) {
            return null;
        }

        return refreshManager.getTask(taskId);
    }

    /**
     * Stops refreshing a specific request related to refeshID without
     * removing the possible binding between end point and registrar/notifier.
     * @param refreshID the ID of the refresh to be stopped. If the ID
     * does not match any refresh task the method does nothing.
     * @param refreshTask refresh task that corresponds to the given refreshID.
     */
    private void doStop(int refreshID, RefreshTask refreshTask) {
        String taskId;

        try {
            taskId = String.valueOf(refreshID);
        } catch (NumberFormatException nfe) {
            return;
        }

        // Cancel the timer
        refreshTask.cancel();
        refreshTask.getSipRefreshListener().refreshEvent(
                refreshID,
                0,
                "refresh stopped");
        refreshManager.removeTask(taskId);
    }

    /**
     * Removes the possible binding between end point and registrar/notifier
     * as described in RFC 3261, section 10.2.2, Removing Bindings:
     * Registrations are soft state and expire unless refreshed, but can also
     * be explicitly removed. A client can attempt to influence the expiration
     * interval selected by the registrar as described in Section 10.2.1. A UA
     * requests the immediate removal of a binding by specifying an expiration
     * interval of "0" for that contact address in a REGISTER request.
     * @param refreshTask the refresh task that refreshes the given request.
     * @param request the request that will be sent with the header Expires = 0.
     * @return true if no IOException has occured
     * @throws SipException INVALID_STATE if the refreshID doesn't represent 
     * an ongoing refresh operation (e.g. the refresh is already stopped by 
     * the application or because of an error) or stop can not be called 
     * in the current state of the refresh operation.
     */
    private boolean removeBindings(RefreshTask refreshTask, Request request)
            throws SipException {
        ExpiresHeader eh = (ExpiresHeader)request.getHeader(ExpiresHeader.NAME);

        if (eh == null) {
            Exception ex = null;
            // Try to add an ExpiresHeader
            try {
                eh = StackConnector.headerFactory.createExpiresHeader(0);
                request.addHeader(eh);
            } catch (SipException se) {
                ex = se;
            } catch (IllegalArgumentException iae) {
                ex = iae;
            }
            if (ex != null) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "SipRefreshHelper.removeBindings(): " + ex);
                }
                return true;
            }
        }

        eh.setExpires(0);

        // Update the request of the sipClientConnection
        // and send it immediately.

        try {
            refreshTask.updateAndSendRequest(request);
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                    "SipRefreshHelper.removeBindings(): " + ex);
            }
            return false;
        }
        return true;
    }
}
