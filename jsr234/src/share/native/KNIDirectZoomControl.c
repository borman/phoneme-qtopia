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
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_zoom_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);    
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nSetOpticalZoom)
{
    KNI_ReturnInt(setGetIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_zoom_control_set_optical_zoom,
        jsropIllegalArgumentException,
        "\nCamera Zoom setting: Bad optical zoom parameter\n"));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetOpticalZoom)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = JAVACALL_AMMS_ZOOM_UNKNOWN;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_optical_zoom(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetMaxOpticalZoom)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 100;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_max_optical_zoom(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetOpticalZoomLevels)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_optical_zoom_levels(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetMinFocalLength)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = JAVACALL_AMMS_ZOOM_UNKNOWN;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_min_focal_length(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nSetDigitalZoom)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_set_digital_zoom(
            pKniInfo->pNativeHandle, &zoom);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
          "\nCamera Zoom setting: Bad digital zoom parameter\n");
    }
    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetDigitalZoom)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = JAVACALL_AMMS_ZOOM_UNKNOWN;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_digital_zoom(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetMaxDigitalZoom)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 100;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_max_digital_zoom(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectZoomControl_nGetDigitalZoomLevels)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_zoom_control_get_digital_zoom_levels(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

