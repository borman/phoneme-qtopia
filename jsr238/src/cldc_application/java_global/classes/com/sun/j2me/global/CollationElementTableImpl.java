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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.jsr238.SecurityInitializer;
import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;
import com.sun.midp.main.Configuration;
import com.sun.midp.configurator.Constants;
import javax.microedition.global.UnsupportedLocaleException;

/**
 * An emulator-specific implementation of the <code>CollationElementTable</code>
 * interface.
 */
public final class CollationElementTableImpl extends CollationElementTable {
	
    /** This class has a different security domain than the MIDlet suite */
	  static private class SecurityTrusted
      	implements ImplicitlyTrustedClass {};

      /** This class has a different security domain than the MIDlet suite */
      private static SecurityToken classSecurityToken =
    	  SecurityInitializer.requestToken(new SecurityTrusted());

    /**
     * Array of collation table files.
     */
    private static CollationFile[] collationFiles;
    
    /**
     * Collation table instances for supported locales.
     */
    private static CollationElementTableImpl[] collationTables;

    /**
     * Array of locales for which collation elements exist.
     */
    private static String[] locales;

    /**
     * Array for converting from a locale string to the collation table index.
     */
    private static int[] localeToTable;

    /**
     * This is used to prevent loading of all collation element tables at once,
     * which is very memory consuming.
     */
    private static Object loadingMutex = new Object();
    
    /**
     * Class representing a file with collation tables data.
     */
    private static class CollationFile implements Runnable {
        /**
         * The name of the file without an extension.
         */
        private final String fileName;
        
        /**
         * The max contraction for that file.
         */
        public final int maxContraction;

        /** 
         * Loading state of the file.
         * 
         * @see #STATE_UNINITIALIZED
         * @see #STATE_LOAD_FINISHED
         * @see #STATE_LOAD_FAILED
         */
        public int loadingState = STATE_UNINITIALIZED;

        /** Collation file data. */
        public byte[] offsets0;
        /** Collation file data. */
        public short[] offsets1;
        /** Collation file data. */
        public short[] offsets2;
        /** Collation file data. */
        public int[] data;
        /** Collation file data. */
        public int[] data2;
        
        /**
         * Creates a new collation file with the given name and maximum
         * contraction.
         *
         * @param fileName the file name
         * @param maxContraction the maximum contraction
         */
        public CollationFile(String fileName, int maxContraction) {
            this.fileName = fileName;
            this.maxContraction = maxContraction;
        }
        
        /**
         * Implements loading of the table data from the input file.
         */
        public void run() {
            int newState = STATE_LOAD_FAILED;

            byte[] buffer0 = null;
            byte[] buffer1 = null;
            byte[] buffer2 = null;
            byte[] buffer3 = null;
            byte[] buffer4 = null;      

            synchronized (loadingMutex) {
            	String storageName=null;
                try {
                    RandomAccessStream storage = 
	                        new RandomAccessStream(classSecurityToken);
                    
                    storageName = File.getConfigRoot(Constants.INTERNAL_STORAGE_ID) + fileName + ".bin";
                    
	                storage.connect(storageName, Connector.READ);
	
	                DataInputStream ds = new DataInputStream(storage.openInputStream());
                    try {
                        int length;

                        length = ds.readUnsignedShort();
                        buffer0 = new byte[length];
                        ds.readFully(buffer0, 0, length);

                        length = ds.readUnsignedShort();
                        length <<= 1;
                        buffer1 = new byte[length];
                        ds.readFully(buffer1, 0, length);

                        length = ds.readUnsignedShort();
                        length <<= 1;
                        buffer2 = new byte[length];
                        ds.readFully(buffer2, 0, length);

                        length = ds.readUnsignedShort();
                        length <<= 2;
                        buffer3 = new byte[length];
                        ds.readFully(buffer3, 0, length);

                        length = ds.readUnsignedShort();
                        length <<= 2;
                        buffer4 = new byte[length];
                        ds.readFully(buffer4, 0, length);

                        newState = STATE_LOAD_FINISHED;
                    } catch (IOException e) {
                        if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                            Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
                                           "Failed to read collation table: " + storageName + 
                                           "\nException: " + e.toString());
                        }
                    }

                    ds.close();
                    storage.disconnect();

                } catch (IOException e) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
                                       "Failed to open collation table: " + storageName + 
                                       "Exception: " + e.toString());
                    }
                }

                if (newState == STATE_LOAD_FINISHED) {
                    offsets0 = buffer0;
                    offsets1 = LocaleHelpers.byteArrayToShortArray(buffer1);
                    buffer1 = null;
                    offsets2 = LocaleHelpers.byteArrayToShortArray(buffer2);
                    buffer2 = null;
                    data = LocaleHelpers.byteArrayToIntArray(buffer3);
                    buffer3 = null;
                    data2 = LocaleHelpers.byteArrayToIntArray(buffer4);
                    buffer4 = null;
                }
            }

            synchronized (this) {
                loadingState = newState;
                notifyAll();
            }
        }
    }
    
    /** Initialization of static members */
    static {
        String propString;
        
        // get and parse the collation files
        propString = Configuration.getProperty("microedition.global.collation");
        if ((propString != null) && (propString.length() != 0)) {
            // helper array used to parse "<left part> = <right part>"
            String[] equationParts = new String[2];

            String[] fileNames = LocaleHelpers.splitString(propString, ",", -1);
            collationFiles = new CollationFile[fileNames.length];

            Vector tmpCollationTables = new Vector();
            Vector tmpLocales = new Vector();
            Vector tmpLocaleToTable = new Vector();
            for (int i = 0; i < collationFiles.length; ++i) {
                String keyPrefix = "microedition.global.collation." + 
                        fileNames[i] + ".";

                // get and parse the max number of contractions
                int maxContractions = 2;
                propString = Configuration.getProperty(keyPrefix + "maxcontr");
                if ((propString != null) && (propString.length() != 0)) {
                    try {
                        maxContractions = Integer.parseInt(propString);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                // create a new CollationFile instance
                collationFiles[i] = new CollationFile(fileNames[i], 
                        maxContractions);

                // get and parse supported locales for the collation file
                propString = Configuration.getProperty(keyPrefix + "locales");
                if ((propString == null) || (propString.length() == 0)) {
                    continue;
                }

                String[] localeParts = LocaleHelpers.splitString(propString, 
                        ";", -1);
                for (int j = 0; j < localeParts.length; ++j) {
                    if ((LocaleHelpers.splitString(
                                equationParts, localeParts[j], "=", 2) != 2) || 
                            (equationParts[1].length() == 0)) {
                        continue;
                    }

                    // parse the locale index
                    int localeIndex;
                    try {
                        localeIndex = Integer.parseInt(equationParts[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    // add to locales and localeToTable
                    Integer tableIndex = 
                            new Integer(tmpCollationTables.size());
                    String[] localeStrings = 
                            LocaleHelpers.splitString(
                                equationParts[0], ",", -1);
                    for (int k = 0; k < localeStrings.length; ++k) {
                        tmpLocales.addElement(localeStrings[k]);
                        tmpLocaleToTable.addElement(tableIndex);
                    }

                    // create a new collation table and add it to the list
                    tmpCollationTables.addElement(new CollationElementTableImpl(
                            localeIndex, collationFiles[i]));
                }
            }

            // convert the vectors to arrays

            int length;

            length = tmpCollationTables.size();
            collationTables = new CollationElementTableImpl[length];
            tmpCollationTables.copyInto(collationTables);

            length = tmpLocales.size();
            locales = new String[length];
            tmpLocales.copyInto(locales);

            length = tmpLocaleToTable.size();
            localeToTable = new int[length];
            for (int i = 0; i < length; ++i) {
                localeToTable[i] = 
                        ((Integer)tmpLocaleToTable.elementAt(i)).intValue();
            }
        } else {
            locales = new String[0];
        }
    }

    /** Before loading of the table data. */
    private static final int STATE_UNINITIALIZED = 0;
    /** After loading of the table data. */
    private static final int STATE_LOAD_FINISHED = 1;
    /** The table is inconsistent and can't be used. */
    private static final int STATE_LOAD_FAILED = 2;

    /** Min value of the L2 weight value of an encoded collation. */
    private static final int MIN_L2 = 1;
    /** Min value of the L3 weight value of an encoded collation. */
    private static final int MIN_L3 = 1;

    /** The mask of the Sequence flag. */
    private static final int SEQUENCE_FLAG  = 0x80000000;
    /** The mask of the Operation flag. */
    private static final int OPERATION_FLAG = 0x40000000;

    /** The mask of the Bookmark offset. */
    private static final int BOOKMARK_OFFSET_MASK = 0x7fff0000;
    /** The mask of the Bookmark code. */
    private static final int BOOKMARK_CODEPT_MASK = 0x0000ffff;

    /** The shift of the Bookmark offset. */
    private static final int BOOKMARK_OFFSET_SHIFT = 16;

    /** The mask of the data entry type flag. */
    private static final int DATA2_ENTRY_TYPE_FLAG = 0x80000000;
    /** The mask of the data sequence flag. */
    private static final int DATA2_SEQUENCE_FLAG = 0x40000000;
    /** The mask of the data locale. */
    private static final int DA2E0_LOCALE_MASK = 0x0fff0000;
    /** The mask of the data offset. */
    private static final int DA2E0_OFFSET_MASK = 0x00007fff;
    /** The mask of the data offset. */
    private static final int DA2E1_OFFSET_MASK = 0x3ff00000;
    /** The mask of the data code. */
    private static final int DA2E1_CODEPT_MASK = 0x000fffff;

    /** The shift of the data locale. */
    private static final int DA2E0_LOCALE_SHIFT = 16;
    /** The shift of the data offset. */
    private static final int DA2E1_OFFSET_SHIFT = 20;

    /** Array of offsets used in the getCollationElements function. */
    private byte[] offsets0;
    /** Array of offsets used in the getCollationElements function. */
    private short[] offsets1;
    /** Array of offsets used in the getCollationElements function. */
    private short[] offsets2;
    /** 
     * Array of offsets used in the getCollationData  and 
     * getCollationElements functions. 
     */
    private int[] data;
    /** 
     * Array of offsets used in the getCollationDataOffset and 
     * getChildBookmark functions.
     */
    private int[] data2;

    /** The assigned collation data file for this table. */ 
    private final CollationFile collationFile;    
    /** The locale index. */
    private int localeIndex;
    /** The locale flag. */
    private int localeFlag;

    /** 
     * Creates a new instance of <code>CollationElementTableImpl</code> for 
     * the given locale index and collation file.
     *
     * @param index the locale index
     * @param file the CollationFile instance
     */
    private CollationElementTableImpl(int index, CollationFile file) {
        localeIndex = index;
        localeFlag = 1 << index;
        collationFile = file;
    }

    /**
     * Returns an instance of the table for the given locale.
     *
     * @param locale the locale
     * @return the instance
     */
    public static synchronized CollationElementTableImpl getInstance(
            String locale) {
        int i;
        for (i = 0; i < locales.length; ++i) {
            if (locales[i].equals(locale)) {
                break;
            }
        }
        
        if (i == locales.length) {
            // not supported
            throw new UnsupportedLocaleException("The locale " + locale + 
                    " is not supported by the string comparator");            
        }
        
        CollationElementTableImpl collationTable = 
                collationTables[localeToTable[i]];
        CollationFile collationFile = collationTable.collationFile;
        
        synchronized (collationFile) {
            if (collationFile.loadingState == STATE_UNINITIALIZED) {
                // Start loading of the data immediately
                new Thread(collationFile).start();
            }
        }

        return collationTable;
    }

    /**
     * Blocks until all table data is loaded from the file.
     *
     * @throws IllegalStateException if the loading has failed
     */
    private void initializeData() {
        synchronized (collationFile) {
            if (collationFile.loadingState != STATE_LOAD_FINISHED) {
                if (collationFile.loadingState == STATE_UNINITIALIZED) {
                    try {
                        collationFile.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (collationFile.loadingState != STATE_LOAD_FINISHED) {
                    throw new IllegalStateException(
                            "Failed to load the collation element table data");
                }
            }

            offsets0 = collationFile.offsets0;
            offsets1 = collationFile.offsets1;
            offsets2 = collationFile.offsets2;
            data = collationFile.data;
            data2 = collationFile.data2;
        }
    }

    /**
     * Computes the implicit weights for the given code point and stores them
     * into the buffer on the given index. Returns the number of stored 
     * collation elements.
     *
     * @param buffer the buffer for the collation elements
     * @param offset the offset into <code>buffer</code>
     * @param cp the code point
     * @return the number of calculated collation elements
     */
    private static final int calculateImplicitWeights(int[] buffer, int offset,
            int cp) {
        int base = 0xfbc0;

        if ((cp >= 0x4e00) && (cp <= 0x9fbf)) {
            // CJK Unified Ideographs
            base = 0xfb40;
        } else if ((cp >= 0x3400) && (cp <= 0x4dbf)) {
            // CJK Unified Ideographs Extension A
            base = 0xfb80;
        } else if ((cp >= 0x20000) && (cp <= 0x2a6df)) {
            // CJK Unified Ideographs Extension B
            base = 0xfb80;
        } // TODO: else if...??

        buffer[offset++] = (MIN_L3 << L3_SHIFT) | 
                (MIN_L2 << L2_SHIFT) |
                (base + (cp >> 15)) & L1_MASK;
        buffer[offset] = ((cp & 0x7fff) | 0x8000) & L1_MASK;

        return 2;
    }

    /**
     * Stores the collation elements from the given data table index and the 
     * code point into the buffer on the given offset. Returns the number of 
     * the stored collation elements.
     *
     * @param buffer the buffer for collation elements
     * @param offset the offset into <code>buffer</code>
     * @param cp the code point
     * @param index the data table index
     * @return the number of stored elements
     */
    private final int getCollationData(int[] buffer, int offset, int cp, 
            int index) {       
        int value = data[index];
        int sequenceFlag = value & SEQUENCE_FLAG;
        if ((value & OPERATION_FLAG) != 0) {
            int tmp = (value & L1_MASK) + cp;
            value = (value & ~L1_MASK) | tmp & L1_MASK;
//          value &= ~OPERATION_FLAG;
        }

        if ((data[index + 1] & SEQUENCE_FLAG) != sequenceFlag) {
            return (value | SINGLE_CE_FLAG) & ~BOOKMARK_FLAG;
        }       

        buffer[offset] = value;
        int i = 1;
        value = data[index + 1];
        do {
//          value &= ~SEQUENCE_FLAG;
            if ((value & OPERATION_FLAG) != 0) {
                int tmp = (value & L1_MASK) + cp;
                value = (value & ~L1_MASK) | tmp & L1_MASK;
//              value &= ~OPERATION_FLAG;
            }
            buffer[offset + i++] = value;
            value = data[index + i];
        } while ((value & SEQUENCE_FLAG) == sequenceFlag);

        return i;
    }

    /**
     * Returns the data table index for the given bookmark.
     *
     * @param bookmark the bookmark
     * @return the data table index
     */
    private final int getCollationDataOffset(int bookmark) {
        int index = (bookmark & BOOKMARK_OFFSET_MASK) >>> BOOKMARK_OFFSET_SHIFT;
        int value = data2[index];
        int sequenceFlag = value & DATA2_SEQUENCE_FLAG;
        int i = 0;
        do {
            if (((value & DATA2_ENTRY_TYPE_FLAG) == 0) &&
                ((((value & DA2E0_LOCALE_MASK) >>> DA2E0_LOCALE_SHIFT) &
                    localeFlag) != 0)) {
                    return value & DA2E0_OFFSET_MASK;
            }
            ++i;
            value = data2[index + i];
        } while ((value & DATA2_SEQUENCE_FLAG) == sequenceFlag);

        return -1;
    }

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
    public int getChildBookmark(int bookmark, int cp) {
        if (bookmark == INVALID_BOOKMARK_VALUE) {
            return INVALID_BOOKMARK_VALUE;
        }

        int index = (bookmark & BOOKMARK_OFFSET_MASK) >>> BOOKMARK_OFFSET_SHIFT;
        int value = data2[index];
        int sequenceFlag = value & DATA2_SEQUENCE_FLAG;
        int i = 0;
        if (cp == TERMINAL_CODE_POINT) {
            do {
                if (((value & DATA2_ENTRY_TYPE_FLAG) == 0) &&
                        ((((value & DA2E0_LOCALE_MASK) >>> DA2E0_LOCALE_SHIFT) &
                            localeFlag) != 0)) {
                    // we have found an entry for our locale
                    return bookmark;
                }                
                ++i;
                value = data2[index + i];
            } while ((value & DATA2_SEQUENCE_FLAG) == sequenceFlag);
        } else {
            do {
                if (((value & DATA2_ENTRY_TYPE_FLAG) != 0) &&
                        ((value & DA2E1_CODEPT_MASK) == cp)) {
                    // construct a new bookmark
                    // replace the old offset with a new one
                    bookmark &= ~BOOKMARK_OFFSET_MASK;
                    bookmark |= ((((value & DA2E1_OFFSET_MASK) 
                            >>> DA2E1_OFFSET_SHIFT) + index) 
                            << BOOKMARK_OFFSET_SHIFT) & BOOKMARK_OFFSET_MASK;
                    return bookmark;
                }
                ++i;
                value = data2[index + i];
            } while ((value & DATA2_SEQUENCE_FLAG) == sequenceFlag);
        }

        return INVALID_BOOKMARK_VALUE;
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
    public int getCollationElements(int[] buffer, int offset, int cp) {
        if (data == null) {
            initializeData();
        }

        if ((cp & BOOKMARK_FLAG) != 0) {
            // handle the case when cp is a bookmark
            if (cp == INVALID_BOOKMARK_VALUE) {
                return 0;
            }
            int collationOffset = getCollationDataOffset(cp);
            if (collationOffset == -1) {
                return 0;                
            }
            return getCollationData(buffer, offset, cp & BOOKMARK_CODEPT_MASK, 
                    collationOffset);
        }

        int index;

        index = (cp >> 8) & 0x1fff;
        if ((index >= offsets0.length) || (offsets0[index] == -1)) {
            return calculateImplicitWeights(buffer, offset, cp);
        }

        index = (((int)offsets0[index] & 0xff) << 4) + ((cp >> 4) & 0xf);
        if (offsets1[index] == -1) {
            return calculateImplicitWeights(buffer, offset, cp);
        }

        if ((offsets1[index] & 0x8000) != 0) {
            index = (int)offsets1[index] & 0x7fff;
            return getCollationData(buffer, offset, cp, index);
        }

        index = (((int)offsets1[index] & 0xfff) << 4) + (cp & 0xf);
        if (offsets2[index] == -1) {
            return calculateImplicitWeights(buffer, offset, cp);
        }

        index = (int)offsets2[index] & 0xffff;

        if ((index & 0x8000) != 0) {
            return BOOKMARK_FLAG | 
                    (index << BOOKMARK_OFFSET_SHIFT) & BOOKMARK_OFFSET_MASK |
                    cp & BOOKMARK_CODEPT_MASK;
        }

        return getCollationData(buffer, offset, cp, index);
    }

    /**
     * Returns the length of the longest possible contraction in the table.
     *
     * @return the longest contraction
     */
    public int getMaxContractionLength() {
        return collationFile.maxContraction;
    }

    // JAVADOC COMMENT ELIDED - see StringComparator.getSupportedLocales()
    // description
    public static String[] getSupportedLocales() {
        // locales without the "empty string" locale 
        String[] filteredLocales = new String[locales.length];
        int filteredCount = 0;
        for (int i = 0; i < locales.length; ++i) {
            if (locales[i].length() != 0) {
                filteredLocales[filteredCount++] = locales[i];
            }
        }
        
        if (filteredCount != locales.length) {
            String[] compactedArray = new String[filteredCount];
            System.arraycopy(filteredLocales, 0, compactedArray, 0, 
                    filteredCount);
            filteredLocales = compactedArray;
        }
        
        return filteredLocales;
    }
}