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

#ifndef __JAVANOTIFY_LOCATION_H
#define __JAVANOTIFY_LOCATION_H

#ifdef __cplusplus
extern "C" {
#endif

    
#include "javacall_defs.h" 
#include "javacall_location.h" 

/*****************************************************************************
 *****************************************************************************
 *****************************************************************************

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -  
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurrence of the
  undelying event.
  The functions need to be executed in platform's task/thread

 *****************************************************************************
 *****************************************************************************
 *****************************************************************************/
    
/**
 * @defgroup MiscNotification_location Notification API for Location
 * @ingroup Location
 * @{
 */

/* Event queue ID for Location API. */
#define JSR179_EVENT_QUEUE_ID 179

/**
 * @enum javacall_location_callback_type
 * @brief Location callback event type
 */
typedef enum {
    /** Provider opened event */
    JAVACALL_EVENT_LOCATION_OPEN_COMPLETED,
    /** Orientation acquired event*/
    JAVACALL_EVENT_LOCATION_ORIENTATION_COMPLETED,
    /** Location updated event */
    JAVACALL_EVENT_LOCATION_UPDATE_ONCE
} javacall_location_callback_type;
    
/**
 * A callback function to be called for notification of non-blocking 
 * location related events.
 * The platform will invoke the call back in platform context for
 * each provider related occurrence. 
 *
 * @param event type of indication: Either
 *          - JAVACALL_EVENT_LOCATION_OPEN_COMPLETED
 *          - JAVACALL_EVENT_LOCATION_ORIENTATION_COMPLETED
 *          - JAVACALL_EVENT_LOCATION_UPDATE_ONCE
 * @param provider handle of provider related to the notification
 * @param operation_result operation result: Either
 *      - JAVACALL_OK if operation completed successfully, 
 *      - JAVACALL_LOCATION_RESULT_CANCELED if operation is canceled 
 *      - JAVACALL_LOCATION_RESULT_TIMEOUT  if operation is timeout 
 *      - JAVACALL_LOCATION_RESULT_OUT_OF_SERVICE if provider is out of service
 *      - JAVACALL_LOCATION_RESULT_TEMPORARILY_UNAVAILABLE if provider is 
 *                                                      temporarily unavailable
 *      - otherwise, JAVACALL_FAIL
 */
void javanotify_location_event(
        javacall_location_callback_type event,
        javacall_handle provider,
        javacall_location_result operation_result);


/**
 * A callback function to be called for notification
 * of proximity monitoring updates.
 *
 * This function will be called only once when the terminal enters
 * the proximity of the registered coordinate. 
 *
 * @param provider handle of provider related to the notification
 * @param latitude of registered coordinate.
 * @param longitude of registered coordinate.
 * @param proximityRadius of registered coordinate.
 * @param pLocationInfo location info
 * @param operation_result operation result: Either
 *      - JAVACALL_OK if operation completed successfully, 
 *      - JAVACALL_LOCATION_RESULT_CANCELED if operation is canceled 
 *      - JAVACALL_LOCATION_RESULT_OUT_OF_SERVICE if provider
 *                                                is out of service
 *      - JAVACALL_LOCATION_RESULT_TEMPORARILY_UNAVAILABLE if provider
 *                                                is temporarily unavailable
 *      - otherwise, JAVACALL_FAIL
 */
void /*OPTIONAL*/javanotify_location_proximity(
        javacall_handle provider,
        double latitude,
        double longitude,
        float proximityRadius,
        javacall_location_location* pLocationInfo,
        javacall_location_result operation_result);

/** @} */
    

#ifdef __cplusplus
}
#endif

#endif /* __JAVANOTIFY_LOCATION_H */


