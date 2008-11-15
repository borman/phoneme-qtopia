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
 * File Name     : RequestEvent.java
 *
 *  HISTORY
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package gov.nist.siplite;
import gov.nist.siplite.message.Request;
import gov.nist.siplite.stack.*;
import gov.nist.siplite.SipListener;

/**
 * This class represents an Request event that is passed from a SipProvider to
 * its SipListener. This specification handles the passing of request
 * messages to the
 * application use the event model. An application (SipListener) will register
 * with the SIP protocol stack (SipProvider) and listen for Request events
 * from the SipProvider.
 * <p>
 * This specification defines a single Request event object to handle
 * all Request
 * messages. The Request event encapsulates the Request message that can be
 * retrieved from {@link RequestEvent#getRequest()}. Therefore the event type
 * of a Request event can be determined as follows:
 * <p>
 * <i>eventType == RequestEvent.getRequest().getMethod();</i>
 * <p>
 * A Request event also encapsulates the server transaction which handles the
 * Request.
 * <p>
 * RequestEvent contains the following elements:
 * <ul>
 * <li>source - the source of the event i.e. the SipProvider sending the
 * RequestEvent
 * <li>serverTransaction - the server transaction this RequestEvent is
 * associated with.
 * <li>Request - the Request message received on the SipProvider
 * that needs passed to the application encapsulated in a RequestEvent.
 * </ul>
 *
 * @since v1.1
 */
public class RequestEvent extends SipEvent  {
    
    
    /**
     * Constructs a RequestEvent encapsulating the Request that has
     * been received by the underlying SipProvider. This RequestEvent
     * once created is passed to
     * {@link gov.nist.siplite.SipListener#processRequest(RequestEvent)}
     *  method of the SipListener for application processing.
     *
     * @param source - the source of ResponseEvent i.e. the SipProvider
     * @param serverTransaction - server transaction upon which
     * this Request was sent
     * @param request - the Request message received by the SipProvider
     */
    public RequestEvent(Object source,
            ServerTransaction serverTransaction, Request request) {
        super(source);
        m_transaction = serverTransaction;
        m_request = request;
    }
    
    /**
     * Gets the server transaction associated with this RequestEvent
     *
     * @return the server transaction associated with this RequestEvent
     */
    public ServerTransaction getServerTransaction() {
        return m_transaction;
    }
    
    /**
     * Gets the Request message associated with this RequestEvent.
     *
     * @return the message associated with this RequestEvent.
     */
    public Request getRequest() {
        return m_request;
    }
    
    /** Curent request. */
    private Request m_request;
    /** Current transaction. */
    private ServerTransaction m_transaction;
}
