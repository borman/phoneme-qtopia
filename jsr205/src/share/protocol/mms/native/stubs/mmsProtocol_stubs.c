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

#include <string.h>
#include <jsr205_mms_protocol.h>
#include <jsr205_mms_pool.h>
#include <pcsl_string.h>

/**
 * Register an application identifier for receiving MMS messages.
 * <p>
 * This API registers a MMS message which matches the registered application
 * identifier. According to the MMS standard, the application ID is a string
 * and a special field in the MMS message header. Its use is similar to the SMS
 * port number for transferring the MMS to a specified WMA application. After
 * registering the special application ID, the target device should check the
 * MMS header to forward the MMS whose application ID matches the registered
 * application ID to the correct MIDP WMA. If this application ID is used by
 * another WMA or target device native application, then this API should return
 * an error code.
 *
 * @param appNumber The null-terminated application identifier string.
 *
 * @return <code>WMA_OK</code> when successful or
 *	<code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_add_mms_listening_appID(unsigned char* appID) {
    (void)appID;
    return WMA_OK;
}

/**
 * Clear a registered application identifier from receiving MMS messages.
 * <p>
 * This API removes a registered message application identifier. After removing
 * this application ID, MIDP WMA should no longer receive incoming MMS messages
 * matching this application ID. If the specified application ID has not been
 * registered, then this API should return an error code.
 *
 * @param appNumber The null-terminated application identifier string.
 *
 * @return <code>WMA_OK</code> when successful or
 *	<code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_remove_mms_listening_appID(unsigned char* appID) {
    (void)appID;
    return WMA_OK;
}

/**
 * Incoming MMS Message.
 * <p>
 * After a WMA application confirms the fetching of an MMS message body, the
 * native protocol layer of the target device should fetch the MMS message body
 * from the network and forward it to MIDP WMA. The message should be forwarded
 * as an asynchronous message. The incoming MMS should be complete and include
 * both the MMS message header and all message parts. For more information
 * about the message header and message parts, refer to Section 2.3.3. The
 * incoming MMS can be sent to WMA by an asynchronous message or by calling a
 * callback function.
 * <P>
 * Note: if There are any WMA MMS notifications in the native mailbox when the
 * VM is launched, those notifications are expected to be delivered by the first
 * WMA application which registers those messages.
 *
 * @param msgBuffer The incoming message.
 */
void jsr205_notify_incoming_mms(void* msgBuffer) {
    (void)msgBuffer;
}

/**
 * Send a Multimedia Message.
 * <p>
 * This API sends an MMS message. According to the JSR 205 specification, one
 * MMS includes a message header and a message body. There can be more than one
 * message part in an MMS message body. MIDP WMA layer. The customer of this API
 * does not have to care about the details of the MMS PDU protocol
 * (WAP-209-MMS-Encapsulation standard). The native protocol of the target
 * device should package the MMS into the MMS PDU and send it to the network.
 * The customer should also tell the MIDP WMA the maximum message length which
 * the target device can support.
 * <p>
 * Sending an MMS message to the network has a long life process. To avoid
 * blocking the JVM for a long time, this API should work in asynchronous mode.
 * It will transfer the parameter to the native software platform and return
 * quickly. The native software platform should take the responsibility for
 * handling the entire communication session and use an asynchronous message or
 * callback function to notify the MIDP WMA of the result.
 *
 * @param toAddr The recipient's MMS address.
 * @param fromAddr The sender's MMS address.
 * @param appID The application ID associated with the message.
 * @param replyToAppID The application ID to which replies can be sent.
 * @param headerLen The length of the message header.
 * @param header The message header.
 * @param bodyLen The length of the message body.
 * @param body The message body.
 * @param bytesSent The number of bytes sent.
 *
 * @return <code>WMA_OK</code> when successful or
 *	<code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_send_mms(char* toAddr, char* fromAddr, char* appID,
    char* replyToAppID, jint headerLen, char* header, jint bodyLen,
    char* body, /* OUT */jint* bytesSent, /* OUT */void **pContext) {

    (void)toAddr;
    (void)fromAddr;
    (void)appID;
    (void)replyToAppID;
    (void)headerLen;
    (void)header;
    (void)bodyLen;
    (void)body;
    (void)bytesSent;
    (void)pContext;

    return WMA_ERR;
}

/**
 * Notify the native software platform that the send operation has completed.
 * This allows the native platform to perform any special operations, too.
 *
 * @param bytesSent Number of bytes sent or <code>-1</code> if there was a problem.
 */
void jsr205_mms_notify_send_completed(/* OUT */ jint *bytesSent) {
    (void)bytesSent;
}

/**
 * When an incoming MMS message arrives in the MMS proxy, the proxy should send
 * a notification to the MMS client (target device). The notification includes
 * only the MMS header. The MMS client decides if it must fetch the message body
 * of this MMS from the network. If the target device finds that the incoming MMS
 * notification is for MIDP WMA, it should send a notification to WMA and wait
 * for a confirmation from WMA. This notification can be sent to WMA by an
 * asynchronous message or by calling a callback function.
 *
 * @param msgHeader The message header contents
 */
void MMSNotification(void* msgHeader) {
    (void)msgHeader;
}

/**
 * Confirm to fetch the incoming MMS.
 * <p>
 * AFter WMA gets the MMS message notification (message header), this callback
 * function provides a confirmation to native software platform for fetching
 * the MMS message body. If the return value is <code>true</code>, the target
 * device should fetch the MMS message body from the network and forward it to
 * WMA; otherwise, the MMS message body should be discarded.
 *
 * @return <code>WMA_OK</code> if the target device should fetch the MMS
 *     message body from the network; <code>WMA_ERR</code>, if the message
 *     body should be discarded.
 */
WMA_STATUS jsr205_fetch_mms() {

    return WMA_OK;
}

/**
 * Computes the number of transport-layer segments that would be required to
 * send the given message. Text payloads should already be encoded by the
 * time this method is called.
 *
 * @param msgBuffer The message payload to be sent.
 * @param msgLen The length of the message.
 * @param msgType The message type: binary or text.
 * @param hasPort Indicates if the message includes a source or destination port
 *	number.
 * @param numSegments The number of transport-layer segments required to send
 *      the message.
 *
 * @return <code>WMA_OK</code> when successful or
 *	<code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_number_of_mms_segments(unsigned char msgBuffer[],
    jint msgLen, jint msgType, jboolean hasPort, /* OUT */jint* numSegments) {

    /** Extra header size for concatenated messages. */
    jint headerSize = 7;

    /** The fragment size for each part to be sent. */
    jint fragmentSize = 1200;

    /** Not used in this implementation. */
    (void)msgType;
    (void)hasPort;

    /** The default number of segments required to send the message. */
    *numSegments = 1;

    if (msgBuffer == NULL) {
    	return WMA_OK;
    }

    if (msgLen < fragmentSize) {
    	*numSegments = 1;
    } else {
    	fragmentSize = fragmentSize - headerSize;
    	*numSegments = (msgLen + fragmentSize - 1) / fragmentSize;
    }

    return WMA_OK;
}

/**
 * Fetches the first message that matches the application identifier without removing
 * the message from the pool.
 *
 * @param appID The application identifier to be matched.
 * @param isNew Get the new message only when not 0.
 *
 * @return The MMS message or <code>NULL</code> if no match could be found.
 */
MmsMessage* jsr205_mms_pool_peek_next_msg1(unsigned char* appID, jint isNew) {
    (void)appID;
    (void)isNew;
    return NULL;
}

/**
 * Gets the phone number of device
 *
 * @return The phone number of device.
 * This function returns PCSL_STRING_NULL on pcsl error.
 * Note: the caller should free return value of this function 
 * after using.
 */
pcsl_string getInternalPhoneNumber(void) {
    return PCSL_STRING_NULL;
}
