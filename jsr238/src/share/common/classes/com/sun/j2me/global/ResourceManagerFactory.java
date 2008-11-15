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

/**
 *  This class represents a factory for creating resource managers.
 *
 */
public abstract class ResourceManagerFactory {

    /**
     *  Every application resource file is under global directory.
     */
    protected final static String GLOBAL_PREFIX = "/global/";

    /**
     * Resource file extension constant.
     */
    protected final static String RESOURCE_FILE_EXTENSION = ".res";


    /**
     *  Creates a new instance of ResourceManagerFactory.
     */
    public ResourceManagerFactory() { }


    /**
     *  Returns an instance of <code>ResourceManager</code> class for the given
     *  base name and system's default locale.
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
    public abstract ResourceManager getManager(String baseName)
             throws ResourceException;


    /**
     *  Creates an instance of <code>ResourceManager</code> class for the given
     *  base name and locale.
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
    public abstract ResourceManager getManager(String baseName, String locale)
             throws ResourceException;


    /**
     *  Creates an instance of <code>ResourceManager</code> class for the given
     *  base name and the first matching locale in the supplied array.
     *
     * @param  baseName                     the base name
     * @param  locales                      the array of locale
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
    public abstract ResourceManager getManager(String baseName,
                                               String[] locales)
             throws ResourceException;


    /**
     *  Returns a list of locales supported by the given baseName. A resource
     *  manager can be constructed for each locale from the list and the base
     *  name.
     *
     * @param  baseName  the base name
     * @return           the list of the supported locales
     */
    public abstract String[] getSupportedLocales(String baseName);
}
