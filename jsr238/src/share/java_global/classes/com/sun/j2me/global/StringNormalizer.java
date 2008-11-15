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
 * A string normalizer is responsible for decomposing strings into their 
 * canonically decomposed equivalents (Normalization Form D). 
 */
public final class StringNormalizer implements StringDecomposer {
    /** The capacity increment value of the internal buffers. */
    private static final int CAPACITY_INCREMENT = 64;
    
    /** Internal decomposition buffer. */
    private int[] decomposition = new int[CAPACITY_INCREMENT];
    /** Decomposition offset. */
    private int decOffset;
    /** Decomposition length. */
    private int decLength;
    /** String offset. */
    private int strOffset;
    /** String length. */
    private int strLength;
    /** String initial offset. */
    private int strInitOffset;
    
    /** Max decomposition length. */
    private int maxDecomposition;
    
    /** 
     * The string being decomposed.
     */
    private String source;
    /**
     * A lookup table which is used during the normalization.
     */
    private NormalizationTable table;
    
    /** 
     * Creates a new instance of <code>StringNormalizer</code>.
     *
     * @param table a lookup table for the normalization
     */
    public StringNormalizer(NormalizationTable table) {
        this.table = table;       
        this.maxDecomposition = table.getMaxDecompositionLength();
    }
    
    /** 
     * Creates a new instance of <code>StringNormalizer</code>.
     *
     * @param s a string for the normaliztion
     * @param table a lookup table for the normalization
     */
    public StringNormalizer(String s, NormalizationTable table) {
        this(table);
        source = s;
        strLength = s.length();
    }

    /**
     * Sets the string for the normalization.
     *
     * @param s the string
     */
    public final void setSource(String s) {
        source = s;
        strLength = s.length();
        strInitOffset = 0;
        reset();        
    }

    /**
     * Sets the string for the normalization.
     *
     * @param s the string
     * @param offset the offset to start the normalization from
     */
    public final void setSource(String s, int offset) {
        source = s;
        strLength = s.length();
        strInitOffset = offset;
        reset();        
    }

    /**
     * Restarts the decomposition.
     */
    public final void reset() {
        decOffset = 0;
        decLength = 0;
        strOffset = strInitOffset;
    }
    
    /**
     * Returns the next code point value from the source string. It expects
     * the input string to be UTF-16 encoded.
     *
     * @return the next code point value
     */
    public final int nextUTF32() {
        if (strOffset >= strLength) {
            return EOF_ELEMENT;
        }

        int cp = (int)source.charAt(strOffset++);
        if (((cp & 0xfc00) == 0xd800) && (strOffset < strLength)) {
            // is a high surrogate cp
            int cp2 = (int)source.charAt(strOffset);
            if ((cp2 & 0xfc00) == 0xdc00) {
                // we have got suplementary low surrogate
                // so construct the final code point
                int wwww = (cp >> 6) & 0xf;                    
                cp = ((wwww + 1) << 16) | ((cp & 0x3f) << 10) | 
                        (cp2 & 0x3ff);

                ++strOffset;
            }
        }
        
        return cp;
    }
    
    /**
     * Returns the next encoded code point value from the normalized input
     * string. The methods of the <code>NormalizationTable</code> class can be
     * used to inspect the returned value. Returns <code>EOF_ELEMENT</code> if
     * the end of string is reached.
     *
     * @return the next encoded code point value from the normalized input 
     *      string or <code>EOF_ELEMENT</code> if the end of string is reached
     * @see NormalizationTable
     */
    public int getNextElement() {
        if (decOffset < decLength) {
            return decomposition[decOffset++];
        }
        
        int value = nextUTF32();
        if (value == EOF_ELEMENT) {
            return EOF_ELEMENT;
        }

        value = table.getCanonicalDecomposition(decomposition, 0, 
                value);
        
        if (NormalizationTable.isSingleCodePoint(value)) {
            if (NormalizationTable.isStable(value)) {
                return value;
            }
            decomposition[0] = value;
            decLength = 1;
        } else {
            decLength = value;
        }
        
        decOffset = 0;
        
        // decompose till we get a stable code point
        value = nextUTF32();
        while (value != -1) {
            if ((decLength + maxDecomposition) > decomposition.length) {
                int[] newDecomposition = new int[decomposition.length + 
                        CAPACITY_INCREMENT];
                System.arraycopy(decomposition, 0, newDecomposition, 0, 
                        decLength);
                decomposition = newDecomposition;
            }
            
            value = table.getCanonicalDecomposition(decomposition, decLength, 
                    value);
            
            if (NormalizationTable.isSingleCodePoint(value)) {
                decomposition[decLength++] = value;
                if (NormalizationTable.isStable(value)) {
                    break;
                }
            } else {
                decLength += value;
            }
            
            value = nextUTF32();
        }
      
        // order the code points according to their combining classes
        boolean checkOrder;
        do {
            checkOrder = false;
            
            for (int i = 1; i < decLength; ++i) {
                int cp1 = decomposition[i - 1];
                int cp2 = decomposition[i];

                int cc1 = NormalizationTable.getCombiningClass(cp1);
                int cc2 = NormalizationTable.getCombiningClass(cp2);
                
                if ((cc1 > cc2) && (cc2 != 0)) {
                    decomposition[i - 1] = cp2;
                    decomposition[i] = cp1;
                    checkOrder = true;
                }
            }
        } while (checkOrder);
        
        if (decLength > 0) {
            return decomposition[decOffset++];
        }
        
        return EOF_ELEMENT;
    }
}
