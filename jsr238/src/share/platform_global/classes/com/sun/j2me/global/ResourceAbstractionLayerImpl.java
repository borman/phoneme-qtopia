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

/**
 * This class customizes the MIA resources implementation from the
 * <code>com.sun.j2me.global</code> package to access device resources through
 * native function calls. Application resources handling functionality of
 * {@link com.sun.j2me.global.AppResourceManager} is reused.
 */
public class ResourceAbstractionLayerImpl extends ResourceAbstractionLayer {
    /**
     * Maximum cache size.
     */
    private final static int MAX_CACHE_SIZE = 250;
    // kB

    /**
     * Maximum cached resource size.
     */
    private final static int MAX_RESOURCE_SIZE = 25;
    // kB

    /**
     * Use cached common resources.
     * This implementation alows to
     * set limit for maximum cache size in kB
     * and maximum resource size in kB.
     */
    private final static ResourceCache commonResourceCache =
            new ResourceCacheLRUImpl(MAX_CACHE_SIZE,
            MAX_RESOURCE_SIZE);

    /**
     * Use cached device resources.
     * This implementation alows to
     * set limit for maximum cache size in kB
     * and maximum resource size in kB.
     */
    private final static ResourceCache deviceResourceCache = 
            new ResourceCacheLRUImpl(MAX_CACHE_SIZE,
            MAX_RESOURCE_SIZE);

    /**
     * A resource manager factory for creating device resource managers.
     */
    private ResourceManagerFactory devResourceManagerFactory;

    /**
     * A resource manager factory for creating application resource managers.
     */
    private ResourceManagerFactory appResourceManagerFactory;


    /**
     * Create instance of <code>ResourceAbstractionLayerImpl</code>. This
     * constructor is necessary because of <code>Class.forName()</code> creation
     * call in {@link com.sun.j2me.global.ResourceAbstractionLayer#getInstance}.
     */
    public ResourceAbstractionLayerImpl() { }


    /**
     * Create device resource manager factory.
     *
     * @return    instance of <code>DevResourceManagerFactory</code>
     *      with caching enabled
     */
    public ResourceManagerFactory getDevResourceManagerFactory() {
        if (devResourceManagerFactory == null) {
            devResourceManagerFactory = 
            new DevResourceManagerFactory(deviceResourceCache);
        }
        return devResourceManagerFactory;
    }


    /**
     * Create application resource manager factory.
     *
     * @return    instance of <code>AppResourceManagerFactory</code>
     *      with caching enabled
     */
    public ResourceManagerFactory getAppResourceManagerFactory() {
        if (appResourceManagerFactory == null) {
            appResourceManagerFactory = 
            new AppResourceManagerFactory(commonResourceCache);
        }
        return appResourceManagerFactory;
    }
}
