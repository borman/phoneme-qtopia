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

// Classes
import com.sun.midp.i3test.TestCase;
import com.sun.midp.main.Configuration;
import javax.microedition.io.Connector;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.MultipartMessage;

// Exceptions
import java.io.IOException;
import javax.wireless.messaging.SizeExceededException;

/**
 * Tests if an MMS message can sent and received.
 */
public class TestMMSSendReceive extends TestCase implements Runnable {

    /** The fully qualified name of this test. */
    private final String TEST_NAME = this.getClass().getName();


    /** The device ID ("phone number") of the recipient. */
    private final String MMS_TO_DEVICE_ID = "+123456";

    /** The application ID of the recipient. */
    private final String MMS_TO_APP_ID = "com.sun.mmstest";

    /** The MMS address to which the message will be sent. */
    private final String MMS_TO_ADDRESS =
        "mms://" + MMS_TO_DEVICE_ID + ":" + MMS_TO_APP_ID;


    /** The device ID ("phone number") of the sender. */
    private final String MMS_FROM_DEVICE_ID = "+654321";

    /** The application ID of the sender. */
    private final String MMS_FROM_APP_ID = "com.sun.mmssender";

    /** The MMS address from which the message will be sent. */
    private final String MMS_FROM_ADDRESS =
        "mms://" + MMS_FROM_DEVICE_ID + ":" + MMS_FROM_APP_ID;


    /** The MMS client address. */
    private final String MMS_CLIENT_ADDRESS =
        "mms://" + ":" + MMS_TO_APP_ID;

    /** The MMS test message. */
    private final String MMS_CONTENT_MESSAGE = "Test MMS message";

    /** The contents of an MMS message. */
    private final byte[] MMS_CONTENT = MMS_CONTENT_MESSAGE.getBytes();

    /** The MMS connection. */
    private MessageConnection con = null;

    /** Test passed/failed flag. */
    private boolean passed = false;

    /**
     * Set up the physical connection.
     */
    void setUp() {

        try {
            createClientConnection(MMS_CLIENT_ADDRESS);
        } catch (IOException ioe) {
            System.out.println(TEST_NAME + " set-up failed:");
            ioe.printStackTrace();
        }
    }

    /**
     * Create, send and receive a multipart message. This test assumes that the
     * underlying code employs the loopback scheme (i.e., messages are not
     * actually sent or received; however, the messages get added to the pool,
     * which triggers notifications.).
     */
    public void run() {

        try {

            MultipartMessage mm =
                createCompleteMessage(MMS_TO_ADDRESS, MMS_FROM_ADDRESS,
                                      MMS_CONTENT);

            con.send(mm);

            Message m = con.receive();
            if (m instanceof MultipartMessage) {
                checkMultipartMessage((MultipartMessage)m);
            } else {
                System.out.println("Not a MultipartMessage.");
                return;
            }

            closeConnection();

            passed = true;
            assertTrue("Indicate that the test passed.", passed);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Provide clean-up services, following the run of this test.
     */
    void cleanUp() {
        closeConnection();
    }

    /**
     * Create and open an MMS connection.
     *
     * @param clientAddress The MMS address of the client that will receive the
     *      message.
     */
    private void createClientConnection(String clientAddress)
        throws IOException {

        con = (MessageConnection)Connector.open(clientAddress);
    }

    /**
     * Create the MMS message to be sent.
     *
     * @param toAddress The recipient's MMS address.
     * @param fromAddress The sender's MMS address.
     * @param content The payload to be sent.
     *
     * @return The <code>MultipartMessage</code> that was created for the
     *     supplied content.
     */
    private MultipartMessage createCompleteMessage(String toAddress,
        String fromAddress, byte[] content) throws SizeExceededException {

        // The MIME type for this message.
        String mimeType = "mms:";

        // The unique content ID.
        String contentID = "message1";

        // No content location.
        String contentLocation = null;

        // No encoding.
        String encoding = null;

        MessagePart part = new MessagePart(content, 0, content.length,
            mimeType, contentID, contentLocation, encoding);

        MultipartObject mm = new MultipartObject(toAddress);
        mm.addMessagePart(part);

        mm.setFromAddress(fromAddress);

        return mm;
    }

    /**
     * Verify that the contents of the multipart message match the message
     * data that were sent..
     *
     * @param msg A <code>MultipartMessage</code>.
     */
    private void checkMultipartMessage(MultipartMessage msg) {

        // Pick up all message parts.
        MessagePart[] parts = msg.getMessageParts();
        if (parts == null) {
            System.out.println("No message parts.");
            return;
        }

        // Make sure the message part contains the expected content.
        for (int i = 0, n = parts.length; i < n; i++) {
            MessagePart part = parts[i];

            byte[] content = part.getContent();
            if (content == null) {
                System.out.println("No content in message part #" + i);
                return;
            }

            String data = new String(content);
            if (!data.equals(MMS_CONTENT_MESSAGE)) {
                System.out.println("Content message mismatch.");
                return;
            }
        }
    }

    /**
     * Close the MMS connection.
     */
    private void closeConnection() {
        try {
            con.close();
        } catch (IOException ioe) {
            // Fail silently.
        } catch (Exception e) {
            // Fail silently.
        }
    }

    /**
     * Main entry point.
     */
    public void runTests() {
        setUp();

        declare(TEST_NAME);

        run();

        cleanUp();
    }

}

