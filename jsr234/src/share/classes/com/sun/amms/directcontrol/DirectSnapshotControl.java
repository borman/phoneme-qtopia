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

import com.sun.amms.AMMSMPEventListener;
import com.sun.mmedia.DirectCamera;
import java.io.IOException;
import java.io.InterruptedIOException;
import javax.microedition.amms.control.camera.*;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.MMAPIPermission;

public class DirectSnapshotControl extends CameraDependentControl 
implements SnapshotControl, PlayerListener {

    private AMMSMPEventListener _mp_listener;
    private Jsr75DirCheckerProxy _dir_checker;

    private String _directory;
    private String _prefix;
    private String _suffix;
    private String tmp_directory;
    private String tmp_prefix;
    private String tmp_suffix;

    private Object stateLock = new Object();

    private final int STATE_IDLE     = 0;
    private final int STATE_SHOOTING = 1;

    private int state = STATE_IDLE;

    public static DirectSnapshotControl createInstance(DirectCamera cam) {
        if (!nIsSupported(cam.getNativeHandle())) {
            return null;
        }
        return new DirectSnapshotControl(cam);
    }
    
    private DirectSnapshotControl(DirectCamera cam) {
        _mp_listener = AMMSMPEventListener.getInstance();
        setCamera(cam);

        cam.addPlayerListener( this );

        String def_loc = System.getProperty( "fileconn.dir.photos" );

        // remove prefix
        if( def_loc.startsWith( "file:" ) ) {
            def_loc = def_loc.substring( "file:".length() );
        }
        
        // leave only one leading slash
        while( def_loc.startsWith( "//" ) ) {
            def_loc = def_loc.substring( 1 );
        }

        // force ending slash
        if( 0 == def_loc.length() || def_loc.charAt( def_loc.length() - 1 ) != '/' ) {
            def_loc += "/";
        }

        tmp_directory = def_loc;

        if( isCameraAccessible() ) {
            nSetDirectory( getNativeHandle(), def_loc );
            _directory = def_loc;
        }
    }
    
    private synchronized Jsr75DirCheckerProxy get75DirChecker() {
        if (_dir_checker == null) {
            Class c = null;
            try {
                c = Class.forName( 
                    "com.sun.amms.directcontrol.Jsr75DirCheckerProxyImpl");
                _dir_checker = (Jsr75DirCheckerProxy)c.newInstance(); 
            } catch (ClassNotFoundException e1) {
            } catch (IllegalAccessException e2) {
                throw new RuntimeException(
                    "Could not access the class" + c.toString());
            } catch (InstantiationException e3) {
                throw new RuntimeException(
                    "Could not instantiate the class" + c.toString());
            }
            if (_dir_checker == null) {
                try {
                    c = Class.forName(
                        "com.sun.amms.directcontrol.Jsr75DirCheckerProxyStub");
                    _dir_checker = (Jsr75DirCheckerProxy)c.newInstance();
                } catch (ClassNotFoundException e1) {
                    throw new RuntimeException(
                        "Could not find DirChecker class");
                } catch (IllegalAccessException e2) {
                    throw new RuntimeException(
                        "Could not access the class" + c.toString());
                } catch (InstantiationException e3) {
                    throw new RuntimeException(
                        "Could not instantiate the class" + c.toString());
                } 
            }
        }

        return _dir_checker;
    }

    public void playerUpdate( Player player, java.lang.String event,
                              java.lang.Object eventData ) {

        if( SHOOTING_STOPPED.equals( event ) || STORAGE_ERROR.equals( event ) ) {

            synchronized( stateLock ) {
                state = STATE_IDLE;

                if( !tmp_directory.equals(_directory) ) {
                    setDirectory( tmp_directory );
                }

                if( !tmp_prefix.equals( _prefix ) ) {
                    setFilePrefix( tmp_prefix );
                }

                if( !tmp_suffix.equals( _suffix ) ) {
                    setFileSuffix( tmp_suffix );
                }
            }
        }
    }

    /**
     * Sets the file directory where the images will be stored.
     * <p>
     * The directory name is given as a String which can contain the trailing
     * slash "/" in the directory name but it is not required.
     * </p>
     * <p>
     * For example, both
     * <code>"/SDCard/"</code> and <code>"/SDCard"</code>
     * set the directory to the root of the SDCard in a platform
     * implementing <code>FileConnection</code> of JSR-75.
     * In case of <code>FileConnection</code> of JSR-75 the return
     * value of <code>FileConnection.getPath()</code> can be used
     * as it is as a parameter for this method.
     * </p>
     * @param directory the storage directory.
     * @throws IllegalArgumentException if the given directory does not exist
     * or it is null.
     * @throws SecurityException if the creation of files to the given location
     * is not allowed.
     */
    public void setDirectory(String directory) {
        if (null == directory) {
            throw new IllegalArgumentException("null passed to setDirectory");
        }
        try {
            get75DirChecker();
        } catch (Throwable t) {
        }
        if (_dir_checker != null) {
            try {
                _dir_checker.checkDirectory(directory);
            } catch (IOException e) {
                throw new SecurityException(
                    "IOException was thrown while checking Camera Snapshot " +
                    "directory security permissions: " + e.getMessage());
            }
        }

        String dir = "" + directory;
        if (0 == dir.length() || dir.charAt(dir.length() - 1) != '/') dir += "/";

        if (isCameraAccessible())
        {
            synchronized( stateLock ) {
                tmp_directory = dir;
                if( STATE_IDLE == state ) {
                    nSetDirectory( getNativeHandle(), dir );
                    _directory = dir;
                }
            }
        }
    }

    /**
     * Gets the storage directory. If <code>setDirectory</code> has not been
     * called the directory
     * points to the default image storing location.
     *
     * @return The current storage directory with the trailing slash "/".
     */
    public String getDirectory() {
        return tmp_directory;
    }

    /**
     * Sets the filename prefix.
     * @param prefix The prefix String for the files to be created.
     * @throws IllegalArgumentException if the given prefix cannot be set or if
     * it is null.
     */
    public void setFilePrefix(String prefix) {
        if (null == prefix) {
            throw new IllegalArgumentException("null passed to setFilePrefix!");
        }
        if (isCameraAccessible()) {
            synchronized( stateLock ) {
                tmp_prefix = prefix;
                if( STATE_IDLE == state ) {
                    nSetFilePrefix(getNativeHandle(), prefix);
                    _prefix = prefix;
                }
            }
        }
    }

    /**
     * Gets the filename prefix.
     *
     * @return The filename prefix.
     */
    public String getFilePrefix() {
        return tmp_prefix;
    }

    /**
     * Sets the filename suffix.
     *
     * @throws IllegalArgumentException if the given suffix cannot be set or if
     * it is null.
     */
    public void setFileSuffix(String suffix) {
        if (null == suffix) {
            throw new IllegalArgumentException("null passed to setFileSuffix!");
        }
        if (isCameraAccessible()) {
            synchronized( stateLock ) {
                tmp_suffix = suffix;
                if( STATE_IDLE == state ) {
                    nSetFileSuffix(getNativeHandle(), suffix);
                    _suffix = suffix;
                }
            }
        }
    }

    /**
     * Gets the filename suffix.
     *
     * @return The filename suffix.
     */
    public String getFileSuffix() {
        return tmp_suffix;
    }

    /**
     * Starts burst shooting.
     *
     * @param maxShots Maximum number of shots that are going to be taken
     * or FREEZE or FREEZE_AND_CONFIRM. Integer.MAX_VALUE
     * will take as many shots as possible.
     *
     * @throws IllegalArgumentException if maxShots is less than one.
     * @throws IllegalStateException if prefix and suffix have not been set.
     * @throws SecurityException if the application does not have permission to
     * take snapshots.
     */
    public void start(int maxShots) throws SecurityException {
        checkPermission();
        if (_prefix == null && _suffix == null) {
            throw new IllegalStateException(
                "Both prefix and suffix are not set!");
        }
        if (!isCameraStarted()) {
            throw new IllegalStateException("Camera is not started!");
        }
        if ((maxShots < 1) && (maxShots != FREEZE) &&
            (maxShots != FREEZE_AND_CONFIRM )) {
            throw new IllegalArgumentException("maxShots " + maxShots +
                " is invalid!");
        }
        if (isCameraAccessible()) {
            getCamera().checkSnapshotPermission();
            synchronized( stateLock ) {
                nStart( getNativeHandle(), maxShots );
                state = STATE_SHOOTING;
            }
            if (FREEZE_AND_CONFIRM == maxShots) {
                getCamera().sendEvent(WAITING_UNFREEZE, null);
            }
        }
    }

    private static void checkPermission() {
        try {
            AppPackage.getInstance().
                checkForPermission(MMAPIPermission.SNAPSHOT);
        } catch (InterruptedException ie) {
            throw new SecurityException(
                "Interrupted while trying to ask the user permission");
        }
    }


    /**
     * Stops burst shooting.
     */
    public void stop() {
        if( isCameraAccessible() ) {
            nStop(getNativeHandle());
        }
    }

    /**
     * Unfreezes the viewfinder and saves the snapshot depending
     * on the parameter. The method does not do anything if
     * <code>start(FREEZE_AND_CONFIRM)</code> has not been called.
     *
     * @param save true=saves the snapshot, false=does not save the snapshot.
     */
    public void unfreeze(boolean save) {
        if( isCameraAccessible() ) {
            synchronized( stateLock ) {
                nUnfreeze(getNativeHandle(), save);
                state = STATE_IDLE;
            }
        }
    }

    protected native void nSetDirectory(int hNative, String directory);
    protected native String nGetDirectory(int hNative);
    protected native void nSetFilePrefix(int hNative, String prefix);
    protected native String nGetFilePrefix(int hNative);
    protected native void nSetFileSuffix(int hNative, String suffix);
    protected native String nGetFileSuffix(int hNative);
    protected native void nStart(int hNative, int maxShots);
    protected native void nStop(int hNative);
    protected native void nUnfreeze(int hNative, boolean save);
    protected static native boolean nIsSupported(int hNative);

}
