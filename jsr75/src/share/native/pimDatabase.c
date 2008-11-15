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

#include <string.h>

#include <kni.h>

#include "jsrop_memory.h"
#include "jsrop_exceptions.h"
#include "javacall_pim.h"
#include "javautil_unicode.h"

#define OUT_BUFFER_LEN      256
#define DEF_LIST_NAME       32*3
#define DATA_BUFFER_LEN     JAVACALL_PIM_MAX_BUFFER_SIZE
#define CATEGORIES_MAX_LEN  (DATA_BUFFER_LEN / 3)

#define ALLOCATE_OUTPUT_BUFFER() \
    JAVAME_MALLOC(OUT_BUFFER_LEN * sizeof(javacall_utf16))
#define ALLOCATE_DATA_BUFFER() \
    JAVAME_MALLOC(DATA_BUFFER_LEN * sizeof(javacall_utf16))
#define FREE_BUFFER(x)		  JAVAME_FREE(x)

#define PIMPROXY_FIELD_NAME_DATA    "dataHandler"
#define PIMPROXY_FIELD_NAME_COUNTER "itemCounter"

typedef enum {
    DATA_BUFFER_EMPTY,
    DATA_BUFFER_ITEM,
    DATA_BUFFER_FIELDS,
    DATA_BUFFER_ATTRIBUTES
} data_buffer_state;

/** buffer for default lists names storing */
//static char default_lists[DEF_LIST_NAME];
//static unsigned char default_list_initialized = 0;

static const struct list_table {
    char* field_name;
    javacall_pim_type type;
} list_types[] = {
    {"CONTACT_LIST", JAVACALL_PIM_TYPE_CONTACT},
    {"EVENT_LIST",   JAVACALL_PIM_TYPE_EVENT},
    {"TODO_LIST",    JAVACALL_PIM_TYPE_TODO}
};

static const struct mode_table {
    char* field_name;
    javacall_pim_open_mode mode;
} open_modes[] = {
    {"READ_ONLY",    JAVACALL_PIM_OPEN_MODE_READ_ONLY},
    {"WRITE_ONLY",   JAVACALL_PIM_OPEN_MODE_WRITE_ONLY},
    {"READ_WRITE",   JAVACALL_PIM_OPEN_MODE_READ_WRITE}
};

static javacall_pim_open_mode
convert_open_mode(KNIDECLARGS int openMode) {
    javacall_pim_open_mode mode = JAVACALL_PIM_OPEN_MODE_READ_ONLY;
    jfieldID            listConstID;
    unsigned            index;

    KNI_StartHandles(1);
    KNI_DeclareHandle(pimClass);

    KNI_FindClass("javax/microedition/pim/PIM", pimClass);
    if (!KNI_IsNullHandle(pimClass)) {
        for (index = 0;
             index < sizeof(open_modes) / sizeof(struct mode_table);
             index++) {
            listConstID = KNI_GetStaticFieldID(pimClass, open_modes[index].field_name, "I");
            if (openMode == KNI_GetStaticIntField(pimClass, listConstID)) {
                mode = open_modes[index].mode;
                break;
            }
        }
    }
    KNI_EndHandles();
    return mode;
}

static javacall_pim_type
convert_list_type(KNIDECLARGS int listType) {
    javacall_pim_type type = 0;
    jfieldID       listConstID;
    unsigned       index;

    KNI_StartHandles(1);
    KNI_DeclareHandle(pimClass);

    KNI_FindClass("javax/microedition/pim/PIM", pimClass);
    if (!KNI_IsNullHandle(pimClass)) {
        for (index = 0;
             index < sizeof(list_types) / sizeof(struct list_table);
             index++) {
            listConstID = KNI_GetStaticFieldID(pimClass, list_types[index].field_name, "I");
            if (listType == KNI_GetStaticIntField(pimClass, listConstID)) {
                type = list_types[index].type;
                break;
            }
        }
    }
    KNI_EndHandles();
    return type;
}
	
/* Gets item's data and categories from Java objects. */
static javacall_result
extract_item_data(KNIDECLARGS jobject data_array,
                             unsigned char **data_buffer,
                             jobject categories,
                             javacall_utf16 **cats_buffer) {
    int data_length;
    int cats_length;
    
    if (KNI_IsNullHandle(data_array)) {
        return JAVACALL_FAIL;
    }
    data_length = KNI_GetArrayLength(data_array);
    *data_buffer = JAVAME_MALLOC(data_length + 1);
    if (*data_buffer == NULL) {
        return JAVACALL_FAIL;
    }

    KNI_GetRawArrayRegion(data_array, 0, data_length, (jbyte *)*data_buffer);
    *(*data_buffer + data_length) = 0;
    
    if (!KNI_IsNullHandle(categories)) {
        cats_length = KNI_GetStringLength(categories);
        *cats_buffer = JAVAME_MALLOC(
            (cats_length + 1) * sizeof(javacall_utf16));
    }
    else {
        cats_length = 0;
    }
    if (cats_length && *cats_buffer == NULL) {
        return JAVACALL_FAIL;
    }
    if (cats_length) {
        KNI_GetStringRegion(categories, 0, cats_length, *cats_buffer);
        *(*cats_buffer + cats_length) = 0;
    }
    return JAVACALL_OK;
}

static int
getIndexOfDelimiter(javacall_utf16* string) {
    javacall_utf16* current;
    
    for (current = string;
         *current != 0 && *current != JAVACALL_PIM_STRING_DELIMITER;
         current++) {
    }
    return current - string;
}

/**
 * Returns field ID for a given field
 * @param name name of a field
 * @param pObject pointer to object
 * @return field ID
 */
static jfieldID
getFieldID(KNIDECLARGS char* name, jobject* pObject) {
    jfieldID fieldID;
    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    
    KNI_GetThisPointer(*pObject);
    KNI_GetObjectClass(*pObject, classHandle);
    fieldID = KNI_GetFieldID(classHandle, name, "I");
    
    KNI_EndHandles();
    return fieldID;
}

/**
 * Sets value of field
 * @param name name of a field
 * @param value value to be set
 */
static void
setFieldValue(KNIDECLARGS char* name, int value) {
    jfieldID fieldID;
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    fieldID = getFieldID(KNIPASSARGS name, &objectHandle);
    KNI_SetIntField(objectHandle, fieldID, (jint) value);
    
    KNI_EndHandles();
}

/**
 * Gets value of a filed
 * @param name name of a field
 * @return field value
 */
static int
getFieldValue(KNIDECLARGS char* name) {
    jfieldID fieldID;
    int value;
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    fieldID = getFieldID(KNIPASSARGS name, &objectHandle);
    value = KNI_GetIntField(objectHandle, fieldID);
    
    KNI_EndHandles();
    return value;
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_pim_PIMProxy_getDefaultListName) {
    javacall_pim_type listType;
    javacall_utf16* output_buffer = ALLOCATE_OUTPUT_BUFFER();
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    listType = convert_list_type(KNIPASSARGS KNI_GetParameterAsInt(1));

    KNI_ReleaseHandle(objectHandle);
    if (output_buffer != NULL) {
        if (javacall_pim_get_lists(listType,
                                output_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            int defaultNameLength = getIndexOfDelimiter(output_buffer);
    
            if (defaultNameLength != -1) {
                KNI_NewString((jchar *)output_buffer, defaultNameLength, objectHandle);
            }
        }
        JAVAME_FREE(output_buffer);
    }
    KNI_EndHandlesAndReturnObject(objectHandle);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getListNamesCount0) {
    javacall_pim_type listType;
    javacall_utf16 *current;
    int namesCount = 0;
    javacall_utf16* output_buffer = ALLOCATE_OUTPUT_BUFFER();
    
    if (output_buffer != NULL) {
        listType = convert_list_type(KNIPASSARGS KNI_GetParameterAsInt(1));
        if (listType && javacall_pim_get_lists(listType, output_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            for (current = output_buffer, namesCount = 1;
                *current;
                current++) {
                if (*current == JAVACALL_PIM_STRING_DELIMITER) {
                    namesCount++;
                }
            }
            if (current == output_buffer) {
                namesCount = 0;
            }
        }
    }
    else {
        KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        setFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_DATA, 0);
    }
    if (output_buffer != NULL) {
        if (namesCount > 0) {
            setFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_COUNTER, namesCount);
            setFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_DATA, (int)output_buffer);
        }
        else {
            JAVAME_FREE(output_buffer);
        }
    }

    KNI_ReturnInt(namesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_j2me_pim_PIMProxy_getListNames0) {
    int namesCount = getFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_COUNTER);
    javacall_utf16* output_buffer = 
        (javacall_utf16 *)getFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_DATA);
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(listNames);
    KNI_DeclareHandle(name);
    
    KNI_GetParameterAsObject(1, listNames);
    
    if (namesCount > 0 &&
        namesCount == KNI_GetArrayLength(listNames) &&
        output_buffer) {
        javacall_utf16 *first, *current;
        int filledCount = 0;

        first = current = output_buffer;
        do {
            if (*current == JAVACALL_PIM_STRING_DELIMITER || 
                *(current) == 0) {
                KNI_NewString(first, current - first, name);
                KNI_SetObjectArrayElement(listNames, filledCount, name);
                first = current + 1;
                filledCount ++;
            }
          current++;
        } while(*(current - 1) && filledCount < namesCount);
    }
    if (output_buffer) {
        JAVAME_FREE(output_buffer);
    }
    setFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_DATA, 0);
    setFieldValue(KNIPASSARGS PIMPROXY_FIELD_NAME_COUNTER, 0);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_listOpen0) {
    int listType, openMode;
    int listNameLength;
    javacall_handle listHandle = (javacall_handle)0;
    javacall_utf16 *list_name = NULL;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(listName);
    
    listType = convert_list_type(KNIPASSARGS KNI_GetParameterAsInt(1));
    KNI_GetParameterAsObject(2, listName);
    openMode = convert_open_mode(KNIPASSARGS KNI_GetParameterAsInt(3));
    
    if (!KNI_IsNullHandle(listName)) {
        listNameLength = KNI_GetStringLength(listName);
        list_name = JAVAME_MALLOC(
            (listNameLength + 1) * sizeof(javacall_utf16));
        if (list_name != NULL) {
            KNI_GetStringRegion(listName, 0, listNameLength, list_name);
            list_name[listNameLength] = 0;
        }
        else {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        }
    }
    javacall_pim_list_open(listType, list_name, openMode, &listHandle);
    if (list_name != NULL) {
        JAVAME_FREE(list_name);
    }

    KNI_EndHandles();
    KNI_ReturnInt(listHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_getNextItemDescription0) {
    int listHandle;
    javacall_result result = JAVACALL_FAIL;
    unsigned char* data_buffer = ALLOCATE_DATA_BUFFER();
    javacall_handle item_handle;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dscrArray);

    listHandle = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dscrArray);

    if (KNI_GetArrayLength(dscrArray) >= 4 &&
        data_buffer != NULL) {
        result = javacall_pim_list_get_next_item((javacall_handle)listHandle, 
                                                 data_buffer,
                                                 DATA_BUFFER_LEN - CATEGORIES_MAX_LEN,
                                                 (javacall_utf16 *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN),
                                                 CATEGORIES_MAX_LEN / sizeof(javacall_utf16),
                                                 &item_handle);
        if (result == JAVACALL_OK) {
            KNI_SetIntArrayElement(dscrArray, 0, (jint)item_handle);
            KNI_SetIntArrayElement(dscrArray, 1, strlen((char *)data_buffer));
            KNI_SetIntArrayElement(dscrArray, 2, 1);
            KNI_SetIntArrayElement(dscrArray, 3, (jint)data_buffer);
        }
        else {
            JAVAME_FREE(data_buffer);
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_listClose0) {
    int listHandle;
    javacall_result result;

    listHandle = KNI_GetParameterAsInt(1);
    result = javacall_pim_list_close((javacall_handle)listHandle);
    
    KNI_ReturnBoolean(result == JAVACALL_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_getNextItemData0) {
    javacall_handle itemHandle;
    int result = 0;
    unsigned char *data_buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataArray);
    KNI_GetParameterAsObject(2, dataArray);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    data_buffer = (unsigned char *)KNI_GetParameterAsInt(3);
    
    if (data_buffer != NULL) {
        KNI_SetRawArrayRegion(dataArray, 0, KNI_GetArrayLength(dataArray), (jbyte *)data_buffer);
        result = 1;
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_pim_PIMProxy_getItemCategories0) {
    javacall_handle itemHandle;
    char *data_buffer;    

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    data_buffer = (char *)KNI_GetParameterAsInt(2);
    
    if (data_buffer != NULL) {
        javacall_utf16* last_cats = (javacall_utf16 *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN);
        javacall_int32 str_len;
    
        if (JAVACALL_OK == javautil_unicode_utf16_chlength(last_cats, &str_len)) {
            KNI_NewString(last_cats, str_len, categories);
        }
        JAVAME_FREE(data_buffer);
    }
    else {
        KNI_ReleaseHandle(categories);
    }
    
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_commitItemData0) {
    javacall_handle  listHandle, itemHandle;
    javacall_result  result = JAVACALL_FAIL;
    javacall_utf16 *cats = NULL;
    unsigned char* data_buffer = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(2);
    KNI_GetParameterAsObject(3, dataArray);
    KNI_GetParameterAsObject(4, categories);
            
    if (JAVACALL_OK == extract_item_data(KNIPASSARGS dataArray, &data_buffer, categories, &cats)) {
        result = javacall_pim_list_modify_item(listHandle, itemHandle, data_buffer, cats);
    }
    if (data_buffer != NULL) {
        JAVAME_FREE(data_buffer);
    }
    if (cats != NULL) {
        JAVAME_FREE(cats);
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_addItem0) {
    javacall_handle listHandle;
    javacall_handle itemHandle = NULL;
    javacall_utf16 *cats = NULL;
    unsigned char* data_buffer = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dataArray);
    KNI_GetParameterAsObject(3, categories);
        
    if (JAVACALL_OK == extract_item_data(KNIPASSARGS dataArray, &data_buffer,
                                categories, &cats)) {    
        javacall_pim_list_add_item(listHandle, data_buffer, cats, &itemHandle);
    }

    if (data_buffer != NULL) {
        JAVAME_FREE(data_buffer);
    }
    if (cats != NULL) {
        JAVAME_FREE(cats);
    }
    
    KNI_EndHandles();
    KNI_ReturnInt(itemHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_removeItem0) {
    javacall_handle listHandle, itemHandle;
    javacall_result result;

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(2);
        
    result = javacall_pim_list_remove_item(listHandle, itemHandle);

    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_pim_PIMProxy_getListCategories0) {
    javacall_handle listHandle;
    javacall_utf16* data_buffer = ALLOCATE_OUTPUT_BUFFER();

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    
    KNI_ReleaseHandle(categories);
    if (data_buffer != NULL) {
        if (javacall_pim_list_get_categories(listHandle, data_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            javacall_int32 str_len;
    
            if (JAVACALL_OK == javautil_unicode_utf16_chlength(data_buffer, &str_len) && str_len > 0) {
                KNI_NewString(data_buffer, str_len, categories);
            }
        }
        JAVAME_FREE(data_buffer);
    }
    else {
        KNI_ThrowNew(jsropOutOfMemoryError, NULL);
    }
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getListMaxCategories0) {
    javacall_handle listHandle;
    int max_categories;

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
        
    max_categories = javacall_pim_list_max_categories(listHandle);

    KNI_ReturnInt(max_categories);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getListMaxCategoriesPerItem0) {
    javacall_handle listHandle;
    int max_categories;

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
        
    max_categories = javacall_pim_list_max_categories_per_item(listHandle);

    KNI_ReturnInt(max_categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_addListCategory0) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        javacall_utf16* category_str;
        
        cat_len = KNI_GetStringLength(category);
        category_str = JAVAME_MALLOC((cat_len + 1) * sizeof(javacall_utf16));
        if (category_str != NULL) {
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = javacall_pim_list_add_category(listHandle, category_str);
            JAVAME_FREE(category_str);
        }
        else {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_deleteListCategory0) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        javacall_utf16* category_str;
        
        cat_len = KNI_GetStringLength(category);
        category_str = JAVAME_MALLOC((cat_len + 1) * sizeof(javacall_utf16));
        if (category_str != NULL) {
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = javacall_pim_list_remove_category(listHandle, category_str);
            JAVAME_FREE(category_str);
        }
        else {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_j2me_pim_PIMProxy_renameListCategory0) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cur_cat_len, new_cat_len;

    KNI_StartHandles(2);
    KNI_DeclareHandle(cur_category);
    KNI_DeclareHandle(new_category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, cur_category);
    KNI_GetParameterAsObject(3, new_category);
        
    if (!KNI_IsNullHandle(cur_category) && !KNI_IsNullHandle(new_category)) {
        javacall_utf16* cur_category_str;
        javacall_utf16* new_category_str;
    
        cur_cat_len = KNI_GetStringLength(cur_category);
        cur_category_str = JAVAME_MALLOC(
            (cur_cat_len + 1) * sizeof(javacall_utf16));
        new_cat_len = KNI_GetStringLength(new_category);
        new_category_str = JAVAME_MALLOC(
            (new_cat_len + 1) * sizeof(javacall_utf16));
        if (cur_category_str != NULL &&
            new_category_str != NULL) {
            KNI_GetStringRegion(cur_category, 0, cur_cat_len, cur_category_str);
            KNI_GetStringRegion(new_category, 0, new_cat_len, new_category_str);
            cur_category_str[cur_cat_len] = 0;
            new_category_str[new_cat_len] = 0;

            result = javacall_pim_list_rename_category(listHandle, cur_category_str, new_category_str);
            
            
        }
        else {
            KNI_ThrowNew(jsropOutOfMemoryError, NULL);
        }
        if (cur_category_str != NULL) {
            JAVAME_FREE(cur_category_str);
        }
        if (new_category_str != NULL) {
            JAVAME_FREE(new_category_str);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getFieldsCount0) {
    int fieldsCount = 0;
    javacall_pim_field* fields;

    fields = JAVAME_MALLOC(
        JAVACALL_PIM_MAX_FIELDS * sizeof(javacall_pim_field));
    if (fields != NULL) {
        javacall_handle list_handle;
        KNI_StartHandles(1);
        KNI_DeclareHandle(arrayHandle);
        
        list_handle = (javacall_handle)KNI_GetParameterAsInt(1);
        KNI_GetParameterAsObject(2, arrayHandle);
        if (!KNI_IsNullHandle(arrayHandle) &&
             KNI_GetArrayLength(arrayHandle) > 0) {
            if (JAVACALL_OK == javacall_pim_list_get_fields(list_handle, fields, JAVACALL_PIM_MAX_FIELDS)) {
                while (fieldsCount < JAVACALL_PIM_MAX_FIELDS &&
                    fields[fieldsCount].id != JAVACALL_PIM_INVALID_ID) {
                    fieldsCount++;
                }
                KNI_SetIntArrayElement(arrayHandle, 0, (jint)fields);
            }
            else {
                JAVAME_FREE(fields);
            }
        }
        else {
            JAVAME_FREE(fields);
        }
        KNI_EndHandles();
    }
    else {
        KNI_ThrowNew(jsropOutOfMemoryError, NULL);
    }

    KNI_ReturnInt(fieldsCount);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getFieldLabelsCount0) {
    javacall_pim_field *fields = (javacall_pim_field *)KNI_GetParameterAsInt(3);
    int fieldIndex = KNI_GetParameterAsInt(2);
    int labelsCount = 0;

    if (fields == NULL) {
        KNI_ThrowNew(jsropRuntimeException, NULL);
    } else {
        while (labelsCount < JAVACALL_PIM_MAX_ARRAY_ELEMENTS &&
               JAVACALL_PIM_INVALID_ID != fields[fieldIndex].arrayElements[labelsCount].id) {
            labelsCount++;
        }
    }

    KNI_ReturnInt(labelsCount);
}

#define GET_FIELDID(classHandle, id, fieldName, fieldType)      \
    if (id == NULL)                                             \
    {                                                           \
        id = KNI_GetFieldID(classHandle, fieldName, fieldType); \
        if (id == 0) {                                          \
            KNI_ThrowNew(jsropNullPointerException,              \
                "Invalid romizer settings");                    \
        }                                                       \
    }

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_j2me_pim_PIMProxy_getFields0) {
    javacall_pim_field *fields = 
        (javacall_pim_field *)KNI_GetParameterAsInt(3);
    int descArraySize;
    int labelArraySize;
    int i, j;
    javacall_utf16 *str;
    jfieldID descFieldID          = NULL;
    jfieldID descDataTypeID       = NULL;
    jfieldID descLabelID          = NULL;
    jfieldID descLabelResourcesID = NULL;
    jfieldID descAttributesID     = NULL;
    jfieldID descMaxValuesID      = NULL;

    KNI_StartHandles(5);
    KNI_DeclareHandle(descArray);
    KNI_DeclareHandle(desc);
    KNI_DeclareHandle(descClass);
    KNI_DeclareHandle(label);
    KNI_DeclareHandle(labelArray);

    if (fields == NULL) {
        KNI_ThrowNew(jsropRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, descArray);
        descArraySize = KNI_GetArrayLength(descArray);
        KNI_FindClass("com/sun/j2me/pim/PIMFieldDescriptor", descClass);
        if (KNI_IsNullHandle(descClass)) {
            KNI_ThrowNew(jsropNullPointerException, "Invalid romizer settings");
        }
        GET_FIELDID(descClass, descFieldID,          "field",          "I")
        GET_FIELDID(descClass, descDataTypeID,       "dataType",       "I")
        GET_FIELDID(descClass, descLabelID,          "label",          "Ljava/lang/String;")
        GET_FIELDID(descClass, descLabelResourcesID, "labelResources", "[Ljava/lang/String;")
        GET_FIELDID(descClass, descAttributesID,     "attributes",     "J")
        GET_FIELDID(descClass, descMaxValuesID,      "maxValues",      "I")

        for (i = 0; i < descArraySize; i++) {
            javacall_int32 str_len;

            KNI_GetObjectArrayElement(descArray, i, desc);
            KNI_SetIntField(desc, descFieldID, fields[i].id);
            KNI_SetIntField(desc, descDataTypeID, fields[i].type);
            KNI_SetIntField(desc, descAttributesID, fields[i].attributes);
            KNI_SetIntField(desc, descMaxValuesID, fields[i].maxValues);
            str = fields[i].label;
    
            if (JAVACALL_OK == javautil_unicode_utf16_chlength(str, &str_len)) {
                KNI_NewString(str, str_len, label);
            }
            KNI_SetObjectField(desc, descLabelID, label);
            KNI_GetObjectField(desc, descLabelResourcesID, labelArray);
            labelArraySize = KNI_GetArrayLength(labelArray);
            // should check array size and labels count here!!!
            for (j = 0; j < labelArraySize; j++) {
                str = fields[i].arrayElements[j].label;
                if (JAVACALL_OK == javautil_unicode_utf16_chlength(str, &str_len)) {
                    KNI_NewString(str, str_len, label);
                }
                KNI_SetObjectArrayElement(labelArray, j, label);
            }
        }
        JAVAME_FREE(fields);
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_j2me_pim_PIMProxy_getAttributesCount0) {
    javacall_pim_field_attribute *attributes;
    int attributesCount = 0;
    javacall_handle list_handle;

    attributes = JAVAME_MALLOC(
        JAVACALL_PIM_MAX_ATTRIBUTES * sizeof(javacall_pim_field_attribute));
    if (attributes != NULL) {
        list_handle = (javacall_handle)KNI_GetParameterAsInt(1);
        if (JAVACALL_OK == javacall_pim_list_get_attributes(list_handle,
                attributes, JAVACALL_PIM_MAX_ATTRIBUTES)) {
            KNI_StartHandles(1);
            KNI_DeclareHandle(arrayHandle);
            
            KNI_GetParameterAsObject(2, arrayHandle);
            if (!KNI_IsNullHandle(arrayHandle) &&
                KNI_GetArrayLength(arrayHandle) > 0) {
                while (attributesCount < JAVACALL_PIM_MAX_ATTRIBUTES &&
                    attributes[attributesCount].id != JAVACALL_PIM_INVALID_ID) {
                    attributesCount++;
                }
                KNI_SetIntArrayElement(arrayHandle, 0, (jint)attributes);
            }
            else {
                JAVAME_FREE(attributes);
            }
            KNI_EndHandles();
        }
        else {
            JAVAME_FREE(attributes);
        }
    }
    else {
        KNI_ThrowNew(jsropOutOfMemoryError, NULL);
    }

    KNI_ReturnInt(attributesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_j2me_pim_PIMProxy_getAttributes0) {
    javacall_pim_field_attribute *attributes =
        (javacall_pim_field_attribute *)KNI_GetParameterAsInt(3);
    int attrArraySize;
    int i;
    javacall_utf16 *str;
    jfieldID attrAttrID  = NULL;
    jfieldID attrLabelID = NULL;

    KNI_StartHandles(5);
    KNI_DeclareHandle(attrArray);
    KNI_DeclareHandle(attr);
    KNI_DeclareHandle(attrClass);
    KNI_DeclareHandle(label);
    KNI_DeclareHandle(attrObj);

    if (attributes == NULL) {
        KNI_ThrowNew(jsropRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, attrArray);
        attrArraySize = KNI_GetArrayLength(attrArray);
        if (attrArraySize > 0) {
            KNI_GetObjectArrayElement(attrArray, 0, attrObj);
            KNI_GetObjectClass(attrObj, attrClass);
            if (KNI_IsNullHandle(attrClass)) {
                KNI_ThrowNew(jsropNullPointerException, "Invalid romizer settings");
            }
            else {
                GET_FIELDID(attrClass, attrAttrID,  "attr",  "I")
                GET_FIELDID(attrClass, attrLabelID, "label", "Ljava/lang/String;")

                for (i = 0; i < attrArraySize; i++) {
                    javacall_int32 str_len;
                    KNI_GetObjectArrayElement(attrArray, i, attr);
                    KNI_SetIntField(attr, attrAttrID, attributes[i].id);
                    str = attributes[i].label;
                    if (JAVACALL_OK == javautil_unicode_utf16_chlength(str, &str_len)) {
                        KNI_NewString(str, str_len, label);
                    }
                    KNI_SetObjectField(attr, attrLabelID, label);
                }
            }
        }
        JAVAME_FREE(attributes);
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}
