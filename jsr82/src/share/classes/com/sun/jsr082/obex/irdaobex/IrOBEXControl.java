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

import com.sun.j2me.main.Configuration;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Vector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/*
 * Performs the IrOBEX initialization, handles the device hint bits state, and
 * responsible for device/service discovery process.
 */
final class IrOBEXControl {

    /* Default constructor. */
    public IrOBEXControl() {
    }

    /*
     * Creates the IrOBEX server notifier object by passing an underlying
     * connection notifier.
     *
     * @param hints hint bits
     * @param ias service class names separated by comma
     * @return IrOBEXNotifier object
     * @exception IOException if something goes wrong
     */
    public IrOBEXNotifier createServerConnection(int hints, String[] ias)
	throws IOException {
	return new IrOBEXNotifier(new IrNativeNotifier(hints, ias));
    }

    /*
     * Attempts to connect to the first device matches the criteria specified
     * (hint bits and IAS list). Cached devices are attempted first. If failed,
     * discovery process is initiated to update the cache, followed by another
     * connection attempt to a cached device. The procedure repeats until the
     * connection is established, or timout occurs.
     *
     * @param hints hint bits required to be set on the device
     * @param ias services required to be provided by the device
     * @return IrOBEXConnection object
     * @exception IOException if something goes wrong
     */
    public IrOBEXConnection createClientConnection(int hints, String[] ias)
	throws IOException {
	if (ias.length == 0) {
	    throw new IllegalArgumentException();
	}
	IrNativeConnection[] connArray = new IrNativeConnection[ias.length];
	for (int i = 0; i < ias.length; i++) {
	    connArray[i] = null;
	}
	int timeout = Integer.parseInt(Configuration.getProperty(
	    "com.sun.midp.io.j2me.irdaobex.DiscoveryTimeout"));
	int interval = Integer.parseInt(Configuration.getProperty(
	    "com.sun.midp.io.j2me.irdaobex.DiscoveryInterval"));
	long end = System.currentTimeMillis() + timeout;
	while (true) {
	    for (int i = 0; i < ias.length; i++) {
		if (System.currentTimeMillis() + interval > end) {
		    throw new IOException(
		        "Could not establish connection.");
		}
		IrNativeConnection conn = connArray[i];
		if (conn == null) {
		    conn = new IrNativeConnection();
		    connArray[i] = conn;
		}
		int[] addr;
		try {
		    addr = conn.discover(hints, ias[i]);
		} catch (IOException e) {
		    continue;
		}
		for (int j = 0; j < addr.length; j++) {
		    try {
			while (!conn.connect(addr[j], ias[i])) {
		    	    if (System.currentTimeMillis() + interval > end) {
				throw new IOException(
			    	    "Could not establish connection.");
			    }
			    try {
				Thread.sleep(10);
			    } catch (InterruptedException e) {
				throw new InterruptedIOException(
			    	    "Operation was interrupted.");
			    }
			}
			return new IrOBEXConnection(conn);
		    } catch (InterruptedIOException ie) {
			throw ie;
		    } catch (IOException e) {
		    }
		}
	    }
	}
    }

}
