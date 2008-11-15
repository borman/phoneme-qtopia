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

#include "multimedia_advanced_reverb.h"
#include "multimedia_advanced_manager.h"

extern void initSpectatorImpl( javacall_audio3d_spectator_t *spectator,
                        javacall_amms_local_manager_t *mgr );

extern javacall_result createSoundSource3D(
                    /*OUT*/ javacall_audio3d_soundsource3d_t** source,
                            javacall_amms_local_manager_t *mgr );

extern javacall_result destroySoundSource3D(
                    javacall_audio3d_soundsource3d_t* source );

static const javacall_media_format_type gSupported3DMedia[] = {
    JAVACALL_MEDIA_FORMAT_MS_PCM
#ifdef ENABLE_AMR
    , JAVACALL_MEDIA_FORMAT_AMR
#endif // ENABLE_AMR
};

/*
 * function javacall_audio3d_get_supported_soundsource3d_player_types()
 * for details see declaration in javacall_multimedia_advanced.h
 */
const javacall_media_format_type*
    javacall_audio3d_get_supported_soundsource3d_player_types(
        /*OUT*/ int *number_of_types )
{
    if( number_of_types == NULL )
    {
        return NULL;
    }
    *number_of_types = sizeof gSupported3DMedia / sizeof gSupported3DMedia[0];
    return gSupported3DMedia;
}

/*
 * function javacall_amms_create_local_manager()
 * for details see declaration in javacall_multimedia_advanced.h
 */
javacall_result javacall_amms_create_local_manager(
                   /*OUT*/ javacall_amms_local_manager_t** mgr )
{
    IGlobalManager                *qs_mgr;
    javacall_amms_local_manager_t *ptr;

    if( NULL == mgr ) return JAVACALL_INVALID_ARGUMENT;

	ptr = malloc( sizeof(javacall_amms_local_manager_t) );
    //ptr = malloc( sizeof( *ptr ) );

    if( NULL == ptr ) return JAVACALL_OUT_OF_MEMORY;

	memset( ptr, 0, sizeof( javacall_amms_local_manager_t ) );
    //memset( ptr, 0, sizeof( *ptr ) );

    /*
     *       Global manager access
     *
     * TODO: I'm using IsolateIdToGM( getCurrentIsolateId() ), but
     *       AFAIK, MIDP API shouldn't be used in javacall...
     */

    ptr->gmIdx = isolateIDtoGM( getCurrentIsolateId() );

    qs_mgr = QSOUND_GET_GM( ptr->gmIdx ).gm;

    //printf( "   GM : create gmIdx = %i\n", ptr->gmIdx );

    reverb_init( &ptr->reverb, mQ234_GlobalManager_getReverbControl( qs_mgr ) );
    equalizer_init( &ptr->equalizer, mQ234_GlobalManager_getEqualizerControl( qs_mgr ) );
    ptr->volume.qs_obj_ptr = mQ234_GlobalManager_getVolumeControl( qs_mgr );
    initSpectatorImpl( &ptr->spectator, ptr );

    ptr->controls[0].type = javacall_music_eReverbControl;
    ptr->controls[0].ptr  = &ptr->reverb;

    ptr->controls[1].type = javacall_amms_eVolumeControl;
    ptr->controls[1].ptr  = &ptr->volume;

    ptr->controls[2].type = javacall_amms_eEqualizerControl;
    ptr->controls[2].ptr  = &ptr->equalizer;

    ptr->controllable.controls = ptr->controls;
    ptr->controllable.number_of_controls
        = sizeof( ptr->controls ) / sizeof( ptr->controls[0] );

    *mgr = ptr;

    return JAVACALL_OK;
}

/*
 * function javacall_amms_destroy_local_manager()
 * for details see declaration in javacall_multimedia_advanced.h
 */
javacall_result javacall_amms_destroy_local_manager(
                        javacall_amms_local_manager_t* manager )
{
    if( manager == NULL )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    free( manager );
    //printf( "   GM : destroyed\n" );

    return JAVACALL_OK;
}


javacall_amms_control_t* javacall_amms_local_manager_get_control (
    javacall_amms_local_manager_t *manager,
    javacall_amms_control_type_enum_t type )
{
    return controllable_get_control( &manager->controllable, type );
}

javacall_amms_control_t* javacall_amms_local_manager_get_controls (
    javacall_amms_local_manager_t *manager,
    /*OUT*/ int *controls_number )
{
    return controllable_get_controls( &manager->controllable,
        controls_number );
}


/*
 * function javacall_amms_local_manager_create_sound_source3d()
 * for details see declaration in javacall_multimedia_advanced.h
 */
javacall_result javacall_amms_local_manager_create_sound_source3d(
                    javacall_amms_local_manager_t* manager,
                    /*OUT*/ javacall_audio3d_soundsource3d_t** source )
{
    return createSoundSource3D( source, manager );
}

/*
 * function javacall_amms_local_manager_destroy_sound_source3d()
 * for details see declaration in javacall_multimedia_advanced.h
 */
javacall_result javacall_amms_local_manager_destroy_sound_source3d(
                    javacall_amms_local_manager_t* manager,
                    javacall_audio3d_soundsource3d_t* source )
{
    return destroySoundSource3D( source );
}

/*
 * function javacall_amms_local_manager_get_spectator()
 * for details see declaration in javacall_multimedia_advanced.h
 */
javacall_audio3d_spectator_t* javacall_amms_local_manager_get_spectator(
                    javacall_amms_local_manager_t* manager )
{
    if( NULL == manager ) return NULL;

    return &manager->spectator;
}
