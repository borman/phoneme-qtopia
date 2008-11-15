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

import java.util.Vector;
import javax.microedition.sip.SipException;

import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;
import gov.nist.siplite.parser.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Message Factory implementation
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class MessageFactory {

    /** Creates a new instance of MessageFactoryImpl */
    public MessageFactory() {
    }

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message
     * with a body in the form of a Java object and the body content type.
     *
     * @param requestURI the new URI object of the requestURI value
     * of this Message.
     * @param method the new string of the method value of this Message.
     * @param callId the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this
     * Message.
     * @param from the new FromHeader object of the from value of this
     * Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param contentType the new ContentTypeHeader object of the content
     * type value of this Message.
     * @param content the new Object of the body content value of this
     * Message.
     * @return a new request object
     * @throws SipException if the request can't be created.
     * IMPL_NOTE: investigate. "throws ParseException which signals that an error
     * has been reached unexpectedly while parsing the method or the body."
     */
    public Request createRequest(URI requestURI,
            String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards,
            ContentTypeHeader contentType, Object content)
                throws SipException {

        if (requestURI == null ||
                method == null ||
                callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null) {
            throw new NullPointerException("Null parameters");
        }

        Request sipRequest = new Request();

        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setHeader(cSeq);
        sipRequest.setHeader(from);
        sipRequest.setHeader(to);
        sipRequest.setVia(via);
        sipRequest.setHeader(maxForwards);
        sipRequest.setContent(content, contentType);

        return sipRequest;
    }

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message
     * with a body in the form of a byte array and body content type.
     *
     * @param requestURI - the new URI object of the
     * requestURI value of this Message.
     * @param method - the new string of the method value of this Message.
     * @param callId - the new CallIdHeader object of the callId value of
     *  this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param content - the new byte array of the body content value of
     * this Message.
     * @param contentType - the new ContentTypeHeader object of the content
     * type value of this Message.
     * @return the new request object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method or the body.
     * @throws SipException if the request can't be created.
     */
    public Request createRequest(URI requestURI, String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards, byte[] content,
            ContentTypeHeader  contentType)
            throws ParseException, SipException {
        if (requestURI == null ||
                method == null ||
                callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new ParseException("JAIN-SIP Exception,"
                    + " some parameters are missing"
                    + ", unable to create the request", 0);

        Request sipRequest = new Request();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setHeader(cSeq);
        sipRequest.setHeader(from);
        sipRequest.setHeader(to);
        sipRequest.setVia(via);
        sipRequest.setHeader(maxForwards);
        sipRequest.setHeader(contentType);
        sipRequest.setMessageContent(content);
        return sipRequest;
    }

    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message.
     * This new Request does not contain a body.
     *
     * @param requestURI - the new URI object of the requestURI value of
     * this Message.
     * @param method - the new string of the method value of this Message.
     * @param callId - the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @return the new request object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method.
     * @throws SipException if the request can't be created.
     */
    public Request createRequest(URI requestURI, String method, CallIdHeader
            callId, CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards)
            throws ParseException, SipException {
        if (requestURI == null ||
                method == null ||
                callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null)
            throw new ParseException("JAIN-SIP Exception, "
                    + "some parameters are missing"
                    + ", unable to create the request", 0);

        Request sipRequest = new Request();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setHeader(cSeq);
        sipRequest.setHeader(from);
        sipRequest.setHeader(to);
        sipRequest.setVia(via);
        sipRequest.setHeader(maxForwards);

        return sipRequest;
    }


    // Standard Response Creation methods

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a Java object and the body content type.
     *
     * @param statusCode - the new integer of the statusCode value of
     * this Message.
     * @param callId - the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param content - the new Object of the body content value of
     * this Message.
     * @param contentType - the new ContentTypeHeader object of the content
     *  type value of this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the request can't be created.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards,
            Object content,
            ContentTypeHeader contentType)
                throws ParseException, SipException {
        if (callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("unable to create the response");

        Response sipResponse = new Response();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        String reasonPhrase = Response.getReasonPhrase(statusCode);
        if (reasonPhrase == null)
            throw new ParseException(statusCode + " Unknown ", 0);
        statusLine.setReasonPhrase(reasonPhrase);
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setHeader(cSeq);
        sipResponse.setHeader(from);
        sipResponse.setHeader(to);
        sipResponse.setVia(via);
        sipResponse.setHeader(maxForwards);
        sipResponse.setContent(content, contentType);

        return sipResponse;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a byte array and the body content type.
     *
     * @param statusCode - the new integer of the statusCode value of
     * this Message.
     * @param callId - the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param content - the new byte array of the body content value of
     * this Message.
     * @param contentType - the new ContentTypeHeader object of the content
     * type value of this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the request can't be created.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards,
            byte[] content,
            ContentTypeHeader contentType)
            throws ParseException, SipException {
        if (callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("Null params");

        Response sipResponse = new Response();
        sipResponse.setStatusCode(statusCode);
        sipResponse.setCallId(callId);
        sipResponse.setHeader(cSeq);
        sipResponse.setHeader(from);
        sipResponse.setHeader(to);
        sipResponse.setVia(via);
        sipResponse.setHeader(maxForwards);
        sipResponse.setHeader(contentType);
        sipResponse.setMessageContent(content);

        return sipResponse;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message. This new
     * Response does not contain a body.
     *
     * @param statusCode - the new integer of the statusCode value of
     * this Message.
     * @param callId - the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse(int statusCode, CallIdHeader callId,
            CSeqHeader cSeq, FromHeader from,
            ToHeader to, Vector via,
            MaxForwardsHeader maxForwards)
            throws ParseException, SipException {
        if (callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null)
            throw new ParseException("JAIN-SIP Exception, "
                    + "some parameters are missing"
                    + ", unable to create the response", 0);

        Response sipResponse = new Response();
        sipResponse.setStatusCode(statusCode);
        sipResponse.setCallId(callId);
        sipResponse.setHeader(cSeq);
        sipResponse.setHeader(from);
        sipResponse.setHeader(to);
        sipResponse.setVia(via);
        sipResponse.setHeader(maxForwards);

        return sipResponse;
    }


    // Response Creation methods based on a Request

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request with a new body in the form of a
     * Java object and the body content type.
     *
     * @param statusCode - the new integer of the statusCode value of
     * this Message.
     * @param request - the received Reqest object upon which to base
     * the Response.
     * @param content - the new Object of the body content value of
     * this Message.
     * @param contentType - the new ContentTypeHeader object of the content
     *  type value of this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse(int statusCode, Request request,
            ContentTypeHeader contentType,
            Object content)
                throws ParseException, SipException {
        if (request == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("null parameters");

        Request sipRequest = (Request)request;
        Response sipResponse = sipRequest.createResponse(statusCode);
        sipResponse.setContent(content, contentType);

        return sipResponse;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request with a new body in the form of a
     * byte array and the body content type.
     *
     * @param statusCode - the new integer of the statusCode value of
     * this Message.
     * @param request - the received Reqest object upon which to base
     *  the Response.
     * @param content - the new byte array of the body content value of
     * this Message.
     * @param contentType - the new ContentTypeHeader object of the content
     * type value of this Message.
     * @return the new resaponse object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse(int statusCode, Request request,
            ContentTypeHeader contentType,
            byte[] content)
                throws ParseException, SipException {
        if (request == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("null Parameters");

        Request sipRequest = (Request)request;
        Response sipResponse = sipRequest.createResponse(statusCode);
        sipResponse.setHeader(contentType);
        sipResponse.setMessageContent(content);

        return sipResponse;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, based on a specific Request message. This new Response does
     * not contain a body.
     *
     * @param statusCode - the new integer of the statusCode value of
     *  this Message.
     * @param request - the received Reqest object upon which to base
     * the Response.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse(int statusCode, Request request)
            throws ParseException, SipException {
        if (request == null)
            throw new NullPointerException("null parameters");

        // if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
        //     Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
        //         "createResponse " + request);
        // }

        Request sipRequest = (Request)request;
        Response sipResponse = sipRequest.createResponse(statusCode);
        // Remove the content from the message
        sipResponse.removeContent();
        sipResponse.removeHeader(ContentTypeHeader.NAME);

        return sipResponse;
    }



    /**
     * Creates a new Request message of type specified by the method paramater,
     * containing the URI of the Request, the mandatory headers of the message
     * with a body in the form of a byte array and body content type.
     *
     * @param requestURI - the new URI object of the requestURI value
     * of this Message.
     * @param method - the new string of the method value of this Message.
     * @param callId - the new CallIdHeader object of the callId value
     * of this Message.
     * @param cSeq - the new CSeqHeader object of the cSeq value of
     * this Message.
     * @param from - the new FromHeader object of the from value of
     * this Message.
     * @param to - the new ToHeader object of the to value of this Message.
     * @param via - the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param contentType - the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content - the new byte array of the body content
     * value of this Message.
     * @return the new request object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method or the body.
     * @throws SipException if the response can't be created.
     */
    public Request createRequest
            (URI requestURI,
            String method,
            CallIdHeader callId,
            CSeqHeader cSeq,
            FromHeader from,
            ToHeader to,
            Vector via,
            MaxForwardsHeader maxForwards,
            ContentTypeHeader contentType, byte[] content)
	        throws ParseException, SipException {
        if (requestURI == null ||
                method == null ||
                callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException
                    ("missing parameters");

        Request sipRequest = new Request();
        sipRequest.setRequestURI(requestURI);
        sipRequest.setMethod(method);
        sipRequest.setCallId(callId);
        sipRequest.setHeader(cSeq);
        sipRequest.setHeader(from);
        sipRequest.setHeader(to);
        sipRequest.setVia(via);
        sipRequest.setHeader(maxForwards);
        sipRequest.setContent(content, contentType);
        return sipRequest;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a Java object and the body content type.
     *
     * @param statusCode the new integer of the
     * statusCode value of this Message.
     * @param callId the new CallIdHeader object of the
     * callId value of this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new Object of the body content value of this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse
            (int statusCode,
            CallIdHeader callId,
            CSeqHeader cSeq,
            FromHeader from,
            ToHeader to,
            Vector via,
            MaxForwardsHeader maxForwards,
            ContentTypeHeader contentType,
            Object content) throws ParseException, SipException {
        if (callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("missing parameters");

        Response sipResponse = new Response();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        String reason = Response.getReasonPhrase(statusCode);
        if (reason == null)
            throw new ParseException(statusCode + " Unknown", 0);
        statusLine.setReasonPhrase(reason);
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setHeader(cSeq);
        sipResponse.setHeader(from);
        sipResponse.setHeader(to);
        sipResponse.setVia(via);
        sipResponse.setContent(content, contentType);

        return sipResponse;
    }

    /**
     * Creates a new Response message of type specified by the statusCode
     * paramater, containing the mandatory headers of the message with a body
     * in the form of a byte array and the body content type.
     *
     * @param statusCode the new integer of the statusCode value of
     * this Message.
     * @param callId the new CallIdHeader object of the callId value of
     * this Message.
     * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
     * @param from the new FromHeader object of the from value of this Message.
     * @param to the new ToHeader object of the to value of this Message.
     * @param via the new Vector object of the ViaHeaders of this Message.
     * @param maxForwards the new MaxForward of this Message.
     * @param contentType the new ContentTypeHeader object of the content type
     * value of this Message.
     * @param content the new byte array of the body content value of
     *  this Message.
     * @return the new response object
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the statusCode or the body.
     * @throws SipException if the response can't be created.
     */
    public Response createResponse(int statusCode,
            CallIdHeader callId,
            CSeqHeader cSeq,
            FromHeader from,
            ToHeader to,
            Vector via,
            MaxForwardsHeader maxForwards,
            ContentTypeHeader contentType,
            byte[] content) throws ParseException, SipException {
        if (callId == null ||
                cSeq == null ||
                from == null ||
                to == null ||
                via == null ||
                maxForwards == null ||
                content == null ||
                contentType == null)
            throw new NullPointerException("missing parameters");

        Response sipResponse = new Response();
        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statusCode);
        String reason = Response.getReasonPhrase(statusCode);
        if (reason == null)
            throw new ParseException(statusCode + " : Unknown", 0);
        statusLine.setReasonPhrase(reason);
        sipResponse.setStatusLine(statusLine);
        sipResponse.setCallId(callId);
        sipResponse.setHeader(cSeq);
        sipResponse.setHeader(from);
        sipResponse.setHeader(to);
        sipResponse.setVia(via);
        sipResponse.setContent(content, contentType);

        return sipResponse;
    }

    /**
     * Create a request from a string. Conveniance method for UACs
     * that want to create an outgoing request from a string. Only the
     * headers of the request should be included in the String that is
     * supplied to this method.
     *
     * @param requestString string from which to create the message
     * @return the new request object
     */
    public Request createRequest(String requestString)
            throws ParseException {
        Request sipRequest = new Request();
        StringMsgParser parser = new StringMsgParser();
        SipURI requestURI = parser.parseSIPUrl(requestString);
        sipRequest.setRequestURI(requestURI);
        return sipRequest;

    }

}
