/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

#ifndef __JSR234_CONTROL_H
#define __JSR234_CONTROL_H

#define KNI_BUFFER_SIZE 512

typedef javacall_result ( *typical_jc_get_int_func_ptr_t )(
    javacall_handle, /*OUT*/ long* );

typedef javacall_result ( *typical_jc_set_int_func_ptr_t )( javacall_handle,
                                                            long );
typedef javacall_result ( *typical_jc_set_get_int_func_ptr_t )(
    javacall_handle, /*IN/OUT*/ long* );

typedef javacall_result ( *typical_jc_get_bool_func_ptr_t )(
    javacall_handle, /*OUT*/ javacall_bool* );
    
typedef javacall_result ( *typical_jc_set_bool_func_ptr_t )(
    javacall_handle, javacall_bool );
    
typedef javacall_result ( *typical_jc_get_string_func_ptr_t )(
    javacall_handle hNative, char*, long );

typedef javacall_result ( *typical_jc_set_string_func_ptr_t )(
    javacall_handle, const char*);

typedef javacall_result (*typical_jc_get_utf16string_func_ptr_t)(
    javacall_handle hNative, javacall_utf16_string, long);

typedef javacall_result ( *typical_jc_set_utf16string_func_ptr_t )(
    javacall_handle, javacall_const_utf16_string);

javacall_amms_control_type_enum_t getControlTypeFromName( 
                                                   const char* type_name );
const char* getControlNameFromEnum( javacall_amms_control_type_enum_t type );

int getControlTypeFromArg( KNIDECLARGS javacall_amms_control_type_enum_t *type );

int controlsToJavaNamesArray( KNIDECLARGS javacall_amms_control_t controls[], int len,
                              jobject javaNamesArray );

javacall_amms_control_t *getNativeControlPtr(KNIDECLARGS int dummy);

javacall_result getUTF8StringFromParameter(KNIDECLARGS int par_num, char *buf);

jint getIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_get_int_func_ptr_t pFunc );

void setIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_int_func_ptr_t pFunc,
                                   const char* exception_name,
                                   const char* exception_text );
                                   
jint setGetIntWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_get_int_func_ptr_t pFunc,
                                   const char* exception_name,
                                   const char* exception_text );
                                   
jboolean getBoolWithTypicalJavacallFunc( KNIDECLARGS 
    typical_jc_get_bool_func_ptr_t pFunc );

javacall_result setBoolWithTypicalJavacallFunc( KNIDECLARGS typical_jc_set_bool_func_ptr_t
                                                pFunc );
                                   
void getStringWithTypicalJavacallFunc( KNIDECLARGS typical_jc_get_string_func_ptr_t pFunc,
    jstring hStr );

void setStringWithTypicalJavacallFunc(KNIDECLARGS typical_jc_set_string_func_ptr_t pFunc,
    const char* exception_name, const char* exception_text);

void getUTF16StringWithTypicalJavacallFunc(KNIDECLARGS
    typical_jc_get_utf16string_func_ptr_t pFunc, jstring hStr);

void setUTF16StringWithTypicalJavacallFunc(KNIDECLARGS
    typical_jc_set_utf16string_func_ptr_t pFunc,
    const char* exception_name, const char* exception_text);
    
#endif /* __JSR234_CONTROL_H */
