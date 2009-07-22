/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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
 */
public class SingleHeaderParser extends ParametersParser {
    /** Default constructor. */
    protected SingleHeaderParser() {}

    /**
     * Constructor with header value.
     * @param value full header value respresented as a string
     */
    public SingleHeaderParser(String value) {
        super(value);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected SingleHeaderParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * Invokes parser for extension header field.
     * IMPL_NOTE: optimize it (maybe use ParserCore.nameValue()?).
     * @return the parsed extension header
     * @throws ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        String name = lexer.getNextToken(':');
        lexer.consume(1);
        lexer.SPorHT();
        String body = "";
        String rest = lexer.getRest();
        if (rest != null) {
            body = StringTokenizer.convertNewLines(rest.trim());
        }
        char colonDelimiter = ';';
        int index = getDelimiterIndex(body, colonDelimiter);

        if (index == -1) {
            // no parameters were specified
            // String body = lexer.getLine().trim();
            ExtensionHeader retval = new ExtensionHeader(name, body);
            return retval;
        } else {
            int currPos = lexer.getPtr();
            body = StringTokenizer.convertNewLines(
                lexer.getBuffer().substring(currPos, currPos + index).trim());
            lexer.consume(index);
            
            ExtensionHeader retval = new ExtensionHeader(name, body);
            
            super.parse(retval);
            this.lexer.match('\n');

            return retval;
        }
    }
}
