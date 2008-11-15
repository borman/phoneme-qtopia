/*
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

#ifndef __MULTIMEDIA_ADVANCED_CONTROLLABLE_H
#define __MULTIMEDIA_ADVANCED_CONTROLLABLE_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct
{
    javacall_amms_control_t * controls;
    int                       number_of_controls;
} controllable_t;

javacall_amms_control_t* controllable_get_control (
    controllable_t *controllable,
    javacall_amms_control_type_enum_t type );

javacall_amms_control_t* controllable_get_controls (
    controllable_t *controllable,
    /*OUT*/ int *controls_number );

#ifdef __cplusplus
}
#endif

#endif /*__MULTIMEDIA_ADVANCED_CONTROLLABLE_H*/
