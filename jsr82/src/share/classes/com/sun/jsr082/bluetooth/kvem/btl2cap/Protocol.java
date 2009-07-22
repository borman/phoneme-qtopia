/*
 *
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.jsr082.bluetooth.kvem.btl2cap;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import java.io.IOException;
import javax.microedition.io.Connection;
import com.sun.j2me.security.BluetoothPermission;

/*
 * Network monitor version of BTL2CAP protocol
 */

public final class Protocol extends com.sun.jsr082.bluetooth.btl2cap.Protocol {

    /*
     * Ensures that permissions are proper and creates client side connection.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>BTSPPConnectionImpl</code> instance
     * @exception IOException if openning connection fails.
     */
    //protected Connection clientConnection(SecurityToken token, int mode)
    protected Connection clientConnection( int mode)
            throws IOException {
        //checkForPermission(token, Permissions.BLUETOOTH_CLIENT);
	checkForPermission( BluetoothPermission.BLUETOOTH_CLIENT);
        return new L2CAPNetmonConnection(url, mode);
    }

    /*
     * Ensures that permissions are proper and creates required notifier at
     * server side.
     * @param token security token if passed by caller, or <code>null</code>
     * @param mode       I/O access mode
     * @return proper <code>BTSPPNotifierImpl</code> instance
     * @exception IOException if openning connection fails
     */

   // protected Connection serverConnection(SecurityToken token, int mode)
     protected Connection serverConnection( int mode)
            throws IOException {
        //checkForPermission(token, Permissions.BLUETOOTH_SERVER);
	 checkForPermission(BluetoothPermission.BLUETOOTH_SERVER);
        return new L2CAPNetmonNotifier(url, mode);
    }

}
