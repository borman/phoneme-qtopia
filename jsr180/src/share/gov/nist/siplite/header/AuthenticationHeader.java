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
 */
package gov.nist.siplite.header;

import gov.nist.core.*;
import gov.nist.siplite.address.*;

/**
 * The generic AuthenticationHeader
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public abstract class AuthenticationHeader extends ParametersHeader {
    /** Domain header label. */
    public static String DOMAIN = "domain";
    /** Realm header label. */
    public static String REALM = "realm";
    /** Opaque header. */
    public static String OPAQUE = "opaque";
    /** Algorithm header label. */
    public static String ALGORITHM = "algorithm";
    /** Qop header (RFC?). */
    public static String QOP = "qop";
    /** Stale header (RFC?). */
    public static String STALE = "stale";
    /** Signature header. */
    public static String SIGNATURE = "signature";
    /** Response header. */
    public static String RESPONSE = "response";
    /** Signed byte header. */
    public static String SIGNED_BY = "signed-by";
    /** NC header (RFC?). */
    public static String NC = "nc";
    /** URI header. */
    public static String URI = "uri";
    /** User name header. */
    public static String USERNAME = "username";
    /** C-nonce header. */
    public static String CNONCE = "cnonce";
    /** Nonce header. */
    public static String NONCE = "nonce";
    /** Digest header. */
    public static String DIGEST = "Digest";
    /** Next nonce header. */
    public static String NEXT_NONCE = "next-nonce";

    /** Current protocol scheme. */
    protected String scheme;

    /**
     * Constructor with header name.
     * @param name header to process
     */
    public AuthenticationHeader(String name) {
        super(name);
        parameters.setSeparator(Separators.COMMA); // oddball
        this.scheme = DIGEST;
    }

    /** Default constructor. */
    public AuthenticationHeader() {
        super();
        parameters.setSeparator(Separators.COMMA);
    }

    /**
     * Sets the specified parameter.
     * @param nv parameter's name/value pair
     */
    public void setParameter(NameValue nv) {
        Object val = nv.getValue();
        setParameter(nv.getName(), (val == null) ? null : val.toString());
    }

    /**
     * Sets the specified parameter.
     * @param name name of the parameter
     * @param value value of the parameter.
     */
    public void setParameter(String name, String value)
            throws IllegalArgumentException {
        NameValue nv =
                super.parameters.getNameValue(name.toLowerCase());

        boolean quotedParam = false;
        
        if (isQuoted(name)) {
            
            if (value == null) {
                throw new NullPointerException("null value");
            }
            
            quotedParam = true;
            boolean quoteStart = value.startsWith(Separators.DOUBLE_QUOTE);
            boolean quoteEnd = value.endsWith(Separators.DOUBLE_QUOTE);
            
            if ((quoteStart && !quoteEnd) || (!quoteStart && quoteEnd)) {
                throw new IllegalArgumentException
                    (value + " : Unexpected DOUBLE_QUOTE");
            }
            
            if (quoteStart) { // quoteEnd is true in this case
                value = value.substring(1, value.length() - 1);
            }
        }
        
        if (nv == null) {
            nv = new NameValue(name.toLowerCase(), value);

            if (quotedParam) {
                nv.setQuotedValue();
            }

            super.setParameter(nv);
        } else {
            nv.setValue(value);
        }

    }
    
    /**
     * Returns the value of the named parameter, or null if it is not set. A
     * zero-length String indicates flag parameter.
     *
     * @param name name of parameter to retrieve
     * @return the value of specified parameter
     */
    public String getParameter(String name) {
        String returnValue = super.getParameter(name);
        if ((returnValue != null) && isQuoted(name)) { // remove quotes
            returnValue = returnValue.substring(1, returnValue.length() - 1);
        }
        return returnValue;
        
    }
    
    /**
     * Returns true if parameter must be quoted, else - false.
     * zero-length String indicates flag parameter.
     *
     * @param name name of parameter to retrieve
     * @return flag of parameter (quoted or not)
     */
    private boolean isQuoted(String name) {
        if (equalsIgnoreCase(name, QOP) ||
                equalsIgnoreCase(name, REALM) ||
                equalsIgnoreCase(name, URI) ||
                equalsIgnoreCase(name, CNONCE) ||
                equalsIgnoreCase(name, NONCE) ||
                equalsIgnoreCase(name, USERNAME) ||
                equalsIgnoreCase(name, DOMAIN) ||
                equalsIgnoreCase(name, OPAQUE) ||
                equalsIgnoreCase(name, NEXT_NONCE) ||
                equalsIgnoreCase(name, RESPONSE)) {
            return true;
        }
        return false;
    }

    /**
     * Encodes in canonical form.
     * @return canonical string.
     */
    public String encodeBody() {
        return this.scheme + Separators.SP + parameters.encode();
    }

    /**
     * Sets the scheme of the challenge information for this
     * AuthenticationHeaderHeader. For example, Digest.
     *
     * @param scheme - the new string value that identifies the challenge
     * information scheme.
     * @since v1.1
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the scheme of the challenge information for this
     * AuthenticationHeaderHeader.
     *
     * @return the string value of the challenge information.
     * @since v1.1
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the Realm of the WWWAuthenicateHeader to the <var>realm</var>
     * parameter value. Realm strings MUST be globally unique. It is
     * RECOMMENDED that a realm string contain a hostname or domain name.
     * Realm strings SHOULD present a human-readable identifier that can be
     * rendered to a user.
     *
     * @param realm the new Realm String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the realm.
     * @since v1.1
     */
    public void setRealm(String realm) {
        if (realm == null)
            throw new NullPointerException("null realm");
        setParameter(REALM, realm);
    }

    /**
     * Returns the Realm value of this WWWAuthenicateHeader. This convenience
     * method returns only the realm of the complete Challenge.
     *
     * @return the String representing the Realm information, null if value is
     * not set.
     * @since v1.1
     */
    public String getRealm() {
        return getParameter(REALM);
    }

    /**
     * Sets the Nonce of the WWWAuthenicateHeader to the <var>nonce</var>
     * parameter value.
     *
     * @param nonce - the new nonce String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the nonce value.
     * @since v1.1
     */
    public void setNonce(String nonce) {
        if (nonce == null)
            throw new NullPointerException("null nonce");
        setParameter(NONCE, nonce);
    }

    /**
     * Returns the Nonce value of this WWWAuthenicateHeader.
     *
     * @return the String representing the nonce information, null if value is
     * not set.
     * @since v1.1
     */
    public String getNonce() {
        return getParameter(NONCE);
    }

    /**
     * Sets the URI of the WWWAuthenicateHeader to the <var>uri</var>
     * parameter value.
     *
     * @param uri - the new URI of this WWWAuthenicateHeader.
     * @since v1.1
     */
    public void setURI(URI uri) {
        if (uri != null) {
            NameValue nv = new NameValue(URI, uri);
            nv.setQuotedValue();
            super.parameters.set(nv);
        } else {
            throw new NullPointerException("Null URI");
        }
    }

    /**
     * Returns the URI value of this WWWAuthenicateHeader,
     * for example DigestURI.
     *
     * @return the URI representing the URI information, null if value is
     * not set.
     * @since v1.1
     */
    public URI getURI() {
        return getParameterAsURI(URI);
    }

    /**
     * Sets the Algorithm of the WWWAuthenicateHeader to the new
     * <var>algorithm</var> parameter value.
     *
     * @param algorithm - the new algorithm String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the algorithm value.
     * @since v1.1
     */
    public void setAlgorithm(String algorithm) throws ParseException {
        if (algorithm == null)
            throw new NullPointerException("null arg");
        setParameter(ALGORITHM, algorithm);
    }

    /**
     * Returns the Algorithm value of this WWWAuthenicateHeader.
     *
     * @return the String representing the Algorithm information, null if the
     * value is not set.
     * @since v1.1
     */
    public String getAlgorithm() {
        return getParameter(ALGORITHM);
    }

    /**
     * Sets the Qop value of the WWWAuthenicateHeader to the new
     * <var>qop</var> parameter value.
     *
     * @param qop - the new Qop string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Qop value.
     * @since v1.1
     */
    public void setQop(String qop) throws ParseException {
        if (qop == null)
            throw new NullPointerException("null arg");
        setParameter(QOP, qop);
    }

    /**
     * Returns the Qop value of this WWWAuthenicateHeader.
     *
     * @return the string representing the Qop information, null if the
     * value is not set.
     * @since v1.1
     */
    public String getQop() {
        return getParameter(QOP);
    }

    /**
     * Sets the Opaque value of the WWWAuthenicateHeader to the new
     * <var>opaque</var> parameter value.
     *
     * @param opaque - the new Opaque string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the opaque value.
     * @since v1.1
     */
    public void setOpaque(String opaque) throws ParseException {
        if (opaque == null)
            throw new NullPointerException("null arg");
        setParameter(OPAQUE, opaque);
    }

    /**
     * Returns the Opaque value of this WWWAuthenicateHeader.
     *
     * @return the String representing the Opaque information, null if the
     * value is not set.
     * @since v1.1
     */
    public String getOpaque() {
        return getParameter(OPAQUE);
    }

    /**
     * Sets the Domain of the WWWAuthenicateHeader to the <var>domain</var>
     * parameter value.
     *
     * @param domain - the new Domain string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the domain.
     * @since v1.1
     */
    public void setDomain(String domain) throws ParseException {
        if (domain == null)
            throw new NullPointerException("null arg");
        setParameter(DOMAIN, domain);
    }


    /**
     * Returns the Domain value of this WWWAuthenicateHeader.
     *
     * @return the String representing the Domain information, null if value is
     * not set.
     * @since v1.1
     */
    public String getDomain() {
        return getParameter(DOMAIN);
    }

    /**
     * Sets the value of the stale parameter of the WWWAuthenicateHeader to the
     * <var>stale</var> parameter value.
     *
     * @param stale - the new boolean value of the stale parameter.
     * @since v1.1
     */
    public void setStale(boolean stale) {
        setParameter(new NameValue(STALE, new Boolean(stale)));
    }

    /**
     * Returns the boolean value of the state paramater of this
     * WWWAuthenicateHeader.
     *
     * @return the boolean representing if the challenge is stale.
     * @since v1.1
     */
    public boolean isStale() {
        return this.getParameterAsBoolean(STALE);
    }

    /**
     * Set the CNonce.
     *
     * @param cnonce -- a nonce string.
     */
    public void setCNonce(String cnonce) throws ParseException {
        this.setParameter(CNONCE, cnonce);
    }

    /**
     * Get the CNonce.
     *
     * @return the cnonce value.
     */
    public String getCNonce() {
        return getParameter(CNONCE);
    }

    /**
     * Counts the nonce.
     * @return the count of nonces
     */
    public int getNonceCount() {
        return this.getParameterAsHexInt(NC);

    }

    /**
     * Set the nonce count parameter.
     *
     * @param nonceCount -- nonce count to set.
     */

    public void setNonceCount(int nonceCount)
    throws ParseException, IllegalArgumentException {
        if (nonceCount < 0)
            throw new IllegalArgumentException("bad value");

        String nc = Integer.toHexString(nonceCount);

        String base = "00000000";
        nc = base.substring(0, 8 - nc.length()) + nc;
        this.setParameter(NC, nc);

    }

    /**
     * Gets the RESPONSE value (or null if it does not exist).
     *
     * @return String response parameter value.
     */
    public String getResponse() {
        return (String) getParameterValue(RESPONSE);
    }


    /**
     * Sets the Response.
     *
     * @param response to set.
     */
    public void setResponse(String response) throws ParseException {
        if (response == null)
            throw new NullPointerException("Null parameter");
        this.setParameter(RESPONSE, response);
    }


    /**
     * Returns the Username value of this AuthorizationHeader.
     * This convenience method returns only the username of the
     * complete Response.
     *
     * @return the String representing the Username information,
     * null if value is not set.
     *
     * @since JAIN SIP v1.1
     *
     */
    public String getUsername() {
        return (String) getParameter
                (USERNAME);
    }

    /**
     * Sets the Username of the AuthorizationHeader to
     * the <var>username</var> parameter value.
     *
     * @param username the new Username String of this AuthorizationHeader.
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the username.
     *
     * @since JAIN SIP v1.1
     *
     */
    public void setUsername(String username) {
        this.setParameter(USERNAME, username);
    }

    /**
     * Clone - do a deep copy.
     * @return Object Authorization
     */
    public Object clone() {
        Exception ex = null;
        try {
            AuthenticationHeader retval = (AuthenticationHeader)
            this.getClass().newInstance();
            if (this.scheme != null) retval.scheme = new String(this.scheme);
            if (this.parameters != null)
                retval.parameters = (NameValueList)parameters.clone();
            return retval;
        } catch (InstantiationException ie) {
            ex = ie;
        } catch (IllegalAccessException iae) {
            ex = iae;
        }
        if (ex != null) {
            InternalErrorHandler.handleException(ex);
        }
        return null;
    }

    /**
     * Compares for equivalence.
     * @param that object to compare
     * @return true if object matches
     */
    public boolean equals(Object that) {
        if (! that.getClass().equals(this.getClass())) {
            return false;
        } else {
            AuthenticationHeader other = (AuthenticationHeader) that;
            return (equalsIgnoreCase(this.scheme, other.scheme) &&
                    this.parameters.equals(other.parameters));
        }
    }

    /**
     * Gets the value of the header (just returns the scheme).
     * @return the scheme object.
     */
    public Object getValue() {
        return getScheme();

    }

}
