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
 * RAck Header.
 *
 *  The RAck header is sent in a PRACK request to support reliability of
 *  provisional responses. For details please see RFC 3262, section 7.2
 */

public class RAckHeader extends ParameterLessHeader {
    /** Class handle. */
    public static Class clazz;

    /** Sequence header. */
    public static final String NAME = Header.RACK;

    static {
        clazz = new RAckHeader().getClass();
    }

    /**
     * response number - it is actually a value of RSeq header in the reliable
     *  provisional response
     */
    protected Integer responseNum;

    /**
     * CSeq number : value of CSeq header field
     */
    protected Integer cseqNum;

    /**
     * method name
     */
    protected String method;


    /**
     * Constructor.
     */
    public RAckHeader() {
        super(RACK);
    }

    /**
     * Constructor given the sequence number and method.
     *
     * @param responseNumber is the response number to assign.
     * @param cseqNumber is the CSeq number to assign.
     * @param method is the method string.
     */
    public RAckHeader(int responseNumber, int cseqNumber, String method) {
        this();
        responseNum = new Integer(responseNumber);
        cseqNum = new Integer(cseqNumber);
        this.method = method;
    }

    /**
     * Compare two RAck headers for equality. Equality of RAck headers means
     * that the class, method, response number and cseq number are same for
     * both the headers
     * @param other Object to compare against.
     * @return true if the two RAck headers are equals, false
     * otherwise.
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass())) {
            return false;
        }
        RAckHeader that = (RAckHeader) other;
        if (! this.responseNum.equals(that.responseNum)) {
            return false;
        }
        if (! this.cseqNum.equals(that.cseqNum)) {
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
        return responseNum + Separators.SP +
                cseqNum + Separators.SP + method.toUpperCase();
    }

    /**
     * Get the method.
     * @return String the method.
     */
    public String getMethod() {
        return method.toUpperCase();
    }

    /**
     * Sets the response number of this RAckHeaderHeader.
     * @param responseNumber is the response number to be set
     */
    public void setResponseNumber(int responseNumber) {
        if (responseNumber < 0)
            throw new IllegalArgumentException
                    ("the sequence number parameter is < 0");
        responseNum = new Integer(responseNumber);
    }

    /**
     * Sets the sequence number of this RAckHeaderHeader. The sequence number
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
        cseqNum = new Integer(sequenceNumber);
    }

    /**
     * Set the method member
     *
     * @param newMethod Method to be set
     */
    public void setMethod(String newMethod) {
        if (newMethod == null)
            throw new NullPointerException("parameter is null");
        method = newMethod;
    }

    /**
     * Gets the response number of this RAckHeaderHeader.
     *
     * @return response number of the RAckHeaderHeader
     */

    public int getResponseNumber() {
        if (this.responseNum == null)
            return 0;
        else return this.responseNum.intValue();
    }

    /**
     * Gets the sequence number of this RAckHeaderHeader.
     *
     * @return sequence number of the RAckHeaderHeader
     */

    public int getSequenceNumber() {
        if (this.cseqNum == null)
            return 0;
        else return this.cseqNum.intValue();
    }

    /**
     * Copies the current instance.
     * @return copy of current object
     */
    public Object clone() {
        RAckHeader retval = new RAckHeader();

        if (this.responseNum != null)
            retval.responseNum = new Integer(this.responseNum.intValue());
        if (this.cseqNum != null)
            retval.cseqNum = new Integer(this.cseqNum.intValue());
        retval.method = this.method;

        return retval;
    }

    /**
     * Gets the header value.
     * @return the header value
     */
    public Object getValue() {
        return responseNum + Separators.SP +
               cseqNum + Separators.SP + method.toUpperCase();
    }

    /**
     * Sets the header value field.
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {
        int    newResponseNo;
        int    newSeqNo;
        String newMethod;
        String strCseqMethod;

        value = value.trim();

        // Check if the sequence number presents
        int delimIndex1 = value.indexOf(' ');
        if (delimIndex1 == -1) {
            throw new IllegalArgumentException("Invalid value");
        }

        try {
            String strResponse = value.substring(0, delimIndex1).trim();
            newResponseNo = Integer.parseInt(strResponse);
            setResponseNumber(newResponseNo);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }

        strCseqMethod = value.substring(delimIndex1, value.length()).trim();
        int delimIndex2 = strCseqMethod.indexOf(' ');
        if (delimIndex2 == -1) {
            throw new IllegalArgumentException("Invalid value");
        }

        try {
            String strCseq = strCseqMethod.substring(0, delimIndex2).trim();
            newSeqNo = Integer.parseInt(strCseq);
            setSequenceNumber(newSeqNo);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }

        newMethod = strCseqMethod.substring(delimIndex2,
            strCseqMethod.length()).trim();
        setMethod(newMethod);
    }
}
