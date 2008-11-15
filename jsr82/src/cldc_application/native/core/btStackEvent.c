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

#include "javanotify_bt.h"
#include "btCommon.h"
#include <string.h>
/*
 * IMPL NOTE: Because VC compiler has only one way to specify 
 * the packing alignment in the source code via pack pragma,
 * the __PPACKED__ shall be defined as 1. In the appropriated
 * place this value shall be checked and the pack pragma shall 
 * be inserted. The __PPACKED__ shall be redefined to the empty 
 * string.
 * NEED REVISIT: 
 * it is necessary to avoid using of the packed structures.
 */
#ifdef _MSC_VER
#if __PPACKED__ == 1
#pragma pack(1)
#endif
#undef __PPACKED__
#define __PPACKED__
#endif

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

void javanotify_bt_inquiry_complete(javacall_bool success)
{
    struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
	}  __PPACKED__ event;
	event.event_type = JAVACALL_BT_EVENT_INQUIRY_COMPLETE;
    event.param_length = sizeof(event) - 2;
    event.status = success == JAVACALL_TRUE ? 0x00 : 0xff;
    add_hci_event(&event);
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
	} __PPACKED__ inquiry_response;
	struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t num_responses;
        inquiry_response response[MAX_INQUIRY_RESPONSES];
	} __PPACKED__ event;
    inquiry_response *rsp = &event.response[0];
    event.event_type = JAVACALL_BT_EVENT_INQUIRY_RESULT;
    event.param_length = 1 + sizeof(inquiry_response);
    event.num_responses = 1;
    memcpy(rsp->bdaddr, addr, JAVACALL_BT_ADDRESS_SIZE);
    rsp->pscan_rep_mode = 0;
    rsp->pscan_period_mode = 0;
    rsp->pscan_mode = 0;
    rsp->dev_class[0] = (uint8_t)(deviceClass >> 16);
    rsp->dev_class[1] = (uint8_t)(deviceClass >> 8);
    rsp->dev_class[2] = (uint8_t)(deviceClass);
    rsp->clock_offset = 0;
    add_hci_event(&event);
}

void javanotify_bt_authentication_complete(
        const javacall_bt_address addr,
        javacall_bool success)
{
    struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        uint16_t handle;
	} __PPACKED__ event;
	int handle;
    javacall_bt_stack_get_acl_handle(addr, &handle);
	event.event_type = JAVACALL_BT_EVENT_AUTHENTICATION_COMPLETE;
    event.param_length = sizeof(event) - 2;
    event.status = success == JAVACALL_TRUE ? 0x00 : 0xff;
    event.handle = handle;
    add_hci_event(&event);
}

void javanotify_bt_remote_name_complete(
        const javacall_bt_address addr,
        const char *name)
{
    struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        javacall_bt_address bdaddr;
        char name[248];
	} __PPACKED__ event;
    event.event_type = JAVACALL_BT_EVENT_REMOTE_NAME_COMPLETE;
    event.param_length = sizeof(event) - 2;
    event.status = name != NULL ? 0x00 : 0xff;
    if (name != NULL) {
        memcpy(event.bdaddr, addr, JAVACALL_BT_ADDRESS_SIZE);
        strncpy(event.name, name, sizeof(event.name));
    }
    add_hci_event(&event);
}

void javanotify_bt_encryption_change(
        const javacall_bt_address addr,
        javacall_bool success,
        javacall_bool on)
{
    struct {
        uint8_t event_type;
        uint8_t param_length;
        uint8_t status;
        uint16_t handle;
        uint8_t encrypt;
	} __PPACKED__ event;
	int handle;
    javacall_bt_stack_get_acl_handle(addr, &handle);
	event.event_type = JAVACALL_BT_EVENT_ENCRYPTION_CHANGE;
    event.param_length = sizeof(event) - 2;
    event.status = success == JAVACALL_TRUE ? 0x00 : 0xff;
    event.handle = handle;
    event.encrypt = on == JAVACALL_TRUE ? 0x01 : 0x00;
    add_hci_event(&event);
}
//#define JAVACALL_BT_EVENT_SERVICE_DISCOVERED        6;
//#define JAVACALL_BT_EVENT_SERVICE_SEARCH_COMPLETED  7;

/*
 * Reports to Java stack that service record found on a remote device.
 *
 * @param transactionID ID of transaction finished
 * @param record_handle handle of service record
 */
void javanotify_bt_service_service_discovered(
        javacall_int32 transactionID,
        javacall_handle record_handle) {

    struct {
        uint8_t event_type;
        uint8_t param_length;
        javacall_bt_service_discovered data;
    } __PPACKED__ event;
    

    /* JSRno and event minor id. */
    event.event_type = JAVACALL_BT_EVENT_SERVICE_DISCOVERED;
    event.data.transaction_id = transactionID;
    event.data.record_handle = record_handle;

    event.param_length = sizeof(event) - 2;
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

    struct {
        uint8_t event_type;
        uint8_t param_length;
        javacall_bt_service_search_completed data;
    } __PPACKED__ event;
    
    event.event_type = JAVACALL_BT_EVENT_SERVICE_SEARCH_COMPLETED;
    event.data.transaction_id = transactionID;
    event.data.result = result;

    event.param_length = sizeof(event) - 2;
    add_hci_event(&event);
}

