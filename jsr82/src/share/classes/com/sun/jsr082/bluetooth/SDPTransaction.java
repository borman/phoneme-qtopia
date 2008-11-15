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

import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

class SDPTransaction {
    private int id;
    private int [] attributeSet;
    private UUID [] uuids;
    private RemoteDevice remoteDevice;
    private DiscoveryListener listener;
    protected final static int SERVICE_RECORD_BUFFER_SIZE = 1024;

    /* Creates a new instance of SDPTransaction
     *
     * @param id ID of the transaction
     * @param attrSet list of attributes whose values are requested.
     * @param uuidSet list of UUIDs that indicate services relevant to request.
     * @param btDev remote Bluetooth device to listen response from.
     * @param discListener discovery listener.
     */
    SDPTransaction(int[] attrSet, UUID[] uuidSet,
            RemoteDevice btDev, DiscoveryListener discListener) {
        attributeSet = attrSet;
        uuids = uuidSet;
        remoteDevice = btDev;
        listener = discListener;
    }

    /*
     * Returns bluetooth address of the corresponding device
     *
     * @return bluetooth address of the remote device
     */
    String getBluetoothAddress() {
        return remoteDevice.getBluetoothAddress();
    }

    /*
     * Returns pattern UUIDs as an array of strings
     *
     * @return array of uuids byte arrays
     */
    byte [][] getUUIDs() {
        byte [][] byteUUIDs = new byte [uuids.length][];
        for (int i = 0; i < uuids.length; i++) {
            String uuid = uuids[i].toString();
            /* Every byte is coded with a pair of characters */
            byteUUIDs[i] = new byte [(uuid.length()) / 2];
            char [] chars = new char [uuid.length()];
            uuid.getChars(0, uuid.length(), chars, 0);
            for (int j = 0; j < byteUUIDs[i].length; j++) {
                String str = new String(chars, j * 2, 2);
                byteUUIDs[i][j] = (byte)Long.parseLong(str, 16);
            }
        }
        return byteUUIDs;
    }

    /*
     * Notifys listener about transaction result
     *
     * @param response response code
     */
    void serviceSearchCompleted(int response) {
        listener.serviceSearchCompleted(id, response);
    }

    /*
     * Notifys listener about transaction result
     *
     * @param recHandle handle of the service record
     */
    void serviceDiscovered(int recHandle) {
        ServiceRecordImpl servRecord[] = new ServiceRecordImpl[1];
        byte rec_pdu[] = new byte[SERVICE_RECORD_BUFFER_SIZE];
        if (getServiceRecord0(recHandle, rec_pdu)>0)
        {
            servRecord[0] = ServiceRecordSerializer.restore(this.remoteDevice, rec_pdu);
        }
        if (listener != null) { 
        	listener.servicesDiscovered(id, servRecord);
        } else {
        	throw new RuntimeException( "DiscoveryListener is not found" );
        }
    }

    /*
     * Returns attribute set
     *
     * @return attribute set
     */
    int [] getAttributeSet() {
        return attributeSet;
    }

    /*
     * Sets transaction's ID
     */
    void setId(int id) {
        this.id = id;
    }

    private native int getServiceRecord0(int recHandle, byte pdu[]);

}
