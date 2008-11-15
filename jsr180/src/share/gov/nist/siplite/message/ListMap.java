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
/*
 */
package gov.nist.siplite.message;

import gov.nist.siplite.header.*;
import java.util.Hashtable;

/**
 * A map of which of the standard headers may appear as a list
 */

class ListMap   {
    /**
     * A table that indicates whether a header has a list representation or
     * not (to catch adding of the non-list form when a list exists.)
     * Entries in this table allow you to look up the list form of a header
     * (provided it has a list form).
     */
    private static Hashtable headerListTable;

    /** Static initializer */
    static {
        initializeListMap();
    }

    /**
     * Builds a table mapping between objects that have a list form
     * and the class of such objects.
     */
    static private void initializeListMap() {
        headerListTable = new Hashtable();

        headerListTable.put(ExtensionHeader.clazz,
                new HeaderList().getClass());

        headerListTable.put(ParameterLessHeader.clazz,
                new HeaderList().getClass());

        headerListTable.put(ContactHeader.clazz,
                new ContactList().getClass());

        headerListTable.put(ViaHeader.clazz,
                new ViaList().getClass());

        headerListTable.put(WWWAuthenticateHeader.clazz,
                new WWWAuthenticateList().getClass());

        headerListTable.put(RouteHeader.clazz,
                new RouteList().getClass());

        headerListTable.put(ProxyAuthenticateHeader.clazz,
                new ProxyAuthenticateList().getClass());

        headerListTable.put(ProxyAuthorizationHeader.clazz,
                new HeaderList().getClass());

        headerListTable.put(RecordRouteHeader.clazz,
                new RecordRouteList().getClass());
    }

    /**
     * Returns true if this has an associated list object.
     * @param sipHeader the requested header to be checked
     * @return true if list is present
     */
    static protected boolean hasList(Header sipHeader) {
        if (sipHeader instanceof HeaderList)
            return false;
        else {
            Class headerClass = sipHeader.getClass();
            return headerListTable.get(headerClass) != null;
        }
    }

    /**
     * Returns true if this has an associated list object.
     * @param sipHdrClass the class to be checked
     * @return true if listis present
     */
    static protected boolean hasList(Class sipHdrClass) {
        return headerListTable.get(sipHdrClass) != null;
    }

    /**
     * Gets the associated list class.
     * @param sipHdrClass the class to be checked
     * @return the list class
     */
    static protected Class getListClass(Class sipHdrClass) {
        return (Class) headerListTable.get(sipHdrClass);
    }

    /**
     * Returns a list object for this header if it has an associated
     * list object.
     * @param sipHeader the requested header with associated list
     * @return list object
     */
    static protected HeaderList getList(Header sipHeader) {
        try {
            Class headerClass = sipHeader.getClass();
            Class listClass = (Class) headerListTable.get(headerClass);
            HeaderList shl =  (HeaderList) listClass.newInstance();
            shl.setHeaderName(sipHeader.getHeaderName());
            return shl;
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex)  {
            ex.printStackTrace();
        }
        return null;
    }
}
