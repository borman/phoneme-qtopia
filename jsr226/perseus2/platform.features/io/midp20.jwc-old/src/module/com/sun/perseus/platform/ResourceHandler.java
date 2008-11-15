/*
 * $RCSfile: ResourceHandler.java,v $
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

package com.sun.perseus.platform;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.Permissions;

import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.j2me.storage.File;
import javax.microedition.io.Connector;

import com.sun.perseus.platform.GZIPInputStream;

/**
 * This class provides a way to securely access platform resources.
 * On some versions of the Java platform, there is a need to specify
 * a security token to access resources. This allows different 
 * versions of the security features.
 *
 * @author <a href="mailto:vincent.hardy@sun.com">Vincent Hardy</a>
 * @version $Id: ResourceHandler.java,v 1.5 2006/07/17 00:35:44 st125089 Exp $
 */
public final class ResourceHandler {

    private static final int DEFAULT_FONT = 1;
    private static final int INITIAL_FONT = 2;
    private static final String DEFAULT_FONT_RESOURCE = "defaultFont.svg";
    private static final String INITIAL_FONT_RESOURCE = "initialFont.svg";

    /**
     * The ResourceHandler class has to be initialized by the SecurityInitializer
     * implementation.
     */
    static private SecurityToken internalSecurityToken;

    static {
        try {
            internalSecurityToken = SecurityInitializer.getSecurityToken();
        } catch (SecurityException se) {
            // Just log the error. This may happen in development context
            // when running a midlet which bundles the SVG engine.
            se.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // Just log the error. This may happen in development context
            // when running a midlet which bundles the SVG engine.
            e.printStackTrace();
        }

        initSecurity("com.sun.perseus.builder.DefaultFontFace");
    }

    /**
     * Set to <code>true</code> to allow {@link #getSecurityToken}
     * will give out the security token. 
     */
    private static boolean dispensingEnabled = false;

    /**
     * Don't allows instances to be created.
     */
    private ResourceHandler() {
    }

    /**
     * This method paralels CLDC's SecurityInitializer's getSecurityToken method.
     *
     * @see #getSystemResource
     * @return null
     */
    public static Object getSecurityToken() {
        if (dispensingEnabled) {
            return internalSecurityToken;
        } else {
            throw new SecurityException();
        }
    }

    /**
     * Method to trigger the static initializer for a class.
     * The {@link #dispensingEnabled} is enabled only for the
     * duration of the class initialization to allow that static
     * initializer to call {@link #getSecurityToken}.
     * <p>
     * ClassNotFoundExceptions are logged but other exceptions
     * caused by class initialization are thrown.
     * @param classname the name of the class to initialize.
     */
    private static void initSecurity(String classname) {
        try {
            /*
             * Enable giving out the security token, force static
             * initialization of named classes, then disable giving
             * out the security token.  This allows the static initializers
             * of the named classes to fetch the security token.
             * ClassNotFoundException is ignored; these may occur
             * if the class has been configured out of the system.
             */
            dispensingEnabled = true;
            Class.forName(classname);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } finally {
            dispensingEnabled = false;
        }
    }
    
    final public static InputStream getInitialFontResource() {
        InputStream is = null;
        
        try {
            is = getSystemResource(INITIAL_FONT_RESOURCE, 
                                   internalSecurityToken);
        } catch (SecurityException se) {
            //log security exceptions
            se.printStackTrace();
        }

        return is;
    }

    final public static InputStream getDefaultFontResource() {
        InputStream is = null;

        try {
            is = getSystemResource(DEFAULT_FONT_RESOURCE, 
                                   internalSecurityToken);
        } catch (SecurityException se) {
            //log security exceptions
            se.printStackTrace();
        }

        return is;
    }


    /**
     * @param resourceName the name of the resource to be retrieved. This has the 
     * same semantic and syntax as the java.lang.Class#getrResourceAsStream's name 
     * parameter.
     * @param securityToken opaque object which the caller must provide to grant
     * access to the system resource. Note that on some platform, this securityToken
     * is ignored because the access to the platform resources are secured through other
     * mechanisms.
     * @return null if the resource is not found. Otherwise, an stream to the 
     * requested resource.
     */
    public static InputStream getSystemResource(final String resourceName,
                                                final Object securityToken) {
        if (securityToken == null) {
            return ResourceHandler.class.getResourceAsStream(resourceName);
        } else {
            InputStream is = null;
            RandomAccessStream storage =
                new RandomAccessStream((SecurityToken) securityToken);

            try {
                // extract the file name part of the full resource name
                int namePartIdx = resourceName.lastIndexOf('/');
                String namePart = (namePartIdx != -1) ?
                    resourceName.substring(namePartIdx + 1) : resourceName;

                if (namePart.equals(DEFAULT_FONT_RESOURCE)) {
                    getRomizedResource(DEFAULT_FONT);
            
                    is = new GZIPInputStream(new ByteArrayInputStream(defaultFont));                                    
                    defaultFont = null;
                } else if (namePart.equals(INITIAL_FONT_RESOURCE)) {
                    getRomizedResource(INITIAL_FONT);
                    is = new GZIPInputStream(new ByteArrayInputStream(
                                                                initialFont));                                            
                    initialFont = null;
                } else { 
                    storage.connect(File.getConfigRoot() + namePart,
                                    Connector.READ);
                    byte[] data = new byte[storage.getSizeOf()];
                    storage.readBytes(data, 0, data.length);
                    is = new ByteArrayInputStream(data);
                }
                
            } catch (IOException e) {
                System.out.println("Error in getSystemResource");
            } finally {
                try {
                    storage.disconnect();
                } catch (IOException ignored) {
                }
            }
            return is;
        }
    }

    /**
     * gets romized byte array from native and stores it in the c
     * orresponding Java array
     */
    static private native void getRomizedResource(int font);

    /** holds romized resource, will be set by native */ 
    public static byte[] defaultFont = new byte[8247];
    public static byte[] initialFont = new byte[417];
}
