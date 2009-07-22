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
import javax.microedition.amms.control.camera.*;
import javax.microedition.media.MediaException;

public class DirectZoomControl implements ZoomControl {

    private int _nativeHandle;
    
    public static DirectZoomControl createInstance(int hNative) {
        if (!nIsSupported(hNative)) {
            return null;
        }
        return new DirectZoomControl(hNative);
    }
    
    private DirectZoomControl(int hNative) {
        _nativeHandle = hNative;
    }
    
    /**
     * Sets the optical zoom of the camera device.
     *
     * The default optical zoom value is not necessarily 100 which is the
     * minimum magnification. <code>getOpticalZoom</code> can be used to ask the
     * default value before setting the new value.
     * <p>
     * The camera may have some set of zoom levels which
     * the optical zoom can be set to. <code>getOpticalZoomLevels</code>
     * can be used to query the number of zoom levels, and setting the zoom
     * to <code>PREVIOUS</code> or <code>NEXT</code> sets the zoom to
     * the previous or next zoom level, respectively.
     * </p>
     * <p>
     * Setting the optical zoom is a mechanical operation which it takes
     * some time to complete. The method only returns
     * when the setting of the zoom has been completed.
     * </p>
     * <p>
     * Implementation of the <code>setOpticalZoom</code>
     * method is synchronized to let only one physical setting of the object
     * lens be effective at a time.
     * </p>
     *
     *<p>
     * Please note that changing the optical zooming might change the minimum
     * possible focusing distance (the return value of
     * {@link javax.microedition.amms.control.camera.FocusControl#getMinFocus
     * FocusControl.getMinFocus})
     * and therefore alter the actual current focusing distance
     * ({@link javax.microedition.amms.control.camera.FocusControl#getFocus
     * FocusControl.getFocus})
     * if the current focus setting is not possible with the new zoom setting.
     *</p>
     *
     * @param level New optical zoom value >= 100 or
     * <code>NEXT</code> or <code>PREVIOUS</code> to set the zoom to the next
     * or previous level, respectively.
     * @return Zoom value that was actually set by the implementation.
     * @exception IllegalArgumentException Thrown if the <code>level</code>
     * is less than 100, or if
     * the <code>level</code> is greater than the maximum optical zoom value,
     * and if the <code>level</code> is not <code>NEXT</code> or
     * <code>PREVIOUS</code>.
     */
    public int setOpticalZoom(int level) {
        return nSetOpticalZoom(_nativeHandle, level);
    }

    /**
     * Returns the optical zoom value of the camera device.
     * @return Optical zoom value or UNKNOWN if the setting is unknown.
     */
    public int getOpticalZoom() {
        return nGetOpticalZoom(_nativeHandle);
    }


    /**
     * Returns the maximum optical zoom value of the camera device.
     * If the maximum optical zoom is 100 the camera device does not support
     * optical zoom.
     * @return Maximum optical zoom value.
     */
    public int getMaxOpticalZoom() {
        return nGetMaxOpticalZoom(_nativeHandle);
    }

    /**
     * Tells how many levels the optical zoom has.
     * @return Number of optical zoom levels.
     */
    public int getOpticalZoomLevels() {
        return nGetOpticalZoomLevels(_nativeHandle);
    }


    /**
     * Returns the minimum (35 mm camera equivalent) focal length of the camera
     * device.
     * <p>
     * The current focal length can be calculated when the minimum focal length
     * and the current
     * optical zoom value are known by the following formula.
     * <p>
     * focal length = magnification * minimum focal length
     * <p>
     * Since the magnification is expressed in a form where 100 = 1x
     * magnification, then in the application the calculation is:
     * <p>
     * <code>int focalLength = getOpticalZoom() * getMinFocalLength() / 100;
     * </code>
     *
     * @return minimum focal length in micrometers or <code>UNKNOWN</code>
     * if minimum focal length is not known.
     */
    public int getMinFocalLength() {
        return nGetMinFocalLength(_nativeHandle);
    }

   /**
    * <p>
    * Sets the digital zoom of the camera device.
    * The default digital zoom value is 100.
    * Setting the digital zoom is effective in images that are taken
    * by <code>getSnapshot</code>.
    * </p>
    * <p>
    * The zoom value of 100 corresponds to no zoom,
    * the value of 200 corresponds to a double zoom, etc.
    * If the exact given value is not supported
    * it will be rounded to the closest supported value.
    * </p>
    *
    * <p>
    * The digital zoom probably has a set of zoom levels which
    * the zoom can be set to. <code>getDigitalZoomLevels</code>
    * can be used to query the number of zoom levels, and setting the zoom
    * to <code>PREVIOUS</code> or <code>NEXT</code> sets the zoom to
    * the previous or next zoom level, respectively.
    * </p>
    *
    * @param level New digital zoom value, or NEXT or PREVIOUS
    * to set the digital zoom to the next or previous supported level,
    * respectively. That is, NEXT zooms in and PREVIOUS zooms out.
    *
    * @return Digital zoom that was actually set.
    *
    * @exception IllegalArgumentException Thrown if the <code>level</code>
    * is less than 100 and not NEXT or PREVIOUS, or if the <code>level</code> is
    * greater than the maximum digital zoom value.
    */
    public int setDigitalZoom(int level) {
        return nSetDigitalZoom(_nativeHandle, level);
    }

    /**
     * Returns the current digital zoom value.
     *
     * @return Current digital zoom value.
     */
    public int getDigitalZoom() {
        return nGetDigitalZoom(_nativeHandle);
    }


    /**
     * Returns the maximum digital zoom value. Maximum value of 100
     * tells that setting of the digital zoom
     * is not supported.
     *
     * @return Maximum zoom value to be used with <code>setDigitalZoom</code>.
     */
    public int getMaxDigitalZoom() {
        return nGetMaxDigitalZoom(_nativeHandle);
    }


    /**
     * Tells how many levels the digital zoom has.
     * @return Number of digital zoom levels.
     */
    public int getDigitalZoomLevels() {
        return nGetDigitalZoomLevels(_nativeHandle);
    }

    protected native int nSetOpticalZoom(int hNative, int level);
    protected native int nGetOpticalZoom(int hNative);
    protected native int nGetMaxOpticalZoom(int hNative);
    protected native int nGetOpticalZoomLevels(int hNative);
    protected native int nGetMinFocalLength(int hNative);
    protected native int nSetDigitalZoom(int hNative, int level);
    protected native int nGetDigitalZoom(int hNative);
    protected native int nGetMaxDigitalZoom(int hNative);
    protected native int nGetDigitalZoomLevels(int hNative);
    protected static native boolean nIsSupported(int hNative);
}
