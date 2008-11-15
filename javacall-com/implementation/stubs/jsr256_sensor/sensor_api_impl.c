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

#include <javacall_sensor.h>


javacall_result javacall_sensor_get_channel_data(javacall_sensor_type sensor, int channel,
    javacall_sensor_channel_data* data, int* dataCount, void** pContext)
{
    (void)sensor;
    (void)channel;
    (void)data;
    (void)dataCount;
    (void)pContext;
	return JAVACALL_FAIL;
}


javacall_result javacall_sensor_is_available(javacall_sensor_type sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_open(javacall_sensor_type sensor, void** pContext)
{
    (void)sensor;
    (void)pContext;
	return JAVACALL_FAIL;
}

javacall_result javacall_sensor_close(javacall_sensor_type sensor, void** pContext)
{
    (void)sensor;
    (void)pContext;
	return JAVACALL_FAIL;
}

javacall_result javacall_sensor_start_measuring_data(javacall_sensor_type sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_stop_measuring_data(javacall_sensor_type sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javanotify_sensor_channel_data_available(javacall_sensor_type sensor, int channel, int errCode)
{
    (void)sensor;
    (void)channel;
    (void)errCode;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_start_monitor_availability(javacall_sensor_type sensor)
{
    (void)sensor;
    return JAVACALL_FAIL;
}

javacall_result javacall_sensor_stop_monitor_availability(javacall_sensor_type sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}


/*
 * Emulator functions
 */

// reserve the buffer of integer array and return its pointer
int* javacall_sensor_emulator_set_int_buffer(int length) {
    (void)length;
	return (int*)NULL;
}

// reserve the buffer of double array and return its pointer
double* javacall_sensor_emulator_set_double_buffer(int length) {
    (void)length;
	return (double*)NULL;
}

