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

import com.sun.midp.main.Configuration;

class SensorConf {

    public final static String PROP_DOMAIN = "com.sun.javame.sensor";

    /**
     * Gets the implementation property indicated by the specified key.
     *
     * @param      key   the name of the implementation property.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getProperty(String key) {
        return Configuration.getProperty(key);
    }

    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specified default value as an int.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command
     *                  line over rides.
     *
     * @return     the int value of the implementation property,
     *             or <code>def</code> if there is no property with that key or
     *             the config value is not an int.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static int getIntProperty(String key, int def) {
        return Configuration.getIntProperty(key, def);
    }

    /**
     * Get the availability poller sleep configuration.
     *
     * @return availability poller sleep in milliseconds
     */
    public static int getAvailabilityPollerSleep() {
        return Configuration.getPositiveIntProperty(
                "com.sun.javame.sensor.AvailabilityPoller."
                        + "POLLER_SLEEP_CONSTANT", 1000);
    }

    /**
     * Get the instances of Configurator objects from configuration.
     *
     * @return array of {@link Configurator} instances
     * @throws BadConfigurationException on any configuration or Configurator
     *         instantiation error
     */
    public static Configurator[] getConfigurators()
            throws BadConfigurationException {
        int count = getIntProperty(PROP_DOMAIN + ".count", 0);
        Configurator[] configurators = new Configurator[count];

        for (int i = 0; i < count; i++) {
            String sensorDomain = PROP_DOMAIN + Integer.toString(i);
            String sensorConfigClassName = getProperty(sensorDomain
                    + ".configurator");
            if (sensorConfigClassName == null) {
                throw new BadConfigurationException(
                        "no class name (" + i + ")");
            }

            try {
                Object objConf = Class.forName(sensorConfigClassName)
                        .newInstance();

                if (objConf instanceof Configurator) {
                    configurators[i] = (Configurator) objConf;
                } else {
                    throw new BadConfigurationException("bad class name (" + i
                            + ")");
                }
            } catch (ClassNotFoundException e) {
                throw new BadConfigurationException(
                        "configurator class not found (" + i + ")");
            } catch (InstantiationException e) {
                throw new BadConfigurationException(
                        "instantiation (" + i + ")");
            } catch (IllegalAccessException e) {
                throw new BadConfigurationException("illegal access (" + i
                        + ")");
            }
        }
        return configurators;
    }
}
