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

package javax.microedition.sip;

/**
 * SipServerConnection represents SIP server transaction.
 * @see JSR180 spec, v 1.0.1, p 33-36
 *
 */
public interface SipServerConnection extends SipConnection {
    
    /**
     * Initializes SipServerConnection with a specific SIP response to the
     * received request.
     * @see JSR180 spec, v 1.1.0, p 47-48
     *
     * @param code - Response status code 1xx - 6xx
     * @throws IllegalArgumentException - if the status code is out of
     * range 100-699 (RFC 3261 p.28-29)
     * @throws SipException - INVALID_STATE if the response can not be
     * initialized, because of wrong state, 
     * ALREADY_RESPONDED if the system has already sent a response to a MESSAGE 
     * request.
     */
    public void initResponse(int code)
    throws java.lang.IllegalArgumentException, SipException;
    
    /**
     * Changes the default reason phrase.
     * @see JSR180 spec, v 1.1.0, p 48
     *
     * @param phrase - the default reason phrase. Empty string or null means 
     * that an empty (zero-length) reason phrase will be set.
     * @throws SipException - INVALID_STATE if the response can not
     * be initialized,
     * because of wrong state. INVALID_OPERATION if the reason phrase can not
     * be set.
     * @throws IllegalArgumentException - if the reason phrase is illegal.
     */
    public void setReasonPhrase(java.lang.String phrase)
    throws SipException;
}
