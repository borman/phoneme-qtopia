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
#include "btL2CAPCommon.h"
#include <javacall_bt.h>
#include <jsrop_exceptions.h>
#include <jsrop_logging.h>
#include <jsrop_libc_ext.h>
#include <sni.h>

static jfieldID connHandleID = NULL;
static jfieldID remoteAddrID = NULL;

/*
 * Retrieves native connection handle from temporary storage
 * inside <code>L2CAPNotifierImpl</code> instance
 * and sets it to this <code>L2CAPConnectionImpl</code> instance.
 *
 * Note: the method sets native connection handle directly to
 * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
 *
 * @param notif reference to corresponding <code>L2CAPNotifierImpl</code>
 *              instance storing native peer handle
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_setThisConnHandle0) {

    REPORT_INFO(LC_PROTOCOL, "btl2cap::setThisConnHandle");

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(notifHandle);
    KNI_GetThisPointer(thisHandle);
    KNI_GetParameterAsObject(1, notifHandle);

    if (KNI_IsNullHandle(notifHandle)) {
        REPORT_ERROR(LC_PROTOCOL,
            "Notifier handle is null in btl2cap::setThisConnHandle");
    } else {
        jfieldID peerHandleID = GetL2CAPPeerHandleID();

        if (peerHandleID == NULL) {
            REPORT_ERROR(LC_PROTOCOL,
                "Peer handle ID is not initialized"
                "in btl2cap::setThisConnHandle");
        } else {
            javacall_handle handle =
                (javacall_handle)KNI_GetIntField(notifHandle, peerHandleID);

            if (handle != JAVACALL_BT_INVALID_HANDLE) {
                /* store native connection handle to Java */
                KNI_SetIntField(thisHandle, connHandleID, (jint)handle);

                /* reset temporary storing field */
                KNI_SetIntField(notifHandle,
                    peerHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);

                REPORT_INFO(LC_PROTOCOL, "btl2cap::setThisConnHandle done!");
            } else {
                REPORT_ERROR(LC_PROTOCOL,
                    "Peer handle is invalid in btl2cap::setThisConnHandle");
            }
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Native static class initializer.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_initialize) {

    REPORT_INFO(LC_PROTOCOL, "btl2cap::initialize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", connHandleID)
    GET_FIELDID(classHandle, "remoteDeviceAddress", "[B", remoteAddrID)

    REPORT_INFO(LC_PROTOCOL, "btl2cap::initialize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Native finalizer.
 * Releases all native resources used by this connection.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_finalize) {
    javacall_handle handle;
    int status = JAVACALL_FAIL;
    jfieldID connHandleID = NULL;

    REPORT_INFO(LC_PROTOCOL, "btl2cap::finalize");

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(classHandle);

    KNI_GetThisPointer(thisHandle);
    KNI_GetClassPointer(classHandle);
    GET_FIELDID(classHandle, "handle", "I", connHandleID)

    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        status = javacall_bt_l2cap_close(handle);

        KNI_SetIntField(thisHandle, connHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
        // Need revisit: add resource counting
/*
        if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
            REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
        }
*/
        if (status == JAVACALL_FAIL) {
            char* pError;
            javacall_bt_l2cap_get_error(handle, &pError);
            JAVAME_SNPRINTF(gBtBuffer, BT_BUFFER_SIZE,
                "IO error in btl2cap::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gBtBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_ERROR1(LC_PROTOCOL,
                "btl2cap::finalize blocked, handle= %d\n", handle);
        }
    }
    // Need revisit: add bluetooth activity indicator
/*    FINISH_BT_INDICATOR; */


    REPORT_INFO(LC_PROTOCOL, "btl2cap::finalize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Creates a client connection object.
 *
 * Note: the method sets native connection handle directly to
 * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
 *
 * @param imtu receive MTU or <code>-1</code> if not specified
 * @param omtu transmit MTU or <code>-1</code> if not specified
 * @param auth   <code>true</code> if authication is required
 * @param enc    <code>true</code> indicates
 *                what connection must be encrypted
 * @param master <code>true</code> if client requires to be
 *               a connection's master
 *
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_create0) {
    int imtu = (int)KNI_GetParameterAsInt(1);
    int omtu = (int)KNI_GetParameterAsInt(2);
    javacall_bool auth  = (KNI_GetParameterAsBoolean(3) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool enc  = (KNI_GetParameterAsBoolean(4) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool master  = (KNI_GetParameterAsBoolean(5) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;

    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;

    REPORT_INFO(LC_PROTOCOL, "btl2cap::create");

    /* create L2CAP server connection */
    if (javacall_bt_l2cap_create_client(imtu, omtu, auth, enc, master, &handle)
            == JAVACALL_FAIL) {
        REPORT_ERROR(LC_PROTOCOL,
            "Connection creation failed during btl2cap::create");
        KNI_ThrowNew(jsropIOException,
            EXCEPTION_MSG("Can not create L2CAP connection"));
        KNI_ReturnVoid();
    }

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

    /* store native connection handle to Java object */
    KNI_SetIntField(thisHandle, connHandleID, (jint)handle);

    REPORT_INFO1(LC_PROTOCOL,
        "btl2cap::create0 handle=%d connection created", handle);

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * Closes client connection.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
 *
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_close0) {
    javacall_handle handle;

    REPORT_INFO(LC_PROTOCOL, "btl2cap::close");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        if (javacall_bt_l2cap_close(handle) == JAVACALL_FAIL) {
            REPORT_ERROR(LC_PROTOCOL,
                "L2CAP connection closing failed in btl2cap::close");
            KNI_ThrowNew(jsropIOException,
                EXCEPTION_MSG("L2CAP connection closing failed"));
        } else {
            // Need revisit: add resource counting
/*
            if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
            }
*/
        }
        /* IMPL_NOTE: revisit */
        /* javacall_bt_l2cap_destroy_handle(handle); */
        KNI_SetIntField(thisHandle, connHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
    }

    REPORT_INFO(LC_PROTOCOL, "btl2cap::close done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * Determines if there is a packet that can be read.
 *
 * If <code>true</code>, a call to <code>receive()</code>
 * will not block the application.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>L2CAPConnectionImpl</code> object.
 *
 * @return <code>true</code> if a packet is present,
 *         <code>false</code> otherwise
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_btl2cap_L2CAPConnectionImpl_ready0) {
    int res;
    javacall_handle handle;
    javacall_bool ready;

    REPORT_INFO(LC_PROTOCOL, "btl2cap::ready");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    switch (javacall_bt_l2cap_get_ready(handle, &ready)) {
    case JAVACALL_OK:
        res = (ready == JAVACALL_TRUE) ? KNI_TRUE : KNI_FALSE;
        REPORT_INFO(LC_PROTOCOL, "btl2cap::ready done!");
        break;

    case JAVACALL_FAIL:
        res = KNI_FALSE;
        REPORT_ERROR(LC_PROTOCOL,
            "L2CAP connection state check failed in btl2cap::ready");
        KNI_ThrowNew(jsropIOException,
            EXCEPTION_MSG("L2CAP connection ready check failure"));
        break;

    default: /* illegal argument */
        REPORT_ERROR(LC_PROTOCOL, "Internal error in btl2cap::ready");
        res = KNI_FALSE;
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}
