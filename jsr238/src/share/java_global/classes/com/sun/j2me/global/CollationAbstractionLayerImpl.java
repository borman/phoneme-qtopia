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

/**
 * This class customizes the {@link javax.microedition.global.StringComparator}
 * implementation to the emulator needs.
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
     * which realizes all <code>StringComparator</code> methods.
     *
     * @param locale  the locale to use with this <code>StringComparator</code>
     * @param level   level of collation, as defined in
     *      <code>StringComparator</code>
     * @return the instance of StringComparatorImpl
     */
    public CommonStringComparator getStringComparator(String locale,
            int level) {
        return new StringComparatorImpl(locale, level);
    }

    // JAVADOC COMMENT ELIDED - see StringComparator.getSupportedLocales()
    // description
    public String[] getSupportedLocales() {
        return CollationElementTableImpl.getSupportedLocales();
    }
}
