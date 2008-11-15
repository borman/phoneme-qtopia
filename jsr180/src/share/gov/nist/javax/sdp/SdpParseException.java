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
 * SdpException.java
 *
 * Created on December 18, 2001, 11:08 AM
 */

package gov.nist.javax.sdp;

/**
 * The SdpParseException encapsulates the information thrown when an
 * error occurs during SDP parsing.
 * @version 1.0
 */
public class SdpParseException extends SdpException {
    /** Current line in parsed text. */
    private int lineNumber;
    /** Current character offset in the parse buffer. */
    private int charOffset;
 
    /**
     * Constructs a new SdpParseException when the parser needs to
     * throw an exception indicating a parsing failure.
     * @param lineNumber SDP line number that caused the exception.
     * @param charOffset offset of the character that caused the exception.
     * @param message a String containing the text of the exception message
     * @param rootCause the Throwable exception that interfered with the
     * Codelet's normal operation, making this Codelet exception necessary.
     */ 
    public SdpParseException(int lineNumber, int charOffset, String message,
			     Throwable rootCause) { 
	super(message, rootCause);
	this.lineNumber = lineNumber;
	this.charOffset = charOffset;
    }
 
    /**
     * Constructs a new SdpParseException when the parser needs to
     * throw an exception indicating a parsing failure.
     * @param lineNumber SDP line number that caused the exception.
     * @param charOffset offset of the characeter that caused the exception.
     * @param message a String containing the text of the exception message
     */
    public SdpParseException(int lineNumber, int charOffset, String message) {
	super(message);
	this.lineNumber = lineNumber;
	this.charOffset = charOffset;
    }
 
    /**
     * Returns the line number where the error occured.
     * @return the line number where the error occured
     */ 
    public int getLineNumber() {
	return lineNumber;
    }
 
    /**
     * Returns the char offset where the error occured.
     * @return the char offset where the error occured.
     */ 
    public int getCharOffset() {
	return charOffset;
    }
 
 
    /**
     * Returns the message stored when the exception was created.
     * @return the message stored when the exception was created.
     */ 
    public String getMessage() {
	return super.getMessage();
    }
 
}
