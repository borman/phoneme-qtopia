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
package gov.nist.siplite.header;

import java.util.Hashtable;

/**
 * A mapping class that returns the Header for a given header name.
 */
public class NameMap {
    /** Contents of the name mapping table. */
    static Hashtable nameMap;

    static {
        initializeNameMap();
    }

    /**
     * Adds an entry to the name map table.
     * @param headerName the header field label key
     * @param clazz the class that handles the processing of the named header
     */
    protected static void putNameMap(String headerName, Class clazz) {
        nameMap.put(headerName.toLowerCase(), clazz);
    }

    /**
     * Checks if the parameter is a short form of some header's name
     * and if this is true, expands it to the full form.
     * @param headerName the name to expand
     * @return the expanded name or the source name if it was not expanded
     */
    public static String expandHeaderName(String headerName) {
        /*
         * Support for short forms of header names.
         *
         * i call-id; m contact; e content-encoding; l content-length;
         * c content-type; f from; s subject; k supported; t to; v via;
         * o event; r refer-to
         */
        String   shortNames = "imelcfsktvoura";
        String[] fullNames = {
            Header.CALL_ID, Header.CONTACT, Header.CONTENT_ENCODING,
            Header.CONTENT_LENGTH, Header.CONTENT_TYPE, Header.FROM,
            Header.SUBJECT, Header.SUPPORTED, Header.TO, Header.VIA,
            Header.EVENT, Header.ALLOW_EVENTS, Header.REFER_TO,
            Header.ACCEPT_CONTACT
        };
        String name = headerName;

        if (name.length() == 1) {
            int idx = shortNames.indexOf(name.toLowerCase().charAt(0));
            if (idx != -1) {
                name = fullNames[idx];
            }
        }

        return name;
    }

    /**
     * Looks up the class to use from the header field label.
     * @param headerName the key for looking up class handler
     * @return the class handler for requested header field
     */
    public static Class getClassFromName(String headerName) {
        return (Class) nameMap.get(headerName.toLowerCase());
    }

    /**
     * Checks if requested header is supported.
     * @param headerName the key for looking up class handler
     * @return true if the header has a class handler
     */
    public static boolean isHeaderSupported(String headerName) {
        return nameMap.containsKey(headerName);
    }

    /**
     * Initializes the header to class mapping table.
     */
    private static void initializeNameMap() {
        nameMap = new Hashtable();

        putNameMap(Header.CSEQ, CSeqHeader.clazz); // 1

        putNameMap(Header.RECORD_ROUTE, RecordRouteHeader.clazz); // 2

        putNameMap(Header.VIA, ViaHeader.clazz); // 3

        putNameMap(Header.FROM, FromHeader.clazz); // 4

        putNameMap(Header.CALL_ID, CallIdHeader.clazz); // 5

        putNameMap(Header.MAX_FORWARDS, MaxForwardsHeader.clazz); // 6


        putNameMap(Header.PROXY_AUTHENTICATE,
                ProxyAuthenticateHeader.clazz); // 7

        putNameMap(Header.CONTENT_TYPE, ContentTypeHeader.clazz); // 8

        putNameMap(Header.CONTENT_LENGTH, ContentLengthHeader.clazz); // 9

        putNameMap(Header.ROUTE, RouteHeader.clazz); // 10

        putNameMap(Header.CONTACT, ContactHeader.clazz); // 11

        putNameMap(Header.WWW_AUTHENTICATE,
                WWWAuthenticateHeader.clazz); // 12

        putNameMap(Header.PROXY_AUTHORIZATION,
                ProxyAuthorizationHeader.clazz); // 13

        putNameMap(Header.DATE, DateHeader.clazz); // 14

        putNameMap(Header.EXPIRES, ExpiresHeader.clazz); // 15

        putNameMap(Header.AUTHORIZATION, AuthorizationHeader.clazz); // 16

        putNameMap(Header.TO, ToHeader.clazz); // 17 -- mg

        putNameMap(Header.EVENT, EventHeader.clazz); // 18

        putNameMap(Header.SUBSCRIPTION_STATE, SubscriptionStateHeader.clazz);

        putNameMap(Header.RSEQ, RSeqHeader.clazz); // 20

        putNameMap(Header.RACK, RAckHeader.clazz); // 21

        putNameMap(Header.ACCEPT_CONTACT, AcceptContactHeader.clazz); // 22
    }

}
