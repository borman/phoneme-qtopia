/*
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
#include <javacall_bt.h>
#include <javanotify_bt.h>
#include <btPush.h>

javacall_result bt_bcc_bond(const javacall_bt_address addr, const char* pin,
        javacall_bool *pBool);
javacall_result bt_bcc_set_encryption(const javacall_bt_address addr, javacall_bool enable,
        javacall_bool *pBool);

// Need revisit get rid of dupes with java - see ServiceConnectionData
/* Appends ServiceConnectionData image to emulation request. */
#define APPEND_ServiceConnectionData(/*int*/ protocol, /*int*/imtu, \
     /*int*/ omtu, /*javacall_bool*/ auth, /*javacall_bool*/ authz, \
     /*javacall_bool*/ enc, /*javacall_bool*/ master, /*int*/ psm) \
    { \
        int _misc; \
        LOG("putServiceConnectionData"); \
        APPEND_INT(-1 /* socket port, defined later in java */); \
        APPEND_INT(psm); \
        APPEND_INT(imtu); \
        APPEND_INT(omtu); \
        _misc = protocol; \
        _misc |= master? 4 : 0; \
        _misc |= enc? 8 : 0; \
        _misc |= authz? 32 : 0; \
        _misc |= auth? 16 : 0; \
        APPEND_BYTE(_misc); \
    }

#define APPEND_CONN_REQ(handle, req) \
    APPEND_BYTE(CONNECTIONS); \
    APPEND_BYTE(handle); \
    APPEND_BYTE(req);

/*
 * Gets the connection handle by address of the remote device.
 *
 * @param addr the Bluetooth address of the remote device
 * @retval <code>handle value</code> on success
 * @retval <code>BT_INVALID_HANDLE</code> when no connection found
 */
static int getConnHandle(const javacall_bt_address addr)
{
    int retval = 0;
	connection_info_t *info;
	for ( ; retval < MAX_CONN; retval++) {
        info = &emul_data.handled_info[retval].conn;
		if ((!memcmp(addr, info->peer_addr, BT_ADDRESS_SIZE)) &&
            (info->connect_status == JAVACALL_OK)) {
            return retval;
		}
	}
	return (int)BT_INVALID_HANDLE;
}

/*
 * Retrieves code and string description of the last occured error.
 *
 * @param handle connection handle
 * @param pErrStr pointer to string pointer initialized with
 *                    result string pointer,
                  if <code>NULL</code> error string is not returned
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
/*OPTIONAL*/ javacall_result bt_l2cap_get_error(javacall_handle handle,
    /*OUT*/ char** pErrStr)
{
    static char* empty = "";
    (void)handle;
    *pErrStr = empty;
    return JAVACALL_FAIL;
}

void resetConnInfo(javacall_handle handle) {
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
    if (info->in) {
        midpFree(info->in);
    }
    if (info->out) {
        midpFree(info->out);
    }
    memset(info, 0, sizeof(connection_info_t));
}

/*
 * Closes the connection.
 *
 * Determines whether the connection is not closed, if so closes it.
 *
 * @param handle connection handle
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL if connection is already closed or
 *                       an error occured during close operation
 */
javacall_result bt_l2cap_close(javacall_handle handle)
{
    LOG1("bt_l2cap_close(%d)", (int)handle);

    START_REQUEST
        APPEND_CONN_REQ(handle, NOTIF_CLOSE);
    END_REQUEST
    resetConnInfo(handle);

    // Notification is just ignored if it is not waited
    javanotify_bt_protocol_event(JAVACALL_EVENT_BT_ACCEPT_COMPLETE, handle,
       JAVACALL_FAIL);
    javanotify_bt_protocol_event(JAVACALL_EVENT_BT_RECEIVE_COMPLETE, handle,
       JAVACALL_FAIL);
    javanotify_bt_protocol_event(JAVACALL_EVENT_BT_SEND_COMPLETE, handle,
       JAVACALL_FAIL);
    javanotify_bt_protocol_event(JAVACALL_EVENT_BT_CONNECT_COMPLETE, handle,
       JAVACALL_FAIL);

    return JAVACALL_OK;
}

static void saveAuthEnc(javacall_handle handle, javacall_bool auth, javacall_bool enc) {
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;

    if (auth) {
        info->flags |= AUTHENTICATE;
	} else {
        info->flags &= ~AUTHENTICATE;
    }
    if (enc) {
        info->flags |= ENCRYPT;
	} else {
        info->flags &= ~ENCRYPT;
    }
}

static void checkAuthEnc(javacall_handle handle) {
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
    bt_string_t fakePin = NULL;
    javacall_bool fakeRetval;

    if (info->flags & AUTHENTICATE) {
        bt_bcc_bond(info->peer_addr, fakePin, &fakeRetval);
    }
    if (info->flags & ENCRYPT) {
        bt_bcc_set_encryption(info->peer_addr, JAVACALL_TRUE, &fakeRetval);
    }
}

int getPort(int handle, int protocol) {
    int port;

    if (L2CAP == protocol) {
        /*
        * Generate next free psm:
        *    - first call is always made to create SDPServer, it
        *      receives fixed value 1.
        *    - valid PSM for user-defined services are in the
        *      range (0x1001..0xFFFF), and the least significant
        *      byte must be odd and all other bytes must be even.
        *
        * There is no overflow check here for the highest value
        * is not reached in usual emulation mode scenarios.
        * SDP_REQ_EXPECTED state means requesting psm for SDP server,
        * which is 1.
        */
        if (state & SDP_REQ_EXPECTED) {
            state ^= SDP_REQ_EXPECTED;
            port = 1;
        } else {
            port = 0x1001 + 2 * handle;
        }
    } else {
        port = handle % 30 + 1;
    }

    return port;
}

javacall_result create_server(int protocol, int imtu, int omtu,
        javacall_bool auth, javacall_bool authz, javacall_bool enc, javacall_bool master,
        javacall_handle *pHandle, int *pPort) {

    LOG("create_server()");

    *pHandle = getNextHandle(protocol);

    if (BT_INVALID_HANDLE == *pHandle) {
        return JAVACALL_FAIL;
    }

    saveAuthEnc(*pHandle, auth, enc);

    *pPort = getPort((int)*pHandle, protocol);

    START_REQUEST
        APPEND_BYTE(CREATE_NOTIF);
        APPEND_BYTE(*pHandle);

        APPEND_CONN_REQ(*pHandle, NOTIF_INIT);
        APPEND_ServiceConnectionData(protocol, imtu, omtu, auth,
            authz, enc, master, *pPort);
    END_REQUEST
    return JAVACALL_OK;
}

/*
 * Creates a new server connection.
 *
 * The method creates a server connection instance
 * but does not put it in listen mode.
 * Anyway it selects and reserves a free PSM to listen for
 * incoming connections on after the listen method is called.
 *
 * Note: returned connection is put in non-blocking mode.
 *
 * @param imtu receive MTU or <code>-1</code> if not specified
 * @param omtu transmit MTU or <code>-1</code> if not specified
 * @param auth   <code>JAVACALL_TRUE</code> if authication is required
 * @param authz  <code>JAVACALL_TRUE</code> if authorization is required
 * @param enc    <code>JAVACALL_TRUE</code> if required to be encrypted
 * @param master <code>JAVACALL_TRUE</code> if required
 *                                         to be a connection's master
 * @param pHandle pointer to connection handle variable,
 *               new connection handle returned in result.
 * @param pPsm pointer to variable, where reserved PSM is returned in.
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_l2cap_create_server(int imtu, int omtu, javacall_bool auth,
    javacall_bool authz, javacall_bool enc, javacall_bool master, 
    /*OUT*/ javacall_handle* pHandle, /*OUT*/ int* pPsm)
{

    LOG("bt_l2cap_create_server()");
    return create_server(L2CAP,
        imtu, omtu,auth, authz, enc, master, pHandle, pPsm);
}

/*
 * Queues acception request to emulation. Emulation makes no difference
 * between listen and accept operations thus it can be called from
 * either listen() or accept_satrt(). This logic cannot be simply moved
 * to accept_start because Push requires the following scenario:
 * listen() - notifyAccepted() - non-blocking-accept_start().
 */
void request_accept(javacall_handle handle) {
    START_REQUEST
        APPEND_CONN_REQ(handle, NOTIF_ACCEPT);
    END_REQUEST
    emul_data.handled_info[(int)handle].accept.status = JAVACALL_WOULD_BLOCK;
}

/*
 * Puts server connection to listening mode.
 *
 * @param handle server connection handle
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_l2cap_listen(
        javacall_handle handle)
{

    LOG("bt_l2cap_listen()");

    request_accept(handle);
    // to avoid re-requesting in immediately following accept_start()
    emul_data.handled_info[(int)handle].accept.flags |= SKIP_REQUEST;

    return JAVACALL_OK;
}

/*
 * Accepts incoming L2CAP connection.
 *
 * @param handle server connection handle
 * @param pPeerHandle pointer to peer handle to store new connection handle
 *                    to work with accepted incoming client connection
 * @param pPeerAddr Bluetooth address variable to store
 *                  the address of accepted client,
 *                  if <code>NULL</code> the value is not returned
 * @param pReceiveMTU pointer to store receive MTU size for a new connection
 * @param pTransmitMTU pointer to store transmit MTU size for a new connection
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> if an error occurred
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_l2cap_accept(
        javacall_handle handle, 
        /*OUT*/javacall_handle *pPeerHandle,
        /*OUT*/javacall_bt_address *pPeerAddr,
        /*OUT*/int *pReceiveMTU,
        /*OUT*/int *pTransmitMTU)
{
    accept_info_t *info = &emul_data.handled_info[(int)handle].accept;
    int status;
	if (info->is_accepting == JAVACALL_FALSE) {
	    info->is_accepting = JAVACALL_TRUE;
        LOG1("bt_l2cap_accept_start(%d)", (int)handle);

        if (info->flags & SKIP_REQUEST) {
            info->flags ^= SKIP_REQUEST;
        } else {
            request_accept(handle);
        }

        // A connection may be already accepted if acception was initiated by
        // listen()
        status = bt_l2cap_accept(handle, pPeerHandle, pPeerAddr,
            pReceiveMTU, pTransmitMTU);
	} else { /* finish */
        LOG("bt_l2cap_accept_finish()");

        status = info->status;
        if (status != JAVACALL_WOULD_BLOCK) {
            info->is_accepting = JAVACALL_FALSE;
        }
        if (status == JAVACALL_OK) {
            connection_info_t *connInfo =
                &emul_data.handled_info[info->conn_handle].conn;
            *pPeerHandle = (javacall_handle) info->conn_handle;

            memcpy(*pPeerAddr, connInfo->peer_addr, BT_ADDRESS_SIZE);
            if (pReceiveMTU && pTransmitMTU) {
                *pReceiveMTU = connInfo->imtu;
                *pTransmitMTU = connInfo->omtu;
            }

            checkAuthEnc(handle);

            // It is a patch for push support. Normally accept notification
            // means that stack is ready to accept without blocking. Push
            // detects incoming connections by those notifications.
            // The same is not applied for the common case for it is not
            // absolutely due to the following emulation feature. Emulation
            // calls notification callback when incoming connection is already
            // accepted.

            if (BT_INVALID_PUSH_HANDLE != bt_push_find_server(handle)) {
                bt_l2cap_listen(handle);
            }
        }
	}
    return status;
}

javacall_result create_client(int protocol,
        int imtu, int omtu, javacall_bool auth, javacall_bool enc,
        javacall_bool master, javacall_handle *pHandle) {

    LOG("create_client()");

    *pHandle = getNextHandle(protocol);
    if (BT_INVALID_HANDLE == *pHandle) {
         return JAVACALL_FAIL;
    }

    saveAuthEnc(*pHandle, auth, enc);

    START_REQUEST
        APPEND_BYTE(CREATE_CONN);
        APPEND_BYTE(*pHandle);

        APPEND_CONN_REQ(*pHandle, CONN_INIT);
        APPEND_ServiceConnectionData(protocol, imtu, omtu, auth,
            JAVACALL_FALSE, enc, master, -1);
    END_REQUEST
    return JAVACALL_OK;
}


/*
 * Creates a new client connection.
 *
 * The method does not establishes real bluetooth connection
 * just creates a client connection instance.
 *
 * @param imtu Input MTU size, 0 means the default value
 * @param omtu Output MTU size, 0 means the default value
 * @param auth  JAVACALL_TRUE if authication is required
 * @param enc       JAVACALL_TRUE if required to be encrypted
 * @param master        JAVACALL_TRUE if required to be a connection's master
 * @param pHandle pointer to connection handle variable,
 *               new connection handle returned in result.
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_l2cap_create_client(int imtu, int omtu, javacall_bool auth,
    javacall_bool enc, javacall_bool master, /*OUT*/ javacall_handle* pHandle)
{

    LOG("bt_l2cap_create_client()");

    return create_client(L2CAP,
        imtu, omtu, auth, enc, master, pHandle);
}


/*
 * Establishes L2CAP connection with the Bluetooth device.
 *
 * If this function returns <code>JAVACALL_WOULD_BLOCK</code>, the notification
 * will be sent via <code>javanotify_bt_protocol_event()</code>
 * with <code>JAVACALL_EVENT_BT_CONNECT_COMPLETE</code> type.
 *
 * @param handle connection handle
 * @param addr pointer to the address of device to connect to
 * @param psm PSM port to connect to
 * @param pReceiveMTU pointer to variable to store negotiated receive MTU
 * @param pTransmitMTU pointer to variable to store negotiated transmit MTU
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 * @retval <code>JAVACALL_WOULD_BLOCK</code> in case of asynchronous operation
 */
javacall_result bt_l2cap_connect(
        javacall_handle handle,
        const javacall_bt_address addr,
        int psm,
        /*OUT*/int *pReceiveMTU,
        /*OUT*/int *pTransmitMTU)
{
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
	if (info->is_connecting == JAVACALL_FALSE) {
        info->is_connecting = JAVACALL_TRUE;
        LOG1("bt_l2cap_connect_start(), port %d", psm);

        memcpy(info->peer_addr, addr, BT_ADDRESS_SIZE);

        START_REQUEST
            APPEND_CONN_REQ(handle, CONN_OPEN);
            APPEND_BYTES(addr, BT_ADDRESS_SIZE);
            APPEND_INT(psm);
        END_REQUEST
        info->connect_status = JAVACALL_WOULD_BLOCK;
        return JAVACALL_WOULD_BLOCK;
	} else { /* finish */
        LOG1("bt_l2cap_connect_finish(%d)", (int)handle);

        if (info->connect_status != JAVACALL_WOULD_BLOCK) {
            info->is_connecting = JAVACALL_FALSE;
        }
        if (info->connect_status == JAVACALL_OK) {
            if (pReceiveMTU && pTransmitMTU) {
                *pReceiveMTU = info->imtu;
                *pTransmitMTU = info->omtu;
            }

            checkAuthEnc(handle);
        }

        return info->connect_status;
	}
}

/*
 * Sends data via connection.
 *
 * If the size of pData is greater than the Transmit MTU, 
 * then only the first Transmit MTU bytes of the packet are sent.
 * Even if size of the buffer is zero, an empty L2CAP packet should be sent.
 *
 * If JAVACALL_WOULD_BLOCK is returned,
 * this function should be called again after the notification through 
 * javanotify_bt_protocol_event() with JAVACALL_EVENT_BT_SEND_COMPLETE type. 
 *
 * @param handle connection handle
 * @param pData pointer to data buffer
 * @param len length of the data
 * @param pBytesSent number of bytes that were really sent
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_l2cap_send(javacall_handle handle,
        const char *pData, int len, /*OUT*/int *pBytesSent)
{
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
    int status;
	if (info->is_sending == JAVACALL_FALSE) {
        info->is_sending = JAVACALL_TRUE;
        status = JAVACALL_WOULD_BLOCK;
        LOG1("bt_l2cap_send_start(%d)", (int)handle);
        (void)pBytesSent;

        info->sent = CONN_FAILURE;
        START_REQUEST
            APPEND_CONN_REQ(handle, CONN_SEND);
            APPEND_INT(len);
        END_REQUEST
        APPEND_BUFFER(info->out, info->out_len, pData, len);
	} else { /* finish */
        info->is_sending = JAVACALL_FALSE;
        status = JAVACALL_OK;
        LOG1("bt_l2cap_send_finish(%d)", (int)handle);

        (void)pData;
        (void)len;

        if (info->sent == CONN_FAILURE) {
            status = JAVACALL_FAIL;
            *pBytesSent = -1;
        } else {
           *pBytesSent = info->sent;
        }

	}
    return status;
}

javacall_result receive_common(javacall_handle handle, char *pData, int len,
        int *pBytesReceived, int defaultStatus) {

    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
    int status = JAVACALL_OK;
    
    if (info->receive_status == JAVACALL_FAIL) {
        status = JAVACALL_FAIL;
        info->receive_status = JAVACALL_OK;
    
    } else if (info->flags & ENDOF_INP_REACHED) {
        *pBytesReceived = 0;
    
    } else if (info->in_len > 0) {
        if (!(info->flags & RFCOMM) && info->in_len > len) {
            // in case of L2CAP rest of data should be discarded
            info->in_len = len;
        }

        if (info->in_len <= len) {
            *pBytesReceived = info->in_len;
            memcpy(pData, info->in, info->in_len);
            midpFree(info->in);
            info->in = NULL;
            info->in_len = 0;

        } else {
            int restLen = info->in_len - len;
            *pBytesReceived = len;
            memcpy(pData, info->in, len);
            memmove(info->in, &info->in[len], restLen);
            info->in = midpRealloc(info->in, restLen);
            info->in_len = restLen;
        }

    } else {
        status = defaultStatus;
        *pBytesReceived = info->in_len;
    }

    return status;
}

static void requestReceive(javacall_handle handle, int len) {
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
    LOG("requestReceive()");
    (void)len;

    if (!(info->flags & SKIP_REQUEST)) {
        LOG1("requestReceive() makes request for handle %d", (int)handle);
        START_REQUEST
            APPEND_CONN_REQ(handle, CONN_RECEIVE);
        END_REQUEST
        info->flags |= SKIP_REQUEST;
    }
}

/*
 * Receives data via connection.
 *
 * If size of the buffer is less than size of the received packet,
 * the rest of the packet is discarded.
 *
 * If JAVACALL_WOULD_BLOCK is returned,
 * this function should be called again after the notification through 
 * javanotify_bt_protocol_event() with JAVACALL_EVENT_BT_RECEIVE_COMPLETE type. 
 *
 * @param handle connection handle
 * @param pData pointer to data buffer
 * @param len length of the buffer
 * @param pBytesReceived number of bytes that were received,
 *             0 indicates end-of-data
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_l2cap_receive(javacall_handle handle, /*OUT*/ char *pData,
        int len, /*OUT*/ int *pBytesReceived)
{
    int status;
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;
	if (info->is_receiving == JAVACALL_FALSE) {
        info->is_receiving = JAVACALL_TRUE;
        LOG1("bt_l2cap_receive_start(), %d bytes", len);

        status = receive_common(handle, pData, len, pBytesReceived, 
            JAVACALL_WOULD_BLOCK);
        
        if (status == JAVACALL_WOULD_BLOCK) {
            requestReceive(handle, len);
        } else {
            info->is_receiving = JAVACALL_FALSE;
        }

        return status;
	} else { /* finish */
        LOG1("bt_l2cap_receive_finish(), %d bytes", len);
        status = receive_common(handle, pData, len, pBytesReceived,
            JAVACALL_OK);
        if (status != JAVACALL_WOULD_BLOCK) {
            info->is_receiving = JAVACALL_FALSE;
        }
        return status;
	}
}

int available(javacall_handle handle) {
    connection_info_t *info = &emul_data.handled_info[(int)handle].conn;

    LOG("available()");

    if (info->in_len == 0) {
        if (!(info->flags & ENDOF_INP_REACHED)) {
            // -1 means receive everything available into input buffer
            requestReceive(handle, -1);
        }
    }

    return info->in_len;
}

/*
 * Determines if there is data to be read from the connection
 * without blocking.
 * 
 * @param handle connection handle
 * @param pReady pointer to variable result is stored in
 *          JAVACALL_TRUE if there are data available
 *          JAVACALL_FALSE otherwise
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_l2cap_get_ready(
        javacall_handle handle,
        /*OUT*/javacall_bool* pReady)
{
    LOG("bt_l2cap_get_ready()");

    *pReady = available(handle) ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

/*
 * Determines if there is a connection to the remote device.
 *
 * @param addr the Bluetooth address of the remote device
 * @param pBool pointer to variable where the result is to be stored:
 *              <code>JAVACALL_TRUE</code> if the device is connected,
 *              <code>JAVACALL_FALSE</code> otherwise
 * @retval <code>JAVACALL_OK</code> on success
 * @retval <code>JAVACALL_FAIL</code> on failure
 */
javacall_result bt_bcc_is_connected(
		const javacall_bt_address addr, 
		int *pBool)
{
    int handle = getConnHandle(addr);
    *pBool = JAVACALL_FALSE;
	if (handle != (int)BT_INVALID_HANDLE) {
        connection_info_t *info = &emul_data.handled_info[handle].conn;
        if (info->connect_status == JAVACALL_OK) {
            *pBool = JAVACALL_TRUE;
		}
	}
	return JAVACALL_OK;
}

/*
 * Checks if a remote device was authenticated.
 *
 * @param addr Bluetooth address of a remote device
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the remote device is authenticated,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_is_authenticated(const javacall_bt_address addr, javacall_bool *pBool)
{
    int handle = getConnHandle(addr);
    *pBool = JAVACALL_FALSE;
	if (handle != (int)BT_INVALID_HANDLE) {
        connection_info_t *info = &emul_data.handled_info[handle].conn;
        if (info->connect_status == JAVACALL_OK) {
            *pBool = info->flags & AUTHENTICATE; 
		}
	}
	return JAVACALL_OK;
}

/*
 * Increases or decreases encryption request counter for a remote device.
 *
 * @param addr the Bluetooth address of the remote device
 * @param enable indicated whether the encryption needs to be enabled
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the encryption needs to been changed,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_set_encryption(const javacall_bt_address addr, javacall_bool enable,
        javacall_bool *pBool)
{
    int handle = getConnHandle(addr);
    *pBool = JAVACALL_FALSE;
	if (handle != (int)BT_INVALID_HANDLE) {
        connection_info_t *info = &emul_data.handled_info[handle].conn;
        if (info->connect_status == JAVACALL_OK) {
            if (enable) {
                *pBool = BT_BOOL((info->flags & ENCRYPT) == 0);
				info->flags |= ENCRYPT;
			} else {
				info->flags &= ~ENCRYPT;
			}
		}
	}
    return JAVACALL_OK;
}

/*
 * Determines if data exchanges with a remote device are being encrypted.
 *
 * @param addr Bluetooth address of a remote device
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if connection to the device is encrypted,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on failure
 */
javacall_result bt_bcc_is_encrypted(const javacall_bt_address addr, javacall_bool *pBool)
{
    int handle = getConnHandle(addr);
    *pBool = JAVACALL_FALSE;
	if (handle != (int)BT_INVALID_HANDLE) {
        connection_info_t *info = &emul_data.handled_info[handle].conn;
        if (info->connect_status == JAVACALL_OK) {
            *pBool = BT_BOOL(info->flags & ENCRYPT);
		}
	}
    return JAVACALL_OK;
}

/*
 * Initiates pairing with a remote device.
 *
 * If this function returns JAVACALL_WOULD_BLOCK, the notification for 
 * operation completion will be sent later through 
 * javanotify_bt_remote_device_event() 
 * with JAVACALL_EVENT_BT_BOND_COMPLETE type.
 *
 * @param addr the Bluetooth address of the device with which to pair
 * @param pin an array containing the PIN code (null-terminated UTF-8 encoded).
 * @param pBool pointer to variable where the result is to be stored:
 *         JAVACALL_TRUE if the device was paired,
 *         JAVACALL_FALSE otherwise
 * @retval JAVACALL_OK if the device was paired.
 * @retval JAVACALL_FAIL on failure
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 */
javacall_result bt_bcc_bond(const javacall_bt_address addr, const char* pin,
        javacall_bool *pBool)
{
    int handle = getConnHandle(addr);
    (void)pin; /* IMPL_NOTE: pin is not use */
	*pBool = JAVACALL_FALSE;
	if (handle != (int)BT_INVALID_HANDLE) {
        connection_info_t *info = &emul_data.handled_info[handle].conn;
		info->flags |= AUTHENTICATE; /* IMPL_NOTE: device is authenticated on connect */
	    *pBool = JAVACALL_TRUE;
	}
    return JAVACALL_OK;
}
