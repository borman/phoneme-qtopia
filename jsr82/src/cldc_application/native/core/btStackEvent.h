/*
 *
 *
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

#ifndef STACK_EVENT_H
#define STACK_EVENT_H

#include <javacall_bt.h>
#include <btCommon.h>

/*
 * @file
 * @ingroup jsr82stack
 * @brief #include <btStackEvent.h>
 * @{
 */

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Checks if Bluetooth events are available.
 *
 * @param retval pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if there are pending events,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_stack_check_events(javacall_bool *retval);

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
javacall_result bt_stack_read_data(void *data, int len, int *retval);

#ifdef __cplusplus
}
#endif

#endif /* STACK_EVENT_H */
