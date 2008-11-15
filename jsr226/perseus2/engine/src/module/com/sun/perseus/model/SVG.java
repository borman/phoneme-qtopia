/*
 * $RCSfile: SVG.java,v $
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

import com.sun.perseus.platform.MathSupport;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPath;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGSVGElement;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Path;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.ViewportProperties;
import com.sun.perseus.j2d.RenderGraphics;

/**
 * An <code>SVG</code> node represents an <code>&lt;code&gt;</code>
 * element. In addition to a standard <code>StructureNode</code>, an
 * <code>SVG</code> node can have a view box which causes a transform
 * to be applied before drawing its children. This transform maps children
 * viewBox coordinates into the SVG's parent <code>Viewport</code>
 * coordinate system.
 *
 * @see Viewport
 *
 * @version $Id: SVG.java,v 1.12 2006/06/29 10:47:34 ln156897 Exp $
 */
public class SVG extends StructureNode implements SVGSVGElement, SVGPoint,
        ViewportNode, PaintTarget {
    
    /**
     * This SVG node's viewBox. If null, then there is
     * no viewBox.
     */
    protected float[][] viewBox;

    /**
     * Controls the way the svg viewBox is aligned in
     * the parent viewport. For SVG Tiny, one of 
     * ALIGN_XMIDYMID or ALIGN_NONE.
     */
    protected int align = ALIGN_XMIDYMID;

    /**
     * The SVG document's requested width
     */
    protected float width = 100;

    /**
     * The SVG document's requested height
     */
    protected float height = 100;

    /**
     * The current scale
     */
    protected float currentScale = 1;

    /**
     * The current translate along the x axis
     */
    protected float tx;

    /**
     * The current translate along the y axis
     */
    protected float ty;

    /**
     * The current rotation angle.
     */
    protected float currentRotate;

    /**
     * Precision adjustment, needed to keep the delta between the current
     * time and the long values used internally.
     */
    protected float currentTimeDelta = 0f;

    /**
     * The current viewport fill color.
     */
    protected PaintServer viewportFill = INITIAL_VIEWPORT_FILL;

    /**
     * The current viewport fill opacity.
     */
    protected float viewportFillOpacity = INITIAL_VIEWPORT_FILL_OPACITY;

    /**
     * The current syncBehaviorDefault attribute value. The default in 
     * our implementation is "canSlip".
     */
    protected String syncBehaviorDefault = SVGConstants.SVG_CAN_SLIP_VALUE;
    
    /**
     * The current syncToleranceDefault attribute value. The default is set
     * to 2s as that is the maximum allowed by the SMIL specification. Using
     * the maximum allows our implementation the greatest flexibility.
     */
    protected Time syncToleranceDefault = new Time(2000);
    
    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public SVG(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_SVG_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_SVG_TAG;
    }

    /**
     * Returns the default value for the width, height, viewport-fill, 
     * viewport-fill-opacity, syncBehaviorDefault and syncToleranceDefault 
     * traits.
     *
     * @return an array of trait default values, used if this element
     *         requires that the default trait value be explicitly 
     *         set through a setTrait call. This happens, for example,
     *         with the begin trait value on animation elements.
     */
    public String[][] getDefaultTraits() {
        return new String[][] { {SVGConstants.SVG_WIDTH_ATTRIBUTE, "100%"},
                                {SVGConstants.SVG_HEIGHT_ATTRIBUTE, "100%"},
                                {SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE, "none"},
                                {SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE, "1.0"},
                                {SVGConstants.SVG_SYNC_BEHAVIOR_DEFAULT_ATTRIBUTE, "canSlip"},
                                {SVGConstants.SVG_SYNC_TOLERANCE_DEFAULT_ATTRIBUTE, "2.0s"}};
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>SVG</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>SVG</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new SVG(doc);
    }

    /**
     * Sets the viewport width. 
     * @param newWidth Should be greater than 0
     */
    public void setWidth(final float newWidth) {
        if (newWidth < 0) {
            throw new IllegalArgumentException();
        }

        if (newWidth == width) {
            return;
        }

        modifyingNode();
        this.width = newWidth;
        computeCanRenderWidthBit(width);

        modifiedNode();
    }

    /**
     * @return the viewport's  width
     */
    public float getWidth() {
        return this.width;
    }

    /**
     * Sets the viewport  height. 
     * @param newHeight Should be greater than 0
     */
    public void setHeight(final float newHeight) {
        if (newHeight < 0) {
            throw new IllegalArgumentException();
        }

        if (newHeight == height) {
            return;
        }

        modifyingNode();
        this.height = newHeight;
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @return the viewport  height
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Sets a new value for the viewBox.
     * @param newViewBox the new viewBox for this <tt>SVG</tt>
     * 
     * @throws IllegalArgumentException if the input viewBox is 
     *         not null and not of a float {float[2], float[1], float[1]} array.
     */
    public void setViewBox(final float[][] newViewBox) {

        if (newViewBox != null) {
            if (newViewBox.length != 3 
                ||
                newViewBox[0] == null
                ||
                newViewBox[1] == null
                ||
                newViewBox[2] == null
                ||
                newViewBox[0].length != 2
                ||
                newViewBox[1][0] < 0 
                || 
                newViewBox[2][0] < 0) {
                throw new IllegalArgumentException();
            }
        }

        if (equal(newViewBox, viewBox)) {
            return;
        }

        modifyingNode();

        if (newViewBox == null) {
            viewBox = null;
        } else {
            if (viewBox == null) {
                viewBox = new float[3][];
                viewBox[0] = new float[2];
                viewBox[1] = new float[1];
                viewBox[2] = new float[1];
            }
            
            viewBox[0][0] = newViewBox[0][0];    
            viewBox[0][1] = newViewBox[0][1];    
            viewBox[1][0] = newViewBox[1][0];    
            viewBox[2][0] = newViewBox[2][0];    
        }

        recomputeTransformState();
        computeCanRenderEmptyViewBoxBit(viewBox);
        modifiedNode();
    }

    /**
     * @return this SVG's viewBox
     */
    public float[][] getViewBox() {
        return viewBox;
    }

    /**
     * @param newAlign new alignment property
     */
    public void setAlign(final int newAlign) {
        if (newAlign != ALIGN_XMIDYMID
            &&
            newAlign != ALIGN_NONE) {
            throw new IllegalArgumentException();
        }

        if (newAlign == align) {
            return;
        }

        modifyingNode();
        this.align = newAlign;
        recomputeTransformState();
        modifiedNode();
    }

    /**
     * @return this node's align property
     */
    public int getAlign() {
        return align;
    }

    /**
     * Returns the value of the given Object-valued property.
     *
     * @return the value of the given Object-valued property.
     */
    protected Object getPropertyState(final int propertyIndex) {
        // TODO: Does this need to support syncBehaviorDefault and
        // syncToleranceDefault attributes
        switch (propertyIndex) {
        case PROPERTY_VIEWPORT_FILL:
            return viewportFill;
        default: 
            return super.getPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value of the given float-valued property.
     *
     * @return the value of the given property.
     */
    protected float getFloatPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case PROPERTY_VIEWPORT_FILL_OPACITY:
            return viewportFillOpacity;
        default: 
            return super.getFloatPropertyState(propertyIndex);
        }
    }

    /**
     * Sets the computed value of the given Object-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setPropertyState(final int propertyIndex,
                                    final Object propertyValue) {
        // TODO: Does this need to support the syncToleranceDefault and 
        // syncToleranceDefault attributes
        switch (propertyIndex) {
        case PROPERTY_VIEWPORT_FILL:
            this.viewportFill = ((PaintServer) propertyValue);
            break;
        default: 
            super.setPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Sets the computed value of the given float-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setFloatPropertyState(final int propertyIndex,
                                          final float propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_VIEWPORT_FILL_OPACITY:
            this.viewportFillOpacity = propertyValue;
	    break;
        default: 
            super.setFloatPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Checks the state of the Object-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected boolean isPropertyState(final int propertyIndex,
                                      final Object propertyValue) {
        // TODO: Does this need to support syncBehaviorDefault and 
        // syncToleranceDefault attributes.
        switch (propertyIndex) {
        case ViewportNode.PROPERTY_VIEWPORT_FILL:
            return viewportFill == propertyValue;
        default: 
            return super.isPropertyState(propertyIndex, propertyValue);
        }
    }

    /**
     * Checks the state of the float property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected boolean isFloatPropertyState(final int propertyIndex,
                                           final float propertyValue) {
        switch (propertyIndex) {
        case ViewportNode.PROPERTY_VIEWPORT_FILL_OPACITY:
            return viewportFillOpacity == propertyValue;
        default: 
            return super.isFloatPropertyState(propertyIndex, propertyValue);
        }
    }

    /**
     * Recomputes all inherited properties.
     */
    void recomputeInheritedProperties() {
        // TODO: Does this need to support syncBehaviorDefault and 
        // syncToleranceDefault attributes.
        ModelNode p = ownerDocument;
        if (parent != null) {
            p = parent;
        }
        recomputePropertyState(PROPERTY_VIEWPORT_FILL, 
                               p.getPropertyState(PROPERTY_VIEWPORT_FILL));
        recomputeFloatPropertyState(PROPERTY_VIEWPORT_FILL_OPACITY,
                               p.getFloatPropertyState(PROPERTY_VIEWPORT_FILL_OPACITY));

	super.recomputeInheritedProperties();
    }

    /**
     * @param newViewportFill new viewport-fill color
     */
    public void setViewportFill(final PaintServer newViewportFill) {
        if (!isInherited(PROPERTY_VIEWPORT_FILL) && equal(newViewportFill, viewportFill)) {
            return;
        }

        modifyingNode();
        if (viewportFill != null) {
            viewportFill.dispose();
        }
        
	this.viewportFill = newViewportFill;
        setInheritedQuiet(PROPERTY_VIEWPORT_FILL, false);
        propagatePropertyState(PROPERTY_VIEWPORT_FILL, viewportFill);
        modifiedNode();
    }

    /**
     * @return this node's viewport fill property
     */
    public PaintServer getViewportFill() {
        return viewportFill;
    }

    /**
     * @return the current viewport-fill opacity property value.
     */
    public float getViewportFillOpacity() {
        return viewportFillOpacity;
    }

    /**
     * Setting the opacity property clears the inherited and color
     * relative states (they are set to false).
     *
     * @param newOpacity the new opacity property
     */
    public void setViewportFillOpacity(float newViewportFillOpacity) {
        if (!isInherited(PROPERTY_VIEWPORT_FILL_OPACITY) 
            && 
            newViewportFillOpacity == getViewportFillOpacity()) {
            return;
        }
        modifyingNode();
        if (newViewportFillOpacity > 1) {
            newViewportFillOpacity = 1;
        } else if (newViewportFillOpacity < 0) {
            newViewportFillOpacity = 0;
        }
        setInheritedQuiet(PROPERTY_VIEWPORT_FILL_OPACITY, false);
	viewportFillOpacity = newViewportFillOpacity;
        propagateFloatPropertyState(PROPERTY_VIEWPORT_FILL_OPACITY, 
                                    viewportFillOpacity);
        modifiedNode();
    }

    /**
     * Returns the current syncBehaviorDefault attribute value.
     */
    public String getSyncBehaviorDefault() {
        return syncBehaviorDefault;
    }
    
    /**
     * Sets the current syncBehaviorDefault attribute value.
     *
     * @param newSyncBehaviorDefault the new syncBehaviorDefault value to set
     */
    public void setSyncBehaviorDefault(String newSyncBehaviorDefault) {
        if (syncBehaviorDefault.equals(newSyncBehaviorDefault)) {
            return;
        }
        
        // TODO: We are not invoking modifyingNode/modifiedNode here since there
        // is no exising infrastructure to propogate changes to attribute
        // values. However the updates should be propogated post the 
        // rearchitecture of property/attribute handling
        syncBehaviorDefault = newSyncBehaviorDefault;
    }
    
    /**
     * Returns the current syncToleranceDefault attribute value.
     */
    public Time getSyncToleranceDefault() {
        return syncToleranceDefault;
    }
    
    /**
     * Sets the current syncToleranceDefault attribute value.
     *
     * @param newSyncToleranceDefault the new syncToleranceDefault value to set
     */
    public void setSyncToleranceDefault(Time newSyncToleranceDefault) {
        if (syncToleranceDefault.equals(newSyncToleranceDefault)) {
            return;
        }
        
        // TODO: We are not invoking modifyingNode/modifiedNode here since there
        // is no exising infrastructure to propogate changes to attribute
        // values. However the updates should be propogated post the 
        // rearchitecture of property/attribute handling
        syncToleranceDefault = newSyncToleranceDefault;
    }
    
    /**
     * Appends the viewBox to viewport transform, if there is a viewBox.
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(Transform tx,
                                        final Transform workTx) {
        if (viewBox == null) {
            return tx;
        }

        tx = recycleTransform(tx, workTx);

        Viewport vp = (Viewport) getOwnerDocument();
        float w = vp.getWidth();
        float h = vp.getHeight();
        getFittingTransform(viewBox[0][0], viewBox[0][1],
                            viewBox[1][0], viewBox[2][0],
                            0, 0, w, h, align, tx);
        
        return tx;
    }

    /**
     * SVG handles the version, baseProfile, viewBox, zoomAndPan,
     * width, height, viewport-fill, viewport-fill-opacity, 
     * preserveAspectRatio, syncBehaviorDefault and syncToleranceDefault traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VERSION_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_SYNC_BEHAVIOR_DEFAULT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_SYNC_TOLERANCE_DEFAULT_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Supported traits: viewport-fill, viewport-fill-opacity
     *
     * @param name the requested trait name (e.g., "viewport-fill-opacity").
     * @return the trait's value, as a string (e.g., "1.0").
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    String getSpecifiedTraitImpl(final String name)
        throws DOMException {
        // TODO: Does this need to support the syncBehaviorDefault and
        // syncToleranceDefault attributes
        if ((SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_VIEWPORT_FILL))
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_VIEWPORT_FILL_OPACITY))) {
            return SVGConstants.CSS_INHERIT_VALUE;
        } else {
            return super.getSpecifiedTraitImpl(name);
        }
    }


    /**
     * SVG handles the version, baseProfile, viewBox, zoomAndPan,
     * viewport-fill, viewport-fill-opacity, syncBehaviorDefault and
     * syncToleranceDefault traits.
     *
     * @param name the requested trait's name (e.g., "zoomAndPan")
     * @return the requested trait string value (e.g., "disable")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VERSION_ATTRIBUTE == name) {
            return SVGConstants.SVG_VERSION_1_1_VALUE;
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            if (viewBox == null) {
                return "";
            } else {
                return "" + viewBox[0][0] + SVGConstants.COMMA 
                    + viewBox[0][1] + SVGConstants.COMMA 
                    + viewBox[1][0] + SVGConstants.COMMA 
                    + viewBox[2][0];
            }
        } else if (SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == name) {
            switch (ownerDocument.zoomAndPan) {
            case Viewport.ZOOM_PAN_MAGNIFY:
                return SVGConstants.SVG_MAGNIFY_VALUE;
            case Viewport.ZOOM_PAN_DISABLE:
                return SVGConstants.SVG_DISABLE_VALUE;
            default:
                throw new Error();
            }
        } else if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == name) {
            return SVGConstants.SVG_BASE_PROFILE_TINY_VALUE;
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return Float.toString(getWidth());
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(getHeight());
        } else if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == name) {
            return alignToStringTrait(align);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            return toString(getViewportFill());
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getViewportFillOpacity());
        }  else if (SVGConstants.SVG_SYNC_BEHAVIOR_DEFAULT_ATTRIBUTE == name) {
            return getSyncBehaviorDefault();
        }  else if (SVGConstants.SVG_SYNC_TOLERANCE_DEFAULT_ATTRIBUTE == name) {
            return Time.toStringTrait(getSyncToleranceDefault());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * SVG handles width, height and viewport-fill-opacity float traits.
     * Other attributes are handled by the super class.
     *
     * @param name the requested trait name.
     * @param the requested trait's floating point value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    float getFloatTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return getWidth();
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return getHeight();
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return getViewportFillOpacity();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * SVG handles the viewBox Rect trait.
     *
     * @param name the requested trait name (e.g., "viewBox")
     * @return the requested trait SVGRect value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRect SVGRect}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRect getRectTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            return toSVGRect(viewBox);
        } else {
            return super.getRectTraitImpl(name);
        }
    }

    /**
     * Supported color traits: viewport-fill
     *
     * @param name the requested trait's name.
     * @return the requested trait's value, as an <code>SVGRGBColor</code>.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRGBColor getRGBColorTraitImpl(String name)
        throws DOMException {
        if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE.equals(name)) {
            return toSVGRGBColor(SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE,
                                 getViewportFill());
        } else {
            return super.getRGBColorTraitImpl(name);
        }
    }

    /**
     * The viewBox, viewport-fill and viewport-fill-opacity traits are
     * animatable.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RECT);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName) {
           return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RGB_COLOR); 
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName) {
           return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT); 
	} else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     */
    void setFloatArrayTrait(final String name, final float[][] value)
        throws DOMException {
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setWidth(value[0][0]);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setHeight(value[0][0]);
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(value);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
	    if (value == null || value.length == 0) {
                setViewportFill(null);
            } else {
                setViewportFill(toRGB(name, value));
            }
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            setViewportFillOpacity(value[0][0]);
        } else {
            super.setFloatArrayTrait(name, value);
        }
    }

    /**
     * Validates the input trait value.
     *
     * @param namespaceURI the trait's namespace URI.
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    String validateTraitNS(final String namespaceURI,
                           final String traitName,
                           final String value,
                           final String reqNamespaceURI,
                           final String reqLocalName,
                           final String reqTraitNamespace,
                           final String reqTraitName) throws DOMException {
        if (namespaceURI != null && namespaceURI != NULL_NS) {
            return super.validateTraitNS(namespaceURI,
                                         traitName,
                                         value,
                                         reqNamespaceURI,
                                         reqLocalName,
                                         reqTraitNamespace,
                                         reqTraitName);
        }
        
        // TODO: Does this need to support the syncBehaviorDefault and 
        // syncToleranceDefault attributes.

        if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName) {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_FLOAT);
        }

        return super.validateTraitNS(namespaceURI,
                                     traitName,
                                     value,
                                     reqNamespaceURI,
                                     reqLocalName,
                                     reqTraitNamespace,
                                     reqTraitName);
    }


    /**
     * Validates the float input trait value.
     *
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return ownerDocument.viewBoxParser.parseViewBox(value);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
            return new float[][] {
                        {parsePositiveLengthTrait(traitName, value, false)}
                    };
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName) {
            RGB color = ViewportProperties.INITIAL_VIEWPORT_FILL;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                color = (RGB) getInheritedPropertyState(PROPERTY_VIEWPORT_FILL);
            } else {
                color = parseColorTrait
                    (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE, value);
            }

	    // A value of "none" is not checked for, even though
	    // it is a valid viewport-fill value, it is not considered
	    // valid for animations. So here color will compute to "null"
	    // for a viewport-fill of "none"
	    //
            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName) {
            float v = ViewportNode.INITIAL_VIEWPORT_FILL_OPACITY;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                if (parent != null) {
                    v = getInheritedFloatPropertyState(PROPERTY_VIEWPORT_FILL_OPACITY);
                }
            } else {
                v = parseFloatTrait(traitName, value);
                if (v < 0) {
                    v = 0;
                } else if (v > 1) {
                    v = 1;
                }
            }
            return new float[][] {{v}};
        } else {
            return super.validateFloatArrayTrait(traitName,
                                                 value,
                                                 reqNamespaceURI,
                                                 reqLocalName,
                                                 reqTraitNamespace,
                                                 reqTraitName);
        }
    }

    /**
     * SVG handles the version, baseProfile, viewBox, zoomAndPan, 
     * viewport-fill, viewport-fill-opacity, syncBehaviorDefault and
     * syncToleranceDefault traits.
     *
     * @param name the trait's name (e.g., "viewBox")
     * @param value the new trait's string value (e.g., "0 0 400 300")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_VERSION_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (!SVGConstants.SVG_VERSION_1_1_VALUE.equals(value)
                &&
                !SVGConstants.SVG_VERSION_1_2_VALUE.equals(value)) {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (!SVGConstants.SVG_BASE_PROFILE_TINY_VALUE.equals(value)) {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(toViewBox(name, value));
        } else if (SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == name) {
            if (SVGConstants.SVG_MAGNIFY_VALUE.equals(value)) {
                ownerDocument.setZoomAndPan(Viewport.ZOOM_PAN_MAGNIFY);
                applyUserTransform();
            } else if (SVGConstants.SVG_DISABLE_VALUE.equals(value)) {
                ownerDocument.setZoomAndPan(Viewport.ZOOM_PAN_DISABLE);
                applyUserTransform();
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setWidth(parsePositiveLengthTrait(name, value, true));
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setHeight(parsePositiveLengthTrait(name, value, false));
        } else if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants
                .SVG_SVG_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE.equals(value)) {
                setAlign(ALIGN_XMIDYMID);
            } else if (SVGConstants.SVG_NONE_VALUE.equals(value)) {
                setAlign(ALIGN_NONE);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {

            // ========================= viewport-fill =========================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_VIEWPORT_FILL, true);
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                setViewportFill(null);
            } else {
                PaintServer viewportFill = parsePaintTrait
                    (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE, this, value);
                if (viewportFill != null) {
                    setViewportFill(viewportFill);
                }
            }
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {

            // ====================== viewport-fill-opacity ======================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setFloatInherited(PROPERTY_VIEWPORT_FILL_OPACITY, true);
            } else {
                setViewportFillOpacity(parseFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_SYNC_BEHAVIOR_DEFAULT_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.SVG_CAN_SLIP_VALUE.equals(value) ||
                SVGConstants.SVG_LOCKED_VALUE.equals(value) ||
                SVGConstants.SVG_INDEPENDENT_VALUE.equals(value)) {
                setSyncBehaviorDefault(value);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                // Since there is no parent to inherit the value from
                // the spec states that the implementation can choose the 
                // default, which for us is "canSlip"
                setSyncBehaviorDefault(SVGConstants.SVG_CAN_SLIP_VALUE);
            } else {
                throw illegalTraitValue(name, value);
            }
        }  else if (SVGConstants.SVG_SYNC_TOLERANCE_DEFAULT_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                // Since there is no parent to inherit the value from
                // the spec states that the implementation can choose the 
                // default, which for us is "2s"
                setSyncToleranceDefault(parseClockTrait(name, "2s"));
            } else {
                setSyncToleranceDefault(parseClockTrait(name, value));
            }
        }  else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * SVG handles the viewBox Rect trait.
     *
     * @param name the trait name (e.g., "viewBox"
     * @param rect the trait value
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGRect
     * SVGRect}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGRect is
     * invalid if the width or height values are set to negative.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setRectTraitImpl(final String name, final SVGRect rect)
        throws DOMException {
        // Note that here, we use equals because the string 
        // has not been interned.
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            if (rect == null) {
                throw illegalTraitValue(name, null);
            }
            
            if (rect.getWidth() < 0 || rect.getHeight() < 0) {
                throw illegalTraitValue(name, toStringTrait(new float[]
                    {rect.getX(), 
                     rect.getY(), 
                     rect.getWidth(), 
                     rect.getHeight()}));
            }
                
            setViewBox(new float[][]
                {new float[] {rect.getX(), rect.getY()}, 
                 new float[] {rect.getWidth()}, 
                 new float[] {rect.getHeight()}
                });
            
        } else {
            super.setRectTraitImpl(name, rect);
        }
    }

    /**
     * SVG handles width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name.
     * @param value the new trait's floating point value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        try {
            if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
                setWidth(value);
            } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
                setHeight(value);
            } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
                setViewportFillOpacity(value);
            } else {
                super.setFloatTraitImpl(name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, Float.toString(value));
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name
            ||
            SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            float[] vb = {value[0][0], value[0][1], value[1][0], value[2][0]};
            return toStringTrait(vb);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
	    // Unlike SVG_FILL_ATTRIBUTE, SVG_VIEWPORT_FILL_ATTRIBUTE can be
	    // null.
	    if (value == null || value.length == 0) {
                return SVGConstants.CSS_NONE_VALUE;
            } else {
                return toRGBString(name, value);
            }
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor}.
     *
     * Supported color traits: viewport-fill
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is null.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    void setRGBColorTraitImpl(final String name, final SVGRGBColor color)
        throws DOMException {
        try {
            // We use .equals here because the name string may not have been
            // interned.
            if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE.equals(name)) {
                setViewportFill((RGB) color);
            } else {
                super.setRGBColorTraitImpl(name, color);
            } 
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                                   iae.getMessage());
        }
    }

    // ========================================================================

    /**
     *
     */
    public void setCurrentScale(final float value)
        throws DOMException {
        if (value == 0) {
            throw new DOMException
                (DOMException.INVALID_ACCESS_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_INVALID_PARAMETER_VALUE,
                  new String[] {"SVGSVGElement",
                                "setCurrentScale",
                                "value",
                                Float.toString(value)}));
        } else {
            currentScale = value;
            applyUserTransform();
        }
    }

    
    /**
     * @return the state of the animation timeline.
     */
    public boolean animationsPaused() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * Pauses animation timeline. If user agent has pause/play control that
     * control is changed to paused state.  Has no effect if timeline already
     * paused.
     */
    public void pauseAnimations() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * Resumes animation timeline. If user agent has pause/play control that
     * control is changed to playing state. Has no effect if timeline is already
     * playing.
     */
    public void unpauseAnimations() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     *
     */
    public float getCurrentScale() {
        return currentScale;
    }

    /**
     * <The position and size of the viewport (implicit or explicit) that
     * corresponds to this 'svg' element. When the user agent is actually
     * rendering the content, then the position and size values represent the
     * actual values when rendering.  If this SVG document is embedded as part
     * of another document (e.g., via the HTML 'object' element), then the
     * position and size are unitless values in the coordinate system of the
     * parent document. (If the parent uses CSS or XSL layout, then unitless
     * values represent pixel units for the current CSS or XSL viewport, as
     * described in the CSS2 specification.)  If the parent element does not
     * have a coordinate system, then the user agent should provide reasonable
     * default values for this attribute.
     *
     * <p> The object itself and its contents are both readonly. {@link
     * org.w3c.dom.DOMException DOMException} with error code
     * NO_MODIFICATION_ALLOWED_ERR is raised if attempt is made to modify
     * it. The returned SVGRect object is "live", i.e. its x, y, width, height
     * is automatically updated if viewport size or position changes.  </p>
     */
    public SVGRect getViewport() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     *
     */
    public void setCurrentRotate(final float value) {
        currentRotate = value;
        applyUserTransform();
    }

    /**
     *
     */
    public float getCurrentRotate() {
        return currentRotate;
    }

    /**
     * The initial values for currentTranslate is SVGPoint(0,0).
     */
    public SVGPoint getCurrentTranslate() {
        return this;
    }

    /**
     *
     */
    public float getCurrentTime() {
        return ownerDocument.timeContainerRootSupport
            .lastSampleTime.value / 1000f + currentTimeDelta;
    }

    /**
     * The following API is needed to support moving forwards in timeline. 
     * An implementation is normally designed to seek forward
     * in timeline and setting the time backwards.
     * This is not meant ability to play the animation backwards.
     *
     * @param seconds the new current time to set to the document (in seconds).
     */
    public void setCurrentTime(final float seconds) {
        currentTimeDelta = seconds - ((long) (seconds * 1000)) / 1000f;
        ownerDocument.timeContainerRootSupport.seekTo
            (new Time((long) (seconds * 1000)));
        ownerDocument.applyAnimations();
    }

    /**
     * This
     * object can be used to modify value of traits which are compatible with
     * {@link org.w3c.dom.svg.SVGMatrix SVGMatrix} type using {@link
     * org.w3c.dom.svg.SVGElement#setMatrixTrait setMatrixTrait} method. The
     * internal representation of the matrix is as follows: <p>
     *
     * <pre>
     *  [  a  c  e  ]
     *  [  b  d  f  ]
     *  [  0  0  1  ]
     * </pre>
     * </p>
     *
     * @see org.w3c.dom.svg.SVGMatrix
     * @param a the 0,0 matrix parameter
     * @param b the 1,0 matrix parameter
     * @param c the 0,1 matrix parameter
     * @param d the 1,1 matrix parameter
     * @param e the 0,2 matrix parameter
     * @param f the 1,2 matrix parameter
     * @return a new <code>SVGMatrix</code> object with the requested 
     *         components.
     */

    public SVGMatrix createSVGMatrixComponents
        (final float a, 
         final float b, 
         final float c, 
         final float d, 
         final float e, 
         final float f) {
        return new Transform(a, b, c, d, e, f);
    }

    /**
     * @return a new {@link org.w3c.dom.svg.SVGRect SVGRect} object. This object
     * can be used to modify value of traits which are compatible with {@link
     * org.w3c.dom.svg.SVGRect SVGRect} type using {@link
     * org.w3c.dom.svg.SVGElement#setRectTrait setRectTrait} method. The intial
     * values for x, y, width, height of this new SVGRect are zero.
     */
    public SVGRect createSVGRect() {
        return new Box(0, 0, 0, 0);
    }

    /**
     * @return new {@link org.w3c.dom.svg.SVGPath SVGPath} object. This object
     * can be used to modify value of traits which are compatible with {@link
     * org.w3c.dom.svg.SVGPath SVGPath} type using {@link
     * org.w3c.dom.svg.SVGElement#setPathTrait setPathTrait} method.
     */
    public SVGPath createSVGPath() {
        return new Path();
    }

    /**
     * @return new {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor} object. This
     * object can be used to modify value of traits which are compatible with
     * {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor} type using {@link
     * org.w3c.dom.svg.SVGElement#setRGBColorTrait setRGBColorTrait} method.
     * @throws SVGException with error code SVG_INVALID_VALUE_ERR: if any of the
     * parameters is not in the 0..255 range.</li>
     * @param red the red rgb component.
     * @param green the green rgb component.
     * @param blue the blue rgb component.
     */
    public SVGRGBColor createSVGRGBColor(final int red, 
                                         final int green, 
                                         final int blue)
        throws SVGException {
        if (red < 0 
            || red > 255 
            || green < 0 
            || green > 255 
            || blue < 0 
            || blue > 255) {
            throw new SVGException(SVGException.SVG_INVALID_VALUE_ERR, null);
        }

        return new RGB(red, green, blue);
    }


    /**
     * Sets the x component of the point to the specified float value.
     *
     * @param value the x component value
     *
     */
    public void setX(final float value) {
        tx = value;
        applyUserTransform();
    }

    /**
     * Sets the y component of the point to the specified float value.
     *
     * @param value the y component value
     *
     */

    public void setY(final float value) {
        ty = value;
        applyUserTransform();
    }


    /**
     * Returns the x component of the point.
     *
     * @return the x component of the point.
     *
     */
    public float getX() {
        return tx;
    }

    /**
     * Returns the y component of the point.
     *
     * @return the y component of the point.
     *
     */
    public float getY() {
        return ty;
    }

    /**
     * Uses currentScale, currentRotate, tx and ty to compute the 
     * owner document's transform.
     */
    void applyUserTransform() {
        if (ownerDocument.zoomAndPan == Viewport.ZOOM_PAN_MAGNIFY) {
            Transform txf = new Transform(currentScale, 0,
                                          0, currentScale,
                                          tx, ty);
            txf.mRotate(currentRotate);

            ownerDocument.setTransform(txf);

        } else {
            if (ownerDocument.getTransform() != null) {
                // ownerDocument's transform has been touched
                // so, set it to identity
                ownerDocument.setTransform(new Transform(null));
            }
        }
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>. 
     * The viewport is filled if viewportFill is not null and then
     * the first child's paint method is called.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        if (canRenderState != 0) {
            return;
        }

        if (viewportFill != null) {
	    // Set fill and opacity values to viewport-fill and viewport-fill-opacity values,
	    // so we can use fillRect() to fill the viewport
            rg.setFill(viewportFill);
            rg.setFillOpacity(viewportFillOpacity);

            // Fill viewport
            rg.setTransform(null);
            rg.fillRect(0,0, ownerDocument.getWidth(), ownerDocument.getHeight(), 0, 0);
        }

        paint(getFirstChildNode(), rg);
    }

    /**
     * @param paintType the key provided by the PaintTarget when it subscribed 
     *        to associated PaintServer.
     * @param paintServer the PaintServer generating the update.
     */
    public void onPaintServerUpdate(final String paintType,
                                    final PaintServer paintServer) {
        if (paintType == SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE) {
            setViewportFill(paintServer);
        } else {
            super.onPaintServerUpdate(paintType, paintServer);
        }
    }

}
