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

import com.sun.midp.i3test.TestCase;

import javax.microedition.sensor.*;
import javax.microedition.io.*;
import java.io.*;

public class TestConditions extends TestCase implements ConditionListener {


    private Data recData;
    private boolean isConditionMet = false;
    private Condition recCondition;

    public synchronized void conditionMet(SensorConnection sensor,
                                          Data data,
                                          Condition condition) {
        isConditionMet = true;
        recData = data;
        recCondition = condition;
        notify();
    }

    private synchronized void testCondition(Object[] dataBuff, TestConditionParam condParams,
                                            String uri) throws IOException {
        SensorConnection conn = (SensorConnection)Connector.open(uri);
        assertTrue(conn.getState() == SensorConnection.STATE_OPENED);
        ChannelImpl channel = (ChannelImpl)((conn.getSensorInfo().getChannelInfos())[0]);
        TestChannelDevice channelDevice = (TestChannelDevice)(((Sensor)conn).getChannelDevice(0));
        assertTrue(channelDevice != null);
        channelDevice.setTestData(dataBuff);

        isConditionMet = false;
        recData = null;
        recCondition = null;

        Condition condition = null;
        if (condParams instanceof TestLimitConditionParam) {
            Object limit = ((TestLimitConditionParam)condParams).limitValue;
            double limitDouble = 0.0;
            if (limit instanceof Integer) {
                limitDouble = ((Integer)limit).doubleValue();
            } else if (limit instanceof Double) {
                limitDouble = ((Double)limit).doubleValue();
            } else {
                assertTrue(false); // wrong type of limit
            }
            String operator = ((TestLimitConditionParam)condParams).limitCond;
            condition = new LimitCondition(limitDouble, operator);
        } else if (condParams instanceof TestRangeConditionParam) {
            Object limitLow = ((TestRangeConditionParam)condParams).lowestValue;
            Object limitHigh = ((TestRangeConditionParam)condParams).highestValue;
            double limitLowDouble = 0.0;
            double limitHighDouble = 0.0;
            if (limitLow instanceof Integer) {
                limitLowDouble = ((Integer)limitLow).doubleValue();
            } else if (limitLow instanceof Double) {
                limitLowDouble = ((Double)limitLow).doubleValue();
            } else {
                assertTrue(false); // wrong type of limit
            }
            if (limitHigh instanceof Integer) {
                limitHighDouble = ((Integer)limitHigh).doubleValue();
            } else if (limitHigh instanceof Double) {
                limitHighDouble = ((Double)limitHigh).doubleValue();
            } else {
                assertTrue(false); // wrong type of limit
            }
            String lowerOp = ((TestRangeConditionParam)condParams).lowestCond;
            String upperOp = ((TestRangeConditionParam)condParams).highestCond;
            condition = new RangeCondition(limitLowDouble, lowerOp, limitHighDouble, upperOp);
        } else {
            assertTrue(false); // wrong type of limit
        }
        
        channel.addCondition(this, condition);
        try {
            wait(4000);
        } catch (InterruptedException ex) {
        }
        if (condParams.isMet) {
            assertTrue(isConditionMet);
            assertTrue(recData != null);
            assertTrue(recData.getChannelInfo() != null);
            assertTrue(recCondition != null);
            assertTrue(recCondition == condition);

            try {
                recData.getTimestamp(0);
                assertTrue(true);
            } catch (Throwable ex) {
                fail("Unexpected exception " + ex);
            }
            try {
                recData.getUncertainty(0);
                assertTrue(true);
            } catch (Throwable ex) {
                fail("Unexpected exception " + ex);
            }
            try {
                assertTrue(recData.isValid(0));
            } catch (Throwable ex) {
                fail("Unexpected exception " + ex);
            }
        } else { // condition shouldn't be met
            assertTrue(!isConditionMet);
        }
        conn.close();
    }

    public void runTests() {
        try {
            SensorTable table = new SensorTable();
            for (int i = 0; i < table.size(); i++) {
                declare("Test of condition");
                for (int j = 0; j < table.condLength(i); j++) {
                    testCondition(table.getData(i),
                    table.getTestCondParam(i,j), table.getUrl(i));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail("" + t);
        }
    }
}
