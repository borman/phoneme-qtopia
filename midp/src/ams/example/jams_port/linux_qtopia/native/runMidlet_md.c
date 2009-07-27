/*
 *
 *
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

//#include <japplication.h>
/**
 * Runs a MIDlet from an installed MIDlet suite. This is an example of
 * how to use the public MIDP API.
 *
 * @param argc The total number of arguments
 * @param argv An array of 'C' strings containing the arguments
 *
 * @return <tt>0</tt> for success, otherwise <tt>-1</tt>
 *
 * IMPL_NOTE:determine if it is desirable for user targeted output
 *       messages to be sent via the log/trace service, or if
 *       they should remain as printf calls
 */
#include <string.h>

#include <jvmconfig.h>
#include <kni.h>
#include <jvm.h>
#include <jvmspi.h>
#include <sni.h>
#include <pcsl_print.h>
#include <pcsl_memory.h>

#include <midpAMS.h>
#include <midpInit.h>
#include <midpMalloc.h>
#include <midpCommandState.h>
#include <midpStorage.h>
#include <midpServices.h>
#include <midp_properties_port.h>
#include <midpTimeZone.h>
#include <midp_logging.h>
#include <midp_properties_port.h>
#include <midpMIDletProxyList.h>
#include <midpEvents.h>
#include <suitestore_task_manager.h>
#include <midpError.h>
#include <midp_run_vm.h>
#include <midp_check_events.h>
#include <midpMidletSuiteUtils.h>
#if (ENABLE_JSR_205 || ENABLE_JSR_120)
#include <jsr120_types.h>
#include <wmaInterface.h>
#endif

#include <lcdlf_export.h>
#include <midpUtilKni.h>
#include <push_server_export.h>

#if !ENABLE_CDC
#include <suspend_resume.h>
#endif


#include <stdio.h>
#include <stdlib.h>


extern "C"
{
    extern int midpRunVm(JvmPathChar* classPath,
                     char* mainClass,
                     int argc,
                     char** argv);
}
/*
extern void qtopia_app_init(int argc, char** argv); 
*/
#define MIDP_MAIN "com.sun.midp.main.MIDletSuiteLoader"

int main(int argc, char** commandlineArgs)
{
    int i = runMidpVm(2, MIDP_MAIN, 0, 0);
    return i;
}
