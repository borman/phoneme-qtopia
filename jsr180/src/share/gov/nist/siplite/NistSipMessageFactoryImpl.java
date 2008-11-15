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
/*
 */

package gov.nist.siplite;
import gov.nist.siplite.stack.*;
import gov.nist.siplite.message.*;
import gov.nist.core.*;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Implements all the support classes that are necessary for the nist-sip
 * stack on which the jain-sip stack has been based.
 * This is a mapping class to map from the NIST-SIP abstractions to
 * the JAIN abstractions. (i.e. It is the glue code that ties
 * the NIST-SIP event model and the JAIN-SIP event model together.
 * When a SIP Request or SIP Response is read from the corresponding
 * messageChannel, the NIST-SIP stack calls the SIPStackMessageFactory
 * implementation that has been registered with it to process the request.)
 *
 * @version  JAIN-SIP-1.1
 *
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NistSipMessageFactoryImpl
        implements SIPStackMessageFactory {
    /** Current SIP stack context. */
    private SipStack 	 sipStackImpl;
    
    /**
     * Constructs a new SIP Server Request.
     * @param sipRequest is the Request from which the SIPServerRequest
     * is to be constructed.
     * @param messageChannel is the MessageChannel abstraction for this
     * 	SIPServerRequest.
     * @return a new SIP Server Request handle
     */
    public SIPServerRequestInterface
            newSIPServerRequest
            (Request sipRequest, MessageChannel messageChannel)
            throws IllegalArgumentException {

        if (messageChannel == null || sipRequest == null)  {
            throw new IllegalArgumentException("Null Arg!");
        }
        
        NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
        if (messageChannel instanceof Transaction) {
            // If the transaction has already been created
            // then set the transaction channel.
            retval.transactionChannel = (Transaction)messageChannel;
        }
        SIPTransactionStack theStack =
                (SIPTransactionStack) messageChannel.getSIPStack();

        /*
         * IMPL_NOTE :
         * May not need to initialize listeningPoint like this. Please refer to
         * processRequest() method in NistSipMessageHandlerImpl
         */
        retval.listeningPoint =
                messageChannel.getMessageProcessor().getListeningPoint();
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "Returning request interface for " +
                sipRequest.getFirstLine() + " " + retval +
                " messageChannel = " + messageChannel);
        }
        
        return  retval;
    }
    
    /**
     * Generates a new server response for the stack.
     * @param sipResponse is the Request from which the SIPServerRequest
     * is to be constructed.
     * @param messageChannel is the MessageChannel abstraction for this
     * 	SIPServerResponse
     * @return a new SIP Server Response handle
     */
    public SIPServerResponseInterface
            newSIPServerResponse(Response sipResponse,
            MessageChannel messageChannel) {
        try {
            NistSipMessageHandlerImpl retval = new NistSipMessageHandlerImpl();
            SIPTransactionStack theStack = (SIPTransactionStack)
            messageChannel.getSIPStack();
            SipStack sipStackImpl = (SipStack) theStack;
            // Tr is null if a transaction is not mapped.
            Transaction tr =
                    (Transaction)
                    ((SIPTransactionStack)theStack).
                    findTransaction(sipResponse, false);
            
            retval.transactionChannel = tr;
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "Found Transaction " + tr + " for " + sipResponse);
                Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                    "MessageProcessor = " +
                    messageChannel.getMessageProcessor() + "/" +
                    messageChannel.getMessageProcessor().getListeningPoint());
            }
            
            ListeningPoint lp =
                    messageChannel.getMessageProcessor().getListeningPoint();
            
            retval.listeningPoint = lp;
            return  retval;
            
        } catch (RuntimeException ex) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_JSR180,
                    "runtime exception caught!");
                ex.printStackTrace();
            }
            return null;
        }
        
    }
    
    /**
     * Contrictor.
     * @param sipStackImpl current SIP stack context
     */
    public NistSipMessageFactoryImpl(SipStack sipStackImpl) {
        this.sipStackImpl = sipStackImpl;
    }
    
    
}
