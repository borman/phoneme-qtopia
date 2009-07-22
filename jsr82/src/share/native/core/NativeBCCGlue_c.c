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

#include <stdlib.h>
#include <sni.h>

#include <jsrop_memory.h>
#include <javacall_bt.h>
/* IMPL_NOTE: revisit */
/* #include <btCommon.h> */
#include "btUtils.h"
#include "btMacros.h"

#define KNI_BOOL(expr) ((expr) ? KNI_TRUE : KNI_FALSE)


/*
 * Allocates native resources.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_initialize)
{
    javacall_bt_bcc_initialize();
    KNI_ReturnVoid();
}

/*
 * Releases native resources.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_finalize)
{
    javacall_bt_bcc_finalize();
    KNI_ReturnVoid();
}

/*
 * Asks user whether Bluetooth radio is allowed to be turned on.
 *
 * @return true if user has allowed to enable Bluetooth, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_confirmEnable)
{
    javacall_bool retval = JAVACALL_FALSE;
    KNI_TEST(javacall_bt_bcc_confirm_enable(&retval) == JAVACALL_OK);
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Determines if the local device is in connectable mode.
 *
 * @return true if the device is connectable, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isConnectable)
{
    javacall_bool retval = JAVACALL_FALSE;
    KNI_TEST(javacall_bt_bcc_is_connectable(&retval) == JAVACALL_OK);
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Checks if the local device has a bond with a remote device.
 *
 * @param address Bluetooth address of a remote device
 * @return true if the two devices were paired, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isPaired)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_is_paired(addr, &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Checks if a remote device was authenticated.
 *
 * @param address Bluetooth address of a remote device
 * @return true if the device was authenticated, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isAuthenticated)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_is_authenticated(addr, &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Checks if a remote device is trusted (authorized for all services).
 *
 * @param address Bluetooth address of a remote device
 * @return true if the device is trusted, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isTrusted)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_is_trusted(addr, &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Determines if data exchanges with a remote device are being encrypted.
 *
 * @param address Bluetooth address of a remote device
 * @return true if connection to the device is encrypted, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isEncrypted)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_is_encrypted(addr, &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Initiates pairing with a remote device.
 *
 * @param address the Bluetooth address of the device with which to pair
 * @param pin an array containing the PIN code
 * @return true if the device was authenticated, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_bond)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    char *pin;
    KNI_StartHandles(2);
    KNI_DeclareHandle(addressHandle);
    KNI_DeclareHandle(pinHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    KNI_GetParameterAsObject(2, pinHandle);
    getBtAddr(addressHandle, addr);
    pin = jStringToChars(pinHandle);
    KNI_TEST(javacall_bt_bcc_bond(addr, pin, &retval) == JAVACALL_OK);
    JAVAME_FREE(pin);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Authorizes a Bluetooth connection.
 *
 * @param address Bluetooth address of a remote device
 * @param handle handle for the service record of the srvice the remote
 *         device is trying to access
 * @return true if authorization succeeded, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_authorize)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_authorize(addr,
            (javacall_handle)KNI_GetParameterAsInt(2), &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Returns list of preknown devices in a packed string.
 *
 * @return packed string containing preknown devices
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_getPreknown)
{
    javacall_bt_address devices[JAVACALL_BT_MAX_PREKNOWN_DEVICES];
    char buffer[JAVACALL_BT_MAX_PREKNOWN_DEVICES * 13];
    int count;
    KNI_StartHandles(1);
    KNI_DeclareHandle(stringHandle);
    KNI_TEST(javacall_bt_bcc_get_preknown_devices(devices, &count) ==
            JAVACALL_OK);
        if (count > 0) {
            int i, j;
            char *ptr = buffer;
            for (i = 0; i < count; i++) {
                for (j = 0; j < 6; j++) {
                    *ptr++ = int2hex(devices[i][5 - j] >> 4);
                    *ptr++ = int2hex(devices[i][5 - j] & 0x0f);
                }
                if (i < count - 1) {
                    *ptr++ = ':';
                }
            }
            *ptr = '\0';
            KNI_NewStringUTF(buffer, stringHandle);
        } else {
            KNI_ReleaseHandle(stringHandle);
        }
    KNI_EndHandlesAndReturnObject(stringHandle);
}

/*
 * Checks if there is a connection to the remote device.
 *
 * @param address the Bluetooth address of the remote device
 * @return true if connection is established with the remote device
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_isConnected)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_is_connected(addr, &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnBoolean(KNI_BOOL(retval));
}

/*
 * Increases or decreases encryption request counter for a remote device.
 *
 * @param address the Bluetooth address of the remote device
 * @param enable indicated whether the encryption needs to be enabled
 * @return true if the encryption needs to been changed, false otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_jsr082_bluetooth_NativeBCC_setEncryption)
{
    javacall_bool retval = JAVACALL_FALSE;
    javacall_bt_address addr;
    KNI_StartHandles(1);
    KNI_DeclareHandle(addressHandle);
    KNI_GetParameterAsObject(1, addressHandle);
    getBtAddr(addressHandle, addr);
    KNI_TEST(javacall_bt_bcc_set_encryption(addr, KNI_GetParameterAsBoolean(2),
            &retval) == JAVACALL_OK);
    KNI_EndHandles();
    KNI_ReturnInt(KNI_BOOL(retval));
}
