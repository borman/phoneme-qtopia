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
 * Overlay filter -> blend one image over another
 */

#include <javacall_multimedia_advanced.h>
#include <javautil_unicode.h>

#include "multimedia_image_filter.h"
#include "multimedia_image_format.h"

#include <javacall_memory.h>

/// For memcpy & memset
#include <memory.h>

#define min(a, b) ((a) < (b) ? (a) : (b))
#define max(a, b) ((a) > (b) ? (a) : (b))

#define FULLY_OPAQUE    1 << 4
#define ALPHA_ENABLED   1 << 3

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////

typedef struct javacall_amms_image_filter_overlay_s {
    javacall_amms_image_filter_s sIF;
    javacall_amms_frame_RGB32*       pPict;

    int     pos_x, pos_y;
    int     tColor;
    javacall_bool    mTransMode;
} javacall_amms_image_filter_overlay_s;

static javacall_image_filter_handle clone(javacall_image_filter_handle filter_handle)
{
    javacall_image_filter_handle result;
    javacall_amms_image_filter_overlay_s* pIFo;
    if (filter_handle == NULL)
        return NULL;

    result = (javacall_image_filter_handle)
        javacall_malloc(filter_handle->sifSize);
    if (result)
        memcpy(result, filter_handle, filter_handle->sifSize);

    pIFo = (javacall_amms_image_filter_overlay_s*)result;
    pIFo->pPict = (javacall_amms_frame_RGB32*)
        javacall_amms_addref_frame(&pIFo->pPict->raw);
    return result;
}

static javacall_result set_image(
                javacall_image_filter_handle filter_handle,
                int* pRGBdata, int width, int height,
                int x, int y, int transparencyColor)
{
    javacall_amms_image_filter_overlay_s* pIFo;
    if ((filter_handle == NULL) || (pRGBdata == NULL))
        return JAVACALL_INVALID_ARGUMENT;
    pIFo = (javacall_amms_image_filter_overlay_s*)filter_handle;

    pIFo->pPict = (javacall_amms_frame_RGB32*)
        javacall_amms_create_frame_rgb32_ex(pRGBdata, width, height);

    if (pIFo->pPict == NULL)
        return JAVACALL_OUT_OF_MEMORY;

    pIFo->pos_x = x;
    pIFo->pos_y = y;
    pIFo->mTransMode = transparencyColor >> 24;
    pIFo->tColor = transparencyColor << 8;

    return JAVACALL_OK;
}


static const javacall_amms_overlay_API overlayAPI = { &set_image };

static void processCopy(int *pData, int pitchData, int *pPict, int pitchPict, 
                         int sizex, int sizey) 
{
    int i;

    for (i = 0; i < sizey; i++) {
        memcpy(pData, pPict, sizex*sizeof(int));
        pData += pitchData;
        pPict += pitchPict;
    }

} 

static void processTrans(int *pData, int pitchData, int *pPict, int pitchPict, 
                         int sizex, int sizey, int tColor) 
{
    int i, j;

    for (i = 0; i < sizey; i++) {
        for (j = 0; j < sizex; j++) {
            int px = pPict[j];
            if ((px << 8) != tColor) {
                pData[j] = px;
            }
        }

        pData += pitchData;
        pPict += pitchPict;
    }
} 

static void processBlend(int *pData, int pitchData, int *pPict, int pitchPict, 
                         int sizex, int sizey) 
{
    typedef unsigned int uint;
    int i, j;

    for (i = 0; i < sizey; i++) {
        for (j = 0; j < sizex; j++) {
            uint pxA = pPict[j];
            uint alA = pxA >> 24;

            if (alA == 0xFF) {
                pData[j] = pxA;
            } else if (alA != 0) {
                uint pxB = pData[j];
                uint alB =  pxB >> 24;

                /// put pixel A over pixel B
                uint resAl = (((alA + alB) << 8) - alA*alB);
                uint result = resAl >> 8;

                if (result == 0)
                    pData[j] = 0;
                else {
                    /// Division may be changed to table division on 1..256
                    uint fn = (alA << 16) / resAl;

                    {
                        int c1A = (pxA >> 16) & 0xFF;
                        int c1B = (pxB >> 16) & 0xFF;
                        result = result << 8;
                        result |= c1B + (((c1A - c1B) * fn) >> 8); 
                    }
                    {
                        int c2A = (pxA >>  8) & 0xFF;
                        int c2B = (pxB >>  8) & 0xFF;
                        result = result << 8;
                        result |= c2B + (((c2A - c2B) * fn) >> 8); 
                    }
                    {
                        int c3A = (pxA      ) & 0xFF;
                        int c3B = (pxB      ) & 0xFF;
                        result = result << 8;
                        result |= c3B + (((c3A - c3B) * fn) >> 8); 
                    }

                    pData[j] = result;
                }
            }
        }

        pData += pitchData;
        pPict += pitchPict;
    }
}

static javacall_result process(javacall_image_filter_handle filter_handle,
                               javacall_amms_frame* pInput, 
                               javacall_amms_frame** ppOutput) 
{
    javacall_amms_frame_RGB32* pData;
    int width, height;

    javacall_amms_frame* pOutput;
    javacall_amms_image_filter_overlay_s* pIFo;

    int lx, rx, ly, ry;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((filter_handle == NULL) || (pInput == NULL) || 
            (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
                    return JAVACALL_INVALID_ARGUMENT;

    pIFo = (javacall_amms_image_filter_overlay_s*)filter_handle;
    pData = (javacall_amms_frame_RGB32*)pInput;

    if (pIFo->pPict == NULL) {
        /// Simply skip frame
        *ppOutput = javacall_amms_addref_frame(pInput);
        return JAVACALL_OK;
    }

    lx = max(0, pIFo->pos_x);
    ly = max(0, pIFo->pos_y);
    rx = min(pData->width, pIFo->pos_x + pIFo->pPict->width);
    ry = min(pData->height, pIFo->pos_y + pIFo->pPict->height);

    if ((lx >= rx) || (ly >= ry)) {
        /// Simply skip frame
        *ppOutput = javacall_amms_addref_frame(pInput);
        return JAVACALL_OK;
    }

    if (pInput->bufferType == JAVACALL_AMMS_BUFFER_TYPE_EXTERN) {
        width = pData->width;
        height = pData->height;
        pOutput = javacall_amms_create_frame_rgb32_in(width, height);

        if (!pOutput) 
            return JAVACALL_OUT_OF_MEMORY;
        memcpy(pOutput->bufferData, pInput->bufferData, 
            width*height*sizeof(int));
    } else {
        pOutput = javacall_amms_addref_frame(pInput);
    }

    {
        int* piData = (int*)pOutput->bufferData;
        int *piPict = (int*)pIFo->pPict->raw.bufferData;

        int swidth = pData->width;
        int dwidth = pIFo->pPict->width;

        piData += ly*swidth + lx;
        piPict += (ly-pIFo->pos_y)*dwidth + (lx-pIFo->pos_x);

        switch (pIFo->mTransMode) {
            case ALPHA_ENABLED:
                processBlend(piData, swidth, piPict, dwidth, rx-lx, ry-ly); 
                break;
            case FULLY_OPAQUE:
                processCopy(piData, swidth, piPict, dwidth, rx-lx, ry-ly); 
                break;
            default:
                processTrans(piData, swidth, piPict, dwidth, rx-lx, ry-ly, 
                    pIFo->tColor); 
                break;
        }
    }

    *ppOutput = pOutput;

    return JAVACALL_OK;
}

static javacall_result destroy(javacall_image_filter_handle filter_handle)
{
    javacall_amms_image_filter_overlay_s* pIFo;

    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    pIFo = (javacall_amms_image_filter_overlay_s*)filter_handle;

    javacall_amms_release_frame((javacall_amms_frame*)(pIFo->pPict));
    javacall_free(pIFo);
    return JAVACALL_OK;
}

static javacall_image_filter_handle 
        create(
            javacall_const_utf16_string source_type, 
            javacall_const_utf16_string dest_type)
{
    javacall_image_filter_handle result = NULL;
    javacall_amms_image_filter_overlay_s* pIFo;

    if (javautil_unicode_equals(source_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
            == JAVACALL_FALSE)
        return NULL;
    if (javautil_unicode_equals(dest_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
            == JAVACALL_FALSE)
        return NULL;

    pIFo = (javacall_amms_image_filter_overlay_s*)
              javacall_malloc(sizeof(javacall_amms_image_filter_overlay_s));
    if (pIFo != NULL) {
        memset(pIFo, 0, sizeof(*pIFo));
        result = (javacall_image_filter_handle)pIFo;
        result->destroy = &destroy;
        result->clone   = &clone;
        result->addtoMP = &javacall_amms_if_simple_add;
        result->pOverlayAPI = &overlayAPI;
        result->process = &process; 
        result->sifSize = sizeof(*pIFo);
    }
 
    return result;
}

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////

const javacall_amms_if_factory javacall_amms_overlay_filter_factory = {
        &create, 
        &javacall_amms_if_get_source_types_only_rgb32, 
        &javacall_amms_if_get_dest_types_only_rgb32
};
