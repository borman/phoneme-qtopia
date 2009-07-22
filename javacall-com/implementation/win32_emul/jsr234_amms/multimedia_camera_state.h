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

#ifndef __MULTIMEDIA_CAMERA_STATE_H
#define __MULTIMEDIA_CAMERA_STATE_H

#include "javacall_multimedia.h"
#include "javacall_multimedia_advanced.h"
#include "multimedia.h"

typedef struct {
    audio_handle*         ah;

    javacall_bool         shutterFeedback;
    int                   expMode;
    long                  stillRes;
    long                  videoRes;

    long                  exposureCurrentFStop;
    long                  exposureCurrentISO;

    int                   flashMode;

    long                  focusDistance;
    javacall_bool         focusMacroMode;

    long                  zoomOptical;
    long                  zoomDigital;

    javacall_utf16_string snapDirectory;
    javacall_utf16_string snapPrefix;
    javacall_utf16_string snapSuffix;
    long                  snapIndex;
    volatile long         snapShotsLeft;
    volatile HANDLE       snapThread;
    volatile BOOL         snapStop;
    volatile BOOL         snapConfirm;
    wchar_t               snapLastName[ MAX_PATH ];
    wchar_t               snapLastNameForJava[ MAX_PATH ];
} camera_state;

#endif // __MULTIMEDIA_CAMERA_STATE_H
