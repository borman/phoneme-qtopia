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

import gov.nist.core.*;
import gov.nist.siplite.header.*;

/**
 * Parser for the SIP status line.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class StatusLineParser extends Parser {
    /**
     * Constructor with initial status line string.
     * @param statusLine initial status line
     */
    public StatusLineParser(String statusLine) {
        this.lexer = new Lexer("status_lineLexer", statusLine);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    public StatusLineParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("status_lineLexer");
    }
    
    /**
     * Gets the status code from the parsed line.
     * @return the status code
     * @exception ParseException if a parsing error occurs
     */
    protected int statusCode() throws ParseException {
        String scode = this.lexer.number();
        if (debug) dbg_enter("statusCode");
        try {
            int retval = Integer.parseInt(scode);
            return retval;
        } catch (NumberFormatException ex) {
            throw new ParseException(lexer.getBuffer() +
                    ":" + ex.getMessage(), lexer.getPtr());
        } finally {
            if (debug) dbg_leave("statusCode");
        }
        
    }
    
    /**
     * Gets the reason phrase.
     * @return the reason phrase
     * @exception ParseException of a parsing error occurs
     */
    protected String reasonPhrase() throws ParseException {
        return this.lexer.getRest().trim();
    }
    
    /**
     * Parses the status line message.
     * @return Header the status line object
     * @throws ParseException if errors occur during the parsing
     */
    public StatusLine parse() throws ParseException {
        try {
            if (debug) dbg_enter("parse");
            StatusLine retval = new StatusLine();
            String version = this.sipVersion();
            retval.setSipVersion(version);
            lexer.SPorHT();
            int scode = statusCode();
            retval.setStatusCode(scode);
            lexer.SPorHT();
            String rp = reasonPhrase();
            retval.setReasonPhrase(rp);
            lexer.SPorHT();
            return retval;
        } finally {
            if (debug) dbg_leave("parse");
        }
    }
    
}
