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

KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_directcontrol_DirectLocationControl_nGetCartesian)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int coord[3] = {0,0,0};

    if( control->type != javacall_audio3d_eLocationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a LocationControl implementation.\
 The getCartesian() method failed\n" );
        KNI_ReturnVoid();
    }

    result = javacall_audio3d_location_control_get_cartesian( 
        ( javacall_audio3d_location_control_t* )control->ptr, coord );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within LocationControl.getCartesian() method\n" );
    }

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( javaCoord );

    KNI_GetParameterAsObject( 1, javaCoord );

    KNI_SetIntArrayElement( javaCoord, 0, ( jint )coord[ 0 ] );
    KNI_SetIntArrayElement( javaCoord, 1, ( jint )coord[ 1 ] );
    KNI_SetIntArrayElement( javaCoord, 2, ( jint )coord[ 2 ] );

    KNI_EndHandles();

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_directcontrol_DirectLocationControl_nSetCartesian)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int x = 0, y = 0, z = 0;

    if( control->type != javacall_audio3d_eLocationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a LocationControl implementation.\
 Cannot invoke setCartesian\n" );
        KNI_ReturnVoid();
    }

    x = KNI_GetParameterAsInt( 1 );
    y = KNI_GetParameterAsInt( 2 );
    z = KNI_GetParameterAsInt( 3 );

    result = javacall_audio3d_location_control_set_cartesian( 
        ( javacall_audio3d_location_control_t* )control->ptr, x, y, z );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within LocationControl.setCartesian() method\n" );
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_directcontrol_DirectLocationControl_nSetSpherical)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int azimuth = 0, elevation = 0, radius = 0;

    if( control->type != javacall_audio3d_eLocationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not a LocationControl implementation.\
 Cannot invoke setSpherical\n" );
        KNI_ReturnVoid();
    }

    azimuth     = KNI_GetParameterAsInt( 1 );
    elevation   = KNI_GetParameterAsInt( 2 );
    radius      = KNI_GetParameterAsInt( 3 );

    if( radius < 0 )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException", 
"\nNegative radius passed to native implementation of \
LocationControl.setSpherical\n" );
        KNI_ReturnVoid();
    }

    result = javacall_audio3d_location_control_set_spherical(
        ( javacall_audio3d_location_control_t* )control->ptr,
        azimuth, elevation, radius );

    if( result == JAVACALL_INVALID_ARGUMENT )
    {
        KNI_ThrowNew( "java/lang/IllegalArgumentException", 
"\nWrong parameters passed to native implementation of \
LocationControl.setSpherical\n" );
        KNI_ReturnVoid();
    }

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within LocationControl.setSpherical() method\n" );
    }

    KNI_ReturnVoid();

}

