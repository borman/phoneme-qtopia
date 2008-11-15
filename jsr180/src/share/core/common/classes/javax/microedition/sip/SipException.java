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

import java.io.IOException;

/**
 * This is an exception class for SIP specific errors. 
 * @see JSR180 spec, v 1.1.0, p 78-81
 *
 */
public class SipException extends IOException {
    /**
     * Other SIP error
     */
    public static final byte GENERAL_ERROR = 0;

    /**
     * The requested transport is not supported
     */
    public static final byte TRANSPORT_NOT_SUPPORTED = 1;

    /**
     * Deprecated. 
     * Thrown for example when SIP connection does not belong to any Dialog. 
     */
    public static final byte DIALOG_UNAVAILABLE = 2;

    /**
     * Used when for example Content-Type is not set before filling
     * the message body.
     */
    public static final byte UNKNOWN_TYPE = 3;

    /**
     * Used when for example Content-Length is not set before filling
     * the message body
     */
    public static final byte UNKNOWN_LENGTH = 4;

    /**
     * Method call not allowed, because of wrong state in SIP connection.
     */
    public static final byte INVALID_STATE = 5;

    /**
     * The system does not allow particular operation. NOTICE! This error does
     * not handle security exceptions.
     */
    public static final byte INVALID_OPERATION = 6;

    /**
     * System can not open any new transactions.
     */
    public static final byte TRANSACTION_UNAVAILABLE = 7;

    /**
     * The message to be sent has invalid format.
     */
    public static final byte INVALID_MESSAGE = 8;

    /**
     * The response can not be sent because the system has already sent a response.
     */
    public static final byte ALREADY_RESPONDED = 9;

    /**
     * Current error code for this sip exception
     */
    private byte error_code;

    /**
     * Construct SipException with error code.
     * @param errorCode - error code. If the error code is none of
     *  the specified
     * codes the Exception is initialized with default GENERAL_ERROR.
     */
    public SipException(byte errorCode) {
        super();

        if (errorCode > INVALID_MESSAGE || errorCode < GENERAL_ERROR) {
            // If the error code is none of the specified codes
            // the Exception is initialized with default GENERAL_ERROR.
            errorCode = GENERAL_ERROR;
        } else {
            error_code = errorCode;
        }
    }

    /**
     * Construct SipException with textual message and error code.
     * @param message - error message.
     * @param errorCode - error code. If the error code is none of
     *  the specified
     * codes the Exception is initialized with default GENERAL_ERROR.
     */
    public SipException(java.lang.String message, byte errorCode) {
        super(message);

        if (errorCode > INVALID_MESSAGE || errorCode < GENERAL_ERROR) {
            // If the error code is none of the specified codes
            // the Exception is initialized with default GENERAL_ERROR.
            errorCode = GENERAL_ERROR;
        } else {
            error_code = errorCode;
        }
    }

    /**
     * Gets the error code
     * @return error code
     */
    public byte getErrorCode() {
        return error_code;
    }
}
