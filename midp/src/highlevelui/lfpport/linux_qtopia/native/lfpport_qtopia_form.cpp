#include <QVBoxLayout>
#include <QScrollBar>
#include <QEvent>
#include <QResizeEvent>

#include <jdisplay.h>
#include <lfpport_error.h>
#include <lfpport_form.h>
#include <lfpport_item.h>
#include <midpEventUtil.h>

#include "lfpport_qtopia_pcsl_string.h"
#include "lfpport_qtopia_item.h"
#include "lfpport_qtopia_form.h"
#include "lfpport_qtopia_debug.h"

JForm *JForm::currentForm = NULL;

extern "C"
{
  MidpError lfpport_form_create(MidpDisplayable *formPtr, const pcsl_string *title, const pcsl_string *ticker)
  {
    JForm *form = new JForm(JDisplay::current(), formPtr,
                            pcsl_string2QString(*title), pcsl_string2QString(*ticker));

    lfpport_log("Create Form");
    if (!form)
      return KNI_ENOMEM;
    else
      return KNI_OK;
  }

  MidpError lfpport_form_set_content_size(MidpDisplayable *formPtr, int w, int h)
  {
    JForm *form = (static_cast<JDisplayable *>(formPtr->frame.widgetPtr))->toForm();
    return form->setContentSize(w, h);
  }

  MidpError lfpport_form_set_current_item(MidpItem *itemPtr, int yOffset)
  {
    lfpport_log("lfpport_form_set_current_item(...)\n");
    JForm *form = JForm::current();
    if (form)
      return form->setCurrentItem(qobject_cast<JItem*>(static_cast<QObject *>(itemPtr->widgetPtr)), yOffset);
    else
      return KNI_OK;
  }

  MidpError lfpport_form_get_scroll_position(int *pos)
  {
    JForm *form = JForm::current();
    if (form)
      *pos = form->getScrollPosition();
    lfpport_log("lfpport_form_get_scroll_position -> %d\n", *pos);
    return KNI_OK;
  }

  MidpError lfpport_form_set_scroll_position(int pos)
  {
    JForm *form = JForm::current();
    lfpport_log("lfpport_form_set_scroll_position(%d)\n", pos);
    if (form)
      return form->setScrollPosition(pos);
    else
      return KNI_OK;
  }

  MidpError lfpport_form_get_viewport_height(int *height)
  {
    JForm *form = JForm::current();
    if (form)
      *height = form->viewportHeight();
    lfpport_log("Viewport height reported %d\n", *height);
    return KNI_OK;
  }
}

JForm::JForm(QWidget *parent, MidpDisplayable *disp, QString title, QString ticker)
  : JDisplayable(disp, title, ticker), QWidget(parent)
{
  form = this;
  lfpport_log("JForm constructor");
  JDisplay::current()->addWidget(this);

  QVBoxLayout *layout = new QVBoxLayout(this);

  w_ticker = new QLabel(ticker, this);
  w_ticker->setTextFormat(Qt::PlainText);

  w_scroller = new QScrollArea(this);
  //w_scroller->setFrameStyle(QFrame::Plain | QFrame::StyledPanel);
  w_scroller->setFrameStyle(QFrame::NoFrame);
  w_scroller->setFocusPolicy(Qt::NoFocus);
  w_scroller->viewport()->installEventFilter(this);
  w_scroller->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  w_viewport = new QWidget(w_scroller->viewport());
  w_viewport->resize(30, 30);
  w_scroller->setWidget(w_viewport);
  w_scroller->setWidgetResizable(true);

  layout->addWidget(w_ticker);
  layout->addWidget(w_scroller);

  javaTitleChanged();
  javaTickerChanged();
}

JForm::~JForm()
{
}

JForm *JForm::current()
{
  if (!currentForm)
    lfpport_log("!!! INVALID CURRENT FORM\n");
  return currentForm;
}

int JForm::viewportWidth()
{
  return w_scroller->viewport()->width();
}

int JForm::viewportHeight()
{
  return w_scroller->viewport()->height();
}


MidpError JForm::setCurrentItem(JItem *item, int y_offset)
{
  if (!item->hasFocus())
  {
    w_scroller->ensureVisible(0, item->y()+y_offset);
    item->setFocus(Qt::OtherFocusReason);
  }
  return KNI_OK;
}

MidpError JForm::setContentSize(int w, int h)
{
  lfpport_log("JForm::setContentSize(%d, %d)\n", w, h);
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

MidpError JForm::j_show()
{
  JDisplay::current()->setCurrentWidget(this);
  currentForm = this;
  return KNI_OK;
}

MidpError JForm::j_hideAndDelete(jboolean onExit)
{
  delete this;
  if (currentForm==this)
    currentForm = NULL;
  return KNI_OK;
}

void JForm::javaTickerChanged()
{
  if (ticker().isEmpty())
    w_ticker->hide();
  else
  {
    w_ticker->setText(ticker());
    if (w_ticker->isHidden())
      w_ticker->show();
  }
}

void JForm::showEvent(QShowEvent *event)
{
  lfpport_log("JForm::showEvent\n");
  javaTitleChanged();
}

bool JForm::eventFilter(QObject *watched, QEvent *event)
{
  if (watched==w_scroller->viewport() && event->type()==QEvent::Resize)
  {
    QResizeEvent *rev = static_cast<QResizeEvent *>(event);
    lfpport_log("Form viewport resized to (%d[%d: -%d] x %d)\n", 
                 rev->size().width(), w_scroller->verticalScrollBar()->isVisible(), w_scroller->verticalScrollBar()->width(),
                 rev->size().height());
                 
    int v_w = rev->size().width();
    if (w_scroller->verticalScrollBar()->isVisible())
      v_w -= w_scroller->verticalScrollBar()->width();
    int v_h = rev->size().height();
    JDisplay::current()->setDisplayWidth(v_w);
    JDisplay::current()->setDisplayHeight(v_h); 
    requestInvalidate();
  }
  return false;
}

#include "lfpport_qtopia_form.moc"
