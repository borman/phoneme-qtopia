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
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/*
 * Provides the input stream implementation for the IrNativeConnection class.
 * It simply forwards read() requests to its holder instance.
 */
class IrNativeInputStream extends InputStream {

    /* IrNativeConnection instance which maintains this stream. */
    private IrNativeConnection conn;

    /*
     * Class constructor.
     *
     * @param conn holder of this instance.
     */
    public IrNativeInputStream(IrNativeConnection conn) {
	this.conn = conn;
    }

    /*
     * Defines an abstract method of the interface. This method is not
     * supposed to be used.
     *
     * @return this method does not return any value
     * @throws RuntimeException on invocation
     */
    public int read() throws IOException {
	throw new RuntimeException("read() method is not supported.");
    }

    /*
     * Reads a number of bytes to the specified byte array starting from the
     * given offset. This method simply forwards the request to the owner
     * IrNativeConnection instance.
     *
     * @param b destination byte array
     * @param off offset in the array
     * @param len number of bytes to read
     * @return number of bytes actually read
     * @throws IOException if an I/O error occurs
     */
    public int read(byte[] b, int off, int len) throws IOException {
	return conn.read(b, off, len);
    }

}

/*
 * Provides the output stream implementation for the IrNativeConnection class.
 * It simply forwards write() requests to its holder instance.
 */
class IrNativeOutputStream extends OutputStream {

    /* IrNativeConnection instance which maintains this stream. */
    private IrNativeConnection conn;

    /*
     * Class constructor.
     *
     * @param conn holder of this instance.
     */
    public IrNativeOutputStream(IrNativeConnection conn) {
	this.conn = conn;
    }

    /*
     * Defines an abstract method of the interface. This method is not
     * supposed to be used.
     *
     * @param b this parameter is not used
     * @throws RuntimeException on invocation
     */
    public void write(int b) throws IOException {
	throw new RuntimeException("write(int) method is not supported.");
    }

    /*
     * Writes a number of bytes from the specified byte array starting from
     * the given offset. This method simply forwards the request to the owner
     * IrNativeConnection instance.
     *
     * @param b source byte array
     * @param off offset in the array
     * @param len number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    public void write(byte[] b, int off, int len) throws IOException {
        conn.write(b, off, len);
    }

}

/*
 * Provides the implementation of a stream connection by the means of
 * native calls. This class implements both Connection and ConnectionNotifier
 * interfaces, and is used on client and server sides.
 */
public class IrNativeConnection implements StreamConnection {

    /*
     * Handle to the native connection. Corresponds to a network socket
     * used in this connection. Only accessed from native code.
     */
    private int connHandle = getInvalidConnHandle();

    /* InputStream interface implementation returned by this instance. */
    private IrNativeInputStream input = new IrNativeInputStream(this);

    /* OutputStream interface implementation returned by this instance. */
    private IrNativeOutputStream output = new IrNativeOutputStream(this);

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
     */
    public IrNativeConnection() {
    }

    /*
     * Native finalizer. Releases all native resources used by this connection.
     */
    protected native void finalize();

    /*
     * Discovers nearby devices with matching hint bits and IAS class name.
     *
     * @param hints hint bits to be used during discovery
     * @param ias IAS string to be used during discovery
     * @return integer array containing addresses of discovered devices
     * @throws IOException if an I/O error occurs
     */
    public int[] discover(int hints, String ias) throws IOException {
	int[] addr = new int[10];
	int count = discover(hints, ias, addr);
	int[] res = new int[count];
	System.arraycopy(addr, 0, res, 0, count);
	return res;
    }

    /*
     * Discovers devices in range with matching hint bits and IAS.
     *
     * @param hints hint bits to be used during discovery
     * @param ias IAS string to be used during discovery
     * @param addr output array to receive addresses of discovered devices
     * @return number of discovered devices
     * @throws IOException if an I/O error occurs
     */
    private native int discover(int hints, String ias, int[] addr)
	throws IOException;

    /*
     * Establishes a new connection to device with the specified address.
     *
     * @param addr address of the target device
     * @param ias IAS class name of the target service
     * @return true if the connection was established, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public native boolean connect(int addr, String ias) throws IOException;

    /*
     * Closes this connection.
     *
     * @throws IOException if an I/O error occurs
     */
    public native void close() throws IOException;

    /*
     * Opens and returns an input stream for this connection
     *
     * @return an input stream
     * @throws IOException if an I/O error occurs
     */
    public InputStream openInputStream() throws IOException {
	return input;
    }

    /*
     * Defines an abstract method of the interface. This method is not
     * supposed to be used.
     *
     * @return this method does not return any value
     * @throws RuntimeException on invocation
     */
    public DataInputStream openDataInputStream() throws IOException {
	throw new RuntimeException(
	    "Method is not supported.");
    }

    /*
     * Opens and returns an output stream for this connection
     *
     * @return an output stream
     * @throws IOException if an I/O error occurs
     */
    public OutputStream openOutputStream() throws IOException {
	return output;
    }

    /*
     * Defines an abstract method of the interface. This method is not
     * supposed to be used.
     *
     * @return this method does not return any value
     * @throws RuntimeException on invocation
     */
    public DataOutputStream openDataOutputStream() throws IOException {
	throw new RuntimeException(
	    "Method is not supported.");
    }

    /*
     * Reads a number of bytes to the specified byte array starting from the
     * given offset. This method uses its native counterpart for the actual
     * data delivery.
     *
     * @param b destination byte array
     * @param off offset in the array
     * @param len number of bytes to read
     * @return number of bytes actually read
     * @throws IOException if an I/O error occurs
     */
    public native int read(byte[] b, int off, int len) throws IOException;

    /*
     * Writes a number of bytes from the specified byte array starting from
     * the given offset. This method uses its native counterpart for the actual
     * data delivery.
     *
     * @param b source byte array
     * @param off offset in the array
     * @param len number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    public native void write(byte[] b, int off, int len) throws IOException;

    /*
     * Returns an invalid handle value to be used during initialization of
     * instances of this class.
     *
     * @return a native handle value considered to be invalid
     */
    private static native int getInvalidConnHandle();

}
