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
import javax.microedition.global.ResourceException;
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
 * This class represents a resource manager factory for creating device
 * resource managers.
 */
public class DevResourceManagerFactory
    extends AppResourceManagerFactory {

    /**
     * Default resource file name for device resources.
     */
    private static final String BASENAME =
        Configuration.getProperty("microedition.global.common");
    /**
     * Class name.
     */
    private static final String classname =
                                DevResourceManagerFactory.class.getName();


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
     * Default constructor for SecurityToken initialization.
     */
    public DevResourceManagerFactory() {
        super(null);
    }

    /**
     *
     * Creates a new resource manager factory for creating device resource
     * managers.
     * @param cache resource cache used
     */
    public DevResourceManagerFactory(ResourceCache cache) {
        super(cache);
    }

    /**
     * Create new {@link ResourceBundleReader} for reading device
     * resources.
     *
     * @param name name of resource
     * @return resource bundle reader
     */
    protected ResourceBundleReader getResourceBundleReaderForName(String name) {
        return DevResourceBundleReader.getInstance(name);
    }

    /**
     * Creates a new instance of <code>DevResourceManager</code>. The new
     * instance can be used to get device resources of the given locale. The
     * resource files will be accessed through the given resource bundle
     * reader.
     *
     * @param baseName baseName should be always "common.res". Because this
     * constructor is called by the {@link DevResourceManagerFactory} it is
     * always ok.
     * @param locales array of supported locales
     * @param readers array of opened readers for supported locales
     * @return new DevResourceManager
     */
    protected ResourceManager newResourceManagerInstance(
                              String baseName,
                              String[] locales,
                              ResourceBundleReader[] readers) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": Creating new DevResourceManager " +
                           "for \"" + baseName + "\" with cache: " +
                           this.resourceCache + "\nwith readers:");
            for (int i = 0; i < readers.length; i++) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               readers[i].toString());
            }
        }
        return new DevResourceManager(baseName,
                                      locales,
                                      readers,
                                      this.resourceCache);
    }

    /**
     * Gives resource URL that can be used for method {@link
     * Class#getResourceAsStream(String name)}.
     *
     * @param  baseName  the base name
     * @param  locale    the locale
     * @return           resource URL
     */
    protected String getResourceUrl(String baseName, String locale) {
        String resourceUrl;
        if (baseName == null ||
                baseName.length() == 0) {
            baseName = "common.res";
        }
        resourceUrl = (locale != null && locale.length() > 0)
                 ? locale + "/" + baseName
                 : baseName;
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": url=" + resourceUrl);
        }
        return resourceUrl;
    }

    /**
     * Read metafile {@link
     * com.sun.j2me.global.AppResourceManagerFactory.MetaFile}
     * for given base name. Metafiles are stored under global directory
     * in form _&lt;base name&gt;.<br>
     *
     *
     * @param  baseName  the base name
     * @return           metafile for the base name
     */
    protected MetaFile getMetafileForBaseName(String baseName) {
        MetaFile mf;
        if (baseName != null && baseName.length() == 0) {
            baseName = BASENAME;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": Get metafile for \"" +
                           baseName + "\"");
        }
        int hcode = resourceCache.getHashCodeForResource("_" + baseName, "", 0);
        Resource r = resourceCache.lookup(hcode);
        if (r == null) {
            String mfileName = "_" + baseName;
            try {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname  + ": " +
                                   "Not found in cache. Reading from store:" +
                                   "\"" + mfileName + "\"");
                }
                RandomAccessStream storage =
                            new RandomAccessStream(classSecurityToken);

                if (storage == null) {
                    throw new IOException("Storage is null.");
                }

                storage.connect(File.getConfigRoot(Constants.INTERNAL_STORAGE_ID)
                        + Configuration.getProperty("microedition.global.root")
                        + mfileName, Connector.READ);

                InputStream is = storage.openInputStream();
                mf = new MetaFile(is);
                is.close();

            } catch (IOException ioe) {
                throw new ResourceException(
                                     ResourceException.METAFILE_NOT_FOUND,
                                     "Not found metafile for base name \""
                                     + baseName + "\"");
            }
            resourceCache.addResource(new Resource(hcode, mf.getSize(), mf));
        } else {
            mf = (MetaFile) r.getValue();
        }
        return (MetaFile)mf.clone();
    }
}
