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
 * Transform filter -> rotate, flip and stretch source image
 */

#include <javacall_multimedia_advanced.h>
#include <javautil_unicode.h>

#include "multimedia_image_filter.h"
#include "multimedia_image_format.h"

#include <javacall_memory.h>

/// For memset
#include <memory.h>

#define min(a, b) ((a) < (b) ? (a) : (b))
#define max(a, b) ((a) > (b) ? (a) : (b))
#define abs(a) ((a) >= 0 ? (a) : (-a))

#define BLACK_COLOR         0

/// RATIO_FIXED_EXPONENT_SHIFT is chosen, so that
/// (sourceWidth << RATIO_FIXED_EXPONENT_SHIFT) and 
/// (sourceHeight << RATIO_FIXED_EXPONENT_SHIFT) fit 31bit int range.
/// So, if RATIO_FIXED_EXPONENT_SHIFT=20, the source picture width and height 
/// may be up to ((2<<12) - 1)=4095.
#define RATIO_FIXED_EXPONENT_SHIFT     20

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////

typedef struct javacall_amms_image_filter_transform_s {
    javacall_amms_image_filter_s sIF;
    int                        sourceX;
    int                        sourceY;
    int                        sourceWidth;
    int                        sourceHeight;

    int                        destWidth;
    int                        destHeight;
    int                        processRotation;
} javacall_amms_image_filter_transform_s;

static javacall_result setSource(javacall_image_filter_handle filter_handle, 
                                 int x, int y, 
                                 int width, int height)
{
    javacall_amms_image_filter_transform_s* pIFt;
    if ((filter_handle == NULL) || (width == 0) || (height == 0))
        return JAVACALL_INVALID_ARGUMENT;
    pIFt = (javacall_amms_image_filter_transform_s*)filter_handle;
    pIFt->sourceX = x;
    pIFt->sourceY = y;
    pIFt->sourceWidth = width;
    pIFt->sourceHeight = height;
    return JAVACALL_OK;
}

static javacall_result setDest(javacall_image_filter_handle filter_handle, 
                               int width, int height, int rotation)
{
    javacall_amms_image_filter_transform_s* pIFt;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    pIFt = (javacall_amms_image_filter_transform_s*)filter_handle;
    pIFt->destWidth = width;
    pIFt->destHeight = height;
    pIFt->processRotation = rotation;
    return JAVACALL_OK;
}

static const javacall_amms_transform_API transformAPI = {&setSource, &setDest};

// pitch is given in ints
static void fillColorRGB32(int* pDest, int lx, int ly, int rx, int ry, 
                           int pitch, int color)
{
    int *pCur;
    int i;
    int size;

    if (lx > rx)
        return;
    if (ly > ry)
        return;

    size = (rx-lx+1)*sizeof(int);
    pCur = pDest + ly*pitch + lx;

    for (i = 0; i < ry-ly+1; i++) {
        memset(pCur, color, size);
        pCur += pitch;
    }
}

// sourceLen > 0 && destLen > 0
static int calculateRatio(int sourceLen, int destLen) {
    int ratio;

    if ((sourceLen > 1) && (destLen > 1)) {
        sourceLen -= 1;
        destLen -=1;
    }

    ratio = (sourceLen << RATIO_FIXED_EXPONENT_SHIFT);
    ratio /= destLen;

    return ratio;
}

// Find such i, that lx <= i*ratioX,  i*ratioX <= rx
static void adjustDest(int lx, int rx, int ratioX, int *plDest, int *prDest)  {
    // added ratioX - 1, to get correct round
    *plDest = ((lx << RATIO_FIXED_EXPONENT_SHIFT) + ratioX - 1) / ratioX; 
    *prDest = (((rx+1) << RATIO_FIXED_EXPONENT_SHIFT) - 1) / ratioX;
}

static void resizeNearest(int* pInput, int sx, int sy, int swidth, int sheight,
                          int* pOutput, int dLx, int dRx, int dLy, int dRy, 
                          int sPitch, int dPitch,
                          int ratioX, int ratioY) 
{
    int i, j;
    int *pIn, *pOut;
    int cx, cy = 0;

    if (swidth < 0) {
        sx += (dRx*ratioX) >> RATIO_FIXED_EXPONENT_SHIFT;
        ratioX = -ratioX;
    } else sx += (dLx*ratioX) >> RATIO_FIXED_EXPONENT_SHIFT;
    if (sheight < 0) {
        sy += (dRy*ratioY) >> RATIO_FIXED_EXPONENT_SHIFT;
        ratioY = -ratioY;
    } else sy += (dLy*ratioY) >> RATIO_FIXED_EXPONENT_SHIFT;

    pInput += sy*sPitch + sx;
    pOut = pOutput + dLy*dPitch + dLx;

    for (i = 0; i <= dRy-dLy; i++) {
        pIn = pInput + (cy >> RATIO_FIXED_EXPONENT_SHIFT)*sPitch;
        cx = 0;
        for (j = 0; j <= dRx-dLx; j++) {
            pOut[j] = pIn[cx >> RATIO_FIXED_EXPONENT_SHIFT];
            cx += ratioX;
        }
        cy += ratioY;
        pOut += dPitch;
    }
}

static javacall_result 
        process(javacall_image_filter_handle ifHandle,
                javacall_amms_frame* pInput, 
                javacall_amms_frame** ppOutput)
{
    javacall_amms_frame_RGB32* pData;
    int sx, sy, swidth, sheight, dwidth, dheight;

    javacall_amms_frame* pOutput;
    javacall_amms_image_filter_transform_s* pIFt;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((ifHandle == NULL) || (pInput == NULL) || 
        (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
        return JAVACALL_INVALID_ARGUMENT;

    pIFt = (javacall_amms_image_filter_transform_s*)ifHandle;
    pData = (javacall_amms_frame_RGB32*)pInput;

    swidth = pIFt->sourceWidth;
    sheight = pIFt->sourceHeight;
    if ((swidth == 0) || (sheight == 0)) {
        swidth = pData->width;
        sheight = pData->height;
        sx = 0;
        sy = 0;
    } else {
        sx = pIFt->sourceX;
        sy = pIFt->sourceY;
    }

    if (pIFt->processRotation == 2) {
        swidth = -swidth;
        sheight = -sheight;
    }

    dwidth = pIFt->destWidth;
    dheight = pIFt->destHeight;
    if (dwidth == 0)
        dwidth = abs(swidth);
    if (dheight == 0)
        dheight = abs(sheight);

    pOutput = javacall_amms_create_frame_rgb32_in(dwidth, dheight);

    if (!pOutput) 
        return JAVACALL_OUT_OF_MEMORY;

    /// Simple nearest resize 
    {
        int swidthAbs = abs(swidth);
        int sheightAbs = abs(sheight);

        /// Check position
        int sourceLX = max(sx, 0);
        int sourceLY = max(sy, 0);
        int sourceRX = min(sx + swidthAbs - 1, pData->width - 1);
        int sourceRY = min(sy + sheightAbs - 1, pData->height - 1);

        javacall_bool fillTheWhole = JAVACALL_TRUE;

        if ((sourceLX <= sourceRX) && (sourceLY <= sourceRY)) {
            /// So the source contains a piece of the picture
            int ratioX;
            int ratioY;

            int destLX, destRX;
            int destLY, destRY;

            ratioX = calculateRatio(swidthAbs, dwidth);
            ratioY = calculateRatio(sheightAbs, dheight);

            adjustDest(sourceLX - sx, sourceRX - sx, ratioX, &destLX, &destRX);
            adjustDest(sourceLY - sy, sourceRY - sy, ratioY, &destLY, &destRY);

            // additional checks of output rectangle
            destLX = max(destLX, 0);
            destLY = max(destLY, 0);

            destRX = min(destRX, dwidth - 1);
            destRY = min(destRY, dheight - 1);

            if ((destLX <= destRX) && (destLY <= destRY)) {
                fillTheWhole = JAVACALL_FALSE;
               
                fillColorRGB32((int*)(pOutput->bufferData), 
                    0, 0, dwidth-1, destLY-1, dwidth, BLACK_COLOR);
                fillColorRGB32((int*)(pOutput->bufferData), 
                    0, destRY+1, dwidth-1, dheight-1, dwidth, BLACK_COLOR);
                fillColorRGB32((int*)(pOutput->bufferData), 
                    0, destLY, destLX-1, destRY, dwidth, BLACK_COLOR);
                fillColorRGB32((int*)(pOutput->bufferData), 
                    destRX+1, destLY, dwidth-1, destRY, dwidth, BLACK_COLOR);

                resizeNearest((int*)(pInput->bufferData), 
                              sx, sy, swidth, sheight, 
                              (int*)(pOutput->bufferData), 
                              destLX, destRX, destLY, destRY, 
                              pData->width, dwidth,
                              ratioX, ratioY);
            }
        } 
        
        if (fillTheWhole == JAVACALL_TRUE) {
            /// Just fill dest by black
            fillColorRGB32((int*)(pOutput->bufferData), 
                0, 0, dwidth - 1, dheight -1, dwidth, BLACK_COLOR);
        }
    }

    *ppOutput = pOutput;

    return JAVACALL_OK;
}


typedef struct javacall_amms_image_filter_rotator_s {
    javacall_amms_image_filter_s sIF;
    int                        rotation;
} javacall_amms_image_filter_rotator_s;

static javacall_result 
        processRotate(javacall_image_filter_handle ifHandle,
                javacall_amms_frame* pInput, 
                javacall_amms_frame** ppOutput)
{
    javacall_amms_frame_RGB32* pData;
    int swidth, sheight, rotation;

    javacall_amms_frame* pOutput;
    javacall_amms_image_filter_rotator_s* pIFr;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((ifHandle == NULL) || (pInput == NULL) || 
        (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
        return JAVACALL_INVALID_ARGUMENT;

    pIFr = (javacall_amms_image_filter_rotator_s*)ifHandle;
    pData = (javacall_amms_frame_RGB32*)pInput;

    swidth = pData->width;
    sheight = pData->height;
    rotation = pIFr->rotation;

    if ((rotation & 1) == 0)
        return JAVACALL_FAIL;

    pOutput = javacall_amms_create_frame_rgb32_in(sheight, swidth);

    if (!pOutput) 
        return JAVACALL_OUT_OF_MEMORY;

    {
        int i, j, destPitch, nextPos;
        int* pIn = (int*)(pInput->bufferData);
        int* pOut = (int*)(pOutput->bufferData);

        destPitch = sheight;
        nextPos = swidth*sheight + 1;
        if (rotation == 3) {
            pOut += nextPos - sheight - 1;
            destPitch =-destPitch;
        } else {
            pOut += sheight - 1;
            nextPos = -nextPos;
        }

        for (i = 0; i < sheight; i++) {
            for (j = 0; j < swidth; j++) {
                *pOut = pIn[j];
                pOut += destPitch;
            }
            pOut += nextPos;
            pIn += swidth;
        }

    }

    *ppOutput = pOutput;

    return JAVACALL_OK;
}

static javacall_image_filter_handle createRotator(int rotation) {
    javacall_image_filter_handle result = NULL;
    javacall_amms_image_filter_rotator_s* pIFr;

    pIFr = (javacall_amms_image_filter_rotator_s*)
        javacall_malloc(sizeof(javacall_amms_image_filter_rotator_s));
    if (pIFr != NULL) {
        memset(pIFr, 0, sizeof(*pIFr));
        pIFr->rotation = rotation;
        result = (javacall_image_filter_handle)pIFr;
        result->destroy = &javacall_amms_if_simple_destroy;
        result->clone   = &javacall_amms_if_simple_clone;
        result->addtoMP = &javacall_amms_if_simple_add;
        result->process = &processRotate; 
        result->sifSize = sizeof(*pIFr);
    }

    return result;
}

static javacall_result javacall_amms_add(
        javacall_image_filter_handle filter_handle,
        javacall_amms_mp_add func_mp_add,
        javacall_media_processor_handle mpHandle)
{
    javacall_result result;
    javacall_amms_image_filter_transform_s* pIFt;

    result = func_mp_add(mpHandle, filter_handle, JAVACALL_FALSE);

    if (JAVACALL_SUCCEEDED(result)) {
        pIFt = (javacall_amms_image_filter_transform_s*)filter_handle;
        if (pIFt->processRotation & 1) {
            javacall_image_filter_handle rotator = 
                createRotator(pIFt->processRotation);
            result = func_mp_add(mpHandle, rotator, JAVACALL_TRUE);
        }
    }

    return result;
}

static javacall_image_filter_handle 
        create(
            javacall_const_utf16_string source_type, 
            javacall_const_utf16_string dest_type)
{
    javacall_image_filter_handle result = NULL;
    javacall_amms_image_filter_transform_s* pIFt;

    if (javautil_unicode_equals(source_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
            == JAVACALL_FALSE)
        return NULL;
    if (javautil_unicode_equals(dest_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
            == JAVACALL_FALSE)
        return NULL;

    pIFt = (javacall_amms_image_filter_transform_s*)
              javacall_malloc(sizeof(javacall_amms_image_filter_transform_s));
    if (pIFt != NULL) {
        memset(pIFt, 0, sizeof(*pIFt));
        result = (javacall_image_filter_handle)pIFt;
        result->destroy = &javacall_amms_if_simple_destroy;
        result->clone   = &javacall_amms_if_simple_clone;
        result->addtoMP = &javacall_amms_add;
        result->process = &process; 
        result->pTransformAPI = &transformAPI;
        result->sifSize = sizeof(*pIFt);
    }

    return result;
}

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////

const javacall_amms_if_factory javacall_amms_transform_filter_factory = {
    &create, 
    &javacall_amms_if_get_source_types_only_rgb32, 
    &javacall_amms_if_get_dest_types_only_rgb32
};
