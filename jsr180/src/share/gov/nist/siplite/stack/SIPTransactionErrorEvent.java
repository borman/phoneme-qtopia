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
package gov.nist.siplite.stack;


/**
 * An event that indicates that a transaction has encountered an error.
 *
 */
public class SIPTransactionErrorEvent {
    /** The originating transaction. */
    protected Transaction sourceTransaction;
    
    /**
     * This event ID indicates that the transaction has timed out.
     */
    public static final int TIMEOUT_ERROR = 1;
    
    /**
     * This event ID indicates that there was an error sending
     * a message using the underlying transport.
     */
    public static final int TRANSPORT_ERROR = 2;
    
    /** ID of this error event. */
    private int errorID;
    
    /**
     * Creates a transaction error event.
     * @param sourceTransaction Transaction which is raising the error.
     * @param transactionErrorID ID of the error that has ocurred.
     */
    SIPTransactionErrorEvent(Transaction sourceTransaction,
            int transactionErrorID) {
        
        this.sourceTransaction = sourceTransaction;
        errorID = transactionErrorID;
    }
    
    /**
     * Gets the error source.
     * @return the source of the error.
     */
    public Transaction getSource() {
        return this.sourceTransaction;
    }
    
    /**
     * Returns the ID of the error.
     *
     * @return Error ID.
     */
    public int getErrorID() {
        return errorID;
    }
}
