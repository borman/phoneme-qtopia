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

/**
 * Authority part of a URI structure. Section 3.2.2 RFC2396
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Authority extends GenericObject {

    /**
     * Host and port field.
     */
    protected HostPort hostPort;

    /**
     * User information field.
     */
    protected UserInfo userInfo;

    /**
     * Returns the host name in encoded form.
     * @return encoded string (does the same thing as toString)
     */
    public String encode() {
        StringBuffer retval = new StringBuffer();

        if (userInfo != null) {
            String user = userInfo.encode();
            if (user != null) {
                retval.append(user).append(Separators.AT);
            }
        }

        if (hostPort != null) {
            retval.append(hostPort.encode());
        }

        return retval.toString();
    }

    /**
     * Returns true if the two Objects are equals , false otherwise.
     * @param other Object to test.
     * @return true if objects match
     */
    public boolean equals(Object other) {
        if (!other.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }
        Authority otherAuth = (Authority) other;
        if (! this.hostPort.equals(otherAuth.hostPort)) {
            return false;
        }
        if (this.userInfo != null && otherAuth.userInfo != null) {
            if (! this.userInfo.equals(otherAuth.userInfo)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the host and port member.
     * @return the host and port
     */
    public HostPort getHostPort() {
        return hostPort;
    }

    /**
     * Gets the user information memnber.
     * @return the user information
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Gets the  password from the user informatio.
     * @return the password
     */
    public String getPassword() {
        if (userInfo == null) {
            return null;
        } else {
            return userInfo.getPassword();
        }
    }

    /**
     * Gets the user name if it exists.
     * @return  user or null if not set.
     */
    public String getUser() {
        return (userInfo != null) ? userInfo.getUser() : null;
    }

    /**
     * Gets the host name.
     * @return Host (null if not set)
     */
    public Host getHost() {
        if (hostPort == null)
            return null;
        else
            return hostPort.getHost();
    }

    /**
     * Gets the port.
     * @return  port (-1) if port is not set.
     */
    public int getPort() {
        if (hostPort == null)
            return -1;
        else
            return hostPort.getPort();
    }

    /**
     * Removes the port.
     */
    public void removePort() {
        if (hostPort != null) {
            hostPort.removePort();
        }
    }

    /**
     * Sets the password.
     * @param passwd String to set
     * @throws IllegalArgumentException if password contains invalid
     * characters
     */
    public void setPassword(String passwd) throws IllegalArgumentException {
        if (userInfo == null) userInfo = new UserInfo();
        userInfo.setPassword(passwd);
    }

    /**
     * Sets the user name of the user information member.
     * @param user String to set
     * @throws IllegalArgumentException if user name contains invalid
     * characters
     */
    public void setUser(String user)  throws IllegalArgumentException {
        if (userInfo == null) {
            userInfo = new UserInfo();
        }

        userInfo.setUser(user);
    }

    /**
     * Sets the host.
     * @param host Host to set
     */
    public void setHost(Host host) {
        if (hostPort == null) {
            hostPort = new HostPort();
        }

        hostPort.setHost(host);
    }

    /**
     * Sets the port.
     * @param port int to set
     */
    public void setPort(int port) {
        if (hostPort == null) {
            hostPort = new HostPort();
        }
        hostPort.setPort(port);
    }

    /**
     * Sets the host and port member.
     * @param h HostPort to set
     */
    public void setHostPort(HostPort h) {
        hostPort = h;
    }

    /**
     * Sets the user information member.
     * @param u UserInfo to set
     */
    public void setUserInfo(UserInfo u) {
        userInfo = u;
    }

    /**
     * Removes the user information.
     */
    public void removeUserInfo() {
        userInfo = null;
    }

    /**
     * Copies the object contents
     * @return the copy of this object
     */
    public Object clone() {
        Authority retval = new Authority();

        try {
          retval.setUser(getUser());
          retval.setPassword(getPassword());
        } catch (IllegalArgumentException e) {
          // intentionally ignored
          // it shoild be impossible to get here due to getUser()
          // and getPassword() return verified values
        }
        retval.setHostPort(getHostPort());

        return retval;
    }

}
