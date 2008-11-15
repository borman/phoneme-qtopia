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
import gov.nist.core.*;
import java.util.Vector;

/**
 * Parser For SIP and Tel URLs. Other kinds of URL's are handled by the
 * J2SE 1.4 URL class.
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class URLParser extends Parser {

    /** Symbols of phone digit (RFC 2806, 2.2) */
	private static final String PHONE_DIGIT = "01234567890-.()";

	/** Symbols of dtmf digit (RFC 2806, 2.2) */
	private static final String DTMF_DIGIT = "*#ABCD";

	/** Pause characters (RFC 2806, 2.2) */
	private static final String PAUSE_CHAR = "pw";
    
    /**
     * Constructor with initial URL string.
     * @param url initial URL
     */
    public URLParser(String url) {
        this.lexer = new Lexer("sip_urlLexer", url);

    }

    /**
     * Constructor with initial lexer engine.
     * @param lexer initial lexer engine
     */
    URLParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }

    /**
     * Checks if character is punctuation mark.
     * @param next character to be checked
     * @return true if character is punctuation mark
     */
    protected static boolean isMark(char next) {
        return
                next == '-' ||
                next == '_' ||
                next == '.' ||
                next == '!' ||
                next == '~' ||
                next == '*' ||
                next == '\''||
                next == '(' ||
                next == ')';
    }

    /**
     * Checks if character is reserved.
     * @param next character to be checked
     * @return true if reserved character.
     */
    protected static boolean isUnreserved(char next) {
        return Lexer.isAlpha(next) || Lexer.isDigit(next) || isMark(next);

    }

    /**
     * Checks if reserved character without a slash.
     * @param next character to be checked
     * @return true if reserved character without a slash
     */
    protected static boolean isReservedNoSlash(char next) {
        return
                next == ';' ||
                next == '?' ||
                next == ':' ||
                next == '@' ||
                next == '&' ||
                next == '+' ||
                next == '$' ||
                next == ',';

    }

    // Missing '=' CR in character set - discovered by interop testing
    // at SIPIT 13 by Bob Johnson and Scott Holben.
    // Replace . by; 
    /**
     * Checks if user is unreserved.
     * @param la the character to be checked
     * @return true if user is not reserved
     */
    protected static boolean isUserUnreserved(char la) {
        return la == '&' ||
                la == '?' ||
                la == '+' ||
                la == '$' ||
                la == '#' ||
                la == '/' ||
                la == ',' ||
                la == ';' ||
                la == '=';
    }

    /**
     * Gets the unreserved string of characters.
     * @return unreserved characters
     */
    protected String unreserved() throws ParseException {
        char next = lexer.lookAhead(0);
        if (isUnreserved(next)) {
            lexer.consume(1);
            return new StringBuffer().append(next).toString();
        } else throw createParseException("unreserved");

    }

    /**
     * Name or value of a parameter.
     * @return parsed name or value
     * @exception ParseException if a parsing error occurs
     */
    protected String paramNameOrValue() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (lexer.hasMoreChars()) {
            char next = lexer.lookAhead(0);
            if (next == '[' || next == '[' || next == '/' ||
                    next == ':' || next == '&' || next == '+' ||
                    next == '$' || next == '#' || isUnreserved(next)) {
                retval.append(next);
                lexer.consume(1);
            } else if (isEscaped()) {
                String esc = lexer.charAsString(3);
                lexer.consume(3);
                retval.append(esc);
            } else break;
        }
        return retval.toString();
    }

    /**
     * Gets the URI pamaeter.
     * @return the parsed URI parameter
     * @exception ParseException if a parsin error occurs
     */
    protected NameValue uriParam() throws ParseException {
        if (debug) dbg_enter("uriParam");
        try {
            String pvalue = null;
            String pname = paramNameOrValue();
            char next = lexer.lookAhead(0);
            if (next == LexerCore.EQUALS) {
                lexer.consume(1);
                next = lexer.lookAhead(0);
                if (next == LexerCore.DOUBLEQUOTE) {
                    pvalue = 
                        "\"" + lexer.quotedString() + "\""; // quoted param
                } else { // unquoted parameter
                    pvalue = paramNameOrValue();
                }
            }
            return new NameValue(pname, (pvalue == null) ? "" : pvalue);
        } finally {
            if (debug) dbg_leave("uriParam");
        }
    }

    /**
     * Checks if character is reserved.
     * @param next character to be checked
     * @return true if character is reserved
     */
    protected static boolean isReserved(char next) {
        return next == ';' ||
                next == '/' ||
                next == '?' ||
                next == ':' ||
                next == '@' ||
                next == '&' ||
                next == '+' ||
                next == '$' ||
                next == '=' ||
                next == ',';
    }

    /**
     * Gets the listof reserved characters.
     * @return string of reserved characters.
     */
    protected String reserved() throws ParseException {
        char next = lexer.lookAhead(0);
        if (isReserved(next)) {
            lexer.consume(1);
            return new StringBuffer().append(next).toString();
        } else throw createParseException("reserved");
    }

    /**
     * Checks if current character is escaped.
     * @return true if processing an escaped sequenec
     */
    protected boolean isEscaped() {
        try {
            char next = lexer.lookAhead(0);
            char next1 = lexer.lookAhead(1);
            char next2 = lexer.lookAhead(2);
            return (next == '%' && Lexer.isHexDigit(next1)
            && Lexer.isHexDigit(next2));
        } catch (ParseException ex) {
            return false;
        }
    }

    /**
     * Gets the escaped character sequence.
     * @return the escaped character sequence
     */
    protected String escaped() throws ParseException {
        if (debug) dbg_enter("escaped");
        try {
            StringBuffer retval = new StringBuffer();
            char next = lexer.lookAhead(0);
            char next1 = lexer.lookAhead(1);
            char next2 = lexer.lookAhead(2);
            if (next == '%' && Lexer.isHexDigit(next1)
            && Lexer.isHexDigit(next2)) {
                lexer.consume(3);
                retval.append(next);
                retval.append(next1);
                retval.append(next2);
            } else throw createParseException("escaped");
            return retval.toString();
        } finally {
            if (debug) dbg_leave("escaped");
        }
    }

    /**
     * Remembers the current stream position.
     * @return the current marked position
     */
    protected String mark() throws ParseException {
        if (debug) dbg_enter("mark");
        try {
            char next = lexer.lookAhead(0);
            if (isMark(next)) {
                lexer.consume(1);
                return new StringBuffer().append(next).toString();
            } else throw createParseException("mark");
        } finally {
            if (debug) dbg_leave("mark");
        }
    }

    /**
     * Gets the uric.
     * @return the uric
     */
    protected String uric() {
        if (debug) dbg_enter("uric");
        try {
            try {
                char la = lexer.lookAhead(0);
                if (isUnreserved(la)) {
                    lexer.consume(1);
                    return Lexer.charAsString(la);
                } else if (isReserved(la)) {
                    lexer.consume(1);
                    return Lexer.charAsString(la);
                } else if (isEscaped()) {
                    String retval = lexer.charAsString(3);
                    lexer.consume(3);
                    return retval;
                } else return null;
            } catch (ParseException ex) {
                return null;
            }
        } finally {
            if (debug) dbg_leave("uric");
        }

    }

    /**
     * Gets the uric without slashes.
     * @return the uric string without slashes.
     */
    protected String uricNoSlash() {
        if (debug) dbg_enter("uricNoSlash");
        try {
            try {
                char la = lexer.lookAhead(0);
                if (isEscaped()) {
                    String retval = lexer.charAsString(3);
                    lexer.consume(3);
                    return retval;
                } else if (isUnreserved(la)) {
                    lexer.consume(1);
                    return Lexer.charAsString(la);
                } else if (isReservedNoSlash(la)) {
                    lexer.consume(1);
                    return Lexer.charAsString(la);
                } else return null;
            } catch (ParseException ex) {
                return null;
            }
        } finally {
            if (debug) dbg_leave("uricNoSlash");
        }
    }

    /**
     * Gets the uric string.
     * @return the uric string
     */
    protected String uricString() {
        StringBuffer retval = new StringBuffer();
        while (true) {
            String next = uric();
            if (next == null) break;
            retval.append(next);
        }
        return retval.toString();
    }

    /**
     * Parses and return a structure for a generic URL.
     * Note that non SIP URLs are just stored as a string (not parsed).
     * @return URI is a URL structure for a SIP url.
     * @throws ParsException if there was a problem parsing.
     */
    public URI uriReference() throws ParseException {
        if (debug) dbg_enter("uriReference");

        URI retval = null;
        Vector vect = lexer.peekNextToken(2);
        Token t1 = (Token) vect.elementAt(0);
        Token t2 = (Token) vect.elementAt(1);

        try {
            // System.out.println("token = " + t1.getTokenValue());
            // System.out.println("tokentype = " + t1.getTokenType());

            int type1, type2;
            type1 = t1.getTokenType();

            // Create an URI
            if (t2.getTokenType() == ':') {
                if (type1 == TokenTypes.SIP || type1 == TokenTypes.SIPS) {
                    // SIP or SIPS URL
                    retval = sipURL(t1);
                } else if (type1 == TokenTypes.TEL) {
                    // TEL URL
                    retval = telURL();
                } else {
                    // Generic URL
                    
                    /*
                     * We can't throw an exception here because according to
                     * RFC 3261, p. 224 the scheme may be different from
                     * sip/sips/tel.
                     * In Connector.open(scheme) only 'sip' and 'sips'
                     * schemes are allowed, but this is not a problem
                     * because the package path where VM will try to find
                     * Protocol.class contains the scheme's name and an
                     * exception will be thrown if it is invalid.
                     */
                    retval = new URI(lexer.getString('>'));
                    
                    int pos = lexer.markInputPosition();
                    // It's safe to use 'pos-1' because at least
                    // one character ('>') was read from the buffer.
                    lexer.rewindInputPosition(pos - 1);
                }
            } else {
                throw createParseException("Expecting \':\'");
            }

        } finally {
            if (debug) dbg_leave("uriReference");
        }

        return retval;
    }

    /**
     * Parses for the base phone number.
     * @return the base phone number
     * @exception ParseException if a parsingerror occurs
     */
    private String base_phone_number() throws ParseException {
        StringBuffer s = new StringBuffer();

        if (debug) dbg_enter("base_phone_number");
        try {
            int lc = 0;
            while (lexer.hasMoreChars()) {
                char w = lexer.lookAhead(0);
                if (LexerCore.isDigit(w) || w == '-' || w == '.' || w == '('
                        || w == ')') {
                    lexer.consume(1);
                    s.append(w);
                    lc ++;
                } else if (lc > 0) break;
                else throw createParseException("unexpected " + w);
            }
            return s.toString();
        } finally {
            if (debug) dbg_leave("base_phone_number");
        }

    }


    /**
     * Parses for the local phone number.
     * @return the local phone number
     * @exception ParseException if a parsing error occurs
     */
    private String local_number()
    throws ParseException {
        StringBuffer s = new StringBuffer();
        if (debug) dbg_enter("local_number");
        try {
            int lc = 0;
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (la == '*' || la == '#' || la == '-' ||
                        la == '.' || la == '(' || la == ')' ||
                        LexerCore.isDigit(la)) {
                    lexer.consume(1);
                    s.append(la);
                    lc ++;
                } else if (lc > 0) break;
                else throw createParseException("unexepcted " + la);
            }
            return s.toString();
        } finally {
            if (debug) dbg_leave("local_number");
        }

    }


    /**
     * Parses for telephone subscriber.
     *
     * @return the parsed telephone number.
     * @exception ParseException if a parsing error occurs
     */
    public final TelephoneNumber parseTelephoneNumber()
    throws ParseException {
        TelephoneNumber tn;

        if (debug) dbg_enter("telephone_subscriber");
        lexer.selectLexer("charLexer");
        try {
            char c = lexer.lookAhead(0);
            if (c == '+') tn = global_phone_number();
            else if (LexerCore.isAlpha(c) || LexerCore.isDigit(c) ||
                    c == '-' || c == '*' || c == '.' ||
                    c == '(' || c == ')' || c == '#') {
                tn = local_phone_number();
            } else throw createParseException("unexpected char " + c);
            return tn;
        } finally {
            if (debug) dbg_leave("telephone_subscriber");
        }

    }

    /**
     * Gets the global phone number.
     * @return the parsed phone number
     * @exception  ParseException ifa parsing error occurs
     */
    private final TelephoneNumber global_phone_number()
    throws ParseException {
        if (debug) dbg_enter("global_phone_number");
        try {
            TelephoneNumber tn = new TelephoneNumber();
            tn.setGlobal(true);
            NameValueList nv = null;
            this.lexer.match(PLUS);
            String b = base_phone_number();
            tn.setPhoneNumber(b);
            if (lexer.hasMoreChars()) {
                char tok = lexer.lookAhead(0);
                if (tok == ';') {
                    nv = tel_parameters();
                    tn.setParameters(nv);
                }
            }
            return tn;
        } finally {
            if (debug) dbg_leave("global_phone_number");
        }
    }

    /**
     * Gets the local phone number.
     * @return the parsed phone number
     * @exception  ParseException ifa parsing error occurs
     */
    private TelephoneNumber local_phone_number()
    throws ParseException {
        if (debug) dbg_enter("local_phone_number");
        TelephoneNumber tn = new TelephoneNumber();
        tn.setGlobal(false);
        NameValueList nv = null;
        String b = null;
        try {
            b = local_number();
            tn.setPhoneNumber(b);
            if (lexer.hasMoreChars()) {
                Token tok = this.lexer.peekNextToken();
                switch (tok.getTokenType()) {
                    case SEMICOLON: {
                        nv = tel_parameters();
                        tn.setParameters(nv);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        } finally {
            if (debug) dbg_leave("local_phone_number");
        }
        return tn;
    }
                            
    /**
     * Gets the telephone field parameters.
     * @return the telephone parameters
     * @exception ParseException if a parsing error occurs
     */
    private NameValueList tel_parameters() throws ParseException {
        NameValueList nvList = new NameValueList();
        while (lexer.hasMoreChars()) {
            lexer.consume(1);
            NameValue nv = uriParam();
            String nameParam = nv.getName();
            String valueParam = (String)(nv.getValue());
            if (nameParam.equalsIgnoreCase("isub")) {
                // RFC 2806, 2.2
                // isdn-subaddress       = ";isub=" 1*phonedigit
                // phonedigit            = DIGIT / visual-separator
                // visual-separator      = "-" / "." / "(" / ")"
                byte[] valueBytes = valueParam.getBytes();
                for (int i = 0; i < valueBytes.length; i++) {
                    if (!isPhoneDigit(valueBytes[i])) {
                        throw new 
                            IllegalArgumentException("Wrong isdn-subaddress");
                    }
                }
            } else if (nameParam.equalsIgnoreCase("postd")) {
                // RFC 2806, 2.2
                // post-dial             = ";postd=" 1*(phonedigit /
                //                         dtmf-digit / pause-character)
                // dtmf-digit            = "*" / "#" / "A" / "B" / "C" / "D"
                // pause-character       = one-second-pause / wait-for-dial-tone
                // one-second-pause      = "p"
                // wait-for-dial-tone    = "w"
                byte[] valueBytes = valueParam.getBytes();
                for (int i = 0; i < valueBytes.length; i++) {
                    int ch = valueBytes[i];
                    if (!isPhoneDigit(ch) && !isDtmf_digit(ch) &&
                        !isPauseChar(ch)) {
                        throw new 
                            IllegalArgumentException("Wrong post-dial");
                    }
                }
            }
            nvList.add(nv);
            char tok = lexer.lookAhead(0);
            if (tok == ';') continue;
            else break;
        }
        return nvList;
    }
                
    /**
     * Checks that the given symbol os a phonedigit.
     * RFC 2806, 2.2
     * phonedigit            = DIGIT / visual-separator
     * visual-separator      = "-" / "." / "(" / ")"
     * @param ch is a given byte to checking
     * @return true if the given char is a phonedigit
     */
    private boolean isPhoneDigit(int ch) {
		return isCharFromString(ch, PHONE_DIGIT);
	}

    /**
     * Checks that the given symbol is a dtmf digit.
     * RFC 2806, 2.2
     * dtmf-digit            = "*" / "#" / "A" / "B" / "C" / "D"
     * @param ch is a given byte to checking
     * @return true if the given char is a dtmf digit
     */
    private boolean isDtmf_digit(int ch) {
		return isCharFromString(ch, DTMF_DIGIT);
	}

    /**
     * Checks that the given symbol is a pause character.
     * pause-character       = one-second-pause / wait-for-dial-tone
     * one-second-pause      = "p"
     * wait-for-dial-tone    = "w"
     * @param ch is a given byte to checking
     * @return true if the given char is a dtmf digit
     */
    private boolean isPauseChar(int ch) {
		return isCharFromString(ch, PAUSE_CHAR);
	}

    /**
     * Checks that the given symbol contains into a string.
     * @param ch is a given byte to checking
     * @param str is a string for checking
     * @return true if the given char can be find in a string
     */
    private boolean isCharFromString(int ch, String str) {
		return (str.indexOf(ch) != -1);
	}

    /**
     * Parses and returns a structure for a Tel URL.
     * @return a parsed tel url structure.
     * @exception ParseException if a parsing error occurs
     */
    public TelURL telURL() throws ParseException {
        lexer.match(TokenTypes.TEL);
        lexer.match(':');
        TelephoneNumber tn = this.parseTelephoneNumber();
        TelURL telUrl = new TelURL();
        telUrl.setTelephoneNumber(tn);
        return telUrl;
    }

    /**
     * Parses and returns a structure for a SIP URL.
     * @param token the token of scheme (SIP or SIPS)
     * @return a URL structure for a SIP url.
     * @throws ParsException if there was a problem parsing.
     * @throws IllegalArgumentException when parsing error is fatal.
     */
    public SipURI sipURL(Token token) throws ParseException {
        if (debug) dbg_enter("sipURL");

        char la;
        SipURI retval = new SipURI();

        try {
            lexer.match(token.getTokenType());
            lexer.match(':');
            retval.setScheme(token.getTokenValue());
            if (!lexer.hasMoreChars()) { // sip: - server dedicated URI
                retval.setServer();
                return retval;
            }
            int m = lexer.markInputPosition();
            // get user part
            String user = user();
            if (!lexer.hasMoreChars()) { // nosymbols after user name  
                lexer.rewindInputPosition(m); // move to start of host
            } else { // check symbols after user name    
                // maybe sip:host
                la = lexer.lookAhead(0);
                // check that user field is not empty
                if (la == ':' || la == '@') { // sip:user@... or sip:user:pass
                    if (user.length() == 0) {
                        throw new
                            IllegalArgumentException("User field is missed");
                    }
                    if (la == ':') {
                        // sip:user:passw...
                        lexer.consume(1);
                        String password = password();
                        if (!lexer.hasMoreChars() 
                            || lexer.lookAhead(0) != '@') {
                            if (StringTokenizer.isDigitString(password)) {
                                // maybe sip:host:port - move to start of host
                                lexer.rewindInputPosition(m);
                            } else { // sip:user:pass<wrong symbol>
                                throw new IllegalArgumentException
                                    ("Expecting \"@\"");
                            }
                        } else { // sip:user:pass@...
                            retval.setUser(user);
                            retval.setUserPassword(password);
                            lexer.consume(1);
                        }
                    } else { // la == '@'
                        retval.setUser(user);
                        lexer.consume(1);
                    }
                } else { // no '@' after user field - maybe sip:host...
                    lexer.rewindInputPosition(m); // move to start of host
                }
            }
            // check for sip:*
            la = lexer.lookAhead(0);
            if (la == '*') {
                lexer.consume(1);
                // server shared URI
                retval.setServer();
                retval.setShared();
            } else if (la == ';') { // sip:;...
                retval.setServer();
            } else { // try to read host
                // host parsing
                HostNameParser hnp = new HostNameParser(this.getLexer());
                HostPort hp = hnp.hostPort();
                String host = hp.getHost().getHostname();
                if (host == null) { // maybe sip:5060
                    if (hp.hasPort()) { // port only - server URI
                        retval.setServer();
                    } else { // sip:1234:5678 - wrong format
                        throw new
                            IllegalArgumentException("Illegal URI format");
                    }
                }
                retval.setHostPort(hp);
            }

            lexer.selectLexer("charLexer");

            // parse parameters
            while (lexer.hasMoreChars()) {
                if (lexer.lookAhead(0) != ';') break;
                lexer.consume(1);
                NameValue parms = uriParam();
                if (retval.hasParameter(parms.getName())) {
                    throw new IllegalArgumentException(
                                "Found duplicate of parameter ");
                }

                retval.setUriParameter(parms);
            }

            if (lexer.hasMoreChars() && lexer.lookAhead(0) == '?') {
                if (retval.isServer()) {
                    throw new IllegalArgumentException(
                        "Server URI cannot contain headers");
                }
                lexer.consume(1);
                while (lexer.hasMoreChars()) {
                    NameValue parms = qheader();
                    retval.setQHeader(parms);
                    if (lexer.hasMoreChars() &&
                            lexer.lookAhead(0) != '&') break;
                    else lexer.consume(1);
                }
            }
            
            return retval;
        } finally {
            if (debug) dbg_leave("sipURL");
        }
    }

    /**
     * Peeks at the scheme field.
     * @return the protocol scheme
     * @exception ParseException if a parsing error occurs
     */
    public String peekScheme() throws ParseException {
        Vector tokens = lexer.peekNextToken(1);

        if (tokens.size() == 0)
            return null;

        String scheme = ((Token)tokens.elementAt(0)).getTokenValue();
        return scheme;
    }

    /**
     * Gets a name value for a given query header (ie one that comes
     * after the ?).
     * @return name value pair for q-header
     * @exception ParseException if a parsing error occurs
     */
    protected NameValue qheader() throws ParseException {

        String name = lexer.getNextToken('=');
        lexer.consume(1);
        String value = hvalue();
        return new NameValue(name, value);

    }

    /**
     * Gets a header value.
     * @return value of current header
     * @exception ParseException if a parsing error occurs
     */
    protected String hvalue() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (lexer.hasMoreChars()) {
            char la = lexer.lookAhead(0);
            // Look for a character that can terminate a URL.
            if (la == '+' || la == '?' || la == ':' || la == '@'
                    || la == '[' || la == ']' || la == '/' || la == '$'
                    || la == '_' || la == '-' || la == '"' || la == '!'
                    || la == '~' || la == '*' || la == '.' || la == '('
                    || la == ')' || LexerCore.isAlpha(la)
                    || LexerCore.isDigit(la)) {
                lexer.consume(1);
                retval.append(la);
            } else if (la == '%') {
                retval.append(escaped());
            } else break;
        }
        return retval.toString();
    }

    /**
     * Scans forward until you hit a terminating character for a URL.
     * We do not handle non sip urls in this implementation.
     * @return the string that takes us to the end of this URL (i.e. to
     * the next delimiter).
     * @exception ParseException if a parsing error occurs
     */
    protected String urlString() throws ParseException {
        StringBuffer retval = new StringBuffer();
        lexer.selectLexer("charLexer");

        while (lexer.hasMoreChars()) {
            char la = lexer.lookAhead(0);
            // Look for a character that can terminate a URL.
            if (la == ' ' || la == '\t' || la == '\n' ||
                    la == '>' || la == '<') break;
            lexer.consume(0);
            retval.append(la);
        }
        return retval.toString();
    }

    /**
     * Gets the user field from the URI.
     * @return ths parsed user field
     * @exception ParseException if a parsing error occurs
     */
    protected String user() throws ParseException {

        if (debug) dbg_enter("user");
        try {
            StringBuffer retval = new StringBuffer();
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (isUnreserved(la) ||
                        isUserUnreserved(la)) {
                    retval.append(la);
                    lexer.consume(1);
                } else if (isEscaped()) {
                    String esc = lexer.charAsString(3);
                    lexer.consume(3);
                    retval.append(esc);
                } else break;
            }
            return retval.toString();
        } finally {
            if (debug) dbg_leave("user");
        }

    }

    /**
     * Gets the password field from the URI.
     * @return ths parsed password field
     * @exception ParseException if a parsing error occurs
     */
    protected String password() throws ParseException {
        StringBuffer retval = new StringBuffer();
        while (true) {
            char la = lexer.lookAhead(0);
            if (isUnreserved(la) || la == '&' || la == '=' ||
                    la == '+' || la == '$' || la == ',') {
                retval.append(la);
                lexer.consume(1);
            } else if (isEscaped()) {
                String esc = lexer.charAsString(3);
                retval.append(esc);
                // CR FIX from Jeff Haynie frm JAIN-SIP
                lexer.consume(3);
            } else break;
        }
        return retval.toString();
    }

    /**
     * Default parse method. This method just calls uriReference.
     * @return ths parsed URI
     * @exception ParseException if a parsing error occurs
     */
    public URI parse() throws ParseException {
        return uriReference();
    }

    /**
     * Parse method with checking the rest of input URL.
     * @return ths parsed URI
     * @exception ParseException if a parsing error occurs
     */
    public URI parseWholeString() throws ParseException {
        URI retValue = uriReference();
        if (lexer.hasMoreChars()) {
            throw createParseException("redundant symbols");
        }
        return retValue;
    }
}
