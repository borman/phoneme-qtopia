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
public interface ExposureControl extends Control {

    // JAVADOC COMMENT ELIDED
    public int[] getSupportedFStops();

    // JAVADOC COMMENT ELIDED
    public int getFStop();

    // JAVADOC COMMENT ELIDED
    public void setFStop(int aperture) throws MediaException;
  
    // JAVADOC COMMENT ELIDED
    public int getMinExposureTime();

    // JAVADOC COMMENT ELIDED
    public int getMaxExposureTime();

    // JAVADOC COMMENT ELIDED
    public int getExposureTime();

    // JAVADOC COMMENT ELIDED
    public int setExposureTime(int time) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public int[] getSupportedISOs();

    // JAVADOC COMMENT ELIDED
    public int getISO();
  
    // JAVADOC COMMENT ELIDED
    public void setISO(int iso) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public int[] getSupportedExposureCompensations();

    // JAVADOC COMMENT ELIDED
    public int getExposureCompensation();
 
    // JAVADOC COMMENT ELIDED
    public void setExposureCompensation(int ec) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public int getExposureValue();

    // JAVADOC COMMENT ELIDED
    public String[] getSupportedLightMeterings();

    // JAVADOC COMMENT ELIDED
    public void setLightMetering(String metering);

    // JAVADOC COMMENT ELIDED
    public String getLightMetering();

}
