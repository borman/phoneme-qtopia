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

package javax.microedition.amms.control.camera;

import javax.microedition.media.MediaException;
import javax.microedition.media.Control;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface CameraControl extends Control {

    // JAVADOC COMMENT ELIDED
    public final static int ROTATE_LEFT = 2;
  
    // JAVADOC COMMENT ELIDED
    public final static int ROTATE_RIGHT = 3;
   
    // JAVADOC COMMENT ELIDED
    public final static int ROTATE_NONE = 1;
   
    // JAVADOC COMMENT ELIDED
    public final static int UNKNOWN = -1004;
  

    // JAVADOC COMMENT ELIDED
    public int getCameraRotation();

    // JAVADOC COMMENT ELIDED
    public void enableShutterFeedback(boolean enable) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public boolean isShutterFeedbackEnabled();

    // JAVADOC COMMENT ELIDED
    public String[] getSupportedExposureModes();

    // JAVADOC COMMENT ELIDED
    public void setExposureMode(String mode);

    // JAVADOC COMMENT ELIDED
    public String getExposureMode();

    // JAVADOC COMMENT ELIDED
    public int[] getSupportedVideoResolutions();
 
    // JAVADOC COMMENT ELIDED
    public int[] getSupportedStillResolutions();

    // JAVADOC COMMENT ELIDED
    public void setVideoResolution(int index);

    // JAVADOC COMMENT ELIDED
    public void setStillResolution(int index);

    // JAVADOC COMMENT ELIDED
    public int getVideoResolution();
     
    // JAVADOC COMMENT ELIDED
    public int getStillResolution();

}
