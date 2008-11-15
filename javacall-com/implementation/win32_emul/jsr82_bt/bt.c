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

/**
 * @file bt.c
 * @ingroup JSR82Bluetooth
 * @brief This file is a placeholder only. Real implementation of javacall API 
 * for the win32_emul platform (emulation over TCP/IP) see in the jsr82 component.
 */
#include <javacall_bt.h>
#include <bt_emul.h>

javacall_result javacall_bt_bcc_initialize(void) {
	return bt_bcc_initialize();
}

javacall_result javacall_bt_bcc_finalize(void) {
	return bt_bcc_finalize();
}

javacall_result javacall_bt_bcc_is_connectable(javacall_bool *pBool) {
	return bt_bcc_is_connectable(pBool);
}

javacall_result javacall_bt_bcc_is_connected(const javacall_bt_address addr, javacall_bool *pBool) {
	return bt_bcc_is_connected(addr, pBool);
}

javacall_result javacall_bt_bcc_is_paired(const javacall_bt_address addr, javacall_bool *pBool) {
	return bt_bcc_is_paired(addr, pBool);
}

javacall_result javacall_bt_bcc_is_authenticated(const javacall_bt_address addr, javacall_bool *pBool) {
	return bt_bcc_is_authenticated(addr, pBool);
}

javacall_result javacall_bt_bcc_is_trusted(const javacall_bt_address addr, javacall_bool *pBool) {
	return bt_bcc_is_trusted(addr, pBool);
}

javacall_result javacall_bt_bcc_is_encrypted(const javacall_bt_address addr, javacall_bool *pBool) {
	return bt_bcc_is_encrypted(addr, pBool);
}

javacall_result javacall_bt_bcc_bond(const javacall_bt_address addr, const char *pin, javacall_bool *pBool) {
	return bt_bcc_bond(addr, pin, pBool);
}

javacall_result javacall_bt_bcc_get_preknown_devices(javacall_bt_address devices[JAVACALL_BT_MAX_PREKNOWN_DEVICES], int *pCount) {
	return bt_bcc_get_preknown_devices(devices, pCount);
}

javacall_result javacall_bt_bcc_set_encryption(const javacall_bt_address addr, javacall_bool enable,javacall_bool *pBool) {
	return bt_bcc_set_encryption(addr, enable, pBool);
}

javacall_result javacall_bt_stack_initialize(void) {
	return bt_stack_initialize();
}

javacall_result javacall_bt_stack_finalize(void) {
	return bt_stack_finalize();
}

javacall_result javacall_bt_stack_is_enabled(javacall_bool *pBool) {
	return bt_stack_is_enabled(pBool);
}

javacall_result javacall_bt_stack_enable(void) {
	return bt_stack_enable();
}

javacall_result javacall_bt_stack_get_local_address(javacall_bt_address *pAddr) {
	return bt_stack_get_local_address(pAddr);
}

javacall_result javacall_bt_stack_get_local_name(char *pName) {
	return bt_stack_get_local_name(pName);
}

javacall_result javacall_bt_stack_set_service_classes(int classes) {
	return bt_stack_set_service_classes(classes);
}

javacall_result javacall_bt_stack_get_device_class(int *pValue) {
	return bt_stack_get_device_class(pValue);
}

javacall_result javacall_bt_stack_get_access_code(int *pValue) {
	return bt_stack_get_access_code(pValue);
}

javacall_result javacall_bt_stack_set_access_code(int accessCode) {
	return bt_stack_set_access_code(accessCode);
}

javacall_result javacall_bt_stack_start_inquiry(int accessCode) {
	return bt_stack_start_inquiry(accessCode);
}

javacall_result javacall_bt_stack_cancel_inquiry(void) {
	return bt_stack_cancel_inquiry();
}

javacall_result javacall_bt_stack_ask_friendly_name(const javacall_bt_address addr) {
	return bt_stack_ask_friendly_name(addr);
}

javacall_result javacall_bt_stack_authenticate(const javacall_bt_address addr) {
	return bt_stack_authenticate(addr);
}

javacall_result javacall_bt_stack_encrypt(const javacall_bt_address addr,javacall_bool enable) {
	return bt_stack_encrypt(addr, enable);
}

javacall_result javacall_bt_sddb_initialize(void) {
	return bt_sddb_initialize();
}

javacall_result javacall_bt_sddb_finalize(void) {
	return bt_sddb_finalize();
}

javacall_result javacall_bt_sddb_update_record(unsigned long *pId,unsigned long classes,void *data,unsigned long size) {
	return bt_sddb_update_record(pId, classes, data, size);
}

javacall_result javacall_bt_sddb_update_psm(unsigned long id,unsigned short psm) {
	return bt_sddb_update_psm(id, psm);
}

javacall_result javacall_bt_sddb_update_channel(unsigned long id,unsigned char cn) {
	return bt_sddb_update_channel(id, cn);
}

javacall_result javacall_bt_sddb_read_record(unsigned long id, unsigned long *pClasses, void *data, unsigned long *pSize) {
	return bt_sddb_read_record(id, pClasses, data, pSize);
}

javacall_result javacall_bt_sddb_remove_record(unsigned long id) {
	return bt_sddb_remove_record(id);
}

unsigned long javacall_bt_sddb_get_records(unsigned long *array,unsigned long count) {
	return bt_sddb_get_records(array, count);
}

unsigned long javacall_bt_sddb_get_service_classes(unsigned long id) {
	return bt_sddb_get_service_classes(id);
}

javacall_result javacall_bt_l2cap_close(javacall_handle handle) {
	return bt_l2cap_close(handle);
}

/*OPTIONAL*/ javacall_result javacall_bt_l2cap_get_error(javacall_handle handle, char **pErrStr) {
	return bt_l2cap_get_error(handle, pErrStr);
}

javacall_result javacall_bt_l2cap_create_server(int receiveMTU,int transmitMTU,javacall_bool authenticate,javacall_bool authorize,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle, int *pPsm) {
	return bt_l2cap_create_server(receiveMTU, transmitMTU, authenticate, authorize, encrypt, master, pHandle, pPsm);
}

javacall_result javacall_bt_l2cap_listen(javacall_handle handle) {
	return bt_l2cap_listen(handle);
}

javacall_result javacall_bt_l2cap_accept(javacall_handle handle, javacall_handle *pPeerHandle, javacall_bt_address *pPeerAddr, int *pReceiveMTU, int *pTransmitMTU) {
	return bt_l2cap_accept(handle, pPeerHandle, pPeerAddr, pReceiveMTU, pTransmitMTU);
}

javacall_result javacall_bt_l2cap_create_client(int receiveMTU,int transmitMTU,javacall_bool authenticate,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle) {
	return bt_l2cap_create_client(receiveMTU, transmitMTU, authenticate, encrypt, master, pHandle);
}

javacall_result javacall_bt_l2cap_connect(javacall_handle handle,const javacall_bt_address addr,int psm, int *pReceiveMTU, int *pTransmitMTU) {
	return bt_l2cap_connect(handle, addr, psm, pReceiveMTU, pTransmitMTU);
}

javacall_result javacall_bt_l2cap_send(javacall_handle handle,const char *pData,int len, int *pBytesSent) {
	return bt_l2cap_send(handle, pData, len, pBytesSent);
}

javacall_result javacall_bt_l2cap_receive(javacall_handle handle, char *pData,int len, int *pBytesReceived) {
	return bt_l2cap_receive(handle, pData, len, pBytesReceived);
}

javacall_result javacall_bt_l2cap_get_ready(javacall_handle handle, javacall_bool *pReady) {
	return bt_l2cap_get_ready(handle, pReady);
}

javacall_result javacall_bt_rfcomm_close(javacall_handle handle) {
	return bt_rfcomm_close(handle);
}

javacall_result javacall_bt_rfcomm_get_error(javacall_handle handle, char **pErrStr) {
	return bt_rfcomm_get_error(handle, pErrStr);
}

javacall_result javacall_bt_rfcomm_create_server(javacall_bool authenticate,javacall_bool authorize,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle, int *pCn) {
	return bt_rfcomm_create_server(authenticate, authorize, encrypt, master, pHandle, pCn);
}

javacall_result javacall_bt_rfcomm_listen(javacall_handle handle) {
	return bt_rfcomm_listen(handle);
}

javacall_result javacall_bt_rfcomm_accept(javacall_handle handle, javacall_handle *pPeerHandle, javacall_bt_address *pPeerAddr) {
	return bt_rfcomm_accept(handle, pPeerHandle, pPeerAddr);
}

javacall_result javacall_bt_rfcomm_create_client(javacall_bool authenticate,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle) {
	return bt_rfcomm_create_client(authenticate, encrypt, master, pHandle);
}

javacall_result javacall_bt_rfcomm_connect(javacall_handle handle,const javacall_bt_address addr, int cn) {
	return bt_rfcomm_connect(handle, addr, cn);
}

javacall_result javacall_bt_rfcomm_send(javacall_handle handle,const char *pData, int len, int *pBytesSent) {
	return bt_rfcomm_send(handle, pData, len, pBytesSent);
}

javacall_result javacall_bt_rfcomm_receive(javacall_handle handle,char *pData, int len, int *pBytesReceived) {
	return bt_rfcomm_receive(handle, pData, len, pBytesReceived);
}

javacall_result javacall_bt_rfcomm_get_available(javacall_handle handle, int *pCount) {
	return bt_rfcomm_get_available(handle, pCount);
}

javacall_result javacall_bt_sdp_request(const javacall_bt_address addr,const javacall_bt_uuid *uuids,javacall_int32 uuids_count,const javacall_uint16 *attrs,javacall_int32 attrs_count, javacall_handle *handle) {
    return bt_sdp_request(addr, uuids, uuids_count, attrs, attrs_count, handle);
}

javacall_result javacall_bt_sdp_cancel(javacall_handle handle) {
	return bt_sdp_cancel(handle);
}

javacall_result javacall_bt_sdp_get_service(const javacall_handle handle,  javacall_uint8 *data, javacall_uint16 *pSize) {
	return bt_sdp_get_service(handle, data, pSize);
}

 /*OPTIONAL*/ javacall_result javacall_bt_bcc_confirm_enable(javacall_bool *pBool) {
	return bt_bcc_confirm_enable(pBool);
}

 /*OPTIONAL*/ javacall_result javacall_bt_bcc_put_passkey(const javacall_bt_address addr, char *pin,javacall_bool ask) {
	return bt_bcc_put_passkey(addr, pin, ask);
}

 /*OPTIONAL*/ javacall_result javacall_bt_bcc_authorize(const javacall_bt_address addr, javacall_handle record,javacall_bool *pBool) {
	return bt_bcc_authorize(addr,  record, pBool);
}

 /*OPTIONAL*/ javacall_result javacall_bt_stack_get_acl_handle(const javacall_bt_address addr, int *pHandle) {
	return bt_stack_get_acl_handle(addr, pHandle);
}
