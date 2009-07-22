/*
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
package com.sun.jsr082.bluetooth;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;

import java.io.IOException;

public class NativeSDPClient implements ServiceDiscoverer, SDPClient {

    private Hashtable transactions;
    String  btAddress;
    RemoteDevice remoteDevice;
    UUID[] uuidSet = null;
    /*
     * Creates a new instance of NativeSDPClient
     */
    public NativeSDPClient() {
        transactions = new Hashtable();
    }
    /*
     * Returns an <code>JavaSDPClient<code> object and opens SDP connection
     * to the remote device with the specified Bluetooth address.
     *
     * @param bluetoothAddress bluetooth address of SDP server
     */
    public SDPClient getSDPClient(String bluetoothAddress) {
        btAddress = bluetoothAddress;
        remoteDevice = (RemoteDevice)new RemoteDeviceImpl(btAddress);
        return (SDPClient)this;
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

        remoteDevice = btDev;
        btAddress = btDev.getBluetoothAddress();
        this.uuidSet = uuidSet;
        
        if (discListener == null) {
            throw new NullPointerException("DiscoveryListener is null");
        }

        SDPTransaction transaction = new SDPTransaction(attrSet, uuidSet,
                                                        btDev, discListener, 
                                                        this);

        int nativeHandle = serviceSearchRequest0(
                                  btAddress,
                                  transaction.getUUIDs(),
                                  transaction.getAttributeSet());
        if (nativeHandle != 0) {
            transaction.setId(nativeHandle);
            synchronized (transactions) {
                transactions.put(new Integer(nativeHandle), transaction);
            }
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
        SDPTransaction transaction;
        synchronized (transactions) {
            transaction = (SDPTransaction) transactions.
            get(new Integer(transID));
        }
        if ((transaction == null) ||
            (transaction.canceled)){
            return false;
        }
        if (cancelSearchRequest0(transID)) {
            transaction.canceled = true;
            return true;
        } else {
            return false;
        }
    }
    /*
     * Initiates ServiceAttribute transaction that retrieves
     * specified attribute values from a specific service record.
     */
    public void serviceAttributeRequest(int serviceRecordHandle, int[] attrSet,
        int transactionID, SDPResponseListener listener) throws IOException {
        
        SADiscoveryListener sad = new SADiscoveryListener(serviceRecordHandle,
                                                          attrSet,
                                                          transactionID,
                                                          listener);
        sad.start();
    }
    
    /*
     * Initiates ServiceSearchAttribute transaction that searches for services
     * on a server by UUIDs specified and retrieves values of specified
     * parameters for service records found.
     */
    public void serviceSearchAttributeRequest(int[] attrSet, UUID[] uuidSet,
        int transactionID, SDPResponseListener listener) throws IOException{

        Boolean synch = new Boolean(true);
        SSDiscoveryListener ssd = new SSDiscoveryListener(synch);

        synchronized (synch)  {
            searchService(attrSet, uuidSet, remoteDevice, ssd);
            try {
                synch.wait();
            } catch (Throwable e) {
            }
        }
        
        ServiceRecord record = ssd.getServiceRecord();
        int[] attrIDs = record.getAttributeIDs();
        DataElement[] attrValues = new DataElement[attrIDs.length];
        for (int i = 0; i < attrIDs.length; i++) {
            attrValues[i] = record.getAttributeValue(attrIDs[i]);
        }
        listener.serviceSearchAttributeResponse(attrIDs, attrValues, transactionID);
    }

    /*
     * Closes connection of this client to the specified server.
     *
     * @throws IOException if no connection is open
     */
    public void close() throws IOException {
        Enumeration trs = null;
        synchronized (transactions) {
    		trs = (Enumeration) transactions.keys();
        }
		while (trs.hasMoreElements()) {
			Object key = trs.nextElement();
            int trId = ((Integer)key).intValue();
            searchCompleted(trId, DiscoveryListener.SERVICE_SEARCH_TERMINATED);
        }   
    }

    /*
     * Reports service discovering completed
     *
     * @param transID ID of a transaction completed
     * @param result result of searching
     */
    public void searchCompleted(int transID, int result) {
        SDPTransaction transaction = null;
        synchronized (transactions) {
            transaction = (SDPTransaction)transactions.
                                          remove(new Integer(transID));
        }
        if (transaction != null) {
            if (transaction.canceled) {
                transaction.serviceSearchCompleted(
                            DiscoveryListener.SERVICE_SEARCH_TERMINATED);
            } else {
                transaction.serviceSearchCompleted(result);
            }
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
        if ((transaction != null) && !(transaction.canceled)){
            transaction.serviceDiscovered(recHandle);
        }
    }

    private native int serviceSearchRequest0(String btAddress,
                                             byte [][] UUIDs,
                                             int [] attrs);

    private native boolean cancelSearchRequest0(int transactionID);

    /*
     * Service Discovery responce listener that is used within 
     * <code>serviceAttributeRequest()</code>
     * processing.
     */
    private class SADiscoveryListener implements DiscoveryListener, Runnable {
        
        private ServiceRecord sRecord = null;
        boolean done;
        int srHandle;
        int[] attrSet; 
        int transactionID; 
        SDPResponseListener listener;

        public SADiscoveryListener(int handle,
            int[] attrSet, int transactionID, SDPResponseListener listener) {
            done = false;
            srHandle = handle;
            this.attrSet = attrSet;
            this.transactionID = transactionID;
            this.listener = listener;
        }
        public ServiceRecord getServiceRecord() {
            return sRecord;
        }
        
        /*
         * Starts this transaction.
         *
         * @throws IOException when an I/O error occurs
         */
        public void run() {
            if (uuidSet == null) {
                uuidSet = new UUID[0];
            }
            synchronized (this)  {
                try {
                    searchService(attrSet, uuidSet, remoteDevice, this);
                    wait();
                } catch (BluetoothStateException bse) {
                    listener.errorResponse(listener.IO_ERROR, bse.getMessage(), 
                                           transactionID);
                } catch (Throwable e) {
                }
            }
       
            ServiceRecord record = getServiceRecord();
            if (record == null) {
                listener.errorResponse(listener.SDP_INVALID_SR_HANDLE, btAddress, 
                                       transactionID);
            } else {
                int numAttr = 0;
                Vector vAttrIDs = new Vector();
                Vector vAttrValues = new Vector();
                for (int i = 0; i < attrSet.length; i++) {
                    DataElement attrValue = record.getAttributeValue(attrSet[i]);
                    if (attrValue != null) {
                        vAttrIDs.addElement(new Integer(attrSet[i]));
                        vAttrValues.addElement(attrValue);
                        numAttr++;
                    }
                }
                numAttr = vAttrIDs.size();
                DataElement[] attrValues = new DataElement[numAttr];
                int[] attrIDs = new int[numAttr];
                for (int i=0; i < numAttr; i++){
                    attrIDs[i] = ((Integer)vAttrIDs.elementAt(i)).intValue();
                    attrValues[i] = (DataElement)vAttrValues.elementAt(i);
                }
                listener.serviceAttributeResponse(attrIDs, (numAttr > 0 ? attrValues : null), 
                                                    transactionID);
            }
        }
    
        public void start() {
            (new Thread(this)).start();
        }
        
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            throw new RuntimeException("unexpected call");
        };

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            if (done) {
                return;
            }
            int recIndex = -1;
            for (int i = 0; i < servRecord.length; i++) {
                if (servRecord[i].
                    getAttributeValue(ServiceRecordImpl.SERVICE_RECORD_HANDLE).
                    getLong() == srHandle) {
                    recIndex = i;
                    break;
                }
            }
            if (recIndex == -1) {
                return;
            }
            synchronized (this) {
                try {
                    sRecord = servRecord[recIndex];
                    done = true;
                    notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        public void serviceSearchCompleted(int transID, int respCode) {
            synchronized (this) {
                try {
                    done = true;
                    notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        public void inquiryCompleted(int discType) {
            throw new RuntimeException("unexpected call");
        };
        
    }

    /*
     * Service Discovery responce listener that is used within 
     * <code>serviceSearchAttributeRequest()</code>
     * processing.
     */
    private class SSDiscoveryListener implements DiscoveryListener {
        
        private Boolean finished;
        private ServiceRecord sRecord;
        boolean done;

        public SSDiscoveryListener(Boolean syn) {
            finished = syn;
            done = false;
        }
        public ServiceRecord getServiceRecord() {
            return sRecord;
        }
        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            throw new RuntimeException("unexpected call");
        };

        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            if (done) {
                return;
            }
            synchronized (finished) {
                try {
                    sRecord = servRecord[0];
                    done = true;
                    finished.notifyAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        public void serviceSearchCompleted(int transID, int respCode) {
            return;
        };

        public void inquiryCompleted(int discType) {
            throw new RuntimeException("unexpected call");
        };
        
    }

}
