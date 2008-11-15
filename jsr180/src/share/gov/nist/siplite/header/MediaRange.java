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
 * Media Range.
 * @since 0.9
 * @version 1.0
 * <pre>
 * Revisions:
 *
 * Version 1.0
 * 1. Added encode method.
 *
 * media-range = ( "STAR/STAR"
 * | ( type "/" STAR )
 * | ( type "/" subtype )
 * ) *( ";" parameter )
 *
 * HTTP RFC 2616 Section 14.1
 * </pre>
 */
public class MediaRange extends GenericObject {

    /**
     * Media range type field.
     */
    protected String type;

    /**
     * Media range subtype field.
     */
    protected String subtype;

    /**
     * Copies the current instance.
     * @return copy of the current object.
     */
    public Object clone() {
        MediaRange retval = new MediaRange();
        if (type != null)
            retval.type = new String(this.type);
        if (subtype != null)
            retval.subtype = new String(this.subtype);
        return retval;
    }

    /**
     * Default constructor.
     */
    public MediaRange() {
    }

    /**
     * Gets the media range type field.
     * @return the type field value
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the media range subtype field.
     * @return the sub type field value
     */
    public String getSubtype() {
        return subtype;
    }


    /**
     * Sets the media range type member....
     * @param t String to set
     */
    public void setType(String t) {
        type = t;
    }

    /**
     * Sets the media range subtype member.
     * @param s String to set
     */
    public void setSubtype(String s) {
        subtype = s;
    }

    /**
     * Encodes the object.
     * @return String canonical encoded version of this object.
     */
    public String encode() {
        String encoding = type + Separators.SLASH + subtype;
        return encoding;
    }

    /**
     * Encodes the object. Calls encode().
     * @return String canonical encoded version of this object.
     */
    public String toString() {
        return encode();
    }
}
