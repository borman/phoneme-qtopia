/*
 * $RCSfile: SVGAnimatorImpl.java,v $
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
package com.sun.perseus.midp;

import java.io.InputStream;

import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;

import com.sun.perseus.model.DocumentNode;

/**
 * The <code>SVGAnimatorImpl</code> class implements the <code>SVGAnimator</code>
 * JSR 226 class. It mostly delegates to the <code>SVGCanvas</code> class.
 *
 * @version $Id: SVGAnimatorImpl.java,v 1.3 2006/04/21 06:40:54 st125089 Exp $
 */
public final class SVGAnimatorImpl extends SVGAnimator {
    /**
     * The class name for a MIDP Canvas.
     */
    static final String MIDP_COMPONENT_CLASS = "javax.microedition.lcdui.Canvas";

    /**
     * The associated SVG canvas.
     */
    SVGCanvas svgCanvas;

    /**
     * Creates a new SVGAnimatorImpl associated with the given SVGImage
     * instance.
     *
     * @param svgImage the SVGImage this animator implementation should play in
     * a MIDP Canvas.
     */
    private SVGAnimatorImpl(final SVGImage svgImage) {
        svgCanvas = new SVGCanvas((DocumentNode) svgImage.getDocument());
    }

    /**
     * Create a new <code>SVGAnimator</code> for the specified SVGImage.
     *
     * @param svgImage the <code>SVGImage</code> to be rendered by this animator.
     * @param componentBaseClass the desired base class for the component associated
     *        with the animator (useful only for platforms supporting multiple 
     *        UI component frameworks). When null, this is equivalent to invoking
     *        <code>createAnimator</code> with the svgImage parameter only.
     * @return the newly created <code>SVGAnimator</code> instance.
     * @throws NullPointerException if <code>svgImage</code> is null.
     * @throws IllegalArgumentException if the specified 
     *         <code>componentBaseClass</code> is not supported by the
     *         implementation.
     */
    public static SVGAnimator createAnimator(SVGImage svgImage,
                                             String componentBaseClass) {
        if (svgImage == null) {
            throw new NullPointerException();
        }

        if (componentBaseClass != null 
            && 
            !MIDP_COMPONENT_CLASS.equals(componentBaseClass)) {
            throw new IllegalArgumentException();
        }

        return new SVGAnimatorImpl(svgImage);
    }

    /**
     * Associate the specified <code>SVGEventListener</code> with this
     * <code>SVGAnimator</code>.
     *
     * @param svgEventListener the SVGEventListener that will receive
     *        events forwarded by this <code>SVGAnimator</code>. If null,
     *        events will not be forwarded by the <code>SVGAnimator</code>.
     */
    public void setSVGEventListener(SVGEventListener svgEventListener) {
        svgCanvas.setSVGEventListener(svgEventListener);
    }

    /**
     * Set the time increment to be used for animation rendering.
     *
     * @param timeIncrement the minimal period of time, in seconds, that
     *         should elapse between frame. Must be greater than zero.
     * @throws IllegalArgumentException if timeIncrement is less than or equal to
     *         zero.
     * @see #getTimeIncrement
     */
    public void setTimeIncrement(float timeIncrement) {
        svgCanvas.setTimeIncrement(timeIncrement);
    }

    /**
     * Get the current time increment for animation rendering. The
     * SVGAnimator increments the SVG document's current time by this amount
     * upon each rendering. The default value is 0.1 (100 milliseconds).
     *
     * @return the current time increment, in seconds, used for animation
     *         rendering.
     * @see #setTimeIncrement
     */
    public float getTimeIncrement() {
        return svgCanvas.getTimeIncrement();
    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>playing</i>
     * state. In the <i>playing</i> state, both Animation and SVGImage
     * updates cause rendering updates. Note that in the playing state,
     * when the document's current time changes, the animator will seek
     * to the new time, and continue to play animations from this place.
     *
     * @throws IllegalStateException if the animator is not currently in
     *         the <i>stopped</i> or <i>paused</i> state.
     */
    public void play() {
        svgCanvas.play();
    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>paused</i> state.
     * The <code>SVGAnimator</code> stops advancing the document's current time
     * automatically (see the SVGDocument's setCurrentTime method). In consequence,
     * animation playback will be paused until another call to the <code>play</code> method
     * is made, at which points animations will resume from the document's current
     * time. SVGImage updates (through API calls) cause a rendering update
     * while the <code>SVGAnimator</code> is in the <i>paused</i> state.
     *
     * @throws IllegalStateException if the animator is not in the <i>playing</i>
     *         state.
     */
    public void pause() {
        svgCanvas.pause();
    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>stopped</i> state.
     * In this state, no rendering updates are performed.
     *
     * @throws IllegalStateException if the animator is not in the <i>playing</i>
     *         or <i>paused</i> state.
     */
    public void stop() {
        svgCanvas.stop();
    }

    /**
     * Retrieve the animator's target component.
     * @return the target component associated with the animator.
     * @see #createAnimator
     */
    public Object getTargetComponent() {
        return svgCanvas;
    }

    /**
     * Invoke the Runnable in the Document update thread and 
     * return only after this Runnable has finished.
     *
     * @param runnable the new Runnable to invoke.
     * @throws InterruptedException if the current thread is waiting,
     * sleeping, or otherwise paused for a long time and another thread
     * interrupts it.
     * @throws NullPointerException if <code>runnable</code> is null.
     * @throws IllegalStateException if the animator is in the <i>stopped</i> state.
     */
    public void invokeAndWait(Runnable runnable) throws InterruptedException {
        svgCanvas.invokeAndWait(runnable);
    }

    /**
     * Schedule execution of the input Runnable in the update thread at a later time.
     *
     * @param runnable the new Runnable to execute in the Document's update
     * thread when time permits.
     * @throws NullPointerException if <code>runnable</code> is null.
     * @throws IllegalStateException if the animator is in the <i>stopped</i> state.
     */
    public void invokeLater(Runnable runnable) {
        svgCanvas.invokeLater(runnable);
    }

}

