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

#ifndef _JSR205_MMS_LISTENERS_H
#define _JSR205_MMS_LISTENERS_H

#include <jsr205_mms_structs.h>
#include <suitestore_common.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * This is the method that gets called as soon as an MMS message has been added
 * to the pool/in-box.
 *
 * @param message  The message that has just arrived in the in-box.
 */
void jsr205_mms_message_arrival_notifier(MmsMessage* message);


/**
 * Checks whether the specified port has been registered by midlet 
 * or push registry for receiving SMS messages. For the case of
 * push registry checks that sourceAddress matches push filter.
 *
 * @param appID The MMS application identifier.
 * @param fromAddr The sender's MMS address.
 */
WMA_STATUS jsr205_mms_is_message_expected(char* appID, char* fromAddr);

/**
 * This is the method that gets called as soon as an MMS message header
 * has been added received.
 *
 * @param appID The MMS application identifier.
 * @param fromAddr The sender's MMS address.
 */
void jsr205_mms_message_available_notifier(char* appID, char* fromAddr);

/**
 * This is the method that gets called as soon as an MMS message has been
 * sent.
 *
 * @param status indication of send completed status result: Either
 *         <tt>WMA_OK</tt> on success,
 *         <tt>WMA_ERR</tt> on failure
 * @param handle of MMS
 */
void jsr205_mms_message_sent_notifier(int handle, WMA_STATUS status);

/**
 * Checks whether the specified application identifier has been registered for
 * receiving MMS messages.
 *
 * @param appID The MMS application identifier to be registered.
 *
 * @return <code>WMA_OK</code> if the application identifier is registered;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_is_midlet_listener_registered(unsigned char* appID);

/**
 * Registers the specified MMS application identifier for the calling MIDlet.
 *
 * @param appID  The MMS application identifier to be registered.
 * @param msid The MIDlet suite identifier.
 * @param handle  A handle to the open MMS connection.
 *
 * @return <code>WMA_OK</code> if application identifier is registered;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_register_midlet_listener(unsigned char* appID,
    SuiteIdType msid, jint handle);

/**
 * Unregister the specified MMS application identifier for the calling MIDlet.
 *
 * @param appID  The MMS application identifier to be unregistered.
 *
 * @return <code>WMA_OK</code> if the application identifier has not been
 *     registered; <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_unregister_midlet_listener(unsigned char* appID);

/**
 * Checks whether the specified application identifier has been registered by
 * the push registry for receiving MMS messages.
 *
 * @param appID The MMS application identifier to be registered.
 *
 * @return <code>WMA_OK</code> if the application identifier is registered;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_is_push_listener_registered(unsigned char* appID);

/**
 * Registers the specified MMS application identifier with the push registry.
 *
 * @param appID The MMS application identifier to be registered.
 * @param msid The MIDlet suite ID.
 * @param handle A handle to the open MMS connection
 *
 * @return <code>WMA_OK</code> if the application identifier was registered;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_register_push_listener(unsigned char* appID,
    SuiteIdType msid, jint handle);

/**
 * Unregister the specified MMS application identifier with the push registry.
 *
 * @param appID The MMS application identifier to be unregistered.
 *
 * @return <code>WMA_OK</code> if the application identifier has not been
 *     registered; <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_unregister_push_listener(unsigned char* appID);

/**
 * Unblocks the thread that matches the specified handle and signal.
 *
 * @param handle The handle to the open MMS connection.
 * @param waitingFor The signal that the thread is waiting for.
 *
 * @return <code>WMA_OK</code> if a matching thread is unblocked;
 *     <code>WMA_ERR</code>, otherwise.
 */
WMA_STATUS jsr205_mms_unblock_thread(int handle, int waitingFor);

/**
 * Delete all MMS messages cached in the pool for the specified midlet suite.
 *
 * @param msid The midlet suite identifier.
 */
void jsr205_mms_delete_midlet_suite_msg(SuiteIdType msid);

/**
 * Delete all MMS messages cached in the pool for the specified midlet suite, by
 * the push subsystem.
 *
 * @param msid The midlet suite identifier.
 */
void jsr205_mms_delete_push_msg(SuiteIdType msid);

#ifdef __cplusplus
}
#endif

#endif /* #ifdef _JSR205_MMS_LISTENERS_H_ */
