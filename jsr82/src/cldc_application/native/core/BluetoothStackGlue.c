/*
 *
 *
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

#include <javacall_bt.h>
#include <kni.h>
#include <commonKNIMacros.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>
#include <midpString.h>
#include <string.h>
#include <stdio.h>
#include <btCommon.h>
#include <btUtils.h>
#include <btStackEvent.h>
#define KNI_BOOL(b) ((b) ? KNI_TRUE : KNI_FALSE)


/*
 * Checks if Bluetooth events are available for retrieval from the Bluetooth
 * stack implementation.
 *
 * @return <code>true</code> if there are pending events,
 *         <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_checkEvents(void)
{
    javacall_bool retval = JAVACALL_FALSE;
    bt_stack_check_events(&retval);
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Reads binary event data from the Bluetooth stack implementation.
 *
 * @param data byte array to be filled with data
 * @return number of bytes read
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_BluetoothStack_readData(void)
{
    int retval = -1;
    int size;
    unsigned char *data;
    KNI_StartHandles(1);
    KNI_DeclareHandle(dataHandle);
    KNI_GetParameterAsObject(1, dataHandle);
    size = KNI_GetArrayLength(dataHandle);
    data = (unsigned char *)JavaByteArray(dataHandle);
    bt_stack_read_data(data, size, &retval);
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}

/*
 * Creates Java String object from UTF-8 encoded string.
 *
 * @param buffer buffer containing the string in UTF-8 format
 * @param offset offset of the first character in the buffer
 * @param length length of the encoded string in bytes
 * @return Java String object containing the string in UTF-2 format
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_jsr082_bluetooth_JavacallBluetoothStack_stringUTF8(void)
{
    int size, offset, length;
    char *string;
    KNI_StartHandles(2);
    KNI_DeclareHandle(bufferHandle);
    KNI_DeclareHandle(stringHandle);
    KNI_GetParameterAsObject(1, bufferHandle);
    size = KNI_GetArrayLength(bufferHandle);
    offset = KNI_GetParameterAsInt(2);
    length = KNI_GetParameterAsInt(3);
    string = (char *)JavaByteArray(bufferHandle) + offset;
    if (offset + length < size) {
        *(string + length) = '\0';
    } else {
        *(string - offset + size - 1) = '\0';
    }
    KNI_NewStringUTF(string, stringHandle);
    KNI_EndHandlesAndReturnObject(stringHandle);
}
