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
#include <jsr120_types.h>

#include <jsr205_mms_protocol.h>
#include <jsr205_mms_listeners.h>
#include <pcsl_memory.h>

typedef struct {
    /** Message buffer */
    char *msg;
    /** The total length of the message. */
    int msgLen;
    /** Index in the list of recipients */
    int listIndex;
    /** Length of the list of recipients */
    int listLength;
    /** The list of recipients being currently processed. */
    char *list;
    /**
     * <code>true</code> if address is to this device; <code>false</code>,
     * otherwise.
     */
    int talkToSelf;
    /** The pointer into the UTF-8 buffer. */
    char* utfp;
    /** The pointer past the last byte in the UTF-8 buffer. */
    char* utfend;
    /** Emuator context */
    void *emulatorContext;
} MMSMessageContext;

extern int is_device_phone_number(char* phoneNumber);
extern WMA_STATUS jsr205_mms_write(jint sendingToSelf, char* toAddress,
    char* fromAddress, char* appID, char* replyToAppID, jint msgLen,
    char* msg, jint* bytesWritten, void **pContext);

void initUTF(char* buf, int length, MMSMessageContext *context) {
    context->utfp = buf;
    context->utfend = context->utfp + length;
} 

/**
 * Read a UTF-8 string. Note: This is not a full UTF-8 reader and just handles
 * simple cases.
 */
char* readUTF(MMSMessageContext *context) {
    char* s;
    int len;

    if (context->utfp >= context->utfend) {
        /* Attempt to read beyond stream length. */
        return NULL;
    }

    /* Pick up the length and advance the pointer beyond the length. */
    len = ((unsigned char)*context->utfp << 8) | (unsigned char)(*(context->utfp+1));
    context->utfp += 2;

    /* Pick up the string and advance the pointer beyond the string. */
    s = (char*)pcsl_mem_malloc(len + 1);
    strncpy(s, context->utfp, len);
    *(s + len) = '\0';  /* terminator */
    context->utfp += len;

    return s;
}

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
 *     <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_add_mms_listening_appID(unsigned char* appID) {
    (void)appID;

    /* The message identifier has been subscribed by other WMA applications. */
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
 *     <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_remove_mms_listening_appID(unsigned char* appID) {
    (void)appID;

    /* The message identifier has not been subscribed. */
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
void jsr205_notify_incoming_mms(char* fromAddress, char* appID,
    char* replyToAppID, int msgLen, unsigned char* msgBuffer) {

    (void)fromAddress;
    (void)appID;
    (void)replyToAppID;
    (void)msgLen;
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
 * @param handle The handle of MMS.
 * @param pContext pointer where to save context of asynchronous operation.
 *
 * @return <code>WMA_OK</code> when successful or
 *     <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_send_mms(char* toAddr, char* fromAddr, char* appID,
    char* replyToAppID, jint headerLen, char* header, jint bodyLen,
    char* body, int handle, /* OUT */void **pContext) {

    /** The status from writing the message. */
    WMA_STATUS status = WMA_NET_IOERROR;

    /** Context of asynchronous operation */
    MMSMessageContext *context;

    /* The actual number of bytes written. */
    int bytesSent = 0;

    if (*pContext == NULL) {
        context = (MMSMessageContext *)pcsl_mem_malloc(sizeof(*context));

        if (context == NULL) {
            return -1;
        }

        memset(context, 0, sizeof(*context));

        /* This implementation combines the header and body. */
        context->msgLen = headerLen + bodyLen;
        context->msg = (char*)pcsl_mem_malloc(context->msgLen * sizeof(char));
        memcpy(context->msg, header, headerLen);
        memcpy(context->msg + headerLen, body, bodyLen);

        *pContext = context;

        /* Prepare to walk through all addresses in "To", "Cc" and "Bcc" lists. */
        initUTF(header, headerLen, context);

    } else {
        context = *pContext;

        /** Finish asynchronous operation */
        status = jsr205_mms_write(context->talkToSelf, toAddr, fromAddr, appID,
                                  replyToAppID, context->msgLen, context->msg, &bytesSent,
                                  &context->emulatorContext);
    }

    /*
     * Process address lists for "To", "Cc" and "Bcc" lists. Whenever this
     * device's phone number is located in any of those lists, indicate that
     * this phone is talking to itself when sending the message.
     */
    while (status != WMA_NET_WOULDBLOCK && status != WMA_NET_INTERRUPTED ) {
        char* semicolon = NULL;
        char* phoneNumber = NULL;
        int phoneNumberLen = 0;

        /** If the current list of recipients is over obtain the next one. */
        if (context->listIndex >= context->listLength) {

            if (context->list != NULL) {
                /* Free memory used by the address list we just processed. */
                pcsl_mem_free(context->list);
            }

            /* Search for header field containing the list. */
            while ((context->list = readUTF(context)) != NULL) {

                if ((strcmp(context->list, "To") == 0) ||
                    (strcmp(context->list, "Cc") == 0) ||
                    (strcmp(context->list, "Bcc") == 0)) {

                    /* Free memory used by the name. */
                    pcsl_mem_free(context->list);

                    /* Pick up the address list. */
                    context->list = readUTF(context);
                    if (context->list != NULL) {
                        context->listLength = strlen(context->list);
                        context->listIndex = 0;
                    }

                    /* Proceed with send. */
                    break;
                } else {
                    /* Free memory used by header name we just skipped. */
                    pcsl_mem_free(context->list);
                }
            }

            if (context->list == NULL) {
                /* No more addresses. */
                break;
            }
        }


        /* Determine the length of the phone number. */
        semicolon = strchr(context->list + context->listIndex, ';');
        if (semicolon != NULL) {
            phoneNumberLen = semicolon - (context->list + context->listIndex);
        } else {
            phoneNumberLen = context->listLength - context->listIndex;
        }

        if (phoneNumberLen > 0) {

            /*
             * Extract the phone number from the list. The phone number
             * may have surrounding whitespace, which gets ignored by
             * the check for "talkToSelf."
             */
            phoneNumber = (char*)pcsl_mem_malloc(phoneNumberLen + 1);
            strncpy(phoneNumber, context->list + context->listIndex, phoneNumberLen);
            *(phoneNumber + phoneNumberLen) = '\0';  /* terminator */

            /*
             * If this phone number is this device's phone number, then
             * the message is being sent to the device itself and must
             * be handled differently.
             */
            context->talkToSelf = (is_device_phone_number(phoneNumber) == WMA_OK);

            pcsl_mem_free(phoneNumber);

            /*
             * Sends the message, gets the number of bytes written or
             * failure status in return.
             */
            status = jsr205_mms_write(context->talkToSelf, toAddr, fromAddr, appID,
                                      replyToAppID, context->msgLen, context->msg, &bytesSent,
                                      &context->emulatorContext);
        }

        context->listIndex = context->listIndex + phoneNumberLen + 1;
    }


    if (status != WMA_NET_WOULDBLOCK) {
        /* Free all context structures if operation is over. */
        if (status == WMA_NET_INTERRUPTED) {
            /* Free address list memory if operation is aborted. */
            pcsl_mem_free(context->list);
        }

        pcsl_mem_free(context->msg);
        pcsl_mem_free(context);
    }

    if (status == WMA_NET_SUCCESS) {
        status = WMA_OK;
    } else if (status != WMA_NET_WOULDBLOCK) {
        status = WMA_ERR;
    }

    jsr205_mms_notify_send_completed(handle, status);
    return status;
}

/**
 * Notify the native software platform that the send operation has completed.
 * This allows the native platform to perform any special operations, too.
 *
 * @param bytesSent Number of bytes sent or <code>-1</code> if there was a problem.
 */
void jsr205_mms_notify_send_completed(int handle, WMA_STATUS status) {

    jsr205_mms_message_sent_notifier(handle, status);
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
 *     message body. <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_fetch_mms(char* appID) {

    (void)appID;
    /* For this implementation, always return true. */
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
 * @param hasPort Indicates if the message includes a source or destination
 *     port number.
 * @param numSegments The number of transport-layer segments required to send
 *     the message.
 *
 * @return <code>WMA_OK</code> when successful or
 *     <code>WMA_ERR</code> if an error occurred.
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

