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
 * Connectin Field of the SDP request.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ConnectionField extends SDPField {
    /** Network type. */
    protected String nettype;
    /** Address type. */
    protected String addrtype;
    /** Connection address. */
    protected ConnectionAddress address;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	ConnectionField retval = new ConnectionField();
	retval.nettype = this.nettype;
	retval.addrtype = this.addrtype;
	if (this.address != null) 
	    retval.address = (ConnectionAddress)
		this.address.clone();
	return retval;
    }


    /** Default constructor. */
 
    public ConnectionField() {
	super(SDPFieldNames.CONNECTION_FIELD);
    }

    /**
     * Gets the network type.
     * @return the network type
     */
    public String getNettype() { 
	return nettype;
    } 

    /**
     * Gets the address type.
     * @return the address type
     */
    public String getAddrtype() {
	return addrtype;
    } 
    /**
     * Gets the network connection address.
     * @return the connection address
     */
    public ConnectionAddress getConnectionAddress() {
	return address;
    } 

    /**
     * Sets the network type member.
     * @param n the new network type
     */
    public void setNettype(String n) {
	nettype = n;
    } 

    /**
     * Sets the address type member.
     * @param a the new address type
     */
    public void setAddrType(String a) {
	addrtype = a;
    } 

    /**
     * Sets the address member.
     * @param a the new connectio address
     */
    public void setAddress(ConnectionAddress a) {
	address = a;
    } 

    /**
     * Gets the string encoded version of this object.
     * @return the encoded text string of this object contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string = CONNECTION_FIELD;
	if (nettype != null) encoded_string += nettype;
	if (addrtype != null) encoded_string += Separators.SP + addrtype;
	if (address != null) encoded_string += Separators.SP + 
	    address.encode();
	return encoded_string += Separators.NEWLINE;
    }
 
    /**
     * Encodes contents as a textstring.
     * @return encoded string of object contents
     */
    public String toString() { return this.encode(); }

    /**
     * Returns the type of the network for this Connection.
     * @throws SdpParseException if a parsing error occurs
     * @return the type of the network
     */
    public String getAddress() throws SdpParseException {
	ConnectionAddress connectionAddress = getConnectionAddress();
	if (connectionAddress == null)
	    return null;
	else { 
	    Host host = connectionAddress.getAddress();
	    if (host == null)
		return null;
	    else return host.getAddress();
	}
    }
 
    /**
     * Returns the type of the address for this Connection.
     * @throws SdpParseException if a parsing error occurs
     * @return the type of the address
     */
    public String getAddressType() throws SdpParseException {
	return getAddrtype(); 
    }
 
    /**
     * Returns the type of the network for this Connection.
     * @throws SdpParseException if a parsing error occurs
     * @return the type of the network
     */
    public String getNetworkType() throws SdpParseException {
	return getNettype();
    }
 
    /**
     * Sets the type of the address for this Connection.
     * @param addr to set
     * @throws SdpException if the type is null
     */
    public void setAddress(String addr) throws SdpException {
	if (addr == null)
	    throw new SdpException("the addr is null");
	else {
	    if (address == null) {
		address = new ConnectionAddress();
		Host host = new Host(addr);
		address.setAddress(host);
	    } else {
		Host host = address.getAddress();
		if (host == null) {
		    host = new Host(addr);
		    address.setAddress(host);
		} else
		    host.setAddress(addr);
	    }
	    setAddress(address);
	}
    }
 
    /**
     * Returns the type of the network for this Connection.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setAddressType(String type) throws SdpException {
	if (type == null) 
	    throw new SdpException("the type is null");
	this.addrtype = type;
    }
 
    /**
     * Sets the type of the network for this Connection.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setNetworkType(String type) throws SdpException {
	if (type == null)
	    throw new SdpException("the type is null");
	else setNettype(type);
    }
 


}
