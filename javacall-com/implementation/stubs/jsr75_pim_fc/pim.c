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

#include "javacall_pim.h"
#include "javacall_logging.h"
#include "javacall_file.h"
#include "javacall_dir.h"


/* FOR DEBUG */
javacall_result javacall_dir_get_root_path(javacall_utf16*, int *);


/**
 * Returns JAVACALL_PIM_STRING_DELIMITER separated list that contains the names
 * of PIM lists that match the given list type
 * (e.g., "Contact" Or "JohnContact\nSuziContact").
 *
 * @param listType Type of the PIM list the user wishes to obtain.
 * @param pimList Pointer to the buffer in which to store the returned list.
 *                The list must be delimited by JAVACALL_PIM_STRING_DELIMITER).
 *                The default list name should appear in the first place.
 * @param pimListLen Buffer size.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL when no list exists or when the buffer size is too
 *                       small.
 */
javacall_result javacall_pim_get_lists(javacall_pim_type listType,
    javacall_utf16 /*OUT*/ *pimList, int pimListLen) {
    return JAVACALL_FAIL;
}

/**
 * Checks if a given PIM list type is supported by the platform.
 *
 * @return JAVACALL_TRUE if the list type is supported,
 *         JAVACALL_FALSE otherwise.
 */
javacall_bool javacall_pim_list_is_supported_type(javacall_pim_type listType) {
    return JAVACALL_FALSE;
}

/**
 * Opens the requested PIM list in the given mode.
 *
 * @param listType type of the PIM list to open.
 * @param pimListName the name of the list to open,
 *        if pimList is null the handle of default dummy
 *        list will be returned; this kind of list may
 *        be used only for getting default list structure.
 * @param mode the open mode for the list.
 * @param listHandle pointer to where to store the list handle.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT If an invalid mode is provided as a
 *         parameter or if listType is not a valid PIM list type
 * @retval JAVACALL_FAIL on other error.
 */
javacall_result javacall_pim_list_open(javacall_pim_type listType,
    javacall_utf16 *pimList, javacall_pim_open_mode mode,
    javacall_handle *listHandle) {   
    return JAVACALL_FAIL;
}

/**
 * Closes an open PIM list.
 *
 * @param listHandle a handle of the list to close.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case the list is no longer accessible.
 */
javacall_result javacall_pim_list_close(javacall_handle listHandle) {
    return JAVACALL_FAIL;
}

/**
 * Returns the next item in the given PIM list.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle of the list from which to get the item.
 * @param item pointer to a buffer in which to store the item.
 * @param maxItemLen the maximum size of the item.
 * @param categories pointer to a buffer in which to store the item's
 *                   categories separated by JAVACALL_PIM_STRING_DELIMITER.
 * @param maxCategoriesLen size of the categories buffer.
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the returned item.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT  maxItemLen is too small
 * @retval JAVACALL_FAIL in case reached the last item in the list.
 */
javacall_result javacall_pim_list_get_next_item(javacall_handle listHandle,
    unsigned char *item, int maxItemLen, javacall_utf16 *categories,
    int maxCategoriesLen, javacall_handle *itemHandle) {
    return JAVACALL_FAIL;
}

/**
 * Modifies an existing item.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle to the list to which the item belongs.
 * @param itemHandle handle of the item to modify.
 * @param item pointer to the new item data.
 * @param categories pointer to the item's categories separated by comma.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of an error.
 */
javacall_result javacall_pim_list_modify_item(javacall_handle listHandle,
    javacall_handle itemHandle, const unsigned char *item,
    const javacall_utf16 *categories) {
    return JAVACALL_FAIL;
}

/**
 * Adds a new item to the given item list.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle to the list to which to add the new item.
 * @param item pointer to the item to add to the list.
 * @param categories pointer to the item's categories separated by comma.
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the new item.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of an error.
 */
javacall_result javacall_pim_list_add_item(javacall_handle listHandle,
    const unsigned char *item, const javacall_utf16 *categories,
    javacall_handle *itemHandle) {
    return JAVACALL_FAIL;
}

/**
 * Removes an item from the list.
 *
 * @param listHandle handle to the list from which to delete the item.
 * @param itemHandle handle to the item to remove.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT if either of the handles is not valid
 * @retval JAVACALL_FAIL in case of any error.
 */
javacall_result javacall_pim_list_remove_item(javacall_handle listHandle,
    javacall_handle itemHandle) {
    return JAVACALL_FAIL;
}

/**
 * Adds the given category to the PIM list.
 * If the given category already exists in the list, this function does not add
 * another category, considers the call successful and returns.
 *
 * If the string is not a valid category as defined by the platform,
 * JAVACALL_FAIL is returned when trying to add it.
 *
 * @param listHandle handle of the list to add the new category to.
 * @param categoryName the name of the category to be added.
 * @retval JAVACALL_OK  on success
 * @retval JAVACALL_FAIL   if categories are unsupported, an error occurs,
 *                         or the list is no longer accessible or closed.
 */
javacall_result javacall_pim_list_add_category(javacall_handle listHandle,
    javacall_utf16 *categoryName) {
    return JAVACALL_FAIL;
}

/**
 * Removes the indicated category from the PIM list. If the indicated category
 * is not in the PIM list, this call is treated as successfully completed.
 *
 * @param listHandle handle to the list from which to remove the given category
 * @param categoryName the name of the category to be removed.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL if categories are unsupported, an error occurs,
 *                       or the list is no longer accessible or closed.
 */
javacall_result javacall_pim_list_remove_category(javacall_handle listHandle,
    javacall_utf16 *categoryName) {
    return JAVACALL_FAIL;
}

/**
 * Renames a category.
 * If the new name is not valid for a category on the platform,
 * JAVACALL_FAIL must be returned when trying to rename a category to it.
 *
 * @param listHandle handle to the list to rename the category in.
 * @param oldCategoryName the old category name.
 * @param newCategoryName the new category name.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of any error.
 */
javacall_result javacall_pim_list_rename_category(javacall_handle listHandle,
    javacall_utf16 *oldCategoryName, javacall_utf16 *newCategoryName) {
    return JAVACALL_FAIL;
}

/**
 * Returns the maximum number of categories that this list can have.
 *
 * @param listHandle handle to the list.
 * @retval -1 - indicates there is no limit to the number of categories that
 *              this list can have
 * @retval 0  - indicates no category support
 * @retval >0 - in case a limit exists.
 */
int javacall_pim_list_max_categories(javacall_handle listHandle) {
    return -1;
}

/**
 * Returns the maximum number of categories a list's item can be assigned to.
 *
 * @param listHandle handle to the list.
 * @retval -1 - indicates there is no limit to the number of categories
 *              this item can be assigned to
 * @retval 0  - indicates no category support
 * @retval >0 - in case a limit exists.
 */
int javacall_pim_list_max_categories_per_item(javacall_handle listHandle) {
    return -1;
}

/**
 * Returns a list of categories defined for the given PIM list in
 * ','-separated format (e.g., "Work,HOME,Friends").
 * If there are no categories defined for the PIM list or categories are
 * unsupported for the list, then JAVACALL_FAIL must be returned.
 *
 * @param listHandle handle to the list to get the categories for.
 * @param categoriesName pointer to a buffer in which to store the
 *                       categories list.
 * @param maxCategoriesLen available size of the categoriesName buffer.
  *
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case no categories found or incase of an error.
 */
javacall_result javacall_pim_list_get_categories(javacall_handle listHandle,
    javacall_utf16 *categoriesName, int maxCategoriesLen) {
    return JAVACALL_FAIL;
}

/**
 * Gets all fields that are supported in the given list.
 *
 * In order to identify unused fields, field attributes, field array elements,
 * JAVACALL_PIM_INVALID_ID should be set for each member that is not in use.
 *
 * @param listHandle handle to the list.
 * @param fields pointer to a buffer in which to store the fields.
 * @param maxFields maximum number of fields the field buffer can hold.
 *
 * @retval JAVACALL_OK  on success
 * @retval JAVACALL_FAIL  in case of reaching the last item in the list.
 */
javacall_result javacall_pim_list_get_fields(javacall_handle listHandle,
    javacall_pim_field *fields, int maxFields) {
    return JAVACALL_FAIL;
}

/**
 * Gets all attributes supported by the given list.
 *
 * @param listHandle handle to the list.
 * @param attributes pointer to a buffer in which to store the attributes.
 * @param maxAttributes the maximum number of attributes the buffer can hold.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of reaching the last item in the list.
 */
javacall_result javacall_pim_list_get_attributes(javacall_handle listHandle,
    javacall_pim_field_attribute *attributes, int maxAttributes) {
    return JAVACALL_FAIL;
}

