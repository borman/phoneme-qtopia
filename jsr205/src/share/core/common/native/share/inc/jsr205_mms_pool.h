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

#ifndef _JSR205_MMS_POOL_H_
#define _JSR205_MMS_POOL_H_

/**
 * @file
 * @ingroup wma
 */

/**
 * @defgroup mmsstorage MMS Storage Porting Interface
 * @ingroup mms
 * @brief Multimedia Message Service storage porting interface. \n
 * ##include <jsr205_mms_structs.h>
 * @{
 *
 * This file defines the Multimedia Message Service storage porting interfaces.
 * Incoming MMS messages are stored in a message pool, from which they can be
 * retrieved and deleted. Messages can also be created, copied and duplicated.
 * The provided RAM-based implementation may be used as-is or replaced/modified
 * to suit a particular platform, using the platform interfaces (e.g.: An
 * implementation that uses the file system to provide persistent message
 * storage.).
 */

#ifdef __cplusplus
extern "C" {
#endif

#include <jsr120_types.h>
#include <jsr205_mms_structs.h>

/**
 * The maximum number of messages permitted in the MMS pool before messages
 * are flushed. The first message in the pool gets flushed first.
 */ 
#define MAX_MMS_MESSAGES_IN_MMS_POOL (50)

/**
 * Helper functions to process MMS messages. These functions help to complete
 * these kinds of basic operations for MMS messages:
 * <p>
 * <table>
 *   <tr>
 *     <th>Function</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_new_msg</code></td>
 *     <td>Allocates memory for a new message.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_copy_msg</code></td>
 *     <td>Copies one message over an existing message.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_dup_msg</code></td>
 *     <td>Copies an existing message into new, allocated memory.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_delete_msg</code></td>
 *     <td>Frees the memory used by an existing message.</td>
 *   </tr>
 *   <tr>
 * </table>
 * <p>
 */

/**
 * Create a new message and populate it with the given data. Memory
 * will be allocated for the new message. If the message length is
 * longer than the maximum length permitted for a MMS message, the new
 * message will contain only the maximum number of bytes permitted
 * for a MMS message.
 *
 * @param fromAddress The address from which the message originated.
 * @param appID The application ID string for the recipient.
 * @param replyToAppID The application ID of the sender.
 * @param msgLen The length of the message, excluding the terminating
 *     character.
 * @param msgBuffer The buffer that holds the text message and its
 *     terminating character.
 *
 * @return The new MMS message.
 */
MmsMessage* jsr205_mms_new_msg(char* fromAddress, char* appID,
    char* replyToAppID, int msgLen, unsigned char* msgBuffer);

/**
 * Copy a MMS message. Copy all data from the source message to the
 * destination message, overwriting all previous destination message
 * data.
 *
 * @param src The source message.
 * @param dst The destination message.
 */
void jsr205_mms_copy_msg(MmsMessage* src, MmsMessage* dst);

/**
 * Duplicate a MMS message by creating a new message and populating its fields
 * with the data from the source message.
 *
 * @param msg The source message.
 *
 * @return The new message (A clone of the source message), or <code>NULL</code>
 *	if the source message was <code>NULL</code>.
 */
MmsMessage* jsr205_mms_dup_msg(MmsMessage* msg);

/**
 * Delete the given MMS message and release the memory used by the message to
 * the memory pool.
 *
 * @param msg The message that will have its memory freed.
 */
void jsr205_mms_delete_msg(MmsMessage* msg);


/**
 * MMS Message Pool Functions
 * <P>
 * <table>
 *   <tr>
 *     <th>Function</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_add_msg</code></td>
 *     <td>Add a new message to the pool.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_get_next_msg</code></td>
 *     <td>Fetch the next message and remove it from the pool.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_retrieve_next_msg</code></td>
 *     <td>Retrieve the next message data and remove only the entry from the
 *       pool. The data must be removed separately.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_remove_next_msg</code></td>
 *     <td>Remove the next message from the pool that matches the
 *       ID.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_remove_all_msgs</code></td>
 *     <td>Remove all messages from the pool.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_peek_next_msg</code></td>
 *     <td>Fetch the next message that matches the ID without removing the
 *       message from the pool.</td>
 *   </tr>
 *   <tr>
 *     <td><code>jsr205_mms_pool_delete_next_msg</code></td>
 *     <td>Delete the next message from the pool that matches the ID.</td>
 *   </tr>
 * </table>
 * <p>
 */


/**
 * Add a MMS message to the message pool. If the pool is full (i.e., there are
 * at least <code>MAX_MMS_MESSAGES_IN_MMS_POOL</code>), then the oldest messages
 * are discarded.
 *
 * @param msg The message to be added.
 *
 * @return <code>WMA_OK</code> if the message was successfully added to the
 *     pool. <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_add_msg(MmsMessage* msg);

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
 *            remove the message from the pool.
 *
 * @return <code>WMA_OK</code> if a message could be located;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_get_next_msg(unsigned char* appID, MmsMessage* out);

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
MmsMessage* jsr205_mms_pool_retrieve_next_msg(unsigned char* appID);

/**
 * Remove the next (oldest) message from the pool that matches the message ID.
 *
 * @param appID The application identifier to be matched.
 *
 * @return <code>WMA_OK</code> when a message was removed;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_remove_next_msg(unsigned char* appID);

/**
 * Remove all messages from the pool that match the application identifier.
 *
 * @param appID The application identifier to be matched.
 */
void jsr205_mms_pool_remove_all_msgs(unsigned char* appID);

/**
 * Fetch the first message that matches the application identifier without removing
 * the message from the pool.
 *
 * @param appID  The application identifier to be matched.
 *
 * @return The MMS message or <code>NULL</code> if no match could be found.
 */
MmsMessage* jsr205_mms_pool_peek_next_msg(unsigned char* appID);

/**
 * Fetches the first message that matches the application identifier without removing
 * the message from the pool.
 *
 * @param appID The application identifier to be matched.
 * @param isNew Get the new message only when not 0.
 *
 * @return The MMS message or <code>NULL</code> if no match could be found.
 */
MmsMessage* jsr205_mms_pool_peek_next_msg1(unsigned char* appID, jint isNew);

/**
 * Deletes the oldest MMS message.
 *
 * @return <code>WMA_OK</code> if the oldest message was found and deleted;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_pool_delete_next_msg();

#ifdef __cplusplus
}
#endif

/** @} */

#endif /* #ifdef _JSR205_MMS_POOL_H_ */
