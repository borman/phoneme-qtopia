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

import java.util.*;
import javax.microedition.sensor.*;

public class DefaultConfigurator implements Configurator {

    /**
     * Configure sensor according to
     * imolementation and input data.
     *
     * @param num sensor number based 0
     * @return new sensor instance
     *
     * @throws BadConfigurationException in case of configuration fails
     *
     */
    public Sensor configure(int num) throws BadConfigurationException {

        if (num < 0) {
            throw new BadConfigurationException("Wrong sensor number");
        }
        
        String sensorPropRoot = SensorConf.PROP_DOMAIN + Integer.toString(num) + ".";
        
        /* Get description */
        String descr = SensorConf.getProperty(sensorPropRoot + "description");
        if ((descr == null) || (descr.length() == 0)) {
            throw new BadConfigurationException("description");
        }

        /* Get quantity */
        String q = SensorConf.getProperty(sensorPropRoot + "quantity");
        if ((q == null) || (q.length() == 0) || (!checkAlphaNum(q))) {
            throw new BadConfigurationException("quantity");
        }

        /* Get context type */
        String cType = SensorConf.getProperty(sensorPropRoot + "context");
        if ((cType == null) || (cType.length() == 0)) {
            throw new BadConfigurationException("context type is not set");
        }

        String[] contextTypes = {SensorInfo.CONTEXT_TYPE_AMBIENT,
                                SensorInfo.CONTEXT_TYPE_DEVICE,
                                SensorInfo.CONTEXT_TYPE_USER,
                                SensorInfo.CONTEXT_TYPE_VEHICLE};
        boolean isFound = false;
        for (int i = 0; i < contextTypes.length; i++) {
            if (contextTypes[i].equals(cType)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            throw new BadConfigurationException("context type has wrong value");
        }

        /* Get connection type */
        String connType = SensorConf.getProperty(sensorPropRoot + "connection");
        if ((connType == null) || (connType.length() == 0)) {
            throw new BadConfigurationException("connection type is not set");
        }

        String[] connTypes = {"embedded", "remote", "short_range", "wired"};
        int[] connValues = {SensorInfo.CONN_EMBEDDED, SensorInfo.CONN_REMOTE,
                            SensorInfo.CONN_SHORT_RANGE_WIRELESS,
                            SensorInfo.CONN_WIRED};
        int connTypeValue = 0;
        isFound = false;
        for (int i = 0; i < connTypes.length; i++) {
            if (connTypes[i].equals(connType)) {
                isFound = true;
                connTypeValue = connValues[i];
                break;
            }
        }
        if (!isFound) {
            throw new BadConfigurationException("connection type has wrong value");
        }

        /* Get model */
        String model = SensorConf.getProperty(sensorPropRoot + "model");
        if ((model == null) || (model.length() == 0) || (!checkAlphaNum(model))) {
            throw new BadConfigurationException("model");
        }

        /* Get "always on" property */
        boolean isAlwaysOn = false;
        String tmpStr = SensorConf.getProperty(sensorPropRoot + "always_on");
        if (tmpStr != null && tmpStr.equalsIgnoreCase("true")) {
            isAlwaysOn = true;
        }

        /* Get "availability push supported" property */
        boolean isAvailabilityPushSupported = false;
        tmpStr = SensorConf.getProperty(sensorPropRoot +
            "availability_push_support");
        if (tmpStr != null && tmpStr.equalsIgnoreCase("true")) {
            if (isAlwaysOn) {
                throw new BadConfigurationException("Availability push "+
                    "couldn't be supported when sensor is always on");
            }
            isAvailabilityPushSupported = true;
        }

        /* Get "condition push supported" property */
        boolean isConditionPushSupported = false;
        tmpStr = SensorConf.getProperty(sensorPropRoot +
            "condition_push_support");
        if (tmpStr != null && tmpStr.equalsIgnoreCase("true")) {
            isConditionPushSupported = true;
        }

        /* Error codes */
        Hashtable errorCodes = new Hashtable();
        String listOfErrorCodes = SensorConf.getProperty(sensorPropRoot +
            "listOfErrorCodes");
        if (listOfErrorCodes != null) {
            int errorCode;
            for (int pos = 0; pos < listOfErrorCodes.length(); ) {
                int i = listOfErrorCodes.indexOf(' ', pos);
                if (i < 0) {
                    i = listOfErrorCodes.length();
                }
                errorCode = 0;
                try {
                    errorCode = 
                        Integer.parseInt(listOfErrorCodes.substring(pos, i));
                } catch (NumberFormatException exc) {
                    throw new BadConfigurationException("Wrong format " +
                        "of number of error code");
                }
                if (errorCode < 1) {
                    throw new BadConfigurationException("Error code " +
                        "must be positive");
                }
                if (errorCodes.containsKey(new Integer(errorCode))) {
                    throw new BadConfigurationException("Error code " +
                        errorCode + " is not unique");
                }
                tmpStr = SensorConf.getProperty(sensorPropRoot +
                    "errorDescription" + errorCode);
                if (tmpStr == null) {
                    throw new BadConfigurationException("Error code " +
                        errorCode + " is not documented");
                }
                errorCodes.put(new Integer(errorCode), tmpStr);
            }
        }

        int maxBufferSize;
        SensorProperties sensorProps;
        try {
            /* Get maxBufferSize */
            String mbs = SensorConf.getProperty(sensorPropRoot + "maxBufferSize");
            if ((mbs == null) || (mbs.length() == 0)) {
                throw new BadConfigurationException("maxBufferSize");
            }
            maxBufferSize = Integer.parseInt(mbs);
            if (maxBufferSize < 256) {
                throw new BadConfigurationException("maxBufferSize < 256");
            }
            
            /* Read optional properties */
            sensorProps = new DefaultSensorProperties();
            String[] listPredefinedNames = {SensorInfo.PROP_LATITUDE,
                                            SensorInfo.PROP_LOCATION,
                                            SensorInfo.PROP_LONGITUDE,
                                            SensorInfo.PROP_MAX_RATE,
                                            SensorInfo.PROP_VENDOR,
                                            SensorInfo.PROP_VERSION,
                                            SensorInfo.PROP_IS_CONTROLLABLE,
                                            SensorInfo.PROP_IS_REPORTING_ERRORS};
            String[] listPredefinedTypes = {"D", "S", "D", "F", "S", "S", "B", "B"};
            String listPropNames = SensorConf.getProperty(sensorPropRoot + "proplist");

            if (listPropNames != null) {
                for (int pos = 0; pos < listPropNames.length(); ) {
                    int i = listPropNames.indexOf(' ', pos);
                    if (i < 0) {
                        i = listPropNames.length();
                    }
                    String key = listPropNames.substring(pos, i);
                    String propValue = SensorConf.getProperty(sensorPropRoot + "prop." + key);

                    /* Check if property name is predefined */
                    isFound = false;
                    int j;
                    for (j = 0; j < listPredefinedNames.length; j++) {
                        if (listPredefinedNames[j].equals(key)) {
                            isFound = true;
                            break;
                        }
                    }

                    if (isFound && propValue == null) {
                        throw new BadConfigurationException("Predefined property has no value");
                    }

                    Object obj = null;
                    if (propValue != null) {

                        char[] propValTypes = {'I', 'D', 'F'};
                        boolean isTypeFound = false;
                        for (int k = 0; k < propValTypes.length; k++) {
                             if (propValue.startsWith("%") &&
                                 propValue.length() > 1 &&
                                 propValue.charAt(1) == propValTypes[k]) {
                                 if (isFound && 
                                     propValTypes[k] != listPredefinedTypes[j].charAt(0)) {
                                     throw
                                         new BadConfigurationException(
                                         "Predefined property has wrong type");
                                 }
                                 switch (propValTypes[k]) {
                                     case 'I':
                                         obj = 
                                             new Integer(
                                             Integer.parseInt(propValue.substring(2)));
                                         break;
                                     case 'D':
                                         obj = 
                                             new Double(
                                             Double.parseDouble(propValue.substring(2)));
                                         break;
                                     case 'F':
                                         obj = 
                                             new Float(
                                             Float.parseFloat(propValue.substring(2)));
                                         break;
                                 }
                                 isTypeFound = true;
                                 break;
                             }
                        }
                        /* Check for String type */
                        if (!isTypeFound) {
                            if (isFound && propValue.trim().length() == 0) {
                                 throw new BadConfigurationException(
                                     "Predefined string property is empty");
                            }
                            if (propValue.equalsIgnoreCase("true") ||
                                propValue.equalsIgnoreCase("false")) {
                                 if (isFound && 
                                     'B' != listPredefinedTypes[j].charAt(0)) {
                                     throw
                                         new BadConfigurationException(
                                         "Predefined property has wrong type");
                                 }
                                obj = new Boolean(propValue.equalsIgnoreCase("true"));
                            } else {
                                if (isFound && 
                                    'S' != listPredefinedTypes[j].charAt(0)) {
                                    throw
                                        new BadConfigurationException(
                                        "Predefined property has wrong type");
                                }
                                obj = propValue;
                            }
                        }
                        sensorProps.setProperty(key, obj);
                    }
                    pos = i + 1;
                }
            }
        } catch (NumberFormatException e) {
            throw new BadConfigurationException("Number format");
        }

        /* Get number of channels */
        int channelCount = SensorConf.getIntProperty(sensorPropRoot + "channelCount", 0);
        if (channelCount <= 0) {
            throw new BadConfigurationException("Channel count must be > 0");
        }
        
        ChannelImpl[] channels = new ChannelImpl[channelCount];
        
        for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
            String channelPropDomain = sensorPropRoot + "ch" + Integer.toString(channelIndex) + ".";
            
            /* Channel name */            
            String propName = channelPropDomain + "name";
            String propValue = SensorConf.getProperty(propName);
            if ((propValue == null) || (propValue.length() == 0)) {
                throw new BadConfigurationException(propName);
            }
            String channelName = new String(propValue);
            
            /* Channel data type */
            propName = channelPropDomain + "dataType";
            int dataType = SensorConf.getIntProperty(propName, -1);
            if (
                    (dataType != ChannelInfo.TYPE_DOUBLE) && 
                    (dataType != ChannelInfo.TYPE_INT) && 
                    (dataType != ChannelInfo.TYPE_OBJECT)) {
                throw new BadConfigurationException("Wrong data type of " + propName);
            }
            if (isConditionPushSupported &&
                dataType == ChannelInfo.TYPE_OBJECT) {
                throw new BadConfigurationException("Data type couldn't be object " +
                    "when sensor supports condition push");
            }
            /* Accuracy */
            propName = channelPropDomain + "accuracy";
            propValue = SensorConf.getProperty(propName);
            if (propValue == null) {
                throw new BadConfigurationException(propName);
            }
            float chAccuracy;
            try {
                chAccuracy = Float.parseFloat(propValue);
            } catch (NumberFormatException e) {
                throw new BadConfigurationException(propName);
            }
            
            /* Scale */
            propName = channelPropDomain + "scale";
            int chScale = SensorConf.getIntProperty(propName, 0);

            /* Unit */
            propName = channelPropDomain + "unit";
            propValue = SensorConf.getProperty(propName);
            if ((propValue == null) || (propValue.length() == 0)) {
                throw new BadConfigurationException(propName);
            }
            Unit chUnit = Unit.getUnit(propValue);
            
            /* Measurement ranges */
            MeasurementRange ranges[] = null;
            /****************************/
            if (dataType != ChannelInfo.TYPE_OBJECT) {
                propName = channelPropDomain + "rangeCount";
                int rangeCount = SensorConf.getIntProperty(propName, 0);
                if (rangeCount < 0) {
                    throw new BadConfigurationException("Wrong range count of " + propName);
                }

                if (rangeCount > 0) {
                    ranges = new MeasurementRange[rangeCount];
                    for (int i = 0; i < rangeCount; i++) {
                        String rangePropDomain = channelPropDomain + "r" + Integer.toString(i);
                        try {
                            /* smallest */
                            propName = rangePropDomain + "smallest";
                            propValue = SensorConf.getProperty(propName);
                            if ((propValue == null) || (propValue.length() == 0)) {
                                throw new BadConfigurationException(propName);
                            }
                            double smallest = Double.parseDouble(propValue);

                            /* largest */
                            propName = rangePropDomain + "largest";
                            propValue = SensorConf.getProperty(propName);
                            if ((propValue == null) || (propValue.length() == 0)) {
                                throw new BadConfigurationException(propName);
                            }
                            double largest = Double.parseDouble(propValue);

                            /* resolution */
                            propName = rangePropDomain + "resolution";
                            propValue = SensorConf.getProperty(propName);
                            if ((propValue == null) || (propValue.length() == 0)) {
                                throw new BadConfigurationException(propName);
                            }
                            double resolution = Double.parseDouble(propValue);
                        
                            ranges[i] = new MeasurementRange(smallest, largest, resolution);
                        } catch (NumberFormatException e) {
                            throw new BadConfigurationException(propName);
                        }
                    }
                }
            }
            
            channels[channelIndex] = new ChannelImpl(num, channelIndex, channelName, dataType,
                                                    chAccuracy, chScale, chUnit, ranges);
        }

        SensorDevice sDev = DeviceFactory.generateSensor(num,channelCount);
        if (sDev == null) {
            throw new BadConfigurationException("Sensor "+num+" hasn't been created by DeviceFactory");
        }
        Sensor sensor = new Sensor(num, descr, q, cType, model, maxBufferSize,
            connTypeValue, sensorProps, isAvailabilityPushSupported,
            isConditionPushSupported, channels, sDev, errorCodes);
        // SensorDevice does not know anything about SensorInfo to which it
        // belongs to, so we assign them
        NativeSensorRegistry.register(sDev, sensor);
        return sensor;
    }
    
    /**
     * Checks that input string contains alphanum
     * symbols only.
     *
     * @param str input string
     *
     * @return true when all symbols are alphanum else false
     */
    private boolean checkAlphaNum(String str) {
        if (str == null) {
            return false;
        }
        boolean retVal = true;
        for (int i = 0; i < str.length(); i++) {
            if (!SensorUrl.isAlphaNum(str.charAt(i))) {
                retVal = false;
                break;
            }
        }
        return retVal;
    }
}
