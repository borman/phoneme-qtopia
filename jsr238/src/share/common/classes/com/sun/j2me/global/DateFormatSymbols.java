/*
 *   
 *
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
package com.sun.j2me.global;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.global.ResourceException;

/**
 *  <code>DateFormatSymbols</code> is class for encapsulating localizable
 *  date/time formatting data, such as the names of the months, the names of the
 *  days of the week, and the time zone data. <code>DateTimeFormat</code> uses
 *  <code>DateFormatSymbols</code> to encapsulate this information. <p>
 *
 *  DateFormatSymbols is typicaly obtained from <code>
 *  javax.microedition.ResourceManager</code> 
 *  respectively from <code>DevResourceManager.getDateFormatSymbols()</code>
 *  from resource file for given locale. <p>
 *
 *  Typically you shouldn't use <code>DateFormatSymbols</code> directly. Rather,
 *  you are encouraged to create a date/time formatter with the 
 *  <code>DateTimeFormat</code> class's factory methods: 
 *  <code>getInstance(int style, String locale)</code>
 *  These methods automatically create a <code>DateFormatSymbols</code> for the
 *  formatter so that you don't have to. All fields are public intentionaly.
 *
 */
public class DateFormatSymbols implements SerializableResource {

    /**
     *  Create new DateFormatSymbol uninitialized.
     */
    public DateFormatSymbols() { }


    /**
     * The method clones the resource.
     *
     * @return copy of the resource or <code>null<code>
     * if clonning wasn't possible 
     */
    public java.lang.Object clone() {
        DateFormatSymbols newDfs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            write(baos);
            baos.close();
            byte[] buffer = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            newDfs = new DateFormatSymbols();
            newDfs.read(bais);
        } catch (IOException ioe) {
            // cannot clone resource
        }
        return newDfs;
    }
    
    /**
     *  eras.
     */
    public String[] eras = new String[2];

    /**
     *  month names.
     */
    public String[] months = new String[13];

    /**
     *  short month names.
     */
    public String[] shortMonths = new String[13];

    /**
     *  day in week names.
     */
    public String[] weekDays = new String[7];

    /**
     *  short day in week names.
     */
    public String[] shortWeekDays = new String[7];

    /**
     *  ampms.
     */
    public String[] ampms = new String[2];

    /**
     *  localized patterns 6 possible styles as they are defined in {@link
     *  DateTimeFormat}.
     */
    public String[] patterns = new String[6];

    /**
     *  locale of this symbols.
     */
    public String locale = new String();


    /**
     * Read DateFormatSymbols object from input stream.
     *
     * @param  in    input stream
     * @throws  java.io.IOException error reading resource
     * @throws  javax.microedition.global.ResourceException  error creating
     * resource
     */
    public void read(java.io.InputStream in) throws IOException,
                                                    ResourceException {
        DataInputStream dis = new DataInputStream(in);
        readStrings(eras, dis);
        readStrings(months, dis);
        readStrings(shortMonths, dis);
        readStrings(weekDays, dis);
        readStrings(shortWeekDays, dis);
        readStrings(ampms, dis);
        readStrings(patterns, dis);
        locale = dis.readUTF();
    }


    /**
     *  Serialize DateFormatSymbols object into output stream.
     *
     * @param   out output stream
     * @throws  java.io.IOException is thrown if write fails
     * @throws  javax.microedition.global.ResourceException  if resource 
     *          can't be written
     */
    public void write(java.io.OutputStream out) throws IOException,
                                                       ResourceException {
        DataOutputStream dous = new DataOutputStream(out);
        writeStrings(eras, dous);
        writeStrings(months, dous);
        writeStrings(shortMonths, dous);
        writeStrings(weekDays, dous);
        writeStrings(shortWeekDays, dous);
        writeStrings(ampms, dous);
        writeStrings(patterns, dous);
        dous.writeUTF(locale);
        dous.flush();
    }
    
    /**
     * Write array helper. Writes string array.
     *
     * @param array strings to write
     * @param dous output stream
     * @throws IOException exception when write failed
     */
    protected void writeStrings(String[] array, DataOutputStream dous) 
                        throws IOException {
        for (int i = 0; i < array.length; i++) {
            dous.writeUTF(array[i]);
        }
    }

    /**
     * Read strings helper. Reads string array.
     *
     * @param array string array to read to
     * @param dis  input stream
     * @throws IOException exception when read failed
     */
    protected void readStrings(String[] array, DataInputStream dis) 
                        throws IOException {
        for (int i = 0; i < array.length; i++) {
            array[i] = dis.readUTF();
        }
    }
}

