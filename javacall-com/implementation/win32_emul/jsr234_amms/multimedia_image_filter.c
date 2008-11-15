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

/**
 * @file
 *
 * Implementation of Image-PostProcessing of JSR234
 * Image filters APIs and common functions
 */

#include <javacall_multimedia_advanced.h>
#include <javautil_unicode.h>
#include <javacall_memory.h>
#include "multimedia_image_filter.h"

/// For memcpy
#include <memory.h>

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////
static javacall_const_utf16_string
    SupportedOnlyRGB32[] = {JAVACALL_AMMS_MIME_RAW_RGBA8888,
		                    JAVACALL_AMMS_MIME_JPEG_JPEG,
		                    JAVACALL_AMMS_MIME_PNG_PNG};
static const int
    SupportedOnlyRGB32size =
        sizeof(SupportedOnlyRGB32)/(sizeof(*SupportedOnlyRGB32));

javacall_const_utf16_string*
    javacall_amms_if_get_source_types_only_rgb32(/*OUT*/ int *number_of_types)
{
    if (number_of_types == NULL)
        return NULL;

    *number_of_types = SupportedOnlyRGB32size;
    return SupportedOnlyRGB32;
}

javacall_const_utf16_string*
    javacall_amms_if_get_dest_types_only_rgb32(
        javacall_const_utf16_string source_mime_type,
        /*OUT*/ int *number_of_types)
{
    if (number_of_types == NULL)
        return NULL;

    *number_of_types = 0;
    {
        javacall_bool bEqual =
            javautil_unicode_equals(source_mime_type, SupportedOnlyRGB32[0]);
        if (bEqual == JAVACALL_FALSE)
            return NULL;
    }

    *number_of_types = SupportedOnlyRGB32size;
    return SupportedOnlyRGB32;
}

javacall_result javacall_amms_if_simple_destroy(
                    javacall_image_filter_handle filter_handle)
{
    javacall_free(filter_handle);
    return JAVACALL_OK;
}

javacall_image_filter_handle
    javacall_amms_if_simple_clone(javacall_image_filter_handle filter_handle)

{
    javacall_image_filter_handle result;
    if (filter_handle == NULL)
        return NULL;

    result = (javacall_image_filter_handle)
        javacall_malloc(filter_handle->sifSize);
    if (result)
        memcpy(result, filter_handle, filter_handle->sifSize);
    return result;
}

javacall_result
    javacall_amms_if_simple_add(
        javacall_image_filter_handle filter_handle,
        javacall_amms_mp_add func_mp_add,
        javacall_media_processor_handle mpHandle)

{
    return func_mp_add(mpHandle, filter_handle, JAVACALL_FALSE);
}

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////
extern const javacall_amms_if_factory javacall_amms_cnv_filter_factory;
extern const javacall_amms_if_factory javacall_amms_effect_filter_factory;
extern const javacall_amms_if_factory javacall_amms_transform_filter_factory;
extern const javacall_amms_if_factory javacall_amms_overlay_filter_factory;

static const javacall_amms_if_factory* ifFactoryList[] = {
                    &javacall_amms_cnv_filter_factory,
                    &javacall_amms_effect_filter_factory,
                    &javacall_amms_transform_filter_factory,
                    &javacall_amms_overlay_filter_factory};

static const int ifFactoryListLength =
                    sizeof(ifFactoryList) / sizeof(*ifFactoryList);


static int getFilterIndex(javacall_amms_image_filter_type filter_type)
{
    int index;
    switch(filter_type) {
        case javacall_amms_image_filter_converter: index = 0; break;
        case javacall_amms_image_filter_effect: index = 1; break;
        case javacall_amms_image_filter_transform: index = 2; break;
        case javacall_amms_image_filter_overlay: index = 3; break;
        default: index = -1; break;
    }
    if (index >= ifFactoryListLength)
        index = -1;
    return index;
}

//////////////////////////////////////////////////////////////////////////
/// Realization of javacall_multimedia_advanced.h
//////////////////////////////////////////////////////////////////////////
javacall_const_utf16_string*
    javacall_image_filter_get_supported_dest_mime_types(
        javacall_amms_image_filter_type filter_type,
        javacall_const_utf16_string source_mime_type,
        /*OUT*/ int *number_of_types)
{
    int index = getFilterIndex(filter_type);
    return (index < 0) ? NULL :
        ifFactoryList[index]->get_supported_dest_mime_types(
                                 source_mime_type, number_of_types);
}

javacall_const_utf16_string*
    javacall_image_filter_get_supported_source_mime_types(
        javacall_amms_image_filter_type filter_type,
        /*OUT*/ int *number_of_types)
{
    int index = getFilterIndex(filter_type);
    return (index < 0) ? NULL :
        ifFactoryList[index]->get_supported_source_mime_types(number_of_types);
}

javacall_image_filter_handle
    javacall_image_filter_clone(javacall_image_filter_handle filter_handle)
{
    if (filter_handle == NULL)
        return NULL;

    if (filter_handle->clone)
        return filter_handle->clone(filter_handle);
    else
        return NULL;
}

javacall_result
    javacall_image_filter_destroy(javacall_image_filter_handle filter_handle)
{
    javacall_result
        (*pntDestroy)(javacall_image_filter_handle) = filter_handle->destroy;

    if (pntDestroy)
        return pntDestroy(filter_handle);
    else
        return JAVACALL_FAIL;
}

javacall_image_filter_handle
    javacall_image_filter_create(
        javacall_amms_image_filter_type filter_type,
        javacall_const_utf16_string source_mime_type,
        javacall_const_utf16_string dest_mime_type)
{
    int index = getFilterIndex(filter_type);

    return (index < 0) ? NULL :
        ifFactoryList[index]->create(source_mime_type, dest_mime_type);
}

javacall_result
    javacall_image_filter_set_preset(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string preset_name)
{
    const javacall_amms_preset_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pPresetAPI;
    if (pAPI)
        return pAPI->set_preset(filter_handle, preset_name);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

javacall_const_utf16_string
    javacall_image_filter_get_preset(javacall_image_filter_handle filter_handle)
{
    const javacall_amms_preset_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    pAPI = filter_handle->pPresetAPI;
    if (pAPI)
        return pAPI->get_preset(filter_handle);
    else
        return NULL;
}

javacall_const_utf16_string*
    javacall_image_filter_get_supported_presets(
        javacall_image_filter_handle filter_handle,
        /*OUT*/ int *number_of_presets)
{
    const javacall_amms_preset_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    if (number_of_presets != NULL)
        *number_of_presets = 0;

    pAPI = filter_handle->pPresetAPI;
    if (pAPI)
        return pAPI->get_supported_presets(filter_handle, number_of_presets);
    else
        return NULL;
}

javacall_result javacall_amms_image_filter_set_source_rect(
                   javacall_image_filter_handle filter_handle,
                   int x, int y,
                   int width, int height)
{
    const javacall_amms_transform_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pTransformAPI;
    if (pAPI)
        return pAPI->set_source_rect(filter_handle, x, y, width, height);
    else
        return JAVACALL_INVALID_ARGUMENT;

}

javacall_result javacall_amms_image_filter_set_dest_size(
              javacall_image_filter_handle filter_handle,
              int width, int height, int rotation)
{
    const javacall_amms_transform_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pTransformAPI;
    if (pAPI)
        return pAPI->set_target(filter_handle, width, height, rotation);
    else
        return JAVACALL_INVALID_ARGUMENT;
}


javacall_result javacall_amms_image_filter_set_image(
                    javacall_image_filter_handle filter_handle,
                    int* pRGBdata, int width, int height,
                    int x, int y, int transparencyColor)
{
    const javacall_amms_overlay_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pOverlayAPI;
    if (pAPI)
        return pAPI->set_image(filter_handle, pRGBdata,
                                width, height,
                                x, y, transparencyColor);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

javacall_const_utf16_string*
    javacall_image_filter_get_str_params(
        javacall_image_filter_handle filter_handle,
        /*OUT*/ int* number_of_params)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_str_params(filter_handle, number_of_params);
    else
        return NULL;
}

javacall_const_utf16_string*
    javacall_image_filter_get_int_params(
        javacall_image_filter_handle filter_handle,
        /*OUT*/ int* number_of_params)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_int_params(filter_handle, number_of_params);
    else
        return NULL;
}

javacall_const_utf16_string*
    javacall_image_filter_get_str_values(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name,
        /*OUT*/ int* number_of_values)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_str_values(filter_handle, param_name,
                                    number_of_values);
    else
        return NULL;
}

javacall_result
    javacall_image_filter_get_int_values(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name,
        /*OUT*/ int* min_range, int* max_range)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_int_values(filter_handle, param_name,
                                    min_range, max_range);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

javacall_result
    javacall_image_filter_set_str_param_value(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name,
        javacall_const_utf16_string new_value)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->set_str_param_value(filter_handle, param_name, new_value);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

javacall_result
    javacall_image_filter_set_int_param_value(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name,
        int new_value)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->set_int_param_value(filter_handle, param_name, new_value);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

javacall_const_utf16_string
    javacall_image_filter_get_str_param_value(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return NULL;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_str_param_value(filter_handle, param_name);
    else
        return NULL;
}

javacall_result
    javacall_image_filter_get_int_param_value(
        javacall_image_filter_handle filter_handle,
        javacall_const_utf16_string param_name,
        /*OUT*/ int* cur_value)
{
    const javacall_amms_param_API* pAPI;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pAPI = filter_handle->pParamAPI;
    if (pAPI)
        return pAPI->get_int_param_value(filter_handle, param_name, cur_value);
    else
        return JAVACALL_INVALID_ARGUMENT;
}

