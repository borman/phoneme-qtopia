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

#include <btMacros.h>
#include <javacall_bt.h>
#include <btL2CAPCommon.h>

#include <midp_thread.h>
#include <sni.h>
#include <midp_libc_ext.h>
#include <kni_globals.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>
#include <midpString.h>
#include <suitestore_common.h>

#include <push_server_resource_mgmt.h>

#include "btPush.h"


/*
 * Checks out (takes ownership of) an active server connection maintained
 * by push subsystem.
 *
 * @param url URL used during registration of the push entry
 * @param suiteId suite id
 * @return true if the operation succeeds, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_btl2cap_L2CAPNotifierImpl_pushCheckout(void)
{
    jboolean retval = KNI_FALSE;
    SuiteIdType suiteId;
    MidpString wsUrl;
    char *szUrl;
    bt_port_t port;
    jfieldID notifHandleID = NULL;
    jfieldID pushHandleID  = NULL;

    KNI_StartHandles(3);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(urlHandle);
    KNI_DeclareHandle(classHandle);

    KNI_GetThisPointer(thisHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", notifHandleID)
    GET_FIELDID(classHandle, "pushHandle", "I", pushHandleID)

    KNI_GetParameterAsObject(1, urlHandle);
    suiteId = KNI_GetParameterAsInt(2);

    wsUrl = midpNewString(urlHandle);
    szUrl = midpJcharsToChars(wsUrl);
    if (bt_push_parse_url(szUrl, &port, NULL) == JAVACALL_OK) {
        if (pushcheckout(szUrl, 0, (char*)midp_suiteid2chars(suiteId)) == -2) {
            KNI_ThrowNew(midpIOException, "Port already in use.");
        } else {
            javacall_handle handle;
            bt_pushid_t pushid = bt_push_checkout_server(&port, &handle, NULL);
            if (pushid != BT_INVALID_PUSH_HANDLE) {
                KNI_SetIntField(thisHandle, pushHandleID, (jint)pushid);
                KNI_SetIntField(thisHandle, notifHandleID, (jint)handle);
                retval = KNI_TRUE;
            }
        }
    }
    midpFree(szUrl);
    MIDP_FREE_STRING(wsUrl);
    KNI_EndHandles();
    KNI_ReturnBoolean(retval);
}

/*
 * Accepts incoming client connection request.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>L2CAPNotifierImpl</code> object.
 *
 * Note: new native connection handle to work with accepted incoming
 * client connection is setted directly to <code>handle</code> field of
 * appropriate <code>L2CAPConnectionImpl</code> object.
 *
 * @return Negotiated ReceiveMTU and TransmitMTU.
 *               16 high bits is ReceiveMTU, 16 low bits is TransmitMTU.
 * @throws IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_btl2cap_L2CAPNotifierImpl_accept0(void) {
    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;
    javacall_handle peer_handle = JAVACALL_BT_INVALID_HANDLE;
    MidpReentryData* info;
    int status = JAVACALL_FAIL;
    int processStatus = KNI_FALSE;
    int imtu, omtu, mtus;
    void *context = NULL;
    javacall_bt_address peer_addr;
    jfieldID notifHandleID = NULL;
    jfieldID peerHandleID  = NULL;
    jfieldID peerAddrID    = NULL;
    jfieldID pushHandleID  = NULL;

    KNI_StartHandles(3);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(arrayHandle);
    KNI_DeclareHandle(classHandle);
    KNI_GetThisPointer(thisHandle);

    KNI_GetClassPointer(classHandle);

    GET_FIELDID(classHandle, "handle", "I", notifHandleID)
    GET_FIELDID(classHandle, "peerHandle", "I", peerHandleID)
    GET_FIELDID(classHandle, "peerAddress", "[B", peerAddrID)
    GET_FIELDID(classHandle, "pushHandle", "I", pushHandleID)

    handle = (javacall_handle)KNI_GetIntField(thisHandle, notifHandleID);
    KNI_GetObjectField(thisHandle, peerAddrID, arrayHandle);

    if (handle == JAVACALL_BT_INVALID_HANDLE) {
        REPORT_ERROR(LC_PROTOCOL,
            "L2CAP server socket was closed before btl2cap_notif::accept");
        KNI_ThrowNew(midpInterruptedIOException, EXCEPTION_MSG(
            "L2CAP notifier was closed"));
    } else {
#ifndef NO_PUSH
        bt_pushid_t pushid = KNI_GetIntField(thisHandle, pushHandleID);
        if (pushid != BT_INVALID_PUSH_HANDLE) {
            if (bt_push_checkout_client(pushid, &peer_handle, peer_addr,
                    &imtu, &omtu) == JAVACALL_OK) {
                pushcheckoutaccept((int)handle);
                processStatus = KNI_TRUE;
                status = JAVACALL_OK;
            }
        }
#endif
        if (peer_handle == JAVACALL_BT_INVALID_HANDLE) {

        info = (MidpReentryData*)SNI_GetReentryData(NULL);
        if (info == NULL) {   /* First invocation */
            REPORT_INFO1(LC_PROTOCOL,
                "btl2cap_notif::accept handle=%d\n", handle);

            // Need revisit: add resource counting
            /*
             * An incoming socket connection counts against the client socket
             * resource limit.
             */
/*
            if (midpCheckResourceLimit(RSC_TYPE_BT_CLI, 1) == 0) {
                const char* pMsg =
                    "Resource limit exceeded for BT client sockets";
                REPORT_INFO(LC_PROTOCOL, pMsg);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
*/
//            } else {
                status = javacall_bt_l2cap_accept(
                    handle, &peer_handle, &peer_addr, &imtu, &omtu);
                processStatus = KNI_TRUE;
//            }
        } else {  /* Reinvocation after unblocking the thread */
            if ((javacall_handle)info->descriptor != handle) {
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "btl2cap_notif::accept handles mismatched %d != %d\n",
                    handle, info->descriptor);
                REPORT_CRIT(LC_PROTOCOL, gKNIBuffer);
                KNI_ThrowNew(midpIllegalStateException, EXCEPTION_MSG(
                    "Internal error in btl2cap_notif::accept"));
            } else {
                // Need revisit: add resource counting
/*
                if (midpCheckResourceLimit(RSC_TYPE_BT_CLI, 1) == 0) {
                    const char* pMsg =
                        "Resource limit exceeded for BT client sockets"
                    REPORT_INFO(LC_PROTOCOL, pMsg);
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
                } else {
*/
                    status = javacall_bt_l2cap_accept(
                        handle, &peer_handle, &peer_addr, &imtu, &omtu);
                    processStatus = KNI_TRUE;
//                }
            }
        }

        }

        if (processStatus) {
            REPORT_INFO1(LC_PROTOCOL,
                "btl2cap_notif::accept server handle=%d\n", handle);
            if (status == JAVACALL_OK) {

                // Need revisit: add resource counting
/*
                if (midpIncResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                    REPORT_INFO(LC_PROTOCOL,
                        "btl2cap_notif: Resource limit update error");
                }
*/

                // store client native connection handle for temporary storing
                KNI_SetIntField(thisHandle, peerHandleID, (jint)peer_handle);

                // copy address to Java object field
                KNI_SetRawArrayRegion(arrayHandle, 0, JAVACALL_BT_ADDRESS_SIZE, (jbyte*) peer_addr);

                REPORT_INFO(LC_PROTOCOL,
                    "btl2cap_notif::accept incoming connection accepted!");
            } else if (status == JAVACALL_WOULD_BLOCK) {
                midp_thread_wait(NETWORK_READ_SIGNAL, (int)handle, context);
            } else if (status == JAVACALL_FAIL) {
                char* pError;
                javacall_bt_l2cap_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "IO error in btl2cap_notif::accept (%s)\n", pError);
                REPORT_INFO(LC_PROTOCOL, gKNIBuffer);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));

            } else {
                char* pMsg = "Unknown error during btl2cap_notif::accept";
                REPORT_INFO(LC_PROTOCOL, pMsg);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
            }
        }
    }

    mtus = (imtu << 16) & 0xFFFF0000;
    mtus |= omtu & 0xFFFF;

    KNI_EndHandles();
    KNI_ReturnInt(mtus);
}

