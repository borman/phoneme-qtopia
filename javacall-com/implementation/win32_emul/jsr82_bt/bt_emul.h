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

javacall_result bt_bcc_initialize(void);
javacall_result bt_bcc_finalize(void);
javacall_result bt_bcc_is_connectable(javacall_bool *pBool);
javacall_result bt_bcc_is_connected(const javacall_bt_address addr, javacall_bool *pBool);
javacall_result bt_bcc_is_paired(const javacall_bt_address addr, javacall_bool *pBool);
javacall_result bt_bcc_is_authenticated(const javacall_bt_address addr, javacall_bool *pBool);
javacall_result bt_bcc_is_trusted(const javacall_bt_address addr, javacall_bool *pBool);
javacall_result bt_bcc_is_encrypted(const javacall_bt_address addr, javacall_bool *pBool);
javacall_result bt_bcc_bond(const javacall_bt_address addr, const char *pin, javacall_bool *pBool);
javacall_result bt_bcc_get_preknown_devices(javacall_bt_address devices[JAVACALL_BT_MAX_PREKNOWN_DEVICES], int *pCount);
javacall_result bt_bcc_set_encryption(const javacall_bt_address addr, javacall_bool enable,javacall_bool *pBool);

javacall_result bt_stack_initialize(void);
javacall_result bt_stack_finalize(void);
javacall_result bt_stack_is_enabled(javacall_bool *pBool);
javacall_result bt_stack_enable(void);
javacall_result bt_stack_get_local_address(javacall_bt_address *pAddr);
javacall_result bt_stack_get_local_name(char *pName);
javacall_result bt_stack_set_service_classes(int classes);
javacall_result bt_stack_get_device_class(int *pValue);
javacall_result bt_stack_get_access_code(int *pValue);
javacall_result bt_stack_set_access_code(int accessCode);
javacall_result bt_stack_start_inquiry(int accessCode);
javacall_result bt_stack_cancel_inquiry(void);
javacall_result bt_stack_ask_friendly_name(const javacall_bt_address addr);
javacall_result bt_stack_authenticate(const javacall_bt_address addr);
javacall_result bt_stack_encrypt(const javacall_bt_address addr,javacall_bool enable);

javacall_result bt_sddb_initialize(void);
javacall_result bt_sddb_finalize(void);
javacall_result bt_sddb_update_record(unsigned long *pId,unsigned long classes,void *data,unsigned long size);
javacall_result bt_sddb_update_psm(unsigned long id,unsigned short psm);
javacall_result bt_sddb_update_channel(unsigned long id,unsigned char cn);
javacall_result bt_sddb_read_record(unsigned long id, unsigned long *pClasses, void *data, unsigned long *pSize);
javacall_result bt_sddb_remove_record(unsigned long id);
unsigned long bt_sddb_get_records(unsigned long *array,unsigned long count);
unsigned long bt_sddb_get_service_classes(unsigned long id);

javacall_result bt_l2cap_close(javacall_handle handle);
/*OPTIONAL*/ javacall_result bt_l2cap_get_error(javacall_handle handle, char **pErrStr);
javacall_result bt_l2cap_create_server(int receiveMTU,int transmitMTU,javacall_bool authenticate,javacall_bool authorize,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle, int *pPsm);
javacall_result bt_l2cap_listen(javacall_handle handle);
javacall_result bt_l2cap_accept(javacall_handle handle, javacall_handle *pPeerHandle, javacall_bt_address *pPeerAddr, int *pReceiveMTU, int *pTransmitMTU);
javacall_result bt_l2cap_create_client(int receiveMTU,int transmitMTU,javacall_bool authenticate,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle);
javacall_result bt_l2cap_connect(javacall_handle handle,const javacall_bt_address addr,int psm, int *pReceiveMTU, int *pTransmitMTU);
javacall_result bt_l2cap_send(javacall_handle handle,const char *pData,int len, int *pBytesSent);
javacall_result bt_l2cap_receive(javacall_handle handle, char *pData,int len, int *pBytesReceived);

javacall_result bt_l2cap_get_ready(javacall_handle handle, javacall_bool *pReady);
javacall_result bt_rfcomm_close(javacall_handle handle);
javacall_result bt_rfcomm_get_error(javacall_handle handle, char **pErrStr);
javacall_result bt_rfcomm_create_server(javacall_bool authenticate,javacall_bool authorize,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle, int *pCn);
javacall_result bt_rfcomm_listen(javacall_handle handle);
javacall_result bt_rfcomm_accept(javacall_handle handle, javacall_handle *pPeerHandle, javacall_bt_address *pPeerAddr);
javacall_result bt_rfcomm_create_client(javacall_bool authenticate,javacall_bool encrypt,javacall_bool master, javacall_handle *pHandle);
javacall_result bt_rfcomm_connect(javacall_handle handle,const javacall_bt_address addr, int cn);
javacall_result bt_rfcomm_send(javacall_handle handle,const char *pData, int len, int *pBytesSent);
javacall_result bt_rfcomm_receive(javacall_handle handle,char *pData, int len, int *pBytesReceived);
javacall_result bt_rfcomm_get_available(javacall_handle handle, int *pCount);
javacall_result bt_sdp_request(const javacall_bt_address addr,const javacall_bt_uuid *uuids,javacall_int32 uuids_count,const javacall_uint16 *attrs,javacall_int32 attrs_count, javacall_handle *handle);
javacall_result bt_sdp_cancel(javacall_handle handle);
javacall_result bt_sdp_get_service(const javacall_handle handle,  javacall_uint8 *data, javacall_uint16 *pSize);
 /*OPTIONAL*/ javacall_result bt_bcc_confirm_enable(javacall_bool *pBool);
 /*OPTIONAL*/ javacall_result bt_bcc_put_passkey(const javacall_bt_address addr, char *pin,javacall_bool ask);
 /*OPTIONAL*/ javacall_result bt_bcc_authorize(const javacall_bt_address addr, javacall_handle record,javacall_bool *pBool);
 /*OPTIONAL*/ javacall_result bt_stack_get_acl_handle(const javacall_bt_address addr, int *pHandle);
