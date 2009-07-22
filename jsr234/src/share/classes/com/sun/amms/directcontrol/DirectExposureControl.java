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
import javax.microedition.amms.control.camera.*;


public class DirectExposureControl implements ExposureControl {

    private int _nativeHandle;
    private int [] _supportedFStops;
    private int [] _supportedISOs;
    private int [] _supportedExpComps;
    private String [] _supportedLightMtrs;
    
    public static DirectExposureControl createInstance(int hNative) {
        if (!nIsSupported(hNative)) {
            return null;
        }
        return new DirectExposureControl(hNative);
    }
    
    private DirectExposureControl(int hNative) {
        _nativeHandle = hNative;
    }
    
    /**
     * Returns supported apertures.
     * Adjusting the optical zoom (via ZoomControl) might change the list.
     *
     * <p>
     *
     * </p>
     * @return the supported F-Stop numbers multiplied by 100.
     * For example, returned value 280 would mean an F-Stop number f/2.8.
     * 0 means automatic aperture.
     */
    public int[] getSupportedFStops() {
        if (_supportedFStops == null) {
            int num = nGetSupportedFStopsNumber(_nativeHandle);
            if (num > 0) {
                _supportedFStops = new int [num];
                for (int i = 0; i < num; i++) {
                    _supportedFStops[i] = nGetSupportedFStopByIndex(
                            _nativeHandle, i);
                }
            }
        }
        return _supportedFStops;
    }

    /**
     * Returns the current aperture.
     *
     * @return the current aperture as an F-Stop number multiplied by 100.
     * For example, returned value 280 would mean an F-Stop number f/2.8.
     */
    public int getFStop() {
        return nGetFStop(_nativeHandle);
    }


    /**
     * Sets the aperture. Adjusting the optical zoom (via ZoomControl) might
     * change the value. Therefore, it is recommended to set the optical zoom
     * first and then set the aperture if needed.
     *
     * @param aperture aperture as an F-Stop number multiplied by 100 or 0 for
     * automatic aperture. For example, returned value 280 would mean an F-Stop
     * number f/2.8.
     *
     * @throws MediaException if the given value is not supported.
     */
    public void setFStop(int aperture) throws MediaException {
        nSetFStop(_nativeHandle, aperture);
    }

    /**
     * Gets the minimum supported exposure time.
     *
     * @return the minimum suported exposure time in microseconds
     * or 0 if only automatic exposure is supported.
     */
    public int getMinExposureTime() {
        return nGetMinExposureTime(_nativeHandle);
    }

    /**
     * Gets the maximum supported exposure time
     * or 0 if only automatic exposure is supported.
     *
     * @return the maximum supported exposure time in microseconds.
     */
    public int getMaxExposureTime() {
        return nGetMaxExposureTime(_nativeHandle);
    }

    /**
     * Gets the current shutter speed.
     *
     * @return the current exposure time in microseconds.
     */
    public int getExposureTime() {
        return nGetExposureTime(_nativeHandle);
    }

    /**
     * Sets the shutter speed.
     *
     * @param time exposure time in microseconds or 0 for automatic exposure
     * time.
     * @return exposure time that was actually set.
     *
     * @throws MediaException if the given value is not supported.
     */
    public int setExposureTime(int time) throws MediaException {
        return nSetExposureTime(_nativeHandle, time);
    }

    /**
     * Returns supported sensitivities.
     *
     * @return the supported sensitivities as ISO values.
     * For example, returned value 200 would mean an ISO 200.
     * 0 means automatic sensitivity.
     */
    public int[] getSupportedISOs() {
        if (_supportedISOs == null) {
            int num = nGetSupportedISOsNumber(_nativeHandle);
            if (num > 0) {
                _supportedISOs = new int [num];
                for (int i = 0; i < num; i++) {
                    _supportedISOs[i] = nGetSupportedISOByIndex(
                            _nativeHandle, i);
                }
            }
        }
        return _supportedISOs;
    }


    /**
     * Gets the current sensitivity.
     *
     * @return the current sensitivity as ISO value.
     * For example, value 200 means ISO 200.
     */
    public int getISO() {
        return nGetISO(_nativeHandle);
    }

    /**
     * Sets the sensitivity.
     *
     * @param iso sensitivity as an ISO value.
     * For example, value 200 means ISO 200.
     * 0 means automatic sensitivity.
     *
     * @throws MediaException if the given value is not supported.
     */
    public void setISO(int iso) throws MediaException {
        nSetISO(_nativeHandle, iso);
    }

    /**
     * Gets the supported exposure compensation values. If none are supported
     * just one zero will be returned.
     *
     * @return the supported exposure compensation values multiplied by 100.
     * For example, a returned value 100 means 1.0 (that means doubling the
     * light exposure).
     */
    public int[] getSupportedExposureCompensations() {
        if (_supportedExpComps == null) {
            int num = nGetSupportedExpCompsNumber(_nativeHandle);
            if (num > 0) {
                _supportedExpComps = new int [num];
                for (int i = 0; i < num; i++) {
                    _supportedExpComps[i] = nGetSupportedExpCompByIndex(
                            _nativeHandle, i);
                }
            }
        }
        return _supportedExpComps;
    }

    /**
     * Gets the current exposure compensation.
     *
     * @return the current exposure compensation values multiplied by 100.
     * For example, a returned value 100 means 1.0 (that means doubling the
     * light exposure).
     */
    public int getExposureCompensation() {
        return nGetExposureCompensation(_nativeHandle);
    }

    /**
     * Sets the exposure compensation. This only affects when using automatic
     * exposure settings.
     *
     * @param ec the wanted exposure compensation value multiplied by 100.
     * For example, a value 100 means 1.0 (that means doubling the light
     * exposure).
     *
     * @throws MediaException if the given value is not supported.
     */
    public void setExposureCompensation(int ec) throws MediaException {
        nSetExposureCompensation(_nativeHandle, ec);
    }


    /**
     * Returns the amount of light received by the sensor due
     * to current settings for aperture, shutter speed and sensitivity.
     *
     * @return the current exposure value (EV).
     */
    public int getExposureValue() {
        return nGetExposureValue(_nativeHandle);
    }

    /**
     * Returns a list of light meterings for automatic exposure settings
     * supported by the camera device. Available meterings might include:
     * <ul>
     * <li><code>matrix</code> (the scene is split into a matrix and each zone
     * is measured and has a weighted algorithm.)</li>
     * <li><code>center-weighted</code> (the metering weighs the center of the
     * image highest (this is the typical normal setting))</li>
     * <li><code>spot</code> (the metering uses just the center and all the rest
     * is ignored).</li>
     * </ul>
     * @return Supported meterings.
     */
    public String[] getSupportedLightMeterings() {
        if (_supportedLightMtrs == null) {
            int num = nGetSupportedLightMtrsNumber(_nativeHandle);
            if (num > 0) {
                _supportedLightMtrs = new String [num];
                for (int i = 0; i < num; i++) {
                    _supportedLightMtrs[i] = nGetSupportedLightMtrByIndex(
                            _nativeHandle, i);
                }
            }
        }
        return _supportedLightMtrs;
    }

    /**
     * Sets the metering mode for the automatic exposure of the camera device.
     * This only affects when using automatic exposure settings.
     *
     * @param metering New metering mode.
     *
     * @throws IllegalArgumentException if the given <code>metering</code> is
     * not among the supported metering modes.
     */
    public void setLightMetering(String metering) {
        nSetLightMetering(_nativeHandle, metering);
    }

    /**
     * Returns the current light metering mode of the camera device.
     *
     * @return light metering mode of the camera device.
     */
    public String getLightMetering() {
        return nGetLightMetering(_nativeHandle);
    }



    protected native int nGetSupportedFStopsNumber(int hNative);
    protected native int nGetSupportedFStopByIndex(int hNative, int index);
    protected native int nGetFStop(int hNative);
    protected native void nSetFStop(int hNative, int aperture);
    protected native int nGetMinExposureTime(int hNative);
    protected native int nGetMaxExposureTime(int hNative);
    protected native int nGetExposureTime(int hNative);
    protected native int nSetExposureTime(int hNative, int time);
    protected native int nGetSupportedISOsNumber(int hNative);
    protected native int nGetSupportedISOByIndex(int hNative, int index);
    protected native int nGetISO(int hNative);
    protected native void nSetISO(int hNative, int iso);
    protected native int nGetSupportedExpCompsNumber(int hNative);
    protected native int nGetSupportedExpCompByIndex(int hNative, int index);
    protected native int nGetExposureCompensation(int hNative);
    protected native void nSetExposureCompensation(int hNative, int ec);
    protected native int nGetExposureValue(int hNative);
    protected native int nGetSupportedLightMtrsNumber(int hNative);
    protected native String nGetSupportedLightMtrByIndex(int hNative,
        int index);
    protected native void nSetLightMetering(int hNative, String metering);
    protected native String nGetLightMetering(int hNative);
    protected static native boolean nIsSupported(int hNative);

}
