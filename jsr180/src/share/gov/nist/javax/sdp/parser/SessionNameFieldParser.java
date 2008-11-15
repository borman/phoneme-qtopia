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
/*
 * SessionNameFieldParser.java
 *
 * Created on February 25, 2002, 10:26 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;

/**
 * Session name field parser.
 * @version 1.0
 */
public class SessionNameFieldParser extends SDPParser {

    /**
     * Creates new SessionNameFieldParser.
     * @param sessionNameField the session name filed to be parsed
     */
    public SessionNameFieldParser(String sessionNameField) {
        lexer = new Lexer("charLexer", sessionNameField);
    }

    /** Default constructor. */
    protected SessionNameFieldParser() { super(); }

    /**
     * Gets the Session Name Field.
     * @return SessionNameField
     * @exception  ParseException if a parsing exception occurs
     */
    public SessionNameField sessionNameField() throws ParseException  {
        lexer.match('s');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        SessionNameField sessionNameField = new SessionNameField();
        String rest = lexer.getRest(); 
        sessionNameField.setSessionName(rest.trim());

        return sessionNameField;
    }

    /**
     * Parses the current field.
     * @return the parsed session name field 
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return sessionNameField();
    }

    /*
    public static void main(String[] args) throws ParseException {
        String session[] = {
            "s=SDP Seminar \n",
                        "s= Session SDP\n"
                };

        for (int i = 0; i < session.length; i++) {
        SessionNameFieldParser sessionNameFieldParser =
            new SessionNameFieldParser(session[i]);
            SessionNameField sessionNameField=
            sessionNameFieldParser.sessionNameField();
        System.out.println("encoded: " +sessionNameField.encode());
        }

    }
    */
}
