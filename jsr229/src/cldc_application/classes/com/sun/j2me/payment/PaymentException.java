/*
 *   
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.j2me.payment;

/**
 * Thrown when validating MIDlet's payment information or when downloading and
 * validating a payment update file (JPP).
 *
 * @version 1.2
 */
public class PaymentException extends Exception {
    
    /** 
     * The payment information has an unsupported version. The 
     * <code>getParam()</code> method returns the number of this version.
     */
    public static final int UNSUPPORTED_PAYMENT_INFO = 0;
    /** 
     * The payment information doesn't contain a mandatory attribute. The 
     * <code>getParam()</code> method returns the name of the attribute.
     */
    public static final int MISSING_MANDATORY_ATTRIBUTE = 1;
    /** 
     * The payment information contains an attribute with an invalid value. The 
     * <code>getParam()</code> method returns the name of the attribute.
     */
    public static final int INVALID_ATTRIBUTE_VALUE = 2;
    /** 
     * The device doesn't support any of the adapters listed in the payment
     * information file. The <code>getParam()</code> method returns the name of 
     * the attribute which contains the adapter names.
     */
    public static final int UNSUPPORTED_ADAPTERS = 3;
    /** 
     * The device doesn't support any of the adapters listed in the payment
     * information file. The <code>getParam()</code> method returns the name of 
     * the attribute which contains the provider names.
     */
    public static final int UNSUPPORTED_PROVIDERS = 4;
    /** 
     * The device doesn't support the scheme of the update URL. The 
     * <code>getParam()</code> method returns the update URL. 
     */
    public static final int UNSUPPORTED_URL_SCHEME = 5;
    /** The payment information is not yet valid. */
    public static final int INFORMATION_NOT_YET_VALID = 6;
    /** The payment information has been expired. */
    public static final int INFORMATION_EXPIRED = 7;
    /** The payment information is incomplete. */
    public static final int INCOMPLETE_INFORMATION = 8;
    /** 
     * The payment information contains an invalid PaymentSpecificInformation
     * field. The <code>getParam()</code> method returns the name of the 
     * attribute which contains the invalid field.
     */
    public static final int INVALID_ADAPTER_CONFIGURATION = 9;
    /** 
     * The payment information contains an invalid PaymentSpecificPrice-
     * Information field. The <code>getParam()</code> method returns the name 
     * of the attribute which contains the invalid field.
     */
    public static final int INVALID_PRICE_INFORMATION = 10;
    /** 
     * The payment update failed because the update URL is invalid. The
     * <code>getParam()</code> method returns the URL.
     */
    public static final int INVALID_UPDATE_URL = 11;
    /** 
     * The update server has been not found. The <code>getParam()</code> method 
     * returns the URL of the payment update file.
     */
    public static final int UPDATE_SERVER_NOT_FOUND = 12;
    /** 
     * The payment update file has not been found on the server. The 
     * <code>getParam()</code> method returns the URL of the payment update 
     * file. 
     */
    public static final int UPDATE_NOT_FOUND = 13;
    /** The update server is busy. */
    public static final int UPDATE_SERVER_BUSY = 14;
    /** 
     * The http request failed. The <code>getParam()</code> method returns the 
     * HTTP response code.
     */
    public static final int UPDATE_REQUEST_ERROR = 15;
    /** 
     * The payment update file has an invalid or missing MIME-type. The 
     * <code>getParam()</code> method returns this type.
     */
    public static final int INVALID_UPDATE_TYPE = 16;
    /** 
     * The payment update file has an unsupported character set. The 
     * <code>getParam()</code> method returns the character set name.
     */
    public static final int UNSUPPORTED_UPDATE_CHARSET = 17;
    /** 
     * The provider's certificate has expired or is not yet valid. The 
     * <code>getParam()</code> method returns the subject name of the 
     * certificate.
     */
    public static final int EXPIRED_PROVIDER_CERT = 18;
    /** 
     * The public key of the provider's root CA has expired. The 
     * <code>getParam()</code> method returns the subject name of the CA 
     * certificate.
     */
    public static final int EXPIRED_CA_CERT = 19;
    /** 
     * The provider's certificate is invalid. The <code>getParam()</code> method
     * returns the subject name of the certificate. 
     */
    public static final int INVALID_PROVIDER_CERT = 20;
    /** 
     * The payment update file does not contain any certification chain which 
     * can be verified. 
     */
    public static final int NO_TRUSTED_CHAIN = 21;
    /** The verification of the payment update file's signature has failed. */
    public static final int SIGNATURE_VERIFICATION_FAILED = 22;
    /** The payment update file has an invalid properties file format. */
    public static final int INVALID_PROPERTIES_FORMAT = 23;

    // the reason for the exception
    private int reason;
    // an additional string param
    private String param;

    /**
     * Creates an instance of the <code>PaymentException</code> class with the 
     * given reason.
     *
     * @param reason the reason
     */
    public PaymentException(int reason) {
        this.reason = reason;
    }

    /**
     * Creates an instance of the <code>PaymentException</code> class with the 
     * given reason and the detail message.
     *
     * @param reason the reason
     * @param detail the detail message
     */
    public PaymentException(int reason, String detail) {
        super(detail);
        this.reason = reason;
    }
    
    /**
     * Creates an instance of the <code>PaymentException</code> class with the 
     * given reason, an additional string value which meaning depends on the 
     * reason and the detail message.
     *
     * @param reason the reason
     * @param param the string value
     * @param detail the detail message
     */
    public PaymentException(int reason, String param, String detail) {
        super(detail);
        this.reason = reason;
        this.param = param;
    }

    /**
     * Returns the reason for the exception as a number.
     *
     * @return the reason
     */
    public final int getReason() {
        return reason;
    }

    /**
     * Sets an additional string value which depends on the reason for the 
     * exception.
     *
     * @param param the string value
     * @see #getParam
     */
    public final void setParam(String param) {
        this.param = param;        
    }

    /**
     * Returns an additional string value which depends on the reason for the 
     * exception.
     *
     * @return the string value
     * @see #setParam
     */
    public final String getParam() {
        return param;        
    }

    /**
     * Returns the detail message for the exception.
     *
     * @return the detail message
     */
    public final String getDetail() {
        return super.getMessage();
    }
    
    /**
     * Returns the full description of the exception. It uses the reason code,
     * the param value and the detail message to construct the description.
     *
     * @return the full description of the exception
     */
    public final String getMessage() {
        String message;
        
        switch (reason) {
            case UNSUPPORTED_PAYMENT_INFO:
                message = "Unsupported version of the payment information (" + 
                        param + ")";
                break;
            case MISSING_MANDATORY_ATTRIBUTE:
                message = "The required " + param + " attribute is missing";
                break;
            case INVALID_ATTRIBUTE_VALUE:
                message = "The " + param + " attribute contains an " +
                        "invalid value";
                if (super.getMessage() != null) {
                    message += " (" + super.getMessage() + ")";
                }
                break;
            case UNSUPPORTED_ADAPTERS:
                message = "None of the adapters is supported (" 
                        + param + ")";
                break;
            case UNSUPPORTED_PROVIDERS:
                message = "None of the providers is supported (" 
                        + param + ")";
                break;
            case UNSUPPORTED_URL_SCHEME:
                message = "The update URL has an unsupported scheme (" 
                        + param + ")";
                break;
            case INFORMATION_NOT_YET_VALID:
                message = "Payment information is not yet valid";
                break;
            case INFORMATION_EXPIRED:
                message = "Payment information is expired";
                break;
            case INCOMPLETE_INFORMATION:
                message = "The payment information is incomplete";
                break;
            case INVALID_ADAPTER_CONFIGURATION:
                message = "The " + param + " attribute contains an " +
                        "invalid adapter configuration string";
                if (super.getMessage() != null) {
                    message += " (" + super.getMessage() + ")";
                }
                break;
            case INVALID_PRICE_INFORMATION:
                message = "The " + param + " attribute contains " +
                        "invalid payment specific price information";
                if (super.getMessage() != null) {
                    message += " (" + super.getMessage() + ")";
                }
                break;
            case INVALID_UPDATE_URL:
                message = "The update URL " + param + " is invalid";
                break;
            case UPDATE_SERVER_NOT_FOUND:
                message = "The server for the payment update was not found " +
                        "at the URL " + param;
                break;
            case UPDATE_NOT_FOUND:
                message = "The payment update file was not found at the URL " +
                        param;
                break;
            case UPDATE_SERVER_BUSY:
                message = "The payment update server is busy";
                break;
            case UPDATE_REQUEST_ERROR:
                message = "The payment update request has been denied by the " +
                        "update server (HTTP response code = " + param + ")";
                break;
            case INVALID_UPDATE_TYPE:
                message = "The payment update file has a missing or " +
                        "incorrect type (" + param + ")";
                break;
            case UNSUPPORTED_UPDATE_CHARSET:
                message = "The payment update file is in an unsupported " +
                        "character set (" + param + ")";
                break;
            case EXPIRED_PROVIDER_CERT:
                message = "The provider certificate (" + param + ") is " +
                        "expired or not yet valid";
                break;
            case EXPIRED_CA_CERT:
                message = "The root CA's public key expired (" + param + ")";
                break;
            case INVALID_PROVIDER_CERT:
                message = "The provider certificate (" + param + ") is " +
                        "invalid or unsupported";
                break;
            case NO_TRUSTED_CHAIN:
                message = "Can't verify any provider certificate";
                break;
            case SIGNATURE_VERIFICATION_FAILED:
                message = "Verification of the payment update signature failed";
                break;
            case INVALID_PROPERTIES_FORMAT:
                message = "The payment update file is not a valid properties " +
                        "file";
                if (super.getMessage() != null) {
                    message += " (" + super.getMessage() + ")";
                }
                break;
            default:
                message = super.getMessage();
                break;
        }
        
        return message;
    }
}
