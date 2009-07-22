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

#include <string.h>

#ifndef SIZEOF_ARRAY
#define SIZEOF_ARRAY( a ) ( sizeof( a ) / sizeof( (a)[ 0 ] ) )
#endif // SIZEOF_ARRAY

static long g_ExposureSupportedFStops[] = { 0, 400, 560, 800, 1600 };
static long g_ExposureSupportedISOs[]   = { 0, 100, 200, 400, 800 };

static const char* g_MatrixMeteringString = "matrix";

static void display_status( camera_state* cs )
{
    mmSetStatusLine( "Exp: ISO%i, Auto, F%.1f, +0, matrix", 
        cs->exposureCurrentISO,
        (float)(cs->exposureCurrentFStop) / 100.0f );
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
    javacall_handle hNative)
{
    return JAVACALL_TRUE;
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
    javacall_handle hNative, /*OUT*/long *count)
{
    if( NULL == count ) return JAVACALL_INVALID_ARGUMENT;
    *count = SIZEOF_ARRAY( g_ExposureSupportedFStops );
    return JAVACALL_OK;
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
 * @param fstop  pointer to receive the aperture value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a index is out of range
 *        - \a fstop is \a NULL
 *
 * @see javacall_amms_exposure_control_get_supported_fstops_count
 */
javacall_result javacall_amms_exposure_control_get_supported_fstop(
    javacall_handle hNative, long index, /*OUT*/long *fstop)
{
    if( NULL == fstop ||
        index < 0 || 
        index >= SIZEOF_ARRAY( g_ExposureSupportedFStops ) )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *fstop = g_ExposureSupportedFStops[ index ];
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *fstop)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == fstop ) return JAVACALL_INVALID_ARGUMENT;
    *fstop = cs->exposureCurrentFStop;
    return JAVACALL_OK;
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
    javacall_handle hNative, long aperture)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );
    int i;

    for( i = 0; i < SIZEOF_ARRAY( g_ExposureSupportedFStops ); i++ )
    {
        if( g_ExposureSupportedFStops[ i ] == aperture )
        {
            cs->exposureCurrentFStop = aperture;
            display_status( cs );
            return JAVACALL_OK;
        }
    }
    return JAVACALL_FAIL;
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
    javacall_handle hNative, /*OUT*/long *eTime)
{
    if( NULL == eTime ) return JAVACALL_INVALID_ARGUMENT;
    *eTime = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *eTime)
{
    if( NULL == eTime ) return JAVACALL_INVALID_ARGUMENT;
    *eTime = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *eTime)
{
    if( NULL == eTime ) return JAVACALL_INVALID_ARGUMENT;
    *eTime = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*IN/OUT*/long *eTime)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == eTime ) return JAVACALL_INVALID_ARGUMENT;
    if( 0 != *eTime ) return JAVACALL_FAIL;
    display_status( cs );
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *count)
{
    if( NULL == count ) return JAVACALL_INVALID_ARGUMENT;
    *count = SIZEOF_ARRAY( g_ExposureSupportedISOs );
    return JAVACALL_OK;
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
    javacall_handle hNative, long index, /*OUT*/long *iso)
{
    if( NULL == iso ||
        index < 0 || 
        index >= SIZEOF_ARRAY( g_ExposureSupportedISOs ) )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *iso = g_ExposureSupportedISOs[ index ];
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *iso)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == iso ) return JAVACALL_INVALID_ARGUMENT;
    *iso = cs->exposureCurrentISO;
    return JAVACALL_OK;
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
    javacall_handle hNative, long iso)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );
    int i;

    for( i = 0; i < SIZEOF_ARRAY( g_ExposureSupportedISOs ); i++ )
    {
        if( g_ExposureSupportedISOs[ i ] == iso )
        {
            cs->exposureCurrentISO = iso;
            display_status( cs );
            return JAVACALL_OK;
        }
    }
    return JAVACALL_FAIL;
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
    javacall_handle hNative, /*OUT*/long *count)
{
    if( NULL == count ) return JAVACALL_INVALID_ARGUMENT;
    *count = 1;
    return JAVACALL_OK;
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
    javacall_handle hNative, long index, /*OUT*/long *ec)
{
    if( NULL == ec || 0 != index ) return JAVACALL_INVALID_ARGUMENT;
    *ec = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *ec)
{
    if( NULL == ec ) return JAVACALL_INVALID_ARGUMENT;
    *ec = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, long ec)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( 0 != ec ) return JAVACALL_FAIL;
    display_status( cs );
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *ev)
{
    if( NULL == ev ) return JAVACALL_INVALID_ARGUMENT;
    *ev = 20000; // IMPL_NOTE: just an arbitrary value
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *count)
{
    if( NULL == count ) return JAVACALL_INVALID_ARGUMENT;
    *count = 1;
    return JAVACALL_OK;
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
    javacall_handle hNative, long index, /*OUT*/char *lm, long bufLen)
{
    if( 0 != index || 
        NULL == lm || 
        bufLen < (long)strlen( g_MatrixMeteringString ) + 1 )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    strcpy( lm, g_MatrixMeteringString );
    return JAVACALL_OK;
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
    javacall_handle hNative, const char *lm)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == lm || strcmp( lm, g_MatrixMeteringString ) )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    display_status( cs );
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/char *lm, long bufLen)
{
    if( NULL == lm || 
        bufLen < (long)strlen( g_MatrixMeteringString ) + 1 )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    strcpy( lm, g_MatrixMeteringString );
    return JAVACALL_OK;
}
