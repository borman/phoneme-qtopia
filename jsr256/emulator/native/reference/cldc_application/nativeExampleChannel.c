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

#include <javacall_sensor.h>
#include <javacall_events.h>
#include <kni.h>
#include <sni.h>
#include <midpServices.h>
#include <midp_thread.h>
#include <midpMalloc.h>
#include <midp_jc_event_defs.h>
#include <nativeExampleChannel.h>

KNIEXPORT KNI_RETURNTYPE_INT 
Java_com_sun_javame_sensor_NativeExampleChannel_doMeasureData(void)
{
    javacall_result res;
    int sensor = KNI_GetParameterAsInt(1);
    int channel = KNI_GetParameterAsInt(2);

    int dataCount=1;
    void *context = NULL; 
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
    int returnValue = MEASURING_FAIL;
	javacall_sensor_channel_data data[1];

	KNI_StartHandles(1);
    KNI_DeclareHandle(buffer);
    KNI_GetParameterAsObject(3, buffer);


	if (info != NULL) { // reinvocation
		context = info->pResult;
	}

	res =
       javacall_sensor_get_channel_data(sensor,channel,data,&dataCount,&context);
		
    switch (res) {
		case JAVACALL_OK:
            KNI_SetIntArrayElement(buffer, (jint)0, data[0].intValue);
			returnValue = DATA_READ_OK;
			break;
		case JAVACALL_FAIL:
			returnValue = MEASURING_FAIL;
			break;
		case JAVACALL_WOULD_BLOCK:
			midp_thread_wait(JSR256_SIGNAL, sensor, context);
			break;
	}

	KNI_EndHandles();
    KNI_ReturnInt(returnValue);
}


KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_NativeExampleSensor_doInitSensor(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    void *context = NULL;
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
    javacall_result res;
	jboolean returnValue = KNI_FALSE;
	if (info != NULL) { // reinvocation
		context = info->pResult;
	}
	res = javacall_sensor_open((javacall_sensor_type)sensor,&context);
		
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
Java_com_sun_javame_sensor_NativeExampleSensor_doFinishSensor(void)
{
    int sensor = KNI_GetParameterAsInt(1);
    void *context = NULL;
    MidpReentryData* info = (MidpReentryData*)SNI_GetReentryData(NULL);
    javacall_result res;
	jboolean returnValue = KNI_FALSE;
	if (info != NULL) { // reinvocation
		context = info->pResult;
	}
	res = javacall_sensor_close((javacall_sensor_type)sensor,&context);
		
    switch (res) {
		case JAVACALL_OK:
			res = KNI_TRUE;
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
Java_com_sun_javame_sensor_NativeExampleSensor_doIsAvailable(void)
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
 *                                                         JAVACALL_FAIL: close.
 * @param errCode                               if there is an error occured when opening/closing the connection, 
 *                                                         this value is set to indicate the error. Otherwise it is set to OK.
*/
void javanotify_sensor_connection_completed( javacall_sensor_type sensor, javacall_bool isOpen, int errCode) {
    midp_jc_event_union e;
	e.eventType = JSR256_JC_EVENT_SENSOR_OPEN_CLOSE;
	e.data.jsr256_jc_event_sensor.sensor = sensor;
	e.data.jsr256_jc_event_sensor.isOpen = isOpen;
	e.data.jsr256_jc_event_sensor.errCode = errCode;
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

// Emulator functions

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_javame_sensor_NativeExampleChannel_setTestIntData(void)
{
    int buffLength;
	int* ptrBuff;
	KNI_StartHandles(1);
    KNI_DeclareHandle(intArray);
    KNI_GetParameterAsObject(1, intArray);
    buffLength = (int)KNI_GetArrayLength(intArray);
	ptrBuff = javacall_sensor_emulator_set_int_buffer(buffLength);
	KNI_GetRawArrayRegion(intArray, 0, buffLength * sizeof(int), (jbyte*)ptrBuff);
	KNI_EndHandles();
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_javame_sensor_NativeExampleChannel_setTestDoubleData(void)
{
    double buffLength;
	double* ptrBuff;
	KNI_StartHandles(1);
    KNI_DeclareHandle(doubleArray);
    KNI_GetParameterAsObject(1, doubleArray);
    buffLength = (int)KNI_GetArrayLength(doubleArray);
	ptrBuff = javacall_sensor_emulator_set_double_buffer(buffLength);
	KNI_GetRawArrayRegion(doubleArray, 0, buffLength * sizeof(double), (jbyte*)ptrBuff);
	KNI_EndHandles();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_javame_sensor_TestingSensor_setAvailableNative(void)
{
    javacall_sensor_type sensor = KNI_GetParameterAsInt(1);
    javacall_bool available = KNI_GetParameterAsBoolean(2);
    javanotify_sensor_availability(sensor, available);
    KNI_ReturnBoolean(KNI_TRUE);
}

