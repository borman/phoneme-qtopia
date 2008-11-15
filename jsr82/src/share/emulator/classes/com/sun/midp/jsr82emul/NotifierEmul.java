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
import java.io.InterruptedIOException;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.Connector;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.midp.io.j2me.serversocket.Socket;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.main.Configuration;
import com.sun.jsr082.bluetooth.BluetoothUtils;
import com.sun.midp.jsr082.SecurityInitializer;

/*
 * Emulates JSR 82 notifier.
 */
public class NotifierEmul implements EmulUnit {
    /* Integer handle that identifies notifier at native level. */
    private int handle;

    /*
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /* Internal security token that grants access to restricted API. */
    private static SecurityToken internalSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /* Device this notifier works at. */
    DeviceEmul device;
    
    /* Server socet to accept connections. */
    Socket serverSocket;
    
    /* next free socket port. */
    static int nextSocketPort = Configuration.getIntProperty(
            "com.sun.midp.jsr82emul.serverPort", 1234) + 1;
            
    /* 
     * Keeps options of service connection string used for this 
     * notifier creation.
     */
    private ServiceConnectionData serviceData;
    
    /* PSM or channel id. */
    int port;
    
    /* An emulation of client connection. */
    private ConnectionEmul clientConnection = null;
    
    /* Current connection acceptor, <code>null</code> if there is no one. */
    Acceptor acceptor = null;
    
    /* Represents socket client acception that runs in a separate thread. */
    private class Acceptor extends RunnableProcessor {
        /* 
         * Processes acception. 
         * Implementation for abstract method of the superclass.
         */
        protected void process() {
            try {
                if (serverSocket == null) {
                    // normally it means that interruption is in progress
                    throw new IOException("Improper acceptor state");
                }
                SocketConnection sc = (SocketConnection)serverSocket.acceptAndOpen();
                clientConnection = new ConnectionEmul(serviceData);
                clientConnection.open(sc);
            } catch (IOException e) {
                if (!interrupted) {
                    clientConnection.close();
                }
                clientConnection = null;
            }
            
            if (!interrupted) {
                if (clientConnection == null) {
                    notifyAccepted(handle, -1, null, -1, -1);
                } else {
                    notifyAccepted(handle, clientConnection.handle,
                        BluetoothUtils.getAddressBytes(
                            clientConnection.getRemoteAddress()),
                        clientConnection.getReceiveMTU(),
                        clientConnection.getTransmitMTU());
                }
            }
            acceptor = null;
        }
    }
    
    /*
     * Creates new instance and registers it in emulation environment with 
     * given handle. It is initialized by the next request to emulation.
     *
     * @param handle integer handle reserved for the new instance 
     *        in native layer.
     */
    public NotifierEmul(int handle) {
        this.handle = handle;
        // IMPL_NOTE redundant?
        device = DeviceEmul.getLocalDeviceEmul();
        
        // Registering this notifier as a processor for requests to emulation
        ConnRegistry.getInstance().register(handle, this);
    }
    
    /*
     * Initializes this notifier emulation unit.
     *
     * @param serviceData packed info on service details
     */
    public void initialize(ServiceConnectionData serviceData) {
        this.serviceData = serviceData;        
        
        serverSocket = new Socket();
    
        success:
        {
            final int maxTrials = 32;
            for (int i = 0; i < maxTrials; i++, nextSocketPort++) {
                try {
                    serverSocket.open(nextSocketPort, internalSecurityToken);
                } catch (IOException e) {
                    // consider port is busy and just continue trials
                    continue;
                }
                
                serviceData.socketPort = nextSocketPort++;
                break success;
            }
            
            error();
        }
    }
    
    /* Rgisters error occued to pass it to code above the porting layer. */
    private void error() {/*need revisit*/}

    /* 
     * Starts accepting incoming connection.
     * IMPL_NOTE it should completely substitute accept() when moving emul
     * below porting layer completed
     */
    public void startAccept() {
        acceptor = new Acceptor();
        acceptor.start();
        
        try {
            device.registerService(serviceData);
        
        } catch (IOException e) {
            error();
            // IMPL_NOTE 
            // add e details;
        }
    }
    
    /* 
     * Closes the notifier (emulation). 
     * @exception IOException if emulation server does not respond properly
     */
    public void close() {
        if (acceptor != null) {
            acceptor.interrupt();
            acceptor = null;
        }
            
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                error();
            }
            
            serverSocket = null;
        }
        
        clientConnection = null;
        
        ConnRegistry.getInstance().unregister(handle);
        if (-1 != serviceData.socketPort) {
            device.unregisterService(serviceData.socketPort);
        }
    }
    
    // IMPL_NOTE move to constants.xml
    /* Accept request code. */
    private static final byte NOTIF_ACCEPT = 0;
    /* Close request code. */
    private static final byte NOTIF_CLOSE = 1;
    /* Initialize request code. */
    private static final byte NOTIF_INIT = 2;
    
    /*
     * Processes request from porting layer to emulation.
     * @param request packed request to process
     */
    public void process(BytePack request) {
        switch(request.extract()) {
        case NOTIF_ACCEPT:
            Log.log("processing NOTIF_ACCEPT");
            startAccept();
            Log.log("processing NOTIF_ACCEPT done");
            break;
        case NOTIF_CLOSE:
            Log.log("processing NOTIF_CLOSE");
            close();
            Log.log("processing NOTIF_CLOSE done");
            break;
        case NOTIF_INIT:
            Log.log("processing NOTIF_INIT");
            initialize(new ServiceConnectionData(
                request.extractBytes(ServiceConnectionData.SERVER_DATA_SIZE),
                ServiceConnectionData.SERVER_DATA));
            Log.log("processing NOTIF_INIT done");
            break;
        default:
            throw new EmulationException("Unknown Notifier request");
        }
    }
    
    /* 
     * Notifies porting layer on completing acception. 
     * @param thisHandle
     * @param connHandle handle connection created as a result of acception,
     *         <code>-1</code> if acception failed.
     * @param peerAddr bluetooth address of device connected. 
     * @param receiveMTU established ReceiveMTU of accepted connection. 
     * @param transmitMTU established TransmitMTU of accepted connection.
     * 
     * IMPL_NOTE 
     * peerAddr may become useless when moving emul below porting layer 
     * completed; thisHandle may be retrieved thru KNI
     *
     */
    private native void notifyAccepted(int thisHandle, int connHandle, 
        byte[] peerAddr, int receiveMTU, int transmitMTU);
}
