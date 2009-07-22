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
 * Tests if tuner control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 *
 * @retval JAVACALL_TRUE if tuner control is available
 * @retval JAVACALL_FALSE if tuner control is not supported
 */
javacall_bool javacall_amms_tuner_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_FALSE;
}

/**
 * The function corresponding to 
 * int TunerControl.getMinFreq()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns minimum frequency supported for the given modulation.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param modulation  the modulation to get the minimum frequency for
 *     (null-terminated string)
 * @param freq  pointer to receive the minimum frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a modulation is \a NULL or refers to an unsupported value
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_min_freq(
    javacall_handle hNative, const char *modulation, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getMaxFreq()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns maximum frequency supported for the given modulation.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param modulation  the modulation to get the maximum frequency for
 *     (null-terminated string)
 * @param freq  pointer to receive the maximum frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a modulation is \a NULL or refers to an unsupported value
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_max_freq(
    javacall_handle hNative, const char *modulation, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.setFrequency()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Tunes to the given frequency.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param freq  pointer to the new frequency value
 * @param modulation  the modulation to be used (null-terminated string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a freq is \a NULL or refers to a value outside of the frequency
 *          band supported by the device
 *        - \a modulation is \a NULL or refers to an unsupported value
 */
javacall_result javacall_amms_tuner_control_set_frequency(
    javacall_handle hNative, /*IN/OUT*/long *freq, const char *modulation) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getFrequency()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the frequency the tuner has been tuned to.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param freq  pointer to receive the frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_frequency(
    javacall_handle hNative, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.seek()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Seeks for the next broadcast signal.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param startFreq  the frequency to start from
 * @param modulation  the modulation to be used (null-terminated string)
 * @param upwards  if \a JAVACALL_TRUE, scan towards higher frequencies;
 *     if \a JAVACALL_FALSE, scan towards lower frequencies
 * @param freq  pointer to receive the new frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if seek functionality is not available for the given
 *     modulation
 * @retval JAVACALL_WOULD_BLOCK if the operation has been started but
 *     not finished yet
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a startFreq is outside of the frequency band supported by the
 *          device
 *        - \a modulation is \a NULL or refers to an unsupported value
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_seek(
    javacall_handle hNative, long startFreq, const char *modulation,
    javacall_bool upwards, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.seek()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the result of the seek operation that was not able to complete
 * immediately.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param freq  pointer to receive the new frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_seek_result(
    javacall_handle hNative, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * boolean TunerControl.getSquelch()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current squelching (muting the frequencies without broadcast)
 * setting.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param squelched  pointer to receive the squelch setting
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a squelched is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_squelch(
    javacall_handle hNative, /*OUT*/javacall_bool *squelched) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void TunerControl.setSquelch()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets squelching (muting the frequencies without broadcast) on or off.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param squelch  \a JAVACALL_TRUE to turn squelching on,
 *     \a JAVACALL_FALSE to turn squelching off
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the given squelch setting is not supported
 */
javacall_result javacall_amms_tuner_control_set_squelch(
    javacall_handle hNative, javacall_bool squelch) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String TunerControl.getModulation()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns modulation in use.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param modulation  pointer to the buffer to receive the modulation
 *     (null-terminated string)
 * @param bufLen  number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a modulation is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_tuner_control_get_modulation(
    javacall_handle hNative, /*OUT*/char *modulation, long bufLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getSignalStrength()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the strength of the recepted signal.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param strength  pointer to receive the signal strength
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if querying the signal strength is not supported
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a strength is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_signal_strength(
    javacall_handle hNative, /*OUT*/long *strength) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getStereoMode()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the stereo mode in use.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param stereoMode  pointer to receive the current stereo mode. The result
 *     can be one the constants \a JAVACALL_AMMS_TUNER_MONO,
 *     \a JAVACALL_AMMS_TUNER_STEREO, \a JAVACALL_AMMS_TUNER_AUTO.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a stereoMode is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_stereo_mode(
    javacall_handle hNative, /*OUT*/long *stereoMode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void TunerControl.setStereoMode()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the stereo mode.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param mode  the new stereo mode, either one of the constants
 *     \a JAVACALL_AMMS_TUNER_MONO, \a JAVACALL_AMMS_TUNER_STEREO,
 *     \a JAVACALL_AMMS_TUNER_AUTO.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - the given mode is not supported
 */
javacall_result javacall_amms_tuner_control_set_stereo_mode(
    javacall_handle hNative, long mode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getNumberOfPresets()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the number of presets.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param number  pointer to receive the number of presets. The result is
 *     0 if presets are not supported.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a number is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_number_of_presets(
    javacall_handle hNative, /*OUT*/long *number) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void TunerControl.usePreset()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Tunes the tuner by using the preset settings.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to be used
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 */
javacall_result javacall_amms_tuner_control_use_preset(
    javacall_handle hNative, long preset) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void TunerControl.setPreset()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Configures the preset using the given settings.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to be configured
 * @param freq  the frequency in 100 Hertzs
 * @param modulation  the modulation to be used (null-terminated string)
 * @param stereoMode  the stereo mode to be used
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a freq is not available
 *        - \a modulation is \a NULL or refers to an unsupported value
 *        - \a stereoMode is not supported
 */
javacall_result javacall_amms_tuner_control_set_preset(
    javacall_handle hNative, long preset, long freq, const char *modulation,
    long stereoMode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getPresetFrequency()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the preset's frequency.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to get frequency for
 * @param freq  pointer to receive the frequency value
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a freq is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_preset_frequency(
    javacall_handle hNative, long preset, /*OUT*/long *freq) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String TunerControl.getPresetModulation()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the preset's modulation.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to get modulation for
 * @param modulation  pointer to the buffer to receive the modulation
 *     (null-terminated string)
 * @param bufLen  number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a modulation is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_tuner_control_get_preset_modulation(
    javacall_handle hNative, long preset, /*OUT*/char *modulation, long bufLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * int TunerControl.getPresetStereoMode()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the preset's stereo mode.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to get stereo mode for
 * @param stereoMode  pointer to receive the stereo mode. The result
 *     can be one the constants \a JAVACALL_AMMS_TUNER_MONO,
 *     \a JAVACALL_AMMS_TUNER_STEREO, \a JAVACALL_AMMS_TUNER_AUTO.
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if the presets do not support storing of stereo mode
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a stereoMode is \a NULL
 */
javacall_result javacall_amms_tuner_control_get_preset_stereo_mode(
    javacall_handle hNative, long preset, /*OUT*/long *stereoMode) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * java.lang.String TunerControl.getPresetName()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the preset name.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to get name of
 * @param name  pointer to the buffer to receive the name
 *     (null-terminated string)
 * @param bufLen  number of bytes the buffer can hold
 *     including null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a name is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_tuner_control_get_preset_name(
    javacall_handle hNative, long preset, /*OUT*/char *name, long bufLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The function corresponding to 
 * void TunerControl.setPresetName()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the preset name.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a radio player,
 *        - the radio player has acquired exclusive
 *          access to the radio reciever device
 * @param preset  the preset to set name of
 * @param name  the new name (null-terminated string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a preset is less than 1 or greater than the number of presets
 *        - \a name is \a NULL
 */
javacall_result javacall_amms_tuner_control_set_preset_name(
    javacall_handle hNative, long preset, const char *name) {
    return JAVACALL_NOT_IMPLEMENTED;
}
