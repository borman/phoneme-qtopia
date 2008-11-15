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

import gov.nist.siplite.SIPConstants;
import gov.nist.siplite.header.*;
import gov.nist.core.*;
import java.util.*;

/**
 * Generic header parser class. The parsers for various headers extend this
 * class. To create a parser for a new header, extend this class and change
 * the createParser class.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class HeaderParser extends Parser {

    /**
     * Parses the weekday field.
     * @return an integer with the calendar content for wkday.
     */
    protected int wkday() throws ParseException {
        dbg_enter("wkday");
        try {
            String tok = lexer.ttoken();
            String id = tok.toLowerCase();

            if (Utils.equalsIgnoreCase(SIPConstants.TOKEN_DAY_MON, id))
                return Calendar.MONDAY;
            else if (Utils.equalsIgnoreCase
                    (SIPConstants.TOKEN_DAY_TUE, id)) return Calendar.TUESDAY;
            else if (Utils.equalsIgnoreCase(SIPConstants.TOKEN_DAY_WED, id))
                return Calendar.WEDNESDAY;
            else if (Utils.equalsIgnoreCase(SIPConstants.TOKEN_DAY_THU, id))
                return Calendar.THURSDAY;
            else if (Utils.equalsIgnoreCase
                    (SIPConstants.TOKEN_DAY_FRI, id))
                return Calendar.FRIDAY;
            else if (Utils.equalsIgnoreCase
                    (SIPConstants.TOKEN_DAY_SAT, id))
                return Calendar.SATURDAY;
            else if (Utils.equalsIgnoreCase
                    (SIPConstants.TOKEN_DAY_SUN, id)) return Calendar.SUNDAY;
            else
                throw createParseException("bad wkday");
        } finally {
            dbg_leave("wkday");
        }

    }

    /**
     * Parses and return a date field.
     * @return a date structure with the parsed value.
     */
    protected Calendar date() throws ParseException {
        String errorMsg = "bad date field";
        try {
            Calendar retval =
                    Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            String s1 = lexer.number();
            int day = Integer.parseInt(s1);
            if (day <= 0 || day >= 31)
                throw createParseException("Bad day ");
            retval.set(Calendar.DAY_OF_MONTH, day);
            lexer.match(' ');
            String month = lexer.ttoken().toLowerCase();
            if (month.equals("jan")) {
                retval.set(Calendar.MONTH, Calendar.JANUARY);
            } else if (month.equals("feb")) {
                retval.set(Calendar.MONTH, Calendar.FEBRUARY);
            } else if (month.equals("mar")) {
                retval.set(Calendar.MONTH, Calendar.MARCH);
            } else if (month.equals("apr")) {
                retval.set(Calendar.MONTH, Calendar.APRIL);
            } else if (month.equals("may")) {
                retval.set(Calendar.MONTH, Calendar.MAY);
            } else if (month.equals("jun")) {
                retval.set(Calendar.MONTH, Calendar.JUNE);
            } else if (month.equals("jul")) {
                retval.set(Calendar.MONTH, Calendar.JULY);
            } else if (month.equals("aug")) {
                retval.set(Calendar.MONTH, Calendar.AUGUST);
            } else if (month.equals("sep")) {
                retval.set(Calendar.MONTH, Calendar.SEPTEMBER);
            } else if (month.equals("oct")) {
                retval.set(Calendar.MONTH, Calendar.OCTOBER);
            } else if (month.equals("nov")) {
                retval.set(Calendar.MONTH, Calendar.NOVEMBER);
            } else if (month.equals("dec")) {
                retval.set(Calendar.MONTH, Calendar.DECEMBER);
            }
            lexer.match(' ');
            String s2 = lexer.number();
            int yr = Integer.parseInt(s2);
            retval.set(Calendar.YEAR, yr);
            return retval;

        } catch (ParseException ex) {
            throw createParseException(errorMsg);
        } catch (NumberFormatException nfe) {
            throw createParseException(errorMsg);
        }
    }

    /**
     * Sets the time field. This has the format hour:minute:second.
     * @param calendar input data for setting the time
     * @exception ParseException if a parsing error occurs
     */
    protected void time(Calendar calendar) throws ParseException {
        String errorMsg = "error processing time ";
        try {
            String s = lexer.number();
            int hour = Integer.parseInt(s);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            lexer.match(':');
            s = lexer.number();
            int min = Integer.parseInt(s);
            calendar.set(Calendar.MINUTE, min);
            lexer.match(':');
            s = lexer.number();
            int sec = Integer.parseInt(s);
            calendar.set(Calendar.SECOND, sec);
        } catch (ParseException ex) {
            throw createParseException(errorMsg);
        } catch (NumberFormatException nfe) {
            throw createParseException(errorMsg);
        }
    }

    /** Default constructor. */
    protected HeaderParser() { }

    /**
     * Sets the header to be parsed.
     * @param header the string to be processed.
     */
    public void setHeaderToParse(String header) throws ParseException {
        if (this.lexer == null)
            this.lexer = new Lexer("command_keywordLexer", header);
        else throw createParseException("header already set");
    }

    /**
     * Creates new HeaderParser.
     * @param header String to parse.
     */
    protected HeaderParser(String header) {
        this.lexer = new Lexer("command_keywordLexer", header);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected HeaderParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }

    /**
     * Parses the SIP header from the buffer and return a parsed
     * structure.
     * @return the parsed header value
     * @throws ParseException if there was an error parsing.
     */
    public Header parse()
            throws ParseException {
        String name = lexer.getNextToken(':');
        lexer.consume(1);
        String body = lexer.getLine().trim();
        // we dont set any fields because the header is
        // ok
        ExtensionHeader retval = new ExtensionHeader(name, body, body);
        return retval;

    }

    /**
     * Parses the header name until the colon and chew WS after that.
     * @param tok the separator token
     */
    protected void headerName(int tok) throws ParseException {
        this.lexer.match(tok);
        this.lexer.SPorHT();
        this.lexer.match(':');
        this.lexer.SPorHT();
    }
}
