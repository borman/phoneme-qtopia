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
 *  Parser for Proto Version.
 *
 *@version  JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProtoVersionFieldParser extends SDPParser {

    /**
     * Creates new ProtoVersionFieldParser.
     * @param protoVersionField the protocol version field to be parsed
     */
    public ProtoVersionFieldParser(String protoVersionField) {
        lexer = new Lexer("charLexer", protoVersionField);
    }

    /** Default constructor. */
    protected ProtoVersionFieldParser() {
        super();
    }


    /**
     * Perform the protocol version field parsing
     * @return the parsed protocol version field
     * @exception ParseException if a parsing error occurs
     */
    public ProtoVersionField protoVersionField() throws ParseException  {
        try {
            lexer.match('v');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();

            ProtoVersionField protoVersionField = new ProtoVersionField();
            lexer.match(Lexer.ID);
            Token version = lexer.getNextToken();
            protoVersionField.setProtoVersion(
                               Integer.parseInt(version.getTokenValue()));
            lexer.SPorHT();

            return protoVersionField;
        } catch (NumberFormatException e) {
            throw lexer.createParseException();
        }
    }

    /**
     * Perform the protocol version field parsing
     * @return the parsed protocol version field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return protoVersionField();
    }


}
