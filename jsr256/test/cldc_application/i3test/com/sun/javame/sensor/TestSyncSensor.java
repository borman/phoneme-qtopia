/*
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

package com.sun.javame.sensor;

import com.sun.midp.i3test.TestCase;
import javax.microedition.sensor.*;
import javax.microedition.io.*;
import java.io.*;

public class TestSyncSensor extends TestCase {
    
    /**
     * Tests reading a data block with wrong size.
     *
     * @param uri - uri of sensor
     */
    private void testReadWrongLength(String uri) {
        SensorConnection conn = null;
        try {
            conn = (SensorConnection)Connector.open(uri);
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail("Unexpected exception "+ex);
        }
        // try read data with length < 1
        try {
            conn.getData(0);
            fail("No IllegalArgumentException has been thrown");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail("Unexpected exception "+ex);
        }
        // try read data with length > max buffer size
        int maxBuff = ((SensorInfo)conn).getMaxBufferSize();
        try {
            conn.getData(maxBuff + 1);
            fail("No IllegalArgumentException has been thrown");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail("Unexpected exception "+ex);
        }
        try {
            conn.close();
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail("Unexpected exception "+ex);
        }
        // try read data in closed state
        try {
            conn.getData(1);
            fail("No IOException is throwed");
        } catch (IOException ex) {
            assertTrue(true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail("Unexpected exception "+ex);
        }
    }


    /**
     * Fetches data in the synchronous mode.
     *
     * @param uri - uri of sensor
     * @param dataBuff - buffer of data which sends in emulator mode to sensor
     * @param bufferingPeriod - the time to buffer values - given in milliseconds, 
     *  bufferingPeriod < 1 means the period is left undefined
     * @param isTimestampIncluded - if true timestamps should be included in returned Data objects
     * @param isUncertaintyIncluded - if true uncertainties should be included in returned Data objects
     * @param isValidityIncluded - if true validities should be included in returned Data objects
     */
    private void testReadSyncData(String uri, Object[] dataBuff, long bufferingPeriod,
        boolean isTimestampIncluded, boolean isUncertaintyIncluded, boolean isValidityIncluded)
        throws IOException {
        SensorConnection conn = (SensorConnection)Connector.open(uri);
        assertTrue(conn.getState() == SensorConnection.STATE_OPENED);
        TestChannelDevice channelDevice = (TestChannelDevice)(((Sensor)conn).getChannelDevice(0));
        assertTrue(channelDevice != null);
        channelDevice.setTestData(dataBuff);
        Data[] arrData = conn.getData(dataBuff.length, bufferingPeriod, isTimestampIncluded,
            isUncertaintyIncluded, isValidityIncluded);
        assertTrue(arrData != null);
        assertTrue(arrData.length > 0);
        double[] doubleResults = null;
        int[] intResults = null;
        Object[] objResults = null;
        int resultsLength = 0;
        int dataType = conn.getSensorInfo().getChannelInfos()[0].getDataType();
        switch (dataType) {
            case ChannelInfo.TYPE_INT:
                intResults = arrData[0].getIntValues();
                assertTrue(intResults != null);
                resultsLength = intResults.length;
                break;
            case ChannelInfo.TYPE_DOUBLE:
                doubleResults = arrData[0].getDoubleValues();
                assertTrue(doubleResults != null);
                resultsLength = doubleResults.length;
                break;
            case ChannelInfo.TYPE_OBJECT:
                objResults = arrData[0].getObjectValues();
                assertTrue(objResults != null);
                resultsLength = objResults.length;
                break;
        }
        assertTrue(resultsLength == dataBuff.length);
        for (int i = 0; i < resultsLength; i++) {
            switch (dataType) {
                case ChannelInfo.TYPE_INT:
                    assertTrue(intResults[i] == ((Integer)dataBuff[i]).intValue());
                    break;
                case ChannelInfo.TYPE_DOUBLE:
                    assertTrue(doubleResults[i] == ((Double)dataBuff[i]).doubleValue());
                    break;
                case ChannelInfo.TYPE_OBJECT:
                    // IMPL_NOTE - temporary no checking
                    break;
            }
            if (isTimestampIncluded) {
                try {
                    arrData[0].getTimestamp(i);
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            } else {
                try {
                    arrData[0].getTimestamp(i);
                    fail("No IllegalStateException is throwed");
                } catch (IllegalStateException ex) {
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            }
            if (isUncertaintyIncluded) {
                try {
                    arrData[0].getUncertainty(i);
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            } else {
                try {
                    arrData[0].getUncertainty(i);
                    fail("No IllegalStateException is throwed");
                } catch (IllegalStateException ex) {
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            }
            if (isValidityIncluded) {
                try {
                    arrData[0].isValid(i);
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            } else {
                try {
                    arrData[0].isValid(i);
                    fail("No IllegalStateException is throwed");
                } catch (IllegalStateException ex) {
                    assertTrue(true);
                } catch (Throwable ex) {
                    fail("Unexpected exception "+ex);
                }
            }
   
        }
        conn.close();
    }
    
    public void runTests() {
        try {
            SensorTable table = new SensorTable();
            String url;
            for (int i = 0; i < table.size(); i++) {
                declare("Test of reading wrong length data");
                url = table.getUrl(i);
                testReadWrongLength(url);
                declare("ReadData");
                testReadSyncData(url, table.getData(i), 2000, false, false, false);
                testReadSyncData(url, table.getData(i), 2000, true, true, true);
            };
        } catch (Throwable t) {
            fail("" + t);
        }
    }
}
