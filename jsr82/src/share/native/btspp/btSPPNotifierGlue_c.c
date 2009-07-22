/*
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

#include "btMacros.h"
#include "btUtils.h"
#include "btRFCOMMCommon.h"
#include "btPush.h" 
#include <javacall_bt.h>

#include <jsrop_exceptions.h>
#include <jsrop_logging.h>
#include <jsrop_libc_ext.h>

#include <sni.h>

static jfieldID notifHandleID = NULL;
static jfieldID peerHandleID  = NULL;
static jfieldID peerAddrID    = NULL;
static jfieldID pushHandleID  = NULL;

/*
 * Retrieves the field ID to access the field temporary storing
 * native client peer handle.
 *
 * @return native peer handle
 */
jfieldID GetRFCOMMPeerHandleID() {
    return peerHandleID;
}

/*
 * Native static class initializer.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_initialize) {

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::initialize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", notifHandleID)
    GET_FIELDID(classHandle, "peerHandle", "I", peerHandleID)
    GET_FIELDID(classHandle, "peerAddress", "[B", peerAddrID)
    GET_FIELDID(classHandle, "pushHandle", "I", pushHandleID)

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::initialize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Native finalizer.
 * Releases all native resources used by this connection.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_finalize) {
    javacall_handle handle, peer;
    int status = JAVACALL_FAIL;

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::finalize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

    handle = (javacall_handle)KNI_GetIntField(thisHandle, notifHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        status = javacall_bt_rfcomm_close(handle);

        KNI_SetIntField(thisHandle, notifHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);

        // Need revisit: add resource counting
/*
        if (midpDecResourceCount(RSC_TYPE_BT_SER, 1) == 0) {
            REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
        }
*/

        if (status == JAVACALL_FAIL) {
            char* pError;
            javacall_bt_rfcomm_get_error(handle, &pError);
            JAVAME_SNPRINTF(gBtBuffer, BT_BUFFER_SIZE,
                    "IO error in bt_rfcomm_notif::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gBtBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_ERROR1(LC_PROTOCOL,
                "btspp_notif::finalize notifier blocked, handle = %d\n",
                handle);
        }
    }

    peer = (javacall_handle)KNI_GetIntField(thisHandle, peerHandleID);

    if (peer != JAVACALL_BT_INVALID_HANDLE) {
        status = javacall_bt_rfcomm_close(peer);

        KNI_SetIntField(thisHandle, peerHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);

        // Need revisit: add resource counting
/*
        if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
            REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
        }
*/

        if (status == JAVACALL_FAIL) {
            char* pError;
            javacall_bt_rfcomm_get_error(peer, &pError);
            JAVAME_SNPRINTF(gBtBuffer, BT_BUFFER_SIZE,
                    "IO error in bt_rfcomm_notif::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gBtBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_ERROR1(LC_PROTOCOL,
                "btspp_notif::finalize blocked, handle = %d\n", peer);
        }
    }

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::finalize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Creates a server connection object.
 *
 * Note: the method sets native connection handle directly to
 * <code>handle<code> field of <code>BTSPPNotifierImpl</code> object.
 *
 * @param auth   <code>true</code> if authication is required
 * @param authz  <code>true</code> if authorization is required
 * @param enc    <code>true</code> indicates
 *                what connection must be encrypted
 * @param master <code>true</code> if client requires to be
 *               a connection's master
 * @return selected channel number to listen for incoming connections on
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_create0) {
    javacall_bool auth  = (KNI_GetParameterAsBoolean(1) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool authz  = (KNI_GetParameterAsBoolean(2) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool enc  = (KNI_GetParameterAsBoolean(3) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool master  = (KNI_GetParameterAsBoolean(4) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;

    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;
    int cn = BT_RFCOMM_INVALID_CN;

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::create0");

    // Need revisit: add resource counting
/*
    if (midpCheckResourceLimit(RSC_TYPE_BT_SER, 1) == 0) {
        const char* pMsg = "Resource limit exceeded for BT server sockets";
        REPORT_INFO(LC_PROTOCOL, pMsg);
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
    } else {
*/

    /* create RFCOMM server connection */
    if (javacall_bt_rfcomm_create_server(auth, authz, enc, master, &handle, &cn)
            == JAVACALL_FAIL) {
        REPORT_ERROR(LC_PROTOCOL,
            "RFCOMM notifier creation failed in btspp_notif::create");
        KNI_ThrowNew(jsropIOException,
            EXCEPTION_MSG("Can not create RFCOMM notifier"));
        KNI_ReturnInt(BT_RFCOMM_INVALID_CN);
    }

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

    /* store native connection handle to Java object */
    KNI_SetIntField(thisHandle, notifHandleID, (jint)handle);

    // Need revisit: add resource counting
/*
    if (midpIncResourceCount(RSC_TYPE_BT_SER, 1) == 0) {
        REPORT_INFO(LC_PROTOCOL, "BT Server: Resource limit update error");
    }
*/

//    }

    REPORT_INFO2(LC_PROTOCOL, "btspp_notif::create notifier created"
        ", port = %d, handle = %d\n", cn, handle);

    KNI_EndHandles();
    KNI_ReturnInt(cn);
}

/*
 * Force Bluetooth stack to listen for incoming client connections.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>BTSPPNotifierImpl</code> object.
 *
 * @throws IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_listen0) {

    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::listen");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    if (KNI_GetIntField(thisHandle, pushHandleID) == BT_INVALID_PUSH_HANDLE) {
        handle = (javacall_handle)KNI_GetIntField(thisHandle, notifHandleID);

        /* force listening */
        if (javacall_bt_rfcomm_listen(handle) == JAVACALL_FAIL) {
            javacall_bt_rfcomm_close(handle);
            REPORT_ERROR(LC_PROTOCOL,
                "RFCOMM notifier listen failed in btspp_notif::listen");
            KNI_ThrowNew(jsropIOException,
                EXCEPTION_MSG("RFCOMM notifier listen failed"));
        } else {
            REPORT_INFO(LC_PROTOCOL, "btspp_notif::listen done!");
        }
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * Closes this server connection.
 * Releases all native resources (such as sockets) owned by this notifier.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>BTSPPNotifierImpl</code> object.
 *
 * @throws IOException IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_close0) {
    javacall_handle handle, peer;

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::close0");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

    handle = (javacall_handle)KNI_GetIntField(thisHandle, notifHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        if (javacall_bt_rfcomm_close(handle) == JAVACALL_FAIL) {
            REPORT_ERROR(LC_PROTOCOL,
                "Notifier handle closing failed in btspp_notif::close");
            KNI_ThrowNew(jsropIOException,
                EXCEPTION_MSG("RFCOMM notifier closing failed"));
        } else {
            // Need revisit: add resource counting
/*
            if (midpDecResourceCount(RSC_TYPE_BT_SER, 1) == 0) {
                REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
            }
*/
        }
        KNI_SetIntField(thisHandle, notifHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
    }

    peer = (javacall_handle)KNI_GetIntField(thisHandle, peerHandleID);

    if (peer != JAVACALL_BT_INVALID_HANDLE) {
        if (javacall_bt_rfcomm_close(peer) == JAVACALL_FAIL) {
            REPORT_ERROR(LC_PROTOCOL,
                "Peer handle closing failed in btspp_notif::close");
            KNI_ThrowNew(jsropIOException,
                EXCEPTION_MSG("RFCOMM notifier closing failed"));
        } else {
            // Need revisit: add resource counting
/*
            if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
            }
*/
        }
        KNI_SetIntField(thisHandle, peerHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
    }

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::close0 done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}
