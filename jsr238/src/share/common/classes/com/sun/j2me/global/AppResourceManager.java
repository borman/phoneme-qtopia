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
import javax.microedition.global.ResourceException;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This class represents a resource manager for accessing application
 * resources.
 *
 */
public class AppResourceManager extends ResourceManager {

    /**
     * Class name
     */
    private static final String classname = AppResourceManager.class.getName();

    /**
     * Constant for string type resource.
     */
    public final static byte TYPE_STRING = (byte) 0x01;

    /**
     * Constant for binary type resource.
     */
    public final static byte TYPE_BINARY = (byte) 0x10;

    /**
     * Constant for end type.
     */
    public final static byte TYPE_END = (byte) 0x00;

    /**
     * Resource cache.
     *
     * @see    ResourceCache
     */
    protected ResourceCache resourceCache;

    /**
     * A resource bundle reader for accessing application resource files.
     *
     * @see    ResourceBundleReader
     */
    protected ResourceBundleReader[] bundleReaders;

    /**
     * Array of locales that is used for hierarchical matching of resources.
     */
    private String[] locales;


    /**
     * Creates a new instance of <code>AppResourceManager</code>. The new
     * instance can be used to get application resources of the given base name
     * and locale. The resource files will be accessed through the given
     * resource bundle readers.
     *
     * @param  base     the base name
     * @param  cache    cache implementation to speed up resource retrieval
     * @param  locales  array of locales to try
     * @param  readers  array of readers corresponding to locales
     */
    public AppResourceManager(String base, String[] locales,
                            ResourceBundleReader[] readers,
                            ResourceCache cache) {

        this.locales = locales;
        setBaseName(base);
        setLocale(locales[0]);
        bundleReaders = readers;
        resourceCache = cache;
    }


    /**
     * Constructor for <code>AppResourceManager</code> without caching.
     *
     * @param  base     the base name
     * @param  locales  array of locales to try
     * @param  readers  array of readers corresponding to locales
     */
    public AppResourceManager(String base,
                              String[] locales,
                              ResourceBundleReader[] readers) {
        this(base, locales, readers, null);
    }


    /**
     * Returns type of the given resource.
     *
     * @param  id                  The id of the resource
     * @return                     The type of resource
     * @throws  ResourceException  If resource wasn't found
     */
    protected byte getResourceType(int id) throws ResourceException {
        byte type;
        AppResourceBundleReader appReader;
        for (int i = 0; i < bundleReaders.length; i++) {
            appReader = (AppResourceBundleReader) bundleReaders[i];
            type = appReader.getResourceType(id);

            if (type == (byte) 0xff) {
                continue;
            } else {
                return type;
            }
        }
        // for
        throw new ResourceException(ResourceException.RESOURCE_NOT_FOUND,
                "Resource not found.");
    }


    /**
     * Retrieves binary resource in the form of a byte array.
     *
     * @param  id                     the id of the resource
     * @return                        resource as array of bytes
     * @exception  ResourceException  is thrown if type of resource isn't
     *      binary.
     */
    public byte[] getData(int id) throws ResourceException {
        Object o;
        try {
            o = getResource(id);
        } catch (ResourceException e) {
            int ec = e.getErrorCode();
            String em = e.getMessage();
            if (ec == ResourceException.UNKNOWN_RESOURCE_TYPE) {
                ec = ResourceException.WRONG_RESOURCE_TYPE;
                em = "Resource is not of type BINARY";
            }
            throw new ResourceException(ec, em);
        }
        if (!(o instanceof byte[])) {
            throw new ResourceException(ResourceException.WRONG_RESOURCE_TYPE,
                "Resource is not of type BINARY");
        }
        return (byte[]) o;
    }


    /**
     * Method returns resources of type string and binary as objects. Methods
     * {@link #getData} and {@link #getString} are routed via this method. <p>
     *
     * Resource cache is used to speed up resource retrieval if ResourceManager
     * was caching.
     *
     * @param  id                     resource id
     * @return                        The resource value
     * @exception  ResourceException  is thrown if type of resource isn't binary
     *                                nor string
     */
    public Object getResource(int id) throws ResourceException {
        if (id < 0) {
            throw new IllegalArgumentException("Illegal resource ID.");
        }

        int length = 0;
        Object resource = null;
        byte type = (byte) 255;
        ResourceBundleReader reader;
        int hashCode;
        Resource resInCache = null;

        for (int i = 0; i < bundleReaders.length; i++) {
            reader = (ResourceBundleReader) bundleReaders[i];
            try {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": getResource reader=" +
                                   reader.getResourceName());
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": " +
                                   " getResource for id \"" + id + "\"");
                }
                if (isCaching()) {
                    // check the cache for resource
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_JSR238,
                                       classname + ": Using caching instance.");
                    }
                    hashCode =
                          resourceCache.getHashCodeForResource(getBaseName(),
                                                               locales[i],
                                                               id);
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_JSR238,
                                       classname + ": hashCode=" + hashCode);
                    }
                    resInCache = resourceCache.lookup(hashCode);
                    if (resInCache != null) {
                        resource = resInCache.getValue();
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                           LogChannels.LC_JSR238,
                                           classname +
                                           ": Resource was in cache.");
                        }
                    } else {
                        // caching instance
                        if (reader.isValidResourceID(id)) {
                            type = reader.getResourceType(id);
                            length = reader.getResourceLength(id);
                            resource = convertToResourceType(id, type,
                                                             length, reader);
                            if (resource == null) {
                                if (Logging.REPORT_LEVEL <=
                                    Logging.INFORMATION) {
                                    Logging.report(Logging.INFORMATION,
                                            LogChannels.LC_JSR238,
                                            classname + ": Cannot " +
                                            "convert resource to object");
                                }
                                throw new ResourceException(ResourceException.
                                        UNKNOWN_RESOURCE_TYPE,
                                        "Resource type is unknown.");
                            }
                            resourceCache.addResource(new Resource(hashCode,
                                                                   length,
                                                                   resource));
                        } else {
                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                               LogChannels.LC_JSR238,
                                               classname + ": " +
                                               "Resource id = " + id +
                                               " is NOT valid.");
                            }
                            continue;
                        }
                    }
                } else {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_JSR238,
                                       classname + ": " +
                                       "Using NOTcaching instance.");
                    }
                    if (reader.isValidResourceID(id)) {
                        type = reader.getResourceType(id);
                        length = reader.getResourceLength(id);
                        resource = convertToResourceType(id, type,
                                                               length, reader);
                        if (resource == null) {
                            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                                Logging.report(Logging.WARNING,
                                               LogChannels.LC_JSR238,
                                               classname + ": Cannot " +
                                               "convert resource to object");
                            }
                            throw new ResourceException(ResourceException.
                                    UNKNOWN_RESOURCE_TYPE,
                                    "Resource type is unknown.");
                        }
                    } else {
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                           LogChannels.LC_JSR238,
                                           classname + ": " +
                                           "Resource id = " + id +
                                           " is NOT valid.");
                        }
                        continue;
                    }
                }

            } catch (IOException ioe) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                                   classname + ": " +
                                   "IOException: " + ioe.getMessage());
                }
                throw new ResourceException(ResourceException.DATA_ERROR,
                                            "Error reading resource");
            }

            if (resource == null) {
                throw new ResourceException(
                                ResourceException.UNKNOWN_RESOURCE_TYPE,
                                "Unknown resource type \"" + type + "\"");
            } else {
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": " +
                                   "Returning resource object " + resource);
                }
                return cloneResource(resource);
            }
        }// for

        throw new ResourceException(ResourceException.RESOURCE_NOT_FOUND,
                "Invalid resource id " + id + ".");
    }


    /**
     * Check if it is known resource type by this manager. Convert it to the
     * expected object. It recognizes {@link #TYPE_STRING} and {@link
     * #TYPE_BINARY} resource types.
     *
     * @param  resourceID       the resource identifier
     * @param  type             the resource type
     * @param  length           the resource length
     * @param  reader           resource bundle reader
     * @return                  resource as object
     *
     * @throws      IOException if read failed
     * @exception   ResourceException  exception is thrown when
     *              reading of resource has failed.
     */
    protected Object convertToResourceType(int resourceID,
                    byte type, int length,
                    ResourceBundleReader reader) throws IOException {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "converting resource id = \"" +
                           resourceID +
                           "\" to object" +
                           " type :\"" + type +
                           "\" with reader : " + reader);
        }
        byte[] data = null;
        if (length != 0) {
            data = reader.getRawResourceData(resourceID);
        } else {
            data = new byte[0];
        }
        if (type == TYPE_STRING) {
            try {
                Object o = getUTF8(data);
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": " +
                                   "data converted to utf8 string \"" +
                                   (String)o + "\"");
                }
                return o;
            } catch (IllegalArgumentException iae) {
                throw new IOException("Cannot convert string to UTF8\n" +
                                      iae.getMessage());
            }

        } else if (type == TYPE_BINARY) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": " +
                               "data not converted using pure binary");
            }
            return data;
        }
        return null;
    }

    /**
     * The method clones resource.
     *
     * @param resource the resource to clone
     * @return copy of resource
     */
    protected Object cloneResource(Object resource) {
        if (resource instanceof String) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": " +
                               "returning cloned string resource");
            }
            return new String((String)resource);
        } else if (resource instanceof byte[]) {
            byte[] arry = (byte[])resource;
            byte[] clone = new byte[arry.length];
            System.arraycopy(arry, 0, clone, 0, arry.length);
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                               classname + ": " +
                               "returning cloned binary resource");
            }
            return clone;
        }
        return resource;
    }


    /**
     * Retrieves string resource.
     *
     * @param  id                     the id of the resource
     * @return                        resource as string
     * @exception  ResourceException  is thrown if type of resource isn't
     *      string.
     */
    public String getString(int id) throws ResourceException {
        Object o;
        try {
            o = getResource(id);
        } catch (ResourceException e) {
            int ec = e.getErrorCode();
            String em = e.getMessage();
            if (ec == ResourceException.UNKNOWN_RESOURCE_TYPE) {
                ec = ResourceException.WRONG_RESOURCE_TYPE;
                em = "Resource is not of type STRING";
            }
            throw new ResourceException(ec, em);
        }
        if (!(o instanceof String)) {
            throw new ResourceException(ResourceException.WRONG_RESOURCE_TYPE,
                    "Resource is not of type STRING");
        }
        return (String) o;
    }


    /**
     * Check if <code>ResourceManager</code> implements caching.
     *
     * @return    <code>true</code> if cache is enabled.
     */
    public boolean isCaching() {
        return resourceCache != null;
    }


    /**
     * Check if resource with given id exists.
     *
     * @param  id  resource id
     * @return     <code>true</code> if resource exists
     */
    public boolean isValidResourceID(int id) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "validating resourceId=" + id);
        }

        if (id < 0) {
            throw new IllegalArgumentException("Illegal resource ID.");
        }

        try {
            return (null != getResource(id));
            
        } catch (ResourceException re) {
            return false;
        }
    }


    /**
     * Conversion of byte array to UTF-8 encoded string.
     *
     * @param  bytearr  bytes that make UTF-8 string
     * @return          UTF-8 encoded string
     */
    protected final String getUTF8(byte[] bytearr) throws IllegalArgumentException {
    		StringBuffer sb = new StringBuffer();
    		int count=0;
    		while (count<bytearr.length) {
    		
    		int utf32 = bytearr[count++];
    		
			int numOfBytes;
			if ((utf32 & 0x80) == 0) { sb.append((char) (utf32 & 0x7F));continue;} else
			if ((utf32 & 0xE0) == 0xC0) { numOfBytes = 2; utf32 &= ~0xFFFFFFE0; } else
			if ((utf32 & 0xF0) == 0xE0) { numOfBytes = 3; utf32 &= ~0xFFFFFFF0; } else
			if ((utf32 & 0xF8) == 0xF0) { numOfBytes = 4; utf32 &= ~0xFFFFFFF8; } else 
				throw new IllegalArgumentException("malformed input: around byte " + count);
			
			while (--numOfBytes != 0) {
				if (count==bytearr.length) throw new IllegalArgumentException("malformed input: partial character at end");
				int nextByte = bytearr[count++];
				if ( (nextByte & 0xC0) != 0x80) throw new IllegalArgumentException("malformed input: around byte " + count);
				utf32 <<= 6;
				utf32 += (nextByte & 0x3F);
			}
			
			if (utf32>=0xDC00 && utf32<=0xD800){
				throw new IllegalArgumentException("malformed input: invalid unicode character at pos " + (count-3));
			}
				
			if (utf32>0xFFFF) {
				//handle surrogates:
				int nextChar = 0xDC00 +  (utf32 & 0x3FF);
				utf32 = 0xD800 + ((utf32 - 0x10000) >> 10);
				sb.append((char)utf32);
				sb.append((char)nextChar);
			} else {
				sb.append((char)utf32);
			}
    	}
    		
    	return sb.toString();
    }
    	
}

