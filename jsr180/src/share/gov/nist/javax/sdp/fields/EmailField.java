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
 * email field in the SDP announce.
 *
 *@version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class EmailField extends SDPField {
    /** Email address. */
    protected EmailAddress emailAddress;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	EmailField retval = new EmailField();
	if (emailAddress != null)
	    retval.emailAddress = (EmailAddress) this.emailAddress.clone();
	return retval;
    }
 
    /** DEfault constructor. */
    public EmailField() {
	super(SDPFieldNames.EMAIL_FIELD);
	emailAddress = new EmailAddress();
    }
 
    /** 
     * Gets the email address.
     * @return the email address
     */
    public EmailAddress getEmailAddress() {
	return emailAddress;
    } 

    /**
     * Sets the email address member.
     * @param emailAddress the new email address
     */
    public void setEmailAddress(EmailAddress emailAddress) {
	this.emailAddress = emailAddress;
    } 
 
    /**
     * Gets the string encoded version of this object.
     * @return encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	return EMAIL_FIELD + emailAddress.encode() + Separators.NEWLINE;
    }

    /**
     * Gets the string encoded version of this object.
     * @return encoded string of object contents
     */
    public String toString() { return this.encode(); }

    /**
     * Gets the email address value.
     * @throws SdpParseException if a parsing error occurs
     * @return the value
     */
    public String getValue() throws SdpParseException {
	if (emailAddress == null)
	    return null;
	else {
	    return emailAddress.getDisplayName(); 
	}
    }
 
    /**
     * Sets the email value.
     * @param value to set
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
	if (value == null)
	    throw new SdpException("The value is null");
	else {
 
	    emailAddress.setDisplayName(value);
	}
    }


}
