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
#include <jsr238_format.h>

/**
 * @file
 * Implementation of Java native methods for JSR 238 classes.
 */

/**
 * Get number of supported locales for data formatting.
 * <p>
 * Java declaration:
 * <pre>
 *     getFormatLocalesCount()I
 * </pre>
 *
 * @return number of locales
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_FormatAbstractionLayerImpl_getFormatLocalesCount) {
	int count=0;
	if (jsr238_get_format_locales_count(&count)<0){
		KNI_ThrowNew(jsropRuntimeException,"Get locale count");
	}
    KNI_ReturnInt(count);
}

/**
 * Get one of supported locales (by number).
 * <p>
 * Java declaration:
 * <pre>
 *     getFormatLocaleName(I)Ljava/lang/String
 * </pre>
 *
 * @param index  index of locale to select
 * @return locale
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_global_FormatAbstractionLayerImpl_getFormatLocaleName) {
	jint len=MAX_LOCALE_LENGTH, res;
    jint index = KNI_GetParameterAsInt(1);
    jchar locale_name[MAX_LOCALE_LENGTH];

    KNI_StartHandles(1);
    KNI_DeclareHandle(hloc);
	res = jsr238_get_format_locale_name(index,locale_name,&len);
    if (res < 0 ) {
        KNI_ReleaseHandle(hloc);
		KNI_ThrowNew(jsropRuntimeException,"Get locale name");
    } else {
        KNI_NewString(locale_name, len - 1, hloc);
    }
    KNI_EndHandlesAndReturnObject(hloc);
}

/**
 * Get index of supported locales for Formatter by its name.
 * <p>
 * Java declaration:
 * <pre>
 *     getFormatterLocaleIndex(Ljava/lang/String)I
 * </pre>
 *
 * @param locale name
 * @return internal index of locale or -1 if locale is not supported 
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_global_FormatAbstractionLayerImpl_getFormatLocaleIndex) {
	jint result =-1, index = -1;
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
		result = jsr238_get_format_locale_index(locale_name, &index);
		if (result < 0) {
			index =-1;
		}
		JAVAME_FREE(locale_name);
	}

	KNI_EndHandles();
	KNI_ReturnInt(index);
}


/**
 * Formats a date as a date string for a specified locale.
 * <p>
 * Java declaration:
 * <pre>
 *     formatDateTime0(I,I,I,I,I,I,I,I,I,I,I)java/lang/String
 * </pre>
 *
 * @param index  index of locale to select
 * @param year current year
 * @param month current month
 * @param dow day of the week
 * @param dom day of the month
 * @param hour current hour
 * @param min current minute
 * @param sec current second
 * @return the date and/or time formatted as a string
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_sun_j2me_global_FormatterImpl_formatDateTime0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jint year = KNI_GetParameterAsInt(2);
    jint month = KNI_GetParameterAsInt(3);
    jint dow = KNI_GetParameterAsInt(4);
    jint dom = KNI_GetParameterAsInt(5);
    jint hour = KNI_GetParameterAsInt(6);
    jint min = KNI_GetParameterAsInt(7);
    jint sec = KNI_GetParameterAsInt(8);
    jint style = KNI_GetParameterAsInt(9);
	jsize len = 0 , res;
    jchar* buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hdata);

    res = jsr238_format_date_time(locale_index, year, month, dow, dom, hour, min, sec, style, NULL, &len);
	if (res < 0) {
		if (res == JSR238_STATUSCODE_INVALID_ARGUMENT) {
			KNI_ThrowNew(jsropIllegalArgumentException,
				"Invalid date value!");
		} else {
			KNI_ThrowNew(jsropRuntimeException, 
						 "Internal error while formatting date!");
		}
		KNI_ReleaseHandle(hdata);
	} else {
        if (NULL == (buffer = JAVAME_MALLOC(len * sizeof(jchar)))) {
            KNI_ThrowNew(jsropOutOfMemoryError, 
                         "Cannot allocate buffer for formated date");
            KNI_ReleaseHandle(hdata);
        } else {
            res = 
            jsr238_format_date_time(locale_index, year, month, dow, dom, hour, min, sec, style, buffer, &len);
            // don't forget to skip trailing zero
			if (res < 0) {
				KNI_ThrowNew(jsropRuntimeException, 
							 "Error formatting date!");
				KNI_ReleaseHandle(hdata);
			} else {
				KNI_NewString(buffer, len - 1, hdata);
			}
            JAVAME_FREE(buffer);
        }
    }
	KNI_EndHandlesAndReturnObject(hdata);
    
}

/**
 * Formats a double number for a specified locale.
 * <p>
 * Java declaration:
 * <pre>
 *     formatNumber0(I,D,I)java/lang/String
 * </pre>
 * @param index  index of locale to select
 * @param number double number to format. 
 * @param decimals the number of fractional digits
 * @return formatted double number
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_sun_j2me_global_FormatterImpl_formatNumber0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jdouble d = KNI_GetParameterAsDouble(2);
	jint decimals = KNI_GetParameterAsInt(4);
    jsize res_len = 0, res;
    jchar *buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hstr);
    res = jsr238_format_double_number(locale_index, d, decimals, NULL, &res_len);
	
    if (res < 0) {
		KNI_ThrowNew(jsropRuntimeException,"Internal error! Could not format number.");
		KNI_ReleaseHandle(hstr);
	} else {
        buffer = (jchar *)JAVAME_MALLOC(res_len * sizeof(jchar)); 
        if (NULL == buffer) {
            KNI_ThrowNew(jsropOutOfMemoryError, 
                         "Cannot allocate buffer for formatted number");
            KNI_ReleaseHandle(hstr);
        } else {
            res = jsr238_format_double_number(locale_index, d, decimals, buffer, &res_len);
			if (res<0){
				KNI_ThrowNew(jsropRuntimeException,"Internal error! Could not format number.");
				KNI_ReleaseHandle(hstr);
			} else {
				KNI_NewString((const jchar*)buffer, res_len - 1, hstr);
			}
            JAVAME_FREE(buffer);
        }
    }
    KNI_EndHandlesAndReturnObject(hstr);
}

/**
 * Formats an integer number for a specified locale.
 * <p>
 * Java declaration:
 * <pre>
 *     formatNumber1(I,L)java/lang/String
 * </pre>
 * @param index  index of locale to select
 * @param number long integer number to format. 
 * @return formatted double number
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
	KNIDECL(com_sun_j2me_global_FormatterImpl_formatNumber1) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jlong l = KNI_GetParameterAsLong(2);
    jsize res_len = 0, res;
    jchar *buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hstr);
    res = jsr238_format_integer_number(locale_index, l, NULL, &res_len);
    if (res < 0) {
		KNI_ThrowNew(jsropRuntimeException,"Internal error! Could not format number.");
		KNI_ReleaseHandle(hstr);
	} else {
        buffer = (jchar *)JAVAME_MALLOC(res_len * sizeof(jchar)); 
        if (NULL == buffer) {
            KNI_ThrowNew(jsropOutOfMemoryError, 
                         "Cannot allocate buffer for formatted number");
            KNI_ReleaseHandle(hstr);
        } else {
            res = jsr238_format_integer_number(locale_index, l, buffer, &res_len);
			if (res < 0) {
				KNI_ThrowNew(jsropRuntimeException,"Internal error! Could not format number.");
				KNI_ReleaseHandle(hstr);
			} else {
				KNI_NewString((const jchar*)buffer, res_len - 1, hstr);
			}
            JAVAME_FREE(buffer);
        }
    }
    KNI_EndHandlesAndReturnObject(hstr);
}

/**
 * Formats a number string as a currency string for a specified locale.
 * <p>
 * Java declaration:
 * <pre>
 *     formatCurrency0(I,D,java/lang/String)java/lang/String
 * </pre>
 * 
 * @param index  index of locale to select
 * @param number  double number to format as currency value. 
 * @param currencyCode  the ISO 4217 currency code to use
 * @return formatted currency amount
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_sun_j2me_global_FormatterImpl_formatCurrency0) {
    jint locale_index = KNI_GetParameterAsInt(1);
	jdouble d = KNI_GetParameterAsDouble(2);
    jsize code_len, res_len = 0, res;
	jint error=0;
    jchar *code, *buffer;

    KNI_StartHandles(2);
    KNI_DeclareHandle(hstr_code);
    KNI_DeclareHandle(hstr_res);

    KNI_GetParameterAsObject(4, hstr_code);
	code_len = KNI_GetStringLength(hstr_code);//must be null terminated

	if (code_len>=0) {
		code = (jchar *)JAVAME_MALLOC((code_len +1)  * sizeof(jchar));
		if (NULL == code) {
			error = 1;
			KNI_ThrowNew(jsropOutOfMemoryError, 
						 "Cannot allocate string for formatting");
			KNI_ReleaseHandle(hstr_res);
		} else {
			KNI_GetStringRegion(hstr_code, 0, code_len, code);
			code[code_len] = 0;
		}
	} else {
		code = NULL;
	}

	if (!error){
		res = jsr238_format_currency(locale_index, d, code, NULL, &res_len);

		if (res < 0) {
			KNI_ThrowNew(jsropRuntimeException, 
						 "Could not format currency value");
				KNI_ReleaseHandle(hstr_res);
		} else {

			buffer = JAVAME_MALLOC(res_len * sizeof(jchar)); 

			if (NULL == buffer) {
					KNI_ThrowNew(jsropOutOfMemoryError, 
								 "Cannot allocate buffer for currency format");
					KNI_ReleaseHandle(hstr_res);
			} else {
				res = jsr238_format_currency(locale_index, d, code, buffer, &res_len);
				if (res < 0) {
					KNI_ThrowNew(jsropRuntimeException, 
								 "Could not format currency value");
						KNI_ReleaseHandle(hstr_res);
				} else {
					KNI_NewString(buffer, res_len - 1, hstr_res);
				}
				JAVAME_FREE(buffer);
			}
		}
	}

	if (code) JAVAME_FREE(code);
    KNI_EndHandlesAndReturnObject(hstr_res);
}


/**
 * Formats an float percentage value using locale-specific rules.
 * <p>
 * Java declaration:
 * <pre>
 *     formatPercentage0(I,F,I)java/lang/String
 * </pre>
 * 
 * @param index  index of locale to select
 * @param number float number to format. 
 * @param decimals the number of fractional digits
 * @return formatted percentage amount
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_sun_j2me_global_FormatterImpl_formatPercentage0) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jfloat f = KNI_GetParameterAsFloat(2);
	jint decimals  = KNI_GetParameterAsInt(3);
    jsize res_len = 0, res;
    jchar *buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hstr);
    res = jsr238_format_float_percentage(locale_index, f, decimals, NULL, &res_len);
    if (res < 0) {
		KNI_ThrowNew(jsropRuntimeException,"Could not format percentage value");
		KNI_ReleaseHandle(hstr);
	} else {
        buffer = (jchar *)JAVAME_MALLOC(res_len * sizeof(jchar)); 
        if (NULL == buffer) {
            KNI_ThrowNew(jsropOutOfMemoryError, 
                         "Cannot allocate buffer for formatted number");
            KNI_ReleaseHandle(hstr);
        } else {
            res = jsr238_format_float_percentage(locale_index, f, decimals, buffer, &res_len);
			if (res < 0){
				KNI_ThrowNew(jsropRuntimeException,"Could not format percentage value");
				KNI_ReleaseHandle(hstr);
			} else {
				KNI_NewString((const jchar*)buffer, res_len - 1, hstr);
			}
            JAVAME_FREE(buffer);
        }
    }
    KNI_EndHandlesAndReturnObject(hstr);
}

/**
 * Formats an integral percentage value using locale-specific rules.
 * <p>
 * Java declaration:
 * <pre>
 *     formatPercentage1(I,L)java/lang/String
 * </pre>
 * 
 * @param index  index of locale to select
 * @param number long integer number to format. 
 * @return formatted percentage amount
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_sun_j2me_global_FormatterImpl_formatPercentage1) {
    jint locale_index = KNI_GetParameterAsInt(1);
    jlong l = KNI_GetParameterAsLong(2);
    jsize res_len = 0, res;
    jchar *buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(hstr);
    res = jsr238_format_integer_percentage(locale_index, l, NULL, &res_len);
    if (res < 0) {
		KNI_ThrowNew(jsropRuntimeException,"Could not format number");
		KNI_ReleaseHandle(hstr);
	} else {
        buffer = (jchar *)JAVAME_MALLOC(res_len * sizeof(jchar)); 
        if (NULL == buffer) {
            KNI_ThrowNew(jsropOutOfMemoryError, 
                         "Cannot allocate buffer for formatted number");
            KNI_ReleaseHandle(hstr);
        } else {
            res = jsr238_format_integer_percentage(locale_index, l, buffer, &res_len);
			if (res<0){
				KNI_ThrowNew(jsropRuntimeException,"Could not format number");
				KNI_ReleaseHandle(hstr);
			} else {
				KNI_NewString((const jchar*)buffer, res_len - 1, hstr);
			}
            JAVAME_FREE(buffer);
        }
    }
    KNI_EndHandlesAndReturnObject(hstr);
}
