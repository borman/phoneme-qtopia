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
 * Phone Field SDP header.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class PhoneField extends SDPField {
    /** Addressbook name. */
    protected String name;
    /** Entry phone number. */
    protected String phoneNumber;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	PhoneField retval = new PhoneField();
	retval.name = this.name;
	retval.phoneNumber = this.phoneNumber;
	return retval;
    }

    /** Default constructor. */
    public PhoneField() {
	super(PHONE_FIELD);
    }

    /**
     * Gets the name field.
     * @return the name
     */
    public String getName() {
	return name;
    } 

    /** 
     * Gets the phone number.
     * @return the textual phone number
     */
    public String getPhoneNumber() {
	return phoneNumber;
    } 

    /**
     * Sets the name member.
     * @param name the name to set.
     */
    public void setName(String name) {
	this.name = name;
    } 

    /**
     * Sets the phone number member.
     * @param phoneNumber phone number to set. 
     */
    public void setPhoneNumber(String phoneNumber) {
	this.phoneNumber = phoneNumber;
    } 

    /**
     * Returns the value.
     * @throws SdpParseException if a parsing error occurs
     * @return the value.
     */ 
    public String getValue()
	throws SdpParseException {
	return getName();
    }
 
    /**
     * Sets the value.
     * @param value the - new information.
     * @throws SdpException if the value is null
     */ 
    public void setValue(String value)
	throws SdpException {
	if (value == null)
	    throw new SdpException("The value parameter is null");
	else setName(value); 
    }
 

    /**
     * Gets the string encoded version of this object.
     * Here, we implement only the "name &lt;phoneNumber&gt;" form
     * and not the "phoneNumber (name)" form
     * @return encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string;
	encoded_string = PHONE_FIELD;
	if (name != null) {
	    encoded_string += name + Separators.LESS_THAN;
	}
	encoded_string += phoneNumber;
	if (name != null) {
	    encoded_string += Separators.GREATER_THAN;
	}
	encoded_string += Separators.NEWLINE;
	return encoded_string;
    }

}
