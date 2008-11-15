/* 
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

// import java.net.URL;
import com.sun.perseus.model.VideoElement;

/**
 * This class must be re-implemented for each platform Perseus is ported
 * to. It provides support for audio.
 *
 * On the J2SE version, the code checks whether or not the Java Media Framework 
 * is available and if so, it uses it. Otherwise, AudioClip and Applet are used
 * to play and mix audio.
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id: AudioSupport.java,v 1.1 2005/03/02 20:32:11 vhardy Exp $
 */
public class MediaSupport {
    /**
     * Returns the AudioPlayer object specified by the URL argument.  
     * This method always returns immediately, whether or not the audio 
     * player can be obtained. 
     *
     * @param url an absolute URL giving the location of the media source.
     * @return the audio player for the specified URL.
     */
    public static AudioPlayer getAudioPlayer(final String url) {
        AudioPlayer player = null;
              
        try {
            player = new AudioPlayer(url);
        } catch (Exception e) {
            System.out.println("Failed to retrieve an audio player!");
        }
        
        return player;
    }

    public static VideoPlayer getVideoPlayer(final String url) {
        VideoPlayer player = null;
        
        try {
            player = new VideoPlayer(url);
        } catch (Exception e) {
            System.out.println("Failed to retrieve a video player!");
        }
        
        return player;
    }
}
