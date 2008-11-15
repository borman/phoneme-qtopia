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
package gov.nist.siplite.stack;

import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import gov.nist.siplite.header.ViaHeader;

import gov.nist.siplite.message.*;
import gov.nist.siplite.*;
import java.util.*;

/**
 * This is the default router. When the implementation wants to forward
 * a request and had run out of othe options, then it calls this method
 * to figure out where to send the request. The default router implements
 * a simple "default routing algorithm" which just forwards to the configured
 * proxy address.
 */

public class DefaultRouter implements Router {
    /** Default route. */
    protected Hop defaultRoute;
    /** Current SIP stack context. */
    protected SipStack sipStack;
    
    /** Default constructor. */
    public DefaultRouter() {
    }
    
    /**
     * Sets the next hop address.
     * @param hopString is a string which is interpreted
     * by us in the following fashion :
     * host:port/TRANSPORT determines the next hop.
     */
    public void setNextHop(String hopString)
    throws IllegalArgumentException {
        defaultRoute = new Hop(hopString);
        defaultRoute.setDefaultRouteFlag();
    }
    
    /**
     * Return a linked list of addresses corresponding to a requestURI.
     * This is called for sending out outbound messages for which we do
     * not directly have the request URI. The implementaion function
     * is expected to return a linked list of addresses to which the
     * request is forwarded. The implementation may use this method
     * to perform location searches etc.
     *
     * @param sipRequest is the message to route.
     * @param isDialog target URI is taken from route list inside of dialog,
     * else it is taken from request URI
     * @return enumeration of next hops
     */
    public Enumeration getNextHops(Request sipRequest, boolean isDialog)
    throws IllegalArgumentException {
        Vector hopList = new Vector();

        if (defaultRoute != null) {
            hopList.addElement(defaultRoute);
        } else {
            URI requestUri = null;
            String transport = null;
            RouteList rl = sipRequest.getRouteHeaders();

            // When request has no route list,
            // it's destination URI is same as out of dialog
            if (rl == null || rl.isEmpty()) {
                isDialog = false;
            }

            Hop hop;

            // out of dialog - get destination URI from request URI
            if (!isDialog) {
                requestUri = sipRequest.getRequestURI();
                SipURI requestLineUri =
                    (SipURI)sipRequest.getRequestLine().getUri();
                if (requestLineUri.hasTransport()) {
                    transport = requestLineUri.getTransportParam();
                } else {
                    transport = sipStack.getDefaultTransport();
                }

                hop = createHop(requestUri, transport);
                hopList.addElement(hop);
            } else { // inside dialog - get destination URI from first route
                Enumeration el = rl.getElements();
                RouteHeader route; 
                while (el.hasMoreElements()) {
                    route = (RouteHeader) el.nextElement();
                    requestUri = route.getAddress().getURI();
                    transport = route.getAddress().
                        getParameter(SIPConstants.GENERAL_TRANSPORT);
                    hop = createHop(requestUri, transport);
                    hopList.addElement(hop);
                }
            }
        }
        return hopList.elements();
    }

    /**
     * Creates new Hop for given uri and transport
     * 
     * @param requestUri next hop address
     * @param transport next hop connection transport type
     * @return new Hop if requestUri is SipURI
     *         otherwise null
     */
    private Hop createHop(URI requestUri, String transport) {
        Hop hop = null;
        if (requestUri.isSipURI()) {
            SipURI sipUri = (SipURI)requestUri;
            int port = sipUri.getPort();
            if (port == -1) { // default port
                port = SIPConstants.DEFAULT_NONTLS_PORT;
            }
            hop = new Hop(sipUri.getHost(), port, 
                              transport);
        }
        return hop;
    }

    /**
     * Gets the default hop.
     * @return defaultRoute is the default route.
     */
    public Hop getOutboundProxy() { return this.defaultRoute; }
    
    /**
     * Sets the outbound proxy.
     * @param outboundProxy the new proxy location
     */
    public void setOutboundProxy(String outboundProxy) {
        this.defaultRoute = new Hop(outboundProxy);
    }
    
    /**
     * Sets the SIP stack context.
     * @param sipStack the new SIP stack
     */
    public void setSipStack(SipStack sipStack) {
        this.sipStack = sipStack;
    }
}
