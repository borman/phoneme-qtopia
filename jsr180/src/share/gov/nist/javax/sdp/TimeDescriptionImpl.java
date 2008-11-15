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
package gov.nist.javax.sdp;
import gov.nist.javax.sdp.fields.*;
import java.util.*;
/**
 * Implementation of Time Description
 *
 * @version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TimeDescriptionImpl {
    /** Time field. */
    private TimeField timeImpl; 
    /** Vector of repeat times. */
    private Vector repeatList;
 
    /** Creates new TimeDescriptionImpl */
    public TimeDescriptionImpl() {
	timeImpl = new TimeField();
	repeatList = new Vector();
 
    }

    /**
     * Constructs a time description with an initial time field.
     * @param timeField to set
     */ 
    public TimeDescriptionImpl(TimeField timeField) {
	this.timeImpl = timeField;
	repeatList = new Vector();
    }
 
    /**
     * Returns the Time field.
     * @return Time
     */ 
    public TimeField getTime() {
	return timeImpl;
    }

    /**
     * Sets the Time field.
     * @param timeField Time to set
     * @throws SdpException if the time is null
     */ 
    public void setTime(TimeField timeField)
	throws SdpException {
	if (timeField == null) {
	    throw new SdpException("The parameter is null");
	} else {
	    if (timeField instanceof TimeField) {
		this.timeImpl = (TimeField)timeField;
	    } else
		throw new SdpException
		    ("The parameter is not an instance of TimeField");
	}
    }
 
    /**
     * Returns the list of repeat times (r= fields) 
     * specified in the SessionDescription.
     * @param create boolean to set
     * @return Vector
     */ 
    public Vector getRepeatTimes(boolean create) {
	return this.repeatList;
    }
 
    /**
     * Returns the list of repeat times (r= fields) 
     * specified in the SessionDescription.
     * @param repeatTimes Vector to set
     * @throws SdpException if the parameter is null
     */ 
    public void setRepeatTimes(Vector repeatTimes)
	throws SdpException {
	this.repeatList = repeatTimes;
    }

    /**
     * Adds a repeat field.
     * @param repeatField repeat field to add.
     */
    public void addRepeatField(RepeatField repeatField) {
	if (repeatField == null) 
	    throw new NullPointerException("null repeatField");
	this.repeatList.addElement(repeatField);
    }

    /**
     * Encodes contents as a string.
     * @return encoded string of object contents
     */
    public String toString() {
	String retval = timeImpl.encode();
	for (int i = 0; i < this.repeatList.size(); i++) {
	    RepeatField repeatField = 
		(RepeatField) this.repeatList.elementAt(i);
	    retval += repeatField.encode();
	}
	return retval;
    }
 
 
 
}
