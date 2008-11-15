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
import gov.nist.siplite.address.*;
import gov.nist.siplite.header.*;
import gov.nist.core.*;

/**
 * From header parser.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class FromParser extends AddressParametersParser {
    /** Default constructor. */
    FromParser() {}
    
    /**
     * Constructor with initial From header string.
     * @param from initial From header
     */
    public FromParser(String from) {
        super(from);
    }
    
    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected FromParser(Lexer lexer) {
        super(lexer);
    }
    
    /**
     * Invokes parser for From header field.
     * @return the parsed From header
     * @exception ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        
        FromHeader from = new FromHeader();
        
        this.lexer.match(TokenTypes.FROM);
        this.lexer.SPorHT();
        this.lexer.match(':');
        this.lexer.SPorHT();
        super.parse(from);
        this.lexer.match('\n');
        if (((Address)from.getAddress()).getAddressType() ==
                Address.ADDRESS_SPEC) {
            // the parameters are header parameters.
            if (from.getAddress().getURI() instanceof SipURI) {
                SipURI sipUri = (SipURI) from.getAddress().getURI();
                NameValueList parms = sipUri.getUriParms();
                if (parms != null && ! parms.isEmpty()) {
                    from.setParameters(parms);
                    sipUri.removeUriParms();
                }
            }
        }
        return from;
    }
}
