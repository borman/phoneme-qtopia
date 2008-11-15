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
int javacall_music_reverb_control_get_reverb_level (
    javacall_music_reverb_control_t *reverb_control )
{
    return  1; /* normally reverb level is negative */ 
}

javacall_result javacall_music_reverb_control_set_reverb_level (
    javacall_music_reverb_control_t *reverb_control,
    int level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

int javacall_music_reverb_control_get_reverb_time (
    javacall_music_reverb_control_t *reverb_control )
{
    return -1;
}

javacall_result javacall_music_reverb_control_set_reverb_time (
    javacall_music_reverb_control_t *reverb_control,
    int time )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

const javacall_utf16* javacall_music_reverb_control_get_preset (
    javacall_music_reverb_control_t *reverb )
{
    return NULL;
}

const javacall_utf16 **javacall_music_reverb_control_get_preset_names (
    javacall_music_reverb_control_t *reverb,
    /*OUT*/ int *number_of_presets )
{
    return NULL;
}


javacall_result javacall_music_reverb_control_set_preset (
    javacall_music_reverb_control_t *reverb,
    const javacall_utf16* preset_name )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_amms_effect_control_scope_enum_t javacall_music_reverb_control_get_scope (
    javacall_music_reverb_control_t *reverb )
{
    return 0;
}


javacall_result javacall_music_reverb_control_set_scope (
    javacall_music_reverb_control_t *reverb,
    javacall_amms_effect_control_scope_enum_t scope )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_bool javacall_music_reverb_control_is_enabled (
    javacall_music_reverb_control_t *reverb )
{
    return JAVACALL_FALSE;
}

javacall_result javacall_music_reverb_control_set_enabled (
    javacall_music_reverb_control_t *reverb,
    javacall_bool enabled )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_bool javacall_music_reverb_control_is_enforced (
    javacall_music_reverb_control_t *reverb )
{
    return JAVACALL_FALSE;
}

javacall_result javacall_music_reverb_control_set_enforced (
    javacall_music_reverb_control_t *reverb,
    javacall_bool enforced )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

