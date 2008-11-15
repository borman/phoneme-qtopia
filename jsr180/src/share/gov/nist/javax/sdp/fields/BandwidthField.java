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
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import gov.nist.javax.sdp.*;

/**
 * Bandwidth field of a SDP header.
 *
 * @version  JSR141-PUBLIC-REVIEW (Subject to change)
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class BandwidthField extends SDPField {
    /** Bandwidth type. */
    protected String bwtype;
    /** Current bandwidth. */
    protected int bandwidth;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	BandwidthField bf  = new BandwidthField();
	bf.bwtype = bwtype;
	bf.bandwidth = bandwidth;
	return bf;
    }

    /** Default constructor. */
    public BandwidthField() {
	super(SDPFieldNames.BANDWIDTH_FIELD);
    }

    /**
     * Gets the current bandwidth type.
     * @return the bandwidth type
     */
    public String getBwtype() { 
	return bwtype;
    }

    /** 
     * Gets the current bandwith.
     * @return the bandwidth
     */ 
    public int getBandwidth() {
	return bandwidth;
    }
 
    /**
     * Sets the bandwidth type member.
     * @param b the new value for band width type
     */
    public void setBwtype(String b) {
	bwtype = b;
    }
    
    /**
     * Sets the bandwidth member.
     * @param b the new value for bandwidth
     */
    public void setBandwidth(int b) {
	bandwidth = b;
    } 

    /**
     * Gets the string encoded version of this object.
     * @return the encoded string contents 
     * @since v1.0
     */
    public String encode() {
	String encoded_string = BANDWIDTH_FIELD;

	if (bwtype != null) encoded_string += bwtype + Separators.COLON;
	return encoded_string + bandwidth + Separators.NEWLINE; 
    }

    /**
     * Returns the bandwidth type.
     * @throws SdpParseException if a parsing error occurs
     * @return type
     */
    public String getType() throws SdpParseException {
        return  getBwtype();
    }
    
    /**
     * Sets the bandwidth type.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setType(String type) throws SdpException {
        if (type == null)
	    throw new SdpException("The type is null");
        else setBwtype(type);
    }
    
    /** 
     * Returns the bandwidth value measured in kilobits per second.
     * @throws SdpParseException if a parsing error occurs
     * @return the bandwidth value
     */
    public int getValue() throws SdpParseException {
        return getBandwidth();
    }
    
    /**
     * Sets the bandwidth value.
     * @param value to set
     * @throws SdpException if the value cannot be set
     */
    public void setValue(int value) throws SdpException {
        setBandwidth(value);
    }
	
    

}
