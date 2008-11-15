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
 * This class customizes the {@link javax.microedition.global.Formatter}
 * implementation to the emulator needs.
 */
public class FormatAbstractionLayerImpl extends FormatAbstractionLayer {

    /**
     * Create instance of <code>FormatAbstractionLayerImpl</code>. This
     * constructor is necessary because of <code>Class.forName()</code> creation
     * call in {@link com.sun.j2me.global.FormatAbstractionLayer#getInstance}.
     */
    public FormatAbstractionLayerImpl() { }

    /**
     * Returns an instance of the <code>FormatterImpl</code> class,
     * which realizes most of <code>Formatter</code> methods.
     *
     * @param locale  the locale to use with this <code>Formatter</code>
     * @return the instance of the FormatterImpl
     */
    public CommonFormatter getFormatter(String locale) {
        return new FormatterImpl(locale);
    }

    // JAVADOC COMMENT ELIDED - see Formatter.getSupportedLocales() description
    public String[] getSupportedLocales() {
        String[] locales;
        String[] res_locales =
                ResourceManager.getSupportedLocales(ResourceManager.DEVICE);
        if (res_locales[0].equals("")) {
            int len = res_locales.length - 1;
            locales = new String[len];
            System.arraycopy(res_locales, 1, locales, 0, len);
            return locales;
        }
        return res_locales;
    }
}
