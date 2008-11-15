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
import com.sun.midp.midlet.*;
import com.sun.midp.midletsuite.*;

import java.io.IOException;
import java.util.Random;

import javax.microedition.payment.TransactionListener;
import javax.microedition.payment.TransactionRecord;
import javax.microedition.payment.TransactionModuleException;
import javax.microedition.payment.TransactionFeatureException;
import javax.microedition.payment.TransactionListenerException;
import javax.microedition.payment.TransactionPayloadException;

import com.sun.midp.security.Permissions;
import com.sun.midp.util.Properties;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

import java.io.InputStream;
import javax.microedition.rms.RecordStoreException;
import com.sun.midp.log.Logging;
import java.io.ByteArrayInputStream;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStore;
import com.sun.midp.installer.JadProperties;
import com.sun.midp.log.LogChannels;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

/**
 * This class implements parts of <code>TransactionModuleImpl</code> which
 * are dependent on the CLDC. It's created by a factory method in the payment
 * module.
 *
 * @version 
 * @see CldcPaymentModule#createTransactionModule
 */
public class CldcTransactionModuleImpl extends TransactionModuleImpl {

    /** MDIletSuite being run */
    private MIDletSuite midletSuite;

    /**
     * The name of the file where the Midlet Payment Update Info is stored.
     */
    private static final String PAYMENT_UPDATE_FILE_NAME =
        "payment_update";

    /**
     * MIDlet property for the Payment Version.
     */
    public static final String PAY_VERSION_PROP = "Pay-Version";

    /** MIDletSuite payment info */
    private PaymentInfo paymentInfo = null;

    /**
     * Creates a new instance of <code>CldcTransactionModuleImpl</code>.
     * Requires a reference to the application MIDlet which initiated the
     * payment.
     * <p>
     * Handles the <code>Pay-Debug-FailInitialize</code> debug mode.
     *
     * @param object the caller MIDlet
     * @throws TransactionModuleException indicates an error preventing the
     *      MIDlet from using the payment API
     * @see javax.microedition.payment.TransactionModule#TransactionModule
     */
    protected CldcTransactionModuleImpl(Object object) throws
        TransactionModuleException {
        super(object);

        PaymentInfo paymentInfo = getPaymentInfo();
        if (paymentInfo == null) {
            throw new TransactionModuleException("Missing provisioning " +
                    "information");
        }

        /* Check if MIDletSuite is Trusted */
        // Removed - SecurityException is thrown if untrusted MIDlet tries to use
        // restricted permission, so no additional check is required
//         if (!paymentInfo.isDemoMode() && !getMIDletSuite().isTrusted()) {
//             throw new TransactionModuleException("MidletSuite is untrusted");
//         }

        // === DEBUG MODE ===
        // (PaymentModule.random.nextInt(192) < 64) = approx. one in three will
        // fail in this way
        if (paymentInfo.isDemoMode() && (
            paymentInfo.getDbgFailInitialize() ||
            paymentInfo.getDbgRandomTests() &&
            (CldcPaymentModule.random.nextInt(192) < 64))) {
            throw new TransactionModuleException("Debug mode");
        }
        // === DEBUG MODE ===
    }

    /**
     * Returns the MIDlet suite of the MIDlet.
     *
     * @return the MIDlet suite
     */
    private MIDletSuite getMIDletSuite() {
        if (midletSuite == null) {
            midletSuite =
                (MIDletSuite) Scheduler.getScheduler().getMIDletSuite();
        }
        return midletSuite;
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
    protected void checkForPermission(int permission, String name) throws
        InterruptedException {
        getMIDletSuite().checkForPermission(permission, name);
    }

    /**
     * Helper class that allows PaymentInfo to be created from MIDletSuiet
     * properies
     */
    class PropertiesWraper extends Properties {
        /** Provision info source */
        private MIDletSuite midletSuite;

        /**
         *  PropertiesWraper constructor
         *
         * @param suite property storage
         */
        public PropertiesWraper(MIDletSuite suite) {
            midletSuite = suite;
        }

        /**
         * Returns property from given MIDletSuite
         *
         * @param key property name
         * @return property value or null
         */
        public String getProperty(String key) {
            return midletSuite.getProperty(key);
        }
    }


    /**
     * Returns the payment provisioning information associated with the MIDlet.
     *
     * @return the payment provisioning information
     */
    protected PaymentInfo getPaymentInfo() {
        if (paymentInfo == null) {
            if (getMIDletSuite().getProperty(PAY_VERSION_PROP) == null) {
                // quick check
                return null;
            }
            RecordStore store;
            PropertiesWraper props = new PropertiesWraper(getMIDletSuite());
            // try to read updated provision info
            try {
                store = RecordStore.openRecordStore(PAYMENT_UPDATE_FILE_NAME,
                    false);
                try {
                    byte[] data = new byte[store.getRecordSize(1)];
                    data = store.getRecord(1);

                    InputStream bis = new ByteArrayInputStream(data);
                    JadProperties payProps = new JadProperties();
                    payProps.load(bis);
                    bis.close();

                    paymentInfo = PaymentInfo.createFromProperties(
                        props, payProps);
                } finally {
                    store.closeRecordStore();
                }
            } catch (RecordStoreNotFoundException ex) {

                // if the is no update file try to read payment info from
                // suite properties
                try {
                    paymentInfo = PaymentInfo.createFromProperties(
                        props, props);
                } catch (PaymentException e) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                  "getPaymentInfo threw an PaymentException: " +
                                   e.getMessage());
                    }
                }
            } catch (RecordStoreException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                              "getPaymentInfo threw an RecordStoreException: " +
                              ex.getMessage());
                }
            } catch (PaymentException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                 "getPaymentInfo threw an PaymentException: " +
                                 ex.getMessage());
                }
            } catch (IOException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_AMS,
                                   "getPaymentInfo threw an IOException: " +
                                   ex.getMessage());
                }
            }
            if (paymentInfo != null) {
                // initialize the transaction store for this MIDletSuite only
                // for once
                ((CldcPaymentModule) PaymentModule.getInstance())
                    .initializeTransactionStore(paymentInfo,
                                                midletSuite.getID(),
                    midletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP));
            }
        }
        return paymentInfo;
    }

    /**
     * Stores the payment provisioning information associated with the MIDlet.
     *
     * @throws IOException indicates an output error
     */
    protected void savePaymentInfo() throws IOException {
        PaymentInfo pInfo = getPaymentInfo();

        if (pInfo != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStreamWriter os = new OutputStreamWriter(bos, "UTF-8");
            byte[] data;

            try {
                pInfo.export(os);
                data = bos.toByteArray();
            } finally {
                os.close();
            }

            RecordStore store = null;
            try {
                store = RecordStore.openRecordStore(PAYMENT_UPDATE_FILE_NAME,
                    true);
                try {
                    if (store.getNumRecords() == 0) {
                        /* Record Store is EMPTY - add new record */
                        store.addRecord(data, 0, data.length);
                    } else {
                        /* Replace first record */
                        store.setRecord(1, data, 0, data.length);
                    }
                } finally {
                    store.closeRecordStore();
                }
            } catch (RecordStoreException e) {
                throw new IOException(
                    "Storage Failure: Can not store Payment Info");
            }
        }

    }

    /**
     * Returns the MIDlet payment ID that can be used to store transaction
     * records for the MIDlet initiated transactions into the transaction store.
     *
     * @return the ID
     */
    protected int getApplicationID() {
        return ((CldcPaymentModule) PaymentModule.getInstance())
            .getPaymentID(getMIDletSuite().getID());
    }

    /**
     * Returns an array of the missed transactions associated with the MIDlet
     * suite.
     * <p>
     * It handles the <code>Pay-Debug-MissedTransactions</code> debug mode.
     *
     * @return an array of the missed transactions
     */
    protected TransactionRecord[] getMissedTransactions() {
        // === DEBUG MODE ===
        PaymentInfo paymentInfo = getPaymentInfo();
        if (paymentInfo.isDemoMode() &&
            paymentInfo.getDbgMissedTransactions() >= 0) {
            int applicationID = getApplicationID();

            CldcPaymentModule paymentModule = (CldcPaymentModule)
                                              PaymentModule.getInstance();
            CldcTransactionStoreImpl transactionStore =
                (CldcTransactionStoreImpl) paymentModule.getTransactionStore();
            TransactionRecord[] fakeMissed = null;

            try {
                fakeMissed = transactionStore.getMissedTransactions(
                    applicationID, true);
            } catch (IOException e) {
            }

            return fakeMissed;
        }
        // === DEBUG MODE ===
        return super.getMissedTransactions();
    }
}
