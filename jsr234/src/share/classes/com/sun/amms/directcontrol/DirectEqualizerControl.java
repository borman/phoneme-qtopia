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

package com.sun.amms.directcontrol;
import javax.microedition.amms.control.audioeffect.*;
import javax.microedition.media.MediaException;

class DirectEqualizerControl extends DirectEffectControl implements EqualizerControl
{
    private native int nGetEctlVtbl();
    protected int GetEctlVtbl()
    {
        return nGetEctlVtbl();
    }

    private native int nGetMinBandLevel();
    public int getMinBandLevel()
    {
        return nGetMinBandLevel();
    }

    private native int nGetMaxBandLevel();
    public int getMaxBandLevel()
    {
        return nGetMaxBandLevel();
    }

    private native void nSetBandLevel(int level, int band);
    public void setBandLevel(int level, int band)
    {
        nSetBandLevel(level,band);
    }

    private native int nGetBandLevel(int band);
    public int getBandLevel(int band)
    {
        return nGetBandLevel(band);
    }

    private native int nGetNumberOfBands();
    public int getNumberOfBands()
    {
        return nGetNumberOfBands();
    }

    private native int nGetCenterFreq(int band);
    public int getCenterFreq(int band)
    {
        return nGetCenterFreq(band);
    }

    private native int nGetBand(int frequency);
    public int getBand(int frequency)
    {
        return nGetBand(frequency);
    }

    private native int nSetBass(int level);
    public int setBass(int level)
    {
        return nSetBass(level);
    }

    private native int nSetTreble(int level);
    public int setTreble(int level)
    {
        return nSetTreble(level);
    }

    private native int nGetBass();
    public int getBass()
    {
        return nGetBass();
    }

    private native int nGetTreble();
    public int getTreble()
    {
        return nGetTreble();
    }
}
