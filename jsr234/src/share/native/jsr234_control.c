/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

#include <stdlib.h>
#include <string.h>

#include "kni.h"

#include "KNICommon.h"
#include "javacall_multimedia_advanced.h"
#include "javacall_memory.h"
#include "jsr234_control.h"
#include "jsr234_nativePtr.h"
#include "javautil_unicode.h"

typedef struct
{
    const char*                         name;
    javacall_amms_control_type_enum_t   code;
} ctl_tbl_entry;

ctl_tbl_entry ctl_tbl[] = 
{
    { "javax.microedition.amms.control.audio3d.LocationControl",
        javacall_audio3d_eLocationControl },

    { "javax.microedition.amms.control.audio3d.OrientationControl",
        javacall_audio3d_eOrientationControl },

    { "javax.microedition.amms.control.audio3d.DistanceAttenuationControl",
        javacall_audio3d_eDistanceAttenuationControl },

    { "javax.microedition.amms.control.audioeffect.ReverbControl",
        javacall_music_eReverbControl },

    { "javax.microedition.amms.control.audioeffect.AudioVirtualizeControl", 
        javacall_amms_eAudioVirtualizerControl },

    { "javax.microedition.amms.control.audioeffect.ChorusControl", 
        javacall_amms_eChorusControl },

    { "javax.microedition.amms.control.audioeffect.EqualizerControl", 
        javacall_amms_eEqualizerControl },

    { "javax.microedition.amms.control.audioeffect.ReverbSourceControl", 
        javacall_amms_eReverbSourceControl },

    { "javax.microedition.media.control.VolumeControl", 
        javacall_amms_eVolumeControl }
};

#define CTL_TBL_N   ( sizeof( ctl_tbl ) / sizeof( ctl_tbl[ 0 ] ) )

javacall_amms_control_type_enum_t getControlTypeFromName( 
                                                   const char* type_name )
{
    unsigned int i;

    for( i = 0; i < CTL_TBL_N; i++ )
        if( 0 == strcmp( type_name, ctl_tbl[ i ].name ) )
            return ctl_tbl[ i ].code;

    return javacall_amms_eUnknownControl;
}

const char* getControlNameFromEnum( javacall_amms_control_type_enum_t type )
{
    unsigned int i;

    for( i = 0; i < CTL_TBL_N; i++ )
        if( ctl_tbl[ i ].code == type )
            return ctl_tbl[ i ].name;

    return NULL;
}

int getControlTypeFromArg( KNIDECLARGS javacall_amms_control_type_enum_t *type )
{
    char *buffer = NULL;
    int len = 0;

    *type = javacall_amms_eUnknownControl;
    KNI_StartHandles(1);
    KNI_DeclareHandle( type_name );
    KNI_GetParameterAsObject( 1, type_name );
    len = ( int )KNI_GetArrayLength( type_name );
    buffer = javacall_malloc( len + 1 );
    if( buffer == NULL )
    {
        KNI_ThrowNew("java/lang/OutOfMemoryError", 
            "Not enough memory to create a string buffer" );
        return 0;
    }

    KNI_GetRawArrayRegion( type_name, 0, len, buffer );
    buffer[ len ] = '\0';

    KNI_EndHandles();

    *type = getControlTypeFromName( buffer );
    javacall_free( buffer );

    return 1;
}

int controlsToJavaNamesArray( KNIDECLARGS javacall_amms_control_t controls[], int len,
                              jobject javaNamesArray )
{
    int i = 0;
    if ( len != KNI_GetArrayLength( javaNamesArray ) )
    {
        KNI_ThrowNew("java/lang/RuntimeException", 
            "\nNumber of supported Controls is ambiguous\n");

        return 0;
    }

    for( i = 0; i < len; i++)
    {
        const char* str = NULL;
        KNI_StartHandles(1);
        KNI_DeclareHandle( name );

        str = getControlNameFromEnum( controls[i].type );
        if ( str == NULL )
        {
            KNI_ThrowNew("java/lang/RuntimeException", 
                "\nControls supported contain a Control of unknown type\n");

            return 0;
        }

        KNI_NewStringUTF( str, name );
        
        KNI_SetObjectArrayElement( javaNamesArray, i, name );

        KNI_EndHandles();
    }

    return 1;
}

javacall_amms_control_t *getNativeControlPtr(KNIDECLARGS int dummy)
{
    dummy = dummy; // unused parameter

    return ( javacall_amms_control_t* )getNativeHandleFromField( KNIPASSARGS
        "_peer" );
}

javacall_result getUTF8StringFromParameter(KNIDECLARGS int par_num, char *str)
{
    javacall_result ret = JAVACALL_FAIL;
    javacall_int32  strLen = 0;
    jchar *jStr = NULL;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hStrParam);
    KNI_GetParameterAsObject(par_num, hStrParam);
    strLen = (javacall_int32)KNI_GetStringLength(hStrParam);
    
    if (strLen > 0) {
        jStr = (jchar *)javacall_malloc(sizeof(jchar) * (strLen + 1));
        if (jStr) {
            KNI_GetStringRegion(hStrParam, 0, strLen, jStr);
            if (JAVACALL_OK == javautil_unicode_utf16_to_utf8(jStr, strLen, str, 
                KNI_BUFFER_SIZE, &strLen)) {
                str[strLen] = 0;
                ret = JAVACALL_OK;
            }
            javacall_free(jStr);
        }
    } else {
        str[ 0 ] = '\0';
        ret = JAVACALL_OK;
    }

    KNI_EndHandles();
    return ret;
}

void setIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_int_func_ptr_t pFunc,
                                   const char* exceptionName,
                                   const char* exceptionText )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long value = ( long )KNI_GetParameterAsInt( 2 );

    if( pKniInfo != NULL )
    {
        res = ( *pFunc )( 
            pKniInfo->pNativeHandle, 
            value );
    }
    
    if( JAVACALL_OK != res && exceptionName != NULL )
    {
        KNI_ThrowNew( exceptionName, exceptionText );
    }
}

jint setGetIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_get_int_func_ptr_t pFunc,
                                   const char* exception_name,
                                   const char* exception_text )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long value = ( long )KNI_GetParameterAsInt( 2 );

    if( pKniInfo != NULL )
    {
        res = ( *pFunc )( pKniInfo->pNativeHandle, &value );
    }

    if( JAVACALL_OK != res )
    {
        KNI_ThrowNew( exception_name, exception_text );
    }
    return ( jint )value ;
}


jboolean getBoolWithTypicalJavacallFunc( KNIDECLARGS 
    typical_jc_get_bool_func_ptr_t pFunc )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_bool value = JAVACALL_FALSE;

    if( pKniInfo != NULL )
    {
        ( *pFunc )( pKniInfo->pNativeHandle, &value );
    }

    return ( JAVACALL_TRUE == value ? KNI_TRUE : KNI_FALSE );
}

javacall_result setBoolWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_bool_func_ptr_t
                                                pFunc )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean value = KNI_GetParameterAsBoolean( 2 );

    if( pKniInfo != NULL )
    {
        res = ( *pFunc )( 
            pKniInfo->pNativeHandle, 
            KNI_TRUE == value ? JAVACALL_TRUE : JAVACALL_FALSE );
    }
    else
    {
        res = JAVACALL_INVALID_ARGUMENT;
    }
    
    return res;
}
                                   
jint getIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_get_int_func_ptr_t pFunc )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    long value = -1;

    if( pKniInfo != NULL )
    {
        ( *pFunc )( pKniInfo->pNativeHandle, &value );
    }

    return ( jint )value;
}

void getStringWithTypicalJavacallFunc( KNIDECLARGS typical_jc_get_string_func_ptr_t pFunc,
    jstring hStr )
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[ KNI_BUFFER_SIZE ];

    modulation[ 0 ] = 0;
    
    if( pKniInfo != NULL )
    {
        res = ( *pFunc )( 
            pKniInfo->pNativeHandle,
                       modulation,
                       sizeof( modulation ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( modulation, hStr );
    }
    else
    {
        KNI_ReleaseHandle( hStr );
    }
    
}

void setStringWithTypicalJavacallFunc(KNIDECLARGS typical_jc_set_string_func_ptr_t pFunc,
    const char* exception_name, const char* exception_text) {
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char value[KNI_BUFFER_SIZE + 1];

    if (pKniInfo != NULL &&
        (JAVACALL_OK == getUTF8StringFromParameter(KNIPASSARGS 2, value))) {
        res = (*pFunc)(pKniInfo->pNativeHandle, value);
    }

    if (JAVACALL_OK != res && exception_name != NULL) {
        KNI_ThrowNew(exception_name, exception_text);
    }
}

void getUTF16StringWithTypicalJavacallFunc(KNIDECLARGS
    typical_jc_get_utf16string_func_ptr_t pFunc, jstring hStr) {
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jchar modulation[KNI_BUFFER_SIZE];
    javacall_int32 len;

    if (pKniInfo != NULL) {
        res = (*pFunc)(pKniInfo->pNativeHandle, modulation, KNI_BUFFER_SIZE);
    }
    if (JAVACALL_OK == res) {
        res = javautil_unicode_utf16_ulength(modulation, &len);
    }

    if (JAVACALL_OK == res) {
        KNI_NewString(modulation, len, hStr);
    } else {
        KNI_ReleaseHandle(hStr);
    }
    
}

void setUTF16StringWithTypicalJavacallFunc(KNIDECLARGS
    typical_jc_set_utf16string_func_ptr_t pFunc,
    const char* exception_name, const char* exception_text) {
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;

    if (pKniInfo != NULL) {
        jsize strLen = 0;
        jchar *jStr = NULL;

        KNI_StartHandles(1);
        KNI_DeclareHandle(hStrParam);
        KNI_GetParameterAsObject(2, hStrParam);
        strLen = KNI_GetStringLength(hStrParam);

        if (strLen > 0) {
            jStr = (jchar *)javacall_malloc(sizeof(jchar) * (strLen + 1));
            if (jStr) {
                KNI_GetStringRegion(hStrParam, 0, strLen, jStr);
                jStr[strLen] = 0;
                res = (*pFunc)(pKniInfo->pNativeHandle, jStr);
                javacall_free(jStr);
            }
        }

        KNI_EndHandles();
    }

    if (JAVACALL_OK != res && exception_name != NULL) {
        KNI_ThrowNew(exception_name, exception_text);
    }
}
