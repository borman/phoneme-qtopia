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

import gov.nist.core.*;
import gov.nist.siplite.address.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.SIPConstants;
import java.util.*;
import java.io.UnsupportedEncodingException;
import javax.microedition.sip.SipException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;
import gov.nist.microedition.io.j2me.sip.DistributedRandom;

/**
 * The SIP Request structure-- this belongs to the parser who fills it up.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class Request extends Message {
    /** Acknowledgement request. */
    public static final String ACK = "ACK";

    /** End of session request. */
    public static final String BYE = "BYE";

    /** Terminate session request. */
    public static final String CANCEL = "CANCEL";

    /** Invitation request. */
    public static final String INVITE = "INVITE";

    /** Optional settings request. */
    public static final String OPTIONS = "OPTIONS";

    /** Regsitration request. */
    public static final String REGISTER = "REGISTER";

    /** Notification request. */
    public static final String NOTIFY = "NOTIFY";

    /** Subscription for notification request. */
    public static final String SUBSCRIBE = "SUBSCRIBE";

    /** Message request. */
    public static final String MESSAGE = "MESSAGE";

    /** Redirection request. */
    public static final String REFER = "REFER";

    /** Basic information request. */
    public static final String INFO = "INFO";

    /** PRACK ??? RFC. */
    public static final String PRACK = "PRACK";

    /** Update request. */
    public static final String UPDATE = "UPDATE";

    /** Publish request. */
    public static final String PUBLISH = "PUBLISH";

    /** Default user name is "ip". */
    public static final String DEFAULT_USER = "ip";

    /** Default time to live is 1 second. */
    public static final int DEFAULT_TTL = 1;

    /** Default transport is "udp". */
    public static final String DEFAULT_TRANSPORT = SIPConstants.TRANSPORT_UDP;

    /** Default method is to intiate an INVITE. */
    public static final String DEFAULT_METHOD = INVITE;

    /** Current transaction pointer. */
    private Object transactionPointer;

    /** Current requestline. */
    protected RequestLine requestLine;

    /** Current refresh request ID. */
    protected int refreshID;

    /**
     * Gets the Request Line of the Request.
     * @return the request line of the SIP Request.
     */
    public RequestLine getRequestLine() {
        return requestLine;
    }

    /**
     * Sets the request line of the SIP Request.
     * @param requestLine is the request line to set in the SIP Request.
     */
    public void setRequestLine(RequestLine requestLine) {
        this.requestLine = requestLine;
    }

    /**
     * Constructor.
     */
    public Request() { 
        super(); 
        refreshID = -1;
    }

    /**
     * Checks header for constraints.
     * <pre>
     * (1) Invite options and bye requests can only have SIP URIs in the
     * contact headers.
     * (2) Request must have cseq, to and from and via headers.
     * (3) Method in request URI must match that in CSEQ.
     * </pre>
     */
    protected void checkHeaders() throws ParseException {
        String prefix = "Missing Header ";

        /* Check for required headers */

        if (getCSeqHeader() == null) {
            throw new ParseException(prefix + Header.CSEQ, 0);
        }
        if (getTo() == null) {
            throw new ParseException(prefix + Header.TO, 0);
        }
        if (getFromHeader() == null) {
            throw new ParseException(prefix + Header.FROM, 0);
        }
        if (getViaHeaders() == null) {
            throw new ParseException(prefix + Header.VIA, 0);
        }

    /*
     * BUGBUG
     * Need to revisit this check later...
     * for now we just leave this to the
     * application to catch.
     */

        if (requestLine != null && requestLine.getMethod() != null &&
                getCSeqHeader().getMethod() != null &&
                compareToIgnoreCase
                (requestLine.getMethod(), getCSeqHeader().getMethod()) != 0) {
            throw
                    new ParseException
                    ("CSEQ method mismatch with Request-Line ", 0);

        }

    }

    /**
     * Sets the default values in the request URI if necessary.
     */
    protected void setDefaults() {
        // The request line may be unparseable (set to null by the
        // exception handler.
        if (requestLine == null)
            return;
        String method = requestLine.getMethod();
        // The requestLine may be malformed!
        if (method == null)
            return;
        URI u = requestLine.getUri();
        if (u == null)
            return;
        if (method.compareTo(REGISTER) == 0
                || method.compareTo(INVITE) == 0) {
            if (u instanceof SipURI) {
                SipURI sipUri = (SipURI) u;
                sipUri.setUserParam(DEFAULT_USER);
                try {
                    sipUri.setTransportParam(DEFAULT_TRANSPORT);
                } catch (ParseException ex) {}
            }
        }
    }

    /**
     * Patch up the request line as necessary.
     */
    protected void setRequestLineDefaults() {
        String method = requestLine.getMethod();
        if (method == null) {
            CSeqHeader cseq = (CSeqHeader) this.getCSeqHeader();
            if (cseq != null) {
                method = cseq.getMethod();
                requestLine.setMethod(method);
            }
        }
    }

    /**
     * A conveniance function to access the Request URI.
     * @return the requestURI if it exists.
     */
    public URI getRequestURI() {
        if (this.requestLine == null)
            return null;
        else
            return this.requestLine.getUri();
    }

    /**
     * Sets the RequestURI of Request. The Request-URI is a SIP or
     * SIPS URI or a general URI. It indicates the user or service to which
     * this request is being addressed. SIP elements MAY support
     * Request-URIs with schemes other than "sip" and "sips", for
     * example the "tel" URI scheme. SIP elements MAY translate
     * non-SIP URIs using any mechanism at their disposal, resulting
     * in SIP URI, SIPS URI, or some other scheme.
     *
     * @param uri the new Request URI of this request message
     */
    public void setRequestURI(URI uri) {
        if (this.requestLine == null) {
            this.requestLine = new RequestLine();
        }
        this.requestLine.setUri((URI)uri);
    }

    /**
     * Sets the method.
     * @param method is the method to set.
     * @throws IllegalArgumentException if the method is null
     */
    public void setMethod(String method) throws IllegalArgumentException {
        if (method == null)
            throw new IllegalArgumentException("null method");
        if (this.requestLine == null) {
            this.requestLine = new RequestLine();
        }
        this.requestLine.setMethod(method);
        if (this.cSeqHeader != null) {
            this.cSeqHeader.setMethod(method);
        }
    }

    /**
     * Gets the method from the request line.
     * @return the method from the request line if the method exits and
     * null if the request line or the method does not exist.
     */
    public String getMethod() {
        if (requestLine == null)
            return null;
        else
            return requestLine.getMethod();
    }

    /**
     * Encodes the SIP Request as a string.
     *
     * @return an encoded String containing the encoded SIP Message.
     */

    public String encode() {
        String retval;
        if (requestLine != null) {
            this.setRequestLineDefaults();
            retval = requestLine.encode() + super.encode();
        } else
            retval = super.encode();
        return retval;
    }

    /**
     * Alias for encode above.
     * @return encoded string of object contents
     */
    public String toString() { return this.encode(); }

    /**
     * Makes a clone (deep copy) of this object.
     * You can use this if you
     * want to modify a request while preserving the original
     *
     * @return a deep copy of this object.
     */

    public Object clone() {

        Request retval = (Request) super.clone();
        if (this.requestLine != null) {
            retval.requestLine = (RequestLine) this.requestLine.clone();
            retval.setRequestLineDefaults();
        }
        return retval;
    }

    /**
     * Compares for equality.
     *
     * @param other object to compare ourselves with.
     * @return true if objects match
     */
    public boolean equals(Object other) {
        if (! this.getClass().equals(other.getClass()))
            return false;
        Request that = (Request) other;

        boolean retval = requestLine.equals(that.requestLine) &&
                super.equals(other);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION && !retval) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "this ... >>>>" + encode());
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "other ... >>>>" + that.encode());
        }

        return retval;
    }

    /**
     * Encodes this into a byte array.
     * This is used when the body has been set as a binary array
     * and you want to encode the body as a byte array for transmission.
     *
     * @return a byte array containing the Request encoded as a byte
     * array.
     */
    public byte[] encodeAsBytes() {
        byte[] rlbytes = null;
        if (requestLine != null) {
            try {
                rlbytes = requestLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes();
        byte[] retval = new byte[rlbytes.length + superbytes.length];
        int i = 0;
        System.arraycopy(rlbytes, 0, retval, 0, rlbytes.length);
        System.arraycopy
                (superbytes, 0, retval, rlbytes.length, superbytes.length);
        return retval;
    }

    /**
     * Creates a default Response message for this request. Note
     * You must add the necessary tags to outgoing responses if need
     * be. For efficiency, this method does not clone the incoming
     * request. If you want to modify the outgoing response, be sure
     * to clone the incoming request as the headers are shared and
     * any modification to the headers of the outgoing response will
     * result in a modification of the incoming request.
     * Tag fields are just copied from the incoming request.
     * Contact headers are removed from the incoming request.
     * Added by Jeff Keyser.
     *
     * @param statusCode Status code for the response.
     * Reason phrase is generated.
     *
     * @return A Response with the status and reason supplied, and a copy
     *of all the original headers from this request.
     */
    public Response createResponse(int statusCode) {
        String reasonPhrase = Response.getReasonPhrase(statusCode);
        return createResponse(statusCode, reasonPhrase);
    }

    /**
     * Creates a default Response message for this request. Note
     * You must add the necessary tags to outgoing responses if need
     * be. For efficiency, this method does not clone the incoming
     * request. If you want to modify the outgoing response, be sure
     * to clone the incoming request as the headers are shared and
     * any modification to the headers of the outgoing response will
     * result in a modification of the incoming request.
     * Tag fields are just copied from the incoming request.
     * Contact headers are removed from the incoming request.
     * Added by Jeff Keyser. Route headers are not added to the
     * response.
     *
     * @param statusCode Status code for the response.
     * @param reasonPhrase Reason phrase for this response.
     * @return A Response with the status and reason supplied.
     * @throws IllegalArgumentException if some argument has an invalid value.
     */
    public Response createResponse(int statusCode,
            String reasonPhrase)
            throws IllegalArgumentException {
        Response newResponse;
        Enumeration headerIterator;
        Header nextHeader;

        newResponse = new Response();
        try {
            newResponse.setStatusCode(statusCode);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Bad code " + statusCode);
        }

        if (reasonPhrase != null) {
            newResponse.setReasonPhrase(reasonPhrase);
        } else {
            newResponse.setReasonPhrase(Response.getReasonPhrase(statusCode));
        }

        headerIterator = super.getHeaders();

        // Time stamp header should be stamped with delay but
        // we dont support this.
        while (headerIterator.hasMoreElements()) {
            nextHeader = (Header)headerIterator.nextElement();
            if (nextHeader instanceof FromHeader ||
                    nextHeader instanceof ToHeader ||
                    nextHeader instanceof ViaList ||
                    nextHeader instanceof CallIdHeader ||
                    nextHeader instanceof RecordRouteList ||
                    nextHeader instanceof CSeqHeader ||
                    // RFC 3265, 3.1.1 200-class responses to SUBSCRIBE
                    // requests also MUST contain an "Expires" header.
                    nextHeader instanceof ExpiresHeader ||
                    Utils.equalsIgnoreCase(nextHeader.getName(),
                        Header.TIMESTAMP)) {
                try {
                    newResponse.attachHeader(nextHeader, false);
                } catch (SipException ex) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                            "Request.createResponse(): can't attach header '" +
                                nextHeader.getHeaderName() + "'.");
                        ex.printStackTrace();
                    }
                }
            } else if (Utils.equalsIgnoreCase(nextHeader.getName(),
                        Header.REQUIRE)) {
                /*
                 * RFC3262, SECTION 3
                 * If the next header contains "Require" header with option
                 * tag as "100rel", we should add this header and also include 
                 * RSeq header field
                 */
                boolean isReliableProvResponse = Header.isReliableTagPresent(
                        nextHeader.getHeaderValue());
                
                if (isReliableProvResponse) {
                    try {
                        newResponse.attachHeader(nextHeader, true);
                    } catch (SipException ex) {
                        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                                "Request.createResponse(): can't attach header" 
                                    +  nextHeader.getHeaderName() + "'.");
                            // ex.printStackTrace();
                        }
                    }
                }
                
            }
        }

        // RFC 3903, p. 5:
        // The Record-Route header field has no meaning in PUBLISH
        // requests or responses, and MUST be ignored if present.
        //
        // RFC 3261, p. 63:
        // Registrars MUST ignore the Record-Route header field if it is
        // included in a REGISTER request. Registrars MUST NOT include a
        // Record-Route header field in any response to a REGISTER request.
        String method = getMethod();
        if (method.equals(Request.PUBLISH) ||
                method.equals(Request.REGISTER)) {
            newResponse.removeHeader(Header.RECORD_ROUTE);
        }

        return newResponse;
    }

    /**
     * Creates a default SIPResquest message that would cancel
     * this request. Note that tag assignment and removal of
     * is left to the caller (we use whatever tags are present in the
     * original request). Acknowledgement: Added by Jeff Keyser.
     *
     * @return A CANCEL Request with a copy all the original headers
     * from this request except for Require, ProxyRequire.
     * @throws SipException if the request can't be created.
     */
    public Request createCancelRequest() throws SipException {
        Request newRequest;
        Enumeration headerIterator;
        Header nextHeader;

        newRequest = new Request();

        // JSR180: Request-URI  // copy from original request
        RequestLine cancelRequestLine = 
            (RequestLine)this.getRequestLine().clone();
        cancelRequestLine.setMethod(CANCEL);
        newRequest.setRequestLine(cancelRequestLine);
        newRequest.setMethod(CANCEL);

        // JSR180: To           // copy from original request
        ToHeader toHeader = this.getTo();
        if (toHeader != null) {
            newRequest.setHeader(toHeader);
        }

        // JSR180: From         // copy from original request
        FromHeader fromHeader = this.getFromHeader();
        if (fromHeader != null) {
            newRequest.setHeader(fromHeader);
        }

        // JSR180: CSeq         // same value for the sequence
        // number as was present in the original request, but
        // the method parameter MUST be equal to "CANCEL"
        CSeqHeader cseqHeader = (CSeqHeader)this.getCSeqHeader().clone();
        if (cseqHeader != null) {
            cseqHeader.setMethod(CANCEL);
            newRequest.setHeader(cseqHeader);
        }

        // JSR180: Call-ID      // copy from original request
        CallIdHeader callIdHeader = this.getCallId();
        if (callIdHeader != null) {
            newRequest.setHeader(callIdHeader);
        }

        // JSR180: Via          // single value equal to the
        // top Via header field of the request being cancelled
        ViaHeader viaHeader = this.getTopmostVia();
        if (viaHeader != null) {
            newRequest.setHeader(viaHeader);
        }

        // JSR180: Route        // If the request being cancelled
        // contains a Route header field, the CANCEL request MUST
        // include that Route header field's values
        RouteList routeList = this.getRouteHeaders();
        if (routeList != null) {
            newRequest.setHeaders(routeList.getHeaders());
        }

        // JSR180: Max-Forwards (TBD)// header field serves to limit the
        // number of hops a request can transit on the way to its destination.
        // Current version: copy from original request
        MaxForwardsHeader mfHeader =
            (MaxForwardsHeader)getHeader(Header.MAX_FORWARDS);
        if (mfHeader != null) {
            newRequest.setHeader(mfHeader);
        }

        return newRequest;
    }

    /**
     * Creates a default ACK Request message for this original request.
     * Note that the defaultACK Request does not include the
     * content of the original Request. If responseToHeader
     * is null then the toHeader of this request is used to
     * construct the ACK. Note that tag fields are just copied
     * from the original SIP Request. Added by Jeff Keyser.
     *
     * @param responseToHeader To header to use for this request.
     * @return A Request with an ACK method.
     * @throws SipException if the request can't be created.
     */
    public Request createAckRequest(ToHeader responseToHeader)
            throws SipException {
        Request newRequest;
        Enumeration headerIterator;
        Header nextHeader;

        newRequest = new Request();
        newRequest.setRequestLine
                ((RequestLine)this.requestLine.clone());
        newRequest.setMethod(ACK);
        headerIterator = getHeaders();
        while (headerIterator.hasMoreElements()) {
            nextHeader = (Header)headerIterator.nextElement();
            if (nextHeader.getHeaderName().equals
                    (Header.ROUTE)) {

                // Route header for ACK is assigned by the
                // Dialog if necessary.
                continue;
            } else if (nextHeader.getHeaderName().equals
                    (Header.PROXY_AUTHORIZATION)) {
                // Remove proxy auth header.
                // Assigned by the Dialog if necessary.
                continue;
            } else if (nextHeader instanceof ContentLengthHeader) {
                // Adding content is responsibility of user.
                nextHeader = (Header) nextHeader.clone();
                ((ContentLengthHeader)nextHeader).setContentLength(0);

            } else if (nextHeader instanceof ContentTypeHeader) {
                // Content type header is removed since
                // content length is 0. Bug fix from
                // Antonis Kyardas.
                continue;
            } else if (nextHeader instanceof CSeqHeader) {
                CSeqHeader cseq = (CSeqHeader) nextHeader.clone();
                cseq.setMethod(ACK);
                nextHeader = cseq;
            } else if (nextHeader instanceof ToHeader) {
                if (responseToHeader != null) {
                    nextHeader = responseToHeader;
                } else {
                    nextHeader = (Header) nextHeader.clone();
                }
            } else {
                nextHeader = (Header) nextHeader.clone();
            }

            newRequest.attachHeader(nextHeader, false);
        }

        return newRequest;
    }

    /**
     * Creates a new default Request from the original request. Warning:
     * the newly created Request, shares the headers of
     * this request but we generate any new headers that we need to modify
     * so the original request is umodified. However, if you modify the
     * shared headers after this request is created, then the newly
     * created request will also be modified.
     * If you want to modify the original request
     * without affecting the returned Request
     * make sure you clone it before calling this method.
     * Following are the differences between the original request headers
     * and the generated request headers.
     * <ul>
     * <li>
     * Contact headers are not included in the newly created request.
     * Setting the appropriate sequence number is the responsibility of
     * the caller. </li>
     * <li> RouteList is not copied for ACK and CANCEL </li>
     * <li> Note that we DO NOT copy the body of the
     * argument into the returned header. We do not copy the content
     * type header from the original request either. These have to be
     * added seperately and the content length has to be correctly set
     * if necessary the content length is set to 0 in the returned header.
     * </li>
     * <li>Contact List is not copied from the original request.</li>
     * <li>RecordRoute List is not included from original request. </li>
     * <li>Via header is not included from the original request. </li>
     * </ul>
     *
     * @param requestLine is the new request line.
     *
     * @param switchHeaders is a boolean flag that causes to and from
     * headers to switch (set this to true if you are the
     * server of the transaction and are generating a BYE
     * request). If the headers are switched, we generate
     * new FromHeader and To headers otherwise we just use the
     * incoming headers.
     *
     * @return a new Default SIP Request which has the requestLine specified.
     *
     * @throws SipException if the request can't be created.
     */
    public Request createRequest(RequestLine requestLine,
            boolean switchHeaders) throws SipException {
        Request newRequest = new Request();
        newRequest.requestLine = requestLine;
        Enumeration headerIterator = this.getHeaders();
        while (headerIterator.hasMoreElements()) {
            Header nextHeader =
                    (Header)headerIterator.nextElement();
            // For BYE and cancel set the CSeqHeader header to the
            // appropriate method.
            if (nextHeader instanceof CSeqHeader) {
                CSeqHeader newCseq = (CSeqHeader) nextHeader.clone();
                nextHeader = newCseq;
                newCseq.setMethod(requestLine.getMethod());
            } else if (requestLine.getMethod().equals(ACK) &&
                    nextHeader instanceof ContactList) {
                // ACKS never get Contact headers.
                continue;
            } else if (nextHeader instanceof ViaList) {
                ViaHeader via = (ViaHeader)
                (((ViaList)nextHeader).getFirst().clone());
                via.removeParameter(SIPConstants.GENERAL_BRANCH);
                nextHeader = via;
                // Cancel and ACK preserve the branch ID.
            } else if (nextHeader instanceof RouteList) {
                continue; // Route is kept by dialog.
            } else if (nextHeader instanceof RecordRouteList) {
                continue; // RR is added by the caller.
            } else if (nextHeader instanceof ContactList) {
                continue;
            } else if (nextHeader instanceof ToHeader) {
                ToHeader to = (ToHeader) nextHeader;
                if (switchHeaders) {
                    nextHeader = new FromHeader(to);
                    ((FromHeader) nextHeader).removeTag();
                } else {
                    nextHeader = (Header) to.clone();
                    ((ToHeader) nextHeader).removeTag();
                }
            } else if (nextHeader instanceof FromHeader) {
                FromHeader from = (FromHeader) nextHeader;
                if (switchHeaders) {
                    nextHeader = new ToHeader(from);
                    ((ToHeader) nextHeader).removeTag();
                } else {
                    nextHeader = (Header) from.clone();
                    ((FromHeader) nextHeader).removeTag();
                }
            } else if (nextHeader instanceof ContentLengthHeader) {
                ContentLengthHeader cl =
                        (ContentLengthHeader)
                        nextHeader.clone();
                cl.setContentLength(0);
                nextHeader = cl;
            } else if (nextHeader instanceof ContentTypeHeader) {
                continue;
            } else if (nextHeader instanceof MaxForwardsHeader) {
                // Header is regenerated if the request is to be switched
                if (switchHeaders) {
                    MaxForwardsHeader mf  =
                            (MaxForwardsHeader)
                            nextHeader.clone();
                    mf.setMaxForwards(70);
                    nextHeader = mf;
                }
            } else if (!(nextHeader instanceof CallIdHeader) &&
                    !(nextHeader instanceof MaxForwardsHeader)) {
                // Route is kept by dialog.
                // RR is added by the caller.
                // Contact is added by the Caller
                // Any extension headers must be added
                // by the caller.
                continue;
            }

            newRequest.attachHeader(nextHeader, false);

        }
        return newRequest;

    }

    /**
     * Creates a BYE request from this request.
     *
     * @param switchHeaders is a boolean flag that causes from and
     * isServerTransaction to headers to be swapped. Set this
     * to true if you are the server of the dialog and are generating
     * a BYE request for the dialog.
     * @return a new default BYE request.
     * @throws SipException if the request can't be created.
     */
    public Request createBYERequest(boolean switchHeaders) throws SipException {
        RequestLine rl = (RequestLine) requestLine.clone();
        rl.setMethod(BYE);
        return createRequest(rl, switchHeaders);
    }

    /**
     * Creates an ACK request from this request. This is suitable for
     * generating an ACK for an INVITE client transaction.
     *
     * @return an ACK request that is generated from this request.
     * @throws SipException if the request can't be created.
     */
    public Request createACKRequest() throws SipException {
        RequestLine rl = (RequestLine) requestLine.clone();
        rl.setMethod(ACK);
        return createRequest(rl, false);
    }

    /**
     * Gets the host from the topmost via header.
     *
     * @return the string representation of the host from the topmost via
     * header.
     */
    public String getViaHost() {
        ViaHeader via = (ViaHeader) this.getViaHeaders().getFirst();
        return via.getHost();

    }

    /**
     * Gets the port from the topmost via header.
     *
     * @return the port from the topmost via header (5060 if there is
     * no port indicated).
     */
    public int getViaPort() {
        ViaHeader via = (ViaHeader) this.getViaHeaders().getFirst();
        if (via.hasPort())
            return via.getPort();
        else
            return 5060;
    }

    /**
     * Gets the first line encoded.
     *
     * @return a string containing the encoded request line.
     */
    public String getFirstLine() {
        if (requestLine == null)
            return null;
        else
            return this.requestLine.encode();
    }

    /**
     * Sets the sip version.
     *
     * @param sipVersion the sip version to set.
     */

    public void setSIPVersion(String sipVersion)
    throws ParseException {
        if (sipVersion == null || !sipVersion.equals("SIP/2.0"))
            throw new ParseException("sipVersion", 0);
        this.requestLine.setSIPVersion(sipVersion);
    }

    /**
     * Gets the SIP version.
     *
     * @return the SIP version from the request line.
     */
    public String getSIPVersion() {
        return this.requestLine.getSipVersion();
    }

    /**
     * Gets the transaction pointer.
     * @return the transaction pointer
     */
    public Object getTransaction() {
        // Return an opaque pointer to the transaction object.
        // This is for consistency checking and quick lookup.
        return this.transactionPointer;
    }

    /**
     * Sets the transaction pointer.
     * @param transaction thenew transaction pointer
     */
    public void setTransaction(Object transaction) {
        this.transactionPointer = transaction;
    }
    
    /**
     * Gets the refresh request ID.
     * @return refresh ID. -1 if refresh helper is not used.
     */
    public int getRefreshID() {
        return this.refreshID;
    }

    /**
     * Sets the refresh request ID.
     * @param refresh ID assigned by refresh manager
     */
    public void setRefreshID(int id) {
        this.refreshID = id;
    }

    /**
     * Gets the Accept-Contact header (null if one does not exist).
     * @return Accept-Contact header
     */
    public AcceptContactHeader getAcceptContact() {
        return (AcceptContactHeader) getHeader(Header.ACCEPT_CONTACT);
    }

}
