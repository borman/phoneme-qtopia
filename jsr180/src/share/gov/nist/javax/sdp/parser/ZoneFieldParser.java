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
 * Parser For the Zone field.
 *
 *@version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ZoneFieldParser extends SDPParser {

    /**
     * Creates new ZoneFieldParser.
     * @param zoneField the time zone field to be parsed
     */
    public ZoneFieldParser(String zoneField) {
        lexer = new Lexer("charLexer", zoneField);
    }
    /** Default constructor. */
    protected ZoneFieldParser() {
        super();
    }

    /**
     * Gets the sign of the offset.
     * @param tokenValue to set
     * @return String
     */
    public String getSign(String tokenValue) {
        if (tokenValue.startsWith("-"))
            return "-";
        else return "+";
    }

    /**
     * Gets the typed time.
     * @param tokenValue to set
     * @return TypedTime
     */
    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime = new TypedTime();
        String offset = null;
        if (tokenValue.startsWith("-"))
            offset = tokenValue.replace('-', ' ');
        else if (tokenValue.startsWith("+"))
            offset = tokenValue.replace('+', ' ');
        else offset = tokenValue;


        if (offset.endsWith("d")) {
            typedTime.setUnit("d");
            String t = offset.replace('d', ' ');

            typedTime.setTime(Integer.parseInt(t.trim()));
        } else
            if (offset.endsWith("h")) {
            typedTime.setUnit("h");
            String t = offset.replace('h', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else
            if (offset.endsWith("m")) {
            typedTime.setUnit("m");
            String t = offset.replace('m', ' ');
            typedTime.setTime(Integer.parseInt(t.trim()));
        } else {
            typedTime.setUnit("s");
            if (offset.endsWith("s")) {
                String t = offset.replace('s', ' ');
                typedTime.setTime(Integer.parseInt(t.trim()));
            } else
                typedTime.setTime(Integer.parseInt(offset.trim()));
        }
        return typedTime;
    }


    /**
     * Parses the Zone field string.
     * @return ZoneField 
     * @exception ParseException if a parsing error occurs
     */
    public ZoneField zoneField() throws ParseException {
        try {
            ZoneField zoneField = new ZoneField();


            lexer.match('z');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();

            // The zoneAdjustment list:
            while (lexer.lookAhead(0) != '\n') {
                ZoneAdjustment zoneAdjustment = new ZoneAdjustment();
                lexer.match(LexerCore.ID);
                Token time = lexer.getNextToken();
                lexer.SPorHT();
                zoneAdjustment.setTime(Long.parseLong(time.getTokenValue())); 

                lexer.match(LexerCore.ID);
                Token offset = lexer.getNextToken();
                lexer.SPorHT();
                String sign = getSign(offset.getTokenValue());
                TypedTime typedTime = getTypedTime(offset.getTokenValue());
                zoneAdjustment.setSign(sign);
                zoneAdjustment.setOffset(typedTime);

                zoneField.addZoneAdjustment(zoneAdjustment);
            }
            return zoneField;
        } catch (NumberFormatException e) {
            throw lexer.createParseException();
        }
    }

    /**
     * Parses the Zone field string.
     * @return ZoneField 
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return this.zoneField();
    }
}
