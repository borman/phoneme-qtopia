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

import javax.bluetooth.UUID;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.ServiceRecord;

import com.sun.jsr082.bluetooth.ServiceSearcher;
import com.sun.jsr082.bluetooth.RemoteDeviceImpl;
import com.sun.jsr082.bluetooth.ServiceRecordSerializer;
/*
 *
 * @author an159474
 */
//public class SDPReqEmul extends ServiceSearcher implements DiscoveryListener {
public class SDPReqEmul implements Runnable, DiscoveryListener {
    
    protected static int BT_UUID_16  = 0x19;
    protected static int BT_UUID_32  = 0x1A;
    protected static int BT_UUID_128 = 0x1C;

    String bt_addr;
    
    UUID[] uuids;
    int[] attrs;
    ServiceRecordSerializer srs;
    
    private String bytesToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++)
        {
            int n = bytes[i] & 0xff;
            if (n < 16)
            {
                sb = sb.append("0");
            }
            sb = sb.append(Integer.toHexString(n));
        }
        return  (new String(sb));

    }

    private String bytesToString_rev(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = bytes.length-1; i >= 0 ; i--)
        {
            int n = bytes[i] & 0xff;
            if (n < 16)
            {
                sb = sb.append("0");
            }
            sb = sb.append(Integer.toHexString(n));
        }
        return  (new String(sb));

    }
    
    private int bytesToInt(byte[] bytes) {
        int N = 0;
        int len = bytes.length;
        if (len > 4) len = 4;
        for (int i = 0; i < len; i++)
        {
            N = N + (bytes[i]  << (8*i));
        }
        return  N;

    }

    /* Creates a new instance of SDPReqEmul */
    public SDPReqEmul(BytePack request) {
        srs = new ServiceRecordSerializer();
        bt_addr = bytesToString_rev(request.extractBytes(6));
        int n_uuid = request.extractInt();
        uuids = new UUID[n_uuid];
        for (int i=0; i<n_uuid; i++)
        {
            StringBuffer sb = new StringBuffer();
            byte t_uuid = request.extract();
            if (t_uuid < BT_UUID_128) {
                uuids[i] = new UUID(request.extractInt());
            } else {
                byte[] b = request.extractBytes(16);
                uuids[i] = new UUID(bytesToString(b), false);
            }
        }
        attrs = new int[request.extractInt()];
        for (int i=0; i<attrs.length; i++)
        {
            attrs[i] = bytesToInt(request.extractBytes(2));
        }
    }

    public int searchService_emul(String address, UUID[] uuidSet, int[] attrSet,
            DiscoveryListener discListener) throws BluetoothStateException {
    
        RemoteDeviceImpl btDev = null;
        ServiceSearcher ss = new ServiceSearcher();
        btDev = new RemoteDeviceImpl(address);
        
        int nativeHandle = ss.searchService(
                                            attrSet, 
                                            uuidSet, 
                                            btDev,
                                            this);

        return nativeHandle;
    }
    
    public void run() {
        try {
            searchService_emul(bt_addr, uuids, attrs, this);
        } catch (BluetoothStateException bse) {
            bse.printStackTrace();
        }
    }

    // JAVADOC COMMENT ELIDED 
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        
    }

    // JAVADOC COMMENT ELIDED 
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        byte[] data;
        for (int i = 0; i < servRecord.length; i++){
            data = srs.serialize(servRecord[i]);
            servicesDiscovered0(transID, data);
        }
        setSignal0(transID);
        
    }

    // JAVADOC COMMENT ELIDED 
    public void serviceSearchCompleted(int transID, int respCode) {
        serviceSearchCompleted0(transID, respCode);
        setSignal0(transID);
    }

    // JAVADOC COMMENT ELIDED 
    public void inquiryCompleted(int discType) {
        
    }

    native void setSignal0(int TransID);
    native void servicesDiscovered0(int transID, byte[] data);
    native void serviceSearchCompleted0(int transID, int respCode);

}
