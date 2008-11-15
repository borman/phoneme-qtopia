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

package javax.microedition.amms.control.tuner;

import javax.microedition.media.MediaException;
import javax.microedition.media.Control;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface TunerControl extends Control {    

    // JAVADOC COMMENT ELIDED
    public static final int MONO = 1;

    // JAVADOC COMMENT ELIDED
    public static final int STEREO = 2;

    // JAVADOC COMMENT ELIDED
    public static final int AUTO = 3;

    // JAVADOC COMMENT ELIDED
    public static final String MODULATION_FM = "fm";

    // JAVADOC COMMENT ELIDED
    public static final String MODULATION_AM = "am";


    // JAVADOC COMMENT ELIDED
    public int getMinFreq(String modulation);

    // JAVADOC COMMENT ELIDED
    public int getMaxFreq(String modulation);

    // JAVADOC COMMENT ELIDED
    public int setFrequency(int freq, String modulation);
    
    // JAVADOC COMMENT ELIDED
    public int getFrequency();

    // JAVADOC COMMENT ELIDED
    public int seek( int startFreq, String modulation, boolean upwards ) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public boolean getSquelch();

    // JAVADOC COMMENT ELIDED
    public void setSquelch( boolean squelch ) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public String getModulation();

    // JAVADOC COMMENT ELIDED
    public int getSignalStrength() throws MediaException;

    // JAVADOC COMMENT ELIDED
    public int getStereoMode();

    // JAVADOC COMMENT ELIDED
    public void setStereoMode( int mode );

    // JAVADOC COMMENT ELIDED
    public int getNumberOfPresets();

    // JAVADOC COMMENT ELIDED
    public void usePreset(int preset);

    // JAVADOC COMMENT ELIDED
    public void setPreset( int preset );

    // JAVADOC COMMENT ELIDED
    public void setPreset(int preset, int freq, String mod, int stereoMode);

    // JAVADOC COMMENT ELIDED
    public int getPresetFrequency(int preset);

    // JAVADOC COMMENT ELIDED
    public String getPresetModulation(int preset);

    // JAVADOC COMMENT ELIDED
    public int getPresetStereoMode(int preset) throws MediaException;

    // JAVADOC COMMENT ELIDED
    public String getPresetName( int preset );

    // JAVADOC COMMENT ELIDED
    public void setPresetName( int preset, String name );

}

