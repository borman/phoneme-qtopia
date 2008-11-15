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

import gov.nist.siplite.address.*;
import gov.nist.core.*;


/**
 * Contact Item. There can be several (strung together in a ContactList).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class ContactHeader extends AddressParametersHeader {
    /** Class handle. */
    public static Class clazz;

    /** Contact header field label. */
    public static final String NAME = Header.CONTACT;

    /** 'expires' parameter label. */
    public static final String PARAM_EXPIRES = "expires";

    /** 'action' parameter label. */
    public static final String PARAM_ACTION  = "action";

    /** 'q' parameter label. */
    public static final String PARAM_Q = "q";

    /**
     * Static initializer.
     */
    static {
        clazz = new ContactHeader().getClass();
    }

    /**
     * wildCardFlag field.
     */
    protected boolean wildCardFlag;

    /**
     * comment field.
     */
    protected String comment;


    /**
     * Default constructor.
     */
    public ContactHeader() {
        super(CONTACT);
        wildCardFlag = false;
    }

    /**
     * Encode this into a cannonical String.
     * @return String
     */
    public String encodeBody() {
        String encoding = "";

        if (wildCardFlag) {
            return encoding + "*";
        }

        if (address != null) {
            // RFC 3261, p. 223, 228
            // addr-spec  =  SIP-URI / SIPS-URI / absoluteURI
            // SIP-URI    =  "sip:" [ userinfo ] hostport
            //                uri-parameters [ headers ]
            encoding += address.encode();

            /*
            if (address.getAddressType() == Address.NAME_ADDR) {
                encoding += address.encode();
            } else {
                // Encoding in canonical form must have <> around address.
                encoding += "<" + address.encode() + ">";
            }
            */
        }

        encoding += encodeWithSep();

        if (comment != null) {
            encoding += "(" + comment + ")";
        }

        return encoding;
    }

    /**
     * get the WildCardFlag field
     * @return boolean
     */
    public boolean getWildCardFlag() {
        return wildCardFlag;
    }

    /**
     * get the Action field.
     * @return String
     */
    public String getAction() {
        return getParameter(PARAM_ACTION);
    }

    /**
     * get the address field.
     * @return Address
     */
    public Object getValue() {
        return address;
    }

    /**
     * get the comment field
     * @return String
     */
    public String getComment() {
        return comment;
    }

    /**
     * get Expires field
     * @return String
     */
    public String getExpires() {
        return getParameter(PARAM_EXPIRES);
    }

    /**
     * Set the expiry time in seconds.
     * @param  expires to set.
     */
    public void setExpires(String expires) {
        setParameter(PARAM_EXPIRES, expires);
    }

    /**
     * Set the expiry time in seconds.
     * @param  expires to set.
     */
    public void setExpires(int expires) {
        setParameter(PARAM_EXPIRES, new Integer(expires).toString());
    }

    /**
     * get the Q-value
     * @return String
     */
    public String getQValue() {
        return getParameter(PARAM_Q);
    }

    /**
     * Returns true if Q-value is present.
     * @return true if this header has a Q-value, false otherwise.
     */
    public boolean hasQValue() {
        return hasParameter(PARAM_Q);
    }

    /**
     * Sets the wildCardFlag member.
     * @param w boolean to set
     */
    public void setWildCardFlag(boolean w) {
        wildCardFlag = w;
    }

    /**
     * Sets the address member.
     * @param newAddress Address to set
     */
    public void setAddress(Address newAddress) {
        if (newAddress != null) {
            address = newAddress;
        }
    }

    /**
     * Sets the comment member.
     * @param newComment String to set
     */
    public void setComment(String newComment) {
        if (newComment != null) {
            comment = newComment;
        }
    }

    /**
     * Clone - do a deep copy.
     * @return Object Contact
     */
    public Object clone() {
        ContactHeader retval = new ContactHeader();
        retval.wildCardFlag = this.wildCardFlag;
        if (this.comment != null) retval.comment = new String(this.comment);
        if (this.parameters != null)
            retval.parameters = (NameValueList)
            parameters.clone();
        if (this.address != null) retval.address = (Address)address.clone();
        return retval;
    }
}
