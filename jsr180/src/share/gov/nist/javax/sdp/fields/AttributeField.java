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
 * Attribute Field.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class AttributeField extends SDPField   {
    /** Current attribute. */
    protected NameValue attribute;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	AttributeField retval = new AttributeField();
	if (attribute != null) 
	    retval.attribute = (NameValue) this.attribute.clone();
	return retval;
    }
    /**
     * Gets the current attribute.
     * @return the current attribute.
     */
    public NameValue getAttribute() {
	return attribute;
    }

    /**
     * Default constructor.
     */
    public AttributeField() {
	super(ATTRIBUTE_FIELD);
    }

    /**
     * Sets the attribute member.
     * @param a the new attribute value
     */
    public	 void setAttribute(NameValue a) { 
	attribute = a; 
	attribute.setSeparator(Separators.COLON); 
    } 

    /**
     * Gets the string encoded version of this object.
     * @return encoding string of attribute contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string = ATTRIBUTE_FIELD;
	if (attribute != null) encoded_string += attribute.encode();
	return  encoded_string + Separators.NEWLINE; 
    }
    /**
     * Returns the encoded string of the attribute field contents.
     * @return encoded string of attribute contents.
     */
    public String toString() {
	return this.encode();
    }

    /**
     * Returns the name of this attribute
     * @throws SdpParseException if the name is not well formatted.
     * @return a String identity or null.
     */
    public String getName() throws SdpParseException {
	NameValue nameValue = getAttribute();
	if (nameValue == null) 
	    return null;
	else {
            String name = nameValue.getName();
            if (name == null)
		return null; 
            else return name;
	}
    }
    
    /**
     * Sets the id of this attribute.
     * @param name  the string name/id of the attribute.
     * @throws SdpException if the name is null
     */    
    public void setName(String name) throws SdpException {
        if (name == null)
	    throw new SdpException("The name is null"); 
        else {
	    NameValue nameValue = getAttribute();
	    if (nameValue == null) 
		nameValue = new NameValue();
	    nameValue.setName(name);
	    setAttribute(nameValue);
        }
    }
    
    /**
     * Determines if this attribute has an associated value.
     * @throws SdpParseException if the value is not well formatted.
     * @return true if the attribute has a value.
     */    
    public boolean hasValue() throws SdpParseException {
	NameValue nameValue = getAttribute();
	if (nameValue == null)
	    return false;
	else {
            Object value = nameValue.getValue();
            if (value == null) 
		return false;
            else return true;
	}
    }
    
    /**
     * Returns the value of this attribute.
     * @throws SdpParseException if the value is not well formatted.
     * @return the value; null if the attribute has no associated value.
     */    
    public String getValue() throws SdpParseException {
	NameValue nameValue = getAttribute();
	if (nameValue == null)
	    return null;
	else { 
            Object value = nameValue.getValue();
            if (value == null) 
		return null;
            else if (value instanceof String) 
		return (String)value;
	    else
		return value.toString();
	}
    }
    
    /** 
     * Sets the value of this attribute.
     * @param value the - attribute value
     * @throws SdpException if the value is null.
     */    
    public void setValue(String value) throws SdpException { 
	if (value == null)
	    throw new SdpException("The value is null"); 
        else {
	    NameValue nameValue = getAttribute();
	    if (nameValue == null)
		nameValue = new NameValue();
	    nameValue.setValue(value);
	    setAttribute(nameValue);
        }
    }
}
