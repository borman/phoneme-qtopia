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
 * AuthenticationProcess.java
 *
 * Created on January 7, 2003, 5:30 PM
 */

package gov.nist.siplite.stack.authentication;

/**
 * Authentication process.
 */
public class AuthenticationProcess {
    
    /** Creates a new instance of AuthenticationProcess */
    public AuthenticationProcess() {
        
    }
    /*
     * need revisit
    public Header getHeader(Response response) {
    try {
     
        // Proxy-Authorization header:
        ProxyAuthenticateHeader authenticateHeader =
        (ProxyAuthenticateHeader)
        response.getHeader(ProxyAuthenticateHeader.NAME);
     
        WWWAuthenticateHeader wwwAuthenticateHeader = null;
        CSeqHeader cseqHeader =
        (CSeqHeader)response.getHeader(CSeqHeader.NAME);
     
        String cnonce = null;
        String uri = "sip:"+imUA.getRegistrarAddress()+":"
        + imUA.getRegistrarPort();
        String method = cseqHeader.getMethod();
        String userName = null;
        String password = null;
        String nonce = null;
        String realm = null;
        String qop = null;
     
        if (authenticateHeader == null) {
        wwwAuthenticateHeader = (WWWAuthenticateHeader)
            response.getHeader(WWWAuthenticateHeader.NAME);
     
        nonce = wwwAuthenticateHeader.getNonce();
        realm = wwwAuthenticateHeader.getRealm();
        if (realm == null) {
            DebugIM.println("AuthenticationProcess,"
                    + " getProxyAuthorizationHeader(), "+
                    " ERROR: the realm is not part "
                    + "of the 401 response!");
            return null;
        }
        cnonce = wwwAuthenticateHeader.getParameter("cnonce");
        qop = wwwAuthenticateHeader.getParameter("qop");
        } else {
     
        nonce = authenticateHeader.getNonce();
        realm = authenticateHeader.getRealm();
        if (realm == null) {
            DebugIM.println("AuthenticationProcess,"
                    + " getProxyAuthorizationHeader(), "+
                    " ERROR: the realm is not part"
                    + " of the 407 response!");
            return null;
        }
        cnonce = authenticateHeader.getParameter("cnonce");
        qop = authenticateHeader.getParameter("qop");
        }
     
        HeaderFactory headerFactory = imUA.getHeaderFactory();
     
        DigestClientAuthenticationMethod digest =
        new DigestClientAuthenticationMethod();
        digest.initialize(realm, userName, uri, nonce,
                  password, method, cnonce, "MD5");
     
        if (authenticateHeader == null) {
        AuthorizationHeader header =
            headerFactory.createAuthorizationHeader("Digest");
        header.setParameter("username", userName);
        header.setParameter("realm", realm);
        header.setParameter("uri", uri);
        header.setParameter("algorithm", "MD5");
        header.setParameter("opaque", "");
        header.setParameter("nonce", nonce);
        header.setParameter("response", digest.generateResponse());
        if (qop != null)
            header.setParameter("qop", qop);
     
        return header;
        } else {
        ProxyAuthorizationHeader header =
            headerFactory.createProxyAuthorizationHeader("Digest");
        header.setParameter("username", userName);
        header.setParameter("realm", realm);
        header.setParameter("uri", uri);
        header.setParameter("algorithm", "MD5");
        header.setParameter("opaque", "");
        header.setParameter("nonce", nonce);
        header.setParameter("response", digest.generateResponse());
        if (qop != null)
            header.setParameter("qop", qop);
     
        return header;
        }
    }
    catch (Exception ex) {
        ex.printStackTrace();
        return null;
    }
    }
    */
}
