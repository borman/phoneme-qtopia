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
#include <javautil_unicode.h>

/// Numbers correspond to com.sun.amms.control.BaseImageFilter.FilterType
#define INVALID_FILTERTYPE                                          0
#define COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_CONVERTER   1
#define COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_FILTER      2
#define COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_TRANSFORM   3
#define COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_OVERLAY     4

static jchar* createJcString(jobject srcString) 
{
    jchar* pResult = NULL;
    if (!KNI_IsNullHandle(srcString)) {
        jsize jlen = KNI_GetStringLength(srcString);

        if (jlen != (jsize)-1)
            pResult = (jchar*)JAVAME_MALLOC(sizeof(jchar)*(jlen + 1));

        if (pResult) {
            KNI_GetStringRegion(srcString, 0, jlen, pResult);
            pResult[jlen] = 0;
        }
    }

    return pResult;
}

static jsize getLength(javacall_const_utf16_string str)
{
    javacall_result result;
    int jlength;

    result= javautil_unicode_utf16_ulength(str, &jlength);
	if (JAVACALL_SUCCEEDED(result))
		return jlength;
    else
		return 0;
}

static int getEffect(int filterType) 
{
    javacall_amms_image_filter_type effect;
    switch (filterType) {
        case COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_CONVERTER: 
            effect = javacall_amms_image_filter_converter; break;
        case COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_FILTER: 
            effect = javacall_amms_image_filter_effect; break;
        case COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_TRANSFORM:
            effect = javacall_amms_image_filter_transform; break;
        case COM_SUN_AMMS_CONTROL_BASEIMAGEFILTER_FILTERTYPE_OVERLAY:
            effect = javacall_amms_image_filter_overlay; break;
        default:
            effect = INVALID_FILTERTYPE; break;
    }
    return effect;
}

static javacall_result 
    createStringArray(KNIDECLARGS
        javacall_const_utf16_string* pStrings, int number, jobject jStrings) 
{
    javacall_result crResult = JAVACALL_OK;

    KNI_StartHandles(1);
    KNI_DeclareHandle(jtype);

    if (pStrings == NULL)
        number = 0;

    SNI_NewArray(SNI_STRING_ARRAY, number, jStrings);
    if (KNI_IsNullHandle(jStrings)) {
        crResult = JAVACALL_OUT_OF_MEMORY;
    } else {
        int i;
        for (i = 0; i < number; i++) {
            int jlength = getLength(pStrings[i]);
            {
                KNI_NewString(pStrings[i], jlength, jtype);
                if (KNI_IsNullHandle(jtype)) {
                    crResult = JAVACALL_OUT_OF_MEMORY;
                    break;
                }
                KNI_SetObjectArrayElement(jStrings, i, jtype);
            }
        }
    }

    KNI_EndHandles();
    return crResult;
}

static void checkAndRaiseException(KNIDECLARGS javacall_result jcresult)
{
    if (!JAVACALL_SUCCEEDED(jcresult))
        switch (jcresult) {
            case JAVACALL_OUT_OF_MEMORY: 
                KNI_ThrowNew(jsropOutOfMemoryError, NULL);
                break;
            case JAVACALL_NOT_IMPLEMENTED:
            case JAVACALL_INVALID_ARGUMENT: 
                KNI_ThrowNew(jsropIllegalArgumentException, NULL); 
                break;
            default:
                KNI_ThrowNew(jsropRuntimeException, NULL);
                break;
    }
}

//static native String[] nGetSupportedFormats(int FilterType, String inputMimeType)
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_NativeImageFilter_nGetSupportedFormats)
{
    javacall_result jresult;
    javacall_amms_image_filter_type effect = getEffect(KNI_GetParameterAsInt(1));

    KNI_StartHandles(2);
    KNI_DeclareHandle(jInput);
    KNI_DeclareHandle(jFormats);
    KNI_GetParameterAsObject(2, jInput);

    if (effect == INVALID_FILTERTYPE)
        jresult = JAVACALL_INVALID_ARGUMENT;
    else {
        int numberTypes;
        jchar* pInputType = createJcString(jInput);

        if (pInputType == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            javacall_const_utf16_string* pTypes =
                javacall_image_filter_get_supported_dest_mime_types(
                    effect, pInputType, &numberTypes);
            JAVAME_FREE(pInputType);

            jresult = createStringArray(KNIPASSARGS pTypes, numberTypes, jFormats);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jFormats);
}

// static native int nCreateFilter(int FilterType, String inputMimeType, String outputMimeType);
KNIEXPORT KNI_RETURNTYPE_INT 
KNIDECL(com_sun_amms_imageprocessor_NativeImageFilterHandle_nCreateFilter)
{
    javacall_result jresult;
    jint handle = 0;
    javacall_amms_image_filter_type effect = getEffect(KNI_GetParameterAsInt(1));

    KNI_StartHandles(2);
    KNI_DeclareHandle(jInput);
    KNI_DeclareHandle(jOutput);
    KNI_GetParameterAsObject(2, jInput);
    KNI_GetParameterAsObject(3, jOutput);

    if (effect == INVALID_FILTERTYPE)
        jresult = JAVACALL_INVALID_ARGUMENT;
    else {
        jchar* pInputType = createJcString(jInput);
        jchar* pOutputType = createJcString(jOutput);

        if ((pInputType == NULL) || (pOutputType == NULL))
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            handle = (jint)javacall_image_filter_create(effect, pInputType, 
                pOutputType);
            jresult = (handle != 0) ? JAVACALL_OK : JAVACALL_INVALID_ARGUMENT;
        }

        if (pInputType)
            JAVAME_FREE(pInputType);
        if (pOutputType)
            JAVAME_FREE(pOutputType);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandles();
    KNI_ReturnInt(handle);
}

// static native void nReleaseFilter(int filterHandle);
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_imageprocessor_NativeImageFilterHandle_nReleaseFilter)
{
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    javacall_result jresult = javacall_image_filter_destroy(pHandle);
    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_ReturnVoid();
}

// static native String[] nGetSupportedPresets(int filterHandle);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_EffectControlProxy_nGetSupportedPresets)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jPresets);

    {
        int number;
        javacall_const_utf16_string* pPresets =  
            javacall_image_filter_get_supported_presets(pHandle, &number);
        jresult = createStringArray(KNIPASSARGS pPresets, number, jPresets);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jPresets);
}

// static native void nSetPreset(int filterHandle, String presetName);
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_imageprocessor_EffectControlProxy_nSetPreset)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jPreset);
    KNI_GetParameterAsObject(2, jPreset);

    {
        jchar* pPresetName = createJcString(jPreset);
        jresult = javacall_image_filter_set_preset(pHandle, pPresetName);
        JAVAME_FREE(pPresetName);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

//static native void nSetSourceRect(int filterHandle, int x, int y, int width, int height);
KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_imageprocessor_TransformControlProxy_nSetSourceRect)
{
    void* pHandle = (void*)KNI_GetParameterAsInt(1);
    jint x =        KNI_GetParameterAsInt(2);
    jint y =        KNI_GetParameterAsInt(3);
    jint width =    KNI_GetParameterAsInt(4);
    jint height =   KNI_GetParameterAsInt(5);

    javacall_result jresult = 
        javacall_amms_image_filter_set_source_rect(
            pHandle, x, y, width, height);
    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_ReturnVoid();
}

//static native void nSetTargetSize(int filterHandle, int width, int height, int rotation);
KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_imageprocessor_TransformControlProxy_nSetTargetSize)
{
    void* pHandle = (void*)KNI_GetParameterAsInt(1);
    jint width = KNI_GetParameterAsInt(2);
    jint height = KNI_GetParameterAsInt(3);
    jint rotation = KNI_GetParameterAsInt(4);

    javacall_result jresult = 
        javacall_amms_image_filter_set_dest_size(pHandle, width, height, rotation);
    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_ReturnVoid();
}


//static native void nSetImage(int filterHandle, int[] rgbData, 
//            int width, int height, int x, int y, int transColor);
KNIEXPORT KNI_RETURNTYPE_VOID 
KNIDECL(com_sun_amms_imageprocessor_OverlayControlProxy_nSetImage)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);
    jint width =  KNI_GetParameterAsInt(3);
    jint height = KNI_GetParameterAsInt(4);
    jint x =      KNI_GetParameterAsInt(5);
    jint y =      KNI_GetParameterAsInt(6);
    jint transColor = KNI_GetParameterAsInt(7);

    KNI_StartHandles(1);
    KNI_DeclareHandle(rgbData);
    KNI_GetParameterAsObject(2, rgbData);
    {
        jsize dataLen = KNI_GetArrayLength(rgbData) * sizeof(jint);
        jint* rawData = (jint*)JAVAME_MALLOC(dataLen);
        if (NULL == rawData) {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        } else {
            KNI_GetRawArrayRegion(rgbData, 0, dataLen, (jbyte*)rawData);
            jresult = javacall_amms_image_filter_set_image(pHandle, 
                rawData, width, height, x, y, transColor);
            JAVAME_FREE(rawData);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);
    
    KNI_EndHandles();
    KNI_ReturnVoid();
}

//static native String[] nGetSupportedStrParams(int fHandle);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetSupportedStrParams)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jParams);

    {
        int number;
        javacall_const_utf16_string* pParams =
            javacall_image_filter_get_str_params(pHandle, &number);

        /// If pParams == NULL, createStringArray will create empty array
        jresult = createStringArray(KNIPASSARGS pParams, number, jParams);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jParams);
}

// static native String[] nGetSupportedIntParams(int fHandle);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetSupportedIntParams)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jParams);

    {
        int number;
        javacall_const_utf16_string* pParams =
            javacall_image_filter_get_int_params(pHandle, &number);

        /// If pParams == NULL, createStringArray will create empty array
        jresult = createStringArray(KNIPASSARGS pParams, number, jParams);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jParams);
}

// static native String[] nGetSupportedStrValues(int fHandle, String parameter);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetSupportedStrValues)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(2);
    KNI_DeclareHandle(jParamName);
    KNI_DeclareHandle(jValues);
    KNI_GetParameterAsObject(2, jParamName);

    {
        jchar* pParamName = createJcString(jParamName);

        if (pParamName == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            int number;
            javacall_const_utf16_string* pValues =
                javacall_image_filter_get_str_values(pHandle, pParamName, &number);

            if (pValues == NULL)
                jresult = JAVACALL_INVALID_ARGUMENT;
            else
                jresult = createStringArray(KNIPASSARGS pValues, number, jValues);
            JAVAME_FREE(pParamName);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jValues);
}

// static native int[] nGetSupportedIntValues(int fHandle, String parameter);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetSupportedIntValues)
{
    javacall_result jresult;
    int pos_min, pos_max; 
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(2);
    KNI_DeclareHandle(jParamName);
    KNI_DeclareHandle(jValues);
    KNI_GetParameterAsObject(2, jParamName);

    {
        jchar* pParamName = createJcString(jParamName);

        if (pParamName == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            jresult = 
                javacall_image_filter_get_int_values(
                    pHandle, pParamName, &pos_min, &pos_max);
            JAVAME_FREE(pParamName);
        }

        if (JAVACALL_SUCCEEDED(jresult)) {
            SNI_NewArray(SNI_INT_ARRAY, 2, jValues);
            if (KNI_IsNullHandle(jValues))
                jresult = JAVACALL_OUT_OF_MEMORY;
            else {
                KNI_SetIntArrayElement(jValues, 0, pos_min);
                KNI_SetIntArrayElement(jValues, 1, pos_max);
            }
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jValues);
}

// static native void nSetStrParameter(int fHandle, String parameter, String value);
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nSetStrParameter)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(2);
    KNI_DeclareHandle(jParamName);
    KNI_DeclareHandle(jNewValue);
    KNI_GetParameterAsObject(2, jParamName);
    KNI_GetParameterAsObject(3, jNewValue);

    {
        jchar* pParamName = createJcString(jParamName);
        jchar* pNewValue = createJcString(jNewValue);

        if ((pParamName == NULL) || (pNewValue == NULL))
            jresult = JAVACALL_OUT_OF_MEMORY;
        else 
            jresult = 
                javacall_image_filter_set_str_param_value(
                    pHandle, pParamName, pNewValue);

        if (pParamName)
            JAVAME_FREE(pParamName);
        if (pNewValue)
            JAVAME_FREE(pNewValue);
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

// static native void nSetIntParameter(int fHandle, String parameter, int value);
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nSetIntParameter)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);
    jint newValue = KNI_GetParameterAsInt(3);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jParamName);
    KNI_GetParameterAsObject(2, jParamName);

    {
        jchar* pParamName = createJcString(jParamName);
        if (pParamName == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            jresult = 
                javacall_image_filter_set_int_param_value(
                    pHandle, pParamName, newValue);
            JAVAME_FREE(pParamName);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

// static native String nGetStrParamValue(int fHandle, String parameter);
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetStrParamValue)
{
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(2);
    KNI_DeclareHandle(jvalue);
    KNI_DeclareHandle(jParamName);
    KNI_GetParameterAsObject(2, jParamName);

    {
        jchar* pParamName = createJcString(jParamName);
        if (pParamName == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            javacall_const_utf16_string strvalue = 
                javacall_image_filter_get_str_param_value(pHandle, pParamName);

            if (strvalue == NULL)
                jresult = JAVACALL_INVALID_ARGUMENT;
            else {
                jsize jlength = getLength(strvalue);
                KNI_NewString(strvalue, jlength, jvalue);
                if (KNI_IsNullHandle(jvalue)) 
                    jresult = JAVACALL_OUT_OF_MEMORY;
            }
            JAVAME_FREE(pParamName);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandlesAndReturnObject(jvalue);
}

// static native int nGetIntParamValue(int fHandle, String parameter);
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_imageprocessor_ImageFormatProxy_nGetIntParamValue)
{
    jint value = 0;
    javacall_result jresult;
    void* pHandle = (void*)KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(jParamName);
    KNI_GetParameterAsObject(2, jParamName);

    {
        jchar* pParamName = createJcString(jParamName);
        if (pParamName == NULL)
            jresult = JAVACALL_OUT_OF_MEMORY;
        else {
            jresult = 
                javacall_image_filter_get_int_param_value(
                    pHandle, pParamName, &value);
            JAVAME_FREE(pParamName);
        }
    }

    checkAndRaiseException(KNIPASSARGS jresult);

    KNI_EndHandles();
    KNI_ReturnInt(value);
}

