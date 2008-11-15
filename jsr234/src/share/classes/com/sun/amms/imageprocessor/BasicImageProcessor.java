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

import java.io.InputStream;
import java.util.Vector;
import javax.microedition.amms.control.imageeffect.ImageEffectControl;

import javax.microedition.media.Control;
import javax.microedition.media.MediaException;

import javax.microedition.amms.MediaProcessorListener;
import javax.microedition.amms.control.EffectControl;

import com.sun.mmedia.Configuration;
import com.sun.mmedia.DefaultConfiguration;
import com.sun.mmedia.ImageAccess;

import com.sun.amms.imageprocessor.SimpleImageFilter;
import com.sun.amms.imageprocessor.EffectsContainer;
import com.sun.amms.imageprocessor.ImageFormatProxy;
import com.sun.amms.imageprocessor.ImageEffectProxy;
import com.sun.amms.imageprocessor.TransformControlProxy;
import com.sun.amms.imageprocessor.OverlayControlProxy;

import com.sun.amms.imageprocessor.EffectsContainer;
import com.sun.j2me.app.AppIsolate;
import com.sun.amms.BasicMediaProcessor;

/*
 * Base class for image Media Processors: RAW, JPEG, PNG.
 * Class is package private to prevent uncontrolled creation of derived
 * classes.
 * Currently BasicImageProcessor contains all functionality, and
 * two subclasses ImageObjectProcessor + StreamImageProcessor are
 * used for JSR-234 compatibility, which strictly disjoints situations
 * of creating from image and  from stream.
 * The best way for supporting new image type is to add parsing
 * functionality to ImageAccessor. But, in special cases you
 * can create derived-class and redefine
 * setInput(InputStream input, int length) with your own stream parsing
 * and setting image data setting(rgb32) setImageData(data, w, h).
 */
abstract class BasicImageProcessor extends BasicMediaProcessor {
    
    protected static native int nCreateProcessor(int isolateId, int mpID);
    protected static native boolean nAddFilter(int mpHande,
            int filterHandle);
    protected static native boolean nDestroyProcessor(int mpHande);
    
    protected static native boolean nStart(int mpHande, int[] data,
            int length, int width, int height);
    protected static native boolean nContinue(int mpHande);
    protected static native boolean nStop(int mpHande);
    protected static native boolean nAbort(int mpHande);
    protected static native boolean nReset(int mpHandle);
    protected static native byte[] nGetOutput(int mpHandle);
    
    protected int   mediaProcessorHandle;
    
    protected void finalize()
    {
        if (mediaProcessorHandle != 0)
            nDestroyProcessor(mediaProcessorHandle);
    }
    
    public BasicImageProcessor() {
        mediaProcessorHandle = nCreateProcessor(AppIsolate.getIsolateId(),
                mediaProcessorID);
        
        if (mediaProcessorHandle == 0)
            throw new RuntimeException("Cannot create native media processor");
        
        imageAccessor = Configuration.getConfiguration().getImageAccessor();
        
        Vector iprocessors = new Vector();
        Vector ctrlNames = new Vector();
        
        try {
            iprocessors.addElement(new ImageEffectProxy());
            ctrlNames.addElement("javax.microedition.amms.control.imageeffect.ImageEffectControl");
        } catch(RuntimeException e) {};
        
        try {
            iprocessors.addElement(new OverlayControlProxy());
            ctrlNames.addElement("javax.microedition.amms.control.imageeffect.OverlayControl");
        } catch(RuntimeException e) {};
        
        try {
            imageTransformCtrl = new TransformControlProxy();
            iprocessors.addElement(imageTransformCtrl);
            ctrlNames.addElement("javax.microedition.amms.control.imageeffect.ImageTransformControl");
        } catch(RuntimeException e) {};
        
        Object[] effects = new Object[iprocessors.size()];
        iprocessors.copyInto(effects);
        
        imageEffectOrderCtrl = new EffectsContainer(effects);
        
        try {
            imageFormatCtrl = new ImageFormatProxy();
            iprocessors.addElement(imageFormatCtrl);
            ctrlNames.addElement("javax.microedition.amms.control.ImageFormatControl");
        } catch (RuntimeException e) {};
        
        iprocessors.addElement(imageEffectOrderCtrl);
        ctrlNames.addElement("javax.microedition.amms.control.EffectOrderControl");
        
        Control[] controls = new Control[iprocessors.size()];
        iprocessors.copyInto(controls);
        
        String[] controlNames = new String[iprocessors.size()];
        ctrlNames.copyInto(controlNames);
        
        super.setControls(controls, controlNames);
    }
    
    /**
     * BasicMediaProcessorInternal I/F method
     *
     * Extracts input data from the InputStream and passes to Controls.
     *
     * @param stream input stream with image data for processing
     * @param length length of data to read from input stream (NOW IGNORED)
     * @returns true on successful read of the image from stream,
     *          false otherwise (signal to caller to throw exception)
     */
    protected boolean doSetInput(InputStream stream, int length)
    throws MediaException {
        Object image = imageAccessor.imageCreateFromStream(stream);
        return (image != null) ? doSetInput(image) : false;
    }
    
    /**
     * BasicMediaProcessorInternal I/F method
     *
     * Extracts input data from the Image and passes to Controls.
     * Can be redefined in derived Image MediaProcessor classes.
     *
     * @param image image for processing
     * @returns true on successful read of the image from stream,
     *          false otherwise (signal to caller to throw exception)
     */
    protected boolean doSetInput(Object image) throws MediaException {
        if (!imageAccessor.isImage(image))
            throw new IllegalArgumentException("Object is not of Image class");
        
        int w = imageAccessor.getImageWidth(image);
        int h = imageAccessor.getImageHeight(image);
        int[] data = imageAccessor.getRGBIntImageData(image);
        
        if ((w == -1) || (h == -1) || (data == null))
            return false;
        
        setImageData(data, w, h);
        if (imageTransformCtrl != null)
            imageTransformCtrl.setSourceImageSize(w, h);
        
        imageData = data;
        imageWidth = w;
        imageHeight = h;
        imageLength = w*h*PIXEL_RAW_SIZE;
        return true;
    }
    
    /*
     * BasicMediaProcessorInternal I/F method
     *
     * Reuqest process starting
     */
    protected boolean doStart() throws MediaException {
        boolean isInit = nReset(mediaProcessorHandle);
        /* create array of ImageFilters, last is always Converter (FormatCtrl)
         */

        EffectControl[] array = imageEffectOrderCtrl.getEffectOrders();
        for (int i = 0; i <= array.length; ++i) {
            boolean isEnabled = true;
            SimpleImageFilter filter;

            if (i == array.length) {
                filter = (SimpleImageFilter)imageFormatCtrl;
            } else {
                EffectControl effect = array[i];
                isEnabled = effect.isEnabled();
                filter = (SimpleImageFilter)effect;
            }

            if (isEnabled) {
                NativeImageFilterHandle[] filters = filter.getFilterHandles();
                for (int j = 0; j < filters.length; j++) 
                    synchronized(filters[j]) {
                        isInit &= nAddFilter(mediaProcessorHandle, 
                                filters[j].getRawHandle());
                    }
            }
        }

        if (!isInit)
            throw new MediaException("Illegal combination of enabled controls");
        
        return nStart(mediaProcessorHandle, imageData, imageLength,
                imageWidth, imageHeight);
    }
    
    /*
     * BasicMediaProcessorInternal I/F method
     *
     * Reuqest continue of processing
     */
    protected boolean doContinue() {
        return nContinue(mediaProcessorHandle);
    }
    
    /*
     * BasicMediaProcessorInternal I/F method
     */
    protected boolean doStop() throws MediaException {
        return nStop(mediaProcessorHandle);
    }
    
    /*
     * BasicMediaProcessorInternal I/F method
     */
    protected boolean doOutput() {
        
        // wtite output data to the stream
        try {
            byte[] resultData = nGetOutput(mediaProcessorHandle);
            outputStream.write(resultData);
            return true;
        } catch (java.io.IOException ioex) {
        };
        return false;
    }
    
    /*
     * BasicMediaProcessorInternal I/F method
     */
    protected boolean doAbort() {
        return nAbort(mediaProcessorHandle);
    }
    
    protected final void setImageData(int[] data, int width, int height) {
        imageData = data;
        imageLength = data.length;
        imageWidth = width;
        imageHeight = height;
    }
    
    /*
     * Store input ARGB image and its dimensions.
     */
    protected int[] imageData;
    protected int imageLength;
    protected int imageWidth;
    protected int imageHeight;
    
    private static int PIXEL_RAW_SIZE = 4;
    
    /*
     * Main controls of image media processor
     */
    EffectsContainer        imageEffectOrderCtrl;
    TransformControlProxy   imageTransformCtrl;
    ImageFormatProxy        imageFormatCtrl;
    
    /*
     * I/F to access Image create/get_property functionality
     * in a platform independent way
     */
    protected ImageAccess imageAccessor;
}
