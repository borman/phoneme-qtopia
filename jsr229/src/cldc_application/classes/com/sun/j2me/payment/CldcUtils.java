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

import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

import com.sun.midp.util.Properties;
import com.sun.midp.util.DateParser;

import com.sun.midp.installer.JadProperties;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

/**
 * This class implements utility methods used in the 
 * <code>com.sun.j2me.payment</code> package.
 *
 * @version 
 */
public class CldcUtils extends Utils {

    /** The offset value between Utils and ResourceConstants resource keys. */
    public static final int PAYMENT_ID_OFFSET = 
            Utils.PAYMENT_PROV_SEL_DLG_NEVER - 
            ResourceConstants.PAYMENT_PROV_SEL_DLG_NEVER;

    /** Creates a new instance of <code>CldcUtils</code>. */
    public CldcUtils() {
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
    public Properties loadProperties(InputStream is, String charset)
            throws UnsupportedEncodingException, IOException {
        JadProperties props = new JadProperties();
        props.load(is, charset);
        
        return props;
    }
    
    /**
     * Returns a resource string for the given key.
     *
     * @param key the key
     * @return the string assigned to the key
     */
    public String getString(int key) {
        if (key >= PAYMENT_ID_OFFSET) {
            key -= PAYMENT_ID_OFFSET;
        }
        return Resource.getString(key);
    }
    
    /**
     * Returns a modified resource string for the given key. It replaces any
     * occurence of %1, %2, ... in the original resource string with the first, 
     * second, ... string from <code>values</code>.
     *
     * @param key the key
     * @param values the replacement strings
     * @return the modified resource string
     */
    public String getString(int key, String[] values) {
        if (key >= PAYMENT_ID_OFFSET) {
            key -= PAYMENT_ID_OFFSET;
        }
        return Resource.getString(key, values);
    }
    
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
    public long parseISODate(String date) {
        return DateParser.parseISO(date);
    }

    /**
     * Converts the given date to the string formatted according to the 
     * ISO 8601 standard.
     *
     * @param date the date as the number of milliseconds elapsed since 
     *      1970-1-1 GMT
     * @return the date string
     */
    public String formatISODate(long date)
    {
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        Calendar gmtCalendar = Calendar.getInstance(gmtTimeZone);
        
        gmtCalendar.setTime(new Date(date));
        
        StringBuffer buffer = new StringBuffer();
        char[] temp = new char[4];
        
        formatNumber(temp, gmtCalendar.get(Calendar.YEAR), 4);
        buffer.append(temp, 0, 4);
        buffer.append('-');
        formatNumber(temp, gmtCalendar.get(Calendar.MONTH) + 1, 2);
        buffer.append(temp, 0, 2);
        buffer.append('-');
        formatNumber(temp, gmtCalendar.get(Calendar.DAY_OF_MONTH), 2);
        buffer.append(temp, 0, 2);
        buffer.append(' ');

        formatNumber(temp, gmtCalendar.get(Calendar.HOUR_OF_DAY), 2);
        buffer.append(temp, 0, 2);
        buffer.append(':');
        formatNumber(temp, gmtCalendar.get(Calendar.MINUTE), 2);
        buffer.append(temp, 0, 2);
        buffer.append(':');
        formatNumber(temp, gmtCalendar.get(Calendar.SECOND), 2);
        buffer.append(temp, 0, 2);

        buffer.append('Z');
        
        return buffer.toString();
    }
    
    private void formatNumber(char[] dest, int number, int digits) {
        int index = digits;
        while (index > 0) {
            dest[--index] = (char)((number % 10) + '0');
            number = number / 10;
        }
    }    
    
}
