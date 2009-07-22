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

public class DirectFlashControl implements FlashControl {

    private int _nativeHandle;
    private int [] _supported_modes;
    
    public static DirectFlashControl createInstance(int hNative) {
        if (!nIsSupported(hNative)) {
            return null;
        }
        return new DirectFlashControl(hNative);
    }
    
    private DirectFlashControl(int hNative) {
        _nativeHandle = hNative;
    }
    
    /**
     * Returns a list of flash modes supported by the camera device.
     * Available flash modes are:
     * <ul>
     * <li><code>OFF</code></li>
     * <li><code>AUTO</code></li>
     * <li><code>AUTO_WITH_REDEYEREDUCE</code></li>
     * <li><code>FORCE</code></li>
     * <li><code>FORCE_WITH_REDEYEREDUCE</code></li>
     * <li><code>FILLIN</code></li>
     * </ul>
     *
     * @return Supported flash modes.
     */
    public int[] getSupportedModes() {
        if (_supported_modes == null) {
            int n = nGetSupportedModesNumber(_nativeHandle);
            if (n > 0) {
                _supported_modes = new int [n];
                for (int i = 0; i < n; i++) {
                    _supported_modes[i] = nGetSupportedModeByIndex(
                            _nativeHandle, i);
                }
            }
        }
        return _supported_modes;
    }

    /**
     * Sets the flash mode of a camera device.
     *
     * @param mode New flash mode.
     *
     * @throws IllegalArgumentException if the given <code>mode</code> is not
     * supported.
     *
     * @see #getSupportedModes
     */
    public void setMode(int mode) {
        nSetMode(_nativeHandle, mode);
    }

    /**
     * Returns the flash mode of the camera device.
     * @return Flash mode of the camera device.
     * @see #getSupportedModes
     */
    public int getMode() {
        return nGetMode(_nativeHandle);
    }

    /**
     * Tells if the flash device is ready.
     *
     * <p>
     * Setting up the flash device usually takes some time. This method tells if
     * the flash device is ready for use.
     * </p>
     * @return <code>true</code> if flash is ready, <code>false</code>
     * otherwise.
     */
    public boolean isFlashReady() {
        return false;
    }

    protected native int nGetSupportedModesNumber(int hNative);
    protected native int nGetSupportedModeByIndex(int hNative, int n);
    protected native void nSetMode(int hNative, int mode);
    protected native int nGetMode(int hNative);
    protected native boolean nIsFlashReady(int hNative);
    protected static native boolean nIsSupported(int hNative);
}
