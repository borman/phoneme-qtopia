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

package javax.microedition.amms.control;

import javax.microedition.media.*;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface FormatControl extends Control  {

    // JAVADOC COMMENT ELIDED
    public final static int METADATA_NOT_SUPPORTED = 0;

    // JAVADOC COMMENT ELIDED
    public final static int METADATA_SUPPORTED_FIXED_KEYS = 1;

    // JAVADOC COMMENT ELIDED
    public final static int METADATA_SUPPORTED_FREE_KEYS = 2;

    // JAVADOC COMMENT ELIDED
    public final static String PARAM_BITRATE = "bitrate";
    
    // JAVADOC COMMENT ELIDED
    public final static String PARAM_BITRATE_TYPE = "bitrate type";

    // JAVADOC COMMENT ELIDED
    public final static String PARAM_SAMPLERATE = "sample rate";
    
    // JAVADOC COMMENT ELIDED
    public final static String PARAM_FRAMERATE = "frame rate";
    
    // JAVADOC COMMENT ELIDED
    public final static String PARAM_QUALITY = "quality";

    // JAVADOC COMMENT ELIDED
    public final static String PARAM_VERSION_TYPE = "version type";


    // JAVADOC COMMENT ELIDED
    String[] getSupportedFormats();
    
    // JAVADOC COMMENT ELIDED
    String[] getSupportedStrParameters();
    
    // JAVADOC COMMENT ELIDED
    String[] getSupportedIntParameters();
    
    // JAVADOC COMMENT ELIDED
    String[] getSupportedStrParameterValues(String parameter);
    
    // JAVADOC COMMENT ELIDED
    int[] getSupportedIntParameterRange(String parameter);
    
    // JAVADOC COMMENT ELIDED
    void setFormat(String format);
    
    // JAVADOC COMMENT ELIDED
    String getFormat();

    // JAVADOC COMMENT ELIDED
    int setParameter(String parameter, int value);
    
    // JAVADOC COMMENT ELIDED
    void setParameter(String parameter, String value);
    
    // JAVADOC COMMENT ELIDED
    String getStrParameterValue(String parameter);
    
    // JAVADOC COMMENT ELIDED
    int getIntParameterValue(String parameter);
    
    // JAVADOC COMMENT ELIDED
    int getEstimatedBitRate() throws MediaException;
    
    // JAVADOC COMMENT ELIDED
    void setMetadata(String key, String value) throws MediaException;

    // JAVADOC COMMENT ELIDED
    String[] getSupportedMetadataKeys();

    // JAVADOC COMMENT ELIDED
    int getMetadataSupportMode();
  
    // JAVADOC COMMENT ELIDED
    void setMetadataOverride(boolean override);

    // JAVADOC COMMENT ELIDED
    boolean getMetadataOverride();
    
}
