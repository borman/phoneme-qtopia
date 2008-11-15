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

#ifndef STACK_EVENT_H
#define STACK_EVENT_H

#include <javacall_bt.h>
#include <btCommon.h>

#ifdef BT_USE_EVENT_API

/*
 * @file
 * @ingroup jsr82stack
 * @brief #include <btStackEvent.h>
 * @{
 */

#ifdef __cplusplus
extern "C" {
#endif

// The following API are notification functions, which may be called from
// any thread. However, no two functions should be executing at the same time.
// Those functions are not generally needed to be re-implemented during port.
// The default implementation should be sufficient for most of the time.

/*
 * Reports to the application that HCI event is available. Usually, this
 * function is used by other notification functions, and should not be called
 * directly elsewhere, unless low-level access to HCI is provided by the
 * underlying Bluetooth stack.
 *
 * @param event pointer to buffer containing HCI event. According to HCI layer
 *         specification, the first byte is event code, the second byte is
 *         is parameter total length (measured in octets), and the rest are
 *         event-specific parameters
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_hci_event(void *event);

/*
 * Reports to the application that the inquiry has been completed.
 *
 * @param success indicates whether the inquiry operation succeeded
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_inquiry_complete(javacall_bool success);

/*
 * Reports to the application that remote devices have been discovered.
 *
 * @param result inquiry result, essentially an array of inquiry records
 * @param count number of elements in the array
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_inquiry_result(bt_inquiry_t result[], int count);

/*
 * Reports to the application that authentication request has been completed.
 *
 * @param handle ACL connection handle
 * @param success indicates whether the authentication succeeded
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_authentication_complete(int handle, javacall_bool success);

/*
 * Reports to the application that the remote device name has been retrieved.
 *
 * @param bdaddr Bluetooth address of the remote device
 * @param name user-friendly name of the remote device
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_remote_name_complete(const javacall_bt_address bdaddr,
        const char *name);

/*
 * Reports to the application that link encryption has been changed.
 *
 * @param handle ACL connection handle
 * @param success indicates whether the change succeeded
 * @param on indicates whether link encryption is enabled
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_stack_on_encryption_change(int handle, javacall_bool success,
        javacall_bool on);

#ifdef __cplusplus
}
#endif

#endif /* BT_USE_EVENT_API */

#endif /* STACK_EVENT_H */
