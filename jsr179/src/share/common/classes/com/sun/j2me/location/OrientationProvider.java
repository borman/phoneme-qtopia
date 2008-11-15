/*
 *
 *
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

package com.sun.j2me.location;

import javax.microedition.location.LocationException;
import javax.microedition.location.Orientation;

/**
 * This is a starting point of Orientation Provider implementation
 */
public class OrientationProvider {

	/** The handler of Orientation Provider */
    private static int provider = 0;
	
    private OrientationProvider(){
    }
    
    public static OrientationProvider getInstance()
    throws LocationException {
    	/* Call open() one time to prepare or start Orientation device */
    	if (provider == 0) {
			provider = open();
		}
    	if (provider == 0) {
            throw new LocationException("Orientation retrieval not supported");
    	}
        return new OrientationProvider();
    }
    
    public Orientation getOrientation() {
        Orientation orientation = null;
        boolean status;
        OrientationInfo orientationInfo = new OrientationInfo();
    
        if (getOrientation0(provider, orientationInfo))
            orientation = orientationInfo.getOrientation();
        else 
            orientationInfo = null;

        return orientation;
    }

	private static native int open();
    private native boolean getOrientation0(int provider, OrientationInfo orientationInfo);
}    

/**
 * The class contains information about Orientation
 */
class OrientationInfo {
    /** Angle off the horizon. */
    private float azimuth;
    /** Sample uses magnetic north. */
    private boolean isMagnetic;
    /** Pitch direction. */
    private float pitch;
    /** Roll direction. */
    private float roll;
    /**
     * Init class info in the native code
     */
    static {
        initNativeClass();
    }
    /**
     * Initializes native file handler.
     */
    private native static void initNativeClass();

    public Orientation getOrientation(){
        Orientation orientation = null;
        
        try{
            orientation = new Orientation(azimuth, isMagnetic, pitch, roll);
        } catch(Exception e){
        }
        
        return orientation; 
    }

    public String toString(){
        return new String("OrientationInfo (azimuth = " + azimuth 
                          + ", isMagnetic = " + isMagnetic
                          + ", pitch = " + pitch                          
                          + ", roll = " + roll + ")");
    }
}

