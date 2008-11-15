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

import java.util.Calendar;
import java.util.TimeZone;
import javax.microedition.global.Formatter;

/**
 *  <code>DateTimeFormat</code> is a concrete class for formatting dates in a
 *  locale-sensitive manner. It allows for formatting (date -> text). <p>
 *
 *  <code>DateTimeFormat</code> allows you to start by choosing one of
 *  predefined patterns for date-time formatting passing appropriate style to
 *  factory method <code>getInstance(style, locale)</code>.
 *  <table>
 *
 *    <tr>
 *
 *      <td>
 *        a
 *      </td>
 *
 *      <td>
 *        am/pm
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        H
 *      </td>
 *
 *      <td>
 *        hour in day 0-23
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        h
 *      </td>
 *
 *      <td>
 *        hour in am/pm 0-11
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        K
 *      </td>
 *
 *      <td>
 *        hour in day 1-24
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        k
 *      </td>
 *
 *      <td>
 *        hour in am/pm 1-12
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        m
 *      </td>
 *
 *      <td>
 *        minute 0-59
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        s
 *      </td>
 *
 *      <td>
 *        second 0-59
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        d
 *      </td>
 *
 *      <td>
 *        day in month (number)
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        dd
 *      </td>
 *
 *      <td>
 *        day in month (number 2 digits)
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        EE
 *      </td>
 *
 *      <td>
 *        day in week short "Mon"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        EEEE
 *      </td>
 *
 *      <td>
 *        day in week long "Monday"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        M
 *      </td>
 *
 *      <td>
 *        month in year (number)
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        MM
 *      </td>
 *
 *      <td>
 *        month in year (number 2 digits)
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        MMM
 *      </td>
 *
 *      <td>
 *        month in year short "Oct"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        MMMM
 *      </td>
 *
 *      <td>
 *        month in year long "October"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        yy
 *      </td>
 *
 *      <td>
 *        year short "05"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        yyyy
 *      </td>
 *
 *      <td>
 *        year long "2005"
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        G
 *      </td>
 *
 *      <td>
 *        era
 *      </td>
 *
 *    </tr>
 *
 *    <tr>
 *
 *      <td>
 *        z
 *      </td>
 *
 *      <td>
 *        timezone
 *      </td>
 *
 *    </tr>
 *
 *  </table>
 *
 *
 */
public class DateTimeFormat {

    /**
     * Holds initialized instance of <code>DateFormatSymbols</code> which
     * encapsulate locale-dependent information like names of days in week,
     * months etc.
     */
    private DateFormatSymbols symbols;

    /**
     * Style of date/time formatting.
     *
     * @see    Formatter#TIME_LONG
     * @see    Formatter#TIME_SHORT
     * @see    Formatter#DATE_LONG
     * @see    Formatter#DATE_SHORT
     * @see    Formatter#DATETIME_LONG
     * @see    Formatter#DATETIME_SHORT
     */
    private int style;

    /**
     * Creates new <code>DateTimeFormat</code> object. It is assumed that
     * <code>style</code> contains correct value and <code>symbols</code>
     * refers to properly initialized <code>DateFormatSymbols</code> object.
     *
     * @param  style    predefined date/time style
     * @param  symbols  object encapsulating localized DateTime symbols
     */
    public DateTimeFormat(int style, DateFormatSymbols symbols) {
        this.style = style;
        this.symbols = symbols;
    }


    /**
     * Formats date/time with the current <code>style</code>.
     *
     * @param  calendar  date/time to format
     * @param  nf        integer instance of <code>NumberFormat</code> to
     *                   perform formatting of integer date and time values
     * @return           formatted string
     */
    protected String format(Calendar calendar, NumberFormat nf) {
        StringBuffer appendTo = new StringBuffer();
        StringBuffer pattern = new StringBuffer(symbols.patterns[style]);
        nf.setGroupingUsed(false);
        char c;
        int value;
        int digits;

        if (style == Formatter.DATETIME_SHORT) {
            //  date first paremeter, time second
            pattern = new StringBuffer(MessageFormat.format(
                    symbols.patterns[style],
                    new String[] {symbols.patterns[Formatter.DATE_SHORT],
                    symbols.patterns[Formatter.TIME_SHORT]}));

        } else if (style == Formatter.DATETIME_LONG) {
            pattern = new StringBuffer(MessageFormat.format(
                    symbols.patterns[style],
                    new String[] {symbols.patterns[Formatter.DATE_LONG],
                    symbols.patterns[Formatter.TIME_LONG]}));
        }
        for (int i = 0; i < pattern.length(); i++) {
            c = pattern.charAt(i);
            switch (c) {
                case 'a':
                    // AM or PM symbol
                    int ampm = calendar.get(Calendar.AM_PM);
                    appendTo.append(symbols.ampms[ampm == Calendar.AM ? 0 : 1]);
                    break;
                case 'H':
                    // Hours in 24-hour mode 0 - based
                    value = calendar.get(Calendar.HOUR_OF_DAY);
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'H') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 'h':
                    // Hours in 12-hour mode, 1 - based
                    value = calendar.get(Calendar.HOUR);
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'h') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 'K':
                    // Hours in 12-hour mode 0 - based
                    value = calendar.get(Calendar.HOUR_OF_DAY);
                    value = value>12 ? value % 12 : value;  // noon is 12 midnight is 0
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'K') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 'k':
                    // Hours in 24-hour mode 1 based
                    value = calendar.get(Calendar.HOUR_OF_DAY);
                    value = (value == 0) ? 24 : value;
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'k') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 'm':
                    // Minutes
                    value = calendar.get(Calendar.MINUTE);
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'm') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 's':
                    // Seconds
                    value = calendar.get(Calendar.SECOND);
                    digits = ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 's') ? 2 : 1;
                    nf.setMinimumIntegerDigits(digits);
                    appendTo.append(nf.format(value));
                    i += digits - 1;
                    break;
                case 'E':
                    // Long or short weekday name
                    if ((i + 3) < pattern.length() &&
                            pattern.charAt(i + 1) == 'E' &&
                            pattern.charAt(i + 2) == 'E' &&
                            pattern.charAt(i + 3) == 'E') {
                        // long day of week name
                        value = calendar.get(Calendar.DAY_OF_WEEK);
                        appendTo.append(symbols.weekDays[value]);
                        i += 3;
                    } else if ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'E') {
                        // short day of week name
                        value = calendar.get(Calendar.DAY_OF_WEEK);
                        appendTo.append(symbols.shortWeekDays[value]);
                        i += 1;
                    }
                    break;
                case 'd':
                    // Numeric day of month
                    if ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'd') {
                        // numeric day 2 digits
                        value = calendar.get(Calendar.DAY_OF_MONTH);
                        nf.setMinimumIntegerDigits(2);
                        appendTo.append(nf.format(value));
                        i += 1;
                    } else {
                        // numeric day 1 digit
                        value = calendar.get(Calendar.DAY_OF_MONTH);
                        nf.setMinimumIntegerDigits(1);
                        appendTo.append(nf.format(value));
                    }
                    break;
                case 'M':
                    // Long or short month name or numeric month
                    if ((i + 3) < pattern.length() &&
                            pattern.charAt(i + 1) == 'M' &&
                            pattern.charAt(i + 2) == 'M' &&
                            pattern.charAt(i + 3) == 'M') {
                        // long month name
                        value = calendar.get(Calendar.MONTH);
                        appendTo.append(symbols.months[value]);
                        i += 3;
                    } else if ((i + 2) < pattern.length() &&
                            pattern.charAt(i + 1) == 'M' &&
                            pattern.charAt(i + 2) == 'M') {
                        // short month name
                        value = calendar.get(Calendar.MONTH);
                        appendTo.append(symbols.shortMonths[value]);
                        i += 2;
                    } else if ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'M') {
                        value = calendar.get(Calendar.MONTH) + 1;
                        nf.setMinimumIntegerDigits(2);
                        appendTo.append(nf.format(value));
                        i += 1;
                    } else {
                        value = calendar.get(Calendar.MONTH) + 1;
                        nf.setMinimumIntegerDigits(1);
                        appendTo.append(nf.format(value));
                    }
                    break;
                case 'y':
                    // Long or short year
                    value = calendar.get(Calendar.YEAR);
                    if ((i + 3) < pattern.length() &&
                            pattern.charAt(i + 1) == 'y' &&
                            pattern.charAt(i + 2) == 'y' &&
                            pattern.charAt(i + 3) == 'y') {
                        // long year
                        nf.setMinimumIntegerDigits(4);
                        appendTo.append(nf.format(value));
                        i += 3;
                    } else if ((i + 1) < pattern.length() &&
                            pattern.charAt(i + 1) == 'y') {
                        // short year
                        nf.setMinimumIntegerDigits(2);
                        String y = nf.format(value);
                        appendTo.append(y.substring(y.length() - 2,
                                y.length()));
                        i += 1;
                    }
                    break;
                /*
                 *  case 'G': // not in midp
                 *  value = calendar.get(Calendar.ERA);
                 *  appendTo.append(symbols.eras[value]);
                 *  break;
                 */
                case 'z':
                    // Time zone
                    TimeZone tzone = calendar.getTimeZone();
                    if (tzone == null) {
                        break;
                    }
                    int offset = tzone.getRawOffset();
                    if (offset == 0) {
                        appendTo.append('Z');
                    } else {
                        offset /= 3600000;
                        if (offset >= 0) {
                            appendTo.append('+');
                        } else {
                            appendTo.append('-');
                            offset = -offset;
                        }
                        if (offset < 10) {
                            appendTo.append('0');
                        }
                        appendTo.append(offset);
                        appendTo.append(":00");
                    }
                    break;
                default:
                    appendTo.append(c);

            }
        }
        // for
        return appendTo.toString();
    }
}


