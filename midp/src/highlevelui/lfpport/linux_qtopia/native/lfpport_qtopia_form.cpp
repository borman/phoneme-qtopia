#incllude "lfpport_qtopia_form.h"

extern "C"
{
  MidpError lfpport_form_create(MidpDisplayable *formPtr, const pcsl_string *title, const pcsl_string *tickerText)
  {
    JForm *form = new JForm(JDisplay::current(), formPtr, pcsl_string2QString(title), pcsl_string2QString(tickerText);
    if (!form)
      return KNI_ENOMEM;
    else
      return KNI_OK;
  }

  MidpError lfpport_form_set_content_size(MidpDisplayable *formPtr, int w, int h)
  {
    JForm *form = (JForm *)formPtr->widgetPtr;
    return form->setContentSize(w, h);
  }

  MidpError lfpport_form_set_current_item(MidpItem *itemPtr, int yOffset)
  {
    JForm *form = (JForm *)formPtr->widgetPtr;
    return form->setCurrentItem(itemPtr->widgetPtr, yOffset);
  }

  MidpError lfpport_form_get_scroll_position(int *pos)
  {
    JForm *form = (JForm *)formPtr->widgetPtr;
    *pos = form->getScrollPosition();
    return KNI_OK;
  }

  MidpError lfpport_form_set_scroll_position(int pos)
  {
    JForm *form = (JForm *)formPtr->widgetPtr;
    return form->setScrollPosition(pos);
  }

  MidpError lfpport_form_get_viewport_height(int *height)
  {
    JForm *form = (JForm *)formPtr->widgetPtr;
    *height = form->getViewportHeight();
    return KNI_OK;
  }
}

JForm::JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker)
  : JDisplayable(disp, title, ticker), QWidget(parent)
{
  JDisplay::current()->addWidget(this);

  QVBoxLayout *layout = new QVBoxLayout(this);

  w_title = new QLabel(title, this);
  w_title->setTextFormat(Qt::PlainText);
  w_ticker = new QLabel(ticker, this);
  w_ticker->setTextFormat(Qt::PlainText);

  w_scroller = new QScrollArea(this);
  w_scroller->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  w_viewport = new QWidget(w_scroller);
  w_scroller->setWidget(w_viewport);

  layout->addWidget(w_title);
  layout->addWidget(w_ticker);
  layout->addWidget(w_scroller);

  if (title.isEmpty())
    w_title->hide();
  if (ticker.isEmpty())
    w_ticker->hide();
}

JForm::~JForm()
{
}

int JForm::viewportWidth()
{
  return w_scroller->viewport()->width();
}

int JForm::viewportHeight()
{
  return w_scroller->viewport()->width();
}


MidpError JForm::setCurrentItem(JItem *item, int y_offset)
{
  w_scroller->ensureVisible(item->x(), item->y()+y_offset);
  item->setFocus(Qt::OtherFocusReason);
  return KNI_OK;
}

MidpError JForm::setContentSize(int w, int h)
{
  w_viewport->setFixedSize(w, h);
  return KNI_OK;
}

int JForm::getScrollPosition()
{
  return w_scroller->verticalScrollBar()->value();
}

MidpError JForm::setScrollPosition(int pos)
{
  w_scroller->verticalScrollBar()->setValue(pos);
  return KNI_OK;
}

MidpError j_show()
{
  JDisplay::current()->setCurrentWidget(this);
}

MidpError j_hideAndDelete(jboolean onExit)
{
  delete this;
}
