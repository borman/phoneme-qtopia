/*
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

#include <javacall_bt.h>
#include <jsrop_exceptions.h>
#include <jsrop_memory.h>
#include <btUtils.h>

#include <midp_thread.h>
#include <sni.h>

/*
 * Retrieves service record from the service search result.
 *
 * @param recHandle native handle of the service record
 * @param array byte array which will receive the data,
 *         or null for size query
 * @return size of the data read/required
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_jsr082_bluetooth_SDPTransaction_getServiceRecord0)
{
    jint           retval  = 0;
    javacall_handle id      = 0;
    unsigned long  classes = 0;
    javacall_uint8  *data    = NULL;
    javacall_uint16 size    = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataHandle);
    KNI_GetParameterAsObject(2, dataHandle);

    id = (javacall_handle)KNI_GetParameterAsInt(1);
    if (!KNI_IsNullHandle(dataHandle))
        size = KNI_GetArrayLength(dataHandle);

    data = JAVAME_MALLOC(size);
    if (data == NULL) {
        KNI_ThrowNew(jsropOutOfMemoryError, "Out of memory inside SDDB.readRecord()");
    } else {

        if (javacall_bt_sdp_get_service(id, data, &size) == JAVACALL_OK) {
            retval = size;
            if (!KNI_IsNullHandle(dataHandle)) {
                KNI_SetRawArrayRegion(dataHandle, 0, size, data);
            }
        } else {
            retval = 0;
        }
        JAVAME_FREE(data);
    }
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}

/*
 * Performs service search request with specified parameters
 *
 * @param btAddress the Bluetooth address of the remote device
 * @param UUIDs array of string UUIDs
 * @param attrs array of attributes
 * @return ID offsetof transaction if succesfully created or 0 if failed
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_jsr082_bluetooth_NativeSDPClient_serviceSearchRequest0)
{
    javacall_result status = JAVACALL_OK;
    javacall_bt_address addr;
    int uuids_array_length, attrs_array_length, cur_index, i;
    javacall_bt_uuid *uuids = NULL, *cur_uuid;
    javacall_uint16 *attrs = NULL, *cur_attr;
    javacall_handle handle = 0;

    void* context = NULL;
    MidpReentryData* info;

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetParameterAsObject(1, objectHandle);

    getBtAddr(objectHandle, addr);
    
    do {
        KNI_GetParameterAsObject(2, objectHandle);
        if (KNI_IsNullHandle(objectHandle)) {
            break;
        }
        uuids_array_length = KNI_GetArrayLength(objectHandle);
        if (uuids_array_length > 0) {
            uuids = JAVAME_MALLOC(
                sizeof(javacall_bt_uuid) * uuids_array_length);
            if (uuids == NULL) {
                break;
            }
            for (cur_uuid = uuids, cur_index = 0;
                 cur_index < uuids_array_length;
                 cur_index++, cur_uuid++) {
                int uuid_length;
                jbyte* data;
                KNI_StartHandles(1);
                KNI_DeclareHandle(uuidHandle);
                
                KNI_GetObjectArrayElement(objectHandle, cur_index,
                                                                    uuidHandle);
                if (KNI_IsNullHandle(uuidHandle)) {
                    break;
                }
                uuid_length = KNI_GetArrayLength(uuidHandle);
                data = JAVAME_MALLOC(uuid_length * sizeof(jbyte));
                if (data == NULL) {
                    status = JAVACALL_FAIL;
                    break;
                }
                KNI_GetRawArrayRegion(uuidHandle, 0, uuid_length, data);
                if (uuid_length == 2) {
                    javacall_uint16 uuid = data[0];
                    uuid = (uuid << 8) | data[1];
                    cur_uuid->type = JAVACALL_BT_UUID_16;
                    cur_uuid->value.uuid16 = uuid;
                }
                else if (uuid_length <= 4) {
                    cur_uuid->type = JAVACALL_BT_UUID_32;
                    cur_uuid->value.uuid32 = 0;
                    for (i=0;i<uuid_length;i++) {
                        cur_uuid->value.uuid32 = (cur_uuid->value.uuid32 << 8) | data[i];
                    }
                }
                else if (uuid_length <= 16) {
                    cur_uuid->type = JAVACALL_BT_UUID_128;
                    for (i=0;i<uuid_length;i++) {
                        cur_uuid->value.uuid128[i] = data[i];
                    }
                }
                else {
                    status = JAVACALL_FAIL;
                    JAVAME_FREE(data);
                    break;
                }
                JAVAME_FREE(data);
                KNI_EndHandles();
            }
        }
        if (status != JAVACALL_OK) {
            break;
        }

        KNI_GetParameterAsObject(3, objectHandle);
        if (KNI_IsNullHandle(objectHandle)) {
            break;
        }
        attrs_array_length = KNI_GetArrayLength(objectHandle);
        if (attrs_array_length > 0) {
            attrs = JAVAME_MALLOC(sizeof(javacall_uint16) * attrs_array_length);
            if (attrs == NULL) {
                break;
            }
            for (cur_attr = attrs, cur_index = 0;
                 cur_index < attrs_array_length;
                 cur_index++, cur_attr++) {
                *cur_attr = (javacall_uint16)KNI_GetIntArrayElement(
                                                    objectHandle, cur_index);
            }
        }
    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {   /* First invocation */
        
        status = javacall_bt_sdp_request(addr,
                                         uuids,
                                         uuids_array_length,
                                         attrs,
                                         attrs_array_length,
                                         &handle);

        if (status == JAVACALL_OK) {
        } else if (status == JAVACALL_FAIL) {
        } else if (status == JAVACALL_WOULD_BLOCK) {
            midp_thread_wait(JSR82_SIGNAL, 1, context);
        } else {
        }
    } else {  /* Reinvocation after unblocking the thread */
        context = info->pResult;

        if ((javacall_handle)info->descriptor != handle) {
//            REPORT_CRIT2(LC_PROTOCOL,
//                "btl2cap::connect handles mismatched %d != %d\n",
//                 handle, (javacall_handle)info->descriptor);
        }
        handle = 1;
        status = javacall_bt_sdp_request(addr,
                                         uuids,
                                         uuids_array_length,
                                         attrs,
                                         attrs_array_length,
                                         &handle);
        if (status == JAVACALL_OK) {
            // Need revisit: add bluetooth activity indicator
//            DEC_BT_INDICATOR;
        } else if (status == JAVACALL_WOULD_BLOCK) {
            midp_thread_wait(JSR82_SIGNAL, 1, context);

        } else  {
        }
    }
    } while(0);
    if (uuids != NULL) {
        JAVAME_FREE(uuids);
    }    
    if (attrs != NULL) {
        JAVAME_FREE(attrs);
    }
    
    KNI_EndHandles();
//    KNI_ReturnInt(status == JAVACALL_OK ? handle : 0);
    KNI_ReturnInt(handle);
}

/*
 * Performs service search cancellation
 *
 * @param transID ID of transaction to be cancelled
 * @return true if Ok
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeSDPClient_cancelSearchRequest0)
{
    javacall_int32 transID = KNI_GetParameterAsInt(1);
    
    KNI_ReturnBoolean(javacall_bt_sdp_cancel((javacall_handle)transID) ==
                                            JAVACALL_OK ? KNI_TRUE : KNI_FALSE);
}

