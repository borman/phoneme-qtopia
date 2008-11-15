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

#include <stdlib.h>
#include <string.h>

#include "kni.h"
#include "javacall_multimedia_advanced.h"
#include "javacall_memory.h"
#include "jsr234_control.h"
#include "jsr234_nativePtr.h"

typedef struct
{
    const char*                         name;
    javacall_amms_control_type_enum_t   code;
} ctl_tbl_entry;

ctl_tbl_entry ctl_tbl[] = 
{
    { "javax.microedition.amms.control.audio3d.LocationControl",
        javacall_audio3d_eLocationControl },

    { "javax.microedition.amms.control.audio3d.OrientationControl",
        javacall_audio3d_eOrientationControl },

    { "javax.microedition.amms.control.audio3d.DistanceAttenuationControl",
        javacall_audio3d_eDistanceAttenuationControl },

    { "javax.microedition.amms.control.audioeffect.ReverbControl",
        javacall_music_eReverbControl },

    { "javax.microedition.amms.control.audioeffect.AudioVirtualizeControl", 
        javacall_amms_eAudioVirtualizerControl },

    { "javax.microedition.amms.control.audioeffect.ChorusControl", 
        javacall_amms_eChorusControl },

    { "javax.microedition.amms.control.audioeffect.EqualizerControl", 
        javacall_amms_eEqualizerControl },

    { "javax.microedition.amms.control.audioeffect.ReverbSourceControl", 
        javacall_amms_eReverbSourceControl },

    { "javax.microedition.media.control.VolumeControl", 
        javacall_amms_eVolumeControl }
};

#define CTL_TBL_N   ( sizeof( ctl_tbl ) / sizeof( ctl_tbl[ 0 ] ) )

javacall_amms_control_type_enum_t getControlTypeFromName( 
                                                   const char* type_name )
{
    int i;

    for( i = 0; i < CTL_TBL_N; i++ )
        if( 0 == strcmp( type_name, ctl_tbl[ i ].name ) )
            return ctl_tbl[ i ].code;

    return javacall_amms_eUnknownControl;
}

const char* getControlNameFromEnum( javacall_amms_control_type_enum_t type )
{
    int i;

    for( i = 0; i < CTL_TBL_N; i++ )
        if( ctl_tbl[ i ].code == type )
            return ctl_tbl[ i ].name;

    return NULL;
}

int getControlTypeFromArg( KNIDECLARGS javacall_amms_control_type_enum_t *type )
{
    char *buffer = NULL;
    int len = 0;

    *type = javacall_amms_eUnknownControl;
    KNI_StartHandles(1);
    KNI_DeclareHandle( type_name );
    KNI_GetParameterAsObject( 1, type_name );
    len = ( int )KNI_GetArrayLength( type_name );
    buffer = javacall_malloc( len + 1 );
    if( buffer == NULL )
    {
        KNI_ThrowNew("java/lang/OutOfMemoryError", 
            "Not enough memory to create a string buffer" );
        return 0;
    }

    KNI_GetRawArrayRegion( type_name, 0, len, buffer );
    buffer[ len ] = '\0';

    KNI_EndHandles();

    *type = getControlTypeFromName( buffer );
    javacall_free( buffer );

    return 1;
}

int controlsToJavaNamesArray( KNIDECLARGS javacall_amms_control_t controls[], int len,
                              jobject javaNamesArray )
{
    int i = 0;
    if ( len != KNI_GetArrayLength( javaNamesArray ) )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nNumber of supported Controls is ambiguous\n");

        return 0;
    }

    for( i = 0; i < len; i++)
    {
        const char* str = NULL;
        KNI_StartHandles(1);
        KNI_DeclareHandle( name );

        str = getControlNameFromEnum( controls[i].type );
        if ( str == NULL )
        {
            KNI_ThrowNew("java/lang/RuntimeException", 
                "\nControls supported contain a Control of unknown type\n");

            return 0;
        }

        KNI_NewStringUTF( str, name );
        
        KNI_SetObjectArrayElement( javaNamesArray, i, name );

        KNI_EndHandles();
    }

    return 1;
}

javacall_amms_control_t *getNativeControlPtr(KNIDECLARGS int dummy)
{
    return ( javacall_amms_control_t* )getNativeHandleFromField( KNIPASSARGS
        "_peer" );
}

