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

package com.sun.midp.io.j2me.mms;

import java.io.IOException;
import java.io.InterruptedIOException;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.microedition.io.Datagram;
import com.sun.midp.io.j2me.sms.*;

/**
 * Datagram implmentation of MMS low level transport emulation.
 * @see com.sun.midp.io.j2me.sms.DatagramImpl 
 */
public class DatagramImpl extends com.sun.midp.io.j2me.sms.DatagramImpl {

    /**
     * Acknowledgement of individual datagram packets
     */
    private static final byte[] ackBytes = "ACK".getBytes();

    /**
     * Sends a buffer of message data to the designated address.
     *
     * @param type message type ("multipart")
     * @param address - the application-id on the recevier's side
     * @param buffer the block of data to be transmitted
     * @param senderAppID sender's reply application-id 
     * @return the timestamp included in the sent message
     * @exception IOException if any I/O error occurs
     */
    public long send(String type, String address, byte[] buffer,
		     String senderAppID) throws IOException {

        /** Formatted datagram record. */
        DatagramRecord dr = new DatagramRecord();
    
        /** Saved timestamp for use with multiple segment records. */
        long sendtime = System.currentTimeMillis();
    
        /** Offset in the sending buffer for the current fragment. */
        int offset = 0;
    
        /** Total length of the multisegment transmission. */
        int length;
    
        /** Number of segments that need to be sent. */
        int segments;
    
        /** Extra header size for concatenated messages. */
        int headersize = 50;
        
        if (buffer == null) {
            /* 
             * Allow sending messages with empty buffer.
             */
            buffer = new byte[0];
        } 
     
        fragmentsize = 1200;

        if (buffer.length < fragmentsize) {
            segments = 1;
        } else {
            fragmentsize = fragmentsize - headersize;
            segments = (buffer.length + fragmentsize - 1) / fragmentsize;
        }
        length = buffer.length;
    
        /* Fragment the data buffer into multiple segments. */
        for (int i = 0; i < segments; i++) {    
            dr.setHeader("Date", String.valueOf(sendtime));
            if (address != null) {
                dr.setHeader("Address", address);
            }
            if (senderAppID == null) {
                dr.setHeader("SenderAddress", phoneNumber);
            } else {
                dr.setHeader("SenderAddress", "mms://" + phoneNumber + ":" + 
                    senderAppID);
            }
    
            dr.setHeader("Content-Type", type);
            dr.setHeader("Content-Length", String.valueOf(buffer.length));
            dr.setHeader("Segments", String.valueOf(segments));
    
            if (segments > 1) {
                offset = i* fragmentsize;
                length =  (i < (segments -1) ? fragmentsize :
                       buffer.length - (fragmentsize * i));
                dr.setHeader("Fragment", String.valueOf(i));
                dr.setHeader("Fragment-Size", String.valueOf(length));
                dr.setHeader("Fragment-Offset", String.valueOf(offset));
            }
            byte[] buf = new byte[length];
            System.arraycopy(buffer, offset, buf, 0, length);
            dr.setData(buf);
            byte [] messdata = dr.getFormattedData();
            
            Datagram mess = dgc.newDatagram(messdata.length);           
            mess.setAddress("datagram://" + dhost + ":" + dportout);
            mess.setData(messdata, 0, messdata.length);
            dgc.send(mess);
        }
        return sendtime;
    }


    /**
     * Receives a block of data from the comm connection. 
     * Overridden to acknowledge packets if necessary.
     * @param forThisConnection the MessageConnection requesting the message.
     * @return an array of raw data from the comm device
     * @exception IOException if any I/O error occurs
     */
    public TransportMessage receive(MessageConnection forThisConnection)
        throws IOException {
        Datagram mess = null;
        DatagramRecord previous  = null;
        DatagramRecord current = null;
    
        if (reader == null) {
            /* Spawn a thread to act as the inbound message reader. */
            reader = new Thread(new SubclassedDatagramReader());
            reader.start();
        }
    
        while (true) {
    
	    /* Wait until a message is available. */
	    synchronized (queue) {
            while (queue.size() == 0) {
                try {
                    queue.wait();
                } catch (InterruptedException ie) {
                    // TEMP: Ignore errors while waiting
                }
                if (!((Protocol)forThisConnection).open) {
                    throw new InterruptedIOException("connection closed");
                }
            }
            /* Pop the top message off the stack. */
            mess = (Datagram) queue.elementAt(0);
            queue.removeElementAt(0);
    
            }
            current = new DatagramRecord();
            boolean expectMore = 
                current.parseData(mess.getData(), mess.getLength());
            String ackPortStr = current.getHeader("Ack-Port");
            String ackCookie = current.getHeader("Ack-Cookie");
            if (ackPortStr != null && ackCookie != null) {
                int ackPort = 0;
                try {
                    ackPort = Integer.parseInt(ackPortStr);
                } catch (NumberFormatException nfe) {
                    // ignore
                }
                if (ackPort != 0) {
                    String ackStr = "ACK:" + ackCookie + "\n";
                    byte[] ackBytes = ackStr.getBytes();                 
                    Datagram ack = dgc.newDatagram(ackBytes.length);
                    ack.setAddress("datagram://" + dhost + ":" + ackPort);
                    ack.setData(ackBytes, 0, ackBytes.length);
                    dgc.send(ack);
                }
            }    
            if (expectMore) {
                /* Multiple segments need to be aggregated together. */
                if (current.addData(previous)) {
                    /*
                     * Break out of the loop when a multi-part
                     * transmission is complete.
                     */
                    break;
                }
            } else {
                break;
            }
            /* Save a pointer to this packet in case there are more. */
            previous = current;
    
        }
    
        String addr = current.getHeader("CBSAddress");
        if (addr == null) {
            addr = current.getHeader("Address");
        }
        String senderAddr = current.getHeader("SenderAddress");
    
        long time = 0;
        try {
            time = Long.parseLong(current.getHeader("Date"));
        } catch (NumberFormatException nfe) {
            // TEMP - ignore NFE
        }
        String type = current.getHeader("Content-Type");
    
        byte[] buf = current.getData();
        if (type.equals("text")) {
            /* Always store text messages as UCS 2 bytes. */
            String te = current.getHeader("Text-Encoding");
            if (te != null && te.equals("gsm7bit")) {
            buf = TextEncoder.decode(buf);
            }
        }
        return new TransportMessage(addr, senderAddr, type, time, buf);
    }
    
    /**
     * Returns how many segments in the underlying protocol would be needed for
     * sending the <code>Message</code> given as the parameter.
     *
     * @param msg the message to be used for the calculation
     * @return number of protocol segments needed for sending the message.
     *     Returns <code>0</code> if the <code>Message</code> object cannot be
     *     sent using the underlying protocol.
     */
    public int numberOfSegments(Message msg) {
        if (!(msg instanceof MultipartObject)) {
            return 0;
        }
        /** Number of segments that need to be sent. */
        int segments;
    
        /** Extra header size for concatenated messages. */
        int headersize = 7;
        
        byte[] buf = null;
        try {
            buf = ((MultipartObject)msg).getAsByteArray();
        } catch (IOException ioe) {
            // ignore this.
        }
    
        /*
         * Check for a message with an empty buffer which is allowed, 
         * and would still require one protocol segment.
         */
        if (buf == null) {
            return 1;
        }

        fragmentsize = 1200;
    
        if (buf.length < fragmentsize) {
            segments = 1;
        } else {
            fragmentsize = fragmentsize - headersize;
            segments = (buf.length + fragmentsize - 1) / fragmentsize;
        }
    
        return  segments; // need revisit 
    }

    /**
     * This class is here only so we can construct an instance of its parent
     * because the parent has a protected constructor.
     */
    public class SubclassedDatagramReader extends 
        com.sun.midp.io.j2me.sms.DatagramImpl.DatagramReader {
	/** Constructor. */            
        public SubclassedDatagramReader() {
            super();
        }
    }
    
    /**
     * Returns the device identifier string. This is usually a phone number.
     * @return the device identifier string
     */
    public String getDeviceID() {
        return phoneNumber;
    }
}
