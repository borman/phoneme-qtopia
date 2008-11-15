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

import java.util.Hashtable;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;

public class NativeSDPClient implements ServiceDiscoverer {

    private Hashtable transactions;

    /*
     * Creates a new instance of NativeSDPClient
     */
    public NativeSDPClient() {
        transactions = new Hashtable();
    }

    /*
     * Start searching services under the given conditions
     *
     * @param attrSet list of attributes whose values are requested.
     * @param uuidSet list of UUIDs that indicate services relevant to request.
     * @param btDev remote Bluetooth device to listen response from.
     * @param discListener discovery listener.
     * @throws BluetoothStateException
     */
    public int searchService(int[] attrSet, UUID[] uuidSet, RemoteDevice btDev,
            DiscoveryListener discListener) throws BluetoothStateException {

        if (discListener == null) {
            throw new NullPointerException("DiscoveryListener is null");
        }

        SDPTransaction transaction = new SDPTransaction(attrSet, uuidSet,
                                                           btDev, discListener);

        int nativeHandle = serviceSearchRequest0(
                                  transaction.getBluetoothAddress(),
                                  transaction.getUUIDs(),
                                  transaction.getAttributeSet());
        if (nativeHandle != 0) {
            transaction.setId(nativeHandle);
            transactions.put(new Integer(nativeHandle), transaction);
            BluetoothStack.getEnabledInstance().startPolling();
            return nativeHandle;
        }
        else {
            throw new BluetoothStateException("Unable to start service searching");
        }
    }

    /*
     * Cancels service discovering
     *
     * @param transID ID of a transaction to be canceled
     * @return true if transaction canceled
     */
    public boolean cancel(int transID) {
        return cancelSearchRequest0(transID);
    }

    /*
     * Reports service discovering completed
     *
     * @param transID ID of a transaction completed
     * @param result result of searching
     */
    public void searchCompleted(int transID, int result) {
        SDPTransaction transaction = (SDPTransaction)transactions.
                                                remove(new Integer(transID));
        if (transaction != null) {
            transaction.serviceSearchCompleted(result);
        }
    }

    /*
     * Reports service discovered
     *
     * @param transID ID of a transaction completed
     * @param recHandle handle of the service record
     */
    public void servicesDiscovered(int transID, int recHandle) {
        SDPTransaction transaction = (SDPTransaction) transactions.
            get(new Integer(transID));
        if (transaction != null) {
            transaction.serviceDiscovered(recHandle);
        }
    }

    private native int serviceSearchRequest0(String btAddress,
                                             byte [][] UUIDs,
                                             int [] attrs);

    private native boolean cancelSearchRequest0(int transactionID);

}
