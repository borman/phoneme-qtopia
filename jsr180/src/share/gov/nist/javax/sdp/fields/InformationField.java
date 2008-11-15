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
 * Information field implementation 
 * @version JSR141-PUBLIC-REVIEW (subject to change)
 *
 */

public class InformationField extends SDPField {
    /** Additional textual description. */
    protected String information;

    /** Default constructor. */
    public InformationField() {
	super(INFORMATION_FIELD);
    }

    /**
     * Gets the information field.
     * @return the information member
     */
    public String getInformation() {
	return information;
    }

    /**
     * Sets the extra descriptive information.
     * @param info the new descriptive text
     */
    public void setInformation(String info) {
	information = info;
    } 

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	InformationField retval = new InformationField();
	retval.information = this.information;
	return retval;
    }

    /**
     * Gets the string encoded version of this object.
     * @return the encode string of object contents
     * @since v1.0
     */
    public String encode() {
	return INFORMATION_FIELD + information + Separators.NEWLINE;
    }

    /**
     * Returns the information value.
     * @throws SdpParseException if a parsing error occurs
     * @return the value
     */
    public String getValue() throws SdpParseException {
	return information;
    }
 
    /**
     * Sets the value.
     * @param value to set
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
	if (value == null)
	    throw new SdpException("The value is null");
	else {
	    setInformation(value);
	}
    }

}

