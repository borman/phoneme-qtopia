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

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;

/**
 *  Parser for Repeat field.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class RepeatFieldParser extends SDPParser {

    /**
     * Creates new RepeatFieldsParser.
     * @param repeatField the repeat times filed to be parsed
     */
    public RepeatFieldParser(String repeatField) {
        lexer = new Lexer("charLexer", repeatField);
    }

    /** Default constructor. */
    protected RepeatFieldParser() {
        super();
    }

    /**
     * Gets the typed time
     * @param  tokenValue to set
     * @return TypedTime
     */
    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime = new TypedTime();

        if (tokenValue.endsWith("d")) {
            typedTime.setUnit("d");
            String t = tokenValue.replace('d', ' ');

            typedTime.setTime(Integer.parseInt(t.trim()));
        } else
            if (tokenValue.endsWith("h")) {
            typedTime.setUnit("h");
            String t = tokenValue.replace('h', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else
            if (tokenValue.endsWith("m")) {
            typedTime.setUnit("m");
            String t = tokenValue.replace('m', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else {
            typedTime.setUnit("s");
            if (tokenValue.endsWith("s")) {
                String t = tokenValue.replace('s', ' ');
                typedTime.setTime(Integer.parseInt(t.trim()));
            } else
                typedTime.setTime(Integer.parseInt(tokenValue.trim()));
        }
        return typedTime;
    }


    /**
     * Parses the field string.
     * @return RepeatFields 
     * @exception ParseException if a parsing error occurs
     */
    public RepeatField repeatField() throws ParseException  {
        lexer.match('r');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        RepeatField repeatField = new RepeatField();

        lexer.match(LexerCore.ID);
        Token repeatInterval = lexer.getNextToken();
        this.lexer.SPorHT();
        TypedTime typedTime = getTypedTime(repeatInterval.getTokenValue());
        repeatField.setRepeatInterval(typedTime);  

        lexer.match(LexerCore.ID);
        Token activeDuration = lexer.getNextToken();
        this.lexer.SPorHT();
        typedTime = getTypedTime(activeDuration.getTokenValue());
        repeatField.setActiveDuration(typedTime);  

        // The offsets list:
        while (lexer.lookAhead(0) != '\n') {
            lexer.match(LexerCore.ID);
            Token offsets = lexer.getNextToken();
            this.lexer.SPorHT();
            typedTime = getTypedTime(offsets.getTokenValue());
            repeatField.addOffset(typedTime);
        }


        return repeatField;
    }

    /**
     * Parses the field string.
     * @return RepeatFields 
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return this.repeatField();
    }



}
