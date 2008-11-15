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
 *  Wrapper around any object to allow store it as resource in ResourceCache.
 *  HashCode of the Resource instance has to be replaced with precalculated
 *  integer value this is key in the cache. ResourceCache itself is responsible
 *  for calculating hash code {@link ResourceCache#getHashCodeForResource}. It
 *  is always calculated from basename, locale, and id.
 *
 */
public class Resource {

    /**
     *  Hashcode identifying instance in cache
     */
    private int code;
    /**
     *  Size of the object to store
     */
    private int size;
    /**
     *  Description of the data to store in cache
     */
    private Object value;


    /**
     *  Creates Resource initialized.
     *
     * @param  code   hashcode identifying instance in cache
     * @param  size   size of the object to store
     * @param  value  Description of the data to store in cache
     */
    public Resource(int code, int size, Object value) {
        this.code = code;
        this.size = size;
        this.value = value;
    }


    /**
     *  Resources are compared using code field.
     *
     * @param  o  another Resource to compare to current
     * @return    <code>true</code> if objects equals
     */
    public boolean equals(Object o) {
        if (o instanceof Resource) {
            return ((Resource) o).getCode() == code;
        }
        return false;
    }


    /**
     *  get hash code of this instance.
     *
     * @return    hash code of the instance
     */
    public int hashCode() {
        return code;
    }


    /**
     *  get code of this resource.
     *
     * @return    the code of resource
     */
    public int getCode() {
        return code;
    }


    /**
     *  get size of the resource instance.
     *
     * @return    sie of the instance in bytes
     */
    public int getSize() {
        return size;
    }


    /**
     *  Get data stored under this resource.
     *
     * @return    resource data
     */
    public Object getValue() {
        return value;
    }

}

