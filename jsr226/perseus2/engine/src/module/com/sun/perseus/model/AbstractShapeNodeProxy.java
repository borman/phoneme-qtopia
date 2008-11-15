/*
 * $RCSfile: AbstractShapeNodeProxy.java,v $
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

import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.RenderContext;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Transform;

/**
 * An <code>AbstractShapeNodeProxy</code> delegates its rendering to a 
 * proxied <code>AbstractShapeNode</code> and has the same policy for
 * handling computed value changes.
 *
 * @version $Id: AbstractShapeNodeProxy.java,v 1.3 2006/06/29 10:47:29 ln156897 Exp $
 */
public class AbstractShapeNodeProxy extends AbstractRenderingNodeProxy {
    /**
     * @param proxiedNode <tt>AbstractShapeNode</tt> to proxy
     */
    protected AbstractShapeNodeProxy(final AbstractShapeNode proxiedNode) {
        super(proxiedNode);
    }

    /**
     * @param newFill the new computed fill property.
     */
    public void setFill(final PaintServer newFill) {
        this.fill = newFill;
    }

    /**
     * @param newStroke the new computed stroke property.
     */
    public void setStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
    }

    /**
     * @param newStrokeWidth the new computed stroke-width property value.
     */
    public void setStrokeWidth(final float newStrokeWidth) {
        strokeWidth = newStrokeWidth;

    }

    /**
     * @param newStrokeLineJoin the new computed value for stroke-line-join
     */
    public void setStrokeLineJoin(final int newStrokeLineJoin) {
        super.setStrokeLineJoin(newStrokeLineJoin);

    }

    /**
     * @param newStrokeLineCap the new value for the stroke-linecap property.
     */
    public void setStrokeLineCap(final int newStrokeLineCap) {
        super.setStrokeLineCap(newStrokeLineCap);

    }

    /**
     * @param newStrokeMiterLimit the new computed stroke-miterlimit property.
     */
    public void setStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = newStrokeMiterLimit; 

    }

    /**
     * @param newStrokeDashArray the new computed stroke-dasharray property 
     *        value.
     */
    public void setStrokeDashArray(final float[] newStrokeDashArray) {
        strokeDashArray = newStrokeDashArray;

    }

    /**
     * @param newStrokeDashOffset the new stroke-dashoffset computed property 
     *        value.
     */
    public void setStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = newStrokeDashOffset;

    }

    /**
     * @param newFillOpacity the new computed value for the fill opacity 
     *        property.
     */
    public void setFillOpacity(final float newFillOpacity) {                
        super.setFillOpacity(newFillOpacity);
        
    }

    /**
     * @param newStrokeOpacity the new computed stroke-opacity property.
     */
    public void setStrokeOpacity(final float newStrokeOpacity) {
        super.setStrokeOpacity(newStrokeOpacity);
        
    }
}
