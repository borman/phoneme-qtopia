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

import java.io.OutputStream;

import javax.microedition.amms.control.ImageFormatControl;
import javax.microedition.media.MediaException;

import com.sun.amms.imageprocessor.SimpleImageFilter;
import com.sun.mmedia.FormatConversionUtils;

import com.sun.amms.imageprocessor.NativeImageFilter;
import java.util.Vector;

public class ImageFormatProxy extends SimpleImageFilter
        implements ImageFormatControl {
    
    private static final int  UNKNOWN_FORMAT = -1;
    private static final String DELIMITER = ";format=";
    
    /* handle of low-level (under JavaCall) filter */
    private NativeImageFilterHandle _nativeHandle = null;
    
    /* supported output formats */
    /* guaranty that supportedOutputFormats and curSupportedVersionTypes != null */
    private String[] supportedOutputFormats;
    private String[][] supportedVersionTypes;
    private String[] curSupportedVersionTypes;
    
    private String[] curSupportedIntParams;
    private String[] curSupportedStringParams;
    
    private static native
            String[] nGetSupportedStrParams(int fHandle);
    private static native
            String[] nGetSupportedIntParams(int fHandle);
    private static native
            String[] nGetSupportedStrValues(int fHandle, String parameter);
    private static native
            int[] nGetSupportedIntValues(int fHandle, String parameter);
    private static native
            void nSetStrParameter(int fHandle, String parameter, String value);
    private static native
            void nSetIntParameter(int fHandle, String parameter, int value);
    private static native
            String nGetStrParamValue(int fHandle, String parameter);
    private static native
            int nGetIntParamValue(int fHandle, String parameter);
    
    /* current metadata override mode */
    private boolean metadataOverride;
    
    /* current output format */
    int     curFormatIndex = -1;
    private String destFormatName;
    private String destVersionType;
    
    private void convertFormats(String[] formats) {
        Vector strs = new Vector(4);
        
        Vector paramVers = new Vector(4);
        Vector paramVerslc = new Vector(4);
        
        if (formats != null) {
            int[] pos = new int[formats.length];
            for (int i = 0; i < formats.length; i++) {
                pos[i] = 0;
                if (formats[i] != null)
                    pos[i] = formats[i].indexOf(DELIMITER);
            }
            
            for (int i = 0; i < formats.length; i++) {
                if (pos[i] > 0) {
                    String subs = formats[i].substring(0, pos[i]);
                    paramVerslc.removeAllElements();
                    for (int j = i; j < formats.length; j++) {
                        if ((pos[i] == pos[j]) && (formats[j].startsWith(subs))) {
                            paramVerslc.addElement(formats[j].substring(pos[j] + DELIMITER.length()));
                            pos[j] = -pos[j];
                        }
                    }
                    String[] pv = new String[paramVerslc.size()];
                    paramVerslc.copyInto(pv);
                    
                    paramVers.addElement(pv);
                    strs.addElement(subs);
                }
            }
        }
        
        supportedVersionTypes = new String[paramVers.size()][];
        paramVers.copyInto(supportedVersionTypes);
        
        supportedOutputFormats = new String[strs.size()];
        strs.copyInto(supportedOutputFormats);
        
    }
    
    public ImageFormatProxy() {
        metadataOverride = true;
        
        String[] _internalOutputFormats = NativeImageFilter.nGetSupportedFormats(
                NativeImageFilter.CONVERTER, NativeImageFilter.RAW_IMAGE_MIME);
        /* Specification defines, that at least jpeg as output is supported */
        
        supportedOutputFormats = _internalOutputFormats;
        convertFormats(_internalOutputFormats);
        
        if (supportedOutputFormats.length == 0)
            throw new RuntimeException("Native layer is not support any format");
        
        applyFormat(0, 0);
    }
    
    private void applyFormat(int indexFormat, int indexPV)  {
        curSupportedVersionTypes = supportedVersionTypes[indexFormat];
        destFormatName = supportedOutputFormats[indexFormat];
        destVersionType = curSupportedVersionTypes[indexPV];
        curFormatIndex = indexFormat;
        
        _nativeHandle = new NativeImageFilterHandle(NativeImageFilter.CONVERTER,
                NativeImageFilter.RAW_IMAGE_MIME,
                supportedOutputFormats[indexFormat] + DELIMITER + destVersionType);
        
        String[] strParams;
        synchronized (_nativeHandle) {
            int handle = _nativeHandle.getRawHandle();
            strParams = nGetSupportedStrParams(handle);
            curSupportedIntParams = nGetSupportedIntParams(handle);
        }
        
        curSupportedStringParams = new String[strParams.length + 1];
        System.arraycopy(strParams, 0, curSupportedStringParams, 1, strParams.length);
        curSupportedStringParams[0] = PARAM_VERSION_TYPE;
    }
    
    /*
     * FormatControl I/F method
     */
    public void setFormat(String format) {
        if (format == null)
            throw new java.lang.IllegalArgumentException("Format is null");
        
        if (format.equals(destFormatName))
            return;
        
        int i = supportedOutputFormats.length - 1;
        while ((i >= 0) && (!supportedOutputFormats[i].equals(format)))
            i--;
        
        if (i < 0)
            throw new java.lang.IllegalArgumentException(
                    "Not supported image format - " + format);
        
        applyFormat(i, 0);
    }
    
    /*
     * FormatControl I/F method
     */
    public String getFormat() {
        return destFormatName;
    }
    
    /*
     * FormatControl I/F method
     */
    public String[] getSupportedFormats() {
        return FormatConversionUtils.stringArrayCopy(supportedOutputFormats);
    }
    
    /*
     * FormatControl I/F method
     */
    public String[] getSupportedStrParameters() {
        if (curSupportedStringParams == null)
            return new String[0];
        else
            return FormatConversionUtils.stringArrayCopy(curSupportedStringParams);
    }
    
    /*
     * FormatControl I/F method
     */
    public String[] getSupportedIntParameters() {
        if (curSupportedIntParams == null)
            return new String[0];
        else
            return FormatConversionUtils.stringArrayCopy(curSupportedIntParams);
    }
    
    /*
     * FormatControl I/F method
     */
    public String[] getSupportedStrParameterValues(String parameter) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        
        if (curSupportedStringParams == null)
            throw new java.lang.IllegalArgumentException("No str parameters supported");
        
        if (parameter.equals(PARAM_VERSION_TYPE)) {
            return FormatConversionUtils.stringArrayCopy(curSupportedVersionTypes);
        } else {
            synchronized (_nativeHandle) {
                return nGetSupportedStrValues(_nativeHandle.getRawHandle(), parameter);
            }
        }
    }
    
    /*
     * FormatControl I/F method
     */
    public int[] getSupportedIntParameterRange(String parameter) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        
        if (curSupportedIntParams == null)
            throw new java.lang.IllegalArgumentException("No int parameters supported");

        synchronized (_nativeHandle) {
            return nGetSupportedIntValues(_nativeHandle.getRawHandle(), parameter);
        }
    }
    
    /*
     * FormatControl I/F method
     */
    public void setParameter(String parameter, String value) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        if (value == null)
            throw new java.lang.IllegalArgumentException("Value is null");
        
        if (curSupportedStringParams == null)
            throw new java.lang.IllegalArgumentException();
        
        if (!parameter.equals(PARAM_VERSION_TYPE)) {
            synchronized (_nativeHandle) {
                nSetStrParameter(_nativeHandle.getRawHandle(), parameter, value);
            }
        } else {
            if (value.equals(destFormatName))
                return;
            
            for (int i = 0; i < curSupportedVersionTypes.length; i++) {
                if (value.equals(curSupportedVersionTypes[i])) {
                    applyFormat(curFormatIndex, i);
                    return;
                }
            }
            throw new java.lang.IllegalArgumentException(value +
                    " is not supported for " + parameter);
        }
    }
    
    /*
     * FormatControl I/F method
     */
    public int setParameter(String parameter, int value) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        
        if (curSupportedIntParams == null)
            throw new java.lang.IllegalArgumentException();
        
        synchronized (_nativeHandle) {
            nSetIntParameter(_nativeHandle.getRawHandle(), parameter, value);
        }
        return value;
    }
    
    /*
     * FormatControl I/F method
     */
    public String getStrParameterValue(String parameter) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        
        if (curSupportedStringParams == null)
            throw new java.lang.IllegalArgumentException(parameter +
                    " is not supported");
        
        if (parameter.equals(PARAM_VERSION_TYPE)) {
            return destVersionType;
        } else {
            synchronized (_nativeHandle) {
                return nGetStrParamValue(_nativeHandle.getRawHandle(), parameter);
            }
        }
    }
    
    /*
     * FormatControl I/F method
     */
    public int getEstimatedBitRate() throws MediaException {
        throw new MediaException("Bitrate size estimation is not supported");
    }
    
    /*
     * ImageFormatControl I/F method
     */
    public int getIntParameterValue(String parameter) {
        if (parameter == null)
            throw new java.lang.IllegalArgumentException("Parameter is null");
        
        if (curSupportedIntParams == null)
            throw new java.lang.IllegalArgumentException("No int parameters supported");

        synchronized (_nativeHandle) {
            return nGetIntParamValue(_nativeHandle.getRawHandle(), parameter);
        }
    }
    
    /*
     * FormatControl I/F method
     */
    public void setMetadata(String key, String value) throws MediaException {
        throw new javax.microedition.media.MediaException("Metadata is not supported");
    }
    
    /*
     * FormatControl I/F method
     */
    public String[] getSupportedMetadataKeys() {
        return new String[0];
    }
    
    /*
     * FormatControl I/F method
     */
    public int getMetadataSupportMode() {
        return METADATA_NOT_SUPPORTED;
    }
    
    /*
     * FormatControl I/F method
     */
    public void setMetadataOverride(boolean override) {
        metadataOverride = override;
    }
    
    /*
     * FormatControl I/F method
     */
    public boolean getMetadataOverride() {
        return metadataOverride;
    }
    
    /*
     * ImageFormatControl I/F method
     */
    public int getEstimatedImageSize() {
        return 0;
    }
    
    NativeImageFilterHandle[] getFilterHandles() {
        NativeImageFilterHandle filterHandle = _nativeHandle;
        if (filterHandle == null)
            return new NativeImageFilterHandle[0];
        else {
            return new NativeImageFilterHandle[]{ filterHandle };
        }
    }
}
