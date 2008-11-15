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
import javax.microedition.global.ResourceException;
import javax.microedition.io.Connector;

/**
 *  An instance of this class is used to access device resource files.
 */
public class DevResourceBundleReader implements ResourceBundleReader {

    /**
     *  Locale index in the list of supported locales for specific
     *  <code>DevResourceBundleReader</code> instance.
     */
    private int locale_index;

    
    /**
     * Creates a new instance of <code>DevResourceBundleReader</code>.
     * @param locale_index  index of locale in array of supported locales
     */
    public DevResourceBundleReader(int locale_index) {
        this.locale_index = locale_index;
    }

    /**
     * Get raw binary data for resource id.
     * 
     * @param resourceID resource identifier
     * @return resource as array of bytes or <code>null</code> 
     * if resource wasn't found.
     * @throws ResourceException if resource couldn't be read
     */
    public byte[] getRawResourceData(int resourceID) {
        int rsize = getResourceLength(resourceID);
		if (rsize<0){
			throw new ResourceException(ResourceException.RESOURCE_NOT_FOUND,
					"Resource not found.");
		}
        byte[] res = new byte[rsize];
        if (rsize != getRawResourceData0(locale_index, resourceID, res, 0, rsize)) {
			throw new ResourceException(ResourceException.DATA_ERROR,
					"Could not read resource.");
        }
        return res;
    }

    /**
     * Get type of resource.
     *
     * @param resourceID resource identifier
     * @return resource type
     */
    public byte getResourceType(int resourceID) {
        int res_type = getResourceType0(locale_index, resourceID);
        if (res_type != -1) {
            return (byte)res_type;
        }
        throw new ResourceException(ResourceException.RESOURCE_NOT_FOUND,
                "Resource not found.");
    }

    /**
     * Checks if given resource id exits in the bundle.
     * 
     * @param resourceID resource identifier
     * @return <code>true</code> if resource of given id exists in bundle.
     */
    public boolean isValidResourceID(int resourceID) {
        return isValidResourceID0(locale_index, resourceID);
    }
    
    /**
     * Gives length of the resource in bytes.
     *
     * @param resourceID resource identifier
     * @return length of resource in bytes, <code>-1</code> if
     * resource doesn't exist in this bundle.
     */
    public int getResourceLength(int resourceID) {
        return getResourceLength0(locale_index, resourceID);
    }
    
    /**
     * Method returns name of resource file
     * used by this reader.
     * 
     * @return resource file name
     */
    public String getResourceName() {
        return new String("DEVICE");
    }

    /**
     * Method retrieves resource data for the given ID and locale.
     * 
     * @param resourceID    resource identifier
	 * @param res           byte array to store resource data
     * @param locale_index  index of locale in array of supported locales
	 * @param offset		offset of resource to start with
	 * @param length		length in bytes to copy
     * @return				length in bytes of copied data
     */
    private static native int getRawResourceData0(int locale_index,
						      int resourceID,
							  byte[] res,
							  int offset,
							  int length
							  );

    /**
     * Method retrieves String resiurce for the given ID and locale.
     * 
     * @param res           byte array to store resource data
     * @param resourceID    resource identifier
     * @param locale_index  index of locale in array of supported locales
     * @return <code>true</code> on success, <code>false</code> otherwise
     * /
    private static native String getStringResource0(int locale_index,
						      int resourceID);
	*/


    /**
     * Method retrieves resource type for the given ID and locale.
     * 
     * @param resourceID    resource identifier
     * @param locale_index  index of locale in array of supported locales
     * @return resource type (<code>0..FF</code>), <code>-1</code> if
     *      something is wrong
     */
    private static native int getResourceType0(int locale_index,
								int resourceID);

    /**
     * Determine if a resource with the given ID and locale exists.
     * 
     * @param resourceID    resource identifier
     * @param locale_index  index of locale in array of supported locales
     * @return <code>true</code> if resource with the given ID exists for
     *      the given locale, <code>false</code> otherwise
     */
    private static native boolean isValidResourceID0(int locale_index,
									int resourceID);

    /**
     * Method retrieves resource length for the given ID and locale.
     * 
     * @param resourceID    resource identifier
     * @param locale_index  index of locale in array of supported locales
     * @return length of the resource with the given ID and locale
     */
    private static native int getResourceLength0(int locale_index,
						 int resourceID);
}
