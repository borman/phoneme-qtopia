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

/**
 * Media Description SDP header
 *
 * @version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class SDPFormat extends SDPObject {
    /** Format string. */
    protected String format;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	SDPFormat retval = new SDPFormat();
	retval.format = format;
	return retval;
    }
 
    /**
     * Sets the format string.
     * @param fmt the new format string
     */
    public void setFormat(String fmt) {
	format = fmt;
    }

    /**
     * Gets the format string.
     * @return the format string
     */
    public String getFormat() {
	return format; 
    }

    /**
     * Constructor with initial format string.
     * @param s the format string
     */
    public SDPFormat(String s) {
	format = s;
    }
 
    /** Deafault constructor. */
    public SDPFormat() {}

    /**
     * Returns the object contents.
     * @return the format string
     */
    public String encode() { return format; }


}

 
