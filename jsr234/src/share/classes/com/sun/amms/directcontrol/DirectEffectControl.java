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

package com.sun.amms.directcontrol;
import javax.microedition.amms.control.*;
import javax.microedition.media.MediaException;

public abstract class DirectEffectControl
    extends DirectAMMSControl
    implements EffectControl
{
    private String[] _Presets = null;

    private native String nGetPreset();
    public String getPreset()
    {
        return nGetPreset();
    }

    private native int nGetNumOfSupportedPresets();
    private native void nGetPresetNames(String[] names);
    public String[] getPresetNames()
    {
        if (_Presets == null)
        {
            int n = nGetNumOfSupportedPresets();
            if (n <= 0)
            {
                _Presets = new String[0];
            }
            else
            {
                _Presets = new String[n];
                nGetPresetNames(_Presets);
            }
        }
        return _Presets;
    }

    private native int nGetScope();
    public int getScope()
    {
        return nGetScope();
    }

    private native boolean nIsEnabled();
    public boolean isEnabled()
    {
        return nIsEnabled();
    }

    private native boolean nIsEnforced();
    public boolean isEnforced()
    {
        return nIsEnforced();
    }

    private native void nSetEnabled(boolean enable);
    public void setEnabled(boolean enable)
    {
        nSetEnabled(enable);
    }

    private native void nSetEnforced(boolean enforced);
    public void setEnforced(boolean enforced)
    {
        nSetEnforced(enforced);
    }

    private native void nSetPreset(String preset_name);
    public void setPreset(String preset)
    {
        if (preset == null)
        {
            throw new IllegalArgumentException("Cannot set a null preset");
        }

        if (preset.length() < 1)
        {
            throw new IllegalArgumentException("Invalid preset string");
        }

        if (_Presets == null)
        {
            getPresetNames();
        }

        boolean available = false;
        for (int i = 0; i < _Presets.length; i++)
        {
            if (preset.equals(_Presets[i]))
            {
                available = true;
                break;
            }
        }

        if (!available)
        {
            throw new IllegalArgumentException(
                    "The preset is not avalable to set");
        }
        nSetPreset(preset);
    }

    private native void nSetScope(int scope) throws MediaException;
    public void setScope(int scope) throws MediaException
    {
        nSetScope(scope);
    }

    /*------------------------------------------------------------*/

    private int _ectl_vtbl;

    /** this method must be overrided to
     *  provide valid native ectl_vtbl pointer.
     *  See jsr234_effect_control.h
     *  and KNIDirectEffectControl.c
     */

    abstract protected int GetEctlVtbl();

    DirectEffectControl()
    {
        _ectl_vtbl = GetEctlVtbl();
    }
}
