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
 * An interface for a genereic message processor for SIP Request messages.
 * This is implemented by the application. The stack calls the message
 * factory with a pointer to the parsed structure to create one of these
 * and then calls processRequest on the newly created SIPServerRequest
 * It is the applications responsibility to take care of what needs to be
 * done to actually process the request.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface SIPServerRequestInterface {
    /**
     * Gets the channel to where to send the response
     * (the outgoing message channel).
     * @return the response message channel
     */
    public MessageChannel getResponseChannel();
    
    /**
     * Processes the message. This incorporates a feature request
     * by Salvador Rey Calatayud &lt;salreyca@TELECO.UPV.ES&gt;
     * @param sipRequest is the incoming SIP Request.
     * @param incomingChannel is the incoming message channel (parameter
     * added in response to a request by Salvador Rey Calatayud.)
     * @throws SIPServerException Exception that gets thrown by
     * this processor when an exception is encountered in the
     * message processing.
     */
    public void processRequest(Request sipRequest,
            MessageChannel incomingChannel)
            throws SIPServerException;
    
    
    /**
     * Gets processing information.
     * The stack queries processing information to add to the message log.
     * by calling this interface. Return null if no processing information
     * of interes thas been generated.
     * @return the processing information
     */
    public String getProcessingInfo();
}
