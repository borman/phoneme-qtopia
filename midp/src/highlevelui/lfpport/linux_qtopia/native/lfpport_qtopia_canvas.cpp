#include <QPainter>
#include <QPaintEvent>
#include <QResizeEvent>

#include <lfpport_error.h>
#include <lfpport_canvas.h>
#include <jdisplay.h>
#include <keymap_input.h>
#include <midpEventUtil.h>
#include <midp_constants_data.h>

#include "lfpport_qtopia_canvas.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

#include <cstdlib>

// MIDP interface for the JCanvas class
extern "C"
{
  MidpError lfpport_canvas_create(MidpDisplayable* canvasPtr, const pcsl_string* title, const pcsl_string* ticker)
  {
    debug_trace();
    JCanvas *canvas = new JCanvas(JDisplay::current(), canvasPtr,
                                  pcsl_string2QString(*title), pcsl_string2QString(*ticker));
    if (canvas)
      return KNI_OK;
    else
      return KNI_ENOMEM;
  }
}

JCanvas::JCanvas(QWidget *parent, MidpDisplayable *canvasPtr, QString title, QString ticker)
  :JDisplayable(canvasPtr, title, ticker), QWidget(parent)
{
  JDisplay *disp = JDisplay::current();
  disp->setDisplayWidth(disp->width());
  disp->setDisplayHeight(disp->height());
  printf("JCanvas constructor\n");
  JDisplay::current()->addWidget(this);

  setAttribute(Qt::WA_OpaquePaintEvent, true);
}

JCanvas::~JCanvas()
{
}

MidpError JCanvas::j_show()
{
  JDisplay::current()->setCurrentWidget(this);
}

MidpError JCanvas::j_hideAndDelete(jboolean onExit)
{
  delete this;
}

void JCanvas::paintEvent(QPaintEvent *ev)
{
  QPainter p(this);
  p.drawPixmap(ev->rect(), *(JDisplay::current()->backBuffer()), ev->rect());
}

void JCanvas::resizeEvent(QResizeEvent *ev)
{
  lfpport_log("JCanvas resized to (%dx%d)\n", width(), height());
  JDisplay::current()->setDisplayWidth(width());
  JDisplay::current()->setDisplayHeight(height());
  requestInvalidate();
}

void JCanvas::mouseMoveEvent(QMouseEvent *event)
{
  MidpEvent evt;

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
#if 0
  MidpEvent midp_event;
  MIDP_EVENT_INITIALIZE(midp_event);

  if (LFJKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = event->isAutoRepeat()?(KEYMAP_STATE_REPEATED):(KEYMAP_STATE_PRESSED);
    midpStoreEventAndSignalForeground(midp_event);
  }
  /*
  else if (!event->text().isEmpty())
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = KEYMAP_STATE_IME;
    QString2pcsl_string(event->text(), midp_event.stringParam1);
    midpStoreEventAndSignalForeground(midp_event);
  }
  */
#endif
}

void JCanvas::keyReleaseEvent(QKeyEvent *event)
{
  /*
  MidpEvent midp_event;
  MIDP_EVENT_INITIALIZE(midp_event);

  if (LFJKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = KEYMAP_STATE_RELEASED;
    midpStoreEventAndSignalForeground(midp_event);
  }
  */
}

void JCanvas::showEvent(QShowEvent *event)
{
  lfpport_log("JCanvas::showEvent\n");
  javaTitleChanged();
}

#include "lfpport_qtopia_canvas.moc"
