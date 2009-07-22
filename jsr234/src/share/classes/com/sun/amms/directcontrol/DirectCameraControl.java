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

import java.io.InterruptedIOException;
import javax.microedition.amms.control.camera.*;
import javax.microedition.media.MediaException;
import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.AMMSPermission;
import com.sun.mmedia.DirectCamera;

public class DirectCameraControl extends CameraDependentControl
implements CameraControl {
    private String [] _supportedExpModes;
    private int [] _supportedVideoRes;
    private int [] _supportedStillRes;
    
    public static DirectCameraControl createInstance(DirectCamera cam) {
        if (!nIsSupported(cam.getNativeHandle())) {
            return null;
        }
        return new DirectCameraControl(cam);
    }
    
    private DirectCameraControl(DirectCamera cam) {
        setCamera(cam);
    }

    private static void checkPermission() {
        try {
            AppPackage.getInstance().
                checkForPermission(AMMSPermission.CAMERA_SHUTTERFEEDBACK);
        } catch (InterruptedException ie) {
            throw new SecurityException(
                "Interrupted while trying to ask the user permission");
        }
    }
    
    /**
     * Returns the rotation of the camera device. There might be
     * a sensor in the device that can sense the actual rotation
     * of the camera device. Rotation refers to the direction the specific
     * camera is pointing to.
     *
     * @return Rotation of the camera device or UNKNOWN if unknown.
     * ROTATE_NONE, ROTATE_LEFT, ROTATE_RIGHT or UNKNOWN.
     */
    public int getCameraRotation() {
        if (isCameraAccessible()) {
            return nGetCameraRotation(getNativeHandle());
        } else {
            return UNKNOWN;
        }
    }


    /**
     * Toggles the native shutter sound and visual shutter feedback on and off.
     *
     * @param enable true = shutter feedback is enabled, false = shutter
     * feedback is disabled.
     *
     * @throws MediaException if setting of the shutter feedback is not
     * possible.
     * @throws SecurityException if setting of the shutter feedback is not
     * allowed.
     */
    public void enableShutterFeedback(boolean enable) throws MediaException {
        if (isCameraAccessible()) {
            checkPermission();
            nEnableShutterFeedback(getNativeHandle(), enable);
        }
    }

    /**
     * Gets the setting of the native shutter feedback.
     *
     * @return true = shutter feedback is enabled, false = shutter feedback is
     * disabled.
     */
    public boolean isShutterFeedbackEnabled() {
        if (isCameraAccessible()) {
            return nIsShutterFeedbackEnabled(getNativeHandle());
        } else {
            return false;
        }
    }

    /**
     * Returns a list of exposure modes supported by the camera device.
     * Available exposure modes might include:
     * <ul>
     * <li><code>auto</code> (full automatic exposure setting)</li>
     * <li><code>landscape</code>  (daylight landscape)</li>
     * <li><code>snow</code> (high light situation)</li>
     * <li><code>beach</code> (high light situation)</li>
     * <li><code>sunset</code></li>
     * <li><code>night</code></li>
     * <li><code>fireworks</code></li>
     * <li><code>portrait</code> (human face in the center is the target)</li>
     * <li><code>backlight</code> (target in center is essentially darker than
     * the background)</li>
     * <li><code>spotlight</code> (target in center is essentially brighter than
     * the background)</li>
     * <li><code>sports</code> (fast moving targets)</li>
     * <li><code>text</code> (for copying texts and drawings, and for bar code
     * reading)</li>
     * </ul>
     * @return Supported exposure modes.
     * @see ExposureControl
     */
    public String[] getSupportedExposureModes() {
        if (!isCameraAccessible()) {
            return null;
        }

        if (null == _supportedExpModes) {
            int n = nGetSupportedExposureModesNum(getNativeHandle());
            if (n > 0) {
                _supportedExpModes = new String[n];
                for (int i = 0; i < n; i++) {
                    _supportedExpModes[i] = nGetSupportedExposureMode(
                            getNativeHandle(), i);
                }
            }
        }
        return _supportedExpModes;
    }

    /**
     * Sets the exposure mode of the camera device.
     * A method call with null parameter will be ignored.
     *
     * <p>A more fine-grained control of the exposure might be available
     * via <code>ExposureControl</code>.
     * In that case, setting the preset exposure mode
     * here will propably cause a change in
     * the settings of the ExposureControl.</p>
     *
     * @param mode New exposure mode.
     *
     * @throws IllegalArgumentException if the <code>mode</code> is not
     * among the supported exposure modes.
     *
     * @see #getSupportedExposureModes
     * @see ExposureControl
     */
    public void setExposureMode(String mode) {
        if (null == mode) {
            //  JSR-234 explicitly specifies that this method should ignore
            //  the calls with null parameter
            return;
        }
        if (isCameraAccessible()) {
            nSetExposureMode(getNativeHandle(), mode);
        }
    }

    /**
     * Returns the current exposure mode of the camera device.
     *
     * @return Exposure mode of the camera device.
     */
    public String getExposureMode() {
        if (isCameraAccessible()) {
            return nGetExposureMode(getNativeHandle());
        } else {
            return null;
        }
    }


    /**
     * Returns supported video resolutions.
     *
     * @return Supported video resolutions as x, y pairs.
     * For example, if the camera supports 1024x768 and 640x480
     * video resolutions, [1024, 768, 640, 480] will be returned.
     */
    public int[] getSupportedVideoResolutions() {
        if (!isCameraAccessible()) {
            return null;
        }
        if (null == _supportedVideoRes) {
            int n = nGetSupportedVideoResolutionsNum(getNativeHandle());
            if (n > 0) {
                _supportedVideoRes = new int[2 * n];
                for (int i = 0; i < _supportedVideoRes.length; ) {
                    int [] res = {-1, -1};
                    nGetSupportedVideoResolution(
                            getNativeHandle(), i / 2, res);
                    _supportedVideoRes[i++] = res[0];
                    _supportedVideoRes[i++] = res[1];
                }
            }
        }
        return _supportedVideoRes;
    }

    /**
     * Returns supported still image resolutions.
     *
     * @return Supported still resolutions as x, y pairs.
     * For example, if the camera supports 1024x768 and 640x480
     * still resolutions, [1024, 768, 640, 480] will be returned.
     */
    public int[] getSupportedStillResolutions() {
        if (!isCameraAccessible()) {
            return null;
        }
        if (null == _supportedStillRes) {
            int n = nGetSupportedStillResolutionsNum(getNativeHandle());
            if (n > 0) {
                _supportedStillRes = new int[ 2*n ];
                for(int i = 0; i < _supportedStillRes.length; ) {
                    int [] res = { -1, -1};
                    nGetSupportedStillResolution(
                            getNativeHandle(), i / 2, res);
                    _supportedStillRes[ i++ ] = res[ 0 ];
                    _supportedStillRes[ i++ ] = res[ 1 ];
                }
            }
        }
        return _supportedStillRes;
    }

    /**
     * Sets the video resolution.
     *
     * @param index Index 0 refers to the first pair returned by
     * getSupportedVideoResolutions() and index 1 refers to the second
     * pair and so on.
     *
     * @throws IllegalArgumentException if the given resolution is
     * not supported.
     */
    public void setVideoResolution(int index) {
        if (isCameraAccessible()) {
            nSetVideoResolution(getNativeHandle(), index );
        }
    }

    /**
     * Sets the still image resolution.
     *
     * @param index Index 0 refers to the first pair returned by
     * getSupportedStillResolutions() and index 1 refers to the second
     * pair and so on.
     *
     * @throws IllegalArgumentException if the given resolution is
     * not supported.
     */
    public void setStillResolution(int index) {
        if (isCameraAccessible()) {
            nSetStillResolution(getNativeHandle(), index );
        }
    }

    /**
     * Gets the current video resolution.
     *
     * @return an index of the current video resolution.
     * Index 0 refers to the first pair returned by
     * getSupportedVideoResolutions() and index 1 refers to the second
     * pair and so on.
     */
    public int getVideoResolution() {
        if (isCameraAccessible()) {
            return nGetVideoResolution(getNativeHandle()) ;
        } else {
            return -1;
        }
    }

    /**
     * Gets the current still image resolution.
     *
     * @return an index of the current still resolution.
     * Index 0 refers to the first pair returned by
     * getSupportedStillResolutions() and index 1 refers to the second
     * pair and so on.
     */
    public int getStillResolution() {
        if (isCameraAccessible()) {
            return nGetStillResolution(getNativeHandle());
        } else {
            return -1;
        }
    }

    protected native int nGetCameraRotation(int hNative);
    protected native void nEnableShutterFeedback(int hNative, boolean enable);
    protected native boolean nIsShutterFeedbackEnabled(int hNative);
    protected native int nGetSupportedExposureModesNum(int hNative);
    protected native String nGetSupportedExposureMode(int hNative, int index);
    protected native void nSetExposureMode(int hNative, String mode);
    protected native String nGetExposureMode(int hNative);
    protected native int nGetSupportedVideoResolutionsNum(int hNative);
    protected native void nGetSupportedVideoResolution(int hNative, int index,
        /*OUT*/int[] resolution);
    protected native int nGetSupportedStillResolutionsNum(int hNative);
    protected native void nGetSupportedStillResolution(int hNative, int index,
        /*OUT*/int[] resolution);
    protected native void nSetVideoResolution(int hNative, int index);
    protected native void nSetStillResolution(int hNative, int index);
    protected native int nGetVideoResolution(int hNative);
    protected native int nGetStillResolution(int hNative);
    protected static native boolean nIsSupported( int hNative );
}
