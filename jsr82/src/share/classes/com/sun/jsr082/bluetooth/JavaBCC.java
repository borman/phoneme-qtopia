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
package com.sun.jsr082.bluetooth;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.Hashtable;
import com.sun.midp.jsr82emul.DeviceEmul;
import com.sun.midp.main.Configuration;
import java.util.Vector;

/*
 * Java-based Bluetooth Control Center.
 */
public class JavaBCC extends BCC {

    /*
     * Structure containing information on a remote device.
     */
    private class PicoNode {
        /* Number of open connections. */
        int connections;
        /* Indicates whether the device has been authenticated. */
        boolean authenticated;
        /* Number of encrypted connections. */
        int encrypted;
    }

    /* Hashtable of PicoNodes. */
    private Hashtable piconet = new Hashtable();

    /* Inquiry access code for the local device. */
    private int accessCode = DiscoveryAgent.GIAC;

    /* Device class without service classes. */
    private int deviceClassBase = 0x204; // 0x200 - phone (major class),
                                         // 0x04 - cellular (minor class)

    /* Device class and service classes of the local device. */
    private DeviceClass deviceClass;

    /* Identifies weither the Bluetooth device is on. */
    private boolean isBluetoothEnabled = true;

    /* The friendly name for the device. */
    private String friendlyName = "BT Emulation";

    /* Pass key for the device. */
    private String passKey = "0000";


    /* Constructs an instance. */
    protected JavaBCC() {
        loadPropertyValues();
        deviceClass = new DeviceClass(deviceClassBase);
    }

    /*
     * Enables Bluetooth radio and the Bluetooth protocol stack for use.
     *
     * @return <code>true</code> if the operation succeeded,
     *         <code>false</code> otherwise
     */
    public boolean enableBluetooth() {
        isBluetoothEnabled = true;
        return true;
    }

    /*
     * Queries the power state of the Bluetooth device.
     *
     * @return <code>true</code> is the Bluetooth device is on,
     *         <code>false</code> otherwise.
     */
    public boolean isBluetoothEnabled() {
         return isBluetoothEnabled;
    }

    /*
     * Returns local Bluetooth address.
     * @return local Bluetooth address
     */
    public String getBluetoothAddress() {
        return com.sun.midp.jsr082.BluetoothUtils.getAddressString(
            DeviceEmul.getLocalDeviceEmul().getAddress());
    }

    /*
     * Returns user-friendly name for the local device.
     *
     * @return User-friendly name for the local device, or
     *         <code>null</code> if the name could not be retrieved
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /*
     * Retrieves the user-friendly name for specified remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return name of the remote device, or
     *         <code>null</code> if the name could not be retrieved
     */
    public String getFriendlyName(String address) {
        return address;
    }

    /*
     * Checks if the local device is in connectable mode.
     *
     * @return <code>true</code> if the device is connectable,
     *         <code>false</code> otherwise
     */
    public boolean isConnectable() {
        return true;
    }

    // JAVADOC COMMENT ELIDED
    public DeviceClass getDeviceClass() {
        return deviceClass;
    }

    // JAVADOC COMMENT ELIDED
    public boolean setServiceClasses(int classes) {
        deviceClass = new DeviceClass(deviceClassBase | classes);
        DeviceEmul.getLocalDeviceEmul().updateDeviceClass(deviceClass);
        return true;
    }

    // JAVADOC COMMENT ELIDED
    public int getAccessCode() {
        return accessCode;
    }

    // JAVADOC COMMENT ELIDED
    public boolean setAccessCode(int accessCode) {
        if (DeviceEmul.getLocalDeviceEmul().updateDiscoverable(accessCode)) {
            this.accessCode = accessCode;
            return true;
        }
        return false;
    }

    /*
     * Checks if the local device has a bond with a remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return <code>true</code> if the two devices were paired,
     *         <code>false</code> otherwise
     */
    public boolean isPaired(String address) {
        return true;
    }

    /*
     * Checks if a remote device was authenticated.
     *
     * @param address Bluetooth address of a remote device
     * @return <code>true</code> if the device was authenticated,
     *         <code>false</code> otherwise
     */
    public boolean isAuthenticated(String address) {
        return true;
    }

    /*
     * Checks if a remote device is trusted (authorized for all services).
     *
     * @param address Bluetooth address of a remote device
     * @return <code>true</code> if the device is trusted,
     *         <code>false</code> otherwise
     */
    public boolean isTrusted(String address) {
        return true;
    }

    /*
     * Checks if connections to a remote are encrypted.
     *
     * @param address Bluetooth address of a remote device
     * @return <code>true</code> if connections to the device are encrypted,
     *         <code>false</code> otherwise
     */
    public boolean isEncrypted(String address) {
        return true;
    }

    /*
     * Retrieves PIN code to use for pairing with a remote device. If the
     * PIN code is not known, PIN entry dialog is displayed.
     *
     * @param address the Bluetooth address of the remote device
     * @return string containing the PIN code
     */
    public String getPasskey(String address) {
        return passKey;
    }

    /*
     * Initiates pairing with a remote device.
     *
     * @param address the Bluetooth address of the device with which to pair
     * @param pin an array containing the PIN code
     * @return <code>true</code> if the device was authenticated,
     *         <code>false</code> otherwise
     */
    public boolean bond(String address, String pin) {
        return true;
    }

    /*
     * Authenticates remote device.
     *
     * @param address Bluetooth address of a remote device
     * @return <code>true</code> if the device was authenticated,
     *         <code>false</code> otherwise
     */
    public boolean authenticate(String address) {
        PicoNode pico = (PicoNode)piconet.get(address);
        if (pico == null) {
            return false;
        }
        pico.authenticated = true;
        return true;
    }

    /*
     * Authorizes a Bluetooth connection.
     *
     * @param address the Bluetooth address of the remote device
     * @param handle handle for the service record of the srvice the remote
     *         device is trying to access
     * @return <code>true</code> if authorization succeeded,
     *         <code>false</code> otherwise
     */
    public boolean authorize(String address, int handle) {
        return true;
    }

    // JAVADOC COMMENT ELIDED
    public boolean encrypt(String address, boolean enable) {
        return false;
    }

    /*
     * Returns list of preknown devices in a Vector.
     *
     * @return Vector object containing preknown devices
     */
    public Vector getPreknownDevices() {
        return null;
    }

    /*
     * Returns the number of connections to the remote device.
     *
     * @param address the Bluetooth address of the remote device
     * @return number of connections established with the remote device
     */
    public int getConnectionCount(String address) {
        PicoNode pico = (PicoNode)piconet.get(address);
        return pico != null ? pico.connections : 0;
    }

    /*
     * Registers a new connection to a remote device.
     *
     * @param address the Bluetooth address of the remote device
     */
    public void addConnection(String address) {
        PicoNode pico = (PicoNode)piconet.get(address);
        if (pico == null) {
            piconet.put(address, pico = new PicoNode());
        }
        pico.connections++;
    }

    /*
     * Unregisters an existing connection to a remote device.
     *
     * @param address the Bluetooth address of the remote device
     */
    public void removeConnection(String address) {
        PicoNode pico = (PicoNode)piconet.get(address);
        if (pico.connections == 0) {
            throw new RuntimeException("No open connections for " + address);
        }
        if (--pico.connections == 0) {
            piconet.remove(address);
        }
    }

    /*
     * Extracts initial configuration values from properties.
     */
    private void loadPropertyValues() {
        // extract the bluetooth device power state
        isBluetoothEnabled = getInternalBooleanProperty("bluetooth.enable",
            isBluetoothEnabled);

        // extract the device friendly name.
        friendlyName = getInternalStringProperty(
            "bluetooth.device.friendlyName", friendlyName);

        // extract the device class.
        deviceClassBase = getInternalIntProperty("bluetooth.device.class", 16,
            deviceClassBase);

        // extract the discoverable mode.
        accessCode = getInternalIntProperty("bluetooth.device.accessCode", 16,
            accessCode);
    }

    /*
     * Gets the internal property indicated by the specified key
     * as an <code>String</code>
     * or returns the specified default value if the property is not found.
     *
     * @param      key   the name of the internal property.
     * @param      def   the default value for the property if there
     *                   is no property with the key.
     * @return     the String value of the internal property
     *             or <code>def</code> if there is no property
     *             with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    private static String getInternalStringProperty(String key, String def) {
        // Implicitly throw NPE if key is null
        if (key.length() ==  0) {
            throw new IllegalArgumentException("Key cannot be empty");
        }

        String prop = Configuration.getProperty(key);
        return (prop != null) ? prop : def;
    }

    /*
     * Gets the internal property indicated by the specified key
     * as an <code>boolean</code>
     * or returns the specified default value if property reading failed.
     *
     * @param      key   the name of the internal property.
     * @param      def   the default value for the property if there
     *                   is no property with the key.
     * @return     the boolean value of the internal property
     *             or <code>def</code> if there is no property
     *             with that key or the value is not valid.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    private static boolean getInternalBooleanProperty(String key, boolean def) {
        // Implicitly throw NPE if key is null
        if (key.length() ==  0) {
            throw new IllegalArgumentException("Key cannot be empty");
        }

        boolean val = def;
        String prop = Configuration.getProperty(key);
        if (prop != null) {
            if (prop.equalsIgnoreCase("true") ||
                prop.equalsIgnoreCase("yes")) {
                val = true;
            } else if (prop.equalsIgnoreCase("false") ||
                       prop.equalsIgnoreCase("no")) {
                val = false;
            }
        }
        return val;
    }

    /*
     * Gets the internal property indicated by the specified key
     * as an <code>int</code> in the specified radix
     * or returns the specified default value if property reading failed.
     *
     * @param      key   the name of the internal property.
     * @param      radix  the radix to be used.
     * @param      def   the default value for the property if there
     *                   is no property with the key.
     * @return     the integer value of the internal property
     *             or <code>def</code> if there is no property
     *             with that key or the value is not valid.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    private static int getInternalIntProperty(String key, int radix, int def) {
        // Implicitly throws NPE if key is null
        if (key.length() ==  0) {
            throw new IllegalArgumentException("Key cannot be empty");
        }

        try {
            String prop = Configuration.getProperty(key);
            return Integer.parseInt(prop, radix);
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

}
