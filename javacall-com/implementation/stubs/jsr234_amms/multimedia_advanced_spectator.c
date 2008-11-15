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

#include "javacall_multimedia.h"
#include "javacall_multimedia_advanced.h"

javacall_amms_control_t* javacall_audio3d_spectator_get_control (
    javacall_audio3d_spectator_t *manager,
    javacall_amms_control_type_enum_t type )
{
    return NULL;
}

javacall_amms_control_t* javacall_audio3d_spectator_get_controls (
    javacall_audio3d_spectator_t *manager,
    /*OUT*/ int *controls_number )
{
    return NULL;
}


javacall_result javacall_audio3d_orientation_control_set_orientation (
        javacall_audio3d_orientation_control_t *orientation_control,
                       int heading,
                       int pitch,
                       int roll )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_orientation_control_set_orientation_vec (
        javacall_audio3d_orientation_control_t *orientation_control,
                       const int frontVector[3],
                       const int aboveVector[3])
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_orientation_control_get_orientation_vectors (
    javacall_audio3d_orientation_control_t *orientation_control,
                       /*OUT*/int vectors[6])
{
    return JAVACALL_NOT_IMPLEMENTED;
}
