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

package javax.microedition.global;
import com.sun.midp.i3test.*;
import java.util.*;

/**
 * i3test for Formatter
 */
public class TestFormatter extends TestCase {
    /**
     * Format Currency with code
     */
    public void testCurrencyWithCode() {
        Formatter f = new Formatter();
        String result = f.formatCurrency(12.34, "USD");
        assertEquals("$12.34", result);
        result = f.formatCurrency(-12.34, "USD");
        assertEquals("($12.34)", result);
    }
    /**
     * Format Currency with nonsupported code
     */
    public void testCurrencyWithNonSupportedCode() {
        Formatter f = new Formatter();
        String result = f.formatCurrency(12.34, "LIT");
        assertEquals("LIT12.34", result);
        result = f.formatCurrency(-12.34, "LIT");
        assertEquals("(LIT12.34)", result);
    }

    /**
     * Test unsupported locale
     */
    public void testUnsupportedLocale() {
        try {
            Formatter f = new Formatter("uk-UK");

        } catch (UnsupportedLocaleException ue) {
            assertTrue(true);
            return;
        }
        fail("Exception wasn't thrown");
    }

    /**
     * Format supported Currency
     */
    public void testFormatSupportedCurrency() {
        try {
            Formatter f = new Formatter("sk-SK");
            String result = f.formatCurrency(12.50);
            assertEquals("12.50 Sk", result);
            Formatter f1 = new Formatter("en-US");
            result = f1.formatCurrency(12.50);
            assertEquals("$12.50", result);
            result = f1.formatCurrency(-12.50);
            assertEquals("($12.50)", result);
            Formatter f2 = new Formatter("en");
            result = f2.formatCurrency(12.50);
            assertEquals("US$12.50", result);
            result = f2.formatCurrency(-12.50);
            assertEquals("-US$12.50", result);
        } catch (UnsupportedLocaleException ue) {
            ue.printStackTrace();
            fail();
        }
    }

    /**
     * Format Rounding Currency Down
     */
    public void testRoundingCurrencyDown() {
        try {
            Formatter f = new Formatter("sk-SK");
            String result = f.formatCurrency(12.502);
            assertEquals("12.50 Sk", result);
        } catch (UnsupportedLocaleException ue) {
            ue.printStackTrace();
            fail();
        }
    }

    /**
     * Format Rounding Currency Up
     */
    public void testRoundingCurrencyUp() {
        try {
            Formatter f = new Formatter("sk-SK");
            String result = f.formatCurrency(12.597);
            assertEquals("12.60 Sk", result);
        } catch (UnsupportedLocaleException ue) {
            ue.printStackTrace();
            fail();
        }
    }

    /**
     * Format Numbers
     */
    public void testNumber() {
        Formatter f = new Formatter("en-US");
        String result = f.formatNumber(1234567890);
        assertEquals("1,234,567,890", result);
        result = f.formatNumber(12345.67890);
        assertEquals("12,345.68", result);
        result = f.formatNumber(12345.67890, 4);
        assertEquals("12,345.6789", result);
        Formatter f1 = new Formatter("zh-CN");
        result = f1.formatNumber(1234567890);
        assertEquals("1,234,567,890", result);
        result = f1.formatNumber(12345.67890);
        assertEquals("12,345.68", result);
        result = f1.formatNumber(12345.67890, 4);
        assertEquals("12,345.6789", result);
        Formatter f2 = new Formatter();
        result = f2.formatNumber(1234567890);
        assertEquals("1,234,567,890", result);
        result = f2.formatNumber(12345.67890);
        assertEquals("12,345.68", result);
        result = f2.formatNumber(12345.67890, 4);
        assertEquals("12,345.6789", result);
    }

    /**
     * Format Percents
     */
    public void testPercent() {
        Formatter f = new Formatter("en-US");
        float fl_value = 0.123456f;
        String result = f.formatPercentage(123);
        assertEquals("123%", result);
        result = f.formatPercentage(fl_value, 3);
        assertEquals("12.346%", result);
        result = f.formatPercentage(1234567890);
        assertEquals("1,234,567,890%", result);
        Formatter f1 = new Formatter("zh-CN");
        result = f1.formatPercentage(1234567890);
        assertEquals("1,234,567,890%", result);
        result = f1.formatPercentage(fl_value, 4);
        assertEquals("12.3456%", result);
        result = f1.formatPercentage(-fl_value, 4);
        assertEquals("-12.3456%", result);
    }

    /**
     * Format Date & Time
     */
    public void testDateTime() {
        long constTime = 1128522923951l;
        String result;
        String month_en = "October";
        String month_sk = "okt\u00f3ber";
        String month_cs = "\u0159\u00edjna";
        Formatter f = new Formatter("en-US");
        Formatter f1 = new Formatter("sk-SK");
        Formatter f2 = new Formatter("cs-CZ");
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();

        c.setTimeZone(TimeZone.getTimeZone("GMT-04:00"));

        date.setTime(constTime);
        c.setTime(date);

        result = f.formatDateTime(c, Formatter.DATE_SHORT);
        assertEquals("10/5/05", result);
        result = f1.formatDateTime(c, Formatter.DATE_SHORT);
        assertEquals("5.10.2005", result);
        result = f2.formatDateTime(c, Formatter.DATE_SHORT);
        assertEquals("5.10.05", result);

        result = f.formatDateTime(c, Formatter.DATE_LONG);
        assertEquals(month_en + " 5, 2005", result);
        result = f1.formatDateTime(c, Formatter.DATE_LONG);
        assertEquals("5. " + month_sk + " 2005", result);
        result = f2.formatDateTime(c, Formatter.DATE_LONG);
        assertEquals("5. " + month_cs + " 2005", result);

        result = f.formatDateTime(c, Formatter.TIME_SHORT);
        assertEquals("9:35 AM", result);
        result = f1.formatDateTime(c, Formatter.TIME_SHORT);
        assertEquals("9:35", result);
        result = f2.formatDateTime(c, Formatter.TIME_SHORT);
        assertEquals("9:35", result);

        result = f.formatDateTime(c, Formatter.TIME_LONG);
        assertEquals("9:35:23 AM -04:00", result);
        result = f1.formatDateTime(c, Formatter.TIME_LONG);
        assertEquals("9:35:23 -04:00", result);
        result = f2.formatDateTime(c, Formatter.TIME_LONG);
        assertEquals("9:35:23 -04:00", result);

        result = f.formatDateTime(c, Formatter.DATETIME_SHORT);
        assertEquals("9:35 AM 10/5/05", result);
        result = f1.formatDateTime(c, Formatter.DATETIME_SHORT);
        assertEquals("9:35 5.10.2005", result);
        result = f2.formatDateTime(c, Formatter.DATETIME_SHORT);
        assertEquals("9:35 5.10.05", result);

        result = f.formatDateTime(c, Formatter.DATETIME_LONG);
        assertEquals("9:35:23 AM -04:00 " + month_en + " 5, 2005",
                        result);
        result = f1.formatDateTime(c, Formatter.DATETIME_LONG);
        assertEquals("9:35:23 -04:00 5. " + month_sk + " 2005",
                        result);
        result = f2.formatDateTime(c, Formatter.DATETIME_LONG);
        assertEquals("9:35:23 -04:00 5. " + month_cs + " 2005",
                        result);
    }

    /**
     * Format Message
     */
    public void testMessageFormat() {
        Formatter f = new Formatter("en-US");
        String result;

        /* Format message with just one parameter */
        result = f.formatMessage("This is {0} parameter",
                                 new String[] {"first"});
        assertEquals("This is first parameter", result);

        /* Format message with more than one parameter */
        result = f.formatMessage("{0} {1} {2} {3} {4} {5} {6} {7}",
                                 new String[] {"first", "second", "third",
                                               "fourth", "fifth", "sixth",
                                               "seventh", "eighth"});
        assertEquals("first second third fourth fifth sixth seventh eighth",
                     result);

        /* Format message containing escaped parenthesis */
        result = f.formatMessage("This is {{ {0} parenthesis",
                                 new String[] {"left"});
        assertEquals("This is { left parenthesis", result);

        /* Format message containing right escaped parenthesis */
        result = f.formatMessage("This is } {0} parenthesis",
                                 new String[] {"right"});
        assertEquals("This is } right parenthesis", result);

        /* Test when placeholders doesn't match number of parameters */
        try {
            result = f.formatMessage("{0} {1} {2}", new String[] {"first"});
            fail("IllegalArgumentException wasn't thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test when there are more parameters than placeholders */
        result = null;
        try {
            result = f.formatMessage("{0}",
                                     new String[] {"first", "second", "third"});
        } catch (IllegalArgumentException e) {
            fail();
        }
        assertEquals("first", result);

        /* Placeholders can repeat */
        result = f.formatMessage("{0} {1} {0} {0}",
                                 new String[] {"first", "second"});
        assertEquals("first second first first", result);

        /* Test leading zero example: <code>{05}</code> */
        result = f.formatMessage("{00} {01}",
                                 new String[] {"first", "second"});
        assertEquals("first second", result);

        /*
         * If a placeholder doesn't contain number, then
         * IllegalArgumentException is thrown
         */
        try {
            result = f.formatMessage("{ciao}", new String[] {"first"});
            fail("IllegalArgumentException wasn't thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Check if NPE is thrown when parameters == null */
        String[] params = null;
        try {
            result = f.formatMessage("{0} {1}", params);
            fail("NullPointerException wasn't thrown.");
        } catch (NullPointerException npe) {
            assertTrue(true);
        }

        /* Check if NPE is thrown when template == null */
        String template = null;
        try {
            result = f.formatMessage(template, new String[] {"first"});
            fail("NullPointerException wasn't thrown.");
        } catch (NullPointerException npe) {
            assertTrue(true);
        }
    }

    /**
     * creates an array containing all the supported Formatters and 'null'
     * for locale-neutral formatting
     * @return Formatter[]  - created array
     */
    private Formatter[] getFormatters() {

        String[] suppLocales = Formatter.getSupportedLocales();
        Formatter[] ret = new Formatter[suppLocales.length + 1];
        int i;

                try {
            for (i = 0; i < suppLocales.length; i++) {
                ret[i] = new Formatter(suppLocales[i]);
            }

            ret[i++] = new Formatter(null);
        } catch (Exception e) {
            return null;
        }
        return ret;
    }


    /**
     * Format NAN, infinity value for Currency
     */
    public void testNaN() {

        double[] val = {Double.NaN, Double.POSITIVE_INFINITY,
                        Double.NEGATIVE_INFINITY };
        String[] currencyCode = { "XXX", "YYY", "ZZZ" };

        Formatter[] formatter = getFormatters();

        for (int i = 0; i < formatter.length; i++) {
                for (int j = 0; j < val.length; j++) {
                        String result = formatter[i].formatCurrency(val[j],
                                                             currencyCode[j]);
                        if (result.indexOf(currencyCode[j]) != -1) {
                            assertTrue(false);
                        } else {
                            assertTrue(true);
                        }
                }
        }
    }

    /**
     * fill suite with test methods
     */
    public void runTests() {

        declare("testMessageFormat");
        testMessageFormat();

        declare("testDateTime");
        testDateTime();

        declare("testPercent");
        testPercent();
        declare("testNumber");
        testNumber();

        declare("testCurrencyWithCode");
        testCurrencyWithCode();

        declare("testUnsupportedLocale");
        testUnsupportedLocale();
        declare("testFormatSupportedCurrency");
        testFormatSupportedCurrency();

        declare("testCurrencyWithNonSupportedCode");
        testCurrencyWithNonSupportedCode();

        declare("testRoundingCurrencyDown");
        testRoundingCurrencyDown();

        declare("testRoundingCurrencyUp");
        testRoundingCurrencyUp();

        declare("testNaN");
        testNaN();
    }
}




