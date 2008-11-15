/*
 *
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

#ifndef NO_PUSH
#include "btPush.h"
#endif

/* IMPL_NOTE: revisit */
/* #include "btCommon.h" */

#include <stddef.h>

#include <javacall_bt.h>
#include <jsrop_exceptions.h>

#include <kni.h>
#include <commonKNIMacros.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>

/*
 * Creates or updates service record in the SDDB.
 *
 * @param handle handle of the service record to be updated;
 *         if equals to 0, a new record will be created
 * @param classes device service classes associated with the record
 * @param data binary data containing attribute-value pairs in the format
 *         identical to the one used in the AttributeList parameter of
 *         the SDP_ServiceAttributeResponse PDU
 * @return service record handle, or 0 if the operation fails
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_SDDB_updateRecord(void)
{
    unsigned long  retval  = 0;
    unsigned long  old_id  = KNI_GetParameterAsInt(1);
    unsigned long  id      = old_id;
    unsigned long  classes = KNI_GetParameterAsInt(2);
    size_t         size    = 0;
    jbyte         *data    = NULL;
#ifndef  NO_PUSH
    bt_record_t    record  = {0};
#endif

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataHandle);
    KNI_GetParameterAsObject(3, dataHandle);
    size = KNI_GetArrayLength(dataHandle);
    data = malloc(size);

    /* copy data from Java input array */
    KNI_GetRawArrayRegion(dataHandle, 0, size, data);

    if (javacall_bt_sddb_update_record(&id, classes, data, size) == JAVACALL_OK) {
        retval = id;
#ifndef  NO_PUSH
        if (old_id != BT_INVALID_SDDB_HANDLE) {
            if (javacall_bt_sddb_read_record(id,
                &record.classes, record.data,
                &record.size)== JAVACALL_OK) {
                record.id = id;
                record.data = malloc(record.size);
                if (javacall_bt_sddb_read_record(id,
                    &record.classes, record.data,
                    &record.size)== JAVACALL_OK) {
                    bt_push_update_record(old_id, &record);
                }
                free(record.data);
            }
        }
#endif
        javacall_bt_stack_set_service_classes(classes);
    } else {
        retval = 0;
    }

    free(data);

    KNI_EndHandles();
    KNI_ReturnInt(retval);
}

/*
 * Removes service record from the SDDB.
 *
 * @param handle hanlde of the service record to be deleted
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_SDDB_removeRecord(void)
{
    javacall_bt_sddb_remove_record(KNI_GetParameterAsInt(1));
    javacall_bt_stack_set_service_classes(javacall_bt_sddb_get_service_classes(0));
    KNI_ReturnVoid();
}

/*
 * Retrieves service record from the SDDB.
 *
 * @param handle handle of the service record to be retrieved
 * @param data byte array which will receive the data,
 *         or null for size query
 * @return size of the data read/required
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_SDDB_readRecord(void)
{
    jint           retval  = 0;
    unsigned long  id      = 0;
    unsigned long  classes = 0;
    jbyte         *data    = NULL;
    unsigned long  size    = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataHandle);
    KNI_GetParameterAsObject(2, dataHandle);

    id = KNI_GetParameterAsInt(1);
    if (!KNI_IsNullHandle(dataHandle))
        size = KNI_GetArrayLength(dataHandle);

    data = malloc(size);
    if (data == NULL) {
        KNI_ThrowNew(jsropOutOfMemoryError, "Out of memory inside SDDB.readRecord()");
    } else {
        if (javacall_bt_sddb_read_record(id, &classes, data, &size) == JAVACALL_OK) {
            retval = size;
            if (!KNI_IsNullHandle(dataHandle)) {
                KNI_SetRawArrayRegion(dataHandle, 0, size, data);
    }
    } else {
        retval = 0;
    }
        free(data);
    }
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}
 
/*
 * Retrieves service classes of a record in the SDDB.
 *
 * @param handle handle of the service record
 * @return service classes set for the record
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_SDDB_getServiceClasses(void)
{
    KNI_ReturnInt(javacall_bt_sddb_get_service_classes(KNI_GetParameterAsInt(1)));
}

/*
 * Retrieves handles of all service records in the SDDB.
 *
 * @param handles array to receive handles, or null for count query
 * @return number of entries read/available
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_SDDB_getRecords(void)
{
    jint retval;
    KNI_StartHandles(1);
    KNI_DeclareHandle(arrayHandle);
    KNI_GetParameterAsObject(1, arrayHandle);
    if (KNI_IsNullHandle(arrayHandle)) {
        retval = javacall_bt_sddb_get_records(NULL, 0);
    } else {
        unsigned long  count = KNI_GetArrayLength(arrayHandle);
        unsigned long  bytes = count * sizeof(unsigned long);
        unsigned long *data  = malloc(bytes);
        if (data == NULL) {
            KNI_ThrowNew(jsropOutOfMemoryError,
                         "Out of memory inside SDDB.getRecords()");
        } else {
            retval = javacall_bt_sddb_get_records(data, count);
            KNI_SetRawArrayRegion(arrayHandle, 0, bytes, (jbyte *)data);
            free(data);
        }
    }
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}

#ifdef NO_PUSH

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_SDDB_initialize(void)
{
    javacall_bt_sddb_initialize();
    KNI_ReturnVoid();
}


KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_SDDB_finalize(void)
{
    javacall_bt_sddb_finalize();
    KNI_ReturnVoid();
}
#endif
