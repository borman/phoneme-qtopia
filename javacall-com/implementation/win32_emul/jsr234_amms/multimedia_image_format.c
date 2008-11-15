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
 * Buffer description: holder of buffer data and buffer type
 */

#include "multimedia_image_format.h"
#include <javacall_memory.h>


/// Used system specific InterlockedIncrement & InterlockedDecrement
#include <windows.h>

/// For memcpy
#include <memory.h>


/* Here used mime type with parameter <format>, which is used for more detailed
* representation of MIME-type. This format used like PARAM_VERSION_TYPE in
* Nokia Format Definitions for JSR-234.
*/

const javacall_utf16 JAVACALL_AMMS_MIME_RAW_RGBA8888[] = {
    'i','m','a','g','e','/','r','a','w',';',
        'f','o','r','m','a','t','=','r','g','b','a','8','8','8','8',0
};

const javacall_utf16 JAVACALL_AMMS_MIME_JPEG_JPEG[] = {
    'i','m','a','g','e','/','j','p','e','g',';',
        'f','o','r','m','a','t','=','J','P','E','G',0
};

/*
const javacall_utf16 JAVACALL_AMMS_MIME_JPEG_JPEG[] = {
    'i','m','a','g','e','/','j','p','e','g',';',
        'J','P','E','G',0
};
*/

const javacall_utf16 JAVACALL_AMMS_MIME_PNG_PNG[] = {
    'i','m','a','g','e','/','p','n','g',';',
        'f','o','r','m','a','t','=','P','N','G',0
};

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////

static javacall_amms_frame*
    create_frame_in(
        int sourceLength, int sSize, javacall_amms_extendedType eType)
{
    void* pnt = NULL;
    javacall_amms_frame* pFrame = NULL;

    if (sourceLength < 0)
        return NULL;

    pnt = javacall_malloc(sourceLength);
    if (pnt)  {
        pFrame = (javacall_amms_frame*)javacall_malloc(sSize);

        if (!pFrame)
            javacall_free(pnt);
    }

    if (pFrame) {
        pFrame->bufferData = pnt;
        pFrame->bufferLength = sourceLength;
        pFrame->bufferType = JAVACALL_AMMS_BUFFER_TYPE_INTERN;
        pFrame->extType = eType;
        pFrame->refCnt = 1;
    }

    return pFrame;
}

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////
javacall_amms_frame* javacall_amms_create_frame_rgb32_in(int width, int height)
{
    int source_length = width*height*JAVACALL_AMMS_RGB32_PIXEL_SIZE;
    javacall_amms_frame* result =
            create_frame_in(source_length, sizeof(javacall_amms_frame_RGB32),
                                           JAVACALL_AMMS_ET_RGB32_DATA);

    if (result) {
        javacall_amms_frame_RGB32* pFrame = (javacall_amms_frame_RGB32*)result;
        pFrame->width = width;
        pFrame->height = height;
    }
    return (javacall_amms_frame*)result;
}

javacall_amms_frame* javacall_amms_create_frame_raw_in(int source_length)
{
    return create_frame_in(source_length, sizeof(javacall_amms_frame),
                           JAVACALL_AMMS_ET_RAW_DATA);
}

javacall_amms_frame*
        javacall_amms_create_frame_raw_ex(unsigned char* pRAWdata, int length)
{
    javacall_amms_frame* result =
        javacall_amms_create_frame_raw_in(length);

    if (result)
        memcpy(result->bufferData, pRAWdata, result->bufferLength);
    return result;
}

javacall_amms_frame*
        javacall_amms_create_frame_rgb32_ex(int* pRGBdata, int width, int height)
{
    javacall_amms_frame* result =
        javacall_amms_create_frame_rgb32_in(width, height);

    if (result)
        memcpy(result->bufferData, pRGBdata, result->bufferLength);
    return result;
}

javacall_amms_frame* javacall_amms_addref_frame(javacall_amms_frame* pFrame)
{
    if (pFrame != NULL) {
        /// Here is preferable to use locked inc
        InterlockedIncrement(&(pFrame->refCnt));
    }
    return pFrame;
}

javacall_amms_frame* javacall_amms_release_frame(javacall_amms_frame* pFrame)
{
    if (pFrame != NULL) {
        /// Here is preferable to use locked dec
        int decs = InterlockedDecrement(&(pFrame->refCnt));
        if (decs == 0) {
            if (pFrame->bufferType == JAVACALL_AMMS_BUFFER_TYPE_INTERN)
                javacall_free(pFrame->bufferData);
            javacall_free(pFrame);
        }
    }
    return NULL;
}
