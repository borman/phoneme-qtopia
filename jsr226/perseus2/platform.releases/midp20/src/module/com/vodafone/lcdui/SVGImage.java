/*
 * @(#)SVGImage.java	1.15 04/04/13
 *
 * © Copyright 2004 Vodafone Group Vodafone House The Connection Newbury 
 * RG14 2FN England All rights reserved. 
 */

/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

package com.vodafone.lcdui;

import javax.microedition.lcdui.Graphics;

import java.io.InputStream;
import java.io.IOException;
import javax.microedition.io.ConnectionNotFoundException;
import java.io.ByteArrayOutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection; 

import javax.microedition.m2g.ScalableGraphics;
import javax.microedition.m2g.ScalableImage;

import com.sun.perseus.model.SVG;

import com.sun.perseus.j2d.Transform;

import com.sun.perseus.model.DocumentNode;

import org.w3c.dom.svg.SVGException;

/**
 * The <code>SVGImage</code> class allows applications to manipulate and
 * render SVG Tiny 1.1 images on MIDP.
 * <br />
 *
 * <p>An SVG image is scalable, which means it can be scaled to 
 * the desired size. In addition, it is possible to zoom into
 * the image (and see details bigger) or zoom out (and see more of the 
 * image). Similarly, it is possible to rotate the image content and
 * pan (translate) the image. All these manipluations are done without 
 * pixelation effects, providing a high quality rendering at any resolution
 * zoom factor or rotation angle.</p>
 *
 * <p>An SVG image uses the SVG concept of viewport as defined
 * by the SVG specification.  The viewport is defined in a
 * coordinate space called the viewport space where one unit is equal
 * to the size of one device pixel, the positive x-axis direction 
 * running from left to right and the positive y-axis direction 
 * running from top to bottom.  The viewport size after initially creating
 * an <code>SVGImage</code> is defined by the width and height
 * attributes on the root <code>&lt;svg&gt;</code> element. If
 * these values use percentage coordinates, a reference value
 * of 100 is used. That means that if the root <code>&lt;svg&gt;</code>
 * element has a width of '50%', then the computed viewport width will
 * be 50.  If the root <code>&lt;svg&gt;</code> element does not specify
 * width/height attributes, default values of 100%/100% are used according
 * to the SVG specification.  Applying these to the reference value of 100
 * yields a default width/height of 100.  The viewport size can be
 * determined using the <code>getViewportWidth</code> and
 * <code>getViewportHeight</code> methods.  The initial viewport size
 * can be overridden through the <code>setViewportSize</code> method.</p>
 * 
 * <p>The <code>SVGImage</code> will fit the SVG content into the viewport 
 * when the content is rendered during a call to the <code>drawImage</code>
 * method.</p>
 * 
 * <p>The content is fit by applying a transform from the SVG user space to the 
 * viewport space. This is called the user space to viewport space transform: 
 * <code>F</code>. <code>F</code> accounts for the image's 
 * <code>preserveAspectRatio</code> and <code>viewBox</code>
 * attributes.</p>
 * 
 * <p>In addition, a user transform can be applied for the purpose of 
 * zooming, panning and rotating the document. This transform is called
 * U.</p>
 * 
 * <p>The effect of applying the various transforms is that a point in 
 * the SVG user space (<code>Pt[us]</code>) maps to a point in viewport 
 * space (<code>Pt[vp]</code>) through the following transform: </p>
 * 
 * <pre>Pt[vp] = U.F.Pt[us]</pre>
 * 
 * <p>In the above equation, <code>U</code> and <code>F</code> are affine
 * transforms representing 3 by 3 matrices and <code>Pt[x]</code> are
 * points with the conventional <code>[x, y, 1]</code> notation. The 
 * "<code>.</code>" operator represents a multiplication between two
 * matrices or a matrix and a point.</p>
 *
 * <p>An SVG Image can be drawn as follows:</p>
 * <pre>
 * String svgURI = "http://www.foo.com/svgMap.svg";
 * SVGImage svgRenderable 
 *    = SVGImage.createImage(svgURI);
 * svgRenderable.setViewportSize(200, 300);
 * svgImage.zoom(2);
 *
 * Graphics g = ...;
 * svgRenderable.drawImage(g, 40, 60);
 * </pre>
 *
 * <p>In SVG Tiny, URI references are found on the use, the anchor and the
 * image elements. The following paragraphs describe the error handling for
 * each of these elements in a <code>createImage</code> call.</p>
 *
 * <p>URI references on anchor elements are ignored in this API and never cause
 * an IOException, even when invalid.</p>
 *
 * <p>URI references on use elements must be valid, non-circular, local
 * references.  Any other reference (e.g., non-local, circular or invalid)
 * causes the document to be in error and an IOException is thrown.</p>
 *
 * <p>URI references on image elements should be valid external references.
 * Valid or invalid local references on image elements are an error and an
 * IOException is thrown.  Invalid external references on an image element
 * do not cause the document to be  in error and nothing should be drawn
 * for the image element which cannot be retrieved.</p>
 *
 * @see <a href="http://www.w3.org/TR/SVG11/">
 * Scalable Vector Graphics (SVG) 1.1 Specification</a>
 * @see <a href=" http://www.w3.org/TR/SVGMobile/">
 *  Mobile SVG Profiles: SVG Tiny and SVG Basic</a>
 * @since VSCL2.0
 */

public class SVGImage {
    /**
     * Error message when an invalid URI is passed to the createImage method.
     */
    public static final String ERROR_INVALID_URI = "Invalid URI";

    /**
     * Error message when a null URI is passed to the createImage method.
     */
    public static final String ERROR_NULL_URI = "Null URI parameter";

    /**
     * Error message when the viewport to user space transform cannot be computed.
     */
    public static final String ERROR_NON_INVERTIBLE_SVG_TRANSFORM = "The viewport to user space transform " + 
        "cannot be computed. Check that the viewBox is not zero width and/or height";

    /**
     * The corresponding JSR 226 ScalableImage
     */
    protected ScalableImage scalableImage;

    /**
     * ScalableGraphics instance used to paint the associated ScalableImage.
     */
    protected ScalableGraphics sg;

    /**
     * The root SVG element.
     */
    protected SVG svg;

    /**
     * The current user transform.
     */
    protected Transform userTransform = new Transform(null);

    /**
     * A work transform, used to avoid creating too many instances.
     */
    protected Transform workTransform = new Transform(null);

    /**
     * The displayed SVG Document instance.
     */
    protected DocumentNode doc;

    /**
     * Constructor is package protected and cannot be called by 
     * applications directly. Applications should use the 
     * <code>createImage</code> factory method to build new 
     * <code>SVGImage</code> instances.
     *
     * @param scalableImage the associated ScalableImage instance. Should
     *        not be null.
     * @see #createImage
     * @throws NullPointerException if the input scalableImage parameter is null.
     */
    SVGImage(final ScalableImage scalableImage) {
        if (scalableImage == null) {
            throw new NullPointerException();
        }

        this.scalableImage = scalableImage;
        this.doc = (DocumentNode) ((javax.microedition.m2g.SVGImage) scalableImage).getDocument();
        this.svg = (SVG) doc.getDocumentElement();
        this.sg = ScalableGraphics.createInstance();
    }

    /**
     * Creates a new <code>SVGImage</code>. The image's initial viewport
     * size is determined as described above.  The initial user transform
     * is set to identity which is the same state as after calling the
     * <code>resetTransform</code> method.
     *
     * @param svgURI describes an SVG URI to be loaded
     *        by the appropriate protocol handler, as described in the 
     *        <code>javax.microedition.io.Connector</code> class.
     * @return an SVGImage from the URI
     *
     * @throws IOException if there is an IO error while reading
     *         the SVG content. An exception is also thrown if the 
     *         SVG document is in error, as specified by Appendix F
     *         of the SVG 1.1 specification. Finally, an IOException
     *         is thrown if the SVG content contains non-local,
     *         circular, or invalid URI references on use elements
     *         or any local URI references on image elements (see
     *         the class comments).
     * @throws IllegalArgumentException if the svgURI parameter is invalid.
     *
     * @see #createImage(java.io.InputStream)
     */
    public static SVGImage createImage(String svgURI) throws IOException {
        ScalableImage scalableImage = null;

        try {
            scalableImage = ScalableImage.createImage(svgURI, null);
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException(ERROR_NULL_URI);
        } 
        
        return new SVGImage(scalableImage);
    }

    /**
     * Creates a new <code>SVGImage</code>. The image's initial viewport
     * size is determined as described above.  The initial user transform
     * is set to identity which is the same state as after calling the
     * <code>resetTransform</code> method.
     * 
     * @param is the input stream from which the SVG content
     *        will be read.
     * @return an SVGImage from the stream
     *

     * @throws IOException if there is an IO error while reading
     *         the SVG content. An exception is also thrown if the 
     *         SVG document is in error, as specified by Appendix F
     *         of the SVG 1.1 specification. Finally, an IOException
     *         is thrown if the SVG content contains non-local,
     *         circular, or invalid URI references on use elements
     *         or any local URI references on image elements (see
     *         the class comments).
     *         
     * @see #createImage(java.lang.String)
     */
    public static SVGImage createImage(InputStream is) throws IOException {
        ScalableImage scalableImage = ScalableImage.createImage(is, null);
        return new SVGImage(scalableImage);
    }

    /**
     * Sets the SVG image's viewport width and height.
     *
     * @param width the new viewport width
     * @param height the new viewport height
     *
     * @throws IllegalArgumentException if width is less than
     *         0 or height is less than 0.
     *
     * @see #getViewportWidth
     * @see #getViewportHeight
     */
    public void setViewportSize(int width, int height)
        throws IllegalArgumentException {
        if (( width < 0 ) || (height < 0)) 
            throw new IllegalArgumentException();

        scalableImage.setViewportWidth(width);
        scalableImage.setViewportHeight(height);
    }

    /**
     * Returns the viewport's width.
     * 
     * @return the current viewport width
     * @see #setViewportSize
     */
    public int getViewportWidth() {
        return scalableImage.getViewportWidth();
    }

    /**
     * Returns the viewport's height.
     * 
     * @return the current viewport height
     * @see #setViewportSize
     */
    public int getViewportHeight() {
        return scalableImage.getViewportHeight();
    }
    
    /**
     * Renders the SVG image into the <code>Graphics</code>
     * object. The viewport is the (0, 0, viewportWidth, viewportHeight)
     * rectangle in viewport space. This rectangle is positioned on the
     * canvas by the x and y parameters.
     *
     * <p>The viewportWidth and viewportHeight are lengths in the 
     * viewport coordinate space, which means they represent the
     * number of device pixels for the width and height of the image.</p>
     *
     * <p>Note that the <code>Graphics</code>'s translation (set by
     * the <code>translate</code> method) affects the location of
     * the viewport as well. For example, consider the following code
     * snippet:</p>
     *
     * <pre>
     * // Initially, the graphics context origin is (0, 0)
     * Graphics g = ...; 
     * 
     * // Change the graphics origin to (10, 30)
     * g.translate(10, 30);
     *
     * // Draw an SVGImage
     * SVGImage svgImage = ...;
     * svgImage.drawImage(g, 20, 40);
     * </pre>
     *
     * <p>The viewport will be positionned at <code>(30, 70)</code>
     * because the <code>drawImage</code> call uses the <code>Graphics</code>
     * translation and the viewport's origin.</p>
     * 
     * <p>In mathematical terms, the following transform is used to
     * transform viewport coordinates to graphics coordinates:</p>
     * <pre>
     * [1  0 tx+x]
     * [0  1 ty+y]
     * [0  0   1 ]
     * </pre>
     * <p>Where <code>tx</code> is the current <code>Graphics</code>
     * translation along the x-axis, <code>ty</code> is the 
     * current <code>Graphics</code> translation along the y-axis,
     * <code>x</code> is the origin of the viewport along the x-axis 
     * and <code>y</code> is the origin of the viewport along the 
     * y-axis. </p>
     * 
     * <p>In addition, rendering is limited to the area defined by the 
     * current <code>Graphics</code>'s clip. The area which may be impacted
     * by this call is the intersection of the viewport and the current
     * clip.</p>
     * 
     * @param g the <code>Graphics</code> where the SVG content
     *        is drawn.
     * @param x the location, along the x-axis, where the SVG content
     *        will be drawn. This is the x location of the viewport
     *        on the canvas.
     * @param y the location, along the y-axis, where the SVG content
     *        will be drawn. This is the y location of the viewport 
     *        on the canvas.
     * 
     * @throws NullPointerException if the <code>Graphics g</code> is null.
     *
     * @see javax.microedition.lcdui.Graphics
     */
    public void drawImage(Graphics g, int x, int y) {
        sg.bindTarget(g);
        sg.render(x, y, scalableImage);
        sg.releaseTarget();
    }

    /**
     * Maps the input viewport coordinate to an SVG user space
     * coordinate. Input coordinates may be outside the 
     * <code>[0, 0, viewportWidth, viewportHeight]</code> 
     * rectangle.
     *
     * <p>The transformation accounts for the SVG user space
     * to viewport space transform due to the processing
     * of preserveAspectRatio and viewBox on the root
     * <code>&lt;svg&gt;</code> element as well as any user transform
     * (see the <code>zoom</code>, <code>pan</code> and 
     * <code>rotate</code> methods). Using the <code>U</code>
     * and <code>F</code> notation from the class description,
     * the transform applied to the input coordinate is:
     * <code>inverse(U.F)</code>, where the <code>inverse()</code>
     * function is such that inverse(U.F).U.F is equal to the 
     * identity transform.</p>
     * 
     * @param viewportCoordinate an array of two integer values,
     *        in the viewport coordinate space. The first array value
     *        is the x-axis coordinate and the second value is the
     *        y-axis coordinate.
     * @param userSpaceCoordinate an array of two floating point
     *        values, in the SVG user space coordinate system. This
     *        is where the result is written.The first array value
     *        is the x-axis coordinate and the second value is the
     *        y-axis coordinate.
     *
     * @throws IllegalArgumentException if one or both of the 
     *         input arguments are null or if they are of 
     *         a length different than 2.
     *        
     * @see #toViewportSpace
     */
    public void toUserSpace(int[] viewportCoordinate,
                            float[] userSpaceCoordinate) {

        if (( userSpaceCoordinate == null ) || ( viewportCoordinate == null )) {
            throw new IllegalArgumentException();
        }
        if (( userSpaceCoordinate.length != 2 ) || ( viewportCoordinate.length != 2 )) {
            throw new IllegalArgumentException();
        }

        // Get the current screenCTM
        Transform txf = svg.getTransformState();

        try {
            txf = (Transform) txf.inverse();
        } catch (SVGException se) {
            throw new IllegalStateException(ERROR_NON_INVERTIBLE_SVG_TRANSFORM);
        }

        float[] pt = new float[2];
        pt[0] = viewportCoordinate[0];
        pt[1] = viewportCoordinate[1];
        txf.transformPoint(pt, userSpaceCoordinate);
    }

    /**
     * Maps the input SVG user space coordinate to a viewport coordinate.
     * 
     * <p>The transformation applied includes the user space
     * to viewport space transform due to the processing
     * of preserveAspectRatio and viewBox on the root
     * <code>&lt;svg&gt;</code> element as well as any user transform
     * (see the <code>zoom</code>, <code>pan</code> and 
     * <code>rotate</code> methods).Using the <code>U</code>
     * and <code>F</code> notation from the class description,
     * the transform applied to the input coordinate is:
     * <code>U.F</code></p>
     *
     * <p>Note that the resulting coordinate may be outside the
     * <code>[0, 0, viewportWidth, viewportHeight]</code> 
     * rectangle.</p>
     *
     * @param userSpaceCoordinate an array of two floating point
     *        values, in the user space coordinate system.
     * @param viewportCoordinate an array of two integer values,
     *        in the viewport coordinate space.
     *        This is where the result is written.
     *
     * @throws IllegalArgumentException if one or both of the 
     *         input arguments are null or if they are of 
     *         a size different than 2.
     * 
     * @see #toUserSpace
     */
    public void toViewportSpace(float[] userSpaceCoordinate,
                                int[] viewportCoordinate) {
        if (( userSpaceCoordinate == null ) || ( viewportCoordinate == null )) {
            throw new IllegalArgumentException();
        }
        
        if (( userSpaceCoordinate.length != 2 ) || ( viewportCoordinate.length != 2 )) {
            throw new IllegalArgumentException();
        }
        
        // Get the current screenCTM
        Transform txf = svg.getTransformState();

        float[] pt = new float[2];
        pt[0] = viewportCoordinate[0];
        pt[1] = viewportCoordinate[1];
        txf.transformPoint(userSpaceCoordinate, pt);
        viewportCoordinate[0] = (int) pt[0];
        viewportCoordinate[1] = (int) pt[1];
    }                                

    /**
     * Preconcatenates a zoom transform to the current user
     * transform.
     * <p>As a result of invoking this method, the
     * new user transform becomes:</p>
     * <pre>
     * U' = Z.U
     * </pre>
     * <p>Where U is the previous user transform and Z is the
     * uniform scale transform:</p>
     * <pre>
     * [zoom    0   0]
     * [  0   zoom  0]
     * [  0     0   1]
     * </pre>
     * @param zoom the zoom factor to add to the user transform
     *
     * @throws IllegalArgumentException if the input zoom factor
     *         is not greater than zero.
     */
    public void zoom(float zoom) {        
        if (zoom <=0) {
            throw new IllegalArgumentException();
        }

        workTransform.setTransform(null);
        workTransform.mScale(zoom);
        workTransform.mMultiply(userTransform);
        doc.setTransform(workTransform);
        swapTransforms();
    }

    /**
     * Preconcatenates a translation to the current user transform.
     * 
     * <p>As a result of invoking this method, the
     * new user transform becomes:</p>
     * <pre>
     * U' = T.U
     * </pre>
     * <p>Where U is the previous user transform and T is the
     * following transform:</p>
     * <pre>
     * [1    0   panX]
     * [0    1   panY]
     * [0    0      1]
     * </pre>
     *
     * @param panX the translation along the x-axis
     * @param panY the translation along the y-axis
     */
    public void pan(float panX, float panY) {
        workTransform.setTransform(null);
        workTransform.mTranslate(panX, panY);
        workTransform.mMultiply(userTransform);
        doc.setTransform(workTransform);
        swapTransforms();
    }

    /**
     * Preconcatenates a rotation about the given center to the 
     * current user transform.
     *
     * <p>As a result of invoking this method, the
     * new user transform becomes:</p>
     * <pre>
     * U' = R.U
     * </pre>
     * <p>Where U is the previous user transform and R is the
     * uniform scale transform:</p>
     * <pre>
     * [1    0   x][cos(theta) -sin(theta)   0][1    0   -x]
     * [0    1   y][sin(theta)  cos(theta)   0][0    1   -y]
     * [0    0   1][    0           0        1][0    0    1]
     * </pre>
     *
     * <p>Postive angles go clockwise because of the orientation
     * of the coordinate system.</p>
     *
     * @param theta the additional angle, in degrees
     * @param x the rotation center along the x-axis
     * @param y the rotation center along the y-axis
     */
    public void rotate(float theta, float x, float y) {
        workTransform.setTransform(null);
        workTransform.mTranslate(x, y);
        workTransform.mRotate(theta);
        workTransform.mTranslate(-x, -y);
        workTransform.mMultiply(userTransform);
        doc.setTransform(workTransform);
        swapTransforms();
    }

    /**
     * Resets the user transform to identity.
     *
     * <p>As a result, the user transform becomes:</p>
     * <pre>
     * [1    0   0]
     * [0    1   0]
     * [0    0   1]
     * </pre>
     */
    public void resetUserTransform() {
        workTransform.setTransform(null);
        doc.setTransform(workTransform);
        swapTransforms();
    }

    /**
     * Implementation helpers. Swaps the workTransform and the 
     * userTransform values.
     */
    private void swapTransforms() {
        Transform tmp = workTransform;
        workTransform = userTransform;
        userTransform = tmp;
    }
    
 }
