/*
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

#ifndef __JAVACALL_SENSOR_DES_H
#define __JAVACALL_SENSOR_DES_H

#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_defs.h" 

typedef struct{
  signed char* pointer;
  int size;
} sensor_dcontext;

sensor_dcontext sensor_d_init(signed char* pointer, int size);

javacall_int32 sensor_d_nextInt(sensor_dcontext* sd);

javacall_utf16_string sensor_d_nextString(sensor_dcontext* sd);

javacall_utf16_string* sensor_d_nextStringArray(sensor_dcontext* sd, javacall_int32 size);

javacall_int8 sensor_d_nextBoolean(sensor_dcontext* sd);

javacall_int64* sensor_d_nextLongArray(sensor_dcontext* sd, javacall_int32 size);

javacall_int32* sensor_d_nextIntArray(sensor_dcontext* sd, javacall_int32 size);

#ifdef __cplusplus
}
#endif
#endif