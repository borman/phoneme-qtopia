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

import com.sun.mmedia.Configuration;
import com.sun.mmedia.ImageAccess;
import com.sun.amms.imageprocessor.NativeImageFilter;
import javax.microedition.amms.control.imageeffect.OverlayControl;
import java.util.Vector;

class OverlayImageData {
    Object                  image;
    NativeImageFilterHandle nHandle;
    
    public OverlayImageData(Object image, NativeImageFilterHandle nHandle) {
        this.image = image;
        this.nHandle = nHandle;
    }
};

public class OverlayControlProxy  extends EffectControlProxy
        implements OverlayControl {
    
    private static final int DEFAULT_VECTOR_SIZE  = 4;
    private static final int NONE_TRANSPANENT_COLOR = 1 << 28;
    
    private ImageAccess imageAccessor;
    private Vector      overlays;
    
    public OverlayControlProxy() {
        overlays = new Vector(DEFAULT_VECTOR_SIZE);
        imageAccessor = Configuration.getConfiguration().getImageAccessor();
    }
    
    private static native void nSetImage(int filterHandle, int[] rgbData,
            int width, int height, int x, int y, int transColor);
    
    private int utilInsertImage(Object image, int x, int y, int order,
            int transColor) throws IllegalArgumentException {
        if (image == null)
            throw new IllegalArgumentException("Null image as argument");
        
        if (order < 0)
            throw new IllegalArgumentException("Invalid order " + order);
        
        if (!imageAccessor.isImage(image))
            throw new IllegalArgumentException("Object is not of Image class");

        /// Get RGB data
        int[] rgbData = imageAccessor.getRGBIntImageData(image);
        
        NativeImageFilterHandle 
                handle = new NativeImageFilterHandle(NativeImageFilter.OVERLAY);
        
        int width = imageAccessor.getImageWidth(image);
        int height = imageAccessor.getImageHeight(image);
        synchronized (handle) {
            nSetImage(handle.getRawHandle(), rgbData, width, height, x, y, transColor);
        }
        
        OverlayImageData oiData = new OverlayImageData(image, handle);
        
        int index;
        synchronized(overlays) {
            if (order >= overlays.size()) {
                overlays.addElement(oiData);
                index = overlays.size() - 1;
            } else {
                overlays.insertElementAt(oiData, order);
                index = order;
            }
        }
        
        return index;
    }
    
    public int insertImage(Object image, int x, int y, int order) throws IllegalArgumentException {
        return utilInsertImage(image, x, y, order, NONE_TRANSPANENT_COLOR);
    }
    
    public int insertImage(Object image, int x, int y, int order,
            int transparentColor) throws IllegalArgumentException {
        /// Ignore XX in 0xXXRRGGBB
        return utilInsertImage(image, x, y, order, transparentColor & 0xFFFFFF );
    }
    
    public void removeImage(Object image) {
        if (image == null)
            throw new IllegalArgumentException("Null image as argument");
        
        boolean isFound = false;
        synchronized(overlays) {
            int i = 0;
            /// Remove all entrances of image
            while (i < overlays.size()) {
                OverlayImageData oiData = (OverlayImageData)overlays.elementAt(i);
                if (oiData.image == image) {
                    overlays.removeElementAt(i);
                    isFound = true;
                } else
                    i++;
            }
        }
        
        if (!isFound)
            throw new IllegalArgumentException("Image don't belong to overlay");
    }
    
    public Object getImage(int order) {
        if (order < 0)
            throw new IllegalArgumentException("Invalid order " + order);
        synchronized (overlays) {
            if (order >= overlays.size())
                return null;
            
            OverlayImageData oiData = (OverlayImageData)overlays.elementAt(order);
            return oiData.image;
        }
    }
    
    public  int numberOfImages() {
        return overlays.size();
    }
    
    public void clear() {
        overlays.removeAllElements();
    }
    
    NativeImageFilterHandle[] getFilterHandles() {
        NativeImageFilterHandle[] result;
        synchronized (overlays) {
            result = new NativeImageFilterHandle[overlays.size()];
            for (int i = 0; i < overlays.size(); i++) {
                OverlayImageData oiData = (OverlayImageData)overlays.elementAt(i);
                result[i] = oiData.nHandle;
            }
        }
        return result;
    }
}
