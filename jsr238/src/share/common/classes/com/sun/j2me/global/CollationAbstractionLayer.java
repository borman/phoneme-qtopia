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

import com.sun.j2me.main.Configuration;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * The <code>CollationAbstractionLayer</code> class provides a layer of
 * abstraction for {@link javax.microedition.global.StringComparator}
 * realization. Sub-classes of <code>CollationAbstractionLayer</code>
 * correspond to individual implementations of string comparison.
 */
public abstract class CollationAbstractionLayer {

    /**
     *  Constant name of collation abstraction layer class property.
     */
    private final static String ABSTRACTION_LAYER_PROPERTY =
            "microedition.global.collation.abstractionlayer";

    /**
     *  Constant name of default collation abstraction layer class.
     */
    private final static String DEFAULT_ABSTRACTION_LAYER =
            "com.sun.j2me.global.CollationAbstractionLayerImpl";

    /**
     * An instance of <code>CollationAbstractionLayer</code>, which is
     * used in the {@link #getInstance() getInstance} method.
     */
    private static CollationAbstractionLayer abstractionLayer;


    /**
     * Returns an instance <code>CollationAbstractionLayer</code>. This
     * instance is created only once (singleton) and then it is reused when
     * the method is called again.
     *
     * @return    the instance of the <code>AbstractionLayer</code> sub-class,
     *            <code>null</code> if unable to get sub-class instance
     */
    public static CollationAbstractionLayer getInstance() {
        if (abstractionLayer == null) {
            String alClsName =
                    Configuration.getProperty(ABSTRACTION_LAYER_PROPERTY);
            if (alClsName != null) {
                try {
                    abstractionLayer = (CollationAbstractionLayer)
                            Class.forName(alClsName).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "default Collation handler class does not exist:"
                            + alClsName);
                    }
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "default Collation handler class missing constructor:"
                            + alClsName);
                    }
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "default Collation handler class incorrect type:"
                            + alClsName);
                    }
                }
            }
            if (abstractionLayer == null) {
                // try default abstraction layer
                try {
                    abstractionLayer = (CollationAbstractionLayer)Class.forName(
                            DEFAULT_ABSTRACTION_LAYER).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Default Collation handler class does not exist:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Collation handler class missing constructor:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Collation handler class incorrect type:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                }
            }
        }

        return abstractionLayer;
    }

    /**
     * Returns an instance of the <code>StringComparator</code> sub-class,
     * which realizes platform-specific <code>StringComparator</code> methods.
     *
     * @param locale  the locale to use with this <code>StringComparator</code>
     * @param level   level of collation, as defined in {@link
     *      javax.microedition.global.StringComparator}
     * @return the instance of the <code>StringComparator</code> realization
     * @see CommonStringComparator
     */
    public abstract CommonStringComparator getStringComparator(String locale,
            int level);

    // JAVADOC COMMENT ELIDED - see StringComparator.getSupportedLocales()
    // description
    public abstract String[] getSupportedLocales();
}
