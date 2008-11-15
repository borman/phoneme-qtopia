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

import javax.microedition.media.Control;
import javax.microedition.media.MediaException;

import javax.microedition.amms.MediaProcessorListener;

import com.sun.mmedia.DefaultConfiguration;

public class ObjectImageProcessor extends BasicImageProcessor {
    // --- override MediaProcessor.setInput methods
    
    /**
     * Sets the input of the media processor.
     * @param input The <code>InputStream</code> to be used as input.
     * @param length The estimated length of the processed media in bytes. Since the input
     * is given as an <code>InputStream</code>, the implementation cannot find out
     * what is the length of the media until it has been processed. The estimated
     * length is used only when the <code>progress</code> method is used to query the
     * progress of the processing. If the length is not known, UNKNOWN should be passed
     * as a length.
     * @throws IllegalStateException if the <code>MediaProcessor</code> was not in UNREALIZED or REALIZED state.
     * @throws javax.microedition.media.MediaException if input can not be given as a stream.
     * @throws IllegalArgumentException if input is null.
     * @throws IllegalArgumentException if length < 1 and length != UNKNOWN.
     *
     */
    public void setInput(InputStream input, int length)
    throws javax.microedition.media.MediaException {
        
        //InputStream is not supported for image/raw, so throw MediaException
        throw new MediaException("Stream input not supported");
    }
}
