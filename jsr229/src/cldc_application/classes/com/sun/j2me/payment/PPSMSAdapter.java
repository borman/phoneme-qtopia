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
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

/**
 *
 * @created    June 9, 2005
 * @version    1.4
 */

/**
 * This Premium Priced SMS (PPSMS) Adapter sends messages to a PPSMS number,
 * which defines a payment model.
 */
public class PPSMSAdapter extends PaymentAdapter {
    private static final int SMS_LENGTH = 140;
    
    /** 
     * This class has a different security domain than the MIDlet suite 
     */
    private static SecurityToken classSecurityToken;
    
    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
    }
    
    /**
     * Separate thread conducting the transaction
     */
    private class PPSMSThread extends Thread {
        private Transaction transaction;
        
        /**
         * Creates a new instance of PPSMSThread
         */
        public PPSMSThread(Transaction transaction) {
            this.transaction = transaction;
        }
        
        /**
         * Sends Premium Priced SMS to a number from adapter configuration
         */
        public void run() {
            
            byte[] payload = transaction.getPayload();
            
            Object[] specInfo = parseAndValidateSpecInfo(
                    transaction.getSpecificPriceInfo());
            
            String msisdn = (String) specInfo[0];
            String prefix = (String) specInfo[1];
            int smsCount = 1;
            
            if (specInfo.length > 2) {
                smsCount = ((Integer) specInfo[2]).intValue();
            }
            
            try {
                Message msg = null;
                MessageConnection conn =
                        (MessageConnection) Connector.open("sms://" + msisdn);
                               
                byte[] prefixBytes = prefix.getBytes("UTF-8");
                int messageLength = prefixBytes.length + 
                        (payload == null ? 0 : payload.length);
                if (messageLength > SMS_LENGTH) {
                    // IMPL_NOTE correcly process exception
                    throw new Exception("Message to be sent is too long");
                }
                byte[] message = new byte[messageLength];
                
                System.arraycopy(prefixBytes, 0, message, 0,
                        prefixBytes.length);
                
                if (payload != null) {
                    System.arraycopy(payload, 0, message, prefixBytes.length,
                            payload.length);
                }
                
                msg = (BinaryMessage)
                conn.newMessage(MessageConnection.BINARY_MESSAGE);
                
                ((BinaryMessage) msg).setPayloadData(message);
                
                for (int i = 0; i < smsCount; i++) {
                    conn.send(msg);
                }
                
                conn.close();
                transaction.setState(Transaction.SUCCESSFUL);
            } catch (Exception e) {
                // IMPL_NOTE correcly process exception
                // ErrorForm errorForm = new ErrorForm(transaction);
                // preemptDisplay(classSecurityToken, errorForm);
                
                // the transaction failed
                transaction.setState(Transaction.FAILED);
            }
            
            transaction.setWaiting(false);
        }
    }
    
    /**
     * Displays error message in case that SMS cannot be sent
     */
    private class ErrorForm extends Form implements CommandListener {
        private Transaction transaction;
        
        private final Command okCommand = new Command("Ok", Command.OK, 1);
        
        /**
         * Creates a new instance of ErrorForm
         */
        public ErrorForm(Transaction transaction) {
            super("Communication Error!");
            append("The Premium Priced SMS cannot be sent");
            this.transaction = transaction;
            addCommand(okCommand);
            setCommandListener(this);
        }
        
        public void commandAction(Command c, Displayable d) {
            preemptDisplay(classSecurityToken, null);
            
            // the transaction failed
            transaction.setState(Transaction.FAILED);
            transaction.setNeedsUI(false);
        }
    }
    
    /**
     * Creates a new instance of PPSMSAdapter
     */
    private PPSMSAdapter() {
    }
    
    /**
     * Gets a display name of this adapter.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return "Premium Priced SMS";
    }
    
    /**
     * Gets a new instance of this adapter.
     *
     * @param configuration configuration info of PPSMSAdapter
     * @return the new instance of PPSMSAdapter
     */
    public static PPSMSAdapter getInstance(String configuration) {
        return new PPSMSAdapter();
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
    public void validatePriceInfo(double price,
            String paySpecificPriceInfo) throws PaymentException {
        
        if ((paySpecificPriceInfo == null) ||
                (paySpecificPriceInfo.length() == 0) ||
                parseAndValidateSpecInfo(paySpecificPriceInfo) == null) {
            throw new PaymentException(
                    PaymentException.INVALID_PRICE_INFORMATION, 
                    paySpecificPriceInfo);
        }
              
    }
    
    /**
     * Parses and validates specific payment information from input parameter
     *
     * @param paySpecificInfo the string representing comma-separated
     * specific payment information
     * @return array of specific information if all information is valid,
     * otherwise <code>null</code>
     */
    private Object[] parseAndValidateSpecInfo(String paySpecificInfo) {
        Object[] info = null;
        int firstIndex = paySpecificInfo.indexOf(',');
        
        if (firstIndex == -1) {
            return null;
        }
        
        int lastIndex = paySpecificInfo.lastIndexOf(',');
        String msisdn = paySpecificInfo.substring(0, firstIndex).trim();
        
        // check if msisdn is in right format
        try {
            long i = msisdn.startsWith("+")
            ? Long.parseLong(msisdn.substring(1))
            : Long.parseLong(msisdn);
        } catch (NumberFormatException nfe) {
            return null;
        }
        
        String prefix = "";
        String smsCount = null;
        
        // no number of messages parameter
        if (firstIndex == lastIndex) {
            prefix = paySpecificInfo.substring(firstIndex + 1,
                    paySpecificInfo.length()).trim();
        } else {
            prefix = paySpecificInfo.substring(firstIndex + 1,
                    lastIndex).trim();
            
            smsCount = paySpecificInfo.substring(lastIndex + 1,
                    paySpecificInfo.length()).trim();
        }
        
        // check if prefix meets all conditions described in specification
        if (!prefix.equals("")) {
            int maxLength = 8;
            if (prefix.startsWith("0x") || prefix.startsWith("0X")) {
                if (prefix.length() % 2 != 0) {
                    // the heximal value must have an even length
                    return null;
                }
                try {
                    prefix = prefix.substring(2);
                    // if prefix == 0xFFFFFFFFFFFFFFFF (allowed value),
                    // Long.parseLong would throw an exception, thus the
                    // string prefix has to be divided up
                    if (prefix.length() >= maxLength) {
                       Long.parseLong(prefix.substring(0, 7), 16);
                       Long.parseLong(prefix.substring(8), 16);
                    } else {
                        Long.parseLong(prefix, 16);
                    }
                    maxLength = 16;
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
            
            if (prefix.length() > maxLength) {
                return null;
            }
        }
        
        if (smsCount == null) {
            info = new Object[2];
        } else {
            info = new Object[3];
            try {
                info[2] = Integer.valueOf(smsCount);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
        
        info[0] = msisdn;
        info[1] = prefix;
        
        return info;
    }
    
    /**
     * Processes the given transaction, updates its state and returns the same
     * transaction instance or a new one (an instance of 
     * a <code>Transaction</code> subclass), which is based on the old 
     * transaction, but adds more (adapter specific) information to it.
     *
     * @param transaction the transaction to be processed
     * @return the transaction after processing
     */
    public Transaction process(Transaction transaction) {
        
        switch (transaction.getState()) {
            case Transaction.ASSIGNED:
                Thread thread = new PPSMSThread(transaction);
                transaction.setWaiting(true);
                thread.start();
                break;
                
            default:
                return super.process(transaction);
        }
       
        return transaction;
    }
}
