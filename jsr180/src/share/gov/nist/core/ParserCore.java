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
package gov.nist.core;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Generic parser class.
 * All parsers inherit this class.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public abstract class ParserCore {
    /** Flag to indicate parsing diagnostics should be logged. */
    public static final boolean debug = false;
    /* (Logging.REPORT_LEVEL <= Logging.INFORMATION); */
    
    /** Current nesting level int recursive parsing. */
    protected static int nesting_level;
    /** Current lexer engine. */
    protected LexerCore lexer;

    /**
     * Gets a name value pair with the specified separator.
     * @param separator character to use int encoded return value
     * @return encoded name value text string.
     * @exception ParseException if a parsing error occurs
     */
    protected NameValue nameValue(char separator) throws ParseException  {
        if (debug)
            dbg_enter("nameValue");
        try {

            lexer.match(LexerCore.ID);
            Token name = lexer.getNextToken();
            // eat white space.
            lexer.SPorHT();
            try {
                boolean quoted = false;
                char la = lexer.lookAhead(0);

                if (la == separator) {
                    lexer.consume(1);
                    lexer.SPorHT();
                    String str = null;
                    if (lexer.lookAhead(0) == '\"')  {
                        str = lexer.quotedString();
                        quoted = true;
                    } else {
                        lexer.match(LexerCore.ID);
                        Token value = lexer.getNextToken();
                        str = value.tokenValue;
                    }
                    NameValue nv =
                            new NameValue(name.tokenValue, str);
                    if (quoted) nv.setQuotedValue();
                    return nv;
                } else {
                    // if the parameter has no value, an empty string is used
                    return new NameValue(name.tokenValue, "");
                }
            } catch (ParseException ex) {
                return new NameValue(name.tokenValue, "");
            }

        } finally {
            if (debug) dbg_leave("nameValue");
        }


    }

    /**
     * Beginning of debug operation.
     * @param rule the name of the rule being evaluated
     */
    protected  void dbg_enter(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        
        for (int i = 0; i < nesting_level; i++)
            stringBuffer.append(">");

        if (debug)  {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    stringBuffer + rule +
                    "\nlexer buffer = \n" +
                    lexer.getRest());
        }
        nesting_level++;
    }
    /**
     * Completion of diagnostic operation.
     * @param rule name of the rule being evaluated
     */
    protected void dbg_leave(String rule) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < nesting_level; i++)
            stringBuffer.append("<");

        if (debug)  {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    stringBuffer +
                    rule +
                    "\nlexer buffer = \n" +
                    lexer.getRest());
        }
        nesting_level --;
    }
    /**
     * Gets name value token using '=' as a separator.
     * @return the name value token
     * @exception ParseException if a parsing error occurs
     */
    protected NameValue nameValue() throws ParseException  {
        return nameValue('=');
    }


    /**
     * Peeks at the next line.
     * @param rule the name of the lexer rule to apply
     */
    protected void peekLine(String rule) {
        if (ParserCore.debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                rule + " " + lexer.peekLine());
        }
    }
}

