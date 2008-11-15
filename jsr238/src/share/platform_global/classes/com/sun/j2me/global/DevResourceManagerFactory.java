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

import javax.microedition.global.*;
import java.util.Vector;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This class represents a resource manager factory for creating device
 * resource managers.
 */
public class DevResourceManagerFactory extends ResourceManagerFactory {

    /**
     * Class name.
     */
    private static final String classname =
                                DevResourceManagerFactory.class.getName();

    /**
     * Cache instance for device resources.
     */
    protected ResourceCache resourceCache;


    /**
     *
     * Creates a new resource manager factory for creating device resource
     * managers.
     *
     * @param cache  resource cache used
     */
    public DevResourceManagerFactory(ResourceCache cache) {
        resourceCache = cache;
    }

    /**
     * Returns an instance of <code>ResourceManager</code> class for the given
     * base name and system's default locale.
     * This method is never used - default system locale is handled in the
     * {@link javax.microedition.global.ResourceManager} class.
     *
     * @param  baseName                     the base name
     * @return                              the resource manager for the base
     *      name
     * @throws  ResourceException           if the resources for the base name
     *      doesn't exist
     * @throws  UnsupportedLocaleException  if the resources of the specified
     *      base name aren't available for the system's default locale
     * @see
     *      javax.microedition.global.ResourceManager#getManager(String)
     */
    public ResourceManager getManager(String baseName)
            throws ResourceException {
        return null;
    }

    /**
     * Creates an instance of <code>ResourceManager</code> class for the given
     * base name and locale.
     *
     * @param  baseName                     the base name
     * @param  locale                       the locale
     * @return                              the resource manager for the base
     *      name and locale
     * @throws  ResourceException           if the resources for the base name
     *      and locale doesn't exist
     * @throws  UnsupportedLocaleException  if the resources of the specified
     *      base name aren't available for the locale
     * @see
     *      javax.microedition.global.ResourceManager#getManager(String, String)
     */
    public ResourceManager getManager(String baseName, String locale)
            throws ResourceException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": getManager (" +
                           baseName + ", " + locale + ")");
        }

        String[] supported_locales = getSupportedLocales(baseName);
        Vector vlocales = new Vector(5);
        Vector vreaders = new Vector(5);

		if (locale == null) locale = "";

        // go through all parent locales match them against supported locales
        for (String alocale = locale; alocale != null;
                 alocale = LocaleHelpers.getParentLocale(alocale)) {
            int index = LocaleHelpers.indexInSupportedLocales(alocale,
                    supported_locales);
            if (index >= 0) {
                ResourceBundleReader reader =
                        new DevResourceBundleReader(index);
                if (null != reader) {
                    vlocales.addElement(alocale);
                    vreaders.addElement(reader);
                } else {
                    throw new ResourceException(
                        ResourceException.NO_RESOURCES_FOR_BASE_NAME,
                        "Device resources for \"" + alocale +
                        "\" are missing.");
                }
            } else {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": " + "locale \"" +
                                   alocale + "\" not supported.");
                }
            }
        }

        if (vlocales.size() == 0) {
            // base name not supported for locale nor for any parent locale
            throw new UnsupportedLocaleException();
        }

        String[] drm_locales = new String[vlocales.size()];
        ResourceBundleReader[] drm_readers =
                new ResourceBundleReader[vreaders.size()];
        vlocales.copyInto(drm_locales);
        vreaders.copyInto(drm_readers);

        // instantiate ResourceManager with supported locales
        return new DevResourceManager(drm_locales, drm_readers, resourceCache);
    }

    /**
     * Creates an instance of <code>ResourceManager</code> class for the given
     * base name and the first matching locale in the supplied array.
     *
     * @param  baseName                     the base name
     * @param  locales                      the array of locales
     * @return                              the resource manager for the base
     *      name and the matched locale
     * @throws  ResourceException           if no resources for the base name
     *      and any of the locales in the array are found
     * @throws  UnsupportedLocaleException  if the resources of the specified
     *      base name are available for no locale from the array
     * @see
     *      javax.microedition.global.ResourceManager#getManager(String,
     *      String[])
     */
    public ResourceManager getManager(String baseName, String[] locales)
             throws ResourceException {
        String[] supported_locales = getSupportedLocales(baseName);
        for (int i = 0; i < locales.length; i++) {
            int index = LocaleHelpers.indexInSupportedLocales(locales[i],
                    supported_locales);
            if (index >= 0) {
                return getManager(baseName, locales[i]);
            }
        }
        throw new UnsupportedLocaleException();
    }

    /**
     * Returns a list of locales supported by <code>DevResourceManager</code>.
     * Device resource manager can be constructed for each locale from the list.
     *
     * @param  baseName  the base name
     * @return           the list of the supported locales
     */
    public String[] getSupportedLocales(String baseName) {
        int lcount = getDevLocalesCount();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           "Number of supported locales: " + lcount);
        }
        String[] locales = new String[lcount--];
        for (; lcount >= 0; lcount--) {
            locales[lcount] = getDevLocaleName(lcount);
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               "Locale #" + lcount + ": " + locales[lcount]);
            }
        }

        return locales;
    }

    /**
     * Get number of supported locales for device resources.
     *
     * @return           number of locales
     */
    private static native int getDevLocalesCount();

    /**
     * Get one of supported device locales (by number).
     *
     * @param  index     index of locale to select
     * @return           locale name
     */
    private static native String getDevLocaleName(int index);

    /**
     * Get index of supported locales for device resources by its name.
     *
	 * @param  locale    name of locale
     * @return           index of locale
     */
    private static native int getDevLocaleIndex(String locale);


}
