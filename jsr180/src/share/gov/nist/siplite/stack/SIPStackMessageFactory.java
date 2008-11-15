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

/**
 * An interface for generating new requests and responses. This is implemented
 * by the application and called by the stack for processing requests
 * and responses. When a Request comes in off the wire, the stack calls
 * newSIPServerRequest which is then responsible for processing the request.
 * When a response comes off the wire, the stack calls newSIPServerResponse
 * to process the response.
 */
public interface SIPStackMessageFactory {
    /**
     * Makes a new SIPServerResponse given a Request and a message
     * channel. This is invoked by the stack on an new incoming request.
     * @param sipRequest is the incoming SIP request.
     * @param msgChan is the message channel on which the incoming
     * sipRequest was received.
     * @return a new SIP server request object
     */
    public SIPServerRequestInterface
            newSIPServerRequest(Request sipRequest,
            MessageChannel msgChan);
    
    /**
     * Generates a new server response for the stack. This is invoked
     * by the stack on a new incoming server response.
     * @param sipResponse is the incoming response.
     * @param msgChan is the message channel on which the incoming response
     * is received.
     * @return a new SIP server request object
     */
    public SIPServerResponseInterface
            newSIPServerResponse
            (Response sipResponse,
            MessageChannel msgChan);
    
}
