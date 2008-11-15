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
 * File Name     : TimeoutEvent.java
 *
 *  HISTORY
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package gov.nist.siplite;

import gov.nist.siplite.stack.*;

/**
 * This class represents an Timeout event that is passed from a SipProvider to
 * its SipListener. A specific message may need retransmitted on a specific
 * transaction numerous times before it is acknowledged by the receiver. If the
 * message is not acknowledged after a specified period in the underlying
 * implementation the transaction will expire, this occurs usually
 * after seven retransmissions. The mechanism to alert an application that a
 * message for a an underlying transaction needs retransmitted (i.e. 200OK) or
 * an underlying transaction has expired is a Timeout Event.
 * <p>
 * A Timeout Event can be of two different types, namely:
 * <ul>
 * <li>{@link Timeout#RETRANSMIT}
 * <li>{@link Timeout#TRANSACTION}
 * </ul>
 * A TimeoutEvent contains the following information:
 * <ul>
 * <li>source - the SipProvider that sent the TimeoutEvent.
 * <li>transaction - the transaction that this Timeout applies to.
 * <li>isServerTransaction - boolean indicating whether the
 * transaction refers to
 * a client or server transaction.
 * <li>timeout - indicates what type of {@link Timeout} occurred.
 * </ul>
 *
 * @see Timeout
 *
 * @since v1.1
 */
public class TimeoutEvent extends SipEvent {
    
    
    /**
     * Constructs a TimeoutEvent to indicate a server retransmission
     * or transaction timeout.
     *
     * @param source - the source of TimeoutEvent.
     * @param serverTransaction - the server transaction that timed out.
     */
    public TimeoutEvent(Object source,
            ServerTransaction serverTransaction) {
        super(source);
        m_serverTransaction = serverTransaction;
        m_isServerTransaction = true;
    }
    
    
    /**
     * Constructs a TimeoutEvent to indicate a
     * client retransmission or transaction
     * timeout.
     *
     * @param source - source of TimeoutEvent.
     * @param clientTransaction - the client transaction that timed out.
     */
    public TimeoutEvent(Object source,
            ClientTransaction clientTransaction) {
        super(source);
        m_clientTransaction = clientTransaction;
        m_isServerTransaction = false;
    }
    /**
     * Constructs a TimeoutEvent to indicate a client
     * retransmission or transaction timeout.
     *
     * @param source - source of TimeoutEvent.
     * @param clientTransaction - the client transaction that timed out.
     * @param timeout - indicates if this is a retranmission or transaction
     * timeout event.
     */
    public TimeoutEvent(Object source,
            ClientTransaction clientTransaction, Timeout timeout) {
        super(source);
        m_clientTransaction = clientTransaction;
        m_isServerTransaction = false;
        m_timeout = timeout;
    }
    
    /**
     * Gets the server transaction associated with this TimeoutEvent.
     *
     * @return server transaction associated with this TimeoutEvent,
     * or null if this event is specific to a client transaction.
     */
    public ServerTransaction getServerTransaction() {
        return m_serverTransaction;
    }
    
    
    /**
     * Gets the client transaction associated with this TimeoutEvent.
     *
     * @return client transaction associated with this TimeoutEvent, or null if
     * this event is specific to a server transaction.
     */
    public ClientTransaction getClientTransaction() {
        return m_clientTransaction;
    }
    
    /**
     * Indicates if the transaction associated with this TimeoutEvent
     * is a server transaction.
     *
     * @return returns true if a server transaction or false if a client
     * transaction.
     */
    public boolean isServerTransaction() {
        return m_isServerTransaction;
    }
    
    /** Flag indicating server transaction. */
    private boolean m_isServerTransaction;
    /** Current server transaction. */
    private ServerTransaction m_serverTransaction = null;
    /** Current client transaction. */
    private ClientTransaction m_clientTransaction = null;
    /** Current transaction timeout. */
    private Timeout m_timeout;
    
}

