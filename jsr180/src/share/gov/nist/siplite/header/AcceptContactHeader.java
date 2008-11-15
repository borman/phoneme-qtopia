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
import gov.nist.siplite.*;
import gov.nist.siplite.address.*;

/**
 * The generic AcceptContact header
 *
 *
 */
public class AcceptContactHeader extends ParametersHeader {

    /** Handle for class. */
    public static Class clazz;
    /** Accept-contact header field label. */
    public static final String NAME = Header.ACCEPT_CONTACT;
    
    
    static {
        clazz = new AcceptContactHeader().getClass();
    }
    
    /**
     * Default constructor.
     */
    public AcceptContactHeader() {
        super(ACCEPT_CONTACT);
    }
    
    /**
     * Sets the specified parameter.
     * @param nv parameter's name/value pair
     */
    public void setParameter(NameValue nv) {
        Object val = nv.getValue();
        setParameter(nv.getName(), (val == null) ? null : val.toString());
    }

    /**
     * Sets the specified parameter.
     * @param name name of the parameter
     * @param value value of the parameter.
     */
    public void setParameter(String name, String value)
            throws IllegalArgumentException {
        NameValue nv =
                super.parameters.getNameValue(name.toLowerCase());
            
        if (value == null) {
            throw new NullPointerException("null value");
        }
        
        boolean quoteStart = value.startsWith(Separators.DOUBLE_QUOTE);
        boolean quoteEnd = value.endsWith(Separators.DOUBLE_QUOTE);
        
        if ((quoteStart && !quoteEnd) || (!quoteStart && quoteEnd)) {
            throw new IllegalArgumentException
                (value + " : Unexpected DOUBLE_QUOTE");
        }
        
        if (quoteStart) { // quoteEnd is true in this case
            value = value.substring(1, value.length() - 1);
        }
        
        if (nv == null) {
            nv = new NameValue(name.toLowerCase(), value);

            nv.setQuotedValue();
            super.setParameter(nv);
        
        } else {
            nv.setValue(value);
        }

    }

    /**
     * Sets the "type" parameter.
     * @param value value of the parameter.
     */
    public void setType(String value) {
        setParameter(SIPConstants.GENERAL_TYPE, value);
    }
    
    /**
     * Returns the value of the named parameter, or null if it is not set. A
     * zero-length String indicates flag parameter.
     *
     * @param name name of parameter to retrieve
     * @return the value of specified parameter
     */
    public String getParameter(String name) {
        String returnValue = super.getParameter(name);
        if (returnValue != null) { // remove quotes
            returnValue = returnValue.substring(1, returnValue.length() - 1);
        }
        return returnValue;
    }
    
    /**
     * Returns the value of the "type" parameter, or null if it is not set.
     *
     * @return the value of specified parameter
     */
    public String getType() {
        return getParameter(SIPConstants.GENERAL_TYPE);
    }

    /**
     * Encodes in canonical form.
     * @return canonical string.
     */
    public String encodeBody() {
        return Separators.STAR + encodeWithSep();
    }

    /**
     * Clone - do a deep copy.
     * @return Object AcceptContactHeader
     */
    public Object clone() {
        try {
            AcceptContactHeader retval = (AcceptContactHeader)
                this.getClass().newInstance();
            if (this.parameters != null)
                retval.parameters = (NameValueList)parameters.clone();
            return retval;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

    /**
     * Compares for equivalence.
     * @param that object to compare
     * @return true if object matches
     */
    public boolean equals(Object that) {
        if (! that.getClass().equals(this.getClass())) {
            return false;
        } else {
            AcceptContactHeader other = (AcceptContactHeader) that;
            return this.parameters.equals(other.parameters);
        }
    }
    
    /**
     * Gets the header value.
     * @return the content type
     */
    public Object getValue() {
        return Separators.STAR;
    }

}
