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

package com.sun.amms;

import com.sun.midp.events.EventListener;
import com.sun.midp.events.Event;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;
import com.sun.midp.events.NativeEvent;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.security.SecurityInitializer;

class AMMSEventType {
    /**
     * the following constants must be consistent
     * with javacall_amms_notification_type enum values
     * JAVACALL_EVENT_AMMS_***, defined in javacall_multimedia_advanced.h
     *
     * IMPL_NOTE: Current javacall_multimedia_advanced.h version
     *            defines this values implicitly
     */
    public static final int MEDIA_PROCESSOR_COMPLETED = 0;
    public static final int MEDIA_PROCESSOR_ERROR = 1;
}

public class AMMSMPEventListener implements EventListener {

    private static class SecurityTrusted implements ImplicitlyTrustedClass{};

    private static SecurityToken classSecurityToken;

    private static AMMSMPEventListener _instance;

    public static AMMSMPEventListener getInstance()
    {
        if (null == _instance)
            _instance = new AMMSMPEventListener();

        return _instance;
    }

    private AMMSMPEventListener()
    {
        classSecurityToken =
            SecurityInitializer.requestToken(new SecurityTrusted());
            
        EventQueue evtq =
            EventQueue.getEventQueue(classSecurityToken);
            
        evtq.registerEventListener(EventTypes.AMMS_EVENT, this);
    }
    
    public boolean preprocess(Event event, Event waitingEvent) {
        return true;
    }
    
    /**
     * Process an event.
     * This method will get called in the event queue processing thread.
     *
     * @param event event to process
     */
    public void process(Event event) {
        NativeEvent nevt = (NativeEvent)event;

        /// Notification may be long
        AMMSMPEventNotifier mpN = new AMMSMPEventNotifier(nevt.intParam4, nevt.intParam1);
        new Thread(mpN).start();
    }
}

class AMMSMPEventNotifier implements Runnable
{
    int type;
    int intParam1;

    AMMSMPEventNotifier(int type, int intParam1) {
        this.type = type;
        this.intParam1 = intParam1;
    }

    public void run() {
        BasicMediaProcessor mp;

        mp = BasicMediaProcessor.get(intParam1);
        if (mp != null)
        {
            switch (type)
            {
                case AMMSEventType.MEDIA_PROCESSOR_COMPLETED:
                    mp.notifyCompleted(true);
                    break;
                case AMMSEventType.MEDIA_PROCESSOR_ERROR:
                    mp.notifyCompleted(false);
                    break;
            }
        }
    }
}

