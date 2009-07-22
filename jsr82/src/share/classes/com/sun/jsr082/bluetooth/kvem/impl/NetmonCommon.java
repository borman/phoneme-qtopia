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

package com.sun.jsr082.bluetooth.kvem.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;

/*
 * Provides jsr082 common native netmon functions.
 * Currently the functions are used by btgoep, irdaobex, tcpobex netmon
 * connections.
 *
 * @version , 
 */
public final class NetmonCommon {
    /* Notify netmon about connection created event. */
    public static native int connect0(String url, long groupid);

    /* Notify netmon about connection closed event. */
    public static native void disconnect0(int id);

    /* Send bytes received from connection to netmon. */
    public static native void read0(int id, byte[] b, int offset, int len);

    /* Send bytes written into connection to netmon. */
    public static native void write0(int id, byte[] b, int offset, int len);


    /* Notifier started to listen. */
    public static native int notifierConnect0(String url, long groupid);

    /* Notifier closed. */
    public static native int notifierDisconnect0(int id);
}

