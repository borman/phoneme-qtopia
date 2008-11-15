
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

/**
 * @file
 *
 * Simple implementation of wma UDP Emulator.
 * The messages supposed to be received from JSR205Tool.jar:
 */

#include "javacall_network.h"
#include "javacall_datagram.h"
#include "javacall_mms.h"
#include "javacall_logging.h"
#include "javacall_defs.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>

extern char *devicePhoneNumber;// char *

int cookiePortNumber = 0;
int cookieLen = 40;
char cookieBuffer [40];

unsigned char*  writeStringsInUTF(unsigned char* p, unsigned char *strPtr);
/**
	Parse recieved data in UTF format
    returns 0 if finished reading, 1 if there are more parts to read
*/
int decodeMmsBuffer(unsigned char *buffer,
    char** fromAddress, char** appID, char** replyToAppID,
    int* bodyLen, unsigned char** body) {


	unsigned char date[50];

	//segmentsHandling
	int	fragmentNum = 0;
	int fragmentSize  = 0;
	int fragmentOffset = 0;
	int totalSegments = 0;

	int internalBodyLen = 0;

	unsigned char *currentLine;
    unsigned char *nextLine;
    char *token, *value;
    char *temp;

	//Parse External Header
	currentLine = strtok( buffer, "\n");

	while(strstr(currentLine,"Buffer:")!=currentLine) {
        nextLine = currentLine+strlen(currentLine)+1;

        token = strtok(currentLine, ": ");
        value = strtok(NULL," ");


        if(strcmp(token,"Date")==0) {strcpy(date,value);}; //@IMPL_NOTE: Copy date value into body
        if(strcmp(token,"Content-Length")==0) {internalBodyLen = atoi(value);};
        if(strcmp(token,"Segments")==0) {totalSegments = atoi(value);};
        if(strcmp(token,"Fragment")==0) {fragmentNum = atoi(value);};
        if(strcmp(token,"Fragment-Size")==0) {fragmentSize = atoi(value);};
        if(strcmp(token,"Fragment-Offset")==0) {fragmentOffset = atoi(value);};
        if(strcmp(token,"Ack-Port")==0) {cookiePortNumber = atoi(value);};
        if(strcmp(token,"Ack-Cookie")==0) {
            strcpy(cookieBuffer,"ACK:");
            strcat(cookieBuffer,value );
            strcat(cookieBuffer,"\n");
            cookieLen = strlen(cookieBuffer) + 1;
        }

        if(strcmp(token,"SenderAddress")==0) {
			char *firstSep;
            *fromAddress = value;
			firstSep = strrchr(value, ':');
            temp = strrchr(*fromAddress, ':');
            if( (temp != NULL) && (temp != firstSep)) { // We also have an appID in the address
                 *replyToAppID = ++temp;
             }
        };

        if(strcmp(currentLine,"Address")==0) {
            temp = strrchr(value,':');
            if (temp!=NULL) {
				if(*temp!= 0)
					*appID=++temp;
            }
        };

        currentLine = strtok(nextLine,"\n");
	}

    nextLine = currentLine+strlen(currentLine)+1;
	//Handle Body

    if( fragmentNum == 0 ) { // First/only segment - allocate memory
        *body = malloc(internalBodyLen);
        *bodyLen = internalBodyLen;

        if (fragmentSize==0) { fragmentSize = internalBodyLen; };
        memcpy(*body,nextLine,fragmentSize);
    } else {
        memcpy(*body+fragmentOffset,nextLine,fragmentSize);
    }

    if( fragmentNum + 1 < totalSegments )
        return 1;
    else
        return 0;


}

int generatePayload( char *payload, // Assume p is large enough ( > BODY + 300)
                  int headerLen,
                  unsigned  char *header,
                  int bodyLen,
                  const unsigned char* body ) {

    memcpy(payload,header,headerLen);
    memcpy(payload+headerLen,body,bodyLen);
	return ( headerLen+bodyLen);
}
/**
	This method builds a string to send
	In order not to modify the java level and be compatible with the WMAConsole
	the parsing below startd with a "\n" delimiters and later adds values in UTF format
*/
int encodeMmsBuffer(long timestamp,
                               int current_fragment,
                               int total_fragments,
                               unsigned  char *encode_mms_buffer,
                               int MMS_BUFF_LENGTH,
                               int payloadLen,
                               const unsigned char* payload,
                               const char *toAddr,
                               const char *appID) {

	int buffer4intsLen = 16;
	char buffer4ints [16];
    int offset;
    int length;
    int header_length;

	memset(buffer4ints, 0, buffer4intsLen);
	memset(encode_mms_buffer, 0, MMS_BUFF_LENGTH);

//BUILDING TIHS STRING SEPARATED WITH "\n"s:
//"Date:1183900385453\nAddress:mms://:example.mms.MMSDemo\nSenderAddress:mms://+5555555555\nContent-Type:multipart\nContent-Length:381\nSegments:1\nBuffer:\n";

	//adding date-stamp

	sprintf(buffer4ints, "%lu", timestamp);

	strcat(encode_mms_buffer, "Date:");
	strcat(encode_mms_buffer, buffer4ints);
	strcat(encode_mms_buffer, "\n");

	strcat(encode_mms_buffer, "Address:mms://:");
    if (appID) {
        strcat(encode_mms_buffer, appID);
    }
	strcat(encode_mms_buffer, "\n");

	strcat(encode_mms_buffer, "SenderAddress:mms://");
	strcat(encode_mms_buffer, devicePhoneNumber);
	strcat(encode_mms_buffer, "\n");

	strcat(encode_mms_buffer, "Content-Type:");
	strcat(encode_mms_buffer, "multipart");
	strcat(encode_mms_buffer, "\n");

	memset(buffer4ints, 0, buffer4intsLen);
	sprintf(buffer4ints, "%d", payloadLen);

	strcat(encode_mms_buffer, "Content-Length:");
	strcat(encode_mms_buffer, buffer4ints);
	strcat(encode_mms_buffer, "\n");

	memset(buffer4ints, 0, buffer4intsLen);
	sprintf(buffer4ints, "%d", total_fragments );
	strcat(encode_mms_buffer, "Segments:");
	strcat(encode_mms_buffer, buffer4ints);
	strcat(encode_mms_buffer, "\n");

    if(total_fragments==1) {
        offset = 0;
        length = payloadLen;
    }    else {

        int fragment_size = 1150; // 1200 - 50, calculated in Emulator

        offset = current_fragment * fragment_size;
        length = ((current_fragment < total_fragments-1) ? fragment_size : payloadLen - (fragment_size* current_fragment));

        memset(buffer4ints, 0, buffer4intsLen);
        sprintf(buffer4ints, "%d", current_fragment );
        strcat(encode_mms_buffer, "Fragment:");
        strcat(encode_mms_buffer, buffer4ints);
        strcat(encode_mms_buffer, "\n");

        memset(buffer4ints, 0, buffer4intsLen);
        sprintf(buffer4ints, "%d", length );
        strcat(encode_mms_buffer, "Fragment-Size:");
        strcat(encode_mms_buffer, buffer4ints);
        strcat(encode_mms_buffer, "\n");

        memset(buffer4ints, 0, buffer4intsLen);
        sprintf(buffer4ints, "%d", offset );
        strcat(encode_mms_buffer, "Fragment-Offset:");
        strcat(encode_mms_buffer, buffer4ints);
        strcat(encode_mms_buffer, "\n");

    }

	strcat(encode_mms_buffer, "Buffer:\n");

    header_length = strlen(encode_mms_buffer);
    memcpy(encode_mms_buffer+header_length, payload+offset, length);

    return length + header_length;

}


/**
Adds into the "p" buffer pointer strings in UTF format as follow:
p[0]+p[1] = string length
p[2]...p[length+2] = string content

Returns the pointer to the end of the added string
Used by javacall_mms_send
*/
unsigned char*  writeStringsInUTF(unsigned char* p, unsigned char *strPtr) {
	int stringLen = strlen(strPtr);
	p[0]   = (unsigned char)((stringLen>>8) & 0xFF);
	p[1] = (unsigned char)((stringLen) & 0xFF);
	memcpy(&p[2], strPtr, stringLen);
	p = p+2+ stringLen;
	return p;
 	}
extern javacall_result javacall_is_mms_appID_registered(const char* appID);

javacall_result process_UDPEmulator_mms_incoming(javacall_handle handle,
                                                 unsigned char *pAddress,
int *port,
unsigned char *buffer,
int length,
int *pBytesRead,
void **pContext) {


    char* fromAddress = "fromAddress";
    char* appID = "appID";
    char* replyToAppID = "replyToAppID";

    int bodyLen;
    unsigned char* body;
    int res = JAVACALL_FAIL;

    javacall_result ok;
    int pBytesWritten = 0;
    void *context;
	int readNextFragment = 0;

        memset(cookieBuffer,0,40);

        while (decodeMmsBuffer(buffer,
    		                &fromAddress,
                            &appID,
    		                &replyToAppID,
    		                &bodyLen,
    		                &body)) {
                // Read Next Fragment

                if(strlen(cookieBuffer) == 0 ) {
                    javautil_debug_print (JAVACALL_LOG_ERROR, "mms", "Error getting next fragment - No Cookie in message\n");

                    break;
                }

    			// we need to reply back the cookieRespond in order the next segment to be sent
                ok = javacall_datagram_sendto_start(handle, pAddress, cookiePortNumber,
                                                    cookieBuffer, cookieLen, &pBytesWritten, &context);

                if ( ok == JAVACALL_OK) {

                    do {
                        ok = javacall_datagram_recvfrom_start(
                            handle, pAddress, port, buffer, length, pBytesRead, &context);
                    } while ( ok == JAVACALL_WOULD_BLOCK );

                    if ( (ok != JAVACALL_OK) || (*pBytesRead == 0 )) {
                         javautil_debug_print (JAVACALL_LOG_ERROR, "mms", "Error getting next fragment.\n");
                         return JAVACALL_FAIL;
                    }
                } else {
                     javautil_debug_print (JAVACALL_LOG_ERROR, "mms", "Error: MMS sending - cookie reply datagram blocked.\n");
                }

                memset(cookieBuffer,0,40);
	} ;

	//else continue and pass the MMS up
    if (javacall_is_mms_appID_registered(appID) != JAVACALL_OK) {
 	 javautil_debug_print (JAVACALL_LOG_INFORMATION, "jsr205_UDPEmulator", "MMS on unregistered appID received!");
        //return
	res = JAVACALL_FAIL;
    } else {

        javanotify_incoming_mms(
        fromAddress, appID, replyToAppID,
        bodyLen,
        body);

        free(body); // was allocated in decodeMmsBuffer

        res = JAVACALL_OK;
    }

    return res;
}
