/**
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 *
 *
 * Module Name   : JAIN SIP Specification
 * File Name     : ResponseEvent.java
 *
 *  HISTORY
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package gov.nist.siplite;
import gov.nist.siplite.message.Response;
import gov.nist.siplite.stack.*;

/**
 * This class represents a Response event that is passed from a SipProvider to
 * its SipListener. This specification handles the passing of Response messages
 * to the application with the event model. An application (SipListener)
 * registers with the SIP protocol stack (SipProvider) and listens for Response
 * events from the SipProvider.
 * <p>
 * This specification defines a single Response event object to handle all
 * Response messages. The Response event encapsulates the Response message
 * that can be retrieved from {@link ResponseEvent#getResponse()}.
 * Therefore the event type of a Response event can be determined as follows:
 * <p>
 * <i>eventType == ResponseEvent.getResponse().getStatusCode();</i>
 * <p>
 * A Response event also encapsulates the client transaction upon which the
 * Response is correlated, i.e. the client transaction of the Request
 * message upon which this is a Response.
 * <p>
 * ResponseEvent contains the following elements:
 * <ul>
 * <li>source - the source of the event i.e. the SipProvider sending the
 * ResponseEvent.
 * <li>clientTransaction - the client transaction this ResponseEvent is
 * associated with.
 * <li>Response - the Response message received on the SipProvider
 * that needs passed to the application encapsulated in a ResponseEvent.
 * </ul>
 *
 * @since v1.1
 */
public class ResponseEvent extends SipEvent {
    
    
    /**
     * Constructs a ResponseEvent encapsulating the Response that has
     * been received by the underlying SipProvider. This ResponseEvent
     * once created is passed to
     * {@link SipListener#processResponse(ResponseEvent)}
     *  method of the SipListener for application processing.
     *
     * @param source - the source of ResponseEvent i.e. the SipProvider
     * @param clientTransaction - client transaction upon which
     * this Response was sent
     * @param response - the Response message received by the SipProvider
     */
    public ResponseEvent(Object source,
            ClientTransaction clientTransaction,
            Response response) {
        super(source);
        m_response = response;
        m_transaction = clientTransaction;
    }
    
    /**
     * Gets the client transaction associated with this ResponseEvent
     *
     * @return client transaction associated with this ResponseEvent
     */
    public ClientTransaction getClientTransaction() {
        return m_transaction;
    }
    
    /**
     * Gets the Response message encapsulated in this ResponseEvent.
     *
     * @return the response associated with this ResponseEvent.
     */
    public Response getResponse() {
        return m_response;
    }
    
    /** Current response to process. */
    private Response m_response;
    /** Current transaction. */
    private ClientTransaction m_transaction;
}

