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
 * Hop.java
 *
 * Created on July 15, 2001, 2:28 PM
 */

package gov.nist.siplite.address;

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Routing algorithms return a list of hops to which the request is
 * routed.
 */
public class Hop extends Object {
    /** Current host. */
    protected String host;
    /** Port number. */
    protected int port;
    /** Connection transport. */
    protected String transport;
    /** Explicit route from a ROUTE header. */
    protected boolean explicitRoute;
    /** Default route from the proxy addr. */
    protected boolean defaultRoute;
    /** URI route from the requestURI. */
    protected boolean uriRoute;
    
    /**
     * Encodes contents int a string.
     * @return encoded string of object contents
     */
    public String toString() {
        return host + ":" + port + "/" + transport;
    }
    
    /**
     * Compares fro equivalence.
     * @param other the object to compare
     * @return true if the object matches
     */
    public boolean equals(Object other) {
        if (other.getClass().equals(this.getClass())) {
            Hop otherhop = (Hop) other;
            return (otherhop.host.equals(this.host) &&
                    otherhop.port == this.port);
        } else return false;
    }
    
    
    /**
     * Create new hop given host, port and transport.
     * @param hostName hostname
     * @param portNumber port
     * @param trans transport
     * @throws IllegalArgument exception if some parameter
     * has invalid value.
     */
    public Hop(String hostName, int portNumber, String trans)
            throws IllegalArgumentException {
        
        if (portNumber < 0) {
            throw new IllegalArgumentException("Invalid port: " + portNumber);
        }
        
        host = hostName;
        port = portNumber;
        
        if (trans == null)
            transport = SIPConstants.TRANSPORT_UDP;
        else if (trans == "")
            transport = SIPConstants.TRANSPORT_UDP;
        else
            transport = trans;
    }
    
    /**
     * Creates new Hop
     * @param hop is a hop string in the form of host:port/Transport
     * @throws IllegalArgument exception if string is not properly formatted or
     * null.
     */
    public Hop(String hop) throws IllegalArgumentException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "create hop for " + hop);
        }
        
        if (hop == null)
            throw new IllegalArgumentException("Null arg!");
        
        try {
            StringTokenizer stringTokenizer = new StringTokenizer(hop + "/");
            String hostPort = stringTokenizer.getNextToken('/');
            // Skip over the slash.
            stringTokenizer.getNextChar();
            // get the transport string.
            transport = stringTokenizer.getNextToken('/').trim();
            if (transport == null)
                transport = SIPConstants.TRANSPORT_UDP;
            else if (transport == "")
                transport = SIPConstants.TRANSPORT_UDP;
            if (Utils.compareToIgnoreCase(transport, SIPConstants.
                    TRANSPORT_UDP) != 0 && 
                    Utils.compareToIgnoreCase(transport, SIPConstants.
                    TRANSPORT_TCP) != 0) {
                System.out.println("Bad transport string " + transport);
                throw new IllegalArgumentException(hop);
            }
            stringTokenizer = new StringTokenizer(hostPort+":");
            host = stringTokenizer.getNextToken(':');
            if (host == null || host.equals(""))
                throw new IllegalArgumentException("no host!");
            stringTokenizer.consume(1);
            String portString = null;
            portString = stringTokenizer.getNextToken(':');
            
            if (portString == null || portString.equals("")) {
                throw new IllegalArgumentException("no port!");
                // port = 5060;
            } else {
                try {
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Bad port spec");
                }
            }
            defaultRoute = true;
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Bad hop");
        }
        
    }
    
    /**
     * Retruns the host string.
     * @return host String
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Returns the port.
     * @return port integer.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the transport string.
     * @return the transport string
     */
    public String getTransport() {
        return transport;
    }
    
    /**
     * Return true if this is an explicit route (extacted from a ROUTE
     * Header).
     * @return the explicit route
     */
    public boolean isExplicitRoute() {
        return explicitRoute;
    }
    
    /**
     * Return true if this is a default route (next hop proxy address).
     * @return true if this is the default route
     */
    public boolean isDefaultRoute() {
        return defaultRoute;
    }
    
    /**
     * Return true if this is uriRoute.
     * @return true if this is the URI route
     */
    public boolean isURIRoute() { return uriRoute; }
    
    /**
     * Set the URIRoute flag.
     */
    public void setURIRouteFlag() { uriRoute = true; }
    
    
    /**
     * Set the defaultRouteFlag.
     */
    public void setDefaultRouteFlag() { defaultRoute = true; }
    
    /**
     * Set the explicitRoute flag.
     */
    public void setExplicitRouteFlag() { explicitRoute = true; }
    
    
}
