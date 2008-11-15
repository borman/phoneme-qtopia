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
 * Effect filter -> apply monochrome or negative, or ...
 */

#include <javacall_multimedia_advanced.h>
#include <javautil_unicode.h>

#include <javacall_memory.h>
#include "multimedia_image_filter.h"

/// For memset
#include <memory.h>

typedef struct javacall_amms_image_filter_effect_s {
    javacall_amms_image_filter_s sIF;
    int                        curPreset;
} javacall_amms_image_filter_effect_s;

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////

static const javacall_utf16 preset_name_monochrome[] = 
    {'m','o','n','o','c','h','r','o','m','e',0};
static const javacall_utf16 preset_name_negative[] = 
    {'n','e','g','a','t','i','v','e',0};

static const javacall_utf16 preset_name_emboss[] = 
    {'e','m','b','o','s','s',0};
static const javacall_utf16 preset_name_sepia[] = 
    {'s','e','p','i','a',0};
static const javacall_utf16 preset_name_solarize[] = 
    {'s','o','l','a','r','i','z','e',0};
static const javacall_utf16 preset_name_redeyereduction[] = 
    {'r','e','d','e','y','e','r','e','d','u','c','t','i','o','n',0};

static javacall_const_utf16_string SupportedPresets[] = {
    preset_name_monochrome, preset_name_negative};

static const int SupportedPresetsCnt =  
        sizeof(SupportedPresets)/sizeof(*SupportedPresets);

typedef void (*process_rgb32)(int* pInput, int* pOutput, int width, int height);

static void process_monochrome_rgb32(int* pInput, int* pOutput, 
                                     int width, int height);
static void process_negative_rgb32(int* pInput, int* pOutput, 
                                   int width, int height);

static const process_rgb32 pProcessor[] = {
    process_monochrome_rgb32, process_negative_rgb32}; 

// Luma via ITU-R BT.601 (CCIR 601)
#define LUMINANCE_1(r, g, b, res) { \
    (res) = ((r)*77 + (g)*150 + (b)*29) >> 8; \
}

// Luma via ITU-R BT.709
#define LUMINANCE_2(r, g, b, res) { \
    (res) = ((r)*54 + (g)*183 + (b)*19) >> 8; \
}

// via HSV
#define LUMINANCE_3(r, g, b, res) { \
    int rx = (r), gx = (g), bx = (b); \
    int max = rx; \
    if (max < gx) \
        max = gx; \
    if (max < bx) \
        max = bx; \
    (res) = max; \
}

// via HSL 
#define LUMINANCE_4(r, g, b, res) { \
    int rx = (r), gx = (g), bx = (b); \
    int max, min; \
    if (rx < gx) {  \
        min = rx; \
        max = gx; \
    } else { \
        min = gx; \
        max = rx; \
    } \
    if (max < bx) \
        max = bx; \
    if (min > bx) \
        min = bx; \
    (res) = (max + min) >> 1; \
}

static javacall_const_utf16_string* 
    get_supported_presets(
                         javacall_handle filter_handle, 
                         /*OUT*/ int *number_of_presets)
{
    if (filter_handle == NULL)
        return NULL;

    if (number_of_presets != NULL)
        *number_of_presets = SupportedPresetsCnt;
    return SupportedPresets;
}

static javacall_result
    set_preset(
              javacall_handle filter_handle, 
              javacall_const_utf16_string preset_name)
{
    int i;
    javacall_amms_image_filter_effect_s* pIFe;
    if ((filter_handle == NULL) || (preset_name == NULL))
        return JAVACALL_INVALID_ARGUMENT;

    pIFe = (javacall_amms_image_filter_effect_s*)filter_handle;

    for (i = 0; i < SupportedPresetsCnt; i++)
        if (javautil_unicode_equals(preset_name, SupportedPresets[i]) 
            == JAVACALL_TRUE) {
            pIFe->curPreset = i;
            return JAVACALL_OK;
        }

    return JAVACALL_INVALID_ARGUMENT;
}

static javacall_const_utf16_string get_preset(javacall_handle filter_handle)
{
    javacall_amms_image_filter_effect_s* pIFe;
    if (filter_handle == NULL)
        return NULL;
    pIFe = (javacall_amms_image_filter_effect_s*)filter_handle;
    return SupportedPresets[pIFe->curPreset];
}

static javacall_result process(javacall_image_filter_handle filter_handle,
                                   javacall_amms_frame* pInput, 
                                   javacall_amms_frame** ppOutput) 
{
    javacall_amms_frame_RGB32* pData;
    int width, height;

    javacall_amms_frame* pOutput;
    javacall_amms_image_filter_effect_s* pIFe;

    if (ppOutput == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    *ppOutput = NULL;

    if ((filter_handle == NULL) || (pInput == NULL) || 
            (pInput->extType != JAVACALL_AMMS_ET_RGB32_DATA))
                    return JAVACALL_INVALID_ARGUMENT;

    pIFe = (javacall_amms_image_filter_effect_s*)filter_handle;
    pData = (javacall_amms_frame_RGB32*)pInput;

    width = pData->width;
    height = pData->height;
    pOutput = javacall_amms_create_frame_rgb32_in(width, height);

    if (!pOutput) 
        return JAVACALL_OUT_OF_MEMORY;

    pProcessor[pIFe->curPreset]((int*)pData->raw.bufferData, 
                                (int*)pOutput->bufferData, width, height);
    *ppOutput = pOutput;

    return JAVACALL_OK;
}

static void process_monochrome_rgb32(int* pInput, int* pOutput, 
                                     int width, int height) 
{
    int i, j;
    unsigned int* pCur = (unsigned int*)pInput;
    unsigned int* pDest = (unsigned int*)pOutput;
    unsigned int pix, mon, r, g, b;

    for (i = 0; i < height; i++) {
        for (j = 0; j < width; j++) {
            pix = pCur[j];
            b = pix & 0xFF;
            g = (pix >> 8) & 0xFF;
            r = (pix >> 16) & 0xFF;
            LUMINANCE_1(r, g, b, mon);
            pDest[j] = ((pix >> 24) << 24) | (mon << 16) | 
                         (mon << 8) | (mon << 0);
        }
        pCur += width;
        pDest += width;
    }
}

static void process_negative_rgb32(int* pInput, int* pOutput, 
                                   int width, int height) 
{
    int i, j;
    unsigned int* pCur = (unsigned int*)pInput;
    unsigned int* pDest = (unsigned int*)pOutput;
    unsigned int pix, pixNeg;

    for (i = 0; i < height; i++) {
        for (j = 0; j < width; j++) {
            pix = pCur[j];
            pixNeg = ~(pix << 8);
            /// save alpha component the same
            pDest[j] = ((pix >> 24) << 24) | (pixNeg >> 8);
        }
        pCur += width;
        pDest += width;
    }
}

static const javacall_amms_preset_API presetAPI = {
    &get_supported_presets, &set_preset, &get_preset};

static javacall_image_filter_handle 
        create(
            javacall_const_utf16_string source_type, 
            javacall_const_utf16_string dest_type)
{
    javacall_image_filter_handle result = NULL;
    javacall_amms_image_filter_effect_s* pIFe;

    if (javautil_unicode_equals(source_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
            == JAVACALL_FALSE)
        return NULL;
    if (javautil_unicode_equals(dest_type, JAVACALL_AMMS_MIME_RAW_RGBA8888) 
             == JAVACALL_FALSE)
        return NULL;

    pIFe = (javacall_amms_image_filter_effect_s*)
              javacall_malloc(sizeof(javacall_amms_image_filter_effect_s));
    if (pIFe != NULL) {
        memset(pIFe, 0, sizeof(*pIFe));
        result = (javacall_amms_image_filter_s*)pIFe;

        result->pPresetAPI = &presetAPI;
        result->destroy    = &javacall_amms_if_simple_destroy;
        result->clone      = &javacall_amms_if_simple_clone;
        result->addtoMP    = &javacall_amms_if_simple_add;
        result->process    = &process;
        result->sifSize    = sizeof(*pIFe);
    }

    return result;
}

//////////////////////////////////////////////////////////////////////////
/// Common image post-processing part
//////////////////////////////////////////////////////////////////////////

const javacall_amms_if_factory javacall_amms_effect_filter_factory = {
        &create, 
        &javacall_amms_if_get_source_types_only_rgb32, 
        &javacall_amms_if_get_dest_types_only_rgb32
};
