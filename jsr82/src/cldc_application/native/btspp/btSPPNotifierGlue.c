/*
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

#include <btMacros.h>
#include <javacall_bt.h>
#include <btRFCOMMCommon.h>

#include <midp_thread.h>
#include <sni.h>
#include <midp_libc_ext.h>
#include <kni_globals.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>
#include <midpString.h>

#include <push_server_resource_mgmt.h>
#include <suitestore_common.h>

#include "btPush.h"

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_initialize(void) {

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_finalize(void) {
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
            midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "IO error in bt_rfcomm_notif::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gKNIBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_CRIT1(LC_PROTOCOL,
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
            midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "IO error in bt_rfcomm_notif::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gKNIBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_CRIT1(LC_PROTOCOL,
                "btspp_notif::finalize blocked, handle = %d\n", peer);
        }
    }

    REPORT_INFO(LC_PROTOCOL, "btspp_notif::finalize done!");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Checks out (takes ownership of) an active server connection maintained
 * by push subsystem.
 *
 * @param url URL used during registration of the push entry
 * @param suiteId suite id
 * @return true if the operation succeeds, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_pushCheckout(void)
{
    jboolean retval = KNI_FALSE;
    MidpString wsUrl;
    SuiteIdType suiteId;
    char *szUrl;
    bt_port_t port;
    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(urlHandle);
    KNI_GetThisPointer(thisHandle);
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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_create0(void) {
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
        KNI_ThrowNew(midpIOException,
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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_listen0(void) {

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
            KNI_ThrowNew(midpIOException,
                EXCEPTION_MSG("RFCOMM notifier listen failed"));
        } else {
            REPORT_INFO(LC_PROTOCOL, "btspp_notif::listen done!");
        }
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * Accepts incoming client connection request.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>BTSPPNotifierImpl</code> object.
 *
 * Note: new native connection handle to work with accepted incoming
 * client connection is setted directly to <code>handle</code> field of
 * appropriate <code>RFCOMMConnectionImpl</code> object.
 *
 * @throws IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_accept0(void) {
    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;
    javacall_handle peer_handle = JAVACALL_BT_INVALID_HANDLE;
    MidpReentryData* info;
    int status = JAVACALL_FAIL;
    int processStatus = KNI_FALSE;
    void *context = NULL;
    javacall_bt_address peer_addr;
    unsigned char *address = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(arrayHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, notifHandleID);
    KNI_GetObjectField(thisHandle, peerAddrID, arrayHandle);

    if (handle == JAVACALL_BT_INVALID_HANDLE) {
        REPORT_ERROR(LC_PROTOCOL,
            "RFCOMM notifier was closed before btspp_notif::accept");
        KNI_ThrowNew(midpInterruptedIOException, EXCEPTION_MSG(
            "RFCOMM notifier was closed"));
    } else {
        bt_pushid_t pushid = KNI_GetIntField(thisHandle, pushHandleID);
        if (pushid != BT_INVALID_PUSH_HANDLE) {
            if (bt_push_checkout_client(pushid, &peer_handle, peer_addr,
                    NULL, NULL) == JAVACALL_OK) {
                pushcheckoutaccept((int)handle);
                processStatus = KNI_TRUE;
                status = JAVACALL_OK;
            }
        }
        if (peer_handle == JAVACALL_BT_INVALID_HANDLE) {

        info = (MidpReentryData*)SNI_GetReentryData(NULL);
		if (info == NULL) {   /* First invocation */
            REPORT_INFO1(LC_PROTOCOL,
                "btspp_notif::accept handle=%d\n", handle);

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
                status = javacall_bt_rfcomm_accept(
                    handle, &peer_handle, &peer_addr);
				processStatus = KNI_TRUE;
//            }
		} else {  /* Reinvocation after unblocking the thread */
			if ((javacall_handle)info->descriptor != handle) {
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "btspp_notif::accept handles mismatched %d != %d\n",
                    handle, info->descriptor);
                REPORT_CRIT(LC_PROTOCOL, gKNIBuffer);
                KNI_ThrowNew(midpIllegalStateException, EXCEPTION_MSG(
                    "Internal error in btspp_notif::accept"));
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
                    context = info->pResult;
				    status = javacall_bt_rfcomm_accept(
                        handle, &peer_handle, &peer_addr);
					processStatus = KNI_TRUE;
//                }
            }
        }

        }

        if (processStatus) {
            REPORT_INFO1(LC_PROTOCOL,
                "btspp_notif::accept server handle=%d\n", handle);
            if (status == JAVACALL_OK) {

                // Need revisit: add resource counting
/*
                if (midpIncResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                    REPORT_INFO(LC_PROTOCOL,
                        "btspp_notif: Resource limit update error");
                }
*/

                // store client native connection handle for temporary storing
                KNI_SetIntField(thisHandle, peerHandleID, (jint)peer_handle);

                // copy address to Java object field
                KNI_SetRawArrayRegion(arrayHandle, 0, JAVACALL_BT_ADDRESS_SIZE, (jbyte*) peer_addr);
//                SNI_BEGIN_RAW_POINTERS;
//                address = JavaByteArray(arrayHandle);
//                for (i = 0; i < BT_ADDRESS_SIZE; i++) {
//                    address[i] = peer_addr[i];
//                }
//                SNI_END_RAW_POINTERS;

                REPORT_INFO(LC_PROTOCOL,
                    "btspp_notif::accept incoming connection accepted!");
            } else if (status == JAVACALL_WOULD_BLOCK) {
                midp_thread_wait(NETWORK_READ_SIGNAL, (int)handle, context);
            } else if (status == JAVACALL_FAIL) {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "IO error in btspp_notif::accept (%s)\n", pError);
                REPORT_INFO(LC_PROTOCOL, gKNIBuffer);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));

            } else {
                char* pMsg = "Unknown error during btspp_notif::accept";
                REPORT_INFO(LC_PROTOCOL, pMsg);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
            }
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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPNotifierImpl_close0(void) {
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
            KNI_ThrowNew(midpIOException,
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
            KNI_ThrowNew(midpIOException,
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
