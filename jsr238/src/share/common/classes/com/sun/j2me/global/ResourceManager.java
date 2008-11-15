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
package com.sun.j2me.global;

import com.sun.j2me.global.LocaleHelpers;
import com.sun.j2me.global.ResourceManagerFactory;
import com.sun.j2me.global.ResourceAbstractionLayer;
import javax.microedition.global.ResourceException;

/**
 * Resource manager functions implemetation. Provides a functionality of
 * {@link javax.microedition.global.ResourceManager}.
 * This class is required due to requirement do not have public/protected
 * constructor in javax.microedition.global.ResourceManager.
 * More detailed comments see for javax.microedition.global.ResourceManager.
 */
public class ResourceManager {

    /**
     * The instance of ResourceAbstractionLayer.
     */
    private static ResourceAbstractionLayer abstractionLayer =
            ResourceAbstractionLayer.getInstance();

    /**
     * Constant to indicate device resources.
     */
    public final static String DEVICE = "";

    /**
     * The base name.
     */
    private String baseName;
    /**
     * The locale identifier.
     */
    private String locale;

    // JAVADOC COMMENT ELIDED
    public final static ResourceManager getManager(String baseName,
            String locale) throws ResourceException {
        // DevResourceManager
        if (baseName.equals(DEVICE)) {
            ResourceManagerFactory devMFactory =
                    abstractionLayer.getDevResourceManagerFactory();
            return devMFactory.getManager(DEVICE, locale);
        }
        // appResourceManager
        else {
            ResourceManagerFactory appMFactory =
                    abstractionLayer.getAppResourceManagerFactory();
            return appMFactory.getManager(baseName, locale);
        }
    }

    // JAVADOC COMMENT ELIDED
    public final static ResourceManager getManager(String baseName,
            String[] locales) {

        String[] norm_locs = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            norm_locs[i] = LocaleHelpers.normalizeLocale(locales[i]);
        }

        // DevResourceManager
        if (baseName.equals(DEVICE)) {
            ResourceManagerFactory devMFactory =
                    abstractionLayer.getDevResourceManagerFactory();
            return devMFactory.getManager(DEVICE, norm_locs);
        }
        // appResourceManager
        else {
            ResourceManagerFactory appMFactory =
                    abstractionLayer.getAppResourceManagerFactory();
            return appMFactory.getManager(baseName, norm_locs);
        }

    }

    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedLocales(String baseName) {
        if (baseName == null) {
            throw new NullPointerException("Base name is null");
        }

        // DevResourceManager
        if (baseName.equals(DEVICE)) {
            ResourceManagerFactory devMFactory =
                    abstractionLayer.getDevResourceManagerFactory();
            return devMFactory.getSupportedLocales(baseName);
        }
        // appResourceManager
        else {
            ResourceManagerFactory appMFactory =
                    abstractionLayer.getAppResourceManagerFactory();
            return appMFactory.getSupportedLocales(baseName);
        }
    }

    /**
     * Creates a new instance of <code>ResourceManager</code>.
     */
    protected ResourceManager() { }

    /**
     * Sets base name for resource files used by this
     * <code>ResourceManager</code>.
     *
     * @param baseName  the base name, non-empty for application
     */
    protected void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Sets locale code for this <code>ResourceManager</code>.
     *
     * @param locale  the locale code
     */
    protected void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Gets the base name of this resource manager.
     *
     * @return    the base name (<code>DEVICE</code> if this resource manager is
     *      retrieving device-specific resources)
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Gets current locale of this <code>ResourceManager</code>.
     *
     * @return    the locale identifier
     */
    public String getLocale() {
        return locale;
    }

    // JAVADOC COMMENT ELIDED
    public byte[] getData(int id) throws ResourceException {
        throw new ResourceException(ResourceException.UNKNOWN_ERROR, "");
    }

    // JAVADOC COMMENT ELIDED
    public String getString(int id) throws ResourceException {
        throw new ResourceException(ResourceException.UNKNOWN_ERROR, "");
    }

    // JAVADOC COMMENT ELIDED
    public Object getResource(int id) throws ResourceException {
        throw new ResourceException(ResourceException.UNKNOWN_ERROR, "");
    }

    // JAVADOC COMMENT ELIDED
    public boolean isCaching() {
        return false;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isValidResourceID(int id) {
        return false;
    }
}
