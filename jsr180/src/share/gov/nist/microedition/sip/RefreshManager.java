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
 * RefreshManager.java
 *
 * Created on Apr 8, 2004
 *
 */
package gov.nist.microedition.sip;

import gov.nist.siplite.message.Request;

import java.util.Hashtable;
import java.util.Timer;

import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipRefreshListener;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Refresh manager.
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RefreshManager {
    /**
     * The unique instance of this class.
     */
    private static RefreshManager instance = null;
    /**
     * The hashtable keeping a mapping between the refreshID and
     * the refresh Tasks.
     */
    private Hashtable refreshTable = null;
    /**
     * generator of task id .
     */
    private int idGenerator = 0;

    /**
     * Creates a new instance of the RefreshManager.
     */
    private RefreshManager() {
        refreshTable = new Hashtable();
    }

    /**
     * Returns the instance of RefreshManager.
     * @return the instance of RefreshManager singleton
     */
    public static synchronized RefreshManager getInstance() {
        if (instance == null)
            instance = new RefreshManager();
        return instance;
    }

    /**
     * Creates a new RefreshTask with a specific id, schedules it and
     * keep the mapping between this newly created task and the id.
     * @param request for which a refresh task must be created
     * @param sipConnectionNotifier  used to send
     * the request
     * @param sipRefreshListener  the callback interface used listening for
     * refresh event on this task
     * @param sipClientConnection the connection to update
     * @return the id of the newly created task
     */
    public int createRefreshTask(
            Request request,
            SipConnectionNotifier sipConnectionNotifier,
            SipRefreshListener sipRefreshListener,
            SipClientConnection sipClientConnection) {
        int taskId = ++idGenerator;
        RefreshTask refreshTask = new RefreshTask(
                String.valueOf(taskId),
                request,
                sipConnectionNotifier,
                sipRefreshListener,
                sipClientConnection);
        refreshTable.put(String.valueOf(taskId), refreshTask);
        return taskId;
    }

    /**
     * Schedules the task whose id is given in parameter for the expires
     * @param taskId - the id of the task to schedule
     * @param expires - the expires time,so the delay until when the stack must
     * schedule the task. If it is -1, it means that the expires has
     * already been given when the task was created.
     */
    public void scheduleTask(String taskId, int expires) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_JSR180,
                "schedule the next register in " + expires + " sec");
        }

        RefreshTask refreshTask = (RefreshTask)refreshTable.get(taskId);
        if (refreshTask == null)
            return;
        if (expires == -1)
            return;
        if (expires >= 0) {
            // The timer to shcedule the tasks
            Timer timer = new Timer();
            // Once the task has been processed, it can be scheduled again
            // so a new one is created
            refreshTask = new RefreshTask(
                    refreshTask.getTaskId(),
                    refreshTask.getRequest(),
                    refreshTask.getSipConnectionNotifier(),
                    refreshTask.getSipRefreshListener(),
                    refreshTask.getSipClientConnection());
            refreshTable.put(taskId, refreshTask);
            timer.schedule(refreshTask, expires * 1000);
        }
    }


    /**
     * Return the task mapping the taskId
     * @param taskId - the id of the task to retrieve
     * @return the Refresh task mapping the taskId
     */
    public RefreshTask getTask(String taskId) {
        return (RefreshTask)refreshTable.get(taskId);
    }

    /**
     * Removes a task
     * @param taskId - the task id of the task to remove
     */
    public void removeTask(String taskId) {
        refreshTable.remove(taskId);
    }
}
