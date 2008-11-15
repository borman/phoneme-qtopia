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
 * ContentLengthHeader Header (of which there can be only one in a SIPMessage).
 * <pre>
 * Format:
 * Content-Length = "Content-Length" ":" 1*DIGIT
 * For details please see RFC 2616, section 14.13
 * </pre>
 *
 *
 * IMPL_NOTE: think about removing the specific parser for ContentLengthHeader.
 */
public class ContentLengthHeader extends ParameterLessHeader {
    
    /**
     * Content length field.
     */
    protected Integer contentLength;
    
    /** Conten length header field label. */
    public static final String NAME = Header.CONTENT_LENGTH;
    
    /** Handle for class. */
    protected static Class clazz;
    
    
    static {
        clazz = new ContentLengthHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public ContentLengthHeader() {
        super(CONTENT_LENGTH);
    }
    
    /**
     * Constructor given a length.
     * @param length the initial content length
     */
    public ContentLengthHeader(int length) {
        super(CONTENT_LENGTH);
        this.contentLength = new Integer(length);
        this.headerValue = String.valueOf(contentLength.intValue());
    }
    
    /**
     * Gets the content length header field.
     * @return the content length
     */
    public int getContentLength() {
        return contentLength.intValue();
    }
    
    /**
     * Sets the content length member.
     * @param contentLength int to set
     */
    public void setContentLength(int contentLength)
            throws IllegalArgumentException {
        if (contentLength < 0)
            throw new IllegalArgumentException("parameter is <0");
        
        this.contentLength = new Integer(contentLength);
        this.headerValue = String.valueOf(contentLength);
    }
    
    /**
     * Encodes into a canonical string.
     * @return String
     */
    public String encodeBody() {
        if (contentLength == null)
            return "0";
        else
            return contentLength.toString();
    }
    
    /**
     * Copies the cirrent instance.
     * @return copy of the current object
     */
    public Object clone() {
        ContentLengthHeader retval = new ContentLengthHeader();
        
        if (contentLength != null) {
            retval.contentLength =
                    new Integer(contentLength.intValue());
        }
        
        return retval;
    }
    
    /**
     * Gets the content length header value.
     * @return the content length
     */
    public Object getValue() {
        return this.contentLength;
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
            setContentLength(val);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }
    }
}

