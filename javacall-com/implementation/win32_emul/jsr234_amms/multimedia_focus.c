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

static void display_status( camera_state* cs )
{
    mmSetStatusLine( "Focus: %s, %s", 
        (( JAVACALL_AMMS_FOCUS_AUTO == cs->focusDistance ) ? "AF" : "AF Lock"),
        (( cs->focusMacroMode ? "Macro" : "Normal" )) );
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
    javacall_handle hNative )
{
    return JAVACALL_TRUE;
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
    javacall_handle hNative, /*IN/OUT*/long *distance)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == distance ) return JAVACALL_INVALID_ARGUMENT;

    if( JAVACALL_AMMS_FOCUS_AUTO == *distance ||
        JAVACALL_AMMS_FOCUS_AUTO_LOCK == *distance )
    {
        cs->focusDistance = *distance;
    }
    else
    {
        return JAVACALL_FAIL;
    }


    display_status( cs );

    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *distance)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == distance ) return JAVACALL_INVALID_ARGUMENT;
    *distance = cs->focusDistance;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *minFocus)
{
    if( NULL == minFocus ) return JAVACALL_INVALID_ARGUMENT;
    *minFocus = JAVACALL_AMMS_FOCUS_UNKNOWN;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/long *steps)
{
    if( NULL == steps ) return JAVACALL_INVALID_ARGUMENT;
    *steps = 0;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported)
{
    if( NULL == isSupported ) return JAVACALL_INVALID_ARGUMENT;
    *isSupported = JAVACALL_FALSE;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported)
{
    if( NULL == isSupported ) return JAVACALL_INVALID_ARGUMENT;
    *isSupported = JAVACALL_TRUE;
    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/javacall_bool *isSupported)
{
    if( NULL == isSupported ) return JAVACALL_INVALID_ARGUMENT;
    *isSupported = JAVACALL_TRUE;
    return JAVACALL_OK;
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
    javacall_handle hNative, javacall_bool enable)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    cs->focusMacroMode = enable;

    display_status( cs );

    return JAVACALL_OK;
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
    javacall_handle hNative, /*OUT*/javacall_bool *macroMode)
{
    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( NULL == macroMode ) return JAVACALL_INVALID_ARGUMENT;
    *macroMode = cs->focusMacroMode;
    return JAVACALL_OK;
}
