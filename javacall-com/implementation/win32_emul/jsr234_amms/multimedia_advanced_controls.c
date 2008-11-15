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

#include "javacall_multimedia_advanced.h"

#include "mm_qsound_audio.h"
#include "mQ_JSR-234.h"

#include "multimedia_advanced_controls.h"
//#include "multimedia_advanced.h"


/*
 *    ================== DISTANCE ATTENUATION =================================
 */

javacall_result javacall_audio3d_distance_attenuation_control_set_parameters (
    javacall_audio3d_distance_attenuation_control_t *dac,
                      int minDistance,
                      int maxDistance,
                      javacall_bool isMuteAfterMax,
                      int rolloffFactor)
{
    MQ234_ERROR e;

    if( NULL == dac ) return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_DistanceAttenuation_setParameters(
            dac->qs_obj_ptr, minDistance, maxDistance,
            isMuteAfterMax, rolloffFactor );

    if( MQ234_ERROR_NO_ERROR == e )
    {
		 /*
        printf(
            "3D Sound Source Distance Attenuation parameters were changed.\
            Min. Distance: %d, Max. Distance: %d,\
            Mute After Max: %d, Rolloff Factor: %d\n",
            minDistance, maxDistance, isMuteAfterMax, rolloffFactor );
		*/
        return JAVACALL_OK;
    }
    else
    {
        //printf( "mQ234_DistanceAttenuation_setParameters failed.\n" );

        return JAVACALL_FAIL;
    }
}

int javacall_audio3d_distance_attenuation_control_get_min_distance (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    if( NULL == dac ) return JAVACALL_INVALID_ARGUMENT;
    return mQ234_DistanceAttenuation_getMinDistance( dac->qs_obj_ptr );
}

int javacall_audio3d_distance_attenuation_control_get_max_distance (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    if( NULL == dac ) return JAVACALL_INVALID_ARGUMENT;
    return mQ234_DistanceAttenuation_getMaxDistance( dac->qs_obj_ptr );
}

javacall_bool javacall_audio3d_distance_attenuation_control_is_mute_after_max(
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    if( NULL == dac ) return JAVACALL_INVALID_ARGUMENT;
    return mQ234_DistanceAttenuation_getMuteAfterMax( dac->qs_obj_ptr );
}

int javacall_audio3d_distance_attenuation_control_get_rolloff_factor (
        javacall_audio3d_distance_attenuation_control_t *dac )
{
    if( NULL == dac ) return JAVACALL_INVALID_ARGUMENT;
    return mQ234_DistanceAttenuation_getRolloffFactor( dac->qs_obj_ptr );
}

/*
 *    ================== LOCATION    ==========================================
 */

javacall_result javacall_audio3d_location_control_set_cartesian (
    javacall_audio3d_location_control_t *loc,
                     int x,
                     int y,
                     int z)
{
    if( NULL == loc ) return JAVACALL_INVALID_ARGUMENT;

    mQ234_Location_setCartesian( loc->qs_obj_ptr, x, y, z );

    //printf( "Location was changed to (%d, %d, %d)\n", x, y, z );

    return JAVACALL_OK;
}

javacall_result javacall_audio3d_location_control_set_spherical (
    javacall_audio3d_location_control_t *loc,
    int azimuth,
    int elevation,
    int radius)
{
    MQ234_ERROR e;

    if( NULL == loc ) return JAVACALL_INVALID_ARGUMENT;

    e = mQ234_Location_setSpherical( loc->qs_obj_ptr,
        azimuth, elevation, radius );

    if( MQ234_ERROR_NO_ERROR == e )
    {
		 /*
        printf(
            "Location changed, azimuth= %d, elevation= %d, radius= %d\n",
            azimuth, elevation, radius );
		 */
        return JAVACALL_OK;
    }
    else
    {
        return JAVACALL_FAIL;
    }
}

javacall_result javacall_audio3d_location_control_get_cartesian(
    javacall_audio3d_location_control_t *loc,
    int coord[ 3 ] )
{
    long tmp[ 3 ];

    if( NULL == loc   ) return JAVACALL_INVALID_ARGUMENT;
    if( NULL == coord ) return JAVACALL_INVALID_ARGUMENT;

    mQ234_Location_getCartesian( loc->qs_obj_ptr, tmp );

    coord[ 0 ] = tmp[ 0 ];
    coord[ 1 ] = tmp[ 1 ];
    coord[ 2 ] = tmp[ 2 ];

    return JAVACALL_OK;
}

/*
 *    ================== ORIENTATION ==========================================
 */

javacall_result javacall_audio3d_orientation_control_set_orientation (
        javacall_audio3d_orientation_control_t *ctl,
                       int heading,
                       int pitch,
                       int roll )
{
    if( NULL == ctl ) return JAVACALL_INVALID_ARGUMENT;

    mQ234_Orientation_setOrientationAngles( ctl->qs_obj_ptr,
        heading, pitch, roll );
/*
    printf( "Orientation has changed. Heading: %d, pitch: %d, roll: %d\n",
        heading, pitch, roll );
*/
    return JAVACALL_OK;
}

javacall_result javacall_audio3d_orientation_control_set_orientation_vec (
        javacall_audio3d_orientation_control_t *ctl,
                       const int frontVector[3],
                       const int aboveVector[3])
{
    MQ234_ERROR e;
    long        f[ 3 ];
    long        a[ 3 ];

    if( NULL == ctl ) return JAVACALL_INVALID_ARGUMENT;

    f[ 0 ] = frontVector[ 0 ];
    f[ 1 ] = frontVector[ 1 ];
    f[ 2 ] = frontVector[ 2 ];

    a[ 0 ] = aboveVector[ 0 ];
    a[ 1 ] = aboveVector[ 1 ];
    a[ 2 ] = aboveVector[ 2 ];

    e = mQ234_Orientation_setOrientationVectors( ctl->qs_obj_ptr, f, a );

    if( MQ234_ERROR_NO_ERROR == e )
    {
		 /*
        printf( "Orientation vectors changed."
                "front vector: (%d,%d,%d),"
                "above vector: (%d,%d,%d)\n",
                frontVector[0], frontVector[1], frontVector[2],
                aboveVector[0], aboveVector[1], aboveVector[2] );
		 */
        return JAVACALL_OK;
    }
    else
    {
        //printf( "mQ234_Orientation_setOrientationVectors failed.\n" );
        return JAVACALL_FAIL;
    }
}

javacall_result javacall_audio3d_orientation_control_get_orientation_vectors (
    javacall_audio3d_orientation_control_t *ctl,
                       int vectors[6] )
{
    long v[ 6 ];

    if( NULL == ctl || NULL == vectors ) return JAVACALL_INVALID_ARGUMENT;

    mQ234_Orientation_getOrientationVectors( ctl->qs_obj_ptr, v );

    vectors[0] = v[0];  vectors[1] = v[1];  vectors[2] = v[2];
    vectors[3] = v[3];  vectors[4] = v[4];  vectors[5] = v[5];

    return JAVACALL_OK;
}

/*
 *    ================== VOLUME ===============================================
 */

int javacall_amms_volume_control_get_level(
        javacall_amms_volume_control_t* vctl )
{
    return mQ135_Volume_GetLevel( vctl->qs_obj_ptr );
}

javacall_bool
    javacall_amms_volume_control_is_muted (
        javacall_amms_volume_control_t* vctl )
{
    return mQ135_Volume_IsMuted( vctl->qs_obj_ptr )
        ? JAVACALL_TRUE
        : JAVACALL_FALSE;
}

javacall_result
    javacall_amms_volume_control_set_level (
        javacall_amms_volume_control_t* vctl,
        int                             level,
        /*OUT*/int*                     new_level )
{
    if( level < 0 || level > 100 ) return JAVACALL_INVALID_ARGUMENT;

    *new_level = mQ135_Volume_SetLevel( vctl->qs_obj_ptr, level );
    return JAVACALL_OK;
}

javacall_result
    javacall_amms_volume_control_set_mute (
        javacall_amms_volume_control_t* vctl,
        javacall_bool                   mute )
{
    mQ135_Volume_SetMute( vctl->qs_obj_ptr, mute );
    return JAVACALL_OK;
}
