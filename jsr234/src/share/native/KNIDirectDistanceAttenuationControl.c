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

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_directcontrol_DirectDistanceAttenuationControl_nGetMaxDistance)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    jint md = 0;

    if( control->type != javacall_audio3d_eDistanceAttenuationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a DistanceAttenuationControl \
implementation. Cannot execute getMaxDistance\n" );
        KNI_ReturnInt( -1 );
    }
    md = javacall_audio3d_distance_attenuation_control_get_max_distance(
        ( javacall_audio3d_distance_attenuation_control_t* )control->ptr );

    KNI_ReturnInt( md );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_directcontrol_DirectDistanceAttenuationControl_nGetMinDistance)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    jint md = 0;

    if( control->type != javacall_audio3d_eDistanceAttenuationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a DistanceAttenuationControl \
implementation. Cannot execute getMinDistance\n" );
        KNI_ReturnInt( -1 );
    }
    md = javacall_audio3d_distance_attenuation_control_get_min_distance(
        ( javacall_audio3d_distance_attenuation_control_t* )control->ptr );

    KNI_ReturnInt( md );
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_directcontrol_DirectDistanceAttenuationControl_nGetMuteAfterMax)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_bool mute = JAVACALL_FALSE;
    if( control->type != javacall_audio3d_eDistanceAttenuationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a DistanceAttenuationControl \
implementation. Cannot execute getMuteAfterMax()\n" );
        KNI_ReturnBoolean( KNI_FALSE );
    }

    mute = javacall_audio3d_distance_attenuation_control_is_mute_after_max(
        ( javacall_audio3d_distance_attenuation_control_t* )control->ptr );

    KNI_ReturnBoolean( ( ( mute == JAVACALL_TRUE ) ? KNI_TRUE : KNI_FALSE ) );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_directcontrol_DirectDistanceAttenuationControl_nGetRolloffFactor)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    jint rof = 0;

    if( control->type != javacall_audio3d_eDistanceAttenuationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a DistanceAttenuationControl \
implementation. Cannot execute getRollofFactor()\n" );
        KNI_ReturnInt( -1 );
    }
    rof = javacall_audio3d_distance_attenuation_control_get_rolloff_factor(
        ( javacall_audio3d_distance_attenuation_control_t* )control->ptr );

    KNI_ReturnInt( rof );
}


KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectDistanceAttenuationControl_nSetParameters)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int minDistance = 0;
    int maxDistance = 0;
    javacall_bool muteAfterMax = KNI_FALSE;
    int rolloffFactor = 0;
    javacall_result result = JAVACALL_FAIL;

    if( control->type != javacall_audio3d_eDistanceAttenuationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a DistanceAttenuationControl \
implementation. Cannot execute setParameters()\n" );
        KNI_ReturnVoid();
    }

    minDistance = ( int )KNI_GetParameterAsInt( 1 );
    maxDistance = ( int )KNI_GetParameterAsInt( 2 );
    muteAfterMax = ( KNI_GetParameterAsBoolean( 3 ) == KNI_TRUE ) ?
        JAVACALL_TRUE : JAVACALL_FALSE;
    rolloffFactor = ( int )KNI_GetParameterAsInt( 4 );

    if( minDistance <= 0 || 
        maxDistance <= 0 || 
        maxDistance <= minDistance || 
        rolloffFactor < 0 )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nWrong arguments were passed to native implementation of\
DistanceAttenuationControl.setParameters()\n");
        KNI_ReturnVoid();
    }

    result = javacall_audio3d_distance_attenuation_control_set_parameters(
        ( javacall_audio3d_distance_attenuation_control_t* )control->ptr,
        minDistance, maxDistance, muteAfterMax, rolloffFactor );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
"\nWrong arguments were passed to JavaCall implementation of \
DistanceAttenuationControl.setParameters()\n");
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
DistanceAttenuationControl.setParameters()\n");
    }

    KNI_ReturnVoid();
}

