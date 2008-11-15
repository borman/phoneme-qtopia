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
package gov.nist.siplite.header;

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

/**
 *  Protocol name and version.
 *
 * @version  1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Protocol extends GenericObject {
    
    /**
     *  protocolName field
     */
    protected String protocolName;
    
    /**
     *  protocolVersion field
     */
    protected String protocolVersion;
    
    /**
     *  transport field
     */
    protected String transport;
    
    
    /**
     * Default constructor.
     */
    public Protocol() {
        protocolName = "SIP";
        protocolVersion = "2.0";
        transport = SIPConstants.TRANSPORT_UDP;
    }
    
    
    /**
     * Compare two protocols for equality.
     * @return true if the two protocols are the same.
     * @param other Object to set
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass())) {
            return false;
        }
        Protocol that = (Protocol) other;
        if (Utils.compareToIgnoreCase(protocolName, that.protocolName)
        != 0) {
            return false;
        }
        if (Utils.compareToIgnoreCase(protocolVersion, protocolVersion)
        != 0) {
            return false;
        }
        if (Utils.compareToIgnoreCase(transport, that.transport) != 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Return canonical form.
     * @return String
     */
    public String encode() {
        return protocolName.toUpperCase() + Separators.SLASH +
                protocolVersion +
                Separators.SLASH + transport.toUpperCase();
    }
    
    /**
     *  get the protocol name
     * @return String
     */
    public String getProtocolName() {
        return protocolName;
    }
    
    /**
     *  get the protocol version
     * @return String
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     *  get the transport
     * @return String
     */
    public String getTransport() {
        return transport;
    }
    
    /**
     * Set the protocolName member
     * @param p String to set
     */
    public void setProtocolName(String p) {
        protocolName = p;
    }
    
    /**
     * Set the protocolVersion member
     * @param p String to set
     */
    public void setProtocolVersion(String p) {
        protocolVersion = p;
    }
    
    /**
     * Set the transport member
     * @param t String to set
     */
    public void setTransport(String t) {
        transport = t;
    }
    
    /**
     *  Clone this structure.
     * @return Object Protocol
     */
    public Object clone() {
        Protocol retval = new Protocol();
        
        if (this.protocolName != null)
            retval.protocolName = new String(this.protocolName);
        if (this.protocolVersion != null)
            retval.protocolVersion = new String(this.protocolVersion);
        if (this.transport != null)
            retval.transport = new String(this.transport);
        return (Object) retval;
        
    }
    
}
