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
package gov.nist.siplite.message;

import java.util.Enumeration;
import java.io.UnsupportedEncodingException;
import javax.microedition.sip.SipException;

import gov.nist.siplite.address.*;
import gov.nist.core.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.SIPErrorCodes;

/**
 * SIP Response structure.
 *
 * @version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class Response extends Message {
    /** The current status line. */
    protected StatusLine statusLine;

    /**
     * Gets the reason phrase.
     * @param rc the reason code
     * @return the reason phrase as a string
     */
    public static String getReasonPhrase(int rc) {
        return SIPErrorCodes.getReasonPhrase(rc);
    }
    
    /**
     *  set the status code.
     * @param statusCode is the status code to set.
     * @throws IllegalArgumentException if invalid status code.
     */
    public void setStatusCode(int statusCode) throws ParseException {
        if (statusCode < 100 || statusCode > 800) {
            throw new ParseException("bad status code", 0);
        }

        if (statusLine == null) {
            statusLine = new StatusLine();
        }
        statusLine.setStatusCode(statusCode);
    }

    /**
     * Get the status line of the response.
     * @return StatusLine
     */
    public StatusLine getStatusLine() { return statusLine; }

    /**
     * Get the staus code (conveniance function).
     * @return the status code of the status line.
     */
    public int getStatusCode() {
        return statusLine.getStatusCode();
    }

    /**
     * Set the reason phrase.
     * @param reasonPhrase the reason phrase.
     * @throws IllegalArgumentException if null string
     */
    public void setReasonPhrase(String reasonPhrase)
    throws IllegalArgumentException {
        if (reasonPhrase == null)
            throw new IllegalArgumentException("Bad reason phrase");
        if (this.statusLine == null) this.statusLine = new StatusLine();
        this.statusLine.setReasonPhrase(reasonPhrase);
    }

    /**
     *  Get the reason phrase.
     * @return the reason phrase.
     */
    public String getReasonPhrase() {
        if (statusLine == null || statusLine.getReasonPhrase() == null)
            return "";
        else return statusLine.getReasonPhrase();
    }

    /**
     *  Return true if the response is a final response.
     * @param rc is the return code.
     * @return true if the parameter is between the range 200 and 700.
     */
    public static boolean isFinalResponse(int rc) {
        return rc >= 200 && rc < 700;
    }

    /**
     * Is this a final response?
     * @return true if this is a final response.
     */
    public boolean isFinalResponse() {
        return isFinalResponse(statusLine.getStatusCode());
    }

    /**
     * Set the status line field.
     * @param sl Status line to set.
     */
    public void setStatusLine(StatusLine sl) { statusLine = sl; }

    /**
     * Constructor.
     */
    public Response() { super(); }

    /**
     * Check the response structure. Must have from, to CSEQ and VIA
     * headers.
     */
    protected void checkHeaders() throws ParseException {
        if (getCSeqHeader() == null) {
            throw new ParseException
                    (Header.CSEQ, 0);
        }
        if (getTo() == null) {
            throw new ParseException
                    (Header.TO, 0);
        }
        if (getFromHeader() == null) {
            throw new ParseException
                    (Header.FROM, 0);
        }
        if (getViaHeaders() == null) {
            throw new ParseException
                    (Header.VIA, 0);
        }
    }

    /**
     * Encode the SIP Request as a string.
     * @return The string encoded canonical form of the message.
     */

    public String encode() {
        String retval;
        if (statusLine != null)
            retval = statusLine.encode() + super.encode();
        else retval = super.encode();
        return retval;
    }

    /**
     * Make a clone (deep copy) of this object.
     * @return a deep copy of this object.
     */

    public Object clone() {
        Response retval = (Response) super.clone();
        retval.statusLine = (StatusLine) this.statusLine.clone();
        return retval;
    }

    /**
     * Compare for equality.
     * @param other other object to compare with.
     * @return true if the objects match
     */
    public boolean equals(Object other) {
        if (! this.getClass().equals(other.getClass()))
            return false;
        Response that = (Response) other;
        return statusLine.equals(that.statusLine) &&
                super.equals(other);
    }

    /**
     * Encode this into a byte array.
     * This is used when the body has been set as a binary array
     * and you want to encode the body as a byte array for transmission.
     *
     * @return a byte array containing the Request encoded as a byte
     * array.
     */
    public byte[] encodeAsBytes() {
        byte[] slbytes = null;
        if (statusLine != null) {
            try {
                slbytes = statusLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes();
        byte[] retval = new byte[slbytes.length + superbytes.length];
        int i = 0;
        if (slbytes != null) {
            for (i = 0; i < slbytes.length; i++) {
                retval[i] = slbytes[i];
            }
        }

        for (int j = 0; j < superbytes.length; j++, i++) {
            retval[i] = superbytes[j];
        }
        return retval;
    }

    /**
     * Create a new Request from the given response. Note that the
     * RecordRoute Via and CSeqHeader headers are not copied from the response.
     * These have to be added by the caller.
     * This method is useful for generating ACK messages from final
     * responses.
     *
     * @param requestURI is the request URI to use.
     * @param via is the via header to use.
     * @param cseq is the cseq header to use in the generated
     * request.
     * @return a new request obect.
     * @throws SipException if the request can't be created.
     */
    public Request createRequest(URI requestURI,
            ViaHeader via, CSeqHeader cseq) throws SipException {
        Request newRequest = new Request();
        String method = cseq.getMethod();

        newRequest.setMethod(method);
        newRequest.setRequestURI(requestURI);

        if ((equalsIgnoreCase(method, Request.ACK) ||
                equalsIgnoreCase(method, Request.CANCEL)) &&
                this.getTopmostVia().getBranch() != null) {
            // Use the
            via.setBranch(this.getTopmostVia().getBranch());
        }

        newRequest.setHeader(via);
        newRequest.setHeader(cseq);

        Enumeration headerIterator = getHeaders();
        while (headerIterator.hasMoreElements()) {
            Header nextHeader = (Header)headerIterator.nextElement();
            // Some headers do not belong in a Request ....
            if (Message.isResponseHeader(nextHeader) ||
                    nextHeader instanceof ViaList ||
                    nextHeader instanceof CSeqHeader ||
                    nextHeader instanceof ContentTypeHeader ||
                    nextHeader instanceof RecordRouteList) {
                continue;
            }
            if (nextHeader instanceof ToHeader)
                nextHeader = (Header)nextHeader.clone();
            else if (nextHeader instanceof FromHeader)
                nextHeader = (Header)nextHeader.clone();

            newRequest.attachHeader(nextHeader, false);
        }

        return newRequest;
    }

    /**
     * Get the encoded first line.
     *
     * @return the status line encoded.
     *
     */
    public String getFirstLine() {
        if (this.statusLine == null)
            return null;
        else return this.statusLine.encode();
    }

    /**
     * Sets the SIP version string.
     * @param sipVersion the new SIP version
     */
    public void setSIPVersion(String sipVersion) {
        this.statusLine.setSipVersion(sipVersion);
    }

    /**
     * Gets the SIP version string.
     * @return the SIP version string
     */
    public String getSIPVersion() {
        return this.statusLine.getSipVersion(); }

    /**
     * Encodes the object contents as a string
     * @return encoded string of object contents
     */
    public String toString() {
        return statusLine.encode() + super.encode();
    }

}
