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

#ifndef _SIPPUSHREGISTRY_H_
#define _SIPUSHREGISTRY_H_

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @file
 * @defgroup sip JSR180 Session Initiation Protocol (SIP)
 * @ingroup stack
 */

/**
 * @defgroup sippushregistry SIP Pushregistry
 * @ingroup sip
 * @brief Session Initiation Protocol pushregistry porting interface. \n
 * ##include <SipPushRegistry.h>
 * @{
 *
 * This file defines the Session Initiation Protocol pushregistry
 * porting interfaces. The functions defined here are called
 * from the MIDP native pushregistry implementation to handle
 * specific operations for SIP inbound connection requests.
 */

/**
 * Extracts the "From" header URI for filter comparison
 * check.
 *
 * @param buf cached SIP message
 * @param len length of the cached data
 * @return a malloc'ed copy of the From header URI field
 *         or NULL if the extraction failed.
 *         Caller must free the string when done.
 */
unsigned char *getSipFromHeaderURI(unsigned char *buf, int len);


/**
 * Extracts the "Accept-Contact" type field for
 * media feature comparison check.
 *
 * @param buf cached SIP message
 * @param len length of the cached data
 * @return a malloc'ed copy of the Accept-Contact
 *         media type field
 *         or NULL if the extraction failed. Caller
 *         must free the string when done.
 */
unsigned char *getSipAcceptContactType(unsigned char *buf, int len);

/**
 * Checks SIP push filter pattern against incoming
 * message sender identification.
 *
 * @param filter push registration entry pattern
 * @param sender extracted URI contained in "From" header
 * @return <code>true</code> if pattern matches
 */
int checksipfilter(unsigned char *pattern, unsigned char *sender);

/** @} */

#ifdef __cplusplus
}
#endif

#endif /* ifndef _SIPPUSHREGISTRY_H_ */
