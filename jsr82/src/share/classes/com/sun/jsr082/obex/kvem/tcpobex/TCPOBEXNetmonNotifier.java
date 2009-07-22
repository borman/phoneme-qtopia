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
import com.sun.jsr082.obex.tcpobex.TCPOBEXConnection;

/*
 * Network monitoring implementation of TCP OBEX notifier.
 * Redefined to provide the URL for connection.
 *
 */
final public class TCPOBEXNetmonNotifier 
        extends com.sun.jsr082.obex.tcpobex.TCPOBEXNotifier {

    private String url;

    public TCPOBEXNetmonNotifier(SecurityToken token, 
			  int port, String url) throws IOException {
        super(token, port);
        this.url = url;
    }

    /*
     * Create tcp obex connection with monitoring code.
     */
    protected TCPOBEXConnection createTransportConnection(Object[] sockData)
            throws IOException {
        return new TCPOBEXNetmonConnection(sockData, url);
    }
}
