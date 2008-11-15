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
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;

/**
 * Email address record.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class Email extends SDPObject {
    /** User name. */
    protected String userName;
    /** Host name. */
    protected String hostName;

    /**
     * Gets the user name.
     * @return the user name
     */
    public String getUserName() {
	return userName;
    }

    /** 
     * Gets the host name.
     * @return host name
     */
    public String getHostName() {
	return hostName;
    }

    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	Email retval = new Email();
	retval.userName = userName;
	retval.hostName = hostName;
	return retval;
    }
 
    /**
     * Sets the user name member.
     * @param u the new user name
     */
    public void setUserName(String u) {
	userName = u;
    } 

    /**
     * Sets the host name member.
     * @param h the new host name
     */
    public void setHostName(String h) {
	hostName = h.trim();
    } 

    /**
     * Gest the string encoded version of this object.
     * @return the encode string of this objects contents
     * @since v1.0
     */
    public String encode() {
	return userName + Separators.AT + hostName;
    }
 

}
