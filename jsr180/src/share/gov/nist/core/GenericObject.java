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
/**
 * Basic instance used as base for data elements.
 */
public abstract class GenericObject {
    
    /**
     * Makes a copy of the current instance.
     * @return copy of current object
     */
    public abstract Object clone();
    
    /**
     * Encodes current object as a text string.
     * @return encoded text string.
     */
    public abstract String encode();
    
    /**
     * Generic case insensitive string comparison utility.
     * @param s1 first string for comparison
     * @param s2 second string for comparison
     * @return true if s1 and s2 match
     */
    public static  boolean equalsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().equals(s2.toLowerCase());
    }
    
    /**
     * Generic case sensitive string comparison utility.
     * @param s1 first string for comparison
     * @param s2 second string for comparison
     * @return true if s1 and s2 match
     */
    public static int compareToIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().compareTo(s2.toLowerCase());
    }
}
