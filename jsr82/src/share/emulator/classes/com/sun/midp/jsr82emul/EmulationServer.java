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
import java.util.Hashtable;
import java.util.Enumeration;

import javax.microedition.io.SocketConnection;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.io.j2me.serversocket.Socket;
import com.sun.midp.main.Configuration;
import com.sun.jsr082.bluetooth.BluetoothUtils;
import com.sun.midp.jsr082.SecurityInitializer;

import javax.bluetooth.BluetoothConnectionException;

/*
 * Represents an emulation server used for JSR 82 emulation environment. 
 * It is not a part of JSR 82 implementation and is only used within 
 * JSR 82 emulation mode. The emulation mode allows running tests without
 * real native Bluetooth libraries or hardware.
 * 
 * In the emulation mode the server runs in a sepate thread or as a 
 * standalone application. It emulates Bluetooth ether, i.e. keeps 
 * information on services being advertised and "devices" that present.
 *
 * Actually it keeps TCP socket connections to clients.
 */
public class EmulationServer implements Runnable {
    /* Shows if emulation server is already running. */
    private static boolean launched = false;
    
    /* The only instance of <code>EmulationServer</code>. */
    private static EmulationServer instance = null;

    /*
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /* This class has a different security domain than the MIDlet suite. */
    private static SecurityToken internalSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());
    
    /* Server socket that accepts TCP clients connections. */
    private static Socket serverSocket;
    
    // IMPL_NOTE: make it configurable.
    /* Port to open server socket at. */
    private static final int SOCKET_PORT = Configuration.getIntProperty("com.sun.midp.jsr82emul.serverPort", 1234);
    /* 
     * Duration of a delay that is applied prior to start inquiry in 
     * order to let all emulated devices update their device classes.
     */
    private static final int INQUIRY_DELAY = 2000;
    
    /* Keeps services being advertised in the ether. */
    private Hashtable services = new Hashtable();
    
    /* Keeps devices that present in the ether. */
    private Hashtable devices = new Hashtable();
    
    /* Default Bluetooth address wrapped by DeviceKey objects. */
    private static final DeviceKey[] defaultAddr = {
        new DeviceKey(BluetoothUtils.getAddressBytes(Configuration.getProperty(
            "com.sun.midp.jsr82emul.localBluetoothAddress"))),
        new DeviceKey(BluetoothUtils.getAddressBytes(Configuration.getProperty(
            "com.sun.midp.jsr82emul.localBluetoothAddress2"))) };
    /* Amount of default addresses. */
    static final int ADDR_COUNT = 2;
    
    /* Value used to generate unique Bluetooth addresses. */
    int nextAddr = 1;
    
    /*
     * Launches the server in a standalone manner.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
    
    /*
     * Launches the server if not yet running.
     */
    public static synchronized void launch() {
        if (launched) {
            return;
        }
        
        try {
            serverSocket = new Socket();
            serverSocket.open(SOCKET_PORT, internalSecurityToken);
            
            instance = new EmulationServer();
            Thread thread = new Thread(instance);
            thread.start();        
        } catch (IOException e) {
            // server is already running within another isolate or under
            // another VM control - nothing to do
        } finally {
            launched = true;
        }
    }
    
    /* 
     * Retrns the only instance if the server. 
     * @return the singleton instance of <code>EmulationServer</code>.
     */
    public static EmulationServer getInstance() {
        launch();
        return instance;
    }

    /*
     * The Runnable interface implementation.
     */
    public void run() {
        while (true) {
            try {
                new ClientHandler(
                        (SocketConnection)serverSocket.acceptAndOpen());
            } catch (Throwable e) {
                // ignoring
            }
        }
    }
    
    /* 
     * Registers new device in the ether.
     *
     * @param deviceState state of device that defines its current discoverable
     * mode and class of device
     * @return Bluetooth addres for the registered device that identifies it 
     * in the emulated ether. 
     */
    public synchronized byte[] registerDevice(DeviceState deviceState) {
        DeviceKey key = null;
        int i;
        
        for (i = 0; i < ADDR_COUNT; i++) {
            if (!devices.containsKey(defaultAddr[i])) {
                key = defaultAddr[i];
                break;
            }
        }
        
        if (i == ADDR_COUNT) {
            byte[] btaddr = new byte[Const.BTADDR_SIZE];
            for (i = 0; i < 4; i++) {
                btaddr[i] = (byte) (nextAddr >> (8 * i));
            }
            key = new DeviceKey(btaddr);
            nextAddr = nextAddr % (Integer.MAX_VALUE - 1) + 1;
        }
        
        devices.put(key, deviceState);
        return key.getAddrBytes();
    }
    
    /*
     * Unregisters device from the emulated ether.
     *
     * @param btaddr Bluetooth address of the device to be unregistered
     */
    public synchronized void unregisterDevice(byte[] btaddr) {
        devices.remove(new DeviceKey(btaddr));
    }
    
    /*
     * Performs inquiry (devices discovery).
     * @param discoverable discoverable mode to search devices with
     * @param btaddr Bluetooth address of device that performs inquiry - it
     *        should not discover itself.
     * @return <code>InquiryResults</code> instance that represents inquiry
     *        results
     */
    public InquiryResults runInquiry(int discoverable, byte[] btaddr) {
        try {
            // Syncronizing classes of devices for the scenario like this:
            //     - make an action that should update class of device1;
            //     - start inquiry on device2 expecting updated class of device1.
            // In this case class of device1 changes immediately locally but
            // it takes time to update it on the emulation server and affect all
            // inquiry operations. Waiting here to let class of device1 to become
            // up-to-date. 
            Thread.sleep(INQUIRY_DELAY);
        } catch (InterruptedException e) {}
        
        InquiryResults res = new InquiryResults();
        Enumeration addresses = devices.keys();
        Enumeration states = devices.elements();
                    
        while (addresses.hasMoreElements()) {
            DeviceKey addr = (DeviceKey) addresses.nextElement();
            DeviceState state = (DeviceState) states.nextElement();
             
            if (state.getDiscoverable() == discoverable && 
                    !addr.equals(btaddr)) {
                res.add(addr.getAddrBytes(), state.getCoD());
            }
        }
        
        return res;
    }
    
    /* 
     * Registers a service in Bluetooth ether, i.e. starts advertising it.
     *
     * @param service service connection data provided by service holder
     * @param btaddr Bluetooth address of service holder
     * @return service key that identifies the service at emulation server,
     *        if registration succeeded, <code>null</code> otherwize
     */
    public ServiceKey registerService(
            ServiceConnectionData service, byte[] btaddr) {
        
        ServiceKey key = new ServiceKey(
            btaddr, service.protocol, service.port);
        
        Log.log("SERVER: registreing service " + key);
        
        ServiceConnectionData registered = 
            (ServiceConnectionData) services.get(key);
            
        if (registered != null) {
            registered.setAccepting();
        } else {
            services.put(key, service);
        }
        
        return key;
    }
    
    /* 
     * Stops advertising the service represented by given record.
     * @param key service key that represents the service to be unregistered
     */
    public void unregisterService(ServiceKey key) {
        Log.log("SERVER: unregistreing service " + key);
        services.remove(key);
    }
    
    /* 
     * Retrieves emulation (TCP) connection URL for connecting to the
     * service represented by given JSR 82 client connection string.
     *
     * @param client client connection parameters
     * @return <code>ServiceConnectionData</code> instance that either
     *         contains connection properties or error code
     */
    public ServiceConnectionData connectToService(
            ServiceConnectionData client) {
        
        ServiceKey key = new ServiceKey(client.address, 
            client.protocol, client.port);
        ServiceConnectionData service = 
            (ServiceConnectionData) services.get(key);
        
        // Log.log("SERVER: requested connection to service " + key);
        
        if (service == null) {
            // indicates error
            client.error = BluetoothConnectionException.FAILED_NOINFO;
            // Log.log("SERVER: service not found " + key);
        } else {
            // either accepts or sets error value
            service.accept(client);
        }
        
        return client;
    }
}

/*
 * Represetnts key for services registry. A service is identified by
 * Bluetooth address of host device, protocol type and PSM or channel
 * id.
 */
class ServiceKey {
    /* Bluetooth address of the advertising server. */
    DeviceKey btaddr;
    /* 
     * Protocol this service is connectable thru. Is one of 
     * <code>BluetoothUrl.L2CAP, BluetoothUrl.RFCOMM</code
     */
    int protocol;
    /* Channel in case of RFCOMM or PSM in case of L2CAP. */
    int channelOrPsm;
    
    /* 
     * Constructs a key.
     * @param btaddr value for <code>bluetoothAddress</code>
     * @param protocol value for <code>protocol</code>
     * @param channelOrPsm value for <code>channelOrPsm</code> 
     */
    ServiceKey(byte[] btaddr, int protocol, int channelOrPsm) {
        this.btaddr = new DeviceKey(btaddr);
        this.protocol = protocol;
        this.channelOrPsm = channelOrPsm;
    }
        
    /* 
     * Checks if this key is equivalent to given one.
     * @param obj a <code>ServiceKey</code> instance to be compared
     *        to this one
     * @return true if this key is equal to the given one,
     *        false otherwise.
     */
    public boolean equals(Object obj) {
        ServiceKey rec = (ServiceKey) obj; 
        
        return btaddr.equals(rec.btaddr) &&
            protocol == rec.protocol &&
            channelOrPsm == rec.channelOrPsm;
        }
        
    /* 
     * Returns a proper hash code for using in Hashtable.
     * @return hash code value for this instance
     */
    public int hashCode() {
        return channelOrPsm;
    }
}
    
/* 
 * Wrapper for bluetooth address bytes that allows keeping 
 * and comparing Bluetooth addresses. Used as a device key
 * in Hashtable.
 */
class DeviceKey {
    /* Bluetooth addres. */
    private final byte[] addr = new byte[Const.BTADDR_SIZE];
    
    /*
     * Creates instance with given addres. 
     * @param addr Bluetooth address
     */
    public DeviceKey(byte[] addr) {
        System.arraycopy(addr, 0, this.addr, 0, Const.BTADDR_SIZE);
    }
    
    /* 
     * Returns a proper hash code for using in Hashtable.
     * @return hash code value for this instance
     */
    public int hashCode() {
        return addr[0];
    }
    
    /* 
     * Checks if this instance and given one wrap the same address. 
     * @param obj object to check for equity
     * @return true if given object represents the same address,
     *        false otherwise
     */
    public boolean equals(Object obj) {
        return equals(((DeviceKey)obj).addr);
    }
        
    /* 
     * Checks if this instance wraps the same address as given one. 
     * @param addr Bluetooth address to check for equity
     * @return true if addresses are equal, false otherwise
     */
    public boolean equals(byte[] addr) {
        for (int i = 0; i < Const.BTADDR_SIZE; i++) {
            if (addr[i] != this.addr[i]) {
                return false;
            }
        }
        
        return true;        
    }
    
    /* 
     * Returns bytes representation of wrapped Bluetooth address. 
     * @return Bluetooth address bytes
     */
    public byte[] getAddrBytes() {
        return addr;
    }
}
