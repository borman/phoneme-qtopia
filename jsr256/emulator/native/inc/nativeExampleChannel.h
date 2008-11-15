
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
#ifndef __NATIVE_EXAMPLE_CHANNEL_
#define __NATIVE_EXAMPLE_CHANNEL_

#ifdef __cplusplus
extern "C" {
#endif 

#ifndef NULL
#define NULL ((void*)0)
#endif

/**
 * @enum channel_error_code
 * @brief error codes which return by measuring channel data
 * @see file ValueListener.java
 */
typedef enum {
    /* Error code: read data is OK. */
    DATA_READ_OK = 0,
	/* Error code: channel is busy. */
	CHANNEL_BUSY = 1,
	/* Error code: buffer is overflow. */
	BUFFER_OVERFLOW = 2,
	/* Error code: sensor becomes unavailable. */
	SENSOR_UNAVAILABLE = 3,
	/* Error code: other channel error. */
	MEASURING_FAIL = 4
} channel_error_code;

#define MAX_SENSOR_NUMBER 20
#define SENSOR_WAIT -1

#ifdef __cplusplus
}
#endif

#endif
