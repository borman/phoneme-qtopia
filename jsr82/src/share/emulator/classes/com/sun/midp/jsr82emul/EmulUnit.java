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
import com.sun.midp.log.Logging;
import  com.sun.jsr082.bluetooth.SDPServer;

/*
 * Emulation unit is an entity that can process requests from
 * porting layer.
 */
interface EmulUnit {
    /* 
     * Processes a request from porting layer to emulation.
     * @param request serialized requests sequence with current
     *        offset that points to request to be processed.
     */
    public void process(BytePack request);
}

/*
 * A emulation unit which is actually a controller that passes request
 * to one of predefined units saved in an array.
 */ 
abstract class EmulUnitCaller implements EmulUnit {
    /* Units to pass requests to. */
    protected EmulUnit[] callee;
    
    /* 
     * Processes requests. The only supported type of requests is: 
     * pass request to the unit with given index.
     * @param request serialized requests under processing.
     */
    public void process(BytePack request) {
        int id = request.extract();
        callee[id].process(request);
    }
}

/*
 * Main controller that passes request to proper units for processing.
 */
class MainCaller extends EmulUnitCaller { 
    /* The only instance. */
    private static MainCaller instance = new MainCaller();
    
    /* Constructs the instance. */
    private MainCaller() {
        new SDPServer().start();
        
        callee = new EmulUnit[] {
            DeviceEmul.getLocalDeviceEmul(),
            new NotifierCreator(),
            new ConnectionCreator(),
            ConnRegistry.getInstance(),
            new SDPReqCreator() 
        };
    }
    
    /* 
     * Retrieves the only instance. 
     * @return (reference to) the only instance.
     */
    static MainCaller getInstance() {
        return instance;
    }
}

/* Emulation unit that supports requests for a SDP requester creation. */
class SDPReqCreator implements EmulUnit {
    /* 
     * Processes requests.
     * @param request serialized requests under processing.
     */
    public void process(BytePack request) {
        // It registers itself as a requests processor
        SDPReqEmul sre = new SDPReqEmul(request);
        new Thread(sre).start();
    }
}

/* Emulation unit that supports requests for a notifier creation. */
class NotifierCreator implements EmulUnit {
    /* 
     * Processes requests.
     * @param request serialized requests under processing.
     */
    public void process(BytePack request) {
        // It registers itself as a requests processor
        new NotifierEmul(request.extract());
    }
}

/* Emulation unit that supports requests for a connection creation. */
class ConnectionCreator implements EmulUnit {
    /* 
     * Processes requests.
     * @param request serialized requests under processing.
     */
    public void process(BytePack request) {
        // It registers itself as a requests processor
        new ConnectionEmul(request.extract());
    }
}

/* 
 * Emulation unit that keeps reference for all available connections and
 * notifiers and passes requests to them. It is singleton that keeps
 * one and the only instance of the registry. 
 */
class ConnRegistry extends EmulUnitCaller {
    /* Keeps the registry instance. */
    private static ConnRegistry instance = new ConnRegistry();
    
    /* Creates instance. */
    private ConnRegistry() {
        callee = new EmulUnit[Const.MAX_CONN];
    }
    
    /* 
     * Retrieves the only instance. 
     * @return (reference to) the only instance.
     */
    static ConnRegistry getInstance() {
        return instance;
    }
    
    /*
     * Registers a newly created connection or notifier in the registry.
     * @param handle handle that identifies the unit to be registered,
     *        must be in a range from 0 to <code>Const.MAX_CONN</code>.
     * @param unit either connection or notifier to be registered.
     */
    void register(int handle, EmulUnit unit) {
        callee[handle] = unit;
    }
    
    /* 
     * Removes connection or notifier with given handle form the registry.
     * @param handle handle that identifies the unit to be removed
     */
    void unregister(int handle) {
        callee[handle] = null;
    }
}

// IMPL_NOTE move to Constants.xml
/* A set of emulation constants. */
class Const {
    /* Size of requests queue. */
    static final int DEFAULT_QUEUE_SIZE = 512;
    
    /* 
     * Maximum amount of connections and notifiers objects supported
     * by emulation. 
     */
    static final int MAX_CONN = 16;
    /* Size of byte representation of Bluetooth address. */
    static final int BTADDR_SIZE = 6;
    /* Size of byte representation of IP address. */
    static final int IP_SIZE = 4;
    /* 
     * Value that indicates send or receive operation failure when passed 
     * as amount of bytes sent or received.
     */
    static final int CONN_FAILURE = -2;
    /* 
     * Value that indicates reaching end of input stream.
     */
    static final int CONN_ENDOF_INP = -1;
} 
