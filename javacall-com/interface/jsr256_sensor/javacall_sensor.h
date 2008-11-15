/*
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

typedef enum {
    // R12 sensor
    /** sensor:current_bearer */
    JAVACALL_SENSOR_CURRENT_BEARER =0,
    /** sensor:network_code */
    JAVACALL_SENSOR_NETWORK_CODE,
    /** sensor:network_quality */
    JAVACALL_SENSOR_NETWORK_QUALITY,
    /** sensor:data_counter */
    JAVACALL_SENSOR_DATA_COUNTER,
    /** sensor:cellid */
    JAVACALL_SENSOR_CELL_ID,
    /** sensor:sound_level */
    JAVACALL_SENSOR_SOUND_LEVEL,
    /** sensor:battery_level */
    JAVACALL_SENSOR_BATTERY_LEVEL,
    /** sensor:battery_charge */
    JAVACALL_SENSOR_BATTERY_CHARGE,
    /** sensor:flip_state */
    JAVACALL_SENSOR_FLIP_STATE,
    // DVB-H sensor
    /** sensor:dvbh_quality */
    JAVACALL_DVBH_SIGNAL_QUALITY,
    /** sensor:dvbh_realtime*/
    JAVACALL_DVBH_REALTIME
} javacall_sensor_type;

/*Class Data can have 3 data types, int, double and object,
* this union is defined for generic function's clarification
*/
typedef union{
  int intValue;
  double doubleValue;
  long objValue;
}javacall_sensor_channel_data;


/* Note: */
/* We will put all sensor properties in Java XML file.
 * So, we don't have javacall for fetching sendor url, model, version, properties here
 */  

/*
 * Check whether the sensor is available.
 * @param sensor            the sensor's type
 *
 * @retval JAVACALL_OK      available
 * @retval JAVACALL_FAIL    unavailable
 */
javacall_result javacall_sensor_is_available(javacall_sensor_type sensor);

/**
 * Call this function to open a sensor connection. After successful instantiation of the SensorConnection, 
 * this function is called and the connection state is set to STATE_OPEN. Generally this function 
 * could have nothing to do, but sometimes this function needs to do some initialization work like 
 * initializing the physical sensor, swithing on a sensor device, etc.
 *
 * @param sensor                                 the sensor's type
 * @param pContext                               ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail 
 */
javacall_result javacall_sensor_open(javacall_sensor_type sensor, void** pContext);

/**
 * Call this function to close a sensor connection. 

 * @param sensor                                 the sensor's type
 * @param pContext                               ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread 
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail 
 */
javacall_result javacall_sensor_close(javacall_sensor_type sensor, void** pContext);

/**
 * Notify the JVM that the open or close action for connection is ready
 *
 * @param sensor                                 the sensor's type
 * @param isOpen                                 indicate this notification is for connection open or connection close.
 *                                                         JAVACALL_TRUE: open, 
 *                                                         JAVACALL_FALSE: close.
 * @param errCode                               if there is an error occured when opening/closing the connection, 
 *                                                         this value is set to indicate the error. Otherwise it is set to OK.
*/
void javanotify_sensor_connection_completed( javacall_sensor_type sensor, javacall_bool isOpen, int errCode);

/**
 * Get channel data  from native sensor.
 *
 * @param sensor            the sensor's type
 * @param channel          the channel of the sensor
 * @param data               the retrived data
 * @param dataCount      IN-OUT, In: maximum number of data can be read, Out: the actually number of data are read
 *
 * @param pContext                                ptr to context from previous calls
 *                                               NULL on start 
 * @retval JAVACALL_WOULD_BLOCK     the operation would block the java thread
 * @retval JAVACALL_OK                       success
 * @retval JAVACALL_FAIL                     fail
 */
javacall_result javacall_sensor_get_channel_data( javacall_sensor_type sensor, int channel, javacall_sensor_channel_data* data, int* dataCount,
                                                 void** pContext);

/**
 * Start measuring sensor data. After this function being called, native platform should call javanotify_sensor_channel_data_available()
 * to notify jvm when there are data available.
 *
 * @param sensor                the sensor's type
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_start_measuring_data(javacall_sensor_type sensor);

/**
 * Stop measuring sensor data. After this function being called, JVM will no longer
 * be notified when there are data available.
 *
 * @param sensor                the sensor's type
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_stop_measuring_data(javacall_sensor_type sensor);

/**
 * Notify java that there are data available or there is an error occured.
 *
 * @param sensor                the sensor's type
 * @param channel              the channel of the sensor that is being monitored
 * @param errCode             The error code, if an error occured when reading data, this function is called 
 *                                       to notify JVM that there is an error and this value is set to indicate the error.
 *                                       Otherwise this value is set to OK.
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javanotify_sensor_channel_data_available(javacall_sensor_type sensor, int channel, int errCode);

/**
 * Start monitoring the specified sensor's availability. If the monitered sensor's available state is changed,
 * native platform should notify jvm by calling javanotify_sensor_availability().
 *
 * @param sensor                the sensor's type
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_start_monitor_availability(javacall_sensor_type sensor);

/**
 * Stop monitoring the specified sensor's availability. Java will no longer be notified if there are 
 * available state changes for the specified sensor.
 *
 * @param sensor                the sensor's type
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_sensor_stop_monitor_availability(javacall_sensor_type sensor);

/**
 * Notify java that the monitered sensor's available state is changed.
 *
 * @param sensor             the sensor's type
 * @param isAvailable       the new available state of the monitered sensor. True means the sensor becomes
 *                                    available, false otherwise.
 */
void javanotify_sensor_availability(javacall_sensor_type sensor, javacall_bool isAvailable);

/*
 * Emulator functions
 */
int* javacall_sensor_emulator_set_int_buffer(int length);
double* javacall_sensor_emulator_set_double_buffer(int length);

#ifdef __cplusplus
}
#endif

#endif

