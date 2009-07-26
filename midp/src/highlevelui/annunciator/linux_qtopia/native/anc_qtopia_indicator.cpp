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
 *
 * This source file is specific for Qt-based configurations.
 */

#include <kni.h>
#include <midpStorage.h>
#include <anc_indicators.h>
#include <anc_qtopia_indicator.h>

#include <QtDebug>

/**
 * @file
 *
 * Native code to handle indicator status.
 */

/**
 * Platform handling code for turning off or on
 * indicators for signed MIDlet.
 *
 * IMPL_NOTE:Currently indicator does nothing for Java
 * and platform widget modules as we are waiting for
 * UI input.
 */
extern "C"
void anc_show_trusted_indicator(jboolean isTrusted) {
    qDebug("STUB: anc_show_trusted_indicator(%d)", isTrusted);
}

/**
 * Porting implementation for network indicator.
 * It controls the LED as the network indicator, it
 * ONLY works on device. There is no equivalent in emulator.
 */
extern "C"
void anc_set_network_indicator(AncNetworkIndicatorState status) {
    qDebug("STUB: anc_set_network_indicator(%d)", status);
}

/**
 * Implement home icon on/off porting interface.
 */
extern "C"
void anc_toggle_home_icon(jboolean isHomeOn) {
    qDebug("STUB: anc_toggle_home_icon(%d)", isHomeOn);
}

/**
 *  Turn on or off the backlight, or toggle it.
 *  The backlight will be turned on to the system configured level.
 *  This function is only valid if QT's COP and QWS is available.
 *
 *  @param mode if <code>mode</code> is:
 *              <code>ANC_BACKLIGHT_ON</code> - turn on the backlight
 *              <code>ANC_BACKLIGHT_OFF</code> - turn off the backlight
 *              <code>ANC_BACKLIGHT_TOGGLE</code> - toggle the backlight
 *              <code>ANC_BACKLIGHT_SUPPORTED<code> - do nothing
 *              (this is used to determine if backlight control is
 *              supported on a system without  changing the state of
 *              the backlight.)
 *  @return <code>KNI_TRUE</code> if the system supports backlight
 *              control, or <code>KNI_FALSE</code> otherwise.
 */
extern "C" jboolean
anc_show_backlight(AncBacklightState mode) {
    (void)mode;
    qDebug("STUB: anc_show_backlight(%d)", mode);
    return KNI_FALSE;
}

