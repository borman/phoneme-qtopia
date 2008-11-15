/*
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

#include <javacall_pim.h>

#include <kni.h>

static void closeList(KNIDECLARGS jobject objectHandle, jfieldID handle_id) {
    jfieldID native_handle_id;
    jint list_handle;

    KNI_StartHandles(2);
    KNI_DeclareHandle(listHandle);
    KNI_DeclareHandle(listClassHandle);

    if (NULL != handle_id) {
        KNI_GetObjectField(objectHandle, handle_id, listHandle);
        if (KNI_FALSE == KNI_IsNullHandle(listHandle)) {
            KNI_GetObjectClass(listHandle, listClassHandle);
            native_handle_id = KNI_GetFieldID(listClassHandle, "handle", "I");
            if (NULL != native_handle_id) {
                list_handle = KNI_GetIntField(listHandle, native_handle_id);
                javacall_pim_list_close((javacall_handle)list_handle);
            }
        }
    }

    KNI_EndHandles();
}

/*
 * private native void finalize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_j2me_pim_AbstractPIMList_finalize) {
    jfieldID open_id;

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(classHandle);
  
    KNI_GetThisPointer(objectHandle);
    KNI_GetObjectClass(objectHandle, classHandle);

    open_id = KNI_GetFieldID(classHandle, "open", "Z");
    if (NULL != open_id &&
        KNI_TRUE == KNI_GetBooleanField(objectHandle, open_id)) {
        closeList(KNIPASSARGS objectHandle,
            KNI_GetFieldID(classHandle, "handle", "java/lang/Object"));
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private native void finalize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_j2me_pim_AbstractPIMItem_finalize) {
    jfieldID dummy_id;

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(classHandle);
  
    KNI_GetThisPointer(objectHandle);
    KNI_GetObjectClass(objectHandle, classHandle);

    dummy_id = KNI_GetFieldID(classHandle, "dummyList", "Z");
    if (NULL != dummy_id &&
        KNI_TRUE == KNI_GetBooleanField(objectHandle, dummy_id)) {
        closeList(KNIPASSARGS objectHandle,
            KNI_GetFieldID(classHandle, "pimListHandle", "java/lang/Object"));
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}
