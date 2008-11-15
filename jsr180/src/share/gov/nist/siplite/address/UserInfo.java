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

import gov.nist.core.*;
import gov.nist.siplite.parser.*;

/**
 * User information part of a URL.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class UserInfo {

    /**
     *  user field
     */
    private String user;

    /**
     *  password field
     */
    private String password;

    /**
     *  userType field
     */
    protected int userType;

    /**
     *  Constant field
     */
    public final static int TELEPHONE_SUBSCRIBER = 1;

    /**
     *  constant field
     */
    public final static int USER = 2;

    /**
     *  Default constructor
     */
    public UserInfo() {
        super();
    }

    /**
     * Compare for equality.
     * @param obj Object to set
     * @return true if the two headers are equals, false otherwise.
     */
    public boolean equals(Object obj) {
        if (!getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        UserInfo other = (UserInfo) obj;
        if (this.userType != other.userType) {
            return false;
        }
        String u1 = this.user;
        String u2 = other.user;
        if (u1 == null) u1 = "";
        if (u2 == null) u2 = "";
        if (!u1.toLowerCase().equals
            (u2.toLowerCase())) {
            return false;
        }
        u1 = this.password;
        u2 = other.password;
        if (u1 == null) u1 = "";
        if (u2 == null) u2 = "";
        if (!u1.equals(u2)) {
            return false;
        }
        return (true);
    }

    /**
     * Encode the user information as a string.
     * @return String
     */
    public String encode() {
        if (password != null) {
            return new StringBuffer().
                    append(user).append(Separators.COLON).
                    append(password).toString();
        } else {
            return user;
        }
    }

    /**
     *  Clear the password field.
     */
    public void clearPassword() {
        this.password = null;
    }

    /**
     * Gets the user type (which can be set to TELEPHONE_SUBSCRIBER or USER).
     * @return the type of user.
     */
    public int getUserType() {
        return userType;
    }

    /**
     * Get the user field.
     * @return String
     */
    public String getUser() {
        return user;
    }

    /**
     * Get the password field.
     * @return String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the user member.
     * @param user String to set
     * @throws IllegalArgumentException if user name contains invalid
     * characters
     */
    public void setUser(String user) throws IllegalArgumentException {
        if (false == Lexer.isValidUserName(user)) {
            throw new IllegalArgumentException("User name '" + user +
                                               "' contains " +
                                               "illegal characters");
        }
        
        if ("".equals(user)) {
            // JSR180: Empty string or null "" removes the user part.
            this.user = null;
        } else {
            this.user = user;
        }
        
        // add this (taken form sip_messageParser)
        // otherwise comparison of two SipUrl will fail because this
        // parameter is not set (whereas it is set in sip_messageParser).
        if (user != null && (user.indexOf(Separators.POUND) >= 0 ||
                             user.indexOf(Separators.SEMICOLON) >= 0)) {
            setUserType(UserInfo.TELEPHONE_SUBSCRIBER);
        } else {
            setUserType(UserInfo.USER);
        }
    }

    /**
     * Set the password member.
     * @param p String to set
     * @throws IllegalArgumentException if user name contains invalid
     * characters
     */
    public void setPassword(String p) throws IllegalArgumentException {
        // IMPL_NOTE: check for the password validity
        password = p;
    }

    /**
     * Set the user type (to TELEPHONE_SUBSCRIBER or USER).
     * @param type int to set
     * @throws IllegalArgumentException if type is not in range.
     */
    public void setUserType(int type) throws IllegalArgumentException {
        if (type != TELEPHONE_SUBSCRIBER && type != USER) {
            throw new IllegalArgumentException
                    ("Parameter not in range");
        }
        userType = type;
    }

    /**
     * Copies the current instance.
     * @return a copy of the current instance
     */
    public Object clone() {
        UserInfo retval = new UserInfo();
        try {
            retval.setUser(user);
            retval.setPassword(password);
        } catch (IllegalArgumentException e) {
            // intentionally ignored
            // it shoild be impossible to get here due to this.user
            // and this.password are verified values
        }

        return retval;
    }

}
