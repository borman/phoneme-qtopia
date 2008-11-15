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

package com.sun.amms.imageprocessor;

import java.util.Vector;
import javax.microedition.amms.control.EffectControl;
import javax.microedition.amms.control.EffectOrderControl;
import javax.microedition.media.MediaException;

public class EffectsContainer implements EffectOrderControl {
    
    public EffectsContainer(Object[] effects) {
        controls = new Vector();

        if (effects != null)
            for (int i = 0; i < effects.length; i++) {
                EffectControl effect = (EffectControl)effects[i];
                if (effect != null)
                    controls.addElement(effect);
            }
    }
    
    /*
     * EffectOrderControl I/F method
     */
    public int setEffectOrder(EffectControl effect, int order) {
        int index = controls.indexOf(effect);
        if (index == -1)
            throw new IllegalArgumentException("Invalid EffectControl");
        /*
         * Although and integer value of "order" is acceptable, 
         * this method converts it to non-negative index in controls[].
         * Thus, effective order is simple index in the range [0..size-1].
         *
         * Attempt to set negative order will be treated as a with to 
         * make given effect first in the chain (put it at containers[0]),
         *
         * Attempt to set big positive order will be treated as a with to 
         * make given effect last in the chain (put it at containers[size-1]).
         */
        synchronized (controls) {
            if (index != order) {
                if (order < 0) {
                    order = 0;
                } else if (order >= controls.size()) {
                    order = controls.size() - 1;
                }
                controls.removeElementAt(index);
                controls.insertElementAt(effect, order);
            }
        }
        return order;
    }

    /*
     * EffectOrderControl I/F method
     */
    public int getEffectOrder(EffectControl effect) {
        int index = controls.indexOf(effect);
        if (index == -1)
            throw new IllegalArgumentException("Invalid EffectControl");
        return index;
    }

    /*
     * EffectOrderControl I/F method
     */
    public EffectControl[] getEffectOrders() {
        EffectControl[] array;
        synchronized (controls) {
            array = new EffectControl[controls.size()];
            for (int i = 0; i < controls.size(); ++i)
                array[i] = (EffectControl)controls.elementAt(i);
        }
        return array;
    }
    
    //array of supported effects
    private Vector controls;
}
