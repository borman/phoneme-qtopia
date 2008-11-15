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
#include <kni.h>
#include <sni.h>
#include <commonKNIMacros.h>
#include <ROMStructs.h>
#include <midp_thread.h>
#include <midpServices.h>
#include <push_server_export.h>
#ifdef ENABLE_JUMP
#include <JUMPEvents.h>
#include <jsr205_jumpdriver.h>
#endif

#include <jsr120_list_element.h>
#include <jsr205_mms_pool.h>
#include <jsr205_mms_listeners.h>
#include <jsr205_mms_protocol.h>
#include <suitestore_common.h>
#include <push_server_resource_mgmt.h> //pushgetfiltermms
#include <wmaPushRegistry.h> // jsr120_check_filter


/** Listeners registered by a currently running midlet */
static ListElement* mms_midlet_listeners = NULL;

/** Listeners registered by the push subsystem. */
static ListElement* mms_push_listeners = NULL;

typedef WMA_STATUS mms_listener_t(MmsMessage* message, void* userData);

/*
 * private methods
 */
static WMA_STATUS jsr205_mms_midlet_listener(MmsMessage *message, void* userData);
static WMA_STATUS jsr205_mms_push_listener(MmsMessage *message, void* userData);
static WMA_STATUS invokeMMSListeners(MmsMessage* message, ListElement *listeners);
static JVMSPI_ThreadID getBlockedThreadFromHandle(long handle, int waitingFor);
static WMA_STATUS registerMMSListener(unsigned char* appID, SuiteIdType msid,
                                      mms_listener_t* listener, void* userData,
                                      ListElement **listeners,
                                      jboolean registerAppID);
static WMA_STATUS unregisterMMSListener(unsigned char* appID,
                                        mms_listener_t* listener,
                                        ListElement **listeners,
                                        jboolean unregisterAppID);

static WMA_STATUS isMMSAppIDRegistered(unsigned char* appID,
    ListElement *listeners);
static WMA_STATUS isMMSAppIDRegisteredByMsid(unsigned char* appID,
                                             ListElement *listeners, SuiteIdType msid);
static void jsr205_mms_delete_all_msgs(SuiteIdType msid, ListElement *head);

/**
 * Invoke registered listeners that match the appID specified in the MMS
 * message.
 *
 * @param message Incoming MMS message
 * @listeners list of registered listeners
 *
 * @result returns <code>WMA_OK</code> if a matching listener is invoked,
 *                 <code>WMA_ERR</code> otherwise
 */
static WMA_STATUS invokeMMSListeners(MmsMessage* message, ListElement *listeners) {
    ListElement* callback;

    /* Assume no listeners were found and threads unblocked */
    WMA_STATUS unblocked = WMA_ERR;

    /* Notify all listeners that match the given port (Application ID) */
    for (callback = jsr120_list_get_by_name(listeners, (unsigned char*)message->appID);
         callback != NULL;
        callback = jsr120_list_get_by_name(callback->next, (unsigned char*)message->appID)) {

        /* Pick up the listener */
        mms_listener_t* listener=(mms_listener_t*)(callback->userDataCallback);

        if (listener != NULL) {
            unblocked = listener(message, callback->userData);
            if (unblocked == WMA_OK) {
                /*
                 * A thread blocked on receiving a message has been unblocked.
                 * So return.
                 */
                break;
            }
        }
    }
    return unblocked;
}

/**
 * See jsr20_mms_listeners.h for documentation
 */
void jsr205_mms_message_available_notifier(char* appID, char* replyToAppID) {

    MmsMessage msg;
    msg.appID = appID; //to do: delete msg param from invokeMMSListeners, mms_listener_t
    if (mms_push_listeners != NULL) {
        pushsetcachedflagmms("mms://:", appID);
        invokeMMSListeners(&msg, mms_push_listeners);
    }
    (void)replyToAppID;
}

/*
 * See jsr20_mms_listeners.h for documentation
 */
void jsr205_mms_message_arrival_notifier(MmsMessage* mmsMessage) {

    WMA_STATUS unblocked = WMA_ERR;

    /*
     * First invoke listeners for current midlet
     */
    if (mms_midlet_listeners != NULL) {
        unblocked = invokeMMSListeners(mmsMessage, mms_midlet_listeners);
    }

    /*
     * If a listener hasn't been invoked, try the push Listeners
     */
    if ((unblocked == WMA_ERR) && (mms_push_listeners != NULL)) {
        pushsetcachedflagmms("mms://:", mmsMessage->appID);
        unblocked = invokeMMSListeners(mmsMessage, mms_push_listeners);
    }
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_is_message_expected(char* appID, char* from) {

    if (WMA_OK == jsr205_mms_is_midlet_listener_registered(appID)) {
        return WMA_OK;
    }

    if (WMA_OK == jsr205_mms_is_push_listener_registered(appID)) {
        char* filter = pushgetfiltermms("mms://:", appID);
        if (filter == NULL || jsr120_check_filter(filter, from)) {
            return WMA_OK;
        }
    }

    return WMA_ERR;
}


/*
 * See jsr205_mms_listeners.h for documentation
 */
void jsr205_mms_message_sent_notifier(int handle, WMA_STATUS result) {

    /*
     * An MMS message has been sent. So unblock thread
     * blocked on WMA_MMS_WRITE_SIGNAL.
     */
    JVMSPI_BlockedThreadInfo *blocked_threads;
    jint n;
    jint i;

    blocked_threads = SNI_GetBlockedThreads(&n);

    for (i = 0; i < n; i++) {
	MidpReentryData *p =
            (MidpReentryData*)(blocked_threads[i].reentry_data);
	if (p != NULL) {
            if (p->waitingFor == WMA_MMS_WRITE_SIGNAL) {
              if (handle == 0 || p->descriptor == handle) {
                p->status = result;
                midp_thread_unblock(blocked_threads[i].thread_id);
		return;
              }
            }
	}
    }
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_is_midlet_listener_registered(unsigned char* appID) {
    return isMMSAppIDRegistered(appID, mms_midlet_listeners);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_register_midlet_listener(unsigned char* appID,
    SuiteIdType msid, jint handle) {

    jboolean isPushRegistered = isMMSAppIDRegisteredByMsid(
        appID, mms_push_listeners, msid) == WMA_OK;

    return registerMMSListener(appID, msid, jsr205_mms_midlet_listener, (void *)handle,
                               &mms_midlet_listeners, !isPushRegistered);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_unregister_midlet_listener(unsigned char* appID) {
    /*
     * As there was open connection push can be registered only for current suite
     * thus no need to check for suite ID
     */
    jboolean hasNoPushRegistration = jsr205_mms_is_push_listener_registered(appID) == WMA_ERR;

    return unregisterMMSListener(appID, jsr205_mms_midlet_listener,
                                 &mms_midlet_listeners, hasNoPushRegistration);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_is_push_listener_registered(unsigned char* appID) {
    return isMMSAppIDRegistered(appID, mms_push_listeners);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_register_push_listener(unsigned char* appID,
    SuiteIdType msid, jint handle) {

     jboolean isMIDletRegistered = isMMSAppIDRegisteredByMsid(
         appID, mms_midlet_listeners, msid) == WMA_OK;

    return registerMMSListener(appID, msid, jsr205_mms_push_listener,
                               (void *)handle, &mms_push_listeners,
                               !isMIDletRegistered);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_unregister_push_listener(unsigned char* appID) {
    /* 
     * As there was push push registration connection can be open only for current suite 
     * thus no need to check for suite ID 
     */ 
    jboolean hasNoConnection = jsr205_mms_is_midlet_listener_registered(appID) == WMA_ERR;

    return unregisterMMSListener(appID, jsr205_mms_push_listener, &mms_push_listeners,
                                 hasNoConnection);
}

/*
 * See jsr205_mms_listeners.h for documentation
 */
WMA_STATUS jsr205_mms_unblock_thread(int handle, int waitingFor) {
#if (ENABLE_CDC != 1)
    JVMSPI_ThreadID id = getBlockedThreadFromHandle((long)handle, waitingFor);
    if (id != 0) {
        midp_thread_unblock(id);
        return WMA_OK;
    }
#else
/* IMPL NOTE implement this */
    JUMPEvent evt = (JUMPEvent) handle;
    if (jumpEventHappens(evt) >= 0) {
        return WMA_OK;
    }
#endif
    return WMA_ERR;
}

/**
 * Find a first thread that can be unblocked for  a given handle
 * and signal type
 *
 * @param handle Platform specific handle
 * @param signalType Enumerated signal type
 *
 * @return JVMSPI_ThreadID Java thread id than can be unblocked
 *         0 if no matching thread can be found
 *
 */
static JVMSPI_ThreadID
getBlockedThreadFromHandle(long handle, int waitingFor) {
    JVMSPI_BlockedThreadInfo *blocked_threads;
    int n;
    int i;

    blocked_threads = SNI_GetBlockedThreads(&n);

    for (i = 0; i < n; i++) {
        MidpReentryData *p =
            (MidpReentryData*)(blocked_threads[i].reentry_data);
        if (p != NULL) {

            /* wait policy: 1. threads waiting for network reads
                            2. threads waiting for network writes
                             3. threads waiting for network push event*/
            if ((waitingFor == WMA_MMS_READ_SIGNAL) &&
                (waitingFor == (int)p->waitingFor) &&
                 (p->descriptor == handle)) {
                return blocked_threads[i].thread_id;
            }

            if ((waitingFor == PUSH_SIGNAL) &&
                (waitingFor == (int)p->waitingFor) &&
                (findPushBlockedHandle(handle) != 0)) {
                return blocked_threads[i].thread_id;
            }

        }

    }

    return 0;
}

/*
 * Listener that should be called, when a MMS message is is added to the inbox.
 *
 * @param message MMS message that was received.
 * @param userData Pointer to user data, if any, that was cached in the inbox.
 *     This is data that was passed to the inbox, when a port is regsitered with
 *     it. Usually a handle to the open connection.
 *
 * @return <code>WMA_OK</code> if a waiting thread is successfully unblocked;
 *     <code>WMA_ERR</code>, otherwise.
 */
static WMA_STATUS jsr205_mms_midlet_listener(MmsMessage* message, void* userData)
{
    WMA_STATUS status;
    (void)message;

    /** unblock the receiver thread here */
#if (ENABLE_CDC != 1)
    status = jsr205_mms_unblock_thread((int)userData, WMA_MMS_READ_SIGNAL);
#else
    INVOKE_REMOTELY(status, jsr205_mms_unblock_thread, ((int)userData, WMA_MMS_READ_SIGNAL));
#endif
    return status;

}

/*
 * Listener that should be called, when a MMS message is is added to the inbox.
 *
 * @param message MMS message that was received
 * @param userData Pointer to user data, if any, that was cached in the inbox.
 *     This is data that was passed to the inbox, when a port is regsitered with
 *     it. Usually a handle to the open connection.
 *
 * @return <code>WMA_OK</code> if a waiting thread is successfully unblocked;
 *     <code>WMA_ERR</code>, otherwise.
 */
static WMA_STATUS jsr205_mms_push_listener(MmsMessage* message, void* userData) {
    (void)message;

    /** unblock the receiver thread here */
    return jsr205_mms_unblock_thread((int)userData, PUSH_SIGNAL);
}

/**
 * Listen for messages that match a specific application identifier.
 * <P>
 * This function optionally calls the native API to listen for incoming messages and
 * optionally registers a user-supplied callback. The callback is invoked when
 * a message has has added to the message pool.
 * <P>
 * The callback function will be called with the incoming MMS and the user
 * supplied data (userData).
 * <P>
 * The message is retained in the message pool until
 * <tt>MMSPool_getNextMMS()</tt> is called.
 *
 * When <tt>NULL</tt> is supplied as a callback function, messages will be added
 * to the message pool, but no listener will be called..
 *
 * @param appID       The application identifier to be matched.
 * @param msid        Suite ID.
 * @param listener    The MMS listener.
 * @param userData    Any special data associated with the listener.
 * @param listeners List of listeners in which to be registered.
 * @param registerAppID set if need to register Java runtime within
 *         platform to receive notifications on message arrival 
 *
 * @return <code>WMA_OK</code> if successful, <code>WMA_ERR</code> if the ID has already been
 *        registered or if native registration failed.
 */
static WMA_STATUS registerMMSListener(unsigned char* appID, SuiteIdType msid,
                                      mms_listener_t* listener, void* userData,
                                      ListElement **listeners,
                                      jboolean registerAppID) {

    /* Assume no success in registering the application identifer. */
    WMA_STATUS ok = WMA_ERR;

    if (isMMSAppIDRegistered(appID, *listeners) == WMA_ERR) {
        ok = WMA_OK;
        if (registerAppID) {
            ok = jsr205_add_mms_listening_appID(appID);
        }
        jsr120_list_new_by_name(listeners, appID, msid, userData, (void*)listener);
    }

    return ok;
}

/**
 * Stop listening for MMS messages that match a application identifer. The
 * native API is optionally called to stop listening for incoming MMS messages, and the
 * registered listener is unregistered.
 *
 * @param appID       The application ID used for matching IDs.
 * @param listener    The listener to be unregistered.
 * @param userData    Any special data associated with the listener.
 * @param listeners List of listeners from which to be unregistered.
 * @param unregisterAppID set if need to unregister Java runtime within
 *         platform to stop listening to incoming messages
 *
 * @return <code>WMA_OK</code> if successful; <code>WMA_ERR</code>, otherwise.
 */
static WMA_STATUS unregisterMMSListener(unsigned char* appID, mms_listener_t* listener,
                                        ListElement **listeners,
                                        jboolean unregisterAppID) {

    /* Assume no success in unregistering the application identifier */
    WMA_STATUS ok  = WMA_ERR;

    if (isMMSAppIDRegistered(appID, *listeners) == WMA_OK) {

        jsr120_list_unregister_by_name(listeners, appID, (void*)listener);
        if (isMMSAppIDRegistered(appID, *listeners) == WMA_ERR &&
            unregisterAppID) {
            ok = jsr205_remove_mms_listening_appID(appID);
        }

    }
    return ok;
}


/**
 * Check if a application identifier is currently registered for given suite.
 *
 * @param appID            The ID to be matched.
 * @param listeners List of listeners in which to check
 * @param msid suite ID to check
 *
 * @return <code>WMA_OK</code> if the application ID has given suite as an
 * associated listener; <code>WMA_ERR</code>, otherwise.
 *
 */
static WMA_STATUS isMMSAppIDRegisteredByMsid(unsigned char* appID,
                                             ListElement *listeners,
                                             SuiteIdType msid) {
    ListElement *entry = jsr120_list_get_by_name(listeners, appID);
    
    return entry != NULL && entry->msid == msid ? WMA_OK : WMA_ERR;
}

/**
 * Check if a application identifier is currently registered.
 *
 * @param appID            The ID to be matched.
 * @param listeners List of listeners in which to check
 *
 * @return <code>WMA_OK</code> if the application ID has an associated listener;
 *        <code>WMA_ERR</code>, otherwise.
 *
 */
static WMA_STATUS isMMSAppIDRegistered(unsigned char* appID, ListElement *listeners) {
    return ((jsr120_list_get_by_name(listeners, appID) != NULL) ? WMA_OK : WMA_ERR);
}

/**
 * Delete all MMS messages cached in the pool for the specified
 * midlet suite
 *
 * @param msid Midlet Suite ID.
 *
 */
void jsr205_mms_delete_midlet_suite_msg(SuiteIdType msid) {
    jsr205_mms_delete_all_msgs(msid, mms_midlet_listeners);
}

/**
 * Delete all MMS messages cached in the pool for the specified
 * midlet suite, for the Push subsystem.
 *
 * @param msid Midlet Suite ID.
 *
 */
void jsr205_mms_delete_push_msg(SuiteIdType msid) {
    jsr205_mms_delete_all_msgs(msid, mms_push_listeners);
}

/**
 * Delete all MMS messages cached in the pool for the specified midlet suite. 
 * The linked list with the (msid, app id) pairings has to be specified.
 *
 * @param msid Midlet Suite ID.
 * @param head Head of linked list, that has (msid, app id)	pairings.
 *
 */
static void jsr205_mms_delete_all_msgs(SuiteIdType msid, ListElement* head) {

    ListElement *elem = NULL;

    if ((elem = jsr120_list_get_first_by_msID(head, msid)) != NULL) {
        /*
         * If the dequeued element has a valid app id,
         * then delete all MMS messages stored for that app id.
         */
        if (elem->strid != NULL) {
            jsr205_mms_pool_remove_all_msgs(elem->strid);
        }
    }
}

