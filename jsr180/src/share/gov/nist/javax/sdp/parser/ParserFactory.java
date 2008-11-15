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
package gov.nist.javax.sdp.parser;
import java.util.*;
import gov.nist.core.*;

/**
 * Factory for creating parsers for the SDP stuff.
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ParserFactory {
    /** Current parser table. */
    private static  Hashtable parserTable;
    /** Parameters for instantiating the parser. */
    private static Class[] constructorArgs;
    /** the PArser classname. */
    private static  
	final String packageName = "gov.nist.javax.sdp.parser";

    /**
     * Factory method to get the parser class.
     * @param parserClass the name of the parser class.
     * @return handle to parser engine
     */
    private static Class getParser(String parserClass) {
	try {
	    return Class.forName(packageName + "." + parserClass);
	} catch (ClassNotFoundException ex) {
	    System.out.println("Could not find class");
	    ex.printStackTrace();
	    System.exit(0);
	    return null; // dummy
	}
    }

    static {
	constructorArgs = new Class[1];
	constructorArgs[0] = new String().getClass();
	parserTable = new Hashtable();
	parserTable.put("a", getParser("AttributeFieldParser"));
	parserTable.put("b", getParser("BandwidthFieldParser"));
	parserTable.put("c", getParser("ConnectionFieldParser"));
	parserTable.put("e", getParser("EmailFieldParser"));
	parserTable.put("i", getParser("InformationFieldParser"));
	parserTable.put("k", getParser("KeyFieldParser"));
	parserTable.put("m", getParser("MediaFieldParser"));
	parserTable.put("o", getParser("OriginFieldParser"));
	parserTable.put("p", getParser("PhoneFieldParser"));
	parserTable.put("v", getParser("ProtoVersionFieldParser"));
	parserTable.put("r", getParser("RepeatFieldParser"));
	parserTable.put("s", getParser("SessionNameFieldParser"));
	parserTable.put("t", getParser("TimeFieldParser"));
	parserTable.put("u", getParser("URIFieldParser"));
	parserTable.put("z", getParser("ZoneFieldParser"));
    }

    /**
     * Factory method to create a parser engine.
     * @param field the name of the next field to parse
     * @return the handle to the SDP parsing engine
     */
    public static  SDPParser 
	createParser(String field) throws ParseException {
	String fieldName = Lexer.getFieldName(field);
	if (fieldName == null)
	    return null;
	Class parserClass = 
	    (Class) parserTable.get(fieldName.toLowerCase());
            
	if (parserClass != null) {
        Exception ex = null;
	    try {
    		SDPParser retval = (SDPParser) parserClass.newInstance();
    		retval.setField(field);
    		return retval;
        } catch (InstantiationException ie) {
            ex = ie;
        } catch (IllegalAccessException iae) {
            ex = iae;
        }
        if (ex != null) {
            // print system message and exit
            InternalErrorHandler.handleException(ex);
            return null;
        }
	} else
	    throw new ParseException
		("Could not find parser for " + fieldName, 0);
    }
                       

}
