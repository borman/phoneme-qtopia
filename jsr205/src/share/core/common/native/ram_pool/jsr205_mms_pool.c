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

#include <jsr120_list_element.h>
#include <jsr205_mms_pool.h>
#include <pcsl_memory.h>
#include <app_package.h>

/*
 * This is a collection of methods to maintain the pool of MMS messages
 * (MmsMessage data structures). There is no interface to define this
 * collection. This is just a "best guess" as to the general types of
 * messages needed to maintain the pool. There will be a porting layer
 * for the pool, later.
 *
 * The collection includes helper methods to create, copy, duplicate
 * and delete MMS messages.
 */

/*
 * MMSPool data members
 */

/** The list of messages in the pool. */
static ListElement* MMSPool_messages = NULL;

/** The number of messages in the pool. */
static int MMSPool_count = 0;


/*
 * Helper functions to process MMS messages. These functions help to complete
 * these kinds of basic operations for MMS messages:
 *
 * jsr205_mms_new_msg
 *	Allocates memory for a new message.
 *
 * jsr205_mms_copy_msg
 *	Copies one message over an existing message.
 *
 * jsr205_mms_dup_msg
 *	Copies an existing message into new, allocated memory.
 *
 * jsr205_mms_delete_msg
 *	Frees the memory used by an existing message.
 */

/**
 * Create a new message and populate it with the given data. Memory
 * will be allocated for the new message.
 *
 * @param fromAddress The strinf of the address from which this message
 *     originated.
 * @param appID The application ID string for the recipient.
 * @param replyToAppID The application ID string of the sender.
 * @param msgBuffer A pointer to the message data.
 *
 * @return The new MMS message.
 */
MmsMessage* jsr205_mms_new_msg(char* fromAddress, char* appID,
    char* replyToAppID, int msgLen, unsigned char* msgBuffer) {

    MmsMessage* message = (MmsMessage*)pcsl_mem_malloc(sizeof(MmsMessage));
    memset(message, 0, sizeof(MmsMessage));

    /* Duplicate the address information. */
    message->fromAddress = (char*)pcsl_mem_strdup(fromAddress);
    message->appID = (char*)pcsl_mem_strdup(appID);
    message->replyToAppID = (char*)pcsl_mem_strdup(replyToAppID);

    /* Make a copy of the buffer length and of the message buffer. */
    message->msgLen = msgLen;
    message->msgBuffer = (char*)pcsl_mem_malloc(msgLen);
	memcpy(message->msgBuffer, msgBuffer, msgLen);

    return message;
}

/**
 * Copy a MMS message. Copy all data from the source message to the
 * destination message, overwriting all previous destination message
 * data.
 *
 * @param src The source message.
 * @param dst The destination message.
 */
void jsr205_mms_copy_msg(MmsMessage* src, MmsMessage* dst){

    int length = dst->msgLen;
    src->msgLen = length;
    memcpy(src->msgBuffer, dst->msgBuffer, length);
}

/**
 * Duplicate a MMS message by creating a new message and populating its fields
 * with the data from the source message.
 *
 * @param message The source message.
 *
 * @return The new message (A clone of the source message), or <code>NULL</code>
 *     if the source message was <code>NULL</code>.
 */
MmsMessage* jsr205_mms_dup_msg(MmsMessage* msg) {

    if (msg == NULL) {
        return NULL;
    }

    return jsr205_mms_new_msg(msg->fromAddress,
                              msg->appID,
                              msg->replyToAppID,
                              msg->msgLen,
                              (unsigned char*)msg->msgBuffer);
}

/**
 * Delete the given MMS message and release the memory used by the message to
 * the memory pool.
 *
 * @param msg The message that will have its memory freed.
 */
void jsr205_mms_delete_msg(MmsMessage* msg) {

    if (msg != NULL) {
        pcsl_mem_free(msg->fromAddress);
        pcsl_mem_free(msg->appID);
        pcsl_mem_free(msg->replyToAppID);
        pcsl_mem_free(msg->msgBuffer);
        pcsl_mem_free(msg);
    }
}


/*
 * Helper methods to operate the pool of messages.
 */

/**
 * Increase the number of messages in the pool by one.
 */
static void jsr205_mms_pool_increase_msg_count() {
    MMSPool_count++;
}

/**
 * Decrease the number of messages in the pool by one.
 */
static void jsr205_mms_pool_decrease_msg_count()     {
    MMSPool_count--;
    if (MMSPool_count < 0) {
        MMSPool_count = 0;
    }
}

/**
 * Return the number of messages in the pool.
 *
 * @return The count of messages in the pool.
 */
static int jsr205_mms_pool_get_msg_count() {
    return MMSPool_count;
}

/**
 * Flush messages from the pool if the pool has filled its quota of messages.
 * <P>
 * When at least the maximum number of messages exists in the pool, flush
 * the oldest messages until the pool has room for only one more message.
 */
static void jsr205_mms_pool_check_quota() {
    while (jsr205_mms_pool_get_msg_count() >= MAX_MMS_MESSAGES_IN_MMS_POOL) {
        jsr205_mms_pool_delete_next_msg();
    }
}


/*
 * MMS Message Pool Functions
 *
 * jsr205_mms_pool_add_msg
 *	Add a new message to the pool.
 *
 * jsr205_mms_pool_get_next_msg
 *	Fetch the next message and remove it from the pool.
 *
 * jsr205_mms_pool_retrieve_next_msg
 *	Retrieve the next message data and remove only the entry from the pool.
 *	The data must be removed separately.
 *
 * jsr205_mms_pool_remove_next_msg
 *	Remove the next message from the pool that matches the ID.
 *
 * jsr205_mms_pool_remove_all_msgs
 *	Remove all messages from the pool.
 *
 * jsr205_mms_pool_peek_next_msg
 *	Fetch the next message that matches the ID without removing the message
 *	from the pool>
 *
 * jsr205_mms_pool_delete_next_msg
 *	Delete the next message from the pool that matches the ID.
 */

/**
 * Add a MMS message to the message pool. If the pool is full (i.e., there are
 * at least <code>MAX_MMS_MESSAGES_IN_MMS_POOL</code>), then the oldest messages
 * are discarded.
 *
 * @param msg The message to be added.
 *
 * @return <code>WMA_OK</code> if the message was successfully added to the
 *	pool. <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_add_msg(MmsMessage* msg) {
    ListElement* newItem;

    /* If there is no message to add, bail out. */
    if (msg == NULL) {
        return WMA_ERR;
    }

    /* Make room for this message, if necessary. */
    jsr205_mms_pool_check_quota();

    /* Create the new pool item and add it to the pool. */
    newItem = jsr120_list_new_by_name(NULL, (unsigned char*)msg->appID, UNUSED_APP_ID,
                               (void*)msg, 0);
    jsr120_list_add_last(&MMSPool_messages, newItem);
    jsr205_mms_pool_increase_msg_count();

    return WMA_OK;
}

/**
 * Retrieve the next message from the pool that matches the application identifier
 * and remove the message entry from the pool. The data for the message itself
 * is not removed here and must be done separately.
 *
 * IMPL_NOTE: This is done to keep the main pool functions separated from
 * the list functions. Combine this later with getNextMMS?
 *
 * @param appID The application identifier to be matched.
 * @param out Space for the message. If <code>NULL</code>, this function will
 *		remove the message from the pool.
 *
 * @return <code>WMA_OK</code> if a message could be located;
 *	<code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_get_next_msg(unsigned char* appID, MmsMessage* out) {

    /* Try to locate the message matching the application identifier */
    MmsMessage* msg = jsr205_mms_pool_retrieve_next_msg(appID);

    if (msg != NULL) {

        /* If space exists for the message, copy the message */
        if (out != NULL) {
            jsr205_mms_copy_msg(msg, out);
        }

        /* Always delete the message from the pool. */
        jsr205_mms_delete_msg(msg);
    }

    return (msg != NULL) ? WMA_OK : WMA_ERR;
}

/**
 * Retrieve the next (oldest) message from the pool that matches the application
 * number. Removes the entry from the message pool, but doesn't free the memory
 * associated with the message. The result MUST be deleted by caller using
 * <code>MmsMessage_delete()</code>.
 *
 * @param appID The application identifier to be matched.
 *
 * @return The message, or <code>NULL</code> if no message could be retrieved.
 */
MmsMessage* jsr205_mms_pool_retrieve_next_msg(unsigned char* appID) {
    ListElement* elem;

    /* The result of the search */
    MmsMessage* msg = NULL;

    elem = jsr120_list_remove_first_by_name(&MMSPool_messages, appID);
    if (elem) {

        msg = elem->userData;

        /* Delete element from pool while preserving user data. */
        jsr120_list_destroy(elem);
        jsr205_mms_pool_decrease_msg_count();
    }

    /* Result MUST be deleted by caller using MmsMessage_delete() */
    return msg;
}

/**
 * Remove the next (oldest) message from the pool that matches the application
 * number.
 *
 * @param appID The application identifier to be matched.
 *
 * @return <code>WMA_OK</code> when a message was removed;
 *	<code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_remove_next_msg(unsigned char* appID) {
    return jsr205_mms_pool_get_next_msg(appID, NULL);
}

/**
 * Remove all messages from the pool that match the application identifier.
 *
 * @param appID The application identifier to be matched.
 */
void jsr205_mms_pool_remove_all_msgs(unsigned char* appID) {
    while(jsr205_mms_pool_remove_next_msg(appID) == WMA_OK);
}

/**
 * Fetch the first message that matches the application identifier without removing
 * the message from the pool.
 *
 * @param appID The application identifier to be matched.
 *
 * @return The MMS message or <code>NULL</code> if no match could be found.
 */
MmsMessage* jsr205_mms_pool_peek_next_msg(unsigned char* appID){
    return jsr205_mms_pool_peek_next_msg1(appID, 0);
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
MmsMessage* jsr205_mms_pool_peek_next_msg1(unsigned char* appID, jint isNew){
    ListElement* elem = jsr120_list_get_by_name1(MMSPool_messages, appID, isNew);
    if (elem) {
        return (MmsMessage*)elem->userData;
    }
    return NULL;
}

/**
 * Deletes the oldest message.
 *
 * @return <code>WMA_OK</code> if the oldest message was found and deleted;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_delete_next_msg() {
    ListElement* elem;

    /* Assume there was no message to delete. */
    WMA_STATUS found = WMA_ERR;

    elem = jsr120_list_remove_first(&MMSPool_messages);
    if (elem) {

        /* Free the memory used for the message. */
        MmsMessage* mms = elem->userData;
        jsr205_mms_delete_msg(mms);

        /* Update the pool list and count. */
        jsr120_list_destroy(elem);
        jsr205_mms_pool_decrease_msg_count();

        /* The first (oldest) message was found and deleted. */
        found = WMA_OK;
    }
    return found;
}

