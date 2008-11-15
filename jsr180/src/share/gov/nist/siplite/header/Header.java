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
import java.util.Calendar;

/**
 * Generic SipHeader class
 * All the Headers inherit of this class
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public abstract class Header extends GenericObject {

    /**
     * Constant ERROR_INFO field.
     */
    public static final String ERROR_INFO = "Error-Info";

    /**
     * Constant MIME_VERSION field.
     */
    public static final String MIME_VERSION = "Mime-Version";

    /**
     * Constant IN_REPLY_TO field.
     */
    public static final String IN_REPLY_TO = "In-Reply-To";

    /**
     * Constant ALLOW field.
     */
    public static final String ALLOW = "Allow";

    /**
     * Constant ALLOW_EVENTS field.
     */
    public static final String ALLOW_EVENTS = "Allow-Events";

    /**
     * Constant CONTENT_LANGUAGE field.
     */
    public static final String CONTENT_LANGUAGE = "Content-Language";

    /**
     * Constant CALL_INFO field.
     */
    public static final String CALL_INFO = "Call-Info";

    /**
     * Constant CSEQ field.
     */
    public static final String CSEQ = "CSeq";

    /**
     * Constant ALERT_INFO field.
     */
    public static final String ALERT_INFO = "Alert-Info";

    /**
     * Constant ACCEPT_ENCODING field.
     */
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * Constant ACCEPT field.
     */
    public static final String ACCEPT = "Accept";

    /**
     * Constant ENCRYPTION field.
     */
    public static final String ENCRYPTION = "Encryption";

    /**
     * Constant ACCEPT_LANGUAGE field.
     */
    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    /**
     * Constant ACCEPT_CONTACT field.
     */
    public static final String ACCEPT_CONTACT = "Accept-Contact";

    /**
     * Constant RECORD_ROUTE field.
     */
    public static final String RECORD_ROUTE = "Record-Route";

    /**
     * Constant TIMESTAMP field.
     */
    public static final String TIMESTAMP = "Timestamp";

    /**
     * Constant TO field.
     */
    public static final String TO = "To";

    /**
     * Constant VIA field.
     */
    public static final String VIA = "Via";

    /**
     * Constant FROM field.
     */
    public static final String FROM = "From";

    /**
     * Constant CALL_ID field.
     */
    public static final String CALL_ID = "Call-ID";

    /**
     * Constant AUTHENTICATION_INFO field.
     */
    public static final String AUTHENTICATION_INFO = "Authentication-Info";

    /**
     * Constant AUTHORIZATION field.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Constant PROXY_AUTHENTICATE field.
     */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * Constant SERVER field.
     */
    public static final String SERVER = "Server";

    /**
     * Constant UNSUPPORTED field.
     */
    public static final String UNSUPPORTED = "Unsupported";

    /**
     * Constant RETRY_AFTER field.
     */
    public static final String RETRY_AFTER = "Retry-After";

    /**
     * Constant CONTENT_TYP field.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Constant CONTENT_ENCODING field.
     */
    public static final String CONTENT_ENCODING = "Content-Encoding";

    /**
     * Constant CONTENT_LENGTH field.
     */
    public static final String CONTENT_LENGTH = "Content-Length";

    /**
     * Constant HIDE field.
     */
    public static final String HIDE = "Hide";

    /**
     * Constant ROUTE field.
     */
    public static final String ROUTE = "Route";

    /**
     * Constant CONTACT field.
     */
    public static final String CONTACT = "Contact";

    /**
     * Constant WWW_AUTHENTICATE field.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    /**
     * Constant MAX_FORWARDS field.
     */
    public static final String MAX_FORWARDS = "Max-Forwards";

    /**
     * Constant ORGANIZATION field.
     */
    public static final String ORGANIZATION = "Organization";

    /**
     * Constant PROXY_AUTHORIZATION field.
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * Constant PROXY_REQUIRE field.
     */
    public static final String PROXY_REQUIRE = "Proxy-Require";

    /**
     * Constant REQUIRE field.
     */
    public static final String REQUIRE = "Require";

    /**
     * Constant CONTENT_DISPOSITION field.
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Constant SUBJECT field.
     */
    public static final String SUBJECT = "Subject";

    /**
     * Constant USER_AGENT field.
     */
    public static final String USER_AGENT = "User-Agent";

    /**
     * Constant WARNING field.
     */
    public static final String WARNING = "Warning";

    /**
     * Constant PRIORITY field.
     */
    public static final String PRIORITY = "Priority";

    /**
     * Constant DATE field.
     */
    public static final String DATE = "Date";

    /**
     * Constant MIN_EXPIRES field.
     */
    public static final String MIN_EXPIRES = "Min-Expires";

    /**
     * Constant EXPIRES field.
     */
    public static final String EXPIRES = "Expires";

    /**
     * Constant RESPONSE_KEY field.
     */
    public static final String RESPONSE_KEY = "Response-Key";

    /**
     * Constant WARN_AGENT field.
     */
    public static final String WARN_AGENT = "Warn-Agent";

    /**
     * Constant SUPPORTED field.
     */
    public static final String SUPPORTED = "Supported";

    /** Constant EVENT field. */
    public static final String EVENT = "Event";

    /** Constant EXTENSION field. */
    public static final String EXTENSION = "Extension";

    /** Constant SUBSCRIPTION_STATE field. */
    public static final String SUBSCRIPTION_STATE = "Subscription-State";

    /** Constant SIP_ETAG field (RFC 3903, p. 21). */
    public static final String SIP_ETAG = "SIP-ETag";

    /** Constant SIP_IF_MATCH field (RFC 3903, p. 22). */
    public static final String SIP_IF_MATCH = "SIP-If-Match";

    /** Constant Refer-To field (RFC 3515, p. 3). */
    public static final String REFER_TO = "Refer-To";

    /** Constant RSEQ field (RFC 3262, p. 11). */
    public static final String RSEQ = "RSeq";

    /** Constant RAck field (RFC 3262, p. 11). */
    public static final String RACK = "RAck";

    /**
     * Name of the header.
     */
    public String headerName;

    /**
     * Value of the header.
     */
    public String headerValue;

    /**
     * Array of the headers that can not have any parameters.
     *
     * NOTE: If the size of this array is changed,
     *       getStringHash() method also must be changed!
     */
    public static final String[] parameterLessHeaders = {
        AUTHENTICATION_INFO,  // hash = 0
        null,
        ALLOW,                // 2
        null, null, null, null, null,
        IN_REPLY_TO,          // 8
        PRIORITY,             // 9
        null, null,
        MIME_VERSION,         // 12
        SERVER,               // 13
        TIMESTAMP,            // 14
        USER_AGENT,           // 15
        null,
        MIN_EXPIRES,          // 17
        SUBJECT,              // 18
        null, null,
        // We can't create an instance of ParameterLessHeader class for
        // Content-Length header, because there is a check like
        // "header instanceof ContentLengthHeader" in Message.encodeAsBytes().
        null, // CONTENT_LENGTH,       // 21
        CONTENT_LANGUAGE,     // 22
        null,
        WARNING,              // 24
        CONTENT_ENCODING,     // 25
        ORGANIZATION,         // 26
        UNSUPPORTED,          // 27
        null, null, null,
        REQUIRE,              // 31
        SUPPORTED,            // 32
        null, null, null,
        PROXY_REQUIRE,        // 36
        null, null
    };

    /**
     * Default constructor.
     */
    public Header() { }

    /**
     * Constructor given the name.
     * @param headerName the initial header field name
     */
    public Header(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Constructor given the name and value.
     * @param headerName is the header name.
     * @param headerValue is the header value.
     */
    public Header(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    /**
     * Sets the header name field.
     * @param name is the header name to set.
     */
    public void setHeaderName(String name) {
        this.headerName = name;
    }

    /**
     * Sets the header value field.
     * @param value is the value field to set.
     */
    public void setHeaderValue(String value) {
        this.headerValue = value;
    }

    /**
     * Gets the header name.
     * @return headerName field
     */
    public String getHeaderName() {
        return this.headerName;
    }

    /**
     * Alias for getHeaderName.
     * @return headerName field
     */
    public String getName() {
        return this.headerName;
    }

    /**
     * Gets the header value.
     * @return headerValue field
     */
    public String getHeaderValue() {
        return this.encodeBody();
    }

    /**
     * Encodes the header into a String.
     * @return String
     */
    public String encode() {
        if (headerName == null) {
            return "";
        } else {
            return headerName + Separators.COLON + Separators.SP +
                encodeBody() + Separators.NEWLINE;
        }
    }

    /**
     * A place holder -- this should be overriden with an actual
     * clone method.
     * @return need revisit copy of the current object
     */
    public Object clone() {
        return this;

    }

    /**
     * A utility for encoding dates.
     * @param date the object to encode
     * @return the encode date string
     */
    public static String encodeCalendar(Calendar date) {
        StringBuffer sbuf = new StringBuffer();
        int wkday = date.get(Calendar.DAY_OF_WEEK);
        switch (wkday) {
            case Calendar.MONDAY:
                sbuf.append("Mon");
                break;
            case Calendar.TUESDAY:
                sbuf.append("Tue");
                break;
            case Calendar.WEDNESDAY:
                sbuf.append("Wed");
                break;
            case Calendar.THURSDAY:
                sbuf.append("Thu");
                break;
            case Calendar.FRIDAY:
                sbuf.append("Fri");
                break;
            case Calendar.SATURDAY:
                sbuf.append("Sat");
                break;
            case Calendar.SUNDAY:
                sbuf.append("Sun");
                break;
            default:
                new Exception
                        ("bad day of week?? Huh?? " + wkday).printStackTrace();
                return null;
        }
        int day = date.get(Calendar.DAY_OF_MONTH);
        if (day < 10) sbuf.append(", 0" + day);
        else sbuf.append(", " + day);
        sbuf.append(" ");
        int month = date.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
                sbuf.append("Jan");
                break;
            case Calendar.FEBRUARY:
                sbuf.append("Feb");
                break;
            case Calendar.MARCH:
                sbuf.append("Mar");
                break;
            case Calendar.APRIL:
                sbuf.append("Apr");
                break;
            case Calendar.MAY:
                sbuf.append("May");
                break;
            case Calendar.JUNE:
                sbuf.append("Jun");
                break;
            case Calendar.JULY:
                sbuf.append("Jul");
                break;
            case Calendar.AUGUST:
                sbuf.append("Aug");
                break;
            case Calendar.SEPTEMBER:
                sbuf.append("Sep");
                break;
            case Calendar.OCTOBER:
                sbuf.append("Oct");
                break;
            case Calendar.NOVEMBER:
                sbuf.append("Nov");
                break;
            case Calendar.DECEMBER:
                sbuf.append("Dec");
                break;
            default:
                return null;
        }

        sbuf.append(" ");
        int year = date.get(Calendar.YEAR);
        sbuf.append(year);
        sbuf.append(" ");
        int hour = date.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) sbuf.append("0"+hour);
        else sbuf.append(hour);
        sbuf.append(":");
        int min = date.get(Calendar.MINUTE);
        if (min < 10) sbuf.append("0"+min);
        else sbuf.append(min);
        sbuf.append(":");
        int sec = date.get(Calendar.SECOND);
        if (sec < 10) sbuf.append("0"+sec);
        else sbuf.append(sec);

        sbuf.append(" GMT");
        return sbuf.toString();

    }

    /**
     * Gets the parameters for the header as a nameValue list.
     * @return the name value list of header field paramaters
     */
    public abstract NameValueList getParameters();

    /**
     * Gets the value for the header as opaque object (returned value
     * will depend upon the header. Note that this is not the same as
     * the getHeaderValue above.
     * @return the header field value
     */
    public abstract Object getValue();

    /**
     * Gets the stuff that follows the headerName.
     * @return a string representation of the stuff that follows the
     * headerName
     */
    protected abstract String encodeBody();

    /**
     * Returns the encoded text contents.
     * @return encode string of object contents
     */
    public String toString() {
        return this.encode();
    }

    /**
     * Calculates a signle-byte hash code of the string.
     * @param s is a string for which a hash code must be calculated
     * @return hash code (from 0 to parameterLessHeaders.length) of the string
     */
    public static byte getStringHash(String s) {
        String sl = s.toLowerCase();
        int len  = sl.length(), headersCount = parameterLessHeaders.length;
        int hash = len;

        for (int i = 0; i < len; i++) {
            hash += sl.charAt(i);
        }

        hash  = (byte)(hash % headersCount);

        if (hash == 6) {
            hash = (hash + 9 + sl.charAt(0)) % headersCount;
        } else if (hash == 7) {
            hash = (hash + 13 + sl.charAt(0)) % headersCount;
        }

        // System.out.println("hash('" + s + "') = " + hash);

        return (byte)hash;
    }

    /**
     * Checks if the specified header can have some parameters.
     * @param name the name of the header
     * @return true if the header can not have any parameters, false otherwise
     */
    public static boolean isParameterLess(String name) {
        byte hash;

        // IMPL_NOTE: remove!!!
        // for (int i = 0; i < parameterLessHeaders.length; i++) {
        //     if (parameterLessHeaders[i] != null)
        //         getStringHash(parameterLessHeaders[i]);
        // }

        hash = getStringHash(name);

        return (parameterLessHeaders[hash] != null &&
                parameterLessHeaders[hash].equalsIgnoreCase(name));
    }

    /**
     * Checks if a header with the given name is an Authorization
     * or Authentication header.
     * @param name the name of the header
     * @return true if the header is an Authorization or Authentication header
     */
    public static boolean isAuthorization(String name) {
        if (name == null) {
            return false;
        }

        return (name.equalsIgnoreCase(Header.AUTHORIZATION) ||
                name.equalsIgnoreCase(Header.PROXY_AUTHORIZATION) ||
                name.equalsIgnoreCase(Header.PROXY_AUTHENTICATE) ||
                name.equalsIgnoreCase(Header.WWW_AUTHENTICATE));
    }

    /**
     * Search for the reliable tag, "100rel" in a "Require" Header value.
     * This method is internally used to identify if the response
     * received at UAC is reliable provisional response
     * @param strOptionTags List of option tags that is actually a value
     *   of Require Header
     * @return true if "100rel" tags is found; else false
     */
    public static boolean isReliableTagPresent(String strOptionTags) {
        if (Utils.equalsIgnoreCase(strOptionTags, "100rel")) {
            return true;
        } else {
            String tag = null;
            // int tagsLength = strOptionTags.length();
            int delimIndex = strOptionTags.indexOf(Separators.COMMA);
            while (delimIndex > 0) {
                tag = strOptionTags.substring(0, delimIndex).trim();
                strOptionTags = strOptionTags.substring(delimIndex + 1,
                        strOptionTags.length()).trim();
                if (Utils.equalsIgnoreCase(tag, "100rel") ||
                    Utils.equalsIgnoreCase(strOptionTags, "100rel")) {
                    return true;
                }
                delimIndex = strOptionTags.indexOf(Separators.COMMA);
            }
        }
        return false;
    }


}
