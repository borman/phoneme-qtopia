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
import com.sun.midp.io.j2me.sms.BinaryObject;
import com.sun.midp.io.j2me.sms.TextObject;
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
 * Tests if the number of transport-layer data segments is computed correctly.
 */
public class TestMMSNumberOfSegments extends TestCase implements Runnable {

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

    /** The maximum size of a fragment. */
    private final int MAX_FRAGMENT_SIZE = 1200;

    /** The maximum number of bytes permitted in an MMS message (30k). */
    private final int MAX_TOTAL_SIZE = 30 * 1024;


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
     * Compute transport-layer segment counts for varying message lengths.
     */
    public void run() {

        try {

            // Make sure that an SMS text object can't be used here.
            String smsAddress = "sms://+5551111:1234";
            Message smsMessage = new TextObject(smsAddress);
            int segs = con.numberOfSegments(smsMessage);
            if (segs != 0) {
                throw new IllegalArgumentException(
                    "TextMessage cannot be used in MMS connection " +
                    "(Segments cannot be computed.).");
            }

            // Make sure that an SMS binary object can't be used here.
            smsMessage = new BinaryObject(smsAddress);
            segs = con.numberOfSegments(smsMessage);
            if (segs != 0) {
                throw new IllegalArgumentException(
                    "BinaryMessage cannot be used in MMS connection " +
                    "(Segments cannot be computed.).");
            }

            // Lower bounds test: This should not fail.
            testMessageLengthOf(0);

            // The following message sizes should not fail:
            testMessageLengthOf(1);
            testMessageLengthOf(MAX_FRAGMENT_SIZE - 1);
            testMessageLengthOf(MAX_FRAGMENT_SIZE);
            testMessageLengthOf(MAX_FRAGMENT_SIZE + 1);
            testMessageLengthOf(MAX_TOTAL_SIZE - 1);
            testMessageLengthOf(MAX_TOTAL_SIZE);

            // Upper bounds test: This should fail.
            try {
                testMessageLengthOf(MAX_TOTAL_SIZE + 1);
            } catch (SizeExceededException see) {
                // ignore
            }

            closeConnection();

            passed = true;
            assertTrue("Indicate that the test passed.", passed);

        } catch (Exception e) {

            // Test failure!
            e.printStackTrace();
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
     * Create a message of the given length. Then compute both the actual and
     * test segment counts required to send that message. If there is a
     * difference between the counts, flag this as a problem.
     *
     * @param msgLen The total number of bytes in the message.
     */
    private void testMessageLengthOf(int msgLen)
        throws SizeExceededException, IOException {

        // Create the message content.
        StringBuffer sb = new StringBuffer(msgLen);
        for (int i = 0; i < msgLen; i++) {
            sb.append('x');
        }
        String mmsContent = sb.toString();

        // Create an MMS multipart object and compute its segment count.
        MultipartMessage mm =
            createCompleteMessage(MMS_TO_ADDRESS, MMS_FROM_ADDRESS,
                                  mmsContent.getBytes());

        int segs = con.numberOfSegments(mm);

        // Perform an independent segment computation for comparison.
        byte[] msgBytes = ((MultipartObject)mm).getAsByteArray();
        int compareSegs = numberOfMMSSegments(msgBytes);

        // Bail out if there are discrepancies.
        if (segs < compareSegs) {
            throw new IllegalArgumentException("Actual segment count of " + segs
                + " is lower than comparison count of " + compareSegs + ".");
        } else if (segs > compareSegs) {
            throw new IllegalArgumentException("Actual segment count of " + segs
                + " is higher than comparison count of " + compareSegs + ".");
        }
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
     * Compute the number of segments required to send the message. This will be
     * matched against the number of segments computed by the actual code.
     *
     * @param msgBuffer The payload to be sent.
     *
     * @return The number of transport-layer segments required to send the
     *     message.
     */
    int numberOfMMSSegments(byte msgBuffer[]) {

        /** The maximum fragment size for each segment to be sent. */
        final int FRAGMENT_SIZE = 1200;

        /** The actual fragment size. */
        int fragmentSize = FRAGMENT_SIZE;

        /** Extra header size for concatenated messages. */
        int headerSize = 7;

        /** The number of segments required to send the message. */
        int segments = 0;

        if (msgBuffer == null) {
            return 1;
        }

        int msgLen = msgBuffer.length;
        if (msgLen < FRAGMENT_SIZE) {
            segments = 1;
        } else {
            fragmentSize = fragmentSize - headerSize;
            segments = (msgLen + fragmentSize - 1) / fragmentSize;
        }

        return segments;
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

