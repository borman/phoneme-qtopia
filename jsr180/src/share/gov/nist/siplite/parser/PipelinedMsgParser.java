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
package gov.nist.siplite.parser;

import gov.nist.siplite.message.*;
import gov.nist.siplite.header.*;
import gov.nist.core.*;
import java.io.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This implements a pipelined message parser suitable for use
 * with a stream - oriented input such as TCP. The client uses
 * this class by instatiating with an input stream from which
 * input is read and fed to a message parser.
 * It keeps reading from the input stream and process messages in a
 * never ending interpreter loop. The message listener interface gets called
 * for processing messages or for processing errors. The payload specified
 * by the content-length header is read directly from the input stream.
 * This can be accessed from the Message using the getContent and
 * getContentBytes methods provided by the Message class.
 *
 * @version JAIN-SIP-1.1
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * It was noticed that the parser was
 * blocking so I threw out some cool pipelining which ran fast but only worked
 * when the phase of the full moon matched its mood. Now things are serialized
 * and life goes slower but more reliably.
 *
 * @see SIPMessageListener
 */
public final class PipelinedMsgParser
        implements Runnable {
    /**
     * The message listener that is registered with this parser.
     * (The message listener has methods that can process correct
     * and erroneous messages.)
     */
    protected SIPMessageListener sipMessageListener;
    /** Handle to the preprocssor thread. */
    private Thread mythread;
    /* Current message body contents. */
    // private byte[] messageBody;
    /* Flag to indicate an error was found. */
    // private boolean errorFlag;
    /** Raw data input to be processed. */
    private InputStream rawInputStream;
    
    /** Default constructor. */
    protected PipelinedMsgParser() {
        super();
        
    }
    
    /**
     * Constructor when we are given a message listener and an input stream
     * (could be a TCP connection or a file)
     * @param sipMessageListener Message listener which has
     * methods that get called
     * back from the parser when a parse is complete
     * @param in Input stream from which to read the input.
     * @param debug Enable/disable tracing or lexical analyser switch.
     */
    public PipelinedMsgParser(
            SIPMessageListener sipMessageListener,
            InputStream in,
            boolean debug) {
        this();
        this.sipMessageListener = sipMessageListener;
        rawInputStream = in;
        mythread = new Thread(this);
        
    }
    
    /**
     * This is the constructor for the pipelined parser.
     * @param mhandler a MessageListener implementation that
     * provides the message handlers to
     * handle correctly and incorrectly parsed messages.
     * @param in An input stream to read messages from.
     */
    public PipelinedMsgParser
            (SIPMessageListener mhandler, InputStream in) {
        this(mhandler, in, false);
    }
    
    /**
     * This is the constructor for the pipelined parser.
     * @param in - An input stream to read messages from.
     */
    public PipelinedMsgParser(InputStream in) {
        this(null, in, false);
    }
    
    /**
     * Start reading and processing input.
     */
    public void processInput() {
        mythread.start();
    }
    
    /**
     * Create a new pipelined parser from an existing one.
     * @return A new pipelined parser that reads from the same input
     * stream.
     */
    protected Object clone() {
        PipelinedMsgParser p = new PipelinedMsgParser();
        
        p.rawInputStream = this.rawInputStream;
        p.sipMessageListener = this.sipMessageListener;
        return p;
    }
    
    /**
     * Add a class that implements a MessageListener interface whose
     * methods get called * on successful parse and error conditons.
     * @param mlistener a MessageListener
     * implementation that can react to correct and incorrect
     * pars.
     */
    public void setMessageListener(SIPMessageListener mlistener) {
        sipMessageListener = mlistener;
    }
    
    /**
     * Reads a line of input (I cannot use buffered reader because we
     * may need to switch encodings mid-stream!
     * @param inputStream source for data to be processed
     * @return the line of text retrieved
     * @exception IOException if an error occurs reading from input
     */
    private String readLine(InputStream inputStream)
    throws IOException {
        StringBuffer retval = new StringBuffer("");
        while (true) {
                char ch;
                int i = inputStream.read();
                if (i == -1) {
                    throw new IOException("End of stream");
                } else {
                    ch = (char) i;
                }
                if (ch != '\r') {
                    retval.append(ch);
                }
                if (ch == '\n') {
                    break;
                }
        }
        return retval.toString();
    }
    
    /**
     * Reads to the next break (CRLFCRLF sequence).
     * @param inputStream source for data to be processed
     * @return the text up to the break character sequence
     * @exception IOException if an error occurs reading from input
     */
    private String readToBreak(InputStream inputStream)
    throws IOException {
        StringBuffer retval = new StringBuffer("");
        boolean flag = false;
        while (true) {
                char ch;
                int i = inputStream.read();
                if (i == -1)
                    break;
                else
                    ch = (char) i;
                if (ch != '\r')
                    retval.append(ch);
                if (ch == '\n') {
                    if (flag)
                        break;
                    else
                        flag = true;
                }
        }
        return retval.toString();
    }
    
    /**
     * This is input reading thread for the pipelined parser.
     * You feed it input through the input stream (see the constructor)
     * and it calls back an event listener interface for message
     * processing or error.
     * It cleans up the input - dealing with things like line continuation
     *
     */
    public void run() {
        
        InputStream inputStream = this.rawInputStream;
        
        // I cannot use buffered reader here because we may need to switch
        // encodings to read the message body.
        try {
            while (true) {
                StringBuffer inputBuffer = new StringBuffer();
                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "Starting parse!");
                }
                
                String line1;
                String line2 = null;
                
                // ignore blank lines.
                while (true) {
                    line1 = readLine(inputStream);

                    if (line1.equals("\n")) {
                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION,
                                LogChannels.LC_JSR180,
                                "Discarding " + line1);
                        }
                        continue;
                    } else {
                        break;
                    }
                }
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "line1 = " + line1);
                }
                
                inputBuffer.append(line1);
                
                while (true) {
                    line2 = readLine(inputStream);
                    inputBuffer.append(line2);
                    if (line2.trim().equals("")) break;
                }

                inputBuffer.append(line2);
                StringMsgParser smp =
                        new StringMsgParser(sipMessageListener);
                smp.readBody = false;
                Message sipMessage = null;

                try {
                    sipMessage =
                            smp.parseSIPMessage(inputBuffer.toString());
                    if (sipMessage == null) continue;
                } catch (ParseException ex) {
                    // Just ignore the parse exception.
                    continue;
                }
                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                        "Completed parsing message");
                }
                
                ContentLengthHeader cl =
                        sipMessage.getContentLengthHeader();
                int contentLength = 0;
                if (cl != null) {
                    contentLength = cl.getContentLength();
                } else {
                    contentLength = 0;
                }
                
                if (contentLength == 0) {
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "content length " + contentLength);
                    }
                    sipMessage.removeContent();
                } else { // deal with the message body.
                    contentLength = cl.getContentLength();
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                            LogChannels.LC_JSR180,
                            "content length " + contentLength);
                    }
                    
                    byte[] message_body =  new byte[contentLength];
                    int nread = 0;
                    
                    while (nread < contentLength) {
                        int readlength =
                                inputStream.read
                                (message_body, nread,
                                contentLength -
                                nread);
                                
                        if (readlength > 0) {
                            nread += readlength;
                            
                            if (Logging.REPORT_LEVEL <=
                                    Logging.INFORMATION) {
                                Logging.report(Logging.INFORMATION,
                                    LogChannels.LC_JSR180,
                                    "read " + nread);
                            }
                        } else {
                            break;
                        }
                    }
                    
                    sipMessage.setMessageContent(message_body);
                }
                
                if (sipMessageListener != null) {
                    sipMessageListener.processMessage(sipMessage);
                }
            }
        } catch (IOException ex) {
            if (sipMessageListener != null) {
                sipMessageListener.handleIOException();
            }
        } finally {
        }
    }
}
