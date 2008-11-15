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

/** Use this class for the headers that can not have any parameters. */
public class ParameterLessHeader extends Header {

    /** Class handle. */
    public static Class clazz;

    static {
        clazz = new ParameterLessHeader().getClass();
    }

    /**
     * Default constructor for a generic parameter-less header.
     */
    public ParameterLessHeader() {}

    /**
     * Constructor given the name.
     * @param headerName is the header name.
     */
    public ParameterLessHeader(String headerName) {
        super(headerName);
    }

    /**
     * Constructor given the name and value.
     * @param headerName is the header name.
     * @param headerVal is the header value.
     */
    public ParameterLessHeader(String headerName, String headerVal) {
        super(headerName);
        headerValue = StringTokenizer.convertNewLines(headerVal.trim());
    }

    /**
     * Sets the value without parameters for a generic header.
     * @param value the new value to be set
     */
    public void setValue(String value) {
        headerValue = StringTokenizer.convertNewLines(value.trim());
    }

    /**
     * Sets the name for a generic header.
     * @param name the new name to be set
     */
    public void setName(String name) {
        headerName = name.trim();
    }

    /**
     * Gets the parameter list (which is always empty) for this header.
     * @return always null
     */
    public NameValueList getParameters() {
        return null;
    }

    /**
     * Encodes the body as a textstring.
     * @return encoded string of body contents
     */
    public String encodeBody() {
        return headerValue;
    }

    /**
     * Gets the value of the header without parameters.
     * @return value of extension header field
     */
    public Object getValue() {
        return headerValue;
    }
}

