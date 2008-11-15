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

package com.sun.tck.wma.mms;

import com.sun.tck.wma.PropLoader;
import com.sun.tck.wma.BinaryMessage;
import com.sun.tck.wma.Message;
import com.sun.tck.wma.MessageConnection;
import com.sun.tck.wma.MessageTransportConstants;
import com.sun.tck.wma.TextMessage;
import com.sun.tck.wma.MultipartMessage;
import com.sun.tck.wma.sms.BinaryObject;
import com.sun.tck.wma.sms.MessagePacket;
import com.sun.tck.wma.sms.TextObject;
import com.sun.midp.io.j2me.sms.TextEncoder;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Vector;

import java.io.IOException;

/**
 * MMS message connection handler.
 */
public class MMSMessageConnection extends PropLoader
    implements MessageConnection {

    /** Machine name - the parsed target address from the URL. */
    protected String host = null;

    /** Application ID - the parsed ID from the URL. */
    protected String appID = null;

    /** Datagram host for sending/receiving. */
    protected String clientHost;

    /** Datagram transport for sending. */
    protected int portOut;

    /** Datagram transport for receiving. */
    protected int portIn;

    /** Phone number of the message sender. */
    protected String phoneNumber;

    /** The application ID to which replies should be sent. */
    protected String replyToAppID;

    /** Datagram server connection. */
    DatagramSocket dgc; 

    /** Datagram buffer. */
    byte [] buf = new byte[MessageTransportConstants.DATAGRAM_PACKET_SIZE];
    
    /** Datagram envelope for sending or receiving messages. */
    DatagramPacket mess =
        new DatagramPacket(buf, MessageTransportConstants.DATAGRAM_PACKET_SIZE);

    /**
     * The "open" flag indicates when the connection is open. When the
     * connection is closed, subsequent operations throw an exception.
     */
    protected boolean open;


    /**
     * Construct a new MMS message connection handler.
     */
    public MMSMessageConnection() {

        /* 
         * Configurable parameters for low level transport.
         * e.g.: mms://+5551234:33300 maps to datagram://129.148.70.80:123
         */

        // Default values:
        clientHost = "localhost";
        portOut = 33300;
        portIn = 33301;
        phoneNumber = "+5551234";
        replyToAppID = "com.sun.mms.MMSTest";

        /* 
         * Check for overrides in the "connections.prop" configuration file.
         */

        clientHost = getProp("localhost", "JSR_120_DATAGRAM_HOST",
            "connections.prop", "DatagramHost");
        
        portOut = getIntProp(33300, "JSR_205_MMS_OUT_PORT",
            "connections.prop", "MMSDatagramPortOut");

        portIn = getIntProp(33301, "JSR_205_MMS_PORT",
            "connections.prop", "MMSDatagramPortIn");

        // Sender (This connection)'s phone number.
        phoneNumber = getProp("+5551234", "JSR_120_PHONE_NUMBER",
            "connections.prop", "PhoneNumber");

        // Sender (This connection)'s application  ID.
        replyToAppID = getProp("com.sun.mms.MMSTest", 
            "JSR_205_MMS_REPLY_TO_ID", "connections.prop", "MMSReplyToAppID");

    }

    /**
     * Opens a connection. This method is called from
     * <code>Connector.open()</code> method to obtain the destination address
     * given in the <code>name</code> parameter.
     * <p>
     * The format for the <code>name</code> string for this method is:
     * <code>mms://<em>phone_number</em>:<em>port</em></code> where the
     * <em>phone_number:</em> is optional. If the <em>phone_number</em>
     * parameter is present, the connection is being opened in "client" mode.
     * This means that messages can be sent. If the parameter is absent, the
     * connection is being opened in "server" mode. This means that messages
     * can be sent and received.
     * <p>
     * The connection is opened to any of the following, low-level transport
     * mechanisms:
     * <ul>
     * <li>A datagram Short Message Peer to Peer (SMPP) to a service center.
     * <li>A <code>comm</code> connection to a phone device with AT-commands.
     * <li>a native MMS stack.
     * </ul>
     *
     * @param name the target of the connection
     * @return this connection
     * @throws IOException if the connection is closed or unavailable.
     */
    public MessageConnection openPrim(String name) throws IOException {

        // Invoke the implementation-specific handler.
        return openPrimInternal(name);
    }


    /**
     * Opens a connection. This method is called from
     * <code>Connector.open()</code> method to obtain the destination address
     * given in the <code>name</code> parameter.
     * <p>
     * The format for the <code>name</code> string for this method is:
     * <code>mms://<em>phone_number</em>:<em>appID</em></code> where the
     * <em>phone_number:</em> is optional. If the <em>phone_number</em>
     * parameter is present, the connection is being opened in "client" mode.
     * This means that messages can be sent. If the parameter is absent, the
     * connection is being opened in "server" mode. This means that messages
     * can be sent and received.
     * <p>
     * The connection is opened to any of the following, low-level transport
     * mechanisms:
     * <ul>
     * <li>A datagram Short Message Peer to Peer (SMPP) to a service center.
     * <li>A <code>comm</code> connection to a phone device with AT-commands.
     * <li>a native MMS stack.
     * </ul>
     *
     * @param name the target of the connection
     * @return this connection
     * @throws IOException if the connection is closed or unavailable.
     */
    public MessageConnection openPrimInternal(String name) throws IOException {

        /*
         * The general form of a MMS address is <code>mms://host:appID</code>.
         * The form at this point should now be <code>//host:appID</code>
         */
        if ((name == null) || (name.length() <= 2) ||
            (name.charAt(0) != '/') || (name.charAt(1) != '/')) {

            throw new IllegalArgumentException("Missing protocol separator.");
        }

        String fullAddress = "mms:" + name;

        MMSAddress parsedAddress = MMSAddress.getParsedMMSAddress(fullAddress);
        if (parsedAddress == null) {
            throw new IllegalArgumentException("Invalid MMS connection URL");
        }

        // Pick up the phone number to which the message will be sent.
        host = null;
        if (parsedAddress.address != null) {
            host = new String(parsedAddress.address);
        }

        // Pick up the application ID to which the message will be sent.
        appID = null;
        if (parsedAddress.appId != null) {
            appID = new String(parsedAddress.appId);
        }

        // Open the inbound server datagram connection. The appID is not used.
        try {
            dgc = open0(appID);
        } catch (IOException ioe) {
            throw new IOException("Unable to open MMS connection.");
        }
        open = true;

        // Return this open connection.
        return this;
    }

    /**
     * Open the transport-layer-level connection.
     *
     * @param appID The application ID (Unused in this implementation).
     * @return The open datagram socket (Transport mechanism).
     */
    private DatagramSocket open0(String appID) throws IOException {

        return new DatagramSocket(portIn);
    }

    /**
     * Constructs a new message object of <code>MULTIPART_MESSAGE</code> type.
     * <p>
     * If this method is called in a sending mode, a new 
     * <code>Message</code> object is requested from the connection. Example:
     * <p>
     * <code>Message msg = conn.newMessage(MULTIPART_MESSAGE);</code>
     * <p>
     * The created <code>Message</code> does not have the destination
     * address set. It must be set by the application before 
     * the message is sent.
     * <p>
     * If it is called in receiving mode, the <code>Message</code> object does
     * have  its address set. The application can act on the object to extract 
     * the address and message data. 
     * <p>
     * <!-- The <code>type</code> parameter indicates the number of bytes 
     * that should be
     * allocated for the message. No restrictions are placed on the application 
     * for the value of <code>size</code>.
     * A value of <code>null</code> is permitted and creates a 
     * <code>Message</code> object 
     * with a 0-length message. -->
     * 
     * @param type <code>MULTIPART_MESSAGE</code> is the only type permitted.
     * @return A new <code>Message</code> object.
     */
    public Message newMessage(String type) {
        return newMessage(type, null);
    }

    /**
     * Constructs a new <code>MULTIPART_MESSAGE</code> message object with the
     * desired a destination address.
     * <p>
     * <p>
     * The destination address <code>addr</code> has the following format:
     * <code>sms://<em>phone_number</em>:<em>port</em></code>.
     *
     * @param type <code>MULTIPART_MESSAGE</code> is the only type permitted.
     * @param addr The destination address of the message.
     * @return A new <code>Message</code> object.
     */
    public Message newMessage(String type, String addr)  {

        // Return the appropriate type of sub-message.
        if (!(type == MessageConnection.MULTIPART_MESSAGE)) {
            throw new IllegalArgumentException("Message type not supported.");
        }

        return new MultipartObject(addr);
    }

    /**
     * Sends an MMS message.
     *
     * @param msg The MMS <code>Message</code> to be sent.
     * @exception ConnectionNotFoundException  if the address is invalid or if
     *     no address is found in the message.
     * @exception IOException  if an I/O error occurs.
     */
    public void send(Message msg) throws IOException {

        if (msg == null) {
            throw new NullPointerException("Null message.");
        }

        if (!(msg instanceof MultipartMessage)) {
            throw new IllegalArgumentException("Unsupported message type.");
        }

        // Create the multi-part object that will be used below.
        MultipartObject mpo = (MultipartObject)msg;

        /*
         * Check for valid MMS URL connection format. Note that the addresses in
         * the lists are not used. This is simply a check to make sure that the
         * addresses can be placed into the multipart object's header when the
         * header and message are bundled within MultipartObject.
         *
         * Process each MMS address in the to:, cc: and bcc: address lists. An
         * MMS address assumes this form: address:appID
         *
         * Each MMS address is parsed to extract the address and application ID
         * data. Those parts are then checked for validity.
         *
         * The loop starts by processing all addresses in the to: field (if
         * any), followed by the addresses in the cc: list, then the bcc: list.
         *
         */
        Vector allAddresses = new Vector();
        String[] addresses = mpo.getAddresses("to");
        int currIndex = 0;
        boolean checkedTo = false;
        boolean checkedCC = false;

        // The application ID extracted from an address in the address list.
        String parsedAppID = null;
        while (true) {

            /*
             * If no addresses were in the to: field, or if all addresses have
             * been extracted and checked from the current address list
             * (Initially, the to: list), then continue to process the cc: list
             * (if any), next, followed by the bcc: list.
             */
            if (addresses == null || currIndex >= addresses.length) {

                if (!checkedTo) {

                    // The to: list has been processed. Process cc: list, next.
                    checkedTo = true;
                    addresses = mpo.getAddresses("cc");
                    currIndex = 0;
                    continue;

                } else if (!checkedCC) {

                    // The cc: list has been processed. Process bcc: list, next.
                    checkedCC = true;
                    addresses = mpo.getAddresses("bcc");
                    currIndex = 0;
                    continue;
                } else {

                    /*
                     * The to:, cc: and bcc: lists have now been checked, so
                     * bail out of the while() loop.
                     */
                    break;
                }
            }

            /*
             * Pick up the next address and add it to the list. Then, parse it
             * to extract the address and application ID parts.
             */
            String addr = addresses[currIndex++];
            allAddresses.addElement(addr);
                        
            MMSAddress parsedAddress = MMSAddress.getParsedMMSAddress(addr);

            if (parsedAddress == null || 
                parsedAddress.type == MMSAddress.INVALID_ADDRESS ||
                parsedAddress.type == MMSAddress.APP_ID) {
                throw new IllegalArgumentException(
                    "Invalid MMS address: " + addr);
            }

            if (parsedAppID == null) {
                parsedAppID = parsedAddress.appId;
            } else if (parsedAddress.appId != null && 
                !parsedAppID.equals(parsedAddress.appId)) {
                throw new IllegalArgumentException("Only one Application-ID "
                    + "can be specified per message");
            }

        } // while

        if (allAddresses.size() == 0) {
            throw new IllegalArgumentException("No to, cc, or bcc addresses");
        }

        // Construct the target address protocol string. 
        String messageAppID = mpo.getApplicationID();
        String toAddress = "mms://:";
        if (messageAppID != null) {
            toAddress = toAddress + messageAppID;
        }

        /*
         * If no application ID was supplied, use the ID that was used to open
         * the connection as the default ID.
         */
        if (messageAppID != null && host == null) {
            mpo.setReplyToApplicationID(replyToAppID);
        }

        // Preserve the original "from" address.
        String oldFromAddress = ((MultipartObject)mpo).getAddress();

        // Establish the return address.
        String fromAddress = "mms://" + phoneNumber;
        if (replyToAppID != null) {
            fromAddress = fromAddress + ":" + replyToAppID;
        }
        mpo.setFromAddress(fromAddress);

	// Send the message and reply information.
	byte[] header = mpo.getHeaderAsByteArray();
	byte[] body = mpo.getBodyAsByteArray();
	int status =
            send0(dgc, toAddress, fromAddress, messageAppID, replyToAppID,
		  header, body);

        // Restore the "from" address.
        mpo.setFromAddress(oldFromAddress);
    }

    /**
     * Sends the MMS message (Transport-layer-specific code).
     *
     * @param ds The transport-layer-specific datagram socket connection.
     * @param toAddress The recipient's MMS address.
     * @param fromAddress The sender's MMS address.
     * @param appID The application ID to be matched against incoming messages.
     * @param replyToAppID The ID of the application that processes replies.
     * @param mmsHeader The message header context.
     * @param mmsBody The message body context.
     *
     * @return Unused status. Always <code>0</code>.
     */
    private int send0(DatagramSocket ds, String toAddress, String fromAddress,
        String appID, String replyToAppID, byte[] mmsHeader, byte[] mmsBody)
            throws IOException {

        // Combine the header and body parts.
        int headerLen = mmsHeader.length;
        int bodyLen = mmsBody.length;
        byte[] msg = new byte[headerLen + bodyLen];
        System.arraycopy(mmsHeader, 0, msg, 0, headerLen);
        System.arraycopy(mmsBody, 0, msg, headerLen, bodyLen);

        // Use MessagePacket to stream low-endian-formatted values.
        MessagePacket stream = new MessagePacket();
        stream.putString(fromAddress);
        stream.putString(appID);
        stream.putString(replyToAppID);
        stream.putInt(msg.length);
        stream.putBytes(msg);
        byte[] buffer = stream.getData();

        /*
         * Write the message as a series of datagram packets.
         */
        int PACKET_MAX_SIZE = 
            // overhead = three shorts + one int.
            MessageTransportConstants.DATAGRAM_PACKET_SIZE - 10;
        short packetNumber;
        short totalPackets;
        int offset = 0; // offset into buffer.
        int count = 0;

        // The total number of bytes to send.
        int length = buffer.length;

        // The total number of packets to send.
        totalPackets =
            (short)((buffer.length + PACKET_MAX_SIZE - 1) / PACKET_MAX_SIZE);

        // Fragment the data buffer into multiple segments.
        for (packetNumber = 1; packetNumber <= totalPackets;
             packetNumber++) {

            // Datagram envelope for sending messages.
            mess = new DatagramPacket(buf,
                   MessageTransportConstants.DATAGRAM_PACKET_SIZE);
            mess.setAddress(InetAddress.getByName(clientHost));
            mess.setPort(portOut);

            // Compute the number of bytes that can be sent in this packet.
            count = length;
            if (count > PACKET_MAX_SIZE) {
                count = PACKET_MAX_SIZE;
            }

            MessagePacket mmsPacket = new MessagePacket();
            mmsPacket.putShort(packetNumber);
            mmsPacket.putShort(totalPackets);
            mmsPacket.putShort((short)count);
            mmsPacket.putInt(length);

            // Now set the payload.
            byte[] buf = new byte[count];
            System.arraycopy(buffer, offset, buf, 0, count);
            mmsPacket.putBytes(buf);

            byte[] buff = mmsPacket.getData();
            mess.setData(buff, 0, buff.length);
            dgc.send(mess);

            // Move the pointer past the bytes and send the next packet.
            offset += count;
            length -= count;
        }

        return 0;
    }
    
    /**
     * Receives the bytes that have been sent over the connection, 
     * constructs a <code>Message</code> object, and returns it. 
     * <p>
     * If there are no <code>Message</code>s waiting on the connection, 
     * this method will block until a message 
     * is received, or the <code>MessageConnection</code> is closed.
     *
     * @return A <code>Message</code> object
     * @throws IOException if an I/O error occurs.
     */
    public synchronized Message receive() throws IOException {

        // Call the implementation-specific code to get a message byte[]
        byte[] buffer = receive0(dgc);

        // Decode the buffer contents to extract the proper parameters.
        MessagePacket mmsPacket = new MessagePacket(buffer);
        String fromAddress = mmsPacket.getString();
        String appID = mmsPacket.getString();
        String replyToAppID = mmsPacket.getString();
        int msgLen = mmsPacket.getInt();
        byte[] message = mmsPacket.getBytes(msgLen);

        // Convert the data into a multipart object and return that message.
        // MultipartObject mpo = MultipartObject.createFromByteArray(message);
        Message msg = MultipartObject.createFromByteArray(message);

        // IMPL_NOTE: FIX
        // IMPL_NOTE: msg.setTimeStamp(tm.getTimeStamp());

        ((MultipartObject)msg).setFromAddress(fromAddress);
        ((MultipartObject)msg).fixupReceivedMessageAddresses(
            fromAddress, phoneNumber);

        return msg;
    }

    /**
     * Internal implementation of the message-receive code. As packets are
     * received, they are passed off to a routine that assembles the data
     * into a large byte array.
     *
     * @param ds The open datagram socket.
     * @return A <code>Message</code> object.
     * @throws IOException if an I/O error occurs.
     */
    private byte[] receive0(DatagramSocket ds) throws IOException {

        while (true) {

            // Wait for a datagram to arrive.
            ds.receive(mess);

            // Assemble the datagram contents.
            if (assembleFrags(mess) == true) {
                /*
                 * When all datagrams have been received, so break out to allow
                 * the data to be processed.
                 */
                break;
            }
        }

        return mmsBuffer;
    }


    /**
     * Closes the connection. Reset the connection-is-open flag so methods can
     * be checked to throws an appropriate exception for operations on a closed
     * connection.
     *
     * @exception IOException  if an I/O error occurs.
     */
    public void close() throws IOException {

        if (open) {
            dgc.close();
            dgc = null;
            open = false;
        }
    }

    /** The current packet number. */
    private int packetNumber = 1;

    /** The offset into the assembly buffer. */
    private int mmsOffset = 0;

    /** The assembly buffer. */
    private byte[] mmsBuffer = null;

    /**
     * Analyze the special datagram payload, extract the data and append the
     * data to the main byte buffer.
     * <p>
     * This particular implementation assumes that the network has short hops
     * and that datagrams arrive in the order in which they were received.
     * Whenever datagrams arrive out of order, the assembly of packets is
     * cancelled. This simple rule avoids the use of more complex packet
     * assembly algorithms, allowing datagrams to be used instead of sockets.
     * <p>
     * The packet header has this format:
     * <p><pre>
     * +--------+--------------+-----------------+-----------------+
     * | Packet | Total number | Total number of | Total number of |
     * | number |  of packets  | bytes in packet | bytes in stream |
     * +--------+--------------+-----------------+-----------------+
     *  2 bytes      2 bytes         2 bytes           4 bytes</pre>
     * <p>
     * Data are stored in low-endian format. The packet data immediately
     * follow the header.
     *
     * @param packet The special datagram payload.
     * @return <code>true</code> when the last packet has been received and
     *     processed. <code>false</code> when there are more packets that are
     *     expected to be processed.
     */
    private boolean assembleFrags(DatagramPacket packet) {

        // Extract the packet header contents and data.
        MessagePacket mmsPacket = new MessagePacket(packet.getData());
        short packNum = mmsPacket.getShort();
        short totalPackets = mmsPacket.getShort();
        short count = mmsPacket.getShort();
        int totalLen = mmsPacket.getInt();
        byte[] data = mmsPacket.getBytes(count);

        if (packNum != packetNumber) {
            /*
             * Mismatch in packet number. Packets have
             * either arrived out of order or a packet
             * has been dropped.
             */
            System.err.println("ERROR: Datagram packets have been dropped.");
            if (mmsBuffer != null) {
                mmsBuffer = null;
            }
            return false;
        }

        /*
         * If this is the first packet, initialize the total size of the
         * assembly buffer and reset the writing offset.
         */
        if (packNum == 1) {
            mmsBuffer = new byte[totalLen];
            mmsOffset = 0;
        }

        /*
         * Append "count" bytes from the packet to the end of the assembly
         * buffer. The data immediately follow the 10-byte header.
         */
        for (int i = 0; i < count; i++) {
            mmsBuffer[mmsOffset++] = data[i];
        }

        /*
         * If the last packet has been received, reset the expected packet
         * number and the writing offset. The assembly buffer cannot be reset,
         * as it contains the latest assembled data.
         */
        if (packNum == totalPackets) {
            packetNumber = 1;
            mmsOffset = 0;

            // Indicate that all data have been assembled.
            return true;
        }

        // Bump the expected packet number
        packetNumber++;

        // Indicate that more packets are expected.
        return false;
    }

}

