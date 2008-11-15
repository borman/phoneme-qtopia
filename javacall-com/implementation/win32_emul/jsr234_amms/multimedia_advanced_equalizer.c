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
#include "multimedia_advanced_equalizer.h"

static effectcontrol_vtbl equalizer_effectcontrol_vtbl =
{
    (MQ234_SETENABLED)     mQ234_Equalizer_setEnabled,
    (MQ234_ISENABLED)      mQ234_Equalizer_isEnabled,
    (MQ234_SETSCOPE)       mQ234_Equalizer_setScope,
    (MQ234_GETSCOPE)       mQ234_Equalizer_getScope,
    (MQ234_SETENFORCED)    mQ234_Equalizer_setEnforced,
    (MQ234_ISENFORCED)     mQ234_Equalizer_isEnforced,
    (MQ234_SETPRESET)      mQ234_Equalizer_setPreset,
    (MQ234_GETPRESET)      mQ234_Equalizer_getPreset,
    (MQ234_GETPRESETNAMES) mQ234_Equalizer_getPresetNames
};

/*---------------------------------------------------------------------------*/

int javacall_amms_equalizer_control_get_min_band_level (
        javacall_amms_equalizer_control_t* eq )
{
    //printf( "         EQ: %08X/%08X : get_min_band_level\n", eq, eq->qs_obj_ptr );
    return mQ234_Equalizer_getMinBandLevel( eq->qs_obj_ptr );
}

int javacall_amms_equalizer_control_get_max_band_level (
        javacall_amms_equalizer_control_t* eq )
{
    //printf( "         EQ: %08X/%08X : get_max_band_level\n", eq, eq->qs_obj_ptr );
    return mQ234_Equalizer_getMaxBandLevel( eq->qs_obj_ptr );
}

javacall_result
    javacall_amms_equalizer_control_set_band_level (
        javacall_amms_equalizer_control_t* eq,
        int                                band,
        int                                level )
{
    MQ234_ERROR e;
    //printf( "         EQ: %08X/%08X : set_band_level\n", eq, eq->qs_obj_ptr );

    if( band < 0
        || band > mQ234_Equalizer_getNumberOfBands( eq->qs_obj_ptr ) - 1
        || level < mQ234_Equalizer_getMinBandLevel( eq->qs_obj_ptr )
        || level > mQ234_Equalizer_getMaxBandLevel( eq->qs_obj_ptr ) )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }

    e = mQ234_Equalizer_setBandLevel( eq->qs_obj_ptr, level, band );
    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

javacall_result
    javacall_amms_equalizer_control_get_band_level (
        javacall_amms_equalizer_control_t* eq,
        int                                band,
        /*OUT*/ int*                       level )
{
    MQ234_ERROR e;
    //printf( "         EQ: %08X/%08X : get_band_level\n", eq, eq->qs_obj_ptr );

    if( band < 0 || band > mQ234_Equalizer_getNumberOfBands( eq->qs_obj_ptr ) - 1 )
        return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Equalizer_getBandLevel( eq->qs_obj_ptr, band, level );
    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

int javacall_amms_equalizer_control_get_number_of_bands (
        javacall_amms_equalizer_control_t* eq )
{
    //printf( "         EQ: %08X/%08X : get_number_of_bands\n", eq, eq->qs_obj_ptr );
    return mQ234_Equalizer_getNumberOfBands( eq->qs_obj_ptr );
}

javacall_result
    javacall_amms_equalizer_control_get_center_freq (
        javacall_amms_equalizer_control_t* eq,
        int                                band,
        /*OUT*/ int*                       freq )
{
    MQ234_ERROR e;
    //printf( "         EQ: %08X/%08X : get_center_freq\n", eq, eq->qs_obj_ptr );

    if( band < 0 || band > mQ234_Equalizer_getNumberOfBands( eq->qs_obj_ptr ) - 1 )
        return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Equalizer_getCenterFreq( eq->qs_obj_ptr, band, freq );
    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

int javacall_amms_equalizer_control_get_band (
        javacall_amms_equalizer_control_t* eq,
        int                                freq )
{
    //printf( "         EQ: %08X/%08X : get_bandl\n", eq, eq->qs_obj_ptr );
    return mQ234_Equalizer_getBand( eq->qs_obj_ptr, freq );
}

javacall_result
    javacall_amms_equalizer_control_set_bass (
        javacall_amms_equalizer_control_t* eq,
        int                                level,
        /*OUT*/int*                        new_level )
{
    MQ234_ERROR e;
    //printf( "         EQ: %08X/%08X : set_bass\n", eq, eq->qs_obj_ptr );

    if( level < 0 || level > 100 )
        return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Equalizer_setBass( eq->qs_obj_ptr, level );
    *new_level = mQ234_Equalizer_getBass( eq->qs_obj_ptr );
    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

javacall_result
    javacall_amms_equalizer_control_set_treble (
        javacall_amms_equalizer_control_t* eq,
        int                                level,
        /*OUT*/int*                        new_level )
{
    MQ234_ERROR e;
    //printf( "         EQ: %08X/%08X : set_treble\n", eq, eq->qs_obj_ptr );

    if( level < 0 || level > 100 )
        return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Equalizer_setTreble( eq->qs_obj_ptr, level );
    *new_level = mQ234_Equalizer_getTreble( eq->qs_obj_ptr );
    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

int javacall_amms_equalizer_control_get_bass(
        javacall_amms_equalizer_control_t* eq )
{
    //printf( "         EQ: %08X/%08X/%08X : get_bass\n", eq, eq->qs_obj_ptr, eq->ectl.qs_obj_ptr );
    return mQ234_Equalizer_getBass( eq->qs_obj_ptr );
}

int javacall_amms_equalizer_control_get_treble (
        javacall_amms_equalizer_control_t* eq )
{
    //printf( "         EQ: %08X/%08X : get_treble\n", eq, eq->qs_obj_ptr );
    return mQ234_Equalizer_getTreble( eq->qs_obj_ptr );
}

/*---------------------------------------------------------------------------*/

const javacall_utf16* javacall_amms_equalizer_control_get_preset (
    javacall_amms_equalizer_control_t *eq )
{
    //printf( "         EQ: %08X/%08X : get_preset\n", eq, eq->qs_obj_ptr );
    return effectcontrol_get_preset( &(eq->ectl) );
}

const javacall_utf16 **javacall_amms_equalizer_control_get_preset_names (
    javacall_amms_equalizer_control_t *eq,
    /*OUT*/ int *number_of_presets )
{
    //printf( "         EQ: %08X/%08X : get_preset_names\n", eq, eq->qs_obj_ptr );
    return effectcontrol_get_preset_names( &(eq->ectl), number_of_presets );
}

javacall_result javacall_amms_equalizer_control_set_preset (
    javacall_amms_equalizer_control_t *eq,
    const javacall_utf16* preset_name )
{
    //printf( "         EQ: %08X/%08X : set_preset\n", eq, eq->qs_obj_ptr );
    return effectcontrol_set_preset( &(eq->ectl), preset_name );
}

javacall_amms_effect_control_scope_enum_t javacall_amms_equalizer_control_get_scope (
    javacall_amms_equalizer_control_t *eq )
{
    //printf( "         EQ: %08X/%08X : get_scope\n", eq, eq->qs_obj_ptr );
    return effectcontrol_get_scope( &(eq->ectl) );
}

javacall_result javacall_amms_equalizer_control_set_scope (
    javacall_amms_equalizer_control_t *eq,
    javacall_amms_effect_control_scope_enum_t scope )
{
    //printf( "         EQ: %08X/%08X : set_scope\n", eq, eq->qs_obj_ptr );
    return effectcontrol_set_scope( &(eq->ectl), scope );
}

javacall_bool javacall_amms_equalizer_control_is_enabled (
    javacall_amms_equalizer_control_t *eq )
{
    //printf( "         EQ: %08X/%08X : is_enabled\n", eq, eq->qs_obj_ptr );
    return effectcontrol_is_enabled( &(eq->ectl) );
}

javacall_result javacall_amms_equalizer_control_set_enabled (
    javacall_amms_equalizer_control_t *eq,
    javacall_bool enabled )
{
    //printf( "         EQ: %08X/%08X : set_enabled\n", eq, eq->qs_obj_ptr );
    return effectcontrol_set_enabled( &(eq->ectl), enabled );
}

javacall_bool javacall_amms_equalizer_control_is_enforced (
    javacall_amms_equalizer_control_t *eq )
{
    //printf( "         EQ: %08X/%08X : is_enforced\n", eq, eq->qs_obj_ptr );
    return effectcontrol_is_enforced( &(eq->ectl) );
}

javacall_result javacall_amms_equalizer_control_set_enforced (
    javacall_amms_equalizer_control_t *eq,
    javacall_bool enforced )
{
    //printf( "         EQ: %08X/%08X : set_enforced\n", eq, eq->qs_obj_ptr );
    return effectcontrol_set_enforced( &(eq->ectl), enforced );
}

/*---------------------------------------------------------------------------*/

void equalizer_init( javacall_amms_equalizer_control_t *eq,
                     IEqualizerControl                 *qs_obj_ptr )
{
    eq->qs_obj_ptr = qs_obj_ptr;

    //printf( "         EQ: %08X/%08X : init\n", eq, eq->qs_obj_ptr );

    mQ234_Equalizer_setScope( eq->qs_obj_ptr, JSR234_SCOPE_LIVE_ONLY );
    mQ234_Equalizer_setEnforced( eq->qs_obj_ptr, 0 );
    mQ234_Equalizer_setEnabled( eq->qs_obj_ptr, 0 );

    effectcontrol_init( &( eq->ectl ),
                        (IEffectControl*)qs_obj_ptr,
                        &equalizer_effectcontrol_vtbl );
}

void equalizer_cleanup( javacall_amms_equalizer_control_t *eq )
{
    /*
     * TODO: reverb_cleanup never gets called, that's a bit sad
     */

    //printf( "         EQ: %08X/%08X : cleanup\n", eq, eq->qs_obj_ptr );

    effectcontrol_cleanup( &(eq->ectl) );

    /* commented out, because linker cannot find this function:
     * mQ234_Equalizer_Destroy( ctl->qs_obj_ptr );
     */
}

