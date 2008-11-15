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

/* 
 * function javacall_audio3d_get_spectator() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
const javacall_media_format_type*
    javacall_audio3d_get_supported_soundsource3d_player_types( 
        /*OUT*/ int *number_of_types )
{
    return NULL;
}

/* 
 * function javacall_amms_create_local_manager() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
javacall_result javacall_amms_create_local_manager( 
                   /*OUT*/ javacall_amms_local_manager_t** manager )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/* 
 * function javacall_amms_destroy_local_manager() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
javacall_result javacall_amms_destroy_local_manager(
                        javacall_amms_local_manager_t* manager )
{
    return JAVACALL_NOT_IMPLEMENTED;
}


javacall_amms_control_t* javacall_amms_local_manager_get_control (
    javacall_amms_local_manager_t *manager,
    javacall_amms_control_type_enum_t type )
{
    return NULL;
}

javacall_amms_control_t* javacall_amms_local_manager_get_controls (
    javacall_amms_local_manager_t *manager,
    /*OUT*/ int *controls_number )
{
    return NULL;
}


/* 
 * function javacall_amms_local_manager_create_sound_source3d() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
javacall_result javacall_amms_local_manager_create_sound_source3d(
                    javacall_amms_local_manager_t* manager,
                    /*OUT*/ javacall_audio3d_soundsource3d_t** source )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/* 
 * function javacall_amms_local_manager_destroy_sound_source3d() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
javacall_result javacall_amms_local_manager_destroy_sound_source3d( 
                    javacall_amms_local_manager_t* manager,
                    javacall_audio3d_soundsource3d_t* source )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/* 
 * function javacall_amms_local_manager_get_spectator() 
 * for details see declaration in javacall_multimedia_advanced.h  
 */
javacall_audio3d_spectator_t* javacall_amms_local_manager_get_spectator(
                    javacall_amms_local_manager_t* manager )
{
    return NULL;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_volume_control_set_mute(
        javacall_amms_volume_control_t* vctl,
        javacall_bool                   mute)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_bool
    javacall_amms_volume_control_is_muted(
        javacall_amms_volume_control_t* vctl )
{
    return JAVACALL_FALSE;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_volume_control_set_level(
        javacall_amms_volume_control_t* vctl,
        int                             level,
        /*OUT*/int*                     new_level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_volume_control_get_level(
        javacall_amms_volume_control_t* vctl )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_equalizer_control_set_enforced(
        javacall_amms_equalizer_control_t* equalizer,
        javacall_bool                      enforced )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_bool
    javacall_amms_equalizer_control_is_enforced(
        javacall_amms_equalizer_control_t* equalizer )
{
    return JAVACALL_FALSE;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_equalizer_control_set_enabled(
        javacall_amms_equalizer_control_t* equalizer,
        javacall_bool                      enabled )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_bool
    javacall_amms_equalizer_control_is_enabled (
        javacall_amms_equalizer_control_t* equalizer )
{
    return JAVACALL_FALSE;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_equalizer_control_set_scope (
        javacall_amms_equalizer_control_t*        equalizer,
        javacall_amms_effect_control_scope_enum_t scope )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_amms_effect_control_scope_enum_t 
    javacall_amms_equalizer_control_get_scope (
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result
    javacall_amms_equalizer_control_set_preset (
        javacall_amms_equalizer_control_t* equalizer,
        const javacall_utf16*              preset_name )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
const javacall_utf16**
    javacall_amms_equalizer_control_get_preset_names (
        javacall_amms_equalizer_control_t*  equalizer,
        /*OUT*/ int*                        number_of_presets )
{
    return NULL;
}

/**
 * See javacall_multimedia_advanced.h
 */
const javacall_utf16*
    javacall_amms_equalizer_control_get_preset (
        javacall_amms_equalizer_control_t* equalizer )
{
    return NULL;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_min_band_level (
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_max_band_level (
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result 
    javacall_amms_equalizer_control_set_band_level (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        int                                level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result 
    javacall_amms_equalizer_control_get_band_level (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        /*OUT*/ int*                       level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_number_of_bands (
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result 
    javacall_amms_equalizer_control_get_center_freq (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        /*OUT*/ int*                       freq )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_band (
        javacall_amms_equalizer_control_t* equalizer,
        int                                freq )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result 
    javacall_amms_equalizer_control_set_bass (
        javacall_amms_equalizer_control_t* equalizer,
        int                                level,
        /*OUT*/int*                        new_level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
javacall_result 
    javacall_amms_equalizer_control_set_treble (
        javacall_amms_equalizer_control_t* equalizer,
        int                                level,
        /*OUT*/int*                        new_level )
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_bass(
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}

/**
 * See javacall_multimedia_advanced.h
 */
int javacall_amms_equalizer_control_get_treble (
        javacall_amms_equalizer_control_t* equalizer )
{
    return -1;
}


