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
package com.sun.midp.jsr82emul;

import com.sun.midp.log.Logging;

/*
 * Keeps debug logging utils.
 * IMPL_NOTE: The class is just for convenient debugging only.
 *       When development the class and usages, remove it 
 *       inlining log() body where it makes sense.
 */
public class Log {
    /* 
     * Just a way to hide com.sun.midp.log.Logging. Comment out putting back
     *
    static class Logging {
        static final int INFORMATION = 0;
        static final int REPORT_LEVEL = INFORMATION;
    } /*/
    
    /*
     * Logs by means of midp Logging utils.
     * @param msg message to log
     */
     public static void log(String msg) {
         if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
             println(msg);
         }
     }
    
    /*
     * Logs by means of System.out.println.
     * Avoid putting code that uses this method into master workspace.
     * @param msg message to log
     */
     public static void println(String msg) {
         System.out.println(msg);
     }
}
