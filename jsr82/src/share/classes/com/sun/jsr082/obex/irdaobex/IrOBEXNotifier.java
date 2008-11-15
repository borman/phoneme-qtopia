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

import com.sun.jsr082.obex.ObexTransport;
import com.sun.jsr082.obex.ObexTransportNotifier;
import java.io.IOException;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnectionNotifier;

/*
 * Provides the implemetation of the OBEX over IrDA notifier.
 */
public class IrOBEXNotifier implements ObexTransportNotifier {

    /* Indicates whether this connection has been closed */
    boolean isClosed = false;

    /* Underlying connection notifier. */
    private StreamConnectionNotifier conn = null;

    /*
     * Creates the notifier object using the underlying connection notifier.
     *
     * @param conn underlying connection notifier
     */
    public IrOBEXNotifier(StreamConnectionNotifier conn) {
	this.conn = conn;
    }

    /*
     * Accepts IrDA OBEX transport connections.
     *
     * @return an IrOBEXConnection object
     * @exception IOException if an I/O error occurs
     */
    public ObexTransport acceptAndOpen() throws IOException {
	return new IrOBEXConnection(conn.acceptAndOpen());
    }

    /*
     * Closes this notifier and the underlying notifier.
     *
     * @exception IOException if an I/O error occurs
     */
    public void close() throws IOException {
        synchronized (this) {
            if (isClosed) {
                return;
            }
            isClosed = true;
        }
	conn.close();
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
