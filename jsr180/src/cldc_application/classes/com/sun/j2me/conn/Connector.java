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
 * Created on Jan 29, 2004
 *
 */
package com.sun.j2me.conn;

import com.sun.j2me.security.Token;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;
import java.io.IOException;

public class Connector {

    static public DatagramConnection getDatagramConnection(Token token,
                 String name, int mode, boolean timeouts) throws IOException {
        com.sun.midp.io.j2me.datagram.Protocol conn = new
            com.sun.midp.io.j2me.datagram.Protocol();

        return (DatagramConnection)conn.openPrim(token.getSecurityToken(), name, mode, timeouts);

    }

    static public SocketConnection getSocketConnection(Token token,
                 String name) throws IOException {
        com.sun.midp.io.j2me.socket.Protocol conn = new
            com.sun.midp.io.j2me.socket.Protocol();

        return (SocketConnection)conn.openPrim(token.getSecurityToken(), name);

    }
    
    static public ServerSocketConnection getServerSocketConnection(Token token,
                 String name) throws IOException {

	com.sun.midp.io.j2me.socket.Protocol conn = new
            com.sun.midp.io.j2me.socket.Protocol();

        return (ServerSocketConnection)conn.openPrim(token.getSecurityToken(), name);

    }
}

