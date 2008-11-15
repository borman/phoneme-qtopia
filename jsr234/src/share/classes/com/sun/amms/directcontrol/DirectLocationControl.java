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

class DirectLocationControl extends DirectAMMSControl implements LocationControl
{
    private native void nGetCartesian( int[] coord );
    public int[] getCartesian()
    {
        int [] coord = new int [3];
        nGetCartesian( coord );
        
        return coord;
    }
    
    private native void nSetCartesian(int x, int y, int z);
    public void setCartesian(int x, int y, int z)
    {
        nSetCartesian( x, y, z );
    }
    
    private native void nSetSpherical(int azimuth, int elevation, int radius);
    public void setSpherical(int azimuth, int elevation, int radius)
    {
        if( radius < 0 )
        {
            throw new IllegalArgumentException( 
                    "Negative radius passed to setSpherical()" );
        }
        nSetSpherical( azimuth, elevation, radius );
    }
}
