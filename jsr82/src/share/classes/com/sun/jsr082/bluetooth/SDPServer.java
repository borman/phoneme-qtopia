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
package com.sun.jsr082.bluetooth;
import javax.bluetooth.DataElement;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.L2CAPConnectionNotifier;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

/*
 * Represents Servive Discovery Protocol Server.
 */
public class SDPServer {

    /* Set to false in RR version - then the javac skip the code. */
    private static final boolean DEBUG = false;

    /*
     *  notifier - a connection used by the server
     * to wait for a client connection
     */
    private L2CAPConnectionNotifier conNotifier = null;

    /* Collects connections to the server. */
    private Vector connections;

    /* Requests acceptor, it is Runnable and works in its own thread. */
    private Acceptor acceptor;

    /* Shows if acceptor thread is running. */
    private boolean acceptorStarted = false;

    /* SDP UUID. */
    private static final int SDP_UUID = 0x0001;

    /* SDP_ErrorResponse PDU ID. */
    private static final int SDP_ERROR_RESPONSE = 0x01;

    /* SDP_ServiceSearchRequest PDU ID. */
    private static final int SDP_SERVICE_SEARCH_REQUEST = 0x02;

    /* SDP_ServiceSearchResponse PDU ID. */
    private static final int SDP_SERVICE_SEARCH_RESPONSE = 0x03;

    /* SDP_ServiceAttributeRequest PDU ID. */
    private static final int SDP_SERVICE_ATTRIBUTE_REQUEST = 0x04;

    /* SDP_ServiceAttributeResponse PDU ID. */
    private static final int SDP_SERVICE_ATTRIBUTE_RESPONSE = 0x05;

    /* SDP_ServiceSearchAttributeResponse PDU ID. */
    private static final int SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST = 0x06;

    /* SDP_ServiceSearchAttributeResponse PDU ID. */
    private static final int SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE = 0x07;

    /* Error code for SDP_ErrorResponse: Invalid/unsupported SDP version. */
    private static final int SDP_INVALID_VERSION = 0x01;

    /* Error code for SDP_ErrorResponse: Invalid Service Record Handle. */
    private static final int SDP_INVALID_SR_HANDLE = 0x02;

    /* Error code for SDP_ErrorResponse: Invalid request syntax. */
    private static final int SDP_INVALID_SYNTAX = 0x03;

    /*
     * Note: The following constants aren't used by emulator
     * but can be used in real device
     * private static final int SDP_INVALID_PDU_SIZE = 0x04;
     * private static final int SDP_INVALID_CONTINUATION_STATE = 0x05;
     * private static final int SDP_INSUFFICIENT_RESOURCES = 0x06;
     */

    /*
     * Constructs <code>SDPServer</code> instance.
     */
    public SDPServer() {
        connections = new Vector();
        acceptor = new Acceptor();
    }

    /*
     * Starts this SDP server if not started.
     */
    public synchronized void start() {
        if (acceptorStarted) {
            return;
        }

        requestPSM();
        UUID sdpUUID = new UUID(SDP_UUID);
        try {
            conNotifier = (L2CAPConnectionNotifier)
                    SDP.getL2CAPConnection("//localhost:"
                    + SDP.UUID + ";name=SDPServer");
        } catch (IOException ioe) {
            // ignore
        }

        if (conNotifier != null) {
            acceptorStarted = true;
            (new Thread(acceptor)).start();
        }
    }

    /*
     * Notifies native emulation code that next PSM requested
     * is for SDP server.
     */
    private native void requestPSM();

    /*
     * Stops this server closing all the connections to it.
     */
    synchronized void stop() {
        try {
            conNotifier.close();
        } catch (IOException ioe) {
            // ignore
        }

        for (int i = connections.size(); i >= 0; i--) {
            L2CAPConnection con = (L2CAPConnection) connections.elementAt(i);

            try {
                synchronized (con) {
                    con.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        connections.removeAllElements();
    }

    /*
     * Retrieves next PDU from a connection and processes it.
     *
     * @param rw <code>DataL2CAPReaderWriter</code> instance that represents
     *        desired connection and provides RW utilities.
     *
     * @throws IOException if a processing error occured.
     */
    private void processRequest(DataL2CAPReaderWriter rw)
            throws IOException {
        byte requestType = rw.readByte();
        short transactionID = rw.readShort();
        short length = rw.readShort();

        if (requestType == SDP_SERVICE_SEARCH_REQUEST) {
            processServiceSearch(rw, transactionID);
        } else if (requestType == SDP_SERVICE_ATTRIBUTE_REQUEST) {
            processServiceAttribute(rw, transactionID);
        } else if (requestType == SDP_SERVICE_SEARCH_ATTRIBUTE_REQUEST) {
            processServiceSearchAttribute(rw, transactionID);
        } else {
            writeErrorResponce(rw, transactionID, SDP_INVALID_SYNTAX,
                    "Invalid Type of Request");
            System.err.println("WARNING: Unsupported SDP request");
        }
    }

    /*
     * Retrieves SDP_ServiceSearchRequest parameters from given connection,
     * processes the requests and sends a respond.
     *
     * @param rw <code>DataL2CAPReaderWriter</code> instance that represents
     *        desired connection and provides RW utilities.
     * @param transactionID ID of transaction the request is recieved in.
     * @throws IOException if a processing error occured.
     */
    private void processServiceSearch(DataL2CAPReaderWriter rw,
            short transactionID) throws IOException {
        DataElement uuidSet = rw.readDataElement();
        short maximimSRCount = rw.readShort();

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        int continuationState = rw.readByte();

        if (continuationState != 0) {
            writeErrorResponce(rw, transactionID, SDP_INVALID_VERSION,
                    "Current implementation don't support continuation state");
            return;
        }

        Vector currentHandles = new Vector();
        int[] handles = SDDB.getInstance().getHandles();
        for (int i = 0; i < handles.length; i++) {
            ServiceRecord sr = SDDB.getInstance().getServiceRecord(handles[i]);

            if (findUUIDs((ServiceRecordImpl) sr, uuidSet)) {
                currentHandles.addElement(new Integer(handles[i]));
            }
        }
        rw.writeByte((byte) SDP_SERVICE_SEARCH_RESPONSE);
        rw.writeShort(transactionID);
        rw.writeShort((short) (currentHandles.size() + 5));

        // Total and current counts are the same for all the results are
        // sent in one response PDU
        rw.writeShort((short) currentHandles.size());
        rw.writeShort((short) currentHandles.size());

        for (int i = 0; i < currentHandles.size(); i++) {
            if (i > maximimSRCount) {
                break;
            }
            int h = ((Integer) currentHandles.elementAt(i)).intValue();
            rw.writeInteger(h);
        }

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        rw.writeByte((byte) 0x00);
        rw.flush();
    }

    /*
     * Retrieves SDP_ServiceAttribute parameters from given connection,
     * processes the requests and sends a respond.
     *
     * @param rw <code>DataL2CAPReaderWriter</code> instance that represents
     *        desired connection and provides RW utilities.
     * @param transactionID ID of transaction the request is recieved in.
     *
     * @throws IOException if a processing error occured.
     */
    private void processServiceAttribute(DataL2CAPReaderWriter rw,
            short transactionID) throws IOException {
        int handle = rw.readInteger();

        // IMPL_NOTE: Add checking for real device
        short maximimSize = rw.readShort();
        DataElement attrSet = rw.readDataElement();

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        int continuationState = rw.readByte();

        if (continuationState != 0) {
            writeErrorResponce(rw, transactionID, SDP_INVALID_VERSION,
                    "Current implementation don't support continuation state");
            return;
        }

        ServiceRecord sr = SDDB.getInstance().getServiceRecord(handle);

        // if service record not found process it
        if (sr == null) {
            writeErrorResponce(rw, transactionID, SDP_INVALID_SR_HANDLE,
                    "Servicce Record with specified ID not found");
            return;
        }

        DataElement attrIDValues = new DataElement(DataElement.DATSEQ);
        Enumeration e = (Enumeration) attrSet.getValue();

        while (e.hasMoreElements()) {
            DataElement attrID = (DataElement) e.nextElement();
            int attr = (int) attrID.getLong();
            DataElement attrValue = sr.getAttributeValue(attr);

            if (attrValue != null) {
                attrIDValues.addElement(attrID);
                attrIDValues.addElement(attrValue);
            }
        }
        int length = (int) rw.getDataSize(attrIDValues);
        rw.writeByte((byte) SDP_SERVICE_ATTRIBUTE_RESPONSE);
        rw.writeShort(transactionID);
        rw.writeShort((short) (length + 3));
        rw.writeShort((short) length);
        rw.writeDataElement(attrIDValues);

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        rw.writeByte((byte) 0x00);
        rw.flush();
    }

    /*
     * Retrieves SDP_ServiceSearchAttribute parameters from given connection,
     * processes the requests and sends a respond.
     *
     * @param rw <code>DataL2CAPReaderWriter</code> instance that represents
     *        desired connection and provides RW utilities.
     * @param transactionID ID of transaction the request is recieved in.
     *
     * @throws IOException if a processing error occured.
     */
    private void processServiceSearchAttribute(DataL2CAPReaderWriter rw,
            short transactionID) throws IOException {
        DataElement uuidSet = rw.readDataElement();

        // IMPL_NOTE: Add checking for real device
        short maximimSize = rw.readShort();
        DataElement attrSet = rw.readDataElement();

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        int continuationState = rw.readByte();

        if (continuationState != 0) {
            writeErrorResponce(rw, transactionID, SDP_INVALID_VERSION,
                    "Current implementation don't support continuation state");
            return;
        }

        int[] handles = SDDB.getInstance().getHandles();
        int handle = -1;

        for (int i = 0; i < handles.length; i++) {
            ServiceRecord sr = SDDB.getInstance().getServiceRecord(handles[i]);

            if (findUUIDs((ServiceRecordImpl) sr, uuidSet)) {
                handle = handles[i];
            }
        }
        ServiceRecord sr = SDDB.getInstance().getServiceRecord(handle);

        // if service record not found process it
        if (sr == null) {
            writeErrorResponce(rw, transactionID, SDP_INVALID_SR_HANDLE,
                    "Servicce Record with specified ID not found");
            return;
        }

        DataElement attributeLists = new DataElement(DataElement.DATSEQ);
        DataElement attrIDValues = new DataElement(DataElement.DATSEQ);
        Enumeration e = (Enumeration) attrSet.getValue();

        while (e.hasMoreElements()) {
            DataElement attrID = (DataElement) e.nextElement();
            int attr = (int) attrID.getLong();
            DataElement attrValue = sr.getAttributeValue(attr);

            if (attrValue != null) {
                attrIDValues.addElement(attrID);
                attrIDValues.addElement(attrValue);
            }
        }

        attributeLists.addElement(attrIDValues);
        int length = (int) rw.getDataSize(attributeLists);

        rw.writeByte((byte) SDP_SERVICE_SEARCH_ATTRIBUTE_RESPONSE);
        rw.writeShort(transactionID);
        rw.writeShort((short) (length + 3));
        rw.writeShort((short) length);
        rw.writeDataElement(attributeLists);

        // IMPL_NOTE: ContinuationState isn't used, but should on real device
        rw.writeByte((byte) 0x00);
        rw.flush();
    }

    /*
     * Sends SDP_ErrorResponse PDU.
     *
     * @param rw <code>DataL2CAPReaderWriter</code> instance that represents
     *        connection to send to and provides RW utilities.
     * @param transactionID ID of transaction to send response within.
     * @param errorCode error code.
     * @param info error details.
     *
     * @throws IOException if a processing error occured.
     */
    private void writeErrorResponce(DataL2CAPReaderWriter rw,
            short transactionID, int errorCode, String info)
            throws IOException  {
        byte[] infoBytes = info.getBytes();
        int length = infoBytes.length + 2;
        rw.writeByte((byte) SDP_ERROR_RESPONSE);
        rw.writeShort(transactionID);
        rw.writeShort((short) length);
        rw.writeShort((short) errorCode);
        rw.writeBytes(infoBytes);
        rw.flush();
    }

    /*
     * Checks if the specified service record contains all of the
     * UUID with values from specified 'uuids' list.
     *
     * Note, that according to spec clarification from spec lead
     * such a search is done over all of the service attribues
     * (not just ServiceClassIDList and ProtocolDescriptorList).
     *
     * @param sr service record to check.
     * @param uuids list of UUIDs to check record for.
     *
     * @return true if the record given contains all the UUIDs, false
     * otherwise.
     */
    private boolean findUUIDs(ServiceRecordImpl sr, DataElement uuids) {
        int[] attrs = sr.getAttributeIDs();
        Enumeration e = (Enumeration) uuids.getValue();

    NEXT_UUID:
        while (e.hasMoreElements()) {
            UUID uuid = (UUID) ((DataElement) e.nextElement()).getValue();
            for (int i = 0; i < attrs.length; i++) {
                DataElement attr = sr.getAttributeValue(attrs[i]);

                if (containsUUID(attr, uuid)) {
                    continue NEXT_UUID;
                }
            }
            return false;
        }
        return true;
    }

    /*
     * Checks if specified 'attr' contains (or equals) to specified 'uuid.
     *
     * @param attr data element to check if it equals to desired UUID or
     *        contains it.
     * @param uuid the UUID to compare with.
     *
     * @return true if data element given represents either specified UUID
     * or a sequence that contains it, false otherwise.
     */
    private boolean containsUUID(DataElement attr, UUID uuid) {
        if (attr.getDataType() == DataElement.UUID) {
            return uuid.equals((UUID) attr.getValue());
        }

        if (attr.getDataType() != DataElement.DATSEQ) {
            return false;
        }
        Enumeration e = (Enumeration) attr.getValue();

        while (e.hasMoreElements()) {
            DataElement de = (DataElement) e.nextElement();

            if (containsUUID(de, uuid)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Requests acceptor, it is Runnable to be launched in a separate thread.
     */
    class Acceptor implements Runnable {
        /*
         * The <code>run()</code> method, see interface
         * {@link java.lang.Runnable Runnable}.
         */
        public void run() {
            while (true) {
                try {
                    L2CAPConnection con = conNotifier.acceptAndOpen();
                    DataL2CAPReaderWriter rw = new DataL2CAPReaderWriter(con);

                    synchronized (SDPServer.this) {
                        connections.addElement(con);
                    }
                    Sender sender = new Sender(con, rw);
                    new Thread(sender).start();
                } catch (IOException ioe) {
                    if (DEBUG) {
                        ioe.printStackTrace();
                    }
                    // connection was closed
                    break;
                }
            }

            synchronized (SDPServer.this) {
                acceptorStarted = false;
                try {
                    conNotifier.close();
                } catch (IOException e) {
                    // no matter
                }
                conNotifier = null;
            }
        }

        /* Cleans possible blockings and extra references up. */
        private void finalize() {
            if (conNotifier != null) {
                acceptorStarted = false;
                try {
                    conNotifier.close();
                } catch (IOException e) {
                    // no matter
                }
                conNotifier = null;
            }
        }
    }

    /*
     * Responses sender, it is Runnable to be launched in a separate thread.
     */
    class Sender implements Runnable {
        /* Connection to send to. */
        L2CAPConnection connection;

        /* Utility object to write to L2CAP connection. */
        DataL2CAPReaderWriter readerWriter;

        /*
         * Constructs sender to send that sends to given connection using
         * specified writer.
         *
         * @param connection L2CAP connection to send to.
         * @param readerWriter read/write utility object to use for sending.
         */
        Sender(L2CAPConnection connection,
                DataL2CAPReaderWriter readerWriter) {
            this.connection = connection;
            this.readerWriter = readerWriter;
        }

        /*
         * The <code>run()</code> method, see interface
         * {@link java.lang.Runnable Runnable}.
         */
        public void run() {
            while (true) {
                try {

                    /*
                     * If this call returns sucessfully, the
                     * next request will be processed (in the
                     * next 'while' loop). IOException means
                     * the remote end-point is closed - no
                     * more requests to be processed.
                     */
                    processRequest(readerWriter);
                } catch (IOException ioe) {

                    // this means the process is done
                    if (DEBUG) {
                        ioe.printStackTrace();
                    }

                    synchronized (SDPServer.this) {
                        connections.removeElement(connection);
                    }

                    try {
                        connection.close();
                    } catch (IOException ioe1) {}
                    break;
                }

                // process is done successfully - go to next one
            }
        }
    }
}
