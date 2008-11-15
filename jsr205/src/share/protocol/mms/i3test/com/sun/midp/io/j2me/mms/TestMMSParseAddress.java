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
import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;

// Exceptions
import javax.microedition.io.ConnectionNotFoundException;

/**
 * Tries various hard-coded addresses to see if they can be parsed.
 * <p>
 * This is a conversion of the JDTS test case, MMSSyntax.java, into I3 test
 * form. The source originated from JDTS 1.4 Update 7.
 * <p>
 * Notes: Test "testEmail22" is disabled at this time, pending an investigation
 * as to whether the JDTS test is in error if if a real code failure is
 * occurring.
 * <p>
 * The test name can eventually be omitted, since it is never used. This can
 * be changed on another pass.
 */
public class TestMMSParseAddress extends TestCase {

    /** The fully qualified name of this test. */
    private final String TEST_NAME = this.getClass().getName();

    /**
     * No set-up at this time.
     */
    void setUp() { }

    /**
     * Test various MMS addresses to see if they can be parsed.
     */
    public void run() throws Throwable {

        // e-mail
        assertValid("testEmail1", "mms://john@yahoo");
        assertValid("testEmail2", "mms://\"john smith\"@yahoo.com");
        assertValid("testEmail3", "mms://\"john smith\".john@yahoo.com");
       
        assertValid("testEmail4", "mms://\"John\".\" Smith\"@yahoo.com");
       
        assertValid("testEmail5", "mms://john@yahoo.com");
        assertValid("testEmail6", "mms://john.smith@yahoo.com");
        assertValid("testEmail7", "mms://\"John\"<john@yahoo.com>");
        assertValid("testEmail8", "mms://\"Joh\\tn\"<john@yahoo.com>");

        assertValid("testEmail8", "mms://\"Joh\\t\\nn\"<john@yahoo.com>");

        assertValid("testEmail9", "mms://\"\\t\\nJohn\"<john@yahoo.com>");
       
        assertValid("testEmail10", "mms://\"\"<john@yahoo.com>");
        assertValid("testEmail12", "mms://john@[yahoo]");
        assertValid("testEmail13", "mms://john@[yahoo_]");
        assertValid("testEmail14", "mms://john@[yahoo\\t]");
        assertValid("testEmail15", "mms://john@[]");
        assertValid("testEmail16", "mms://\"john\"<@domain:john@yahoo>");

        assertValid("testEmail18",
                "mms://\"john\"<@domain,@[domain]:john@yahoo>");
        assertValid("testEmail19", "mms://group:john@yahoo;");
        assertValid("testEmail20", "mms://group:;");
        assertValid("testEmail21", "mms://group:john@yahoo,john@yahoo;");

        /*
         * FIX: Should this really be assertValid?
         * assertInvalid("testEmail22",
         *        "mms://\"john\"<@domain,,@domain:john@yahoo>");
         */
        assertInvalid("testEmail23", "mms://john@");
        assertInvalid("testEmail24", "mms://@com");
        assertInvalid("testEmail25", "mms://\"john\\\"@yahoo.com");
        assertInvalid("testEmail26", "mms://\"\"john\"@yahoo.com");
        assertInvalid("testEmail27", "mms://\"john@yahoo.com");
        assertInvalid("testEmail28", "mms://john\"@yahoo.com");
        assertInvalid("testEmail29", "mms://john]yahoo.com");
        assertInvalid("testEmail30", "mms://john.@yahoo.com");
        assertInvalid("testEmail31", "mms://.john@yahoo.com");
        assertInvalid("testEmail32", "mms://john@.yahoo.com");
        assertInvalid("testEmail33", "mms://john.smith.yahoo.com");
        assertInvalid("testEmail34", "mms://john<yahoo.com");
        assertInvalid("testEmailNN", "mms://\"\"John\"<john@yahoo.com>");
       
        assertInvalid("testEmail36", "mms://\"john smith\"    @yahoo.com");

        assertInvalid("testEmail37", "mms://john.smith@\"yahoo\".com");

        assertInvalid("testEmail38", "mms://john@[yahoo[]");
        assertInvalid("testEmail39", "mms://john@[yahoo\\]");
        assertInvalid("testEmail40", "mms://\"John\"  <john@yahoo.com>");
       
        // IMPL_NOTE add test to test \r, \n and ctrl character
        // phone - general
        assertValid("testPhone1", "mms://+1");
        assertValid("testPhone2", "mms://1");
        assertValid("testPhone3", "mms://+11234");
        assertValid("testPhone4", "mms://+1234:sun.package.class");
        assertValid("testPhone5", "mms://1123");
        assertValid("testPhone6", "mms://1234:sun.package.class");
        assertValid("testPhone7", "mms://1234:class_name");
        assertInvalid("testPhone8", "mms://+");
        assertInvalid("testPhone9", "mms://+1234:sun.package.");
        assertInvalid("testPhone10", "mms://1234:sun*package.class");
        assertInvalid("testPhone11", "mms://1234:.package.class");
        assertInvalid("testPhone12",
                "mms://+1234:123456789012345678901234567890123");

        // ipv4
        assertValid("testIpv4_1", "mms://1.1.1.1");
        assertValid("testIpv4_2", "mms://111.111.111.111");
        assertValid("testIpv4_3", "mms://255.255.255.255");
        assertValid("testIpv4_4", "mms://111.111.111.111:sun.package.class");
       
        assertValid("testIpv4_5", "mms://111.111.111.111:class");
        assertInvalid("testIpv4_6", "mms://256.1.1.1");
        assertInvalid("testIpv4_7", "mms://1.1.1.256");
        assertInvalid("testIpv4_8", "mms://1111.1.1.1");
        assertInvalid("testIpv4_9", "mms://1.1111.1.1");
        assertInvalid("testIpv4_10", "mms://1.1.1111.1");
        assertInvalid("testIpv4_11", "mms://1.1.1.1111");
        assertInvalid("testIpv4_12", "mms://1:1:1:1");
        assertInvalid("testIpv4_13", "mms://1.1");
        assertInvalid("testIpv4_14", "mms://1.1.1");
        assertInvalid("testIpv4_15", "mms://1.1.1.");
        assertInvalid("testIpv4_16", "mms://.1.1.1");
        assertInvalid("testIpv4_17", "mms://111.111.111.111:sun.package.");
       
        assertInvalid("testIpv4_18", "mms://111.111.111.111:sun(package.class");
       
        assertInvalid("testIpv4_19", "mms://111.111.111.111:.package.class");
       
        assertInvalid("testIpv4_20",
                "mms://111.111.111.111" + ":123456789012345678901234567890123");
       
        // ipv6
        assertValid("testIpv6_1", "mms://1:1:1:1:1:1:1:1");
        assertValid("testIpv6_2", "mms://1111:1:1:1:1:1:1:1");
        assertValid("testIpv6_3",
                "mms://1111:1111:1111:1111:1111:1111:1111:1111");
        assertValid("testIpv6_4", "mms://0:0:0:0:0:0:0:0");
        assertValid("testIpv6_5", "mms://A:B:C:D:E:F:1:1");
        assertValid("testIpv6_6", "mms://1:1:1:1:1:1:1:1:sun.package.class");
       
        assertValid("testIpv6_7", "mms://A:B:1:1:C:D:1:1:class");
        assertInvalid("testIpv6_8", "mms://1:1:1");
        assertInvalid("testIpv6_9", "mms://1:1:1:1");
        assertInvalid("testIpv6_10", "mms://1:1:1:1:1");
        assertInvalid("testIpv6_11", "mms://1:1:1:1:1:1");
        assertInvalid("testIpv6_12", "mms://1:1:1:1:1:1:1");
        assertInvalid("testIpv6_13", "mms://1:1:1:1:1:1:1:1:");
        assertInvalid("testIpv6_14", "mms://:1:1:1:1:1:1:1:1");
        assertInvalid("testIpv6_15", "mms://1:1:1:1:1:1:1:11111");
        assertInvalid("testIpv6_16", "mms://11111:1:1:1:1:1:1:1");
        assertInvalid("testIpv6_17", "mms://1:1:1:1:11111:1:1:1");
        assertInvalid("testIpv6_18", "mms://1:1:G:1:1:1:1:1");
        assertInvalid("testIpv6_19", "mms://1:1:a:1:1:1:1:1");
        assertInvalid("testIpv6_20", "mms://1:1:A:1:1:1:1:sun.package.");

        assertInvalid("testIpv6_21", "mms://1:1:A:1:1:1:1:1:sun.package.");
       
        assertInvalid("testIpv6_22", "mms://1:1:B:1:1:1:1:1:sun#package.class");
       
        assertInvalid("testIpv6_23", "mms://1:1:1111:1:1:1:1:1:.package.class");
       
        assertInvalid("testIpv6_24",
                "mms://1:1:D:1:1:1:1:1" + ":123456789012345678901234567890123");

        // shortcode
        assertValid("testShcd1", "mms://shortcode");

        assertInvalid("testShcd3", "mms://invalid_shortcode");

        // application id
        assertValid("testAppid1", "mms://:sun.package.class");
        assertValid("testAppid2", "mms://:package.class");
        assertValid("testAppid3", "mms://:longer_class");
        assertValid("testAppid4", "mms://:2345678901234567890123456789012");
       
        assertValid("testAppid6", "mms://:23456789012345678901234567890123");
        assertInvalid("testAppid6", "mms://:234567890123456789012345678901234");
       
        assertInvalid("testAppid7", "mms://:className%");
        assertInvalid("testAppid8", "mms://:.class");
        assertInvalid("testAppid9", "mms://:com.sun.package.");
        assertInvalid("testAppid10", "mms://:");
        assertInvalid("testAppid11", "mms://:.");
        assertInvalid("testAppid12", "mms://:*.");

        // those that are false for some types but true for the other
        assertInvalid("testHost1", "mms://"); 
        assertValid("testHost2", "mms://:class");

        // true for app, false for phone
        assertValid("testHost3", "mms://0"); // true for phone,

        // false for ipv4,ipv6
        assertValid("testHost4", "mms://1:1"); // true for phone, false for ipv6

        // More positive tests
        assertValid("test1", "mms://:appID1");
        assertValid("test2", "mms://+34567890");
        assertValid("test3", "mms://+34567890:com.sun.Messenger");
        assertValid("test4", "mms://:com.sun.Messenger45627");
        assertValid("test5", "mms://test@domain.com");
        assertValid("test6", "mms://12shortcode");
        assertValid("test7", "mms://:app.ID");
        assertValid("test8", "mms://:aPl1cat1on_iD.is.WHAT_th15.15");
        assertValid("test9", "mms://1");
        assertValid("test10", "mms://+2");
        assertValid("test12", "mms://1234:5678");
        assertValid("test13", "mms://123:5678.910A");
        assertValid("test14", "mms://+1234:5678");
        assertValid("test15", "mms://+1234:5678.910A");
        assertValid("test17", "mms://1.2.3.4");
        assertValid("test18", "mms://1.2.3.4:5.6.7.8");
        assertValid("test19", "mms://1:2:3:4:5:6:7:8");
        assertValid("test20", "mms://A:B:C:D:E:F:A:B");
        assertValid("test21", "mms://A:B:C:D:E:F:A:B:CAFE");
        assertValid("test22", "mms://A:2:C:4:E:6:A:8:999");
        assertValid("test23", "mms://shortcodeBaby");
        assertValid("test24", "mms://123shortcodeBabes456");
        assertValid("test25", "mms://here\"is a \"phrase:;");
        assertValid("test26", "mms://\"and another phrase\":;");
        assertValid("test27", "mms://word\"anotherWord\"andAThird:;");

        assertValid("test28", "mms://\"with\\b,A Comma\":;");
        assertValid("test29", "mms://\"\\b\":;");
        assertValid("test30", "mms://withAnAddress:hamSandwich@domain;");

        assertValid("test31", "mms://listOf\" Mboxes\":mbox1@dom1,mbox2@dom2;");

        assertValid("test32", "mms://RyeOnLoaf@my.domain.com");
        assertValid("test33", "mms://<RouteAddress@foo.com>");
        assertValid("test34", "mms://\"No Space B4 \"Angle<no.space@angle>");

        assertValid("test35", "mms://Joe\" Bloggs \"<joe@bloggs.com>");
       
        assertValid("test36", "mms://\"listOf Mboxes\":mbox1@dom1,\"Another" + 
                " Mailbox \"<mbox2@dom2>;");
        assertValid("test37", "mms://strangeDomain@foo.[].com");
        assertValid("test38", "mms://strangerDomain@foo.[\\a].com");
        assertValid("test39", "mms://weirdDomain@foo.[some dtext].com");

        assertValid("test40", "mms://weirdDomain@foo.[,.:;].com");
        assertValid("test41", "mms://<@route:\" funny\"@business.yeah>");

        assertValid("test42",
                "mms://<@route1,@route2.com2:\" funny\"@business.yeah>");
       
        assertValid("test43",
                "mms://we\"\\a\"ve\" got a live one here \"<poopsie" +
                "@floor.stomp>");

        // More negative tests
        assertInvalid("test_1", "");
        assertInvalid("test_2", "mms:/");
        assertInvalid("test_3", "mms://");
        assertInvalid("test_5", "mms://++");
        assertInvalid("test_6", "mms://1234:5678:");
        assertInvalid("test_7", "mms://A23:5678.910A");
        assertInvalid("test_8", "mms://+1234:");
        assertInvalid("test_9", "mms://+1234:567*");
        assertInvalid("test_10", "mms://123.456.789.012.");
        assertInvalid("test_11", "mms://1.2.3.4:a:b");
        assertInvalid("test_12", "mms://1.2.3.");
        assertInvalid("test_13", "mms://1:2:3:4:5:6:7:8:");
        assertInvalid("test_14", "mms://A:B:C:D:E:F:A:H");
        assertInvalid("test_15", "mms://A:B:C:D:E:F:A:B:_:");
        assertInvalid("test_16", "mms://A:2:C:4:E:6:A:8:999*");
        assertInvalid("test_17", "mms://shortcode:app.id");
        assertInvalid("test_18", "mms://\"unclosedQuote:");
        assertInvalid("test_19", "mms://listOf Mboxes:mbox1@,;");
        assertInvalid("test_20", "mms://listOf Mboxes:mbox1@,mbox2@");
       
        assertInvalid("test_21", "mms://Joe Bloggs");
        assertInvalid("test_22", "mms://Joe Bloggs <joe>");
        assertInvalid("test_23", "mms://Joe Bloggs <joe@>");
        assertInvalid("test_24", "mms://Joe Bloggs <joe@b@com>");
        assertInvalid("test_25", "mms://< @dom1, : funny@business.yeah>");
       
        assertInvalid("test_26",
                "mms://< @dom1, goober : funny@business.yeah>");

        // more negative tests
        assertInvalid("test16", "mms://123.456.789.012");
        assertInvalid("test25", "mms://here is a phrase:;");
        assertInvalid("test28", "mms://\"with\\,A Comma\":;");
        assertInvalid("test29", "mms://\"\\\b\":;");
        assertInvalid("test31", "mms://listOf Mboxes:mbox1@dom1,mbox2@dom2;");
       
        assertInvalid("test34", "mms://No Space B4 Angle<no.space@angle>");
       
        assertInvalid("test35", "mms://Joe Bloggs <joe@bloggs.com>");
        assertInvalid("test36",
                "mms://listOf Mboxes:mbox1@dom1,Another Mailbox " +
                "<mbox2@dom2>;");
        assertInvalid("test38", "mms://strangerDomain@foo.[\\4].com");

        assertInvalid("test41", "mms://< @route: funny@business.yeah>");

        assertInvalid("test42",
                "mms://< @route1, @route2.com2: funny@business.yeah>");

        assertInvalid("test43",
                "mms://we\"\\'\"ve got a live one here <poopsie"
                + "@floor.stomp>");

        /*
         * Questionable issues
         *
         * The following tests violate the spec., but we think it is correct
         * behavior.
         */

        // 1) ipv4 should be tested for numbers < 256
        assertValid("TEST_issues_1_1", "mms://255.255.255.255");
        assertValid("TEST_issues_1_2", "mms://255.255.255.255:appId");

        assertInvalid("TEST_issues_1_3", "mms://256.1.1.1");
        assertInvalid("TEST_issues_1_4", "mms://1.256.1.1");
        assertInvalid("TEST_issues_1_5", "mms://1.1.256.1");
        assertInvalid("TEST_issues_1_6", "mms://1.1.1.256");

        // 2) "." is of part of applicationID-symbol
        assertInvalid("TEST_issues_2_1", "mms://:package..class");
        assertInvalid("TEST_issues_2_2", "mms://:.");
        assertInvalid("TEST_issues_2_3", "mms://:...class");

        // 3) route is defined as ("@"domain)[*(","("@"domain))]
        //    which makes ":" be a valid route specification but
        //    in RFC it is 1#("@"domain)":" and ":" will be not allowed
        assertInvalid("TEST_issues_3_1", "mms://\"J\"<:john@yahoo>");

        // ----------------- The following does not violate the spec
        // ----------------- but probably should

        // 4) "mms://" is allowed by the spec in shortcode
        assertInvalid("TEST_issues_4", "mms://");

        // 5) group is defined as phrase":"[mailbox *(","mailbox)]";"
        // which makes the following not possible: phrase:mailbox,,mailbox;
        // while in the RFC 822 it is specified phrase":"[#mailbox]";"
        // and the example is possible
        assertInvalid("TEST_issues_5_1", "mms://group:john@yahoo,,john@yahoo;");
       
        // 6) mailbox is specified as addr-spec | [phrase]route-addr
        // which make phrase to be optional and the following is possible
        // mms://<john@yahoo.com>
        // while in RFC 822 mailbox is specified as
        // addr-spec | phrase route-addr
        assertValid("TEST_issues_6_2", "mms://<john@yahoo.com>");
    } 

    /**
     * Provide clean-up services, following the run of this test.
     */
    void cleanUp() {
    }

    /**
     * Positive test case.
     * @param testID Test case name.
     * @param addressString The connection string to be tested.
     */
    private void assertValid(String x, String addressString)
        throws Throwable {

        testAddressValidity(addressString, true);
    }

    /**
     * Negative test case.
     * @param testID Test case name.
     * @param addressString The connection string to be tested.
     */
    private void assertInvalid(String x, String addressString)
        throws Throwable {

        testAddressValidity(addressString, false);
    }

    /**
     * Test the validity of a single connection string.
     *
     * @param addressString The connection string to be tested.
     * @param valid <code>true</code> if the address is expected to be valid;
     *     <code>false</code>, otherwise.
     *
     * @return <code>true</code> if the address was valid;
     *     <code>false</code>, otherwise.
     */
    private void testAddressValidity(String addressString, boolean valid)
        throws Throwable {

        MessageConnection mc = null;
        boolean iaeThrown = false;

        try {

            mc = (MessageConnection)Connector.open(addressString);

        } catch (IllegalArgumentException iae) {

            iaeThrown = true;

        } finally {

            if (mc != null) {
                mc.close();
            }
        }


        if (valid) {

             assertNotNull("Expected valid for " + addressString, mc);

        } else {

             assertTrue("Expected invalid for " + addressString, iaeThrown);
        }
    }

    /**
     * Main entry point.
     */
    public void runTests() throws Throwable {
        setUp();

        declare(TEST_NAME);

        run();

        cleanUp();
    }

}

