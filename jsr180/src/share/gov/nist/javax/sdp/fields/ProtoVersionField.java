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

/**
 * Proto version field of SDP announce.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProtoVersionField extends SDPField {
    /** Protocol version. */
    protected int protoVersion;
    
    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	ProtoVersionField retval = new ProtoVersionField();
	retval.protoVersion = this.protoVersion;
	return retval;
    }
    /** Default constructor. */
    public ProtoVersionField() {
	super(PROTO_VERSION_FIELD); 
    }
    /**
     * Gets the protocol version number.
     * @return the protocol version number
     */
    public int getProtoVersion() {
	return protoVersion;
    }

    /**
     * Sets the protocol version member.
     * @param pv the new protocol version number
     */
    public void setProtoVersion(int pv) {
	protoVersion = pv;
    }

    /**
     * Returns the version number.
     * @throws SdpParseException if a parsing error occurs
     * @return the version number
     */ 
    public int getVersion()
	throws SdpParseException {
	return getProtoVersion();
    }
 
    /**
     * Sets the version number.
     * @param value the new version value.
     * @throws SdpException if the value is less than 0
     */ 
    public void setVersion(int value)
	throws SdpException {
	if (value < 0)
	    throw new SdpException("The value is < 0");
	else setProtoVersion(value); 
    }

    /**
     * Gets the string encoded version of this object.
     * @return encoded string of object contents
     * @since v1.0
     */
    public String encode() {
	return PROTO_VERSION_FIELD + protoVersion + Separators.NEWLINE;
    }
 
}
