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

import com.sun.midp.io.j2me.storage.*;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.main.Configuration;
import com.sun.midp.configurator.Constants;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.jsr238.SecurityInitializer;

/**
 * An emulator specific implementation of the <code>NormalizationTable</code>
 * interface.
 */
public final class NormalizationTableImpl extends NormalizationTable 
        implements Runnable {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** Security token to allow access to implementation APIs */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** The only instance of the class. */
    private static NormalizationTableImpl instance;

    /** Before loading of the table data. */
    private static final int STATE_UNINITIALIZED = 0;
    /** After loading of the table data. */
    private static final int STATE_LOAD_FINISHED = 1;
    /** The table is inconsistent and can't be used. */
    private static final int STATE_LOAD_FAILED = 2;

    /** 
     * The state of the table.
     * 
     * @see #STATE_UNINITIALIZED
     * @see #STATE_LOAD_FINISHED
     * @see #STATE_LOAD_FAILED
     */
    private int state = STATE_UNINITIALIZED;

    /** The mask of the Sequence flag. */
    private static final int SEQUENCE_FLAG = 0x80000000;
    
    /** The Hangul decomposition S base. */
    private static final int HANGUL_SBASE = 0xac00;
    /** The Hangul decomposition L base. */
    private static final int HANGUL_LBASE = 0x1100;
    /** The Hangul decomposition V base. */
    private static final int HANGUL_VBASE = 0x1161;
    /** The Hangul decomposition T base. */
    private static final int HANGUL_TBASE = 0x11a7;   
    /** The Hangul decomposition L count. */
    private static final int HANGUL_LCOUNT = 19;
    /** The Hangul decomposition V count. */
    private static final int HANGUL_VCOUNT = 21;
    /** The Hangul decomposition T count. */
    private static final int HANGUL_TCOUNT = 28;
    /** The Hangul decomposition N count. */
    private static final int HANGUL_NCOUNT = HANGUL_VCOUNT * HANGUL_TCOUNT;
    /** The Hangul decomposition S count. */
    private static final int HANGUL_SCOUNT = HANGUL_LCOUNT * HANGUL_NCOUNT;
    
    /** Array of offsets used in the getCanonicalDecomposition function. */
    private byte[] offsets0;
    /** Array of offsets used in the getCanonicalDecomposition function. */
    private short[] offsets1;
    /** Array of offsets used in the getCanonicalDecomposition function. */
    private short[] offsets2;
    /** Array of data used in the getCanonicalDecomposition function. */
    private int[] data;
    /** The maximum possible decomposition length used in implementation*/
    private final int maxDecomposition;
    
    /** Creates a new instance of <code>NormalizationTableImpl</code>. */
    private NormalizationTableImpl() {
        // get and parse the max decomposition length
        int tmpMaxDecomposition = 3;
        String propString = Configuration.getProperty(
                "microedition.global.normalization.maxdecomp");
        if ((propString != null) && (propString.length() != 0)) {
            try {
                tmpMaxDecomposition = Integer.parseInt(propString);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        maxDecomposition = tmpMaxDecomposition;
    }

    /**
     * Returns an instance of the table.
     *
     * @return the instance
     */
    public static synchronized NormalizationTable getInstance() {
        if (instance != null) {
            return instance;
        }
        
        instance = new NormalizationTableImpl();

        // start loading of the data immediately
        new Thread(instance).start();
        
        return instance;
    }

    /**
     * Blocks until all table data is loaded from the file.
     *
     * @throws IllegalStateException if the loading has failed
     */
    private void initializeData() {
        synchronized (instance) {
            if (instance.state != STATE_LOAD_FINISHED) {
                if (instance.state == STATE_UNINITIALIZED) {
                    try {
                        instance.wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (instance.state != STATE_LOAD_FINISHED) {
                    throw new IllegalStateException();
                }
            }
        }
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

        try {
            RandomAccessStream storage = 
                    new RandomAccessStream(classSecurityToken);
            storage.connect(File.getConfigRoot(Constants.INTERNAL_STORAGE_ID) + Configuration
                            .getProperty("microedition.global.normalization")
                            + ".bin", Connector.READ);

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

                newState = STATE_LOAD_FINISHED;
            } catch (IOException e) {
            }

            ds.close();
            storage.disconnect();
            
        } catch (IOException e) {
        }

        if (newState == STATE_LOAD_FINISHED) {
            offsets0 = buffer0;
            offsets1 = LocaleHelpers.byteArrayToShortArray(buffer1);
            offsets2 = LocaleHelpers.byteArrayToShortArray(buffer2);
            data = LocaleHelpers.byteArrayToIntArray(buffer3);
        }
        
        synchronized (this) {
            state = newState;
            notifyAll();
        }
    }

    /**
     * Implements the Hangul decomposition. Returns the resulting code points
     * in the given buffer stored on the specified offset.
     *
     * @param buffer the buffer for the result
     * @param offset the offset into <code>buffer</code>
     * @param cp the code point to decompose
     * @return the number of stored code points
     */
    private static final int hangulDecomposition(int[] buffer, int offset,
            int cp) {
        int SIndex = cp - HANGUL_SBASE;
        buffer[offset++] = HANGUL_LBASE + SIndex / HANGUL_NCOUNT;
        buffer[offset++] = HANGUL_VBASE + 
                (SIndex % HANGUL_NCOUNT) / HANGUL_TCOUNT;

        int tmp = SIndex % HANGUL_TCOUNT;
        if (tmp > 0) {
            buffer[offset] = HANGUL_TBASE + tmp;
            return 3;
        }

        return 2;
    }

    /**
     * Test for a valid code point.
     *
     * @param cp the tested code point value
     * @return <code>true</code> if the given code point is valid
     */
    private static final boolean isValidCodePoint(int cp) {
        if ((cp < 0) || (cp > 0x10ffff)) {
            // out of range code point
            return false;
        } else if ((cp >= 0xfdd0) && (cp <= 0xfdef) ||
                ((cp & 0xffff) == 0xfffe) ||
                ((cp & 0xffff) == 0xffff)) {
            // non character code points
            return false;
        } else if ((cp & 0x1ff800) == 0xd800) {
            // unpaired surrogates
            return false;
        }
        
        return true;
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
    public int getCanonicalDecomposition(int[] buffer, int offset, int cp) {
        if (data == null) {
            initializeData();
        }
        
        if (!isValidCodePoint(cp)) {
            return 0;
        }

        int index;
        
        if ((cp >= HANGUL_SBASE) && (cp < (HANGUL_SBASE + HANGUL_SCOUNT))) {
            // the hangul algoritmic decomposition
            return hangulDecomposition(buffer, offset, cp);
        }
        
        index = (cp >> 8) & 0x1fff;
        if ((index >= offsets0.length) || (offsets0[index] == -1)) {
            return cp | SINGLE_CODE_POINT_FLAG;
        }
        
        index = (((int)offsets0[index] & 0xff) << 4) + ((cp >> 4) & 0xf);
        if (offsets1[index] == -1) {
            return cp | SINGLE_CODE_POINT_FLAG;
        }

        index = (((int)offsets1[index] & 0xffff) << 4) + (cp & 0xf);
        if (offsets2[index] == -1) {
            return cp | SINGLE_CODE_POINT_FLAG;
        }

        index = (int)offsets2[index] & 0xffff;
        
        int value = data[index];
        int sequenceFlag = value & SEQUENCE_FLAG;
        
        if ((data[index + 1] & SEQUENCE_FLAG) != sequenceFlag) {
            return value | SINGLE_CODE_POINT_FLAG;
        }
        
        buffer[offset] = value;
        int i = 1;
        value = data[index + 1];       
        do {
//          value &= ~SEQUENCE_FLAG;
            buffer[offset + i++] = value;
            value = data[index + i];
        } while ((value & SEQUENCE_FLAG) == sequenceFlag);
        
        return i;
    }
   
    /**
     * Returns the length of the longest decomposition in the table.
     *
     * @return the maximum decomposition length
     */
    public int getMaxDecompositionLength() {
        return maxDecomposition;
    }
}
