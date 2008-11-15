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
import gov.nist.siplite.address.*;

/**
 * Parameters header. Suitable for extension by headers that have parameters.
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1
 *
 */
public abstract class ParametersHeader extends Header {
    /** Contents of the parameter list. */
    protected NameValueList parameters;
    
    /** Default constructor. */
    protected ParametersHeader() {
        this.parameters = new NameValueList();
    }
    
    /**
     * Constructor with initial header name.
     * @param hdrName an initial header name
     */
    protected ParametersHeader(String hdrName) {
        super(hdrName);
        this.parameters = new NameValueList();
    }
    
    /**
     * Returns the value of the named parameter, or null if it is not set. A
     * zero-length String indicates flag parameter.
     *
     * @param name name of parameter to retrieve
     * @return the value of specified parameter
     */
    public String getParameter(String name) {
        return this.parameters.getParameter(name);
        
    }
    
    /**
     * Returns the parameter as an object (dont convert to string).
     *
     * @param name is the name of the parameter to get.
     * @return the object associated with the name.
     *
     */
    public Object getParameterValue(String name) {
        return this.parameters.getValue(name);
    }
    
    /**
     * Returns an Vector over the names (Strings) of all parameters present
     * in this ParametersHeader.
     *
     * @return an Iterator over all the parameter names
     */
    
    public Vector getParameterNames() {
        return parameters.getNames();
    }
    
    /**
     * Returns true if you have a parameter and false otherwise.
     *
     * @return true if the parameters list is non-empty.
     */
    
    public boolean hasParameters() {
        return parameters != null && ! parameters.isEmpty();
    }
    
    /**
     * Removes the specified parameter from Parameters of this ParametersHeader.
     * This method returns silently if the parameter is not part of the
     * ParametersHeader.
     *
     * @param name - a String specifying the parameter name
     */
    
    public void removeParameter(String name) {
        this.parameters.delete(name);
    }
    
    
    /**
     * Sets the value of the specified parameter. If the parameter already had
     *
     * a value it will be overwritten. A zero-length String indicates flag
     *
     * parameter.
     *
     *
     *
     * @param name - a String specifying the parameter name
     *
     * @param value - a String specifying the parameter value
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the parameter name or value.
     *
     */
    public void setParameter(String name, String value) {
        NameValue nv = parameters.getNameValue(name);
        
        if (nv != null) {
            nv.setValue(value);
        } else {
            nv = new NameValue(name, value);
        }
        
        this.parameters.set(nv);
    }
    
    /**
     * Sets the value of the specified parameter. If the parameter already had
     *
     * a value it will be overwritten. A zero-length String indicates flag
     *
     * parameter.
     *
     *
     *
     * @param name - a String specifying the parameter name
     *
     * @param value - a String specifying the parameter value
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the parameter name or value.
     *
     */
    public void setQuotedParameter(String name, String value)
    throws ParseException {
        NameValue nv = parameters.getNameValue(name);
        if (nv != null) {
            nv.setValue(value);
            nv.setQuotedValue();
        } else {
            nv = new NameValue(name, value);
            nv.setQuotedValue();
            this.parameters.set(nv);
        }
    }
    
    /**
     * Sets the value of the specified parameter. If the parameter already had
     *
     * a value it will be overwritten.
     *
     *
     * @param name - a String specifying the parameter name
     *
     * @param value - an int specifying the parameter value
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the parameter name or value.
     *
     */
    protected void setParameter(String name, int value) {
        Integer val = new Integer(value);
        NameValue nv = parameters.getNameValue(name);
        if (nv != null) {
            nv.setValue(val);
        } else {
            nv = new NameValue(name, val);
            this.parameters.set(nv);
        }
    }
    
    /**
     * Sets the value of the specified parameter. If the parameter already had
     *
     * a value it will be overwritten.
     *
     *
     * @param name - a String specifying the parameter name
     *
     * @param value - a boolean specifying the parameter value
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the parameter name or value.
     *
     */
    protected void setParameter(String name, boolean value) {
        Boolean val = new Boolean(value);
        NameValue nv = parameters.getNameValue(name);
        if (nv != null) {
            nv.setValue(val);
        } else {
            nv = new NameValue(name, val);
            this.parameters.set(nv);
        }
    }
    
    
    
    /**
     * Sets the value of the specified parameter. If the parameter already had
     *
     * a value it will be overwritten. A zero-length String indicates flag
     *
     * parameter.
     *
     *
     *
     * @param name - a String specifying the parameter name
     *
     * @param value - a String specifying the parameter value
     *
     * @throws ParseException which signals that an error has been reached
     *
     * unexpectedly while parsing the parameter name or value.
     *
     */
    protected void setParameter(String name, Object value) {
        NameValue nv = parameters.getNameValue(name);
        if (nv != null) {
            nv.setValue(value);
        } else {
            nv = new NameValue(name, value);
            this.parameters.set(nv);
        }
    }
    
    
    /**
     * Returns true if has a parameter.
     *
     * @param parameterName is the name of the parameter.
     *
     * @return true if the parameter exists and false if not.
     */
    public boolean hasParameter(String parameterName) {
        return this.parameters.hasNameValue(parameterName);
    }
    
    /**
     * Removes all parameters.
     */
    public void removeParameters() {
        this.parameters = new NameValueList();
    }
    
    /**
     * get the parameter list.
     * @return parameter list
     */
    public NameValueList getParameters() {
        return parameters;
    }
    
    /**
     * Sets the parameter given a name and value.
     *
     * @param nameValue - the name value of the parameter to set.
     */
    public void setParameter(NameValue nameValue) {
        this.parameters.set(nameValue);
    }
    
    /**
     * Sets the parameter list.
     *
     * @param parameters the name value list to set as the parameter list.
     */
    public void setParameters(NameValueList parameters) {
        this.parameters = parameters;
    }
    
    
    /**
     * Gets the parameter as an integer value.
     *
     * @param parameterName -- the parameter name to fetch.
     *
     * @return -1 if the parameter is not defined in the header.
     */
    protected int getParameterAsInt(String parameterName) {
        if (this.getParameterValue(parameterName) != null) {
            try {
                if (this.getParameterValue(parameterName)
                instanceof String) {
                    return Integer.parseInt
                            (this.getParameter(parameterName));
                } else {
                    return
			((Integer)getParameterValue(parameterName)).intValue();
                }
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else return -1;
    }
    
    /**
     * Gets the parameter as an integer when it is entered as a hex.
     *
     * @param parameterName -- The parameter name to fetch.
     *
     * @return -1 if the parameter is not defined in the header.
     */
    protected int getParameterAsHexInt(String parameterName) {
        if (this.getParameterValue(parameterName) != null) {
            try {
                if (this.getParameterValue(parameterName)
                instanceof String) {
                    return Integer.parseInt
                            (this.getParameter(parameterName), 16);
                } else {
                    return
			((Integer)getParameterValue(parameterName)).intValue();
                }
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else return -1;
    }
    
    
    
    
    /**
     * Gets the parameter as a long value.
     *
     * @param parameterName -- the parameter name to fetch.
     *
     * @return -1 if the parameter is not defined or the parameter as a long.
     */
    
    protected long getParameterAsLong(String parameterName) {
        if (this.getParameterValue(parameterName) != null) {
            try {
                if (this.getParameterValue(parameterName)
                instanceof String) {
                    return Long.parseLong
                            (this.getParameter(parameterName));
                } else {
                    return
			((Long)getParameterValue(parameterName)).longValue();
                }
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else return -1;
    }
    
    /**
     * Gets the parameter value as a URI.
     *
     * @param parameterName -- the parameter name
     *
     * @return value of the parameter as a URI or null if the parameter
     * not present.
     */
    protected URI getParameterAsURI(String parameterName) {
        Object val = getParameterValue(parameterName);
        if (val instanceof URI)
            return (URI) val;
        else {
            try {
                return new URI((String)val);
            } catch (ParseException ex) {
                // catch (URISyntaxException ex) {
                return null;
            }
        }
    }
    
    /**
     * Gets the parameter value as a boolean.
     *
     * @param parameterName the parameter name
     * @return boolean value of the parameter.
     */
    protected boolean getParameterAsBoolean(String parameterName) {
        Object val = getParameterValue(parameterName);
        if (val == null) {
            return false;
        } else if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        } else if (val instanceof String) {
            return equalsIgnoreCase((String)val, "true");
        } else return false;
    }
    
    
    /**
     * This is for the benifit of the TCK.
     *
     * @param parameterName the parameter name
     * @return the name value pair for the given parameter name.
     */
    public NameValue getNameValue(String parameterName) {
        return parameters.getNameValue(parameterName);
    }
    
    /**
     * Encodes the contents as a string.
     * @return encoded string of object contents.
     */
    protected abstract String encodeBody();

    /**
     * Encodes the parameters as a string.
     * @return encoded string of object contents.
     */
    protected String encodeWithSep() {
        if (parameters == null) {
            return "";
        } else {
            return parameters.encodeWithSep();
        }
    }
}
