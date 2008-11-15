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
import  gov.nist.siplite.address.*;
import  gov.nist.siplite.header.*;
import  gov.nist.siplite.message.*;
import com.sun.j2me.security.Token;

/**
 * Main SIP factory for instance classes.
 */
public class SipFactory {
    /** Main SIP factory. */
    private static SipFactory myFactory;
    /** Address factory. */
    private static AddressFactory addressFactory;
    /** Messgae factory. */
    private static MessageFactory msgFactory;
    /** Header factoey. */
    private static HeaderFactory headerFactory;
    
    /** Default private constructor (singleton). */
    private SipFactory() {
        // Dont let outsiders call me!
    }
    
    /**
     * Gets a handle the the SIP factory handler.
     * @return handle to the SIP factory singelton
     */
    public static  SipFactory getInstance() {
        if (myFactory == null) myFactory = new SipFactory();
        return myFactory;
    }
    
    /**
     * Creates a SIP Stack based on requested properties.
     * @param properties configuration of current SIP STack
     * @param classSecurityToken Token object for saving
     * @return SIP Stack context
     */
    public SipStack createSipStack
            (ConfigurationProperties properties,
	    Token classSecurityToken)
            throws PeerUnavailableException {
        return new SipStack(properties, classSecurityToken);
    }
    
    /**
     * Creates a mesage factory.
     * @return handle to message factory handler
     */
    public MessageFactory createMessageFactory() {
        if (msgFactory != null)
            return msgFactory;
        msgFactory =  new MessageFactory();
        return msgFactory;
    }
    
    /**
     * Creates a header factory.
     * @return handle to header factory handler
     */
    public HeaderFactory createHeaderFactory() {
        if (headerFactory != null)
            return headerFactory;
        headerFactory = new HeaderFactory();
        return headerFactory;
    }
    
    /**
     * Creates a address factory.
     * @return handle to address factory handler
     */
    public AddressFactory createAddressFactory() {
        if (addressFactory != null)
            return addressFactory;
        addressFactory = new AddressFactory();
        return addressFactory;
    }
    
}



