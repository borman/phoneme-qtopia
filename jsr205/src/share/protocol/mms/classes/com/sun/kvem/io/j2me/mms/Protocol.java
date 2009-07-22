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

package com.sun.kvem.io.j2me.mms;

import javax.wireless.messaging.*;
import java.io.*;
import java.util.*;
import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.j2me.mms.*;

public class Protocol extends com.sun.midp.io.j2me.mms.Protocol {

    static final String STREAM_SIGNATURE = "application/vnd.wap.mms-message";

    private static final String HEADER_CONTENT_ID = "Content-ID";
    private static final String CONTENT_TYPE__NAME_PREFIX = "; name=\"";
    private static final String CONTENT_TYPE__CHARSET_PREFIX = "; charset=\"";

    static final String[] ALLOWED_HEADER_FIELDS = {
        "X-Mms-Delivery-Time",
        "X-Mms-Priority"
    };

    static final int NETWORK_MONITOR_SEND = 1;
    static final int NETWORK_MONITOR_RECEIVE = 2;

    private static native void send0(byte buf []);
    private static native void received0(byte buf[]);


    public void send(Message dmsg) throws IOException {

        super.send(dmsg);

        MultipartMessage m;

        // Validating the dmsg is of type MultipartMessage
         if (dmsg instanceof MultipartMessage) {
            m = (MultipartMessage) dmsg;

        } else  {
            throw new IOException("MMS Message isn't instance of MultipartMessage");
        }

        sendMessageToNetworkMonitor(m, NETWORK_MONITOR_SEND);
    }


    public synchronized Message receive()
        throws IOException {

        Message dmsg = super.receive();

        MultipartMessage m;

        if (dmsg instanceof MultipartMessage) {
            m = (MultipartMessage) dmsg;

        } else  {
            throw new IOException("Received MMS Message isn't instance of MultipartMessage");
        }


        sendMessageToNetworkMonitor(m, NETWORK_MONITOR_RECEIVE);

        return dmsg;
    }


     /**
     * Notify the Network Monitor that a message has been sent or arrived,
     * according to <code> direction</code> parameter
     * @exception IOException if any I/O errors occur
     */

    private void sendMessageToNetworkMonitor(MultipartMessage m, int direction) throws IOException{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(STREAM_SIGNATURE);

        dos.writeUTF("X-Mms-Message-Type");
        dos.writeUTF("m-send-req");
        dos.writeUTF("X-Mms-Transaction-ID");
        dos.writeUTF(String.valueOf(System.currentTimeMillis()));
        dos.writeUTF("X-Mms-Version");
        dos.writeUTF("1.0");

        for (int i = 0; i < ALLOWED_HEADER_FIELDS.length; ++i) {
            String headerValue = m.getHeader(ALLOWED_HEADER_FIELDS[i]);
            if (headerValue != null) {
                dos.writeUTF(ALLOWED_HEADER_FIELDS[i]);
                dos.writeUTF(headerValue);
            }
        }

        if (m.getAddress() != null) {
            dos.writeUTF("From");
            String add = null;
            switch (direction) {
            case NETWORK_MONITOR_SEND:

                /* here we don't get the "from" field from the message
                   since it might be null!                          */

                add = "mms://" + getPhoneNumber0();

                break;
            case NETWORK_MONITOR_RECEIVE:
                add = (m.getAddresses("from"))[0];

                break;
            }
            dos.writeUTF(getDevicePortionOfAddress(add ));
        }

        if (m.getAddresses("to").length != 0) {
            dos.writeUTF("To");
            Vector to = new Vector();
            switch (direction) {
            case NETWORK_MONITOR_SEND:

                for (int i=0 ; i<m.getAddresses("to").length ; i++)
                {
                    to.addElement(m.getAddresses("to")[i]);
                }
                break;

            case NETWORK_MONITOR_RECEIVE:
                to.addElement("mms://"+getPhoneNumber0());
                break;
            }
            writeVector(dos, to, true);

        }

        if ((m.getAddresses("cc")!=null) && (m.getAddresses("cc").length != 0)) {
            dos.writeUTF("Cc");
            Vector cc = new Vector();
            for (int i=0 ; i<m.getAddresses("cc").length ; i++)
            {
                cc.addElement(m.getAddresses("cc")[i]);
            }
            writeVector(dos, cc, true);
        }

        if ((m.getAddresses("bcc")!=null) && (m.getAddresses("bcc").length != 0)) {
            dos.writeUTF("Bcc");
            Vector bcc = new Vector();
            for (int i=0 ; i<m.getAddresses("bcc").length ; i++)
            {
                bcc.addElement(m.getAddresses("bcc")[i]);
            }
            writeVector(dos, bcc, true);
        }

        dos.writeUTF("Date");
        dos.writeUTF(String.valueOf(System.currentTimeMillis()));

       if (m.getSubject() != null) {
            dos.writeUTF("Subject");
            dos.writeUTF(m.getSubject());
        }

        dos.writeUTF("Content-Type");
        Vector contentTypeElements = new Vector();

        if (m.getStartContentId() != null) {
            contentTypeElements.addElement(
                "application/vnd.wap.multipart.related");
        } else {
            contentTypeElements.addElement(
                "application/vnd.wap.multipart.mixed");
        }


        if (m.getStartContentId() != null) {
            contentTypeElements.addElement("start = <" + m.getStartContentId() + ">");
            contentTypeElements.addElement("type = " +
                m.getMessagePart(m.getStartContentId()).getMIMEType());
        }


        if (m instanceof MultipartObject)
        {

            MultipartObject mpo = (MultipartObject)m;
            if (mpo.getApplicationID() != null) {

                contentTypeElements.addElement("Application-ID = " + mpo.getApplicationID());
            } else
                if (direction == NETWORK_MONITOR_RECEIVE)
                {
                   contentTypeElements.addElement("Application-ID = " + getAppID());
                }
            if (mpo.getReplyToApplicationID() != null) {
                contentTypeElements.addElement("Reply-To-Application-ID = " +
                    mpo.getReplyToApplicationID());
            }
        }

        writeVector(dos, contentTypeElements, false);
        dos.writeUTF("nEntries");
        int numParts = 0;
        if (m.getMessageParts()!=null) numParts = m.getMessageParts().length;
        dos.writeUTF(String.valueOf(numParts));
        for (int i = 0; i < numParts; ++i) {

            MessagePart p = m.getMessageParts()[i];
            writeMessagePart(dos, p);
        }

        dos.close();
        byte[] returnMe = baos.toByteArray();
        baos.close();

        // Invoking the native LIME command
        switch (direction) {
        case NETWORK_MONITOR_SEND:
            send0(returnMe);
            break;
        case NETWORK_MONITOR_RECEIVE:
            received0(returnMe);
            break;
        }
    }


    /**
     * Writes a message part to the output stream
     * @param dos the data output stream for writing
     * @param p the message part to be written
     * @exception IOException if any I/O errors occur
     */
    private static void writeMessagePart(DataOutputStream dos, MessagePart p)
            throws IOException {
        dos.writeUTF("Content-Type");
        StringBuffer contentType = new StringBuffer(p.getMIMEType());
        String loc = p.getContentLocation();
        if (loc != null) {
            contentType.append(CONTENT_TYPE__NAME_PREFIX);
            contentType.append(loc);
            contentType.append("\"");
        }
        String enc = p.getEncoding();
        if (enc != null) {
            contentType.append(CONTENT_TYPE__CHARSET_PREFIX);
            contentType.append(enc);
            contentType.append("\"");
        }

        dos.writeUTF(contentType.toString());

        String id = p.getContentID();   // TODO: always not null (checked earlier)??
        if (id != null) {
            dos.writeUTF(HEADER_CONTENT_ID);
            dos.writeUTF(id);
        }

        // the payload
        dos.writeUTF("Content-Length");
        dos.writeInt(p.getLength());
        dos.writeUTF("Content");
        dos.write(p.getContent());
    }

    /**
     * Writes a vector to an output stream. If the contents are MMS addresses,
     * as indicated by the <code>isAddress</code> parameter,
     * then only the device part of the address is placed into the vector,
     * not the application-id, if any.
     * @param dos the data output stream for writing
     * @param v the array to be written
     * @param isAddress is the contents of the vector an MMS address.
     * @exception IOException if any I/O errors occur
     */
    private static void writeVector(DataOutputStream dos, Vector v, boolean isAddress)
        throws IOException {
        StringBuffer buff = new StringBuffer();
        int len = v.size();
        String appendMe = null;
        if (len > 0) {
            appendMe = (String)v.elementAt(0);
            if (isAddress) {
                appendMe = getDevicePortionOfAddress(appendMe);
            }
            buff.append(appendMe);
        }
        for (int i = 1; i < len; ++i) {
            buff.append("; ");
            appendMe = (String)v.elementAt(i);
            if (isAddress) {
                appendMe = getDevicePortionOfAddress(appendMe);
            }
            buff.append(appendMe);
        }
        dos.writeUTF(buff.toString());
    }

     /**
     * Returns only the device part of the MMS Address.
     * @return the device portion of the MMS Address
     * @param address the MMS address
     * @throws IllegalArgumentException the MMS Address has no device portion.
     */
    private static String getDevicePortionOfAddress(String address)
        throws IllegalArgumentException {
        MMSAddress parsedAddress = MMSAddress.getParsedMMSAddress(address);
        if ((parsedAddress == null) || (parsedAddress.address == null)) {
            throw new IllegalArgumentException(
                "MMS Address has no device portion");
        }
        return parsedAddress.address;
    }


}
