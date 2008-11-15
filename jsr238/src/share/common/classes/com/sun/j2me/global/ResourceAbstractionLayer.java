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

import com.sun.j2me.main.Configuration;
import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * The <code>ResourceAbstractionLayer</code> class provides a layer of
 * abstraction for {@link javax.microedition.global.ResourceManager}
 * implementation. Sub-classes of <code>ResourceAbstractionLayer</code>
 * correspond to individual implementations of access to application and
 * device resources.
 */
public abstract class ResourceAbstractionLayer {

    /**
     *  Constant name of resource abstraction layer class property.
     */
    private final static String ABSTRACTION_LAYER_PROPERTY =
            "microedition.global.resource.abstractionlayer";

    /**
     *  Constant name of default resource abstraction layer class.
     */
    private final static String DEFAULT_ABSTRACTION_LAYER =
            "com.sun.j2me.global.ResourceAbstractionLayerImpl";

    /**
     * An instance of <code>ResourceAbstractionLayer</code>, which is
     * used in the {@link #getInstance() getInstance} method.
     */
    private static ResourceAbstractionLayer abstractionLayer;


    /**
     * Returns an instance <code>ResourceAbstractionLayer</code>. This
     * instance is created only once (singleton) and then it is reused when
     * the method is called again.
     *
     * @return    the instance of the <code>AbstractionLayer</code> sub-class,
     *            <code>null</code> if unable to get sub-class instance
     */
    public static ResourceAbstractionLayer getInstance() {
        if (abstractionLayer == null) {
            String alClsName =
                    Configuration.getProperty(ABSTRACTION_LAYER_PROPERTY);
            if (alClsName != null) {
                try {
                    abstractionLayer = (ResourceAbstractionLayer)
                            Class.forName(alClsName).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Resource handler class does not exist:"
                            + alClsName);
                    }
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Resource handler class missing constructor:"
                            + alClsName);
                    }
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Resource handler class incorrect type:"
                            + alClsName);
                    }
                }
            }
            if (abstractionLayer == null) {
                // try default abstraction layer
                try {
                    abstractionLayer = (ResourceAbstractionLayer)Class.forName(
                            DEFAULT_ABSTRACTION_LAYER).newInstance();
                } catch (ClassNotFoundException cnf_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Default Resource handler class does not exist:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                } catch (InstantiationException ie_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Resource handler class missing constructor:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                } catch (IllegalAccessException iae_ignore) {
                    /* intentionally ignored */
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_JSR238,
                            "Resource handler class incorrect type:"
                            + DEFAULT_ABSTRACTION_LAYER);
                    }
                }
            }
        }

        return abstractionLayer;
    }

    /**
     *  A resource manager factory for creating application resource managers.
     */
    protected ResourceManagerFactory appResourceManagerFactory;


    /**
     * Returns an instance of the <code>ResourceManagerFactory</code> sub-class,
     * which is used to create resource managers for accessing application
     * resources.
     *
     * @return the instance of the application resource manager factory
     * @see ResourceManagerFactory
     */
    public ResourceManagerFactory getAppResourceManagerFactory() {
        if (appResourceManagerFactory == null) {
            appResourceManagerFactory = new AppResourceManagerFactory(null);
        }
        return appResourceManagerFactory;
    }

    /**
     * Returns an instance of the <code>ResourceManagerFactory</code> sub-class,
     * which is used to create resource managers for accessing device specific
     * resources.
     *
     * @return the instance of the device resource manager factory
     * @see ResourceManagerFactory
     */
    public abstract ResourceManagerFactory getDevResourceManagerFactory();
}
