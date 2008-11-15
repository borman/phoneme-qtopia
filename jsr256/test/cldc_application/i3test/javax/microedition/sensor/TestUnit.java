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

public class TestUnit extends TestCase {
    private static final String STRING_1 = "ms";
    private static final String STRING_2 = "kg";
    
    private Unit u1;
    private Unit u2;
    
    /** Creates a new instance of Unit */
    public TestUnit() {
    }

    private void testCreation() {
        u1 = Unit.getUnit(STRING_1);
        u2 = Unit.getUnit(STRING_2);    
        assertTrue(u1 != u2);
    }
    
    private void testUnitString() {
        assertTrue(u1.toString().equals(STRING_1));
        assertTrue(u2.toString().equals(STRING_2));
    }
    
    private void testUnitFactory() {
        assertTrue(u1 == Unit.getUnit(STRING_1));
        assertTrue(u2 == Unit.getUnit(STRING_2));                
    }
    
    private void testNullPointerException() {
        try {
            Unit u = Unit.getUnit(null);
            assertTrue(false);
        }
        catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    private void testIllegalArgumentException() {
        try {
            Unit u = Unit.getUnit("");
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
            
            declare("testUnitString");
            testUnitString();
            
            declare("testUnitFactory");
            testUnitFactory();
            
            declare("testNullPointerException");
            testNullPointerException();
            
            declare("testIllegalArgumentException");
            testIllegalArgumentException();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }    
}
