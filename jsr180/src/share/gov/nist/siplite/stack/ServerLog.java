/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
/*
 */

package gov.nist.siplite.stack;
import gov.nist.siplite.message.*;
import gov.nist.siplite.header.*;
import gov.nist.core.*;
import java.io.PrintStream;
import java.util.Enumeration;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Log file wrapper class.
 * Log messages into the message trace file and also write the log into the
 * debug file if needed. This class keeps an XML formatted trace around for
 * later access via RMI. The trace can be viewed with a trace viewer (see
 * tools.traceviewerapp).
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */
public class ServerLog {
    /** Dont trace */
    public static int TRACE_NONE = 0;
    /** Trace messages. */
    public static int TRACE_MESSAGES = 16;
    /** Trace exception processing. */
    public static int TRACE_EXCEPTION = 17;
    /** Debug trace level (all tracing enabled). */
    public static int TRACE_DEBUG = 32;
    /** Print stream for writing out debug messages. */
    protected static PrintStream printWriter = null;
    /** Print stream for writing out tracing messages. */
    protected static PrintStream traceWriter = null;
    /** Auxililary information to log with this trace. */
    protected static String auxInfo;
    /** Desription for mesasge.  */
    protected static String description;
    /** Stack pointer for mesasge.  */
    protected static String stackIpAddress;
    /** Default trace level.  */
    protected static int traceLevel = TRACE_MESSAGES;
    
    /**
     * Checks for valid logging output destination.
     */
    public static void checkLogFile() {
        // Append buffer to the end of the file.
        if (printWriter == null) {
            printWriter = traceWriter;
            if (printWriter == null) printWriter = System.out;
            /*
	    if (auxInfo != null)
                printWriter.println
                        (" < description\n logDescription = \""+description+
                        "\"\n name = \"" + stackIpAddress +
                        "\"\n auxInfo = \"" + auxInfo +
                        "\"/ > \n ");
            else
                printWriter.println(" < description\n logDescription = \""
                        + description
                        + "\"\n name = \""
                        + stackIpAddress
                        + "\" / > \n");
	    */
        }
    }
    
    /**
     * Gets the status header from the requested message.
     * @param message the message being processed
     * @return the extracted status line
     */
    private static String getStatusHeader(Message message) {
        // If this message has a "NISTStatus" extension then we extract
        // it for logging.
        Enumeration statusHeaders = message.getHeaders("NISTExtension");
        String status = null;
        if (statusHeaders.hasMoreElements()) {
            Header statusHdr = (Header) statusHeaders.nextElement();
            status = statusHdr.getHeaderValue();
        }
        return status;
    }
    
    /**
     * Checks to see if logging is enabled at a level (avoids
     * unecessary message formatting.
     * @param logLevel level at which to check.
     * @return true if tracing the requested logging level
     */
    public static boolean needsLogging(int logLevel) {
        return traceLevel >= logLevel;
    }
    
    
    /**
     * Global check for whether to log or not. ToHeader minimize the time
     * return false here.
     * @return true -- if logging is globally enabled and false otherwise.
     */
    
    public static boolean needsLogging() {
        return traceLevel >= 16;
    }
    
    /**
     * Sets the log file name. (need revisit)
     * @param loggerURL is the name of the log file to set.
     */
    public static void setLogFileName(String loggerURL) {
        
    }
    
    /**
     * Logs a message into the log file.
     * @param message message to log into the log file.
     */
    public static void logMessage(String message) {
        // String tname = Thread.currentThread().getName();
        checkLogFile();
        String logInfo = message;
        // printWriter.println(logInfo);
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180, logInfo);
        }
    }
    
    /**
     * Logs a message into the log directory.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender (true if I am the sender).
     * @param callId CallId of the message to log into the log directory.
     * @param firstLine First line of the message to display
     * @param status Status information (generated while processing message).
     * @param tid is the transaction id for the message.
     * @param time the reception time (or date).
     */
    public synchronized static void logMessage(String message,
            String from,
            String to,
            boolean sender,
            String callId,
            String firstLine,
            String status,
            String tid,
            String time) {
        
        MessageLog log = new MessageLog(message, from, to, time,
                sender, firstLine, status, tid, callId);
        logMessage(log.flush());
    }
    
    /**
     * Logs a message into the log directory.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender (true if I am the sender).
     * @param callId CallId of the message to log into the log directory.
     * @param firstLine First line of the message to display
     * @param status Status information (generated while processing message).
     * @param tid is the transaction id for the message.
     * @param time the reception time (or date).
     */
    public synchronized static void logMessage(String message,
            String from,
            String to,
            boolean sender,
            String callId,
            String firstLine,
            String status,
            String tid,
            long time) {
        
        MessageLog log = new MessageLog(message, from, to, time,
                sender, firstLine,
                status, tid, callId);
        logMessage(log.flush());
    }
    
    /**
     * Logs a message into the log directory.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender
     * @param callId CallId of the message to log into the log directory.
     * @param firstLine First line of the message to display
     * @param status Status information (generated while processing message).
     * @param tid is the transaction id for the message.
     */
    public static void logMessage(String message,
            String from, String to,
            boolean sender,
            String callId,
            String firstLine,
            String status,
            String tid) {
        String time = new Long(System.currentTimeMillis()).toString();
        logMessage
                (message, from, to, sender, callId, firstLine, status,
                tid, time);
    }
    
    /**
     * Logs a message into the log directory. Status information is extracted
     * from the NISTExtension Header.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender
     * @param time is the time to associate with the message.
     */
    public static void logMessage(Message message, String from,
            String to, boolean sender, String time) {
        checkLogFile();
        CallIdHeader cid = (CallIdHeader)message.getCallId();
        String callId = null;
        if (cid != null)
            callId = ((CallIdHeader)message.getCallId()).getCallId();
        String firstLine = message.getFirstLine();
        String inputText = message.encode();
        String status = getStatusHeader(message);
        String tid = message.getTransactionId();
        logMessage(inputText, from, to, sender,
                callId, firstLine, status, tid, time);
    }
    
    /**
     * Logs a message into the log directory.
     * Status information is extracted from the NISTExtension Header.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender
     * @param time is the time to associate with the message.
     */
    public static void logMessage(Message message, String from,
            String to, boolean sender, long time) {
        checkLogFile();
        CallIdHeader cid = (CallIdHeader)message.getCallId();
        String callId = null;
        if (cid != null) callId = cid.getCallId();
        String firstLine = message.getFirstLine().trim();
        String inputText = message.encode();
        String status = getStatusHeader(message);
        String tid = message.getTransactionId();
        logMessage(inputText, from, to, sender,
                callId, firstLine, status, tid, time);
    }
    
    /**
     * Logs a message into the log directory. Status information is extracted
     * from SIPExtension header. The time associated with the message is the
     * current time.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param sender is the server the sender
     */
    public static void logMessage(Message message, String from,
            String to, boolean sender) {
        logMessage(message, from, to, sender,
                new Long(System.currentTimeMillis()).toString());
    }
    
    /**
     * Logs a message into the log directory.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param status the status to log. This is appended to any NISTExtension
     * header present in the message.
     * @param sender is the server the sender or receiver (true if sender).
     * @param time is the reception time.
     */
    public static void logMessage(Message message, String from,
            String to, String status,
            boolean sender, String time) {
        checkLogFile();
        CallIdHeader cid = (CallIdHeader) message.getCallId();
        String callId = null;
        if (cid != null) callId = cid.getCallId();
        String firstLine = message.getFirstLine().trim();
        String encoded = message.encode();
        String tid = message.getTransactionId();
        String shdr = getStatusHeader(message);
        if (shdr != null) {
            status = shdr + "/" + status;
        }
        logMessage(encoded, from, to, sender,
                callId, firstLine, status, tid, time);
    }
    
    /**
     * Logs a message into the log directory.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param status the status to log. This is appended to any NISTExtension
     * header present in the message.
     * @param sender is the server the sender or receiver (true if sender).
     * @param time is the reception time.
     */
    public static void logMessage(Message message, String from,
            String to, String status,
            boolean sender, long time) {
        checkLogFile();
        CallIdHeader cid = (CallIdHeader) message.getCallId();
        String callId = null;
        if (cid != null) callId = cid.getCallId();
        String firstLine = message.getFirstLine().trim();
        String encoded = message.encode();
        String tid = message.getTransactionId();
        String shdr = getStatusHeader(message);
        if (shdr != null) {
            status = shdr + "/" + status;
        }
        logMessage(encoded, from, to, sender,
                callId, firstLine, status, tid, time);
    }
    
    /**
     * Logs a message into the log directory. Time stamp associated with the
     * message is the current time.
     * @param message a Message to log
     * @param from from header of the message to log into the log directory
     * @param to to header of the message to log into the log directory
     * @param status the status to log.
     * @param sender is the server the sender or receiver (true if sender).
     */
    public static void logMessage(Message message, String from,
            String to, String status,
            boolean sender) {
        logMessage(message, from, to, status, sender,
                System.currentTimeMillis());
    }
    
    /**
     * Logs an exception stack trace.
     * @param ex Exception to log into the log file
     */
    
    public static void logException(Exception ex) {
        if (traceLevel >= TRACE_EXCEPTION) {
            checkLogFile();
            if (printWriter != null) ex.printStackTrace();
        }
    }
    
    /**
     * Sets the trace level for the stack.
     *
     * @param level -- the trace level to set. The following trace levels are
     * supported:
     * <ul>
     * <li>
     * 0 -- no tracing
     * </li>
     *
     * <li>
     * 16 -- trace messages only
     * </li>
     *
     * <li>
     * 32 Full tracing including debug messages.
     * </li>
     *
     * </ul>
     */
    public static void setTraceLevel(int level) {
        traceLevel = level;
    }
    
    /**
     * Gets the trace level for the stack.
     *
     * @return the trace level
     */
    public static int getTraceLevel() { return traceLevel; }
    
    /**
     * Sets aux information. Auxiliary information may be associated
     * with the log file. This is useful for remote logs.
     * @param auxInfo -- auxiliary information.
     */
    public static void setAuxInfo(String auxInfo) {
        ServerLog.auxInfo = auxInfo;
    }
    
    /**
     * Sets the descriptive String for the log.
     * @param desc is the descriptive string.
     */
    public static void setDescription(String desc) {
        description = desc;
    }
}
