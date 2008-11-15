/*
 * $RCSfile: AudioElement.java,v $
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

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.platform.AudioPlayer;
import com.sun.perseus.platform.MediaSupport;

/**
 * The <code>AudioElement</code> class models the &lt;audio&gt; tag in 
 * SVG Tiny 1.2. 
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id: Audio.java,v 1.3 2005/03/24 16:06:36 vhardy Exp $
 */
public class AudioElement extends MediaElement {
    /**
     * The associated AudioPlayer
     */
    private AudioPlayer audioPlayer;

    /**
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null.
     */
    public AudioElement(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_AUDIO_TAG);
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
        return new AudioElement(doc);
    }

    /**
     * Initializes the audio element.
     */
    void init() throws Exception {
       String url = getHref();
        
        if (url == null)
            throw new Exception ("media locator not set");
        
        if (audioPlayer == null) {
            audioPlayer = MediaSupport.getAudioPlayer(url);
        }
    }
    
    /**
     * Plays the audio.
     *
     * @param startTime  The start time in milliseconds.
     */
    void play(long startTime) {
       if (audioPlayer != null) {
            audioPlayer.play(startTime);
        }
    }
    
    /**
     * Stops the audio player.
     */
    void stop() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
    }
    
    /**
     * Closes the audio player.
     */
    void close() {
	audioPlayer.close();
        audioPlayer = null;
    }   
    
    void updateFrame() {
        // do nothing
    }

    /**
     * Set the volume level using a floating point scale with values 
     * between 0.0 and 1.0. 0.0 is silence; 1.0 is the loudest useful 
     * level that this GainControl supports.
     */
    void setVolume(float volume) {
        if (audioPlayer != null) {
            audioPlayer.setVolume(volume);
        }
    }
}

