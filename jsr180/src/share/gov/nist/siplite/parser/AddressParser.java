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
import gov.nist.core.*;
import gov.nist.siplite.address.*;

/**
 * Parser for addresses.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class AddressParser extends Parser {
    /**
     * Constructor  with initial lexer engine.
     * @param lexer initial lexer engine to use
     */
    protected AddressParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("charLexer");
    }

    /**
     * Constructor with initial addres.
     * @param address initial address
     */
    public AddressParser(String address) {
        this.lexer = new Lexer("charLexer", address);
    }

    /**
     * Gets the address.
     * @return the parsed address
     */
    public Address address() throws ParseException  {
        Address retval = new Address();

        lexer.SPorHT();
        if (!lexer.hasMoreChars()) {
            throw createParseException("Empty address");
        }
        char next = lexer.lookAhead(0);
        // JSR 180: input string could be URL or display name <URL>
        // RFC 3261, ABNF:
        // display-name   =  *(token LWS)/ quoted-string
        String displayName = null;
        if (next == '*') { // "*" - stop parsing
            retval.setAddressType(Address.WILD_CARD);
            return retval;
        }
        if (next == '\"') { // quoted string
            displayName = "\"" + lexer.quotedString() + "\"";
            lexer.getString('<'); // skip till '<'
        } else if (lexer.getRest().indexOf('<') > -1) { // unquoted display name
            displayName = lexer.getString('<');
        }
        if (displayName != null) {
            try {
                retval.setDisplayName(displayName.trim());
            } catch (IllegalArgumentException exc) {
                throw createParseException("Wrong display name "+displayName);
            }
            retval.setAddressType(Address.NAME_ADDR);
        } else {
            retval.setAddressType(Address.ADDRESS_SPEC);
        }
        // Parsing URL
        lexer.SPorHT();
        URLParser uriParser = new URLParser((Lexer)lexer);
        URI uri = uriParser.uriReference();
        lexer.SPorHT();
        if (displayName != null) { // check for closing '>'
            lexer.match('>');
        }
        retval.setURI(uri);
        return retval;

    }
}
