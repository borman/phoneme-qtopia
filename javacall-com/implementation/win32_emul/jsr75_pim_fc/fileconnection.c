
/*
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
 *
 * win32 implemenation for  FileConnection API
 */
#include "lime.h"
#include "file.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
 *
 *             JSR075's FileConnection API
 *            =============================
 *
 *
 * The following API definitions are required by JSR075.
 * These APIs are not required by standard implementations.
 * These APIs are file related only. Additional APIs required by JSR075
 * which are directory related can be found in javacall_dir.h
 *
 * These extensions include:
 * - javacall_fileconnection_init()
 * - javacall_fileconnection_finalize()
 * - javacall_fileconnection_is_hidden()
 * - javacall_fileconnection_is_readable()
 * - javacall_fileconnection_is_writable()
 * - javacall_fileconnection_set_hidden()
 * - javacall_fileconnection_set_readable()
 * - javacall_fileconnection_set_writable()
 * - javacall_fileconnection_get_last_modified()
 * - javacall_fileconnection_get_illegal_filename_chars()
 * - javacall_fileconnection_is_directory()
 * - javacall_fileconnection_create_dir()
 * - javacall_fileconnection_delete_dir()
 * - javacall_fileconnection_dir_exists()
 * - javacall_fileconnection_rename_dir()
 * - javacall_fileconnection_get_free_size()
 * - javacall_fileconnection_get_total_size()
 * - javacall_fileconnection_get_mounted_roots()
 * - javacall_fileconnection_get_photos_dir()
 * - javacall_fileconnection_get_videos_dir()
 * - javacall_fileconnection_get_graphics_dir()
 * - javacall_fileconnection_get_tones_dir()
 * - javacall_fileconnection_get_music_dir()
 * - javacall_fileconnection_get_recordings_dir()
 * - javacall_fileconnection_get_private_dir()
 * - javacall_fileconnection_get_localized_mounted_roots()
 * - javacall_fileconnection_get_localized_photos_dir()
 * - javacall_fileconnection_get_localized_videos_dir()
 * - javacall_fileconnection_get_localized_graphics_dir()
 * - javacall_fileconnection_get_localized_tones_dir()
 * - javacall_fileconnection_get_localized_music_dir()
 * - javacall_fileconnection_get_localized_recordings_dir()
 * - javacall_fileconnection_get_localized_private_dir()
 * - javacall_fileconnection_get_path_for_root()
 * - javacall_fileconnection_dir_content_size()
 * - javanotify_fileconnection_root_changed()
 */

#include <windows.h>
#include <direct.h>
#include <wchar.h>
//#include <fcntl.h>
//#include <string.h>
//#include <sys/types.h>
#include <sys/stat.h>
#include "javacall_time.h"
#include "javacall_logging.h"
#include "javacall_dir.h"
#include "javacall_file.h"
#include "javacall_fileconnection.h"
#include "javacall_properties.h"

/*
 * This constant is defined in "WinBase.h" when using MS Visual C++ 7, but absent
 * in Visual C++ 6 headers. For successful build with VC6 we need to define it manually.
 */
#ifndef INVALID_FILE_ATTRIBUTES
#define INVALID_FILE_ATTRIBUTES ((DWORD)-1)
#endif

#define MAX_DIRECTORY_NESTING_LEVEL 50

static const javacall_utf16 photos_dir[]     = L"photos/";
static const javacall_utf16 videos_dir[]     = L"";
static const javacall_utf16 graphics_dir[]   = L"";
static const javacall_utf16 tones_dir[]      = L"";
static const javacall_utf16 music_dir[]      = L"";
static const javacall_utf16 recordings_dir[] = L"";
static const javacall_utf16 private_dir[]    = L"private/";

static const javacall_utf16 localized_root_prefix[]    = L"Drive ";
static const javacall_utf16 localized_photos_dir[]     = L"My Photos";
static const javacall_utf16 localized_videos_dir[]     = L"My Videos";
static const javacall_utf16 localized_graphics_dir[]   = L"My Graphics";
static const javacall_utf16 localized_tones_dir[]      = L"My Tones";
static const javacall_utf16 localized_music_dir[]      = L"My Music";
static const javacall_utf16 localized_recordings_dir[] = L"My Recordings";
static const javacall_utf16 localized_private_dir[]    = L"Private";

/**
 * Makes all the required initializations for JSR 75 FileConnection
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully
 *         <tt>JAVACALL_FAIL</tt> if an error occured or feature is not supported
 */
javacall_result javacall_fileconnection_init(void) {
	return JAVACALL_OK;
}

/**
 * Cleans up resources used by fileconnection
 * @return JAVACALL_OK on success, JAVACALL_FAIL otherwise
 */
javacall_result javacall_fileconnection_finalize(void) {
    return JAVACALL_OK;
}

/**
 * Returns the HIDDEN attribute for the specified file or directory
 * If hidden files are not supported, the function should
 * return JAVACALL_FALSE
 *
 * @param fileName      name in UNICODE of file
 * @param fileNameLen   length of file name
 * @param result        returned value: JAVACALL_TRUE if file is hidden
 *                      JAVACALL_FALSE file is not hidden or
 *                      feature is not supported
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully
 *         <tt>JAVACALL_FAIL</tt> if an error occured
 */
javacall_fileconnection_is_hidden(javacall_const_utf16_string fileName,
                                  javacall_bool* /* OUT */ result) {
    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name
    int attrs;
    int fileNameLen = wcslen(fileName);

    if( fileNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_hidden(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, fileName, fileNameLen*sizeof(wchar_t));
    wOsFilename[fileNameLen] = 0;

    attrs = GetFileAttributesW(wOsFilename);
    if (INVALID_FILE_ATTRIBUTES == attrs) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_hidden(), file not found\n");
        return JAVACALL_FAIL;
    }

    *result = ((attrs & FILE_ATTRIBUTE_HIDDEN) == 0) ? JAVACALL_FALSE : JAVACALL_TRUE;
    return JAVACALL_OK;
}

/**
 * Returns the READABLE attribute for the specified file or directory.
 *
 * @param pathName      name of file or directory.
 * @param result        returned value: <tt>JAVACALL_TRUE</tt> if file/dir is readable
 *                      <tt>JAVACALL_FALSE</tt> if file/dir is not readable.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_is_readable(javacall_const_utf16_string pathName,
                                    javacall_bool* /* OUT */ result){

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name
    int pathNameLen = wcslen(pathName);

    if( pathNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_readable(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, pathName, pathNameLen*sizeof(wchar_t));
    wOsFilename[pathNameLen] = 0;

    if(_waccess(wOsFilename, 0) == -1) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_readable(), file is not accessible\n");
        return JAVACALL_FAIL;
    }

    *result = (_waccess(wOsFilename, 4) == 0) ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * Returns the WRITABLE attribute for the specified file or directory.
 *
 * @param pathName      name of file or directory.
 * @param result        returned value: <tt>JAVACALL_TRUE</tt> if file/dir is writable,
 *                      <tt>JAVACALL_FALSE</tt> if file/dir is not writable.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_is_writable(javacall_const_utf16_string pathName,
                                    javacall_bool* /* OUT */ result) {

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int pathNameLen = wcslen(pathName);

    if( pathNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_writable(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, pathName, pathNameLen*sizeof(wchar_t));
    wOsFilename[pathNameLen] = 0;

    if(_waccess(wOsFilename, 0) == -1) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_writable(), file is not accessible\n");
        return JAVACALL_FAIL;
    }

    *result = (_waccess(wOsFilename, 2) == 0) ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * Sets the HIDDEN attribute for the specified file or directory.
 *
 * @param fileName      name of file or directory.
 * @param value         <tt>JAVACALL_TRUE</tt> to set file as hidden,
 *                      <tt>JAVACALL_FALSE</tt> to set file as not hidden.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_set_hidden(javacall_const_utf16_string fileName,
                                   javacall_bool value) {

    int attrs;
    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int fileNameLen = wcslen(fileName);

    if( fileNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_hidden(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, fileName, fileNameLen*sizeof(wchar_t));
    wOsFilename[fileNameLen] = 0;

    attrs = GetFileAttributesW(wOsFilename);

    if(-1 == attrs) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_hidden(), cannot get file attributes\n");
        return JAVACALL_FAIL;
    }

    if(JAVACALL_TRUE == value) {
        attrs = attrs | FILE_ATTRIBUTE_HIDDEN;
    } else {
        attrs = attrs & ~FILE_ATTRIBUTE_HIDDEN;
    }

    if(!SetFileAttributesW(wOsFilename, attrs)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_hidden(), cannot set file attributes\n");
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK;
}

/**
 * Sets the READABLE attribute for the specified file or directory.
 *
 * @param pathName      name of file or directory.
 * @param value         <tt>JAVACALL_TRUE</tt> to set file as readable,
 *                      <tt>JAVACALL_FALSE</tt> to set file as not readable.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_set_readable(javacall_const_utf16_string pathName,
                                     javacall_bool value) {

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int pathNameLen = wcslen(pathName);
    if( pathNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_readable(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, pathName, pathNameLen*sizeof(wchar_t));
    wOsFilename[pathNameLen] = 0;

    if(_waccess(wOsFilename, 0) == -1) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_readable(), file is not accessible\n");
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK; // files are always readable, the call is ignored
}

/**
 * Sets the WRITABLE attribute for the specified file or directory.
 *
 * @param pathName      name of file or directory.
 * @param value         <tt>JAVACALL_TRUE</tt> to set file as writable,
 *                      <tt>JAVACALL_FALSE</tt> to set file as not writable.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_set_writable(javacall_const_utf16_string pathName,
                                     javacall_bool value) {

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int pathNameLen = wcslen(pathName);

    if( pathNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_writable(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, pathName, pathNameLen*sizeof(wchar_t));
    wOsFilename[pathNameLen] = 0;

    if(_wchmod(wOsFilename, (JAVACALL_TRUE == value) ? _S_IWRITE : _S_IREAD) == -1) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_set_writable(), file is not accessible\n");
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK;
}

/**
 * Returns the time when the file or directory was last modified.
 *
 * @param fileName      name of file or directory.
 * @param result        A javacall_int64 value representing the time the file was
 *                      last modified, measured in seconds since the epoch (00:00:00 GMT,
 *                      January 1, 1970).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_last_modified(javacall_const_utf16_string fileName,
                                          javacall_int64* /* OUT */ result) {
    struct _stat buf;
    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int fileNameLen = wcslen(fileName);

    if( fileNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_last_modified(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, fileName, fileNameLen*sizeof(wchar_t));
    wOsFilename[fileNameLen] = 0;

    if (_wstat(wOsFilename, &buf) == -1) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_last_modified(), file is not accessible\n");
        return JAVACALL_FAIL;
    }

    *result = buf.st_mtime;
    return JAVACALL_OK;
}

/**
 * Returns the list of illegal characters in file names. The list must not
 * include '/', but must include native file separator, if it is different
 * from '/' character
 * @param illegalChars returned value: pointer to UNICODE string, allocated
 *                     by the VM, to be filled with the characters that are
 *                     not allowed inside file names.
 * @param illegalCharsLenMaxLen available size, in javacall_utf16 symbols,
 *                              of the buffer provided
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully
 *         <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result javacall_fileconnection_get_illegal_filename_chars(javacall_utf16* /* OUT */ illegalChars,
                                                                   int illegalCharsMaxLen) {
    int i;
    char str[] = "<>:\"\\|";

    if(illegalCharsMaxLen < 7) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_illegal_filename_chars(), insufficient buffer size\n");
        return JAVACALL_FAIL;
    }

    for(i = 0; i <= 6; i++) { // all chars, including trailing zero
        illegalChars[i] = (unsigned short) str[i];
    }

    return JAVACALL_OK;
}


/**
 * Checks if the path exists in the file system storage and if
 * it is a directory.
 *
 * @param pathName name of file or directory.
 * @param result returned value: <tt>JAVACALL_TRUE</tt> if path is a directory,
 *                               <tt>JAVACALL_FALSE</tt> otherwise.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result
javacall_fileconnection_is_directory(javacall_const_utf16_string pathName,
                                     javacall_bool* /* OUT */ result) {
    int attrs;
    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int pathNameLen = wcslen(pathName);

    if( pathNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_directory(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, pathName, pathNameLen*sizeof(wchar_t));
    wOsFilename[pathNameLen] = 0;

    attrs = GetFileAttributesW(wOsFilename);
    if(-1 == attrs) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_is_directory(), cannot get file attributes\n");
        return JAVACALL_FAIL;
    }

    *result = ((attrs & FILE_ATTRIBUTE_DIRECTORY) != 0) ? JAVACALL_TRUE : JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * Creates a directory.
 *
 * @param dirName path name of directory.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> on failure.
 */
javacall_result
javacall_fileconnection_create_dir(javacall_const_utf16_string dirName)
 {

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int dirNameLen = wcslen(dirName);

    if( dirNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_create_dir(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, dirName, dirNameLen*sizeof(wchar_t));
    wOsFilename[dirNameLen] = 0;

    if(0 != _wmkdir(wOsFilename)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_create_dir(), cannot create directory\n");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Deletes an empty directory from the persistent storage.
 * If directory is not empty this function must fail.
 *
 * @param dirName path name of directory.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> on failure.
 */
javacall_result
javacall_fileconnection_delete_dir(javacall_const_utf16_string dirName) {

    wchar_t wOsFilename[JAVACALL_MAX_FILE_NAME_LENGTH]; // max file name

    int dirNameLen = wcslen(dirName);
    if( dirNameLen > JAVACALL_MAX_FILE_NAME_LENGTH ) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_delete_dir(), file name is too long\n");
        return JAVACALL_FAIL;
    }

    memcpy(wOsFilename, dirName, dirNameLen*sizeof(wchar_t));
    wOsFilename[dirNameLen] = 0;

    if(0 != _wrmdir(wOsFilename)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_delete_dir(), cannot delete directory\n");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Check if the directory exists in file system storage.
 *
 * @param pathName name of directory in unicode format.
 * @return <tt>JAVACALL_OK </tt> if it exists and it is a regular directory,
 *         <tt>JAVACALL_FAIL</tt> if directory does not exist, or any error
 *         has occured.
 */
javacall_result
javacall_fileconnection_dir_exists(javacall_const_utf16_string pathName){

    javacall_bool res;
    int pathNameLen = wcslen(pathName);
    if(JAVACALL_OK != javacall_fileconnection_is_directory(pathName, &res)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_dir_exists(), cannot access directory\n");
        return JAVACALL_FAIL;
    }

    if(JAVACALL_FALSE == res) {
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Renames the specified directory.
 *
 * @param oldDirName current name of directory.
 * @param newDirName new name of directory.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_rename_dir(javacall_const_utf16_string oldDirName,
                                   javacall_const_utf16_string newDirName){

    if(JAVACALL_OK != javacall_file_rename(oldDirName, wcslen(oldDirName), newDirName, wcslen(newDirName))) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_rename_dir(), cannot rename directory\n");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Determines the free memory in bytes that is available on the
 * file system the file or directory resides on.
 *
 * @param pathName path name of any file within the file system.
 * @param result returned value: on success, size of available storage space (bytes).
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_free_size(javacall_const_utf16_string pathName,
                                      javacall_int64* /* OUT */ result) {
    struct _diskfree_t df;

    if(0 != _getdiskfree(pathName[0] - (pathName[0] > 'Z' ? 'a' : 'A') + 1, &df)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_free_size(), cannot get free space\n");
        return JAVACALL_FAIL;
    }

    *result = (javacall_int64)(df.avail_clusters) * df.sectors_per_cluster * df.bytes_per_sector;
    return JAVACALL_OK;
}

/**
 * Determines the total size in bytes of the file system the file
 * or directory resides on.
 *
 * @param pathName file name of any file within the file system.
 * @param pathNameLen length of path name.
 * @param result returned value: on success, total size of storage space (bytes).
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_total_size(javacall_const_utf16_string pathName,
                                       javacall_int64* /* OUT */ result) {

    struct _diskfree_t df;

    if(0 != _getdiskfree(pathName[0] - (pathName[0] > 'Z' ? 'a' : 'A') + 1, &df)) {
	 javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_total_size(), cannot get total space\n");
        return JAVACALL_FAIL;
    }

    *result = (javacall_int64)(df.total_clusters) * df.sectors_per_cluster * df.bytes_per_sector;
    return JAVACALL_OK;
}

/**
 * Returns the mounted root file systems (UNICODE format). Each root must end
 * with '/' character
 * @param roots buffer to store the UNICODE string containing
 *              currently mounted roots separated by '\n' character
 * @param rootsLen available buffer size (maximum number of javacall_utf16
 *                 symbols to be stored)
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result javacall_fileconnection_get_mounted_roots(javacall_utf16* /* OUT */ roots,
                                                          int rootsLen) {
    static LimeFunction *f = NULL;

    unsigned char * data;
    int dataLength, len=0, i;

    if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.midp", "FileConnEventGenPanel", "getMountedRoots");
    }

    f->call(f, &data, &dataLength);

    if (data == NULL) {
    } else {
		if(dataLength > 0 && dataLength > rootsLen - 5 || rootsLen < 4) {
	              javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_mounted_roots(), buffer is too small\n");
			return JAVACALL_FAIL;
		}
		memset(roots,0,rootsLen);

		/* initialize roots and look for / tokens, append \n after each token. */
		for (i=0; i < dataLength; i++) {
			roots[len++] = data[i*2];
			if (data[i*2] == '/') {
				roots[len++] = '\n';
			}
		}
		roots[len++] = '/'; // append / after the last token
    }
    return JAVACALL_OK;
}

/**
 * Internal function
 * Returns the next root of mounted root file systems (UNICODE format).
 * @param root buffer to store the UNICODE string containing
 *              currently mounted roots separated by '\n' character
 * @param rootLen available buffer size (maximum number of javacall_utf16
 *                 symbols to be stored)
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result get_first_root(javacall_utf16* /* OUT */ root, int rootLen) {
   javacall_utf16 mroots[JAVACALL_MAX_ROOTS_LIST_LENGTH];
   int i = 0;
   memset(root,0,rootLen);

    // get mounted roots
    if (JAVACALL_OK != javacall_fileconnection_get_mounted_roots(mroots, JAVACALL_MAX_ROOTS_LIST_LENGTH))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:get_next_root(), can't get roots");
    }
    i = 0;
    if(mroots != NULL){
	while(mroots[i] != '\n' && i < rootLen){
            ++i;
	}
       memcpy(root, mroots, i*2);
    }
    else{
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:get_next_root(), mounted roots is null");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Internal function
 * Returns the path to property and other images storage, using '/' as
 * file separator. The path must end with this separator as well
 * @param dir buffer to store the UNICODE string containing path to
 *            directory with photos
 * @param dirLen available buffer size of dir (maximum number of javacall_utf16
 *               symbols to be stored)
 * @param propDir directory name of the property in UNICODE
 * @param propDirLen available buffer size of propDir (maximum number of
 *               javacall_utf16 symbols to be stored)
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result get_property_dir(javacall_utf16* /* OUT */ dir, int dirLen,
								javacall_utf16* propDir, int propDirLen ) {
    javacall_utf16 root[JAVACALL_MAX_FILE_NAME_LENGTH];

    //get root name
    if (JAVACALL_OK != get_first_root(root, JAVACALL_MAX_FILE_NAME_LENGTH))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:get_property_dir(), can't get root");
        return JAVACALL_FAIL;
    }

    // create path
    if (dirLen < (wcslen(root)*2 + sizeof(propDir))/sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: get_property_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(dir, root, wcslen(root)*2);
    memcpy(dir+wcslen(root), propDir, propDirLen);
    return JAVACALL_OK;
}

/**
 * Returns the path to photo and other images storage, using '/' as
 * file separator. The path must end with this separator as well.
 *
 * @param dir buffer to store the string containing path to
 *            directory with photos.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_photos_dir(javacall_utf16_string /* OUT */ dir,
                                       int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, photos_dir, sizeof(photos_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_photos_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to video clips storage, using '/' as file separator.
 * The path must end with this separator as well.
 *
 * @param dir buffer to store the string containing path to
 *            directory with video clips.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_videos_dir(javacall_utf16_string /* OUT */ dir,
                                       int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, videos_dir, sizeof(videos_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_videos_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to clip art graphics storage, using '/' as file separator.
 * The path must end with this separator as well.
 *
 * @param dir buffer to store the string containing path to
 *            directory with graphics.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_graphics_dir(javacall_utf16_string /* OUT */ dir,
                                         int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, graphics_dir, sizeof(graphics_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_graphics_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to ring tones and other related audio files storage,
 * using '/' as file separator. The path must end with this separator as well.
 *
 * @param dir buffer to store the string containing path to
 *            directory with ring tones.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_tones_dir(javacall_utf16_string /* OUT */ dir,
                                      int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, tones_dir, sizeof(tones_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_tones_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to music files storage, using '/' as file separator.
 * The path must end with this separator as well
 *
 * @param dir buffer to store the string containing path to
 *            directory with music.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_music_dir(javacall_utf16_string /* OUT */ dir,
                                      int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, music_dir, sizeof(music_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_music_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to voice recordings storage, using '/' as file separator.
 * The path must end with this separator as well.
 *
 * @param dir buffer to store the string containing path to
 *            directory with voice recordings.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_recordings_dir(javacall_utf16_string /* OUT */ dir,
                                           int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, recordings_dir, sizeof(recordings_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_recordings_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the path to directory, that is used to store private directories
 * for all applications (accessed via "fileconn.dir.private" system property).
 * The returned path must use '/' as file separator and have this separator at
 * the end.
 *
 * @param dir buffer to store the string containing path to
 *            location of private directories for all applications.
 * @param dirLen available buffer size (maximum number of
 *               characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_private_dir(javacall_utf16_string /* OUT */ dir,
                                        int dirLen, javacall_bool fromCache) {
    if (JAVACALL_OK != get_property_dir(dir, dirLen, private_dir, sizeof(private_dir)))
    {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Err:javacall_fileconnection_get_private_dir(), can't get dir");
        return JAVACALL_FAIL;
    }
    return JAVACALL_OK;
}

/**
 * Returns the localized names for mounted root file systems.
 *
 * @param roots buffer to store the string containing localized names
 *              of currently mounted roots separated by ';' character.
 * @param rootsLen available buffer size (maximum number of
 *                 characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_mounted_roots(javacall_utf16_string /* OUT */ names,
                                                    int namesLen,
                                                    javacall_bool fromCache) {
	unsigned short filesystemStr[]={'\\','f','i','l','e','s','y','s','t','e','m','\\',0};
    int rootPathLen = JAVACALL_MAX_FILE_NAME_LENGTH;
    javacall_utf16 rootPath[JAVACALL_MAX_FILE_NAME_LENGTH];
	char *storage_root;

	javacall_utf16 storage_root_w[JAVACALL_MAX_FILE_NAME_LENGTH*sizeof(javacall_utf16)];
	WIN32_FIND_DATAW dir_data;
	int nextExists = 1;
	HANDLE listHandle;
	javacall_bool includeSubdirs = JAVACALL_TRUE;
	int index = 0;
	int len = 0;

    javacall_get_property("system.storage_root",
                          JAVACALL_APPLICATION_PROPERTY,
                          &storage_root); //device name

    if(JAVACALL_MAX_FILE_NAME_LENGTH < 1 + wcslen(filesystemStr) + strlen(storage_root)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_path_for_root(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

	/* get current working directory, append appdb\device name\filesystem */
    wcscpy(rootPath, char_to_unicode(storage_root));
	wcscat(rootPath, filesystemStr);
	rootPathLen = wcslen(rootPath);

    rootPath[rootPathLen++] = '*';
    rootPath[rootPathLen] = 0;

    listHandle = FindFirstFileW(rootPath, &dir_data);
    if (INVALID_HANDLE_VALUE == listHandle) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_mounted_roots(), cannot open directory\n");
        return JAVACALL_FAIL;
    }

	/* find list of files located under appdb\device name\filesystem */
    while (nextExists) {
        if ((dir_data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0) {
            // found subdirectory
            if (JAVACALL_TRUE == includeSubdirs) {
                // must count subdirectory sizes
                int dirNameLen = wcslen(dir_data.cFileName);
                if (wcscmp(dir_data.cFileName, L".") && wcscmp(dir_data.cFileName, L"..")) {
                    // the subdirectory is not "." or ".."
					if(index > 0 &&
					   index > namesLen - 5 - (int)(sizeof(localized_root_prefix) / sizeof(javacall_utf16) - 1) ||
					   namesLen < 4 + (sizeof(localized_root_prefix) / sizeof(javacall_utf16) - 1)) {
					       javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_mounted_roots(), buffer is too small\n");
						return JAVACALL_FAIL;
					}
					if(index > 0) {
						names[len++] = ';';
					} else {
						memset(names,0,namesLen);
					}

					wcscat(names, dir_data.cFileName);
					len = wcslen(names);
					// roots must be in URL format, so file separator is used
					names[len++] = javacall_get_file_separator();
					index ++;
                }
            }
        }
        nextExists = FindNextFileW(listHandle, &dir_data);
	}

    return JAVACALL_OK;
}

/**
 * Returns localized name for directory with photos and other images, corresponding to
 * the path returned by <tt>System.getProperty("fileconn.dir.photos")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_photos_dir(javacall_utf16_string /* OUT */ name,
                                                 int nameLen,
                                                 javacall_bool fromCache) {
    if (nameLen < sizeof(localized_photos_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_photos_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_photos_dir, sizeof(localized_photos_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized name for video clips location, corresponding to
 * the path returned by <tt>System.getProperty("fileconn.dir.videos")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_videos_dir(javacall_utf16_string /* OUT */ name,
                                                 int nameLen,
                                                 javacall_bool fromCache) {
    if (nameLen < sizeof(localized_videos_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_videos_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_videos_dir, sizeof(localized_videos_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized name for directory containing clip art graphics, corresponding
 * to the path returned by <tt>System.getProperty("fileconn.dir.graphics")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_graphics_dir(javacall_utf16_string /* OUT */ name,
                                                   int nameLen,
                                                   javacall_bool fromCache) {
    if (nameLen < sizeof(localized_graphics_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_graphics_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_graphics_dir, sizeof(localized_graphics_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized name for directory with ring tones and other related audio files,
 * corresponding to the path returned by
 * <tt>System.getProperty("fileconn.dir.tones")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_tones_dir(javacall_utf16_string /* OUT */ name,
                                                int nameLen,
                                                javacall_bool fromCache) {
    if (nameLen < sizeof(localized_tones_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_tones_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_tones_dir, sizeof(localized_tones_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized name for music files storage, corresponding to
 * the path returned by <tt>System.getProperty("fileconn.dir.music")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_music_dir(javacall_utf16_string /* OUT */ name,
                                                int nameLen,
                                                javacall_bool fromCache) {
    if (nameLen < sizeof(localized_music_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_music_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_music_dir, sizeof(localized_music_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized name for voice recordings storage, corresponding to
 * the path returned by <tt>System.getProperty("fileconn.dir.recordings")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_recordings_dir(javacall_utf16_string /* OUT */ name,
                                                     int nameLen,
                                                     javacall_bool fromCache) {
    if (nameLen < sizeof(localized_recordings_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_recordings_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_recordings_dir, sizeof(localized_recordings_dir));

    return JAVACALL_OK;
}

/**
 * Returns localized private directory name corresponding to the path returned by
 * <tt>System.getProperty("fileconn.dir.private")</tt>.
 *
 * @param name buffer to store the string containing localized name.
 * @param nameLen available buffer size (maximum number of
 *                characters to be stored).
 * @param fromCache indicates whether the returned value should be taken from
 *                  internal cache (this parameter can be ignored if properties
 *                  caching is not supported by underlying implementation).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_localized_private_dir(javacall_utf16_string /* OUT */ name,
                                                  int nameLen,
                                                  javacall_bool fromCache) {
    if (nameLen < sizeof(localized_private_dir) / sizeof(javacall_utf16)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_localized_private_dir(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    memcpy(name, localized_private_dir, sizeof(localized_private_dir));

    return JAVACALL_OK;
}


/**
 * Returns OS-specific path for the specified file system root.
 *
 * @param rootName root name.
 * @param pathName buffer to store the string containing
 *                 the system-dependent path to access the specified
 *                 root.
 * @param pathNameLen available buffer size (maximum number of
 *                    characters to be stored).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_path_for_root(javacall_const_utf16_string rootName,
                                          javacall_utf16_string /* OUT */ pathName,
                                          int pathNameLen) {

	unsigned short filesystemStr[]={'\\','f','i','l','e','s','y','s','t','e','m','\\',0};
	char *storage_root;
	int len=0;

    //Assume system.storage_root is full path (Taken care by dir.c)
    javacall_get_property("system.storage_root",
                          JAVACALL_APPLICATION_PROPERTY,
                          &storage_root);

    memset(pathName,0,pathNameLen);

    if(pathNameLen < strlen(rootName) + wcslen(filesystemStr) + strlen(storage_root)) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_get_path_for_root(), buffer is too small\n");
        return JAVACALL_FAIL;
    }

    wcscpy(pathName,char_to_unicode(storage_root));
	wcscat(pathName, filesystemStr);

	wcscat(pathName, rootName);
    return JAVACALL_OK;
}


/**
 * Get size in bytes of all files and possibly subdirectories contained
 * in the specified dir.
 *
 * @param pathName          path name of directory.
 * @param includeSubdirs    if <tt>JAVACALL_TRUE</tt>, include subdirectories size too;
 *                          if <tt>JAVACALL_FALSE</tt>, do not include subdirectories.
 * @param result            returned value: size in bytes of all files contained in
 *                          the specified directory and possibly its subdirectories.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_dir_content_size(javacall_const_utf16_string pathName,
                                         javacall_bool includeSubdirs,
                                         javacall_int64* /* OUT */ result) {
    wchar_t subSearch[JAVACALL_MAX_FILE_NAME_LENGTH + 3];
    WIN32_FIND_DATAW dir_data;
    javacall_int64 contentSize = 0;
    HANDLE listHandle[MAX_DIRECTORY_NESTING_LEVEL];
    int pathLen[MAX_DIRECTORY_NESTING_LEVEL];
    int nestLevel = 0;
    int nextExists = 1;

    int pathNameLen = wcslen(pathName);

    memcpy(subSearch, pathName, pathNameLen * sizeof(javacall_utf16));
    subSearch[pathNameLen++] = javacall_get_file_separator();
    subSearch[pathNameLen++] = '*';
    subSearch[pathNameLen] = 0;

    listHandle[0] = FindFirstFileW(subSearch, &dir_data);
    pathLen[0] = pathNameLen - 1;
    if (INVALID_HANDLE_VALUE == listHandle[0]) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_dir_content_size(), cannot open directory\n");
        return JAVACALL_FAIL;
    }

    for ( ; ; ) {
        while (nextExists) {
            if ((dir_data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0) {
                // found subdirectory
                if (JAVACALL_TRUE == includeSubdirs) {
                    // must count subdirectory sizes
                    int dirNameLen = wcslen(dir_data.cFileName);
                    if (wcscmp(dir_data.cFileName, L".") && wcscmp(dir_data.cFileName, L"..")) {
                        // the subdirectory is not "." or ".."
                        if (nestLevel >= MAX_DIRECTORY_NESTING_LEVEL - 1) {
                            // nesting level overflow
                            while (nestLevel >= 0) {
                                FindClose(listHandle[nestLevel--]);
                            }
			       javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_dir_content_size(), directory nesting overflow\n");
                            return JAVACALL_FAIL;
                        }
                        subSearch[pathLen[nestLevel]] = 0;
                        wcscat(subSearch, dir_data.cFileName);
                        pathLen[nestLevel + 1] = pathLen[nestLevel] + dirNameLen;
                        subSearch[pathLen[++nestLevel]++] = javacall_get_file_separator();
                        subSearch[pathLen[nestLevel]] = '*';
                        subSearch[pathLen[nestLevel] + 1] = 0;
                        listHandle[nestLevel] = FindFirstFileW(subSearch, &dir_data);
                        if (INVALID_HANDLE_VALUE == listHandle[nestLevel]) {
                            while (--nestLevel >= 0) {
                                FindClose(listHandle[nestLevel]);
                            }
			       javautil_debug_print (JAVACALL_LOG_ERROR, "fileconnection", "Error: javacall_fileconnection_dir_content_size(), cannot open subdirectory\n");
                            return JAVACALL_FAIL;
                        }
                        nextExists = 1;
                        continue;
                    }
                }
            } else {
                contentSize += ((javacall_int64)(dir_data.nFileSizeHigh) << 32) + dir_data.nFileSizeLow;
            }
            nextExists = FindNextFileW(listHandle[nestLevel], &dir_data);
        }
        FindClose(listHandle[nestLevel]);
        if (nestLevel > 0) {
            nextExists = FindNextFileW(listHandle[--nestLevel], &dir_data);
        } else {
            break;
        }
    }

    *result = contentSize;
    return JAVACALL_OK;
}

#ifdef __cplusplus
}
#endif
