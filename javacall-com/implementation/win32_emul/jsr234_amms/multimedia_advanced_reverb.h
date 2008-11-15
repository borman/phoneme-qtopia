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

#ifndef __MULTIMEDIA_ADVANCED_REVERB_H
#define __MULTIMEDIA_ADVANCED_REVERB_H

#include "multimedia_advanced_effectcontrol.h"

#ifdef __cplusplus
extern "C" {
#endif

struct tag_javacall_music_reverb_control
{ 
    IReverbControl*                 qs_obj_ptr;
    int                             rtime;
    effectcontrol_t                 ectl;
};

void reverb_init( javacall_music_reverb_control_t* ctl,
                  IReverbControl*                  qs_obj_ptr );

void reverb_cleanup( javacall_music_reverb_control_t *ctl );

#ifdef __cplusplus
}
#endif

#endif /*__MULTIMEDIA_ADVANCED_REVERB_H*/