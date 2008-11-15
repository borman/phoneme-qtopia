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
import gov.nist.siplite.parser.Lexer;

/**
 * Stores hostname.
 *@version  JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 *
 */
public class Host {
    /** Type of host is a textual host name. */
    protected static final int HOSTNAME = 1;
    /** Type of the host is a numeric IPV4 address. */
    protected static final int IPV4ADDRESS = 2;
    /** Type of the host is a numeric IPV6 address. */
    protected static final int IPV6ADDRESS = 3;

    /** Host name field.  */
    protected String hostname;

    /** Address field. */
    protected int addressType;

    /** Default constructor. */
    public Host() {
        addressType = HOSTNAME;
    }

    /**
     * Constructor given host name or IP address.
     * This method checks the type of and IP address
     * to determine if it is an IpV4 or IPv6 address.
     *
     * @param newHostName host name to be stored.
     * @throws IllegalArgumentException in case of invalid host name
     */
    public Host(String newHostName) throws IllegalArgumentException {
        setHostname(newHostName);
    }

    /**
     * Return the host name in encoded form.
     * @return the host name saved when the instance was
     * created
     */
    public String encode() {
        if (addressType == IPV6ADDRESS && 
            '[' != hostname.charAt(0))
            return "[" + hostname + "]";
        return hostname;
    }

    /**
     * Compares for equality of hosts.
     * Host names are compared by textual equality. No dns lookup
     * is performed.
     * @param obj Object to set
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        Host otherHost = (Host) obj;
        return otherHost.hostname.equals(hostname);

    }

    /**
     * Gets the HostName field.
     * @return the host name saved when the instance was created
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the Address field.
     * @return the host name saved when the instance was created
     */
    public String getAddress() {
        return hostname;
    }


    /**
     * Sets the hostname member.
     * @param h host name to set
     * @throws IllegalArgumentException in case of invalid host name
     */
    public void setHostname(String h) throws IllegalArgumentException {
        if (h == null) {
            throw new IllegalArgumentException("Null address");
        }
        h = h.trim().toLowerCase();

        // IPv4 has stronger restriction than hostname
        if (Lexer.isValidIpv4Address(h)) {
            addressType = IPV4ADDRESS;
        } else if (Lexer.isValidHostname(h)) {
            addressType = HOSTNAME;
        } else {
            String addr = h;
            // IPv6 reference?
            if (h.charAt(0) == '[' && 
                h.charAt(h.length()-1) == ']') {
                addr = h.substring(1, h.length()-1);
            }
            if (!Lexer.isValidIpv6Address(addr)) {
                throw new IllegalArgumentException(
                                                  "Illegal hostname " + addr);
            }
        }
        hostname = h;
    }

    /**
     * Sets the address member.
     * @param address address to set
     * @throws IllegalArgumentException in case of invalid host name
     */
    public void setAddress(String address) throws IllegalArgumentException {
        setHostname(address);
    }

    /**
     * Returns true if the address is a DNS host name
     *  (and not an IPV4 address).
     * @return true if the hostname is a DNS name
     */
    public boolean isHostname() {
        return addressType == HOSTNAME;
    }

    /**
     * Returns true if the address is a DNS host name
     *  (and not an IPV4 address).
     * @return true if the hostname is host address.
     */
    public boolean isIPAddress() {
        return addressType != HOSTNAME;
    }

    /**
     * Makes a copy of the current instance.
     * @return copy of current object
     */
    public Object clone() {
        Host retval = new Host();
        retval.addressType = this.addressType;
        retval.hostname = new String(this.hostname);
        return retval;
    }
}
