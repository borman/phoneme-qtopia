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

#ifndef __JAVACALL_SENSOR_H
#define __JAVACALL_SENSOR_H

/**
 * @file javacall_sensor.h
 * @ingroup JSR256
 * @brief Javacall interfaces for JSR-256 Sensor API
 *
 */

#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_defs.h" 

typedef struct {
    javacall_utf16_string name;
    javacall_utf16_string unit;
    javacall_int32 data_type; // 1 == Double type, 2 == Integer type, 4 == Object type (unsupported)
    javacall_int32 accuracy;
    javacall_int32 scale;
    javacall_int32 mrange_count;
    javacall_int64* mranges; // for each mrange: smallest, largest, resolution double values as long (converted in Java)
} javacall_sensor_channel;

typedef struct {
   javacall_utf16_string description;
   javacall_utf16_string model;
   javacall_utf16_string quantity;
   javacall_utf16_string context_type; // user, device, ambient
   javacall_int32 connection_type; // 1 == embedded, 2 == remote, 4 == short range wireless, 8 == wired 
   javacall_int8 availability_push;
   javacall_int8 condition_push;
   javacall_int32 max_buffer_size;
   javacall_int32 channel_count;
   // properties
   javacall_utf16_string* properties; // even number of strings, taken as pairs (key-value)
   javacall_int32 prop_size;
   // errors
   javacall_utf16_string* err_messages;
   javacall_int32* err_codes;
   javacall_int32 err_size;
} javacall_sensor_info;

/*
 * Fetch the number of sensors
 * NOTE: called only once 
 * @param count             a pointer where to write the actual number 
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    failure 
 */ 
javacall_result javacall_sensor_count(javacall_int32* count);

/*
 * Get Sensor information 
 * @param sensor            sensor's id
 * @param info              OUT:sensor's info, pointer fields will be freed correctly by javacall_free
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    failure
 */
javacall_result javacall_sensor_get_info(int sensor, javacall_sensor_info* info);

/*
 * Get Channel information 
 * @param sensor            sensor's id
 * @param channel           channel's id
 * @param data              OUT:sensor's channel's info, pointer fields will be freed correctly by javacall_free
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    failure
 */
javacall_result javacall_sensor_get_channel(int sensor,int channel, javacall_sensor_channel* data);

/*
 * Check whether the sensor is available.
 * @param sensor            the sensor's id
 *
 * @retval JAVACALL_OK      available
 * @retval JAVACALL_FAIL    unavailable
 */
javacall_result javacall_sensor_is_available(int sensor);

/**
 * Call this function to open a sensor connection. After successful instantiation of the SensorConnection, 
 * this function is called and the connection state is set to STATE_OPEN. Generally this function 
 * could have nothing to do, but sometimes this function needs to do some initialization work like 
 * initializing the physical sensor, swithing on a sensor device, etc.
 *
 * @param sensor                                 the sensor's id
 * @param pContext                               ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail 
 */
javacall_result javacall_sensor_open(int sensor, void** pContext);

/**
 * Call this function to close a sensor connection. 

 * @param sensor                                 the sensor's id
 * @param pContext                               ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread 
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail 
 */
javacall_result javacall_sensor_close(int sensor, void** pContext);

/**
 * Notify the JVM that the open or close action for connection is ready
 *
 * @param sensor                                 the sensor's id
 * @param isOpen                                 indicate this notification is for connection open or connection close.
 *                                                         JAVACALL_TRUE: open, 
 *                                                         JAVACALL_FALSE: close.
 * @param errCode                               if there is an error occured when opening/closing the connection, 
 *                                                         this value is set to indicate the error. Otherwise it is set to OK.
*/
void javanotify_sensor_connection_completed( int sensor, javacall_bool isOpen, int errCode);

/**
 * Get channel data  from native sensor.
 *
 * @param sensor            the sensor's id
 * @param channel          the channel of the sensor
 * @param data               the retrived data buffer, WILL BE FREED by javacall_free.
 *                    Data are expected to be in following structure (serialized by Java):
 *                 * data Type (1B, 1 is double type, 2 is integer type)
 *                 * data length (4B integer) 
 *                 for each value
 *                   * validity (1B boolean)
 *                   * uncertainty (4B float)
 *                   * value (4B int OR 8B double)
 *   
 * @param dataCount       Out: the size of the buffer
 *
 * @param pContext                                ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail
 */
javacall_result javacall_sensor_get_channel_data( int sensor, int channel, signed char** data, int* dataCount,
                                                 void** pContext);

/**
 * Start measuring sensor data. After this function being called, native platform should call javanotify_sensor_channel_data_available()
 * to notify jvm when there are data available.
 *
 * @param sensor                the sensor's id
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_start_measuring_data(int sensor);

/**
 * Stop measuring sensor data. After this function being called, JVM will no longer
 * be notified when there are data available.
 *
 * @param sensor                the sensor's id
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_stop_measuring_data(int sensor);

/**
 * Notify java that there are data available or there is an error occured.
 *
 * @param sensor                the sensor's id
 * @param channel              the channel of the sensor that is being monitored
 * @param errCode             The error code, if an error occured when reading data, this function is called 
 *                                       to notify JVM that there is an error and this value is set to indicate the error.
 *                                       Otherwise this value is set to OK.
 *
 */
void javanotify_sensor_channel_data_available(int sensor, int channel, int errCode);

/**
 * Start monitoring the specified sensor's availability. If the monitered sensor's available state is changed,
 * native platform should notify jvm by calling javanotify_sensor_availability().
 *
 * @param sensor                the sensor's id
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_start_monitor_availability(int sensor);

/**
 * Stop monitoring the specified sensor's availability. Java will no longer be notified if there are 
 * available state changes for the specified sensor.
 *
 * @param sensor                the sensor's id
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_stop_monitor_availability(int sensor);

/**
 * Notify java that the monitered sensor's available state is changed.
 *
 * @param sensor             the sensor's id
 * @param isAvailable       the new available state of the monitered sensor. True means the sensor becomes
 *                                    available, false otherwise.
 */
void javanotify_sensor_availability(int sensor, javacall_bool isAvailable);

#ifdef __cplusplus
}
#endif

#endif

