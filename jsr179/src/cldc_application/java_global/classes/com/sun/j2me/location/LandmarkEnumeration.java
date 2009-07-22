/*
 *
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.j2me.location;

import java.util.*;
import java.io.*;

    /**
     * This class allows us to traverse the landmarks in the store
     */
    class LandmarkEnumeration implements Enumeration {
        // JAVADOC COMMENT ELIDED
        private String category;
        // JAVADOC COMMENT ELIDED
        private String name;
        // JAVADOC COMMENT ELIDED
        private double minLatitude;
        // JAVADOC COMMENT ELIDED
        private double maxLatitude;
        // JAVADOC COMMENT ELIDED
        private double minLongitude;
        // JAVADOC COMMENT ELIDED
        private double maxLongitude;
        // JAVADOC COMMENT ELIDED
        private Enumeration enumeration;

        // JAVADOC COMMENT ELIDED
        LandmarkEnumeration(String storeName0, String category0, String name0, 
                double minLatitude0, double maxLatitude0, double minLongitude0, 
                double maxLongitude0) throws IOException {
            category = category0;
            name = name0;
            minLatitude = minLatitude0;
            maxLatitude = maxLatitude0;
            minLongitude = minLongitude0;
            maxLongitude = maxLongitude0;
            byte[] result =
                LocationPersistentStorage.
                    getLandmarks(storeName0);
            DataInputStream stream =
                new DataInputStream(new ByteArrayInputStream(result));
            Vector cleanVec = new Vector();
            try {
                while (true) {
                    int id = stream.readInt();
                    int lmSize = stream.readInt();
                    byte[] serializedLm = new byte[lmSize];
                    stream.read(serializedLm);
                    if (matches(serializedLm)) {
                        cleanVec.addElement(
                            new LandmarkImpl(serializedLm, id, storeName0));
                    }
                }
            }
            catch (EOFException eofe) {
            }
            enumeration = cleanVec.elements();
	}

        // JAVADOC COMMENT ELIDED
        public Object nextElement() {
            return enumeration.nextElement();
        }

        // JAVADOC COMMENT ELIDED
        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }

        // JAVADOC COMMENT ELIDED
        public boolean matches(byte[] candidate) {
            LandmarkImpl l = new LandmarkImpl(candidate, -1, null);
            if (category != null) {
                if (!l.isInCategory(category)) {
                    return false;
                }
            }
            if (name != null) {
                if (!name.equals(l.getName())) {
                    return false;
                }
            }
            if (l.getQualifiedCoordinates() == null) {
                return true;
            }
	    double lat = l.getQualifiedCoordinates().getLatitude();
	    double lon = l.getQualifiedCoordinates().getLongitude();
            boolean val;
            if (minLongitude > maxLongitude) {
                val = (minLatitude <= lat) && (maxLatitude >= lat)
		    && ((minLongitude < lon) || (maxLongitude > lon));
            } else {
                val = (minLatitude <= lat) && (minLongitude <= lon) &&
                    (maxLongitude >= lon) && (maxLatitude >= lat);
            }
            return val;
        }
    }
