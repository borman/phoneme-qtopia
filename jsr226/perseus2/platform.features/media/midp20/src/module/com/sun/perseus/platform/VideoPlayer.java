/*
 * $RCSfile: AudioPlayer.java,v $
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
package com.sun.perseus.platform;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import com.sun.perseus.j2d.RasterImage;
import javax.microedition.media.control.VolumeControl;

/**
 * This class must be re-implemented for each platform Perseus is ported
 * to. It provides support for video.
 *
 * @author <a href="mailto:marc.owerfeldt@sun.com">Marc Owerfeldt</a>
 * @version $Id: VideoPlayer.java
 */
public class VideoPlayer {
    private Player p;
    private VolumeControl vc;
    private int state;
    
    final static int UNKNOWN  = 0;
    final static int REALIZED = 1;
    final static int STARTED  = 2;
    final static int STOPPED  = 3;    
    final static int CLOSED  = 4; 
    
    VideoPlayer(String url) throws Exception {
        state = UNKNOWN;
    
        if (url == null) {
            throw new Exception("URL is null");
        }
        
        try {
            p = Manager.createPlayer(url);
            p.realize();
            state = REALIZED;
            vc = (VolumeControl)p.getControl("VolumeControl");
        } catch (MediaException e) {
            throw new Exception (e.getMessage());
        }
    }
 
    /**
     * Plays the media.
     *
     * @param startTime the start time in milliseconds.
     */
    public void play(long startTime) {
        if (p == null) return;
    
        try {
            p.setMediaTime(startTime);
            p.start();
            state = STARTED;
        } catch (MediaException e) {
        }       
    }
    
    /**
     * Set the volume level using a floating point scale with values 
     * between 0.0 and 1.0. 0.0 is silence; 1.0 is the loudest useful 
     * level that this GainControl supports.
     *
     * NOTE:
     * The output signal level is calculated using the logarithmic scale 
     * described below (where vol is the value of the element volume):
     * dB change in signal level = 20 * log10(vol)
     * User agents may limit the actual signal level to some maximum, 
     * based on user preferences and hardware limitations.
     * If the element has an element volume of 0, then the output signal must 
     * be inaudible. If the element has an element volume of 1, then the output 
     * signal must be at the system volume level. Neither the audio-level property 
     * nor the element volume override the system volume setting. 
     */    
    public void setVolume(float volume) {        
        if (vc != null) {
            int level = (int) (100 * volume);
    
            vc.setLevel(level);
        }
    }

    /**
     * Retrieves the volume level using a floating point scale with values 
     * between 0.0 and 1.0. 0.0 is silence; 1.0 is the loudest useful 
     * level that is supported.
     */
    public float getVolume() {
        int level = vc.getLevel();
        
        float volume = ((float)level) / 100.0f;
        
        return volume;
    }
    
    /**
     * Returns the most recent decoded frame
     * in form of a RasterImage.
     */
    public RasterImage getFrame() {
        return null;
    }

    /**
     * Stops the media player.
     */
    public void stop() {
        if (p == null) return;

        try {
            p.stop();
        } catch (MediaException e) {
            
        }
    }

    /**
     * Closes the media player.
     */
    public void close() {
	if (p != null) {
	    p.close();
	    p = null;
	}
    }
}