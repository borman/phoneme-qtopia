/*
 *
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.jsr082.obex.kvem.tcpobex;

import java.io.IOException;
import com.sun.midp.security.SecurityToken;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonCommon;

/*
 * Provides the implemetation of the TCP OBEX
 * transport for WTK with monitoring.
 *
 */
final class TCPOBEXNetmonConnection 
        extends com.sun.jsr082.obex.tcpobex.TCPOBEXConnection {

    /* Obex Netmon connection id. */
    private int id;

    /* closed flag. */
    private boolean isNetmonClosed;

    TCPOBEXNetmonConnection(SecurityToken token,
			    String host, int port, String url)
            throws IOException {
        super(token, host, port);
        long groupid = System.currentTimeMillis();
        id = NetmonCommon.connect0(url, groupid);
    }

    TCPOBEXNetmonConnection(Object[] sockData, String url) throws IOException {
        super(sockData);
        long groupid = System.currentTimeMillis();
        id = NetmonCommon.connect0(url, groupid);
    }

    public void close() throws IOException {
        disconnect();
        super.close();
    }

    public void write(byte[] outData, int len) throws IOException {
        try {
            super.write(outData, len);
            NetmonCommon.write0(id, outData, 0, len);
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }

    public int read(byte[] inData) throws IOException {
        try {
            int len = super.read(inData);
            if (len != -1) {
                NetmonCommon.read0(id, inData, 0, len);
            }
            return len;
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }

    private void disconnect() {
        synchronized(this) {
            if (isNetmonClosed) {
                return;
            }
            isNetmonClosed = true;
        }
        NetmonCommon.disconnect0(id);
    }
}
