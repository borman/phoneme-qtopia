#include <cstdio>

#include <QWidget>
#include <QPaintEvent>
#include <QMouseEvent>
#include <QString>
#include <QSoftMenuBar>
#include <QPainter>

#include <jdisplay.h>
#include <jgraphics.h>

#include <keymap_input.h>
#include <midpEventUtil.h>
#include <midp_constants_data.h>

#include "lfjport_qtopia_keymap.h"
#include "lfjport_qtopia_pcsl_string.h"
#include "lfjport_qtopia_screen.h"

LFJScreen *LFJScreen::m_screen = NULL;

LFJScreen::LFJScreen(QWidget *parent)
  : QWidget(parent)
{
  setFocusPolicy(Qt::StrongFocus);
  setAttribute(Qt::WA_OpaquePaintEvent, true);
}

LFJScreen::~LFJScreen()
{
}

void LFJScreen::setSoftButtonLabel(int index, const QString &label)
{
  printf("LFJScreen::setSoftButtonLabel(%d, \"%s\")\n", index, label.toUtf8().constData());
  static int key_map[] = {Qt::Key_Back, Qt::Key_Context1};
  int nsoftkeys = sizeof(key_map)/sizeof(int);
  if (index>nsoftkeys-1)
  {
    printf("Wrong softkey number");
    return;
  }
  QSoftMenuBar::setLabel(this, key_map[index], QString::null, label);
}

// Qt events

void LFJScreen::paintEvent(QPaintEvent *event)
{
  printf("LFJScreen::paintEvent()\n");
  QPixmap *backBuffer = JDisplay::current()->backBuffer();
  JGraphics::flush(backBuffer);
  QPainter p(this);
  p.drawPixmap(event->rect().topLeft(), *backBuffer, event->rect());
}

void LFJScreen::resizeEvent(QResizeEvent *event)
{
  JDisplay::current()->setDisplayWidth(event->size().width());
  JDisplay::current()->setDisplayHeight(event->size().height());
}

void LFJScreen::mouseMoveEvent(QMouseEvent *event)
{
  MidpEvent evt;

  MIDP_EVENT_INITIALIZE(evt);

  evt.type = MIDP_PEN_EVENT;
  evt.ACTION = KEYMAP_STATE_DRAGGED;
  evt.X_POS = event->x();
  evt.Y_POS = event->y();

  midpStoreEventAndSignalForeground(evt);
}

void LFJScreen::mousePressEvent(QMouseEvent *event)
{
  MidpEvent evt;

  MIDP_EVENT_INITIALIZE(evt);

  evt.type = MIDP_PEN_EVENT;
  evt.ACTION = KEYMAP_STATE_PRESSED;
  evt.X_POS = event->x();
  evt.Y_POS = event->y();

  midpStoreEventAndSignalForeground(evt);
}

void LFJScreen::mouseReleaseEvent(QMouseEvent *event)
{
  MidpEvent evt;

  MIDP_EVENT_INITIALIZE(evt);


  evt.type = MIDP_PEN_EVENT;
  evt.ACTION = KEYMAP_STATE_RELEASED;
  evt.X_POS = event->x();
  evt.Y_POS = event->y();

  midpStoreEventAndSignalForeground(evt);
}

void LFJScreen::keyPressEvent(QKeyEvent *event)
{
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
}

void LFJScreen::keyReleaseEvent(QKeyEvent *event)
{
  MidpEvent midp_event;
  MIDP_EVENT_INITIALIZE(midp_event);

  if (LFJKeyMap::instance()->map(event->key(), event->text(), midp_event.CHR))
  {
    midp_event.type = MIDP_KEY_EVENT;
    midp_event.ACTION = KEYMAP_STATE_RELEASED;
    midpStoreEventAndSignalForeground(midp_event);
  }
}

// Singleton interface

LFJScreen *LFJScreen::instance()
{
  return m_screen;
}

void LFJScreen::init(QWidget *parent)
{
  if (m_screen)
    return;
  m_screen = new LFJScreen(parent);
}

void LFJScreen::destroy()
{
  if (!m_screen)
    return;
  delete m_screen;
  m_screen = NULL;
}

#include "moc_lfjport_qtopia_screen.cpp"
