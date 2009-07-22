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

package com.sun.jsr082.bluetooth.kvem.btl2cap;

//package com.sun.jsr082.bluetooth.btl2cap;
// was import com.sun.midp.io.j2me.btl2cap.L2CAPNotifierImpl;
import com.sun.jsr082.bluetooth.btl2cap.L2CAPNotifierImpl;
// was import com.sun.midp.io.BluetoothUrl;
import com.sun.jsr082.bluetooth.BluetoothUrl;
import javax.microedition.io.StreamConnection;
import java.io.IOException;


/*
 * Provides the implemetation of network monitoring BTSPP connection.
 */

// was public class L2CAPNetmonConnection extends com.sun.midp.io.j2me.btl2cap.L2CAPConnectionImpl
public class L2CAPNetmonConnection extends com.sun.jsr082.bluetooth.btl2cap.L2CAPConnectionImpl
{
    /*
     * Obex Netmon connection id.
     */
    private int id = -1;

    /*
     * Closed flag.
     */
    private boolean isNetmonClosed;

    L2CAPNetmonConnection(BluetoothUrl url, int mode) throws IOException {
        super(url, mode, null);

        //Network monitor section
        long groupid = System.currentTimeMillis();

        id = connect0(url.toString(), groupid);
    }

    protected L2CAPNetmonConnection(BluetoothUrl url,
            int mode, L2CAPNotifierImpl notif) throws IOException {
        super(url, mode,notif);

        //Network monitor section
        long groupid = System.currentTimeMillis();
        id = connect0(url.toString()    , groupid);

    }

    /* Overrided Function */
    public void close() throws IOException {
        disconnect();  // discon from NetMon
        super.close(); // close the connection
    }

    /* Overrided function.
      Sends the specified data via Bluetooth stack (native).
      this function is being executed from super.write in sychronized way*/

    protected int sendData(byte[] outData, int offset, int len)
                        throws IOException {
        int sentBytes = 0;
        try {
            sentBytes = super.send0(outData, offset, len);
            write0(id, outData, offset, len);  // to NetMon
        } catch (IOException e) {
            disconnect(); // if there is problem, discon from the NetMon
            throw e;
        }

        return sentBytes;
    }

    /* Overrided function
       Reads data from a packet received via Bluetooth stack. (native)
       This function is being executed is super.read in synchronized way  */

    protected int receiveData(byte[] inData, int offset, int length)
            throws IOException {
        try {
            int len = super.receive0(inData, offset, length);
            if (len != -1) {
                read0(id, inData, offset, len);  // to Netmon
            }
            return len;
        } catch (IOException e) {
            disconnect(); // if there is problem, discon from the NetMon
            throw e;
        }
    }


    /* Used only to disconnect from the network monitor */
    private void disconnect() {
        synchronized(this) {
            if (isNetmonClosed) {
                return;
            }
            isNetmonClosed = true;
        }

        disconnect0(id);

    }

    /* NATIVE SECTION */

    private  static native int  connect0(String url, long groupid);
    private  static native void disconnect0(int id);
    private  static native void read0(int id, byte[] b, int offset, int len);
    private  static native void write0(int id, byte[] b, int offset, int len);


}
