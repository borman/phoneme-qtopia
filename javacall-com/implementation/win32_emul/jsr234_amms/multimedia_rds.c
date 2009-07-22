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

#include "javacall_multimedia.h"
#include "javacall_multimedia_advanced.h"


/**
 * This function determines whether the Radio Data System functionality
 * described in this group supported for a given radio Player
 * If it returns JAVACALL_FALSE, all the functions in this group should return
 * JAVACALL_NOT_IMPLEMENTED. Otherwise, they all must be implemented properly
 * and all the corresponding funcionality should be supported.
 * 
 * @param javacall_handle	handle to the given Player which is usually
 *                          a radio player, e.g. created for 
 *                          "radio://capture" URI
 * @retval JAVACALL_TRUE        Radio Data System functionality is supported
 * @retval JAVACALL_FALSE       Radio Data System functionality 
 *                              IS NOT supported
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player
 * @see javacall_media_create
 */
javacall_bool javacall_amms_rds_control_is_supported(
    javacall_handle hNative)
{
    return JAVACALL_TRUE;
}

/**
 * This function determines whether the current signal is RDS signal
 *
 * @param hNative       handle to the given radio player
 * @param rdsSignal     pointer to return the boolean value to answer
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_is_rds_signal(
    javacall_handle hNative, /*OUT*/javacall_bool *rdsSignal)
{
    return JAVACALL_FALSE;
}

/**
 * This function is analogous to the JSR-234 RDSControl.getPS(), see
 * JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param ps            address of the buffer to return the string
 * @param bufLen        lengh of the buffer to return the string
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or bufLen is not enough to
 *                                      return the PS name
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_get_ps(
    javacall_handle hNative, /*OUT*/char *ps, long bufLen)
{
    if( NULL == ps || bufLen <= 0)
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *ps = '\0';
    return JAVACALL_OK;
}

/**
 * This function is analogous to the JSR-234 RDSControl.getRT(), see
 * JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param rt            address of the buffer to return the string
 * @param bufLen        lengh of the buffer to return the string
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or bufLen is not enough to
 *                                      return the RT name
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_get_rt(
    javacall_handle hNative, /*OUT*/char *rt, long bufLen)
{
    if( NULL == rt || bufLen <= 0 )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *rt = '\0';

    return JAVACALL_OK;
}

/**
 * This function is analogous to the JSR-234 RDSControl.getPTY(), see
 * JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param pty           pointer to return the number
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or pty is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_get_pty(
    javacall_handle hNative, /*OUT*/long *pty)
{
    if( NULL == pty )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *pty = 0;
    return JAVACALL_OK;
}

/**
 * This function is analogous to the JSR-234 RDSControl.getPTYString(), see
 * JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param longer        whether longer PTY string are supported, 
 *                      see JSR-234 Spec
 * @param ptyStr        address of the buffer to return the string
 * @param bufLen        lengh of the buffer to return the string
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or the buffer address is NULL,
 *                                      or bufLen is not enough to
 *                                      return the PTY string
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_get_pty_string(
    javacall_handle hNative, javacall_bool longer, /*OUT*/char *ptyStr, long bufLen)
{
    if( NULL == ptyStr )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    if( JAVACALL_TRUE == longer )
    {
        static const char _pty_str[] = "NOT AVAILABLE";
        int i = sizeof( _pty_str ) / sizeof( _pty_str[0] );
        if( i > bufLen )
        {
            return JAVACALL_INVALID_ARGUMENT;
        }
        for( i--; i >= 0; i-- )
        {
            ptyStr[ i ] = _pty_str[ i ];
        }
    }
    else
    {
        static const char _pty_str[] = "UNKNOWN";
        int i = sizeof( _pty_str ) / sizeof( _pty_str[0] );
        if( i > bufLen )
        {
            return JAVACALL_INVALID_ARGUMENT;
        }
        for( i--; i >= 0; i-- )
        {
            ptyStr[ i ] = _pty_str[ i ];
        }
    }
    return JAVACALL_OK;
}

/**
 * This function is analogous to the JSR-234 RDSControl.getPI(), see
 * JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param pi            pointer to return the number
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or \a pi is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * 
 */
javacall_result javacall_amms_rds_control_get_pi(
    javacall_handle hNative, /*OUT*/long *pi)
{
    if( NULL == pi )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *pi = 0;
    return JAVACALL_OK;
}

/**
 * This function returns the size of the array returned by JSR-234 method
 * RDSControl.getFreqsByPTY(), see JSR-234 Spec
 *
 * @param hNative       handle to the given radio player
 * @param pty           programme type
 * @param count         pointer to return the array length
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or pty is not valid, 
 *                                      or \a count is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_freq_by_pty
 */
javacall_result javacall_amms_rds_control_get_freqs_by_pty_count(
    javacall_handle hNative, long pty, /*OUT*/long *count)
{
    if( NULL == count )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *count = 0;
    return JAVACALL_OK;
}

/**
 * This function returns an element of the array returned by JSR-234 method
 * RDSControl.getFreqsByPTY(), see JSR-234 Spec
 * @param hNative       handle to the given radio player
 * @param pty           programme type
 * @param index         the index of the requested element
 * @param freq          pointer to return the element
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or pty is not valid, 
 *                                      or \a freq is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_freqs_by_pty_count
 */
javacall_result javacall_amms_rds_control_get_freq_by_pty(
    javacall_handle hNative, long pty, long index, /*OUT*/long *freq)
{
    return JAVACALL_INVALID_ARGUMENT;
}

/**
 * This function returns the dimensions of the two-dimensional array returned
 * by JSR-234 method RDSControl.getFreqsByTA(), 
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param ta            TA, see JSR-234 method RDSControl.getFreqsByTA()
 *                      description
 * @param programmes_num    pointer to return the array rows number (the number
 *                          of programmes)
 * @param alt_freqs_num     pointer to return the array columns number 
 *                          (the number of alternative frequencies)
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a programmes_num is NULL,
 *                                      or \a alt_freqs_num is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_freq_by_ta_matrix_element
 */
javacall_result javacall_amms_rds_control_get_freqs_by_ta_matrix_dimensions(
    javacall_handle hNative, javacall_bool ta, /*OUT*/long *programmes_num,
    /*OUT*/long *alt_freqs_num )
{
    if( NULL == programmes_num || NULL == alt_freqs_num )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *programmes_num = 0;
    *alt_freqs_num = 0;
    return JAVACALL_OK;
}

/**
 * This function returns an element of the two-dimensional array returned
 * by JSR-234 method RDSControl.getFreqsByTA(), 
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param ta            TA, see JSR-234 method RDSControl.getFreqsByTA()
 *                      description
 * @param i             row index
 * @param j             column index
 * @param freq          pointer to return the element
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a i is out of the array bounds,
 *                                      or \a j is out of the array bounds,
 *                                      or \a freq is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_freqs_by_ta_matrix_dimensions
 */
javacall_result javacall_amms_rds_control_get_freq_by_ta_matrix_element(
    javacall_handle hNative, javacall_bool ta, long i, long j,
    /*OUT*/long *freq)
{
    return JAVACALL_INVALID_ARGUMENT;
}

/**
 * This function returns the size of the array returned by JSR-234 method
 * RDSControl.getPSByPTY(), see JSR-234 Spec
 *
 * @param hNative       handle to the given radio player
 * @param pty           programme type
 * @param count         pointer to return the array length
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or pty is not valid, 
 *                                      or \a count is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_ps_by_pty
 */
javacall_result javacall_amms_rds_control_get_ps_by_pty_count(
    javacall_handle hNative, long pty, /*OUT*/long *count)
{
    if( NULL == count )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *count = 0;
    return JAVACALL_OK;
}

/**
 * This function returns an element of the array returned by JSR-234 method
 * RDSControl.getPSByPTY(), see JSR-234 Spec
 * @param hNative       handle to the given radio player
 * @param pty           programme type
 * @param index         the index of the requested element
 * @param ps            address of the buffer to return the element string
 * @param bufLen        length of the buffer to return the element string
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player; or pty is not valid, 
 *                                      or \a ps is NULL, or bufLen is not
 *                                      enough to return the PS name
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_ps_by_pty_count
 */
javacall_result javacall_amms_rds_control_get_ps_by_pty(
    javacall_handle hNative, long pty, long index, /*OUT*/char *ps, long bufLen)
{
    return JAVACALL_INVALID_ARGUMENT;
}

/**
 * This function returns the size of the array returned by JSR-234 method
 * RDSControl.getPSByTA(), see JSR-234 Spec
 *
 * @param hNative       handle to the given radio player
 * @param ta            TA, see JSR-234 Spec, RDSControl.getPSByTA()
 * @param count         pointer to return the array length
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a count is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_ps_by_ta
 */
javacall_result javacall_amms_rds_control_get_ps_by_ta_count(
    javacall_handle hNative, javacall_bool ta, /*OUT*/long *count)
{
    if( NULL == count )
    {
        return JAVACALL_INVALID_ARGUMENT;
    }
    *count = 0;
    return JAVACALL_OK;
}

/**
 * This function returns an element of the array returned by JSR-234 method
 * RDSControl.getPSByTA(), see JSR-234 Spec
 * @param hNative       handle to the given radio player
 * @param ta            TA, see JSR-234 Spec, RDSControl.getPSByTA()
 * @param index         the index of the requested element
 * @param ps            address of the buffer to return the element string
 * @param bufLen        length of the buffer to return the element string
 *
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a ps is NULL, or bufLen is not
 *                                      enough to return the PS name
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 * @see javacall_amms_rds_control_get_ps_by_ta_count
 */
javacall_result javacall_amms_rds_control_get_ps_by_ta(
    javacall_handle hNative, javacall_bool ta, long index, /*OUT*/char *ps, long bufLen)
{
    return JAVACALL_INVALID_ARGUMENT;
}

/**
 * This function is analogous to JSR-234 RDSControl.getTA(), see JSR-234 Spec
 * at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param ta            pointer to return TA, 
 *                      see JSR-234 Spec, RDSControl.getTA()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a ta is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_get_ta(
    javacall_handle hNative, /*OUT*/javacall_bool *ta)
{
    *ta = JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * This function is analogous to JSR-234 RDSControl.getTP(), see JSR-234 Spec
 * at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param TP            pointer to return TP, 
 *                      see JSR-234 Spec, RDSControl.getTP()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a tp is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_get_tp(
    javacall_handle hNative, /*OUT*/javacall_bool *tp)
{
    *tp = JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * This function is analogous to JSR-234 RDSControl.setAutomaticSwitching(),
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param automatic     Automatic Switching mode to set,
 *                      see JSR-234 Spec, RDSControl.setAutomaticSwitching()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_set_automatic_switching(
    javacall_handle hNative, javacall_bool automatic)
{
    if( JAVACALL_TRUE == automatic )
    {
        return JAVACALL_NOT_IMPLEMENTED;
    }
    return JAVACALL_OK;
}

/**
 * This function is analogous to JSR-234 RDSControl.getAutomaticSwitching(),
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param automatic     pointer to return the current Automatic Switching mode,
 *                      see JSR-234 Spec, RDSControl.getAutomaticSwitching()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a automatic is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_get_automatic_switching(
    javacall_handle hNative, /*OUT*/javacall_bool *automatic)
{
    *automatic = JAVACALL_FALSE;
    return JAVACALL_OK;
}

/**
 * This function is analogous to JSR-234 RDSControl.setAutomaticTA(),
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param automatic     Automatic TA mode to set,
 *                      see JSR-234 Spec, RDSControl.setAutomaticTA()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_set_automatic_ta(
    javacall_handle hNative, javacall_bool automatic)
{
    if( JAVACALL_TRUE == automatic )
    {
        return JAVACALL_NOT_IMPLEMENTED;
    }
    return JAVACALL_OK;
}

/**
 * This function is analogous to JSR-234 RDSControl.getAutomaticTA(),
 * see JSR-234 Spec at www.jcp.org
 *
 * @param hNative       handle to the given radio player
 * @param automatic     pointer to return the current Automatic TA mode,
 *                      see JSR-234 Spec, RDSControl.getAutomaticTA()
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    General failure
 * @retval JAVACALL_INVALID_ARGUMENT    the handle is \a NULL, or 
 *                                      not a valid media player handle, or 
 *                                      refers to a player which is not a radio
 *                                      player;  
 *                                      or \a automatic is NULL
 * @retval JAVACALL_NOT_IMPLEMENTED     RDS functionality is not supported
 * @see javacall_amms_rds_control_is_supported
 */
javacall_result javacall_amms_rds_control_get_automatic_ta(
    javacall_handle hNative, /*OUT*/javacall_bool *automatic)
{
    *automatic = JAVACALL_FALSE;
    return JAVACALL_OK;
}

