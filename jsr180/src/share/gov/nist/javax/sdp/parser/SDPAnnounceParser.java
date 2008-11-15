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
import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.*;
/**
 * Announcement parser.
 */
public class SDPAnnounceParser extends ParserCore {
    /** Current lexer engine. */
    protected Lexer lexer;
    /** The SDP message to be parsed. */
    protected Vector sdpMessage;

    
    /**
     * Creates new SDPAnnounceParser.
     * @param sdpMessage Vector of messages to parse.
     */
    public SDPAnnounceParser(Vector sdpMessage) {
        this.sdpMessage = sdpMessage;
	
    }

    /**
     * Creates a new SDPAnnounceParser.
     * @param sdpAnnounce message containing the sdp announce message.
     */
    public SDPAnnounceParser(String sdpAnnounce) {
	sdpMessage = new Vector();
	int start = 0;
	if (sdpAnnounce == null)
	    return;
	while (start < sdpAnnounce.length()) {
	    int add = 0;
	    int index = sdpAnnounce.indexOf("\n", start);
	    if (index == -1) break;
	    if (sdpAnnounce.charAt(index - 1) == '\r') {
    		index = index - 1;
    		add = 1;
	    }
	    String line = sdpAnnounce.substring(start, index);
	    start = index + 1 + add;
  	    sdpMessage.addElement(line);
	}
	  	
    }

    /**
     * Parses the session description field.
     * @exception ParseException if a parsing error occurs
     * @return the parsed session description field
     */
    public SessionDescriptionImpl parse()  throws ParseException {
	SessionDescriptionImpl retval = new SessionDescriptionImpl();
	for (int i = 0; i <  sdpMessage.size(); i++) {
	    String field = (String) sdpMessage.elementAt(i);
	    SDPParser sdpParser = ParserFactory.createParser(field);
	    SDPField sdpField = sdpParser.parse();
	    retval.addField(sdpField);
	}
	return retval;

    }

}
