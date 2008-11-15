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
 * FromHeader SIP Header
 * <a "href=${docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public final class FromHeader extends AddressParametersHeader {
    /** Label for tag. */
    public static final String TAG = "tag";
    /** Label for from header field. */
    public static final String NAME = Header.FROM;
    /** Class handle. */
    public static Class clazz;
    
    static {
        clazz = new FromHeader().getClass();
    }
    
    
    /**
     * Default constructor.
     */
    public FromHeader() {
        super(FROM);
    }
    
    /**
     * Generate a FROM header from a TO header.
     * @param to target receipient
     */
    public FromHeader(ToHeader to) {
        super(FROM);
        this.address = (Address) to.address.clone();
        this.parameters = (NameValueList) to.parameters.clone();
    }
    
    /**
     * Compares two from headers for equality.
     * @param otherHeader Object to set
     * @return true if the two headers are the same, false otherwise.
     */
    public boolean equals(Object otherHeader) {
        if (otherHeader == null || address == null)
            return false;
        if (!otherHeader.getClass().equals(this.getClass())) {
            return false;
        }
        
        return super.equals(otherHeader);
    }
    
    /**
     * Gets the tag parameter from the address parm list.
     * @return String
     */
    public String getTag() {
        return super.getParameter(FromHeader.TAG);
    }
    
    /**
     * Returns true if the tag label is found.
     * @return true if this header has a Tag, false otherwise.
     */
    public boolean hasTag() {
        return super.hasParameter(TAG);
        
    }
    
    /**
     * Removes the Tag field.
     */
    public void removeTag() {
        super.removeParameter(TAG);
        
    }
    
    /**
     * Sets the tag member.
     * @param tag String to set.
     */
    public void setTag(String tag) {
        super.setParameter(TAG, tag);
        
    }
}

