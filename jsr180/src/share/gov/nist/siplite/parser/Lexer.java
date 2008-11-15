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

import gov.nist.siplite.SIPConstants;
import gov.nist.siplite.message.Request;
import gov.nist.siplite.header.*;
import gov.nist.core.*;
import java.util.Hashtable;

/**
 * Lexer class for the parser.
 *
 *@version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Lexer extends LexerCore {
    /**
     * Constructor with initial lecername and buffer to
     * process.
     * @param lexerName lexer for processing
     * @param buffer data to be parsed
     */
    public Lexer(String lexerName, String buffer) {
        super(lexerName, buffer);
        this.selectLexer(lexerName);
    }

    /**
     * Gets the header name of the line.
     * @param line the text to be parsed
     * @return the header name
     */
    public static String getHeaderName(String line) {
        if (line == null) {
            return null;
        }
        String headerName = null;
        try {
            int begin = line.indexOf(":");
            headerName = null;
            if (begin >= 1) {
                headerName = line.substring(0, begin);
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return headerName;
    }

    /**
     * Gets the header value of the line.
     * @param line the text to be parsed
     * @return the header value
     */
    public static String getHeaderValue(String line) {
        if (line == null) {
            return null;
        }
        String headerValue = null;
        try {
            int begin = line.indexOf(":");
            headerValue = line.substring(begin + 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return headerValue;
    }

    /**
     * Checks if the given string is a valid method/header/parameter name.
     * @param name the text to be parsed
     * @return true if the string is a valid name, false otherwise
     */
    public static boolean isValidName(String name) {
        // RFC 3261, p.p. 225, 221:
        //
        // Method = INVITEm / ACKm / OPTIONSm / BYEm
        //          / CANCELm / REGISTERm
        //          / extension-method
        // extension-method = token
        // token = 1*(alphanum / "-" / "." / "!" / "%" / "*"
        //          / "_" / "+" / "`" / "'" / "~" )
        // alphanum = ALPHA / DIGIT
        //
        // p.227:
        // generic-param  =  token [ EQUAL gen-value ]
        //
        // p. 232:
        // extension-header = header-name HCOLON header-value
        // header-name      = token
        //
        if (name == null || name.length() == 0) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);

            if (!isValidChar(ch)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given string is a valid header/parameter value.
     * @param value the text to be parsed
     * @param isParameter true if the value to be checked is a parameter
     * value, false otherwise
     * @return true if the string is a valid value, false otherwise
     */
    protected static boolean isValidValue(String value, boolean isParameter) {

        // System.out.println(">>> value = " + value);

        if (value == null) {
            value = ""; // null is a valid parameter value
        }

        // Check that the value doesn't contain unescaped semicolons
        boolean isEscaped = false;
        boolean isQuoteOn = false;
        boolean isBracketOn = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            // Ignore escaped (with preceding '\') characters
            if (isEscaped) {
                isEscaped = false;
                continue;
            }

            // Ignore characters that are a part of the string (inside qoutes)
            if (ch == '"') {
                isQuoteOn = !isQuoteOn;
                continue;
            }

            if (isQuoteOn) {
                continue;
            }

            if (ch == '\\') {
                isEscaped = true;
                continue;
            }

            // Ignore characters inside "<" and ">"
            if (isBracketOn) {
                if (ch == '>') {
                    isBracketOn = false;
                    continue;
                }
            } else {
                if (ch == '<') {
                    isBracketOn = true;
                    continue;
                }

                if (isParameter) {
                    // Restrictions on a parameter's value are more strict
                    // when header's value may be almost any text.
                    if (!isValidChar(ch)) {
                        return false;
                    }
                } else {
                    if (ch == ';') {
                        return false;
                    }
                }
            }
        }

        // System.out.println(">>> VALID");
        return true;
    }

    /**
     * Checks if the given string is a valid header value.
     * @param value the text to be parsed
     * @return true if the string is a valid value, false otherwise
     */
    public static boolean isValidHeaderValue(String value) {
        return isValidValue(value, false);
    }

    /**
     * Checks if the given string is a valid parameter value.
     * @param value the text to be parsed
     * @return true if the string is a valid value, false otherwise
     */
    public static boolean isValidParameterValue(String value) {
        return isValidValue(value, true);
    }

    /**
     * Checks if the given string is valid as user part of a SIP(S)-URI.
     *
     * @param name the text to be parsed
     * @return true if the string is a valid name, false otherwise
     */
    public static boolean isValidUserName(String name) {
        // RFC3261 p.222
        // user             =  1*( unreserved / escaped / user-unreserved )
        // user-unreserved  =  "&" / "=" / "+" / "$" / "," / ";" / "?" / "/"
        // p.219
        // alphanum  =  ALPHA / DIGIT
        // p.220
        // unreserved  =  alphanum / mark
        // mark        =  "-" / "_" / "." / "!" / "~" / "*" / "'" / "(" / ")"
        // escaped     =  "%" HEXDIG HEXDIG
        //

        if ((name == null) || (name.length() == 0)) {
            return true;
        }

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);

            if (URLParser.isUnreserved(ch) ||
                isEscaped(name, i) ||
                URLParser.isUserUnreserved(ch)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }


    /**
     * Checks if the given string is valid display name.
     *
     * @param displayName the text to be parsed
     * @return true if the string is a valid display name, false otherwise
     */
    public static boolean isValidDisplayName(String displayName) {
        // RFC 3261 p.228
        // display-name   =  *(token LWS)/ quoted-string
        // p.220
        // LWS  =  [*WSP CRLF] 1*WSP ; linear whitespace
        // UTF8-NONASCII   =  %xC0-DF 1UTF8-CONT
        //                 /  %xE0-EF 2UTF8-CONT
        //                 /  %xF0-F7 3UTF8-CONT
        //                 /  %xF8-Fb 4UTF8-CONT
        //                 /  %xFC-FD 5UTF8-CONT
        // UTF8-CONT       =  %x80-BF
        // p.221
        // token       =  1*(alphanum / "-" / "." / "!" / "%" / "*"
        //                      / "_" / "+" / "`" / "'" / "~" )
        // p.222
        // quoted-string  =  SWS DQUOTE *(qdtext / quoted-pair ) DQUOTE
        // qdtext         =  LWS / %x21 / %x23-5B / %x5D-7E
        //                   / UTF8-NONASCII
        // quoted-pair  =  "\" (%x00-09 / %x0B-0C
        //                 / %x0E-7F)
        if (null == displayName) {
            return false;
        }
        boolean quoted = false;
        displayName = StringTokenizer.convertNewLines(displayName);
        displayName = displayName.trim();
        int i = 0;
        if ('"' == displayName.charAt(0)) {
            quoted = true;
            i++;
        }
        while (i < displayName.length()) {
            char ch = displayName.charAt(i);
            if (!quoted) {
                if (!isValidChar(ch) && ch != ' ' && ch != 0x09) {
                    return false;
                }
            } else {
                // left UTF8-NONASCII proper converting on i18n subsystem
                if (ch < 0x20 ||
                    (ch == '"' && i != displayName.length() - 1) ||
                    (ch > 0x7E && ch < 0xC0)) {
                    return false;
                }
                if (ch == '\\') {
                    if (isQuotedPair(displayName, i)) {
                        i++;
                    } else {
                        return false;
                    }
                }
            }
            i++;
        }
        return true;
    }

    /**
     * Checks if the given string is valid scheme name.
     *
     * @param scheme the text to be parsed
     * @return true if the string is a valid scheme name, false otherwise
     */
    public static boolean isValidScheme(String scheme) {
        // RFC3261 p.224
        // scheme         =  ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        if (null == scheme || 
            0 ==  scheme.length() || 
            !isAlpha(scheme.charAt(0))) {
            return false;
        }
        char ch;
        for (int i = 1; i < scheme.length(); i++) {
            ch = scheme.charAt(i);
            if (!Character.isDigit(ch)   &&
                !isAlpha(ch)             &&
                ch != '+'                &&
                ch != '-'                &&
                ch != '.') {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given string is valid IPv6Address.
     * 
     * BNF (RFC3261, p. 223, 232):
     *     IPv4address  =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
     *     IPv6address  =  hexpart [ ":" IPv4address ]
     *     hexpart      =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
     *     hexseq       =  hex4 *( ":" hex4)
     *     hex4         =  1*4HEXDIG
     * 
     * @param address the text to be parsed
     * @return true if the string is a valid IPv6Address, false otherwise
     */
    public static boolean isValidIpv6Address(String address) {
        char ch;
        if (address == null || 0 == address.length()) {
            return false;
        }
        int  len = address.length();
        int  colonCount = 0, hexdigCount = 0;

        for (int i = 0; i < len; i++) {
            ch = address.charAt(i);

            if (ch == ':') {
                colonCount++;
                continue;
            }

            if (ch == '.') {
                int colonPos = address.lastIndexOf(':', i);
                if (colonPos > 0) {
                    return isValidIpv4Address(address.substring(colonPos + 1));
                } else {
                    return false;
                }
            }

            if (hexdigCount > 4 || colonCount > 2) {
                return false;
            }

            colonCount = 0;

            // Check for IP v6:
            // hex digit?
            if (isHexDigit(ch)) {
                hexdigCount++;
                continue;
            }

            if (hexdigCount > 0) {
                // Hex part must be followed by ":", "::" or by the end
                // of address. '.' means IP v6 address.
                if ((i < len-1) && (ch != ':') && (ch != '.')) {
                    return false;
                }
            }

            hexdigCount = 0;

            // Check for IP v4.
            if (!(Character.isDigit(ch) || (ch == '.'))) {
                return false;
            }
        } // end for

        // report about wrong address "::::::"
        //  and "::44444
        if (hexdigCount > 4 || colonCount > 2) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given string is valid IPv4Address.
     * 
     * BNF (RFC3261, p. 223, 232):
     *     IPv4address  =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
     * 
     * @param address the text to be parsed
     * @return true if the string is a valid IPv4Address, false otherwise
     */
    public static boolean isValidIpv4Address(String address) {
        char ch;
        if (address == null || 0 == address.length()) {
            return false;
        }
        int  len = address.length();
        int  pointCount = 0, digitCount = 0;
        int totalPoint = 0;
        for (int i = 0; i < len; i++) {
            ch = address.charAt(i);

            if (ch == '.') {
                if (i == len - 1) {
                    return false;
                }
                pointCount++;
                totalPoint++;
                digitCount = 0;
                continue;
            }

            if (digitCount > 3 || pointCount > 1) {
                return false;
            }

            pointCount = 0;

            if (!isDigit(ch)) {
                return false;
            } else {
                digitCount++;
            }
        } // end for

        if (totalPoint != 3) {
            return false;
        }
        return true;
    }


    /**
     * Checks if the given string is valid hostname 
     *
     * BNF(RFC3261 p.222)
     * hostname         =  *( domainlabel "." ) toplabel [ "." ]
     * domainlabel      =  alphanum
     *                     / alphanum *( alphanum / "-" ) alphanum
     * toplabel         =  ALPHA / ALPHA *( alphanum / "-" ) alphanum 
     * 
     * @param address the text to be parsed
     * @return true if the string is a valid hostname,
     *         false otherwise
     */
    public static boolean isValidHostname(String address) {
        if (address == null || 0 == address.length()) {
            return false;
        }
        int pCount = 0;
        boolean isHostname = false;
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (c == '.' ||
                isAlpha(c) ||
                c == '-' ||
                isDigit(c)) {
                continue;
            } else {
                return false;
            }
        }

        int lastPointPos = address.lastIndexOf('.');
        String toplabel;
        if (lastPointPos == address.length() - 1) {
            if (0 == lastPointPos) {
                // address is "."
                return false;
            }
            // get the previous point position
            // or -1
            lastPointPos  = address.lastIndexOf('.', lastPointPos - 1);
        }
        // if there is no previous point toplabel equals whole string
        toplabel = address.substring(lastPointPos + 1);
        if (!isAlpha(toplabel.charAt(0))) {
            return false;
        }
        return true;
    }

    /**
     * Selects the lexer to used based
     * on the current parsing context.
     * @param lexerName the lexer engine
     */
    public void selectLexer(String lexerName) {
        currentLexer = (Hashtable) lexerTables.get(lexerName);
        this.currentLexerName = lexerName;

        /*
         * 'SIP'/'SIPS' keywords are added to the keyword list
         * for all lexers except "command_keywordLexer" and
         * "method_keywordLexer" according to the RFC 3261:
         *
         * For "status_lineLexer" (p. 225):
         *
         *     Response  =  Status-Line
         *                  *( message-header )
         *                  CRLF
         *                  [ message-body ]
         *     Status-Line  =  SIP-Version SP Status-Code SP Reason-Phrase CRLF
         *
         * For "request_lineLexer" (p. 223):
         *
         *     Request  =  Request-Line
         *                 *( message-header )
         *                 CRLF
         *                 [ message-body ]
         * Request-Line  =  Method SP Request-URI SP SIP-Version CRLF
         *
         * For "sip_urlLexer" (p. 222):
         *
         *     SIP-URI  =  "sip:" [ userinfo ] hostport
         *                 uri-parameters [ headers ]
         */

        if (currentLexer == null) {
            addLexer(lexerName);
            if (lexerName.equals("method_keywordLexer")) {
                addKeyword(Request.REGISTER.toUpperCase(),
                           TokenTypes.REGISTER);
                addKeyword(Request.ACK.toUpperCase(),
                           TokenTypes.ACK);
                addKeyword(Request.OPTIONS.toUpperCase(),
                           TokenTypes.OPTIONS);
                addKeyword(Request.BYE.toUpperCase(),
                           TokenTypes.BYE);
                addKeyword(Request.INVITE.toUpperCase(),
                           TokenTypes.INVITE);
                addKeyword(Request.SUBSCRIBE.toUpperCase(),
                           TokenTypes.SUBSCRIBE);
                addKeyword(Request.NOTIFY.toUpperCase(),
                           TokenTypes.NOTIFY);
                addKeyword(Request.MESSAGE.toUpperCase(),
                           TokenTypes.MESSAGE);
                addKeyword(Request.PUBLISH.toUpperCase(),
                           TokenTypes.PUBLISH);
                addKeyword(Request.REFER.toUpperCase(),
                           TokenTypes.REFER);
                addKeyword(Request.INFO.toUpperCase(),
                           TokenTypes.INFO);
                addKeyword(Request.UPDATE.toUpperCase(),
                           TokenTypes.UPDATE);
            } else if (lexerName.equals("command_keywordLexer")) {
                addKeyword(Header.FROM.toUpperCase(),
                           TokenTypes.FROM); // 1
                addKeyword(Header.TO.toUpperCase(),
                           TokenTypes.TO); // 2
                addKeyword(Header.VIA.toUpperCase(),
                           TokenTypes.VIA); // 3
                addKeyword(Header.ROUTE.toUpperCase(),
                           TokenTypes.ROUTE); // 4
                addKeyword(Header.MAX_FORWARDS.toUpperCase(),
                           TokenTypes.MAX_FORWARDS); // 5
                addKeyword(Header.AUTHORIZATION.toUpperCase(),
                           TokenTypes.AUTHORIZATION); // 6
                addKeyword(Header.PROXY_AUTHORIZATION.toUpperCase(),
                           TokenTypes.PROXY_AUTHORIZATION); // 7
                addKeyword(Header.DATE.toUpperCase(),
                           TokenTypes.DATE); // 8
                addKeyword(Header.CONTENT_ENCODING.toUpperCase(),
                           TokenTypes.CONTENT_ENCODING); // 9
                addKeyword(Header.CONTENT_LENGTH.toUpperCase(),
                           TokenTypes.CONTENT_LENGTH); // 10
                addKeyword(Header.CONTENT_TYPE.toUpperCase(),
                           TokenTypes.CONTENT_TYPE); // 11
                addKeyword(Header.CONTACT.toUpperCase(),
                           TokenTypes.CONTACT); // 12
                addKeyword(Header.CALL_ID.toUpperCase(),
                           TokenTypes.CALL_ID); // 13
                addKeyword(Header.EXPIRES.toUpperCase(),
                           TokenTypes.EXPIRES); // 14
                addKeyword(Header.RECORD_ROUTE.toUpperCase(),
                           TokenTypes.RECORD_ROUTE); // 15
                addKeyword(Header.CSEQ.toUpperCase(),
                           TokenTypes.CSEQ); // 16
                addKeyword(Header.WWW_AUTHENTICATE.toUpperCase(),
                           TokenTypes.WWW_AUTHENTICATE); // 17
                addKeyword(Header.PROXY_AUTHENTICATE.toUpperCase(),
                           TokenTypes.PROXY_AUTHENTICATE); // 18
                addKeyword(Header.EVENT.toUpperCase(),
                           TokenTypes.EVENT); // 19
                addKeyword(Header.SUBJECT.toUpperCase(),
                           TokenTypes.SUBJECT); // 20
                addKeyword(Header.SUPPORTED.toUpperCase(),
                           TokenTypes.SUPPORTED); // 21
                addKeyword(Header.ALLOW_EVENTS.toUpperCase(),
                           TokenTypes.ALLOW_EVENTS); // 22
                addKeyword(Header.ACCEPT_CONTACT.toUpperCase(),
                           TokenTypes.ACCEPT_CONTACT); // 23
                //  And now the dreaded short forms....
                addKeyword(SIPConstants.TOKEN_LETTER_C.toUpperCase(),
                           TokenTypes.CONTENT_TYPE);
                //  CR fix 
                addKeyword(SIPConstants.TOKEN_LETTER_F.toUpperCase(),
                           TokenTypes.FROM);
                addKeyword(SIPConstants.TOKEN_LETTER_I.toUpperCase(),
                           TokenTypes.CALL_ID);
                addKeyword(SIPConstants.TOKEN_LETTER_M.toUpperCase(),
                           TokenTypes.CONTACT);
                addKeyword(SIPConstants.TOKEN_LETTER_E.toUpperCase(),
                           TokenTypes.CONTENT_ENCODING);
                addKeyword(SIPConstants.TOKEN_LETTER_L.toUpperCase(),
                           TokenTypes.CONTENT_LENGTH);
                addKeyword(SIPConstants.TOKEN_LETTER_C.toUpperCase(),
                           TokenTypes.CONTENT_TYPE);
                addKeyword(SIPConstants.TOKEN_LETTER_T.toUpperCase(),
                           TokenTypes.TO);
                addKeyword(SIPConstants.TOKEN_LETTER_V.toUpperCase(),
                           TokenTypes.VIA);
                addKeyword(SIPConstants.TOKEN_LETTER_O.toUpperCase(),
                           TokenTypes.EVENT);
                addKeyword(SIPConstants.TOKEN_LETTER_S.toUpperCase(),
                           TokenTypes.SUBJECT);
                addKeyword(SIPConstants.TOKEN_LETTER_K.toUpperCase(),
                           TokenTypes.SUPPORTED);
                addKeyword(SIPConstants.TOKEN_LETTER_U.toUpperCase(),
                           TokenTypes.ALLOW_EVENTS);
                addKeyword(SIPConstants.TOKEN_LETTER_A.toUpperCase(),
                           TokenTypes.ACCEPT_CONTACT);
            } else if (lexerName.equals("status_lineLexer") ||
                       lexerName.equals("request_lineLexer")) {
                addKeyword(SIPConstants.SCHEME_SIP.toUpperCase(),
                           TokenTypes.SIP);
                addKeyword(SIPConstants.SCHEME_SIPS.toUpperCase(),
                           TokenTypes.SIPS);
            } else if (lexerName.equals("sip_urlLexer")) {
                addKeyword(SIPConstants.SCHEME_TEL.toUpperCase(),
                           TokenTypes.TEL);
                addKeyword(SIPConstants.SCHEME_SIP.toUpperCase(),
                           TokenTypes.SIP);
                addKeyword(SIPConstants.SCHEME_SIPS.toUpperCase(),
                           TokenTypes.SIPS);
            }

        }

    }

}
