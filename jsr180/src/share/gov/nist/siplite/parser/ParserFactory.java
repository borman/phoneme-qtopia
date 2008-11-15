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

        parserTable.put("t", new ToParser().getClass());
        parserTable.put(ToHeader.NAME.toLowerCase(), new ToParser().getClass());

        parserTable.put(FromHeader.NAME.toLowerCase(),
                new FromParser().getClass());
        parserTable.put("f", new FromParser().getClass());

        parserTable.put(CSeqHeader.NAME.toLowerCase(),
                new CSeqParser().getClass());

        parserTable.put(ViaHeader.NAME.toLowerCase(),
                new ViaParser().getClass());
        parserTable.put("v", new ViaParser().getClass());

        parserTable.put(ContactHeader.NAME.toLowerCase(),
                new ContactParser().getClass());
        parserTable.put("m", new ContactParser().getClass());

        parserTable.put(
                ContentTypeHeader.NAME.toLowerCase(),
                new ContentTypeParser().getClass());
        parserTable.put("c", new ContentTypeParser().getClass());

        parserTable.put(
                ContentLengthHeader.NAME.toLowerCase(),
                new ContentLengthParser().getClass());
        parserTable.put("l", new ContentLengthParser().getClass());

        parserTable.put(
                AuthorizationHeader.NAME.toLowerCase(),
                new AuthorizationParser().getClass());

        parserTable.put(
                WWWAuthenticateHeader.NAME.toLowerCase(),
                new WWWAuthenticateParser().getClass());

        parserTable.put(CallIdHeader.NAME.toLowerCase(),
                new CallIDParser().getClass());
        parserTable.put("i", new CallIDParser().getClass());

        parserTable.put(RouteHeader.NAME.toLowerCase(),
                new RouteParser().getClass());

        parserTable.put(
                RecordRouteHeader.NAME.toLowerCase(),
                new RecordRouteParser().getClass());

        parserTable.put(DateHeader.NAME.toLowerCase(),
                new DateParser().getClass());

        parserTable.put(
                ProxyAuthorizationHeader.NAME.toLowerCase(),
                new ProxyAuthorizationParser().getClass());

        parserTable.put(
                ProxyAuthenticateHeader.NAME.toLowerCase(),
                new ProxyAuthenticateParser().getClass());

        parserTable.put(
                MaxForwardsHeader.NAME.toLowerCase(),
                new MaxForwardsParser().getClass());

        parserTable.put(ExpiresHeader.NAME.toLowerCase(),
                new ExpiresParser().getClass());

        parserTable.put(EventHeader.NAME.toLowerCase(),
                new EventParser().getClass());
        parserTable.put("o", new EventParser().getClass());

        parserTable.put(SubscriptionStateHeader.NAME.toLowerCase(),
                new SubscriptionStateParser().getClass());

        parserTable.put("a", new AcceptContactParser().getClass());
        parserTable.put(AcceptContactHeader.NAME.toLowerCase(),
            new AcceptContactParser().getClass());
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
