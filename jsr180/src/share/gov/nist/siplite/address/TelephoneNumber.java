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

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

/**
 * Telephone number class.
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TelephoneNumber extends GenericObject {
    /** Post dial method string. */
    public static final String POSTDIAL = SIPConstants.GENERAL_POSTDIAL;
    /** Phone context tag string. */
    public static final String PHONE_CONTEXT_TAG =
            SIPConstants.GENERAL_PHONE_CONTEXT_TAG;
    /** ISDN subaddress parameter label. */
    public static final String ISUB = SIPConstants.GENERAL_ISUB;
    /** Provider tag label. */
    public static final String PROVIDER_TAG = SIPConstants.GENERAL_PROVIDER_TAG;

    /**
     * Flag indicating international phone number.
     */
    protected boolean isglobal;

    /**
     * Phone number field.
     */
    protected String phoneNumber;

    /**
     * Parmeters list.
     */
    protected NameValueList parms;

    /**
     * Creates new TelephoneNumber.
     */
    public TelephoneNumber() {
        parms = new NameValueList("telparms");
    }

    /**
     * Deletes the specified parameter.
     * @param name String to set
     */
    public void deleteParm(String name) {
        parms.delete(name);
    }

    /**
     * Gets the PhoneNumber field.
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Gets the PostDial field.
     * @return String
     */
    public String getPostDial() {
        return (String) parms.getValue(POSTDIAL);
    }

    /**
     * Get the isdn subaddress for this number.
     * @return String
     */
    public String getIsdnSubaddress() {
        return (String) parms.getValue(ISUB);
    }

    /**
     * Returns true if the PostDial field exists.
     * @return true if post dial field is included
     */
    public boolean hasPostDial() {
        return parms.getValue(POSTDIAL) != null;
    }

    /**
     * Returns true if this header has parameters.
     * @param pname String to set
     * @return true if parameter is present
     */
    public boolean hasParm(String pname) {
        return parms.hasNameValue(pname);
    }

    /**
     * Returns true if the isdn subaddress exists.
     * @return True if isdn sub address exists.
     */
    public boolean hasIsdnSubaddress() {
        return hasParm(ISUB);
    }

    /**
     * Returns tru if telephone number is a global telephone number.
     * @return true if global phone number
     */
    public boolean isGlobal() {
        return isglobal;
    }

    /**
     * Removes the PostDial field.
     */
    public void removePostDial() {
        parms.delete(POSTDIAL);
    }

    /**
     * Removes the isdn subaddress (if it exists).
     */
    public void removeIsdnSubaddress() {
        deleteParm(ISUB);
    }

    /**
     * Sets the list of parameters.
     * @param p NameValueList to set
     */
    public void setParameters(NameValueList p) {
        parms = p;
    }

    /**
     * Sets the Global field.
     * @param g boolean to set
     */
    public void setGlobal(boolean g) {
        isglobal = g;
    }

    /**
     * Sets the PostDial field.
     * @param p String to set
     */
    public void setPostDial(String p) {
        NameValue nv = new NameValue(POSTDIAL, p);
        parms.add(nv);
    }

    /**
     * Sets the specified parameter.
     * @param name String to set
     * @param value Object to set
     */
    public void setParm(String name, Object value) {
        NameValue nv = new NameValue(name, value);
        parms.add(nv);
    }

    /**
     * Sets the isdn subaddress for this structure.
     * @param isub String to set
     */
    public void setIsdnSubaddress(String isub) {
        setParm(ISUB, isub);
    }

    /**
     * Sets the PhoneNumber field
     * @param num String to set
     */
    public void setPhoneNumber(String num) {
        phoneNumber = num;
    }

    /**
     *( Encodes instance contents as a string.
     * @return encoded string of object contents
     */
    public String encode() {
        String retval = "";
        if (isglobal) retval += "+";
        retval += phoneNumber;
        if (! parms.isEmpty()) {
            retval += Separators.SEMICOLON;
            retval += parms.encode();
        }
        return retval;
    }

    /**
     * Copies the current object.
     * @return copy of current instance
     */
    public Object clone() {
        TelephoneNumber retval = new TelephoneNumber();
        retval.isglobal = this.isglobal;
        retval.phoneNumber = new String(this.phoneNumber);
        retval.parms = (NameValueList)this.parms.clone();
        return retval;
    }

}
