/*
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

package com.sun.amms.directcontrol;
import com.sun.amms.AMMSMPEventListener;
import javax.microedition.media.MediaException;
import javax.microedition.amms.control.tuner.*;
import java.util.Date;


public class DirectRDSControl implements RDSControl {
    private int _nativeHandle;
    private AMMSMPEventListener _mp_listener;
    
    public static DirectRDSControl createInstance( int hNative )
    {
        if( !nIsSupported( hNative ) )
        {
            System.out.println("RDSControl not supported from Native!");
            return null;
        }
        return new DirectRDSControl( hNative );
    }
    
    private DirectRDSControl( int hNative )
    {
        _nativeHandle = hNative;
        _mp_listener = AMMSMPEventListener.getInstance();
    }

    /**
     * Returns the status of the RDS reception.
     *
     * @return True if RDS signal can be recepted, false otherwise.
     */
    public boolean isRDSSignal() {
        return nIsRDSSignal( _nativeHandle );
    }

    /**
     * Gets the current Programme Service name.
     *
     * @return Name of the Programme Service or a zero-length String if
     * unknown.
     */
    public String getPS() {
        return nGetPS( _nativeHandle );
    }


    /**
     * Gets the current Radio Text.
     *
     * @return Radio Text or zero-length String if unknown.
     */
    public String getRT() {
        return nGetRT( _nativeHandle );
    }


    /**
     * Gets the current Programme TYpe as short. The return value zero
     * corresponds to No Programme Type or to undefined type.
     *
     * <p>Please note that PTYs in RBDS differ from the ones in RDS.</p>
     *
     * @return Programme TYpe or zero for undefined type.
     */
    public short getPTY() {
        return nGetPTY( _nativeHandle );
    }


    /**
     * Gets the current Programme TYpe as a String with the maximum
     * of 8 or 16 characters in English.
     *
     * <p>Please note that PTYs in RBDS differ from the ones in RDS.</p>
     *
     * @param longer true = the maximum lenght is 16,
     *              false = the maximum lenght is 8.
     *
     * @return Programme TYpe or "None" for an undefined type.
     */
    public String getPTYString(boolean longer) {
        return nGetPTYString( _nativeHandle, longer );
    }


    /**
     * Gets the current Programme Identification code.
     *
     * @return Programme Identification code or zero for an undefined PI code.
     */
    public short getPI() {
        return nGetPI( _nativeHandle );
    }


    /**
     * Gets the frequencies sending the given Programme TYpe.
     * Based on the EON field.
     * The alternate frequencies will not be returned.
     *
     * @return frequencies of programs in 100 Hertzs or null if none are found.
     */
    public int[] getFreqsByPTY(short PTY) {
        int[] freqs = null;
        int n = nGetNumFreqsByPTY( _nativeHandle, PTY );
        if( n > 0 )
        {
            freqs = new int[ n ];
            for( int i = 0;  i < n; i++ )
            {
                freqs[ i ] = nGetFreqByPTY( _nativeHandle, PTY, i );
            }
        }
        return freqs;
    }


    /**
     * Gets the frequencies sending Traffic Anouncements.
     * Based on EON field.
     *
     * @param TA true = get TAs, false = get no TAs
     *
     * @return Matrix of Programmes * Alternative Frequencies in 100 Hertzs
     * or null if none is found
     */
    public int[][] getFreqsByTA(boolean TA) {
        int[][] freqs = null;
        int[] dim = new int[ 2 ];
        
        nGetFreqsByTADimensions( _nativeHandle, TA, dim );
        
        int rows = dim[0];
        int cols = dim[1];
        
        if( rows > 0 && cols > 0 )
        {
            freqs = new int [ rows ] [ cols ];
            for( int i = 0; i < rows; i++ )
            {
                for( int j = 0; j < cols; j++ )
                {
                    freqs[ i ][ j ] = nGetFreqsByTAElement( _nativeHandle, TA,
                            i, j );
                }
            }
        }
        
        return freqs;
    }


    /**
     * Gets the Programme Service names (PS) sending the given Programme TYpe.
     * Based on the EON field.
     *
     * @return Programme Service names or null if none are found.
     */
    public String[] getPSByPTY(short PTY) {
        String[] PS = null;
        int n = nGetNumPSByPTY( _nativeHandle, PTY );
        if( n > 0 )
        {
            PS = new String[ n ];
            for( int i = 0; i < n; i++ )
            {
                PS[ i ] = nGetPSByPTY( _nativeHandle, PTY, i );
            }
        }
        return null;
    }


    /**
     * Gets the Programme Service names (PS) sending Traffic Anouncements.
     * Based on the EON field.
     *
     * @param TA true = get TAs, false = get no TAs
     *
     * @return Programme Service names or null if none are found.
     */
    public String[] getPSByTA(boolean TA) {
        String[] PS = null;
        int n = nGetNumPSByTA( _nativeHandle, TA );
        if( n > 0 )
        {
            PS = new String[ n ];
            for( int i = 0; i < n; i++ )
            {
                PS[ i ] = nGetPSByTA( _nativeHandle, TA, i );
            }
        }
        return null;
    }


    /**
     * Gets the current Clock Time and date (CT).
     *
     * @return current time and date or null if unknown.
     */
    public Date getCT() {
        return new Date( System.currentTimeMillis() );
    }


    /**
     * Gets the current status of the Traffic Anouncement (TA) switch.
     *
     * @return true = TA, false = no TA.
     */
    public boolean getTA() {
        return nGetTA( _nativeHandle );
    }


    /**
     * Gets the current status of the Traffic Programme (TP) switch.
     *
     * @return true = TP, false = no TP.
     */
    public boolean getTP() {
        return nGetTP( _nativeHandle );
    }


    /**
     * Gets the current Traffic Message Channel's (TMC) message.
     *
     * @return TBD based on CEN standard ENV 12313-1
     */
    //Object getTMC()

    /**
     * Sets the automatic switching of the transmitter in the case of
     * a stronger transmitter with the same PI presence.
     * Based on AF and/or EON fields.
     *
     * @throws MediaException If setting a value is not supported.
     */
    public void setAutomaticSwitching(boolean automatic)
    throws MediaException {
        nSetAutomaticSwitching( _nativeHandle, automatic );
    }


    /**
     * Gets the mode of the automatic switching of the transmitter
     * in case of
     * a stronger transmitter with the same PI presence.
     */
    public boolean getAutomaticSwitching() {
        return nGetAutomaticSwitching( _nativeHandle );
    }


    /**
     * Sets the automatic switching of the program in case of
     * the presence of Traffic Anouncement in another program.
     * Based on TP and TA fields.
     *
     * @throws MediaException If setting the given value is not supported.
     */
    public void setAutomaticTA(boolean automatic) throws MediaException {
        nSetAutomaticTA( _nativeHandle, automatic );
    }


    /**
     * Gets the mode of the automatic switching of the program in case of
     * the presence of Traffic Anouncement in another program.
     * Based on TP and TA fields.
     */
    public boolean getAutomaticTA() {
        return nGetAutomaticTA( _nativeHandle );
    }


    protected native boolean nIsRDSSignal(int hNative);
    protected native String nGetPS(int hNative);
    protected native String nGetRT(int hNative);
    protected native short nGetPTY(int hNative);
    protected native String nGetPTYString(int hNative, boolean longer);
    protected native short nGetPI(int hNative);
    protected native int nGetNumFreqsByPTY(int hNative, short PTY);
    protected native int nGetFreqByPTY(int hNative, short PTY, int index);
    protected native void nGetFreqsByTADimensions(int hNative, boolean TA,
            /*OUT*/int[] dims);
    protected native int nGetFreqsByTAElement(int hNative, boolean TA,
            int i, int j);
    protected native int nGetNumPSByPTY(int hNative, short PTY);
    protected native String nGetPSByPTY(int hNative, short PTY, int index);
    protected native int nGetNumPSByTA(int hNative, boolean TA);
    protected native String nGetPSByTA(int hNative, boolean TA, int index);
    protected native boolean nGetTA(int hNative);
    protected native boolean nGetTP(int hNative);
    protected native void nSetAutomaticSwitching(int hNative, boolean automatic);
    protected native boolean nGetAutomaticSwitching(int hNative);
    protected native void nSetAutomaticTA(int hNative, boolean automatic);
    protected native boolean nGetAutomaticTA(int hNative);
    protected static native boolean nIsSupported( int hNative );

}
