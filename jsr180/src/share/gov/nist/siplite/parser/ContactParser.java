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

import gov.nist.siplite.header.*;
import gov.nist.core.*;

/**
 * A parser for The SIP contact header.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ContactParser extends AddressParametersParser {
    /** Default constructor. */
    ContactParser() {}
    
    /**
     * Constructor with initial contact string.
     * @param contact initial contact field to be processed.
     */
    public ContactParser(String contact) {
        super(contact);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ContactParser(Lexer lexer) {
        super(lexer);
        this.lexer = lexer;
    }
    
    /**
     * Invokes the parser for the contact header field.
     * @return theparsed contact header
     */
    public Header parse() throws ParseException {
        // past the header name and the colon.
        headerName(TokenTypes.CONTACT);
        ContactList retval = new ContactList();
        while (true) {
            ContactHeader contact = new ContactHeader();
            if (lexer.lookAhead(0) == '*') {
                this.lexer.match('*');
                contact.setWildCardFlag(true);
            } else super.parse(contact);
            retval.add(contact);
            this.lexer.SPorHT();
            if (lexer.lookAhead(0) == ',') {
                this.lexer.match(',');
                this.lexer.SPorHT();
            } else if (lexer.lookAhead(0) == '\n') break;
            else throw createParseException("unexpected char");
        }
        return retval;
    }
    /*
    public static void main(String args[]) throws ParseException {
    String contact[] = {
        "Contact:<sip:utente@127.0.0.1:5000;transport=TCP>;expires=3600\n",
        "Contact:BigGuy<sip:utente@127.0.0.1:5000>;expires=3600\n",
        "Contact: sip:4855@166.35.224.216:5060\n",
        "Contact: sip:user@host.company.com\n",
        "Contact: Bo Bob Biggs\n"+
        "< sip:user@example.com?Route=%3Csip:sip.example.com%3E >\n",
        "Contact: Joe Bob Briggs <sip:mranga@nist.gov>\n",
        "Contact: \"Mr. Watson\" <sip:watson@worcester.bell-telephone.com>"+
        " ; q=0.7; expires=3600,\"Mr. Watson\" "
        + "<mailto:watson@bell-telephone.com>"+
        ";q=0.1\n",
        "Contact: LittleGuy <sip:UserB@there.com;user=phone>"+
        ",<sip:+1-972-555-2222@gw1.wcom.com;user=phone>,"
        + "tel:+1-972-555-2222"+
        "\n",
        "Contact:*\n",
        "Contact:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n"
    };
     
    for (int i = 0; i < contact.length; i++) {
        ContactParser cp =
        new ContactParser(contact[i]);
        ContactList cl = (ContactList) cp.parse();
        ContactHeader c = (ContactHeader) cl.getFirst();
        System.out.println("encoded = " + c.toString());
        System.out.println("address = " + c.getAddress());
        System.out.println();
    }
    }
    */
}


