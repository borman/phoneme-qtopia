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

public class TestRangeCondition extends TestCase {
    private static final String OBJECT_VALUE = "123";
    
    private static final double LOWER_LIMIT = 1.0;
    private static final double UPPER_LIMIT = 10.0;
    
    private static final double VALUE_WITHIN = 5.0;
    private static final double VALUE_OUTSIDE_1 = 20.0;
    private static final double VALUE_OUTSIDE_2 = -5.0;
    
    /** Creates a new instance of TestRangeCondition */
    public TestRangeCondition() {
    }
    
    private void testCreation() {
        /* Test IllegalArgumentException 1 */
        try {
            RangeCondition c = new RangeCondition(UPPER_LIMIT, Condition.OP_GREATER_THAN, LOWER_LIMIT, Condition.OP_LESS_THAN);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }        
        
        /* Test IllegalArgumentException 2 */
        try {
            RangeCondition c = new RangeCondition(LOWER_LIMIT, Condition.OP_EQUALS, UPPER_LIMIT, Condition.OP_LESS_THAN);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }                
        
        /* Test IllegalArgumentException 3 */
        try {
            RangeCondition c = new RangeCondition(LOWER_LIMIT, Condition.OP_GREATER_THAN, UPPER_LIMIT, Condition.OP_EQUALS);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }                        
    }
    
    private void testValues() {
        double lowerLimit = LOWER_LIMIT;
        double upperLimit = UPPER_LIMIT;
        String lowerOp = Condition.OP_GREATER_THAN;
        String upperOp = Condition.OP_LESS_THAN;
        
        RangeCondition cond = new RangeCondition(lowerLimit, lowerOp, upperLimit, upperOp);
        
        assertTrue(lowerLimit == cond.getLowerLimit());
        assertTrue(upperLimit == cond.getUpperLimit());
        assertTrue(lowerOp == cond.getLowerOp());
        assertTrue(upperOp == cond.getUpperOp());
    }
    
    private void testClosedRange() {
        String lowerOp = Condition.OP_GREATER_THAN_OR_EQUALS;
        String upperOp = Condition.OP_LESS_THAN_OR_EQUALS;
        RangeCondition cond = new RangeCondition(LOWER_LIMIT, lowerOp, UPPER_LIMIT, upperOp);
        
        assertTrue(cond.isMet(VALUE_WITHIN));
        assertFalse(cond.isMet(VALUE_OUTSIDE_1));
        assertFalse(cond.isMet(VALUE_OUTSIDE_2));
        assertTrue(cond.isMet(LOWER_LIMIT));
        assertTrue(cond.isMet(UPPER_LIMIT));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testOpenRange() {
        String lowerOp = Condition.OP_GREATER_THAN;
        String upperOp = Condition.OP_LESS_THAN;
        RangeCondition cond = new RangeCondition(LOWER_LIMIT, lowerOp, UPPER_LIMIT, upperOp);
        
        assertTrue(cond.isMet(VALUE_WITHIN));
        assertFalse(cond.isMet(VALUE_OUTSIDE_1));
        assertFalse(cond.isMet(VALUE_OUTSIDE_2));
        assertFalse(cond.isMet(LOWER_LIMIT));
        assertFalse(cond.isMet(UPPER_LIMIT));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }

    private void testOpenLeft() {
        String lowerOp = Condition.OP_GREATER_THAN;
        String upperOp = Condition.OP_LESS_THAN_OR_EQUALS;
        RangeCondition cond = new RangeCondition(LOWER_LIMIT, lowerOp, UPPER_LIMIT, upperOp);
        
        assertTrue(cond.isMet(VALUE_WITHIN));
        assertFalse(cond.isMet(VALUE_OUTSIDE_1));
        assertFalse(cond.isMet(VALUE_OUTSIDE_2));
        assertFalse(cond.isMet(LOWER_LIMIT));
        assertTrue(cond.isMet(UPPER_LIMIT));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testOpenRight() {
        String lowerOp = Condition.OP_GREATER_THAN_OR_EQUALS;
        String upperOp = Condition.OP_LESS_THAN;
        RangeCondition cond = new RangeCondition(LOWER_LIMIT, lowerOp, UPPER_LIMIT, upperOp);
        
        assertTrue(cond.isMet(VALUE_WITHIN));
        assertFalse(cond.isMet(VALUE_OUTSIDE_1));
        assertFalse(cond.isMet(VALUE_OUTSIDE_2));
        assertTrue(cond.isMet(LOWER_LIMIT));
        assertFalse(cond.isMet(UPPER_LIMIT));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    public void runTests() {
        try {
            declare("testCreation");
            testCreation();
            
            declare("testValues");
            testValues();
            
            declare("testClosedRange");
            testClosedRange();
            
            declare("testOpenRange");
            testClosedRange();
            
            declare("testOpenLeft");
            testClosedRange();
            
            declare("testOpenRight");
            testClosedRange();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }        
}
