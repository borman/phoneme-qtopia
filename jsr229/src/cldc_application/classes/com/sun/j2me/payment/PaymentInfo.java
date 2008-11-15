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

import java.util.Date;
import java.util.Vector;

import java.io.Writer;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.sun.midp.util.Properties;
import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.Util;
import com.sun.midp.io.Base64;

import com.sun.midp.crypto.*;
import com.sun.midp.pki.*;
import com.sun.midp.security.*;
import com.sun.midp.publickeystore.*;

import javax.microedition.pki.CertificateException;

/**
 * This class represents the payment information read from the application 
 * Manifest file or obtained from the associated update URL.
 *
 * @version 1.11
 */
public final class PaymentInfo {
    
    /** A value indicating that the auto request mode is disabled. */
    public static final int AUTO_REQUEST_OFF = 0;
    /** A value indicating that the auto request mode is set to accept. */
    public static final int AUTO_REQUEST_ACCEPT = 1;
    /** A value indicating that the auto request mode is set to reject. */
    public static final int AUTO_REQUEST_REJECT = 2; 

    /** The version number of the JAR-Manifest fields. */
    private static final String CURRENT_VERSION = 
            System.getProperty("microedition.payment.version");
    /** Pay version attribute name. */
    private static final String PAY_VERSION = 
            "Pay-Version";
    /** Pay-adapters attribute name. */
    private static final String PAY_ADAPTERS = 
            "Pay-Adapters";
    /** Pay-Debug-DemoMode attribute name. */
    private static final String PAY_DBG_DEMOMODE = 
            "Pay-Debug-DemoMode";
    /** Pay-Debug-FailInitialize attribute name. */
    private static final String PAY_DBG_FAILINITIALIZE = 
            "Pay-Debug-FailInitialize";
    /** Pay-Debug-FailIO attribute name. */
    private static final String PAY_DBG_FAILIO =
            "Pay-Debug-FailIO";
    /** Pay-Debug-MissedTransactions attribute name. */
    private static final String PAY_DBG_MISSEDTRANSACTIONS = 
            "Pay-Debug-MissedTransactions";
    /** Pay-Debug-RandomTests attribute name. */
    private static final String PAY_DBG_RANDOMTESTS =
            "Pay-Debug-RandomTests";
    /** Pay-Debug-AutoRequestMode attribute name. */
    private static final String PAY_DBG_AUTOREQUESTMODE =
            "Pay-Debug-AutoRequestMode";
    /** Pay-Debug-NoAdapter attribute name. */
    private static final String PAY_DBG_NOADAPTER =
            "Pay-Debug-NoAdapter";
    /** Pay-Update-Date attribute name. */    
    private static final String PAY_UPDATE_DATE =
            "Pay-Update-Date";
    /** Pay-Update-Stamp attribute name. */
    private static final String PAY_UPDATE_STAMP =
            "Pay-Update-Stamp";
    /** Pay-Update-URL attribute name. */
    private static final String PAY_UPDATE_URL =
            "Pay-Update-URL";
    /** Pay-Cache attribute name. */
    private static final String PAY_CACHE =
            "Pay-Cache";
    /** Pay-Providers attribute name. */
    private static final String PAY_PROVIDERS =
            "Pay-Providers";
    /** Prefix for constructing provider specific attribute name. */
    private static final String PAY_PREFIX =
            "Pay-";
    /** Prefix for constructing feature description attribute name. */
    private static final String PAY_FEATURE_PREFIX =
            "Pay-Feature-";
    /** Suffix for constructing provider info attribute name. */
    private static final String INFO_SUFFIX =
            "-Info";
    /** 
     * Suffix for constructing price and payment specific information
     * attribute name. 
     */
    private static final String TAG =
            "-Tag-";
    /** Pay-Certificate-(n)-(m) attribute name prefix. */
    private static final String PAY_CERTIFICATE_PREFIX =
            "Pay-Certificate-";
    /** Pay-Signature-XXX-XXX attribute name prefix. */
    private static final String PAY_SIGNATURE_PREFIX =
            "Pay-Signature-";
    /** Pay-Signature-RSA-SHA1 attribute name. */
    private static final String PAY_SIGNATURE_RSA_SHA1 =
            "Pay-Signature-RSA-SHA1";
    /** PKI prefixes are used for property strip. */
    private static final char[][] PKI_PREFIXES = {
        PAY_CERTIFICATE_PREFIX.toCharArray(),
        PAY_SIGNATURE_PREFIX.toCharArray()
    };
    /** List of supported adapters. */
    private static final String[] VALID_ADAPTER_NAMES = {
        "PPSMS"
    };
    /** Pointer to "yes" string. */
    private static final String YES_VALUE = "yes";
    /** Pointer to "no" string. */
    private static final String NO_VALUE = "no";
    /** Array of options could only exist in payment attributes. */
    private static final String[] YES_NO_OPTIONS = {
        YES_VALUE,
        NO_VALUE
    };
    /** Array of options can only exist in payment attributes. */    
    private static final String[] ACCEPT_REJECT_OPTIONS = {
        "accept",
        "reject"
    };
    /** Instance of Utils class */
    private static final Utils utilities = 
            PaymentModule.getInstance().getUtilities();

    /** List of MIDlet requested adapters. */
    private String[] adapters;
    /** Pay-Debug-DemoMode attribute value. */
    private boolean dbgDemoMode;
    /** Pay-Debug-FailInitialize attribute value. */
    private boolean dbgFailInitialize;
    /** Pay-Debug-FailIO attribute value. */
    private boolean dbgFailIO;
    /** Pay-Debug-MissedTransactions attribute value. */
    private int dbgMissedTransactions;
    /** Pay-Debug-RandomTests attribute value. */
    private boolean dbgRandomTests;
    /** Pay-Debug-AutoRequestMode attribute value. */
    private int dbgAutoRequestMode;
    /** Pay-Update-Date attribute value. */
    private Date updateDate;
    /** Pay-Update-Stamp attibute value. */
    private Date updateStamp;
    /** Pay-Update-URL attribute value. */
    private String updateURL;
    /** Pay-Cache attribute value. */
    private boolean cache;
    /** Payment info expiration date. */
    private Date expirationDate;
    /** Array of features price tags. */
    private int[] featureToTag;
    /** List of MIDlet supported payment providers. */
    private ProviderInfo[] providers;   

    /** Default constructor. */
    private PaymentInfo() {
    }
    
    /**
     * Creates an instance of the <code>PaymentInfo</code> class. It reads
     * information from the provided JAD and Manifest properties.
     *
     * @param jadProperties the JAD properties
     * @param jarProperties the Manifest properties
     * @return the instance of the <code>PaymentInfo</code> class
     * @throws PaymentException if some of the properties are incorrect, 
     *      incomplete, unsupported, etc.
     */
    public static PaymentInfo createFromProperties(
            Properties jadProperties, Properties jarProperties) 
                throws PaymentException {
        PaymentInfo paymentInfo = new PaymentInfo();
        
        paymentInfo.loadFromJadProperties(jadProperties);
        paymentInfo.loadFromJarProperties(jarProperties);
        
        return paymentInfo;        
    }
    
    /**
     * Validates JAD properties.
     *
     * @param jadProperties the JAD properties
     * @throws PaymentException if some of the properties are incorrect, 
     *      incomplete, unsupported, etc.
     */
    public static void validateJadProperties(Properties jadProperties) 
            throws PaymentException {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.loadFromJadProperties(jadProperties);
    }
    
    /**
     * Validates the given payment update and if correct it updates the internal
     * state of the object accordingly.
     *
     * @param data a byte array which contains the payment update
     * @param charset the character set of the payment update
     * @throws PaymentException if the payment update is incorrect
     */
    public void updatePaymentInfo(byte[] data, String charset) 
            throws PaymentException {
        Properties props;

        InputStream bis = new ByteArrayInputStream(data);
        try {
            try {
                props = utilities.loadProperties(bis, charset);
            } finally {
                bis.close();
            }
        } catch (UnsupportedEncodingException e) {
            throw new PaymentException(
                    PaymentException.UNSUPPORTED_UPDATE_CHARSET, 
                    charset, null);
        } catch (IOException e) {
            throw new PaymentException(
                    PaymentException.INVALID_PROPERTIES_FORMAT,
                    e.getMessage());
        }
        
        // find a trusted provider certificate in one of the certification 
        // chains of the payment update
        X509Certificate trustedCertificate = findTrustedCertificate(
                props);

        // get the public key for the trusted certificate
        PublicKey publicKey;
        try {
            publicKey = trustedCertificate.getPublicKey();
        } catch (CertificateException e) {
            throw new PaymentException(
                    PaymentException.INVALID_PROVIDER_CERT, 
                    trustedCertificate.getSubject(), null);
        }
        
        // get the encoded signature
        String encodedSignature = props.getProperty(PAY_SIGNATURE_RSA_SHA1);
        if (encodedSignature == null) {
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_SIGNATURE_RSA_SHA1, null);
        }
        
        byte[] signature;
        try {
            signature = Base64.decode(encodedSignature);
        } catch (IOException e) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    PAY_SIGNATURE_RSA_SHA1, "invalid or unsupported signature");
        }
        
        // get the data for verification
        String propString;
        byte[] testData;
        
        try {
             propString = new String(data, charset);
             testData = removePKIProperties(propString).getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new PaymentException(
                    PaymentException.UNSUPPORTED_UPDATE_CHARSET, 
                    charset, null);
        }
        
        // verify the signature
        try {
            Signature sigVerifier = Signature.getInstance("SHA1withRSA");

            sigVerifier.initVerify(publicKey);
            
            sigVerifier.update(testData, 0, testData.length);
            if (!sigVerifier.verify(signature)) {
                throw new PaymentException(
                    PaymentException.SIGNATURE_VERIFICATION_FAILED);
            }
        } catch (GeneralSecurityException e) {
            throw new PaymentException(
                    PaymentException.SIGNATURE_VERIFICATION_FAILED);
        }
        
        // validate and accept new values
        loadFromJppProperties(props);
        updateDate = new Date();
    }

    /**
     * Exports the payment information into the given character output stream.
     *
     * @param os the output stream
     * @throws IOException indicates an output error
     */
    public void export(Writer os) throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        // Pay-Version: 1.0
        buffer.append(PAY_VERSION);
        buffer.append(": ");
        buffer.append(CURRENT_VERSION);
        buffer.append("\n");
        
        // Pay-Update-Date: <Date>
        if (updateDate != null) {
            buffer.append(PAY_UPDATE_DATE);
            buffer.append(": ");
            buffer.append(utilities.formatISODate(updateDate.getTime()));
            buffer.append("\n");
        }
        
        // Pay-Update-Stamp: <Date>
        buffer.append(PAY_UPDATE_STAMP);
        buffer.append(": ");
        buffer.append(utilities.formatISODate(updateStamp.getTime()));
        buffer.append("\n");
        
        // Pay-Update-URL: <UpdateURL>
        buffer.append(PAY_UPDATE_URL);
        buffer.append(": ");
        buffer.append(updateURL);
        buffer.append("\n");
        
        // Pay-Cache: [yes|no|<Expiration-Date>]
        buffer.append(PAY_CACHE);
        buffer.append(": ");
        if (expirationDate != null) {
            buffer.append(utilities.formatISODate(expirationDate.getTime()));
        } else {
            buffer.append(cache ? YES_VALUE : NO_VALUE);
        }
        buffer.append("\n");
        
        // Pay-Feature-<n>: <m>
        for (int i = 0; i < featureToTag.length; ++i) {
            buffer.append(PAY_FEATURE_PREFIX);
            buffer.append(i);
            buffer.append(": ");
            buffer.append(featureToTag[i]);
            buffer.append("\n");
        }
        
        os.write(buffer.toString());
        buffer.setLength(0);

        // Pay-Providers: <ProviderTitles>
        buffer.append(PAY_PROVIDERS);
        buffer.append(": ");
        buffer.append(providers[0].getName());
        for (int i = 1; i < providers.length; ++i) {
            buffer.append(", ");
            buffer.append(providers[i].getName());
        }
        buffer.append("\n");
        
        for (int i = 0; i < providers.length; ++i) {
            exportProvider(buffer, providers[i]);
        }
        
        os.write(buffer.toString());
    }

    /**
     * Test if the payment information can be used for payment as is or it
     * needs to be updated first from the update URL.
     *
     * @return <code>true</code> if the payment information needs to be updated
     */
    public boolean needsUpdate() {
        // 1. no cache => update
        if (!cache) {
            return true;
        }
        
        // 2. expired cache => update
        if (expirationDate != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > expirationDate.getTime()) {
                return true;
            }
        }
        
        // 3. missing tags => update
        for (int i = 0; i < providers.length; ++i) {
            if (providers[i].getNumPriceTags() == 0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns <code>true</code> if the payment information should be stored
     * for the next time.
     *
     * @return <code>true</code> if the payment information should be cached
     */
    public boolean cache() {
        return cache;
    }

    /**
     * Test for the system debug mode.
     *
     * @return <code>true</code> if the is running in the system debug mode
     */
    private native boolean isDebugMode();
    
    /**
     * Test for the debug demo mode.
     *
     * @return <code>true</code> if the debug demo mode should be activated
     */
    public boolean isDemoMode() {
        return dbgDemoMode && isDebugMode();
    }

    /**
     * Test for the debug fail initialize mode.
     *
     * @return <code>true</code> if the debug fail initialize mode should be
     *      activated
     */
    public boolean getDbgFailInitialize() {
        return dbgFailInitialize;
    }

    /**
     * Test for the debug fail IO mode.
     *
     * @return <code>true</code> if the debug fail IO mode should be activated
     */
    public boolean getDbgFailIO() {
        return dbgFailIO;
    }
    
    /**
     * Returns the number of fake missed transactions that should be generated
     * when the application starts.
     *
     * @return the number of missed transactions to generate or <code>-1</code>
     *      if this debug mode is disabled
     */
    public int getDbgMissedTransactions() {
        return dbgMissedTransactions;
    }
    
    /**
     * Test for the debug random tests mode.
     *
     * @return <code>true</code> if the debug random tests mode should be
     *      activated
     */
    public boolean getDbgRandomTests() {
        return dbgRandomTests;
    }
    
    /**
     * Returns the debug auto request mode setting.
     *
     * @return <code>AUTO_REQUEST_OFF</code> if the auto request mode is 
     *      disabled, <code>AUTO_REQUEST_ACCEPT</code> if the auto request mode
     *      is set to accept and <code>AUTO_REQUEST_REJECT</code> if it is set 
     *      to reject
     * @see #AUTO_REQUEST_OFF
     * @see #AUTO_REQUEST_ACCEPT
     * @see #AUTO_REQUEST_REJECT
     */
    public int getDbgAutoRequestMode() {
        return dbgAutoRequestMode;
    }
    
    /**
     * Returns the URL of the payment update.
     *
     * @return the update URL
     */
    public String getUpdateURL() {
        return updateURL;
    }
    
    /**
     * Returns the date of the last update or <code>null</code> if the payment
     * information has been never updated.
     *
     * @return the last update date or <code>null</code>
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /** 
     * Gets the time stamp of last update.
     *
     * @return the time stamp
     */
    public Date getUpdateStamp() {
        return updateStamp;
    }

    /** 
     * Gets the number of features the application can request the user to pay
     * for.
     *
     * @return the number of paid features
     */
    public int getNumFeatures() {
        return featureToTag.length;
    }
    
    /** 
     * Returns the price tag for the given feature id.
     *
     * @param index the feature id
     * @return the price tag
     */
    public int getPriceTagForFeature(int index) {
        return featureToTag[index];
    }
    
    /** 
     * Returns the number of providers which can be used to pay for the 
     * application features.
     *
     * @return the number of providers
     */
    public int getNumProviders() {
        return providers.length;
    }
    
    /**
     * Return the provider information for the given provider id.
     *
     * @param index the provider id
     * @return the provider information
     */
    public ProviderInfo getProvider(int index) {
        return providers[index];
    }
   
    /**
     * Returns <code>true</code> if the given vector contains duplicate values.
     *
     * @param vector the vector of strings
     * @return <code>true</code> if the vector contains duplicate values
     */
    private boolean hasDuplicates(Vector vector) {
        int lastIndex = vector.size() - 1;
        for (int i = 0; i < lastIndex; ++i) {
            if (vector.indexOf(vector.elementAt(i), i + 1) != -1) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Constructs an string array from the given vector of strings. The 
     * resulting array will contain the same strings as the vector and in the 
     * same order as appeared in the vector.
     *
     * @param vector the vector of strings
     * @return the array of strings
     */
    private String[] toStringArray(Vector vector) {
        String[] strings = new String[vector.size()];
        vector.copyInto(strings);
        
        return strings;
    }
    
    /**
     * Returns <code>true</code> if the given name is a valid adapter name.
     *
     * @param name the name to test
     * @return <code>true</code> if the name is a valid adapter name
     */
    private boolean validateAdapterName(String name) {
        if (name.startsWith("X-")) {
            return name.length() > 2;
        }
        
        for (int i = 0; i < VALID_ADAPTER_NAMES.length; ++i) {
            if (VALID_ADAPTER_NAMES[i].equals(name)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Returns <code>true</code> if the given string value represents a valid
     * currency code.
     *
     * @param name the string to test
     * @return <code>true</code> if the string is a valid currency code
     */
    private boolean validateCurrencyCode(String name) {
        if (name.length() != 3) {
            return false;
        }
        
        for (int i = 0; i < 3; ++i) {
            if ((name.charAt(i) < 'A') ||
                    (name.charAt(i) > 'Z')) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Parses an attribute which can have only one of the given predefined 
     * values. It returns the index of the attribute's value or the 
     * <code>defValue</code> if the attribute is not defined.
     *
     * @param props the properties to read the attribute from
     * @param attribute the name of the attribute
     * @param options the predefined values
     * @param defValue a value to return when the attribute is not defined
     * @return the index of a string from <code>options</code> which equals to
     *      the attribute's value or <code>defValue</code>
     * @throws PaymentException if the attribute's value doesn't match any of
     *      the predefined values
     */
    private int readOptionalSelection(Properties props,
            String attribute, String[] options, int defValue)
                throws PaymentException {
        String value = props.getProperty(attribute);
        if (value == null) {
            return defValue;
        }
        
        for (int i = 0; i < options.length; ++i) {
            if (options[i].equals(value)) {
                return i;
            }
        }
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("expecting ");
        buffer.append(options[0]);
        int i;
        for (i = 1; i < (options.length - 1); ++i) {
            buffer.append(", ");
            buffer.append(options[i]);
        }
        buffer.append(" or ");
        buffer.append(options[i]);

        throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    attribute, buffer.toString());
    }

    /**
    *   Parse and check the version number of the JAR-Manifest
    *   or JAD fields.
    * 
    *   @param payVersion version string to check
    *   @throws PaymentException if the parameter contains wrong
    *                            value or its value is greater than
    *                            the version of the Payment API
    *                            implemented in the device.
    */
    private void checkPayVersion(String payVersion) throws PaymentException{
        double curVer;
        double appVer;
        payVersion = payVersion.trim();

        // The format must be <major>.<minor>.
        // First accepted version is 1.0
        if ('1' > payVersion.charAt(0)    || 
            -1 == payVersion.indexOf('.') ||
            2 > payVersion.length() - payVersion.indexOf('.')  ) {
            // unsupported payment version
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    PAY_VERSION, null);
        }

        try {
            curVer = Float.parseFloat(CURRENT_VERSION);
            appVer = Float.parseFloat(payVersion);
        } catch ( NumberFormatException nfe ) {
            // unsupported payment version
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    PAY_VERSION, null);
        }
    
        if (curVer < appVer) {
            // unsupported payment version
            throw new PaymentException(
                    PaymentException.UNSUPPORTED_PAYMENT_INFO, 
                    PAY_VERSION, null);
        }
    }
    /**
     * Updates the payment information from the given JAD file properties. 
     * If an exception is thrown during the update the original object state 
     * remains intact.
     *
     * @param props the JAD file properties
     * @throws PaymentException if the data read are incorrect or incomplete
     */
    private void loadFromJadProperties(Properties props) 
            throws PaymentException {
        String payVersion = props.getProperty(PAY_VERSION);
        String payAdapters = props.getProperty(PAY_ADAPTERS);
     
        if (payVersion != null) {
            checkPayVersion(payVersion);
        
            if (payAdapters == null) {
                // missing PAY_ADAPTERS attribute
                throw new PaymentException(
                        PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                        PAY_ADAPTERS, null);
            }
        } else {
            if (payAdapters != null) {
                // missing PAY_VERSION attribute
                throw new PaymentException(
                        PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                        PAY_VERSION, null);
            }
        }
        
        String[] adapters = null;
        // read & validate adapters
        if (payVersion != null) {
            Vector names = Util.getCommaSeparatedValues(payAdapters);

            if (names.size() == 0) {
                throw new PaymentException(
                        PaymentException.INVALID_ATTRIBUTE_VALUE, 
                        PAY_ADAPTERS, "the value is empty");
            }
            
            // avoid duplicate names
            if (hasDuplicates(names)) {
                throw new PaymentException(
                        PaymentException.INVALID_ATTRIBUTE_VALUE, 
                        PAY_ADAPTERS, "duplicate fields in the value");
            }
            
            adapters = toStringArray(names);
            
            // validate adapter names
            for (int i = 0; i < adapters.length; ++i) {
                if (!validateAdapterName(adapters[i])) {
                    throw new PaymentException(
                            PaymentException.INVALID_ATTRIBUTE_VALUE, 
                            PAY_ADAPTERS, adapters[i] + " is not a valid " +
                                "adapter name");
                }
            }
            
            // validate supported adapters
            PaymentModule paymentModule = PaymentModule.getInstance();
            int j;
            
            for (j = 0; j < adapters.length; ++j) {
                if (paymentModule.isSupportedAdapter(adapters[j])) {
                    break;
                }
            }

            if (j == adapters.length) {
                throw new PaymentException(
                        PaymentException.UNSUPPORTED_ADAPTERS,
                        PAY_ADAPTERS, null);
            }
        }

        // read and validate Pay-Debug-* attributes
        boolean dbgDemoMode = readOptionalSelection(props, 
                PAY_DBG_DEMOMODE, YES_NO_OPTIONS, 1) != 1;
        boolean dbgFailInitialize = readOptionalSelection(props, 
                PAY_DBG_FAILINITIALIZE, YES_NO_OPTIONS, 1) != 1;
        boolean dbgFailIO = readOptionalSelection(props, 
                PAY_DBG_FAILIO, YES_NO_OPTIONS, 1) != 1;
        int dbgMissedTransactions = -1;
        boolean dbgRandomTests = readOptionalSelection(props, 
                PAY_DBG_RANDOMTESTS, YES_NO_OPTIONS, 1) != 1;
        int dbgAutoRequestMode = readOptionalSelection(props,
                PAY_DBG_AUTOREQUESTMODE, ACCEPT_REJECT_OPTIONS, -1) + 1;

        // Peyment spec 1.1
        // It is not used yet, but need for TCK passing
        boolean dbgNoAdapter = readOptionalSelection(props,
                PAY_DBG_NOADAPTER, YES_NO_OPTIONS, 1) != 1;

        String dbgMissedTransactionsStr = props.getProperty(
                PAY_DBG_MISSEDTRANSACTIONS);
        if (dbgMissedTransactionsStr != null) {
            try {
                dbgMissedTransactions = Integer.parseInt(
                        dbgMissedTransactionsStr);
            } catch (NumberFormatException e) {
            }
            
            if (dbgMissedTransactions < 0) {
                throw new PaymentException(
                        PaymentException.INVALID_ATTRIBUTE_VALUE, 
                        PAY_DBG_MISSEDTRANSACTIONS, 
                        "expecting a positive number");
            }
        }
        
        // everything is correct, let's change the object state
        this.adapters = adapters;
        this.dbgDemoMode = dbgDemoMode;
        this.dbgFailInitialize = dbgFailInitialize;
        this.dbgFailIO = dbgFailIO;
        this.dbgMissedTransactions = dbgMissedTransactions;
        this.dbgRandomTests = dbgRandomTests;
        this.dbgAutoRequestMode = dbgAutoRequestMode;
    }
    
    /**
     * Parses and returns the provider information for the given provider name
     * from the properties.
     *
     * @param props the properties to get provider from
     * @param provider the provider name
     * @return the provider information
     * @throws PaymentException if the provider information is incorrect or
     *      incomplete
     */
    private ProviderInfo loadProvider(Properties props, String provider) 
            throws PaymentException {
        String tempValue;
        String tempKey = PAY_PREFIX + provider + INFO_SUFFIX;
        
        tempValue = props.getProperty(tempKey);
        
        if (tempValue == null) {
            // missing or incorrect provider
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    tempKey, null);
        }

        int offset = 0;
        int index;
        
        index = tempValue.indexOf(',');
        if (index == -1) {
            // missing currency code
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    tempKey, "the currency code is not present");
        }

        String adapter = tempValue.substring(offset, index).trim();
        // validate adapter name
        if (!validateAdapterName(adapter)) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    tempKey, adapter + " is not a valid adapter name");
        }
        
        offset = index + 1;
        index = tempValue.indexOf(',', offset);
        
        String currency;
        if (index == -1) {
            currency = tempValue.substring(offset).trim();
        } else {
            currency = tempValue.substring(offset, index).trim();
        }
        // validate currency
        if (!validateCurrencyCode(currency)) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    tempKey, "not a valid currency code");
        }

        // get configuration
        String configuration;
        if (index == -1) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    tempKey, "the payment specific info is not present");
        }

        offset = index + 1;
        configuration = tempValue.substring(offset).trim();
        
        tempKey = PAY_PREFIX + provider + TAG;
        tempValue = props.getProperty(tempKey + 0);
        double[] prices = null;
        String[] paySpecificPriceInfo = null;
        if (tempValue != null) {
            // contains tag attributes
            int numTags = 0;
            Vector tempVector = new Vector();
            do {
                tempVector.addElement(tempValue);
                tempValue = props.getProperty(tempKey + ++numTags);
            } while (tempValue != null);

            // we know the number of tags
            prices = new double[numTags];
            paySpecificPriceInfo = new String[numTags];

            for (int i = 0; i < numTags; ++i) {
                tempValue = (String)tempVector.elementAt(i);
                
                index = tempValue.indexOf(',');
                
                // parse and validate the price
                try {
                    String tempPrice;
                    if (index == -1) {
                        tempPrice = tempValue.trim();
                    } else {
                        tempPrice = tempValue.substring(0, index).trim();
                    }
                    prices[i] = Double.parseDouble(tempPrice);
                } catch (NumberFormatException e) {
                    throw new PaymentException(
                            PaymentException.INVALID_ATTRIBUTE_VALUE, 
                            tempKey + i, "invalid price");
                }
                
                // get pay specific price info if present
                if (index != -1) {
                    paySpecificPriceInfo[i] = 
                            tempValue.substring(index + 1).trim();
                }
            }
        }
        
        // everything is correct, create the object
        return new ProviderInfo(provider, adapter, configuration, currency, 
                prices, paySpecificPriceInfo);
    }
    
    /**
     * Loads the payment information from the given Manifest properties or
     * update file properties. The <code>strict</code> indicates if the 
     * additional tests should be executed on the data read from the properties.
     * After passing these additional tests the resulting payment information 
     * can be used for payment without any further update. Should an exception
     * be thrown during the loading the object state will remain intact.
     *
     * @param props the properties
     * @param strict if <code>true</code> the requirements on the data read
     *      from the properties are harder
     * @throws PaymentException if the data read are incorrect or incomplete
     */
    private void loadFromPropertiesAux(Properties props, boolean strict)
            throws PaymentException {
        String tempValue;
        
        long currentTime = System.currentTimeMillis();
        
        tempValue = props.getProperty(PAY_VERSION);
        
        if (tempValue == null) {
            // missing PAY_VERSION attribute
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_VERSION, null);
        }

        // throws Payment exception 
        // if app pay version is greater than stack version
        checkPayVersion(tempValue);
        
        tempValue = props.getProperty(PAY_UPDATE_STAMP);
        Date updateStamp;
        
        if (tempValue == null) {
            // missing PAY_UPDATE_STAMP attribute
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_UPDATE_STAMP, null);
        }
        
        // parse and validate the date
        try {
            long millis = utilities.parseISODate(tempValue);
            if (millis > currentTime) {
                throw new PaymentException(
                        PaymentException.INFORMATION_NOT_YET_VALID);
            }
            updateStamp = new Date(millis);
        } catch (IllegalArgumentException e) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    PAY_UPDATE_STAMP, e.getMessage());
        }
        
        String updateURL = props.getProperty(PAY_UPDATE_URL);
        
        if (updateURL == null) {
            // missing PAY_UPDATE_URL attribute
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_UPDATE_URL, null);
        }
        
        // validate the URL
        try {
            HttpUrl tempURL = new HttpUrl(updateURL);
            if (!"http".equals(tempURL.scheme) && 
                    !"https".equals(tempURL.scheme)) {
                throw new PaymentException(
                        PaymentException.UNSUPPORTED_URL_SCHEME,
                        tempURL.scheme, null);
            }

        } catch (IllegalArgumentException e) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE,
                    PAY_UPDATE_URL, e.getMessage());
        }
        
        tempValue = props.getProperty(PAY_CACHE);
        boolean cache = true;
        Date expirationDate = null;
        
        // validate and parse the PAY_CACHE attribute
        if (tempValue != null) {
            if (YES_VALUE.equals(tempValue)) {
                cache = true;
            } else if (NO_VALUE.equals(tempValue)) {
                cache = false;
            } else {
                try {
                    long millis = utilities.parseISODate(tempValue);
                    if (strict && (millis < currentTime)) {
                        throw new PaymentException(
                                PaymentException.INFORMATION_EXPIRED);
                    }
                    expirationDate = new Date(millis);
                } catch (IllegalArgumentException e) {
                    throw new PaymentException( 
                            PaymentException.INVALID_ATTRIBUTE_VALUE,
                            PAY_CACHE, "expecting yes, no or a valid date");
                }
            }
        }
        
        Vector tempVector = new Vector();

        tempValue = props.getProperty(PAY_FEATURE_PREFIX + 0);

        if (tempValue == null) {
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_FEATURE_PREFIX + 0, null);
        }

        // read Pay-Feature-<n>
        int index = 0;
        tempVector.setSize(0);
        do {
            tempVector.addElement(tempValue);
            tempValue = props.getProperty(PAY_FEATURE_PREFIX + ++index);
        } while (tempValue != null);

        int maxTag = 0;
        int[] featureToTag = new int[index];
        // parse and validate the numbers
        for (int i = 0; i < index; ++i) {
            int value = -1;
            try {
                value = Integer.parseInt((String)tempVector.elementAt(i));
            } catch (NumberFormatException e) {
            }

            if (value < 0) {
                throw new PaymentException(
                        PaymentException.INVALID_ATTRIBUTE_VALUE,
                        PAY_FEATURE_PREFIX + i, "expecting a positive number");
            }
            
            if (maxTag < value) {
                maxTag = value;
            }

            featureToTag[i] = value;
        }

        tempValue = props.getProperty(PAY_PROVIDERS);
        
        if (tempValue == null) {
            // missing PAY_PROVIDERS attribute
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE, 
                    PAY_PROVIDERS, null);
        }
        
        Vector names = Util.getCommaSeparatedValues(tempValue);

        if (names.size() == 0) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    PAY_PROVIDERS, "the value is empty");
        }

        // avoid duplicate names
        if (hasDuplicates(names)) {
            throw new PaymentException(
                    PaymentException.INVALID_ATTRIBUTE_VALUE, 
                    PAY_PROVIDERS, "duplicate fields in the value");
        }
        
        // IMPL_NOTE: check provider name?
        
        ProviderInfo[] providers = new ProviderInfo[names.size()];
        int numTags = maxTag + 1;
        boolean hasSupportedProvider = false;
        PaymentModule paymentModule = PaymentModule.getInstance();
        // read and validate provider infos
        for (int i = 0; i < providers.length; ++i) {
            // read provider
            ProviderInfo provider = loadProvider(
                    props, (String)names.elementAt(i));

            if ((strict || (provider.getNumPriceTags() != 0))
                    && (provider.getNumPriceTags() < numTags)) {
                throw new PaymentException(
                        PaymentException.INCOMPLETE_INFORMATION);
            }
            
            // try to create an adapter for the provider
            PaymentAdapter adapter;
            try {
                adapter = paymentModule.getAdapter(provider.getAdapter(),
                    provider.getConfiguration());
            } catch (PaymentException e) {
                e.setParam(PAY_PREFIX + provider.getName() + INFO_SUFFIX);
                throw e;
            }
            
            if (adapter != null) {
                // adapter has been created == we support at least one payment
                // provider
                hasSupportedProvider = true;
                
                int numTags2 = provider.getNumPriceTags();
                for (int j = 0; j < numTags2; ++j) {
                    try {
                        adapter.validatePriceInfo(provider.getPrice(j),
                                provider.getPaySpecificPriceInfo(j));
                    } catch (PaymentException e) {
                        e.setParam(PAY_PREFIX + provider.getName() + TAG + j);
                        throw e;
                    }
                }
            }
            
            providers[i] = provider;
        }

        if (!hasSupportedProvider) {
            throw new PaymentException(
                    PaymentException.UNSUPPORTED_PROVIDERS,
                    PAY_PROVIDERS, null);
        }
        
        // everything is correct, let's change the object state
        this.updateStamp = updateStamp;
        this.updateURL = updateURL;
        this.cache = cache;
        this.expirationDate = expirationDate;
        this.featureToTag = featureToTag;
        this.providers = providers;
    }
    
    /**
     * Updates the payment information from the given Manifest properties. If 
     * an exception is thrown during the update the original object state 
     * remains intact.
     *
     * @param props the Manifest properties
     * @throws PaymentException if the data read are incorrect or incomplete
     */
    private void loadFromJarProperties(Properties props) 
            throws PaymentException {
        loadFromPropertiesAux(props, false);
        
        // load the update date
        String tempValue = props.getProperty(PAY_UPDATE_DATE);
        updateDate = null;
        if (tempValue != null) {
            try {
                long millis = utilities.parseISODate(tempValue);
                updateDate = new Date(millis);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    /**
     * Updates the payment information from the given update file properties. 
     * If an exception is thrown during the update the original object state 
     * remains intact.
     *
     * @param props the update file properties
     * @throws PaymentException if the data read are incorrect or incomplete
     */
    private void loadFromJppProperties(Properties props) 
            throws PaymentException {
        loadFromPropertiesAux(props, true);
    }
    
    /**
     * Exports the given provider information into the given 
     * <code>StringBuffer</code>.
     *
     * @param buffer the <code>StringBuffer</code>
     * @param provider the provider information
     */
    private void exportProvider(StringBuffer buffer, ProviderInfo provider) {
        
        // Pay-<ProviderTitle>
        String providerPrefix = PAY_PREFIX + provider.getName();
        
        // Pay-<ProviderTitle>-Info: <RegAdapter>, <ISO4217CurrencyCode>,
        //      <PaymentSpecificInformation>
        buffer.append(providerPrefix);
        buffer.append(INFO_SUFFIX);
        buffer.append(": ");
        buffer.append(provider.getAdapter());
        buffer.append(", ");
        buffer.append(provider.getCurrency());
        buffer.append(", ");
        buffer.append(provider.getConfiguration());
        buffer.append("\n");

        // Pay-<ProviderTitle>-Tag-<m>: <Price>[, 
        //      <PaymentSpecificPriceInformation>]
        int count = provider.getNumPriceTags();
        for (int i = 0; i < count; ++i) {
            buffer.append(providerPrefix);
            buffer.append(TAG);
            buffer.append(i);
            buffer.append(": ");
            buffer.append(provider.getPrice(i));
            String value = provider.getPaySpecificPriceInfo(i);
            if (value != null) {
                buffer.append(", ");
                buffer.append(value);
            }
            buffer.append("\n");
        }
    }

    /**
     * Finds a trusted provider certificate in one of the certification 
     * chains read from the given properties.
     *
     * @param props the properties
     * @return the trusted provider certificate
     * @throws PaymentException if some certification chain is incorrect or
     *      none of them can be trusted
     */
    private static X509Certificate findTrustedCertificate(Properties props)
            throws PaymentException {
        int certPath = 1;
        int certIndex = 1;
        Vector certificates = new Vector();
        String encodedCert = props.getProperty(PAY_CERTIFICATE_PREFIX + 
                certPath + "-" + certIndex);

        if (encodedCert == null) {
            throw new PaymentException(
                    PaymentException.MISSING_MANDATORY_ATTRIBUTE,
                    PAY_CERTIFICATE_PREFIX + certPath + "-" + certIndex,
                    null);
        }
        
        do {
            certificates.setSize(0);
            
            do {
                try {
                    byte[] binaryCert = Base64.decode(encodedCert);
                    certificates.addElement(X509Certificate.generateCertificate(
                            binaryCert, 0, binaryCert.length));
                } catch (IOException e) {
                    throw new PaymentException(
                        PaymentException.INVALID_ATTRIBUTE_VALUE, 
                        PAY_CERTIFICATE_PREFIX + certPath + "-" + certIndex, 
                        "invalid or unsupported certificate");
                }
                
                encodedCert = props.getProperty(PAY_CERTIFICATE_PREFIX +
                        certPath + "-" + ++certIndex);
            } while (encodedCert != null);
            
            try {
                String[] authPath = X509Certificate.verifyChain(certificates, 
                        X509Certificate.DIGITAL_SIG_KEY_USAGE,
                        X509Certificate.CODE_SIGN_EXT_KEY_USAGE,
                        WebPublicKeyStore.getTrustedKeyStore());
                String domain = Permissions.UNIDENTIFIED_DOMAIN_BINDING;
                Vector keys = WebPublicKeyStore.getTrustedKeyStore().
                     findKeys(authPath[0]);
                if (keys != null) {
                    domain = ((PublicKeyInfo)keys.elementAt(0)).getDomain();
                }

                if (!Permissions.UNIDENTIFIED_DOMAIN_BINDING.equals(domain)) {
                    // we verified the chain
                    return (X509Certificate)certificates.elementAt(0);
                }

                // try next chain
                
            } catch (CertificateException e) {
                switch (e.getReason()) {
                    case CertificateException.UNRECOGNIZED_ISSUER:
                        // try next chain
                        break;
                    case CertificateException.EXPIRED:
                    case CertificateException.NOT_YET_VALID:
                        throw new PaymentException(
                                PaymentException.EXPIRED_PROVIDER_CERT, 
                                e.getCertificate().getSubject(), null);
                    case CertificateException.ROOT_CA_EXPIRED:
                        throw new PaymentException(
                                PaymentException.EXPIRED_CA_CERT, 
                                e.getCertificate().getIssuer(), null);
                    default:
                        throw new PaymentException(
                                PaymentException.INVALID_PROVIDER_CERT, 
                                e.getCertificate().getSubject(), null);
                }
            }
            
            certIndex = 1;
            encodedCert = props.getProperty(PAY_CERTIFICATE_PREFIX +
                    ++certPath + "-" + certIndex);
        } while (encodedCert != null);
        
        throw new PaymentException(PaymentException.NO_TRUSTED_CHAIN);
    }
       
    /**
     * Strips all empty lines and lines containing any of 
     * <code>Pay-Certificate-*</code> or <code>Pay-Signature-*</code> 
     * attributes from the given string.
     *
     * @param string property containing string
     * @return the altered string
     */
    private static String removePKIProperties(String string) {
        char[] data = string.toCharArray();
        int length = data.length;
        StringBuffer buffer = new StringBuffer();
        
        int i = 0;
        int j, k;
        do {
            // skip empty lines
            for (j = i; (j < length) && (data[j] != '\n') && (data[j] <= ' ');
                ++j) {
            }
            if (j == length) {
                break;
            }
            if (data[j] == '\n') {
                i = j + 1;
                continue;
            }
            
            // find matching prefix
            int prefixIdx;
            for (prefixIdx = 0; prefixIdx < PKI_PREFIXES.length; ++prefixIdx) {
                char[] prefix = PKI_PREFIXES[prefixIdx];
                for (k = 0, j = i; (j < length) && (k < prefix.length) &&
                        (data[j] == prefix[k]); ++j, ++k) {
                }
                if (k == prefix.length) {
                    break;
                }
            }

            // find the end of the line
            for (j = i; (j < length) && (data[j] != '\n'); ++j) {
            }
            if (j < length) {
                // skip '\n'
                ++j;
            }
            
            // accept the lines that don't start with any of PKI_PREFIXES
            if (prefixIdx == PKI_PREFIXES.length) {
                buffer.append(data, i, j - i);
            }
            i = j;
        } while (i < length);
        
        return buffer.toString();
    }  
}
