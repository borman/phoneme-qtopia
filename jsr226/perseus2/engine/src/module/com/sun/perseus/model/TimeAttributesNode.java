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

import com.sun.perseus.util.SVGConstants;
import org.w3c.dom.DOMException;

/**
 * <code>TimeAttributesNode</code> centralizes functionality common to 
 * many <code>TimedElementNode</code> subclasses such as trait handling
 * for timing attributes.
 */
public class TimeAttributesNode extends TimedElementNode {

    /**
     * Constructor for TimeAttributesNode.
     * 
     * @param ownerDocument the owner DocumentNode
     */
    public TimeAttributesNode(final DocumentNode ownerDocument) {
        super(ownerDocument);

        // If the document is _not_ loaded (i.e., we are at parse time),
        // add this TraitAnimationNode to the document's timedElementNodes 
	// vector for validation at the end of the loading phase.
        if (!ownerDocument.loaded) {
            ownerDocument.timedElementNodes.addElement(this);
        }
    }
    
    /**
     * Builds a TimeAttributesNode that belongs to the given
     * document. This <code>TimeAttributesNode</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     * 
     * @param ownerDocument the document this node belongs to.
     * @param localName the element's local name
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public TimeAttributesNode(final DocumentNode ownerDocument,
                                final String localName) {
        super(ownerDocument, localName);

        // If the document is _not_ loaded (i.e., we are at parse time),
        // add this TraitAnimationNode to the document's timedElementNodes 
	// vector for validation at the end of the loading phase.
        if (!ownerDocument.loaded) {
            ownerDocument.timedElementNodes.addElement(this);
        }
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimeAttributesNode</code>.
     * 
     * 
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimeAttributesNode</code> for the requested 
     * document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new TimeAttributesNode(doc, localName);
    }

    /**
     * TimeAttributesNode supports the begin, end, dur, min, max, restart,
     * repeatCount, repeatDur and fill attributes.
     * 
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods (such as <code>getTrait</code> or 
     *         <code>setFloatTrait</code>.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_BEGIN_ATTRIBUTE == traitName ||
            SVGConstants.SVG_END_ATTRIBUTE == traitName || 
            SVGConstants.SVG_DUR_ATTRIBUTE == traitName || 
            SVGConstants.SVG_MIN_ATTRIBUTE == traitName || 
            SVGConstants.SVG_MAX_ATTRIBUTE == traitName || 
            SVGConstants.SVG_RESTART_ATTRIBUTE == traitName || 
            SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == traitName || 
            SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == traitName || 
            SVGConstants.SVG_FILL_ATTRIBUTE == traitName) {
            return true;
        }
        return super.supportsTrait(traitName);
    }

    /**
     * TimeAttributesNode supports the begin, end, dur, min, max, restart,
     * repeatCount, repeatDur and fill attributes.
     * 
     * Returns the trait value as String. In SVG Tiny only certain traits can 
     * be obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly 
     * equivalent to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} 
     * with namespaceURI set to null.
     * 
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not
     * supported or that it cannot be seen as a String.
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
            return TimeCondition.toStringTrait(
                                          timedElementSupport.beginConditions);
        } else if (SVGConstants.SVG_END_ATTRIBUTE == name) {
	    if (timedElementSupport.endConditions.size() == 0) {
		return SVGConstants.SVG_INDEFINITE_VALUE;
	    }
	    return TimeCondition.toStringTrait(
                                          timedElementSupport.endConditions);
	} else if (SVGConstants.SVG_DUR_ATTRIBUTE == name) {
	    return Time.toStringTrait(timedElementSupport.dur);
	} else if (SVGConstants.SVG_MIN_ATTRIBUTE == name) {
	    return Time.toStringTrait(timedElementSupport.min);
	} else if (SVGConstants.SVG_MAX_ATTRIBUTE == name) {
	    return Time.toStringTrait(timedElementSupport.max);
	} else if (SVGConstants.SVG_RESTART_ATTRIBUTE == name) {
	    switch (timedElementSupport.restart) {
	    case TimedElementSupport.RESTART_ALWAYS:
		return SVGConstants.SVG_ALWAYS_VALUE;
	    case TimedElementSupport.RESTART_WHEN_NOT_ACTIVE:
		return SVGConstants.SVG_WHEN_NOT_ACTIVE_VALUE;
	    case TimedElementSupport.RESTART_NEVER:
		return SVGConstants.SVG_NEVER_VALUE;
	    default:
		throw new IllegalStateException();
	    }
	} else if (SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == name) {
	    if (Float.isNaN(timedElementSupport.repeatCount)) {
		return null;
	    } else if (timedElementSupport.repeatCount == Float.MAX_VALUE) {
		return SVGConstants.SVG_INDEFINITE_VALUE;
	    }
	    return Float.toString(timedElementSupport.repeatCount);
	} else if (SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == name) {
	    return Time.toStringTrait(timedElementSupport.repeatDur);
	} else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
	    switch (timedElementSupport.fillBehavior) {
	    case TimedElementSupport.FILL_BEHAVIOR_REMOVE:		
		return SVGConstants.SVG_REMOVE_VALUE;
	    case TimedElementSupport.FILL_BEHAVIOR_FREEZE:
		return SVGConstants.SVG_FREEZE_VALUE;
	    default:
		throw new IllegalStateException();
	    }
	} else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * TimeAttributesNode supports the begin, end, dur, min, max, restart,
     * repeatCount, repeatDur and fill attributes.
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
        } else if (SVGConstants.SVG_END_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    timedElementSupport.endConditions.removeAllElements();
	    parseTimeConditionsTrait(name, value, false);
	} else if (SVGConstants.SVG_DUR_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
		return;
	    }
	    timedElementSupport.setDur(parseClockTrait(name, value));
	} else if (SVGConstants.SVG_MIN_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
		return;
	    }
	    timedElementSupport.setMin(parseMinMaxClock(name, value, true));
	} else if (SVGConstants.SVG_MAX_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_MEDIA_VALUE.equals(value)) {
		return;
	    }
	    timedElementSupport.setMax(parseMinMaxClock(name, value, false));
	} else if (SVGConstants.SVG_RESTART_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_ALWAYS_VALUE.equals(value)) {
		timedElementSupport.restart = 
		    TimedElementSupport.RESTART_ALWAYS;
	    } else if (SVGConstants.SVG_WHEN_NOT_ACTIVE_VALUE.equals(value)) {
		timedElementSupport.restart = 
		    TimedElementSupport.RESTART_WHEN_NOT_ACTIVE;
	    } else if (SVGConstants.SVG_NEVER_VALUE.equals(value)) {
		timedElementSupport.restart = 
		    TimedElementSupport.RESTART_NEVER;
	    } else {
		throw illegalTraitValue(name, value);
	    }
	} else if (SVGConstants.SVG_REPEAT_COUNT_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
		timedElementSupport.repeatCount = Float.MAX_VALUE;
	    } else {
		timedElementSupport.repeatCount = parseFloatTrait(name, value);
	    }
	} else if (SVGConstants.SVG_REPEAT_DUR_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
		timedElementSupport.repeatDur = Time.INDEFINITE;
	    } else {
		timedElementSupport.setRepeatDur(parseClockTrait(name, value));
	    }
	} else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
	    checkWriteLoading(name);
	    if (SVGConstants.SVG_REMOVE_VALUE.equals(value)) {
		timedElementSupport.fillBehavior = 
		    TimedElementSupport.FILL_BEHAVIOR_REMOVE;
	    } else if (SVGConstants.SVG_FREEZE_VALUE.equals(value)) {
		timedElementSupport.fillBehavior = 
		    TimedElementSupport.FILL_BEHAVIOR_FREEZE;
	    } else {
		throw illegalTraitValue(name, value);
	    }
	} else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * When a TimeAttributesNode is hooked into the document tree, it needs
     * to validate (only if the Document is loaded).
     */
    final void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        if (ownerDocument.loaded) {
            validate();
        }
    }

}
