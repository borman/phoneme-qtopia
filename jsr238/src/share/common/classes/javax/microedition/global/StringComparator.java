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

import com.sun.j2me.global.CollationAbstractionLayer;
import com.sun.j2me.global.CommonStringComparator;
import com.sun.j2me.global.LocaleHelpers;

// JAVADOC COMMENT ELIDED
public final class StringComparator {

    /** 
     * Constant for the primary collation level. For European languages this 
     * level honours differences between alphabetical characters at the language
     * level.
     */
    public static final int LEVEL1 = 1;
    /** 
     * Constant for the secondary collation level. For European languages this
     * level honours differences in normal and accented versions of the same 
     * character.
     */
    public static final int LEVEL2 = 2;
    /** 
     * Constant for the tertiary collation level. For European languages this 
     * level honours differences in character case.
     */
    public static final int LEVEL3 = 3;
    /** 
     * Constant for the comparison level that takes all differences between 
     * characters into account.
     */
    public static final int IDENTICAL = 15;
    
    /** 
     * Collation level
     */
    private int level;
    /** 
     * Locale
     */
    private String locale;

    /** 
     * Instance of the CollationAbstractionLayer
     */
    private static CollationAbstractionLayer collationAbstractionLayer = 
            CollationAbstractionLayer.getInstance();

    /** 
     * String comparator
     */
    private CommonStringComparator stringComparatorImpl;
    
    // JAVADOC COMMENT ELIDED
    public StringComparator() {
        this(System.getProperty("microedition.locale"), LEVEL1);
    }

    // JAVADOC COMMENT ELIDED
    public StringComparator(String locale) {
        this(locale, LEVEL1);
    }
    
    // JAVADOC COMMENT ELIDED
    public StringComparator(String locale, int level)
            throws UnsupportedLocaleException, IllegalArgumentException {
        
        // check the level
        if ((level < LEVEL1) || (level > LEVEL3) && (level != IDENTICAL)) {
            throw new IllegalArgumentException("Invalid level");
        }

        if (!LocaleHelpers.isValidLocale(locale) && !("".equals(locale))) {
            throw new IllegalArgumentException("Invalid locale format");
        }

        locale = LocaleHelpers.normalizeLocale(locale);

        if ("".equals(locale)) {
            this.locale = null;
        } else {
            this.locale = locale;
        }
        this.level = level;

        if (locale == null) {
            locale = "";
        }

        stringComparatorImpl = collationAbstractionLayer.getStringComparator(
                locale, level);
    }

    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedLocales() {
        return collationAbstractionLayer.getSupportedLocales();
    }

    // JAVADOC COMMENT ELIDED
    public int compare(String s1, String s2) {
        return stringComparatorImpl.compare(s1, s2);
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(String s1, String s2) {
        return stringComparatorImpl.equals(s1, s2);
    }

    // JAVADOC COMMENT ELIDED
    public int getLevel() {
        return level;
    }

    // JAVADOC COMMENT ELIDED
    public String getLocale() {
        return locale;
    }
}
