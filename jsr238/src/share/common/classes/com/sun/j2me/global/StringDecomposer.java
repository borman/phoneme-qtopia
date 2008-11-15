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
 * The <code>StringDecomposer</code> interface defines requirements for a 
 * class which can be used to decompose / transform string into elements. The 
 * decomposition is done in steps (each call to the <code>getNextElement</code> 
 * method returns the next element of the decomposed string).
 * <p>
 * The <code>StringCollator</code> class uses this interface to be independent 
 * of a string normalizer.
 */
public interface StringDecomposer {
    /** A value which represents the end of the decomposition. */
    public static final int EOF_ELEMENT = -1;    
    
    /**
     * Returns the next element of the decomposed string. The kind of returned 
     * element depends on the type of the "decomposer", but it has to be 
     * encoded as a single integer value.
     *
     * @return the decomposed element or <code>EOF_ELEMENT</code> when the 
     *      decomposition is at the end
     */
    public int getNextElement();
    
    /**
     * Restarts the decomposition.
     */
    public void reset();
}
