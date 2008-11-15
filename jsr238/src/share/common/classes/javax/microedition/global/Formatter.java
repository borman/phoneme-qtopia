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
package javax.microedition.global;

import com.sun.j2me.global.FormatAbstractionLayer;
import java.util.Calendar;

import com.sun.j2me.global.CommonFormatter;
import com.sun.j2me.global.MessageFormat;
import com.sun.j2me.global.LocaleHelpers;

// JAVADOC COMMENT ELIDED
public final class Formatter {

    /**
     *  Constant for long time style.
     */
    public static final int TIME_LONG = 3;

    /**
     *  Constant for short time style.
     */
    public static final int TIME_SHORT = 2;
    /**
     *  Constant long date style.
     */
    public static final int DATE_LONG = 1;

    /**
     *  Constant short date style.
     */
    public static final int DATE_SHORT = 0;

    /**
     *  Constant long datetime style.
     */
    public static final int DATETIME_LONG = 5;

    /**
     *  Constant for short datetime style.
     */
    public static final int DATETIME_SHORT = 4;

    /**
     *  Current Formatter locale.
     */
    private String locale;

    /**
     *  FormatAbstractionLayer subclass instance for obtaining of Formatter
     *  realization.
     */
    private static FormatAbstractionLayer formatAbstractionLayer =
            FormatAbstractionLayer.getInstance();

    /**
     *  Current Formatter realization instance.
     */
    private CommonFormatter formatterImpl;

    // JAVADOC COMMENT ELIDED
    public Formatter() throws UnsupportedLocaleException {
        this(System.getProperty("microedition.locale"));
    }


    /**
     *  Constructs a formatter for the specified locale.
     *
     * @param  locale  desired Formatter locale.
     * @throws  UnsupportedLocaleException  The exception
     * is thrown when locale isn't supported.
     * @throws  IllegalArgumentException  The exception
     * is thrown when locale has invalid format, i.e.
     * the locale format is not as the following:
     * null|({a..z}{a..z}[{-|_}{A..Z}{A..Z}[{-|_}<any symbols>]])
     */
    public Formatter(String locale)
            throws UnsupportedLocaleException, IllegalArgumentException {
        if (!LocaleHelpers.isValidLocale(locale) && !("".equals(locale))) {
            throw new IllegalArgumentException("Invalid locale format");
        }

        locale = LocaleHelpers.normalizeLocale(locale);

        if ("".equals(locale)) {
            this.locale = null;
        } else {
            this.locale = locale;
        }

        if (this.locale == null) {
            formatterImpl = FormatAbstractionLayer.getNeutralFormatter();
        } else {
            formatterImpl = formatAbstractionLayer.getFormatter(locale);
        }
    }


    // JAVADOC COMMENT ELIDED
    public static String formatMessage(String template, String[] params) {
    	if (template == null || params == null) {
    		throw new NullPointerException("Template or parameter array is null.");
    	}	    	
    	return MessageFormat.format(template, params);
    }


    // JAVADOC COMMENT ELIDED
    public String formatDateTime(Calendar dateTime, int style) {
        if (dateTime == null) {
            throw new NullPointerException("Calendar is null.");
        }
        if (style < DATE_SHORT || style > DATETIME_LONG) {
            throw new IllegalArgumentException("Illegal style value");
        }

        return formatterImpl.formatDateTime(dateTime, style);
    }


    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number) {
        return formatterImpl.formatCurrency(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number, String currencyCode)
            throws IllegalArgumentException {

        if (currencyCode.length() != 3 ||
            currencyCode.charAt(0) < 'A' || currencyCode.charAt(0) > 'Z' ||
            currencyCode.charAt(1) < 'A' || currencyCode.charAt(1) > 'Z' ||
            currencyCode.charAt(2) < 'A' || currencyCode.charAt(2) > 'Z') {
            throw new IllegalArgumentException("Illegal currency code");
        }

        return formatterImpl.formatCurrency(number, currencyCode);
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number) {
        return formatterImpl.formatNumber(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number, int decimals) throws
            IllegalArgumentException {

        if (decimals < 1 || decimals > 15) {
            throw new IllegalArgumentException("Illegal number of decimals");
        }

        return formatterImpl.formatNumber(number, decimals);
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(long number) {
        return formatterImpl.formatNumber(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatPercentage(long number) {
        return formatterImpl.formatPercentage(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatPercentage(float number, int decimals) throws
            IllegalArgumentException {

        if (decimals < 1 || decimals > 15) {
            throw new IllegalArgumentException("Illegal number of decimals");
        }

        return formatterImpl.formatPercentage(number, decimals);
    }


    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedLocales() {
        return formatAbstractionLayer.getSupportedLocales();
    }


    // JAVADOC COMMENT ELIDED
    public String getLocale() {
        return locale;
    }
}
