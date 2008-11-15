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

/**
 * @file
 *
 * Implementation of Image-PostProcessing of JSR234
 * Converter filter -> convert from RGB32 to JPEG or PNG, or ...
 */

#include <javacall_multimedia_advanced.h>
#include <javautil_unicode.h>

#include "multimedia_image_filter.h"
#include "multimedia_image_format.h"

#include <javacall_memory.h>

#include <jpegencoder.h>
#include "javautil_media.h"

/// For memset
#include <memory.h>

typedef struct javacall_amms_image_filter_cnv_s {
    javacall_amms_image_filter_s sIF;
    int                          quality;
} javacall_amms_image_filter_cnv_s;

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////
static const javacall_utf16
    QUALITY_PARAM_NAME[] = {'q','u','a','l','i','t','y',0};
static javacall_const_utf16_string
    INT_PARAMS[] = {QUALITY_PARAM_NAME};

#define DEFAULT_QUALITY     80
#define QUALITY_MIN_RANGE   1
#define QUALITY_MAX_RANGE   100

#define JFIF_HEADER_MAXIMUM_LENGTH  1024

static javacall_const_utf16_string* get_str_params(
    javacall_image_filter_handle filter_handle,
    /*OUT*/ int* number_of_params)
{
    return NULL;
}

static javacall_const_utf16_string* get_int_params(
    javacall_image_filter_handle filter_handle,
    /*OUT*/ int* number_of_params)
{
    if ((filter_handle == NULL) || (number_of_params == NULL))
        return NULL;

    *number_of_params = sizeof(INT_PARAMS)/sizeof(*INT_PARAMS);
    return INT_PARAMS;
}

static javacall_const_utf16_string* get_str_values(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name,
    /*OUT*/ int* number_of_values)
{
    return NULL;
}

static javacall_result get_int_values(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name,
    /*OUT*/ int* min_range, int* max_range)
{
    if ((filter_handle == NULL) || (param_name == NULL)
        || (min_range == NULL) || (max_range == NULL))
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_bool bEqual =
            javautil_unicode_equals(param_name, QUALITY_PARAM_NAME);
        if (bEqual == JAVACALL_FALSE)
            return JAVACALL_INVALID_ARGUMENT;
    }

    /// QUALITY_PARAM_NAME
    *min_range = QUALITY_MIN_RANGE;
    *max_range = QUALITY_MAX_RANGE;
    return JAVACALL_OK;
}

static javacall_result set_str_param_value(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name,
    javacall_const_utf16_string new_value)
{
    return JAVACALL_INVALID_ARGUMENT;
}

static javacall_result set_int_param_value(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name,
    int new_value)
{
    if ((filter_handle == NULL) || (param_name == NULL))
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_bool bEqual =
            javautil_unicode_equals(param_name, QUALITY_PARAM_NAME);
        if (bEqual == JAVACALL_FALSE)
            return JAVACALL_INVALID_ARGUMENT;
        if ((new_value < QUALITY_MIN_RANGE) || (QUALITY_MAX_RANGE < new_value))
            return JAVACALL_INVALID_ARGUMENT;
    }

    {
        /// QUALITY_PARAM_NAME
        javacall_amms_image_filter_cnv_s* pIFc =
            (javacall_amms_image_filter_cnv_s*)filter_handle;
        pIFc->quality = new_value;
    }

    return JAVACALL_OK;
}

static javacall_const_utf16_string get_str_param_value(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name)
{
    return NULL;
}

static javacall_result get_int_param_value(
    javacall_image_filter_handle filter_handle,
    javacall_const_utf16_string param_name,
    /*OUT*/ int* cur_value)
{
    if ((filter_handle == NULL) || (param_name == NULL) || (cur_value == NULL))
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_bool bEqual =
            javautil_unicode_equals(param_name, QUALITY_PARAM_NAME);
        if (bEqual == JAVACALL_FALSE)
            return JAVACALL_INVALID_ARGUMENT;
    }

    {
        /// QUALITY_PARAM_NAME
        javacall_amms_image_filter_cnv_s* pIFc =
            (javacall_amms_image_filter_cnv_s*)filter_handle;
        *cur_value = pIFc->quality;
    }
    return JAVACALL_OK;
}

static const javacall_amms_param_API paramAPI = {
    &get_str_params, &get_int_params,
    &get_str_values, &get_int_values,
    &set_str_param_value, &set_int_param_value,
    &get_str_param_value, &get_int_param_value
};

//////////////////////////////////////////////////////////////////////////
///
//////////////////////////////////////////////////////////////////////////

static javacall_const_utf16_string SupportedOutput[] =
    {JAVACALL_AMMS_MIME_JPEG_JPEG, JAVACALL_AMMS_MIME_PNG_PNG};
static const int SupportedOutputCnt =
                    sizeof(SupportedOutput)/sizeof(*SupportedOutput);

static javacall_result convertJPEG(javacall_image_filter_handle ifHandle,
                                   javacall_amms_frame* pInput,
                                   javacall_amms_frame** ppOutput)
{
    javacall_amms_frame_RGB32* pData;
    javacall_amms_image_filter_cnv_s* pIFc;
    int width, height;
    int outLength;

    javacall_amms_frame* pOutput;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((ifHandle == NULL) || (pInput == NULL) ||
                (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
        return JAVACALL_INVALID_ARGUMENT;

    pIFc = (javacall_amms_image_filter_cnv_s*)ifHandle;
    pData = (javacall_amms_frame_RGB32*)pInput;

    width = pData->width;
    height = pData->height;
    /// It's hard to suppose, how large will be jpeg image
    {
        int posSize;

        int nWidth = ((width+7)&(~7));
        int nHeight = ((height+7)&(~7));

        posSize = nWidth*nHeight*5 + JFIF_HEADER_MAXIMUM_LENGTH;

        pOutput = javacall_amms_create_frame_raw_in(posSize);
    }

    if (!pOutput)
        return JAVACALL_OUT_OF_MEMORY;

    /// Currently used JPEG_ENCODER_COLOR_BGRX, because the machine is LITTLE-ENDIAN,
    /// but on the BIG-ENDIAN use JPEG_ENCODER_COLOR_XRGB
    outLength = RGBToJPEG(pData->raw.bufferData, width, height,
                          pIFc->quality, (char*)pOutput->bufferData,
                          JPEG_ENCODER_COLOR_BGRX);

    if (outLength <= 0) {
        javacall_amms_release_frame(pOutput);
        return JAVACALL_FAIL;
    } else pOutput->bufferLength = outLength;

    *ppOutput = pOutput;
    return JAVACALL_OK;
}

static javacall_result convertPNG(javacall_image_filter_handle ifHandle,
                                  javacall_amms_frame* pInput,
                                  javacall_amms_frame** ppOutput)
{
    javacall_amms_frame_RGB32* pData;
    int width, height;
    int outLength;

    javacall_amms_frame* pOutput;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((ifHandle == NULL) || (pInput == NULL) ||
            (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
        return JAVACALL_INVALID_ARGUMENT;

    pData = (javacall_amms_frame_RGB32*)pInput;

    width = pData->width;
    height = pData->height;

    pOutput = javacall_amms_create_frame_raw_in(
                    javautil_media_get_png_size(width, height));
    if (!pOutput)
        return JAVACALL_OUT_OF_MEMORY;

    outLength = javautil_media_rgbX888_to_png((unsigned char*)pData->raw.bufferData,
                    (unsigned char*)pOutput->bufferData, width, height);

    if (outLength <= 0) {
        javacall_amms_release_frame(pOutput);
        return JAVACALL_FAIL;
    } else pOutput->bufferLength = outLength;

    *ppOutput = pOutput;
    return JAVACALL_OK;
}

typedef
    javacall_result (*convertorFnc)(javacall_image_filter_handle filter_handle,
                                   javacall_amms_frame* pInput,
                                   javacall_amms_frame** ppOutput);

static const convertorFnc convertorsPnt[] = {&convertJPEG, &convertPNG};

static javacall_image_filter_handle
        create(
            javacall_const_utf16_string source_type,
            javacall_const_utf16_string dest_type)
{
    javacall_amms_image_filter_cnv_s* pIFc;
    javacall_image_filter_handle result;

    int i;
    if (javautil_unicode_equals(source_type, JAVACALL_AMMS_MIME_RAW_RGBA8888)
                == JAVACALL_FALSE)
        return NULL;

    for (i = 0; i < SupportedOutputCnt; i++)
        if (javautil_unicode_equals(dest_type, SupportedOutput[i])
                            == JAVACALL_TRUE) {
            pIFc = javacall_malloc(sizeof(javacall_amms_image_filter_cnv_s));
            if (pIFc != NULL) {
                memset(pIFc, 0, sizeof(*pIFc));
                pIFc->quality = DEFAULT_QUALITY;
                result = (javacall_image_filter_handle)pIFc;
                result->destroy = &javacall_amms_if_simple_destroy;
                result->clone   = &javacall_amms_if_simple_clone;
                result->addtoMP = &javacall_amms_if_simple_add;
                result->process = convertorsPnt[i];
                if (i == 0) /// JPEG
                    result->pParamAPI = &paramAPI;
                result->sifSize = sizeof(javacall_amms_image_filter_cnv_s);
                return result;
            } else break; /// Out of memory
        }

    return NULL;
}

static javacall_const_utf16_string*
        get_supported_dest_mime_types(
            javacall_const_utf16_string source_mime_type,
            /*OUT*/ int *number_of_types)
{
    if (number_of_types == NULL)
        return NULL;

    *number_of_types = 0;

    if (javautil_unicode_equals(source_mime_type,
            JAVACALL_AMMS_MIME_RAW_RGBA8888) == JAVACALL_FALSE)
        return NULL;

    *number_of_types = SupportedOutputCnt;
    return SupportedOutput;
}

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////

const javacall_amms_if_factory javacall_amms_cnv_filter_factory = {
        &create,
        &javacall_amms_if_get_source_types_only_rgb32,
        &get_supported_dest_mime_types
};
