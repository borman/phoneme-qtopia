/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
/*
 * DistributedRandom.java
 *
 * Created on Feb 9, 2004
 *
 */
package gov.nist.microedition.io.j2me.sip;

import java.util.Random;

/**
 * Generate a random number.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class DistributedRandom extends Random {
    
    /**
     * Method copied from the jdk src 1.4.2_03.
     * @param n the seed for the next random number
     * @param confirmPositive boolean flag that is set to true when the random 
     *  number to be returned has to be a positive integer
     * @return the random number
     * @exception IllegalArgumentException if seed is less than zero
     */
    public int nextCommon(int n, boolean confirmPositive) 
    throws IllegalArgumentException {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive");
        if ((n & -n) == n) // i.e. n is a power of 2
            return (int)((n * (long)next(31)) >> 31);
        
        int bits, val;
        boolean condition;
        do {
            bits = next(31);
            val = bits%n;
            condition = (bits - val + (n - 1) < 0);
            if (confirmPositive) {
                condition = (condition&&(val > 0));
            }
        } while (condition);    
        return val;
    }

    /**
     * Returns a random integer.
     * @param n the seed for the next random number
     * @return the random number
     * @exception IllegalArgumentException if seed is less than zero
     */
    public int nextInt(int n) throws IllegalArgumentException {
        return nextCommon(n, false);
    }
    
    /**
     * Returns a positive random integer.
     * @param n the seed for the next random number
     * @return the random number
     * @exception IllegalArgumentException if seed is less than zero
     */
    public int nextPositiveInt(int n) throws IllegalArgumentException {
        return nextCommon(n, true);
    }
}
