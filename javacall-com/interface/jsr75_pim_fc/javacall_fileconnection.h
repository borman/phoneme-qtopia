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

#ifndef __JAVACALL_JSR75_FC_H_
#define __JAVACALL_JSR75_FC_H_

/**
 * @file javacall_fileconnection.h
 * @ingroup JSR75
 * @brief Javacall interfaces for JSR-75 FileConnection
 */

#include "javacall_defs.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @defgroup JSR75 JSR75 File Connection API
 *
 * The following API definitions are required by JSR-75.
 * These APIs are not required by standard JTWI implementations.
 *
 * @{
 */

/*
 * 
 *             JSR075's FileConnection API
 *            =============================
 * 
 * 
 * The following API definitions are required by JSR-75.
 * This API is not required by standard JTWI implementations.
 * 
 * Mandatory API for JSR-75:
 * - javacall_fileconnection_init()
 * - javacall_fileconnection_finalize()
 * - javacall_fileconnection_set_hidden()
 * - javacall_fileconnection_set_readable()
 * - javacall_fileconnection_set_writable()
 * - javacall_fileconnection_get_illegal_filename_chars()
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
 * - javacall_fileconnection_is_hidden()
 * - javacall_fileconnection_is_readable()
 * - javacall_fileconnection_is_writable()
 * - javacall_fileconnection_get_last_modified()
 * - javacall_fileconnection_is_directory()
 * - javacall_fileconnection_create_dir()
 * - javacall_fileconnection_delete_dir()
 * - javacall_fileconnection_dir_exists()
 * - javacall_fileconnection_rename_dir()
 * - javacall_fileconnection_dir_content_size()
 *
 * Functions specific for CDC-based implementations:
 * - javacall_fileconnection_cache_properties()
 * - javacall_fileconnection_activate_notifications()
 * - javacall_fileconnection_deactivate_notifications()
 */

/** 
 * @defgroup jsrMandatoryFileConnection Mandatory FC API
 * @ingroup JSR75
 *
 * @{
 */ 

/**
 * Makes all the required initializations for JSR-75 FileConnection.
 *
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured or feature is not supported.
 */
javacall_result javacall_fileconnection_init(void);

/**
 * Cleans up resources used by FileConnection.
 *
 * @return <tt>JAVACALL_OK</tt> on success, <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result javacall_fileconnection_finalize(void);

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
                                   javacall_bool value);

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
                                     javacall_bool value);

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
                                     javacall_bool value);    

/**
 * Returns the list of illegal characters in file names. The list must not
 * include '/', but must include native file separator, if it is different
 * from '/' character.
 *
 * @param illegalChars returned value: pointer to string, allocated
 *                     by the VM, to be filled with the characters that are
 *                     not allowed inside file names.
 * @param illegalCharsMaxLen available size, in characters,
 *                              of the buffer provided.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_illegal_filename_chars(javacall_utf16_string /* OUT */ illegalChars,
                                                   int illegalCharsMaxLen);

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
                                      javacall_int64* /* OUT */ result);

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
                                       javacall_int64* /* OUT */ result);

/** 
 * Returns the mounted root file systems. Each root must end
 * with '/' character.
 *
 * @param roots buffer to store the string containing
 *              currently mounted roots separated by '\n' character.
 * @param rootsLen available buffer size (maximum number of
 *                 characters to be stored).
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_get_mounted_roots(javacall_utf16_string /* OUT */ roots,
                                          int rootsLen);

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
                                       int dirLen, javacall_bool fromCache);

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
                                       int dirLen, javacall_bool fromCache);

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
                                         int dirLen, javacall_bool fromCache);

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
                                      int dirLen, javacall_bool fromCache);

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
                                      int dirLen, javacall_bool fromCache);

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
                                           int dirLen, javacall_bool fromCache);

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
                                        int dirLen, javacall_bool fromCache);

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
                                                    javacall_bool fromCache);

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
                                                 javacall_bool fromCache);

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
                                                 javacall_bool fromCache);

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
                                                   javacall_bool fromCache);

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
                                                javacall_bool fromCache);

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
                                                javacall_bool fromCache);

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
                                                     javacall_bool fromCache);

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
                                                  javacall_bool fromCache);

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
                                          int pathNameLen);

/**
 * Returns the HIDDEN attribute for the specified file or directory.
 * If hidden files are not supported, the function should 
 * return <tt>JAVACALL_FALSE</tt>.
 *
 * @param fileName      name of file or directory.
 * @param result        returned value: <tt>JAVACALL_TRUE</tt> if file is hidden,
 *                      <tt>JAVACALL_FALSE</tt> if file is not hidden or 
 *                      feature is not supported.
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully,
 *         <tt>JAVACALL_FAIL</tt> if an error occured.
 */
javacall_result  
javacall_fileconnection_is_hidden(javacall_const_utf16_string fileName,
                                  javacall_bool* /* OUT */ result);

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
                                    javacall_bool* /* OUT */ result);

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
                                    javacall_bool* /* OUT */ result);

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
                                          javacall_int64* /* OUT */ result);

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
                                     javacall_bool* /* OUT */ result);

/**
 * Creates a directory.
 *
 * @param dirName path name of directory.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> on failure.
 */
javacall_result 
javacall_fileconnection_create_dir(javacall_const_utf16_string dirName);

/**
 * Deletes an empty directory from the persistent storage.
 * If directory is not empty this function must fail.
 *
 * @param dirName path name of directory.
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> on failure.
 */
javacall_result 
javacall_fileconnection_delete_dir(javacall_const_utf16_string dirName);

/**
 * Check if the directory exists in file system storage.
 *
 * @param pathName name of directory in unicode format.
 * @return <tt>JAVACALL_OK </tt> if it exists and it is a regular directory, 
 *         <tt>JAVACALL_FAIL</tt> if directory does not exist, or any error
 *         has occured.
 */
javacall_result 
javacall_fileconnection_dir_exists(javacall_const_utf16_string pathName);

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
                                   javacall_const_utf16_string newDirName);    

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
                                         javacall_int64* /* OUT */ result);

/** @} */


/** 
 * @defgroup jsrCDCFileConnection CDC-specific FC API
 * @ingroup JSR75
 *
 * @{
 */ 

/** 
 * Tells the implementation to cache values of all FileConnection
 * dynamic properties (this call can be ignored if property caching is not
 * supported by the implementation).
 * @note This function is only used by CDC-based Java stack.
 *
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result
javacall_fileconnection_cache_properties(void);

/**
 * Activates notifications for file system roots being mounted or unmounted.
 * After calling this function, javanotify_fileconnection_root_changed() must
 * be called by the platform every time the event occurs.
 * @note This function is only used by CDC-based Java stack.
 *
 * @return <tt>JAVACALL_OK</tt> on success, <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result javacall_fileconnection_activate_notifications(void);

/**
 * Deactivates notifications for file system roots being mounted or unmounted.
 * After calling this function, javanotify_fileconnection_root_changed() must
 * not be called by the platform.
 * @note This function is only used by CDC-based Java stack.
 *
 * @return <tt>JAVACALL_OK</tt> on success, <tt>JAVACALL_FAIL</tt> otherwise.
 */
javacall_result javacall_fileconnection_deactivate_notifications(void);


/** @} */

/** @} */

#ifdef __cplusplus
}
#endif

#endif 


