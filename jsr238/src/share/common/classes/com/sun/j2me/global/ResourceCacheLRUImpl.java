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

import java.util.Vector;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 *  {@link ResourceCache} implementation. Implements LRU algorithm. Cache is
 *  created with parameters max cache capacity [Kb] and max allowed resource
 *  size [kB]. Cache cannot grow over max cache capacity and if resource is
 *  bigger then max allowed resource size it isn't stored in cache.
 *
 */
public class ResourceCacheLRUImpl implements ResourceCache {

    /**
     * Class name.
     */
    private static final String classname = 
                                ResourceCacheLRUImpl.class.getName();
    
    /**
     * Storage for resources.
     */
    private static Vector cache = new Vector();

    /**
     * Maximum cache capacity in KB.
     */
    private static int maxCacheCapacity;

    /**
     * Maximum size of resource in KB.
     */
    private static int maxAllowedResourceSize;

    /**
     * Current cache size in bytes.
     */
    private static int cacheSize = 0;


    /**
     * Constructor creates cache initialized with capacity and max resource
     * size.
     *
     * @param  capacityKB       max cache capacity in KB
     * @param  maxResourceSize  maximum size of resource in KB
     */
    public ResourceCacheLRUImpl(int capacityKB, int maxResourceSize) {
        this.maxCacheCapacity = capacityKB * 1024;
        this.maxAllowedResourceSize = maxResourceSize * 1024;
    }


    /**
     * Add resource into cache. Size of resource is checked against
     * maxResourceSize and cache capacity. Only if resource doesn't exceed
     * maxResourceSize it's stored in cache. If adding resource would exceeed
     * max cache size, resources are removed from cache until the resource
     * could have be en stored.
     *
     * @param  o  The feature to be added to the Resource attribute
     * @return    <code>true</code> if resource was stored in cache.
     */
    public synchronized boolean addResource(Resource o) {
        if (o.getSize() > maxAllowedResourceSize) {
            return false;
        }
        while (cacheSize + o.getSize() > maxCacheCapacity) {
            cacheSize -= ((Resource) cache.elementAt(0)).getSize();
            cache.removeElementAt(0);
            // remove last used element
        }
        // while
        cache.addElement(o);
        cacheSize += o.getSize();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "\nCache: " + this +
                           "\nResource added to cache\n" +
                           "\nCache size = " + cacheSize);
        }
        return true;
    }


    /**
     * Resource is looked up in cache. If resource was found it is returned.
     * Otherwise <code>null</code> is returned.
     *
     * @param  hashCode  Description of the Parameter
     * @return           resource if it was found or <code>null</code> if
     *                   resource wasn't found.
     */
    public synchronized Resource lookup(int hashCode) {
        Resource o = null;
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "\nCache: " + this +
                           "\nLooking up resource with key=" + hashCode);
        }
        for (int i = cache.size() - 1; i >= 0; i--) {
            o = (Resource) cache.elementAt(i);
            if (cache.elementAt(i).hashCode() == hashCode) {
                // move element to front
                cache.removeElementAt(i);
                cache.addElement(o);
                return o;
            }
        }
        // for
        return null;
    }


    /**
     * Calculates hash code for the resource.
     *
     * @param  base    the base name
     * @param  locale  the locale
     * @param  id      the resource id
     * @return         hash code for resource
     */
    public int getHashCodeForResource(String base, String locale, int id) {
        int hashCode = new String(locale + 
                                  base + 
                                  Integer.toString(id)).hashCode();
        return hashCode;
    }
}

