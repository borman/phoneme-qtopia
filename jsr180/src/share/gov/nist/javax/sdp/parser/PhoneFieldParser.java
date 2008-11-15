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
 * Parser for the Phone field.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class PhoneFieldParser extends SDPParser {

    /**
     * Creates new PhoneFieldParser.
     * @param phoneField the phone field to be parsed
     */
    public PhoneFieldParser(String phoneField) {
        lexer = new Lexer("charLexer", phoneField);
    }

    /** Default constructor. */
    protected PhoneFieldParser() {
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
            // p=+44-171-380-7777 (Mark Handley)
            retval = rest.substring(begin+1, end);
        } else {
            // The alternative RFC822 name quoting convention is 
            // also allowed for
            // email addresses. ex: p=Mark Handley <+44-171-380-7777>
            int ind = rest.indexOf("<");
            if (ind != -1) {
                retval = rest.substring(0, ind);
            } else {
                // There is no display name !!!
            }
        }

        return retval;
    }

    /**
     * Gets the phone number.
     * @param rest the remaining string to be parsed
     * @return the phone number
     */
    public String getPhoneNumber(String rest) {
        String phoneNumber = null;    

        int begin = rest.indexOf("(");

        if (begin != -1) {
            // p=+44-171-380-7777 (Mark Handley)
            phoneNumber = rest.substring(0, begin).trim();
        } else {
            // The alternative RFC822 name quoting convention is
            // also allowed for email addresses. ex: p=Mark
            // Handley <+44-171-380-7777>
            int ind = rest.indexOf("<");
            int end = rest.indexOf(">");

            if (ind != -1 && end != -1 && end > ind) {
                phoneNumber = rest.substring(ind+1, end);
            } else {
                // p=+44-171-380-7777
                phoneNumber = rest.trim();
            }
        }
        return phoneNumber;
    }

    /**
     * Perform the phone number field parsing
     * @return the parsed phone number field
     * @exception ParseException if a parsing error occurs
     */
    public PhoneField phoneField() throws ParseException  {
        lexer.match('p');
        lexer.SPorHT();
        lexer.match('=');
        lexer.SPorHT();

        PhoneField phoneField = new PhoneField();
        String rest = lexer.getRest();

        String displayName = getDisplayName(rest.trim());
        phoneField.setName(displayName);
        String phoneNumber = getPhoneNumber(rest);
        phoneField.setPhoneNumber(phoneNumber);

        return phoneField;
    }

    /**
     * Perform the phone number field parsing
     * @return the parsed phone number field
     * @exception ParseException if a parsing error occurs
     */
    public SDPField parse() throws ParseException {
        return phoneField();
    }

}
