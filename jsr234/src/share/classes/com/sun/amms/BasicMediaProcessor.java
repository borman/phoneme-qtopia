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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.amms.MediaProcessor;
import javax.microedition.amms.MediaProcessorListener;
import javax.microedition.media.Control;
import javax.microedition.media.MediaException;

import java.util.Hashtable;

public abstract class BasicMediaProcessor implements MediaProcessor {
    /*
     * Several comments and rules: 
     *
     * 1). this class implements public methods of MediaProcessor I/F. 
     * For some of them it defines internal requestXXX() & doXXX() 
     * methods that are to be redefined and implemented in derived classes.
     * Public methods, that have "internal" pair assumed to be "final"
     * and SHALL NOT be redefined in derived classes.
     * These are: start(), stop(), complete(), abort(), 
     * setInput(), setOutput().
     * 
     * In general, a public method updates the state and invokes callback, 
     * while its internal pair does all object-specific processing and return 
     * true/false to indicate success/failure. Based on these return codes
     * public methods update state, invoke appropriate callbacks and 
     * throw MediaException when needed.
     *
     * 2). state changes & callbacks shall be done in a synchronized section
     * (use "stateLock" object for that). A dedicated object instead of 'this" 
     * is used to protect algorithm from accidental use of "this"-based  
     * synchronization for different purposes in derived classes.
     *
     * 3). "abort()", "stop()" & "notifyCompleted()" can conflict 
     * in resulting state and invoked callback. 
     *
     */

    /* Input and output parameters */
    protected InputStream inputStream = null;
    protected int inputStreamLength;
    protected Object inputObject = null;
    protected OutputStream outputStream = null;
    private   boolean inputWasSet = false; 

    /* Current media processor state */
    protected int state;
    
    /* Media processor controls */
    private Control[] controls;
    private String[]  controlNames;
    
    /* Media processor listeners */
    private Vector listeners;
    private boolean listenersModified;
    
    /* Media processor synchronizers */
    private Object stateLock = new Object();
    private Object processorLock = new Object();

    private static Object  idLock = new Object(); 
    private static int curID = 0;
    protected int      mediaProcessorID;

    /* Listener for complete events */
    private MPListenerWait mpWait = new MPListenerWait();
    
    /**
     * hashtable to map processorsID to instances
     */
    private static Hashtable mprocessors = new Hashtable(4);
    

    /**
     * Amount of work completed (0 - 100%)
     *
     * Subclasses need to set this value!
     */
    protected int progress = UNKNOWN;
    
    protected void setControls(Control[] controls, String[] controlNames) {
        this.controls = controls;
        this.controlNames = controlNames;
    }
    
    /**
     * BasicMediaProcessorInternal I/F method
     * Subclasses need to implement this to perform implementation specific 
     * checks, allocations, etc
     *
     * @param input input stream.
     * @param length length of input stream or UNKNOWN.
     * @return true on success, false otherwise 
     * @throws MediaException if the input could not be set 
     *         and exception shall be delivered to user.
     */
    protected abstract boolean doSetInput(InputStream input, int length) 
        throws MediaException;

    /**
     * BasicMediaProcessorInternal I/F method
     * Subclasses need to implement this to perform implementation specific 
     * checks, allocations, etc
     *
     * @return true on success, false otherwise 
     * @throws MediaException if the input could not be set 
     *         and exception shall be delivered to user.
     */
    protected abstract boolean doSetInput(Object image) throws MediaException;

    /**
     * BasicMediaProcessorInternal I/F method
     * Subclasses need to implement this to start
     * the <code>MediaProcessor</code>.
     *
     * @return true on success (signal to move to STARTED state)
     * @throws MediaException if the <code>MediaProcessor</code>
     * could not be started and exception shall be delivered to user.
     */
    protected abstract boolean doStart() throws MediaException;

    /**
     * BasicMediaProcessorInternal I/F method
     * Subclasses need to implement this to stop
     * the <code>MediaProcessor</code>.
     *
     * @return true on success (MediaProcessor going to stop)
     * @throws MediaException if the <code>MediaProcessor</code>
     * could not be stopped.
     */
    protected abstract boolean doStop() throws MediaException;

    /**
     * BasicMediaProcessorInternal I/F method
     * Subclasses need to implement this to continue execution
     * the <code>MediaProcessor</code>.
     *
     * @return true on success (MediaProcessor continued)
     * @throws MediaException if the <code>MediaProcessor</code>
     * could not be continued.
     */
    protected abstract boolean doContinue() throws MediaException;
    
    /**
     * BasicMediaProcessorInternal I/F method
     * Writes output data to the stream.
     *
     * @return true on successful completion, false otherwise 
     * (processing errors)
     */
    protected abstract boolean doOutput();

    /**
     * BasicMediaProcessorInternal I/F method
     * Aborts media processing.
     *
     * @return true on successful completion,
     */
    protected abstract boolean doAbort();

    /**
     * Public constructor.
     *
     * @param allCtrls acceptable controls lists.
     *
     */
    public BasicMediaProcessor() {
        this.controlNames = new String[0];
        this.controls = new Control[0];
        
        mediaProcessorID = 0;
        synchronized (idLock) {
            mediaProcessorID = (curID + 1) & 32767;
            curID = mediaProcessorID;
        }
        mprocessors.put(new Integer(mediaProcessorID), this);
        
        listeners = new Vector();

        inputStream = null;
        outputStream = null;

        state = UNREALIZED;
    }

    public Control getControl(String controlType) {
        if (controlType == null) 
            throw new IllegalArgumentException("Invalid control type"); 
        
/* Currently, the specification say, that controls may be get in unrealized 
   state, but in the proposals for JSR 234 (N20) suggested to forbid it. */
/*        if (state == UNREALIZED)
            throw new IllegalStateException("Invalid state: "  + state);*/
        

        // Prepend the package name if the type given does not
        // have the package prefix.
        String type = (controlType.indexOf('.') < 0)
            ? ("javax.microedition.media.control." + controlType)
            : controlType;

        for (int i = 0; i < controlNames.length; i++)
            if (type.equals(controlNames[i]))
                return controls[i];
        
        return null;
    }

    public Control[] getControls() {
/* Currently, the specification say, that controls may be get in unrealized 
   state, but in the proposals for JSR 234 (N20) suggested to forbid it. */
/*        if (state == UNREALIZED)
              throw new IllegalStateException("Invalid state: "  + state);*/
        Control[] result = new Control[controls.length];
        System.arraycopy(controls, 0, result, 0, controls.length);
        return result;
    }

/* JAVADOC ELIDED */
    public void setInput(InputStream input, int length) 
        throws javax.microedition.media.MediaException {
        
        inputWasSet = false;
        synchronized (stateLock) {
            if (state != UNREALIZED && state != REALIZED) {
                throw new IllegalStateException("Invalid state " + state);
            }

            if (null == input) {
                throw new IllegalArgumentException("Invalid input stream");
            }

            if (length < 1 && length != UNKNOWN) {
                throw new IllegalArgumentException("Invalid input stream length " + length);
            }

            inputWasSet = doSetInput(input, length);
            
            // Reset object. Use stream.
            inputObject = null;
            inputStream = input;
            inputStreamLength = length;

            if(isRealizable())
                notifyRealized();
        }
    }

/* JAVADOC ELIDED */
    public synchronized void setInput(Object image) 
    throws javax.microedition.media.MediaException {
        inputWasSet = false;
        synchronized (stateLock) {
        
            if (state != UNREALIZED && state != REALIZED)
                throw new IllegalStateException("Invalid state " + state);
            
            inputWasSet = doSetInput(image);
            // Reset stream. Use object.
            if (inputStream != null) {
                //try {inputStream.close();} catch();
                inputStream = null;
            }
            inputObject = image;

            if(isRealizable())
                notifyRealized();
        }
    }

    /**
     * Sets the output of the media processor.
     * @param output The <code>OutputStream</code> to be used as output.
     * @throws IllegalArgumentException if output is null.
     * @throws IllegalStateException if the <code>MediaProcessor</code> was not in <i>UNREALIZED</i> or <i>REALIZED</i> state.
     */
    public void setOutput(OutputStream output) {
        synchronized (stateLock) {
            if (state != UNREALIZED && state != REALIZED) {
                throw new IllegalStateException("Invalid state " + state);
            }
            
            if (output == null) {
                throw new IllegalArgumentException("Invalid output stream");
            }

            outputStream = output;
            if(isRealizable()) {
                notifyRealized();
            }
        }
    }

    /**
     * checks if the media processor can be transitioned  
     * from UNREALIZED to REALIZED state.
     */
    private boolean isRealizable() {
        return ( ((inputStream != null) || (inputObject != null))
             && (outputStream != null) && (state == UNREALIZED));
    }

/* JAVADOC ELIDED */
    public final void start() throws MediaException {
        if (state == STARTED) {
            return;
        }
        
        synchronized (stateLock) {
            /* do nothing if already in STARTED state */
            if (state == STARTED) {
                return;
            }
            /* valid states are only REALIZED and STOPPED */
            if (state == UNREALIZED)
                throw new IllegalStateException("Invalid state " + state);
            
            if (!inputWasSet)
                throw new MediaException("Incorrect or unsupported input data");

            boolean isStarted = false;
            if (state == STOPPED)
                isStarted = doContinue();
            else
                isStarted = doStart();
            
            if (isStarted) {
                    state = STARTED;
                    /** requestStart & notification is done inside stateLock. So 
                     * in any case PROCESSING_STARTED will be send early than 
                     * PROCESSING_COMPLETED or PROCESSING_EROR
                     */
                    notifyListeners(MediaProcessorListener.PROCESSING_STARTED,
                        new Integer(getProgressInTenths()));
            } 
            else throw new MediaException("Failed to start operation");
        }   
    }

/* JAVADOC ELIDED */
    public final void stop() throws MediaException {
        /* do nothing if not in STARTED state */
        if (state != STARTED) {
            return;
        }

        synchronized (stateLock) {
            if (state != STARTED) {
                return;
            }
            
            if (doStop()) {
                notifyStopped();
            } else {
                throw new MediaException("Failed to stop operation");
            }
        }
    }

/* JAVADOC ELIDED */
    public final void complete() throws MediaException {
        /*
         * need to do same checks as start() method does ...
         * need to be inlined if there is a risk that start() method in derived 
         * classes will be inlined !
         */
        synchronized(stateLock) {
            if (state == UNREALIZED)
                throw new IllegalStateException("Invalid state " + state);
            
            if (state != STARTED) /// If not started start
                    start(); 
        }

        String event = mpWait.Complete();
        if (event != MediaProcessorListener.PROCESSING_COMPLETED)
            throw new MediaException("Media processing was interrupted: " + event);
    }
    
    
    /**
     * Aborts the processing even if the processing was not
     * complete.
     * Any bytes that were written to output may not be
     * reasonable and should be discarded. A <code>PROCESSING_ABORTED</code>
     * event is posted and the <code>MediaProcessor</code> is
     * moved into <i>UNREALIZED</i> state.
     * <p>
     * Ignored if the
     * <code>MediaProcessor</code> was in <i>REALIZED</i> or <i>UNREALIZED</i> state.
     */
    public synchronized final void abort() {
        /* do nothing of not in STARTED or STOPPED state - nothing to abort */
        if (state != STARTED && state != STOPPED) {
            return;
        }
        
        // close input & output streams before callback
        synchronized (stateLock) {
            if (state != STARTED && state != STOPPED) {
                return;
            }
            
            if (doAbort()) {
                synchronized (stateLock) {
                    closeAllStreams();
                    state = UNREALIZED;
                    notifyListeners(MediaProcessorListener.PROCESSING_ABORTED,
                        new Integer(getProgressInTenths()));
                }
            } else {
                /// Strange state. Abort doesn't throw exceptions, so what do with 
                /// unsuccessful result ?
            }            
        }
    }

    public final void addMediaProcessorListener(MediaProcessorListener listener) {
        if (listener != null) {
            /* 
             * Explicit "sync" is needed to raise "modified" flag. 
             * Implicit "sync" is already inside addElement() method, 
             * so second sync from the same thread will do nothing ...
             */
            synchronized (listeners) { 
                listenersModified = true;
                listeners.addElement(listener);
            }
        }
    }

    public final void removeMediaProcessorListener(MediaProcessorListener listener) {
        if (listener != null) {
            /* 
             * Explicit "sync" is needed to raise "modified" flag. 
             * Implicit "sync" is already inside removeElemet() method, 
             * so second sync from the same thread will do nothing ...
             */
            synchronized (listeners) {
                listenersModified = true;
                listeners.removeElement(listener);
            }
        }
    }

    /**
     * Get an estimated percentage of work that has been done.
     *
     * @return
     * <ul>
     * <li>0, if the <code>MediaProcessor</code> is in <i>UNREALIZED</i> or <i>REALIZED</i> state
     * <li>amount of work completed (0 - 100%) if <code>MediaProcessor</code> is in <i>STARTED</i> or <i>STOPPED</i> states
     * <li><code>UNKNOWN</code>, if the estimation cannot be calculated.
     * </ul>
     */
    public int getProgress() {
        return ((state == UNREALIZED) || (state == REALIZED)) 
            ? 0 : progress;
    }

    /**
     * Get the current MediaProcessor state.
     *
     * @return
     * <ul>
     * <li><i>UNREALIZED</i>
     * <li><i>REALIZED</i>
     * <li><i>STOPPED</i>
     * <li><i>STARTED</i>
     * </ul>
     */
    public int getState() {
        return state;
    }

    /**
     * Called to notify about processing completed (or processing error)
     */
    protected void notifyCompleted(boolean processingSuccess) {
        if (processingSuccess) {
            processingSuccess = doOutput();

            synchronized (stateLock) {
                // close input & output streams before callback
                closeAllStreams();

                state = UNREALIZED;
                notifyListeners(MediaProcessorListener.PROCESSING_COMPLETED,
                    new Boolean(processingSuccess));
            }
        } else {
            synchronized (stateLock) {
                state = REALIZED;
                notifyListeners(MediaProcessorListener.PROCESSING_ERROR,
                    "");
            }
        }
            
    }

    protected void notifyStopped() {
        synchronized (stateLock) {
            state = STOPPED;
            notifyListeners(MediaProcessorListener.PROCESSING_STOPPED, 
                    new Integer(getProgressInTenths()));
        }
    }
    
    /**
     * Called from setInput() & setOutput() methods 
     * to notify that processor wants to move to REALIZED state. 
     */
    private void notifyRealized() {
        synchronized (stateLock) {
            state = REALIZED;
            notifyListeners(MediaProcessorListener.PROCESSOR_REALIZED, 
                    new Integer(getProgressInTenths()));
        }
    }

    private void notifyListeners(String message, Object obj) {
        Object copy[];
        synchronized (listeners) {
            copy = new Object[listeners.size()];
            listeners.copyInto(copy);
            listenersModified = false;
        }
        
        for (int i = 0; i < copy.length; i++) {
            MediaProcessorListener listener = (MediaProcessorListener)copy[i];
            listener.mediaProcessorUpdate(this, message, obj);
        }

        mpWait.mediaProcessorUpdate(this, message, obj);
        /*
         * need to check for "listenersModified == true", 
         * this means that one of callbacks updated listeners ->
         * need some actions ...
         */
    }
    
    private int getProgressInTenths() {
        return ((progress > 0) && (progress <= 100) 
            ? (progress * 10)
            : progress);
    }
    
    private boolean closeAllStreams() {
        boolean result;
        try {
            if (inputStream != null) 
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            /*
             * Reset handles to avoid false transition to REALIZED state after
             * next input setup
             */
            inputStream = null;
            outputStream = null;
            inputObject = null;
            result = true;
        } catch (java.io.IOException ioex) {
            result = false;
        }
        return result;
    }
    
    private boolean waitProcessing() {
        return false;
    }
    
    /**
     * For processors management
     *
     * @param  pid  Description of the Parameter
     * @return      Description of the Return Value
     */
    public static BasicMediaProcessor get(int mpid) {
        return (BasicMediaProcessor) (mprocessors.get(new Integer(mpid)));
    }
}

class MPListenerWait implements MediaProcessorListener{
    boolean isWait = true;
    String  event = MediaProcessorListener.PROCESSING_ERROR;
    
    public void mediaProcessorUpdate( MediaProcessor processor, String event, Object eventData ) {
           this.event = event;
           isWait = (event == MediaProcessorListener.PROCESSING_STARTED);
           if (!isWait) {
               synchronized(this) {
                  this.notifyAll();
               }
           }
    }
    
    public String Complete() {
        synchronized(this) {
            while (isWait) 
                try { this.wait(); } catch(Exception e) {};
        }
        return event;
    }
}

