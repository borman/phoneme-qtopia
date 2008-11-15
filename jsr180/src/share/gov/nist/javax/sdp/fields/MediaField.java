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
package gov.nist.javax.sdp.fields;
import gov.nist.javax.sdp.*;
import gov.nist.core.*;
import java.util.*;
/**
 * Media field SDP header.
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class MediaField extends SDPField {
    /** Type of media. */
    protected String media;
    /** Current port number. */
    protected int port;
    /** Number of ports. */
    protected int nports;
    /** Protocol used. */
    protected String proto;
    /** Media formats. */
    protected Vector formats;
 
    /**
     * Copies the current instance.
     * @return the copy of this object
     */
    public Object clone() {
	MediaField retval = new MediaField();
	retval.media = media;
	retval.port = port;
	retval.nports = nports;
	retval.proto = proto;
	for (int i = 0; i < formats.size(); i++) {
	    retval.formats.addElement(formats.elementAt(i));
	}
	return retval;
    }

    /** Default constructor. */
    public MediaField() {
	super(SDPFieldNames.MEDIA_FIELD); 
	formats = new Vector();
    }

    /**
     * Gets the media type.
     * @return the media type
     */
    public String getMedia() {
	return media;
    } 

    /**
     * Gets the port information.
     * @return the current port number
     */
    public int getPort() {
	return port;
    } 

    /**
     * Gets the number of ports.
     * @return the number of ports
     */
    public int getNports() {
	return nports;
    } 

    /**
     * Gets the protocl information.
     * @return the protocol
     */
    public String getProto() {
	return proto;
    } 

    /**
     * Gets the vector of media formats
     * @return the formats
     */
    public Vector getFormats() {
	return formats;
    } 

    /**
     * Sets the media member.
     * @param m the new media type
     */
    public void setMedia(String m) {
	media = m;
    } 

    /**
     * Sets the port member.
     * @param p the new port number
     */
    public void setPort(int p) {
	port = p;
    } 

    /**
     * Sets the number of ports member.
     * @param n the new number of ports
     */
    public void setNports(int n) {
	nports = n;
    } 

    /**
     * Sets the protocol type member.
     * @param p the protocol type
     */
    public void setProto(String p) {
	proto = p;
    } 

    /**
     * Sets the formats member.
     * @param formats the new vector of media formats
     */
    public void setFormats(Vector formats) {
	this.formats = formats;
    } 

    /** 
     * Returns the type (audio,video etc) of the 
     * media defined by this description.
     * @throws SdpParseException if a parsing erro occurs
     * @return the string media type.
     */ 
    public String getMediaType()
	throws SdpParseException {
	return getMedia();
    }
 
    /**
     * Sets the type (audio,video etc) of the media defined by this description.
     * @param mediaType to set
     * @throws SdpException if mediaType is null
     */ 
    public void setMediaType(String mediaType)
	throws SdpException {
	if (mediaType == null)
	    throw new SdpException("The mediaType is null");
	else setMedia(mediaType); 
    }
 
    /**
     * Returns the port of the media defined by this description
     * @throws SdpParseException if a parsing error occurs
     * @return the integer media port.
     */ 
    public int getMediaPort()
	throws SdpParseException {
	return getPort();
    }
 
    /**
     * Sets the port of the media defined by this description
     * @param port to set
     * @throws SdpException if th eport is null
     */ 
    public void setMediaPort(int port)
	throws SdpException {
	if (port < 0)
	    throw new SdpException("The port is < 0");
	else setPort(port); 
    }
 
    /**
     * Returns the number of ports associated with this media description
     * @throws SdpParseException if a parsing error occurs
     * @return the integer port count.
     */ 
    public int getPortCount()
	throws SdpParseException {
	return getNports();
    }
 
    /** 
     * Sets the number of ports associated with this media description.
     * @param portCount portCount - the integer port count.
     * @throws SdpException if the count is less than zero
     */ 
    public void setPortCount(int portCount)
	throws SdpException {
	if (portCount < 0)
	    throw new SdpException("The port count is < 0");
	else setNports(portCount);
    }
 
    /**
     * Returns the protocol over which this media should be transmitted.
     * @throws SdpParseException if a parsing error occurs
     * @return the String protocol, e.g. RTP/AVP.
     */ 
    public String getProtocol()
	throws SdpParseException {
	return getProto();
    }
 
    /**
     * Sets the protocol over which this media should be transmitted.
     * @param protocol - the String protocol, e.g. RTP/AVP.
     * @throws SdpException if the protocol is null
     */ 
    public void setProtocol(String protocol)
	throws SdpException {
	if (protocol == null)
	    throw new SdpException("The protocol is null");
	else setProto(protocol);
    }
 
    /**
     * Returns an Vector of the media formats supported by this description.
     * Each element in this Vector will be an String value which matches one of
     * the a=rtpmap: attribute fields of the media description.
     * @param create to set
     * @throws SdpException
     * @return the Vector.
     */ 
    public Vector getMediaFormats(boolean create)
	throws SdpParseException {
 
	if (!create && formats.size() == 0) 
	    return null;
	else return formats;
    }
 
    /**
     * Adds a media format to the media description.
     * Each element in this Vector should be an String value which
     * matches one of the
     * a=rtpmap: attribute fields of the media description.
     * @param mediaFormats the format to add.
     * @throws SdpException if the vector is null
     */ 
    public void setMediaFormats(Vector mediaFormats)
	throws SdpException {
	if (mediaFormats == null)
	    throw new SdpException("The mediaFormats is null");
	this.formats = mediaFormats;
    }

    /** 
     * Gets the string encoded version of the media formats.
     * @return encoded string of media formats contents
     */
    private String encodeFormats() {
	String retval = "";
	for (int i = 0; i < formats.size(); i++) {
	    retval += formats.elementAt(i);
	    if (i < formats.size() -1) 
		retval += Separators.SP;
	}
	return retval;
    }

    /**
     * Gets the string encoded version of this object.
     * @return encoded string of instance contents
     * @since v1.0
     */
    public String encode() {
	String encoded_string;
	encoded_string = MEDIA_FIELD;
	if (media != null) encoded_string += media + Separators.SP + port;
	// Leave out the nports parameter as this confuses the messenger.
	if (nports > 1) encoded_string += Separators.SLASH + nports; 

	if (proto != null) encoded_string += Separators.SP + proto;
 
	if (formats != null) 
	    encoded_string += Separators.SP + encodeFormats(); 

	encoded_string += Separators.NEWLINE;
	return encoded_string;
    }
}
