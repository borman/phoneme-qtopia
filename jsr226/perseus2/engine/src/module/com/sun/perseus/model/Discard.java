/*
 * $RCSfile$
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
import org.w3c.dom.Node;
import com.sun.perseus.util.SVGConstants;

/**
 * <code>Discard</code> represents an SVG Tiny <code>&lt;discard&gt;</code>
 * element.
 */
public class Discard extends TimedElementNode implements IDRef {

    /**
     * This node's target element.
     */
    protected ElementNode targetElement;

    /**
     * The identifier of the target element.
     */
    protected String idRef;

    /**
     * Constructor for Discard
     *
     * @param doc the owner DocumentNode
     */
    public Discard(final DocumentNode doc) {
        super(doc, SVGConstants.SVG_DISCARD_TAG);
        
        // If the document is _not_ loaded (i.e., we are at parse time),
        // add this TimedElementNode to the document's timedElementNodes 
        // vector for validation at the end of the loading phase.
        if (!ownerDocument.loaded) {
            ownerDocument.timedElementNodes.addElement(this);
        }
    }
    
    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Discard</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimedElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Discard(doc);
    }
    
    /**
     * @return this element's idRef.
     */
    public String getIdRef() {
        return idRef;
    }
    
    /**
     * <code>IDRef</code> implementation.
     *
     * @param ref the resolved reference (mapped from the
     *        id passed to the setIdRef method).
     */
    public void resolveTo(final ElementNode ref) {
        targetElement = ref;
    }

    /**
     * Sets this node's targetElement's idRef
     *
     * @param idRef the identifier of the this node's target element.
     *        Should not be null
     */
    public void setIdRef(final String idRef) {
        this.idRef = idRef;
        ownerDocument.resolveIDRef(this, idRef);
    }

    /**
     * Discard supports the begin attribute.
     * 
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods (such as <code>getTrait</code> or 
     *         <code>setFloatTrait</code>.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == traitName) {
            return true;
        }
        return super.supportsTrait(traitName);
    }

    /**
     * Discard supports the begin attribute.
     * 
     * Returns the trait value as String. In SVG Tiny only certain traits can be
     * obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly equivalent
     * to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} with
     * namespaceURI set to null.
     * 
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     * 
     * 
     * @param name the requested trait name.
     * @return the trait value.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name) throws DOMException {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == name) {
            if (timedElementSupport.beginConditions.size() == 0) {
                return "0s";
            }
            return TimeCondition.toStringTrait(timedElementSupport.beginConditions);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Discard supports the begin attribute.
     * 
     * @param name the trait's name.
     * @param value the trait's value.
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value) throws DOMException {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == name) {
            checkWriteLoading(name);
            timedElementSupport.beginConditions.removeAllElements();
            parseTimeConditionsTrait(name, value, true);
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Discard handles the xlink href attribute
     * 
     * @param namespaceURI the requested trait's namespace URI.
     * @param name the requested trait's local name (i.e., un-prefixed, as 
     *        "href")
     * @return the requested trait's string value.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public String getTraitNSImpl(String namespaceURI, String name) throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
             && SVGConstants.SVG_HREF_ATTRIBUTE.equals(name)) {
            if (idRef == null) {
                return "";
            } 
            return "#" + idRef;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * Discard supports the xlink:href trait.
     * 
     * @param namespaceURI the trait's namespace.
     * @param name the trait's local name (un-prefixed, e.g., "href");
     * @param value the new trait value (e.g., "http://www.sun.com/mypng.png")
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setTraitNSImpl(final String namespaceURI, final String name, final String value) throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
             && SVGConstants.SVG_HREF_ATTRIBUTE.equals(name)) {
            if (value == null || !value.startsWith("#")) {
                throw illegalTraitValue(name, value);
            }
            setIdRef(value.substring(1));
        } else {
            super.setTraitNSImpl(namespaceURI, name, value);
        }
    }

    /**
     * Supported NS traits: xlink:href
     *
     * @param namespaceURI the trait's namespace.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    protected boolean supportsTraitNS(final String namespaceURI, final String traitName) {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
             && SVGConstants.SVG_HREF_ATTRIBUTE.equals(traitName)) {
            return true;
        } else {
            return super.supportsTraitNS(namespaceURI, traitName);
        }
    }

    /**
     * Validating a Discard consists of setting its target
     * element. If there was no idRef, then targetElement is still null
     * and will be set to the parent node.
     */
    void validate() throws DOMException {
        // Set the target element.
        if (targetElement == null) {
            targetElement = (ElementNode) parent;
        }
    }

    /**
     * When a Discard is hooked into the document tree, it needs
     * to validate (only if the Document is loaded).
     */
    final void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        if (ownerDocument.loaded) {
            validate();
        }
    }
    
    /**
     * Method to perform any necessary cleanup when a Discard element is 
     * unhooked from the tree. This can include making sure that its 
     * targetElement and itself is removed from the DocumentNode's 
     * elementsToDiscard list.
     */
    final void nodeUnhookedFromDocumentTree() {
        super.nodeUnhookedFromDocumentTree();
        
        // The discard element functioning adds the targetElement and the
        // discard itself to the ownerDocument's elementsToDiscard Vector. Thus
        // when a discard is unhooked from the tree, in the unhooking cleanup,
        // both the targetElement as well as the discard element should be 
        // removed from the elementsToDiscard Vector, so that no attempt is 
        // made to remove them from the tree after the unhooking is complete.
        // Note that we should do this only in the case where the unhook from
        // the tree is caused not by the functioning of the discard itself but
        // from some other circumstance such as the user calling removeChild()
        // or adoptNode() on the discard or one of its ancestors. 
        // 
        // However if the nodeUnhookedFromDocumentTree() is being called as a 
        // result of discard functioning, then we do not need to do the 
        // unhooking related cleanup mentioned above. This is due to the fact
        // Document.applyAnimations() will perform the actual discard, and
        // after removing the targetElement and discard from the tree, will
        // also remove them from the ownerDocument.elementsToDiscard Vector. So
        // no cleanup of this Vector is required here.
        // 
        // To differentiate the two cases, we use the applyAnimationsCalled
        // flag, which is set when DocumentNode.applyAnimations() is called. 
        // If this flag is set to true, we know that
        // nodeUnhookedFromDocumentTree() is being called from the discard
        // being applied in ownerDocument.applyAnimations(), so no cleanup is
        // required.
        //
        // It should also be noted that during DocumentNode.applyAnimations(),
        // no other piece of code can be manipulating the tree, since this
        // method is called in the update thread and any document tree 
        // manipulation is only allowed in the update thread. No listener code
        // (which would be user code that could manipulate the tree) is called
        // during DocumentNode.applyAnimations() which is why the 
        // elementsToDiscard Vector will not change once this method starts
        // executing. However listeners can be called during 
        // DocumentNode.sample(), as being/end/repeat events are dispatched
        // during this call. 
        if (!ownerDocument.applyAnimationsCalled) {
            // Remove the targetElement from the ownerDocument's 
            // elementsToDiscard Vector
            ownerDocument.elementsToDiscard.removeElement(targetElement);
            ownerDocument.elementsToDiscard.removeElement(this);
        }
    }
    
    /**
     * Event dispatching override, to capture begin event.
     *
     * When a begin event occurs, the target element is added to a list of
     * elements to discard.
     *
     * @param  evt the event that occured
     */
    public void dispatchEvent(final ModelEvent evt) {
        super.dispatchEvent(evt);

        if (targetElement != null) {
            if (TimedElementSupport.BEGIN_EVENT_TYPE.equals(evt.getType())
                ||
                TimedElementSupport.SEEK_BEGIN_EVENT_TYPE.equals(
                                                             evt.getType())) {
                // Add the targetElement to the list of elements to be
                // discarded
                ownerDocument.elementsToDiscard.addElement(targetElement);
            }
        }
        
        // Remove the discard element itself
        ownerDocument.elementsToDiscard.addElement(this);
    }
}
