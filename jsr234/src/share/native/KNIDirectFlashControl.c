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
#include "javacall_multimedia_advanced.h"
#include "jsrop_exceptions.h"

#include "jsr234_control.h"


KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_flash_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);    
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nGetSupportedModesNumber)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_flash_control_get_supported_mode_count(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nGetSupportedModeByIndex)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;
    jint index = KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_flash_control_get_supported_mode(
            pKniInfo->pNativeHandle, (long)index, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nSetMode)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long mode = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_flash_control_set_mode(
            pKniInfo->pNativeHandle, mode);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nCamera Flash: this flash mode is not supported\n");
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nGetMode)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_flash_control_get_mode(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFlashControl_nIsFlashReady)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool ready = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_flash_control_is_flash_ready(
            pKniInfo->pNativeHandle, &ready);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == ready ? KNI_TRUE : KNI_FALSE);
}
