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

#include <emul.h>
#include <midp_thread.h>
#include <sni.h>

#define APPEND_DEV_REQ(req) \
    APPEND_BYTE(DEVICE); \
    APPEND_BYTE(req);

static const char *deviceName = "emulName";

/*
 * Acquires the contorl of Bluetooth stack.
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_initialize(void)
{
    LOG("bt_stack_initialize()");
    ensureInitialized();
    return JAVACALL_OK;
}

/*
 * Releases the contorl of Bluetooth stack.
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_finalize(void)
{
    return JAVACALL_OK;
}

javacall_result bt_stack_is_initialized(javacall_bool *retval)
{
    ensureInitialized();
    *retval = JAVACALL_TRUE;
    return JAVACALL_OK;
}

/*
 * Checks if the Bluetooth radio is enabled.
 *
 * @param pBool pointer to variable where the result is to be stored:
 *              <code>JAVACALL_TRUE</code> if the Bluetooth radio is enabled,
 *              <code>JAVACALL_FALSE</code> otherwise
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_is_enabled(
        /*OUT*/javacall_bool *pBool)
{
    ensureInitialized();
    *pBool = state & BLUETOOTH_ON ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

void initDevice() {
    if (!(state & DEVICE_INITED)) {
        START_REQUEST
            APPEND_DEV_REQ(INIT_DEVICE);
        END_REQUEST
        if (NULL == SNI_GetReentryData(NULL)) {
            /* This condition preserves from calling midp_thread_wait
               twice without unblocking current thread. */
            midp_thread_wait(JSR82_SIGNAL, BTE_SIGNAL_HANDLE, NULL);
        }
    }
}

/*
 * Enables Bluetooth radio.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_event()</code>
 * with <code>JAVACALL_EVENT_BT_ENABLE_RADIO_COMPLETE</code> type.
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_stack_enable(void)
{
    ensureInitialized();
    initDevice();
    state |= BLUETOOTH_ON;
    return JAVACALL_OK;
}

/*
 * Returns Bluetooth address of the local device.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_event()</code>
 * with <code>JAVACALL_EVENT_BT_LOCAL_ADDRESS_COMPLETE</code> type.
 *
 * @param pAddr pointer to variable where the result is to be stored:
 *              Bluetooth address of the local device
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_stack_get_local_address(
        /*OUT*/javacall_bt_address *pAddr)
{
    initDevice();
    memcpy(pAddr, emul_data.local_addr, BTADDR_SIZE);
    return JAVACALL_OK;
}
    
/*
 * Retrieves user-friendly name for the local device.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_event()</code>
 * with <code>JAVACALL_EVENT_BT_LOCAL_NAME_COMPLETE</code> type.
 *
 * @param pName string to store the name of the local device
 *              (null-terminated in UTF-8 encoding). The length should be
 *              at least <code>JAVACALL_BT_MAX_USER_FRIENDLY_NAME</code> bytes
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_stack_get_local_name(char *pName)
{
    memcpy(pName, deviceName, strlen(deviceName));
    return JAVACALL_OK;
}

/*
 * Retrieves the class of local device value that represents the service 
 * classes, major device class, and minor device class of the local device.
 *
 * @param pValue pointer to variable where the result is to be stored:
 *               class of device value
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_get_device_class(
        /*OUT*/int *pValue)
{
    initDevice();
    *pValue = emul_data.device_class;
    return JAVACALL_OK;
}

/*
 * Sets major service class bits of the device.
 *
 * @param classes an integer whose binary representation indicates the major
 *                service class bits that should be set
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_set_service_classes(int classes)
{
    initDevice();

    emul_data.device_class = emul_data.device_class_base | classes;
    START_REQUEST
        APPEND_DEV_REQ(UPDATE_CLASS);
        APPEND_INT(classes);
    END_REQUEST
    
    return JAVACALL_OK;
}

/*
 * Retrieves the inquiry access code that the local Bluetooth device is
 * scanning for during inquiry scans.
 *
 * @param pValue pointer to variable where the result is to be stored:
 *               inquiry access code
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_get_access_code(
        /*OUT*/int *pValue)
{
    ensureInitialized();
    initDevice();
    *pValue = emul_data.access_code;
    return JAVACALL_OK;
}

/*
 * Sets the inquiry access code that the local Bluetooth device is
 * scanning for during inquiry scans.
 *
 * @param accessCode inquiry access code to be set (valid values are in the
 *                   range <code>0x9e8b00-0x9e8b3f</code>),
 *                   or <code>0</code> to take the device out
 *                   of discoverable mode
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_set_access_code(int accessCode)
{
    ensureInitialized();
    initDevice();
    emul_data.access_code = accessCode;
    START_REQUEST
        APPEND_DEV_REQ(UPDATE_ACCESS);
        APPEND_INT(emul_data.access_code);
    END_REQUEST
    return JAVACALL_OK;
}

/*
 * Retrieves default ACL connection handle for the specified remote device.
 *
 * @param bdaddr the Bluetooth address of the remote device
 * @param pHandle pointer to variable where the result is to be stored:
 *         default ACL connection handle in range 0x0000-0x0FFF, or
 *         <code>-1</code> when there is no connection to the remote device.
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_get_acl_handle(
		const javacall_bt_address addr, 
		int *pHandle)
{
    *pHandle = 0;
    memcpy(pHandle, addr, 2);
    return JAVACALL_OK;
}

/*
 * Starts asynchronous device discovery.
 *
 * Whenever a device is discovered,
 * <code>javanotify_bt_device_discovered()</code> gets called.
 *
 * The notification of inquiry completion will be sent via
 * <code>javanotify_bt_inquiry_complete()</code>.
 *
 * @param accessCode the type of inquiry
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_start_inquiry(int accessCode)
{
    LOG("bt_stack_start_inquiry()");
    START_REQUEST
        APPEND_DEV_REQ(START_INQUIRY);
        APPEND_INT(accessCode);
    END_REQUEST
    return JAVACALL_OK;
}

/*
 * Cancels asynchronous device discovery.
 *
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_cancel_inquiry(void)
{
    LOG("bt_stack_cancel_inquiry()");
    START_REQUEST
        APPEND_DEV_REQ(CANCEL_INQUIRY);
    END_REQUEST
    return JAVACALL_OK;
}
    
/*
 * Retrieves friendly name of the specified Bluetooth device.
 *
 * If this function should always peform asynchronously. Upon completion of the
 * operation, <code>javanotify_bt_remote_name_complete()</code> is expected
 * to be called.
 *
 * @param addr Bluetooth address of the device which name is to be retrieved
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_stack_ask_friendly_name(
        const javacall_bt_address addr)
{
    LOG("bt_stack_ask_friendly_name()");
    javanotify_bt_remote_name_complete(addr, deviceName);
    return JAVACALL_OK;
}

/*
 * Attempts to authenticate a remote device.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_remote_device_event()</code>
 * with <code>JAVACALL_EVENT_BT_AUTHENTICATE_COMPLETE</code> type.
 *
 * @param addr address of the remote device
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_stack_authenticate(
        const javacall_bt_address addr)
{
    LOG("bt_stack_authenticate()");
    javanotify_bt_authentication_complete(addr, JAVACALL_TRUE);
    return JAVACALL_OK;
}

/*
 * Attempts to change encryption for all connections to the remote device.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_remote_device_event()</code>
 * with <code>JAVACALL_EVENT_BT_ENCRYPT_COMPLETE</code> type.
 *
 * @param addr address of the remote device
 * @param enable specifies if encryption to be enabled or disabled
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_stack_encrypt(
        const javacall_bt_address addr,
        javacall_bool enable)
{
    LOG("bt_stack_encrypt()");
    if (enable) {
        javanotify_bt_encryption_change(addr, JAVACALL_TRUE, JAVACALL_TRUE);
    } else {
        javanotify_bt_encryption_change(addr, JAVACALL_TRUE, JAVACALL_FALSE);
    }
    return JAVACALL_OK;
}
