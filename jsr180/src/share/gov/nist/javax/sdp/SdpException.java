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
 *  The SdpException defines a general exception for the SDP classes
 *  to throw when they encounter a difficulty.
 *
 * @version 1.0
 */
public class SdpException extends Exception {

   
    /**
     *  Creates new SdpException
     */
    public SdpException() {
	super();
    }

    /**
     *  Constructs a new SdpException with the message you specify.
     * @param message a String specifying the text of the exception message
     */    
    public SdpException(String message) {
	super(message);
    }
    
    /**
     *  Constructs a new SdpException when the Codelet needs to throw an 
     * exception and include a message about another exception that interfered
     * with its normal operation.
     * @param message a String specifying the text of the exception message
     * @param rootCause the Throwable exception that interfered with the 
     * Codelet's normal operation, making this Codelet exception necessary
     */    
    public SdpException(String message,
			Throwable rootCause) {
        super(rootCause.getMessage()+";"+message);
    }
    
    /**
     * Constructs a new SdpException as a result of a system exception and uses
     * the localized system exception message.
     * @param rootCause the system exception that makes this 
     * SdpException necessary
     */    
    public SdpException(Throwable rootCause) {
        super(rootCause.getMessage());
    }
    
    
}
