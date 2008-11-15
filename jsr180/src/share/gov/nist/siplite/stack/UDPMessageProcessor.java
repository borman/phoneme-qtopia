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

import javax.microedition.io.*;
import gov.nist.core.*;
import java.io.IOException;
import java.io.InterruptedIOException;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Sit in a loop and handle incoming udp datagram messages. For each Datagram
 * packet, a new UDPMessageChannel is created. (Each UDP message is processed
 * in its own thread).
 */
public class UDPMessageProcessor extends MessageProcessor implements Runnable {
    /** Flag indicating the mesage processor is already running. */
    private  boolean running;
    /** Max datagram size.  */
    protected static final int MAX_DATAGRAM_SIZE = 1500;
    /** Our stack (that created us). */
    private SIPMessageStack sipStack;
    /** Current datagram connection handle. */
    private DatagramConnection dc = null;
    /** Inbound message port number. */
    int port;
    /** Message processor thread. */
    private Thread thread;

    /**
     * Constructor with initial stack and port number.
     * @param sipStack pointer to the stack.
     * @param port for incoming messages
     */
    protected UDPMessageProcessor(SIPMessageStack sipStack,
                                  int port) {
        this.sipStack = sipStack;
        this.port = port;        
    }

    /**
     * Starts our processor thread.
     */
    synchronized public void start() {
        /*
         * Creating datagram connection.
         */
        // Create a new datagram socket.
        /*
         * Original NIST method for opening the datagram connection
         * has been replaced by direct calls to instantiate the protocol
         * handler, in order to pass the security token for use of lower
         * level transport connection.
         * Original NIST sequence is :
         *
         * dc =
         *       (DatagramConnection)Connector.open("datagram://:"+
         *       sipStack.getPort("udp"));
         *
         */

        if (dc != null) {
            // the mesage processor is already running
            return;
        }

        try {
            // It's incorrect to use sipStack.getPort() to get the port
            // for listening, because if multiple ports are enabled for
            // the same transport then this function returns the first one.
            // So the member 'port' is used.
            dc = com.sun.j2me.conn.Connector.getDatagramConnection(
                               sipStack.getSecurityToken(), "//:" +
                               port, Connector.READ_WRITE, true);
            // timeouts are ignored

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "UDPMessageProcessor, start(), datagram server" +
                           "listener:  datagram://:" + port);
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                           "UDPMessageProcessor, start(), datagram size:" +
                           MAX_DATAGRAM_SIZE);
            }
        } catch (IOException ioe) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                           "UDPMessageProcessor, start(), can't" +
                           "create DatagramConnection: " + ioe.getMessage());
            }
        }

        /*
         * Start a thread to receive data on datagramconnection, dc
         */
        if (dc != null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stops the message processor.
     */
    synchronized public void stop() {
        try {
            if (dc != null) {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                                           "UDPMessageProcessor, stop(), " +
                                           "The stack is going to stop!");
                }
                // interrupt run() infinite loop 
                dc.close();
                if (thread != null && thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException exc) {
                        // intentionally ignored
                    }
                    thread = null;
                }
                // enable start of the processor 
                dc = null;
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "UDPMessageProcessor, stop(), The stack is stopped");
                }
            }
        } catch (IOException ex) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "UDPMessageProcessor, stop(), exception raised:");
            }
        }
    }


    /**
     * Thread main routine.
     */
    public void run() {
        try {
            // infinite loop will be broken by stop() function
            while (true) {
                Datagram datagram = dc.newDatagram(MAX_DATAGRAM_SIZE);

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "SYSTEM, UDPMessageProcessor, run(), listening!");
                }

                dc.receive(datagram);
                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_JSR180,
                                   "SYSTEM, UDPMessageProcessor, run(), " +
                                   "packet revceived!");
                }

                // Create synchronous message handler
                // for this message (UDPMessageChannel.run() will be used 
                // inside UDPMessageChannel constructor)
                // IMPL_NOTE: execute UDPMessageChannel.run directly
                //       or create a new thread
                UDPMessageChannel udpMessageChannel =
                    new UDPMessageChannel(datagram, sipStack, this);                               
            }
        } catch (IOException ex) {
            // intentionally ignored.
            // can't create new datagram or
            // recieve exception
        }
    }

    /**
     * Returns the transport string.
     * @return the transport string
     */
    public String getTransport() {
        return "udp";
    }
    
    /**
     * Gets the port from which the UDPMessageProcessor is reading messages
     * @return Port from which the udp message processor is
     * reading messages.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the stack.
     * @return my sip stack.
     */
    public SIPMessageStack getSipStack() {
        return sipStack;
    }

    /**
     * Create and return new TCPMessageChannel for the given host/port.
     * @param targetHostPort target host and port number
     * @return the new message channel
     */
    public MessageChannel createMessageChannel(HostPort targetHostPort) {
        return new UDPMessageChannel(targetHostPort.getHost().getHostname(),
                                 targetHostPort.getPort(), sipStack, this);
    }

    /**
     * Create and return new TCPMessageChannel for the given host/port.
     * @param host target host
     * @param port target port
     * @return the new message channel
     */
    public MessageChannel createMessageChannel(String host, int port) {
        return new UDPMessageChannel(host, port, sipStack, this);
    }

    /**
     * UDP is not a secure protocol.
     * @return always false
     */
    public boolean isSecure() {
        return false;
    }

    /**
     * Gets the SIP Stack.
     * @return the sip stack.
     */
    public SIPMessageStack getSIPStack() {
        return sipStack;
    }

}
