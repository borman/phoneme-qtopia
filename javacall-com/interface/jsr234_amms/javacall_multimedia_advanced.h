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

#ifndef __JAVACALL_MULTIMEDIA_ADVANCED_H
#define __JAVACALL_MULTIMEDIA_ADVANCED_H
/**
 * @file javacall_multimedia_advanced.h
 * @ingroup JSR234
 * @brief Javacall interfaces for JSR-234, Advanced Multimedia Supplements
 *
 */
#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_multimedia.h" 

/** 
 * @defgroup JSR234 JSR234 Advanced Multimedia Supplements (AMMS) API
 *
 * <H2>Introduction</H2>
 * Advanced Multimedia Supplements (AMMS, JSR-234) is based on the
 * Mobile Media API (MMAPI, JSR-135). AMMS introduces a lot of new Controls
 * and other features.
 *  
 * See proper JSR documents for exact requirements.
 *
 * @{
 */

/**
 * @defgroup jsrMandatoryJSR234Common The part common for all JSR-234 features
 * @ingroup JSR234
 * @{
 */

/**
 * @enum javacall_amms_control_type_enum_t
 * @brief AMMS Control types
 */
typedef enum 
{
    /** This type value indicates that the control type requested is unknown
      */
    javacall_amms_eUnknownControl,
    /** This type value means that the Control is LocationControl,
      * see JSR-234 Spec 
      * @see javacall_audio3d_location_control_t
      */
    javacall_audio3d_eLocationControl,
    /** This type value means that the Control is OrientationControl,
      * see JSR-234 Spec
      * @see javacall_audio3d_orientation_control_t
      */
    javacall_audio3d_eOrientationControl,
    /** This type value means that the Control is DistanceAttenuationControl
      * see JSR-234 Spec
      * @see javacall_audio3d_distance_attenuation_control_t
      */
    javacall_audio3d_eDistanceAttenuationControl,
    /** This type value means that the Control is AudioVirtualizerControl
      * see JSR-234 Spec
      * @see javacall_amms_audiovirtualizer_control_t
      */
    javacall_amms_eAudioVirtualizerControl,
    /** This type value means that the Control is ChorusControl
      * see JSR-234 Spec
      * @see javacall_amms_chorus_control_t
      */
    javacall_amms_eChorusControl,
    /** This type value means that the Control is EqualizerControl
      * see JSR-234 Spec
      * @see javacall_amms_equalizer_control_t
      */
    javacall_amms_eEqualizerControl,
    /** This type value means that the Control is ReverbControl,
      * see JSR-234 Spec
      * @see javacall_music_reverb_control_t
      */
    javacall_music_eReverbControl,
    /** This type value means that the Control is ReverbSourceControl
      * see JSR-234 Spec
      * @see javacall_amms_reverb_source_control_t
      */
    javacall_amms_eReverbSourceControl,
    /** This type value means that the Control is VolumeControl
      * see JSR-135 Spec
      * @see javacall_amms_volume_control_t
      */
    javacall_amms_eVolumeControl
} javacall_amms_control_type_enum_t;

    
/**
 * struct javacall_amms_control_t
 * @brief Structure corresponding to the Control interface
 * of MMAPI Java API, see JSR-135 Spec
 */
typedef struct 
{
    /** the type of the control 
      * @see  javacall_amms_control_type_enum_t 
      */
    javacall_amms_control_type_enum_t type;
    /** pointer to the specific control structure according to its type 
      * @see  javacall_amms_control_type_enum_t 
      */
    void *ptr;
} javacall_amms_control_t;

/**
 * @enum javacall_amms_effect_control_scope_enum_t
 * @brief   possible Scope values for EffectControl,
 *          for example ReverbControl (javacall_music_reverb_control_t),
 *          see JSR-234 Spec
 */
typedef enum
{
    /**
      * Corresponds to EffectControl.SCOPE_LIVE_ONLY constant
      * in AMMS Java API, see JSR-234 Spec
      */
    javacall_amms_eSCOPE_LIVE_ONLY = 1,
    /**
      * Corresponds to EffectControl.SCOPE_RECORD_ONLY constant
      * in AMMS Java API, see JSR-234 Spec
      */
    javacall_amms_eSCOPE_RECORD_ONLY = 2,
    /**
      * Corresponds to EffectControl.SCOPE_LIVE_AND_RECORD constant
      * in AMMS Java API, see JSR-234 Spec
      */
    javacall_amms_eSCOPE_LIVE_AND_RECORD = 3
} javacall_amms_effect_control_scope_enum_t;

/**
  * The function corresponding to 
  * GlobalManager.getSupportedSoundSource3DPlayerTypes()
  *  method of AMMS Java API, see JSR-234 Spec
  * Return array of media format types, device and capture locators
  * (see the defines from JSR-135 Javacall API) that can be
  * used to create players connectable to 3D Sound Sources.
  * 
  * @param number_of_types  pointer to return the returned array size
  *
  * @return array of the player media format types supported by 3D stuff. The
  *         size of the array is returned using "javacall_media_type"
  *         parameter
  *         @see javacall_media_format_type
  */
const javacall_media_format_type*
    javacall_audio3d_get_supported_soundsource3d_player_types( 
        /*OUT*/ int *number_of_types );

/**
  * @typedef javacall_amms_local_manager_t
  * @brief Type corresponding to the class GlobalManager in the Java API
  * to a certain extent (see JSR-234 Spec). The difference is that there can
  * be multiple Local Managers if several isolated Java applications are
  * run concurrently.
  *
  */
typedef struct tag_javacall_amms_local_manager
    javacall_amms_local_manager_t;  

/** @} */

/**
 * @defgroup jsrMandatoryJSR234Audio3D Mandatory part of 3D Audio Capability
 * @ingroup JSR234
 * @{
 */

/**
  * @typedef javacall_audio3d_spectator_t
  * @brief type corresponding to Spectator class of AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_audio3d_spectator
    javacall_audio3d_spectator_t;

/**
  * @typedef javacall_audio3d_soundsource3d_t
  * @brief type corresponding to SoundSource3D interface of AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_audio3d_soundsource3d 
    javacall_audio3d_soundsource3d_t;

/** 
  * @typedef javacall_audio3d_distance_attenuation_control_t 
  * @brief type corresponding to DistanceAttenuationControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_audio3d_distance_attenuation_control
    javacall_audio3d_distance_attenuation_control_t;

/** 
  * @typedef javacall_audio3d_location_control_t 
  * @brief type corresponding to LocationControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_audio3d_location_control
                    javacall_audio3d_location_control_t;

/** 
  * @typedef javacall_audio3d_orientation_control_t 
  * @brief type corresponding to OrientationControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_audio3d_orientation_control
            javacall_audio3d_orientation_control_t;

/** 
  * @typedef javacall_music_reverb_control_t 
  * @brief type corresponding to ReverbControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_music_reverb_control
                javacall_music_reverb_control_t;

/** @} */

/**
 * @defgroup jsrMandatoryJSR234Music Mandatory part of Music Capability
 * @ingroup JSR234
 * @{
 */

/** 
  * @typedef javacall_amms_equalizer_control_t 
  * @brief type corresponding to EqualizerControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_amms_equalizer_control
                javacall_amms_equalizer_control_t;

/** 
  * @typedef javacall_amms_volume_control_t 
  * @brief type corresponding to VolumeControl in MMAPI Java API,
  * see JSR-135 Spec
  */
typedef struct tag_javacall_amms_volume_control
            javacall_amms_volume_control_t;

/** @} */

/**
 * @defgroup jsrOptionalJSR234SoundEffects Optional parts of SoundEffects package
 * @ingroup JSR234
 * @{
 */

/**
  * @typedef javacall_amms_effectmodule_t;
  * @brief type corresponding to EffectModule interface of AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_amms_effectmodule 
    javacall_amms_effectmodule_t;

/** 
  * @typedef javacall_amms_audiovirtualizer_control_t 
  * @brief type corresponding to AudioVirtualizerControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_amms_audiovirtualizer_control
                javacall_amms_audiovirtualizer_control_t;

/** 
  * @typedef javacall_amms_chorus_control_t 
  * @brief type corresponding to ChorusControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_amms_chorus_control
                javacall_amms_chorus_control_t;

/** 
  * @typedef javacall_amms_reverb_source_control_t 
  * @brief type corresponding to ReverbSourceControl in AMMS Java API,
  * see JSR-234 Spec
  */
typedef struct tag_javacall_amms_reverb_source_control
    javacall_amms_reverb_source_control_t;

/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Common
 * @ingroup JSR234
 * @{
 */

/**
  * To create a new Local Manager
  * 
  * @param manager                   pointer to return the address of the new
  *                                  Local Manager
  *
  * @retval JAVACALL_OK              Success
  * @retval JAVACALL_FAIL            Fail
  * @retval JAVACALL_OUT_OF_MEMORY   if there was not enough memory to 
  *                                  create new Local Manager
  * @see javacall_result
  * @see javacall_amms_local_manager_t
  */
javacall_result javacall_amms_create_local_manager( 
                   /*OUT*/ javacall_amms_local_manager_t **manager );

/**
  * To destroy a Local Manager
  * 
  * @param manager                   pointer to the Local Manager
  *
  * @retval JAVACALL_OK              Success
  * @retval JAVACALL_FAIL            Fail
  *
  * @see javacall_result
  * @see javacall_amms_local_manager_t
  */
javacall_result javacall_amms_destroy_local_manager(
                        javacall_amms_local_manager_t *manager );
    
/**
  * The function corresponding to GlobalManager.getControl()
  * method of MMAPI, see JSR-135 Spec
  *
  * Return a "local" Control which scope is the current MIDP application.
  * (but not any other MIDP applications run concurrently)
  *
  * @param manager     pointer to the local manager.
  * @param type     the type of the requested Control (from the enum)
  * 
  * @return pointer to the requested Control structure or NULL if this
  *         type of control is not supported
  * @see javacall_amms_controllable_t
  */
javacall_amms_control_t* javacall_amms_local_manager_get_control (
    javacall_amms_local_manager_t *manager,
    javacall_amms_control_type_enum_t type );

/**
  * The function corresponding to GlobalManager.getControls()
  * method of MMAPI, see JSR-135 Spec
  *
  * Obtain the array of the supported "local" Controls common for the 
  *  current MIDP application
  * (but not any other MIDP applications run concurrently)
  * 
  * @param manager     pointer to the local manager.
  * @param controls_number  pointer to return the returned array length
  * 
  * @return array of the supported Controls, which length is return through
  *         the controls_number parameter. The array of Control's 
  *         returned will not contain any duplicates. And the list will
  *         not change over time. If no Control is supported, NULL is 
  *         returned.
  * @see javacall_amms_local_manager_t
  */
javacall_amms_control_t* javacall_amms_local_manager_get_controls (
    javacall_amms_local_manager_t *manager,
    /*OUT*/ int *controls_number );

/**
  * The function corresponding to GlobalManager.createSoundSource3D()
  *  method of AMMS Java API, see JSR-234 Spec
  * Create new source of sound in 3D space.
  *
  * @param  manager     pointer to the Local Manager responsible for creation
  *                     3D Sound Sources within the current Java application.
  *                     @see javacall_amms_local_manager_t
  * @param  source  pointer to return the address of the new 
  *                 javacall_audio3d_soundsource3d_t instance
  *                 or NULL if creation of 3D sound sources is not supported
  *                 @see javacall_audio3d_soundsource3d_t
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if creation of 3D sound sources is not
  *                                     supported
  * @retval JAVACALL_OUT_OF_MEMORY      if there is not enough memory to 
  *                                     create new SoundSource3D
  *                                     @see javacall_result
  */
javacall_result javacall_amms_local_manager_create_sound_source3d(
                    javacall_amms_local_manager_t* manager,
                    /*OUT*/ javacall_audio3d_soundsource3d_t** source );

/**
  * Destroys source of sound in 3D space when it is no longer needed.
  *
  * @param  manager     pointer to the Local Manager responsible for creation
  *                     3D Sound Sources within the current Java application.
  *                     @see javacall_amms_local_manager_t
  * @param  source  pointer to the 3D sound source to be destroyed
  *                 @see javacall_audio3d_soundsource3d_t
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_INVALID_ARGUMENT   if source is NULL or it is not a 
  *                                     pointer to 
  *                                     javacall_audio3d_soundsource3d_t
  */
javacall_result javacall_amms_local_manager_destroy_sound_source3d( 
                    javacall_amms_local_manager_t* manager,
                    javacall_audio3d_soundsource3d_t* source );

/**
  * The function corresponding to GlobalManager.createEffectModule()
  *  method of AMMS Java API, see JSR-234 Spec
  * Create new EffectModule.
  *
  * @param  manager     pointer to the Local Manager responsible for creation
  *                     of Effect Modules within the current Java application.
  *                     @see javacall_amms_local_manager_t
  * @param  module  pointer to return the address of the new 
  *                 javacall_amms_effectmodule_t instance
  *                 or NULL if creation of Effect Modules is not supported
  *                 @see javacall_amms_effectmodule_t
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if creation of EffectModules is not
  *                                     supported
  * @retval JAVACALL_OUT_OF_MEMORY      if there is not enough memory to 
  *                                     create new EffectModule
  *                                     @see javacall_result
  */
javacall_result
    javacall_amms_local_manager_create_effectmodule(
        javacall_amms_local_manager_t         *manager,
        /*OUT*/ javacall_amms_effectmodule_t **module );

/**
  * Destroys effect module when it is no longer needed.
  *
  * @param  manager     pointer to the Local Manager responsible for creation
  *                     effect modules within the current Java application.
  *                     @see javacall_amms_local_manager_t
  * @param  module  pointer to the effect module to be destroyed
  *                 @see javacall_amms_effectmodule_t
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_INVALID_ARGUMENT   if source is NULL or it is not a 
  *                                     pointer to 
  *                                     javacall_amms_effectmodule_t
  */
javacall_result
    javacall_amms_local_manager_destroy_effectmodule( 
        javacall_amms_local_manager_t *manager,
        javacall_amms_effectmodule_t  *module );

/**
  * The function corresponding to GlobalManager.getSpectator()
  *  method of AMMS Java API, see JSR-234 Spec
  * Return pointer to the Spectator in 3D Audio space
  *
  * @param  manager     pointer to the Local Manager responsible for 
  *                     the current Java application.
  *                     @see javacall_amms_local_manager_t
  * @return pointer to the javacall_audio3d_spectator_t instance which is
  *         unique for the current Java application or NULL if Spectator
  *         is not supported
  *         @see javacall_audio3d_spectator_t
  */
javacall_audio3d_spectator_t* javacall_amms_local_manager_get_spectator(
                    javacall_amms_local_manager_t* manager );

/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Audio3D
 * @ingroup JSR234
 * @{
 */

/**
  * The function corresponding to Spectator.getControl()
  * method of MMAPI, see JSR-135 Spec
  *
  * Return a Control to control the specified Spectator
  *
  * @param spectator     pointer to the spectator.
  * @param type     the type of the requested Control (from the enum)
  * 
  * @return pointer to the requested Control structure or NULL if this
  *         type of control is not supported
  * @see javacall_amms_controllable_t
  */
javacall_amms_control_t* javacall_audio3d_spectator_get_control (
    javacall_audio3d_spectator_t *spectator,
    javacall_amms_control_type_enum_t type );

/**
  * The function corresponding to Spectator.getControls()
  * method of MMAPI, see JSR-135 Spec
  *
  * Obtain the array of the supported Controls which can be used to 
  *  control the specified Spectator
  * 
  * @param spectator     pointer to the spectator.
  * @param controls_number  pointer to return the returned array length
  * 
  * @return array of the supported Controls, which length is return through
  *         the controls_number parameter. The array of Control's 
  *         returned will not contain any duplicates. And the list will
  *         not change over time. If no Control is supported, NULL is 
  *         returned.
  * @see javacall_audio3d_spectator_t
  */
javacall_amms_control_t* javacall_audio3d_spectator_get_controls (
    javacall_audio3d_spectator_t *spectator,
    /*OUT*/ int *controls_number );

/**
  * The function corresponding to SoundSource3D.addPlayer() method
  * of AMMS Java API, see JSR-234 Spec
  *
  * Add a Player to the SoundSource3D
  * 
  * @param soundsource3d    pointer to the SoundSource3D.
  * @param handle   Handle of native player. Java layer is responsible
  *                 for the following: 
  *                 - neither the player nor any of its channels is already 
  *                 belonging to the SoundSource3D,
  *                 - there is no player currently belonging to the
  *                  SoundSource3D in PREFETCHED or STARTED state,
  *                 - the player to be added is in neither PREFETCHED nor
  *                 STARTED state,
  *                 - the player handle is not NULL
  *                 See JSR-135 Spec for Player states explanation
  *                 @see javacall_media_acquire_device
  *                 @see javacall_media_release_device
  *                 @see javacall_media_start
  *                 @see javacall_media_stop
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_NOT_IMPLEMENTED    The addition is not supported 
  *                                     by the implementation. (For 
  *                                     example, if the implementation 
  *                                     does not support adding the same 
  *                                     Player to multiple modules or if 
  *                                     the implementation does not 
  *                                     support the type of the Player.) 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  */
javacall_result javacall_audio3d_soundsource3d_add_player ( 
    javacall_audio3d_soundsource3d_t *soundsource3d, 
    javacall_handle handle );

/**
  * The function corresponding to SoundSource3D.removePlayer() method
  * of AMMS Java API, see JSR-234 Spec
  *
  * Remove a Player from the SoundSource3D
  * 
  * @param soundsource3d    pointer to the SoundSource3D.
  * @param handle   Handle of native player. Java layer is responsible
  *                 for the following: 
  *                 - the player is currently being a part the SoundSource3D,
  *                 - the SoundSource3D is currently containing no Players in
  *                 PREFETCHED or STARTED state,
  *                 - the Player to be removed is not in PREFETCHED or STARTED
  *                 state,
  *                 - the player handle is not NULL
  *                 See JSR-135 Spec for Player states explanation
  *                 @see javacall_media_acquire_device
  *                 @see javacall_media_release_device
  *                 @see javacall_media_start
  *                 @see javacall_media_stop
  *                 
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  * @see javacall_audio3d_soundsource3d_t
  */
javacall_result javacall_audio3d_soundsource3d_remove_player ( 
    javacall_audio3d_soundsource3d_t *soundsource3d, 
    javacall_handle handle );

/**
  * The function corresponding to SoundSource3D.addMIDIChannel()
  * method of AMMS Java API, see JSR-234 Spec
  * 
  * Add MIDI channel to the SoundSource3D
  * 
  * @param soundsource3d    pointer to the SoundSource3D.
  * @param handle       Handle of native player. Java layer is responsible
  *                     for the following: 
  *                     - the player is a MIDI player,
  *                     - neither the channel nor the whole player is already a
  *                     part of the SoundSource3D, 
  *                     - the SoundSource3D currently is containing no Player in
  *                     PREFETCHED or STARTED state,
  *                     - the Player to be added is not in PREFETCHED or
  *                     STARTED state,
  *                     - the player is not NULL
  *                     See JSR-135 Spec for Player states explanation
  *                     @see javacall_media_acquire_device
  *                     @see javacall_media_release_device
  *                     @see javacall_media_start
  *                     @see javacall_media_stop
  * @param channel      the channel of the player to be added. The range 
  *                     is 0-15. Java layer is responsible for the 
  *                     following: 
  *                     - neither the channel nor the whole player is already a
  *                     part of the SoundSource3D
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Some error occurred
  * @retval JAVACALL_NOT_IMPLEMENTED    Adding MIDI channels is not supported
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL or 
  *                                     the channel is not in 
  *                                     the range 0-15
  * @see javacall_audio3d_soundsource3d_t
  */
javacall_result javacall_audio3d_soundsource3d_add_midi_channel ( 
    javacall_audio3d_soundsource3d_t *soundsource3d, 
    javacall_handle handle, int channel );

/**
  * The function corresponding to SoundSource3D.removeMIDIChannel()
  * method of AMMS Java API, see JSR-234 Spec
  * 
  * Remove MIDI channel from the SoundSource3D
  * 
  * @param soundsource3d    pointer to the SoundSource3D.
  * @param handle       Handle of native player. Java layer is responsible
  *                     for the following: 
  *                     - the player is a MIDI player,
  *                     - the channel is a part of the SoundSource3D, 
  *                     - the SoundSource3D currently is containing no Player
  *                     in PREFETCHED or STARTED state,
  *                     - the Player to be removed is not in PREFETCHED or
  *                     STARTED state,
  *                     - the player handle is not NULL
  *                     See JSR-135 Spec for Player states explanation
  *                     @see javacall_media_acquire_device
  *                     @see javacall_media_release_device
  *                     @see javacall_media_start
  *                     @see javacall_media_stop
  * @param channel      the channel of the player to be added. The range 
  *                     is 0-15. Java layer is responsible for the 
  *                     following: 
  *                     - the channel is a part of the SoundSource3D
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  * @see javacall_audio3d_soundsource3d_t
  */
javacall_result javacall_audio3d_soundsource3d_remove_midi_channel ( 
    javacall_audio3d_soundsource3d_t *soundsource3d, 
    javacall_handle handle, int channel );

/**
  * The function corresponding to SoundSource3D.getControl()
  * method of MMAPI, see JSR-135 Spec
  *
  * Return a Control to control the specified SoundSource3D
  *
  * @param soundsource3d     pointer to the soundsource3d.
  * @param type     the type of the requested Control (from the enum)
  * 
  * @return pointer to the requested Control structure or NULL if this
  *         type of control is not supported
  * @see javacall_audio3d_soundsource3d_t
  */
javacall_amms_control_t* javacall_audio3d_soundsource3d_get_control (
    javacall_audio3d_soundsource3d_t *soundsource3d,
    javacall_amms_control_type_enum_t type );

/**
  * The function corresponding to SoundSource3D.getControls()
  * method of MMAPI, see JSR-135 Spec
  *
  * Obtain the array of the supported Controls which can be used to 
  *  control the specified SoundSource3D
  * 
  * @param soundsource3d     pointer to the soundsource3d. 
  * @param controls_number  pointer to return the returned array length
  * 
  * @return array of the supported Controls, which length is return through
  *         the controls_number parameter. The array of Control's 
  *         returned will not contain any duplicates. And the list will
  *         not change over time. If no Control is supported, NULL is 
  *         returned.
  * @see javacall_audio3d_soundsource3d_t
  */
javacall_amms_control_t* javacall_audio3d_soundsource3d_get_controls (
    javacall_audio3d_soundsource3d_t *soundsource3d,
    /*OUT*/ int *controls_number );

/** @} */

/**
 * @addtogroup jsrOptionalJSR234SoundEffects
 * @ingroup JSR234
 * @{
 */

/**
  * The function corresponding to EffectModule.addPlayer() method
  * of AMMS Java API, see JSR-234 Spec
  *
  * Add a Player to the EffectModule
  * 
  * @param module   pointer to the EffectModule.
  * @param handle   Handle of native player. Java layer is responsible
  *                 for the following: 
  *                 - neither the player nor any of its channels already 
  *                 belong to the EffectModule,
  *                 - none of the players that currently belong to 
  *                 the EffectModule are in PREFETCHED or STARTED state,
  *                 - the player to be added is in neither PREFETCHED nor
  *                 STARTED state,
  *                 - the player handle is not NULL
  *                 See JSR-135 Spec for Player states explanation
  *                 @see javacall_media_acquire_device
  *                 @see javacall_media_release_device
  *                 @see javacall_media_start
  *                 @see javacall_media_stop
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_NOT_IMPLEMENTED    The addition is not supported 
  *                                     by the implementation. (For 
  *                                     example, if the implementation 
  *                                     does not support adding the same 
  *                                     Player to multiple modules or if 
  *                                     the implementation does not 
  *                                     support the type of the Player.) 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  */
javacall_result
    javacall_amms_effectmodule_add_player ( 
        javacall_amms_effectmodule_t *module, 
        javacall_handle               handle );

/**
  * The function corresponding to EffectModule.removePlayer() method
  * of AMMS Java API, see JSR-234 Spec
  *
  * Remove a Player from the EffectModule
  * 
  * @param module   pointer to the EffectModule.
  * @param handle   Handle of native player. Java layer is responsible
  *                 for the following: 
  *                 - the player is currently being a part the EffectModule,
  *                 - the EffectModule currently contains no Players in
  *                 PREFETCHED or STARTED state,
  *                 - the Player to be removed is not in PREFETCHED or STARTED
  *                 state,
  *                 - the player handle is not NULL
  *                 See JSR-135 Spec for Player states explanation
  *                 @see javacall_media_acquire_device
  *                 @see javacall_media_release_device
  *                 @see javacall_media_start
  *                 @see javacall_media_stop
  *                 
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  * @see javacall_amms_effectmodule_t
  */
javacall_result
    javacall_amms_effectmodule_remove_player ( 
        javacall_amms_effectmodule_t *module,
        javacall_handle               handle );

/**
  * The function corresponding to EffectModule.addMIDIChannel()
  * method of AMMS Java API, see JSR-234 Spec
  * 
  * Add MIDI channel to the EffectModule
  * 
  * @param module       pointer to the EffectModule.
  * @param handle       Handle of native player. Java layer is responsible
  *                     for the following: 
  *                     - the player is a MIDI player,
  *                     - neither the channel nor the whole player is already a
  *                     part of the EffectModule, 
  *                     - the EffectModule currently contains no Player in
  *                     PREFETCHED or STARTED state,
  *                     - the Player to be added is not in PREFETCHED or
  *                     STARTED state,
  *                     - the player is not NULL
  *                     See JSR-135 Spec for Player states explanation
  *                     @see javacall_media_acquire_device
  *                     @see javacall_media_release_device
  *                     @see javacall_media_start
  *                     @see javacall_media_stop
  * @param channel      the channel of the player to be added. The range 
  *                     is 0-15. Java layer is responsible for the 
  *                     following: 
  *                     - neither the channel nor the whole player is already a
  *                     part of the EffectModule
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Some error occurred
  * @retval JAVACALL_NOT_IMPLEMENTED    Adding MIDI channels is not supported
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL or 
  *                                     the channel is not in 
  *                                     the range 0-15
  * @see javacall_amms_effectmodule_t
  */
javacall_result
    javacall_amms_effectmodule_add_midi_channel ( 
        javacall_amms_effectmodule_t *module, 
        javacall_handle               handle,
        int                           channel );

/**
  * The function corresponding to EffectModule.removeMIDIChannel()
  * method of AMMS Java API, see JSR-234 Spec
  * 
  * Remove MIDI channel from the EffectModule
  * 
  * @param module       pointer to the EffectModule.
  * @param handle       Handle of native player. Java layer is responsible
  *                     for the following: 
  *                     - the player is a MIDI player,
  *                     - the channel is a part of the EffectModule, 
  *                     - the EffectModule currently contains no Player
  *                     in PREFETCHED or STARTED state,
  *                     - the Player to be removed is not in PREFETCHED or
  *                     STARTED state,
  *                     - the player handle is not NULL
  *                     See JSR-135 Spec for Player states explanation
  *                     @see javacall_media_acquire_device
  *                     @see javacall_media_release_device
  *                     @see javacall_media_start
  *                     @see javacall_media_stop
  * @param channel      the channel of the player to be added. The range 
  *                     is 0-15. Java layer is responsible for the 
  *                     following: 
  *                     - the channel is a part of the EffectModule
  * 
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail, 
  * @retval JAVACALL_INVALID_ARGUMENT   if the player handle is NULL
  * @see javacall_amms_effectmodule_t
  */
javacall_result
    javacall_amms_effectmodule_remove_midi_channel ( 
        javacall_amms_effectmodule_t *module, 
        javacall_handle               handle,
        int                           channel );

/**
  * The function corresponding to EffectModule.getControl()
  * method of MMAPI, see JSR-135 Spec
  *
  * Return a Control to control the specified EffectModule
  *
  * @param module   pointer to the EffectModule.
  * @param type     the type of the requested Control (from the enum)
  * 
  * @return pointer to the requested Control structure or NULL if this
  *         type of control is not supported
  * @see javacall_amms_effectmodule_t
  */
javacall_amms_control_t*
    javacall_amms_effectmodule_get_control (
        javacall_amms_effectmodule_t     *module,
        javacall_amms_control_type_enum_t type );

/**
  * The function corresponding to EffectModule.getControls()
  * method of MMAPI, see JSR-135 Spec
  *
  * Obtain the array of the supported Controls which can be used to 
  *  control the specified EffectModule
  * 
  * @param module           pointer to the EffectModule. 
  * @param controls_number  pointer to return the returned array length
  * 
  * @return array of the supported Controls, which length is return through
  *         the controls_number parameter. The array of Control's 
  *         returned will not contain any duplicates. And the list will
  *         not change over time. If no Control is supported, NULL is 
  *         returned.
  * @see javacall_amms_effectmodule_t
  */
javacall_amms_control_t*
    javacall_amms_effectmodule_get_controls (
        javacall_amms_effectmodule_t *module,
        /*OUT*/ int                  *controls_number );

/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Audio3D
 * @ingroup JSR234
 * @{
 */

/** 
  * The function corresponding to 
  * ReverbControl.getReverbLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param reverb_control       pointer to the ReverbControl 
  * @return     current level of the reverberation. For detailed
  *             explanation see JSR-234 Spec, ReverbControl explanation
  */
int javacall_music_reverb_control_get_reverb_level (
    javacall_music_reverb_control_t *reverb_control );

/** 
  * The function corresponding to 
  * ReverbControl.setReverbLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param reverb_control       pointer to the ReverbControl 
  * @param level            this parameter has the same meaning as 
  *                         "level" parameter of 
  *                         ReverbControl.setReverbLevel() 
  *                         method of AMMS
  *                         Java API. See JSR-234 Spec
  *                         N.B.: this value should not be positive,
  *                         because it denotes the level in millibels!
  *                         Java layer is responsible for this value
  *                         being less than or equal to zero.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_music_reverb_control_set_reverb_level (
    javacall_music_reverb_control_t *reverb_control,
    int level );

/** 
  * The function corresponding to 
  * ReverbControl.getReverbTime()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param reverb_control       pointer to the ReverbControl 
  * @return     current reverberation time or -1 if reporting the reverb
  *             time is not supported. For detailed
  *             explanation see JSR-234 Spec, ReverbControl explanation
  */
int javacall_music_reverb_control_get_reverb_time (
    javacall_music_reverb_control_t *reverb_control );

/** 
  * The function corresponding to 
  * ReverbControl.setReverbTime()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param reverb_control       pointer to the ReverbControl 
  * @param time             this parameter has the same meaning as 
  *                         "time" parameter of 
  *                         ReverbControl.setReverbTime() 
  *                         method of AMMS
  *                         Java API. See JSR-234 Spec
  *                         Java layer is responsible for this value
  *                         being greater than or equal to zero.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  * @retval JAVACALL_NOT_IMPLEMENTED    if changing the Reverberation Time
  *                                     is not supported by the device
  */
javacall_result javacall_music_reverb_control_set_reverb_time (
    javacall_music_reverb_control_t *reverb_control,
    int time );

/**
  * The function corresponding to ReverbControl.getPreset() 
  * method of the AMMS Java API. See JSR-234 Spec
  * 
  * Get the current preset of the ReverbControl.
  * 
  * @param reverb   pointer to the ReverbControl.
  * @return null-terminated JavaCall-unicode string naming the currently
  *         set preset or NULL if no preset
  *         is set at the moment. For the detailed explanation of presets
  *         see JSR-234 Spec, ReverbControl interface description.
  */
const javacall_utf16* javacall_music_reverb_control_get_preset (
    javacall_music_reverb_control_t *reverb );

/**
  * The function corresponding to 
  * ReverbControl.getPresetNames() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Get the available preset names.
  * 
  * @param reverb   pointer to the ReverbControl.
  * @param number_of_presets    pointer to return the returned array
  *                             length
  * @return the names of all the available preset modes as an array of
  *         null-terminated JavaCall-unicode strings. The length of the
  *         array is returned by the
  *         "number_of_preset" parameter. For detailed explanation of
  *         presets see JSR-234 Spec, ReverbControl interface description.
  *         
  */
const javacall_utf16 **javacall_music_reverb_control_get_preset_names (
    javacall_music_reverb_control_t *reverb,
    /*OUT*/ int *number_of_presets );

/**
  * The function corresponding to 
  * ReverbControl.setPreset() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Set the effect according to the given preset
  * 
  * @param reverb   pointer to the ReverbControl.
  * @param preset_name  the preset to be set. A null-terminated
  *                     JavaCall-unicode string.
  *                     For detailed explanation of presets see JSR-234
  *                     Spec, ReverbControl interface description.
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if the preset is not available or it is
  *                                     NULL
  */
javacall_result javacall_music_reverb_control_set_preset (
    javacall_music_reverb_control_t *reverb,
    const javacall_utf16* preset_name );

/**
  * The function corresponding to ReverbControl.getScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the current scope of the effect.
  * For detailed explanation of effect scope see JSR-234 Spec,
  * ReverbControl interface description.
  * @see javacall_music_reverb_control_scope_enum_t
  * 
  * @param reverb   pointer to the ReverbControl.
  * @return the current scope of the effect
  * 
  */
javacall_amms_effect_control_scope_enum_t 
    javacall_music_reverb_control_get_scope (
        javacall_music_reverb_control_t *reverb );

/**
  * The function corresponding to ReverbControl.setScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the scope of the effect. 
  * For detailed explanation of effect scope see JSR-234 Spec,
  * ReverbControl interface description.
  * @see javacall_amms_effect_control_scope_enum_t
  * 
  * @param reverb   pointer to the ReverbControl.
  * @param scope                the scope to be set for this effect
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if the scope passed is not supported
  * 
  */
javacall_result javacall_music_reverb_control_set_scope (
    javacall_music_reverb_control_t *reverb,
    javacall_amms_effect_control_scope_enum_t scope );

/**
  * The function corresponding to ReverbControl.isEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param reverb   pointer to the ReverbControl.
  * @retval JAVACALL_TRUE       if the effect is enabled
  * @retval JAVACALL_FALSE      if the effect is not enabled
  * 
  */
javacall_bool javacall_music_reverb_control_is_enabled (
    javacall_music_reverb_control_t *reverb );

/**
  * The function corresponding to ReverbControl.setEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Enable or disable the effect 
  * (according to the boolean parameter passed)
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param reverb   pointer to the ReverbControl.
  * @param enabled              pass JAVACALL_TRUE to enable the effect or
  *                             JAVACALL_FALSE to disable it
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enabled" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result javacall_music_reverb_control_set_enabled (
    javacall_music_reverb_control_t *reverb,
    javacall_bool enabled );

/**
  * The function corresponding to ReverbControl.isEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ReverbControl interface description
  * @see javacall_bool
  * 
  * @param reverb   pointer to the ReverbControl.
  * @retval JAVACALL_TRUE       if the effect is enforced
  * @retval JAVACALL_FALSE      if the effect is not enforced
  * 
  */
javacall_bool javacall_music_reverb_control_is_enforced (
    javacall_music_reverb_control_t *reverb );

/**
  * The function corresponding to ReverbControl.setEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the effect enforced or not enforced 
  * (according to the boolean parameter passed)
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ReverbControl interface description
  * @see javacall_bool
  * 
  * @param reverb   pointer to the ReverbControl.
  * @param enforced             pass JAVACALL_TRUE to set the effect
  *                             enforced or JAVACALL_FALSE to set it not
  *                             enforced
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enforced" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result javacall_music_reverb_control_set_enforced (
    javacall_music_reverb_control_t *reverb,
    javacall_bool enforced );

/** @} */

/**
 * @addtogroup jsrOptionalJSR234SoundEffects
 * @ingroup JSR234
 * @{
 */

/**
  * The function corresponding to AudioVirtualizerControl.getPreset() 
  * method of the AMMS Java API. See JSR-234 Spec
  * 
  * Get the current preset of the AudioVirtualizerControl.
  * 
  * @param  av_ctl   pointer to the AudioVirtualizerControl.
  * @return null-terminated JavaCall-unicode string naming the currently
  *         set preset or NULL if no preset
  *         is set at the moment. For the detailed explanation of presets
  *         see JSR-234 Spec, AudioVirtualizerControl interface description.
  */
const javacall_utf16*
    javacall_amms_audiovirtualizer_control_get_preset (
        javacall_amms_audiovirtualizer_control_t *av_ctl );

/**
  * The function corresponding to 
  * AudioVirtualizerControl.getPresetNames() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Get the available preset names.
  * 
  * @param av_ctl               pointer to the AudioVirtualizerControl.
  * @param number_of_presets    pointer to return the returned array
  *                             length
  * @return the names of all the available preset modes as an array of
  *         null-terminated JavaCall-unicode strings. The length of the
  *         array is returned by the
  *         "number_of_preset" parameter. For detailed explanation of
  *         presets see JSR-234 Spec, AudioVirtualizerControl
  *         interface description.
  */
const javacall_utf16**
    javacall_amms_audiovirtualizer_control_get_preset_names (
        javacall_amms_audiovirtualizer_control_t* av_ctl,
        /*OUT*/ int*                              number_of_presets );

/**
  * The function corresponding to 
  * AudioVirtualizerControl.setPreset() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Set the effect according to the given preset
  * 
  * @param av_ctl       pointer to the AudioVirtualizerControl.
  * @param preset_name  the preset to be set. A null-terminated
  *                     JavaCall-unicode string.
  *                     For detailed explanation of presets see JSR-234
  *                     Spec, AudioVirtualizerControl interface description.
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if the preset is not available or it is
  *                                     NULL
  */
javacall_result
    javacall_amms_audiovirtualizer_control_set_preset (
        javacall_amms_audiovirtualizer_control_t* av_ctl,
        const javacall_utf16*                     preset_name );

/**
  * The function corresponding to AudioVirtualizerControl.getScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the current scope of the effect.
  * For detailed explanation of effect scope see JSR-234 Spec,
  * AudioVirtualizerControl interface description.
  * @see javacall_amms_audiovirtualizer_control_scope_enum_t
  * 
  * @param  av_ctl   pointer to the AudioVirtualizerControl.
  * @return the current scope of the effect
  * 
  */
javacall_amms_effect_control_scope_enum_t 
    javacall_amms_audiovirtualizer_control_get_scope (
        javacall_amms_audiovirtualizer_control_t* av_ctl );

/**
  * The function corresponding to AudioVirtualizerControl.setScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the scope of the effect. 
  * For detailed explanation of effect scope see JSR-234 Spec,
  * AudioVirtualizerControl interface description.
  * @see javacall_amms_effect_control_scope_enum_t
  * 
  * @param av_ctl   pointer to the AudioVirtualizerControl.
  * @param scope    the scope to be set for this effect
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if the scope passed is not supported
  * 
  */
javacall_result
    javacall_amms_audiovirtualizer_control_set_scope (
        javacall_amms_audiovirtualizer_control_t* av_ctl,
        javacall_amms_effect_control_scope_enum_t scope );

/**
  * The function corresponding to AudioVirtualizerControl.isEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param  av_ctl              pointer to the AudioVirtualizerControl.
  * @retval JAVACALL_TRUE       if the effect is enabled
  * @retval JAVACALL_FALSE      if the effect is not enabled
  * 
  */
javacall_bool
    javacall_amms_audiovirtualizer_control_is_enabled (
        javacall_amms_audiovirtualizer_control_t* av_ctl );

/**
  * The function corresponding to AudioVirtualizerControl.setEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Enable or disable the effect 
  * (according to the boolean parameter passed)
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param av_ctl               pointer to the AudioVirtualizerControl.
  * @param enabled              pass JAVACALL_TRUE to enable the effect or
  *                             JAVACALL_FALSE to disable it
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enabled" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_audiovirtualizer_control_set_enabled (
        javacall_amms_audiovirtualizer_control_t* av_ctl,
        javacall_bool                             enabled );

/**
  * The function corresponding to AudioVirtualizerControl.isEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, AudioVirtualizerControl interface description
  * @see javacall_bool
  * 
  * @param  av_ctl              pointer to the AudioVirtualizerControl.
  * @retval JAVACALL_TRUE       if the effect is enforced
  * @retval JAVACALL_FALSE      if the effect is not enforced
  * 
  */
javacall_bool
    javacall_amms_audiovirtualizer_control_is_enforced (
        javacall_amms_audiovirtualizer_control_t* av_ctl );

/**
  * The function corresponding to AudioVirtualizerControl.setEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the effect enforced or not enforced 
  * (according to the boolean parameter passed)
  * For explanation of an effect being enforced
  * see JSR-234 Spec, AudioVirtualizerControl interface description
  * @see javacall_bool
  * 
  * @param av_ctl   pointer to the AudioVirtualizerControl.
  * @param enforced             pass JAVACALL_TRUE to set the effect
  *                             enforced or JAVACALL_FALSE to set it not
  *                             enforced
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enforced" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_audiovirtualizer_control_set_enforced (
        javacall_amms_audiovirtualizer_control_t* av_ctl,
        javacall_bool                             enforced );

/**
  * The function corresponding to ChorusControl.getPreset() 
  * method of the AMMS Java API. See JSR-234 Spec
  * 
  * Get the current preset of the ChorusControl.
  * 
  * @param  chorus   pointer to the ChorusControl.
  * @return null-terminated JavaCall-unicode string naming the currently
  *         set preset or NULL if no preset
  *         is set at the moment. For the detailed explanation of presets
  *         see JSR-234 Spec, ChorusControl interface description.
  */
const javacall_utf16*
    javacall_amms_chorus_control_get_preset (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to 
  * ChorusControl.getPresetNames() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Get the available preset names.
  * 
  * @param chorus   pointer to the ChorusControl.
  * @param number_of_presets    pointer to return the returned array
  *                             length
  * @return the names of all the available preset modes as an array of
  *         null-terminated JavaCall-unicode strings. The length of the
  *         array is returned by the
  *         "number_of_preset" parameter. For detailed explanation of
  *         presets see JSR-234 Spec, ChorusControl interface description.
  *         
  */
const javacall_utf16**
    javacall_amms_chorus_control_get_preset_names (
        javacall_amms_chorus_control_t* chorus,
        /*OUT*/ int*                    number_of_presets );

/**
  * The function corresponding to 
  * ChorusControl.setPreset() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Set the effect according to the given preset
  * 
  * @param chorus   pointer to the ChorusControl.
  * @param preset_name  the preset to be set. A null-terminated
  *                     JavaCall-unicode string.
  *                     For detailed explanation of presets see JSR-234
  *                     Spec, ChorusControl interface description.
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if the preset is not available or it is
  *                                     NULL
  */
javacall_result
    javacall_amms_chorus_control_set_preset (
        javacall_amms_chorus_control_t* chorus,
        const javacall_utf16*           preset_name );

/**
  * The function corresponding to ChorusControl.getScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the current scope of the effect.
  * For detailed explanation of effect scope see JSR-234 Spec,
  * ChorusControl interface description.
  * @see javacall_amms_chorus_control_scope_enum_t
  * 
  * @param chorus   pointer to the ChorusControl.
  * @return the current scope of the effect
  * 
  */
javacall_amms_effect_control_scope_enum_t
    javacall_amms_chorus_control_get_scope (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.setScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the scope of the effect. 
  * For detailed explanation of effect scope see JSR-234 Spec,
  * ChorusControl interface description.
  * @see javacall_amms_effect_control_scope_enum_t
  * 
  * @param chorus   pointer to the ChorusControl.
  * @param scope                the scope to be set for this effect
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if the scope passed is not supported
  * 
  */
javacall_result
    javacall_amms_chorus_control_set_scope (
        javacall_amms_chorus_control_t*           chorus,
        javacall_amms_effect_control_scope_enum_t scope );

/**
  * The function corresponding to ChorusControl.isEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param chorus   pointer to the ChorusControl.
  * @retval JAVACALL_TRUE       if the effect is enabled
  * @retval JAVACALL_FALSE      if the effect is not enabled
  * 
  */
javacall_bool
    javacall_amms_chorus_control_is_enabled (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.setEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Enable or disable the effect 
  * (according to the boolean parameter passed)
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param chorus   pointer to the ChorusControl.
  * @param enabled              pass JAVACALL_TRUE to enable the effect or
  *                             JAVACALL_FALSE to disable it
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enabled" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_chorus_control_set_enabled (
        javacall_amms_chorus_control_t* chorus,
        javacall_bool                   enabled );

/**
  * The function corresponding to ChorusControl.isEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * @see javacall_bool
  * 
  * @param chorus   pointer to the ChorusControl.
  * @retval JAVACALL_TRUE       if the effect is enforced
  * @retval JAVACALL_FALSE      if the effect is not enforced
  * 
  */
javacall_bool
    javacall_amms_chorus_control_is_enforced (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.setEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the effect enforced or not enforced 
  * (according to the boolean parameter passed)
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * @see javacall_bool
  * 
  * @param chorus   pointer to the ChorusControl.
  * @param enforced             pass JAVACALL_TRUE to set the effect
  *                             enforced or JAVACALL_FALSE to set it not
  *                             enforced
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enforced" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_chorus_control_set_enforced (
        javacall_amms_chorus_control_t* chorus,
        javacall_bool                   enforced );

/**
  * The function corresponding to ChorusControl.getAverageDelay
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the average delay.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The current average delay in microseconds.
  */
int javacall_amms_chorus_control_get_average_delay (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.getMaxAverageDelay
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the maximum supported average delay.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The maximum supported average delay in microseconds.
  */
int javacall_amms_chorus_control_get_max_average_delay (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.getMaxModulationDepth
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the maximum supported delay modulation depth.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The maximum supported delay modulation amplitude
  *                 (peak-to-zero depth) in percents of the average delay.
  */
int javacall_amms_chorus_control_get_max_modulation_depth (
        javacall_amms_chorus_control_t* chorus );
      
/**
  * The function corresponding to ChorusControl.getMaxModulationRate
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the maximum supported delay modulation rate.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The maximum supported delay modulation rate in mHz.
  */
int javacall_amms_chorus_control_get_max_modulation_rate (
        javacall_amms_chorus_control_t* chorus );

      
/**
  * The function corresponding to ChorusControl.getMinModulationRate
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the minimum supported delay modulation rate.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The maximum supported delay modulation rate in mHz.
  */
int javacall_amms_chorus_control_get_min_modulation_rate (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.getModulationDepth
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the current delay modulation depth.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The current delay modulation amplitude (peak-to-zero depth)
  *                 in percents of the current average delay.
  */
int javacall_amms_chorus_control_get_modulation_depth (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.getModulationRate
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the delay modulation rate.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The current delay modulation rate in mHz.
  */
int javacall_amms_chorus_control_get_modulation_rate (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.getWetLevel
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the effect's wet level.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @return         The effect wet level in percents
  */
int javacall_amms_chorus_control_get_wet_level (
        javacall_amms_chorus_control_t* chorus );

/**
  * The function corresponding to ChorusControl.setAverageDelay
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the average delay.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @param delay    The new average delay in microseconds.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If delay < 0 or 
  *                                     delay > getMaxAverageDelay()
  */
javacall_result
    javacall_amms_chorus_control_set_average_delay (
        javacall_amms_chorus_control_t* chorus, 
        int                             delay );

/**
  * The function corresponding to ChorusControl.setModulationDepth
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the delay modulation depth as a percentage of the average delay.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus     Pointer to the ChorusControl.
  * @param percentage The new delay modulation amplitude (peak-to-zero depth)
  *                   in percents of the average delay.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If depth < 0 or depth > getMaxModulationDepth()
  */
javacall_result
    javacall_amms_chorus_control_set_modulation_depth (
        javacall_amms_chorus_control_t* chorus,
        int                             percentage );

/**
  * The function corresponding to ChorusControl.setModulationRate
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the delay modulation rate.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus   Pointer to the ChorusControl.
  * @param rate     The new delay modulation rate in mHz.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If rate < getMinModulationRate() or 
  *                                     rate > getMaxModulationRate()
  */
javacall_result
    javacall_amms_chorus_control_set_modulation_rate(
        javacall_amms_chorus_control_t* chorus,
        int                             rate );

/**
  * The function corresponding to ChorusControl.setWetLevel
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the effect's wet level.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, ChorusControl interface description
  * 
  * @param chorus    Pointer to the ChorusControl.
  * @param level     The new effect wet level for the effect in percents.
  * @param new_level Receives the value that was actually set
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If level < 0 or level > 100
  */
javacall_result
    javacall_amms_chorus_control_set_wet_level(
        javacall_amms_chorus_control_t* chorus,
        int                             level,
        /*OUT*/int*                     new_level );

/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Music
 * @ingroup JSR234
 * @{
 */

/**
  * The function corresponding to EqualizerControl.getPreset() 
  * method of the AMMS Java API. See JSR-234 Spec
  * 
  * Get the current preset of the EqualizerControl.
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @return null-terminated JavaCall-unicode string naming the currently
  *         set preset or NULL if no preset
  *         is set at the moment. For the detailed explanation of presets
  *         see JSR-234 Spec, EqualizerControl interface description.
  */
const javacall_utf16*
    javacall_amms_equalizer_control_get_preset (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to 
  * EqualizerControl.getPresetNames() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Get the available preset names.
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @param number_of_presets    pointer to return the returned array
  *                             length
  * @return the names of all the available preset modes as an array of
  *         null-terminated JavaCall-unicode strings. The length of the
  *         array is returned by the
  *         "number_of_preset" parameter. For detailed explanation of
  *         presets see JSR-234 Spec, EqualizerControl interface description.
  *         
  */
const javacall_utf16**
    javacall_amms_equalizer_control_get_preset_names (
        javacall_amms_equalizer_control_t*  equalizer,
        /*OUT*/ int*                        number_of_presets );

/**
  * The function corresponding to 
  * EqualizerControl.setPreset() method of the AMMS Java API. 
  * See JSR-234 Spec
  * 
  * Set the effect according to the given preset
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @param preset_name  the preset to be set. A null-terminated
  *                     JavaCall-unicode string.
  *                     For detailed explanation of presets see JSR-234
  *                     Spec, EqualizerControl interface description.
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if the preset is not available or it is
  *                                     NULL
  */
javacall_result
    javacall_amms_equalizer_control_set_preset (
        javacall_amms_equalizer_control_t* equalizer,
        const javacall_utf16*              preset_name );

/**
  * The function corresponding to EqualizerControl.getScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the current scope of the effect.
  * For detailed explanation of effect scope see JSR-234 Spec,
  * EqualizerControl interface description.
  * @see javacall_amms_equalizer_control_scope_enum_t
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @return the current scope of the effect
  * 
  */
javacall_amms_effect_control_scope_enum_t 
    javacall_amms_equalizer_control_get_scope (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.setScope()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the scope of the effect. 
  * For detailed explanation of effect scope see JSR-234 Spec,
  * EqualizerControl interface description.
  * @see javacall_amms_effect_control_scope_enum_t
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @param scope                the scope to be set for this effect
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_NOT_IMPLEMENTED    if the scope passed is not supported
  * 
  */
javacall_result
    javacall_amms_equalizer_control_set_scope (
        javacall_amms_equalizer_control_t*        equalizer,
        javacall_amms_effect_control_scope_enum_t scope );

/**
  * The function corresponding to EqualizerControl.isEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @retval JAVACALL_TRUE       if the effect is enabled
  * @retval JAVACALL_FALSE      if the effect is not enabled
  * 
  */
javacall_bool
    javacall_amms_equalizer_control_is_enabled (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.setEnabled()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Enable or disable the effect 
  * (according to the boolean parameter passed)
  * An effect should be enabled in order to take any effect
  * @see javacall_bool
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @param enabled              pass JAVACALL_TRUE to enable the effect or
  *                             JAVACALL_FALSE to disable it
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enabled" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_equalizer_control_set_enabled (
        javacall_amms_equalizer_control_t* equalizer,
        javacall_bool                      enabled );

/**
  * The function corresponding to EqualizerControl.isEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Determine whether the effect is enabled or not.
  * For explanation of an effect being enforced
  * see JSR-234 Spec, EqualizerControl interface description
  * @see javacall_bool
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @retval JAVACALL_TRUE       if the effect is enforced
  * @retval JAVACALL_FALSE      if the effect is not enforced
  * 
  */
javacall_bool
    javacall_amms_equalizer_control_is_enforced (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.setEnforced()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Set the effect enforced or not enforced 
  * (according to the boolean parameter passed)
  * For explanation of an effect being enforced
  * see JSR-234 Spec, EqualizerControl interface description
  * @see javacall_bool
  * 
  * @param equalizer   pointer to the EqualizerControl.
  * @param enforced             pass JAVACALL_TRUE to set the effect
  *                             enforced or JAVACALL_FALSE to set it not
  *                             enforced
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if "enforced" parameter is neither
  *                                     JAVACALL_TRUE nor JAVACALL_FALSE
  */
javacall_result
    javacall_amms_equalizer_control_set_enforced (
        javacall_amms_equalizer_control_t* equalizer,
        javacall_bool                      enforced );


/**
  * The function corresponding to EqualizerControl.getMinBandLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the minimum band level supported.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   pointer to the EqualizerControl
  * @return             minimum band level in millibels
  */
int javacall_amms_equalizer_control_get_min_band_level (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.getMaxBandLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Returns the maximum band level supported.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   pointer to the EqualizerControl
  * @return             maximum band level in millibels
  */
int javacall_amms_equalizer_control_get_max_band_level (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.setBandLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the given equalizer band to the given gain value.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @param  band        The frequency band that will have the new gain.
  *                     The numbering of the bands starts from 0
  *                     and ends at (getNumberOfBands() - 1).
  * @param  level       The new gain in millibels.
  *                     getMinBandLevel() and getMaxBandLevel()
  *                     will define the maximum and minimum values
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT When the given band or level
  *                                   is out of range
  */
javacall_result 
    javacall_amms_equalizer_control_set_band_level (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        int                                level );

/**
  * The function corresponding to EqualizerControl.getBandLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the gain set for the given equalizer band.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @param  band        The frequency band whose gain is asked.
  *                     The numbering of the bands starts from 0
  *                     and ends at (getNumberOfBands() - 1).
  * @param  level       Receives the gain set for the given band in millibels.
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT When the given band is out of range
  */
javacall_result 
    javacall_amms_equalizer_control_get_band_level (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        /*OUT*/ int*                       level );

/**
  * The function corresponding to EqualizerControl.getNumberOfBands()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the number of frequency bands that the equalizer supports.
  * A valid equalizer MUST have at least two bands.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   pointer to the EqualizerControl
  * @return             number of frequency bands supported
  */
int javacall_amms_equalizer_control_get_number_of_bands (
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.getCenterFreq()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the center frequency of the given band.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @param  band        The frequency band whose gain is asked.
  *                     The numbering of the bands starts from 0
  *                     and ends at (getNumberOfBands() - 1).
  * @param  freq        Receives the center frequency in milliHertz
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT When the given band is out of range
  */
javacall_result 
    javacall_amms_equalizer_control_get_center_freq (
        javacall_amms_equalizer_control_t* equalizer,
        int                                band,
        /*OUT*/ int*                       freq );

/**
  * The function corresponding to EqualizerControl.getBand()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the band that has the most effect on the given frequency.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   pointer to the EqualizerControl
  * @param  freq        the frequency in milliHertz which is to be
  *                     equalized via the returned band
  * @return             the frequency band that has most effect on
  *                     the given frequency or -1 if no band has
  *                     effect on the given frequency
  */
int javacall_amms_equalizer_control_get_band (
        javacall_amms_equalizer_control_t* equalizer,
        int                                freq );

/**
  * The function corresponding to EqualizerControl.setBass()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the bass level using a linear point scale with values between 0 and 100.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @param  level       The new level on a linear point scale
  *                     that will be set to the bass band
  * @param  new_level   Receives the level that was actually set
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT When the given level is below 0 or above 100
  */
javacall_result 
    javacall_amms_equalizer_control_set_bass (
        javacall_amms_equalizer_control_t* equalizer,
        int                                level,
        /*OUT*/int*                        new_level );

/**
  * The function corresponding to EqualizerControl.setTreble()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Sets the treble level using a linear point scale with values between 0 and 100.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @param  level       The new level on a linear point scale
  *                     that will be set to the treble band
  * @param  new_level   Receives the level that was actually set
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT When the given level is below 0 or above 100
  */
javacall_result 
    javacall_amms_equalizer_control_set_treble (
        javacall_amms_equalizer_control_t* equalizer,
        int                                level,
        /*OUT*/int*                        new_level );

/**
  * The function corresponding to EqualizerControl.getBass()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the bass level.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @return The current level that is set to the bass band,
  *         or -1 if the bass level hasn't been set.
  */
int javacall_amms_equalizer_control_get_bass(
        javacall_amms_equalizer_control_t* equalizer );

/**
  * The function corresponding to EqualizerControl.getTreble()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * Gets the treble level.
  * For explanation see JSR-234 Spec,
  * EqualizerControl interface description
  * 
  * @param  equalizer   Pointer to the EqualizerControl
  * @return The current level that is set to the treble band,
  *         or -1 if the treble level hasn't been set.
  */
int javacall_amms_equalizer_control_get_treble (
        javacall_amms_equalizer_control_t* equalizer );

/** @} */

/**
 * @addtogroup jsrOptionalJSR234SoundEffects
 * @ingroup JSR234
 * @{
 */

/**
 * @def JAVACALL_AMMS_REVERB_SOURCE_CONTROL_DISCONNECT
 * Special value used to disconnect sound source
 * controlled by Reverb Source Control from reverberator
 */
#define JAVACALL_AMMS_REVERB_SOURCE_CONTROL_DISCONNECT      INT_MAX

/** 
  * The function corresponding to 
  * ReverbSourceControl.getRoomLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param  rsource pointer to the ReverbSourceControl
  * @return    the object specific level for the reverberant sound,
  *            or DISCONNECT
  *            For explanation what is 'Room Level', see JSR-234 Spec,
  *            ReverbSourceControl description
  */
int javacall_amms_reverb_source_control_get_room_level(
        javacall_amms_reverb_source_control_t*  rsource );    

/** 
  * The function corresponding to 
  * ReverbSourceControl.getRoomLevel()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param  rsource pointer to the ReverbSourceControl
  * @param  level   the object specific level for the reverberant sound.
  *                 For explanation what is 'Room Level', see JSR-234 Spec,
  *                 ReverbSourceControl description.
  *                 Level must be <= 0 or DISCONNECT
  * @retval JAVACALL_OK               Success
  * @retval JAVACALL_FAIL             Fail
  * @retval JAVACALL_INVALID_ARGUMENT If level is not DISCONNECT and level is > 0
  */
javacall_result
    javacall_amms_reverb_source_control_set_room_level(
        javacall_amms_reverb_source_control_t*  rsource,
        int                                     level );
    
/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Audio3D
 * @ingroup JSR234
 * @{
 */

/** 
  * The function corresponding to 
  * DistanceAttenuationControl.setParameters()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param dac             pointer to the DistanceAttenuationControl
  * @param minDistance      this parameter has the same meaning as 
  *                         minDistance parameter of 
  *                         DistanceAttenuationControl.setParameters().
  *                         See JSR-234 Spec.
  *                         Java layer is responsible for this parameter
  *                         being greater than zero
  * @param maxDistance      this parameter has the same meaning as 
  *                         maxDistance parameter of 
  *                         DistanceAttenuationControl.setParameters()
  *                         See JSR-234 Spec
  *                         Java layer is responsible for this parameter
  *                         being greater than zero and greater than
  *                         minDistance parameter
  * @param isMuteAfterMax   this parameter has the same meaning as 
  *                         muteAfterMax parameter of 
  *                         DistanceAttenuationControl.setParameters()
  *                         See JSR-234 Spec
  * @param rolloffFactor    this parameter has the same meaning as 
  *                         rolloffFactor parameter of 
  *                         DistanceAttenuationControl.setParameters()
  *                         See JSR-234 Spec
  *                         Java layer is responsible for this parameter
  *                         being greater than or equal to zero.
  * 
  *
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_audio3d_distance_attenuation_control_set_parameters (
    javacall_audio3d_distance_attenuation_control_t *dac,
                      int minDistance,
                      int maxDistance,
                      javacall_bool isMuteAfterMax,
                      int rolloffFactor);

/** 
  * The function corresponding to 
  * DistanceAttenuationControl.getMinDistance()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param dac             pointer to the DistanceAttenuationControl
  * @return     the Minimum Distance currently set for this Control.
  *             For explanation what is Minimum Distance see JSR-234 Spec,
  *             DistanceAttenuationControl description
  */
int javacall_audio3d_distance_attenuation_control_get_min_distance (
        javacall_audio3d_distance_attenuation_control_t *dac );

/** 
  * The function corresponding to 
  * DistanceAttenuationControl.getMaxDistance()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param dac             pointer to the DistanceAttenuationControl
  * @return     the Maximum Distance currently set for this Control.
  *             For explanation what is Maximum Distance see JSR-234 Spec,
  *             DistanceAttenuationControl description
  */
int javacall_audio3d_distance_attenuation_control_get_max_distance (
        javacall_audio3d_distance_attenuation_control_t *dac );

/** 
  * The function corresponding to 
  * DistanceAttenuationControl.getMuteAfterMax()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param dac             pointer to the DistanceAttenuationControl
  * @retval JAVACALL_TRUE   if the Control setting muteAfterMax is on
  *                         For explanation what is muteAfterMax setting
  *                         see JSR-234 Spec, DistanceAttenuationControl
  *                         description.
  * @retval JAVACALL_FALSE  if the Control setting muteAfterMax is off
  *                         For explanation what is muteAfterMax setting
  *                         see JSR-234 Spec, DistanceAttenuationControl
  *                         description.
  */
javacall_bool javacall_audio3d_distance_attenuation_control_is_mute_after_max (
                javacall_audio3d_distance_attenuation_control_t *dac );

/** 
  * The function corresponding to 
  * DistanceAttenuationControl.getRolloffFactor()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param dac             pointer to the DistanceAttenuationControl
  * @return     the Rolloff Factor currently set for this Control.
  *             For explanation what is Rolloff Factor see JSR-234 Spec,
  *             DistanceAttenuationControl description
  */
int javacall_audio3d_distance_attenuation_control_get_rolloff_factor (
        javacall_audio3d_distance_attenuation_control_t *dac );

/** 
  * The function corresponding to 
  * LocationControl.setCartesian()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * @param location_control     pointer to the LocationControl 
  * @param x                this parameter has the same meaning as 
  *                         "x" parameter of 
  *                         LocationControl.setCartesian() method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param y                this parameter has the same meaning as 
  *                         "y" parameter of 
  *                         LocationControl.setCartesian() method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param z                this parameter has the same meaning as 
  *                         "z" parameter of 
  *                         LocationControl.setCartesian() method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  */
javacall_result javacall_audio3d_location_control_set_cartesian (
    javacall_audio3d_location_control_t *location_control,
                     int x,
                     int y,
                     int z);

/** 
  * The function corresponding to 
  * LocationControl.setSpherical()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param location_control     pointer to the LocationControl 
  * @param azimuth          this parameter has the same meaning as 
  *                         "azimuth" parameter of 
  *                         LocationControl.setSpherical() method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param elevation        this parameter has the same meaning as 
  *                         "elevation" parameter of 
  *                         LocationControl.setSpherical() method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param radius           this parameter has the same meaning as 
  *                         "radius" parameter of 
  *                         LocationControl.setSpherical() method of AMMS 
  *                         Java API. See JSR-234 Spec
  *                         Java layer is responsible for this parameter
  *                         being greater than or equal to zero.
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_audio3d_location_control_set_spherical (
    javacall_audio3d_location_control_t *location_control,
                     int azimuth,
                     int elevation,
                     int radius);

/** 
  * The function corresponding to 
  * LocationControl.getCartesian()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param location_control     pointer to the LocationControl 
  * @param coord                pointer to array that receives
  *                             3 consecutive integers,
  *                             "x", "y" and "z".
  *                             @see setCartesian
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_audio3d_location_control_get_cartesian (
    javacall_audio3d_location_control_t *location_control,
    /*OUT*/int coord[3]);

/** 
  * The function corresponding to 
  * OrientationControl.setOrientation(int, int, int)
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param orientation_control    pointer to the OrientationControl 
  * @param heading          this parameter has the same meaning as 
  *                         "heading" parameter of 
  *                         OrientationControl.setOrientation(int, int, 
  *                         int) method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param pitch            this parameter has the same meaning as 
  *                         "pitch" parameter of 
  *                         OrientationControl.setOrientation(int, int,
  *                         int) method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @param roll             this parameter has the same meaning as 
  *                         "roll" parameter of 
  *                         OrientationControl.setOrientation(int, int,
  *                         int) method of AMMS 
  *                         Java API. See JSR-234 Spec
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  */
javacall_result javacall_audio3d_orientation_control_set_orientation (
        javacall_audio3d_orientation_control_t *orientation_control,
                       int heading,
                       int pitch,
                       int roll );

/**
  * The function corresponding to 
  * OrientationControl.setOrientation(int[], int[])
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param orientation_control    pointer to the OrientationControl 
  * @param frontVector      this parameter has the same meaning as 
  *                         "frontVector" parameter of 
  *                         OrientationControl.setOrientation(int[],
  *                         int[]) method of AMMS 
  *                         Java API. See JSR-234 Spec.
  *                         frontVector is a vector in 3D space which
  *                         is frontward-directed in regard to the 
  *                         spectator. Java layer is responsible for
  *                         the following:
  *                         - this vector is not (0,0,0)
  *                         - this vector is not parallel to the aboveVector in
  *                         3D space
  * @param aboveVector      this parameter has the same meaning as 
  *                         "aboveVector" parameter of 
  *                         OrientationControl.setOrientation(int[],
  *                         int[]) method of AMMS 
  *                         Java API. See JSR-234 Spec
  *                         aboveVector is a vector in 3D space which
  *                         is directed somewhere above but w/o left or
  *                         right deviation in regard to the 
  *                         spectator. Java layer is responsible for
  *                         the following:
  *                         - this vector is not (0,0,0)
  *                         - this vector is not parallel to the frontVector in
  *                         3D space
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_audio3d_orientation_control_set_orientation_vec (
        javacall_audio3d_orientation_control_t *orientation_control,
                       const int frontVector[3],
                       const int aboveVector[3]);

/** 
  * The function corresponding to 
  * OrientationControl.getOrientationVectors()
  * method of AMMS Java API. See JSR-234 Spec
  * 
  * 
  * @param orientation_control    pointer to the OrientationControl 
  * @param vectors pointer to an array that receives 6 elements
  *                which are two consecutive vectors in 3D space:
  *                - frontVector
  *                - aboveVector
  *                For explanation what are these vectors see the following: 
  *                - JSR-234 Spec, OrientationControl description.
  *                - SeeAlso link
  *                @see setOrientationVec
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   if any parameter value limitation
  *                                     mentioned above is violated by
  *                                     Java layer
  */
javacall_result javacall_audio3d_orientation_control_get_orientation_vectors (
    javacall_audio3d_orientation_control_t *orientation_control,
                       /*OUT*/int vectors[6]);

/** @} */

/**
 * @addtogroup jsrMandatoryJSR234Music
 * @ingroup JSR234
 * @{
 */

/** 
  * The function corresponding to VolumeControl.getLevel()
  * method of MMAPI Java API. See JSR-135 Spec
  * 
  * Get the current volume level set.
  * @param vctl    pointer to the VolumeControl 
  * @return        current volume level
  */
int javacall_amms_volume_control_get_level(
        javacall_amms_volume_control_t* vctl );

/** 
  * The function corresponding to VolumeControl.isMuted()
  * method of MMAPI Java API. See JSR-135 Spec
  * 
  * Get the mute state of the signal associated with this VolumeControl.
  * @param vctl    pointer to the VolumeControl 
  * @retval JAVACALL_TRUE               Signal is muted
  * @retval JAVACALL_FALSE              Signal is not muted
  */
javacall_bool
    javacall_amms_volume_control_is_muted (
        javacall_amms_volume_control_t* vctl );

/** 
  * The function corresponding to VolumeControl.setLevel()
  * method of MMAPI Java API. See JSR-135 Spec
  * 
  * Set the volume using a linear point scale with values between 0 and 100.
  * @param vctl      pointer to the VolumeControl 
  * @param level     volume level to be set
  * @param new_level receives volume level that was actually set
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If level < 0 or level > 100
  */
javacall_result
    javacall_amms_volume_control_set_level (
        javacall_amms_volume_control_t* vctl,
        int                             level,
        /*OUT*/int*                     new_level );

/** 
  * The function corresponding to VolumeControl.()
  * method of MMAPI Java API. See JSR-135 Spec
  * 
  * Mute or unmute signal associated with this VolumeControl.
  * @param vctl    pointer to the VolumeControl 
  * @param mute    JAVACALL_TRUE to mute signal,
  *                JAVACALL_FALSE to unmute signal
  * @retval JAVACALL_OK                 Success
  * @retval JAVACALL_FAIL               Fail
  * @retval JAVACALL_INVALID_ARGUMENT   If mute is other than JAVACALL_TRUE or JAVACALL_FALSE
  */
javacall_result
    javacall_amms_volume_control_set_mute (
        javacall_amms_volume_control_t* vctl,
        javacall_bool                   mute );
 
/** @} */

/**
* @defgroup jsrMandatoryJSR234ImageEncoding Mandatory \
*           part of image encoding and post-processing
* @ingroup JSR234
* @{
*/

/**
* @enum javacall_amms_image_filter_type
* @brief  This enum describes possible filter types in Image processing. 
* See JSR-234 Spec: MediaProcessor, ImageEffect.
*/
typedef enum javacall_amms_image_filter_type {
    /** Filter which can convert from one format to another (FormatControl). */
    javacall_amms_image_filter_converter     = 1,
    /** Filter which can apply image effects (ImageEffectControl). */
    javacall_amms_image_filter_effect        = 2,
    /** Filter which can transform images (ImageTransformControl). */
    javacall_amms_image_filter_transform     = 3,
    /** Filter which can put images on top of processed (ImageOveralyControl). */
    javacall_amms_image_filter_overlay       = 4
} javacall_amms_image_filter_type;

/**
* @typedef javacall_image_filter_handle
* @brief Type corresponding to the image filter context (state and data).
* Image filter is the one of image processing modules, which 
* are combined using media_processor.
*/
typedef struct javacall_amms_image_filter_s* javacall_image_filter_handle;

/**
* @typedef javacall_media_processor_handle
* @brief Type corresponding to the image processor context (state and data).
*/
typedef struct javacall_amms_media_processor_s* javacall_media_processor_handle;

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Get the supported mime types as filter input.
*
* @param filter_type     filter type
* @param number_of_types pointer to return the returned array length.
* @see javacall_amms_image_filter_type
*
* @return 
* - NULL, if the input data are incorrect, or no types supported.
* - Otherwise return array of the supported mime types for source. The size
* of the array is returned using \a number_of_types parameter.
*
* @see javacall_image_filter_get_supported_dest_mime_types
*/
javacall_const_utf16_string* 
    javacall_image_filter_get_supported_source_mime_types(
        javacall_amms_image_filter_type filter_type, 
        /*OUT*/ int *number_of_types);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Get the supported output formats (destination mime types) for 
* specified input type and filter.
* 
* @param filter_type      filter type
* @param source_mime_type proposed source mime type.
* @param number_of_types  pointer to return the returned array length.
* @return 
* - NULL, if the input data are incorrect, or no types supported.
* - Otherwise return array of the supported mime types for destination. The size
* of the array is returned using \a number_of_types parameter.
*
* @see javacall_image_filter_get_supported_source_mime_types
*/
javacall_const_utf16_string* 
    javacall_image_filter_get_supported_dest_mime_types(
        javacall_amms_image_filter_type filter_type, 
        javacall_const_utf16_string source_mime_type, 
        /*OUT*/ int *number_of_types);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. EffectControl.getPresetNames()
* 
* Get the supported presets for specified filter.
* 
* @param filter_handle          pointer to the filter context
* @param number_of_presets      pointer to return the returned array length.
* @return 
* - NULL, if no presets supported.
* - Otherwise, return array of the supported mime types for destination. The size
*           of the array is returned using \a number_of_types parameter.
*/
javacall_const_utf16_string* 
    javacall_image_filter_get_supported_presets(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int *number_of_presets);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. EffectControl.setPreset()
* 
* Get the supported presets for specified filter.
* 
* @param filter_handle            pointer to the filter context
* @param preset_name              string which describes preset name
*
* @retval JAVACALL_OK             Preset was set
* @retval JAVACALL_INVALID_ARGUMENT Wrong filter_handle or preset is not supported
* @retval JAVACALL_FAIL           Fail
*/
javacall_result
    javacall_image_filter_set_preset(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string preset_name);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. EffectControl.getPreset()
* 
* Get the current enabled preset for specified filter.
* 
* @param filter_handle            pointer to the filter context
*
* @return Preset name, or NULL, if presets are not supported
*/
javacall_const_utf16_string 
    javacall_image_filter_get_preset(
        javacall_image_filter_handle filter_handle);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageTransformControl.setSourceRect()
* 
* Specify the source rectangle for transform filter.
* 
* @param filter_handle  pointer to the filter context
* @param x              x coordinate of upper-left corner
* @param y              y coordinate of upper-left corner
* @param width          width of the rectangle (may be <0)
* @param height         height of the rectangle (may be <0)
*
* @retval JAVACALL_OK       Source rect was set
* @retval JAVACALL_INVALID_ARGUMENT 
*         If filter_handle is NULL, or width == 0, or wrong height == 0
* @retval JAVACALL_FAIL     If filter_handle doesn't support transform
*/
javacall_result 
    javacall_amms_image_filter_set_source_rect(
        javacall_image_filter_handle filter_handle, 
        int x, int y, 
        int width, int height);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageTransformControl.setTargetSize()
* 
* Specify the size  and rotation of the target image.
* 
* @param filter_handle  pointer to the filter context
* @param width          width of the target image (must be >0)
* @param height         height of the target image (must be >0)
* @param rotation       desired rotation (count of applied 90 degree 
* clockwise rotations) (must be in 0..3)
*
* @retval JAVACALL_OK       Target size was set
* @retval JAVACALL_INVALID_ARGUMENT 
*         If filter_handle is NULL, or wrong width, or wrong height 
* @retval JAVACALL_FAIL     If filter_handle doesn't support transformation
*/
javacall_result 
    javacall_amms_image_filter_set_dest_size(
        javacall_image_filter_handle filter_handle, 
        int width, int height, int rotation);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. OverlayControl.insertImage()
* 
* Set the overlay image to be drawn on the processing image. All 
* data from the pRGBdata will be copied.
* \a transparencyColor used to specify mode of overlay drawing:
*  - \a transparencyColor == 1 << 28, overlay image treated as fully opaque 
* (alpha ignored).
*  - otherwise, overlay image treated as image with dedicated 
* transparency color (pixel with RGB color equal to \a transparencyColor 
* treated as fully transparent).
* 
* @param filter_handle            pointer to the filter context
* @param pRGBdata                 pointer to rgb32 data 
* @param width                    image width
* @param height                   image height
* @param x                        x-position on layer
* @param y                        y-position on layer
* @param transparencyColor        color (XRGB), treated as transparency color
*
* @retval JAVACALL_OK       Image for drawing on top was set
* @retval JAVACALL_INVALID_ARGUMENT 
*         If filter_handle is NULL, or pRGBdata is NULL, 
*         or wrong width, or wrong height 
* @retval JAVACALL_FAIL     If filter_handle doesn't support overlay
*/
javacall_result 
    javacall_amms_image_filter_set_image(
        javacall_image_filter_handle filter_handle,
        int* pRGBdata, int width, int height,
        int x, int y, int transparencyColor);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getSupportedStrParameters()
* 
* Get the names of supported string-valued parameters
* 
* @param filter_handle      pointer to the filter context
* @param number_of_params   pointer to return the returned array size
*
* @return
*   - NULL, if string-valued parameters are not supported 
*   - Otherwise return array of the supported string-valued parameters. 
* The size of the array is returned using \a number_of_params parameter.
*/
javacall_const_utf16_string* 
    javacall_image_filter_get_str_params(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getSupportedIntParameters()
* 
* Get the names of supported int-valued parameters
* 
* @param filter_handle      pointer to the filter context
* @param number_of_params   pointer to return the returned array size
*
* @return
*   - NULL, if string-valued parameters are not supported 
*   - Otherwise return array of the supported int-valued parameters. 
* The size of the array is returned using \a number_of_params parameter.
*/
javacall_const_utf16_string*
    javacall_image_filter_get_int_params(
        javacall_image_filter_handle filter_handle, 
        /*OUT*/ int* number_of_params);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getSupportedStrParameterValues()
* 
* Get the possible values for string-valued parameter
* 
* @param filter_handle      pointer to the filter context
* @param param_name         string-valued parameter name
* @param number_of_values   pointer to return the returned array size
*
* @return
*   - NULL, if specified string-valued parameter is not supported
*   - Otherwise return array of possible values for string-valued parameter
* The size of the array is returned using \a number_of_values parameter.
*/
javacall_const_utf16_string*
    javacall_image_filter_get_str_values(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* number_of_values);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getSupportedIntParameterValues()
* 
* Get the range of possible values for int-valued parameter
* 
* @param filter_handle  pointer to the filter context
* @param param_name     int-valued parameter name
* @param min_range      pointer to min range value
* @param max_range      pointer to max range value
*
* @retval   JAVACALL_OK if query was successful
* min_range and max_range are filled with corresponded values
* @retval   JAVACALL_INVALID_ARGUMENT 
* if any of the parameters is NULL, or specified parameter is not supported
*/
javacall_result
    javacall_image_filter_get_int_values(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* min_range, int* max_range);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.setParameter(string, string)
* 
* Set the value of the string-valued parameter
* 
* @param filter_handle  pointer to the filter context
* @param param_name     string-valued parameter name
* @param new_value      new value of string-valued parameter
*
* @retval   JAVACALL_OK if value was set successful
* @retval   JAVACALL_INVALID_ARGUMENT 
* if any of the parameters is NULL, or specified parameter is not supported,
* or specified value is not supported
*/
javacall_result
    javacall_image_filter_set_str_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        javacall_const_utf16_string new_value);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.setParameter(string, int)
* 
* Set the value of the int-valued parameter
* 
* @param filter_handle  pointer to the filter context
* @param param_name     string-valued parameter name
* @param new_value      new value of int-valued parameter
*
* @retval   JAVACALL_OK if value was set successful
* @retval   JAVACALL_INVALID_ARGUMENT 
* if any of the parameters is NULL, or specified parameter is not supported,
* or specified value is out of range
*/
javacall_result
    javacall_image_filter_set_int_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        int new_value);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getStrParameterValue()
* 
* Get the value of the string-valued parameter
* 
* @param filter_handle  pointer to the filter context
* @param param_name     string-valued parameter name
*
* @return
*   - NULL, if filter_handle is NULL, or param_name is NULL,
* or specified parameter is not supported
*   - Otherwise, return null-terminated JavaCall-unicode string
* with corresponded value
*/
javacall_const_utf16_string
    javacall_image_filter_get_str_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name);

/**
* The function is the part of the Image Process AMMS Java API. 
* See JSR-234 Spec. ImageFormat.getIntParameterValue()
* 
* Get the value of the string-valued parameter
* 
* @param filter_handle  pointer to the filter context
* @param param_name     int-valued parameter name
* @param cur_value      pointer to returned value
*
* @retval JAVACALL_OK if parameter is supported, and cur_value
* is filled with corresponding value
* @retval JAVACALL_INVALID_ARGUMENT if filter_handle is NULL, 
* or param_name is NULL, or cur_value is NULL, 
* or specified parameter is not supported
*/
javacall_result
    javacall_image_filter_get_int_param_value(
        javacall_image_filter_handle filter_handle, 
        javacall_const_utf16_string param_name, 
        /*OUT*/ int* cur_value);

/** 
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Create specified filter for processing image from specified source mime type 
* to specified destination mime type.
* 
* 
* @param filter_type kind of filter
* @param source_mime_type mime type of source.
* @param dest_mime_type   mime type of destination.
* @return filter handle, which is used for converter managing.
* @see javacall_image_filter_destroy
* @see javacall_media_processor_add_filter
*/
javacall_image_filter_handle 
    javacall_image_filter_create(
        javacall_amms_image_filter_type filter_type,
        javacall_const_utf16_string source_mime_type, 
        javacall_const_utf16_string dest_mime_type);

/** 
* The function is the part of the Image Process AMMS Java API.
* 
* Clone specified filter. Create new handle and copy settings. 
* The original and cloned filters are independent.
* 
* @param filter_handle filter description
* @return cloned filter handle, which is used for converter managing.
* @see javacall_image_filter_destroy
* @see javacall_media_processor_add_filter
*/
javacall_image_filter_handle 
    javacall_image_filter_clone(javacall_image_filter_handle filter_handle);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Destroy filter.
* 
* @param filter_handle             pointer to the filter context
* @retval JAVACALL_OK              Success
* @retval JAVACALL_FAIL            Fail
*
* @see javacall_result
* @see javacall_image_converter_create
*/
javacall_result 
    javacall_image_filter_destroy(javacall_image_filter_handle filter_handle);

/** 
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Create converter for processing image from specified source mime type 
* to specified destination mime type.
* 
* @param media_processor_id unique media processor ID which is holder
*           of created converter. This unique ID is generated by Java MMAPI
 *          library.
* @return processor handle, which is used for conversion process managing.
* @see javacall_media_processor_destroy
* @see javacall_media_processor_reset
* @see javacall_media_processor_add_filter
* @see javacall_media_processor_start
* @see javacall_media_processor_stop
* @see javacall_media_processor_abort
*/
javacall_media_processor_handle 
    javacall_media_processor_create(javacall_int64  media_processor_id);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Reset media processor.
* 
* @param media_processor_handle    pointer to the media processor
* @retval JAVACALL_OK              Success
* @retval JAVACALL_FAIL            Fail
*
* @see javacall_result
* @see javacall_media_processor_create
*/
javacall_result 
    javacall_media_processor_reset(
        javacall_media_processor_handle media_processor_handle);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Add filter to the processing line in media processor. 
* \attention \{ Media processor doesn't check correctness of filter connecting 
* and their order. \}
* \attention \{ When filter added, media processor clone it via 
* javacall_image_filter_clone. 
* So, if you modify filter settings, your need to call
* javacall_media_processor_reset and add all filters again. \}
* 
* @param media_processor_handle    pointer to the media processor
* @param filter_handle             pointer to the filter context
* @retval JAVACALL_OK              Success
* @retval JAVACALL_FAIL            Fail
*
* @see javacall_result
* @see javacall_media_processor_create
* @see javacall_image_filter_reset
* @see javacall_image_filter_clone
*/
javacall_result 
    javacall_media_processor_add_filter(
        javacall_media_processor_handle media_processor_handle, 
        javacall_image_filter_handle filter_handle);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Destroy media processor.
* 
* @param media_processor_handle    pointer to the media_processor
* @retval JAVACALL_OK              Success
* @retval JAVACALL_FAIL            Fail
*
* @see javacall_result
* @see javacall_media_processor_create
*/
javacall_result 
    javacall_media_processor_destroy(
        javacall_media_processor_handle media_processor_handle);

/** 
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Start converter processing. 
* If conversion started, the one of 
 * \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED 
* or \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_ERROR will be posted, 
* regarding the conversion status.
* \note The realization may be synchronous, so, in this case it will post 
* \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED before returning from this
* function.
* @see javacall_amms_notification_type 
* 
* @param media_processor_handle    pointer to the processor
* @retval JAVACALL_OK              Conversion started
* @retval JAVACALL_FAIL            Conversion failed to start
*
* @see javacall_result
* @see javacall_image_converter_get_expected_size
*/
javacall_result 
    javacall_media_processor_start(
        javacall_media_processor_handle media_processor_handle);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Stop(pause) converter processing. 
* If it is possible to stop conversion, method will return JAVACALL_OK.
* \attention Possible cases when on javacall_media_processor_stop, 
* you may get \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED, because
* it was too late to stop something.
*
* @param media_processor_handle          pointer to the processor
* @retval JAVACALL_OK              
*           Stopping of processing started, or conversion is not running.
* @retval JAVACALL_FAIL            
 *          Failed to stop conversion
*
* @see javacall_result
*/
javacall_result 
    javacall_media_processor_stop(
        javacall_media_processor_handle media_processor_handle);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Abort converter processing. The function is synchronous, according to the 
 * proposal for JSR-234 (MediaProcessor abort() method).
* 
* @param media_processor_handle    pointer to the processor
* @retval JAVACALL_OK              
 *          Conversion aborted, or conversion is not running.
* @retval JAVACALL_FAIL            
 *          Failed to abort conversion
*
* @see javacall_result
*/
javacall_result 
    javacall_media_processor_abort(
        javacall_media_processor_handle media_processor_handle);


/** 
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Set input for image processing (RGB32). All data from the \a pRGBdata will 
* be copied into internal buffer.
* 
* @param media_processor_handle    pointer to the processor
* @param pRGBdata                  pointer to rgb32 data 
* @param width                     image width
* @param height                    image height
*
* @retval JAVACALL_OK              The data is correct
* @retval JAVACALL_FAIL            The data is incorrect
*
* @see javacall_result
*/
javacall_result 
    javacall_media_processor_set_input_rgb32(
        javacall_media_processor_handle media_processor_handle,
        int* pRGBdata, int width, int height);


/** 
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Set input for image processing. All data from the \a pRAWdata will 
* be copied into internal buffer.
* 
* @param media_processor_handle    pointer to the processor
* @param pRAWdata                  pointer to raw data 
* @param length                    length of source data
*
* @retval JAVACALL_OK              The data is correct
* @retval JAVACALL_FAIL            The data is incorrect
*
* @see javacall_result
*/
javacall_result 
    javacall_media_processor_set_input_raw(
        javacall_media_processor_handle media_processor_handle,
        unsigned char* pRAWdata, int length);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Retrieve processor output data. Use this function to get data, after 
* event \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED came. The pointer
* will be valid until call to one of this functions (with the same handle): 
*   - \c javacall_media_processor_start()
*   - \c javacall_media_processor_set_input_rgb32()
*   - \c javacall_media_processor_reset()
*   - \c javacall_media_processor_destroy()
*
* @param media_processor_handle    pointer to the processor
* @param result_length             pointer to return the length of resulted data
* @return pointer to processed data, or NULLL
*/
const unsigned char* 
    javacall_media_processor_get_raw_output(
        javacall_media_processor_handle media_processor_handle, 
        /*OUT*/ int* result_length);

/**
* The function is the part of the Image Process AMMS Java API. See JSR-234 Spec.
* 
* Retrieve processor output data sizes. Use this function to get output size, 
* after event \a JAVACALL_EVENT_AMMS_MEDIA_PROCESSOR_COMPLETED came.
*
* @param media_processor_handle    pointer to the processor
* @param width                     pointer to return the width
* @param height                    pointer to return the height
* @retval JAVACALL_OK              Success
* @retval JAVACALL_FAIL            Fail
*
* @see javacall_result
*/
javacall_result
    javacall_media_processor_get_output_size(
        javacall_media_processor_handle media_processor_handle, 
        /*OUT*/ int* width, int* height);

/** @} */

/** @} */

#ifdef __cplusplus
}
#endif

#endif /*__JAVACALL_MULTIMEDIA_ADVANCED_H*/
