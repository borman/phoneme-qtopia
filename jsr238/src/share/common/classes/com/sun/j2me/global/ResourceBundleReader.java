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
 * An interface for accessing resource files.
 */
public interface ResourceBundleReader {
    
    /**
     * Get raw binary data for resource id.
     * 
     * @param resourceID resource identifier
     * @return resource as array of bytes or <code>null</code> 
     * if resource wasn't found.
     * @throws ResourceException if resource couldn't be read
     */
    public byte[] getRawResourceData(int resourceID);
    
    /**
     * Get type of resource.
     *
     * @param resourceID resource identifier
     * @return resource type
     */
    public byte getResourceType(int resourceID);
    
    /**
     * Checks if given resource id exits in the bundle.
     * 
     * @param resourceID resource identifier
     * @return <code>true</code> if resource of given id exists in bundle.
     */
    public boolean isValidResourceID(int resourceID);
    
    /**
     * Gives length of the resource in bytes.
     *
     * @param resourceID resource identifier
     * @return length of resource in bytes, <code>-1</code> if
     * resource doesn't exist in this bundle.
     */
    public int getResourceLength(int resourceID);
    
    /**
     * Method returns name of resource file
     * used by this reader.
     * 
     * @return resource file name
     */
    public String getResourceName();
}
