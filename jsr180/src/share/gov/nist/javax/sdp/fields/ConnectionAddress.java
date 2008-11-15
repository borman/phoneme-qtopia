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
 * Connection Address of the SDP header (appears as part of the
 * Connection field).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ConnectionAddress extends SDPObject {
    /** The host address. */
    protected Host address;
    /** Time to live. */
    protected int ttl;
    /** The target port. */
    protected int port;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	ConnectionAddress retval = new ConnectionAddress();
	if (address != null) 
	    retval.address = (Host) address.clone();
	retval.ttl = ttl;
	retval.port = port;
	return retval;
    }

    /** 
     * Gets the target host address. 
     * @return the host address
     */
    public Host getAddress() {
	return address; 
    } 

    /** 
     * Gets the time to live parameter.
     * @return the time to live value
     */
    public int getTtl() {
	return ttl;
    } 

    /**
     * Gets the target port address.
     * @return the port number
     */
    public int getPort() {
	return port;
    } 

    /**
     * Sets the address member.
     * @param a the new value for address
     */
    public void setAddress(Host a) {
	address = a;
    } 

    /**
     * Sets the time to live member.
     * @param ttl the new time to live value
     */
    public void setTtl(int ttl) {
	this.ttl = ttl;
    } 

    /**
     * Sets the port member.
     * @param p the new target port number
     */
    public void setPort(int p) {
	port = p; 
    } 

    /**
     * Gets the string encoded version of this object.
     * @return the encode string of the object contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string = "";

	if (address != null) encoded_string = address.encode();
	if (ttl != 0 && port != 0) {
	    encoded_string += Separators.SLASH + 
		ttl + Separators.SLASH + port;
	} else if (ttl != 0) {
	    encoded_string += Separators.SLASH + ttl;
	}
	return encoded_string; 
    }

}
