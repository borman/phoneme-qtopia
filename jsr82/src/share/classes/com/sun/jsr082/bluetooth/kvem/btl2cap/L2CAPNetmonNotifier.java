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

import java.io.IOException;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonCommon;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonBluetooth;
import javax.bluetooth.ServiceRegistrationException;
import com.sun.jsr082.bluetooth.ServiceRecordImpl;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.BluetoothStateException;

/*
 * Network monitoring implementation of L2CAP notifier.
 * Redefined to provide the URL for connection.
 */

//was class L2CAPNetmonNotifier extends com.sun.midp.io.j2me.btl2cap.L2CAPNotifierImpl{
class L2CAPNetmonNotifier extends com.sun.jsr082.bluetooth.btl2cap.L2CAPNotifierImpl{

    private BluetoothUrl url;

    /*
     * Obex Netmon connection id.
     */
    private int id = -1;

    L2CAPNetmonNotifier(BluetoothUrl url, int mode) throws IOException,
        ServiceRegistrationException {
        super(url, mode);
        this.url = url;
    }


    protected L2CAPConnection doAccept() throws IOException {

        if (!isListenMode) {
            throw new BluetoothStateException("Device is not in listen mode");
        }

        /*
         * Note: native handle is set to peerHandleID field directly
         * by accept0 method and retrieved by L2CAPConnectionImpl constructor.
         */
        super.accept0();

        return new L2CAPNetmonConnection(url, mode, this);
    }


    /* Closes the Notifier */
    protected void doClose() throws IOException {
        synchronized(this){
            if (isClosed) {
                return;
            }
            if (id != -1) {
                NetmonCommon.notifierDisconnect0(id);  /*signal the netmon to disconnect*/
            }
        }
        super.doClose(); /* closes the notifier */
    }

    /*
     * Stores the service record for this notifier in the local SDDB.
     * If there is no SDDB version of the service record, this method will
     * do nothing.
     *
     * @param record new service record value
     * @throws IllegalArgumentException if new record is invalid
     *
     * @throws ServiceRegistrationException if the record cannot be
     *         updated successfully in the SDDB
     */
    protected void updateServiceRecord(ServiceRecordImpl record)
            throws ServiceRegistrationException {
        super.updateServiceRecord(record);
        synchronized (this) {
            if (id == -1) {
                id = NetmonCommon.notifierConnect0(url.toString(), 0);
            }
            NetmonBluetooth.notifierUpdateServiceRecord(id, record);
        }
        
    }

}
