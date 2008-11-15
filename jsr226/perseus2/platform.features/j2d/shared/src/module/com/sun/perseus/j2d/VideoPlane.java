/*
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
package com.sun.perseus.j2d;

import java.util.Vector;
import java.util.Enumeration;

/**
 * The <code>VideoPlane</code> class maintains a list of video elements that need
 * to be painted on "top", i.e painted after we are done painting the elements in the
 * document tree. Elements that need "top" rendering add themselves to this list
 * in their paint method.
 *
 */
public class VideoPlane {
    
    /**
     * List of video elements to be rendered on "top".
     */
    private Vector  topList = null;

    /**
     * A target Component associated with this graphics context. This may be
     * null. The component depends on the Java profile :
     * javax.microedition.lcdui.Canvas on profiles supporting LCDUI
     * javax.microedition.lcdui.CustomItem on profiles supporting LCDUI
     * java.awt.Component on profiles supporting AWT
     */
    private Object targetComponent = null;

    /**
     * <code>VideoPlane</code> Constructor
     */
    public VideoPlane() {}

    /**
     * Adds specified element to list of elements that will be rendered
     * on "top"
     *
     * @param topElement the element to be added to "top" list
     */
    public void addToVideoPlane(Object topElement) {
        if (topList == null) {
            topList = new Vector(1);
        }
        topList.addElement(topElement);
    }

    /**
     * The list of elements that need "top" rendering is cleared. 
     *
     */
    public void clearVideoPlane() {
        if (topList != null) {
            topList.removeAllElements();
        }
    }

    /**
     * Returns list of elements in <code>VideoPlane</code> as an Enumeration. 
     *
     * @return the list of elements maintained by <code>VideoPlane</code>.
     */
    public Enumeration getVideoPlaneElements() {
        if (topList != null) {
            return topList.elements();
        }
	return null;
    }

    /**
     * Sets the current targetComponent.
     *
     * @param targetComponent the new target component.
     */
    public void setTargetComponent(final Object targetComponent) {
        this.targetComponent = targetComponent;
    }

    /**
     * Gets the current targetComponent.
     *
     * @return the current target component. May be null.
     */
    public Object getTargetComponent() {
        return targetComponent;
    }
}

