/*
 *
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
#include <kni.h>
#include <jsrop_kni.h>
#include <jsrop_memory.h>
#include <sni.h>
#include <commonKNIMacros.h>
#include <midpMalloc.h>
#include <midpString.h>
#include <midpUtilKni.h>
#include <midpServices.h>
#include <midpError.h>

#include <stdio.h>
#include <javacall_location.h>
#include <javautil_unicode.h>

#include <midp_thread.h>
#include <midpEvents.h>

#define MAX_PROVIDERS   5   /* max number of Platform providers */

#define JSR179_MAX_EXTRAINFO_TYPES (JAVACALL_LOCATION_EXTRAINFO_OTHER + 1)
#define JSR179_MAX_EXTRAINFO_SIZE 512

/**
 * internal information about opened Location Provider
 */
typedef struct {
    jboolean locked;
	jboolean newLocationAvailable;
    jint num_instances;
    javacall_handle id;
    jlong lastLocationTimestamp;
    javacall_location_location lastLocation;
    javacall_location_addressinfo_fieldinfo lastAddressInfo[JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD];
    javacall_utf16 lastExtraInfo[JSR179_MAX_EXTRAINFO_TYPES][JSR179_MAX_EXTRAINFO_SIZE];
    javacall_utf16 otherExtraInfoMimeType[JAVACALL_LOCATION_MAX_MIMETYPE_LENGTH];
}ProviderInfo;
static ProviderInfo providerInfo[MAX_PROVIDERS];
static jint numOpenedProviders = 0;

/** ID of last updated Location Provider */
static ProviderInfo *lastUpdatedProvider = NULL;


/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID isValid;
    jfieldID timestamp;
    jfieldID latitude;
    jfieldID longitude;
    jfieldID altitude;
    jfieldID horizontalAccuracy;
    jfieldID verticalAccuracy;
    jfieldID speed;
    jfieldID course;
    jfieldID method;
    /* AddressInfo fields */
    jfieldID isAddressInfo;
    jfieldID AddressInfo_EXTENSION;
    jfieldID AddressInfo_STREET;
    jfieldID AddressInfo_POSTAL_CODE;
    jfieldID AddressInfo_CITY;
    jfieldID AddressInfo_COUNTY;
    jfieldID AddressInfo_STATE;
    jfieldID AddressInfo_COUNTRY;
    jfieldID AddressInfo_COUNTRY_CODE;
    jfieldID AddressInfo_DISTRICT;
    jfieldID AddressInfo_BUILDING_NAME;
    jfieldID AddressInfo_BUILDING_FLOOR;
    jfieldID AddressInfo_BUILDING_ROOM;
    jfieldID AddressInfo_BUILDING_ZONE;
    jfieldID AddressInfo_CROSSING1;
    jfieldID AddressInfo_CROSSING2;
    jfieldID AddressInfo_URL;
    jfieldID AddressInfo_PHONE_NUMBER;
    /* ExtraInfo field */
    jfieldID extraInfoNMEA;
    jfieldID extraInfoLIF;
    jfieldID extraInfoPlain;
    jfieldID extraInfoOther;
    jfieldID extraInfoOtherMIMEType;
}LocationInfoFieldIDs;
static LocationInfoFieldIDs locationInfoFieldID;

/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationProviderInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID incurCost;
    jfieldID canReportAltitude;
    jfieldID canReportAddressInfo;
    jfieldID canReportSpeedCource;
    jfieldID powerConsumption;
    jfieldID horizontalAccuracy;     
    jfieldID verticalAccuracy;       
    jfieldID defaultTimeout;         
    jfieldID defaultMaxAge;          
    jfieldID defaultInterval;        
    jfieldID averageResponseTime;    
    jfieldID defaultStateInterval;
}LocationProviderInfoFieldIDs;
static LocationProviderInfoFieldIDs locationProviderInfoFieldID;

/**
 * Field IDs of the <tt>com.sun.j2me.location.OrienationInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID azimuth;
    jfieldID isMagnetic;
    jfieldID pitch;
    jfieldID roll;
}OrientationInfoFieldIDs;
static OrientationInfoFieldIDs orientationInfoFieldID;

/* Helper-functions */
static void lock_thread(jint waitingFor, jint provider, void *info);
/*static void unlock_threads(jint waitingFor, jint provider);*/
static ProviderInfo* register_provider(javacall_handle provider_id);
static void unregister_provider(ProviderInfo* pInfo);
void notifyLocationEvent(javacall_handle provider_id, jint status, jint event);

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_j2me_location_PlatformLocationProvider_getListOfLocationProviders) {

    javacall_utf16 listOfProviders[JAVACALL_LOCATION_MAX_PROPERTY_LENGTH];

    KNI_StartHandles(1);
    KNI_DeclareHandle(tempHandle);
    if (javacall_location_property_get(JAVACALL_LOCATION_PROVIDER_LIST, 
        listOfProviders) == JAVACALL_OK) {
        jsrop_jstring_from_utf16_string(KNIPASSARGS listOfProviders, tempHandle);
    } else {
        KNI_ReleaseHandle(tempHandle);
    }
    KNI_EndHandlesAndReturnObject(tempHandle);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_open) {

    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = NULL;
    javacall_result res;
    javacall_handle pProvider = NULL;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_UTF16_STRING(1, name)

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {
        res = javacall_location_provider_open(name, &pProvider);
        switch (res) {
            case JAVACALL_OK:
                /* handler returned immediatelly */
                pInfo = register_provider(pProvider);
                break;
            case JAVACALL_INVALID_ARGUMENT:
                /* wrong provider name */
                KNI_ThrowNew(midpIllegalArgumentException, 
                            "wrong provider name");
                break;
            case JAVACALL_FAIL:
                /* fail */
                KNI_ThrowNew(midpIOException, "open failed");
                break;
            case JAVACALL_WOULD_BLOCK:
                /* wait for javanotify */
                pInfo = register_provider(pProvider);
                if (pInfo != NULL) {
                    pInfo->locked = KNI_TRUE;
                }
                lock_thread(JAVACALL_EVENT_LOCATION_OPEN_COMPLETED, (jint)pInfo->id, pInfo);
                break;
            default:
                break;
        }
    } else {
        /* Second call for this thread - finish open */
        if (info->status == JAVACALL_OK) {
            /* Provider opened successfully */
            pInfo = (ProviderInfo *)info->pResult;
            if ((pInfo != NULL) && (pInfo->id == info->descriptor)) {
                pInfo->locked = KNI_FALSE;
            }
        } else {
            /* Provider open failed*/
            pInfo = (ProviderInfo *)info->pResult;
            if ((pInfo != NULL) && (pInfo->id == info->descriptor)) {
                unregister_provider(pInfo);
                pInfo = NULL;
            }
        }
    }
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt((jint)pInfo);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_getCriteria) {

    javacall_location_provider_info provider_info;
    javacall_result res;
    jboolean ret = KNI_FALSE;

    KNI_StartHandles(3);
    /* get NULL terminated provider name */
    KNI_DeclareHandle(criteria);
    KNI_DeclareHandle(class_obj);
    
    GET_PARAMETER_AS_UTF16_STRING(1, name)

    /* call provider_open to get provider handler */
    res = javacall_location_provider_getinfo(name, &provider_info);
    if (res == JAVACALL_OK) {
        KNI_GetParameterAsObject(2, criteria);
        KNI_GetObjectClass(criteria, class_obj);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.incurCost, 
            provider_info.incurCost);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportAltitude, 
            provider_info.canReportAltitude);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportAddressInfo, 
            provider_info.canReportAddressInfo);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportSpeedCource, 
            provider_info.canReportSpeedCource);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.powerConsumption, 
            provider_info.powerConsumption);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.horizontalAccuracy, 
            provider_info.horizontalAccuracy);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.verticalAccuracy, 
            provider_info.verticalAccuracy);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultTimeout, 
            provider_info.defaultTimeout);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultMaxAge, 
            provider_info.defaultMaxAge);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultInterval, 
            provider_info.defaultInterval);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.averageResponseTime, 
            provider_info.averageResponseTime);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultStateInterval, 
            provider_info.defaultStateInterval);

        ret = KNI_TRUE;
    } else if (res == JAVACALL_INVALID_ARGUMENT) {
        /* wrong provider name */
        KNI_ThrowNew(jsropIllegalArgumentException, "wrong provider name");
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_waitForNewLocation) {

    jboolean ret = KNI_FALSE;
    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = (ProviderInfo *)KNI_GetParameterAsInt(1);
    javacall_result res;
    jint provider;
    jlong timeout;
    KNI_StartHandles(1);
    provider = KNI_GetParameterAsInt(1);
    timeout = KNI_GetParameterAsLong(2);

    if(pInfo != NULL) {
        info = (MidpReentryData*)SNI_GetReentryData(NULL);
        if (info == NULL) {
            /* First call -request */
            if(pInfo->locked == KNI_TRUE) {
                lock_thread(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, (jint)pInfo->id, pInfo);
            } else {
                /* request new location */
                res = javacall_location_update_set(pInfo->id, timeout);
                switch (res) {
                    case JAVACALL_WOULD_BLOCK:
                        /* wait for javanotify */
                        pInfo->locked = KNI_TRUE;
                        lock_thread(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, (jint)pInfo->id, pInfo);
                        break;
                    case JAVACALL_OK:
                        /* location updated successfully */
                        pInfo->locked = KNI_FALSE;
			            pInfo->newLocationAvailable = KNI_TRUE;
                        ret = KNI_TRUE;
                        break;
                    case JAVACALL_FAIL:
                        /* fail */
                        pInfo->locked = KNI_FALSE;
                        /* wrong provider name */
                        KNI_ThrowNew(midpIllegalArgumentException, 
                                    "wrong provider");
                        break;
                    default:
                        /* fail */
                        pInfo->locked = KNI_FALSE;
                        break;
                }
            }
        } else {
            /* Response */
            if (info->status == JAVACALL_OK) {
                pInfo = (ProviderInfo *)info->pResult;
                /* location updated successfully */
                if ((pInfo != NULL) && (pInfo->id == info->descriptor)) {
				    pInfo->newLocationAvailable = KNI_TRUE;
                }
                ret = KNI_TRUE;
            } else {
                /* location updated failed */
                ret = KNI_FALSE;
            }
            pInfo->locked = KNI_FALSE;
        }
    }
    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_receiveNewLocationImpl) {

    ProviderInfo *pInfo = (ProviderInfo *)KNI_GetParameterAsInt(1);
    jlong timestamp = 0;
    jboolean ret = KNI_FALSE;
    javacall_handle provider;
    int mimetype;

    timestamp = KNI_GetParameterAsLong(2);
    if(pInfo != NULL) {
        provider = pInfo->id;
		if (pInfo->newLocationAvailable == KNI_TRUE) {
			if(javacall_location_get(provider, &pInfo->lastLocation) ==
					JAVACALL_OK) {
                int extra_info_size = pInfo->lastLocation.extraInfoSize;
				/* get addressInfo if it is present */
				if (pInfo->lastLocation.addressInfoFieldNumber > 0) {
					if(javacall_location_get_addressinfo(provider,
						&pInfo->lastLocation.addressInfoFieldNumber, 
						pInfo->lastAddressInfo) != JAVACALL_OK) {
						/* drop Address Info for this location */
						pInfo->lastLocation.addressInfoFieldNumber = 0;
					}
				}

				/* get extraInfo if it is present */
                if (extra_info_size > 0) {
                    for(mimetype =0; mimetype<JSR179_MAX_EXTRAINFO_TYPES; mimetype++) {
                        if (javacall_location_get_extrainfo(provider, mimetype,
                                JSR179_MAX_EXTRAINFO_SIZE, pInfo->lastExtraInfo[mimetype], 
                                pInfo->otherExtraInfoMimeType) != JAVACALL_OK) {
                                pInfo->lastExtraInfo[mimetype][0] = 0;
                        }
                    }
				}
				
				pInfo->lastLocationTimestamp = timestamp;
				pInfo->newLocationAvailable = KNI_FALSE;

   				lastUpdatedProvider = pInfo;

                ret = KNI_TRUE;
			}
		} else {
			ret = KNI_TRUE;
		}
    }

    KNI_ReturnBoolean(ret);
}

static jboolean getLocation(KNIDECLARGS jobject locationInfo, jobject string_obj, ProviderInfo *pInfo) {
    jint i;
    jfieldID fid;
    jboolean ret = KNI_FALSE;

    if(pInfo != NULL) {
		if (pInfo->lastLocationTimestamp != 0) {
			/* Get location parameters */
			KNI_SetBooleanField(locationInfo,   
				locationInfoFieldID.isValid,    
				pInfo->lastLocation.isValidCoordinate);
			KNI_SetDoubleField(locationInfo,    
				locationInfoFieldID.latitude,   
				pInfo->lastLocation.latitude);
			KNI_SetDoubleField(locationInfo,    
				locationInfoFieldID.longitude,  
				pInfo->lastLocation.longitude);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.altitude,   
				pInfo->lastLocation.altitude);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.horizontalAccuracy, 
				pInfo->lastLocation.horizontalAccuracy);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.verticalAccuracy, 
				pInfo->lastLocation.verticalAccuracy);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.speed,      
				pInfo->lastLocation.speed);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.course,     
				pInfo->lastLocation.course);
			KNI_SetIntField(locationInfo,       
				locationInfoFieldID.method,     
				pInfo->lastLocation.method);
			KNI_SetLongField(locationInfo,      
				locationInfoFieldID.timestamp,  
				pInfo->lastLocationTimestamp);
			if (pInfo->lastLocation.addressInfoFieldNumber == 0) {
				KNI_SetBooleanField(locationInfo,   
					locationInfoFieldID.isAddressInfo, KNI_FALSE);
			} else {
				KNI_SetBooleanField(locationInfo,   
					locationInfoFieldID.isAddressInfo, KNI_TRUE);
			
				for(i=0; i<pInfo->lastLocation.addressInfoFieldNumber; i++) {
			        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->lastAddressInfo[i].data, string_obj);

					switch(pInfo->lastAddressInfo[i].fieldId) {
						case JAVACALL_LOCATION_ADDRESSINFO_EXTENSION:
							fid = locationInfoFieldID.
									AddressInfo_EXTENSION;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_STREET:
							fid = locationInfoFieldID.
									AddressInfo_STREET;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_POSTAL_CODE:
							fid = locationInfoFieldID.
									AddressInfo_POSTAL_CODE;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_CITY:
							fid = locationInfoFieldID.
									AddressInfo_CITY;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_COUNTY:
							fid = locationInfoFieldID.
									AddressInfo_COUNTY;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_STATE:
							fid = locationInfoFieldID.
									AddressInfo_STATE;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_COUNTRY:
							fid = locationInfoFieldID.
									AddressInfo_COUNTRY;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_COUNTRY_CODE:
							fid = locationInfoFieldID.
									AddressInfo_COUNTRY_CODE;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_DISTRICT:
							fid = locationInfoFieldID.
									AddressInfo_DISTRICT;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_NAME:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_NAME;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_FLOOR:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_FLOOR;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ROOM:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_ROOM;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ZONE:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_ZONE;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_CROSSING1:
							fid = locationInfoFieldID.
									AddressInfo_CROSSING1;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_CROSSING2:
							fid = locationInfoFieldID.
									AddressInfo_CROSSING2;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_URL:
							fid = locationInfoFieldID.
									AddressInfo_URL;
							break;
						case JAVACALL_LOCATION_ADDRESSINFO_PHONE_NUMBER:
							fid = locationInfoFieldID.
									AddressInfo_PHONE_NUMBER;
							break;
						default:
							fid = 0;
							break;
					}
					if(fid != 0) {
						KNI_SetObjectField(locationInfo, fid, 
							string_obj);
					}
				}
			}			
			if (pInfo->lastLocation.extraInfoSize > 0) {
		        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->lastExtraInfo[0], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoNMEA, 
					string_obj);
		        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->lastExtraInfo[1], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoLIF, 
					string_obj);
		        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->lastExtraInfo[2], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoPlain, 
					string_obj);
		        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->lastExtraInfo[3], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoOther, 
					string_obj);
		        jsrop_jstring_from_utf16_string(KNIPASSARGS pInfo->otherExtraInfoMimeType, string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoOtherMIMEType, 
					string_obj);
			}
			ret = KNI_TRUE;
        }
    }
    return ret;
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_getLastLocationImpl) {
    jboolean ret = KNI_FALSE;
    ProviderInfo *pInfo = (ProviderInfo *)KNI_GetParameterAsInt(1);

    if (pInfo != NULL) {
        KNI_StartHandles(2);
        KNI_DeclareHandle(string_obj);
        KNI_DeclareHandle(locationInfo);
    
        KNI_GetParameterAsObject(2, locationInfo);
    
        ret = getLocation(KNIPASSARGS locationInfo, string_obj, pInfo);
    
        KNI_EndHandles();
    }
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_getLastKnownLocationImpl) {
    jboolean ret = KNI_FALSE;

    if (lastUpdatedProvider != NULL) {
        KNI_StartHandles(2);
        KNI_DeclareHandle(string_obj);
        KNI_DeclareHandle(locationInfo);
    
        KNI_GetParameterAsObject(1, locationInfo);
    
        ret = getLocation(KNIPASSARGS locationInfo, string_obj, lastUpdatedProvider);
    
        KNI_EndHandles();
    }
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
typedef struct {
    jboolean filled;
    jint available;
    jint temporarilyUnavailable;
    jint outOfService;
} ProviderStateValue;
static ProviderStateValue stateValue = {KNI_FALSE, 0, 0, 0};

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_getStateImpl) {

    javacall_location_state state = JAVACALL_LOCATION_OUT_OF_SERVICE;
    ProviderInfo *pInfo = (ProviderInfo *)KNI_GetParameterAsInt(1);
    jint ret=0; /* out of service */
    
    if (stateValue.filled == KNI_FALSE) {

        KNI_StartHandles(1);
        KNI_DeclareHandle(clazz);
    
        KNI_FindClass("javax/microedition/location/LocationProvider", clazz);
        if(!KNI_IsNullHandle(clazz)) {
            stateValue.available = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "AVAILABLE", "I"));
            stateValue.temporarilyUnavailable = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "TEMPORARILY_UNAVAILABLE", "I"));
            stateValue.outOfService = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "OUT_OF_SERVICE", "I"));
            stateValue.filled = KNI_TRUE;
        }
        KNI_EndHandles();

    }        

    if (pInfo != NULL) {
        if (stateValue.filled == KNI_TRUE) {
            javacall_location_provider_state(pInfo->id, &state);
            switch(state) {
                case JAVACALL_LOCATION_AVAILABLE:
                    ret = stateValue.available;
                    break;
                case JAVACALL_LOCATION_TEMPORARILY_UNAVAILABLE:
                    ret = stateValue.temporarilyUnavailable;
                    break;
                case JAVACALL_LOCATION_OUT_OF_SERVICE:
                default:
                    ret = stateValue.outOfService;
                    break;
            }
        }
    }

    KNI_ReturnInt(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_resetImpl) {

    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = (ProviderInfo *)KNI_GetParameterAsInt(1);
    javacall_result res;

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {
        /* reset provider */
        res = javacall_location_update_cancel(pInfo->id);
        switch (res) {
            case JAVACALL_OK:
            case JAVACALL_FAIL:
                break;
            case JAVACALL_INVALID_ARGUMENT:
                /* wrong provider name */
                KNI_ThrowNew(midpIllegalArgumentException, "wrong provider");
                break;
            case JAVACALL_WOULD_BLOCK:
                /* wait for javanotify */
                if(pInfo != NULL) {
                    pInfo->locked = KNI_FALSE;
                }
                lock_thread(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, (jint)pInfo->id, pInfo);
                break;
            default:
                break;
        }
    }

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_PlatformLocationProvider_finalize) {

    ProviderInfo *pInfo;

    KNI_StartHandles(2);
    KNI_DeclareHandle(this_obj);
    KNI_DeclareHandle(class_obj);

    KNI_GetThisPointer(this_obj);
    KNI_GetObjectClass(this_obj, class_obj);

    pInfo = (ProviderInfo *)KNI_GetIntField(this_obj, 
                    KNI_GetFieldID(class_obj, "provider", "I"));
    if (pInfo) {
        unregister_provider(pInfo);
        javacall_location_provider_close(pInfo->id);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationProviderInfo_initNativeClass) {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        locationProviderInfoFieldID.incurCost = 
            KNI_GetFieldID(class_obj,"incurCost","Z");
        locationProviderInfoFieldID.canReportAltitude = 
            KNI_GetFieldID(class_obj,"canReportAltitude","Z");
        locationProviderInfoFieldID.canReportAddressInfo = 
            KNI_GetFieldID(class_obj,"canReportAddressInfo","Z");
        locationProviderInfoFieldID.canReportSpeedCource = 
            KNI_GetFieldID(class_obj,"canReportSpeedCource","Z");

        locationProviderInfoFieldID.powerConsumption = 
            KNI_GetFieldID(class_obj,"powerConsumption","I");
        locationProviderInfoFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj,"horizontalAccuracy","I");
        locationProviderInfoFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj,"verticalAccuracy","I");
        locationProviderInfoFieldID.defaultTimeout = 
            KNI_GetFieldID(class_obj,"defaultTimeout","I");
        locationProviderInfoFieldID.defaultMaxAge = 
            KNI_GetFieldID(class_obj,"defaultMaxAge","I");
        locationProviderInfoFieldID.defaultInterval = 
            KNI_GetFieldID(class_obj,"defaultInterval","I");
        locationProviderInfoFieldID.averageResponseTime = 
            KNI_GetFieldID(class_obj,"averageResponseTime","I");
        locationProviderInfoFieldID.defaultStateInterval = 
            KNI_GetFieldID(class_obj,"defaultStateInterval","I");
    } else {
        KNI_ThrowNew(jsropNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationInfo_initNativeClass) {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        locationInfoFieldID.isValid = 
            KNI_GetFieldID(class_obj, "isValid", "Z");
        locationInfoFieldID.timestamp = 
            KNI_GetFieldID(class_obj, "timestamp", "J");
        locationInfoFieldID.latitude = 
            KNI_GetFieldID(class_obj, "latitude", "D");
        locationInfoFieldID.longitude = 
            KNI_GetFieldID(class_obj, "longitude", "D");
        locationInfoFieldID.altitude = 
            KNI_GetFieldID(class_obj, "altitude", "F");
        locationInfoFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj, "horizontalAccuracy", "F");
        locationInfoFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj, "verticalAccuracy", "F");
        locationInfoFieldID.speed = 
            KNI_GetFieldID(class_obj, "speed", "F");
        locationInfoFieldID.course = 
            KNI_GetFieldID(class_obj, "course", "F");
        locationInfoFieldID.method = 
            KNI_GetFieldID(class_obj, "method", "I");
        locationInfoFieldID.isAddressInfo = 
            KNI_GetFieldID(class_obj, "isAddressInfo", "Z");
        locationInfoFieldID.AddressInfo_EXTENSION = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_EXTENSION", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_STREET = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STREET", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_POSTAL_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_POSTAL_CODE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CITY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CITY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_STATE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STATE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTRY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTRY_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY_CODE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_DISTRICT = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_DISTRICT", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_NAME = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_NAME", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_FLOOR = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_FLOOR", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_ROOM = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ROOM", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_ZONE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ZONE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CROSSING1 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING1", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CROSSING2 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING2", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_URL = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_URL", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_PHONE_NUMBER = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_PHONE_NUMBER", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoNMEA = 
            KNI_GetFieldID(class_obj, "extraInfoNMEA", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoLIF = 
            KNI_GetFieldID(class_obj, "extraInfoLIF", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoPlain = 
            KNI_GetFieldID(class_obj, "extraInfoPlain", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoOther = 
            KNI_GetFieldID(class_obj, "extraInfoOther", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoOtherMIMEType = 
            KNI_GetFieldID(class_obj, "extraInfoOtherMIMEType", "Ljava/lang/String;");
    } else {
        KNI_ThrowNew(jsropNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_OrientationProvider_open) {

    MidpReentryData *info = NULL;
    javacall_result res;
    javacall_handle provider     = NULL;
    javacall_utf16 listOfOrientationProviders[JAVACALL_LOCATION_MAX_PROPERTY_LENGTH];
    javacall_int32 length = 0;

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {
        if (javacall_location_property_get(JAVACALL_LOCATION_ORIENTATION_LIST, 
            listOfOrientationProviders) == JAVACALL_OK) {
            javautil_unicode_utf16_ulength(listOfOrientationProviders, &length);
        }
        if (length > 0) {

            res = javacall_location_provider_open(listOfOrientationProviders, &provider);

            switch (res) {
                case JAVACALL_OK:
                    break;
                case JAVACALL_INVALID_ARGUMENT:
                    /* wrong provider name */
                    KNI_ThrowNew(jsropIllegalArgumentException, 
                                "wrong provider name");
                    break;
                case JAVACALL_WOULD_BLOCK:
                    /* wait for javanotify */
                    lock_thread(JAVACALL_EVENT_LOCATION_OPEN_COMPLETED, (jint)provider, NULL);
                    break;

                case JAVACALL_FAIL:
                    /* fail */
                    KNI_ThrowNew("javax/microedition/location/LocationException", "open orientation provider failed");
                    break;
                default:
                    break;
            }
        }
    } else {
        if (info->status == JAVACALL_OK) {
            /* Provider opened successfully */
            provider = (javacall_handle)info->descriptor;
        } else {
            /* Provider open failed*/
            provider = 0;
        }
    }
    KNI_ReturnInt((jint)provider);
}


/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    KNIDECL(com_sun_j2me_location_OrientationProvider_getOrientation0) {

    javacall_result status = JAVACALL_FAIL;
    javacall_location_orientation orientation;
    jboolean ret = KNI_FALSE;
    javacall_handle provider;

    MidpReentryData* info = NULL;

    provider = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_StartHandles(1);
    KNI_DeclareHandle(orientationInfo);    
    //KNI_GetParameterAsObject(1, orientationInfo);
    KNI_GetParameterAsObject(2, orientationInfo);
    
    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL){
        status = javacall_location_orientation_update(provider);
        switch(status){
        case JAVACALL_OK:
            status = javacall_location_orientation_get(provider, 
                                               &orientation);
            if (status == JAVACALL_OK) {
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.azimuth,   
                                      orientation.compassAzimuth);
                KNI_SetBooleanField(orientationInfo, orientationInfoFieldID.isMagnetic,   
                                  orientation.isMagnetic);
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.pitch,   
                                  orientation.pitch);
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.roll,   
                                  orientation.roll);
                ret = KNI_TRUE;
            }
            break;
        case JAVACALL_WOULD_BLOCK:
            midp_thread_wait(JSR179_ORIENTATION_SIGNAL, (jint)provider, NULL);
            break;
        case JAVACALL_FAIL:
        default:
            break;  
        }
    } else {
        if (info->status == JAVACALL_OK) {
            /* Orientation received successfully */
            provider = (javacall_handle)info->descriptor;
            status = javacall_location_orientation_get(provider,
                                                           &orientation);
            if (status == JAVACALL_OK) {
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.azimuth,   
                                      orientation.compassAzimuth);
                KNI_SetBooleanField(orientationInfo, orientationInfoFieldID.isMagnetic,   
                                  orientation.isMagnetic);
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.pitch,   
                                  orientation.pitch);
                KNI_SetFloatField(orientationInfo, orientationInfoFieldID.roll,   
                                  orientation.roll);
                ret = KNI_TRUE;
            }
        } 
    }

    KNI_EndHandles();   
    KNI_ReturnBoolean(ret);
}


/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_OrientationInfo_initNativeClass) {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        orientationInfoFieldID.azimuth = 
            KNI_GetFieldID(class_obj, "azimuth", "F");
        orientationInfoFieldID.isMagnetic = 
            KNI_GetFieldID(class_obj, "isMagnetic", "Z");
        orientationInfoFieldID.pitch = 
            KNI_GetFieldID(class_obj, "pitch", "F");
        orientationInfoFieldID.roll = 
            KNI_GetFieldID(class_obj, "roll", "F");
    } else {
        KNI_ThrowNew(jsropNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* Helper-functions */

/* JAVADOC COMMENT ELIDED */
static void lock_thread(jint waitingFor, jint provider, void *info) {
    (void)waitingFor;
    midp_thread_wait(JSR179_LOCATION_SIGNAL, provider, info);
}

/* JAVADOC COMMENT ELIDED */
static ProviderInfo* register_provider(javacall_handle provider_id) {
    ProviderInfo *providerInfo = JAVAME_MALLOC(sizeof(ProviderInfo));
    
    if (providerInfo != NULL) {
        int i;

        /* new provider */
        providerInfo->id = provider_id;
        providerInfo->num_instances = 1;
        providerInfo->newLocationAvailable = KNI_FALSE;
        providerInfo->lastLocationTimestamp = 0;
        /* Initialize lastLocation data */
        providerInfo->lastLocation.addressInfoFieldNumber = 0;
        for (i=0; i<JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD; i++) {
            *(providerInfo->lastAddressInfo[i].data) = (javacall_utf16)0;
        }
        for (i=0; i<JSR179_MAX_EXTRAINFO_TYPES; i++) {
            providerInfo->lastExtraInfo[i][0] = 0;
        }
        *(providerInfo->otherExtraInfoMimeType) = 
            (javacall_utf16)0;
    }
    return providerInfo;
}

/* JAVADOC COMMENT ELIDED */
static void unregister_provider(ProviderInfo *pInfo) {
    if (pInfo != NULL)
        JAVAME_FREE(pInfo);
}
