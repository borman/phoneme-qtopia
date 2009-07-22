/*
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
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

import java.util.Hashtable;
import gov.nist.siplite.header.*;
import gov.nist.core.*;

/**
 * A factory class that does a name lookup on a registered parser and
 * returns a header parser for the given name.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ParserFactory {
    /** Current parser table. */
    private static Hashtable parserTable;

    /** Parameters to pass to this parser when instantiated. */
    private static Class[] constructorArgs;

    static {
        parserTable = new Hashtable();
        constructorArgs = new Class[1];
        constructorArgs[0] = new String().getClass();

        parserTable.put("t", ToParser.class);
        parserTable.put(ToHeader.NAME.toLowerCase(), ToParser.class);

        parserTable.put(FromHeader.NAME.toLowerCase(), FromParser.class);
        parserTable.put("f", FromParser.class);

        parserTable.put(CSeqHeader.NAME.toLowerCase(), CSeqParser.class);

        parserTable.put(ViaHeader.NAME.toLowerCase(), ViaParser.class);
        parserTable.put("v", ViaParser.class);

        parserTable.put(ContactHeader.NAME.toLowerCase(), ContactParser.class);
        parserTable.put("m", ContactParser.class);

        parserTable.put(
                ContentTypeHeader.NAME.toLowerCase(), ContentTypeParser.class);
        parserTable.put("c", ContentTypeParser.class);

        parserTable.put(ContentLengthHeader.NAME.toLowerCase(),
                ContentLengthParser.class);
        parserTable.put("l", ContentLengthParser.class);

        parserTable.put(AuthorizationHeader.NAME.toLowerCase(),
                AuthorizationParser.class);

        parserTable.put(WWWAuthenticateHeader.NAME.toLowerCase(),
                WWWAuthenticateParser.class);

        parserTable.put(CallIdHeader.NAME.toLowerCase(), CallIDParser.class);
        parserTable.put("i", CallIDParser.class);

        parserTable.put(RouteHeader.NAME.toLowerCase(), RouteParser.class);

        parserTable.put(RecordRouteHeader.NAME.toLowerCase(),
                RecordRouteParser.class);

        parserTable.put(DateHeader.NAME.toLowerCase(), DateParser.class);

        parserTable.put(ProxyAuthorizationHeader.NAME.toLowerCase(),
                ProxyAuthorizationParser.class);

        parserTable.put(ProxyAuthenticateHeader.NAME.toLowerCase(),
                ProxyAuthenticateParser.class);

        parserTable.put(MaxForwardsHeader.NAME.toLowerCase(),
                MaxForwardsParser.class);

        parserTable.put(ExpiresHeader.NAME.toLowerCase(), ExpiresParser.class);

        parserTable.put(EventHeader.NAME.toLowerCase(), EventParser.class);
        parserTable.put("o", EventParser.class);

        parserTable.put(SubscriptionStateHeader.NAME.toLowerCase(),
                SubscriptionStateParser.class);

        parserTable.put("a", AcceptContactParser.class);
        parserTable.put(AcceptContactHeader.NAME.toLowerCase(),
            AcceptContactParser.class);
        
        parserTable.put(Header.CONTENT_DISPOSITION.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.MIME_VERSION.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.MIN_EXPIRES.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.ORGANIZATION.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.PRIORITY.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.RETRY_AFTER.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.SERVER.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.SUBJECT.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put("s", SingleHeaderParser.class);
        parserTable.put(Header.TIMESTAMP.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.USER_AGENT.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.SIP_IF_MATCH.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.SIP_ETAG.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.RSEQ.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.RACK.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.REFER_TO.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put("r", SingleHeaderParser.class);
        parserTable.put(Header.RESPONSE_KEY.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.HIDE.toLowerCase(), 
                SingleHeaderParser.class);
        parserTable.put(Header.ENCRYPTION.toLowerCase(), 
                SingleHeaderParser.class);
    }

    /**
     * Creates a parser for a header. This is the parser factory.
     * @param line the text to be parsed
     * @return the parsed data
     * @exception ParseException if a parsing error occurs
     */
    public static HeaderParser createParser(String line) throws ParseException {
        String headerName = Lexer.getHeaderName(line);
        String headerValue = Lexer.getHeaderValue(line);

        if (headerName == null || headerValue == null) {
            throw new ParseException("The header name or value is null", 0);
        }

        headerName = NameMap.expandHeaderName(headerName);
        Class parserClass = (Class) parserTable.get(headerName.toLowerCase());

        if (parserClass != null) {
            Exception ex = null;
            try {
                HeaderParser retval = (HeaderParser) parserClass.newInstance();
                retval.setHeaderToParse(line);
                return retval;
            } catch (InstantiationException ie) {
                ex = ie;
            } catch (IllegalAccessException iae) {
                ex = iae;
            }
            if (ex != null) {
                // print system message and exit
                InternalErrorHandler.handleException(ex);
            }
            return null;
        } else {
            if (Header.isParameterLess(headerName) ||
                    headerName.equalsIgnoreCase(Header.RSEQ) ||
                        headerName.equalsIgnoreCase(Header.RACK)) {
                // The header can't have any parameters
                HeaderParser retval = new ParameterLessParser(line);
                return retval;
            } else {
                // Create a generic header parser
                HeaderParser retval = new ExtensionParser(line);
                return retval;
            }
        }
    }
}
