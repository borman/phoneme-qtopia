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

import java.io.*;
import java.util.*;
import javax.microedition.sensor.*;


public class Sensor implements SensorInfo, SensorConnection,
        ChannelDataListener, AvailabilityNotifier {

    /* Sensor information */
    private int number;
    private String description;
    private String contextType;
    private String model;
    private int maxBufferSize;
    private int connType;
    private String quantity;

    /** device-specific code relating the whole sensor */
    private SensorDevice sensorDevice = null;

    /** Channel count. */
    private int channelCount;

    /** Data array which red from sensor. */
    private DataImpl[] retData;

    /** Listener for notification about data receiving. */
    private DataListener listener;

    /** Last channel status. */
    private int channelStatus;

    /** Channel number. */
    private int channelNumber;

    /** Error timestamp. */
    private long errorTimestamp;

    /** Sensor properties */
    SensorProperties props;
    
    /** Sensor state */
    private int state;
    
    /** Sensor state for data collecting */
    private int stateData;

    /** Channels */
    private ChannelImpl[] channels;

    /** Availability push supporting flag. */
    private boolean isSensorAvailabilityPushSupported;

    /** Condition push supporting flag. */
    private boolean isSensorConditionPushSupported;

    /** Flag of notification. */
    private boolean isNotify;

    /** ListenerProcess counter. */
    private int listeningProcessCounter = 0;

    /** Sensor message queue */
    private Vector messages = new Vector();

    /** Error codes table */
    private Hashtable errorCodes;

    /** 
     * Creates a new instance of Sensor.
     *
     * @param number number of the sensor
     * @param description description of the sensor
     * @param quantity quantity of the sensor
     * @param contextType context type of the sensor
     * @param model model of the sensor
     * @param maxBufferSize maximal buffer size of the sensor
     * @param connType connection type of the sensor
     * @param props properties of the sensor
     * @param isSensorAvailabilityPushSupported true when sensor supports
     * availability push
     * @param isSensorConditionPushSupported true when sensor supports
     * condition push
     * @param channels channels of the sensor
     * @param sensorDev an instance suited for this particular sensor
     * @param errorCodes the hashtable of error codes and its descriptions
     */
    Sensor(int number, String description, String quantity, String contextType,
           String model, int maxBufferSize, int connType,
           SensorProperties props, boolean isSensorAvailabilityPushSupported,
           boolean isSensorConditionPushSupported, ChannelImpl[] channels, SensorDevice sensorDev,
           Hashtable errorCodes) {
        this.number = number;
        this.description = description;
        this.quantity = quantity;
        this.contextType = contextType;
        this.model = model;
        this.maxBufferSize = maxBufferSize;
        this.connType = connType;
        this.props = props;
        this.channels = channels;
        this.isSensorAvailabilityPushSupported = isSensorAvailabilityPushSupported;
        this.isSensorConditionPushSupported = isSensorConditionPushSupported;
        this.sensorDevice = sensorDev;
        this.errorCodes = errorCodes;
        if (channels != null && channels.length > 0) {
            for (int i = 0; i < channels.length; i++) {
                channels[i].setSensor(this);
            }
        }
        state = STATE_CLOSED;
        stateData = StatesEvents.SENSOR_IDLE;
    }

    /**
     * Gets the sensor's ChannelInfo array
     * representing channels of the sensor.
     *
     * @return ChannelInfo array
     */
    public ChannelInfo[] getChannelInfos() {
        return channels;
    }

    /**
     * Gets the connection type.
     *
     * @return one of values: CONN_EMBEDDED, CONN_REMOTE,
     * CONN_SHORT_RANGE_WIRELESS or CONN_WIRED
     */
    public int getConnectionType() {
        return connType;
    }

    /**
     * Gets the context type.
     *
     * @return one of values: CONTEXT_TYPE_USER,
     * CONTEXT_TYPE_DEVICE or CONTEXT_TYPE_AMBIENT
     */
    public String getContextType() {
        return contextType;
    }

    /**
     * Gets the description of sensor.
     *
     * @return the readable description of sensor
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the maximal data buffer length of sensor.
     *
     * @return maximal data buffer length of sensor
     */
    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    /**
     * Gets the model name of sensor.
     *
     * @return the model name of sensor
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the quantity of sensor.
     *
     * @return the quantity of sensor
     */
    public String getQuantity() {
        return quantity;
    }


    /**
     * Gets URL needed to open SensorConnection.
     *
     * @return the URL needed to open SensorConnection
     */
    public String getUrl() {
        return SensorUrl.createUrl(this);
    }

    /**
     * Checks is sensor supports availability push.
     *
     * @return true when sensor supports availability push
     */
    public boolean isAvailabilityPushSupported() {
        return isSensorAvailabilityPushSupported;
    }

    /**
     * Checks is sensor available.
     *
     * @return true when sensor is available else false
     */
    public boolean isAvailable() {
        return sensorDevice.isAvailable();
    }

    /**
     * Checks is sensor supports condition push.
     *
     * @return true when sensor supports condition push
     */
    public boolean isConditionPushSupported() {
        return isSensorConditionPushSupported;
    }

    /**
     * Gets the property of sensor by name.
     *
     * @param name the name of property
     * @return the quantity of sensor
     */
    public Object getProperty(String name) {        
        if (name == null) {
            throw new NullPointerException();
        }

        if (!props.containsName(name)) {
            throw new IllegalArgumentException();
        }

        return props.getProperty(name);
    }

    /**
     * Gets the array of property names of sensor.
     *
     * @return an array of property keys for the sensor
     */
    public String[] getPropertyNames() {
        return props.getPropertyNames();
    }

    /**
     * Checks is sensor contains given quantity and context type.
     *
     * @return true when sensor contains given quantity and context type
     */
    public boolean matches(String quantity, String contextType) {
        return ((quantity == null) || quantity.equals(this.quantity)) &&
               ((contextType == null) || contextType.equals(this.contextType));
    }

    /**
     * Checks is sensor matches to given URL.
     *
     * @return true when sensor matches to given URL
     */
    public boolean matches(SensorUrl url) {
        String location = (String)props.getProperty(PROP_LOCATION);

        return quantity.equals(url.getQuantity()) &&
               ((url.getContextType() == null) || url.getContextType().equals(contextType)) &&
               ((url.getLocation() == null) || url.getLocation().equals(location)) &&
               ((url.getModel() == null) || url.getModel().equals(model));
    }

    /**
     * Opens sensor.
     *
     * @throws IOException if the sensor has wrong state
     */
    public void open() throws IOException {

        if (state != STATE_CLOSED) {
            // Decide later if we allow multiple connections to the same sensor
            // JSR 256 spec leaves this to implementation
            throw new IOException("Sensor is already opened");
        }

        boolean isInitOk = true;
        isInitOk &= sensorDevice.initSensor();
        for (int i = 0; isInitOk && i < channels.length; i++) {
            isInitOk &= channels[i].getChannelDevice().initChannel();
        }

        if (!isInitOk) {
            throw new IOException("Sensor start fails");
        }

        state = STATE_OPENED;
    }
    
    /*
     * SensorConnection methods
     */
    
    public int getState() {
        return state;
    }
    
    public Channel getChannel(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            throw new NullPointerException();
        }

        /* In this implementation ChannelInfo is the same as Channel. So, we just
         * have to check that this specific sensor owns the channel.
         */
        for (int i = 0; i < channels.length; i++) {
            if (channelInfo == channels[i]) {
                return channels[i];
            }
        }
        
        /* This is not a channel from this sensor */
        throw new IllegalArgumentException("This channel is not from this sensor");
    }


    /**
     * Fetches data in the synchronous mode.
     *
     * @param bufferSize the size of the data buffer ( &gt; 0)
     * @return the collected data of all the channels
     * of this sensor
     * @throws IllegalArgumentException - when bufferSize &lt; 1
     * or if bufferSize &gt; the maximum size of the buffer
     * @throws java.io.IOException - if the state is STATE_CLOSED
     * or if any input/output problems are occured
     * @throws java.lang.IllegalStateException - in case of  the
     * state is STATE_LISTENING
     */
    public Data[] getData(int bufferSize) throws java.io.IOException {
        return getData(bufferSize, 0L, false, false, false);
    }


    /**
     * Retrieves data in the synchronous mode.
     *
     * @param bufferSize - the size of the data buffer 
     * @param bufferingPeriod - the time to buffer values
     * @param isTimestampIncluded - if true timestamps should be 
     *  included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be
     *  included in returned Data objects
     * @param isValidityIncluded - if true validities should be
     *  included in returned Data objects
     * @return collected data of all the channels of this sensor.
     * @throws java.lang.IllegalArgumentException - if the both, bufferSize
     *  and bufferingPeriod, have values less than 1, or if bufferSize
     *  exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if the state is STATE_LISTENING 
     * @throws java.io.IOException - if the state is STATE_CLOSED
     *  or if any input/output problems are occured
     */
   public synchronized Data[] getData(int bufferSize,
       long bufferingPeriod,
       boolean isTimestampIncluded,
       boolean isUncertaintyIncluded,
       boolean isValidityIncluded)
       throws java.io.IOException {
        if ((bufferSize < 1 && bufferingPeriod < 1) ||
            bufferSize > maxBufferSize) {
            throw new IllegalArgumentException(
                    "Wrong buffer size or/and period values");
        }

        if (state == STATE_LISTENING) {
            throw new IllegalStateException("Wrong state");
        }

        if (state == STATE_CLOSED) {
            throw new IOException("Wrong state");
        }

        if (bufferSize < 1) {
            bufferSize = maxBufferSize;
        }

        /* Sending signals to each channel to start getting data */
        long startTime = System.currentTimeMillis();
        this.listener = null;
        channelCount = 0;
        channelStatus = ValueListener.DATA_READ_OK;
        retData = new DataImpl[channels.length];
        stateData = StatesEvents.WAIT_DATA;
        for (int i = 0; i < channels.length; i++) {
            channels[i].startGetData(this, bufferSize, bufferingPeriod,
                isTimestampIncluded, isUncertaintyIncluded,
                isValidityIncluded, false, startTime);
        }
        isNotify = false;
        while (!isNotify) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if (channelStatus != ValueListener.DATA_READ_OK) {
            throw new IOException("Read data error with code " + channelStatus +
            " on channel " + channelNumber);
        }
        return retData;
    }

    /**
     * Put message to queue.
     *
     * @param msg message code
     */
    private synchronized void putMessage(int msg) {
        messages.addElement(new Integer(msg));
        NativeSensorRegistry.postSensorEvent(NativeSensorRegistry.EVENT_SENSOR_MESSAGE,
            number, 0, 0);
    }

    /**
     * Sends message to queue.
     *
     * @param msg message code
     */
    private synchronized void sendMessage(int msg) {
        messages.addElement(new Integer(msg));
        processMessage();
    }

    /**
     * Process message from queue.
     *
     */
    synchronized void processMessage() {
        while (messages.size() > 0) {
            int msg = ((Integer)messages.firstElement()).intValue();
            messages.removeElementAt(0);
            switch (msg) {
                case StatesEvents.IND_DATA: // data from channel was received
                    if (stateData == StatesEvents.WAIT_DATA) {
                        switch (state) {
                            case STATE_OPENED: // getData(...)
                                isNotify = true;
                                notify();
                                stateData = StatesEvents.SENSOR_IDLE;
                                break;
                            case STATE_LISTENING: // call listener.dataReceived(...)
                                NativeSensorRegistry.postSensorEvent(
                                    NativeSensorRegistry.EVENT_SENSOR_DATA_RECEIVED,
                                    number, 0, 0);
                                break;
                        }
                    }
                    break;
                case StatesEvents.IND_ERROR: // error from channel
                    listeningProcessCounter++;
                    try {
                        ((DataAndErrorListener)listener).errorReceived((SensorConnection)this,
                            channelStatus, errorTimestamp);
                    } catch (Exception ex) { // ignore exception in user's code
                    }
                    listeningProcessCounter--;
                    break;
                case StatesEvents.STOP_GET_DATA: // data collecting is need to stop
                    if (stateData == StatesEvents.WAIT_DATA) {
                        channelCount = 0;
                        for (int i = 0; i < channels.length; i++) {
                            channels[i].stopGetData();
                        }
                        stateData = StatesEvents.WAIT_CLOSE_DATA;
                    }
                    break;
                case StatesEvents.STOP_GET_DATA_CONF: // data collecting stop confirmation
                    if (stateData == StatesEvents.WAIT_CLOSE_DATA) {
                        if (++channelCount == channels.length) {
                            stateData = StatesEvents.SENSOR_IDLE;
                            if (state == STATE_LISTENING) {
                                isNotify = true;
                                notify();
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Notification about data from channel.
     *
     * @param number channel number
     * @param data data instance from channel
     */
    public synchronized void channelDataReceived(int number, DataImpl data) {
        retData[number] = data;
        if (++channelCount == channels.length) {
            putMessage(StatesEvents.IND_DATA);
            channelCount = 0;
        }
    }

    /**
     * Notification about channel error.
     *
     * @param number channel number
     * @param errorCode code of channel error
     * @param timeStamp timestamp of error
     */
    public synchronized void channelErrorReceived(int number, int errorCode,
        long timestamp) {
        if (state == STATE_LISTENING && stateData == StatesEvents.WAIT_DATA &&
            listener instanceof DataAndErrorListener) {
            channelStatus = errorCode;
            channelNumber = number;
            errorTimestamp = timestamp;
            putMessage(StatesEvents.IND_ERROR);
        }
    }

    /**
     * Notification stop collecting data from channel.
     *
     * @param number channel number
     */
    void confirmStopData(int number) {
        putMessage(StatesEvents.STOP_GET_DATA_CONF);
    }

    public SensorInfo getSensorInfo() {
        return this;
    }


    /**
     * Removes the DataListener registered to this SensorConnection.
     *
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public synchronized void removeDataListener() {
        if (state == STATE_CLOSED) {
            throw new IllegalStateException("Connection is already closed");
        }
        if (state == STATE_LISTENING) {
            if (listeningProcessCounter > 0) { // call from data listener
                sendMessage(StatesEvents.STOP_GET_DATA);
            } else { // call out of data listener
                putMessage(StatesEvents.STOP_GET_DATA);
                isNotify = false;
                while (!isNotify) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                listener = null;
            }
            state = STATE_OPENED;
        }
    }

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - DataListener to be registered
     * @param bufferSize - size of the buffer, value must be &gt; 0
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize &lt; 1,
     *  or if bufferSize exceeds the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection
     * is already closed
     */
    public void setDataListener(DataListener listener, int bufferSize) {
        setDataListener(listener, bufferSize, 0L, false, false, false);
    }

    /**
     * Registers a DataListener to receive collected data asynchronously.
     *
     * @param listener - the listener to be registered
     * @param bufferSize - the size of the buffer of the data values, bufferSize &lt; 1
     * means the size is left undefined
     * @param bufferingPeriod - the time in milliseconds to buffer values inside
     * one Data object. bufferingPeriod &lt; 1 means the period is left undefined.
     * @param isTimestampIncluded - if true timestamps should be included in
     * returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be included
     * in returned Data objects
     * @param isValidityIncluded - if true validities should be included in
     * returned Data objects
     * @throws java.lang.NullPointerException - if the listener is null
     * @throws java.lang.IllegalArgumentException - if the bufferSize
     * and the bufferingPeriod both are &lt; 1 or if bufferSize exceeds
     * the maximum size of the buffer
     * @throws java.lang.IllegalStateException - if this SensorConnection is already closed
     */
    public synchronized void setDataListener(DataListener listener,
                            int bufferSize,
                            long bufferingPeriod,
                            boolean isTimestampIncluded,
                            boolean isUncertaintyIncluded,
                            boolean isValidityIncluded) {

        if ((bufferSize < 1 && bufferingPeriod < 1) ||
            bufferSize > maxBufferSize) {
            throw new IllegalArgumentException(
                    "Wrong buffer size or/and period values");
        }

        if (state == STATE_CLOSED) {
            throw new IllegalStateException("Connection is closed");
        }

        if (listener == null) {
            throw new NullPointerException("Listener is null");
        }

        if (state == STATE_LISTENING) {
            removeDataListener();
        }

        state = STATE_LISTENING;

        if (bufferSize < 1) {
            bufferSize = maxBufferSize;
        }

        /* Sending signals to each channel to start getting data */
        long startTime = System.currentTimeMillis();
        this.listener = listener;
        channelCount = 0;
        channelStatus = ValueListener.DATA_READ_OK;
        stateData = StatesEvents.WAIT_DATA;
        retData = new DataImpl[channels.length];
        for (int i = 0; i < channels.length; i++) {
            channels[i].startGetData(this, bufferSize, bufferingPeriod,
                isTimestampIncluded, isUncertaintyIncluded,
                isValidityIncluded, true, startTime);
        }
    }

    /**
     * Calls data listener.
     *
     */
    void callDataListener() {
        boolean isDataLost = false;
        listeningProcessCounter++;
        if (listeningProcessCounter > 1) {
            isDataLost = true;
        }
        try {
            listener.dataReceived(this, retData, isDataLost);
        } catch (Exception ex) { // user exception - ignore
        }
        listeningProcessCounter--;
    }
    
    public void close() throws IOException {
        sensorDevice.finishSensor(); // ignore the success flag
        if (state == STATE_LISTENING) {
            removeDataListener();
        }
        state = STATE_CLOSED;
    }

    /**
     * Gets ChannelDevice instance (i3tests only).
     *
     * @param number channel number
     * @return ChannelDevice instance
     */
    ChannelDevice getChannelDevice(int number) {
        ChannelDevice device = null;
        if (0 <= number && number < channels.length) {
            device = channels[number].getChannelDevice();
        }
        return device;
    }

    /** Smart conversion to String. It's a debugging means.
     *
      * @return human-readable representation
     */
    public String toString() { // IMPL_NOTE: this is needed only for debugging.N
        return super.toString()+"{ quantity="+quantity+" contextType="+contextType+" model="+model
                +" prop:location="+props.getProperty(PROP_LOCATION)+"}";
    }

    /**
     * Inform SensorDevice to start sending availability informations.
     *
     * @param listener which will receive the notifications
     */
    public void startMonitoringAvailability(AvailabilityListener listener) {
        sensorDevice.startMonitoringAvailability(listener);
    }

    /**
     * Inform SensorDevice to stop sending availability informations.
     *
     * @param listener which will stop receiving the notifications
     */
    public void stopMonitoringAvailability(AvailabilityListener listener) {
        sensorDevice.stopMonitoringAvailability();
    }

    /**
     * Gets the sensor error codes.
     *
     * @return array of error codes specified for the given sensor 
     */
    public int[] getErrorCodes() {
        int[] retV = new int[errorCodes.size()];
        if (retV.length > 0) {
            Enumeration enumErrCodes = errorCodes.keys();
            for (int i = 0; enumErrCodes.hasMoreElements(); i++) {
                retV[i] = ((Integer)(enumErrCodes.nextElement())).intValue();
            }
        }
        return retV;
    }

    /**
     * Gets the error description.
     *
     * @param errorCode code of the error
     * @return description of error
     */
    public String getErrorText(int errorCode) {
        Integer errCodeObject = new Integer(errorCode);
        if (!errorCodes.containsKey(errCodeObject)) {
            throw new IllegalArgumentException("Wrong error code");
        }
        return (String)errorCodes.get(errCodeObject);
    }
}
