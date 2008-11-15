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

/**
 * This class customizes the {@link javax.microedition.global.StringComparator}
 * implementation to rely on platform string comparison capabilities.
 */
public class CollationAbstractionLayerImpl extends CollationAbstractionLayer {

    /**
     * Create instance of <code>CollationAbstractionLayerImpl</code>. This
     * constructor is necessary because of <code>Class.forName()</code> creation
     * call in
     * {@link com.sun.j2me.global.CollationAbstractionLayer#getInstance}.
     */
    public CollationAbstractionLayerImpl() { }

    /**
     * Returns an instance of the {@link StringComparatorImpl} class,
     * which realizes all <code>StringComparator</code> methods in a
     * platform-specific way.
     *
     * @param  locale  the locale
     * @param  level   the level of comparison
     * @return    the instance of the <code>StringComparatorImpl</code>
     */
    public CommonStringComparator getStringComparator(String locale,
            int level) {
        return new StringComparatorImpl(locale, level);
    }

    // JAVADOC COMMENT ELIDED - see StringComparator.getSupportedLocales()
    // description
    public String[] getSupportedLocales() {
        int lcount = getCollationLocalesCount();
		if (lcount == 0) return new String[0];
		int index = 0;
		String locale = getCollationLocaleName(0);
		if (locale == null || locale.equals("")){
			index = 1;
		}
        String[] locales = new String[lcount-index];
        for (int i=0; index < lcount; index++) {
            locales[i++] = getCollationLocaleName(index);
        }

        return locales;
    }

    /**
     *  Get number of supported locales for string collation.
     *
     * @return           number of locales
     */
    public static native int getCollationLocalesCount();

    /**
     *  Get one of supported locales (by number).
     *
     * @param  index     index of locale to select
     * @return           locale
     */
    public static native String getCollationLocaleName(int index);

    /**
     * Get index of supported locales for string collation by its name.
     *
	 * @param  locale    name of locale
     * @return           index of locale
     */
    public static native int getCollationLocaleIndex(String locale);

}
