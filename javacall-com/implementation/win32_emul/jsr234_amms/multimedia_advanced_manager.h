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

#ifndef __MULTIMEDIA_ADVANCED_MANAGER_H
#define __MULTIMEDIA_ADVANCED_MANAGER_H

#include "multimedia_advanced_controllable.h"
#include "multimedia_advanced_controls.h"
#include "multimedia_advanced_spectator.h"
#include "multimedia_advanced_reverb.h"
#include "multimedia_advanced_equalizer.h"
           
#ifdef __cplusplus
extern "C" {
#endif

struct tag_javacall_amms_local_manager
{
    int                             gmIdx;
    javacall_music_reverb_control_t reverb;
    javacall_amms_equalizer_control_t equalizer;
    javacall_amms_volume_control_t  volume;
    javacall_audio3d_spectator_t    spectator;
    controllable_t                  controllable;
    javacall_amms_control_t         controls[3];
};

extern globalMan g_QSoundGM[];
#define QSOUND_GET_GM( n ) ( g_QSoundGM[ (n) ] )

#ifdef __cplusplus
}
#endif

#endif /*__MULTIMEDIA_ADVANCED_MANAGER_H*/
