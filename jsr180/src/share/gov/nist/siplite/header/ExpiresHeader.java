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
 * Expires SIP Header.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IMPL_NOTE: think about removing the specific parser for ExpiresHeader.
 *
 */
public class ExpiresHeader extends ParameterLessHeader {
    
    /**
     * Expires field.
     */
    protected Integer expires;
    
    /** Expires header field label. */
    public static final String NAME = Header.EXPIRES;
    
    /** Class handle. */
    public static Class clazz;
    
    
    static {
        clazz = new ExpiresHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public ExpiresHeader() {
        super(EXPIRES);
    }
    
    /**
     * Returns canonical form.
     * @return String
     */
    public String encodeBody() {
        return expires.toString();
    }
    
    /**
     * Gets the expires value of the ExpiresHeader. This expires value is
     *
     * relative time.
     *
     *
     *
     * @return the expires value of the ExpiresHeader.
     *
     * @since JAIN SIP v1.1
     *
     */
    public int getExpires() {
        return expires.intValue();
    }
    
    /**
     * Sets the relative expires value of the ExpiresHeader.
     * The expires value MUST be greater than zero and MUST be
     * less than 2**31.
     *
     * @param expires - the new expires value of this ExpiresHeader
     *
     * @throws InvalidArgumentException if supplied value is less than zero.
     *
     * @since JAIN SIP v1.1
     *
     */
    public void setExpires(int expires)
            throws IllegalArgumentException {
        if (expires < 0)
            throw new IllegalArgumentException("bad argument " + expires);
        this.expires = new Integer(expires);
    }
    
    /**
     * Gets the value for the header as opaque object (returned value
     * will depend upon the header. Note that this is not the same as
     * the getHeaderValue above.
     * @return the expires header field value
     */
    public Object getValue() {
        return expires;
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
            setExpires(val);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
    }
    
}
