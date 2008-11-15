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
 * Parser for SIP Expires Parser. Converts from SIP Date to the
 * internal storage (Calendar).
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ExpiresParser extends HeaderParser {
    /** Default constructor. */
    public ExpiresParser() {}
    
    /**
     * Constructor with initial expires header to be processed.
     * @param text is the text of the header to parse
     */
    public ExpiresParser(String text) {
        super(text);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ExpiresParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Parses the header.
     * @return parsed Expires header
     * @exception ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        ExpiresHeader expires = new ExpiresHeader();
        if (debug) dbg_enter("parse");
        try {
            lexer.match(TokenTypes.EXPIRES);
            lexer.SPorHT();
            lexer.match(':');
            lexer.SPorHT();
            String nextId = lexer.getNextId();
            lexer.match('\n');
            try {
                int delta = Integer.parseInt(nextId);
                expires.setExpires(delta);
                return expires;
            } catch (NumberFormatException ex) {
                throw createParseException("bad integer format");
            }
        } finally {
            if (debug) dbg_leave("parse");
        }
        
        
    }
}
