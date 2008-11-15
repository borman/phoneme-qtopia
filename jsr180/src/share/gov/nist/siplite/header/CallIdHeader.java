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
 * Call ID Header
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IMPL_NOTE: think about removing the specific parser for CallIdHeader.
 *
 */
public class CallIdHeader extends ParameterLessHeader {
    /** Caller ID header label. */
    public static final String NAME = Header.CALL_ID;
    
    /** Handle to class. */
    public static Class clazz;
    
    /**
     * Caller Identifier field.
     */
    protected CallIdentifier callIdentifier;
    
    
    static {
        clazz = new CallIdHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public CallIdHeader() {
        super(CALL_ID);
    }
    
    /**
     * Compares two call ids for equality.
     *
     * @param other Object to set
     * @return true if the two call ids are equals, false otherwise
     */
    public boolean equals(Object other) {
        if (! this.getClass().equals(other.getClass())) {
            return false;
        }
        CallIdHeader that = (CallIdHeader) other;
        
        return this.callIdentifier.equals(that.callIdentifier);
    }
    
    /**
     * Gets the encoded version of this id.
     *
     * @return String.
     */
    public String encode() {
        return headerName + Separators.COLON + Separators.SP +
                callIdentifier.encode() + Separators.NEWLINE;
    }
    
    /**
     * Encodes the body part of this header (leave out the hdrName).
     *
     * @return String encoded body part of the header.
     */
    public String encodeBody() {
        if (callIdentifier == null)
            return "";
        else return callIdentifier.encode();
    }
    
    /**
     * Gets the Caller Id field. This does the same thing as
     * encodeBody.
     * @return String the encoded body part of the
     */
    public String getCallId() {
        return encodeBody();
    }
    
    /**
     * Gets the call Identifer member.
     * @return CallIdentifier
     */
    public CallIdentifier getCallIdentifer() {
        return callIdentifier;
    }
    
    /**
     * Sets the CallId field
     * @param cid String to set. This is the body part of the Call-Id
     * header. It must have the form localId@host or localId.
     * @throws IllegalArgumentException if cid is null, not a token, or is
     * not a token@token.
     */
    public void setCallId(String cid) throws IllegalArgumentException {
        callIdentifier = new CallIdentifier(cid);
    }
    
    /**
     * Sets the callIdentifier member.
     * @param cid CallIdentifier to set (localId@host).
     */
    public void setCallIdentifier(CallIdentifier cid) {
        callIdentifier = cid;
    }
    
    /**
     * Clone - do a deep copy.
     * @return Object CallIdHeader
     */
    public Object clone() {
        CallIdHeader retval = new CallIdHeader();
        if (this.callIdentifier != null)
            retval.callIdentifier = (CallIdentifier)this.callIdentifier.clone();
        return retval;
    }
    
    /**
     * Gets the caller id header value.
     * @return the caller id value
     */
    public Object getValue() {
        return callIdentifier;
        
    }
    
    /**
     * Sets the header value field.
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {
        setCallId(value);
    }
}

