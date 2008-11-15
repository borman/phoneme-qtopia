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

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * <code>NumberFormat</code> has features designed to make it possible to
 * format numbers in any locale. It also supports different kinds of numbers,
 * including integers (123), fixed-point numbers (123.4), percentages (12%),
 * and currency amounts ($123). All of these can be localized. <p>
 *
 * To obtain a <code>NumberFormat</code> for a specific locale call one of
 * <code>NumberFormat</code>'s factory methods, such as:
 * <ul>
 *    <li> <code>getPercentageInstance(locale)</code>
 *    <li> <code>getIntegerInstance(locale)</code>
 *    <li> <code>getCurrencyInstance(locale)</code>
 *    <li> <code>getDecimalInstance(local)</code>
 *  </ul>
 *  <p>
 *
 * Usage: <pre>
 * NumberFormat f = NumberFormat.getCurrencyInstance(loc);
 * StringBuffer sb = f.format(new Double(123.45), new StringBuffer());
 * </pre> <p>
 *
 * Or eventualy it's possible to change number of decimals displayed <pre>
 * NumberFormat f = NumberFormat.getCurrencyInstance(loc);
 * f.setMaximumFractionDigits(2);
 * StringBuffer sb = f.format(new Double(123.45559), new StringBuffer());
 * </pre>
 *
 */
public class NumberFormat {

    /**
     * Class name.
     */
    private static final String classname = NumberFormat.class.getName();
    /**
     * Upper limit on integer digits for a Java double.
     */
    private final static int DOUBLE_INTEGER_DIGITS = 309;
    /**
     * Upper limit on fraction digits for a Java double.
     */
    private final static int DOUBLE_FRACTION_DIGITS = 340;
    /**
     * Non localized percent sign.
     */
    public final static char NONLOCALIZED_PERCENT_SIGN = '\u0025';

    /**
     * Unicode INFINITY character.
     */
    public final static char UNICODE_INFINITY = '\u221e';

    /**
     * Styles of formatting j2se compatible.
     */

    /**
     * General number.
     */
    public final static int NUMBERSTYLE = 0;
    /**
     * Currency style.
     */
    public final static int CURRENCYSTYLE = 1;
    /**
     * Percent style.
     */
    public final static int PERCENTSTYLE = 2;
    /**
     * Integer style.
     */
    public final static int INTEGERSTYLE = 3;


    /**
     * Holds initialized instance of DecimalFormatSymbols which encapsulate
     * locale dependent informations like currency symbol, percent symbol etc.
     */
    private NumberFormatSymbols symbols;

    /**
     * Is this <code>NumberFormat</code> instance for currency formatting?
     */
    private boolean isCurrencyFormat = false;

    /**
     * Is this <code>NumberFormat</code> instance of percentage formatting?
     */
    private boolean isPercentageFormat = false;

    /**
     * Digit list does most of formatting work.
     */
    private DigitList digitList = new DigitList();

    /**
     * Style of <code>NumberFormat</code>. Possible styles are:
     * <ul>
     *   <li> {@link #NUMBERSTYLE}
     *   <li> {@link #CURRENCYSTYLE}
     *   <li> {@link #PERCENTSTYLE}
     *   <li> {@link #INTEGERSTYLE}
     * </ul>
     */
    private int style = NUMBERSTYLE;


    /**
     * Create <code>NumberFormat</code> with given number pattern and set of
     * locale numeric symbols.
     *
     * @param  style    the style of <code>NumberFormat</code>
     *      <ul>
     *        <li> {@link #NUMBERSTYLE}
     *        <li> {@link #CURRENCYSTYLE}
     *        <li> {@link #PERCENTSTYLE}
     *        <li> {@link #INTEGERSTYLE}
     *      </ul>
     *
     * @param  symbols  NumberFormatSymbols identifying numbers formatting for
     *      given locale.
     */
    public NumberFormat(int style, NumberFormatSymbols symbols) {
        this.style = style;
        this.symbols = symbols;
        isCurrencyFormat = (style == CURRENCYSTYLE);
        isPercentageFormat = (style == PERCENTSTYLE);
        applySymbols();
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + ": " +
                           "NumberFormat created\n" +
                           "style is " + style + "\n" +
                           "symbols is " + symbols);
        }
    }


    /**
     * Set maximal number of decimals to be displayed.
     *
     * @param  count  number of decimals to display
     * @see #getMaximumFractionDigits
     */
    public void setMaximumFractionDigits(int count) {
        if (symbols != null &&
                count <= DOUBLE_FRACTION_DIGITS &&
            count >= 0 &&
                style != INTEGERSTYLE) {
            symbols.maximumFractionDigits[style] = count;
            if (symbols.minimumFractionDigits[style] < count) {
                symbols.minimumFractionDigits[style] = count;
            }
        }
    }


    /**
     * How many decimals is used to display number.
     *
     * @return    maximum number of decimals or <code>-1</code> if non-localized
     *      formatting is used.
     * @see #setMaximumFractionDigits
     */
    public int getMaximumFractionDigits() {
        return (symbols != null) ?
                symbols.maximumFractionDigits[style] :
                -1;
    }


    /**
     * Sets minimum number of decimals to be displayed.
     *
     * @param  count  minimum number of decimals to display
     * @see #getMinimumFractionDigits
     */
    public void setMinimumFractionDigits(int count) {
        if (symbols != null &&
                count >= 0 &&
                style != INTEGERSTYLE) {
            symbols.minimumFractionDigits[style] = count;
            if (count > symbols.maximumFractionDigits[style]) {
                symbols.maximumFractionDigits[style] = count;
            }
        }
    }


    /**
     * Sets multiplier to different value than symbols for this locale do.
     *
     * @param  multiplier  new value for multiplier;
     * @see #getMultiplier
     */
    public void setMultiplier(int multiplier) {
        if (symbols != null) {
            symbols.multiplier[style] = multiplier;
        }
    }


    /**
     * Gets actual multilier used by this locale for this number style. Usually
     *  (1 or 100).
     *
     * @return    the multiplier
     * @see #setMultiplier
     */
    public int getMultiplier() {
        return (symbols != null) ? symbols.multiplier[style] : 1;
    }


    /**
     * Sets if grouping is used.
     *
     * @param  used <code>true</code> if grouping should be used
     */
    public void setGroupingUsed(boolean used) {
        if (symbols != null) {
            symbols.groupingUsed = used;
        }
    }


    /**
     * Get minimum of decimals used to display number.
     *
     * @return    minimum number of decimals or <code>-1</code> if non-localized
     *      formatting is used.
     * @see #setMinimumFractionDigits
     */
    public int getMinimumFractionDigits() {
        return (symbols != null) ?
                symbols.minimumFractionDigits[style] :
                -1;
    }


    /**
     * Sets minimum integer digits.
     *
     * @param  count the count of digits
     * @see #getMinimumIntegerDigits
     */
    public void setMinimumIntegerDigits(int count) {
        if (symbols != null && count > 0) {
            symbols.minimumIntegerDigits[style] = count;
        }
    }


    /**
     * Gets minimum integer digits.
     *
     * @return number minimum of integer digits
     * @see #setMinimumIntegerDigits
     */
    public int getMinimumIntegerDigits() {
        return (symbols != null) ?
                symbols.minimumIntegerDigits[style] :
                -1;
    }

    /**
     * Sets currency symbol.
     *
     * @param symbol the currency symbol
     * @return previously used currency symbol
     */
    public String setCurrencySymbol(String symbol) {
    	String oldsymbol = null;
        if (isCurrencyFormat) {
            if (symbols != null) {
            	oldsymbol = symbols.currencySymbol;
                if (!symbol.equals(symbols.currencySymbol)) {
                    symbols.currencySymbol = symbol;
                    symbols.suffixes[style] =
                            replSubStr(symbols.suffixes[style], oldsymbol,
                                       symbol);
                    symbols.prefixes[style] =
                            replSubStr(symbols.prefixes[style], oldsymbol,
                                       symbol);
                    symbols.negativeSuffix[style] =
                            replSubStr(symbols.negativeSuffix[style], oldsymbol,
                                       symbol);
                    symbols.negativePrefix[style] =
                            replSubStr(symbols.negativePrefix[style], oldsymbol,
                                       symbol);
                    symbols.positiveSuffix[style] =
                            replSubStr(symbols.positiveSuffix[style], oldsymbol,
                                       symbol);
                    symbols.positivePrefix[style] =
                            replSubStr(symbols.positivePrefix[style], oldsymbol,
                                       symbol);
                }
            }
        }
        return oldsymbol;
    }
    /**
     * Replaces substring in the string onto new string.
     *
     * @param str the changed string
     * @param oldVal the replaced substring
     * @param newVal the replacing string
     * @return changed string
     */
    private String replSubStr(String str, String oldVal, String newVal) {
        String res = str;
        if (str.length() > 0) {
            int pos = str.indexOf(oldVal);
            if (pos >= 0) {
                res = str.substring(0, pos);
                res = res.concat(newVal);
                res = res.concat(str.substring(pos + oldVal.length()));
                return res;
            }
        }
        return res;
    }

    /**
     * Lookup table of supported currencies for appropriate symbol.
     * 
     * @param currencyCode code ISO 4217. 
     * @return currency symbol or <code>null</code> if none was found.
     */
    public String getCurrencySymbolForCode(String currencyCode) {
    	if (symbols != null && symbols.currencies != null){
	        for (int i = 0; i < symbols.currencies.length; i++) {
	            if (symbols.currencies[i].length>0 && symbols.currencies[i][0].equals(currencyCode))
	                if (symbols.currencies[i].length>1){ 
	                	return  symbols.currencies[i][1];
	                } else {
	                	return null;
	                }
	        }
    	}
        return null;
    }
        
    /**
     * Check if some attributes of <code>NumberFormatSymbols</code> are
     * undefined and replace them with default values.
     */
    private void applySymbols() {
        if (symbols != null) {
            if (symbols.maximumIntegerDigits[style] == -1) {
                symbols.maximumIntegerDigits[style] = DOUBLE_INTEGER_DIGITS;
            }
            if (symbols.maximumFractionDigits[style] == -1) {
                symbols.maximumFractionDigits[style] = DOUBLE_FRACTION_DIGITS;
            }
        }
    }


    /**
     * Method formats long.
     *
     * @param  value  long number to format
     * @return        formatted long number
     */
    public String format(long value) {
        return format(new Long(value));
    }


    /**
     * Method formats double.
     *
     * @param  value  double value to format
     * @return        formatted double number
     */
    public String format(double value) {
        if (symbols != null) {
            if (Double.isNaN(value)) {
                return symbols.NaN;
            }
            if (Double.isInfinite(value)) {
                String prefix = (value > 0.0) ? "" : symbols.negativePrefix[style];
                String suffix = (value > 0.0) ? "" : symbols.negativeSuffix[style];
                return prefix + symbols.infinity + suffix;
            }
        } else {
            if (Double.isNaN(value)) {
                return "NaN";
            }
            if (Double.isInfinite(value)) {
                String prefix = (value > 0.0) ? "" : "-";
                return prefix + UNICODE_INFINITY;
            }
        }
        return format(new Double(value));
    }    

    /**
     * Method formats integer.
     *
     * @param  value  integer value to format
     * @return        formatted integer number
     */
    public String format(int value) {
        return format(new Long(value));
    }


    /**
     * Method formats float.
     *
     * @param  value  float value to format
     * @return        formatted float number
     */
    public String format(float value) {
        return format((double)value);
    }


    /**
     * Does formatting. Result is appended to parameter
     * <code>StringBuffer appendTo</code>.
     *
     * @param  o         object to format
     * @return           buffer with appended formatted text
     */
    protected String format(Object o) {
        StringBuffer appendTo = new StringBuffer();
        if (o == null) {
            return "";
        }
        if (symbols != null) {
            if (o instanceof Double) {
                format(((Double) o).doubleValue(), appendTo);
            }
            if (o instanceof Long) {
                format(((Long) o).longValue(), appendTo);
            }
        } else {
            if (isPercentageFormat) {
                if (o instanceof Double) {
                    appendTo.append(Double.toString(
                                    ((Double)o).doubleValue() * 100.0));
                } else if (o instanceof Long) {
                    long value = ((Long) o).longValue();
                    appendTo.append(Long.toString(value));
                    if (value != 0) appendTo.append("00");
                }
                appendTo.append(NONLOCALIZED_PERCENT_SIGN);
            } else {
                return o.toString();
            }
        }
        return appendTo.toString();
    }


    /**
     * Formats double number.
     *
     * @param  number  the double number to formatt
     * @param  result  formatted number
     * @return         buffer with appended formatted number
     */
    private StringBuffer format(double number, StringBuffer result) {
        if (Double.isNaN(number)) {
            result.append(symbols.NaN);
            return result;
        }
        boolean isNegative = (number < 0.0) ||
                             (number == 0.0 && 1 / number < 0.0);
        if (isNegative) {
            number = -number;
        }

        if (symbols.multiplier[style] != 1) {
            number *= symbols.multiplier[style];
        }

        if (Double.isInfinite(number)) {
            if (isNegative) {
                result.append(symbols.negativePrefix[style]);
            } else {
                result.append(symbols.positivePrefix[style]);
            }
            result.append(symbols.infinity);

            if (isNegative) {
                result.append(symbols.negativeSuffix[style]);
            } else {
                result.append(symbols.positiveSuffix[style]);
            }
            return result;
        }

        digitList.set(number, symbols.maximumFractionDigits[style]);
        result = subformat(result, isNegative, false);

        return result;
    }



    /**
     * Format a long to produce a string.
     *
     * @param  number  The long to format
     * @param  result  where the text is to be appended
     * @return         The formatted number
     */
    private StringBuffer format(long number, StringBuffer result) {
        boolean isNegative = (number < 0);
        if (isNegative) {
            number = -number;
        }

        if (symbols.multiplier[style] != 1 &&
                symbols.multiplier[style] != 0) {
            boolean useDouble = false;

            if (number < 0) {
                //  This can only happen if number == Long.MIN_VALUE

                long cutoff = Long.MIN_VALUE / symbols.multiplier[style];
                useDouble = (number < cutoff);
            } else {
                long cutoff = Long.MAX_VALUE / symbols.multiplier[style];
                useDouble = (number > cutoff);
            }

            if (useDouble) {
                double dnumber = (double) (isNegative ? -number : number);
                return format(dnumber, result);
            }
        }

        number *= symbols.multiplier[style];
        synchronized (digitList) {
            digitList.set(number, 0);

            return subformat(result, isNegative, true);
        }
    }


    /**
     * Formats content of DigitList.
     *
     * @param  result      buffer to append formatted number to
     * @param  isNegative  <code>true</code> if number is negative
     * @param  isInteger   <code>true</code> if integer number will be formatted
     * @return             buffer with appended formatted number
     */
    private StringBuffer subformat(StringBuffer result,
            boolean isNegative, boolean isInteger) {

        char zero = symbols.zeroDigit;
        int zeroDelta = zero - '0';
        //  '0' is the DigitList representation of zero
        char grouping = symbols.groupingSeparator;
        char decimal = isCurrencyFormat ?
                symbols.monetarySeparator :
                symbols.decimalSeparator;

        if (digitList.isZero()) {
            digitList.decimalAt = 0;
            //  Normalize
        }

        int fieldStart = result.length();

        if (isNegative) {
            result.append(symbols.negativePrefix[style]);
        } else {
            result.append(symbols.positivePrefix[style]);
        }

        String prefix = symbols.prefixes[style];
        result.append(prefix);

        int count = symbols.minimumIntegerDigits[style];
        int digitIndex = 0;
        //  Index into digitList.fDigits[]
        if (digitList.decimalAt > 0 && count < digitList.decimalAt) {
            count = digitList.decimalAt;
        }

        if (count > symbols.maximumIntegerDigits[style]) {
            count = symbols.maximumIntegerDigits[style];
            digitIndex = digitList.decimalAt - count;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                           classname + " :" +
                           "grouping used " + symbols.groupingUsed + "\n" +
                           "grouping separator \"" + grouping + "\"\n" +
                           "decimal separator \"" + decimal + "\"\n" +
                           "digit count " + count);
        }

        int sizeBeforeIntegerPart = result.length();
        for (int i = count - 1; i >= 0; --i) {
            if (i < digitList.decimalAt && digitIndex < digitList.count) {
                //  Output a real digit
                result.append((char) (digitList.digits[digitIndex++] +
                                      zeroDelta));
            } else {
                //  Output a leading zero
                result.append(zero);
            }

            //  Output grouping separator if necessary.  Don't output a
            //  grouping separator if i==0 though; that's at the end of
            //  the integer part.
            if (symbols.groupingUsed && i > 0 &&
                    (symbols.groupingCount != 0) &&
                    (i % symbols.groupingCount == 0)) {
                int gStart = result.length();
                result.append(grouping);
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR238,
                                   classname + ": " +
                                   "add grouping at " + (digitIndex-1));
                }
            }
        }// for

        boolean fractionPresent = (symbols.minimumFractionDigits[style] > 0) ||
                (!isInteger && digitIndex < digitList.count);

        if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
            result.append(zero);
        }
        //  Output the decimal separator if we always do so.
        int sStart = result.length();
        if (symbols.decimalSeparatorAlwaysShown || fractionPresent) {
            result.append(decimal);
        }

        for (int i = 0; i < symbols.maximumFractionDigits[style]; ++i) {
            if (i >= symbols.minimumFractionDigits[style] &&
                    (isInteger || digitIndex >= digitList.count)) {
                break;
            }

            if (-1 - i > (digitList.decimalAt - 1)) {
                result.append(zero);
                continue;
            }

            if (!isInteger && digitIndex < digitList.count) {
                result.append((char) (digitList.digits[digitIndex++] +
                                      zeroDelta));
            } else {
                result.append(zero);
            }
        }

        String suffix = symbols.suffixes[style];
        result.append(suffix);

        if (isNegative) {
            result.append(symbols.negativeSuffix[style]);
        } else {
            result.append(symbols.positiveSuffix[style]);
        }

        return result;
    }
}
