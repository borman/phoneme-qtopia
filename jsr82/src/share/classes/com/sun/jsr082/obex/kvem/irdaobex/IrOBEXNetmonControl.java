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
import com.sun.jsr082.obex.irdaobex.IrOBEXNotifier;
import com.sun.jsr082.obex.irdaobex.IrOBEXConnection;
import com.sun.jsr082.obex.irdaobex.IrNativeConnection;
        
/*
 * Performs the IrOBEX initialization, handles the device hint bits state, and
 * responsible for device/service discovery process.
 */
final class IrOBEXNetmonControl extends com.sun.jsr082.obex.irdaobex.IrOBEXControl {

    /* Connection url. */
    private String url;

    /*
     * Creates the IrOBEX netmon server notifier object by passing an underlying
     * connection notifier.
     *
     * @param hints hint bits
     * @param ias service class names separated by comma
     * @param url URL of connection
     * @return IrOBEXNotifier object
     * @exception IOException if something goes wrong
     */
    public IrOBEXNotifier createNetmonServerConnection(int hints, String[] ias,
            String url)
	throws IOException {
	return new IrOBEXNotifier(new IrNetmonNativeNotifier(hints, ias, url));
    }

    /*
     * Creates the IrOBEX netmon client connection object by passing an underlying
     * connection notifier.
     *
     * @param hints hint bits
     * @param ias service class names separated by comma
     * @param url URL of connection
     * @return IrOBEXConnection object
     * @exception IOException if something goes wrong
     */
    public IrOBEXConnection createNetmonClientConnection(int hints, String[] ias,
            String url)
	throws IOException {
        this.url = url;
	return createClientConnection(hints, ias);
    }

    /*
     * Creates the new IrNativeConnection instance.
     * @return new IrNativeConnection instance
     * @exception IOException if something goes wrong
     */
    protected IrNativeConnection newIrNativeConnection() throws IOException {
        return new IrNetmonNativeConnection(url);
    }

}
