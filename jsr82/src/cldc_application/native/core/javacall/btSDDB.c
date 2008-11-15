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
#include <javacall_memory.h>
#include <jsrop_memory.h>
#include <btCommon.h>
#include <stdio.h>
#include <string.h>
#include <emul.h>

#define INDEX_TO_SDDBID(index) (bt_sddbid_t)(0x10000 + (index))
#define SDDBID_TO_INDEX(id) (int)(id - 0x10000)

static bt_record_t *g_records;
static int g_count;

bt_record_t *get_record(bt_sddbid_t id)
{
    int index = SDDBID_TO_INDEX(id);
    if (index < 0 || index >= g_count) {
        return NULL;
    }
    return &g_records[index];
}

uint8_t *get_attr_value(bt_record_t *record, uint16_t id)
{
    uint8_t *data = (uint8_t *)record->data;
    uint8_t *end = data + record->size;
    uint8_t type = *data >> 3;
    uint8_t size = *data & 0x07;
    if (type != 6) {
        return NULL;
    }
    switch (size) {
        case 5:
        case 6:
        case 7:
            data += size - 3;
            break;
        default:
            return NULL;
    }
    while (data < end) {
        if (*data != 0x09) {
            return NULL;
        }
        data++;
        if (*data == (id & 0xff00) && *(data + 1) == (id & 0x00ff)) {
            return data + 2;
        }
        data += 2;
        type = *data >> 3;
        size = *data & 0x07;
        data++;
        if (type == 0) {
            continue;
        }
        switch (size) {
            case 0:
            case 1:
            case 2:
            case 3:
                data += 1 << size;
                break;
            case 5:
                data += *data + 1;
                break;
            case 6:
                data += (((uint16_t)*data << 8) | *(data + 1)) + 2;
                break;
            case 7:
                data += (((uint32_t)*data << 24) |
                        ((uint32_t)*(data + 1) << 16) |
                        ((uint16_t)*(data + 2) << 8) |
                        *(data + 3)) + 4;
                break;
        }
    }
    return NULL;
}

/*
 * Initializes service discovery database.
 *
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL if an error occurred
 */
javacall_result bt_sddb_initialize()
{
    return JAVACALL_OK;
}

/*
 * Closes service discovery database. All remaining Java-supplied service
 * records must be removed.
 */
javacall_result bt_sddb_finalize()
{
    int i;
    for (i = 0; i < g_count; i++) {
        if (g_records[i].data != NULL) {
            javacall_free(g_records[i].data);
        }
    }
    javacall_free(g_records);
    return JAVACALL_OK;
}

/*
 * Creates or updates service record in the service discovery database (SDDB).
 *
 * @param pId pointer to handle of a service record to be modified;
              <code>*pId == 0</code> for new record, and will be assigned
              to a new value
 * @param classes service classes
 * @param data service record data
 * @param size length of the data
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sddb_update_record(
        /*OUT*/unsigned long *pId,
        unsigned long classes,
        void *data,
        unsigned long size)
{
    if (*pId == BT_INVALID_SDDB_HANDLE) {
        int i;
        bt_record_t *rec = NULL;
        bt_sddbid_t id = BT_INVALID_SDDB_HANDLE;
        uint8_t *handle;
        for (i = 0; i < g_count; i++) {
            rec = &g_records[i];
            if (rec->data == NULL) {
                id = INDEX_TO_SDDBID(i);
                break;
            }
        }
        if (i == g_count) {
            bt_record_t *records = (bt_record_t *)javacall_malloc(
                    (g_count + 1) * sizeof(bt_record_t));
            if (records == NULL) {
                return JAVACALL_FAIL;
            }
            memcpy(records, g_records, g_count * sizeof(bt_record_t));
            javacall_free(g_records);
            g_records = records;
            rec = &g_records[g_count];
            id = INDEX_TO_SDDBID(g_count);
            g_count++;
        }
        rec->data = javacall_malloc(size);
        if (rec->data == NULL) {
            return JAVACALL_FAIL;
        }
        memcpy(rec->data, data, size);
        rec->size = size;
        rec->classes = classes;
        rec->id = *pId = id;
        handle = get_attr_value(rec, 0x0000);
        if (handle != NULL && *handle++ == 0x0a) {
            *handle++ = (uint8_t)(id >> 24);
            *handle++ = (uint8_t)(id >> 16);
            *handle++ = (uint8_t)(id >> 8);
            *handle = (uint8_t)id;
        }
        return JAVACALL_OK;
    } else {
        bt_record_t *rec = get_record(*pId);
        if (rec == NULL) {
            return JAVACALL_FAIL;
        }
        rec->classes = classes;
        if (rec->size != size) {
            void *data = javacall_malloc(size);
            if (data == NULL) {
                return JAVACALL_FAIL;
            }
            javacall_free(rec->data);
            rec->data = data;
            rec->size = size;
        }
        memcpy(rec->data, data, size);
        return JAVACALL_OK;
    }
}

/*
 * Updates PSM parameter value of the L2CAP protocol in the service record.
 * This function is required for push implementation.
 *
 * @param id handle of the service record to be updated
 * @param psm new PSM value
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sddb_update_psm(
        unsigned long id,
        unsigned short psm)
{
    const uint8_t l2cap_uuid[16] = {
        0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x10, 0x00,
        0x80, 0x00, 0x00, 0x80, 0x5f, 0x9b, 0x34, 0xfb
    };
    int i;
    uint8_t *data;
    bt_record_t *record = get_record(id);
    if (record == NULL) {
        return JAVACALL_FAIL;
    }
    data = (uint8_t *)record->data;
    if (data == NULL) {
        return JAVACALL_FAIL;
    }
    for (i = 0; i < (int)record->size - 18; i++) {
        if (!memcmp(data, l2cap_uuid, 16) && *(data + 16) == 0x09) {
            *(data + 17) = psm >> 8;
            *(data + 18) = psm & 0xff;
            return JAVACALL_OK;
        }
        data++;
    }
    return JAVACALL_FAIL;
}

/*
 * Updates channel parameter value of the RFCOMM protocol in the service record.
 * This function is required for push implementation.
 *
 * @param id handle of the service record to be updated
 * @param cn new channel value
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sddb_update_channel(
        unsigned long id,
        unsigned char cn)
{
    const uint8_t rfcomm_uuid[16] = {
        0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x10, 0x00,
        0x80, 0x00, 0x00, 0x80, 0x5f, 0x9b, 0x34, 0xfb
    };
    int i;
    uint8_t *data;
    bt_record_t *record = get_record(id);
    if (record == NULL) {
        return JAVACALL_FAIL;
    }
    data = (uint8_t *)record->data;
    if (data == NULL) {
        return JAVACALL_FAIL;
    }
    for (i = 0; i < (int)record->size - 17; i++) {
        if (!memcmp(data, rfcomm_uuid, 16) && *(data + 16) == 0x08) {
            *(data + 17) = cn;
            return JAVACALL_OK;
        }
        data++;
    }
    return JAVACALL_FAIL;
}

/*
 * Reads a service record from SDDB.
 * This function is required for push implementation.
 *
 * @param id service record handle
 * @param pClasses pointer to variable which will receive service classes
 * @param data buffer to be written to
 * @param pSize points to size of the buffer, or size query if *pSize == 0
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sddb_read_record(unsigned long id,
        /*OUT*/unsigned long *pClasses, void *data,
        /*OUT*/unsigned long *pSize)
{
    bt_record_t *rec = get_record(id);
    if (rec == NULL) {
        return JAVACALL_FAIL;
    }
    if (*pSize == 0) {
        *pSize = rec->size;
        return JAVACALL_OK;
    }
    if (*pSize > rec->size) {
        *pSize = rec->size;
    }
    memcpy(data, rec->data, *pSize);
    *pClasses = rec->classes;
    return JAVACALL_OK;
}

/*
 * Removes service record from SDDB.
 *
 * @param id handle of a service record to remove
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sddb_remove_record(unsigned long id)
{
    bt_record_t *record = get_record(id);
    if (record == NULL) {
        return JAVACALL_FAIL;
    }
    javacall_free(record->data);
    record->data = NULL;
    record->size = 0;
    record->classes = 0;
    return JAVACALL_OK;
}

/*
 * Returns handles for all service records in SDDB.
 * This function is required for the emulation build, when SDP server is
 * maintained by Java, rather than by Bluetooth stack.
 *
 * @param array buffer to receive service record handles
 * @param count number of entries in the array, or 0 for count query
 * @return number of entries available/saved to the array,
 *         or <code>0</code> if an error occurs
 */
unsigned long bt_sddb_get_records(
        /*OUT*/unsigned long *array,
        unsigned long count)
{
    int i;
    bt_sddbid_t *last;
    if (count <= 0) {
        count = 0;
        for (i = 0; i < g_count; i++) {
            if (g_records[i].data != NULL) {
                count++;
            }
        }
        return count;
    }
    last = array + count;
    count = 0;
    for (i = 0; i < g_count && array < last; i++) {
        if (g_records[i].data != NULL) {
            *array++ = INDEX_TO_SDDBID(i);
            count++;
        }
    }
    return count;
}

/*
 * Retrieves service classes for either a single service record,
 * or representing the current state of entire SDDB.
 *
 * @param id service record handle
 * @return major device service classes
 */
unsigned long bt_sddb_get_service_classes(unsigned long id)
{
    int i;
    uint32_t classes = BT_SDDB_SERVICE_CLASSES;
    if (id != BT_INVALID_SDDB_HANDLE) {
        bt_record_t *record = get_record(id);
        if (record == NULL) {
            return 0;
        }
        return (classes | record->classes);
    }
    for (i = 0; i < g_count; i++) {
        bt_record_t *record = &g_records[i];
        if (record->data != NULL) {
            classes |= record->classes;
        }
    }
    return classes;
}

/*
 * Reads a service record from the service search rsult.
 *
 * @param handle service record handle
 * @param data buffer to be written to
 * @param pSize points to size of the buffer, or size query if *pSize == 0
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sdp_get_service(
    const javacall_handle handle,
    /*OUT*/ javacall_uint8 *data,
    /*IN|OUT*/javacall_uint16 *pSize)
{
    emul_pdu_t* pdu = (emul_pdu_t*)handle;
    int length = *pSize;

    if (pdu->size < length) {
        length = pdu->size;
    }
    if (length > 0) {
        memcpy(data, pdu->data, length);
    }
    JAVAME_FREE(pdu->data);

    return JAVACALL_OK;
}
#define APPEND_CONN_REQ(handle, req) \
    APPEND_BYTE(CONNECTIONS); \
    APPEND_BYTE(handle); \
    APPEND_BYTE(req);

/*
 * Performs SDP request with specified parameters. This call must not be
 * blocking. It should return immediatelly and return results of request
 * via javanotify_bt_service_result and javanotify_bt_service_complete calls.
 *
 * @param addr Bluetooth address of a remote device
 * @param uuids an array of UUIDs of service
 * @param uuids_count number of UUID elements in array
 * @param attrs an array of service attributes IDs to be retrieved
 * @param attrs_count number of attribute IDs in array
 * @param handle a handle which could be used for searching interruption
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sdp_request(
    const javacall_bt_address addr,
    const javacall_bt_uuid *uuids,
    javacall_int32 uuids_count,
    const javacall_uint16   *attrs,
    javacall_int32 attrs_count,
    /* out */ javacall_handle *handle) 
{
    char a[]={0x02,0x00,0x02,0x00,0x27,0x35,0x22,0x1c,0xf0,0xe0,0xd0,
              0xc0,0xb0,0xa0,0x00,0x90,0x80,0x70,0x60,0x50,0x40,0x30,
              0x20,0x10,0x1c,0x00,0x00,0x11,0x01,0x00,0x00,0x10,0x00,
              0x80,0x00,0x00,0x80,0x5f,0x9b,0x34,0xfb,0x0f,0xff,0x00};
    char a1[] ={0xaf,0x89,0x67,0x45,0x23,0x01};
    int i, len = sizeof(uuids->value.uuid128);
    javacall_uint8 uuidType;
    char* p;

    if (*handle == 0) {
    START_REQUEST
        APPEND_BYTE(SDP_REQUEST);
        APPEND_BYTES(addr, BT_ADDRESS_SIZE);
        APPEND_INT(uuids_count);
        for (i=0; i<uuids_count; i++) {
            /* Some trick to avoid a structure align problem */
            uuidType = uuids[i].type;
            p = (char*)&(uuids[i].value);
            APPEND_BYTE(uuidType);
            APPEND_BYTES(p, len);
        }
        APPEND_INT(attrs_count);
        p = (char*)attrs;
        APPEND_BYTES(p, sizeof(javacall_uint16)*attrs_count);
    END_REQUEST
    *handle = (javacall_handle)1;
    return JAVACALL_WOULD_BLOCK;
    } else {
        *handle = (javacall_handle)emul_data.access_code;
        return JAVACALL_OK;
    }

}

/*
 * Cancells specified search transaction.
 *
 * @param handle a handle of transaction to be cancelled
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_sdp_cancel(javacall_handle handle) 
{
    return JAVACALL_NOT_IMPLEMENTED;
 }
