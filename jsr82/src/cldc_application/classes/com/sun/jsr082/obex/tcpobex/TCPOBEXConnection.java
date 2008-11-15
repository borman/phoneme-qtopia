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
package com.sun.jsr082.obex.tcpobex;

// jsr082 (impl) classes
import com.sun.jsr082.obex.ObexTransport;

// cldc classes
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

// midp classes
import com.sun.midp.security.SecurityToken;
import com.sun.midp.main.Configuration;
import com.sun.midp.io.j2me.socket.Protocol;
import javax.microedition.io.SocketConnection;

/*
 * Provides TCP OBEX underlying stream connection as a transport to
 * shared OBEX implementation.
 *
 */
public class TCPOBEXConnection implements ObexTransport {
    /* Indicates if this notifier has been closed. */
    private boolean isClosed = false;

    /* Keeps the correspondent input stream. */
    private InputStream in;

    /* Keeps the correspondent output stream. */
    private OutputStream out;

    /*
     * Opens the underlaying tcp/socket connection and
     * initiates the streams.
     *
     * @param host Target host name (ip address).
     * @param port Target's port to connect to.
     * @throws IOException if any error occurs.
     */
    protected TCPOBEXConnection(String host, int port) throws IOException {
        
        Protocol sp = new Protocol();
        StreamConnection conn = (StreamConnection) sp.openPrim(
                "//" + host + ":" + port, Connector.READ_WRITE, false);

        // do not delay request since this delays the response.
        sp.setSocketOption(SocketConnection.DELAY, 0);
        
        in = conn.openInputStream();;
        out = conn.openOutputStream();
        conn.close();
    }

    /*
     * Used by notifier that opened tcp/socket connection -
     * just copy the streams here.
     *
     * @param streams An array of the streams.
     *                The first item is input stream.
     *                The second, output stream.
     */
    protected TCPOBEXConnection(Object[] streams) {
        in = (InputStream) streams[0];
        out = (OutputStream) streams[1];
    }

    /*
     * Gets underlying connection.
     *
     * @return Always returns <code>null</code>.
     */
    public Connection getUnderlyingConnection() {
        return null;
    }

    /*
     * Closes this connection.
     *
     * @throws IOException if I/O error.
     */
    public void close() throws IOException {
        synchronized (TCPOBEXConnection.class) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }
        boolean hasException = false;

        try {
            in.close();
        } catch (IOException e) {
            hasException = true;
        }

        try {
            out.close();
        } catch (IOException e) {
            hasException = true;
        }

        if (hasException) {
            throw new IOException("Can't close connection");
        }
    }

    /* 
     * Reading obex packet.
     *
     * @param inData the buffer to read packet from
     * @return the packet size
     */
    public int read(byte[] inData) throws IOException {
        readFully(inData, 0, 3); // read header
        int packetLength = decodeLength16(inData, 1);

        if (packetLength < 3 || packetLength > inData.length) {
            throw new IOException("protocol error");
        }
        readFully(inData, 3, packetLength - 3);
        return packetLength;
    }

    /* 
     * Writing obex packet.
     *
     * @param outData the buffer to write packet to
     * @param len the packet size
     */
    public void write(byte[] outData, int len) throws IOException {
        out.write(outData, 0, len);
        out.flush();
    }

    /*
     * Gets maximum packet size.
     *
     * @return The maximum packet size to be used by obex implementation.
     */
    public int getMaximumPacketSize() {
        return Configuration.getIntProperty(
            "obex.packetLength.max", 4096);
    }

    /*
     * Reads <code>size</code> bytes of data from the connection
     * into an array of bytes.
     *
     * @param array the buffer into which the data is read.
     * @param offset the start offset in array <code>array</code> 
     *               at which the data is written.
     * @param size the number of bytes to read.
     */
    private final void readFully(byte[] array, int offset, int size)
            throws IOException {
        while (size != 0) {
            int count = in.read(array, offset, size);
            if (count == -1) {
                throw new IOException("read error");
            }
            offset += count;
            size -= count;
        }
    }

    /*
     * Interprets two bytes located in <code>buffer</code> with <code>off</code>
     * offset as packet length.
     *
     * @param buffer to read length from
     * @param off the offset in <code>buffer</code>
     * @return the packet length
     */
    private final int decodeLength16(byte[] buffer, int off) {
        return ((((int)buffer[off]) & 0xFF) << 8)
            + (((int)buffer[off + 1]) & 0xFF);
    }
}
