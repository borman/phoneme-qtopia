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
import java.util.Vector;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Base parser class.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public abstract class Parser extends ParserCore implements TokenTypes {
    /** Default constructor. */
    protected Parser() {}

    /**
     * Creates a ParseException with the provided message.
     * @param exceptionString message for cause of execption
     * @return the parser exception
     */
    protected ParseException
            createParseException(String exceptionString) {
        return new ParseException
                (lexer.getBuffer() + ":" + exceptionString, lexer.getPtr());
    }

    /**
     * Gets the current lexer engine.
     * @return the current lexer engine
     */
    protected Lexer getLexer() {
        return (Lexer) this.lexer;
    }

    /**
     * Gets the current SIP version string.
     * @return the SIP version string.
     * @exception ParseException if a parsing error occurs
     */
    protected String sipVersion() throws ParseException {
        if (debug) dbg_enter("sipVersion");
        try {
            Token tok = lexer.match(SIP);
            if (! tok.getTokenValue().equals("SIP"))
                createParseException("Expecting SIP");
            lexer.match('/');
            tok = lexer.match(ID);
            if (! tok.getTokenValue().equals("2.0"))
                createParseException("Expecting SIP/2.0");

            return "SIP/2.0";
        } finally {
            if (debug) dbg_leave("sipVersion");
        }
    }

    /**
     * Parses a method. Consumes if a valid method has been found.
     * @return the parsed method string
     * @exception ParseException if a parsing error occurs
     */
    protected String method() throws ParseException {
        try {
            if (debug) dbg_enter("method");
            Vector tokens = this.lexer.peekNextToken(1);
            Token token = (Token) tokens.elementAt(0);
            int tokenType = token.getTokenType();

            if (tokenType == INVITE ||
                    tokenType == ACK ||
                    tokenType == OPTIONS ||
                    tokenType == BYE ||
                    tokenType == REGISTER ||
                    tokenType == CANCEL ||
                    tokenType == SUBSCRIBE ||
                    tokenType == NOTIFY ||
                    tokenType == ID ||
                    tokenType == MESSAGE ||
                    tokenType == INFO ||
                    tokenType == PUBLISH ||
                    tokenType == UPDATE ||
                    tokenType == PRACK ||
                    tokenType == REFER) {
                lexer.consume();
                return token.getTokenValue();
            } else {
                throw createParseException
                        ("Invalid Method");
            }
        } finally {
            if (debug) dbg_leave("method");
        }
    }
}
