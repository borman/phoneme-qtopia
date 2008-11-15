/*
 *   
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

package com.sun.j2me.global;

import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import com.sun.midp.main.Configuration;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.jsr238.SecurityInitializer;

/**
 *  An instance of this class is used to access device resource files.
 *
 */
public class DevResourceBundleReader extends AppResourceBundleReader {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** Security token to allow access to implementation APIs */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * The <code>RandomAccessStream</code> private object.
     */
    RandomAccessStream storage = null;

    /**
     * Creates a new instance of <code>DevResourceBundleReader</code>.
     */
    public DevResourceBundleReader() {
        super();
    }

    /**
     * Creates initialized instance of <code>ResourceBundleReader</code>, opens
     * resource bundle and reads up header.
     *
     * @return      An initialized instance of ResourceBundleReader, 
     * <code>null</code> if resource bundle can't be read.
     * @param  name path to resource bundle
     */
    public static ResourceBundleReader getInstance(String name) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           "DevResourceBundleReader: Get device resource " +
                           "bundle reader for \"" + name + "\"");
        }
        DevResourceBundleReader devreader = new DevResourceBundleReader();
        if (!devreader.initialize(name)) {
            return null;
        }
        // bundle wasn't found

        return devreader;
    }
    
    /**
     * Opens resource bundle and return its stream.
     * Bundle is opened from storage root.
     *
     * @return    stream for reading resource bundle or <code>null</code> if
     *      stream can't be opened.
     */
    protected InputStream getResourceBundleAsStream() {
        try {
            if (storage == null) {
                storage = new RandomAccessStream(classSecurityToken);
            }
            if (storage == null) {
                throw new IOException("Cannot access storage.");
            }
	    storage.connect(File.getConfigRoot(Constants.INTERNAL_STORAGE_ID) + Configuration
                            .getProperty("microedition.global.root")
                            + resourceName, Connector.READ);
            return storage.openInputStream();
            
        } catch (IOException ioe) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(ioe, "");
            }
            return null;
        }
    }

    /**
     * Closes resource bundle.
     */
    protected void freeResourceBundle(){
        if (storage == null) {
	        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
	            Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
	                           "Trying to close null storage");
	        }
        	return;
        }
        try {
        	istream.close();        	
		} catch (IOException e) {
	        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
	            Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
	                           "Exception while closing input stream: " + e.toString());
	        }
		}
        try {
        	storage.disconnect();        	
		} catch (IOException e) {
	        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
	            Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
	                           "Exception while disconnecting from storage: " + e.toString());
	        }
		}
    }

    /**
     * Reads resource from the stream.
     *
     * @param sr the resource that will be read from stream
     * @param resourceID the resource identifier
     * @return resource read from the stream
     * @exception IOException exception is thrown when error occurs
     * while reading.
     */
    public SerializableResource deserializeResource(SerializableResource sr,
                                                    int resourceID)
                                                    throws IOException {
        moveStreamTo(resourceID);
        sr.read(istream);
        return sr;
    }
}

