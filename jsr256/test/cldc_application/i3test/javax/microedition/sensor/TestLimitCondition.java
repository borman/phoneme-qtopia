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

public class TestLimitCondition extends TestCase {
    private static final String OBJECT_VALUE = "123";
    
    /** Creates a new instance of TestLimitCondition */
    public TestLimitCondition() {
    }

    private void testCreation() {
        /* Test NullPointerException */
        try {
            LimitCondition c = new LimitCondition(1.0, null);
            assertFalse(true);
        } catch (NullPointerException e) {
            assertTrue(true);
        }                
        
        /* Test IllegalArgumentException */
        try {
            LimitCondition c = new LimitCondition(1.0, "123");
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }        
    }
    
    private void testValues() {
        double limit = 1.0;
        String op = Condition.OP_EQUALS;
        
        LimitCondition cond = new LimitCondition(limit, op);
        
        assertTrue(limit == cond.getLimit());
        assertTrue(op == cond.getOperator());
    }
    
    private void testEquals() {
        double limit = 10.0;
        double value1 = 10.0;
        double value2 = 20.0;        
        String op = Condition.OP_EQUALS;
        
        LimitCondition cond = new LimitCondition(limit, op);
        assertTrue(cond.isMet(value1));
        assertFalse(cond.isMet(value2));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testGreaterThan() {
        double limit = 10.0;
        double value1 = 15.0;
        double value2 = 5.0;
        String op = Condition.OP_GREATER_THAN;
        
        LimitCondition cond = new LimitCondition(limit, op);
        assertTrue(cond.isMet(value1));
        assertFalse(cond.isMet(value2));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testLessThan() {
        double limit = 10.0;
        double value1 = 5.0;
        double value2 = 15.0;
        String op = Condition.OP_LESS_THAN;
        
        LimitCondition cond = new LimitCondition(limit, op);
        assertTrue(cond.isMet(value1));
        assertFalse(cond.isMet(value2));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testLessThanOrEquals() {
        double limit = 10.0;
        double value1 = 5.0;
        double value2 = 15.0;
        double value3 = limit;
        String op = Condition.OP_LESS_THAN_OR_EQUALS;
        
        LimitCondition cond = new LimitCondition(limit, op);
        assertTrue(cond.isMet(value1));
        assertFalse(cond.isMet(value2));
        assertTrue(cond.isMet(value3));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    private void testGreaterThanOrEquals() {
        double limit = 10.0;
        double value1 = 15.0;
        double value2 = 5.0;
        double value3 = limit;
        String op = Condition.OP_GREATER_THAN_OR_EQUALS;
        
        LimitCondition cond = new LimitCondition(limit, op);
        assertTrue(cond.isMet(value1));
        assertFalse(cond.isMet(value2));
        assertTrue(cond.isMet(value3));
        
        assertFalse(cond.isMet(OBJECT_VALUE));
    }
    
    public void runTests() {
        try {
            declare("testCreation");
            testCreation();
            
            declare("testValues");
            testValues();
            
            declare("testEquals");
            testEquals();
            
            declare("testGreaterThan");
            testGreaterThan();
            
            declare("testLessThan");
            testLessThan();
            
            declare("testLessThanOrEquals");
            testLessThanOrEquals();
            
            declare("testGreaterThanOrEquals");
            testGreaterThanOrEquals();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }        
}
