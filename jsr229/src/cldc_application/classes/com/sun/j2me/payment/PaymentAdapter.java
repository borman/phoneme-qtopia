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

import com.sun.midp.security.*;

import javax.microedition.lcdui.Displayable;

/**
 * This class represents an adapter which is responsible for handling of
 * transactions with an assigned provider. Each instance should be responsible
 * for some provider specific payment method (one instance of premium priced
 * SMS adapter for each PPSMS provider).
 * 
 * @version 1.3
 */
public abstract class PaymentAdapter implements TransactionProcessor { 
    /**
     * Replaces the current <code>Displayable</code> with the new one if the
     * <code>nextDisplayable</code> is not <code>null</code> or it recovers 
     * the previous <code>Displayable</code> if the <code>nextDisplayable</code>
     * is <code>null</code>.
     *
     * @param token a security token, which allows preempting
     * @param nextDisplayable the <code>Displayable</code> to show or 
     *      <code>null</code> if the recovery of the old 
     *      <code>Displayable</code> is requested
     */
    protected final void preemptDisplay(SecurityToken token, 
            Displayable nextDisplayable) {
        PaymentModule.getInstance().preemptDisplay(token, nextDisplayable);
    }

    /**
     * Validates the price information which are specified in the application
     * manifest file for the provider handled by this adapter. It throws an
     * <code>PaymentException</code> if the parameters are incorrect.
     *
     * @param price the price to pay when using this provider
     * @param paySpecificPriceInfo the specific price information string from 
     *      the manifest
     * @throws PaymentException if the provided information is correct
     */
    public void validatePriceInfo(double price, String paySpecificPriceInfo) 
            throws PaymentException {
    }
    
    /**
     * Returns a display name for this particular adapter. It should represent
     * a payment method (premium priced sms, credit card...).
     *
     * @return the display name
     */
    public abstract String getDisplayName();
    
    /**
     * Returns a question which is used for this adapter instance when the
     * user chooses between providers for the particular payment.
     *
     * @param provider the application supplied provider name
     * @param price the price to pay when using the provider
     * @param currency the currency of the payment
     * @return the question to ask (Are you sure, you want to buy this 
     *      feature...)
     */
    public String getPaymentQuestion(String provider, double price, 
            String currency) {
        int multiplied = (int)(price * 100 + 0.5);
        String priceString = Integer.toString(multiplied);
        int length = priceString.length();

        String[] values = {
            currency,
            priceString.substring(0, length - 2),
            priceString.substring(length - 2),
            getDisplayName(),
            provider
        };

        return PaymentModule.getInstance().getUtilities().
                getString(Utils.PAYMENT_UPDATE_DLG_QUESTION, values);
    }
    
    /**
     * Processes the given transaction, updates its state and returns the same
     * transaction instance or a new one (an instance of 
     * a <code>Transaction</code> subclass), which is based on the old 
     * transaction, but adds more (adapter specific) information to it.
     *
     * The current implementation fails any transaction with the
     * <code>Transaction.ASSIGNED</code> state, so it has to be overriden to
     * work properly. It also sets the transaction processor of the given 
     * transaction to the payment module instance. It's recomended to call
     * this method from the overriden one for every unhandled transaction
     * (with the state, which can't be further processed in the adapter). 
     * It ensures that the control over the transaction will return to the 
     * payment module.
     *
     * @param transaction the transaction to be processed
     * @return the transaction after processing
     */
    public Transaction process(Transaction transaction) {
        if (transaction.getState() == Transaction.ASSIGNED) {
            // was not processed by the child adapter class
            transaction.setState(Transaction.FAILED);
            transaction.setNeedsUI(false);
        }

        // return any transaction which was not processed by the payment
        // adapter back to the payment module
        transaction.setTransactionProcessor(PaymentModule.getInstance());
        return transaction;
    }
}
