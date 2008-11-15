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

/**
 * This class stores a message along with some other informations
 * Used to log messages.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
class MessageLog {
    /** Message to be logged. */
    private String message;
    /** Originator of this mesage. */
    private String source;
    /** Target recipient for the message. */
    private String destination;
    /** Time of the logging event. */
    private long timeStamp;
    /** Flag indicating if we are the message sender. */
    private boolean isSender;
    /** First line from the message. */
    private String firstLine;
    /** Status line from the transaction. */
    private String statusMessage;
    /** Transaction identifier. */
    private String tid;
    /** Caller identification. */
    private String callId;
    
    /**
     * Compares object for equivalence.
     * @param other object to be compared
     * @return true if the object matches
     */
    public boolean equals(Object other) {
        if (! (other instanceof MessageLog)) {
            return false;
        } else {
            MessageLog otherLog = (MessageLog) other;
            return otherLog.message.equals(message) &&
                    otherLog.timeStamp == timeStamp;
        }
    }
    
    /**
     * Constructor with initial parameters.
     * @param message the message to be logged
     * @param source the originator of the message
     * @param destination the target recipient
     * @param timeStamp the logging event timestamp
     * @param isSender true is we are the sender
     * @param firstLine the first line from the message
     * @param statusMessage the status line from the transaction
     * @param tid the transaction identifier
     * @param callId the caller identification
     * @exception IllegalArgumentException if the message is null,
     * or the timeStamp is not valid
     */
    public MessageLog(String message, String source, String destination,
            String timeStamp, boolean isSender,
            String firstLine, String statusMessage,
            String tid, String callId)
            throws IllegalArgumentException {
        if (message == null
                || message.equals(""))
            throw new IllegalArgumentException("null msg");
        this.message = message;
        this.source = source;
        this.destination = destination;
        try {
            long ts = Long.parseLong(timeStamp);
            if (ts < 0)
                throw new IllegalArgumentException("Bad time stamp ");
            this.timeStamp = ts;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Bad number format "
                    + timeStamp);
        }
        this.isSender = isSender;
        this.firstLine = firstLine;
        this.statusMessage = statusMessage;
        this.tid = tid;
        this.callId = callId;
    }
    
    /**
     * Gets the logged timestamp.
     * @return the timestamp
     */
    protected long getTimeStamp() {
        return this.timeStamp;
    }
    
    /**
     * Constructor with initial parameters.
     * @param message the message to be logged
     * @param source the originator of the message
     * @param destination the target recipient
     * @param timeStamp the logging event timestamp
     * @param isSender true is we are the sender
     * @param firstLine the first line from the message
     * @param statusMessage the status line from the transaction
     * @param tid the transaction identifier
     * @param callId the caller identification
     * @exception IllegalArgumentException if the message is null,
     * or the timeStamp is not valid
     */
    public MessageLog(String message, String source, String destination,
            long timeStamp, boolean isSender,
            String firstLine, String statusMessage,
            String tid, String callId)
            throws IllegalArgumentException {
        if (message == null
                || message.equals(""))
            throw new IllegalArgumentException("null msg");
        this.message = message;
        this.source = source;
        this.destination = destination;
        if (timeStamp < 0)
            throw new IllegalArgumentException("negative ts");
        this.timeStamp = timeStamp;
        this.isSender = isSender;
        this.firstLine = firstLine;
        this.statusMessage = statusMessage;
        this.tid = tid;
        this.callId = callId;
    }
    
    /**
     * Constructs an XML formatted log message.
     * @param startTime time of message output
     * @return the encode log message
     */
    public String flush(long startTime) {
        String log;
        
        if (statusMessage != null) {
            log = " <message\nfrom = \"" + source +
                    "\" \nto = \"" + destination +
                    "\" \ntime = \"" + (timeStamp - startTime) +
                    "\" \nisSender = \"" + isSender +
                    "\" \nstatusMessage = \"" + statusMessage +
                    "\" \ntransactionId = \"" + tid +
                    "\" \ncallId = \"" + callId +
                    "\" \nfirstLine = \"" + firstLine.trim() +
                    "\" > \n";
            log += "<![CDATA[";
            log += message;
            log += "]]>\n";
            log += "</message>\n";
        } else {
            log = " <message\nfrom = \"" + source +
                    "\" \nto = \"" + destination +
                    "\" \ntime = \"" + (timeStamp - startTime) +
                    "\" \nisSender = \"" + isSender +
                    "\" \ntransactionId = \"" + tid +
                    "\" \ncallId = \"" + callId +
                    "\" \nfirstLine = \"" + firstLine.trim() +
                    "\" > \n";
            log += "<![CDATA[";
            log += message;
            log += "]]>\n";
            log += "</message>\n";
        }
        return log;
    }
    
    /**
     * Constructs an XML formatted log message.
     * @return the encode log message
     */
    public String flush() {
        String log;
        
        if (statusMessage != null) {
            log = " < message\nfrom = \"" + source +
                    "\" \nto = \"" + destination +
                    "\" \ntime = \"" + timeStamp  +
                    "\" \nisSender = \"" + isSender  +
                    "\" \nstatusMessage = \"" + statusMessage +
                    "\" \ntransactionId = \"" + tid +
                    "\" \nfirstLine = \"" + firstLine.trim() +
                    "\" \ncallId = \"" + callId +
                    "\" \n > \n";
            log += "<![CDATA[";
            log += message;
            log += "]]>\n";
            log += "</message>\n";
        } else {
            log = " < message\nfrom = \"" + source +
                    "\" \nto = \"" + destination +
                    "\" \ntime = \"" + timeStamp  +
                    "\" \nisSender = \"" + isSender  +
                    "\" \ntransactionId = \"" + tid +
                    "\" \ncallId = \"" + callId +
                    "\" \nfirstLine = \"" + firstLine.trim() +
                    "\" \n > \n";
            log += "<![CDATA[";
            log += message;
            log += "]]>\n";
            log += "</message>\n";
        }
        return log;
    }
    
}
