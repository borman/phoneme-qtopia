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
 * Bandwidth field parser.
 * @version 1.0
 */
public class BandwidthFieldParser extends SDPParser {

    /** 
     * Creates new BandwidthFieldParser.
     * @param bandwidthField the bandwidth string to be parsed
     */
    public BandwidthFieldParser(String bandwidthField) {
        lexer = new Lexer("charLexer", bandwidthField);
    }

    /** Default constructor. */
    protected BandwidthFieldParser() {
        super();
    }

    /**
     * Perform the bandwidth field parsing
     * @return the parsed bandwidth field
     * @exception ParseException if a parsing error occurs
     */
    public BandwidthField bandwidthField() throws ParseException  {
        lexer.match('b');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        BandwidthField bandwidthField = new BandwidthField();

        NameValue nameValue = nameValue(':');
        String name = nameValue.getName();
        String value = (String) nameValue.getValue();

        bandwidthField.setBandwidth(Integer.parseInt(value.trim()));
        bandwidthField.setBwtype(name);

        lexer.SPorHT();
        return bandwidthField;
    }

    /**
     * Perform the bandwidth field parsing
     * @return the parsed bandwidth field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException { 
        return bandwidthField();
    }

}
