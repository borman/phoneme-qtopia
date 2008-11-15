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

/*---------------------------------------------------------------------------*/

#define BOOL_KNI2JC( b ) ( ( KNI_FALSE == (b) ) ? JAVACALL_FALSE : JAVACALL_TRUE )
#define BOOL_JC2KNI( b ) ( ( JAVACALL_FALSE == (b) ) ? KNI_FALSE : KNI_TRUE )

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_directcontrol_DirectVolumeControl_nSetMute)
{
    javacall_bool            mute;
    javacall_result          result;
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    if( control->type != javacall_amms_eVolumeControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe underlying native control implementation"
            "is not a Volume control."
            "The setMute() method failed\n" );
        KNI_ReturnVoid();
    }

    mute = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_volume_control_set_mute( 
        (javacall_amms_volume_control_t*)control->ptr,
        BOOL_KNI2JC( mute ) );

    if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within VolumeControl.setMute() method\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_directcontrol_DirectVolumeControl_nIsMuted)
{
    javacall_bool           result;
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    if( control->type != javacall_amms_eVolumeControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe underlying native control implementation"
            "is not a Volume control."
            "The isMuted() method failed\n" );
        KNI_ReturnBoolean( KNI_FALSE );
    }

    result = javacall_amms_volume_control_is_muted( 
        (javacall_amms_volume_control_t*)control->ptr );

    KNI_ReturnBoolean( BOOL_JC2KNI( result ) );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectVolumeControl_nSetLevel)
{
    int                     level, new_level;
    javacall_result         result;
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    if( control->type != javacall_amms_eVolumeControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe underlying native control implementation"
            "is not a Volume control."
            "The setLevel() method failed\n" );
        KNI_ReturnInt( -1 );
    }

    level = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_volume_control_set_level( 
        ( javacall_amms_volume_control_t* )control->ptr,
        level,
        &new_level );

    if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within VolumeControl.setLevel() method\n" );
    }

    KNI_ReturnInt( new_level );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_directcontrol_DirectVolumeControl_nGetLevel)
{
    int                      result;
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    if( control->type != javacall_amms_eVolumeControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe underlying native control implementation"
            "is not a Volume control."
            "The getLevel() method failed\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_volume_control_get_level( 
        ( javacall_amms_volume_control_t* )control->ptr );

    KNI_ReturnInt( result );
}
