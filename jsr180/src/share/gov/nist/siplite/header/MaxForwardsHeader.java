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

import gov.nist.core.*;
/**
 * MaxForwards Header
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IMPL_NOTE: think about removing the specific parser for MaxForwardsHeader.
 */
public class MaxForwardsHeader extends ParameterLessHeader {
    
    /**
     * Max forwards field.
     */
    protected int maxForwards;
    /** Max forwards header field label. */
    public static final String NAME = Header.MAX_FORWARDS;
    /** Class handle. */
    public final static Class clazz;
    
    static {
        clazz = new MaxForwardsHeader().getClass();
    }
    
    
    /**
     * Default constructor.
     */
    public MaxForwardsHeader() {
        super(Header.MAX_FORWARDS, "");
    }
    
    /**
     * Gets the MaxForwards field.
     * @return the maxForwards member.
     */
    public int getMaxForwards() {
        return maxForwards;
    }
    
    /**
     * Sets the maxForwards member.
     * @param maxForwards maxForwards parameter to set
     */
    public void setMaxForwards(int maxForwards)
            throws IllegalArgumentException {
        if (maxForwards < 0 || maxForwards > 255)
            throw new IllegalArgumentException
                    ("bad max forwards value " + maxForwards);
        this.maxForwards = maxForwards;
    }
    
    /**
     * Encodes into a string.
     * @return encoded string.
     *
     */
    public String encodeBody() {
        return new Integer(maxForwards).toString();
    }
    
    /**
     * Returns true if max forwards is zero.
     * @return true if MaxForwards field reached zero.
     */
    public boolean hasReachedZero() {
        return maxForwards == 0;
    }
    
    /**
     * Decrements max forwards field one by one.
     */
    public void decrementMaxForwards() {
        if (maxForwards >= 0) maxForwards--;
    }
    
    /**
     * Gets the max forwards header field value.
     * @return the max forwards header field value
     */
    public Object getValue() {
        return new Integer(maxForwards);
    }
    
    /**
     * Sets the header value field.
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {
        int val;
                
        try {
            val = Integer.parseInt(value);
            setMaxForwards(val);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
    }
}

