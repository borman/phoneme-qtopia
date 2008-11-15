/*
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.amms.imageprocessor;

import javax.microedition.amms.control.imageeffect.ImageTransformControl;
import com.sun.amms.imageprocessor.NativeImageFilter;
        
class TransformControlProxy extends EffectControlProxy 
        implements ImageTransformControl {
    
    private int _width      = 0;
    private int _height     = 0;
    
    private NativeImageFilterHandle _filterHandle = 
            new NativeImageFilterHandle(NativeImageFilter.TRANSFORM);
    
    private static native void nSetSourceRect(int filterHandle, int x, int y, 
            int width, int height);
    private static native void nSetTargetSize(int filterHandle, 
            int width, int height, int rotation);
    
    public TransformControlProxy() {
        super.setEffectFilterHandle(_filterHandle);
    }
   
    void setSourceImageSize(int width, int height) {
        _width = width;
        _height = height;
    }
    
    public int getSourceWidth() {
        if (_width == 0)
            throw new IllegalStateException("Source image was not set");
        return _width;
    }
    
    public int getSourceHeight() {
        if (_width == 0)
            throw new IllegalStateException("Source image was not set");
        return _height;
    }
    
    public void setSourceRect(int x, int y, int width, int height) {
        if ((width == 0) || (height == 0))
            throw new IllegalArgumentException("Invalid image source size " +
                    width + '*' + height);

        /// Uncertainty between text and example. Currently support example version
        if (width < 0)
                x += width;
        if (height < 0)
                y += height;
        /// Here x,y - upper left corner of the source rectangle.
        
        synchronized (_filterHandle) {
            nSetSourceRect(_filterHandle.getRawHandle(), x, y, width, height);
        }
    }
    
    public void setTargetSize(int width, int height, int rotation) {
        if ((width < 0) || (height < 0))
            throw new IllegalArgumentException("Invalid image target size "+
                    width + '*' + height);
        
        if (rotation % 90 != 0)
            throw new IllegalArgumentException("Invalid target rotation angle "
                    + rotation);
        
        /// Count to 90 degree clockwise rotations
        int rotCount = rotation / 90;
        rotCount = rotCount & 3;

        synchronized (_filterHandle) {
            nSetTargetSize(_filterHandle.getRawHandle(), width, height, rotCount);
        }
    }
    
}
