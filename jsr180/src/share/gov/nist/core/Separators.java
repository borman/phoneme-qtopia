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

/**
 * Separators.
 * Constants for common punctuation used
 * ase textual separators.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface Separators {
    /** Semicolon. */
    public static final String SEMICOLON = ";";
    /** Colon. */
    public static final String COLON = ":";
    /** Comma. */
    public static final String COMMA = ",";
    /** Forward slash. */
    public static final String SLASH = "/";
    /** Space (SP) character. */
    public static final String SP = " ";
    /** Equal sign (EQUALS). */
    public static final String EQUALS = "=";
    /** Asterisk (STAR). */
    public static final String STAR =  "*";
    /** Carriage return followed byte linefeed (NEWLINE) */
    public static final String NEWLINE  =  "\r\n";
    /** Carriage return (RETURN). */
    public static final String RETURN = "\n";
    /** Less than (left angle bracket). */
    public static final String LESS_THAN  =  "<";
    /** Greater than (right angle bracket). */
    public static final String GREATER_THAN  =  ">";
    /** At sign. */
    public static final String AT  =  "@";
    /** Period (DOT). */
    public static final String DOT  =  ".";
    /** Question mark. */
    public static final String QUESTION  =  "?";
    /** Hash mark (POUND sign). */
    public static final String POUND  =  "#";
    /** Ampersand (AND). */
    public static final String AND  =  "&";
    /** Left paren (LPAREN). */
    public static final String LPAREN  =  "(";
    /** Right paren (RPAREN). */
    public static final String RPAREN  =  ")";
    /** Double quotation mark. */
    public static final String DOUBLE_QUOTE  =  "\"";
    /** Single quotation mark. */
    public static final String QUOTE  =  "\'";
    /** Horizontal tab. */
    public static final String HT  =  "\t";
    /** Percentage mark. */
    public static final String PERCENT  =  "%";
}
