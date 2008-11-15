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
/*
 * TimeFieldParser.java
 *
 * Created on February 25, 2002, 9:58 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
/**
 * Time field parser.
 * @version 1.0
 */
public class TimeFieldParser extends SDPParser {

    /**
     * Creates new TimeFieldParser.
     * @param timeField the time field to be parsed
     */
    public TimeFieldParser(String timeField) {
        lexer = new Lexer("charLexer", timeField);
    }

    /** Default constructor. */   
    protected TimeFieldParser() {
         super(); 
    }


    /**
     * Gets the typed time.
     * @param tokenValue to set
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
     * Gets the time value.
     * @return the time value
     * @exception if a parsing error occurs
     */
    private long getTime() throws ParseException {
        try {
            String startTime = this.lexer.number();
            return Long.parseLong(startTime);
        } catch (NumberFormatException ex) {
            throw  lexer.createParseException();
        }

    }

    /**
     * Parses the field string.
     * @return TimeField 
     * @exception ParseException is a parsing error occurs
     */
    public TimeField timeField() throws ParseException  {
        lexer.match('t');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        TimeField timeField = new TimeField();

        long st = this.getTime();
        timeField.setStartTime(st);
        lexer.SPorHT();

        st = this.getTime();
        timeField.setStopTime(st);

        return timeField;
    }

    /**
     * Parses the field string.
     * @return TimeField 
     * @exception ParseException is a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return timeField();
    }

}
