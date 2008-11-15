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

#ifndef __MULTIMEDIA_ADVANCED_EFFECTCONTROL_H
#define __MULTIMEDIA_ADVANCED_EFFECTCONTROL_H

#ifdef __cplusplus
extern "C" {
#endif

typedef MQ234_ERROR   (MQ234_API * MQ234_SETENABLED    )( IEffectControl*, int          );
typedef int           (MQ234_API * MQ234_ISENABLED     )( IEffectControl*               );
typedef MQ234_ERROR   (MQ234_API * MQ234_SETSCOPE      )( IEffectControl*, JSR234_SCOPE );
typedef JSR234_SCOPE  (MQ234_API * MQ234_GETSCOPE      )( IEffectControl*               );
typedef MQ234_ERROR   (MQ234_API * MQ234_SETENFORCED   )( IEffectControl*, int          );
typedef int           (MQ234_API * MQ234_ISENFORCED    )( IEffectControl*               );
typedef MQ234_ERROR   (MQ234_API * MQ234_SETPRESET     )( IEffectControl*, MQ234_String );
typedef MQ234_String  (MQ234_API * MQ234_GETPRESET     )( IEffectControl*               );
typedef MQ234_String* (MQ234_API * MQ234_GETPRESETNAMES)( IEffectControl*               );

typedef struct
{
    MQ234_SETENABLED     set_enabled;
    MQ234_ISENABLED      is_enabled;
    MQ234_SETSCOPE       set_scope;
    MQ234_GETSCOPE       get_scope;
    MQ234_SETENFORCED    set_enforced;
    MQ234_ISENFORCED     is_enforced;
    MQ234_SETPRESET      set_preset;
    MQ234_GETPRESET      get_preset;
    MQ234_GETPRESETNAMES get_preset_names;
} effectcontrol_vtbl;

typedef struct
{
    IEffectControl*      qs_obj_ptr;

    int                  presets_n;
    javacall_utf16**     presets;
    effectcontrol_vtbl*  vtbl;
} effectcontrol_t;

void
    effectcontrol_init( 
        effectcontrol_t*     ctl,
        IEffectControl*      qs_obj_ptr,
        effectcontrol_vtbl*  vtbl );

void effectcontrol_cleanup( effectcontrol_t* ctl );


const javacall_utf16*
    effectcontrol_get_preset(
        effectcontrol_t* ectl );

const javacall_utf16**
    effectcontrol_get_preset_names(
        effectcontrol_t* ectl,
        /*OUT*/ int*     number_of_presets );

javacall_result
    effectcontrol_set_preset (
        effectcontrol_t*      ectl,
        const javacall_utf16* preset_name );

javacall_amms_effect_control_scope_enum_t 
    effectcontrol_get_scope (
        effectcontrol_t* ectl );

javacall_result
    effectcontrol_set_scope (
        effectcontrol_t*                          ectl,
        javacall_amms_effect_control_scope_enum_t scope );

javacall_bool
    effectcontrol_is_enabled (
        effectcontrol_t* ectl );

javacall_result
    effectcontrol_set_enabled (
        effectcontrol_t* ectl,
        javacall_bool    enabled );

javacall_bool
    effectcontrol_is_enforced (
        effectcontrol_t* ectl );

javacall_result
    effectcontrol_set_enforced (
        effectcontrol_t* ectl,
        javacall_bool    enforced );

#ifdef __cplusplus
}
#endif

#endif /*__MULTIMEDIA_ADVANCED_EFFECTCONTROL_H*/
