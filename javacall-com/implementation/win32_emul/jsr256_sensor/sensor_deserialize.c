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

#include <assert.h>

#include "javacall_sensor_deserialize.h"

#include <javacall_memory.h>

in_place_switch(signed char* p, int n)
{
  signed char tmp;
  int i;
  
  for (i=0; i<(n/2); ++i){
    tmp = p[i];
    p[i] = p[n-i-1];
    p[n-i-1] = tmp;
  }
}

sensor_dcontext sensor_d_init(signed char* pointer, int size)
{
  sensor_dcontext ret;
  ret.pointer = pointer;
  ret.size = size;
  return ret;
}

int sensor_d_hasNext(sensor_dcontext* sd)
{
  return sd->size;
}

javacall_int32 sensor_d_nextInt(sensor_dcontext* sd)
{
  javacall_int32 ret;
  
  assert(sd->size >= sizeof(javacall_int32));
  
  sd->size -= sizeof(javacall_int32);
  in_place_switch(sd->pointer,sizeof(javacall_int32));
  ret = *((javacall_int32*) sd->pointer);
  sd->pointer += sizeof(javacall_int32);
  return ret;
}

javacall_utf16_string sensor_d_nextString(sensor_dcontext* sd)
{
  javacall_int32 size = sensor_d_nextInt(sd);
  javacall_utf16_string ret;
  javacall_int32 i;
  
  assert(sd->size >= size);
  
  for (i=0; i < size; ++i){
    in_place_switch(sd->pointer+2*i,2);
  }
  
  ret = javacall_malloc(sizeof(javacall_utf16)*(size + 1));
  if (!ret)
    return NULL;
  memcpy(ret, sd->pointer, sizeof(javacall_utf16)*size);
  ret[size] = 0;
  
  sd->pointer += size*2;
  sd->size -= size*2;
  
  return ret;
}

javacall_utf16_string* sensor_d_nextStringArray(sensor_dcontext* sd, javacall_int32 size)
{
  javacall_utf16_string* ret;
  javacall_int32 i;
  
  ret = javacall_malloc(sizeof(javacall_utf16_string)*size);
  if (!ret)
    return NULL;
  for (i=0; i<size; ++i){
    ret[i] = sensor_d_nextString(sd);
  }
  return ret;
}

javacall_int32* sensor_d_nextIntArray(sensor_dcontext* sd, javacall_int32 size)
{
  javacall_int32* ret;
  javacall_int32 i;
  
  ret = javacall_malloc(sizeof(javacall_int32)*size);
  if (!ret)
    return NULL;
  for (i=0; i<size; ++i){
    ret[i] = sensor_d_nextInt(sd);
  }
  return ret;
}

javacall_int8 sensor_d_nextBoolean(sensor_dcontext* sd)
{
  javacall_int8 ret;
  
  assert(sd->size >= sizeof(javacall_int8));
  
  sd->size -= sizeof(javacall_int8);
  ret = *((javacall_int8*) sd->pointer);
  sd->pointer += sizeof(javacall_int8);
  return ret;
}

javacall_int64* sensor_d_nextLongArray(sensor_dcontext* sd, javacall_int32 size)
{
  javacall_int64* ret;
  javacall_int32 i;
  
  assert(sd->size >= sizeof(javacall_int64)*size);
  
  for (i=0;i<size;++i){
    in_place_switch(sd->pointer+i*sizeof(javacall_int64),sizeof(javacall_int64));
  }
  
  ret = javacall_malloc(size*sizeof(javacall_int64));
  if (!ret)
    return NULL;
    
  memcpy(ret, sd->pointer, sizeof(javacall_int64)*size);
  
  sd->size -= sizeof(javacall_int64)*size;  
  sd->pointer += sizeof(javacall_int64)*size;
  
  return ret;
}
