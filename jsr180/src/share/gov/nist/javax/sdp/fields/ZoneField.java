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
 * Zone SDP field.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public class ZoneField extends SDPField {
    /** Zone adjustments. */
    protected SDPObjectList zoneAdjustments;


    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	ZoneField retval = new ZoneField();
	retval.zoneAdjustments = 
	    (SDPObjectList) this.zoneAdjustments.clone();
	return retval;
    }

    /**
     * Default onstructor.
     */
    public ZoneField() {
	super(ZONE_FIELD);
	zoneAdjustments = new SDPObjectList();
    }

    /**
     * Adds an element to the zone adjustment list.
     * @param za zone adjustment to add.
     */
    public void addZoneAdjustment(ZoneAdjustment za) {
	zoneAdjustments.addElement(za);
    }


    /**
     * Gets the zone adjustment list.
     * @return the list of zone adjustments.
     */
    public SDPObjectList getZoneAdjustments() {
	return zoneAdjustments;
    }


    /**
     * Encodes this structure into a canonical form.
     * @return encoded string of object contents
     */
    public String encode() {
	StringBuffer retval = new StringBuffer(ZONE_FIELD);
	for (int i = 0; i < zoneAdjustments.size(); i++) {
	    ZoneAdjustment za = (ZoneAdjustment)
		zoneAdjustments.elementAt(i);
	    if (i > 0) retval.append(Separators.SP);
	    retval.append(za.encode());
	}
	retval.append(Separators.NEWLINE);
	return retval.toString();
    }

    /**
     * Returns a Hashtable of adjustment times, where:
     * key = Date. This is the equivalent of the decimal NTP time value.
     * value = Int Adjustment. This is a relative time value in seconds.
     * @param create to set
     * @throws SdpParseException if a parsing error occurs
     * @return create when true, an empty Hashtable is created, if it is null.
     */ 
    public Hashtable getZoneAdjustments(boolean create)
	throws SdpParseException {
	Hashtable result = new Hashtable();
	SDPObjectList zoneAdjustments = getZoneAdjustments();
	ZoneAdjustment zone;
	if (zoneAdjustments == null)
	    if (create)
		return new Hashtable();
	    else return null;
	else {
	    for (int i = 0; i < zoneAdjustments.size(); i++) {
		zone = (ZoneAdjustment) zoneAdjustments.elementAt(i);
		Long l = new Long(zone.getTime());
		Integer time = new Integer((int) l.longValue());
		Date date = new Date(zone.getTime());
		result.put(date, time);
	    }
	    return result;
	}
    }
 
    /**
     * Sets the Hashtable of adjustment times, where:
     * key = Date. This is the equivalent of the decimal NTP time value.
     * value = Int Adjustment. This is a relative time value in seconds.
     * @param map Hashtable to set
     * @throws SdpException if the parameter is null
     */ 
    public void setZoneAdjustments(Hashtable map)
	throws SdpException {
	if (map == null) 
	    throw new SdpException("The map is null");
	else {
	    SDPObjectList zoneAdjustments = getZoneAdjustments();
	    for (Enumeration e = map.keys(); e.hasMoreElements(); ) {
		Object o = e.nextElement();
		if (o instanceof Date) {
		    Date date = (Date)o;
		    ZoneAdjustment zone = new ZoneAdjustment();
		    zone.setTime(date.getTime());
		    addZoneAdjustment(zone);
		} else
		    throw new SdpException("The map is not well-formated ");
	    }
	}
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
     * @param typedTime typedTime - if set true, the start
     * and stop times will be
     * output in an optimal typed time format; if false, the
     * times will be output as integers.
     */ 
    public void setTypedTime(boolean typedTime) {
	// Dummy -- feature not implemented.
    }
 
    /**
     * Returns whether the field will be output as a typed time or a
     * integer value.
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
     * @return true, if the field will be output as a typed time;
     * false, if as an integer value.
     */ 
    public boolean getTypedTime() {
	return false;
    }
}

