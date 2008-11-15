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
package gov.nist.siplite.address;
import gov.nist.siplite.parser.*;
import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

/**
 * Implementation of the JAIN-SIP address factory.
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */
public class AddressFactory {

    /**
     * Creates a new instance ofAddressFactoryImpl.
     */
    public AddressFactory() {
    }


    /**
     * Creates anAddress with the new display name and URI attribute
     * values.
     *
     * @param displayName - the new string value of the display name of the
     * address. A <code>null</code> value does not set the display name.
     * @param uri - the new URI value of the address.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the displayName value.
     * @return the new address
     */
    public Address
            createAddress(String displayName, URI uri) {
        if (uri == null)
            throw new NullPointerException("null URI");
        Address addressImpl = new Address();
        if (displayName != null) addressImpl.setDisplayName(displayName);
        addressImpl.setURI(uri);
        return addressImpl;

    }

    /**
     * Creates a sip uri.
     *
     * @param uri the uri to parse.
     * @return the new URI
     */
    public SipURI createSipURI(String uri)
    // throws java.netURISyntaxException {
    throws ParseException {
        if (uri == null)
            throw new NullPointerException("null URI");
        try {
            StringMsgParser smp = new StringMsgParser();
            SipURI sipUri = smp.parseSIPUrl(uri);
            return (SipURI) sipUri;
        } catch (ParseException ex) {
            // throw new java.netURISyntaxException(uri, ex.getMessage());
            throw new ParseException(ex.getMessage(), 0);
        }

    }


    /**
     * Creates a SipURI.
     *
     * @param user  the user
     * @param host  the host.
     * @return the new SIP URI
     */
    public SipURI createSipURI(String user, String host)
    throws ParseException {
        if (host == null)
            throw new NullPointerException("null host");

        StringBuffer uriString = new StringBuffer("sip:");
        if (user != null) {
            uriString.append(user);
            uriString.append("@");
        }

        // if host is an IPv6 string we should enclose it in sq brackets
        if (host.indexOf(':') != host.lastIndexOf(':')
        && host.trim().charAt(0) != '[')
            host = '[' + host + ']';

        uriString.append(host);

        StringMsgParser smp = new StringMsgParser();
        try {

            SipURI sipUri = smp.parseSIPUrl(uriString.toString());
            return sipUri;
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }

    /**
     * Creates a TelURL based on given URI string. The scheme or '+' should
     * not be included in the phoneNumber string argument.
     *
     * @param uri the new string value of the phoneNumber.
     * @return the new telephone URL
     * @throws URISyntaxException if the URI string is malformed.
     */
    public TelURL createTelURL(String uri)
    throws ParseException {
        if (uri == null)
            throw new NullPointerException("null url");
        String telUrl = "tel:" + uri;
        try {
            StringMsgParser smp = new StringMsgParser();
            TelURL timp = (TelURL) smp.parseUrl(telUrl);
            return (TelURL) timp;
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
    }


    /**
     * Creates a new address.
     * @param uri the location to use
     * @return the address
     * @exception  NullPointerException if uri is null
     */
    public Address createAddress(URI uri) {
        if (uri == null)
            throw new NullPointerException("null address");
        Address addressImpl = new Address();
        addressImpl.setURI(uri);
        return addressImpl;
    }

    /**
     * Creates anAddress with the new address string value. The address
     * string is parsed in order to create the new Address instance. Create
     * with a String value of "*" creates a wildcard address. The wildcard
     * can be determined if
     * <code>(SipURIAddress.getURI).getUser() == *;</code>.
     *
     * @param address  the new string value of the address.
     * @return the new Address
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the address value.
     * @exception NullPointerException if address is null
     */
    public Address createAddress(String address)
    throws ParseException {
        if (address == null)
            throw new NullPointerException("null address");

        if (address.equals("*")) {
            Address addressImpl = new Address();
            addressImpl.setAddressType(Address.WILD_CARD);
            return addressImpl;
        } else {
            StringMsgParser smp = new StringMsgParser();
            return smp.parseAddress(address);
        }
    }

    /**
     * Creates a URI based on given URI string. The URI string is parsed in
     * order to create the new URI instance. Depending on the scheme the
     * returned may or may not be aSipURI or TelURL cast as a URI.
     *
     * @param uri the new string value of the URI.
     * @return the new SIP URI
     * @throws URISyntaxException if the URI string is malformed.
     * @exception NullPointerException if uri is null
     */
    public URI createURI(String uri) throws ParseException {
        if (uri == null)
            throw new NullPointerException("null arg");
        try {
            Lexer lexer = new Lexer("sip_urlLexer", uri);
            Token token = lexer.peekNextToken();
            // For cases when URI begins with "<" and for TCK passing
            if (token.getTokenType() == LexerCore.LESS_THAN) {
                lexer.consume();
                token = lexer.peekNextToken();
                uri = uri.substring(uri.indexOf(LexerCore.LESS_THAN)+1,
                              uri.lastIndexOf(LexerCore.GREATER_THAN));
            }
            URLParser urlParser = new URLParser(uri);
            String scheme = token.getTokenValue();
            if (scheme == null || !Lexer.isValidScheme(scheme))
                throw new ParseException("bad scheme", 0);
            if (Utils.equalsIgnoreCase(scheme, SIPConstants.SCHEME_SIP)) {
                return (URI) urlParser.sipURL(token);
            } else if (Utils.equalsIgnoreCase(scheme,
                                              SIPConstants.SCHEME_SIPS)) {
                return (URI) urlParser.sipURL(token);
            } else if (Utils.equalsIgnoreCase(scheme,
                                              SIPConstants.SCHEME_TEL)) {
                return (URI) urlParser.telURL();
            }
        } catch (ParseException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }
        return new URI(uri);
    }

}
