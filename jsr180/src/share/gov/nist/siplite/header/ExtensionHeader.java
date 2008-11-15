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
package gov.nist.siplite.header;

import gov.nist.core.*;

/** Use this class when there is no parser for parsing the given header. */
public class ExtensionHeader extends ParametersHeader {
    /** Extension header label. */
    public static final String NAME = Header.EXTENSION;

    /** Class handle. */
    public static Class clazz;

    /** Header field value, without parameters. */
    protected String valueWithoutParam;

    static {
        clazz = new ExtensionHeader().getClass();
    }

    /**
     * Default constructor for a generic header.
     */
    public ExtensionHeader() {}

    /**
     * Constructor given the name and value.
     * @param hdrName is the header name.
     * @param hdrValue is the header value.
     * @param pureHeaderValue is the header value without parameters.
     */
    public ExtensionHeader(String hdrName, String hdrValue,
                           String pureHeaderValue) {
        super(hdrName);
        headerValue  = hdrValue;
        valueWithoutParam = pureHeaderValue;
    }

    /**
     * Sets the header value field.
     * Overloads the function from the base class.
     * @param value is the value field to set
     */
    public void setHeaderValue(String value) {
        headerValue = value;
        valueWithoutParam = value;
    }

    /**
     * Sets the value without parameters for a generic header.
     * @param value the new value to be set
     */
    public void setValue(String value) {
        valueWithoutParam = value;
    }

    /**
     * Sets the name for a generic header.
     * @param name the new name to be set
     */
    public void setName(String name) {
        headerName = name;
    }

    /**
     * Gets the parameter list for this extension header.
     * @return name value list for extension header field
     */
    public NameValueList getParameters() {
        return parameters;
    }

    /**
     * Encodes the body as a textstring.
     * @return encoded string of body contents
     */
    public String encodeBody() {
        if (parameters != null && !parameters.isEmpty()) {
            if (valueWithoutParam.equals("")) {
                return parameters.encode();
            } else {
                String separator = Header.isAuthorization(headerName) ?
                    Separators.SP : Separators.SEMICOLON;
                return valueWithoutParam + separator +
                    parameters.encode();
            }
        } else {
            return headerValue;
        }
    }

    /**
     * Gets the value of the header without parameters.
     * @return value of extension header field
     */
    public Object getValue() {
        return valueWithoutParam;
    }

}
