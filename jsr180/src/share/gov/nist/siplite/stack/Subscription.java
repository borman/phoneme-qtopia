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

import gov.nist.core.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;

/**
 * Subscription object.
 */
public class Subscription {
    /** Reference to the dialog which this subscription belongs to. */
    private Dialog dialog = null;

    /** Event header defining this subscription */
    private EventHeader eventHeader = null;

    /**
     * Constructor given a dialog and a request creating the subscription.
     * @param subscriptionDialog
     * @param request
     */
    public Subscription(Dialog subscriptionDialog, Request request) {
        // System.out.println("*** Creating a new SUBSCRIPTION, dialog = " +
        //     subscriptionDialog);

        dialog = subscriptionDialog;

        if (request.getMethod().equalsIgnoreCase(Request.REFER)) {
            // RFC 3515, p. 2:
            // A REFER request implicitly establishes a subscription
            // to the refer event.
            eventHeader = new EventHeader();
            try {
                eventHeader.setEventType(Request.REFER);
            } catch (ParseException pe) {
                // Request.REFER is a valid event type,
                // so we can't get here.
            }
        } else {
            EventHeader eh = (EventHeader)request.getHeader(Header.EVENT);
            if (eh != null) {
                eventHeader = (EventHeader)eh.clone();
            }
        }
    }

    /**
     * Return true if the given response message or NOTIFY request matches
     * this subscription.
     * @param message a response or NOTIFY request to check for matching
     * @return true if the given NOTIFY matches this subscription,
     * false otherwise
     */
    public boolean containsSubscription(Message message) {
        CallIdHeader hCallId = (CallIdHeader)message.getHeader(Header.CALL_ID);
        if (hCallId == null) {
            return false;
        }

        // Get callId and fromTag from the dialog.
        CallIdHeader callId = dialog.getCallId();
        if (callId == null) {
            return false;
        }

        String fromTag = dialog.isServer() ?
	    dialog.getRemoteTag() : dialog.getLocalTag();
        if (fromTag == null) {
            return false;
        }

        if (message instanceof Response) {
            /*
             * RFC3265, section 3.3.4
             * Responses are matched to such SUBSCRIBE requests if they
             * contain the same the same "Call-ID", the same "From" header
             * "tag", and the same "CSeq".
             *
             * IMPL_NOTE: compare CSeq
             */
            String fTag = message.getFromHeaderTag();
            if (fTag == null) {
                return false;
            }

            return (hCallId.equals(callId) && fTag.equalsIgnoreCase(fromTag));
        }

        /*
         * RFC 3265, section 3.3.4:
         * If an initial SUBSCRIBE request is not sent on a pre-existing dialog,
         * the subscriber will wait for a response to the SUBSCRIBE request or a
         * matching NOTIFY.
         *
         * NOTIFY requests are matched to such SUBSCRIBE requests if they
         * contain the same "Call-ID", a "To" header "tag" parameter which
         * matches the "From" header "tag" parameter of the SUBSCRIBE, and the
         * same "Event" header field.
         */
        EventHeader hEvent = (EventHeader)message.getHeader(Header.EVENT);
        if (hEvent == null) {
            String reqMethod = ((Request)message).getMethod();
            if (reqMethod.equalsIgnoreCase(Request.REFER)) {
                hEvent = eventHeader;
            } else {
                return false;
            }
        }

        String toTag = message.getToTag();
        if (toTag == null) {
            return false;
        }

        return (hEvent.match(eventHeader) && toTag.equalsIgnoreCase(fromTag) &&
            hCallId.equals(callId));
    }
}
