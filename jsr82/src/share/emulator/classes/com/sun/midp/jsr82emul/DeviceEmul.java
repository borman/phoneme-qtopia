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
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.DiscoveryAgent;

import com.sun.jsr082.bluetooth.DiscoveryAgentImpl;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.midp.main.Configuration;
import com.sun.midp.log.Logging;
import com.sun.jsr082.bluetooth.BCC;
import com.sun.jsr082.bluetooth.BluetoothUtils;

/*
 * Emulates a Bluetooth device.
 */
public class DeviceEmul extends EmulationClient
        implements EmulUnit {
    /*
     * Represents and perfoms inquiry.
     * Constructing an instance starts inquiry in a new thread.
     */
    private class Inquiry implements Runnable {
        /* Access code parameter for the inquiry. */
        int accessCode;

        /* Indicates if this inquiry has been cancelled. */
        private boolean cancelled = false;

        /* Inquiry thread. */
        private Thread thread;

        /*
         * Constructs an instance and starts corresponding inquiry thread.
         * @param accessCode access code to be used within the inqiry
         */
        Inquiry(int accessCode) {
            this.accessCode = accessCode;
            thread = new Thread(this);
            thread.start();
        }

        /* Cancells current inquiry. */
        synchronized void cancel() {
            // not interrupting the corresponding thread to let it finish
            // emulation server communications
            cancelled = true;
        }

        /*
         * Implements <code>run()</code> of <code>Runnable</code> running
         * the inquiry
         */
        public void run() {
            try {
                InquiryResults inquiryResults;

                synchronized (serverTransaction) {
                    messenger.sendInt(toServer, Messenger.START_INQUIRY,
                        accessCode);

                    // let other threads work while the inquire is processed by
                    // emulation server
                    Thread.yield();

                    messenger.receive(fromServer);

                    if (messenger.getCode() != Messenger.INQUIRY_COMPLETED) {
                        throw new EmulationException();
                    }

                    inquiryResults = new InquiryResults(
                        messenger.getBytes());
                }

                byte[][] addresses = inquiryResults.getAddresses();
                int[] classes = inquiryResults.getClasses();

                for (int i = 0; i < addresses.length; i++) {
                    if (CheckCancelAndReportDiscovery(
                            addresses[i], classes[i])) {
                        break;
                    }
                }

                if (!cancelled) {
                    inquiryCompleted(true);
                }

            } catch (Throwable e) {
                inquiryCompleted(false);
             }

        }

        /*
         * Checks if inquiry cancelled and reports on device discovery to
         * listener if it is not.
         *
         * @param btaddr BluetoothAddress of device discovered
         * @param cod class of device discovered
         * @return <code>true</code> if inquiry is cancelled,
         *         <code>false</code> otherwise
         */
        private synchronized boolean CheckCancelAndReportDiscovery(
                byte[] btaddr, int cod) {

            if (!cancelled) {
                deviceDiscovered(btaddr, cod);
            }
            return cancelled;
        }
    }

    /* Initial access code. */
    private static int DEFAULT_AC = DiscoveryAgent.GIAC;
    
    /* Keeps current inquiry if any. */
    private Inquiry curInquiry = null;

    /* Bluetooth address. */
    byte[] address = null;
    
    /* Device state i.e. discoverable mode and device class. */
    private DeviceState deviceState = null;

    /* Device emulation for the local device. */
    private static DeviceEmul localDeviceEmul = null;

    /*
     * Constructs an emulation instance and retrieves addres for it from
     *        emulation server.
     */
    public DeviceEmul() {
        try {
            connect();

            messenger.sendBytes(toServer, Messenger.REGISTER_DEVICE, 
                    getLocalIpBytes());
            messenger.receive(fromServer);

            if (messenger.getCode() != Messenger.REGISTERED) {
                throw new IOException("Error communicating emulation server");
            }
            
            address = messenger.getBytes();
            int cod = initDevice(address, DEFAULT_AC);
            deviceState = new DeviceState(cod, DEFAULT_AC);
            updateState();
            
            Log.log("DeviceEmul: my address is " +
                    BluetoothUtils.getAddressString(address));
        } catch (IOException e) {
            throw new EmulationException(
                "Error initializing local device emulation");
        }
    }
    
    /* 
     * Initializes local device parameters in shared emulation storage.
     * @param addr Bluetoth address bytes retrieved form emulation server
     * @param ac initial access code
     * @return initial class of device with service classes that are possibly
     *         saved after previous usage of device with the same address
     */
    private native int initDevice(byte[] addr, int ac);

    /*
     * Returns instance of this class for local device emulation.
     *
     * @return the device emulation object for the local device
     */
    public static synchronized DeviceEmul getLocalDeviceEmul() {
        if (localDeviceEmul == null) {
            localDeviceEmul = new DeviceEmul();
        }
        return localDeviceEmul;
    }

    /*
     * Retrieves IP address.
     * @return host computer IP address as byte array.
     */
    private byte[] getLocalIpBytes() {
        String ip = EmulationClient.getLocalIP();
        byte[] res = null;
        
        if (ip != null && ip.length() > 0) {
            int from = 0;
            int to = 0;
            byte[] parsed = new byte[4];
            
            try {
                for (int i = 0; i < 4; i++) {
                    to = ip.indexOf('.', from);
                    if (to < 0) {
                        to = ip.length();
                    }
                    parsed[i] = (byte)Integer.parseInt(ip.substring(from, to));
                    from = to + 1;
                }
                
                res = parsed;
            } catch (NumberFormatException e) {
                // res == null idenitifies retrieving failure
            }
        }
        
        if (res == null) {
            res = new byte[] {127, 0, 0, 1};
        }
        return res;
    }

    /*
     * Returns address of this device.
     * @return Bluetooth address
     */
    public byte[] getAddress() {
        return address;
    }

    /*
     * Registers service at emulation server.
     * @param serviceData combined service connection info
     * @throws IOException if connection to emulation server failed.
     */
    void registerService(ServiceConnectionData serviceData)
            throws IOException {

        synchronized (serverTransaction) {
            messenger.sendBytes(toServer, Messenger.REGISTER_SERVICE,
                serviceData.toByteArray(ServiceConnectionData.SERVER_DATA));
        }
    }

    /*
     * Unregisters service at emulation server.
     * @param serverSocketPort socket port desired service
     *        accepted connections at
     */
    void unregisterService(int serverSocketPort) {
        try {
            messenger.sendInt(toServer, Messenger.UNREGISTER_SERVICE,
                serverSocketPort);
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Unregistering service failed");
            }
        }
    }

    /* Sends device state update to emulation server. */
    private void updateState() {
        try {
            messenger.sendInt(toServer,
                Messenger.UPDATE_DEVICE_STATE, deviceState.toInt());
        } catch (IOException e) {
            throw new EmulationException(e.getMessage());
        }
    }

    /*
     * Starts inquiry.
     * @param accessCode access code of desired devices
     */
    private synchronized void startInquiry(int accessCode) {
        curInquiry = new Inquiry(accessCode);
    }

    /*
     * Cancels current inquiry.
     */
    private synchronized void cancelInquiry() {
        if (curInquiry != null) {
            curInquiry.cancel();
        }
    }
    
    /* 
     * Notifies on device discovery. 
     * @param addr Bluetooth address of device discovered
     * @param cod class of device discovered
     */
    private native void deviceDiscovered(byte[] addr, int cod);
    /* 
     * Notifies on inquiry completion.
     * @param success true if completed successfully, false otherwize
     */
    private native void inquiryCompleted(boolean success);

    /* Request code for updating service classes. */
    static final int UPDATE_CLASS = 0;
    /* Request code for updating access code. */
    static final int UPDATE_ACCESS = 1;
    /* Request code for updating starting inquiry. */
    static final int START_INQUIRY = 2;
    /* Request code for cancelling inquiry. */
    static final int CANCEL_INQUIRY = 3;
    /* Request code for initing devie. */
    static final int INIT_DEVICE = 4;
    
    /*
     * Processes the request.
     * @param request Packeted request
     */
    public void process(BytePack request) {
        switch (request.extract()) {
        case UPDATE_CLASS:
            Log.log("Processing UPDATE_CLASS");
            deviceState.setServiceClasses(request.extractInt());
            updateState();
            break;
        case UPDATE_ACCESS:
            Log.log("Processing UPDATE_ACCESS");
            deviceState.setDiscoverable(request.extractInt());
            updateState();
            break;
        case START_INQUIRY:
            Log.log("Processing START_INQUIRY");
            startInquiry(request.extractInt());
            break;
        case CANCEL_INQUIRY:
            Log.log("Processing CANCEL_INQUIRY");
            cancelInquiry();
            break;
        case INIT_DEVICE:
            Log.log("Processing INIT_DEVICE");
            // Nothing to do: the request has already caused 
            // construction of all the objects required.
            break;
        }
    }
    
    /* 
     * Saves device information in persistent storage for future use. 
     */
    private native void finalize();
}

/*
 * Utility class that allows packing inquiry results in bytes array.
 */
class InquiryResults {
    /* Amount of devices discovered. */
    int count = 0;

    /* Size of Bluetooth address byte representation. */
    private static final int ADDRESS_SIZE = Const.BTADDR_SIZE;
    /* Size of device class byte representation. */
    private static final int COD_SIZE = 3;
    /* Size of one result. */
    private static final int RESULT_SIZE = ADDRESS_SIZE + COD_SIZE;

    /* Addresses of discovered devices if not <code>null</code>. */
    private byte[][] addresses;
    /* Classes of discovered devices if not <code>null</code>. */
    private int[] classes = null;

    /* Inquiry results: Bluetooth address, device class pairs. */
    Vector results;

    /* Constructs an instance for filling up. */
    InquiryResults() {
        results = new Vector();
    }

    /*
     * Constructs an instance by byte representation and unpacks it to
     * normal addresses and device classes.
     *
     * @param data byte representation of inquiry results.
     */
    InquiryResults(byte[] data) {
        if (data == null || (data.length % RESULT_SIZE) != 0) {
            throw new IllegalArgumentException();
        }

        count = data.length / RESULT_SIZE;
        addresses = new byte[count][ADDRESS_SIZE];
        classes = new int[count];

        for (int i = 0; i < count; i++) {
            int j = i * RESULT_SIZE;

            classes[i] = (data[j++] & 0xff) | ((data[j++] & 0xff) << 8) |
                        ((data[j++] & 0xff) << 16);

            System.arraycopy(data, j, addresses[i], 0, ADDRESS_SIZE);
        }
    }

    /*
     * Returns classes of discovered devices.
     * @return int array that contains classes of discovered devices
     */
    int[] getClasses() {
        if (classes == null) {
            throw new IllegalArgumentException();
        }
        return classes;
    }

    /*
     * Returns addresses of discovered devices
     * @return array of bluetooth addresses, byte representation.
     */
    byte[][] getAddresses() {
        if (addresses == null) {
            throw new IllegalArgumentException();
        }
        return addresses;
    }

    /*
     * Adds new device discovered to results.
     * @param btaddr Bluetooth address of device discovered
     * @param cod class of device discovered
     */
    void add(byte[] btaddr, int cod) {
        byte[] bytes = new byte[RESULT_SIZE];

        bytes[0] = (byte)(cod & 0xff);
        bytes[1] = (byte)((cod >> 8) & 0xff);
        bytes[2] = (byte)((cod >> 16) & 0xff);

        System.arraycopy(btaddr, 0,
                bytes, COD_SIZE, ADDRESS_SIZE);

        results.addElement(bytes);
    }

    /*
     * Packs results to byte representation.
     * @return byte array that represent current results
     */
    byte[] toByteArray() {
        count = results.size();
        byte[] data = new byte[count * RESULT_SIZE];

        for (int i = 0; i < count; i++) {
            System.arraycopy((byte[])results.elementAt(i), 0,
                data, i * RESULT_SIZE, RESULT_SIZE);
        }

        return data;
    }
}

/*
 * Utility class that allows packing device class and discoverable mode into
 * single integer.
 */
class DeviceState {
    /* Packed information. */
    private int data = 0;
    
    /* Mask for highlighting device class. */
    private static final int DEVICE_CLASS = 0x1ffc;
    /* Mask for highlighting device class. */
    private static final int SERV_CLASSES = 0xffe000;
    /* 
     * Mask for highlighting entire device class including device and 
     * service classes. 
     */
    private static final int COD = DEVICE_CLASS | SERV_CLASSES;
    /* Mask for highlighting a bit that shows device discoverable mode. */
    private static final int DISCOVERABLE = 0xff000000;
    /* LIAC bit. */
    private static final int LIAC = 0x01000000;
    /* GIAC bit. */
    private static final int GIAC = 0x02000000;
    /* Uniscoverable. */
    private static final int UNDISCOVERABLE = 0;

    /*
     * Constructs an instance with given value.
     * @param cod value for class of device
     * @param mode value for discoverable mode
     */
    DeviceState(int cod, int mode) {
        data = cod;
        setDiscoverable(mode);
    }

    /*
     * Constructs default state that is undiscoverable and has invalid
     * device class.
     */
    DeviceState() {
        this.data = UNDISCOVERABLE;
    }

    /*
     * Retrieves integer representation.
     * @return integer representation
     */
    int toInt() {
        return data;
    }

    /*
     * Retrieves entire class of device including device and service classes.
     * @return integer value that represents class of device.
     */
    int getCoD() {
        return data & COD;
    }

    /*
     * Returns discoverable mode.
     * @return integer that represents discoverable mode.
     */
    int getDiscoverable() {
        switch (data & DISCOVERABLE) {
            case LIAC: return DiscoveryAgent.LIAC;
            case GIAC: return DiscoveryAgent.GIAC;
            default: return DiscoveryAgent.NOT_DISCOVERABLE;
        }
    }

    /*
     * Sets service classes of device.
     * @param classes new service classes value
     */
    void setServiceClasses(int classes) {
        data =  data & (DISCOVERABLE | DEVICE_CLASS) | classes;
    }

    /*
     * Sets discoverable mode to given one.
     * @param mode discoverable mode to set to
     * @return true if the discoverable mode is supported, otherwise - false
     */
    boolean setDiscoverable(int mode) {
        int bits = 0;
        boolean ret = true;
        switch (mode) {
            case DiscoveryAgent.LIAC:
                bits = LIAC;
                break;
            case DiscoveryAgent.GIAC:
                bits = GIAC;
                break;
            case DiscoveryAgent.NOT_DISCOVERABLE:
                bits = UNDISCOVERABLE;
                break;
            default:
                ret = false;
                break;
        }
        if (ret) {
            data = bits | data & DEVICE_CLASS;
        }
        return ret;
    }

    /*
     * Updates values from new integer representation.
     * @param data new integer representation
     */
    void update(int data) {
        this.data = data;
    }
}
