#include <QtopiaApplication>
#include <QRect>
#include <QLabel>
#include <QVBoxLayout>
#include <QDebug>
#include <jdisplay.h>
#include <jimmutableimage.h>
#include <lfpport_alert.h>

#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_alert.h"
#include "lfpport_qtopia_command.h"
#include "lfpport_qtopia_debug.h"

#include <cstdlib>

extern "C"
{
  MidpError lfpport_alert_create(MidpDisplayable* alertPtr,
                                 const pcsl_string* title,
                                 const pcsl_string* ticker,
                                 MidpComponentType alertType)
  {
    debug_trace();
    JAlert *alert = new JAlert(JDisplay::current(), alertPtr,
                               pcsl_string2QString(*title), pcsl_string2QString(*ticker),
                               alertType);
    if (!alert)
      return KNI_ENOMEM;
    return KNI_OK;
  }

  MidpError lfpport_alert_set_contents(MidpDisplayable* alertPtr,
                                       unsigned char* imgPtr,
                                       int* gaugeBounds,
                                       const pcsl_string* text)
  {
    debug_trace();
    qDebug("lfpport_alert_set_contents");
    JAlert *alert = static_cast<JDisplayable *>(alertPtr->frame.widgetPtr)->toAlert();
    QRect gaugeRect;
    if (gaugeBounds)
      gaugeRect.setCoords(gaugeBounds[0], gaugeBounds[1], gaugeBounds[2], gaugeBounds[3]);
    alert->j_setContents(JIMMutableImage::fromHandle(imgPtr), gaugeRect, gaugeBounds, pcsl_string2QString(*text));
    return KNI_OK;
  }

  MidpError lfpport_alert_need_scrolling(jboolean* needScrolling, MidpDisplayable* alertPtr)
  {
    debug_trace();
    *needScrolling = true;
    return KNI_OK;
  }

  MidpError lfpport_alert_set_commands(MidpFrame* alertPtr, MidpCommand* cmds, int numCmds)
  {
    debug_trace();
    JAlert *alert = static_cast<JDisplayable *>(alertPtr->widgetPtr)->toAlert();
    JCommandManager::instance()->setAlertCommands(alert, cmds, numCmds);
    return KNI_OK;
  }
}

JAlert::JAlert(QWidget *parent, MidpDisplayable *alertDisp, QString title, QString ticker, MidpComponentType type)
  : QDialog(parent), JDisplayable(alertDisp, title, ticker)
{
  (void)ticker;

  m_alertPeer = this;

  qDebug("JAlert created");
  QVBoxLayout *layout = new QVBoxLayout(this);
  label = new QLabel("", this);
  layout->addWidget(label);
  setWindowTitle(title);
}

JAlert::~JAlert()
{
}

MidpError JAlert::j_show()
{
  QtopiaApplication::showDialog(this);
  return KNI_OK;
}

MidpError JAlert::j_hideAndDelete(jboolean onExit)
{
  delete this;
  return KNI_OK;
}

MidpError JAlert::j_setContents(const QPixmap *image, const QRect &gauge, int *gaugeBounds, const QString &text)
{
  (void)image;
  (void)gauge;
  (void)gaugeBounds;
  qDebug() << "Alert text: " << text;
  label->setText(text);
  return KNI_OK;
}

#include "lfpport_qtopia_alert.moc"
