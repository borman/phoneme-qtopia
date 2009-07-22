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
import com.sun.mmedia.DirectCamera;

public class DirectFocusControl extends CameraDependentControl
implements FocusControl {

    public static DirectFocusControl createInstance(DirectCamera cam) {
        if (!nIsSupported(cam.getNativeHandle())) {
            return null;
        }
        return new DirectFocusControl(cam);
    }
    
    private DirectFocusControl(DirectCamera cam) {
        setCamera(cam);
    }
    
    /**
     * Sets the focus distance.
     *
     * @param distance in millimeters or AUTO for autofocus
     * or AUTO_LOCK for locking the autofocus or Interger.MAX_VALUE
     * for focus to infinity or NEXT for next
     * supported distance (further from the camera) or PREVIOUS
     * for previous supported distance (closer to the camera).
     * Setting a value other than AUTO, AUTO_LOCK or Integer.MAX_VALUE might
     * be rounded to the closest distance supported.
     * If the current focus setting is other than AUTO,
     * <code>setFocus(AUTO_LOCK)</code> will not affect anything.
     *
     * @throws MediaException if the given focus setting is not supported.
     *
     * @return the distance that was set or AUTO for autofocus
     * or AUTO_LOCK for locked autofocus
     * or Integer.MAX_VALUE for infinity.
     *
     * @see #isManualFocusSupported
     * @see #isAutoFocusSupported
     */
    public int setFocus(int distance) throws MediaException {
        if (isCameraAccessible()) {
            return nSetFocus(getNativeHandle(), distance);
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Returns the focus setting of the camera device.
     *
     * @return distance in millimeters or AUTO for autofocus
     * or AUTO_LOCK for locked autofocus
     * or Integer.MAX_VALUE for focus to infinity
     * or UNKNOWN if the focus setting is unknown.
     */
    public int getFocus() {
        if (isCameraAccessible()) {
            return nGetFocus(getNativeHandle());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Returns the minimum focus distance supported (either manual or AUTO).
     *
     * @return the minimum focus distance in millimeters supported or UNKNOWN.
     */
    public int getMinFocus() {
        if (isCameraAccessible()) {
            return nGetMinFocus(getNativeHandle());
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Tells how many steps the focus has. That is how many different
     * focusing distances we are able to set. AUTO is not counted as a step
     * and therefore, if manual focus is not supported this method
     * will return 0.
     *
     * @return The amount of steps.
     */
    public int getFocusSteps() {
        if (isCameraAccessible()) {
            return nGetFocusSteps(getNativeHandle());
        } else {
            return 0;
        }
    }

    /**
     * Returns true if the manual focus is supported by being able
     * to set the distance (or NEXT or PREVIOUS or Integer.MAX_VALUE)
     * with setFocus method.
     *
     * @return true if the manual focus is supported, false otherwise.
     */
    public boolean isManualFocusSupported() {
        if (isCameraAccessible()) {
            return nIsManualFocusSupported(getNativeHandle());
        } else {
            return false;
        }
    }


    /**
     * Returns true if the automatic focus is supported by being able
     * to set AUTO as a parameter for the <code>setFocus</code> method.
     *
     * @return true if the automatic focus is supported, false otherwise.
     */
    public boolean isAutoFocusSupported() {
        if (isCameraAccessible()) {
            return nIsAutoFocusSupported(getNativeHandle());
        } else {
            return false;
        }
    }


    /**
     * Returns true if the macro focus mode is supported.
     *
     * @return true if the macro focus is supported, false otherwise.
     */
    public boolean isMacroSupported() {
        if (isCameraAccessible()) {
            return nIsMacroSupported(getNativeHandle());
        } else {
            return false;
        }
    }

    /**
     * <p>Toggles the macro focus mode. With this mode on, you can focus
     * closer to the camera compared to a normal focusing mode.
     * On the contrary, the range of available optical zoom settings
     * might be more limited in the macro mode.</p>
     *
     * <p>Please note, that this method might affect all optical
     * zoom settings in the <code>ZoomControl</code>.
     * This method might also change all the settings
     * in this <code>FocusControl</code>.</p>
     *
     * @param enable true = macro mode on, false = macro mode off.
     *
     * @throws MediaException if the given mode is not supported.
     */
    public void setMacro(boolean enable) throws MediaException {
        if (isCameraAccessible()) {
            nSetMacro(getNativeHandle(), enable);
        }
    }

    /**
     * Gets the current macro focus mode.
     *
     * @return true = macro mode on, false = macro mode off.
     */
    public boolean getMacro() {
        if (isCameraAccessible()) {
            return nGetMacro(getNativeHandle());
        } else {
            return false;
        }
    }

    protected native int nSetFocus(int hNative, int distance);
    protected native int nGetFocus(int hNative);
    protected native int nGetMinFocus(int hNative);
    protected native int nGetFocusSteps(int hNative);
    protected native boolean nIsManualFocusSupported(int hNative);
    protected native boolean nIsAutoFocusSupported(int hNative);
    protected native boolean nIsMacroSupported(int hNative);
    protected native void nSetMacro(int hNative, boolean enable);
    protected native boolean nGetMacro(int hNative);
    protected static native boolean nIsSupported(int hNative);

}
