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
#include <limits.h>

#include "javacall_multimedia_advanced.h"

#include "mm_qsound_audio.h"
#include "mQ_JSR-234.h"

#include "multimedia_advanced_controllable.h"
#include "multimedia_advanced_controls.h"
#include "multimedia_advanced_manager.h"

#include "multimedia.h"

/*---------------------------------------------------------------------------*/

struct tag_javacall_audio3d_soundsource3d
{
    ISoundSource3D*                                 qs_obj_ptr;
    javacall_audio3d_location_control_t             location;
    javacall_audio3d_distance_attenuation_control_t dist_att;
    javacall_amms_control_t                         controls[ 2 ];
    controllable_t                                  controllable;
};

/*---------------------------------------------------------------------------*/


javacall_result javacall_audio3d_soundsource3d_add_player (
    javacall_audio3d_soundsource3d_t *module,
    javacall_handle handle )
{
    ISoundSource3D*     pSndSrc = module->qs_obj_ptr;
    ah*                 h;
    IWaveStream*        pPlayer;
    MQ234_ERROR         e;
    javacall_media_format_type mType;
    
    if (javacall_media_get_format(handle, &mType) != JAVACALL_OK) {
        return JAVACALL_FAIL;
    }

    if (0 != strcmp(mType, JAVACALL_MEDIA_FORMAT_MS_PCM) 
        && 0 != strcmp(mType, JAVACALL_MEDIA_FORMAT_AMR))
	{
		//printf("Not a WAV/AMR player to SS3D!\r\n");
		return JAVACALL_NOT_IMPLEMENTED;
	}
    
    h = (ah*)( ((javacall_impl_player*)handle)->mediaHandle );
    pPlayer = h->wav.stream;
    
    if ((pPlayer == NULL) || (h->hdr.gmIdx==0))
	{
		//printf("Non-prefetched player to SS3D!\r\n");
		return JAVACALL_FAIL;
	}
	/*
     * first remove player from global effect module, otherwize
     * QSound will fetch player data twice, doubling rate and
     * introducing clicks
     */


    e = mQ234_EffectModule_removePlayer( h->wav.em, pPlayer );

    if( MQ234_ERROR_NO_ERROR != e )
    {
        //printf( "Cannot remove player %x from EM135!\n", handle );
        return JAVACALL_FAIL;
    }

    h->wav.em = NULL;

    h->hdr.controls[ CON135_RATE ]
        = (IControl*)mQ234_SoundSource3D_getRateControl( pSndSrc );

    h->hdr.controls[ CON135_VOLUME ]
        = (IControl*)mQ234_SoundSource3D_getVolumeControl( pSndSrc );

    /*
     * TODO: do I have to: - somehow check player type? (must be WAVE)
     *                     - check parameters? (must be not-NULL)
     */

    e = mQ234_SoundSource3D_addPlayer( pSndSrc, pPlayer );

    if( MQ234_ERROR_NO_ERROR == e )
    {
        //printf( "The player %x was added to the 3D sound source %x\n",handle, module );
        h->wav.em = pSndSrc;
        return JAVACALL_OK;
    }
    else
    {
        //printf( "mQ234_SoundSource3D_addPlayer( %x, %x ) failed!\n", module, handle );
        return JAVACALL_FAIL;
    }
}

javacall_result javacall_audio3d_soundsource3d_remove_player (
    javacall_audio3d_soundsource3d_t *module,
    javacall_handle handle )
{
    ISoundSource3D*     pSndSrc = module->qs_obj_ptr;
    ah*                 h;
    IWaveStream*        pPlayer;
    MQ234_ERROR         e;

	if (handle == NULL)
		return JAVACALL_OK;
	h = (ah*)( ((javacall_impl_player*)handle)->mediaHandle );
	if (h == NULL)
		return JAVACALL_OK;
	pPlayer = h->wav.stream;
	if (pPlayer==NULL)
		return JAVACALL_OK;

    /*
     * TODO: do I have to: - somehow check player type? (must be WAVE)
     *                     - check parameters? (must be not-NULL)
     */

    e = mQ234_SoundSource3D_removePlayer( pSndSrc, pPlayer );

    /*
     * return player to global SoundEffectModule, or noone will fetch its data
     * and it becomes unaudible
     */

    if( MQ234_ERROR_NO_ERROR == e )
    {
        h->wav.em = NULL;
        //printf( "The player %x was removed from the 3D sound source %x\n",
        //    handle, module );

        e = mQ234_EffectModule_addPlayer( QSOUND_GET_GM( h->hdr.gmIdx ).EM135, pPlayer );

        if( MQ234_ERROR_NO_ERROR == e )
        {
            h->wav.em = QSOUND_GET_GM( h->hdr.gmIdx ).EM135;

            h->hdr.controls[CON135_RATE]
                = (IControl*)mQ234_EffectModule_getRateControl( QSOUND_GET_GM( h->hdr.gmIdx ).EM135 );

            h->hdr.controls[CON135_VOLUME]
                = (IControl*)mQ234_EffectModule_getVolumeControl( QSOUND_GET_GM( h->hdr.gmIdx ).EM135 );
        }
        else
        {
            //printf( "Warning: Player %x cannot be added back to EM135\n", handle );
        }

        return JAVACALL_OK;
    }
    else
    {
        //printf( "mQ234_SoundSource3D_removePlayer( %x, %x ) failed!\n", module, handle );
        return JAVACALL_FAIL;
    }
}

/*---------------------------------------------------------------------------*/

javacall_result javacall_audio3d_soundsource3d_add_midi_channel (
    javacall_audio3d_soundsource3d_t *module,
    javacall_handle handle, int channel )
{
    return JAVACALL_NOT_IMPLEMENTED;

    /* *** maybe we'll support 3d midi later
     *
     *
    ISoundSource3D*     pSndSrc = module->qs_obj_ptr;
    ah*                 h = (ah*)( ((javacall_impl_player*)handle)->mediaHandle );
    IPlayControl*       pPlayer = h->midi.synth;
    MQ234_ERROR         e;

    e = mQ234_SoundSource3D_addMIDIChannel(
        pSndSrc, pPlayer, channel );

    if( MQ234_ERROR_NO_ERROR == e )
    {
        printf( "MIDI cannel %x(%d) was added to the 3D sound source %x\n",
            handle, channel, module );
        return JAVACALL_OK;
    }
    else
    {
        printf( "mQ234_SoundSource3D_addMIDIChannel( %x, %x, %d ) failed!\n",
            module, handle, channel );
        return JAVACALL_FAIL;
    }
    */
}

javacall_result javacall_audio3d_soundsource3d_remove_midi_channel (
    javacall_audio3d_soundsource3d_t *module,
    javacall_handle handle, int channel )
{
    ISoundSource3D*     pSndSrc = module->qs_obj_ptr;
    ah*                 h = (ah*)( ((javacall_impl_player*)handle)->mediaHandle );
    IPlayControl*       pPlayer = h->midi.synth;
    MQ234_ERROR         e;

    e = mQ234_SoundSource3D_removeMIDIChannel(
        pSndSrc, pPlayer, channel );

    if( MQ234_ERROR_NO_ERROR == e )
    {
        //printf( "MIDI cannel %x(%d) removed from 3D sound source %x\n",
        //    handle, channel, module );
        return JAVACALL_OK;
    }
    else
    {
        //printf( "mQ234_SoundSource3D_remove"
        //        "MIDIChannel( %x, %x, %d ) failed!\n",
        //    module, handle, channel );
        return JAVACALL_FAIL;
    }
}

/*---------------------------------------------------------------------------*/

javacall_amms_control_t* javacall_audio3d_soundsource3d_get_control (
    javacall_audio3d_soundsource3d_t *source,
    javacall_amms_control_type_enum_t type )
{
    return controllable_get_control( &source->controllable, type );
}

javacall_amms_control_t* javacall_audio3d_soundsource3d_get_controls (
    javacall_audio3d_soundsource3d_t *source,
    /*OUT*/ int *controls_number )
{
    return controllable_get_controls( &source->controllable, controls_number );
}

/*---------------------------------------------------------------------------*/

javacall_result createSoundSource3D(
                    /*OUT*/ javacall_audio3d_soundsource3d_t** source,
                        javacall_amms_local_manager_t *mgr )
{
    MQ234_ERROR                       e;
    javacall_audio3d_soundsource3d_t* ptr;

    if( NULL == source ) return JAVACALL_INVALID_ARGUMENT;

    ptr = malloc( sizeof( javacall_audio3d_soundsource3d_t ) );
    if( NULL == ptr ) return JAVACALL_OUT_OF_MEMORY;
    memset( ptr, 0, sizeof( *ptr ) );

    ptr->qs_obj_ptr
        = mQ234_GlobalManager_createSoundSource3D( QSOUND_GET_GM( mgr->gmIdx ).gm );

    if( NULL == ptr->qs_obj_ptr )
    {
        free( ptr );
        return JAVACALL_FAIL;
    }

    ptr->location.qs_obj_ptr =
        mQ234_SoundSource3D_getLocationControl( ptr->qs_obj_ptr );

    ptr->dist_att.qs_obj_ptr =
        mQ234_SoundSource3D_getDistanceAttenuationControl( ptr->qs_obj_ptr );

    if( NULL == ptr->dist_att.qs_obj_ptr || NULL == ptr->location.qs_obj_ptr )
    {
        mQ234_SoundSource3D_Destroy( ptr->qs_obj_ptr );
        free( ptr );
        return JAVACALL_FAIL;
    }

    mQ234_Location_setCartesian( ptr->location.qs_obj_ptr, 0, 0, 0 );

    e = mQ234_DistanceAttenuation_setParameters(
        ptr->dist_att.qs_obj_ptr, 1000, INT_MAX, JAVACALL_TRUE, 1000 );

    ptr->controls[0].type = javacall_audio3d_eLocationControl;
    ptr->controls[0].ptr = &ptr->location;
    ptr->controls[1].type = javacall_audio3d_eDistanceAttenuationControl;
    ptr->controls[1].ptr = &ptr->dist_att;
    ptr->controllable.controls = ptr->controls;
    ptr->controllable.number_of_controls =
        sizeof( ptr->controls ) / sizeof( ptr->controls[0] );

    *source = ptr;
    return JAVACALL_OK;
}

javacall_result destroySoundSource3D(
                    javacall_audio3d_soundsource3d_t* source )
{
    if( NULL == source ) return JAVACALL_INVALID_ARGUMENT;

    mQ234_SoundSource3D_Destroy( source->qs_obj_ptr );

    free( source );

    return JAVACALL_OK;
}
