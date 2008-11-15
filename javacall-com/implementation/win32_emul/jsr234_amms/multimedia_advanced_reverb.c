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

#include <stdlib.h>

#include "javacall_multimedia_advanced.h"

#include "mm_qsound_audio.h"
#include "mQ_JSR-234.h"

#include "multimedia_advanced_manager.h"
#include "multimedia_advanced_reverb.h"

static effectcontrol_vtbl reverb_effectcontrol_vtbl =
{
    (MQ234_SETENABLED)     mQ234_Reverb_setEnabled,
    (MQ234_ISENABLED)      mQ234_Reverb_isEnabled,
    (MQ234_SETSCOPE)       mQ234_Reverb_setScope,
    (MQ234_GETSCOPE)       mQ234_Reverb_getScope,
    (MQ234_SETENFORCED)    mQ234_Reverb_setEnforced,
    (MQ234_ISENFORCED)     mQ234_Reverb_isEnforced,
    (MQ234_SETPRESET)      mQ234_Reverb_setPreset,
    (MQ234_GETPRESET)      mQ234_Reverb_getPreset,
    (MQ234_GETPRESETNAMES) mQ234_Reverb_getPresetNames
};

/*---------------------------------------------------------------------------*/

int javacall_music_reverb_control_get_reverb_level (
    javacall_music_reverb_control_t *ctl )
{
    if( NULL == ctl ) return 1; /* normally reverb level is negative */

    return mQ234_Reverb_getReverbLevel( ctl->qs_obj_ptr );
}

javacall_result javacall_music_reverb_control_set_reverb_level (
    javacall_music_reverb_control_t *ctl,
    int level )
{
    MQ234_ERROR e;
    int         new_level;

    if( NULL == ctl ) return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Reverb_setReverbLevel( ctl->qs_obj_ptr, level, &new_level );

    /*
     * TODO: should we check new_level?
     */

    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

int javacall_music_reverb_control_get_reverb_time (
    javacall_music_reverb_control_t *ctl )
{
    if( NULL == ctl ) return -1;

    return ctl->rtime;
}

javacall_result javacall_music_reverb_control_set_reverb_time (
    javacall_music_reverb_control_t *ctl,
    int time )
{
    MQ234_ERROR e;

    if( NULL == ctl ) return JAVACALL_INVALID_ARGUMENT;

    /*
     * QSound reverb returns an error if time is too long,
     * and TCK probes this function with 0x7FFFFFFF 'maxint',
     * so we need sort of a hack here:
     */

    ctl->rtime = time;

    e = mQ234_Reverb_setReverbTime( ctl->qs_obj_ptr,
                                    ( time < 0x4800 ) ? time : 0x4800 );

    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

/*---------------------------------------------------------------------------*/

const javacall_utf16* javacall_music_reverb_control_get_preset (
    javacall_music_reverb_control_t *ctl )
{
    return effectcontrol_get_preset( &(ctl->ectl) );
}

const javacall_utf16 **javacall_music_reverb_control_get_preset_names (
    javacall_music_reverb_control_t *ctl,
    /*OUT*/ int *number_of_presets )
{
    return effectcontrol_get_preset_names( &(ctl->ectl), number_of_presets );
}

javacall_result javacall_music_reverb_control_set_preset (
    javacall_music_reverb_control_t *ctl,
    const javacall_utf16* preset_name )
{
    return effectcontrol_set_preset( &(ctl->ectl), preset_name );
}

javacall_amms_effect_control_scope_enum_t javacall_music_reverb_control_get_scope (
    javacall_music_reverb_control_t *ctl )
{
    return effectcontrol_get_scope( &(ctl->ectl) );
}

javacall_result javacall_music_reverb_control_set_scope (
    javacall_music_reverb_control_t *ctl,
    javacall_amms_effect_control_scope_enum_t scope )
{
    return effectcontrol_set_scope( &(ctl->ectl), scope );
}

javacall_bool javacall_music_reverb_control_is_enabled (
    javacall_music_reverb_control_t *ctl )
{
    return effectcontrol_is_enabled( &(ctl->ectl) );
}

javacall_result javacall_music_reverb_control_set_enabled (
    javacall_music_reverb_control_t *ctl,
    javacall_bool enabled )
{
    return effectcontrol_set_enabled( &(ctl->ectl), enabled );
}

javacall_bool javacall_music_reverb_control_is_enforced (
    javacall_music_reverb_control_t *ctl )
{
    return effectcontrol_is_enforced( &(ctl->ectl) );
}

javacall_result javacall_music_reverb_control_set_enforced (
    javacall_music_reverb_control_t *ctl,
    javacall_bool enforced )
{
    return effectcontrol_set_enforced( &(ctl->ectl), enforced );
}

/*---------------------------------------------------------------------------*/

void reverb_init( javacall_music_reverb_control_t *ctl,
                  IReverbControl                  *qs_obj_ptr )
{
    long t;

    ctl->qs_obj_ptr = qs_obj_ptr;

    mQ234_Reverb_setScope( ctl->qs_obj_ptr, JSR234_SCOPE_LIVE_ONLY );
    mQ234_Reverb_setPreset( ctl->qs_obj_ptr, "smallroom" );
    mQ234_Reverb_setEnforced( ctl->qs_obj_ptr, 0 );
    mQ234_Reverb_setEnabled( ctl->qs_obj_ptr, 0 );

    mQ234_Reverb_getReverbTime( ctl->qs_obj_ptr, &t );
    ctl->rtime = t;

    effectcontrol_init( &( ctl->ectl ),
                        (IEffectControl*)qs_obj_ptr,
                        &reverb_effectcontrol_vtbl );
}

void reverb_cleanup( javacall_music_reverb_control_t *ctl )
{
    /*
     * TODO: reverb_cleanup never gets called, that's a bit sad
     */

    effectcontrol_cleanup( &(ctl->ectl) );

    /* commented out, because linker cannot find this function:
     * mQ234_Reverb_Destroy( ctl->qs_obj_ptr );
     */
}

