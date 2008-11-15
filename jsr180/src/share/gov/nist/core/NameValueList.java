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
package gov.nist.core;
import java.util.*;
import java.util.Hashtable;

/**
 * Implements a simple NameValue association with a quick lookup.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NameValueList   extends GenericObject {
    /** Internal name value list. */
    private  Hashtable    nvList;
    /** Separator character. */
    private  String    separator;


    /**
     * Constructs a new list.
     * @param listName label for this list
     */
    public NameValueList(String listName) {
        nvList = new Hashtable();
        this.separator = Separators.SEMICOLON;
    }

    /**
     * Gets a list of key names.
     * @return list of key names.
     */
    public Vector getNames() {
        Vector names = new Vector();
	Enumeration enumNames = nvList.keys();

	while (enumNames.hasMoreElements()) {
	    names.addElement(enumNames.nextElement());
	}

        return names;
    }

    /**
     * Gets the enumeration of key names.
     * @return enumeration of key names.
     */
    public Enumeration getKeys() {
        return nvList.keys();
    }

    /**
     * Adds a name value pair to the list.
     * @param nv the data to be stored
     */
    public void add(NameValue nv) {
        if (nv == null)
            throw new NullPointerException("null nv");
        nvList.put(nv.getName(), nv.getValue());
    }

    /**
     * Sets a namevalue object in this list.
     * @param nv the data to be updated
     */
    public void set(NameValue nv) {
        this.add(nv);
    }

    /**
     * Sets a namevalue object in this list.
     * @param name the label for the data element
     * @param value the value for the element
     */
    public void set(String name, Object value) {
        NameValue nv = new NameValue(name, value);
        this.set(nv);
    }

    /**
     * Adds a name value record to this list.
     * @param name the label for the data element
     * @param obj the value for the data element
     */
    public void add(String name, Object obj) {
        if (name == null)
            throw new NullPointerException("name in null ! ");
        NameValue nv = new NameValue(name, obj);
        add(nv);
    }

    /**
     * Compares if two NameValue lists are equal.
     * @param otherObject  is the object to compare to.
     * @return true if the two objects compare for equality.
     */
    public boolean equals(Object otherObject) {
        if (!otherObject.getClass().equals
                (this.getClass())) {
            return false;
        }
        NameValueList other = (NameValueList) otherObject;

        if (this.nvList.size() != other.nvList.size()) {
            return false;
        }
	Enumeration enumNames = nvList.keys();
        String currKey;
	Object currValue, currValueOther;
	while (enumNames.hasMoreElements()) {
	    currKey = (String)enumNames.nextElement();
	    currValue = this.nvList.get(currKey);
	    currValueOther = other.nvList.get(currKey);
	    if (
	        (currValueOther == null) || !currValue.equals(currValueOther)) {
                return false;
	    }
	}
        return true;
    }


    /**
     * Do a lookup on a given name and return value associated with it.
     * @param name to be looked up
     * @return the object that was found or null if not found
     */
    public Object  getValue(String name) {
        return nvList.get(name.toLowerCase());
    }

    /**
     * Do a lookup on a given name and return value associated with it.
     * @param name to be looked up
     * @param nameDefault to be returned when name is not found
     * @return the object that was found or default if not found
     */
    public String getValueDefault(String name, String nameDefault) {
        String returnValue = (String)nvList.get(name.toLowerCase());
	if (returnValue == null) returnValue = nameDefault;
        return returnValue;
    }

    /**
     * Gets the NameValue record given a name.
     * @param name the data element laebl to find
     * @return the name value found or null if not found
     * @since 1.0
     */
    public NameValue getNameValue(String name) {
        if (name == null)
            throw new NullPointerException("null arg!");
        String name1  = name.toLowerCase();
        NameValue returnValue = null;
	Object value = getValue(name1);
	if (value != null)
	    returnValue = new NameValue(name1, value);
        return returnValue;
    }

    /**
     * Returns a boolean telling if this NameValueList
     * has a record with this name.
     * @param name the label to find
     * @return true if the element includes a value
     * @since 1.0
     */
    public boolean hasNameValue(String name) {
        return nvList.containsKey(name.toLowerCase());
    }

    /**
     * Removes the element corresponding to this name.
     * @param name the label to find
     * @return true if successfully removed
     * @since 1.0
     */
    public boolean delete(String name) {
        if (name == null) {
            return true;
        }

        String name1  = name.toLowerCase();
	nvList.remove(name1);

        return true;
    }

    /**
     * Default constructor.
     */
    public NameValueList() {
        nvList = new Hashtable();
	this.separator = Separators.SEMICOLON;
    }

    /**
     * Makes a copy of the current instance.
     * @return copy of current object
     */
    public Object clone()   {
        NameValueList retval = new NameValueList();
        retval.separator = this.separator;
	Enumeration enumNames = nvList.keys();
        String currKey;
	while (enumNames.hasMoreElements()) {
            currKey = (String)enumNames.nextElement();
	    retval.add(currKey, nvList.get(currKey));
	}
        return retval;

    }

    /**
     * Gets the parameter as a String.
     * @param name the label to find
     * @return the parameter as a string.
     */
    public String getParameter(String name) {
        Object val = this.getValue(name);
        if (val == null)
            return null;
        if (val instanceof GenericObject)
            return ((GenericObject)val).encode();
        else return val.toString();
    }

    /**
     * Encodes the contenst as a string.
     * @return the encoded text string.
     */
    public String encode() {
        if (nvList.size() == 0)
            return "";

        StringBuffer encoding = new StringBuffer();
	Enumeration enumNames = nvList.keys();
        String currKey;
	Object currValue;

	while (enumNames.hasMoreElements()) {
            currKey = (String)enumNames.nextElement();
	    encoding.append(currKey);
	    currValue = nvList.get(currKey);

	    if (currValue != null) {
                if (currValue instanceof GenericObject) {
                    GenericObject gobj = (GenericObject) currValue;
                    encoding.append(Separators.EQUALS + gobj.encode());
                } else {
                    String s = currValue.toString();

                    if (s.length() > 0) {
                        encoding.append(Separators.EQUALS + s);
                    }
                }
	    }

	    if (enumNames.hasMoreElements()) // not last
	        encoding.append(separator);
	}

        return encoding.toString();
    }

    /**
     * Encodes the contenst as a string followd by separator.
     * @return the encoded text string.
     */
    public String encodeWithSep() {
        String retVal = encode();
        if (retVal.length() > 0) {
            retVal = separator + retVal;
        }
        return retVal; 
    }

    /**
     * Converts contenets to a string.
     * @return contenets encoded in a text string
     */
    public String toString() {
        return this.encode();
    }

    /**
     * Sets the separator string to be used in formatted contents.
     * @param separator string to use between fields
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Checks if the listis empty.
     * @return true if the size of the list is zero
     */
    public boolean isEmpty() {
        return this.nvList.size() == 0;
    }

    /**
     * Gets the size of the list.
     * @return the count of elements in the list
     */
    public int size() {
        return nvList.size();
    }
}
