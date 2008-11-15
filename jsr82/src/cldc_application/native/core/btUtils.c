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

#include "btUtils.h"
#include <malloc.h>
#include <string.h>
#include <stdio.h>




/* 'C' string for java.lang.OutOfMemoryError */
const char* const jumpOutOfMemoryError = "java/lang/OutOfMemoryError";
/* 'C' string for java.lang.RuntimeException */
const char* const jumpRuntimeException = "java/lang/RuntimeException";
/* 'C' string for java.lang.NullPointerException */
const char* const jumpNullPointerException = "java/lang/NullPointerException";
/* * 'C' string for java.io.IOException */
const char* const jumpIOException = "java/io/IOException";
/* * 'C' string for java.io.InterruptedIOException */
const char* const jumpInterruptedIOException = "java/io/InterruptedIOException";

/*
 * Converts hex char to integer value.
 */
int hex2int(char c)
{
    if (c >= '0' && c <= '9') {
        return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
        return c - 'a' + 10;
    }
    return c - 'A' + 10;
}

/*
 * Converts integer to hex char.
 */
char int2hex(int i)
{
    if (i >= 0 && i <= 9) {
        return '0' + i;
    }
    if (i >= 10 && i <= 15) {
        return 'A' + i - 10;;
    }
    return '\0';
}

/*
 * Extracts Bluetooth address from Java string.
 *
 * @param addressHandle handle to Java string containing Bluetooth address
 * @param addr receives Bluetooth address value
 */
void getBtAddr(jstring addressHandle, javacall_bt_address addr)
{
    int i;
    char *address = jStringToChars(addressHandle);
    char *ptr = address + 11;
    for (i = 0; i < JAVACALL_BT_ADDRESS_SIZE; i++) {
        *addr = hex2int(*ptr--);
        *addr++ |= hex2int(*ptr--) << 4;
    }
    free(address);
}

char* jStringToChars(jstring jStringHandle)
{
    jchar* data = NULL;
	jchar* pSrc;
    jint len = KNI_GetStringLength(jStringHandle);
	char* out;
	char* pDest;
    int i;

    if (len < 0) {
        return NULL;
    } else if (len > 0) {
    	pSrc = data = (jchar*)malloc(len * sizeof (jchar));
		if (data == NULL) {
	        return NULL;
		} else {
			KNI_GetStringRegion(jStringHandle, 0, len, data);
		}
    }

    pDest = out = (char*)malloc(len + 1);
    if (out == NULL) {
		free(data);
        return NULL;
    }

    for (i = len; i; i--) {
        *pDest++ = (char)*pSrc++;
    }

    *pDest = 0;
	free(data);
    return out;
}

char* JumpErrMsgAlloc(char* format, char* string)
{
	int len = strlen(format) + strlen(string);
	char* sNew = (char*) malloc(len);
	sprintf(sNew, format, string);
	return sNew;
}

void JumpErrMsgFree(char* string)
{
	free(string);
}
