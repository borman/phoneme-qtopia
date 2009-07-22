/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

#include "javacall_multimedia.h"
#include "javacall_multimedia_advanced.h"

#include "multimedia.h"
#include "multimedia_camera_state.h"

#include <stdio.h>
#include <string.h>


typedef struct {
    long width;
    long height;
} resolution;

void extra_camera_controls_init( audio_handle * pHandle )
{
    camera_state* cs = (camera_state*)malloc( sizeof( camera_state ) );

    cs->ah                   = pHandle;

    cs->shutterFeedback      = JAVACALL_FALSE;
    cs->expMode              = 0; // auto
    cs->stillRes             = 0;
    cs->videoRes             = -1;

    cs->exposureCurrentFStop = 0;
    cs->exposureCurrentISO   = 0;

    cs->flashMode            = JAVACALL_AMMS_FLASH_MODE_OFF;

    cs->focusDistance        = JAVACALL_AMMS_FOCUS_AUTO;
    cs->focusMacroMode       = JAVACALL_FALSE;

    cs->zoomOptical          = 100;
    cs->zoomDigital          = 100;

    cs->snapDirectory        = NULL;
    cs->snapPrefix           = NULL;
    cs->snapSuffix           = NULL;
    cs->snapIndex            = 0;
    cs->snapShotsLeft        = 0;
    cs->snapThread           = NULL;
    cs->snapStop             = FALSE;
    cs->snapLastName[ 0 ]        = 0;
    cs->snapLastNameForJava[ 0 ] = 0;

    pHandle->pExtraCC = cs;
}

void extra_camera_controls_cleanup( audio_handle * pHandle )
{
    if( NULL != pHandle->pExtraCC )
    {
        camera_state* cs = (camera_state*)( pHandle->pExtraCC );

        pHandle->pExtraCC = NULL;

        cs->snapConfirm = FALSE;
        cs->snapStop    = TRUE;

        if( NULL != cs->snapThread )
        {
            int wait_result = WaitForSingleObject( cs->snapThread, 5000 );
            assert( WAIT_OBJECT_0 == wait_result );
            // even without assert in release version, 
            // we still don't want to free referenced data.
            if( WAIT_OBJECT_0 != wait_result ) return;
        }

        if( NULL != cs->snapDirectory ) free( cs->snapDirectory        );
        if( NULL != cs->snapPrefix    ) free( cs->snapPrefix           );
        if( NULL != cs->snapSuffix    ) free( cs->snapSuffix           );

        free( cs );
    }
}

static const char *expModes[] = {
    "auto",
    "landscape",
    "snow",
    "beach",
    "sunset",
    "night",
    "fireworks",
    "portrait",
    "backlight",
    "spotlight",
    "sports",
    "text"
};
static const int expModeCount = sizeof(expModes) / sizeof(char *);

static resolution stillResolutions[] = {{160, 120}};
static const int stillResCount = sizeof(stillResolutions) / sizeof(resolution);

static resolution videoResolutions[] = {{160, 120}};
static const int videoResCount = sizeof(stillResolutions) / sizeof(resolution);

/**
 * Tests if camera control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *
 * @retval JAVACALL_TRUE if camera control is available
 * @retval JAVACALL_FALSE if camera control is not supported
 */
javacall_bool javacall_amms_camera_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_TRUE;
}

/**
 * The function corresponding to 
 * void CameraControl.enableShutterFeedback(boolean enable)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param enable  \a JAVACALL_TRUE to enable shutter feedback,
 *     \a JAVACALL_FALSE to disable it
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if operation failed or not supported
 */
javacall_result javacall_amms_camera_control_enable_shutter_feedback(
    javacall_handle hNative, javacall_bool enable) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    cs->shutterFeedback = enable;
    printf("[CameraControl] shutter feedback is now %s\n",
        JAVACALL_FALSE == enable ? "OFF" : "ON");
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * int CameraControl.getCameraRotation()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param rotation  pointer to receive the rotation value. The result can be
 *     one of the constants \a JAVACALL_AMMS_CAMERA_ROTATE_NONE,
 *     \a JAVACALL_AMMS_CAMERA_ROTATE_LEFT,
 *     \a JAVACALL_AMMS_CAMERA_ROTATE_RIGHT,
 *     \a JAVACALL_AMMS_CAMERA_ROTATE_UNKNOWN
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a rotation is \a NULL
 */
javacall_result javacall_amms_camera_control_get_camera_rotation(
    javacall_handle hNative, /*OUT*/long *rotation) {
    if (NULL == rotation) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *rotation = JAVACALL_AMMS_CAMERA_ROTATE_UNKNOWN;
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * java.lang.String CameraControl.getExposureMode()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param exposureMode  pointer to the buffer to receive the exposure mode
 *     (result will be null-terminated string)
 * @param bufLength     maximum number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a exposureMode is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_camera_control_get_exposure_mode(
    javacall_handle hNative, /*OUT*/char *exposureMode, long bufLength) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == exposureMode ||
        bufLength <= (long)strlen(expModes[cs->expMode])) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    strcpy(exposureMode, expModes[cs->expMode]);
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * int CameraControl.getStillResolution()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Get index of the current still image resolution.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  pointer to get index. It is an index in the array of
 *     supported still resolutions, or -1 if no still resolutions are supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_still_resolution_count
 * @see javacall_amms_camera_control_get_supported_still_resolution
 */
javacall_result javacall_amms_camera_control_get_still_resolution(
    javacall_handle hNative, /*OUT*/long *index) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == index) {
        return JAVACALL_INVALID_ARGUMENT;
    }
    
    *index = cs->stillRes;
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_exposure_mode,
 * corresponds to java.lang.String[] CameraControl.getSupportedExposureModes()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported exposure modes.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to get the number of supported exposure modes
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_exposure_mode
 */
javacall_result javacall_amms_camera_control_get_supported_exposure_mode_count(
    javacall_handle hNative, /*OUT*/long *count) {
    if (NULL == count) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *count = expModeCount;
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_exposure_mode_count,
 * corresponds to java.lang.String[] CameraControl.getSupportedExposureModes()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the supported exposure mode by index.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of the exposure mode to get
 * @param exposureMode  pointer to the buffer to get the exposure mode
 *     (result will be null-terminated string)
 * @param bufLength  number of bytes exposureMode can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a exposureMode is \a NULL
 *        - insufficient buffer size
 *
 * @see javacall_amms_camera_control_get_supported_exposure_mode_count
 */
javacall_result javacall_amms_camera_control_get_supported_exposure_mode(
    javacall_handle hNative, long index, /*OUT*/char *exposureMode,
    long bufLength) {
    if (index < 0 || index >= expModeCount ||
        NULL == exposureMode || bufLength <= (long)strlen(expModes[index])) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    strcpy(exposureMode, expModes[index]);
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_still_resolution,
 * correspons to int[] CameraControl.getSupportedStillResolutions()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported still image resolutions.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to get the number of supported still resolutions
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_still_resolution
 */
javacall_result
javacall_amms_camera_control_get_supported_still_resolution_count(
    javacall_handle hNative, /*OUT*/long *count) {
    if (NULL == count) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *count = stillResCount;
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_still_resolution_count,
 * correspons to int[] CameraControl.getSupportedStillResolutions()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns a supported still image resolution by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of still image resolution to get
 * @param width  pointer to get width
 * @param height  pointer to get height
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a width is \a NULL
 *        - \a height is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_still_resolution_count
 */
javacall_result javacall_amms_camera_control_get_supported_still_resolution(
    javacall_handle hNative, long index, /*OUT*/long *width,
    /*OUT*/long *height) {
    if (index < 0 || index >= stillResCount ||
        NULL == width || NULL == height) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *width = stillResolutions[index].width;
    *height = stillResolutions[index].height;
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_video_resolution,
 * corresponds to int[] CameraControl.getSupportedVideoResolutions()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported video resolutions.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to get the number of supported video resolutions
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_video_resolution
 */
javacall_result
javacall_amms_camera_control_get_supported_video_resolution_count(
    javacall_handle hNative, /*OUT*/long *count) {
    if (NULL == count) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *count = videoResCount;
    return JAVACALL_OK;
}

/**
 * This function, together with
 * \a javacall_amms_camera_control_get_supported_video_resolution_count,
 * corresponds to int[] CameraControl.getSupportedVideoResolutions()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns a supported video resolution by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of the video resolution to get
 * @param width  pointer to get width
 * @param height  pointer to get height
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a width is \a NULL
 *        - \a height is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_video_resolution_count
 */
javacall_result javacall_amms_camera_control_get_supported_video_resolution(
    javacall_handle hNative, long index, /*OUT*/long *width,
    /*OUT*/long *height) {
    if (index < 0 || index >= videoResCount ||
        NULL == width || NULL == height) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *width = videoResolutions[index].width;
    *height = videoResolutions[index].height;
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * int CameraControl.getVideoResolution()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns index of the current video resolution.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  pointer to get index. It is an index in the array of
 *     supported video resolutions, or -1 if no video resolutions are supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is \a NULL
 *
 * @see javacall_amms_camera_control_get_supported_video_resolution_count
 * @see javacall_amms_camera_control_get_supported_video_resolution
 */
javacall_result javacall_amms_camera_control_get_video_resolution(
    javacall_handle hNative, /*OUT*/long *index) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == index) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *index = cs->videoRes;
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * boolean CameraControl.isShutterFeedbackEnabled()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param enabled  pointer to get the shutter feedback setting
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a enabled is \a NULL
 */
javacall_result javacall_amms_camera_control_is_shutter_feedback_enabled(
    javacall_handle hNative, /*OUT*/javacall_bool *enabled) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == enabled) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *enabled = cs->shutterFeedback;
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * void CameraControl.setExposureMode(java.lang.String mode)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param mode  exposure mode to set (null-terminated string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a mode is \a NULL or refers to an unsupported value
 *
 * @see javacall_amms_camera_control_get_supported_exposure_mode_count
 * @see javacall_amms_camera_control_get_supported_exposure_mode
 */
javacall_result javacall_amms_camera_control_set_exposure_mode(
    javacall_handle hNative, const char *mode) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    int i;

    if (NULL == mode) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    for (i = 0; i < expModeCount; i++) {
        if (!strcmp(mode, expModes[i])) {
            cs->expMode = i;
            printf("[CameraControl] exposure mode has been set to '%s'\n",
                expModes[i]);
            return JAVACALL_OK;
        }
    }
    return JAVACALL_INVALID_ARGUMENT;
}

/**
 * The function corresponding to 
 * void CameraControl.setStillResolution(int index)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of the still image resolution to set
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *
 * @see javacall_amms_camera_control_get_supported_still_resolution_count
 * @see javacall_amms_camera_control_get_supported_still_resolution
 */
javacall_result javacall_amms_camera_control_set_still_resolution(
    javacall_handle hNative, long index) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (index < 0 || index >= stillResCount) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    cs->stillRes = index;
    printf("[CameraControl] still resolution has been set to %dx%d\n",
        stillResolutions[index].width, stillResolutions[index].height);
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * void CameraControl.setVideoResolution(int index)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of the video resolution to set
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *
 * @see javacall_amms_camera_control_get_supported_video_resolution_count
 * @see javacall_amms_camera_control_get_supported_video_resolution
 */
javacall_result javacall_amms_camera_control_set_video_resolution(
    javacall_handle hNative, long index) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (index < 0 || index >= videoResCount) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    cs->videoRes = index;
    printf("[CameraControl] video resolution has been set to %dx%d\n",
        videoResolutions[index].width, videoResolutions[index].height);
    return JAVACALL_OK;
}
