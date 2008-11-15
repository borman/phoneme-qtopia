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
 * CSeqHeader SIP Header.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1
 *
 */

public class CSeqHeader extends ParameterLessHeader {
    /** Class handle. */
    public static Class clazz;
    
    /** Sequence header. */
    public static final String NAME = Header.CSEQ;

    static {
        clazz = new CSeqHeader().getClass();
    }

    /**
     * seqno field
     */
    protected Integer seqno;

    /**
     * method field
     */
    protected String method;


    /**
     * Constructor.
     */
    public CSeqHeader() {
        super(CSEQ);
    }

    /**
     * Constructor given the sequence number and method.
     *
     * @param seqno is the sequence number to assign.
     * @param method is the method string.
     */
    public CSeqHeader(int seqno, String method) {
        this();
        this.seqno = new Integer(seqno);
        this.method = method;
    }

    /**
     * Compare two cseq headers for equality.
     * @param other Object to compare against.
     * @return true if the two cseq headers are equals, false
     * otherwise.
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass())) {
            return false;
        }
        CSeqHeader that = (CSeqHeader) other;
        if (! this.seqno.equals(that.seqno)) {
            return false;
        }
        if (! equalsIgnoreCase(this.method, that.method)) {
            return false;
        }
        return true;
    }


    /**
     * Return canonical header content. (encoded header except headerName:)
     *
     * @return encoded string.
     */
    public String encodeBody() {
        return seqno + Separators.SP + method.toUpperCase();
    }



    /**
     * Get the method.
     * @return String the method.
     */
    public String getMethod() {
        return method.toUpperCase();
    }


    /**
     * Sets the sequence number of this CSeqHeaderHeader. The sequence number
     * MUST be expressible as a 32-bit unsigned integer and MUST be less than
     * 2**31.
     *
     * @param sequenceNumber - the sequence number to set.
     * @throws InvalidArgumentException -- if the seq number is <= 0
     */
    public void setSequenceNumber(int sequenceNumber) {
        if (sequenceNumber < 0)
            throw new IllegalArgumentException
                    ("the sequence number parameter is < 0");
        seqno = new Integer(sequenceNumber);
    }

    /**
     * Set the method member
     *
     * @param meth -- String to set
     */
    public void setMethod(String meth) {
        if (meth == null)
            throw new NullPointerException("parameter is null");
        method = meth;
    }

    /**
     * Gets the sequence number of this CSeqHeaderHeader.
     *
     * @return sequence number of the CSeqHeaderHeader
     */

    public int getSequenceNumber() {
        if (this.seqno == null)
            return 0;
        else return this.seqno.intValue();
    }

    /**
     * Copies the current instance.
     * @return copy of current object
     */
    public Object clone() {
        CSeqHeader retval = new CSeqHeader();
        
        if (this.seqno != null)
            retval.seqno = new Integer(this.seqno.intValue());
        retval.method = this.method;
        
        return retval;
    }

    /**
     * Gets the header value.
     * @return the header value
     */
    public Object getValue() {
        return seqno + Separators.SP + method.toUpperCase();
    }
    
    /**
     * Sets the header value field.
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {
        int    newSeqNo;
        String newMethod;
        
        value = value.trim();
        
        // Check if the sequence number presents
        int delimIndex = value.indexOf(' ');
        
        if (delimIndex == -1) {
            // IMPL_NOTE: set some flag to indicate that
            //       the Sequence Number is not set.
            setMethod(value.trim());
            return;
        }
        
               
        try {
            String strNum = value.substring(0, delimIndex).trim();
            newSeqNo = Integer.parseInt(strNum);
            setSequenceNumber(newSeqNo);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
        
        newMethod = value.substring(delimIndex, value.length()).trim();
        setMethod(newMethod);
    }

    /**
     * Gets the parameters.
     * @return name value list
     */
    public NameValueList getParameters() {
        return null;
    }

}

