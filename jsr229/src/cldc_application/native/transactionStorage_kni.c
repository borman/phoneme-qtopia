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
#include <stdio.h>

#include <jvm.h>
#include <kni.h>
#include <sni.h>
#include <commonKNIMacros.h>
#include <midpServices.h>
#include <midp_thread.h>
#include <midpServices.h>

#include <midpMalloc.h>

/**
 * Storage locking flag
 */
static jboolean locked = KNI_FALSE;

#define getStorageOpenFlag(__handle) \
    (unhand(struct Java_com_sun_j2me_payment_TransactionStorageImpl, __handle))

/**
 * The function blocks the calling thread if Transaction Store is locked.
 * <p>Java declaration:
 * <pre>
 * private native void lockStore();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_j2me_payment_TransactionStorageImpl_lockStore() {
    MidpReentryData *info = NULL;

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisObject);
    KNI_GetThisPointer(thisObject);

    if (locked == KNI_FALSE) {
        locked = KNI_TRUE;
        KNI_GetThisPointer(thisObject);
        getStorageOpenFlag(thisObject)->isOpen = (jboolean)KNI_TRUE;
    } else {
        info = (MidpReentryData*)SNI_GetReentryData(NULL);
        if (info == NULL) {
            info = (MidpReentryData*)
                (SNI_AllocateReentryData(sizeof (MidpReentryData)));
        }
        info->waitingFor = PAYMENT_TRANSACTION_STORE_SIGNAL;
        info->descriptor = 0;
        info->status = KNI_FALSE;
        info->pResult = NULL;

        /* try again later */
        SNI_BlockThread();
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/**
 *  The function unblocks threads waiting for Transaction Store
 */
void unlock() {
    int i, n;
    JVMSPI_BlockedThreadInfo *blocked_threads;
    MidpReentryData* pThreadReentryData;

    KNI_StartHandles(1);
    KNI_DeclareHandle(thisObject);
    KNI_GetThisPointer(thisObject);

    if ((locked == KNI_TRUE) && 
            (getStorageOpenFlag(thisObject)->isOpen == KNI_TRUE)) {
        blocked_threads = SNI_GetBlockedThreads(&n);
        getStorageOpenFlag(thisObject)->isOpen = KNI_FALSE;
        locked = KNI_FALSE;
        for (i = 0; i < n; i++) {

            pThreadReentryData =
                (MidpReentryData*)(blocked_threads[i].reentry_data);

            if (pThreadReentryData == NULL) {
                continue;
            }
     
            if (pThreadReentryData->waitingFor != 
                    PAYMENT_TRANSACTION_STORE_SIGNAL) {
                continue;
            }

            pThreadReentryData->status = KNI_TRUE;
 
            midp_thread_unblock(blocked_threads[i].thread_id);
        }

    }

    KNI_EndHandles();
}

/**
 *  The function unblocks threads waiting for Transaction Store
 * <p>Java declaration:
 * <pre>
 * private native void unlockStore();
 * </pre>
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_j2me_payment_TransactionStorageImpl_unlockStore() {
    unlock();
    KNI_ReturnVoid();
}

/**
 *  Finalizer. Unlocks threads waiting for Transaction Store.
 * <p>Java declaration:
 * <pre>
 * private native void finalize();
 * </pre>
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_j2me_payment_TransactionStorageImpl_finalize() {
    unlock();
    KNI_ReturnVoid();
}
