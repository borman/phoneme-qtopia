/*
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

import com.sun.jsr082.obex.ObexTransport;
import com.sun.j2me.main.Configuration;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/*
 * Provides the implemetation of the OBEX over IrDA transport.
 */
public class IrOBEXConnection implements ObexTransport {

    /* Indicates whether this connection has been closed */
    boolean isClosed = false;

    /* Underlying connection. */
    private StreamConnection conn = null;

    /* Input stream provided by the underlying connection. */
    private InputStream input = null;

    /* Output stream provided by the underlying connection. */
    private OutputStream output = null;

    /*
     * Creates this connection using the underlying stream connection.
     *
     * @param conn undelying connection object
     * @throws IOException if the connection can not be established
     */
    public IrOBEXConnection(StreamConnection conn) throws IOException {
	this.conn = conn;
	input = conn.openInputStream();
	output = conn.openOutputStream();
    }

    /*
     * Closes this connection and the underlying connection.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        synchronized (this) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }
	input.close();
	output.close();
	conn.close();
    }

    /*
     * Reads the packet data into the specified buffer.
     *
     * @param inData destination buffer
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if the specified buffer is null
     * @return number of bytes read
     */
    public int read(byte[] inData) throws IOException {
	if (isClosed) {
	    throw new IOException("The connection is closed.");
	}
	if (inData == null) {
	    throw new NullPointerException("Input buffer is null.");
	}
	int size = getMaximumPacketSize();
	int len = inData.length;
	if (size == 0 || len <= size) {
	    return input.read(inData, 0, len);
	}
	int off = 0;
	while (off < len) {
	    int count =	input.read(inData, off, Math.min(size, len - off));
	    if (count == 0) {
		break;
	    }
	    off += count;
	}
	return off;
    }

    /*
     * Transfer the len bytes from specified packet over the irda connection.
     *
     * @param outData source buffer
     * @param len data length
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if the specified buffer is null
     */
    public void write(byte[] outData, int len) throws IOException {
	if (isClosed) {
	    throw new IOException("The connection is closed.");
	}
	if (outData == null) {
	    throw new NullPointerException("Output buffer is null.");
	}
	int size = getMaximumPacketSize();
	if (size == 0 || len <= size) {
	    output.write(outData, 0, len);
	    return;
	}
	int off = 0;
	while (off < len) {
	    int count = Math.min(size, len - off);
	    output.write(outData, off, count);
	    off += count;
	}
    }

    /*
     * Returns the amount of data that can be successfully sent or received
     * in a single read/write operation.
     *
     * @return the maximum packet size, or zero if any size may be used
     */
    public final int getMaximumPacketSize() {
        return Configuration.getIntProperty("obex.packetLength.max", 0);
    }

    /*
     * Returns the underlying connection, or null if there is none.
     *
     * @return underlying connection object
     */
    public Connection getUnderlyingConnection() {
        return conn;
    }

}
