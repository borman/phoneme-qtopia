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
#include "javautil_unicode.h"
#include "fcUtil.h"


/**
 * Converts suite ID into its string representation.
 *
 * @param id suite ID to convert.
 * @param buf pre-allocated buffer the result will be written to.
 * @param len on input, pointer to the available buffer size;
 *            on output, pointer to store the resulting string length.
 * @return JAVACALL_OK on success, error code otherwise.
 */
javacall_result jsr75_get_suite_id_string(javacall_int32 id,
    /* OUT */ javacall_utf16 *buf, /* IN | OUT */ int *len) {
    javacall_result res = javautil_unicode_from_int32(id, buf, *len);
    if (JAVACALL_OK != res) {
        return res;
    }
    return javautil_unicode_utf16_ulength(buf, len);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_cdc_io_j2me_file_DefaultFileHandler_getSuiteIdString) {
    jint id = KNI_GetParameterAsInt(1);
    javacall_utf16 ids[MAX_ID_LENGTH] = { 0 };
    javacall_int32 len = MAX_ID_LENGTH;

    KNI_StartHandles(1);
    KNI_DeclareHandle(idStr);

    if (JAVACALL_OK == jsr75_get_suite_id_string(id, ids, &len)) {
        KNI_NewString(ids, len, idStr);
    } else {
        KNI_ReleaseHandle(idStr);
    }
    KNI_EndHandlesAndReturnObject(idStr);
}
