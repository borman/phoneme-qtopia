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

// #include <commonKNIMacros.h>
#include <jsrop_kni.h>
#include <jsrop_memory.h>
#include <jsrop_exceptions.h>
#include <jsr238.h>
#include <jsr238_collation.h>

/**
 * @file
 * Implementation of Java native methods for JSR 238 classes.
 */

/**
 * Get number of supported locales for string collation.
 * <p>
 * Java declaration:
 * <pre>
 *     getCollationLocalesCount()I
 * </pre>
 *
 * @return number of locales
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_CollationAbstractionLayerImpl_getCollationLocalesCount) {
	int count=0;
	if (jsr238_get_collation_locales_count(&count)<0){
		KNI_ThrowNew(jsropRuntimeException,"Get locale count");
	}
    KNI_ReturnInt(count);
}

/**
 * Get one of supported locales (by number).
 * <p>
 * Java declaration:
 * <pre>
 *     getCollationLocale(I)Ljava/lang/String
 * </pre>
 *
 * @param index  index of locale to select
 * @return locale
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_global_CollationAbstractionLayerImpl_getCollationLocaleName) {
	jint len=MAX_LOCALE_LENGTH, res;
    jint index = KNI_GetParameterAsInt(1);
    jchar locale_name[MAX_LOCALE_LENGTH];

    KNI_StartHandles(1);
    KNI_DeclareHandle(hloc);
	res = jsr238_get_collation_locale_name(index,locale_name,&len);
    if (res < 0 ) {
        KNI_ReleaseHandle(hloc);
		KNI_ThrowNew(jsropRuntimeException,"Get locale name");
    } else {
        KNI_NewString(locale_name, len - 1, hloc);
    }
    KNI_EndHandlesAndReturnObject(hloc);
}

/**
 * Get index of supported locales for collation by its name.
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
KNIDECL(com_sun_j2me_global_CollationAbstractionLayerImpl_getCollationLocaleIndex) {
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
		result = jsr238_get_collation_locale_index(locale_name, &index);
		if (result < 0) index =-1;
		JAVAME_FREE(locale_name);
	}

	KNI_EndHandles();
	KNI_ReturnInt(index);
}


/**
 * Compare two strings using locale- and level-specific rules.
 * <p>
 * Java declaration:
 * <pre>
 *     compare0(Ljava/lang/String;Ljava/lang/String;II)I
 * </pre>
 *
 * @param locale_index  the locale index in supported locales list
 * @param hstr1         first string to compare
 * @param hstr2         second string to compare
 * @param level         the collation level to use
 * @return negative if <code>s1</code> belongs before <code>s2</code>,
 *      zero if the strings are equal, positive if <code>s1</code> belongs
 *      after <code>s2</code>
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_StringComparatorImpl_compare0) {
   jint locale_index = KNI_GetParameterAsInt(1);
   jint level = KNI_GetParameterAsInt(4);
   jchar *s1, *s2;
   jsize s1_len, s2_len;
   jint res, compare_result = 0;

   KNI_StartHandles(2);
   KNI_DeclareHandle(hstr1);
   KNI_DeclareHandle(hstr2);
   KNI_GetParameterAsObject(2, hstr1);
   KNI_GetParameterAsObject(3, hstr2);

   s1_len = KNI_GetStringLength(hstr1);
   if (s1_len == -1){
		KNI_ThrowNew(jsropNullPointerException, NULL);
   } else {
	   s1 = (jchar *)JAVAME_MALLOC(s1_len * sizeof(jchar));
	   if (NULL == s1) {
		   KNI_ThrowNew(jsropOutOfMemoryError, 
			   "Cannot allocate string for collation");
	   } else {
		   s2_len = KNI_GetStringLength(hstr2);
		   if (s2_len == -1){
			   KNI_ThrowNew(jsropNullPointerException, NULL);
		   } else {
			   s2 = (jchar *)JAVAME_MALLOC(s2_len * sizeof(jchar));
			   if (NULL == s2) {
				   KNI_ThrowNew(jsropOutOfMemoryError, 
					   "Cannot allocate string for collation");
			   } else {
				   KNI_GetStringRegion(hstr1, 0, s1_len, s1);
				   KNI_GetStringRegion(hstr2, 0, s2_len, s2);
				   res = jsr238_compare_strings(locale_index, s1, s1_len, s2, s2_len, 
													   level, &compare_result);
				   if (res < 0){
						KNI_ThrowNew(jsropRuntimeException,"Error comparing strings");
				   }
				   JAVAME_FREE(s2);
			   }
		   }
		   JAVAME_FREE(s1);
	   }
   }

   KNI_EndHandles();
   KNI_ReturnInt(compare_result);
}
