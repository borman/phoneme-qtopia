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
package gov.nist.siplite.parser;

import gov.nist.siplite.header.*;
import gov.nist.core.*;

/**
 * Parser for WWW authenitcate header.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 */
public class WWWAuthenticateParser extends ChallengeParser {
    /** Default constructor. */
    protected WWWAuthenticateParser() {}
    
    /**
     * Constructor with initial WWW AUthenticate header.
     * @param wwwAuthenticate message to parse
     */
    public WWWAuthenticateParser(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected WWWAuthenticateParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Parses the String message.
     * @return Header (WWWAuthenticate object)
     * @throws ParseException if the message does not respect the spec.
     */
    public Header parse() throws ParseException {
        if (debug) dbg_enter("parse");
        try {
            headerName(TokenTypes.WWW_AUTHENTICATE);
            WWWAuthenticateHeader
                    wwwAuthenticate = new WWWAuthenticateHeader();
            super.parse(wwwAuthenticate);
            return wwwAuthenticate;
        } finally {
            if (debug) dbg_leave("parse");
        }
    }
}

