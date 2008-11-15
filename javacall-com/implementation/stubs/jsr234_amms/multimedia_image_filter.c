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
 * Stubs implementation of Image-PostProcessing of JSR234
 * Image filters APIs and common functions
 */

#include <javacall_multimedia.h>
#include <javacall_multimedia_advanced.h>

javacall_const_utf16_string* 
    javacall_image_filter_get_supported_source_mime_types(
        javacall_amms_image_filter_type filter_type, 
        /*OUT*/ int *number_of_types)
{
    (void)filter_type;
    (void)number_of_types;
    return NULL;
}

javacall_const_utf16_string* 
    javacall_image_filter_get_supported_dest_mime_types(
        javacall_amms_image_filter_type filter_type, 
        javacall_const_utf16_string source_mime_type, 
        /*OUT*/ int *number_of_types)
{
    (void)filter_type;
    (void)source_mime_type;
    (void)number_of_types;
    return NULL;
}

javacall_const_utf16_string* 
    javacall_image_filter_get_supported_presets(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int *number_of_presets)
{
    (void)filter_handle;
    (void)number_of_presets;
    return NULL;
}

javacall_result
    javacall_image_filter_set_preset(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string preset_name)
{
    (void)filter_handle;
    (void)preset_name;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_const_utf16_string 
    javacall_image_filter_get_preset(
        javacall_image_filter_handle filter_handle)
{
    (void)filter_handle;
    return NULL;
}

javacall_result 
    javacall_amms_image_filter_set_source_rect(
        javacall_image_filter_handle filter_handle, 
        int x, int y, 
        int width, int height)
{
    (void)filter_handle;
    (void)x;
    (void)y;
    (void)width;
    (void)height;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_amms_image_filter_set_dest_size(
        javacall_image_filter_handle filter_handle, 
        int width, int height, int rotation)
{
    (void)filter_handle;
    (void)width;
    (void)height;
    (void)rotation;
    return JAVACALL_NOT_IMPLEMENTED;
}


javacall_result 
    javacall_amms_image_filter_set_image(
        javacall_image_filter_handle filter_handle,
        int* pRGBdata, int width, int height,
        int x, int y, int transparencyColor)
{
    (void)filter_handle;
    (void)pRGBdata;
    (void)width;
    (void)height;
    (void)x;
    (void)y;
    (void)transparencyColor;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_const_utf16_string* 
    javacall_image_filter_get_str_params(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params)
{
    (void)filter_handle;
    (void)number_of_params;
    return NULL;
}

javacall_const_utf16_string*
    javacall_image_filter_get_int_params(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params)
{
    (void)filter_handle;
    (void)number_of_params;
    return NULL;
}

javacall_const_utf16_string*
    javacall_image_filter_get_str_values(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* number_of_values)
{
    (void)filter_handle;
    (void)param_name;
    (void)number_of_values;
    return NULL;
}

javacall_result
    javacall_image_filter_get_int_values(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* min_range, int* max_range)
{
    (void)filter_handle;
    (void)param_name;
    (void)min_range;
    (void)max_range;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result
    javacall_image_filter_set_str_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        javacall_const_utf16_string new_value)
{
    (void)filter_handle;
    (void)param_name;
    (void)new_value;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result
    javacall_image_filter_set_int_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        int new_value)
{
    (void)filter_handle;
    (void)param_name;
    (void)new_value;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_const_utf16_string
    javacall_image_filter_get_str_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name)
{
    (void)filter_handle;
    (void)param_name;
    return NULL;
}

javacall_result
    javacall_image_filter_get_int_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* cur_value)
{
    (void)filter_handle;
    (void)param_name;
    (void)cur_value;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_image_filter_handle 
    javacall_image_filter_create(
        javacall_amms_image_filter_type filter_type,
        javacall_const_utf16_string source_mime_type, 
        javacall_const_utf16_string dest_mime_type)
{
    (void)filter_type;
    (void)source_mime_type;
    (void)dest_mime_type;
    return NULL;
}

javacall_image_filter_handle 
    javacall_image_filter_clone(javacall_image_filter_handle filter_handle)
{
    (void)filter_handle;
    return NULL;
}

javacall_result 
    javacall_image_filter_destroy(javacall_image_filter_handle filter_handle)
{
    (void)filter_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}
