/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
package gov.nist.siplite;
import java.util.Hashtable;

    /**
    * Configuration properties.
  + * Configuration properties are set in StackConnector before initializing 
  + * a sipstack
  + * Following are mandatory Configuration Properties :
  + *
  + * javax.sip.IP_ADDRESS 
  + * javax.sip.STACK_NAME 
  + *
  + * Following are optional Configuration Properties :
  + *
  + * javax.sip.OUTBOUND_PROXY
  + * javax.sip.EXTENSION_METHODS
  + * gov.nist.javax.sip.LOG_FILE_NAME
  + * gov.nist.javax.sip.TRACE_LEVEL
  + * gov.nist.javax.sip.BAD_MESSAGE_LOG
  + * gov.nist.javax.sip.SERVER_LOG
  + * gov.nist.javax.sip.MAX_CONNECTIONS
  + * gov.nist.javax.sip.THREAD_POOL_SIZE
  + * gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS
    */
public class ConfigurationProperties extends Hashtable {
    /** Default constructor. */
    public ConfigurationProperties() {
        super();
    }
    
    /**
     * Gets a property value.
     * @param name key for the property
     * @return the value of the property
     */
    public String getProperty(String name) {
        return (String)super.get(name);
    }
    
    /**
     * Sets a property value.
     * @param name the key for theproperty
     * @param value the value for the property
     */
    public void setProperty(String name, String value) {
        super.put(name, value);
    }
    
}

