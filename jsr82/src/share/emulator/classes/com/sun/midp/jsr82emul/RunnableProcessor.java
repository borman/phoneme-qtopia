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

/*
 * Runs emulation request processing in a separate thread.
 */
abstract public class RunnableProcessor implements Runnable {
    /* Shows if processing can be started. */
    protected boolean ready = false;
    /* Shows if processing has been interrupted. */
    protected boolean interrupted = false;
    /* Shows if processing should be performed repeatedly. */
    private boolean loop = false;
    /* Thread that performs processing. */
    private Thread thread = new Thread(this);
    
    /* 
     * Constructs an instance. 
     * @param loop flag to define if repeated processing required.
     */
    RunnableProcessor(boolean loop) {
        this.loop = loop;
        thread = new Thread(this);
        thread.start();
    }
    
    /* Constructs an instance for one time processing. */
    RunnableProcessor() {
        this(false);
    }
    
    /* 
     * Implements <code>Runnable</code>. 
     * Accepts and opens connection to client 
     */
    public void run() {
        do {
            synchronized (this) {
                if (!ready) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                ready = false;
            }
            
            process();
        
        } while (loop);
    }
        
    /* Interrupts processing thread. */
    public void interrupt() {
        interrupted = true;
        loop = false;
        thread.interrupt();
    }
    
    /* 
     * Allows one processing procedure. No processing is performed prior
     * to this method call. For a repeatedly working processor each 
     * iteration waits for <code>start()</code>.
     */
    public synchronized void start() {
        ready = true;
        notify();
    }
        
    /* Processing procedure. */
    abstract protected void process();
}              
