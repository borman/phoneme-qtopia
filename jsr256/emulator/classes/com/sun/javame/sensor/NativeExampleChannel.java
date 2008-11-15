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

public class NativeExampleChannel extends TestChannelDevice {

    private int[] buffer = new int[1];

    /**
      * Constructor of the device.
      *
      * @param numberSensor - number of sensor
      * @param numberChannel - number of channel
     * @param sensorType Sensor type. This is an ID needed only at the native level.
      */
     public NativeExampleChannel(int numberSensor, int numberChannel, int sensorType) {
         super(numberSensor, numberChannel, sensorType);
     }

   /**
     * Initialization of channel device.
     *
     * @return true when initialization of channel device
     * was OK else false
     */
    public boolean initChannel() {
        /*
         * IMPL_NOTE: with one channel per sensor, everything is done in
         * sensor initialization code in the corresponding SensorDevice,
         * namely, NativeExampleSensor.initSensor(). 
         */
        return true;
    }

   /**
     * Measures the next data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @param sensorType
     * @return error code of measuring
     */
    protected synchronized int measureData(int sensorNumber, int channelNumber, int sensorType) {
       // memorize data inside
       return doMeasureData(sensorType,channelNumber,buffer);

    }

   /**
     * Measures the next data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @param sensorType
     * @return error code of measuring
     */
    private native int doMeasureData(int sensorType, int channelNumber, int[] buffer);

   /**
     * Gets the last data from channel.
     *
     * @param sensorNumber - number of sensor
     * @param channelNumber - number of channel
     * @return data of measuring
     */
    protected Object[] getData(int sensorNumber, int channelNumber) {
        Integer[] data = {new Integer(buffer[0])};
        return data;
    }

    /**
      * Gets the last uncertainty from channel.
      *
      * @param sensorNumber - number of sensor
      * @param channelNumber - number of channel
      * @return uncertainty of measuring
      */
     protected float getUncertainty(int sensorNumber, int channelNumber) {
        // IMPL_NOTE: place here returning uncertainty from last measuring
        return 0.05f;
    }

    /**
      * Gets the last validity from channel.
      *
      * @param sensorNumber - number of sensor
      * @param channelNumber - number of channel
      * @return validity of measuring
      */
     protected boolean getValidity(int sensorNumber, int channelNumber) {
         // IMPL_NOTE: place here returning validity from last measuring
         return true;
    }

   /**
     * Checks is channel device available.
     *
     * @return true when channel device is available else false
     */
    public boolean isAvailable() {
        return true; // always available
    }

    /**
     * Sets the data buffer for tests (emulator).
     *
     * @param data data for placing into sensor for tests
     */
    synchronized void setTestData(Object[] data) {
        if (data[0] instanceof Integer) {
            int[] intData = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                intData[i] = ((Integer)data[i]).intValue();
            }
            setTestIntData(intData);
        } else if (data[0] instanceof Double) {
            double[] doubleData = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                doubleData[i] = ((Double)data[i]).doubleValue();
            }
            setTestDoubleData(doubleData);
        }
    }

    /**
     * Sets the integer data buffer for tests (emulator).
     *
     * @param data integer data for placing into sensor for tests
     */
    private native void setTestIntData(int[] data);

    /**
     * Sets the double data buffer for tests (emulator).
     *
     * @param data double data for placing into sensor for tests
     */
    private native void setTestDoubleData(double[] data);
}