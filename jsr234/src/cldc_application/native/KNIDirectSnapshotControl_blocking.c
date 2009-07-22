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

#include "kni.h"
#include "KNICommon.h"
#include "midpEvents.h"
#include "midpServices.h"
#include "javacall_multimedia_advanced.h"
#include "jsrop_exceptions.h"

#include "jsr234_control.h"

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nStart)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    MidpReentryData* info;
    javacall_result res = JAVACALL_FAIL;
    long value = (long)KNI_GetParameterAsInt(2);

    MMP_DEBUG_STR("+nStart\n");

    if (NULL == pKniInfo || NULL == pKniInfo->pNativeHandle) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nCamera Control: Invalid handle\n");
    }

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (NULL == info) {
        res = javacall_amms_snapshot_control_start(
            pKniInfo->pNativeHandle, value);
    } else {
        res = info->status;
    }

    if (JAVACALL_WOULD_BLOCK == res) {
        if (NULL == info && NULL == (info = (MidpReentryData*)
            (SNI_AllocateReentryData(sizeof(MidpReentryData))))) {
            KNI_ThrowNew(jsropRuntimeException,
                "\nCamera Control: Reentry Data Allocation Failed\n");
        }

        /* IMPL_NOTE: Compose 16 bit of isolate ID and 16 bit of player ID
           to generate descriptor */
        info->descriptor = (((pKniInfo->appId & 0xFFFF) << 16) | 
            (pKniInfo->playerId & 0xFFFF));
        info->waitingFor = JSR234_SNAPSHOT_STARTED_SIGNAL;

        MMP_DEBUG_STR1("nStart blocked descriptor %d\n", info->descriptor);
        SNI_BlockThread();
    } else if (JAVACALL_INVALID_ARGUMENT == res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nCamera Control: Bad parameters to start shapshots\n");
    } else if (JAVACALL_OK != res) {
        KNI_ThrowNew("java/lang/IllegalStateException",
            "\nCamera snapshot starting: Illegal state of Player "
            "or suffix/prefix not set\n");
    }
    KNI_ReturnVoid();
}
