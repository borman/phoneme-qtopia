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

import java.io.*;

/**
 * SipClientConnection represents SIP client transaction.
 * Application can create a new SipClientConnection with Connector 
 * or SipDialog object. 
 * @see JSR180 spec, v 1.0.1, p 21-33
 *
 */
public interface SipClientConnection extends SipConnection {
    
    /**
     * Initializes SipClientConnection to a specific SIP 
     * request method (REGISTER, INVITE, MESSAGE, ...).  
     * @see JSR180 spec, v 1.1.0, p 33-35
     *
     * @param method - Name of the method
     * @param scn - SipConnectionNotifier to which the request will be
     * associated. If SipConnectionNotifier is null the request will not be
     * associated to a user defined listening point.
     * @throws NullPointerException - if the method is null
     * @throws IllegalArgumentException - if the method is invalid
     * @throws SipException - INVALID_STATE if the request can not be set,
     * because of wrong state in SipClientConnection. Furthermore, ACK and
     * CANCEL methods can not be initialized in Created state.
     * @throws SipException - INVALID_OPERATION if the method argument is one
     * of {BYE, NOTIFY, PRACK, UPDATE}
     */
    public void
            initRequest(java.lang.String method,
            javax.microedition.sip.SipConnectionNotifier scn)
            throws SipException;
    
    /**
     * Sets Request-URI explicitly. Request-URI can be set only in
     * Initialized state.
     * @see JSR180 spec, v 1.1.0, p 35
     *
     * @param URI - Request-URI
     * @throws IllegalArgumentException - MAY be thrown if the URI is invalid
     * @throws SipException - INVALID_STATE if the Request-URI can not be set,
     * because of wrong state.
     * INVALID_OPERATION if the Request-URI is not allowed to be set.
     */
    public void setRequestURI(java.lang.String URI)
    throws SipException;
    
    /**
     * Convenience method to initialize SipClientConnection with SIP request
     * method ACK. ACK can be applied only to INVITE request.
     * @see JSR180 spec, v 1.1.0, p 35-36
     *
     */
    public void initAck()
    throws SipException;
    
    /**
     * Convenience method to initialize SipClientConnection with SIP request
     * method CANCEL.
     * @see JSR180 spec, v 1.1.0, p 36-37
     *
     */
    public javax.microedition.sip.SipClientConnection initCancel()
    throws SipException;
    
    /**
     * Receives SIP response message. 
     * @see JSR180 spec, v 1.1.0, p 37
     *
     * @param timeout - the maximum time to wait in milliseconds.
     * 0 = do not wait, just poll
     * @return Returns true if response was received. Returns false if
     * the given timeout elapsed and no response was received.
     * @throws SipException - INVALID_STATE if the receive can not be
     * called because of wrong state.
     * @throws IOException - if the message could not be received or
     * because of network failure
     */
    public boolean receive(long timeout)
    throws SipException, IOException;
    
    /**
     * Sets the listener for incoming responses. If a listener is
     * already set it
     * will be overwritten. Setting listener to null will remove the current
     * listener.
     * @see JSR180 spec, v 1.1.0, p 37
     *
     * @param sccl - reference to the listener object. Value null will remove
     *  the existing listener.
     * @throws IOException - if the connection is closed.
     */
    public void
            setListener(javax.microedition.sip.SipClientConnectionListener
            sccl)
            throws IOException;
    
    /**
     * Enables the refresh on for the request to be sent. The method return a
     * refresh ID, which can be used to update or stop the refresh.
     * @see JSR180 spec, v 1.1.0, p 37-38
     *
     * @param srl - callback interface for refresh events, if this is null the
     * method returns 0 and refresh is not enabled.
     * @return refresh ID. If the request is not refreshable returns 0.
     * @throws SipException - INVALID_STATE if the refresh can not be enabled
     * in this state.
     */
    public int enableRefresh(javax.microedition.sip.SipRefreshListener srl)
    throws SipException;
    
    /**
     * Sets credentials for possible digest authentication.
     * @see JSR180 spec, v 1.1.0, p 38-42
     *
     * @param username - username (for this protection domain)
     * @param password - user password (for this protection domain)
     * @param realm - defines the protection domain
     * @throws SipException - INVALID_STATE if the credentials can not
     * be set in this state.
     * @throws NullPointerException - if the username, password or realm is null
     */
    public void setCredentials(java.lang.String username,
            java.lang.String password,
            java.lang.String realm)
            throws SipException;
    
    /**
     * Sets multiple credentials triplets for possible digest authentication.
     * @see JSR180 spec, v 1.1.0, p 43
     *
     * @param usernames - array of user names. The array element username[i] is
     * for the protection domain realm[i]
     * @param passwords - array of user passwords. The array element
     * passwords[i] is for the protection domain realm[i]
     * @param realms - array of protection domains
     * @throws SipException - INVALID_STATE if the credentials can not
     * be set in this state.
     * @throws NullPointerException - if the usernames, passwords or realms
     * array is null or any of their elements is null
     * @throws java.lang.IllegalArgumentException - if the length of the 
     * parameter arrays are not equal or the length of at least one of them is 0
     */
    public void setCredentials(java.lang.String[] usernames,
            java.lang.String[] passwords,
            java.lang.String[] realms)
            throws SipException;
}

