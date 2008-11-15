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

import com.sun.javame.sensor.helper.SensorUrlGenerator;
import com.sun.midp.i3test.TestCase;
import javax.microedition.sensor.*;

public class TestSensorUrl extends TestCase {
    /* Correct URLs */
    private static final String  URL_1 = "sensor:temperature";
    private static final String  URL_2 = "sensor:temperature;contextType=user";
    private static final String  URL_3 = "sensor:temperature;contextType=user;model=coolSensor";
    private static final String  URL_4 = "sensor:temperature;contextType=user;model=coolSensor;location=kitchen";
    private static final String  URL_5 = "sensor:temperature;model=coolSensor;location=kitchen";
    private static final String  URL_6 = "sensor:temperature;contextType=user;location=kitchen";
    private static final String  URL_7 = "sensor:temperature;contextType=user;model=coolSensor";
    private static final String  URL_8 = "sensor:temperature;model=coolSensor";
    
    /* Incorrect URLs */
    private static final String[]  URL_INCORRECT = {
        "wrong_scheme:",
        "sensor:-no_quantity",
        "sensor:quantity-no_separator",
        "sensor:quantity;", // no header after separator
        "sensor:quantity;wrong", // wrong header after separator
        "sensor:quantity;contextType+", // no "=" after content type
        "sensor:quantity;contextType=wrong", // wrong content type value
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_AMBIENT +
            ";contextType="+SensorInfo.CONTEXT_TYPE_DEVICE, // double content type def
        "sensor:quantity;model+", // no "=" after model
        "sensor:quantity;model=one;model=two", // double module def
        "sensor:quantity;location+", // no "=" after location
        "sensor:quantity;location=one;location=two" // double location def
    };

    /* Correct URLs */
    private static final String[]  URL_CORRECT = {
        "sensor:quantity", // no parameters
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_AMBIENT, // valid content type
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_DEVICE, // valid content type
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_USER, // valid content type
        "sensor:quantity;model=mod1", // valid model
        "sensor:quantity;location=loc1", // valid location
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_AMBIENT+
            ";model=mod1", // content type + model
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_AMBIENT+
            ";location=loc1", // content type + location
        "sensor:quantity;model=mod1;location=loc1", // model + location
        "sensor:quantity;contextType="+SensorInfo.CONTEXT_TYPE_AMBIENT+
            ";model=mod1;location=loc1", // content type + model + location
        "sensor:temperature;model=",
        "sensor:body_fat_percentage;model=1Gf2e427b7nS28F;location="
    };

    /* Push parameters incorrect */
    private static final String[]  URL_PUSH_INCORRECT = {
        "+", // wrong separator (not "?")
        "?nochannel", // no "channel=..."
        "?channel+", // no "=" after channel
        "?channel=+", // wrong channel id
        "?channel=channel1&channel&limit=5.3&op=eq", // no condition list
        "?channel=channel1+aaa", // no separator after channel id
        "?channel=channel1&wrong", // neither "limit" nor "lowerLimit"
        "?channel=channel1&limit+", // no "=" after limit
        "?channel=channel1&limit=ud&op=eq", // no number after "limit="
        "?channel=channel1&limit=.&op=eq", // wrong number format
        "?channel=channel1&limit=-&op=eq", // wrong number format
        "?channel=channel1&limit=5-&op=eq", // wrong number format
        "?channel=channel1&limit=-.&op=eq", // wrong number format
        "?channel=channel1&limit=6.5.4&op=eq", // wrong number format
        "?channel=channel1&limit=5.3&ua=eq", // no "op"
        "?channel=channel1&limit=5.3&op.eq", // no "=" after "op"
        "?channel=channel1&limit=5.3&op=wrong", // wrong op value
        "?channel=channel1&lowerLimit+", // no "=" after "lowerLimit"
        "?channel=channel1&lowerLimit=aaa", // no number after "lowerLimit"
        "?channel=channel1&lowerLimit=4.67+", // no separator
        "?channel=channel1&lowerLimit=4.67&lowerOp+", // no "=" after "lowerOp"
        "?channel=channel1&lowerLimit=4.67&lowerOp=wrong", // wrong lowerOp value
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge+", // no separator
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&wrong", // no upperLimit
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&upperLimit+", // no "=" after "upperLimit"
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&upperLimit=wrong", // no number
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&upperLimit=-56+", // no separator
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&upperLimit=-56&wrong", // no upperOp
        "?channel=channel1&lowerLimit=4.67&lowerOp=ge&upperLimit=-56&upperOp=wrong" // upperOp wrong value
    };

    /* Push parameters correct */
    private static final String[]  URL_PUSH_CORRECT = {
        "?channel=channel1&limit=5.3&op=eq",
        "?channel=channel1&limit=5.3&op=lt",
        "?channel=channel1&limit=5.3&op=le",
        "?channel=channel1&limit=5.3&op=ge",
        "?channel=channel1&limit=5.3&op=gt",
        "?channel=channel1&limit=5.3&op=eq&limit=5.3&op=eq",
        "?channel=channel1&lowerLimit=-4.67&lowerOp=ge&upperLimit=56&upperOp=lt",
        "?channel=channel1&lowerLimit=-4.67&lowerOp=ge&upperLimit=56&upperOp=lt&limit=5.3&op=eq",
        "?channel=channel1&limit=5.3&op=eq&channel=channel1&limit=5.3&op=eq",
        "?channel=channel1&limit=5.3&op=eq&lowerLimit=-4.67&lowerOp=ge&upperLimit=56&upperOp=lt&channel=channel1&limit=5.3&op=eq"
    };

    private void testCorrectURLs() {
        try {
        SensorUrl su = SensorUrl.parseUrl(URL_1);
        assertTrue(su.getQuantity().equals("temperature"));

        su = SensorUrl.parseUrl(URL_2);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType().equals("user"));

        su = SensorUrl.parseUrl(URL_3);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType().equals("user"));
        assertTrue(su.getModel().equals("coolSensor"));

        su = SensorUrl.parseUrl(URL_4);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType().equals("user"));
        assertTrue(su.getModel().equals("coolSensor"));
        assertTrue(su.getLocation().equals("kitchen"));
        
        su = SensorUrl.parseUrl(URL_5);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType() == null);
        assertTrue(su.getModel().equals("coolSensor"));
        assertTrue(su.getLocation().equals("kitchen"));

        su = SensorUrl.parseUrl(URL_6);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType().equals("user"));
        assertTrue(su.getModel() == null);
        assertTrue(su.getLocation().equals("kitchen"));        
        
        su = SensorUrl.parseUrl(URL_7);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType().equals("user"));
        assertTrue(su.getModel().equals("coolSensor"));
        assertTrue(su.getLocation() == null);        

        su = SensorUrl.parseUrl(URL_8);
        assertTrue(su.getQuantity().equals("temperature"));
        assertTrue(su.getContextType() == null);
        assertTrue(su.getModel().equals("coolSensor"));
        assertTrue(su.getLocation() == null);        
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void testCorrectURLs1() {
        for (int i = 0; i < URL_CORRECT.length; i++) {
            try {
                SensorUrl.parseUrl(URL_CORRECT[i]);
                assertTrue(true);
            } catch (Exception ex) {
                assertFalse(true);
            }
        }
    }
    
    private void testIncorrectURLs() {
        for (int i = 0; i < URL_INCORRECT.length; i++) {
            try {
                SensorUrl.parseUrl(URL_INCORRECT[i]);
                assertFalse(true);
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            } catch (Exception ex) {
                assertFalse(true);
            }
        }
    }
    
    private void testIncorrectPushURLs() {
        for (int i = 0; i < URL_CORRECT.length; i++) {
            for (int j = 0; j < URL_PUSH_INCORRECT.length; j++) {
                try {
                    SensorUrl.parseUrlPush(URL_CORRECT[i]+URL_PUSH_INCORRECT[j]);
                    assertFalse(true);
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                } catch (Exception ex) {
                    assertFalse(true);
                }
            }
        }
    }
    
    private void testCorrectPushURLs() {
        for (int i = 0; i < URL_CORRECT.length; i++) {
            for (int j = 0; j < URL_PUSH_CORRECT.length; j++) {
                try {
                    SensorUrl.parseUrlPush(URL_CORRECT[i]+URL_PUSH_CORRECT[j]);
                    assertTrue(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    assertFalse(true);
                }
            }
        }
    }
    
    private void testGeneratedUrl() {
        assertTrue(true);
        for (int i = 0; i < 100000; i++) {
            String url = SensorUrlGenerator.generateSensorUrl();
            try {
                SensorUrl.parseUrl(url);
            } catch (IllegalArgumentException e) {
                System.out.println("Parse failed: " + url);
                throw e;
            }
        }
    }

    private void testMultithreadedParsingUrl(final int threadsCount)
            throws InterruptedException {
        assertTrue(true);
        Thread[] threads = new Thread[threadsCount];
        final CountDownLatch startupLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread() {
                public void run() {
                    try {
                        String url = SensorUrlGenerator.generateSensorUrl();
                        SensorUrl sensorUrl = null;
                        startupLatch.await();
                        try {
                            sensorUrl = SensorUrl.parseUrl(url);
                            assertEquals("Source and parsed URL must be equal",
                                    url, urlToString(sensorUrl));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Parse failed: " + url);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        endLatch.countDown();
                    }
                }
            };
            threads[i].start();
        }
        startupLatch.countDown();
        if (!endLatch.await(10000)) {
            fail("Timed out");
        }
    }

    public void runTests() {
        try {
            declare("testCorrectURLs");
            testCorrectURLs();
            declare("testCorrectURLs1");
            testCorrectURLs1();
            declare("testIncorrectURLs");
            testIncorrectURLs();
            declare("testIncorrectPushURLs");
            testIncorrectPushURLs();
            declare("testCorrectPushURLs");
            testCorrectPushURLs();

            declare("testGeneratedUrl");
            testGeneratedUrl();

            declare("testMultithreadedParsingUrl: 10 threads");
            testMultithreadedParsingUrl(10);
            
            declare("testMultithreadedParsingUrl: 100 threads");
            testMultithreadedParsingUrl(100);
            
            declare("testMultithreadedParsingUrl: 500 threads");
            testMultithreadedParsingUrl(500);
        }
        catch (Throwable t) {
            fail("" + t);
        }        
    }

    private String urlToString(SensorUrl sensorUrl) {
        StringBuffer sb = new StringBuffer();
        sb.append("sensor:");
        sb.append(sensorUrl.getQuantity());

        if (sensorUrl.getContextType() != null) {
            sb.append(";contextType=");
            sb.append(sensorUrl.getContextType());
        }

        if (sensorUrl.getModel() != null) {
            sb.append(";model=");
            sb.append(sensorUrl.getModel());
        }

        if (sensorUrl.getLocation() != null) {
            sb.append(";location=");
            sb.append(sensorUrl.getLocation());
        }
        return sb.toString();
    }
}

class CountDownLatch {

    private int count;

    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.count = count;
    }

    public void await() throws InterruptedException {
        synchronized(this) {
            while (count > 0)
                wait();
        }
    }

    public boolean await(long timeout) throws InterruptedException {
        synchronized (this) {
            if (count <= 0) {
                return true;
            } else if (timeout <= 0) {
                return false;
            } else {
                long deadline = System.currentTimeMillis() + timeout;
                for (;;) {
                    wait(timeout);
                    if (count <= 0) {
                        return true;
                    } else {
                        timeout = deadline - System.currentTimeMillis();
                        if (timeout <= 0) {
                            return false;
                        }
                    }
                }
            }
        }
    }

    public synchronized void countDown() {
        if (count == 0)
            return;
        if (--count == 0)
            notifyAll();
    }

    public long getCount() {
        return count;
    }

    public String toString() {
        return super.toString() + "[Count = " + getCount() + "]";
    }
}
