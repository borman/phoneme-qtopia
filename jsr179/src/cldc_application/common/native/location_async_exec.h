/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

#ifndef __location_async_exec_H__
#define __location_async_exec_H__

#include <sni.h>
#include <midpServices.h>
#include <midpEvents.h>

#define JAVACALL_LOCATION_ASYNC_EXEC(status_,code_,handle_,javacall_event_,data_) \
do { \
    MidpReentryData* ctx__ = (MidpReentryData *)SNI_GetReentryData(NULL); \
    javacall_result result__ = JAVACALL_FAIL; \
    if (ctx__ == NULL) { \
        result__ = (code_); \
    } else { \
        (result__ = ctx__->status); \
        (data_ = ctx__->pResult); \
    } \
    if (result__ == JAVACALL_WOULD_BLOCK) { \
        if (ctx__ == NULL) { \
            if ((ctx__ = (MidpReentryData *)(SNI_AllocateReentryData(sizeof (MidpReentryData)))) == NULL) { \
                (status_) = JAVACALL_OUT_OF_MEMORY; \
                break; \
            } \
        } \
        ctx__->descriptor = (int)handle_; \
        if (javacall_event_ == JAVACALL_EVENT_LOCATION_ORIENTATION_COMPLETED) { \
            ctx__->waitingFor = JSR179_ORIENTATION_SIGNAL; \
        } else { \
            ctx__->waitingFor = JSR179_LOCATION_SIGNAL; \
        } \
        ctx__->pResult = data_; \
        SNI_BlockThread(); \
    } \
    (status_) = result__; \
} while(0)
#endif /* __location_async_exec_H__ */
