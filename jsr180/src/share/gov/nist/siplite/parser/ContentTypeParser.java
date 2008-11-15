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
 * Parser for content type header.
 *
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ContentTypeParser extends ParametersParser {
    /** Default constructor. */
    ContentTypeParser() {}
    
    /**
     * Constructor with initial content type header string.
     * @param contentType initial content length header
     */
    public ContentTypeParser(String contentType) {
        super(contentType);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ContentTypeParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Invokes parser for content type header field.
     * @return the parsed content type header
     * @exception ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        
        ContentTypeHeader contentType = new ContentTypeHeader();
        if (debug) dbg_enter("ContentTypeParser.parse");
        
        try {
            this.headerName(TokenTypes.CONTENT_TYPE);
            
            // The type:
            lexer.match(TokenTypes.ID);
            Token type = lexer.getNextToken();
            this.lexer.SPorHT();
            contentType.setContentType(type.getTokenValue());
            
            
            // The sub-type:
            lexer.match('/');
            lexer.match(TokenTypes.ID);
            Token subType = lexer.getNextToken();
            this.lexer.SPorHT();
            contentType.setContentSubType(subType.getTokenValue());
            super.parse(contentType);
            this.lexer.match('\n');
        } finally {
            if (debug) dbg_leave("ContentTypeParser.parse");
        }
        return contentType;
        
    }
    
}

