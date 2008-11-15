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
package javax.microedition.global;

import com.sun.j2me.global.LocaleHelpers;
import com.sun.j2me.global.ResourceManagerFactory;
import com.sun.j2me.global.ResourceAbstractionLayer;
import javax.microedition.global.ResourceException;

// JAVADOC COMMENT ELIDED
public class ResourceManager {

    /** The instance of ResourceAbstractionLayer. */
    private static ResourceAbstractionLayer abstractionLayer =
            ResourceAbstractionLayer.getInstance();

    /** Constant to indicate device resources. */
    public final static String DEVICE = "";

    /** The base name. */
    private String baseName;
    /** The locale identifier. */
    private String locale;

    /** The resource manager object. */
    private com.sun.j2me.global.ResourceManager rm;

    // JAVADOC COMMENT ELIDED
    public final static ResourceManager getManager(String baseName)
             throws ResourceException {
        // get system locale
        String locale = System.getProperty("microedition.locale");
        if (locale == null) {
            throw new ResourceException(
                    ResourceException.NO_SYSTEM_DEFAULT_LOCALE,
                    "System default locale is undefined");
        }
        return getManager(baseName, locale);
    }

    // JAVADOC COMMENT ELIDED
    public final static ResourceManager getManager(String baseName,
            String locale) throws ResourceException {
        if (baseName == null) {
            throw new NullPointerException("Base name is null");
        }

        if (locale == null) {
            throw new NullPointerException("Locale is null");
        }

        if (!LocaleHelpers.isValidLocale(locale) && !("".equals(locale))) {
            throw new IllegalArgumentException("Invalid locale format");
        }

        locale = LocaleHelpers.normalizeLocale(locale);

        return new ResourceManager(
            com.sun.j2me.global.ResourceManager.getManager(baseName, locale));
    }

    // JAVADOC COMMENT ELIDED
    public final static ResourceManager getManager(String baseName,
            String[] locales) {

        if (baseName == null) {
            throw new NullPointerException("Base name is null");
        }

        if (locales == null) {
            throw new NullPointerException("Locales array is null");
        }

        if (locales.length == 0) {
            throw new IllegalArgumentException("Empty locales array");
        }

        for (int i = 0; i < locales.length; i++) {
            if (locales[i] == null) {
                throw new NullPointerException("Locale at position " +
                                                i + " is null");
            }
            if (!LocaleHelpers.isValidLocale(locales[i]) &&
                !("".equals(locales[i]))) {
                throw new IllegalArgumentException("Locale at position " +
                                                    i + " has invalid format");
            }
        }

        return new ResourceManager(
            com.sun.j2me.global.ResourceManager.getManager(baseName, locales));
    }

    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedLocales(String baseName) {
        if (baseName == null) {
            throw new NullPointerException("Base name is null");
        }
        return com.sun.j2me.global.ResourceManager.
                                   getSupportedLocales(baseName);
    }

    /**
     * Creates a new instance of <code>ResourceManager</code>.
     * @param rm       the ResourceManager functionality object
     */
    private ResourceManager(com.sun.j2me.global.ResourceManager rm) {
        this.rm = rm;
    }

    // JAVADOC COMMENT ELIDED
    public String getBaseName() {
        return rm.getBaseName();
    }

    // JAVADOC COMMENT ELIDED
    public String getLocale() {
        return rm.getLocale();
    }

    // JAVADOC COMMENT ELIDED
    public byte[] getData(int id) throws ResourceException {
        return rm.getData(id);
    }

    // JAVADOC COMMENT ELIDED
    public String getString(int id) throws ResourceException {
        return rm.getString(id);
    }

    // JAVADOC COMMENT ELIDED
    public Object getResource(int id) throws ResourceException {
        return rm.getResource(id);
    }

    // JAVADOC COMMENT ELIDED
    public boolean isCaching() {
        return rm.isCaching();
    }

    // JAVADOC COMMENT ELIDED
    public boolean isValidResourceID(int id) {
        return rm.isValidResourceID(id);
    }
}
