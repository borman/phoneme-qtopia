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
//#include "midpServices.h"
#include "javacall_multimedia_advanced.h"
#include "jsrop_exceptions.h"

#include "jsr234_control.h"

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_tuner_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);    
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetMinFreq)
{
    jint handle = KNI_GetParameterAsInt(1);
    long ret = -1;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[KNI_BUFFER_SIZE + 1];

    if (pKniInfo != NULL && (JAVACALL_OK ==
        getUTF8StringFromParameter(KNIPASSARGS 2, modulation))) {
        res = javacall_amms_tuner_control_get_min_freq(
            pKniInfo->pNativeHandle, modulation, &ret);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner getMinFreq: unsupported modulation\n");
    }

    KNI_ReturnInt((jint)ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetMaxFreq)
{
    jint handle = KNI_GetParameterAsInt(1);
    long ret = -1;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[KNI_BUFFER_SIZE + 1];

    if (pKniInfo != NULL && (JAVACALL_OK ==
        getUTF8StringFromParameter(KNIPASSARGS 2, modulation))) {
        res = javacall_amms_tuner_control_get_max_freq(
            pKniInfo->pNativeHandle, modulation, &ret);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner getMaxFreq: unsupported modulation\n");
    }

    KNI_ReturnInt((jint)ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSetFrequency)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    char modulation[KNI_BUFFER_SIZE + 1];
    javacall_result res = JAVACALL_FAIL;
    long freq = ( long )KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL && (JAVACALL_OK ==
        getUTF8StringFromParameter(KNIPASSARGS 3, modulation))) {
        res = javacall_amms_tuner_control_set_frequency(
            pKniInfo->pNativeHandle, &freq, modulation);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner SetFrequency: unsupported modulation or frequency\n");
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetFrequency)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_frequency(
            pKniInfo->pNativeHandle, &freq);
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetSquelch)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    javacall_bool squelched = JAVACALL_FALSE;

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_squelch(
            pKniInfo->pNativeHandle, &squelched);
    }

    KNI_ReturnBoolean(JAVACALL_TRUE == squelched ? KNI_TRUE : KNI_FALSE);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSetSquelch)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean squelch = KNI_GetParameterAsBoolean(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_set_squelch(
            pKniInfo->pNativeHandle,
            KNI_TRUE == squelch ? JAVACALL_TRUE : JAVACALL_FALSE);
    }
    
    if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException",
            "\nTuner setSquelch(): this squelch setting is not supported\n");
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetModulation)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[KNI_BUFFER_SIZE];

    modulation[0] = 0;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_modulation(
            pKniInfo->pNativeHandle, modulation, sizeof(modulation));
    }
    if (res == JAVACALL_OK) {
        KNI_NewStringUTF(modulation, hMod);
    } else {
        KNI_ReleaseHandle(hMod);
    }
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetSignalStrength)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long strength = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_signal_strength(
            pKniInfo->pNativeHandle, &strength);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException",
            "\nTuner: querying the signal strength is not supported\n");
    }
    KNI_ReturnInt((jint)strength);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetStereoMode)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long mode = -1;

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_stereo_mode(
            pKniInfo->pNativeHandle, &mode);
    }

    KNI_ReturnInt((jint)mode);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSetStereoMode)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long mode = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_set_stereo_mode(
            pKniInfo->pNativeHandle, mode);
    }
    
    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner: this stereo mode is not supported\n");
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetNumberOfPresets)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long n = 0;

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_number_of_presets(
            pKniInfo->pNativeHandle, &n);
    }

    KNI_ReturnInt((jint)n);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nUsePreset)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long preset = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_use_preset(
            pKniInfo->pNativeHandle, preset);
    }
    
    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner: bad preset number was passed\n");
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSetPreset)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long preset = (long)KNI_GetParameterAsInt(2);
    long freq = (long)KNI_GetParameterAsInt(3);
    char modulation[KNI_BUFFER_SIZE];
    long stereoMode = (long)KNI_GetParameterAsInt(5);

    if (pKniInfo != NULL && (JAVACALL_OK ==
        getUTF8StringFromParameter(KNIPASSARGS 4, modulation))) {
        res = javacall_amms_tuner_control_set_preset(pKniInfo->pNativeHandle,
            preset, freq, modulation, stereoMode);
    }
    
    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner setPreset(): bad preset parameters are passed\n");
    }
    
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetPresetFrequency)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;
    long preset = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_preset_frequency(
            pKniInfo->pNativeHandle, preset, &freq);
    }
    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner: bad preset number was passed\n");
    }

    KNI_ReturnInt((jint)freq);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetPresetModulation)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char modulation[KNI_BUFFER_SIZE];
    long preset = (long)KNI_GetParameterAsInt(2);

    modulation[0] = 0;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_preset_modulation(
            pKniInfo->pNativeHandle, preset, modulation, sizeof(modulation));
        if (res == JAVACALL_OK) {
            KNI_NewStringUTF(modulation, hMod);
        } else {
            KNI_ReleaseHandle(hMod);
            KNI_ThrowNew(jsropIllegalArgumentException,
                "\nTuner: bad preset number was passed\n");
        }
    } else {
        KNI_ReleaseHandle(hMod);
    }
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetPresetStereoMode)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long mode = -1;
    long preset = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_preset_stereo_mode(
            pKniInfo->pNativeHandle, preset, &mode);
    }

    if (JAVACALL_INVALID_ARGUMENT == res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner: bad preset number was passed\n");
    } else if (JAVACALL_OK != res) {
        KNI_ThrowNew("javax/microedition/media/MediaException",
            "\nTuner: the presets do not support storing of the stereo mode\n");
    }
    
    KNI_ReturnInt((jint)mode);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nGetPresetName)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char name[KNI_BUFFER_SIZE];
    long preset = (long)KNI_GetParameterAsInt(2);

    name[0] = 0;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(nName);
    if (pKniInfo != NULL) {
        res = javacall_amms_tuner_control_get_preset_name(
            pKniInfo->pNativeHandle, preset, name, sizeof(name));
    }
    if (res == JAVACALL_OK) {
        KNI_NewStringUTF(name, nName);
    } else {
        KNI_ReleaseHandle(nName);
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner: bad preset number was passed\n");
    }
    
    KNI_EndHandlesAndReturnObject(nName);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectTunerControl_nSetPresetName)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char name[KNI_BUFFER_SIZE + 1];
    long preset = (long)KNI_GetParameterAsInt(2);

    if (pKniInfo != NULL && (JAVACALL_OK ==
        getUTF8StringFromParameter(KNIPASSARGS 3, name))) {
        res = javacall_amms_tuner_control_set_preset_name(
            pKniInfo->pNativeHandle, preset, name);
    }

    if (JAVACALL_OK != res) {
        KNI_ThrowNew(jsropIllegalArgumentException,
            "\nTuner setPresetName(): bad parameters passed\n");
    }

    KNI_ReturnVoid();
}

