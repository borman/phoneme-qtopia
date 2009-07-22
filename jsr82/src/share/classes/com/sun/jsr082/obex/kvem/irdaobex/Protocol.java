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
import com.sun.jsr082.obex.irdaobex.IrOBEXControl;
import com.sun.jsr082.obex.irdaobex.IrOBEXNotifier;
import com.sun.jsr082.obex.irdaobex.IrOBEXConnection;

/*
 * Network monitor version of IRDA OBEX protocol.
 *
 */
public final class Protocol extends com.sun.jsr082.obex.irdaobex.Protocol {

    /*
     * Creates new irdaobex control.
     */
    protected IrOBEXControl newIrOBEXControl() {
        return new IrOBEXNetmonControl();
    }

    /*
     * Creates new irdaobex notifier.
     * @param hints hint bits required to be set on the device
     * @param ias services required to be provided by the device
     * @param url URL of connection
     * @return the instance of server connection
     * @exception IOException if creating connection fails.
     */
    protected IrOBEXNotifier newIrOBEXNotifier(int hints,
            String[] iasArray, String url) throws IOException {
        return ((IrOBEXNetmonControl)control).createNetmonServerConnection(
                hints, iasArray, url);
    }

    /*
     * Creates new irdaobex connection.
     * @param hints hint bits required to be set on the device
     * @param ias services required to be provided by the device
     * @param url URL of connection
     * @return the instance of client connection
     * @exception IOException if creating connection fails.
     */
    protected IrOBEXConnection newIrOBEXConnection(int hints,
            String[] iasArray, String url) throws IOException {
        return ((IrOBEXNetmonControl)control).createNetmonClientConnection(
                hints, iasArray, url);
    }
    
}
