
/*
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

#include "javacall_mms.h"
#include "javacall_memory.h"
#include "javacall_logging.h"
#include "javacall_datagram.h"
#include "javacall_network.h"
#include "javacall_time.h"
#include <memory.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

static FILE* logFd = NULL;
#define LOG_FILE_NAME "mms_log.txt"

extern int smsOutPortNumber;
extern javacall_handle smsDatagramSocketHandle;
extern char *devicePhoneNumber;

extern int generatePayload(char *p, // Assume p is large enough ( > BODY + 300)
                  int headerLen,
                  const unsigned  char *header,
                  int bodyLen,
                  const unsigned char* body );

extern int encodeMmsBuffer(long timestamp,
                               int current_fragment,
                               int total_fragments,
                               unsigned  char *encode_mms_buffer,
                               int MMS_BUFF_LENGTH,
                               int payloadLen,
                               const unsigned char* payload,
                               const char *toAddr,
                               const char *appID);

extern char* getIPBytes_nonblock(char *hostname);

static void printToLog(unsigned char* buf, int len) {
#if 0
    if(logFd == NULL) {
        //FILE *fopen(const char *filename,const char *mode)
        logFd = fopen(LOG_FILE_NAME, "w");
        if(logFd == NULL) {
            return;
        }
    }
    //size_t fwrite(const void *buffer, size_t size, size_t count,FILE *stream);
    fwrite(buf, 1, len, logFd);
#else
    unsigned char* buf1 = (unsigned char*)javacall_malloc(len+1);
    memcpy(buf1, buf, len);
    buf1[len] = 0;
    javautil_debug_print (JAVACALL_LOG_INFORMATION, "mms", buf1);
    javacall_free(buf1);
#endif
}



 /**
  * checks if the Multimedia Message Service (MMS) is available, and MMS
  * messages can be sent and received
  *
  * @return <tt>JAVACALL_OK</tt> if MMS service is avaialble
  *         <tt>JAVACALL_FAIL</tt> or negative value otherwise
  */
javacall_result javacall_mms_is_service_available(void) {

    return JAVACALL_OK;
}

extern char* getProp(const char* propName, char* defaultValue);
extern int getIntProp(const char* propName, int defaultValue);

/**
 * sends an MMS message
 *
 * The MMS message header and body have to conforms to the message
 * structure in D.1 and D.2 of JSR205 spec.
 *
 * @param headerLen The length of the message header.
 * @param header The message header should include Subject, DeliveryData,
 *          Priority, From, To, Cc and Bcc.
 *          If the MMS message is for Java applications, Application-ID
 *          and Reply-To-Application-ID are added to the Content-Type
 *          header field as additional Content-Type parameters.
 * @param bodyLen The length of the message body.
 * @param body The message body.
 *        The MMS message body is composed of one or more message parts.
 *        The following fields are in the message part structure:
 *          MIME-Type - the MIME Content-Type for the Message Part [RFC 2046]
 *          content-ID - the content-id header field value for the Message Part [RFC 2045].
 *              The content-id is unique over all Message Parts of an MMS and must always be set
 *          content-Location - the content location which specifies the  name of the
 *              file that is attached. If  the content location is set to null,
 *              no content location will be set for this message part.
 *          contents -  the message contents of the message part
 * @param pHandle of sent mms
 *
 * @return <tt>JAVACALL_OK</tt> if send request success
 *         <tt>JAVACALL_FAIL</tt> or negative value otherwise
 *
 * Note: javanotify_mms_send_completed() needs to be called to notify
 *       completion of sending operation.
 *       The returned handle will be passed to javanotify_mms_send_completed( ) upon completion
 */

 #define BUF_LEN 256
 char buffer[BUF_LEN];

//IMPL_NOTE: dynamically handle messages larger than MMS_BUFF_LENGTH
 #define MMS_BUFF_LENGTH 1500
 char encode_mms_buffer[MMS_BUFF_LENGTH];



/*
  *  builds the MMS header and body in a format similar to WMA console "send"
  *  the string output is a header partially divided by "\n"s at the begininng
  *  and other bard is chained in UTF format,
  *  at the end the body is chained as well after parsing the "nEntries" value
  */
javacall_result javacall_mms_send(
    					int headerLen,
                        const char* header,
                        int bodyLen,
                        const unsigned char* body,
                        const char *toAddr,
                        const char *appID,
                        int handle) {
    javacall_result ok;
    int pBytesWritten = 0;
    void *pContext;
    unsigned char *pAddress;
    static int mmsID = 0;
    long timestamp;
    int mmsFragments, i;
    char *payload;
    int payloadLen;


    javacall_int64 timeStamp = 0;
    int encodedMMSLength = 0;
    char* IP_text = getProp("JSR_205_DATAGRAM_HOST", "127.0.0.1");

	javacall_network_init_start();
   	pAddress = getIPBytes_nonblock(IP_text);


    timestamp = (long)javacall_time_get_milliseconds_since_1970();

    payload = malloc(bodyLen + headerLen );
	memset(payload,0,bodyLen + headerLen );

    payloadLen = generatePayload( payload,
                  headerLen,
                  header,
                  bodyLen,
                  body );


    mmsFragments = javacall_mms_get_number_of_segments(payload, payloadLen);

    for( i = 0; i < mmsFragments; i++ ) {

	encodedMMSLength = encodeMmsBuffer (
                                    timestamp,
                                    i,
                                    mmsFragments,
                                    encode_mms_buffer,
                                    MMS_BUFF_LENGTH,
                                    payloadLen,
                                    payload,
                                    toAddr,
                                    appID);

		//IMPL_NOTE: ERROR CHECK
    	    ok = javacall_datagram_sendto_start(smsDatagramSocketHandle, pAddress, smsOutPortNumber,
    	        encode_mms_buffer, encodedMMSLength, &pBytesWritten, &pContext);

    	    if (ok != JAVACALL_OK) {
    	        javautil_debug_print (JAVACALL_LOG_ERROR, "mms", "Error: MMS sending - datagram blocked.\n");
    	    }


    	javautil_debug_print (JAVACALL_LOG_INFORMATION, "mms", "## javacall: MMS sending...\n");

    }
    mmsFragments = 0;
    free(payload);

	javanotify_mms_send_completed(JAVACALL_OK, handle);

	return JAVACALL_OK;
}

/**
 * Requests to fetch the incoming MMS message.
 *
 * This function requests to fetch MMS message and should return quickly.
 * After a MMS indication was notified, this API requests the platform to retrieve the MMS message body.
 *
 * @param handle of available MMS message
 * @param fetch if JAVACALL_TRUE, the platform should fetch the MMS message
 *          body from the network and call javanotify_incoming_mms().
 *          Otherwise, the MMS message body should be discarded.
 *
 * @return <tt>JAVACALL_OK</tt> if fetch request success
 *         <tt>JAVACALL_FAIL</tt> or negative value otherwise
 */
javacall_result javacall_mms_fetch(javacall_handle handle, javacall_bool fetch) {

   memset(buffer, 0, BUF_LEN);
   sprintf(buffer, "\nmms.c: javacall_mms_fetch() called with handle=%d fetch=%d\n", handle, (int)fetch);
   printToLog(buffer, strlen(buffer));

    return JAVACALL_OK;
}


#define APP_ID_MAX 8
static char* appIDList[APP_ID_MAX] = {0,0,0,0,0,0,0,0};

/**
 * The platform must have the ability to identify the target application of incoming
 * MMS messages, and delivers messages with application ID to the WMA implementation.
 * If this application ID has already been registered either by a native
 * application or by another WMA application, then the API should return an error code.
 *
 * @param appID The application ID associated with the message.
 * @return <tt>JAVACALL_OK</tt> if started listening, or
 *         <tt>JAVACALL_FAIL</tt> or negative value if unsuccessful
 */
javacall_result javacall_mms_add_listening_appID(const char* appID) {

    int i;
    int free = -1;
    for (i=0; i<APP_ID_MAX; i++) {
        if (appIDList[i] == NULL) {
            free = i;
            continue;
        }
        if (0 == strcmp(appIDList[i], appID)) {
            return JAVACALL_FAIL;
        }
    }

    if (free == -1) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "mms", "appID amount exceeded");
        return JAVACALL_FAIL;
    }

    appIDList[free] = strcpy((char*)javacall_malloc(strlen(appID)), appID);

    memset(buffer, 0, BUF_LEN);
    sprintf(buffer, "\nmms.c: javacall_mms_add_listening_appID() called with appID=%s\n", appID);
    printToLog(buffer, strlen(buffer));

    return JAVACALL_OK;
}

/**
 * Stops listening to an application ID.
 * After unregistering an application ID, MMS messages received by the device
 * for the specified application ID should not be delivered to the WMA
 * implementation.
 * If this API specifies an application ID which is not registered, then it
 * should return an error code.
 *
 * @param appID The application ID to stop listening to
 * @return <tt>JAVACALL_OK </tt> if stopped listening to the application ID,
 *          or <tt>0</tt> if failed, or the application ID not registered
 */
javacall_result javacall_mms_remove_listening_appID(const char* appID) {

    int i;
    for (i=0; i<APP_ID_MAX; i++) {
        if (appIDList[i] == NULL) {
            continue;
        }
        if (0 == strcmp(appIDList[i], appID)) {
            appIDList[i] = NULL;
            return JAVACALL_OK;
        }
    }

    memset(buffer, 0, BUF_LEN);
    sprintf(buffer, "\nmms.c: javacall_mms_remove_listening_appID() called with appID=%s\n", appID);
    printToLog(buffer, strlen(buffer));

    return JAVACALL_FAIL;
}

javacall_result javacall_is_mms_appID_registered(const char* appID) {
    int i;
    for (i=0; i<APP_ID_MAX; i++) {
        if (appIDList[i] == NULL) {
            continue;
        }
        if (0 == strcmp(appIDList[i], appID)) {
            return JAVACALL_OK;
        }
    }
    return JAVACALL_FAIL;
}

/**
 * Computes the number of transport-layer segments that would be required to
 * send the given message.
 *
 * @param msgBuffer The message to be sent.
 * @param msgLen The length of the message.
 * @return The number of transport-layer segments required to send the message.
 */
int javacall_mms_get_number_of_segments(unsigned char msgBuffer[], int msgLen) {

    int result;

    if (msgLen <= 1200) {
        result =  1;
    } else {
        result = (msgLen + 1150 -1)/ 1150;  // Hardcoded, same calculation as in Emulator
    }

    memset(buffer, 0, BUF_LEN);
    sprintf(buffer, "\nmms.c: javacall_mms_get_number_of_segments() called with msgLen=%d\n", msgLen);
    printToLog(buffer, strlen(buffer));

    return result;
}

/**
 * Gets the phone number of device
 *
 * @return The phone number of device.
 */
javacall_utf16_string javacall_mms_get_internal_phone_number() {
    static javacall_utf16 internal_phone_number[256];
    unsigned int i;

    for(i=0; i <= strlen(devicePhoneNumber); i++ ){
        internal_phone_number[i] = devicePhoneNumber[i];
    }
    return internal_phone_number;
}
