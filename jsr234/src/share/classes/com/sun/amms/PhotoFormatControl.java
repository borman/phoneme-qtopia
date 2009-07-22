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


package com.sun.amms;

import com.sun.mmedia.DirectCamera;
import javax.microedition.amms.control.ImageFormatControl;
import javax.microedition.media.MediaException;

public class PhotoFormatControl implements ImageFormatControl {
    
    private DirectCamera _camera;
    private int _quality;

    private static final String [] _formats = { "image/jpeg" };
    private static final String [] _str_pars = { PARAM_VERSION_TYPE };
    private static final String [] _int_pars = { PARAM_QUALITY };
    private static final String [] _jpeg_par_versions = { "JPEG" };
    private static final int [] _quality_range = { 1, 100 };
    
    private static final int _NOT_SUPPORTED = -1;
    private static final int _UNKNOWN = -2;
    
    private static int _defaultQuality = _UNKNOWN;
    
    private static int getDefaultQuality( String encodings )
    {
       int q = 100;

       if( 0 == encodings.indexOf( "encoding=jpeg" ) )
       {
           int pos;
           if( 0 > ( pos = encodings.indexOf( ' ' ) ) )
           {
               pos = encodings.length();
           }

           String defaultEnc = encodings.substring( 0, pos );
           pos = defaultEnc.indexOf( "&quality=" );

           if( pos >= 0)
           {
               int end_pos;
               if( 0 > ( end_pos = 
                       defaultEnc.indexOf( '&', pos + 1 ) ) )
               {
                   end_pos = defaultEnc.length();
               }
               String defQ = defaultEnc.substring( 
                       pos + "&quality=".length(), end_pos );
               try
               {
                    q = Byte.parseByte( defQ );
                    q = 
                        q < _quality_range[0] ? 
                            _quality_range[0] : 
                            ( q > _quality_range[1] ? 
                                _quality_range[1] : 
                                q ) ;
               }
               catch( NumberFormatException e ) 
               {
                   q = 100;
               }
           }
       }
       return q;
    }
    
    private static boolean isSupported()
    {
        //already checked, not supported
        if ( _NOT_SUPPORTED == _defaultQuality )
        {
            return false;
        }
        //already checked, supported
        if( _UNKNOWN != _defaultQuality )
        {
            return true;
        }
        
        String photoEncodings = System.getProperty("video.snapshot.encodings");
        if( null != photoEncodings &&  
                 photoEncodings.indexOf( "encoding=jpeg" ) >= 0 && 
                 photoEncodings.indexOf( "&quality=" ) >= 0 )
        {
            _defaultQuality = getDefaultQuality( photoEncodings );
            return true;
        }
        
        _defaultQuality = _NOT_SUPPORTED;
        return false;
    }

    public static PhotoFormatControl createInstance( DirectCamera cam )
    {
        PhotoFormatControl inst = null;
        if ( isSupported() )
        {
            inst = new PhotoFormatControl( cam );
        }
        return inst;
    }
    
    private PhotoFormatControl( DirectCamera cam ) {
        _camera = cam;
        _quality = _defaultQuality;
        _camera.setSnapshotQuality( _quality );
    }

    public int getEstimatedImageSize() {
        return 0;
    }

    public String[] getSupportedFormats() {
        return _formats;
    }

    public String[] getSupportedStrParameters() {
        return _str_pars;
    }

    public String[] getSupportedIntParameters() {
        return _int_pars;
    }

    public String[] getSupportedStrParameterValues(String parameter) {
        checkStrParamName( parameter );
        return _jpeg_par_versions;
    }

    public int[] getSupportedIntParameterRange(String parameter) {
        checkIntParamName( parameter );
        return _quality_range;
    }

    public void setFormat(String format) {
        if( null == format )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl format is null" );
        }
        if( !format.equals( _formats[0] ) )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl format is not supported" );
        }
    }

    public String getFormat() {
        return _formats[0];
    }

    public int setParameter(String parameter, int value) {
        checkIntParamName( parameter );
        if( value < _quality_range[0] || value > _quality_range[1] )
        {
            throw new IllegalArgumentException(
                    "ImageFormatControl Int parameter value is out of range" );
        }
        _camera.setSnapshotQuality( value );
        _quality = value;
        return _quality;
    }

    private void checkStrParamName( String parameter )
    {
        if( null == parameter )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl String parameter is null" );
        }
        if( !parameter.equals( _str_pars[0] ) )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl String parameter is not supported" );
        }
    }
    public void setParameter(String parameter, String value) {
        checkStrParamName( parameter );
        if( !value.equals( _jpeg_par_versions[0] ) )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl String parameter value is not supported" );
        }
    }

    public String getStrParameterValue(String parameter) {
        checkStrParamName( parameter );
        return _jpeg_par_versions[0];
    }

    private void checkIntParamName( String parameter )
    {
        if( null == parameter )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl Int parameter is null" );
        }
        if( !parameter.equals( _int_pars[0] ) )
        {
            throw new IllegalArgumentException( 
                    "ImageFormatControl Int parameter is not supported" );
        }
    }
    public int getIntParameterValue(String parameter) {
        checkIntParamName( parameter );
        return _quality;
    }

    public int getEstimatedBitRate() throws MediaException {
        throw new MediaException( "Bit Rate estimation is not supported for ImageFormatControl" );
    }

    public void setMetadata(String key, String value) throws MediaException {
        throw new MediaException( "Metadata setting is not supported" );
    }

    public String[] getSupportedMetadataKeys() {
        return new String [0];
    }

    public int getMetadataSupportMode() {
        return METADATA_NOT_SUPPORTED;
    }

    public void setMetadataOverride(boolean override) {
    }

    public boolean getMetadataOverride() {
        return false;
    }
    
}
