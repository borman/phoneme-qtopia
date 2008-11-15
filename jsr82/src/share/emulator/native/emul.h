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

#ifndef __BT_EMUL_H_
#define __BT_EMUL_H_

#include <kni.h>
#include <commonKNIMacros.h>
#include <ROMStructs.h>
#include <btCommon.h>
#include <btMacros.h>
#include <midp_logging.h>
#include <string.h>
#include <stdio.h>
#include <midpMalloc.h>

#define BTE_LOG_CH 77777
#define LOG(msg) REPORT_INFO(BTE_LOG_CH, msg)
#define LOG1(msg, param) REPORT_INFO1(BTE_LOG_CH, msg, param)

typedef struct Java_com_sun_midp_jsr82emul_EmulationPolling _emulationPolling;
#define getEmulationPollingPtr(handle) (unhand(_emulationPolling,(handle)))

typedef struct Java_com_sun_midp_jsr82emul_ServiceConnectionData _connData;
#define getServiceConnectionDataPtr(handle) (unhand(_connData,(handle)))

// IMPL_NOTE use constants.xml instead. 
// So far these values must be supported in correspondence with
// Const in EmulUnit.java
#define DEFAULT_QUEUE_SIZE 512
#define MAX_REQ 32
#define MAX_CONN 16
#define NOTIF_REQ 2
#define DEVICE_REQ 3
#define IP_SIZE 4
#define BTADDR_SIZE 6
#define CONN_FAILURE -2
#define CONN_ENDOF_INP -1
#define DEFAULT_COD 0x204
#define BTE_SIGNAL_HANDLE 0

#ifdef __cplusplus
extern "C" {
#endif

void ensureInitialized();
void queueRequest(char* req, int len);
bt_handle_t getNextHandle(int protocol);
void resetConnInfo(bt_handle_t handle);

#ifdef __cplusplus
}
#endif

/* 
 * Emulation state. It is bit scale that keeps flags indicating
 * if data initialized, polling started, etc.
 */
extern int state;

/* Flags bits for state. */
enum state_flags {
    INITIALIZED = 1,
    POLLING_STARTED = 2,
    SDP_REQ_EXPECTED = 4,
    BLUETOOTH_ON = 8,
    DEVICE_INITED = 16
};

typedef struct _accept_info_t {
    char flags;
    int status;
    int conn_handle;
    bt_bdaddr_t peer_addr;
    
    int imtu;
    int omtu;
} accept_info_t;

typedef struct _connection_info_t {
    char flags;
    int connect_status;
    int receive_status;
    bt_bdaddr_t peer_addr;
    
    int imtu;
    int omtu;
    
    int sent;
    
    jbyte* in;
    int in_len;
    
    jbyte* out;
    int out_len;
    
} connection_info_t;

typedef union _conn_notif_u {
    accept_info_t accept; 
    connection_info_t conn; 
} conn_notif_u;

typedef struct _emul_data_t {
    /* local device address. */
    bt_bdaddr_t local_addr;
    
    /* device class without service classes. */
    int device_class_base;
    /* device class with service classes. */
    int device_class;
    /* device access code. */
    int access_code;
    
    /* Queue of emulation requests. */
    jbyte *request;
    /* Amount of bytes currently allocated for requests queue. */
    int req_size;
    /* Current offset in requests queue. */
    int req_offset;
    
    conn_notif_u handled_info[MAX_CONN];
} emul_data_t;

/* Shared emulation data. */
extern emul_data_t emul_data;

/* Requests for MainCaller. */
enum {    
    DEVICE,
    CREATE_NOTIF,
    CREATE_CONN,
    CONNECTIONS
}; 

/* Requests for NotifierEmul. */
enum {    
    NOTIF_ACCEPT,
    NOTIF_CLOSE,
    NOTIF_INIT,
    NOTIF_SET_OPTS
};   

/* Requests for ConnectionEmul. */
enum {    
    CONN_OPEN,
    CONN_CLOSE,
    CONN_INIT,
    CONN_SEND,
    CONN_RECEIVE
};   

/* Requests for DeviceEmul. */
enum {
    UPDATE_CLASS,
    UPDATE_ACCESS,
    START_INQUIRY,
    CANCEL_INQUIRY,
    INIT_DEVICE 
};

/* 
 * Flags for connection and notifiers information structures. 
 * Be careful with L2CAP - it is 0 for easy comliance with java.
 * Use (!(flags & RFCOMM)) instead of (flags & L2CAP). 
 */
enum connflags {
    L2CAP = 0,
    RFCOMM = 1,
    IN_USE = 2,
    SKIP_REQUEST = 4,
    ENDOF_INP_REACHED = 8,
    ENCRYPT = 16,
    AUTHENTICATE = 32
};

#define START_REQUEST \
    {\
        jbyte _buff[MAX_REQ]; \
        int _offset = 0; \
        (void)_offset; {

#define END_REQUEST queueRequest((char*)_buff, _offset); }}

#define APPEND_BYTE(b) _buff[_offset++] = (jbyte)(int)(b);

#define APPEND_BYTES(bytes, length) \
    { \
        int i; \
        for (i = 0; i < length; i++) { \
            APPEND_BYTE(bytes[i]); \
        } \
    }

#define APPEND_INT(value) \
    APPEND_BYTE(value); \
    APPEND_BYTE(value >> 8); \
    APPEND_BYTE(value >> 16); \
    APPEND_BYTE(value >> 24);

#define APPEND_BUFFER(buffer1, len1, buffer2, len2) \
    buffer1 = midpRealloc  (buffer1, len1 + len2); \
    memcpy(&buffer1[len1], buffer2, len2); \
    len1 += len2;

#endif //__BT_EMUL_H_
