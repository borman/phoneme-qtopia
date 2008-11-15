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

import javax.microedition.io.Connection;
import java.io.*;

/**
 * This interface defines a SIP server connection notifier.
 * @see JSR180 spec, v 1.0.1, p 37-39
 *
 */
public interface SipConnectionNotifier extends Connection {
    
    /**
     * Accepts and opens a new SipServerConnection in this listening point.
     * If there are no messages in the queue method will block until a
     * new request is received.
     * @return SipServerConnection which carries the received request
     * @throws IOException - if the connection can not be established
     * @throws InterruptedIOException - if the connection is closed
     * @throws SipException - TRANSACTION_UNAVAILABLE if the system
     *  can not open new SIP transactions
     * @throws SecurityException - if the caller does not have the required 
     *  permissions to create server connections.
     */
    public javax.microedition.sip.SipServerConnection acceptAndOpen()
    throws IOException, SipException;
    
    /**
     * Sets a listener for incoming SIP requests. If a listener is
     * already set it
     * will be overwritten. Setting listener to null will remove the current
     * listener.
     * @param sscl - listener for incoming SIP requests
     * @throws IOException - if the connection was closed
     */
    public void
            setListener(javax.microedition.sip.SipServerConnectionListener
            sscl)
            throws IOException;
    
    /**
     * Gets the local IP address for this SIP connection.
     * @return local IP address. Returns null if the address is not available.
     * @throws IOException - if the connection was closed
     */
    public java.lang.String getLocalAddress()
    throws IOException;
    
    /**
     * Gets the local port for this SIP connection.
     * @return local port number, that the notifier is listening to.
     * Returns 0 if the port is not available.
     * @throws IOException - if the connection was closed
     */
    public int getLocalPort()
    throws IOException;
}
