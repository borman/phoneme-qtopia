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

import javax.microedition.global.Formatter;
import com.sun.j2me.main.Configuration;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * The <code>FormatAbstractionLayer</code> class provides a layer of
 * abstraction for {@link javax.microedition.global.Formatter}
 * realization. Sub-classes of <code>FormatAbstractionLayer</code>
 * correspond to individual implementations of data formatting.
 */
public abstract class FormatAbstractionLayer {

    /**
     *  Constant name of formatting abstraction layer class property.
     */
    private final static String ABSTRACTION_LAYER_PROPERTY =
            "microedition.global.format.abstractionlayer";

    /**
     *  Constant name of default formatting abstraction layer class.
     */
    private final static String DEFAULT_ABSTRACTION_LAYER =
            "com.sun.j2me.global.FormatAbstractionLayerImpl";

    /**
     * An instance of <code>FormatAbstractionLayer</code>, which is
     * used in the {@link #getInstance} method.
     */
    private static FormatAbstractionLayer abstractionLayer;

    /**
     * An instance of <code>NeutralFormatterImpl</code>, which is
     * used in the {@link #getNeutralFormatter} method.
     */
    private static CommonFormatter neutralFormatter;

    /**
     * Returns an instance <code>FormatAbstractionLayer</code>. This
     * instance is created only once (singleton) and then it is reused when
     * the method is called again.
     *
     * @return    the instance of the <code>AbstractionLayer</code> sub-class,
     *            <code>null</code> if unable to get sub-class instance
     */
    public static FormatAbstractionLayer getInstance() {
        if (abstractionLayer == null) {
            String alClsName =
                    Configuration.getProperty(ABSTRACTION_LAYER_PROPERTY);
            if (alClsName != null) {
                try {
                    abstractionLayer = (FormatAbstractionLayer)
                            Class.forName(alClsName).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Formatter handler class does not exist or renamed: " + alClsName);
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Formatter handler class missing constructor: "
                        + alClsName);
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Formatter handler class incorrect type: "
                        + alClsName);
                }
            }
            if (abstractionLayer == null) {
                // try default abstraction layer
                try {
                    abstractionLayer = (FormatAbstractionLayer)Class.forName(
                            DEFAULT_ABSTRACTION_LAYER).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Default Formatter handler class does not exist or renamed: "
                        + DEFAULT_ABSTRACTION_LAYER);
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Formatter handler class missing constructor: "
                        + DEFAULT_ABSTRACTION_LAYER);
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                        "Formatter handler class incorrect type: "
                        + DEFAULT_ABSTRACTION_LAYER);
                }
            }
        }

        return abstractionLayer;
    }

    /**
     * Returns an instance <code>NeutralFormatterImpl</code>. This
     * instance is created only once (singleton) and then it is reused when
     * the method is called again.
     *
     * @return    the instance of the <code>NeutralFormatterImpl</code> class
     */
    public static CommonFormatter getNeutralFormatter() {
        if (neutralFormatter == null) {
            neutralFormatter = new NeutralFormatterImpl();
        }
        return neutralFormatter;
    }

    /**
     * Returns an instance of the <code>Formatter</code> sub-class, which
     * realizes platform-specific <code>Formatter</code> methods.
     *
     * @param locale  the locale to use with this <code>Formatter</code>
     * @return the instance of the <code>Formatter</code> sub-class
     * @see javax.microedition.global.Formatter
     */
    public abstract CommonFormatter getFormatter(String locale);

    // JAVADOC COMMENT ELIDED - see Formatter.getSupportedLocales() description
    public abstract String[] getSupportedLocales();
}
