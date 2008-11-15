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
#include "javautil_string.h"

#define MAX_MIMETYPE_LEN    30

static javacall_amms_local_manager_t *getNativePtr(KNIDECLARGS int dummy)
{
    return ( javacall_amms_local_manager_t* )getNativeHandleFromField(KNIPASSARGS
        "_peer" );
}

const char *getMIMEFromMediaTypeEnum( javacall_media_format_type type, char *buf, int buf_len )
{
    const javacall_media_configuration* cfg;
    javacall_media_caps* cap;
    
    if (javacall_media_get_configuration(&cfg) != JAVACALL_OK) {
        /* the configuration isn't available */
        return NULL;
    }
    for (cap = cfg->mediaCaps; cap->mediaFormat != NULL; cap++) {
        if (javautil_string_equals(cap->mediaFormat, type)) {
            char *p = cap->contentTypes, *p1 = p;
            int len;
            if (p == NULL) {
                /* invalid configuration */
                return NULL;
            }
            if ((p1 = strchr(p, ' ')) != NULL) {
                len = (int)(p1 - p);
            } else {
                len = strlen(p);
            }
            if (buf_len <= len) {
                /* provided buffer is too small */
                return NULL;
            }
            memcpy(buf, p, len);
            buf[len] = '\0';
            return buf;
        }
    }
    return NULL;
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_GlobalMgrImpl_nCreatePeer)
{
    javacall_result res = JAVACALL_FAIL;
    javacall_amms_local_manager_t *ret = NULL;

    res = javacall_amms_create_local_manager( &ret );
    if( res == JAVACALL_OUT_OF_MEMORY )
    {
        KNI_ThrowNew("java/lang/OutOfMemoryError", 
            "\nNot enough memory to create GlobalManager\n");
        KNI_ReturnInt( NULL );
    }
    
    if( res == JAVACALL_FAIL )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nFailed to create GlobalManager\n");
        KNI_ReturnInt( NULL );
    }

    KNI_ReturnInt( ( jint )ret );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_GlobalMgrImpl_finalize)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);

    if( mgr != NULL )
    {
        javacall_amms_destroy_local_manager( mgr );
        mgr = NULL;
    }


    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_GlobalMgrImpl_nCreateSoundSource3D)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);
    javacall_audio3d_soundsource3d_t *src = NULL;
    javacall_result res = JAVACALL_FAIL;


    res = javacall_amms_local_manager_create_sound_source3d( mgr, &src );
    if( res == JAVACALL_OUT_OF_MEMORY )
    {
        KNI_ThrowNew("java/lang/OutOfMemoryError", 
            "\nNot enough memory to create SoundSource3D\n");
        KNI_ReturnInt( NULL );
    }
    
    if( res == JAVACALL_FAIL )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nFailed to create SoundSource3D\n");
        KNI_ReturnInt( NULL );
    }

    KNI_ReturnInt( ( jint )src );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetControlPeer)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);
    javacall_amms_control_t *control;
    javacall_amms_control_type_enum_t type;

    getControlTypeFromArg( KNIPASSARGS &type );

    if( type == javacall_amms_eUnknownControl )
    {
        KNI_ReturnInt( NULL );
    }

    control = javacall_amms_local_manager_get_control( mgr, type );

    KNI_ReturnInt( ( jint )control );
}

KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetNumOfSupportedControls)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);
    int n = 0;

    javacall_amms_local_manager_get_controls( mgr, &n );

    KNI_ReturnInt( ( jint )n );
    
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetSupportedControlNames)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);
    int n = 0;
    javacall_amms_control_t *control = NULL;

    KNI_StartHandles( 1 );
    KNI_DeclareHandle( names );
    KNI_GetParameterAsObject( 1, names );
    control = javacall_amms_local_manager_get_controls( mgr, &n );

    controlsToJavaNamesArray( KNIPASSARGS control, n, names );

    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetSpectatorPeer)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);

    KNI_ReturnInt( ( jint ) 
        javacall_amms_local_manager_get_spectator( mgr ) );

}


KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetNumOf3DPlayerTypes)
{
    int n = 0;

    javacall_audio3d_get_supported_soundsource3d_player_types( &n );

    KNI_ReturnInt( ( jint )n );
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_GlobalMgrImpl_nGetSupportedSoundSource3DPlayerTypes)
{
    javacall_amms_local_manager_t *mgr = getNativePtr(KNIPASSARGS 0);
    int n = 0;
    int i = 0;
    const javacall_media_format_type *types;

    KNI_StartHandles(1);
    KNI_DeclareHandle( type_names );
    KNI_GetParameterAsObject( 1, type_names );
    types = javacall_audio3d_get_supported_soundsource3d_player_types( &n );

    if ( n != KNI_GetArrayLength( type_names ) )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nNumber of supported 3D Player Media Types is ambiguous\n");

        KNI_ReturnVoid();
    }

    for( i = 0; i < n; i++ )
    {
        const char *str = NULL;
        char str_buf[MAX_MIMETYPE_LEN];
        KNI_StartHandles( 1 );
        KNI_DeclareHandle( type_name );
        str = getMIMEFromMediaTypeEnum( types[i], str_buf, sizeof str_buf );
        
        if ( str == NULL )
        {
            KNI_ThrowNew("java/lang/RuntimeException", 
                "\nUnknown 3D Player Media Type supported\n");

            KNI_ReturnVoid();
        }

        KNI_NewStringUTF( str, type_name );
        
        KNI_SetObjectArrayElement( type_names, i, type_name );

        KNI_EndHandles();
    }

    KNI_EndHandles();

    KNI_ReturnVoid();
}