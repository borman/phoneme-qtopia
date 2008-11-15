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

#ifndef __JSR238_H_
#define __JSR238_H_

#define MAX_LOCALE_LENGTH   100

/**
 * @enum JSR238_STATUSCODE
 * jsr238 return codes
 */
typedef enum JSR238_STATUSCODE_ENUM {
    JSR238_STATUSCODE_OK = 0,                   /* Generic success             */
    JSR238_STATUSCODE_FAIL = -1,                /* Generic failure             */
    JSR238_STATUSCODE_NOT_IMPLEMENTED = -2,     /* Not implemented             */
    JSR238_STATUSCODE_OUT_OF_MEMORY = -3,       /* Out of memory               */
    JSR238_STATUSCODE_INVALID_ARGUMENT = -4,    /* Invalid argument            */
    JSR238_STATUSCODE_WOULD_BLOCK = -5,         /* Would block                 */
    JSR238_STATUSCODE_CONNECTION_NOT_FOUND = -6,/* Connection not found        */
    JSR238_STATUSCODE_INTERRUPTED = -7          /* Operation is interrupted    */
} JSR238_STATUSCODE;

#endif /* __JSR238_H_ */
