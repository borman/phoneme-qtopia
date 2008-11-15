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

//#include <windows.h>
#include "javacall_multimedia_advanced.h"

#include "mm_qsound_audio.h"
#include "mQ_JSR-234.h"

#include "multimedia_advanced_effectcontrol.h"
#include "javautil_unicode.h"

void
    effectcontrol_init(
        effectcontrol_t*     ectl,
        IEffectControl*      qs_obj_ptr,
        effectcontrol_vtbl*  vtbl )
{
    int           i;
    MQ234_String* pst234;
    javacall_int32 utf16_len;
    javacall_int32 utf8_len;
    javacall_int32 length;

    ectl->qs_obj_ptr = qs_obj_ptr;
    ectl->vtbl       = vtbl;
    ectl->presets_n  = 0;

    //printf( "            EC: %08X/%08X(%08X) : init\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    pst234 = ectl->vtbl->get_preset_names( ectl->qs_obj_ptr );

    if( NULL != pst234 )
    {
        /* I assume that this is a null-terminated list */
        while( NULL != pst234[ ectl->presets_n ] ) ectl->presets_n++;

        ectl->presets = malloc( ectl->presets_n * sizeof( javacall_utf16* ) );

        for( i = 0; i < ectl->presets_n; i++ ) {
            utf8_len = strlen(pst234[ i ]);
            if (JAVACALL_OK != javautil_unicode_utf8_to_utf16(pst234[ i ], utf8_len, NULL, 0, &utf16_len)) {
                continue;
            }

            /* Add one the the zero terminator. */
            utf16_len++;
            ectl->presets[ i ] = malloc(utf16_len * sizeof(javacall_utf16));
            if (ectl->presets[ i ] == NULL) {
                continue;
            }

            if (JAVACALL_OK != javautil_unicode_utf8_to_utf16(pst234[ i ], utf8_len, ectl->presets[ i ], utf16_len, &length)) {
                free(ectl->presets[ i ]);
                continue;
            }
            ectl->presets[ i ][ length ] = 0;
        }
    }
    else
    {
        ectl->presets   = NULL;
        ectl->presets_n = 0;
    }
}

void effectcontrol_cleanup(
        effectcontrol_t* ectl )
{
    //printf( "            EC: %08X/%08X(%08X) : cleanup\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );
    if( NULL != ectl->presets ) free( ectl->presets );
}


const javacall_utf16* effectcontrol_get_preset(
        effectcontrol_t* ectl )
{
    MQ234_String            pname;
    javacall_utf16_string   pname_utf16;
    int                     i;
    javacall_int32          utf16_len;
    javacall_int32          utf8_len;
    javacall_int32          length;

    //printf( "            EC: %08X/%08X(%08X) : get_preset\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    if( NULL == ectl ) return NULL;

    pname = ectl->vtbl->get_preset( ectl->qs_obj_ptr );
    if( NULL == pname ) return NULL;

    utf8_len = strlen(pname);
    if (JAVACALL_OK != javautil_unicode_utf8_to_utf16(pname, utf8_len, NULL, 0, &utf16_len)) {
        return NULL;
    }

    /* Add one the the zero terminator. */
    utf16_len++;
    pname_utf16 = malloc(utf16_len * sizeof(javacall_utf16));
    if (pname_utf16 == NULL) {
        return NULL;
    }

    if (JAVACALL_OK != javautil_unicode_utf8_to_utf16(pname, utf8_len, pname_utf16, utf16_len, &length)) {
        free(pname_utf16);
        return NULL;
    }
    pname_utf16[ length ] = 0;

    for( i = 0; i < ectl->presets_n; i++ )
    {
        if( 0 == wcscmp( pname_utf16, ectl->presets[ i ] ) )
        {
            free( pname_utf16 );
            return ectl->presets[ i ];
        }
    }

    return NULL;
}

const javacall_utf16** effectcontrol_get_preset_names(
        effectcontrol_t* ectl,
        int* number_of_presets )
{
    //printf( "            EC: %08X/%08X(%08X) : get_preset_names\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    if( NULL == number_of_presets ) return NULL;

    *number_of_presets = ectl->presets_n;
    return ectl->presets;
}

javacall_result effectcontrol_set_preset(
        effectcontrol_t* ectl,
        const javacall_utf16* preset_name )
{
    int             i;
    MQ234_String*   pst234;

    //printf( "            EC: %08X/%08X(%08X) : set_preset\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    if( NULL == preset_name || NULL == ectl )
        return JAVACALL_INVALID_ARGUMENT;

    for( i = 0; i < ectl->presets_n; i++ )
    {
        if( 0 == wcscmp( preset_name, ectl->presets[i] ) )
        {
            pst234 = ectl->vtbl->get_preset_names( ectl->qs_obj_ptr );
            ectl->vtbl->set_preset( ectl->qs_obj_ptr, pst234[ i ] );
            return JAVACALL_OK;
        }
    }

    return JAVACALL_INVALID_ARGUMENT;
}

javacall_amms_effect_control_scope_enum_t
    effectcontrol_get_scope(
        effectcontrol_t* ectl )
{
    JSR234_SCOPE  scope;

    if( NULL == ectl ) return -1;

    //printf( "            EC: %08X/%08X(%08X) : get_scope\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    scope = ectl->vtbl->get_scope( ectl->qs_obj_ptr );

    switch( scope )
    {
    case JSR234_SCOPE_LIVE_ONLY:
        return javacall_amms_eSCOPE_LIVE_ONLY;
    case JSR234_SCOPE_RECORD_ONLY:
        return javacall_amms_eSCOPE_RECORD_ONLY;
    case JSR234_SCOPE_LIVE_AND_RECORD:
        return javacall_amms_eSCOPE_LIVE_AND_RECORD;
    default:
        return -1;
    }
}

javacall_result effectcontrol_set_scope(
        effectcontrol_t* ectl,
        javacall_amms_effect_control_scope_enum_t scope )
{
    MQ234_ERROR   e;
    JSR234_SCOPE  scp234;

    if( NULL == ectl ) return JAVACALL_INVALID_ARGUMENT;

    //printf( "            EC: %08X/%08X(%08X) : set_scope\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );
    /*
     * seems like QSound doesn't support other scopes.
     */

    if( javacall_amms_eSCOPE_LIVE_ONLY != scope )
        return JAVACALL_NOT_IMPLEMENTED;

    switch( scope )
    {
    case javacall_amms_eSCOPE_LIVE_ONLY:
        scp234 = JSR234_SCOPE_LIVE_ONLY;
        break;
    case javacall_amms_eSCOPE_RECORD_ONLY:
        scp234 = JSR234_SCOPE_RECORD_ONLY;
        break;
    case javacall_amms_eSCOPE_LIVE_AND_RECORD:
        scp234 = JSR234_SCOPE_LIVE_AND_RECORD;
        break;
    default:
        return JAVACALL_INVALID_ARGUMENT;
    }

    e = ectl->vtbl->set_scope( ectl->qs_obj_ptr, scp234 );

    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

javacall_bool effectcontrol_is_enabled(
        effectcontrol_t* ectl )
{
    if( NULL == ectl ) return -2;

    //printf( "            EC: %08X/%08X(%08X) : is_enabled\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );

    return ectl->vtbl->is_enabled( ectl->qs_obj_ptr );
}

javacall_result effectcontrol_set_enabled(
        effectcontrol_t* ectl,
        javacall_bool enabled )
{
    MQ234_ERROR e;

    if( NULL == ectl ) return JAVACALL_INVALID_ARGUMENT;
    //printf( "            EC: %08X/%08X(%08X) : set_enabled\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );
    if( JAVACALL_TRUE != enabled && JAVACALL_FALSE != enabled )
        return JAVACALL_INVALID_ARGUMENT;

    e = ectl->vtbl->set_enabled( ectl->qs_obj_ptr, enabled );

    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}

javacall_bool effectcontrol_is_enforced(
        effectcontrol_t* ectl )
{
    if( NULL == ectl ) return -2;

    //printf( "            EC: %08X/%08X(%08X) : is_enforced\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );
    return ectl->vtbl->is_enforced( ectl->qs_obj_ptr );
}

javacall_result effectcontrol_set_enforced (
        effectcontrol_t* ectl,
        javacall_bool    enforced )
{
    MQ234_ERROR e;

    if( NULL == ectl ) return JAVACALL_INVALID_ARGUMENT;
    //printf( "            EC: %08X/%08X(%08X) : set_enforced\n", ectl, ectl->qs_obj_ptr, ectl->vtbl->is_enabled );
    if( JAVACALL_TRUE != enforced && JAVACALL_FALSE != enforced )
        return JAVACALL_INVALID_ARGUMENT;

    e = ectl->vtbl->set_enforced( ectl->qs_obj_ptr, enforced );

    return ( MQ234_ERROR_NO_ERROR == e ) ? JAVACALL_OK : JAVACALL_FAIL;
}
