/*
 *
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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
package com.sun.jsr082.obex.tcpobex;

// jsr082 impl
import com.sun.jsr082.obex.ClientSessionImpl;
import com.sun.jsr082.obex.SessionNotifierImpl;

import com.sun.j2me.io.ConnectionBaseInterface;
import com.sun.j2me.security.OBEXPermission;
import com.sun.j2me.app.AppPackage;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import java.io.IOException;
import java.io.InterruptedIOException;

/*
 * Provides a wrapper for "tcpobex" protocol implementation
 * to answer the GCF style.
 */
public class Protocol implements ConnectionBaseInterface {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /* DEBUG: this class name for debug. */
    private static final String cn = "tcpobex.Protocol";

    /* Shows whether slient permissions checked. */
    private boolean clientPermitted = false;

    /* Shows whether server permissions checked. */
    private boolean serverPermitted = false;

    /* Connection url for netmon. */
    protected String origName;

    /*
     * Required for instantation via reflection.
     */
    public Protocol() {}

    /*
     * Creates the tcpsocket connection or notifier.
     *
     * @param name The URL for the connection.
     * @param mode OBEX supports READ_WRITE mode only.
     * @param timeouts A flag to indicate that the caller (ignored).
     * @return ClientSession for client url or SessionNotifier for server url.
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {

        // print what do we have here
        if (DEBUG) {
            System.out.println(cn + ":name = " + name + " mode = " + mode
                    + " timeouts = " + timeouts);
        }
        final String errorMsg = "Malformed URL: tcpobex:" + name;
        origName = "tcpobex:" + name;

        // this implementation supports READ_WRITE mode only
        if (mode != Connector.READ_WRITE) {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }

        // do the URL parsing here
        if (!name.startsWith("//")) {
            throw new IllegalArgumentException(errorMsg);
        }

        /*
         * JSR82 Specification 1.0a contains two contradicting forms of
         * tcpobex server url. See 11.4.2 and 11.4.4. Supporting only
         * for described in example:pIndex
         *     tcpobex://:port
         *
         * The possible URL values are:
         *
         *     tcpobex://               - server on default port (650)
         *     tcpobex://:port          - server on specified port
         *     tcpobex://<name/ip>      - client on default port
         *     tcpobex://<name/ip>:port - client on specified port
         *
         */
        int pIndex = name.indexOf(':');
        boolean isServer = name.length() == 2 || pIndex == 2;
        int port = 650;

        // extract the port if specified
        if (pIndex != -1) {
            if (pIndex == name.length() - 1) {
                throw new IllegalArgumentException(errorMsg);
            }

            try {
                port = Integer.parseInt(name.substring(pIndex + 1));

                if (port <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(errorMsg);
            }
        }

        // server connection on specified port
        if (isServer) {

            // check for server permissions
            if (!serverPermitted) {
                checkForPermission(OBEXPermission.TCP_OBEX_SERVER, name);
                serverPermitted = true;
            }
            return new SessionNotifierImpl(createTransportNotifier(port));
        }

        // check for client permissions
        if (!clientPermitted) {
            checkForPermission(OBEXPermission.TCP_OBEX_CLIENT, name);
            clientPermitted = true;
        }

        // get the client target host name (ip address)
        pIndex = pIndex == -1 ? name.length() : pIndex;
        String host = name.substring(2, pIndex);
        return new ClientSessionImpl(createTransportConnection(host, port));
    }

    /*
     * Makes sure caller has the permission set to "allowed".
     * @param permission requested permission
     * @param name resource name to check permissions against
     * @exception IOInterruptedException if another thread interrupts the
     *        calling thread while this method is waiting to preempt the
     *        display.
     */
    private void checkForPermission(OBEXPermission permission, String name)
            throws InterruptedIOException {

        AppPackage app = AppPackage.getInstance();

        if (app != null) {
            try {
                app.checkForPermission(new OBEXPermission(
                    permission.getName(), name));
            }
            catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        }
    }

    /*
     * Create tcp obex transport connection.
     *
     * @param host Target host name (ip address).
     * @param port Target's port to connect to.
     * @return TCPOBEXConnection instance.
     */
    protected TCPOBEXConnection createTransportConnection(String host, int port)
            throws IOException {

	    if (this.getClass() != Protocol.class) {
	        throw new SecurityException(
		    "Illegal Access to tcpobex implementation");
	    }

        return new TCPOBEXConnection(host, port);
    }

    /*
     * Create tcp obex transport notifier.
     *
     * @param port The server's port number to listen on.
     * @return TCPOBEXNotifier instance.
     */
    protected TCPOBEXNotifier createTransportNotifier(int port)
            throws IOException {

	    if (this.getClass() != Protocol.class) {
	        throw new SecurityException(
		    "Illegal Access to tcpobex implementation");
	    }

        return new TCPOBEXNotifier(port);
    }
}
