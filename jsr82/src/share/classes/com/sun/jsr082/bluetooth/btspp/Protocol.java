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
package com.sun.jsr082.bluetooth.btspp;

import java.io.IOException;
import javax.microedition.io.Connection;
import javax.bluetooth.BluetoothConnectionException;
import com.sun.j2me.security.BluetoothPermission;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothProtocol;

/*
 * Provides 'btspp' protocol support.
 */
public class Protocol extends BluetoothProtocol {
    /*
     * Constructs an instance.
     */
    public Protocol() {
        super(BluetoothUrl.RFCOMM);
    }

    /*
     * Ensures URL parameters have valid values.
     * @param url URL to check
     * @exception IllegalArgumentException if invalid url parameters found
     */
    protected void checkUrl(BluetoothUrl url)
            throws IllegalArgumentException, BluetoothConnectionException {
        super.checkUrl(url);

        if (!url.isServer && (url.port < 1 || url.port > 30)) {
            throw new IllegalArgumentException("Invalid channel: " + url.port);
        }
    }

    /*
     * Ensures that permissions are proper and creates client side connection.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>BTSPPConnectionImpl</code> instance
     * @exception IOException if openning connection fails.
     */
    protected Connection clientConnection(int mode)
            throws IOException {
        checkForPermission(BluetoothPermission.BLUETOOTH_CLIENT);
        return new BTSPPConnectionImpl(url, mode);
    }

    /*
     * Ensures that permissions are proper and creates required notifier at
     * server side.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>BTSPPNotifierImpl</code> instance
     * @exception IOException if openning connection fails
     */
    protected Connection serverConnection(int mode)
            throws IOException {
        checkForPermission(BluetoothPermission.BLUETOOTH_SERVER);
        return new BTSPPNotifierImpl(url, mode);
    }
}
