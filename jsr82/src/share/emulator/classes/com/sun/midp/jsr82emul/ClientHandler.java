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
import com.sun.jsr082.bluetooth.BluetoothUrl;
import java.util.Hashtable;
import java.util.Enumeration;

/*
 * Handles communication with a client.
 */
class ClientHandler implements Runnable {
    /* Handle that identifies corresponding data on server. */
    int handle;
    /* Common handlers counter. */
    private static int nextHandle = 0;
    
    /* Bluetooth address of device or device emulation client belongs to. */
    byte[] bluetoothAddress = null;
    
    /* If not <code>null</code>, IP address of computer client works at. */
    byte[] ipAddress = null;
    
    /*
     * Keeps device state if this handler handles device, 
     * <code>null</code> otherwise. 
     */
    DeviceState deviceState = null;
        
    /*
     * if this handler handles device, then keeps services available at it,
     * else <code>null</code>. 
     */
    Hashtable deviceServices = null;
        
    /*
     * Keeps messenger that provides utilities for communicating with client.
     */
    private Messenger messenger = new Messenger();
    
    /* Keeps socket connection to the server. */
    private SocketConnection connection; 
    /* Keeps input stream from server. */
    private InputStream fromClient;                                   
    /* Keeps output stream to server. */
    private OutputStream toClient;
    
    private boolean inProcess = false;
    
    /* 
     * Retrieves handle value for a new handler. 
     * @return new free handle value
     */
    private static synchronized int getNextHandle() {
        return ++nextHandle;
    }
    
    /*
     * Constructs client handle to communicate with a client thru
     * given connection.
     * @param connection an open connection with a client on the other side
     * @exception IOException if connection fails
     */
    ClientHandler(SocketConnection connection) throws Exception {
        this.connection = connection;
        fromClient = connection.openDataInputStream();
        toClient = connection.openDataOutputStream();
        handle = getNextHandle();
        
        Thread t = new Thread(this);
        t.start();
    }

    /* 
     * Implements <code>Runnable</code>, processes client's rquests.
     */
    public void run() {
        Exception exception = null;
    
        try {
            loop:
            while (true) {
                messenger.receive(fromClient);
            
                switch (messenger.getCode()) {
                    case Messenger.REGISTER_DEVICE:
                        synchronized (messenger) {
                            inProcess = true;
                            try {
                                ipAddress = messenger.getBytes();
                                deviceServices = new Hashtable();
                                // IMPL_NOTE: how it works against Push TCK tests?
                                // default device state: not discoverable
                                deviceState = new DeviceState();
                                bluetoothAddress = EmulationServer
                                        .getInstance().registerDevice(
                                                deviceState);
                                messenger.sendBytes(toClient,
                                        Messenger.REGISTERED, bluetoothAddress);
                            } finally {
                                inProcess = false;
                                messenger.notify();
                            }
                        }
                        break;
                        
                    case Messenger.UPDATE_DEVICE_STATE:
                        synchronized (messenger) {
                            deviceState.update(messenger.getInt());
                        }
                        break;
                        
                    case Messenger.REGISTER_SERVICE:
                        ServiceConnectionData serv;
                        ServiceKey key;
        
                        synchronized (messenger) {
                            inProcess = true;
                            try {
                                serv = new ServiceConnectionData(messenger
                                        .getBytes(),
                                        ServiceConnectionData.SERVER_DATA);
                                serv.address = ipAddress;
                                key = EmulationServer.getInstance()
                                        .registerService(serv, bluetoothAddress);
                                if (!deviceServices.containsKey(key)) {
                                    deviceServices.put(
                                            new Integer(serv.socketPort), key);
                                }
                            } finally {
                                inProcess = false;
                                messenger.notify();
                            }
                        }
                        break;

                    case Messenger.UNREGISTER_SERVICE:
                        synchronized (messenger) {
                            key = (ServiceKey) deviceServices
                                    .remove(new Integer(messenger.getInt()));
                            if (key != null) {
                                EmulationServer.getInstance()
                                        .unregisterService(key);
                                key = null;
                            }
                        }
                        break;
                                                
                    case Messenger.CONNECT_TO_SERVICE:
                        synchronized (messenger) {
                            int attempts = 0;
                            while (inProcess && attempts < 100) {
                                try {
                                    messenger.wait(200);
                                    attempts += 1;
                                } catch (InterruptedException e) {
                                    throw new EmulationException("Service isn't registred");
                                }
                            }
                            serv = EmulationServer
                                .getInstance()
                                .connectToService(
                                    new ServiceConnectionData(
                                            messenger.getBytes(),
                                            ServiceConnectionData.CONN_REQUEST_DATA));
                            messenger
                                .sendBytes(
                                        toClient,
                                        Messenger.SERVICE_AT,
                                        serv
                                            .toByteArray(ServiceConnectionData.CONNECTION_DATA));
                        }
                        break;
                        
                    case Messenger.START_INQUIRY:
                        synchronized (messenger) {
                            InquiryResults results = EmulationServer
                                    .getInstance().runInquiry(
                                            messenger.getInt(),
                                            bluetoothAddress);
                            messenger.sendBytes(toClient,
                                    Messenger.INQUIRY_COMPLETED, results
                                            .toByteArray());
                        }
                        break;
                        
                    case Messenger.DONE:
                        break loop;
                
                    default: throw new EmulationException("Unknown client request");
                }
            } // end of loop:
        
        } catch (EmulationException ee) {
            exception = ee;
        } catch (IOException e) {
            exception = e;
        } finally {
            if (exception != null) {
                try {
                    messenger.send(toClient, Messenger.ERROR, exception.getMessage());
                } catch (IOException e) {
                    // ignoring - stop handling anyway
                }
            }
            
            if (deviceState != null) {
                EmulationServer.getInstance().
                    unregisterDevice(bluetoothAddress);
            
                Enumeration records = deviceServices.elements();
                while (records.hasMoreElements()) {
                    EmulationServer.getInstance().unregisterService(
                        (ServiceKey)records.nextElement());
                }
            }
            disconnect();
        }
    }
    
    /*
     * Closes current socket connection.
     */
    private void disconnect() {
        try { 
            connection.close();
            toClient.close();
            fromClient.close();
        } catch (IOException e) {
            // ignoring
        }
        
        connection = null;
        fromClient = null;
        toClient = null;
    }
}
