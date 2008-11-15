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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.global.ResourceException;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;


/**
 * An instance of this class can be used for accessing application resource
 * files.
 *
 */
public class AppResourceBundleReader implements ResourceBundleReader {

    /**
     * Class name
     */
    private static final String classname = 
                AppResourceBundleReader.class.getName();
    
    /**
     * Array of resource file versions supported by this reader.
     */
    protected byte[] supportedVersions = new byte[] {(byte) 0x10};
    
    /**
     * ID of the last entry in the offset table.
     */
    protected long LASTRESOURCE_ID = 0x80000000;

    /**
     * Type of the last entry in the offset table.
     */
    protected byte LASTRESOURCE_TYPE = 0x00;
    

    /**
     * The name of resource.
     */
    protected String resourceName;

    /**
     * The binary resource file header.
     */
    protected Header header;

    /**
     * The stream to read resource.
     */
    protected InputStream istream;


    /**
     * Creates initialized instance of ResourceBundleReader It opens resource
     * bundle and reads up header.
     *
     * @return      An initialized instance of ResourceBundleReader, 
     * <code>null</code> if resource bundle can't be read.
     * @param  name path to resource bundle
     */
    public static ResourceBundleReader getInstance(String name) {
        AppResourceBundleReader appreader = new AppResourceBundleReader();
        if (!appreader.initialize(name)) {
            return null;
        }
        return appreader;
    }


    /**
     * Creates new instance of AppResourceBundle. Always use {@link
     * #getInstance} method.
     */
    protected AppResourceBundleReader() { }


    /**
     * Opens resource bundle and return its stream.
     *
     * @return    stream for reading resource bundle or <code>null</code> if
     *     stream can't be opened.
     */
    protected InputStream getResourceBundleAsStream() {
        return getClass().getResourceAsStream(resourceName);
    }

    /**
     * Closes resource bundle.
     * @throws  IOException  if istream.close is unsuccessful.
     */
    protected void freeResourceBundle(){
    	try {
    		istream.close();
    	} catch (IOException ioe){
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                               classname + "Exception while closing resource stream: "
                               + ioe.toString());
            }
    	}
    }

    /**
     * Creates a new instance of <code>AppResourceBundleReader</code>.
     *
     * @param  name  path to resource bundle
     * @return       <code>true</code> if header was initialized
     */
    protected boolean initialize(String name) {
        resourceName = name;
        header = readHeader();
        return (header != null);
    }


    /**
     * Method returns name of resource file
     * used by this reader.
     * 
     * @return resource file name
     */
    public String getResourceName() {
        return resourceName;
    }
    

    /**
     * Method checks if given resource id was valid.
     *
     * @param  resourceID  the resource id
     * @return             <code>true</code> if resource id was found in header.
     */
    public synchronized boolean isValidResourceID(int resourceID) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": validating resourceId=" + resourceID);
        }
        int index = header.getEntryIndex(resourceID);
        return (index >= 0);
    }


    /**
     * Method finds out resource type.
     *
     * @param  resourceID  the resource id
     * @return             type code.<code>255</code>if resource wasn't found
     */
    public synchronized byte getResourceType(int resourceID) {
        long entry = header.getEntry(resourceID);
        if (entry == 0) {
            throw new ResourceException(ResourceException.RESOURCE_NOT_FOUND,
                                        "Cannot get resource type");
        }
        // flag that resource wasn't found
        byte type = header.getResourceType(entry);
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": resource type is " + type);
        }
        return type;
    }


    /**
     * Method gets length of resource.
     *
     * @param  resourceID  the resource id
     * @return             resource length in bytes, or <code>-1</code> if
     *      resource wasn't found.
     */
    public synchronized int getResourceLength(int resourceID) {
        int lth = header.getResourceLength(resourceID);
        return lth;
    }

    /**
     * Method moves stream into position of given resource.
     * 
     * @param resourceID the resource id
     */
    protected synchronized void moveStreamTo(int resourceID) {
        try {
            long entry = header.getEntry(resourceID);
            if (entry == 0) {
                throw new ResourceException(
                          ResourceException.RESOURCE_NOT_FOUND, 
                          "Can't find resource \"" + resourceID + "\""); 
            }
            int offset = header.getResourceOffset(entry);
            // reopen stream
            freeResourceBundle();
            istream = getResourceBundleAsStream();
            // move to resource position
            long skipped=istream.skip(offset);
            if (skipped!=offset){
                throw new ResourceException(ResourceException.DATA_ERROR,
                        "Invalid offset of resource " + resourceID);
            }
        } catch (IOException ioe) {
            throw new ResourceException(ResourceException.DATA_ERROR,
                    ioe.getMessage());
        }
    }

    /**
     * Get raw binary data for resource id.
     *
     * @param  resourceID  the resource identifier
     * @return             resource as array of bytes or <code>null</code> if
     *      resource wasn't found.
     */
    public synchronized byte[]
            getRawResourceData(int resourceID) {

        try {
            int length = header.getResourceLength(resourceID);
            if (length < 0) {
                throw new ResourceException(ResourceException.DATA_ERROR,
                        "Invalid resource length " + length);
            }
            byte[] buffer = new byte[length];
            if (length > 0){
                moveStreamTo(resourceID);
                int pos = 0;
                while (length > 0){
                	int bytesRead = istream.read(buffer,pos,length);
                	if (pos < 0){
                		throw new ResourceException(ResourceException.DATA_ERROR,
                                            "End of file.");
                	}
                	pos +=  bytesRead;
                	length -= bytesRead;
                }
            	freeResourceBundle();
           	}
            return buffer;

        } catch (IOException ioe) {
            throw new ResourceException(ResourceException.DATA_ERROR,
                    ioe.getMessage());
        }
    }

    /**
     * Reads header of resource file.
     *
     * @return                     initialized {@link Header}
     * @throws  ResourceException  if bundle file version is invalid.
     */
    protected Header readHeader() throws ResourceException {

        try {
            Header header = new Header();
            istream = getResourceBundleAsStream();
            if (istream == null) {
                throw new ResourceException(
                        ResourceException.NO_RESOURCES_FOR_BASE_NAME,
                        "Resource file not found.");
                // bundle can't be read
            }
            
            istream.read(header.getSignature());
            if (!header.isSignatureValid()) {
                throw new ResourceException(ResourceException.DATA_ERROR,
                        "Invalid resource file.");
            }
            int headerLength = readInt(istream);
            if ((headerLength <= 0) || ((headerLength & 0x7) != 0)){
                throw new ResourceException(ResourceException.DATA_ERROR,
                "Invalid resource file.");
            }
            int entriesCount = headerLength >> 3; // /8

            long[] entries;
            /* Check if the entriesCount is too large */
            try {
                entries = new long[entriesCount];
            } catch (OutOfMemoryError e) {
                throw new ResourceException(ResourceException.DATA_ERROR,
                                            "Out of memory.");
            }
            
            int prevID = -1;
            int prevOffset = headerLength + 8;
            for (int i = 0; i < entriesCount; ++i) {
            	entries[i] = readLong(istream);

                int id = header.getResourceId(entries[i]);
                int type = header.getResourceType(entries[i]);
                
                if (i == (entriesCount-1)) {
                	// terminator resource
	                if (id != LASTRESOURCE_ID){
	                    throw new ResourceException(ResourceException.DATA_ERROR,
	                            "Last entry should have ID " + LASTRESOURCE_ID + " but found ID " + id);
	                }
	                if (type != LASTRESOURCE_TYPE){
	                    throw new ResourceException(ResourceException.WRONG_RESOURCE_TYPE,
	                            "Last entry shoud have type " + LASTRESOURCE_TYPE + " but found type " + type);
	                }
	            }  else {
	                if (id<0){
	                    throw new ResourceException(ResourceException.DATA_ERROR,
	                    "Negative resource ID " + id);
	                }
	                if (id==prevID){
	                    throw new ResourceException(ResourceException.DATA_ERROR,
	                            "Duplicated id: " + id);
	                }
	                if (id<prevID){
	                    throw new ResourceException(ResourceException.DATA_ERROR,
	                            "Resources are not in ascending order: " + prevID + "," + id);
	                }
	                prevID = id;
	                
	                if (type == LASTRESOURCE_TYPE){
	                    throw new ResourceException(ResourceException.WRONG_RESOURCE_TYPE,
	                            "Only last entry can have type " + LASTRESOURCE_TYPE);
	                }
	            }
                
                int offset = header.getResourceOffset(entries[i]);
                
                if (offset < prevOffset) {
                    throw new ResourceException(ResourceException.DATA_ERROR,
                            "Invalid resource offset: " + id);
                }
                
                prevOffset = offset;
            }
            long dataLength = prevOffset - headerLength - 8;
            if (dataLength!=istream.skip(dataLength)){
                throw new ResourceException(ResourceException.DATA_ERROR,
                        "Resource file too short");
            }
            header.setEntries(entries);
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
	                    classname + " Found "+ entries.length + " resource enries in " + resourceName);
            }
            freeResourceBundle();
            return header;
        } catch (IOException ioe) {
            throw new ResourceException(ResourceException.DATA_ERROR,
                    ioe.getMessage());
        } 
    }


    /**
     * Read integer from resource bundle stream.
     *
     * @param  in            resource bundle input stream
     * @return               integer read from resource bundle
     * @throws  IOException  if error occured while reading
     */
    protected final int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * Read long from resource bundle stream.
     *
     * @param  in            resource bundle input stream
     * @return               integer read from resource bundle
     * @throws  IOException  if error occured while reading
     */
    protected final long readLong(InputStream in) throws IOException {
    	long result=0;
    	for (int j = 0; j < 8; ++j) {
			int i = in.read();
			if (i < 0) {
				throw new EOFException();
			}
			result <<= 8;
			result += (long)(i & 0xFF);
		}
    	return result;
    }


    /**
     * Binary resource file header. Header consists of:<br>
     *
     * <ul>
     *   <li> 4-byte signature
     *   <li> header length
     *   <li> file header entries
     * </ul>
     *
     * See "The binary resource file format" in the JSR specification for
     * more details.
     */
    protected class Header {

        /**
         * Signature is four bytes 0xEE 0x4D 0x49 version.
         */
        private byte[] signature = new byte[4];

        /**
         * Entries list.
         */
        private long[] entries;


        /**
         * Create uninitialized Header.
         */
        Header() { }


        /**
         * Initialize Header with entries.
         *
         * @param  entries  array of entries
         */
        void setEntries(long[] entries) {
            this.entries = entries;
        }


        /**
         * Get entry value of given id.
         *
         * @param  id  of the entry
         * @return     entry or <code>0</code> if entry wasn't found.
         */
        long getEntry(int id) {
            int index = getEntryIndex(id);
            return index > -1 ? entries[index] : 0;
        }


        /**
         * Get index of the entry in entries list.
         * All ID's are in ascending order.
         * @param  id  entry id
         * @return     index of entry or <code>-1</code> if entry wasn't found.
         */
        int getEntryIndex(int id) {
            int nextID = -1;
            for (int i = 0; i < entries.length; ++i){
            	nextID = getResourceId(entries[i]);
            	if (nextID < id) continue;
            	if (id == nextID) return i;
            	break;
            } 
            return -1;
        }

        /**
         * Check if signature of resource bundle was valid.
         * Expects version 1.0.
         *
         * @return    The signatureValid value
         */
        boolean isSignatureValid() {
            boolean sigIsValid = ((
                    (signature[0] << 24) |
                    (signature[1] << 16) |
                    (signature[2] << 8) |
                    (byte) 0) == 0xee4d4900);

            if (sigIsValid) {
                for (int i = 0; i < supportedVersions.length; i++) {
                    if (signature[3] == supportedVersions[i]) {
                        return true;
                    }
                }
            }
            return false;
        }


        /**
         * Get signature of this resource bundle.
         *
         * @return    4 bytes signature
         */
        byte[] getSignature() {
            return signature;
        }


        /**
         * Calculates resource length.
         *
         * @param  id  the resource id
         * @return     length of resource in bytes
         */
        int getResourceLength(int id) {
            int index = getEntryIndex(id);
            if (-1 == index) {
                return -1;
            }
            int length = getResourceOffset(entries[index + 1]) -
                    getResourceOffset(entries[index]);
            return length;
        }


        /**
         * Get resource id.
         *
         * @param  entry  the entry
         * @return        resource identifier
         */
        int getResourceId(long entry) {
            return (int) ((entry >>> 32) & 0xffffffff);
        }


        /**
         * Get resource type.
         *
         * @param  entry  the entry
         * @return        resource type
         */
        byte getResourceType(long entry) {
            return (byte) ((entry >> 24) & 0xff);
        }


        /**
         * Get offset of the entry.
         *
         * @param  entry  the entry
         * @return        offset from the beginning of resource bundle
         */
        int getResourceOffset(long entry) {
            return (int) ((entry & 0xffffff));
        }
    }
}

