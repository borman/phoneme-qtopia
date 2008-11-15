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

import java.io.IOException;
import javax.microedition.payment.TransactionRecord;

/**
 * This interface defines method, which a class needs to implement to act as
 * a transaction store. An implementation of this class must be thread safe.
 *
 * @version 1.3
 * @see PaymentModule#getTransactionStore
 */
public interface TransactionStore {
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
    public int getNextApplicationID() 
            throws IOException;
    
    /**
     * Reserves space for the given transaction in the store. It should be 
     * called before any call to the <code>addTransaction</code> method to 
     * ensure that the <code>addTransaction</code> method won't fail later
     * (when it is inappropriate) due to full store. This method can apply some 
     * store policies, like enforcing a maximum number of missed transactions
     * per <code>MIDletSuite</code>.
     *
     * @param applicationID the application id
     * @param transaction the transaction
     * @return an unique ID created for the transaction
     * @throws IOException indicates that the store is full or won't accept any
     *      further transaction records from that application
     */
    public int reserve(int applicationID, Transaction transaction) 
            throws IOException;
    
    /**
     * Adds the given transaction to the store. It returns a new transaction 
     * record for the transaction. This transaction record must have its 
     * <code>wasMissed</code> flag cleared.
     *
     * @param transaction the transaction
     * @return the new transaction record
     * @throws IOException indicates a storage failure
     */
    public TransactionRecord addTransaction(Transaction transaction) 
            throws IOException;
    
    /**
     * It returns <code>true</code> if the <code>setDelivered</code> method 
     * was called for the given transaction ID.
     *
     * @param transactionID the transaction ID
     * @return true if the <code>setDelivered</code> method 
     * was called for the given transaction ID
     *
     * @throws IOException indicates a storage failure
     */
    public boolean wasDelivered(int transactionID) throws IOException;

    /**
     * This method is called after the application is successfully notified 
     * about the transaction with the given transaction ID.
     *
     * @param transactionID the transaction ID
     * @throws IOException indicates a storage failure
     */
    public void setDelivered(int transactionID) 
            throws IOException;
    
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
    public TransactionRecord[] getMissedTransactions(int applicationID)
            throws IOException;
    
    /**
     * Returns an array of the past transaction records for the given 
     * application ID. The transaction record are returned in the reverse order
     * as they have been added to the store (most recent first). Each 
     * transaction record must have its <code>wasMissed</code> flag cleared.
     *
     * @param applicationID the application ID
     * @return the array of the missed transaction records
     * @throws IOException indicates a storage failure
     */
    public TransactionRecord[] getPassedTransactions(int applicationID)
            throws IOException;

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
            throws IOException;
    
    /**
     * Removes the missed records used by the application of the given 
     * application ID. This is to be used, when the MIDlet suite is uninstalled.
     *
     * @param applicationID the application ID
     * @throws IOException indicates a storage failure
     */
    public void removeApplicationRecords(int applicationID)
            throws IOException;

    /**
     * Removes all transaction records from the store. This is a helper method
     * which is used in test suites to get clean state before test execution.
     *
     * @throws IOException indicates a storage failure
     */
    public void cleanUp() throws IOException;
}
