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

#ifndef __JAVANOTIFY_BT_H_
#define __JAVANOTIFY_BT_H_

#include <javacall_bt.h>

/**
 * @file javanotify_bt.h
 * @ingroup JSR82
 * @brief Notification functions for JSR-82 Bluetooth
 */

#ifdef __cplusplus
extern "C" {
#endif


/******************************************************************************
 ******************************************************************************
 ******************************************************************************

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -  
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurence of the
  underlying event.
  The functions need to be executed in platform's task/thread

 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/

/**
 * @defgroup MiscNotification Notification API for Bluetooth
 * @ingroup Bluetooth
 * @{
 */
#define JAVACALL_JSR_82_ID  82

/**
 * @enum javacall_bt_event_minor_id
 * @brief Minor ids for bluetooth events
 */
typedef enum {
    JAVACALL_BT_EVENT_DUMMY                     = 0,
    JAVACALL_BT_EVENT_INQUIRY_COMPLETE          = 1,
    JAVACALL_BT_EVENT_INQUIRY_RESULT            = 2,
    JAVACALL_BT_EVENT_AUTHENTICATION_COMPLETE   = 3,
    JAVACALL_BT_EVENT_REMOTE_NAME_COMPLETE      = 4,
    JAVACALL_BT_EVENT_ENCRYPTION_CHANGE         = 5,
    JAVACALL_BT_EVENT_SERVICE_DISCOVERED        = 6,
    JAVACALL_BT_EVENT_SERVICE_SEARCH_COMPLETED  = 7
} javacall_bt_event_minor_id;

typedef struct {
    javacall_bt_address address;
    javacall_int32      device_class;
} javacall_bt_inquiry_result;

typedef struct {
    javacall_bt_address address;
    char name[JAVACALL_BT_MAX_USER_FRIENDLY_NAME];
} javacall_bt_remote_name_result;

/**
 * @enum javacall_bt_service_search_result
 * !NOTE! enum values were got from the spec, DO NOT CHANGE
 * @brief Result codes
 */
typedef enum {
    JAVACALL_BT_SERVICE_SEARCH_OK                    = 1,
    JAVACALL_BT_SERVICE_SEARCH_TERMINATED            = 2,
    JAVACALL_BT_SERVICE_SEARCH_ERROR                 = 3,
    JAVACALL_BT_SERVICE_SEARCH_NO_RECORDS            = 4,
    JAVACALL_BT_SERVICE_SEARCH_DEVICE_NOT_REACHABLE  = 6,
} javacall_bt_service_search_result;

typedef struct {
    javacall_int32  transaction_id;
    javacall_handle record_handle;
} javacall_bt_service_discovered;

typedef struct {
    javacall_int32 transaction_id;
    javacall_bt_service_search_result result;
} javacall_bt_service_search_completed;

typedef struct {
    javacall_int32 event_id;
    javacall_int32 event_minor_id;
    union {
        javacall_bool                        result;
        javacall_bt_inquiry_result           inquiry_result;
        javacall_bt_remote_name_result       name_result;
        javacall_bt_service_discovered       service_discovered;
        javacall_bt_service_search_completed service_completed;
    } data;
} javacall_bt_event;

/**
 * @enum javacall_bt_callback_type
 * @brief Event names
 */
typedef enum {
    JAVACALL_EVENT_BT_CONNECT_COMPLETE = 1000,
    JAVACALL_EVENT_BT_SEND_COMPLETE,
    JAVACALL_EVENT_BT_RECEIVE_COMPLETE,
    JAVACALL_EVENT_BT_ACCEPT_COMPLETE,
} javacall_bt_callback_type;

/**
 * A callback function to be called for notification of user confirmation
 * to enable bluetooth radio.
 *
 * @param answer confirmation from user
 *      - JAVACALL_TRUE if the user has allowed to enable Bluetooth,
 *      - JAVACALL_FALSE otherwise
 * @param operation_result operation result: Either
 *      - JAVACALL_OK if operation completed successfully,
 *      - otherwise, JAVACALL_FAIL
 */
/*OPTIONAL*/ void javanotify_bt_confirm_enable(
        javacall_bool answer,
        javacall_result operation_result);

/**
 * A callback function to be called for notification of non-blocking
 * protocol related events.
 * The platform will invoke the call back in platform context for
 * connection related occurrence.
 *
 * @param type type of indication: Either
 *     - JAVACALL_EVENT_BT_CONNECT_COMPLETE
 *     - JAVACALL_EVENT_BT_SEND_COMPLETE
 *     - JAVACALL_EVENT_BT_RECEIVE_COMPLETE
 *     - JAVACALL_EVENT_BT_ACCEPT_COMPLETE
 *
 * @param handle related to the notification
 * @param operation_result operation result: Either
 *      - JAVACALL_OK if operation completed successfully,
 *      - otherwise, JAVACALL_FAIL
 */
void javanotify_bt_protocol_event(
        javacall_bt_callback_type event,
        javacall_handle handle,
        javacall_result operation_result);

/**
 * Reports to the application that remote devices have been discovered.
 *
 * @param addr Bluetooth address of the discovered device
 * @param deviceClass class of the discovered device
 */
void javanotify_bt_device_discovered(
        const javacall_bt_address addr,
        int deviceClass);

/**
 * Reports to the application that the inquiry has been completed.
 *
 * @param success indicates whether the inquiry operation succeeded
 */
void javanotify_bt_inquiry_complete(javacall_bool success);

/**
 * Reports to the application that authentication request has been completed.
 *
 * @param addr Bluetooth address of the remote device
 * @param success indicates whether the authentication succeeded
 */
void javanotify_bt_authentication_complete(
        const javacall_bt_address addr,
        javacall_bool success);

/**
 * Reports to the application that the remote device name has been retrieved.
 *
 * @param addr Bluetooth address of the remote device
 * @param name user-friendly name of the remote device
 */
void javanotify_bt_remote_name_complete(
        const javacall_bt_address addr,
        const char *name);

/**
 * Reports to the application that link encryption has been changed.
 *
 * @param address Bluetooth address of the remote device
 * @param success indicates whether the change succeeded
 * @param on indicates whether link encryption is enabled
 */
void javanotify_bt_encryption_change(
        const javacall_bt_address addr,
        javacall_bool success,
        javacall_bool on);

/**
 * Reports to Java stack that service record found on a remote device.
 *
 * @param transactionID ID of transaction finished
 * @param record_handle handle of service record
 */
void javanotify_bt_service_service_discovered(
        javacall_int32 transactionID,
        javacall_handle record_handle);

/**
 * Reports to Java stack that service search finished.
 *
 * @param transactionID ID of transaction finished
 * @param result indicates result of operation
 */
void javanotify_bt_service_search_completed(
        javacall_int32 transactionID,
        javacall_bt_service_search_result result);

/** @} */

#ifdef __cplusplus
}
#endif

#endif 


