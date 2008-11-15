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
package gov.nist.core;

import gov.nist.siplite.parser.Lexer;

/**
 * Parser for host names.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public class HostNameParser extends ParserCore {


    /**
     * The lexer is initialized with the buffer.
     * @param lexer class to use for parsing
     */
    public HostNameParser(LexerCore lexer) {
        this.lexer = lexer;
        lexer.selectLexer("charLexer");
    }

    /**
     * Gets the domain name.
     * @return the domain label
     * @exception ParseException if an error occurs parsing
     */
    protected String domainLabel() throws ParseException {
        StringBuffer retval = new StringBuffer();
        char la;
        if (debug) dbg_enter("domainLabel");
        try {
            while (lexer.hasMoreChars()) {
                la = lexer.lookAhead(0);
                if (LexerCore.isAlpha(la)) {
                    lexer.consume(1);
                    retval.append(la);
                } else if (LexerCore.isDigit(la)) {
                    lexer.consume(1);
                    retval.append(la);
                } else if (la == '-') {
                    lexer.consume(1);
                    retval.append(la);
                } else
                    break;
            }
            // Debug.println("returning " + retval.toString());
            if (retval.length() == 0) {
                throw new ParseException("Error parsing domain label  "
                                         + lexer.getBuffer(), lexer.getPtr());
            }
            return retval.toString();
        } finally {
            if (debug) dbg_leave("domainLabel");
        }
    }
    /**
     * Gets the IPV6 reference.
     * @return the IPV6 address
     */
    protected String ipv6Reference()
    throws ParseException {
        StringBuffer retval = new StringBuffer();
        if (debug) dbg_enter("domainLabel");
        try {
            while (lexer.hasMoreChars()) {
                char la = lexer.lookAhead(0);
                if (LexerCore.isHexDigit(la)) {
                    lexer.consume(1);
                    retval.append(la);
                } else if (la == '.'
                        || la == ':'
                        || la == '[') {
                    lexer.consume(1);
                    retval.append(la);
                } else if (la == ']') {
                    lexer.consume(1);
                    retval.append(la);
                    return retval.toString();
                } else
                    break;
            }

            throw new ParseException
                    (lexer.getBuffer() + ": Illegal Host name ",
                    lexer.getPtr());
        } finally {
            if (debug) dbg_leave("domainLabel");
        }
    }

    /**
     * Gets the host name.
     * @return the host name
     * @exception ParseException if an error occurs parsing
     */
    public String hostName() throws ParseException {
        if (debug) dbg_enter("host");
        try {
            StringBuffer hname = new StringBuffer();
            // IPv6 referene
            if (lexer.lookAhead(0) == '[') {
                hname.append(ipv6Reference());
            }
            // IPv4 address or hostname
            else {
                String nextTok = domainLabel();
                hname.append(nextTok);
                while (lexer.hasMoreChars()) {
                    // Reached the end of the buffer.
                    if (lexer.lookAhead(0) == '.') {
                        lexer.consume(1);
                        nextTok = domainLabel();
                        hname.append(".");
                        hname.append(nextTok);
                    } else
                        break;
                }
            }

            return hname.toString();
        } finally {
            if (debug) dbg_leave("host");
        }
    }

    /**
     * Gets the host name and port number.
     * @return the host name and port number encoded string
     * @exception ParseException if an error occurs parsing
     */
    public HostPort hostPort() throws ParseException {
        if (debug) dbg_enter("hostPort");
        try {
            int m = lexer.markInputPosition();
            String hostName = this.hostName();
            Host host = new Host();
            HostPort hp = new HostPort();
            boolean parsePort = false;
            if (StringTokenizer.isDigitString(hostName)) { // maybe server URI
                lexer.rewindInputPosition(m); // try to port parsing
                parsePort = true;
            } else { // save hostname
                host.setHostname(hostName);
            }
            hp.setHost(host);

            // For some headers (for ex., Via) there may be spaces
            // between the host name and the port number.
            lexer.SPorHT();

            // Has a port?
            if (!parsePort && lexer.hasMoreChars() && 
                lexer.lookAhead(0) == ':') {
                lexer.consume(1);
                lexer.SPorHT();
                parsePort = true;
            }
            if (parsePort) {
                parsePort(lexer, hp);
            }

            return hp;
        } finally {
            if (debug) dbg_leave("hostPort");
        }

    }


    /**
     * Port parsing method.
     * All port symbols must be digital. Port length must be >0.
     * Resulting port value stores in input HostPort instance.
     * @param lexer class to use for parsing
     * @param hp HostPort instance for accepting port value
     * @exception ParseException if an error occurs parsing
     * @exception NumberFormatException if any number format error
     * @exception IllegalArgumentException if any other format error
     */
    public static void parsePort(LexerCore lexer, HostPort hp) 
        throws ParseException {
        try {
            String port = lexer.number();
            if (port.length() == 0) { // nondigit port symbols
                throw new ParseException("Port format error",
                                          lexer.getPtr());
            } else {
                hp.setPort(Integer.parseInt(port));
            }
        } catch (NumberFormatException nfe) {
            throw new ParseException
                    (lexer.getBuffer() + " :Error parsing port ",
                    lexer.getPtr());
        } catch (IllegalArgumentException iae) {
            // setPort can throw IAE
            throw new ParseException(iae.getMessage(),
                                     lexer.getPtr());
        }
    }


}
