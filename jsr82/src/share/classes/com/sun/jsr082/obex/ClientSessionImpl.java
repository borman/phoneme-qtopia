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
package com.sun.jsr082.obex;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;

/*
 * The class implements client side of session.
 */
public class ClientSessionImpl extends ObexPacketStream
        implements ClientSession {

    private boolean busy;
    private Object lockObject = new Object();
    private int owner;

    /*
     * Current active operation. When not null, all method is ClientSession
     * is blocked.
     */
    Operation operation = null;

    private long connId = -1;

    public ClientSessionImpl(ObexTransport transport) throws IOException {
        super(transport);

        // owner field of all created HeaderSets
        owner = HeaderSetImpl.OWNER_CLIENT;
        isClient = true;
    }

    public HeaderSet createHeaderSet() {
        return new HeaderSetImpl(HeaderSetImpl.OWNER_CLIENT_USER);
    }

    private void lockCheckHeaders(HeaderSet headers) throws IOException {
        if (isClosed()) {
            throw new IOException("session closed");
        }

        if (operation != null) {
            throw new IOException("already in operation");
        }

        if (headers != null) {
            if (!(headers instanceof HeaderSetImpl)
                    || ((HeaderSetImpl)headers).owner == owner) {
                throw new IllegalArgumentException("wrong headerset class");
            }
        }
        synchronized (lockObject) {
            if (busy) {
                throw new IOException("already in operation");
            }
            busy = true;
        }
    }

    private void unlock() {
        synchronized (lockObject) {
            busy = false;
        }
    }

    void onAuthenticationFailure(byte[] username) throws IOException {
        if (operation != null) {
            operation.abort();
        }
        throw new IOException("server is not authenticated");
    }

    void onMissingAuthResponse() throws IOException {
        if (packetType != ResponseCodes.OBEX_HTTP_UNAUTHORIZED) {
            if (operation != null) {
                operation.abort();
            }
            throw new IOException("no auth response from server");
        }
    }

    public void setConnectionID(long id) {
        if (id < 0L || id > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("invalid id");
        }
        connId = id;
    }

    public long getConnectionID() {
        return connId;
    }

    void headerTooLarge() throws IOException {
        throw new IOException("header too large");
    }

    public HeaderSet connect(HeaderSet headers) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (isConnected) {
                throw new IOException("already connected");
            }
            byte[] head = {
                (byte)OPCODE_CONNECT,
                0, 0, // length will be here
                0x10, // obex protocol version 1.0
                0x00, // flags, all zero for this version of OBEX
                // maximum client supported packet length
                (byte) (OBEX_MAXIMUM_PACKET_LENGTH / 0x100),
                (byte) (OBEX_MAXIMUM_PACKET_LENGTH % 0x100),
            };

            sendPacket(head, -1, (HeaderSetImpl) headers, true);
            recvPacket();
            if (packetLength < 7 || buffer[3] != 0x10) {
		// IMPL_NOTE: It is not decided what the implementation should do
		// if the OBEX version does not match. For example, Windows
		// implementation uses version 1.2, Linux implementation
		// uses version 1.1. JSR-82 should probably work with both.
                // throw new IOException("unsupported server obex version");
            }
            HeaderSetImpl recvHeaders = new HeaderSetImpl(owner);
            parsePacketHeaders(recvHeaders, 7);

            if (shouldSendAuthResponse()) {
                // server ignores challenge before authenticating client
                authFailed = false;
                sendPacket(head, -1, (HeaderSetImpl) headers, true);
                recvPacket();
                if (packetLength < 7 || buffer[3] != 0x10) {
                    // IMPL_NOTE: See the comment above.
		    // throw new IOException("unsupported server obex version");
                }
                recvHeaders = new HeaderSetImpl(owner);
                parsePacketHeaders(recvHeaders, 7);
            }

            maxSendLength = decodeLength16(5);

            if (maxSendLength > OBEX_MAXIMUM_PACKET_LENGTH) {
                maxSendLength = OBEX_MAXIMUM_PACKET_LENGTH;
            }

            if (packetType == ResponseCodes.OBEX_HTTP_OK) {
                if (authFailed) {
                    throw new IOException("server is not authenticated");
                }
                isConnected = true;
            }
            return recvHeaders;
        } finally {
            unlock();
        }
    }

    public HeaderSet disconnect(HeaderSet headers) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (!isConnected) {
                throw new IOException("not connected");
            }
            sendPacket(PACKET_DISCONNECT, connId,
                    (HeaderSetImpl) headers, true);
            recvPacket();
            HeaderSetImpl recvHeaders = new HeaderSetImpl(owner);
            parsePacketHeaders(recvHeaders, 3);
            if (packetType == ResponseCodes.OBEX_HTTP_OK) {
                isConnected = false;
            }
            return recvHeaders;
        } finally {
            unlock();
        }
    }

    public Operation put(HeaderSet headers) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (!isConnected) {
                throw new IOException("not connected");
            }
            new ClientOperation(this,
                    (HeaderSetImpl) headers, false);
            return operation;
        } finally {
            unlock();
        }
    }

    public Operation get(HeaderSet headers) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (!isConnected) {
                throw new IOException("not connected");
            }
            new ClientOperation(this, (HeaderSetImpl)headers, true);
            return operation;
        } finally {
            unlock();
        }
    }

    public HeaderSet setPath(HeaderSet headers, boolean backup,
        boolean create) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (!isConnected) {
                throw new IOException("not connected");
            }
            byte[] head = {
                (byte)OPCODE_SETPATH,
                0, 0, // length will be here
                (byte)((backup ? 1 : 0) + (create ? 0 : 2)), // flags
                0x00, // constants
            };

            sendPacket(head, connId, (HeaderSetImpl) headers, true);
            recvPacket();
            HeaderSetImpl recvHeaders = new HeaderSetImpl(owner);
            parsePacketHeaders(recvHeaders, 3);
            return recvHeaders;
        } finally {
            unlock();
        }
    }

    public HeaderSet delete(HeaderSet headers) throws IOException {
        lockCheckHeaders(headers);
        try {
            if (!isConnected) {
                throw new IOException("not connected");
            }
            Operation op = new ClientOperation(this,
                    (HeaderSetImpl)headers, false);

            op.getResponseCode(); // indicates end of operation
            HeaderSet recvHeaders = op.getReceivedHeaders();
            op.close();
            return recvHeaders;
        } finally {
            unlock();
        }
    }
}
