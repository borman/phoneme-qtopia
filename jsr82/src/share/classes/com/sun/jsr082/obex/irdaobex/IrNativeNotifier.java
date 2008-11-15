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
package com.sun.jsr082.obex.irdaobex;

import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InterruptedIOException;

/*
 * Provides the implementation of a stream connection notifier for the IrDA
 * protocol.
 */
public class IrNativeNotifier implements StreamConnectionNotifier {

    /* Hint bits to be set by the server. */
    private int hints;

    /* IAS services to be advertised by the server. */
    private String[] ias;

    /*
     * Handle to peer connection. This handle is mainained until it is passed
     * to IrNativeConnection to prevent resource leaks in case of VM
     * termination. Only accessed from native code.
     */
    private int peerHandle = getInvalidPeerHandle();

    /*
     * Handle to native array containing connection handles,
     * except for <code>peer</code>. Only accessed from native code.
     */
    private int dataHandle = getInvalidDataHandle();

    /* Flag forcing acceptAndOpen() method to exit. */
    private boolean closeRq = false;

    /* Period in milliseconds between connection attempts. */
    private final int ACCEPT_RETRY_PERIOD = 50;

    /* Static initializer. */
    static {
	initialize();
    }

    /*
     * Native class initializer.
     */
    private static native void initialize();

    /*
     * Class constructor.
     *
     * @param hints hint bits
     * @param ias IAS string
     */
    public IrNativeNotifier(int hints, String[] ias) {
	this.hints = hints;
	this.ias = ias;
    }

    /*
     * Native finalizer. Releases all native resources used by this notifier.
     */
    protected native void finalize();

    /*
     * Returns a StreamConnection object that represents a server side
     * connection.
     *
     * @return a StreamConnection to communicate with a client
     * @throws IOException if an I/O error occurs
     */
    public StreamConnection acceptAndOpen() throws IOException {
	try {
	    closeRq = false;
	    for (int i = 0; i < ias.length; i++) {
		listen(i, hints, ias[i]);
	    }
	    int i = 0;
	    while (!accept(i) && !closeRq) {
	        i = (i + 1) % ias.length;
	        try {
		    Thread.sleep(ACCEPT_RETRY_PERIOD);
		} catch (InterruptedException e) {
		    throw new InterruptedIOException(
		        "Operation was interrupted.");
		}
	    }
	    if (closeRq) {
		throw new IOException("Connection was closed.");
	    }
	    IrNativeConnection conn = new IrNativeConnection();
	    passPeer(conn);
	    return conn;
	} finally {
	    release();
	}
    }

    /*
     * Interrupts acceptAndOpen() operation.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
	closeRq = true;
    }

    /*
     * Allocates native connection resource for listening with specified
     * hint bits and IAS set.
     *
     * @param index connection number starting from 0
     * @param hints hint bits to be set
     * @param ias IAS to be advertised
     * @throws IOException if an I/O error occurs
     */
    private native void listen(int index, int hints, String ias)
	throws IOException;

    /*
     * Accepts a pending client connection.
     *
     * @param index connection number starting from 0
     * @return true if the connection was established, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private native boolean accept(int index) throws IOException;

    /*
     * Releases native resources.
     */
    private native void release();

    /*
     * Passes ownership of the native peer handle to the IrNativeConnection
     * object. <code>peer</code> is set to <code>-1</code> after calling this
     * method.
     *
     * @param conn IrNativeConnection object to pass native peer handle to.
     */
    private native void passPeer(IrNativeConnection conn);

    /*
     * Returns an invalid connection handle value to be used during
     * initialization of instances of this class.
     *
     * @return a native connection handle value considered to be invalid
     */
    private static native int getInvalidPeerHandle();

    /*
     * Returns an invalid memory handle value to be used during
     * initialization of instances of this class.
     *
     * @return a native memory handle value considered to be invalid
     */
    private static native int getInvalidDataHandle();

}
