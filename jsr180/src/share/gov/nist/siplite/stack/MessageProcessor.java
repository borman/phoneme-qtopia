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

import java.io.IOException;
import gov.nist.core.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.*;

/**
 * This is the Stack abstraction for the active object that waits
 * for messages to appear on the wire and processes these messages
 * by calling the MessageFactory interface to create a ServerRequest
 * or ServerResponse object. The main job of the message processor is
 * to instantiate message channels for the given transport.
 *
 * @version JAIN-SIP-1.1
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public abstract class MessageProcessor {
    /** Currrent listening point. */
    private ListeningPoint listeningPoint;
    /** Flag indicating exit in progress. */
    boolean exitFlag;
    /** Number of message processor users. */
    private int useCount;
    
    /**
     * Gets the transport string.
     * @return A string that indicates the transport.
     * (i.e. "tcp" or "udp")
     */
    public abstract String getTransport();
    
    /**
     * Gets the port identifier.
     * @return the port for this message processor. This is where you
     * receive messages.
     */
    public abstract int getPort();
    
    /**
     * Gets the SIP Stack.
     * @return the sip stack.
     */
    public abstract SIPMessageStack getSIPStack();
    
    /**
     * Creates a message channel for the specified host/port.
     * @param targetHostPort destination for logged message
     * @return New MessageChannel for this processor.
     */
    public abstract MessageChannel
            createMessageChannel(HostPort targetHostPort)
            throws IOException;
    
    
    /**
     * Starts our thread.
     */
    public abstract void start() throws IOException;
    
    /**
     * Stops the processor thread.
     */
    public abstract void stop();
    
    
    /**
     * Flags whether this processor is secure or not.
     * @return true if this processor is secure
     */
    public abstract boolean isSecure();
    
    /**
     * Return true if there are pending messages to be processed
     * (which prevents the message channel from being closed).
     * @return true if message channel is in use
     */
    synchronized public boolean inUse() {
        return (useCount != 0);
    }
    
    /**
     * Get the Via header to assign for this message processor.
     * @return the via header
     */
    public ViaHeader getViaHeader() {
        
        ViaHeader via = new ViaHeader();
        Host host = new Host();
        host.setHostname(this.getSIPStack().getHostAddress());
        via.setHost(host);
        via.setPort(this.getPort());
        via.setTransport(this.getTransport());
        return via;
    }
    
    /**
     * Returns the exitin progres flag.
     * @return true if exit is in progress
     */
    public boolean toExit() {
        return exitFlag;
    }
    
   
    /**
     * Sets the listening point for message processor.
     * @param lp the new listening point
     */
    public void setListeningPoint(ListeningPoint lp) {
        listeningPoint = lp;
    }
    
    /**
     * Gets the current listening point.
     * @return the current listening point
     */
    public ListeningPoint getListeningPoint() {
        return listeningPoint;
    }
    
    /**
     * Increments the count of channels.
     */
    synchronized void incrementUseCount() {
        useCount ++;
    }
    
    /**
     * Clears the count of channels.
     */
    synchronized void clearUseCount() {
        useCount = 0;
    }
    
    /**
     * Decrements the count of channels.
     */
    synchronized void decrementUseCount() {
        if (0 != useCount) {
             useCount --;
        }
    }
}
