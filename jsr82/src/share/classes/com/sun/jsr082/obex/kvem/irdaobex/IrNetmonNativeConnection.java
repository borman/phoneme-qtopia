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
package com.sun.jsr082.obex.kvem.irdaobex;

import java.io.IOException;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonCommon;

/*
 * Provides the implementation of a stream connection by the means of
 * native calls. This class implements both Connection and ConnectionNotifier
 * interfaces, and is used on client and server sides.
 */
public class IrNetmonNativeConnection 
        extends com.sun.jsr082.obex.irdaobex.IrNativeConnection {

    /*
     * Obex Netmon connection id.
     */
    private int id;

    /*
     * Closed flag.
     */
    private boolean isNetmonClosed = false;
    
    /*
     * Class constructor.
     * 
     * @param url connection URL
     */
    public IrNetmonNativeConnection(String url) {
        long groupid = System.currentTimeMillis();
        id = NetmonCommon.connect0(url, groupid);
    }

    /*
     * Closes this connection.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        disconnect();
        super.close();
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
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int len1 = super.read(b, off, len);
            if (len1 != -1) {
                NetmonCommon.read0(id, b, off, len1);
            }
            return len1;
        } catch (IOException e) {
            disconnect();
            throw e;
        }        
    }

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
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            super.write(b, off, len);
            NetmonCommon.write0(id, b, off, len);
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
