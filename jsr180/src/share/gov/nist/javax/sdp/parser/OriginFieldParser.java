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
 * Origin field parser.
 * @version 1.0
 */
public class OriginFieldParser extends SDPParser {

    /** 
     * Creates new OriginFieldParser.
     * @param originField origin field to be parsed
     */
    public OriginFieldParser(String originField) {
        lexer = new Lexer("charLexer", originField);
    }

    /** Default constructor. */
    protected OriginFieldParser() {
        super();
    }

    /**
     * Perform the origin field parsing.
     * @return the parsed origin field
     * @exception ParseException if a parsing error occurs
     */
    public OriginField originField() throws ParseException  {
        try {
            OriginField originField = new OriginField();

            lexer.match('o');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token userName = lexer.getNextToken();
            originField.setUsername(userName.getTokenValue());
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token sessionId = lexer.getNextToken();
            originField.setSessId(Long.parseLong(sessionId.getTokenValue()));
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token sessionVersion = lexer.getNextToken();
            originField.setSessVersion(Long.parseLong(
                                            sessionVersion.getTokenValue()));
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token networkType = lexer.getNextToken();
            originField.setNettype(networkType.getTokenValue());
            this.lexer.SPorHT();

            lexer.match(LexerCore.ID);
            Token addressType = lexer.getNextToken();
            originField.setAddrtype(addressType.getTokenValue());
            this.lexer.SPorHT();

            String host = lexer.getRest();
            Lexer lexer = new Lexer("charLexer", host);
            HostNameParser hostNameParser = new HostNameParser(lexer);
            Host h = hostNameParser.host();
            originField.setAddress(h);

            return originField;
        } catch (SdpException e) {
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        } catch (NumberFormatException nfe) {
            throw new ParseException(lexer.getBuffer(), lexer.getPtr());
        }
    }

    /**
     * Perform the origin field parsing.
     * @return the parsed origin field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return originField();
    }

    /*    
    public static void main(String[] args) throws ParseException {
        String origin[] = {
    "o=4855 13760799956958020 13760799956958020 IN IP4 166.35.224.216\r\n",
        "o=mhandley 2890844526 2890842807 IN IP4 126.16.64.4\n",
    "o=UserB 2890844527 2890844527 IN IP4 everywhere.com\n",
    "o=UserA 2890844526 2890844526 IN IP4 here.com\n",
    "o=IFAXTERMINAL01 2890844527 2890844527 IN IP4 ift.here.com\n",
    "o=GATEWAY1 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n",
    "o=- 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n"
    };

    for (int i = 0; i < origin.length; i++) {
        OriginFieldParser originFieldParser =
        new OriginFieldParser(origin[i]);
        OriginField originField = originFieldParser.originField();
        System.out.println("toParse :" + origin[i]);
        System.out.println("encoded: " +originField.encode());
    }

    }
    */
}
