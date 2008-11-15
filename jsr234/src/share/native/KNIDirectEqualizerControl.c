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

static ectl_vtbl equalizer_ectl_vtbl =
{
    (JCFN_GET_PRESET)        javacall_amms_equalizer_control_get_preset,
    (JCFN_GET_PRESET_NAMES)  javacall_amms_equalizer_control_get_preset_names,
    (JCFN_SET_PRESET)        javacall_amms_equalizer_control_set_preset,
    (JCFN_GET_SCOPE)         javacall_amms_equalizer_control_get_scope,
    (JCFN_SET_SCOPE)         javacall_amms_equalizer_control_set_scope,
    (JCFN_IS_ENABLED)        javacall_amms_equalizer_control_is_enabled,
    (JCFN_SET_ENABLED)       javacall_amms_equalizer_control_set_enabled,
    (JCFN_IS_ENFORCED)       javacall_amms_equalizer_control_is_enforced,
    (JCFN_SET_ENFORCED)      javacall_amms_equalizer_control_set_enforced
};

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetEctlVtbl)
{
    KNI_ReturnInt( (int)( &equalizer_ectl_vtbl ) );
}

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetMinBandLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute ()\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_equalizer_control_get_min_band_level( 
        (javacall_amms_equalizer_control_t*)control->ptr );

    KNI_ReturnInt( result );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetMaxBandLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute ()\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_equalizer_control_get_max_band_level( 
        (javacall_amms_equalizer_control_t*)control->ptr );

    KNI_ReturnInt( result );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nSetBandLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int band, level;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute ()\n" );
        KNI_ReturnVoid();
    }

    band  = KNI_GetParameterAsInt( 2 ); /* note reverse parameter order */
    level = KNI_GetParameterAsInt( 1 ); /* note reverse parameter order */

    result = javacall_amms_equalizer_control_set_band_level( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        band, level );

    if( JAVACALL_INVALID_ARGUMENT == result )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
            "\nInvalid arguments were passed to native part of \
            EqualizerControl.setBandLevel\n" );
    }
    else if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within EqualizerControl.setBandLevel()\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetBandLevel)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int band, level;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetBandLevel()\n" );
        KNI_ReturnInt( -1 );
    }

    band = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_equalizer_control_get_band_level( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        band, &level );

    if( JAVACALL_INVALID_ARGUMENT == result )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
            "\nInvalid arguments were passed to native part of \
            EqualizerControl.GetBandLevel()\n" );
    }
    else if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within EqualizerControl.GetBandLevel()\n" );
    }

    KNI_ReturnInt( level );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetNumberOfBands)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetNumberOfBands()\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_equalizer_control_get_number_of_bands( 
        (javacall_amms_equalizer_control_t*)control->ptr );

    KNI_ReturnInt( result );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetCenterFreq)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int band, freq;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetCenterFreq()\n" );
        KNI_ReturnInt( -1 );
    }

    band = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_equalizer_control_get_center_freq( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        band, &freq );

    if( JAVACALL_INVALID_ARGUMENT == result )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
            "\nInvalid arguments were passed to native part of \
            EqualizerControl.GetCenterFreq()\n" );
    }
    else if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within EqualizerControl.GetCenterFreq()\n" );
    }

    KNI_ReturnInt( freq );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetBand)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    int freq;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetBand()\n" );
        KNI_ReturnInt( -1 );
    }

    freq = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_equalizer_control_get_band( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        freq);

    KNI_ReturnInt( result );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nSetBass)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int level, new_level;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute SetBass()\n" );
        KNI_ReturnInt( -1 );
    }

    level = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_equalizer_control_set_bass( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        level,
        &new_level );

    if( JAVACALL_INVALID_ARGUMENT == result )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
            "\nInvalid arguments were passed to native part of \
            EqualizerControl.SetBass()\n" );
    }
    else if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within EqualizerControl.SetBass()\n" );
    }

    KNI_ReturnInt( new_level );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nSetTreble)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int level, new_level;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute SetTreble()\n" );
        KNI_ReturnInt( -1 );
    }

    level = KNI_GetParameterAsInt( 1 );

    result = javacall_amms_equalizer_control_set_treble( 
        (javacall_amms_equalizer_control_t*)control->ptr,
        level,
        &new_level );

    if( JAVACALL_INVALID_ARGUMENT == result )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException",
            "\nInvalid arguments were passed to native part of \
            EqualizerControl.SetTreble()\n" );
    }
    else if( JAVACALL_OK != result )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within EqualizerControl.SetTreble()\n" );
    }

    KNI_ReturnInt( new_level );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetBass)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetBass()\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_equalizer_control_get_bass( 
        (javacall_amms_equalizer_control_t*)control->ptr );

    KNI_ReturnInt( result );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEqualizerControl_nGetTreble)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int result;
    
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EqualizerControl \
            implementation. Cannot execute GetTreble()\n" );
        KNI_ReturnInt( -1 );
    }

    result = javacall_amms_equalizer_control_get_treble( 
        (javacall_amms_equalizer_control_t*)control->ptr );

    KNI_ReturnInt( result );
}
