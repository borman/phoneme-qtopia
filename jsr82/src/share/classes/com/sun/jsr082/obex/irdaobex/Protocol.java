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
package com.sun.jsr082.obex.irdaobex;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import com.sun.jsr082.obex.ClientSessionImpl;
import com.sun.jsr082.obex.SessionNotifierImpl;
import com.sun.j2me.app.AppPackage;
import com.sun.j2me.io.ConnectionBaseInterface;
import com.sun.j2me.security.OBEXPermission;


/*
 * Provides a wrapper for the irdaobex protocol implementation
 * to answer the GCF style.
 */
public class Protocol implements ConnectionBaseInterface {

    /* This class has a different security domain than the MIDlet suite */
//    private static SecurityToken classSecurityToken = null;

    /* Shows whether cilent permissions checked successfilly. */
    private boolean clientPermitted = false;

    /* Shows whether server permissions checked successfilly. */
    private boolean serverPermitted = false;

    /* Keeps the device properties and attributes. */
    static IrOBEXControl control = null;

    /* Host name used for the server side. */
    private final String serverHost = "localhost";

    /* Host name used for the client side. */
    private final String clientHost = "discover";

    /* Default constructor. */
    public Protocol() {
    }

    /*
     * Returns either ClientSession or SessionNotifier for OBEX connections,
     * depending whether client or server URL was specified.
     *
     * @param name the URL for the connection (without "irdaobex:" prefix)
     * @param mode only READ_WRITE mode is supported by OBEX
     * @param timeouts ignored
     * @return ClientSession for client URL or SessionNotifier for server URL
     * @exception IllegalArgumentException if the URL specified is invalid
     * @exception ConnectionNotFoundException if the target cannot be found
     * @exception IOException if something goes wrong
     * @exception SecurityException if access is prohibited
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {

        // instantiate control class on the first time of method invocation
        synchronized (Protocol.class) {
            if (control == null) {
                control = new IrOBEXControl();
            }
        }

        // save the URL for later use
        String url = "irdaobex:" + name;

        if (!name.startsWith("//")) {
            throw new IllegalArgumentException("Malformed URL: " + url);
        }

        // cut off the "//" prefix
        name = name.substring(2);

        // OBEX supports READ_WRITE mode only
        if (mode != Connector.READ_WRITE) {
            throw new IllegalArgumentException("Unsupported mode: " + mode);
        }

        String ias = "OBEX,OBEX:IrXfer"; // Default IAS

        // ";ias=" indicates the beginning of the IAS list
        int index = name.toLowerCase().indexOf(";ias=");
        if (index != -1) {
            ias = name.substring(index + ";ias=".length());
            // check IAS validity
            if (!checkIAS(ias)) {
                throw new IllegalArgumentException("Invalid IAS: " + ias);
            }
            // cut off IAS from the name
            name = name.substring(0, index);
        }

        Vector iasVector = new Vector();
        ias = ias.concat(",");
        while (ias.length() > 0) {
            index = ias.indexOf(',');
            iasVector.addElement(ias.substring(0, index));
            ias = ias.substring(index + 1);
        }

        String[] iasArray = new String[iasVector.size()];
        iasVector.copyInto(iasArray);
        String host = name.toLowerCase();
        boolean isServer;
        int hints;
        if (host.startsWith(serverHost)) {
            isServer = true;
            name = name.substring(serverHost.length());
            hints = 0x0200;
        } else if (host.startsWith(clientHost)) {
            isServer = false;
            name = name.substring(clientHost.length());
            hints = 0;
        } else {
            throw new IllegalArgumentException("Malformed URL: " + url);
        }

        if (name.length() > 0 && name.charAt(0) == '.') {
            // hint bits should follow
            String hstring = name.substring(1).toUpperCase();
            if (!checkHints(hstring)) {
                throw new IllegalArgumentException(
                    "Invalid hint bits: " + hstring);
            }
            hints |= Integer.parseInt(hstring, 16);
            hints &= 0x7f7f7f7f;
        }

        if (isServer) {
            if (!serverPermitted) {
                checkForPermission(OBEXPermission.OBEX_SERVER, url);
                serverPermitted = true;
            }
            return new SessionNotifierImpl(
                control.createServerConnection(hints, iasArray));

        } else {
            if (!clientPermitted) {
                checkForPermission(OBEXPermission.OBEX_CLIENT, url);
                clientPermitted = true;
            }

            return new ClientSessionImpl(
                control.createClientConnection(hints, iasArray));
        }
    }

    /*
     * Makes sure caller has the com.sun.midp permission set to "allowed".
     * @param permission requested permission
     * @param name resource name to check permissions against
     * @exception IOInterruptedException if another thread interrupts the
     *        calling thread while this method is waiting to preempt the
     *        display.
     */
    private void checkForPermission(OBEXPermission permission, String name)
            throws InterruptedIOException {
        AppPackage app = AppPackage.getInstance();

        if (app != null)
        {
            try {
                app.checkForPermission(new OBEXPermission(
                    permission.getName(), name));
            }
            catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        }
    }

    /*
     * Checks the hint bits. The number of hex digits must be even,
     * two digits minimum, eight digits maximum.
     *
     * @param hints hint bits passed in uppercase
     * @return true if the parameter is valid, false otherwise
     */
    private static boolean checkHints(String hints) {
        if (hints.length() < 2 || hints.length() > 8 ||
                hints.length() % 2 != 0) {
            return false;
        }

        byte[] data = hints.getBytes();
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= '0' && data[i] <= '9' ||
                    data[i] >= 'A' || data[i] <= 'F') {
                continue;
            }
            return false;
        }
        return true;
    }

    /*
     * Checks if the IAS (Information Access Service) string complies
     * with the grammar.
     *
     * @param ias IrDA class names separated by comma
     * @return true if the list is valid, false otherwise
     */
    private static boolean checkIAS(String ias) {
        // should not be empty, should not start or end with a comma
        if (ias.length() == 0 || ias.charAt(0) == ',' ||
            ias.charAt(ias.length() - 1) == ',') {
            return false;
        }

        // add a comma to the end of the list to facilitate iteration
        ias = ias.concat(",");
        while (ias.length() > 0) {
            int index = ias.indexOf(',');
            byte[] data = ias.substring(0, index).getBytes();
            ias = ias.substring(index + 1);
            int hex = 0;
            // parse single IrDA class name
            for (int i = 0; i < data.length; i++) {
                if (hex > 0) {
                    // hex digit is expected
                    if (data[i] >= '0' && data[i] <= '9' ||
                            data[i] >= 'A' && data[i] <= 'F' ||
                            data[i] >= 'a' && data[i] <= 'f') {
                        hex--;
                        continue;
                    }
                    return false;
                }
                if (data[i] == '%') {
                    // excapedOcted should follow (two hex digits)
                    hex = 2;
                    continue;
                }
                if (data[i] >= '0' && data[i] <= '9' || data[i] == ':' ||
                        data[i] >= 'A' && data[i] <= 'Z' ||
                        data[i] >= 'a' && data[i] <= 'z') {
                    continue;
                }
                return false;
            }
            if (hex > 0) {
                return false;
            }
        }
        return true;
    }
}
