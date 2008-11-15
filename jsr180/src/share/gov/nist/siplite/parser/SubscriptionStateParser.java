/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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
import gov.nist.siplite.header.*;

/**
 * Parser for Subscription State header.
 */
public class SubscriptionStateParser extends ParametersParser {
    /** Default constructor. */
    protected SubscriptionStateParser() {}

    /**
     * Constructor with header value.
     * @param value full header value respresented as a string
     */
    public SubscriptionStateParser(String value) {
        super(value);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected SubscriptionStateParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * Invokes parser for Subscription State header field.
     * @return the parsed Subscription State header
     * @throws ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        // A parser for this header should be the same as for any
        // generic header, so use the parser from ExtensionParser.
        Header h = new ExtensionParser(lexer.getBuffer()).parse();

        SubscriptionStateHeader retval = null;
        try {
            retval = new SubscriptionStateHeader(h.getValue().toString());
        } catch (IllegalArgumentException iae) {
            throw new ParseException(iae.getMessage(), 0);
        }

        // Copy parameters from the extension header.
        NameValueList parameterList = ((ParametersHeader)h).getParameters();
        if (parameterList != null && parameterList.size() > 0) {
            retval.setParameters(parameterList);
        }

        // System.out.println(">>> parse(): " + retval);

        return retval;
    }
}
