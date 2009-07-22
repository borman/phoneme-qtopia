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

package com.sun.jsr082.obex.kvem.btgoep;

import java.io.IOException;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.bluetooth.ServiceRecord;
import com.sun.jsr082.obex.btgoep.BTGOEPNotifier;
import com.sun.jsr082.obex.btgoep.BTGOEPConnection;
import com.sun.jsr082.bluetooth.kvem.impl.BTNetmonNotifier;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonCommon;
import com.sun.jsr082.bluetooth.kvem.impl.NetmonBluetooth;

/*
 * Network monitoring implementation of BTGOEP notifier.
 * Redefined to provide the URL for connection.
 *
 */
final class BTGOEPNetmonNotifier extends BTGOEPNotifier
        implements BTNetmonNotifier {
    private String url;

    /* Netmon connection id. */
    private int id = -1;

    BTGOEPNetmonNotifier(StreamConnectionNotifier sock, String url)
        throws IOException {
        super(sock);
        this.url = url;
    }

    /*
     * Create btgoep connection with monitoring code.
     */
    protected BTGOEPConnection createTransportConnection(
            StreamConnection sock) throws IOException {
        return new BTGOEPNetmonConnection(sock, url);
    }

    public void netmonUpdate(ServiceRecord rec) {
        if (id == -1) {
            id = NetmonCommon.notifierConnect0(url, 0);
        }
        NetmonBluetooth.notifierUpdateServiceRecord(id, rec);
    }

    public void close() throws IOException {
        NetmonCommon.notifierDisconnect0(id);
        super.close();
    }
}
