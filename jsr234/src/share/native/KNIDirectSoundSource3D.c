/*
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

#include "kni.h"
#include "javacall_multimedia_advanced.h"
#include "jsr234_control.h"
#include "jsr234_nativePtr.h"
#include "KNICommon.h"

static javacall_audio3d_soundsource3d_t *getNativePtr(KNIDECLARGS int dummy)
{
    return ( javacall_audio3d_soundsource3d_t* )getNativeHandleFromField(KNIPASSARGS
        "_peer" );
}

static javacall_amms_local_manager_t *getNativeMgrPtr(KNIDECLARGS int dummy)
{
    return ( javacall_amms_local_manager_t* )getNativeHandleFromField(KNIPASSARGS
        "_managerPeer" );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_finalize)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    javacall_amms_local_manager_t *mgr = getNativeMgrPtr(KNIPASSARGS 0);

    javacall_amms_local_manager_destroy_sound_source3d( mgr, src );

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nAddMIDIChannel)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)KNI_GetParameterAsInt( 1 );
    javacall_handle player = ( javacall_handle )( pKniInfo->pNativeHandle );
    int channel = KNI_GetParameterAsInt(2);
    javacall_result result = JAVACALL_FAIL;


    result = javacall_audio3d_soundsource3d_add_midi_channel( src, player,
        channel );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nInvalid arguments were passed to native part of \
SoundSource3D.addMIDIChannel\n"
                );
        KNI_ReturnVoid();
    }

    if( result == JAVACALL_NOT_IMPLEMENTED )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException",
                "\nAdding MIDI Channels to SoundSource3D is not supported\n"
            );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred while adding the MIDI channel to SoundSource3D \n"
            );
        KNI_ReturnVoid();
    }


    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nCheckSupported) {
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)KNI_GetParameterAsInt(1);
    javacall_handle player = (javacall_handle)(pKniInfo->pNativeHandle);
    javacall_result result = JAVACALL_FAIL;
    javacall_media_format_type format, *supported;
    int num;
    if (JAVACALL_OK == javacall_media_get_format(player, &format) &&
        NULL != (supported = javacall_audio3d_get_supported_soundsource3d_player_types(&num))) {
        
        int i;
        for (i = 0; i < num; i++) {
            if (javautil_string_equals(format, supported[i])) {
                result = JAVACALL_OK;
                break;
            }
        }
    }
    if (JAVACALL_OK != result) {
        KNI_ThrowNew("javax/microedition/media/MediaException",
                "\nThe Player cannot be added to SoundSource3D\n"
            );
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nAddPlayer)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)KNI_GetParameterAsInt( 1 );
    javacall_handle player = ( javacall_handle )( pKniInfo->pNativeHandle );
    javacall_result result = JAVACALL_FAIL;

    result = javacall_audio3d_soundsource3d_add_player( src, player );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nInvalid arguments were passed to the native part of \
SoundSource3D.addPlayer\n"
                );
        KNI_ReturnVoid();
    }

    if( result == JAVACALL_NOT_IMPLEMENTED )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException",
                "\nAdding the Player to SoundSource3D is not supported\n"
            );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred while adding the Player to SoundSource3D\n"
            );
        KNI_ReturnVoid();
    }


    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nRemoveMIDIChannel)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)KNI_GetParameterAsInt( 1 );
    javacall_handle player = ( javacall_handle )( pKniInfo->pNativeHandle );
    int channel = KNI_GetParameterAsInt(2);
    javacall_result result = JAVACALL_FAIL;

    result = javacall_audio3d_soundsource3d_remove_midi_channel( src, player, 
        channel );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nInvalid arguments were passed to the native part of \
SoundSource3D.removeMIDIChannel\n"
                );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred while removing the MIDI channel from SoundSource3D\n"
            );
        KNI_ReturnVoid();
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nRemovePlayer)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)KNI_GetParameterAsInt( 1 );
    javacall_handle player = ( javacall_handle )( pKniInfo->pNativeHandle );
    javacall_result result = JAVACALL_FAIL;

    result = javacall_audio3d_soundsource3d_remove_player( src, player );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nInvalid arguments were passed to the native part of \
SoundSource3D.removePlayer\n"
                );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred while removing the Player from SoundSource3D\n"
            );
        KNI_ReturnVoid();
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_DirectSoundSource3D_nGetControlPeer)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    javacall_amms_control_t *control;
    javacall_amms_control_type_enum_t type;

    getControlTypeFromArg( KNIPASSARGS &type );

    if( type == javacall_amms_eUnknownControl )
    {
        KNI_ReturnInt( NULL );
    }

    control = javacall_audio3d_soundsource3d_get_control( src, type );

    KNI_ReturnInt( ( jint )control );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_DirectSoundSource3D_nGetNumOfSupportedControls)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    int n = 0;

    javacall_audio3d_soundsource3d_get_controls( src, &n );

    KNI_ReturnInt( ( jint )n );
    
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSoundSource3D_nGetSupportedControlNames)
{
    javacall_audio3d_soundsource3d_t *src = getNativePtr(KNIPASSARGS 0);
    int n = 0;
    javacall_amms_control_t *control = NULL;

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( names );
    KNI_GetParameterAsObject( 1, names );
    control = javacall_audio3d_soundsource3d_get_controls( src, &n );

    controlsToJavaNamesArray( KNIPASSARGS control, n, names );

    KNI_EndHandles();
    KNI_ReturnVoid();
}
