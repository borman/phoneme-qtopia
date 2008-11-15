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

/**
 * Zone adjustment class.
 *
 * @version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ZoneAdjustment extends SDPObject {
    /** The time value. */
    protected long time;
    /** The sign of the time zone offset. */ 
    protected String sign;
    /** The value of the time zone offset. */
    protected TypedTime offset;

    /**
     * Sets the time.
     * @param t time to set.
     */
    public void setTime(long t) {
	time = t;
    }

    /**
     * Gets the time.
     * @return the time value
     */
    public long getTime() {
	return time;
    }

    /**
     * Gets the offset.
     * @return the time offset
     */
    public TypedTime getOffset() {
	return offset;
    }

    /**
     * Sets the offset.
     * @param off typed time offset to set.
     */
    public void setOffset(TypedTime off) {
	offset = off;
    }

    /**
     * Sets the sign.
     * @param s sign for the offset.
     */
    public void setSign(String s) {
	sign = s;
    }
 

    /**
     * Encodes this structure into canonical form.
     * @return encoded form of the header.
     */
    public String encode() {
	String retval = new Long(time).toString();
	retval += Separators.SP;
	if (sign != null) retval += sign;
	retval += offset.encode();
	return retval;
    }

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	ZoneAdjustment retval = new ZoneAdjustment();
	retval.sign = this.sign;
	retval.offset = (TypedTime) this.offset.clone();
	retval.time = this.time;
	return retval;
    }
 


}
