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

class DirectReverbControl extends DirectEffectControl implements ReverbControl
{
    private native int nGetEctlVtbl();
    protected int GetEctlVtbl()
    {
        return nGetEctlVtbl();
    }

    private native int nGetReverbLevel();

    public int getReverbLevel()
    {
        return nGetReverbLevel();
    }
    

    private native int nGetReverbTime()
        throws javax.microedition.media.MediaException;

    public int getReverbTime()
        throws javax.microedition.media.MediaException
    {
        return nGetReverbTime();
    }
    

    private native int nSetReverbLevel(int level);

    public int setReverbLevel(int level)
        throws IllegalArgumentException
    {
        if( level > 0  )
        {
            throw new IllegalArgumentException( 
                "Cannot set positive reverberation level,"
                + " the units are millibells" );
        }
        level = nSetReverbLevel( level );
        return level;
    }


    private native void nSetReverbTime(int time) throws MediaException;

    public void setReverbTime(int time)
        throws IllegalArgumentException, MediaException
    {
        if( time < 0 )
        {
            throw new IllegalArgumentException(
                "Cannot set negative Reverb Time" );
        }
        nSetReverbTime( time );
    }
}
