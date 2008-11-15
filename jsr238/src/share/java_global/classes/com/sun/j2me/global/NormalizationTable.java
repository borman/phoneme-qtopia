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
 * This interface is used to get the unicode decomposition for the given unicode
 * character.
 */
public abstract class NormalizationTable {
    /** A mask to decode the code point value. */
    protected static final int CODE_POINT_MASK              = 0x001fffff;
    /** A mask to decode the combining class. */
    protected static final int COMBINING_CLASS_MASK         = 0x07e00000;
    /** A flag indicating a code point that is unsafe to start UCA with. */
    protected static final int UNSAFE_CODE_POINT_FLAG       = 0x10000000;
    /** A flag indicating an ignorable code point. */
    protected static final int IGNORABLE_CODE_POINT_FLAG    = 0x20000000;
    /** A flag indicating a code point with a logical order exception. */
    protected static final int LOGICAL_ORDER_EXCEPTION_FLAG = 0x40000000;
    /** A flag indicating a single encoded code point. */
    protected static final int SINGLE_CODE_POINT_FLAG       = 0x80000000;
    
    /** A shift value to decode the combining class. */
    protected static final int COMBINING_CLASS_SHIFT = 21;
    
    /**
     * Returns the code point value of an encoded code point.
     *
     * @param ecp the encoded code point
     * @return the code point value
     */
    public static final int getCodePoint(int ecp) {
        return ecp & CODE_POINT_MASK;
    }

    /**
     * Returns the combining class of an encoded code point.
     *
     * @param ecp the encoded code point
     * @return the combining class
     */
    public static final int getCombiningClass(int ecp) {
        return (ecp & COMBINING_CLASS_MASK) >>> COMBINING_CLASS_SHIFT;
    }
    
    /**
     * Checks if the given code point has a logical order exception.
     *
     * @param ecp the encoded code point
     * @return <code>true</code> if the code point has a logical order 
     *      exception
     */
    public static final boolean hasLogicalOrderException(int ecp) {
        return (ecp & LOGICAL_ORDER_EXCEPTION_FLAG) != 0;
    }
    
    /**
     * Returns <code>true</code> if the given code point is unsafe to start
     * the collation algorithm with.
     *
     * @param ecp the encoded code point
     * @return <code>true</code> if the code point is unsafe
     */
    public static final boolean isUnsafe(int ecp) {
        return (ecp & UNSAFE_CODE_POINT_FLAG) != 0;
    }

    /**
     * Returns <code>true</code> if the given code point is ignorable for 
     * the collation algorithm (at all levels).
     *
     * @param ecp the encoded code point
     * @return <code>true</code> if the code point is ignorable
     */
    public static final boolean isIgnorable(int ecp) {
        return (ecp & IGNORABLE_CODE_POINT_FLAG) != 0;
    }

    /**
     * Returns <code>true</code> if the given code point has a zero combining 
     * class.
     *
     * @param ecp the encoded code point
     * @return <code>true</code> if the code point has a zero combining class
     */
    public static final boolean isStable(int ecp) {
        return (ecp & COMBINING_CLASS_MASK) == 0;
    }
    
    /**
     * Returns <code>true</code> if the given value represents a single encoded
     * code point and <code>false</code> if it's a number of code points.
     *
     * @param codeOrCount the value
     * @return <code>true</code> if the value represent a single code point,
     *      <code>false</code> if it's only a number of code points
     * @see #getCanonicalDecomposition
     */
    public static final boolean isSingleCodePoint(int codeOrCount) {
        return (codeOrCount & SINGLE_CODE_POINT_FLAG) != 0;
    }
    
    /**
     * Gets the canonical decomposition elements for the given unicode 
     * character. The decompositon is returned as a single or an array of 
     * encoded code points. The encoded values can be further decoded by 
     * the static methods of this class.
     *
     * The return value depends on the number of code points to which 
     * the given code point decomposes. If it decomposes to a single code point,
     * its encoded value is returned, otherwise the decomposition is stored in
     * the given array and the method returns only the length of the 
     * decomposition.
     *
     * @param buffer an array for the decomposition
     * @param offset the offset from the beginning of the array, where to place
     *      the decomposition
     * @param cp the code point to decompose
     * @return the length of the decomposition or a single encoded code point 
     * @see #isSingleCodePoint
     */
    public abstract int getCanonicalDecomposition(int[] buffer, int offset, 
            int cp);
    
    /**
     * Returns the length of the longest decomposition in the table.
     *
     * @return the maximum decomposition length
     */
    public abstract int getMaxDecompositionLength();
}
