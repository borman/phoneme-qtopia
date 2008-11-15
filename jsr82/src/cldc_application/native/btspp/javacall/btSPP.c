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

#include <javacall_bt.h>
#include <javanotify_bt.h>
#include <btCommon.h>
#include <emul.h>

javacall_result create_server(int protocol, int imtu, int omtu, 
        javacall_bool auth, javacall_bool authz, javacall_bool enc, javacall_bool master, 
        javacall_handle *pHandle, int *pPort);

javacall_result create_client(int protocol, 
        int imtu, int omtu, javacall_bool auth, javacall_bool enc, 
        javacall_bool master, javacall_handle* pHandle);

int available(javacall_handle handle);

/*
 * Retrieves code and string description of the last occured error.
 *
 * @param handle connection handle
 * @param pErrStr pointer to string pointer initialized with
 *                    result string pointer,
                  if <code>NULL</code> error string is not returned
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL if connection is already closed or
 *                       an error occured during close operation
 */
javacall_result bt_rfcomm_get_error(javacall_handle handle,
    /*OUT*/ char** pErrStr)
{
    return bt_l2cap_get_error(handle, pErrStr);
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
javacall_result bt_rfcomm_close(javacall_handle handle)
{
    return bt_l2cap_close(handle);
}

/*
 * Creates a new server connection.
 *
 * The method creates a server connection instance
 * but does not put it in listen mode.
 * Anyway it selects and reserves a free channel to listen for
 * incoming connections on after the listen method is called.
 *
 * @param auth    JAVACALL_TRUE if authication is required
 * @param authz   JAVACALL_TRUE if authorization is required
 * @param enc     JAVACALL_TRUE if required to be encrypted
 * @param master  JAVACALL_TRUE if required to be a connection's master
 * @param pHandle pointer to connection handle variable,
 *               new connection handle returned in result.
 * @param cn pointer to variable, where reserved channel is returned in.
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_rfcomm_create_server(
        javacall_bool auth,
        javacall_bool authz,
        javacall_bool enc,
        javacall_bool master,
        /*OUT*/javacall_handle* pHandle,
        /*OUT*/int* pCn)
{
    return create_server(RFCOMM, 
        -1, -1, auth, authz, enc, master, pHandle, pCn);
}

/*
 * Puts server connection to listening mode.
 *
 * @param handle server connection handle
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL otherwise
 */
javacall_result bt_rfcomm_listen(
        javacall_handle handle)
{
    return bt_l2cap_listen(handle);
}

/*
 * Accepts pending incoming connection if any.
 *
 * If JAVACALL_WOULD_BLOCK is returned,
 * this function should be called again after the notification through 
 * javanotify_bt_event() with JAVACALL_EVENT_BT_ACCEPT_COMPLETE type.
 *
 * @param handle server connection handle
 * @param pPeerHandle pointer to peer handle to store new connection handle
 *             to work with accepted incoming client connection
 * @param pPeerAddr bluetooth address variable to store
 *                  the address of accepted client, 
 *                  if NULL the value is not returned
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on error,
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 */
javacall_result bt_rfcomm_accept(
        javacall_handle handle, 
        /*OUT*/javacall_handle* pPeerHandle,
        /*OUT*/javacall_bt_address* pPeerAddr)
{    
    return bt_l2cap_accept(handle, pPeerHandle, pPeerAddr, 
        NULL, NULL);
}

/*
 * Creates a new client connection.
 *
 * The method does not establishes real bluetooth connection
 * just creates a client connection instance.
 *
 * @param authenticate  JAVACALL_TRUE if authication is required
 * @param encrypt       JAVACALL_TRUE if required to be encrypted
 * @param master        JAVACALL_TRUE if required to be a connection's master
 * @param pHandle pointer to connection handle variable,
 *               new connection handle returned in result.
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on error
 */
javacall_result bt_rfcomm_create_client(
        javacall_bool authenticate,
        javacall_bool encrypt,
        javacall_bool master,
        /*OUT*/javacall_handle* pHandle)
{
    return create_client(RFCOMM, -1, -1, authenticate, encrypt, master, pHandle);
}        

/*
 * Establishes connection with the Bluetooth device.
 *
 * If this function returns JAVACALL_WOULD_BLOCK, 
 * the notification is delivered through javanotify_bt_protocol_event() 
 * with JAVACALL_EVENT_BT_CONNECT_COMPLETE type. 
 *
 * @param handle connection handle
 * @param addr pointer to the address of device to connect to
 * @param cn channel number to connect to
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 * @retval JAVACALL_FAIL on error
 */
javacall_result bt_rfcomm_connect(
        javacall_handle handle,
        const javacall_bt_address addr, 
        int cn)
{
    return bt_l2cap_connect(handle, addr, cn, NULL, NULL);
}


/*
 * Initiates data sending via connection.
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
 * @retval JAVACALL_FAIL on error
 */
javacall_result bt_rfcomm_send(
        javacall_handle handle,
        const char* pData, 
        int len, 
        /*OUT*/ int* pBytesSent)
{
    return bt_l2cap_send(handle, pData, len, pBytesSent);
}    

/*
 * Initiates data receiving via connection.
 *
 * If JAVACALL_WOULD_BLOCK is returned,
 * this function should be called again after the notification through 
 * javanotify_bt_protocol_event() with JAVACALL_EVENT_BT_RECEIVE_COMPLETE type. 
 *
 * @param handle connection handle
 * @param pData pointer to data buffer
 * @param len length of the buffer
 * @param pBytesRead number of bytes that were received,
 *             0 indicates end-of-data
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_WOULD_BLOCK if the caller needs a notification to complete the operation
 * @retval JAVACALL_FAIL on error
 */
javacall_result bt_rfcomm_receive(
        javacall_handle handle,
        char* pData, 
        int len, 
        /*OUT*/ int* pBytesReceived)
{
    return bt_l2cap_receive(handle, pData, len, pBytesReceived);
}

/*
 * Returns the number of bytes available to be read from the connection
 * without blocking.
 * 
 * @param handle connection handle
 * @param pCount pointer to variable the number of available bytes is stored in
 *
 * @retval JAVACALL_OK on success,
 * @retval JAVACALL_FAIL on error
 */
javacall_result bt_rfcomm_get_available(
        javacall_handle handle,
        /*OUT*/int* pCount)
{
    *pCount = available(handle);
    return JAVACALL_OK;
}        
