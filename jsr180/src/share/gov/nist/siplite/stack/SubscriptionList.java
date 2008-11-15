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
package gov.nist.siplite.stack;

import java.util.*;
import gov.nist.core.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;

/**
 * Class representing a list of subscription.
 */
public class SubscriptionList {
    /** Vector of the active subscriptions */
    private Vector subscriptionVector;

    /**
     * Default constructor.
     */
    public SubscriptionList() {
        subscriptionVector = new Vector();
    }

    /**
     * Checks if the subscription list is empty.
     * @return true if the subscription list is empty, false otherwise
     */
    public boolean isEmpty() {
        return subscriptionVector.isEmpty();
    }

    /**
     * Removes the given subscription from the list.
     * @param s a subscription to remove
     */
    public void removeSubscription(Subscription s) {
        if (s != null) {
            // System.out.println("*** Removing SUBSCRIPTION");
            subscriptionVector.removeElement(s);
        }
    }

    /**
     * Adds a new subscription to the list.
     * @param s a subscription to add
     */
    public void addSubscription(Subscription s) {
        if (s != null) {
            // System.out.println("*** Adding a new SUBSCRIPTION");
            subscriptionVector.addElement(s);
        }
    }

    /**
     * Finds a subscription matching the given response or NOTIFY.
     * @param message response or NOTIFY message
     * @return a subscription matching to the given response or NOTIFY
     * or null if the subscription was not found
     */
    public Subscription getMatchingSubscription(Message message) {
        Enumeration en = subscriptionVector.elements();

        while (en.hasMoreElements()) {
            Subscription s = (Subscription)en.nextElement();
            if (s.containsSubscription(message)) {
                // System.out.println("*** Matching SUBSCRIPTION found!");
                return s;
            }
        }

        // System.out.println("*** Matching SUBSCRIPTION NOT found!");
        return null;
    }
}
