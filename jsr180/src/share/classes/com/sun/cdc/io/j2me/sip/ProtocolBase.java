/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cdc.io.j2me.sip;

import com.sun.j2me.security.SecurityTokenInitializer;
import com.sun.j2me.security.TrustedClass;
import com.sun.j2me.security.Token;
import com.sun.j2me.io.ConnectionBaseInterface;
import gov.nist.core.ParseException;
import gov.nist.microedition.sip.StackConnector;
import gov.nist.siplite.address.SipURI;
import gov.nist.siplite.parser.URLParser;
import gov.nist.siplite.SIPConstants;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.sip.SipException;

import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.SIPPermission;

import gov.nist.core.PushUtil;

/**
 * This class implements the base necessary functionality
 * for a SIP connection. Classes sip.Protocol and sips.Protocol
 * are subclasses of ProtocolBase class
 * This class is a thin wrapper around the NIST
 * JSR180 implementation of the sip URI
 * protocol handler.
 * This class handles the security token
 * intialization and invokes the NISt handler.
 */

public abstract class ProtocolBase 
    implements ConnectionBaseInterface {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements TrustedClass {};

    /** Security token for SIP/SIPS protocol class */
    private static Token classSecurityToken =
        SecurityTokenInitializer.requestToken(new SecurityTrusted());

    /**
     * Checks if transport protocol is supported.
     *
     * @param protocol protocol name
     *
     * @return true when protocol is supported
     */
    public boolean isProtocolSupported(String protocol) {
        if (protocol == null) {
          return false;
        } else {
            return protocol.equalsIgnoreCase(SIPConstants.TRANSPORT_UDP) ||
                protocol.equalsIgnoreCase(SIPConstants.TRANSPORT_TCP);
        }
    }

    /**
     * Sets up the state of the connection, but
     * does not actually connect to the server until there's something
     * to do.
     * <p>
     * @param name the URL for the connection, without the
     *  without the protocol part.
     * @param mode the access mode, ignored
     * @param timeouts flag to indicate that the caller wants
     * timeout exceptions, ignored
     * @return reference to this connection
     * @exception IllegalArgumentException if a parameter is invalid
     * @exception ConnectionNotFoundException if the connection cannot be
     * found
     * @exception IOException if some other kind of I/O error occurs
     */
    public abstract Connection openPrim(String name, int mode, boolean timeouts)
    throws IOException, IllegalArgumentException,
            ConnectionNotFoundException;

    /**
     * The Connector convenience methods to gain access to a specific
     * input or output stream directly are not
     * supported by the SIP API. The implementations MUST
     * throw IOException if these methods are called with SIP URIs.
     */

    /**
     * Throw IOException on call openInputStream() method.
     *
     * @return  nothing
     * @exception  IOException always throws
     *
     */
    public InputStream openInputStream() throws IOException {
        throw new IOException("SIP connection doesn't support input stream");
    }


    /**
     * Throw IOException on call openInputStream() method.
     *
     * @return  nothing
     * @exception  IOException always throws
     *
     */
    public OutputStream openOutputStream() throws IOException {
        throw new IOException("SIP connection doesn't support output stream");
    }


    /**
     * Throw IOException on call openInputStream() method.
     *
     * @return  nothing
     * @exception  IOException always throws
     *
     */
    public DataInputStream openDataInputStream() throws IOException {
        throw new IOException("SIP connection doesn't support input stream");
    }


    /**
     * Throw IOException on call openInputStream() method.
     *
     * @return  nothing
     * @exception  IOException always throws
     *
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        throw new IOException("SIP connection doesn't support output stream");
    }

    /**
     * Checks for the required permission.
     * @param name  resource to insert into the permission question
     * @param protocol  protocol name
     * @param permission  value of the permission constant
     * @exception InterruptedIOException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    static public void checkForPermission(String name, String protocol,
            String permission) throws InterruptedIOException {
        name = protocol + ":" + name;
        try {
            AppPackage.getInstance().checkForPermission(new SIPPermission(permission,
                    name));
        } catch (InterruptedException ie) {
            throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
        }
    }

    /**
     * Open a client or server socket connection.
     *
     * @param name             the [{target}][{params}] for the connection
     * @param scheme           the scheme name (sip or sips)
     *
     * @return client or server SIPS connection
     *
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be
     * connected to
     * @exception  IllegalArgumentException  if the name is malformed
     */
    protected Connection openConn(String name, String scheme)
    throws IOException, IllegalArgumentException, SipException {
        
        Connection retval = null;
        URLParser parser = new URLParser(scheme + ":" + name);
        SipURI inputURI = null;
        
        try {
            inputURI = (SipURI)parser.parseWholeString();
        } catch (ParseException exc) {
            throw new IllegalArgumentException(exc.getMessage());
        }
        
        String transport = inputURI.getTransportParam();
        if (transport == null) { // get default transport name
            transport =  SIPConstants.TRANSPORT_UDP;
        } else if (!transport.equalsIgnoreCase(SIPConstants.TRANSPORT_UDP) &&
            !transport.equalsIgnoreCase(SIPConstants.TRANSPORT_TCP)) {
            throw new SipException(SipException.TRANSPORT_NOT_SUPPORTED);
        }
        
        int portNum = inputURI.getPort();
        
        boolean isSecure = inputURI.isSecure();
        
        StackConnector stackConnector =
            StackConnector.getInstance(classSecurityToken);
        
        if (inputURI.isServer()) { // server URI
            String mimeType = inputURI.getTypeParam();
            if (inputURI.isShared()) { // sip:*;type="application/..."
                if (mimeType == null) { // no type parameter
                    throw new IllegalArgumentException("No type parameter "
                        + "in shared URI");
                }

                /* Used for MIDP/CDC Push functionality */
                retval = (Connection)PushUtil.getPushConnection(stackConnector, 
                        scheme + ":" + name, mimeType);
                if (retval == null) {
                    retval = (Connection)stackConnector.
                        createSharedSipConnectionNotifier(
                            isSecure,
                            transport,
                            mimeType);
                }
            } else { // dedicated sip:5060;...
                /* Used for MIDP/CDC Push functionality */
                retval = (Connection)PushUtil.getPushConnection(stackConnector, 
                        scheme + ":" + name, portNum, mimeType);
                if (retval == null) {
                    retval = (Connection)stackConnector.
                        createSipConnectionNotifier(
                            portNum,
                            isSecure,
                            transport,
                            mimeType);
                }
            }
        } else { // client URI

            if (portNum == -1) { // set default port
                inputURI.setPort(SIPConstants.DEFAULT_NONTLS_PORT);
            }

            retval = (Connection) stackConnector.
                createSipClientConnection(inputURI);
        }
        return retval;
    }

}

