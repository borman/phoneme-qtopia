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

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

/**
 * Implementation of the URI class.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class URI extends GenericObject {
    /** POST Dial method label text. */
    public static final String POSTDIAL = SIPConstants.GENERAL_POSTDIAL;
    /** Phone context parameter label text. */
    public static final String PHONE_CONTEXT_TAG =
            SIPConstants.GENERAL_PHONE_CONTEXT_TAG;
    /** ISDN subaddress parameter label text. */
    public static final String ISUB = SIPConstants.GENERAL_ISUB;
    /** Provider parameter label text. */
    public static final String PROVIDER_TAG = SIPConstants.GENERAL_PROVIDER_TAG;
    /** User name parameter label text. */
    public static final String USER = SIPConstants.GENERAL_USER;
    /** Transport type parameter label text. */
    public static final String TRANSPORT = SIPConstants.GENERAL_TRANSPORT;
    /** Method name parameter label text. */
    public static final String METHOD = SIPConstants.GENERAL_METHOD;
    /** Time to live parameter label text. */
    public static final String TTL = SIPConstants.GENERAL_TTL;
    /** Mail address parameter label text. */
    public static final String MADDR = SIPConstants.GENERAL_MADDR;
    /** LR parameter label text. */
    public static final String LR = SIPConstants.GENERAL_LR;

    /**
     * Imbedded URI.
     */
    protected String uriString;
    /** Current URI scheme. */
    protected String scheme;

    /**
     * Constuctor.
     */
    protected URI() {}

    /**
     * Constructor given the URI string.
     * @param uriString The imbedded URI string.
     * @throws URISyntaxException When there is a syntaz error in the
     * imbedded URI.
     */
    public URI(String uriString) throws ParseException {
        try {
            this.uriString = uriString;
            int colPos = uriString.indexOf(":");

            if (colPos == -1) { // no ":"
                throw new ParseException("URI, no separator after scheme", 0);
            }

            // Don't check the scheme's name, because according
            // to the RFC 3261, p. 224 it may be almost any token,
            // not only 'sip' and 'sips'.

            // rfc3261: when symbol '@' is present in URI, user part
            // can't be empty
            String uriCutScheme = uriString.substring(colPos + 1);
            int symAtPos = uriCutScheme.indexOf("@");

            if (symAtPos == 0) { // first symbol is '@'
                throw new ParseException("URI, no user part", 0);
            }
        } catch (Throwable e) {
            throw new ParseException("URI, Bad URI format", 0);
        }
    }

    /**
     * Encode the URI.
     * @return The encoded URI
     */
    public String encode() {
        return uriString;
    }

    /**
     * Encodes this URI.
     * @return The encoded URI
     */
    public String toString() {
        return this.encode();
    }

    /**
     * URI part of the address without parameters.
     * @return URI part of the address
     */
    public String getPlainURI() {
        return encode();
    }

    /**
     * Overrides the base clone method.
     * @return The Cloned strucutre,
     */
    public Object clone() {
        try {
            return new URI(this.uriString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage() + this.uriString);
        }
    }

    /**
     * Returns the value of the "scheme" of
     * this URI, for example "sip", "sips" or "tel".
     *
     * @return the scheme paramter of the URI
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the value of the "scheme" of
     * this URI, for example "sip", "sips" or "tel".
     *
     * Ref. RFC 3261, p. 224:
     *     scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
     *
     * @param sch the scheme of this URI
     * @throws IllegalArgumentException if the scheme is invalid
     */
    public void setScheme(String sch) throws IllegalArgumentException {
        final String errMsg = "Invalid scheme format";
        char ch;

        // Check if the scheme is valid
        if (sch == null || !StringTokenizer.isAlpha(sch.charAt(0))) {
            throw new IllegalArgumentException(errMsg);
        }

        for (int i = 1; i < sch.length(); i++) {
            ch = sch.charAt(i);

            if (!StringTokenizer.isAlpha(ch) && !StringTokenizer.isDigit(ch) &&
                    (ch != '+') && (ch != '-') && (ch != '.')) {
                throw new IllegalArgumentException(errMsg);
            }
        }

        scheme = sch;
    }

    /**
     * This method determines if this is a URI with a scheme of
     * "sip" or "sips".
     *
     * @return true if the scheme is "sip" or "sips", false otherwise.
     */
    public boolean isSipURI() {
        return this instanceof SipURI;

    }

    /**
     * This method determines if this is a URI with a scheme of
     * "tel"
     *
     * @return true if the scheme is "tel", false otherwise.
     */
    public boolean isTelURL() {
          return this instanceof TelURL;
    }

}
