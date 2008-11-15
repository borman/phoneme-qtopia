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

package com.sun.j2me.global;

/**
 *  This class provides general helper methods related with locale manipulation.
 */
public class LocaleHelpers {

    /**
     * Checks whether given locale is valid or not according to
     * JSR 238 specification, i.e. is in form of
     * &lt;language&gt;-&lt;country&gt;-&lt;variant&gt;.
     * Restrictions apply to length and
     * validity of characters of language part, country part and separators.
     * No checking of variant is performed.
     *
     * @param locale  locale code
     * @return <code>true</code> if locale is valid, 
     *         <code>false</code> otherwise
     */
    public static boolean isValidLocale(String locale) {
        if (locale == null) {
            // Null locale is acceptable according to MIDP 2.0.
            return true;
        }

        int len = locale.length();
	if (len == 0) {
	    return true;
	}
        if (len < 2) {
            // Language part, if present, must be 2 characters long.
            return false;
        }

        char ch = locale.charAt(0);
        if (ch < 'a' || ch > 'z') {
            return false;
        }
        ch = locale.charAt(1);
        if (ch < 'a' || ch > 'z') {
            return false;
        }

        if (len > 2) {
            if (len < 5 || len == 6) {
                // Country part, if present, must be 2 characters long.
                return false;
            }

            ch = locale.charAt(2);
            if (ch != '-' && ch != '_') {
                return false;
            }
            ch = locale.charAt(3);
            if (ch < 'A' || ch > 'Z') {
                return false;
            }
            ch = locale.charAt(4);
            if (ch < 'A' || ch > 'Z') {
                return false;
            }

            if (len > 6) {
                ch = locale.charAt(5);
                if (ch != '-' && ch != '_') {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Replaces characters (expected dashes or underscores) on appropriate
     * positions of locale code by dashes. The resulting locale name is likely
     * to conform with MIDP 2.0 specification.
     *
     * @param locale  locale code
     * @return corrected locale code, or <code>null</code> in case of invalid
     *      format of given locale
     */
    public static String normalizeLocale(String locale) {
        if (locale == null) {
            return null;
        }

        int len = locale.length();
        if (len > 3) {
            locale = locale.substring(0, 2) + '-' + locale.substring(3);
            if (len > 6) {
                locale = locale.substring(0, 5) + '-' + locale.substring(6);
            }
        }

        return locale;
    }

    /**
     * Returns parent for the given locale. Parent locale is obtained by removing
     * the last component of locale code, which is in form:
     * &lt;language&gt;-&lt;country&gt;-&lt;variant&gt;. Parent locale for
     * &lt;language&gt; is empty string.
     *
     * @param locale  the locale to obtain parent of.
     * @return parent locale or <code>null</code> if locale doesn't have
     *      parent (if locale is already <code>null</code> or empty string.
     */
    public static String getParentLocale(String locale) {

        if (locale == null) {
            return null;
        }
        int len = locale.length();
        if (len == 0) {
            return null;
        }

        if (len > 6) {
            return locale.substring(0, 5);
        }
        if (len > 3) {
            return locale.substring(0, 2);
        }
        return "";
    }

    /**
     * Get an index of the given locale in array of locales returned from any
     * of <code>getSupportedLocales</code> methods (e.g.
     * {@link javax.microedition.global.ResourceManager#getSupportedLocales}).
     *
     * @param  locale   the locale to search for
     * @param  locales  array of locales to search in
     * @return locale index in the given array, or <code>-1</code> if the
     *      array doesn't contain the specified locale
     */
    public static int indexInSupportedLocales(String locale, 
                                            String[] locales) {

        if (locale == null || locales == null || locales.length == 0) {
            return -1;
        }

        for (int i = 0; i < locales.length; i++) {
            if (locale.equals(locales[i])) {
                return i;
            }
        }

        return -1;
    }

    private static String extractElement(String input) {
        String trimmed = input.trim();
        int length = trimmed.length();
        
        if ((length >= 2) && (trimmed.charAt(0) == '"') && 
                (trimmed.charAt(length - 1) == '"')) {
            return trimmed.substring(1, length - 1);
        }
        
        return trimmed;
    }

    /**
     * Splits the given string into parts according to the given separator. It
     * stops parsing the string after the first <code>limit</code> parts are
     * extracted. If <code>limit</code> equals <code>-1</code> all parts are
     * extracted.
     *
     * @param input the input string
     * @param separator the separator
     * @param limit the maximum number of parts or <code>-1</code> if no maximum
     *      is given
     * @return the extracted parts as an array of strings
     */ 
    public static String[] splitString(String input, String separator, 
            int limit) {
        int index = input.indexOf(separator);
        int count = 1;
        
        // get the count of elements
        while (index >= 0) {
            ++count;
            index = input.indexOf(separator, index + 1);
        }
        
        if ((limit != -1) && (count > limit)) {
            count = limit;
        }
        
        // create array for elements
        String[] elements = new String[count];
        splitString(elements, input, separator, limit);
        
        return elements;
    }
    
    /**
     * Splits the given string into parts according to the given separator. It
     * stops parsing the string after the first <code>limit</code> parts are
     * extracted. If <code>limit</code> equals <code>-1</code> all parts are
     * extracted.
     *
     * @param elements an array of strings where to put the results to
     * @param input the input string
     * @param separator the separator
     * @param limit the maximum number of parts or <code>-1</code> if no maximum
     *      is given
     * @return the number of extracted parts
     */ 
    public static int splitString(String[] elements, String input, 
            String separator, int limit) {
        int lastIndex = -1;
        int index = input.indexOf(separator);
        int count = 0;

        if ((limit == -1) || (limit > elements.length)) {
            limit = elements.length;
        }
        
        if (limit == 0) {
            return 0;
        }
        
        while (index >= 0) {
            elements[count++] = extractElement(input.substring(lastIndex + 1,
                    index));
            if (count == limit) {
                return count;
            }
            lastIndex = index;
            index = input.indexOf(separator, lastIndex + 1);
        }

        elements[count++] = extractElement(input.substring(lastIndex + 1,
                input.length()));
        
        return count;
    }

    /**
     * Creates an array of <code>short</code> values from a given
     * <code>byte</code> array. Every two sequential one-byte values are
     * packed into one <code>short</code> value. <p>
     * Caution: initial array must have even length.
     *
     * @param source  original array of bytes
     * @return new array of shorts
     */
    public static short[] byteArrayToShortArray(byte[] source) {
        short[] output = new short[source.length >> 1];
        for (int i = 0, j = 0; i < source.length; i += 2, ++j) {
            output[j] = (short)(((source[i] & 0xff) << 8) | 
                    source[i + 1] & 0xff);
        }
        return output;
    }
    
    /**
     * Creates an array of <code>int</code> values from a given
     * <code>byte</code> array. Every four sequential one-byte values are
     * packed into one <code>int</code> value. <p>
     * Caution: initial array's length must be a multiple of 4.
     *
     * @param source  original array of bytes
     * @return new array of ints
     */
    public static int[] byteArrayToIntArray(byte[] source) {
        int[] output = new int[source.length >> 2];
        for (int i = 0, j = 0; i < source.length; i += 4, ++j) {
            output[j] = ((source[i] & 0xff) << 24) | 
                    ((source[i + 1] & 0xff) << 16) | 
                    ((source[i + 2] & 0xff) << 8) | 
                    source[i + 3] & 0xff;
        }
        return output;
    }
}
