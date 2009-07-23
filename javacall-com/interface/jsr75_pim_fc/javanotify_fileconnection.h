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

#ifndef __JAVANOTIFY_JSR75_FC_H_
#define __JAVANOTIFY_JSR75_FC_H_

/**
 * @file javanotify_fileconnection.h
 * @ingroup JSR75
 * @brief Notification functions for JSR-75 FileConnection
 */

#ifdef __cplusplus
extern "C" {
#endif


/******************************************************************************
 ******************************************************************************
 ******************************************************************************

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -  
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurence of the
  underlying event.
  The functions need to be executed in platform's task/thread

 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/

/** 
 * @defgroup NotificationFileConnection Notification API for JSR-75 FC
 * @ingroup JSR75
 * @{
 */ 

/* Event queue ID for FileConnection. */
#define JSR75_EVENT_QUEUE_ID 75

/**
 * A callback function to be called by the platfrom in order to notify
 * about changes in the available file system roots (new root was added/
 * a root was removed).
 */
void javanotify_fileconnection_root_changed();

/** @} */

#ifdef __cplusplus
}
#endif

#endif 


