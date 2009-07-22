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

package com.sun.jsr082.bluetooth.kvem.btspp;

import java.io.IOException;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.util.Enumeration;
// was import com.sun.midp.io.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import javax.bluetooth.*;
import javax.bluetooth.ServiceRecord;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
//import com.sun.kvem.jsr082.bluetooth.ServiceRecordImpl;
import com.sun.jsr082.bluetooth.ServiceRecordImpl;

/*
 * Network monitoring implementation of BTSPP notifier.
 * Redefined to provide the URL for connection.
 */

//was class BTSPPNetmonNotifier extends com.sun.midp.io.j2me.btspp.BTSPPNotifierImpl{
class BTSPPNetmonNotifier extends com.sun.jsr082.bluetooth.btspp.BTSPPNotifierImpl{

    private BluetoothUrl url;

    /*
     * Obex Netmon connection id.
     */
    private int id = -1;

    BTSPPNetmonNotifier(BluetoothUrl url, int mode) throws IOException,
        ServiceRegistrationException {
        super(url, mode);
        this.url = url;
    }


    protected StreamConnection doAccept() throws IOException {

        if (!isListenMode) {
            throw new BluetoothStateException("Device is not in listen mode");
        }

        /*
         * Note: native handle is set to peerHandleID field directly
         * by accept0 method and retrieved by L2CAPConnectionImpl constructor.
         */
        super.accept0();

        return new BTSPPNetmonConnection(url, mode, this);
    }


    private void netmonUpdate(ServiceRecord rec) {

        synchronized(this){

            if (id == -1) {
                id = notifierConnect0(url.toString(), 0);
            }
        }

        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(o);
            int ids[] = rec.getAttributeIDs();
            for(int i = 0; i < ids.length; i++) {
                int id = ids[i];
                out.writeInt(id);
                write(out, rec.getAttributeValue(id));
            }
            byte[] array = o.toByteArray();
            updateServiceRecord(id, array, 0, array.length); // native
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /* Encode DataElement into out DataOutputStream. */
    private static void write(DataOutputStream out, DataElement el)
            throws IOException {
        int type = el.getDataType();
        out.writeInt(type);
        switch (type) {
            case DataElement.U_INT_1:
            case DataElement.U_INT_2:
            case DataElement.U_INT_4:
            case DataElement.INT_1:
            case DataElement.INT_2:
            case DataElement.INT_4:
            case DataElement.INT_8:
                out.writeLong(el.getLong());
                break;
            case DataElement.BOOL:
                out.writeBoolean(el.getBoolean());
                break;
            case DataElement.U_INT_8:
            case DataElement.U_INT_16:
            case DataElement.INT_16:
                byte[] data = (byte[]) el.getValue();
                out.write(data);
                break;
            case DataElement.NULL:
                break;
            case DataElement.STRING:
            case DataElement.URL:
                out.writeUTF((String) el.getValue());
                break;
            case DataElement.UUID:
                out.writeUTF(((UUID)el.getValue()).toString());
                break;
            case DataElement.DATALT:
            case DataElement.DATSEQ:
                for (Enumeration e = (Enumeration) el.getValue();
                        e.hasMoreElements();) {
                    write(out, (DataElement) e.nextElement());
                }
                // end of list marker
                out.writeInt(-1);
        }
    }


    protected void updateServiceRecord(ServiceRecordImpl srvRecord)
            throws ServiceRegistrationException {

        super.updateServiceRecord(srvRecord);
        netmonUpdate(srvRecord);
    }

    /* Closes the Notifier */
    public void close() throws IOException {
        synchronized(this){
            if (isClosed) {
                return;
            }
            if (id != -1) {
                disconnect0(id);  /*signal the netmon to disconnect*/
            }
        }
        super.close(); /* closes the notifier */
    }


    /* NATIVE SECTION */

    private static native int  notifierConnect0(String url, long groupid);
    private static native void disconnect0(int id);
    private static native void updateServiceRecord(int id,
                                                   byte[] rec,
                                                   int offset,
                                                   int length);

}
