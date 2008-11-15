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

package com.sun.javame.sensor.helper;

public class SensorUrlGenerator {

    private static final String[] CONTEXT_TYPES = new String[] { "ambient",
            "device", "user" };

    private static final String[] QUANTITY_TYPES = new String[] {
            "absorbed_dose", "absorbed_dose_rate", "acceleration", "activity",
            "alcohol", "altitude", "amount_of_substance",
            "amount_of_substance_concentration", "angle",
            "angular_acceleration", "angular_velocity", "area",
            "battery_charge", "blood_glucose_level", "blood_oxygen_level",
            "blood_pressure", "body_fat_percentage", "capacitance",
            "catalytic_activity", "catalytic_concentration", "character",
            "current_density", "direction", "direction_of_motion",
            "dose_equivalent", "duration", "dynamic_viscosity",
            "electric_charge", "electric_charge_density",
            "electric_conductance", "electric_currency", "electric_current",
            "electric_field_strength", "electric_flux_density",
            "electric_potential_difference", "electric_resistance", "energy",
            "energy_density", "entropy", "exposure", "fingerprint",
            "flip_state", "force", "frequency", "gesture", "heart_rate",
            "heat_capacity", "heat_flux_density", "humidity", "illuminance",
            "inductance", "irradiance", "kerma", "length", "location",
            "luminance", "luminous_flux", "luminous_intensity",
            "magnetic_field_strength", "magnetic_flux",
            "magnetic_flux_density", "mass", "mass_density", "molar_energy",
            "molar_entropy", "molar_heat_capacity", "moment_of_force",
            "percentage", "permeability", "permittivity", "plane_angle",
            "power", "pressure", "proximity", "quantity_of_heat", "radiance",
            "radiant_flux", "radiant_intensity", "RR_interval", "solid_angle",
            "sound_intensity", "specific_energy", "specific_entropy",
            "specific_heat_capacity", "specific_volume", "step_count",
            "stress", "surface_tension", "temperature", "thermal_conductivity",
            "time", "wave_number", "velocimeter", "velocity", "wind_speed",
            "volume", "work", "com.some_company.attitude" };

    private static final SensorUrlGenerator INSTANCE = new SensorUrlGenerator();

    private final Random rnd;    
    private StringBuffer txt;

    public static String generateSensorUrl() {
        return INSTANCE.generate();
    }

    public SensorUrlGenerator() {
        rnd = new Random();
    }

    public synchronized String generate() {
        txt = new StringBuffer();
        sensor_url();
        return txt.toString();
    }

    private void sensor_url() {
        // <sensor_url> ::= "sensor:"<sensor_id>
        txt.append("sensor:");
        sensor_id();
    }

    private void sensor_id() {
        // <sensor_id> ::= <quantity>[<contextType>][<model>][<location>]
        quantity();
        if (rnd.nextBoolean()) contextType();
        if (rnd.nextBoolean()) model();
        if (rnd.nextBoolean()) location();
    }

    private void quantity() {
        // <quantity> ::= ("temperature"|"acceleration"|...)
        txt.append(rnd.oneOf(QUANTITY_TYPES));
    }

    private void contextType() {
        // <contextType> ::=
        //      <separator>"contextType="("ambient"|"device"|"user")
        separator();
        txt.append("contextType=");
        txt.append(rnd.oneOf(CONTEXT_TYPES));
    }

    private void model() {
        // <model> ::= <separator>"model="<model_id>
        separator();
        txt.append("model=");
        model_id();
    }

    private void model_id() {
        // <model_id> ::= <alphanum>*
        int cnt = rnd.nextInt(1000);
        for (int i = 0; i < cnt; i++) alphanum();
    }

    private void location() {
        // <location> ::= <separator>"location="<location_id>
        separator();
        txt.append("location=");
        location_id();
    }

    private void location_id() {
        // <location_id> ::= <alphanum>*
        int cnt = rnd.nextInt(1000);
        for (int i = 0; i < cnt; i++) alphanum();
    }

    private void separator() {
        // <separator>   ::= ";"
        txt.append(";");
    }

    private void alphanum() {
        if (rnd.nextBoolean()) { alpha(); } else { num(); }
    }

    private void alpha() {
        if (rnd.nextBoolean()) {
            txt.append((char) ('a' + rnd.nextInt('z' - 'a')));
        } else {
            txt.append((char) ('A' + rnd.nextInt('Z' - 'A')));
        }
    }

    private void num() {
        txt.append(rnd.nextInt(10));
    }
}

class Random {

    private final java.util.Random rnd = new java.util.Random();

    public boolean nextBoolean() {
        return rnd.nextInt(2) == 0;
    }

    public String oneOf(String[] strings) {
        if (strings.length < 1) {
            throw new IllegalArgumentException();
        }
        return strings[rnd.nextInt(strings.length)];
    }

    public double nextDouble() {
        return rnd.nextDouble();
    }

    public float nextFloat() {
        return rnd.nextFloat();
    }

    public int nextInt() {
        return rnd.nextInt();
    }

    public int nextInt(int arg0) {
        return rnd.nextInt(arg0);
    }

    public long nextLong() {
        return rnd.nextLong();
    }

    public void setSeed(long arg0) {
        rnd.setSeed(arg0);
    }
}
