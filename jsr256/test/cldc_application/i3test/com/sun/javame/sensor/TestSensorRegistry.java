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

public class TestSensorRegistry extends TestCase {
    public static final int SENSOR_COUNT = 4;
    public static final String QUANTITY[] = { "temperature", "battery_level",
            "battery_charge", "sensor_tester" };
    private static final int MAX_BUF_SIZE[] = { 512, 256, 256, 512 };
    private static final int CHANNEL_COUNT[] = { 1, 1, 1, 1 };
    private static final String CONTEXT[] = { SensorInfo.CONTEXT_TYPE_USER,
            SensorInfo.CONTEXT_TYPE_DEVICE, SensorInfo.CONTEXT_TYPE_DEVICE,
            SensorInfo.CONTEXT_TYPE_DEVICE };
    private static final String URL[] = {
            "sensor:temperature;contextType=user;model=temp01;location=Phone",
            "sensor:battery_level;contextType=device;model=temp11",
            "sensor:battery_charge;contextType=device;model=temp21",
            "sensor:sensor_tester;contextType=device;model=temp01", };

    private static final String URL_0 = "sensor:temperature";
    private static final String URL_1 = "sensor:alcohol";
    private static final String URL_2 = "sensor:temperature;contextType=user";
    
    /** Creates a new instance of TestSensorRegistry */
    public TestSensorRegistry() {
    }

    private void testSensorCreation() throws BadConfigurationException {
        assertEquals("Sensors count is equal",
                SENSOR_COUNT, SensorRegistry.getSensorCount());
        
        SensorInfo[] infos = SensorRegistry.getAllSensors();
        for (int i = 0; i < SENSOR_COUNT; i++) {
            assertEquals("Quantity equals", QUANTITY[i],
                    infos[i].getQuantity());
            assertEquals("Max buffer size equals", MAX_BUF_SIZE[i],
                    infos[i].getMaxBufferSize());
            assertEquals("Context type equals", CONTEXT[i],
                    infos[i].getContextType());
            assertEquals("Url equals", URL[i], infos[i].getUrl());
            
            /* Channels */
            ChannelInfo[] channels = infos[i].getChannelInfos();
            assertEquals("Channels count equals", CHANNEL_COUNT[i],
                    channels.length);
        }
    }
    
    private void testFindSensors1() throws BadConfigurationException {
        SensorInfo[] infos = SensorManager.findSensors(QUANTITY[0], null);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo quantity equals", QUANTITY[0],
                infos[0].getQuantity());

        infos = SensorManager.findSensors(QUANTITY[1], null);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo quantity equals", QUANTITY[1],
                infos[0].getQuantity());

        infos = SensorManager.findSensors(null, CONTEXT[0]);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo context equals", CONTEXT[0],
                infos[0].getContextType());

        infos = SensorManager.findSensors(null, CONTEXT[1]);
        assertEquals("SensorInfo count equals", 3, infos.length);
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[0].getContextType());
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[1].getContextType());
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[2].getContextType());
    }

    private void testFindSensors2() throws BadConfigurationException {
        SensorInfo[] infos = SensorManager.findSensors(URL_0);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo quantity equals", QUANTITY[0],
                infos[0].getQuantity());

        infos = SensorManager.findSensors(URL_1);
        assertEquals("SensorInfo count equals", 0, infos.length);

        infos = SensorManager.findSensors(URL_2);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo quantity equals", QUANTITY[0],
                infos[0].getQuantity());
        assertEquals("SensorInfo context equals", SensorInfo.CONTEXT_TYPE_USER,
                infos[0].getContextType());
    }

    private void testFindSensorsByEqualContext() {
        String[] CONTEXT = new String[TestSensorRegistry.CONTEXT.length];
        for (int i = 0; i < CONTEXT.length; i++) {
            // Create new String instance
            CONTEXT[i] = new String(TestSensorRegistry.CONTEXT[i]);
        }

        SensorInfo[] infos = SensorManager.findSensors(null, CONTEXT[0]);
        assertEquals("SensorInfo count equals", 1, infos.length);
        assertEquals("SensorInfo context equals", CONTEXT[0],
                infos[0].getContextType());

        infos = SensorManager.findSensors(null, CONTEXT[1]);
        assertEquals("SensorInfo count equals", 3, infos.length);
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[0].getContextType());
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[1].getContextType());
        assertEquals("SensorInfo context equals", CONTEXT[1],
                infos[2].getContextType());
    }

    public void runTests() {
        try {
            declare("testSensorCreation");
            testSensorCreation();
            
            declare("testFindSensors1");
            testFindSensors1();
            
            declare("testFindSensors2");
            testFindSensors2();

            declare("testFindSensorsByEqualContext");
            testFindSensorsByEqualContext();

        } catch (Throwable t) {
            fail("" + t);
        }
    }    
}
