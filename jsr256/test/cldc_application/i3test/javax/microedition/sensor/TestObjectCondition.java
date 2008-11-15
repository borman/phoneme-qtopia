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

/* Class represents some custom made value type for object limits */
class ValueType {
    
}

public class TestObjectCondition extends TestCase {
    private ObjectCondition cond;
    private ValueType limit;

    /** Creates a new instance of TestObjectCondition */
    public TestObjectCondition() {
        limit = new ValueType();
    }
    
    
    private void testCreation() {
        cond = new ObjectCondition(limit);
        assertTrue(true);
        
        try {
            ObjectCondition c = new ObjectCondition(null);
            assertFalse(true);
        } catch (NullPointerException e) {
            assertTrue(true);
        }                
    }
    
    private void testValues() {
        assertTrue(cond.getLimit() == limit);
    }
    
    private void testComparison() {
        double doubleValue = 123.0;
        
        /* Comparison with double should be false for any value */
        assertFalse(cond.isMet(doubleValue));
        
        /* Comparison with limit must give true */
        assertTrue(cond.isMet(limit));
        
        /* Comparison with any Object but limit must give false */
        assertFalse(cond.isMet(new ValueType()));
    }
    
    public void runTests() {
        try {
            declare("testCreation");
            testCreation();
            
            declare("testValues");
            testValues();
            
            declare("testComparison");
            testComparison();
            
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }    
}
