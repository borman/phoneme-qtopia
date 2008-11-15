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

package javax.microedition.sensor.control;

import javax.microedition.sensor.ChannelInfo;
import javax.microedition.sensor.MeasurementRange;

public interface MeasurementRangeControl extends Control {

    /**
     * Gets a measurement range of the given channel.
     *
     * @param channelInfo - the channel whose measurement range is queried
     * @return the current measurement range of the given channel
     * @throws IllegalArgumentException - if the data type of the channel is TYPE_OBJECT
     * @throws NullPointerException - if the given channel is null
     */
    public MeasurementRange getMeasurementRange(ChannelInfo channelInfo);

    /**
     * Sets the new measurement range.
     *
     * @param channelInfo - the channel measurement range is to be set
     * @param measurementRange - the measurement range to be set
     * @throws IllegalArgumentException - if the given range is none of the ranges
     * returned by the method ChannelInfo.getMeasurementRanges(), or if the
     * data type of the channel is TYPE_OBJECT.
     * @throws IllegalStateException - if the measurement range cannot be set
     * @throws NullPointerException - if the given channel or measurement range is null
     */
    public void setMeasurementRange(ChannelInfo channelInfo,
                         MeasurementRange measurementRange);
}
