/*
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

//#include <commonKNIMacros.h>
#include <jsrop_kni.h>
#include <jsrop_memory.h>
#include <jsrop_exceptions.h>
#include <jsr238.h>
#include <jsr238_resources.h>

/**
 * @file
 * Implementation of Java native methods for JSR 238 classes.
 */


/**
 * Get number of supported locales for device resources.
 * <p>
 * Java declaration:
 * <pre>
 *     getDevLocalesCount()I
 * </pre>
 *
 * @return number of locales
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_DevResourceManagerFactory_getDevLocalesCount) {
	int count=0;
	if (jsr238_get_resource_locales_count(&count)<0){
		KNI_ThrowNew(jsropRuntimeException,"Get locale count");
	}
    KNI_ReturnInt(count);
}

/**
 * Get one of supported device locales (by number).
 * <p>
 * Java declaration:
 * <pre>
 *     getDevLocaleName(I)Ljava/lang/String
 * </pre>
 *
 * @param index  index of locale to select
 * @return locale name
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_global_DevResourceManagerFactory_getDevLocaleName) {
	jint len=MAX_LOCALE_LENGTH, res;
    jint index = KNI_GetParameterAsInt(1);
    jchar locale_name[MAX_LOCALE_LENGTH];

    KNI_StartHandles(1);
    KNI_DeclareHandle(hloc);
	res = jsr238_get_resource_locale_name(index,locale_name,&len);
    if (res < 0 ) {
        KNI_ReleaseHandle(hloc);
		KNI_ThrowNew(jsropRuntimeException,"Get locale name");
    } else {
        KNI_NewString(locale_name, len - 1, hloc);
    }
    KNI_EndHandlesAndReturnObject(hloc);
}

/**
 * Get index of supported locales for device resources by its name.
 * <p>
 * Java declaration:
 * <pre>
 *     getDevLocaleIndex(Ljava/lang/String)I
 * </pre>
 *
 * @param locale name
 * @return internal index of locale or -1 if locale is not supported 
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_DevResourceManagerFactory_getDevLocaleIndex) {
	jint result =-1, index = 0;
	jsize len = 0;
	int error = 0;
	jchar* locale_name;

	KNI_StartHandles(1);
	KNI_DeclareHandle(hstr1);
	KNI_GetParameterAsObject(1, hstr1);

	if (KNI_IsNullHandle(hstr1)) {
		locale_name = NULL;
	} else  {
		len = KNI_GetStringLength(hstr1);
		locale_name = (jchar *)JAVAME_MALLOC((len + 1) * sizeof(jchar));
		if (NULL == locale_name) {
		   KNI_ThrowNew(jsropOutOfMemoryError, 
			   "Out of memory");
		   error = 1;
		} else {
			KNI_GetStringRegion(hstr1, 0, len, locale_name);
			locale_name[len]=0;
		}
	}

	if (!error){
		result = jsr238_get_resource_locale_index(locale_name, &index);
		if (result < 0) index =-1;
		JAVAME_FREE(locale_name);
	}

	KNI_EndHandles();
	KNI_ReturnInt(index);
}


/**
 * Retrieves resource data for the given ID and locale.
 * <p>
 * Java declaration:
 * <pre>
 *     getRawResourceData0(I,I,[B)Z
 * </pre>
 * 
 * @param hdata         byte array to store resource data
 * @param resource_id   resource identifier
 * @param locale_index  index of locale in array of supported locales
 * @param offset		offset of resource to start with
 * @param length		length in bytes to copy
 * @return length in bytes of copied data
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_DevResourceBundleReader_getRawResourceData0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jint resource_id = KNI_GetParameterAsInt(2);
	jint offset = KNI_GetParameterAsInt(4);
	jint length = KNI_GetParameterAsInt(5);
	jint array_len;

    jbyte* buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hdata);

    KNI_GetParameterAsObject(3, hdata);

    array_len = KNI_GetArrayLength(hdata);
	if (array_len < length){
        length = 0;
        KNI_ThrowNew(jsropIllegalArgumentException, 
            "Error! Array size is too few!");
	} else {
		buffer = JAVAME_MALLOC(array_len);
		if (NULL == buffer) {
			length = 0;
			KNI_ThrowNew(jsropOutOfMemoryError, 
				"Cannot allocate buffer for device resource");
		} else {
			if (JSR238_STATUSCODE_OK != jsr238_get_resource(locale_index,resource_id,
										buffer, offset, &length)) {
				length = 0;
			} else {
				KNI_SetRawArrayRegion(hdata, 0, length, buffer);
			}
			JAVAME_FREE(buffer);
		}
	}

    KNI_EndHandles();
    KNI_ReturnInt(length);
}

/**
 * Retrieves resource type for the given ID and locale.
 * <p>
 * Java declaration:
 * <pre>
 *     getResourceType0(II)I
 * </pre>
 * 
 * @param resource_id   resource identifier
 * @param locale_index  index of locale in array of supported locales
 * @return resource type (<code>0..FF</code>), <code>-1</code> if
 *      something is wrong
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_DevResourceBundleReader_getResourceType0) {
	jint locale_index = KNI_GetParameterAsInt(1);
    jint resource_id = KNI_GetParameterAsInt(2);
    jint resType;
    if (JSR238_STATUSCODE_OK != jsr238_get_resource_type(locale_index, resource_id,
														&resType)) {
        KNI_ReturnInt(KNI_ERR);
    } else {
        KNI_ReturnInt(resType);
    }
}

/**
 * Determine if a resource with the given ID and locale exists.
 * <p>
 * Java declaration:
 * <pre>
 *     isValidResourceID0(II)Z
 * </pre>
 * 
 * @param resource_id   resource identifier
 * @param locale_index  index of locale in array of supported locales
 * @return <code>true</code> if resource with the given ID exists for
 *      the given locale, <code>false</code> otherwise
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_global_DevResourceBundleReader_isValidResourceID0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jint resource_id = KNI_GetParameterAsInt(2);
	jboolean res = 0;
	if (JSR238_STATUSCODE_OK != jsr238_is_valid_resource_id(locale_index, resource_id, &res)){
		KNI_ThrowNew(jsropRuntimeException,"error getting isValidResourceID");
	}
    KNI_ReturnBoolean(res);
}

/**
 * Retrieves resource length for the given ID and locale.
 * <p>
 * Java declaration:
 * <pre>
 *     getResourceLength0(II)I
 * </pre>
 * 
 * @param resource_id   resource identifier
 * @param locale_index  index of locale in array of supported locales
 * @return length of the resource with the given ID and locale or 
 *         KNI_ERR if something is wrong
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_DevResourceBundleReader_getResourceLength0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jint resource_id = KNI_GetParameterAsInt(2);
    jsize resLength;
    if (JSR238_STATUSCODE_OK != jsr238_get_resource_length(locale_index, resource_id,
														   &resLength)) {
        KNI_ReturnInt(KNI_ERR);
    } else {
        KNI_ReturnInt(resLength);
    }

}
