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


javacall_result javacall_sensor_count(javacall_int32* count)
{
    (void)count;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_get_info(int sensor, javacall_sensor_info* info)
{
    (void)sensor;
    (void)info;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_get_channel(int sensor,int channel, javacall_sensor_channel* data)
{
    (void)sensor;
    (void)channel;
    (void)data;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_is_available(int sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_open(int sensor, void** pContext)
{
    (void)sensor;
    (void)pContext;
	return JAVACALL_FAIL;
}

javacall_result javacall_sensor_close(int sensor, void** pContext)
{
    (void)sensor;
    (void)pContext;
	return JAVACALL_FAIL;
}

javacall_result javacall_sensor_get_channel_data( int sensor, int channel, signed char** data, int* dataCount,
    void** pContext)
{
    (void)sensor;
    (void)channel;
    (void)data;
    (void)dataCount;
    (void)pContext;
	return JAVACALL_FAIL;
}

javacall_result javacall_sensor_start_measuring_data(int sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_stop_measuring_data(int sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

/**
 * Notify java that there are data available or there is an error occured.
 *
 * @param sensor                the sensor's type
 * @param channel              the channel of the sensor that is being monitored
 * @param errCode             The error code, if an error occured when reading data, this function is called 
 *                                       to notify JVM that there is an error and this value is set to indicate the error.
 *                                       Otherwise this value is set to OK.
 *
 */
void javanotify_sensor_channel_data_available(int sensor, int channel, int errCode) {
    (void)sensor;
    (void)channel;
    (void)errCode;
}

javacall_result javacall_sensor_start_monitor_availability(int sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_stop_monitor_availability(int sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

