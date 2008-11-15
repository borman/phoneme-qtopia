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

#ifndef __BT_PUSH_H__
#define __BT_PUSH_H__

#include "btCommon.h"
#include "ProxyAPI.h"

#ifdef __cplusplus
extern "C" {
#endif

/* Type name for storing handles to internal push records. */
typedef int bt_pushid_t;

/* Type for storing connection parameters. */
typedef struct bt_params_t bt_params_t;
struct bt_params_t {
    javacall_bool authenticate; /* ';authenticate=' parameter. */
    javacall_bool authorize; /* ';authorize=' parameter. */
    javacall_bool encrypt; /* ';encrypt=' parameter. */
    javacall_bool master; /* ';master=' parameter. */
    int rmtu; /*< Receive MTU. */
    int tmtu; /*< Transmit MTU. */
};

/* Type for storing accepted client connections. */
typedef struct bt_client_t bt_client_t;
struct bt_client_t {
    javacall_handle handle; /*< Client connection handle. */
    javacall_bt_address bdaddr; /*< Client Bluetooth address. */
    int rmtu; /*< Receive MTU. */
    int tmtu; /*< Transmit MTU. */
    bt_client_t *next; /*< Next accepted client connection. */
};

/* Internal push registration entry type. */
typedef struct bt_push_t bt_push_t;
struct bt_push_t {
    bt_port_t port; /*< Protocol type and service uuid. */
    bt_params_t params; /*< Server parameters. */
    bt_record_t record; /*< Service record. */
    javacall_handle server; /*< Server connection handle. */
    bt_client_t *client; /*< Accepted client connections. */
    bt_push_t *prev, *next; /*< Previous & next record in the linked list. */
};

#define BT_INVALID_PUSH_HANDLE 0

/*
 * Starts up Bluetooth push support, and allocates all required resources.
 *
 * @return JAVACALL_OK if successful, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_startup();

/*
 * Shuts down Bluetooth push support, and releases all related resources.
 */
void bt_push_shutdown();

/*
 * Checks if the supplied URL is Bluetooth URL.
 *
 * @param url url to ckeck
 * @return JAVACALL_TRUE if the parameter is Bluetooth URL,
 *         JAVACALL_FALSE otherwise
 */
javacall_bool bt_is_bluetooth_url(const char *url);

/*
 * Parses URL and fills in structures supplied.
 *
 * @param url Bluetooth url to parse
 * @param port [out] points to bt_port_t structure to be filled in
 * @param params [out] points to bt_params_t structure to be filled in
 * @return JAVACALL_OK on success, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_parse_url(const char *url, bt_port_t *port,
        bt_params_t *params);

/*
 * Registers a new URL in the push subsystem.
 * This function works with both static and dynamic registrations.
 *
 * @param url URL to register
 * @param data serialized service record
 * @param size size of the data in bytes
 * @return JAVACALL_OK if successful, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_register_url(const char *url, const void *data,
        size_t size);

/*
 * Removes registration of an URL, and removes the corresponding SDDB service
 * record.
 *
 * @param url URL to unregister
 * @return JAVACALL_OK if successful, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_unregister_url(const char *url);

/*
 * Tests Bluetooth address against the filter specified.
 *
 * @param bdaddr Bluetooth address to test
 * @param filter filter to be used
 * @return JAVACALL_TRUE if the comparison succeeds, JAVACALL_FALSE otherwise
 */
javacall_bool bt_push_test_filter(const javacall_bt_address bdaddr, const char *filter);

/*
 * Searches the internal push registry for the specified server connection.
 *
 * @param server server connection handle
 * @return internal push handle, or invalid handle if the server is not running
 */
bt_pushid_t bt_push_find_server(javacall_handle server);

/*
 * Searches the internal push registry for the specified client connection.
 *
 * @param handle client connection handle
 * @return internal push handle, or invalid handle if the client is not found
 */
bt_pushid_t bt_push_find_client(javacall_handle handle);

/*
 * Attempts to accept an incoming client connection.
 *
 * @param pushid handle to the internal push entry
 * @param filter filter against which the client connection will be tested
 * @param handle variable to receive client connection handle
 * @return JAVACALL_TRUE if the connection was accepted, JAVACALL_FALSE otherwise
 */
javacall_bool bt_push_accept(bt_pushid_t pushid, const char *filter,
        javacall_handle *handle);

/*
 * Checks out (takes ownership of) an active server connection from push.
 *
 * @param port protocol and service uuid of the server connection
 * @param server [out] handle of active server connection associated with the
 *         port, or BT_INVALID_HANDLE if the server is not running
 * @param record [out] service record associated with the port
 * @return handle to the internal push entry, or BT_INVALID_PUSH_HANDLE if
 *         the server is not registered in push
 */
bt_pushid_t bt_push_checkout_server(const bt_port_t *port, javacall_handle *server,
        bt_sddbid_t *sddbid);

/*
 * Checks out (takes ownership of) an already-accepted client connection from
 * push subsystem.
 *
 * @param pushid handle to the internal push entry
 * @param handle variable to receive client connection handle
 * @param bdaddr variable to receive client Bluetooth address
 * @param rmtu variable to receive receive MTU of the accepted connection
 * @param tmtu variable to receive transmit MTU of the accepted connection
 * @return JAVACALL_OK on success, JAVACALL_FAIL on failure or
 *         if there are no client connections accepted
 */
javacall_result bt_push_checkout_client(bt_pushid_t pushid, javacall_handle *handle,
        javacall_bt_address bdaddr, int *rmtu, int *tmtu);

/*
 * Instructs push subsystem to enable the corresponding service record and
 * start listening for incoming connections on the given port.
 *
 * @param port port to start listening on
 * @return server connection handle on success, or invalid handle value on error
 */
javacall_handle bt_push_start_server(const bt_port_t *port);

/*
 * Closes all accepted client connections.
 *
 * @param pushid handle to the internal push entry
 * @return JAVACALL_OK on success, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_reject(bt_pushid_t pushid);

/*
 * Retrieves service record handle for the given port.
 *
 * @param port port to retrieve service record handle for
 * @return service record handle associated with the given port,
 *         or invalid record handle if the operation fails
 */
bt_sddbid_t bt_push_get_record(const bt_port_t *port);

/*
 * Updates service record maintained by the push subsystem.
 *
 * @param sddbid old value of service record handle
 * @param record service record to be used for the service
 * @return JAVACALL_OK if successful, JAVACALL_FAIL otherwise
 */
javacall_result bt_push_update_record(bt_sddbid_t sddbid,
        const bt_record_t *record);

#ifdef __cplusplus
}
#endif

#endif
