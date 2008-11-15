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

/**
 * Email address field of the SDP header.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change)
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class EmailAddress extends SDPObject {
    /** Current user friendly display name. */
    protected String displayName;
    /** Current email address */
    protected Email email;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	EmailAddress retval = new EmailAddress();
	retval.displayName = displayName;
	if (email != null) retval.email = (Email) email.clone();
	return retval;
    }

    /** 
     * Gets the current user friendly name.
     * @return the display name
     */
    public String getDisplayName() {
	return displayName;
    } 

    /**
     * Sets the display name member.
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    } 

    /**
     * Sets the email address member.
     * @param email the new email address
     */
    public void setEmail(Email email) {
	this.email = email;
    } 

    /**
     * Gets the string encoded version of this object.
     * Here, we implement only the "displayName &lt;email&gt;" form
     * and not the "email (displayName)" form.
     * @return the encoded string of this object contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string;
 
	if (displayName != null) {
	    encoded_string = displayName + Separators.LESS_THAN;
	} else {
	    encoded_string = "";
	}
	encoded_string += email.encode();
	if (displayName != null) {
	    encoded_string += Separators.GREATER_THAN;
	}
	return encoded_string;
    }

}
