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
package gov.nist.core;

/**
 * Holds the hostname:port.
 *
 *@version  JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class HostPort  {
    
    // host / ipv4/ ipv6/
    /** Host field.  */
    protected Host  host;
    
    /** Port field. */
    protected Integer    port;
    
    /** Default constructor. */
    public HostPort() {
        
        host = null;
        port = null; // marker for not set.
    }
    
    /**
     * Encodes this hostport into its string representation.
     * Note that this could be different from the string that has
     * been parsed if something has been edited.
     * @return host and port encoded string
     */
    public String encode() {
        StringBuffer retval = new StringBuffer();
        if (host != null)
            retval.append(host.encode());
        if (port != null)
            retval.append(':').append(port.toString());
        return retval.toString();
    }
    
    /**
     * Returns true if the two objects are equals, false otherwise.
     * @param other Object to set
     * @return boolean
     */
    public boolean equals(Object other) {
        if (! this.getClass().equals(other.getClass())) {
            return false;
        }
        HostPort that = (HostPort) other;
        if ((this.port == null && that.port != null) ||
                (this.port != null && that.port == null))
            return false;
        else if (this.port == that.port && this.host.equals(that.host))
            return true;
        else
            return this.host.equals(that.host) && this.port.equals(that.port);
    }
    
    /**
     * Gets the Host field.
     * @return host field
     */
    public Host getHost() {
        return host;
    }
    
    /**
     * Gets the port field.
     * @return int
     */
    public	 int getPort() {
        if (port == null) {
            return -1;
        } else {
            return port.intValue();
        }
    }
    
    /**
     * Returns boolean value indicating if Header has port
     * @return boolean value indicating if Header has port
     */
    public boolean hasPort() {
        return  port != null;
    }
    
    /** Removes the  port. */
    public void removePort() {
        port = null;
    }
    
    /**
     * Sets the host member.
     * @param h Host to set
     */
    public void setHost(Host h) {
        host = h;
    }
    
    /**
     * Sets the port member.
     * @param p int to set
     * @throws IllegalArgumentException in case of illegal port value
     */
    public void setPort(int p) throws IllegalArgumentException {
        if (p > 65535 || p < 0) {
            throw new IllegalArgumentException("Illegal port value " + p);
        }
        port = new Integer(p);
    }
    
    /**
     * Makes a copy of the current instance.
     * @return copy of current object
     */
    public Object clone() {
        HostPort retval = new HostPort();
        if (this.host != null) retval.host = (Host)this.host.clone();
        if (this.port != null) retval.port = new Integer
                (this.port.intValue());
        return retval;
    }
}
