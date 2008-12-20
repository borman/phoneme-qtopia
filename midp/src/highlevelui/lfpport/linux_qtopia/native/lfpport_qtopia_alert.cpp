#include <QtopiaApplication>
#include <QRect>
#include <QLabel>
#include <QVBoxLayout>

#include <jdisplay.h>
#include <jgraphics.h>
#include <lfpport_alert.h>

#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_alert.h"
#include "lfpport_qtopia_command.h"
#include "lfpport_qtopia_debug.h"

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
    JAlert *alert = (JAlert *)alertPtr->frame.widgetPtr;
    QRect gaugeRect;
    if (gaugeBounds)
      gaugeRect.setCoords(gaugeBounds[0], gaugeBounds[1], gaugeBounds[2], gaugeBounds[3]);
    alert->j_setContents(JGraphics::immutablePixmap(imgPtr), gaugeRect, pcsl_string2QString(*text));
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
    JCommandManager::instance()->setAlertCommands((JAlert *)alertPtr->widgetPtr, cmds, numCmds);
    return KNI_OK;
  }
}

JAlert::JAlert(QWidget *parent, MidpDisplayable *alertDisp, QString title, QString ticker, MidpComponentType type)
  : QDialog(parent), JDisplayable(alertDisp, title, ticker)
{
  (void)ticker;
  
  alertDisp->frame.widgetPtr = this;
  
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

MidpError JAlert::j_setContents(const QPixmap *image, const QRect &gauge, const QString &text)
{
  (void)image;
  (void)gauge;
  label->setText(text);
  return KNI_OK;
}

#include "lfpport_qtopia_alert.moc"
