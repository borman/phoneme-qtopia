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

package gov.nist.siplite.header;
import gov.nist.core.*;

/**
 * RSeq Header.
 *
 * The RSeq header is used in provisional responses in order to transmit
 * them reliably. For details please see RFC 3262, section 7.1
 */
public class RSeqHeader extends ParameterLessHeader {
    
    /**
     * Sequence number field.
     */
    protected Integer seqNum;
    
    /** Sequence number header field label. */
    public static final String NAME = Header.RSEQ;
    
    /** Handle for class. */
    protected static Class clazz;
    
    
    static {
        clazz = new RSeqHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public RSeqHeader() {
        super(RSEQ);
        seqNum = new Integer(0);
    }
    
    /**
     * Constructor given a seq number.
     * @param num the initial sequence number
     */
    public RSeqHeader(int num) {
        super(RSEQ);
        seqNum = new Integer(num);
        headerValue = String.valueOf(num);
    }
    
    /**
     * Gets the sequence number header field.
     * @return the sequence number
     */
    public int getRSeqNum() {
        return seqNum.intValue();
    }
    
    /**
     * Sets the sequence number member.
     * @param num sequence number to be set
     */
    public void setRSeqNum(int num)
            throws IllegalArgumentException {
        if (num < 0)
            throw new IllegalArgumentException("parameter is <0");
        
        seqNum = new Integer(num);
        headerValue = String.valueOf(num);
    }
    
    /**
     * Encodes into a canonical string.
     * @return String
     */
    public String encodeBody() {
        if (seqNum == null)
            return "0";
        else
            return seqNum.toString();
    }
    
    /**
     * Copies the current instance.
     * @return copy of the current object
     */
    public Object clone() {
        RSeqHeader retval = new RSeqHeader();
        
        if (seqNum != null) {
            retval.seqNum =
                    new Integer(seqNum.intValue());
        }
        
        return retval;
    }
    
    /**
     * Gets the sequence number header value.
     * @return the sequence number
     */
    public Object getValue() {
        return this.seqNum;
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
            setRSeqNum(val);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
    }
}

