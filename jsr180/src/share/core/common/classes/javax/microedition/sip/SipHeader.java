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

package javax.microedition.sip;

import java.util.Vector;

import gov.nist.core.NameValueList;
import gov.nist.core.ParseException;
import gov.nist.microedition.sip.StackConnector;
import gov.nist.siplite.header.Header;
import gov.nist.siplite.header.ParametersHeader;
import gov.nist.siplite.header.ExtensionHeader;
import gov.nist.siplite.parser.ExtensionParser;
import gov.nist.siplite.parser.Lexer;

/**
 * SipHeader provides generic SIP header parser helper. This class can be used
 * to parse bare String header values that are read from SIP message using e.g.
 * SipConnection.getHeader() method. It should be noticed that SipHeader
 * is separate helper class and not mandatory to use for creating SIP
 * connections.
 * @see JSR180 spec, v 1.0.1, p 47-51
 *
 */
public class SipHeader {
    /**
     * The nist-siplite corresponding header.
     */
    private ExtensionHeader header = null;

    /**
     * Constructs a SipHeader from name value pair. For example:
     * <pre>
     * name = Contact
     * value = &lt;sip:UserB@192.168.200.201&gt;;expires=3600
     * </pre>
     * @param name name of the header (Contact, Call-ID, ...)
     * @param value full header value as String
     * @throws NullPointerException if name is null 
     * @throws IllegalArgumentException if the header value or
     * name are invalid
     */
    public SipHeader(String name, String value) {
        if (name != null) {
            // trim() will cut control characters, so at first we have to check
            // that the header's name doesn't contain them.
            if ((name.indexOf('\n') != -1) || (name.indexOf('\r') != -1)) {
                throw new IllegalArgumentException("'" + name +
                    "' contains control character(s).");
            }

            name = name.trim();
        } else {
            throw new NullPointerException("Header name is null");
        }

        // Validating the name.
        if (!Lexer.isValidName(name)) {
            throw new IllegalArgumentException("Invalid header's name: '" +
                name + "'");
        }

        // Validating the value.
        if (value != null) {
            value = value.trim();
        } else {
            value = "";
        }

        try {
            if (Header.isAuthorization(name)) {
                Header h = StackConnector.headerFactory.
                    createHeader(name, value);
                NameValueList authParamList = h.getParameters();
                String authVal = h.getValue().toString();

                header = new ExtensionHeader(name, value, authVal);
                header.setParameters(authParamList);
            } else {
                ExtensionParser ep = new ExtensionParser(name + ":" + value);
                header = (ExtensionHeader)ep.parse();
            }
        } catch (ParseException pe) {
            throw new IllegalArgumentException(pe.getMessage());
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException(npe.getMessage());
        }
    }

    /**
     * Sets the header name, for example Contact
     * @param name Header name
     * @throws NullPointerException if name is null 
     * @throws IllegalArgumentException if the name is invalid
     */
    public void setName(java.lang.String name) {
        if (name != null) {
            name = name.trim();
        } else {
            throw new NullPointerException("Header name is null");
        }

        // Validating the name.
        if (!Lexer.isValidName(name)) {
            throw new IllegalArgumentException("Invalid name: '" + name + "'");
        }

        header.setHeaderName(name);
    }

    /**
     * Returns the name of this header
     * @return the name of this header as String
     */
    public java.lang.String getName() {
        return header.getName();
    }

    /**
     * Returns the header value without header parameters.
     * For example for header
     * &lt;sip:UserB@192.168.200.201&gt;;expires=3600 method
     * returns &lt;sip:UserB@192.168.200.201&gt;
     * In the case of an authorization or authentication header getValue()
     * returns only the authentication scheme e.g. Digest.
     * @return header value without header parameters.
     * It is an empty String if the value was set to be null or empty.
     */
    public java.lang.String getValue() {
        String name  = header.getName();
        String value = header.getValue().toString();

        if (Header.isAuthorization(name)) {
            value = value.trim();
            int end = value.indexOf(' ');

            if (end == -1) {
                end = value.length();
            }

            return value.substring(0, end);
        } else {
            return value;
        }
    }

    /**
     * Returns the full header value including parameters.
     * For example Alice &lt;sip:alice@atlanta.com&gt;;tag=1928301774
     * @return full header value including parameters
     */
    public java.lang.String getHeaderValue() {
        return header.getHeaderValue();
    }

    /**
     * Sets the header value as String without parameters.
     * For example &lt;sip:UserB@192.168.200.201&gt;.
     * The existing (if any) header parameter values are not modified.
     * For the authorization and authentication header this method sets
     * the authentication scheme e.g. Digest.
     * @param value the header value
     * @throws IllegalArgumentException if the value is invalid or there is
     * parameters included.
     */
    public void setValue(java.lang.String value) {
        if (value == null) {
            value = "";
        }

        // Validating the value.
        if (!Lexer.isValidHeaderValue(value)) {
            throw new IllegalArgumentException("Invalid header's value.");
        }

        String name = header.getName();

        if (Header.isAuthorization(name)) {
            // Authorization headers need additional validation.
            // An existing parser is used to check the validity of the value.
            try {
                Header h = StackConnector.headerFactory.
                    createHeader(name, value);
                if (((ParametersHeader)h).hasParameters()) {
                    throw new IllegalArgumentException("Value must not " +
                        "contain parameters.");
                }
            } catch (ParseException pe) {
                throw new IllegalArgumentException(pe.getMessage());
            }
        }

        header.setHeaderValue(value);
    }

    /**
     * Returns the value of one header parameter.
     * For example, from value &lt;sip:UserB@192.168.200.201&gt;;expires=3600
     * the method call getParameter(expires) will return 3600.
     * @param name name of the header parameter
     * @return value of header parameter. returns empty string for a parameter
     * without value and null if the parameter does not exist.
     * @throws NullPointerException if name is null 
     */
    public java.lang.String getParameter(java.lang.String name) {
        if (name == null) {
            throw new NullPointerException("Header name is null");
        }
        NameValueList parameterList = header.getParameters();       
        if (parameterList == null) {
            return null;
        }

        name = name.trim();

        return parameterList.getParameter(name);
    }

    /**
     * Returns the names of header parameters. Returns null if there are no
     * header parameters.
     * @return names of the header parameters. Returns null if there are
     * no parameters.
     */
    public java.lang.String[] getParameterNames() {
        NameValueList parameterList = header.getParameters();

        if (parameterList == null || parameterList.size() == 0) {
            return null;
        }

        Vector parameterNameList = parameterList.getNames();
        String parameterNames[] = new String[parameterNameList.size()];

        for (int i = 0; i < parameterList.size(); i++)
            parameterNames[i] = (String)parameterNameList.elementAt(i);

        return parameterNames;
    }

    /**
     * Sets value of header parameter. If parameter does not exist it
     * will be added.
     * For example, for header value &lt;sip:UserB@192.168.200.201&gt;
     * calling setParameter(expires, 3600) will construct header value
     * &lt;sip:UserB@192.168.200.201&gt;;expires=3600.
     * If the value is null, the parameter is interpreted as a parameter
     * without value.
     * @param name name of the header parameter
     * @param value value of the parameter
     * @throws NullPointerException if name is null 
     * @throws IllegalArgumentException if the parameter name or
     * value are invalid
     */
    public void setParameter(java.lang.String name, java.lang.String value) {
        NameValueList parameterList = header.getParameters();

        // Validating the name.
        if (name != null) {
            name = name.trim();
        } else {
            throw new NullPointerException("Header name is null");
        }

        if (!Lexer.isValidName(name)) {
            throw new IllegalArgumentException("Invalid parameter's name.");
        }

        // Validating the value.
        if (value != null) {
            value = value.trim();
        }

        if (!Lexer.isValidParameterValue(value)) {
            throw new IllegalArgumentException("Invalid parameter's value.");
        }

        // If the value is null, the parameter is interpreted as a parameter
        // without value.
        if (value == null) {
            value = "";
        }

        header.setParameter(name, value);
    }

    /**
     * Removes the header parameter, if it is found in this header.
     * @param name name of the header parameter
     * @throws NullPointerException if name is null 
     */
    public void removeParameter(java.lang.String name) {
        if (name == null) {
            throw new NullPointerException("Header name is null");
        }
        NameValueList parameterList = header.getParameters();

        if (parameterList != null && name != null) {
            parameterList.delete(name);
        }
    }

    /**
     * Returns the String representation of the header according to
     * header type.
     * @return encoded string of object contents
     */
    public java.lang.String toString() {
        return header.toString();
    }
}
