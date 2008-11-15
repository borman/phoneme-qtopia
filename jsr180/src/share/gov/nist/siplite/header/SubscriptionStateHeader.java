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
package gov.nist.siplite.header;

import gov.nist.core.*;
import gov.nist.siplite.header.*;

/**
 * <pre>Subscription-State Header class.
 *
 * RFC 3265, p. 36:
 * Subscription-State     = "Subscription-State" HCOLON substate-value
 *                            *( SEMI subexp-params )
 * substate-value         = "active" / "pending" / "terminated"
 *                            / extension-substate
 * extension-substate     = token
 * subexp-params          = ("reason" EQUAL event-reason-value)
 *                            / ("expires" EQUAL delta-seconds)
 *                            / ("retry-after" EQUAL delta-seconds)
 *                            / generic-param
 * event-reason-value     = "deactivated"
 *                            / "probation"
 *                            / "rejected"
 *                            / "timeout"
 *                            / "giveup"
 *                            / "noresource"
 *                            / event-reason-extension
 * event-reason-extension = token</pre>
 */
public class SubscriptionStateHeader extends ParametersHeader {
    /** Class handle. */
    public static Class clazz;

    /** SubscriptionState header field label. */
    public static final String NAME = Header.SUBSCRIPTION_STATE;

    /** 'active' state label. */
    public static final String STATE_ACTIVE  = "active";

    /** 'pending' state label. */
    public static final String STATE_PENDING  = "pending";

    /** 'terminated' state label. */
    public static final String STATE_TERMINATED  = "terminated";

    /** 'reason' parameter label. */
    public static final String PARAM_REASON = "reason";

    /** 'expires' parameter label. */
    public static final String PARAM_EXPIRES = "expires";

    /** 'retry-after' parameter label. */
    public static final String PARAM_RETRY_AFTER = "retry-after";

    /** Current subscription state. */
    private String state = null;

    /**
     * Static initializer.
     */
    static {
        clazz = new SubscriptionStateHeader().getClass();
    }

    /**
     * Default constructor.
     */
    public SubscriptionStateHeader() {
        super(NAME);
    }

    /**
     * Constructor given a state.
     * @param subscriptionState state of the subscription
     * @throws IllegalArgumentException if the subscriptionState is invalid.
     */
    public SubscriptionStateHeader(String subscriptionState)
            throws IllegalArgumentException {
        super(NAME);
        setState(subscriptionState);
    }

    /**
     * Encode this into a cannonical String.
     * @return String
     */
    public String encodeBody() {
        if (state == null) {
            return "";
        }

        return state + encodeWithSep();
    }

    /**
     * Checks if the current subscription state is "active".
     * @return true if the subscription is active, false otherwise.
     */
    public boolean isActive() {
        if (state == null) {
            return false;
        }
        return state.equalsIgnoreCase(STATE_ACTIVE);
    }

    /**
     * Checks if the current subscription state is "terminated".
     * @return true if the subscription is terminated, false otherwise.
     */
    public boolean isTerminated() {
        if (state == null) {
            return false;
        }
        return state.equalsIgnoreCase(STATE_TERMINATED);
    }

    /**
     * Returns the current subscription state.
     * @return subscription state.
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the subscription state.
     * @param newState subscription state to set.
     * @throws IllegalArgumentException if the newState value is invalid.
     */
    public void setState(String newState)
            throws IllegalArgumentException {
        if (newState == null || newState.equals("")) {
            throw new IllegalArgumentException("Invalid state value: " +
                newState);
        }
        state = newState.trim();
    }

    /**
     * Returns the value of the current object, i.e., the subscription state.
     * @return subscription state.
     */
    public Object getValue() {
        return state;
    }

    /**
     * Returns the subscription expiration time in seconds.
     * @return subscription expiration time.
     */
    public String getExpires() {
        return getParameter(PARAM_EXPIRES);
    }

    /**
     * Sets the subscription expiration time in seconds.
     * @param expires expiration time to set.
     * @throws IllegalArgumentException if the expires parameter is invalid.
     */
    public void setExpires(String expires)
            throws IllegalArgumentException {
        int intExpires;

        try {
          intExpires = Integer.parseInt(expires);
        } catch (NumberFormatException e) {
            intExpires = -1;
        }

        if (intExpires < 0) {
            throw new IllegalArgumentException("Invalid 'expires': " + expires);
        }

        setParameter(PARAM_EXPIRES, expires);
    }

    /**
     * Sets the subscription expiration time in seconds.
     * @param expires expiration time to set.
     * @throws IllegalArgumentException if the expires parameter is invalid.
     */
    public void setExpires(int expires)
            throws IllegalArgumentException {
        if (expires < 0) {
            throw new IllegalArgumentException("Invalid 'expires': " + expires);
        }
        setParameter(PARAM_EXPIRES, new Integer(expires).toString());
    }
}
