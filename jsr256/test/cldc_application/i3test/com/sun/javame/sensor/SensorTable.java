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

package com.sun.javame.sensor;

import javax.microedition.sensor.*;

public class SensorTable {

    /** Set of sensor's urls. */
    private final String[] urls = {"sensor:temperature", "sensor:battery_level"};

    /** Set of data arrays for testing. */
    private final Object[][] dataTable = {
                                          // sensor:temperature
                                          {
                                              new Double(36.5),
                                              new Double(36.8),
                                              new Double(37.2),
                                              new Double(38.7),
                                              new Double(39.1),
                                              new Double(38.4),
                                              new Double(37.6),
                                              new Double(37.2),
                                              new Double(36.8),
                                              new Double(36.6)
                                          },
                                          // sensor:battery_level
                                          {
                                              new Integer(65),
                                              new Integer(68),
                                              new Integer(72),
                                              new Integer(87),
                                              new Integer(91),
                                              new Integer(84),
                                              new Integer(76),
                                              new Integer(72),
                                              new Integer(68),
                                              new Integer(66)
                                          }
    };

    /** Set of condition values for testing. */
    private final TestConditionParam[][] dataValues = {
                                          // sensor:temperature
                                          {
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Double(36.6),
                                                                          Condition.OP_LESS_THAN_OR_EQUALS
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Double(36.6),
                                                                          Condition.OP_LESS_THAN
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Double(36.6),
                                                                          Condition.OP_GREATER_THAN
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Double(36.6),
                                                                          Condition.OP_GREATER_THAN_OR_EQUALS
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          false,
                                                                          new Double(34.2),
                                                                          Condition.OP_LESS_THAN
                                                                         ),
                                              new TestRangeConditionParam(
                                                                          true,
                                                                          new Double(34.2),
                                                                          Condition.OP_GREATER_THAN,
                                                                          new Double(37.9),
                                                                          Condition.OP_LESS_THAN
                                                                         )
                                          },
                                          // sensor:battery_level
                                          {
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Integer(66),
                                                                          Condition.OP_EQUALS
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Integer(66),
                                                                          Condition.OP_LESS_THAN_OR_EQUALS
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Integer(66),
                                                                          Condition.OP_LESS_THAN
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Integer(66),
                                                                          Condition.OP_GREATER_THAN
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          true,
                                                                          new Integer(66),
                                                                          Condition.OP_GREATER_THAN_OR_EQUALS
                                                                         ),
                                              new TestLimitConditionParam(
                                                                          false,
                                                                          new Integer(42),
                                                                          Condition.OP_LESS_THAN
                                                                         ),
                                              new TestRangeConditionParam(
                                                                          true,
                                                                          new Integer(42),
                                                                          Condition.OP_GREATER_THAN,
                                                                          new Integer(97),
                                                                          Condition.OP_LESS_THAN
                                                                         )
                                          }
    };

    int size() {
        return urls.length;
    }

    String getUrl(int i) {
        return urls[i];
    }
    
    Object[] getData(int i) {
        return dataTable[i];
    }
    
    int condLength(int i) {
        return dataValues[i].length;
    }

    TestConditionParam getTestCondParam(int i, int j) {
        return dataValues[i][j];
    }
}

class TestConditionParam {
    boolean isMet;
    TestConditionParam(boolean isMet) {
        this.isMet = isMet;
    }
}

class TestLimitConditionParam extends TestConditionParam {
    Object limitValue;
    String limitCond;
    TestLimitConditionParam(boolean isMet, Object limitValue, String limitCond) {
        super(isMet);
        this.limitValue = limitValue;
        this.limitCond = limitCond;
    }
}

class TestRangeConditionParam extends TestConditionParam {
    Object lowestValue;
    String lowestCond;
    Object highestValue;
    String highestCond;
    TestRangeConditionParam(boolean isMet, Object lowestValue, String lowestCond,
        Object highestValue, String highestCond) {
        super(isMet);
        this.lowestValue = lowestValue;
        this.lowestCond = lowestCond;
        this.highestValue = highestValue;
        this.highestCond = highestCond;
    }
}
