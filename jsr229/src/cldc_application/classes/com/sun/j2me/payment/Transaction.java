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

/**
 * A transaction holds the state and information about payment. It's state 
 * is updated by the associated transaction processor. 
 * <p>
 * There are the following predefined states:
 * <ul>
 *      <li><code>ENTERED</code> - the payment has been initiated by the 
 *              application
 *      </li>
 *      <li><code>UPDATE</code> - the transaction should be updated from the
 *              update URL
 *      </li>
 *      <li><code>ASSIGNED</code> - the transaction has been assigned to the 
 *              payment adapter instance associated with the user selected 
 *              provider
 *      </li>
 *      <li><code>SUCCESSFUL</code> - the payment has been successfully finished
 *      </li>
 *      <li><code>FAILED</code> - the payment failed because of errors
 *      </li>
 *      <li><code>REJECTED</code> - the payment has been rejected by the user
 *      </li>
 *      <li><code>DISCARDED</code> - the transaction should be silently 
 *              discarded by the payment module
 *      </li>
 * </ul>
 * <p>
 * The <code>Transaction</code> class can be extended by some adapter specific
 * subclass, which can hold more information about payment and can define some
 * adapter specific states. For this purpose, there is a special constructor 
 * which initiates the transaction from the information provided by the 
 * transaction given as a parameter. There is also defined the 
 * <code>ADAPTER_SPECIFIC</code> constant, which should be used to number 
 * adapter specific states (<code>ADAPTER_SPECIFIC</code>, 
 * <code>ADAPTER_SPECIFIC + 1</code>, <code>ADAPTER_SPECIFIC + 2</code>...).
 *
 * @version 1.7
 */
public class Transaction {

    /** A predefined transaction state. */
    public static final int ENTERED = 0;
    /** A predefined transaction state. */
    public static final int UPDATE = 1;
    /** A predefined transaction state. */
    public static final int ASSIGNED = 2;
    /** A predefined transaction state. */
    public static final int SUCCESSFUL = 3;
    /** A predefined transaction state. */
    public static final int FAILED = 4;
    /** A  predefined transaction state. */
    public static final int REJECTED = 5;
    /** A  predefined transaction state. */
    public static final int DISCARDED = 6;
    
    /** The starting value of adapter specific states. */
    protected static final int ADAPTER_SPECIFIC = 0x100;
   
    private int transactionID;

    private int featureID;
    private String featureTitle;
    private String featureDescription;
    private byte[] payload;
    
    private TransactionModuleImpl transactionModule;

    private String providerName;
    private String currency;
    private double price;
    private String specificPriceInfo;

    private int state;

    private boolean waiting;
    private boolean needsUI;
    
    private TransactionProcessor processor;  
    
    /** 
     * Creates a new instance of <code>Transaction</code>.
     *
     * @param processor the initial transaction processor responsible for 
     *      processing of this transaction
     * @param module the transaction module associated with the transaction
     * @param featureID the identifier of the feature to be paid for
     * @param featureTitle the title of the feature
     * @param featureDescription the description of the feature
     * @param payload the payload to be transfered as a part of the payment or
     *      <code>null</code> if no such payload required
     */
    Transaction(TransactionProcessor processor,
            TransactionModuleImpl module,
            int featureID, 
            String featureTitle,
            String featureDescription,
            byte[] payload) {

        this.transactionModule = module;
        
        this.featureID = featureID;
        this.featureTitle = featureTitle;
        this.featureDescription = featureDescription;
        this.payload = payload;
        
        this.processor = processor;
        this.needsUI = true;
    }

    /**
     * Creates a new instance of <code>Transaction</code> with the fields 
     * initialized from the given original transaction.
     *
     * @param templ the original transaction
     */
    public Transaction(Transaction templ) {
        transactionID = templ.transactionID;
        
        featureID = templ.featureID;
        featureTitle = templ.featureTitle;
        featureDescription = templ.featureDescription;
        payload = templ.payload;
        transactionModule = templ.transactionModule;

        providerName = templ.providerName;
        currency = templ.currency;
        price = templ.price;
        specificPriceInfo = templ.specificPriceInfo;
        
        state = templ.state;
        
        waiting = templ.waiting;
        needsUI = templ.needsUI;

        processor = templ.processor;
    }

    /** 
     * Returns the title of the feature, which is paid for by this transaction.
     *
     * @return the title of the feature
     */
    public final String getFeatureTitle() {
        return featureTitle;
    }

    /** 
     * Returns the description of the feature, which is paid for by this 
     * transaction.
     *
     * @return the description of the feature
     */
    public final String getFeatureDescription() {
        return featureDescription;
    }
        
    /** 
     * Returns the payload which is a part of the payment or <code>null</code>
     * if it's undefined.
     *
     * @return the payload or <code>null</code>
     */
    public final byte[] getPayload() {
        return payload;
    }
    
    /** 
     * Returns the payload which is a part of the payment or <code>null</code>
     * if it's undefined.
     *
     * @return the payload or <code>null</code>
     */
    public final String getProviderName() {
        return providerName;
    }

    /** 
     * Returns the currency of the payment.
     *
     * @return the currency of the payment
     */
    public final String getCurrency() {
        return currency;
    }
    
    /** 
     * Returns the price of the feature, which is paid.
     *
     * @return the price of the feature
     */
    public final double getPrice() {
        return price;
    }
    
    /** 
     * Returns the provider specific price information associated with 
     * the paid feature.
     *
     * @return the provider specific price information
     */
    public final String getSpecificPriceInfo() {
        return specificPriceInfo;
    }

    /** 
     * Sets the transaction processor of the transaction.
     *
     * @param processor the new transaction processor
     */
    public void setTransactionProcessor(TransactionProcessor processor) {
        this.processor = processor;
    }

    /**
     * Puts the transaction into or resumes it from the waiting. A transaction 
     * which is waiting is not processed by the transaction processing thread 
     * of the payment module (its state doesn't change). A transaction can wait 
     * for some user response or the end of some adapter specific thread.
     *
     * @param value if <code>true</code> the transaction is entering the 
     *      waiting, if <code>false</code> the transaction is ending its waiting
     */
    public final void setWaiting(boolean value) {
        // should be synchronized?
        if (waiting == value) {
            return;
        }
        if (waiting) {
            waiting = false;
            // notify the payment module
            PaymentModule.getInstance().continueProcessing();
            return;
        }
        waiting = true;
    }

    /**
     * Indicates if the transaction is waiting for some event.
     *
     * @return <code>true</code> if the transaction is waiting
     */
    public final boolean isWaiting() {
        return waiting;
    }

    /**
     * Sets the value which indicates if the transaction needs or will need
     * some user response to be finished. Setting this value to 
     * <code>true</code> can block this or other transactions that also need 
     * user response from processing (only one such transaction can be 
     * processed at a time). Initialy this value is set to <code>true</code>
     * and is an adapter responsibility to set it to <code>false</code> at the
     * right time.
     *
     * @param value <code>true</code> if the transaction needs or will need
     *      an user response to be finished
     */
    public final void setNeedsUI(boolean value) {
        needsUI = value;
    }
    
    /**
     * Indicates if the transaction needs or will need an user response to be
     * finished.
     *
     * @return <code>true</code> if the transaction needs an user response
     */
    public final boolean needsUI() {
        return needsUI;
    }
    
    /**
     * Sets the state of the transaction to the new value.
     *
     * @param newState the new state
     * @see #getState
     */
    public final void setState(int newState) {
        state = newState;
    }

    /**
     * Returns the current state of the transaction.
     *
     * @return the current state
     * @see #setState
     */
    public final int getState() {
        return state;
    }
    
    /**
     * Returns the transaction ID value.
     *
     * @return the transaction ID
     */
    public final int getTransactionID() {
        return transactionID;
    }
    
    /**
     * Returns the id of the paid feature.
     *
     * @return the feature id
     */
    public final int getFeatureID() {
        return featureID;
    }

    /**
     * Returns the associated transaction module.
     *
     * @return the transaction module
     */
    public final TransactionModuleImpl getTransactionModule() {
        return transactionModule;
    }
    
    /**
     * Processes the transaction. Delegates the call to the associated 
     * transaction processor.
     *
     * @return the fully or partially processed transaction.
     */
    final Transaction process() {
        return processor.process(this);
    }

    /**
     * Sets the transaction ID for the transaction.
     *
     * @param value the new transaction ID value
     */
    final void setTransactionID(int value) {
        transactionID = value;
    }
    
    /**
     * Sets the name of the selected provider.
     *
     * @param value the new provider name
     */
    final void setProviderName(String value) {
        providerName = value;
    }

    /**
     * Sets the currency of the payment.
     *
     * @param value the currency
     */
    final void setCurrency(String value) {
        currency = value;
    }
    
    /**
     * Sets the price of the paid feature.
     *
     * @param value the price
     */
    final void setPrice(double value) {
        price = value;
    }

    /**
     * Sets the provider specific price information.
     *
     * @param value the provider specific price information
     */
    final void setSpecificPriceInfo(String value) {
        specificPriceInfo = value;
    }
}
