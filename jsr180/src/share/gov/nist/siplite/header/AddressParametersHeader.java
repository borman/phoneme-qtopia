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
package gov.nist.siplite.header;

import gov.nist.siplite.parser.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;

/**
 * An abstract class for headers that take an address and parameters.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public abstract class AddressParametersHeader extends ParametersHeader {
    /** Saved address. */
    protected Address address;

    /**
     * Gets the Address field.
     * @return the imbedded Address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the Address field.
     * @param address Address to set
     */
    public void setAddress(Address address) {
        this.address = (Address) address;
    }

    /**
     * Constructor given the name of the header.
     * @param name header to process
     */
    protected AddressParametersHeader(String name) {
        super(name);
    }

    /**
     * Gets the address value.
     * @return the address value
     */
    public Object getValue() {
        return address;
    }

    /**
     * Sets the header value field.
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {
        Header header;
        StringMsgParser smp = new StringMsgParser();
        String strNewHeader = getName() + Separators.COLON + value;

        try {
            header = smp.parseHeader(strNewHeader);

            if (header instanceof HeaderList) {
                header = ((HeaderList)header).getFirst();
            }

            setAddress(((AddressParametersHeader)header).getAddress());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Gets the user friendly display name.
     * @return the display name
     */
    public String getDisplayName() {
        return address.getDisplayName();
    }

    /**
     * Gets the user at host and port name.
     * @return user@host:port
     */
    public String getUserAtHostPort() {
        return address.getUserAtHostPort();
    }

    /**
     * Gets the host and port name.
     * @return host:port
     */
    public HostPort getHostPort() {
        return address.getHostPort();
    }

    /**
     * Compares obejct for equivalence.
     * @param other the object to compare
     * @return true if matches
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass()))
            return false;
        Address otherAddress = ((AddressParametersHeader) other).getAddress();
        if (otherAddress == null)
            return false;
        if (! otherAddress.equals(address)) {
            return false;
        }
        if (! parameters.equals
                (((AddressParametersHeader)other).parameters)) {
            return false;
        } else return true;
    }

    /**
     * Encode the header content into a String.
     * @return String
     */
    public String encodeBody() {
        if (address == null) {
            throw new RuntimeException("No body!");
        }
        StringBuffer retval = new StringBuffer();
        retval .append(address.encode());
        retval.append(encodeWithSep());
        return retval.toString();
    }

    /**
     * Copies the current instance.
     * @return copy of currrent object
     */
    public Object clone() {
        Exception ex = null;
        try {
            AddressParametersHeader retval =
                    (AddressParametersHeader) this.getClass().newInstance();
            if (this.address != null)
                retval.address = (Address) this.address.clone();
            if (this.parameters != null)
                retval.parameters = (NameValueList) this.parameters.clone();
            return retval;
        } catch (InstantiationException ie) {
            ex = ie;
        } catch (IllegalAccessException iae) {
            ex = iae;
        }
        if (ex != null) {
            InternalErrorHandler.handleException(ex);
        }
        return null;
    }
}


