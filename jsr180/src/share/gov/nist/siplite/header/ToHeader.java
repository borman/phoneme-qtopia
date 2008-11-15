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
/*
 */
package gov.nist.siplite.header;

import gov.nist.siplite.address.*;
import gov.nist.core.*;

/**
 * ToHeader SIP Header.
 * <a "href=${docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class ToHeader extends AddressParametersHeader {
    /** To header field label. */
    public static final String NAME = Header.TO;
    /** Tag label. */
    public static final String TAG = "tag";
    /** Class handle. */
    public static Class clazz;
    
    static {
        clazz = new ToHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public ToHeader() {
        super(TO);
    }
    /**
     * Generates a TO header from a FROM header.
     * @param from sender addres
     */
    public ToHeader(FromHeader from) {
        super(TO);
        address = (Address)from.address.clone();
        parameters = (NameValueList)from.parameters.clone();
    }
    
    /**
     * Compares two ToHeader headers for equality.
     * @param otherHeader Object to set
     * @return true if the two headers are the same.
     */
    public boolean equals(Object otherHeader) {
        try {
            if (!otherHeader.getClass().equals(this.getClass())) {
                return false;
            }
            return super.equals(otherHeader);
            
        } finally {
            // System.out.println("equals " + retval + exitpoint);
        }
    }
    
    /**
     * Encodes the header content into a String.
     * @return String
     */
    public String encodeBody() {
        String retval = "";
        retval += address.encode();
        retval += encodeWithSep();
        return retval;
    }
    
    /**
     * Gets the tag parameter from the address parm list.
     * @return tag field
     */
    public String getTag() {
        return getParameter(TAG);
    }
    
    /**
     * Returns true if tag is present.
     * @return true if the Tag exist
     */
    public boolean hasTag() {
        return hasParameter(TAG);
    }
    
    
    /**
     * Sets the tag member.
     * @param t String to set
     */
    public void setTag(String t) {
        setParameter(TAG, t);
    }
    
    /**
     * Removes the tag field.
     */
    public void removeTag() {
        removeParameter(TAG);
    }
}
