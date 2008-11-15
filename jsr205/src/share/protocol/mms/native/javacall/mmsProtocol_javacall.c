/*
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

#include <jsr205_mms_protocol.h>
#include <jsr205_mms_listeners.h>
#include <pcsl_memory.h>
#include <javacall_defs.h>
#include <javacall_mms.h>
#include <string.h>

/**
 * Register an application identifier for receiving MMS messages.
 * <p>
 * This API registers a MMS message which matches the registered application
 * identifier. According to the MMS standard, the application identifier is a
 * string and a special field in the MMS message header. Its use is similar to
 * the SMS port number for transferring the MMS to a specified WMA application.
 * After registering the special application ID, the target device should check
 * the MMS header to forward the MMS whose application ID matches the registered
 * application ID to the correct MIDP WMA. If this application ID is used by
 * another WMA or target device native application, then this API should return
 * an error code.
 *
 * @param appID The application identifier associated with the message..
 *
 * @return <code>WMA_OK</code> when successful or
 *      <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_add_mms_listening_appID(unsigned char* appID) {

    javacall_result result = javacall_mms_add_listening_appID((char*)appID);

    return (result == JAVACALL_OK) ? WMA_OK : WMA_ERR;
}

/**
 * Clear a registered application identifier from receiving MMS messages.
 * <p>
 * This API removes a registered message application identifier. After removing
 * this application ID, MIDP WMA should no longer receive incoming MMS messages
 * matching this application ID. If the specified application ID has not been
 * registered, then this API should return an error code.
 *
 * @param appID The application identifier associated with the message..
 *
 * @return <code>WMA_OK</code> when successful or
 *      <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_remove_mms_listening_appID(unsigned char* appID) {

    javacall_result result = javacall_mms_remove_listening_appID((char*)appID); 

    return (result == JAVACALL_OK) ? WMA_OK : WMA_ERR;
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
 * @param fromAddress The sender's MMS address.
 * @param appID The application ID associated with the message.
 * @param replyToAppID The application ID to which replies can be sent.
 *
 * @param bodyLen The length of the message.
 * @param body The message with header.
 */
void jsr205_notify_incoming_mms(char* fromAddress, char* appID,
        char* replyToAppID, int msgLen, unsigned char* msgBuffer) {

    if (WMA_OK == jsr205_mms_is_message_expected(appID, replyToAppID)) {

        MmsMessage* mms = jsr205_mms_new_msg(fromAddress, appID,
            replyToAppID, msgLen, msgBuffer);

        jsr205_mms_pool_add_msg(mms);

        /* Notify all listeners of the new message. */
        jsr205_mms_message_arrival_notifier(mms);
    }
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
 * handling the entire communcation session and use an asynchronous message or
 * callback function to notify the MIDP WMA of the result.
 *
 * @param toAddress The recipient's MMS address.
 * @param fromAddress The sender's MMS address.
 * @param appID The application ID associated with the message.
 * @param replyToAppID The application ID to which replies can be sent.
 * @param headerLen The length of the message header.
 * @param header The message header.
 * @param bodyLen The length of the message body.
 * @param body The message body.
 * @param bytesSent The number of bytes that were sent.
 * @param pContext pointer where to save context of asynchronous operation.
 *
 * @return <code>WMA_OK</code>, <code>WMA_NET_WOULDBLOCK</code> or
 * <code>WMA_ERR</code>.
 */
WMA_STATUS jsr205_send_mms(char* toAddr, char* fromAddr, char* appID,
    char* replyToAppID, jint headerLen, char* header, jint bodyLen,
    char* body, int handle, /* OUT */void **pContext) {

    javacall_result result = javacall_mms_send((int)headerLen,
                                         (const char*)header,
                                         (int)bodyLen,
                                         (const unsigned char*)body,
                                         toAddr,
                                         appID,
                                         handle);

    return (result == JAVACALL_OK) ? WMA_OK : WMA_ERR;

    (void)fromAddr;
    (void)replyToAppID;

    (void)pContext;
}

/**
 * Notify the native software platform that the send operation has completed.
 * This allows the native platform to perform any special operations, too.
 *
 * @param status indication of send completed status result: Either
 *         <tt>WMA_OK</tt> on success,
 *         <tt>WMA_ERR</tt> on failure
 * @param handle of available MMS returned by javacall_mms_send
 */
void jsr205_mms_notify_send_completed(int handle, WMA_STATUS status) {

    //called by javanotify_mms_send_completed(javacall_mms_sending_result result, javacall_handle handle)
    jsr205_mms_message_sent_notifier(handle, status);

    (void)handle;
    (void)status;
}

typedef struct available_mms {
    int handle;
    char* appID;
    struct available_mms* next;
} available_mms;
static available_mms* available_mms_list = NULL;

/**
 * When an incoming MMS message arrives in the MMS proxy, the proxy should send
 * a notification to the MMS client (target device). The notification includes
 * only the MMS header. The MMS client decides if it must fetch the message body
 * of this MMS from the network. If the target device finds that the incoming MMS
 * notification is for MIDP WMA, it should send a notification to WMA and wait
 * for a confirmation from WMA. This notification can be sent to WMA by an
 * asynchronous message or by calling a callback function.
 *
 * @param handle of available MMS
 * @param appID The application ID associated with the message.
 */
void jsr205_notify_mms_available(int handle, char* appID, char* fromAddr) {

    if (WMA_OK == jsr205_mms_is_midlet_listener_registered(appID)) {

        javacall_mms_fetch((javacall_handle)handle, JAVACALL_TRUE);

    } else if (WMA_OK == jsr205_mms_is_message_expected(appID, fromAddr)) {

        available_mms* mms;
        mms = pcsl_mem_malloc(sizeof(available_mms));
        mms->handle = handle;
        mms->appID = appID;
        mms->next = available_mms_list;
        available_mms_list = mms;

        jsr205_mms_message_available_notifier(appID, fromAddr);

    } else {
        javacall_mms_fetch((javacall_handle)handle, JAVACALL_FALSE);
    }
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
 * @param appID The application ID associated with the message.
 *
 * @return <code>WMA_OK</code> if the target device should fetch the MMS
 *     message body. <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_fetch_mms(char* appID) {

    javacall_result result = JAVACALL_FAIL;
    available_mms** ptr = &available_mms_list;
    while (*ptr) {
      if (strcmp((*ptr)->appID, appID) == 0) {
          available_mms* next = (*ptr)->next;
          result = javacall_mms_fetch((javacall_handle)(*ptr)->handle, JAVACALL_TRUE);
          //fetch_mms should be called from the same thread with notify_mms_available
          pcsl_mem_free(*ptr);
          *ptr = next;
       } else {
          ptr = &((*ptr)->next);
       }
    }

    return (result == JAVACALL_OK) ? WMA_OK : WMA_ERR;
}

/**
 * Returns the number of segments that would be needed in the underlying
 * protocol to send a specified message. The specified message is included as a
 * parameter of this function. Note that this method does not actually send the
 * message. It will only calculate the number of protocol segments needed for
 * sending the message.
 *
 * @param msgBuffer The message body.
 * @param msgLen Message body length (in bytes).
 * @param msgType Message type: Binary or Text.
 * @param hasPort indicates if the message includes source or destination port number.
 * @param numSegments The number of segments required to send the message.
 *
 * @return <code>WMA_OK</code> when successful or
 *      <code>WMA_ERR</code> if an error occurred.
 */
WMA_STATUS jsr205_number_of_mms_segments(unsigned char msgBuffer[],
    jint msgLen, jint msgType, jboolean hasPort, /* OUT */jint* numSegments) {

    int result = javacall_mms_get_number_of_segments(msgBuffer, msgLen);
    *numSegments = result;

    (void)msgType;
    (void)hasPort;

    return (result > 0) ? WMA_OK : WMA_ERR;
}

/**
 * Gets the phone number of device
 *
 * @return The phone number of device.
 */
/*
void getInternalPhoneNumber(jchar** result, int* str_len) {
    javacall_utf16_string phoneNumber = javacall_mms_get_internal_phone_number();
    *result = phoneNumber;
    *str_len = 0; while (*phoneNumber != 0) { *str_len += 1; phoneNumber++; }
}
*/
pcsl_string getInternalPhoneNumber(void) {
    pcsl_string retValue = PCSL_STRING_NULL;
    const char* phoneNumber = "911";
    pcsl_string_status status = PCSL_STRING_ERR;
    //load_var_char_env_prop((char**)&phoneNumber, "JSR_120_PHONE_NUMBER",
    //    "com.sun.midp.io.j2me.sms.PhoneNumber");
    if (phoneNumber != NULL) {
        status = pcsl_string_convert_from_utf8((const jbyte *)phoneNumber,
            strlen(phoneNumber), &retValue);
        if (status != PCSL_STRING_OK) {
            retValue = PCSL_STRING_NULL;
        }
    }
    return retValue;
}


#ifdef JSR205_ENABLE_JAVANOTIFY_STUBS
/**
 * A callback function to be called by platform to notify that an MMS
 * has completed sending operation.
 * The platform will invoke the call back in platform context for
 * each mms sending completion.
 *
 * @param result indication of send completed status result: Either
 *         <tt>JAVACALL_MMS_CALLBACK_SEND_SUCCESSFULLY</tt> on success,
 *         <tt>JAVACALL_MMS_CALLBACK_SEND_FAILED</tt> on failure
 * @param handle of available MMS
 */
void javanotify_mms_send_completed(
                        javacall_mms_sending_result result,
                        javacall_handle             handle) {
}

/**
 * callback that needs to be called by platform when an incoming MMS message arrives in the MMS proxy.
 *
 * The MMS message header have to conforms to the message
 * structure in D.1 and D.2 of JSR205 spec.
 *
 * @param handle of available MMS
 * @param headerLen The length of the message header.
 * @param header The message header should include Subject, DeliveryData,
 *          Priority, From, To, Cc and Bcc.
 *          If the MMS message is for Java applications, Application-ID
 *          and Reply-To-Application-ID are added to the Content-Type
 *          header field as additional Content-Type parameters.
 */
void javanotify_incoming_mms_available(
        javacall_handle         handle,
        int                     headerLen,
        const char*             header
        ) {
}

/**
 * callback that needs to be called by platform to handover an incoming MMS message intended for Java
 *
 * After this function is called, the MMS message should be removed from platform inbox
 *
 * The MMS message body have to conforms to the message
 * structure in D.1 and D.2 of JSR205 spec.
 *
 * @param handle of available MMS
 * @param bodyLen The length of the message body.
 * @param body The message body.
 *        The MMS message body is composed of one or more message parts.
 *        The following fields are in the message part structure:
 *          MIME-Type - the MIME Content-Type for the Message Part
 *          content-ID - the content-id header field value for the Message Part.
 *              The content-id is unique over all Message Parts of an MMS and must always be set
 *          content-Location - the content location which specifies the  name of the
 *              file that is attached. If  the content location is set to null,
 *              no content location will be set for this message part.
 *          contents -  the message contents of the message part
 */
void javanotify_incoming_mms(
        javacall_handle handle,
        int             bodyLen,
        const unsigned char*  body) {
}

/**
 * The (temporary?) callback function for the whole message.
 * The function contains all necessary information, it does not use the scenario below:
 *   -> javanotify_incoming_mms_available(hangle, header)
 *   <- javacall_mms_fetch(handle, ok)
 *   -> javanotify_incoming_mms(handle, body)
 */
void javanotify_incoming_mms_singlecall(
        char* fromAddress, char* appID, char* replyToAppID,
        int             bodyLen,
        unsigned char*  body) {
}
#endif
