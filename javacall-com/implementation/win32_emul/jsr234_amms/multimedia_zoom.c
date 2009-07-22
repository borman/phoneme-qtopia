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

#define MAX_OPTICAL_ZOOM    200
#define MAX_DIGITAL_ZOOM    200

static void display_status( camera_state* cs )
{
    mmSetStatusLine( "Zoom: Opt %.2fX, Dig %.2fX",
        (float)cs->zoomOptical / 100.0f,
        (float)cs->zoomDigital / 100.0f );
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
    javacall_handle hNative)
{
    return JAVACALL_TRUE;
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
    javacall_handle hNative, /*IN/OUT*/long *level)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;

    if( JAVACALL_AMMS_ZOOM_PREVIOUS == *level )
    {
        if( cs->zoomOptical > 100 ) cs->zoomOptical--;
        *level = cs->zoomOptical;
    }
    else if( JAVACALL_AMMS_ZOOM_NEXT == *level )
    {
        if( cs->zoomOptical < MAX_OPTICAL_ZOOM ) cs->zoomOptical++;
        *level = cs->zoomOptical;
    }
    else if( *level < 100 || *level > MAX_OPTICAL_ZOOM )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    else
    {
        cs->zoomOptical = *level;
    }
    
    display_status( cs );

    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *level)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;
    *level = cs->zoomOptical;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *level)
{
    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;
    *level = MAX_OPTICAL_ZOOM;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *levels)
{
    if( NULL == levels ) return JAVACALL_INVALID_ARGUMENT;
    *levels = ( MAX_OPTICAL_ZOOM - 100 ) + 1;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *level)
{
    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;
    *level = 28000;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*IN/OUT*/ long *level)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;

    if( JAVACALL_AMMS_ZOOM_PREVIOUS == *level )
    {
        if( cs->zoomDigital > 100 ) cs->zoomDigital--;
        *level = cs->zoomDigital;
    }
    else if( JAVACALL_AMMS_ZOOM_NEXT == *level )
    {
        if( cs->zoomDigital < MAX_DIGITAL_ZOOM ) cs->zoomDigital++;
        *level = cs->zoomDigital;
    }
    else if( *level < 100 || *level > MAX_DIGITAL_ZOOM )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    else
    {
        cs->zoomDigital = *level;
    }

    display_status( cs );

    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *level)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;
    *level = cs->zoomDigital;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *level)
{
    if( NULL == level ) return JAVACALL_INVALID_ARGUMENT;
    *level = MAX_DIGITAL_ZOOM;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/ long *levels)
{
    if( NULL == levels ) return JAVACALL_INVALID_ARGUMENT;
    *levels = ( MAX_DIGITAL_ZOOM - 100 ) + 1;
    return JAVACALL_OK;
}
