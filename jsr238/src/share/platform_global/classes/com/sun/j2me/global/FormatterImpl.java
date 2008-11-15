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
package com.sun.j2me.global;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import javax.microedition.global.Formatter;
import javax.microedition.global.ResourceException;
import javax.microedition.global.UnsupportedLocaleException;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This class actually realizes most of the methods of
 * {@link javax.microedition.global.Formatter}. Specifically, these are:
 * <ul>
 *   <li> {@link #formatDateTime(Calendar, int)}
 *   <li> {@link #formatCurrency(double)}
 *   <li> {@link #formatCurrency(double, String)}
 *   <li> {@link #formatNumber(double)}
 *   <li> {@link #formatNumber(double, int)}
 *   <li> {@link #formatNumber(long)}
 *   <li> {@link #formatPercentage(long number)}
 *   <li> {@link #formatPercentage(float, int)}
 * </ul>
 * <p>
 * This realization of <code>Formatter</code> is intended to be based on
 * some native functions for formatting support. Regarding this,
 * <code>FormatterImpl</code> uses locale index that points to its locale
 * instead of working with explicit locale code.
 */
public class FormatterImpl implements CommonFormatter {

    /**
     *  Current FormatterImpl locale index in the list of supported locales.
     */
    private int locale_index;

    /**
     * Constructs a <code>Formatter</code> implementation for the specified
     * locale.
     *
     * @param  locale  desired <code>FormatterImpl</code> locale
     * @throws UnsupportedLocaleException  The exception
     *      is thrown when locale isn't supported.
     */
    public FormatterImpl(String locale) throws UnsupportedLocaleException {
		locale_index = FormatAbstractionLayerImpl.getFormatLocaleIndex(locale);
		if (locale_index < 0) {
			throw new UnsupportedLocaleException("Locale \""
												 + locale +
												 "\" unsupported.");
		}
    }


    // JAVADOC COMMENT ELIDED
    public String formatDateTime(Calendar dateTime, int style) {
            return formatDateTime0(locale_index,
                                   dateTime.get(Calendar.YEAR), 
                                   dateTime.get(Calendar.MONTH) + 1, 
                                   dateTime.get(Calendar.DAY_OF_WEEK),
                                   dateTime.get(Calendar.DAY_OF_MONTH),
                                   dateTime.get(Calendar.HOUR_OF_DAY),
                                   dateTime.get(Calendar.MINUTE),
                                   dateTime.get(Calendar.SECOND),
                                   style);
    }

    private native String formatDateTime0(int locale,
                                          int year, int month,
                                          int dayOfWeek, int day,
                                          int hour, int minute, int secs,
                                          int style);


    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number) {
        return formatCurrency(number, null);
    }

    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number, String currencyCode) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return new Double(number).toString();
        }
        return formatCurrency0(locale_index, number, currencyCode);
    }

    private native String formatCurrency0(int locale, double number, String currency);


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return new Double(number).toString();
        }
        return formatNumber0(locale_index, number, -1);
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number, int decimals) {
		if (decimals<=0 || decimals>15){
			throw new IllegalArgumentException("Wrong decimals parameter");
		}
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return new Double(number).toString();
        }
        return formatNumber0(locale_index, number, decimals);
    }


    /**
     * Formats an integer using locale-specific rules. The result may include
     * grouping separators.
     *
     * @param  number  the number to format
     * @return         formatted integer number
     */
    public String formatNumber(long number) {
        return formatNumber1(locale_index, number);
    }

    private native String formatNumber0(int locale, double number, int decimals);

	private native String formatNumber1(int locale, long number);

    // JAVADOC COMMENT ELIDED
    public String formatPercentage(long number) {
        return formatPercentage1(locale_index, number);
    }

    // JAVADOC COMMENT ELIDED
    public String formatPercentage(float number, int decimals) {
		if (decimals<=0 || decimals>15){
			throw new IllegalArgumentException("Wrong decimals parameter");
		}
        if (Float.isNaN(number) || Float.isInfinite(number)) {
            return new Float(number).toString();
        }
        return formatPercentage0(locale_index, number, decimals);
    }

    private native String formatPercentage0(int locale_index, float number, int decimals);

	private native String formatPercentage1(int locale_index, long number);
}
