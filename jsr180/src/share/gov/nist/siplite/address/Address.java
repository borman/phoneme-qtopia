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
package gov.nist.siplite.address;

import java.util.Vector;

import com.sun.j2me.log.LogChannels;
import com.sun.j2me.log.Logging;

import gov.nist.core.*;
import gov.nist.microedition.sip.StackConnector;
import gov.nist.siplite.parser.*;

/**
 * Address structure. Imbeds a URI and adds a display name.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1
 *
 */
public class Address extends GenericObject {

    /** Constant field. */
    public static final int NAME_ADDR = 1;

    /** Constant field. */
    public static final int ADDRESS_SPEC = 2;

    /** Constant field. */
    public static final int WILD_CARD = 3;

    /** Address type. */
    protected int addressType;

    /** Display name field. */
    protected String displayName;

    /** Address field. */
    protected URI address;
    
    /** Exception message for immutable address */
    private final String exceptionMessage = 
            "The address represents the immutable \"*\" value";
            
    /**
     * Gets exception message for immutable address.
     * @return exception message for immutable address.
     */
    public String getEceptionMessage() {
        return exceptionMessage;
    }
    
    /**
     * Gets the host port portion of the address spec.
     * @return host:port in a HostPort structure.
     */
    public HostPort getHostPort() {
        // if the address is wildcard ("*"), all properties are null
        if (this.addressType == WILD_CARD) {
            return null;
        }
        if (address.isSipURI()) {
            return ((SipURI) address).getHostPort();
        }
        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "Wrong URI HostPort request");
        }
        return null;
    }

    /**
     * Gets the port from the imbedded URI. This assumes that a SIP URL
     * is encapsulated in this address object.
     *
     * @return the port from the address.
     *
     */
    public int getPort() {
        // if the address is wildcard ("*"), return 0
        if (this.addressType == WILD_CARD) {
            return 0;
        }
        if (address.isSipURI()) {
            SipURI uri = (SipURI) address;
            int port = uri.getPort();
            if (port < 0) {
                port = uri.getDefaultPort();
            }
            return port;
        }
        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "getPort() for wrong URI");
        }
        // if the port is not set, return the default (5060)
        return 5060;
    }

    /**
     * Gets the user@host:port for the address field. This assumes
     * that the encapsulated object is a SipURI.
     *
     * @return string containing user@host:port.
     */
    public String getUserAtHostPort() {
        // if the address is wildcard ("*"), all properties are null
        if (addressType == WILD_CARD) {
            return null;
        }

        if (address.isSipURI()) {
            SipURI uri = (SipURI) address;
            return uri.getUserAtHostPort();
        }
        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "getUserAtHostPort() for wrong URI");
        }
        return address.toString();

    }

    /**
     * Gets the host name from the address.
     *
     * @return the host name.
     */
    public String getHost() {
        // if the address is wildcard ("*"), all properties are null
        if (this.addressType == WILD_CARD) {
            return null;
        }
        if (address.isSipURI()) {
            // IMPL_NOTE: why not SipURI.getHost()?
            return ((SipURI) address).getHostPort().getHost().getHostname();
        }
        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "getHost() for wrong URI");
        }
        return null;
    }

    /**
     * Sets the host part of the SIP address.
     * @param host  host part
     * @throws IllegalArgumentException if the host part is formated wrong way
     */
    public void setHost(String host) throws IllegalArgumentException {
        if (host == null) {
            throw new NullPointerException("Host is null");
        }
                
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        
        if (address.isSipURI()) {
            ((SipURI)address).setHost(host);
        } else
            ((TelURL)address).setPostDial(host);
    }

    /**
     * Removes a parameter from the address.
     *
     * @param parameterName is the name of the parameter to remove.
     */
    public void removeParameter(String parameterName) {
        // if the address is wildcard ("*"), all properties are null
        if (this.addressType == WILD_CARD) {
            return;
        }
        if (address.isSipURI()) {
            ((SipURI) address).removeParameter(parameterName);
        }
    }


    /**
     * Encodes the address as a string and return it.
     * @return String canonical encoded version of this address.
     */
    public String encode() {
        if (this.addressType == WILD_CARD) {
            return "*";
        }

        StringBuffer encoding = new StringBuffer();

        if (displayName != null) {
            // Now the quotes are added to the displayName in AddressParser
            // if they presented in the original header
            encoding.append(displayName).append(Separators.SP);
        }

        if (address != null) {
            if (addressType == NAME_ADDR || displayName != null) {
                encoding.append(Separators.LESS_THAN);
            }
            encoding.append(address.encode());
            if (addressType == NAME_ADDR || displayName != null) {
                encoding.append(Separators.GREATER_THAN);
            }
        }

        return encoding.toString();
    }

    /**
     * Creates a string representation of the address object.
     * @return String canonical encoded version of this address.
     */
    public String toString() {
        return encode();
    }

    /**
     * Default constructor.
     */
    public Address() {
        this.addressType = NAME_ADDR;
    }

    /**
     * Gets the address type.
     * @return the addres type
     */
    public int getAddressType() {
        return addressType;
    }

    /**
     * Sets the address type. The address can be NAME_ADDR, ADDR_SPEC or
     * WILD_CARD.
     *
     * @param atype int to set
     *
     */
    public void setAddressType(int atype) {
        addressType = atype;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name member.
     *
     * @param dn String to set
     * @throws IllegalArgumentException if the name contains invalid
     * characters
     */
    public void setDisplayName(String dn) throws
            IllegalArgumentException {
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }

        if ((null == dn) || (dn.equals(""))) {
            // JSR180: Empty string or null removes the display name.
            //         getDisplayName() returns null if
            //         the display name is not available.
            displayName = null;
        } else {
            if (false == Lexer.isValidDisplayName(dn)) {
                throw new
                        IllegalArgumentException("Invalid display name " +
                                                 dn);
            }
            displayName = dn;
            addressType = NAME_ADDR;
        }
    }


    /**
     * Sets the address field.
     *
     * @param addr SipURI to set
     *
     */
    public void setURI(URI addr) {
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        
        address = (URI) addr;
    }

    /**
     * Parses and creates the URI field.
     *
     * @param URI SipURI to set
     *
     */
    public void setURI(String URI) {
        if (URI == null) {
            throw new NullPointerException("URI is null");
        }
        
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        
        URI uri = null;
        try {
            uri = StackConnector.addressFactory.createURI(URI);
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe.getMessage());
        }

        if (uri == null)
            throw new IllegalArgumentException("The URI is invalid");

        if (uri.isSipURI()) {
            ((SipURI)uri).clearUriParms();

            // copy parameters value from old uri
            if (address.isSipURI()) {
                SipURI addr = (SipURI)address;
                Vector names = addr.getParameterNames();
                String value;
                String name;
                for (int i = 0; i < names.size(); i++) {
                    name = (String)names.elementAt(i);
                    value = addr.getParameter(name);
                    try {
                        ((SipURI)uri).setParameter(name, value);
                    } catch (ParseException ex) {
                        // intentionally ignored
                        // parameters were parsed already
                    }
                }

            }
        }

        setURI(uri);
    }

    /**
     * Compares two address specs for equality.
     *
     * @param other Object to compare this this address
     *
     * @return true if the objects match
     *
     */
    public boolean equals(Object other) {
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }

        Address that = (Address) other;
        if (this.addressType == WILD_CARD &&
            that.addressType != WILD_CARD) {
            return false;
        }

        // Ignore the display name; only compare the address spec.
        boolean retval = this.address.equals(that.address);
        return retval;
    }

    /**
     * return true if DisplayName exist.
     *
     * @return boolean
     */
    public boolean hasDisplayName() {
        return (displayName != null);
    }

    /**
     * remove the displayName field
     */
    public void removeDisplayName() {
        displayName = null;
    }

    /**
     * Return true if the embedded URI is a sip URI.
     *
     * @return true if the embedded URI is a SIP URI.
     *
     */
    public boolean isSIPAddress() {
        return address.isSipURI();
    }

    /**
     * Returns the URI address of this Address. The type of URI can be
     * determined by the scheme.
     *
     * @return address parmater of the Address object
     */
    public URI getURI() {
        return this.address;
    }

    /**
     * URI part of the address without parameters.
     * @return URI part of the address
     */
    public String getPlainURI() {
        if (addressType == WILD_CARD) {
            return "*";
        }
        
        return address.getPlainURI();
    }

    /**
     * This determines if this address is a wildcard address. That is
     * <code>Address.getAddress.getUserInfo() == *;</code>
     *
     * @return true if this name address is a wildcard, false otherwise.
     */
    public boolean isWildcard() {
        return this.addressType == WILD_CARD;
    }

    /**
     * Sets the user part of the embedded URI.
     *
     * @param user user name to set for the embedded URI.
     * @throws IllegalArgumentException if the user part contains invalid
     * characters
     */
    public void setUser(String user) throws IllegalArgumentException {
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }

        if (address.isSipURI()) {
            SipURI uri = (SipURI) address;
            int colonPos;
            if (null != user && 
                (colonPos = user.indexOf(Separators.COLON)) != -1) {
                uri.setUser(user.substring(0, colonPos));
                uri.setUserPassword(user.substring(colonPos + 1));
            } else {
                uri.setUser(user);
                uri.clearPassword();
            }
        } else if (address.isTelURL()) {
            // IMPL_NOTE : check if the tel number is valid
            ((TelURL) address).setPhoneNumber(user);
        } else {
            throw new IllegalArgumentException("Can't set a user " +
                                               "for this type of address");
        }
    }

    /**
     * Returns the user part of SIP address.
     * @return user part of SIP address. Returns null if the
     * user part is missing or URI has unknown type
     */
    public String getUser() {
        // RFC3261 p.222
        // SIP-URI   =  "sip:" [ userinfo ] hostport
        //             uri-parameters [ headers ]
        // SIPS-URI  =  "sips:" [ userinfo ] hostport
        //             uri-parameters [ headers ]
        // userinfo =  ( user / telephone-subscriber ) [ ":" password ] "@"
        //
        // This function returns user name/telephon number + password if it is
        // existing
        //
        // if the address is wildcard ("*"), all properties are null

        if (addressType == WILD_CARD) {
            return null;
        }
        if (address.isSipURI()) {
            SipURI uri = (SipURI) address;
            String psswd = uri.getUserPassword();
            if (null != psswd) {
                return new String(uri.getUser() +
                                  Separators.COLON +
                                  psswd);
            } else {
                return uri.getUser();
            }
        } else if (address.isTelURL()) {
            // IMPL_NOTE: Do we have TelURL only?
            return ((TelURL) address).getPhoneNumber();
        } else {
            return null;
        }
    }

    /**
     * Returns a String array of all parameter names.
     * @return String array of parameter names. Returns null if
     * the address does not have any parameters.
     */
    public String[] getParameterNames() {
        if (addressType == WILD_CARD) {
            return null;
        }
        Vector parameterNameList;
        if (address.isSipURI()) {
            parameterNameList = ((SipURI)address).getParameterNames();

        } else if (address.isTelURL()) {
            parameterNameList = ((TelURL)address).getParameterNames();
        } else {
            return null;
        }
        if (null == parameterNameList || parameterNameList.size() == 0) {
            return null;
        }

        String parameterNames[] = new String[parameterNameList.size()];

        for (int i = 0; i < parameterNameList.size(); i++)
            parameterNames[i] = (String)parameterNameList.elementAt(i);

        return parameterNames;
    }

    /**
     * Returns the scheme of SIP address.
     * @return the scheme of this SIP address e.g. sip or sips
     */
    public java.lang.String getScheme() {
        if (addressType == WILD_CARD) {
            return null;
        }
        return address.getScheme();
    }

    /**
     * Sets the named URI parameter to the specified value. If the
     * value is null
     * the parameter is interpreted as a parameter without value.
     * Existing parameter will be overwritten, otherwise the parameter
     * is added.
     * @param name - the named URI parameter
     * @param value - the value
     * @throws IllegalArgumentException - if the parameter is
     *  invalid RFC 3261,
     * chapter 19.1.1 SIP and SIPS URI Components "URI parameters" p.149
     */
    public void setParameter(java.lang.String name, java.lang.String value)
            throws IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("Name of parameter is null");
        }
                
        if (addressType == WILD_CARD) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        
        // Validating the name.
        if (!Lexer.isValidName(name)) {
            throw new IllegalArgumentException("Invalid parameter's name.");
        }

        // Validating the value.
        if (!Lexer.isValidParameterValue(value)) {
            throw new IllegalArgumentException("Invalid parameter's value.");
        }

        URI uri = getURI();
        if (uri.isSipURI()) {
            try {
                ((SipURI)uri).setParameter(name, value);
            } catch (ParseException pe) {
                throw new IllegalArgumentException(pe.getMessage());
            }
        }
        // IMPL_NOTE : do something for the tel URL
    }

    /**
     * Returns the value associated with the named URI parameter.
     * @param name - the name of the parameter
     * @return the value of the parameter, or empty string (no value).
     * without value and null if the parameter is not defined
     */
    public java.lang.String getParameter(java.lang.String name) {
        if (name == null) {
            throw new NullPointerException("Name of parameter is null");
        }
                
        if (addressType == WILD_CARD) {
            return null;
        }
        
        URI uri = getURI();
        if (uri.isSipURI()) {
            return ((SipURI)uri).getParameter(name);
        }
        // IMPL_NOTE : return something for the tel URL
        return null;
    }


    /**
     * Clone this structure.
     * @return Object Address
     */
    public Object clone() {
        Address retval = new Address();
        retval.addressType = this.addressType;
        if (displayName != null) {
            retval.displayName = new String(displayName);
        }
        if (address != null) {
            retval.address = (URI) address.clone();
        }
        return (Object) retval;

    }

}
