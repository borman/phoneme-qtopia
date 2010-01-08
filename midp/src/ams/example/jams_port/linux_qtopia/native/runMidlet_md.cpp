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
 * Qtopia adaptation trollsid email: trollsid@gmail.com
 */

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <midp_logging.h>
#include <midpAMS.h>
#include <midpMalloc.h>
#include <jvm.h>
#include <findMidlet.h>
#include <midpUtilKni.h>
#include <suitestore_task_manager.h>
#include <commandLineUtil.h>
#include <commandLineUtil_md.h>
#include <heap.h>
#include <ams_params.h>
#include <midp_properties_port.h>
#include <midp_run_vm.h>
#include <Qtopia>
#include <QDir>
#include <qtopiaapplication.h>
#include <japplication.h>
#include <unistd.h>
#include <QValueSpaceObject>

/** Maximum number of command line arguments. */
#define RUNMIDLET_MAX_ARGS 32


/** Usage text for the run MIDlet executable. */
static const char* const runUsageText =
"\n"
"Usage: runMidlet [<VM args>] [-debug] [-loop] [-classpathext <path>]\n"
"           (-ordinal <suite number> | <suite ID>)\n"
"           [<classname of MIDlet to run> [<arg0> [<arg1> [<arg2>]]]]\n"
"         Run a MIDlet of an installed suite. If the classname\n"
"         of the MIDlet is not provided and the suite has multiple MIDlets,\n"
"         the first MIDlet from the suite will be run.\n"
"          -debug: start the VM suspended in debug mode\n"
"          -loop: run the MIDlet in a loop until system shuts down\n"
"          -classpathext <path>: append <path> to classpath passed to VM\n"
"             (can access classes from <path> as if they were romized)\n"
"\n"
"  where <suite number> is the number of a suite as displayed by the\n"
"  listMidlets command, and <suite ID> is the unique ID a suite is \n"
"  referenced by\n\n";

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
 
void checkDir(QString path)
{
    QDir target(path);
    if(!target.exists())
    {
	target.mkdir(path);
    }
} 

int pid()
{
	return getpid();
}

int runJVM()
{
	int status = -1;
	char *appDir;
	char *confDir;
	int repeatMidlet = 0;
	MIDPError mipdError;
        pcsl_string classname = PCSL_STRING_NULL;
        
	JVM_Initialize();

	QString path = Qtopia::packagePath() + "java/appdb";
	appDir = path.toLatin1().data();
	confDir = path.toLatin1().data();
	if(appDir == NULL)
	{
		REPORT_ERROR(LC_AMS, "Failed to recieve midp application directory.");
		return -1;
	}
	midpSetAppDir(appDir);
	if(confDir == NULL)
	{
		REPORT_ERROR(LC_AMS, "Failed to recieve midp configuration directory.");
		return -2;
	}
	midpSetConfigDir(confDir);
	setHeapParameters();
	char *additionalPath = "internal";
        char *clName = "com.sun.midp.appmanager.MVMManager";
        do
        {
            if(pcsl_string_from_chars(clName, &classname) != PCSL_STRING_OK)
            {
                return -3;
            }
            pcsl_string arg = PCSL_STRING_NULL;;
            do{
                    status = midp_run_midlet_with_args_cp(-1, &classname, &arg, &arg, &arg,
                                                                                             0, additionalPath);
            }while(repeatMidlet && status  != MIDP_SHUTDOWN_STATUS);
        }while(0);
        switch (status)
        {
            case MIDP_SHUTDOWN_STATUS:
                break;

            case MIDP_ERROR_STATUS:
                REPORT_ERROR(LC_AMS, "The MIDlet suite could not be run.");
                break;

            case SUITE_NOT_FOUND_STATUS:
                REPORT_ERROR(LC_AMS, "The MIDlet suite was not found.");
                break;
                
            default:
                break;
        }
        if (JVM_GetConfig(JVM_CONFIG_SLAVE_MODE) == KNI_FALSE)
        {
            midpFinalize();
        }
        pcsl_string_free(&classname);
        return status;
}

int main(int argc, char **argv)
{
//    QtopiaApplication app(argc, argv);
//	QValueSpaceObject *so = new QValueSpaceObject("/System/Applications/phoneMe");
//	so->setAttribute("Info/Pid", pid());
//	so->setAttribute("Info/Name", "phoneMe");
//	so->setAttribute("Info/State", QByteArray("Running"));
//	so->setAttribute("Info/LaunchTime",  QDateTime::currentDateTime());
//	so->setAttribute("Tasks/UI", true);//*/
	int stat = runJVM();//argc, argv);start(argc, argv);//

    return stat;
}

