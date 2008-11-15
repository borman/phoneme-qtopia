/*
 *
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

#include "javacall_multimedia_advanced.h"

#include "mm_qsound_audio.h"
#include "mQ_JSR-234.h"

#include "multimedia_advanced_manager.h"
#include "multimedia_advanced_spectator.h"

void initSpectatorImpl( javacall_audio3d_spectator_t *spectator,
                        javacall_amms_local_manager_t *mgr )
{
    const long orient_init_f[ 3 ] = { 0, 0, -1000 };
    const long orient_init_a[ 3 ] = { 0, 1000, 0 };

    memset( spectator, 0, sizeof( *spectator ) );

    spectator->qs_obj_ptr=mQ234_GlobalManager_getSpectator( QSOUND_GET_GM( mgr->gmIdx ).gm );

    spectator->orient.qs_obj_ptr
        = mQ234_Spectator_getOrientationControl( spectator->qs_obj_ptr );

    spectator->location.qs_obj_ptr
        = mQ234_Spectator_getLocationControl( spectator->qs_obj_ptr );

    spectator->controls[ 0 ].ptr  = &( spectator->orient );
    spectator->controls[ 0 ].type = javacall_audio3d_eOrientationControl;

    spectator->controls[ 1 ].ptr  = &( spectator->location );
    spectator->controls[ 1 ].type = javacall_audio3d_eLocationControl;

    spectator->controllable.controls = spectator->controls;
    spectator->controllable.number_of_controls =
        sizeof( spectator->controls ) / sizeof( spectator->controls[ 0 ] );

    mQ234_Orientation_setOrientationVectors(
        spectator->orient.qs_obj_ptr,
        orient_init_f, orient_init_a );

    mQ234_Location_setCartesian(
        spectator->location.qs_obj_ptr,
        0, 0, 0 );
}

/*---------------------------------------------------------------------------*/

javacall_amms_control_t* javacall_audio3d_spectator_get_control (
    javacall_audio3d_spectator_t *manager,
    javacall_amms_control_type_enum_t type )
{
    return controllable_get_control( &manager->controllable, type );
}

javacall_amms_control_t* javacall_audio3d_spectator_get_controls (
    javacall_audio3d_spectator_t *manager,
    /*OUT*/ int *controls_number )
{
    return controllable_get_controls( &manager->controllable, controls_number );
}


