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

#include "lime.h"
#include "javacall_multimedia.h"
#include "javacall_multimedia_advanced.h"
#include "javacall_fileconnection.h"
#include "javanotify_multimedia_advanced.h"
#include "multimedia_camera_state.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <windows.h>

#define LIME_MMAPI_PACKAGE "com.sun.mmedia"
#define LIME_MMAPI_CLASS "JavaCallBridge"

javacall_result audio_pause( javacall_handle handle );
javacall_result audio_resume( javacall_handle handle );

void notify( camera_state* cs, BOOL ok )
{
    javacall_int64 id = cs->ah->isolateId;
    id = ( id << 32 ) + cs->ah->playerId;

    javanotify_on_amms_notification( 
        ( ok ? JAVACALL_EVENT_AMMS_SNAP_SHOOTING_STOPPED
             : JAVACALL_EVENT_AMMS_SNAP_STORAGE_ERROR ),
        id, cs->snapLastNameForJava );


}

void freeze_viewfinder( camera_state* cs, BOOL freeze )
{
    if( freeze )
    {
        audio_pause( (javacall_handle)( cs->ah ) );
    }
    else
    {
        audio_resume( (javacall_handle)( cs->ah ) );
    }
}

BOOL snapshot( camera_state* cs )
{
    static LimeFunction* snap_func = NULL;

    static javacall_utf16_string type = L"encoding=jpeg";

    long  dataLength;
    long  fileLength;
    char* data;
    FILE* f;

    if( NULL == snap_func )
    {
        snap_func = NewLimeFunction( LIME_MMAPI_PACKAGE, LIME_MMAPI_CLASS, "snapshot" );
    }
    
    snap_func->call( snap_func, &data, &dataLength, cs->ah->hWnd, type, wcslen( type ) );

    _snwprintf( cs->snapLastName, MAX_PATH, L"%s%s%04d%s",
        ( cs->snapDirectory ? cs->snapDirectory : L"" ),
        ( cs->snapPrefix ? cs->snapPrefix : L"" ),
        cs->snapIndex,
        ( cs->snapSuffix ? cs->snapSuffix : L"" ) );

    _snwprintf( cs->snapLastNameForJava, MAX_PATH, L"%s%04d%s",
        ( cs->snapPrefix ? cs->snapPrefix : L"" ),
        cs->snapIndex,
        ( cs->snapSuffix ? cs->snapSuffix : L"" ) );

    f = _wfopen( cs->snapLastName, L"r" );

    if( NULL != f ) // File with this name already exists
    {
        fclose( f );
        return FALSE;
    }
    else
    {
        f = _wfopen( cs->snapLastName, L"wb" );
        if( NULL == f ) // Cannot create file
        {
            return FALSE;
        }
        else
        {
            fileLength = fwrite( data, 1, dataLength, f );
            fclose( f );

            if( fileLength != dataLength ) // Write failed
            {
                return FALSE;
            }
        }
    }
    
    cs->snapIndex++;

    return TRUE;
}

static DWORD WINAPI snapshot_thread( LPVOID param )
{
    camera_state* cs = (camera_state*)param;
    BOOL snap_ok;

    if( JAVACALL_AMMS_SNAPSHOT_FREEZE == cs->snapShotsLeft )
    {
        freeze_viewfinder( cs, TRUE );
        snap_ok = snapshot( cs );
        if( snap_ok ) Sleep( 3000 );
        notify( cs, snap_ok );
        freeze_viewfinder( cs, FALSE );
    }
    else if( JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM == cs->snapShotsLeft )
    {
        freeze_viewfinder( cs, TRUE );

        while( !cs->snapStop ) Sleep( 0 );

        if( cs->snapConfirm )
        {
            notify( cs, snapshot( cs ) );
        }

        freeze_viewfinder( cs, FALSE );
    }
    else
    {
        snap_ok = TRUE;
        while( !cs->snapStop && 0 != cs->snapShotsLeft )
        {
            snap_ok &= snapshot( cs );
            if( !snap_ok ) break;

            if( JAVACALL_AMMS_SNAPSHOT_MAX_VALUE != cs->snapShotsLeft 
                && 0 != cs->snapShotsLeft )
            {
                cs->snapShotsLeft--;
            }

            if( !cs->snapStop && 0 != cs->snapShotsLeft ) Sleep( 500 );
        }
        notify( cs, snap_ok );
    }

    cs->snapThread = NULL;
    return 0;
}

/**
 * Tests if snapshot control is available for the object
 * referred by the given native handle.
 * 
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_TRUE if snapshot control is available
 * @retval JAVACALL_FALSE if snapshot control is not supported
 */
javacall_bool javacall_amms_snapshot_control_is_supported(
    javacall_handle hNative) {
    return JAVACALL_TRUE;
}

/**
 * The function corresponding to 
 * void SnapshotControl.setDirectory(java.lang.String directory)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the file directory where the images will be stored.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param dir  storage directory to set (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a dir is \a NULL or the directory does not exist
 */
javacall_result javacall_amms_snapshot_control_set_directory(
    javacall_handle hNative, javacall_const_utf16_string dir) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );
    int i;
    int attrs;

    if( NULL == dir ) {
        return JAVACALL_INVALID_ARGUMENT;
    }


    if( NULL != cs->snapDirectory ) {
        free( cs->snapDirectory );
    }

    cs->snapDirectory = (javacall_utf16_string)malloc( MAX_PATH * sizeof(javacall_utf16) );

    javacall_fileconnection_get_path_for_root( ( ( L'/' == dir[ 0 ] ) ? ( dir + 1 ) : dir ),
                                               cs->snapDirectory, 
                                               MAX_PATH );

    i = 0;
    while( cs->snapDirectory[ i ] )
    {
        if( L'/' == cs->snapDirectory[ i ] ) cs->snapDirectory[ i ] = L'\\';
        i++;
    }

    attrs = GetFileAttributesW(cs->snapDirectory);
    if (-1 == attrs || (attrs & FILE_ATTRIBUTE_DIRECTORY) == 0) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getDirectory()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Gets the storage directory.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param dir  pointer to the buffer to receive the storage directory
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a dir is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_directory(
    javacall_handle hNative, /*OUT*/javacall_utf16_string dir, long bufLength)
{
    return JAVACALL_NOT_IMPLEMENTED; // deprecated
}

/**
 * The function corresponding to 
 * void SnapshotControl.setFilePrefix(java.lang.String prefix)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the filename prefix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param prefix  prefix for the files that will be created
 *     (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a prefix is \a NULL or refers to a value that cannot be set
 */
javacall_result javacall_amms_snapshot_control_set_file_prefix(
    javacall_handle hNative, javacall_const_utf16_string prefix) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == prefix) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    if (cs->snapPrefix) {
        free(cs->snapPrefix);
    }
    cs->snapPrefix = (javacall_utf16_string)malloc(
        (wcslen(prefix) + 1) * sizeof(javacall_utf16));
    wcscpy(cs->snapPrefix, prefix);
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getFilePrefix()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current filename prefix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param prefix  pointer to the buffer to receive the current filename prefix
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a prefix is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_file_prefix(
    javacall_handle hNative, /*OUT*/javacall_utf16_string prefix,
    long bufLength)
{
    return JAVACALL_NOT_IMPLEMENTED; // deprecated
}

/**
 * The function corresponding to 
 * void SnapshotControl.setFileSuffix(java.lang.String suffix)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Sets the filename suffix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param suffix  sufix for the files that will be created
 *     (null-terminated UTF-16 string)
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a suffix is \a NULL or refers to a value that cannot be set
 */
javacall_result javacall_amms_snapshot_control_set_file_suffix(
    javacall_handle hNative, javacall_const_utf16_string suffix) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if (NULL == suffix) {
        return JAVACALL_INVALID_ARGUMENT;
    }

    if (cs->snapSuffix) {
        free(cs->snapSuffix);
    }
    cs->snapSuffix = (javacall_utf16_string)malloc(
        (wcslen(suffix) + 1) * sizeof(javacall_utf16));
    wcscpy(cs->snapSuffix, suffix);
    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * java.lang.String SnapshotControl.getFileSuffix()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Returns the current filename suffix.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param suffix  pointer to the buffer to receive the current filename suffix
 *     (result will be null-terminated UTF-16 string)
 * @param bufLength  number of characters the buffer can hold including
 *     null-terminator
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a suffix is \a NULL
 *        - insufficient buffer size
 */
javacall_result javacall_amms_snapshot_control_get_file_suffix(
    javacall_handle hNative, /*OUT*/javacall_utf16_string suffix,
    long bufLength)
{
    return JAVACALL_NOT_IMPLEMENTED; // deprecated
}

/**
 * The function corresponding to 
 * void SnapshotControl.start(int)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Starts burst shooting.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param maxShots  maximum number of shots to take or one of the constants
 *     \a JAVACALL_AMMS_SNAPSHOT_FREEZE,
 *     \a JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM,
 *     \a JAVACALL_AMMS_SNAPSHOT_MAX_VALUE
 *
 * @retval JAVACALL_OK if operation is successful
 * @retval JAVACALL_FAIL if prefix and suffix have not been set
 * @retval JAVACALL_INVALID_ARGUMENT indicates one of the following errors:
 *        - \a maxShots is less than 1 and is not one of the constants
 *          \a JAVACALL_AMMS_SNAPSHOT_FREEZE and
 *          \a JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM
 */
javacall_result javacall_amms_snapshot_control_start(
    javacall_handle hNative, long maxShots) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    int   wait_result;
    DWORD timeout_ms;

    // IMPL_NOTE: it's not clear from specification,
    //            what shall we do if second start arrives before we've stopped.
    //            for now, we'll force stop if maxShots was 'infinite',
    //            or just wait until it stops by itself.
    //            if waiting for unfreeze, we'll do unfreeze( false ).

    if( NULL != cs->snapThread )
    {
        if( JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM == cs->snapShotsLeft )
        {
            if( JAVACALL_OK != javacall_amms_snapshot_control_unfreeze( 
                                    hNative, JAVACALL_FALSE ) )
            {
                return JAVACALL_FAIL; // ???
            }
        }
        else
        {
            if( JAVACALL_AMMS_SNAPSHOT_MAX_VALUE == cs->snapShotsLeft )
            {
                cs->snapStop = TRUE;
                timeout_ms   = 5000;
            }
            else
            {
                timeout_ms   = 5000 + 500 * cs->snapShotsLeft;
            }

            wait_result = WaitForSingleObject( cs->snapThread, timeout_ms );
            assert( WAIT_OBJECT_0 == wait_result );

            if( WAIT_OBJECT_0 != wait_result )
            {
                TerminateThread( cs->snapThread, 0 );
                cs->snapThread = NULL;
            }
        }
    }

    cs->snapShotsLeft    = maxShots;
    cs->snapStop         = FALSE;
    cs->snapThread       = CreateThread( NULL, 0, snapshot_thread, cs, 0, NULL );

    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * void SnapshotControl.stop()
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Stops burst shooting.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 *
 * @retval JAVACALL_OK if operation is successful
 */
javacall_result javacall_amms_snapshot_control_stop(
    javacall_handle hNative) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    cs->snapConfirm = FALSE;
    cs->snapStop    = TRUE;

    if( NULL != cs->snapThread && 
        WAIT_OBJECT_0 != WaitForSingleObject( cs->snapThread, 5000 ) )
    {
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK;
}

/**
 * The function corresponding to 
 * void SnapshotControl.unfreeze(boolean save)
 *  method of AMMS Java API, see JSR-234 Spec
 *
 * Unfreezes the viewfinder and saves the snapshot 
 * depending on the parameter.
 *
 * @param hNative  native handle.
 *     Java layer guarantees the following:
 *        - it is a valid media player handle,
 *        - it refers to a video capture player,
 *        - the player has acquired exclusive
 *          access to the camera device
 * @param save  \a JAVACALL_TRUE to save the snapshot
 *     \a JAVACALL_FALSE not to save the snapshot
 *
 * @retval JAVACALL_OK if operation is successful
 */
javacall_result javacall_amms_snapshot_control_unfreeze(
    javacall_handle hNative, javacall_bool save) {

    audio_handle* ah = (audio_handle*)(((javacall_impl_player*)hNative)->mediaHandle);
    camera_state* cs = (camera_state*)( ah->pExtraCC );

    if( JAVACALL_AMMS_SNAPSHOT_FREEZE_AND_CONFIRM != cs->snapShotsLeft ) return JAVACALL_OK;

    cs->snapConfirm = ( JAVACALL_TRUE == save );
    cs->snapStop    = TRUE;

    if( NULL != cs->snapThread && 
        WAIT_OBJECT_0 != WaitForSingleObject( cs->snapThread, 5000 ) )
    {
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK;
}
