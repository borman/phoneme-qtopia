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
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.Connector;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.main.Configuration;

import javax.microedition.payment.TransactionRecord;

import com.sun.j2me.payment.PaymentInfo;
import com.sun.j2me.payment.ProviderInfo;
import com.sun.j2me.payment.Transaction;
import com.sun.j2me.payment.TransactionStore;

import com.sun.midp.midletsuite.*;
import com.sun.midp.security.*;
import javax.microedition.rms.*;

/**
 * This class implements the transaction store. It uses RMS
 * to store the transaction records.
 *
 */
public final class CldcTransactionStoreImpl implements TransactionStore {

    /**
     * This class has a different security domain than the MIDlet suite.
     * The token is initialized with a value handed to class constructor.
     */
    private static SecurityToken securityToken;

    /** The list of Transaction Records with reserved and missed status */
    private Vector inProcessRecords = new Vector();

    /**
     * The reference to the property containing maximum number of passed
     * transactions
     */
    private static final String TRANSACTIONS_LIMIT_PROPERTY =
        "payment.transactions.limit";
    /** The maximum number of passed transactions */
    private static final int DEFAULT_TRANSACTIONS_LIMIT = 16;

    /** The maximum number of missed transactions per application. */
    static final int MISSED_TRANSACTIONS_LIMIT = 4;
    /** The maximum total number of past transactions. */
    static final int PASSED_TRANSACTIONS_LIMIT;

    /** The RMS record ID of the NextTransactionID counter */
    private static final int NEXT_TRANSACTION_RECORD_ID = 1;
    /** The RMS record ID of the NextApplicationD counter */
    private static final int NEXT_APPLICATION_RECORD_ID = 2;

    /** The number of bytes in the int */
    private static final int SIZE_OF_INT = 4;

    /** lock used to synchronize this Transaction Store */
    Object tsLock;
    
    static {
        // get the passed transactions limit from the system
        int limit = DEFAULT_TRANSACTIONS_LIMIT;
        String limitStr = Configuration.getProperty(TRANSACTIONS_LIMIT_PROPERTY);
        if (limitStr != null) {
            try {
                int value = Integer.parseInt(limitStr);
                if (value >= 0) {
                    limit = value;
                }
            } catch (NumberFormatException e) {
            }
        }

        PASSED_TRANSACTIONS_LIMIT = limit;
    }


    /**
     * Creates a new instance of <code>CldcTransactionStoreImpl</code>. It needs
     * a security token to perform file system manipulations.
     *
     * @param securityToken the security token
     * @throws IOException indicates a storage failure
     */
    public CldcTransactionStoreImpl(SecurityToken securityToken)
        throws IOException {
        this.securityToken = securityToken;
        tsLock = new Object();
        initStore();
    }

    /**
     * Adds the given transaction to the store. It returns a new transaction
     * record for the transaction. This transaction record must have its
     * <code>wasMissed</code> flag cleared.
     *
     * @param transaction the transaction
     * @return the new transaction record
     * @throws IOException indicates a storage failure
     */
    public TransactionRecord addTransaction(
        Transaction transaction) throws IOException {
        int transactionID = transaction.getTransactionID();
        CldcTransactionRecordImpl record = findReservedRecord(transactionID);
        if (record == null) {
            throw new IllegalStateException("The transaction record hasn't " +
                                            "been reserved");
        }

        record.setStatus(CldcTransactionRecordImpl.MISSED);
        record.update(transaction);

        if (!record.isFake()) {
            
            try {
                TransactionStorageImpl store = 
                        new TransactionStorageImpl(securityToken, false);
                try {
                    writeTransactionRecord(record, store, record.getRecordID());
                } finally {
                    store.closeStore();
                }
            } catch (RecordStoreException ex) {
                throw new IOException("Storage Failure: " + ex.getMessage());
            }
        }

        // store the transaction record as missed but return it as not missed
        return record.createDuplicateRecord();
    }

    /**
     * It returns <code>true</code> if the <code>setDelivered</code> method
     * was called for the given transaction ID.
     *
     * @param transactionID the transaction ID
     * @return true if transaction with Transaction ID were delivered to user
     * @throws IOException indicates a storage failure
     */
    public boolean wasDelivered(int transactionID)
        throws IOException {
        CldcTransactionRecordImpl transactionRecord = findMissedRecord(
            transactionID);
        return transactionRecord == null;
    }

    /**
     * This method is called after the application is successfully notified
     * about the transaction with the given transaction ID.
     *
     * @param transactionID the transaction ID
     * @throws IOException indicates a storage failure
     */
    public void setDelivered(int transactionID)
        throws IOException {
        CldcTransactionRecordImpl transactionRecord = null;

        synchronized (tsLock) {
            transactionRecord = findMissedRecord(transactionID);
            if (transactionRecord == null) {
                throw new IllegalArgumentException("Not a stored transaction");
            }

            inProcessRecords.removeElement(transactionRecord);
        }
        
        if (!transactionRecord.isFake()) {
            transactionRecord.setStatus(CldcTransactionRecordImpl.PASSED);

            try {
                TransactionStorageImpl store = 
                        new TransactionStorageImpl(securityToken, false);
                try {
                    if (transactionRecord.getRecordID() > 0) {
                        writeTransactionRecord(transactionRecord, 
                                store, transactionRecord.getRecordID());
                    }
                    // limit the passed transactions count
                    limitPassedRecords(store);
                } finally {
                    store.closeStore();
                }
            } catch (RecordStoreException ex) {
                throw new IOException("Storage Failure " + ex.getMessage());
            }
        }
    }


    /**
     * Returns an identification number, which can be used as
     * <code>applicationID</code> in the other methods. During installation
     * each payment supporting <code>MIDletSuite</code> should get such number
     * and have it stored. From that point this number will identify that
     * <code>MIDletSuite</code> to the transaction store.
     *
     * @return the payment application id
     * @throws IOException indicates a storage failure
     */
    public int getNextApplicationID() throws IOException {
        int nextApplicationID = 0;
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                nextApplicationID = getIntFromByteArray(
                    store.getRecord(NEXT_APPLICATION_RECORD_ID));
                nextApplicationID++;
                if (nextApplicationID <= 0) {
                    nextApplicationID = 1;
                }
                store.setRecord(NEXT_APPLICATION_RECORD_ID,
                        getByteArrayFromInt(nextApplicationID));
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure");
        }
        return nextApplicationID;
    }

    /**
     * Returns an array of the missed transaction records for the given
     * application ID. The transaction records are returned in the order in
     * which they have been added to the store. Each transaction record must
     * have its <code>wasMissed</code> flag set.
     *
     * @param applicationID the application ID
     * @return the array of the missed transaction records
     * @throws IOException indicates a storage failure
     */
    public TransactionRecord[] getMissedTransactions(
        int applicationID) throws IOException {
        return getMissedTransactions(applicationID, false);
    }

    /**
     * Returns a Vector of passed or missed transaction records for the given
     * application ID. 
     *
     * @param applicationID the application ID
     * @param wasMissed true if missed transactions are required
     *
     * @return the Vector of transaction records
     * @throws IOException indicates a storage failure
     */
    private Vector getTransactions(int applicationID, boolean wasMissed)
        throws IOException {
        Vector records = new Vector();

        /* Get none-fake elements */
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                /* read passed transactions */
                int[] recordIDs = store.getRecordIDs();
                CldcTransactionRecordImpl r;
                for (int i = 0; i < recordIDs.length; i++) {
                    int recId = recordIDs[i];
                    r = readTransactionRecord(store, recId);
                    if (null != r &&
                        (r.getApplicationID() == applicationID) &&
                        (r.wasMissed() == wasMissed)) {
                        r.add2list(records);
                    }
                }
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }

        return records;
    }
    /**
     * Returns an array of the past transaction records for the given
     * application ID. The transaction record are returned in the reverse order
     * as they have been added to the store (most recent first). Each
     * transaction record must have its <code>wasMissed</code> flag cleared.
     *
     * @param applicationID the application ID
     * @return the array of the passed transaction records
     * @throws IOException indicates a storage failure
     */
    public TransactionRecord[] getPassedTransactions(
        int applicationID) throws IOException {

        Vector passedRecords = getTransactions(applicationID, false);

        int count = passedRecords.size();

        if (count == 0) {
            return null;
        }

        CldcTransactionRecordImpl[] appRecords =
            new CldcTransactionRecordImpl[count];
        /* sort passedRecords and store in appRecords (reverse mode) */
        for (int i = 0, j = (count - 1); i < count; i++, j--) {
            appRecords[j] = (CldcTransactionRecordImpl)
                            passedRecords.elementAt(i);
        }
        passedRecords.removeAllElements();

        return appRecords;
    }

    /**
     * Reserves space for the given transaction in the store. It should be
     * called before any call to the <code>addTransaction</code> method to
     * ensure that the <code>addTransaction</code> method won't fail later
     * (when it is inappropriate) due to full store. This method can apply some
     * store policies, like enforcing a maximum number of missed transactions
     * per <code>MIDletSuite</code>.
     * <p>
     * The <code>applicationID</code> identifies the application (MIDlet suite)
     * to the transaction store. MIDlet suites which are run directly can use
     * negative application IDs to avoid permanent storing of created
     * transaction records.
     *
     * @param applicationID the application id
     * @param transaction the transaction
     * @return an unique ID created for the transaction
     * @throws IOException indicates that the store is full or won't accept any
     *      further transaction records from that application
     */
    public int reserve(int applicationID, Transaction transaction)
        throws  IOException {

        /* assign transactionID */
        int transactionID;
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {

                int count = 0;
                /* count missed and reserved transactions */
                CldcTransactionRecordImpl r;
                for (int i = 0; i < inProcessRecords.size(); i++) {
                    r = (CldcTransactionRecordImpl)
                        inProcessRecords.elementAt(i);
                    if (r.wasMissed() &&
                        (r.getApplicationID() == applicationID)) {
                        count++;
                    }
                }

                /* check the limit */
                if (count >= MISSED_TRANSACTIONS_LIMIT) {
                    throw new IOException("No more space for records");
                }

                String applicationName = transaction.
                              getTransactionModule().getMIDlet().
                              getAppProperty(MIDletSuiteImpl.SUITE_NAME_PROP);

                /* assign transactionID */
                int nextTransactionID = getIntFromByteArray(
                    store.getRecord(NEXT_TRANSACTION_RECORD_ID));
                transactionID = nextTransactionID++;
                if (nextTransactionID <= 0) {
                    nextTransactionID = 1;
                }
                transaction.setTransactionID(transactionID);

                /* don't reserve space if applicationID < 0 */
                if (applicationID > 0) {
                    /* create reserved transaction */
                    CldcTransactionRecordImpl newTransactionRecord =
                        new CldcTransactionRecordImpl(
                            applicationID, applicationName,
                            transaction, System.currentTimeMillis(),
                            false);

                    store.setRecord(NEXT_TRANSACTION_RECORD_ID,
                                    getByteArrayFromInt(nextTransactionID));

                    writeTransactionRecord(newTransactionRecord, store, 0);

                    inProcessRecords.addElement(newTransactionRecord);
                } else {
                    /* create reserved fake transaction */
                    CldcTransactionRecordImpl newTransactionRecord =
                        new CldcTransactionRecordImpl(
                            applicationID, applicationName,
                            transaction, System.currentTimeMillis(),
                            true);
                    inProcessRecords.addElement(newTransactionRecord);
                }
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " +
                                  ex.getMessage());
        }
        return transactionID;
    }

    /**
     * Returns the size which is used in the store by the application of the
     * given application ID. This size doesn't include the size of the passed
     * transactions (it includes only the part of the store which is
     * removed/uninstalled by the <code>removeApplicationRecords</code> method).
     *
     * @param applicationID the application ID
     * @return the size used by the application
     * @throws IOException indicates a storage failure
     */
    public int getSizeUsedByApplication(int applicationID)
        throws  IOException {

        int size = 0;
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                /* read missed transactions */
                int[] recordIDs = store.getRecordIDs();
                CldcTransactionRecordImpl r;
                for (int i = 0; i < recordIDs.length; i++) {
                    int recId = recordIDs[i];
                    r = readTransactionRecord(store, recId);
                    if (null != r &&
                        (r.getApplicationID() == applicationID) &&
                        r.wasMissed()) {
                        size += store.getRecordSize(recId);
                    }
                }
            } finally {
                store.closeStore();
            }
        }

        catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }

        return size;
    }


    /**
     * Removes the missed records used by the application of the given
     * application ID. This is to be used, when the MIDlet suite is uninstalled.
     *
     * @param applicationID the application ID
     * @throws IOException indicates a storage failure
     */
    public void removeApplicationRecords(int applicationID)
        throws  IOException {
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                /* read missed and passed transactions */
                int[] recordIDs = store.getRecordIDs();
                CldcTransactionRecordImpl r;
                for (int i = 0; i < recordIDs.length; i++) {
                    int recId = recordIDs[i];
                    r = readTransactionRecord(store, recId);
                    if (null != r &&
                        (r.getApplicationID() == applicationID) &&
                        r.wasMissed()) {
                        store.deleteRecord(recId);
                    }
                }
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }

        synchronized (tsLock) {
            /* count missed and reserved transactions */
            CldcTransactionRecordImpl r;
            for (int i = (inProcessRecords.size() - 1); i >= 0; i--) {
                r = (CldcTransactionRecordImpl)
                    inProcessRecords.elementAt(i);
                if (r.wasMissed() &&
                    (r.getApplicationID() == applicationID)) {
                    inProcessRecords.removeElementAt(i);
                }
            }
        }
    }

    /**
     * Removes all transaction records from the store. This is a helper method
     * which is used in test suites to get clean state before test execution.
     *
     * @throws IOException indicates a storage failure
     */
    public void cleanUp() throws IOException {
        try {
            TransactionStorageImpl.deleteStore(securityToken);
        } catch (RecordStoreException ex) {
            // Nothing to do
        }

        initStore();
    }

    /**
     * Returns an array of the missed transaction records for the given
     * application ID. The transaction records are returned in the order in
     * which they have been added to the store. Each transaction record must
     * have its <code>wasMissed</code> flag set.
     *
     * @param applicationID the application ID
     * @param fakeOnly if true returns only fake transactions for given
     * applicationID
     * @return the array of the missed transaction records
     * @throws IOException indicates a storage failure
     */
    TransactionRecord[] getMissedTransactions(
        int applicationID, boolean fakeOnly) throws IOException {
        Vector missedRecords = new Vector();
        if (!fakeOnly) {
            missedRecords = getTransactions(applicationID, true);
        }

        synchronized (tsLock) {
            /* Get FakeOnly elements */
            for (int i = 0; i < inProcessRecords.size(); ++i) {
                CldcTransactionRecordImpl record = 
                    (CldcTransactionRecordImpl)inProcessRecords.elementAt(i);
                if ((record.getApplicationID() == applicationID) &&
                    record.wasMissed() && record.isFake()) {
                    record.add2list(missedRecords);
                }
            }
        }
        
        int count = missedRecords.size();

        if (count == 0) {
            return null;
        }

        CldcTransactionRecordImpl[] appRecords =
            new CldcTransactionRecordImpl[count];
        
        missedRecords.copyInto(appRecords);

        return appRecords;
    }

// === DEBUG MODE ===
    /**
     * Generates Fake records for Debug mode
     *
     * @param applicationID the application ID
     * @param applicationName the application Name
     * @param paymentInfo properties of the payment module
     * @param featurePrefix prefix of the feature
     * @param validProviders array of Provider IDs
     * @param count number of fake transactions to add
     * @throws IOException indicates a storage failure
     */
    void generateFakeRecords(int applicationID,
                     String applicationName, PaymentInfo paymentInfo,
                     String featurePrefix, int[] validProviders, int count)
        throws IOException {
        // count > 0

        // reserve transaction IDs
        int transactionID;

        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                /* assign transactionID */
                int nextTransactionID = getIntFromByteArray(
                    store.getRecord(NEXT_TRANSACTION_RECORD_ID));
                transactionID = nextTransactionID;
                nextTransactionID += count;
                if (nextTransactionID <= 0) {
                    nextTransactionID = count;
                }

                store.setRecord(NEXT_TRANSACTION_RECORD_ID,
                      getByteArrayFromInt(nextTransactionID));
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }

        Random random = new Random();

        long timestamp = System.currentTimeMillis() - 3600000;
        int deltaTime = 3600000 / count;
        int numFeatures = paymentInfo.getNumFeatures();

        for (int i = 0; i < count; ++i) {
            int state = random.nextInt(3);
            int featureID = (i + (count >>> 1)) * (numFeatures - 1) / count;
            double price;
            String currency;

            if (state != TransactionRecord.TRANSACTION_REJECTED) {
                int priceTag = paymentInfo.getPriceTagForFeature(featureID);
                int providerID = validProviders[
                                 random.nextInt(validProviders.length)];
                ProviderInfo providerInfo = paymentInfo.getProvider(providerID);

                price = providerInfo.getPrice(priceTag);
                currency = providerInfo.getCurrency();
            } else {
                price = 0;
                currency = "";
            }

            inProcessRecords.addElement(
                CldcTransactionRecordImpl.createFakeRecord(
                    transactionID++, applicationID, applicationName,
                    featureID, featurePrefix + featureID, price, currency,
                    state, timestamp));

            if (transactionID <= 0) {
                transactionID = 1;
            }

            timestamp += deltaTime;
        }
    }

// === DEBUG MODE ===

    /**
     * Finds Transaction Record with given Transaction ID and status
     *
     * @param transactionID the transaction ID
     * @param status the status of transaction
     * @return the transaction record if found or null
     * @throws IOException indicates a storage failure
     */
    private CldcTransactionRecordImpl findRecord(int transactionID, int status)
         throws IOException {

        CldcTransactionRecordImpl r = null;
        CldcTransactionRecordImpl record = null;

        synchronized (tsLock) {
            int count = inProcessRecords.size();
            /* Try to find in the vector */
            for (int i = 0; i < count; ++i) {
                r = (CldcTransactionRecordImpl)
                    inProcessRecords.elementAt(i);
                if (r.getTransactionID() == transactionID) {
                    return r;
                }
            }
        }
        /* Get none-fake elements */
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                /* fiind reserved transaction */
                int[] recordIDs = store.getRecordIDs();
                for (int i = 0; i < recordIDs.length; i++) {
                    int recId = recordIDs[i];
                    r = readTransactionRecord(store, recId);
                    if (null != r &&
                        (r.getTransactionID() == transactionID) &&
                        (r.getStatus() == status)) {
                        r.setRecordID(recId);
                        record = r;
                        break;
                    }
                }
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        } catch (IOException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }

        return record;
    }

    /**
     * Finds missed Transaction Record with the given Transaction ID
     *
     * @param transactionID the transaction ID
     * @return the transaction record if found or null
     * @throws IOException indicates a storage failure
     */
    private CldcTransactionRecordImpl findMissedRecord(int transactionID)
        throws IOException {
        return findRecord(transactionID, CldcTransactionRecordImpl.MISSED);
    }

    /**
     * Finds reserved Transaction Record with the given Transaction ID
     *
     * @param transactionID the transaction ID
     * @return the transaction record if found or null
     * @throws IOException indicates a storage failure
     */
    private CldcTransactionRecordImpl findReservedRecord(int transactionID)
        throws IOException {
        return findRecord(transactionID, CldcTransactionRecordImpl.RESERVED);
    }

    /**
     * retrives Int from the ByteArray
     *
     * @param data the array of bytes
     * @return integer value converted from byte array or -1
     */
    static int getIntFromByteArray(byte[] data) {
        if (data.length == SIZE_OF_INT) {
            return (((int) (data[0]) << 24) |
                    ((int) (data[1]) << 16) |
                    ((int) (data[2]) << 8) |
                    (int) (data[3]));
        }
        return -1;
    }

    /**
     * puts integer into the ByteArray
     *
     * @param v the integer value
     * @return byte array containing the integer value
     */
    static byte[] getByteArrayFromInt(int v) {
        byte[] data = new byte[SIZE_OF_INT];

        data[0] = (byte) ((byte) ((v) >> 24) & 0xFF);
        data[1] = (byte) ((byte) ((v) >> 16) & 0xFF);
        data[2] = (byte) ((byte) ((v) >> 8) & 0xFF);
        data[3] = (byte) ((byte) (v) & 0xFF);

        return data;
    }

    /**
     * Initializes the store. It's used when the transaction store file doesn't
     * exists.
     *
     * @throws IOException indicates a storage failure
     */
    private void initStore() throws IOException {
        int nextTransactionID = 1;
        int nextApplicationID = 1;

        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, true);
            try {
		if (store.getNumRecords() == 0) {
                    /* Record ID = NEXT_TRANSACTION_RECORD_ID */
                    store.addRecord(getByteArrayFromInt(nextTransactionID));
                    /* Record ID = NEXT_APPLICATION_RECORD_ID */
                    store.addRecord(getByteArrayFromInt(nextApplicationID));
                }
            } catch (RecordStoreException ex) {
                throw new IOException(
                    "Error initializing Transaction Store: " + ex.getMessage());
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreFullException ex) {
            throw new IOException("Error initializing Transaction Store: " + 
                    ex.getMessage());
        } catch (RecordStoreException ex) {
            /* Nothing to do. The store is creating by another isolate */
        }
    }

    /**
     * Limits the count of the passed transaction records in the given
     * vector to the given value.
     *
     * @param st TransactionStorageImpl reference
     *
     * @throws RecordStoreException indicates a storage failure
     * @throws IOException indicates a storage failure
     */
    private void limitPassedRecords(TransactionStorageImpl st) 
            throws RecordStoreException, IOException {
        Vector passedRecords = new Vector();
        CldcTransactionRecordImpl r;
        /* read passed transactions */
        int[] recordIDs = st.getRecordIDs();
        for (int i = 0; i < recordIDs.length; i++) {
            int recId = recordIDs[i];
            r = readTransactionRecord(st, recId);
            if (null != r &&
                !r.wasMissed()) {
                r.add2list(passedRecords);
            }
        }

        while (passedRecords.size() > PASSED_TRANSACTIONS_LIMIT) {
            r = (CldcTransactionRecordImpl) passedRecords.elementAt(0);
            st.deleteRecord(r.getRecordID());
            passedRecords.removeElementAt(0);
        }
    }


    /**
     * Remove missed transaction for give application.
     *
     * @param appID application payment id
     * @throws IOException if there is an error accessing storage
     */
    protected void removeMissedTransaction(int appID) throws IOException {
        CldcTransactionRecordImpl[] recs =
            (CldcTransactionRecordImpl[]) getMissedTransactions(appID);
        if (null == recs) {
            return;
        }
        try {
            TransactionStorageImpl store = 
                    new TransactionStorageImpl(securityToken, false);
            try {
                for (int i = 0; i < recs.length; i++) {
                    store.deleteRecord(recs[i].getRecordID());
                }
            } finally {
                store.closeStore();
            }
        } catch (RecordStoreException ex) {
            throw new IOException("Storage Failure: " + ex.getMessage());
        }
    }

    /**
     * This service function reads and returns TransactionRecord from store.
     *
     * @param st TransactionStorageImpl reference
     * @param recId ID of the record to be read
     * @return CldcTransactionRecordImpl if recId points to proper record,
     *  otherwise null
     * @throws IOException  if an I/O error occurs
     * @throws RecordStoreException if a general record store exception occurs
     */
    private CldcTransactionRecordImpl readTransactionRecord(
        TransactionStorageImpl st, int recId) throws IOException,
        RecordStoreException {
        byte[] nextRec = st.getRecord(recId);
        CldcTransactionRecordImpl r = null;
        if (nextRec.length > SIZE_OF_INT) {
            ByteArrayInputStream bis =
                new ByteArrayInputStream(nextRec);
            DataInputStream is = new DataInputStream(bis);
            r = CldcTransactionRecordImpl.read(is);
            r.setRecordID(recId);
        }
        return r;
    }

    /**
     * This service function writes TransactionRecord into store.
     *
     * @param record CldcTransactionRecordImpl reference
     * @param st TransactionStorageImpl reference
     * @param recId ID of the record to be replaced or 0
     *
     * @throws IOException  if an I/O error occurs
     * @throws RecordStoreException if a general record store exception occurs
     */
    private void writeTransactionRecord(CldcTransactionRecordImpl record,
        TransactionStorageImpl st, int recId) throws IOException,
        RecordStoreException {
        
        /* formate transaction inside ByteArray */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        record.write(os);
        byte[] data = bos.toByteArray();

        if (recId == 0) {
            recId = st.addRecord(data);
            record.setRecordID(recId);
        } else {
            st.setRecord(recId, data);
        }
    }
}
