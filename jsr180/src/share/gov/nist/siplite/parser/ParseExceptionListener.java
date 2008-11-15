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
package gov.nist.siplite.parser;
import gov.nist.siplite.message.*;
import gov.nist.core.*;
/**
 * A listener interface that enables customization of parse error handling.
 * An class that implements this interface is registered with the
 * parser and is called back from the parser handle parse errors.
 */

public interface ParseExceptionListener {
    /**
     * This gets called from the parser when a parse error is generated.
     * The handler is supposed to introspect on the error class and
     * header name to handle the error appropriately. The error can
     * be handled by :
     *<ul>
     * <li>1. Re-throwing an exception and aborting the parse.
     * <li>2. Ignoring the header (attach the unparseable header to
     * the Message being parsed).
     * <li>3. Re-Parsing the bad header and adding it to the sipMessage
     * </ul>
     *
     * @param ex parse exception being processed.
     * @param sipMessage sip message being processed.
     * @param headerClass the parser for the particular header
     * @param headerText  header/RL/SL text being parsed.
     * @param messageText message where this header was detected.
     */
    public void handleException(ParseException ex,
            Message sipMessage,
            Class headerClass,
            String headerText,
            String messageText)
            throws ParseException;
}
