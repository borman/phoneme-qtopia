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
package gov.nist.siplite.stack;

import gov.nist.siplite.message.*;
import gov.nist.core.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Exception that gets generated when the Stack encounters an error.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class SIPServerException extends Exception {
    /** Return code. */
    protected int rc;
    /** Message. */
    protected String message;
    /** The saved SIP server message.  */
    protected Message sipMessage;
    
    /**
     * Gets the reason code.
     * @return the reason code
     */
    public int getRC() {
        return this.rc;
    }
    
    /**
     * Constructor when we are given only the error code
     * @param rc Return code.
     */
    public SIPServerException(int rc) {
        this.rc = rc;
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            printStackTrace();
        }
    }
    
    /**
     * Constructor for when we have the error code and some error info.
     * @param rc SIP Return code
     * @param msg Error message
     */
    public SIPServerException(int rc, String msg) {
        this.rc = rc;
        this.message = msg;
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            printStackTrace();
        }
    }
    
    /**
     * Constructor for when we have a return code and a Message.
     * @param rc SIP error code
     * @param message SIP Error message
     * @param msg Auxiliary error message
     */
    public SIPServerException(int rc, Message message, String msg) {
        this.rc = rc;
        this.sipMessage = message;
        this.message = msg;
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            printStackTrace();
        }
    }
    
    /**
     * Constructor when we have a pre-formatted response.
     * @param response Pre-formatted response to send back to the
     * other end.
     */
    public SIPServerException(String response) {
        super(response);
        ServerLog.logException(this);
    }
    
    /**
     * Constructor that constructs the message from the standard
     * Error messages.
     *
     * @param rc is the SIP Error code.
     * @param sipMessage is the SIP Message that caused the exception.
     */
    public SIPServerException(int rc, Message sipMessage) {
        this.rc = rc;
        this.sipMessage = sipMessage;
    }
    
    /**
     * Gets the message that generated this exception.
     *
     * @return -- the message that generated this exception.
     */
    public Message getSIPMessage() {
        return this.sipMessage;
    }
    
    
}
