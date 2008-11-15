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

#include <javacall_bt.h>
#include <javanotify_bt.h>
#include <btPush.h>

#include <midp_thread.h>
#include <push_server_export.h>
#include <stdio.h>


/*
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
void javanotify_bt_protocol_event(javacall_bt_callback_type type,
  javacall_handle handle, javacall_result operation_result) {

    switch (type) {
    case JAVACALL_EVENT_BT_ACCEPT_COMPLETE:
        midp_thread_signal(NETWORK_READ_SIGNAL, (int)handle, operation_result);
        if (bt_push_find_server(handle) != BT_INVALID_PUSH_HANDLE) {
            findPushBlockedHandle((int)handle);
            midp_thread_signal(PUSH_SIGNAL, 0, 0);
        }
        break;

    case JAVACALL_EVENT_BT_CONNECT_COMPLETE:
        midp_thread_signal(NETWORK_WRITE_SIGNAL, (int)handle, operation_result);
        break;

    case JAVACALL_EVENT_BT_SEND_COMPLETE:
        midp_thread_signal(NETWORK_WRITE_SIGNAL, (int)handle, operation_result);
        break;

    case JAVACALL_EVENT_BT_RECEIVE_COMPLETE:
        midp_thread_signal(NETWORK_READ_SIGNAL, (int)handle, operation_result);
        break;

/*    default: /* illegal argument */
    }
}
