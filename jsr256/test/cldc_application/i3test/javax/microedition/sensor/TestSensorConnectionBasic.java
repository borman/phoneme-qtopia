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
import javax.microedition.io.*;
import java.io.*;
import com.sun.javame.sensor.*;

public class TestSensorConnectionBasic extends TestCase {
    
    /** Creates a new instance of TestSensorConnectionBasic */
    public TestSensorConnectionBasic() {
    }
    
    private void testConnectionOpenOne() throws IOException {
        SensorInfo[] info = SensorManager.findSensors(TestSensorRegistry.QUANTITY[1], null);
        assertTrue(info.length == 1);
        
        SensorConnection conn = (SensorConnection)Connector.open(info[0].getUrl());
        assertTrue(conn == info[0]);
        assertTrue(conn.getState() == SensorConnection.STATE_OPENED);
        conn.close();
    }

    private void testConnectionOpenAll() throws IOException {
        SensorInfo[] info = SensorManager.findSensors(null, null);
        assertTrue(info.length == TestSensorRegistry.SENSOR_COUNT);
        
        for (int i = 0; i < info.length; i++) {
            SensorConnection conn = (SensorConnection)Connector.open(info[i].getUrl());
            assertTrue(conn == info[i]);
            assertTrue(conn.getState() == SensorConnection.STATE_OPENED);
            conn.close();
        }
    }
    
    public void runTests() {
        try {
            declare("testConnectionOpenOne");
            testConnectionOpenOne();
            
            declare("testConnectionOpenAll");
            testConnectionOpenAll();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }            
}
