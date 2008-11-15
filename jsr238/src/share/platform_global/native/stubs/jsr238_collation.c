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
#include <jsr238_collation.h>
#include <midp_logging.h>


/**
 * Gets the number of supported locales for string collation.
 *
 * @param pcount out count of collation locales
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_collation_locales_count(/* OUT */jint* pcount){
	(void)pcount;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_collation_locales_count()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Gets a locale name for string collation for the given locale index.
 *
 * @param index  index of the locale. If neutral locale supported its index MUST be 0
 * @param locale_name_out   buffer for the locale.
 * @param plen	in - length of output buffer,
 *				out - length of returned string in jchars including terminating zero 
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_collation_locale_name(jint index, /* OUT */ jchar* locale_name_out, /* IN|OUT */ jint* plen){
	(void)index;
	(void)locale_name_out;
	(void)plen;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_collation_locale_name()\n");
    return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Gets locale index used for collation by the given locale name
 *
 * @param locale_name string containing requested locale name, null for neutral locale
 * @param pindex out index of requested locale
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_get_collation_locale_index(const jchar* locale_name, /* OUT */ jint* pindex){
	(void)locale_name;
	(void)pindex;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_get_collation_locale_index()\n");
	return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}


/**
 * Compare two strings.
 *
 * @param locale_index  index of the locale.
 * @param s1            first string to compare.
 * @param len1          length of the the first string.
 * @param s2            second string to compare.
 * @param len2          length of the second string.
 * @param level         level of collation.
 * @param pres          (out) return negative if s1 belongs before s2, 0 if the strings are equal, 
 *						positive if s1 belongs after s2.
 * @return JSR238_STATUSCODE_OK if all done successfuly, 
 *         JSR238 error code otherwise 
 */
JSR238_STATUSCODE jsr238_compare_strings(jint locale_index,
								   const jchar* s1, jsize len1, 
								   const jchar* s2, jsize len2,
								   jint level,
								   /* OUT */ jint * pres){
	(void)locale_index;
	(void)s1;
	(void)len1;
	(void)s2;
	(void)len2;
	(void)level;
	(void)pres;
    REPORT_CALL_TRACE(LC_LOWUI, "LF:STUB:jsr238_compare_strings()\n");
	return JSR238_STATUSCODE_NOT_IMPLEMENTED;
}

