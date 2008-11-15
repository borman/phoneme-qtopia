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

package com.sun.j2me.payment;

import java.io.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.security.*;
import javax.microedition.rms.*;
import com.sun.midp.rms.*;
import com.sun.midp.midlet.MIDletSuite;

/**
 * This class extends RMS API to implement blocked operations
 * for Transaction Store
 * The {@link com.sun.midp.rms.RecordStoreImpl} class is used
 * to access Transaction Store
 * The global native mutex is used to manage access to Transaction Store
 *
 * @see RecordStoreImpl
 *
 * @version 1.1
 */
class TransactionStorageImpl {

    /** The name of the RMS where to store the transaction records. */
    static final String PAYMENT_FILE_NAME = "paytrans";

    /** The name of the SuiteId where to store the transaction records. */
    static final int PAYMENT_SUITEID_NAME = MIDletSuite.INTERNAL_SUITE_ID;

    /** Indicates if a Storage is opened. */
    private boolean isOpen = false;

    /** Real Record Store. */
    private RecordStoreImpl store;

    /**
     * Constructor. Opens Transaction Store location and locks it
     * If the Transaction Store is already locked the calling thread is blocking
     * untill Transaction Store is unlocked
     *
     * @param token security token for authorization
     * @param create if true, create the record store if it doesn't exist
     *
     * @exception RecordStoreException if something goes wrong setting up
     *            the new RecordStore.
     * @exception RecordStoreNotFoundException if can't find the record store
     *            and create is set to false.
     * @exception RecordStoreFullException if there is no room in storage
     *            to create a new record store
     *
     * @see com.sun.midp.rms.RecordStoreImpl#openRecordStore
     */
    TransactionStorageImpl(SecurityToken token, boolean create)
        throws RecordStoreException, RecordStoreFullException,
               RecordStoreNotFoundException {
        lockStore();
        store = RecordStoreImpl.openRecordStore(token,
                PAYMENT_SUITEID_NAME, PAYMENT_FILE_NAME, create);
    }

    /**
     * Delete record from the Transaction Store.
     *
     * @param recordId the ID of the record to delete
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @see com.sun.midp.rms.RecordStoreImpl#deleteRecord
     */
    void deleteRecord(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
            RecordStoreException {
	checkOpen();
        store.deleteRecord(recordId);
    }

    /**
     * Updates content of the record in the Transaction Store
     *
     * @param recordId the ID of the record to use in this operation
     * @param newData the new data to store in the record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception RecordStoreFullException if the operation cannot be
     *          completed because the record store has no more room
     *
     * @see com.sun.midp.rms.RecordStoreImpl#setRecord
     */
    void setRecord(int recordId, byte[] newData)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
            RecordStoreException, RecordStoreFullException {

	checkOpen();
        store.setRecord(recordId, newData, 0, newData.length);
    }

    /**
     * Returns all of the recordId's currently in the Transaction Store.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @return an array of the recordId's currently in the record store
     *         or null if the record store is closed.
     *
     * @see com.sun.midp.rms.RecordStoreImpl#getRecordIDs
     */
    int[] getRecordIDs()
        throws RecordStoreNotOpenException {
	checkOpen();
        return store.getRecordIDs();
    }

    /**
     * Returns a copy of the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     *
     * @return the data stored in the given record. Note that if the
     *          record has no data, this method will return null.
     *
     * @see com.sun.midp.rms.RecordStoreImpl#getRecord
     */
    public byte[] getRecord(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException {
	checkOpen();
        return store.getRecord(recordId);
    }

    /**
     * Close Transaction Store and unlocks it.
     * All threads waiting for Transaction Store will be unblocked
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *          store-related exception occurred
     *
     * @see com.sun.midp.rms.RecordStoreImpl#closeRecordStore
     */
    public void closeStore()
        throws RecordStoreNotOpenException, RecordStoreException {
        checkOpen();
        try {
            store.closeRecordStore();
        } finally {
            unlockStore();
        }
    };

    /**
     * Adds a new record into the Transaction Store
     *
     * @param data the data to be stored in this record.
     *
     * @return the recordId for the new record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *          store-related exception occurred
     * @exception RecordStoreFullException if the operation cannot be
     *          completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     *          to the RecordStore
     *
     * @see com.sun.midp.rms.RecordStoreImpl#addRecord
     */
    int addRecord(byte[] data)
        throws RecordStoreNotOpenException, RecordStoreException,
            RecordStoreFullException {
	checkOpen();
        return store.addRecord(data, 0, data.length);
    }

    /**
     * Returns the size (in bytes) of the record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @return the size (in bytes) of the MIDlet data available
     *          in the given record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     *
     * @see com.sun.midp.rms.RecordStoreImpl#getRecordSize
     */
    public int getRecordSize(int recordId)
        throws RecordStoreNotOpenException, InvalidRecordIDException,
               RecordStoreException {
	checkOpen();
        return store.getRecordSize(recordId);
    }

    /**
     * Returns the number of records currently in the Transaction Store.
     *
     * @return the number of records currently in the record store
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     *
     * @see com.sun.midp.rms.RecordStoreImpl#getNumRecords
     */
    int getNumRecords() throws RecordStoreNotOpenException {
	checkOpen();
        return store.getNumRecords();
    }

    /**
     * Throws a RecordStoreNotOpenException if the RecordStore
     * is closed.
     *
     * @exception RecordStoreNotOpenException if RecordStore is closed
     *
     * @see com.sun.midp.rms.RecordStoreImpl#checkOpen
     */
    private void checkOpen() throws RecordStoreNotOpenException {
        if (!isOpen) {
            throw new RecordStoreNotOpenException();
        }
    }

    /**
     * Deletes Transaction Store
     *
     * @param token security token for authorization
     *
     * @exception RecordStoreException if a record store-related
     *          exception occurred
     * @exception RecordStoreNotFoundException if the record store
     *          could not be found
     *
     * @see com.sun.midp.rms.RecordStoreImpl#deleteRecordStore
     */
    static void deleteStore(SecurityToken token)
        throws RecordStoreException, RecordStoreNotFoundException {
        RecordStoreImpl.deleteRecordStore(token,
                PAYMENT_SUITEID_NAME, PAYMENT_FILE_NAME);
    }

    /**
     * Waits for access to Transaction Store
     *
     */
    private native void lockStore();

    /**
     * Unlocks Transaction Store
     *
     */
    private native void unlockStore();

    /**
     * Finalizes the store.
     *
     */
    private native void finalize();

}
