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

import com.sun.javame.sensor.TestingSensor;
import com.sun.midp.i3test.TestCase;

public class TestSensorManager extends TestCase {

    private final static SensorInfo DUMMY_SENSOR_INFO = new SensorInfo() {
        public ChannelInfo[] getChannelInfos() {
            return null;
        }

        public int getConnectionType() {
            return 0;
        }

        public String getContextType() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public int getMaxBufferSize() {
            return 0;
        }

        public String getModel() {
            return null;
        }

        public Object getProperty(String name) {
            return null;
        }

        public String[] getPropertyNames() {
            return null;
        }

        public String getQuantity() {
            return null;
        }

        public String getUrl() {
            return null;
        }

        public boolean isAvailabilityPushSupported() {
            return false;
        }

        public boolean isAvailable() {
            return false;
        }

        public boolean isConditionPushSupported() {
            return false;
        }
    };

    private final static SensorListener DUMMY_SENSOR_LISTENER = new SensorListener() {

        public void sensorAvailable(SensorInfo info) {
        }

        public void sensorUnavailable(SensorInfo info) {
        }
    };

    private void testAddSensorListenerThrowsNullPointerException() {
        // Just to remove the ERROR message in i3test
        assertTrue(true);
        // NullPointerException - if either of the parameters is null
        try {
            SensorManager.addSensorListener(null, (SensorInfo) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
        try {
            SensorManager.addSensorListener(null, DUMMY_SENSOR_INFO);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
        try {
            SensorManager.addSensorListener(DUMMY_SENSOR_LISTENER,
                    (SensorInfo) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
        // NullPointerException - if the listener, or the quantity is null
        try {
            SensorManager.addSensorListener(null, (String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
        try {
            SensorManager.addSensorListener(DUMMY_SENSOR_LISTENER,
                    (String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
        try {
            SensorManager.addSensorListener(null, "");
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
    }

    private void testAddSensorListenerThrowsIllegalArgumentException() {
        // Just to remove the ERROR message in i3test
        assertTrue(true);
        // IllegalArgumentException - if info does not match to any of the
        // provided sensors
        try {
            SensorManager.addSensorListener(DUMMY_SENSOR_LISTENER,
                    DUMMY_SENSOR_INFO);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignore) {
        }
    }

    private void testRemoveSensorListenerThrowsNull() {
        // Just to remove the ERROR message in i3test
        assertTrue(true);
        try {
            SensorManager.removeSensorListener(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignore) {
        }
    }

    private void testRemoveNonExistingSensorListener() {
        // Just to remove the ERROR message in i3test
        assertTrue(true);
        // We call this two times to be sure the listener is not registered
        SensorManager.removeSensorListener(DUMMY_SENSOR_LISTENER);
        SensorManager.removeSensorListener(DUMMY_SENSOR_LISTENER);
    }

    class MockSensorListener implements SensorListener {

        private int available;
        private int unavailable;
        private boolean isavailable;

        public void sensorAvailable(SensorInfo info) {
            incAvailable();
        }

        public void sensorUnavailable(SensorInfo info) {
            incUnAvailable();
        }

        private synchronized void incAvailable() {
            available++;
            isavailable = true;
        }

        private synchronized void incUnAvailable() {
            unavailable++;
            isavailable = false;
        }

        // Mock validation methods

        private int expectedAvailable;
        private int expectedUnavailable;
        private boolean expectedIsavailable;

        synchronized void validate() {
            assertEquals("Available calls count should equal",
                    expectedAvailable, available);
            assertEquals("Unavailable calls count should equal",
                    expectedUnavailable, unavailable);
            assertTrue("Availability should equal",
                    expectedIsavailable == isavailable);
        }

        synchronized public void expectCallback(boolean available) {
            if (available) {
                expectedAvailable++;
            } else {
                expectedUnavailable++;
            }
            expectedIsavailable = available;
        }
    }

    class MalliciousMockSensorListener extends MockSensorListener {

        /** To avoid throwing from the addSensorListener method. */ 
        private volatile boolean firstTime = true;

        public void sensorAvailable(SensorInfo info) {
            super.sensorAvailable(info);
            if (!firstTime) {
                throw new RuntimeException("Mallicious throw");
            }
        }

        public void sensorUnavailable(SensorInfo info) {
            super.sensorUnavailable(info);
            if (!firstTime) {
                throw new RuntimeException("Mallicious throw");
            }
            firstTime = false;
        }
    }

    private void testMultipleAddSensorListenerCalls(boolean available,
            MockSensorListener listener) {
        SensorInfo[] infos = SensorManager.findSensors("sensor:sensor_tester");
        assertTrue(infos.length > 0);
        // Retrieve the internal class
        TestingSensor sensor = TestingSensor.tryGetInstance();

        // Test the initial listener state
        listener.validate();
        sensor.setAvailable(available);
        assertTrue(available == infos[0].isAvailable());
        listener.validate();
        try {
            // Add listener and test changed state
            SensorManager.addSensorListener(listener, infos[0]);
            listener.expectCallback(available);
            listener.validate();

            // This call should be ignored, check state
            SensorManager.addSensorListener(listener, infos[0]);
            listener.validate();
        } finally {
            SensorManager.removeSensorListener(listener);
        }
    }

    private void testSwapAvailabilityState(long timeout, boolean async,
            boolean nativeAsync, MockSensorListener listener)
            throws InterruptedException {
        SensorInfo[] infos = SensorManager.findSensors("sensor:sensor_tester");
        assertTrue(infos.length > 0);
        // Retrieve and setup the internal class
        TestingSensor sensor = TestingSensor.tryGetInstance();
        sensor.setNotificationEnabled(async);
        sensor.setNativeAvailability(nativeAsync);

        boolean state = false;
        listener.validate();
        try {
            // Add listener, counters are incremented immediately
            SensorManager.addSensorListener(listener, infos[0]);
            listener.expectCallback(state);
            // Check and swap several times
            for (int i = 0; i < 10; i++) {
                listener.validate();
                // Swap state
                state = !state;
                sensor.setAvailable(state);
                assertTrue("Info should be updated immediately",
                        state == infos[0].isAvailable());
                listener.expectCallback(state);
                // Wait for poller action
                Thread.sleep(timeout);
            }
        } finally {
            SensorManager.removeSensorListener(listener);
        }
        // Swap state without attached listener
        for (int i = 0; i < 10; i++) {
            sensor.setAvailable(!infos[0].isAvailable());
        }
        // Wait for poller action
        Thread.sleep(timeout);
        // Check that listener state was not changed after removal
        listener.validate();
    }

    public void runTests() throws Exception {

        declare("testAddSensorListenerThrowsNullPointerException");
        testAddSensorListenerThrowsNullPointerException();

        declare("testAddSensorListenerThrowsIllegalArgumentException");
        testAddSensorListenerThrowsIllegalArgumentException();

        declare("testRemoveSensorListenerThrowsNull");
        testRemoveSensorListenerThrowsNull();

        declare("testRemoveNonExistingSensorListener");
        testRemoveNonExistingSensorListener();

        // Tests with broken listeners are called first to know whether they
        // broke something
        declare("testMultipleAddSensorListenerCalls: available=true, mallicious");
        testMultipleAddSensorListenerCalls(true,
                new MalliciousMockSensorListener());

        declare("testMultipleAddSensorListenerCalls: available=false, mallicious");
        testMultipleAddSensorListenerCalls(false,
                new MalliciousMockSensorListener());

        declare("testSwapAvailabilityState: sync, mallicious");
        testSwapAvailabilityState(2000, false, false,
                new MalliciousMockSensorListener());

        declare("testSwapAvailabilityState: async, nativeAsync=false, mallicious");
        testSwapAvailabilityState(100, true, false,
                new MalliciousMockSensorListener());

        declare("testSwapAvailabilityState: async, nativeAsync=true, mallicious");
        testSwapAvailabilityState(100, true, true,
                new MalliciousMockSensorListener());

        // Tests with ordinary listeners
        declare("testMultipleAddSensorListenerCalls: available=true");
        testMultipleAddSensorListenerCalls(true, new MockSensorListener());

        declare("testMultipleAddSensorListenerCalls: available=false");
        testMultipleAddSensorListenerCalls(false, new MockSensorListener());

        declare("testSwapAvailabilityState: sync");
        testSwapAvailabilityState(2000, false, false, new MockSensorListener());

        declare("testSwapAvailabilityState: async, nativeAsync=false");
        testSwapAvailabilityState(100, true, false, new MockSensorListener());

        declare("testSwapAvailabilityState: async, nativeAsync=true");
        testSwapAvailabilityState(100, true, true, new MockSensorListener());
    }
}
