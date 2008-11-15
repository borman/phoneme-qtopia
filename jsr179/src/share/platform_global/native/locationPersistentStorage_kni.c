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
#include <kni.h>

#include <javacall_location.h>
#include <javacall_landmarkstore.h>

#include <jsrop_exceptions.h>
#include <jsrop_kni.h>

/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID name;
    jfieldID description;
    /* Coordinates fields */
    jfieldID isCoordinates;
    jfieldID latitude;
    jfieldID longitude;
    jfieldID altitude;
    jfieldID horizontalAccuracy;
    jfieldID verticalAccuracy;
    /* AddressInfo fields */
    jfieldID isAddressInfo;
    jfieldID numAddressInfoFields;
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
}LandmarkImplFieldIDs;
static LandmarkImplFieldIDs landmarkImplFieldID;

static jboolean fill_landmark(jobject landmarkObj, javacall_landmarkstore_landmark *landmark, 
                                jobject stringObj);

static jboolean jsr179_jstring_to_utf16(jobject stringObj, javacall_utf16 *string, jint len);
static void jsr179_jstring_from_utf16(KNIDECLARGS jobject *stringObj, javacall_utf16 *string);

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_openLandmarkStoreList) {
    
    javacall_handle pHandle;
    jint hndl = 0;
    javacall_result res;
    
    /* call provider_open to get provider handler */
    res = javacall_landmarkstore_list_open(&pHandle);
    switch (res) {
        case JAVACALL_OK:
            /* landmarkStore list open successfully */
            hndl = (jint)pHandle;
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    KNI_ReturnInt(hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_landmarkStoreGetNext) {
    
    javacall_handle hndl;
    javacall_result res;
    javacall_utf16_string storeName;

    KNI_StartHandles(1);
    KNI_DeclareHandle(stringObj);
    hndl = (javacall_handle)KNI_GetParameterAsInt(1);

    res = javacall_landmarkstore_list_next(hndl, &storeName);
    switch (res) {
        case JAVACALL_OK:
            /* LandmarkStore name returned successfully */
            if (storeName != NULL) {
                jsrop_jstring_from_utf16_string(KNIPASSARGS storeName, stringObj);
            } else {
                /* Category name returned successfully */
                KNI_ReleaseHandle(stringObj);
            }
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    KNI_EndHandlesAndReturnObject(stringObj);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_closeLandmarkStoreList) {
    
    jint hndl = KNI_GetParameterAsInt(1);

    javacall_landmarkstore_list_close((javacall_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_createLandmarkStore) {
    
    javacall_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    /* call provider_open to get provider handler */
    res = javacall_landmarkstore_create(storeName);
    switch (res) {
      case JAVACALL_OK:
            /* LandmarkStore created successfully */
            break;
        case JAVACALL_FAIL:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
        case JAVACALL_INVALID_ARGUMENT:
            /* operation Failed */
            KNI_ThrowNew(jsropIllegalArgumentException, 
                "name is too long or Landmarkstore already exists");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_removeLandmarkStore) {
    
    javacall_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)

    /* call provider_open to get provider handler */
    res = javacall_landmarkstore_delete(storeName);
    switch (res) {
        case JAVACALL_OK:
            /* LandmarkStore created successfully */
            break;
        case JAVACALL_FAIL:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
        case JAVACALL_INVALID_ARGUMENT:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "name is too long");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_openCategoryList) {
    
    javacall_handle pHandle;
    jint hndl = 0;
    javacall_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    /* call provider_open to get provider handler */
    res = javacall_landmarkstore_categorylist_open(storeName, &pHandle);
    switch (res) {
        case JAVACALL_OK:
            /* Category list open successfully */
            hndl = (jint)pHandle;
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt(hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_categoryGetNext) {
    
    jint hndl;
    javacall_result res;
    javacall_utf16_string categoryName;

    KNI_StartHandles(1);
    KNI_DeclareHandle(stringObj);
    hndl = KNI_GetParameterAsInt(1);

    res = javacall_landmarkstore_categorylist_next((javacall_handle)hndl, &categoryName);
    switch (res) {
        case JAVACALL_OK:
            /* Category name returned successfully */
            jsrop_jstring_from_utf16_string(KNIPASSARGS categoryName, stringObj);
            break;
        case JAVACALL_FAIL:
            /* Category name returned successfully */
            KNI_ReleaseHandle(stringObj);
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    KNI_EndHandlesAndReturnObject(stringObj);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_closeCategoryList) {
    
    jint hndl = KNI_GetParameterAsInt(1);

    javacall_landmarkstore_categorylist_close((javacall_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_addCategoryImpl) {
    
    javacall_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    GET_PARAMETER_AS_UTF16_STRING(2, categoryName)

    res = javacall_landmarkstore_category_add(storeName, categoryName);
    switch (res) {
        case JAVACALL_OK:
            /* Category added successfully */
            break;
        case JAVACALL_INVALID_ARGUMENT:
            /* wrong provider name */
            KNI_ThrowNew(jsropIllegalArgumentException, 
                        "category name already exist");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_deleteCategoryImpl) {
    
    javacall_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    GET_PARAMETER_AS_UTF16_STRING(2, categoryName)

    res = javacall_landmarkstore_category_delete(storeName, categoryName);
    switch (res) {
        case JAVACALL_OK:
            /* Category deleted successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_addLandmarkToCategoryImpl) {
    
    jint landmarkID;
    javacall_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    GET_PARAMETER_AS_UTF16_STRING(3, categoryName)

    res = javacall_landmarkstore_landmark_add_to_category(storeName, 
                            (javacall_handle)landmarkID, categoryName);
    switch (res) {
        case JAVACALL_OK:
            /* Category added successfully */
            break;
        case JAVACALL_INVALID_ARGUMENT:
            /* wrong category name */
            KNI_ThrowNew(jsropIllegalArgumentException, 
                        "category name is invalid");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_addLandmarkToStoreImpl) {
    
    javacall_handle landmarkID = 0;
    javacall_result res;
    javacall_utf16_string categoryName = NULL;
    javacall_landmarkstore_landmark *landmark;
    jint numAddressFields;
    
    KNI_StartHandles(3);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);

    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    KNI_GetParameterAsObject(2, landmarkObj);

    /* CategoryName can be NULL -> check it and extract */
    KNI_GetParameterAsObject(3, stringObj);
    if (!KNI_IsNullHandle(stringObj)) {
        if (JAVACALL_OK != 
            jsrop_jstring_to_utf16_string(stringObj, &categoryName)) {
            categoryName = NULL;
        }
    }
    
    numAddressFields = KNI_GetIntField(landmarkObj, 
            landmarkImplFieldID.numAddressInfoFields);
    landmark = JAVAME_MALLOC(SIZE_OF_LANDMARK_INFO(numAddressFields));

    if (landmark != NULL) {
        if ( fill_landmark(landmarkObj, landmark, stringObj) == KNI_TRUE ) {
            res = javacall_landmarkstore_landmark_add_to_landmarkstore(storeName, 
                                    landmark, categoryName, &landmarkID);
            switch (res) {
                case JAVACALL_OK:
                    /* Category added successfully */
                    break;
                case JAVACALL_INVALID_ARGUMENT:
                    /* wrong category name */
                    KNI_ThrowNew(jsropIllegalArgumentException, 
                                "category name is invalid");
                    break;
                default:
                    /* operation Failed */
                    KNI_ThrowNew(jsropIOException, "I/O error");
                    break;
            }
        } else {
            KNI_ThrowNew(jsropIllegalArgumentException, 
                        "landmark name is too long");
        }
        JAVAME_FREE(landmark);
    }
    if (categoryName != NULL) {
        JAVAME_FREE(categoryName);
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt((jint)landmarkID);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_deleteLandmarkFromStoreImpl) {
    
    jint landmarkID;
    javacall_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);

    res = javacall_landmarkstore_landmark_delete_from_landmarkstore(
                                storeName, (javacall_handle)landmarkID);
    switch (res) {
        case JAVACALL_OK:
            /* Category added successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_deleteLandmarkFromCategoryImpl) {
    
    jint landmarkID;
    javacall_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    GET_PARAMETER_AS_UTF16_STRING(3, categoryName)

    res = javacall_landmarkstore_landmark_delete_from_category(storeName, 
                        (javacall_handle)landmarkID, categoryName);
    switch (res) {
        case JAVACALL_OK:
            /* Category added successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_openLandmarkList) {
    
    javacall_handle hndl = 0;
    javacall_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    GET_PARAMETER_AS_UTF16_STRING(2, categoryName)

    res =  javacall_landmarkstore_landmarklist_open(storeName, categoryName, &hndl);
    switch (res) {
        case JAVACALL_OK:
            /* Category list open successfully */
            break;
        case JAVACALL_INVALID_ARGUMENT:
            /* wrong category name */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    RELEASE_UTF16_STRING_PARAMETER
    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt((jint)hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_landmarkGetNext) {
    
    jint hndl;
    jint landmarkID = 0;
    javacall_result res;
    javacall_landmarkstore_landmark *landmark;
    jfieldID fid;
    jint i;

    KNI_StartHandles(2);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);
    hndl = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, landmarkObj);

    res = javacall_landmarkstore_landmarklist_next((javacall_handle)hndl, &landmarkID, &landmark);
    switch (res) {
        case JAVACALL_OK:
            if (landmark != NULL) {
                /* landmark.name */
                jsrop_jstring_from_utf16_string(KNIPASSARGS landmark->name, stringObj);
                KNI_SetObjectField(landmarkObj, landmarkImplFieldID.name, stringObj);

                /* landmark.description */
                jsr179_jstring_from_utf16(KNIPASSARGS &stringObj, landmark->description);
                KNI_SetObjectField(landmarkObj, landmarkImplFieldID.description, stringObj);

                if (!landmark->isValidCoordinate)
                {
                    /* landmark.isCoordinates */
                    KNI_SetBooleanField(landmarkObj, 
                                      landmarkImplFieldID.isCoordinates, KNI_FALSE);
                } else {
                    /* landmark.latitude */
                    KNI_SetDoubleField(landmarkObj, landmarkImplFieldID.latitude,
                                        landmark->latitude);
                    /* landmark.longitude */
                    KNI_SetDoubleField(landmarkObj, landmarkImplFieldID.longitude,
                                        landmark->longitude);
                    /* landmark.altitude */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.altitude,
                                        landmark->altitude);
                    /* landmark.horizontalAccuracy */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.horizontalAccuracy,
                                        landmark->horizontalAccuracy);
                    /* landmark.verticalAccuracy */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.verticalAccuracy,
                                        landmark->verticalAccuracy);
                    /* landmark.isCoordinates */
                    KNI_SetBooleanField(landmarkObj, 
                                      landmarkImplFieldID.isCoordinates, KNI_TRUE);
                }
                /* landmark.addressInfoFieldNumber */
                KNI_SetIntField(landmarkObj, 
                                landmarkImplFieldID.numAddressInfoFields,
                                landmark->addressInfoFieldNumber);
                /* landmark.isAddressInfo */
                KNI_SetBooleanField(landmarkObj, 
                                  landmarkImplFieldID.isAddressInfo, 
                 (landmark->addressInfoFieldNumber > 0) ? KNI_TRUE : KNI_FALSE);
                for (i=0; i < landmark->addressInfoFieldNumber; i++) {
                    switch (landmark->fields[i].fieldId) {
                        case JAVACALL_LOCATION_ADDRESSINFO_EXTENSION:
                            fid = landmarkImplFieldID.
                                    AddressInfo_EXTENSION;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_STREET:
                            fid = landmarkImplFieldID.
                                    AddressInfo_STREET;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_POSTAL_CODE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_POSTAL_CODE;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_CITY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CITY;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_COUNTY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTY;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_STATE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_STATE;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_COUNTRY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTRY;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_COUNTRY_CODE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTRY_CODE;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_DISTRICT:
                            fid = landmarkImplFieldID.
                                    AddressInfo_DISTRICT;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_NAME:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_NAME;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_FLOOR:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_FLOOR;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ROOM:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_ROOM;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ZONE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_ZONE;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_CROSSING1:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CROSSING1;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_CROSSING2:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CROSSING2;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_URL:
                            fid = landmarkImplFieldID.
                                    AddressInfo_URL;
                            break;
                        case JAVACALL_LOCATION_ADDRESSINFO_PHONE_NUMBER:
                            fid = landmarkImplFieldID.
                                    AddressInfo_PHONE_NUMBER;
                            break;
                        default:
                            fid = 0;
                            break;
                    }
                    if (fid != 0) {
                        /* addressInfo */
                        jsr179_jstring_from_utf16(KNIPASSARGS &stringObj, landmark->fields[i].data);
                        KNI_SetObjectField(landmarkObj, fid, stringObj);
                    }
                }
            }
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(jsropIOException, "I/O error");
            break;
    }

    KNI_EndHandles();
    KNI_ReturnInt(landmarkID);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_closeLandmarkList) {
    
    jint hndl = KNI_GetParameterAsInt(1);

    javacall_landmarkstore_landmarklist_close((javacall_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LocationPersistentStorage_updateLandmarkImpl) {
    
    jint landmarkID;
    javacall_result res;
    javacall_landmarkstore_landmark *landmark;
    jint numAddressFields;
    
    KNI_StartHandles(3);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);
    GET_PARAMETER_AS_UTF16_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    KNI_GetParameterAsObject(3, landmarkObj);
    numAddressFields = KNI_GetIntField(landmarkObj, 
            landmarkImplFieldID.numAddressInfoFields);
    landmark = JAVAME_MALLOC(SIZE_OF_LANDMARK_INFO(numAddressFields));

    if (landmark != NULL) {
        if (fill_landmark(landmarkObj, landmark, stringObj) == KNI_TRUE) {
            res = javacall_landmarkstore_landmark_update(storeName, 
                                    (javacall_handle)landmarkID, landmark);
            switch (res) {
                case JAVACALL_OK:
                    /* Landmark updated successfully */
                    break;
                case JAVACALL_INVALID_ARGUMENT:
                    /* wrong landmark ID */
                    KNI_ThrowNew(jsropIllegalArgumentException, 
                                "Landmark does not belong to this store");
                    break;
                default:
                    /* operation Failed */
                    KNI_ThrowNew(jsropIOException, "I/O error");
                    break;
            }
        }
        JAVAME_FREE(landmark);
    }

    RELEASE_UTF16_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    KNIDECL(com_sun_j2me_location_LandmarkImpl_initNativeClass) {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        landmarkImplFieldID.name = 
            KNI_GetFieldID(class_obj, "name", "Ljava/lang/String;");
        landmarkImplFieldID.description = 
            KNI_GetFieldID(class_obj, "description", "Ljava/lang/String;");
        landmarkImplFieldID.isCoordinates = 
            KNI_GetFieldID(class_obj, "isCoordinates", "Z");
        landmarkImplFieldID.latitude = 
            KNI_GetFieldID(class_obj, "latitude", "D");
        landmarkImplFieldID.longitude = 
            KNI_GetFieldID(class_obj, "longitude", "D");
        landmarkImplFieldID.altitude = 
            KNI_GetFieldID(class_obj, "altitude", "F");
        landmarkImplFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj, "horizontalAccuracy", "F");
        landmarkImplFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj, "verticalAccuracy", "F");
        landmarkImplFieldID.isAddressInfo = 
            KNI_GetFieldID(class_obj, "isAddressInfo", "Z");
        landmarkImplFieldID.numAddressInfoFields = 
            KNI_GetFieldID(class_obj, "numAddressInfoFields", "I");
        landmarkImplFieldID.AddressInfo_EXTENSION = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_EXTENSION", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_STREET = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STREET", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_POSTAL_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_POSTAL_CODE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CITY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CITY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_STATE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STATE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTRY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTRY_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY_CODE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_DISTRICT = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_DISTRICT", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_NAME = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_NAME", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_FLOOR = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_FLOOR", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_ROOM = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ROOM", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_ZONE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ZONE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CROSSING1 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING1", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CROSSING2 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING2", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_URL = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_URL", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_PHONE_NUMBER = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_PHONE_NUMBER", "Ljava/lang/String;");
    } else {
        KNI_ThrowNew(jsropNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/* Helper-functions */

/**
 * (Internal) Fill addressInfo Field.
 */
static jboolean fill_adressInfoField(jobject landmarkObj, jfieldID fieldID, 
                jobject stringObj,  javacall_location_addressinfo_fieldinfo *fieldInfo, 
                javacall_location_addressinfo_field addressInfoFieldId) {
    KNI_GetObjectField(landmarkObj, fieldID, stringObj);
    if (!KNI_IsNullHandle(stringObj)) {
        fieldInfo->fieldId = addressInfoFieldId;
        return jsr179_jstring_to_utf16(stringObj, fieldInfo->data, JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD);
    }
    return KNI_FALSE;
}

/**
 * (Internal) Fill landmark structure from the Landmark Object.
 */
static jboolean fill_landmark(jobject landmarkObj, javacall_landmarkstore_landmark *landmark, 
                                jobject stringObj) {
    /* Fill javacall_landmarkstore_landmark structure */
    /* landmark.name */
    KNI_GetObjectField(landmarkObj, landmarkImplFieldID.name, stringObj);
    if (!KNI_IsNullHandle(stringObj) && 
        KNI_GetStringLength(stringObj)<=JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME) {
        if (JAVACALL_OK != 
                jsrop_jstring_to_utf16(stringObj, landmark->name, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME)) {
            return KNI_FALSE;
        }
    } else {
        return KNI_FALSE;
    }
    /* landmark.description */
    KNI_GetObjectField(landmarkObj, landmarkImplFieldID.description, stringObj);
    jsr179_jstring_to_utf16(stringObj, landmark->description, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION);

    landmark->isValidCoordinate = KNI_GetBooleanField(landmarkObj, 
                                landmarkImplFieldID.isCoordinates);
    if (landmark->isValidCoordinate) {
        /* landmark.latitude */
        landmark->latitude = KNI_GetDoubleField(landmarkObj, 
                                    landmarkImplFieldID.latitude);
        /* landmark.longitude */
        landmark->longitude = KNI_GetDoubleField(landmarkObj, 
                                    landmarkImplFieldID.longitude);
        /* landmark.altitude */
        landmark->altitude = KNI_GetFloatField(landmarkObj, 
                                    landmarkImplFieldID.altitude);
        /* landmark.horizontalAccuracy */
        landmark->horizontalAccuracy = KNI_GetFloatField(landmarkObj, 
                                    landmarkImplFieldID.horizontalAccuracy);
        /* landmark.altitude */
        landmark->verticalAccuracy = KNI_GetFloatField(landmarkObj, 
                                    landmarkImplFieldID.verticalAccuracy);
    }

    /* AddressInfo */
    landmark->addressInfoFieldNumber = KNI_GetIntField(landmarkObj, 
            landmarkImplFieldID.numAddressInfoFields);
    if (landmark->addressInfoFieldNumber > 0) {
            jint counter = 0;
            /* AddressInfo_EXTENSION */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_EXTENSION,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_EXTENSION) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_STREET */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_STREET,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_STREET) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_POSTAL_CODE */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_POSTAL_CODE,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_POSTAL_CODE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CITY */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CITY,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_CITY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTY */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTY,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_COUNTY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_STATE */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_STATE,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_STATE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTRY */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTRY,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_COUNTRY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTRY_CODE */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTRY_CODE,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_COUNTRY_CODE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_DISTRICT */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_DISTRICT,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_DISTRICT) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_NAME */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_NAME,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_BUILDING_NAME) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_FLOOR */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_FLOOR,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_BUILDING_FLOOR) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_ROOM */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_ROOM,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ROOM) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_ZONE */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_ZONE,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_BUILDING_ZONE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CROSSING1 */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CROSSING1,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_CROSSING1) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CROSSING2 */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CROSSING2,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_CROSSING2) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_URL */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_URL,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_URL) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_PHONE_NUMBER */
            if ((counter < landmark->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_PHONE_NUMBER,
                            stringObj, 
                            &landmark->fields[counter],
                            JAVACALL_LOCATION_ADDRESSINFO_PHONE_NUMBER) == KNI_TRUE)) {
                counter++;
            }
    }
    return KNI_TRUE;
}

static jboolean jsr179_jstring_to_utf16(jobject stringObj, javacall_utf16 *string, jint len) {
    if (!KNI_IsNullHandle(stringObj)) {
        if (KNI_GetStringLength(stringObj) > 0) {
            if (JAVACALL_OK != jsrop_jstring_to_utf16(stringObj, string, len)) {
                return KNI_FALSE;
            }
        } else {
            string[0] = (javacall_utf16)0x0000;
            string[1] = (javacall_utf16)0x0000;
        }
    } else {
        string[0] = (javacall_utf16)0x0000;
        string[1] = (javacall_utf16)0xFFFF;
    }
    return KNI_TRUE;
}

static void jsr179_jstring_from_utf16(KNIDECLARGS jobject *stringObj, javacall_utf16 *string) {
    if (string != NULL) {
        if ((string[0] == (javacall_utf16)0x0000) &&
            (string[1] == (javacall_utf16)0xFFFF)) {
            KNI_ReleaseHandle(*stringObj);
        } else {
            if (JAVACALL_OK != jsrop_jstring_from_utf16_string(KNIPASSARGS string, *stringObj)) {
                KNI_ReleaseHandle(*stringObj);
            }
        }
    } else {
        KNI_ReleaseHandle(*stringObj);
    }
}
