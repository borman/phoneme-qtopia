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

#include <QLabel>
#include <QWidget>
#include <QKeyEvent>

#include <japplication.h>
#include <jdisplay.h>

#include <midpPauseResume.h>
#include <midpEventUtil.h>

#include "anc_qtopia_pauseresume.h"

#include <moc_anc_qtopia_pauseresume.cpp>
/**
 * @file
 *
 * Platform dependent native code to handle incoming call.
 */

SuspendDialog::SuspendDialog(QWidget *parent)
    : QWidget(parent){
    setGeometry(40, 100, screen_width, screen_height);

    label = new QLabel(this);
    label->setText("Application is paused.\nClose this dialog to resume.");
    label->setGeometry(0, 10, screen_width, label_height);
    label->show();

    button = new QPushButton(this);
    button->setGeometry((screen_width - button_width)  >> 1, label_height + 10, button_width, button_height);
    button->setText(button_text);
    connect(button, SIGNAL(clicked()), this, SLOT(buttonActivate()));
    button->show();
    this->installEventFilter(this);
}


bool SuspendDialog::close(bool ) {
    JApplication::instance()->resumeMidp();
    return FALSE; // Wait for dismiss callback
}

void SuspendDialog::buttonActivate() {
    pdMidpNotifyResumeAll();
}


// Returns TRUE if this event has been handled
bool SuspendDialog::eventFilter(QObject *obj, QEvent *e) {
    if (((e->type() == QEvent::KeyPress) &&
#ifdef QT_KEYPAD_MODE
         ((QKeyEvent *)e)->key() == Qt::Key_Hangup)) {
#else
         ((QKeyEvent *)e)->key() == Qt::Key_End)) {
#endif
    // Pressing the (x) button means to destroy the
        // foreground MIDlet.
        MidpEvent evt;

        MIDP_EVENT_INITIALIZE(evt);

#if ENABLE_MULTIPLE_ISOLATES
        evt.type = MIDLET_DESTROY_REQUEST_EVENT;
        evt.DISPLAY = gForegroundDisplayId;
        evt.intParam1 = gForegroundIsolateId;
        midpStoreEventAndSignalAms(evt);
#else
        evt.type = DESTROY_MIDLET_EVENT;
        midpStoreEventAndSignalForeground(evt);
#endif
        return TRUE;
    }
    return QWidget::eventFilter(obj, e );
}


static SuspendDialog *suspendDialog;

/**
 * Platform handling code for VM pause notification call.
 */
extern "C"
void pdMidpNotifySuspendAll() {
    if (suspendDialog == NULL) {
        suspendDialog = new SuspendDialog(JDisplay::current());
        suspendDialog->show();
    }
}

/**
 * Platform handling code for VM resume notification call.
 */
extern "C"
void pdMidpNotifyResumeAll() {

    if (suspendDialog != NULL) {
      JApplication::instance()->resumeMidp();
        // Hide and delete
        delete suspendDialog;
        suspendDialog = NULL;
    }
}
