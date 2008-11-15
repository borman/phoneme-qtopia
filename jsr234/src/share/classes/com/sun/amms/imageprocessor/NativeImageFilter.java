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

public abstract class NativeImageFilter {
    static final int CONVERTER = 1;
    static final int EFFECT = 2;
    static final int TRANSFORM = 3;
    static final int OVERLAY = 4;
    
    static final String RAW_IMAGE_MIME = "image/raw;format=rgba8888";
    static native String[] nGetSupportedFormats(int filterType, 
            String inputMimeType);
};

/**
 * Wrapper for the native handle
 */
final class NativeImageFilterHandle {
    private int _filterHandle;

    private static native int nCreateFilter(int filterType,
            String inputMimeType, String outputMimeType);
    private static native void nReleaseFilter(int filterHandle);

    public NativeImageFilterHandle(int filterType) {
        _filterHandle = nCreateFilter(filterType, 
                NativeImageFilter.RAW_IMAGE_MIME, 
                NativeImageFilter.RAW_IMAGE_MIME);
        
        if (_filterHandle == 0)
            throw new RuntimeException("Requested filter type is not supported");
    }
    
    public NativeImageFilterHandle(int filterType,
            String inputMimeType, String outputMimeType) {
        _filterHandle = nCreateFilter(filterType, inputMimeType,
                outputMimeType);
        if (_filterHandle == 0)
            throw new RuntimeException("Requested filter type is not supported");
    }

    /*
     * Be correct with this method. Use synchronized on NativeImageFilterHandle, 
     * while working with raw handle
     */
    public int getRawHandle() {
        return _filterHandle;
    }
    
    protected void finalize() {
        nReleaseFilter(_filterHandle);
    }
};

