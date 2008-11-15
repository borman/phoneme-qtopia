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

public interface SampleRateControl extends Control {

    /**
     * Gets a sample rate.
     *
     * @return the sample rate
     */
    public float getSampleRate();

    /**
     * Gets sample rates.
     *
     * @return the all set of sample rates of sensor
     */
    public float[] getSampleRates();

    /**
     * Sets a sample rate.
     *
     * @param sampleRate the sample rate
     * @throws IllegalArgumentException - if the sample rate is not a valid value
     * @throws IllegalStateException - if the sample rate cannot be set at the moment
     */
    public void setSampleRate(float sampleRate);
}
