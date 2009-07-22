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

package com.sun.amms.imageprocessor;

import java.io.InputStream;

import javax.microedition.media.Control;
import javax.microedition.media.MediaException;

import javax.microedition.amms.MediaProcessorListener;

import com.sun.mmedia.DefaultConfiguration;

public class StreamImageProcessor extends BasicImageProcessor {
    
    /**
     * Sets the input of the media processor as an Image.
     * <p>
     * Setting the input as an Image allows use of raw image data in a convenient way. It also
     * allows converting Images to image files.
     * <code>image</code> is an UI Image of the implementing platform. For example, in MIDP
     * <code>image</code> is <code>javax.microedition.lcdui.Image</code> object.
     * </p>
     * <p>
     * Mutable Image is allowed as an input but the behavior is unspecified if the Image
     * is changed during processing.
     * </p>
     * @param image The <code>Image</code> object to be used as input.
     * @throws IllegalStateException if the <code>MediaProcessor</code> was not in <i>UNREALIZED</i> or <i>REALIZED</i> state.
     * @throws javax.microedition.media.MediaException if input can not be given as an image.
     *
     * @throws IllegalArgumentException if the image is not an Image object.
     *
     */
    public void setInput(Object image) 
    throws javax.microedition.media.MediaException {

    //Object is not supported for image/jpeg, so throw MediaException
    throw new MediaException("Image input not supported");
    }

}
