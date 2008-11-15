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

/**
 * Interface for Resource cache.
 * Defines methods which resource cache must implement.
 * For default implementation see {@link ResourceCacheLRUImpl}.
 */
public interface ResourceCache {
    
    /** 
     * Lookup resource in cache. Key is hashcode of 
     * the Resource calculated by {@link #getHashCodeForResource}
     *
     * @param hashCode has code for resource used as key
     * @return resource or <code>null</code> if resource wasn't in cache
     */
    public Resource lookup(int hashCode);
    
    /**
     * Add Resource into cache.
     *
     * @param resource the Resource to add
     * @return <code>true</code> if call succeeded and resource was added.
     */
    public boolean addResource(Resource resource);
    
    /**
     * Calculate key to resource cache.
     * It's calculated from base name, locale, resource id.
     *
     * @param base    the base name
     * @param locale  the locale
     * @param id      the resource id
     * @return hash code key for resource in cache
     */
    public int getHashCodeForResource(String base, String locale, int id);
}
