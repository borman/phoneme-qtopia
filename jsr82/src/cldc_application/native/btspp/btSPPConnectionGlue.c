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
#include <btCommon.h>
#include <btRFCOMMCommon.h>

#include <midp_thread.h>
#include <sni.h>
#include <midp_libc_ext.h>
#include <kni_globals.h>
//#include <midpResourceLimit.h>

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_setThisConnHandle0(void) {

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_initialize(void) {

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_finalize(void) {
    javacall_handle handle;
    int status = JAVACALL_FAIL;

    REPORT_INFO(LC_PROTOCOL, "btspp::finalize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);

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
            midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                "IO error in btspp::finalize (%s)\n", pError);
            REPORT_ERROR(LC_PROTOCOL, gKNIBuffer);
        } else if (status == JAVACALL_WOULD_BLOCK) {
            /* blocking during finalize is not supported */
            REPORT_CRIT1(LC_PROTOCOL,
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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_create0(void) {
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
        KNI_ThrowNew(midpIOException,
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
 * Performs client connection establishment.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
 * @param addr bluetooth address of device to connect to
 * @param cn Channel number (CN) value
 * @throws IOException if any I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_connect0(void) {
    int cn = (int)KNI_GetParameterAsInt(2);

    javacall_handle handle = JAVACALL_BT_INVALID_HANDLE;
    int status;
    void* context = NULL;
    MidpReentryData* info;
    javacall_bt_address addr;

    KNI_StartHandles(2);
    KNI_DeclareHandle(thisHandle);
    KNI_DeclareHandle(arrayHandle);
    KNI_GetThisPointer(thisHandle);
    KNI_GetParameterAsObject(1, arrayHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);

    REPORT_INFO1(LC_PROTOCOL, "btspp::connect handle=%d", handle);

    /* copy address from Java input array */
    KNI_GetRawArrayRegion(arrayHandle, 0, sizeof(javacall_bt_address), (jbyte*) addr);

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {   /* First invocation */
        // Need revisit: add resource counting
        /*
         * Verify that the resource is available well within limit as per
         * the policy in ResourceLimiter
         */
/*
        if (midpCheckResourceLimit(RSC_TYPE_BT_CLI, 1) == 0) {
            const char* pMsg = "Resource limit exceeded for BT client sockets";
            REPORT_INFO(LC_PROTOCOL, pMsg);
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
        } else {
*/
            status = javacall_bt_rfcomm_connect(handle, addr, cn);

            if (status == JAVACALL_OK) {
                // Need revisit: add resource counting
/*
                if (midpIncResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                    REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
                }
*/
            } else if (status == JAVACALL_FAIL) {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                        "IO error in btspp::connect (%s)\n", pError);
                REPORT_INFO(LC_PROTOCOL, gKNIBuffer);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));
            } else if (status == JAVACALL_WOULD_BLOCK) {
                // Need revisit: add bluetooth activity indicator
//                INC_BT_INDICATOR;
                // Need revisit: add resource counting
/*
                if (midpIncResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                    REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
                }
*/
                REPORT_INFO1(LC_PROTOCOL,
                    "btspp::connect is waiting for complete notify"
                    ", handle = %d\n", handle);
                midp_thread_wait(NETWORK_WRITE_SIGNAL, (int)handle, context);
            } else {
                char* pMsg = "Unknown error during btspp::connect";
                REPORT_INFO(LC_PROTOCOL, pMsg);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(pMsg));
            }
//        }
    } else {  /* Reinvocation after unblocking the thread */
        context = info->pResult;

        if ((javacall_handle)info->descriptor != handle) {
            REPORT_CRIT2(LC_PROTOCOL,
                "btspp::connect handles mismatched %d != %d\n",
                 handle, (javacall_handle)info->descriptor);
        }

        status = javacall_bt_rfcomm_connect(handle, addr, cn);

        if (status == JAVACALL_OK) {
            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else if (status == JAVACALL_WOULD_BLOCK) {
            midp_thread_wait(NETWORK_WRITE_SIGNAL, (int)handle, context);
        } else  {
            char* pError;

            KNI_SetIntField(thisHandle, connHandleID, (jint)JAVACALL_BT_INVALID_HANDLE);

            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;

            // Need revisit: add resource counting
/*
            if (midpDecResourceCount(RSC_TYPE_BT_CLI, 1) == 0) {
                REPORT_INFO(LC_PROTOCOL, "Resource limit update error");
            }
*/
            javacall_bt_rfcomm_get_error(handle, &pError);
            midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "Error in btspp::connect (%s)", pError);
            REPORT_INFO(LC_PROTOCOL, gKNIBuffer);
            KNI_ThrowNew(midpConnectionNotFoundException,
                EXCEPTION_MSG(gKNIBuffer));
        }
    }

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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_close0(void) {
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
            KNI_ThrowNew(midpIOException,
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
 * Sends the specified data via Bluetooth stack.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
 * @param buf the data to send
 * @param off the offset into the data buffer
 * @param len the length of the data in the buffer
 * @return total number of send bytes,
 *         or <code>0</code> if empty pcaket has been send
 * @throws IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_send0(void) {
    int length, offset;
    javacall_handle handle;
    int bytesWritten = 0;
    int status = JAVACALL_FAIL;
    void *context = NULL;
    MidpReentryData* info;

    offset = (int)KNI_GetParameterAsInt(2);
    length = (int)KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(arrayHandle);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);
    KNI_GetParameterAsObject(1, arrayHandle);

    REPORT_INFO3(LC_PROTOCOL,
        "btspp::send off=%d len=%d handle=%d\n", offset, length, handle);

    info = (MidpReentryData*)SNI_GetReentryData(NULL);

    // Need revisit: add bluetooth activity indicator
//    START_BT_INDICATOR;

    if (info == NULL) {   /* First invocation */
        if (JAVACALL_BT_INVALID_HANDLE == handle) {
            KNI_ThrowNew(midpIOException,
                EXCEPTION_MSG("Invalid handle during btspp::send"));
        } else {
            // Need revisit: add bluetooth activity indicator
//            INC_BT_INDICATOR;
            SNI_BEGIN_RAW_POINTERS;
            status = javacall_bt_rfcomm_send(handle,
                           (char*)&(JavaByteArray(arrayHandle)[offset]),
                           length, &bytesWritten);
            SNI_END_RAW_POINTERS;
        }
    } else { /* Reinvocation after unblocking the thread */
        if (JAVACALL_BT_INVALID_HANDLE == handle) {
            /* closed by another thread */
            KNI_ThrowNew(midpInterruptedIOException, EXCEPTION_MSG(
                         "Interrupted IO error during btspp::send"));
            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else {
            if ((javacall_handle)info->descriptor != handle) {
                REPORT_CRIT2(LC_PROTOCOL,
                    "btspp::send handles mismatched %d != %d\n",
                    handle, (javacall_handle)info->descriptor);
            }
            context = info->pResult;
            SNI_BEGIN_RAW_POINTERS;
            status = javacall_bt_rfcomm_send(handle,
                       (char*)&(JavaByteArray(arrayHandle)[offset]),
                       length, &bytesWritten);
            SNI_END_RAW_POINTERS;
        }
    }

   if (JAVACALL_BT_INVALID_HANDLE != handle) {
        if (status == JAVACALL_OK) {
            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else {
            char* pError;
            javacall_bt_rfcomm_get_error(handle, &pError);
            REPORT_INFO1(LC_PROTOCOL, "btspp::send (%s)\n", pError);

            if (status == JAVACALL_WOULD_BLOCK) {
                midp_thread_wait(NETWORK_WRITE_SIGNAL, (int)handle, context);
            } else if (status == JAVACALL_INTERRUPTED) {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "Interrupted IO during btspp::send (%s)", pError);
                KNI_ThrowNew(midpInterruptedIOException,
                    EXCEPTION_MSG(gKNIBuffer));

                // Need revisit: add bluetooth activity indicator
//                DEC_BT_INDICATOR;
            } else {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "IO error during btspp::send (%s)", pError);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));

                // Need revisit: add bluetooth activity indicator
//                DEC_BT_INDICATOR;
            }
        }
    }

    REPORT_INFO1(LC_PROTOCOL, "btspp::send bytes=%d\n", bytesWritten);

    KNI_EndHandles();
    KNI_ReturnInt((jint)bytesWritten);
}


/*
 * Reads data from a packet received via Bluetooth stack.
 *
 * Note: the method gets native connection handle directly from
 * <code>handle<code> field of <code>RFCOMMConnectionImpl</code> object.
 *
 * @param buf the buffer to read to
 * @param offset the start offset in array <code>buf</code>
 *               at which the data to be written
 * @param size the maximum number of bytes to read,
 *             the rest of the packet is discarded.
 * @return total number of bytes read into the buffer or
 *             <code>0</code> indicates end-of-stream
 * @throws IOException if an I/O error occurs
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_receive0(void) {
    int length, offset;
    javacall_handle handle;
    int bytesRead = -1;
    int status = JAVACALL_FAIL;
    void* context = NULL;
    MidpReentryData* info;

    offset = (int)KNI_GetParameterAsInt(2);
    length = (int)KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(arrayHandle);
    KNI_DeclareHandle(thisHandle);
    KNI_GetThisPointer(thisHandle);
    handle = (javacall_handle)KNI_GetIntField(thisHandle, connHandleID);
    KNI_GetParameterAsObject(1, arrayHandle);

    REPORT_INFO3(LC_PROTOCOL,
        "btspp::receive off=%d len=%d handle=%d\n", offset, length, handle);

    info = (MidpReentryData*)SNI_GetReentryData(NULL);

    // Need revisit: add bluetooth activity indicator
//    START_BT_INDICATOR;

    if (info == NULL) {   /* First invocation */
        if (JAVACALL_BT_INVALID_HANDLE == handle) {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(
                "Invalid handle during btspp::receive"));
        } else {
            // Need revisit: add bluetooth activity indicator
//            INC_BT_INDICATOR;

            SNI_BEGIN_RAW_POINTERS;
            status = javacall_bt_rfcomm_receive(handle,
                (unsigned char*)&(JavaByteArray(arrayHandle)[offset]),
                length, &bytesRead);
            SNI_END_RAW_POINTERS;
        }
    } else {  /* Reinvocation after unblocking the thread */
        if (JAVACALL_BT_INVALID_HANDLE == handle) {
            /* closed by another thread */
            KNI_ThrowNew(midpInterruptedIOException, EXCEPTION_MSG(
                         "Interrupted IO error during btspp::receive"));

            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else {
            if ((javacall_handle)info->descriptor != handle) {
                REPORT_CRIT2(LC_PROTOCOL,
                    "btspp::receive handles mismatched %d != %d\n",
                    handle, (javacall_handle)info->descriptor);
            }
            context = info->pResult;
            SNI_BEGIN_RAW_POINTERS;
            status = javacall_bt_rfcomm_receive(handle,
                (unsigned char*)&(JavaByteArray(arrayHandle)[offset]),
                length, &bytesRead);
            SNI_END_RAW_POINTERS;
        }
    }

    REPORT_INFO1(LC_PROTOCOL, "btspp::receive bytes=%d\n", bytesRead);

    if (JAVACALL_BT_INVALID_HANDLE != handle) {
        if (status == JAVACALL_OK) {
            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else {
            char* pError;
            javacall_bt_rfcomm_get_error(handle, &pError);
            REPORT_INFO1(LC_PROTOCOL, "btspp::receive (%s)\n", pError);

            if (status == JAVACALL_WOULD_BLOCK) {
                midp_thread_wait(NETWORK_READ_SIGNAL, (int)handle, context);
            } else if (status == JAVACALL_INTERRUPTED) {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                        "Interrupted IO error during btspp::receive (%s)",
                        pError);
                KNI_ThrowNew(midpInterruptedIOException,
                    EXCEPTION_MSG(gKNIBuffer));

                // Need revisit: add bluetooth activity indicator
//                DEC_BT_INDICATOR;
            } else {
                char* pError;
                javacall_bt_rfcomm_get_error(handle, &pError);
                midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
                    "Unknown error during btspp::receive (%s)", pError);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));

                // Need revisit: add bluetooth activity indicator
//                DEC_BT_INDICATOR;
            }
        }
    }

    // Need revisit: add bluetooth activity indicator
//    STOP_BT_INDICATOR;

    KNI_EndHandles();
    KNI_ReturnInt((jint)bytesRead);
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
Java_com_sun_jsr082_bluetooth_btspp_BTSPPConnectionImpl_available0(void) {
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
        midp_snprintf(gKNIBuffer, KNI_BUFFER_SIZE,
            "IO error during btspp::::available (%s)", pError);
        REPORT_ERROR(LC_PROTOCOL, gKNIBuffer);
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(gKNIBuffer));
        break;

    default: /* illegal argument */
        REPORT_CRIT(LC_PROTOCOL, "Internal error in btspp::available");
    }

    KNI_EndHandles();
    KNI_ReturnInt(count);
}
