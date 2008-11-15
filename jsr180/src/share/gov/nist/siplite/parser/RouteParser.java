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
 * Parser for a list of route headers.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 */

public class RouteParser extends AddressParametersParser {
    /** Default constructor. */
    RouteParser() {}
    
    /**
     * Constructor with initial routeheader field.
     * @param route message to parse to set
     */
    public RouteParser(String route) {
        super(route);
    }
    
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected RouteParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Parses the String message and generate the Route List Object.
     * @return Header the Route List object
     * @throws ParseException if errors occur during the parsing
     */
    public Header parse() throws ParseException {
        RouteList routeList = new RouteList();
        if (debug) dbg_enter("parse");
        
        try {
            this.lexer.match(TokenTypes.ROUTE);
            this.lexer.SPorHT();
            this.lexer.match(':');
            this.lexer.SPorHT();
            while (true) {
                RouteHeader route = new RouteHeader();
                super.parse(route);
                routeList.add(route);
                this.lexer.SPorHT();
                if (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                } else if (lexer.lookAhead(0) == '\n') break;
                else throw createParseException("unexpected char");
            }
            return routeList;
        } finally {
            if (debug) dbg_leave("parse");
        }
    }
}
