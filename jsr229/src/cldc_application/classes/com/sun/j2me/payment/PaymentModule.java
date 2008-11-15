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

import javax.microedition.payment.TransactionListener;
import javax.microedition.payment.TransactionRecord;
import javax.microedition.payment.TransactionModuleException;

import com.sun.midp.security.*;

import com.sun.midp.util.DateParser;
import com.sun.midp.io.Util;
import com.sun.midp.main.Configuration;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.ConnectionNotFoundException;

/**
 * This class represents a payment module. An instance of this class accepts
 * payment requests from transaction modules. For each payment request it
 * creates an instance of the <code>Transaction</code> class, set itself as
 * an initial transaction processor of this transaction and adds it to the
 * internal queue.
 * <p>
 * The internal queue is processed by a background thread created by the
 * instance of the payment module. This thread iterates through the queued
 * transactions and calls their <code>process</code> methods. This changes the
 * internal states of the transactions until they are fully processed and are
 * removed from the queue.
 * <p>
 * The <code>process</code> method of a transaction delegates the call to the
 * associated transaction processor. This is initially the payment module. That
 * allows him to show a provider selection form. After the user selects the
 * provider for the transaction, the payment module changes the transaction
 * processor of the transaction to the provider's payment adapter. This adapter
 * gets the control over the transaction till it finishes the transaction and
 * returns the control over it back to the payment module. The payment module
 * finally removes the transaction from the transaction queue and notifies the
 * application about the result of the payment.
 * <p>
 * The notification is done by creating a new transaction record for the
 * finished transaction and adding it to the transaction notification
 * queue which is processed by an extra thread which handles the notifications.
 * Each transaction record is automatically added to the transaction store, so
 * it can be obtained after crashing of the emulator.
 *
 * @version 
 */
public abstract class PaymentModule implements TransactionProcessor {
    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** Standard timeout for alerts. */
    private static final int ALERT_TIMEOUT = 1250;

    /** The property that contains payment module class name. */
    private static final String PAYMENT_MODULE =
            "microedition.payment.paymentmodule";

    /** Payment module singleton. */
    private static PaymentModule paymentModule;

    /** Transaction processing thread. */
    private TransactionProcessingThread processingThread;

    /** Transaction state notification thread. */
    private TransactionNotificationThread notificationThread;
    
    /**
     * Creates a new instance of PaymentModule.
     */
    protected PaymentModule() {
    }
    
    /**
     * Returns an instance of the <code>PaymentModule</code> class. It creates
     * only one instance and reuses it each time the method is called.
     *
     * @return the instance
     */
    public static PaymentModule getInstance() {
        if (paymentModule == null) {
            String className = Configuration.getProperty(PAYMENT_MODULE);
            if (className != null) {
                try {
                    paymentModule = (PaymentModule)
                    Class.forName(className).newInstance();
                } catch (ClassNotFoundException  cnfe) {
                    // intentionally ignored
                } catch (InstantiationException  ie) {
                    // intentionally ignored
                } catch (IllegalAccessException  iae) {
                    // intentionally ignored
                }
            }
//            if (paymentModule == null) {
//                paymentModule = new PaymentModule();
//            }
        }
        return paymentModule;
    }
    
    /**
     * It's a factory method for <code>TransactionModuleImpl</code>.
     *
     * @param object the application MIDlet initiating a payment transaction
     * @return a new instance of a <code>TransactionModuleImpl</code> subclass.
     * @throws TransactionModuleException indicates a creation failure
     */
    public abstract TransactionModuleImpl createTransactionModule(
            Object object) throws TransactionModuleException;
    
    /**
     * Returns an instrance of the <code>TransactionStore</code> wich is used
     * for storing of all transaction records produced in the payment module.
     * There is only one such instance, which is returned each time the method
     * is called. This instance is used from both internal threads of the
     * payment module (<code>TransactionProcessingThread</code> and
     * <code>TransactionNotificationThread</code>) and it is left to an
     * implementation of the <code>TransactionStore</code> to be thread safe.
     *
     * @return the transaction store
     */
    protected abstract TransactionStore getTransactionStore();
    
    /**
     * Returns an instance of <code>Utils</code> subclass.
     *
     * @return the instance
     */
    protected abstract Utils getUtilities();
    
    /**
     * Returns a new application ID, which can be used to store transaction
     * records to the transaction store.
     *
     * @return the new application ID
     * @throws IOException if there is a I/O failure while working with the
     *      store
     */
    public final int getNextApplicationID() throws IOException {
        return getTransactionStore().getNextApplicationID();
    }
    
    /**
     * Returns the associated payment information for the given transaction.
     *
     * @param transaction the transaction
     * @return the payment information
     */
    protected static PaymentInfo getPaymentInfo(Transaction transaction) {
        return transaction.getTransactionModule().getPaymentInfo();
    }
    
    /**
     * Saves the payment information for the given transaction into storage.
     *
     * @param transaction the transaction
     */
    protected static void savePaymentInfo(Transaction transaction) {
        try {
            transaction.getTransactionModule().savePaymentInfo();
        } catch (IOException e) {
            // ignore
        }
    }
    
    /**
     * This class represents a transaction processing thread. It iterates
     * through the queued transactions and executes the <code>process</code>
     * method on them.
     */
    private class TransactionProcessingThread extends Thread {

        /** List of transactions being processing. */
        private Transaction[] transactionQueue = new Transaction[16];

        /** Processing thread exit flag. */
        private boolean finished;

        /** Wait for next transaction flag. */
        private boolean wait = true;
        
        /**
         * This method is run when the thread starts. It implements the
         * selection and execution of the queued transactions.
         */
        public void run() {
            // transaction processing queue
            while (!finished) {
                boolean blockUI = false;
                for (int i = 0; i < transactionQueue.length; ++i) {
                    Transaction transaction = transactionQueue[i];
                    if (transaction == null) {
                        continue;
                    }
                    if (blockUI && transaction.needsUI()) {
                        continue;
                    }
                    if (transaction.needsUI()) {
                        blockUI = true;
                    }
                    if (transaction.isWaiting()) {
                        continue;
                    }
                    
                    transaction = transaction.process();
                    if ((transaction != null) && transaction.needsUI()) {
                        blockUI = true;
                    }
                    
                    transactionQueue[i] = transaction;
                    wait = false;
                }
                
                synchronized (transactionQueue) {
                    if (wait) {
                        try {
                            transactionQueue.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    wait = true;
                }
            }
        }
        
        /**
         * Adds a transaction to the transaction queue and makes the
         * transaction processing thread to continue its work.
         *
         * @param transaction the transaction
         * @throws TransactionModuleException if there is no more space for the
         *      new transaction in the queue
         */
        public void addTransaction(Transaction transaction)
        throws TransactionModuleException {
            int i;
            for (i = 0; i < transactionQueue.length; ++i) {
                if (transactionQueue[i] == null) {
                    transactionQueue[i] = transaction;
                    // signal to the transaction processing thread, that there
                    // is something to do
                    continueWork();
                    break;
                }
            }
            
            if (i == transactionQueue.length) {
                throw new TransactionModuleException("No more space for " +
                        "new transactions");
            }
        }
        
        /**
         * Tells the transaction processing thread to continue processing the
         * transactions.
         */
        public void continueWork() {
            synchronized (transactionQueue) {
                wait = false;
                transactionQueue.notify();
            }
        }
    }
    
    /**
     * This class represents a transaction notification thread. It notifies
     * the transaction listeners associated with the transactions about the
     * final state of the transactions. After a successful notification it
     * clears the transaction record's "was missed" flag.
     * <p>
     * It gets the transactions for the notification from its internal
     * transaction notification queue and it has methods allowing the addition
     * of new transaction records to this queue. Each transaction in the queue
     * has associated a transaction module (<code>TransactionModuleImpl</code>)
     * which in turn holds a reference to the transaction listener, that should
     * be notified.
     */
    private class TransactionNotificationThread extends Thread {

        /** Transaction listeners array */
        private Vector notificationQueue = new Vector();

        /** Notification thread exit flag. */
        private boolean finished;

        /** Wait for next transaction flag. */
        private boolean wait = true;

        /** 
         * Offset of TransactionRecord object 
         * inside notificationQueue element. 
         */
        private static final int RECORD = 0;

        /**
         * Offset of TransactionModuleImpl object
         * inside notificationQueue element. 
         */
        private static final int MODULE = 1;
        
        /**
         * This method is run when the thread starts.
         */
        public void run() {
            TransactionStore transactionStore = getTransactionStore();
            
            // listeners notification queue
            while (!finished) {
                int count = notificationQueue.size();
                
                while (count > 0) {
                    Object[] element = (Object[])notificationQueue.elementAt(0);
                    
                    // synchronized with the transaction module setListener
                    // method => if the application invokes rhe
                    // <code>TransactionModule.setListener</code> method with
                    // <code>null</code>, after the call ends, there will be
                    // no further notifications and no notification will be in
                    // progress at that time
                    synchronized (element[MODULE]) {
                        TransactionRecord record = (TransactionRecord)
                                element[RECORD];
                        TransactionListener listener = ((TransactionModuleImpl)
                                element[MODULE]).getListener();
     
                        if (listener != null) {
                            try {
                                int transactionID = record.getTransactionID();
                                // test if the record has been delivered before,
                                // because of for example 2 successive calls to 
                                // deliverMissedTransactions
                                if (!transactionStore.wasDelivered(
                                        transactionID)) {
                                    listener.processed(record);
                                    transactionStore.setDelivered(
                                            transactionID);
                                }
                            } catch (IOException e) {
                                // failed to notify or a transaction store
                                // failure
                                // ignore
                            }
                        }
                        
                        notificationQueue.removeElementAt(0);
                        --count;
                    }
                }
                
                synchronized (notificationQueue) {
                    if (wait) {
                        try {
                            notificationQueue.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    wait = true;
                }
            }
        }
        
        /**
         * Add the given transaction record and transaction module to the
         * transaction notification queue. It wakes up the transaction
         * notification thread if necessary.
         *
         * @param record the transaction record
         * @param module the transaction module
         */
        public void addTransaction(TransactionRecord record,
                TransactionModuleImpl module) {
            Object[] element = new Object[] {
                record,
                module
            };
            
            notificationQueue.addElement(element);
            continueWork();
        }
        
        /**
         * Adds the given transaction records to the transaction notification
         * queue. Each transaction record is associated with the given
         * transaction module. If the transaction notification thread is not
         * running at the time the method is executed, it is woken up by the
         * method.
         *
         * @param records an array of the transaction records
         * @param module the module associated with the records
         */
        public void addTransactions(TransactionRecord[] records,
                TransactionModuleImpl module) {
            Object[] element;
            
            for (int i = 0; i < records.length; ++i) {
                element = new Object[2];
                element[RECORD] = records[i];
                element[MODULE] = module;
                notificationQueue.addElement(element);
            }
            
            continueWork();
        }
        
        /**
         * This method makes the transaction notification thread to continue
         * notification.
         */
        public void continueWork() {
            synchronized (notificationQueue) {
                wait = false;
                notificationQueue.notify();
            }
        }
    }
    
    /**
     * Signals the transaction processing thread to continue processing of the
     * queued transactions. It means that there is at least one transaction in
     * the queue, which is not waiting for some event (an user action,
     * finishing of some payment adapter's thread, etc.).
     */
    final void continueProcessing() {
        processingThread.continueWork();
    }
    
    /**
     * Creates a new transaction from the given payment requests. It sets the
     * payment module as a transaction processor and adds the new transaction
     * to the transaction queue. Returns a generated identification number,
     * which identifies the new transaction.
     *
     * @param transactionModule the transaction module, which called the method
     * @param featureID the identifier of the feature to be paid for
     * @param featureTitle the title of the feature
     * @param featureDescription the description of the feature
     * @param payload the payload to be transfered as a part of the payment or
     *      <code>null</code> if no such payload required
     * @return the identification number of the transaction
     * @throws TransactionModuleException if there is no more space to store
     *      the new transaction
     */
    synchronized public final int addTransaction(
            TransactionModuleImpl transactionModule,
            int featureID,
            String featureTitle,
            String featureDescription,
            byte[] payload) throws TransactionModuleException {
        
        // execute the transaction processing thread if not running
        if (processingThread == null) {
            processingThread = new TransactionProcessingThread();
            processingThread.start();
        }
        
        TransactionStore transactionStore = getTransactionStore();
        
        Transaction transaction = new Transaction(this, transactionModule,
                featureID, featureTitle, featureDescription, payload);
        
        int transactionID;
        try {
            transactionID = transactionStore.reserve(
                    transactionModule.getApplicationID(), transaction);
        } catch (IOException e) {
            throw new TransactionModuleException("No more space for " +
                    "transaction records");
        }
        transaction.setTransactionID(transactionID);
        
        PaymentInfo paymentInfo = getPaymentInfo(transaction);
        synchronized (paymentInfo) {
            processingThread.addTransaction(transaction);
            
            if (paymentInfo.needsUpdate()) {
                try {
                    paymentInfo.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
                
                if (paymentInfo.needsUpdate()) {
                    throw new TransactionModuleException("The provisioning " +
                            "information needs an update");
                }
            }
        }
        
        return transactionID;
    }
    
    /**
     * Adds the given transaction record to the transaction notification queue
     * and associates it with the given payment module. The payment module holds
     * a reference to the listener, which should be notified.
     *
     * @param record the transaction record
     * @param module the transaction module
     */
    final void addTransactionForNotification(TransactionRecord record,
            TransactionModuleImpl module) {
        // execute the transaction notification thread if not running
        if (notificationThread == null) {
            notificationThread = new TransactionNotificationThread();
            notificationThread.start();
        }
        
        notificationThread.addTransaction(record, module);
    }
    
    /**
     * Adds the given transaction records to the transaction notification
     * queue and associates them with the given payment module. The payment
     * module holds a reference to the listener, which should be notified.
     *
     * @param records an array of the transaction records
     * @param module the transaction module
     */
    final void addTransactionsForNotification(TransactionRecord[] records,
            TransactionModuleImpl module) {
        // execute the transaction notification thread if not running
        if (notificationThread == null) {
            notificationThread = new TransactionNotificationThread();
            notificationThread.start();
        }
        
        notificationThread.addTransactions(records, module);
    }

    /** Pointer to utility methods class. */
    private final Utils utilities = getUtilities();

    /** 'NEVER' string */
    private final String NEVER = utilities.getString(
            Utils.PAYMENT_PROV_SEL_DLG_NEVER);
    
    /**
     * This class represents an UI for displaying and updating of payment
     * information and selecting a payment provider for the payment. The
     * payment is represented by an instance of the <code>Transaction</code>
     * class.
     */
    private class PaymentModuleUI implements CommandListener,
            ItemCommandListener, ItemStateListener {

        /** A transaction which represents the payment. */
        private Transaction transaction;

        /** Reject payment command. */
        private final Command rejectCommand = new Command(
                utilities.getString(Utils.PAYMENT_PROV_SEL_DLG_NO),
                Command.CANCEL, 1);

        /** Accept payment command. */
        private final Command acceptCommand = new Command(
                utilities.getString(Utils.PAYMENT_PROV_SEL_DLG_YES),
                Command.OK, 1);

        /** Update payment info command. */
        private final Command updateCommand = new Command(
                utilities.getString(Utils.PAYMENT_PROV_SEL_DLG_UPDATE),
                Command.ITEM, 2);

        /** Cancel payment info update command. */
        private final Command stopCommand = new Command(
                utilities.getString(Utils.PAYMENT_UPDATE_DLG_STOP),
                Command.STOP, 1);
        
        /** Provider selection form. */
        private Form providerSelectionForm;

        /** Question string max length. */
        private static final int QUESTION_LENGTH = 90;

        /** Feature description form item. */
        private StringItem featureDescriptionItem;

        /** Payment question form item. */
        private StringItem paymentQuestionItem;

        /** Provider selection choice group. */
        private ChoiceGroup providerSelectionChoice;

        /** Payment info update date item. */
        private StringItem updateDateItem;

        /** Last update stamp item. */
        private StringItem updateStampItem;

        /** Provider identifiers array. */
        private int[] providers;
        
        /** Payment info update form. */
        private Form paymentUpdateForm;

        /** Payment info update progress gauge. */
        private Gauge progressGauge;

        /** Payment info update state. */
        private int updateState = -1;

        /** Flag indicates that payment info update was canceled. */
        private boolean cancel;
        
        /**
         * Creates an instance of the <code>PaymentModuleUI</code> class.
         * It requires a transaction which represents the payment.
         *
         * @param transaction the transaction
         */
        public PaymentModuleUI(Transaction transaction) {
            this.transaction = transaction;
        }
        
        /** Displays a payment update form for the transaction. */
        public void showPaymentUpdateForm() {
            if (paymentUpdateForm == null) {
                // create the form
                paymentUpdateForm = new Form(utilities.getString(
                        Utils.PAYMENT_UPDATE_DLG_CAPTION));
                
                progressGauge = new Gauge(null, false, Gauge.INDEFINITE,
                        Gauge.CONTINUOUS_RUNNING);
                progressGauge.setPreferredSize(paymentUpdateForm.getWidth(),
                        -1);
                
                paymentUpdateForm.append(progressGauge);
                
                paymentUpdateForm.addCommand(stopCommand);
                paymentUpdateForm.setCommandListener(this);
            }
            
            updatePaymentUpdateForm();
            preemptDisplay(classSecurityToken, paymentUpdateForm);
        }
        
        /** Displays a provider selection form for the transaction. */
        public void showProviderSelectionForm() {
            if (providerSelectionForm == null) {
                // create the form
                providerSelectionForm = new Form(transaction.getFeatureTitle());
                
                featureDescriptionItem = new StringItem(
                        transaction.getFeatureDescription(), null);
                paymentQuestionItem = new StringItem(null, null);
                providerSelectionChoice = new ChoiceGroup(
                        utilities.getString(Utils.PAYMENT_PROV_SEL_DLG_PAY_BY),
                        ChoiceGroup.POPUP);
                updateDateItem = new StringItem(utilities.getString(
                        Utils.PAYMENT_PROV_SEL_DLG_UPDATE_DATE), null);
                updateStampItem = new StringItem(utilities.getString(
                        Utils.PAYMENT_PROV_SEL_DLG_UPDATE_STAMP), null);
                
                featureDescriptionItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                paymentQuestionItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                providerSelectionChoice.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                updateDateItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                updateStampItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                
                Font defaultFont = Font.getDefaultFont();
                StringItem separator;
                int separatorHeight = defaultFont.getHeight() >>> 1;
                int separatorWidth = providerSelectionForm.getWidth() >>> 1;
                
                int reserveLines = defaultFont.charWidth('M') *
                        QUESTION_LENGTH / providerSelectionForm.getWidth() + 1;
                paymentQuestionItem.setPreferredSize(-1, reserveLines *
                        defaultFont.getHeight());
                
                separator = new StringItem(null, null);
                separator.setPreferredSize(separatorWidth, separatorHeight);
                separator.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                providerSelectionForm.append(featureDescriptionItem);
                providerSelectionForm.append(separator);
                providerSelectionForm.append(paymentQuestionItem);
                providerSelectionForm.append(providerSelectionChoice);
                
                separator = new StringItem(null, null);
                separator.setPreferredSize(separatorWidth, separatorHeight);
                separator.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                providerSelectionForm.append(separator);
                providerSelectionForm.append(updateDateItem);
                providerSelectionForm.append(updateStampItem);
                
                StringItem updateItem = new StringItem(null,
                        updateCommand.getLabel(), Item.BUTTON);
                
                updateItem.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_RIGHT);
                updateItem.setDefaultCommand(updateCommand);
                updateItem.setItemCommandListener(this);
                
                separator = new StringItem(null, null);
                separator.setPreferredSize(separatorWidth, separatorHeight);
                separator.setLayout(Item.LAYOUT_NEWLINE_BEFORE |
                        Item.LAYOUT_NEWLINE_AFTER);
                providerSelectionForm.append(separator);
                providerSelectionForm.append(updateItem);
                
                // reset the layout to the left
                StringItem lastItem = new StringItem(null, null);
                lastItem.setLayout(Item.LAYOUT_LEFT);
                providerSelectionForm.append(lastItem);
                
                providerSelectionForm.addCommand(acceptCommand);
                providerSelectionForm.addCommand(rejectCommand);
                
                providerSelectionForm.setCommandListener(this);
                providerSelectionForm.setItemStateListener(this);
            }
            
            PaymentInfo paymentInfo = getPaymentInfo(transaction);
            providers = getValidProviders(paymentInfo);
            
            // fill the provider selection choice group
            int oldIndex = providerSelectionChoice.getSelectedIndex();
            providerSelectionChoice.deleteAll();
            for (int i = 0; i < providers.length; ++i) {
                ProviderInfo providerInfo = paymentInfo.getProvider(
                        providers[i]);
                
                PaymentAdapter adapter = null;
                try {
                    adapter = getAdapter(providerInfo.getAdapter(),
                            providerInfo.getConfiguration());
                } catch (PaymentException e) {
                }
                
                providerSelectionChoice.append(adapter.getDisplayName() +
                        " - " + providerInfo.getName(), null);
            }
            if (oldIndex >= providers.length) {
                oldIndex = providers.length - 1;
            }
            if (oldIndex < 0) {
                oldIndex = 0;
            }
            providerSelectionChoice.setSelectedIndex(oldIndex, true);
            
            updateProviderSelectionForm();
            preemptDisplay(classSecurityToken, providerSelectionForm);
        }
        
        /**
         * Displays an alert with the given title and message.
         *
         * @param title the title
         * @param message the message
         */
        private void displayException(String title, String message) {
            Alert a = new Alert(title, message, null, AlertType.ERROR);
            
            a.setTimeout(Alert.FOREVER);
            a.setCommandListener(this);
            
            preemptDisplay(classSecurityToken, a);
        }
        
        /**
         * Updates the user interface of the provider selection form to reflect
         * changes made by the user.
         */
        private void updateProviderSelectionForm() {
            int providerID = providers[
                    providerSelectionChoice.getSelectedIndex()];
            
            PaymentInfo paymentInfo = getPaymentInfo(transaction);
            int priceTag = paymentInfo.getPriceTagForFeature(
                    transaction.getFeatureID());
            
            updateDateItem.setText((paymentInfo.getUpdateDate() == null) ?
                NEVER : paymentInfo.getUpdateDate().toString());
            updateStampItem.setText(paymentInfo.getUpdateStamp().toString());
            
            ProviderInfo providerInfo = paymentInfo.getProvider(providerID);
            PaymentAdapter adapter = null;
            try {
                adapter = getAdapter(providerInfo.getAdapter(),
                        providerInfo.getConfiguration());
            } catch (PaymentException e) {
            }
            
            String question = adapter.getPaymentQuestion(providerInfo.getName(),
                    providerInfo.getPrice(priceTag),
                    providerInfo.getCurrency());
            
            paymentQuestionItem.setText(question);
        }
        
        /**
         * Updates the payment update form to reflect the state the payment
         * update is in.
         */
        private void updatePaymentUpdateForm() {
            int key;
            int retry = 0;
            
            if (updateState == -1) {
                progressGauge.setLabel(null);
                return;
            }
            
            if (cancel) {
                progressGauge.setLabel(utilities.getString(
                        Utils.PAYMENT_UPDATE_DLG_CANCELLING));
                return;
            }
            
            if (updateState < STATE_DOWNLOADING) {
                retry = updateState >>> RETRY_SHIFT;
                updateState &= (1 << RETRY_SHIFT) - 1;
            }
            
            switch (updateState) {
                case STATE_CONNECTING:
                    key = Utils.PAYMENT_UPDATE_DLG_CONNECTING;
                    break;
                case STATE_SENDING_REQUEST:
                    key = Utils.PAYMENT_UPDATE_DLG_SENDING;
                    break;
                case STATE_RETRY_WAITING:
                    key = Utils.PAYMENT_UPDATE_DLG_WAITING;
                    break;
                case STATE_DOWNLOADING:
                    key = Utils.PAYMENT_UPDATE_DLG_DOWNLOADING;
                    break;
                case STATE_VERIFYING:
                    key = Utils.PAYMENT_UPDATE_DLG_VERIFYING;
                    break;
                default:
                    return;
            }
            
            String message = utilities.getString(key);
            
            if (retry > 0) {
                String[] params = {
                    Integer.toString(retry),
                            Integer.toString(MAX_RETRY_COUNT)
                };
                
                message += "\n" + utilities.getString(
                        Utils.PAYMENT_UPDATE_DLG_RETRY, params);
            }
            
            progressGauge.setLabel(message);
        }

        /** Time of payment update form last update. */
        private long lastUIUpdate;
        
        /**
         * A method which is called by the payment module when the state of the
         * payment update changes.
         *
         * @param newState the new state of the payment update
         * @throws InterruptedException if the payment update has been
         *      interrupted by the user
         */
        public void notifyStateChange(int newState)
        throws InterruptedException {
            if (cancel) {
                throw new InterruptedException("stopped");
            }
            if (updateState != newState) {
                long sleepTime = lastUIUpdate + ALERT_TIMEOUT -
                        System.currentTimeMillis();
                
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                
                updateState = newState;
                updatePaymentUpdateForm();
                
                lastUIUpdate = System.currentTimeMillis();
            }
        }
        
        /**
         * Implements a response to user actions.
         *
         * @param c the executed command
         * @param d the <code>Displayable</code> on which the command has
         *      been executed
         */
        public void commandAction(Command c, Displayable d) {
            if (c == acceptCommand) {
                preemptDisplay(classSecurityToken, null);
                currentUI = null;
                
                assignTransaction(transaction, providers[
                        providerSelectionChoice.getSelectedIndex()]);
                transaction.setWaiting(false);
            } else if (c == rejectCommand) {
                preemptDisplay(classSecurityToken, null);
                currentUI = null;
                
                // reject the transaction
                transaction.setState(Transaction.REJECTED);
                transaction.setNeedsUI(false);
                transaction.setWaiting(false);
            } else if (c == Alert.DISMISS_COMMAND) {
                preemptDisplay(classSecurityToken, null);
                PaymentInfo paymentInfo = getPaymentInfo(transaction);
                if (paymentInfo.needsUpdate()) {
                    // we failed to update and the current information can't
                    // be used => fail the transaction
                    currentUI = null;
                    
                    // discard the transaction
                    transaction.setState(Transaction.DISCARDED);
                    transaction.setNeedsUI(false);
                    transaction.setWaiting(false);
                } else {
                    transaction.setState(Transaction.ENTERED);
                    transaction.setWaiting(false);
//                    showProviderSelectionForm();
                }
                
                // release the process method if waiting for an update
                synchronized (paymentInfo) {
                    paymentInfo.notifyAll();
                }
            } else if (c == stopCommand) {
                if (!cancel) {
                    cancel = true;
                    updatePaymentUpdateForm();
//                  processingThread.interrupt();
                }
            }
        }
        
        /**
         * Implements a response to user actions.
         *
         * @param c the executed command
         * @param item the item associated with the executed command
         */
        public void commandAction(Command c, Item item) {
            if (c == updateCommand) {
                preemptDisplay(classSecurityToken, null);
                updateState = -1;
                cancel = false;
                transaction.setState(Transaction.UPDATE);
                transaction.setWaiting(false);
            }
        }

        /**
         * Called when internal state of an Item has been changed by the user.
         *
         * @param item the item that was changed
         */
        public void itemStateChanged(Item item) {
            if (item == providerSelectionChoice) {
                updateProviderSelectionForm();
            }
        }
    }
    
    /**
     * Assigns the given transaction to the provider identified by its
     * identification number. It sets the transaction processor of the
     * transaction to the provider's payment adapter.
     *
     * @param transaction the transaction
     * @param providerID the provider id
     */
    protected void assignTransaction(Transaction transaction, int providerID) {
        PaymentInfo paymentInfo = getPaymentInfo(transaction);
        int priceTag = paymentInfo.getPriceTagForFeature(
                transaction.getFeatureID());
        
        ProviderInfo providerInfo = paymentInfo.getProvider(providerID);
        // get the adapter instance for the given provider
        PaymentAdapter adapter = null;
        try {
            adapter = getAdapter(providerInfo.getAdapter(),
                    providerInfo.getConfiguration());
        } catch (PaymentException e) {
        }
        
        // fill the transaction fields with the provider specific values
        transaction.setProviderName(providerInfo.getName());
        transaction.setCurrency(providerInfo.getCurrency());
        transaction.setPrice(providerInfo.getPrice(priceTag));
        transaction.setSpecificPriceInfo(
                providerInfo.getPaySpecificPriceInfo(priceTag));
        
        // === DEBUG MODE ===
        // let a subclass to handle the transaction in the debug mode, if it
        // does, don't forward the control over the transaction to the payment 
        // adapter
        if (handleTransactionDebugMode(transaction)) {
            return;
        }
        // === DEBUG MODE ===
        
        // set the adapter to be a transaction processor for the transaction
        transaction.setTransactionProcessor(adapter);
        
        // update the state of the transaction
        transaction.setState(Transaction.ASSIGNED);
    }
    
    /**
     * Returns an array of provider identifiers, which could be used to pay
     * for features.
     *
     * @param paymentInfo the payment information for the MIDlet which
     *      initiated the payment
     * @return the array of provider identifiers
     */
    protected final int[] getValidProviders(PaymentInfo paymentInfo) {
        int numProviders = paymentInfo.getNumProviders();
        int numAccepted = 0;
        boolean[] accepted = new boolean[numProviders];
        
        for (int i = 0; i < numProviders; ++i) {
            accepted[i] = false;
            
            ProviderInfo providerInfo = paymentInfo.getProvider(i);
            
            PaymentAdapter adapter = null;
            try {
                adapter = getAdapter(providerInfo.getAdapter(),
                        providerInfo.getConfiguration());
            } catch (PaymentException e) {
            }
            if (adapter == null) {
                continue;
            }
            
            accepted[i] = true;
            ++numAccepted;
        }
        
        int[] providers = new int[numAccepted];
        for (int i = 0, j = 0; i < numProviders; ++i) {
            if (accepted[i]) {
                providers[j++] = i;
            }
        }
        
        return providers;
    }

    /** Array of created payment adapters. */
    private Hashtable paymentAdapters = new Hashtable();
    
    /**
     * Indicates if the given adapter is supported by the device.
     *
     * @param name the name of the adapter
     * @return <code>true</code> if the given adapter is supported
     */
    public boolean isSupportedAdapter(String name) {
        if ("PPSMS".equals(name)) {
            return true;
        }
        return false;
    }
    
    /**
     * Creates the payment adapter for the given registered adapter name and
     * the adapter configuration string. It returns <code>null</code> if no
     * such adapter can be created.
     *
     * @param adapter the registered adapter name
     * @param configuration the adapter configuration string
     * @return the instance of the payment adapter or <code>null</code>
     * @throws PaymentException if the adapter configuration string
     *      has an invalid format
     */
    protected PaymentAdapter createAdapter(String adapter,
            String configuration) throws PaymentException {
        if ("PPSMS".equals(adapter)) {
            if (configuration.indexOf(',') != -1) {
                String mcc =
                        configuration.substring(0, configuration.indexOf(','));
                String mnc =
                        configuration.substring(configuration.indexOf(',') + 1);
                
                if ((mcc != null) && (mnc != null)) {
                    mcc = mcc.trim();
                    mnc = mnc.trim();
                    
                    try {
                        Integer.parseInt(mcc);
                        Integer.parseInt(mnc);
                    } catch (NumberFormatException nfe) {
                        throw new PaymentException(
                            PaymentException.INVALID_ADAPTER_CONFIGURATION, 
                            configuration);
                    }
                    
                    if ((mcc.length() != 3) || (mnc.length() < 2) || 
                            (mnc.length() > 3)) {
                        throw new PaymentException(
                            PaymentException.INVALID_ADAPTER_CONFIGURATION, 
                            configuration);
                    }
                    
                    // get system property
                    String MCC = System.getProperty("MCC");
                    // get jsr default property
                    if (null == MCC) {
                        MCC = System.getProperty("payment.mcc");
                    }
                    // get system property
                    String MNC = System.getProperty("MNC");
                    // get jsr default property
                    if (null == MNC) {
                        MNC = System.getProperty("payment.mnc");
                    }

                    if (mcc.equals(MCC) &&
                            mnc.equals(MNC)) {
                        return PPSMSAdapter.getInstance(configuration);
                    } else {
                        return null;
                    }
                } else {
                    throw new PaymentException(
                            PaymentException.INVALID_ADAPTER_CONFIGURATION,
                            configuration);
                }
            } else {
                throw new PaymentException(
                            PaymentException.INVALID_ADAPTER_CONFIGURATION,
                            configuration);
            }
        }
        return null;
    }
    
    /**
     * Returns the payment adapter for the given registered adapter name and
     * the adapter configuration string. It either returns an adapter created
     * before for the given combination of <code>name</code> and
     * <code>providerString</code> or creates a new instance if the old one
     * doesn't exist. It returns <code>null</code> if no adapter of the given
     * parameters can be created.
     *
     * @param name the registered adapter name
     * @param configuration the adapter configuration string
     * @return the instance of the payment adapter or <code>null</code>
     * @throws PaymentException if the adapter configuration string
     *      has an invalid format
     */
    PaymentAdapter getAdapter(String name, String configuration)
    throws PaymentException {
        String adapterLookupString = name + "#" +
                normalizeConfigurationString(configuration);
        PaymentAdapter adapter = (PaymentAdapter)paymentAdapters.get(
                adapterLookupString);
        if (adapter == null) {
            adapter = createAdapter(name, configuration);
            if (adapter != null) {
                paymentAdapters.put(adapterLookupString, adapter);
            }
        }
        return adapter;
    }
    
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
    protected abstract void preemptDisplay(SecurityToken token,
            Displayable nextDisplayable);

    /** Current payment module UI. */
    private PaymentModuleUI currentUI;
    
    /**
     * A method which is responsible for processing of transactions which are
     * not yet assigned to the provider specific adapters or they are finished
     * and need to be removed from the transaction processing queue.
     *
     * @param transaction the transaction to be processed
     * @return the processed transaction or <code>null</code> if the transaction
     *      should be removed from the transaction queue
     */
    public Transaction process(Transaction transaction) {
        PaymentInfo paymentInfo;
        boolean needsUpdate;
        
        switch (transaction.getState()) {
            case Transaction.ENTERED:
                paymentInfo = getPaymentInfo(transaction);
                needsUpdate = paymentInfo.needsUpdate();
                // === DEBUG MODE ===
                if (!needsUpdate && handleAutoRequestMode(transaction)) {
                    break;
                }
                // === DEBUG MODE ===
                currentUI = new PaymentModuleUI(transaction);
                if (needsUpdate) {
                    transaction.setState(Transaction.UPDATE);
                } else {
                    transaction.setWaiting(true);
                    currentUI.showProviderSelectionForm();
                }
                break;
                
            case Transaction.UPDATE:
                // currentUI != null
                paymentInfo = getPaymentInfo(transaction);
                
                // validate the http or https connection now (before we preempt
                // the display)
                try {
                    String name = paymentInfo.getUpdateURL();
                    int permission = name.startsWith("https") ?
                        Permissions.HTTPS : Permissions.HTTP;
                    int colon = name.indexOf(':');
                    if (colon != -1) {
                        if (colon < name.length() - 1) {
                            name = name.substring(colon + 1);
                        } else {
                            name = "";
                        }
                    }
                    transaction.getTransactionModule().checkForPermission(
                            permission, name);
                } catch (InterruptedException e) {
                    // ignore, let the download fail
                }
                
                transaction.setWaiting(true);
                currentUI.showPaymentUpdateForm();

                Exception ex = null;
                try {
                    synchronized (paymentInfo) {
                        updatePaymentInfo(paymentInfo);
                    }
                    
                    if (paymentInfo.cache()) {
                        savePaymentInfo(transaction);
                    }
                    
                    preemptDisplay(classSecurityToken, null);
                    // === DEBUG MODE ===
                    if (handleAutoRequestMode(transaction)) {
                        transaction.setWaiting(false);
                        currentUI = null;
                        break;
                    }
                    // === DEBUG MODE ===
                    currentUI.showProviderSelectionForm();
                } catch (InterruptedException e) {
                    preemptDisplay(classSecurityToken, null);
                    if (paymentInfo.needsUpdate()) {
                        currentUI = null;
                        
                        // discard the transaction
                        transaction.setState(Transaction.DISCARDED);
                        transaction.setNeedsUI(false);
                        transaction.setWaiting(false);
                    } else {
                        currentUI.showProviderSelectionForm();
                    }
                } catch (PaymentException pe) {
                    ex = pe;
                } catch (IOException ioe) {
                    ex = ioe;
                }
                if (ex != null) {
                    preemptDisplay(classSecurityToken, null);
                    currentUI.displayException(utilities.getString(
                            Utils.PAYMENT_ERROR_DLG_CAPTION),
                            getErrorMessage(ex));

                    // don't release the process method if waiting for an
                    // update yet
                    break;
                }
                
                // release the process method if waiting for an update
                synchronized (paymentInfo) {
                    paymentInfo.notifyAll();
                }
                
                break;
                
            case Transaction.REJECTED:
            case Transaction.SUCCESSFUL:
            case Transaction.FAILED:
                TransactionStore transactionStore = getTransactionStore();
                
                try {
                    // create a transaction record for the transaction and add
                    // it to the transaction notification queue
                    TransactionRecord transactionRecord =
                            transactionStore.addTransaction(transaction);
                    addTransactionForNotification(transactionRecord,
                            transaction.getTransactionModule());
                } catch (IOException e) {
                }
                
                // fall through
                
            case Transaction.DISCARDED:
                return null;
        }
        
        return transaction;
    }

    /** Number of payment update file download attempts. */
    private static final int MAX_RETRY_COUNT = 3;

    /** UI update constant. */
    private static final int RETRY_SHIFT = 2;

    /** Payment info update stage. */
    private static final int STATE_CONNECTING = 0;
    /** Payment info update stage. */
    private static final int STATE_SENDING_REQUEST = 1;
    /** Payment info update stage. */
    private static final int STATE_RETRY_WAITING = 2;
    /** Payment info update stage. */
    private static final int STATE_DOWNLOADING = 0x100;
    /** Payment info update stage. */
    private static final int STATE_VERIFYING = 0x101;
    /** Payment info update stage. */
    private static final int STATE_FINISHED = 0x200;

    /** Index of MIME type object inside content type array. */
    private static final int MIME_TYPE = 0;

    /** Index of CHARSET type object inside content type array. */
    private static final int CHARSET = 1;

    /** Maximum size of payment update file. */
    private static final int TRANSFER_CHUNK = 1024;

    /** Payment info update file MIME type. */
    private static final String UPDATE_MIME_TYPE = "text/vnd.sun.pay.provision";
    
    /**
     * Returns an error message for the given exception. It handles exceptions
     * thrown during a payment update.
     *
     * @param e the exception
     * @return the error message
     */
    private String getErrorMessage(Exception e) {
        int prefixKey = Utils.PAYMENT_ERROR_PREFIX;
        int suffixKey = Utils.PAYMENT_ERROR_SUFFIX;
        int key = -1;
        if (e instanceof SecurityException) {
            key = Utils.PAYMENT_ERROR_PERMISSIONS;
            suffixKey = -1;
        } else if (e instanceof IOException) {
            key = Utils.PAYMENT_ERROR_DOWNLOAD_FAILED;
            suffixKey = -1;
        } else if (e instanceof PaymentException) {
            PaymentException pe = (PaymentException)e;
            
            switch (pe.getReason()) {
                case PaymentException.UNSUPPORTED_PAYMENT_INFO:
                case PaymentException.UNSUPPORTED_ADAPTERS:
                case PaymentException.UNSUPPORTED_PROVIDERS:
                case PaymentException.UNSUPPORTED_URL_SCHEME:
                case PaymentException.UNSUPPORTED_UPDATE_CHARSET:
                    key = Utils.PAYMENT_ERROR_UPDATE_NOT_SUPPORTED;
                    break;
                case PaymentException.INFORMATION_NOT_YET_VALID:
                    key = Utils.PAYMENT_ERROR_UPDATE_NOT_YET_VALID;
                    break;
                case PaymentException.INFORMATION_EXPIRED:
                    key = Utils.PAYMENT_ERROR_UPDATE_EXPIRED;
                    break;
                case PaymentException.MISSING_MANDATORY_ATTRIBUTE:
                case PaymentException.INVALID_ATTRIBUTE_VALUE:
                case PaymentException.INVALID_ADAPTER_CONFIGURATION:
                case PaymentException.INVALID_PRICE_INFORMATION:
                case PaymentException.INVALID_PROPERTIES_FORMAT:
                    key = Utils.PAYMENT_ERROR_UPDATE_INVALID;
                    break;
                case PaymentException.INCOMPLETE_INFORMATION:
                    key = Utils.PAYMENT_ERROR_UPDATE_INCOMPLETE;
                    break;
                case PaymentException.UPDATE_SERVER_NOT_FOUND:
                case PaymentException.UPDATE_NOT_FOUND:
                case PaymentException.INVALID_UPDATE_URL:
                    key = Utils.PAYMENT_ERROR_UPDATE_NOT_FOUND;
                    break;
                case PaymentException.UPDATE_SERVER_BUSY:
                case PaymentException.UPDATE_REQUEST_ERROR:
                    key = Utils.PAYMENT_ERROR_CONNECTION_FAILED;
                    break;
                case PaymentException.INVALID_UPDATE_TYPE:
                    key = Utils.PAYMENT_ERROR_UPDATE_INVALID_TYPE;
                    break;
                case PaymentException.EXPIRED_PROVIDER_CERT:
                    key = Utils.PAYMENT_ERROR_CERTIFICATE_EXPIRED;
                    break;
                case PaymentException.INVALID_PROVIDER_CERT:
                    key = Utils.PAYMENT_ERROR_CERTIFICATE_INCORRECT;
                    break;
                case PaymentException.EXPIRED_CA_CERT:
                case PaymentException.NO_TRUSTED_CHAIN:
                    key = Utils.PAYMENT_ERROR_CERTIFICATE_UNTRUSTED;
                    break;
                case PaymentException.SIGNATURE_VERIFICATION_FAILED:
                    key = Utils.PAYMENT_ERROR_VERIFICATION_FAILED;
                    break;
            }
        }
        
        StringBuffer buffer = new StringBuffer(utilities.getString(prefixKey));
        
        if (key != -1) {
            buffer.append(" ");
            buffer.append(utilities.getString(key));
            if (suffixKey != -1) {
                buffer.append(" ");
                buffer.append(utilities.getString(suffixKey));
            }
        }
        
        return buffer.toString();
    }
    
    /**
     * Creates a http or https connection with the payment update server. After
     * opening the connection it sends the http request for the update file and
     * checks the reply. If everything is correct it returns the connection
     * which can be used to get the update file.
     *
     * @param url the URL of the payment update file
     * @return the opened connection
     * @throws PaymentException indicates failure
     * @throws IOException indicates failure
     * @throws InterruptedException indicates that the thread has been
     *      interrupted while waiting for the server
     */
    private HttpConnection createConnection(String url)
    throws PaymentException, IOException, InterruptedException {
        HttpConnection httpConnection = null;
        int responseCode = -1;
        
        try {
            int retry = 0;
            do {
                currentUI.notifyStateChange(STATE_CONNECTING +
                        (retry << RETRY_SHIFT));
                
                Connection connection;
                try {
                    connection = Connector.open(url);
                } catch (IllegalArgumentException e) {
                    throw new PaymentException(
                            PaymentException.INVALID_UPDATE_URL, url, null);
                } catch (ConnectionNotFoundException e) {
                    throw new PaymentException(
                            PaymentException.INVALID_UPDATE_URL, url, null);
                }
                
                if (!(connection instanceof HttpConnection)) {
                    connection.close();
                    throw new PaymentException(
                            PaymentException.INVALID_UPDATE_URL, url, null);
                }
                
                httpConnection = (HttpConnection)connection;
                
                // set User-Agent
                String prof = System.getProperty("microedition.profiles");
                int space = prof.indexOf(' ');
                if (space != -1) {
                    prof = prof.substring(0, space);
                }
                httpConnection.setRequestProperty("User-Agent", "Profile/" +
                        prof + " Configuration/" +
                        System.getProperty("microedition.configuration"));
                
                // set Accept-Charset
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8, " +
                        System.getProperty("microedition.encoding"));
                
                // set Accept-Language
                String locale = System.getProperty("microedition.locale");
                if (locale != null) {
                    httpConnection.setRequestProperty("Accept-Language",
                            locale);
                }
                
                currentUI.notifyStateChange(STATE_SENDING_REQUEST +
                        (retry << RETRY_SHIFT));
                
                try {
                    responseCode = httpConnection.getResponseCode();
                } catch (IOException e) {
                    if (httpConnection.getHost() == null) {
                        throw new PaymentException(
                                PaymentException.INVALID_UPDATE_URL, url, null);
                    }
                    
                    throw new PaymentException(
                            PaymentException.UPDATE_SERVER_NOT_FOUND,
                            url, null);
                }
                
                if ((responseCode != HttpConnection.HTTP_UNAVAILABLE) ||
                        (++retry > MAX_RETRY_COUNT)) {
                    break;
                }
                
                long sleepTime = 10000;
                
                String value = httpConnection.getHeaderField("Retry-After");
                // parse the Retry-After field
                if (value != null) {
                    try {
                        sleepTime = Integer.parseInt(value) * 1000;
                    } catch (NumberFormatException ne) {
                        // not a number
                        try {
                            sleepTime = DateParser.parse(value);
                            sleepTime -= System.currentTimeMillis();
                        } catch (IllegalArgumentException de) {
                        }
                    }
                }
                
                httpConnection.close();
                httpConnection = null;
                
                if (sleepTime < 0) {
                    sleepTime = 10000;
                } else if (sleepTime > 60000) {
                    sleepTime = 60000;
                }
                
                currentUI.notifyStateChange(STATE_RETRY_WAITING +
                        (retry << RETRY_SHIFT));
                
                Thread.sleep(sleepTime);
                
            } while (true);
            
            switch (responseCode) {
                case HttpConnection.HTTP_OK:
                    break;
                case HttpConnection.HTTP_NOT_FOUND:
                    throw new PaymentException(
                            PaymentException.UPDATE_NOT_FOUND, url, null);
                case HttpConnection.HTTP_UNAVAILABLE:
                    throw new PaymentException(
                            PaymentException.UPDATE_SERVER_BUSY, url, null);
                default:
                    throw new PaymentException(
                            PaymentException.UPDATE_REQUEST_ERROR,
                            Integer.toString(responseCode), null);
            }
            
        } catch (PaymentException e) {
            if (httpConnection != null) {
                httpConnection.close();
            }
            // rethrow
            throw e;
        } catch (IOException e) {
            if (httpConnection != null) {
                httpConnection.close();
            }
            // rethrow
            throw e;
        }
        
        return httpConnection;
    }
    
    /**
     * Updates the given payment information from the update URL. If an
     * exception is thrown during the update, the payment information is not
     * changed.
     *
     * @param paymentInfo the payment information
     * @throws PaymentException indicates failure
     * @throws IOException indicates failure
     * @throws InterruptedException if the update has been stopped by the user
     */
    private void updatePaymentInfo(PaymentInfo paymentInfo)
    throws PaymentException, IOException, InterruptedException {
        String url = paymentInfo.getUpdateURL();
        
        // 1. CONNECT TO THE SERVER AND SEND REQUEST
        HttpConnection httpConnection = createConnection(url);
        
        String[] contentType = { null, null };
        parseContentType(contentType, httpConnection.getType());
        
        // check the mime type
        if (!UPDATE_MIME_TYPE.equals(contentType[MIME_TYPE])) {
            httpConnection.close();
            throw new PaymentException(PaymentException.INVALID_UPDATE_TYPE,
                    contentType[MIME_TYPE], null);
        }
        
        // 2. DOWNLOAD THE UPDATE FILE
        byte[] data = null;
        
        InputStream is;
        try {
            currentUI.notifyStateChange(STATE_DOWNLOADING);
            
            is = httpConnection.openDataInputStream();
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream(
                        TRANSFER_CHUNK);
                byte[] buffer = new byte[TRANSFER_CHUNK];
                int read;
                while ((read = is.read(buffer, 0, TRANSFER_CHUNK)) > 0) {
                    os.write(buffer, 0, read);
                }
                
                // not necessary
                os.flush();
                
                data = os.toByteArray();
                os.close();
                
            } finally {
                is.close();
            }
        } finally {
            httpConnection.close();
        }
        
        // 3. VERIFY AND UPDATE PAYMENT INFORMATION
        currentUI.notifyStateChange(STATE_VERIFYING);
        paymentInfo.updatePaymentInfo(data, contentType[CHARSET]);
        
        currentUI.notifyStateChange(STATE_FINISHED);
    }
    
    /**
     * Returns the end index of the first substring of the given string which
     * matches the given mask or <code>-1</code> if no such substring can be
     * found. The mask is given as a character array and it has only one
     * special character '+', which represents 0..* number of space characters.
     *
     * @param string the string
     * @param mask the mask
     * @return the index or <code>-1</code>
     */
    private static int findEndOf(String string, char[] mask) {
        char[] data = string.toCharArray();
        
        int i = 0; // index into data
        int j = 0; // index into mask
        int k = 0; // saved index
        
        while ((i < data.length) && (j < mask.length)) {
            if (mask[j] == '+') {
                if (data[i] <= ' ') {
                    ++i;
                } else {
                    ++j;
                }
                continue;
            }
            if (data[i] == mask[j]) {
                ++i;
                ++j;
                continue;
            }
            i = k + 1;
            j = 0;
            k = i;
        }
        
        for (; (j < mask.length) && (mask[j] == '+'); ++j) {
        }
        
        return (j == mask.length) ? i : -1;
    }

    /** Mask for content type searching. */
    private static final char[] CHARSET_MASK = {
        ';', '+', 'c', 'h', 'a', 'r', 's', 'e', 't', '+', '='
    };
    
    /**
     * Extracts the MIME type and the character set from the given Content-Type
     * value. It returns the extracted values in the given string array. They
     * are stored under the <code>MIME_TYPE</code> and <code>CHARSET</code>
     * indexes.
     *
     * @param values the array for extracted values
     * @param contentType the Content-Type value
     */
    private static void parseContentType(String[] values, String contentType) {
        int index;
        String mimeType = null;
        String charset = "ISO-8859-1";  // the default charset
        
        // decode mimeType and charset
        if (contentType != null) {
            index = contentType.indexOf(';');
            mimeType = contentType;
            if (index != -1) {
                mimeType = contentType.substring(0, index);
                index = findEndOf(contentType, CHARSET_MASK);
                if (index != -1) {
                    int index2 = contentType.indexOf(';', index);
                    if (index2 == -1) {
                        index2 = contentType.length();
                    }
                    charset = contentType.substring(index, index2);
                    charset = charset.trim().toUpperCase();
                }
            }
            mimeType = mimeType.trim().toLowerCase();
        }
        
        values[MIME_TYPE] = mimeType;
        values[CHARSET] = charset;
    }
    
    /**
     * Normalizes the given configuration strings. It removes all white spaces
     * before and after any comma in the string.
     *
     * @param configuration the input string
     * @return the normalized string
     */
    private static String normalizeConfigurationString(String configuration) {
        StringBuffer reconstructed = new StringBuffer();
        Vector elements = Util.getCommaSeparatedValues(configuration);
        
        int count = elements.size();
        if (count > 0) {
            reconstructed.append((String)elements.elementAt(0));
            for (int i = 1; i < count; ++i) {
                reconstructed.append(",");
                reconstructed.append((String)elements.elementAt(i));
            }
        }
        
        return reconstructed.toString();
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
    protected boolean handleTransactionDebugMode(Transaction transaction) {
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
    protected boolean handleAutoRequestMode(Transaction transaction) {
        return false;
    }
    // === DEBUG MODE ===

    static {
       /* Hand out security token */
       PPSMSAdapter.initSecurityToken(classSecurityToken);
    }
}
