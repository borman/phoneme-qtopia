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

import javax.microedition.midlet.MIDlet;

import java.io.IOException;
import java.util.Random;

import javax.microedition.payment.TransactionListener;
import javax.microedition.payment.TransactionRecord;
import javax.microedition.payment.TransactionModuleException;
import javax.microedition.payment.TransactionFeatureException;
import javax.microedition.payment.TransactionListenerException;
import javax.microedition.payment.TransactionPayloadException;

import com.sun.midp.security.Permissions;
        
/**
 * This class holds a "real" implementation of the transaction module. All 
 * calls to a transaction module instance are delegated to the instance of this 
 * class which has been associated with the delegating instance.
 *
 * @version 
 */
public abstract class TransactionModuleImpl {
    /** The caller MIDlet. */
    protected MIDlet midlet;
    /** 
     * The listener set by the application or <code>null</code> if hasn't been
     * set. 
     */
    protected TransactionListener listener;

    /** The size limit of a payload from the specification. */
    public static final int PAYLOAD_LIMIT = 132;
    
    /** 
     * Creates a new instance of <code>TransactionModuleImpl</code>. It 
     * requires a reference to the object, which originated the call. This 
     * object should be a MIDlet.
     *
     * @param object the caller MIDlet
     * @throws TransactionModuleException indicates an error preventing the 
     *      MIDlet from using the payment API
     * @see javax.microedition.payment.TransactionModule#TransactionModule
     */
    protected TransactionModuleImpl(Object object) 
            throws TransactionModuleException {
        if (object == null) {
            throw new NullPointerException();
        }
        if (!(object instanceof MIDlet)) {
            throw new TransactionModuleException("The object is not referring to the application");
        }
         midlet = (MIDlet)object;

    }

    /** 
     * Sets the listener which should be notified about finishing of 
     * transactions or removes the listener which has been set before if  
     * <code>listener</code> is <code>null</code>.
     *
     * @param listener the new listener or <code>null</code>
     * @see javax.microedition.payment.TransactionModule#setListener
     */
    synchronized public final void setListener(TransactionListener listener) {
        this.listener = listener;
    }

    /** 
     * Initiates a new payment transaction from the given input values.
     *
     * @param featureID the feature ID
     * @param featureTitle the feature title
     * @param featureDescription the feature description
     * @param payload the payload
     * @return the ID of the new transaction
     * @throws TransactionModuleException indicates incorrect input parameters
     *      or overloading of the payment module
     * @throws TransactionFeatureException if the <code>featureID</code> is 
     *      incorrect
     * @throws TransactionListenerException if the transaction listener hasn't
     *      been set prior the call
     * @throws TransactionPayloadException if the payload exceed the limit
     *      allowed by the specification
     * @see javax.microedition.payment.TransactionModule#process
     * @see javax.microedition.payment.TransactionModuleException
     * @see javax.microedition.payment.TransactionFeatureException
     * @see javax.microedition.payment.TransactionListenerException
     * @see javax.microedition.payment.TransactionPayloadException
     */
    public final int process(int featureID, String featureTitle, 
            String featureDescription, byte[] payload) 
                throws TransactionModuleException, TransactionFeatureException,
                    TransactionListenerException, TransactionPayloadException {
        
        // check for the javax.microedition.payment.process permission
        try {
            checkForPermission(Permissions.PAYMENT, null);
        } catch (InterruptedException e) {
            throw new SecurityException();
        }
        
        // null featureTitle or featureDescription
        if ((featureTitle == null) || (featureDescription == null)) {
            throw new TransactionModuleException("The " + 
                    ((featureTitle == null) ? 
                        "featureTitle" : "featureDescription") + " is null");
        }
        
        // empty featureTitle or featureDescription
        if ((featureTitle.length() == 0) || 
                (featureDescription.length() == 0)) {
            throw new TransactionModuleException("The " + 
                    ((featureTitle.length() == 0) ? 
                        "featureTitle" : "featureDescription") + " is empty");
        }

        // payload limit exceeded
        if ((payload != null) && (payload.length > PAYLOAD_LIMIT)) {
            throw new TransactionPayloadException();
        }

        // invalid feature ID
        if ((featureID < 0) || 
                (featureID >= getPaymentInfo().getNumFeatures())) {
            throw new TransactionFeatureException();
        }

        // listener hasn't been set
        if (listener == null) {
            throw new TransactionListenerException();
        }
        
        return PaymentModule.getInstance().addTransaction(this, featureID, 
                featureTitle, featureDescription, payload);
    }

    /**
     * Returns an array of <code>TransactionRecord</code> objects that 
     * represent the past transactions, initiated by the MIDlet and about whose
     * final state the MIDlet has been already notified.
     *
     * @param max limits the number of returned transaction records
     * @return an array of the transaction records or <code>null</code> if 
     *      <code>max</code> is set to <code>0</code> or there is no transaction
     *      to return
     * @see javax.microedition.payment.TransactionModule#getPastTransactions
     */
    public final TransactionRecord[] getPastTransactions(int max) {
        if (max == 0) {
            return null;
        }

        TransactionRecord[] allPassed = getPassedTransactions();
        
        if ((allPassed == null) || (allPassed.length <= max)) {
            return allPassed;
        }

        TransactionRecord[] truncated = new TransactionRecord[max];
        System.arraycopy(allPassed, 0, truncated, 0, max);

        return truncated;
    }

    /**
     * Makes the payment module to notify the listener about the MIDlet 
     * transactions which the listener couldn't have been notified about before 
     * (the MIDlet crashed or ended before the notification could take place).
     *
     * @throws TransactionListenerException if the transaction listener hasn't
     *      been set prior the call
     * @see 
     * javax.microedition.payment.TransactionModule#deliverMissedTransactions
     */
    public void deliverMissedTransactions() 
            throws  TransactionListenerException {
        if (listener == null) {
            throw new TransactionListenerException();
        }

        TransactionRecord[] allMissed = getMissedTransactions();

        if (allMissed != null) {
            PaymentModule.getInstance().addTransactionsForNotification(
                    allMissed, this);
        }
    }

    /**
     * Returns the MIDlet.
     *
     * @return the MIDlet
     */
    public final MIDlet getMIDlet() {
        return midlet;
    }

    /**
     * Returns an array of the missed transactions associated with the MIDlet
     * suite.
     *
     * @return an array of the missed transactions
     */
    protected TransactionRecord[] getMissedTransactions() {
        int applicationID = getApplicationID();
                
        TransactionStore transactionStore = 
                PaymentModule.getInstance().getTransactionStore();
        TransactionRecord[] allMissed = null;
        
        try {
            allMissed = transactionStore.getMissedTransactions(applicationID);
        } catch (IOException e) {
        }
        
        return allMissed;
    }
    
    /**
     * Returns an array of the passed transactions associated with the MIDlet
     * suite.
     *
     * @return an array of the passed transactions
     */
    protected TransactionRecord[] getPassedTransactions() {
        int applicationID = getApplicationID();
                
        TransactionStore transactionStore = 
                PaymentModule.getInstance().getTransactionStore();
        TransactionRecord[] allPassed = null;
        
        try {
            allPassed = transactionStore.getPassedTransactions(applicationID);
        } catch (IOException e) {
        }
        
        return allPassed;
    }

    /**
     * Returns the transaction listener.
     *
     * @return the transaction listener
     */
    final TransactionListener getListener() {
        return listener;
    }

    /**
     * Ensures that the MIDlet will have required privileges to do a protected
     * operation.
     *
     * @param permission the required permission
     * @param name an additional info string
     * @throws SecurityException if the permission is not granted
     * @throws InterruptedException if the thread waiting for the permission
     *      is interrupted
     */
    protected abstract void checkForPermission(int permission, String name) 
            throws InterruptedException;
    
    /**
     * Returns the payment provisioning information associated with the MIDlet.
     *
     * @return the payment provisioning information
     */
    protected abstract PaymentInfo getPaymentInfo();

    /**
     * Stores the payment provisioning information associated with the MIDlet.
     *
     * @throws IOException indicates an output error
     */
    protected abstract void savePaymentInfo() throws IOException;

    /**
     * Returns the MIDlet payment ID that can be used to store transaction 
     * records for the MIDlet initiated transactions into the transaction store.
     *
     * @return the ID
     */
    protected abstract int getApplicationID();
}
