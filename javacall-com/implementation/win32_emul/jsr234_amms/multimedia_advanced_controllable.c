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

#include "javacall_multimedia_advanced.h"
#include "multimedia_advanced_controllable.h"

javacall_amms_control_t* controllable_get_control (
    controllable_t *controllable,
    javacall_amms_control_type_enum_t type )
{
    int i = 0;
    if( controllable == NULL )
    {
        return NULL;
    }
    if( controllable->controls == NULL || controllable->number_of_controls < 1 )
    {
        return NULL;
    }

    for( i = 0; i < controllable->number_of_controls; i++ )
    {
        if( type == controllable->controls[i].type )
        {
            return &controllable->controls[i];
        }
    }

    return NULL;
}

javacall_amms_control_t* controllable_get_controls (
    controllable_t *controllable,
    /*OUT*/ int *controls_number )
{
    if( controllable == NULL )
    {
        return NULL;
    }

    *controls_number = controllable->number_of_controls;
    return controllable->controls;
}
