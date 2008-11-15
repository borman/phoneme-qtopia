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

static javacall_audio3d_spectator_t *getNativePtr(KNIDECLARGS int dummy)
{
    return ( javacall_audio3d_spectator_t* )getNativeHandleFromField(KNIPASSARGS
        "_peer" );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_DirectSpectatorImpl_nGetControlPeer)
{
    javacall_audio3d_spectator_t *spectator = getNativePtr(KNIPASSARGS 0);
    javacall_amms_control_t *control;
    javacall_amms_control_type_enum_t type;

    getControlTypeFromArg( KNIPASSARGS &type );

    if( type == javacall_amms_eUnknownControl )
    {
        KNI_ReturnInt( NULL );
    }

    control = javacall_audio3d_spectator_get_control( spectator, type );

    KNI_ReturnInt( ( jint )control );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_DirectSpectatorImpl_nGetNumOfSupportedControls)
{
    javacall_audio3d_spectator_t *spectator = getNativePtr(KNIPASSARGS 0);
    int n = 0;

    javacall_audio3d_spectator_get_controls( spectator, &n );

    KNI_ReturnInt( ( jint )n );
    
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_DirectSpectatorImpl_nGetSupportedControlNames)
{
    javacall_audio3d_spectator_t *spectator = getNativePtr(KNIPASSARGS 0);
    int n = 0;
    javacall_amms_control_t *control = NULL;

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( names );
    KNI_GetParameterAsObject( 1, names );
    control = javacall_audio3d_spectator_get_controls( spectator, &n );

    controlsToJavaNamesArray( KNIPASSARGS control, n, names );

    KNI_EndHandles();
    KNI_ReturnVoid();
}
