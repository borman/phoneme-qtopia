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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.global.ResourceException;
import javax.microedition.global.UnsupportedLocaleException;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This class represents a resource manager factory for creating application
 * resource managers.
 *
 */
public class AppResourceManagerFactory extends ResourceManagerFactory {

    /**
     * Class name.
     */
    private static final String classname =
                                AppResourceManagerFactory.class.getName();

    /**
     * Implementation of resource cache.
     */
    protected ResourceCache resourceCache;


    /**
     * Creates a new resource manager factory for creating application resource
     * managers.
     *
     * @param  cache  implementation of {@link ResourceCache} to cache
     *      resources.
     */
    public AppResourceManagerFactory(ResourceCache cache) {
        resourceCache = cache;
    }


    /**
     * Get manager for system locale. It's never used. {@link
     * AppResourceManagerFactory#getManager(String)} replaces this call.
     *
     * @param  baseName  the base name
     * @return  <code>null</code>
     * @throws  javax.microedition.global.ResourceException  if
     *          manager cannot be created
     */
    public ResourceManager getManager(String baseName) {
        return null;
    }

    /**
     * Creates initialized instance of ResourceBundleReader for given name.
     * It opens resource file and reads header.
     *
     * @param name name of resource file
     * @return reader for this resource file
     */
    protected ResourceBundleReader getResourceBundleReaderForName(String name) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": getResourceBundleReaderForName (" +
                           name + ")");
        }
        return AppResourceBundleReader.getInstance(name);
    }

    /**
     * Creates a new instance of <code>AppResourceManager</code>. The new
     * instance can be used to get application resources of the given base name
     * and locale. The resource files will be accessed through the given
     * resource bundle reader.
     *
     * @param  baseName     the base name
     * @param  locales  array of locales to try
     * @param  readers  array of readers corresponding to locales
     * @return new <code>AppResourceManager</code> object
     */
    protected ResourceManager newResourceManagerInstance(
                              String baseName,
                              String[] locales,
                              ResourceBundleReader[] readers) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "Creating new AppResourceManager for \"" +
                           baseName + "\" with cache: " + this.resourceCache +
                           "\nwith readers:");
            for (int i = 0; i < readers.length; i++) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               readers[i].toString());
            }
        }

        return new AppResourceManager(baseName,
                                      locales,
                                      readers,
                                      this.resourceCache);
    }


    /**
     * Instantiates <code>ResourceManager</code> for a given locale, going
     * through all parent locales and creating respective
     * {@link ResourceBundleReader}s for them.
     *
     * @param  baseName            the base name
     * @param  locale              the locale
     * @param mf                   the metafile
     * @param rbr                  the bundle reader
     * @return                     created {@link AppResourceManager} object
     */
    private ResourceManager getManager(String baseName, String locale,
                                       MetaFile mf, ResourceBundleReader rbr) {
        // locales
        Vector l = new Vector(5);
        // readers
        Vector r = new Vector(5);

        l.addElement(locale);
        r.addElement(rbr);
        locale = LocaleHelpers.getParentLocale(locale);

        /*
         * Go through all parent locales,
         * e.g. "en-US-var", "en-US", "en", "", null
         */
        while (locale != null) {
            if (mf.containsLocale(locale)) {
                try {
                    ResourceBundleReader reader =
                            getResourceBundleReaderForName(
                                    getResourceUrl(baseName, locale));
                    l.addElement(locale);
                    r.addElement(reader);
                } catch (ResourceException re_ignore) {
                /* intentionally ignored */
                /* For parent locales it does not throw exception in case any */
                /* problems. The exception is thrown for the current locale */
                /* only in the public getManager methods. */
                }
            }
            locale = LocaleHelpers.getParentLocale(locale);
        }

        String[] locales = new String[l.size()];
        ResourceBundleReader[] readers = new ResourceBundleReader[r.size()];
        l.copyInto(locales);
        r.copyInto(readers);
        // instantiate ResourceManager with supported locales

        return newResourceManagerInstance(baseName, locales, readers);
    }


    /**
     * Creates <code>ResourceManager</code> for given base name and locale.
     * Locale inheritance is resolved here. All parent locales are found and
     * <code>ResourceManager</code> is instantiated for the first locale that
     * is supported and contains any resources. Metafile is read for base name
     * and parent locales are matched with locales from the metafile.
     *
     * @param  baseName            the base name
     * @param  locale              the locale code
     * @return                     created {@link AppResourceManager} object
     * @throws  ResourceException  if resources are not found for any of
     *      the matched locales or resource file has invalid format.
     * @throws  UnsupportedLocaleException  if given base name
     *      supports neither requested locale nor any of its parent locales.
     */
    public ResourceManager getManager(String baseName, String locale)
             throws ResourceException, UnsupportedLocaleException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " + "getManager (" +
                           baseName + "," + locale + ")");
        }
        Vector l = new Vector(4);

        while (locale != null) {
            l.addElement(locale);
            locale = LocaleHelpers.getParentLocale(locale);
        }

        String[] locales = new String[l.size()];
        l.copyInto(locales);

        return getManager(baseName, locales);
    }


    /**
     * Create ResourceManager for given base name and list of locales. No
     * locale inheritance used here. Metafile is read for base name and locales
     * in array are matched with locales from metafile. Only those that match
     * are used.
     *
     * @param  baseName            the base name
     * @param  locales             array of locales
     * @return                     created {@link AppResourceManager} object
     * @throws  ResourceException  if no resources for the base name and any of
     *      the locales were found.
     * @throws  UnsupportedLocaleException  if no locales from the array are
     *      listed in metafile.
     */
    public ResourceManager getManager(String baseName, String[] locales)
             throws ResourceException, UnsupportedLocaleException {

        // read metafile
        MetaFile mf = getMetafileForBaseName(baseName);

        // match locales with those read from metafile
        for (int i = 0; i < locales.length; i++) {
            if (mf.containsLocale(locales[i])) {
                ResourceBundleReader reader = getResourceBundleReaderForName(
                        getResourceUrl(baseName, locales[i]));
                return getManager(baseName, locales[i], mf, reader);
            }
        }
        throw new UnsupportedLocaleException();
    }


    /**
     *  Get array of supported locales for given base name. It reads metafile
     *  for the base name.
     *
     * @param  baseName  the base name
     * @return           array of locales
     */
    public String[] getSupportedLocales(String baseName) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": getSupportedLocales");
        }
        if (baseName == null) 
            throw new NullPointerException("Basename is null.");
        
        MetaFile mf = getMetafileForBaseName(baseName);
        String[] locales = mf.toArray();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            for (int i = 0; i < locales.length; i++) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               locales[i]);
            }
        }
        return locales;
    }


    /**
     * Gives resource URL that can be used for method {@link
     * Class#getResourceAsStream}.
     *
     * @param  baseName  the base name
     * @param  locale    the locale
     * @return           resource URL
     */
    protected String getResourceUrl(String baseName, String locale) {
        String resourceUrl;
        resourceUrl = (locale != null && locale.length() > 0)
                 ? GLOBAL_PREFIX + locale + "/" + baseName
                 : GLOBAL_PREFIX + baseName;

        String url = resourceUrl + RESOURCE_FILE_EXTENSION;
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": url=" + url);
        }
        return url;

    }

    /**
     * Read metafile {@link MetaFile} for givane base name. Metafiles are
     * stored under global directory in form _&lt;base name&gt;.<br>
     *
     *
     * @param  baseName  the base name
     * @return           metafile for the base name
     */
    protected MetaFile getMetafileForBaseName(String baseName) {
        MetaFile mf;
        int hcode = resourceCache.getHashCodeForResource("_" + baseName, "", 0);
        Resource r = resourceCache.lookup(hcode);
        if (r == null) {
            try {
                mf = new MetaFile(GLOBAL_PREFIX + "_" + baseName);

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


    /**
     * Class MetaFile represents metafile for base name. Meta file is text file
     * which contains list of locales for base name. Locales are separated by
     * space (U+0020). Locales can be quoted. <br>
     * "" - empty string in locale list means that common resource bundle is
     * available for this base name.
     */
    public class MetaFile {

        /**
         * Approximate size used for storage of the object in cache.
         */
        int size = 0;

        /**
         * List of locales.
         */
        Vector locales = new Vector(5);


        /**
         * Create new metafile.
         *
         */
        private MetaFile() {
        }

        /**
         * Create new metafile.
         *
         * @param  filename         path to metafile
         * @exception  IOException  Description of the Exception
         * @throws  IOException     if filename can't be read or error occurs
         *      during read.
         */
        public MetaFile(String filename) throws IOException {
            InputStream is = getClass().getResourceAsStream(filename);
            if (is == null) {
                throw new IOException();
            }
            read(is);
        }


        /**
         * Create new metafile.
         *
         * @param  in               stream to read metafile from
         * @exception  IOException  Description of the Exception
         * @throws  IOException     if filename can't be read or error occurs
         *      during read.
         */
        public MetaFile(InputStream in) throws IOException {
            read(in);
        }

	/**
	 * Creates a copy of the instance.
	 * @return the instance copy.
	 */
        public Object clone() {
            MetaFile mf = new MetaFile();
            for (Enumeration e = locales.elements(); e.hasMoreElements(); ) {
                String locale = new String((String)e.nextElement());
                mf.locales.addElement(locale);
            }
            return mf;
        }

        /**
         * Check if given locale appears in metafile.
         *
         * @param  locale  the locale
         * @return         <code>true</code> if locale appeared in metafile
         */
        public boolean containsLocale(String locale) {
            String l;
            for (Enumeration e = locales.elements(); e.hasMoreElements(); ) {
                l = (String) e.nextElement();
                if (l.equals(locale)) {
                    return true;
                }
            }
            return false;
        }


        /**
         * Convert locales to string array.
         *
         * @return    array of locales
         */
        public String[] toArray() {
            String[] larr = new String[locales.size()];
            locales.copyInto(larr);
            return larr;
        }


        /**
         * Initialize metafile. Parse input stream.
         *
         * @param  in               Description of the Parameter
         * @exception  IOException  Description of the Exception
         */
        private void read(InputStream in) throws IOException {
            int BUFFER_LENGTH = 16;
            int readChars = 0;
            char[] buffer = new char[BUFFER_LENGTH];
            InputStreamReader iReader = new InputStreamReader(in);
            StringBuffer sb = new StringBuffer();
            do {
                readChars = iReader.read(buffer);
                if (readChars > 0) {
                    sb.append(buffer, 0, readChars);
                }
            } while (readChars >= 0);
            if (sb.length() == 0) {
                return;
            }
            if (sb.charAt(sb.length() - 1) != ' ') {
            	sb.append(' ');
            }

            boolean inside = false;
            StringBuffer sbuf = new StringBuffer();
            char c = ' ';
            char last = ' ';
            char prev = ' ';
            int sb_length = sb.length();
            for (int i = 0; i < sb_length; i++) {
                c = sb.charAt(i);
                if (c == ' ' || c == '"') {
                    if (inside) {
                        if (last == c) {
                            String locale;
                            if (sbuf.length() == 0) {
                                locale = "";
                            } else {
                                locale = sbuf.toString().trim();
                                if (!LocaleHelpers.isValidLocale(locale)) {
                                    throw new ResourceException(
                                            ResourceException.DATA_ERROR,
                                            "Invalid locale in metafile: \"" +
                                            locale + "\"");
                                }
                            }
                            locale = LocaleHelpers.normalizeLocale(locale);
                            if (!locales.contains(locale)) {
                                if (locale.length() == 0) {
                                    locales.insertElementAt(locale, 0);
                                } else {
                                    locales.addElement(locale);
                                }
                                size += sbuf.length();
                            }
                            sbuf.setLength(0);
                            inside = false;
                                
                        } else {
                            sbuf.append(c);
                        }
                    } else {
                        if (c == prev) {
                            throw new ResourceException(
                                      ResourceException.DATA_ERROR,
                                      "Invalid metafile format, double '" +
                                      c + "' detected");
                        }
                        last = c;
                        if (c == '"') {
                            inside = true;
                        }
                    }
                } else {
                    inside = true;
                    sbuf.append(c);
                }
                prev = c;
            }
            if (inside) {
                throw new ResourceException(ResourceException.DATA_ERROR,
                        "Unclosed quotation");
            }
        }

        /**
         * Get approximate size of metafile instance It counts only string
         * lengths. Necessary for use with {@link ResourceCache}
         *
         * @return    approximate size of MetaFile instance
         */
        public int getSize() {
            return size;
        }
    }
    // MetaFile

}
