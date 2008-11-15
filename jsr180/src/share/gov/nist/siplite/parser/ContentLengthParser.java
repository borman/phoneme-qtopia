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
 * Parser for Content-Length Header.
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ContentLengthParser extends HeaderParser {
    /** Default constructor. */
    public ContentLengthParser() { }
    
    /**
     * Constructor with initial content length header string.
     * @param contentLength initial content length header field
     */
    public ContentLengthParser(String contentLength) {
        super(contentLength);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ContentLengthParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Invokes parser for content length header field.
     * @return the parsed content length header
     */
    public Header parse() throws ParseException {
        if (debug) dbg_enter("ContentLengthParser.enter");
        try {
            ContentLengthHeader contentLength = new ContentLengthHeader();
            headerName(TokenTypes.CONTENT_LENGTH);
            String number = lexer.number();
            contentLength.setContentLength(Integer.parseInt(number));
            this.lexer.SPorHT();
            this.lexer.match('\n');
            return contentLength;
        } catch (ParseException pe) {
            throw createParseException(pe.getMessage());
        } finally {
            if (debug) dbg_leave("ContentLengthParser.leave");
        }
    }
}
