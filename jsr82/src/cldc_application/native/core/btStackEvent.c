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

#include "javanotify_bt.h"
#include "btCommon.h"
#include <string.h>

#define MAX_INQUIRY_RESPONSES 18

#define MAX_HCI_QUEUE_SIZE 4
#define MAX_HCI_EVENT_SIZE 257

static unsigned char hci_queue[MAX_HCI_QUEUE_SIZE][MAX_HCI_EVENT_SIZE];
static int hci_queue_head, hci_queue_tail, hci_queue_size;

/*
 * Checks if Bluetooth events are available.
 *
 * @param retval pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if there are pending events,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_stack_check_events(javacall_bool *retval)
{
    *retval = hci_queue_size > 0 ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

/*
 * Reads stack implementation-specific event data.
 *
 * @param data buffer where the data will be written to
 * @param len length of the buffer in bytes
 * @param retval pointer to variable where the result is to be stored:
 *         actual number of bytes read
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_stack_read_data(void *data, int len, int *retval)
{
    void *event;
    int size;
    if (hci_queue_size == 0) {
        return JAVACALL_FAIL;
    }
    event = hci_queue[hci_queue_head++];
    if (hci_queue_head == MAX_HCI_QUEUE_SIZE) {
        hci_queue_head = 0;
    }
    size = *((uint8_t *)event + 1) + 2;
    if (size > len) {
        size = len;
    }
    memcpy(data, event, size);
    *retval = size;
    hci_queue_size--;
    return JAVACALL_OK;
}

void add_hci_event(void *event)
{
    if (hci_queue_size >= MAX_HCI_QUEUE_SIZE) {
        return;
    }
    memcpy(hci_queue[hci_queue_tail++], event, *((uint8_t *)event + 1) + 2);
    if (hci_queue_tail == MAX_HCI_QUEUE_SIZE) {
        hci_queue_tail = 0;
    }
    hci_queue_size++;
}

/*
 * Auxiliary functions used for the data packing
 */
void packByte(uint8_t** p_dst, const uint8_t byte) {
    *(*p_dst)++ = byte;
}

void packString(uint8_t** p_dst, const uint8_t* bytes, const int len) {
    strncpy((char*)*p_dst, (char*)bytes, len);
    *p_dst += len;
}

void packBytes(uint8_t** p_dst, uint8_t* bytes, const int len) {
    memcpy((void*)*p_dst, (void*)bytes, len);
    *p_dst += len;
}
 /*
  * Javanotify functions
  */
void javanotify_bt_inquiry_complete(javacall_bool success)
{
    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
	} t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
    packByte(&p_event, JAVACALL_BT_EVENT_INQUIRY_COMPLETE);
    packByte(&p_event, sizeof(t_event) - 2);
    packByte(&p_event, (success == JAVACALL_TRUE ? 0x00 : 0xff));
    add_hci_event(event);
}

void javanotify_bt_device_discovered(
        const javacall_bt_address addr,
        int deviceClass)
{
    typedef struct {
        javacall_bt_address bdaddr;
        uint8_t pscan_rep_mode;
        uint8_t pscan_period_mode;
        uint8_t pscan_mode;
        uint8_t dev_class[3];
        uint16_t clock_offset;
	} t_inquiry_response;
	typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t num_responses;
        t_inquiry_response response[MAX_INQUIRY_RESPONSES];
	} t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
    packByte(&p_event, JAVACALL_BT_EVENT_INQUIRY_RESULT);
    packByte(&p_event, 1 + sizeof(t_inquiry_response));
    /* num_responses */
    packByte(&p_event, 1);
    /* t_inquiry_response */
    /* bdaddr */
    packBytes(&p_event, (uint8_t*)addr, JAVACALL_BT_ADDRESS_SIZE);
    /* pscan_rep_mode */
    packByte(&p_event, 0);
    /* pscan_period_mode */
    packByte(&p_event, 0);
    /* rsp->pscan_mode */
    packByte(&p_event, 0);
    /* device class */
    packByte(&p_event, (uint8_t)(deviceClass >> 16));
    packByte(&p_event, (uint8_t)(deviceClass >>  8));
    packByte(&p_event, (uint8_t)(deviceClass));
    /* clock offset */
    packByte(&p_event, 0);
    packByte(&p_event, 0);
    packByte(&p_event, 0);
    packByte(&p_event, 0);

    add_hci_event(event);
}

void javanotify_bt_authentication_complete(
        const javacall_bt_address addr,
        javacall_bool success)
{
    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        uint16_t handle;
	} t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
	int handle;
    javacall_bt_stack_get_acl_handle(addr, &handle);
    packByte(&p_event, JAVACALL_BT_EVENT_AUTHENTICATION_COMPLETE);
    packByte(&p_event, sizeof(t_event) - 2);
    /* status */
    packByte(&p_event, (success == JAVACALL_TRUE ? 0x00 : 0xff));
    /* handle */
    packBytes(&p_event, (uint8_t*)&handle, sizeof(uint16_t));

    add_hci_event(event);
}

void javanotify_bt_remote_name_complete(
        const javacall_bt_address addr,
        const char *name)
{
    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        javacall_bt_address bdaddr;
        char name[248];
	} t_event;
    uint8_t event[sizeof(t_event)] ={0};
    uint8_t* p_event = event;
    packByte(&p_event, JAVACALL_BT_EVENT_REMOTE_NAME_COMPLETE);
    packByte(&p_event, (uint8_t)(sizeof(t_event) - 2));
    /* status */
    packByte(&p_event, (name != NULL ? 0x00 : 0xff));
    if (name != NULL) {
        /* bdaddr */
        packBytes(&p_event, (uint8_t*)addr, JAVACALL_BT_ADDRESS_SIZE);
        /* name */
        packString(&p_event, (uint8_t*)name, (MAX_HCI_EVENT_SIZE - JAVACALL_BT_ADDRESS_SIZE - 3));
    }
    add_hci_event(event);
}

void javanotify_bt_encryption_change(
        const javacall_bt_address addr,
        javacall_bool success,
        javacall_bool on)
{
    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        uint16_t handle;
        uint8_t encrypt;
	} t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
	int handle;
    javacall_bt_stack_get_acl_handle(addr, &handle);
    packByte(&p_event, JAVACALL_BT_EVENT_ENCRYPTION_CHANGE);
    packByte(&p_event, sizeof(t_event) - 2);
    /* status */
    packByte(&p_event, (success == JAVACALL_TRUE ? 0x00 : 0xff));
    /* handle */
    packBytes(&p_event, (uint8_t*)&handle, sizeof(uint16_t));
    /* encrypt */
    packByte(&p_event, (on == JAVACALL_TRUE ? 0x00 : 0xff));
    
    add_hci_event(&event);
}

/*
 * Reports to Java stack that service record found on a remote device.
 *
 * @param transactionID ID of transaction finished
 * @param record_handle handle of service record
 */
void javanotify_bt_service_service_discovered(
        javacall_int32 transactionID,
        javacall_handle record_handle) {

    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        javacall_int32  transaction_id;
        javacall_handle record_handle;
    } t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
    packByte(&p_event, JAVACALL_BT_EVENT_SERVICE_DISCOVERED);
    packByte(&p_event, sizeof(t_event) - 2);
    /* transaction_id */
    packBytes(&p_event, (uint8_t*)&transactionID, sizeof(transactionID));
    /* record_handle */
    packBytes(&p_event, (uint8_t*)&record_handle, sizeof(record_handle));
    
    add_hci_event(&event);
}

/*
 * Reports to the application that service search finished.
 *
 * @param transactionID ID of transaction finished
 * @param result indicates result of operation
 */
void javanotify_bt_service_search_completed(
        javacall_int32 transactionID,
        javacall_bt_service_search_result result) {

    typedef struct {
        uint8_t event_type;
        uint8_t param_length;
        javacall_int32 transaction_id;
        javacall_bt_service_search_result result;
    } t_event;
    uint8_t event[sizeof(t_event)] = {0};
    uint8_t* p_event = event;
    packByte(&p_event, JAVACALL_BT_EVENT_SERVICE_SEARCH_COMPLETED);
    packByte(&p_event, sizeof(t_event) - 2);
    /* transaction_id */
    packBytes(&p_event, (uint8_t*)&transactionID, sizeof(transactionID));
    /* result */
    packBytes(&p_event, (uint8_t*)&result, sizeof(result));
    
    add_hci_event(&event);
}

