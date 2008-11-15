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

import java.util.Vector;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import javax.microedition.payment.TransactionRecord;

import com.sun.j2me.payment.Transaction;
import com.sun.j2me.payment.ProviderInfo;

/**
 * This class implements <code>TransactionRecord</code>.
 *
 * @version 1.4
 * @see javax.microedition.payment.TransactionRecord
 */
final class CldcTransactionRecordImpl implements TransactionRecord {
    
    /** Status of Transaction - Reserved */
    static final int RESERVED = 0;
    /** Status of Transaction - Missed */
    static final int MISSED = 1;
    /** Status of Transaction - Passed */
    static final int PASSED = 2;

    /** Transaction ID */
    private int transactionID;
    /** Application ID */
    private int applicationID;
    /** Application Name */
    private String applicationName;
    /** Feature ID */
    private int featureID;
    /** Feature Title */
    private String featureTitle;
    /** price */
    private double price;
    /** currency */
    private String currency;
    /** state of Transaction */
    private int state;
    /** Timestamp of Transaction */
    private long timestamp;
    /** Transaction status - Reserved/Missed/Passed */
    private int status;
    /** Fake Transaction */
    private boolean fake;

    /** record ID */
    private int recordID;
    
    /** empty currency */
    private static final String EMPTY_CURRENCY = "";
    /** USD currency */
    private static final String TEMPLATE_CURRENCY = "USD";
    
    /**
     * Creates an instance of the <code>CldcTransactionRecordImpl</code> class
     * from the given application ID, name of the MIDlet suite, transaction,
     * the timestamp and the fake flag. The new transaction record has its
     * <code>wasMissed</code> flag set.
     *
     * @param applicationID the application ID
     * @param applicationName the application name
     * @param transaction the transaction
     * @param timestamp the timestamp
     * @param fake the fake flag
     */
    CldcTransactionRecordImpl(int applicationID, String applicationName, 
            Transaction transaction, long timestamp, boolean fake) {
        this.applicationID = applicationID;
        this.applicationName = applicationName;
        
        transactionID = transaction.getTransactionID();
        featureID = transaction.getFeatureID();
        featureTitle = transaction.getFeatureTitle();
        price = transaction.getPrice();
        currency = transaction.getCurrency();
        
        if (currency == null) {
            currency = EMPTY_CURRENCY;
        }

        switch (transaction.getState()) {
            case Transaction.SUCCESSFUL:
                state = TransactionRecord.TRANSACTION_SUCCESSFUL;
                break;
            case Transaction.REJECTED:
                state = TransactionRecord.TRANSACTION_REJECTED;
                break;
            case Transaction.FAILED:
                state = TransactionRecord.TRANSACTION_FAILED;
                break;
        }
        
        this.timestamp = timestamp;
        this.fake = fake;
        this.status = RESERVED;
        
        this.recordID = 0;
    }

    /**
     * Returns the exact duplicate of this transaction record except of the
     * <code>wasMissed</code> flag which is set to the value of the input
     * parameter.
     *
     * @return the new duplicate transaction record
     */
    CldcTransactionRecordImpl createDuplicateRecord() {
        CldcTransactionRecordImpl newRecord = new CldcTransactionRecordImpl();
        
        newRecord.transactionID = transactionID;
        newRecord.applicationID = applicationID;
        newRecord.applicationName = applicationName;
        newRecord.featureID = featureID;
        newRecord.featureTitle = featureTitle;
        newRecord.price = price;
        newRecord.currency = currency;
        newRecord.state = state;
        newRecord.timestamp = timestamp;
        newRecord.fake = fake;
        newRecord.recordID = recordID;

        newRecord.status = PASSED;
        
        return newRecord;
    }
    
    /**
     * Returns the feature ID.
     *
     * @return the feature ID
     */
    public int getFeatureID() {
        return featureID;
    }

    /**
     * Returns the timestamp when the transaction was finished.
     *
     * @return the timestamp
     */
    public long getFinishedTimestamp() {
        return timestamp;
    }

    /**
     * Returns the final state of the transaction. It can be one of 
     * <code>TRANSACTION_SUCCESSFUL</code>, <code>TRANSACTION_FAILED</code>,
     * <code>TRANSACTION_REJECTED</code>.
     *
     * @return the final state
     * @see javax.microedition.payment.TransactionRecord#TRANSACTION_SUCCESSFUL
     * @see javax.microedition.payment.TransactionRecord#TRANSACTION_FAILED
     * @see javax.microedition.payment.TransactionRecord#TRANSACTION_REJECTED
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state of the transaction. It can be one of 
     * <code>TRANSACTION_SUCCESSFUL</code>, <code>TRANSACTION_FAILED</code>,
     * <code>TRANSACTION_REJECTED</code>.
     *
     * @param transaction the transaction
     */
    void update(Transaction transaction) {
            timestamp = System.currentTimeMillis();
            switch (transaction.getState()) {
            case Transaction.SUCCESSFUL:
                state = TransactionRecord.TRANSACTION_SUCCESSFUL;
                break;
            case Transaction.REJECTED:
                state = TransactionRecord.TRANSACTION_REJECTED;
                break;
            case Transaction.FAILED:
                state = TransactionRecord.TRANSACTION_FAILED;
                break;
        }
    }
    /**
     * Returns the transaction ID of the transaction.
     *
     * @return the transaction ID
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Indicates if the MIDlet have or haven't been notified about the final 
     * state of the transaction.
     *
     * @return <code>true</code> if the MIDlet haven't been notified yet
     */
    public boolean wasMissed() {
        return (status != PASSED);
    }

    /**
     * Indicates status of Transaction:
     * RESERVED/MISSED/PASSED
     * @return  RESERVED if Transaction is started
     *          MISSED if confirmation is received
     *          PASSED if user was notifyed
     */
    public int getStatus() {
        return status;
    }

    /**
     * Change status of Transaction:
     * RESERVED/MISSED/PASSED
     * @param status of Transaction 
     */
    void setStatus(int status) {
        if ((status == MISSED) || (status == PASSED))
        {
            this.status = status;
        }
    }

    /**
     * Returns the appplication ID of the application (MIDlet) which initiated
     * the transaction.
     *
     * @return the application ID
     * @see com.sun.j2me.payment.TransactionStore#getNextApplicationID
     */
    public int getApplicationID() {
        return applicationID;
    }
    
    /**
     * Returns the name of the application (MIDlet) which initiated the 
     * transaction.
     *
     * @return the application name
     */
    public String getApplicationName() {
        return applicationName;
    }
    
    /**
     * Returns the title of the feature.
     *
     * @return the feature title
     */
    public String getFeatureTitle() {
        return featureTitle;
    }

    /**
     * Returns the price of the feature or <code>0</code> if it hasn't been
     * set when the transaction ended.
     *
     * @return the price
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Returns the currency of the price or an empty string if it hasn't been
     * set when the transaction ended.
     *
     * @return the currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Indicates if the current transaction record is fake. A fake record isn't
     * permanently stored into the transaction store file. Fake records are 
     * produced in the debug mode and by MIDlet suites which are run without
     * beeing installed.
     *
     * @return <code>true</code> if the record is fake
     */
    public boolean isFake() {
        return fake;
    }

    /**
     * Returns the size (in bytes) which is needed to serialize the transaction
     * record.
     *
     * @return the size of the serialized transaction record
     */
    int getSerializedSize() {
        if (fake) {
            return 0;
        }
        
        int size = 0;
        try {
            size = calculateSize(applicationName, featureTitle);
        } catch (IOException e) {
        }
        
        return size;
    }
    
    /**
     * Constructs empty instance
     */
    private CldcTransactionRecordImpl() {
    }
    
    // === DEBUG MODE ===
    /**
     * Creates a fake transaction record from the given information. It's
     * used in the debug mode.
     *
     * @param transactionID unique ID of transaction
     * @param applicationID the application ID
     * @param applicationName the application name
     * @param featureID the feature ID
     * @param featureTitle the title of the feature 
     * @param price the price of the feature
     * @param currency the currency to pay
     * @param state the state of the transaction
     * @param timestamp the timestamp
     *
     * @return new instance of CldcTransactionRecordImpl 
     */
    static CldcTransactionRecordImpl createFakeRecord(int transactionID,
            int applicationID, String applicationName, 
            int featureID, String featureTitle, 
            double price, String currency, int state,
            long timestamp) {
        CldcTransactionRecordImpl record = new CldcTransactionRecordImpl();
                
        record.transactionID = transactionID;
        record.applicationID = applicationID;
        record.applicationName = applicationName;
        record.featureID = featureID;
        record.featureTitle = featureTitle;
        record.price = price;
        record.currency = currency;
        record.state = state;
        record.timestamp = timestamp;
        
        record.status = RESERVED;
        record.fake = true;
        
        record.recordID = 0;
        
        return record;
    }
    // === DEBUG MODE ===
    
    /**
     * Creates a new transaction record from the data read from the given input 
     * stream.
     *
     * @param is the input stream
     * @return the new transaction record
     * @throws IOException indicates a reading failure
     */
    static CldcTransactionRecordImpl read(DataInputStream is) 
            throws IOException {
        CldcTransactionRecordImpl newRecord = new CldcTransactionRecordImpl();
        
        newRecord.transactionID = is.readInt();
        newRecord.applicationID = is.readInt();
        newRecord.applicationName = is.readUTF();
        newRecord.featureID = is.readInt();
        newRecord.featureTitle = is.readUTF();
        newRecord.price = is.readDouble();
        newRecord.currency = is.readUTF();
        newRecord.state = is.readInt();
        newRecord.timestamp = is.readLong();
        newRecord.status = is.readInt();

        return newRecord;
    }
    
    /**
     * Stores the transaction record into the given output stream.
     *
     * @param os the output stream
     * @throws IOException indicates a writing failure
     */
    void write(DataOutputStream os) throws IOException {
        os.writeInt(transactionID);
        os.writeInt(applicationID);
        os.writeUTF(applicationName);
        os.writeInt(featureID);
        os.writeUTF(featureTitle);
        os.writeDouble(price);
        os.writeUTF(currency);
        os.writeInt(state);
        os.writeLong(timestamp);
        os.writeInt(status);
    }

    /**
     * Calculates the size in bytes which will be needed to store a transaction
     * record with the specified application name and feature title.
     *
     * @param applicationName the application name
     * @param featureTitle the feature title
     * @return the size needed to store such record
     */
    static int calculateSize(String applicationName, 
            String featureTitle) throws IOException {
        // calculate the required space
        int size;
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        
        try {
            os.writeUTF(applicationName);
            os.writeUTF(featureTitle);
            os.writeUTF(TEMPLATE_CURRENCY);
            size = bos.size();
            
        } finally {
            os.close();
        }
        
        size += 4 + // transactionID
                4 + // applicationID
                4 + // featureID
                8 + // price
                4 + // state
                8 + // timestamp
                4;  // status
        
        return size;
    }
    
    /**
     * Stores Record ID for internal usage
     *
     * @param recordID unique ID of the Transaction Record 
     */
    void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    /**
     * Returns Record ID of the transaction
     *
     * @return recordID of the Transaction
     */
    int getRecordID() {
        return recordID;
    }
    
    /**
     * Adds Transaction Record into the list
     * 
     * @param v the list of sorted transactions
     * @return index in the element
     */
    int add2list(Vector v) {
        int count = v.size();
        if (count == 0) {
            v.addElement(this);
        } else {
            do {
                if (((CldcTransactionRecordImpl)v.elementAt(count-1)).
                        getFinishedTimestamp() <= this.timestamp) {
                    v.insertElementAt(this,  count);
                    break;
                }
                count--;
            } while (count > 0);
            if (count == 0) {
                v.insertElementAt(this,  count);
            }
        }
        return count;
    }
}
