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

package gov.nist.javax.sdp.parser;
import  gov.nist.core.*;

/**
 * Basica lexigographical processor.
 */
public class Lexer extends LexerCore {
    /**
     * Constructs a Lexer byte name to process the
     * requested buffer of text.
     * @param lexerName the requested processor
     * @param buffer the data to be processed
     */
    public Lexer(String lexerName, String buffer) {
	super(lexerName, buffer);

    }

    /**
     * Selects a new lexer byte name.
     * @param lexerName the new processor to use
     */
    public void selectLexer(String lexerName) {}

    /**
     * Gets the firld name.
     * @param line the text to parse
     * @return the name of the field to process
     */
    public static String getFieldName(String line) {
	int i = line.indexOf("=");
	if (i == -1)
	    return null;
	else
	    return line.substring(0, i);
    }


}

