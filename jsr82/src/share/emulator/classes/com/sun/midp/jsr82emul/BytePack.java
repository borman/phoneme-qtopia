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
package com.sun.midp.jsr82emul;

/*
 * Utility class that packs various data to resores from byte array.
 * An instance refers to byte array that keeps packed data and passed 
 * to the instance thru either constructor or reset() method.
 */
public class BytePack {
    /* Current offset in byte array */
    protected int offset;
    /* Byte array that keeps packed data */
    protected byte[] buffer;
    
    /* 
     * Constructs an instance with given byte array. 
     * @param buffer byte array to either unpak data from or pack into.
     */
    public BytePack(byte[] buffer) {
        reset(buffer);
    }
    
    /* Constructs an instance. */
    public BytePack() {}
    
    /* 
     * Resets state, setting packed data destination to given array 
     * and current offset to 0. 
     * 
     * @param buffer new byte array to either unpak data from or pack into.
     */
    public void reset(byte[] buffer) {
        offset = 0;
        this.buffer = buffer;
    }
    
    /* Resets state, setting current offset to 0. */
    public void reset() {
        offset = 0;
    }
    
    /* 
     * Removes internal reference to current byte array, returning
     * the reference outside.
     * @return (reference to) byte array processed.
     */
    public byte[] release() {
        byte[] tmp = buffer;
        buffer = null;
        return tmp;
    }
    
    /*
     * Appends byte to packed data.
     * @param b byte to append.
     */
    public void append(byte b) {
        buffer[offset++] = b;
    }
    
    /*
     * Appends given bytes to packed data.
     * @param bytes bytes to append.
     */
    public void appendBytes(byte[] bytes) {
        System.arraycopy(bytes, 0, buffer, offset, bytes.length);
        offset += bytes.length;
    }
    
    /* 
     * Appends a 4-byte rpresentation of given integer value to packed data.
     * The value can be restored by extractInt() method.
     * @param value value to append
     */
    public void appendInt(int value) {
        for (int i = 0; i < 4; i++) {
            buffer[offset++] = (byte)value; 
            value >>= 8;
        }
    }
    
    /*
     * Extracts integer value previously stored by appendInt() from byte 
     * represnetation.
     * @return value extracted
     */
    public int extractInt() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (buffer[offset++] & 0xff) << (8 * i);
        }
        return value;
    }
    
    /*
     * Extracts next byte from packed data.
     * @return byte extracted.
     */
    public byte extract() {
        return buffer[offset++];
    }
    
    /*
     * Extracts next requested amount of bytes from packed data.
     * @param length amount of bytes to extract
     * @return (refernce to) newly created byte array that contains bytes
     *         extracted.
     */
    public byte[] extractBytes(int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(buffer, offset, bytes, 0, length);
        offset += length;
        return bytes;
    }
}
