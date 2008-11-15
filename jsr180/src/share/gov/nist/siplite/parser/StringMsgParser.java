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

import java.util.Vector;
import java.io.*;
import javax.microedition.sip.SipException;

import gov.nist.siplite.*;
import gov.nist.siplite.header.*;
import gov.nist.siplite.message.*;
import gov.nist.siplite.address.*;
import gov.nist.core.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Parse SIP message and parts of SIP messages such as URI's etc
 * from memory and return a structure.
 * Intended use: UDP message processing.
 * This class is used when you have an entire SIP message or Header
 * or SIP URL in memory and you want to generate a parsed structure from
 * it. For SIP messages, the payload can be binary or String.
 * If you have a binary payload,
 * use parseMessage(byte[]) else use parseSIPMessage(String)
 * The payload is accessible from the parsed message using the getContent and
 * getContentBytes methods provided by the Message class.
 * Currently only eager parsing of the message is supported (i.e. the
 * entire message is parsed in one feld swoop).
 *
 *
 * @version JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class StringMsgParser {
    /** Flag indicating body read requested. */
    protected boolean readBody;
    /** Unprocessed message part 1 (for error reporting). */
    private String rawMessage;
    /** Unprocessed message part 2 (for error reporting). */
    private String rawMessage1;
    /** Current message. */
    private String currentMessage;
    /** Parsing exeception listener. */
    private ParseExceptionListener parseExceptionListener;
    /** Message headers. */
    private Vector messageHeaders;
    /** Current buffer pointer. */
    private int bufferPointer;
    /** Flag indicating bodyis a text string. */
    private boolean bodyIsString;
    /** Current message contents as an arrayof bytes. */
    private byte[] currentMessageBytes;
    /** Lengthg of current message body. */
    protected int contentLength;
    /** Debugging enabled flag. */
    private boolean debugFlag;
    /** Current line being parsed. */
    private int currentLine;
    /** Current header being processed. */
    private String currentHeader;

    /** Default constructor. */
    public StringMsgParser() {
        super();
        messageHeaders = new Vector(10, 10);
        bufferPointer = 0;
        currentLine = 0;
        readBody = true;
    }

    /**
     * Constructor (given a parse exception handler).
     * @since 1.0
     * @param exhandler is the parse exception listener for the message parser.
     */
    public StringMsgParser(ParseExceptionListener exhandler) {
        this();
        parseExceptionListener = exhandler;
    }

    /**
     * Gets the message body.
     * @return the message body
     */
    protected String getMessageBody() {

        if (this.contentLength == 0) {
            return null;
        } else {
            int endIndex = bufferPointer + this.contentLength;
            String body;
            // guard against bad specifications.
            if (endIndex > currentMessage.length()) {
                endIndex = currentMessage.length();
                body = currentMessage.substring(bufferPointer, endIndex);
                bufferPointer = endIndex;
            } else {
                body = currentMessage.substring(bufferPointer, endIndex);
                bufferPointer = endIndex + 1;
            }
            this.contentLength = 0;
            return body;
        }

    }

    /**
     * Gets the message body as a byte array.
     * @return the mesage body
     */
    protected byte[] getBodyAsBytes() {
        if (this.contentLength == 0) {
            return null;
        } else {
            int endIndex = bufferPointer + this.contentLength;
            // guard against bad specifications.
            if (endIndex > currentMessageBytes.length) {
                endIndex = currentMessageBytes.length;
            }
            byte[] body = new byte[endIndex - bufferPointer];
            System.arraycopy
                    (currentMessageBytes, bufferPointer, body, 0, body.length);
            bufferPointer = endIndex;
            this.contentLength = 0;
            return body;
        }

    }

    /**
     * Returns the contents till the end of the buffer (this is useful when
     * you encounter an error.
     * @return text up to end of message
     */
    protected String readToEnd() {
        String body = currentMessage.substring(bufferPointer);
        bufferPointer += body.length();
        return body;
    }

    /**
     * Returns the bytes to the end of the message.
     * This is invoked when the parser is invoked with an array of bytes
     * rather than with a string.
     * @return the bytes to the end of message
     */
    protected byte[] readBytesToEnd() {
        byte[] body = new byte[currentMessageBytes.length - bufferPointer];
        int endIndex = currentMessageBytes.length;
        for (int i = bufferPointer, k = 0; i < endIndex; i++, k++) {
            body[k] = currentMessageBytes[i];
        }
        bufferPointer = endIndex;
        this.contentLength = 0;
        return body;
    }

    /**
     * Adds a handler for header parsing errors.
     * @param pexhandler is a class
     * that implements the ParseExceptionListener interface.
     */
    public void setParseExceptionListener
            (ParseExceptionListener pexhandler) {
        parseExceptionListener = pexhandler;
    }

    /**
     * Returns true if the body is encoded as a string.
     * If the parseMessage(String) method is invoked then the body
     * is assumed to be a string.
     * @return true if body is a string
     */
    protected boolean isBodyString() {
        return bodyIsString;
    }


    /**
     * Parses a buffer containing a single SIP Message where the body
     * is an array of un-interpreted bytes. This is intended for parsing
     * the message from a memory buffer when the buffer.
     * Incorporates a bug fix for a bug that was noted by Will Sullin of
     * Callcast
     * @param msgBuffer a byte buffer containing the messages to be parsed.
     * This can consist of multiple SIP Messages concatenated together.
     * @return a Message[] structure (request or response)
     * containing the parsed SIP message.
     * @exception ParseException is thrown when an
     * illegal message has been encountered (and
     * the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
    public Message parseSIPMessage(byte[] msgBuffer)
    throws ParseException {

        bufferPointer = 0;
        bodyIsString = false;
        currentMessageBytes = msgBuffer;
        int start;
        // Squeeze out leading CRLF
        // Squeeze out the leading nulls (otherwise the parser will crash)
        for (start = bufferPointer; start < msgBuffer.length; start++) {
            final char chr = (char)msgBuffer[start];
            if (chr != '\r'
             && chr != '\n'
             && chr != '\0') break;
        }


        if (start == msgBuffer.length)
            return null;

        // Find the end of the SIP message.
        int fin;
        for (fin = start; fin < msgBuffer.length -4; fin ++) {
            if ((char) msgBuffer[fin] == '\r'
             && (char) msgBuffer[fin+1] == '\n'
             && (char) msgBuffer[fin+2] == '\r'
             && (char) msgBuffer[fin+3] == '\n') {
                break;
            }
        }
        if (fin < msgBuffer.length) {
            // we do not handle the (theoretically possible) case that the
            // headers end with LFLF but there *is* CRLFCRLF in the body
            fin += 4;
        } else {
            // Could not find CRLFCRLF end of message so look for LFLF
            for (fin = start; fin < msgBuffer.length -2; fin++) {
                if ((char)msgBuffer[fin] == '\n'
                 && (char)msgBuffer[fin+1] == '\n') break;
            }
            if (fin < msgBuffer.length) fin += 2;
            else throw new ParseException("Message not terminated", 0);
        }

        // Encode the body as a UTF-8 string.
        String messageString = null;
        try {
            messageString = new String(msgBuffer, start, fin - start, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new ParseException("Bad message encoding!", 0);
        }
        bufferPointer = fin;
        int length = messageString.length();
        StringBuffer message = new StringBuffer(length);
        // Get rid of CR to make it uniform for the parser.
        for (int k = 0; k < length; k++) {
            final char currChar = messageString.charAt(k);
            if (currChar != '\r') {
                message.append(currChar);
            }
        }
        length = message.length();

        if (Parser.debug) {
            for (int k = 0; k < length; k++) {
                rawMessage1 = rawMessage1 + "[" + message.charAt(k) +"]";
            }
        }

        // The following can be written more efficiently in a single pass
        // but it is somewhat complex.
        StringTokenizer tokenizer = new StringTokenizer
                (message.toString(), '\n');
        StringBuffer cooked_message = new StringBuffer();
        while (tokenizer.hasMoreChars()) {
            String nexttok = tokenizer.nextToken();
            // Ignore blank lines with leading spaces or tabs.
            if (nexttok.trim().equals("")) cooked_message.append("\n");
            else cooked_message.append(nexttok);
        }

        cooked_message = normalizeMessage(cooked_message);
        cooked_message.append("\n\n");

        // Separate the string out into substrings for
        // error reporting.
        currentMessage = cooked_message.toString();
        Message sipmsg = parseMessage(currentMessage);
        if (readBody && sipmsg.getContentLengthHeader() != null
                && sipmsg.getContentLengthHeader().getContentLength() != 0) {
            contentLength = sipmsg.getContentLengthHeader().getContentLength();
            byte body[] = getBodyAsBytes();
            sipmsg.setMessageContent(body);
        }
        // System.out.println("Parsed = [" + sipmsg + "]");
        return sipmsg;

    }

    /**
     * Parses a buffer containing one or more SIP Messages and return
     * an array of
     * Message parsed structures. Note that the current limitation is that
     * this does not handle content encoding properly. The message content is
     * just assumed to be encoded using the same encoding as the sip message
     * itself (i.e. binary encodings such as gzip are not supported).
     * @param sipMessages a String containing the messages to be parsed.
     * This can consist of multiple SIP Messages concatenated together.
     * @return a Message structure (request or response)
     * containing the parsed SIP message.
     * @exception ParseException is thrown when an
     * illegal message has been encountered (and
     * the rest of the buffer is discarded).
     * @see ParseExceptionListener
     */
    public Message parseSIPMessage(String sipMessages)
    throws ParseException {
        // Handle line folding and evil DOS CR-LF sequences
        rawMessage = sipMessages;
        Vector retval = new Vector();
        String pmessage = sipMessages;
        bodyIsString = true;

        this.contentLength = 0;
        if (pmessage.trim().equals(""))
            return null;

        pmessage += "\n\n";
        StringBuffer message = new StringBuffer(pmessage);
        // squeeze out the leading crlf sequences.
        while (message.charAt(0) == '\r' || message.charAt(0) == '\n') {
            bufferPointer ++;
            message.deleteCharAt(0);
        }

        // squeeze out the crlf sequences and make them uniformly CR
        String message1 = message.toString();
        int length;
        length = message1.indexOf("\r\n\r\n");
        if (length > 0) length += 4;
        if (length == -1) {
            length = message1.indexOf("\n\n");
            if (length == -1)
                throw new ParseException("no trailing crlf", 0);
        } else length += 2;


        // Get rid of CR to make it uniform.
        for (int k = 0; k < length; k++) {
            if (message.charAt(k) == '\r') {
                message.deleteCharAt(k);
                length --;
            }
        }


        if (debugFlag) {
            for (int k = 0; k < length; k++) {
                rawMessage1 = rawMessage1 + "[" + message.charAt(k) +"]";
            }
        }

        // The following can be written more efficiently in a single pass
        // but it is somewhat complex.
        StringTokenizer tokenizer = new StringTokenizer
                (message.toString(), '\n');
        StringBuffer cooked_message = new StringBuffer();
        while (tokenizer.hasMoreChars()) {
            String nexttok = tokenizer.nextToken();
            // Ignore blank lines with leading spaces or tabs.
            if (nexttok.trim().equals("")) cooked_message.append("\n");
            else cooked_message.append(nexttok);
        }

        cooked_message = normalizeMessage(cooked_message);
        cooked_message.append("\n\n");


        // Separate the string out into substrings for
        // error reporting.

        currentMessage = cooked_message.toString();

        if (Parser.debug) {
            Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                currentMessage);
        }

        bufferPointer = currentMessage.indexOf("\n\n") + 3;
        Message sipmsg = this.parseMessage(currentMessage);
        if (readBody && sipmsg.getContentLengthHeader() != null &&
                sipmsg.getContentLengthHeader().getContentLength() != 0) {
            this.contentLength =
                    sipmsg.getContentLengthHeader().getContentLength();
            String body = this.getMessageBody();
            sipmsg.setMessageContent(body);
        }
        return sipmsg;


    }

    /**
     * Normalize message string, i.e. remove whitespace
     * @param srcMsg message to be processed
     * @return normalized message
     */
    private StringBuffer normalizeMessage(StringBuffer srcMsg) {
        StringBuffer normalizedMessage = new StringBuffer(srcMsg.length());
        String message1 = srcMsg.toString();
        int length = message1.indexOf("\n\n") + 2;
        int k = 0;
        while (k < length - 1) {
            final char thisChar = srcMsg.charAt(k);
            final char thatChar = srcMsg.charAt(k+1);

            // is it a continuation line?
            if (thisChar == '\n' && (thatChar == '\t' || thatChar == ' ')) {
                normalizedMessage.append(' ');
                k++; // skipping \n
                // now remove whitespace
                char nextChar;
                do {
                    k++; // skipping \t or space
                    if (k == length) {
                        break;
                    }
                    nextChar = srcMsg.charAt(k);
                } while (nextChar == ' ' || nextChar == '\t');
            } else {
                normalizedMessage.append(thisChar);
                k++;
            }
        }
        return normalizedMessage;
    }

    /**
     * This is called repeatedly by parseMessage to parse
     * the contents of a message buffer. This assumes the message
     * already has continuations etc. taken care of.
     * prior to its being called.
     * @param currentMessage current message to process
     * @return parsed message data
     */
    private Message parseMessage(String currentMessage)
            throws ParseException {
        // position line counter at the end of the
        // sip messages.
        //
// System.out.println("parsing <<" + currentMessage+">>");

        Message sipmsg = null;
        StringTokenizer tokenizer = new StringTokenizer(currentMessage, '\n');
        messageHeaders = new Vector(); // A list of headers for error reporting

        while (tokenizer.hasMoreChars()) {
            String nexttok = tokenizer.nextToken();
            if (nexttok.equals("\n")) {
                String nextnexttok = tokenizer.nextToken();
                if (nextnexttok.equals("\n")) {
                    break;
                } else messageHeaders.addElement(nextnexttok);
            } else messageHeaders.addElement(nexttok);
        }

        currentLine = 0;
        currentHeader = (String) messageHeaders.elementAt(currentLine);
        String firstLine = currentHeader;
        // System.out.println("first Line " + firstLine);

        if (!firstLine.startsWith(SIPConstants.SIP_VERSION_STRING)) {
            sipmsg = new Request();
            try {
                RequestLine rl =
                        new RequestLineParser(firstLine+ "\n").parse();
                ((Request) sipmsg).setRequestLine(rl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null)
                    this.parseExceptionListener.handleException
                            (ex, sipmsg, new RequestLine().getClass(),
                            firstLine, currentMessage);
                else throw ex;

            }
        } else {
            sipmsg = new Response();

            try {
                StatusLine sl = new StatusLineParser(firstLine + "\n").parse();
                ((Response) sipmsg).setStatusLine(sl);
            } catch (ParseException ex) {
                if (this.parseExceptionListener != null) {
                    this.parseExceptionListener.handleException
                            (ex, sipmsg,
                            new StatusLine().getClass(),
                            firstLine, currentMessage);
                } else throw ex;

            }
        }

        for (int i = 1; i < messageHeaders.size(); i++) {
            String hdrstring = (String) messageHeaders.elementAt(i);

            if (hdrstring == null || hdrstring.trim().equals("")) {
                continue;
            }

            HeaderParser hdrParser = null;

            try {
                // System.out.println("'" + hdrstring + "'");
                hdrParser = ParserFactory.createParser(hdrstring + "\n");
            } catch (ParseException ex) {
                parseExceptionListener.handleException(
                    ex, sipmsg, null, hdrstring, currentMessage);
                continue;
            }

            Header sipHeader = null;

            try {
                sipHeader = hdrParser.parse();
                sipmsg.attachHeader(sipHeader, false);
            } catch (ParseException ex) {
                // ex.printStackTrace();
                if (parseExceptionListener != null) {
                    String hdrName = Lexer.getHeaderName(hdrstring);
                    Class hdrClass = NameMap.getClassFromName(hdrName);

                    if (hdrClass == null) {
                        hdrClass = ExtensionHeader.clazz;
                    }

                    parseExceptionListener.handleException(
                        ex, sipmsg, hdrClass, hdrstring, currentMessage);
                } else { // use generic parser
                    hdrParser  = new ExtensionParser(hdrstring + "\n");
                    sipHeader = hdrParser.parse();
                    try {
                        sipmsg.attachHeader(sipHeader, false);
                    } catch (SipException exc) {
                        throw new ParseException(sipHeader.toString(), 0);
                    }
                }
            } catch (SipException ex) {
                // Invalid header.
                throw new ParseException(sipHeader.toString(), 0);
            }
        }

        return sipmsg;
    }

    /**
     * Parses an address (nameaddr or address spec) and return and address
     * structure.
     * @param address is a String containing the address to be parsed.
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the address is badly formatted.
     */

    public Address parseAddress(String address)
    throws ParseException {
        AddressParser addressParser = new AddressParser(address);
        return addressParser.address();
    }

    /**
     * Parses a host:port and return a parsed structure.
     * @param hostport is a String containing the host:port to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the address is badly formatted.
     */
    public HostPort parseHostPort(String hostport)
    throws ParseException {
        Lexer lexer = new Lexer("charLexer", hostport);
        return new HostNameParser(lexer).hostPort();

    }

    /**
     * Parse a host name and return a parsed structure.
     * @param host is a String containing the host name to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the hostname is badly formatted.
     */
    public Host parseHost(String host)
    throws ParseException {
        Lexer lexer = new Lexer("charLexer", host);
        HostNameParser hp = new HostNameParser(lexer);
        return new Host(hp.hostName());

    }


    /**
     * Parses a telephone number return a parsed structure.
     * @param telephone_number is a String containing the
     * telephone # to be parsed
     * @return a parsed address structure.
     * @since v1.0
     * @exception ParseException when the address is badly formatted.
     */
    public TelephoneNumber parseTelephoneNumber(String telephone_number)
    throws ParseException {
        return new URLParser(telephone_number).parseTelephoneNumber();

    }


    /**
     * Parses a SIP url from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException if there was an error parsing the message.
     */

    public SipURI parseSIPUrl(String url)
    throws ParseException {
        try {
            URLParser parser = new URLParser(url);
            SipURI uri = (SipURI)parser.parse();

            // whole string has to be consumed
            // otherwise it is wrong URL or not URL only
            if (parser.getLexer().hasMoreChars()) {
                throw new ParseException(url + " Not a URL string",
                                         parser.getLexer().getPtr());
            }
            return  uri;
        } catch (ClassCastException ex) {
            throw new ParseException(url + " Not a SIP URL ", 0);
        }
    }


    /**
     * Parses a uri from a string and return a URI structure for it.
     * @param url a String containing the URI structure to be parsed.
     * @return A parsed URI structure
     * @exception ParseException if there was an error parsing the message.
     */

    public URI parseUrl(String url)
    throws ParseException {
        return new URLParser(url).parse();
    }

    /**
     * Parses an individual SIP message header from a string.
     * @param header String containing the SIP header.
     * @return a Header structure.
     * @exception ParseException if there was an error parsing the message.
     */
    public Header parseHeader(String header)
            throws ParseException {
        // It's not clear why "\n\n" was added to the header.
        // header += "\n\n";

        // Handle line folding.
        String nmessage = StringTokenizer.convertNewLines(header);
        nmessage += "\n"; /* why not Separators.NEWLINE ? */

        // System.out.println(">>> '" + nmessage + "'");

        HeaderParser hp = ParserFactory.createParser(nmessage);
        if (hp == null)
            throw new ParseException("could not create parser", 0);

        return hp.parse();
    }

    /**
     * Parses the SIP Request Line
     * @param requestLine a String containing the request line to be parsed.
     * @return a RequestLine structure that has the parsed RequestLine
     * @exception ParseException if there was an error parsing the requestLine.
     */
    public RequestLine parseRequestLine(String requestLine)
    throws ParseException {
        requestLine += "\n";
        return new RequestLineParser(requestLine).parse();
    }

    /**
     * Parses the SIP Response message status line
     * @param statusLine a String containing the Status line to be parsed.
     * @return StatusLine class corresponding to message
     * @exception ParseException if there was an error parsing
     * @see StatusLine
     */
    public StatusLine parseSIPStatusLine(String statusLine)
    throws ParseException {
        statusLine += "\n";
        return new StatusLineParser(statusLine).parse();
    }

    /**
     * Gets the current header.
     * @return the current header
     */
    public String getCurrentHeader() {
        return currentHeader;
    }


    /**
     * Gets the current line number.
     * @return the current line number
     */
    public int getCurrentLineNumber() {
        return currentLine;
    }
}

