#include <jdisplay.h>
#include <midpEventUtil.h>

#include "lfpport_qtopia_displayable.h"
#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_debug.h"

// MIDP interface for the JDisplayable class
extern "C"
{
  MidpError jdisplayable_show(MidpFrame *screenPtr)
  {
    JDisplayable *widget = static_cast<JDisplayable *>(screenPtr->widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      lfpport_log("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->j_show();
  }

  MidpError jdisplayable_hideAndDelete(MidpFrame *screenPtr, jboolean onExit)
  {
    JDisplayable *widget = static_cast<JDisplayable *>(screenPtr->widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      lfpport_log("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->j_hideAndDelete(onExit);
  }

  MidpError jdisplayable_setTitle(MidpDisplayable *screenPtr, const pcsl_string *text)
  {
    JDisplayable *widget = static_cast<JDisplayable *>(screenPtr->frame.widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      lfpport_log("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->setTitle(pcsl_string2QString(*text));
  }

  MidpError jdisplayable_setTicker(MidpDisplayable *screenPtr, const pcsl_string *text)
  {
    JDisplayable *widget = static_cast<JDisplayable *>(screenPtr->frame.widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      lfpport_log("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->setTicker(pcsl_string2QString(*text));
  }
}

//JDisplayable implementation
JDisplayable::JDisplayable(MidpDisplayable *disp, QString title, QString ticker)
: form(NULL), m_disp(disp)
{
  lfpport_log("JDisplayable 0x%08x initialised\n", disp);
  disp->frame.widgetPtr = this; // THIS IS NOT A WIDGET
  disp->frame.show = jdisplayable_show;
  disp->frame.hideAndDelete = jdisplayable_hideAndDelete;
  disp->frame.handleEvent = NULL; // QT event handling is used
  disp->setTicker = jdisplayable_setTicker;
  disp->setTitle = jdisplayable_setTitle;
  
  debug_dumpdisp(disp);

  m_title = title;
  m_ticker = ticker;
}

JDisplayable::~JDisplayable()
{
}

MidpError JDisplayable::setTitle(const QString &text)
{
  if (text!=m_title)
  {
    m_title = text;
    javaTitleChanged();
  }
  return KNI_OK;
}

MidpError JDisplayable::setTicker(const QString &text)
{
  if (text!=m_ticker)
  {
    m_ticker = text;
    javaTickerChanged();
  }
  return KNI_OK;
}

QString JDisplayable::title() const
{
  return m_title;
}

QString JDisplayable::ticker() const
{
  return m_ticker;
}

JForm *JDisplayable::toForm() const
{
  return form;
}

MidpDisplayable *JDisplayable::toMidpDisplayable() const
{
  return m_disp;
}

void JDisplayable::javaTitleChanged()
{
  if (title().isEmpty())
    JDisplay::current()->setWindowTitle("phoneME");
  else
    JDisplay::current()->setWindowTitle(title());
}

void JDisplayable::requestInvalidate()
{
  MidpEvent evt;
  MIDP_EVENT_INITIALIZE(evt);
  evt.type = MIDP_REQUEST_INVALIDATE_EVENT;
  midpStoreEventAndSignalForeground(evt);
}

#include "lfpport_qtopia_displayable.moc"
