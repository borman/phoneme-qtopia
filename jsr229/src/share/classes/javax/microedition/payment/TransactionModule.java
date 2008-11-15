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

package javax.microedition.payment;

import com.sun.j2me.payment.TransactionModuleImpl;
import com.sun.j2me.payment.PaymentModule;

/**
 * This class is defined by the JSR-229 specification
 * <em>Payment API (PAPI),
 * Version 1.0.0</em>
 */
// JAVADOC COMMENT ELIDED
public class TransactionModule {
    private TransactionModuleImpl impl;
    
    // JAVADOC COMMENT ELIDED
    public TransactionModule(Object object) throws TransactionModuleException {
        impl = PaymentModule.getInstance().createTransactionModule(object);
    }
    
    // JAVADOC COMMENT ELIDED
    public void setListener(TransactionListener listener) {
        impl.setListener(listener);
    }
    
    // JAVADOC COMMENT ELIDED
    public int process(int featureID,
            String featureTitle,
            String featureDescription)
            throws  TransactionModuleException,
            TransactionFeatureException,
            TransactionListenerException {
        int transactionID = 0;
        try {
            transactionID = impl.process(featureID, featureTitle, 
                    featureDescription, null);
        } catch (TransactionPayloadException e) {
            // never happens
        }

        return transactionID;
    }
    
    // JAVADOC COMMENT ELIDED
    public int process(int featureID,
            String featureTitle,
            String featureDescription,
            byte[] payload)
            throws  TransactionModuleException,
            TransactionFeatureException,
            TransactionListenerException,
            TransactionPayloadException {
        return impl.process(featureID, featureTitle, featureDescription, 
                payload);
    }
    
    // JAVADOC COMMENT ELIDED
    public TransactionRecord[] getPastTransactions(int max) {
        return impl.getPastTransactions(max);
    }
    
    // JAVADOC COMMENT ELIDED
    public void deliverMissedTransactions() 
            throws  TransactionListenerException {
        impl.deliverMissedTransactions();
    }
    
}
