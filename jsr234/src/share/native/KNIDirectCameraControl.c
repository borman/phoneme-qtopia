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
#include "javacall_multimedia_advanced.h"
#include "jsrop_exceptions.h"

#include "KNICommon.h"
#include "jsr234_control.h"

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_camera_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nGetCameraRotation)
{
    jint res = getIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_camera_rotation);
    KNI_ReturnInt(-1 == res ? JAVACALL_AMMS_CAMERA_ROTATE_UNKNOWN : res);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nEnableShutterFeedback)
{
    javacall_result res = setBoolWithTypicalJavacallFunc(KNIPASSARGS
         javacall_amms_camera_control_enable_shutter_feedback);

    if (JAVACALL_OK != res) {
        if (JAVACALL_INVALID_ARGUMENT == res) {
            KNI_ThrowNew(jsropRuntimeException,
                "\nWrong arguments passed to "
                "DirectCameraControl.nEnableShutterFeedback()\n");
        } else {
            KNI_ThrowNew("javax/microedition/media/MediaException",
                "\nCamera Control: setting shutter feedback is "
                "not possible\n");
        }
    }

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nIsShutterFeedbackEnabled)
{
    KNI_ReturnBoolean(getBoolWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_is_shutter_feedback_enabled));
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedExposureModesNum)
{
    KNI_ReturnInt((jint)getIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_supported_exposure_mode_count));
}

KNIEXPORT KNI_RETURNTYPE_OBJECT KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedExposureMode)
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
        res = javacall_amms_camera_control_get_supported_exposure_mode(
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
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nSetExposureMode)
{
    setStringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_set_exposure_mode,
        jsropIllegalArgumentException,
        "\nCameraControl: this exposure mode is not supported\n");

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nGetExposureMode)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    getStringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_exposure_mode, hMod);

    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedVideoResolutionsNum)
{
    KNI_ReturnInt((jint)getIntWithTypicalJavacallFunc(KNIPASSARGS
         javacall_amms_camera_control_get_supported_video_resolution_count));
}

KNIEXPORT KNI_RETURNTYPE_VOID KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedVideoResolution)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jint index = KNI_GetParameterAsInt(2);
    long width = -1;
    long height = -1;
    KNI_StartHandles(1);
    KNI_DeclareHandle(hDim);
    KNI_GetParameterAsObject(3, hDim);
    
    KNI_SetIntArrayElement(hDim, 0, -1);
    KNI_SetIntArrayElement(hDim, 1, -1);
    
    if (pKniInfo != NULL) {
        res = javacall_amms_camera_control_get_supported_video_resolution(
            pKniInfo->pNativeHandle, index, &width, &height);
        if (JAVACALL_OK == res) {
            KNI_SetIntArrayElement(hDim, 0, width);
            KNI_SetIntArrayElement(hDim, 1, height);
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnVoid();

}

KNIEXPORT KNI_RETURNTYPE_INT KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedStillResolutionsNum)
{
    KNI_ReturnInt((jint)getIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_supported_still_resolution_count));
}

KNIEXPORT KNI_RETURNTYPE_VOID KNIDECL(
com_sun_amms_directcontrol_DirectCameraControl_nGetSupportedStillResolution)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jint index = KNI_GetParameterAsInt(2);
    long width = -1;
    long height = -1;
    KNI_StartHandles(1);
    KNI_DeclareHandle(hDim);
    KNI_GetParameterAsObject(3, hDim);
    
    KNI_SetIntArrayElement(hDim, 0, -1);
    KNI_SetIntArrayElement(hDim, 1, -1);
    
    if (pKniInfo != NULL) {
        res = javacall_amms_camera_control_get_supported_still_resolution(
             pKniInfo->pNativeHandle, index, &width, &height);
        if (JAVACALL_OK == res) {
            KNI_SetIntArrayElement(hDim, 0, width);
            KNI_SetIntArrayElement(hDim, 1, height);
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nSetVideoResolution)
{
    setIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_set_video_resolution,
        jsropIllegalArgumentException,
        "\nCameraControl: the given resolution is not supported\n");
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nSetStillResolution)
{
    setIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_set_still_resolution,
        jsropIllegalArgumentException,
        "\nCameraControl: the given resolution is not supported\n");
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nGetVideoResolution)
{
    KNI_ReturnInt((jint)getIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_video_resolution));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectCameraControl_nGetStillResolution)
{
    KNI_ReturnInt((jint)getIntWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_camera_control_get_still_resolution));
}
