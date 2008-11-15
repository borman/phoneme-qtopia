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
package com.sun.midp.jsr82emul;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.SocketConnection;
import com.sun.midp.main.Configuration;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.jsr082.SecurityInitializer;

/*
 * Represents a client for JSR 82 emulation environment. 
 * It is not a part of JSR 82 implementation and is only used within 
 * JSR 82 emulation mode. The emulation mode allows running tests without
 * real native Bluetooth libraries or hardware.
 * 
 * In the emulation mode the client runs under J2ME VM control and connects
 * thru a socket to a server which runs somewhere on the Internet under J2SE
 * VM control.
 *
 * A client represents a Bluetooth local or remote device, connection 
 * or anything that requires Bluetooth ether communication. A server 
 * in turn represents Bluetooth ether.
 */
public class EmulationClient {

    /*
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /* Internal security token that grants access to restricted API. */
    protected static SecurityToken internalSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /* Keeps default port number for server connections. */
    private final static int EMUL_PORT = 
        Configuration.getIntProperty("com.sun.midp.jsr82emul.serverPort", 1234);
    
    /* IP address of EmulationServer. */
    private static String serverIP;
    
    /* Lock for emulation server communications that require response. */
    protected Object serverTransaction = new Object();
    
    /* 
     * Keeps messenger that proveds utilities for communicating with server.
     */
    protected Messenger messenger = new Messenger();
    
    /* Keeps socket connection to the server. */
    protected SocketConnection connection = null; 
    /* Keeps input stream from server. */
    protected InputStream fromServer = null;                                   
    /* Keeps output stream to server. */
    protected OutputStream toServer = null;
  
    /*
     * Creates an instance.
     */
    protected EmulationClient() {}
    
    /* 
     * Retrieves port of emulation server.
     * @return EmulationServer IP address from JSR82_EMUL_IP environment
     *        variable, <code>null</code> if the variable is not defined.
     */
    private static native String getServerIP();
    
    /*
     * Retrieves local IP address.
     * @return <code>String</code> object containing local IP address, 
     *         <code>null</code> if retrieving failed.
     */
    static native String getLocalIP();
    
    /*
     * Retrieves emulation server IP address, launches server if needed.
     * @return emulation server IP address.
     */
    private static synchronized String getServer() {
        if (serverIP != null) {
            return serverIP;
        }
        
        serverIP = getServerIP();
        
        // Local IP of emulation server means that it is possibly
        // expected that current application will launch the server.
        if (serverIP == null || serverIP.length() == 0 
            || serverIP.equals("127.0.0.1")) {
            EmulationServer.launch();
            serverIP = "localhost";
        } else if (serverIP.equals(getLocalIP())) {
            EmulationServer.launch();
        }
        
        return serverIP;
    }
    
    /*
     * Opens socket connection to current host at current port.
     *
     * @exception IOException if there is an open connection or 
     * en error ocurred while trying to connect.
     */
    protected void connect() throws IOException {
        if (connection != null) {
            throw new IOException("connection is already open");
        }
        
        connection = (SocketConnection) 
            new com.sun.midp.io.j2me.socket.Protocol().openPrim(
                internalSecurityToken,
                "//" + getServer() + ":" + EMUL_PORT);
                
        fromServer = connection.openDataInputStream();
        toServer = connection.openDataOutputStream();
    }
    
    /*
     * Closes current socket connection.
     *
     * @exception IOException if an error occured or ocurred.
     */
    protected void disconnect() throws IOException {
        if (connection != null) {
            messenger.send(toServer, Messenger.DONE, "");
            
            toServer.close();
            fromServer.close();
            connection.close();
            
            toServer = null;
            fromServer = null;
            connection = null;
        }
    }
}
