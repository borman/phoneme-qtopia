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
 * A string collator is responsible for a conversion of strings from their 
 * canonically decomposed forms to collation elements. It requires a decomposer 
 * which is able to convert a string to its canonically decomposed equivalent
 * (Normalization Form D).
 */
public class StringCollator implements StringDecomposer {
    /** The capacity increment value of the internal buffers. */
    private static final int CAPACITY_INCREMENT = 64;
    
    /** Internal collation buffer. */
    private int[] collation = new int[CAPACITY_INCREMENT];
    /** Collation offset. */
    private int colOffset;
    /** Collation length. */
    private int colLength;
    
    /** Internal decomposition buffer. */
    private int[] decomposition = new int[CAPACITY_INCREMENT];
    /** Decomposition offset. */
    private int decOffset;
    /** Decomposition length. */
    private int decLength;
    
    /** The longest possible contraction in the table. */
    private int maxContraction;

    /** A decomposer for the canonical decomposition of strings. */
    private StringDecomposer source;
    /**
     * A lookup table to convert from characers to their collation element
     * representation.
     */
    private CollationElementTable table;

    /**
     * Creates a new instance of <code>StringCollator</code>.
     *
     * @param decomposer a decomposer for the canonical decomposition
     * @param table a lookup table for the conversion
     */
    public StringCollator(StringDecomposer decomposer, 
            CollationElementTable table) {
        this.source = decomposer;
        this.table = table;
        
        maxContraction = table.getMaxContractionLength();
        
        savedBookmarks = new int[maxContraction];

        match = new int[maxContraction];
        maxMatch = new int[maxContraction];
    }

    /**
     * Restarts the decomposition.
     */
    public final void reset() {
        decOffset = 0;
        decLength = 0;
        colOffset = 0;
        colLength = 0;
//      source.reset();
    }
   
    /**
     * Returns the next encoded code point value from the normalized input
     * string (in Normalization Form D). It skips all ignorable code points 
     * and reorders code points with the logical order exception.
     *
     * @return the next encoded code point value from the normalized input
     *      string or <code>StringNormalizer.EOF_ELEMENT</code> if the end of 
     *      string is reached.
     */
    private final int getNextSourceElement() {
        if (decOffset < decLength) {
            return decomposition[decOffset++];
        }
        
        int ecp;
        do {
            ecp = source.getNextElement();
            if (ecp == StringNormalizer.EOF_ELEMENT) {
                return ecp;
            }
        } while (NormalizationTable.isIgnorable(ecp));

        if (NormalizationTable.hasLogicalOrderException(ecp)) {
            decomposition[0] = ecp;
            decOffset = 0;
            decLength = 1;
            do {
                ecp = source.getNextElement();
                if (ecp == StringNormalizer.EOF_ELEMENT) {
                    return decomposition[decOffset++];
                }
            } while (NormalizationTable.isIgnorable(ecp));
        }

        return ecp;
    }
    
    /**
     * Returns the encoded code point value from the normalized input string 
     * (in Normalization Form D) on the specified offset. It doesn't 
     * automatically discard the returned element. So two successive calls 
     * to <code>peekSourceElement(0)</code> return the same value. The method
     * skips all ignorable code points and reorders code points with the 
     * logical order exception.
     *
     * @param offset the offset of the element to return
     * @return the encoded code point value from the offset
     */
    private final int peekSourceElement(int offset) {
        if ((decOffset + offset) < decLength) {
            return decomposition[decOffset + offset];
        }
        
//        if ((decOffset + offset) >= (decLength + 1)) {
//            throw new IllegalStateException();
//        }
       
        int ecp;
        do {
            ecp = source.getNextElement();
            if (ecp == StringNormalizer.EOF_ELEMENT) {
                return ecp;
            }
        } while (NormalizationTable.isIgnorable(ecp));

        offset += decOffset;
        
        if ((offset + 1) >= decomposition.length) {
            offset -= decOffset;
            decLength -= decOffset;
            // reserve for possible logical order exception
            if (decOffset <= 1) {
                // allocate a new array
                int[] newDecomposition = new int[decomposition.length + 
                        CAPACITY_INCREMENT];
                System.arraycopy(decomposition, decOffset, newDecomposition, 0, 
                        decLength);
                decomposition = newDecomposition;               
            } else {
                // shift only
                System.arraycopy(decomposition, decOffset,
                        decomposition, 0, decLength);
            }
            decOffset = 0;
        }
        
        if (NormalizationTable.hasLogicalOrderException(ecp)) {
            int lastCP = ecp;
            do {
                ecp = source.getNextElement();
                if (ecp == StringNormalizer.EOF_ELEMENT) {
                    decomposition[offset] = lastCP;
                    return lastCP;
                }
            } while (NormalizationTable.isIgnorable(ecp));

            decomposition[offset++] = ecp;
            decomposition[offset] = lastCP;
            decLength += 2;
            return ecp;
        }

        decomposition[offset] = ecp;
        ++decLength;
        return ecp;
    }   

    /** Array of the saved bookmarks. */
    private int[] savedBookmarks;
    /** Internal buffer. */
    private int[] maxMatch;
    /** Internal buffer. */
    private int[] match;
    /** Max length. */
    private int maxLength;
    /** Length of the prefix. */
    private int prefixLength;
    /** Max bookmark. */
    private int maxBookmark;
    
    /**
     * A helper recursive method for <code>matchTail</code>.
     *
     * @param bookmark the bookmark which represents the current code point 
     *      sequence with a partial match in the collation element table
     * @param index the index of the next tested code point
     * @param accepted the number of code points which has been accepted so far
     * @param lastCC the combining class of the previous code point
     */
    private final void matchTailAux(int bookmark, int index, 
            int accepted, int lastCC) {
        if ((prefixLength + accepted) >= maxContraction) {
            return;
        }

        int ecp, cc;
        if ((ecp = peekSourceElement(index)) == 
                StringNormalizer.EOF_ELEMENT) {
            return;
        }
        cc = NormalizationTable.getCombiningClass(ecp);
        while (cc == lastCC) {
            ++index;
            if ((ecp = peekSourceElement(index)) == 
                    StringNormalizer.EOF_ELEMENT) {
                return;
            }
            cc = NormalizationTable.getCombiningClass(ecp);
        }
        
        if (cc == 0) {
            return;
        }

        // IMPL_NOTE: reorder?
        // 1. skip
        matchTailAux(bookmark, index + 1, accepted, cc);
        int bookmark2 = table.getChildBookmark(bookmark, 
                NormalizationTable.getCodePoint(ecp));
        if (bookmark2 != CollationElementTable.INVALID_BOOKMARK_VALUE) {
            // 2. accept
            match[accepted] = ecp;
            matchTailAux(bookmark2, index + 1, accepted + 1, lastCC);
            
            if ((accepted + 1) > maxLength) {
                int bookmark3 = table.getChildBookmark(bookmark2, 
                        CollationElementTable.TERMINAL_CODE_POINT);
                if (bookmark3 != CollationElementTable.INVALID_BOOKMARK_VALUE) {
                    // 3. terminate
                    maxLength = accepted + 1;
                    maxBookmark = bookmark3;
                    System.arraycopy(match, 0, maxMatch, 0, maxLength);
                }
            }
        }
    }   
    
    /**
     * Tries to find the longest possible match among code points with non-zero
     * combining class. Some of them can be skipped to form the longest matching
     * code point sequence. This implies usage of a recursive algorithm 
     * (<code>matchTailAux</code>).
     *
     * @param bookmark the bookmark which represents the current code point 
     *      sequence with a partial match in the collation element table
     * @param index the index of the next code point
     * @return the bookmark representing the longest matching code point 
     *      sequence
     */
    private final int matchTail(int bookmark, int index) {
        maxLength = 0;
        maxBookmark = CollationElementTable.INVALID_BOOKMARK_VALUE;
        prefixLength = index;
        matchTailAux(bookmark, index, 0, -1);

        if (maxBookmark != CollationElementTable.INVALID_BOOKMARK_VALUE) {
            decOffset += index;
            int length = decLength - decOffset;
            if (length == maxLength) {
                decOffset = 0;
                decLength = 0;
                return maxBookmark;
            }
            for (int i = 0, j = 0, k = 0; j < length; ++j) {
                int cp = decomposition[decOffset + j];
                if ((k < maxLength) && (cp == maxMatch[k])) {
                    ++k;
                } else {
                    decomposition[i] = cp;
                    ++i;
                }
            }
            decOffset = 0;
            decLength -= maxLength + index;
        }

        return maxBookmark;
    }
    
    /**
     * Tries to find the longest possible code point sequence from the collation
     * element table that have a match at the current position of the normalized
     * input string.
     *
     * @param initialBookmark the starting bookmark
     * @return the bookmark representing the longest matching code point 
     *      sequence
     */
    private final int match(int initialBookmark) {
        int i;
        
        int bookmark = initialBookmark;
        int ecp;
        for (i = 0; i < maxContraction; ++i) {
            if ((ecp = peekSourceElement(i)) == StringNormalizer.EOF_ELEMENT) {
                break;
            }
            bookmark = table.getChildBookmark(bookmark, 
                    NormalizationTable.getCodePoint(ecp));
            if (bookmark == CollationElementTable.INVALID_BOOKMARK_VALUE) {
                break;
            }
            savedBookmarks[i] = bookmark;
        }
        
        do {
            for (--i; i >= 0; --i) {
                if (NormalizationTable.isStable(
                        decomposition[decOffset + i])) {
                    break;
                }
            }
            
            bookmark = (i >= 0) ? savedBookmarks[i] : initialBookmark;
            bookmark = matchTail(bookmark, i + 1);
            if (bookmark != CollationElementTable.INVALID_BOOKMARK_VALUE) {
                return bookmark;
            }
           
            bookmark = (i >= 0) ? savedBookmarks[i] : initialBookmark;
            bookmark = table.getChildBookmark(bookmark, 
                CollationElementTable.TERMINAL_CODE_POINT);
            if (bookmark != CollationElementTable.INVALID_BOOKMARK_VALUE) {
                decOffset += i + 1;
                if (decOffset == decLength) {
                    decOffset = 0;
                    decLength = 0;
                }
                return bookmark;
            }

        } while (i > 0);
                
        return initialBookmark;
    }
    
    /**
     * Returns the next encoded collation element from the input string. The
     * returned value can be inspected by the methods of the 
     * <code>CollationElementTable</code> class. Returns 
     * <code>EOF_ELEMENT</code> if the end of string is reached.
     *
     * @return the next encoded collation element or <code>EOF_ELEMENT</code> 
     *      if the end of string is reached
     * @see CollationElementTable
     */
    public final int getNextElement() {
        if (colOffset < colLength) {
            return collation[colOffset++];
        }
        
        int value;

        if ((value = getNextSourceElement()) == 
                StringNormalizer.EOF_ELEMENT) {
            return EOF_ELEMENT;
        }
        
        value = table.getCollationElements(collation, 0,
                NormalizationTable.getCodePoint(value));

        if (CollationElementTable.isBookmark(value)) {
            value = match(value);
            value = table.getCollationElements(collation, 0, value);
            // check for INVALID_BOOKMARK_VALUE?
        }

        if (CollationElementTable.isSingleCollationEl(value)) {
            return value;
        }

        colOffset = 1;
        colLength = value;
        return collation[0];
    }
}
