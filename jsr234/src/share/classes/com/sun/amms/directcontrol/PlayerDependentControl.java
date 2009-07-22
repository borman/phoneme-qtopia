/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.amms.directcontrol;
import com.sun.mmedia.DirectPlayer;
import java.io.IOException;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;

class PlayerDependentControl {
    private DirectPlayer _player;
    private int _nativeHandle;
    static final int INVALID_HANDLE = -1;

    public PlayerDependentControl(DirectPlayer player) {
        setPlayer(player);
    }

    public PlayerDependentControl() {
        setPlayer(null);
    }

    protected void setPlayer(DirectPlayer player) {
        _player = player;
    }

    protected int getNativeHandle() {
        if (null != _player) {
            return _player.getNativeHandle();
        }
        return INVALID_HANDLE;
    }

    protected boolean isPlayerAccessible() {
        if (_player == null || 
            _player.getState() == javax.microedition.media.Player.CLOSED) {
            return false;
        }
        return true;
    }

    protected boolean isPlayerStarted() {
        if (_player == null) {
            return false;
        }
        if (_player.getState() != javax.microedition.media.Player.STARTED) {
            return false;
        }
        return true;
    }

    protected DirectPlayer getPlayer() {
        return _player;
    }
}
