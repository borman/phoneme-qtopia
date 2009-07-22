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
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_exposure_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);    
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedFStopsNumber)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_fstops_count(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedFStopByIndex)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;
    jint index = KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_fstop( 
            pKniInfo->pNativeHandle, (long)index, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetFStop)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_fstop(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nSetFStop)
{
    setIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_exposure_control_set_fstop,
        "javax/microedition/media/MediaException",
        "\nSetting this aperture value for camera exposure is not supported\n");
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetMinExposureTime)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_min_exposure_time(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetMaxExposureTime)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_max_exposure_time(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetExposureTime)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = -1;

    if (pKniInfo != NULL)
    {
        res = javacall_amms_exposure_control_get_exposure_time(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nSetExposureTime)
{
    KNI_ReturnInt(setGetIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_exposure_control_set_exposure_time,
        "javax/microedition/media/MediaException",
        "\nTried to set camera exposure time outside of the supported "
        "range\n"));
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedISOsNumber)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_isos_count(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);

}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedISOByIndex)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;
    jint index = KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_iso(
            pKniInfo->pNativeHandle, (long)index, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetISO)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_iso(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nSetISO)
{
    setIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_exposure_control_set_iso,
        "javax/microedition/media/MediaException",
        "\nSetting this camera sensitivity value is not supported\n");
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedExpCompsNumber)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_exposure_compensations_count(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedExpCompByIndex)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;
    jint index = KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res =
            javacall_amms_exposure_control_get_supported_exposure_compensation(
            pKniInfo->pNativeHandle, (long)index, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetExposureCompensation)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_exposure_compensation(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_VOID KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nSetExposureCompensation)
{
    setIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_exposure_control_set_exposure_compensation,
        "javax/microedition/media/MediaException",
        "\nSetting this exposure compensation value is not supported\n");
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetExposureValue)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long zoom = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_exposure_value(
            pKniInfo->pNativeHandle, &zoom);
    }

    KNI_ReturnInt((jint)zoom);
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedLightMtrsNumber)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res =
            javacall_amms_exposure_control_get_supported_light_meterings_count(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT KNIDECL(
com_sun_amms_directcontrol_DirectExposureControl_nGetSupportedLightMtrByIndex)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[KNI_BUFFER_SIZE];
    long index = (long)KNI_GetParameterAsInt(2);

    modulation[0] = 0;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    if (pKniInfo != NULL) {
        res = javacall_amms_exposure_control_get_supported_light_metering(
            pKniInfo->pNativeHandle, index, modulation, sizeof(modulation));
    }
    if (res == JAVACALL_OK) {
        KNI_NewStringUTF(modulation, hMod);
    } else {
        KNI_ReleaseHandle(hMod);
    }
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nSetLightMetering) {
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char name[KNI_BUFFER_SIZE + 1];

    if (pKniInfo != NULL &&
        (JAVACALL_OK == getUTF8StringFromParameter(KNIPASSARGS 2, name))) {
        res = javacall_amms_exposure_control_set_light_metering(
            pKniInfo->pNativeHandle, name);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nCamera setLightMetering: the given metering is not supported\n");
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectExposureControl_nGetLightMetering)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    getStringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_exposure_control_get_light_metering, hMod);
    
    KNI_EndHandlesAndReturnObject(hMod);
}

