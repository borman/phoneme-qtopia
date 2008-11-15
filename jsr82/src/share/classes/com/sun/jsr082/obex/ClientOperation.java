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
package com.sun.jsr082.obex;

import javax.obex.Operation;
import javax.obex.HeaderSet;
import javax.obex.ResponseCodes;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/*
 * The class implements client side of put/get operation.
 */
final class ClientOperation implements Operation {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;

    private ClientSessionImpl stream;
    private HeaderSetImpl recvHeaders;
    private HeaderSetImpl sentHeaders;
    private byte[] head;
    private Object lock = new Object();

    private boolean inputStreamOpened;
    private boolean inputStreamClosed;
    private boolean outputStreamOpened;
    private boolean outputStreamClosed;

    private boolean inputStreamEof;

    private boolean firstDataBlock = true;
    private int openObjects = 1;

    private OperationInputStream is;
    private OperationOutputStream os;

    /*
     * True if this operation is get operation.
     * Otherwise it is put operation.
     */
    private boolean isGet;

    /*
     * Output stream finished, receiving data from input stream.
     */
    private boolean requestEnd;
    private boolean operationEnd;
    private boolean operationClosed;

    private boolean restartable = true;
    private boolean restarting;

    /*
     * Determines whether aborting of operation is in progress.
     * If <code>true</code> any read/write calls are denied.
     */
    private boolean abortingOperation;

    ClientOperation(ClientSessionImpl stream, HeaderSetImpl sendHeaders,
            boolean isGet) throws IOException {
        if (DEBUG) {
            System.out.println("clientOperation.constructor(): isGet = "
                    + isGet);
        }
        this.isGet = isGet;
        head = new byte[] {
            isGet ? (byte) ObexPacketStream.OPCODE_GET
                  : (byte) ObexPacketStream.OPCODE_PUT,
            0, 0};

        stream.operation = this;
        stream.isEof = false;
        this.stream = stream;
        this.sentHeaders = sendHeaders == null ?
            new HeaderSetImpl(HeaderSetImpl.OWNER_CLIENT) : sendHeaders;
        recvHeaders = new HeaderSetImpl(HeaderSetImpl.OWNER_CLIENT);
        is = new OperationInputStream();
        os = new OperationOutputStream();

        // starting send process, fill in send buffer
        stream.packetBegin(head);
        if (sendHeaders == null
                || sendHeaders.getHeader(HeaderSet.TARGET) == null) {

            // early TARGET vs CONNECTION ID conflict check
            stream.packetAddConnectionID(
                    stream.getConnectionID(), sendHeaders);
        }
        stream.packetAddAuthResponses();
        stream.packetAddHeaders(sendHeaders);

        // if buffer is overflowed - begining to send packets
        while (stream.challengesToSend || !stream.queuedHeaders.isEmpty()) {
            if (!packetExchange()) {
                // Some headers may be lost if server early finish the
                // operation.
                return;
            }
        }
    }

    /*
     * Finish and send packet, received response, start new packet.
     * @return packetType == OPCODE_CONTINUE.
     */
    private boolean packetExchange() throws IOException {
        if (DEBUG) {
            System.out.println("client: packetExchange()");
        }
        if (operationEnd) {
            if (requestEnd && stream.shouldSendAuthResponse()
                    && restartOperation()) {
                return true;
            }
            return false;
        }
        if (!requestEnd) {
            // finish packet end send it
            stream.packetEndStripConnID();

            // receive packet
            stream.recvPacket();
            operationEnd =
                stream.packetType != ObexPacketStream.OPCODE_CONTINUE;
            synchronized (recvHeaders) {
                stream.parsePacketHeaders(recvHeaders, 3);
            }

            // check code
            if (operationEnd) {
                if (stream.shouldSendAuthResponse() && restartOperation()) {
                    return true;
                }
                operationEnd = requestEnd = true;
                return false;
            }

            // begin new packet
            stream.packetBegin(head);
            stream.packetAddAuthResponses();
            stream.packetAddHeaders(null);
            return true;
        }

        // requestEnd = true

        stream.parseEnd();
        stream.sendPacket(head, -1, null, false);
        stream.recvPacket();
        operationEnd = stream.packetType != ObexPacketStream.OPCODE_CONTINUE;

        // check of errorcode should be done before after data parsing
        stream.parsePacketDataBegin(recvHeaders, 3);
        return true;
    }

    private void requestEnd() throws IOException {
        if (DEBUG) {
            System.out.println("client: requestEnd()");
        }
        synchronized (lock) {
            if (requestEnd) {
                return;
            }
            requestEnd = true;
        }
        head[0] |= ObexPacketStream.OPCODE_FINAL;

        if (operationEnd) {
            return;
        }

        if (outputStreamOpened) {
            boolean res = stream.packetEOFBody();
            if (!res) { // error adding EOFB previous packet too long
                if (!packetExchange()) {
                    return;
                }
                stream.packetEOFBody();
            }
        }

        stream.packetMarkFinal();
        stream.packetEndStripConnID();
        stream.recvPacket();
        operationEnd = stream.packetType != ObexPacketStream.OPCODE_CONTINUE;

        if (!isGet) {
            stream.parsePacketHeaders(recvHeaders, 3);
            return;
        }

        stream.parsePacketDataBegin(recvHeaders, 3);

        while (true) {
            // special request to check data availability
            int hasData = stream.parsePacketData(recvHeaders, null, 0, 0);
            if (hasData == 1 || stream.isEof) break;

            if (stream.shouldSendAuthResponse() && restartOperation()) {
                return;
            }
            if (!packetExchange()) {
                return;
            }
        }
    }

    private void notRestartable() {
        restartable = false;
        sentHeaders = null;
    }

    private boolean restartOperation() throws IOException {
        if (DEBUG) {
            System.out.println("client: restartOperation()");
        }
        if (!restartable) {
            return false;
        }
        HeaderSetImpl headers = sentHeaders;
        notRestartable();
        operationEnd = false;
        boolean prevRequestEnd = requestEnd;
        requestEnd = false;
        head[0] = isGet ? (byte) ObexPacketStream.OPCODE_GET
                        : (byte) ObexPacketStream.OPCODE_PUT;

        recvHeaders = new HeaderSetImpl(HeaderSetImpl.OWNER_CLIENT);
        stream.queuedHeaders.removeAllElements();
        stream.isEof = false;

        // starting send process, fill in send buffer
        stream.packetBegin(head);
        stream.packetAddConnectionID(stream.getConnectionID(), headers);
        stream.packetAddAuthResponses();
        stream.packetAddHeaders(headers);

        // if buffer is overflowed - begining to send packets
        while (!stream.queuedHeaders.isEmpty()) {
            if (!packetExchange()) {
                return true;
            }
        }
        if (prevRequestEnd) {
            requestEnd();
        }
        restarting = true;
        return true;
    }

    private void sendAbortPacket() throws IOException {
        if (operationEnd) {
            return;
        }

        inputStreamClosed = true;
        outputStreamClosed = true;
        operationEnd = true;
        requestEnd = true;
        stream.queuedHeaders.removeAllElements();
        stream.sendPacket(ObexPacketStream.PACKET_ABORT, -1, null, true);
        stream.recvPacket();
        stream.parsePacketHeaders(recvHeaders, 3);

        if (stream.packetType != ResponseCodes.OBEX_HTTP_OK) {
            stream.brokenLink();
        }
    }

    public void abort() throws IOException {
        abortingOperation = true;
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: abort()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }
            try {
                if (operationEnd) {
                    throw new IOException(
                            "operation already finished");
                }
                sendAbortPacket();
            } finally {
                operationClosed = true;
                openObjects = 0;
                stream.operation = null;
            }
        }
    }

    public HeaderSet getReceivedHeaders() throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: getReceivedHeaders()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }

            HeaderSetImpl res = new HeaderSetImpl(recvHeaders);
            res.packetType = ObexPacketStream.validateStatus(res.packetType);
            return res;
        }
    }

    public int getResponseCode() throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: getResponseCodes()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }

            requestEnd();

	    inputStreamOpened  = false;
            outputStreamOpened = false;


	    inputStreamClosed = true;
            outputStreamClosed = true;

	    openObjects = 1;

            return ObexPacketStream.validateStatus(recvHeaders.packetType);
        }
    }

    public void sendHeaders(HeaderSet headers) throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: sendHeaders()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }
            if (headers == null) {
                throw new NullPointerException("null headerset");
            }
            if (!(headers instanceof HeaderSetImpl)) {
                throw new IllegalArgumentException("wrong headerset class");
            }
            HeaderSetImpl headersImpl = (HeaderSetImpl) headers;
            if (!headersImpl.isSendable()) {
                throw new IllegalArgumentException(
                        "not created with createHeaderSet");
            }
            if (operationEnd) {
                throw new IOException("operation finished");
            }

            if (restartable) {
                // store the headers to accumulated headers
                sentHeaders.merge(headersImpl);
            }

            stream.packetAddHeaders(headersImpl);

            if (requestEnd) {
                return;
            }

            if (!stream.queuedHeaders.isEmpty()) {
                if (!packetExchange()) {
                    throw new IOException(
                            "server finished operation, not all headers sent");
                }
            }
        }
    }

    public String getEncoding() {
        return null; // acording to docs
    }

    public long getLength() {
        Long res = (Long)recvHeaders.getHeader(HeaderSetImpl.LENGTH);
        if (res == null) {
            return -1;
        }
        return res.longValue();
    }

    public String getType() {
        return (String)recvHeaders.getHeader(HeaderSetImpl.TYPE);
    }


    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: openOutputStream()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }
            if (outputStreamOpened) {
                throw new IOException("no more output streams available");
            }
            if (requestEnd) {
                throw new IOException("too late to open output stream");
            }
            outputStreamOpened = true;
            openObjects++;
            return os;
        }
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public InputStream openInputStream() throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: openInputStream()");
            }
            if (operationClosed) {
                throw new IOException("operation closed");
            }
            if (inputStreamOpened) {
                throw new IOException("no more input streams available");
            }
            inputStreamOpened = true;
            openObjects++;
            if (!isGet) {
                return new FakeInputStream();
            }

            // flush rest of headers and data
            requestEnd();

            return is;
        }
    }

    private void terminate() throws IOException {
        if (DEBUG) {
            System.out.println("client: terminate() = "
                    + (openObjects - 1));
        }
        openObjects--;
        if (openObjects != 0) {
            return;
        }

        // all closed what was opened.
        sendAbortPacket();
        stream.operation = null;
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (DEBUG) {
                System.out.println("client: op.close()");
            }
            if (!operationClosed) {
                operationClosed = true;
                terminate();
            }
        }
    }

    private class OperationOutputStream extends OutputStream {
        OperationOutputStream() {}

        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }

        public void write(byte[] b, int offset, int len) throws IOException {
            int initialOffset = offset;
            int initialLen = len;
            boolean firstDataPacket = true;
            synchronized (lock) {
                if (DEBUG) {
                    // System.out.println("client: write()");
                }
                if (outputStreamClosed || requestEnd) {
                    throw new IOException("stream closed");
                }
                if (len < 0 || offset < 0 || offset + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                while (len > 0) {
                    if (abortingOperation) {
                        throw new IOException("operation aborted");
                    }
                    int wr = stream.packetAddData(b, offset, len);
                    if (wr != len || firstDataBlock && firstDataPacket) {
                        firstDataPacket = false;
                        restarting = false;
                        if (!packetExchange()) {
                            // fix CR: when sending and closin the socket
                            if (!stream.shouldSendAuthResponse()) {
                                if (wr == 0) {
                                    break;
				}
                            } else {
                                throw new IOException(
				    "server rejected the data");
                            }
                        }
                        if (restarting) {
                            len = initialLen;
                            offset = initialOffset;
                        }
                    }
                    len -= wr;
                    offset += wr;
                }
            }
            firstDataBlock = false;
            notRestartable();
        }

        public void flush() throws IOException {
            synchronized (lock) {
                if (DEBUG) {
                    System.out.println("client: flush()");
                }

                if (outputStreamClosed || requestEnd) {
                    throw new IOException("stream closed");
                }
                if (stream.packetLength != 3) {
                    packetExchange();
                }
            }
        }

        public void close() throws IOException {
            synchronized (lock) {
                if (DEBUG) {
                    System.out.println("client: os.close()");
                }
                if (!outputStreamClosed) {
                    outputStreamClosed = true;
                    requestEnd();
                    terminate();
                }
            }
        }
    }

    private class OperationInputStream extends InputStream {
        OperationInputStream() {}

        public int read() throws IOException {
            byte[] b = new byte[1];
            int len = read(b, 0, 1);
            if (len == -1) {
                return -1;
            }
            return b[0] & 0xFF;
        }

        public int read(byte[] b, int offset, int len) throws IOException {
            synchronized (lock) {
                if (DEBUG) {
                    // System.out.println("client: read()");
                }
                if (inputStreamClosed) {
                    throw new IOException("stream closed");
                }
                // Nullpointer exception thrown here
                if (len < 0 || offset < 0 || offset + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                if (len == 0) {
                    return 0;
                }

                if (inputStreamEof) {
                    notRestartable();
                    return -1;
                }
                int result = 0;
                while (true) {
                    if (abortingOperation) {
                        throw new IOException("operation aborted");
                    }
                    int rd = stream.parsePacketData(recvHeaders, b,
						    offset, len);
                    if (rd != 0) {
                        offset += rd;
                        len -= rd;
                        result += rd;
                        if (len == 0) {
                            notRestartable();
                            return result;
                        }
                    }

                    // need more data, packet is finished

                    // check if stream is finished
                    if (stream.isEof) {
                        // received END_OF_BODY
                        while (!operationEnd) {
                            // strange, no response code - waiting
                            stream.parseEnd();
                            stream.sendPacket(head, -1, null, false);
                            stream.recvPacket();
                            operationEnd = stream.packetType
                                != ObexPacketStream.OPCODE_CONTINUE;
                            stream.parsePacketHeaders(recvHeaders, 3);
                        }

                        inputStreamEof = true;
                        notRestartable();
                        return (result == 0) ? -1 : result;
                    }

                    if (stream.packetType
                            != ObexPacketStream.OPCODE_CONTINUE) {
                        throw new IOException("server errorcode received");
                    }
                    packetExchange();
                }
            }
        }

        public void close() throws IOException {
            synchronized (lock) {
                if (DEBUG) {
                    System.out.println("client: is.close()");
                }
                if (inputStreamClosed) {
                    return;
                }
                inputStreamEof = false;

                // sending abort packet if operation not ended
                try {
                    sendAbortPacket();
                } catch (IOException e) {
                    // nothing, link should be marked already as broken
                }

                inputStreamClosed = true;
                terminate();
            }
        }
    }

    private class FakeInputStream extends InputStream {
        FakeInputStream() {}

        public int read() throws IOException {
            throw new IOException("not supported");
        }

        public void close() throws IOException {
        }
    }
}
