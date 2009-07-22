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
#include <javacall_bt.h>
/* IMPL_NOTE: revisit */
/* #include <btCommon.h> */
#include <jsrop_exceptions.h>
#include <jsrop_logging.h>
#include <sni.h>
#include <jsrop_libc_ext.h>

static jfieldID connHandleID = NULL;
static jfieldID remoteAddrID = NULL;

/*
 * Retrieves native connection handle from temporary storage
 * inside <code>RFCOMMNotifierImpl</code> instance
 * and sets it to this <code>RFCOMMConnectionImpl</code> instance.
 *
 * Note: the method sets native connection handle directly to
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
 * @param notif reference to corresponding <code>RFCOMMNotifierImpl</code>
 *              instance storing native peer handle
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_setThisConnHandle0) {

    REPORT_INFO(LC_PROTOCOL, "btspp::setThisConnHandle");

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(notifHandle);
    KNI_GetThisPointer(thisHandle);
    KNI_GetParameterAsObject(1, notifHandle);

    if (KNI_IsNullHandle(notifHandle)) {
        REPORT_ERROR(LC_PROTOCOL,
            "Notifier handle is null in btspp::setThisConnHandle");
    } else {
        jfieldID peerHandleID = GetRFCOMMPeerHandleID();

        if (peerHandleID == NULL) {
            REPORT_ERROR(LC_PROTOCOL,
                "Peer handle ID is not initialized"
                "in btspp::setThisConnHandle");
        } else {
            javacall_handle handle =
                (javacall_handle)KNI_GetIntField(notifHandle, peerHandleID);

            if (handle != JAVACALL_BT_INVALID_HANDLE) {
                /* store native connection handle to Java */
                KNI_SetIntField(thisHandle, connHandleID, (jint)handle);

                /* reset temporary storing field */
                KNI_SetIntField(notifHandle,
                    peerHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);

                REPORT_INFO(LC_PROTOCOL, "btspp::setThisConnHandle done!");
            } else {
                REPORT_ERROR(LC_PROTOCOL,
                    "Peer handle is invalid in btspp::setThisConnHandle");
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
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_initialize) {

    REPORT_INFO(LC_PROTOCOL, "btspp::initialize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", connHandleID)
    GET_FIELDID(classHandle, "remoteDeviceAddress", "[B", remoteAddrID)

    REPORT_INFO(LC_PROTOCOL, "btspp::initialize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Native finalizer.
 * Releases all native resources used by this connection.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_finalize) {
    javacall_handle handle;
    int status = JAVACALL_FAIL;

    REPORT_INFO(LC_PROTOCOL, "btspp::finalize");

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(classHandle);
    KNI_GetThisPointer(thisHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", connHandleID)

    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        status = javacall_bt_rfcomm_close(handle);

        KNI_SetIntField(thisHandle, connHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
        // Need revisit: add resource counting
/*
        if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
            REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
        }
*/
        if (status == JAVACALL_FAIL) {
            char* pError;
            javacall_bt_rfcomm_get_error(handle, &pError);
            JAVAME_SNPRINTF(gBtBuffer, BT_BUFFER_SIZE,
                "IO error in btspp::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gBtBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_ERROR1(LC_PROTOCOL,
                "btspp::finalize blocked, handle= %d\n", handle);
        }
    }
    // Need revisit: add bluetooth activity indicator
/*    FINISH_BT_INDICATOR; */


    REPORT_INFO(LC_PROTOCOL, "btspp::finalize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Creates a client connection object.
 *
 * Note: the method sets native connection handle directly to
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
  * @param auth   <code>true</code> if authication is required
 * @param enc    <code>true</code> indicates
 *                what connection must be encrypted
 * @param master <code>true</code> if client requires to be
 *               a connection's master
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_create0) {
    javacall_bool auth  = (KNI_GetParameterAsBoolean(1) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool enc  = (KNI_GetParameterAsBoolean(2) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;
    javacall_bool master  = (KNI_GetParameterAsBoolean(3) == KNI_TRUE)
        ? JAVACALL_TRUE : JAVACALL_FALSE;

    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;

    REPORT_INFO(LC_PROTOCOL, "btspp::create");

    /* create RFCOMMC server connection */
    if (javacall_bt_rfcomm_create_client(auth, enc, master, &handle)
            == JAVACALL_FAIL) {
        REPORT_ERROR(LC_PROTOCOL,
            "Connection creation failed during btspp::create");
        KNI_ThrowNew(jsropIOException,
            EXCEPTION_MSG("Can not create RFCOMM connection"));
        KNI_ReturnVoid();
    }

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

    /* store native connection handle to Java object */
    KNI_SetIntField(thisHandle, connHandleID, (jint)handle);

    REPORT_INFO1(LC_PROTOCOL,
        "btspp::create0 handle=%d connection created", handle);

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * Closes client connection.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_close0) {
    javacall_handle handle;

    REPORT_INFO(LC_PROTOCOL, "btspp::close");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    if (handle != JAVACALL_BT_INVALID_HANDLE) {
        if (javacall_bt_rfcomm_close(handle) == JAVACALL_FAIL) {
            REPORT_ERROR(LC_PROTOCOL,
                "RFCOMM connection closing failed in btspp::close");
            KNI_ThrowNew(jsropIOException,
                EXCEPTION_MSG("RFCOMM connection closing failed"));
        } else {
            // Need revisit: add resource counting
/*
            if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
            }
*/
        }
        KNI_SetIntField(thisHandle, connHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);
    }

    REPORT_INFO(LC_PROTOCOL, "btspp::close done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * Returns the number of bytes available to be read from the connection
 * without blocking.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>BTSPPConnectionImpl</code> object.
 *
 * @return the number of available bytes
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_available0) {
    javacall_handle handle;
    int count = -1;
    char* pError;

    REPORT_INFO(LC_PROTOCOL, "btspp::available");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    switch (javacall_bt_rfcomm_get_available(handle, &count)) {
    case JAVACALL_OK:
        REPORT_INFO(LC_PROTOCOL, "btspp::available done!");
        break;

    case JAVACALL_FAIL:
        javacall_bt_rfcomm_get_error(handle, &pError);
        JAVAME_SNPRINTF(gBtBuffer, BT_BUFFER_SIZE,
            "IO error during btspp::::available (%s)", pError);
        REPORT_ERROR(LC_PROTOCOL, gBtBuffer);
        KNI_ThrowNew(jsropIOException, EXCEPTION_MSG(gBtBuffer));
        break;
    default: /* illegal argument */
        REPORT_ERROR(LC_PROTOCOL, "Internal error in btspp::available");
    }

    KNI_EndHandles();
    KNI_ReturnInt(count);
}

