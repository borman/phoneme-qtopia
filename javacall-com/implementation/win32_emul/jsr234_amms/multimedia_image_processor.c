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
 * Media processor logic
 */

#include <javacall_multimedia_advanced.h>
#include <javanotify_multimedia_advanced.h>
#include "multimedia_image_filter.h"
#include "multimedia_image_format.h"

#include <javacall_memory.h>

#include <windows.h>

#define MAX_FILTER_NUMBER 10

//////////////////////////////////////////////////////////////////////////
/// Internal part
//////////////////////////////////////////////////////////////////////////

typedef struct javacall_amms_media_processor_s {
    javacall_int64                  media_processor_id;
    CRITICAL_SECTION                csHandle;
    HANDLE                          hMPThread;

    javacall_bool                   bStartIsContinue;
    javacall_image_filter_handle    filters[MAX_FILTER_NUMBER];
    int                             filtersCnt;

    javacall_amms_frame*            inputData;
    javacall_amms_frame*            outputData;
} javacall_amms_media_processor_s;

static void closeThread(javacall_amms_media_processor_s* pMP)
{
    EnterCriticalSection(&pMP->csHandle);
    CloseHandle(pMP->hMPThread);
    pMP->hMPThread = NULL;
    LeaveCriticalSection(&pMP->csHandle);
}

static DWORD WINAPI MPThreadProc(LPVOID lpParameter) 
{
    javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)lpParameter;

    if (lpParameter == NULL)
        return (DWORD)-1;

    {
        javacall_amms_media_processor_s* pMP = 
                (javacall_amms_media_processor_s*)lpParameter;
        javacall_amms_frame* cur = javacall_amms_addref_frame(pMP->inputData);
        int i;

        if (cur == NULL) {
            javanotify_on_amms_notification(
                JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_ERROR, 
                pMP->media_processor_id, NULL);
            closeThread(pMP);
            return 0;
        }

        pMP->outputData = javacall_amms_release_frame(pMP->outputData);

        for (i = 0; i < pMP->filtersCnt; i++) {
            javacall_amms_frame* out;
            javacall_image_filter_handle pIF = pMP->filters[i];
            javacall_result result = pIF->process(pIF, cur, &out);
            javacall_amms_release_frame(cur);

            if (!JAVACALL_SUCCEEDED(result)) {
                javanotify_on_amms_notification(
                    JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_ERROR, 
                    pMP->media_processor_id, NULL);
                closeThread(pMP);
                return 0;
            }
            cur = out;
        }
        
        /// If there was no filters, pMP->outputData will be the same as input
        pMP->outputData = cur;

        javanotify_on_amms_notification(
            JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED, 
            pMP->media_processor_id, NULL);
        closeThread(pMP);
        return 0;
    }
}

//////////////////////////////////////////////////////////////////////////
/// Realization of javacall_multimedia_advanced.h
//////////////////////////////////////////////////////////////////////////

javacall_media_processor_handle 
    javacall_media_processor_create(javacall_int64  media_processor_id)
{
    javacall_amms_media_processor_s* pMP = 
            javacall_malloc(sizeof(javacall_amms_media_processor_s));

    InitializeCriticalSection(&pMP->csHandle);
    pMP->filtersCnt = 0;
    pMP->media_processor_id = media_processor_id;
    pMP->inputData = NULL;
    pMP->outputData = NULL;
    pMP->hMPThread = NULL;
    pMP->bStartIsContinue = JAVACALL_FALSE;
    return pMP;
}

javacall_result 
    javacall_media_processor_reset(
       javacall_media_processor_handle media_processor_handle)
{
    javacall_amms_media_processor_s* pMP;
    int i;

    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    pMP = (javacall_amms_media_processor_s*)media_processor_handle;
    pMP->inputData = javacall_amms_release_frame(pMP->inputData);
    pMP->outputData = javacall_amms_release_frame(pMP->outputData);

    /// Close thread handles
    javacall_media_processor_abort(media_processor_handle);

    for (i = 0; i < pMP->filtersCnt; i++)
        javacall_image_filter_destroy(pMP->filters[i]);
    pMP->bStartIsContinue = JAVACALL_FALSE;
    pMP->filtersCnt = 0;

    return JAVACALL_OK;
}

static javacall_result 
    media_processor_add_filter_real(
        javacall_media_processor_handle media_processor_handle, 
        javacall_image_filter_handle filter_handle,
        javacall_bool change_owner)
{
    javacall_result result;

    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    result = (media_processor_handle == NULL) ? 
             JAVACALL_INVALID_ARGUMENT : JAVACALL_OK;

    if (JAVACALL_SUCCEEDED(result)) {
        javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)media_processor_handle;
        javacall_image_filter_handle   nHandle = filter_handle;

        if (pMP->filtersCnt >= MAX_FILTER_NUMBER)
            result = JAVACALL_OUT_OF_MEMORY;
        else {
            if (change_owner == JAVACALL_FALSE)
                nHandle = javacall_image_filter_clone(filter_handle);

            if (nHandle == NULL)
                return JAVACALL_OUT_OF_MEMORY;
            else
                pMP->filters[pMP->filtersCnt++] = nHandle;
        }
    }

    if ((change_owner == JAVACALL_TRUE) && (!JAVACALL_SUCCEEDED(result))) {
        javacall_image_filter_destroy(filter_handle);
    }

    return result;
}

javacall_result 
    javacall_media_processor_add_filter(
         javacall_media_processor_handle media_processor_handle, 
         javacall_image_filter_handle filter_handle)
{
    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;
    if (filter_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    if (filter_handle->addtoMP == NULL)
        return 
            media_processor_add_filter_real(media_processor_handle, 
                filter_handle, JAVACALL_TRUE);
    else 
        return 
            filter_handle->addtoMP(
                filter_handle, &media_processor_add_filter_real, 
                media_processor_handle);
}

javacall_result 
    javacall_media_processor_destroy(
        javacall_media_processor_handle media_processor_handle)
{
    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)media_processor_handle;
        javacall_result res = javacall_media_processor_reset(pMP);
        DeleteCriticalSection(&pMP->csHandle);
        javacall_free(pMP);
        return res;
    }
}


javacall_result 
    javacall_media_processor_start(
        javacall_media_processor_handle media_processor_handle)
{
    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)media_processor_handle;
        javacall_result result = JAVACALL_OK;

        EnterCriticalSection(&pMP->csHandle);
        if (pMP->hMPThread == NULL) {
            pMP->hMPThread  = CreateThread(NULL, 0, MPThreadProc, pMP, 0, NULL);

            pMP->bStartIsContinue = JAVACALL_FALSE;
            if (pMP->hMPThread == NULL) 
                result = JAVACALL_FAIL;
        } else {
            if (pMP->bStartIsContinue) {
                /// May be working, or may be sleep
                int suspCnt = ResumeThread(pMP->hMPThread);
                if (suspCnt == (DWORD) -1)
                    result = JAVACALL_FAIL;

                pMP->bStartIsContinue = JAVACALL_FALSE;
            } else {
                /// Already started
            }
        }
        LeaveCriticalSection(&pMP->csHandle);

        return result;
    }
}

javacall_result 
    javacall_media_processor_stop(
        javacall_media_processor_handle media_processor_handle)
{
    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)media_processor_handle;
        javacall_result result = JAVACALL_OK;

        EnterCriticalSection(&pMP->csHandle);
        if ((pMP->hMPThread != NULL) && (!pMP->bStartIsContinue)) {
            int suspCnt = SuspendThread(pMP->hMPThread);
            /// Checks that thread suspended
            if (suspCnt == (DWORD) -1)
                result = JAVACALL_FAIL;

            pMP->bStartIsContinue = JAVACALL_TRUE;
        }
        LeaveCriticalSection(&pMP->csHandle);

        return result;
    }
}

javacall_result 
    javacall_media_processor_abort(
        javacall_media_processor_handle media_processor_handle)
{
    if (media_processor_handle == NULL)
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_media_processor_s* pMP = 
            (javacall_amms_media_processor_s*)media_processor_handle;

        javacall_result result = JAVACALL_OK;

        EnterCriticalSection(&pMP->csHandle);
        if (pMP->hMPThread != NULL) {
            if (!TerminateThread(pMP->hMPThread, (DWORD)-1))
                result = JAVACALL_FAIL;
            else {
                if (CloseHandle(pMP->hMPThread))
                    pMP->hMPThread = NULL;
                else 
                    result = JAVACALL_FAIL;
            }
        }
        LeaveCriticalSection(&pMP->csHandle);

        return result;
    }
}

javacall_result     
    javacall_media_processor_set_input_rgb32(            
        javacall_media_processor_handle media_processor_handle,
        int* pRGBdata, int width, int height)
{
    if ((media_processor_handle == NULL) || (pRGBdata == NULL) 
        || (width <= 0) || (height <= 0))
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_frame* sType = 
            javacall_amms_create_frame_rgb32_ex(pRGBdata, width, height);
        if (sType == NULL)
            return JAVACALL_OUT_OF_MEMORY;

        javacall_amms_release_frame(media_processor_handle->inputData);
        media_processor_handle->inputData = sType;
    }
    return JAVACALL_OK;
}

javacall_result 
    javacall_media_processor_set_input_raw(
        javacall_media_processor_handle media_processor_handle,
        unsigned char* pRAWdata, int length)
{
    if ((media_processor_handle == NULL) || (pRAWdata == NULL) || (length <= 0))
        return JAVACALL_INVALID_ARGUMENT;

    {
        javacall_amms_frame* sType = 
            javacall_amms_create_frame_raw_ex(pRAWdata, length);
        if (sType == NULL)
            return JAVACALL_OUT_OF_MEMORY;

        javacall_amms_release_frame(media_processor_handle->inputData);
        media_processor_handle->inputData = sType;
    }
    return JAVACALL_OK;

}

const unsigned char* 
    javacall_media_processor_get_raw_output(
        javacall_media_processor_handle media_processor_handle, 
        /*OUT*/ int* result_length)
{
    javacall_amms_frame* pOut;
    if (result_length)
        *result_length = 0;
    if (media_processor_handle == NULL)
        return NULL;

    pOut = media_processor_handle->outputData;

    if (pOut == NULL)
        return NULL;

    if (result_length)
        *result_length = pOut->bufferLength;
    return (unsigned char*)(pOut->bufferData);
}

javacall_result
    javacall_media_processor_get_output_size(
        javacall_media_processor_handle media_processor_handle, 
        /*OUT*/ int* width, int* height)
{
    javacall_amms_frame* pOut;
    javacall_amms_frame_RGB32* pRGBbuf;
    if (media_processor_handle == NULL)
        return JAVACALL_FAIL;

    pOut = media_processor_handle->outputData;
    if ((pOut == NULL) || (pOut->extType != JAVACALL_AMMS_ET_RGB32_DATA))
        return JAVACALL_FAIL;

    pRGBbuf = (javacall_amms_frame_RGB32*)pOut;

    if (width)
        *width = pRGBbuf->width;
    if (height)
        *height = pRGBbuf->height;
    return JAVACALL_OK;
}


