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

/**
 * @file
 * Implementation of low-level porting API for JSR 238 (MI18N)
 */

#include <java_types.h>
#include <jsr238_resources.h>
#include <midp_logging.h>

/**
 * Gets the number of supported locales for data formatting
 * @param pcount out count of collation locales
 *
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_resource_locales_count(/* OUT */jint* pcount){
	(void)pcount;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource_locales_count()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}

/**
 * Gets locale name for data formatting with the given index.
 *
 * @param locale_index of the locale. If neutral locale supported its index MUST be 0
 * @param locale_name_out buffer for the locale.
 * @param plen	in - length of output buffer,
 *				out - length of returned string in jchars including terminating zero 
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_resource_locale_name(jint index, /* OUT */ jchar* locale_name_out, /* IN|OUT */ jint* plen){
	(void)locale_index;
	(void)locale_name_out;
	(void)plen;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource_locale()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}

/**
 * Gets locale index used for date/number formatting by the given locale name
 *
 * @param locale_name string containing requested locale name, null for neutral locale
 * @param pindex out index of requested locale
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_resource_locale_index(const jchar* locale_name, /* OUT */ jint* pindex){
	(void)locale_name;
	(void)pindex;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource_locale_index()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Gets a resource length for pointed reource identifier and locale.
 *
 * @param locale_index  index of the locale.
 * @param resource_id   resource identifier.
 * @param resource      buffer for the resource.
 * @param offset	offset of first byte of resource data 
 * @param length	pointer to integer that set to desired number of bytes to copy from resource data
 *					receiving length of copied resource data in bytes, 
 *					if length is less than remaining resource size only length bytes are copied		

 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 Error code otherwise
 */
JSR238_STATUSCODE jsr238_get_resource(jint locale_index, jint resource_id, 
					/* OUT */jbyte* resource, jint offset, /* IN|OUT */ jsize* plength){
	(void)locale_index;
	(void)resource_id;
	(void)resource;
	(void)offset;
	(void)plength;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Gets a resource type for pointed resource identifier and locale.
 *
 * @param locale_index  index of the locale
 * @param resource_id   resource identifier
 * @param resType  returned resource type
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238_STATUSCODE_FAIL otherwise
 */
JSR238_STATUSCODE jsr238_get_resource_type(jint locale_index, jint resource_id,
					   jint* resType /* OUT */){
	(void)locale_index;
	(void)resource_id;
	(void)resType;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource_type()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Checks if resource with given identifier exists.
 *
 * @param locale_index  index of the locale.
 * @param resource_id   resource identifier.
 * @param pres not equal 0 if resource ID is valid and zero if not
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238_STATUSCODE_FAIL otherwise
 */
JSR238_STATUSCODE jsr238_is_valid_resource_id(jint locale_index,
						jint resource_id,
						jboolean* pres){
	(void)locale_index;
	(void)resource_id;
	(void)pres;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_is_valid_resource_id()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Gets a resource length for pointed reource identifier and locale.
 *
 * @param locale_index  index of the locale.
 * @param resource_id   resource identifier.
 * @param length  returned size of the resource (in bytes).
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238_STATUSCODE_FAIL otherwise
 */
JSR238_STATUSCODE jsr238_get_resource_length(jint locale_index, jint resource_id, 
						 /* OUT */ jsize* plength){
	(void)locale_index;
	(void)resource_id;
	(void)plength;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_resource_length()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}
