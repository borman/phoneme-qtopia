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
 * Parser for the challenge portion of the authentication header.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 */
public abstract class ChallengeParser extends HeaderParser {
    /** Default constructor. */
    protected ChallengeParser() {}

    /**
     * Constructor with initial challenge string.
     * @param challenge message to parse to set
     */
    protected ChallengeParser(String challenge) {
        super(challenge);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ChallengeParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * Gets the parameter of the challenge string.
     * @param header header field to process
     */
    protected void parseParameter(AuthenticationHeader header)
    throws ParseException {

        if (debug) dbg_enter("parseParameter");
        try {
            NameValue nv = nameValue('=');
            if (header.hasParameter(nv.getName())) {
                throw new ParseException("Duplicated parameter: " +
                    nv.getName(), 0);
            }
            header.setParameter(nv);
        } finally {
            if (debug) dbg_leave("parseParameter");
        }
    }

    /**
     * Parses the String message.
     * @param header Challenge object for parsed data
     * @throws ParseException if the message does not respect the spec.
     */
    public void parse(AuthenticationHeader header) throws ParseException {
        // the Scheme:
        this.lexer.SPorHT();
        lexer.match(TokenTypes.ID);
        Token type = lexer.getNextToken();
        this.lexer.SPorHT();
        header.setScheme(type.getTokenValue());

        // The parameters:
        try {
            while (lexer.lookAhead(0) != '\n') {
                this.parseParameter(header);
                this.lexer.SPorHT();
                if (lexer.lookAhead(0) == '\n' ||
                        lexer.lookAhead(0) == '\0') break;
                this.lexer.match(',');
                this.lexer.SPorHT();
            }
        } catch (ParseException ex) {
            throw ex;
        }
    }
}
