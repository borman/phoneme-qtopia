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
package gov.nist.siplite.header;

import gov.nist.core.*;
import java.util.*;

/**
 * This is a list for the headers.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class HeaderList extends Header {
    /** SIP header list. */
    protected Vector sipHeaderVector;

    /**
     * Copies the current instance.
     * @return copy of the current instance
     */
    public Object clone() {
        try {
            HeaderList retval =
                    (HeaderList)this.getClass().newInstance();
            if (this.headerName != null)
                retval.headerName = new String(this.headerName);
            if (this.headerValue != null)
                retval.headerValue = new String(this.headerValue);
            retval.sipHeaderVector = new Vector();
            for (int i = 0; i < sipHeaderVector.size(); i ++) {
                Header siphdr = (Header) sipHeaderVector.elementAt(i);
                Header newHdr = (Header) siphdr.clone();
                retval.sipHeaderVector.addElement(newHdr);
            }
            return (Object) retval;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.print("Problem with clone method");
            System.exit(0);
            return null;
        }
    }

    /**
     * Default constructor
     */
    public HeaderList() {
        sipHeaderVector = new Vector();
    }

    /**
     * Concatenates two compatible lists. This appends or prepends the new list
     * to the end of this list.
     * @param other HeaderList to set
     * @param top boolean to set
     */
    public void concatenate(HeaderList other, boolean top) {
        if (other != null) {
            if (top) {
                for (int i = 0; i < size(); i++) {
                    Header sipHeader = (Header)elementAt(i);
                    other.add(sipHeader);
                }
            } else {
                for (int i = 0; i < other.size(); i++) {
                    Header sipHeader = (Header)other.elementAt(i);
                    add(sipHeader);
                }
            }
        }
    }

    /**
     * Constructor with initial header.
     * @param sipHeaderName to set
     */
    public HeaderList(String sipHeaderName) {
        sipHeaderVector = new Vector();
        this.headerName = sipHeaderName;
    }

    /**
     * Adds a new element.
     * @param sipHeader to add
     */
    public void add(Object sipHeader) throws IllegalArgumentException {
        if (headerName != null) {
            String expandedName1 = NameMap.expandHeaderName(headerName);
            String expandedName2 = NameMap.expandHeaderName(
                ((Header)sipHeader).getHeaderName());

            if (!expandedName1.equalsIgnoreCase(expandedName2)) {
                throw new IllegalArgumentException("bad type");
            }
        }

        if (sipHeader != null)
            sipHeaderVector.addElement(sipHeader);
    }

    /**
     * Adds a new element on the top of the list.
     * @param sipHeader to add
     */
    public void addFirst(Object sipHeader) {
        if (sipHeader != null) {
            Vector vec = new Vector();
            vec.addElement(sipHeader);
            for (int i = 0; i < sipHeaderVector.size(); i++) {
                vec.addElement(sipHeaderVector.elementAt(i));
            }
            sipHeaderVector = vec;
        }
    }

    /**
     * Returns true if this is empty.
     * @return true if no headers in the list
     */
    public boolean isEmpty() {
        return sipHeaderVector.isEmpty();
    }

    /**
     * Returns the list size.
     * @return size
     */
    public int size() {
        return sipHeaderVector.size();
    }

    /**
     * Returns the element at the position i.
     * @return Object
     * @param i index of the requested element
     */
    public Object elementAt(int i) {
        return sipHeaderVector.elementAt(i);
    }

    /**
     * Removes the specified element.
     * @param element entry to delete
     */
    public void removeElement(Object element) {
        sipHeaderVector.removeElement(element);
    }

    /**
     * Removes the first element of the list.
     */
    public void removeFirst() {
        if (sipHeaderVector.size() == 0)
            return;
        else
            sipHeaderVector.removeElementAt(0);

    }

    /**
     * Removes the lastentry in the header list.
     */
    public void removeLast() {
        if (sipHeaderVector.size() != 0) {
            sipHeaderVector.removeElementAt(sipHeaderVector.size() -1);
        }
    }

    /**
     * Returns a vector of encoded strings (one for each sipheader).
     * @return Vector containing encoded strings in this header list.
     * an empty vector is returned if this header list contains no
     * sip headers.
     */
    public Vector getHeadersAsEncodedStrings() {
        Vector retval = new Vector();

        for (int i = 0; i < size(); i++) {
            Header sipheader = (Header) elementAt(i);
            retval.addElement(sipheader.encode());
        }
        return retval;

    }

    /**
     * Returns an enumeration of the imbedded vector.
     * @return an Enumeration of the elements of the vector.
     */
    public Enumeration getElements() {
        return this.sipHeaderVector.elements();
    }

    /**
     * Gets the first element of the vector.
     * @return the first element of the vector.
     */
    public Header getFirst() {
        if (sipHeaderVector.size() == 0)
            return null;
        return (Header) this.sipHeaderVector.elementAt(0);
    }

    /**
     * Gets the first element of the vector.
     * @return the first element of the vector.
     */
    public Object first() {
        if (sipHeaderVector.size() == 0)
            return null;
        return this.sipHeaderVector.elementAt(0);
    }

    /**
     * Gets the last element of the vector.
     * @return the last element of the vector.
     */
    public Object last() {
        if (sipHeaderVector.size() == 0)
            return null;
        else return (Header) this.sipHeaderVector.elementAt
                (sipHeaderVector.size() - 1);

    }

    /**
     * Gets the value of the header list.
     * @return a vector of the header list contents
     */
    public Object getValue() {
        Vector retval = new Vector();
        for (int i = 0; i < size(); i++) {
            Header sipheader = (Header) elementAt(i);
            retval.addElement(sipheader);
        }
        return retval;
    }

    /**
     * Gets the parameters of the header list.
     *
     * @return always returns null
     */
    public NameValueList getParameters() {
        return null;
    }

    /**
     * Encodes the contents as a string.
     * @return encoded string of object contents
     */
    public String encode() {
        if (sipHeaderVector.isEmpty() || headerName == null)
            return "";

        StringBuffer encoding = new StringBuffer();

        // The following headers do not have comma separated forms for
        // multiple headers. Thus, they must be encoded separately.
        if (this.headerName.equals(WWW_AUTHENTICATE) ||
                this.headerName.equals(PROXY_AUTHENTICATE) ||
                this.headerName.equals(AUTHORIZATION) ||
                this.headerName.equals(PROXY_AUTHORIZATION)) {

            for (int i = 0; i < sipHeaderVector.size(); i++) {
                Header sipheader =
                        (Header) sipHeaderVector.elementAt(i);
                encoding.append(sipheader.encode());
            }

            return encoding.toString();
        } else {
            // These can be concatenated together in an comma separated
            // list.
            return headerName + Separators.COLON + Separators.SP +
                    this.encodeBody() + Separators.NEWLINE;
        }
    }

    /**
     * Encodes body as a string.
     * @return encoded string of body contents
     */
    protected String encodeBody() {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < sipHeaderVector.size(); i++) {
            Header sipHeader =
                    (Header) sipHeaderVector.elementAt(i);
            sbuf.append(sipHeader.encodeBody());
            if (i + 1 < sipHeaderVector.size())
                sbuf.append(",");
        }
        return sbuf.toString();
    }

    /**
     * Gets the header list.
     * @return the header list
     */
    public Vector getHeaders() {
        return this.sipHeaderVector;
    }
}
