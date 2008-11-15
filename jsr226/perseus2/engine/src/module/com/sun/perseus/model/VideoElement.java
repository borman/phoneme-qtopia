/*
 * $RCSfile: VideoElement.java,v $
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

import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGMatrix;

import com.sun.perseus.util.SVGConstants;
import com.sun.perseus.j2d.RasterImage;
import com.sun.perseus.platform.VideoPlayer;
import com.sun.perseus.platform.MediaSupport;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.ViewportProperties;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Transform;

/**
 * The <code>VideoElement</code> class models the &lt;video&gt; tag in 
 * SVG Tiny 1.2. 
 *
 * @author <a href="mailto:marc.owerfeldt@sun.com">Marc Owerfeldt</a>
 * @version $Id: 
 */
public class VideoElement extends MediaElement implements ViewportNode, PaintTarget, Transformable {
    /**
     * The associated VideoPlayer
     */
    private VideoPlayer videoPlayer;

    /**
     * The x-position of the viewport.
     */
    protected float x = 0.0f;

    /**
     * The y-position of the viewport.
     */
    protected float y = 0.0f;

    /**
     * The width of the viewport.
     */
    protected float width = 0.0f;

    /**
     * The height of the viewport.
     */
    protected float height = 0.0f;

    /**
     * The transform behavior (Default: "geometric").
     */
    protected String transformBehavior = SVGConstants.SVG_GEOMETRIC_VALUE;

    /**
     * The overlay format (Default: "none").
     */
    protected String overlay = SVGConstants.SVG_NONE_VALUE;

    /**
     * The initial visibility format (Default: "whenStarted").
     */
    protected String initialVisibility = SVGConstants.SVG_WHEN_STARTED_VALUE;

    /**
     * The ability to gain keyboard focus (Default: "auto").
     */
    protected String focusable = SVGConstants.SVG_AUTO_VALUE;

    /**
     * The current video frame.
     */
    private RasterImage image;

    /**
     * The current viewport fill color.
     */
    protected PaintServer viewportFill = INITIAL_VIEWPORT_FILL;

    /**
     * The current viewport fill opacity.
     */
    protected float viewportFillOpacity = INITIAL_VIEWPORT_FILL_OPACITY;

    /**
     * The Transform applied to this node. 
     */
    protected Transform transform;

    /**
     * Cached Transform. May point to the parent transform.
     */
    protected Transform txf = null;

    /**
     * Cached inverse transform. May point to the parent inverse transform.
     */
    protected Transform inverseTxf = null;
    
    /**
     * @param doc the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null.
     */
    public VideoElement(final DocumentNode doc) {
        super(doc, SVGConstants.SVG_VIDEO_TAG);

        // By default, a VideoElementNode is renderable
        canRenderState &= CAN_RENDER_RENDERABLE_MASK;
        // Initially, the video's width and height are zero, so we
        // set the corresponding bits accordingly.
        canRenderState |= CAN_RENDER_ZERO_WIDTH_BIT;
        canRenderState |= CAN_RENDER_ZERO_HEIGHT_BIT;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>AnchorNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Anchor</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new VideoElement(doc);
    }

    /**
     * Initializes the video element.
     */
    void init() throws Exception {
        String url = getHref();
        
        if (url == null)
            throw new Exception ("media locator not set");
        
        if (videoPlayer == null) {
            videoPlayer = MediaSupport.getVideoPlayer(url);
        }
    }
    
    /**
     * Plays the video.
     *
     * @param startTime  The start time in nanoseconds.
     */
    void play(long startTime) {
        if (videoPlayer != null) {
            videoPlayer.play(startTime);
        }
    }
    
    /**
     * Stops the video player.
     */
    void stop() {
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
    }
    
    /**
     * Closes the video player.
     */
    void close() {
	videoPlayer.close();
        videoPlayer = null;
    }   
    
    /**
     * Set the volume level using a floating point scale with values 
     * between 0.0 and 1.0. 0.0 is silence; 1.0 is the loudest useful 
     * level that this GainControl supports.
     */
    void setVolume(float volume) {
        if (videoPlayer != null) {
            videoPlayer.setVolume(volume);
        }
    }

    /**
     * Sets the x-coordinate of the viewport.
     * @param x The x-coordinate.
     */
    public void setX(final float newX) {
        if (newX == x) {
            return;
        }

        modifyingNode();
        x = newX;
        modifiedNode();
    }

    /**
     * @return The x-coordinate of the viewport.
     */
    public float getX() {
        return x;
    }

    /**
     * Sets the y-coordinate of the viewport.
     * @param newY The y-coordinate.
     */
    public void setY(final float newY) {
        if (newY == y) {
            return;
        }

        modifyingNode();
        y = newY;
        modifiedNode();
    }

    /**
     * @return The Y-coordinate of the viewport.
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the width of the viewport.
     * @param width The width.
     */
    void setWidth(final float newWidth) {

        if (newWidth < 0) {
            throw new IllegalArgumentException();
        }

        if (newWidth == width) {
            return;
        }

        modifyingNode();
        width = newWidth;
        computeCanRenderWidthBit(width);
	modifiedNode();
    }

    /**
     * @return The width of the viewport.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets the height of the viewport.
     * @param height The height.
     */
    public void setHeight(final float newHeight) {

        if (newHeight < 0) {
            throw new IllegalArgumentException();
        }

        if (newHeight == height) {
            return;
        }

        modifyingNode();
        height = newHeight;
        computeCanRenderHeightBit(height);
	modifiedNode();
    }

    /**
     * @return The height of the viewport.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the overlay format of the viewport.
     * @param overlayFormat The new overlay format: <code>top</code> or
     *     <code>none</code>.
     */
    public void setOverlay(final String overlayFormat) {

        if (overlayFormat == null) {
            throw new IllegalArgumentException();
        }

        if (overlayFormat.equals(overlay)) {
            return;
        }

        if (SVGConstants.SVG_TOP_VALUE.equals(overlayFormat)
            ||
            SVGConstants.SVG_NONE_VALUE.equals(overlayFormat)) {

            modifyingNode();
            overlay = overlayFormat;
            modifiedNode();

        } else {
            throw new IllegalArgumentException("Unknown overlay format: " +
                overlayFormat);
        }
    }

    /**
     * @return The overlay format of the viewport.
     */
    public String getOverlay() {
        return overlay;
    }

    /**
     * Sets the transform behavior of the viewport.
     * @param behavior The new transform behavior: <code>geometric</code>,
     * <code>pinned</code>, <code>pinned90</code>, <code>pinned180</code>
     * or <code>pinned270</code>.
     */
    public void setTransformBehavior(final String behavior) {

        if (behavior == null) {
            throw new IllegalArgumentException();
        }

        if (behavior.equals(transformBehavior)) {
            return;
        }

        if (SVGConstants.SVG_GEOMETRIC_VALUE.equals(behavior)
            ||
            SVGConstants.SVG_PINNED_VALUE.equals(behavior)
            ||
            SVGConstants.SVG_PINNED90_VALUE.equals(behavior)
            ||
            SVGConstants.SVG_PINNED180_VALUE.equals(behavior)
            ||
            SVGConstants.SVG_PINNED270_VALUE.equals(behavior)) {

            modifyingNode();
            transformBehavior = behavior;
            modifiedNode();

        } else {
            throw new IllegalArgumentException("Unknown transform behavior: " +
                behavior);
        }
    }

    /**
     * @return The transform behavior of the viewport.
     */
    public String getTransformBehavior() {
        return transformBehavior;
    }

    /**
     * Sets the initial visibility of the viewport.
     * @param visibility The new initial visibility:
     * <code>whenStarted</code> or <code>always</code>.
     */
    public void setInitialVisibility(final String visibility) {

        if (visibility == null) {
            throw new IllegalArgumentException();
        }

        if (visibility.equals(initialVisibility)) {
            return;
        }

        if (SVGConstants.SVG_WHEN_STARTED_VALUE.equals(visibility)
            ||
            SVGConstants.SVG_ALWAYS_VALUE.equals(visibility)) {

            modifyingNode();
            initialVisibility = visibility;
            modifiedNode();

        } else {
            throw new IllegalArgumentException("Unknown initial visibility: " +
                visibility);
        }
    }

    /**
     * @return The initial visibility of the viewport.
     */
    public String getInitialVisibility() {
        return initialVisibility;
    }

    /**
     * Sets the focusable state of this element.
     * @param focusable The new "focusable" state: <code>true</code>,
     *     <code>false</code> or <code>auto</code>.
     */
    public void setFocusable(final String newFocusable) {

        if (newFocusable == null) {
            throw new IllegalArgumentException();
        }

        if (newFocusable.equals(focusable)) {
            return;
        }

        if (SVGConstants.SVG_TRUE_VALUE.equals(newFocusable)
            ||
            SVGConstants.SVG_FALSE_VALUE.equals(newFocusable)
            ||
            SVGConstants.SVG_AUTO_VALUE.equals(newFocusable)) {

            modifyingNode();
            focusable = newFocusable;
            modifiedNode();

        } else {
            throw new IllegalArgumentException("Unknown focusable: " +
                newFocusable);
        }
    }

    /**
     * @return The focusable state of the element.
     */
    public String getFocusable() {
        return focusable;
    }

    /**
     * Returns the value of the given Object-valued property.
     *
     * @return the value of the given Object-valued property.
     */
    protected Object getPropertyState(final int propertyIndex) {
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
     * @param newViewportFillOpacity the new viewport-fill-opacity property
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
     * @param newTransform this node's new transform. Note that the
     *        input value is used by reference.
     */
    public void setTransform(final Transform newTransform) {
        if (equal(newTransform, transform)) {
            return;
        }

        modifyingNode();
        this.transform = newTransform;
        recomputeTransformState();
        recomputeProxyTransformState();
        modifiedNode();
    }

    /**
     * @return this node's transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Recomputes the transform cache, if one exists. This should recursively
     * call recomputeTransformState on children node or expanded content, if
     * any.
     *
     * By default, because a ModelNode has no transform and no cached transform,
     * this only does a pass down.
     *
     * @param parentTransform the Transform applied to this node's parent.
     */
    protected void recomputeTransformState(final Transform parentTransform) {
        txf = appendTransform(parentTransform, txf);
        computeCanRenderTransformBit(txf);
        inverseTxf = null;
        // inverseTxf = computeInverseTransform(txf, parentTransform, 
        //                                      inverseTxf);
        recomputeTransformState(txf, getFirstChildNode());
    }

    /**
     * @return this node's cached transform. 
     */
    public Transform getTransformState() {
        return txf;
    }

    /**
     * Appends this node's transform, if it is not null.
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
        if (transform == null) {
            return tx;
        } 

        tx = recycleTransform(tx, workTx);

        if (transform != null) {
            tx.mMultiply(transform);
        }

        return tx;    
    }

    /**
     * @return this node's cached inverse transform. 
     */
    Transform getInverseTransformState() {
        if (((canRenderState & CAN_RENDER_NON_INVERTIBLE_TXF_BIT) == 0)) {
            if (inverseTxf == null) {
                // If there is a parent, check if this node's transform is the 
                // same as the parent's in which cahse
                if (parent != null && txf == parent.getTransformState()) {
                    inverseTxf = parent.getInverseTransformState();
                } else {
                    inverseTxf = new Transform(null);
                    try {
                        inverseTxf = (Transform) txf.inverse(inverseTxf);
                    } catch (Exception e) {
                        // If we get an exception, then we have a real error
                        // condition, because we just checked that the 
                        // transform was invertible.
                        throw new Error();
                    }
                }
            }
        } else {
            inverseTxf = null;
        }
        return inverseTxf;
    }

    /**
     * VideoElement supports the following traits: x, y, width, height,
     * transform behavior, overlay, initial visibility, focusable, transform,
     * viewport-fill and viewport-fill-opacity.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if ((SVGConstants.SVG_X_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_Y_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_TRANSFORM_BEHAVIOR_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_OVERLAY_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_INITIAL_VISIBILITY_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_FOCUSABLE_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName)) {
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
        if ((SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name)
             &&
             isInherited(PROPERTY_VIEWPORT_FILL)
            ||
            ((SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name)
             &&
             isInherited(PROPERTY_VIEWPORT_FILL_OPACITY))) {
            return SVGConstants.CSS_INHERIT_VALUE;
        } else {
            return super.getSpecifiedTraitImpl(name);
        }
    }


    /**
     * VideoElement handles the x, y, width, height, transform behavior,
     * overlay, initial visibility, focusable, transform, viewport-fill and
     * viewport-fill-opacity traits. Other attributes are handled by the
     * super class.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return Float.toString(x);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return Float.toString(y);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return Float.toString(width);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(height);
        } else if (SVGConstants.SVG_TRANSFORM_BEHAVIOR_ATTRIBUTE == name) {
            return transformBehavior;
        } else if (SVGConstants.SVG_OVERLAY_ATTRIBUTE == name) {
            return overlay;
        } else if (SVGConstants.SVG_INITIAL_VISIBILITY_ATTRIBUTE == name) {
            return initialVisibility;
        } else if (SVGConstants.SVG_FOCUSABLE_ATTRIBUTE == name) {
            return focusable;
	} else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            return toStringTrait(transform);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            return toString(getViewportFill());
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getViewportFillOpacity());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * VideoElement handles the x, y, width, height and viewport-fill-opacity
     * float trait. Other attributes are handled by the super class.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return getX();
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return getY();
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
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
     * VideoElement handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name matrix trait name.
     * @return the trait value corresponding to name as SVGMatrix.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGMatrix SVGMatrix}
     */
    SVGMatrix getMatrixTraitImpl(final String name)throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            return toSVGMatrixTrait(transform);
        } else {
            return super.getMatrixTraitImpl(name);
        }
    }

    /**
     * VideoElement handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name name of trait to set
     * @param matrix Transform value of trait
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGMatrix
     * SVGMatrix}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    void setMatrixTraitImpl(final String name, 
                            final Transform matrix) throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            setTransform(matrix);
        } else {
            super.setMatrixTraitImpl(name, matrix);
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
        if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            return toSVGRGBColor(SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE,
                                 getViewportFill());
        } else {
            return super.getRGBColorTraitImpl(name);
        }
    }

    /**
     * These traits can be created: x, y, width, height, viewport-fill,
     * viewport-fill-opacity and transform. Other attributes are handled by the
     * super-class.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if ((SVGConstants.SVG_X_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_Y_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName)
	    ||
            (SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName)) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName) {
           return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RGB_COLOR); 
        } else if (SVGConstants.SVG_FOCUSABLE_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);

	} else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName) {
            return new TransformTraitAnim(this, traitName);
	} else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float for x, y, width, height, viewport-fill,
     * viewport-fill-opacity and transform.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            setX(value[0][0]);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            setY(value[0][0]);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            setWidth(value[0][0]);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            setHeight(value[0][0]);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            setViewportFill(toRGB(name, value));
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            setViewportFillOpacity(value[0][0]);
	} else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            if (transform == null) {
                modifyingNode();
                transform = new Transform(value[0][0],
                                          value[1][0],
                                          value[2][0],
                                          value[3][0],
                                          value[4][0],
                                          value[5][0]);
            } else {
                if (!transform.equals(value)) {
                    modifyingNode();
                    transform.setTransform(value[0][0],
                                           value[1][0],
                                           value[2][0],
                                           value[3][0],
                                           value[4][0],
                                           value[5][0]);
                } else {
                    return;
                }
            }
            recomputeTransformState();
            recomputeProxyTransformState();
            modifiedNode();
        } else {
            super.setFloatArrayTrait(name, value);
        }
    }

    /**
     * Validates the input trait value for the viewport-fill,
     * viewport-fill-opacity and transform attributes.
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
        if ((SVGConstants.SVG_X_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_Y_ATTRIBUTE == traitName)) {
            return new float[][] {{parseFloatTrait(traitName, value)}};
        } else if ((SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName)
                   ||
                   (SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName)) {
            return new float[][] {{parsePositiveFloatTrait(traitName, value)}};
	} else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName) {
            Transform txf = parseTransformTrait(traitName, value);
            return new float[][] {{(float) txf.getComponent(0)},
                                  {(float) txf.getComponent(1)},
                                  {(float) txf.getComponent(2)},
                                  {(float) txf.getComponent(3)},
                                  {(float) txf.getComponent(4)},
                                  {(float) txf.getComponent(5)}};
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
     * Validates the input trait value for x, y, width, height, focusable,
     * viewport-fill and viewport-fill-opacity.
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

        if ((SVGConstants.SVG_X_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_Y_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName)) {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_FLOAT);
        }

        if (SVGConstants.SVG_FOCUSABLE_ATTRIBUTE == traitName) {
            if (SVGConstants.SVG_TRUE_VALUE.equals(value)
                ||
                SVGConstants.SVG_FALSE_VALUE.equals(value)
                ||
                SVGConstants.SVG_AUTO_VALUE.equals(value)) {
                return value;
            }
            throw illegalTraitValue(traitName, value);
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
     * VideoElement handles the x, y, width, height, transform behavior,
     * overlay, initial visibility, focusable, transform, viewport-fill and
     * viewport-fill-opacity traits.
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

        try {

            // ============== x, y, width and height ============== //

            if (SVGConstants.SVG_X_ATTRIBUTE == name) {
                setX(parseFloatTrait(name, value));
            } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
                setY(parseFloatTrait(name, value));
            } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
                setWidth(parsePositiveFloatTrait(name, value));
            } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
                setHeight(parsePositiveFloatTrait(name, value));
            } else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
                setTransform(parseTransformTrait(name, value));

            // ============= transform behavior ================= //
            } else if (SVGConstants.SVG_TRANSFORM_BEHAVIOR_ATTRIBUTE == name) {
                // transformBehavior is not animatable.
                setTransformBehavior(value);

            // =================== overlay ====================== //
            } else if (SVGConstants.SVG_OVERLAY_ATTRIBUTE == name) {
                // overlay is not animatable.
                setOverlay(value);

            // ============= initial visibility ================= //
            } else if (SVGConstants.SVG_INITIAL_VISIBILITY_ATTRIBUTE == name) {
                // initialVisibility is not animatable.
                setInitialVisibility(value);

            // ================== focusable ===================== //
	    } else if (SVGConstants.SVG_FOCUSABLE_ATTRIBUTE == name) {
                setFocusable(value);

            } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {

                // ================= viewport-fill ==================== //

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

                // ============== viewport-fill-opacity =============== //

                if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                    setFloatInherited(PROPERTY_VIEWPORT_FILL_OPACITY, true);
                } else {
                    setViewportFillOpacity(parseFloatTrait(name, value));
                }
            } else {
                super.setTraitImpl(name, value);
            }

        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * VideoElement handles the viewport-fill-opacity float trait.
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
            if (SVGConstants.SVG_X_ATTRIBUTE == name) {
                setX(value);
            } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
                setY(value);
            } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
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
        if ((SVGConstants.SVG_X_ATTRIBUTE == name)
            ||
            (SVGConstants.SVG_Y_ATTRIBUTE == name)
            ||
            (SVGConstants.SVG_WIDTH_ATTRIBUTE == name)
            ||
            (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name)
            ||
            (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name)) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
	    // Unlike SVG_FILL_ATTRIBUTE, SVG_VIEWPORT_FILL_ATTRIBUTE can be
	    // null.
	    if (value == null || value.length == 0) {
                return SVGConstants.CSS_NONE_VALUE;
            } else {
                return toRGBString(name, value);
            }
	} else if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            Transform txf = new Transform(value[0][0],
                                          value[1][0],
                                          value[2][0],
                                          value[3][0],
                                          value[4][0],
                                          value[5][0]);
            return toStringTrait(txf);
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
            if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
                setViewportFill((RGB) color);
            } else {
                super.setRGBColorTraitImpl(name, color);
            } 
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                                   iae.getMessage());
        }
    }
    /**
     * @return the tight bounding box in current user coordinate
     * space. Tight bounding box is the smallest possible rectangle that
     * includes the geometry of all contained graphics elements excluding
     * stroke.  The calculation is done in the user coordinate space of the
     * element, i.e. in the coordinate space used for element's drawing, after
     * application of the transform attribute, if any. When bounding box is
     * calculated elements with display property (trait) set to none are
     * ignored. Exact rules for the bounding box calculation are given in the <a
     * href="http://www.w3.org/TR/SVG/coords.html#ObjectBoundingBox">SVG
     * spec</a>.
     */
    public SVGRect getBBox() {
        return null;
    }
    
    /**
     * @param paintType the key provided by the PaintTarget when it subscribed 
     *        to associated PaintServer.
     * @param paintServer the PaintServer generating the update.
     */
    public void onPaintServerUpdate(final String paintType,
                                    final PaintServer paintServer) {
        if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == paintType) {
            setViewportFill(paintServer);
        } else {
            throw new Error();
        }
    }

    /**
     * Paint the current video frame. By default, nothing is painted.
     *
     * @param rg The <code>RenderGraphics</code> context.
     */
    public void paint(final RenderGraphics rg) {

	/*
        if (!gp.getVisibility()) {
            return;
        }
	*/

        if (transform != null) {
            rg.setTransform(this.transform);
        }

	if (viewportFill != null) {
            // Set fill and opacity values to viewport-fill and
            // viewport-fill-opacity values so fillRect() can be used to fill
            // the viewport.
            rg.setFill(viewportFill);
            rg.setFillOpacity(viewportFillOpacity);

            // Fill viewport before rendering the video
	    //
            rg.fillRect(x, y, width, height, 0, 0);
        }

        //
        // Scale the image so that it fits into width/height and is centered.
        //

	if (image == null) {
            return;
        }

        // Show the frame only if there are both a width and a height to render.
        int iw = image.getWidth();
        if (iw == 0) {
            return;
        }
        int ih = image.getHeight();
        if (ih == 0) {
            return;
        }
        rg.drawImage(image, x, y, iw, ih);

	/*
        if (StructureNode.ALIGN_NONE.equals(align)) {
            rg.drawImage(image, x, y, width, height);
        } else { 
            float ws = width / iw;
            float hs = height / ih;
            float is = ws;
            if (hs < ws) {
                is = hs;
            }

            float oh = ih * is;
            float ow = iw * is;
            float dx = (width - ow) / 2;
            float dy = (height - oh) / 2;

            rg.drawImage(image, (x + dx), (y + dy), ow, oh);
        }
	*/
    }

    /**
     * Always returns <code>true</code>, since a <code>VideoElement</code> can
     * be rendered.
     *
     * @return <code>true</code>, always.
     */
    protected boolean hasNodeRendering() {
        return true;
    }

    /**
     * Invoked when a new video frame becomes available.
     */
    void updateFrame() {
        RasterImage img = videoPlayer.getFrame();
        modifyingNode();
	image = img;
	modifiedNode();
    }
}

