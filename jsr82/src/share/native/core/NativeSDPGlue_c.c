/*
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

#include <javacall_bt.h>
#include <jsrop_exceptions.h>
#include <jsrop_memory.h>
#include <btUtils.h>

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
 * Performs service search cancellation
 *
 * @param transID ID of transaction to be cancelled
 * @return true if Ok
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeSDPClient_cancelSearchRequest0)
{
    javacall_int32 transID = KNI_GetParameterAsInt(1);
    javacall_result res;

    res = javacall_bt_sdp_cancel((javacall_handle)transID);
    if ((res == JAVACALL_OK) || (res == JAVACALL_NOT_IMPLEMENTED)) {
        KNI_ReturnBoolean( KNI_TRUE );
    } else {
        KNI_ReturnBoolean( KNI_FALSE );
    }
    
}

