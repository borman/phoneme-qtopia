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

import com.sun.midp.events.EventQueue;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.NativeEvent;

public class TestingSensor extends SensorDevice {

    private static TestingSensor INSTANCE;

    /** Current availability state. */
    private volatile boolean available;

    /** If state of the monitoring service - start/stop monitoring calls. */
    private volatile boolean monitorAvailability;

    /** If true the sensor sends notifications from native code. */
    private volatile boolean nativeAvailability;

    /** If true the sensor actively sends notifications. */
    private volatile boolean notificationEnabled;

    public static synchronized TestingSensor getInstance(int numberSensor,
            int channelCount) {
        if (INSTANCE == null) {
            INSTANCE = new TestingSensor(numberSensor, channelCount,
                    DeviceFactory.SENSOR_TESTER);
        }
        return INSTANCE;
    }

    public static synchronized TestingSensor tryGetInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        return INSTANCE;
    }

    private TestingSensor(int numberSensor, int numberOfChannels,
            int sensorType) {
        super(numberSensor, numberOfChannels, sensorType);
    }

    public boolean finishSensor() {
        return true;
    }

    public boolean initSensor() {
        return true;
    }

    public boolean isAvailable() {
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        if (monitorAvailability && notificationEnabled) {
            if (nativeAvailability) {
                setAvailableNative(sensorType, available);
            } else {
                NativeEvent event = new NativeEvent(EventTypes.SENSOR_EVENT);
                event.intParam1 = NativeSensorRegistry.EVENT_AV_LISTENER_CODE;
                event.intParam2 = sensorType;
                event.intParam3 = available ? 1 : 0;
                EventQueue.getEventQueue().post(event);
            }
        }
    }

    public void startMonitoringAvailability(AvailabilityListener listener) {
        monitorAvailability = true;
        NativeSensorRegistry.startMonitoringAvailability(sensorType, listener);
    }

    public void stopMonitoringAvailability() {
        NativeSensorRegistry.stopMonitoringAvailability(sensorType);
        monitorAvailability = false;
    }

    public void setNativeAvailability(boolean nativeAvailability) {
        this.nativeAvailability = nativeAvailability;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    private native void setAvailableNative(int numberSensor, boolean available);
}
