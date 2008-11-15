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
package com.sun.midp.jsr82emul;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Represents JSR 82 emulation protocol, namely messages and codes recognized 
 * by emulation client and/or server. 
 *
 * It is not a part of JSR 82 implementation and is only used within JSR 82 
 * emulation mode.  The emulation mode allows running tets without real native 
 * Bluetooth libraries or hardware.
 *
 * Emulation server and client communicate thru a socket by means of sending 
 * data packets of the following format: 
 * <table border>
 *   <tr>
 *     <td>1 byte</td>
 *     <td>1 byte</td>
 *     <td>as defined by the previous field</td>
 *   </tr><tr>
 *     <td>packet type code</td>
 *     <td>length of information in bytes</td>
 *     <td>information bytes</td>
 *   </tr>
 * </table>
 */
public final class Messenger {
    /* Keeps encoding for messages. */
    public static final String ENCODING = "ISO8859_1";
    
    /* Responce code that identifies a failure. */
    public static final byte ERROR = -1;
    /* Device registration request code. */
    public static final byte REGISTER_DEVICE = 1;
    /* Successfull registration response. */
    public static final byte REGISTERED = 2;
    
    /* Notification that client does not require server any more. */
    public static final byte DONE = 3;
    /* 
     * Code for specific messages that are only recognized by specific 
     * handlers at server side.
     */
    public static final byte SPECIFIC_MESSAGE = 4;
    
    /* Code for starting advertising service in the ether. */
    public static final byte REGISTER_SERVICE = 5;
    /* Code for unregistering service. */
    public static final byte UNREGISTER_SERVICE = 6;
    /* Request for service connection. */
    public static final byte CONNECT_TO_SERVICE = 7;
    /* Respond that provides service connection details. */
    public static final byte SERVICE_AT = 8;
    
    /* Request for inquiry start. */
    public static final byte START_INQUIRY = 9;
    /* Respond on inquiry completion. */
    public static final byte INQUIRY_COMPLETED = 10;
        
    /* 
     * Command to update device state that includes discoverable mode and
     * device class.
     */
    public static final byte UPDATE_DEVICE_STATE = 11;
    
    /* Keeps code value retrieved by last <code>receive()</code> invocation. */
    private byte code = -1;
    /* Keeps bytes retrieved by last <code>receive()</code> invocation. */
    private byte bytes[] = null;
    
    /* 
     * Constructs an instance. Always use different instances for different 
     * clients/servers to make sure data stored in <code>message</code> and
     * <code>code</code> is appropriate.
     */
    public Messenger() {
    }
    
    /* 
     * Forms a packet with given code and message and sends it to given output
     * stream.
     *
     * @param out the output stream to send to.
     * @param code the code to send.
     * @param message the message to send.
     *
     * @exception IOException if one is issued by <code>out</code> methods.
     */
    public void send(OutputStream out, byte code, String message) 
            throws IOException {
        if (out == null) {
            throw new IllegalArgumentException();
        }
        
        if (message == null) {
            message = "";
        }
        
        byte[] messageBytes = message.getBytes(ENCODING);
        
        sendBytes(out, code, messageBytes);
    }
    
    /* 
     * Forms a packet with given code and bytes that represent given integer, 
     * then sends it to given output stream.
     *
     * @param out the output stream to send to
     * @param code the code to send
     * @param value integer to send
     *
     * @exception IOException if one is issued by <code>out</code> methods.
     */
    public void sendInt(OutputStream out, byte code, int value)
            throws IOException {
            
        byte[] intBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            intBytes[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        
        sendBytes(out, code, intBytes);
    }

    /* 
     * Forms a packet with given code and bytes to send and sends it to 
     * given output stream.
     *
     * @param out the output stream to send to
     * @param code the code to send
     * @param info byte array to be sent entirely
     *
     * @exception IOException if one is issued by <code>out</code> methods.
     */
    public void sendBytes(OutputStream out, byte code, byte[] info)
            throws IOException {
        
        if (info.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        
        synchronized (out) {
            out.write(code);
            out.write((byte)info.length);
            out.write(info);
            out.flush();
        }
    }

    /* 
     * Receives a packet from input stream given saving retrieved data in
     * <code>code</code> and <code>message</code>.
     *
     * @param in the input stream to read from.
     * @exception IOException if one is issued by <code>in</code> methods.
     */
    public void receive(InputStream in) throws IOException {
        code = -1;
        bytes = null;
        
        if (in == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized (in) {
            code = (byte) in.read();
            int length = in.read();
            
            if (length == -1) {
                throw new IOException();
            }
            
            bytes = new byte[length];
            
            if (length != in.read(bytes)) {
                throw new IOException();
            }
        }
    }

    /*
     * Provides code read by the last <code>receive()</code> invocation.
     * @return the code received last time, -1 if there is no valid value.
     */
    public byte getCode() {
        return code;
    }
    
    /*
     * Provides message read by the last <code>receive()</code> invocation.
     * @return string message received last time.
     * @exception EmulationException if no valid value read
     */
    public String getMessage() {
        try {
            return new String(bytes, 0, bytes.length, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new EmulationException();
        }
    }

    /*
     * Retrieves integer represented by bytes read by the last 
     * <code>receive()</code> invocation. Throws IllegalArgument exception
     * if those bytes do not represent an integer.
     *
     * @return integer represented by bytes received last time.
     * @exception EmulationException if no valid value read
     */
    public int getInt() {
        if (bytes == null || bytes.length != 4) {
            throw new EmulationException();
        }
        
        int res = 0;
        for (int i = 3; i >= 0; i--) {
            res = (res << 8) | (bytes[i] & 0xff); 
        }
        
        return res;
    }
    
    /*
     * Provides bytes read by the last <code>receive()</code> invocation.
     * @return bytes received last time.
     */
    public byte[] getBytes() {
        return bytes;
    }
}
