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

#include <javacall_sensor.h>
#include <javacall_events.h>
#include <javacall_memory.h>
#include <kni.h>
#include <sni.h>
#include <midpServices.h>
#include <midp_thread.h>
#include <midpMalloc.h>
#include <midp_jc_event_defs.h>
#include <nativeChannel.h>

KNIEXPORT KNI_RETURNTYPE_OBJECT  
Java_com_sun_javame_sensor_NativeChannel_doMeasureData(void)
{
    javacall_result res;
    int sensor = KNI_GetParameterAsInt(1);
    int channel = KNI_GetParameterAsInt(2);

    jsize i;
    void *context = NULL; 
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
	  signed char* data;
	  int data_length;
	  
	  KNI_StartHandles(1);
    KNI_DeclareHandle(buffer);

	if (info != NULL) { // reinvocation
		context = info->pResult;
	}

	res =
       javacall_sensor_get_channel_data(sensor,channel,&data,&data_length,&context);
		
    switch (res) {
		case JAVACALL_OK:
		  SNI_NewArray(SNI_BYTE_ARRAY, data_length, buffer);
      for (i = 0; i<data_length; ++i ) {
        KNI_SetByteArrayElement(buffer, i, (jbyte) data[i]);
      }
      javacall_free(data); // free data allocated in javacall code
			break;
		case JAVACALL_FAIL:
			SNI_NewArray(SNI_BYTE_ARRAY, 0, buffer);
			break;
		case JAVACALL_WOULD_BLOCK:
			midp_thread_wait(JSR256_SIGNAL, sensor, context);
			break;
	}
	KNI_EndHandlesAndReturnObject(buffer);
}


KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeSensor_doInitSensor(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    void *context = NULL;
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
    javacall_result res;
	jboolean returnValue = KNI_FALSE;
	if (info != NULL) { // reinvocation
		context = info->pResult;
	}
	res = javacall_sensor_open(sensor,&context);
		
	switch (res) {
		case JAVACALL_OK:
			returnValue = KNI_TRUE;
			break;
		case JAVACALL_FAIL:
			break;
		case JAVACALL_WOULD_BLOCK:
			midp_thread_wait(JSR256_SIGNAL, sensor, context);
			break;
	}
    KNI_ReturnBoolean(returnValue);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeSensor_doFinishSensor(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    void *context = NULL;
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
    javacall_result res;
	jboolean returnValue = KNI_FALSE;
	if (info != NULL) { // reinvocation
		context = info->pResult;
	}
	res = javacall_sensor_close(sensor,&context);
		
    switch (res) {
		case JAVACALL_OK:
			returnValue = KNI_TRUE;
			break;
		case JAVACALL_FAIL:
			break;
		case JAVACALL_WOULD_BLOCK:
			midp_thread_wait(JSR256_SIGNAL, sensor, context);
			break;
	}

	KNI_ReturnBoolean(returnValue);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeSensor_doIsAvailable(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    javacall_result res = javacall_sensor_is_available(sensor);
    KNI_ReturnBoolean(JAVACALL_OK==res);
}

// Processing of notifications

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
void javanotify_sensor_connection_completed( int sensor, javacall_bool isOpen, int errCode) {
    midp_jc_event_union e;
	e.eventType = JSR256_JC_EVENT_SENSOR_OPEN_CLOSE;
	e.data.jsr256_jc_event_sensor.sensor = sensor;
	e.data.jsr256_jc_event_sensor.isOpen = isOpen;
	e.data.jsr256_jc_event_sensor.errCode = errCode;
	javacall_event_send(&e, sizeof(e));
}

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
void javanotify_sensor_channel_data_available(int sensor, int channel, int errCode) {
    midp_jc_event_union e;
	e.eventType = JSR256_JC_EVENT_SENSOR_DATA_READY;
	e.data.jsr256_jc_event_sensor_data_ready.sensor = sensor;
	e.data.jsr256_jc_event_sensor_data_ready.channel = channel;
	e.data.jsr256_jc_event_sensor_data_ready.errCode = errCode;
	javacall_event_send(&e, sizeof(e));
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeSensorRegistry_doStartMonitoringAvailability(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    javacall_result res = javacall_sensor_start_monitor_availability(sensor);
    KNI_ReturnBoolean(JAVACALL_OK==res);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeSensorRegistry_doStopMonitoringAvailability(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    javacall_result res = javacall_sensor_stop_monitor_availability(sensor);
    KNI_ReturnBoolean(JAVACALL_OK==res);
}

