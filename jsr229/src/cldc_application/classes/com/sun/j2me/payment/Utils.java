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

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.sun.midp.util.Properties;

/**
 * This class provides utility methods. They are implemented in a subclass
 * of this class. An instance of it can be obtained from the payment module.
 * <p>
 * This approach allows the <code>com.sun.j2me.payment</code> package to be 
 * more independent on MIDP classes.
 *
 * @version 1.2
 */
public abstract class Utils {
    /** The starting value of the resource keys. */
    public static final int PAYMENT_ID_BASE = 0x02290000;

    // JSR229 Payment resource strings 0x02290000 - 0x02290021
    /** Resource string. never */
    public static final int PAYMENT_PROV_SEL_DLG_NEVER =
            PAYMENT_ID_BASE + 0;
    /** Resource string. No */
    public static final int PAYMENT_PROV_SEL_DLG_NO =
            PAYMENT_ID_BASE + 1;
    /** Resource string. Yes */
    public static final int PAYMENT_PROV_SEL_DLG_YES =
            PAYMENT_ID_BASE + 2;
    /** Resource string. Update */
    public static final int PAYMENT_PROV_SEL_DLG_UPDATE =
            PAYMENT_ID_BASE + 3;
    /** Resource string. Stop */
    public static final int PAYMENT_UPDATE_DLG_STOP =
            PAYMENT_ID_BASE + 4;
    /** Resource string. Updating payment information... */
    public static final int PAYMENT_UPDATE_DLG_CAPTION =
            PAYMENT_ID_BASE + 5;
    /** Resource string. Pay by */
    public static final int PAYMENT_PROV_SEL_DLG_PAY_BY =
            PAYMENT_ID_BASE + 6;
    /** Resource string. Update date: */
    public static final int PAYMENT_PROV_SEL_DLG_UPDATE_DATE =
            PAYMENT_ID_BASE + 7;
    /** Resource string. Update stamp: */
    public static final int PAYMENT_PROV_SEL_DLG_UPDATE_STAMP =
            PAYMENT_ID_BASE + 8;
    /** Resource string. Cancelling... */
    public static final int PAYMENT_UPDATE_DLG_CANCELLING =
            PAYMENT_ID_BASE + 9;
    /** Resource string. Connecting... */
    public static final int PAYMENT_UPDATE_DLG_CONNECTING =
            PAYMENT_ID_BASE + 10;
    /** Resource string. Sending request... */
    public static final int PAYMENT_UPDATE_DLG_SENDING =
            PAYMENT_ID_BASE + 11;
    /** Resource string. Waiting for server... */
    public static final int PAYMENT_UPDATE_DLG_WAITING =
            PAYMENT_ID_BASE + 12;
    /** Resource string. Downloading... */
    public static final int PAYMENT_UPDATE_DLG_DOWNLOADING =
            PAYMENT_ID_BASE + 13;
    /** Resource string. Verifying... */
    public static final int PAYMENT_UPDATE_DLG_VERIFYING =
            PAYMENT_ID_BASE + 14;
    /** Resource string. (Retry %1/%2) */
    public static final int PAYMENT_UPDATE_DLG_RETRY =
            PAYMENT_ID_BASE + 15;
    /** Resource string. Update Error */
    public static final int PAYMENT_ERROR_DLG_CAPTION =
            PAYMENT_ID_BASE + 16;
    /** Resource string. Can not update. */
    public static final int PAYMENT_ERROR_PREFIX =
            PAYMENT_ID_BASE + 17;
    /** 
     * Resource string. 
     * Contact your application provider to correct this situation. 
     */
    public static final int PAYMENT_ERROR_SUFFIX = 
            PAYMENT_ID_BASE + 18;
    /** 
     * Resource string. 
     * The application has not got permission to download the update file from 
     * the server.
     */
    public static final int PAYMENT_ERROR_PERMISSIONS =
            PAYMENT_ID_BASE + 19;
    /** Resource string. Failed to download the update file from the server. */
    public static final int PAYMENT_ERROR_DOWNLOAD_FAILED =
            PAYMENT_ID_BASE + 20;
    /** 
     * Resource string. 
     * The payment information on the server is not supported. 
     */
    public static final int PAYMENT_ERROR_UPDATE_NOT_SUPPORTED =
            PAYMENT_ID_BASE + 21;
    /** 
     * Resource string. 
     * The payment information on the server is not yet valid. 
     */
    public static final int PAYMENT_ERROR_UPDATE_NOT_YET_VALID =
            PAYMENT_ID_BASE + 22;
    /** Resource string. The payment information on the server is expired. */
    public static final int PAYMENT_ERROR_UPDATE_EXPIRED =
            PAYMENT_ID_BASE + 23;
    /** Resource string. The payment information on the server is invalid. */
    public static final int PAYMENT_ERROR_UPDATE_INVALID =
            PAYMENT_ID_BASE + 24;
    /** Resource string. The payment information on the server is incomplete. */
    public static final int PAYMENT_ERROR_UPDATE_INCOMPLETE =
            PAYMENT_ID_BASE + 25;
    /** 
     * Resource string. 
     * The payment update file has not been found at its URL. 
     */
    public static final int PAYMENT_ERROR_UPDATE_NOT_FOUND =
            PAYMENT_ID_BASE + 26;
    /** Resource string. Connection with the server failed. */
    public static final int PAYMENT_ERROR_CONNECTION_FAILED =
            PAYMENT_ID_BASE + 27;
    /** 
     * Resource string. 
     * The payment information on the server is of an invalid type. 
     */
    public static final int PAYMENT_ERROR_UPDATE_INVALID_TYPE =
            PAYMENT_ID_BASE + 28;
    /** 
     * Resource string. 
     * The certificate used to sign the update file on the server is expired or
     * not yet valid.
     */
    public static final int PAYMENT_ERROR_CERTIFICATE_EXPIRED =
            PAYMENT_ID_BASE + 29;
    /** 
     * Resource string. 
     * The certificate used to sign the update file on the server is incorrect 
     * or unsupported. 
     */
    public static final int PAYMENT_ERROR_CERTIFICATE_INCORRECT =
            PAYMENT_ID_BASE + 30;
    /** 
     * Resource string. 
     * The certificate used to sign the update file on the server is not 
     * trusted. 
     */
    public static final int PAYMENT_ERROR_CERTIFICATE_UNTRUSTED =
            PAYMENT_ID_BASE + 31;
    /** 
     * Resource string. 
     * The verification of the update file signature failed. 
     */
    public static final int PAYMENT_ERROR_VERIFICATION_FAILED =
            PAYMENT_ID_BASE + 32;
    /**
     * Resource string.
     * Are you sure, you want to buy this feature for %1 %2.%3 using %4 
     * provided by %5?
     */
    public static final int PAYMENT_UPDATE_DLG_QUESTION =
            PAYMENT_ID_BASE + 33;

    /** Creates a new instance of <code>Utils</code>. */
    protected Utils() {
    }

    /**
     * Parses properties from the given input stream and returns them as an
     * instance of the <code>Properties</code> class. The <code>charset</code>
     * parameter contains the actual character encoding of the input stream.
     *
     * @param is the input stream which contains properties
     * @param charset the character set of the input stream
     * @return the properties read from the stream
     * @throws UnsupportedEncodingException if <code>charset</code> is not 
     *      supported by the implementation
     * @throws IOException if the input stream doesn't have properties in the
     *      correct format
     */
    public abstract Properties loadProperties(InputStream is, String charset) 
            throws UnsupportedEncodingException, IOException;

    /**
     * Returns a resource string for the given key.
     *
     * @param key the key
     * @return the string assigned to the key
     */
    public abstract String getString(int key);
    
    /**
     * Returns a modified resource string for the given key. It replaces any
     * occurence of %1, %2, ... in the original resource string with the first, 
     * second, ... string from <code>values</code>.
     *
     * @param key the key
     * @param values the replacement strings
     * @return the modified resource string
     */
    public abstract String getString(int key, String[] values);
    
    /**
     * Parses a date string according to the ISO 8601 standard.
     *
     * @param date the date string in the format YYYY-MM-DDTHH:MM[:SS][[+|-]
     *      HH[MM]]
     * @return the number of milliseconds elapsed since 1970-1-1 GMT to this
     *      date
     * @throws IllegalArgumentException if the format of the date string is
     *      incorrect or the date is invalid
     */
    public abstract long parseISODate(String date);
    
    /**
     * Converts the given date to the string formatted according to the 
     * ISO 8601 standard.
     *
     * @param date the date as the number of milliseconds elapsed since 
     *      1970-1-1 GMT
     * @return the date string
     */
    public abstract String formatISODate(long date);
}
