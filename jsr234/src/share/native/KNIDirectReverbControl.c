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

#include "jsr234_control.h"
#include "jsr234_effect_control.h"

static ectl_vtbl reverb_ectl_vtbl =
{
    (JCFN_GET_PRESET)        javacall_music_reverb_control_get_preset,
    (JCFN_GET_PRESET_NAMES)  javacall_music_reverb_control_get_preset_names,
    (JCFN_SET_PRESET)        javacall_music_reverb_control_set_preset,
    (JCFN_GET_SCOPE)         javacall_music_reverb_control_get_scope,
    (JCFN_SET_SCOPE)         javacall_music_reverb_control_set_scope,
    (JCFN_IS_ENABLED)        javacall_music_reverb_control_is_enabled,
    (JCFN_SET_ENABLED)       javacall_music_reverb_control_set_enabled,
    (JCFN_IS_ENFORCED)       javacall_music_reverb_control_is_enforced,
    (JCFN_SET_ENFORCED)      javacall_music_reverb_control_set_enforced
};

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectReverbControl_nGetEctlVtbl)
{
    KNI_ReturnInt( (int)( &reverb_ectl_vtbl ) );
}

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectReverbControl_nGetReverbLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    if( control->type != javacall_music_eReverbControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a ReverbControl \
implementation. Cannot execute getReverbLevel()\n" );
        KNI_ReturnInt( 1 );
    }

    KNI_ReturnInt( javacall_music_reverb_control_get_reverb_level(
        ( javacall_music_reverb_control_t* )control->ptr ) );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectReverbControl_nGetReverbTime)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int rTime = 0;

    if( control->type != javacall_music_eReverbControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a ReverbControl \
implementation. Cannot execute getReverbTime()\n" );
        KNI_ReturnInt( -1 );
    }

    rTime = javacall_music_reverb_control_get_reverb_time(
        ( javacall_music_reverb_control_t* )control->ptr );

    if( rTime < 0 )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException", 
"\nGetting Reverb Time is not supported\n" );
    }

    KNI_ReturnInt( rTime );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectReverbControl_nSetReverbLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int level = 1;

    if( control->type != javacall_music_eReverbControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a ReverbControl \
implementation. Cannot execute setReverbLevel()\n" );
        KNI_ReturnInt( 1 );
    }

    level = ( int )KNI_GetParameterAsInt( 1 );

    result = javacall_music_reverb_control_set_reverb_level(
        ( javacall_music_reverb_control_t* )control->ptr, level );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
ReverbControl.setReverbLevel()\n");
    }

    KNI_ReturnInt( ( jint )level );

}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectReverbControl_nSetReverbTime)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int time = -1;

    if( control->type != javacall_music_eReverbControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a ReverbControl \
implementation. Cannot execute setReverbLevel()\n" );
        KNI_ReturnVoid();
    }

    time = ( int )KNI_GetParameterAsInt( 1 );

    result = javacall_music_reverb_control_set_reverb_time(
        ( javacall_music_reverb_control_t* )control->ptr, time );

    if( result == JAVACALL_NOT_IMPLEMENTED )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException", 
"\nSetting Reverb Time is not supported\n" );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
ReverbControl.setReverbLevel()\n");
        KNI_ReturnVoid();
    }

    KNI_ReturnVoid();
}
