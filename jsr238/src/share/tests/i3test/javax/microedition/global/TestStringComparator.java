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
 * I3 test for StringComparator.
 */
public class TestStringComparator extends TestCase {

    private StringComparator sc;
    private String message;

    private String[] illegal = {"Cs", "cs-", "cs/CZ",
                                "cs-cz", "cs-CZ-", "cs-CZ:utf8"};

    private static final String[] words = {
        "role",
        "ro\u0083le",
        "Role",
        "r\u00f4le",
        "roles",
        "rule"
    };
    private static final int[][] cmp_level1 = {
        { 0,  0,  0,  0, -1, -1},
        { 0,  0,  0,  0, -1, -1},
        { 0,  0,  0,  0, -1, -1},
        { 0,  0,  0,  0, -1, -1},
        { 1,  1,  1,  1,  0, -1},
        { 1,  1,  1,  1,  1,  0}
    };
    private static final int[][] cmp_level2 = {
        { 0,  0,  0, -1, -1, -1},
        { 0,  0,  0, -1, -1, -1},
        { 0,  0,  0, -1, -1, -1},
        { 1,  1,  1,  0, -1, -1},
        { 1,  1,  1,  1,  0, -1},
        { 1,  1,  1,  1,  1,  0}
    };
    private static final int[][] cmp_level3 = {
        { 0,  0, -1, -1, -1, -1},
        { 0,  0, -1, -1, -1, -1},
        { 1,  1,  0, -1, -1, -1},
        { 1,  1,  1,  0, -1, -1},
        { 1,  1,  1,  1,  0, -1},
        { 1,  1,  1,  1,  1,  0}
    };
    private static final int[][] cmp_identical = {
        { 0, -1, -1, -1, -1, -1},
        { 1,  0, -1, -1, -1, -1},
        { 1,  1,  0, -1, -1, -1},
        { 1,  1,  1,  0, -1, -1},
        { 1,  1,  1,  1,  0, -1},
        { 1,  1,  1,  1,  1,  0}
    };


    /* Test cases of valid StringComparator creation */
    public void testValidConstruction() {
        /* Create string comparator for default locale */
        sc = null;
        sc = new StringComparator();
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertEquals(System.getProperty("microedition.locale"), sc.getLocale());

        /* Create string comparator for generic collation */
        sc = null;
        sc = new StringComparator(null);
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertNull(sc.getLocale());

        /* Create string comparator for generic collation */
        sc = null;
        sc = new StringComparator("");
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertNull(sc.getLocale());

        /* Create string comparator for "zh" locale */
        sc = null;
        sc = new StringComparator("zh");
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertEquals("zh", sc.getLocale());

        /* Create string comparator for "sk-SK" locale */
        sc = null;
        sc = new StringComparator("sk-SK");
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertEquals("sk-SK", sc.getLocale());

        /* Create string comparator with comparison level LEVEL1 */
        sc = null;
        sc = new StringComparator("sk-SK", StringComparator.LEVEL1);
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL1, sc.getLevel());
        assertEquals("sk-SK", sc.getLocale());

        /* Create string comparator with comparison level LEVEL2 */
        sc = null;
        sc = new StringComparator("sk-SK", StringComparator.LEVEL2);
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL2, sc.getLevel());
        assertEquals("sk-SK", sc.getLocale());

        /* Create string comparator with comparison level LEVEL3 */
        sc = null;
        sc = new StringComparator("sk-SK", StringComparator.LEVEL3);
        assertNotNull(sc);
        assertEquals(StringComparator.LEVEL3, sc.getLevel());
        assertEquals("sk-SK", sc.getLocale());

        /* Create string comparator with comparison level IDENTICAL */
        sc = null;
        sc = new StringComparator("sk-SK", StringComparator.IDENTICAL);
        assertNotNull(sc);
        assertEquals(StringComparator.IDENTICAL, sc.getLevel());
        assertEquals("sk-SK", sc.getLocale());
    }

    /* Test cases of invalid StringComparator creation */
    public void testInvalidConstruction() {
        /* Test string comparator creation with illegal (wrong format) locale */
        for (int i = 0; i < illegal.length; i++) {
            try {
                sc = new StringComparator(illegal[i]);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }

        /* Test string comparator creation with illegal level of comparison */
        try {
            sc = new StringComparator("en-US", 10);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test string comparator creation with unsupported locale */
        try {
            sc = new StringComparator("fi-FI");
            fail("UnsupportedException expected");
        } catch (UnsupportedLocaleException e) {
            assertTrue(true);
        }
    }

    /* Compare 2 entries of words[] array, prepare failure message */
    private int compareWords(int i, int j) {
        message = "compareWords(" + i + ", " + j + ") is wrong";
        return sc.compare(words[i], words[j]);
    }

    /* Test 2 entries of words[] array for equality, prepare failure message */
    private boolean equalWords(int i, int j) {
        message = "equalWords(" + i + ", " + j + ") is wrong";
        return sc.equals(words[i], words[j]);
    }

    /* Signum of integer argument */
    private int sign(int num) {
        if (num > 0) {
            return 1;
        }
        if (num < 0) {
            return -1;
        }
        return 0;
    }

    /* Test LEVEL1 generic collation: base character differences only */
    public void testLevel1() {
        sc = null;
        sc = new StringComparator(null);

        for (int i = 0; i < cmp_level1.length; i++) {
            for (int j = 0; j < cmp_level1[i].length; j++) {
                int res = compareWords(i, j);
                assertEquals(message, cmp_level1[i][j], sign(res));
            }
        }
        for (int i = 0; i < cmp_level1.length; i++) {
            for (int j = 0; j < cmp_level1[i].length; j++) {
                boolean eq = equalWords(i, j);
                assertTrue(message, (cmp_level1[i][j] == 0) == eq);
            }
        }
    }

    /* Test LEVEL2 generic collation: take character accents into account */
    public void testLevel2() {
        sc = null;
        sc = new StringComparator(null, StringComparator.LEVEL2);

        for (int i = 0; i < cmp_level2.length; i++) {
            for (int j = 0; j < cmp_level2[i].length; j++) {
                int res = compareWords(i, j);
                assertEquals(message, cmp_level2[i][j], sign(res));
            }
        }
        for (int i = 0; i < cmp_level2.length; i++) {
            for (int j = 0; j < cmp_level2[i].length; j++) {
                boolean eq = equalWords(i, j);
                assertTrue(message, (cmp_level2[i][j] == 0) == eq);
            }
        }
    }

    /* Test LEVEL3 generic collation: take character case into account */
    public void testLevel3() {
        sc = null;
        sc = new StringComparator(null, StringComparator.LEVEL3);

        for (int i = 0; i < cmp_level3.length; i++) {
            for (int j = 0; j < cmp_level3[i].length; j++) {
                int res = compareWords(i, j);
                assertEquals(message, cmp_level3[i][j], sign(res));
            }
        }
        for (int i = 0; i < cmp_level3.length; i++) {
            for (int j = 0; j < cmp_level3[i].length; j++) {
                boolean eq = equalWords(i, j);
                assertTrue(message, (cmp_level3[i][j] == 0) == eq);
            }
        }
    }

    /* Test IDENTICAL generic collation: take all differences into account */
    public void testIdentical() {
        sc = null;
        sc = new StringComparator(null, StringComparator.IDENTICAL);

        for (int i = 0; i < cmp_identical.length; i++) {
            for (int j = 0; j < cmp_identical[i].length; j++) {
                int res = compareWords(i, j);
                assertEquals(message, cmp_identical[i][j], sign(res));
            }
        }
        for (int i = 0; i < cmp_identical.length; i++) {
            for (int j = 0; j < cmp_identical[i].length; j++) {
                boolean eq = equalWords(i, j);
                assertTrue(message, (cmp_identical[i][j] == 0) == eq);
            }
        }
    }

    /* Test illegal comparison attempts */
    public void testIllegalComparison() {
        sc = null;
        sc = new StringComparator(null);

        /* Test comparison of null strings */
        try {
            sc.compare(null, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        try {
            sc.compare(null, words[0]);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        try {
            sc.compare(words[0], null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test equality testing of null strings */
        try {
            sc.equals(null, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        try {
            sc.equals(null, words[0]);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        try {
            sc.equals(words[0], null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    /**
     * fill suite with test methods
     */
    public void runTests() {
        declare("testValidConstruction");
        testValidConstruction();
        declare("testInvalidConstruction");
        testInvalidConstruction();
        declare("testLevel1");
        testLevel1();
        declare("testLevel2");
        testLevel2();
        declare("testLevel3");
        testLevel3();
        declare("testIdentical");
        testIdentical();
        declare("testIllegalComparison");
        testIllegalComparison();
    }
}
