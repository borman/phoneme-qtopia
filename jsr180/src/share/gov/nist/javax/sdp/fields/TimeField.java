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
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import java.util.*;
import gov.nist.javax.sdp.*;
/**
 * Time Field.
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TimeField extends SDPField {
    /** Start time. */
    protected long startTime;
    /** Stop time. */
    protected long stopTime;

    /** Default constructor. */
    public TimeField() {
	super(TIME_FIELD);
    }

    /** 
     * Gets the start time.
     * @return start time
     */
    public long getStartTime() { 
	return startTime; 
    } 

    /** 
     * Gets the stop time.
     * @return start stop time
     */
    public long getStopTime() { 
	return stopTime; 
    } 

    /**
     * Sets the start time member.
     * @param startTime tthe new start time
     */
    public void setStartTime(long startTime) { 
	this.startTime = startTime; 
    } 

    /**
     * Sets the stop time member.
     * @param stopTime the new stop mtime
     */
    public void setStopTime(long stopTime) { 
	this.stopTime = stopTime; 
    } 

    /**
     * Returns the start time of the conference/session.
     * @throws SdpParseException if a parsing error occurs
     * @return the date
     */ 
    public Date getStart()
	throws SdpParseException {
	return new Date(startTime*1000 + SdpConstants.NTP_CONST);
    }

    /**
     * Returns the stop time of the session.
     * @throws SdpParseException if a parsing error occurs
     * @return the stop time of the session.
     */ 
    public Date getStop()
	throws SdpParseException {
	return new Date(stopTime*1000 + SdpConstants.NTP_CONST);
    }
 
    /**
     * Sets the stop time of the session.
     * @param stop the new stop time
     * @throws SdpException if the date is null
     */ 
    public void setStop(Date stop)
	throws SdpException {
	if (stop == null)
	    throw new SdpException("The date is null");
	else {
	    this.stopTime = stop.getTime() / 1000 - SdpConstants.NTP_CONST;
	}
    }
 
    /**
     * Sets the start time of the conference/session.
     * @param start the new start time for the session.
     * @throws SdpException if the date is null
     */ 
    public void setStart(Date start)
	throws SdpException {
	if (start == null)
	    throw new SdpException("The date is null");
	else {
	    this.startTime = start.getTime()/1000 - SdpConstants.NTP_CONST;
	}
    }

    /**
     * Returns whether the field will be output as a typed time 
     * or a integer value.
     *
     * Typed time is formatted as an integer followed by a unit character.
     * The unit indicates an appropriate multiplier for
     * the integer.
     *
     * The following unit types are allowed.
     * <pre>
     * d - days (86400 seconds)
     * h - hours (3600 seconds)
     * m - minutes (60 seconds)
     * s - seconds ( 1 seconds)
     * </pre>
     * @return true, if the field will be output as a 
     * typed time; false, if as an integer value.
     */ 
    public boolean getTypedTime() {
	return false;
    }
 
    /** 
     * Sets whether the field will be output as a typed time or a integer value.
     *
     * Typed time is formatted as an integer followed by a unit character. 
     * The unit indicates an appropriate multiplier for
     * the integer.
     *
     * The following unit types are allowed.
     * <pre>
     * d - days (86400 seconds)
     * h - hours (3600 seconds)
     * m - minutes (60 seconds)
     * s - seconds ( 1 seconds)
     * </pre>
     * @param typedTime if set true, the start and stop times will
     * be output in an optimal typed time format; if false, the
     * times will be output as integers.
     */ 
    public void setTypedTime(boolean typedTime) {
 
    }

    /**
     * Returns whether the start and stop times were set to zero (in NTP).
     * @return true if tsrat or stop time are zero
     */ 
    public boolean isZero() {
	long stopTime = getStopTime();
	long startTime = getStartTime();
	if (stopTime == 0 && startTime == 0)
	    return true;
	else return false;
    }
 
    /**
     * Sets the start and stop times to zero (in NTP).
     */ 
    public void setZero() {
	setStopTime(0);
	setStartTime(0);
    }



    /**
     * Gets the string encoded version of this object.
     * @return encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	return new StringBuffer()
	    .append(TIME_FIELD)
	    .append(startTime) 
	    .append(Separators.SP)
	    .append(stopTime)
	    .append(Separators.NEWLINE)
	    .toString();
    }

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	TimeField retval = new TimeField();
	retval.startTime = this.startTime;
	retval.stopTime = this.stopTime;
	return retval;
    }
 
}
