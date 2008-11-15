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
package gov.nist.siplite;

/**
 * Constant values.
 * Each constant has a prefix defining a group that the constant belongs to:
 * SCHEME - scheme constants;
 * TRANSPORT - transport names;
 * TOKEN - tokens designating names of days, months and letters;
 * GENERAL - all other constants.
 * Constants related to SIP requests and responses are located
 * in Request and Response respectively.
 */
public interface SIPConstants {
    /** SIP version string. */
    String SIP_VERSION_STRING = "SIP/2.0";
    /** SIP protocol scheme label. */
    public static final String SCHEME_SIP = "SIP";
    /** SIPS protocol scheme label. */
    public static final String SCHEME_SIPS = "SIPS";
    /** TEL protocol scheme token. */
    public static final String SCHEME_TEL = "TEL";
    /** UDP transport label. */
    public static final String TRANSPORT_UDP = "UDP";
    /** TCP transport label. */
    public static final String TRANSPORT_TCP = "TCP";
    /** TLS transport label. */
    public static final String TRANSPORT_TLS = "TLS";
    /** SCTP transport label. */
    public static final String TRANSPORT_SCTP = "SCTP";
    /** Default port for TCP, UDP and SCTP. */
    public static int DEFAULT_NONTLS_PORT = 5060;
    /** Default port for TLS. */
    public static int DEFAULT_TLS_PORT = 5061;

    /** Post a dialing method. */
    public static final String GENERAL_POSTDIAL = "postdial";
    /** Current phone context label. */
    public static final String GENERAL_PHONE_CONTEXT_TAG = "context-tag";
    /** ISDN subaddress label. */
    public static final String GENERAL_ISUB = "isub";
    /** Label for current provider attribute. */
    public static final String GENERAL_PROVIDER_TAG = "provider-tag";
    /** User name label. */
    public static final String GENERAL_USER = "user";
    /** Transport type label. */
    public static final String GENERAL_TRANSPORT = "transport";
    /** Method label. */
    public static final String GENERAL_METHOD = "method";
    /** Time to live label. */
    public static final String GENERAL_TTL = "ttl";
    /** Mail address label. */
    public static final String GENERAL_MADDR = "maddr";
    /** lr label (???). RFC */
    public static final String GENERAL_LR = "lr";
    /** Branch label. */
    public static final String GENERAL_BRANCH = "branch";
    /** Type label. */
    public static final String GENERAL_TYPE = "type";

    /**
     * Prefix for the branch parameter that identifies
     * BIS 09 compatible branch strings. This indicates
     * that the branch may be as a global identifier for
     * identifying transactions.
     */
    public static final String GENERAL_BRANCH_MAGIC_COOKIE = "z9hG4bK";

    /** GMT time zone token. */
    public static final String TOKEN_GMT = "GMT";
    /** Monday token. */
    public static final String TOKEN_DAY_MON = "MON";
    /** Tuesday token. */
    public static final String TOKEN_DAY_TUE = "TUE";
    /** Wednesday token. */
    public static final String TOKEN_DAY_WED = "WED";
    /** Thursday token. */
    public static final String TOKEN_DAY_THU = "THU";
    /** Friday token. */
    public static final String TOKEN_DAY_FRI = "FRI";
    /** Saturday token. */
    public static final String TOKEN_DAY_SAT = "SAT";
    /** Sunday token. */
    public static final String TOKEN_DAY_SUN = "SUN";
    /** January token. */
    public static final String TOKEN_MONTH_JAN = "JAN";
    /** February token. */
    public static final String TOKEN_MONTH_FEB = "FEB";
    /** March token. */
    public static final String TOKEN_MONTH_MAR = "MAR";
    /** April token. */
    public static final String TOKEN_MONTH_APR = "APR";
    /** May token. */
    public static final String TOKEN_MONTH_MAY = "MAY";
    /** June token. */
    public static final String TOKEN_MONTH_JUN = "JUN";
    /** July token. */
    public static final String TOKEN_MONTH_JUL = "JUL";
    /** August token. */
    public static final String TOKEN_MONTH_AUG = "AUG";
    /** September token. */
    public static final String TOKEN_MONTH_SEP = "SEP";
    /** October token. */
    public static final String TOKEN_MONTH_OCT = "OCT";
    /** November token. */
    public static final String TOKEN_MONTH_NOV = "NOV";
    /** December token. */
    public static final String TOKEN_MONTH_DEC = "DEC";
    /** K token. */
    public static final String TOKEN_LETTER_K = "K";
    /** C token. */
    public static final String TOKEN_LETTER_C = "C";
    /** E token. */
    public static final String TOKEN_LETTER_E = "E";
    /** F token. */
    public static final String TOKEN_LETTER_F = "F";
    /** I token. */
    public static final String TOKEN_LETTER_I = "I";
    /** M token. */
    public static final String TOKEN_LETTER_M = "M";
    /** L token. */
    public static final String TOKEN_LETTER_L = "L";
    /** O token. */
    public static final String TOKEN_LETTER_O = "O";
    /** S token. */
    public static final String TOKEN_LETTER_S = "S";
    /** T token. */
    public static final String TOKEN_LETTER_T = "T";
    /** V token. */
    public static final String TOKEN_LETTER_V = "V";
    /** U token. */
    public static final String TOKEN_LETTER_U = "U";
    /** A token. */
    public static final String TOKEN_LETTER_A = "A";
}
