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
 *   
 *
 * Created on Jan 29, 2004
 *
 */
package gov.nist.microedition.sip;

import gov.nist.siplite.message.*;
import gov.nist.siplite.address.URI;
import gov.nist.siplite.header.Header;
import gov.nist.siplite.header.AuthorizationHeader;
import gov.nist.siplite.header.ProxyAuthorizationHeader;
import gov.nist.siplite.header.SubscriptionStateHeader;
import gov.nist.siplite.stack.Dialog;
import gov.nist.siplite.stack.Subscription;
import gov.nist.siplite.parser.Lexer;
import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipClientConnectionListener;
import javax.microedition.sip.SipConnection;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipDialog;
import javax.microedition.sip.SipException;
import com.sun.j2me.security.Token;

/**
 * SIP Dialog implementation.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class SipDialogImpl implements SipDialog {
    /**
     * Initialized, initial state of dialog. This state is initialized
     * in this class instead of SipDialog interface as it is an internal
     * state
     */
    protected static final int INITIALIZED = -1;
    /**
     * current state of the dialog
     */
    private byte state;
    /**
     * dialog ID. Refer to RFC 3261 page 69 for the 
     * exact format of the dialog ID.
     */
    private String dialogID = null;
    /**
     * This implementation of dialog is linked to the Nist-Siplite dialog
     */
    protected Dialog dialog = null;
    /**
     * Current handle to asynchronous notifier.
     */
    private SipConnectionNotifier sipConnectionNotifier = null;
    /**
     * Handle for current listener.
     */
    private SipClientConnectionListener sipClientConnectionListener = null;
    /** Proxy server autorization headers. */
    protected ProxyAuthorizationHeader proxyAuthorizationHeader = null;
    /** Authorization header key. */
    protected AuthorizationHeader authorizationHeader = null;
    /**
     * Security token for SIP/SIPS protocol class
     */
    private Token classSecurityToken;
    /**
     * The refresh ID of the refresh task associated with this client connection
     * if there is any
     */
    private String refreshID = null;
    /**
     * Permission check before sending a PRACK request.
     */
    protected boolean isReliableProvReceived = false;
    /**
     * True if this dialog can't be terminated until BYE is received.
     */
    private boolean waitForBye = false;

    /**
     * Constructs this dialog based upon the Nist-Siplite dialog
     * @param dialog Nist-Siplite dialog
     * @param sipConnectionNotifier the notification handler
     * @param classSecurityToken Security token for SIP/SIPS protocol class
     * with this client connection
     */
    protected SipDialogImpl(Dialog dialog,
            SipConnectionNotifier sipConnectionNotifier,
            Token classSecurityToken) {
        state = INITIALIZED;

        if ((dialog != null) && (dialog.getState() != Dialog.INITIAL_STATE)) {
            dialogID = dialog.getDialogId();
            int underlyingDlgState = dialog.getState();

            if (underlyingDlgState == Dialog.CONFIRMED_STATE) {
                state = CONFIRMED;
            } else if (underlyingDlgState == Dialog.COMPLETED_STATE ||
                       underlyingDlgState == Dialog.TERMINATED_STATE) {
                state = TERMINATED;
            }
        }

        this.dialog = dialog;
        this.sipConnectionNotifier = sipConnectionNotifier;
        this.classSecurityToken = classSecurityToken;
    }

    /**
     * Returns new SipClientConnection in this dialog.
     * The SipClientConnection will be pre-initialized with the given
     * method and
     * following headers will be set at least
     * (for details see RFC 3261 [1] 12.2.1.1 Generating the Request, p.73):
     * <pre>
     * To
     * From
     * CSeq
     * Call-ID
     * Max-Forwards
     * Via
     * Contact
     * Route//ifthedialogrouteisnotempty
     * </pre>
     * @param method - given method
     * @return SipClientConnection with preset headers.
     * @throws IllegalArgumentException - if the method is invalid
     * @throws NullPointerException - if method name is null
     * @throws SipException - INVALID_STATE if the new connection can not be
     * established in the current state of dialog. TRANSACTION_UNAVAILABLE if 
     * the creation of the SipClientConnection object is not possible 
     * for any reason.
     * @throws NullPointerException if the method name is null
     */
    public SipClientConnection getNewClientConnection(String method)
            throws SipException {

        // JSR180: all methods are available in CONFIRMED and EARLY states.
        if (state != SipDialog.CONFIRMED && state != SipDialog.EARLY) {
            throw new SipException("the client connection can not "
                    + "be initialized, because of wrong state.",
                    SipException.INVALID_STATE);
        }

        if (method == null) {
            throw new NullPointerException("The method can not be null");
        }

        // Validating the method.
        if (!Lexer.isValidName(method)) {
            throw new IllegalArgumentException("Invalid method: '" +
                method + "'");
        }

        // Create the new sip client connection
        // and init the request
        SipClientConnection sipClientConnection =
                new SipClientConnectionImpl(
                    getDialog().getRemoteTarget().getURI(), this);
        // ((SipClientConnectionImpl)sipClientConnection).start();

        if ((sipConnectionNotifier != null) &&
            !((SipConnectionNotifierImpl)sipConnectionNotifier).
                isConnectionOpen()) {
            sipConnectionNotifier = null;
        }

        ((SipClientConnectionImpl)sipClientConnection).initRequestImpl(
                method.toUpperCase().trim(), sipConnectionNotifier);

        // keep a trace of the connection created
        return sipClientConnection;
    }

    /**
     * Does the given SipConnection belong to this dialog.
     * @param sc - SipConnection to be checked, can be either
     * SipClientConnection or SipServerConnection
     * @return true if the SipConnection belongs to the this dialog.
     * Returns false
     * if the connection is not part of this dialog or the dialog is terminated.
     * @throws NullPointerException if sc is null
     */
    public boolean isSameDialog(SipConnection sc) {
        if (sc == null) {
            throw new NullPointerException("Argument is null");
        }
        
        if (state == SipDialog.TERMINATED) {
            return false;
        }

        SipDialog dlg = sc.getDialog();

        if (dlg != null) {
            String id = dlg.getDialogID();
            if (id != null && id.equals(dialogID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the state of the SIP Dialog.
     * @return dialog state byte number.
     */
    public byte getState() {
        return state;
    }

    /**
     * Returns the ID of the SIP Dialog.
     * @return Dialog ID. Refer to RFC 3261 page 69 for the exact format 
     * of the dialog ID.
     * Returns null if the dialog is terminated.
     */
    public String getDialogID() {
        if (state  ==  TERMINATED) {
            return null;
        }
        return dialogID;
    }

    /**
     * Sets the Dialog identifier.
     * @param newDialogID dialog identifier
     */
    protected void setDialogID(String newDialogID) {
        dialogID = newDialogID;
    }

    /**
     * Sets the current Dialog handler.
     * @param newDialog the new Dialog
     */
    protected void setDialog(Dialog newDialog) {
        dialog = newDialog;
        setDialogID(newDialog.getDialogId());
    }

    /**
     * Gets the curre SIP Dialog.
     * @return the current Dialog handle
     */
    protected Dialog getDialog() {
        return dialog;
    }

    /**
     * Sets the current connection listener.
     * @param newSipClientConnectionListener the new listener
     */
    protected void setSipClientConnectionListener(
            SipClientConnectionListener newSipClientConnectionListener) {
        sipClientConnectionListener = newSipClientConnectionListener;
    }

    /**
     * Gets the current listener.
     * @return the current listener
     */
    protected SipClientConnectionListener getSipClientConnectionListener() {
        return sipClientConnectionListener;
    }

    /**
     * Changes the state of this dialog
     * @param newState the new state of this dialog
     */
    protected void setState(byte newState) {
        state = newState;
    }

    /**
     * Changes the state of this dialog to TERMINATED
     * if there are no active subscriptions.
     */
    protected void terminateIfNoSubscriptions() {
        if (dialog == null) {
            setState(TERMINATED);
            return;
        }

        if (dialog.subscriptionList.isEmpty() && !waitForBye) {
            // TERMINATE the dialog
            setState(TERMINATED);
        }
    }

    /**
     * Adds a new subscription to the list of active subscriptions.
     * @param s a subscription to add
     */
    protected void addSubscription(Subscription s) {
        dialog.subscriptionList.addSubscription(s);
    }

    /**
     * Removes the subscription matching the given response or NOTIFY
     * from the list of active subscriptions.
     * @param message response or NOTIFY message
     */
    protected void removeSubscription(Message message) {
        Subscription s =
            dialog.subscriptionList.getMatchingSubscription(message);
        if (s != null) {
            dialog.subscriptionList.removeSubscription(s);
        }
    }

    /**
     * Accessor for 'waitForBye' field.
     * @param bye true if we have to wait until 'BYE' is received
     * to terminate the dialog, false otherwise
     */
    protected void setWaitForBye(boolean bye) {
        waitForBye = bye;
    }

    /**
     * Handles NOTIFY request.
     * @param request NOTIFY message
     * @param newDialog a new underlying dialog implementation
     * to associate with this dialog, may be null
     * @param newDialogId a new dialog id to set for this dialog
     */
    protected void handleNotify(Request request,
            Dialog newDialog, String newDialogId) {
        SubscriptionStateHeader ssh = (SubscriptionStateHeader)
            request.getHeader(Header.SUBSCRIPTION_STATE);

        if (ssh != null && ssh.isTerminated()) {
            Subscription s =
                dialog.subscriptionList.getMatchingSubscription(request);

            if (s != null) {
                dialog.subscriptionList.removeSubscription(s);
            }

            if (dialog.isSubscribeDialog() || dialog.isInviteDialog()) {
                // IMPL_NOTE: currently we don't handle the following scenario:
                //
                // INVITE/200OK - INVITE/200OK - SUBSCRIBE/200OK - BYE/200OK -
                // NOTIFY(terminate subscription)/200OK - (*) ...
                //
                // At the point (*) the dialog will be in TERMINATED state
                // what is incorrect.
                terminateIfNoSubscriptions();
            }
        } else {
            setState(CONFIRMED);

            if (newDialog != null) {
                setDialog(newDialog);
            } else {
                setDialogID(newDialogId);
            }
        }
    }

    /**
     * Gets the current security token.
     * @return the current security token
     */
    protected Token getSecurityToken() {
        return classSecurityToken;
    }

    /**
     * Gets the current refreshID.
     * @return the current refreshID
     */
    protected String getRefreshID() {
        return refreshID;
    }

    /**
     * Sets the current refreshID.
     * @param newRefreshID new refreshID value
     */
    protected void setRefreshID(String newRefreshID) {
        refreshID = newRefreshID;
    }
}
