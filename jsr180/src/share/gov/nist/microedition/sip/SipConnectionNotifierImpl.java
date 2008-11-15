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
 * SipConnectionNotifierImpl.java
 *
 * Created on Jan 29, 2004
 *
 */
package gov.nist.microedition.sip;

import java.util.Vector;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connection;

import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipDialog;
import javax.microedition.sip.SipException;
import javax.microedition.sip.SipServerConnection;
import javax.microedition.sip.SipServerConnectionListener;

import gov.nist.siplite.ListeningPoint;
import gov.nist.siplite.ObjectInUseException;
import gov.nist.siplite.SipProvider;
import gov.nist.siplite.SipStack;
import gov.nist.siplite.message.Request;
import gov.nist.siplite.stack.Dialog;
import gov.nist.siplite.stack.ServerTransaction;
import gov.nist.siplite.header.*;
import gov.nist.siplite.address.*;
import com.sun.j2me.security.Token;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

import com.sun.cdc.io.j2me.sip.ProtocolBase;
import com.sun.j2me.security.SIPPermission;

/**
 * SIP Connection notifier implementation.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class SipConnectionNotifierImpl
        implements SipConnectionNotifier { // , Runnable {
    /**
     * Security token for SIP/SIPS protocol class
     */
    private Token classSecurityToken;
    /**
     * Listener interface for incoming SIP requests.
     */
    private SipServerConnectionListener sipServerConnectionListener = null;
    /**
     * Messages received held in this vector
     */
    private Vector messageQueue = null;
    /**
     * flag to know the state of the connection (open or close)
     */
    private boolean connectionOpen;
    /**
     * listen address
     */
    private String localHost = null;
    /**
     * port number
     */
    private int localPort;
    /**
     * The Sip Provider for this connection Notifier
     */
    private SipProvider sipProvider = null;
    /**
     * Asynchronous notifier thread handle.
     */
    private Thread listeningThread = null;
    /**
     * Stack of associtaed connectors.
     */
    private StackConnector stackConnector = null;
    /**
     * Type value from 
     * connector.open(...type="application/vnd.company.battleships"...)
     */
    private String mimeType = null;
    /**
     * Boolean flag to indicate that this object is in waiting state during 
     * acceptAndOpen()
     */
    private boolean waitingForAcceptAndOpen = false;

    /**
     * Indicates whether this SipConnectionNotifier is opened in shared mode
     * or not
     */
    boolean sharedMode = false;
    
    /**
     * Indicates whether the secure layer is used
     */
    boolean isSecure = false;
    
    /**
     * Constructor called by the Connector.open() method
     * @param sipProvider the network service provider
     * @param localAddress the local connection end point
     * @param localPort the port number on which the listener will wait for
     * incoming messages
     * @param classSecurityToken Security Token from SIP/SIPS protocol class
     * @param mimeType MIMEtype associated with SipConnectionNotifier; used for 
     *         filtering incomming SIP packets; null if not available
     * @param sharedMode Flag to indicate that SipConnectionNotifier is in
     *        shared Mode
     * @throws IOException if an I/OO error is detected
     */
    protected SipConnectionNotifierImpl(
            SipProvider sipProvider,
            String localAddress,
            int localPort,
            Token classSecurityToken,
            String mimeType,
            boolean sharedMode,
            boolean isSecure) {
        this.classSecurityToken = classSecurityToken;
        this.sipProvider = sipProvider;
        this.localHost = localAddress;
        this.localPort = localPort;
        this.mimeType = mimeType;
        this.sharedMode = sharedMode;
        this.isSecure = isSecure;
        
        // Setting default value to attributes
        connectionOpen = true;
        messageQueue = new Vector();
        try {
            stackConnector = StackConnector.getInstance(classSecurityToken);
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                    "can't create SipConnectionNotifier : " + ioe);
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Accept and open the client connection.
     * @return connection handle
     * @see javax.microedition.sip.SipConnectionNotifier#acceptAndOpen()
     */
    public SipServerConnection acceptAndOpen()
    throws IOException, SipException {
        if (!isConnectionOpen()) {
            throw new InterruptedIOException("Connection was closed!");
        }

        waitingForAcceptAndOpen = true;
        
        // IMPL_NOTE : handle the two others exceptions

        // create the sipServerConnection when a request is received
        // through the processRequest method the processRequest method
        // will add the Request in the Queue and notify()
        if (messageQueue == null || messageQueue.size() < 1) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "SipConnectionNotifierImpl.acceptAndOpen() :" +
                                "InterruptedException during wait() : " + ie);
                        ie.printStackTrace();
                    }
                } catch (IllegalMonitorStateException imse) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "SipConnectionNotifierImpl.acceptAndOpen() :" +
                                "Illgeal monitor state during wait(): " + imse);
                       imse.printStackTrace();
                    }   
                }
            }
        }
        
        if (!isConnectionOpen()) {
            throw new InterruptedIOException("Connection is interrupted!");
        }

        waitingForAcceptAndOpen = false;
                
        // Get the request received from the message queue
        Request request = (Request)messageQueue.firstElement();
        // Set up the dialog
        SipDialog sipDialog = null;
        ServerTransaction serverTransaction =
                (ServerTransaction)request.getTransaction();
        // Get the nist-siplite dialog
        Dialog dialog = serverTransaction.getDialog();

        // If the method is an INVITE, SUBSCRIBE or REFER, we create
        // a new dialog and add it to the list of dialog we have.
        String method = request.getMethod();
        
        if (stackConnector.getSipStack().isDialogCreated(method)) {
            // request URI
            URI reqURI = null;
            ContactList cl = request.getContactHeaders();
            if (cl != null) {
                if (cl.size() > 0) {
                    ContactHeader ch = (ContactHeader)cl.getFirst();
                    Address addr = (Address)ch.getValue();
                    if (addr != null) {
                        reqURI = addr.getURI();
                    }
                }
            }
        
            if (reqURI == null) {
                reqURI = request.getFromHeader().getAddress().getURI();
            }
            sipDialog = new SipDialogImpl(
                    dialog,
                    this,
                    classSecurityToken);
            stackConnector.sipDialogList.addElement(sipDialog);

            if (method.equals(Request.INVITE)) {
                ((SipDialogImpl)sipDialog).setWaitForBye(true);
            }
        } else if ((dialog != null) &&
                   (!request.getMethod().equals(Request.CANCEL))) {
            sipDialog = stackConnector.findDialog(dialog.getDialogId());
        }
        
        /* Check the suite is permitted to use a SIPS/SIP connection. */
        if (isSecure) {
            ProtocolBase.checkForPermission(localHost + ":" + localPort, "sips", 
                    SIPPermission.SIPS_CONNECTION.getName());
        } else {
            ProtocolBase.checkForPermission(localHost + ":" + localPort, "sip", 
                    SIPPermission.SIP_CONNECTION.getName());
        }
        
        SipServerConnection sipServerConnection =
                new SipServerConnectionImpl(
                request,
                sipDialog,
                this);
        // We remove the request from the queue
        messageQueue.removeElementAt(0);

        return sipServerConnection;
    }

    /**
     * Sets a listener for incoming SIP requests. If a listener is
     * already set it
     * will be overwritten. Setting listener to null will remove the current
     * listener.
     * @param sscl  listener for incoming SIP requests
     * @throws IOException  if the connection was closed
     */
    public void setListener(SipServerConnectionListener sscl)
    throws IOException {
        if (!isConnectionOpen())
            throw new IOException("Connection was closed!");
        this.sipServerConnectionListener = sscl;

    }

    /**
     * Gets the local IP address for this SIP connection.
     * @return local IP address. Returns null if the address is not available.
     * @throws IOException - if the connection was closed
     */
    public String getLocalAddress() throws IOException {
        if (!isConnectionOpen())
            throw new IOException("Connection was closed!");
        return localHost;
    }

    /**
     * Gets the local port for this SIP connection.
     * @return local port number, that the notifier is listening to.
     * Returns 0 if the port is not available.
     * @throws IOException - if the connection was closed
     */
    public int getLocalPort() throws IOException {
        if (!isConnectionOpen())
            throw new IOException("Connection was closed!");
        return this.localPort;
    }

    /**
     * Closes the connection notifier handling.
     * @exception IOException if an error occurs while terminating
     * @see javax.microedition.io.Connection#close()
     */
    public void close() throws IOException {
        if (!isConnectionOpen()) { // connection is already closed
            return;
        }
        // stop the listening points and sipProvider
        if (sharedMode) {
            stackConnector.closeSharedSipConnectionNotifier(mimeType);
        } else {
            SipStack sipStack = sipProvider.getSipStack();
            ListeningPoint listeningPoint = sipProvider.getListeningPoint();

            try {
                sipStack.deleteListeningPoint(listeningPoint);
                sipStack.deleteSipProvider(sipProvider);
            } catch (ObjectInUseException oiue) {
                throw new IOException(oiue.getMessage());
            }
            /*
             * sipStack should be closed only if there are no more
             * associated listening points
             */
            if (!sipStack.getListeningPoints().hasMoreElements()) {
                sipStack.stopStack();
            }
        }
        
        // Removing the connection from the connection list held by
        // the stackConnector
        stackConnector.connectionNotifiersList.removeElement(this);


        // Notification that the connection has been closed
        connectionOpen = false;
        
        if (waitingForAcceptAndOpen) {
            synchronized (this) {
                try {
                    notify();
                } catch (IllegalMonitorStateException imse) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "SipConnectionNotifierImpl.close() :" +
                                "Illgeal monitor state during wait(): " + imse);
                       imse.printStackTrace();
                    }   
                }
            }
        }
        // listeningThread = null;
    }

    /**
     *
     */

    /**
     * The stack connector notifies this class when it receive a new request
     * @param request - the new received request
     */
    protected void notifyRequestReceived(Request request) {
        messageQueue.addElement(request);
        // We notify the listener that a request has been received
        if (this.sipServerConnectionListener != null)
            try { // The user code is called 
                sipServerConnectionListener.notifyRequest(this);
            } catch (Throwable exc) { // Ignore any user exception
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR180,
                        "Exception in notifyRequest() method has been thrown");
                }
            }
        synchronized (this) {
            try {
                notify();
            } catch (IllegalMonitorStateException imse) {
                imse.printStackTrace();
            }
        }
    }

    /**
     * Gets the sip provider.
     * @return the sip provider
     */
    protected SipProvider getSipProvider() {
        return sipProvider;
    }
    
    /**
     * Gets the mode od the connection.
     * @return true when the mode is shared
     */
    protected boolean isSharedMode() {
        return sharedMode;
    }

    /**
     * Gets the connection state.
     * @return true when connection is opened
     */
    protected boolean isConnectionOpen() {
        return connectionOpen;
    }

    /**
     * Get the MIME type for this SipConnectionNotifier
     *
     * @return MIMEtype of the SipConnectionNotifier
     */
    protected String getMIMEType() {
        return mimeType;
    } 
    
    /**
     * Gets the current stack connector.
     * @return the current stack connector
     */
    protected StackConnector getStackConnector() {
        return stackConnector;
    }

}
