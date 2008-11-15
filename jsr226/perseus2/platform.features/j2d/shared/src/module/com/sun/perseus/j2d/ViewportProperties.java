/*
 * $RCSfile: ViewportProperties.java,v $
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

/**
 * This interface is used to access and set computed property values
 * for viewport specific properties like viewport-fill color and
 * viewport-fill-opacity. 
 *
 */
public interface ViewportProperties {
    // =======================================================================
    // Initial Property values
    // =======================================================================

    /**
     * Default value for viewport-fill opacity
     */
    float INITIAL_VIEWPORT_FILL_OPACITY = 1.0f;

    /**
     * Default value for the viewport fill paint
     */
    RGB INITIAL_VIEWPORT_FILL = null;


    // =======================================================================
    // Property access
    // =======================================================================


    /**
     * @param viewportFillOpacity the new opacity to use for viewport
     *        fill operations.
     *        The value is clamped to the [0, 1] range.
     */
    void setViewportFillOpacity(float viewportFillOpacity);

    /**
     * @return the opacity used for viewport fill operations, in the 
     *         [0, 1] range.
     */
    float getViewportFillOpacity();

    /**
     * @param fill the new paint to use for viewport fill operations
     */
    void setViewportFill(PaintServer fill);    

    /**
     * @return the paint used for viewport fill operations
     */
    PaintServer getViewportFill();
}
