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

#include <string.h>
#include <midp_logging.h>
#include <emul.h>
#include <javacall_bt.h>

static char *emul_pin = "1234";

/* Piconet record type. */
typedef struct {
    /* Address of a remote device. */
    javacall_bt_address bdaddr;
    /* Default ACL handle of the connection. */
    int handle;
    /* Number of open connections. */
    int connections;
    /* Indicates whether the device has been authenticated. */
    int authenticated;
    /* Number of encrypted connections. */
    int encrypted;
} bcc_pico_t;

/* Piconect. */
static bcc_pico_t bcc_piconet[MAX_DEVICES];

/*
 * Searches piconet record by address.
 */
static bcc_pico_t *find_pico(const javacall_bt_address bdaddr)
{
    int i;
    for (i = 0; i < MAX_DEVICES; i++) {
        bcc_pico_t *pico = &bcc_piconet[i];
        if (!memcmp(pico->bdaddr, bdaddr, BT_ADDRESS_SIZE)) {
            return pico;
        }
    }
    
    return NULL;
}

/*
 * Searches piconet record by address, creates new if not found.
 */
static bcc_pico_t *get_pico(const javacall_bt_address bdaddr) {
    bcc_pico_t *pico = find_pico(bdaddr);
    
    if (pico == NULL) {
        javacall_bt_address null;
        memset(null, 0, sizeof(null));
        pico = find_pico(null);
        if (pico != NULL) {
            memcpy(pico->bdaddr, bdaddr, BT_ADDRESS_SIZE);
			pico->connections=0;
			pico->authenticated=0;
			pico->encrypted=0;
        }
    }
    
    return pico;
}
    


/*
 * Allocates BCC related native resources.
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL if an error occurred
 */
javacall_result bt_bcc_initialize()
{
    return JAVACALL_OK;
}

/*
 * Releases BCC related native resources.
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL if an error occurred
 */
javacall_result bt_bcc_finalize()
{
    return JAVACALL_OK;
}

/*
 * Asks user whether Bluetooth radio is allowed to be turned on.
 *
 * This function can be either synchronous or asynchronous.
 *
 * For Synchronous:
 *  1. return JAVACALL_OK on success.
 *  2. return JAVACALL_FAIL in case prompting the dialog failed.
 *
 * For Asynchronous:
 *  1. JAVACALL_WOULD_BLOCK is returned immediately. 
 *  2. The notification for the user confirmation will be sent later through 
 *      javanotify_bt_confirm_enable()
 *
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the user has allowed to enable Bluetooth,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL if an error occurred
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 */
javacall_result bt_bcc_confirm_enable(javacall_bool *retval)
{
    *retval = JAVACALL_TRUE;
    return JAVACALL_OK;
}

/*
 * Determines if the local device is in connectable mode.
 *
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the device is connectable,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_is_connectable(javacall_bool *retval)
{
    *retval = JAVACALL_TRUE;
    return JAVACALL_OK;
}

/*
 * Checks if the local device has a bond with a remote device.
 *
 * @param addr Bluetooth address of a remote device
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the devices are paired,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_is_paired(const javacall_bt_address bdaddr, javacall_bool *retval)
{
    (void)bdaddr;
    /* returns false to force display of PIN entry dialog */
    *retval = JAVACALL_FALSE;
    return JAVACALL_OK;
}

/*
 * Checks if a remote device is trusted (authorized for all services).
 *
 * It is possible that the user is allowed by BCC to permit an access 
 * to all local services for a remote device. Such device is called 
 * "trusted device".
 *
 * @param addr Bluetooth address of a remote device
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the remote device is trusted,
 *         JAVACALL_FALSE otherwise
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_is_trusted(
        const javacall_bt_address addr, 
        /*OUT*/javacall_bool *pBool)
{
    (void)addr;
    *pBool = JAVACALL_FALSE;
    return JAVACALL_OK;
}

/*
 * Retrieves PIN code to use for pairing with a remote device. If the
 * PIN code is not known, PIN entry dialog is displayed.
 *
 * @param bdaddr the Bluetooth address of the remote device
 * @param pin array to receive the PIN code (null-terminated UTF-8 encoded)
 * @param ask indicates whether PIN can be retrieved from cache, or user must be
 *         asked to enter one (regardless whether cached PIN is available)
 * @return JAVACALL_OK on success,
 *         JAVACALL_FAIL on failure,
 *         JAVACALL_WOULD_BLOCK if async dialog was displayed
 */
javacall_result bt_bcc_put_passkey(const javacall_bt_address bdaddr, bt_string_t pin,
        javacall_bool ask)
{
    (void)bdaddr;
    (void)ask;
    memcpy(pin, emul_pin, strlen(emul_pin) + 1);
    return JAVACALL_OK;
}

/*
 * Attempts to authorize a connection.
 *
 * The BCC may be expected to consult with the user to obtain approval 
 * based on the given service record and client device information.
 *
 * This function should be called after javanotify_bt_authorize_request() 
 *
 * @param addr Bluetooth address of a remote device
 * @param record handle for the service record of the srvice the remote
 *         device is trying to access
 * @param retval pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the device was paired,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL if an error occurred
 */
javacall_result bt_bcc_authorize(const javacall_bt_address bdaddr, 
        javacall_handle record,
        javacall_bool *retval)
{
    (void)bdaddr;
    (void)record;
    *retval = JAVACALL_TRUE;
    return JAVACALL_OK;
}

/*
 * Returns list of preknown devices.
 *
 * @param devices an output array which will receive the list of preknown devices
 * @param pCount pointer to variable where the result is to be stored:
 *         number of records stored in the output array.
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_get_preknown_devices(javacall_bt_address devices[],
        int *pCount)
{
    (void)devices;
    *pCount = 0;
    return JAVACALL_OK;
}

