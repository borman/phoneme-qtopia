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
#include "kni_globals.h"
#include "KNICommon.h"
#include "midpServices.h"
#include "javacall_multimedia_advanced.h"
#include "jsrop_exceptions.h"

#include "jsr234_control.h"

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSeek)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    MidpReentryData* info;
    char modulation[KNI_BUFFER_SIZE + 1];
    javacall_result res = JAVACALL_FAIL;
    long startFreq = (long)KNI_GetParameterAsInt(2);
    long freq = -1;
    jboolean upwards = KNI_GetParameterAsBoolean(4);

    if (pKniInfo == NULL) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner seek(): bad handle passed\n");
    }
    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (NULL == info) {
        if (JAVACALL_OK !=
            getUTF8StringFromParameter(KNIPASSARGS 3, modulation)) {
            KNI_ThrowNew(jsropIllegalArgumentException,
                "\nTuner seek(): bad modulation passed\n");
            KNI_ReturnInt((jint)freq);
        }
        res = javacall_amms_tuner_control_seek(
            pKniInfo->pNativeHandle, startFreq, modulation,
            upwards == KNI_TRUE ? JAVACALL_TRUE : JAVACALL_FALSE, &freq);
    } else {
        res = info->status;
        javacall_amms_tuner_control_seek_result(pKniInfo->pNativeHandle, &freq);
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
        info->waitingFor = JSR234_TUNER_SOUGHT_SIGNAL;

        MMP_DEBUG_STR1("nSeek blocked descriptor %d\n", info->descriptor);
        SNI_BlockThread();
    } else if (JAVACALL_INVALID_ARGUMENT == res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner seek(): bad modulation or frequency passed\n");
    } else if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException",
            "\nTuner seek(): the seek functionality is not available for this "
            "modulation\n");
    }

    KNI_ReturnInt((jint)freq);
}
