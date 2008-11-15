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

package javax.microedition.sensor;

import com.sun.midp.i3test.TestCase;

public class TestMeasurementRange extends TestCase {
    private final static double smallest = 1.0;
    private final static double largest = 10.0;
    private final static double resolution = 0.1;
    
    private MeasurementRange range;
    
    /** Creates a new instance of TestMeasurementRange */
    public TestMeasurementRange() {
    }
    
    private void testCreation() {
        range = new MeasurementRange(smallest, largest, resolution);
        assertTrue(true);
    }
    
    private void testValues() {
        assertTrue(smallest == range.getSmallestValue());
        assertTrue(largest == range.getLargestValue());
        assertTrue(resolution == range.getResolution());
    }
    
    private void testRanges() {
        try {
            MeasurementRange r = new MeasurementRange(largest, smallest, resolution);
            assertTrue(false);
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    private void testResolution() {
        try {
            MeasurementRange r = new MeasurementRange(smallest, largest, -resolution);
            assertTrue(false);
        }
        catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    
    
    public void runTests() {
        try {
            declare("testCreation");
            testCreation();
            
            declare("testValues");
            testValues();
            
            declare("testRanges");
            testRanges();
            
            declare("testResolution");
            testResolution();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }        
}
