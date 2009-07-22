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

#include <stdlib.h>
#include <kni.h>
#include <commonKNIMacros.h>
#include <midpError.h>
#include <midpMalloc.h>
#include <midpUtilKni.h>
#include <midp_thread.h>
#include <sni.h>
#include <midpUtilKni.h>
#include <midpError.h>

#include <javacall_bt.h>
#include <btCommon.h>
#include <btUtils.h>




/*
 * Retrieves PIN code to use for pairing with a remote device. If the
 * PIN code is not known, PIN entry dialog is displayed.
 *
 * @param address the Bluetooth address of the remote device
 * @return string containing the PIN code
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_jsr082_bluetooth_NativeBCC_getPasskey(void)
{
    char passkey[16];
    javacall_bt_address addr;
    KNI_StartHandles(2);
    KNI_DeclareHandle(addressHandle);
    KNI_DeclareHandle(passkeyHandle);
    MidpReentryData *reentry = (MidpReentryData *)SNI_GetReentryData(NULL);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    if (reentry == NULL) {
        switch (javacall_bt_bcc_put_passkey(addr, passkey, JAVACALL_TRUE)) {
            case JAVACALL_OK:
                KNI_NewStringUTF(passkey, passkeyHandle);
                break;
            case JAVACALL_FAIL:
                KNI_ReleaseHandle(passkeyHandle);
                break;
            case JAVACALL_WOULD_BLOCK:
                /* Need revisit: add BT_SIGNAL to the midp workspace or
                   use existing signal instead
                   midp_thread_wait(BT_SIGNAL, 1, NULL); */
                break;
            default:
                break;
        }
    } else {
        if (reentry->status) {
            switch (javacall_bt_bcc_put_passkey(addr, passkey, JAVACALL_FALSE)) {
                case JAVACALL_OK:
                    KNI_NewStringUTF(passkey, passkeyHandle);
                    break;
                case JAVACALL_FAIL:
                    KNI_ReleaseHandle(passkeyHandle);
                    break;
                default:
                    break;
            }
        } else {
            KNI_ReleaseHandle(passkeyHandle);
        }
    }
    KNI_EndHandlesAndReturnObject(passkeyHandle);
}


