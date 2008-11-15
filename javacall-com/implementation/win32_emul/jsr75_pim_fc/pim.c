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


#include <string.h>
#include <windows.h>
#include <stdio.h>
#include <memory.h>

#include "javacall_pim.h"
#include "javacall_logging.h"
#include "javacall_file.h"
#include "javacall_dir.h"

#define PIM_DB_PATH_LEN         1024
#define DATA_BUFFER_LENGTH      1024

javacall_result javacall_dir_get_root_path(javacall_utf16*, int *);

typedef struct {
    javacall_pim_type type;
    wchar_t                 *path;
    HANDLE                   handle;
    BOOL                     first_time;
    PWIN32_FIND_DATAW        dir_data;
    struct _pim_opened_item *item_list;
}pim_opened_list;

typedef struct _pim_opened_item {
    wchar_t* name;
    struct _pim_opened_item *next;
    pim_opened_list *list;
}pim_opened_item;

typedef struct {
    javacall_pim_type   type;
    wchar_t*            path;
    int                 num_fields;
    javacall_pim_field* fields;
    wchar_t*            default_list;
}pim_list_dscr;

static wchar_t readMode[]       = {'r', 0};
static wchar_t writeMode[]      = {'w', 0};
static wchar_t appendMode[]     = {'a', 0};
static wchar_t contactExt[]     = {'.', 'v', 'c', 'f', 0};
static wchar_t calendarExt[]    = {'.', 'v', 'c', 's', 0};
static wchar_t searchMask[]     = {'\\', '*', '.', '*', 0};
static wchar_t currentDir[]     = {'.', 0};
static wchar_t topDir[]         = {'.', '.', 0};
static wchar_t fileSeparator[]  = {'\\', 0};
static wchar_t pimDBDir[]       = {'\\', 'P', 'I', 'M', 0};
static wchar_t pimContactDir[]  = {'c', 'o', 'n', 't', 'a', 'c', 't', 's', 0};
static wchar_t pimContactDef[]  = {'C', 'o', 'n', 't', 'a', 'c', 't', 's', 0};
static wchar_t pimEventsDir[]   = {'e', 'v', 'e', 'n', 't', 's', 0};
static wchar_t pimEventsDef[]   = {'E', 'v', 'e', 'n', 't', 's', 0};
static wchar_t pimTodoDir[]     = {'t', 'o', 'd', 'o', 0};
static wchar_t pimTodoDef[]     = {'T', 'o', ' ', 'D', 'o', 0};
static wchar_t categoriesFile[] = {'c', 'a', 't', 'e', 'g', 'o', 'r', 'i', 'e', 's', '.', 't', 'x', 't', 0};
static wchar_t categoryDelim    = {','};

static javacall_pim_field pimContactFields[] = {
    // NAME field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_NAME,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING_ARRAY,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', 0},
        // code
        {'N'},
        // attributes
        0,
        // arrayElements
        {
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_NAME_FAMILY,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '0', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_NAME_GIVEN,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '1', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_NAME_OTHER,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '2', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_NAME_PREFIX,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '3', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_NAME_SUFFIX,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '4', 0}
            },
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // ADDR field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_ADDR,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING_ARRAY,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', 0},
        // code
        {'A','D','R'},
        // attributes
        0x2a8,
        // arrayElements
        {
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_POBOX,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '0', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_EXTRA,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '1', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_STREET,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '2', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_LOCALITY,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '3', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_REGION,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '4', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_POSTALCODE,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '5', 0}
            },
            {
                JAVACALL_PIM_CONTACT_FIELD_ARRAY_ELEMENT_ADDR_COUNTRY,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '6', 0}
            },
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // EMAIL field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_EMAIL,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'E', 'm', 'a', 'i', 'l', 0},
        // code
        {'E','M','A','I','L'},
        // attributes
        0x2a8,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // FORMATTED_NAME field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_FORMATTED_NAME,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'F', 'o', 'r', 'm', 'a', 't', 't', 'e', 'd', 'N', 'a', 'm', 'e', 0},
        // code
        {'F','N'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // FORMATTED_ADDR field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_FORMATTED_ADDR,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'F', 'o', 'r', 'm', 'a', 't', 't', 'e', 'd', 'A', 'd', 'd', 'r', 0},
        // code
        {'L','A','B','E','L'},
        // attributes
        0x2a8,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // NICKNAME field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_NICKNAME,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'i', 'c', 'k', 'n', 'a', 'm', 'e', 0},
        // code
        {'N','I','C','K','N','A','M','E'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // NOTE field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_NOTE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // code
        {'N','O','T','E'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // ORG field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_ORG,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'O', 'r', 'g', 0},
        // code
        {'O','R','G'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // TEL field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_TEL,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'T', 'e', 'l', 0},
        // code
        {'T','E','L'},
        // attributes
        0x3ff,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // TITLE field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_TITLE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'T', 'i', 't', 'l', 'e', 0},
        // code
        {'T','I','T','L','E'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_UID,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // code
        {'U','I','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // BIRTHDAY field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_BIRTHDAY,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'B', 'i', 'r', 't', 'h', 'd', 'a', 'y', 0},
        // code
        {'B','D','A','Y'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_REVISION,
        // maxValues
        1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // code
        {'R','E','V'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // PHOTO field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_PHOTO,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_BINARY,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'h', 'o', 't', 'o', 0},
        // code
        {'P','H','O','T','O',';','E','N','C','O','D','I','N','G','=','B','A','S','E','6','4'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_CLASS,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_INT,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // code
        {'C','L','A','S','S'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // PUBLIC_KEY field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_PUBLIC_KEY,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_BINARY,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'u', 'b', 'l', 'i', 'c', 'K', 'e', 'y', 0},
        // code
        {'K','E','Y',';','E','N','C','O','D','I','N','G','=','B','A','S','E','6','4'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // PUBLIC_KEY_STRING field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_PUBLIC_KEY_STRING,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'u', 'b', 'l', 'i', 'c', 'K', 'e', 'y', 'S', 't', 'r', 'i', 'n', 'g', 0},
        // code
        {'K','E','Y'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // URL field
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_URL,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'U', 'R', 'L', 0},
        // code
        {'U','R','L'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // PHOTO_URL field - not present in release implementation !
    {
        // id
        JAVACALL_PIM_CONTACT_FIELD_PHOTO_URL,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'h', 'o', 't', 'o', 'U', 'R', 'L', 0},
        // code
        {'P','H','O','T','O',';','V','A','L','U','E','=','U','R','L'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    }
};

static javacall_pim_field pimEventFields[] = {
    // LOCATION field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_LOCATION,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'L', 'o', 'c', 'a', 't', 'i', 'o', 'n', 0},
        // code
        {'L','O','C','A','T','I','O','N'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // NOTE field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_NOTE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // code
        {'D','E','S','C','R','I','P','T','I','O','N'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // SUMMARY field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_SUMMARY,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'S', 'u', 'm', 'm', 'a', 'r', 'y', 0},
        // code
        {'S','U','M','M','A','R','Y'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_UID,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // code
        {'U','I','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // END field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_END,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'E', 'n', 'd', 0},
        // code
        {'D','E','N','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_REVISION,
        // maxValues
        1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // code
        {'L','A','S','T','-','M','O','D','I','F','I','E','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // START field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_START,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'S', 't', 'a', 'r', 't', 0},
        // code
        {'D','T','S','T','A','R','T'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // ALARM field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_ALARM,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_INT,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'A', 'l', 'a', 'r', 'm', 0},
        // code
        {'D','A','L','A','R','M'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        JAVACALL_PIM_EVENT_FIELD_CLASS,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_INT,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // code
        {'C','L','A','S','S'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    }
};

static javacall_pim_field pimTodoFields[] = {
    // NOTE field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_NOTE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // code
        {'D','E','S','C','R','I','P','T','I','O','N'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // SUMMARY field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_SUMMARY,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'S', 'u', 'm', 'm', 'a', 'r', 'y', 0},
        // code
        {'S','U','M','M','A','R','Y'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_UID,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_STRING,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // code
        {'U','I','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_CLASS,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_INT,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // code
        {'C','L','A','S','S'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // PRIORITY field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_PRIORITY,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_INT,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'P', 'r', 'i', 'o', 'r', 'i', 't', 'y', 0},
        // code
        {'P','R','I','O','R','I','T','Y'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // COMPLETION_DATE field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_COMPLETION_DATE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'o', 'm', 'p', 'l', 'e', 't', 'i', 'o', 'n', 'D', 'a', 't', 'e', 0},
        // code
        {'C','O','M','P','L','E','T','E','D'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // DUE field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_DUE,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'D', 'u', 'e', 0},
        // code
        {'D','U','E'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_REVISION,
        // maxValues
        1,
        // type
        JAVACALL_PIM_FIELD_TYPE_DATE,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // code
        {'R','E','V'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    },
    // COMPLETED field
    {
        // id
        JAVACALL_PIM_TODO_FIELD_COMPLETED,
        // maxValues
        -1,
        // type
        JAVACALL_PIM_FIELD_TYPE_BOOLEAN,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'o', 'm', 'p', 'l', 'e', 't', 'e', 'd', 0},
        // code
        {'S','T','A','T','U','S'},
        // attributes
        0,
        // arrayElements
        {
            {JAVACALL_PIM_INVALID_ID, {0}}
        }
    }
};

static javacall_pim_field_attribute pimAttributes[] = {
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_ASST,
        {'A', 's', 's', 'i', 's', 't', 'a', 'n', 't',  0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_AUTO,
        {'A', 'u', 't', 'o', 'm', 'o',  'b', 'i', 'l', 'e', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_FAX,
        {'F', 'a', 'c', 's', 'i', 'm', 'i', 'l', 'i', 'e', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_HOME,
        {'H', 'o', 'm', 'e', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_MOBILE,
        {'M', 'o', 'b',  'i', 'l', 'e', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_OTHER,
        {'O', 't', 'h', 'e', 'r', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_PAGER,
        {'P', 'a', 'g', 'e', 'r', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_PREFERRED,
        {'P', 'r','e', 'e', 'f', 'e', 'r', 'r', 'e', 'd', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_SMS,
        {'S', 'M', 'S', 0}
    },
    {
        JAVACALL_PIM_CONTACT_FIELD_ATTR_WORK,
        {'W', 'o', 'r', 'k', 0}
    }
};


static javacall_utf16 pimStringBuffer[PIM_DB_PATH_LEN];
static unsigned char    pimDataBuffer[DATA_BUFFER_LENGTH];

static pim_list_dscr list_data[] = {
    {
        JAVACALL_PIM_TYPE_CONTACT,
        pimContactDir,
        sizeof(pimContactFields) / sizeof(javacall_pim_field),
        pimContactFields,
        pimContactDef
    },
    {
        JAVACALL_PIM_TYPE_EVENT,
        pimEventsDir,
        sizeof(pimEventFields) / sizeof(javacall_pim_field),
        pimEventFields,
        pimEventsDef
    },
    {
        JAVACALL_PIM_TYPE_TODO,
        pimTodoDir,
        sizeof(pimTodoFields) / sizeof(javacall_pim_field),
        pimTodoFields,
        pimTodoDef
    }
};

static wchar_t*
get_default_list(javacall_pim_type type) {
    pim_list_dscr *list, *last;

    for (list = list_data, last = &list_data[sizeof(list_data) / sizeof(pim_list_dscr)];
         list < last;
         list++) {
        if (list->type == type) {
            break;
        }
    }
    if (list == last) {
        return NULL;
    }
    return list->default_list;
}

static pim_list_dscr*
find_list_description(javacall_pim_type type) {
    pim_list_dscr *current, *end;

    for (current = list_data, end = list_data + sizeof(list_data) / sizeof(pim_list_dscr);
         current < end;
         current++) {
         if (current->type == type) {
            return current;
         }
    }
    return NULL;
}

static BOOL
check_dir_and_create(wchar_t *path) {
    HANDLE pimbd_dir_handle;
    WIN32_FIND_DATAW find_data;

    pimbd_dir_handle = FindFirstFileW(pimStringBuffer, &find_data);
    FindClose(pimbd_dir_handle);
    if (pimbd_dir_handle == INVALID_HANDLE_VALUE) {
        return CreateDirectoryW(pimStringBuffer, NULL);
    }
    return TRUE;
}

static wchar_t*
get_list_path(pim_list_dscr *description, wchar_t *name) {
    int maxlen = PIM_DB_PATH_LEN;
    int pimDBDirLen = wcslen(pimDBDir);
    int list_root_len, def_name_len, name_len;

    wchar_t separator[2];
    separator[0] = javacall_get_file_separator();
    separator[1] = 0;

    if( description == NULL ) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "pim", "get_list_path: description == NULL\n");
        return NULL;
    }

    if( description->path == NULL ) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "pim", "get_list_path: description->path == NULL\n");
        return NULL;
    }

    if( description->path == NULL ) {
        javautil_debug_print (JAVACALL_LOG_ERROR, "pim", "get_list_path: description->default_list == NULL\n");
        return NULL;
    }


    list_root_len = wcslen(description->path);
    def_name_len  = wcslen(description->default_list);
    name_len = name == NULL ? 0 : wcslen(name);
    memset(pimStringBuffer, 0, PIM_DB_PATH_LEN*sizeof(javacall_utf16));

    /* Check for root of pim database */
    if (javacall_dir_get_root_path(pimStringBuffer, &maxlen) != JAVACALL_OK) {
        return NULL;
    }

    if (maxlen + pimDBDirLen + list_root_len + 2 +
        (name_len > def_name_len ? name_len : def_name_len) > PIM_DB_PATH_LEN) {
        return NULL;
    }

    wcscat(pimStringBuffer,pimDBDir);
    if (!check_dir_and_create(pimStringBuffer)) {
        return NULL;
    }

    wcscat(pimStringBuffer, separator );
    wcscat(pimStringBuffer, description->path);
    if (!check_dir_and_create(pimStringBuffer)) {
        return NULL;
    }

    wcscat(pimStringBuffer, separator );
    // Save current length, so we can restore to this point
    // if we want to use the non-default name
    name_len = wcslen(pimStringBuffer);

    wcscat(pimStringBuffer, description->default_list);
    if (!check_dir_and_create(pimStringBuffer)) {
        return NULL;
    }

    if (name != NULL) {
        pimStringBuffer[name_len] = 0;
        wcscat(pimStringBuffer, separator );
        wcscat(pimStringBuffer, name);
    }

    return wcsdup(pimStringBuffer);
}

static wchar_t* get_item_path(pim_opened_list *list, wchar_t* item_name) {
    wchar_t  *file_path;

    file_path = malloc((wcslen(list->path) +
                                 wcslen(item_name) + 2) *
                                    sizeof(wchar_t));
    if (file_path) {
        swprintf(file_path, L"%s%s%s", list->path, fileSeparator, item_name);
	  wprintf(L"get_item_path() get item path: %s\n", file_path);
    }
    return file_path;
}

static pim_opened_item* find_item(pim_opened_list *list, wchar_t* item_name) {
    pim_opened_item* item;

    for (item = list->item_list; item; item = item->next) {
        if (item->name == NULL) {
            wprintf(L"item->name NULL\n");
            return NULL;
        }
        if (!wcscmp(item->name, item_name)) {
            break;
        }
    }
    return item;
}

static void add_new_item(pim_opened_list *list, pim_opened_item *item) {
    pim_opened_item **current;

    for (current = &list->item_list;
         *current;
         current = &(*current)->next) {};
    *current = item;
    item->list = list;
}

static void remove_item(pim_opened_list *list, pim_opened_item *item) {
    pim_opened_item **current;

    for (current = &list->item_list;
         *current;
         current = &(*current)->next) {
         if (*current == item) {
            break;
         }
    }
    if (*current) {
        *current = item->next;
    }
    item->next = NULL;
    item->list = NULL;
}

static FILE* open_list_item(pim_opened_list *list, pim_opened_item *item, int read_only) {
    FILE     *item_stream;
    wchar_t  **item_name = &item->name;
    wchar_t  *file_path;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "open_list_item<<");
    if (!*item_name) {
        /* New item, generate file name */
        int index = 0;
        wchar_t *ext = list->type == JAVACALL_PIM_TYPE_CONTACT ? contactExt : calendarExt;

        do {
            index++;
            swprintf(pimStringBuffer, L"%d%s", index, ext);
        } while (find_item(list, pimStringBuffer));
        *item_name = malloc((wcslen(pimStringBuffer) + 1) * sizeof(wchar_t));
        if (!*item_name) {
            return NULL;
        }
        wcscpy(*item_name, pimStringBuffer);
    }

    if (wcscmp(item->name, categoriesFile) == 0) {
        return NULL;
    }
    file_path = get_item_path(list, *item_name);
    if (!file_path) {
        return NULL;
    }

    item_stream = _wfopen(file_path, read_only ? readMode : writeMode);
    free(file_path);

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "open_list_item>>\n");
    return item_stream;
}

static FILE* open_categories_file(pim_opened_list *list, wchar_t *mode) {
    wchar_t *cat_path;
    FILE* cat_stream;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "open_categories_file:\n");
    cat_path = get_item_path(list, categoriesFile);
    if (!cat_path) {
        return NULL;
    }
    cat_stream = _wfopen(cat_path, mode);
    free(cat_path);
    return cat_stream;
}

static wchar_t* get_list_categories(pim_opened_list *list) {
    wchar_t *categories;
    FILE* cat_stream;
    size_t read_count;

    cat_stream = open_categories_file(list, readMode);
    if (!cat_stream) {
        return NULL;
    }
    categories = (wchar_t *)pimDataBuffer;
    read_count = fread(categories, 1, DATA_BUFFER_LENGTH - 1, cat_stream) / sizeof(wchar_t);
    categories[read_count++] = 0;
    fclose(cat_stream);

    return categories;
}

/**
 * Returns JAVACALL_PIM_STRING_DELIMITER separated list that contains the names
 * of PIM lists that match the given list type
 * (e.g., "Contact" Or "JohnContact\nSuziContact").
 *
 * @param listType Type of the PIM list the user wishes to obtain.
 * @param pimList Pointer to the buffer in which to store the returned list.
 *                The list must be delimited by JAVACALL_PIM_STRING_DELIMITER).
 *                The default list name should appear in the first place.
 * @param pimListLen Buffer size.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL when no list exists or when the buffer size is too
 *                       small.
 */
javacall_result javacall_pim_get_lists(javacall_pim_type listType,
                                       javacall_utf16 /*OUT*/ *pimList,
                                       int pimListLen) {
    wchar_t *list_path;
    wchar_t *search_path;
    HANDLE list_dir_handle;
    WIN32_FIND_DATAW find_data;
    int path_len;
    pim_list_dscr *description;
    int def_list_len;
    javacall_result result;
    javacall_utf16 *current;

    description = find_list_description(listType);
    if (description == NULL) {
        return JAVACALL_INVALID_ARGUMENT;
    }
    def_list_len = wcslen(description->default_list);
    list_path = get_list_path(description, L"");
    if (!list_path) {
        return JAVACALL_FAIL;
    }
    path_len = wcslen(list_path);
    search_path = malloc(path_len * sizeof(wchar_t) + sizeof(searchMask));
    if (search_path) {
        memcpy(search_path, list_path, path_len * sizeof(wchar_t));
    }
    free(list_path);
    if (!search_path) {
        return JAVACALL_OUT_OF_MEMORY;
    }
    memcpy(search_path + path_len, searchMask, sizeof(searchMask));
    wcscpy(pimList, description->default_list);
    *(pimList + def_list_len) = JAVACALL_PIM_STRING_DELIMITER;
    current = pimList + def_list_len + 1;
    pimListLen -= def_list_len + 1;

    list_dir_handle = FindFirstFileW(search_path, &find_data);
    if (list_dir_handle != INVALID_HANDLE_VALUE) {
        result = JAVACALL_OK;
        do {
            wchar_t *fname = (wchar_t *)find_data.cFileName;
            if (find_data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY &&
                wcscmp(fname, currentDir) && wcscmp(fname, topDir) &&
                wcscmp(fname, description->default_list)) {
                int cur_name_len = wcslen((wchar_t *)find_data.cFileName) + 1;
                if (pimListLen >= cur_name_len) {
                    wcscpy(current, (wchar_t *)find_data.cFileName);
                    pimListLen -= cur_name_len;
                    current += cur_name_len;
                    *(current - 1) = JAVACALL_PIM_STRING_DELIMITER;
                } else {
                    result = JAVACALL_FAIL;
                    break;
                }
            }
        } while(FindNextFileW(list_dir_handle, &find_data));
        if (current != pimList) {
            *(current - 1) = 0;
        }

        FindClose(list_dir_handle);
    }

    free(search_path);
    return result;
}

/**
 * Checks if a given PIM list type is supported by the platform.
 *
 * @return JAVACALL_TRUE if the list type is supported,
 *         JAVACALL_FALSE otherwise.
 */
javacall_bool javacall_pim_list_is_supported_type(javacall_pim_type listType) {
    switch (listType) {
    case JAVACALL_PIM_TYPE_CONTACT:
    case JAVACALL_PIM_TYPE_EVENT:
    case JAVACALL_PIM_TYPE_TODO:
        return JAVACALL_TRUE;
    }

    return JAVACALL_FALSE;
}

/**
 * Opens the requested PIM list in the given mode.
 *
 * @param listType type of the PIM list to open.
 * @param pimListName the name of the list to open,
 *        if pimList is null the handle of default dummy
 *        list will be returned; this kind of list may
 *        be used only for getting default list structure.
 * @param mode the open mode for the list.
 * @param listHandle pointer to where to store the list handle.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT If an invalid mode is provided as a
 *         parameter or if listType is not a valid PIM list type
 * @retval JAVACALL_FAIL on other error.
 */
javacall_result javacall_pim_list_open(javacall_pim_type listType,
                                       javacall_utf16 *pimList,
                                       javacall_pim_open_mode mode,
                                       javacall_handle *listHandle) {
    wchar_t *listPath;
    wchar_t *searchPath;
    PWIN32_FIND_DATAW javacall_dir_data;
    pim_opened_list* list;
    HANDLE dir_handle;
    int      list_path_len;
    pim_list_dscr *description;

    *listHandle = NULL;
    if (mode != JAVACALL_PIM_OPEN_MODE_READ_ONLY  &&
        mode != JAVACALL_PIM_OPEN_MODE_WRITE_ONLY &&
        mode != JAVACALL_PIM_OPEN_MODE_READ_WRITE) {
        return JAVACALL_INVALID_ARGUMENT;
    }
    description = find_list_description(listType);
    if (description == NULL) {
        return JAVACALL_INVALID_ARGUMENT;
    }
    if (!pimList || wcslen(pimList) == 0) {
        pimList = get_default_list(listType);
    }
    listPath = get_list_path(description, pimList);
    if (!listPath) {
        return JAVACALL_FAIL;
    }
    list_path_len = wcslen(listPath);
    searchPath = malloc(list_path_len * sizeof(wchar_t) + sizeof(searchMask));
    if (!searchPath) {
        free(listPath);
        return JAVACALL_FAIL;
    }
    swprintf(searchPath, L"%s%s", listPath, searchMask);

    javacall_dir_data = (PWIN32_FIND_DATAW)malloc(sizeof(WIN32_FIND_DATAW));
    if (!javacall_dir_data) {
        free(searchPath);
        free(listPath);
        return JAVACALL_FAIL;
    }
    dir_handle = FindFirstFileW(searchPath, javacall_dir_data);
    if (dir_handle == INVALID_HANDLE_VALUE) {
        free(listPath);
        free(searchPath);
        free(javacall_dir_data);
        return JAVACALL_FAIL;
    }
    list = malloc(sizeof(pim_opened_list));
    if (!list) {
        FindClose(dir_handle);
        free(searchPath);
        free(listPath);
        free(javacall_dir_data);
        return JAVACALL_FAIL;
    }
    list->type       = listType;
    list->path       = listPath;
    list->handle     = dir_handle;
    list->first_time = 1;
    list->dir_data   = javacall_dir_data;
    list->item_list  = NULL;

    *listHandle = (javacall_handle)list;
    free(searchPath);
    return JAVACALL_OK;
}

/**
 * Closes an open PIM list.
 *
 * @param listHandle a handle of the list to close.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case the list is no longer accessible.
 */
javacall_result
javacall_pim_list_close(
  javacall_handle listHandle
  ) {
    pim_opened_item *current_item, *next_item;
    pim_opened_list *list = (pim_opened_list *)listHandle;

    if (listHandle == NULL) {
        return JAVACALL_FAIL;
    }

    for (current_item = list->item_list;
         current_item;) {
        next_item = current_item->next;
        free(current_item->name);
        free(current_item);
        current_item = next_item;
    }
    FindClose(list->handle);
    free(list->path);
    free(list->dir_data);
    free(list);
    return JAVACALL_OK;
}


/**
 * Returns the next item in the given PIM list.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle of the list from which to get the item.
 * @param item pointer to a buffer in which to store the item.
 * @param maxItemLen the maximum size of the item.
 * @param categories pointer to a buffer in which to store the item's
 *                   categories separated by JAVACALL_PIM_STRING_DELIMITER.
 * @param maxCategoriesLen size of the categories buffer.
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the returned item.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT  maxItemLen is too small
 * @retval JAVACALL_FAIL in case reached the last item in the list.
 */
javacall_result
javacall_pim_list_get_next_item(
    javacall_handle listHandle,
    unsigned char* item,
    int maxItemLen,
    javacall_utf16* categories,
    int maxCategoriesLen,
    javacall_handle* itemHandle
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    PWIN32_FIND_DATAW dir_data;
    wchar_t *item_name;
    pim_opened_item *opened_item;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_get_next_item:<<\n");
    if (listHandle == NULL) {
        return JAVACALL_FAIL;
    }

    dir_data = list->dir_data;
    if (categories && maxCategoriesLen > 0) {
        *categories = 0;
    }

    do {
NEXTFILE:
        if (list->first_time) {
            list->first_time = 0;
        } else {
            if (!FindNextFileW(list->handle, dir_data)) {
                return JAVACALL_FAIL;
            }

            if (wcscmp(dir_data->cFileName, categoriesFile) == 0) {
                 goto NEXTFILE;
            }
        }
    } while(dir_data->dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);
    item_name = malloc((wcslen(dir_data->cFileName) + 1) * sizeof(wchar_t));
    if (!item_name) {
        return JAVACALL_FAIL;
    }
    opened_item = malloc(sizeof(pim_opened_item));
    if (!opened_item) {
        free(item_name);
        return JAVACALL_FAIL;
    }
    wcscpy(item_name, dir_data->cFileName);
    opened_item->name = item_name;
    opened_item->next = NULL;
    add_new_item(list, opened_item);
    *itemHandle = (javacall_handle)opened_item;


    if (item && maxItemLen > 0) {
        FILE     *item_stream = open_list_item(list, opened_item, 1);
        size_t   read_count;

        if (item_stream == NULL) {
            *item = 0;
            return JAVACALL_FAIL;
        }
        read_count = fread(item, 1, maxItemLen - 1, item_stream);
        item[read_count] = 0;
        fclose(item_stream);
    }

    return JAVACALL_OK;
}

/**
 * Modifies an existing item.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle to the list to which the item belongs.
 * @param itemHandle handle of the item to modify.
 * @param item pointer to the new item data.
 * @param categories pointer to the item's categories separated by comma.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of an error.
 */
javacall_result
javacall_pim_list_modify_item(
  javacall_handle listHandle,
  javacall_handle itemHandle,
  const unsigned char *item,
  const javacall_utf16 *categories
  ) {
    pim_opened_item *modifing_item = (pim_opened_item *)itemHandle;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    FILE* file_handle;
    int data_length;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_modify_item<<");
    if (!modifing_item) {
        return JAVACALL_FAIL;
    }

    file_handle = open_list_item(list, modifing_item, 0);
    if (!file_handle) {
        return JAVACALL_FAIL;
    }

    data_length = strlen(item);
    if (data_length) {
        fwrite(item, 1, data_length, file_handle);
    }

    fclose(file_handle);
	  //      javacall_printf("javacall_pim_list_modify_item>>");
    return JAVACALL_OK;
}

/**
 * Adds a new item to the given item list.
 * For Contact item the item will be in vCard 2.1 / 3.0 format.
 * For Event/Todo item the item will be in vCalendar 1.0 format.
 *
 * @param listHandle handle to the list to which to add the new item.
 * @param item pointer to the item to add to the list.
 * @param categories pointer to the item's categories separated by comma.
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the new item.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of an error.
 */
javacall_result
javacall_pim_list_add_item(
  javacall_handle listHandle,
  const unsigned char *item,
  const javacall_utf16 *categories,
  javacall_handle *itemHandle
  ) {
    pim_opened_item *new_item;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    FILE* file_handle;
    int data_length;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_add_item<<\n");
    new_item = malloc(sizeof(pim_opened_item));
    if (!new_item) {
        return JAVACALL_FAIL;
    }
    memset(new_item, 0, sizeof(pim_opened_item));
    file_handle = open_list_item(list, new_item, 0);
    if (!file_handle) {
        return JAVACALL_FAIL;
    }

    data_length = strlen(item);
    if (data_length) {
        fwrite(item, 1, data_length, file_handle);
    }

    fclose(file_handle);
    *itemHandle = (javacall_handle)new_item;
    add_new_item(list, new_item);
    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_add_item>>\n");
    return JAVACALL_OK;
}

/**
 * Removes an item from the list.
 *
 * @param listHandle handle to the list from which to delete the item.
 * @param itemHandle handle to the item to remove.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_INVALID_ARGUMENT if either of the handles is not valid
 * @retval JAVACALL_FAIL in case of any error.
 */
javacall_result
javacall_pim_list_remove_item(javacall_handle listHandle,
                              javacall_handle itemHandle) {
    pim_opened_item *item = (pim_opened_item *)itemHandle;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    wchar_t *path;
    BOOL result;

    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_remove_item:\n");
    if (!item) {
        return JAVACALL_FAIL;
    }
    path = get_item_path(list, item->name);
    if (!path) {
        return JAVACALL_FAIL;
    }
    remove_item(list, item);
    result = DeleteFileW(path);

    free(path);
    free(item);
    return result ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Adds the given category to the PIM list.
 * If the given category already exists in the list, this function does not add
 * another category, considers the call successful and returns.
 *
 * If the string is not a valid category as defined by the platform,
 * JAVACALL_FAIL is returned when trying to add it.
 *
 * @param listHandle handle of the list to add the new category to.
 * @param categoryName the name of the category to be added.
 * @retval JAVACALL_OK  on success
 * @retval JAVACALL_FAIL   if categories are unsupported, an error occurs,
 *                         or the list is no longer accessible or closed.
 */
javacall_result
javacall_pim_list_add_category(javacall_handle listHandle,
                               javacall_utf16 *categoryName) {
    FILE*  cat_file;
    javautil_debug_print (JAVACALL_LOG_INFORMATION, "pim", "javacall_pim_list_add_category.\n");
    cat_file = open_categories_file((pim_opened_list *)listHandle, appendMode);
    if (!cat_file) {
        return JAVACALL_FAIL;
    }
	           fwrite(&categoryDelim, sizeof(wchar_t), 1, cat_file);
    fwrite(categoryName,  sizeof(wchar_t), wcslen(categoryName), cat_file);
    fclose(cat_file);
    return JAVACALL_OK;
}

/**
 * Removes the indicated category from the PIM list. If the indicated category
 * is not in the PIM list, this call is treated as successfully completed.
 *
 * @param listHandle handle to the list from which to remove the given category
 * @param categoryName the name of the category to be removed.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL if categories are unsupported, an error occurs,
 *                       or the list is no longer accessible or closed.
 */
javacall_result
javacall_pim_list_remove_category(javacall_handle listHandle,
                                  javacall_utf16 *categoryName) {
    wchar_t* categories;
    FILE*    cat_file;
    wchar_t* del_cat;
    wchar_t* delimeter;
    int      del_size;
    wchar_t* search_start;

    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories) {
        return JAVACALL_FAIL;
    }
    del_size = wcslen(categoryName);
    search_start = categories;
    do {
        del_cat = wcsstr(search_start, categoryName);
        search_start = del_cat + 1;
    } while (del_cat && del_cat[del_size] != 0 && del_cat[del_size] != categoryDelim);
    if (!del_cat) {
        return JAVACALL_FAIL;
    }
    delimeter = wcschr(del_cat, categoryDelim);
    if (!delimeter) {
        if (del_cat == categories) {
            cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
            if (!cat_file) {
                return JAVACALL_FAIL;
            }
            fclose(cat_file);
            return JAVACALL_OK;
        }
        del_cat--;
    }

    if (delimeter) {
        delimeter ++;
        memmove(del_cat, delimeter, (wcslen(delimeter) + 1) * sizeof(wchar_t));
    } else {
        *del_cat = 0;
    }
    cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
    if (!cat_file) {
        return JAVACALL_FAIL;
    }
    fwrite(categories,  sizeof(wchar_t), wcslen(categories), cat_file);
    fclose(cat_file);
    return JAVACALL_OK;
}

/**
 * Renames a category.
 * If the new name is not valid for a category on the platform,
 * JAVACALL_FAIL must be returned when trying to rename a category to it.
 *
 * @param listHandle handle to the list to rename the category in.
 * @param oldCategoryName the old category name.
 * @param newCategoryName the new category name.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of any error.
 */
javacall_result
javacall_pim_list_rename_category(
  javacall_handle listHandle,
  javacall_utf16 *oldCategoryName,
  javacall_utf16 *newCategoryName
  ) {
    wchar_t* categories;
    FILE*    cat_file;
    wchar_t* del_cat;
    int      del_size;
    int      new_name_size;
    wchar_t* search_start;
    wchar_t* renamed_tail;

    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories) {
        return JAVACALL_FAIL;
    }
    del_size = wcslen(oldCategoryName);
    search_start = categories;
    do {
        del_cat = wcsstr(search_start, oldCategoryName);
        renamed_tail = del_cat + del_size;
        search_start = del_cat + 1;
    } while (del_cat && *renamed_tail != 0 && *renamed_tail != categoryDelim);
    if (!del_cat) {
        return JAVACALL_FAIL;
    }
    del_size = renamed_tail - del_cat;
    new_name_size = wcslen(newCategoryName);

    if (wcslen(categories) - del_size + new_name_size + 1 > DATA_BUFFER_LENGTH) {
        return JAVACALL_FAIL;
    }

    memmove(renamed_tail - del_size + new_name_size, renamed_tail, (wcslen(renamed_tail) + 1) * sizeof(wchar_t));
    memcpy(del_cat, newCategoryName, new_name_size * sizeof(wchar_t));
    cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
    if (!cat_file) {
        return JAVACALL_FAIL;
    }
    fwrite(categories,  sizeof(wchar_t), wcslen(categories), cat_file);
    fclose(cat_file);
    return JAVACALL_OK;
}

/**
 * Returns the maximum number of categories that this list can have.
 *
 * @param listHandle handle to the list.
 * @retval -1 - indicates there is no limit to the number of categories that
 *              this list can have
 * @retval 0  - indicates no category support
 * @retval >0 - in case a limit exists.
 */
int javacall_pim_list_max_categories(javacall_handle listHandle) {
    return -1;
}

/**
 * Returns the maximum number of categories a list's item can be assigned to.
 *
 * @param listHandle handle to the list.
 * @retval -1 - indicates there is no limit to the number of categories
 *              this item can be assigned to
 * @retval 0  - indicates no category support
 * @retval >0 - in case a limit exists.
 */
int javacall_pim_list_max_categories_per_item(javacall_handle listHandle) {
    return  -1;
}

/**
 * Returns a list of categories defined for the given PIM list in
 * ','-separated format (e.g., "Work,HOME,Friends").
 * If there are no categories defined for the PIM list or categories are
 * unsupported for the list, then JAVACALL_FAIL must be returned.
 *
 * @param listHandle handle to the list to get the categories for.
 * @param categoriesName pointer to a buffer in which to store the
 *                       categories list.
 * @param maxCategoriesLen available size of the categoriesName buffer.
 *
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case no categories found or incase of an error.
 */
javacall_result
javacall_pim_list_get_categories(
  javacall_handle listHandle,
  javacall_utf16 *categoriesName,
  int maxCategoriesLen
  ) {
    wchar_t *categories;

    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories || wcslen(categories) >= (size_t)maxCategoriesLen) {
        return JAVACALL_FAIL;
    }
    wcscpy(categoriesName, categories);

    return JAVACALL_OK;
}

/**
 * Gets all fields that are supported in the given list.
 *
 * In order to identify unused fields, field attributes, field array elements,
 * JAVACALL_PIM_INVALID_ID should be set for each member that is not in use.
 *
 * @param listHandle handle to the list.
 * @param fields pointer to a buffer in which to store the fields.
 * @param maxFields maximum number of fields the field buffer can hold.
 *
 * @retval JAVACALL_OK  on success
 * @retval JAVACALL_FAIL  in case of reaching the last item in the list.
 */
javacall_result
javacall_pim_list_get_fields(
  javacall_handle listHandle,
  javacall_pim_field *fields,
  int maxFields
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    int list_index;
    int i;

    if (listHandle == NULL) {
        return JAVACALL_FAIL;
    }

    for (list_index = 0;
         list_index < sizeof(list_data) / sizeof(pim_list_dscr);
         list_index++) {
         if (list_data[list_index].type == list->type) {
            break;
         }
    }
    if (list_index == sizeof(list_data) / sizeof(pim_list_dscr)) {
        return JAVACALL_FAIL;
    }

    if (maxFields < list_data[list_index].num_fields) {
        return JAVACALL_FAIL;
    }

    for (i = 0; i < list_data[list_index].num_fields; i++) {
        fields[i] = list_data[list_index].fields[i];
    }
    if (i < maxFields) {
        fields[i].id = JAVACALL_PIM_INVALID_ID;
    }
    return JAVACALL_OK;
}

/**
 * Gets all attributes supported by the given list.
 *
 * @param listHandle handle to the list.
 * @param attributes pointer to a buffer in which to store the attributes.
 * @param maxAttributes the maximum number of attributes the buffer can hold.
 * @retval JAVACALL_OK on success
 * @retval JAVACALL_FAIL in case of reaching the last item in the list.
 */
javacall_result
javacall_pim_list_get_attributes(
  javacall_handle listHandle,
  javacall_pim_field_attribute *attributes,
  int maxAttributes
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    int i;

    if (listHandle == NULL) {
        return JAVACALL_FAIL;
    }

    if (maxAttributes < JAVACALL_PIM_MAX_ATTRIBUTES) {
        return JAVACALL_FAIL;
    }

    if (JAVACALL_PIM_TYPE_CONTACT == list->type) {
        for (i = 0; i < JAVACALL_PIM_MAX_ATTRIBUTES; i++) {
            attributes[i] = pimAttributes[i];
        }
        if (i < maxAttributes) {
            attributes[i].id = JAVACALL_PIM_INVALID_ID;
        }
    } else {
        attributes[0].id = JAVACALL_PIM_INVALID_ID;
    }
    return JAVACALL_OK;
}
