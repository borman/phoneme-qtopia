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
package gov.nist.siplite;
import gov.nist.siplite.stack.*;
import gov.nist.core.*;

/**
 * Implementation of the ListeningPoint interface
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ListeningPoint  {
    /** Host being monitored. */
    protected String host;
    /** Current connection. */
    protected String transport;
    
    /** My port. (same thing as in the message processor) */
    
    int port;
    
    /**
     * Pointer to the imbedded mesage processor.
     */
    protected MessageProcessor messageProcessor[];
    
    /**
     * Provider back pointer.
     */
    protected SipProvider sipProviderImpl;
    
    /**
     * Our stack.
     */
    protected SipStack sipStack;
    
    /**
     * Constructs a key to refer to this structure from the SIP stack.
     * @param host host string
     * @param port port
     * @param transport transport
     * @return a string that is used as a key
     */
    public static String makeKey(String host, int port, String transport) {
        return new StringBuffer(host).
                append(":").
                append(port).
                append("/").
                append(transport).
                toString().
                toLowerCase();
    }
    
    /**
     * Gets the key for this struct.
     * @return  get the host
     */
    protected String getKey() {
        return makeKey(host, port, transport);
        // return makeKey(host, port, transport);
    }
    
    
    /**
     * Sets the sip provider for this structure.
     * @param sipProviderImpl provider to set
     */
    protected void setSipProvider(SipProvider sipProviderImpl) {
        this.sipProviderImpl = sipProviderImpl;
    }
    
    /**
     * Removes the sip provider from this listening point.
     */
    protected void removeSipProvider() {
        this.sipProviderImpl = null;
    }
    
    /**
     * Constructor.
     * @param sipStack context for the current transaction
     * @param host Host name
     * @param port the channel to moonitor
     * @param transport the connection being used
     */
    protected ListeningPoint(
            SipStack sipStack,
            String host,
            int port,
            String transport) {
        this.sipStack = (SipStack) sipStack;
        // this.host = sipStack.getIPAddress();
        this.host = host;
        this.port = port;
        this.transport = transport;
        this.messageProcessor = new MessageProcessor[2];
    }
    
    
    
    
    
    /**
     * Gets host name of this ListeningPoint.
     *
     * @return host of ListeningPoint
     */
    public String getHost() {
        return this.sipStack.getHostAddress();
    }
    
    /**
     * Gets the port of the ListeningPoint. The default port of a ListeningPoint
     * is dependent on the scheme and transport.  For example:
     * <ul>
     * <li>The default port is 5060 if the transport
     * UDP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport
     * is TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5060 if the transport
     * is SCTP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport
     * is TLS over TCP the scheme is <i>sip:</i>.
     * <li>The default port is 5061 if the transport
     * is TCP the scheme is <i>sips:</i>.
     * </ul>
     *
     * @return port of ListeningPoint
     */
    public int getPort() {
        return messageProcessor[0].getPort();
    }
    
    /**
     * Gets transport of the ListeningPoint.
     *
     * @return transport of ListeningPoint
     */
    public String getTransport() {
        return messageProcessor[0].getTransport();
    }
    
    /**
     * Getsthe provider.
     *
     * @return the provider.
     */
    public SipProvider getProvider() {
        return this.sipProviderImpl;
    }
}
