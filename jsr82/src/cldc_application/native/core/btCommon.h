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

#ifndef __BT_COMMON_H__
#define __BT_COMMON_H__

/*
 * @file
 * @defgroup jsr82common Common Bluetooth declarations
 * @ingroup jsr82
 * @brief #include <btCommon.h>
 * @{
 */

#include <stddef.h>
#include <javacall_bt.h>

typedef javacall_uint8 uint8_t;
typedef javacall_uint16 uint16_t;
typedef javacall_uint32 uint32_t;
/*
 * @def BT_ADDRESS_SIZE
 * Bluetooth address size.
 */
#define BT_ADDRESS_SIZE  JAVACALL_BT_ADDRESS_SIZE

/*
 * @def MAX_DEVICES
 * Maximum number of devices connected.
 */
#define MAX_DEVICES 7

/* Major device service classes which should always be set. */
#define BT_SDDB_SERVICE_CLASSES 0x100000

/* Type name for storing UTF-8 strings, e.g. user-friendly device names. */
typedef char *bt_string_t;

/* Type for storing inquiry result response. */
typedef struct {
    javacall_bt_address bdaddr; /*< Bluetooth address of a discovered device. */
    uint32_t cod;       /*< 'Class of device' parameter. */
} bt_inquiry_t;

/*
 * @def BT_INVALID_ACL_HANDLE
 * Defines invalid ACL handle value.
 *
 * Note: ACL valid handle range: 0x0000-0x0EFF (0x0F00-0x0FFF reserved for
 * future use).
 */
#define BT_INVALID_ACL_HANDLE  -1

/*
 * @def BT_INVALID_HANDLE
 * Defines invalid L2CAP/RFCOMM connection handle.
 */
#define BT_INVALID_HANDLE (void *)(-1)

/* Type for storing 128-bit UUIDs. */
typedef uint8_t bt_uuid_t[16];

/* Protocol types enumeration. */
typedef enum { BT_L2CAP, BT_SPP, BT_GOEP } bt_protocol_t;

/* Type representing push registration entry. */
typedef struct {
    bt_protocol_t protocol; /*< Protocol type. */
    bt_uuid_t uuid;         /*< Service UUID. */
} bt_port_t;

/*
 * @def BT_INVALID_SDDB_HANDLE
 * Defines invalid service record handle.
 */
#define BT_INVALID_SDDB_HANDLE 0

/*
 * @typedef bt_sddbid_t
 * @brief Service record handle type.
 */
typedef uint32_t bt_sddbid_t;

/*
 * @typedef bt_record_t
 * @brief Type for stroing service records.
 */
typedef struct bt_record_t bt_record_t;

/*
 * @struct bt_record_t
 * @brief Type for stroing service records.
 */
struct bt_record_t {
    bt_sddbid_t id;   /*< Service record handle. */
    uint32_t classes; /*< Device service classes activated by this record. */
    void *data;       /*< Binary representation of this record. */
    size_t size;      /*< Size of the binary data. */
};

typedef javacall_handle bt_handle_t;
typedef javacall_bt_address bt_bdaddr_t;
typedef javacall_result bt_result_t;
typedef javacall_bool bt_bool_t;
/* @} */

#endif /* __BT_COMMON_H__ */
