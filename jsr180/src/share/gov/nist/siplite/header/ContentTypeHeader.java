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
/**
 * ContentTypeHeader SIP Header
 * <pre>
 *14.17 Content-Type
 *
 * The Content-Type entity-header field indicates the media type of the
 * entity-body sent to the recipient or, in the case of the HEAD method,
 * the media type that would have been sent had the request been a GET.
 *
 * Format: Content-Type = "Content-Type" ":" media-type
 *
 * For details please see RFC 2616, section 14.17
 * </pre>
 *
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ContentTypeHeader extends ParametersHeader {
    
    /**
     * Media range header field.
     */
    protected MediaRange mediaRange;
    /** Handle for class. */
    public static Class clazz;
    /** Media range header field label. */
    public static final String NAME = Header.CONTENT_TYPE;
    
    
    static {
        clazz = new ContentTypeHeader().getClass();
    }
    
    
    /**
     * Default constructor.
     */
    public ContentTypeHeader() {
        super(CONTENT_TYPE);
    }
    
    /**
     * Constructor given a content type and subtype.
     * @param contentType is the content type.
     * @param contentSubtype is the content subtype
     */
    public ContentTypeHeader(String contentType, String contentSubtype) {
        this();
        this.setContentType(contentType, contentSubtype);
    }
    
    /**
     * Compares two MediaRange headers.
     * @param media String to set
     * @return int.
     */
    public int compareMediaRange(String media) {
        return compareToIgnoreCase
                (mediaRange.type + "/" + mediaRange.subtype, media);
    }
    
    /**
     * Encodes into a canonical string.
     * @return String.
     */
    public String encodeBody() {
        if (hasParameters())
            return new StringBuffer(mediaRange.encode())
            .append(encodeWithSep()).toString();
        else return mediaRange.encode();
    }
    
    /**
     * Gets the mediaRange field.
     * @return MediaRange.
     */
    public MediaRange getMediaRange() {
        return mediaRange;
    }
    
    /**
     * Gets the Media Type.
     * @return String.
     */
    public String getMediaType() {
        return mediaRange.type;
    }
    
    /**
     * Gets the MediaSubType field.
     * @return String.
     */
    public String getMediaSubType() {
        return mediaRange.subtype;
    }
    
    /**
     * Gets the content subtype.
     * @return the content subtype string (or null if not set).
     */
    public String getContentSubType() {
        return mediaRange == null ? null : mediaRange.getSubtype();
    }
    
    /**
     * Get the content subtype.
     * @return the content tyep string (or null if not set).
     */
    
    public String getContentTypeHeader() {
        return mediaRange == null ? null : mediaRange.getType();
    }
    
    
    /**
     * Gets the charset parameter.
     * @return  the content type header value
     */
    public String getCharset() {
        return this.getParameter("charset");
    }
    
    
    /**
     * Sets the mediaRange member
     * @param m mediaRange field.
     */
    public void setMediaRange(MediaRange m) {
        mediaRange = m;
    }
    
    /**
     * Sets the content type and subtype.
     * @param contentType Content type string.
     * @param contentSubType content subtype string
     */
    public void setContentType(String contentType, String contentSubType) {
        if (mediaRange == null) mediaRange = new MediaRange();
        mediaRange.setType(contentType);
        mediaRange.setSubtype(contentSubType);
    }
    
    /**
     * Sets the content type.
     * @param contentType Content type string.
     */
    
    public void setContentType(String contentType) {
        if (contentType == null)
            throw new NullPointerException("null arg");
        if (mediaRange == null) mediaRange = new MediaRange();
        mediaRange.setType(contentType);
    }
    
    /**
     * Sets the content subtype.
     * @param contentType String to set
     */
    public void setContentSubType(String contentType) {
        if (contentType == null)
            throw new NullPointerException("null arg");
        if (mediaRange == null) mediaRange = new MediaRange();
        mediaRange.setSubtype(contentType);
    }
    
    /**
     * Gets the content type header value.
     * @return the content type
     */
    public Object getValue() {
        return this.mediaRange;
    }
    
    /**
     * Copies the current instance.
     * @return copy od the current object
     */
    public Object clone() {
        ContentTypeHeader retval = new ContentTypeHeader();
        retval.parameters = (NameValueList) this.parameters.clone();
        retval.mediaRange = (MediaRange)this.mediaRange.clone();
        return retval;
    }
    
    
}
