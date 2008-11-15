/*
 *
 *
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

#include "btPush.h"

#include <kni.h>
#include <commonKNIMacros.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>
#include <midpString.h>

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothPush_registerUrl(void)
{
    jboolean retval = KNI_FALSE;
    const void *data;
    jint size;
    MidpString wsUrl;
    char *szUrl;
    KNI_StartHandles(2);
    KNI_DeclareHandle(urlHandle);
    KNI_DeclareHandle(dataHandle);
    KNI_GetParameterAsObject(1, urlHandle);
    KNI_GetParameterAsObject(2, dataHandle);
    data = JavaByteArray(dataHandle);
    size = KNI_GetArrayLength(dataHandle);
    wsUrl = midpNewString(urlHandle);
    szUrl = midpJcharsToChars(wsUrl);
    if (bt_push_register_url(szUrl, data, size) == JAVACALL_OK) {
        retval = KNI_TRUE;
    }
    midpFree(szUrl);
    MIDP_FREE_STRING(wsUrl);
    KNI_EndHandles();
    KNI_ReturnBoolean(retval);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_BluetoothPush_getRecordHandle(void)
{
    jint retval = 0;
    MidpString wsUrl;
    char *szUrl;
    bt_port_t port;
    KNI_StartHandles(1);
    KNI_DeclareHandle(urlHandle);
    KNI_GetParameterAsObject(1, urlHandle);
    wsUrl = midpNewString(urlHandle);
    szUrl = midpJcharsToChars(wsUrl);
    if (bt_push_parse_url(szUrl, &port, NULL) == JAVACALL_OK) {
        retval = bt_push_get_record(&port);
    }
    midpFree(szUrl);
    MIDP_FREE_STRING(wsUrl);
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}
