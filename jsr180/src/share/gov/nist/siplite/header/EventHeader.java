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
package gov.nist.siplite.header;
import gov.nist.core.*;

/**
 * Event SIP Header.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class EventHeader extends ParametersHeader {
    /** Current event header field value. */
    protected String eventType;
    /** Idientifier label. */
    public final static String ID = "id";
    /** Event header field label. */
    public final static String NAME = Header.EVENT;
    /** Class handle. */
    public static Class clazz;


    static {
        clazz = new EventHeader().getClass();
    }

    /** Creates a new instance of Event */
    public EventHeader() {
        super(EVENT);
    }

    /**
     * Sets the eventType to the newly supplied eventType string.
     *
     * @param eventType - the new string defining the eventType supported
     * in this EventHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventType value.
     */
    public void setEventType(String eventType) throws ParseException {
        if (eventType == null)
            throw new NullPointerException(" the eventType is null");
        this.eventType = eventType;
    }

    /**
     * Gets the eventType of the EventHeader.
     *
     * @return the string object identifing the eventType of EventHeader.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the id to the newly supplied <var>eventId</var> string.
     *
     * @param eventId - the new string defining the eventId of this EventHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventId value.
     */
    public void setEventId(String eventId) throws ParseException {
        if (eventId == null)
            throw new NullPointerException("the eventId parameter is null");
        setParameter(ID, eventId);
    }

    /**
     * Gets the id of the EventHeader. This method may return null if the
     * "eventId" is not set.
     * @return the string object identifing the eventId of EventHeader.
     */
    public String getEventId() {
        return getParameter(ID);
    }

    /**
     * Encode in canonical form.
     * @return String
     */
    public String encodeBody() {
        StringBuffer retval = new StringBuffer();

        if (eventType != null) {
            retval.append(eventType);
            // retval.append(Separators.SP + eventType + Separators.SP);
        }

        retval.append(encodeWithSep());

        return retval.toString();
    }

    /**
     * Return true if the given event header matches the supplied one.
     *
     * @param matchTarget -- event header to match against.
     * @return true if object matches
     */
    public boolean match(EventHeader matchTarget) {
        if (matchTarget.eventType == null && this.eventType != null)
            return false;
        else if (matchTarget.eventType != null && this.eventType == null)
            return false;
        else if (this.eventType == null && matchTarget.eventType == null)
            return false;
        else if (getEventId() == null && matchTarget.getEventId() != null)
            return false;
        else if (getEventId() != null && matchTarget.getEventId() == null)
            return false;
        return equalsIgnoreCase(matchTarget.eventType, this.eventType) &&
                ((this.getEventId() == matchTarget.getEventId()) ||
                equalsIgnoreCase(this.getEventId(), matchTarget.getEventId()));
    }

    /**
     * Gets the event header value.
     * @return the event header value
     */
    public Object getValue() {
        return this.eventType;
    }


}
