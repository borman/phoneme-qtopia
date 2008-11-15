/*
 * $RCSfile: ViewportNode.java,v $
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
package com.sun.perseus.model;

import com.sun.perseus.j2d.ViewportProperties;

import com.sun.perseus.util.SVGConstants;

/**
 * <code>Viewport1Node</code> is the interface that all <code>ModelNode</code>
 * (see {@link com.sun.perseus.model.ModelNode ModelNode}) which create 
 * viewports, such as image, svg, video and animation, implement.
 *
 * IMPORTANT NOTE: setting a property automatically sets the inherited flag 
 * to false.
 *
 * @see ModelNode
 *
 */
public interface ViewportNode extends DecoratedNode, ViewportProperties {
    // ===================================================================
    // Property indices. Values are used as masks in operations
    // ===================================================================

    /**
     * The viewport-fill property controls the color of the 
     * viewport fill operation
     * @see com.sun.perseus.j2d.GraphicsProperties#setFill
     */ 
    int PROPERTY_VIEWPORT_FILL        = 1 << 21;

    /**
     * Controls the opacity used when filling the viewport1 
     * @see com.sun.perseus.j2d.GraphicsProperties#setOpacity
     */
    int PROPERTY_VIEWPORT_FILL_OPACITY = 1 << 22;

    /**
     * Default inheritance setting (Y=yes, N=no):
     * <pre>
     * - N viewport-fill
     * - N viewport-opacity
     * </pre>
     */
    int DEFAULT_INHERITANCE = 0x0000;

    /**
     * Default color relative (Y=yes, N=no):
     * <pre>
     * - N viewport-fill
     * - N viewport-opacity
     * </pre>
     */
    int DEFAULT_COLOR_RELATIVE = 0x0000;


    /**
     * Number of properties in a ViewportNode
     */
    int NUMBER_OF_PROPERTIES = 2;
}

