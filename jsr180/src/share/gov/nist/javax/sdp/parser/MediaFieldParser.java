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
import java.util.*;

/**
 * Parser for Media field.
 *
 * @version  JAIN-SDP-PUBLIC-RELEASE
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class MediaFieldParser extends SDPParser {

    /**
     * Creates new MediaFieldParser.
     * @param mediaField the media string to be parsed
     */
    public MediaFieldParser(String mediaField) {
        lexer = new Lexer("charLexer", mediaField);
    }

    /** Default constructor. */
    protected MediaFieldParser() {
        super();
    }

    /**
     * Perform the media type field parsing
     * @return the parsed media type field
     * @exception ParseException if a parsing error occurs
     */
    public MediaField mediaField() throws ParseException  {
        if (ParserCore.debug) dbg_enter("mediaField");
        try {
            MediaField mediaField = new MediaField();

            lexer.match('m');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();



            lexer.match(Lexer.ID);
            Token media = lexer.getNextToken();
            mediaField.setMedia(media.getTokenValue());
            lexer.SPorHT();

            lexer.match(Lexer.ID);
            Token port = lexer.getNextToken();
            mediaField.setPort(Integer.parseInt(port.getTokenValue()));

            lexer.SPorHT();

            // Some strange media formatting from Sun Ray systems with media
            if (lexer.hasMoreChars() && lexer.lookAhead(1) == '\n')
                return  mediaField;
            if (lexer.lookAhead(0) == '/') {
                // The number of ports is present:
                lexer.consume(1);
                lexer.match(Lexer.ID);
                Token portsNumber = lexer.getNextToken();
                mediaField.setNports(Integer.parseInt
                                     (portsNumber.getTokenValue()));
                lexer.SPorHT();
            }

            lexer.match(Lexer.ID);
            Token token = lexer.getNextToken();
            this.lexer.SPorHT();
            String transport = token.getTokenValue();
            if (lexer.lookAhead(0) == '/') {
                lexer.consume(1);
                lexer.match(Lexer.ID);
                Token transportTemp = lexer.getNextToken();
                transport = transport + "/" + transportTemp.getTokenValue();
                lexer.SPorHT();
            }

            mediaField.setProto(transport);

            // The formats list:
            Vector formatList = new Vector();
            while (lexer.hasMoreChars()) {
                if (lexer.lookAhead(0) == '\n' || lexer.lookAhead(0) == '\r')
                    break;
                lexer.SPorHT();
                // while(lexer.lookAhead(0) == ' ') lexer.consume(1);
                lexer.match(Lexer.ID);
                Token tok = lexer.getNextToken();
                lexer.SPorHT();
                String format = tok.getTokenValue().trim();
                if (! format.equals(""))
                    formatList.addElement(format);
            }
            mediaField.setFormats(formatList);

            return mediaField;
        } catch (NumberFormatException e) {
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        } finally {
            dbg_leave("mediaField");
        }
    }

    /**
     * Perform the media type field parsing
     * @return the parsed media type field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return mediaField();
    }
}
