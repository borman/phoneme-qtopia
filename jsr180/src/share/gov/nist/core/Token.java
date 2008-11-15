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
package gov.nist.core;

/**
 * Base token class.
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public class Token  {
    /** The value of the token. */
    protected String tokenValue;
    /** The type of the token. */
    protected int tokenType;
    
    /**
     * Gets the token value.
     * @return the textual token value
     */
    public String getTokenValue() {
        return this.tokenValue;
    }
    
    /**
     * Gets the token type.
     * @return the token type
     */
    public int getTokenType() {
        return this.tokenType;
    }
    
    /**
     * Gets a textual representation of the
     * token value and type.
     * @return the encoded string
     */
    public String toString() {
        return "tokenValue = " + tokenValue +
                "/tokenType = " + tokenType;
    }
}



