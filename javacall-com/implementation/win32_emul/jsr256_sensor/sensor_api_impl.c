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

#include <javacall_sensor.h>

#include "lime.h"
#include "javacall_sensor_deserialize.h"

#include <javacall_memory.h>

#define SENSOR_PACKAGE "com.sun.kvem.sensor"
#define SENSOR_DEVICE_PROXY "SensorProxyServer"

javacall_result javacall_sensor_get_channel_data(int sensor, int channel,
    signed char** data,int* data_length, void** pContext)
{
    int buffer_size;
    signed char *buffer;
    static LimeFunction * f = NULL;
   
    if (f == NULL) {
        f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "measureData");
    }
    f->call(f, &buffer, &buffer_size, sensor, channel);
    
    // copy buffer
    *data = javacall_malloc(sizeof(signed char)*buffer_size);
    if (*data == 0){
      return JAVACALL_FAIL;
    }    
    memcpy(*data, buffer, buffer_size);
    *data_length = buffer_size;
        
    return JAVACALL_OK;
}

javacall_result javacall_sensor_is_available(int sensor)
{
    int ret_val;
    static LimeFunction * f = NULL;
    
    if (f == NULL) {
        f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "isAvailable");
    }
    f->call(f, &ret_val, sensor);
    return ret_val?JAVACALL_OK:JAVACALL_FAIL;
}

javacall_result javacall_sensor_open(int sensor, void** pContext)
{
  static LimeFunction * f = NULL;
  int ret_val;
  
  if (f == NULL) {
    f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "initSensor");
  }
  f->call(f, &ret_val, sensor);

  return ret_val?JAVACALL_OK:JAVACALL_FAIL;
}

javacall_result javacall_sensor_close(int sensor, void** pContext)
{
  static LimeFunction * f = NULL;
  int ret_val;
  
  if (f == NULL) {
    f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "finishSensor");
  }
  f->call(f, &ret_val, sensor);
    
  return ret_val?JAVACALL_OK:JAVACALL_FAIL;
}

javacall_result javacall_sensor_start_measuring_data(int sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

javacall_result javacall_sensor_stop_measuring_data(int sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

javacall_result javacall_sensor_start_monitor_availability(int sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

javacall_result javacall_sensor_stop_monitor_availability(int sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}


javacall_result javacall_sensor_count(javacall_int32* count)
{
  LimeFunction * f =  NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "sensorCount");
  f->call(f, count);
  DeleteLimeFunction(f); // This function is called only once, we can now delete the Lime function
  return JAVACALL_OK;
}

javacall_result javacall_sensor_get_info(int sensor, javacall_sensor_info* info)
{
  static LimeFunction * f = NULL;
  int buffer_size;
  signed char *buffer;
  sensor_dcontext dc;
  
  if (f == NULL) {
    f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "getSensor");
  }
  f->call(f, &buffer, &buffer_size, sensor);
  
  dc = sensor_d_init(buffer, buffer_size);
  
  info->description = sensor_d_nextString(&dc);
  info->model = sensor_d_nextString(&dc);  
  info->quantity = sensor_d_nextString(&dc);
  info->context_type = sensor_d_nextString(&dc);
  info->connection_type = sensor_d_nextInt(&dc); 
  info->availability_push = sensor_d_nextBoolean(&dc); 
  info->condition_push = sensor_d_nextBoolean(&dc); 
  info->max_buffer_size = sensor_d_nextInt(&dc); 
  info->channel_count = sensor_d_nextInt(&dc); 
  info->prop_size = sensor_d_nextInt(&dc);
  info->properties = sensor_d_nextStringArray(&dc,info->prop_size*2);
  info->err_size = sensor_d_nextInt(&dc);
  info->err_codes = sensor_d_nextIntArray(&dc,info->err_size);
  info->err_messages = sensor_d_nextStringArray(&dc,info->err_size);
  
  if(!info->description || !info->model || !info->quantity || !info->context_type
    || !info->properties || !info->err_messages || !info->err_codes)
    return JAVACALL_FAIL;
  
  return JAVACALL_OK;
}

javacall_result javacall_sensor_get_channel(int sensor,int channel, javacall_sensor_channel* data)
{
  static LimeFunction * f = NULL;
  int buffer_size;
  signed char *buffer;
  sensor_dcontext dc;
  
  if (f == NULL) {
    f = NewLimeFunction(SENSOR_PACKAGE, SENSOR_DEVICE_PROXY, "getChannel");
  }
  f->call(f, &buffer, &buffer_size, sensor, channel);
  
  dc = sensor_d_init(buffer, buffer_size);
  
  data->name = sensor_d_nextString(&dc);
  data->unit = sensor_d_nextString(&dc);
  data->data_type = sensor_d_nextInt(&dc);
  data->accuracy = sensor_d_nextInt(&dc);
  data->scale = sensor_d_nextInt(&dc);
  data->mrange_count = sensor_d_nextInt(&dc);
  data->mranges = sensor_d_nextLongArray(&dc,data->mrange_count*3); // each range has 3 long/double values
  
  if (!data->name || !data->unit || !data->mranges) // some malloc failed
    return JAVACALL_FAIL;
  
  return JAVACALL_OK;
}

