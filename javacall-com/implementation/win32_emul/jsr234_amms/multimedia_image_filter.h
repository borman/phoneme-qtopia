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

#ifndef __AMMS_MEDIA_PROCESSING_IMAGE_FILTER_H__
#define __AMMS_MEDIA_PROCESSING_IMAGE_FILTER_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <javacall_multimedia_advanced.h>
#include "multimedia_image_format.h"

typedef struct javacall_amms_if_factory {

    javacall_image_filter_handle (*create)(
        javacall_const_utf16_string source_mime_type, 
        javacall_const_utf16_string dest_mime_type);

    javacall_const_utf16_string* (*get_supported_source_mime_types)(
        /*OUT*/ int *number_of_types);

    javacall_const_utf16_string* (*get_supported_dest_mime_types)(
        javacall_const_utf16_string source_mime_type, 
        /*OUT*/ int *number_of_types);

} javacall_amms_if_factory;

typedef struct javacall_amms_preset_API {

    javacall_const_utf16_string* (*get_supported_presets)(
        javacall_handle filter_handle, 
        /*OUT*/ int *number_of_presets);

    javacall_result (*set_preset)(
        javacall_handle filter_handle, 
        javacall_const_utf16_string preset_name);

    javacall_const_utf16_string (*get_preset)(
        javacall_handle filter_handle);

} javacall_amms_preset_API;

typedef struct javacall_amms_transform_API {

    javacall_result (*set_source_rect)(
        javacall_image_filter_handle filter_handle, 
        int x, int y, 
        int width, int height);

    javacall_result (*set_target)(
        javacall_image_filter_handle filter_handle, 
        int width, int height, int rotation);

} javacall_amms_transform_API;

typedef struct javacall_amms_overlay_API {

    javacall_result (*set_image)(
        javacall_image_filter_handle filter_handle,
        int* pRGBdata, int width, int height,
        int x, int y, int transparencyColor);

} javacall_amms_overlay_API;

typedef struct javacall_amms_param_API {
    javacall_const_utf16_string* (*get_str_params)(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params);

    javacall_const_utf16_string* (*get_int_params)(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params);

    javacall_const_utf16_string* (*get_str_values)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* number_of_values);

    javacall_result (*get_int_values)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* min_range, int* max_range);

    javacall_result (*set_str_param_value)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        javacall_const_utf16_string new_value);

    javacall_result (*set_int_param_value)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        int new_value);

    javacall_const_utf16_string (*get_str_param_value)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name);

    javacall_result (*get_int_param_value)(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* cur_value);

} javacall_amms_param_API;

typedef 
    javacall_result (*javacall_amms_mp_add)(
         javacall_media_processor_handle mpHandle, 
         javacall_image_filter_handle ifHandle,
         javacall_bool  changeOwner);

typedef struct javacall_amms_image_filter_s {
    javacall_result (*destroy)(
        javacall_image_filter_handle filter_handle);

    javacall_result (*addtoMP)(
        javacall_image_filter_handle filter_handle,
        javacall_amms_mp_add func_mp_add,
        javacall_media_processor_handle mpHandle);

    javacall_image_filter_handle (*clone)(
        javacall_image_filter_handle filter_handle);

    javacall_result (*process)(
        javacall_image_filter_handle filter_handle,
        javacall_amms_frame* pInput, 
        javacall_amms_frame** ppOutput);


    const javacall_amms_param_API*     pParamAPI; 
    const javacall_amms_preset_API*    pPresetAPI;
    const javacall_amms_transform_API* pTransformAPI;
    const javacall_amms_overlay_API*   pOverlayAPI;
    int                                sifSize;
} javacall_amms_image_filter_s;

javacall_const_utf16_string* 
    javacall_amms_if_get_source_types_only_rgb32(
        /*OUT*/ int *number_of_types);

javacall_const_utf16_string* 
    javacall_amms_if_get_dest_types_only_rgb32(
        javacall_const_utf16_string source_mime_type, 
        /*OUT*/ int *number_of_types);

javacall_result 
    javacall_amms_if_simple_destroy(
        javacall_image_filter_handle filter_handle);

javacall_image_filter_handle 
    javacall_amms_if_simple_clone(
        javacall_image_filter_handle filter_handle);

javacall_result 
    javacall_amms_if_simple_add(
        javacall_image_filter_handle filter_handle,
        javacall_amms_mp_add func_mp_add,
        javacall_media_processor_handle mpHandle);

#ifdef __cplusplus
}
#endif

#endif // __AMMS_MEDIA_PROCESSING_IMAGE_FILTER_H__
