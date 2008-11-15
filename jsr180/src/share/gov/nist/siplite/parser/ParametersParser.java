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
 * Parameters parser header.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public abstract class ParametersParser extends HeaderParser {
    /** Current parsed parameters from the header line. */
    protected ParametersHeader parametersHeader;

    /** Default constructor. */
    protected ParametersParser() {}

    /**
     * Constructor with initial parameters header string.
     * @param buffer initial parameters header
     */
    protected ParametersParser(String buffer) {
        super(buffer);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ParametersParser(Lexer lexer) {
        super((Lexer)lexer);
    }

    /**
     * Invokes parser for header field parameters.
     * @param parametersHeader the parsed header field parameters
     * @exception ParseException if a parsing error occurs
     */
    protected void parse(ParametersHeader parametersHeader)
    throws ParseException {
        this.lexer.SPorHT();
        while (lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            // this.lexer.match(';');
            // eat white space
            this.lexer.SPorHT();
            NameValue nv = nameValue();

            // RFC 3261, p. 32:
            // Even though an arbitrary number of parameter pairs may be
            // attached to a header field value, any given parameter-name
            // MUST NOT appear more than once.
            if (parametersHeader.hasParameter(nv.getName())) {
                throw new ParseException("Duplicated parameter: " +
                    nv.getName(), 0);
            }

            parametersHeader.setParameter(nv);

            // eat white space
            this.lexer.SPorHT();
        }
    }

}
