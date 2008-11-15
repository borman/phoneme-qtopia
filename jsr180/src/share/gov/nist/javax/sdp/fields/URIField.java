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
 * Implementation of URI field.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class URIField extends SDPField {
    /** URL string field contents. */
    protected String urlString;

    /** Default constructor. */
    public URIField() {
	super(URI_FIELD);
    }
 
    /**
     * Gets the URL field contents.
     * @return the URL string
     */
    public String getURI() {
	return urlString;
    }

    /** 
     * Sets the URL string contents.
     * @param uri the URL string
     */
    public void setURI(String uri) {
	this.urlString = uri;
    }

    /**
     * Gets the URL field contents.
     * @return  the URL string
     */
    public String get() {
	return urlString;
    }

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	URIField retval = new URIField();
	retval.urlString = this.urlString;
	return retval;
    }

    /**
     * Gets the string encoded version of this object.
     * @return the encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	if (urlString != null) {
	    return URI_FIELD + urlString + Separators.NEWLINE;
	} else return "";
    }
}
