/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * Parser for parameterless headers.
 */
public class ParameterLessParser extends HeaderParser {
    /** Default constructor. */
    protected ParameterLessParser() {}

    /**
     * Constructor with header value.
     * @param value full header value respresented as a string
     */
    public ParameterLessParser(String value) {
        super(value);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ParameterLessParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * Invokes parser for a parameterless header.
     * @return the parsed ParameterLessHeader
     * @throws ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        try {
            String name = lexer.getNextToken(':');
            lexer.consume(1);
            String value = lexer.getLine().trim();

            // Some parameterless headers (for ex., RACK) have corresponding
            // classes but share this parser so we have to check the name
            // of header to decide which class to create.
            ParameterLessHeader retval;

            if (name.equalsIgnoreCase(Header.RACK)) {
                retval = new RAckHeader();
                retval.setHeaderValue(value);
            } else if (name.equalsIgnoreCase(Header.RSEQ)) {
                retval = new RSeqHeader();
                retval.setHeaderValue(value);
            } else {
                retval = new ParameterLessHeader(name, value);
            }

            return retval;
        } catch (Exception e) {
            throw new ParseException("Error in parse(): " + e.getMessage(), 0);
        }
    }
}
