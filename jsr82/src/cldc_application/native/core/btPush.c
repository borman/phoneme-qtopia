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

#include "btPush.h"

#include <javacall_bt.h>

#include <stdio.h>
#include <string.h>

#include <midp_libc_ext.h>
#include <midpStorage.h>
#include <pcsl_memory.h>
#include <pcsl_string.h>
#include <midp_logging.h>

#define DEFAULT_MTU 672

/* Filename to save Bluetooth push data. */
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(BT_PUSH_FILENAME)
    {'B', 't', 'P', 'u', 's', 'h', '\0'} /* "BtPush" */
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(BT_PUSH_FILENAME);

/* First entry in the linked list representing the internal registry. */
static bt_push_t *g_registry;
static int g_count;

static int get_hex(char c)
{
    if (c >= '0' && c <= '9') {
        return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
        return c - 'a' + 10;
    }
    if (c >= 'A' && c <= 'F') {
        return c - 'A' + 10;
    }
    return -1;
}

static int test_prefix(const char **pstring, const char *prefix)
{
    int len = strlen(prefix);
	if (!midp_strncasecmp(*pstring, prefix, len)) {
		*pstring += len;
        return 1;
    }
    return 0;
}

static int test_address(const char *address, const char **pattern)
{
    /* Need revisit: provide proper pattern matching - currently '*' is only allowed
       as the last symbol (no support for patterns such as '*ABC' or 'ABC*DEF').
    */
    int i;
    for (i = 0; i < 12; i++) {
        char ch = *(*pattern)++;
        if (ch == '*') {
            return 1;
        }
        if (ch != '?' && midp_strncasecmp(address, &ch, 1)) {
            return 0;
        }
        address++;
    }
    return 1;
}

static javacall_bool read_bool(const char **pstring)
{
    if (test_prefix(pstring, "true")) {
        return JAVACALL_TRUE;
    }
    if (test_prefix(pstring, "false")) {
        return JAVACALL_FALSE;
    }
    /* unexpected boolean value */
    REPORT_ERROR(LC_PUSH, "Invalid boolean value.");
    return JAVACALL_FALSE;
}

static unsigned short read_short(const char **pstring)
{
    unsigned short retval = 0;
    while (**pstring >= '0' && **pstring <= '9') {
        retval *= 10;
        retval += *(*pstring++) - '0';
    }
    return retval;
}

static bt_push_t *find_push(const bt_port_t *port, bt_push_t **prev)
{
    bt_push_t *push = g_registry;
    if (prev != NULL) {
        *prev = NULL;
    }
    while (push != NULL) {
        if (push->port.protocol == port->protocol &&
                !memcmp(push->port.uuid, port->uuid, sizeof(bt_uuid_t))) {
            return push;
        }
        if (prev != NULL) {
            *prev = push;
        }
        push = push->next;
    }
    return NULL;
}

static javacall_result close_handle(bt_protocol_t protocol, javacall_handle handle)
{
    javacall_result result = JAVACALL_FAIL;
    if (handle == BT_INVALID_HANDLE) {
        return JAVACALL_OK;
    }
    switch (protocol) {
        case BT_L2CAP:
            result = javacall_bt_l2cap_close(handle);
            break;
        case BT_SPP:
        case BT_GOEP:
            result = javacall_bt_rfcomm_close(handle);
            break;
        default:
            break;
    }
    return result;
}

static void close_all(bt_push_t *push)
{
    bt_client_t *client = push->client;
    close_handle(push->port.protocol, push->server);
    while (client != NULL) {
        bt_client_t *next = client->next;
        close_handle(push->port.protocol, client->handle);
        pcsl_mem_free(client);
        client = next;
    }
    push->client = NULL;
}

static void push_save()
{
    char *error;
    bt_push_t *push = g_registry;
    pcsl_string full_name = PCSL_STRING_NULL;
    int storage;
    pcsl_string_cat(storage_get_root(INTERNAL_STORAGE_ID), &BT_PUSH_FILENAME,
            &full_name);
    storage = storage_open(&error, &full_name, OPEN_READ_WRITE_TRUNCATE);
    pcsl_string_free(&full_name);
    if (error != NULL) {
        REPORT_ERROR1(LC_PUSH, "Error opening `BtPush' file: %s", error);
        storageFreeError(error);
        return;
    }
    storageWrite(&error, storage, (char *)&g_count, sizeof(g_count));
    while (push != NULL && error == NULL) {
        bt_push_t *next = push->next;
        storageWrite(&error, storage, (char *)&push->port, sizeof(bt_port_t));
        if (error != NULL) {
            break;
        }
        storageWrite(&error, storage, (char *)&push->params,
                sizeof(bt_params_t));
        if (error != NULL) {
            break;
        }
        storageWrite(&error, storage, (char *)&push->record.classes,
            sizeof(push->record.classes));
        if (error != NULL) {
            break;
        }
        storageWrite(&error, storage, (char *)&push->record.size,
            sizeof(push->record.size));
        if (error != NULL) {
            break;
        }
        storageWrite(&error, storage, (char *)push->record.data,
                push->record.size);
        if (error != NULL) {
            break;
        }
        push = next;
    }
    if (error != NULL) {
        REPORT_ERROR1(LC_PUSH, "Error writing `BtPush' file: %s", error);
        storageFreeError(error);
    }
    storageClose(&error, storage);
    storageFreeError(error);
}

javacall_result bt_push_startup()
{
    int i;
    char *error;
    pcsl_string full_name = PCSL_STRING_NULL;
    int storage;
    REPORT_INFO(LC_PUSH, "Bluetooth PushRegistry is now starting.");
    javacall_bt_sddb_initialize();
    pcsl_string_cat(storage_get_root(INTERNAL_STORAGE_ID), &BT_PUSH_FILENAME,
            &full_name);
    if (!storage_file_exists(&full_name)) {
        pcsl_string_free(&full_name);
        return JAVACALL_OK;
    }
    storage = storage_open(&error, &full_name, OPEN_READ);
    pcsl_string_free(&full_name);
    if (error != NULL) {
        REPORT_ERROR1(LC_PUSH, "Failed to open `BtPush' file: %s", error);
        storageFreeError(error);
        return JAVACALL_FAIL;
    }
    storageRead(&error, storage, (char *)&g_count, sizeof(g_count));
    for (i = 0; error == NULL && i < g_count; i++) {
        bt_push_t *push = (bt_push_t *)pcsl_mem_malloc(sizeof(bt_push_t));
        if (push == NULL) {
            REPORT_ERROR(LC_PUSH, "Failed to allocate memory.");
            storageClose(&error, storage);
            storageFreeError(error);
            return JAVACALL_FAIL;
        }
        storageRead(&error, storage, (char *)&push->port, sizeof(push->port));
        if (error != NULL) {
            pcsl_mem_free(push);
            break;
        }
        storageRead(&error, storage, (char *)&push->params,
                sizeof(push->params));
        if (error != NULL) {
            pcsl_mem_free(push);
            break;
        }
        push->record.id = BT_INVALID_SDDB_HANDLE;
        storageRead(&error, storage, (char *)&push->record.classes,
                sizeof(push->record.classes));
        if (error != NULL) {
            pcsl_mem_free(push);
            break;
        }
        storageRead(&error, storage, (char *)&push->record.size,
                sizeof(push->record.size));
        if (error != NULL) {
            pcsl_mem_free(push);
            break;
        }
        push->record.data = pcsl_mem_malloc(push->record.size);
        if (push->record.data == NULL) {
            pcsl_mem_free(push);
            REPORT_ERROR(LC_PUSH, "Failed to allocate memory.");
            storageClose(&error, storage);
            storageFreeError(error);
            return JAVACALL_FAIL;
        }
        storageRead(&error, storage, (char *)push->record.data,
                push->record.size);
        if (error != NULL) {
            pcsl_mem_free(push->record.data);
            pcsl_mem_free(push);
            break;
        }
        push->server = BT_INVALID_HANDLE;
        push->client = NULL;
        push->next = g_registry;
        g_registry = push;
    }
    if (error != NULL) {
        REPORT_ERROR1(LC_PUSH, "Error reading `BtPush' file: %s", error);
        storageFreeError(error);
        storageClose(&error, storage);
        storageFreeError(error);
        return JAVACALL_FAIL;
    }
    REPORT_INFO1(LC_PUSH, "%d record(s) read.", g_count);
    storageClose(&error, storage);
    storageFreeError(error);
    if (g_count > 0) {
        /* Attempt to enable Bluetooth radio, if it is not already on. */
        javacall_bool enabled;
        javacall_bt_stack_initialize();
        if (javacall_bt_stack_is_enabled(&enabled) == JAVACALL_OK &&
                enabled == JAVACALL_FALSE) {
            javacall_bt_stack_enable();
        }
    }
    return JAVACALL_OK;
}

void bt_push_shutdown()
{
    bt_push_t *push = g_registry;
    REPORT_INFO(LC_PUSH, "Shutting down Bluetooth PushRegistry.");
    while (push != NULL) {
        bt_push_t *next = push->next;
        javacall_bt_sddb_remove_record(push->record.id);
        pcsl_mem_free(push->record.data);
        close_all(push);
        pcsl_mem_free(push);
        push = next;
    }
    g_registry = NULL;
    javacall_bt_sddb_finalize();
}

javacall_bool bt_is_bluetooth_url(const char *url)
{
    if (test_prefix(&url, "btl2cap://")) {
        return JAVACALL_TRUE;
    }
    if (test_prefix(&url, "btspp://")) {
        return JAVACALL_TRUE;
    }
    if (test_prefix(&url, "btgoep://")) {
        return JAVACALL_TRUE;
    }
    return JAVACALL_FALSE;
}

javacall_result bt_push_parse_url(const char *url, bt_port_t *port,
        bt_params_t *params)
{
    int i;
    if (test_prefix(&url, "btl2cap://")) {
        port->protocol = BT_L2CAP;
    } else if (test_prefix(&url, "btspp://")) {
        port->protocol = BT_SPP;
    } else if (test_prefix(&url, "btgoep://")) {
        port->protocol = BT_GOEP;
    } else {
        return JAVACALL_FAIL;
    }
    if (!test_prefix(&url, "localhost:")) {
        return JAVACALL_FAIL;
    }
    for (i = 0; i < 16; i++) {
        int hex1, hex2;
        hex1 = get_hex(*url++);
        if (hex1 < 0) {
            return JAVACALL_FAIL;
        }
        hex2 = get_hex(*url++);
        if (hex2 < 0) {
            return JAVACALL_FAIL;
        }
        port->uuid[i] = hex1 << 4 | hex2;
    }
    if (params == NULL) {
        /* params parsing is not needed */
        return JAVACALL_OK;
    }
    params->authenticate = JAVACALL_FALSE;
    params->authorize = JAVACALL_FALSE;
    params->encrypt = JAVACALL_FALSE;
    params->rmtu = DEFAULT_MTU;
    params->tmtu = -1;
    url = strchr(url, ';');
    while (url != NULL) {
        if (test_prefix(&url, ";authenticate=")) {
            params->authenticate = read_bool(&url);
        } else if (test_prefix(&url, ";authorize=")) {
            params->authorize = read_bool(&url);
        } else if (test_prefix(&url, ";encrypt=")) {
            params->encrypt = read_bool(&url);
        } else if (test_prefix(&url, ";master=")) {
            params->master = read_bool(&url);
        } else if (test_prefix(&url, ";receiveMTU=")) {
            params->rmtu = read_short(&url);
        } else if (test_prefix(&url, ";transmitMTU=")) {
            params->tmtu = read_short(&url);
        }
        url = strchr(url + 1, ';');
    }
    if (params->authorize || params->encrypt) {
        params->authenticate = JAVACALL_TRUE;
    }
    return JAVACALL_OK;
}

javacall_result bt_push_register_url(const char *url, const void *data,
        size_t size)
{
    bt_port_t port;
    bt_params_t params;
    bt_push_t *push;
    REPORT_INFO(LC_PUSH, "Bluetooth PushRegistry URL registration:");
    REPORT_INFO1(LC_PUSH, "%s", url);
    bt_push_parse_url(url, &port, &params);
    push = find_push(&port, NULL);
    if (push != NULL) {
        /* found existing entry with the same protocol/uuid, can not proceed */
        REPORT_ERROR(LC_PUSH, "Entry already exists, registration failed.");
        return JAVACALL_FAIL;
    }
    /* save the entry in the registry */
    push = (bt_push_t *)pcsl_mem_malloc(sizeof(bt_push_t));
    if (push == NULL) {
        REPORT_ERROR(LC_PUSH, "Failed to allocate memory.");
        return JAVACALL_FAIL;
    }
    memcpy(&push->port, &port, sizeof(bt_port_t));
    memcpy(&push->params, &params, sizeof(bt_params_t));
    push->record.id = BT_INVALID_SDDB_HANDLE;
    push->record.classes = 0;
    if (data != NULL) {
        push->record.data = pcsl_mem_malloc(size);
        if (push->record.data == NULL) {
            pcsl_mem_free(push);
            return JAVACALL_FAIL;
        }
        memcpy(push->record.data, data, size);
    } else {
        push->record.data = NULL;
    }
    push->record.size = size;
    push->server = BT_INVALID_HANDLE;
    push->client = NULL;
    push->next = g_registry;
    g_registry = push;
    g_count++;
    push_save();
    REPORT_INFO(LC_PUSH, "Registration successful.");
    return JAVACALL_OK;
}

javacall_result bt_push_unregister_url(const char *url)
{
    bt_port_t port;
    bt_push_t *push, *prev;
    REPORT_INFO(LC_PUSH, "Bluetooth PushRegistry URL un-registration:");
    REPORT_INFO1(LC_PUSH, "%s", url);
    bt_push_parse_url(url, &port, NULL);
    push = find_push(&port, &prev);
    if (push == NULL) {
        return JAVACALL_FAIL;
    }
    /* remove the service record */
    javacall_bt_sddb_remove_record(push->record.id);
    /* close server and client connections */
    close_all(push);
    /* remove the entry */
    if (prev != NULL) {
        prev->next = push->next;
    } else {
        g_registry = push->next;
    }
    g_count--;
    pcsl_mem_free(push);
    push_save();
    javacall_bt_stack_set_service_classes(javacall_bt_sddb_get_service_classes(0));
    REPORT_INFO(LC_PUSH, "Un-registration successful.");
    return JAVACALL_OK;
}

javacall_bool bt_push_test_filter(const javacall_bt_address bdaddr, const char *filter)
{
    char address[13] = {0};
    javacall_bool auth;

    sprintf(address, "%02X%02X%02X%02X%02X%02X", bdaddr[5], bdaddr[4],
	   bdaddr[3], bdaddr[2], bdaddr[1], bdaddr[0]);

    if (!test_address(address, &filter)) {
        return JAVACALL_FALSE;
    }
    if (javacall_bt_bcc_is_trusted(bdaddr, &auth) != JAVACALL_OK) {
        return JAVACALL_FALSE;
    }
    if (auth == JAVACALL_TRUE) {
        if (!test_prefix(&filter, ";authorized")) {
            return JAVACALL_FALSE;
        }
    } else {
        if (javacall_bt_bcc_is_authenticated(bdaddr, &auth) != JAVACALL_OK) {
            return JAVACALL_FALSE;
        }
        if (auth == JAVACALL_TRUE) {
            if (!test_prefix(&filter, ";authenticated")) {
                return JAVACALL_FALSE;
            }
        }
    }
    if (test_prefix(&filter, ";blacklist=")) {
        while (*filter != '\0') {
            if (test_address(address, &filter)) {
                return JAVACALL_FALSE;
            }
            if (*filter == ';') {
                filter++;
            } else if (*filter != '\0') {
                return JAVACALL_FALSE;
            }
        }
    }
    return JAVACALL_TRUE;
}

bt_pushid_t bt_push_find_server(javacall_handle server)
{
    bt_push_t *push = g_registry;
    while (push != NULL) {
        if (push->server == server) {
            return (bt_pushid_t)push;
        }
        push = push->next;
    }
    return BT_INVALID_PUSH_HANDLE;
}

bt_pushid_t bt_push_find_client(javacall_handle handle)
{
    bt_push_t *push = g_registry;
    while (push != NULL) {
        bt_client_t *client = push->client;
        while (client != NULL) {
            if (client->handle == handle) {
                return (bt_pushid_t)push;
            }
            client = client->next;
        }
        push = push->next;
    }
    return BT_INVALID_PUSH_HANDLE;
}

javacall_bool bt_push_accept(bt_pushid_t pushid, const char *filter,
        javacall_handle *handle)
{
    bt_push_t *push = (bt_push_t *)pushid;
    bt_client_t *client;
    if (push == NULL) {
        return JAVACALL_FALSE;
    }
    client = (bt_client_t *)pcsl_mem_malloc(sizeof(bt_client_t));
    switch (push->port.protocol) {
        case BT_L2CAP:
            if (javacall_bt_l2cap_accept(push->server,
                    &client->handle, &client->bdaddr,
                    &client->rmtu, &client->tmtu) != JAVACALL_OK) {
                pcsl_mem_free(client);
                return JAVACALL_FALSE;
            }
            break;
        case BT_SPP:
        case BT_GOEP:
            if (javacall_bt_rfcomm_accept(push->server,
                    &client->handle, &client->bdaddr) != JAVACALL_OK) {
                pcsl_mem_free(client);
                return JAVACALL_FALSE;
            }
            break;
        default:
            pcsl_mem_free(client);
            return JAVACALL_FALSE;
    }
    if (bt_push_test_filter(client->bdaddr, filter) == JAVACALL_TRUE) {
        bt_client_t **client_ptr = &push->client;
        while (*client_ptr != NULL) {
            client_ptr = &(*client_ptr)->next;
        }
        *client_ptr = client;
        client->next = NULL;
        *handle = client->handle;
        return JAVACALL_TRUE;
    }
    close_handle(push->port.protocol, client->handle);
    pcsl_mem_free(client);
    return JAVACALL_FALSE;
}

bt_pushid_t bt_push_checkout_server(const bt_port_t *port, javacall_handle *server,
        bt_sddbid_t *sddbid)
{
    bt_push_t *push = find_push(port, NULL);
    if (push == NULL || push->server == BT_INVALID_HANDLE) {
        return BT_INVALID_PUSH_HANDLE;
    }
    *server = push->server;
    if (sddbid != NULL) {
        *sddbid = push->record.id;
    }
    push->server = BT_INVALID_HANDLE;
    return (bt_pushid_t)push;
}

javacall_result bt_push_checkout_client(bt_pushid_t pushid, javacall_handle *handle,
        javacall_bt_address bdaddr, int *rmtu, int *tmtu)
{
    bt_push_t *push = (bt_push_t *)pushid;
    bt_client_t *client;
    if (push == NULL || push->client == NULL) {
        return JAVACALL_FAIL;
    }
    client = push->client;
    *handle = client->handle;
    memcpy(bdaddr, client->bdaddr, sizeof(javacall_bt_address));
    if (rmtu != NULL) {
        *rmtu = client->rmtu;
    }
    if (tmtu != NULL) {
        *tmtu = client->tmtu;
    }
    push->client = client->next;
    pcsl_mem_free(client);
    return JAVACALL_OK;
}

javacall_handle bt_push_start_server(const bt_port_t *port)
{
    int psm, cn;
    bt_params_t *params;
    bt_push_t *push = find_push(port, NULL);
    if (push == NULL || push->server != BT_INVALID_HANDLE) {
        return BT_INVALID_HANDLE;
    }
    if (javacall_bt_sddb_update_record(&push->record.id, push->record.classes,
            push->record.data, push->record.size) != JAVACALL_OK) {
        return BT_INVALID_HANDLE;
    }
    params = &push->params;
    switch (port->protocol) {
        case BT_L2CAP:
            if (javacall_bt_l2cap_create_server(params->rmtu, params->tmtu,
                    params->authenticate, params->authorize, params->encrypt,
                    params->master, &push->server, &psm) !=
                    JAVACALL_OK) {
                return BT_INVALID_HANDLE;
            }
            javacall_bt_sddb_update_psm(push->record.id, psm);
            javacall_bt_l2cap_listen(push->server);
            break;
        case BT_SPP:
        case BT_GOEP:
            if (javacall_bt_rfcomm_create_server(params->authenticate, params->authorize,
                    params->encrypt, params->master, &push->server, &cn) !=
                    JAVACALL_OK) {
                return BT_INVALID_HANDLE;
            }
            javacall_bt_sddb_update_channel(push->record.id, cn);
            javacall_bt_rfcomm_listen(push->server);
            break;
        default:
            return BT_INVALID_HANDLE;
    }
    javacall_bt_stack_set_service_classes(javacall_bt_sddb_get_service_classes(0));
    return push->server;
}

javacall_result bt_push_reject(bt_pushid_t pushid)
{
    bt_push_t *push = (bt_push_t *)pushid;
    bt_client_t *client;
    if (push == NULL) {
        return JAVACALL_FAIL;
    }
    client = push->client;
    while (client != NULL) {
        bt_client_t *next = client->next;
        close_handle(push->port.protocol, client->handle);
        pcsl_mem_free(client);
        client = next;
    }
    push->client = NULL;
    return JAVACALL_OK;
}

bt_sddbid_t bt_push_get_record(const bt_port_t *port)
{
    bt_push_t *push = find_push(port, NULL);
    return push != NULL ? push->record.id : BT_INVALID_SDDB_HANDLE;
}

javacall_result bt_push_update_record(bt_sddbid_t sddbid,
        const bt_record_t *record)
{
    bt_push_t *push = g_registry;
    while (push != NULL) {
        bt_record_t *rec = &push->record;
        if (rec->id == sddbid) {
            rec->id = record->id;
            rec->classes = record->classes;
            if (rec->size != record->size) {
                void *data = pcsl_mem_realloc(rec->data, record->size);
                if (data == NULL) {
                    return JAVACALL_FAIL;
                }
                rec->data = data;
                rec->size = record->size;
            }
            memcpy(rec->data, record->data, record->size);
            push_save();
            return JAVACALL_OK;
        }
        push = push->next;
    }
    return JAVACALL_FAIL;
}
