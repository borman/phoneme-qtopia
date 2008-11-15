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
import gov.nist.javax.sdp.*;
import java.util.Vector;
/**
 * Repeat SDP Field (part of the time field).
 *
 *@version  JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class RepeatField  extends SDPField {
    /** Repeat interval. */
    protected TypedTime repeatInterval;
    /** Duration. */
    protected TypedTime activeDuration;
    /** List of time offsets. */
    protected SDPObjectList offsets;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	RepeatField retval = new RepeatField();
	if (this.repeatInterval != null) 
	    retval.repeatInterval = 
		(TypedTime) this.repeatInterval.clone();
	if (this.activeDuration != null) 
	    retval.activeDuration = 
		(TypedTime)this.activeDuration.clone();
	retval.offsets = 
	    (SDPObjectList) this.offsets.clone();
	return retval;
    }
			
    /** Default constructor. */
    public RepeatField() { 
	super(REPEAT_FIELD); 
	offsets = new SDPObjectList();
    }

    /**
     * Sets the repeat interval.
     * @param interval the new repeat interval
     */
    public void setRepeatInterval(TypedTime interval) {
	repeatInterval = interval;
    }

    /** 
     * Sets the duration.
     * @param duration the active duration time period
     */
    public void setActiveDuration(TypedTime duration)  {
	activeDuration = duration; 
    }

    /** 
     * Adds an starting time offset.
     * @param offset the new time offset to process
     */
    public void addOffset(TypedTime offset) {
	offsets.addElement(offset);
    }

    /**
     * Gets a vector of starting time offsets.
     * @return vector of time offsets
     */
    public Vector getOffsets() 
    { return offsets; }

    /**
     * Returns the "repeat interval" in seconds.
     * @throws SdpParseException if a parsing error occurs
     * @return the "repeat interval" in seconds.
     */    
    public int getRepeatInterval()
	throws SdpParseException {
        if (repeatInterval == null)
	    return -1;
        else {
            return repeatInterval.getTime();
        }
    }
    
    /**
     * Sets the repeat interval in seconds.
     * @param repeatInterval the "repeat interval" in seconds.
     * @throws SdpException if repeatInterval is less than 0
     */    
    public void setRepeatInterval(int repeatInterval)
	throws SdpException {
        if (repeatInterval < 0) 
	    throw new SdpException("The repeat interval is < 0");
        else {
	    if (this.repeatInterval == null) 
		this.repeatInterval = new TypedTime();
	    this.repeatInterval.setTime(repeatInterval);
        }
    }
    
    /**
     * Returns the "active duration" in seconds.
     * @throws SdpParseException if a parinsg error occurs
     * @return the "active duration" in seconds.
     */    
    public int getActiveDuration()
	throws SdpParseException {
        if (activeDuration == null)
	    return -1;
        else {
            return activeDuration.getTime();
        }
    }
    
    /** 
     * Sets the "active duration" in seconds.
     * @param activeDuration the "active duration" in seconds.
     * @throws SdpException if the active duration is less than 0
     */    
    public void setActiveDuration(int activeDuration)
	throws SdpException {
        if (activeDuration < 0) 
	    throw new SdpException("The active Duration is < 0");
        else {
	    if (this.activeDuration == null) 
		this.activeDuration = new TypedTime();
	    this.activeDuration.setTime(activeDuration);
        }
    }
    
    /**
     * Returns the list of offsets. These are relative to the start-time given
     * in the Time object (t=field) with which this RepeatTime is associated.
     * @throws SdpParseException if a parsing error occurs
     * @return the list of offsets
     */    
    public int[] getOffsetArray()
	throws SdpParseException {
	int[] result = new int[offsets.size()];
	for (int i = 0; i < offsets.size(); i++) {
            TypedTime typedTime = (TypedTime)offsets.elementAt(i);
            result[i] = typedTime.getTime();
	}
	return result;
    }
    
    /**
     * Sets the list of offsets. These are relative to the start-time
     * given in the Time object (t=field) with which this RepeatTime
     * is associated.
     * @param offsets array of repeat time offsets
     * @throws SdpException if an error occurs setting the offsets
     */    
    public void setOffsetArray(int[] offsets)
	throws SdpException {
        for (int i = 0; i < offsets.length; i++) {
            TypedTime typedTime = new TypedTime();
            typedTime.setTime(offsets[i]);
            addOffset(typedTime);
        }
        
    }
    
    /**
     * Returns whether the field will be output as a typed time or a
     * integer value.
     *
     *     Typed time is formatted as an integer followed by a unit
     *     character. The unit indicates an
     *     appropriate multiplier for the integer.
     *<pre>
     *     The following unit types are allowed.
     *          d - days (86400 seconds)
     *          h - hours (3600 seconds)
     *          m - minutes (60 seconds)
     *          s - seconds ( 1 seconds)
     * </pre>
     * @throws SdpParseException if a parsing error occurs
     * @return true, if the field will be output as a typed time; false,
     * if as an integer value.
     */    
    public boolean getTypedTime()
	throws SdpParseException {
        return true;
    }
    
    /**
     * Sets whether the field will be output as a typed time or a integer value.
     *
     *     Typed time is formatted as an integer followed by a unit
     *     character. The unit indicates an
     *     appropriate multiplier for the integer.
     * <pre>
     *     The following unit types are allowed.
     *          d - days (86400 seconds)
     *          h - hours (3600 seconds)
     *          m - minutes (60 seconds)
     *          s - seconds ( 1 seconds)
     * </pre>
     * @param typedTime typedTime - if set true, the start and stop times
     *          will be output in an optimal typed
     *          time format; if false, the times will be output as integers.
     */    
    public void setTypedTime(boolean typedTime) {
        
    }

    /**
     * Gets an encoded string representation of the object.
     * @return the encoded string of object contents
     */
    public String encode() {
	String retval =  REPEAT_FIELD + repeatInterval.encode() 
	    + Separators.SP +
	    activeDuration.encode();
	for (int i = 0; i < offsets.size(); i++) { 
	    TypedTime off = (TypedTime) offsets.elementAt(i);
	    retval += Separators.SP + off.encode();
	}
	retval += Separators.NEWLINE;
	return retval;
    }


}
