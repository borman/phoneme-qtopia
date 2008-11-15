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
 * Parser for Email Field.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class EmailFieldParser extends SDPParser {

    /**
     * Creates new EmailFieldParser.
     * @param emailField the email field to be parsed
     */
    public EmailFieldParser(String emailField) {
        lexer = new Lexer("charLexer", emailField);
    }

    /** Default constructor. */
    protected  EmailFieldParser() {
        super();
    }

    /**
     * Gets the user friendly display name.
     * @param rest the remaining string to be parsed
     * @return the display name
     */
    public String getDisplayName(String rest) {
        String retval = null;

        int begin = rest.indexOf("(");
        int end = rest.indexOf(")");

        if (begin != -1 && end != -1 && end > begin) {
            // e=mjh@isi.edu (Mark Handley)
            retval = rest.substring(begin+1, end);
        } else {
            // The alternative RFC822 name quoting convention 
            // is also allowed for
            // email addresses. ex: e=Mark Handley <mjh@isi.edu>
            begin = rest.indexOf("<");
            if (begin != -1) {
                retval = rest.substring(0, begin);
            } else {
                // There is no display name !!!
            }
        }
        return retval;
    }


    /**
     * Gets the email address.
     * @param rest the remaining string to be parsed
     * @return the email address
     */
    public Email getEmail(String rest) {
        Email email = new Email();

        int begin = rest.indexOf("(");
        try {
            if (begin != -1) {
                // e=mjh@isi.edu (Mark Handley)
                String emailTemp = rest.substring(0, begin);
                int i = emailTemp.indexOf("@");
                if (i != -1) {
                    email.setUserName(emailTemp.substring(0, i));
                    email.setHostName(emailTemp.substring(i+1));
                } else {
                    // Pb: the email is not well formatted
                }
            } else {
                // The alternative RFC822 name quoting convention is 
                // also allowed for
                // email addresses. ex: e=Mark Handley <mjh@isi.edu>
                int ind = rest.indexOf("<");
                int end = rest.indexOf(">");

                if (ind != -1) {
                    String emailTemp = rest.substring(ind+1, end);
                    int i = emailTemp.indexOf("@");
                    if (i != -1) {
                        email.setUserName(emailTemp.substring(0, i));
                        email.setHostName(emailTemp.substring(i+1));
                    } else {
                        // Pb: the email is not well formatted
                    }

                } else {
                    int i = rest.indexOf("@");
                    int j = rest.indexOf("\n");
                    if (i != -1) {
                        email.setUserName(rest.substring(0, i));
                        email.setHostName(rest.substring(i+1, j));
                    } else {
                        // Pb: the email is not well formatted
                    }
                }
            }
            return email;
        } catch (IndexOutOfBoundsException iobe) {
            return new Email();
        }
    }

    /**
     * Perform the email address field parsing
     * @return the parsed email address field
     * @exception ParseException if a parsing error occurs
     */
    public EmailField emailField() throws ParseException  {
        lexer.match('e');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        EmailField emailField = new EmailField();
        EmailAddress emailAddress = new EmailAddress();

        String rest = lexer.getRest();

        String displayName = getDisplayName(rest.trim());
        emailAddress.setDisplayName(displayName);
        Email email = getEmail(rest);
        emailAddress.setEmail(email);

        emailField.setEmailAddress(emailAddress);
        return emailField;
    }

    /**
     * Perform the email address field parsing
     * @return the parsed email address field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return this.emailField();
    }

}
