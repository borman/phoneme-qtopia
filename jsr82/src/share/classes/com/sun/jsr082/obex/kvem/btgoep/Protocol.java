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
import com.sun.jsr082.obex.btgoep.BTGOEPConnection;
import com.sun.jsr082.obex.btgoep.BTGOEPNotifier;
import com.sun.jsr082.obex.kvem.btgoep.BTGOEPNetmonConnection;
import com.sun.jsr082.obex.kvem.btgoep.BTGOEPNetmonNotifier;

/*
 * Network monitor version of BTGOEP protocol.
 *
 */
public final class Protocol extends com.sun.jsr082.obex.btgoep.Protocol {
    public Protocol() {}


    /*
     * Creates new btgoep connection.
     * @param sock stream
     * @param url URL of connection
     * @return netmon btgoep connection instance
     * @exception IOException if creating connection fails.
     */
    protected BTGOEPConnection newBTGOEPConnection(StreamConnection sock,
            String url) throws IOException {
        return new BTGOEPNetmonConnection(sock, url);
    }

    /*
     * Creates new btgoep server connection.
     * @param sock stream
     * @param url URL of connection
     * @return btgoep connection instance
     * @exception IOException if creating connection fails.
     */
    protected BTGOEPNotifier newBTGOEPNotifier(StreamConnectionNotifier sock,
            String url) throws IOException {
        return new BTGOEPNetmonNotifier(sock, url);
    }

}
