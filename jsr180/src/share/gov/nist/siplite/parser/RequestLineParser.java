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
import gov.nist.siplite.address.*;
import gov.nist.siplite.header.*;
import gov.nist.core.*;

/**
 * Parser for the SIP request line.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

class RequestLineParser extends Parser {
    /**
     * Constructor with initial request line string.
     * @param requestLine initial request line
     */
    public RequestLineParser(String requestLine) {
        this.lexer = new Lexer("method_keywordLexer", requestLine);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    public RequestLineParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("method_keywordLexer");
    }
    
    /**
     * Invokes parser for the request line.
     * @return the parsed request line
     * @exception ParseException if a parsing error occurs
     */
    public RequestLine parse() throws ParseException {
        if (debug) dbg_enter("parse");
        try {
            RequestLine retval = new RequestLine();
            String m = method();
            lexer.SPorHT();
            retval.setMethod(m);
            this.lexer.selectLexer("sip_urlLexer");
            URLParser urlParser = new URLParser(this.getLexer());
            URI url = urlParser.uriReference();
            lexer.SPorHT();
            retval.setUri(url);
            this.lexer.selectLexer("request_lineLexer");
            String v = sipVersion();
            retval.setSipVersion(v);
            lexer.SPorHT();
            lexer.match('\n');
            return retval;
        } finally {
            if (debug) dbg_leave("parse");
        }
    }
}
