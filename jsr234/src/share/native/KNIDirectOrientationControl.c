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
KNIDECL(com_sun_amms_directcontrol_DirectOrientationControl_nGetOrientationVectors)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int vectors[6] = { 0,0,0,0,0,0 };

    if( control->type != javacall_audio3d_eOrientationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not an OrientationControl \
implementation. Cannot execute getOrientationVectors()\n" );
        KNI_ReturnVoid();
    }

    result = javacall_audio3d_orientation_control_get_orientation_vectors(
        ( javacall_audio3d_orientation_control_t* )control->ptr, vectors );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
OrientationControl.getOrientationVectors()\n");
        KNI_ReturnVoid();
    }

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( javaVectors );
    KNI_GetParameterAsObject( 1, javaVectors );

    {
        int i;
        for( i = 0; i < 6; i++ )
        {
            KNI_SetIntArrayElement( javaVectors, i, ( jint )vectors[i] );
        }
    }

    KNI_EndHandles();
    

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectOrientationControl_nSetOrientationVec)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int front[ 3 ] = { 0,0,0 };
    int above[ 3 ] = { 0,0,0 };

    if( control->type != javacall_audio3d_eOrientationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not an OrientationControl \
implementation. Cannot execute setOrientation( int[], int[] )\n" );
        KNI_ReturnVoid();
    }

    KNI_StartHandles( 2 );
    KNI_DeclareHandle( frontVector );
    KNI_DeclareHandle( aboveVector );
    KNI_GetParameterAsObject( 1, frontVector );
    KNI_GetParameterAsObject( 2, aboveVector );

    front[ 0 ] = ( int )KNI_GetIntArrayElement( frontVector, 0 );
    front[ 1 ] = ( int )KNI_GetIntArrayElement( frontVector, 1 );
    front[ 2 ] = ( int )KNI_GetIntArrayElement( frontVector, 2 );
    above[ 0 ] = ( int )KNI_GetIntArrayElement( aboveVector, 0 );
    above[ 1 ] = ( int )KNI_GetIntArrayElement( aboveVector, 1 );
    above[ 2 ] = ( int )KNI_GetIntArrayElement( aboveVector, 2 );

    KNI_EndHandles();

    result = javacall_audio3d_orientation_control_set_orientation_vec(
        ( javacall_audio3d_orientation_control_t* )control->ptr,
        front, above );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
OrientationControl.setOrientation( int[], int[] )\n");
        KNI_ReturnVoid();
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectOrientationControl_nSetOrientation)
{
    javacall_amms_control_t *control = getNativeControlPtr(KNIPASSARGS 0);
    javacall_result result = JAVACALL_FAIL;
    int heading = 0, pitch = 0, roll = 0;

    if( control->type != javacall_audio3d_eOrientationControl )
    {
        KNI_ThrowNew( "java/lang/RuntimeException", 
"\nThe native Control implementation is not an OrientationControl \
implementation. Cannot execute setOrientation( int, int, int )\n" );
        KNI_ReturnVoid();
    }

    heading = ( int )KNI_GetParameterAsInt( 1 );
    pitch   = ( int )KNI_GetParameterAsInt( 2 );
    roll    = ( int )KNI_GetParameterAsInt( 3 );

    result = javacall_audio3d_orientation_control_set_orientation(
        ( javacall_audio3d_orientation_control_t* )control->ptr,
        heading, pitch, roll );

    if( result != JAVACALL_OK )
    {
        KNI_ThrowNew( "java/lang/RuntimeException",
"\nNative error occurred within the method \
OrientationControl.setOrientation( int, int, int )\n");
        KNI_ReturnVoid();
    }

    KNI_ReturnVoid();
}

