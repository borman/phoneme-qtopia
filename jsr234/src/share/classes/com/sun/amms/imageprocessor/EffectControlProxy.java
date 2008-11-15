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

import javax.microedition.media.MediaException;
import javax.microedition.amms.control.EffectControl;
import com.sun.amms.imageprocessor.NativeImageFilter;

public abstract class EffectControlProxy extends SimpleImageFilter 
        implements EffectControl {
    
    private boolean  _isEnabled = false;
    private int      _scope;
    private boolean  _isEnforced;
    private String   _curPreset;
    private String[] _Presets;
    private NativeImageFilterHandle _IFhandle = null;
    
    private static native String[] nGetSupportedPresets(int filterHandle);
    private static native void nSetPreset(int filterHandle, String presetName);
    
    final protected void setEffectFilterHandle(NativeImageFilterHandle filterHandle) {
        _IFhandle = filterHandle;
        String[] presets;
        synchronized (_IFhandle) {
            presets = nGetSupportedPresets(_IFhandle.getRawHandle());
        }
        if (presets == null)
            presets = new String[0];
        _Presets = presets;
    }
    
    protected EffectControlProxy() {
        _isEnabled = false;
        _isEnforced = false;
        _curPreset = null;
        
        _Presets = new String[0];
    }
    
    public void setEnabled(boolean enable) {
        _isEnabled = enable;
    }
    
    public boolean isEnabled() {
        return _isEnabled;
    }
    
    public void setScope(int scope) throws MediaException {
        if (scope != SCOPE_RECORD_ONLY)
            throw new MediaException("Wrong scope " + scope);
        
        _scope = scope;
    }
    
    public int getScope() {
        return SCOPE_RECORD_ONLY;
    }
    
    public void setEnforced(boolean enforced) {
        _isEnforced = enforced;
    }
    
    public boolean isEnforced() {
        return _isEnforced;
    }
    
    public synchronized void setPreset(String preset) {
        if (preset == null)
            throw new IllegalArgumentException("Proposed preset is null");
        
        if (_IFhandle == null)
            throw new IllegalArgumentException
                    ("Preset " + preset + " is not supported ");
        else {
            synchronized (_IFhandle) {
                nSetPreset(_IFhandle.getRawHandle(), preset);
                _curPreset = preset;
            }
        }
    }
    
    public String getPreset() {
        return _curPreset;
    }
    
    public String[] getPresetNames() {
        return _Presets;
    }
    
    NativeImageFilterHandle[] getFilterHandles() {
        NativeImageFilterHandle filterHandle = _IFhandle;
        if (filterHandle == null)
            return new NativeImageFilterHandle[0];
        else {
            return new NativeImageFilterHandle[]{ filterHandle };
        }
    }
    
}
