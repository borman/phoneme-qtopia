/*
 *   
 *
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
 */

package com.sun.j2me.payment;

/**
 * This interface defines methods which should be implemented by classes which
 * want to act as a transaction processor for transactions.
 *
 * @version 1.1
 */
public interface TransactionProcessor {
    /**
     * Processes the given transaction, updates its state and returns the same
     * transaction instance or a new one (an instance of 
     * a <code>Transaction</code> subclass), which is based on the old 
     * transaction, but adds more (adapter specific) information to it.
     *
     * @param transaction the transaction to be processed
     * @return the transaction after processing
     */
    Transaction process(Transaction transaction);
}
