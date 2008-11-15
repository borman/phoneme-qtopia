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


#include <malloc.h>
#include <process.h>
#include <windows.h>

static int isInt = 1;
static int bufIntLen = 1;
static int* pBufInt = NULL;
static int bufDoubleLen = 1;
static double* pBufDouble = NULL;
static int buffIndex = 0;

javacall_result javacall_sensor_get_channel_data(javacall_sensor_type sensor, int channel,
    javacall_sensor_channel_data* data, int* dataCount, void** pContext)
{
    javacall_result res = JAVACALL_OK;
    (void)pContext;
    (void)sensor;
    (void)channel;

    *dataCount=1; // temporary
	if (isInt) {
		if (pBufInt == NULL) { // TCK
			int retValue = 0;
			switch (sensor) {
				case JAVACALL_SENSOR_CURRENT_BEARER:
					{
						int bearer_values[10] = {-1, 10, 11, 20, 21, 30, 31, 50, 51, 60};
						retValue = bearer_values[(int)GetTickCount()%10];
					}
					break;
				case JAVACALL_SENSOR_BATTERY_LEVEL:
						retValue = (int)GetTickCount()%101;
					break;
				case JAVACALL_SENSOR_BATTERY_CHARGE:
						retValue = (int)GetTickCount()%2;
					break;
			}
            data[0].intValue = retValue;
		} else { // i3tests
		    if (buffIndex < bufIntLen) {
                data[0].intValue = *(pBufInt + buffIndex++);
		    } else { // buffer overflow
		        res = JAVACALL_FAIL;
			}
		}
	} else { //double
		if (buffIndex < bufDoubleLen) {
               // IMPL_NOTE: TBD
		} else { // buffer overflow
		    res = JAVACALL_FAIL;
		}
	}

    return res;
}


javacall_result javacall_sensor_is_available(javacall_sensor_type sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

// context structure
typedef struct {
    javacall_sensor_type sensor;
	javacall_bool    isOpened;
} sensor_context_t;

static sensor_context_t sensor_context;

static void notify_open( sensor_context_t* pContext );
static void notify_close( sensor_context_t* pContext );

javacall_result javacall_sensor_open(javacall_sensor_type sensor, void** pContext)
{
	javacall_result returnValue = JAVACALL_FAIL;
	if (*pContext == NULL) { // first call
        *pContext = (void*)&sensor_context;
		sensor_context.sensor = sensor;
	    _beginthread( notify_open, 0, &sensor_context );
        returnValue = JAVACALL_WOULD_BLOCK;
	} else { // reinvocation
		if (((sensor_context_t*)*pContext)->sensor == sensor && ((sensor_context_t*)*pContext)->isOpened) {
            returnValue = JAVACALL_OK;
		}
	}
	return returnValue;
}

// sending event from another thread
static void notify_open( sensor_context_t* pContext ) {
    pContext->isOpened = JAVACALL_TRUE;
	javanotify_sensor_connection_completed(pContext->sensor, JAVACALL_TRUE, 0);
	_endthread();
}

javacall_result javacall_sensor_close(javacall_sensor_type sensor, void** pContext)
{
	javacall_result returnValue = JAVACALL_FAIL;
	if (*pContext == NULL) { // first call
        *pContext = (void*)&sensor_context;
		sensor_context.sensor = sensor;
	    _beginthread( notify_close, 0, &sensor_context );
        returnValue = JAVACALL_WOULD_BLOCK;
	} else { // reinvocation
		if (((sensor_context_t*)*pContext)->sensor == sensor && !((sensor_context_t*)*pContext)->isOpened) {
            returnValue = JAVACALL_OK;
		}
	}

	if (pBufInt != NULL) {
        free(pBufInt);
		pBufInt = NULL;
    }
    if (pBufDouble != NULL) {
        free(pBufDouble);
		pBufDouble = NULL;
    }
	return returnValue;
}

// sending event from another thread
static void notify_close( sensor_context_t* pContext ) {
    pContext->isOpened = JAVACALL_FALSE;
	javanotify_sensor_connection_completed(pContext->sensor, JAVACALL_TRUE, 0);
	_endthread();
}

javacall_result javacall_sensor_start_measuring_data(javacall_sensor_type sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

javacall_result javacall_sensor_stop_measuring_data(javacall_sensor_type sensor)
{
     (void)sensor;
     return JAVACALL_OK;
}

javacall_result javanotify_sensor_channel_data_available(javacall_sensor_type sensor, int channel, int errCode)
{
     (void)sensor;
     (void)channel;
     (void)errCode;
     return JAVACALL_OK;
}

javacall_result javacall_sensor_start_monitor_availability(javacall_sensor_type sensor)
{
     (void)sensor;
     return JAVACALL_OK;
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
	if (pBufInt != NULL) {
        free(pBufInt);
	}
    bufIntLen = length;
	pBufInt = (int*)malloc(bufIntLen * sizeof(int));
    buffIndex = 0;
	isInt = 1;
    return pBufInt;
}

// reserve the buffer of double array and return its pointer
double* javacall_sensor_emulator_set_double_buffer(int length) {
	if (pBufDouble != NULL) {
        free(pBufDouble);
	}
    bufDoubleLen = length;
	pBufDouble = (double*)malloc(bufDoubleLen * sizeof(double));
    buffIndex = 0;
	isInt = 0;
    return pBufDouble;
}

