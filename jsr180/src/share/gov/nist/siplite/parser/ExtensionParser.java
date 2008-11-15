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

import java.util.Stack;
import gov.nist.core.*;
import gov.nist.siplite.header.*;

/**
 * Parser for content type header.
 *
 */
public class ExtensionParser extends ParametersParser {
    /** Default constructor. */
    protected ExtensionParser() {}

    /**
     * Constructor with header value.
     * @param value full header value respresented as a string
     */
    public ExtensionParser(String value) {
        super(value);
    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    protected ExtensionParser(Lexer lexer) {
        super(lexer);
    }

    /**
     * Searches a given string for a given character and returns its index.
     * This function should be used instead of String.indexOf(char) to handle
     * the cases like <br>
     * SomeHeader: "Token1; Token2" &lt;sip:something;sip_param&gt;param=value
     * <br>
     * It will return an index of the ';' located before the "param=value".
     * IMPL_NOTE: optimize and think about moving it to Lexer.
     * @param buffer a string that will be searched for the delimiter
     * @param delimiter a character to look for
     * @return an index of the delimiter of -1 if it was not found
     */
    private int getDelimiterIndex(String buffer, char delimiter) {
        Stack stack = new Stack();
        char ch, top;

        for (int i = 0; i < buffer.length(); i++) {
            ch = buffer.charAt(i);

            if (!stack.empty()) {
                top = ((Character)stack.peek()).charValue();
            } else {
                top = '\0';
            }

            if (ch == '<') {
                if (top != '"') {
                    stack.push(new Character(ch));
                }
                continue;
            }

            if (ch == '>') {
                if (top == '<') {
                    stack.pop();
                }
                continue;
            }

            if (ch == '"') {
                if (top == '"') {
                    stack.pop();
                } else {
                    stack.push(new Character(ch));
                }
                continue;
            }

            if (!stack.empty()) {
                continue;
            }

            if (ch == delimiter) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Invokes parser for extension header field.
     * IMPL_NOTE: optimize it (maybe use ParserCore.nameValue()?).
     * @return the parsed extension header
     * @throws ParseException if a parsing error occurs
     */
    public Header parse() throws ParseException {
        String name = lexer.getNextToken(':');
        lexer.consume(1);
        lexer.SPorHT();
        String bodyWithParam = "";
        String rest = lexer.getRest();
        if (rest != null) {
            bodyWithParam = StringTokenizer.convertNewLines(rest.trim());
        }
        char colonDelimiter = ';';
        int index = getDelimiterIndex(bodyWithParam, colonDelimiter);

        if (index == -1) {
            // no parameters were specified
            // String body = lexer.getLine().trim();
            ExtensionHeader retval = new ExtensionHeader(name,
                bodyWithParam, bodyWithParam);
            return retval;
        } else {
            int currPos = lexer.getPtr();
            String body = StringTokenizer.convertNewLines(
                lexer.getBuffer().substring(currPos, currPos + index).trim());
            lexer.consume(index + 1);
            lexer.SPorHT();

            ExtensionHeader retval = new
                ExtensionHeader(name, bodyWithParam, body);

            String  paramName, paramVal;
            boolean headerEnd = false;
            int     eqIndex, semicolonIndex;

            while (!headerEnd) {
                paramName = lexer.peekLine().trim();

                eqIndex = paramName.indexOf('=');
                semicolonIndex = paramName.indexOf(colonDelimiter);

                if ((eqIndex != -1) &&
                    (semicolonIndex > eqIndex || semicolonIndex == -1)) {
                    // parameter with a value
                    paramName = lexer.getString('=');

                    // take the rest of the line
                    paramVal = lexer.peekLine().trim();

                    // IMPL_NOTE: remove !!!
                    // System.out.println("*** Rest is '" + paramVal + "'");

                    if (paramVal.indexOf(colonDelimiter) != -1) {
                        paramVal = lexer.getString(colonDelimiter);
                    } else {
                        paramVal = lexer.getLine().trim();
                        headerEnd = true;
                    }
                } else {
                    if (semicolonIndex != -1) {
                        // parameter without a value
                        paramName = lexer.getString(colonDelimiter);
                    } else {
                        // the last parameter without a value
                        paramName = lexer.getLine().trim();
                        headerEnd = true;
                    }

                    paramVal = "";
                }

                paramName = paramName.trim();
                paramVal  = paramVal.trim();

                // IMPL_NOTE: remove !!!
                // System.out.println("*** paramName = " + paramName);

                // IMPL_NOTE: remove !!!
                // System.out.println(">>> Adding '" +
                //     paramName + "' = '" + paramVal + "'");

                // The following toLowerCase() call is required because
                // some 'known' headers (like Accept-Language) haven't
                // corresponding implementation classes, but
                // ExtensionHeader class is used to represent them.
                //
                // So, here we have to care about 'known' parameters for
                // the mentioned headers. For example, a parameter 'q'
                // must be case-insensitive.
                paramName = paramName.toLowerCase();

                // Validate parameter's name and value
                if (!Lexer.isValidName(paramName)) {
                    throw new ParseException("Invalid parameter's name.", 0);
                }

                if (!Lexer.isValidParameterValue(paramVal)) {
                    throw new ParseException("Invalid parameter's value.", 0);
                }

                if (retval.getParameter(paramName) != null) {
                    throw new ParseException(
                        "Duplicated parameter: " + paramName, 0);
                }

                retval.setParameter(paramName, paramVal);
            } // end while()

            return retval;
        }
    }
}
