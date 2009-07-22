/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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


package com.sun.amms;

import com.sun.amms.directcontrol.DirectSnapshotControl;
import com.sun.amms.directcontrol.DirectZoomControl;
import com.sun.amms.PhotoFormatControl;
import javax.microedition.media.Control;
import com.sun.mmedia.Jsr234Proxy;
import com.sun.mmedia.DirectPlayer;
import com.sun.mmedia.DirectCamera;
import com.sun.amms.directcontrol.DirectRDSControl;
import com.sun.amms.directcontrol.DirectTunerControl;
import com.sun.amms.directcontrol.DirectCameraControl;
import com.sun.amms.directcontrol.DirectExposureControl;
import com.sun.amms.directcontrol.DirectFlashControl;
import com.sun.amms.directcontrol.DirectFocusControl;

public class SupplementsToMMAPI extends Jsr234Proxy{
    
    private final static String [] _playerAmmsControlNames = {
        "javax.microedition.amms.control.camera.CameraControl",
        "javax.microedition.amms.control.camera.ExposureControl",
        "javax.microedition.amms.control.camera.FlashControl",
        "javax.microedition.amms.control.camera.FocusControl",
        "javax.microedition.amms.control.camera.SnapshotControl",
        "javax.microedition.amms.control.camera.ZoomControl",
        "javax.microedition.amms.control.ImageFormatControl",
        "javax.microedition.amms.control.tuner.RDSControl",
        "javax.microedition.amms.control.tuner.TunerControl" };
    
    public SupplementsToMMAPI() {}
    
    public String[] getJsr234PlayerControlNames() {
        return _playerAmmsControlNames;
    }
    
    public Control getRDSControl( DirectPlayer p )
    {
        return DirectRDSControl.createInstance( p.getNativeHandle() ) ;
    }
    
    public Control getTunerControl( DirectPlayer p )
    {
        return DirectTunerControl.createInstance( p.getNativeHandle(), p.getLocator() );
    }

    public Control getCameraControl( DirectCamera cam )
    {
        return DirectCameraControl.createInstance( cam );
    }
    
    public Control getExposureControl( DirectPlayer p )
    {
        return DirectExposureControl.createInstance( p.getNativeHandle() );
    }
    
    public Control getFlashControl( DirectPlayer p )
    {
        return DirectFlashControl.createInstance( p.getNativeHandle() );
    }
    
    public Control getFocusControl( DirectCamera cam )
    {
        return DirectFocusControl.createInstance( cam );
    }
    
    public Control getSnapshotControl( DirectCamera cam )
    {
        return DirectSnapshotControl.createInstance( cam );
    }
    
    public Control getZoomControl( DirectPlayer p )
    {
        return DirectZoomControl.createInstance( p.getNativeHandle() );
    }
    
    public Control getImageFormatControl( DirectCamera cam )
    {
        return PhotoFormatControl.createInstance( cam );
    }
}
