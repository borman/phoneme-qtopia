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

package gov.nist.siplite.header;

import gov.nist.core.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Date sip header.
 *
 * IMPL_NOTE: implement setHeaderValue()!
 */

public class DateHeader extends ParameterLessHeader {
    /** Value for current date header field. */
    private ExtendedCalendar date;

    /** Label for date header field. */
    public static final String NAME = Header.DATE;

    /** Class handle. */
    protected static Class clazz;

    static {
        clazz = new DateHeader().getClass();
    }

    /** Default constructor. */
    public DateHeader() {
        super(Header.DATE);
        date = new ExtendedCalendar();
    }

    /**
     * Sets the expiry date.
     * @param dateToSet is the date to set.
     */
    public void setDate(Date dateToSet) {
        this.date.setTime(dateToSet);
    }

    /**
     * Sets the date.
     * @param calendarToSet the new value for date field
     */
    public void setDate(Calendar calendarToSet) {
        this.date.setCalendar(calendarToSet);
    }


    /**
     * Gets the expiry date.
     * @return get the expiry date.
     */
    public Date getDate() {
        return this.date.getTime();
    }

    /**
     * Gets the calendar date.
     * @return the current date value
     */
    public Object getValue() {
        return this.date;
    }


    /**
     * Encodes into canonical form.
     * @return encoded string of object contents
     */
    public String encodeBody() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(encodeCalendar(date.getCalendar()));
        return sbuf.toString();
    }

    /**
     * Copies the current instance.
     * @return copy of the current object
     */
    public Object clone() {
        DateHeader retval = new DateHeader();
        retval.setDate(this.getDate());
        return retval;
    }

    /**
     * Encodes the object. Calls encode().
     * @return String string representation of the date.
     */
    public String toString() {
        return encode();
    }


    /**
     * ExtendedCalendar class is a wrapper for Calendar class.
     * It's required because java.util.Calendar in CLDC doesn't implement
     * toString(), so DateHeader.getValue().toString() will return
     * something like com.sun.cldc.util.j2me$Calendar@... instead of
     * the string date representation expected by the caller.
     */
    private class ExtendedCalendar extends Object {

        /** Internal Calendar object which is represented by this class. */
        private Calendar date;

        /** Default constructor. */
        public ExtendedCalendar() {
            date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        }

        /**
         * Sets the internal Calendar object.
         * @param calendarToSet the new value for date field
         */
        public void setCalendar(Calendar calendarToSet) {
            date = calendarToSet;
        }

        /**
         * Return the internal Calendar object.
         * @return the internal Calendar object
         */
        public Calendar getCalendar() {
            return date;
        }

        /**
         * Sets this Calendar's current time with the given Date.
         * @param timeToSet the given Date
         */
        public void setTime(Date timeToSet) {
            date.setTime(timeToSet);
        }

        /**
         * Gets this Calendar's current time.
         * @return the current time
         */
        public Date getTime() {
            return date.getTime();
        }

        /**
         * Encodes the object.
         * @return string representation of the date.
         */
        public String toString() {
            return Header.encodeCalendar(date);
        }

    } // ExtendedCalendar end

}
