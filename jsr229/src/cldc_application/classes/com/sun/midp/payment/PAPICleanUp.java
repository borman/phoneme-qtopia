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

package com.sun.midp.payment;

import java.io.*;

import com.sun.j2me.payment.*;
import com.sun.midp.i18n.*;

/**
 * Real interface to handle MIDlet Payment functionality.
 */
public class PAPICleanUp {

    /** public constructor. */
    public PAPICleanUp() {
    }

    /**
     * The function for a Missed Transactions  validation.
     *
     * @param suiteId The MIDletSuite ID
     * @return null if there is no pending Transaction for this Suite
     *         otherwise returns formated list of missed transactions
     */
    public static String checkMissedTransactions(int suiteId) {
        /* 1) Get list of records */
        CldcPaymentModule payModule =
        ((CldcPaymentModule) PaymentModule.getInstance());
        int paymentID = payModule.getPaymentID(suiteId);
        String[] recs =
            payModule.getMissedRecordsHeaders(suiteId);

        /* 2) Prepare message from record feature tile and price */
        if (recs == null) {
            return null;
        }

        StringBuffer mess = new StringBuffer(
        Resource.getString(ResourceConstants
                           .AMS_PERMISSION_PAYMENT_DELETION_QUESTION));
        for (int i = 0; i < recs.length; i++) {
            mess.append(i + 1);
            mess.append(") ");
            mess.append(recs[i]);
            mess.append("\n");
        }
        return mess.toString();
    }

    /**
     * Remove missed transaction for given midlet suite.
     *
     * @param suiteId midlet suite id
     */
    public static void removeMissedTransaction(int suiteId) {
        ((CldcPaymentModule) PaymentModule.getInstance()).removeMissed(suiteId);

    }
}
