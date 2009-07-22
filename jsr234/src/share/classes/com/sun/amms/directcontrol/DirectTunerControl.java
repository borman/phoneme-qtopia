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

package com.sun.amms.directcontrol;
import javax.microedition.media.MediaException;
import javax.microedition.amms.control.tuner.*;
import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.AMMSPermission;

public class DirectTunerControl implements TunerControl {

    private int _nativeHandle;
    
    public static DirectTunerControl createInstance(int hNative, String locator) {
        if (!nIsSupported(hNative)) {
            return null;
        }
        return new DirectTunerControl(hNative,locator);
    }

    private String extractParam(String locator, String param_name) {
        int pos = locator.indexOf(param_name + "=");
        if (-1 == pos) return null;
        String val = locator.substring(pos + param_name.length() + 1);
        int amp_pos = val.indexOf('&');
        if (-1 != amp_pos) val = val.substring(0, amp_pos);
        return val;
    }
    
    private DirectTunerControl(int hNative, String locator) {
        _nativeHandle = hNative;

        try {
            String mod    = getModulation();
            int    freq   = getFrequency();

            String loc_mod    = extractParam(locator, "mod");
            String loc_freq   = extractParam(locator, "f"  );
            String loc_stereo = extractParam(locator, "st" );

            boolean freq_specified = false;

            if (null != loc_freq) {
                try {
                    freq = Integer.valueOf(loc_freq).intValue();
                    freq_specified = true;
                } catch (Exception e) {
                }
            }

            if( !freq_specified ) {
                // frequency is not specified, but if modulation is changed,
                // let's make sure freq is within valid range
                if (null != loc_mod && !mod.equals(loc_mod)) {
                    freq = getMinFreq(loc_mod);
                }
            }

            setFrequency(freq, (null != loc_mod) ? loc_mod : mod);

            if (loc_stereo.equals("mono"))
                setStereoMode( MONO );
            else if (loc_stereo.equals("stereo"))
                setStereoMode( STEREO );
            else if (loc_stereo.equals("auto"))
                setStereoMode( AUTO );

        } catch (Exception e) {
        }
    }

    /**
     * Check the &quot;set preset&quot; permission.
     */
    protected void checkPermission() {
        try {
            AppPackage.getInstance().
                checkForPermission(AMMSPermission.TUNER_SETPRESET);
        } catch (InterruptedException ie) {
            throw new SecurityException(
                "Interrupted while trying to ask the user permission");
        }
    }

    /**
     * Check stereo mode validity.
     */
    protected void checkStereoMode(int mode)
    {
        if (MONO != mode && STEREO != mode && AUTO != mode) {
            throw new IllegalArgumentException("Unsupported stereo mode (" + mode + ")");
        }
    }

    /**
     * Gets the minimum frequency supported by this tuner
     * with the given modulation.
     *
     * @param modulation The modulation whose supported minimum frequency
     * is asked.
     * @return The minimum frequency in 100 Hertzs.
     * @throws IllegalArgumentException if the <code>modulation</code> is not
     * supported or it is null.
     */
    public int getMinFreq(String modulation) {
        return nGetMinFreq(_nativeHandle, modulation);
    }

    /**
     * Gets the maximum frequency supported by this tuner
     * with the given modulation.
     *
     * @param modulation The modulation whose supported maximum frequency
     * is asked.
     * @return The maximum frequency in 100 Hertzs.
     * @throws IllegalArgumentException if the <code>modulation</code> is not
     * supported or it is null.
     */
    public int getMaxFreq(String modulation) {
        return nGetMaxFreq(_nativeHandle, modulation);
    }

    /**
     * Tunes to the given frequency or to the closest supported frequency.
     *
     * @param freq The frequency in 100 Hertzs that will be taken into use.
     * If that frequency is not supported, the closest supported
     * frequency will be taken into use.
     *
     * @param modulation The modulation to be used. <code>TunerControl</code>
     * specifies predefined constants <code>MODULATION_FM</code> and
     * <code>MODULATION_AM</code> but other modulations can be supported as
     * well. Supported modulations can be queried by <code>System</code>
     * property <code>tuner.modulations</code>.
     *
     * @throws IllegalArgumentException if <code>freq</code> is not inside the
     * frequency band supported by the device or if the <code>modulation</code>
     * is not supported or the <code>modulation</code> is null.
     *
     * @return the frequency in 100 Hertzs that was taken into use.
     */
    public int setFrequency(int freq, String modulation) {
        return nSetFrequency(_nativeHandle, freq, modulation);
    }

    /**
     * Gets the frequency which the tuner has been tuned to.
     *
     * @return The frequency to which the device has been tuned, in 100 Hertz
     * units.
     */
    public int getFrequency() {
        return nGetFrequency(_nativeHandle);
    }

    /**
     * <p>Seeks for the next broadcast signal. If the end of the Player's
     * frequency band is reached before a signal was found, the scan
     * continues from the other end until a signal is found or the
     * starting frequency is reached.</p>
     *
     * <p>After seeking, the frequency of the Player is the one that
     * was returned or if nothing was found, the original frequency.</p>
     *
     * @param startFreq the frequency in 100 Hertzs wherefrom the scan
     * starts (inclusive)
     *
     * @param modulation The modulation to be used. <code>TunerControl</code>
     * specifies predefined constants <code>MODULATION_FM</code> and
     * <code>MODULATION_AM</code> but other modulations can be supported as
     * well. Supported modulations can be queried by <code>System</code>
     * property <code>tuner.modulations</code>.
     * @param upwards if <code>true</code>, the scan proceeds towards higher
     * frequencies, otherwise towards lower frequencies
     *
     * @return The found frequency in 100 Hertzs or, if no signal was
     * found, 0.
     *
     * @throws IllegalArgumentException if <code>startFreq</code> is not
     * between the supported minimum and maximum frequencies or if the
     * <code>modulation</code> is null.
     * @throws MediaException if the seek functionality is not available for
     * the given modulation.
     */
    public int seek(int startFreq, String modulation, boolean upwards)
        throws MediaException {
        return nSeek(_nativeHandle, startFreq, modulation, upwards);
    }

    /**
     * Gets the current squelching (muting in frequencies without broadcast)
     * setting.
     *
     * @return <code>true</code> if squelch is on or <code>false</code> if
     * squelch is off.
     */
    public boolean getSquelch() {
        return nGetSquelch(_nativeHandle);
    }

    /**
     * Sets squelching on or off. Squelching means muting the frequencies
     * that do not contain radio broadcast.
     *
     * @param squelch <code>true</code> to turn the squelch on or
     * <code>false</code> to turn the squelch off.
     * @throws MediaException if the given squelch setting is not supported.
     */
    public void setSquelch(boolean squelch) throws MediaException {
        nSetSquelch(_nativeHandle, squelch);
    }

    /**
     * Gets the modulation in use.
     *
     * @return The modulation currently in use.
     */
    public String getModulation() {
        return nGetModulation(_nativeHandle);
    }

    /**
     * Gets the strength of the recepted signal.
     *
     * @return A value between 0 and 100 where 0 means the faintest and 100 the
     * strongest possible signal strength.
     * @throws MediaException if querying the signal strength is not supported.
     */
    public int getSignalStrength() throws MediaException {
        return nGetSignalStrength(_nativeHandle);
    }

    /**
     * Gets the stereo mode in use.
     *
     * @return The stereo mode in use. Stereo mode is one of <code>MONO</code>,
         * <code>STEREO</code> or <code>AUTO</code>.
     */
    public int getStereoMode() {
        return nGetStereoMode(_nativeHandle) ;
    }

    /**
     * Sets the stereo mode.
     *
     * @param mode The stereo mode to be used. Stereo mode is one of
     * <code>MONO</code>, <code>STEREO</code> or <code>AUTO</code>.
     * @throws IllegalArgumentException if the given mode is not supported.
     */
    public void setStereoMode(int mode) {
        checkStereoMode(mode);
        nSetStereoMode(_nativeHandle, mode);
    }

    /**
     * Gets the number of presets. The numbering of presets starts from one and
     * the largest preset number equals the value returned from this method.
     *
     * @return The number of presets, or zero if the presets are not supported.
     */
    public int getNumberOfPresets() {
        return nGetNumberOfPresets(_nativeHandle);
    }

    /**
     * Tunes the tuner by using settings specified in the preset. Changes to
     * presets following a <code>usePreset</code> call do not tune the tuner
     * automatically.
     *
     * @param preset the preset to be used.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets.
     */
    public void usePreset(int preset) {
        nUsePreset(_nativeHandle, preset);
    }

    /**
     * Configures the preset using current frequency and modulation
     * (and stereo mode if native presets support storing it).
     *
     * @param preset the preset to be set.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of preset range.
     * @throws SecurityException if setting presets has been prohibited.
     */
    public void setPreset(int preset) {
        setPreset(preset, getFrequency(), getModulation(), getStereoMode());
    }

    /**
     * Configures the preset using given settings.
     * The stereo mode might not be stored if it is not supported by the
     * presets. (In that case, <code>IllegalArgumentException</code> is not
     * thrown.)
     *
     * @param preset the preset to be configured.
     * @param freq the frequency of the preset in 100 Hertzs.
     * @param mod the modulation of the preset.
     * @param stereoMode the stereo mode of the preset.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets or
     * <code>freq</code> or <code>modulation</code> are not available or if the
     * <code>modulation</code> is null or if <code>stereoMode</code> is not a
     * supported stereo mode.
     * @throws SecurityException if setting presets has been prohibited.
     */
    public void setPreset(int preset, int freq, String mod, int stereoMode) {
        checkPermission();
        checkStereoMode(stereoMode);
        nSetPreset(_nativeHandle, preset, freq, mod, stereoMode);
    }

    /**
     * Gets the preset's frequency.
     *
     * @param preset the preset whose frequency is to be returned.
     * @return The frequency of the preset in 100 Hertzs.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets.
     */
    public int getPresetFrequency(int preset) {
        return nGetPresetFrequency(_nativeHandle, preset);
    }

    /**
     * Gets the preset's modulation.
     *
     * @param preset the preset whose modulation is to be returned.
     * @return The modulation of the preset.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets.
     */
    public String getPresetModulation(int preset) {
        return nGetPresetModulation(_nativeHandle, preset);
    }

    /**
     * Gets the preset's stereo mode.
     *
     * @param preset the preset whose stereo mode is to be returned.
     * @return The stereo mode of the preset. Stereo mode is one of
     * <code>MONO</code>, <code>STEREO</code> or <code>AUTO</code>.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets.
     * @throws MediaException if the presets do not support storing of the
     * stereo mode.
     */
    public int getPresetStereoMode(int preset) throws MediaException {
        return nGetPresetStereoMode(_nativeHandle, preset);
    }

    /**
     * Gets the preset name.
     *
     * @param preset the preset whose name is to be returned.
     * @return A <code>String</code> containing the preset name.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets.
     */
    public String getPresetName(int preset) {
        return nGetPresetName(_nativeHandle, preset);
    }

    /**
     * Sets the preset name.
     *
     * @param preset the preset whose name is to be set.
     * @param name the name of the preset.
     * @throws IllegalArgumentException if <code>preset</code> &lt 1 or
     * <code>preset</code> &gt number of presets or
     * if the <code>name</code> is null.
     * @throws SecurityException if setting presets has been prohibited.
     */
    public void setPresetName(int preset, String name) {
        checkPermission();
        if( null == name ) {
            throw new IllegalArgumentException("setPresetName(): name is null");
        }

        nSetPresetName(_nativeHandle, preset, name);
    }

    protected native int nGetMinFreq(int hNative, String modulation);
    protected native int nGetMaxFreq(int hNative, String modulation);
    protected native int nSetFrequency(int hNative, int freq,
        String modulation);
    protected native int nGetFrequency(int hNative);
    protected native int nSeek(int hNative, int startFreq, String modulation,
        boolean upwards);
    protected native boolean nGetSquelch(int hNative);
    protected native void nSetSquelch(int hNative, boolean squelch);
    protected native String nGetModulation(int hNative);
    protected native int nGetSignalStrength(int hNative);
    protected native int nGetStereoMode(int hNative);
    protected native void nSetStereoMode(int hNative, int mode);
    protected native int nGetNumberOfPresets(int hNative);
    protected native void nUsePreset(int hNative, int preset);
    protected native void nSetPreset(int hNative, int preset, int freq,
        String mod, int stereoMode);
    protected native int nGetPresetFrequency(int hNative, int preset);
    protected native String nGetPresetModulation(int hNative, int preset);
    protected native int nGetPresetStereoMode(int hNative, int preset);
    protected native String nGetPresetName(int hNative, int preset);
    protected native void nSetPresetName(int hNative, int preset, String name);
    protected static native boolean nIsSupported(int hNative);

}
