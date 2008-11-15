/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
package gov.nist.core;

import com.sun.j2me.crypto.MessageDigest;
import com.sun.j2me.crypto.NoSuchAlgorithmException;
import com.sun.j2me.crypto.DigestException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * A set of utilities that compensate for things that are missing in CLDC 1.0
 * @version 1.0
 */
public class Utils  {
    
    /** Instatce of MessageDigest */
    private static MessageDigest  messageDigest;
    
    /** Instatce loads once */
    static {
        try {
            messageDigest = new MessageDigest("MD5");
        } catch (NoSuchAlgorithmException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "Exception on MessageDigest instance creating " + ex);
            }
        }
    }
    
    /**
     * Do an MD5 Digest.
     *
     * @param digestBytes input data
     * @return MD5 hash value of the input data
     */
    public static byte[] digest(byte[] digestBytes) {
        byte[] returnValue;

        returnValue = new byte[messageDigest.getDigestLength()];
        messageDigest.update(digestBytes, 0, digestBytes.length);

        try {
            messageDigest.digest(returnValue, 0, returnValue.length);
        } catch (DigestException de) {
            // nothing to do
        }

        return returnValue;
    }
    
    
    /**
     * Generate a tag for a FROM header or TO header. Just return a
     * random 4 digit integer (should be enough to avoid any clashes!)
     * @return a string that can be used as a tag parameter.
     */
    public static String generateTag() {
        return new Long(System.currentTimeMillis()).toString();
    }
    
    /**
     * to hex converter
     */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
					  '7', '8', '9', 'a', 'b', 'c', 'd',
					  'e', 'f' };
    
    /**
     * Compares two strings lexicographically, ignoring case
     * considerations.
     * @param s1 string to compare
     * @param s2 string to compare.
     * @return 1, -1, 0 as in compare To
     */
    public static int compareToIgnoreCase(String s1, String s2) {
	// System.out.println(s1+" "+s2);
	String su1 = s1.toUpperCase();
	String su2 = s2.toUpperCase();
	return su1.compareTo(su2);
    }
    /**
     * Compares two strings lexicographically.
     * @param s1 string to compare
     * @param s2 string to compare.
     * @return 1,-1,0 as in compare To
     */
    public static  boolean equalsIgnoreCase(String s1, String s2) {
	return s1.toLowerCase().equals(s2.toLowerCase());
    }
    
    /**
     * convert an array of bytes to an hexadecimal string
     * @return a string
     * @param b bytes array to convert to a hexadecimal
     * string
     */
    
    public static String toHexString(byte b[]) {
	int pos = 0;
	char[] c = new char[b.length*2];
	for (int i = 0; i < b.length; i++) {
	    c[pos++] = toHex[(b[i] >> 4) & 0x0F];
	    c[pos++] = toHex[b[i] & 0x0f];
	}
	return new String(c);
    }
    
    /**
     * Put quotes around a string and return it.
     * @return a quoted string
     * @param str string to be quoted
     */
    public static String getQuotedString(String str) {
	return  '"' + str + '"';
    }
    
    
    /**
     * Squeeze out white space from a string and return the reduced
     * string.
     * @param input input string to sqeeze.
     * @return String a reduced string.
     */
    public static String reduceString(String input) {
	String newString = input.toLowerCase();
	int len = newString.length();
	String retval = "";
	for (int i = 0; i < len; i++) {
	    if (newString.charAt(i) == ' '
		|| newString.charAt(i) == '\t')
		continue;
	    else retval += newString.charAt(i);
	}
	return retval;
    }
}
