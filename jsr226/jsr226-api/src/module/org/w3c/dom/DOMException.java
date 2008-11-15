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
 * information or have any questions.  */
 
/*
* Copyright (c) 2004 World Wide Web Consortium,
*
* (Massachusetts Institute of Technology, European Research Consortium for
* Informatics and Mathematics, Keio University). All Rights Reserved. This
* work is distributed under the W3C(r) Software License [1] in the hope that
* it will be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*
* [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
*/

package org.w3c.dom;

/**
 *
 */
public class DOMException extends RuntimeException {

    /**
     *
     */
    public static final short WRONG_DOCUMENT_ERR          = 4;

    /**
     *
     */
    public static final short INDEX_SIZE_ERR = 1;

    /**
     *
     */
    public static final short HIERARCHY_REQUEST_ERR = 3;

    /**
     *
     */
    public static final short NO_MODIFICATION_ALLOWED_ERR = 7;

    /**
     *
     */
    public static final short NOT_FOUND_ERR = 8;

    /**
     *
     */
    public static final short NOT_SUPPORTED_ERR = 9;

    /**
     *
     */
    public static final short INVALID_STATE_ERR = 11;

    /**
     *
     */
    public static final short INVALID_MODIFICATION_ERR = 13;

    /**
     *
     */
    public static final short INVALID_ACCESS_ERR = 15;

    /**
     *
     */
    public static final short TYPE_MISMATCH_ERR = 17;

    /**
     *
     */
    public short code;

    /**
     *
     */
    public DOMException(final short code,
                        final String message) {
        super(message);
        this.code = code;
    }

}
