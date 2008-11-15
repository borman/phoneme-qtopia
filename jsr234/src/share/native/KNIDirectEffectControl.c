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
#include "jsr234_effect_control.h"

ectl_vtbl* get_vtbl(KNIDECLARGS int dummy)
{
    return (ectl_vtbl*)getNativeHandleFromField( KNIPASSARGS "_ectl_vtbl" );
}

/*---------------------------------------------------------------------------*/

#define GET_VTBL    (get_vtbl(KNIPASSARGS 0))

/*---------------------------------------------------------------------------*/

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nGetPreset)
{
    const javacall_utf16 *preset;

    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute getPreset()\n" );
        KNI_ReturnInt( -1 );
    }
    */

    preset = GET_VTBL->get_preset( (ectl_t)control->ptr );

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( javaPresetName );

    if( NULL != preset )
        KNI_NewString( preset, wcslen( preset ), javaPresetName );
    else
        KNI_ReleaseHandle( javaPresetName );

    KNI_EndHandlesAndReturnObject( javaPresetName );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nGetNumOfSupportedPresets)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int n=0;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute getPresetNames()\n" );
        KNI_ReturnInt( -1 );
    }
    */

    GET_VTBL->get_preset_names( (ectl_t)control->ptr, &n );

    KNI_ReturnInt( n );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nGetPresetNames)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    int n = 0;
    int i = 0;
    const javacall_utf16 **names;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute getPresetNames()\n" );
        KNI_ReturnVoid();
    }
    */

    names = GET_VTBL->get_preset_names( (ectl_t)control->ptr, &n );

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( javaNames );
    KNI_GetParameterAsObject( 1, javaNames );

    if( n != KNI_GetArrayLength( javaNames ) )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nNumber of supported presets is ambiguous\n");
        KNI_ReturnVoid();
    }

    for( i = 0; i < n; i++ )
    {
        KNI_StartHandles( 1 );
        KNI_DeclareHandle( javaPresetName );
        KNI_NewString( names[i], wcslen( names[i] ), javaPresetName );
        KNI_SetObjectArrayElement( javaNames, i, javaPresetName );
        KNI_EndHandles();
    }

    KNI_EndHandles();

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nSetPreset)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_utf16 *buffer = NULL;
    int len = 0;
    javacall_result result = JAVACALL_FAIL;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute setPreset()\n" );
        KNI_ReturnVoid();
    }
    */

    KNI_StartHandles(1);
    KNI_DeclareHandle( jstrPresetName );

    KNI_GetParameterAsObject( 1, jstrPresetName );
    len = ( int )KNI_GetStringLength( jstrPresetName );
    
    buffer = javacall_malloc( sizeof( *buffer ) * ( len + 1 ) );
    if( buffer == NULL )
    {
        KNI_ThrowNew("java/lang/OutOfMemoryError", 
            "Not enough memory to create a string buffer" );
        KNI_ReturnVoid();
    }

    KNI_GetStringRegion( jstrPresetName, 0, len, buffer );
    buffer[ len ] = ( javacall_utf16 )0;

    KNI_EndHandles();

    result = GET_VTBL->set_preset( (ectl_t)control->ptr, buffer );
    
    javacall_free( buffer );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within the method EffectControl.setPreset()\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nGetScope)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute getScope()\n" );
        KNI_ReturnInt( -1 );
    }
    */

    KNI_ReturnInt( (jint)GET_VTBL->get_scope( (ectl_t)control->ptr ) );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nSetScope)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int scope = -1;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute getScope()\n" );
        KNI_ReturnVoid();
    }
    */

    scope = KNI_GetParameterAsInt( 1 );

    if( scope != javacall_amms_eSCOPE_LIVE_ONLY &&
        scope != javacall_amms_eSCOPE_RECORD_ONLY &&
        scope != javacall_amms_eSCOPE_LIVE_AND_RECORD )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException",
            "\nCannot set the scope. Not supported\n");
        KNI_ReturnVoid();
    }

    result = GET_VTBL->set_scope( (ectl_t)control->ptr, scope );

    if( result == JAVACALL_NOT_IMPLEMENTED )
    {
        KNI_ThrowNew( "javax/microedition/media/MediaException",
            "\nCannot set the scope. Not supported\n");
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within the method EffectControl.setScope()\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nIsEnabled)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    jboolean enabled = KNI_FALSE;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute isEnabled()\n" );
        KNI_ReturnBoolean( KNI_FALSE );
    }
    */

    enabled = ( GET_VTBL->is_enabled( (ectl_t)control->ptr ) == JAVACALL_TRUE ) ?
        KNI_TRUE : KNI_FALSE;


    KNI_ReturnBoolean( enabled );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nSetEnabled)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_bool enable = KNI_FALSE;
    javacall_result result = JAVACALL_FAIL;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute setEnabled()\n" );
        KNI_ReturnVoid();
    }
    */

    enable = ( KNI_FALSE == KNI_GetParameterAsBoolean( 1 ) ) ? 
        JAVACALL_FALSE : JAVACALL_TRUE;

    result = GET_VTBL->set_enabled( (ectl_t)control->ptr, enable );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within the method EffectControl.setEnabled()\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nIsEnforced)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    jboolean enforced = KNI_FALSE;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute isEnforced()\n" );
        KNI_ReturnBoolean( KNI_FALSE );
    }
    */

    enforced = ( GET_VTBL->is_enforced( (ectl_t)control->ptr ) 
                 == JAVACALL_TRUE ) ? KNI_TRUE : KNI_FALSE;

    KNI_ReturnBoolean( enforced );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectEffectControl_nSetEnforced)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_bool enforce = KNI_FALSE;
    javacall_result result = JAVACALL_FAIL;

    /*
    if( control->type != javacall_amms_eEqualizerControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
            "\nThe native Control implementation is not an EffectControl \
            implementation. Cannot execute setEnforced()\n" );
        KNI_ReturnVoid();
    }
    */

    enforce = ( KNI_FALSE == KNI_GetParameterAsBoolean( 1 ) ) ? 
        JAVACALL_FALSE : JAVACALL_TRUE;

    result = GET_VTBL->set_enforced( (ectl_t)control->ptr, enforce );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
            "\nNative error occurred within the method EffectControl.setEnforced()\n" );
    }

    KNI_ReturnVoid();
}
