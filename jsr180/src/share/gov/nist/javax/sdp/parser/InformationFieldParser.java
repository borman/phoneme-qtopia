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
 * InformationFieldParser.java
 *
 * Created on February 19, 2002, 5:28 PM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
/**
 * Information field parser.
 * @version 1.0
 */
public class InformationFieldParser extends SDPParser {

    /**
     * Creates new InformationFieldParser.
     * @param informationField the initial information field 
     */
    public InformationFieldParser(String informationField) {
        lexer = new Lexer("charLexer", informationField);
    }

    /** Default constructor. */
    protected InformationFieldParser() {
        super();
    }

    /**
     * Parse the information field.
     * @return the parsed information field
     * @exception ParseException if a parsing error occurs
     */    
    public InformationField informationField() throws ParseException  {
        lexer.match('i');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        InformationField informationField = new InformationField();
        String rest = lexer.getRest(); 
        informationField.setInformation(rest.trim());

        return informationField;
    }

    /**
     * Perform the information field parsing
     * @return the parsed information field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException { 
        return informationField();
    }
}
