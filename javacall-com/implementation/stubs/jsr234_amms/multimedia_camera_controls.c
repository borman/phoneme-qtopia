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
    return JAVACALL_FALSE;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
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
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Tests if flash control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if flash control is available
 * @retval JAVACALL_FALSE if flash control is not supported
 */
javacall_bool javacall_amms_flash_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
} 

/**
 * The function corresponding to 
 * int FlashControl.getMode()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param mode  pointer to get the flash mode value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a mode is \a NULL
 *
 * @see javacall_amms_flash_control_get_supported_mode_count
 * @see javacall_amms_flash_control_get_supported_mode
 */
javacall_result javacall_amms_flash_control_get_mode(
    javacall_handle hNative, /*OUT*/long *mode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with 
 * \a javacall_amms_flash_control_get_supported_mode,
 * corresponds to int[] FlashControl.getSupportedModes()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * get number of supported modes
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to get the number of supported modes
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_flash_control_get_supported_mode
 */
javacall_result javacall_amms_flash_control_get_supported_mode_count(
    javacall_handle hNative, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with 
 * \a javacall_amms_flash_control_get_supported_mode_count,
 * corresponds to int[] FlashControl.getSupportedModes()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * get supported mode by index
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of the supported mode to get
 * @param mode  pointer to get the supported mode value. The result can be
 *     one of the constants: \a JAVACALL_AMMS_FLASH_MODE_OFF,
 *     \a JAVACALL_AMMS_FLASH_MODE_AUTO,
 *     \a JAVACALL_AMMS_FLASH_MODE_AUTO_WITH_REDEYEREDUCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FORCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FORCE_WITH_REDEYEREDUCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FILLIN
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a mode is \a NULL
 *
 * @see javacall_amms_flash_control_get_supported_mode_count
 */
javacall_result javacall_amms_flash_control_get_supported_mode(
    javacall_handle hNative, long index, /*OUT*/long *mode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean FlashControl.isFlashReady()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param isReady  pointer to get the flash device status. The result will be
 *     \a JAVACALL_TRUE if the flash is ready, \a JAVACALL_FALSE otherwise.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a isReady is \a NULL
 */
javacall_result javacall_amms_flash_control_is_flash_ready(
    javacall_handle hNative, /*OUT*/javacall_bool *isReady) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void FlashControl.setMode(int mode)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param mode  mode to set, one of the constants:
 *     \a JAVACALL_AMMS_FLASH_MODE_OFF,
 *     \a JAVACALL_AMMS_FLASH_MODE_AUTO,
 *     \a JAVACALL_AMMS_FLASH_MODE_AUTO_WITH_REDEYEREDUCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FORCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FORCE_WITH_REDEYEREDUCE,
 *     \a JAVACALL_AMMS_FLASH_MODE_FILLIN
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a mode is not supported
 *
 * @see javacall_amms_flash_control_get_supported_mode_count
 * @see javacall_amms_flash_control_get_supported_mode
 */
javacall_result javacall_amms_flash_control_set_mode(
    javacall_handle hNative, long mode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Tests if focus control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if focus control is available
 * @retval JAVACALL_FALSE if focus control is not supported
 */
javacall_bool javacall_amms_focus_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
}

/**
 * The function corresponding to 
 * int FocusControl.setFocus()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the focus distance of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param distance  pointer to the focus to set: either distance in millimeters
 *     or a special value, one of \a JAVACALL_AMMS_FOCUS_AUTO,
 *     \a JAVACALL_AMMS_FOCUS_AUTO_LOCK, \a JAVACALL_AMMS_FOCUS_NEXT,
 *     \a JAVACALL_AMMS_FOCUS_PREVIOUS, or \a JAVACALL_AMMS_FOCUS_INFINITY.
 *     Returns the value actally set. See specification for details.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given focus setting is not supported
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a distance is \a NULL
 *
 * @see javacall_amms_focus_control_is_manual_focus_supported
 * @see javacall_amms_focus_control_is_auto_focus_supported
 */
javacall_result javacall_amms_focus_control_set_focus(
    javacall_handle hNative, /*IN/OUT*/long *distance) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int FocusControl.getFocus()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the focus setting of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param distance pointer to receive the focus setting. The result can
 *     be either distance in millimeters or one of the constants
 *     \a JAVACALL_AMMS_FOCUS_AUTO, \a JAVACALL_AMMS_FOCUS_AUTO_LOCK,
 *     \a JAVACALL_AMMS_FOCUS_UNKNOWN, \a JAVACALL_AMMS_FOCUS_INFINITY.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a distance is \a NULL
 */
javacall_result javacall_amms_focus_control_get_focus(
    javacall_handle hNative, /*OUT*/long *distance) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int FocusControl.getMinFocus()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the minimum focus distance supported.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param minFocus pointer to receive the minumum focus distance. The result
 *     can be either distance in millimeters or \a JAVACALL_AMMS_FOCUS_UNKNOWN.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a minFocus is \a NULL
 */
javacall_result javacall_amms_focus_control_get_min_focus(
    javacall_handle hNative, /*OUT*/long *minFocus) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int FocusControl.getFocusSteps()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of different focus distances that can be set.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param steps  pointer to get the value. The result will be the number of 
 *     supported focusing distances, or 0 if manual focus is not supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a steps is \a NULL
 */
javacall_result javacall_amms_focus_control_get_focus_steps(
    javacall_handle hNative, /*OUT*/long *steps) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean FocusControl.isManualFocusSupported()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param isSupported  pointer to get the result
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a isSupported is \a NULL
 */
javacall_result javacall_amms_focus_control_is_manual_focus_supported(
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean FocusControl.isAutoFocusSupported()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param isSupported  pointer to get the result
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a isSupported is \a NULL
 */
javacall_result javacall_amms_focus_control_is_auto_focus_supported(
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean FocusControlisMacroSupported()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param isSupported  pointer to get the result
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a isSupported is \a NULL
 */
javacall_result javacall_amms_focus_control_is_macro_supported(
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void FocusControl.setMacro(boolean enable)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Toggles the macro focus mode.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param enable  \a JAVACALL_TRUE to enable the macro mode,
 *     \a JAVACALL_FALSE to disable it.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given mode is not supported
 */
javacall_result javacall_amms_focus_control_set_macro(
    javacall_handle hNative, javacall_bool enable) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean FocusControl.getMacro()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current macro focus mode.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param macroMode  pointer to get the result
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a macroMode is \a NULL
 */
javacall_result javacall_amms_focus_control_get_macro(
    javacall_handle hNative, /*OUT*/javacall_bool *macroMode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Tests if snapshot control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if snapshot control is available
 * @retval JAVACALL_FALSE if snapshot control is not supported
 */
javacall_bool javacall_amms_snapshot_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
}

/**
 * The function corresponding to 
 * void SnapshotControl.setDirectory(java.lang.String directory)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the file directory where the images will be stored.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param dir  storage directory to set (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a dir is \a NULL or the directory does not exist
 */
javacall_result javacall_amms_snapshot_control_set_directory(
    javacall_handle hNative, javacall_const_utf16_string dir) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getDirectory()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Gets the storage directory.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param dir  pointer to the buffer to receive the storage directory
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a dir is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_directory(
    javacall_handle hNative, /*OUT*/javacall_utf16_string dir, long bufLength) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void SnapshotControl.setFilePrefix(java.lang.String prefix)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the filename prefix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param prefix  prefix for the files that will be created
 *     (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a prefix is \a NULL or refers to a value that cannot be set
 */
javacall_result javacall_amms_snapshot_control_set_file_prefix(
    javacall_handle hNative, javacall_const_utf16_string prefix) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getFilePrefix()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current filename prefix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param prefix  pointer to the buffer to receive the current filename prefix
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a prefix is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_file_prefix(
    javacall_handle hNative, /*OUT*/javacall_utf16_string prefix,
    long bufLength) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void SnapshotControl.setFileSuffix(java.lang.String suffix)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the filename suffix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param suffix  sufix for the files that will be created
 *     (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a suffix is \a NULL or refers to a value that cannot be set
 */
javacall_result javacall_amms_snapshot_control_set_file_suffix(
    javacall_handle hNative, javacall_const_utf16_string suffix) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getFileSuffix()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current filename suffix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param suffix  pointer to the buffer to receive the current filename suffix
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a suffix is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_file_suffix(
    javacall_handle hNative, /*OUT*/javacall_utf16_string suffix,
    long bufLength) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void SnapshotControl.start(int)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Starts burst shooting.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param maxShots  maximum number of shots to take or one of the constants
 *     \a JAVACALL_AMMS_SNAPSHOT_FREEZE,
 *     \a JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM,
 *     \a JAVACALL_AMMS_SNAPSHOT_MAX_VALUE
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if prefix and suffix have not been set
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a maxShots is less than 1 and is not one of the constants
 *          \a JAVACALL_AMMS_SNAPSHOT_FREEZE and
 *          \a JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM
 */
javacall_result javacall_amms_snapshot_control_start(
    javacall_handle hNative, long maxShots) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void SnapshotControl.stop()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Stops burst shooting.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_OK if operation is successful
 */
javacall_result javacall_amms_snapshot_control_stop(
    javacall_handle hNative) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void SnapshotControl.unfreeze(boolean save)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Unfreezes the viewfinder and saves the snapshot 
 * depending on the parameter.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param save  \a JAVACALL_TRUE to save the snapshot
 *     \a JAVACALL_FALSE not to save the snapshot
 *
 * @retval JAVACALL_OK if operation is successful
 */
javacall_result javacall_amms_snapshot_control_unfreeze(
    javacall_handle hNative, javacall_bool save) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Tests if zoom control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if zoom control is available
 * @retval JAVACALL_FALSE if zoom control is not supported
 */
javacall_bool javacall_amms_zoom_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
}

/**
 * The function corresponding to 
 * int ZoomControl.setOpticalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the optical zoom of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to the new zoom value. The value must be either not
 *     less than 100 or one of the constants \a JAVACALL_AMMS_ZOOM_NEXT,
 *     \a JAVACALL_AMMS_ZOOM_PREVIOUS.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 *        - \a level refers to a value less than 100 and not one of the
 *          constants \a JAVACALL_AMMS_ZOOM_NEXT and
 *          \a JAVACALL_AMMS_ZOOM_PREVIOUS
 *        - \a level refers to a value exceeding maximum optical zoom
 */
javacall_result javacall_amms_zoom_control_set_optical_zoom(
    javacall_handle hNative, /*IN/OUT*/long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getOpticalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current optical zoom value of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the zoom value. The result will be the
 *     current optical zoom value or \a JAVACALL_AMMS_ZOOM_UNKNOWN.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_optical_zoom(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getMaxOpticalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the maximum optical zoom value of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the maximum zoom value. The result will be
 *     100 if the camera does not support optical zoom.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_max_optical_zoom(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getOpticalZoomLevels()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of optical zoom levels.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the zoom levels number
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_optical_zoom_levels(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getMinFocalLength()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the minimum focal length of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive result. The result will be the minimum
 *     focal length in micrometers or \a JAVACALL_AMMS_ZOOM_UNKNOWN.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_min_focal_length(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.setDigitalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the digital zoom of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to the new zoom value. The value must be either not
 *     less than 100 or one of the constants \a JAVACALL_AMMS_ZOOM_NEXT,
 *     \a JAVACALL_AMMS_ZOOM_PREVIOUS.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 *        - \a level refers to a value less than 100 and not one of the
 *          constants \a JAVACALL_AMMS_ZOOM_NEXT and
 *          \a JAVACALL_AMMS_ZOOM_PREVIOUS
 *        - \a level refers to a value exceeding maximum digital zoom
 */
javacall_result javacall_amms_zoom_control_set_digital_zoom(
    javacall_handle hNative, /*IN/OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getDigitalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current digital zoom value of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the zoom value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_digital_zoom(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getMaxDigitalZoom()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the maximum digital zoom value of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the maximum zoom value. The result will be
 *     100 if the camera does not support optical zoom.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if operation failed
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_max_digital_zoom(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ZoomControl.getDigitalZoomLevels()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of digital zoom levels.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param level  pointer to receive the zoom levels number
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a level is \a NULL
 */
javacall_result javacall_amms_zoom_control_get_digital_zoom_levels(
    javacall_handle hNative, /*OUT*/ long *level) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Tests if exposure control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if exposure control is available
 * @retval JAVACALL_FALSE if exposure control is not supported
 */
javacall_bool javacall_amms_exposure_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_fstop,
 * corresponds to int[] ExposureControl.getSupportedFStops()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported apertures.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to receive the number of apertures
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_fstop
 */
javacall_result javacall_amms_exposure_control_get_supported_fstops_count(
    javacall_handle hNative, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_fstops_count,
 * corresponds to int[] ExposureControl.getSupportedFStops()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns supported aperture by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of aperture to get
 * @param count  pointer to receive the aperture value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a count is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_fstops_count
 */
javacall_result javacall_amms_exposure_control_get_supported_fstop(
    javacall_handle hNative, long index, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getFStop()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current aperture.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param fstop  pointer to receive the aperture
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a fstop is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_fstop(
    javacall_handle hNative, /*OUT*/long *fstop) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void ExposureControl.setFStop()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the aperture.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param aperture  new aperture value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given value is not supported
 *
 * @see javacall_amms_exposure_control_get_supported_fstops_count
 * @see javacall_amms_exposure_control_get_supported_fstop
 */
javacall_result javacall_amms_exposure_control_set_fstop(
    javacall_handle hNative, long aperture) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getMinExposureTime()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the minimum supported exposure time.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param eTime  pointer to receive result. The result is the minimum exposure
 *     time in microseconds or 0 if the only automatic exposure is supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a eTime is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_min_exposure_time(
    javacall_handle hNative, /*OUT*/long *eTime) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getMaxExposureTime()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the maximum supported exposure time.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param eTime  pointer to receive result. The result is the maximum exposure
 *     time in microseconds or 0 if the only automatic exposure is supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a eTime is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_max_exposure_time(
    javacall_handle hNative, /*OUT*/long *eTime) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getExposureTime()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current shutter speed.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param eTime  pointer to receive the current exposure time in microseconds.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a eTime is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_exposure_time(
    javacall_handle hNative, /*OUT*/long *eTime) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.setExposureTime()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the shutter speed.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param eTime  pointer to the new exposure time value. The value is in
 *     microseconds, 0 indicates automatic exposure time.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given time is outside of the supported range
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a eTime is \a NULL
 *
 * @see javacall_amms_exposure_control_get_min_exposure_time
 * @see javacall_amms_exposure_control_get_max_exposure_time
 */
javacall_result javacall_amms_exposure_control_set_exposure_time(
    javacall_handle hNative, /*IN/OUT*/long *eTime) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_iso,
 * corresponds to int[] ExposureControl.getSupportedISOs()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported sensitivities.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to receive the number of sensitivities
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_iso
 */
javacall_result javacall_amms_exposure_control_get_supported_isos_count(
    javacall_handle hNative, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_isos_count,
 * corresponds to int[] ExposureControl.getSupportedISOs()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns supported sensitivity by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of sensitivity to get
 * @param iso  pointer to receive the sensitivity value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a iso is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_isos_count
 */
javacall_result javacall_amms_exposure_control_get_supported_iso(
    javacall_handle hNative, long index, /*OUT*/long *iso) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getISO()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current sensitivity.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param iso  pointer to receive the current sensitivity value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a iso is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_iso(
    javacall_handle hNative, /*OUT*/long *iso) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void ExposureControl.setISO()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the sensitivity.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param iso  new sensitivity value, 0 for automatic sensitivity.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given value is not supported
 *
 * @see javacall_amms_exposure_control_get_supported_isos_count
 * @see javacall_amms_exposure_control_get_supported_iso
 */
javacall_result javacall_amms_exposure_control_set_iso(
    javacall_handle hNative, long iso) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_exposure_compensation,
 * corresponds to int[] ExposureControl.getSupportedExposureCompensations()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported exposure compensation values.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to receive the number of exposure compensation values
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_exposure_compensation
 */
javacall_result
javacall_amms_exposure_control_get_supported_exposure_compensations_count(
    javacall_handle hNative, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_exposure_compensations_count,
 * corresponds to int[] ExposureControl.getSupportedExposureCompensations()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns supported exposure compensation value by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of exposure compensation value to get
 * @param ec  pointer to receive the exposure compensation value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a ec is \a NULL
 *
 * @see
 *     javacall_amms_exposure_control_get_supported_exposure_compensations_count
 */
javacall_result
javacall_amms_exposure_control_get_supported_exposure_compensation(
    javacall_handle hNative, long index, /*OUT*/long *ec) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getExposureCompensation()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current exposure compensation.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param ec  pointer to receive the current exposure compensation value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a ec is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_exposure_compensation(
    javacall_handle hNative, /*OUT*/long *ec) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void ExposureControl.setExposureCompensation()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the exposure compensation.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param ec  new exposure compensation value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given value is not supported
 *
 * @see
 *     javacall_amms_exposure_control_get_supported_exposure_compensations_count
 * @see javacall_amms_exposure_control_get_supported_exposure_compensation
 */
javacall_result javacall_amms_exposure_control_set_exposure_compensation(
    javacall_handle hNative, long ec) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int ExposureControl.getExposureValue()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the amount of light received by the sensor with the current settings.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param ev  pointer to receive the current exposure value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a ev is \a NULL
 */
javacall_result javacall_amms_exposure_control_get_exposure_value(
    javacall_handle hNative, /*OUT*/long *ev) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_light_metering,
 * corresponds to 
 * java.lang.String[] ExposureControl.getSupportedLightMeterings()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of supported light meterings for automatic exposure
 * settings.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param count  pointer to receive the number of light meterings
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a count is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_light_metering
 */
javacall_result
javacall_amms_exposure_control_get_supported_light_meterings_count(
    javacall_handle hNative, /*OUT*/long *count) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function, together with
 * \a javacall_amms_exposure_control_get_supported_light_meterings_count,
 * corresponds to 
 * java.lang.String[] ExposureControl.getSupportedLightMeterings()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the supported light metering by index.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param index  index of light metering value to get
 * @param lm  pointer to the buffer to receive the light metering
 *     (null-terminated string)
 * @param bufLen  number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a lm is \a NULL
 *        - insufficient buffer size
 *
 * @see javacall_amms_exposure_control_get_supported_light_meterings_count
 */
javacall_result javacall_amms_exposure_control_get_supported_light_metering(
    javacall_handle hNative, long index, /*OUT*/char *lm, long bufLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void ExposureControl.setLightMetering()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the metering mode for the automatic exposure of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param lm  the new metering mode (null-terminated string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a lm is \a NULL or refers to an unsupported value
 *
 * @see javacall_amms_exposure_control_get_supported_light_meterings_count
 * @see javacall_amms_exposure_control_get_supported_light_metering
 */
javacall_result javacall_amms_exposure_control_set_light_metering(
    javacall_handle hNative, const char *lm) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String ExposureControl.getLightMetering()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current light metering mode of the camera.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param lm  pointer to the buffer to receive the light metering
 *     (null-terminated string)
 * @param bufLen  number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a lm is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_exposure_control_get_light_metering(
    javacall_handle hNative, /*OUT*/char *lm, long bufLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}
