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
/**
 *  Generic structure for storing name-value pairs.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NameValue   extends GenericObject {
    /** FLag to indicate the value is a quoted string. */
    protected boolean isQuotedString;
    /** Field separator for name value encoding. */
    protected String separator;
    /** Characters used for quoting strings. */
    protected String quotes;
    /** The label for the name value pair. */
    protected String name;
    /** The value for this data element. */
    protected Object value;

    /** Default constructor. */
    public NameValue() {
        name = null; value = null;
        separator = Separators.EQUALS;
        this.quotes = "";
    }

    /**
     * Constructs a name value pair from initial strings.
     * @param n the name of the key
     * @param v the value for the pair
     */
    public NameValue(String n, Object v) {
        name = n;
        separator = Separators.EQUALS;
        quotes = "";
        setValue(v);
    }
    /**
     * Sets the separator for the encoding method below.
     * @param sep the field separator for the encoded string
     */
    public void setSeparator(String sep) {
        separator = sep;
    }

    /**
     * Sets the type of the field to be a quoted string.
     * A flag that indicates that doublequotes should be put around the
     * value when encoded
     * (for example name=value when value is doublequoted).
     */
    public void setQuotedValue() {
        isQuotedString = true;
        this.quotes = Separators.DOUBLE_QUOTE;
    }

    /**
     * Returns true if the value is quoted in doublequotes.
     * @return true if the value is of type quoted string
     */
    public boolean isValueQuoted() {
        return isQuotedString;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value.
     * @return the value
     */
    public Object getValue() {
        if (isValueQuoted()) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }

    /**
     * Gets the unquoted value.
     * @return the value
     */
    public Object getUnquotedValue() {
        return value;
    }

    /**
     * Sets the name member.
     * @param n the name for the key
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * Sets the value member.
     * @param v the value for the pair
     */
    public void setValue(Object v) {
        value = v;
        if (value != null) {
            if (value instanceof String) {
                String str = (String)value;
                if (str.startsWith("\"") && str.endsWith("\"")) {
                    setQuotedValue();
                    str = str.substring(1, str.length() - 1);
                    value = (Object)str;
                }
            }
        }
    }

    /**
     * Gets the encoded representation of this namevalue object.
     * Added doublequote for encoding doublequoted values
     * @since 1.0
     * @return an encoded name value (eg. name=value) string.
     */
    public String encode() {
        if (name != null && value != null) {
            return name + separator + quotes +
                    value.toString() +  quotes;
        } else if (name == null && value != null) {
            return quotes + value.toString() + quotes;
        } else if (name != null && value == null) {
            return name;
        } else return "";
    }

    /**
     * Makes a copy of the current instance.
     * @return copy of current object
     */
    public Object clone() {
        NameValue retval = new NameValue();
        retval.separator = this.separator;
        retval.isQuotedString = this.isQuotedString;
        retval.quotes = this.quotes;
        retval.name = this.name;
        if (value != null && value instanceof GenericObject) {
            retval.value = ((GenericObject)this.value).clone();
        } else retval.value = this.value;
        return retval;
    }

    /**
     * Equality comparison predicate.
     * @param other the object for comparison
     * @return true if the instances are equivalent
     */
    public boolean equals(Object other) {
        if (! other.getClass().equals(this.getClass()))
            return false;
        NameValue that = (NameValue) other;
        if (this == that)
            return true;
        if (this.name  == null && that.name != null ||
                this.name != null && that.name == null) return false;
        if (this.name != null && that.name != null &&
                this.name.toLowerCase().compareTo
                (that.name.toLowerCase()) != 0)
            return false;
        if (this.value != null && that.value == null ||
                this.value == null && that.value != null)
            return false;
        if (this.value == that.value)
            return true;
        if (value instanceof String) {
            // Quoted string comparisions are case sensitive.
            if (isQuotedString)
                return this.value.equals(that.value);
            String val = (String) this.value;
            String val1 = (String) that.value;
            return val.toLowerCase().
                    equals(val1.toLowerCase());
        } else return this.value.equals(that.value);
    }

    /**
     * Converts the contents to a string.
     * @return the encoded string contenets
     */
    public String toString() {
        return this.encode();
    }

}
