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
import java.io.OutputStream;
import javax.microedition.global.ResourceException;

/**
 * Interface for reading and writing resource custom objects.
 * Each custom object that can be used in device resources must
 * implement this interface. At least read method. 
 */
public interface SerializableResource {
    
        /**
         * Read resource from stream.
         *
         * @param in input stream to be read
         * @throws IOException thrown when read failed.
         * @throws ResourceException thrown when resource can't be read.
         */
        public void read(InputStream in) throws IOException,
                                                ResourceException;
        /**
         * Write resource to stream.
         *
         * @param out output stream to write to
         * @throws IOException thrown when write failed
         * @throws ResourceException thrown when resource can't be written.
         */
        public void write(OutputStream out) throws IOException, 
                                                   ResourceException;
        
        /**
         * Has to implement clone method to 
         * allow of making copy of object.
         *
         * @return object copy
         */
        public Object clone();
}
