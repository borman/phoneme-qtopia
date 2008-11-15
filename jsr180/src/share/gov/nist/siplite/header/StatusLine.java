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
import gov.nist.core.*;
import gov.nist.siplite.*;

/**
 * Status Line (for SIPReply) messages.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class StatusLine extends GenericObject {
    /**
     * SipVersion field.
     */
    protected String sipVersion;
    
    /**
     * Status code field.
     */
    protected int statusCode;
    
    /**
     * Reason phrase field.
     */
    protected String reasonPhrase;
    
    /**
     * Default Constructor.
     */
    public StatusLine() {
        reasonPhrase = null;
        sipVersion = SIPConstants.SIP_VERSION_STRING;
    }
    
    /** Class handle. */
    public static Class clazz;
    
    static {
        clazz = new StatusLine().getClass();
    }
    
    /**
     * Encodes into a canonical form.
     * @return String
     */
    public String encode() {
        String encoding = SIPConstants.SIP_VERSION_STRING + Separators.SP +
                statusCode;
        if (reasonPhrase != null) encoding += Separators.SP + reasonPhrase;
        encoding += Separators.NEWLINE;
        return encoding;
    }
    
    /**
     * Gets the Sip Version.
     * @return SipVersion
     */
    public String getSipVersion() {
        return sipVersion;
    }
    
    /**
     * Gets the Status Code.
     * @return StatusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * Gets the ReasonPhrase field.
     * @return ReasonPhrase field
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    /**
     * Sets the sipVersion member.
     * @param s String to set
     */
    public void setSipVersion(String s) {
        sipVersion = s;
    }
    
    /**
     * Sets the statusCode member.
     * @param statusCode int to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    /**
     * Set the reasonPhrase member.
     * @param reasonPhrase String to set
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }
    
    /**
     * Gets the major version number.
     * @return String major version number
     */
    public String getVersionMajor() {
        if (sipVersion == null)
            return null;
        String major = null;
        boolean slash = false;
        for (int i = 0; i < sipVersion.length(); i++) {
            if (sipVersion.charAt(i) == '.') slash = false;
            if (slash) {
                if (major == null)
                    major = "" + sipVersion.charAt(i);
                else major += sipVersion.charAt(i);
            }
            if (sipVersion.charAt(i) == '/') slash = true;
        }
        return major;
    }
    
    /**
     * Gets the minor version number.
     * @return String minor version number
     */
    public String getVersionMinor() {
        if (sipVersion == null)
            return null;
        String minor = null;
        boolean dot = false;
        for (int i = 0; i < sipVersion.length(); i++) {
            if (dot) {
                if (minor == null)
                    minor = "" + sipVersion.charAt(i);
                else minor += sipVersion.charAt(i);
            }
            if (sipVersion.charAt(i) == '.') dot = true;
        }
        return minor;
    }
    
    /**
     * Copies the current instance.
     * @return copy of the current objectt
     */
    public Object clone() {
        StatusLine retval = new StatusLine();
        
        if (this.sipVersion != null)
            retval.sipVersion = new String(this.sipVersion);
        
        retval.statusCode = this.statusCode;
        
        if (this.reasonPhrase != null)
            retval.reasonPhrase = new String(this.reasonPhrase);
        
        return retval;
        
    }
    
    /**
     * Compares this instance to the requested object.
     * @param that object for comparison
     * @return true if the object matches.
     */
    public boolean equals(Object that) {
        if (that instanceof StatusLine)
            return this.statusCode == ((StatusLine)that).statusCode;
        else return false;
    }
    
    
    
}
