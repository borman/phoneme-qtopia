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
 * DigestClientAuthentication.java
 *
 * Created on January 7, 2003, 10:45 AM
 */

package gov.nist.siplite.stack.authentication;

import java.util.Enumeration;
import java.util.Vector;

import gov.nist.core.*;
import gov.nist.siplite.*;
import gov.nist.siplite.parser.Lexer;
import gov.nist.siplite.message.*;
import gov.nist.siplite.header.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Digest Client Authentication.
 */
public class DigestClientAuthentication
    implements AuthenticationListener {
    /** MD5 value. */
    private static final String MD5 = "MD5";
    /** MD5-sess value. */
    private static final String MD5_SESS = "MD5-sess";
    /** Authorization category. */
    private String realm;
    /** Algorithm name. */
    private String algorithm;
    /** URI to be validated. */
    private String uri;
    /** Nonce. */
    private String nonce;
    /** Authorization method. */
    private String method;
    /** Client nonce. */
    private String cnonce;
    /** Qop. */
    private String qop;
    /** Cnonce counter value. */
    private String nonceCountPar;
    /** Credentials containing the keys. */
    private Vector credentials;

    /**
     * Constructor with initial credentials.
     * @param credentials array of credentials
     */
    public DigestClientAuthentication(Vector credentials) {
        this.credentials = credentials;
        /*
         * need revisit
                 try {
            rs = RecordStore.openRecordStore("pass", true);
                 }
                 catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "DigestClientAuthentication, " +
                    "exception raised: " + e.getMessage());
            }
                 }
         */
    }

    /**
     * Creates a new ProxyAuthorizationHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme - the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme value.
     * @return the newly created ProxyAuthorizationHeader object.
     */
    public ProxyAuthorizationHeader createProxyAuthorizationHeader
        (String scheme) throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("bad scheme arg");
        }

        ProxyAuthorizationHeader p = new ProxyAuthorizationHeader();
        p.setScheme(scheme);

        return p;
    }

    /**
     * Creates a new AuthorizationHeader based on the newly supplied
     * scheme value.
     *
     * @param scheme - the new string value of the scheme.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the scheme value.
     * @return the newly created AuthorizationHeader object.
     */
    public AuthorizationHeader createAuthorizationHeader(String scheme)
        throws ParseException {
        if (scheme == null) {
            throw new NullPointerException("null arg scheme ");
        }

        AuthorizationHeader auth = new AuthorizationHeader();
        auth.setScheme(scheme);

        return auth;
    }

    /**
     * Hexadecimal conversion table.
     */
    private static final char[] toHex = {
        '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'a', 'b', 'c', 'd',
        'e', 'f'};

    /**
     * Converts an array of bytes to an hexadecimal string.
     * @return a string
     * @param b bytes array to convert to a hexadecimal
     * string
     */
    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            c[pos++] = toHex[ (b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }
        return new String(c);
    }

    /**
     * Creates a new request.
     * @param sipStack the curent SIP stack context
     * @param originalRequest initiating request
     * @param response reply to original request
     * @param count number of request for nonce-count
     * (please see RFC 2617, 3.2.2)
     * @return the new request object with authentication
     * headers
     */
    public Request createNewRequest
        (SipStack sipStack, Request originalRequest,
         Response response, int count) {
        Exception ex = null;
        try {
            Request newRequest = (Request) originalRequest.clone();
            CSeqHeader cseqHeader = newRequest.getCSeqHeader();
            cseqHeader.setSequenceNumber(cseqHeader.getSequenceNumber() + 1);

            // Proxy-Authenticate header:
            ProxyAuthenticateHeader proxyAuthHeader =
                (ProxyAuthenticateHeader)
                response.getHeader(ProxyAuthenticateHeader.NAME);

            // WWWAuthenticate header:
            WWWAuthenticateHeader wwwAuthenticateHeader =
                (WWWAuthenticateHeader)
                response.getHeader(WWWAuthenticateHeader.NAME);

            // Cseq header:
            cseqHeader = response.getCSeqHeader();
            method = cseqHeader.getMethod();

            // RFC 2617, 3.2.2:
            // digest-uri
            //   The URI from Request-URI of the Request-Line
            uri = originalRequest.getRequestURI().toString();

            String opaque = null;

            if (proxyAuthHeader == null) {
                if (wwwAuthenticateHeader == null) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR,
                                       LogChannels.LC_JSR180,
                                       "DigestClientAuthentication, " +
                                       "ERROR: No ProxyAuthenticate header " +
                                       "or WWWAuthenticateHeader " +
                                       "in the response!");
                    }
                    return null;
                }

                algorithm = wwwAuthenticateHeader.getAlgorithm();

                nonce = wwwAuthenticateHeader.getNonce();
                realm = wwwAuthenticateHeader.getRealm();

                if (realm == null) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR,
                                       LogChannels.LC_JSR180,
                                       "DigestClientAuthentication, " +
                                       "ERROR: the realm is not part " +
                                       "of the 401 response!");
                    }
                    return null;
                }

                qop = wwwAuthenticateHeader.getParameter("qop");
                opaque = wwwAuthenticateHeader.getParameter("opaque");
            } else {

                algorithm = proxyAuthHeader.getAlgorithm();
                nonce = proxyAuthHeader.getNonce();
                realm = proxyAuthHeader.getRealm();

                if (realm == null) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR,
                                       LogChannels.LC_JSR180,
                                       "DigestClientAuthentication, " +
                                       "ERROR: the realm is not part " +
                                       "of the 407 response!");
                    }
                    return null;
                }

                qop = proxyAuthHeader.getParameter("qop");
                opaque = proxyAuthHeader.getParameter("opaque");
            }

            if (algorithm == null) {
                algorithm = MD5; // default value
            }

            if (!algorithm.equalsIgnoreCase(MD5) &&
                !algorithm.equalsIgnoreCase(MD5_SESS)) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR,
                                 LogChannels.LC_JSR180,
                                 "Algorithm parameter is wrong: " + algorithm);
                }
                return null;
            }

            Credentials credentials = getCredentials(realm);
            if (credentials == null) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR,
                                   LogChannels.LC_JSR180,
                                   "DigestClientAuthentication, " +
                                   "ERROR: unable to retrieve " +
                                   "the credentials from RMS!");
                }
                return null;
            }

            if (nonce == null) {
                nonce = "";
            }

            String digestEntityBody = null;
            if (qop != null) {
                cnonce = toHexString(Utils.digest(
                    ("" + System.currentTimeMillis() + ":ETag:" +
                     credentials.getPassword()).getBytes()));
                if (qop.equalsIgnoreCase("auth-int")) {
                    String entityBody =
                        new String(originalRequest.getRawContent());
                    if (entityBody == null) {
                        entityBody = "";
                    }
                    digestEntityBody = toHexString(
                        Utils.digest(entityBody.getBytes()));
                }
            }

            AuthenticationHeader header = null;

            if (proxyAuthHeader == null) {
                header =
                    createAuthorizationHeader("Digest");
            } else {
                header =
                    createProxyAuthorizationHeader("Digest");
            }

            header.setParameter("username",
                                credentials.getUserName());
            header.setParameter("realm", realm);
            header.setParameter("uri", uri);
            header.setParameter("algorithm", algorithm);
            header.setParameter("nonce", nonce);

            if (qop != null) {
                // RFC 2617, 3.2.2
                // qop contains a comma-separated list
                // we should find "auth" or "auth-int"
                Lexer qopLexer = new Lexer("qop", qop);
                boolean foundAuth = false;
                String currToken;
                while (qopLexer.lookAhead(0) != '\0') {
                    currToken =
                        qopLexer.byteStringNoComma().toLowerCase();
                    if (currToken.equals("auth") ||
                        currToken.equals("auth-int")) {
                        foundAuth = true;
                        qop = currToken;
                        break;
                    }
                }
                if (!foundAuth) { // wrong qop value
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING,
                                       LogChannels.LC_JSR180,
                                       "DigestClientAuthentication, " +
                                       "the digest response is null " +
                                       "for the Authorization header!");
                    }
                    return null;
                }
                header.setParameter("qop", qop);
                // RFC 2617, 3.2.2
                header.setParameter("cnonce", cnonce);
                // constructing 8LHEX
                String nonceCount = Integer.toHexString(count);
                nonceCountPar = "";
                int lengthNonceCount = nonceCount.length();
                if (lengthNonceCount < 8) {
                    for (int i = lengthNonceCount; i < 8; i++) {
                        nonceCountPar += "0";
                    }
                }
                nonceCountPar += nonceCount;
                header.setParameter("nc", nonceCountPar);
            }

            String digestResponse =
                generateResponse(
                    credentials.getUserName(),
                    credentials.getPassword(),
                    digestEntityBody);

            if (digestResponse == null) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING,
                                   LogChannels.LC_JSR180,
                                   "DigestClientAuthentication, " +
                                   "the digest response is null " +
                                   "for the Authorization header!");
                }
                return null;
            }

            header.setParameter("response", digestResponse);

            if (opaque != null) {
                header.setParameter("opaque", opaque);
            }

            newRequest.setHeader(header);
            return newRequest;
        } catch (ParseException pe) {
            ex = pe;
        } catch (javax.microedition.sip.SipException se) {
            ex = se;
        }
        if (ex != null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "createNewRequest() " +
                               "exception raised: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Generates the response message.
     * @param userName user name for authentication
     * @param password password for authentication
     * @param digestEntityBody MD5 value of body
     * @return the new response message
     */
    private String generateResponse(String userName, String password,
                                    String digestEntityBody) {
        if (userName == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no userName parameter");
            }
            return null;
        }

        if (realm == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no realm parameter");
            }
            return null;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(): " +
                           "Trying to generate a response " +
                           "for the user: " +
                           userName +
                           " , with " +
                           "the realm: " +
                           realm);
        }

        if (password == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no password parameter");
            }
            return null;
        }

        if (method == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no method parameter");
            }
            return null;
        }

        if (uri == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no uri parameter");
            }
            return null;
        }

        if (nonce == null) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                               "DigestClientAuthentication, " +
                               "generateResponse(): " +
                               "ERROR: no nonce parameter");
            }
            return null;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), userName: " +
                           userName + "!");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), realm: " + realm + "!");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), password: " + password + "!");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), uri: " + uri + "!");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), nonce: " + nonce + "!");
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), method: " + method + "!");
        }

        // RFC 2617, 3.2.2.2
        String A1 = userName + ":" + realm + ":" + password;
        if (algorithm.equalsIgnoreCase(MD5_SESS)) {
            byte[] A1bytes = Utils.digest(A1.getBytes());
            byte[] tmp = (":" + nonce + ":" + cnonce).getBytes();
            byte[] join = new byte[A1bytes.length + tmp.length];
            System.arraycopy(A1bytes, 0, join, 0, A1bytes.length);
            System.arraycopy(tmp, 0, join, A1bytes.length, tmp.length);
            A1 = new String(join);
        }
        String A2 = method.toUpperCase() + ":" + uri;

        // RFC 2617, 3.2.2.3 - body is empty
        if (qop != null) {
            if (qop.equalsIgnoreCase("auth-int")) {
                A2 += ":" + digestEntityBody;
            }
        }

        byte mdbytes[] = Utils.digest(A1.getBytes());

        String HA1 = toHexString(mdbytes);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), HA1:" + HA1 + "!");
        }

        mdbytes = Utils.digest(A2.getBytes());

        String HA2 = toHexString(mdbytes);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(), HA2: " + HA2 + "!");
        }

        String KD = HA1 + ":" + nonce;
        if (qop != null) { // RFC 2617, 3.2.2.1
            KD += ":" + nonceCountPar + ":" + cnonce + ":" + qop;
        }
        KD += ":" + HA2;

        mdbytes = Utils.digest(KD.getBytes());
        String response = toHexString(mdbytes);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "DigestClientAuthentication, " +
                           "generateResponse(): " +
                           "response generated: " + response);
        }

        return response;
    }

    /**
     * Gets the credentials to use int the authentication request.
     * @param realm the domain of the requested credentials
     * @return the requested credentials
     */
    public Credentials getCredentials(String realm) {
        Enumeration e = credentials.elements();

        while (e.hasMoreElements()) {
            Credentials credentials = (Credentials) e.nextElement();
            if (credentials.getRealm().equals(realm)) {
                return credentials;
            }
        }

        return null;

        /*
         * need revisit
                 try {
            byte[] recData = new byte[200];
            int len;

            for (int i = 1; i <= rs.getNumRecords(); i++) {
                len = rs.getRecord(i, recData, 0);
                String data = new String(recData, 0, len);

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "DigestClientAuthentication, " +
                        "data recovered from RMS: " + data);
                }

                int realmIndex = data.indexOf(":");
                String rmsRealm = data.substring(0, realmIndex);
                int userNameIndex = data.indexOf(":", realmIndex+1);
                String userName = data.substring(realmIndex+1, userNameIndex);

                if (realm.equals(rmsRealm.trim())) {
                    Credentials credentials =
                    new Credentials(realm, userName,
                            data.substring(userNameIndex + 1));

                    return credentials;
                }
            }
            return null;
                 }
                 catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "DigestClientAuthentication, getCredentials() " +
                    "exception raised:", e.getMessage());
            }
            return null;
                 }
         */
    }
}
