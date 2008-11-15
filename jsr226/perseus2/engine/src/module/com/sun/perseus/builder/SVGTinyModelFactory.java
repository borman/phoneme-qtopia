/*
 * $RCSfile: SVGTinyModelFactory.java,v $
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
package com.sun.perseus.builder;

//import com.sun.perseus.model.AudioElement;
//import com.sun.perseus.model.VideoElement;
// import com.sun.perseus.model.TSpan;

import com.sun.perseus.model.*;
import com.sun.perseus.util.SVGConstants;

import java.util.Vector;

/**
 * This ModelFactory implementation is initialized with all the handlers
 * necessary to handle SVG Tiny 1.1 content.
 *
 * @version $Id: SVGTinyModelFactory.java,v 1.5 2006/04/21 06:36:12 st125089 Exp $
 */
public class SVGTinyModelFactory {

    /** List of required attributes for foreignObject. */
    public static final String[] FOREIGN_OBJECT_REQUIRED_ATTRIBUTES =
        {SVGConstants.SVG_WIDTH_ATTRIBUTE, SVGConstants.SVG_HEIGHT_ATTRIBUTE};

    /**
     * @param doc the document for which the prototypes are built.
     * @return a Vector with all the prototypes for SVG Tiny content.
     */
    public static Vector getPrototypes (final DocumentNode doc) {
        Vector v = new Vector();

        //
        // == Structure Module =================================================
        //
        v.addElement(new SVG(doc));
        v.addElement(new Group(doc));
        v.addElement(new Use(doc));
        v.addElement(new Defs(doc));
        v.addElement(new ImageNode(doc));
        v.addElement(new Switch(doc));
        v.addElement(new Symbol(doc));

        //
        // == Shape Module =====================================================
        //
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_PATH_TAG));
        v.addElement(new Rect(doc));
        v.addElement(new Line(doc));
        v.addElement(new Ellipse(doc));
        v.addElement(new Ellipse(doc, true)); // <circle>
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_POLYGON_TAG));
        v.addElement(new ShapeNode(doc, SVGConstants.SVG_POLYLINE_TAG));

        //
        // == Text Module ======================================================
        //
        v.addElement(new Text(doc));
        // v.addElement(new TSpan(doc));

        //
        // == Font Module ======================================================
        //
        v.addElement(new Font(doc));
        v.addElement(new FontFace(doc));
        v.addElement(new Glyph(doc));
        v.addElement(new Glyph(doc, SVGConstants.SVG_MISSING_GLYPH_TAG));
        v.addElement(new HKern(doc));

        //
        // == Hyperlinking Module ==============================================
        //
        v.addElement(new Anchor(doc));

        //
        // == Animation Module =================================================
        //
        v.addElement(new Animate(doc));
        v.addElement(new AnimateMotion(doc));
        v.addElement(new Set(doc));
        v.addElement(new AnimateTransform(doc));
        v.addElement(new Animate(doc, SVGConstants.SVG_ANIMATE_COLOR_TAG));
        v.addElement(new Discard(doc));

        //
        // == SolidColor Module ================================================
        //
        v.addElement(new SolidColor(doc));

        //
        // == Gradient Module ================================================
        //
        v.addElement(new LinearGradient(doc));
        v.addElement(new RadialGradient(doc));
        v.addElement(new Stop(doc));

        //
        // == Extensibility Module =========================================
        //
        v.addElement(
            new StrictElement(
                doc,
                SVGConstants.SVG_FOREIGN_OBJECT_TAG,
                SVGConstants.SVG_NAMESPACE_URI,
                FOREIGN_OBJECT_REQUIRED_ATTRIBUTES,
                null));
        //
        // == Media Module ================================================
        //
        //v.addElement(new AudioElement(doc));
        //v.addElement(new VideoElement(doc));
        addOptionalElement(v, "com.sun.perseus.builder.AudioElementSupport", doc);
        addOptionalElement(v, "com.sun.perseus.builder.VideoElementSupport", doc);

        return v;
    }

    /**
     * Helper method for adding optional elements
     * Due to restrictions on usage of reflection (only Class.forName), elements are created via factories.
     */
    static void addOptionalElement (
        final Vector v,
        final String elementFactoryName,
        final DocumentNode doc) {

        try {
            final Class clazz = Class.forName(elementFactoryName);
            final Object o = clazz.newInstance();
            v.addElement(((ElementFactory) o).newElement(doc));
        }
        catch (Exception e) {
        }
    }
}
