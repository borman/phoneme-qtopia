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

#include "jsr234_control.h"


KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_focus_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);    
}
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nSetFocus)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long focus = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_set_focus(
            pKniInfo->pNativeHandle, &focus);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException", 
            "\nCamera Focus: setting this focus is not supported\n");
    }
    KNI_ReturnInt( ( jint )focus );
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nGetFocus)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long focus = JAVACALL_AMMS_FOCUS_UNKNOWN;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_get_focus(
            pKniInfo->pNativeHandle, &focus);
    }

    KNI_ReturnInt((jint)focus);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nGetMinFocus)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long focus = JAVACALL_AMMS_FOCUS_UNKNOWN;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_get_min_focus(
            pKniInfo->pNativeHandle, &focus);
    }

    KNI_ReturnInt((jint)focus);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nGetFocusSteps)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long focusSteps = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_get_focus_steps(
            pKniInfo->pNativeHandle, &focusSteps);
    }

    KNI_ReturnInt((jint)focusSteps);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nIsManualFocusSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool supported = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_is_manual_focus_supported(
            pKniInfo->pNativeHandle, &supported);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == supported ? KNI_TRUE : KNI_FALSE);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nIsAutoFocusSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool supported = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_is_auto_focus_supported(
            pKniInfo->pNativeHandle, &supported);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == supported ? KNI_TRUE : KNI_FALSE);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nIsMacroSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool supported = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_is_macro_supported(
            pKniInfo->pNativeHandle, &supported);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == supported ? KNI_TRUE : KNI_FALSE);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nSetMacro)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean macroMode = KNI_GetParameterAsBoolean(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_set_macro(pKniInfo->pNativeHandle,
            KNI_TRUE == macroMode ? JAVACALL_TRUE : JAVACALL_FALSE);
    }
    
    if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException", 
            "\nCamera Focus Macro mode: setting to this mode is not "
            "supported\n");
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectFocusControl_nGetMacro)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool macro = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_focus_control_get_macro(
            pKniInfo->pNativeHandle, &macro);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == macro ? KNI_TRUE : KNI_FALSE);
}
