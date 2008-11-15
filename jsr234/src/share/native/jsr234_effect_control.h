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

#ifndef __JSR234_EFFECT_CONTROL_H
#define __JSR234_EFFECT_CONTROL_H

/*  this type will be used here as an alias for
 *   pointers to native EffectControl derivatives,
 *   such as javacall_music_reverb_control_t* or
 *           javacall_music_equalizer_control_t*
 */

typedef void* ectl_t; 

/*
 *   JavaCall functions presenting EffectControl
 *   interface in derivatives 
 */

typedef const javacall_utf16*  (*JCFN_GET_PRESET)      ( ectl_t );
typedef const javacall_utf16** (*JCFN_GET_PRESET_NAMES)( ectl_t, int* );
typedef javacall_result        (*JCFN_SET_PRESET)      ( ectl_t, const javacall_utf16* );
typedef javacall_amms_effect_control_scope_enum_t
                               (*JCFN_GET_SCOPE)       ( ectl_t );
typedef javacall_result        (*JCFN_SET_SCOPE)       ( ectl_t, javacall_amms_effect_control_scope_enum_t scope );
typedef javacall_bool          (*JCFN_IS_ENABLED)      ( ectl_t );
typedef javacall_result        (*JCFN_SET_ENABLED)     ( ectl_t, javacall_bool enabled );
typedef javacall_bool          (*JCFN_IS_ENFORCED)     ( ectl_t );
typedef javacall_result        (*JCFN_SET_ENFORCED)    ( ectl_t, javacall_bool enforced );


typedef struct
{
    JCFN_GET_PRESET        get_preset;
    JCFN_GET_PRESET_NAMES  get_preset_names;
    JCFN_SET_PRESET        set_preset;
    JCFN_GET_SCOPE         get_scope;
    JCFN_SET_SCOPE         set_scope;
    JCFN_IS_ENABLED        is_enabled;
    JCFN_SET_ENABLED       set_enabled;
    JCFN_IS_ENFORCED       is_enforced;
    JCFN_SET_ENFORCED      set_enforced;
} ectl_vtbl;

#endif //__JSR234_EFFECT_CONTROL_H

