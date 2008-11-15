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

/*
 * SUBSYSTEM: WMA (Wireless Messaging API)
 * FILE:      mmsProtocol.c
 * OVERVIEW:  This file handles WMA functions such as
 *            - sending an MMS message to a destination address
 *            - receiving an MMS message
 */

#include <sys/types.h>
#include <string.h>
#include <errno.h>
#include <kni.h>
#include <pcsl_memory.h>
#include <pcsl_string.h>
#include <midp_thread.h>
#include <midpUtilKni.h>

#include <sni.h>
#include <commonKNIMacros.h>
#include <ROMStructs.h>
#include <midpError.h>
#include <midp_properties_port.h>
#include <midp_logging.h>
#include <midpResourceLimit.h>
#include <suitestore_common.h>

#include <ROMStructs.h>

/* MMS protocol, native layer APIs and message pool APIs. */
#include <jsr205_mms_protocol.h>
#include <jsr205_mms_pool.h>
#include <jsr205_mms_listeners.h>

/**
 * The number of bytes that were sent. This is outside of send0, because of the
 * re-invocation part of the code in send.
 */
static int bytesSent = -1;

typedef struct {
    /** The "to" address. */
    char* toAddress;
    /** The "from" address. */
    char* fromAddress;
    /** The application ID. */
    char* appID;
    /** The reply-to application ID. */
    char* replyToAppID;
    /** The total number of bytes in the message header. */
    int headerLen;
    /** The message header to be sent . */
    char* header;
    /** The total number of bytes in the message body. */
    int bodyLen;
    /** The message body to be sent. */
    char* body;
    /** Reinvocation context of platform-specific send function. */
    void *pdContext;
} jsr205_mms_message_state_data;

/**
 * Helper function to unblock the thread that sends the MMS read signal. Locate
 * the first blocked thread that can be unblocked for a given handle and signal
 * type.
 *
 * @param handle    Platform-specific handle.
 * @param waitingFor    Enumerated signal type.
 *
 * @return   The Java thread ID than can be unblocked or <code>0</code> if the
 *     blocked thread could not be found.
 */
static JVMSPI_ThreadID
getBlockedThreadFromHandle(long handle, int waitingFor) {

    /* The blocked thread list and number of blocked threads. */
    JVMSPI_BlockedThreadInfo* blocked_threads;
    int n;

    int i;

    /* Pick up the blocked thread list and count. */
    blocked_threads = SNI_GetBlockedThreads(&n);

    /* Search the list for one with a matching handle and signal. */
    for (i = 0; i < n; i++) {

        MidpReentryData* p =
            (MidpReentryData*)(blocked_threads[i].reentry_data);

        if (p != NULL) {

            /* wait policy: 1. Threads waiting for network reads.
                            2. Threads waiting for network writes.
                            3. Threads waiting for network push event. */
            if (p->descriptor == handle &&
                waitingFor == (int)p->waitingFor) {
                return blocked_threads[i].thread_id;
            }
        }

    }

    return 0;
}

/**
 * Unblocks the java thread.
 *
 * @param handle    Platform-specific handle.
 * @param waitingFor    Enumerated signal type.
 *
 * @return <code>0</code> if successful; <code>1</code> The VM needed to be woken up.
 */
jboolean mms_unblock_thread(int handle, int waitingFor) {
    JVMSPI_ThreadID id;

    id = getBlockedThreadFromHandle((long)handle, waitingFor);
    if (id != 0) {
        midp_thread_unblock(id);
        return 1;
    }

    return 0;
}

#ifndef ENABLE_MIDP
typedef struct MidpString {
    int len;
    jchar* data;
} MidpString;
static MidpString NULL_MIDP_STRING = {0, NULL};
char* midpJcharsToChars(MidpString data) {
    char* result = pcsl_mem_malloc(data.len+1);
    int i;
    for (i=0; i<data.len; i++) { 
	result[i] = (char)data.data[i];
    }
    result[data.len] = 0;
    return result;
}
#endif

/**
 * Opens an MMS connection.
 *
 * @param host The name of the host for this connection. Can be
 *     <code>null</code>.
 * @param appID The application ID associated with this connection.
 * Can be <code>null</code> fou unblock sending and receiving messages.
 * @param msid The MIDlet suite ID string.
 *
 * @return A handle to the open MMS connection.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_open0(void) {

    /** The MIDP String version of the host. */
    MidpString msHost = NULL_MIDP_STRING;

    /* The host name for this connection. */
    unsigned char* host = NULL;

    /** The MIDP String version of the application ID. */
    MidpString msAppID = NULL_MIDP_STRING;

    /* The application ID to be matched against incoming messages. */
    unsigned char* appID = NULL;

    /** The MIDlet suite ID. */
    int midletSuiteID = UNUSED_SUITE_ID;

    /* The handle associated with this MMS connection. */
    int handle = 0;

    /* Create handles for all Java objects. */
    KNI_StartHandles(2);
    KNI_DeclareHandle(javaStringHost);
    KNI_DeclareHandle(javaStringAppID);

    /* Pick up the application ID string. */
    KNI_GetParameterAsObject(1, javaStringHost);
    KNI_GetParameterAsObject(2, javaStringAppID);

    /* When appID is null then return else continue */
    if (!KNI_IsNullHandle(javaStringAppID)) {
        midletSuiteID = KNI_GetParameterAsInt(3);

        do {

            /*
             * Open connection; get unique handle.
             * Note this is done regardless of appID i.e appID
             * can be NULL.
             * If appID is not NULL, then it is registered with
             * the pool.
             */
#if (ENABLE_CDC != 1)
            handle = (int)(pcsl_mem_malloc(1));
#else
            handle = (int)jumpEventCreate();
#endif
            if (handle == 0) {
                KNI_ThrowNew(midpOutOfMemoryError,
                            "Unable to start MMS.");
                break;
            }

            /* Pick up the host name, if any. */
            if (!KNI_IsNullHandle(javaStringHost)) {

                msHost.len = KNI_GetStringLength(javaStringHost);
                msHost.data = (jchar*)pcsl_mem_malloc(msHost.len * sizeof(jchar));
                if (msHost.data == NULL) {
                    /* Couldn't allocate space for the host name string. */
                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                    break;
                } else {

                    /* Convert the MIDP string contents to a character array. */
                    KNI_GetStringRegion(javaStringHost, 0, msHost.len, msHost.data);
                    host = (unsigned char*)midpJcharsToChars(msHost);
                    pcsl_mem_free(msHost.data);
                }
            }

            /* Pick up the application ID. */
            if (!KNI_IsNullHandle(javaStringAppID)) {

                msAppID.len = KNI_GetStringLength(javaStringAppID);
                msAppID.data = (jchar*)pcsl_mem_malloc(msAppID.len * sizeof(jchar));
                if (msAppID.data == NULL) {
                    /* Couldn't allocate space for the application ID string. */
                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                    break;
                } else {

                    /* Convert the MIDP string contents to a character array. */
                    KNI_GetStringRegion(javaStringAppID, 0, msAppID.len, msAppID.data);
                    appID = (unsigned char*)midpJcharsToChars(msAppID);
                    pcsl_mem_free(msAppID.data);

                    /*
                     * Register the application ID with the message pool, only if this
                     * is a server connection (NULL host name.).
                     */
                    if (host == NULL) {
                        if (jsr205_mms_is_midlet_listener_registered(appID) == WMA_OK) {
                            KNI_ThrowNew(midpIOException, "Application ID already in use.");
                            break;
                        } else {
                            /* Attempt to register the application ID. */
                            if (jsr205_mms_register_midlet_listener(appID,
                                    midletSuiteID, handle) == WMA_ERR) {
                                KNI_ThrowNew(midpIOException, "Application ID already in use.");
                                break;
                            }
                        }
                    }
                }
            }
        } while (0);

        /* Memory clean-up (The strings can be NULL). */
        pcsl_mem_free(host);
        pcsl_mem_free(appID);
    }

    KNI_EndHandles();
    KNI_ReturnInt(handle);
}

/**
 * Internal helper function implementing connection close routine
 *
 * @param javaStringAppID handle of String object containing application ID
 * @param handle The handle of the open MMS message connection.
 * @param deRegister Deregistration os the port when parameter is 1.
 */
static int closeConnection(jstring javaStringAppID, int handle, int deRegister) {

    /** The MIDP String version of the application ID. */
    MidpString msAppID = NULL_MIDP_STRING;

    /** The application ID to be matched against incoming messages. */
    unsigned char* appID = NULL;

    /** Status of closing operation (Default: OK; no problems.). */
    int status = 0;

    if (KNI_IsNullHandle(javaStringAppID)) {
        /*
         * No exceptions thrown during close.
         * KNI_ThrowNew(midpIllegalArgumentException, "No application ID available.");
         */
    } else {

        msAppID.len = KNI_GetStringLength(javaStringAppID);
        msAppID.data = (jchar*)pcsl_mem_malloc(msAppID.len * sizeof (jchar));
        if (msAppID.data == NULL) {
            /* Couldn't allocate space for the application ID string.
             *
             * No exceptions thrown during close.
             *.
             * KNI_ThrowNew(midpOutOfMemoryError, NULL);
             */
        } else if (handle == 0) {

            /* MMS was never started. */
            status = -1;

        } else {

            /* Convert the MIDP string contents to a character array. */
            KNI_GetStringRegion(javaStringAppID, 0, msAppID.len, msAppID.data);
            appID = (unsigned char*)midpJcharsToChars(msAppID);
            pcsl_mem_free(msAppID.data);

#if (ENABLE_CDC != 1)
            /* Unblock any blocked threads. */
            jsr205_mms_unblock_thread((int)handle, WMA_MMS_READ_SIGNAL);
#else
            jumpEventHappens((JUMPEvent)handle);
#endif

            /* Unregister the application ID, only if it was previously registered. */
            if (deRegister) {
                if (jsr205_mms_is_midlet_listener_registered(appID) == WMA_OK) {
                    jsr205_mms_unregister_midlet_listener(appID);
                }
                /* Release the handle associated with this connection. */
#if (ENABLE_CDC != 1)
                pcsl_mem_free((void *)handle);
#else
                jumpEventDestroy((JUMPEvent)handle);
#endif
            }

            pcsl_mem_free(appID);
        }
    }

    if (handle && deRegister) {
        /* Release the handle associated with this connection. */
        pcsl_mem_free((void *)handle);
    }

    return status;
}

/**
 * Closes an MMS connection.
 *
 * @param appID The application ID associated with this connection.
 * @param handle The handle of the open MMS message connection.
 * @param deRegister Deregistration appID when parameter is 1.
 * @return <code>0</code> if successful, <code>-1</code> failed.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_close0(void) {

    /** A handle to the open connection. */
    jint handle;

    /** Status of closing operation (Default: OK; no problems.). */
    int status = 0;

    /** Deregistration flag. */
    int deRegister;

    /* Create handles for all Java objects. */
    KNI_StartHandles(1);
    KNI_DeclareHandle(javaStringAppID);

    /* Pick up the application ID string. */
    KNI_GetParameterAsObject(1, javaStringAppID);
    handle = KNI_GetParameterAsInt(2);
    deRegister = KNI_GetParameterAsInt(3);

    status = closeConnection(javaStringAppID, handle, deRegister);

    KNI_EndHandles();
    KNI_ReturnInt(status);
}

/*
 * Sends an MMS message.
 *
 * @param handle The handle to the open MMS connection.
 * @param toAddress The recipient's MMS address.
 * @param fromAddress The sender's MMS address.
 * @param appID The application ID to be matched against incoming messages.
 * @param replyToAppID The ID of the application that processes replies.
 * @param mmsHeader The message header context.
 * @param mmsBody The message body context.
 *
 * @return <code>WMA_OK</code> when bytes were sent; <code>WMA_ERR</code>
 *     when there is an error (This is accompanied by an exception.).
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_send0(void) {
#if (ENABLE_CDC != 1)
    /** MIDP event information structure. */
    MidpReentryData *info = (MidpReentryData*)SNI_GetReentryData(NULL);
#endif
    /** The handle to the open MMS connection. */
    int handle;

    /** The MIDP String version of the "to" address. */
    MidpString msToAddress = NULL_MIDP_STRING;

    /** The "to" address. */
    char* toAddress = NULL;

    /** The MIDP String version of the "from" address. */
    MidpString msFromAddress = NULL_MIDP_STRING;

    /** The "from" address. */
    char* fromAddress = NULL;

    /** The MIDP String version of the application ID. */
    MidpString msAppID = NULL_MIDP_STRING;

    /** The application ID. */
    char* appID = NULL;

    /** The MIDP String version of the reply-to application ID. */
    MidpString msReplyToAppID = NULL_MIDP_STRING;

    /** The reply-to application ID. */
    char* replyToAppID = NULL;

    /** The total number of bytes in the message header. */
    jint headerLen = 0;

    /** The message header to be sent . */
    char* header = NULL;

    /** The total number of bytes in the message body. */
    jint bodyLen = 0;

    /** The message body to be sent. */
    char* body = NULL;

    /** The status of sending the message. */
    WMA_STATUS status = WMA_ERR;

    /** Indicates that send operation has finished with WOULDBLOCK. */
    jboolean stillWaiting = KNI_FALSE;

    /*
     * Indicates the data is ready to be sent, either just prepared or ready
     *  from previous invocation.
     */
    jboolean trySend = KNI_FALSE;

    /** Reinvocation context of platform-specific part */
    void *pdContext = NULL;

    /** Mirror of field open. true if connection is still open */
    jboolean isOpen;

    /* Create handles for all Java objects. */
    KNI_StartHandles(8);
    KNI_DeclareHandle(this);
    KNI_DeclareHandle(thisClass);
    KNI_GetThisPointer(this);
    KNI_GetObjectClass(this, thisClass);
    isOpen = KNI_GetBooleanField(this, KNI_GetFieldID(thisClass, "open", "Z"));

    if (isOpen) { /* No close in progress */
        /** Reinvocation context of platform-independent part */
        jsr205_mms_message_state_data *messageStateData = NULL;

        KNI_DeclareHandle(javaStringToAddress);
        KNI_DeclareHandle(javaStringFromAddress);
        KNI_DeclareHandle(javaStringAppID);
        KNI_DeclareHandle(javaStringReplyToAppID);
        KNI_DeclareHandle(mmsHeader);
        KNI_DeclareHandle(mmsBody);

        /* Create references to the parameters that were passed. */
        handle = KNI_GetParameterAsInt(1);
        KNI_GetParameterAsObject(2, javaStringToAddress);
        KNI_GetParameterAsObject(3, javaStringFromAddress);
        KNI_GetParameterAsObject(4, javaStringAppID);
        KNI_GetParameterAsObject(5, javaStringReplyToAppID);
        KNI_GetParameterAsObject(6, mmsHeader);
        KNI_GetParameterAsObject(7, mmsBody);

        do {
            /*
             * Check the invocation status to see if the address and message need to be
             * processed, or if this is a wrap-up re-invocation to check the status of
             * the send.
             */
#if (ENABLE_CDC != 1)
            if (info == NULL) {
#endif

                /*
                 * First invocation
                 */

                /*
                 * Initialize bytesSent. -1 indicates network failure. When successful,
                 * bytesSent is set to the actual number of bytes sent.
                 */
                bytesSent = -1;

                /* Extract the "to" address (Java String). */
                if (KNI_IsNullHandle(javaStringToAddress)) {
                    KNI_ThrowNew(midpIllegalArgumentException, "Missing 'to' address.");
                    break;
                } else {

                    msToAddress.len = KNI_GetStringLength(javaStringToAddress);
                    msToAddress.data = (jchar*)pcsl_mem_malloc(msToAddress.len * sizeof(jchar));
                    if (msToAddress.data == NULL) {
                        /* Couldn't allocate space for the "to" address string. */
                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                        break;
                    } else {

                        /* Convert the MIDP string contents to a character array. */
                        KNI_GetStringRegion(javaStringToAddress, 0, msToAddress.len, msToAddress.data);
                        toAddress = (char*)midpJcharsToChars(msToAddress);
                        pcsl_mem_free(msToAddress.data);

                        /* Extract the "from" address (Java String). */
                        if (KNI_IsNullHandle(javaStringFromAddress)) {
                            KNI_ThrowNew(midpIllegalArgumentException, "Missing 'from' address.");
                            break;
                        } else {

                            msFromAddress.len = KNI_GetStringLength(javaStringFromAddress);
                            msFromAddress.data = (jchar*)pcsl_mem_malloc(msFromAddress.len * sizeof(jchar));
                            if (msFromAddress.data == NULL) {
                                /* Couldn't allocate space for the "from" address string. */
                                KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                break;
                            } else {

                                /* Convert the MIDP string contents to a character array. */
                                KNI_GetStringRegion(javaStringFromAddress, 0,
                                                       msFromAddress.len, msFromAddress.data);
                                fromAddress = (char*)midpJcharsToChars(msFromAddress);
                                pcsl_mem_free(msFromAddress.data);

                                /* Extract the application ID (Java String). */
                                if (!KNI_IsNullHandle(javaStringAppID)) {

                                    msAppID.len = KNI_GetStringLength(javaStringAppID);
                                    msAppID.data = (jchar*)pcsl_mem_malloc(msAppID.len * sizeof(jchar));
                                    if (msAppID.data == NULL) {
                                        /* Couldn't allocate space for the application ID string. */
                                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                        break;
                                    } else {

                                        /* Convert the MIDP string contents to a character array. */
                                        KNI_GetStringRegion(javaStringAppID, 0, msAppID.len, msAppID.data);
                                        appID = (char*)midpJcharsToChars(msAppID);
                                        pcsl_mem_free(msAppID.data);
				    }
				}
                                        /* Extract the reply-to application ID (Java String). */
                                        if (!KNI_IsNullHandle(javaStringReplyToAppID)) {
                                            msReplyToAppID.len = KNI_GetStringLength(javaStringReplyToAppID);
                                            msReplyToAppID.data =
                                              (jchar*)pcsl_mem_malloc(msReplyToAppID.len * sizeof(jchar));
                                            if (msReplyToAppID.data == NULL) {
                                                /*
                                                 * Couldn't allocate space for the reply-to
                                                 * application ID string.
                                                 */
                                                KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                                break;
                                            } else {

                                                /* Convert the MIDP string contents to a character array. */
                                                KNI_GetStringRegion(javaStringReplyToAppID,
                                                  0, msReplyToAppID.len, msReplyToAppID.data);
                                                replyToAppID = (char*)midpJcharsToChars(msReplyToAppID);
                                                pcsl_mem_free(msReplyToAppID.data);
                                            }
                                        }

                                        /* Extract the message header (Java byte[]). */
                                        if (KNI_IsNullHandle(mmsHeader)) {
                                            KNI_ThrowNew(midpIllegalArgumentException, "Missing header.");
                                            break;
                                        } else {
                                            headerLen = KNI_GetArrayLength(mmsHeader);
                                            if (headerLen <= 0) {
                                                KNI_ThrowNew(midpIllegalArgumentException, "No header.");
                                                break;
                                            } else {
                                                header = (char*)pcsl_mem_malloc(headerLen);
                                                if (header == NULL) {
                                                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                                    break;
                                                } else {
                                                    memset(header, 0, headerLen);
                                                    KNI_GetRawArrayRegion(mmsHeader, 0,
                                                      headerLen, (jbyte*)header);
                                                }
                                                /* Extract the message body (Java byte[]). */
                                                if (KNI_IsNullHandle(mmsBody)) {
                                                    KNI_ThrowNew(midpIllegalArgumentException, "Missing body.");
                                                    break;
                                                } else {
                                                    bodyLen = KNI_GetArrayLength(mmsBody);
                                                    if (bodyLen <= 0) {
                                                        KNI_ThrowNew(midpIllegalArgumentException, "No body.");
                                                        break;
                                                    } else {
                                                        body = (char*)pcsl_mem_malloc(bodyLen);
                                                        if (body == NULL) {
                                                            KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                                            break;
                                                        } else {
                                                            memset(body, 0, bodyLen);
                                                            KNI_GetRawArrayRegion(mmsBody,
                                                              0, bodyLen, (jbyte*)body);
                                                        }

                                                        trySend = KNI_TRUE;
                                                    } /* if bodyLen <= 0 */
                                                } /* if the MMS body exists */
                                            } /* if headerLen <= 0 */
                                        } /* if the MMS header exists */
                            } /* if fromAddress exists */
                        } /* if javaStringFromAddress exists */
                    } /* if toAddress exists */
                } /* if javaStringToAddress exists */
#if (ENABLE_CDC != 1)
            } else {
                /* waiting for mms_send_completed event */
                if (info->pResult == NULL) {
                    if (info->status == WMA_ERR) {
                        KNI_ThrowNew(midpInterruptedIOException, "Sending MMS");
                    }
                    break;                        
                }

                messageStateData = info->pResult;

                toAddress = messageStateData->toAddress;
                fromAddress = messageStateData->fromAddress;
                appID = messageStateData->appID;
                replyToAppID = messageStateData->replyToAppID;
                headerLen = messageStateData->headerLen;
                header = messageStateData->header;
                bodyLen = messageStateData->bodyLen;
                body = messageStateData->body;
                pdContext = messageStateData->pdContext;

                trySend = KNI_TRUE;
            }
#endif
            if (trySend == KNI_TRUE) {

                /* Send message. */
                status = jsr205_send_mms(toAddress,
                                        fromAddress, appID, replyToAppID,
                                        headerLen, header, bodyLen, body,
                                        handle, &pdContext);

                if (status == WMA_ERR) {
                    KNI_ThrowNew(midpIOException, "Sending MMS");
                    break;
                } if (status == WMA_NET_WOULDBLOCK) {
                    if (messageStateData == NULL) {
                        messageStateData =
                            (jsr205_mms_message_state_data *)pcsl_mem_malloc(
                                sizeof(*messageStateData));
                        messageStateData->toAddress = toAddress;
                        messageStateData->fromAddress = fromAddress;
                        messageStateData->appID = appID;
                        messageStateData->replyToAppID = replyToAppID;
                        messageStateData->headerLen = headerLen;
                        messageStateData->header = header;
                        messageStateData->bodyLen = bodyLen;
                        messageStateData->body = body;
                    }

                    messageStateData->pdContext = pdContext;
#if (ENABLE_CDC != 1)
                    /* Block calling Java Thread */
                    midp_thread_wait(WMA_MMS_WRITE_SIGNAL, handle,
                                     messageStateData);
#endif
                    stillWaiting = KNI_TRUE;
                } else {
                    /* waiting for mms_send_completed event */
                    midp_thread_wait(WMA_MMS_WRITE_SIGNAL, handle, NULL);
                }
            }
        } while (0);

        if (!stillWaiting) {
            pcsl_mem_free(appID);
            pcsl_mem_free(replyToAppID);
            pcsl_mem_free(toAddress);
            pcsl_mem_free(fromAddress);
            pcsl_mem_free(header);
            pcsl_mem_free(body);
        }
    }

    KNI_EndHandles();

    /* The status is currently ignored. */
    KNI_ReturnInt(0);
}


/**
 * Receives an MMS message.
 *
 * @param handle The handle to the open MMS message connection.
 * @param appID The application ID to be matched against incoming messages.
 * @param messageObject The Java message object to be populated.
 *
 * @return The total length (bytes) of the MMS message, or <code>-1</code>
 *     if the message could not be received.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_receive0(void) {

    /** The handle to the open MMS connection. */
    int handle;

    /** The MIDP String version of the application ID. */
    MidpString msAppID = NULL_MIDP_STRING;

    /** The application ID. */
    unsigned char* appID = NULL;

    /** The MIDlet suite ID. */
    int midletSuiteID = UNUSED_SUITE_ID;

    /** Generic string length, including the null terminator. */
    int length = -1;
#if (ENABLE_CDC != 1)
    /** MIDP event data */
    MidpReentryData *info = (MidpReentryData*)SNI_GetReentryData(NULL);
#endif
    /** A pointer to the MMS message structure. */
    MmsMessage* pMmsData = NULL;

    /** Total message length. */
    int msgLen = -1;

    /** Mirror of field open. true if connection is still open */
    jboolean isOpen;

    /* Create handles for all Java objects. */
    KNI_StartHandles(10);
    KNI_DeclareHandle(this);
    KNI_DeclareHandle(thisClass);
    KNI_GetThisPointer(this);
    KNI_GetObjectClass(this, thisClass);
    isOpen = KNI_GetBooleanField(this, KNI_GetFieldID(thisClass, "open", "Z"));

    if (isOpen) { /* No close in progress */
        KNI_DeclareHandle(javaStringAppID);
        KNI_DeclareHandle(fromAddressArray);
        KNI_DeclareHandle(appIDArray);
        KNI_DeclareHandle(replyToAppIDArray);
        KNI_DeclareHandle(messageObject);
        KNI_DeclareHandle(messageClazz);
        KNI_DeclareHandle(msgArray);

        /* Pick up the parameters for this method. */
        handle = KNI_GetParameterAsInt(1);
        KNI_GetParameterAsObject(2, javaStringAppID);
        KNI_GetParameterAsObject(4, messageObject);

        midletSuiteID = KNI_GetParameterAsInt(3);

        do {
            /*
             * Extract the application ID, which will be matched against incoming
             * header application IDs.
             */
            if (KNI_IsNullHandle(javaStringAppID)) {
                KNI_ThrowNew(midpIllegalArgumentException, "No address available.");
                break;
            } else {
                msAppID.len = KNI_GetStringLength(javaStringAppID);
                msAppID.data = (jchar*)pcsl_mem_malloc(msAppID.len * sizeof (jchar));
                if (msAppID.data == NULL) {
                    /* Couldn't allocate space for the application ID string. */
                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                    break;
                } else {

                    /* Convert the MIDP string contents to a character array. */
                    KNI_GetStringRegion(javaStringAppID, 0, msAppID.len, msAppID.data);
                    appID = (unsigned char*)midpJcharsToChars(msAppID);
                    pcsl_mem_free(msAppID.data);
                }
            }
#if (ENABLE_CDC != 1)
            /* If there was an event, see if a message arrived. */
            if (!info) {
#endif
                /* If a message isn't available, register the ID and wait. */
                pMmsData = jsr205_mms_pool_peek_next_msg(appID);
                if (pMmsData == NULL) {

#if (ENABLE_CDC != 1)
                    /* block and wait for a message */
                    midp_thread_wait(WMA_MMS_READ_SIGNAL, handle, NULL);
#else
        CVMD_gcSafeExec(_ee, {
                    if (jumpEventWait((JUMPEvent)handle) == 0) {
                        pMmsData = jsr205_mms_pool_peek_next_msg(appID);
                    }
        }); 
#endif
                }
#if (ENABLE_CDC != 1)
            } else {

                /* Re-entry */
                pMmsData = jsr205_mms_pool_peek_next_msg(appID);
                if(pMmsData == NULL) {
                    msgLen = 0;
                }
            }
#endif
            if (pMmsData == NULL) {
                jsr205_fetch_mms(appID);
            }

            if (pMmsData != NULL) {

                if ((pMmsData = jsr205_mms_pool_retrieve_next_msg(appID)) != NULL) {

                    KNI_GetObjectClass(messageObject, messageClazz);
                    if (KNI_IsNullHandle(messageClazz)) {
                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                        break;
                    } else {

                        /* Get references to the internal message representation fields. */
                        /* IMPL_NOTE: The appID field is not used at this time. */
                        jfieldID from_address_field = KNI_GetFieldID(messageClazz, "fromAddress","[B");
                        jfieldID app_id_field = KNI_GetFieldID(messageClazz, "appID","[B");
                        jfieldID reply_to_app_id_field = KNI_GetFieldID(messageClazz, "replyToAppID","[B");
                        jfieldID message_field = KNI_GetFieldID(messageClazz, "message","[B");

                        if ((from_address_field == 0) || (reply_to_app_id_field == 0) ||
                            (reply_to_app_id_field == 0) || (message_field == 0)) {
                            /* REPORT_ERROR(LC_WMA, "ERROR can't get class field ID"); */
                            KNI_ThrowNew(midpRuntimeException, NULL);
                            break;
                        } else {

                            /* Populate the fromAddress field. */
                            if (pMmsData->fromAddress != NULL) {

                                length = strlen(pMmsData->fromAddress);
                                if (length > 0) {
                                    SNI_NewArray(SNI_BYTE_ARRAY, length, fromAddressArray);
                                    if (KNI_IsNullHandle(fromAddressArray)) {
                                        /* Couldn't allocate space for the "to" address. */
                                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                        break;
                                    } else {
                                        int i;
                                        for (i = 0; i < length; i++) {
                                            KNI_SetByteArrayElement(fromAddressArray, i,
                                                                    pMmsData->fromAddress[i]);
                                        }
                                        /* Copy the array data to the Java byte[]. */
                                        KNI_SetObjectField(messageObject, from_address_field,
                                                           fromAddressArray);
                                    }
                                }
                            }

                            /* Populate the appID field. */
                            if (pMmsData->appID != NULL) {

                                length = strlen(pMmsData->appID);
                                if (length > 0) {
                                    SNI_NewArray(SNI_BYTE_ARRAY, length, appIDArray);
                                    if (KNI_IsNullHandle(appIDArray)) {
                                        /* Couldn't allocate space for the application ID. */
                                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                        break;
                                    } else {
                                        int i;
                                        for (i = 0; i < length; i++) {
                                            KNI_SetByteArrayElement(appIDArray, i,
                                                                    pMmsData->appID[i]);
                                        }
                                        /* Copy the array data to the Java byte[]. */
                                        KNI_SetObjectField(messageObject, app_id_field,
                                                           appIDArray);
                                    }
                                }
                            }

                            /* Populate the replyToAppID field. */
                            if (pMmsData->replyToAppID != NULL) {

                                length = strlen(pMmsData->replyToAppID);
                                if (length > 0) {
                                    SNI_NewArray(SNI_BYTE_ARRAY, length, replyToAppIDArray);
                                    if (KNI_IsNullHandle(replyToAppIDArray)) {
                                        /* Couldn't allocate space for the reply-to application ID. */
                                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                        break;
                                    } else {
                                        int i;
                                        for (i = 0; i < length; i++) {
                                            KNI_SetByteArrayElement(replyToAppIDArray, i,
                                                                    pMmsData->replyToAppID[i]);
                                        }
                                        /* Copy the array data to the Java byte[]. */
                                        KNI_SetObjectField(messageObject, reply_to_app_id_field,
                                                           replyToAppIDArray);
                                    }
                                }
                            }


                            /*
                             * If the message exists, allocate space and read the entire
                             * message (Unformatted data).
                             */
                            msgLen = pMmsData->msgLen;
                            if (msgLen > 0) {
                                SNI_NewArray(SNI_BYTE_ARRAY, msgLen, msgArray);
                                if (KNI_IsNullHandle(msgArray)) {
                                    /* Couldn't allocate space for the message. */
                                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                    break;
                                } else {
                                    int i;
                                    for (i = 0; i < msgLen; i++) {
                                        KNI_SetByteArrayElement(msgArray, i,
                                                                pMmsData->msgBuffer[i]);
                                    }
                                    /* Copy the array data to the Java byte[]. */
                                    KNI_SetObjectField(messageObject, message_field,
                                                       msgArray);
                                }
                            }

                        }  /* if valid class fields */

                    }  /* if class object exists */

                }  /* if a message was retrieved from the pool. */

            }  /* if message data were available. */
        } while (0);

        jsr205_mms_delete_msg(pMmsData);
        pcsl_mem_free(appID);
    }

    KNI_EndHandles();

    KNI_ReturnInt(msgLen);
}

/**
 * Wait for a message to become available.
 *
 * @param appID The application ID to be matched against incoming MMS messages.
 * @param handle The handle to the open MMS message connection.
 *
 * @return The length of the unformatted message, waiting to be received.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_waitUntilMessageAvailable0(void) {
#if (ENABLE_CDC != 1)
    /** MIDP event information structure. */
    MidpReentryData *info = (MidpReentryData*)SNI_GetReentryData(NULL);
#endif
    /** The MIDP String version of the application ID. */
    MidpString msAppID = NULL_MIDP_STRING;

    /* The application ID to be matched against incoming messages. */
    unsigned char* appID = NULL;

    /** The handle to the open MMS connection. */
    int handle;

    /** The MMS message that was received. */
    MmsMessage* pMmsData = NULL;

    /** The length of the unformatted MMS message data. */
    int msgLen = -1;

    /** Mirror of field open. true if connection is still open */
    jboolean isOpen;

    /* Create handles for all Java objects. */
    KNI_StartHandles(3);

    KNI_DeclareHandle(this);
    KNI_DeclareHandle(thisClass);
    KNI_GetThisPointer(this);
    KNI_GetObjectClass(this, thisClass);
    isOpen = KNI_GetBooleanField(this, KNI_GetFieldID(thisClass, "open", "Z"));

    if (isOpen) { /* No close in progress */
        KNI_DeclareHandle(javaStringAppID);

        /* Pick up the application ID string. */
        KNI_GetParameterAsObject(1, javaStringAppID);
        handle = KNI_GetParameterAsInt(2);

        if (KNI_IsNullHandle(javaStringAppID)) {
            /*
             * A receive thread wouldn't have been started with a null
             * appID. But a connection can be closed and appID set to null,
             * just before this method is called.
             * The receiverThread uses the IllegalArgumentException to
             * check for this situation and gracefully terminates.
             */
            KNI_ThrowNew(midpIllegalArgumentException, "No application ID available.");
        } else {

            /* Extract the application ID string from the parameter list. */
            msAppID.len = KNI_GetStringLength(javaStringAppID);
            msAppID.data = (jchar *)pcsl_mem_malloc(msAppID.len * sizeof (jchar));
            if (msAppID.data == NULL) {
                /* Couldn't allocate space for the application ID string. */
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                /* Convert the MIDP string contents to a character array. */
                KNI_GetStringRegion(javaStringAppID, 0, msAppID.len, msAppID.data);
                appID = (unsigned char*)midpJcharsToChars(msAppID);
                pcsl_mem_free(msAppID.data);
            }
        }

        /*
         * If data exist, return the message length; otherwise, block and wait for
         * a message.
         */
        if (appID != NULL) {

            /* See if there's a new message waiting in the pool. */
            pMmsData = jsr205_mms_pool_peek_next_msg1(appID, 1);
            if (pMmsData != NULL) {
                msgLen = pMmsData->msgLen;
            } else {
#if (ENABLE_CDC != 1)
                if (!info) {

                     /* Block and wait for a message */
                    midp_thread_wait(WMA_MMS_READ_SIGNAL, handle, NULL);

                } else {
#endif
                     /* May have been awakened due to an interrupt. */
                     msgLen = -1;
#if (ENABLE_CDC != 1)
                }
#endif
            }
        }

        pcsl_mem_free(appID);
    }

    KNI_EndHandles();

    KNI_ReturnInt(msgLen);
}

/**
 * Computes the number of transport-layer segments that would be required to
 * send the given message.
 *
 * @param msgBuffer The message to be sent.
 * @param msgLen The length of the message.
 * @param msgType The message type: binary or text.
 * @param hasPort Indicates if the message includes a source or destination port
 *	number.
 *
 * @return The number of transport-layer segments required to send the message.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_mms_Protocol_numberOfSegments0(void) {

    unsigned char* msgBuffer = NULL;
    int msgLen = 0;
    int msgType = 0;
    jboolean hasPort = KNI_FALSE;

    int segments = 0;

    WMA_STATUS status = WMA_ERR;

    KNI_StartHandles(1);
    KNI_DeclareHandle(msgBufferObject);

    KNI_GetParameterAsObject(1, msgBufferObject);
    msgLen = KNI_GetParameterAsInt(2);
    msgType = KNI_GetParameterAsInt(3);
    hasPort = KNI_GetParameterAsBoolean(4);

    if (!KNI_IsNullHandle(msgBufferObject)) {

        /*
         * Pick up the length of the message, which should be the same as
         * <code>msgLen</code>. This is just done here as a formality.
         */
        int length = KNI_GetArrayLength(msgBufferObject);

        if (length > 0) {
            msgBuffer = (unsigned char *)pcsl_mem_malloc(length);
            memset(msgBuffer, 0, length);
            KNI_GetRawArrayRegion(msgBufferObject, 0, length, (jbyte*)msgBuffer);
	}
	/* Compute the number of segments required to send the message. */
	status = jsr205_number_of_mms_segments(msgBuffer, msgLen, msgType, hasPort, &segments);
	/* The status is not used at this time. */
    }

    pcsl_mem_free(msgBuffer);
    KNI_EndHandles();

    KNI_ReturnInt(segments);
}

/**
 * Gets the phone number of device
 *
 * CLASS:    com.sun.midp.io.j2me.mms.Protocol
 * TYPE:     virtual native function
 * INTERFACE (operand stack manipulation):
 *
 * @return The phone number of device.
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_mms_Protocol_getPhoneNumber0(void) {
    pcsl_string phoneNumber = getInternalPhoneNumber();
    KNI_StartHandles(1);
    KNI_DeclareHandle(tempHandle);
    midp_jstring_from_pcsl_string(&phoneNumber, tempHandle);
    pcsl_string_free(&phoneNumber);
    KNI_EndHandlesAndReturnObject(tempHandle);
}

/**
 * Native finalization of the Java object
 *
 * CLASS:    com.sun.midp.io.j2me.mms.Protocol
 * TYPE:     virtual native function
 * INTERFACE (operand stack manipulation):
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_mms_Protocol_finalize(void) {
    int handle;
    jboolean isOpen;

    KNI_StartHandles(3);
    KNI_DeclareHandle(this);
    KNI_DeclareHandle(thisClass);

    KNI_GetThisPointer(this);
    KNI_GetObjectClass(this, thisClass);
    isOpen = KNI_GetBooleanField(this, KNI_GetFieldID(thisClass, "open", "Z"));

    if (isOpen) {
        KNI_DeclareHandle(appID);

        KNI_GetObjectField(this, KNI_GetFieldID(thisClass, "appID", "Ljava/lang/String;"), appID);
        handle = KNI_GetIntField(this, KNI_GetFieldID(thisClass, "connHandle", "I"));

        closeConnection(appID, handle, 1);
    }

    KNI_EndHandles();

    KNI_ReturnVoid();
}

