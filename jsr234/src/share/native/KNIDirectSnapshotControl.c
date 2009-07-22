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
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1);
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        if (JAVACALL_TRUE == javacall_amms_snapshot_control_is_supported(
            pKniInfo->pNativeHandle)) {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean(ret);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nSetDirectory)
{
    setUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_set_directory,
        jsropIllegalArgumentException,
        "SnapshotControl.setDirectory() invalid directory");

    KNI_ReturnVoid();
}
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nGetDirectory)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    getUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_get_directory, hMod);
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nSetFilePrefix)
{
    setUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_set_file_prefix,
        jsropIllegalArgumentException,
        "SnapshotControl: attempt to set bad file prefix");

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nGetFilePrefix)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    getUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_get_file_prefix, hMod);
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nSetFileSuffix)
{
    setUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_set_file_suffix,
        jsropIllegalArgumentException,
        "SnapshotControl: attempt to set bad file suffix");

    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nGetFileSuffix)
{
    KNI_StartHandles(1);
    KNI_DeclareHandle(hMod);
    getUTF16StringWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_get_file_suffix, hMod);
    
    KNI_EndHandlesAndReturnObject(hMod);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nStop)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if (pKniInfo != NULL) {
        javacall_amms_snapshot_control_stop(pKniInfo->pNativeHandle);
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_amms_directcontrol_DirectSnapshotControl_nUnfreeze)
{
    setBoolWithTypicalJavacallFunc(KNIPASSARGS
        javacall_amms_snapshot_control_unfreeze);

    KNI_ReturnVoid();
}
