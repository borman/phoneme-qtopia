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
 * SDPOutputStream.java
 *
 * Created on Feb 20, 2004
 *
 */
package gov.nist.microedition.sip;

import gov.nist.siplite.message.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.sip.SipConnection;

/**
 * SDP output stream.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public class SDPOutputStream extends OutputStream {

    /**
     * Constructs a stream for the requested connection.
     * @param connection the associated connection
     */
    public SDPOutputStream(SipConnection connection) {
        this.connection = connection;
        setOpen(true);
    }

    /** The current connection. */
    private SipConnection connection = null;

    /**
     * the ByteArrayOutputStream object wrapped by this class.
     * We cannot just inherit from the ByteArrayOutputStream class because
     * ByteArrayOutputStream.write() does not throw an IOException.
     * (Is it important? Yes. When the parent class does not throw an
     * exception, the child class must not throw the exception, because
     * the child class may be used everywhere in place of the parent class.
     * If the child classes could throw exceptions not declared for parents,
     * we would have undeclared exceptions thrown. And Java does not permit
     * this. So we have to use a wrapper class.)
     */
    private final ByteArrayOutputStream outputStream =
            new ByteArrayOutputStream();

    /** is the stream open? */
    private boolean isOpen;

    /**
     * Convert to a string containing debugging information.
     * @return string containing: class name + hash value + isOpen state
     */
    public String toString() {
        return super.toString()+" isOpen="+isOpen;
    }

    /**
     * Return the ByteArrayOutputStream object wrapped by this class.
     * The problem is that ByteArrayOutputStream methods do not throw
     * IOException, while SDPOutputStream methods should.
     * @return the ByteArrayOutputStream object
     */
    protected ByteArrayOutputStream getByteArrayOutputStream() {
        return outputStream;
    }

    /**
     * check if the stream is open
     * @throws IOException if the stream is closed
     */
    private void checkOpen() throws IOException {
        if (!isOpen) {
            throw new IOException("the output stream has been closed");
        }
    }

    /**
     * Writes the specified byte to the wrapped byte array output stream.
     *
     * @param   b   the byte to be written.
     * @exception  IOException  if the stream is closed.
     */
    public void write(int b) throws IOException {
        checkOpen();
        outputStream.write(b);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out.
     *
     * @exception  IOException  if the stream is closed.
     */
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to the
     * wrapped output stream. The general contract for <code>write(b)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(b, 0, b.length)</code>.
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.OutputStream#write(byte[], int, int)
     * @exception  IOException  if the stream is closed.
     */
    public void write(byte[] b) throws IOException {
        checkOpen();
        outputStream.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the wrapped byte
     * array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     * @exception  IOException  if the stream is closed.
     */
    public void write(byte[] b, int off, int len) throws IOException {
        checkOpen();
        outputStream.write(b, off, len);
    }

    /**
     * Return the status of the output stream (open or closed).
     * When the stream is closed, write() throws an IOException.
     * The open or closed status does not affect the internal
     * ByteArrayOutputStream object.
     * @return true if the stream is open
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * The send() functions use this function to toggle the stream state.
     * (send() cannot call close() because it's done vice versa:
     * close() calls send())
     * @param newOpenState the new state
     */
    protected void setOpen(boolean newOpenState) {
        isOpen = newOpenState;
    }

    /**
     * Close the SDPOutputStream and send the message held by the
     * sip connection
     */
    public void close() throws IOException {
        if (!isOpen) {
            throw new IOException("stream already closed");
        }
        setOpen(false);
        outputStream.close();

        if (connection instanceof SipClientConnectionImpl) {
            SipClientConnectionImpl sipClientConnection =
                    (SipClientConnectionImpl)connection;
            // If the client connection is in a STREAM_OPEN state and
            // the request is an ACK
            // The connection goes into the COMPLETED state
            if (sipClientConnection.state ==
                    SipClientConnectionImpl.STREAM_OPEN) {
                if (sipClientConnection.getMethod().equals(Request.ACK)) {
                    sipClientConnection.state =
                            SipClientConnectionImpl.COMPLETED;
                    try {
                        super.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return;
                }
            }
        }
        try {
            connection.send();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            super.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

