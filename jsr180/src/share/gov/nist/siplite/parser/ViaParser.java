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
 * Parser for via headers.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ViaParser extends HeaderParser {
    /** Default constructor. */
    ViaParser() {}
    
    /**
     * Constructor with initial via header string.
     * @param via initial via header
     */
    public ViaParser(String via) {
        super(via);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    public ViaParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * A parser for the essential part of the via header.
     * @param v initial via header
     * @exception ParseException if a parsing error occurs
     */
    private void parseVia(ViaHeader v) throws ParseException {
        // The protocol
        lexer.match(TokenTypes.ID);
        Token protocolName = lexer.getNextToken();
        
        this.lexer.SPorHT();
        // consume the "/"
        lexer.match('/');
        this.lexer.SPorHT();
        lexer.match(TokenTypes.ID);
        this.lexer.SPorHT();
        Token protocolVersion = lexer.getNextToken();
        
        this.lexer.SPorHT();
        
        // We consume the "/"
        lexer.match('/');
        this.lexer.SPorHT();
        lexer.match(TokenTypes.ID);
        this.lexer.SPorHT();
        
        Token transport = lexer.getNextToken();
        this.lexer.SPorHT();
        
        Protocol protocol = new Protocol();
        protocol.setProtocolName(protocolName.getTokenValue());
        protocol.setProtocolVersion(protocolVersion.getTokenValue());
        protocol.setTransport(transport.getTokenValue());
        v.setSentProtocol(protocol);
        
        // sent-By
        HostNameParser hnp = new HostNameParser(this.getLexer());
        HostPort hostPort = hnp.hostPort();
        v.setSentBy(hostPort);
        
        // Ignore blanks
        this.lexer.SPorHT();
        
        // parameters
        while (lexer.lookAhead(0) == ';') {
            this.lexer.match(';');
            this.lexer.SPorHT();
            NameValue nameValue = this.nameValue();
            String name = nameValue.getName();
            nameValue.setName(name.toLowerCase());
            v.setParameter(nameValue);
            this.lexer.SPorHT();
        }
        
        if (lexer.lookAhead(0) == '(') {
            this.lexer.selectLexer("charLexer");
            lexer.consume(1);
            StringBuffer comment = new StringBuffer();
            boolean cond = true;
            while (true) {
                char ch = lexer.lookAhead(0);
                if (ch == ')') {
                    lexer.consume(1);
                    break;
                } else if (ch == '\\') {
                    // Escaped character
                    Token tok = lexer.getNextToken();
                    comment.append(tok.getTokenValue());
                    lexer.consume(1);
                    tok = lexer.getNextToken();
                    comment.append(tok.getTokenValue());
                    lexer.consume(1);
                } else if (ch == '\n') {
                    break;
                } else {
                    comment.append(ch);
                    lexer.consume(1);
                }
            }
            v.setComment(comment.toString());
        }
        
    }
    
    /**
     * Invokes parser for Via header field.
     * @return the parsed Via header
     * @exception ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        if (debug) dbg_enter("parse");
        try {
            ViaList viaList = new ViaList();
            // The first via header.
            this.lexer.match(TokenTypes.VIA);
            this.lexer.SPorHT(); // ignore blanks
            this.lexer.match(':'); // expect a colon.
            this.lexer.SPorHT(); // ingore blanks.
            
            while (true) {
                ViaHeader v = new ViaHeader();
                parseVia(v);
                viaList.add(v);
                this.lexer.SPorHT(); // eat whitespace.
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.consume(1); // Consume the comma
                    this.lexer.SPorHT(); // Ignore space after.
                }
                if (this.lexer.lookAhead(0) == '\n') break;
            }
            this.lexer.match('\n');
            return viaList;
        } finally {
            if (debug) dbg_leave("parse");
        }
        
    }
    
}
