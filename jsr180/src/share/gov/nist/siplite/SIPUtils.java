/*
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
package gov.nist.siplite;
import gov.nist.core.*;
import java.util.Random;
import java.util.Date;

/**
 * SIP utility functions.
 */
public class SIPUtils extends  Utils {
    /** Seed for randomizer. */ 
    private static int counter;

    /**
     * Generates the branch identifiers.
     *
     * @return a branch identifier.
     */
    public static String generateBranchId() {
        String b =  new Long(System.currentTimeMillis()).toString() +
                new Random().nextLong();
        byte bid[] = digest(b.getBytes());

        // cryptographically random string.
        // prepend with a magic cookie to indicate we
        // are bis09 compatible.
        return SIPConstants.GENERAL_BRANCH_MAGIC_COOKIE +
            Utils.toHexString(bid);
    }

    /**
     * Generates a call identifier for the stack.
     * @param stackAddr current stack address
     *
     * @return a call id.
     */
    public static String  generateCallIdentifier(String stackAddr) {
        String date = (new Date()).toString() + 
                       new Random(counter++).nextLong();
        byte cidbytes[] = digest(date.getBytes());
        String cidString = Utils.toHexString(cidbytes);
        return cidString + Separators.AT + stackAddr;
    }
}
