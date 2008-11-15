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
 * Key field part of an SDP header
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change)
 *
 */
public class KeyField extends SDPField {
    /** Type of key. */
    protected String type;
    /** Key data. */
    protected String keyData;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	KeyField retval = new KeyField();
	retval.type = type;
	retval.keyData = keyData;
	return retval;
    }

    /** Default constructor. */ 
    public KeyField() {
	super(KEY_FIELD);
    }

    /**
     * Gets the type member.
     * @return thetype
     */
    public String getType() 
    { return type; }

    /** 
     * Gets the key data.
     * @return the key data
     */
    public String getKeyData() 
    { 
	return keyData;
    }

    /**
     * Sets the type member.
     * @param t the new key type 
     */
    public void setType(String t) {
	type = t;
    } 
    /**
     * Sets the key data member.
     * @param k the new key data
     */
    public void setKeyData(String k) {
	keyData = k;
    } 

    /**
     * Gets the string encoded version of this object.
     * @return the encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string; 
	encoded_string = KEY_FIELD + type;
	if (Utils.compareToIgnoreCase(type, SDPKeywords.PROMPT) == 0) {
	    if (Utils.compareToIgnoreCase(type, SDPKeywords.URI) == 0) {
		encoded_string += Separators.COLON;
		encoded_string += keyData;
	    } else {
		if (keyData != null) {
		    encoded_string += Separators.COLON;
		    encoded_string += keyData;
		}
	    }
	}
	encoded_string += Separators.NEWLINE;
	return encoded_string;
    }

    /**
     * Returns the name of this attribute. 
     * @throws SdpParseException if a parsing error occurs
     * @return the name of this attribute
     */
    public String getMethod()
	throws SdpParseException {
	return this.type;
    }
 
    /**
     * Sets the id of this attribute.
     * @param name to set
     * @throws SdpException if the name is null
     */
    public void setMethod(String name)
	throws SdpException {
	this.type = name;
    }
 
    /**
     * Determines if this attribute has an associated value.
     * @throws SdpParseException if a parsing error occurs
     * @return if this attribute has an associated value.
     */
    public boolean hasKey()
	throws SdpParseException {
	String key = getKeyData();
	return key != null;
    }
 
    /**
     * Returns the value of this attribute.
     * @throws SdpParseException if a parsing error occurs
     * @return the value of this attribute
     */ 
    public String getKey()
	throws SdpParseException {
	return getKeyData();
    }
 
    /**
     * Sets the value of this attribute.
     * @param key to set
     * @throws SdpException if key is null
     */ 
    public void setKey(String key)
	throws SdpException {
	if (key == null)
	    throw new SdpException("The key is null");
	else setKeyData(key); 
    }
}
