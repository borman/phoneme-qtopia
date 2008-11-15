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
import javax.microedition.amms.control.audio3d.*;

class DirectOrientationControl extends DirectAMMSControl 
        implements OrientationControl
{
    private native void nGetOrientationVectors( int[] vectors );
    public int[] getOrientationVectors()
    {
        int [] vectors = new int [6];
        nGetOrientationVectors( vectors );
        return vectors;
    }

    private native void nSetOrientationVec( int[] frontVector, 
            int[] aboveVector)
      throws IllegalArgumentException;
    public void setOrientation( int[] frontVector, int[] aboveVector)
      throws IllegalArgumentException
    {
        if( frontVector == null || aboveVector == null )
        {
            throw new IllegalArgumentException( "A null vector was passed to" +
                    "OrientationControl.setOrientation()" );
        }
        if( frontVector.length != 3 || aboveVector.length !=3 )
        {
            throw new IllegalArgumentException( 
"A vector of wrong dimension was passed to " +
                    "OrientationControl.setOrientationVectors" );
        }
        
        if( ( frontVector[0] == 0 && 
              frontVector[1] == 0 && 
              frontVector[2] == 0 ) || 
            ( aboveVector[0] == 0 && 
              aboveVector[1] == 0 && 
              aboveVector[2] == 0 ) )
        {
            throw new IllegalArgumentException( 
"A zero vector of was passed to OrientationControl.setOrientationVectors" );
        }
        
        if( ( frontVector[1] * aboveVector[2] - 
                frontVector[2] * aboveVector[1] == 0 ) && 
            ( frontVector[2] * aboveVector[0] - 
                frontVector[0] * aboveVector[2] == 0 ) && 
            ( frontVector[0] * aboveVector[1] - 
                frontVector[1] * aboveVector[0] == 0 ) )
        {
            throw new IllegalArgumentException( 
"The front and above vectors passed to " +
                "OrientationControl.setOrientationVectors cannot be parallel" );
        }
        
        nSetOrientationVec( frontVector, aboveVector );
    }

    private native void nSetOrientation(int heading, int pitch, int roll);
    public void setOrientation(int heading, int pitch, int roll)
    {
        nSetOrientation( heading, pitch, roll );
    }
}