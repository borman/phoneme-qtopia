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

#ifndef SIZEOF_ARRAY
#define SIZEOF_ARRAY( a ) ( sizeof( a ) / sizeof( (a)[ 0 ] ) )
#endif // SIZEOF_ARRAY

static int g_SupportedFlashModes[] = {
    JAVACALL_AMMS_FLASH_MODE_OFF,
    JAVACALL_AMMS_FLASH_MODE_AUTO,
    JAVACALL_AMMS_FLASH_MODE_AUTO_WITH_REDEYEREDUCE,
    JAVACALL_AMMS_FLASH_MODE_FORCE,
    JAVACALL_AMMS_FLASH_MODE_FORCE_WITH_REDEYEREDUCE,
    JAVACALL_AMMS_FLASH_MODE_FILLIN
};

static const char * g_FlashModeNames[] = {
    "OFF",
    "AUTO",
    "AUTO + RED EYE",
    "FORCE",
    "FORCE + RED EYE",
    "FILLIN"
};

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
    javacall_handle hNative)
{
    return JAVACALL_TRUE;
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
    javacall_handle hNative, /*OUT*/long *mode)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == mode ) return JAVACALL_INVALID_ARGUMENT;
    *mode = cs->flashMode;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *count)
{
    if( NULL == count ) return JAVACALL_INVALID_ARGUMENT;
    *count = SIZEOF_ARRAY( g_SupportedFlashModes );
    return JAVACALL_OK;
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
    javacall_handle hNative, long index, /*OUT*/long *mode)
{
    if( NULL == mode || 
        index < 0 || 
        index >= SIZEOF_ARRAY( g_SupportedFlashModes ) )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }

    *mode = g_SupportedFlashModes[ index ];

    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/javacall_bool *isReady)
{
    if( NULL == isReady ) return JAVACALL_INVALID_ARGUMENT;
    *isReady = JAVACALL_TRUE;
    return JAVACALL_OK;
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
    javacall_handle hNative, long mode)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );
    int i;

    for( i = 0; i < SIZEOF_ARRAY( g_SupportedFlashModes ); i++ )
    {
        if( mode == g_SupportedFlashModes[ i ] )
        {
            cs->flashMode = mode;
            mmSetStatusLine( "Flash: %s", g_FlashModeNames[ i ] );
            return JAVACALL_OK;
        }
    }
    return JAVACALL_INVALID_ARGUMENT;
}
