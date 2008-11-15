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
 * SessionDescriptionImpl.java
 *
 * Created on January 10, 2002, 3:11 PM
 */

package gov.nist.javax.sdp;

import java.util.*;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
/**
 * Implementation of the SessionDescription interface.
 *
 * 
 * a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class SessionDescriptionImpl {
    /** Descriptive label for current time. */
    private TimeDescriptionImpl currentTimeDescription;
    /** Descriptive lavel for current media. */
    private MediaDescriptionImpl currentMediaDescription;
    /** Protocol version. */
    protected ProtoVersionField versionImpl;
    /** Session originator. */
    protected OriginField originImpl;
    /** Current session name. */
    protected SessionNameField sessionNameImpl;
    /** Descriptive session information. */
    protected InformationField infoImpl;
    /** Current URi. */
    protected URIField uriImpl;
    /** Current connection. */
    protected ConnectionField connectionImpl;
    /** Key field. */
    protected KeyField keyImpl;
    /** Vector of time descriptions. */
    protected Vector timeDescriptions;
    /** Vector of media types. */
    protected Vector mediaDescriptions;
    /** Vector of time zone adjustments. */
    protected Vector zoneAdjustments;
    /** Vector of email addresses. */
    protected Vector emailList;
    /** Vector of phone numbers. */
    protected Vector phoneList;
    /** Vector of bandwidths. */
    protected Vector bandwidthList;
    /** Vector of connection attributes. */
    protected Vector attributesList;
 
    /** Creates new SessionDescriptionImpl */
    public SessionDescriptionImpl() {
	zoneAdjustments = new Vector();
	emailList = new Vector();
	phoneList = new Vector();
	bandwidthList = new Vector();
	timeDescriptions = new Vector();
	mediaDescriptions = new Vector();
	attributesList = new Vector();
 
    }
 
    /**
     * Adds a new SDP field.
     * @param sdpField the new field to be processed
     * @exception ParseException if a parsing error occurs.
     */
    public void addField(SDPField sdpField) throws ParseException {
	try {
	    if (sdpField instanceof ProtoVersionField) {
		versionImpl = (ProtoVersionField)sdpField;
	    } else if (sdpField instanceof OriginField) {
		originImpl = (OriginField) sdpField;
	    } else if (sdpField instanceof SessionNameField) {
		sessionNameImpl = (SessionNameField) sdpField;
	    } else if (sdpField instanceof InformationField) {
		if (currentMediaDescription != null)
		    currentMediaDescription.setInformationField
			((InformationField) sdpField);
		else this.infoImpl = (InformationField) sdpField;
	    } else if (sdpField instanceof URIField) {
		uriImpl = (URIField) sdpField;
	    } else if (sdpField instanceof ConnectionField) {
		if (currentMediaDescription != null)
		    currentMediaDescription.setConnectionField
			((ConnectionField) sdpField);
		else this.connectionImpl = (ConnectionField) sdpField;
	    } else if (sdpField instanceof KeyField) {
		if (currentMediaDescription != null)
		    currentMediaDescription.setKey((KeyField)sdpField);
		else keyImpl = (KeyField) sdpField;
	    } else if (sdpField instanceof EmailField) {
		emailList.addElement(sdpField);
	    } else if (sdpField instanceof PhoneField) {
		phoneList.addElement(sdpField);
	    } else if (sdpField instanceof TimeField) {
		currentTimeDescription = new TimeDescriptionImpl
		    ((TimeField)sdpField);
		timeDescriptions.addElement(currentTimeDescription);
	    } else if (sdpField instanceof RepeatField) {
		if (currentTimeDescription == null) {
		    throw new ParseException("no time specified", 0);
		} else {
		    currentTimeDescription.addRepeatField
			((RepeatField) sdpField);
		}
	    } else if (sdpField instanceof ZoneField) {
		zoneAdjustments.addElement(sdpField);
	    } else if (sdpField instanceof BandwidthField) {
		if (currentMediaDescription != null) {
		    currentMediaDescription.addBandwidthField
			((BandwidthField) sdpField);
		} else {
		    bandwidthList.addElement(sdpField);
		} 
	    } else if (sdpField instanceof AttributeField) {
		if (currentMediaDescription != null) {
		    AttributeField af = (AttributeField) sdpField;
		    String s = af.getName();
		    currentMediaDescription.addAttribute
			((AttributeField) sdpField);
		} else { 
		    attributesList.addElement(sdpField);
		}
	    } else if (sdpField instanceof MediaField) {
		currentMediaDescription = new MediaDescriptionImpl();
		mediaDescriptions.addElement(currentMediaDescription);
		currentMediaDescription.setMediaField((MediaField)sdpField);
	    }
	} catch (SdpException ex) {
	    throw new ParseException(sdpField.encode(), 0);
	}
    }
 
 
    /**
     * Public clone declaration.
     * @throws CloneNotSupportedException if clone method is not supported
     * @return Object
     */
    public Object clone() {
	Class myClass = this.getClass();
	SessionDescriptionImpl hi;
	try {
	    hi = (SessionDescriptionImpl) myClass.newInstance();
	} catch (InstantiationException ex) {
	    return null;
	} catch (IllegalAccessException ex) {
	    return null;
	}
	hi.versionImpl = (ProtoVersionField) this.versionImpl.clone();
	hi.originImpl = (OriginField) this.originImpl.clone();
	hi.sessionNameImpl = (SessionNameField) this.sessionNameImpl.clone();
	hi.infoImpl = (InformationField) this.infoImpl.clone();
	hi.uriImpl = (URIField) this.uriImpl.clone();
	hi.connectionImpl = (ConnectionField) this.connectionImpl.clone();
	hi.keyImpl = (KeyField) this.keyImpl.clone();
	hi.timeDescriptions = cloneVector(this.timeDescriptions);
 
	hi.emailList = cloneVector(this.emailList);
	hi.phoneList = cloneVector(this.phoneList);
	hi.zoneAdjustments = cloneVector(this.zoneAdjustments);
	hi.bandwidthList = cloneVector(this.bandwidthList);
	hi.attributesList = cloneVector(this.attributesList);
	hi.mediaDescriptions = cloneVector(this.mediaDescriptions);
	return hi;
    }
 
    /**
     * Returns the version of SDP in use.
     * This corresponds to the v= field of the SDP data.
     * @return the integer version (-1 if not set).
     */
    public ProtoVersionField getVersion() {
	return versionImpl;
    }
 
    /**
     * Sets the version of SDP in use.
     * This corresponds to the v= field of the SDP data.
     * @param v version - the integer version.
     * @throws SdpException if the version is null
     */
    public void setVersion(ProtoVersionField v)
	throws SdpException {
	if (v == null)
	    throw new SdpException("The parameter is null");
	if (v instanceof ProtoVersionField) {
	    versionImpl = (ProtoVersionField)v;
	} else
	    throw new SdpException
		("The parameter must be an instance of VersionField");
    }
 
    /**
     * Returns information about the originator of the session.
     * This corresponds to the o= field of the SDP data.
     * @return the originator data.
     */
    public OriginField getOrigin() {
	return originImpl;
    }
 
    /**
     * Sets information about the originator of the session.
     * This corresponds to the o= field of the SDP data.
     * @param origin origin - the originator data.
     * @throws SdpException if the origin is null
     */
    public void setOrigin(OriginField origin)
	throws SdpException {
	if (origin == null)
	    throw new SdpException("The parameter is null");
	if (origin instanceof OriginField) {
	    OriginField o = (OriginField)origin;
	    originImpl = o;
	} else
	    throw new SdpException("The parameter must be "
				   + "an instance of OriginField");
    }
 
    /**
     * Returns the name of the session.
     * This corresponds to the s= field of the SDP data.
     * @return the session name.
     */
    public SessionNameField getSessionName() {
	return sessionNameImpl;
    }
 
 
    /**
     * Sets the name of the session.
     * This corresponds to the s= field of the SDP data.
     * @param sessionName name - the session name.
     * @throws SdpException if the sessionName is null
     */
    public void setSessionName(SessionNameField sessionName)
	throws SdpException {
	if (sessionName == null) 
	    throw new SdpException("The parameter is null");
	if (sessionName instanceof SessionNameField) {
	    SessionNameField s = (SessionNameField)sessionName;
	    sessionNameImpl = s;
	} else
	    throw new SdpException("The parameter must be "
				   + "an instance of SessionNameField");
    }
 
    /**
     * Returns value of the info field (i=) of this object.
     * @return info
     */
    public InformationField getInfo() {
	return infoImpl;
    }
 
    /**
     * Sets the i= field of this object.
     * @param i s - new i= value; if null removes the field
     * @throws SdpException if the info is null
     */
    public void setInfo(InformationField i)
	throws SdpException {
	if (i == null)
	    throw new SdpException("The parameter is null");
	if (i instanceof InformationField) {
	    InformationField info = (InformationField)i;
	    infoImpl = info;
	} else 
	    throw new SdpException("The parameter must be "
				   + "an instance of InformationField");
    }
 
    /**
     * Returns a uri to the location of more details about the session.
     * This corresponds to the u=
     * field of the SDP data.
     * @return the uri.
     */
    public URIField getURI() {
	return uriImpl;
    }
 
    /**
     * Sets the uri to the location of more details about the session. This
     * corresponds to the u=
     * field of the SDP data.
     * @param uri uri - the uri.
     * @throws SdpException if the uri is null
     */
    public void setURI(URIField uri)
	throws SdpException {
	if (uri == null)
	    throw new SdpException("The parameter is null");
	if (uri instanceof URIField) {
	    URIField u = (URIField)uri;
	    uriImpl = u;
	} else
	    throw new SdpException
		("The parameter must be an instance of URIField");
    }
 
    /**
     * Returns an email address to contact for further information
     * about the session.
     * This corresponds to the e= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the email address.
     */
    public Vector getEmails(boolean create)
	throws SdpParseException {
	if (emailList == null) {
	    if (create)
		emailList = new Vector();
	}
	return emailList;
    }
 
    /**
     * Sets a an email address to contact for further information
     * about the session.
     * This corresponds to the e= field of the SDP data.
     * @param emails email - the email address.
     * @throws SdpException if the vector is null
     */
    public void setEmails(Vector emails)
	throws SdpException {
	if (emails == null)
	    throw new SdpException("The parameter is null");
	else 
	    emailList = emails;
    }
 
    /**
     * Returns a phone number to contact for further information about
     * the session. This corresponds to the p= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the phone number.
     */
    public Vector getPhones(boolean create)
	throws SdpException {
	if (phoneList == null) {
	    if (create)
		phoneList = new Vector();
	}
	return phoneList;
    }
 
    /**
     * Sets a phone number to contact for further information about
     * the session. This corresponds to the p= field of the SDP data.
     * @param phones phone - the phone number.
     * @throws SdpException if the vector is null
     */
    public void setPhones(Vector phones)
	throws SdpException {
	if (phones == null)
	    throw new SdpException("The parameter is null");
	else
	    phoneList = phones;
    }
 
    /**
     * Returns a TimeField indicating the start, stop, repetition and time zone
     * information of the
     * session. This corresponds to the t= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the Time Field.
     */
    public Vector getTimeDescriptions(boolean create)
	throws SdpException {
	if (timeDescriptions == null) {
	    if (create)
		timeDescriptions = new Vector();
	}
	return timeDescriptions;
    }
 
    /**
     * Sets a TimeField indicating the start, stop, repetition and time zone
     * information of the
     * session. This corresponds to the t= field of the SDP data.
     * @param times time - the TimeField.
     * @throws SdpException if the vector is null
     */
    public void setTimeDescriptions(Vector times)
	throws SdpException {
	if (times == null)
	    throw new SdpException("The parameter is null");
	else {
	    timeDescriptions = times;
	}
    }
 
    /**
     * Returns the time zone adjustments for the Session
     * @param create boolean to set
     * @throws SdpException
     * @return a Hashtable containing the zone adjustments, where the key is the
     * Adjusted Time
     * Zone and the value is the offset.
     */
    public Vector getZoneAdjustments(boolean create)
	throws SdpException {
	if (zoneAdjustments == null) {
	    if (create)
		zoneAdjustments = new Vector();
	}
	return zoneAdjustments;
    }
 
    /**
     * Sets the time zone adjustment for the TimeField.
     * @param zoneAdjustments zoneAdjustments - a Hashtable containing the zone
     * adjustments, where the key
     * is the Adjusted Time Zone and the value is the offset.
     * @throws SdpException if the vector is null
     */
    public void setZoneAdjustments(Vector zoneAdjustments)
	throws SdpException {
	if (zoneAdjustments == null)
	    throw new SdpException("The parameter is null");
	else this.zoneAdjustments = zoneAdjustments;
    }
 
    /**
     * Returns the connection information associated with this object. This may
     * be null for SessionDescriptions if all Media objects have a connection
     * object and may be null
     * for Media objects if the corresponding session connection is non-null.
     * @return connection
     */
    public ConnectionField getConnection() {
	return connectionImpl;
    }
 
    /**
     * Sets the connection data for this entity.
     * @param conn to set
     * @throws SdpException if the parameter is null
     */
    public void setConnection(ConnectionField conn)
	throws SdpException {
	if (conn == null)
	    throw new SdpException("The parameter is null");
	if (conn instanceof ConnectionField) {
	    ConnectionField c = (ConnectionField)conn;
	    connectionImpl = c;
	} else
	    throw new
		SdpException("Bad implementation class ConnectionField");
    }
 
    /**
     * Returns the Bandwidth of the specified type.
     * @param create type - type of the Bandwidth to return
     * @return the Bandwidth or null if undefined
     */
    public Vector getBandwidths(boolean create) {
	if (bandwidthList == null) {
	    if (create)
		bandwidthList = new Vector();
	}
	return bandwidthList;
    }
 
    /**
     * Sets the value of the Bandwidth with the specified type.
     * @param bandwidthList to set
     * @throws SdpException if the vector is null
     */
    public void setBandwidths(Vector bandwidthList)
	throws SdpException {
	if (bandwidthList == null)
	    throw new SdpException("The parameter is null");
	else
	    this.bandwidthList = bandwidthList;
    }
 
    /**
     * Returns the integer value of the specified bandwidth name.
     * @param name name - the name of the bandwidth type
     * @throws SdpParseException
     * @return the value of the named bandwidth
     */
    public int getBandwidth(String name)
	throws SdpParseException {
	if (name == null)
	    return -1;
	else if (bandwidthList == null)
	    return -1;
	for (int i = 0; i < bandwidthList.size(); i++) {
	    Object o = bandwidthList.elementAt(i);
	    if (o instanceof BandwidthField) {
		BandwidthField b = (BandwidthField)o;
		String type = b.getType();
		if (type != null) {
		    if (name.equals(type)) {
			return b.getValue();
		    }
		}
	    }
	}
	return -1;
    }
 
    /**
     * Sets the value of the specified bandwidth type.
     * @param name name - the name of the bandwidth type.
     * @param value value - the value of the named bandwidth type.
     * @throws SdpException if the name is null
     */
    public void setBandwidth(String name,
			     int value)
	throws SdpException {
	if (name == null)
	    throw new SdpException("The parameter is null");
	else
	    if (bandwidthList != null) {
		for (int i = 0; i < bandwidthList.size(); i++) {
		    Object o = bandwidthList.elementAt(i);
		    if (o instanceof BandwidthField) {
			BandwidthField b = (BandwidthField)o;
			String type = b.getType();
			if (type != null) {
			    if (name.equals(type)) {
				b.setValue(value);
			    }
			}
		    }
		}
	    }
    }
 
    /**
     * Removes the specified bandwidth type.
     * @param name name - the name of the bandwidth type
     */
    public void removeBandwidth(String name) {
	if (name != null)
	    if (bandwidthList != null) {
		for (int i = 0; i < bandwidthList.size(); i++) {
		    Object o = bandwidthList.elementAt(i);
		    if (o instanceof BandwidthField) {
			BandwidthField b = (BandwidthField)o;
			try {
			    String type = b.getType();
			    if (type != null) {
				if (name.equals(type)) {
				    bandwidthList.removeElement(b);
				}
			    }
			}
			catch (SdpParseException e) {}
		    }
		}
	    }
    }
 
    /**
     * Returns the key data.
     * @return key
     */
    public KeyField getKey() {
	return keyImpl;
    }
 
    /**
     * Sets encryption key information.
     * This consists of a method and an encryption key included inline.
     * @param key key - the encryption key data; depending on method may be null
     * @throws SdpException if the parameter is null
     */
    public void setKey(KeyField key)
	throws SdpException {
	if (key == null)
	    throw new SdpException("The parameter is null");
	if (key instanceof KeyField) {
	    KeyField k = (KeyField)key;
	    keyImpl = k;
	} else 
	    throw new
		SdpException("The parameter must be an instance of KeyField");
    }
 
    /**
     * Returns the value of the specified attribute.
     * @param name name - the name of the attribute
     * @throws SdpParseException
     * @return the value of the named attribute
     */
    public String getAttribute(String name)
	throws SdpParseException {
	if (name == null)
	    return null;
	else if (attributesList == null)
	    return null;
	for (int i = 0; i < attributesList.size(); i++) {
	    Object o = attributesList.elementAt(i);
	    if (o instanceof AttributeField) {
		AttributeField a = (AttributeField)o;
		String n = a.getName();
		if (n != null) {
		    if (name.equals(n)) {
			return a.getValue();
		    }
		}
	    }
	}
	return null;
    }
 
    /**
     * Returns the set of attributes for this Description as a Vector
     * of Attribute objects in the order they were parsed.
     * @param create create - specifies whether to return null or a new empty
     * Vector in case no
     * attributes exists for this Description
     * @return attributes for this Description
     */
    public Vector getAttributes(boolean create) {
	if (attributesList == null) {
	    if (create)
		attributesList = new Vector();
	}
	return attributesList;
    }
 
    /**
     * Removes the attribute specified by the value parameter.
     * @param name name - the name of the attribute
     */
    public void removeAttribute(String name) {
	if (name != null)
	    if (attributesList != null) {
		for (int i = 0; i < attributesList.size(); i++) {
		    Object o = attributesList.elementAt(i);
		    if (o instanceof AttributeField) {
			AttributeField a = (AttributeField)o;
			try {
			    String n = a.getName();
			    if (n != null) {
				if (name.equals(n)) {
				    attributesList.removeElement(a);
				}
			    }
			}
			catch (SdpParseException e) {}
 
		    }
		}
	    }
    }
 
    /**
     * Sets the value of the specified attribute.
     * @param name name - the name of the attribute.
     * @param value value - the value of the named attribute.
     * @throws SdpException if the name or the value is null
     */
    public void setAttribute(String name,
			     String value)
	throws SdpException {
	if (name == null || value == null)
	    throw new SdpException("The parameter is null");
	else
	    if (attributesList != null) {
		for (int i = 0; i < attributesList.size(); i++) {
		    Object o = attributesList.elementAt(i);
		    if (o instanceof AttributeField) {
			AttributeField a = (AttributeField)o;
			String n = a.getName();
			if (n != null) {
			    if (name.equals(n)) {
				a.setValue(value);
			    }
			}
		    }
		}
	    }
    }
 
    /**
     * Adds the specified Attribute to this Description object.
     * @param attributes the attributes to add
     * @throws SdpException if the vector is null
     */
    public void setAttributes(Vector attributes)
	throws SdpException {
	if (attributes == null)
	    throw new SdpException("The parameter is null");
	else attributesList = attributes;
    }
 
    /**
     * Adds a MediaDescription to the session description.
     * These correspond to the m=
     * fields of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return media - the field to add.
     */
    public Vector getMediaDescriptions(boolean create)
	throws SdpException {
	if (mediaDescriptions == null) {
	    if (create)
		mediaDescriptions = new Vector();
	}
	return mediaDescriptions;
    }
 
    /**
     * Removes all MediaDescriptions from the session description.
     * @param mediaDescriptions to set
     * @throws SdpException if the parameter is null
     */
    public void setMediaDescriptions(Vector mediaDescriptions)
	throws SdpException {
	if (mediaDescriptions == null)
	    throw new SdpException("The parameter is null");
	else this.mediaDescriptions = mediaDescriptions;
    }

    /** 
     * Returns an encoded string of vector contents.
     * @param vector the objects to process
     * @return encode string of vector contents
     */ 
    private String encodeVector(Vector vector) {
	StringBuffer encBuff = new StringBuffer();
 
	for (int i = 0; i < vector.size(); i++)
	    encBuff.append(vector.elementAt(i));
 
	return encBuff.toString();
    }
 

    /**
     * Utility method for cloning a Vector 
     * Acknowledgement - this code was contributed by Sverker Abrahamsson.
     * @param v the vector to be copied
     * @return the cloned vector
     */
    private Vector cloneVector(Vector v) {
	Vector clone = new Vector(v.capacity());

	int size = v.size();
	for (int i = 0; i < size; i++) {
	    clone.setElementAt(v.elementAt(i), i);
	}
	return clone;
    }
 
    /**
     * Returns the canonical string representation of the
     * current SessionDescrption. Acknowledgement - this code
     *
     * @return Returns the canonical string representation
     * of the current SessionDescrption.
     */
    public String toString() {
	StringBuffer encBuff = new StringBuffer();
 
	// Encode single attributes
	encBuff.append(getVersion() == null ? "" : getVersion().toString());
	encBuff.append(getOrigin() == null ? "" : getOrigin().toString());
	encBuff.append(getSessionName() == null ? "" : getSessionName().toString
		       ());
	encBuff.append(getInfo() == null ? "" : getInfo().toString());
 
	// Encode attribute vectors
	try {
	    encBuff.append(getURI() == null ? "" : getURI().toString());
	    encBuff.append(encodeVector(getEmails(true)));
	    encBuff.append(encodeVector(getPhones(true)));
	    encBuff.append(getConnection() == null ? "" :
			   getConnection().toString
			   ());
	    encBuff.append(encodeVector(getBandwidths(true)));
	    encBuff.append(encodeVector(getTimeDescriptions(true)));
	    encBuff.append(encodeVector(getZoneAdjustments(true)));
	    encBuff.append(getKey() == null ? "" : getKey().toString());
	    encBuff.append(encodeVector(getAttributes(true)));
	    encBuff.append(encodeVector(getMediaDescriptions(true)));
	    // adds the final crlf
	}
	catch (SdpException exc) {
	    // add exception handling if necessary
	}
	return encBuff.toString();
    }
 
}
