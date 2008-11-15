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
import gov.nist.siplite.address.*;

/**
 * RequestLine of SIP Request.
 *
 */
public class RequestLine extends GenericObject {
    
    /**
     * Uri field. Note that this can be a SIP URI or a generic URI
     * like tel URI.
     */
    protected URI uri;
    
    /**
     * Method field.
     */
    protected String method;
    
    /**
     * SipVersion field
     */
    protected String sipVersion;
    
    /** Class handle. */
    public static Class clazz;
    
    
    static {
        clazz = new RequestLine().getClass();
    }
    
    /**
     * Default constructor.
     */
    public RequestLine() {
        sipVersion = "SIP/2.0";
    }
    
    
    /**
     * Set the SIP version.
     * @param sipVersion -- the SIP version to set.
     */
    public void setSIPVersion(String sipVersion) {
        this.sipVersion = sipVersion;
    }
    
    /**
     * Encodes the request line as a String.
     *
     * @return requestLine encoded as a string.
     */
    public String encode() {
        StringBuffer encoding = new StringBuffer();
        if (method != null) {
            encoding.append(method);
            encoding.append(Separators.SP);
        }
        if (uri != null) {
            encoding.append(uri.encode());
            encoding.append(Separators.SP);
        }
        encoding .append(sipVersion + Separators.NEWLINE);
        return encoding.toString();
    }
    
    /**
     * Gets the Request-URI.
     * @return the request URI
     */
    public URI getUri() {
        return uri;
    }
    
    /**
     * Constructor given the request URI and the method.
     * @param requestURI the request URI
     * @param method the operation to perform
     */
    public RequestLine(URI requestURI, String method) {
        this.uri = requestURI;
        this.method = method;
        this.sipVersion = "SIP/2.0";
    }
    
    /**
     * Get the Method
     *
     * @return method string.
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Get the SIP version.
     *
     * @return String
     */
    public String getSipVersion() {
        return sipVersion;
    }
    
    /**
     * Set the uri member.
     * @param uri URI to set.
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }
    
    /**
     * Set the method member
     *
     * @param method String to set
     */
    public void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * Set the sipVersion member
     *
     * @param s String to set
     */
    public void setSipVersion(String s) {
        sipVersion = s;
    }
    
    /**
     * Get the major verrsion number.
     *
     * @return String major version number
     */
    public String getVersionMajor() {
        if (sipVersion == null)
            return null;
        String major = null;
        boolean slash = false;
        for (int i = 0; i < sipVersion.length(); i++) {
            if (sipVersion.charAt(i) == '.') break;
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
     * Get the minor version number.
     *
     * @return String minor version number
     *
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
     * Compare for equality.
     *
     * @param other object to compare with. We assume that all fields
     * are set.
     * @return true if the object matches
     */
    public boolean equals(Object other) {
        boolean retval;
        if (! other.getClass().equals(this.getClass())) {
            return false;
        }
        RequestLine that = (RequestLine) other;
        try {
            retval = this.method.equals(that.method)
            && this.uri.equals(that.uri)
            && this.sipVersion.equals(that.sipVersion);
        } catch (NullPointerException ex) {
            retval = false;
        }
        return retval;
        
        
    }
    
    /**
     * Clone this request.
     * @return copt of the current object
     */
    public Object clone() {
        RequestLine retval = new RequestLine();
        if (this.uri != null)
            retval.uri = (URI) this.uri.clone();
        if (this.method != null)
            retval.method = new String(this.method);
        if (this.sipVersion != null)
            retval.sipVersion = new String(this.sipVersion);
        return (Object) retval;
    }
    
    
}
