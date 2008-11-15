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
package gov.nist.siplite.address;
import java.util.Vector;

/**
 * Implementation of the TelURL interface.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TelURL extends URI {

    /** Currrent telephone portion of URI. */
    protected TelephoneNumber telephoneNumber;

    /** Creates a new instance of TelURLImpl */
    public TelURL() {
        this.scheme = "tel";
    }


    /**
     * Sets the telephone number.
     * @param telephoneNumber telephone number to set.
     */

    public void setTelephoneNumber(TelephoneNumber telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }


    /**
     * Returns the value of the <code>isdnSubAddress</code> parameter, or null
     * if it is not set.
     *
     * @return the value of the <code>isdnSubAddress</code> parameter
     */
    public String getIsdnSubAddress() {
        return telephoneNumber.getIsdnSubaddress();
    }

    /**
     * Returns the value of the <code>postDial</code> parameter, or null if it
     * is not set.
     *
     * @return the value of the <code>postDial</code> parameter
     */
    public String getPostDial() {
        return telephoneNumber.getPostDial();
    }

    /**
     * Returns <code>true</code> if this TelURL is global i.e. if the TelURI
     * has a global phone user.
     *
     * @return <code>true</code> if this TelURL represents a global phone user,
     * and <code>false</code> otherwise.
     */
    public boolean isGlobal() {
        return telephoneNumber.isGlobal();
    }

    /**
     * This method determines if this is a URI with a scheme of "sip"
     * or "sips".
     *
     * @return true if the scheme is "sip" or "sips", false otherwise.
     */
    public boolean isSipURI() {
        return false;
    }

    /**
     * This method determines if this is a URI with a scheme of
     * "tel"
     *
     * @return true if the scheme is "tel", false otherwise.
     */
    public boolean isTelURL() {
        return true;
    }


    /**
     * Sets phone user of this TelURL to be either global or local. The default
     * value is false, hence the TelURL is defaulted to local.
     *
     * @param global - the boolean value indicating if the TelURL has a global
     * phone user.
     */
    public void setGlobal(boolean global) {
        this.telephoneNumber.setGlobal(true);
    }

    /**
     * Sets ISDN subaddress of this TelURL. If a subaddress is present, it is
     * appended to the phone number after ";isub=".
     *
     * @param isdnSubAddress - new value of the <code>isdnSubAddress</code>
     * parameter
     */
    public void setIsdnSubAddress(String isdnSubAddress) {
        this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
    }

    /**
     * Sets post dial of this TelURL. The post-dial sequence describes what and
     * when the local entity should send to the phone line.
     *
     * @param postDial - new value of the <code>postDial</code> parameter
     */
    public void setPostDial(String postDial) {
        this.telephoneNumber.setPostDial(postDial);
    }


    /**
     * Set the telephone number.
     * @param telephoneNumber -- long phone number to set.
     */
    public void setPhoneNumber(String telephoneNumber) {
        this.telephoneNumber.setPhoneNumber(telephoneNumber);
    }

    /**
     * Get the telephone number.
     *
     * @return -- the telephone number.
     */
    public String getPhoneNumber() {
        return this.telephoneNumber.getPhoneNumber();
    }

    /**
     * Return the string encoding.
     *
     * @return -- the string encoding.
     */
    public String toString() {
        return this.scheme + ":" + telephoneNumber.encode();
    }

    /**
     * Encodes contents in a string.
     * @return encoded string of object contents
     */
    public String encode() {
        return this.scheme + ":" + telephoneNumber.encode();
    }

    /**
     * URI part of the address without parameters.
     * @return URI part of the address
     */
    public String getPlainURI() {
        StringBuffer retval = new StringBuffer();
        retval.append(scheme).append(":");
        if (telephoneNumber.isGlobal()) {
            retval.append("+");
        }
        retval.append(telephoneNumber.getPhoneNumber());
        return retval.toString();
    }

    /**
     * Returns an Iterator over the names (Strings) of all parameters present
     * in this ParametersHeader.
     * @return an Iterator over all the parameter names
     *
     */
    public Vector getParameterNames() {
        if (null != telephoneNumber) {
            return telephoneNumber.parms.getNames();
        }
        return null;
    }

    /**
     * Deep copy clone operation.
     *
     * @return -- a cloned version of this telephone number.
     */
    public Object clone() {
        TelURL retval = new TelURL();
        retval.scheme = this.scheme;
        if (this.telephoneNumber != null) {
            retval.telephoneNumber =
                    (TelephoneNumber)this.telephoneNumber.clone();
        }
        return retval;
    }


}
