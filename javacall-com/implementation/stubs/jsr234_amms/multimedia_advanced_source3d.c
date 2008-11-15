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


javacall_result javacall_audio3d_location_control_set_cartesian (
    javacall_audio3d_location_control_t *location_control,
                     int x,
                     int y,
                     int z)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_location_control_set_spherical (
    javacall_audio3d_location_control_t *location_control,
                     int azimuth,
                     int elevation,
                     int radius)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_location_control_get_cartesian (
    javacall_audio3d_location_control_t *location_control,
    /*OUT*/int coord[3])
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_distance_attenuation_control_set_parameters (
    javacall_audio3d_distance_attenuation_control_t *dac,
                      int minDistance,
                      int maxDistance,
                      javacall_bool isMuteAfterMax,
                      int rolloffFactor)
{
    return JAVACALL_NOT_IMPLEMENTED;
}


int javacall_audio3d_distance_attenuation_control_get_min_distance (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    return -1;
}

int javacall_audio3d_distance_attenuation_control_get_max_distance (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    return -1;
}

javacall_bool javacall_audio3d_distance_attenuation_control_is_mute_after_max (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    return JAVACALL_FALSE;
}

int javacall_audio3d_distance_attenuation_control_get_rolloff_factor (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    return -1;
}

javacall_result javacall_audio3d_soundsource3d_add_player ( 
    javacall_audio3d_soundsource3d_t *module, 
    javacall_handle handle )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_soundsource3d_remove_player ( 
    javacall_audio3d_soundsource3d_t *module, 
    javacall_handle handle )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_soundsource3d_add_midi_channel ( 
    javacall_audio3d_soundsource3d_t *module, 
    javacall_handle handle, int channel )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result javacall_audio3d_soundsource3d_remove_midi_channel ( 
    javacall_audio3d_soundsource3d_t *module, 
    javacall_handle handle, int channel )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_amms_control_t* javacall_audio3d_soundsource3d_get_control (
    javacall_audio3d_soundsource3d_t *source,
    javacall_amms_control_type_enum_t type )
{
    return NULL;
}

javacall_amms_control_t* javacall_audio3d_soundsource3d_get_controls (
    javacall_audio3d_soundsource3d_t *source,
    /*OUT*/ int *controls_number )
{
    return NULL;
}


