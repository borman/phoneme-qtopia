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

#define KNI_BOOL(b) ((b) ? KNI_TRUE : KNI_FALSE)

/* Maximum name length is 248 according to the spec.
   However, we use only this many bytes in order to avoid stack overflow. */
#define MAX_NAME_LENGTH 128

static const char *szBluetoothStateException =
    "javax/bluetooth/BluetoothStateException";

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_initialize(void)
{
    KNI_ReturnBoolean(javacall_bt_stack_initialize() == JAVACALL_OK ?
            KNI_TRUE : KNI_FALSE);
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_jsr082_bluetooth_BluetoothStack_finalize(void)
{
    javacall_bt_stack_finalize();
    KNI_ReturnVoid();
}

/*
 * Checks if the Bluetooth radio is enabled.
 *
 * @return true if Bluetooth is enabled, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_isEnabled(void)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_stack_is_enabled(&retval);
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Enables Bluetooth radio.
 *
 * @return true if Bluetooth is enabled, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_enable(void)
{
    KNI_ReturnBoolean(KNI_BOOL(javacall_bt_stack_enable() == JAVACALL_OK));
}

/*
 * Returns Bluetooth address of the local device.
 *
 * @return Bluetooth address of the local device, or null if
 *         the address could not be retrieved
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_jsr082_bluetooth_BluetoothStack_getLocalAddress(void)
{
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(stringHandle);
    if (javacall_bt_stack_get_local_address(&addr) == JAVACALL_OK) {
	    char addrstr[13];
	    sprintf(addrstr, "%02X%02X%02X%02X%02X%02X", addr[5], addr[4],
	            addr[3], addr[2], addr[1], addr[0]);
        KNI_NewStringUTF(addrstr, stringHandle);
    } else {
        KNI_ReleaseHandle(stringHandle);
    }
    KNI_EndHandlesAndReturnObject(stringHandle);
}

/*
 * Returns user-friendly name for the local device.
 *
 * @return User-friendly name for the local device, or null if
 *         the name could not be retrieved
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_jsr082_bluetooth_BluetoothStack_getLocalName(void)
{
    char name[MAX_NAME_LENGTH + 1];
    KNI_StartHandles(1);
    KNI_DeclareHandle(stringHandle);
    if (javacall_bt_stack_get_local_name(name) == JAVACALL_OK) {
        KNI_NewStringUTF(name, stringHandle);
    } else {
        KNI_ReleaseHandle(stringHandle);
    }
    KNI_EndHandlesAndReturnObject(stringHandle);
}

/*
 * Retrieves the class of device value that represents the service classes,
 * major device class, and minor device class of the local device.
 *
 * @return class of device value, or -1 if the information could not
 *         be retrieved
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_BluetoothStack_getDeviceClass(void)
{
    jint retval = -1;
    javacall_bt_stack_get_device_class(&retval);
    KNI_ReturnInt(retval);
}

/*
 * Sets major service class bits of the device.
 *
 * @param classes an integer whose binary representation indicates the major
 *        service class bits that should be set
 * @return true if the operation succeeded, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_setServiceClasses(void)
{
    KNI_ReturnBoolean(KNI_BOOL(javacall_bt_stack_set_service_classes(
            KNI_GetParameterAsInt(1)) == JAVACALL_OK));
}

/*
 * Retrieves the inquiry access code that the local Bluetooth device is
 * scanning for during inquiry scans.
 *
 * @return inquiry access code, or -1 if the information could not
 *         be retrieved
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_BluetoothStack_getAccessCode(void)
{
    jint retval = -1;
    javacall_bt_stack_get_access_code(&retval);
    KNI_ReturnInt(retval);
}

/*
 * Sets the inquiry access code that the local Bluetooth device is
 * scanning for during inquiry scans.
 *
 * @param accessCode inquiry access code to be set (valid values are in the
 *        range 0x9e8b00 to 0x9e8b3f), or 0 to take the device out of
 *        discoverable mode
 * @return true if the operation succeeded, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_setAccessCode(void)
{
    KNI_ReturnBoolean(KNI_BOOL(javacall_bt_stack_set_access_code(
            KNI_GetParameterAsInt(1)) == JAVACALL_OK));
}

/*
 * Retrieves default ACL connection handle for the specified remote device.
 *
 * @param addr the Bluetooth address of the remote device
 * @return ACL connection handle value
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_jsr082_bluetooth_BluetoothStack_getHandle(void)
{
    int retval;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addrHandle);
    KNI_GetParameterAsObject(1, addrHandle);
    getBtAddr(addrHandle, addr);
    javacall_bt_stack_get_acl_handle(addr, &retval);
    KNI_EndHandles();
    KNI_ReturnInt(retval);
}

/*
 * Passes device discovery request to the Bluetooth stack implementation.
 *
 * @param accessCode the type of inquiry
 * @return <code>true</code> if the operation was accepted,
 *         <code>false</code> otherwise
 * @throws BluetoothStateException if the device does not allow an inquiry
 *                                 due to other operations being performed
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_startInquiry(void)
{
    KNI_ReturnBoolean(KNI_BOOL(javacall_bt_stack_start_inquiry(
            KNI_GetParameterAsInt(1)) == JAVACALL_OK));
}

/*
 * Passes cancellation of device discovery request to the Bluetooth stack
 * implementation.
 *
 * @return <code>true</code> if the operation was accepted,
 *         <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_cancelInquiry(void)
{
    KNI_ReturnBoolean(KNI_BOOL(
            javacall_bt_stack_cancel_inquiry() == JAVACALL_OK));
}

/*
 * Passes remote device's friendly name acquisition request to the Bluetooth
 * stack implementation.
 *
 * @param addr Bluetooth address of the remote device
 * @return <code>true</code> if the operation was accepted,
 *         <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_askFriendlyName(void)
{
    javacall_bt_address addr;
    jboolean retval = KNI_FALSE;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addrHandle);
    KNI_GetParameterAsObject(1, addrHandle);
    getBtAddr(addrHandle, addr);
    retval = KNI_BOOL(javacall_bt_stack_ask_friendly_name(addr) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(retval);
}

/*
 * Passes authentication request to the Bluetooth stack implementation.
 *
 * @param addr the Bluetooth address of the remote device
 * @return <code>true</code> if the operation was accepted,
 *         <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_authenticate(void)
{
    javacall_bt_address addr;
    jboolean retval = KNI_FALSE;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addrHandle);
    KNI_GetParameterAsObject(1, addrHandle);
    getBtAddr(addrHandle, addr);
    retval = KNI_BOOL(javacall_bt_stack_authenticate(addr) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(retval);
}

/*
 * Passes set encryption request to the Bluetooth stack implementation.
 *
 * @param addr the Bluetooth address of the remote device
 * @param enable <code>true</code> if the encryption needs to be enabled,
 *               <code>false</code> otherwise
 * @return <code>true</code> if the operation was accepted,
 *         <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_jsr082_bluetooth_BluetoothStack_encrypt(void)
{
    javacall_bt_address addr;
    jboolean retval = KNI_FALSE;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addrHandle);
    KNI_GetParameterAsObject(1, addrHandle);
    getBtAddr(addrHandle, addr);
    retval = KNI_BOOL(javacall_bt_stack_encrypt(addr,
            KNI_GetParameterAsBoolean(2) ?
            JAVACALL_TRUE : JAVACALL_FALSE) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(retval);
}

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
