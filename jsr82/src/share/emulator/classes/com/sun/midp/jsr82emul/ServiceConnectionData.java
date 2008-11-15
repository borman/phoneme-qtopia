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

import java.io.UnsupportedEncodingException;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import javax.bluetooth.BluetoothConnectionException;
import com.sun.jsr082.bluetooth.BluetoothUtils;

/* 
 * Utility class that keeps service connection information, packs it
 * into byte array and restores form it. Has methods to check if a
 * service represented by an instance can accept a connection represented
 * by another one.
 */
class ServiceConnectionData extends BytePack {
    /* 
     * Error type, like in <code>BluetoothConnectionException</code>,
     * ff error occured while looking for connection, <code>-1</code> 
     * if there is no error. Only makes sense in case of CONNECTION_DATA.
     */
    int error = -1;
    
    /* TCP socket port emulated service listens on. */
    int socketPort;
    /* 
     * IP address or host name of service in case of CONNECTION_DATA,  
     * Bluetooth address of client in case of CLIENT_DATA
     */
    byte[] address;
    /* Channel ID or PSM. */
    int port;
    /* ReceiveMTU in server side terms. */
    int receiveMTU;
    /* TransmitMTU in server side terms. */
    int transmitMTU;
    /* Protocol ID as in <code>BluetoothUrl</code>. */
    int protocol;
    /* Shows wither server is master. */
    boolean master;
    /* Shows wither encrypted connection required. */
    boolean encrypt; 
    /* Shows wither authorization required by server. */
    boolean authorize; 
    /* Shows wither authentication required by server. */
    boolean authenticate;
    
    /* 
     * Identifies if a service defined by this instance accepting currently. 
     * This flag is used to overcome TCK inconsistency where client may
     * start connecting to Bluetooth TCK Agent while the latter is still not 
     * ready after previous connection.
     */
    private boolean accepting = true;
    /* 
     * Delay duration for the case when acception is requested while it is
     * not known if corresponding service is accepting.
     */
    private static final int ACCEPT_DELAY = 1000;
    /* 
     * Max amount of acception trials when it is not known if service 
     * is accepting 
     */
    private static final int MAX_ACCEPT_TRIALS = 24;
    
    /* 
     * Packed data type that implies that the following fields are included:
     * socketPort, port, receiveMTU, transmitMTU, receiveMTU, protocol, 
     * master, encrypt, authorize, authenticate.
     */
    static final int SERVER_DATA = 0; 
    /* 
     * Packed data type that implies that the following fields are included:
     * socketPort, IP address, receiveMTU, transmitMTU.
     */
    static final int CONNECTION_DATA = 1; 
    /* 
     * Packed data type that implies that the following fields are included:
     * transmitMTU, receiveMTU, Bluetooth address.
     */
    static final int CLIENT_DATA = 2; 
    /* 
     * Packed data type that implies that the following fields are included:
     * Bluetooth address, protocol, port, transmitMTU, receiveMTU, encrypt,
     * authenticate, master.
     * It is used CONNECT_TO_SERVICE request to the emulation server
     */
    static final int CONN_REQUEST_DATA = 3; 
    
    /* Size of Bluetooth address in bytes representation. */
    private static final int BTADDR_SIZE = 6;
    
    /* Byte array size when packed with CLIENT_DATA type. */
    static final int CLIENT_DATA_SIZE = 4 * 2 + BTADDR_SIZE;
    /* Byte array size when packed with SERVER_DATA type. */
    static final int SERVER_DATA_SIZE = 4 * 4 + 1;
    
    /* Bit mask to highlight protcol type. */
    private final static int PROTOCOL_MASK = 3;
    /* Bit mask to highlight master flag. */
    private final static int MASTER_MASK = 4;
    /* Bit mask to highlight encrypt flas. */
    private final static int ENCRYPT_MASK = 8;
    /* Bit mask to highlight authorize flag. */
    private final static int AUTHORIZE_MASK = 32;
    /* Bit mask to highlight authenticate flag. */
    private final static int AUTHENTICATE_MASK = 16;
    
    /* 
     * Constructs an instance which indicates that unknown error occured
     * while looking for connection.
     */
    ServiceConnectionData() {
        this(BluetoothConnectionException.FAILED_NOINFO);
    }
    
    /* 
     * Constructs an instance which indicates that a connection error occured.
     * @param error error code as in <code>BluetoothConnectionException</code>
     */
    ServiceConnectionData(int error) {
        this.error = error;
    }
    
    /*
     * Constructs an instance by given byte representation.
     * @param data byte representation
     * @param type type of packed data, must be one of <code>
     * SERVER_DATA, CONNECTION_DATA, CLIENT_DATA, CONN_REQUEST_DATA 
     * </code>
     */
    ServiceConnectionData(byte[] data, int type) {
        super(data);        

        switch (type) {        
        case CONN_REQUEST_DATA:
            address = extractBytes(BTADDR_SIZE);
        
        // the rest of CONN_REQUEST_DATA case is poceeded here as well
        case SERVER_DATA:
            if (type == SERVER_DATA) {
                socketPort = extractInt();
            }
            
            port = extractInt();
            receiveMTU = extractInt();
            transmitMTU = extractInt();
        
            int misc = extract();
            protocol = misc & PROTOCOL_MASK;
            master = (misc & MASTER_MASK) != 0;
            encrypt = (misc & ENCRYPT_MASK) != 0;
            authorize = (misc & AUTHORIZE_MASK) != 0;
            authenticate = (misc & AUTHORIZE_MASK) != 0;
            break;
            
        case CONNECTION_DATA:
            error = extractInt();
            if (error == -1) {
                socketPort = extractInt();
                receiveMTU = extractInt();
                transmitMTU = extractInt();
                address = extractBytes(Const.IP_SIZE);
            }
            break;
        
        case CLIENT_DATA:
            receiveMTU = extractInt();
            transmitMTU = extractInt();
            address = extractBytes(BTADDR_SIZE);
            break;
            
        default:
            throw new IllegalArgumentException();
        }
        
        release();
    }
         
    /*
     * Retrieves bytes representation.     
     * @param type type of packed data, that defines which fields
     *     are to be packed, must be one of <code>
     *     SERVER_DATA, CONNECTION_DATA, CLIENT_DATA, CONN_REQUEST_DATA
     *     </code>
     * @return byte array that keeps packed properties
     */
    byte[] toByteArray(int type) {
        switch (type) {
        case CONN_REQUEST_DATA:
            reset(new byte[BTADDR_SIZE + SERVER_DATA_SIZE - 1]);
            appendBytes(address);
        
        // the rest of CONN_REQUEST_DATA is proceeded here as well
        case SERVER_DATA:
            if (type == SERVER_DATA) {
                reset(new byte[SERVER_DATA_SIZE]);
                appendInt(socketPort);
            }
            
            appendInt(port);
            appendInt(receiveMTU);
            appendInt(transmitMTU);
        
            int misc = protocol;
            misc |= master? MASTER_MASK : 0;
            misc |= encrypt? ENCRYPT_MASK : 0;
            misc |= authorize? AUTHORIZE_MASK : 0;
            misc |= authenticate? AUTHORIZE_MASK : 0;
            append((byte)misc);
            break;
        
        case CONNECTION_DATA:
            if (error == -1) {
                reset(new byte[4 * 4 + Const.IP_SIZE]);
                appendInt(error);
                appendInt(socketPort);
                appendInt(receiveMTU);
                appendInt(transmitMTU);
                appendBytes(address);        
            
            } else {
                reset(new byte[4]);
                appendInt(error);
            }
            break;
            
        case CLIENT_DATA:
            reset(new byte[CLIENT_DATA_SIZE]);
            appendInt(receiveMTU);
            appendInt(transmitMTU);
            appendBytes(address);
            break;
            
        default:
            throw new IllegalArgumentException();
        }
        
        return release();
    }
     
    /*
     * Checks if client connection with given parameters can be accepted,
     * if it can not, sets error code. If acception is possible but 
     * requires fields alignment, makes it modifying connection data 
     * passed as a parameter.
     *
     * @param client parameters of client connection request derived from 
     *         client url, the parameters can be modified by this method
     *         if connection can be accepted.
     * @return error code as in <code>BluetoothConnectionException</code>, if
     *         connection can not be accepted, <code>-1</code> if it can
     */
    void accept(ServiceConnectionData client) {
        client.address = address;
        client.socketPort = socketPort;
        client.error = -1;
        
        if (client.master && master) {
            client.error = BluetoothConnectionException.UNACCEPTABLE_PARAMS;
        
        } else if (client.protocol == BluetoothUrl.L2CAP) {
            // If connection accepted successfully,
            // (receiveMTU, transmitMTU) set here for the parameter 
            // become actual (receiveMTU, transmitMTU) for server and
            // actual (transmitMTU, receiveMTU) for client.
            
            if (client.transmitMTU > receiveMTU || transmitMTU > client.receiveMTU) {
                client.error = BluetoothConnectionException.UNACCEPTABLE_PARAMS;
            } else {
                if (client.transmitMTU == -1) {
                    client.transmitMTU = receiveMTU;
                }
                if (transmitMTU != -1) {
                    client.receiveMTU = transmitMTU;
                }
            }
        }
        
        if (client.error == -1) {
            synchronized (this) {
                int trial = 0;
                for (; trial < MAX_ACCEPT_TRIALS && !accepting; trial++) {
                    try {
                        Thread.sleep(ACCEPT_DELAY);
                    } catch (Throwable t) {}
                }
                
                if (trial == MAX_ACCEPT_TRIALS) {
                    client.error = BluetoothConnectionException.TIMEOUT;
                } else {
                    // accepted successfully, now service is occupied by current client
                    accepting = false;
                }
            }
        }
    }
    
    /* 
     * Sets acception flag to true to identify that represented service 
     * is currebtly accepting. A part of the way that overcomes TCK 
     * inconsistency.
     */
    void setAccepting() {
        accepting = true;
    }
}
