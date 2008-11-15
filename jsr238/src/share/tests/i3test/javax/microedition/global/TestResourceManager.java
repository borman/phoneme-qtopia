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
public class TestResourceManager extends TestCase {

    private ResourceManager rm;
    private String message;

    private String[] locs_match_en     = {"fi-FI", "en", "cs"};
    private String[] locs_match_empty  = {"de-DE", "fi-FI", "", "zh"};
    private String[] locs_with_null    = {"fi-FI", "en", null, "cs"};
    private String[] locs_with_illegal = {"de-DE", "illegal", "ru-RU", "it"};
    private String[] locs_unsupported  = {"de-DE", "fi", "ru-RU", "it"};
    private String[] illegal           = {"Cs", "cs-", "cs/CZ", "cs-cz",
                                          "cs-CZ-", "cs-CZ:utf8"};

    private String[] res_cs = {"Konec", "Dal\u0161\u00ed", "Zp\u011bt"};
    private String[] res_en = {"Exit", "Next", "Back"};
    private String[] res_ja = {"\u7d42\u4e86", "\u6b21\u3078 ", "\u623b\u308b"};
    private String[] res_sk = {"Koniec", "\u010ealej", "Sp\u00e4\u0165"};
    private String[] res_common = res_en;

    private final int RES_BASE = 0x65;
    private final int FORMAT_SYMBOLS = 0x7ffffffd;


    public void testValidConstruction() {
        /* Retrieve JSR 238 version information */
        assertEquals("1.0", System.getProperty("microedition.global.version"));

        /* Create device resource manager for default locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE);
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals(System.getProperty("microedition.locale"), rm.getLocale());

        /* Create device resource manager for common resources */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("", rm.getLocale());

        /* Create device resource manager for an existing locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en-US");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("en-US", rm.getLocale());

        /* Create device resource manager for an unsupported locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "fi-FI");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("", rm.getLocale());

        /*
         * Create device resource manager for an unsupported locale with
         * variant
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "de-DE-utf8");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("", rm.getLocale());

        /* Create device resource manager for a locale with underscore */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "sk_SK");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("sk-SK", rm.getLocale());

        /*
         * Create device resource manager for a locale with variant and
         * underscore
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "he-IL_utf8");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("he-IL", rm.getLocale());

        /*
         * Create device resource manager for a locale with variant and
         * underscores
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "cs_CZ_utf8");
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("cs-CZ", rm.getLocale());

        /*
         * Device resource manager for a list of locales,
         * "en" is the first supported locale in the list
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, locs_match_en);
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("en", rm.getLocale());

        /*
         * Device resource manager for a list of locales,
         * "" (common) is the first supported locale in the list
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                        locs_match_empty);
        assertNotNull(rm);
        assertEquals(ResourceManager.DEVICE, rm.getBaseName());
        assertEquals("", rm.getLocale());
    }

    public void testInvalidConstruction() {
        /* Test resource manager creation with baseName == null */
        try {
            rm = ResourceManager.getManager(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with locale == null */
        try {
            rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                            (String)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with baseName == null */
        try {
            rm = ResourceManager.getManager(null, "en-US");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with illegal (wrong format) locale */
        for (int i = 0; i < illegal.length; i++) {
            try {
                rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                                illegal[i]);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }

        /* Test resource manager creation with locales == null */
        try {
            rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                            (String[])null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with baseName == null */
        try {
            rm = ResourceManager.getManager(null, locs_match_en);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with locales containing null */
        try {
            rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                            locs_with_null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with empty list of locales */
        try {
            rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                            new String[0]);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test resource manager creation with list containing illegal locale */
        for (int i = 0; i < illegal.length; i++) {
            try {
                locs_with_illegal[1] = illegal[i];
                rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                                locs_with_illegal);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }

        /*
         * Test resource manager creation with a list containing only
         * unsupported locales
         */
        try {
            rm = ResourceManager.getManager(ResourceManager.DEVICE,
                                            locs_unsupported);
            fail("UnsupportedLocaleException expected");
        } catch (UnsupportedLocaleException e) {
            assertTrue(true);
        }
    }

    /* Helper method for array comparison (element-by-element) */
    private boolean equalArrays(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public void testValidDeviceResources() {
        /*
         * Retrieve device resources for "cs-CZ" locale ("cs" resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "cs-CZ");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_cs[i], rm.getString(RES_BASE + i));
            assertEquals(res_cs[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /* Retrieve device resources for "cs" locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "cs");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_cs[i], rm.getString(RES_BASE + i));
            assertEquals(res_cs[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "en-US" locale ("en" resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en-US");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_en[i], rm.getString(RES_BASE + i));
            assertEquals(res_en[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /* Retrieve device resources for "en" locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_en[i], rm.getString(RES_BASE + i));
            assertEquals(res_en[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "he-IL" locale (common resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "he-IL");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_common[i], rm.getString(RES_BASE + i));
            assertEquals(res_common[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "he" locale (common resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "he");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_common[i], rm.getString(RES_BASE + i));
            assertEquals(res_common[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "ja-JP" locale ("ja" resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "ja-JP");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_ja[i], rm.getString(RES_BASE + i));
            assertEquals(res_ja[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /* Retrieve device resources for "ja" locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "ja");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_ja[i], rm.getString(RES_BASE + i));
            assertEquals(res_ja[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "sk-SK" locale ("sk" resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "sk-SK");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_sk[i], rm.getString(RES_BASE + i));
            assertEquals(res_sk[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /* Retrieve device resources for "sk" locale */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "sk");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_sk[i], rm.getString(RES_BASE + i));
            assertEquals(res_sk[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "zh-CN" locale (common resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "zh-CN");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_common[i], rm.getString(RES_BASE + i));
            assertEquals(res_common[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /*
         * Retrieve device resources for "zh" locale (common resources are
         * hierarchically matched)
         */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "zh");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_common[i], rm.getString(RES_BASE + i));
            assertEquals(res_common[i], rm.getResource(RES_BASE + i));
        }
        assertNotNull(rm.getResource(FORMAT_SYMBOLS));

        /* Retrieve common device resources explicitly */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "");
        assertNotNull(rm);
        for (int i = 0; i < 3; i++) {
            assertTrue(rm.isValidResourceID(RES_BASE + i));
            assertEquals(res_common[i], rm.getString(RES_BASE + i));
            assertEquals(res_common[i], rm.getResource(RES_BASE + i));
        }
    }

    public void testInvalidDeviceResources() {
        /* Test reading of resource with illegal identifier */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en-US");
        try {
            rm.getResource(0x80000000);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test reading of resource with illegal identifier as string */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en-US");
        try {
            rm.getString(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test reading of resource with illegal identifier as binary data */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "en-US");
        try {
            rm.getData(-12345);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        /* Test reading of non-existent resource */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "zh");
        try {
            rm.getResource(0x12345678);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.RESOURCE_NOT_FOUND,
                         e.getErrorCode());
        }

        /* Test reading of non-existent resource as string */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "zh");
        try {
            rm.getString(0);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.RESOURCE_NOT_FOUND,
                         e.getErrorCode());
        }

        /* Test reading of non-existent resource as binary data */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "zh");
        try {
            rm.getData(0xabcdef);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.RESOURCE_NOT_FOUND,
                         e.getErrorCode());
        }

        /* Test reading of string resource as binary data */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "cs-CZ");
        try {
            rm.getData(RES_BASE);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.WRONG_RESOURCE_TYPE,
                         e.getErrorCode());
        }

        /* Test reading of user-defined resource as binary data */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "sk-SK");
        try {
            rm.getData(FORMAT_SYMBOLS);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.WRONG_RESOURCE_TYPE,
                         e.getErrorCode());
        }

        /* Test reading of user-defined resource as string */
        rm = null;
        rm = ResourceManager.getManager(ResourceManager.DEVICE, "sk-SK");
        try {
            rm.getString(FORMAT_SYMBOLS);
            fail("ResourceException expected");
        } catch (ResourceException e) {
            assertEquals(ResourceException.WRONG_RESOURCE_TYPE,
                         e.getErrorCode());
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
        declare("testValidDeviceResources");
        testValidDeviceResources();
        declare("testInvalidDeviceResources");
        testInvalidDeviceResources();
    }
}
