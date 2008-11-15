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

// jsr082 (impl) classes
import com.sun.jsr082.obex.ObexTransport;
import com.sun.jsr082.obex.ObexTransportNotifier;

// cldc API classes
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import java.io.IOException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.io.j2me.socket.Protocol;
import com.sun.midp.io.j2me.serversocket.Socket;
import javax.microedition.io.StreamConnection;

/*
 * Provides TCP OBEX stream notifier to shared obex implementation.
 */
public class TCPOBEXNotifier implements ObexTransportNotifier {
    /* TCP server socket that accepts connections to this notifier. */
    private Socket serverSocket;
    /* The sever socket port this notifier accepts connections on. */
    private int port;

    /* Indicates if this notifier is closed. */
    private boolean isClosed = false;

    /*
     * Constracts TCPOBEXNotifier instance.
     *
     * @param port The host's port to listen on.
     * @throws IOException if any error occures.
     */
    protected TCPOBEXNotifier(int port)
                                                       throws IOException {
        String name = (port == 0) ? "//" : "//:" + port;
        Protocol sp = new Protocol();

//        if (port == 0) {
//            serverSocket = (Socket) sp.openPrim(securityToken, "//");
//        } else {
//            serverSocket = (Socket) sp.openPrim(securityToken, "//:" + port);
        serverSocket = (Socket) sp.openPrim(name, Connector.READ_WRITE, false);
//        }
        
        this.port = serverSocket.getLocalPort();
    }

    /*
     * Accepts new tcpobex transport connection.
     *
     * @return TCPOBEXConnection.
     * @throws IOException if thrown by server socket operations or this 
     *         notifier is closed
     */
    public ObexTransport acceptAndOpen() throws IOException {
        StreamConnection conn = serverSocket.acceptAndOpen();
        Object[] param = new Object[3];
        
        param[0] = conn.openInputStream();
        param[1] = conn.openOutputStream();
        param[2] = null; // close the connection now
        conn.close();
        
        return createTransportConnection(param);
    }

    /*
     * Closes the underlaying tcp/socket notifier.
     *
     * @throws IOException if thrown by server socket methods
     */
    public synchronized void close() throws IOException {
        if (!isClosed) {
            isClosed = true;
            serverSocket.close();
        }
    }

    /*
     * Gets underlying connection.
     *
     * @return Always returns <code>null</code>.
     */
    public Connection getUnderlyingConnection() {
        return null;
    }

    /*     
     * Creates TCP OBEX transport connection.
     *
     * @param sockData An array of the streams.
     * @return TCPOBEXConnection.
     * @throws IOException
     */
    protected TCPOBEXConnection createTransportConnection(Object[] sockData)
            throws IOException {
        return new TCPOBEXConnection(sockData);
    }
}
