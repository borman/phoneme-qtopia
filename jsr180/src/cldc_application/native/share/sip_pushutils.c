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

#include <string.h>
#include <ctype.h>

#include <pcsl_memory.h>
#include <midp_libc_ext.h>

/**
 * @file
 * @ingroup sippushregistry
 * 
 * SIP native push utility for message header
 * field extraction.
 * <P>
 * These routines are used to precheck that a
 * SIP message is allowed to launch a particular
 * MIDlet based on a prior push connection 
 * registration. The message contains a "From:"
 * header. It is the "<uri>" string with
 * brackets removed, which must match the 
 * SIP push filter.
 */

/**
 * Extracts the "From" header URI for filter comparison
 * check.
 *
 * @param buf cached SIP message
 * @param len length of the cached data
 * @return a malloc'ed copy of the From header URI field
 *         or NULL if the extraction failed. Caller
 *         must free the string when done.
 */
    char *getSipFromHeaderURI(char *buf, int len) {
    /** Current index in the message. */
    int i;
    /** Flag indicating beginning of line. */
    int bol = 0;
    /** Copy of the URI subfield. */
    char *uri = NULL;
    /** Long form of "From" header flag. */
    int longform;

    /* Check for valid arguments. */
    if (buf == NULL || len == 0)
	return NULL;

    /* Scan the buffer one character at a time. */
    for (i=0; i < len ; i++) {
	/*
	 * At the beginning of line check for
	 * the "From:" header prefix.
	 */
	if (bol == 1) {
	    /* Check for end of buffer. */
	    if (i+4 < len) {
		/* Check for the "From:" header. */
		longform = midp_strncasecmp((char*)&buf[i], "From", 4) == 0 ;
		if (longform || midp_strncasecmp((char*)&buf[i], "f", 1) == 0) {
		    /* Length of the URI subfield. */
		    int urilen;
		    /* Index of end of uri subfield. */
		    int j;

		    /* Skip the "From: label. */
		    i += (longform ? 4 : 1);
		    /* Skip whitespace before the colon*/ 
		    while (isspace(buf[i])) {
			i++;
			if (i == len) break;
		    }
		    /* Consume the colon. */
		    if (buf[i++] != ':') break;

		    /* Skip whitespace after the colon*/ 
		    while (isspace(buf[i])) {
			i++;
			if (i == len) break;
		    }
		    /*  Initialize the end of uri subfield marker. */
		    j = i;

		    /* Extract "<uri>" or "uri" from the value */
		    if (buf[i] == '<') {
			/* Advance the start of field past the '<' */
			i++;
			/* Find the end of "<uri>" marker. */
			while (buf[j] != '>') {
			    j++;
			    if (j == len) break;
			}
		    } else {
			/*
			 * Extract up to end of line marker.
			 */
			while (buf[j] != '\n') {
			    j++;
			    if (j == len) break;
			}
		    }
		    /*
		     * Allocate memory for the uri subfield including
		     * the null terminator.
		     */
		    urilen = j - i;
		    uri = pcsl_mem_malloc(urilen + 1);
		    if (uri != NULL) {
			strncpy(uri, (char*)&buf[i], urilen);
			uri[urilen] = '\0';
			/* Trim if a semicolon is included. */
			j = 0;
			while (j <urilen) {
			    if (uri[j] == ';') {
			        uri[j] = '\0';
			        break;
			    }
			    j++;
			}
			/* Return the uri subfield. */
			return uri;
		    }
		}
	    }
	}

	/* Check for beginning of line. */
	if (buf[i] == '\n') {
	    bol = 1;
	} else {
	    bol = 0;
	}
    }
    
    /* Did not find the "From:" header. */
    return NULL;
}


/**
 * Extracts the "Accept-Contact" type field for
 * media feature comparison check.
 *
 * @param buf cached SIP message
 * @param len length of the cached data
 * @return a malloc'ed copy of the Accept-Contact
 *         media type field
 *         or NULL if the extraction failed. Caller
 *         must free the string when done.
 */
    char* getSipAcceptContactType(char *buf, int len) {
    /** Current index in the message. */
    int i;
    /** Flag indicating beginning of line. */
    int bol = 0;
    /** Copy of the media type subfield. */
    char *acceptcontact_type = NULL;
    /** Long form of "Accept-Contact" header flag. */
    int longform;

    /* Check for valid arguments. */
    if (buf == NULL || len == 0)
	return NULL;

    /* Scan the buffer one character at a time. */
    for (i=0; i < len ; i++) {
	/*
	 * At the beginning of line check for
	 * the "Accept-Contact:" header prefix.
	 */
	if (bol == 1) {
	    /* Check for end of buffer. */
	    if (i+14 < len) {
		/* Check for the "Accept-Contact" header. */
		longform = midp_strncasecmp((char*)&buf[i], "Accept-Contact", 14) == 0;
		if (longform || midp_strncasecmp((char*)&buf[i], "a", 1) == 0) {
		    /* Length of the type subfield. */
		    int acceptcontact_type_len;
		    /* Media type subfield pointer. */
		    char *p;
		    /* End of the media type subfield. */
		    char *end = NULL;

		    /* Skip the "Accept-Contact: label. */
		    i += (longform ? 14 : 1);
		    /* Skip whitespace before the colon*/ 
		    while (isspace(buf[i])) {
			i++;
			if (i == len) break;
		    }
		    /* Consume the colon. */
		    if (buf[i++] != ':') break;

		    /* Skip whitespace after the colon*/ 
		    while (isspace(buf[i])) {
			i++;
			if (i == len) break;
		    }

		    /* Check for media type subfield. */
		    for (p = (char*)&buf[i]; *p ; p++) {
		        if(midp_strncasecmp(p, "type=\"application/", 18) == 0 ){
			    /* Extract just the quoted media type. */
		            p += 18;
			    for (end = p; *end; end++) {
			        if (*end == '"') {
				    /* Found end of media type subfield. */ 
				    break;
				}
			    }
			    /* Stop scanning after media type subfield is located. */
			    break;
			}
		    }
		    /* Extract the type field if present. */
		    if (*p != '\0') {
		        /*
			 * Allocate memory for the type subfield including
			 * the null terminator.
			 */
		      acceptcontact_type_len = end - p;
		      acceptcontact_type = pcsl_mem_malloc(acceptcontact_type_len + 1);
		      if (acceptcontact_type != NULL) {
			  strncpy(acceptcontact_type, p, acceptcontact_type_len);
			  acceptcontact_type[acceptcontact_type_len] = '\0';
			  
			  /* Return the acceptcontact_type subfield. */
			  return acceptcontact_type;
		      }
		    }
		}
	    }
	}

	/* Check for beginning of line. */
	if (buf[i] == '\n') {
	    bol = 1;
	} else {
	    bol = 0;
	}
    }
    
    /* Did not find the "Accept-Contact:" header. */
    return NULL;
}

/**
 * Checks SIP push filter pattern against incoming
 * message sender identification.
 *
 * @param filter push registration entry pattern
 * @param sender extracted URI contained in "From" header
 * @return <code>true</code> if pattern matches
 */
int checksipfilter(char *pattern, char *sender) {
    char *p1 = NULL;
    char *p2 = NULL;

    if ((pattern == NULL) || (sender == NULL))
	return 0;

    /* Filter is exactly "*", then all senders are allowed. */
    if (strcmp((char*)pattern, "*") == 0)
	return 1;

    /* 
     * Otherwise walk through the filter string looking for character
     * matches and wildcard matches.
     * The filter pointer is incremented in the main loop and the
     * sender pointer is incremented as characters and wildcards
     * are matched.
     */
    for (p1=(char*)pattern, p2=(char*)sender; *p1 && *p2; p1++) {
	/*
	 * For an asterisk, consume all the characters up to
	 * a matching next character.
	 */
	if (*p1 == '*') {
	    /* Initialize the next two filter characters. */
	    char f1 = *(p1+1);
	    char f2 = '\0';
            if (f1 != '\0') {
		f2 = *(p1+2);
	    }

	    /* Skip multiple wild cards. */
	    if (f1 == '*') {
		continue;
	    }

	    /* 
	     * Consume all the characters up to a match of the next
	     * character from the filter string. Stop consuming
	     * characters, if the sender string is fully consumed.
	     */
	    while (*p2) {
		/* 
		 * When the next character matches, check the second character
		 * from the filter string. If it does not match, continue
		 * consuming characters from the sender string.
		 */
		if(*p2 == f1 || f1 == '?') {
		    if (*(p2+1) == f2 || f2 == '?' || f2 == '*') {
			/* Always consume a sender string character. */
			p2++;
			if (f2 != '?' || *(p2+1) == '\0') {
			    /* Also, consume a filter character. */
			    p1++;
			}
			break;
		    }
		}
		p2++;
	    }
	} else if (*p1 == '?') {
	    p2 ++;
	} else if (*p1 != *p2) {
	    /* If characters do not match, filter failed. */
	    return 0;
	} else {
	    p2 ++;
	}
    } 
    if (*p1 != *p2 && *p1 != '*') {
	/* Sender string was longer than filter string. */
	return 0;
    }

    return 1;
}

#if ENABLE_I3_TEST
/*
 * The following glue code is only included
 * when the unit test program is being built.
 */
#include <string.h>

#include <kni.h>
#include <sni.h>
#include <ROMStructs.h>
#include <commonKNIMacros.h>
#include <midpError.h>
#include <midpMalloc.h>

/**
 * Gets the URI in the "From" header field of the
 * incoming message.
 * <p>
 * Java declaration:
 * <pre>
 *     int getSipFromHeaderURI(byte[],int)
 * </pre>
 *
 * @param message the cached message
 * @param sender the extract URI
 *
 * @return <tt>0</tt> if successful, otherwise <tt>-1</tt>
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_javax_microedition_sip_TestPushUtils_getSipFromHeaderURI() {
    char  *szMessage = NULL;
    int   messageLen;
    int   ret = -1;
    char* fromURI;
    int   fromURILength;
    int   senderLength;

    KNI_StartHandles(2);
    KNI_DeclareHandle(message);
    KNI_DeclareHandle(sender);
    KNI_GetParameterAsObject(2, sender);
    senderLength = (int)KNI_GetParameterAsInt(3);

    /* Get the message string. */
    KNI_GetParameterAsObject(1, message);
    messageLen = KNI_GetArrayLength(message);
    if ((szMessage = midpMalloc(messageLen)) != NULL) {
        KNI_GetRawArrayRegion(message, 0, messageLen, (jbyte*)szMessage);

	/* Perform the extract sender operation. */
	fromURI = getSipFromHeaderURI(szMessage, messageLen);
	if (NULL != fromURI) {
	    fromURILength = strlen(fromURI) + 1;      /* Include trailing '\0' */
	    if (fromURILength < senderLength) {
		memcpy((char*)JavaByteArray(sender),
		       fromURI, fromURILength);
		ret = 0;
	    }
	    midpFree(fromURI);
	}
	midpFree(szMessage);
    } else {
	KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    KNI_EndHandles();  
    
    KNI_ReturnInt(ret);
}

/**
 * Gets the media type in the "Accept-Contact" header field of the
 * incoming message.
 * <p>
 * Java declaration:
 * <pre>
 *     int getSipAcceptContactType(byte[],int)
 * </pre>
 *
 * @param message the cached message
 * @param acceptcontact_type the extracted subfield
 *
 * @return <tt>0</tt> if successful, otherwise <tt>-1</tt>
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_javax_microedition_sip_TestPushUtils_getSipAcceptContactType() {
    char  *szMessage = NULL;
    int   messageLen;
    int   ret = -1;
    char* acceptcontactType;
    int   acceptcontactTypeLength;
    int   ctLength;

    KNI_StartHandles(2);
    KNI_DeclareHandle(message);
    KNI_DeclareHandle(ct);
    KNI_GetParameterAsObject(2, ct);
    ctLength = (int)KNI_GetParameterAsInt(3);

    /* Get the message string. */
    KNI_GetParameterAsObject(1, message);
    messageLen = KNI_GetArrayLength(message);
    if ((szMessage = midpMalloc(messageLen)) != NULL) {
        KNI_GetRawArrayRegion(message, 0, messageLen, (jbyte*)szMessage);

	/* Perform the extract acceptcontactType operation. */
	acceptcontactType = getSipAcceptContactType(szMessage, messageLen);
	if (NULL != acceptcontactType) {
	    acceptcontactTypeLength = strlen(acceptcontactType) + 1;      /* Include trailing '\0' */
	    if (acceptcontactTypeLength < ctLength) {
		memcpy((char*)JavaByteArray(ct),
		       acceptcontactType, acceptcontactTypeLength);
		ret = 0;
	    }
	    midpFree(acceptcontactType);
	}
	midpFree(szMessage);
    } else {
	KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    KNI_EndHandles();  
    
    KNI_ReturnInt(ret);
}

/**
 * Compares the allowed sender pattern to the 
 * extracted From header URI.
 * <p>
 * Java declaration:
 * <pre>
 *     boolean checksipfilter(byte[],byte[])
 * </pre>
 *
 * @param filter the filter pattern to check
 * @param sender the extracted URI to verified
 *
 * @return <tt>1</tt> if successful, otherwise <tt>0</tt>
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_javax_microedition_sip_TestPushUtils_checksipfilter() {
    char *szFilter = NULL;
    int filterLen;
    char *szSender = NULL;
    int senderLen;
    int ret = -1;

    KNI_StartHandles(2);
    KNI_DeclareHandle(filter);
    KNI_DeclareHandle(sender);

    /* Get the filter string. */
    KNI_GetParameterAsObject(1, filter);
    filterLen = KNI_GetArrayLength(filter);
    if ((szFilter = midpMalloc(filterLen + 1)) != NULL) {
        KNI_GetRawArrayRegion(filter, 0, filterLen, (jbyte*)szFilter);
	szFilter[filterLen] = '\0';
        /* Get the sender information string. */
        KNI_GetParameterAsObject(2, sender);
        senderLen = KNI_GetArrayLength(sender);
        if ((szSender = midpMalloc(senderLen)) != NULL) {
            KNI_GetRawArrayRegion(sender, 0, senderLen, (jbyte*)szSender);
            
            /* Perform the check filter comparison operation. */
            ret = checksipfilter(szFilter, szSender);

            midpFree(szSender);
        }
        else {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        midpFree(szFilter);
    }
    else {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    KNI_EndHandles();  

    KNI_ReturnBoolean(ret);
}

#endif
