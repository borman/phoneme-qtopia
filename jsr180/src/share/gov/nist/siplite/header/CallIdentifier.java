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
import gov.nist.siplite.parser.Lexer;

/**
 * The call identifer that goes into a callID header and a in-reply-to header.
 * @see CallIdHeader
 */
public final class CallIdentifier extends GenericObject {

    /**
     * localId field
     */
    protected String localId;

    /**
     * host field
     */
    protected String host;

    /**
     * Default constructor
     */
    public CallIdentifier() {}

    /**
     * Constructor
     * @param localId is the local id.
     * @param host is the host.
     */
    public CallIdentifier(String localId, String host) {
        this.localId = localId;
        this.host = host;
    }

    /**
     * constructor
     * @param cid String to set
     * @throws IllegalArgumentException if cid is null or is not a token,
     * or token@token
     */
    public CallIdentifier(String cid) throws IllegalArgumentException {
        setCallIdHeader(cid);
    }

    /**
     * Get the encoded version of this id.
     * @return String to set
     */
    public String encode() {
        if (host != null) {
            return localId + Separators.AT + host;
        } else {
            return localId;
        }
    }

    /**
     * Compare two call identifiers for equality.
     * @param other Object to set
     * @return true if the two call identifiers are equals, false
     * otherwise
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass())) {
            return false;
        }
        CallIdentifier that = (CallIdentifier) other;
        if (this.localId.compareTo(that.localId) != 0) {
            return false;
        }
        if (this.host == that.host)
            return true;
        if ((this.host == null && that.host != null) ||
                (this.host != null && that.host == null)) return false;
        if (Utils.compareToIgnoreCase(host, that.host) != 0) {
            return false;
        }
        return true;
    }

    /**
     * get the LocalId field
     * @return String
     */
    public String getLocalId() {
        return localId;
    }

    /**
     * get the host field
     * @return host member String
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the localId member
     * @param localId String to set
     */
    public void setLocalId(String localId) {
        this.localId = localId;
    }

    /**
     * set the callId field
     * @param cid Strimg to set
     * @throws IllegalArgumentException if cid is null or is not a token or
     * token@token
     */
    public void setCallIdHeader(String cid) throws IllegalArgumentException {
        if (cid == null)
            throw new IllegalArgumentException("NULL!");
        int index = cid.indexOf('@');
        if (index == -1) {
            checkValue(cid);
            localId = cid;
            host = null;
        } else {
            if (index == 0 || index == cid.length()-1) {
                throw new IllegalArgumentException
                        ("CallIdHeader must be token@token or token");
            }
            String temp1 = cid.substring(0, index);
            String temp2 = cid.substring(index+1, cid.length());
            checkValue(temp1);
            checkValue(temp2);
            localId = temp1;
            host = temp2;
        }
    }

    /**
     * Checks Call-Id value validity
     * 
     * @param cid string to be checked
     * 
     * @throws IllegalArgumentException in case of illegal symbol use
     */
    private void checkValue(String cid) throws IllegalArgumentException {
        // RFC 3261 p.228
        // Call-ID  =  ( "Call-ID" / "i" ) HCOLON callid
        // callid   =  word [ "@" word ]
        // word        =  1*(alphanum / "-" / "." / "!" / "%" / "*" /
        //                "_" / "+" / "`" / "'" / "~" /
        //                "(" / ")" / "<" / ">" /
        //                ":" / "\" / DQUOTE /
        //                "/" / "[" / "]" / "?" /
        //                "{" / "}" )
        // The word construct is used in
        // Call-ID to allow most separators to be used.
        String word = "-.!%*_+`'~()<>:\\\"/[]?{}";
        char c;
        for (int i = 0; i < cid.length(); i++) {
            c = cid.charAt(i);
            if (Lexer.isAlpha(c) || Lexer.isHexDigit(c) ||
                word.indexOf(c) != -1) {
                 continue;
            }
            throw new IllegalArgumentException("Wrong Call-Id value:"
                                               + "illegal use of symbol '"
                                               +c+"' at '"+cid+"'"); 
        }
    }

    /**
     * Set the host member
     * @param host String to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Clone - do a deep copy.
     * @return Object CallIdentifier
     */
    public Object clone() {
        CallIdentifier retval = new CallIdentifier();

        if (this.localId != null) retval.localId = new String(this.localId);
        if (this.host != null) retval.host = new String(this.host);
        return retval;
    }

    /**
     * Encodes the object. Calls encode().
     * @return String canonical encoded version of this CallIdentifier.
     */
    public String toString() {
        return encode();
    }

}
