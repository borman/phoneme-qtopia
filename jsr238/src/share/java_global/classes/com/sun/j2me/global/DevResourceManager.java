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

import javax.microedition.global.ResourceException;
import java.io.IOException;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 *  This class represents a resource manager for accessing device resources. In
 *  the case of emulator it reuses the application resource manager
 *  implementation and extends the application resource file format with new
 *  data types which are needed by the emulator.
 *
 */
public class DevResourceManager extends AppResourceManager {

    /**
     *  Class name
     */
    private static final String classname = DevResourceManager.class.getName();
    
    /**
     *  constant for NumberFormatSymbols type.
     */
    public final static byte TYPE_NUMBER_FORMAT_SYMBOLS = (byte) 0xfe;

    /**
     *  constant for DateFormatSymbols type.
     */
    public final static byte TYPE_DATETIME_FORMAT_SYMBOLS = (byte) 0xfd;


    /**
     *  Creates a new instance of <code>DevResourceManager</code>. The new
     *  instance can be used to get device resources of the given locale. The
     *  resource files will be accessed through the given resource bundle
     *  reader.
     *
     * @param  baseName baseName should be always "common.res". Because this
     * constructor is called by the {@link DevResourceManagerFactory} it is 
     * always ok.
     * @param  locales  array of supported locales
     * @param  readers  array of opened readers for supported locales
     * @param  resourceCache cache instance used across resource managers.
     */
    public DevResourceManager(String baseName,
                              String[] locales, 
                              ResourceBundleReader[] readers,
                              ResourceCache resourceCache) {
        super(baseName, locales, readers, resourceCache);
    }

    /**
     *  Creates a new instance of <code>DevResourceManager</code>. The new
     *  instance can be used to get device resources of the given locale. The
     *  resource files will be accessed through the given resource bundle
     *  reader.
     *
     * @param base the base name
     * @param locales array of supported locales
     * @param readers array of opened readers for supported locales
     */
    public DevResourceManager(String base, 
                              String[] locales,
                              ResourceBundleReader[] readers) {
        this(base, locales, readers, null);
    }

    /**
     * Check if it is known resource type by this manager. Convert it to the
     * expected object. It recognizes {@link #TYPE_STRING},
     * {@link #TYPE_BINARY},
     * {@link #TYPE_NUMBER_FORMAT_SYMBOLS},
     * {@link #TYPE_DATETIME_FORMAT_SYMBOLS}.
     *
     * @param  resourceID       resource identifier
     * @param  type             the resource type
     * @param  length           the resource length
     * @param  reader           resource bundle reader
     * @return                  resource as object
     * @throws  IOException     thrown when resource cannot be read or
     *                          cannot be converted
     */
    protected Object convertToResourceType(int resourceID,
            byte type, int length,
            ResourceBundleReader reader) throws IOException {

        DevResourceBundleReader devReader = (DevResourceBundleReader) reader;
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": converting resource id = \"" +
                           resourceID + "\" to object type :\"" + type +
                           "\" with reader : " + reader);
        }

        if (type == TYPE_NUMBER_FORMAT_SYMBOLS) {
            NumberFormatSymbols nfs = new NumberFormatSymbols();
            devReader.deserializeResource(nfs, resourceID);
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": " +
                               "Resource type is NUMBER_FORMAT_SYMBOLS");
            }
            return nfs;
        } else if (type == TYPE_DATETIME_FORMAT_SYMBOLS) {
            DateFormatSymbols dfs = new DateFormatSymbols();
            devReader.deserializeResource(dfs, resourceID);
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": " +
                               "Resource type is DATETIME_FORMAT_SYMBOLS");
            }
            return dfs;
        } else {
            byte[] data = null;
            if (length != 0) {
                data = reader.getRawResourceData(resourceID);
            } else {
                data = new byte[0];
            }
            if (type == TYPE_STRING) {
                Object o = getUTF8(data);
                return o;
            } else if (type == TYPE_BINARY) {
                return data;
            }
        }
        return null;
    }

    /**
     * The method clones resource.
     *
     * @return copy of resource
     * @param resource the resource to clone
     */
    protected Object cloneResource(Object resource) {
        Object clon = super.cloneResource(resource);

        if (resource instanceof NumberFormatSymbols ||
            resource instanceof DateFormatSymbols) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": clone resource");
            }
            SerializableResource sr = (SerializableResource)resource;
            clon = sr.clone();
        }
        return clon;
    }
}

