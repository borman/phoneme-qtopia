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
 * Origin Field SDP header
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class OriginField extends SDPField {
    /** User name. */
    protected String username;
    /** Session identifier. */
    protected long sessId;
    /** Session version number. */
    protected long sessVersion; 
    /** Network type. */
    protected String nettype; // INT
    /** Address type. */
    protected String addrtype; // IPV4/6
    /** Target host address. */
    protected Host address;

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	OriginField retval = new OriginField();
	retval.username = this.username;
	retval.sessId = this.sessId;
	retval.sessVersion = this.sessVersion;
	retval.nettype = this.nettype;
	retval.addrtype = this.addrtype;
	if (this.address != null) 
	    retval.address = (Host) this.address.clone();
	return retval;
    }

    /** Default constructor. */
    public OriginField() {
	super(ORIGIN_FIELD);
    }

    /**
     * Returns the name of the session originator.
     * @throws SdpParseException
     * @return the string username.
     */ 
    public String getUsername() 
	throws SdpParseException {
	return username; 
    } 

    /**
     * Gets the session identifier member.
     * @return the session identifier
     */
    public long getSessId() {
	return sessId;
    } 

    /**
     * Gets the session version member.
     * @return the session version number
     */
    public long getSessVersion() {
	return sessVersion;
    } 

    /**
     * Gets the network type member.
     * @return the network type
     */
    public String getNettype() {
	return nettype;
    } 

    /**
     * Gets the address type member.
     * @return the target address
     */
    public String getAddrtype() {
	return addrtype;
    } 

    /**
     * Gets the host member.
     * @return the host
     */
    public Host getHost() {
	return address;
    } 

    /**
     * Sets the session identifier member.
     * @param s the new session identifier
     */
    public void setSessId(long s) {
	sessId = s;
    } 

    /**
     * Sets the session version member.
     * @param s the new session version number
     */
    public void setSessVersion(long s) {
	sessVersion = s;
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
    public void setAddrtype(String a) {
	addrtype = a;
    } 

    /**
     * Sets the address member.
     * @param a the new address
     */
    public void setAddress(Host a) {
	address = a;
    } 

 
    /**
     * Sets the name of the session originator.
     * @param user the string username.
     * @throws SdpException if the parameter is null
     */ 
    public void setUsername(String user)
	throws SdpException {
	if (user == null)
	    throw new SdpException("The user parameter is null");
	else {
	    this.username = user;
	}
    }
 
    /**
     * Returns the unique identity of the session.
     * @throws SdpParseException if a parsing error occurs
     * @return the session id.
     */ 
    public long getSessionId()
	throws SdpParseException {
	return getSessId(); 
    }
 
    /**
     * Sets the unique identity of the session.
     * @param id the session id.
     * @throws SdpException if the id is less than 0
     */ 
    public void setSessionId(long id)
	throws SdpException {
	if (id < 0)
	    throw new SdpException("The is parameter is < 0");
	else
	    setSessId(id);
    }
 
    /**
     * Returns the unique version of the session.
     * @throws SdpException if the session version is not set
     * @return the session version.
     */ 
    public long getSessionVersion()
	throws SdpParseException {
	return getSessVersion();
    }
 
    /**
     * Sets the unique version of the session.
     * @param version the session version.
     * @throws SdpException if the version is  less than 0
     */ 
    public void setSessionVersion(long version)
	throws SdpException {
	if (version < 0) 
	    throw new SdpException("The version parameter is < 0");
	else
	    setSessVersion(version);
    }
 
    /**
     * Returns the type of the network for this Connection.
     * @throws SdpParseException if a parsing error occurs
     * @return the string network type.
     */ 
    public String getAddress()
	throws SdpParseException {
	Host addr = getHost(); 
	if (addr == null)
	    return null;
	else return addr.getAddress();
    }
 
    /**
     * Returns the type of the address for this Connection.
     * @throws SdpParseException if a parsing error occurs
     * @return the string address type.
     */ 
    public String getAddressType()
	throws SdpParseException {
	return getAddrtype();
    }
 
    /** 
     * Returns the type of the network for this Connection
     * @throws SdpParseException if a parsing error occurs
     * @return the string network type.
     */ 
    public String getNetworkType()
	throws SdpParseException {
	return getNettype();
    }
 
    /**
     * Sets the type of the address for this Connection.
     * @param addr string address type.
     * @throws SdpException if the addr is null
     */ 
    public void setAddress(String addr)
	throws SdpException {
	if (addr == null)
	    throw new SdpException("The addr parameter is null");
	else {
	    Host host = getHost(); 
	    if (host == null)
		host = new Host();
	    host.setAddress(addr);
	    setAddress(host);
	}
    }
 
    /**
     * Returns the type of the network for this Connection.
     * @param type the string network type.
     * @throws SdpException if the type is null
     */ 
    public void setAddressType(String type)
	throws SdpException {
	if (type == null)
	    throw new SdpException("The type parameter is < 0");
	else
	    setAddrtype(type);
    }
 
    /**
     * Sets the type of the network for this Connection.
     * @param type the string network type.
     * @throws SdpException if the type is null
     */ 
    public void setNetworkType(String type)
	throws SdpException {
	if (type == null)
	    throw new SdpException("The type parameter is < 0");
	else setNettype(type);
    }

    /**
     * Gets the string encoded version of this object.
     * @return encode string of object contents
     * @since v1.0
     */
    public String encode() {
	return ORIGIN_FIELD + username + Separators.SP 
	    + sessId + Separators.SP
	    + sessVersion + Separators.SP
	    + nettype + Separators.SP
	    + addrtype + Separators.SP
	    + address.encode() + Separators.NEWLINE;
    }

}
