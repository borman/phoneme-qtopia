/*
 *
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
 *
 * This source file is specific for Qtopia4-based configurations.
 * Email: trollsid@gmail.com
 */
#include <QPainter>
#include <QPaintEvent>
#include <QResizeEvent>

#include <lfpport_error.h>
#include <lfpport_canvas.h>
#include <jdisplay.h>
#include <jmutableimage.h>
#include <keymap_input.h>
#include <midpEventUtil.h>
#include <midp_constants_data.h>

#include "lfpport_qtopia_canvas.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

#include <jkey.h>
#include <QKeyEvent>
#include <cstdlib>

// MIDP interface for the JCanvas class
extern "C"
{
  MidpError lfpport_canvas_create(MidpDisplayable* canvasPtr, const pcsl_string* title, const pcsl_string* ticker)
  {
    debug_trace();
    qDebug() << "Create canvas";
    JCanvas *canvas = new JCanvas(JDisplay::current(), canvasPtr,
                                  pcsl_string2QString(*title), pcsl_string2QString(*ticker));
    canvas->j_show();
    if (canvas)
      return KNI_OK;
    else
      return KNI_ENOMEM;
  }
}

//extern "C"
//{
//    MidpError canvas_show(MidpFrame* screenPtr)
//    {
//        qDebug() << "canvas_show";
//        JCanvas *canvas = static_cast<JCanvas *>(screenPtr->widgetPtr);
//        canvas->resize(JDisplay::current()->displayWidth(), JDisplay::current()->displayHeight());
//        canvas->j_show();
//        return KNI_OK;
//    }
//}

JCanvas::JCanvas(QWidget *parent, MidpDisplayable *canvasPtr, QString title, QString ticker)
  :JDisplayable(canvasPtr, title, ticker), QWidget(parent)
{
    //m_disp = canvasPtr;
    JDisplay *disp = JDisplay::current();
    disp->setDisplayWidth(disp->width());
    disp->setDisplayHeight(disp->height());
    JDisplay::current()->addWidget(this);

    setAttribute(Qt::WA_OpaquePaintEvent, true);
    setAttribute(Qt::WA_PaintOnScreen, true);

    setFocusPolicy(Qt::StrongFocus);
}

JCanvas::~JCanvas()
{
}

MidpError JCanvas::j_show()
{
  qDebug() << "Show canvas";
  JDisplay::current()->setCurrentWidget(this);
}

MidpError JCanvas::j_hideAndDelete(jboolean onExit)
{
  deleteLater();
  return KNI_OK;
}

void JCanvas::paintEvent(QPaintEvent *event)
{
  JMutableImage *backBuffer = JMutableImage::fromHandle(NULL);
  backBuffer->flush();
  QPainter p(this);
  p.drawImage(event->rect().topLeft(), *backBuffer, event->rect());
}

void JCanvas::resizeEvent(QResizeEvent *)
{
  qDebug("JCanvas resized to (%dx%d)", width(), height());
  JDisplay::current()->setDisplayWidth(width());
  JDisplay::current()->setDisplayHeight(height());
  requestInvalidate();
}

void JCanvas::mouseMoveEvent(QMouseEvent *event)
{
  MidpEvent evt;
//  qDebug() << "X:"<< event->x()<<"  Y:" << event->y();
  MIDP_EVENT_INITIALIZE(evt);

  evt.type = MIDP_PEN_EVENT;
  evt.ACTION = KEYMAP_STATE_DRAGGED;
  evt.X_POS = event->x();
  evt.Y_POS = event->y();

  midpStoreEventAndSignalForeground(evt);
}

void JCanvas::mousePressEvent(QMouseEvent *event)
{
  MidpEvent evt;
//  qDebug() << "X:"<< event->x()<<"  Y:" << event->y();
  MIDP_EVENT_INITIALIZE(evt);
  evt.type = MIDP_PEN_EVENT;
  evt.ACTION = KEYMAP_STATE_PRESSED;
  evt.X_POS = event->x();
  evt.Y_POS = event->y();

  midpStoreEventAndSignalForeground(evt);
}

void JCanvas::mouseReleaseEvent(QMouseEvent *event)
{
  MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);
    evt.type = MIDP_PEN_EVENT;
    evt.ACTION = KEYMAP_STATE_RELEASED;
    evt.X_POS = event->x();
    evt.Y_POS = event->y();

    midpStoreEventAndSignalForeground(evt);
}

void JCanvas::keyPressEvent(QKeyEvent *event)
{
    MidpEvent midp_event;
    MIDP_EVENT_INITIALIZE(midp_event);
    if(LFPKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
    {
        midp_event.type = MIDP_KEY_EVENT;
        midp_event.ACTION = event->isAutoRepeat()?(KEYMAP_STATE_REPEATED):(KEYMAP_STATE_PRESSED);
        midpStoreEventAndSignalForeground(midp_event);
    }
}

void JCanvas::keyReleaseEvent(QKeyEvent *event)
{
    MidpEvent midp_event;
    MIDP_EVENT_INITIALIZE(midp_event);

    if(LFPKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
    {
        midp_event.type = MIDP_KEY_EVENT;
        midp_event.ACTION = KEYMAP_STATE_RELEASED;
        midpStoreEventAndSignalForeground(midp_event);
    }
}

void JCanvas::showEvent(QShowEvent *event)
{
  qDebug("JCanvas::showEvent");
  javaTitleChanged();
}

#include "lfpport_qtopia_canvas.moc"