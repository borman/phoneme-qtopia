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
 * This interface is used to get collation elements from one or more unicode
 * code points.
 */
public abstract class CollationElementTable {
    /** 
     * A value which is used to terminate the code point sequence when 
     * traversing possible contractions.
     *
     * @see #getChildBookmark
     */
    public static final int TERMINAL_CODE_POINT     = 0xffffffff;
    /**
     * A value which represents an invalid bookmark. It can be returned by the
     * {@link #getChildBookmark} method when trying to get the bookmark for
     * a non-existing contraction.
     */
    public static final int INVALID_BOOKMARK_VALUE  = 0xffffffff;
    
    /** A mask to decode the L1 weight value of an encoded collation element. */
    protected static final int L1_MASK          = 0x0000ffff;
    /** A mask to decode the L2 weight value of an encoded collation element. */
    protected static final int L2_MASK          = 0x01ff0000;
    /** A mask to decode the L3 weight value of an encoded collation element. */
    protected static final int L3_MASK          = 0x1e000000;   
    /** A flag indicating a code point with a variable weight. */
    protected static final int VW_FLAG          = 0x20000000;
    /** A flag indicating a single encoded collation element. */
    protected static final int SINGLE_CE_FLAG   = 0x40000000;
    /** A flag indicating a bookmark. */
    protected static final int BOOKMARK_FLAG    = 0x80000000;            
    
    /** 
     * A shift value to decode the L2 weight value of an encoded collation 
     * element. 
     */
    protected static final int L2_SHIFT = 16;
    /** 
     * A shift value to decode the L3 weight value of an encoded collation 
     * element. 
     */
    protected static final int L3_SHIFT = 25;   

    /**
     * Returns the L1 weight value of an encoded collation element.
     *
     * @param ecol the encoded collation element
     * @return the L1 weight value
     */
    public static final int getL1(int ecol) {
        return ecol & L1_MASK;
    }

    /**
     * Returns the L2 weight value of an encoded collation element.
     *
     * @param ecol the encoded collation element
     * @return the L2 weight value
     */
    public static final int getL2(int ecol) {
        return (ecol & L2_MASK) >>> L2_SHIFT;
    }

    /**
     * Returns the L3 weight value of an encoded collation element.
     *
     * @param ecol the encoded collation element
     * @return the L3 weight value
     */
    public static final int getL3(int ecol) {
        return (ecol & L3_MASK) >>> L3_SHIFT;
    }

    /**
     * Test if the given collation element has a variable weight.
     *
     * @param ecol the encoded collation element
     * @return <code>true</code> if the collation element has a variable weight
     */
    public static final boolean isVariableElement(int ecol) {
        return (ecol & VW_FLAG) != 0;
    }

    /**
     * Test if the given value is a bookmark.
     *
     * @param cbc the value
     * @return <code>true</code> if the value is a bookmark
     * @see #getCollationElements
     */
    public static final boolean isBookmark(int cbc) {
        return (cbc & BOOKMARK_FLAG) != 0;
    }
    
    /**
     * Test if the given value is a single encoded collation element.
     *
     * @param cbc the value
     * @return <code>true</code> if the value is a single encoded collation 
     *      element
     * @see #getCollationElements
     */
    public static final boolean isSingleCollationEl(int cbc) {
        return (cbc & SINGLE_CE_FLAG) != 0;
    }
    
    /**
     * Returns the collation element/elements for the given code point/points. 
     * Each returned collation element is encoded in a single integer value, 
     * which can be further decoded by the static methods of this class.
     * <p>
     * There are three types of possible return value and two types of the 
     * input values. 
     * </p><p>
     * If the parameters are an integer buffer, an offset to this buffer and 
     * a single code point, the method can return:
     * </p><p>
     * <ol>
     *      <li>
     *          A single encoded collation element value, when the code point 
     *          decomposes into one collation element and it isn't a starting 
     *          code point of any contraction. In this case nothing is written 
     *          into the buffer.
     *      </li>
     *      <li>
     *          The number of encoded collation elements, when the code point
     *          decomposes into more than one collation elements and it isn't
     *          a starting code point of any contraction. The encoded collation
     *          elements are written to the buffer on the given offset.
     *      </li>
     *      <li>
     *          A bookmark value, when the given code point is a starting code
     *          point of a contraction. Nothing is written into the buffer.
     *      </li>
     * </ol>
     * </p><p>
     * If the parameters are an integer buffer, an offset to this buffer and 
     * a bookmark, the method can return:
     * </p><p>
     * <ol>
     *      <li>
     *          A single encoded collation element value, when the code point
     *          sequence behind the bookmark decomposes into one collation 
     *          element. Nothing is written into the buffer.
     *      </li>
     *      <li>
     *          The number of encoded collation elements, when the code point
     *          sequence behind the bookmark decomposes into more than one 
     *          collation elements. The encoded collation elements are written 
     *          to the buffer on the given offset.
     *      </li>
     *      <li>
     *          A zero value, when the given bookmark is invalid or it doesn't 
     *          target the complete (terminated) code point sequence.
     *      </li>
     * </ol>
     * </p>
     *
     * @param buffer the array for the decomposition
     * @param offset the offset from the beginning of the array, where to place
     *      the collation elements
     * @param cp a code point or a bookmark
     * @return a single encoded collation element or the number of returned 
     *      collation elements or a bookmark or 
     *      <code>INVALID_BOOKMARK_VALUE</code>
     * @see #isBookmark
     * @see #isSingleCollationEl
     * @see #getChildBookmark
     */
    public abstract int getCollationElements(int[] buffer, int offset, int cp);
    
    /**
     * This method can be used to traverse the contractions. The traversing 
     * starts when the {@link #getCollationElements} method returns a bookmark 
     * instead of collation elements. The returned bookmark, which represents
     * a code point sequence consisting only of one code point, can be further 
     * tested if it's extensible by various other code points. 
     * <p>
     * If a partial match is found, the method returns another bookmark which
     * represents the new sequence. The new bookmark can be further "refined" 
     * as well. To get the collation elements for the sequence, the sequence 
     * has to be terminated by the 
     * <code>getChildBookmark(bookmark, TERMINAL_CODE_POINT)</code> call.
     * If the call returns a valid bookmark, it is guaranteed, that the 
     * <code>getCollationElements</code> method will return the collation 
     * elements for this final bookmark.
     * </p><p>
     * If no match can be found for the given bookmark and the code point 
     * value, the method returns <code>INVALID_BOOKMARK_VALUE</code>.
     * </p>
     *
     * @param bookmark the bookmark
     * @param cp a code point value or <code>TERMINAL_CODE_POINT</code>
     * @return the new bookmark for the new code point sequence if a match is
     *      found or <code>INVALID_BOOKMARK_VALUE</code> if no match can be
     *      found in the table
     * @see #getCollationElements
     */
    public abstract int getChildBookmark(int bookmark, int cp);
    
    /**
     * Returns the length of the longest possible contraction in the table.
     *
     * @return the longest contraction
     */
    public abstract int getMaxContractionLength();
}
