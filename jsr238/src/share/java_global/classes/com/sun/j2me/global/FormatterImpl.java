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

import java.util.Calendar;
import java.util.Hashtable;
import javax.microedition.global.Formatter;
import javax.microedition.global.ResourceManager;
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
 */
public class FormatterImpl implements CommonFormatter {

    /**
     * Current FormatterImpl locale code.
     */
    private String locale;

    /**
     * Formatting symbols for locale.
     */
    private NumberFormatSymbols symbols = null;

    /**
     * Formatting symbols cache.
     */
    private static Hashtable symbols_cache = new Hashtable();

    /**
     * Resource ID for the Number Format Symbol Resource
     */
    private static int NUMBER_FORMAT_SYMBOL_RESOURCE_ID = 0x7ffffffe;

    /**
     * Resource ID for the DateTime Format Symbol Resource
     */
    private static int DATETIME_FORMAT_SYMBOL_RESOURCE_ID = 0x7ffffffd;
    
    
    /**
     * Constructs a formatter implementation for the specified locale.
     *
     * @param  locale  desired FormatterImpl locale.
     * @throws UnsupportedLocaleException  The exception
     *      is thrown when locale isn't supported.
     */
    public FormatterImpl(String locale) throws UnsupportedLocaleException {
        int locale_index = LocaleHelpers.indexInSupportedLocales(locale,
                Formatter.getSupportedLocales());
        if (locale_index == -1) {
            throw new UnsupportedLocaleException("Locale \""
                                                 + locale +
                                                 "\" unsupported.");
        }

        this.locale = locale;
    }


    /**
     * Creates and returns a set of symbols for number formatting.
     *
     * @return  <code>NumberFormatSymbols</code> instance for current locale
     */
    private NumberFormatSymbols getNumberFormatSymbols() {
    	if (symbols == null) {
    		symbols = (NumberFormatSymbols)symbols_cache.get(locale);
    		if (symbols == null) {
		        try {
		            ResourceManager rm = ResourceManager.getManager(
		                    ResourceManager.DEVICE, locale);
		            symbols = (NumberFormatSymbols)rm.getResource(
		                    	NUMBER_FORMAT_SYMBOL_RESOURCE_ID);
		        } catch (NullPointerException npe_ignore) {
		            /* intentionally ignored */
		            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		                Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
		                    "Base name or locale is null");
		            }
		        } catch (IllegalArgumentException iae_ignore) {
		            /* intentionally ignored */
		            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		                Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
		                    "Locale identifier is not valid" + locale);
		            }
		        } catch (ResourceException re_ignore) {
		            /* intentionally ignored */
		            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		                Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
		                    "No resources are found or the resource file is invalid");
		            }
		        } catch (UnsupportedLocaleException ule_ignore) {
		            /* intentionally ignored */
		            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
		                Logging.report(Logging.ERROR, LogChannels.LC_JSR238,
		                    "Locale is not listed in the meta-information file"
		                    + locale);
		            }
		        }
	    		if (symbols == null) {
			        if (Logging.REPORT_LEVEL <= Logging.WARNING) {
			            Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
			                "Using neutral locale symbols for locale: " 
			                + locale);
			        }
			        symbols = new NumberFormatSymbols();
	    		}
    		}
    		symbols_cache.put(locale, symbols);
    	}
    	return symbols;
    }

    // JAVADOC COMMENT ELIDED
    public String formatDateTime(Calendar dateTime, int style) {
        try {
            ResourceManager rm = ResourceManager.getManager(
                    ResourceManager.DEVICE, locale);
            DateFormatSymbols symbols = (DateFormatSymbols)rm.getResource(
                    DATETIME_FORMAT_SYMBOL_RESOURCE_ID);
            DateTimeFormat dtf = new DateTimeFormat(style, symbols);
            NumberFormat nf = new NumberFormat(NumberFormat.INTEGERSTYLE,
                                               getNumberFormatSymbols());
            return dtf.format(dateTime, nf);
        } catch (NullPointerException npe_ignore) {
            /* intentionally ignored */
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                    "Base name or locale is null");
            }
        } catch (IllegalArgumentException iae_ignore) {
            /* intentionally ignored */
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                    "locale identifier is not valid" + locale);
            }
        } catch (ResourceException re_ignore) {
            /* intentionally ignored */
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                    "No resources are found or the resource file is invalid");
            }
        } catch (UnsupportedLocaleException ule_ignore) {
            /* intentionally ignored */
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                    "Locale is not listed in the meta-information file"
                    + locale);
            }
        }
        return "";
    }
    
    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number) {
        NumberFormat nf = new NumberFormat(NumberFormat.CURRENCYSTYLE,
                                           getNumberFormatSymbols());
        if (Double.isInfinite(number) ||
            Double.isNaN(number)) {
            String oldSymbol = nf.setCurrencySymbol("");
            String formatted = nf.format(number);
            nf.setCurrencySymbol(oldSymbol);
            return formatted;
        } else {
            return nf.format(number);
        }
    }


    // JAVADOC COMMENT ELIDED
    public String formatCurrency(double number, String currencyCode) {
        NumberFormat nf = new NumberFormat(NumberFormat.CURRENCYSTYLE,
                                           getNumberFormatSymbols());
        String symbol = "";
        
        if (!(Double.isInfinite(number) ||
                Double.isNaN(number))) {
	        symbol = nf.getCurrencySymbolForCode(currencyCode);
	        if (symbol == null) {
	            symbol = currencyCode;
	        }
        }
        
        String oldSymbol = nf.setCurrencySymbol(symbol);
        String formatted = nf.format(number);
        nf.setCurrencySymbol(oldSymbol);

        return formatted;
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number) {
        NumberFormat nf = new NumberFormat(NumberFormat.NUMBERSTYLE,
                                           getNumberFormatSymbols());
        return nf.format(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatNumber(double number, int decimals) {
        NumberFormat nf = new NumberFormat(NumberFormat.NUMBERSTYLE,
                                           getNumberFormatSymbols());
        nf.setMinimumFractionDigits(decimals);
        nf.setMaximumFractionDigits(decimals);
        return nf.format(number);
    }


    /**
     * Formats an integer using locale-specific rules. The result may include
     * grouping separators.
     *
     * @param  number  the number to format
     * @return         formatted integer number
     */
    public String formatNumber(long number) {
        NumberFormat nf = new NumberFormat(NumberFormat.INTEGERSTYLE,
                                           getNumberFormatSymbols());
        return nf.format(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatPercentage(long number) {
        NumberFormat nf = new NumberFormat(NumberFormat.PERCENTSTYLE,
                                           getNumberFormatSymbols());
        nf.setMultiplier(1);
        return nf.format(number);
    }


    // JAVADOC COMMENT ELIDED
    public String formatPercentage(float number, int decimals) {
        NumberFormat nf = new NumberFormat(NumberFormat.PERCENTSTYLE,
                                           getNumberFormatSymbols());
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(decimals);
        return nf.format(number);
    }
}
