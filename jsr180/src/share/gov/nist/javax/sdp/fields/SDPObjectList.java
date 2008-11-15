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

import java.util.Vector;
/**
 * Base list interface for SDP objects.
 */
public class SDPObjectList extends Vector {
    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	SDPObjectList retval = new SDPObjectList();
	for (int i = 0; i < this.size(); i++) {
	    Object obj = 
		((SDPObject) this.elementAt(i)).clone();
	    retval.addElement(obj);
	}
	return retval;
    }

    /**
     * Adds an sdp object to this list.
     * @param s new object for the list
     */
    public void add(Object s) { 
	this.addElement(s);
    }


    /**
     * Gets the input text of the sdp object (from which the object was
     * generated).
     */
    public SDPObjectList() {
	super();
    }

    /**
     * Encodes list a single text string.
     * @return encoded string of object contents
     */
    public String encode() {
	StringBuffer retval = new StringBuffer();
	SDPObject sdpObject;
	for (int i = 0; i < this.size(); i++) {
	    sdpObject = (SDPObject) this.elementAt(i);
	    retval.append(sdpObject.encode());
	}
	return retval.toString();
    }

    /**
     * Encodes list as single text string.
     * @return encode string of object contents
     */
    public String toString() {
	return this.encode();
    }
}
