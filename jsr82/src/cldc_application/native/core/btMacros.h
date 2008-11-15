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

#ifndef _BT_NATIVE_COMMON_H_
#define _BT_NATIVE_COMMON_H_

/* KNI includes */
#include <kni.h>
#include <commonKNIMacros.h>

/* MIDP trace includes */
#include <midp_logging.h>
#include <midpError.h>

/* C trace includes */
#include <stdio.h>
#include <errno.h>
#include <string.h>

#if 0
#define PRINT_INFO(msg) printf("%s\n", msg);
#else
#define PRINT_INFO(msg)
#endif

#if 1
#define PRINT_ERROR(msg) printf("%s : %s(%d)\n", msg, strerror(errno), errno);
#else
#define PRINT_ERROR(msg)
#endif

#if 1
#define EXCEPTION_MSG(s) s
#else
#define EXCEPTION_MSG(s) NULL
#endif


#define GET_FIELDID(classHandle, fieldName, fieldType, id)      \
    if (id == NULL)                                             \
    {                                                           \
        id = KNI_GetFieldID(classHandle, fieldName, fieldType); \
        if (id == 0) {                                          \
            PRINT_ERROR("Invalid field or romizer settings")    \
            KNI_ThrowNew(midpRuntimeException,                  \
                "Invalid field or romizer settings");           \
        }                                                       \
    }

/* @def BT_BOOL Creates javacall_bool value. */
#define BT_BOOL(expr) ((expr) ? JAVACALL_TRUE : JAVACALL_FALSE)

/* @def BT_TEST Checks an expression for validness. */
#define BT_TEST(expr) \
        if (!(expr)) { \
            REPORT_WARN2(0, "Assertion failed: file %s line %d", \
                    __FILE__, __LINE__); \
            return JAVACALL_FAIL; \
}


#endif /* _BT_NATIVE_COMMON_H_ */
