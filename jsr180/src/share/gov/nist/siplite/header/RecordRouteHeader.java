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

import gov.nist.siplite.address.*;


/**
 * The Request-Route header is added to a request by any proxy that insists on
 * being in the path of subsequent requests for the same call leg.
 *
 * Record-Route = "Record-Route" HCOLON 1#
 * ( name-addr *( SEMI rr-param ))
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class RecordRouteHeader extends AddressParametersHeader {
    /** Class handle. */
    public static Class clazz;
    /** Record route header field label. */
    public static final String NAME = Header.RECORD_ROUTE;
    
    static {
        clazz = new RecordRouteHeader().getClass();
    }
    
    /**
     * Constructor with initial addres.
     * @param address address to set
     */
    public RecordRouteHeader(Address address) {
        super(RECORD_ROUTE);
        this.address = address;
        
    }
    
    /**
     * Default constructor.
     */
    public RecordRouteHeader() {
        super(RECORD_ROUTE);
        
    }
}
