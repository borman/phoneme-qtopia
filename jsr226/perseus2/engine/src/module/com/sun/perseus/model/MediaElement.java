/*
 * $RCSfile: MediaElement.java,v $
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
import com.sun.perseus.platform.URLResolver;
import com.sun.perseus.util.SVGConstants;

/**
 * The <code>MediaElement</code> class is used for audio and video
 * support in SVG Tiny 1.2.
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id: MediaElement.java,v 1.4 2005/03/31 13:21:51 vhardy Exp $
 */
public abstract class MediaElement extends TimeAttributesNode 
    implements DecoratedNode {

    /**
     * The media's hyperlink reference, xlink:href attribute
     * Default is an empty String.
     */
    protected String href = "";
    
    /**
     * The media format type attribute
     */
    protected String type = "";
    
    /**
     * The syncBehavior attribute, default is derived from
     * SVG element's syncBehaviorDefault attribute value.
     */
    protected String syncBehavior;
    
    /**
     * The syncTolerance attribute, default is derived from
     * SVG element's syncToleranceDefault attribute value.
     */
    protected Time syncTolerance;

    /**
     * The syncMaster attribute, default value is false
     */
    protected boolean syncMaster = false;
    
    /**
     * The current audio level, audio-level property
     */
    protected float audioLevel = 1f;

    /**
     * Markers are used to keep track of inherited properties, color relative
     * properties and bolder/lighter font weights.
     *
     * 0-22  : property inheritance
     * 23-24 : color relative
     * 24    : is bolder marker
     * 25    : is lighter marker
     */
    protected int markers = ViewportNode.DEFAULT_INHERITANCE; 

    /**
     * xlink:href is required on MediaElements.
     */
    static final String[][] REQUIRED_TRAITS_NS
        = { {SVGConstants.XLINK_NAMESPACE_URI, 
             SVGConstants.SVG_HREF_ATTRIBUTE} };
    
    /**
     * Indicates whether media playback has started.
     */
    private boolean mediaStarted = false;
    
    /**
     * The most recent sample time.
     */
    private long sampleTime;

    /**
     * Builds a new timed element that belongs to the given
     * document. This <code>MediaElement</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @param localName the element's local name
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public MediaElement(final DocumentNode ownerDocument,
                        final String localName) {
        super(ownerDocument, localName);
        
        ModelNode mn = ownerDocument.getFirstChildNode();
        if (mn != null && mn instanceof SVG) {
            SVG svg = (SVG)mn;
            syncBehavior = svg.getSyncBehaviorDefault();
            syncTolerance = svg.getSyncToleranceDefault();
        } else {
            // If the document tree does not have a "svg" element in it, just
            // use the values that we know are defaults in the svg element.
            syncBehavior = SVGConstants.SVG_CAN_SLIP_VALUE;
            syncTolerance = new Time(2000);
        }
    }

    /**
     * This returns the <b>absolute</b> URI, even though
     * the href may have been a relative URI
     *
     * @return this anchor's href, as an absolute URL or
     *         null if the href set was null or if the 
     *         absolute URL could not be computed.
     */    
    String getHref() {
        String uriBase = getURIBase();
        try {
            if (uriBase != null) {
                String url = URLResolver.resolve(uriBase, href);
                return url;
            } else {
                return href;
            }
        } catch (IllegalArgumentException mue) {
            return null;
        }
    }

    /**
     * Sets the media element's resource reference.
     *
     * @param newHref the new media element's href
     */
    void setHref(final String newHref) {
        if (newHref == null) {
            throw new IllegalArgumentException();
        }

        if (equal(newHref, href)) {
            return;
        }

        modifyingNode();
        this.href = newHref;
        modifiedNode();
    }

    /**
     * Returns the current media format type.
     *
     * @param returns the current media format type format.
     */
    String getType() {
        return type;
    }
    
    /**
     * Set the media format type.
     *
     * @param newType the media format type to set
     * @throws IllegalArgumentException if newType is null
     */
     void setType(String newType) {
        if (newType == null) {
            throw new IllegalArgumentException();
        }
        
        if (equal(newType, type)) {
            return;
        }
        
        modifyingNode();
        this.type = newType;
        modifiedNode();
    }
     
    /**
     * Returns the current audio level. Should be between 0 and 1.
     *
     * @return the current audio level, in the [0, 1] range.
     */
    final float getAudioLevel() {
        return audioLevel;
    }

    /**
     * Sets the current audio level. Should be between 0 and 1.
     *
     * @param newAudioLevel the new audio level, in the [0, 1] range.
     */
    final void setAudioLevel(final float newAudioLevel) {
        if (newAudioLevel < 0 || newAudioLevel > 1) {
            throw new IllegalArgumentException();
        }

        if (audioLevel == newAudioLevel) {
            return;
        }

        modifyingNode();
        this.audioLevel = newAudioLevel;
        setVolume(audioLevel);
        modifiedNode();
    }

    /**
     * MediaElement handles the audio-level, type, syncBehavior, syncTolerance
     * and syncMaster traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if ((SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_TYPE_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_SYNC_BEHAVIOR_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_SYNC_TOLERANCE_ATTRIBUTE == traitName)
            ||
            (SVGConstants.SVG_SYNC_MASTER_ATTRIBUTE == traitName)) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Supported traits: xlink:href
     *
     * @param namespaceURI the trait's namespace.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTraitNS(final String namespaceURI,
                            final String traitName) {
        if ((SVGConstants.XLINK_NAMESPACE_URI == namespaceURI)
            &&
            (SVGConstants.SVG_HREF_ATTRIBUTE == traitName)) {
            return true;
        } else {
            return super.supportsTraitNS(namespaceURI, traitName);
        }
    }

    /**
     * The audio-level trait is animatable, so create an appropriate TraitAnim 
     * for it.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * xlink:href is animatable, so create an appropriate TraitAnim for it.
     *
     * @param traitName the trait name.
     * @param traitNamespace the trait's namespace. Should not be null.
     */
    TraitAnim createTraitAnimNSImpl(final String traitNamespace, 
                                    final String traitName) {
        if ((traitNamespace == SVGConstants.XLINK_NAMESPACE_URI)
            &&
            (traitName == SVGConstants.SVG_HREF_ATTRIBUTE)) {
            return new StringTraitAnim(this, traitNamespace, traitName);
        }

        return super.createTraitAnimNSImpl(traitNamespace, traitName);
    }

    /**
     * MediaElement handles the audio-level, type, syncBehavior, syncTolerance 
     * and syncMaster traits.
     *
     * @param name the requested trait name
     * @return the requested trait's value.
     * 
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the 
     * requested trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            return Float.toString(audioLevel);
        } else if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            return type;
        }  else if (SVGConstants.SVG_SYNC_BEHAVIOR_ATTRIBUTE == name) {
            return syncBehavior;
        } else if (SVGConstants.SVG_SYNC_TOLERANCE_ATTRIBUTE == name) {
            return Time.toStringTrait(syncTolerance);
        } else if (SVGConstants.SVG_SYNC_MASTER_ATTRIBUTE == name) {
            return String.valueOf(syncMaster);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * MediaElement handles the audio-level, type, syncBehavior, syncTolerance 
     * and syncMaster traits.
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the
     * requested trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, 
                             final String value)
        throws DOMException {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setAudioLevel(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            // type is not animatable
            setType(value);
        }  else if (SVGConstants.SVG_SYNC_BEHAVIOR_ATTRIBUTE == name) {
            // syncBehavior is not animatable
            if (SVGConstants.SVG_CAN_SLIP_VALUE.equals(value) ||
                SVGConstants.SVG_INDEPENDENT_VALUE.equals(value) ||
                SVGConstants.SVG_LOCKED_VALUE.equals(value)) {
                syncBehavior = value;
            } else if (SVGConstants.XML_DEFAULT_VALUE.equals(value)) {
                ModelNode mn = getOwnerDocument().getFirstChildNode();
                if (mn != null && mn instanceof SVG) {
                    SVG svg = (SVG)mn;
                    syncBehavior = svg.getSyncBehaviorDefault();
                } else {
                    // If the document tree does not contain a "svg" element
                    // that we can get the "syncBehaviorDefault" value from,
                    // use the default that we know is used for 
                    // "syncBehaviorDefault" in the "svg" element.
                    syncBehavior = SVGConstants.SVG_CAN_SLIP_VALUE;
                }
            } else {
                throw illegalTraitValue(name, value);
            }
        }  else if (SVGConstants.SVG_SYNC_TOLERANCE_ATTRIBUTE == name) {
            // syncTolerance is not animatable
            if (value.equals(SVGConstants.XML_DEFAULT_VALUE)) {
                ModelNode mn = getOwnerDocument().getFirstChildNode();
                if (mn != null && mn instanceof SVG) {
                    SVG svg = (SVG)mn;
                    syncTolerance = svg.getSyncToleranceDefault();
                } else {
                    // If the document tree does not contain a "svg" element
                    // that we can get the "syncToleranceDefault" value from,
                    // use the default that we know is used for 
                    // "synToleranceDefault" in the "svg" element.
                    syncTolerance = new Time(2000);
                }
            } else {
                syncTolerance = parseClockTrait(name, value);
            }
        } else if (SVGConstants.SVG_SYNC_MASTER_ATTRIBUTE == name) {
            // syncMaster is not animatable
            if (SVGConstants.SVG_TRUE_VALUE.equals(value)) {
                syncMaster = true;
            } else if (SVGConstants.SVG_FALSE_VALUE.equals(value)) {
                syncMaster = false;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * MediaElement handles the xlink:href attribute
     *
     * @param namespaceURI the URI for the requested trait.
     * @param name the requested trait's local name (i.e., un-prefixed).
     * 
     * @return the requested trait's value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    String getTraitNSImpl(final String namespaceURI, 
                          final String name)
        throws DOMException {
        if ((SVGConstants.XLINK_NAMESPACE_URI == namespaceURI)
            &&
            (SVGConstants.SVG_HREF_ATTRIBUTE == name)) {
            return href;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * MediaElement supports the xlink:href trait.
     *
     * @param namespaceURI the URI for the trait's namespace.
     * @param name the trait's local name (i.e., un-prefixed).
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the
     * requested trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setTraitNSImpl(final String namespaceURI, 
                               final String name, 
                               final String value)
        throws DOMException {
        try {
            if ((SVGConstants.XLINK_NAMESPACE_URI == namespaceURI)
                &&
                (SVGConstants.SVG_HREF_ATTRIBUTE == name)) {
                if (value == null) {
                    throw illegalTraitValue(name, value);
                }
                setHref(value);
            } else {
                super.setTraitNSImpl(namespaceURI, name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   iae.getMessage());
        }
    }

    /**
     * @param name the requested trait name (e.g., "audio-level")
     * @return the requested trait value
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
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            return audioLevel;
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * Set the trait value as float.  
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the 
     * requested trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            setAudioLevel(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            return type;
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Validates the input trait value.
     *
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(final String traitName,
                                             final String value,
                                             final String reqNamespaceURI,
                                             final String reqLocalName,
                                             final String reqTraitNamespace,
                                             final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == traitName) {
            float v = parseFloatTrait(traitName, value);
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
        if (SVGConstants.SVG_AUDIO_LEVEL_ATTRIBUTE == name) {
            setAudioLevel(value[0][0]);
        } else {
            super.setFloatArrayTrait(name, value);
        }
    }

    /**
     * Event dispatching override, to capture begin and end events.
     *
     * When a begin event occurs, the audio is started. When the end event is
     * fired, the audio is stopped. When a restart event is dispatched, the 
     * audio is restarted.
     *
     * When an end event occurs, the animation removes itself from the target
     * element and trait's TraitAnim if the animation is not in the frozen
     * state.
     *
     * @param  evt the event that occured
     */
    public void dispatchEvent(final ModelEvent evt) {
        super.dispatchEvent(evt);

        if (TimedElementSupport.BEGIN_EVENT_TYPE.equals(evt.getType())) {
            ownerDocument.activeMediaElements.addElement(this);            
        } else if (TimedElementSupport.REPEAT_EVENT_TYPE.equals(evt.getType())) {
            ownerDocument.activeMediaElements.addElement(this);
        } else if (TimedElementSupport.END_EVENT_TYPE.equals(evt.getType())) {
            endMedia();
            ownerDocument.activeMediaElements.removeElement(this);
        } else {
        }
    }

    /**
     * Clears the input marker.
     *
     * @param marker the marker to clear.
     */
    void clearMarker(final int marker) {
        markers &= ~marker;
    }
    
    /**
     * @return true if the input marker is set.
     */
    final boolean isMarkerSet(final int marker) {
        return (markers & marker) != 0;
    }

    /**
     * Sets the input marker.
     *
     * @param marker the marker to set.
     */
    void setMarker(final int marker) {
        markers |= marker;
    }

    /** 
     * Check if the property is inherited.
     *
     * @param propertyIndex the index of the property whose 
     *        inherit status is checked.
     * @return true if the input property is inherited. False
     *         otherwise
     */
    public boolean isInherited(int propertyIndex) {
         return isMarkerSet(propertyIndex);
    }

    /**
     * Sets the input property's inheritance status
     * @param propertyIndex the index of the property whose inherit
     *        status is set
     * @param inherit the new inherit status for the property at
     *        index propertyIndex.
     */
    public void setInherited(int propertyIndex, boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            Object inheritedValue = getInheritedPropertyState(propertyIndex);
            setPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagatePropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * Implementation. Sets the input property's inheritance status,
     * but does not send modification events.
     *
     * @param propertyIndex the index for the property whose inherited state 
     *        is set
     * @param inherit the new property's state
     */
    protected void setInheritedQuiet(final int propertyIndex, 
                                     final boolean inherit) {
        if (inherit) {
            setMarker(propertyIndex);
        } else {
            clearMarker(propertyIndex);
        }
    }

    /**
     * Sets the input float property's inheritance status
     * @param propertyIndex the index of the property whose inherit
     *        status is set
     * @param inherit the new inherit status for the property at
     *        index propertyIndex.
     */
    public void setFloatInherited(int propertyIndex, boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            float inheritedValue = 
                    getInheritedFloatPropertyState(propertyIndex);
            setFloatPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagateFloatPropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * Sets the input packed property's inheritance status
     * @param propertyIndex the index of the property whose inherit
     *        status is set
     * @param inherit the new inherit status for the property at
     *        index propertyIndex.
     */
    public void setPackedInherited(int propertyIndex, boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            int inheritedValue = getInheritedPackedPropertyState(propertyIndex);
            setPackedPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagatePackedPropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * @return the number of properties on this node
     * Note: This method will have to be updated when new properties are
     * supported on this element.
     */
    public int getNumberOfProperties() {
        return ViewportNode.NUMBER_OF_PROPERTIES;
    }

    /*
     * Note: The method recomputePackedPropertyState() will have to be implemented
     * if a packed property is to be added to MediaElement or any of its sub-classes.
     */

    /**
     * Recomputes the given Object-valued property's state given the
     * new parent property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!isInherited(propertyIndex) 
            || 
            isPropertyState(propertyIndex, parentPropertyValue)) {
            // If the property is color relative, the propagation happens
            // through the color property changes.  This means that with
            // currentColor, we inherit the computed value, not the specified
            // currentColor indirection.
            return;
        }

        setPropertyState(propertyIndex, parentPropertyValue);
        propagatePropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes the given float-valued property's state given the new parent 
     * property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputeFloatPropertyState(final int propertyIndex,
                                          final float parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!isInherited(propertyIndex) 
            || 
            isFloatPropertyState(propertyIndex, parentPropertyValue)) {
            // If the property is color relative, the propagation happens
            // through the color property changes.  This means that with
            // currentColor, we inherit the computed value, not the specified
            // currentColor indirection.
            return;
        }

        setFloatPropertyState(propertyIndex, parentPropertyValue);
        propagateFloatPropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Starts the underlying media player and handles repetitions if the sample
     * time wraps around.
     */
    public void playMedia() {
        if (!mediaStarted) {
            try {
                init();
                play(timedElementSupport.lastSampleTime);
            } catch (Exception e) {
                System.err.println("Cannot initialize media player");
            }            

            mediaStarted = true;
        } else {
            // check for repeat            
            if (timedElementSupport.lastSampleTime < sampleTime) {
                play(timedElementSupport.lastSampleTime);
            }
        }
 
        if (mediaStarted) {
            updateFrame();
        }
        
        sampleTime = timedElementSupport.lastSampleTime;
   }
    
    /**
     * Stops the underlying media player.
     */
    public void endMedia() {        
        if (mediaStarted) {
            stop();
            mediaStarted = false;
        }
    }
    
    /**
     * When a MediaElement is unhooked from the document tree, it should stop
     * playing if it is currently playing media. In addition it should remove 
     * itself from its ownerDocument's activeMediaElements Vector. 
     */
    void nodeUnhookedFromDocumentTree() {
        super.nodeUnhookedFromDocumentTree();
        
        // If media is playing, stop it
        endMedia();
        
        // Remove this MediaElement from ownerDocument's list of 
        // activeMediaElements
        ownerDocument.activeMediaElements.removeElement(this);
    }
    
    /**
     * Initializes the media player.
     *
     * @throws Exception if the initialization fails.
     */
    abstract void init() throws Exception;
    
    /**
     * Starts the media player.
     *
     * @param startTime  The start time in milliseconds.
     */
    abstract void play(long startTime);

    /**
     * Stops the media player.
     */
    abstract void stop();
    
    /**
     * Closes the media player.
     */
    abstract void close();

    /**
     * Updates the video frame.
     */    
    abstract void updateFrame();
    
    /**
     * Set the volume level using a floating point scale with values between 0.0
     * and 1.0. 0.0 is silence; 1.0 is the loudest useful level that this
     * GainControl supports.
     *
     * Should be used by implementations to set the volume on the 
     * associated player abstractions, for example.
     *
     * @param volume the volume level to apply to the element.
     */
    abstract void setVolume(float volume);
}

