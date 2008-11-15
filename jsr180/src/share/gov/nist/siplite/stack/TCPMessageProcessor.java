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
package gov.nist.siplite.stack;

import java.io.*;
import java.util.Vector;

import javax.microedition.io.*;
import javax.microedition.io.SocketConnection;

import com.sun.j2me.log.LogChannels;
import com.sun.j2me.log.Logging;

import gov.nist.core.*;
import gov.nist.siplite.SIPConstants;

/**
 * TCP Message Processor.
 *
 * @version 1.0
 */
public class TCPMessageProcessor extends MessageProcessor implements Runnable {
    /** Local Port number. */
    private int localPort = SIPConstants.DEFAULT_NONTLS_PORT;
    /** Inbound server socket connection. */
    private ServerSocketConnection serverSocket;
    /** Current error socket disabled. */
    public boolean ERROR_SOCKET = false;
    /* Max size. */
    // private static final int MAX_LENGTH = 1000;
    /** Our stack (that created us). */
    private SIPMessageStack sipStack;
    /** Thread of TCP server. */
    private Thread incomingHandler;
    /** Array of registered message channels. */
    private Vector tcpMsgChannels = new Vector();
    
    /**
     * Main loop for TCP message handling.
     */
    public void run() {
        TCPMessageChannel tmc;
        try {
            while (!exitFlag) {
                SocketConnection sock =
                        (SocketConnection) serverSocket.acceptAndOpen();
                if (sock != null) {
                    tmc = new TCPMessageChannel(sock, sipStack, this);
					tcpMsgChannels.addElement(tmc);
				}
            }
        } catch (IOException ex) {
            /*
             * If IOEXception occurs after main thread closes serversocket, 
             * ignore it
             */
            if (!exitFlag) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "Server socket exception. "+
                        "Shutting down of incoming handler thread!");
                }
            }
        }
    }
    
    
    /**
     * Gets the SIP stack context.
     * @return the currenit SIP stack
     */
    public SIPMessageStack getSIPStack() {
        return sipStack;
    }
    
    /**
     * Creates new TCPMessageProcessor.
     * @param ss the current SIP stack context
     * @param lp Local port number for messages
     */
    public TCPMessageProcessor(SIPMessageStack ss, int lp) {
        sipStack = ss;
        localPort = lp;        
    }
    
    /**
     * Gets the transport string.
     * @return A string that indicates the transport.
     * (i.e. "tcp" or "udp")
     */
    public String getTransport() {
        return SIPConstants.TRANSPORT_TCP;
    }
    
    /**
     * Gets the local port identifier.
     * @return the port for this message processor.
    */
    public int getPort() {
        return localPort;
    }
    
    /**
     * Gets the SIP Stack.
     * @return the sip stack.
     */
    public SIPMessageStack getSipStack() {
        return sipStack;
    }
    
    /**
     * Creates and return new TCPMessageChannel for the given host/port.
     * @param targetHostPort target host and port address
     * @return a new message channel
     */

    public MessageChannel createMessageChannel(HostPort targetHostPort)
    throws IOException {
        TCPMessageChannel tmc;
        synchronized (tcpMsgChannels) {
            for (int i = 0; i < tcpMsgChannels.size(); i++) {
                tmc = ((TCPMessageChannel)tcpMsgChannels.elementAt(i));
                if (tmc.getPeerHostPort().equals(targetHostPort)) {
                    tmc.incrementUseCounter();
                    return tmc;
                }
            }
        }
        tmc = new TCPMessageChannel(targetHostPort, sipStack, this);
        tcpMsgChannels.addElement(tmc);
        return tmc;
    }
    
    
    /**
     * Checks if the currenttransport is secure channel
     * @return always false
     */
    public boolean isSecure() {
        return false;
    }
    
    /**
     * Starts our thread.
     * @exception IOException if the connection failed
     */
    synchronized public void start() throws IOException {
        /*
         * Original NIST method for opening the serversocket connection
         * has been replaced by direct calls to instantiate the protocol 
         * handler, in order to pass the security token for use of lower 
         * level transport connection. 
         * Original NIST sequence is :
         *
         * serverSocket =  (ServerSocketConnection)
         *    Connector.open("socket://:"+getPort());
         */

        serverSocket = com.sun.j2me.conn.Connector.getServerSocketConnection(sipStack.getSecurityToken(), "//:" + localPort);
        
        // A serversocket needs to be created to handle incoming data.
        // This part of the code is used while creating 
        // a SipConnectionNotifier with TCP transport
        // 
        // Also under error conditions, the server may attempt to open 
        // a new connection to send the response. To handle this case,
        // the transport layer MUST also be prepared to receive
        // an incoming connection on the source IP address from which
        // the request was sent and port number in 
        // the "sent-by" field (RFC3261, p.143)
        incomingHandler = new Thread(this);
        incomingHandler.start();
    }
    
    /**
     * Stops the current processor.
     */
    synchronized public void stop() {
        try {
            exitFlag = true;
            if (serverSocket != null) {
                serverSocket.close();
                try {
                    incomingHandler.join();
                } catch (InterruptedException exc) { 
                    // ignore
                }
                serverSocket = null;
            }
            Object[] arr;
            synchronized (tcpMsgChannels) {
                arr = new Object[tcpMsgChannels.size()];
                tcpMsgChannels.copyInto(arr);
            }
            TCPMessageChannel tmc;
            for (int i = 0; i < arr.length; i++) {
                tmc = (TCPMessageChannel)arr[i];
                tmc.exit();
            }
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "SYSTEM, TCPMessageProcessor, stop(), " +
                    "exception raised: " + ex);
            }
        }
    }

    /**
     * Notification about message channel shutting down. 
     * Now it cat be removed from registration vector.
     * @param tmc channel that was closed
     */
    public void notifyClose(TCPMessageChannel tmc) {
        tcpMsgChannels.removeElement(tmc);
    }
}
