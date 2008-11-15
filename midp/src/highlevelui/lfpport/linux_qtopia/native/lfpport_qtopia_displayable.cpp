#include "lfpport_qtopia_displayable.h"
#include "lfpport_qtopia_pcsl_string.h"

// MIDP interface for the JDisplayable class
extern "C"
{
  MidpError jdisplayable_show(MidpFrame *screenPtr)
  {
    JDisplayable *widget = (JDisplayable *)(screenPtr->widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      printf("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->j_show();
  }

  MidpError jdisplayable_hideAndDelete(MidpFrame *screenPtr, jboolean onExit)
  {
    JDisplayable *widget = (JDisplayable *)(screenPtr->widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      printf("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->j_hideAndDelete(onExit);
  }

  MidpError jdisplayable_setTitle(MidpDisplayable *screenPtr, const pcsl_string *text)
  {
    JDisplayable *widget = (JDisplayable *)(screenPtr->frame.widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      printf("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->setTitle(pcsl_string2QString(*text));
  }

  MidpError jdisplayable_setTicker(MidpDisplayable *screenPtr, const pcsl_string *text)
  {
    JDisplayable *widget = (JDisplayable *)(screenPtr->frame.widgetPtr);
#ifdef DEBUG
    if (!(widget) || !widget->inherits("JDisplayable"))
    {
      printf("ERROR: invalid displayable\n");
      return KNI_EINVAL;
    }
#endif
    return widget->setTicker(pcsl_string2QString(*text));
  }
}

//JFrame implementation
JDisplayable::JDisplayable(MidpDisplayable *disp, QString title, QString ticker)
{
  disp->frame.widgetPtr = this;
  disp->frame.show = jdisplayable_show;
  disp->frame.hideAndDelete = jdisplayable_hideAndDelete;
  disp->frame.handleEvent = NULL; // QT event handling is used
  disp->setTicker = jdisplayable_setTicker;
  disp->setTitle = jdisplayable_setTitle;

  m_title = title;
  m_ticker = ticker;
}

JDisplayable::~JDisplayable()
{
}

JDisplayable::setTitle(const QString &text)
{
  if (text!=m_title)
  {
    m_title = text;
  }
}

JDisplayable::setTicker(const QString &text)
{
  if (text!=m_ticker)
  {
    m_ticker = text;
  }
}

QString JDisplayable::title() const
{
  return m_title;
}

QString JDisplayable::ticker() const
{
  return m_ticker;
}
