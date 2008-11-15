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
package gov.nist.siplite.message;

import gov.nist.siplite.header.*;
import gov.nist.siplite.parser.*;
import gov.nist.siplite.*;
import java.util.*;
import gov.nist.core.*;
import java.io.UnsupportedEncodingException;
import javax.microedition.sip.SipException;
import javax.microedition.sip.SipErrorListener;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * This is the main SIP Message structure.
 *
 * @see StringMsgParser
 * @see PipelinedMsgParser
 *
 *
 * @version JAIN-SIP-1.1
 *
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * IMPL_NOTE: remove 'Vector headers' because its contents is duplicated in nameTable
 */
public abstract class Message extends GenericObject {
    /** Class handle. */
    private static Class sipHeaderListClass;
    /** Default encoding string. */
    protected static final String DEFAULT_ENCODING = "UTF-8";
    /** Unparsed headers. */
    protected Vector unrecognizedHeaders;
    /** List of parsed headers (in the order they were added).  */
    protected Vector headers;
    /** From header. */
    protected FromHeader fromHeader;
    /** To header. */
    protected ToHeader toHeader;
    /** C sequence header. */
    protected CSeqHeader cSeqHeader;
    /** Caller identification header. */
    protected CallIdHeader callIdHeader;
    /** Content length header. */
    protected ContentLengthHeader contentLengthHeader;
    // protected MaxForwards maxForwardsHeader;
    /** Body of message content. */
    protected String messageContent;
    /** Length of message content in bytes. */
    protected byte[] messageContentBytes;
    /** Object holding body contents. */
    protected Object messageContentObject;
    /** Listener to notify about failures of an asynchoronous send operation */
    private SipErrorListener sipErrorListener;


    static {
        try {
            sipHeaderListClass = Class.forName
                    ("gov.nist.siplite.header.HeaderList");
        } catch (ClassNotFoundException ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    /** Table of headers indexed by name. */
    private Hashtable nameTable;
    
    /**
     * Sets SipErrorListener for the message.
     * Error is notified to SipErrorListener if message was not sent during 
     * asynchronous operation.
     */
    public void setErrorListener(SipErrorListener sel) {
        this.sipErrorListener = sel;
    }
    
    /**
     * Gets SipErrorListener for the message.
     * Error is notified to SipErrorListener if message was not sent during 
     * asynchronous operation.
     */
    public SipErrorListener getErrorListener() {
        return this.sipErrorListener;
    }

    /**
     * Removes the specified header from "headers" list.
     *
     * @param headerName expanded name of the header to remove.
     */
    private void removeHeaderFromList(String headerName) {
        Enumeration li = headers.elements();
        int index = -1;

        while (li.hasMoreElements()) {
            Header sipHeader = (Header) li.nextElement();
            index ++;

            String currName = NameMap.expandHeaderName(
                sipHeader.getName());

            if (Utils.equalsIgnoreCase(currName, headerName)) {
                break;
            }
        }

        if (index != -1 && index < headers.size()) {
            headers.removeElementAt(index);
        }
    }

    /**
     * Returns true if the header belongs only in a Request.
     *
     * @param sipHeader is the header to test.
     * @return true if header is part of a request
     */
    public static boolean isRequestHeader(Header sipHeader) {
        return sipHeader.getHeaderName().equals(Header.ALERT_INFO) ||
                sipHeader.getHeaderName().equals(Header.IN_REPLY_TO) ||
                sipHeader.getHeaderName().equals(Header.AUTHORIZATION) ||
                sipHeader.getHeaderName().equals(Header.MAX_FORWARDS) ||
                sipHeader.getHeaderName().equals(Header.PRIORITY) ||
                sipHeader.getHeaderName().equals(Header.PROXY_AUTHORIZATION) ||
                sipHeader.getHeaderName().equals(Header.PROXY_REQUIRE) ||
                sipHeader.getHeaderName().equals(Header.ROUTE) ||
                sipHeader.getHeaderName().equals(Header.SUBJECT) ||
                sipHeader.getHeaderName().equals(Header.ACCEPT_CONTACT);

    }

    /**
     * Returns true if the header belongs only in a response.
     *
     * @param sipHeader is the header to test.
     * @return true if header is part of a response
     */
    public static boolean isResponseHeader(Header sipHeader) {
        return sipHeader.getHeaderName().equals(Header.ERROR_INFO) ||
                sipHeader.getHeaderName().equals(Header.PROXY_AUTHENTICATE) ||
                sipHeader.getHeaderName().equals(Header.SERVER) ||
                sipHeader.getHeaderName().equals(Header.UNSUPPORTED) ||
                sipHeader.getHeaderName().equals(Header.RETRY_AFTER) ||
                sipHeader.getHeaderName().equals(Header.WARNING) ||
                sipHeader.getHeaderName().equals(Header.WWW_AUTHENTICATE);

    }

    /**
     * Gets a dialog identifier.
     * Generates a string that can be used as a dialog identifier.
     *
     * @param isServer is set to true if this is the UAS
     * and set to false if this is the UAC
     * @return the dialig identifier
     */
    public String getDialogId(boolean isServer) {
        CallIdHeader cid = (CallIdHeader) this.getCallId();
        StringBuffer retval = new StringBuffer(cid.getCallId());
        FromHeader from = (FromHeader) this.getFromHeader();
        ToHeader to = (ToHeader) this.getTo();
        if (! isServer) {
            if (to.getTag() != null) {
                retval.append(to.getTag());
            }
            if (from.getTag() != null) {
                retval.append(from.getTag());
            }
        } else {
            if (from.getTag() != null) {
                retval.append(from.getTag());
            }
            if (to.getTag() != null) {
                retval.append(to.getTag());
            }
        }
        return retval.toString().toLowerCase();

    }

    /**
     * Gets a dialog id given the remote tag.
     * @param isServer flag indicating a server request
     * @param toTag the target recipient
     * @return the dialog identifier
     */
    public String getDialogId(boolean isServer, String toTag) {
        FromHeader from = (FromHeader) this.getFromHeader();
        ToHeader to = (ToHeader) this.getTo();
        CallIdHeader cid = (CallIdHeader) this.getCallId();
        StringBuffer retval = new StringBuffer(cid.getCallId());
        if (! isServer) {
            if (toTag != null) {
                retval.append(toTag);
            }
            if (from.getTag() != null) {
                retval.append(from.getTag());
            }
        } else {
            if (from.getTag() != null) {
                retval.append(from.getTag());
            }
            if (toTag != null) {
                retval.append(toTag);
            }
        }
        return retval.toString().toLowerCase();
    }

    /**
     * Encodes this message as a string. This is more efficient when
     * the payload is a string (rather than a binary array of bytes).
     * If the payload cannot be encoded as a UTF-8 string then it is
     * simply ignored (will not appear in the encoded message).
     * @return The Canonical String representation of the message
     * (including the canonical string representation of
     * the SDP payload if it exists).
     */
    public String encode() {
        StringBuffer encoding = new StringBuffer();
        // Synchronization added because of concurrent modification exception
        // noticed by Lamine Brahimi.
        synchronized (this.headers) {
            Enumeration it = this.headers.elements();

            while (it.hasMoreElements()) {
                Header siphdr = (Header) it.nextElement();
                if (! (siphdr instanceof ContentLengthHeader)) {
                    encoding.append(siphdr.encode());
                }
            }
        }

        // Add the content-length header
        if (contentLengthHeader != null) {
            encoding.append(contentLengthHeader.encode()).append
                    (Separators.NEWLINE);
        }

        if (this.messageContentObject != null) {
            String mbody = this.getContent().toString();
            encoding.append(mbody);
        } else if (this.messageContent != null ||
                this.messageContentBytes != null) {
            String content = null;
            try {
                if (messageContent != null) {
                    content = messageContent;
                } else {
                    content = new String(messageContentBytes,
                                         DEFAULT_ENCODING);
                }
            } catch (UnsupportedEncodingException ex) {
                content = "";
            }
            encoding.append(content);
        }
        
        return encoding.toString();
    }

    /**
     * Encodes the message as a byte array.
     * Use this when the message payload is a binary byte array.
     *
     * @return The Canonical byte array representation of the message
     * (including the canonical byte array representation of
     * the SDP payload if it exists all in one contiguous byte array).
     *
     */
    public byte[] encodeAsBytes() {
        StringBuffer encoding = new StringBuffer();
        Enumeration it = this.headers.elements();

        while (it.hasMoreElements()) {
            Header siphdr = (Header) it.nextElement();
            if (! (siphdr instanceof ContentLengthHeader))
                encoding.append(siphdr.encode());

        }
        byte[] retval = null;
        byte[] content = this.getRawContent();
        if (content != null) {
            encoding.append(Header.CONTENT_LENGTH +
                    Separators.COLON +
                    Separators.SP + content.length
                    + Separators.NEWLINE);
            encoding.append(Separators.NEWLINE);
            // Append the content
            byte[] msgarray = null;
            try {
                msgarray = encoding.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }

            retval = new byte[msgarray.length + content.length];
            System.arraycopy(msgarray, 0, retval, 0, msgarray.length);
            System.arraycopy(content, 0, retval, msgarray.
                    length, content.length);
        } else {
            // Message content does not exist.
            encoding.append(Header.CONTENT_LENGTH +
                    Separators.COLON + Separators.SP + '0'
                    + Separators.NEWLINE);
            encoding.append(Separators.NEWLINE);
            try {
                retval = encoding.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        return retval;
    }

    /**
     * Clones this message (create a new deep physical copy).
     * All headers in the message are cloned.
     * You can modify the cloned copy without affecting
     * the original.
     *
     * @return A cloned copy of this object.
     */
    public Object clone() {
        Message retval = null;
        try {
            retval = (Message) this.getClass().newInstance();
        } catch (IllegalAccessException ex) {
            InternalErrorHandler.handleException(ex);
        } catch (InstantiationException ex) {
            InternalErrorHandler.handleException(ex);
        }

        Enumeration li = headers.elements();
        while (li.hasMoreElements()) {
            Header sipHeader = (Header) ((Header) li.nextElement()).clone();
            try {
                retval.attachHeader(sipHeader);
            } catch (SipException ex) {
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "Message.clone(): can't attach header '" +
                            sipHeader.getName() + "'.");
                    ex.printStackTrace();
                }
            }
        }

        if (retval instanceof Request) {
            Request thisRequest = (Request) this;
            RequestLine rl = (RequestLine)
            (thisRequest.getRequestLine()).clone();
            ((Request) retval).setRequestLine(rl);
        } else {
            Response thisResponse = (Response) this;
            StatusLine sl = (StatusLine)
            (thisResponse.getStatusLine()).clone();
            ((Response) retval).setStatusLine(sl);
        }

        if (getContent() != null) {
            try {
                retval.setContent(getContent(), getContentTypeHeader());
            } catch (SipException ex) {  // Ignore
                if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                    Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                        "Message.clone(): can't set the content!");
                    ex.printStackTrace();
                }
            }
        }

        return retval;
    }

    /**
     *
     * Constructor: Initializes lists and list headers.
     * All the headers for which there can be multiple occurances in
     * a message are derived from the HeaderListClass. All singleton
     * headers are derived from Header class.
     *
     */
    public Message() {
        unrecognizedHeaders = new Vector();
        headers = new Vector();
        nameTable = new Hashtable();
    }

    /**
     * Attaches a header and dies if you get a duplicate header exception.
     * @param h Header to attach.
     * @throws IllegalArgumentException if the header to attach is null.
     * @throws SipException if the header can't be attached for some reason.
     */
    private void attachHeader(Header h)
            throws IllegalArgumentException, SipException {
        if (h == null)
            throw new IllegalArgumentException("null header!");

        if (h instanceof HeaderList) {
            HeaderList hl = (HeaderList) h;
            if (hl.isEmpty()) {
                // System.out.println("Attaching an empty header: " +
                // h.getClass().getName());
                return;
            }
        }

        attachHeader(h, false, false);
    }

    /**
     * Attaches a header (replacing the original header).
     * @param header Header that replaces a header of the same type.
     * @throws IllegalArgumentException if the header to add is null.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setHeader(Header header)
            throws IllegalArgumentException, SipException {
        if (header == null)
            throw new IllegalArgumentException("null header!");

        if (header instanceof HeaderList) {
            HeaderList hl = (HeaderList) header;
            // Ignore empty lists.
            if (hl.isEmpty())
                return;
        }

        attachHeader(header, true, false);
    }

    /**
     * Sets a header from a linked list of headers.
     *
     * @param headers -- a list of headers to set.
     * @throws SipException if some header can't be set for some reason.
     */
    public void setHeaders(Vector headers) throws SipException {
        Enumeration elements = headers.elements();
        while (elements.hasMoreElements()) {
            Header sipHeader = (Header) elements.nextElement();
            attachHeader(sipHeader, false);
        }
    }

    /**
     * Attaches a header to the end of the existing headers in
     * this Message structure.
     * This is equivalent to the attachHeader(Header,replaceflag,false);
     * which is the normal way in which headers are attached.
     * This was added in support of JAIN-SIP.
     *
     * @since 1.0 (made this public)
     * @param h header to attach.
     * @param replaceflag if true then replace a header if it exists.
     * @throws SipException if the header can't be attached for some reason.
     */
    public void attachHeader(Header h, boolean replaceflag)
            throws SipException {
        attachHeader(h, replaceflag, false);
    }

    /**
     * Attaches the header to the SIP Message structure at a specified
     * position in its list of headers.
     *
     * @param header Header to attach.
     * @param replaceFlag If true then replace the existing header.
     * @param top flag to indicate attaching header to the front of list.
     * @throws SipException if the header can't be attached for some reason.
     */
    public void attachHeader(Header header, boolean replaceFlag, boolean top)
            throws SipException {
        if (header == null) {
            throw new NullPointerException("null header");
        }

        // System.out.println(">>> attachHeader( " +
        //                   header + ", " + replaceFlag + ");");

        Header h;
        String expandedHeaderName = NameMap.expandHeaderName(
            header.getHeaderName()).toLowerCase();

        if (ListMap.hasList(header) &&
                ! sipHeaderListClass.isAssignableFrom(header.getClass())) {
            HeaderList hdrList = ListMap.getList(header);

            // Actually, hdrList.size() is always 0.
            if (replaceFlag && (hdrList.size() > 0)) {
                // remove first element
                hdrList.removeElement(hdrList.elementAt(0));
            }

            hdrList.add(header);
            h = hdrList;
        } else {
            h = header;
        }

        if (!replaceFlag && nameTable.containsKey(expandedHeaderName) &&
                !(h instanceof HeaderList)) {
            // Throw an exception here because according to JSR180:
            // "The implementations MAY restrict the access to some
            // headers according to RFC 3261."
            //
            // It may happen if this function is called to add a header that
            // already exist and the only one header of this type may present
            // (Call-Id, From, To).
            //
            throw new SipException("Header '" + header.getHeaderName() +
                "' already exist. Only one header of this type is allowed.",
                    SipException.INVALID_OPERATION);
        }

        // Delete the first header with name = headerName
        // from our list structure.
        // If case of HeaderList the whole list is removed
        // to avoid duplication (it is added bellow).
        if (replaceFlag || (h instanceof HeaderList)) {
            Enumeration li = headers.elements();
            int index;

            for (index = 0; li.hasMoreElements(); index++) {
                Header next = (Header) li.nextElement();
                String currName = NameMap.expandHeaderName(
                    next.getHeaderName());

                if (expandedHeaderName.equalsIgnoreCase(currName)) {
                    headers.removeElementAt(index);
                    break;
                }
            }
        }

        Header hRef = h;

        if (h instanceof HeaderList) {
            HeaderList hdrlist = (HeaderList) nameTable.get(expandedHeaderName);

            if (hdrlist != null) {
                if (replaceFlag) {
                    hdrlist.removeFirst();
                }

                hdrlist.concatenate((HeaderList)h, top);

                // This is required due to the way that 'concatenate'
                // is implemented: if 'top' is false, it modifies
                // the objects itself; otherwise, the list passed
                // as the first parameter is modified.
                if (!top) {
                    hRef = hdrlist;
                }
            }
        }

        nameTable.put(expandedHeaderName, hRef);
        headers.addElement(hRef);

        // Direct accessor fields for frequently accessed headers.
        if (h instanceof FromHeader) {
            this.fromHeader = (FromHeader)h;
        } else if (h instanceof ContentLengthHeader) {
            this.contentLengthHeader = (ContentLengthHeader) h;
        } else if (h instanceof ToHeader) {
            this.toHeader = (ToHeader)h;
        } else if (h instanceof CSeqHeader) {
            this.cSeqHeader = (CSeqHeader) h;
        } else if (h instanceof CallIdHeader) {
            this.callIdHeader = (CallIdHeader) h;
        }
    }

    /**
     * Removes a header given its name. If multiple headers of a given name
     * are present then the top flag determines which end to remove headers
     * from.
     *
     * @param headerName is the name of the header to remove.
     * @param top flag that indicates which end of header list to process.
     */
    public void removeHeader(String headerName, boolean top) {
        // System.out.println("removeHeader " + headerName);

        headerName = NameMap.expandHeaderName(headerName).toLowerCase();
        Header toRemove = (Header) nameTable.get(headerName);

        // nothing to do then we are done.
        if (toRemove == null)
            return;

        if (toRemove instanceof HeaderList) {
            HeaderList hdrList = (HeaderList) toRemove;
            if (top) hdrList.removeFirst();
            else hdrList.removeLast();

            // Clean up empty list
            if (hdrList.isEmpty()) {
                removeHeaderFromList(headerName);
            }
        } else {
            nameTable.remove(headerName);

            if (toRemove instanceof FromHeader) {
                this.fromHeader = null;
            } else if (toRemove instanceof ToHeader) {
                this.toHeader = null;
            } else if (toRemove instanceof CSeqHeader) {
                this.cSeqHeader = null;
            } else if (toRemove instanceof CallIdHeader) {
                this.callIdHeader = null;
            } else if (toRemove instanceof ContentLengthHeader) {
                this.contentLengthHeader = null;
            }

            removeHeaderFromList(headerName);
        }
    }

    /**
     * Removes all headers given its name.
     *
     * @param headerName is the name of the header to remove.
     */
    public void removeHeader(String headerName) {
        if (headerName == null) {
            throw new NullPointerException("null arg");
        }

        headerName = NameMap.expandHeaderName(headerName).toLowerCase();
        Header toRemove = (Header) nameTable.get(headerName);

        // nothing to do then we are done.
        if (toRemove == null)
            return;

        nameTable.remove(headerName);

        // Remove the fast accessor fields.
        if (toRemove instanceof FromHeader) {
            this.fromHeader = null;
        } else if (toRemove instanceof ToHeader) {
            this.toHeader = null;
        } else if (toRemove instanceof CSeqHeader) {
            this.cSeqHeader = null;
        } else if (toRemove instanceof CallIdHeader) {
            this.callIdHeader = null;
        } else if (toRemove instanceof ContentLengthHeader) {
            this.contentLengthHeader = null;
        }

        removeHeaderFromList(headerName);
    }

    /**
     * Generates (compute) a transaction ID for this SIP message.
     * @return A string containing the concatenation of various
     * portions of the FromHeader,To,Via and RequestURI portions
     * of this message as specified in RFC 2543:
     * All responses to a request contain the same values in
     * the Call-ID, CSeqHeader, To, and FromHeader fields
     * (with the possible addition of a tag in the To field
     * (section 10.43)). This allows responses to be matched with requests.
     * Incorporates a fix 
     * for generating transactionIDs when no port is present in the
     * via header.
     * Incorporates a fix 
     *  (converts to lower case when returning the
     * transaction identifier).
     *
     * @return a string that can be used as a transaction identifier
     * for this message. This can be used for matching responses and
     * requests (i.e. an outgoing request and its matching response have
     * the same computed transaction identifier).
     */
    public String getTransactionId() {
        ViaHeader topVia = null;
        if (! this.getViaHeaders().isEmpty()) {
            topVia = (ViaHeader) this.getViaHeaders().first();
        }
        // Have specified a branch Identifier so we can use it to identify
        // the transaction.
        if (topVia.getBranch() != null &&
                topVia.getBranch().startsWith
                (SIPConstants.GENERAL_BRANCH_MAGIC_COOKIE)) {
            // Bis 09 compatible branch assignment algorithm.
            // implies that the branch id can be used as a transaction
            // identifier.
            return topVia.getBranch().toLowerCase();
        } else {
            // Old style client so construct the transaction identifier
            // from various fields of the request.
            StringBuffer retval = new StringBuffer();
            FromHeader from = (FromHeader) this.getFromHeader();
            ToHeader to = (ToHeader) this.getTo();
            String hpFromHeader = from.getUserAtHostPort();
            retval.append(hpFromHeader).append(":");
            if (from.hasTag()) retval.append(from.getTag()).append(":");
            String hpTo = to.getUserAtHostPort();
            retval.append(hpTo).append(":");
            String cid = this.callIdHeader.getCallId();
            retval.append(cid).append(":");
            retval.append(this.cSeqHeader.getSequenceNumber()).append(":").
                    append(this.cSeqHeader.getMethod());
            if (topVia != null) {
                retval.append(":").append(topVia.getSentBy().encode());
                if (!topVia.getSentBy().hasPort()) {
                    retval.append(":").append(5060);
                }
            }
            String hc =
		Utils.toHexString(retval.toString().toLowerCase().getBytes());
            if (hc.length() < 32)
                return hc;
            else return hc.substring(hc.length() - 32, hc.length() -1);
        }
        // Convert to lower case
    }

    /**
     * Returns true if this message has a body.
     * @return true if message body included
     */
    public boolean hasContent() {
        return messageContent != null || messageContentBytes != null;
    }

    /**
     * Returns an iterator for the list of headers in this message.
     * @return an Iterator for the headers of this message.
     */
    public Enumeration getHeaders() {
        return headers.elements();
    }

    /**
     * Gets the first header of the given name.
     * @param headerName requested header
     * @return header the first header of the given name.
     */
    public Header getHeader(String headerName) {
        if (headerName == null)
            throw new NullPointerException("bad name");

        headerName = NameMap.expandHeaderName(headerName).toLowerCase();
        Header sipHeader = (Header)nameTable.get(headerName);

        if (sipHeader == null) {
            return null;
        }

        if (sipHeader instanceof HeaderList) {
            return (Header) ((HeaderList) sipHeader).getFirst();
        } else {
            return (Header) sipHeader;
        }
    }

    /**
     * Gets the contentType header (null if one does not exist).
     * @return contentType header
     */
    public ContentTypeHeader getContentTypeHeader() {
        return (ContentTypeHeader) getHeader(Header.CONTENT_TYPE);
    }

    /**
     * Gets the from header.
     * @return the from header.
     */
    public FromHeader getFromHeader() {
        return (FromHeader) fromHeader;
    }

    /**
     * Gets the Contact list of headers (null if one does not exist).
     * @return List containing Contact headers.
     */
    public ContactList getContactHeaders() {
        return (ContactList) getHeaderList(Header.CONTACT);
    }

    /**
     * Gets the Via list of headers (null if one does not exist).
     * @return List containing Via headers.
     */
    public ViaList getViaHeaders() {
        return (ViaList) getHeaderList(Header.VIA);
    }

    /**
     * Gets an iterator to the list of vial headers.
     * @return a list iterator to the list of via headers.
     * public ListIterator getVia() {
     * return this.viaHeaders.listIterator();
     * }
     */

    /**
     * Sets a list of via headers.
     * @param viaList a list of via headers to add.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setVia(ViaList viaList) throws SipException {
        setHeader(viaList);
    }

    /**
     * Sets a list of via headers.
     * @param viaList a list of via headers to add.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setVia(Vector viaList)  throws SipException {
        this.removeHeader(ViaHeader.NAME);
        for (int i = 0; i < viaList.size(); i++) {
            ViaHeader via = (ViaHeader) viaList.elementAt(i);
            this.addHeader(via);
        }
    }

    /**
     * Sets the header given a list of headers.
     *
     * @param sipHeaderList a headerList to set
     * @throws SipException if the header can't be set for some reason.
     */
    public void setHeader(HeaderList sipHeaderList) throws SipException {
        setHeader((Header)sipHeaderList);
    }

    /**
     * Gets the topmost via header.
     * @return the top most via header if one exists or null if none exists.
     * @throws SipException if the header can't be set for some reason.
     */
    public ViaHeader getTopmostVia() {
        if (getViaHeaders() == null)
            return null;
        else
            return (ViaHeader) (getViaHeaders().getFirst());
    }

    /**
     * Gets the CSeqHeader list of header (null if one does not exist).
     * @return CSeqHeader header
     */
    public CSeqHeader getCSeqHeader() {
        return cSeqHeader; }

    /**
     * Gets the sequence number.
     * @return the sequence number.
     */
    public int getCSeqHeaderNumber() {
        return cSeqHeader.getSequenceNumber();
    }

    /**
     * Gets the Route List of headers (null if one does not exist).
     * @return List containing Route headers
     */
    public RouteList getRouteHeaders() {
        return (RouteList) getHeaderList(Header.ROUTE);
    }

    /**
     * Gets the CallIdHeader header (null if one does not exist).
     *
     * @return Call-ID header.
     */
    public CallIdHeader getCallId() {
        return callIdHeader;
    }

    /**
     * Sets the call id header.
     *
     * @param callId call idHeader (what else could it be?)
     * @throws SipException if the header can't be set for some reason.
     */
    public void setCallId(CallIdHeader callId) throws SipException {
        setHeader(callId);
    }

    /**
     * Gets the CallIdHeader header (null if one does not exist)
     *
     * @param callId -- the call identifier to be assigned to the call id header
     * @throws SipException if the header can't be set for some reason.
     */
    public void setCallId(String callId)
            throws ParseException, SipException {
        if (callIdHeader == null) {
            setHeader(new CallIdHeader());
        }
        callIdHeader.setCallId(callId);
    }

    /**
     * Gets the call ID string.
     * A conveniance function that returns the stuff following
     * the header name for the call id header.
     *
     * @return the call identifier.
     *
     */
    public String getCallIdentifier() {
        return callIdHeader.getCallId();
    }

    /**
     * Gets the RecordRoute header list (null if one does not exist).
     *
     * @return Record-Route header
     */
    public RecordRouteList getRecordRouteHeaders() {
        return (RecordRouteList) this.getHeaderList(Header.RECORD_ROUTE); }


    /**
     * Gets the To header (null if one does not exist).
     * @return To header
     */
    public ToHeader getTo() {
        return (ToHeader) toHeader; }

    /**
     * Sets the To header field  value.
     * @param to the new To field value
     * @throws SipException if the header can't be set for some reason.
     */
    public void setTo(ToHeader to) throws SipException {
        setHeader(to);
    }

    /**
     * Sets the From header field value.
     * @param from the new From header field  value.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setFromHeader(FromHeader from) throws SipException {
        setHeader(from);
    }

    /**
     * Gets the ContentLengthHeader header (null if one does not exist).
     *
     * @return content-length header.
     */
    public ContentLengthHeader getContentLengthHeader() {
        return contentLengthHeader;
    }

    /**
     * Gets the message body as a string.
     * If the message contains a content type header with a specified
     * charset, and if the payload has been read as a byte array, then
     * it is returned encoded into this charset.
     *
     * @return Message body (as a string)
     */
    public String getMessageContent() throws UnsupportedEncodingException {
        if (this.messageContent == null && this.messageContentBytes == null)
            return null;
        else if (this.messageContent == null) {
            ContentTypeHeader contentTypeHeader =
                    (ContentTypeHeader) this.nameTable
                    .get(Header.CONTENT_TYPE.toLowerCase());
            if (contentTypeHeader != null) {
                String charset = contentTypeHeader.getCharset();
                if (charset != null) {
                    this.messageContent =
                            new String(messageContentBytes, charset);
                } else {
                    this.messageContent =
                            new String(messageContentBytes, DEFAULT_ENCODING);
                }
            } else this.messageContent =
                    new String(messageContentBytes, DEFAULT_ENCODING);
        }
        return this.messageContent;
    }

    /**
     * Gets the message content as an array of bytes.
     * If the payload has been read as a String then it is decoded using
     * the charset specified in the content type header if it exists.
     * Otherwise, it is encoded using the default encoding which is
     * UTF-8.
     *
     * @return an array of bytes that is the message payload.
     *
     */
    public byte[] getRawContent() {
        try {
            if (this.messageContent == null &&
                    this.messageContentBytes == null &&
                    this.messageContentObject == null) {
                return null;
            } else if (this.messageContentObject != null) {
                String messageContent = this.messageContentObject.toString();
                byte[] messageContentBytes;
                ContentTypeHeader contentTypeHeader =
                        (ContentTypeHeader)this.nameTable.get
                        (Header.CONTENT_TYPE.toLowerCase());
                if (contentTypeHeader != null) {
                    String charset = contentTypeHeader.getCharset();
                    if (charset != null) {
                        messageContentBytes = messageContent.getBytes(charset);
                    } else {
                        messageContentBytes =
                                messageContent.getBytes(DEFAULT_ENCODING);
                    }
                } else messageContentBytes =
                        messageContent.getBytes(DEFAULT_ENCODING);
                return messageContentBytes;
            } else if (this.messageContent != null) {
                byte[] messageContentBytes;
                ContentTypeHeader contentTypeHeader =
                        (ContentTypeHeader)this.nameTable.get
                        (Header.CONTENT_TYPE.toLowerCase());
                if (contentTypeHeader != null) {
                    String charset = contentTypeHeader.getCharset();
                    if (charset != null) {
                        messageContentBytes =
                                this.messageContent.getBytes(charset);
                    } else {
                        messageContentBytes =
                                this.messageContent.getBytes(DEFAULT_ENCODING);
                    }
                } else messageContentBytes =
                        this.messageContent.getBytes(DEFAULT_ENCODING);
                return messageContentBytes;
            } else {
                return messageContentBytes;
            }
        } catch (UnsupportedEncodingException ex) {
            InternalErrorHandler.handleException(ex);
            return null;
        }
    }

    /**
     * Sets the message content given type and subtype.
     *
     * @param type is the message type (eg. application)
     * @param subType is the message sybtype (eg. sdp)
     * @param messageContent is the message content as a string.
     * @throws IllegalArgumentException if some parameter is invalid.
     */
    public void setMessageContent(String type, String subType,
            String messageContent)
            throws IllegalArgumentException {
        if (messageContent == null) {
            throw new IllegalArgumentException("messgeContent is null");
        }

        ContentTypeHeader ct = new ContentTypeHeader(type, subType);

        try {
            setHeader(ct);
        } catch (SipException se) {
            throw new IllegalArgumentException(se.getMessage());
        }

        messageContent = messageContent;
        messageContentBytes = null;
        messageContentObject = null;

        ContentLengthHeader h = getContentLengthHeader();
        if (h != null) {
            h.setContentLength(messageContent.length());
        }
    }

    /**
     * Sets the message content after converting the given object to a
     * String.
     *
     * @param content content to set.
     * @param contentTypeHeader content type header corresponding to
     * content.
     * @throws NullPointerException if the 'content' parameter is null.
     * @throws SipException if the content can't be set for some reason.
     */
    public void setContent(Object content, ContentTypeHeader contentTypeHeader)
            throws SipException {
        if (content == null) {
            throw new NullPointerException("null content");
        }

        String contentString = content.toString();
        this.setMessageContent(contentString);
        this.setHeader(contentTypeHeader);
        this.removeContent();

        if (content instanceof String) {
            this.messageContent = (String)content;
        } else if (content instanceof byte[]) {
            this.messageContentBytes = (byte[]) content;
        } else this.messageContentObject = content;

        int length = -1;
        if (content instanceof String)
            length = ((String)content).length();
        else if (content instanceof byte[])
            length = ((byte[])content).length;

        ContentLengthHeader h = getContentLengthHeader();
        if (length != -1 && h != null) {
            h.setContentLength(length);
        }
    }

    /**
     * Sets the message content after converting the given object to a
     * String.
     *
     * @param content content to set.
     * @throws NullPointerException if the content parameter is null.
     */
    public void setContent(Object content) {
        if (content == null)
            throw new NullPointerException("null content");

        String contentString = content.toString();
        this.setMessageContent(contentString);
        this.removeContent();

        if (content instanceof String) {
            this.messageContent = (String)content;
        } else if (content instanceof byte[]) {
            this.messageContentBytes = (byte[]) content;
        } else this.messageContentObject = content;

        int length = -1;
        if (content instanceof String)
            length = ((String)content).length();
        else if (content instanceof byte[])
            length = ((byte[])content).length;

        ContentLengthHeader h = getContentLengthHeader();
        if (length != -1 && h != null) {
            h.setContentLength(length);
        }
    }

    /**
     * Gets the content of the header.
     *
     * @return the content of the sip message.
     */
    public Object getContent() {
        if (this.messageContentObject != null)
            return messageContentObject;
        else if (this.messageContentBytes != null)
            return this.messageContentBytes;
        else if (this.messageContent != null)
            return this.messageContent;
        else return null;
    }

    /**
     * Sets the message content for a given type and subtype.
     *
     * @param type is the messge type.
     * @param subType is the message subType.
     * @param messageContent is the message content as a byte array.
     * @throws SipException if the message content can't be set.
     */
    public void setMessageContent(String type, String subType,
            byte[] messageContent) throws SipException {
        ContentTypeHeader ct = new ContentTypeHeader(type, subType);
        setHeader(ct);
        setMessageContent(messageContent);

        ContentLengthHeader h = getContentLengthHeader();
        if (h != null) {
            h.setContentLength(messageContent.length);
        }
    }

    /**
     * Sets the message content for this message.
     *
     * @param content Message body as a string.
     */
    public void setMessageContent(String content) {
        ContentLengthHeader h = getContentLengthHeader();

        if (h != null) {
            int clength = (content == null ? 0: content.length());
            h.setContentLength(clength);
        }

        messageContent = content;
        messageContentBytes = null;
        messageContentObject = null;
    }

    /**
     * Sets the message content as an array of bytes.
     *
     * @param content is the content of the message as an array of bytes.
     */
    public void setMessageContent(byte[] content) {
        ContentLengthHeader h = getContentLengthHeader();

        if (h != null) {
            h.setContentLength(content.length);
        }

        messageContentBytes = content;
        messageContent = null;
        messageContentObject = null;
    }

    /**
     * Removes the message content if it exists.
     */
    public void removeContent() {
        messageContent = null;
        messageContentBytes = null;
        messageContentObject = null;
    }

    /**
     * Gets a SIP header or Header list given its name.
     * @param headerName is the name of the header to get.
     * @return a header or header list that contians the retrieved header.
     */
    public Enumeration getHeaders(String headerName) {
        if (headerName == null) {
            throw new NullPointerException("null headerName");
        }

        Header sipHeader = (Header)nameTable.get(
                NameMap.expandHeaderName(headerName).toLowerCase());

        // empty iterator
        if (sipHeader == null) {
            return new Vector().elements();
        }

        if (sipHeader instanceof HeaderList) {
            return ((HeaderList) sipHeader).getElements();
        } else {
            Vector v = new Vector();
            v.addElement(sipHeader);
            return v.elements();
        }
    }

    /**
     * Gets a SIP Header list given its name.
     * @param headerName is the name of the header to get.
     * @return a header list that contains the retrieved header.
     */
    public HeaderList getHeaderList(String headerName) {
        Header header = (Header)nameTable.get(
            NameMap.expandHeaderName(headerName).toLowerCase());

        if (header == null) {
            return null;
        }

        if (header instanceof HeaderList) {
            return (HeaderList)header;
        } else {
            HeaderList hl = new HeaderList();
            hl.add(header);
            return hl;
        }
    }

    /**
     * Returns true if the Message has a header of the given name.
     *
     * @param headerName is the header name for which we are testing.
     * @return true if the header is present in the message
     */
    public boolean hasHeader(String headerName) {
        return nameTable.containsKey(
            NameMap.expandHeaderName(headerName).toLowerCase());
    }

    /**
     * Returns true if the message has a FromHeader header tag.
     *
     * @return true if the message has a from header and that header has
     * a tag.
     */
    public boolean hasFromHeaderTag() {
        return fromHeader != null && fromHeader.getTag() != null;
    }

    /**
     * Returns true if the message has a To header tag.
     *
     * @return true if the message has a to header and that header has
     * a tag.
     */
    public boolean hasToTag() {
        return toHeader != null && toHeader.getTag() != null;
    }

    /**
     * Returns the from tag.
     *
     * @return the tag from the from header.
     *
     */
    public String getFromHeaderTag() {
        return fromHeader == null? null: fromHeader.getTag();
    }

    /**
     * Sets the FromHeader Tag.
     *
     * @param tag -- tag to set in the from header.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setFromHeaderTag(String tag) throws SipException {
        fromHeader.setTag(tag);
    }

    /**
     * Sets the to tag.
     *
     * @param tag -- tag to set.
     * @throws SipException if the header can't be set for some reason.
     */
    public void setToTag(String tag) throws SipException {
        toHeader.setTag(tag);
    }

    /**
     * Returns the To tag.
     * @return the To tag field
     */
    public String getToTag() {
        return toHeader == null ? null : toHeader.getTag();
    }

    /**
     * Returns the encoded first line.
     * @return the first line
     */
    public abstract String getFirstLine();

    /**
     * Adds a SIP header.
     * @param sipHeader -- sip header to add.
     * @throws SipException if the header can't be added for some reason.
     */
    public void addHeader(Header sipHeader) throws SipException {
        Header sh = (Header) sipHeader;
        // Add the header value topmost of this type of headers
        // as required by JSR180.
        attachHeader(sh, false, true);

        /*
        if (sipHeader instanceof ViaHeader) {
            attachHeader(sh, false, true);
        } else {
            attachHeader(sh, false, false);
        }
        */
    }

    /**
     * Adds a header to the unparsed list of headers.
     *
     * @param unparsed -- unparsed header to add to the list.
     */
    public void addUnparsed(String unparsed) {
        unrecognizedHeaders.addElement(unparsed);
    }

    /**
     * Adds a SIP header.
     * @param sipHeader -- string version of SIP header to add.
     * @throws SipException if the header can't be added for some reason.
     */
    public void addHeader(String sipHeader) throws SipException {
        String hdrString = sipHeader.trim() + "\n";
        try {
            HeaderParser parser = ParserFactory.createParser(sipHeader);
            Header sh = parser.parse();
            attachHeader(sh, false);
        } catch (ParseException ex) {
            unrecognizedHeaders.addElement(hdrString);
        }
    }

    /**
     * Gets a list containing the unrecognized headers.
     * @return a linked list containing unrecongnized headers.
     */
    public Enumeration getUnrecognizedHeaders() {
        return unrecognizedHeaders.elements();
    }

    /**
     * Gets the header names.
     *
     * @return a list iterator to a list of header names. These are ordered
     * in the same order as are present in the message.
     */
    public Enumeration getHeaderNames() {
        Enumeration li = this.headers.elements();
        Vector retval = new Vector();

        while (li.hasMoreElements()) {
            Header sipHeader = (Header) li.nextElement();
            String name = sipHeader.getName();
            retval.addElement(name);
        }

        return retval.elements();
    }

    /**
     * Compares for equality.
     *
     * @param other the other object to compare with.
     * @return true if object matches
     */
    public boolean equals(Object other) {
        if (!other.getClass().equals(this.getClass()))
            return false;

        Message otherMessage = (Message) other;
        Enumeration values = this.nameTable.elements();
        Hashtable otherNameTable = otherMessage.nameTable;

        if (otherNameTable.size() != nameTable.size())
            return false;

        while (values.hasMoreElements()) {
            Header mine = (Header) values.nextElement();
            // maybe short form
            String mineName = 
                NameMap.expandHeaderName(mine.getHeaderName()).trim()
                    .toLowerCase();
            Header hisHeader = (Header) otherNameTable.get(mineName);
            String his = null;
            if (hisHeader != null) {
                his = hisHeader.toString().trim();
            }
            
            if (his == null) {
                return false;
            } else if (! his.equals(mine.toString().trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the content length header.
     *
     * @param contentLength content length header.
     * @throws SipException if Content-Length header can't be set
     * for some reason.
     */
    public void setContentLength(ContentLengthHeader contentLength)
            throws SipException {
        setHeader(contentLength);
    }

    /**
     * Sets the CSeqHeader header.
     *
     * @param cseqHeader CSeqHeader Header.
     * @throws SipException if CSeq header can't be added for some reason.
     */
    public void setCSeqHeader(CSeqHeader cseqHeader) throws SipException {
        setHeader(cseqHeader);
    }

    /**
     * Sets the SIP version header.
     * @param sipVersion the new version string
     */
    public abstract void setSIPVersion(String sipVersion) throws ParseException;

    /**
     * Gets the SIP vesrion string.
     * @return the SIP version string
     */
    public abstract String getSIPVersion();
}
