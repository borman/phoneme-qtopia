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

#include <stdio.h>
#include "lime.h"
#include <windows.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <io.h>
#include <wchar.h>
#include <stdlib.h>
#include <errno.h>
#include "javacall_location.h"
#include "javacall_landmarkstore.h"

/** File separator */
#define FILESEP '\\'
#define MAX_FILE_NAME_LENGTH            450
#define MAX_FILE_NAME_LENGTH_WITH_PATH  512
#define MAX_ADDRESSINFO_FIELDS          17

/* size of landmarkStore and Category fields: */
#define SIZE_OF_NUMLANDMARKS    (long)sizeof(long)
#define SIZE_OF_NUMCATEGORIES   (long)sizeof(long)
#define SIZE_OF_LANDMARKID      (long)sizeof(long)
#define SIZE_OF_NAMELEN         (long)sizeof(long)
#define SIZE_OF_NAME(len)       ((len)*(long)sizeof(javacall_utf16))

/* Structures to support Landmark<->Category association */
typedef struct _landmark_decorator {
	int landmarkID;
	javacall_landmarkstore_landmark *instance;
	int numCategories;
	javacall_utf16_string *categories;
	struct _landmark_decorator *next;
	boolean created_independently; /* Whether the implementation should free this Landmark's
									  * space or let the javacall implementation do that */
}landmark_decorator;

/** Structure to support Category List Next operation */
typedef struct _categoryListType{
    javacall_handle handle;
    int numCategories;
    javacall_utf16_string* categoryNames;
    int curCategory;
    struct _categoryListType *next;
}categoryListType;

categoryListType* categoryListHead = NULL;

/** Structure to support Landmark List Next operation */
typedef struct _landmarkListType{
    javacall_handle handle; /* descriptor */
    javacall_utf16_string storeName; /* LandmarkStore Name */
    javacall_utf16_string categoryName; /* Category Name or NULL */
    long numLandmarks;
    landmark_decorator *landmarks;
    long currentID;
    landmark_decorator *curLandmark;
    struct _landmarkListType *next;
}landmarkListType;

landmarkListType* landmarkListHead = NULL;

landmark_decorator* landmarkToUpdate = NULL;

boolean landmark_store_list_open = FALSE; /* is an allocated list already open */
javacall_utf16_string *landmark_store_list = NULL; /* list names array */
int landmark_store_list_size = -1; /* size of list names array */
int landmark_store_current_item = -1; /* current item in list names array */
#define LANDMARK_STORE_HANDLE 1 /* handle of list names array (only 1 exists) */


/************************************************************************/
/* PROTOTYPES                                                           */
/************************************************************************/
/* returns the specific type, pos is moved by the amount of bytes read */
boolean read_boolean(const byte* stream, int *pos);
byte read_byte(const byte* stream, int *pos);
short read_short(const byte* stream, int *pos);
int read_int(const byte* stream, int *pos);
float read_float(const byte* stream, int *pos);
double read_double(const byte* stream, int *pos);
__int64 read_long(const byte* stream, int *pos);

/* Returns the length of the string variable, pos is moved by the bytes read		*/
/* Returns -1 instead of UTFDataFormatException() (from DataInputStream in Java)	*/
/* It is up to the user to free the returned string!!!								*/
int read_utf(const byte* stream, int *pos, javacall_utf16_string /*OUT*/ *outString);

/**
 * Methods to convert Java's
 * serialized byte array to the Landmark
 * struct javacall_landmarkstore_landmark.
 * returns the number of bytes read/written from/to the byte array
 */
int bytearray_to_landmark(
			const byte *serialized_lm,
			int *serialized_pos,
			landmark_decorator /*OUT*/ **lmdec);
int landmark_to_bytearray(
			const landmark_decorator *landmark,
			byte /*OUT*/ **serialized_lm);

int new_landmark_to_bytearray(const landmark_decorator *landmark,
							  byte /*OUT*/ **serialized_lm,
							  boolean preallocated);
/************************************************************************/

byte read_byte(const byte* stream, int *pos) {
	boolean byte_read = stream[*pos];
	*pos += sizeof(byte);
	return byte_read;
}

boolean read_boolean(const byte* stream, int *pos) {
	return (boolean)read_byte(stream, pos);
}

short read_short(const byte* stream, int *pos) {
	byte byte1 = 0, byte2 = 0;
	short short_read = 0;
	byte1 = stream[*pos];
	byte2 = stream[*pos+1];

	short_read = (byte1<<8) | (byte2);
	*pos += sizeof(short);

	return short_read;
}

int read_int(const byte* stream, int *pos) {
	byte byte1 = 0, byte2 = 0, byte3 = 0, byte4 = 0;
	int int_read = 0;
	byte1 = read_byte(stream, pos);
	byte2 = read_byte(stream, pos);
	byte3 = read_byte(stream, pos);
	byte4 = read_byte(stream, pos);

	int_read = (byte1<<24) | (byte2<<16) | (byte3<<8) | (byte4<<0);

	return int_read;
}

__int64 read_long(const byte* stream, int *pos) {
	__int64 long_read = 0;
	byte *bytes_read = NULL;

	bytes_read = malloc(sizeof(__int64));
    if (bytes_read != NULL) {
	    bytes_read[0] = read_byte(stream, pos);
	    bytes_read[1] = read_byte(stream, pos);
	    bytes_read[2] = read_byte(stream, pos);
	    bytes_read[3] = read_byte(stream, pos);
	    bytes_read[4] = read_byte(stream, pos);
	    bytes_read[5] = read_byte(stream, pos);
	    bytes_read[6] = read_byte(stream, pos);
	    bytes_read[7] = read_byte(stream, pos);

	    memcpy(&long_read, bytes_read, sizeof(__int64));
	    free(bytes_read);
    }

	return long_read;
}

float read_float(const byte* stream, int *pos) {
	float float_read = 0;
	byte *bytes_read = NULL;
	int i = 0;

	bytes_read = malloc(sizeof(float));
    if (bytes_read != NULL) {
	    for (i=sizeof(float)-1 ; i>=0 ; i--)
		    bytes_read[i] = read_byte(stream, pos);

	    memcpy(&float_read, bytes_read, sizeof(float));
	    free(bytes_read);
    }

	return float_read;
}

double read_double(const byte* stream, int *pos) {
	double double_read = 0;
	byte *bytes_read = NULL;
	int i = 0;

	bytes_read = malloc(sizeof(double));
    if (bytes_read != NULL) {
	    for (i=sizeof(double)-1 ; i>=0 ; i--)
		    bytes_read[i] = read_byte(stream, pos);

	    memcpy(&double_read, bytes_read, sizeof(double));
	    free(bytes_read);
    }

	return double_read;
}

int read_utf(const byte* stream, int *pos, javacall_utf16_string /*OUT*/ *outString) {
    unsigned short utflen = read_short(stream, pos);
    unsigned short *str = NULL;
    byte *bytearr = NULL;
    int c = 0, char2 = 0, char3 = 0, i = 0;
    int count = 0;
    int strlen = 0;
    boolean error = FALSE;

    str = malloc((utflen+1)*sizeof(short));
    if(!str)
            return -1;

    bytearr = malloc(utflen*sizeof(byte));
    if(!bytearr) {
            free(str);
            return -1;
        }

    for (i=0 ; i<utflen ; i++)
            bytearr[i] = read_byte(stream, pos);

    while (count < utflen && !error) {
        c = (int) bytearr[count] & 0xff;
        switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                /* 0xxxxxxx*/
                count++;
                str[strlen++] = (unsigned short)c;
                break;
            case 12: case 13:
                /* 110x xxxx   10xx xxxx*/
                count += 2;
                if (count > utflen) {
                    error = TRUE;
                    break;
                }
                char2 = (int) bytearr[count-1];
                if ((char2 & 0xC0) != 0x80) {
                    error = TRUE;
                    break;
                }
                str[strlen++] = (unsigned short)(((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                count += 3;
                if (count > utflen) {
                    error = TRUE;
                    break;
                }
                char2 = (int) bytearr[count-2];
                char3 = (int) bytearr[count-1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                    error = TRUE;
                    break;
                }
                str[strlen++] = (unsigned short)(((c     & 0x0F) << 12) |
                                       ((char2 & 0x3F) << 6)  |
                                       ((char3 & 0x3F) << 0));
                break;
            default:
                /* 10xx xxxx,  1111 xxxx */
                error = TRUE;
            }
    }

    if (!error) {
	    str[strlen] = NULL; /* end of string */

	    *outString = malloc(SIZE_OF_NAME(strlen+1));
	    memcpy(*outString, str, SIZE_OF_NAME(strlen+1));
    } else {
        strlen = -1;
    }

    free(str);
    free(bytearr);

    // The number of chars produced may be less than utflen
    return strlen;
}

void write_byte(byte** stream, int *pos, byte toWrite) {
	(*stream)[*pos] = toWrite;
	*pos += sizeof(byte);
}

void write_boolean(byte** stream, int *pos, boolean toWrite) {
	write_byte(stream, pos, toWrite);
}

void write_short(byte** stream, int *pos, short toWrite) {
	byte byte1 = 0, byte2 = 0;

	byte1 = toWrite>>8;
	byte2 = toWrite>>0;
	write_byte(stream, pos, byte1);
	write_byte(stream, pos, byte2);
}

void write_int(byte** stream, int *pos, int toWrite) {
	byte byte1 = 0, byte2 = 0, byte3 = 0, byte4 = 0;

	byte1 = toWrite>>24;
	byte2 = toWrite>>16;
	byte3 = toWrite>>8;
	byte4 = toWrite>>0;
	write_byte(stream, pos, byte1);
	write_byte(stream, pos, byte2);
	write_byte(stream, pos, byte3);
	write_byte(stream, pos, byte4);
}

void write_long(byte** stream, int *pos, long toWrite) {
	int int1 = 0, int2 = 0;

	int1 = toWrite>>32;
	int2 = toWrite>>0;
	write_int(stream, pos, int1);
	write_int(stream, pos, int2);
}

void write_float(byte** stream, int *pos, float toWrite) {
	byte *pByte = malloc(sizeof(float)*sizeof(byte));
    if (pByte != NULL) {
        memcpy(pByte, &toWrite, sizeof(float));

        write_byte(stream, pos, pByte[3]);
        write_byte(stream, pos, pByte[2]);
        write_byte(stream, pos, pByte[1]);
	    write_byte(stream, pos, pByte[0]);

	    free(pByte);
    }
}

void write_double(byte** stream, int *pos, double toWrite) {
    byte *pByte = malloc(sizeof(double)*sizeof(byte));
    if (pByte != NULL) {
        memcpy(pByte, &toWrite, sizeof(double));

        write_byte(stream, pos, pByte[7]);
        write_byte(stream, pos, pByte[6]);
        write_byte(stream, pos, pByte[5]);
        write_byte(stream, pos, pByte[4]);
        write_byte(stream, pos, pByte[3]);
        write_byte(stream, pos, pByte[2]);
        write_byte(stream, pos, pByte[1]);
        write_byte(stream, pos, pByte[0]);

	    free(pByte);
    }
}

javacall_result write_utf(byte** stream, int *pos, javacall_utf16_string toWrite) {
    int strlen = wcslen(toWrite);
    int utflen = 0;
    unsigned short *charr = NULL;
    int c = 0, count = 0, i = 0;
	byte *bytearr = NULL;

	charr = malloc((strlen+1)*sizeof(short));
	if (!charr)
		return JAVACALL_FAIL;

    wcscpy(charr, toWrite);

    for (i = 0; i < strlen; i++) {
        c = charr[i];
        if ((c >= 0x0001) && (c <= 0x007F)) {
            utflen++;
        } else if (c > 0x07FF) {
            utflen += 3;
        } else {
            utflen += 2;
        }
    }

    if (utflen > 65535) {
        free(charr);
        return JAVACALL_FAIL;
    }

    bytearr = malloc((utflen+2)*sizeof(byte));
    if (!bytearr) {
        free(charr);
		return JAVACALL_FAIL;
    }

    /* write the string's size */
    bytearr[count++] = (byte) ((utflen >> 8) & 0xFF);
    bytearr[count++] = (byte) ((utflen >> 0) & 0xFF);

    /* write the string */
    for (i = 0; i < strlen; i++) {
        c = charr[i];
        if ((c >= 0x0001) && (c <= 0x007F)) {
            bytearr[count++] = (byte) c;
        } else if (c > 0x07FF) {
            bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
            bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
            bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
        } else {
            bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
            bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
        }
    }

    /* the byte[] should already be allocated */
    for (i=0 ; i<utflen+2 ; i++)
        write_byte(stream, pos, bytearr[i]);

    free(charr);
    free(bytearr);

    return JAVACALL_OK;
 }

void write_pcsl_null_string(javacall_utf16_string string) {
	string[0] = 0x0000;
	string[1] = 0xFFFF;
}

int bytearray_to_landmark(const byte *serialized_lm, int *serialized_pos,
						  landmark_decorator /*OUT*/ **lmdec) {
	javacall_utf16_string tempString = NULL;
	int i, j, k;
	boolean ai_exists, coords_exist, descr_exists, name_exists, cat_in_lm;
	javacall_location_addressinfo_fieldinfo *ai = NULL;
	javacall_utf16_string temp_utf = NULL;
	int original_pos = *serialized_pos;
	landmark_decorator *landmark = NULL;
	int strlen = 0;
	landmark = malloc(sizeof(landmark_decorator));
	if (NULL == landmark)
		return -1;

	landmark->instance = malloc(SIZE_OF_LANDMARK_INFO(MAX_ADDRESSINFO_FIELDS));
    if (NULL == landmark->instance) {
        free(landmark);
		return -1;
    }
	ai = landmark->instance->fields;
	landmark->instance->addressInfoFieldNumber = 0;

	/* read AddressInfo */
	ai_exists = read_boolean(serialized_lm, serialized_pos);
	if (ai_exists) {
        k=0;
		for (i=0 ; i<MAX_ADDRESSINFO_FIELDS ; i++) {
			/* now the stream goes: |boolean(field exists)|name(if exists)|boolean|...| */
			if (read_boolean(serialized_lm, serialized_pos)) {
				landmark->instance->addressInfoFieldNumber++;
                ai[k].fieldId = i+1;
				strlen = read_utf(serialized_lm, serialized_pos, &temp_utf);
				tempString = ai[k++].data;
				memset(tempString, 0, SIZE_OF_NAME(JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD));
				if (temp_utf) {
                    if (strlen <= JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD) {
					    wcsncpy(tempString, temp_utf, strlen);
                    }
					free(temp_utf);
					temp_utf = NULL;
				}
			}
		}
	}

	/* read coordinates */
	coords_exist = read_boolean(serialized_lm, serialized_pos);
	if (coords_exist) {
		landmark->instance->verticalAccuracy = read_float(serialized_lm, serialized_pos);
		landmark->instance->horizontalAccuracy = read_float(serialized_lm, serialized_pos);
		landmark->instance->altitude = read_float(serialized_lm, serialized_pos);
		landmark->instance->longitude = read_double(serialized_lm, serialized_pos);
		landmark->instance->latitude = read_double(serialized_lm, serialized_pos);

		landmark->instance->isValidCoordinate = JAVACALL_TRUE;
	} else landmark->instance->isValidCoordinate = JAVACALL_FALSE;

	/* read description */
	descr_exists = read_boolean(serialized_lm, serialized_pos);
	memset(landmark->instance->description, 0, SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION));
	if (descr_exists) {
		strlen = read_utf(serialized_lm, serialized_pos, &temp_utf);
		if (temp_utf) {
            if (strlen <= JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION) {
			    wcsncpy(landmark->instance->description, temp_utf, strlen);
            }
			free(temp_utf);
			temp_utf = NULL;
		}
	} else write_pcsl_null_string(landmark->instance->description);

	/* read name */
	name_exists = read_boolean(serialized_lm, serialized_pos);
	memset(landmark->instance->name, 0, SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME));
	if (name_exists) {
		strlen = read_utf(serialized_lm, serialized_pos, &temp_utf);
		if (temp_utf) {
            if (strlen <= JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME) {
			    wcsncpy(landmark->instance->name, temp_utf, strlen);
            }
			free(temp_utf);
			temp_utf = NULL;
		}
	} else write_pcsl_null_string(landmark->instance->name);

	/* read categories */
	landmark->numCategories = read_int(serialized_lm, serialized_pos);
    if (landmark->numCategories > 0) {
	    landmark->categories = malloc(sizeof(javacall_utf16_string)*landmark->numCategories);
        if (NULL==landmark->categories) {
            free(landmark->instance);
            free(landmark);
            return -1;
        }
	    memset(landmark->categories, 0, sizeof(javacall_utf16_string)*landmark->numCategories);
	    for (i=0 ; i<landmark->numCategories ; i++) {
		    cat_in_lm = read_boolean(serialized_lm, serialized_pos);
		    if (cat_in_lm) {
			    landmark->categories[i] = malloc(SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME));
			    if (NULL==landmark->categories[i]) {
				    for (j=0 ; j<i ; j++){
					    free(landmark->categories[j]);
				    }
				    free(landmark->categories);
                    free(landmark->instance);
                    free(landmark);
				    return -1;
			    }
			    memset(landmark->categories[i], 0, JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME);
			    strlen = read_utf(serialized_lm, serialized_pos, &temp_utf);
			    if (temp_utf) {
    				wcsncpy(landmark->categories[i], temp_utf, 
                            JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
                    landmark->categories[i][JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
				    free(temp_utf);
				    temp_utf = NULL;
			    }
		    }
	    }
    } else {
        landmark->categories = NULL;
    }
	landmark->next = NULL;

	*lmdec = landmark;

	return (*serialized_pos)-original_pos;
}

int landmark_to_bytearray(const landmark_decorator *landmark,
						  byte /*OUT*/ **serialized_lm) {
	int read_bytes = 0;
	int pos = 0;
	int max_landmark_size = 0;
	byte *stream = NULL;
	byte *new_stream = NULL;

	/* Calculate maximum size to be written */
	max_landmark_size += sizeof(int) /* Landmark ID */
		+ sizeof(int) /* Landmark size */
		+ sizeof(byte) /* AddressInfo exists? */
		+ MAX_ADDRESSINFO_FIELDS*SIZE_OF_NAME(JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD) /* AddressInfo data */
		+ sizeof(byte) /* Coordinates exist? */
		+ sizeof(float)*3 /* vertical accuracy, horizontal accuracy, altitude */
		+ sizeof(double)*2 /* longitude, latitude */
		+ sizeof(byte) /* Description exists? */
		+ SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION) /* Description size */
		+ sizeof(byte) /* Name exists? */
		+ SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME) /* Name size */
		+ sizeof(int) /* category count */
		+ landmark->numCategories*SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME);

	stream = malloc(max_landmark_size);
	if (NULL==stream)
		return -1;

	/* Write Landmark ID */
	write_int(&stream, &pos, landmark->landmarkID);
	/* save the position to write the object size later on */

	new_stream = stream+(sizeof(int)*2);
	read_bytes = new_landmark_to_bytearray(landmark, &new_stream, TRUE);
	if (-1==read_bytes) {
		/* error */
		free(stream);
		return -1;
	}

	/* Write size of landmark */
	write_int(&stream, &pos, read_bytes);
	/* add the landmark id and size bytes */
	read_bytes += (sizeof(int)*2);

	*serialized_lm = malloc(read_bytes*sizeof(byte));
	if (NULL == *serialized_lm) {
		free(stream);
		return -1;
	}
	memcpy(*serialized_lm, stream, read_bytes);

	free(stream);
	return read_bytes;
}

int new_landmark_to_bytearray(const landmark_decorator *landmark,
							  byte /*OUT*/ **serialized_lm,
							  boolean preallocated) {
	int written_bytes = 0;
	int *pos = &written_bytes;
	int max_landmark_size = 0;
	byte *stream = NULL;
	int i = 0, j = 0;
	boolean fieldFound = FALSE;
	/* Calculate maximum size to be written */
	max_landmark_size += sizeof(byte) /* AddressInfo exists? */
			+ MAX_ADDRESSINFO_FIELDS*SIZE_OF_NAME(JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD) /* AddressInfo data */
			+ sizeof(byte) /* Coordinates exist? */
			+ sizeof(float)*3 /* ver. accuracy, hor. accuracy, altitude */
			+ sizeof(double)*2 /* longitude, latitude */
			+ sizeof(byte) /* Description exists? */
			+ SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION) /* Description size */
			+ sizeof(byte) /* Name exists? */
			+ SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME) /* Name size */
			+ sizeof(int) /* category count */
			+ landmark->numCategories*SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME);

	stream = (byte*)malloc(max_landmark_size);
	if (NULL==stream)
		return -1;

	/* Write AddressInfo */
	write_boolean(&stream, pos, TRUE);
	for (i=0 ; i<MAX_ADDRESSINFO_FIELDS ; i++) {
		for (j=0 ; j<landmark->instance->addressInfoFieldNumber ; j++) {
			/* only write the fields that are populated in the struct to the stream */
			if (i+1 == landmark->instance->fields[j].fieldId) {
				write_boolean(&stream, pos, TRUE);
				write_utf(&stream, pos, landmark->instance->fields[j].data);
				fieldFound = TRUE;
				break;
			}
		}
		if (!fieldFound) {
			write_boolean(&stream, pos, FALSE);
		}

		fieldFound = FALSE; /* for next iteration */
	}

	/* Write coordinates */
	if (landmark->instance->isValidCoordinate == JAVACALL_TRUE) {
		write_boolean(&stream, pos, TRUE);
		write_float(&stream, pos, landmark->instance->verticalAccuracy);
		write_float(&stream, pos, landmark->instance->horizontalAccuracy);
		write_float(&stream, pos, landmark->instance->altitude);
		write_double(&stream, pos, landmark->instance->longitude);
		write_double(&stream, pos, landmark->instance->latitude);
	} else write_boolean(&stream, pos, FALSE);

	/* Write description */
	if (!wcscmp(landmark->instance->description, L""))
		write_boolean(&stream, pos, FALSE);
	else {
		write_boolean(&stream, pos, TRUE);
		write_utf(&stream, pos, landmark->instance->description);
	}

	/* Write name */
	if (!wcscmp(landmark->instance->name, L""))
		write_boolean(&stream, pos, FALSE);
	else {
		write_boolean(&stream, pos, TRUE);
		write_utf(&stream, pos, landmark->instance->name);
	}

	/* Write categories */
	write_int(&stream, pos, landmark->numCategories);
	for (i=0 ; i<landmark->numCategories ; i++) {
		write_boolean(&stream, pos, TRUE);
		write_utf(&stream, pos, landmark->categories[i]);
	}

	/* copy the ready stream and return it */
	if (!preallocated) {
		*serialized_lm = malloc(sizeof(byte)*written_bytes);
        if (NULL == *serialized_lm) {
            free(stream);
			return -1;
        }
	}
	memcpy(*serialized_lm, stream, written_bytes);
	free(stream);

	return written_bytes;
}

/* frees an allocated space for landmark_decorator */
void free_landmark_space(landmark_decorator *landmark) {
	int i;
	for (i=0 ; i<landmark->numCategories ; i++){
        if (landmark->categories[i]) {
		    free(landmark->categories[i]);
        }
	}
    if (landmark->instance != NULL) {
        free(landmark->instance);
    }
	free(landmark);
}

/* Returns the landmark_decorator struct associated with the given parameters
 * or NULL if no such was found. Searches only in open lists */
landmark_decorator *find_landmark_decorator_in_open_list(
							javacall_utf16_string storeName,
							int landmarkID,
							javacall_utf16_string categoryName) {
	int i;
    landmarkListType *curEl, *foundEl=NULL;
	landmark_decorator *landmark = NULL;
	landmark_decorator *landmark_temp = NULL;

	curEl = landmarkListHead;

	while (curEl!= NULL) {
		if (curEl->storeName == storeName ||
			!wcscmp(curEl->storeName,storeName)) {
			if (curEl->categoryName == categoryName ||
				!wcscmp(curEl->categoryName, categoryName)) {
				foundEl = curEl;
				break;
			}
		}
		curEl = curEl->next;
	}

	if (foundEl!=NULL) {
        landmark_temp = foundEl->landmarks;
		for (i=0 ; i<foundEl->numLandmarks ; i++){
			if (landmark_temp->landmarkID == landmarkID) {
				/* the landmark was found in an open list */
				landmark = landmark_temp;
				break;
            } else {
                landmark_temp = landmark_temp->next;
            }
		}
	}

	return landmark;
}

/* Returns the landmark_decorator struct associated with the given parameters in foundLandmark
	JAVACALL_OK					success
	JAVACALL_FAIL				failure
	JAVACALL_INVALID_ARGUMENT	invalid category */
javacall_result find_landmark_decorator(
							const javacall_utf16_string storeName,
							const int landmarkID,
							const javacall_utf16_string categoryName,
							landmark_decorator /*OUT*/ **foundLandmark) {
	landmark_decorator *newLandmark = NULL;
	javacall_handle lmlistHandle;
	javacall_result result;
	int i;
	(*foundLandmark) = NULL;

	/* First see if it exists in any of the open lists */
	(*foundLandmark) = find_landmark_decorator_in_open_list(storeName, landmarkID, categoryName);

	if ((*foundLandmark)!=NULL) {
		return JAVACALL_OK;
	} else {
		/* the landmark wasn't in one of the open list, we need to open
		 * a new list and fetch the data
		 */

		/* Open a new list and get the landmark */
		result = javacall_landmarkstore_landmarklist_open(storeName, categoryName, &lmlistHandle);
		if (JAVACALL_INVALID_ARGUMENT== result) {
			javacall_landmarkstore_landmarklist_close(lmlistHandle);
			/* category is invalid */
			return JAVACALL_INVALID_ARGUMENT;
		}
		(*foundLandmark) = find_landmark_decorator_in_open_list(storeName, landmarkID, categoryName);
		if ((*foundLandmark) == NULL) {
			/* Landmark doesn't exist */
			javacall_landmarkstore_landmarklist_close(lmlistHandle);
			return JAVACALL_FAIL;
		}

		/* copy the landmark to allocated space because the current address wouldn't be
		 * valid once the landmark list is closed
		 */
		newLandmark = malloc(sizeof(landmark_decorator));
		if (NULL==newLandmark) {
			return JAVACALL_FAIL;
		}

		newLandmark->landmarkID = (*foundLandmark)->landmarkID;

		/* space for categories */
		newLandmark->numCategories = (*foundLandmark)->numCategories;
		if (newLandmark->numCategories != 0) {
			/* allocate space for the number of strings */
			newLandmark->categories = malloc((*foundLandmark)->numCategories *
				sizeof(javacall_utf16_string));
			if (NULL==newLandmark->categories) {
				free(newLandmark);
				return JAVACALL_FAIL;
			}
			/* allocate space for the strings themselves */
			for (i=0 ; i<newLandmark->numCategories ; i++)
            {
					newLandmark->categories[i] =
						malloc(SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME));
					if (newLandmark->categories[i] != NULL) {
						wcsncpy(newLandmark->categories[i], (*foundLandmark)->categories[i], 
                            JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
                        newLandmark->categories[i][JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
					} else {
						free(newLandmark->categories);
						free(newLandmark);
						return JAVACALL_FAIL;
					}
			}
		}

		/* space for javacall_landmarkstore_landmark instance */
		newLandmark->instance = malloc(SIZE_OF_LANDMARK_INFO(MAX_ADDRESSINFO_FIELDS));
		if (NULL==newLandmark->instance) {
            if (newLandmark->categories != NULL) {
    		    free(newLandmark->categories);
            }
			free(newLandmark);
			return JAVACALL_FAIL;
		}

		/* copy the found landmark fields to a new landmark instance */
		newLandmark->instance->latitude =
			(*foundLandmark)->instance->latitude ;
		newLandmark->instance->longitude =
			(*foundLandmark)->instance->longitude ;
		newLandmark->instance->altitude =
			(*foundLandmark)->instance->altitude ;
		newLandmark->instance->horizontalAccuracy =
			(*foundLandmark)->instance->horizontalAccuracy ;
		newLandmark->instance->verticalAccuracy =
			(*foundLandmark)->instance->verticalAccuracy ;
		newLandmark->instance->isValidCoordinate =
			(*foundLandmark)->instance->isValidCoordinate;
		newLandmark->instance->addressInfoFieldNumber =
			(*foundLandmark)->instance->addressInfoFieldNumber;

		for (i=0 ; i<newLandmark->instance->addressInfoFieldNumber ; i++) {
			newLandmark->instance->fields[i] = (*foundLandmark)->instance->fields[i];
		}

		wcsncpy(newLandmark->instance->name, (*foundLandmark)->instance->name, 
            JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME-1);
        newLandmark->instance->name[JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME-1] = NULL;
		wcsncpy(newLandmark->instance->description, (*foundLandmark)->instance->description,
            JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION-1);
        newLandmark->instance->description[JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION-1] = NULL;

		/* make sure that the implementation knows it should free this space */
		newLandmark->created_independently = TRUE;

		/* close the opened landmark list */
		javacall_landmarkstore_landmarklist_close(lmlistHandle);

		*foundLandmark = newLandmark;
		return JAVACALL_OK;
	}
}

/************************************************************************/
/* Parses a wide char string (original) delimited with a certain		*/
/* character (delimiter) and returns it as an array of wide char strings*/
/* (parsed).															*/
/* Returns the size of the array or -1 for error.						*/
/************************************************************************/
int wcsparse(const javacall_utf16_string original,
			 const javacall_utf16 delimiter,
			 javacall_utf16_string /*OUT*/ **parsed) {
	javacall_utf16_string temp_original, temp_string;
	int size = 0, num_of_strings = 0;
	int i,j;

	size = wcslen(original);
	temp_original = malloc(SIZE_OF_NAME(size+1));

	if (NULL == temp_original)
		return -1;

	wcscpy(temp_original,original);
	if (wcstok(temp_original,&delimiter)!=NULL)
		num_of_strings++;
	else {
		free(temp_original);
		return 0;
	}

	while (wcstok(NULL,&delimiter))
		num_of_strings++;

	*parsed = malloc(num_of_strings*sizeof(javacall_utf16_string));

	if (NULL == parsed) {
		free(temp_original);
		free(parsed);
		return -1;
	}


	temp_string = wcstok(original,&delimiter);

	for (i=0 ; i<num_of_strings ; i++) {
		size = wcslen(temp_string);
		(*parsed)[i] = malloc(SIZE_OF_NAME(size+1));
		if (NULL ==(*parsed)[i]) {
			for (j=0 ; j<i ; j++)
				free((*parsed)[j]);
			free(temp_original);
			free(parsed);
			return -1;
		}

		memset((*parsed)[i],0,SIZE_OF_NAME(size+1));
		wcscpy((*parsed)[i],temp_string);
		temp_string = wcstok(NULL,&delimiter);
	}

	free(temp_original);

	return num_of_strings;
}

/**
 * Adds a landmark to a landmark store.
 *
 * @param landmarkStoreName where the landmark will be added
 * @param landmark to add
 * @param categoryName where the landmark will belong to, NULL implies that the landmark does not belong to any category.
 * @param outLandmarkID returned id of added landmark
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on error
 * @retval JAVACALL_INVALID_ARGUMENT  if the category name is invalid or the landmark has a longer name field than the implementation can support.
 */
javacall_result javacall_landmarkstore_landmark_add_to_landmarkstore(
        const javacall_utf16_string landmarkStoreName,
        const javacall_landmarkstore_landmark* landmark,
        const javacall_utf16_string categoryName,
        javacall_handle* /*OUT*/outLandmarkID) {
	static LimeFunction *f = NULL;
	int newLandmarkID;
	landmark_decorator newLandmark;
	byte *serialized_lm;
	int serialized_size;
	javacall_handle categoryHandle = NULL;
	javacall_utf16_string category = NULL;
	javacall_bool category_found = JAVACALL_FALSE;
	int wLandmarkStoreNameLen = 0;

	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	/* check to see if the category name is valid */
	if (categoryName != NULL) {
		javacall_landmarkstore_categorylist_open(landmarkStoreName, &categoryHandle);
		javacall_landmarkstore_categorylist_next(categoryHandle, &category);
		while (category != NULL) {
			if (!wcscmp(category, categoryName)) {
				category_found = JAVACALL_TRUE;
				break;
			}
			javacall_landmarkstore_categorylist_next(categoryHandle, &category);
		}
		javacall_landmarkstore_categorylist_close(categoryHandle);

		if (!category_found) {
			return JAVACALL_INVALID_ARGUMENT;
		}
	}

	if (NULL != categoryName) {
		newLandmark.categories = malloc(sizeof(javacall_utf16_string));
		if (NULL==newLandmark.categories)
			return JAVACALL_FAIL;
		newLandmark.categories[0] = malloc(SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME));
		if (NULL==newLandmark.categories[0]) {
			free(newLandmark.categories);
			return JAVACALL_FAIL;
		}
        if (NULL != categoryName) {
			wcsncpy(newLandmark.categories[0], categoryName, JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
            newLandmark.categories[0][JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
        } else newLandmark.categories[0] = NULL;
		newLandmark.numCategories = 1;
	} else {
		newLandmark.categories = NULL;
		newLandmark.numCategories = 0;
	}
	newLandmark.created_independently = TRUE;
	newLandmark.instance = landmark;
	newLandmark.next = NULL;

	serialized_size = new_landmark_to_bytearray(&newLandmark, &serialized_lm, FALSE);
	if (-1==serialized_size) {
		free(newLandmark.categories[0]);
		free(newLandmark.categories);
		return JAVACALL_FAIL;
	}

	/* int addLandmarkToStore(byte[] lm, String storeName) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "addLandmarkToStore");
    }

    f->call(f, &newLandmarkID,
			serialized_lm,
			serialized_size,
			landmarkStoreName,
			wLandmarkStoreNameLen);

	*outLandmarkID = (javacall_handle)newLandmarkID;
	newLandmark.landmarkID = newLandmarkID;
	if (newLandmark.categories && newLandmark.categories[0])
		free(newLandmark.categories[0]);
	if (newLandmark.categories)
		free(newLandmark.categories);


	return JAVACALL_OK;
}

/**
 * Adds a landmark to a category.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID landmark id to add
 * @param categoryName which the landmark will be added to
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on error
 * @retval JAVACALL_INVALID_ARGUMENT  if the category name is invalid
 */
javacall_result javacall_landmarkstore_landmark_add_to_category(
        const javacall_utf16_string landmarkStoreName,
        javacall_handle landmarkID,
        const javacall_utf16_string categoryName) {
	/* two things need to be done:
	 * 1. add the landmark to the category in the persistent storage
	 * 2. if the landmark is in an already open list, update it there
	 */

	landmark_decorator *foundLandmark = NULL;
	javacall_result result;
	landmarkListType *curEl, *foundEl=NULL;
	int i=0, j=0, k=0;
	javacall_utf16_string *tempCategories = NULL;

	/* first get a landmark_decorator object from the given parameters */
	/* category name is NULL here because we want to find the landmark */
	/* among all of the landmarks in the store */
	result = find_landmark_decorator(landmarkStoreName, (int)landmarkID, NULL, &foundLandmark);
	if (JAVACALL_FAIL==result)
		return JAVACALL_FAIL;
	if (JAVACALL_INVALID_ARGUMENT==result)
		return JAVACALL_INVALID_ARGUMENT;

	/* add the category to the landmark_decorator */
	tempCategories = malloc(sizeof(javacall_utf16_string)*(foundLandmark->numCategories+1));
	if (NULL == tempCategories)
		return JAVACALL_FAIL;
	for (k=0 ; k<foundLandmark->numCategories ; k++) {
		tempCategories[k] = foundLandmark->categories[k];
	}
	tempCategories[foundLandmark->numCategories] =
		malloc(SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME));
	if (NULL==tempCategories[foundLandmark->numCategories]) {
		free(tempCategories);
		return JAVACALL_FAIL;
	}
	if (foundLandmark->numCategories > 0 && foundLandmark->categories){
		free(foundLandmark->categories);
	}
	wcsncpy(tempCategories[foundLandmark->numCategories], categoryName, 
        JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
    tempCategories[foundLandmark->numCategories][JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
	foundLandmark->categories = tempCategories;
	foundLandmark->numCategories++;

	landmarkToUpdate = foundLandmark;

	/* update the landmark in the persistent storage */
	result = javacall_landmarkstore_landmark_update(landmarkStoreName,
													landmarkID,
													foundLandmark->instance);
	if (JAVACALL_OK!=result) {
		/* something's wrong */
		if (foundLandmark->created_independently)
			free_landmark_space(foundLandmark);
		return result;
	}

	if (TRUE == foundLandmark->created_independently) {
		free_landmark_space(foundLandmark);
	}

	return JAVACALL_OK;
}

/**
 * Update existing landmark in the landmark store.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID landmark id to update
 * @param landmark to update
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on error
 * @retval JAVACALL_INVALID_ARGUMENT  if the landmarkID is invalid
 */
javacall_result javacall_landmarkstore_landmark_update(
        const javacall_utf16_string landmarkStoreName,
        javacall_handle landmarkID,
        const javacall_landmarkstore_landmark* landmark) {
	javacall_handle categoryHandle = NULL;
	javacall_utf16_string categoryName = NULL;
	landmark_decorator *foundLandmark = NULL;
	byte *serializedLandmark = NULL;
	int serializedLandmarkSize = 0;
	static LimeFunction *f = NULL;
	javacall_result result;
	int ret = 0;
	int wLandmarkStoreNameLen = 0;
	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	if (landmarkToUpdate == NULL) {
		/* get the categories list from the store and search for the landmark in each of them */
		javacall_landmarkstore_categorylist_open(landmarkStoreName, &categoryHandle);
		do {
			javacall_landmarkstore_categorylist_next(categoryHandle, &categoryName);
			result = find_landmark_decorator(landmarkStoreName,
											 (int)landmarkID,
											 categoryName,
											 &foundLandmark);
			if (foundLandmark != NULL)
				break;
		} while (categoryName != NULL);
		javacall_landmarkstore_categorylist_close(categoryHandle);

		if (JAVACALL_FAIL == result)
			return JAVACALL_INVALID_ARGUMENT;
		if (JAVACALL_INVALID_ARGUMENT == result || NULL == foundLandmark)
			return JAVACALL_FAIL;

		foundLandmark->instance = landmark;

		/* get the landmark in byte[] form to serialize */
		serializedLandmarkSize = new_landmark_to_bytearray(foundLandmark,
														   &serializedLandmark,
														   FALSE);
		if (-1 == serializedLandmarkSize) {
			/* check if the returned landmark_decorator should be freed */
			if (foundLandmark->created_independently) {
				free_landmark_space(foundLandmark);
			}
			return JAVACALL_FAIL;
		}
	} else {
		/* get the landmark in byte[] form to serialize */
		serializedLandmarkSize = new_landmark_to_bytearray(landmarkToUpdate,
														   &serializedLandmark,
														   FALSE);
		if (-1 == serializedLandmarkSize) {
			/* check if the returned landmark_decorator should be freed */
			if (foundLandmark->created_independently) {
				free_landmark_space(foundLandmark);
			}
			landmarkToUpdate = NULL;
			return JAVACALL_FAIL;
		}
		landmarkToUpdate = NULL;
	}

	/* void updateLandmarkInStore(String storeName, int recordID, byte[] lmData) */
	if (f == NULL) {
		f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "updateLandmarkInStore");
	}

	f->call(f, &ret, landmarkStoreName, wLandmarkStoreNameLen, landmarkID,
		serializedLandmark, serializedLandmarkSize);

	free (serializedLandmark);

	return JAVACALL_OK;
}

/**
 * Deletes a landmark from a landmark store.
 *
 * This function removes the specified landmark from all categories and deletes the information from this landmark store.
 * If the specified landmark does not belong to this landmark store, then the request is silently ignored and this function call returns without error.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID        id of a landmark to delete
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on error
 */
javacall_result javacall_landmarkstore_landmark_delete_from_landmarkstore(
        const javacall_utf16_string landmarkStoreName,
        javacall_handle landmarkID) {
	static LimeFunction *f = NULL;
	int wLandmarkStoreNameLen = 0;
	int ret = 0;

	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	/* void deleteLandmarkFromStore(int recordID, String storeName) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "deleteLandmarkFromStore");
    }

    f->call(f, &ret, landmarkID, landmarkStoreName, wLandmarkStoreNameLen);

    return JAVACALL_OK;
}

/**
 * Deletes a landmark from a category.
 *
 * If the specified landmark does not belong to the specified landmark store or category, then the request is silently ignored and this function call returns without error.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID id of a landmark to delete
 * @param categoryName from which the landmark to be removed
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on error
 */
javacall_result javacall_landmarkstore_landmark_delete_from_category(
        const javacall_utf16_string landmarkStoreName,
        javacall_handle landmarkID,
        const javacall_utf16_string categoryName) {
    /* two things need to be done:
     * 1. delete the landmark from the category in the persistent storage
     * 2. if the landmark is in an already open list, update it there
     */
    landmark_decorator *foundLandmark = NULL;
    landmark_decorator *landmark_temp = NULL;
    javacall_result result;
    landmarkListType *curEl, *foundEl=NULL;
    int i=0, j=0, k=0;
    javacall_utf16_string *tempCategories = NULL;

    /* first get a landmark_decorator object from the given parameters */
    result = find_landmark_decorator(landmarkStoreName, (int)landmarkID, categoryName, &foundLandmark);
    if (JAVACALL_FAIL==result) {
		/* landmark wasn't found in this category, ignore method call */
			return JAVACALL_OK;
	}
    if (JAVACALL_INVALID_ARGUMENT==result) {
            /* category is not associated with the landmark */
            if (foundLandmark != NULL && foundLandmark->created_independently)
                    free_landmark_space(foundLandmark);
            return JAVACALL_OK;
    }

    if (foundLandmark->numCategories > 1) {
        /* remove the category from the landmark_decorator */
        tempCategories = malloc(sizeof(javacall_utf16_string)*(foundLandmark->numCategories-1));
	    if (NULL == tempCategories) {
		    return JAVACALL_FAIL;
	    }
	    memset(tempCategories, 0, sizeof(javacall_utf16_string)*(foundLandmark->numCategories-1));
    }
	while(i<foundLandmark->numCategories) {
			if (!wcscmp(foundLandmark->categories[i], categoryName)){
					/* this is the category to be removed */
					free(foundLandmark->categories[i]);
					i++;
					continue;
			}
            if (k<(foundLandmark->numCategories-1)) {
			    tempCategories[k] = malloc(sizeof(javacall_utf16)*
									       JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME);
			    wcsncpy(tempCategories[k], foundLandmark->categories[i], 
                    JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
                tempCategories[k][JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
            }
			free(foundLandmark->categories[i]);
			i++;
			k++;
	}
    free(foundLandmark->categories);
    foundLandmark->categories = tempCategories;
    foundLandmark->numCategories--;
    tempCategories = NULL;

	landmarkToUpdate = foundLandmark;

    /* update the landmark in the persistent storage */
    result = javacall_landmarkstore_landmark_update(landmarkStoreName,
													landmarkID,
													foundLandmark->instance);
    if (JAVACALL_OK!=result) {
            /* something's wrong */
            if (foundLandmark->created_independently)
                    free_landmark_space(foundLandmark);
            return result;
    }

    /* now update the landmark in an open list if needed */
    if (TRUE==foundLandmark->created_independently) {
            /* landmark was just created, not part of an open list */
            free_landmark_space(foundLandmark);
            return JAVACALL_OK;
    } else {
            curEl = landmarkListHead;

            while (curEl!= NULL) {
				if (landmarkStoreName == curEl->storeName ||
					!wcscmp(curEl->storeName,landmarkStoreName)) {
					if (categoryName == curEl->categoryName ||
						!wcscmp(curEl->categoryName, categoryName)) {
						foundEl = curEl;
						break;
						}
				}
				curEl = curEl->next;
            }

            if (NULL!=foundEl) {
                    landmark_temp = foundEl->landmarks;
                    for ( i=0 ; i<foundEl->numLandmarks ; i++) {
                            if (landmark_temp->landmarkID == (int)landmarkID) {
                                    /* landmark found, check if the category is already here */
                                    for (j=0 ; j<landmark_temp->numCategories ; j++) {
                                            if (!wcscmp(landmark_temp->categories[j],categoryName)) {
                                                    /* category found, need to remove it */
                                                tempCategories = NULL;
                                                if (landmark_temp->numCategories > 1) {
                                                    tempCategories = malloc((landmark_temp->numCategories-1));
                                                    for (k=0 ; k <j; k++) {
                                                        tempCategories[k] = landmark_temp->categories[k];
                                                    }
                                                    free(landmark_temp->categories[j]);
                                                    for (k=j+1 ; k <landmark_temp->numCategories; k++) {
                                                        tempCategories[j++] = landmark_temp->categories[k];
                                                    }
                                                }
                                                free(landmark_temp->categories);
                                                landmark_temp->categories = tempCategories;
                                                landmark_temp->numCategories--;
                                                break;
                                            }
                                    }

                                    if (j==landmark_temp->numCategories) {
                                            /* category wasn't found, everything's OK */
                                            return JAVACALL_OK;
                                    }
                                    break;
                            } else {
                                landmark_temp = landmark_temp->next;
                            }
                    }
            }
    }

    return JAVACALL_OK;
}

/**
 * Gets a handle for Landmark list.
 *
 * @param landmarkStoreName landmark store to get the landmark from
 * @param categoryName of the landmark to get, NULL implies a wildcard that matches all categories.
 * @param pHandle that can be used in javacall_landmarkstore_landmarklist_next
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_INVALID_ARGUMENT  if the category name is invalid
 * @retval JAVACALL_FAIL        on other error
 */
javacall_result javacall_landmarkstore_landmarklist_open(
        const javacall_utf16_string landmarkStoreName,
        const javacall_utf16_string categoryName,
        javacall_handle* /*OUT*/pHandle) {
	boolean getAllCategories = (NULL==categoryName)?TRUE:FALSE;
	static LimeFunction *f = NULL;
	int wLandmarkStoreNameLen = 0;
	byte *serializedLandmarks;
	int serializedSize = 0;
	landmark_decorator *landmark = NULL;
	int tempLandmarkID = -1;
	int position = 0;
	int currentLandmarkSize = 0, amountRead = 0;
	landmarkListType *newList = NULL, *curEl;
	long expectedHandleID = 1;
	int i=0;
	javacall_handle clHandle;
	javacall_utf16_string category = NULL;
	boolean categoryFound = FALSE;
	javacall_result result;
	boolean lmInCategory = FALSE;

	/* first check if the category name is valid */
	if (categoryName != NULL) {
		result = javacall_landmarkstore_categorylist_open(landmarkStoreName, &clHandle);
		if (JAVACALL_FAIL==result)
			return JAVACALL_FAIL;

		result = javacall_landmarkstore_categorylist_next(clHandle, &category);

		while(category!=NULL) {
			if (JAVACALL_FAIL==result)
				return JAVACALL_FAIL;
			if(!wcscmp(category,categoryName)) {
				categoryFound = TRUE;
				break;
			}
			result = javacall_landmarkstore_categorylist_next(clHandle, &category);
		}

		javacall_landmarkstore_categorylist_close(clHandle);

		if (!categoryFound) {
			return JAVACALL_INVALID_ARGUMENT;
		}
	}

	/* Calculate length of landmarkStoreName */
	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	/* public static byte[] getLandmarksFromStore(String storeName) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "getLandmarksFromStore");
    }

    f->call(f, &serializedLandmarks, &serializedSize, landmarkStoreName, wLandmarkStoreNameLen);

	newList = malloc(sizeof(landmarkListType));
	if (NULL==newList)
		return JAVACALL_FAIL;

	newList->numLandmarks = 0;
	newList->landmarks = NULL;
	newList->handle = (javacall_handle)1;
	newList->next = NULL;
    if (landmarkStoreName != NULL) {
        newList->storeName = malloc(SIZE_OF_NAME(wLandmarkStoreNameLen+1));
		if(NULL==newList->storeName){
			free (newList);
			return JAVACALL_FAIL;
		}
        wcscpy(&newList->storeName[0],
				   &landmarkStoreName[0]);
			/* newList->storeName[wLandmarkStoreNameLen] = 0; */
    } else {
        newList->storeName = NULL;
    }

    if (categoryName != NULL) {
        newList->categoryName = malloc(SIZE_OF_NAME(JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME));
		if(NULL==newList->categoryName) {
			if (newList->storeName)
				free(newList->storeName);
			free(newList);
			return JAVACALL_FAIL;
		}
        wcsncpy(newList->categoryName, categoryName, JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1);
        newList->categoryName[JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME-1] = NULL;
    } else {
        newList->categoryName = NULL;
    }

	while (position < serializedSize) {
		/* allocate space for the landmark to be populated. */
		landmark = malloc(sizeof(landmark_decorator));
		if (NULL == landmark) {
			if (newList->storeName)
				free(newList->storeName);
			if (newList->categoryName)
				free(newList->categoryName);
			free(newList);
            return JAVACALL_FAIL;
		}

		/* The stream is |landmark id|landmark size|landmark object|ID|size|object|... */
		tempLandmarkID = read_int(serializedLandmarks, &position);
		currentLandmarkSize = read_int(serializedLandmarks, &position);
		amountRead = bytearray_to_landmark(serializedLandmarks, &position, &landmark);
		if (amountRead != currentLandmarkSize) {
			if (newList->storeName)
				free(newList->storeName);
			if (newList->categoryName)
				free(newList->categoryName);
			for (i=0 ; i<newList->numLandmarks ; i++)
				free_landmark_space(landmark);
			free(newList);
            return JAVACALL_FAIL;
		}
		landmark->landmarkID = tempLandmarkID;

		/* check if the landmark belongs to categoryName */
		if (NULL != categoryName) {
			for (i=0 ; i<landmark->numCategories ; i++) {
				if (landmark->categories[i] && !wcscmp(landmark->categories[i],categoryName)){
					newList->numLandmarks++;
					lmInCategory = TRUE;
					break;
				}
			}
		} else {
			newList->numLandmarks++;
			lmInCategory = TRUE;
		}

		if (!lmInCategory) {
			/* the landmark doesn't belong to categoryName  */
			free_landmark_space(landmark);
			continue;
		}
		else {
			landmark->next = newList->landmarks;
			newList->landmarks = landmark;
		}
		lmInCategory = FALSE;
	}

	/* add newList to landmarkList */
    if (landmarkListHead == NULL) {
        landmarkListHead = newList;
    } else {
        if ((long)landmarkListHead->handle > expectedHandleID) {
            newList->next = landmarkListHead;
            landmarkListHead = newList;
        } else {
            curEl = landmarkListHead;
            expectedHandleID++;
            while (curEl->next != NULL) {
                if ((long)curEl->next->handle > expectedHandleID) {
                    newList->handle = (javacall_handle) expectedHandleID;
                    newList->next = curEl->next;
                    curEl->next = newList;
                    newList = NULL;
                    break;
                }
                expectedHandleID++;
                curEl = curEl->next;
            };
            if (newList != NULL) {
                newList->handle = (javacall_handle) expectedHandleID;
                curEl->next = newList;
            }
        }
    }

	newList->curLandmark = NULL;
	newList->currentID = -1;

    *pHandle = newList->handle;

    return JAVACALL_OK;
}

/**
 * Closes the specified landmark list.
 *
 * This handle will no longer be associated with this landmark list.
 *
 * @param handle that is returned by javacall_landmarkstore_landmarklist_open
 *
 */
void javacall_landmarkstore_landmarklist_close(
                  javacall_handle handle) {
    landmarkListType *delEl = NULL, *curEl;
    landmark_decorator *currentLandmark, *nextLandmark;

    if (landmarkListHead != NULL) {
        if (landmarkListHead->handle == handle) {
            delEl = landmarkListHead;
            landmarkListHead = landmarkListHead->next;
        } else {
            curEl = landmarkListHead;
            while (curEl->next != NULL) {
                if (curEl->next->handle == handle) {
                    delEl = curEl->next;
                    curEl->next = delEl->next;
                    break;
                }
                curEl = curEl->next;
            }
        }
    }
    if (delEl != NULL) {
        free(delEl->storeName);
        if (delEl->categoryName != NULL) {
            free(delEl->categoryName);
        }
		currentLandmark = delEl->landmarks;
		if (NULL!=currentLandmark){
			while (NULL!=currentLandmark){
				nextLandmark = currentLandmark->next;
				free_landmark_space(currentLandmark);
				currentLandmark = nextLandmark;
			}
		}

        free(delEl);
		delEl = NULL;
    }

    (void)handle;
}

/**
 * Returns the next landmark from a landmark store.
 *
 * Assumes that the returned landmark memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pLandmarkID id of returned landmark.
 * @param pLandmark pointer to landmark on sucess, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on other error
 */
javacall_result javacall_landmarkstore_landmarklist_next(
        javacall_handle handle,
        int* /*OUT*/pLandmarkID,
        javacall_landmarkstore_landmark** /*OUT*/pLandmark) {
    landmarkListType *curEl, *foundEl=NULL;
    landmark_decorator *landmark;

    *pLandmark = NULL;
    *pLandmarkID = 0;
    curEl = landmarkListHead;
    while(curEl != NULL) {
        if (curEl->handle == handle) {
            foundEl = curEl;
            break;
        } else curEl = curEl->next;
    }

    if (foundEl != NULL) {
		/* see if there are any landmarks in the list */
		if (foundEl->numLandmarks == 0){
			/* empty landmark list */
			*pLandmark = NULL;
			*pLandmarkID = -1;
			return JAVACALL_OK;
		}

		if (NULL == foundEl->curLandmark) {
			/* beginning of the list */
			foundEl->curLandmark = foundEl->landmarks;
			landmark = foundEl->curLandmark;
		} else landmark = foundEl->curLandmark->next;

		if (NULL==landmark) {
			/* end of the list */
			*pLandmarkID = -1;
			*pLandmark = NULL;
			return JAVACALL_OK;
		} else {
			/* middle of the list */
			foundEl->curLandmark = landmark;
			foundEl->currentID = landmark->landmarkID;
			*pLandmark = landmark->instance;
			*pLandmarkID = landmark->landmarkID;
			return JAVACALL_OK;
		}
    }
    return JAVACALL_FAIL;
}

/**
 * Gets a handle to get Category list in specified landmark store.
 *
 * @param landmarkStoreName landmark store to get the categories from
 * @param pHandle that can be used in javacall_landmarkstore_categorylist_next
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 */
javacall_result javacall_landmarkstore_categorylist_open(
    const javacall_utf16_string landmarkStoreName,
    javacall_handle* /*OUT*/pHandle){
    static LimeFunction *f = NULL;
    long num_categories = 0;
    categoryListType *newList = NULL, *curEl;
    long expectedHandleID = 1;
    javacall_utf16_string temp_category_list = NULL;
    javacall_utf16_string *categories_array = NULL;
    javacall_utf16_string categoryList = NULL, tempCategory = NULL;
    int categoryListLen = 0, landmarkStoreNameLen = 0, tempCategoryLen = 0;
    int i = 0;

    if (NULL != landmarkStoreName)
            landmarkStoreNameLen = wcslen(landmarkStoreName);

    /* Get the categories string from Java                           */
    /* public static String getCategoriesForStore(String storeName) */
    if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "getCategoriesForStore");
    }

    f->call(f, &categoryList, &categoryListLen, landmarkStoreName, landmarkStoreNameLen);

    /* Put NULL at the end of the string. This call sometimes returns strings longer
        than the returned size */
    temp_category_list = malloc(SIZE_OF_NAME(categoryListLen+1));
    if (!temp_category_list)
            return JAVACALL_FAIL;
    wcsncpy(temp_category_list,categoryList, categoryListLen+1);
    temp_category_list[categoryListLen] = NULL;
    num_categories = wcsparse(temp_category_list,';',&categories_array);

    if (num_categories == -1){
		free(temp_category_list);
        return JAVACALL_FAIL;
	}

    /* Allocate and initialize new List information */
    newList = malloc(sizeof(categoryListType));
    if (newList == NULL) {
		free(temp_category_list);
        free(newList);
        return JAVACALL_FAIL;
    }
    newList->handle = (javacall_handle)1;
    newList->numCategories = num_categories;

    newList->categoryNames = malloc(sizeof(javacall_utf16_string)*num_categories);
    if (NULL == newList->categoryNames) {
        free(newList);
        for (i=0 ; i<num_categories ; i++)
            free(categories_array[i]);
        free(temp_category_list);
        return JAVACALL_FAIL;
    }
    for (i=0 ; i<num_categories ; i++) {
        tempCategoryLen = wcslen(categories_array[i]);
        newList->categoryNames[i] = malloc(sizeof(javacall_utf16)*(tempCategoryLen+1));
        if (NULL == newList->categoryNames[i]) {
            free(newList);
            for (i=0 ; i<num_categories ; i++)
                free(categories_array[i]);
            free(temp_category_list);
            return JAVACALL_FAIL;
        }
		memset(newList->categoryNames[i], 0, sizeof(javacall_utf16)*(tempCategoryLen+1));
        wcsncpy(newList->categoryNames[i], categories_array[i], tempCategoryLen);
        newList->categoryNames[i][tempCategoryLen] = NULL;
    }

    newList->curCategory = 0;
    newList->next = NULL;

/*
        if (newList->categoryNames == NULL) {
            free(newList);
            return JAVACALL_FAIL;
        };*/


    /* add newList to categoryList*/
    if (categoryListHead == NULL) {
        categoryListHead = newList;
    } else {
        if ((long)categoryListHead->handle > expectedHandleID) {
            newList->next = categoryListHead;
            categoryListHead = newList;
        } else {
            curEl = categoryListHead;
            expectedHandleID++;
            while (curEl->next != NULL) {
                if ((long)curEl->next->handle > expectedHandleID) {
                    newList->handle = (javacall_handle) expectedHandleID;
                    newList->next = curEl->next;
                    curEl->next = newList;
                    newList = NULL;
                    break;
                }
                expectedHandleID++;
                curEl = curEl->next;
            };
            if (newList != NULL) {
                newList->handle = (javacall_handle) expectedHandleID;
                curEl->next = newList;
            }
        }
    }

    *pHandle = (javacall_handle)newList->handle;
    if (!temp_category_list)
        free(temp_category_list);
    for (i=0 ; i<num_categories ; i++)
        free(categories_array[i]);

    return JAVACALL_OK;
}

/**
 * Closes the specified category list.
 * This handle will no longer be associated with this category list.
 *
 * @param handle that is returned by javacall_landmarkstore_categorylist_open
 *
 */
void javacall_landmarkstore_categorylist_close(
          javacall_handle handle) {
    int i;
    categoryListType *delEl = NULL, *curEl;

    if (categoryListHead != NULL) {
        if (categoryListHead->handle == handle) {
            delEl = categoryListHead;
            categoryListHead = categoryListHead->next;
        } else {
            curEl = categoryListHead;
            while (curEl->next != NULL) {
                if (curEl->next->handle == handle) {
                    delEl = curEl->next;
                    curEl->next = delEl->next;
                    break;
                }
                curEl = curEl->next;
            }
        }
    }
    if (delEl != NULL) {
        for (i=0; i<delEl->numCategories; i++) {
			if (delEl->categoryNames[i])
				free(delEl->categoryNames[i]);
        }
		if (delEl->categoryNames)
			free(delEl->categoryNames);
		if (delEl)
			free(delEl);
    }
}


/**
 * Returns the next category name in specified landmark store.
 *
 * Assumes that the returned landmark memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pCategoryName pointer to UNICODE string for the next category name on success, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 *
 */
javacall_result javacall_landmarkstore_categorylist_next(
        javacall_handle handle,
        javacall_utf16_string* /*OUT*/pCategoryName) {
    categoryListType *curEl, *foundEl=NULL;

    curEl = categoryListHead;
    while(curEl != NULL) {
        if (curEl->handle == handle) {
            foundEl = curEl;
            break;
        } else curEl = curEl->next;
    }

    if (foundEl != NULL) {
        if (foundEl->curCategory < foundEl->numCategories) {
            *pCategoryName = foundEl->categoryNames[foundEl->curCategory++];
        } else {
            *pCategoryName = NULL;
            javacall_landmarkstore_categorylist_close(handle);
        }
    }

    return JAVACALL_OK;
}

/**
 * Creates a landmark store.
 *
 * If the implementation supports several storage media, this function may e.g. prompt the end user to make the choice.
 *
 * @param landmarkStoreName name of a landmark store.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 * @retval JAVACALL_INVALID_ARGUMENT   if the name is too long or if a landmark store with specified name already exists.
 */
javacall_result /*OPTIONAL*/ javacall_landmarkstore_create(
                        const javacall_utf16_string landmarkStoreName) {
	int storeNameLen = wcslen(landmarkStoreName);
	int i;
	javacall_handle listHandle;
	static LimeFunction *f = NULL;
	int ret = 0;

	if (NULL==landmarkStoreName)
		return JAVACALL_INVALID_ARGUMENT;

	/* check if that landmark store already exists */
	if (landmark_store_list_open) {
		for (i=0 ; i<landmark_store_list_size ; i++) {
			if (NULL==landmarkStoreName)
				return JAVACALL_INVALID_ARGUMENT;
			if (NULL==landmark_store_list[i])
				continue;
			if (!wcscmp(landmark_store_list[i],landmarkStoreName))
				return JAVACALL_INVALID_ARGUMENT;
		}
	} else {
		javacall_landmarkstore_list_open(&listHandle);
		for (i=0 ; i<landmark_store_list_size ; i++) {
			if (NULL==landmark_store_list[i])
				continue;
			if (!wcscmp(landmark_store_list[i],landmarkStoreName)) {
				javacall_landmarkstore_list_close(listHandle);
				return JAVACALL_INVALID_ARGUMENT;
			}
		}
		javacall_landmarkstore_list_close(listHandle);
	}

	/* public static void addStoreName(String name) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "addStoreName");
    }

	/* Add new store */
    f->call(f, &ret, landmarkStoreName, storeNameLen);

    return JAVACALL_OK;
}

/**
 * Deletes a landmark store.
 *
 * All the landmarks and categories defined in named landmark store are removed.
 * If a landmark store with the specified name does not exist, this function returns silently without any error.
 *
 * @param landmarkStoreName name of a landmark store.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 * @retval JAVACALL_INVALID_ARGUMENT   if the name is too long
 */
javacall_result /*OPTIONAL*/ javacall_landmarkstore_delete(
                        const javacall_utf16_string landmarkStoreName) {
	int storeNameLen = wcslen(landmarkStoreName);
	static LimeFunction *f = NULL;
	int ret = 0;

	/* public static void removeStoreName(String name) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "removeStoreName");
    }

	/* Remove store */
    f->call(f, &ret, landmarkStoreName, storeNameLen);

    return JAVACALL_OK;
}

/**
 * Adds a category to a landmark store.
 *
 * This function must support category name that have length up to and
 * including 32 chracters.
 *
 * @param landmarkStoreName the name of the landmark store.
 * @param categoryName category name - UNICODE string
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 * @retval JAVACALL_INVALID_ARGUMENT   if a category name already exists
 */
javacall_result /*OPTIONAL*/ javacall_landmarkstore_category_add(
        const javacall_utf16_string landmarkStoreName,
        const javacall_utf16_string categoryName) {
    static LimeFunction *f = NULL;
	int wCategoryNameLen = 0, wLandmarkStoreNameLen = 0;
	javacall_handle categorylistHandle;
	javacall_result retval;
	javacall_utf16_string currentCategory;
	int ret = 0;

    /* Calculate len of categoryName */
	wCategoryNameLen = wcslen(categoryName);

	/* Calculate len of landmarkStoreName */
	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	/* check if category already exists in landmark store */
	retval = javacall_landmarkstore_categorylist_open(landmarkStoreName, &categorylistHandle);
	if (retval != JAVACALL_OK)
		return JAVACALL_FAIL;

	javacall_landmarkstore_categorylist_next(categorylistHandle, &currentCategory);
	while(currentCategory != NULL) {
		if (!wcscmp(categoryName, currentCategory))
			return JAVACALL_INVALID_ARGUMENT;
		javacall_landmarkstore_categorylist_next(categorylistHandle, &currentCategory);
	}
	javacall_landmarkstore_categorylist_close(categorylistHandle);


	/* public static void addCategoryToStore(String category, String storeName) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "addCategoryToStore");
    }

    f->call(f, &ret, categoryName, wCategoryNameLen, landmarkStoreName, wLandmarkStoreNameLen);

    return JAVACALL_OK;
}

/**
 * Removes a category from a landmark store.
 *
 * The category will be removed from all landmarks that are in that category. However, this function will not remove any of the landmarks. Only the associated category information from the landmarks are removed.
 * If a category with the supplied name does not exist in the specified landmark store, this function returns silently without error.
 *
 * @param landmarkStoreName the name of the landmark store.
 * @param categoryName category name - UNICODE string
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        the other error
 */
javacall_result /*OPTIONAL*/ javacall_landmarkstore_category_delete(
        const javacall_utf16_string landmarkStoreName,
        const javacall_utf16_string categoryName) {
	static LimeFunction *f = NULL;
	int wCategoryNameLen = 0, wLandmarkStoreNameLen = 0;
	int ret = 0;

	/* Calculate len of categoryName */
	if (NULL == categoryName)
		return JAVACALL_FAIL;
    else wCategoryNameLen = wcslen(categoryName);

	/* Calculate len of landmarkStoreName */
	if (NULL != landmarkStoreName)
		wLandmarkStoreNameLen = wcslen(landmarkStoreName);

	/* public static void deleteCategoryFromStore(String category, String storeName) */
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "deleteCategoryFromStore");
    }

    f->call(f, &ret, categoryName, wCategoryNameLen, landmarkStoreName, wLandmarkStoreNameLen);

    return JAVACALL_OK;
}

/**
 * Gets a handle for LandmarkStore list.
 *
 * @param pHandle that can be used in javacall_landmarkstore_list_next
 * @param pDefLandmarkStore pointer to UNICODE string for the default LandmarkStore name.
 *      default LandmarkStore name can not be NULL
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on other error
 */
javacall_result javacall_landmarkstore_list_open(
        javacall_handle* /*OUT*/pHandle) {
	static LimeFunction *f = NULL;
	javacall_utf16_string store_list = NULL;
	javacall_utf16_string *store_list_array = NULL;
	javacall_utf16_string temp_store_list = NULL;
	int store_list_length, store_list_array_length;
	int i;

	if (landmark_store_list_open){
		for (i=0 ; i<landmark_store_list_size ; i++) {
			if (landmark_store_list[i])
				free(landmark_store_list[i]);
		}
		free (landmark_store_list);
	}

    /* Get the landmark stores string from Java	*/
	/* public static String listStoreNames()	*/
	if (f == NULL) {
        f = NewLimeFunction("com.sun.kvem.location", "LocationBridge", "listStoreNames");
    }

	f->call(f, &store_list, &store_list_length);

	/* Put NULL at the end of the string. This call sometimes returns strings longer
            than the returned size */
	temp_store_list = malloc(SIZE_OF_NAME(store_list_length+1));
	if (!temp_store_list)
		return JAVACALL_FAIL;
	wcsncpy(temp_store_list,store_list, store_list_length);
	temp_store_list[store_list_length] = NULL;
	store_list_array_length = wcsparse(temp_store_list,';',&store_list_array);

	if (store_list_array_length == -1) {
		if (store_list_array)
			free(store_list_array);
		free(temp_store_list);
		return JAVACALL_FAIL;
	}
	if (store_list_array_length) {
		landmark_store_list = malloc((store_list_array_length)*sizeof(javacall_utf16_string));
		if (NULL==landmark_store_list) {
			free(store_list_array);
			free(temp_store_list);
			return JAVACALL_FAIL;
		}
		for (i=0 ; i<store_list_array_length ; i++) {
			/* copy the names from the parsed string to the new string */
			landmark_store_list[i] = store_list_array[i];
		}
		free (store_list_array);
	}

	landmark_store_list_size = store_list_array_length;
	landmark_store_current_item = 0;
	*pHandle = (javacall_handle)LANDMARK_STORE_HANDLE;
	landmark_store_list_open = TRUE;

	free(temp_store_list);

    return JAVACALL_OK;
}

/**
 * Closes the specified landmarkstore list.
 *
 * This handle will no longer be associated with this landmarkstore list.
 *
 * @param handle that is returned by javacall_landmarkstore_list_open
 *
 */
void javacall_landmarkstore_list_close(
                  javacall_handle handle) {
	int i=0;
	if (landmark_store_list) {
		for (i=0 ; i<landmark_store_list_size ; i++) {
			if (landmark_store_list[i])
				free(landmark_store_list[i]);
		}
		//free(landmark_store_list); //IMPL_NOTE: app. error here even though the address is valid
	}
	landmark_store_list_size = -1;
	landmark_store_current_item = -1;
	landmark_store_list_open = FALSE;
}

/**
 * Returns the next landmark store name
 *
 * Assumes that the returned landmarkstore memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pLandmarkStore pointer to UNICODE string for the next landmark store name on success, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        on other error
 */
javacall_result javacall_landmarkstore_list_next(
        javacall_handle handle,
        javacall_utf16_string* /*OUT*/pLandmarkStore) {
	if ((LANDMARK_STORE_HANDLE!=(int)handle) || (FALSE==landmark_store_list_open))
		return JAVACALL_FALSE;

	if (landmark_store_current_item == landmark_store_list_size){
		*pLandmarkStore = NULL;
		return JAVACALL_OK;
	}

	/* if we got here we're in the middle of the open list */
	*pLandmarkStore = landmark_store_list[landmark_store_current_item];

	landmark_store_current_item++;

	return JAVACALL_OK;
}
