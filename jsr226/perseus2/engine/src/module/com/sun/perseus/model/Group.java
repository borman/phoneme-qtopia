/*
 * $RCSfile: Group.java,v $
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

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGRGBColor;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGMatrix;

import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.ViewportProperties;

/**
 * A <code>Group</code> corresponds to an SVT Tiny <code>&lt;g&gt;</code>
 * element.
 *
 * @version $Id: Group.java,v 1.10 2006/06/29 10:47:32 ln156897 Exp $
 */
public class Group extends StructureNode 
    implements Transformable, ViewportNode, PaintTarget {
    /**
     * The Transform applied to this node. 
     */
    protected Transform transform;

    /**
     * The motion transform applied to this node. This is typically used for 
     * animateMotion, but it can be used as a regular trait as well.
     */
    protected Transform motion;

    /**
     * The parent space to child space transform
     */
    protected Transform inverseTransform;

    /**
     * The current viewport fill color.
     */
    protected PaintServer viewportFill = INITIAL_VIEWPORT_FILL;

    /**
     * The current viewport fill opacity.
     */
    protected float viewportFillOpacity = INITIAL_VIEWPORT_FILL_OPACITY;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Group(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }


    /**
     * @return the SVGConstants.SVG_GROUP_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_G_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Group</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Group</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Group(doc);
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
     * @param newMotion The new motion transform.
     */
    public void setMotion(final Transform newMotion) {
        if (equal(newMotion, motion)) {
            return;
        }

        modifyingNode();
        this.motion = newMotion;
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
     * @return this node's motion transform
     */
    public Transform getMotion() {
        return motion;
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
     * @param newViewportFillOpacity the new viewport-fill opacity property
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
        if (transform == null && motion == null) {
            return tx;
        } 

        tx = recycleTransform(tx, workTx);
        
        if (motion != null) {
            tx.mMultiply(motion);
        }

        if (transform != null) {
            tx.mMultiply(transform);
        }

        return tx;    
    }

    /**
     * Supported traits: transform.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
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
     * Group handles the transform, motion-pseudo, viewport-fill and
     * viewport-fill-opacity attributes.
     * Other attributes are handled by the super class.
     *
     * @param name the requested trait name.
     * @return the requested trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            return toStringTrait(transform);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            return toStringTrait(motion);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            return toString(getViewportFill());
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getViewportFillOpacity());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Group handles the viewport-fill-opacity float traits.
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
        if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return getViewportFillOpacity();
        } else {
            return super.getFloatTraitImpl(name);
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
     * Group handles the transform, motion-pseudo, viewport-fill and
     * viewport-fill-opacity attributes.
     * Other attributes are handled by the super class.
     *
     * @param name the trait's name.
     * @param value the new trait value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            setTransform(parseTransformTrait(name, value));
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            setMotion(parseTransformTrait(name, value));
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
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Group handles the transform and motion-pseudo attributes.
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
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(name)) {
            return toSVGMatrixTrait(transform);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE.equals(name)) {
            return toSVGMatrixTrait(motion);
        } else {
            return super.getMatrixTraitImpl(name);
        }
    }


    /**
     * Group handles the transform and motion-pseudo attributes.
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
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(name)) {
            setTransform(matrix);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE.equals(name)) {
            setMotion(matrix);
        } else {
            super.setMatrixTraitImpl(name, matrix);
        }
    }

    /**
     * Group handles the viewport-fill-opacity float traits.
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
            if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
                setViewportFillOpacity(value);
            } else {
                super.setFloatTraitImpl(name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, Float.toString(value));
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName) {
            return new TransformTraitAnim(this, traitName);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
            return new MotionTraitAnim(this, traitName);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == traitName) {
           return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RGB_COLOR); 
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == traitName) {
           return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT); 
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float array.
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
        // We use .equals for the transform attribute as the string may not
        // have been interned. We use == for the motion pseudo attribute because
        // it is only used internally and from the SVGConstants strings.
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(name)) {
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
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            if (motion == null) {
                modifyingNode();
                motion = new Transform(value[0][0],
                                       value[1][0],
                                       value[2][0],
                                       value[3][0],
                                       value[4][0],
                                       value[5][0]);
            } else {
                if (!motion.equals(value)) {
                    modifyingNode();
                    motion.setTransform(value[0][0],
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
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
            setViewportFill(toRGB(name, value));
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
     * Validates the input trait value.
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
     * value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
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
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                return new float[0][0];
            } else {
                color = parseColorTrait
                    (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE, value);
            }

            if (color == null) {
                return new float[0][0];
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
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            Transform transform = new Transform(value[0][0],
                                                value[1][0],
                                                value[2][0],
                                                value[3][0],
                                                value[4][0],
                                                value[5][0]);
            return toStringTrait(transform);
        } else if (SVGConstants.SVG_VIEWPORT_FILL_ATTRIBUTE == name) {
	    // Unlike SVG_FILL_ATTRIBUTE, SVG_VIEWPORT_FILL_ATTRIBUTE can be
	    // null.
	    if (value == null || value.length == 0) {
                return SVGConstants.CSS_NONE_VALUE;
            } else {
                return toRGBString(name, value);
            }
        } else if (SVGConstants.SVG_VIEWPORT_FILL_OPACITY_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
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
