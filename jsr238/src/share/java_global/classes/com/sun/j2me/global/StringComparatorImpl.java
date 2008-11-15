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

import javax.microedition.global.StringComparator;
import javax.microedition.global.UnsupportedLocaleException;

/**
 * This class realizes string comparison methods of
 * {@link javax.microedition.global.StringComparator}. Specifically, these are:
 * <ul>
 *   <li> {@link #compare(String, String)}
 *   <li> {@link #equals(String, String)}
 * </ul>
 */
public class StringComparatorImpl implements CommonStringComparator {
    /** The capacity increment value of the internal buffers. */
    private static final int CAPACITY_INCREMENT = 64;

    /** Current collation level. */
    private int level;

    /** 
     * Internal buffer used in the compare function for temporary data
     * storage. 
     */
    private int[] buffer1 = new int[CAPACITY_INCREMENT];
    /** 
     * Internal buffer used in the compare function for temporary data
     * storage. 
     */
    private int[] buffer2 = new int[CAPACITY_INCREMENT];
    /** Internal buffers length. */
    int buffersLength = CAPACITY_INCREMENT;
    /** Normalization table */
    private final NormalizationTable nrmTable;
    /** String normalizer 1 */
    private final StringNormalizer strNormalizer1;
    /** String normalizer 2 */
    private final StringNormalizer strNormalizer2;
    /** String collator 1 */
    private final StringCollator strCollator1;
    /** String collator 2 */
    private final StringCollator strCollator2;   
    
    // JAVADOC COMMENT ELIDED
    public StringComparatorImpl(String locale, int level) {

        this.level = level;

        CollationElementTable colTable = null;

        nrmTable = NormalizationTableImpl.getInstance();
        colTable = CollationElementTableImpl.getInstance(locale);
        
        strNormalizer1 = new StringNormalizer(nrmTable);
        strCollator1 = new StringCollator(strNormalizer1, colTable);

        strNormalizer2 = new StringNormalizer(nrmTable);
        strCollator2 = new StringCollator(strNormalizer2, colTable);
    }

    /**
     * Test if the given character is unsafe to start the UCA with.
     *
     * @param c the tested character
     * @return <code>true</code> if the character is unsafe to start the UCA
     *       with
     */
    private final boolean isUnsafeCharacter(char c) {
        if ((c & 0xf800) == 0xd800) {
            // low or high surrogate
            return true;
        }
        int value = nrmTable.getCanonicalDecomposition(buffer1, 0, c);
        if (!nrmTable.isSingleCodePoint(value)) {
            return true;
        }
        return nrmTable.isUnsafe(value);
    }
    
    /**
     * Returns the starting offset for the UCA comparison. Tries to skip 
     * the first characters from both input strings which are equal. This speeds
     * up the comparison.
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return the starting offset
     */ 
    private final int skipBeginning(String s1, String s2) {
            int offset;
            int length = s1.length();
            if (s2.length() < length) {
                length = s2.length();
            }
            if (length == 0) {
                return 0;
            }
            if (s1.charAt(0) != s2.charAt(0)) {
                return 0;
            }
            for (offset = 1; offset < length; ++offset) {
                if (s1.charAt(offset) != s2.charAt(offset)) {
                    if (!isUnsafeCharacter(s1.charAt(offset)) &&
                            !isUnsafeCharacter(s2.charAt(offset))) {
                        return offset;
                    }

                    for (--offset; offset > 0; --offset) {
                        if (!isUnsafeCharacter(s1.charAt(offset))) {
                            return offset;
                        }
                    }
                    
                    return offset;
                }
            }
            
            if (length < s1.length()) {
                if (!isUnsafeCharacter(s1.charAt(length))) {
                    return length;
                }
                
                for (--length; length > 0; --length) {
                    if (!isUnsafeCharacter(s1.charAt(length))) {
                        return length;
                    }
                }
                                
                return length;
            }
            
            if (length < s2.length()) {
                if (!isUnsafeCharacter(s2.charAt(length))) {
                    return length;
                }

                for (--length; length > 0; --length) {
                    if (!isUnsafeCharacter(s1.charAt(length))) {
                        return length;
                    }
                }
                
                return length;
            }
            
            return length; 
    }
    
    // JAVADOC COMMENT ELIDED
    public int compare(String s1, String s2) {
        synchronized (this) {

            // try to skip as much as possible
            int offset = skipBeginning(s1, s2);
            
            // initialize objects
            strNormalizer1.setSource(s1, offset);
            strNormalizer2.setSource(s2, offset);
            strCollator1.reset();
            strCollator2.reset();
            
            int idx1;
            int idx2;
            
            int result;
            
            int buffered = 0;

            int value1;
            int value2;
            
            idx1 = 0;
            idx2 = 0;
            do {
                if (buffered >= buffersLength) {
                    buffersLength += CAPACITY_INCREMENT;
                    int[] newBuffer1 = 
                            new int[buffersLength];
                    int[] newBuffer2 = 
                            new int[buffersLength];
                    System.arraycopy(buffer1, 0, newBuffer1, 0, buffered);
                    System.arraycopy(buffer2, 0, newBuffer2, 0, buffered);
                    buffer1 = newBuffer1;
                    buffer2 = newBuffer2;                    
                }
                
                buffer1[buffered] = strCollator1.getNextElement();
                buffer2[buffered] = strCollator2.getNextElement();
                
                ++buffered;

                value1 = buffer1[idx1];
                value2 = buffer2[idx2];
                
                if (value1 != -1) {
                    value1 = CollationElementTable.getL1(value1);
                }
                if (value2 != -1) {
                    value2 = CollationElementTable.getL1(value2);
                }
                
                if (value1 != 0) {
                    if (value2 != 0) {
                        result = value1 - value2;
                        if (result != 0) {
                            return result;
                        }
                        ++idx1;
                    }
                    ++idx2;
                } else {
                    if (value2 == 0) {
                        ++idx2;
                    }
                    ++idx1;
                }
            } while ((value1 != -1) || (value2 != -1));
            
            if (level < StringComparator.LEVEL2) {
                return 0;
            }

            idx1 = 0;
            idx2 = 0;
            do {
                value1 = buffer1[idx1];
                value2 = buffer2[idx2];
                
                if (value1 != -1) {
                    value1 = CollationElementTable.getL2(value1);
                }
                if (value2 != -1) {
                    value2 = CollationElementTable.getL2(value2);
                }
                
                if (value1 != 0) {
                    if (value2 != 0) {
                        result = value1 - value2;
                        if (result != 0) {
                            return result;
                        }
                        ++idx1;
                    }
                    ++idx2;
                } else {
                    if (value2 == 0) {
                        ++idx2;
                    }
                    ++idx1;
                }
            } while ((value1 != -1) || (value2 != -1));

            if (level < StringComparator.LEVEL3) {
                return 0;
            }

            idx1 = 0;
            idx2 = 0;
            do {
                value1 = buffer1[idx1];
                value2 = buffer2[idx2];
                
                if (value1 != -1) {
                    value1 = CollationElementTable.getL3(value1);
                }
                if (value2 != -1) {
                    value2 = CollationElementTable.getL3(value2);
                }
                
                if (value1 != 0) {
                    if (value2 != 0) {
                        result = value1 - value2;
                        if (result != 0) {
                            return result;
                        }
                        ++idx1;
                    }
                    ++idx2;
                } else {
                    if (value2 == 0) {
                        ++idx2;
                    }
                    ++idx1;
                }
            } while ((value1 != -1) || (value2 != -1));
            
            if (level == StringComparator.IDENTICAL) {
                strNormalizer1.reset();
                strNormalizer2.reset();
                
                do {
                    value1 = strNormalizer1.getNextElement();
                    value2 = strNormalizer2.getNextElement();
                    
                    if (value1 != -1) {
                        value1 = NormalizationTable.getCodePoint(value1);
                    }
                    if (value2 != -1) {
                        value2 = NormalizationTable.getCodePoint(value2);
                    }

                    result = value1 - value2;
                    if (result != 0) {
                        return result;
                    }
                } while ((value1 != -1) || (value2 != -1));
            }
        }
        
        return 0;
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(String s1, String s2) {
        return compare(s1, s2) == 0;
    }
}
