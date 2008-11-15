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
 * Buffer description: buffer holder and type of buffer content
 */

#ifndef __AMMS_MEDIA_PROCESSING_IMAGE_FORMAT_H__
#define __AMMS_MEDIA_PROCESSING_IMAGE_FORMAT_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <javacall_multimedia_advanced.h>

#define JAVACALL_AMMS_RGB32_PIXEL_SIZE 4
extern const javacall_utf16 JAVACALL_AMMS_MIME_RAW_RGBA8888[];
extern const javacall_utf16 JAVACALL_AMMS_MIME_JPEG_JPEG[];
extern const javacall_utf16 JAVACALL_AMMS_MIME_PNG_PNG[];


typedef enum javacall_amms_extendedType {
    JAVACALL_AMMS_ET_RAW_DATA    = 1,
    JAVACALL_AMMS_ET_RGB32_DATA
} javacall_amms_extendedType;

typedef enum javacall_amms_buffer_type {
    JAVACALL_AMMS_BUFFER_TYPE_INTERN = 1,
    JAVACALL_AMMS_BUFFER_TYPE_EXTERN
} javacall_amms_buffer_type;


/// AMMS_FRAME contains info about frame data and about frame type
/// refCnt = total reference to this frame
/// when refCnt == 0, frame deleted. Depend on javacall_amms_buffer_type
/// bufferData may by also deleted
typedef struct javacall_amms_frame {
    int                             refCnt;
    void*                           bufferData;
    int                             bufferLength;
    javacall_amms_buffer_type       bufferType;

    javacall_amms_extendedType      extType;
} javacall_amms_frame;

typedef struct javacall_amms_frame_RGB32 {
    javacall_amms_frame     raw;
    int                     width;
    int                     height;
} javacall_amms_frame_RGB32;

javacall_amms_frame* 
    javacall_amms_create_frame_rgb32_ex(int* pRGBdata, int width, int height);
javacall_amms_frame* 
    javacall_amms_create_frame_raw_ex(unsigned char* pRAWdata, int length);

javacall_amms_frame* javacall_amms_create_frame_rgb32_in(int width, int height);
javacall_amms_frame* javacall_amms_create_frame_raw_in(int source_length);

javacall_amms_frame* javacall_amms_addref_frame(javacall_amms_frame* pFrame);
javacall_amms_frame* javacall_amms_release_frame(javacall_amms_frame* pFrame);

#ifdef __cplusplus
}
#endif

#endif // __AMMS_MEDIA_PROCESSING_IMAGE_FORMAT_H__
