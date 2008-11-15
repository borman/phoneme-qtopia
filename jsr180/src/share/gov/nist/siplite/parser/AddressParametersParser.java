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
import  gov.nist.siplite.header.*;
import  gov.nist.siplite.address.*;
import  gov.nist.core.*;

/**
 *  Address parameters parser.
 *
 * @version  JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
class AddressParametersParser  extends ParametersParser {
    /** Current parsed address parameters. */
    protected AddressParametersHeader addressParametersHeader;
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer the character parsing engine
     */
    protected AddressParametersParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Constructor with initial buffer.
     * @param buffer data to be parsed
     */
    protected AddressParametersParser(String buffer) {
        super(buffer);
    }
    
    /** Default constructor. */
    protected AddressParametersParser() {}
    
    /**
     * Invokes the parsing engine.
     * @param addressParametersHeader out put of parsed parameters
     */
    protected void parse(AddressParametersHeader addressParametersHeader)
    throws ParseException {
        dbg_enter("AddressParametersParser.parse");
        try {
            this.addressParametersHeader  = addressParametersHeader;
            AddressParser addressParser = new AddressParser
                    (this.getLexer());
            Address addr = addressParser.address();
            addressParametersHeader.setAddress(addr);
            super.parse(addressParametersHeader);
        } finally {
            dbg_leave("AddressParametersParser.parse");
        }
        
    }
    
}

