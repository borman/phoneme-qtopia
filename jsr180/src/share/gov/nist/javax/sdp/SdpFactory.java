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
package gov.nist.javax.sdp;

import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.parser.*;
import java.util.*;
import gov.nist.core.*;

/**
 * The SdpFactory enables applications to encode and decode SDP messages.
 * The SdpFactory can be used to construct a SessionDescription 
 * object programmatically. 
 * The SdpFactory can also be used to construct a 
 * SessionDescription based on the
 * contents of a String.
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 *
 */
public class SdpFactory extends Object {
    /** Session description. */
    protected static Vector sessionDescriptionsList;
 
    /** Creates new SdpFactory */
    public SdpFactory() {
	sessionDescriptionsList = new Vector();
    }
 
    /**
     * Obtains an instance of an SdpFactory.
     * <p>
     * This static method returns a factory instance.
     * <p>
     * Once an application has obtained a reference to an SdpFactory it can use
     * the factory to
     * configure and obtain parser instances and to create SDP objects.
     * @throws SdpException if a factory can not be accessed
     * @return a factory instance
     */ 
    public static SdpFactory getInstance()
	throws SdpException {
	return new SdpFactory();
    }
 
    /**
     * Creates a new, empty SessionDescription. The session is set as follows:
     * <pre>
     * v=0
     *
     * o=this.createOrigin ("user",
     * InetAddress.getLocalHost().toString());
     *
     * s=-
     *
     * t=0 0
     * </pre>
     * @throws SdpException  if there is a problem
     * constructing the SessionDescription.
     * @return a new, empty SessionDescription.
     */ 
    public SessionDescriptionImpl createSessionDescription()
	throws SdpException {
	SessionDescriptionImpl 
	    sessionDescriptionImpl = new SessionDescriptionImpl();
 
	ProtoVersionField ProtoVersionField = new ProtoVersionField();
	ProtoVersionField.setVersion(0);
	sessionDescriptionImpl.setVersion(ProtoVersionField);
 
	OriginField originImpl = null;
	originImpl = (OriginField)this.createOrigin("user",
						    "127.0.0.1");
	sessionDescriptionImpl.setOrigin(originImpl);
 
	SessionNameField sessionNameImpl = new SessionNameField();
	sessionNameImpl.setValue("-");
	sessionDescriptionImpl.setSessionName(sessionNameImpl);
 
	TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
	TimeField timeImpl = new TimeField();
	timeImpl.setZero();
	timeDescriptionImpl.setTime(timeImpl);
	Vector times = new Vector();
	times.addElement(timeDescriptionImpl);
	sessionDescriptionImpl.setTimeDescriptions(times);
 
	sessionDescriptionsList.addElement(sessionDescriptionImpl);
	return sessionDescriptionImpl;
    }
 
    /**
     * Creates a SessionDescription populated with the information
     * contained within the string parameter.
     * <p>
     * Note: unknown field types should not cause exceptions.
     * @param s s - the sdp message that is to be parsed.
     * @throws SdpParseException  if there is a
     * problem parsing the String.
     * @return a populated SessionDescription object.
     */ 
    public SessionDescriptionImpl createSessionDescription(String s)
	throws SdpParseException {
	try {
 
	    SDPAnnounceParser sdpParser = new SDPAnnounceParser(s);
	    return sdpParser.parse();
	} catch (ParseException e) {
	    e.printStackTrace();
	    throw new SdpParseException(0, 0, "Could not parse message");
	}
    }
 
    /**
     * Returns Bandwidth object with the specified values.
     * @param modifier  the bandwidth type
     * @param value the bandwidth value measured in kilobits per second
     * @return bandwidth
     */ 
    public BandwidthField createBandwidth(String modifier,
					  int value) {
	BandwidthField bandWidthImpl = new BandwidthField();
	try {
 
	    bandWidthImpl.setType(modifier);
	    bandWidthImpl.setValue(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return bandWidthImpl;
    }
 
    /**
     * Returns Attribute object with the specified values.
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return the Attribute
     */ 
    public AttributeField createAttribute(String name,
					  String value) {
	AttributeField attributeImpl = new AttributeField();
	try {
 
	    attributeImpl.setName(name);
	    attributeImpl.setValue(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return attributeImpl;
    }
 
    /**
     * Returns Info object with the specified value.
     * @param value the string containing the description.
     * @return Info
     */ 
    public InformationField createInfo(String value) {
	InformationField infoImpl = new InformationField();
	try {
 
	    infoImpl.setValue(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return infoImpl;
    }
 
    /**
     * Returns Phone object with the specified value.
     * @param value the string containing the description.
     * @return Phone
     */ 
    public PhoneField createPhone(String value) {
	PhoneField phoneImpl = new PhoneField();
	try {
 
	    phoneImpl.setValue(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return phoneImpl;
    }
 
    /**
     * Returns EMail object with the specified value.
     * @param value the string containing the description.
     * @return EMail
     */ 
    public EmailField createEMail(String value) {
	EmailField emailImpl = new EmailField();
	try {
 
	    emailImpl.setValue(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return emailImpl;
    }
 
    /**
     * Returns URI object with the specified value.
     * @param value the URL containing the description.
     * @throws SdpException
     * @return URI
     */ 
    public URIField createURI(String value)
	throws SdpException {
	URIField uriImpl = new URIField();
	uriImpl.setURI(value);
	return uriImpl;
 
    }
 
    /**
     * Returns SessionName object with the specified name.
     * @param name the string containing the name of the session.
     * @return SessionName
     */ 
    public SessionNameField createSessionName(String name) {
	SessionNameField sessionNameImpl = new SessionNameField();
	try {
 
	    sessionNameImpl.setValue(name);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	}
	return sessionNameImpl;
    }
 
    /**
     * Returns Key object with the specified value.
     * @param method the string containing the method type.
     * @param key the key to set
     * @return Key
     */ 
    public KeyField createKey(String method,
			      String key) {
	KeyField keyImpl = new KeyField();
	try {
 
	    keyImpl.setMethod(method);
	    keyImpl.setKey(key);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	    return null;
	}
	return keyImpl;
    }
 
    /**
     * Returns Version object with the specified values.
     * @param value the version number.
     * @return Version
     */ 
    public ProtoVersionField createVersion(int value) {
	ProtoVersionField protoVersionField = new ProtoVersionField();
	try {
 
	    protoVersionField.setVersion(value);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	    return null;
	}
	return protoVersionField;
    }
 
    /**
     * Returns Media object with the specified properties.
     * @param media the media type, eg "audio"
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes vector to set
     * @throws SdpException
     * @return Media
     */ 
    public MediaField createMedia(String media,
				  int port,
				  int numPorts,
				  String transport,
				  Vector staticRtpAvpTypes)
	throws SdpException {
	MediaField mediaImpl = new MediaField();
	mediaImpl.setMediaType(media);
	mediaImpl.setMediaPort(port);
	mediaImpl.setPortCount(numPorts);
	mediaImpl.setProtocol(transport);
	mediaImpl.setMediaFormats(staticRtpAvpTypes);
	return mediaImpl;
    }
 
    /**
     * Returns Origin object with the specified properties.
     * @param userName the user name.
     * @param address the IP4 encoded address.
     * @throws SdpException if the parameters are null
     * @return Origin
     */ 
    public OriginField createOrigin(String userName,
				    String address)
	throws SdpException {
	OriginField originImpl = new OriginField();
	originImpl.setUsername(userName);
	originImpl.setAddress(address);
	// need revisit - originImpl.setNetworkType(SDPKeywords.IN);
	originImpl.setAddressType(SDPKeywords.IPV4);
	return originImpl;
    }
 
    /**
     * Returns Origin object with the specified properties.
     * @param userName String containing the user that created the
     * string.
     * @param sessionId long containing the session identifier.
     * @param sessionVersion long containing the session version.
     * @param networkType String network type for the origin (usually
     * "IN").
     * @param addrType String address type (usually "IP4").
     * @param address String IP address usually the address of the
     * host.
     * @throws SdpException if the parameters are null
     * @return Origin object with the specified properties.
     */ 
    public OriginField createOrigin(String userName,
				    long sessionId,
				    long sessionVersion,
				    String networkType,
				    String addrType,
				    String address)
	throws SdpException {
	OriginField originImpl = new OriginField();
	originImpl.setUsername(userName);
	originImpl.setAddress(address);
	originImpl.setSessionId(sessionId);
	originImpl.setSessionVersion(sessionVersion);
	originImpl.setAddressType(addrType);
	originImpl.setNetworkType(networkType);
	return originImpl; 
    }
 
    /**
     * Returns MediaDescription object with the specified properties.
     * The returned object will respond to
     * Media.getMediaFormats(boolean) with a Vector of media formats.
     * @param media media -
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes list of static RTP/AVP media payload
     * types which should be specified by the
     * returned MediaDescription throws IllegalArgumentException if passed
     * an invalid RTP/AVP payload type
     * @throws IllegalArgumentException
     * @throws SdpException
     * @return MediaDescription
     */ 
    public MediaDescriptionImpl createMediaDescription(String media,
						       int port,
						       int numPorts,
						       String transport,
						       int[] staticRtpAvpTypes)
	throws IllegalArgumentException,
	       SdpException {
	MediaDescriptionImpl 
	    mediaDescriptionImpl = new MediaDescriptionImpl();
	MediaField mediaImpl = new MediaField();
	mediaImpl.setMediaType(media);
	mediaImpl.setMediaPort(port);
	mediaImpl.setPortCount(numPorts);
	mediaImpl.setProtocol(transport);
	mediaDescriptionImpl.setMedia(mediaImpl);
	return mediaDescriptionImpl; 
    }
 
    /**
     * Returns MediaDescription object with the specified properties.
     * The returned object will respond to
     * Media.getMediaFormats(boolean) with a Vector of String objects
     * specified by the 'formats argument.
     * @param media the media type, eg "audio"
     * @param port port number on which to receive media
     * @param numPorts number of ports used for this media stream
     * @param transport transport type, eg "RTP/AVP"
     * @param formats list of formats which should be specified by the
     * returned MediaDescription
     * @return MediaDescription
     */ 
    public MediaDescriptionImpl createMediaDescription(String media,
						       int port,
						       int numPorts,
						       String transport,
						       String[] formats) {
	MediaDescriptionImpl mediaDescriptionImpl = new MediaDescriptionImpl();
	try {
 
	    MediaField mediaImpl = new MediaField();
	    mediaImpl.setMediaType(media);
	    mediaImpl.setMediaPort(port);
	    mediaImpl.setPortCount(numPorts);
	    mediaImpl.setProtocol(transport);
 
	    Vector formatsV = new Vector(formats.length);
	    for (int i = 0; i < formats.length; i++)
		formatsV.addElement(formats[i]);
	    mediaImpl.setMediaFormats(formatsV);
	    mediaDescriptionImpl.setMedia(mediaImpl);
	} catch (SdpException s) {
	    s.printStackTrace();
	}
	return mediaDescriptionImpl; 
    }
 
    /**
     * Returns TimeDescription object with the specified properties.
     * @param t the Time that the time description applies to. Returns
     * TimeDescription object with the specified properties.
     * @throws SdpException
     * @return TimeDescription
     */ 
    public TimeDescriptionImpl createTimeDescription(TimeField t)
	throws SdpException {
	TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
	timeDescriptionImpl.setTime(t);
	return timeDescriptionImpl; 
    }
 
    /**
     * Returns TimeDescription unbounded (i.e. "t=0 0");
     * @throws SdpException
     * @return TimeDescription unbounded (i.e. "t=0 0");
     */ 
    public TimeDescriptionImpl createTimeDescription()
	throws SdpException {
	TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
	TimeField timeImpl = new TimeField();
	timeImpl.setZero();
	timeDescriptionImpl.setTime(timeImpl);
	return timeDescriptionImpl; 
    }
 
    /**
     * Returns TimeDescription object with the specified properties.
     * @param start start time.
     * @param stop stop time.
     * @throws SdpException if the parameters are null
     * @return TimeDescription
     */ 
    public TimeDescriptionImpl createTimeDescription(Date start,
						     Date stop)
	throws SdpException {
	TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
	TimeField timeImpl = new TimeField();
	timeImpl.setStart(start);
	timeImpl.setStop(stop);
	timeDescriptionImpl.setTime(timeImpl);
	return timeDescriptionImpl; 
    }
    /**
     * Returns a String containing the computed form for a
     * multi-connection address. 
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast
     * addresses
     * @param numAddrs number of addresses used by the
     * connection 
     * @return  computed form for a multi-connection address.
     */
    public String formatMulticastAddress(String addr,
					 int ttl,
					 int numAddrs) {
	String res = addr + "/" + ttl + "/"+numAddrs; 
	return res;
    }
 
    /**
     * Returns a Connection object with the specified properties a
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */ 
    public ConnectionField createConnection(String netType,
					    String addrType,
					    String addr,
					    int ttl,
					    int numAddrs)throws SdpException {
	ConnectionField connectionImpl = new ConnectionField(); 
 
	connectionImpl.setNetworkType(netType);
	connectionImpl.setAddressType(addrType);
	connectionImpl.setAddress(addr);
 
	return connectionImpl;
    }
 
    /**
     * Returns a Connection object with the specified properties and no
     * TTL and a default number of addresses (1).
     * @param netType network type, eg "IN" for "Internet"
     * @param addrType address type, eg "IP4" for IPv4 type addresses
     * @param addr connection address
     * @throws SdpException if the parameters are null
     * @return Connection
     */ 
    public ConnectionField createConnection(String netType,
					    String addrType,
					    String addr)
	throws SdpException {
	ConnectionField connectionImpl = new ConnectionField();
 
	connectionImpl.setNetworkType(netType);
	connectionImpl.setAddressType(addrType);
	connectionImpl.setAddress(addr);
 
	return connectionImpl;
    }
 
    /**
     * Returns a Connection object with the specified properties and a
     * network and address type of "IN" and "IP4" respectively.
     * @param addr connection address
     * @param ttl time to live (TTL) for multicast addresses
     * @param numAddrs number of addresses used by the connection
     * @return Connection
     */ 
    public ConnectionField createConnection(String addr,
					    int ttl,
					    int numAddrs) throws SdpException {
	ConnectionField connectionImpl = new ConnectionField(); 
 
	connectionImpl.setAddress(addr);
 
	return connectionImpl;
    }
 
    /**
     * Returns a Connection object with the specified address. This is
     * equivalent to
     *
     * createConnection("IN", "IP4", addr);
     *
     * @param addr connection address
     * @throws SdpException if the parameter is null
     * @return Connection
     */ 
    public ConnectionField createConnection(String addr)
	throws SdpException {
 
	return createConnection("IN", " IPV4", addr);
 
    }
 
    /**
     * Returns a Time specification with the specified start and stop
     * times.
     * @param start start time
     * @param stop stop time
     * @throws SdpException if the parameters are null
     * @return a Time specification with the specified start and stop
     * times.
     */ 
    public TimeField createTime(Date start,
				Date stop)
	throws SdpException {
	TimeField timeImpl = new TimeField();
	timeImpl.setStart(start);
	timeImpl.setStop(stop);
	return timeImpl;
    }
 
    /**
     * Returns an unbounded Time specification (i.e., "t=0 0").
     * @throws SdpException
     * @return an unbounded Time specification (i.e., "t=0 0").
     */ 
    public TimeField createTime()
	throws SdpException {
	TimeField timeImpl = new TimeField();
	timeImpl.setZero();
	return timeImpl;
    }
 
    /**
     * Returns a RepeatTime object with the specified interval,
     * duration, and time offsets.
     * @param repeatInterval the "repeat interval" in seconds
     * @param activeDuration the "active duration" in seconds
     * @param offsets the list of offsets relative to the start time of
     * the Time object with which the returned RepeatTime will be
     * associated
     * @return RepeatTime
     */ 
    public RepeatField createRepeatTime(int repeatInterval,
					int activeDuration,
					int[] offsets) {
	RepeatField repeatTimeField = new RepeatField();
	try {
 
	    repeatTimeField.setRepeatInterval(repeatInterval);
	    repeatTimeField.setActiveDuration(activeDuration);
	    repeatTimeField.setOffsetArray(offsets);
 
	}
	catch (SdpException s) {
	    s.printStackTrace();
	} 
	return repeatTimeField;
    }
 
    /**
     * Constructs a timezone adjustment record.
     * @param d the Date at which the adjustment is going to take
     * place.
     * @param offset the adjustment in number of seconds relative to
     * the start time of the SessionDescription with which this
     * object is associated.
     * @return TimeZoneAdjustment
     */ 
    public ZoneField createTimeZoneAdjustment(Date d,
					      int offset) {
	ZoneField timeZoneAdjustmentImpl = new ZoneField();
	try {
 
	    Hashtable map = new Hashtable();
	    map.put(d, new Integer(offset));
	    timeZoneAdjustmentImpl.setZoneAdjustments(map);
	}
	catch (SdpException s) {
	    s.printStackTrace();
	} 
	return timeZoneAdjustmentImpl;
    }
 
 
    /**
     * Returns a collection of Strings containing session description.
     * @param source String containing session descriptions.
     * @return a collection of Strings containing session descriptions.
     */ 
    public static Vector findSessions(String source) {
	return sessionDescriptionsList;
    }
 
    /**
     * Converts a NTP to regular date value. 
     * @param ntpTime long to set
     * @return Returns a Date object for a given NTP date value.
     */ 
    public static Date getDateFromNtp(long ntpTime) {
	return new Date((ntpTime - SdpConstants.NTP_CONST) * 1000);
    }
 
    /**
     * Returns a long containing the NTP value for a given Java Date.
     * @param d Date to set
     * @return long
     */ 
    public static long getNtpTime(Date d) throws SdpParseException {
	if (d == null)
	    return -1;
	return ((d.getTime()/1000) + SdpConstants.NTP_CONST);
    }
    /*
    public static void main(String[] args) 
	throws SdpParseException, SdpException {
	String sdpFields = "v=0\r\n"+
	    "o=phone 1057165447541 1057165447541 IN IP4 123.4.566.156\r\n"+
	    "s=-\r\n" +
	    "c=IN IP4 123.4.566.156\r\n"+
	    "t=0 0\r\n" +
	    "m=data 3011 tcp OSA\r\n";

	SdpFactory sdpFactory = new SdpFactory();
	SessionDescriptionImpl sessionDescription = 
	    sdpFactory.createSessionDescription(sdpFields);
 
	System.out.println("sessionDescription = " + sessionDescription);
	Vector mediaDescriptions = 
	    sessionDescription.getMediaDescriptions(false);
	for (int i = 0; i < mediaDescriptions.size(); i++) {
	    MediaDescriptionImpl m = (MediaDescriptionImpl) 
		mediaDescriptions.elementAt(i);
	    System.out.println("m = " + m.toString());
	    MediaField media = m.getMedia();
	    Vector formats = media.getMediaFormats(false);
	    System.out.println("formats = " + formats);
	}
    }
    */
 
}
