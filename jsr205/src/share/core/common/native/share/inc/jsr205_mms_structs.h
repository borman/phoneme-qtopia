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

#ifndef _JSR205_MMS_STRUCTS_H_
#define _JSR205_MMS_STRUCTS_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <kni.h>

/*
 * MMS Message Structures.
 */

/**
 * A Multimedia Message Service message.
 */
typedef struct _mms_message_struct {

    /* The sender's address. */
    char* fromAddress;

    /* The application identifier string associated with this message. */
    char* appID;

    /* The reply-to application identifier string of the sender. */
    char* replyToAppID;

    /* The total number of bytes in the message. */
    int msgLen;

    /* The combined message header and body data. */
    char* msgBuffer;

} MmsMessage;

/**
 * An MMS message header.
 */
typedef struct _mms_message_header_struct {

    /* The subject line text */
    char* subject;

    /* The delivery time text */
    char* deliveryTime;

    /* The message priority: "normal", "high" or "low" */
    char* priority;

    /* The sender's address. */
    char* from;

    /* The primary recipient address list. */
    char* to;

    /* The carbon copy recipient address list. */
    char* cc;

    /* The blind carbon copy recipient address list. */
    char* bcc;

} MmsHeader;

/**
 * An MMS message body, which is composed of a number of message parts.
 */
/* IMPL_NOTE: DISABLED
typedef struct _mms_message_body_struct {

    / * The number of parts in this message body. * /
    int numParts;

    / * The array of message parts pointers. * /
    MmsMessagePart** msgParts;

} MmsBody;
*/

/**
 * An MMS message part.
 */
/* DISABLED
typedef struct _mms_message_part_struct {

    / * The MIME type [See RFC 2046]. * /
    char* mimeType;

    / *
     * The content-id header field value [See RFC 2045]. The content-id is
     * unique over all message parts of an MMS and must always be set for each
     * message part.
     * /
    char* contentId;

    / *
     * The content location, which specifies the name of the file that is
     * attached. If the content location is set to <code>null</code>, no
     * content location will be set.
     * /
    char* contentLocation;

    / * The length of the message contents. * /
    int msgPartLen;

    / * The message contents. * /
    char* msgPartBuffer;

} MmsMessagePart;
*/

#ifdef __cplusplus
}
#endif

#endif /* #ifdef _JSR205_MMS_STRUCTS_H_ */

