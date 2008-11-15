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
 * Parser for Connection Field.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ConnectionFieldParser extends SDPParser {

    /**
     * Creates new ConnectionFieldParser.
     * @param connectionField connection string to be parsed
     */
    public ConnectionFieldParser(String connectionField) {
        this.lexer = new Lexer("charLexer", connectionField);
    }

    /** Default constructor. */
    protected ConnectionFieldParser() {
        super();
    }

    /**
     * Perform the connection address field parsing
     * @param address the connection address string to parse
     * @return the parsed connection address field
     */
    public ConnectionAddress connectionAddress(String address) {
        ConnectionAddress connectionAddress = new ConnectionAddress();

        int begin = address.indexOf("/");

        if (begin != -1) {
            connectionAddress.setAddress(new Host(address.substring(0, begin)));

            int middle = address.indexOf("/", begin+1);
            if (middle != -1) {
                String ttl = address.substring(begin+1, middle);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim()));

                String addressNumber = address.substring(middle+1);
                connectionAddress
                .setPort(Integer.parseInt(addressNumber.trim()));
            } else {
                String ttl = address.substring(begin+1);
                connectionAddress.setTtl(Integer.parseInt(ttl.trim()));
            }
        } else
            connectionAddress.setAddress(new Host(address));

        return connectionAddress;
    }

    /**
     * Perform the connection address field parsing
     * @return the parsed connection address field
     * @exception ParseException if a parsing error occurs
     */
    public ConnectionField connectionField() throws ParseException  {
        try {
            lexer.match('c');
            lexer.SPorHT();
            lexer.match('=');
            lexer.SPorHT();

            ConnectionField connectionField = new ConnectionField();

            lexer.match(LexerCore.ID);
            lexer.SPorHT();
            Token token = lexer.getNextToken();
            connectionField.setNettype(token.getTokenValue());

            lexer.match(LexerCore.ID);
            lexer.SPorHT();
            token = lexer.getNextToken();
            connectionField.setAddressType(token.getTokenValue());
            lexer.SPorHT();
            String rest = lexer.getRest();
            ConnectionAddress connectionAddress =
            connectionAddress(rest.trim());

            connectionField.setAddress(connectionAddress);

            return connectionField;
        } catch (SdpException e) {
            throw new ParseException(e.getMessage(), lexer.getPtr());
        }
    }

    /**
     * Perform the connection address field parsing
     * @return the parsed connection address field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return this.connectionField();
    }

}
