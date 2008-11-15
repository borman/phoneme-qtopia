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

#include "kni.h"
#include "jsr234_nativePtr.h"

int getNativeHandleFromField( KNIDECLARGS const char *field_name )
{
    int handle = 0;
    jfieldID fID_peer;

    KNI_StartHandles(2);
    KNI_DeclareHandle( javaThis );
    KNI_DeclareHandle( javaClass );

//    KNI_GetClassPointer( javaClass );
    KNI_GetThisPointer( javaThis );
    KNI_GetObjectClass( javaThis, javaClass );

    fID_peer = KNI_GetFieldID( javaClass, field_name, "I" );
    handle = KNI_GetIntField( javaThis, fID_peer );

    KNI_EndHandles();

    return handle;
}
