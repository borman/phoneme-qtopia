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
import java.io.OutputStream;
import java.io.DataInputStream;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.Connector;
import javax.bluetooth.BluetoothConnectionException;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothUtils;
import com.sun.jsr082.bluetooth.BCC;

/*
 * Emulates JSR 82 connection.
 */
public class ConnectionEmul extends EmulationClient 
        implements EmulUnit {
    /* Processes open request in a separate thread. */
    class Opener extends RunnableProcessor {
        /* Processes open request. */
        protected void process() {
            try {
                open();
            } catch (IOException e) {
                Log.log("Processing CONN_OPEN FAILED "); 
                notifyOpen(handle, false, -1, -1); 
                close();
            }
            
            if (!interrupted) {
                notifyOpen(handle, true, serviceData.receiveMTU, 
                    serviceData.transmitMTU);
            }
            opener = null;
        }
    }    
   
    /* Options of service to connect to. */
    ServiceConnectionData serviceData;
    
    /* Opener object needed to perform open operation in a separate thread. */
    private Opener opener;
    /* Socket connection that emulates JSR82 one. */
    private SocketConnection socketConnection;
    /* Protocol dependent data sener. */
    private Sender sender;
    /* Protocol dependent data receiver. */
    private Receiver receiver;
        
    /* Handle that identifies this connection emulation. */
    public int handle;
    
    /*
     * Opens client-side connection. Requests connection with given client
     * connection string from emulation server
     *
     * @return handle value
     * @exception IOException if communication to emulation server 
     *        or a service fails.
     */
    public int open() throws IOException {
        ServiceConnectionData connData = null;
        
        connect();
        int count=0;
        while(count < 100) {
            synchronized (serverTransaction) {
                messenger.sendBytes(toServer, Messenger.CONNECT_TO_SERVICE, 
                serviceData.toByteArray(
                                ServiceConnectionData.CONN_REQUEST_DATA));
                messenger.receive(fromServer);
            }
        
            if (messenger.getCode() != Messenger.SERVICE_AT) {
                throw new EmulationException(
                    "Unexpected emulation server response " + 
                    messenger.getCode());
            }
            
            connData = new ServiceConnectionData(
                messenger.getBytes(), ServiceConnectionData.CONNECTION_DATA);
        
            if (connData.error == -1) {
                break;
            }
            count++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
                    // nothing to do
            }
        }        
        // no need to communicate with emulation sever any more
        // since a direct connection is open
        disconnect();
        
        if (connData == null) {
            throw new 
            BluetoothConnectionException(BluetoothConnectionException.
                                         FAILED_NOINFO);
        } else {
        if (connData.error != -1) {
            throw new BluetoothConnectionException(connData.error);
            }
        }
        
        // vice versa to server side properties
        serviceData.receiveMTU = connData.transmitMTU;
        serviceData.transmitMTU = connData.receiveMTU;
        
        // applying bitwise operation to get unsigned values from bytes
        socketConnection = (SocketConnection) 
            new com.sun.midp.io.j2me.socket.Protocol().openPrim(
                internalSecurityToken, "//" +
                    (connData.address[0] & 0xff) + "." +
                    (connData.address[1] & 0xff) + "." +
                    (connData.address[2] & 0xff) + "." +
                    (connData.address[3] & 0xff) +
                    ":" + connData.socketPort);
        
        connData.address = DeviceEmul.getLocalDeviceEmul().address;
        sender = Sender.create(socketConnection, 
                    serviceData.protocol, handle);
        
        // It is important to use blocking sender method here
        sender.plainSend(connData.toByteArray(
            ServiceConnectionData.CLIENT_DATA));
        
        return handle;
    }

    /*
     * Opens server-side connection, i.e. assigns this emulation with already 
     * open server-side socket connection. 
     * @param socketConnection already open server-side socket connection.
     * @return handle value
     * @exception IOException if communication to emulation server 
     *        or a service fails.
     */
    public int open(SocketConnection socketConnection) 
            throws IOException {
        
        this.socketConnection = socketConnection;
        
        receiver = Receiver.create(socketConnection, 
                    serviceData.protocol, handle);
        
        // Calling blocking method intentionally here
        ServiceConnectionData connData = new ServiceConnectionData(
            receiver.forceReceive(ServiceConnectionData.CLIENT_DATA_SIZE),
            ServiceConnectionData.CLIENT_DATA);
        
        serviceData.receiveMTU = connData.receiveMTU;
        serviceData.transmitMTU = connData.transmitMTU;
        serviceData.address = connData.address;
        
        return handle;
    }

    /* Close this connection emulation. */
    public void close() {
        if (opener != null) {
            opener.interrupt();
            opener = null;
        }
        
        if (sender != null) {
            sender.close();
            sender = null;
        }
        
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
        
        if (socketConnection != null) {
            try {
                socketConnection.close();
            } catch (IOException e) {
                // ignoring
            }
            socketConnection = null;
        }
       
        ConnRegistry.getInstance().unregister(handle);
    }
    
    /*
     * Sends data to the other side of the emulated connection.
     *
     * @param len amount of bytes to send; given amount of bytes are
     *        sent from output buffer.
     * @exception IOException  if an I/O error occurs
     */
    private void send(int len) {
        byte[] buf = new byte[len];
        
        if (getOutData(handle, buf)) {
            if (sender == null) {
                sender = Sender.create(socketConnection, 
                    serviceData.protocol, handle);
            }
            sender.send(buf);
        }
        
        buf = null;
    }

    /*
     * Reads data from emulated connection.
     */
    private void receive() {
        if (receiver == null) {
            receiver = Receiver.create(socketConnection, 
                    serviceData.protocol, handle);
        }
        
        receiver.receive();
    }
    
    /* 
     * Returns actually established ReceiveMTU.
     * @return established ReceiveMTU.
     */
    public int getReceiveMTU() {
        return serviceData.receiveMTU;
    }
    
    /* 
     * Returns actually established TransmitMTU.
     * @return established TransmitMTU.
     */
    public int getTransmitMTU() {
        return serviceData.transmitMTU;
    }
    
    /* 
     * Returns Bluetooth address of other side of this connection.
     * @return Bluetooth address of the other side
     */
    public String getRemoteAddress() {
        return BluetoothUtils.getAddressString(serviceData.address);
    }
    
    /*
     * Constructs an instance with given handle. Handle is an 
     * integer value that identifies correspondence between connections
     * in porting layer nd ConnectionEmul instances. In case of client 
     * side connection handle is already defined in native 
     * <code>create_client()</code> functions. In case of server side
     * it is generated in another constructor.
     *
     * @param handle handle receieved form native layer.
     */
    public ConnectionEmul(int handle) {
        this.handle = handle;
        ConnRegistry.getInstance().register(this.handle, this);
    }
    
    /*
     * Constructs an instance at server side.
     * @param serviceData connection options passed by notifier that 
     *        creates this connection.
     */
    public ConnectionEmul(ServiceConnectionData serviceData) {
        this(getHandle(serviceData.protocol));
        this.serviceData = serviceData;
    }
    
    /* Request code for Open operation. */
    final static int CONN_OPEN = 0;
    /* Request code for Close operation. */
    final static int CONN_CLOSE = 1; 
    /* Request code for Init operation. */
    final static int CONN_INIT = 2;
    /* Request code for Send operation. */
    final static int CONN_SEND = 3;
    /* Request code for Receive operation. */
    final static int CONN_RECEIVE = 4;
    
    /*
     * Processes request from porting layer to emulation.
     * @param request packed request to process
     */
    public void process(BytePack request) {
        
        switch (request.extract()) {
        case CONN_OPEN:
            Log.log("Processing CONN_OPEN " + handle); 
            serviceData.address = request.extractBytes(Const.BTADDR_SIZE);
            serviceData.port = request.extractInt();
            opener = new Opener();
            opener.start();
            break;
            
        case CONN_CLOSE:
            Log.log("Processing CONN_CLOSE " + handle); 
            close();
            break;
        
        case CONN_INIT:
            Log.log("Processing CONN_INIT " + handle); 
            serviceData = new ServiceConnectionData(
                request.extractBytes(ServiceConnectionData.SERVER_DATA_SIZE),
                ServiceConnectionData.SERVER_DATA);
            break;
        
        case CONN_SEND:
            Log.log("Processing CONN_SEND " + handle); 
            send(request.extractInt());
            break;
        
        case CONN_RECEIVE:
            Log.log("Processing CONN_RECEIVE " + handle); 
            receive();
            break;
            
        default:
            Log.log("Processing CONN_UNKNOWN " + handle); 
            throw new EmulationException("Unknown connection request");
        }
    }

    /*
     * Retrieves new handle for a newly created connection at server side.
     * @param protocol integer protocol identifier
     * @return integral handle for a newly created connection
     */
    private static native int getHandle(int protocol);
    
    /*
     * Retrieves bytes from output native buffer for sending.
     * @param handle this connection handle
     * @param buf buffer to copy data to, it is implied that
     *        exactly <code>buf.length</code> to be sent from
     *        the native buffer
     * @return <code>true</code> if there is data to be sent,
     *         <code>false<code> otherwise
     */
    private static native boolean getOutData(int handle, byte[] buf);
    
    /* 
     * Notifies porting layer on receive operation completion.
     * @param handle connection handle
     * @param bytes bytes received
     * @param len amount of bytes received, <code>Const.CONN_FAILURE</code> 
     *        if receiving failed, <code>Const.CONN_ENDOF_INP</code> if
     *        there is nothing to receive due reaching end of input stream.
     */
    static native void notifyReceived(int handle, byte[] bytes, int len);
    
    /* 
     * Notifies porting layer on send operation completion.
     * @param handle connection handle
     * @param len amount of bytes sent, <code>CONN_FAILURE</code> if sending
     *        failed
     */
    static native void notifySent(int handle, int len);
    
    /* 
     * Notifies porting layer on open operation completion.
     * @param handle this connection handle
     * @param success <code>true</code> if opened successfully, 
     *        <code>false</code> otherwize
     * @param receiveMTU receiveMTU of L2CAP connection established,
     *        ignored for RFCOMM
     * @param transmitMTU transmitMTU of L2CAP connection established,
     *        ignored for RFCOMM
     */
    private static native void notifyOpen(int handle, boolean success,
        int receiveMTU, int transmitMTU);
}

/*
 * Implements simple send methods that just call 
 * proper stream methods from a separate thread.
 */
class Sender extends RunnableProcessor {
    /* Socket output stream. */
    private OutputStream out;
    /* Data being sent. */
    protected byte[] outbuf;
    /* Emulated connection handle. */
    int handle;
    
    /*
     * Creates proper instance depending on protocol
     * @param protocol protocol (as in <code>BluetoothUrl</code>) to create
     *        sernder for
     * @param socketConnection open socet connection to use for sending
     * @param handle handle of emulated connection to create sender for
     * @return instance created
     */
    static Sender create(SocketConnection socketConnection, int protocol,
                int handle) {
        
        Sender res = null;
        try {
            res = protocol == BluetoothUrl.L2CAP ? 
                new L2CAPSender(socketConnection, handle) :
                new Sender(socketConnection, handle);
        } catch (IOException e) {
            ConnectionEmul.notifySent(handle, Const.CONN_FAILURE);
        }
        
        return res;
    }
    
    /*
     * Initiates streams.
     * @param socketConnection open socet connection to use. 
     * @param handle handle of emulated connection this sender works for
     * Exception IOException if I/O error occured while opening streams
     */
    Sender(SocketConnection socketConnection, int handle) throws IOException {
        super(true);
        this.handle = handle;
        out = socketConnection.openDataOutputStream();
    }
    
    /* Processes send request. */
    public void process() {
        try {
            sendImpl();
        } catch (IOException e) {
            outbuf = null;
        }
        
        if (!interrupted) {
            int sent = outbuf == null ? Const.CONN_FAILURE : outbuf.length;
            ConnectionEmul.notifySent(handle, sent);
        }
        outbuf = null;
    }
    
    /* Closes assigned streams. */
    void close() {
        interrupt();
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // ignoring
            }
            out = null;
        }
    }
    
    /*
     * Queues givem data for sending.
     * @param buf bytes to send.
     */
    void send(byte[] buf) {
        if (outbuf != null) {
            throw new EmulationException("Unexpected sending conflict");
        }
        outbuf = buf;
        start();
    }

    /*
     * Sends data to the other side of the emulated connection.
     * @exception IOException  if an I/O error occurs
     */
    protected void sendImpl() throws IOException {
        plainSend(outbuf);
    }
    
    /*
     * Sends data by simple OutputStream.write(), ignoring all other 
     * logic impied by Sender and subclasses.
     * @param buf the data to send
     */
    void plainSend(byte[] buf) throws IOException {
        if (out != null) {
            out.write(buf, 0, buf.length);
            out.flush();
        }
    }
}

/*
 * Reloads send methods in L2CAP-specific way. The L2CAP spacific 
 * is discarding bytes sent in a time by one side and not received by 
 * another.
 */
class L2CAPSender extends Sender {
    /* 
     * Creates an instance.
     * @param socketConnection open socet connection to use. 
     * @param handle handle of emulated connection this sender works for
     * Exception IOException if I/O error occured while opening streams
     */
    L2CAPSender(SocketConnection socketConnection, int handle) 
            throws IOException {
        super(socketConnection, handle);
    }
    
    /*
     * Sends data to the other side of the emulated connection.
     * @exception IOException  if an I/O error occurs
     */
    protected void sendImpl() throws IOException {
        byte[] lenbytes = new byte[2];
        lenbytes[0] = (byte)(outbuf.length & 0xff);
        lenbytes[1] = (byte)((outbuf.length >> 8) & 0xff);
        
        plainSend(lenbytes);
        plainSend(outbuf);
    }

}


/*
 * Implements simple receive methods that just call 
 * proper I/O streams methods from a separate thread.
 */
class Receiver extends RunnableProcessor {
    /* Socket input stream. */
    private DataInputStream in;
    /* Byes received. */
    byte[] inbuf;
    /* Handle of emulated connection this receiver works for. */
    int handle;
    
    /*
     * Creates proper instance depending on protocol
     * @param protocol protocol (as in <code>BluetoothUrl</code>) to create
     *        receiver for
     * @param socketConnection open socket connection to use for receiving
     * @param handle handle of emulated connection to create receiver for
     * @return instance created
     * Exception IOException if I/O error occured while opening streams
     */
    static Receiver create(SocketConnection socketConnection, int protocol,
                int handle) {
        
         Receiver res = null;
         try {
            res = protocol == BluetoothUrl.L2CAP ? 
                new L2CAPReceiver(socketConnection, handle) :
                new Receiver(socketConnection, handle);
        } catch (IOException e) {
            ConnectionEmul.notifyReceived(handle, null, Const.CONN_FAILURE);
        }
        
        return res;
    }
    
    /*
     * Initiates streams.
     * @param socketConnection open socet connection to use. 
     * @param handle handle of emulated connection this receiver works for
     * Exception IOException if I/O error occured while opening streams
     */
    Receiver(SocketConnection socketConnection, int handle) 
            throws IOException {
        super(true);
        this.handle = handle;
        in = socketConnection.openDataInputStream();
    }
    
    /* Processes receive request. */
    public void process() {
        int len;
        try {
            len = receiveImpl();
        } catch (Throwable e) {
            len = Const.CONN_FAILURE;
        }
        
        if (!interrupted) {
            ConnectionEmul.notifyReceived(handle, inbuf, len);
        }
        inbuf = null;
    }
    
    /* Closes assigned stream. */
    void close() {
        interrupt();
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // ignoring
            }
            
            in = null;
        }
    }
    
    /* Initiates receving data. */
    void receive() {
        start();
    }

    /*
     * Reads data from emulated connection to given buffer.
     * @return total number of bytes read into the buffer,
     *         <code>-1</code> if there is nothing to read.
     *         
     * @exception  IOException  if an I/O error occurs.
     */
    protected int receiveImpl() throws IOException {
        if (interrupted) {
            throw new IOException();
        }
        
        int len = in.available();
        if (len >= 0) {
            if (len == 0) {
                len = 1;
            }
            
            inbuf = new byte[len];
            len = in.read(inbuf, 0, len);
        }
        
        return len;
    }
    
    /*
     * Reads exactly requested amount of bytes form input stream.
     * Blocks until they appear.
     * @param len amount of bytes to read
     * @return newly created byte array that contains read bytes
     * @exception IOException if reading fails or connection is closed 
     *            while waiting for input
     */
    byte[] forceReceive(int len) throws IOException {
        byte[] bytes = new byte[len];
        int offset = 0;
        in.readFully(bytes); 

        return bytes;
    }
}

/*
 * Reloads receive methods in L2CAP-specific way. The L2CAP specific 
 * is discarding bytes sent in a time by one side and not received by 
 * another.
 */
class L2CAPReceiver extends Receiver {
    /* 
     * Creates an instance.
     * @param socketConnection open socet connection to use. 
     * @param handle handle of emulated connection this receiver works for
     * Exception IOException if I/O error occured while opening streams
     */
    L2CAPReceiver(SocketConnection socketConnection, int handle) 
            throws IOException {
        super(socketConnection, handle);
    }
    
    /*
     * Reads data from emulated connection. It receves exact amount
     * of bytes that is sent by other side, if requested amount is less
     * than actually read, last read bytes are discarded. 
     *
     * @return total number of bytes read into <code>inbuf</code>
     * @exception IOException  if an I/O error occurs
     */
    protected int receiveImpl() throws IOException {
        if (interrupted) {
            throw new InterruptedIOException();
        }
        
        try {
            inbuf = forceReceive(2);
        } catch (IOException e) {
            // It is OK, just the end of stream reached
            return Const.CONN_ENDOF_INP;
        }
        
        int packLen = (inbuf[0] & 0xff) | ((inbuf[1] & 0xff) << 8);
        inbuf = forceReceive(packLen);
        
        return packLen;
    }
}
