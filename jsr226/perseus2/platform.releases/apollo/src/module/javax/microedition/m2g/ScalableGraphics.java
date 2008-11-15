/*
 * $RCSfile: ScalableGraphics.java,v $
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
package javax.microedition.m2g;

import com.sun.j2me.proxy.lcdui.Graphics;

import com.sun.perseus.model.DocumentNode;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;

import com.sun.pisces.GraphicsSurface;
import com.sun.pisces.NativeSurface;
import com.sun.pisces.PiscesRenderer;


/**
 * This is the fundamental class for 2D rendering. The ScalableGraphics context 
 * class provides and handles all the rendering capability within this package. 
 * In other words, the rendering can only be achieved through the render methods 
 * provided in this class. Note that the ScalableGraphics instance must be bound 
 * to the rendering target prior to calling the render method. The 
 * implementation must clip to the viewport boundaries.
 */
public class ScalableGraphics  {
    /**
     * Paint used to clear screens.
     */
    static final RGB CLEAR_PAINT = new RGB(0, 0, 0, 0);

    /**
     * The Graphics object bound to this class
     */
    Object g = null;
    Graphics graphics = null;

    /**
     * The current quality mode.
     */
    int qualityMode = RENDERING_QUALITY_HIGH;

    /**
     * The current transparency for rendering images.
     */
    float alpha = 1f;

    /**
     * Adapter for Graphics to be used in PiscesRenderer as a Surface.
     */
    GraphicsSurface gs;

    /**
     * The PiscesRenderer associated with the screen.
     */
    PiscesRenderer pr;

    /**
     * The RenderGraphics used to draw to the PiscesRenderer
     */
    RenderGraphics rg;
    /**
     * The offscreen native surface used for drawing with alpha < 1.
     */         
    NativeSurface offscreenNS;
    RenderGraphics offscreenRG;
    /**
     * The Tile to which rendering is clipped.
     */         
    final Tile clipTile = new Tile();
    
    /**
     * Defines a low rendering quality level.
     */
    public static final int RENDERING_QUALITY_LOW = 1;

    /**
     * Defines a high rendering quality level.
     */
    public static final int RENDERING_QUALITY_HIGH = 2;

    /**
    * Constructor
    */
    private ScalableGraphics() {
    }
	
    /**
     *
     * Bind the given Graphics as the rendering target of this ScalableGraphics 
     * context. The type of the Graphics object depends on the Java profile that
     * this specification is implemented on, as follows:
     *
     * <ul>
     * <li> javax.microedition.lcdui.Graphics on profiles supporting LCDUI;</li>
     * <li> java.awt.Graphics on profiles supporting AWT;</li>
     * <li> either of the above on profiles supporting both AWT and LCDUI.</li>
     * </ul>

     * @param target the object (Graphics) to receive the rendered image.
     *
     * @throws NullPointerException if <code>target</code> is null.
     * @throws IllegalArgumentException if <code>target</code> is invalid.
     * @throws IllegalStateException if <code>target</code> is already bound.
     */
    public void bindTarget(java.lang.Object target) {
        if (target == null) {
            throw new NullPointerException();
        }

        if (!("javax.microedition.lcdui.Graphics".equals (target.getClass().getName ()))) {
            throw new IllegalArgumentException("target is not instance of Graphics");
        }
        
        if (g != null) {
            throw new IllegalStateException("bindTarget(" + target + ") with g : " + g);
        }
        
        g = target;
        graphics = (Graphics)Graphics.__getInstance(target);  // compile time hack

        if (rg == null) {
            gs = new GraphicsSurface();
            pr = new PiscesRenderer(gs);
            /* 
             * we want to reuse RenderGraphics for multiple target 
             * widths / heights, so we use the maximum values             
             */             
            rg = new RenderGraphics(pr, Short.MAX_VALUE, Short.MAX_VALUE);
        }

        gs.bindTarget(g);
    }
    
    /**
     *
     * Flushes the rendered ScalableImage to the currently bound target and then
     * releases the target. This ensures that the ScalableImage is actually made
     * visible on the target that was set in bindTarget. Otherwise, the image 
     * may or may not become visible.
     *
     * @throws IllegalStateException if <code>target</code> is not bound.
     */
    public void releaseTarget() {
        if (g == null) {
            throw new IllegalStateException("releaseTarget() with null current target");
        }
        /* allow g to be garbage collected */
        g = null;
        graphics = null;
        gs.releaseTarget();
    }

    /**
     * Renders the specified ScalableImage using the supplied anchor point. The
     * anchor point given is relative to the upper left corner of the rendering
     * surface. It is important to note that the content is made visible or 
     * flushed to the display only after a call is made to
     * <code>releaseTarget</code>.
     *
     * @param x the X coordinate of the anchor point, in pixels.
     * @param y the Y coordinate of the anchor point, in pixels.
     * @param image the ScalableImage to be rendered.
     *
     * @throws NullPointerException if <code>image</code> is null.
     * @throws IllegalStateException if <code>target</code> is not bound.
     *
     * @see #releaseTarget
     */
    public void render(int x, int y, ScalableImage image) {
        if (image == null) {
            throw new NullPointerException();
        }
        
        if (graphics == null) {
            throw new IllegalStateException();
        }
        
        DocumentNode documentNode = 
            (DocumentNode) ((SVGImage) image).getDocument();
    
                int vpw = image.getViewportWidth();
        int vph = image.getViewportHeight();
        
        int clipX = graphics.getClipX();
        int clipY = graphics.getClipY();
        int clipW = graphics.getClipWidth();
        int clipH = graphics.getClipHeight();
        
        int translateX = 0;
        int translateY = 0;
        
        int intersectX2, intersectX = x;
        int intersectY2, intersectY = y;

        // calculate intersection of clip and viewport rectangles
        intersectX2 = intersectX + vpw;
        if ((intersectX2 < intersectX) || (intersectX2 > (clipX + clipW))) {
            // (intersectX2 < x) = overflow test
            intersectX2 = clipX + clipW;
        }
        
        intersectY2 = intersectY + vph;
        if ((intersectY2 < intersectY) || (intersectY2 > (clipY + clipH))) {
            // (intersectY2 < y) = overflow test
            intersectY2 = clipY + clipH;
        }
        
        if (intersectX < clipX) {
            translateX = intersectX - clipX;
            intersectX = clipX;
        }
        
        if (intersectY < clipY) {
            translateY = intersectY - clipY;
            intersectY = clipY;
        }
        
        int intersectW = intersectX2 - intersectX;
        int intersectH = intersectY2 - intersectY;
    
        if ((intersectW <= 0) || (intersectH <= 0)) {
            // empty intersection
            return;
        }
    
        // intersection rectangle = (intersectX, intersectY, intersectW,
        //                           intersectH)    
        
        rg.setRenderingQuality(qualityMode == RENDERING_QUALITY_HIGH);

        documentNode.sample(documentNode.getCurrentTime());
        documentNode.applyAnimations();
        
        clipTile.setTile(intersectX + graphics.getTranslateX(), intersectY +
                         graphics.getTranslateY(), intersectW, intersectH);

        // reset rg
        rg.setTransform(null);        
        
        // finally paint
        if (alpha != 0.0 && alpha < 1.0) {
            if (offscreenNS == null ||  
                offscreenNS.getWidth() != intersectW || 
                offscreenNS.getHeight() != intersectH) {
                
                offscreenNS = new NativeSurface(intersectW, intersectH);
                    
                offscreenRG = new RenderGraphics(new PiscesRenderer(offscreenNS), 
                                                 intersectW, 
                                                 intersectH);                                                 
            } else {
                offscreenNS.clean();
            }
            
            offscreenRG.translate(translateX, translateY);

            documentNode.paint(offscreenRG);
                                                
            offscreenNS.draw(g, intersectX, intersectY, intersectW,
                             intersectH, alpha);                                                                        
        } else if (alpha == 1.0){
            int renderX = x + graphics.getTranslateX();
            int renderY = y + graphics.getTranslateY();
            
            gs.bindTarget(g);   // IMPL note: it is already bound, isn't it? (bindTarget was called, otherwise graphics==null and we could not get here) 
            
            rg.translate(renderX, renderY);
            rg.setRenderingTile(clipTile);
 
            documentNode.paint(rg);
            gs.releaseTarget(); // IMPL note: why release?
        }
    }

    /**
     * Set the quality of rendering in the ScalableGraphics context. It can take
     * one of the values, RENDERING_QUALITY_LOW or RENDERING_QUALITY_HIGH. 
     * Default=RENDERING_QUALITY_HIGH. The implementation of these quality 
     * levels is implementation dependent and should be mapped to definitions in 
     * SVG spec (shape, text, image and color rendering).
     *
     * @param mode this value indicates the quality of rendering required.
     *
     * @throws IllegalArgumentException if the <code>mode</code> is invalid.
     */
    public void setRenderingQuality(int mode) {
        if (mode != RENDERING_QUALITY_LOW && mode != RENDERING_QUALITY_HIGH) {
            throw new IllegalArgumentException("" + mode);
        }

        this.qualityMode = mode;
    }

    /**
     * Set the transparency in the ScalableGraphics context with the supplied 
     * alpha value. Alpha value must be a floating point number in the range 
     * [0.0, 1.0]. The source pixels are always combined with destination pixels
     * using the <i>Source Over Destination</i> rule [Porter-Duff]. In this 
     * context, the Source Over Destination rule has the following properties: 
     * a fully opaque pixel in the source must replace the destination pixel, a
     * fully transparent pixel in the source must leave the destination pixel 
     * unchanged, and a semitransparent pixel in the source must be alpha 
     * blended with the destination pixel using the supplied value. The default 
     * alpha value is 1.0 (fully opaque), when not specified.
     *
     * @param alpha the constant alpha value to be used for rendering.
     *
     * @throws IllegalArgumentException if <code>alpha</code> is out of range.
     */
    public void setTransparency(float alpha) {
        if (alpha < 0f || alpha > 1f) {
            throw new IllegalArgumentException();
        }

        this.alpha = alpha;
    }

    /**
     * Retrieve a new instance of ScalableGraphics that can be associated to
     * an application.
     * <p>
     *
     * @return the newly created <code>ScalableGraphics</code> instance.
     *
     */
    public static ScalableGraphics createInstance() {
       return new ScalableGraphics();
    }
}


