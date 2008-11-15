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

import java.util.Random;

import javax.microedition.payment.*;
import com.sun.midp.rms.*;
import javax.microedition.rms.*;
import com.sun.midp.security.*;

import com.sun.midp.util.DateParser;
import com.sun.midp.io.Util;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.midp.lcdui.*;
import com.sun.midp.midlet.*;
import com.sun.midp.midletsuite.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.main.MIDletSuiteVerifier;

import com.sun.midp.midlet.MIDletSuite;

/**
 * This class extends the <code>PaymentModule</code> class with the device
 * dependent methods.
 *
 * @version
 */
public class CldcPaymentModule extends PaymentModule {
    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    // === DEBUG MODE ===
    /** Random generator for debug purpose */
    static Random random = new Random();
    // === DEBUG MODE ===

    /** The name of the file where to store the Midlet PaymentID  */
    private static final String PAYMENT_ID_FILE_NAME = "payment_id";

    /**  Record ID for application payment ID store    */
    private static final int PAYMENT_ID_RECORD = 1;

    /**
     * Creates a new instance of CldcPaymentModule.
     */
    protected CldcPaymentModule() {
    }

    /**
     * It's a factory method for <code>TransactionModuleImpl</code>.
     *
     * @param object the application MIDlet initiating a payment transaction
     * @return a new instance of a <code>TransactionModuleImpl</code> subclass.
     * @throws TransactionModuleException indicates a creation failure
     */
    public TransactionModuleImpl createTransactionModule(Object object) throws
        TransactionModuleException {
        return new CldcTransactionModuleImpl(object);
    }

    /**
     * Initializes the transaction store for the given MIDlet suite.
     * <p>
     * Generates fake missed transactions for the
     * <code>Pay-Debug-MissedTransactions</code> debug mode.
     *
     * @param paymentInfo provision information
     * @param suiteId MDIletSuite ID
     * @param appName application name
     */
    public final void initializeTransactionStore(PaymentInfo paymentInfo,
                                                 int suiteId,
                                                 String appName) {
        // === DEBUG MODE ===
        int paymentID = getPaymentID(suiteId);

        int numMissedTransactions = paymentInfo.getDbgMissedTransactions();

        if (paymentInfo.isDemoMode() && (numMissedTransactions > 0)) {
            CldcTransactionStoreImpl transactionStore =
                (CldcTransactionStoreImpl) getTransactionStore();
            try {
                transactionStore.generateFakeRecords(
                    paymentID,
                    appName,
                    paymentInfo,
                    "Feature ",
                    getValidProviders(paymentInfo),
                    numMissedTransactions);
            } catch (IOException e) {
                // ignore
            }
        }
        // === DEBUG MODE ===
    }

    /**
     * Returns the size the given MIDlet suite uses in the transaction store.
     * This size doesn't include the size of the passed transactions (it
     * includes only the part of the store which is removed when the MIDletSuite
     * is uninstalled).
     *
     * @param applicationID the payment application ID of the MIDlet suite
     * @return the size the MIDlet suite takes in the store
     */
    public final int getSizeUsedInStore(int applicationID) {
        TransactionStore transactionStore = getTransactionStore();
        int size = 0;
        try {
            size = transactionStore.getSizeUsedByApplication(applicationID);
        } catch (IOException e) {
            // ignore
        }

        return size;
    }

    /**
     * Uninstalls the given MIDlet suite from the transaction store. It means
     * that the missed transaction records that belong to the suite are removed
     * from the transaction store.
     *
     * @param securityToken a security token with <code>Permissions.AMS</code>
     * @param applicationID the payment application ID of the MIDlet suite
     */
    public final void uninstallFromStore(SecurityToken securityToken,
                                         int applicationID) {
        securityToken.checkIfPermissionAllowed(Permissions.AMS);

        TransactionStore transactionStore = getTransactionStore();
        try {
            transactionStore.removeApplicationRecords(applicationID);
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Return missed(pending) transactions headers for given MIdlet suite.
     *
     * @param suiteId MIDlet suite ID
     * @return header of missed records
     */
    public final String[] getMissedRecordsHeaders(int suiteId) {
        CldcTransactionStoreImpl transactionStore =
            (CldcTransactionStoreImpl) getTransactionStore();
        CldcTransactionRecordImpl[] recs = null;
        String[] headers = null;
        try {
            int appID = getPaymentID(suiteId);
            recs = (CldcTransactionRecordImpl[])
                   transactionStore.getMissedTransactions(appID);

            // there is no missed transaction
            if (recs == null) {
                return null;
            }
            headers = new String[recs.length];
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < headers.length; i++) {
                buff.setLength(0);
                buff.append(recs[i].getFeatureTitle());
                buff.append(": ");
                buff.append(recs[i].getPrice());
                buff.append(recs[i].getCurrency());
                headers[i] = buff.toString();
            }
        } catch (IOException e) {
            // skip, return as it is
        }
        return headers;
    }

    /**
     * Return application payment ID for given midlet suite.
     * Create such ID if it is necessary.
     *
     * @param suiteId suite ID
     * @return payment ID
     */
    public final int getPaymentID(int suiteId) {
        RecordStoreImpl store = null;
        int paymentID = -1;
        try {
            store = RecordStoreImpl.openRecordStore(
                classSecurityToken, suiteId, PAYMENT_ID_FILE_NAME, false);
            try {
                byte[] data = new byte[4];
                data = store.getRecord(1);
                if (data.length == 4) {
                    paymentID = CldcTransactionStoreImpl.
                                getIntFromByteArray(data);
                } else {
                    paymentID = -1;
                }
            } finally {
                store.closeRecordStore();
            }
        } catch (RecordStoreNotFoundException ex) {
            try {
                int appPaymentId = PaymentModule.getInstance().
                                   getNextApplicationID();
                store = RecordStoreImpl.openRecordStore(
                    classSecurityToken, suiteId, PAYMENT_ID_FILE_NAME, true);
                try {
                    byte[] data = CldcTransactionStoreImpl.
                                  getByteArrayFromInt(appPaymentId);
                    store.addRecord(data, 0, data.length);
                    paymentID = appPaymentId;
                } finally {
                    store.closeRecordStore();
                }
            } catch (RecordStoreException e) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                   "Storage Failure: Can not store Payment ID");
                }
            } catch (IOException e) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                   "getPaymentID threw an IOException: " +
                                   e.getMessage());
                }
            }
        } catch (RecordStoreException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                               "Storage Failure: Can not read Payment ID");
            }
        }
        return paymentID;
    }

    /**
     * Remove missed transaction for given suite.
     *
     * @param suiteId suite ID
     */
    public final void removeMissed(int suiteId) {
        int id = getPaymentID(suiteId);
        CldcTransactionStoreImpl transactionStore =
            (CldcTransactionStoreImpl) getTransactionStore();
        try {
            transactionStore.removeMissedTransaction(id);
        } catch (IOException e) {
            // skip
        }
    }

    /**
     * The cleanUp method for the TCK tests. Should be removed when not
     * needed!!! Erases the transaction store.
     *
     * @throws IOException indicating Transaction Store failure
     */
    public final void cleanUp() throws IOException {
        getTransactionStore().cleanUp();
    }

// === DEBUG MODE ===
    /**
     * Handles the success/failure/random debug mode for the given transaction.
     * It's called from the parts of the <code>PaymentModule</code> code where
     * this mode can be applied. If the debug mode is in effect the transaction
     * state is set accordingly and the method returns <code>true</code>.
     *
     * @param transaction the transaction
     * @return <code>true</code> if the transaction is handled in the method
     */
    protected final boolean handleTransactionDebugMode(
        Transaction transaction) {
        PaymentInfo paymentInfo = getPaymentInfo(transaction);

        // (random.nextInt(128) < 64) = approx. one in two will fail
        if (paymentInfo.isDemoMode()) {
            if (paymentInfo.getDbgFailIO() ||
                (paymentInfo.getDbgRandomTests() &&
                 (random.nextInt(128) < 64))) {
                transaction.setState(Transaction.FAILED);
            } else {
                transaction.setState(Transaction.SUCCESSFUL);
            }

            transaction.setNeedsUI(false);
            return true;
        }

        return false;
    }

    /**
     * Handles the auto request debug mode for the given transaction. It's
     * called from the parts of the <code>PaymentModule</code> code where this
     * mode can be applied. If the auto request mode is in effect the
     * transaction state is set accordingly and the method returns
     * <code>true</code>.
     *
     * @param transaction the transaction
     * @return <code>true</code> if the transaction is handled in the method
     *      (= the auto request mode is in effect)
     */
    protected final boolean handleAutoRequestMode(Transaction transaction) {
        PaymentInfo paymentInfo = getPaymentInfo(transaction);

        if (!paymentInfo.isDemoMode()) {
            return false;
        }

        switch (paymentInfo.getDbgAutoRequestMode()) {
        case PaymentInfo.AUTO_REQUEST_REJECT:
            transaction.setState(Transaction.REJECTED);
            transaction.setNeedsUI(false);
            break;

        case PaymentInfo.AUTO_REQUEST_ACCEPT:
            int[] providers = getValidProviders(paymentInfo);

            // we do have at least one supported payment provider
            assignTransaction(transaction, providers[0]);
            break;

        default:
            return false;
        }

        return true;
    }

// === DEBUG MODE ===
    /** Instance of TransactionStore */
    private TransactionStore transactionStore;

    /**
     * Init and return instance of <code>TransactionStore</code>.
     *
     * @return TransactionStore
     */
    public TransactionStore getTransactionStore() {
        if (transactionStore == null) {
            try {
                transactionStore =
                    new CldcTransactionStoreImpl(classSecurityToken);
            } catch (IOException e) {
            }
        }

        return transactionStore;
    }

    /** Instance of Utils class */
    private Utils utilities = getUtilities();

    /**
     * Returns an instance of <code>CldcUtils</code> class.
     *
     * @return the instance
     */
    protected Utils getUtilities() {
        if (utilities == null) {
            utilities = new CldcUtils();
        }

        return utilities;
    }

    /** an preempt token object to pass to donePreempting */
    private Object PreemptToken;

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
    protected void preemptDisplay(SecurityToken token,
                                  Displayable nextDisplayable) {
        DisplayEventHandler d =
            DisplayEventHandlerFactory.getDisplayEventHandler(token);
        if (nextDisplayable != null) {
            try {
                PreemptToken = d.preemptDisplay(nextDisplayable, true);
            } catch (InterruptedException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_NONE,
                             "preemptDisplay threw an InterruptedException: " +
                              ex.getMessage());
                }
            }
        } else {
            d.donePreempting(PreemptToken);
            PreemptToken = null;
        }
    }

    static {
        /* Hand out security token */
        PPSMSAdapter.initSecurityToken(classSecurityToken);
    }
}
