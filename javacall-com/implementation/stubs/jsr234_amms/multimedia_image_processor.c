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
 * Media processor logic
*/

#include <javacall_multimedia.h>
#include <javacall_multimedia_advanced.h>

javacall_media_processor_handle 
    javacall_media_processor_create(javacall_int64  media_processor_id)
{
    (void)media_processor_id;
    return NULL;
}

javacall_result 
    javacall_media_processor_reset(
        javacall_media_processor_handle media_processor_handle)
{
    (void)media_processor_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_add_filter(
        javacall_media_processor_handle media_processor_handle, 
        javacall_image_filter_handle filter_handle)
{
    (void)media_processor_handle;
    (void)filter_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_destroy(
        javacall_media_processor_handle media_processor_handle)
{
    (void)media_processor_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_start(
        javacall_media_processor_handle media_processor_handle)
{
    (void)media_processor_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_stop(
            javacall_media_processor_handle media_processor_handle)
{
    (void)media_processor_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_abort(
        javacall_media_processor_handle media_processor_handle)
{
    (void)media_processor_handle;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_set_input_rgb32(
        javacall_media_processor_handle media_processor_handle,
        int* pRGBdata, int width, int height)
{
    (void)media_processor_handle;
    (void)pRGBdata;
    (void)width;
    (void)height;
    return JAVACALL_NOT_IMPLEMENTED;
}

javacall_result 
    javacall_media_processor_set_input_raw(
        javacall_media_processor_handle media_processor_handle,
        unsigned char* pRAWdata, int length)
{
    (void)media_processor_handle;
    (void)pRAWdata;
    (void)length;
    return JAVACALL_NOT_IMPLEMENTED;
}

const unsigned char* 
    javacall_media_processor_get_raw_output(
        javacall_media_processor_handle media_processor_handle, 
        /*OUT*/ int* result_length)
{
    (void)media_processor_handle;
    (void)result_length;
    return NULL;
}

