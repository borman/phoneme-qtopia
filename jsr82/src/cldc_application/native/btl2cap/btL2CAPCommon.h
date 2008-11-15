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

#ifndef _BT_L2CAP_COMMON_H_
#define _BT_L2CAP_COMMON_H_

/* IMPL_NOTE: revisit */
/* #include <btCommon.h> */

#include <javacall_bt.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Retrieves the field ID to access the field temporary storing
 * native client peer handle.
 *
 * @return native peer handle
 */
jfieldID GetL2CAPPeerHandleID();

/*
 * Registers connection in BCC.
 *
 * It's neccassary to call the function
 * after new L2CAP connection is estabished.
 *
 * @param addr bluetooth address of remote device 
 * @param handle L2CAP connection handle
 * @return <code>JAVACALL_TRUE</code> if success,
 *         <code>JAVACALL_FALSE</code> otherwise
 */
javacall_bool RegisterL2CAPConnection0(javacall_bt_address addr, javacall_handle handle);

#ifdef __cplusplus
}
#endif

#endif /* _BT_L2CAP_COMMON_H_ */
