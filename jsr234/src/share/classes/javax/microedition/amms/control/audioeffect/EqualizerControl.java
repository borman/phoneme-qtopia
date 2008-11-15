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

package javax.microedition.amms.control.audioeffect;

import javax.microedition.amms.control.*;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public interface EqualizerControl extends EffectControl {
    
    // JAVADOC COMMENT ELIDED
    public final int UNDEFINED = -1004;


    // JAVADOC COMMENT ELIDED
    int getMinBandLevel();

    // JAVADOC COMMENT ELIDED
    int getMaxBandLevel();
    
    // JAVADOC COMMENT ELIDED
    void setBandLevel(int level, int band) throws IllegalArgumentException;
    
    // JAVADOC COMMENT ELIDED
    int getBandLevel(int band) throws IllegalArgumentException;

    // JAVADOC COMMENT ELIDED
    int getNumberOfBands();

    // JAVADOC COMMENT ELIDED
    int getCenterFreq(int band) throws IllegalArgumentException;

    // JAVADOC COMMENT ELIDED
    int getBand(int frequency);

    // JAVADOC COMMENT ELIDED
    int setBass(int level) throws IllegalArgumentException;

    // JAVADOC COMMENT ELIDED
    int setTreble(int level) throws IllegalArgumentException;

    // JAVADOC COMMENT ELIDED
    int getBass();

    // JAVADOC COMMENT ELIDED
    int getTreble();

}
