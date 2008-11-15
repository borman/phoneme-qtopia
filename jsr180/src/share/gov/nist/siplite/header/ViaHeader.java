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

import gov.nist.siplite.parser.*;
import gov.nist.core.*;

/**
 * Via Header (these are strung together in a ViaList).
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ViaHeader extends gov.nist.siplite.header.ParametersHeader {
    /** Class handle. */
    public static Class clazz;
    /** Via header field label. */
    public static final String NAME = Header.VIA;

    /**
     * The branch parameter is included by every forking proxy.
     */
    public static final String BRANCH = "branch";

    /**
     * The "hidden" paramter is included if this header field
     * was hidden by the upstream proxy.
     */
    public static final String HIDDEN = "hidden";

    /**
     * The "received" parameter is added only for receiver-added Via Fields.
     */
    public static final String RECEIVED = "received";

    /**
     * The "maddr" parameter is designating the multicast address.
     */
    public static final String MADDR = "maddr";

    /**
     * The "TTL" parameter is designating the time-to-live value.
     */
    public static final String TTL = "ttl";

    /**
     * Sent protocol field.
     */
    protected Protocol sentProtocol;

    /**
     * Sent by field.
     */
    protected HostPort sentBy;

    /**
     * Comment field.
     */
    protected String comment;


    static {
        clazz = new ViaHeader().getClass();
    }

    /**
     * Default constructor.
     */
    public ViaHeader() {
        super(VIA);
        this.sentBy = new HostPort();

        sentProtocol = new Protocol();
    }

    /**
     * Compares two via headers for equaltiy.
     * @param other Object to set.
     * @return true if the two via headers are the same.
     */
    public boolean equals(Object other) {
        if (! this.getClass().equals(other.getClass())) {
            return false;
        }

        ViaHeader that = (ViaHeader) other;

        if (! this.sentProtocol.equals(that.sentProtocol)) {
            return false;
        }

        if (! this.parameters.equals(that.parameters)) {
            return false;
        }

        if (! this.sentBy.equals(that.sentBy)) {
            return false;
        }

        return true;
    }

    /**
     * Encodes the via header into a cannonical string.
     * @return String containing cannonical encoding of via header.
     */
    public String encodeBody() {
        String encoding = "";
        encoding += sentProtocol.encode() + Separators.SP + sentBy.encode();

        // Add the default port if there is no port specified.
        if (! sentBy.hasPort()) encoding += Separators.COLON + "5060";

        if (comment != null) {
            encoding += Separators.LPAREN + comment + Separators.RPAREN;
        }

        encoding += encodeWithSep();

        return encoding;
    }

    /**
     * Gets the Protocol Version.
     * @return String
     */
    public String getProtocolVersion() {
        if (sentProtocol == null)
            return null;
        else
            return sentProtocol.getProtocolVersion();
    }

    /**
     * Accessor for the sentProtocol field.
     * @return Protocol field
     */
    public Protocol getSentProtocol() {

        return sentProtocol;
    }

    /**
     * Accessor for the sentBy field
     * @return SentBy field
     */
    public HostPort getSentBy() {
        return sentBy;
    }

    /**
     * Gest the host name. (null if not yet set).
     * @return host name from the via header.
     */
    public String getHost() {
        if (sentBy == null)
            return null;
        else {
            Host host = sentBy.getHost();
            if (host == null)
                return null;
            else return host.getHostname();
        }
    }

    /**
     * Port of the Via header.
     * @return port field.
     */
    public int getPort() {
        if (sentBy == null)
            return -1;
        return sentBy.getPort();
    }

    /**
     * Port of the Via Header.
     * @return true if Port exists.
     */
    public boolean hasPort() {
        if (sentBy == null)
            return false;
        return (getSentBy()).hasPort();
    }

    /**
     * Accessor for the comment field.
     * @return comment field.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the Branch parameter if it exists.
     * @return Branch field.
     */
    public String getBranch() {
        return super.getParameter(ViaHeader.BRANCH);
    }

    /**
     * Gets the received parameter if it exists.
     * @return received parameter.
     */
    public String getReceived() {
        return super.getParameter(ViaHeader.RECEIVED);

    }

    /**
     * Gets the maddr parameter if it exists.
     * @return maddr parameter.
     */
    public String getMaddr() {
        return super.getParameter(ViaHeader.MADDR);

    }

    /**
     * get the ttl parameter if it exists.
     * @return ttl parameter.
     */
    public String getTTL() {
        return super.getParameter(ViaHeader.TTL);
    }

    /**
     * Comment of the Via Header.
     *
     * @return false if comment does not exist and true otherwise.
     */
    public boolean hasComment() {
        return comment != null;
    }

    /**
     * Removes the comment field.
     */
    public void removeComment() {
        comment = null;
    }

    /**
     * Sets the Protocol Version.
     *
     * BNF (RFC3261, p. 222, 232):
     *     protocol-version = token
     *
     * @param protocolVersion String to set
     */
    public void setProtocolVersion(String protocolVersion) {
        if (sentProtocol == null) sentProtocol = new Protocol();
        sentProtocol.setProtocolVersion(protocolVersion);
    }

    /**
     * Sets the sentProtocol member
     * @param s Protocol to set.
     */
    public void setSentProtocol(Protocol s) {
        sentProtocol = s;
    }

    /**
     * Sets the transport string.
     *
     * BNF (RFC3261, p. 222, 232):
     *     transport = "UDP" / "TCP" / "TLS" / "SCTP" / other-transport
     *
     * @param transport String to set
     */
    public void setTransport(String transport) {
        if (sentProtocol == null) sentProtocol = new Protocol();
        sentProtocol.setTransport(transport);
    }

    /**
     * Sets the sentBy member
     * @param s HostPort to set.
     */
    public void setSentBy(HostPort s) {
        sentBy = s;
    }

    /**
     * Sets the comment member
     * @param c String to set.
     */
    public void setComment(String c) {
        comment = c;
    }

    /**
     * Clone - do a deep copy.
     * @return Object Via
     */
    public Object clone() {
        ViaHeader retval = new ViaHeader();

        if (this.comment != null) retval.comment = new String(this.comment);
        if (this.parameters != null) retval.parameters =
                (NameValueList) parameters.clone();
        if (this.sentBy != null) retval.sentBy = (HostPort)sentBy.clone();
        if (this.sentProtocol != null)
            retval.sentProtocol = (Protocol)sentProtocol.clone();
        return retval;
    }

    /**
     * Gets the value portion of this header (does nto include the parameters).
     * @return the via header field value
     */
    public Object getValue() {
        return sentProtocol.encode() + " " + sentBy.encode();

    }

    /**
     * Sets the header value field (without parameters).
     * @param value is the value field to set.
     * @throws IllegalArgumentException if the value is invalid.
     */
    public void setHeaderValue(String value)
            throws IllegalArgumentException {

        StringMsgParser smp = new StringMsgParser();
        ViaList hl = null;
        String strNewHeader = NAME + Separators.COLON + value;

        try {
            hl = (ViaList)smp.parseHeader(strNewHeader);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.toString());
        }

        if (hl.size() > 1) {
            throw new IllegalArgumentException("Ivalid Via header " +
                                               "value: " + value);
        }

        ViaHeader header = (ViaHeader)hl.elementAt(0);

        // Copy the values from the header created from the parsed value
        setSentBy(header.getSentBy());
        setSentProtocol(header.getSentProtocol());

        if (sentProtocol != null) {
            setProtocolVersion(header.getProtocolVersion());
            setTransport(header.getTransport());
        }

        if (sentBy != null) {
            setHost(header.getHost());
            setPort(header.getPort());
        }

        // IMPL_NOTE:
        // System.out.println("new: '" + strNewHeader + "'");
        // System.out.println("this: '" + strNewHeader + "'");
    }

    /**
     * This function is overloaded in order to validate the parameters.
     * Sets the value of the specified parameter. If the parameter already
     * had a value it will be overwritten. A zero-length String indicates flag
     * parameter.
     *
     * IMPL_NOTE: add a support for all parameters that are stored as
     *       members of this class.
     *
     * @param name a String specifying the parameter name
     * @param value a String specifying the parameter value
     * @throws IllegalArgumentException if the parameter's name or its value
     * is invalid.
     */
    public void setParameter(String name, String value)
            throws IllegalArgumentException {
        if (name.equalsIgnoreCase(ViaHeader.TTL)) {
            setTTL(value);
        } else if (name.equalsIgnoreCase(ViaHeader.RECEIVED)) {
            setReceived(value);
        } else {
            super.setParameter(name, value);
        }
    }

    /**
     * Sets the 'received' parameter.
     *
     * BNF (RFC3261, p. 223, 232):
     *     via-received = "received" EQUAL (IPv4address / IPv6address)
     *     IPv4address  =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
     *     IPv6address  =  hexpart [ ":" IPv4address ]
     *     hexpart      =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
     *     hexseq       =  hex4 *( ":" hex4)
     *     hex4         =  1*4HEXDIG
     *
     * @param received the new 'received' value.
     * @throws IllegalArgumentException if the new 'received' value is invalid.
     */
    public void setReceived(String received) throws IllegalArgumentException {
        if (!Lexer.isValidIpv4Address(received) &&
            !Lexer.isValidIpv6Address(received)) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        
        super.setParameter(RECEIVED, received);
    }

    /**
     * Sets the maddr parameter.
     *
     * BNF (RFC3261, p. 222, 232):
     *     via-maddr   = "maddr" EQUAL host
     *     host        =  hostname / IPv4address / IPv6reference
     *     hostname    =  *( domainlabel "." ) toplabel [ "." ]
     *     domainlabel =  alphanum / alphanum *( alphanum / "-" ) alphanum
     *     toplabel    =  ALPHA / ALPHA *( alphanum / "-" ) alphanum
     *
     *     IPv4address   =  1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT "." 1*3DIGIT
     *     IPv6reference =  "[" IPv6address "]"
     *     IPv6address   =  hexpart [ ":" IPv4address ]
     *     hexpart       =  hexseq / hexseq "::" [ hexseq ] / "::" [ hexseq ]
     *     hexseq        =  hex4 *( ":" hex4)
     *     hex4          =  1*4HEXDIG
     *
     * IMPL_NOTE: check maddr for validity.
     * @param maddr the new maddr value.
     * @throws IllegalArgumentException if the new maddr value is invalid.
     */
    public void setMaddr(String maddr) throws IllegalArgumentException {
        super.setParameter(MADDR, maddr);
    }

    /**
     * Sets the ttl parameter.
     *
     * BNF (RFC3261, p. 232):
     *     ttl = 1*3DIGIT ; 0 to 255
     *
     * @param strTTL the new ttl value given in a string form.
     * @throws IllegalArgumentException if the new ttl value is invalid.
     */
    public void setTTL(String strTTL) throws IllegalArgumentException {
        int ttl = 0;

        try {
            ttl = Integer.parseInt(strTTL);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse TTL '" +
                strTTL + "': " + e);
        }

        if (ttl < 0 || ttl > 255) {
            throw new IllegalArgumentException("Invalid TTL: " + strTTL);
        }

        super.setParameter(TTL, strTTL);
    }

    /**
     * Sets the branch field.
     *
     * BNF (RFC3261, p. 232):
     *     via-branch = "branch" EQUAL token
     *
     * @param branch the new branch value
     */
    public void setBranch(String branch) {
        super.setParameter(BRANCH, branch);
    }

    /**
     * Sets the host field.
     *
     * BNF (RFC3261, p. 222):
     *     host =  hostname / IPv4address / IPv6reference
     * See setMaddr() for the full BNF.
     *
     * @param host the new host value
     */
    public void setHost(String host) {
        this.sentBy.setHost(new Host(host));
    }

    /**
     * Sets the host field.
     * @param host the new host value
     */
    public void setHost(Host host) {
        this.sentBy.setHost(host);
    }

    /**
     * Sets the port field.
     *
     * BNF (RFC3261, p. 232):
     *     port = 1*DIGIT
     *
     * @param port the new port value
     */
    public void setPort(int port) {
        this.sentBy.setPort(port);
    }

    /**
     * Gets the current transport.
     * @return the current transport
     */
    public String getTransport() {
        if (this.sentProtocol == null)
            return null;
        else return this.sentProtocol.getTransport();
    }

}
