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

/**
 * Field names for SDP Fields.
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface SDPFieldNames {
    /** Session name field label. */
    public static final String SESSION_NAME_FIELD = "s=";
    /** Information field label. */
    public static final String INFORMATION_FIELD = "i=";
    /** Email field label. */
    public static final String EMAIL_FIELD = "e=";
    /** Phone number field label. */
    public static final String PHONE_FIELD = "p=";
    /** Network connection field label. */
    public static final String CONNECTION_FIELD = "c=";
    /** Bandwidth field label. */
    public static final String BANDWIDTH_FIELD = "b=";
    /** Originator field label. */
    public static final String ORIGIN_FIELD = "o=";
    /** Time field label. */
    public static final String TIME_FIELD = "t=";
    /** Key field label. */
    public static final String KEY_FIELD = "k=";
    /** Attribute field label. */
    public static final String ATTRIBUTE_FIELD = "a=";
    /** Protocol version field label. */
    public static final String PROTO_VERSION_FIELD = "v=";
    /** URI field label. */
    public static final String URI_FIELD = "u=";
    /** Media type field label. */
    public static final String MEDIA_FIELD = "m=";
    /** Repeating event field label. */
    public static final String REPEAT_FIELD = "r=";
    /** Timezone field label. */
    public static final String ZONE_FIELD = "z=";
}
