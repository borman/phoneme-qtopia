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
 * AttributeFieldParser.java
 *
 * Created on February 19, 2002, 10:09 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;

/**
 * Attribute field parser.
 * @version 1.0
 */
public class AttributeFieldParser extends SDPParser {
    /**
     * Creates new AttributeFieldParser.
     * @param attributeField string to be parsed
     */
    public AttributeFieldParser(String attributeField) {
       
        this.lexer = new Lexer("charLexer", attributeField);
    }

    /** Default constructor. */
    protected AttributeFieldParser() {

    }


    /**
     * Perform the attribute field parsing
     * @return the parsed attribute field
     * @exception ParseException if a parsing error occurs
     */
    public AttributeField attributeField() throws ParseException  {
	    AttributeField attributeField = new AttributeField();
			
	    this.lexer.match('a');
          
	    this.lexer.SPorHT();
	    this.lexer.match('=');
          
	    this.lexer.SPorHT();
                
                  
	    NameValue nameValue = new NameValue();

	    int ptr =   this.lexer.markInputPosition();
	    try {
		String name = lexer.getNextToken(':');
		this.lexer.consume(1);
		String value = lexer.getRest();
		nameValue = new NameValue
		    (name.trim(), value.trim());
	    } catch (ParseException ex) {
		this.lexer.rewindInputPosition(ptr);
		String rest = this.lexer.getRest();
		if (rest == null) 
		    throw new ParseException(this.lexer.getBuffer(),
					     this.lexer.getPtr());
		    nameValue = new NameValue(rest.trim(), null);
	    }
	    attributeField.setAttribute(nameValue);
               
	    this.lexer.SPorHT();
               
	    return attributeField;
    }

    /**
     * Perform the attribute field parsing
     * @return the parsed attribute field
     * @exception ParseException if a parsing error occurs
     */
    public  SDPField parse() throws ParseException {
        return this.attributeField();
    }

}
