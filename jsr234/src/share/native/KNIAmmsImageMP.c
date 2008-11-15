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

#include <kni.h>
#include <sni.h>

#include <jsrop_exceptions.h>
#include <jsrop_memory.h>

#include <javacall_defs.h>
#include <javacall_multimedia_advanced.h>

// For memcpy
#include <string.h>     

//protected static native int nCreateProcessor(int isolateId, int mpID);
KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nCreateProcessor)
{
    jint isolateId = KNI_GetParameterAsInt(1);
    jint mpID = KNI_GetParameterAsInt(2);

    jint result;
    /// Compose global id
    javacall_int64 global_mpID = isolateId;
    global_mpID = (global_mpID << 32) | mpID;
    result = (jint)javacall_media_processor_create(global_mpID);

    KNI_ReturnInt(result);
}

// protected static native boolean addFilter(int mpHandle, int filterHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nAddFilter)
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    void* pFilter = (void*)KNI_GetParameterAsInt(2);
    jboolean result = JAVACALL_SUCCEEDED(
        javacall_media_processor_add_filter(pMP, pFilter));
    KNI_ReturnBoolean(result);
}

// protected static native boolean destroyProcessor(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nDestroyProcessor)
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    jboolean result = JAVACALL_SUCCEEDED(javacall_media_processor_destroy(pMP));
    KNI_ReturnBoolean(result);
}

// protected static native boolean nStart(int mpHandle, int[] data, int length, int width, int height);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nStart)
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    int length = KNI_GetParameterAsInt(3);
    int width = KNI_GetParameterAsInt(4);
    int height = KNI_GetParameterAsInt(5);
    jboolean result;

    KNI_StartHandles(1);
    KNI_DeclareHandle(data);

    KNI_GetParameterAsObject(2, data);
    {
        jsize dataLen = KNI_GetArrayLength(data) * sizeof(jint);
        jint* rawData = (jint*)JAVAME_MALLOC(dataLen);
        if (NULL == rawData) {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        } else {
            KNI_GetRawArrayRegion(data, 0, dataLen, (jbyte*)rawData);
            result = JAVACALL_SUCCEEDED(
                javacall_media_processor_set_input_rgb32(pMP, rawData, width, height));
            JAVAME_FREE(rawData);
        }
    }

    if (result)
        result = JAVACALL_SUCCEEDED(javacall_media_processor_start(pMP));

    KNI_EndHandles();
    KNI_ReturnBoolean(result);
}


// protected static native boolean nContinue(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nContinue)
{
    jboolean result = KNI_FALSE;
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    if (pMP != NULL)
        result = JAVACALL_SUCCEEDED(javacall_media_processor_start(pMP));
    KNI_ReturnBoolean(result);
}

// protected static native boolean nStop(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nStop)
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    jboolean result = JAVACALL_SUCCEEDED(javacall_media_processor_stop(pMP));
    KNI_ReturnBoolean(result);
}

// protected static native boolean nAbort(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nAbort) 
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    jboolean result = JAVACALL_SUCCEEDED(javacall_media_processor_abort(pMP));
    KNI_ReturnBoolean(result);
}

// protected static native boolean nReset(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_BOOLEAN 
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nReset)
{
    void* pMP = (void*)KNI_GetParameterAsInt(1);
    jboolean result = JAVACALL_SUCCEEDED(javacall_media_processor_reset(pMP));
    KNI_ReturnBoolean(result);
}

// protected static native byte[] nGetOutput(int mpHandle);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_BasicImageProcessor_nGetOutput)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(result);

    void* pMP = (void*)KNI_GetParameterAsInt(1);
    int length;
    const unsigned char* pData = javacall_media_processor_get_raw_output(pMP, &length);
    if ((length < 0) || (pData == NULL)) 
        KNI_ThrowNew(jsropRuntimeException, "Data is not ready");
    else {
        SNI_NewArray(SNI_BYTE_ARRAY, length, result);
        if (KNI_IsNullHandle(result)) {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        } else {
            KNI_SetRawArrayRegion(result, 0, length, (const jbyte*)pData);
        }
    }

    KNI_EndHandlesAndReturnObject(result);
}

