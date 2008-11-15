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
package gov.nist.siplite;

/**
 * A list of error codes for SIP
 */
public class SIPErrorCodes {
    
    /**
     * This response indicates that the request has been received by the
     * next-hop server and that some unspecified action is being taken on
     * behalf of this call (for example, a database is being consulted). This
     * response, like all other provisional responses, stops retransmissions of
     * an INVITE by a UAC. The 100 (Trying) response is different from other
     * provisional responses, in that it is never forwarded upstream by a
     * stateful proxy.
     */
    public static final int TRYING = 100;
    
    /**
     * The User Agent receiving the INVITE is trying to alert the user. This
     * response MAY be used to initiate local ringback.
     */
    public static final int RINGING = 180;
    
    /**
     * A server MAY use this status code to indicate that the call is being
     * forwarded to a different set of destinations.
     */
    public static final int CALL_IS_BEING_FORWARDED = 181;
    
    /**
     * The called party is temporarily unavailable, but the server has decided
     * to queue the call rather than reject it. When the callee becomes
     * available, it will return the appropriate final status response. The
     * reason phrase MAY give further details about the status of the call,
     * for example, "5 calls queued; expected waiting time is 15 minutes". The
     * server MAY issue several 182 (Queued) responses to update the caller
     * about the status of the queued call.
     */
    public static final int QUEUED = 182;
    
    /**
     * The 183 (Session Progress) response is used to convey information about
     * the progress of the call that is not otherwise classified. The
     * Reason-Phrase, header fields, or message body MAY be used to convey more
     * details about the call progress.
     *
     * @since v1.1
     */
    public static final int SESSION_PROGRESS = 183;
    
    /**
     * The request has succeeded. The information returned with the response
     * depends on the method used in the request.
     */
    public static final int OK = 200;
    
    /**
     * The Acceptable extension response code signifies that the request has
     * been accepted for processing, but the processing has not been completed.
     * The request might or might not eventually be acted upon, as it might be
     * disallowed when processing actually takes place. There is no facility
     * for re-sending a status code from an asynchronous operation such as this.
     * The 202 response is intentionally non-committal. Its purpose is to allow
     * a server to accept a request for some other process (perhaps a
     * batch-oriented process that is only run once per day) without requiring
     * that the user agent's connection to the server persist until the process
     * is completed. The entity returned with this response SHOULD include an
     * indication of the request's current status and either a pointer to a
     * status monitor or some estimate of when the user can expect the request
     * to be fulfilled. This response code is specific to the event
     * notification framework.
     *
     * @since v1.1
     */
    public static final int ACCEPTED = 202;
    
    /**
     * The address in the request resolved to several choices, each with its
     * own specific location, and the user (or UA) can select a preferred
     * communication end point and redirect its request to that location.
     * <p>
     * The response MAY include a message body containing a list of resource
     * characteristics and location(s) from which the user or UA can choose
     * the one most appropriate, if allowed by the Accept request header field.
     * However, no MIME types have been defined for this message body.
     * <p>
     * The choices SHOULD also be listed as Contact fields. Unlike HTTP, the
     * SIP response MAY contain several Contact fields or a list of addresses
     * in a Contact field. User Agents MAY use the Contact header field value
     * for automatic redirection or MAY ask the user to confirm a choice.
     * However, this specification does not define any standard for such
     * automatic selection.
     * <p>
     * This status response is appropriate if the callee can be reached at
     * several different locations and the server cannot or prefers not to
     * proxy the request.
     */
    public static final int MULTIPLE_CHOICES = 300;
    
    /**
     * The user can no longer be found at the address in the Request-URI, and
     * the requesting client SHOULD retry at the new address given by the
     * Contact header field. The requestor SHOULD update any local directories,
     * address books, and user location caches with this new value and redirect
     * future requests to the address(es) listed.
     */
    public static final int MOVED_PERMANENTLY = 301;
    
    /**
     * The requesting client SHOULD retry the request at the new address(es)
     * given by the Contact header field. The Request-URI of the new request
     * uses the value of the Contact header field in the response.
     * <p>
     * The duration of the validity of the Contact URI can be indicated through
     * an Expires header field or an expires parameter in the Contact header
     * field. Both proxies and User Agents MAY cache this URI for the duration
     * of the expiration time. If there is no explicit expiration time, the
     * address is only valid once for recursing, and MUST NOT be cached for
     * future transactions.
     * <p>
     * If the URI cached from the Contact header field fails, the Request-URI
     * from the redirected request MAY be tried again a single time. The
     * temporary URI may have become out-of-date sooner than the expiration
     * time, and a new temporary URI may be available.
     */
    public static final int MOVED_TEMPORARILY = 302;
    
    /**
     * The requested resource MUST be accessed through the proxy given by the
     * Contact field.  The Contact field gives the URI of the proxy. The
     * recipient is expected to repeat this single request via the proxy.
     * 305 (Use Proxy) responses MUST only be generated by UASs.
     */
    public static final int USE_PROXY = 305;
    
    /**
     * The call was not successful, but alternative services are possible. The
     * alternative services are described in the message body of the response.
     * Formats for such bodies are not defined here, and may be the subject of
     * future standardization.
     */
    public static final int ALTERNATIVE_SERVICE = 380;
    
    /**
     * The request could not be understood due to malformed syntax. The
     * Reason-Phrase SHOULD identify the syntax problem in more detail, for
     * example, "Missing Call-ID header field".
     */
    public static final int BAD_REQUEST = 400;
    
    /**
     * The request requires user authentication. This response is issued by
     * UASs and registrars, while 407 (Proxy Authentication Required) is used
     * by proxy servers.
     */
    public static final int UNAUTHORIZED = 401;
    
    /**
     * Reserved for future use.
     */
    public static final int PAYMENT_REQUIRED = 402;
    
    /**
     * The server understood the request, but is refusing to fulfill it.
     * Authorization will not help, and the request SHOULD NOT be repeated.
     */
    public static final int FORBIDDEN = 403;
    
    /**
     * The server has definitive information that the user does not exist at
     * the domain specified in the Request-URI.  This status is also returned
     * if the domain in the Request-URI does not match any of the domains
     * handled by the recipient of the request.
     */
    public static final int NOT_FOUND = 404;
    
    /**
     * The method specified in the Request-Line is understood, but not allowed
     * for the address identified by the Request-URI. The response MUST include
     * an Allow header field containing a list of valid methods for the
     * indicated address
     */
    public static final int METHOD_NOT_ALLOWED = 405;
    
    /**
     * The resource identified by the request is only capable of generating
     * response entities that have content characteristics not acceptable
     * according to the Accept header field sent in the request.
     */
    public static final int NOT_ACCEPTABLE = 406;
    
    /**
     * This code is similar to 401 (Unauthorized), but indicates that
     * the client MUST first authenticate itself with the proxy. This
     * status code can be used for applications where access to the
     * communication channel (for example, a telephony gateway) rather
     * than the callee requires authentication.
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    
    /**
     * The server could not produce a response within a suitable amount of
     * time, for example, if it could not determine the location of the user
     * in time. The client MAY repeat the request without modifications at
     * any later time.
     */
    public static final int REQUEST_TIMEOUT = 408;
    
    /**
     * The requested resource is no longer available at the server and no
     * forwarding address is known. This condition is expected to be considered
     * permanent. If the server does not know, or has no facility to determine,
     * whether or not the condition is permanent, the status code 404
     * (Not Found) SHOULD be used instead.
     */
    public static final int GONE = 410;
    
    /**
     * The server is refusing to process a request because the request
     * entity-body is larger than the server is willing or able to process. The
     * server MAY close the connection to prevent the client from continuing
     * the request. If the condition is temporary, the server SHOULD include a
     * Retry-After header field to indicate that it is temporary and after what
     * time the client MAY try again.
     *
     * @since v1.1
     */
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    
    /**
     * The server is refusing to service the request because the Request-URI
     * is longer than the server is willing to interpret.
     *
     * @since v1.1
     */
    public static final int REQUEST_URI_TOO_LONG = 414;
    
    /**
     * The server is refusing to service the request because the message body
     * of the request is in a format not supported by the server for the
     * requested method. The server MUST return a list of acceptable formats
     * using the Accept, Accept-Encoding, or Accept-Language header field,
     * depending on the specific problem with the content.
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    
    /**
     * The server cannot process the request because the scheme of the URI in
     * the Request-URI is unknown to the server.
     *
     * @since v1.1
     */
    public static final int UNSUPPORTED_URI_SCHEME = 416;
    
    /**
     * The server did not understand the protocol extension specified in a
     * Proxy-Require or Require header field. The server MUST include a list of
     * the unsupported extensions in an Unsupported header field in
     * the response.
     */
    public static final int BAD_EXTENSION = 420;
    
    /**
     * The UAS needs a particular extension to process the request, but this
     * extension is not listed in a Supported header field in the request.
     * Responses with this status code MUST contain a Require header field
     * listing the required extensions.
     * <p>
     * A UAS SHOULD NOT use this response unless it truly cannot provide any
     * useful service to the client. Instead, if a desirable extension is not
     * listed in the Supported header field, servers SHOULD process the request
     * using baseline SIP capabilities and any extensions supported by the
     * client.
     *
     * @since v1.1
     */
    public static final int EXTENSION_REQUIRED = 421;
    
    /**
     * The server is rejecting the request because the expiration time of the
     * resource refreshed by the request is too short. This response can be
     * used by a registrar to reject a registration whose Contact header field
     * expiration time was too small.
     *
     * @since v1.1
     */
    public static final int INTERVAL_TOO_BRIEF = 423;
    
    /**
     * The callee's end system was contacted successfully but the callee is
     * currently unavailable (for example, is not logged in, logged in but in a
     * state that precludes communication with the callee, or has activated the
     * "do not disturb" feature). The response MAY indicate a better time to
     * call in the Retry-After header field. The user could also be available
     * elsewhere (unbeknownst to this server). The reason phrase
     * SHOULD indicate
     * a more precise cause as to why the callee is unavailable. This value
     * SHOULD be settable by the UA. Status 486 (Busy Here) MAY be used to more
     * precisely indicate a particular reason for the call failure.
     * <p>
     * This status is also returned by a redirect or proxy server that
     * recognizes the user identified by the Request-URI, but does not
     * currently
     * have a valid forwarding location for that user.
     *
     * @since v1.1
     */
    public static final int TEMPORARILY_UNAVAILABLE = 480;
    
    /**
     * This status indicates that the UAS received a request that does not
     * match any existing dialog or transaction.
     */
    public static final int CALL_OR_TRANSACTION_DOES_NOT_EXIST = 481;
    
    /**
     * The server has detected a loop.
     */
    public static final int LOOP_DETECTED = 482;
    
    /**
     * The server received a request that contains a Max-Forwards header field
     * with the value zero.
     */
    public static final int TOO_MANY_HOPS = 483;
    
    /**
     * The server received a request with a Request-URI that was incomplete.
     * Additional information SHOULD be provided in the reason phrase. This
     * status code allows overlapped dialing. With overlapped dialing, the
     * client does not know the length of the dialing string. It sends strings
     * of increasing lengths, prompting the user for more input, until it no
     * longer receives a 484 (Address Incomplete) status response.
     */
    public static final int ADDRESS_INCOMPLETE = 484;
    
    /**
     * The Request-URI was ambiguous. The response MAY contain a listing of
     * possible unambiguous addresses in Contact header fields. Revealing
     * alternatives can infringe on privacy of the user or the organization.
     * It MUST be possible to configure a server to respond with status 404
     * (Not Found) or to suppress the listing of possible choices for ambiguous
     * Request-URIs. Some email and voice mail systems provide this
     * functionality. A status code separate from 3xx is used since the
     * semantics are different: for 300, it is assumed that the same person or
     * service will be reached by the choices provided. While an automated
     * choice or sequential search makes sense for a 3xx response, user
     * intervention is required for a 485 (Ambiguous) response.
     */
    public static final int AMBIGUOUS = 485;
    
    /**
     * The callee's end system was contacted successfully, but the callee is
     * currently not willing or able to take additional calls at this end
     * system. The response MAY indicate a better time to call in the
     * Retry-After
     * header field. The user could also be available elsewhere, such as
     * through a voice mail service. Status 600 (Busy Everywhere) SHOULD be
     * used if the client knows that no other end system will be able to accept
     * this call.
     */
    public static final int BUSY_HERE = 486;
    
    /**
     * The request was terminated by a BYE or CANCEL request. This response is
     * never returned for a CANCEL request itself.
     *
     * @since v1.1
     */
    public static final int REQUEST_TERMINATED = 487;
    
    /**
     * The response has the same meaning as 606 (Not Acceptable), but only
     * applies to the specific resource addressed by the Request-URI and the
     * request may succeed elsewhere. A message body containing a description
     * of media capabilities MAY be present in the response, which is formatted
     * according to the Accept header field in the INVITE (or application/sdp
     * if not present), the same as a message body in a 200 (OK) response to
     * an OPTIONS request.
     *
     * @since v1.1
     */
    public static final int NOT_ACCEPTABLE_HERE = 488;
    
    /**
     * The Bad Event extension response code is used to indicate that the
     * server did not understand the event package specified in a "Event"
     * header field. This response code is specific to the event notification
     * framework.
     *
     * @since v1.1
     */
    public static final int BAD_EVENT = 489;
    
    /**
     * The request was received by a UAS that had a pending request within
     * the same dialog.
     *
     * @since v1.1
     */
    public static final int REQUEST_PENDING = 491;
    
    /**
     * The request was received by a UAS that contained an encrypted MIME body
     * for which the recipient does not possess or will not provide an
     * appropriate decryption key. This response MAY have a single body
     * containing an appropriate public key that should be used to encrypt MIME
     * bodies sent to this UA.
     *
     * @since v1.1
     */
    public static final int UNDECIPHERABLE = 493;
    
    /**
     * The server encountered an unexpected condition that prevented it from
     * fulfilling the request. The client MAY display the specific error
     * condition and MAY retry the request after several seconds. If the
     * condition is temporary, the server MAY indicate when the client may
     * retry the request using the Retry-After header field.
     */
    public static final int SERVER_INTERNAL_ERROR = 500;
    
    /**
     * The server does not support the functionality required to fulfill the
     * request. This is the appropriate response when a UAS does not recognize
     * the request method and is not capable of supporting it for any user.
     * Proxies forward all requests regardless of method. Note that a 405
     * (Method Not Allowed) is sent when the server recognizes the request
     * method, but that method is not allowed or supported.
     */
    public static final int NOT_IMPLEMENTED = 501;
    
    /**
     * The server, while acting as a gateway or proxy, received an invalid
     * response from the downstream server it accessed in attempting to
     * fulfill the request.
     */
    public static final int BAD_GATEWAY = 502;
    
    /**
     * The server is temporarily unable to process the request due to a
     * temporary overloading or maintenance of the server. The server MAY
     * indicate when the client should retry the request in a Retry-After
     * header field. If no Retry-After is given, the client MUST act as if it
     * had received a 500 (Server Internal Error) response.
     * <p>
     * A client (proxy or UAC) receiving a 503 (Service Unavailable) SHOULD
     * attempt to forward the request to an alternate server. It SHOULD NOT
     * forward any other requests to that server for the duration specified
     * in the Retry-After header field, if present.
     * <p>
     * Servers MAY refuse the connection or drop the request instead of
     * responding with 503 (Service Unavailable).
     *
     * @since v1.1
     */
    public static final int SERVICE_UNAVAILABLE = 503;
    
    /**
     * The server did not receive a timely response from an external server
     * it accessed in attempting to process the request. 408 (Request Timeout)
     * should be used instead if there was no response within the
     * period specified in the Expires header field from the upstream server.
     */
    public static final int SERVER_TIMEOUT = 504;
    
    /**
     * The server does not support, or refuses to support, the SIP protocol
     * version that was used in the request. The server is indicating that
     * it is unable or unwilling to complete the request using the same major
     * version as the client, other than with this error message.
     */
    public static final int VERSION_NOT_SUPPORTED = 505;
    
    /**
     * The server was unable to process the request since the message length
     * exceeded its capabilities.
     *
     * @since v1.1
     */
    public static final int MESSAGE_TOO_LARGE = 513;
    
    /**
     * The callee's end system was contacted successfully but the callee is
     * busy and does not wish to take the call at this time. The response
     * MAY indicate a better time to call in the Retry-After header field.
     * If the callee does not wish to reveal the reason for declining the call,
     * the callee uses status code 603 (Decline) instead. This status response
     * is returned only if the client knows that no other end point (such as a
     * voice mail system) will answer the request. Otherwise, 486 (Busy Here)
     * should be returned.
     */
    public static final int BUSY_EVERYWHERE = 600;
    
    /**
     * The callee's machine was successfully contacted but the user explicitly
     * does not wish to or cannot participate. The response MAY indicate a
     * better time to call in the Retry-After header field. This status
     * response is returned only if the client knows that no other end point
     * will answer the request.
     */
    public static final int DECLINE = 603;
    
    /**
     * The server has authoritative information that the user indicated in the
     * Request-URI does not exist anywhere.
     */
    public static final int DOES_NOT_EXIST_ANYWHERE = 604;
    
    /**
     * The user's agent was contacted successfully but some aspects of the
     * session description such as the requested media, bandwidth, or
     * addressing
     * style were not acceptable. A 606 (Not Acceptable) response means that
     * the user wishes to communicate, but cannot adequately support the
     * session described. The 606 (Not Acceptable) response MAY contain a list
     * of reasons in a Warning header field describing why the session
     * described cannot be supported.
     * <p>
     * A message body containing a description of media capabilities MAY be
     * present in the response, which is formatted according to the Accept
     * header field in the INVITE (or application/sdp if not present), the same
     * as a message body in a 200 (OK) response to an OPTIONS request.
     * <p>
     * It is hoped that negotiation will not frequently be needed, and when a
     * new user is being invited to join an already existing conference,
     * negotiation may not be possible. It is up to the invitation initiator to
     * decide whether or not to act on a 606 (Not Acceptable) response.
     * <p>
     * This status response is returned only if the client knows that no other
     * end point will answer the request. This specification renames this
     * status code from NOT_ACCEPTABLE as in RFC3261 to SESSION_NOT_ACCEPTABLE
     * due to it conflict with 406 (Not Acceptable) defined in this interface.
     */
    public static final int SESSION_NOT_ACCEPTABLE = 606;
    
    /**
     * Gets the reason phrase.
     * @param rc the reason code
     * @return the reason phrase as a string
     */
    public static String getReasonPhrase(int rc) {
        String retval = null;

        switch (rc) {
            case TRYING:
                retval = "Trying";
                break;

            case RINGING:
                retval = "Ringing";
                break;

            case CALL_IS_BEING_FORWARDED:
                retval = "Call is being forwarded";
                break;

            case QUEUED:
                retval = "Queued";
                break;

            case SESSION_PROGRESS:
                retval = "Session progress";
                break;

            case OK:
                retval = "OK";
                break;

            case ACCEPTED :
                retval = "Accepted";
                break;

            case MULTIPLE_CHOICES :
                retval = "Multiple choices";
                break;

            case MOVED_PERMANENTLY:
                retval = "Moved permanently";
                break;

            case MOVED_TEMPORARILY :
                retval = "Moved Temporarily";
                break;

            case USE_PROXY :
                retval = "Use proxy";
                break;

            case ALTERNATIVE_SERVICE :
                retval = "Alternative service";
                break;

            case BAD_REQUEST:
                retval = "Bad request";
                break;

            case UNAUTHORIZED :
                retval = "Unauthorized";
                break;

            case PAYMENT_REQUIRED :
                retval = "Payment required";
                break;

            case FORBIDDEN :
                retval = "Forbidden";
                break;

            case NOT_FOUND :
                retval = "Not found";
                break;

            case METHOD_NOT_ALLOWED :
                retval = "Method not allowed";
                break;

            case NOT_ACCEPTABLE:
                retval = "Not acceptable";
                break;

            case PROXY_AUTHENTICATION_REQUIRED :
                retval = "Proxy Authentication required";
                break;

            case REQUEST_TIMEOUT :
                retval = "Request timeout";
                break;

            case GONE :
                retval = "Gone";
                break;

            case TEMPORARILY_UNAVAILABLE :
                retval = "Temporarily Unavailable";
                break;

            case REQUEST_ENTITY_TOO_LARGE :
                retval = "Request entity too large";
                break;

            case REQUEST_URI_TOO_LONG :
                retval = "Request-URI too large";
                break;

            case UNSUPPORTED_MEDIA_TYPE:
                retval = "Unsupported media type";
                break;

            case UNSUPPORTED_URI_SCHEME:
                retval = "Unsupported URI Scheme";
                break;

            case BAD_EXTENSION :
                retval = "Bad extension";
                break;

            case EXTENSION_REQUIRED :
                retval = "Etension Required";
                break;

            case INTERVAL_TOO_BRIEF :
                retval = "Interval too brief";
                break;

            case CALL_OR_TRANSACTION_DOES_NOT_EXIST :
                retval = "Call leg/Transaction does not exist";
                break;

            case LOOP_DETECTED:
                retval = "Loop detected";
                break;

            case TOO_MANY_HOPS :
                retval = "Too many hops";
                break;

            case ADDRESS_INCOMPLETE:
                retval = "Address incomplete";
                break;

            case AMBIGUOUS:
                retval = "Ambiguous";
                break;

            case BUSY_HERE:
                retval = "Busy here";
                break;

            case REQUEST_TERMINATED:
                retval = "Request Terminated";
                break;

            case NOT_ACCEPTABLE_HERE:
                retval = "Not Accpetable here";
                break;

            case BAD_EVENT:
                retval = "Bad Event";
                break;

            case REQUEST_PENDING:
                retval = "Request Pending";
                break;

            case SERVER_INTERNAL_ERROR:
                retval = "Server Internal Error";
                break;

            case UNDECIPHERABLE :
                retval = "Undecipherable";
                break;

            case NOT_IMPLEMENTED :
                retval = "Not implemented";
                break;

            case BAD_GATEWAY :
                retval = "Bad gateway";
                break;

            case SERVICE_UNAVAILABLE :
                retval = "Service unavailable";
                break;

            case SERVER_TIMEOUT :
                retval = "Gateway timeout";
                break;

            case VERSION_NOT_SUPPORTED :
                retval = "SIP version not supported";
                break;

            case MESSAGE_TOO_LARGE:
                retval = "Message Too Large";
                break;

            case BUSY_EVERYWHERE :
                retval = "Busy everywhere";
                break;

            case DECLINE:
                retval = "Decline";
                break;

            case DOES_NOT_EXIST_ANYWHERE:
                retval = "Does not exist anywhere";
                break;

            case SESSION_NOT_ACCEPTABLE:
                retval = "Session Not acceptable";
                break;

            default:
                retval = "";
                break;
        }

        return retval;
    }
}
