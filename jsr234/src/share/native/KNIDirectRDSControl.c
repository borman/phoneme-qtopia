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

#include "kni.h"
#include "KNICommon.h"
#include "javacall_multimedia_advanced.h"

#include "jsr234_control.h"


KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nIsSupported)
{
    jint handle = KNI_GetParameterAsInt(1); 
    jboolean ret = KNI_FALSE;
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    if( pKniInfo != NULL )
    {
        if( JAVACALL_TRUE == javacall_amms_rds_control_is_supported(
               pKniInfo->pNativeHandle ) )
        {
            ret = KNI_TRUE;
        }
    }
    KNI_ReturnBoolean( ret );    
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nIsRDSSignal)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_bool ret = JAVACALL_FALSE;
    
    if( pKniInfo != NULL )
    {
        javacall_amms_rds_control_is_rds_signal( pKniInfo->pNativeHandle,
                                                &ret );
    }
    KNI_ReturnBoolean( JAVACALL_TRUE == ret ? KNI_TRUE : KNI_FALSE  );    
    
}

KNIEXPORT KNI_RETURNTYPE_OBJECT // String 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPS)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char ps[ KNI_BUFFER_SIZE ];
    
    ps[ 0 ] = 0;
    
    KNI_StartHandles( 1 );
    KNI_DeclareHandle( hPS );
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_ps( 
                        pKniInfo->pNativeHandle,
                       ps,
                       sizeof( ps ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( ps, hPS );
    }
    else
    {
        KNI_ReleaseHandle( hPS );
    }
    
    KNI_EndHandlesAndReturnObject( hPS );
}

KNIEXPORT KNI_RETURNTYPE_OBJECT // String 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetRT)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char ps[ KNI_BUFFER_SIZE ];
    
    ps[ 0 ] = 0;
    
    KNI_StartHandles( 1 );
    KNI_DeclareHandle( hPS );
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_rt( 
                        pKniInfo->pNativeHandle,
                       ps,
                       sizeof( ps ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( ps, hPS );
    }
    else
    {
        KNI_ReleaseHandle( hPS );
    }
    
    KNI_EndHandlesAndReturnObject( hPS );
}

KNIEXPORT KNI_RETURNTYPE_SHORT // short 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPTY)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_pty( 
            pKniInfo->pNativeHandle, &freq );
    }

    KNI_ReturnShort( ( jshort )freq );
}

KNIEXPORT KNI_RETURNTYPE_OBJECT // String 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPTYString)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char pty[ KNI_BUFFER_SIZE ];
    jboolean longer = KNI_GetParameterAsBoolean( 2 );
    
    pty[ 0 ] = 0;
    
    KNI_StartHandles( 1 );
    KNI_DeclareHandle( hPTY );
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_pty_string( 
                        pKniInfo->pNativeHandle,
                        longer == KNI_TRUE ? JAVACALL_TRUE : JAVACALL_FALSE,
                       pty,
                       sizeof( pty ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( pty, hPTY );
    }
    else
    {
        KNI_ReleaseHandle( hPTY );
    }
    
    KNI_EndHandlesAndReturnObject( hPTY );
}

KNIEXPORT KNI_RETURNTYPE_INT // short 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPI)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    long freq = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_pi( 
            pKniInfo->pNativeHandle, &freq );
    }

    KNI_ReturnShort( ( jshort )freq );
}

KNIEXPORT KNI_RETURNTYPE_INT // int 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetNumFreqsByPTY)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jshort pty = KNI_GetParameterAsShort( 2 );
    long num = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_freqs_by_pty_count( 
            pKniInfo->pNativeHandle, ( long )pty, &num );
    }

    KNI_ReturnInt( ( jint )num );
}

KNIEXPORT KNI_RETURNTYPE_INT // int 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetFreqByPTY)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jshort pty = KNI_GetParameterAsShort( 2 );
    long index = ( long )KNI_GetParameterAsInt( 3 );
    long freq = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_freq_by_pty( 
            pKniInfo->pNativeHandle, ( long )pty,  index, &freq );
    }

    KNI_ReturnInt( ( jint )freq );
}

KNIEXPORT KNI_RETURNTYPE_VOID // void 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetFreqsByTADimensions)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean ta = KNI_GetParameterAsBoolean( 2 );
    long rows = -1;
    long cols = -1;
    KNI_StartHandles(1);
    KNI_DeclareHandle( hDim );
    KNI_GetParameterAsObject(3, hDim);
    
    KNI_SetIntArrayElement( hDim, 0, -1 );
    KNI_SetIntArrayElement( hDim, 1, -1 );
    
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_freqs_by_ta_matrix_dimensions(
             pKniInfo->pNativeHandle, 
             KNI_TRUE == ta ? JAVACALL_TRUE : JAVACALL_FALSE,
             &rows, &cols );
        if( JAVACALL_OK == res )
        {
            KNI_SetIntArrayElement( hDim, 0, rows );
            KNI_SetIntArrayElement( hDim, 1, cols );
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT // int
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetFreqsByTAElement)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean ta = KNI_GetParameterAsBoolean( 2 );
    long row = ( long )KNI_GetParameterAsInt( 3 );
    long col = ( long )KNI_GetParameterAsInt( 4 );
    long freq = 0;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_freq_by_ta_matrix_element(
                  pKniInfo->pNativeHandle,
                  KNI_TRUE == ta ? JAVACALL_TRUE : JAVACALL_FALSE,
                  row, col, &freq );
    }
    
    KNI_ReturnInt( ( jint )freq );
}

KNIEXPORT KNI_RETURNTYPE_INT // String[] 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetNumPSByPTY)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jshort pty = KNI_GetParameterAsShort( 2 );
    long num = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_ps_by_pty_count( 
            pKniInfo->pNativeHandle, ( long )pty, &num );
    }

    KNI_ReturnInt( ( jint )num );
}

KNIEXPORT KNI_RETURNTYPE_OBJECT // String 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPSByPTY)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char ps[ KNI_BUFFER_SIZE ];
    jshort pty = KNI_GetParameterAsShort( 2 );
    long index = ( long )KNI_GetParameterAsInt( 3 );

    ps[ 0 ] = 0;
    
    KNI_StartHandles( 1 );
    KNI_DeclareHandle( hPS );
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_ps_by_pty(
                       pKniInfo->pNativeHandle,
                       ( long )pty,
                       index,
                       ps,
                       sizeof( ps ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( ps, hPS );
    }
    else
    {
        KNI_ReleaseHandle( hPS );
    }
    
    KNI_EndHandlesAndReturnObject( hPS );
}

KNIEXPORT KNI_RETURNTYPE_INT // int
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetNumPSByTA)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    jboolean ta = KNI_GetParameterAsBoolean( 2 );
    long num = -1;

    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_ps_by_ta_count( 
            pKniInfo->pNativeHandle, 
            KNI_TRUE == ta ? JAVACALL_TRUE : JAVACALL_FALSE,
            &num );
    }

    KNI_ReturnInt( ( jint )num );
}

KNIEXPORT KNI_RETURNTYPE_OBJECT // String 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetPSByTA)
{
    jint handle = KNI_GetParameterAsInt(1);
    KNIPlayerInfo* pKniInfo = (KNIPlayerInfo*)handle;
    javacall_result res = JAVACALL_FAIL;
    char ps[ KNI_BUFFER_SIZE ];
    jboolean ta = KNI_GetParameterAsBoolean( 2 );
    long index = ( long )KNI_GetParameterAsInt( 3 );

    ps[ 0 ] = 0;
    
    KNI_StartHandles( 1 );
    KNI_DeclareHandle( hPS );
    if( pKniInfo != NULL )
    {
        res = javacall_amms_rds_control_get_ps_by_ta(
                       pKniInfo->pNativeHandle,
            KNI_TRUE == ta ? JAVACALL_TRUE : JAVACALL_FALSE,
                       index,
                       ps,
                       sizeof( ps ) );
    }
    if( res == JAVACALL_OK)
    {
        KNI_NewStringUTF( ps, hPS );
    }
    else
    {
        KNI_ReleaseHandle( hPS );
    }
    
    KNI_EndHandlesAndReturnObject( hPS );
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetTA)
{
    KNI_ReturnBoolean( getBoolWithTypicalJavacallFunc( KNIPASSARGS 
                                  javacall_amms_rds_control_get_ta ) );
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetTP)
{
    KNI_ReturnBoolean( getBoolWithTypicalJavacallFunc( KNIPASSARGS 
                                  javacall_amms_rds_control_get_tp ) );
}

KNIEXPORT KNI_RETURNTYPE_VOID // void 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nSetAutomaticSwitching)
{
    javacall_result res = setBoolWithTypicalJavacallFunc( KNIPASSARGS 
       javacall_amms_rds_control_set_automatic_switching );
    if( JAVACALL_OK != res )
    {
        if( JAVACALL_NOT_IMPLEMENTED == res )
        {
            KNI_ThrowNew( "javax/microedition/media/MediaException", "\nRDSCon\
trol setting AutomaticSwitching to this value is not supported\n" );
        }
        else if( JAVACALL_INVALID_ARGUMENT == res )
        {
            KNI_ThrowNew( "java/lang/RuntimeException", "\nWrong arguments pas\
sed to DirectRDSControl.nSetAutomaticSwitching()\n" );
        }
        else
        {
            KNI_ThrowNew( "java/lang/RuntimeException", "\nDirectRDSControl.nS\
etAutomaticSwitching() failed\n" );
        }
    }
    
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetAutomaticSwitching)
{
    KNI_ReturnBoolean( getBoolWithTypicalJavacallFunc( KNIPASSARGS 
                      javacall_amms_rds_control_get_automatic_switching ) );
}

KNIEXPORT KNI_RETURNTYPE_VOID // void 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nSetAutomaticTA)
{
    javacall_result res = setBoolWithTypicalJavacallFunc( KNIPASSARGS 
       javacall_amms_rds_control_set_automatic_ta );
    if( JAVACALL_OK != res )
    {
        if( JAVACALL_NOT_IMPLEMENTED == res )
        {
            KNI_ThrowNew( "javax/microedition/media/MediaException", "\nRDSCon\
trol setting AutomaticTA to this value is not supported\n" );
        }
        else if( JAVACALL_INVALID_ARGUMENT == res )
        {
            KNI_ThrowNew( "java/lang/RuntimeException", "\nWrong arguments pas\
sed to DirectRDSControl.nSetAutomaticTA()\n" );
        }
        else
        {
            KNI_ThrowNew( "java/lang/RuntimeException", "\nDirectRDSControl.nS\
etAutomaticTA() failed\n" );
        }
    }
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN // boolean 
KNIDECL(com_sun_amms_directcontrol_DirectRDSControl_nGetAutomaticTA)
{
    KNI_ReturnBoolean( getBoolWithTypicalJavacallFunc( KNIPASSARGS 
                          javacall_amms_rds_control_get_automatic_ta ) );
}

